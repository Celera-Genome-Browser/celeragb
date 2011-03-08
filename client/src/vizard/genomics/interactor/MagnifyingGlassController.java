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
package vizard.genomics.interactor;

import vizard.EventDispatcher;
import vizard.MultiplexerGlyph;
import vizard.ParentGlyph;
import vizard.RootGlyph;
import vizard.glyph.MagnifyingGlassGlyph;
import vizard.glyph.TranslationGlyph;

import java.awt.*;
import java.awt.event.MouseEvent;


public class MagnifyingGlassController
    //@todo implements MotionInteractor.Adapter
{
    ParentGlyph rulerMultiplexer;
    double magX, magY;
    ParentGlyph rulerParent;
    ParentGlyph tiersParent;
    ParentGlyph tiersMultiplexer;
    ParentGlyph topGlyph;
    MagnifyingGlassGlyph mag;

    //@todo preferences
    public static int DIAM = 300;

    public MagnifyingGlassController(ParentGlyph rulerMultiplexer) {
	this.rulerMultiplexer = rulerMultiplexer;
	MagnifyingGlassGlyph.glassColor = new Color(10, 47, 44);
    }

    public boolean isValidStart(MouseEvent e) {
	return ((e.getModifiers() & e.BUTTON1_MASK) != 0 &&
		(e.getModifiers() & e.SHIFT_MASK) != 0);
    }

    public void motionStarted(double x, double y) {
	magX = x - DIAM/2;
	magY = y - DIAM/2;
	RootGlyph currentRoot = (RootGlyph)EventDispatcher.instance.currentGlyph();
	tiersMultiplexer = (MultiplexerGlyph)currentRoot.child(0);

	tiersParent = new ParentGlyph();
	tiersParent.addChild(tiersMultiplexer);

	rulerParent = new TranslationGlyph() {
		public double tx() { return 0; }
		public double ty() { return magY + DIAM-95; }
	    };
	rulerParent.addChild(rulerMultiplexer);

	topGlyph = new ParentGlyph();
	topGlyph.addChild(tiersParent);
	topGlyph.addChild(rulerParent);

	mag = new MagnifyingGlassGlyph(topGlyph) {
		public double x() { return magX; }
		public double y() { return magY; }
		public double diameter() { return DIAM; }
		public double scaleX() { return 10; }
		public double scaleY() { return 1; }
	    };
	currentRoot.addChild(mag);
    }

    public void move(double x, double y, double tx, double ty) {
	mag.repaint();
	magX += tx/3;
	magY += ty;
	mag.repaint();
    }

    public void motionStopped() {
	tiersParent.removeChild(tiersParent.lastChild());
	rulerParent.removeChild(rulerParent.lastChild());
	mag.delete();

	//We changed the glyphs structure:
	EventDispatcher.instance.dispatchEnterLeave();
    }
}
