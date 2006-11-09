// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package client.gui.components.annotation.axis_annotation;

import vizard.EventDispatcher;
import vizard.Glyph;
import vizard.RootGlyph;
import vizard.genomics.component.TiersComponent;
import vizard.glyph.FastLineGlyph;
import vizard.glyph.WorldViewTransformGlyph;
import vizard.interactor.MotionInteractor;

import java.awt.Color;
import java.awt.Component;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class RubberSelectionController extends Controller
    implements MotionInteractor.Adapter
{
    private FastLineGlyph rubber;
    private int startI, startJ;
    private int x, y, width;
    private MotionInteractor interactor;
    private boolean justAClick;

    public RubberSelectionController(GenomicAxisAnnotationView view,
				     final Color color)
    {
	super(view);

        interactor = new MotionInteractor(this);

	rubber = new FastLineGlyph() {
		public double x1() { return x; }
		public double y1() { return y; }
		public double x2() { return x + width; }
		public double y2() { return y; }
		public Color color() { return color; }
	    };

        EventDispatcher disp = EventDispatcher.instance;
        disp.addInteractor(view.forwardTable().tiersComponent().rootGlyph().getClass(), interactor,
            new EventDispatcher.Filter() {
              public boolean isValid(Glyph glyph) {
                if (glyph == null || glyph.getRootGlyph()==null) return false;
                Component comp = (Component)glyph.getRootGlyph().container();
                return EventDispatcher.hasAncestor(comp, RubberSelectionController.this.view);
              }
            });
        disp.addInteractor(view.reverseTable().tiersComponent().rootGlyph().getClass(), interactor,
            new EventDispatcher.Filter() {
              public boolean isValid(Glyph glyph) {
                if (glyph == null || glyph.getRootGlyph()==null) return false;
                Component comp = (Component)glyph.getRootGlyph().container();
                return EventDispatcher.hasAncestor(comp, RubberSelectionController.this.view);
              }
            });
        disp.addInteractor(view.axisTable().tiersComponent().rootGlyph().getClass(), interactor,
            new EventDispatcher.Filter() {
              public boolean isValid(Glyph glyph) {
                if (glyph == null || glyph.getRootGlyph()==null) return false;
                Component comp = (Component)glyph.getRootGlyph().container();
                return EventDispatcher.hasAncestor(comp, RubberSelectionController.this.view);
              }
            });
    }

    public void delete() {
        EventDispatcher disp = EventDispatcher.instance;
        disp.addInteractor(view.forwardTable().tiersComponent().rootGlyph().getClass(), interactor);
        disp.addInteractor(view.reverseTable().tiersComponent().rootGlyph().getClass(), interactor);
        disp.addInteractor(view.axisTable().tiersComponent().rootGlyph().getClass(), interactor);

	rubber.delete();

        super.delete();
    }

    private RootGlyph root() {
	return EventDispatcher.instance.root();
    }

    private WorldViewTransformGlyph viewTransform() {
        TiersComponent comp = (TiersComponent)root().container();
	return comp.viewTransform();
    }

    //MotionInteractor adapter specialization

    public void motionStarted(MotionInteractor itor) {
	Point2D.Double p = new Point2D.Double();
	p.setLocation(itor.startingLocation());
	try { viewTransform().transform().inverseTransform(p, p); }
	catch(NoninvertibleTransformException ex) {}

	this.x = (int)p.x;
	this.y = (int)p.y;
	width = 0;

	viewTransform().addChild(rubber);

        startI = itor.event().getX();
        startJ = itor.event().getY();
        justAClick = true;
    }

    public void move(MotionInteractor itor) {
        if (justAClick &&
            (Math.abs(itor.event().getX() - startI) >= 2 ||
             Math.abs(itor.event().getY() - startJ) >= 2))
             justAClick = false;
        if (justAClick)
            return;

        Point2D pStart = new Point2D.Double();
	pStart.setLocation(itor.startingLocation());
	Point2D pCurrent = new Point2D.Double();
	pCurrent.setLocation(itor.currentLocation());

	try {
            viewTransform().transform().inverseTransform(pStart, pStart);
            viewTransform().transform().inverseTransform(pCurrent, pCurrent);
        }
	catch(NoninvertibleTransformException ex) {}

	rubber.repaint();
	x = (int)Math.min(pCurrent.getX(), pStart.getX());
	y = (int)pCurrent.getY();
	int xmax = (int)Math.ceil(Math.max(pCurrent.getX(), pStart.getX()));
	width = xmax - x;
	rubber.repaint();

        view.axisRulerGlyph().setSelectedRange(x, x + width);
    }

    public void motionStopped(MotionInteractor itor) {
	viewTransform().removeChild(rubber);

        if (justAClick)
            view.selectGlyph(null, false, false, false);
        else
            view.rubberSelectionDone(x, x + width);
    }

    public void motionCancelled(MotionInteractor itor) {
        motionStopped(itor);
    }
}
