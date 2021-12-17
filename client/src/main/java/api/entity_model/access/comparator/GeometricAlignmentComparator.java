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

import java.util.Comparator;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 *
 * This is a comparator that takes two GeometricAlignments and compares them.
 * @todo: test it.
 */

public class GeometricAlignmentComparator implements Comparator {
  private boolean ascending = true;


  /**
   * Constructor no-args.
   */
  public GeometricAlignmentComparator() {
    this(true);
  }

  /**
   * Constructor with ascending arg.
   */
  public GeometricAlignmentComparator(boolean ascending) {
    this.ascending = ascending;
  }


  /**
   * Compare two Geometric Alignments.
   * Compares its two arguments for order.  Returns a negative integer,
   * zero, or a positive integer as the first argument is less than, equal
   * to, or greater than the second.<p>
   */
  public int compare(Object o1, Object o2) {
    GeometricAlignment ga1, ga2;
    ga1 = (GeometricAlignment)o1;
    ga2 = (GeometricAlignment)o2;
    if (ascending) return (ga1.getRangeOnAxis().getMinimum() - ga2.getRangeOnAxis().getMinimum());
    return (ga2.getRangeOnAxis().getMaximum() - ga1.getRangeOnAxis().getMaximum());
  }
}