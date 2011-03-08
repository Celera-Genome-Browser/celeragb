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

import api.entity_model.management.CommandPreconditionException;
import api.entity_model.management.ModelMgr;
import api.entity_model.management.PropertyMgr;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.*;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.SingleAlignmentSingleAxis;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.stub.data.OID;

import java.util.*;

/**
 * The DoAddEvidenceAndCreateExonIfNecessary is a CommandToken which (time of writing)
 * handles all "drag from non-curation-tier feature to curation-tier-feature".
 * Two cases exist: 1) the base-pair location of the source feature's HSPs do
 * not have corresponding EXONs in the target tier. 2) the base-pair location
 * of the source HSP is occupied in the target tier.
 *
 * Another way of looking at it: is user dragging straight down to an
 * existing curation, or is user dragging "sideways".  In the first case, user
 * is attempting to add exon(s) to an existing transcript.  In the second
 * case, the user is attempting to add evidence to existing exon(s). User can add forward
 * evidence on reverse exon
 *
 *   Arguments of the command are;
 *   Vector undoMemento = any previous inverted form of this command
 *   Feature evidence = source feature
 *   CuratedTranscript transcript = target feature

 * Is Undoable?  YES
 * Inverse command class is; DoRemoveFromExistingCurationCommand
 * Postconditions & side effects include;
 *   The target will have more exons than previously
 *
 *
 * Algorithm;
 *   NOT UNDO:
 *   Tests whether any of the target feature's exons occupy the same axis
 *   location as the source exon(s).  If so, adds evidence where they overlap.
 *   If not, adds source as Exons of the target transcript.

 *   UNDO:
 *   If this command is run as an undo of another "DoRemoveFromExistingCurationCommand,
 *   (To be completed)
 *
 */
public class DoAddEvidenceAndCreateExonIfNecessary extends FeatureStructureBoundedCommand {
    private String cmdName = "Add To Existing Curation If Necessary";
    private Feature evidence;
    private CuratedTranscript transcript;
    private CuratedExon exonOfFocus;
    private Vector memento = new Vector();
    private Vector undoMemento = null;
    private String undoToScratchState = null;
    // private CuratedTranscript.CuratedTranscriptMutator transcriptMutator=null;
    // private CuratedExon.CuratedExonMutator curatedExonMutator=null;
    // private Feature.FeatureMutator featureMutator=null;

    /**
     * Constuctor used when acting as the original command, and NOT as an Undo...
     */
    public DoAddEvidenceAndCreateExonIfNecessary (Axis anAxis, Feature evidence,
                                                  CuratedTranscript transcript) {
        super(anAxis);
        this.evidence = evidence;
        this.transcript = transcript;
        this.undoMemento = null;
        this.setIsActingAsUndoAndUndoName(false, null);
    }


    /**
     * Constuctor used when acting as an Undo...
     */
    /*
    public DoAddToExistingCurationCommand(Vector undoMemento, FeaturePI evidence,
                  CuratedTranscriptFeatPI transcript, GenomicAxisPI axisPI,
                  String undoCommandName, String undoToScratchState) {
        this.evidence = evidence;
        this.transcript = transcript;
        this.undoMemento = undoMemento;
        this.genomicAxisPI = axisPI;
        this.setIsActingAsUndoAndUndoName(true, undoCommandName);
        this.undoToScratchState = undoToScratchState;
    }
    */


