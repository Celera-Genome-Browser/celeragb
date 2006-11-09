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
import api.entity_model.management.GenomicEntityFactory;
import api.entity_model.management.ModelMgr;
import api.entity_model.management.PropertyMgr;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.CuratedExon;
import api.entity_model.model.annotation.CuratedFeature;
import api.entity_model.model.annotation.CuratedGene;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.annotation.Workspace;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.fundtype.MutableAlignment;
import api.entity_model.model.fundtype.SingleAlignmentSingleAxis;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.annotations.GeneFacade;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.stub.data.AccessionGenerator;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;
import api.stub.data.ReplacementRelationship;
import api.stub.data.Util;
import api.stub.geometry.Range;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class DoSplitCurationAt extends FeatureStructureBoundedCommand {
   private static boolean DEBUG_CLASS = false;
   public static final String COMMAND_NAME = "Split Curation";
   private CuratedTranscript transcriptToBeSplit;
   private CuratedTranscript belowSplitTranscript;
   private CuratedTranscript aboveSplitTranscript;
   private int genomicAxisPosition;
   static boolean gapped = false;

   /**
    * Constructor...
    */
   public DoSplitCurationAt(Axis anAxis, CuratedTranscript transcriptToBeSplit, int genomicAxisPosition) {
      super(anAxis);
      this.transcriptToBeSplit = transcriptToBeSplit;
      this.genomicAxisPosition = genomicAxisPosition;
   }

   /**
    * Invoked BEFORE the command is executed.
    * @returns the set of pre-existing root features that will be affected.
    */
   public HashSet getCommandSourceRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      if (transcriptToBeSplit != null) {
         rootFeatureSet.add(transcriptToBeSplit.getRootFeature());
      }
      return rootFeatureSet;
   }

   /**
    * Invoked AFTER the command is executed.
    * @returns the set of root features that display changes "after" the command has executed.
    */
   public HashSet getCommandResultsRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      Feature belowSplitRoot = null;
      Feature aboveSplitRoot = null;
      if (belowSplitTranscript != null) {
         belowSplitRoot = belowSplitTranscript.getRootFeature();
         rootFeatureSet.add(belowSplitRoot);
      }
      if (aboveSplitTranscript != null) {
         aboveSplitRoot = aboveSplitTranscript.getRootFeature();
         if (aboveSplitRoot != belowSplitRoot)
            rootFeatureSet.add(aboveSplitRoot);
      }
      return rootFeatureSet;
   }

   /**
   * Checks the for valid preconditions, if the precoditions are not met,
   * throws a CommandPreconditionException.
   *
   * Preconditions include;
   * - If the transcript has one subFeature no need to split
   */
   public void validatePreconditions() throws CommandPreconditionException {
      CommandPreconditionException exceptionToThrow;

      if (transcriptToBeSplit.getSubFeatureCount() == 1) {
         exceptionToThrow = new CommandPreconditionException(this, "Transcript has only one subfeature");
         throw exceptionToThrow;
      }
   }

   /**
    * Execute the command with out returning the undo.
    * The undo will be created for us by the GeneBoundaryCommand super-class.
    */
   public void executeWithNoUndo() throws Exception {
      doSplitCurationAt(transcriptToBeSplit, genomicAxisPosition);
      setFocusEntity(aboveSplitTranscript);
      this.timeofCommandExecution = new Date().toString();
      return;
   }

   protected OID getUndoFocusOID() {
      return transcriptToBeSplit.getOid();
   }

   protected OID getRedoFocusOID() {
      return aboveSplitTranscript.getOid();
   }

   /**
    *  Assumes exons have been ordered
    * WARNING: currently using same exons for new transcripts
    */

   private GenomicEntity doSplitCurationAt(CuratedTranscript transcriptToBeSplit, int genomicAxisPosition) {
      Hashtable exonToRepRel = new Hashtable();
      CuratedTranscript oldTranscript = transcriptToBeSplit;

      Axis genomicAxis = oldTranscript.getOnlyAlignmentToOnlyAxis().getAxis();
      Workspace workspace = oldTranscript.getGenomeVersion().getWorkspace();
      // Save off the old scratch state... because it's not available once the transcript is detached from the GenomicAxisPI...
      // String oldTrascriptScratchState = oldTranscriptPI.getScratchModifiedState();

      // Check to see if we have enough exons to split...
      int origExonCount = oldTranscript.getSubFeatureCount();

      // If debugging class, print out some precondition statistics that may help...
      if (DEBUG_CLASS) {
         /*
         System.out.println("PREconditions of SPLIT...");
         oldTranscript.printDetails();
         System.out.println("Transcript has range on axis:"
            + genomicAxisPI.findOnlyAlignedRangeFor(oldTranscriptPI).getRangeOnAxis());
         System.out.println("Spliting at:" + genomicAxisPosition);
         */
      }

      // Check to make sure we have enough exons to split...
      if (origExonCount <= 1) {
         System.out.println("DoSplitCurationAtCommand: ERROR - Must have 2 or more exons to split a transcript.");
         //return null;
      }

      // Lets sort them into two collections, before the split and after the split.
      // Iterator orderedExonItr = oldTranscriptPI.getSortedExons(genomicAxisPI, true);  // true = ascending.
      List exonsBelowSplit = new ArrayList();
      List exonsAboveSplit = new ArrayList();
      List exonsBoundingSplit = new ArrayList();
      this.sortEntitiesAroundAxisPosition(genomicAxisPosition, exonsAboveSplit, exonsBelowSplit, exonsBoundingSplit);

      // What if we have one that bounds...

      if (!exonsBoundingSplit.isEmpty()) {
         System.out.println("DoSplitCurationAtCommand ERROR: We have an exon that bounds the split.");
         //  return null;
      }

      // Make sure we have exons on both sides...
      if (exonsBelowSplit.isEmpty() || exonsAboveSplit.isEmpty()) {
         System.out.println("DoSplitCurationAtCommand ERROR: We don't have exons both before and after the split.");
         // return null;
      }

      // Make two new transcripts...
      GenomeVersion gv = transcriptToBeSplit.getOnlyAlignmentToOnlyAxis().getAxis().getGenomeVersion();
         belowSplitTranscript =
            (CuratedTranscript) ModelMgr
               .getModelMgr()
               .getEntityFactory()
               .create(OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.SCRATCH_NAMESPACE, gv.hashCode()), "Transcript", // displayname
      EntityType.getEntityTypeForValue(EntityTypeConstants.NonPublic_Transcript), // entityType
      "Curation" // String discoveryEnvironment.
   );
      CuratedTranscript.CuratedTranscriptMutator belowSplitTransMutator;
      belowSplitTransMutator = mutatorAcceptor.getCuratedTranscriptMutatorFor(belowSplitTranscript);

         aboveSplitTranscript =
            (CuratedTranscript) ModelMgr
               .getModelMgr()
               .getEntityFactory()
               .create(OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.SCRATCH_NAMESPACE, gv.hashCode()), "Transcript", // displayname
      EntityType.getEntityTypeForValue(EntityTypeConstants.NonPublic_Transcript), // entityType
      "Curation" // String discoveryEnvironment.
   );
      CuratedTranscript.CuratedTranscriptMutator aboveSplitTransMutator;
      aboveSplitTransMutator = mutatorAcceptor.getCuratedTranscriptMutatorFor(aboveSplitTranscript);

      // Get the mutator for the old transcript...
      CuratedTranscript.CuratedTranscriptMutator oldTransMutator;
      oldTransMutator = mutatorAcceptor.getCuratedTranscriptMutatorFor(oldTranscript);
      GeometricAlignment oldTransAlign = transcriptToBeSplit.getOnlyGeometricAlignmentToOnlyAxis();

      // add the new transcript alignments to the axis
      MutableAlignment belowTranscriptAlign;
      CuratedExon anExon;
      Range range = null, totalRange = null;
      for (Iterator iter = exonsBelowSplit.iterator(); iter.hasNext();) {
         anExon = (CuratedExon) iter.next();
         range = anExon.getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis();
         if (range != null) {
            // Keep track of totalRange...
            if (totalRange == null)
               totalRange = range;
            else
               totalRange = Range.union(totalRange, range);
         }
      }
      belowTranscriptAlign = new MutableAlignment(oldTransAlign.getAxis(), belowSplitTranscript, totalRange);
      try {
         belowSplitTransMutator.addAlignmentToAxis(belowTranscriptAlign);
      }
      catch (Exception ex1) {
         ModelMgr.getModelMgr().handleException(ex1);
      }

      MutableAlignment aboveTranscriptAlign;
      range = null;
      totalRange = null;
      for (Iterator iter = exonsAboveSplit.iterator(); iter.hasNext();) {
         anExon = (CuratedExon) iter.next();
         range = anExon.getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis();
         if (range != null) {
            // Keep track of totalRange...
            if (totalRange == null)
               totalRange = range;
            else
               totalRange = Range.union(totalRange, range);
         }
      }
      aboveTranscriptAlign = new MutableAlignment(oldTransAlign.getAxis(), aboveSplitTranscript, totalRange);
      try {
         aboveSplitTransMutator.addAlignmentToAxis(aboveTranscriptAlign);
      }
      catch (Exception ex2) {
         ModelMgr.getModelMgr().handleException(ex2);
      }

      // Move the exons...
      Object[] belowExonArray = exonsBelowSplit.toArray();
      Feature.FeatureMutator oldExonMutator;
      CuratedFeature.CuratedFeatureMutator newExonMutator;
      for (int i = 0; i < belowExonArray.length; i++) {
         try {
            CuratedExon oldExon = (CuratedExon) belowExonArray[i];
            Collection oldExonEvidences = oldExon.getEvidenceOids();
            ReplacementRelationship oldExonRepRel = oldExon.getReplacementRelationship();
            GenomicEntityFactory geFactory = (ModelMgr.getModelMgr()).getEntityFactory();
               CuratedExon newExon =
                  (
                     CuratedExon) geFactory
                        .create(OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.SCRATCH_NAMESPACE, gv.hashCode()), oldExon.getDisplayName(),
            // displayname
      oldExon.getEntityType(), // entityType
      oldExon.getEnvironment(), // String discoveryEnvironment.,
      "subclassification", // String subClassification,
      belowSplitTranscript, // GenomicEntity parent
   oldExon.getDisplayPriority());
            MutableAlignment newExonma = new MutableAlignment(genomicAxis, newExon, oldExon.getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis());

            oldExonMutator = mutatorAcceptor.getFeatureMutatorFor(oldExon);
            oldExonMutator.removeAlignmentToAxis(oldExon.getOnlyGeometricAlignmentToOnlyAxis());

            newExonMutator = mutatorAcceptor.getCuratedFeatureMutatorFor(newExon);
            newExonMutator.addAlignmentToAxis(newExonma);
            newExonMutator.addAllEvidenceOids(oldExonEvidences);

            exonToRepRel.put(newExon, oldExonRepRel);
         }
         catch (Exception e) {
            ModelMgr.getModelMgr().handleException(e);
         }
      }

      Object[] aboveExonArray = exonsAboveSplit.toArray();
      for (int j = 0; j < aboveExonArray.length; j++) {
         try {

            CuratedExon oldExon = (CuratedExon) aboveExonArray[j];
            Collection oldExonEvidences = oldExon.getEvidenceOids();
            ReplacementRelationship oldExonRepRel = oldExon.getReplacementRelationship();
            GenomicEntityFactory geFactory = (ModelMgr.getModelMgr()).getEntityFactory();
               CuratedExon newExon =
                  (
                     CuratedExon) geFactory
                        .create(OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.SCRATCH_NAMESPACE, gv.hashCode()), oldExon.getDisplayName(),
            // displayname
      oldExon.getEntityType(), // entityType
      oldExon.getEnvironment(), // String discoveryEnvironment.,
      "subclassification", // String subClassification,
      aboveSplitTranscript, // GenomicEntity parent
   oldExon.getDisplayPriority());
            MutableAlignment newExonma = new MutableAlignment(genomicAxis, newExon, oldExon.getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis());

            oldExonMutator = mutatorAcceptor.getFeatureMutatorFor(oldExon);
            oldExonMutator.removeAlignmentToAxis(oldExon.getOnlyGeometricAlignmentToOnlyAxis());

            newExonMutator = mutatorAcceptor.getCuratedFeatureMutatorFor(newExon);
            newExonMutator.addAlignmentToAxis(newExonma);
            newExonMutator.addAllEvidenceOids(oldExonEvidences);

            exonToRepRel.put(newExon, oldExonRepRel);
         }
         catch (Exception e1) {
            ModelMgr.getModelMgr().handleException(e1);
         }
      }

      //Trying to get attached curated gene
      CuratedGene gene = (CuratedGene) oldTranscript.getSuperFeature();
      if (gene != null) {
         try {
            //attach curated to new born transcripts

            Feature.FeatureMutator geneMutator = mutatorAcceptor.getFeatureMutatorFor(gene);
            geneMutator.addSubFeature(belowSplitTranscript);
            geneMutator.addSubFeature(aboveSplitTranscript);

            //detach curated gene from original transcript before call deleteRemote
            geneMutator.removeSubFeature(oldTranscript);
            updatePropertiesForFeature(gene);

         }

         catch (Exception ex) {
            try {
               ModelMgr.getModelMgr().handleException(ex);
            }
            catch (Exception ex1) {
               ex.printStackTrace();
            }
         }
      }

      CuratedExon newExon;
      CuratedExon.CuratedExonMutator exonMutator;
      createSetPropertiesForFeature(belowSplitTranscript);

      Iterator exonIter1 = belowSplitTranscript.getSubFeatures().iterator();
      while (exonIter1.hasNext()) {
         newExon = (CuratedExon) exonIter1.next();
         createSetPropertiesForFeature(newExon);
         exonMutator = mutatorAcceptor.getCuratedExonMutatorFor(newExon);
         exonMutator.setReplacementRelationship((ReplacementRelationship) exonToRepRel.get(newExon));
      }

      createSetPropertiesForFeature(aboveSplitTranscript);

      Iterator exonIter2 = aboveSplitTranscript.getSubFeatures().iterator();
      while (exonIter2.hasNext()) {
         newExon = (CuratedExon) exonIter2.next();
         createSetPropertiesForFeature(newExon);
         exonMutator = mutatorAcceptor.getCuratedExonMutatorFor(newExon);
         exonMutator.setReplacementRelationship((ReplacementRelationship) exonToRepRel.get(newExon));
      }

      try {

         if (gene != null) {
            // setProperty on CuratedGene will setproperty of Gene accession on its transcripts
            // and exons as well
            Feature.FeatureMutator geneMutator = mutatorAcceptor.getFeatureMutatorFor(gene);
            String geneacc = AccessionGenerator.getAccessionGenerator().generateAccessionString("WG");
            geneMutator.setProperty(GeneFacade.GENE_ACCESSION_PROP, geneacc);
            // if accession is changed to WG then assigend by needs to change to annotator
            // and date needs to change to annotation time
            geneMutator.setProperty(GeneFacade.CREATED_BY_PROP, System.getProperties().getProperty("user.name"));
            geneMutator.setProperty(GeneFacade.DATE_CREATED_PROP, Util.getDateTimeStringNow());
         }
      }
      catch (Exception e2) {
         ModelMgr.getModelMgr().handleException(e2);
      }

      // copy over additional properties from old transcript?
      // NOT YET IMPLEMENTED
      // attach exons for new transcripts?
      // shouldn't need to --
      // already done as side effect of CuratedTranscriptFeatPI.addSubFeature()

      // detach the old transcript from the genomic axis... can't detach until after the replaceds are transferred (getAccession...).
      // oldTranscript.getMutator(this, "acceptCuratedTranscriptMutator");
      // New transcript should have the same "replaces" relationships as the old transcript...
      oldTransMutator.recordWasSplitInto(aboveSplitTranscript, belowSplitTranscript);
      oldTransMutator.removeSubStructureAndRemoveAlignments(true, workspace);

      // Return the split memento...
      // Need to return old and both new...
      // return new SplitMemento(oldTranscriptPI, null);

      return aboveSplitTranscript;
   }

   public void createSetPropertiesForFeature(Feature aFeature) {
      PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.NEW_ENTITY, aFeature, false);
      // f.getMutator(this,"acceptFeatureMutator");
      Feature.FeatureMutator featureMutator = mutatorAcceptor.getFeatureMutatorFor(aFeature);
      featureMutator.updatePropertiesBasedOnGeometricAlignment(((SingleAlignmentSingleAxis) aFeature).getOnlyGeometricAlignmentToOnlyAxis());
      try {
         PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.NEW_ENTITY, aFeature, false);
      }
      catch (Exception e) {
         ModelMgr.getModelMgr().handleException(e);
      }
   }

   public void updatePropertiesForFeature(Feature aFeature) {
      // f.getMutator(this,"acceptFeatureMutator");
      Feature.FeatureMutator featureMutator = mutatorAcceptor.getFeatureMutatorFor(aFeature);
      featureMutator.updatePropertiesBasedOnGeometricAlignment(((SingleAlignmentSingleAxis) aFeature).getOnlyGeometricAlignmentToOnlyAxis());
      try {
         PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.UPDATE_ENTITY, aFeature, false);
      }
      catch (Exception e) {
         ModelMgr.getModelMgr().handleException(e);
      }
   }

   /**
    * A utility to sort Entities into either, above, below or bounding a postion
    * on a genomic axis.
    */
   private void sortEntitiesAroundAxisPosition(int genomicAxisPosition, Collection sortedAbove, Collection sortedBelow, Collection sortedBounding) {
      GeometricAlignment alignmentGeo = transcriptToBeSplit.getOnlyGeometricAlignmentToOnlyAxis();
      int transcriptStart = alignmentGeo.getRangeOnAxis().getStart();
      int transcriptEnd = alignmentGeo.getRangeOnAxis().getEnd();
      Feature featureAtPositionOnAxis = transcriptToBeSplit.getSubFeatureAtPositionOnAxis(alignmentGeo.getAxis(), genomicAxisPosition);
      if (featureAtPositionOnAxis == null) {
         sortedAbove.addAll(transcriptToBeSplit.getSubFeaturesInRangeOnAxis(new Range(transcriptStart, genomicAxisPosition)));
         sortedBelow.addAll(transcriptToBeSplit.getSubFeaturesInRangeOnAxis(new Range(genomicAxisPosition, transcriptEnd)));
      }
      else {
         sortedBounding.add(featureAtPositionOnAxis);
      }

   }

   /**
    * toString() will return the name of the command.
    */
   public String toString() {
      return COMMAND_NAME;
   }

   /** This returns the Log message with the time stamp expalaning which entities
     * underewent change, of what kind
     *
     */
   public String getCommandLogMessage() {

      String transcriptAcc = transcriptToBeSplit.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP).getInitialValue();
      String trscptId = transcriptToBeSplit.getOid().toString();
      String belowTrscptAcc = belowSplitTranscript.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP).getInitialValue();
      String aboveTrscptAcc = aboveSplitTranscript.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP).getInitialValue();
      // assuming that the transcripts after split are under the same GENE parent
      CuratedFeature gene = (CuratedFeature) aboveSplitTranscript.getSuperFeature();
      String geneAcc = null;
      if (gene == null) {
         geneAcc = " No Gene Parent";
      }
      else {
         geneAcc = " With Gene Parent " + gene.getProperty(GeneFacade.GENE_ACCESSION_PROP).getInitialValue();
      }
      this.actionStr = "Split Transcript " + transcriptAcc + " id= " + trscptId + " Into " + belowTrscptAcc + ", " + aboveTrscptAcc + geneAcc;
      return (super.getCommandLogMessage());
   }

}
