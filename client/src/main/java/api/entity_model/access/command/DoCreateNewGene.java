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
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.annotations.GeneFacade;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;

import java.util.Date;
import java.util.HashSet;

public class DoCreateNewGene extends FeatureStructureBoundedCommand {
    public static final String COMMAND_NAME = "Create New Gene";
    private CuratedGene newGene = null;
    private CuratedTranscript transcript = null;


    public DoCreateNewGene(Axis anAxis, CuratedTranscript transcript) {
      super(anAxis);
      GenomeVersion gv=transcript.getGenomeVersion();
      newGene = (CuratedGene)ModelMgr.getModelMgr().getEntityFactory().create(
                   OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.SCRATCH_NAMESPACE,gv.hashCode()),"Curation",EntityType.getEntityTypeForValue(EntityTypeConstants.NonPublic_Gene),"Curation");
      this.transcript = transcript;
    }


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
      rootFeatureSet.add(this.newGene);
      return rootFeatureSet;
    }


      /**
     * Checks the for valid preconditions, if the precoditions are not met,
     * throws a CommandPreconditionException.
     *
     * Preconditions include;
     * - If the transcript has no Gene then, there is need to Create New Gene
     */
    public void validatePreconditions() throws CommandPreconditionException {
      CommandPreconditionException exceptionToThrow;

      if (transcript.getSuperFeature()!= null) {
        exceptionToThrow = new CommandPreconditionException(this, "Transcript has a Gene");
        throw exceptionToThrow;
      }
    }



    /**
     * Execute the command with out returning the undo.
     * The undo will be created for us by the GeneBoundaryCommand super-class.
     */
    public void executeWithNoUndo() throws Exception {
      CuratedGene.CuratedGeneMutator curatedGeneMutator = null;
      curatedGeneMutator = mutatorAcceptor.getCuratedGeneMutatorFor(newGene);
      curatedGeneMutator.addSubFeature(transcript);
      // newGene.setScratchModifiedState(FeaturePI.SCRATCH_MOD_STATE_NEW);

      // create and set properties for Gene
      PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.NEW_ENTITY, newGene, false);

      // update the properties for transcripts.  Handle for child exons too.
      PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.UPDATE_ENTITY, transcript, true);
      setFocusEntity(newGene);
      this.timeofCommandExecution=new Date().toString();
    }


    protected OID getUndoFocusOID() {
      return transcript.getOid();
    }

    protected OID getRedoFocusOID() {
      return newGene.getOid();
    }


    public String toString() {
        return COMMAND_NAME;
    }


    /** This returns the Log message with the time stamp expalaning which entities
    * underewent change, of what kind
    *
    */
   public String getCommandLogMessage() {

     String geneAcc=(newGene.getProperty(GeneFacade.GENE_ACCESSION_PROP)).getInitialValue();
     String transcriptAcc=(transcript.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP)).getInitialValue();
     this.actionStr="Create new Gene "+geneAcc+" For Transcript "+transcriptAcc ;
     return(super.getCommandLogMessage());
   }
}