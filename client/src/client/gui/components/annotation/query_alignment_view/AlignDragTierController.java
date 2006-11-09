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
package client.gui.components.annotation.query_alignment_view;

import vizard.EventDispatcher;
import vizard.Glyph;
import vizard.genomics.glyph.TierGlyph;
import vizard.genomics.glyph.TierNameGlyph;
import vizard.interactor.MotionInteractor;

import java.awt.Component;


public class AlignDragTierController implements MotionInteractor.Adapter
{
    private MotionInteractor interactor;
    private TierGlyph draggedGlyph;
    private TierGlyph previousDestGlyph;
    private QueryAlignmentView view;

    public AlignDragTierController(final QueryAlignmentView view) {
        interactor = new MotionInteractor(this);
	EventDispatcher.instance.addInteractor(TierNameGlyph.class, interactor,
            new EventDispatcher.Filter() {
              public boolean isValid(Glyph glyph) {
                if (glyph == null || glyph.getRootGlyph()==null) return false;
                Component comp = (Component)glyph.getRootGlyph().container();
                return EventDispatcher.hasAncestor(comp, view);
              }
            });
    }

    public void delete() {
	EventDispatcher.instance.removeInteractor(TierNameGlyph.class, interactor);
    }

    private void repositionTier(TierGlyph src, TierGlyph dst) {
      // If not the tier we are watching, do nothing.
      //TiersColumnGlyph tiersColumn = src.tierColumn();
      //int srcOrder = Math.abs(tiersColumn.tierOrderIndex(src));
      //int dstOrder = Math.abs(tiersColumn.tierOrderIndex(dst));

      //ViewPrefMgr.getViewPrefMgr().swapTierOrder(view.VIEW_NAME, "" + srcOrder, "" + dstOrder);
      //view.reorderTierGlyphs();
    }

    //MotionInteractor adapter specialization

    public void motionStarted(MotionInteractor itor) {
	draggedGlyph = ((TierNameGlyph)itor.glyph()).correspondingTierGlyph();
        previousDestGlyph = null;
    }

    public void move(MotionInteractor itor) {
	if (!(itor.currentGlyph() instanceof TierNameGlyph))
	    return;
	TierGlyph destGlyph = ((TierNameGlyph)itor.currentGlyph()).correspondingTierGlyph();
        if (destGlyph == draggedGlyph)
            return;
        if (destGlyph == previousDestGlyph) //avoids "flicker"
            return;
        if (destGlyph.tierColumn() != draggedGlyph.tierColumn())
	    return;

        previousDestGlyph = destGlyph;
	repositionTier(draggedGlyph, destGlyph);
    }

    public void motionStopped(MotionInteractor itor) {
    }

    public void motionCancelled(MotionInteractor itor) {
    }
}
