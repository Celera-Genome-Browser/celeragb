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
import api.entity_model.management.CommandException;
import api.entity_model.management.CommandPreconditionException;
import api.entity_model.management.ModelMgr;
import api.entity_model.management.PropertyMgr;
import api.entity_model.model.annotation.CuratedGene;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.SingleAlignmentSingleAxis;
import api.facade.abstract_facade.annotations.GeneFacade;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.stub.data.OID;

import java.util.Date;
import java.util.HashSet;


public class DoDetachTranscriptFromGene extends FeatureStructureBoundedCommand {
    public static final String COMMAND_NAME = "Detach Transcript From Gene";
    private CuratedGene gene = null;
    private CuratedTranscript transcript = null;
    private Feature postCommandSuperFeature=null;

    /**
     * Detach the transcript
     */
    public DoDetachTranscriptFromGene(Axis anAxis, CuratedTranscript transcript) {
      super(anAxis);
      this.gene=(CuratedGene)transcript.getSuperFeature();
      this.transcript = transcript;
    }


    /**
     * Invoked BEFORE the command is executed.
     * @returns the set of pre-existing root features that will be affected.
     */
    public HashSet getCommandSourceRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      rootFeatureSet.add(this.gene);
      return rootFeatureSet;
    }


    /**
     * Invoked AFTER the command is executed.
     * @returns the set of root features that display changes "after" the command has executed.
     */
    public HashSet getCommandResultsRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      if (postCommandSuperFeature==null) {
        rootFeatureSet.add(this.transcript);
      }
      else {
        rootFeatureSet.add(postCommandSuperFeature);
        rootFeatureSet.add(this.transcript);
      }
      return rootFeatureSet;
    }

     /**
     * Checks the for valid preconditions, if the precoditions are not met,
     * throws a CommandPreconditionException.
     *
     * Preconditions include;
     * - If the transcript has no Gene then, there is nothing to detach from
     */
    public void validatePreconditions() throws CommandPreconditionException {
      CommandPreconditionException exceptionToThrow;

      if (gene == null) {
        exceptionToThrow = new CommandPreconditionException(this, "Transcript doesnot have Gene");
        throw exceptionToThrow;
      }

    }


  public void validateWorkFlow() throws CommandException{
     CommandException exceptionToThrow;

      if (gene.getSubFeatureCount() == 1 && gene.isScratchReplacingPromoted()) {
        exceptionToThrow = new CommandException(this, "Last Transcript is being detached, and gene will be obsoleted."+"\n"+ "This is not a recommended Workflow!");
        throw exceptionToThrow;
      }


  }


    /**
     * Execute the command with out returning the undo.
     * The undo will be created for us by the GeneBoundaryCommand super-class.
     */
    public void executeWithNoUndo() throws Exception {
        /**
         * First thing to do is to remove the transcript as a subfeature of the GENE
         * Update the transcripts properties ie it should no longer have gene accession property
         * Assumption here is that if the transcript is removed as subfeature, its alignment is still retained
         */
        if (gene.getSubFeatureCount() == 1) {
          postCommandSuperFeature=null;

          // warning message to tell the user that the
          // promoted gene will be obsoleted
/*
        if (gene.getSubFeatureCount() == 1 && gene.isScratchReplacingPromoted()) {
         throw new CommandWorkFlowException(this, "Last Transcript is being detached, and gene will be obsoleted."+"\n"+ "This is not a recommended Workflow!");
         }
*/
        }
        else {
          postCommandSuperFeature=gene;
        }
        CuratedGene.CuratedGeneMutator geneMutator=null;
        geneMutator = mutatorAcceptor.getCuratedGeneMutatorFor(gene);
        geneMutator.removeSubFeature(transcript);

        // update the properties for Gene
        geneMutator.updatePropertiesBasedOnGeometricAlignment(((SingleAlignmentSingleAxis)gene).getOnlyGeometricAlignmentToOnlyAxis());
        try {
          PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.UPDATE_ENTITY, gene, false);
        }
        catch (Exception e) {
          ModelMgr.getModelMgr().handleException(e);
        }

        // update the properties for transcript wrt to new parent
         CuratedTranscript.CuratedTranscriptMutator transcriptMutator = null;
         transcriptMutator = mutatorAcceptor.getCuratedTranscriptMutatorFor(transcript);
         transcriptMutator.updatePropertiesBasedOnGeometricAlignment(((SingleAlignmentSingleAxis)transcript).getOnlyGeometricAlignmentToOnlyAxis());
         try {
           // It is important to update the transcript and it's children after this command
           // so that all properties are accounted for and up to date.
           PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.UPDATE_ENTITY, transcript, true);
         }
         catch (Exception e) {
           ModelMgr.getModelMgr().handleException(e);
         }
         setFocusEntity(transcript);
         this.timeofCommandExecution=new Date().toString();



    }


    protected OID getUndoFocusOID() {
      return transcript.getOid();
    }

    protected OID getRedoFocusOID() {
      return transcript.getOid();
    }

    public String toString() {
        return COMMAND_NAME;
    }



    /** This returns the Log message with the time stamp expalaning which entities
    * underewent change, of what kind
    *
    */
   public String getCommandLogMessage() {

     String geneAcc=(gene.getProperty(GeneFacade.GENE_ACCESSION_PROP)).getInitialValue();
     String transcriptAcc=(transcript.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP)).getInitialValue();
     this.actionStr="Detach Transcript "+transcriptAcc+" From Gene "+geneAcc ;
     return(super.getCommandLogMessage());
   }



}
