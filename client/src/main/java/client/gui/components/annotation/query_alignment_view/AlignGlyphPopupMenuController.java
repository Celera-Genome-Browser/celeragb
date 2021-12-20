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
//*============================================================================
//* $Author$
//*
//* $Date$
//*
//* $Header$r: /cm/cvs/enterprise/src/client/gui/components/annotation/query_alignment_view/AlignGlyphPopupMenuController.java,v 1.5 2002/11/07 17:33:32 lblick Exp $
//*============================================================================
package client.gui.components.annotation.query_alignment_view;

import client.shared.vizard.DNAComparisonGlyph;
import client.shared.vizard.ProteinComparisonGlyph;
import client.shared.vizard.SubjectAndQueryComparisonGlyph;
import vizard.EventDispatcher;
import vizard.Glyph;
import vizard.interactor.ClickInteractor;

import javax.swing.*;
import java.awt.*;


/**
 * DOCUMENT ME!
 * 
 * @version $Revision$
 * @author Lou Blick
 */
public class AlignGlyphPopupMenuController implements ClickInteractor.Adapter {
   /** DOCUMENT ME! */
   private ClickInteractor interactor;
   /** DOCUMENT ME! */
   private QueryAlignmentView view;

   /**
    * Creates a new AlignGlyphPopupMenuController object.
    * 
    * @param view        DOCUMENT ME!
    */
   public AlignGlyphPopupMenuController( QueryAlignmentView view ) {
      this.view                       = view;
      interactor                      = new ClickInteractor( this );
      interactor.activeWithLeftButton = false;
      EventDispatcher.instance.addInteractor( DNAComparisonGlyph.class, interactor, new EventDispatcher.Filter() {
         public boolean isValid( Glyph glyph ) {
            if ( ( glyph == null ) || ( glyph.getRootGlyph() == null ) ) {
               return false;
            }
            Component comp = ( Component ) glyph.getRootGlyph().container();
            return EventDispatcher.hasAncestor( comp, AlignGlyphPopupMenuController.this.view );
         }
      } );
      EventDispatcher.instance.addInteractor( ProteinComparisonGlyph.class, interactor, new EventDispatcher.Filter() {
         public boolean isValid( Glyph glyph ) {
            if ( ( glyph == null ) || ( glyph.getRootGlyph() == null ) ) {
               return false;
            }
            Component comp = ( Component ) glyph.getRootGlyph().container();
            return EventDispatcher.hasAncestor( comp, AlignGlyphPopupMenuController.this.view );
         }
      } );
   }

   /**
    * DOCUMENT ME!
    * 
    * @param itor    DOCUMENT ME!
    */
   public void clicked( ClickInteractor itor ) {
      SubjectAndQueryComparisonGlyph gg             = ( SubjectAndQueryComparisonGlyph ) itor.glyph();
      Point                          windowLocation = new Point( itor.event().getX(), itor.event().getY() );
      view.glyphPopup( ( JComponent )itor.event().getComponent(), gg, windowLocation );
   }


   /**
    * DOCUMENT ME!
    */
   public void delete() {
      EventDispatcher.instance.removeInteractor( DNAComparisonGlyph.class, interactor );
      EventDispatcher.instance.removeInteractor( ProteinComparisonGlyph.class, interactor );
   }
}