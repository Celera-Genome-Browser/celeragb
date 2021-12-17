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
package api.entity_model.management;

import api.entity_model.model.annotation.*;
import api.entity_model.model.fundtype.GenomicEntity;


/**
 * Command provides an interface for functors that modify the model,
 * allowing undo and redo of actions. See @ModifyManager for additional
 * details.
 *
 * In an interactive application, it is adequate for the UI to focus on the result of the
 * execution of a command. We call it the "focusEntity".
 * The public method getFocusEntity() can be used by the UI, after the execution of
 * a command, to retrieve the entity that should be selected.
 * Each command, in its execute() method, is responsible to set the focus entity
 * by calling setFocusEntity(entity).
 */
public abstract class Command
{
    private static GenomicEntity focusEntity;

    protected boolean isActingAsUndo = false;
    protected String undoCommandName = null;
    protected MutatorAcceptor mutatorAcceptor = new MutatorAcceptor();
    protected String timeofCommandExecution;
    protected String actionStr;

    /**
     * Set if this command is acting as an undo for some other command.  Pass in the
     * Undo command string, or null.
     */
    public void setIsActingAsUndoAndUndoName(boolean isActingAsUndoFlag, String undoCommandName) {
      this.isActingAsUndo = isActingAsUndoFlag;
      if (!isActingAsUndoFlag) this.undoCommandName = null;
      else this.undoCommandName = undoCommandName;
    }

    /**
     * Modifies the local (in-memory) model, returning a command
     * capable of undoing the resultant change. The returned command may
     * be NULL, but must NOT be a reference to this command
     */
    public abstract Command execute() throws Exception;

    /**
     * Set the entity that the UI should focus on after the execution of this command.
     */
    protected void setFocusEntity(GenomicEntity focusEntity) {
        this.focusEntity = focusEntity;
    }

    /**
     * Return the entity that the UI should focus on after the execution of this command.
     */
    public static GenomicEntity getFocusEntity() {
        return focusEntity;
    }


    /**
     * Performs precondition validation, executes, and postcondition validation.
     * The returned command may
     * be NULL, but must NOT be a reference to this command
     */
    public final Command executeWithValidation() throws Exception {
      // Validate the preconditions...
      this.validatePreconditions();
      // Execute the command (without validation)...
      Command returnCommand = this.execute();
      // Validate the postconditions...
      this.validatePostconditions();

      return returnCommand;
    }

    /**
     * Checks the for valid preconditions, if the precoditions are not met,
     * throws a CommandPreconditionException.
     */
    public void validatePreconditions() throws CommandPreconditionException {
    }

    /**
     * Checks the for valid preconditions, if the precoditions are not met,
     * throws a CommandPreconditionException.
     */
    public void validatePostconditions() throws CommandPostconditionException {
    }


    /**
     * @return the boolean if this command is acting as an undo for some previously
     * executed command.  This affect which string to use to represent the command
     * as well as how the command should transition the Scratch Modified states
     * of the PIs affected by the command (either call transition methods or simply
     * goto previous states for undo).
     */
    public boolean isActingAsUndo() {  return isActingAsUndo;  }

    /**
     * Return the undo command string...
     */
    public String getUndoCommandName() {  return this.undoCommandName;  }

    /**
     * useful for printing command log
     * sub classes give the real implementation
     */
    public /*abstract*/ String getCommandLogMessage(){

      return (this.toString()+"--"+timeofCommandExecution+"--"+actionStr+"\n");
    }




    /**
     * Inner class that is used to observer the Features...
     */
    public class MutatorAcceptor {
      private Feature.FeatureMutator featMutator = null;
      private CuratedFeature.CuratedFeatureMutator curatedFeatMutator = null;
      private CuratedGene.CuratedGeneMutator geneMutator = null;
      private CuratedTranscript.CuratedTranscriptMutator transcriptMutator = null;
      private CuratedExon.CuratedExonMutator exonMutator = null;
      private CuratedCodon.CuratedCodonMutator codonMutator = null;


