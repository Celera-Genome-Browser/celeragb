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
package vizard.glyph;

import vizard.Bounds;
import vizard.GraphicContext;
import vizard.ParentGlyph;
import vizard.PickedList;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;


/**
 * The purpose of the BoundsGlyph is to optimize the execution of
 * paint and pick by filtering out the calls that do not intersect
 * the glyph's bounds.
 * If there is an intersection, the bounds glyph delegates the
 * calls to its children.
 *
 * The bounds are not automatically computed by the glyph but
 * must be provided by a subclass.
 */
public abstract class BoundsGlyph extends ParentGlyph
{
    /**
     * Return the x location of the rectangular bounds.
     */
    public abstract double x();

    /**
     * Return the y location of the rectangular bounds.
     */
    public abstract double y();

    /**
     * Return the width of the rectangular bounds.
     */
    public abstract double width();

    /**
     * Return the height of the rectangular bounds.
     */
    public abstract double height();

    /**
     * Check whether the bounds intersect the dirty area
     */
    public boolean intersectsDirtyArea(GraphicContext gc) {
        Bounds b = gc.tempBounds();
        addBounds(b);
        return b.intersects(gc, gc.dirtyBounds());
    }

    /**
     * Forward paint to its children but only
     * if the bounds intersect the dirty area
     * (which is available in the graphic context)
     */
    public void paint(GraphicContext gc) {
	if (intersectsDirtyArea(gc))
	    super.paint(gc);
    }

    /**
     * Forward pick to its children but only if the bounds
     * intersect the given pixel rectangle.
     */
    public PickedList pick(GraphicContext gc, Rectangle rect) {
	Rectangle2D.Double bounds = gc.tempRectangle();
	bounds.setRect(x(), y(),
		       width() + gc.pixelWidth(),
		       height() + gc.pixelHeight());

	return gc.hit(rect, bounds, false)
	    ? super.pick(gc, rect)
	    : null;
    }

    /**
     * Add the bounds of this glyph to the given bounds.
     */
    public void addBounds(Bounds bounds) {
	bounds.add(x(), y(), width(), height());
    }

    /**
     * The purpose of the BoundsGlyph.Concrete class is to provide
     * a ready-to-use glyph to the application programmer.
     */
    public static class Concrete extends BoundsGlyph
    {
	private double x, y, w, h;

	public Concrete(double x, double y, double w, double h) {
	    setBounds(x, y, w, h);
	}

	public double x() { return x; }
	public double y() { return y; }
	public double width() { return w; }
	public double height() { return h; }

	public void setBounds(double x, double y, double w, double h) {
            repaint();
	    this.x = x;
	    this.y = y;
	    this.w = w;
	    this.h = h;
            repaint();
	    boundsChanged();
	}
    }
}
