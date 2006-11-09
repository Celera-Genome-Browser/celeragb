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

/**
 * The DoMakeCurationFromScratch is a CommandToken used to make a Curation
 * (in the form of a single Exon Transcript) from only a range on the genomic
 * Axis.
 *

 * Arguments of the command are;
 *   rangeOnAxis = the range on the Genomic Axis.
 * Is Undoable?  YES
 * Inverse / Undo command class is; DoDeleteCurationCommand
 * Postconditions & side effects include;
 *   Should have a "new" Transcript with a "new" Exon.
 *
 */


import api.entity_model.management.GenomicEntityFactory;
import api.entity_model.management.ModelMgr;
import api.entity_model.management.PropertyMgr;
import api.entity_model.model.annotation.CuratedExon;
import api.entity_model.model.annotation.CuratedFeature;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.MutableAlignment;
import api.entity_model.model.fundtype.SingleAlignmentSingleAxis;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;
import api.stub.data.ReplacementRelationship;
import api.stub.geometry.Range;

import java.util.Date;
import java.util.HashSet;

public class DoMakeCurationFromScratch extends FeatureStructureBoundedCommand {
    private static boolean DEBUG_CLASS = false;
    String cmdName = "Create New Curation From Scratch";
    Range rangeOnAxis;
    CuratedTranscript transcript;
    // private CuratedFeature.CuratedFeatureMutator featureMutator = null;
    // private GenomicAxis genomicAxis;
    // private CuratedExon.CuratedExonMutator exonMutator;
    private CuratedTranscript newCuration;

    /**
     * The constructor.
     */
    public DoMakeCurationFromScratch(GenomicAxis ga,Range rangeOnAxis) {
      super(ga);
      // genomicAxis=ga;
      this.rangeOnAxis = rangeOnAxis;
    }


    /**
     * Invoked BEFORE the command is executed.
     * @returns the set of pre-existing root features that will be affected.
     */
    public HashSet getCommandSourceRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      return rootFeatureSet;
    }


    /**
     * Invoked AFTER the command is executed.
     * @returns the set of root features that display changes "after" the command has executed.
     */
    public HashSet getCommandResultsRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      rootFeatureSet.add(newCuration.getRootFeature());
      return rootFeatureSet;
    }


    /**
     * Execute the command with out returning the undo.
     * The undo will be created for us by the GeneBoundaryCommand super-class.
     */
    public void executeWithNoUndo() throws Exception {
        if (DEBUG_CLASS) {
          System.out.println("Executing DoMakeCurationFromScratch");
          System.out.println("  Range On Axis = " + rangeOnAxis);
        }
        newCuration = doMakeNewCuration();
        setFocusEntity(newCuration);
        this.timeofCommandExecution=new Date().toString();
    }


    protected OID getUndoFocusOID() {
      return null;
    }

    protected OID getRedoFocusOID() {
      return newCuration.getOid();
    }

    /**
     * The actual internal execution...
     * - Roll over to new factory.
     */
    private CuratedTranscript doMakeNewCuration() {


        // Get a entity factory from the ModelMgr...

        GenomicEntityFactory geFactory = (ModelMgr.getModelMgr()).getEntityFactory();

        // Construct the new Transcript...
        // Make sure the new transcript in "Scratch".
        GenomeVersion gv = axis.getGenomeVersion();
        transcript = (CuratedTranscript)geFactory.create( OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.SCRATCH_NAMESPACE,gv.hashCode()),
           "Transcript",                                     // displayname
            EntityType.getEntityTypeForValue(EntityTypeConstants.NonPublic_Transcript),  // entityType
            "Curation"                                       // String discoveryEnvironment.
            );


        CuratedFeature.CuratedFeatureMutator transcriptMutator;
        transcriptMutator = mutatorAcceptor.getCuratedFeatureMutatorFor(transcript);

        try {
          transcriptMutator.addAlignmentToAxis(new MutableAlignment(axis, transcript, rangeOnAxis));
        }
        catch (Exception e1) {
          ModelMgr.getModelMgr().handleException(e1);
        }

        // Make sure the new transcript is "new".
       //  transcript.setScratchModifiedState(FeaturePI.SCRATCH_MOD_STATE_NEW);


        // Construct an Exon...
        // Make sure the new transcript in "Scratch".
        CuratedExon exon = (CuratedExon)geFactory.create(OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.SCRATCH_NAMESPACE,gv.hashCode()),
                           "exon",
                            EntityType.getEntityTypeForValue(EntityTypeConstants.Exon),
                            "Curation");



        // add alignment for the exon to the xis
        CuratedExon.CuratedExonMutator exonMutator;
        exonMutator = mutatorAcceptor.getCuratedExonMutatorFor(exon);
        try {
          exonMutator.addAlignmentToAxis(new MutableAlignment(axis, exon, rangeOnAxis));
        }
        catch (Exception e1) {
          ModelMgr.getModelMgr().handleException(e1);
        }
        // Make sure the new exon is "new".
        // exon.setScratchModifiedState(FeaturePI.SCRATCH_MOD_STATE_NEW);

        // Add the exon to the Transcript...
        transcriptMutator = mutatorAcceptor.getCuratedFeatureMutatorFor(transcript);
        try {
          transcriptMutator.addSubFeature(exon);
        }
        catch (Exception e2) {
          ModelMgr.getModelMgr().handleException(e2);
        }

        createSetPropertiesForFeature(transcript);
        transcriptMutator.setReplacementRelationshipType(ReplacementRelationship.NEW);
        createSetPropertiesForFeature(exon);
        exonMutator.setReplacementRelationshipType(ReplacementRelationship.NEW);
        return transcript;
    }


    public CuratedTranscript getCreatedTranscript() {
        return transcript;
    }


     public void createSetPropertiesForFeature(Feature feature){

       PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.NEW_ENTITY, feature, false);
       Feature.FeatureMutator featureMutator;
       featureMutator = mutatorAcceptor.getFeatureMutatorFor(feature);
       featureMutator.updatePropertiesBasedOnGeometricAlignment(((SingleAlignmentSingleAxis)feature).getOnlyGeometricAlignmentToOnlyAxis());
       try{
         PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.NEW_ENTITY, feature, false);
       }
       catch(Exception e){ModelMgr.getModelMgr().handleException(e);}

     }


    /**
     * toString() will return the name of the command.
     */
    public String toString() {
        return cmdName;
    }



   /** This returns the Log message with the time stamp expalaning which entities
    * underewent change, of what kind
    *
    */
   public String getCommandLogMessage() {

     String transcriptAcc=(transcript.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP)).getInitialValue();
     String gaId=axis.getOid().toString();
     this.actionStr="Create new Transcript "+transcriptAcc+" From Scratch "+"on Axis "+gaId +" with Range "+rangeOnAxis.getStart()+", "+rangeOnAxis.getEnd();
     return(super.getCommandLogMessage());
   }

}
