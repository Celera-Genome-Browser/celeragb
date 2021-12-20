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
 * Title:        Genome Browser Client Description:  This project is for JBuilder 4.0
 * @author       Deepali Bhandari
 * @version $Id$
 */
import api.entity_model.management.GenomicEntityFactory;
import api.entity_model.management.ModelMgr;
import api.entity_model.management.PropertyMgr;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.CuratedExon;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.annotation.SuperFeature;
import api.entity_model.model.fundtype.AlignableGenomicEntity;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.MutableAlignment;
import api.entity_model.model.fundtype.SingleAlignmentSingleAxis;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
/**
 * The DoCreateNewCurationAlignmentCommand is a Command which (time of writing)
 * handles all "drag from non-curation tiers to Workspace tier
 * Primary subject of the command is; a Sub or a Super Feature (e.g. HSP or BlastHit)'s GEOMETRIC alignment
 * Splice sites and Codons are not subject of the command Preconditions of the command are; alignments for precompute features
 * exist from the datasources Inverse command class is; DoDeleteCurationAlignmentCommand
 * Postconditions & side effects include; Workspace will have new CuratedTranscript with evidence as the subject of the command
 * Assumption : All features are assumed to have Geometric alignments only and have one and
 * only one geometric alignment to a particular axis Algorithm; NOT UNDO:
 * Use the alignment(evidence) to get the underlying entity The entity should have entity type in set of precompute types
 * Construct a new transcript using the factory Use the evidence alignment to addEvidence(s) on the new transcript
 * Set properties for the new transcript using the PropertyManager
 * Add alignments to the Genomic Axis for each of the new exons Set properties for the new exons using the PropertyManager
 * Finally Add alignment for the transcript to the axis
 */
public class DoCreateNewCurationAndAlign extends FeatureStructureBoundedCommand {
   private static boolean DEBUG_CLASS = false;
   private String cmdName = "Create New Curation And Align";
   private Alignment newEvidence;
   private Alignment resultingAlignment;

   /** Constructor... */
   public DoCreateNewCurationAndAlign(Alignment evidence) {
      super(evidence.getAxis());
      this.newEvidence = evidence;
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
      Feature alignedFeature = (Feature)this.resultingAlignment.getEntity();
      rootFeatureSet.add(alignedFeature.getRootFeature());
      return rootFeatureSet;
    }


    /**
     * Execute the command with out returning the undo.
     * The undo will be created for us by the GeneBoundaryCommand super-class.
     */
    public void executeWithNoUndo() throws Exception {
      resultingAlignment = doCreateNewCurationAlignment(newEvidence);
      setFocusEntity(resultingAlignment.getEntity());
      this.timeofCommandExecution=new Date().toString();
    }

    protected OID getUndoFocusOID() {
      return newEvidence.getEntity().getOid();
    }

    protected OID getRedoFocusOID() {
      return resultingAlignment.getEntity().getOid();
    }