    /**
     * Invoked BEFORE the command is executed.
     * @returns the set of pre-existing root features that will be affected.
     */
    public HashSet getCommandSourceRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      rootFeatureSet.add(transcript.getRootFeature());
      return rootFeatureSet;
    }


    /**
     * Invoked AFTER the command is executed.
     * @returns the set of root features that display changes "after" the command has executed.
     */
    public HashSet getCommandResultsRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      rootFeatureSet.add(transcript.getRootFeature());
      return rootFeatureSet;
    }




    public void validatePreconditions() throws CommandPreconditionException {
       CommandPreconditionException exceptionToThrow;
      if (!transcript.isScratch()) {
        exceptionToThrow = new CommandPreconditionException(this, "Transcript is promoted cannot curate");
        throw exceptionToThrow;
      }
     if((evidence instanceof CuratedFeature)){
        exceptionToThrow = new CommandPreconditionException(this, "Evidence cannot be a CuratedFeature");
        throw exceptionToThrow;
     }
   }





    /**
     * Execute the command with out returning the undo.
     * The undo will be created for us by the GeneBoundaryCommand super-class.
     */
    public void executeWithNoUndo() throws Exception {
        memento = doAddToExistingCuration(undoMemento, evidence, transcript);
        setFocusEntity(exonOfFocus);
        this.timeofCommandExecution=new Date().toString();
    }


    protected OID getUndoFocusOID() {
      return transcript.getOid();
    }

    protected OID getRedoFocusOID() {
      return transcript.getOid();
    }

    private Vector doAddToExistingCuration(Vector undoMemento, Feature evidence, CuratedTranscript transcript) {

      CuratedTranscript.CuratedTranscriptMutator transMutator = mutatorAcceptor.getCuratedTranscriptMutatorFor(transcript);
      // transcript.getMutator(this, "acceptCuratedTranscriptMutator");
      //added undoMemento checking for undoing DoRemoveFromExistinCurationCommand to
      //avoid making another new exon object reference which will cause NullPointerException by following
      //redo DoDeleteCuration
      //  if (undoMemento == null) {
            // if composite evidence, then recurse down through composite to add leafs to existing curation
            if (!evidence.isSimple()&& (((Feature)evidence).getSubFeatureCount()!=0/*evidence instanceof SuperFeature &&((SuperFeature)evidence).getSubFeatures()!=null*/)) {
                // System.out.println("Adding to " + transcript + ", based on composite feature: " + evidence);
                SuperFeature parent_feature = (SuperFeature)evidence;
                Feature sub_feature;
                Collection subFeatures = parent_feature.getSubFeatures();
                Iterator it=subFeatures.iterator();
                while (it.hasNext()) {
                    sub_feature = (Feature)it.next();
                    doAddToExistingCuration(null, sub_feature, transcript);
                }
            }

           else {
              if (!(evidence instanceof SingleAlignmentSingleAxis)) {
                // todo: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s).
                System.out.println("DoAddEvidenceAndCreateExonIfNecessary: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s)");
              }
              Collection involvedExons = transMutator.addEvidenceAndCreateExonIfNecessary(
                    ((SingleAlignmentSingleAxis)evidence).getOnlyGeometricAlignmentToOnlyAxis());
              if (involvedExons == null) return null;
              CuratedExon exonNew = null;
              CuratedExon.CuratedExonMutator curatedExonMutator = null;
              // For each of the exons involved...
              for (Iterator itr = involvedExons.iterator(); itr.hasNext(); ) {
                exonNew = (CuratedExon)itr.next();
                //Now set the properties for all the exons
                PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.NEW_ENTITY, exonNew, false);
                try {
                  PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.NEW_ENTITY, exonNew, false);
                  Alignment exona=((SingleAlignmentSingleAxis)exonNew).getOnlyAlignmentToOnlyAxis();
                  curatedExonMutator = mutatorAcceptor.getCuratedExonMutatorFor(exonNew);
                  curatedExonMutator.updatePropertiesBasedOnGeometricAlignment((GeometricAlignment)exona);
                }
                catch (Exception e) {
                  ModelMgr.getModelMgr().handleException(e);
                }
              }
              // Only need to update the parents for the last...
              // ... assumes that all the exons share the same parent.
              updatePropertiesForParent(exonNew.getSuperFeature());
              updatePropertiesForParent(exonNew.getSuperFeature().getSuperFeature());
              // I think the user assumes that usually only one exon is effected here;
              // in any case, I am focusing on the last one.
              exonOfFocus = exonNew;
           }
           return null;
    }




    private void updatePropertiesForParent(Feature parentFeature){
      if(parentFeature!=null && parentFeature instanceof SingleAlignmentSingleAxis){
        Feature.FeatureMutator featureMutator = mutatorAcceptor.getFeatureMutatorFor(parentFeature);
        featureMutator.updatePropertiesBasedOnGeometricAlignment(((SingleAlignmentSingleAxis)parentFeature).getOnlyGeometricAlignmentToOnlyAxis());
        try{
          PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.UPDATE_ENTITY,parentFeature, false);
        }catch(Exception e){ModelMgr.getModelMgr().handleException(e);}
      }
   }



    public String toString() {
        return cmdName;
    }

     public String getCommandLogMessage(){

       String transcriptAcc=(transcript.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP)).getInitialValue();
       String evidenceType=evidence.getEntityType().toString();
       String evidenceId=evidence.getOid().toString();
       this.actionStr="Added Evidence/Exons To Transcript "+transcriptAcc+" Using Evidence type"+evidenceType +" with id= "+evidenceId;
       return(super.getCommandLogMessage());


    }
}
