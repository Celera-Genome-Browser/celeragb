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
import vizard.genomics.component.ForwardAndReverseTiersComponent;
import vizard.genomics.glyph.TierGlyph;
import vizard.genomics.glyph.TierNameGlyph;
import vizard.interactor.ClickInteractor;

import java.awt.Component;

import javax.swing.JPopupMenu;


public class AlignTierPopupMenuController implements ClickInteractor.Adapter
{
    private ClickInteractor interactor;
    private TierGlyph currentTier;
    private ForwardAndReverseTiersComponent view;
    private JPopupMenu targetMenu = new JPopupMenu();

    public AlignTierPopupMenuController(final ForwardAndReverseTiersComponent view,
      JPopupMenu targetMenu) {
        this.targetMenu = targetMenu;
        interactor = new ClickInteractor(this);
        interactor.activeWithLeftButton = false;

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

    //ClickInteractor adapter specialization

    public void clicked(ClickInteractor itor) {
//        TierNameGlyph tierNameGlyph = (TierNameGlyph)itor.glyph();
//        JComponent component = (JComponent)EventDispatcher.instance.root().container();
//        targetMenu.show(tierNameGlyph.correspondingTierGlyph(), component,
//                                   itor.event().getX(), itor.event().getY());
    }
}
