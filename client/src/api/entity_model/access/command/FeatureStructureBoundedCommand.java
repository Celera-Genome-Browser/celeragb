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


import api.entity_model.management.Command;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.annotation.Workspace;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.genetics.GenomeVersion;
import api.stub.data.OID;

import java.util.HashSet;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 *
 * A type of command that has a natural "Gene" boundary of change.
 * All direct and indirect changes of this command are confined to some list of
 * Genes and thier sub-structure (Transcripts, Exons, Codons, etc)
 *
 * This is the superclass for all commands that change the boundaries or relationships
 * between entities.
 * Undo/redo is implemented once and for all by this class, in its execute() method. Concrete
 * subclasses only have to implement executeWithNoUndo().
 *
 * Commands can be either executed, undo-ed, or redo-ed. In each case, it is important
 * for the UI to know which entity should have the focus as a result of a command activation.
 * To solve this problem, a concrete subclass of FeatureStructureBoundedCommand must
 * implement the following two methods:
 *      getRedoFocusOID() - returning the "focus entity" after execution (or redo) of a command
 *      getUndoFocusOID() - returning the "focus entity" after undo of a command
 * Note that getRedoFocusOID() can be available only AFTER the command has been executed.
 *
 * @author Jay T. Schira
 * @version $Id$
 */
public abstract class FeatureStructureBoundedCommand extends Command {
  protected Axis axis;
  protected GenomeVersion genomeVersion;
  protected Workspace workspace;

  /**
   * Generic constructor...
   */
  public FeatureStructureBoundedCommand(Axis anAxis) {
    if (anAxis == null) {
      ModelMgr.getModelMgr().handleException(new IllegalArgumentException(
        "GeneBoundryCommand must have a non-NULL Axis for construction."));
    }
    this.axis = anAxis;
    this.genomeVersion = anAxis.getGenomeVersion();
    this.workspace = genomeVersion.getWorkspace();
  }


  /**
   * Give package level access to the
   */
  Axis getAxis() {
    return axis;
  }


  Workspace getWorkspace() {
    return workspace;
  }


    /**
    * Execute the command returning the inverse command for undo.
    * @returns an UndoRedoGeneSet instance.
    * @todo: implement this api.entity_model.management.Command abstract method.
    */
    public Command execute() throws java.lang.Exception {
        UndoRedoFeatureStructure undoRedoCommand = new UndoRedoFeatureStructure(getAxis(), toString());

        // Save off the feature structure that will be affected.
        undoRedoCommand.initializeForPreviousCommandsSourceRootFeatureSet(getCommandSourceRootFeatures());

        executeWithNoUndo();

        //The undo command has the undoFocus/redoFocus inverted
        undoRedoCommand.setUndoRedoFocusOIDs(getRedoFocusOID(), getUndoFocusOID());

        undoRedoCommand.setUndoRedoCommandLogMessage(getCommandLogMessage());

        // Save off the tokens of the feature structure took
        undoRedoCommand.initializeForPreviousCommandsResultsRootFeatureSet(this.getCommandResultsRootFeatures());

        return undoRedoCommand;
    }



  /**
   * template execution method.
   * Throws an exception.
   *
   */
  public abstract void executeWithNoUndo() throws Exception;






  /**
   * Invoked BEFORE the command is executed.
   * @returns the set of root features that WILL be affected.
   * These will be saved off in the state they were in BEFORE the command is executed.
   * Generic UNDO will re-instanciate them.
   */
  public abstract HashSet getCommandSourceRootFeatures();


  /**
   * Return the OID of the feature that should have the user focus after the
   * command is undo-ed.
   */
  protected OID getUndoFocusOID() {
      return null;
  }

  protected OID getRedoFocusOID() {
      return null;
  }

  /**
   * Invoked AFTER the command is executed.
   * @returns the set of root features that display changes "after" the command has executed.
   * These will be removed from the GenomeVersion during a generic UNDO and replaced
   * by the Source Root Features returned from getCommandSourceRootFeatures();
   * Generic UNDO will re-instanciate them.
   */
  public abstract HashSet getCommandResultsRootFeatures();

}