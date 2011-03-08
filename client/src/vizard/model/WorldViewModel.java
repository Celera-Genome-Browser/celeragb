/*
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 Copyright (c) 1999 - 2006 Applera Corporation.
 301 Merritt 7 
 P.O. Box 5435 
 Norwalk, CT 06856-5435 USA

 This is free software; you can redistribute it and/or modify it under the 
 terms of the GNU Lesser General Public License as published by the 
 Free Software Foundation; version 2.1 of the License.

 This software is distributed in the hope that it will be useful, but 
 WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE. 
 See the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License 
 along with this software; if not, write to the Free Software Foundation, Inc.
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
*/
package vizard.model;

import vizard.util.ObserverList;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;


/**
 * The purpose of the WorldViewModel class is to provide an observable model
 * of a rectangular view in a rectangular world.
 * The view is mapped on the "window" of an AWT component.
 *
 * INTRODUCTION
 *
 * @todo Replace the empty talk below with something real. The real constraints are:
 *       - the view must not be bigger than the world
 *         (would be incompatible with swing scrollbar models)
 *       - The visualization must be in sync with the origin of the view.
 *       - the visualization must be in sync with the scale==viewSize/windowSize.
 *       - scrollbar(s) must be in sync with view/world ratios.
 *       - a zoom-bar(s) must be in sync with the scale
 *       - and vice/versa for all the above.
 *       - the center-of-zoom must always be in the view.
 *
 * The visualization of a two dimensional model must take into account
 * the following three geometries:
 *   1. the model coordinates (eg, genomic axis)
 *   2. the coordinates of the viewed part of the model (eg, visible range)
 *   3. the size of the window where the view is painted.
 * Any of these 3 geometries can change. For example:
 *   1. a new tier is shown (world becomes bigger along the Y axis)
 *   2. the user scrolls along the genomic axis (the view origin is changed)
 *   3. the user resizes the window.
 *
 * PROBLEM
 * These 3 geometries are related and it is a painful programming task to
 * keep them in sync. In addition to these 3 geometries, it is likely that
 * the application will show some scroll bars that must also be kept in sync.
 *
 * SOLUTION
 * The WorldViewModel is composed of a rectangular world,
 * of a rectangular view, and of a reference to a component (the window).
 * Whenever anyone of these changes, the WorldViewModel keeps the others
 * in sync.
 *
 * In addition, the WorldViewModel accepts two BoundedRangeModels:
 * one for scrolling and one for zooming.
 * If these are given, for example, to a JScrollBar and to a JSlider,
 * then the scrollbar and the slider are kept in sync with the
 * WorldViewModel (and vice versa).
 *
 * The WorldViewModel only handles one axis, X or Y.
 * What happens on one axis does not impact the other.
 * A typical application will use one WorldViewModel for the X axis
 * and one for the Y axis.
 *
 * EXAMPLE
 * The code example below keeps in sync:
 * - a transform glyph so that its subtree of glyphs can "work" in world coords.
 * - a horizontal scroll bar showing the viewed part of the axis.
 * - a vertical scroll bar showing the viewed part of the tiers.
 * - a zoom slider showing the current scale.
 *
 *     WorldViewModel axisViewModel =
 *         new WorldViewModel(true, //ie, X-axis
 *                            0,    //world origin
 *                            axis.baseCount(), //world extent
 *                            16);  //maximum pixels per base (= max zoom)
 *
 *     WorldViewGlyphComponent tierListView =
 *         new WorldViewGlyphComponent(axisViewModel, //along X-axis
 *                                     null); //along Y-axis
 *
 *     WorldViewTransformGlyph worldViewTransformGlyph =
 *         new WorldViewTransformGlyph(axisViewModel, //along X-axis
 *                                     tierListView.yAxisModel());
 *
 *     JScrollBar axisScrollBar = ...;
 *     axisScrollBar.setBoundedRangeModel(axisViewModel.scrollModel());
 *
 *     JScrollBar verticalScrollBar = ...;
 *     verticalScrollbar.setBoundedRangeModel(tierListView.verticalModel());
 *
 *     JSlider zoomSlider = ...;
 *     zoomSlider.setBoundedRangeModel(axisViewModel.zoomModel());
 *
 *     axisViewModel.setWindow(tierListView);
 *     axisViewModel.EXTEND_WORLD = false; //zoom the view instead
 *
 *     tierListView.root().addChild(worldViewTransformGlyph);
 *
 * In the same way, additional components can be added to show, for example,
 * another list of tiers, or the axis itself. By reusing the same model,
 * they will be kept in sync as well.
 *
 * @see WorldViewGlyphComponent
 * @see WorldViewTransformGlyph
 */
