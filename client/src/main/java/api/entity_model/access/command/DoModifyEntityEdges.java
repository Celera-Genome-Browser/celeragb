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


import api.entity_model.management.CommandPreconditionException;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.CuratedExon;
import api.entity_model.model.annotation.CuratedFeature;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.annotation.Feature;
import api.stub.data.OID;
import api.stub.geometry.Range;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 *
 * The DoModifyProxyEdgesCommand is a CommandToken used to change the edges of
 * a Curated Exon Feature and therefore changing the Alignment of its Parent as well
 * Maintain the current orientation if the exon already has an alignment.
 *
 * Arguments of the command are;
 *   exon = the curated exon feature to modify.
 *   start = the new start value.
 *   end = the new end value.
 * Is Undoable?  YES
 * Inverse command class is; DoModifyProxyEdgesCommand
 * Postconditions & side effects include;
 *   Removes all current alignments between the genomicAxis and the exon
 *   leaving only the new alignment.
 *
 * Assumptions;
 * Even though the model allows multiple alignments for any Entity/ GenomicAxis
 * pair, we are assuming that there will be only one alignment and that too it is Mutable
 *
 * @author       Deepali Bhandari
 * @version $Id$
 */

public class DoModifyEntityEdges extends FeatureStructureBoundedCommand {
    static final boolean DEBUG_CLASS = false;
  //  static final boolean USE_GAPPED_DATA = false;
  //  static final boolean USE_MIRRORED = false;
    public static final String COMMAND_NAME = "Change Entity Edges";
    private CuratedExon exon;
    private int start;
    private int end;
    private Range newRange;
    private GeometricAlignment originalAlignment;
    private Feature rootFeature;
  //  private EdgeMemento edgeMemento = null;
  //  private String prevScratchModState = null;
  //  private String codonPrevScratchModState = null;

    /**
     * Constructor that takes the CuratedExon,
     * the new start and end.
     * The new start and end should be specified relative to GenomicAxis.
     * Constuctor used when acting as the original command, and NOT as an Undo...
     */
    public DoModifyEntityEdges(CuratedExon exon, int start, int end) {
      super(exon.getOnlyGeometricAlignmentToOnlyAxis().getAxis());
      this.originalAlignment = exon.getOnlyGeometricAlignmentToOnlyAxis();
      if (originalAlignment == null) {
        ModelMgr.getModelMgr().handleException(new IllegalArgumentException(
          "Can not modify an entity edge if the entity has no geometric alignment."));
      }
      this.exon = exon;
      this.start = start;
      this.end = end;
      // make a new range with same orientation...
      int newMin = (int)Math.min(start, end);
      int newMax = (int)Math.max(start, end);
      if (originalAlignment.getRangeOnAxis().isReversed()) newRange = new Range(newMax, newMin);
      else newRange = new Range(newMin, newMax);
      this.rootFeature = exon.getRootFeature();
    }


    /**
     * Constuctor used when acting as an Undo...
     * Constructor that takes an EdgeMemento.
     */
    /*
    public DoModifyEntityEdges(EdgeMemento edgeMemento, boolean isUndo,
                                      String prevScratchModState, String codonPrevScratchModState) {
        this.gAxisProxy = edgeMemento.getGenomicAxisProxy();
        if (this.gAxisProxy == null) {
          System.err.println("EdgeMemento Constructor: Can not construct a DoModifyProxyEdges command without a GenomicAxisPI.");
        }
        this.exonProxy = edgeMemento.getExonProxy();
        this.start = edgeMemento.getStart();
        this.end = edgeMemento.getEnd();
        this.setIsActingAsUndoAndUndoName(isUndo, this.toString());
        this.prevScratchModState = prevScratchModState;
        this.codonPrevScratchModState=codonPrevScratchModState;
    }
    */


