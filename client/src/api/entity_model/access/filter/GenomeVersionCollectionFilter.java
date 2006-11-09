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
package api.entity_model.access.filter;

import api.entity_model.model.genetics.GenomeVersion;

import java.util.Comparator;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version      $Id$
 *
 * This class allows filtering of returned collections.  It encapsulates two
 * concepts, filtering and sorting.  It can do any of the following:
 *
 * 1) return the full collection as is - probably should not be used for this
 *      Other methods are provided.
 * 2) return a partial collection - subClass and override
 *      addAlignmentToReturnCollection, call no-arg constructor
 * 3) return a sorted collection - call static createSortOnlyCollectionFilter
 *      passing a Comparator to get a CollectionFilter instance that will do this
 * 4) return a partial collection that is sorted - subClass and override
 *      addAlignmentToReturnCollection, call constructor that takes a comparator
 *
 *
 * Filtering:  Every alignment that should be returned normally, will be passed
 * to addAlignmentToReturnCollection.  The method then has the
 * ability to approve or disapprove the alignment.  It can do this based on the
 * type of alignment, or the type of the entity or axis the alignment is refering to.
 * If can also approve or disapprove based on the range of the alignment or any
 * accessable criteria.
 *
 * Sorting: Using a comparator, the returned Collection will be sorted.
 */

public abstract class GenomeVersionCollectionFilter extends CollectionFilter {

  public GenomeVersionCollectionFilter(){}

  public GenomeVersionCollectionFilter(Comparator comparator) {
     super(comparator);
  }

  /**
   * This method must be overridden to return a filtered collection
   */
  public abstract boolean addGenomeVersionToReturnCollection(GenomeVersion genomeVersion);


  /**
   * Static method that will construct a SortOnlyCollectionFilter.
   */
  public static GenomeVersionCollectionFilter createSortOnlyGenomeVersionCollectionFilter(Comparator comparator) {
     return new SortOnlyGenomeVersionCollectionFilter(comparator);
  }

  /**
   * Inner class used for return of static createSortOnlyCollectionFilter
   */
  private static final class SortOnlyGenomeVersionCollectionFilter extends
     GenomeVersionCollectionFilter {

     private SortOnlyGenomeVersionCollectionFilter(Comparator comparator) {
        super(comparator);
     }

     public boolean requestFilteredCollection() {
         return false;
     }

     public boolean addGenomeVersionToReturnCollection(GenomeVersion genomeVersion) {
        return true;
     }
  }
}