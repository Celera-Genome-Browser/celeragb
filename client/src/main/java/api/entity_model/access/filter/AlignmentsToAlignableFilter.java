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

/**
 * Title:        Genome Browser<p>
 * Description:  <p>
 * @author Peter Davies
 * @version $Id$
 *
 * This Alignment filter only includes those alignments that point to a particular
 * AlignableGenomicEntity.
 */
package api.entity_model.access.filter;


import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.fundtype.AlignableGenomicEntity;

import java.util.Comparator;


public class AlignmentsToAlignableFilter extends AlignmentCollectionFilter {
  AlignableGenomicEntity theAlignableEntity;

  /**
   * Constructors...
   */
  public AlignmentsToAlignableFilter(AlignableGenomicEntity alignable) {
    this(alignable, null);
  }

  public AlignmentsToAlignableFilter(AlignableGenomicEntity alignable, Comparator comparator) {
    super(comparator);
    this.theAlignableEntity = alignable;
  }


  /**
   * Determine if an alignment should be included in the collection...
   */
  public boolean addAlignmentToReturnCollection(Alignment parm1) {
    return (parm1.getEntity() == theAlignableEntity);
  }
}