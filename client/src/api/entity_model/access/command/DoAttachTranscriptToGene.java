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
import api.entity_model.model.annotation.CuratedGene;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.fundtype.SingleAlignmentSingleAxis;
import api.facade.abstract_facade.annotations.GeneFacade;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.stub.data.OID;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

public class DoAttachTranscriptToGene extends FeatureStructureBoundedCommand {
   public static final String COMMAND_NAME = "Attach Transcript To Gene";
   private CuratedGene gene = null;
   //  private CuratedTranscript transcript= null;
   private ArrayList transcripts = new ArrayList();
   private CuratedGene.CuratedGeneMutator geneMutator = null;
   private CuratedTranscript.CuratedTranscriptMutator transcriptMutator = null;

   public DoAttachTranscriptToGene(Axis anAxis, CuratedGene gene, ArrayList transcripts) {
      super(anAxis);
      this.gene = gene;
      //   this.transcript = transcript;
      this.transcripts = transcripts;

   }

   /**
    * Invoked BEFORE the command is executed.
    * @returns the set of pre-existing root features that will be affected.
    */
   public HashSet getCommandSourceRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      rootFeatureSet.add(gene.getRootFeature());
      for (Iterator iter = transcripts.iterator(); iter.hasNext();) {
         CuratedTranscript t = (CuratedTranscript) iter.next();
         rootFeatureSet.add(t.getRootFeature());
      }
      return rootFeatureSet;
   }

   /**
    * Invoked AFTER the command is executed.
    * @returns the set of root features that display changes "after" the command has executed.
    */
   public HashSet getCommandResultsRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      rootFeatureSet.add(gene.getRootFeature());
      return rootFeatureSet;
   }

   /**
    * Checks the for valid preconditions, if the precoditions are not met,
    * throws a CommandPreconditionException.
    *
    * Preconditions include;
    * - If the transcript has Gene then, then first detach the transcript
    * If Gene is null
    * If Transcript is null
    */
   public void validatePreconditions() throws CommandPreconditionException {
      CommandPreconditionException exceptionToThrow;

      if (gene == null) {
         exceptionToThrow = new CommandPreconditionException(this, "No Gene found to attach to");
         throw exceptionToThrow;
      }
      for (Iterator iter = transcripts.iterator(); iter.hasNext();) {
         CuratedTranscript t = (CuratedTranscript) iter.next();
         if (t == null) {
            exceptionToThrow = new CommandPreconditionException(this, "Select transcript");
            throw exceptionToThrow;
         }

         if (t.getSuperFeature() != null) {
            exceptionToThrow = new CommandPreconditionException(this, "Transcript already has a gene");
            throw exceptionToThrow;
         }

         if (!(gene.getOnlyGeometricAlignmentToOnlyAxis().getOrientationOnAxis().equals(t.getOnlyAlignmentToOnlyAxis().getOrientationOnAxis()))) {
            exceptionToThrow = new CommandPreconditionException(this, "Transcript and Gene Orientation donot match");
            throw exceptionToThrow;
         }
      }

   }

   /**
    * Execute the command with out returning the undo.
    * The undo will be created for us by the GeneBoundaryCommand super-class.
    */
   public void executeWithNoUndo() throws Exception {
      gene.getMutator(this, "acceptCuratedGeneMutator");
      for (Iterator iter = transcripts.iterator(); iter.hasNext();) {
         CuratedTranscript t = (CuratedTranscript) iter.next();
         geneMutator.addSubFeature(t);
         // update the properties for Gene
         gene.getMutator(this, "acceptCuratedGeneMutator");
         geneMutator.updatePropertiesBasedOnGeometricAlignment(((SingleAlignmentSingleAxis) gene).getOnlyGeometricAlignmentToOnlyAxis());
         try {
            PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.UPDATE_ENTITY, gene, false);
         }
         catch (Exception e) {
            ModelMgr.getModelMgr().handleException(e);
         }

         // update the properties for transcript wrt to new parent
         t.getMutator(this, "acceptCuratedTranscriptMutator");
         transcriptMutator.updatePropertiesBasedOnGeometricAlignment(((SingleAlignmentSingleAxis) t).getOnlyGeometricAlignmentToOnlyAxis());
         try {
            PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.UPDATE_ENTITY, t, true);
         }
         catch (Exception e) {
            ModelMgr.getModelMgr().handleException(e);
         }

      }

      setFocusEntity(gene);
      this.timeofCommandExecution = new Date().toString();
   }

   protected OID getUndoFocusOID() {
      return ((CuratedTranscript) transcripts.get(0)).getOid();
   }

   protected OID getRedoFocusOID() {
      return gene.getOid();
   }

   public void acceptCuratedTranscriptMutator(GenomicEntity.GenomicEntityMutator ctmutator) {
      if (ctmutator instanceof CuratedTranscript.CuratedTranscriptMutator) {
         this.transcriptMutator = (CuratedTranscript.CuratedTranscriptMutator) ctmutator;
      }
   }

   public void acceptCuratedGeneMutator(GenomicEntity.GenomicEntityMutator cgmutator) {
      if (cgmutator instanceof CuratedGene.CuratedGeneMutator) {
         this.geneMutator = (CuratedGene.CuratedGeneMutator) cgmutator;
      }
   }

   public String toString() {
      return COMMAND_NAME;
   }

   /** This returns the Log message with the time stamp expalaning which entities
   * underewent change, of what kind
   *
   */
   public String getCommandLogMessage() {

      String geneAcc = (gene.getProperty(GeneFacade.GENE_ACCESSION_PROP)).getInitialValue();
      String transcriptAcc = "";
      for (Iterator iter = transcripts.iterator(); iter.hasNext();) {
         CuratedTranscript t = (CuratedTranscript) iter.next();
         transcriptAcc = transcriptAcc + (t.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP)).getInitialValue() + " ";
      }
      // String transcriptAcc="";/*(transcript.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP)).getInitialValue();*/
      this.actionStr = "Attach Transcript " + transcriptAcc + " To Gene " + geneAcc;
      return (super.getCommandLogMessage());
   }

}
