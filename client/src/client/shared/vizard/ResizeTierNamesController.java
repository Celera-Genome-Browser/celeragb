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
import vizard.GraphicContext;
import vizard.genomics.component.ForwardAndReverseTiersComponent;
import vizard.glyph.FastLineGlyph;
import vizard.interactor.EnterLeaveInteractor;
import vizard.interactor.MotionInteractor;

import javax.swing.*;
import java.awt.*;


public class ResizeTierNamesController
    implements MotionInteractor.Adapter,
               EnterLeaveInteractor.Adapter
{
    private static Cursor cursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
    private MySensorBarGlyph forwardBar = new MySensorBarGlyph();
    private MySensorBarGlyph reverseBar = new MySensorBarGlyph();
    private MySensorBarGlyph axisBar = new MySensorBarGlyph();
    private MotionInteractor motionItor;
    private EnterLeaveInteractor enterLeaveItor;
    private JComponent cursorComponent;
    private Cursor savedCursor;
    private boolean isMotionStarted;
    private boolean wasCursorChanged;
    private ForwardAndReverseTiersComponent view;

    public ResizeTierNamesController(final ForwardAndReverseTiersComponent view) {
        this.view=view;
        view.forwardTable().tierNamesComponent().rootGlyph().addChild(forwardBar);
        view.reverseTable().tierNamesComponent().rootGlyph().addChild(reverseBar);
        view.axisTable().tierNamesComponent().rootGlyph().addChild(axisBar);

        motionItor = new MotionInteractor(this);
        enterLeaveItor = new EnterLeaveInteractor(this);

        EventDispatcher.instance.addInteractor(MySensorBarGlyph.class, motionItor,
            new EventDispatcher.Filter() {
              public boolean isValid(Glyph glyph) {
                if (glyph == null || glyph.getRootGlyph()==null) return false;
                Component comp = (Component)glyph.getRootGlyph().container();
                return EventDispatcher.hasAncestor(comp, view);
              }
            });
        EventDispatcher.instance.addInteractor(MySensorBarGlyph.class, enterLeaveItor,
            new EventDispatcher.Filter() {
              public boolean isValid(Glyph glyph) {
                if (glyph == null || glyph.getRootGlyph()==null) return false;
                Component comp = (Component)glyph.getRootGlyph().container();
                return EventDispatcher.hasAncestor(comp, view);
              }
            });
    }

    public void delete() {
        EventDispatcher.instance.removeInteractor(MySensorBarGlyph.class, motionItor);
        EventDispatcher.instance.removeInteractor(MySensorBarGlyph.class, enterLeaveItor);

        forwardBar.delete();
        reverseBar.delete();
        axisBar.delete();

    }

    //EnterLeaveInteractor adapter specialization

    public void glyphEntered(EnterLeaveInteractor itor) {
        if (!wasCursorChanged) {
            wasCursorChanged = true;
            cursorComponent = (JComponent)EventDispatcher.instance.root().container();
            savedCursor = cursorComponent.getCursor();
            cursorComponent.setCursor(cursor);
        }
    }

    public void glyphExited(EnterLeaveInteractor itor) {
        if (!isMotionStarted)
            restoreCursor();
    }

    //MotionInteractor adapter specialization

    public void motionStarted(MotionInteractor itor) {
        isMotionStarted = true;
    }

    public void move(MotionInteractor itor) {
        int parentX = (int)itor.currentLocation().getX() + cursorComponent.getX();
        int newWidth = cursorComponent.getParent().getWidth() - parentX;

        view.setTierNamesWidth(newWidth);
    }

    public void motionStopped(MotionInteractor itor) {
        isMotionStarted = false;
        restoreCursor();
    }

    public void motionCancelled(MotionInteractor itor) {
        isMotionStarted = false;
        restoreCursor();
    }

    private void restoreCursor() {
        if (wasCursorChanged) {
            wasCursorChanged = false;
            cursorComponent.setCursor(savedCursor);
            savedCursor = null;
        }
    }

    private class MySensorBarGlyph extends FastLineGlyph
    {
        public double x1() { return 0; }
        public double y1() { return -4000; }
        public double x2() { return 0; }
        public double y2() { return 4000; }
        public Color color() { return null; }
        public void draw(GraphicContext gc) {}
    }
}
