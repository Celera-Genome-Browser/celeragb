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

import api.entity_model.management.CommandExecutionException;
import api.entity_model.management.CommandPreconditionException;
import api.entity_model.management.GenomicEntityFactory;
import api.entity_model.management.ModelMgr;
import api.entity_model.management.PropertyMgr;
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
import api.facade.abstract_facade.annotations.ExonFacade;
import api.facade.abstract_facade.annotations.GeneFacade;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.stub.data.AccessionGenerator;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;
import api.stub.data.ReplacementRelationship;
import api.stub.data.Util;
import api.stub.geometry.Range;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;


public class DoMergeTranscripts extends FeatureStructureBoundedCommand {
    private static boolean DEBUG_CLASS = false;
    private static int MERGE_CASE_UNKNOWN = 0;
    private static int MERGE_CASE_2NEW = 1;
    private static int MERGE_CASE_1NEW1PROMOTED = 2;
    private static int MERGE_CASE_2PROMOTED = 3;
    public final static String COMMAND_NAME = "Merge Transcripts";
    private CuratedTranscript transcript1;
    private CuratedTranscript transcript2;
    private Feature preExecuteRootFeat = null;
    private Feature postExectuteRootFeat = null;
    private CuratedTranscript resultTranscript=null;
    private HashMap subFeatureCreatedByHashMap =new HashMap();
    private HashMap subFeatureCreatedDateHashMap =new HashMap();

    /**
     * Constructor...
     */
    public DoMergeTranscripts(Axis anAxis, CuratedTranscript transcript1, CuratedTranscript transcript2) {
      super(anAxis);
      this.transcript1 = transcript1;
      this.transcript2 = transcript2;
      this.preExecuteRootFeat = transcript1.getRootFeature();
    }


