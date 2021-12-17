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
package api.entity_model.access.command;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Deepali Bhandari
 * @version $Id$
 */

import api.entity_model.management.PropertyMgr;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.annotation.*;
import api.stub.data.OID;

import java.util.Date;
import java.util.HashSet;



public class DoDeleteCuration extends FeatureStructureBoundedCommand {
  private static boolean DEBUG_CLASS = false;
  public static final String COMMAND_NAME = "Delete Curation Alignment";
  private Alignment alignmentToBeRemoved = null;
  private CuratedFeature parentFeature;
  private CuratedGene gene = null;
  // private AlignmentFeatureMemento memento = null;
  private Workspace workspace = null;
  private boolean tobeObsoleted=false;
 //highest feature that remains after execution of the command
  private Feature postCommandSuperFeature = null;

    public DoDeleteCuration(Alignment alignmentToBeRemoved) {
        super(alignmentToBeRemoved.getAxis());
        this.alignmentToBeRemoved = alignmentToBeRemoved;

        parentFeature = (CuratedFeature)((Feature)alignmentToBeRemoved.getEntity()).getSuperFeature();

        if (alignmentToBeRemoved.getEntity() instanceof CuratedGene) {
            gene = (CuratedGene)alignmentToBeRemoved.getEntity();
        }
        else if (alignmentToBeRemoved.getEntity() instanceof CuratedTranscript) {
            gene = (CuratedGene)parentFeature;
        }
        else if (alignmentToBeRemoved.getEntity() instanceof CuratedExon){
            gene = (CuratedGene)parentFeature.getSuperFeature();
        }
        workspace = alignmentToBeRemoved.getEntity().getGenomeVersion().getWorkspace();
    }

  /**
   * constructor used when we want to use this command for
   * obsoletion, removal from of database or not just removal
   * from workspace
   */
  public  DoDeleteCuration(Alignment alignmentToBeRemoved, boolean tobeObsoleted){
    this(alignmentToBeRemoved);
    this.tobeObsoleted=tobeObsoleted;
  }


