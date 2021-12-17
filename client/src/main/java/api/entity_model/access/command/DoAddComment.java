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


import api.entity_model.model.annotation.Feature;
import api.entity_model.model.fundtype.Axis;
import api.stub.data.GenomicEntityComment;
import api.stub.data.OID;

import java.util.Date;
import java.util.HashSet;



/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 *
 * This command is used to add a comment to an existing Feature.
 * This command is a FeatureStructureBoundedCommand, which means it's affects
 * (both direct and indirect) are limited to a predictable feature structure
 * boundary and can have a generic Undo / Redo command.
 *
 * @author       Deepali Bhandari
 * @version $Id$
 */
public class DoAddComment extends FeatureStructureBoundedCommand {
  private static boolean DEBUG_CLASS = false;
  private String cmdName = "Add Comment";
  private Feature feature = null;
  private GenomicEntityComment comment = null;
  private String undoToScratchState = null;

  /**
   * Constuctor used when acting as the original command, and NOT as an Undo...
   */
  public DoAddComment(Axis anAxis, Feature aFeature, GenomicEntityComment aComment) {
    super(anAxis);
    this.feature = aFeature;
    this.comment = aComment;
    // this.setIsActingAsUndoAndUndoName(false, null);
  }


    /**
     * Constuctor used when acting as an Undo...
     */
    public DoAddComment(Axis anAxis, Feature aFeature, GenomicEntityComment aComment,
                        String undoCommandName, String undoToScratchState) {
      super(anAxis);
      this.feature = aFeature;
      this.comment = aComment;
      this.setIsActingAsUndoAndUndoName(true, undoCommandName);
      this.undoToScratchState = undoToScratchState;
    }


    /**
     * Invoked BEFORE the command is executed.
     * @returns the set of pre-existing root features that will be affected.
     */
    public HashSet getCommandSourceRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      rootFeatureSet.add(this.feature.getRootFeature());
      return rootFeatureSet;
    }


    /**
     * Invoked AFTER the command is executed.
     * @returns the set of root features that display changes "after" the command has executed.
     */
    public HashSet getCommandResultsRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      rootFeatureSet.add(this.feature.getRootFeature());
      return rootFeatureSet;
    }


    /**
     * Execute the command with out returning the undo.
     * The undo will be created for us by the GeneBoundaryCommand super-class.
     */
    public void executeWithNoUndo() throws Exception {
      if (DEBUG_CLASS) System.out.println("DoAddComment: Executing!");
        Feature.FeatureMutator featureMutator = null;
        featureMutator = mutatorAcceptor.getFeatureMutatorFor(feature);
        featureMutator.addComment(comment);
        this.timeofCommandExecution=new Date().toString();
        setFocusEntity(feature);
    }


    protected OID getUndoFocusOID() {
      return feature.getOid();
    }

    protected OID getRedoFocusOID() {
      return feature.getOid();
    }

    public String toString() {
        return cmdName;
    }



    /** This returns the Log message with the time stamp expalaning which entities
    * underewent change, of what kind
    *
    */
   public String getCommandLogMessage() {

     String featureType=feature.getEntityType().toString();
     String featureId=feature.getOid().toString();
     this.actionStr="Added Comment ="+comment.toString()+" on Entity "+featureType+" id "+featureId+"" ;
     return(super.getCommandLogMessage());

   }
}
