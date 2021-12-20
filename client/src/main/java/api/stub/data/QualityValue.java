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
package api.stub.data;
/*********************************************************************
 *********************************************************************
 CVS_ID$
*********************************************************************/



public class QualityValue implements java.io.Serializable
{
  private long startNucleotidsPos;
  private long endNucleotidePos;
  private short qualityVal;

  // REVISIT:
  // Could potentially store a reference to the GenomicEntity from which the
  // quality value was sources.

  public QualityValue
    (long startNucleotidsPos,
     long endNucleotidePos,
     short qualityVal)
  {
    this.startNucleotidsPos = startNucleotidsPos;
    this.endNucleotidePos = endNucleotidePos;
    this.qualityVal = qualityVal;
  }
}

/*
  $Log$
  Revision 1.2  2002/11/07 18:38:30  lblick
  Removed obsolete imports and unused local variables.

  Revision 1.1  2000/04/14 22:51:20  jbaxenda
  Data class that will allow run length encoded quality value data

*/