    /**
     * Invoked BEFORE the command is executed.
     * @returns the set of pre-existing root features that will be affected.
     */
    public HashSet getCommandSourceRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      rootFeatureSet.add(((CuratedFeature)this.alignmentToBeRemoved.getEntity()).getRootFeature());
      return rootFeatureSet;
    }


    /**
     * Invoked AFTER the command is executed.
     * @returns the set of root features that display changes "after" the command has executed.
     */
    public HashSet getCommandResultsRootFeatures() {
      HashSet rootFeatureSet = new HashSet();

      if ((postCommandSuperFeature != null)) {
        rootFeatureSet.add(postCommandSuperFeature);
      }

      //rootFeatureSet.add(postCommandSuperFeature);
      return rootFeatureSet;
    }



    /**
     * Execute the command with out returning the undo.
     * The undo will be created for us by the GeneBoundaryCommand super-class.
     */
    public void executeWithNoUndo() throws Exception {
      if (!((alignmentToBeRemoved.getEntity()).isWorkspace())) {
        if (DEBUG_CLASS) System.out.println("DoDeleteCuration called on a non-Scratch curation!  Will not Delete!");
        return;
      }
      CuratedFeature featureBeforeDeletion = (CuratedFeature)alignmentToBeRemoved.getEntity();
      if (DEBUG_CLASS) {
        System.out.println("Feature to be deleted = " +
                            featureBeforeDeletion.getEntityType().getEntityName() +
                            ":OID=" + featureBeforeDeletion.getOid());
      }



      // Simple DELETE for Promoted Clone
      if (featureBeforeDeletion.isScratchReplacingPromoted() && !tobeObsoleted) {
        //featureBeforeDeletion is actually going to be the rootFeature hence....
        postCommandSuperFeature=null;
        Feature rootFeature = featureBeforeDeletion.getRootFeature();
        doDeleteCurationAlignment((CuratedFeature)rootFeature);
      }

      //DELETE for feature from precompute
      else if (!featureBeforeDeletion.isScratchReplacingPromoted() && !tobeObsoleted) {
         //case where the tobedeleted feature is ROOT feature
         if(featureBeforeDeletion.getRootFeature().equals(featureBeforeDeletion)){
           postCommandSuperFeature=null;
         }
         // case where the tobedeleted feature results in deletion of the Superfeature
         else if (featureBeforeDeletion instanceof CuratedTranscript){
           CuratedGene cg=(CuratedGene)featureBeforeDeletion.getSuperFeature();
           if(cg!=null && cg.getSubFeatureCount()==1){
             postCommandSuperFeature=null;
           }else{
             postCommandSuperFeature=featureBeforeDeletion.getRootFeature();
           }
         }
         else if (!(featureBeforeDeletion instanceof SuperFeature)){
           CuratedTranscript ct=(CuratedTranscript)featureBeforeDeletion.getSuperFeature();
           CuratedGene cg=(CuratedGene)ct.getSuperFeature();
           if(cg!=null && ct.getSubFeatureCount()==1 && cg.getSubFeatureCount()==1){
             postCommandSuperFeature=null;
           }else if(cg==null && ct.getSubFeatureCount()==1){
             postCommandSuperFeature=null;
           }else{
             postCommandSuperFeature=featureBeforeDeletion.getRootFeature();
           }
         }
         doDeleteCurationAlignment(featureBeforeDeletion);
      }

      //OBSOLETE, just unalign
      else if (featureBeforeDeletion.isScratchReplacingPromoted() && tobeObsoleted) {
        //case where the tobedeleted feature is ROOT feature
         if(featureBeforeDeletion.getRootFeature().equals(featureBeforeDeletion)){
           postCommandSuperFeature=null;
         }
         // case where the tobedeleted feature results in deletion of the Superfeature
         else if (featureBeforeDeletion instanceof CuratedTranscript){
           CuratedGene cg=(CuratedGene)featureBeforeDeletion.getSuperFeature();
           if(cg!=null && cg.getSubFeatureCount()==1){
             postCommandSuperFeature=null;
           }else{
             postCommandSuperFeature=featureBeforeDeletion.getRootFeature();
           }
         }

         else if (!(featureBeforeDeletion instanceof SuperFeature)){
            CuratedTranscript ct=(CuratedTranscript)featureBeforeDeletion.getSuperFeature();
           CuratedGene cg=(CuratedGene)ct.getSuperFeature();
           if(cg!=null && ct.getSubFeatureCount()==1 && cg.getSubFeatureCount()==1){
             postCommandSuperFeature=null;
           }else if(cg==null && ct.getSubFeatureCount()==1){
             postCommandSuperFeature=null;
           }else{
             postCommandSuperFeature=featureBeforeDeletion.getRootFeature();
           }
         }

         CuratedFeature entityToBeObsoleted=(CuratedFeature)(alignmentToBeRemoved.getEntity());
         CuratedFeature.CuratedFeatureMutator curatedFeatureMutator;
         curatedFeatureMutator = mutatorAcceptor.getCuratedFeatureMutatorFor(entityToBeObsoleted);
         curatedFeatureMutator.removeSubStructureAndRemoveAlignments(false);
         if(entityToBeObsoleted instanceof CuratedExon){
            curatedFeatureMutator=mutatorAcceptor.getCuratedFeatureMutatorFor((CuratedFeature)entityToBeObsoleted.getSuperFeature());
            // invoking again to order the exons.
            curatedFeatureMutator.updatePropertiesBasedOnGeometricAlignment(((CuratedFeature)entityToBeObsoleted.getSuperFeature()).getOnlyGeometricAlignmentToOnlyAxis());
         }

      }



      if (parentFeature != null && (!parentFeature.hasSubFeatures() || parentFeature.getOnlyAlignmentToAnAxis(axis) == null))
          parentFeature = null;
      setFocusEntity(parentFeature);

      timeofCommandExecution = new Date().toString();
    }

    protected OID getUndoFocusOID() {
        return alignmentToBeRemoved.getEntity().getOid();
    }

    protected OID getRedoFocusOID() {
      return (parentFeature != null && parentFeature.hasSubFeatures()) ? parentFeature.getOid() : null;
    }

  private void doDeleteCurationAlignment(CuratedFeature curatedFeature){

    // first cache off the super features for the property update after the subfeature
    // deletion
    CuratedFeature ct=null;
    CuratedFeature cg=null;
    if(curatedFeature instanceof CuratedExon){
       ct=(CuratedFeature)curatedFeature.getSuperFeature();
       cg=(CuratedGene)ct.getSuperFeature();
    }else if(curatedFeature instanceof CuratedTranscript){
       ct=(CuratedTranscript)curatedFeature;
       cg=(CuratedGene)curatedFeature.getSuperFeature();
    }

    CuratedFeature.CuratedFeatureMutator curatedFeatureMutator;
    curatedFeatureMutator = mutatorAcceptor.getCuratedFeatureMutatorFor(curatedFeature);
    if (curatedFeatureMutator != null) {
      // Need to grab the  workspace BEFORE removing the features because AFTER
      // un-aligning them they no longer belong to a GenomeVersion...
      // Remove the sub-structure and remove thier alignments...
      System.out.println("DoDeleteCuration... remove substrcuture on: " +
                          curatedFeature.getEntityType().getEntityName() + ":"
                          + curatedFeature.getOid());
      curatedFeatureMutator.removeSubStructureAndRemoveAlignments(true);

    }


    // update transcript for properties after delete
     if(ct!=null){
      CuratedTranscript.CuratedTranscriptMutator transcriptMutator;
      transcriptMutator = mutatorAcceptor.getCuratedTranscriptMutatorFor((CuratedTranscript)ct);
      transcriptMutator.updateAllSuperFeatureGeomAlignmentsBasedOnSubFeatures();// will also update the geomteric properties
      transcriptMutator.updatePropertiesBasedOnGeometricAlignment(ct.getOnlyGeometricAlignmentToOnlyAxis());
      PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.UPDATE_ENTITY, ct, false);
    }

    // update gene for properties after delete
    if(cg!=null){
      CuratedGene.CuratedGeneMutator geneMutator;
      geneMutator = mutatorAcceptor.getCuratedGeneMutatorFor((CuratedGene)cg);
      geneMutator.updatePropertiesBasedOnGeometricAlignment(cg.getOnlyGeometricAlignmentToOnlyAxis());
      PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.UPDATE_ENTITY, cg, false);
    }

  }



     /**
     * toString() will return the name of the command.
     */
    public String toString() {
        return COMMAND_NAME;
    }

     public String getCommandLogMessage(){

       String deletedEntityOid=alignmentToBeRemoved.getEntity().getOid().toString();
       String deletedEntityType=alignmentToBeRemoved.getEntity().getEntityType().toString();
       String deletedOrObsoleted;
       String postCommandSuperFeatureStatus;
       if(postCommandSuperFeature==null){
         postCommandSuperFeatureStatus="SuperFeature removal";
       }else{
         postCommandSuperFeatureStatus="no removal of SuperFeature";
       }
       if(tobeObsoleted){
         deletedOrObsoleted="Obsoleted";
       }else{
         deletedOrObsoleted="Deleted";
       }
       this.actionStr=deletedOrObsoleted+" id= "+deletedEntityOid+" with featureType="+deletedEntityType+", results in "+postCommandSuperFeatureStatus;
       return(super.getCommandLogMessage());
      }
}
