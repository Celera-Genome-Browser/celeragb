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
//* $Header$r: /cm/cvs/enterprise/src/vizard/genomics/component/AxisScrollBar.java,v 1.2 2002/11/07 16:11:47 lblick Exp $
//*============================================================================
package vizard.genomics.component;

import vizard.component.GScrollBar;
import vizard.model.WorldViewModel;


public class AxisScrollBar extends GScrollBar {
   /**
    * Creates a new AxisScrollBar object.
    *
    * @param	model		DOCUMENT ME!
    */
   public AxisScrollBar( WorldViewModel model ) {
      this( model, VERTICAL );
   }


   /**
    * Creates a new AxisScrollBar object.
    *
    * @param	model		DOCUMENT ME!
    * @param	direction		DOCUMENT ME!
    */
   public AxisScrollBar( WorldViewModel model, int direction ) {
      super( model, direction );
   }

   /**
    * DOCUMENT ME!
    *
    * @param       model    DOCUMENT ME!
    */
   public void modelChanged( WorldViewModel model ) {
      super.modelChanged( model );
      double ratio         = fullZoom_OneBaseToScreenWidth_Ratio( model );
      int    unitIncrement = ( int ) ( ratio * getModel().getExtent() );
      setUnitIncrement( unitIncrement );
   }


   /**
    * DOCUMENT ME!
    *
    * @param       model    DOCUMENT ME!
    *
    * @return 	DOCUMENT ME!
    */
   private double fullZoom_OneBaseToScreenWidth_Ratio( WorldViewModel model ) {
      return 1.0 / model.viewSizeAtMaxZoom();
   }
}