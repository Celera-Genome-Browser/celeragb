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
import api.entity_model.model.fundtype.SingleAlignmentSingleAxis;

import java.util.Comparator;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

public class SingleGeoAlignSingleAxisComparator implements Comparator {
  private boolean ascending = true;
  private GeometricAlignmentComparator geoComparator;


  /**
   * Constructor no-args.
   */
  public SingleGeoAlignSingleAxisComparator() {
    this(true);
  }


  /**
   * Constructor no-args.
   */
  public SingleGeoAlignSingleAxisComparator(boolean ascending) {
    this.ascending = ascending;
    geoComparator = new GeometricAlignmentComparator(ascending);
  }


  /**
   * Compare two Feature using thier ONLY Geometric Alignments to ANY axis.
   * Compares its two arguments for order.  Returns a negative integer,
   * zero, or a positive integer as the first argument is less than, equal
   * to, or greater than the second.<p>
   */
  public int compare(Object o1, Object o2) {
    SingleAlignmentSingleAxis sasaFeat1, sasaFeat2;
    GeometricAlignment ga1, ga2;
    sasaFeat1 = (SingleAlignmentSingleAxis)o1;
    sasaFeat2 = (SingleAlignmentSingleAxis)o2;
    ga1 = sasaFeat1.getOnlyGeometricAlignmentToOnlyAxis();
    ga2 = sasaFeat2.getOnlyGeometricAlignmentToOnlyAxis();
    if (ga1 == null) return -1;
    else if (ga2 == null) return 1;
    return geoComparator.compare(ga1, ga2);
  }


}