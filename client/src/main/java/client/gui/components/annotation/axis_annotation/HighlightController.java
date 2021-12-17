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

import api.entity_model.management.PropertyMgr;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.fundtype.AlignableGenomicEntity;
import vizard.EventDispatcher;
import vizard.Glyph;
import vizard.glyph.AdornmentGlyph;
import vizard.interactor.EnterLeaveInteractor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class HighlightController extends Controller
    implements EnterLeaveInteractor.Adapter
{
    //@todo preferences
    public static Color HIGHLIGHT_COLOR = Color.gray;
    private boolean showTooltip = false;
    private AdornmentGlyph highlight;
    private EnterLeaveInteractor interactor;

    public HighlightController(GenomicAxisAnnotationView view) {
	super(view);

        //  The lines below smell of a hacky java bug but it must be done to display tooltips.
        ((JComponent)view.forwardColumn().getRootGlyph().container()).setToolTipText("");
        ((JComponent)view.reverseColumn().getRootGlyph().container()).setToolTipText("");
        ((JComponent)view.axisColumn().getRootGlyph().container()).setToolTipText("");

        interactor = new EnterLeaveInteractor(this);
	EventDispatcher.instance.addInteractor(GBGenomicGlyph.class, interactor,
            new EventDispatcher.Filter() {
              public boolean isValid(Glyph glyph) {
                if (glyph == null || glyph.getRootGlyph()==null) return false;
                Component comp = (Component)glyph.getRootGlyph().container();
                return EventDispatcher.hasAncestor(comp, HighlightController.this.view);
              }
        });
    }

    public void delete() {
	EventDispatcher.instance.removeInteractor(GBGenomicGlyph.class,
				                  interactor);
	if (highlight != null)
            highlight.delete();

        super.delete();
    }

    //EnterLeaveInteractor adapter specialization

    public void glyphEntered(EnterLeaveInteractor itor) {
      GBGenomicGlyph glyph = (GBGenomicGlyph)itor.glyph();
      highlight = new AdornmentGlyph(glyph, HIGHLIGHT_COLOR, 1);
      glyph.addChild(highlight);

      if (showTooltip) {
        ToolTipManager.sharedInstance().setDismissDelay(60000);
        if (glyph instanceof GBGenomicGlyph) {
          // Check to see what keys are depressed.  Shift="Transcript", Ctrl="Gene"
          AlignableGenomicEntity tmpEntity = (AlignableGenomicEntity)glyph.alignment().getEntity();
          MouseEvent tmpEvent = EventDispatcher.instance.getLastMouseEvent();
          String tip;
          if (tmpEntity instanceof Feature) {
            Feature tmpFeature = (Feature)tmpEntity;
            if (tmpEvent.isShiftDown() && tmpFeature.getSuperFeature()!=null)
              tmpEntity = tmpFeature.getSuperFeature();
            else if (tmpEvent.isControlDown() && tmpFeature.getRootFeature()!=null)
              tmpEntity = tmpFeature.getRootFeature();
          }

          tip = PropertyMgr.getPropertyMgr().getToolTipForEntity(tmpEntity);
          ((JComponent)glyph.getRootGlyph().container()).setToolTipText(tip);
        }
      }
    }

    public void glyphExited(EnterLeaveInteractor itor) {
      GBGenomicGlyph glyph = (GBGenomicGlyph)itor.glyph();
      if (highlight != null) highlight.delete();
      highlight = null;

      if (showTooltip) {
        ToolTipManager.sharedInstance().setDismissDelay(4000);
        if (glyph != null && glyph instanceof GBGenomicGlyph && glyph.getRootGlyph()!=null  &&
            glyph.getRootGlyph().container()!=null) {
          ((JComponent)glyph.getRootGlyph().container()).setToolTipText("");
        }
      }
    }
}
