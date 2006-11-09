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
package api.entity_model.access.comparator;


import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.SingleAlignmentMultipleAxes;

import java.util.Comparator;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

public class SingleGeoAlignMultipleAxesComparator implements Comparator  {
  private Axis axis;
  private boolean ascending = true;
  private GeometricAlignmentComparator geoComparator;

  public SingleGeoAlignMultipleAxesComparator(Axis theAxis) {
    this(theAxis, true);
  }


  /**
   * Constructor no-args.
   */
  public SingleGeoAlignMultipleAxesComparator(Axis theAxis, boolean ascending) {
    this.axis = theAxis;
    this.ascending = ascending;
    geoComparator = new GeometricAlignmentComparator(ascending);
  }


  /**
   * Compare two Feature using thier ONLY Geometric Alignments to an Axis.
   * Compares its two arguments for order.  Returns a negative integer,
   * zero, or a positive integer as the first argument is less than, equal
   * to, or greater than the second.<p>
   */
  public int compare(Object o1, Object o2) {
    SingleAlignmentMultipleAxes alignableGE1, alignableGE2;
    GeometricAlignment align1, align2;
    alignableGE1 = (SingleAlignmentMultipleAxes)o1;
    alignableGE2 = (SingleAlignmentMultipleAxes)o2;
    align1 = alignableGE1.getOnlyGeometricAlignmentToAnAxis(axis);
    align2 = alignableGE2.getOnlyGeometricAlignmentToAnAxis(axis);
    if (align1 == null) return -1;
    else if (align2 == null) return 1;
    return geoComparator.compare(align1, align2);
  }
}