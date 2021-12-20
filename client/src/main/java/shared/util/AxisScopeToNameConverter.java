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

public class AxisScopeToNameConverter {
   public static String convertIntToAxisScopeName(int value) {
      String typeName;
      switch (value) {
         case 0 :
            typeName = "Chr Arm";
            break;
         case 1 :
            typeName = "Partial Arm";
            break;
         case 2 :
            typeName = "Unknown";
            break;
         case 3 :
            typeName = "Experimental";
            break;
         case 4 :
            typeName = "Genome";
            break;
         case 5 :
            typeName = "Chromosome";
            break;
         case 6 :
            typeName = "Annotation";
            break;
         default :
            typeName = "Invalid - erroneous data";
      }
      return typeName;
   }
}

/*
  $Log$
  Revision 1.5  2002/11/07 16:11:38  lblick
  Removed obsolete imports and unused local variables.

  Revision 1.4  2002/06/13 00:15:51  tsaf
  Re-entering these two util classes.  My bad.

  Revision 1.2  2000/08/17 19:35:33  lblick
  Updated for IDS 3.1.1 schema.

  Revision 1.1  2000/05/02 15:16:19  jbaxenda
  Changed GenomicAxis to be a GenomicEntity sub type.

  Revision 1.1  2000/04/27 16:00:49  jbaxenda
  Converts database codes to names for Genomic Entities.

*/
