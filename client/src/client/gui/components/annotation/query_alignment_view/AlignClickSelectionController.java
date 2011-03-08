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

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

import client.shared.vizard.SubjectAndQueryComparisonGlyph;
import vizard.EventDispatcher;
import vizard.Glyph;
import vizard.interactor.ClickInteractor;

import java.awt.*;

/**
 * This class is the same as the SubjectClickSelectionController except that it takes
 * a different subview so it can call a method in that subview directly.
 * This situation needs to be improved.  Perhaps a GlyphListener is in order
 * as GlyphSelected seems like listener events anyway.
 */
/**
 * @todo Correct the situation mentioned above.
 */
public class AlignClickSelectionController implements ClickInteractor.Adapter
{
    private ClickInteractor interactor;
    private ClickInteractor shiftInteractor;
    private ClickInteractor controlInteractor;
    private QueryAlignmentView view;

    public AlignClickSelectionController(final QueryAlignmentView view) {
        this.view = view;
        interactor = new ClickInteractor(this);

   shiftInteractor = new ClickInteractor(this);
   shiftInteractor.activeWithShift = true;

   controlInteractor = new ClickInteractor(this);
   controlInteractor.activeWithControl = true;

        interactor.activatedOnButtonRelease = false;
        shiftInteractor.activatedOnButtonRelease = false;
        controlInteractor.activatedOnButtonRelease = false;
   EventDispatcher.instance.addInteractor(SubjectAndQueryComparisonGlyph.class, interactor,
            new EventDispatcher.Filter() {
              public boolean isValid(Glyph glyph) {
                if (glyph == null || glyph.getRootGlyph()==null) return false;
                Component comp = (Component)glyph.getRootGlyph().container();
                return EventDispatcher.hasAncestor(comp, view);
              }
            });
   EventDispatcher.instance.addInteractor(SubjectAndQueryComparisonGlyph.class, shiftInteractor,
            new EventDispatcher.Filter() {
              public boolean isValid(Glyph glyph) {
                if (glyph == null || glyph.getRootGlyph()==null) return false;
                Component comp = (Component)glyph.getRootGlyph().container();
                return EventDispatcher.hasAncestor(comp, view);
              }
            });
   EventDispatcher.instance.addInteractor(SubjectAndQueryComparisonGlyph.class, controlInteractor,
            new EventDispatcher.Filter() {
              public boolean isValid(Glyph glyph) {
                if (glyph == null || glyph.getRootGlyph()==null) return false;
                Component comp = (Component)glyph.getRootGlyph().container();
                return EventDispatcher.hasAncestor(comp, view);
              }
            });
    }


    public void delete() {
   EventDispatcher dispatcher = EventDispatcher.instance;

   dispatcher.removeInteractor(SubjectAndQueryComparisonGlyph.class, interactor);
   dispatcher.removeInteractor(SubjectAndQueryComparisonGlyph.class, shiftInteractor);
   dispatcher.removeInteractor(SubjectAndQueryComparisonGlyph.class, controlInteractor);
    }


    private SubjectAndQueryComparisonGlyph selectedGlyph(ClickInteractor itor) {
   return (SubjectAndQueryComparisonGlyph)itor.glyph();
    }


    //ClickInteractor adapter specialization
    public void clicked(ClickInteractor itor) {
   view.selectGlyph(selectedGlyph(itor),
          itor == shiftInteractor,
          itor == controlInteractor);
    }
}