    /**
     * Invoked BEFORE the command is executed.
     * @returns the set of pre-existing root features that will be affected.
     */
    public HashSet getCommandSourceRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      rootFeatureSet.add(this.preExecuteRootFeat);
      return rootFeatureSet;
    }


    /**
     * Invoked AFTER the command is executed.
     * @returns the set of root features that display changes "after" the command has executed.
     */
    public HashSet getCommandResultsRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      rootFeatureSet.add(this.postExectuteRootFeat);
      return rootFeatureSet;
    }


    /**
     * Execute the command with out returning the undo.
     * The undo will be created for us by the GeneBoundaryCommand super-class.
     */
    public void executeWithNoUndo() throws Exception {
        doMergeCurations(transcript1, transcript2);
        setFocusEntity(resultTranscript);
        this.timeofCommandExecution=new Date().toString();
        return;
    }

    protected OID getUndoFocusOID() {
      return transcript1.getOid();
    }

    protected OID getRedoFocusOID() {
      return resultTranscript.getOid();
    }

    /**
     * We need a good way for commands to determine if the preconditions are met for
     * the command, and for views to check.
     * @todo: REVISIT This is a quick hack.  The commands should NOT be putting up
     * UI resources such as JOptionPane(s).  Instead we need to move the commands
     * so they have a formal validatePreconditions() and validatePostconditions().
     */
    public boolean validatePreconditions(StringBuffer returnMessage) {
      if (DEBUG_CLASS) System.out.println("DoMergeCurationsCommand: Checking for valid predonditions...");

      // Check for different Genes...
      if (DEBUG_CLASS) System.out.println("Check transcripts from different Genes...");
      CuratedGene gene1 = (CuratedGene)transcript1.getSuperFeature();
      CuratedGene gene2 = (CuratedGene)transcript2.getSuperFeature();
      if ((gene1 != null) && (gene2 != null) && (gene1 != gene2)) {
          //can't merge two transcripts from different genes
          returnMessage.append("Can not merge Transcripts from different Genes.");
          return false;
      }

      int origExonCount1 = transcript1.getSubFeatureCount();

      // check overlaps -- merging curation that have overlapping exons is not currently allowed
      if (DEBUG_CLASS) System.out.println("Check exons from both transcripts for overlaps...");
      // brute force pairwise comparison for now...
      Range[] ranges = new Range[origExonCount1];
      CuratedExon exon;
      Range alignedRange = null;
      Range range1 = null, range2 = null, totalRange = null;
      boolean exonsDoCollide = false;
      // First cache all the ranges of trans1 exons... (just saves a little time).
      int i = 0;
      for (Iterator iter1=(transcript1.getSubFeatures()).iterator(); iter1.hasNext();) {
            exon = (CuratedExon)iter1.next();
            alignedRange = exon.getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis();
            ranges[i] = null;
            if (alignedRange != null) {
              ranges[i++] = alignedRange;
              // Keep track of totalRange...
              if (totalRange == null) totalRange = alignedRange;
              else totalRange = Range.union(totalRange, alignedRange);
            }
      }
      for (Iterator iter2 = transcript2.getSubFeatures().iterator(); iter2.hasNext(); ) {
            exon = (CuratedExon)iter2.next();
            alignedRange =exon.getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis();
            if (alignedRange != null) {
              range2 = alignedRange;
              // Keep track of totalRange...
              if (totalRange == null) totalRange = range2;
              else totalRange = Range.union(totalRange, range2);
              for (int j = 0; j < ranges.length; j++) {
                range1 = ranges[j];
                if (range2.intersects(range1)) {
                    exonsDoCollide = true;
                    break;
                }
              }
            }
      }
      if (exonsDoCollide) {
          //warning can't merge curations with different parents
          // @todo: REVISIT This is a quick hack.  The commands should NOT be putting up
          // UI resources such as JOptionPane(s).  Instead we need to move the commands
          // so they have a formal validatePreconditions() and validatePostconditions().
          returnMessage.append("Can not merge Transcripts with overlapping Exons.");
          return false;
      }
      returnMessage.append("Merge preconditions are met.");
      return true;
    }



      public void validatePreconditions() throws CommandPreconditionException {

           CommandPreconditionException exceptionToThrow;


           // Make sure they have the same parent...
           CuratedGene gene1 = (CuratedGene)transcript1.getSuperFeature();
           CuratedGene gene2 = (CuratedGene)transcript2.getSuperFeature();
           if(gene1==null||gene2==null){
              exceptionToThrow = new CommandPreconditionException(this, "Transcripts must be parented to be merged");
              throw exceptionToThrow;

           }
           if ((gene1 != null) && (gene2 != null) && (gene1 != gene2)) {
              exceptionToThrow = new CommandPreconditionException(this, "Transcripts must have the same parent Gene in order to be merged.");
              throw exceptionToThrow;
           }

          // Make sure the source transcripts are in Scratch Modified States that allow us to Merge...
           if (!transcript1.isScratch() || !transcript2.isScratch()) {
              exceptionToThrow = new CommandPreconditionException(this, "Both Transcripts should be in Workspace for Merge");
              throw exceptionToThrow;

            }


            // ... can't Merge something that has been split...
            if (transcript1.isReplacementType(ReplacementRelationship.SPLIT)  ||
                transcript2.isReplacementType(ReplacementRelationship.SPLIT)) {
                exceptionToThrow = new CommandPreconditionException(this, "Cant merge transcripts that are in split state already");
                throw exceptionToThrow;
            }


            int origExonCount1 = transcript1.getSubFeatureCount();

            // check overlaps -- merging curation that have overlapping exons is not currently allowed

           // brute force pairwise comparison for now...
            Range[] ranges = new Range[origExonCount1];
            CuratedExon exon;
            Range alignedRange = null;
            Range range1 = null, range2 = null, totalRange = null;
            boolean exonsDoCollide = false;
           //First cache all the ranges of trans1 exons... (just saves a little time).
           int i = 0;
           for (Iterator iter1=(transcript1.getSubFeatures()).iterator(); iter1.hasNext();) {
             exon = (CuratedExon)iter1.next();
             alignedRange = exon.getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis();
             ranges[i] = null;
             if (alignedRange != null) {
              ranges[i++] = alignedRange;
              // Keep track of totalRange...
              if (totalRange == null) totalRange = alignedRange;
              else totalRange = Range.union(totalRange, alignedRange);
            }
           }
           for (Iterator iter2 = transcript2.getSubFeatures().iterator(); iter2.hasNext(); ) {
            exon = (CuratedExon)iter2.next();
            alignedRange =exon.getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis();
            if (alignedRange != null) {
              range2 = alignedRange;
              // Keep track of totalRange...
              if (totalRange == null) totalRange = range2;
              else totalRange = Range.union(totalRange, range2);
              for (int j = 0; j < ranges.length; j++) {
                range1 = ranges[j];
                if (range2.intersects(range1)) {
                    exonsDoCollide = true;
                    break;
                }
              }
            }
      }
      if (exonsDoCollide) {
         exceptionToThrow = new CommandPreconditionException(this, "Can not merge Transcripts with overlapping Exons.");
         throw exceptionToThrow;
      }


    }

    /**
     * WARNING: currently using same exons for new transcript
     */
    private GenomicEntity doMergeCurations(CuratedTranscript trans1, CuratedTranscript trans2)
                                                throws CommandExecutionException {
        boolean debugMethod = false;
        Hashtable exonToRepRel = new Hashtable();
        Axis genomicAxis = trans1.getOnlyAlignmentToOnlyAxis().getAxis();
        // deselect (to handle other views that were showing old curation)
        // Notice: transcript2 is the curation which you use right mouse click to select
        // and transcript1 is the curation you use right mouse click to invoke merge action and
        // are going to merge with transcript2,
        // Check the genes...
        Workspace workspace = trans1.getGenomeVersion().getWorkspace();
        CuratedGene gene1 = (CuratedGene)trans1.getSuperFeature();
        CuratedGene gene2 = (CuratedGene)trans2.getSuperFeature();
        CuratedGene gene = null;
        if ((gene1 != null) && (gene2 != null) && (gene1 != gene2)) {
          //can't merge two transcripts with different parents

          ModelMgr.getModelMgr().handleException(new IllegalArgumentException(
                  "Transcripts can not belong to different Genes when Merging."));
          return null;
        }
        if (gene1 != null) gene = gene1;
        else gene = gene2;

        // Check what merge case we have...
        boolean trans1IsNew = trans1.isReplacementType(ReplacementRelationship.NEW);
        boolean trans2IsNew = trans2.isReplacementType(ReplacementRelationship.NEW);
        int mergeCase = DoMergeTranscripts.MERGE_CASE_UNKNOWN;
        if (trans1IsNew && trans2IsNew) {
          // Both are new
          mergeCase = DoMergeTranscripts.MERGE_CASE_2NEW;
        }
        else if (trans1IsNew || trans2IsNew) {
          // One is new
          mergeCase = DoMergeTranscripts.MERGE_CASE_1NEW1PROMOTED;
        }
        else {
          // Bot are non-new...
          mergeCase = DoMergeTranscripts.MERGE_CASE_2PROMOTED;
        }

        CuratedTranscript.CuratedTranscriptMutator transcriptToKeepMutator, transcript1Mutator, transcript2Mutator;
        transcript1Mutator = mutatorAcceptor.getCuratedTranscriptMutatorFor(transcript1);
        transcript2Mutator = mutatorAcceptor.getCuratedTranscriptMutatorFor(transcript2);

        // check orientation -- can only merge curations that are in same orientation
        // NOT YET IMPLEMENTED

        int origExonCount1 = trans1.getSubFeatureCount();

        // If debugging class, print out some precondition statistics that may help...
        if (DEBUG_CLASS) {
          System.out.println("DoMergeTranscripts - PREconditions of merge...");
          System.out.println("Transcript#1 is " + trans1.getOid());
          System.out.println("Transcript#2 is " + trans2.getOid());
          this.getWorkspace().printChangeTraces();
        }

        // check overlaps -- merging curation that have overlapping exons is not currently allowed
        if (DEBUG_CLASS) System.out.println("Check exons from both transcripts for overlaps...");

        Range[] ranges = new Range[origExonCount1];
        CuratedExon exon;
        Range alignedRange = null;
        Range range1 = null, range2 = null;
        Range totalRange1 = null, totalRange2 = null, totalTotalRange = null;
        boolean exonsDoCollide = false;
        // First cache all the ranges of trans1 exons... (just saves a little time).
        int i = 0;
        for (Iterator iter1 = transcript1.getSubFeatures().iterator(); iter1.hasNext();) {
            exon = (CuratedExon)iter1.next();
            alignedRange = exon.getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis();
            ranges[i] = null;
            if (alignedRange != null) {
              ranges[i] = alignedRange;
              // Keep track of totalRange...
              if (totalRange1 == null) totalRange1 = alignedRange;
              else totalRange1 = Range.union(totalRange1, alignedRange);
              i++;
            }
        }
       for (Iterator iter2 = transcript2.getSubFeatures().iterator(); iter2.hasNext(); ) {
            exon = (CuratedExon)iter2.next();
            alignedRange =exon.getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis();
            if (alignedRange != null) {
              range2 = alignedRange;
              // Keep track of totalRange...
              if (totalRange2 == null) totalRange2 = range2;
              else totalRange2 = Range.union(totalRange2, range2);
              for (int j = 0; j < ranges.length; j++) {
                range1 = ranges[j];
                if (range2.intersects(range1)) {
                    exonsDoCollide = true;
                    break;
                }
              }
            }
      }
      if (exonsDoCollide) {
          //warning can't merge curations with different parents
          // @todo: REVISIT This is a quick hack.  The commands should NOT be putting up
          // UI resources such as JOptionPane(s).  Instead we need to move the commands
          // so they have a formal validatePreconditions() and validatePostconditions().
         // returnMessage.append("Can not merge Transcripts with overlapping Exons.");
         throw new CommandExecutionException(this, "Can NOT Merge Transcripts where Exons overlap.");
      }
      totalTotalRange = Range.union(totalRange1, totalRange2);

        // Check to see if the original transcripts were reversed...
        // ... if original and our total have different orientation... reverse it...'s


        // Decide which Transcript to keep and use as the destination and which
        // Transcript to delete...
        // start with trans1 as the one to keep...
        if (DEBUG_CLASS) System.out.println("Create a new transcript for the merge result...");
        GenomeVersion gv=trans1.getOnlyGeometricAlignmentToOnlyAxis().getAxis().getGenomeVersion();
        CuratedTranscript transcriptToKeep = (CuratedTranscript)ModelMgr.getModelMgr().getEntityFactory().create(
            OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.SCRATCH_NAMESPACE,gv.hashCode()),
           "Transcript",                                     // displayname
            EntityType.getEntityTypeForValue(EntityTypeConstants.NonPublic_Transcript),  // entityType
            "Curation"                                       // String discoveryEnvironment.
            );
        transcriptToKeepMutator = mutatorAcceptor.getCuratedTranscriptMutatorFor(transcriptToKeep);

        // add the new transcript alignments to the axis
        MutableAlignment transcriptToKeepma=new MutableAlignment(transcript1.getOnlyAlignmentToOnlyAxis().getAxis(), transcriptToKeep, totalTotalRange);
        try {
          transcriptToKeepMutator.addAlignmentToAxis(transcriptToKeepma);
        }
        catch (Exception ex1) {
          ModelMgr.getModelMgr().handleException(ex1);
        }



        ReplacementRelationship oldExonRepRel;
        CuratedExon oldExon, newExon;
        CuratedExon.CuratedExonMutator oldExonMutator, newExonMutator;
        MutableAlignment oldExonAlign, newExonAlign;
        GenomicEntityFactory geFactory = (ModelMgr.getModelMgr()).getEntityFactory();

        // Move the exons from the transcriptToDelete to the transcriptToKeep...
        if (DEBUG_CLASS) System.out.println("Moving exons from Trans#1 to merged transcript...");
        Object[] transcript1Array=transcript1.getSubFeatures().toArray();
        for (int k=0; k < transcript1Array.length; k++) {
          // System.out.println("Trans1 moving Exon #" + k);
          // Get the old exon and it's replacement relationship.
          oldExon = (CuratedExon)transcript1Array[k];
          oldExonRepRel = oldExon.getReplacementRelationship();
          Collection oldExonEvidences=oldExon.getEvidenceOids();
          oldExonAlign = (MutableAlignment)oldExon.getOnlyGeometricAlignmentToOnlyAxis();
          oldExonMutator = mutatorAcceptor.getCuratedExonMutatorFor(oldExon);

          // Create the new exon..
          newExon= (CuratedExon)geFactory.create(
                        OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.SCRATCH_NAMESPACE,gv.hashCode()),
                        oldExon.getDisplayName(),      // displayname
                        oldExon.getEntityType(),       // entityType
                        oldExon.getEnvironment(),      // String discoveryEnvironment.,
                        "subclassification",                    // String subClassification,
                        transcriptToKeep,         // GenomicEntity parent
                        oldExon.getDisplayPriority()
                        );
          newExonMutator = mutatorAcceptor.getCuratedExonMutatorFor(newExon);

          // Create a new alignment...
          newExonAlign=new MutableAlignment(genomicAxis, newExon, oldExonAlign.getRangeOnAxis());

         // save of the assigend by and assigned dates for the old exon
         // the new exon needs to have  them, if the transcript and gene
         // company accessions are retained.
          if(oldExon.isScratchReplacingPromoted()){
            subFeatureCreatedByHashMap.put(newExon.getOid(), oldExon.getProperty(ExonFacade.CREATED_BY_PROP).getInitialValue() );
            subFeatureCreatedDateHashMap.put(newExon.getOid(), oldExon.getProperty(ExonFacade.DATE_CREATED_PROP).getInitialValue() );
          }

          // Remove the alignment from the old exon...
          oldExonMutator.removeAlignmentToAxis(oldExonAlign);
          // Add the alignment to the new exon...
          try {
            newExonMutator.addAlignmentToAxis(newExonAlign);
            newExonMutator.addAllEvidenceOids(oldExonEvidences);
          }
          catch (Exception ex1) {
            ModelMgr.getModelMgr().handleException(ex1);
          }

          // newExonMutator.setReplacementRelationship(oldExonRepRel);
          exonToRepRel.put(newExon,oldExonRepRel);
        }

        // Move the exons from the transcriptToDelete to the transcriptToKeep...
        if (DEBUG_CLASS) System.out.println("Moving exons from Trans#2 to merged transcript...");
        Object[] transcript2Array=transcript2.getSubFeatures().toArray();
        for (int l=0; l < transcript2Array.length; l++) {
          // Get the old exon and it's replacement relationship.
          // System.out.println("Trans2 moving Exon #" + l);
          oldExon = (CuratedExon)transcript2Array[l];
          oldExonRepRel = oldExon.getReplacementRelationship();
          Collection oldExonEvidences=oldExon.getEvidenceOids();
          oldExonAlign = (MutableAlignment)oldExon.getOnlyGeometricAlignmentToOnlyAxis();
          oldExonMutator = mutatorAcceptor.getCuratedExonMutatorFor(oldExon);

          // Create the new exon..
          newExon= (CuratedExon)geFactory.create(
                        OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.SCRATCH_NAMESPACE,gv.hashCode()),
                        oldExon.getDisplayName(),      // displayname
                        oldExon.getEntityType(),       // entityType
                        oldExon.getEnvironment(),      // String discoveryEnvironment.,
                        "subclassification",                    // String subClassification,
                        transcriptToKeep,         // GenomicEntity parent
                        oldExon.getDisplayPriority()
                        );
          newExonMutator = mutatorAcceptor.getCuratedExonMutatorFor(newExon);

          // Create a new alignment...
          newExonAlign=new MutableAlignment(genomicAxis, newExon, oldExonAlign.getRangeOnAxis());

         // save of the assigend by and assigned dates for the old exon
         // the new exon needs to have  them, if the transcript and gene
         // company accessions are retained.
          if(oldExon.isScratchReplacingPromoted()){
            subFeatureCreatedByHashMap.put(newExon.getOid(), oldExon.getProperty(ExonFacade.CREATED_BY_PROP).getInitialValue() );
            subFeatureCreatedDateHashMap.put(newExon.getOid(), oldExon.getProperty(ExonFacade.DATE_CREATED_PROP).getInitialValue() );
          }

          // Remove the alignment from the old exon...
          oldExonMutator.removeAlignmentToAxis(oldExonAlign);
          // Add the alignment to the new exon...
          try {
            newExonMutator.addAlignmentToAxis(newExonAlign);
            newExonMutator.addAllEvidenceOids(oldExonEvidences);
          }
          catch (Exception ex1) {
            ModelMgr.getModelMgr().handleException(ex1);
          }
          // newExonMutator.setReplacementRelationship(oldExonRepRel);
          exonToRepRel.put(newExon,oldExonRepRel);
        }

        // Adjust the gene...
        //Trying to get attached curated gene
        if (gene != null) {
          try {
            CuratedGene.CuratedGeneMutator geneMutator;
            geneMutator = mutatorAcceptor.getCuratedGeneMutatorFor(gene);
            // attach curated to new transcript
            geneMutator.addSubFeature(transcriptToKeep);
            // detach curated gene from original transcripts
            geneMutator.removeSubFeature(transcript1);
            geneMutator.removeSubFeature(transcript2);
            // update the gene's properties...
            updatePropertiesForFeature(gene);

          }
          catch(Exception ex) {
            try {
              ModelMgr.getModelMgr().handleException(ex);
            }
            catch(Exception ex1) { ex.printStackTrace(); }
          }
        }

         // Create and set properties for the merged transccript
         updatePropertiesForFeature(transcriptToKeep);
        // Remove the transcriptToDelete from it's Gene...
        // ... also detach it from the GenomicAxis...
        if (DEBUG_CLASS) System.out.println("Removing doner transcript from Gene and GenomicAxis...");
        try {
            createSetPropertiesForFeature(transcriptToKeep);

            // for the resultant transcript the accession should be the
            // same as one of the promoted derived transcripts if only one of
            // the merging transcripts is derived from promtoed
            if(transcript1.isScratchReplacingPromoted() && !transcript2.isScratchReplacingPromoted()){
               transcriptToKeepMutator.setProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP,transcript1.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP).getInitialValue());
               transcriptToKeepMutator.setProperty(TranscriptFacade.CREATED_BY_PROP,transcript1.getProperty(TranscriptFacade.CREATED_BY_PROP).getInitialValue());
               transcriptToKeepMutator.setProperty(TranscriptFacade.DATE_CREATED_PROP,transcript1.getProperty(TranscriptFacade.DATE_CREATED_PROP).getInitialValue());


            }else if(transcript2.isScratchReplacingPromoted() && !transcript1.isScratchReplacingPromoted()){
               transcriptToKeepMutator.setProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP,transcript2.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP).getInitialValue());
               transcriptToKeepMutator.setProperty(TranscriptFacade.CREATED_BY_PROP,transcript2.getProperty(TranscriptFacade.CREATED_BY_PROP).getInitialValue());
               transcriptToKeepMutator.setProperty(TranscriptFacade.DATE_CREATED_PROP,transcript2.getProperty(TranscriptFacade.DATE_CREATED_PROP).getInitialValue());

            }



          Iterator exonIter=transcriptToKeep.getSubFeatures().iterator();
          CuratedExon anExon;
          CuratedExon.CuratedExonMutator exonMutator;
          while(exonIter.hasNext()){
            anExon = (CuratedExon)exonIter.next();
            createSetPropertiesForFeature(anExon);
            exonMutator = mutatorAcceptor.getCuratedExonMutatorFor(anExon);
            exonMutator.setReplacementRelationship((ReplacementRelationship)exonToRepRel.get(anExon));

          }

            // If one transcript is new and one non - new then the exons coming from
            // non new transcript need to have same assigned by and date
            // as their replaced entities
            if((transcript1.isScratchReplacingPromoted() && !transcript2.isScratchReplacingPromoted())
               ||(transcript2.isScratchReplacingPromoted() && !transcript1.isScratchReplacingPromoted())){

               for(Iterator iter =transcriptToKeep.getSubFeatures().iterator();iter.hasNext();){
                  CuratedFeature c=(CuratedFeature)iter.next();
                  if(c instanceof CuratedExon  &&  c.isScratchReplacingPromoted()){
                    CuratedExon.CuratedExonMutator exonmutator=mutatorAcceptor.getCuratedExonMutatorFor((CuratedExon)c);
                    exonmutator.setProperty
                    (ExonFacade.CREATED_BY_PROP, (String)subFeatureCreatedByHashMap.get(c.getOid()));
                    exonmutator.setProperty(ExonFacade.DATE_CREATED_PROP, (String)subFeatureCreatedDateHashMap.get(c.getOid()));
                  }

               }
               subFeatureCreatedByHashMap=null;
               subFeatureCreatedDateHashMap=null;
            }


            // making the accession of the gene as WG, because after a merge
            // gene is considered new
            // <JTS> Only do this if both Transcripts were non-New...
            if(mergeCase != DoMergeTranscripts.MERGE_CASE_1NEW1PROMOTED) {
              String geneacc=AccessionGenerator.getAccessionGenerator().generateAccessionString("WG");
              CuratedGene.CuratedGeneMutator geneMutator;
              geneMutator = mutatorAcceptor.getCuratedGeneMutatorFor(gene);
              geneMutator.setProperty(GeneFacade.GENE_ACCESSION_PROP,geneacc);
              geneMutator.setProperty(GeneFacade.CREATED_BY_PROP,System.getProperties().getProperty("user.name"));
              geneMutator.setProperty(GeneFacade.DATE_CREATED_PROP,Util.getDateTimeStringNow());
            }



        }
        catch(Exception ex) {
            try {
                ModelMgr.getModelMgr().getModelMgr().handleException(ex);
            }
            catch(Exception ex1) {
                ex.printStackTrace();
            }
        }





        // transcriptToKeep should have the aggregate "replaces" relationships as the transcriptToDelete...
        if (DEBUG_CLASS) System.out.println("Move all replaces relationships from trans1 & trans2 to transcriptToKeep...");
        transcriptToKeepMutator.recordIsResultOfMerged(transcript1, transcript2);
        // Need to save off the root feature.
        postExectuteRootFeat = transcriptToKeep.getRootFeature();

        // Delete the old transcript sub-structure
        transcript1Mutator.removeSubStructureAndRemoveAlignments(true, workspace);
        transcript2Mutator.removeSubStructureAndRemoveAlignments(true, workspace);

        // If debugging class, print out some postcondition statistics that may help...
        if (DEBUG_CLASS || debugMethod) {
          System.out.println("DoMergeTranscripts - POSTconditions of merge...");
          System.out.println("Resulting Transcript is " + transcriptToKeep.getOid());
          this.getWorkspace().printChangeTraces();
        }
        resultTranscript=transcriptToKeep;
        return resultTranscript;
    }





     public void updatePropertiesForFeature(Feature aFeature){
        Feature.FeatureMutator featureMutator = mutatorAcceptor.getFeatureMutatorFor(aFeature);
        featureMutator.updatePropertiesBasedOnGeometricAlignment(((SingleAlignmentSingleAxis)aFeature).getOnlyGeometricAlignmentToOnlyAxis());
        PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.UPDATE_ENTITY, aFeature, false);
     }


     public void createSetPropertiesForFeature(Feature aFeature){
       Feature.FeatureMutator featureMutator = mutatorAcceptor.getFeatureMutatorFor(aFeature);
       PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.NEW_ENTITY, aFeature, false);
       featureMutator.updatePropertiesBasedOnGeometricAlignment(((SingleAlignmentSingleAxis)aFeature).getOnlyGeometricAlignmentToOnlyAxis());
       PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.NEW_ENTITY, aFeature, false);
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

     String resultTranscriptAcc=resultTranscript.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP).getInitialValue();
     String trscpt1Acc=transcript1.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP).getInitialValue();
     String trscpt2Acc=transcript2.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP).getInitialValue();
     CuratedFeature gene=(CuratedFeature)resultTranscript.getSuperFeature();
     String geneAcc=null;
     geneAcc=gene.getProperty(GeneFacade.GENE_ACCESSION_PROP).getInitialValue();
     this.actionStr="Merged Transcripts "+trscpt1Acc+" ,"+trscpt2Acc+" Into "+resultTranscriptAcc+" With Gene Parent "+geneAcc;
     return(super.getCommandLogMessage());
   }

    public CuratedTranscript getResultTranscript() {
        return resultTranscript;
    }
}
