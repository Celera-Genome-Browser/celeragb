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
package shared.util;
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
*********************************************************************/

public class EntityTypeToNameConverter {
   public static String convertIntToEntityTypeName(int value) {
      String typeName;
      switch (value) {
         case 0 :
            typeName = "Gap_Filler";
            break;
         case 1 :
            typeName = "Contig";
            break;
         case 2 :
            typeName = "Scaffold";
            break;
         case 3 :
            typeName = "STS";
            break;
         case 4 :
            typeName = "Clone";
            break;
         case 5 :
            typeName = "Cyto";
            break;
         case 6 :
            typeName = "Bin";
            break;
         default :
            typeName = "Unknown";
      }
      return typeName;
   }
}

/*
  $Log$
  Revision 1.2  2002/11/07 16:11:38  lblick
  Removed obsolete imports and unused local variables.

  Revision 1.1  2000/04/27 16:00:49  jbaxenda
  Converts database codes to names for Genomic Entities.

*/
