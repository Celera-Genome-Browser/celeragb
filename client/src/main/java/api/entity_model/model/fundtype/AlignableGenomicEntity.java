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
import api.entity_model.access.filter.FiltrationDevice;
import api.entity_model.access.observer.AlignableGenomicEntityObserver;
import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.AlignmentNotAllowedException;
import api.entity_model.model.alignment.GeometricAlignment;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.NavigationNode;
import api.stub.data.OID;
import api.stub.geometry.Range;

import java.util.*;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version $Id$
 */


public abstract class AlignableGenomicEntity extends GenomicEntity
                          implements MultipleAlignmentsMultipleAxes {

   /**
       *@associates <{api.entity_model.model.alignment.Alignment}>
       * @supplierCardinality 0..*
       * @label axis alignments
       */
    transient private HashSet alignmentsToAxes; //Alignment

    // Types of actions that Observers will be notified of.
    private static final int NOTE_ALIGNED_TO_AXIS = 10;
    private static final int NOTE_UNALIGNED_FROM_AXIS = 11;
    private static final int NOTE_ALIGNMENT_TO_AXIS_CHANGED = 12;

    private static final long serialVersionUID=6;

//****************************************
//*  Public methods
//****************************************

 /**
  * @see GenomicEntity
  */
  public AlignableGenomicEntity(EntityType entityType, OID oid, String displayName)
  {
    this(entityType, oid, displayName, null);
  }

 /**
  * @see GenomicEntity
  */
  public AlignableGenomicEntity(EntityType entityType, OID oid, String displayName, FacadeManagerBase overrideDataLoader)
  {
    super(entityType, oid, displayName, overrideDataLoader);
  }


   /**
    * For Alignable Genomic Entity(s), if it has no alignment, it doesn't belong
    * to a GenomeVersion any more.
   public GenomeVersion getGenomeVersion() {
      if (!this.hasAnyAlignments()) return null;
      return super.getGenomeVersion();
   }
    */

//*  Observation

    /**
     *  To track changes in the this object over time, you must add
     *  yourself as an observer of the it.  NOTE: this will post
     *  notices of all existing children
     *  @see AlignableGenomicEntityObserver.java
     */
    public void addAlignableGenomicEntityObserver(AlignableGenomicEntityObserver observer) {
       addAlignableGenomicEntityObserver(observer, true);
    }

    /**
     *  To track changes in the this object over time, you must add
     *  yourself as an observer of the it.  NOTE: this will optionally post
     *  notices of all existing children
     *  @see AlignableGenomicEntityObserver.java
     */
    public void addAlignableGenomicEntityObserver(AlignableGenomicEntityObserver observer,boolean bringUpToDate) {
       addGenomicEntityObserver(observer,bringUpToDate);
       if (bringUpToDate) {
          if (alignmentsToAxes!=null) {
             Alignment[] alignments=getAlignmentsToAxesArray();
             for (int i=0;i<alignments.length;i++) {
                observer.noteAlignedToAxis(alignments[i]);
             }
          }
       }
    }

    /**
     * Remove an existing observer
     */
    public void removeAlignableGenomicEntityObserver(AlignableGenomicEntityObserver observer) {
       removeGenomicEntityObserver(observer);
    }

    /**
     * Accepts visitors that implement GenomicEntityVisitor. acceptVisitorForSelf
     * is specialized by every subclass of GenomicEntity to call the appropriate
     * "visit...(...)" function on the passed visitor.
     * @param theVisitor the visitor.
     */
    public void acceptVisitorForSelf(GenomicEntityVisitor theVisitor) {
        try {
                theVisitor.visitAlignableGenomicEntity(this);
        }
        catch (Exception ex) {
                handleException(ex);
        }
    }

    /**
     * @return Set of all Acenstor Axes that are of the passed class
     *
     * class==ancestorClass is used, not instanceof, so passing something like GenomicEntity
     * will not work.
     */
    public Set getAllAncestorAxesOfClass(Class  ancestorClass) {
       Set set=new HashSet();
       getAllAncestorAxesHelper(set,ancestorClass);
       return set;
    }

    /**
     * @return Set of all Acenstor Axes
     */
    public Set getAllAncestorAxes() {
       Set set=new HashSet();
       getAllAncestorAxesHelper(set,null);
       return set;
    }

   protected int getNavigationNodeType() {
     return NavigationNode.UNKNOWN;
   }

//== Support for alignments ==============================


    /**
     * Returns a Set of alignments to the passed axis.
     * Support for MultipleAlignmentsMultipleAxes interface.
     */
    public Set getAlignmentsToAxis(Axis anAxis) {
       Set axisAlignments=getAlignmentsToAxes();
       Set rtnSet=new HashSet();
       Alignment tmpAlignment;
       for (Iterator it=axisAlignments.iterator();it.hasNext();) {
          tmpAlignment=(Alignment)it.next();
          if (tmpAlignment.getAxis()==anAxis)rtnSet.add(tmpAlignment);
       }
       return rtnSet;
    }



    /**
     * Convenience method to check if it has any alignments.
     */
    public synchronized boolean hasAnyAlignments() {
      if (alignmentsToAxes==null) return false;
      if (alignmentsToAxes.isEmpty()) return false;
      return true;
    }


    /**
     * @return Collection of Alignments to the Axes this entity is aligned to
     * Support for MultipleAlignmentsMultipleAxes interface.
     */
    public synchronized Set getAlignmentsToAxes() {
      if (alignmentsToAxes==null)
      {
        return Collections.EMPTY_SET;
      }
      return (Set)alignmentsToAxes.clone();
    }


    /**
     * @return a collection of GeometricAlignments to one of the (many) Axes this entity is aligned to.
     * Alignments that are NOT GeometricAlignments, this will not be returned in the colleciton.
     * Support for MultipleAlignmentsMultipleAxes interface.
     */
    public Set getAllGeometricAlignmentsToAxis(Axis anAxis) {
      Set alignments = this.getAlignmentsToAxis(anAxis);
      Object[] alignmentArray = alignments.toArray();
      for (int i=0; i<alignmentArray.length; i++) {
        if (!(alignmentArray[i] instanceof GeometricAlignment)) alignments.remove(alignmentArray[i]);
      }
      return alignments;
    }


    /**
     * @return a collection of GeometricAlignments to all Axes this entity is aligned to.
     * Alignments that are NOT GeometricAlignments, will not be returned in the colleciton.
     * Support for MultipleAlignmentsMultipleAxes interface.
     */
    public Set getAllGeometricAlignmentsToAxes() {
      Set alignments = this.getAlignmentsToAxes();
      Object[] alignmentArray = alignments.toArray();
      for (int i=0; i<alignmentArray.length; i++) {
        if (!(alignmentArray[i] instanceof GeometricAlignment)) alignments.remove(alignmentArray[i]);
      }
      return alignments;
    }


    /**
     * This method will return a subSet, sortedSet or sortedSubSet of alignments
     * to axis depending on the CollectionFilter.
     * Support for MultipleAlignmentsMultipleAxes interface.
     *
     * @see api.entity_model.access.filter.CollectionFilter
     */
    public List getAlignmentsToAxes(AlignmentCollectionFilter filter) {
      return
        FiltrationDevice.getDevice().executeAlignmentFilter(getAlignmentsToAxes(),filter);
    }


//****************************************
//*  Protected methods
//****************************************

    /**
     * @return The single Alignment to one of the (many) Axes this entity is aligned to.
     * Support for SingleAlignmentsMultipleAxes interface.
     * Returns the first alignment found to this axis.
     */
    protected Alignment getOnlyAlignmentToAnAxis(Axis anAxis) {
      if (anAxis == null) return null;
      Collection alignments = this.getAlignmentsToAxis(anAxis);
      Alignment anAlign = null;
      if (alignments != null) {
        Iterator itr = alignments.iterator();
        if (itr.hasNext()) anAlign = (Alignment)itr.next();
      }
      return anAlign;
    }



    /**
     * @return The single GeometricAlignment to one of the (many) Axes this entity is aligned to.
     * If the alignment is NOT a GeometricAlignment, this will return null.
     * Support for SingleAlignmentsMultipleAxes interface.
     * Returns the first geometric alignment to the axis found.
     */
    protected GeometricAlignment getOnlyGeometricAlignmentToAnAxis(Axis anAxis) {
      if (anAxis == null) return null;
      Collection alignments = this.getAlignmentsToAxis(anAxis);
      Object anAlign = null;
      GeometricAlignment aGeoAlign = null;
      if (alignments != null) {
        for (Iterator itr = alignments.iterator(); itr.hasNext(); ) {
          anAlign = itr.next();
          if (anAlign instanceof GeometricAlignment) {
            aGeoAlign = (GeometricAlignment)anAlign;
            break;
          }
        }
      }
      return aGeoAlign;
    }


    /**
     * @return Collection of Alignments to the one Axis this entity is aligned to.
     * All Alignments returned in the collection are guaranteed to be to the same
     * axis.
     * Support for MultipleAlignmentsSingleAxis interface.
     * Uses the axis of the first alignment found to be the "control" axis,
     * and only returns alignments to that axis.
     */
    protected Set getAllAlignmentsToOnlyAxis() {
      Axis controlAxis = null;
      Alignment anAlignment;
      Set alignments = this.getAlignmentsToAxes();
      Object[] alignmentArray = alignments.toArray();
      for (int i=0; i<alignmentArray.length; i++) {
        anAlignment = (Alignment)alignmentArray[i];
        if (controlAxis != null) controlAxis = anAlignment.getAxis();
        else if (anAlignment.getAxis() != controlAxis) alignments.remove(controlAxis);
      }
      return alignments;
    }


    /**
     * @return Collection of GeometricAlignments to the one Axis this entity is aligned to.
     * All Alignments returned in the collection are guaranteed to be to the same
     * axis and are guaranteed to be Geometric alignments.
     * Support for MultipleAlignmentsSingleAxis interface.
     * Uses the axis of the first geometric alignment found to be the "control" axis,
     * and only returns Geometric alignments to that axis.
     */
    protected Set getAllGeometricAlignmentsToOnlyAxis() {
      Axis controlAxis = null;
      GeometricAlignment aGeoAlignment;
      Set alignments = this.getAlignmentsToAxes();
      Object[] alignmentArray = alignments.toArray();
      for (int i=0; i<alignmentArray.length; i++) {
        if (!(alignmentArray[i] instanceof GeometricAlignment)) alignments.remove(alignmentArray[i]);
        else {
          aGeoAlignment = (GeometricAlignment)alignmentArray[i];
          if (controlAxis != null) controlAxis = aGeoAlignment.getAxis();
          else if (aGeoAlignment.getAxis() != controlAxis) alignments.remove(controlAxis);
        }
      }
      return alignments;
    }


    /**
     * @return The single Alignment to the one Axis this entity is aligned to.
     * Support for SingleAlignmentSingleAxis interface.
     * Returns the first alignment in the collection.
     */
    protected Alignment getOnlyAlignmentToOnlyAxis() {
      Collection alignments = this.getAlignmentsToAxes();
      Alignment anAlign = null;
      if (alignments != null) {
        Iterator itr = alignments.iterator();
        if (itr.hasNext()) anAlign = (Alignment)itr.next();
      }
      return anAlign;
    }


    /**
     * @return The single GeometricAlignment to the one Axis this entity is aligned to.
     * If the alignment is NOT a GeometricAlignment, this will return null.
     * Support for SingleAlignmentSingleAxis interface.
     * Returns the first Geometric alignment to ANY axis it finds.
     */
    protected GeometricAlignment getOnlyGeometricAlignmentToOnlyAxis() {
      Collection alignments = this.getAlignmentsToAxes();
      Object anAlign = null;
      GeometricAlignment aGeoAlign = null;
      if (alignments != null) {
        for(Iterator itr = alignments.iterator(); itr.hasNext(); ) {
          anAlign = itr.next();
          if (anAlign instanceof GeometricAlignment) {
            aGeoAlign = (GeometricAlignment)anAlign;
            break;
          }
        }
      }
      return aGeoAlign;
    }


    /**
     * Used for unloading.  Will be called on all features within the specified
     * range.
     *
     * @return number of entities this call unaligned (should be 1 except for features)
     *  Scratch features and non-root features should return 0.  Root features
     *  should return the number of features that were unloaded under them, as well
     *  as themselves.
     */

    protected int unloadIfPossible (Alignment alignment) {
      ((AlignableGenomicEntityMutator)getMutator()).removeAlignmentToAxis(alignment);
      return 1;
    }

    /**
     * template pattern for mutator construction
     */
    protected GenomicEntityMutator constructMyMutator() {
        return new AlignableGenomicEntityMutator();
    }


   /**
    * Post notification that this entity was aligned to an axis.
    */
    protected final void postAlignedToAxis ( Alignment newAlignment ) {
       new AlignableEntityNotificationObject(
          newAlignment,NOTE_ALIGNED_TO_AXIS,
          true).run();
       getNotificationQueue().addQueue(new AlignableEntityNotificationObject(
          newAlignment,NOTE_ALIGNED_TO_AXIS,false));
    }

   /**
    * Post notification that this entity was unaligned from an axis.
    */
    protected final void postUnalignedFromAxis ( Alignment oldAlignment ) {
       new AlignableEntityNotificationObject(
          oldAlignment,NOTE_UNALIGNED_FROM_AXIS,
          true).run();
       getNotificationQueue().addQueue(new AlignableEntityNotificationObject(
          oldAlignment,NOTE_UNALIGNED_FROM_AXIS,false));
    }



   /**
    * Post notification that this entity's alignment to an axis changed.
    */
    protected final void postAlignmentToAxisChanged ( Alignment changedAlignment ) {
       new AlignableEntityNotificationObject(
          changedAlignment,NOTE_ALIGNMENT_TO_AXIS_CHANGED,
          true).run();
       getNotificationQueue().addQueue(new AlignableEntityNotificationObject(
          changedAlignment,NOTE_ALIGNMENT_TO_AXIS_CHANGED,
          false));
    }

    /**
     * template pattern for predicting the number of alignments to axes
     */
    protected int getPredictedNumberOfAlignmentsToAxis() {
       return 1;
    }

    /**
     * Aligned Parent Child Validation.
     * Any overrides SHOULD call this version to enforce single alignment.
     */
    protected void willAcceptAlignmentToAxis(Alignment alignmentToAxis)
       throws AlignmentNotAllowedException {

       Set alignmentSet = this.getAlignmentsToAxis(alignmentToAxis.getAxis());

       // First check for duplicate alignments...
       if (alignmentSet.contains(alignmentToAxis)) {
         throw new AlignmentNotAllowedException(true,alignmentToAxis);
       }

       // Now check for multiple alignments...
       if (alignmentSet.size()>0) {
         throw new AlignmentNotAllowedException("The entity has already been aligned to this axis.  "+
            "The entities type does not allow multiple alignments to the same axis.", alignmentToAxis);
       }
    }

    /**
     * Recursive method to get Ancestor Axes
     * @paramter ancestorClass- pass null to not use
     */
    protected void getAllAncestorAxesHelper(Set set,Class ancestorClass){
       Alignment[] alignmentsToAxes=getAlignmentsToAxesArray();
       for (int i=0;i<alignmentsToAxes.length;i++) {
          if (ancestorClass==null) {
             set.add(alignmentsToAxes[i].getAxis());
             alignmentsToAxes[i].getAxis().getAllAncestorAxesHelper(set,ancestorClass);
          }
          else {
             if (alignmentsToAxes[i].getAxis().getClass()==ancestorClass)
                set.add(alignmentsToAxes[i].getAxis());
             alignmentsToAxes[i].getAxis().getAllAncestorAxesHelper(set,ancestorClass);
          }
       }
    }

//****************************************
//*  Package methods
//****************************************



//****************************************
//*  Private methods
//****************************************

    private synchronized void ensureStorageForAlignmentsToAxesAvailable() {
      if (alignmentsToAxes==null) {
         int prediction=getPredictedNumberOfAlignmentsToAxis();
         int initialSize=(int)(prediction+(PREDICTION_PADDING_FACTOR*prediction));
         alignmentsToAxes=new HashSet(initialSize);
      }
    }

    private synchronized void ensureStorageForAlignmentsToAxesRemoved() {
       if (alignmentsToAxes.size()==0) alignmentsToAxes=null;
    }

    private Alignment[] getAlignmentsToAxesArray() {
       synchronized (this) {
          if (alignmentsToAxes==null) return new Alignment[0];
          return (Alignment[])alignmentsToAxes.toArray(
            new Alignment[alignmentsToAxes.size()]);
       }
    }

//****************************************
//*  Inner Classes
//****************************************

  public class AlignableGenomicEntityMutator extends GenomicEntityMutator{
      private static final long serialVersionUID=1L;
      protected AlignableGenomicEntityMutator() {}

    /**
     * Should be called to align an entity to this axis.
     * Should NOT be called if calling from AlignableEntity
     */
      public void addAlignmentToAxis(Alignment alignment)  throws
         AlignmentNotAllowedException {
         if (alignment == null) return;
         ((Axis.AxisMutator)alignment.getAxis().getMutator()).addAlignmentToEntity(alignment);
      }


      public void removeAlignmentToAxis(Alignment alignment) {
         if (alignment == null) return;
         ((Axis.AxisMutator)alignment.getAxis().getMutator()).removeAlignmentToEntity(alignment);
      }

      /**
       * Should NEVER be called by anything except the Axis that it is being aligned
       * to.
       */
      void protectedAddAlignmentToAxis(Alignment alignment) throws
         AlignmentNotAllowedException {
         willAcceptAlignmentToAxis(alignment);
         if (alignment.getEntity()!=AlignableGenomicEntity.this) throw
            new AlignmentNotAllowedException("The alignment passed to "+
              AlignableGenomicEntity.this+" Entity for addition is refering"+
              "to entity "+alignment.getEntity(),alignment);
         ensureStorageForAlignmentsToAxesAvailable();
         synchronized (AlignableGenomicEntity.this) {
           alignmentsToAxes.add(alignment);
         }
      }

      /**
       * Should NEVER be called by anything except the Axis that it is being aligned
       * to.
       */
      void protectedRemoveAlignmentToAxis(Alignment alignment) {
         synchronized (AlignableGenomicEntity.this) {
           if (alignmentsToAxes==null) return;
           alignmentsToAxes.remove(alignment);
         }
         ensureStorageForAlignmentsToAxesRemoved();
      }


      /**
       * Change the Range of a GeometricAlignment for this AlignableGenomicEntity.
       * Will only change the range, if it is one of this AlignableGenomicEntity.
       * Sends a notification of Alignment changed to BOTH the observers of the
       * AlignableGenomicEntity and the observers of the Axis.
       * This method delegates to the same method on Axis.
       */
      public void changeRangeOnAlignment(GeometricAlignment geoAlignment, Range newRange) {
        // Make sure it's one of my
        if (AlignableGenomicEntity.this != geoAlignment.getEntity()) return;
        // Delegate the Axis as the "control".
        ((Axis.AxisMutator)geoAlignment.getAxis().getMutator()).changeRangeOnAlignment(geoAlignment, newRange);
      }


      /**
       * Change the Range of a GeometricAlignment for this AlignableGenomicEntity.
       * Will only change the range, if it is one of this AlignableGenomicEntity.
       * Does NOT send a notification of Alignment changed.
       * This method delegates to the same method on Axis.
       */
      protected void changeRangeOnAlignmentNoNotification(GeometricAlignment geoAlignment, Range newRange) {
        // Make sure it's one of my
        if (AlignableGenomicEntity.this != geoAlignment.getEntity()) return;
        // Delegate the Axis as the "control".
        ((Axis.AxisMutator)geoAlignment.getAxis().getMutator()).changeRangeOnAlignmentNoNotification(geoAlignment, newRange);
      }

  }

  // notification
  protected class AlignableEntityNotificationObject extends
    EntityNotificationObject {

    private Alignment changedAlignment;

    protected AlignableEntityNotificationObject (Alignment changedAlignment,
       int notedAction, boolean notifyModelSyncObservers) {
       super(notedAction,false,notifyModelSyncObservers);
       this.changedAlignment=changedAlignment;
    }

    protected AlignableEntityNotificationObject (int notedAction, boolean notifyModelSyncObservers) {
       super(notedAction,false,notifyModelSyncObservers);
    }

    protected final Alignment getChangedAlignment() {
       return changedAlignment;
    }

    protected Class getObserverFilteringClass() {
       return AlignableGenomicEntityObserver.class;
    }

    public void run() {
       switch (getNotedAction()) {
         case NOTE_ALIGNED_TO_AXIS: {
           sendAlignedToAxisMessage();
           break;
         }
         case NOTE_UNALIGNED_FROM_AXIS: {
           sendUnalignedFromAxisMessage();
           break;
         }
         case NOTE_ALIGNMENT_TO_AXIS_CHANGED: {
           sendAlignmentToAxisChangedMessage();
           break;
         }
         default: {
            super.run();
         }
       }
    }

    private void sendAlignedToAxisMessage() {
        AlignableGenomicEntityObserver observer;
        List observers=getObserversToNotifyAsList();
        for( int i= 0; i< observers.size(); i++ ) {
          observer=(AlignableGenomicEntityObserver)observers.get(i);
          observer.noteAlignedToAxis(changedAlignment);
        }
    }

    private void sendUnalignedFromAxisMessage() {
        AlignableGenomicEntityObserver observer;
        List observers=getObserversToNotifyAsList();
        for( int i= 0; i< observers.size(); i++ ) {
           observer=(AlignableGenomicEntityObserver)observers.get(i);
           observer.noteUnalignedFromAxis(changedAlignment);
        }
    }

    private void sendAlignmentToAxisChangedMessage() {
        AlignableGenomicEntityObserver observer;
        List observers=getObserversToNotifyAsList();
        for( int i= 0; i< observers.size(); i++ ) {
             observer=(AlignableGenomicEntityObserver)observers.get(i);
             observer.noteAxisAlignmentChanged(changedAlignment);
        }
    }


  }

}
