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
package client.gui.components.annotation.query_alignment_view;

import client.shared.vizard.SubjectAndQueryComparisonGlyph;
import vizard.EventDispatcher;
import vizard.Glyph;
import vizard.glyph.AdornmentGlyph;
import vizard.interactor.EnterLeaveInteractor;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;

/**
 * This class is the same as the SubjectHighlighController except that it takes
 * a different subview so it can call a method in that subview directly.
 * This situation needs to be improved.  Perhaps a GlyphListener is in order
 * as GlyphEntered, GlyphExited, GlyphClicked all seem like listener events anyway.
 */
/**
 * @todo Correct the situation mentioned above.
 */
public class AlignHighlightController
    implements EnterLeaveInteractor.Adapter
{

    private QueryAlignmentView alignmentView;
    public static Color HIGHLIGHT_COLOR = Color.red;
    private AdornmentGlyph highlight;
    private EnterLeaveInteractor interactor;

    public AlignHighlightController(final QueryAlignmentView alignmentView) {
      //  The lines below smell of a hacky java bug but it must be done to display tooltips.
      ((JComponent)alignmentView.forwardColumn().getRootGlyph().container()).setToolTipText("");
      ((JComponent)alignmentView.reverseColumn().getRootGlyph().container()).setToolTipText("");
      ((JComponent)alignmentView.axisColumn().getRootGlyph().container()).setToolTipText("");

      this.alignmentView=alignmentView;
      interactor = new EnterLeaveInteractor(this);
      EventDispatcher.instance.addInteractor(SubjectAndQueryComparisonGlyph.class, interactor,
          new EventDispatcher.Filter() {
            public boolean isValid(Glyph glyph) {
              if (glyph == null || glyph.getRootGlyph()==null) return false;
              Component comp = (Component)glyph.getRootGlyph().container();
              return EventDispatcher.hasAncestor(comp, alignmentView);
            }
          });
    }

    public void delete() {
      EventDispatcher.instance.removeInteractor(SubjectAndQueryComparisonGlyph.class, interactor);
      if (highlight != null)
          highlight.delete();
    }

    //EnterLeaveInteractor adapter specialization

    public void glyphEntered(EnterLeaveInteractor itor) {
      SubjectAndQueryComparisonGlyph glyph = (SubjectAndQueryComparisonGlyph)itor.glyph();
      highlight = new AdornmentGlyph(glyph, HIGHLIGHT_COLOR, 1);
      glyph.addChild(highlight);
      alignmentView.glyphEntered(glyph);
    }

    public void glyphExited(EnterLeaveInteractor itor) {
      alignmentView.glyphExited((SubjectAndQueryComparisonGlyph)highlight.parent());
      highlight.delete();
      highlight = null;
    }
}