public class WorldViewModel
    implements ComponentListener,
	       ChangeListener
{
    private final static int RANGE_MAX = Integer.MAX_VALUE/3;

    private double worldOrigin, worldSize;
    private double origin;
    private double scale; // world units per pixel
    private double minScale; //never zooms more than that
    private Component window;
    private boolean isHorizontal;
    private double zoomCenter;

    private BoundedRangeModel scrollModel = new DefaultBoundedRangeModel();
    private BoundedRangeModel zoomModel = new DefaultBoundedRangeModel();
    private int lastExtent;
    private boolean isMaster;

    /**
     * This is one of the 3 constants that define the behavior when the view
     * gets bigger than the world.
     *
     * GLUE_START means that the view and the world will be constrainted
     * to have the same origin.
     */
    public static final int GLUE_START = 0;

    /**
     * This is one of the 3 constants that define the behavior when the view
     * gets bigger than the world.
     *
     * GLUE_END means that the view and the world will be constrainted
     * to have the same end.
     */
    public static final int GLUE_END = 1;

    /**
     * This is one of the 3 constants that define the behavior when the view
     * gets bigger than the world.
     *
     * SCALE means that the view is scaled to fit the world.
     */
    public static final int SCALE = 2;

    //@todo preferences
    public int mode = SCALE;

    public static interface Observer
    {
	void modelChanged(WorldViewModel model);
	void zoomCenterChanged(WorldViewModel model);
    }

    public final ObserverList observers = new ObserverList(Observer.class);

    public WorldViewModel(boolean isHorizontal,
			  double worldOrigin,
			  double worldSize,
			  double scale, //world units per pixel
			  double minScale)
    {
	this.isHorizontal = isHorizontal;
	this.worldOrigin = origin = worldOrigin;
	this.worldSize = worldSize;
	this.minScale = minScale;
	this.scale = scale;

	zoomCenter = worldSize / 2;

	resetScrollModel();
	resetZoomModel();

	scrollModel.addChangeListener(this);
	zoomModel.addChangeListener(this);
    }

    public double worldOrigin() { return worldOrigin; }
    public double worldSize() { return worldSize; }
    public double worldEnd() { return worldOrigin + worldSize; }
    public double origin() { return origin; }
    public double viewEnd() { return origin + viewSize(); }
    public double viewSize() { return scale * windowSizeInPixels(); }
    public double viewCenter() { return origin + pixelToSize(windowSizeInPixels() / 2); }

    public double viewSizeAtMaxZoom() { return minScale * windowSizeInPixels(); }

    public BoundedRangeModel scrollModel() { return scrollModel; }
    public BoundedRangeModel zoomModel() { return zoomModel; }

    // the scale is the number of world units per pixel
    public double scale() {
	return scale;
    }

    public double zoomCenter() {
	return zoomCenter;
    }

    public void setWindow(Component component) {
	if (window != null)
	    window.removeComponentListener(this);
	window = component;
	if (window != null)
	    window.addComponentListener(this);
    }

    public void delete() {
	if (window != null)
	    window.removeComponentListener(this);
	scrollModel.removeChangeListener(this);
	zoomModel.removeChangeListener(this);
    }

    private void resetScrollModel() {
	isMaster = true;
        int origin = Math.max(0, posToRange(this.origin));
        int size = Math.min(RANGE_MAX - origin, sizeToRange(viewSize()));
	scrollModel.setRangeProperties(origin, size, 0, RANGE_MAX, false);
	isMaster = false;
    }

    private void resetZoomModel() {
	isMaster = true;
        int percent = (int)(zoomToPercent() / 100 * RANGE_MAX);
        if (percent < 0) percent = 0;
        else if (percent > RANGE_MAX) percent = RANGE_MAX;
	zoomModel.setRangeProperties(percent, 0, 0, RANGE_MAX, false);
	isMaster = false;
    }

    public void setWorld(double worldOrigin, double worldSize) {
        if (worldOrigin != this.worldOrigin || worldSize != this.worldSize) {
    	    this.worldOrigin = worldOrigin;
	    this.worldSize = worldSize;
	    checkAndNotify();
        }
    }

    public void setView(double origin, double scale) {
        if (origin != this.origin || scale != this.scale) {
    	    this.origin = origin;
	    this.scale = scale;
	    checkAndNotify();
        }
    }

    public void setOrigin(double origin) {
	setView(origin, scale);
    }

    public void setViewEnd(double end) {
        setOrigin(end - viewSize());
    }

    public void setScale(double scale) {
        setView(origin, scale);
    }

    public void setViewMinMax(double min, double max) {
	setView(min, (max - min) / windowSizeInPixels());
    }

    public void setViewCenter(double center) {
	setView(origin + center - viewCenter(), scale);
    }

    /**
     * Makes the view equal to the world.
     */
    public void showEverything() {
	setView(worldOrigin, worldSize / windowSizeInPixels());
    }

    public void scroll(int pixels) {
	setView(origin + pixelToSize(pixels),
		scale);
    }

    public void setZoomCenter(double center) {
        final double K = 0.03;
	if (center < origin)
	    center = origin + K * viewSize();
	else if (center > origin + viewSize())
	    center = origin + (1 - K) * viewSize();

	if (zoomCenter != center) {
	    zoomCenter = center;
	    observers.notify(new ObserverList.Caller() {
		    public void call(Object o) {
			((Observer)o).zoomCenterChanged(WorldViewModel.this);
		    }});
	}
    }

    public void zoom(double percentZoom) {
	double zoom = percentToZoom(percentZoom);
	double newViewSize = zoom * worldSize;

	double k = (zoomCenter - origin) / viewSize();
	double newViewOrigin = zoomCenter - k * newViewSize;
	double newScale = newViewSize / windowSizeInPixels();

	setView(newViewOrigin, newScale);
    }

    private double percentToZoom(double percent) {
	double minZoom = minViewSize(minScale) / worldSize;

	// z**0       = minZoom / minZoom = 1
	// z**100     = maxZoom / minZoom    (maxZoom == 1)
	// z**percent =    zoom / minZoom
	//
	//    ==>
	//
	// 1) z = e[log(1 / minZoom)/100]
	// 2) zoom = minZoom . z**percent

	double z = Math.exp(Math.log(1/minZoom)/100);
	return minZoom * Math.pow(z, percent);
    }

    private double zoomToPercent() {
	double zoom = viewSize() / worldSize;
	double minZoom = minViewSize(minScale) / worldSize;

	// See above.
	// 1) z = e[log(1 / minZoom)/100]
	// 2) percent = log(zoom / minZoom) / log(z)

	double z = Math.exp(Math.log(1/minZoom)/100);
	return (int)(Math.log(zoom / minZoom) / Math.log(z));
    }

    public int windowSizeInPixels() {
	if (window == null)
	    return 1;
	int size = isHorizontal ? window.getWidth() : window.getHeight();
	return (size == 0) ? 1 : size;
    }


    //Component listener

    public void componentMoved(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
    public void componentHidden(ComponentEvent e) {}

    private double storedPreviousViewSize = 1;

    private double previousViewSize() {
        double previousViewSize = storedPreviousViewSize;
        storedPreviousViewSize = viewSize();
        return previousViewSize;
    }

    public void componentResized(ComponentEvent e) {
        handleViewSizeChange(previousViewSize());
	checkAndNotify();
    }

    //Change listener

    public void stateChanged(ChangeEvent e) {
	if (!isMaster) {
	    if (e.getSource() == scrollModel)
		scrollRangeChanged();
	    else
		zoomRangeChanged();
	}
    }

    private void scrollRangeChanged() {
	double size = viewSize();
	if (lastExtent != scrollModel.getExtent()) {
	    lastExtent = scrollModel.getExtent();
	    size = rangeToSize(lastExtent);
	}

	setView(rangeToPos(scrollModel.getValue()),
		size / windowSizeInPixels());
    }

    private void zoomRangeChanged() {
	zoom(((double)zoomModel.getValue())/RANGE_MAX*100);
    }

    private void checkView() {
        if (scale < minScale) {
            double center = viewCenter();
            scale = minScale;
            setViewCenter(center);
        }

	if (viewSize() > worldSize) {
	    if (mode == SCALE) {
		scale = worldSize / windowSizeInPixels();
		origin = worldOrigin;
	    }
	    else if (mode == GLUE_START) {
		origin = worldOrigin;
	    }
	    else { //mode == GLUE_END
		origin = worldOrigin + worldSize - viewSize();
	    }
	}
        else if (origin < worldOrigin)
            origin = worldOrigin;
        else if (origin + viewSize() > worldOrigin + worldSize)
            origin = worldOrigin + worldSize - viewSize();

	setZoomCenter(zoomCenter);
    }

    private void handleViewSizeChange(double previousViewSize) {
        double newViewSize = viewSize();
        if (newViewSize == previousViewSize)
            return;

        if (mode == GLUE_END)
            origin += previousViewSize - newViewSize;
    }

    public void checkAndNotify() {
        checkView();

	resetScrollModel();
	resetZoomModel();

        observers.notify(new ObserverList.Caller() {
                public void call(Object o) {
                    ((Observer)o).modelChanged(WorldViewModel.this);
                }});
    }

    private int posToRange(double x) {
	return (int)((x - worldOrigin) / worldSize * RANGE_MAX);
    }

    private int sizeToRange(double size) {
	return (int)(size / worldSize * RANGE_MAX);
    }

    private double rangeToPos(int x) {
	return worldOrigin + rangeToSize(x);
    }

    private double rangeToSize(int size) {
	return size * worldSize / RANGE_MAX;
    }

    public double pixelToSize(int size) {
	return scale * size;
    }

    public double sizeToPixels(double size) {
	return size / scale;
    }

    private double minViewSize(double minScale) {
	return windowSizeInPixels() * minScale;
    }

    private double pixelToPos(int x) {
	return origin + x * scale;
    }
}
