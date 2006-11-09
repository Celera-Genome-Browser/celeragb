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
//* $Header$r: /cm/cvs/enterprise/src/shared/util/AxisBeginSourceToNameConverter.java,v 1.5 2002/12/19 15:23:17 lblick Exp $
//*============================================================================
package shared.util;

/**
 * CVS_ID:  $Id$
 */
public class AxisBeginSourceToNameConverter {
   /**
    * DOCUMENT ME!
    *
    * @param       value    DOCUMENT ME!
    *
    * @return 	DOCUMENT ME!
    */
   public static String convertIntToAxisBeginSourceName( int value ) {
      String typeName;
      switch ( value ) {
         case 0:
            typeName = "As in Assembly";
            break;
         case 1:
            typeName = "Estimated by Map Team";
            break;
         case 2:
            typeName = "External";
            break;
         default:
            typeName = "Invalid - erroneous data";
      }
      return typeName;
   }
}

/*
  $Log$
  Revision 1.5  2002/12/19 15:23:17  lblick
  Fixed build problem.

  Revision 1.4  2002/11/07 16:11:37  lblick
  Removed obsolete imports and unused local variables.

  Revision 1.3  2002/06/13 00:15:51  tsaf
  Re-entering these two util classes.  My bad.

  Revision 1.1  2000/05/02 15:16:19  jbaxenda
  Changed GenomicAxis to be a GenomicEntity sub type.

*/
