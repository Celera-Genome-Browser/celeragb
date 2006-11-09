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
package client.shared.vizard;

import vizard.EventDispatcher;
import vizard.Glyph;
import vizard.ParentGlyph;
import vizard.RootGlyph;
import vizard.genomics.component.ForwardAndReverseTiersComponent;
import vizard.genomics.component.TiersComponent;
import vizard.genomics.glyph.TierGlyph;
import vizard.glyph.MagnifyingGlassGlyph;
import vizard.glyph.ProxyGlyph;
import vizard.glyph.TransformGlyph;
import vizard.glyph.TranslationGlyph;
import vizard.interactor.MotionInteractor;

import java.awt.Color;
import java.awt.Component;
import java.awt.geom.AffineTransform;

public class MagnifyingGlassController implements MotionInteractor.Adapter
{
    //@todo preferences
    public static int DIAM = 300;

    private MotionInteractor interactor;
    private double magX, magY;
    private ParentGlyph rulerParent;
    private ParentGlyph tiersParent;
    private ParentGlyph tiersMultiplexer;
    private ParentGlyph topGlyph;
    private MagnifyingGlassGlyph mag;
    private ForwardAndReverseTiersComponent view;

    public MagnifyingGlassController(final ForwardAndReverseTiersComponent view) {
	this.view = view;
        MagnifyingGlassGlyph.glassColor = new Color(10, 47, 44);

        interactor = new MotionInteractor(this);
	interactor.startWithLeftButton = false;
        interactor.startWithAlt = true;

        EventDispatcher disp = EventDispatcher.instance;
        disp.addInteractor(TierGlyph.class, interactor,
            new EventDispatcher.Filter() {
              public boolean isValid(Glyph glyph) {
                if (glyph==null || glyph.getRootGlyph()==null) return false;
                Component comp = (Component)glyph.getRootGlyph().container();
                return EventDispatcher.hasAncestor(comp, view);
              }
            });
        disp.addInteractor(TierGlyph.class, interactor,
            new EventDispatcher.Filter() {
              public boolean isValid(Glyph glyph) {
                if (glyph==null || glyph.getRootGlyph()==null) return false;
                Component comp = (Component)glyph.getRootGlyph().container();
                return EventDispatcher.hasAncestor(comp, view);
              }
            });
        disp.addInteractor(TierGlyph.class, interactor,
            new EventDispatcher.Filter() {
              public boolean isValid(Glyph glyph) {
                if (glyph==null || glyph.getRootGlyph()==null) return false;
                Component comp = (Component)glyph.getRootGlyph().container();
                return EventDispatcher.hasAncestor(comp, view);
              }
            });
    }

    public void delete() {
        EventDispatcher disp = EventDispatcher.instance;
        disp.removeInteractor(view.forwardTable().tiersComponent().rootGlyph().getClass(), interactor);
        disp.removeInteractor(view.reverseTable().tiersComponent().rootGlyph().getClass(), interactor);
        disp.removeInteractor(view.axisTable().tiersComponent().rootGlyph().getClass(), interactor);
    }

    //MotionInteractor adapter specialization

    public void motionStarted(MotionInteractor itor) {
	magX = itor.currentLocation().getX() - DIAM/2;
	magY = itor.currentLocation().getY() - DIAM/2;

	RootGlyph currentRoot = EventDispatcher.instance.root();
        TiersComponent component = (TiersComponent)currentRoot.container();
	tiersMultiplexer = component.multiplexer();

	tiersParent = new ParentGlyph();
	tiersParent.addChild(tiersMultiplexer);

	topGlyph = new ParentGlyph();
	topGlyph.addChild(tiersParent);

        rulerParent = null;
        if (component != view.axisTable().tiersComponent()) {
    	    rulerParent = new TranslationGlyph() {
		    public double tx() { return 0; }
		    public double ty() { return magY + DIAM-95; }
	        };
            ParentGlyph rulerTransform = new TransformGlyph() {
                    public AffineTransform transform() {
                        return view.axisTable().tiersComponent().viewTransform().transform();
                    }};
            rulerTransform.addChild(new ProxyGlyph(view.axisRulerGlyph()));
	    rulerParent.addChild(rulerTransform);
	    topGlyph.addChild(rulerParent);
        }

	mag = new MagnifyingGlassGlyph(topGlyph) {
		public double x() { return magX; }
		public double y() { return magY; }
		public double diameter() { return DIAM; }
		public double scaleX() { return 10; }
		public double scaleY() { return 1; }
	    };
	currentRoot.addChild(mag);
    }

    public void move(MotionInteractor itor) {
	mag.repaint();
	magX = itor.currentLocation().getX() - DIAM/2;
	magY = itor.currentLocation().getY() - DIAM/2;
	mag.repaint();
    }

    public void motionStopped(MotionInteractor itor) {
	tiersParent.removeChild(tiersParent.lastChild());
        if (rulerParent != null)
    	    rulerParent.removeChild(rulerParent.lastChild());
	mag.delete();

	//We changed the glyphs structure:
	EventDispatcher.instance.dispatchEnterLeave();
    }

    public void motionCancelled(MotionInteractor itor) {
        motionStopped(itor);
    }
}
