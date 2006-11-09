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

import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.fundtype.EntityTypeSet;
import api.stub.data.FeatureDisplayPriority;
import api.stub.geometry.Range;
import api.stub.geometry.RangeSet;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

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

public abstract class AlignmentCollectionFilter extends CollectionFilter {

  public AlignmentCollectionFilter(){}

  public AlignmentCollectionFilter(Comparator comparator) {
     super(comparator);
  }

  /**
   * This method must be overridden to return a filtered collection
   */
  public abstract boolean addAlignmentToReturnCollection(Alignment alignment);


  /**
   * Static method that will construct a SortOnlyCollectionFilter.
   */
  public static AlignmentCollectionFilter createSortOnlyAlignmentCollectionFilter(Comparator comparator) {
     return new SortOnlyAlignmentCollectionFilter(comparator);
  }

  /**
   * Static method that will construct a RangeFilter.
   *
   * @return a collection filter that will only return the alignments in  the
   * given range
   */
  public static AlignmentCollectionFilter createAlignmentCollectionFilter(Range axisRange) {
     return new ConcreteCollectionFilter(axisRange);
  }

  /**
   * Static method that will construct a EntityTypeFilter.
   *
   * @return a collection filter that will only return the entity types specified
   */
  public static AlignmentCollectionFilter createAlignmentCollectionFilter(EntityTypeSet entityTypeSet) {
     return new ConcreteCollectionFilter(entityTypeSet);
  }

  /**
   * Static method that will construct a EntityTypeFilter.
   *
   * @return a collection filter that will only return the entity types specified
   *  in the given range
   *
   */
  public static AlignmentCollectionFilter createAlignmentCollectionFilter(
    EntityTypeSet entityTypeSet, Range axisRange) {
     return new ConcreteCollectionFilter(entityTypeSet,axisRange);
  }

  /**
   * Static method that will construct a EntityTypeFilter.
   *
   * @return a collection filter that will only return the entity types specified
   *  in the given range
   *
   */
  public static AlignmentCollectionFilter createAlignmentCollectionFilter(
    EntityTypeSet entityTypeSet, Set axisRangeSet) {
     return new ConcreteCollectionFilter(entityTypeSet,axisRangeSet);
  }

  /**
   * Static method that will construct a EntityTypeFilter.
   *
   * @return a collection filter that will only return the entity types specified
   *  in the given range
   *
   */
  public static AlignmentCollectionFilter createAlignmentCollectionFilter(
    EntityTypeSet entityTypeSet, Set axisRangeSet, boolean useRangesForStrand) {
     return new ConcreteCollectionFilter(entityTypeSet,axisRangeSet,useRangesForStrand);
  }

  /**
   * Static method that will construct a FeatureDisplayPriorityFilter.
   *
   * @return a collection filter that will only return features with the proper
   * FeatureDisplayPriority.
   *
   */
  public static AlignmentCollectionFilter createAlignmentCollectionFilter(
    FeatureDisplayPriority featureDisplayPriority, boolean includeNonFeatures) {
     return new FeatureDisplayFilter(featureDisplayPriority,includeNonFeatures);
  }

  /**
   * Inner class used for return of static createSortOnlyCollectionFilter
   */
  private static final class SortOnlyAlignmentCollectionFilter extends AlignmentCollectionFilter {
     private SortOnlyAlignmentCollectionFilter(Comparator comparator) {
        super(comparator);
     }

     public boolean requestFilteredCollection() {
         return false;
     }

     public boolean addAlignmentToReturnCollection(Alignment alignment) {
        return true;
     }
  }

  private static final class FeatureDisplayFilter extends AlignmentCollectionFilter {
    private FeatureDisplayPriority featureDisplayPriority;
    private boolean includeNonFeatures;

    private FeatureDisplayFilter(FeatureDisplayPriority featureDisplayPriority,boolean includeNonFeatures) {
      this.featureDisplayPriority=featureDisplayPriority;
      this.includeNonFeatures=includeNonFeatures;
    }

     public boolean requestFilteredCollection() {
         return true;
     }

     public boolean addAlignmentToReturnCollection(Alignment alignment) {
        if (alignment.getEntity() instanceof Feature) {
          if (featureDisplayPriority.contains(((Feature)alignment.getEntity()).getDisplayPriority())) return true;
          else return false;
        }
        if (includeNonFeatures) return true;
        else return false;
     }

  }

  /**
   * Inner class used for return of static createAlignmentCollectionFilter
   */
  private static final class ConcreteCollectionFilter extends AlignmentCollectionFilter {
    private Set rangeSetOnAxis;
    private EntityTypeSet entityTypeSet;
    private boolean useRangesForStrand;

     private ConcreteCollectionFilter(Range rangeOnAxis) {
       rangeSetOnAxis=new RangeSet();
       rangeSetOnAxis.add((Range)rangeOnAxis);
     }

     private ConcreteCollectionFilter(EntityTypeSet entityTypeSet) {
       this.entityTypeSet=entityTypeSet;
     }

     private ConcreteCollectionFilter(EntityTypeSet entityTypeSet, Range rangeOnAxis) {
       this(rangeOnAxis);
       this.entityTypeSet=entityTypeSet;
     }

     private ConcreteCollectionFilter(EntityTypeSet entityTypeSet, Set rangeSetOnAxis) {
       this.rangeSetOnAxis=rangeSetOnAxis;
       this.entityTypeSet=entityTypeSet;
     }

     private ConcreteCollectionFilter(EntityTypeSet entityTypeSet, Set rangeSetOnAxis, boolean useRangesForStrand) {
       this.rangeSetOnAxis=rangeSetOnAxis;
       this.entityTypeSet=entityTypeSet;
       this.useRangesForStrand=useRangesForStrand;
     }

     public boolean requestFilteredCollection() {
         return true;
     }

     public boolean addAlignmentToReturnCollection(Alignment alignment) {
        if (entityTypeSet!=null) {
          if (!entityTypeSet.contains(alignment.getEntity().getEntityType())) return false;
        }
        if (rangeSetOnAxis != null) {
          Range rangeOnAxis=((GeometricAlignment)alignment).getRangeOnAxis();
          Object obj;
          if (!(alignment instanceof GeometricAlignment)) return false;
          for (Iterator it=rangeSetOnAxis.iterator();it.hasNext(); ){
            obj=it.next();
            if (useRangesForStrand) {
              if (obj instanceof Range &&
                (!(rangeOnAxis.isForwardOrientation() ^ ((Range)obj).isForwardOrientation())) && //!xor the ranges
                ((Range)obj).contains(rangeOnAxis)) return true;
            }
            else {
              if (obj instanceof Range && ((Range)obj).contains(rangeOnAxis)) return true;
            }
          }
          return false;

        }
        return true;
     }
  }
}