    /**
     * Invoked BEFORE the command is executed.
     * @returns the set of pre-existing root features that will be affected.
     */
    public HashSet getCommandSourceRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      rootFeatureSet.add(this.rootFeature);
      return rootFeatureSet;
    }


    /**
     * Invoked AFTER the command is executed.
     * @returns the set of root features that display changes "after" the command has executed.
     */
    public HashSet getCommandResultsRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      rootFeatureSet.add(this.rootFeature);
      return rootFeatureSet;
    }

    /**
     * Checks the for valid preconditions, if the precoditions are not met,
     * throws a CommandPreconditionException.
     *
     * Preconditions include;
     * - If the entity is an Exon, make sure the new range does not overlap
     * an Exon of the same Transcript.
     * - Make sure the new range does not invalidate the start codon.
     * - Make sure the new range does not change the orientation.
     */
    public void validatePreconditions() throws CommandPreconditionException {
      CommandPreconditionException exceptionToThrow;

      // Get the transcript...
      CuratedTranscript transcript = (CuratedTranscript)exon.getSuperFeature();
      if (transcript == null) {
        exceptionToThrow = new CommandPreconditionException(this, "Exon does not have a Transcript");
        throw exceptionToThrow;
      }

      /* This is not an error and should be handled silently.
         Particularly when considering that this one command can be part of a composite
         command with multiple edge settings (this precondition would then
         invalidate the whole command just because this one edge is already equal!)
      // Check to make sure the new range is NOT the same as the old range...
      if (originalAlignment.getRangeOnAxis().equals(newRange)) {
        exceptionToThrow = new CommandPreconditionException(this, "New range is identical to old range.");
        throw exceptionToThrow;
      }
      */

      // Check for Naked Start Codon...
      if (transcript.exonRangeChangeWillLeaveNakedStartCodon(exon, newRange)) {
        exceptionToThrow = new CommandPreconditionException(this, "The specified adjustment would cause an invalid start codon.");
        throw exceptionToThrow;
      }

      // Check to make sure the new exon range does not overlap multiple exons...
      Collection oldExonsInNewRange = transcript.getSubFeaturesInRangeOnAxis(newRange);
      // Remove the exon that is changing... don't care if it is in the new range
      // note: the exon (in old alignment) does not have to be in the new range.
      oldExonsInNewRange = new ArrayList(oldExonsInNewRange);
      oldExonsInNewRange.remove(exon);
      // If there are any left... error...
      if (!oldExonsInNewRange.isEmpty()) {
        exceptionToThrow = new CommandPreconditionException(this, "The specified adjustment would cause an exon overlap.");
        throw exceptionToThrow;
      }
    }


    /**
     * Execute the command with out returning the undo.
     * The undo will be created for us by the GeneBoundaryCommand super-class.
     */
    public void executeWithNoUndo() throws Exception {
        doModifyEntityEdges();
        setFocusEntity(null);
        this.timeofCommandExecution=new Date().toString();
    }

    protected OID getUndoFocusOID() {
      return exon.getSuperFeature().getOid();
    }

    protected OID getRedoFocusOID() {
      return exon.getSuperFeature().getOid();
    }

    /**
     * Do the command action and return an EdgeMemento that can be used to
     * construct the inverse command.
     */
    private /*EdgeMemento*/ void doModifyEntityEdges() {
       // StopCodonFeatPI stopCodon = null;
        CuratedTranscript parentTranscript = (CuratedTranscript)exon.getSuperFeature();

        // If debugging class, print out some precondition diagnostics that may help...
        if (DEBUG_CLASS) {
          System.out.println("Preconditions of DoModifyProxyEdgesCommand...");
          System.out.println("Exon (" + exon.getOid() + ")'s range on GenomicAxis ("
                              + ") to be changed to [start:" + start
                              + " end: " + end + "]");
        }

        // Check to make sure the exonProxy is editable...

        // Get the current range so as to maintain the orientation.
       Range oldAlignmentRange = originalAlignment.getRangeOnAxis();
       // make a new range with same orientation...
        Range newRange;
        int newMin = (int)Math.min(start, end);
        int newMax = (int)Math.max(start, end);
        if (oldAlignmentRange.isReversed()) newRange = new Range(newMax, newMin);
        else newRange = new Range(newMin, newMax);

        if (DEBUG_CLASS) {
          System.out.println("New Range = " + newRange);
        }

        try {
          Feature.FeatureMutator exonMutator = mutatorAcceptor.getFeatureMutatorFor(exon);
          exonMutator.changeRangeOnAlignment(exon.getOnlyGeometricAlignmentToOnlyAxis(),newRange);
          exonMutator.updatePropertiesBasedOnGeometricAlignment(exon.getOnlyGeometricAlignmentToOnlyAxis());
          if (parentTranscript != null) {
            Feature.FeatureMutator transMutator = mutatorAcceptor.getFeatureMutatorFor(parentTranscript);
            transMutator.updateAllSuperFeatureGeomAlignmentsBasedOnSubFeatures();
          }
        }
        catch (Exception ex) {
          ModelMgr.getModelMgr().handleException(ex);
        }

        // If we already have a stop codon, proactively obsolete it...
        // ...we're doing this because the adjustTranslationStop() shouldn't
        // know about obsoletion...
        // @todo: Will REVISIT in 3.1.
       /*
        if ((parentTranscript != null) && parentTranscript.isReplacement()) {
          stopCodon = parentTranscript.getStopCodonProxy();
          // Only obsolete the stopCodon if it is a Replacement, otherwise
          // the codon will be modified by parentTranscript.adjustTranslationStop(gAxisPI)
          if ((stopCodon != null) && stopCodon.isReplacement()) this.obsoleteCodonFeatPI(gAxisPI, stopCodon, parentTranscript);
        }

        // Update the alignements from the root down
        Feature rootFeature = exon.getRootFeature();
        if (rootFeature != null) {
         rootFeature.getMutator(this, "acceptFeatureMutator");
         boolean alignmentsChanged=featureMutator.updateAllGeomAlignmentsFromRootFeatureBasedOnSubFeatures();

        }
        // Update  properties based on new alignments for exon, transcript and gene...
         updatePropertiesBasedOnAlignment(exon);
         updatePropertiesBasedOnAlignment(parentTranscript);
         if(parentTranscript.getSuperFeature()!=null){
           updatePropertiesBasedOnAlignment((CuratedGene)parentTranscript.getSuperFeature());
         }
        */


        // Make sure we set the exon's scratch modified state...
        // If we are undo-ing... we set to the previous.
       // if (this.isActingAsUndo()) exonProxy.setScratchModifiedState(this.prevScratchModState);
        // Else we set that it's been modified, and let the state machine manage
        // the transition.
     //   else exonProxy.setScratchWasModified();


        //get the transcript and adjust the stop
        /*
        if (parentTranscript != null) {
            // Adjust the Start / Stop codons...
            if (parentTranscript.adjustTranslationStop(gAxisPI)) {
                stopCodon = parentTranscript.getStopCodonProxy();
                if (this.isActingAsUndo()){
                  stopCodon.setScratchModifiedState(codonPrevScratchModState);
                }
                else stopCodon.setScratchWasModified();
            }
        }
        */

        // If debugging class, print out some precondition diagnostics that may help...
        if (DEBUG_CLASS) {
          /*
          System.out.println("POST-conditions of DoModifyProxyEdgesCommand...");
          System.out.println("Exon (" + exonProxy + ")'s range on GenomicAxis (" + gAxisPI
                              + ") to be changed to [start:" + start
                              + " end: " + end + "]");
          System.out.println("Exons is in Scratch state ("
                              + exonProxy.getScratchModifiedState() + ")");
          if (parentTranscript != null) parentTranscript.printDetails();
          */
         }

        // return an EdgeMemento for the construtor of the inverse.
   //     return new EdgeMemento(gAxisPI, exonProxy, oldRange.getStart(), oldRange.getEnd());
    }


     private void updatePropertiesBasedOnAlignment(CuratedFeature feature){
       Feature.FeatureMutator featureMutator = mutatorAcceptor.getFeatureMutatorFor(feature);
       featureMutator.updatePropertiesBasedOnGeometricAlignment(feature.getOnlyGeometricAlignmentToOnlyAxis());
     }



   /**
    * ATOMIC Obsolete a feature...
    * ... no recursion up or down the parent / child tree.
    * ... no consistency.
    * @todo: REVISIT This method should be migrated to FeaturePI... in 3.1
    */
   /*
   private void obsoleteCodonFeatPI(GenomicAxisPI gAxisPI, CodonFeatPI codonToObsolete, CuratedTranscriptFeatPI curTrans) {
     if ((codonToObsolete == null) || (curTrans == null)) return;

     if (DEBUG_CLASS) System.out.println("Obsoleting "
                        + codonToObsolete.getClass().getName()
                        + " OID = " + codonToObsolete.getOID());
     // Record original state... will need to support undo...
     // recordFirstScratchModifiedState(featureToObsolete);

     // Set was Obsoleted...
     codonToObsolete.setScratchWasObsoleted();

     // Set the obsoletedByScratch for all the PI's it replaces...
     codonToObsolete.updateObsoletedByScratchOnPromotedPIsAndPost(gAxisPI);

     // Set the Parents relationship to the obsoleted feature...
     if (curTrans != null) {
       // recordFirstScratchModifiedState(curTrans);
       if (codonToObsolete instanceof StartCodonFeatPI) {
         curTrans.addToObsoletedStartCodons((StartCodonFeatPI)codonToObsolete);
         // curTrans.setObsoletingStopCodon(curTrans.getStopCodonProxy());
       }
       else if (codonToObsolete instanceof StopCodonFeatPI) {
         curTrans.addToObsoletedStopCodons((StopCodonFeatPI)codonToObsolete);
       }
       curTrans.setScratchWasModified();
     }

     // Fire of a notification...
     gAxisPI.postNewDetails(0,codonToObsolete);
   }
   */

    /**
     * Provide the name of the command...
     */
    public String toString() {
        return COMMAND_NAME;
    }


    /** This returns the Log message with the time stamp expalaning which entities
    * underwent change, of what kind
    *
    */
   public String getCommandLogMessage() {

     String exonId=exon.getOid().toString();
     Range exonrng=((GeometricAlignment)exon.getOnlyAlignmentToOnlyAxis()).getRangeOnAxis();
     this.actionStr="Modified Edges of Exon id= "+exonId+" To Range= "+exonrng.getStart() +", "+exonrng.getEnd() ;
     return(super.getCommandLogMessage());
   }
}