   /** The actual internal execution.... */
   public Alignment doCreateNewCurationAlignment(Alignment evidence) {
      AlignableGenomicEntity evidenceEntity = evidence.getEntity();
      // Get a entity interval factory from the ModelMgr...
      GenomicEntityFactory geFactory = (ModelMgr.getModelMgr()).getEntityFactory();
      // Create the transcript...
      // Note the discoveryEnvironment is hardcoded, should be actually obtained
      // from the view
      GenomeVersion gv = evidence.getAxis().getGenomeVersion();
      CuratedTranscript transcript = (CuratedTranscript)geFactory.create(
          OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.SCRATCH_NAMESPACE, gv.hashCode()), "Transcript", // displayname
          EntityType.getEntityTypeForValue(EntityTypeConstants.NonPublic_Transcript), // entityType
          "Curation" // String discoveryEnvironment.
          );
      // create alignment for new transcript
      CuratedTranscript.CuratedTranscriptMutator transcriptMutator = null;
      transcriptMutator = mutatorAcceptor.getCuratedTranscriptMutatorFor(transcript);
      transcriptMutator.setUnderConstruction(true);
      MutableAlignment transcriptma = new MutableAlignment(evidence.getAxis(),
          transcript, ((GeometricAlignment)evidence).getRangeOnAxis());
      try {
         ((CuratedTranscript.CuratedTranscriptMutator) transcriptMutator).addAlignmentToAxis(transcriptma);
      }
      catch (Exception ex1) { ModelMgr.getModelMgr().handleException(ex1); }
      // if super/composite evidence entity... create  the evidence sub-features...
      if (!((Feature)evidenceEntity).isSimple() &&
          (((Feature)evidenceEntity).getSubFeatureCount()!=0/*evidenceEntity instanceof SuperFeature && ((SuperFeature)evidenceEntity).getSubFeatures() != null)*/)) {
         SuperFeature super_feature = (SuperFeature)evidenceEntity;
         Feature subFeature;
         SingleAlignmentSingleAxis sasaSubFeature;
         Collection sub_features = super_feature.getSubFeatures();
         Object[] sub_feature_array = sub_features.toArray();
         for (int i = 0; i < sub_feature_array.length; i++) {
            subFeature = (Feature)sub_feature_array[i];
            if (subFeature != null) {
               if (!(subFeature instanceof SingleAlignmentSingleAxis)) {
                  // todo: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s).
                  System.out.println("DoCreateNewCurationAndAlignCommand: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s)");
               }
               else {
                  sasaSubFeature = (SingleAlignmentSingleAxis)subFeature;
                  Alignment sub_feature_alignment = sasaSubFeature.getOnlyGeometricAlignmentToOnlyAxis();
                  createExonFromEvidenceAndAddToTranscript(transcriptMutator, sub_feature_alignment);
               }
            }
         }
      }
      // else leaf evidence -- create a composite with single leaf (currently
      // curations are always composite)
      else {
         Collection c = evidenceEntity.getAlignmentsToAxis(evidence.getAxis());
         Alignment sub_feature_alignment = (Alignment)((c.iterator()).next());
         createExonFromEvidenceAndAddToTranscript(transcriptMutator, sub_feature_alignment);
      }
      /**
       * @todo:
       */
      // Make sure the new transcript is "new".
      // transcript.setScratchModifiedState(FeaturePI.SCRATCH_MOD_STATE_NEW);
      // take care of the properties for new transcript
      PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.NEW_ENTITY, transcript, false);
      ((CuratedTranscript.CuratedTranscriptMutator) transcriptMutator).updatePropertiesBasedOnGeometricAlignment(transcriptma);
      Collection transcriptProps = transcript.getProperties();
      Iterator transcriptPropsIter = transcriptProps.iterator();
      while (transcriptPropsIter.hasNext()) {
         GenomicProperty transcriptProp = PropertyMgr.getPropertyMgr().handleProperty((GenomicProperty)transcriptPropsIter.next(),
             PropertyMgr.NEW_ENTITY, transcript);
         try {
            if (transcriptProp != null && !transcriptProp.getName().equals(TranscriptFacade.GENE_ACCESSION_PROP)) {
               transcriptMutator.setProperty(transcriptProp.getName(), transcriptProp.getInitialValue());
            }
         }
         catch (Exception e) { ModelMgr.getModelMgr().handleException(e); }
      }
      //Now set the properties for all the exons
      Collection exonfeatures = transcript.getSubFeatures();
      Object[] exonfeaturearray = exonfeatures.toArray();
      for (int j = 0; j < exonfeaturearray.length; j++) {
         CuratedExon exonfeature = (CuratedExon)exonfeaturearray[j];
         PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.NEW_ENTITY, exonfeature, false);
         CuratedExon.CuratedExonMutator curatedExonMutator = null;
         curatedExonMutator = mutatorAcceptor.getCuratedExonMutatorFor(exonfeature);
         if (exonfeature != null) {
            Collection exonProps = exonfeature.getProperties();
            Iterator exonPropsIter = exonProps.iterator();
            while (exonPropsIter.hasNext()) {
               GenomicProperty exonProp = PropertyMgr.getPropertyMgr().handleProperty((GenomicProperty)exonPropsIter.next(),
                   PropertyMgr.NEW_ENTITY, exonfeature);
               try {
                  if (exonProp != null) {
                     curatedExonMutator.setProperty(exonProp.getName(), exonProp.getInitialValue());
                  }
                  Alignment exona = ((SingleAlignmentSingleAxis)exonfeature).getOnlyAlignmentToOnlyAxis();
                  curatedExonMutator.updatePropertiesBasedOnGeometricAlignment((GeometricAlignment)exona);

               }
               catch (Exception e) { ModelMgr.getModelMgr().handleException(e); }
            }
         }
      }
      ((CuratedTranscript.CuratedTranscriptMutator) transcriptMutator).updatePropertiesBasedOnGeometricAlignment(transcriptma);
      transcriptMutator.setFeatureStructureUnderConstruction(false);
      return transcriptma;
   }


   private void createExonFromEvidenceAndAddToTranscript(
                      CuratedTranscript.CuratedTranscriptMutator transcriptMutator,
                      Alignment evidence) {
      CuratedExon exon = null;
      CuratedExon.CuratedExonMutator curatedExonMutator = null;
      Collection involvedExons = transcriptMutator.addEvidenceAndCreateExonIfNecessary((GeometricAlignment)evidence);
      if (involvedExons == null) return;

      for (Iterator itr = involvedExons.iterator(); itr.hasNext(); ) {
        exon = (CuratedExon)itr.next();
        // set the range/alignment properties for exon
        curatedExonMutator = mutatorAcceptor.getCuratedExonMutatorFor(exon);
        curatedExonMutator.updatePropertiesBasedOnGeometricAlignment(exon.getOnlyGeometricAlignmentToOnlyAxis());
      }
   }


   /** toString() will return the name of the command. */
   public String toString() {
      return cmdName;
   }



   /** This returns the Log message with the time stamp expalaning which entities
    * underewent change, of what kind
    *
    */
   public String getCommandLogMessage() {

     String transcriptAcc=(resultingAlignment.getEntity().getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP)).getInitialValue();
     String evidenceType=newEvidence.getEntity().getEntityType().toString();
     String evidenceId=newEvidence.getEntity().getOid().toString();
     this.actionStr="Create new Transcript "+transcriptAcc+" from "+evidenceType +" evidence id= "+evidenceId;
     return(super.getCommandLogMessage());
   }


}
