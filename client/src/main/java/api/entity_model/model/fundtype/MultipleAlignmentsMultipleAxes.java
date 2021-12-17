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
package api.entity_model.model.fundtype;


import api.entity_model.access.filter.AlignmentCollectionFilter;

import java.util.List;
import java.util.Set;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 *
 * This interface represents AlignableGenomicEntity (subtypes) which;
 * - may be aligned to multiple axes.
 * - may be aligned many times to each of the axes.
 */
public interface MultipleAlignmentsMultipleAxes {

  /**
   * This method will return a subSet, sortedSet or sortedSubSet of alignments
   * to axis depending on the CollectionFilter.
   *
   * @see api.entity_model.access.filter.CollectionFilter
   */
  public List getAlignmentsToAxes(AlignmentCollectionFilter filter);

  /**
   * Returns a collection of alignments to the passed axis
   */
  public Set getAlignmentsToAxis(Axis anAxis);

  /**
   * @return a collection of GeometricAlignments to one of the (many) Axes this entity is aligned to.
   * Alignments that are NOT GeometricAlignments, will not be returned in the colleciton.
   */
  public Set getAllGeometricAlignmentsToAxis(Axis anAxis);

  /**
   * @return a collection of GeometricAlignments to all Axes this entity is aligned to.
   * Alignments that are NOT GeometricAlignments, will not be returned in the colleciton.
   */
  public Set getAllGeometricAlignmentsToAxes();

  /**
   * @return All the alignments to all Axes.
   */
  public Set getAlignmentsToAxes();
}