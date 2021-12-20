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

import client.gui.framework.view_pref_mgr.TierInfo;
import client.gui.framework.view_pref_mgr.ViewPrefMgr;
import vizard.EventDispatcher;
import vizard.Glyph;
import vizard.genomics.glyph.TierGlyph;
import vizard.genomics.glyph.TierNameGlyph;
import vizard.interactor.ClickInteractor;

import java.awt.*;

public class ClickExpandTierController extends Controller implements ClickInteractor.Adapter {
   private ClickInteractor interactor;
   private TierNameGlyph glyphToWatch;

   public ClickExpandTierController(final GenomicAxisAnnotationView view) {
      super(view);
      interactor = new ClickInteractor(this);
      EventDispatcher.instance.addInteractor(TierNameGlyph.class, interactor, new EventDispatcher.Filter() {
         public boolean isValid(Glyph glyph) {
            if (glyph == null || glyph.getRootGlyph() == null)
               return false;
            Component comp = (Component)glyph.getRootGlyph().container();
            return EventDispatcher.hasAncestor(comp, view);
         }
      });
   }

   public void delete() {
      EventDispatcher.instance.removeInteractor(TierNameGlyph.class, interactor);
      super.delete();
   }

   //ClickInteractor adapter specialization

   public void clicked(ClickInteractor itor) {
      TierNameGlyph tierNameGlyph = (TierNameGlyph)itor.glyph();
      TierGlyph tierGlyph = tierNameGlyph.correspondingTierGlyph();

      String tierName = tierGlyph.name();
      if (tierName.endsWith(view.REV_TIER_SUFFIX))
         tierName = tierName.substring(0, tierName.length() - 6);
      ViewPrefMgr viewPrefs = ViewPrefMgr.getViewPrefMgr();
      TierInfo tierInfo = viewPrefs.getTierInfo(tierName);

      if (tierInfo.getState() == TierInfo.TIER_COLLAPSED) {
         viewPrefs.setTierState(tierName, TierInfo.TIER_EXPANDED);
         viewPrefs.commitChanges(true);
         viewPrefs.fireTierStateChangeEvent(tierInfo);
      }
      else if (tierInfo.getState() == TierInfo.TIER_EXPANDED) {
         viewPrefs.setTierState(tierName, TierInfo.TIER_COLLAPSED);
         viewPrefs.commitChanges(true);
         viewPrefs.fireTierStateChangeEvent(tierInfo);
      }
   }
}