      /**
       * Protected constructor for the mutator acceptor class...
       */
      protected MutatorAcceptor() {}


      public Feature.FeatureMutator getFeatureMutatorFor(Feature aFeature) {
        if (aFeature == null) return null;
        aFeature.getMutator(this,"acceptFeatureMutator");
        return featMutator;
      }


      public CuratedFeature.CuratedFeatureMutator getCuratedFeatureMutatorFor(CuratedFeature aCuratedFeature) {
        if (aCuratedFeature == null) return null;
        aCuratedFeature.getMutator(this,"acceptCuratedFeatureMutator");
        return curatedFeatMutator;
      }


      public CuratedGene.CuratedGeneMutator getCuratedGeneMutatorFor(CuratedGene aCuratedGene) {
        if (aCuratedGene == null) return null;
        aCuratedGene.getMutator(this,"acceptCuratedGeneMutator");
        return geneMutator;
      }


      public CuratedTranscript.CuratedTranscriptMutator getCuratedTranscriptMutatorFor(CuratedTranscript aCuratedTranscript) {
        if (aCuratedTranscript == null) return null;
        aCuratedTranscript.getMutator(this,"acceptCuratedTranscriptMutator");
        return transcriptMutator;
      }


      public CuratedExon.CuratedExonMutator getCuratedExonMutatorFor(CuratedExon aCuratedExon) {
        if (aCuratedExon == null) return null;
        aCuratedExon.getMutator(this,"acceptCuratedExonMutator");
        return exonMutator;
      }


      public CuratedCodon.CuratedCodonMutator getCuratedCodonMutatorFor(CuratedCodon aCuratedCodon) {
        if (aCuratedCodon == null) return null;
        aCuratedCodon.getMutator(this,"acceptCuratedCodonMutator");
        return codonMutator;
      }


      /**
       * Accept a FeatureMutator.
       */
      public void acceptFeatureMutator(GenomicEntity.GenomicEntityMutator mutator){
        this.featMutator = null;
        if(mutator instanceof Feature.FeatureMutator){
          this.featMutator=(Feature.FeatureMutator)mutator;
        }
      }


      /**
       * Accept a CuratedFeatureMutator.
       */
      public void acceptCuratedFeatureMutator(GenomicEntity.GenomicEntityMutator mutator){
        this.curatedFeatMutator = null;
        if (mutator instanceof CuratedFeature.CuratedFeatureMutator){
          this.curatedFeatMutator=(CuratedFeature.CuratedFeatureMutator)mutator;
        }
      }


      /**
       * Accept a CuratedTranscriptMutator.
       */
      public void acceptCuratedGeneMutator(GenomicEntity.GenomicEntityMutator mutator){
        this.geneMutator = null;
        if (mutator instanceof CuratedGene.CuratedGeneMutator){
          this.geneMutator = (CuratedGene.CuratedGeneMutator)mutator;
        }
      }


      /**
       * Accept a CuratedTranscriptMutator.
       */
      public void acceptCuratedTranscriptMutator(GenomicEntity.GenomicEntityMutator mutator){
        this.transcriptMutator = null;
        if (mutator instanceof CuratedTranscript.CuratedTranscriptMutator){
          this.transcriptMutator=(CuratedTranscript.CuratedTranscriptMutator)mutator;
        }
      }


      /**
       * Accept a CuratedExonMutator.
       */
      public void acceptCuratedExonMutator(GenomicEntity.GenomicEntityMutator mutator){
        this.exonMutator = null;
        if (mutator instanceof CuratedExon.CuratedExonMutator){
          this.exonMutator = (CuratedExon.CuratedExonMutator)mutator;
        }
      }


      /**
       * Accept a CuratedExonMutator.
       */
      public void acceptCuratedCodonMutator(GenomicEntity.GenomicEntityMutator mutator){
        this.codonMutator = null;
        if (mutator instanceof CuratedCodon.CuratedCodonMutator){
          this.codonMutator = (CuratedCodon.CuratedCodonMutator)mutator;
        }
      }

    }  // End MutatorAcceptor inner class.

  }  // End Command Class.

