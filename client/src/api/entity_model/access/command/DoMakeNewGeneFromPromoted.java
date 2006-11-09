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

import api.entity_model.management.ClientStandardEntityFactory;
import api.entity_model.management.CommandExecutionException;
import api.entity_model.management.GenomicEntityFactory;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.CuratedFeature;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.SingleAlignmentSingleAxis;
import api.facade.abstract_facade.annotations.GeneFacade;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class DoMakeNewGeneFromPromoted extends FeatureStructureBoundedCommand {
  private static boolean DEBUG_CLASS = false;
  private String cmdName = "Create New Gene From Promoted";
    private Feature promotedFeat;
    private CuratedFeature newGene;


    /**
     * Constructor...
     */
    public DoMakeNewGeneFromPromoted(Axis anAxis, Feature promotedFeat) {
      super(anAxis);
      this.promotedFeat = promotedFeat;
    }


    /**
     * Invoked BEFORE the command is executed.
     * @returns the set of root features that will be affected.
     * This command does not affect any pre-exisitng features.
     * Return an empty set.
     */
    public HashSet getCommandSourceRootFeatures() {
      return new HashSet();
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
     * Execute the command with out returning the undo.
     * The undo will be created for us by the GeneBoundaryCommand super-class.
     */
    public void executeWithNoUndo() throws Exception {
      if (DEBUG_CLASS) {
        System.out.println("ENTER => DoMakeNewGeneFromPromoted.executeWithNoUndo();");
      }
      newGene = doMakeNewGeneFromPromoted(promotedFeat);
      if (DEBUG_CLASS) {
        System.out.println("New Feature (" + newGene.getEntityType().getEntityName()
                            + "." + newGene.getOid() + ")");
        Set newFeatureProps=newGene.getProperties();
        for (Iterator itr = newFeatureProps.iterator(); itr.hasNext(); ) {
          GenomicProperty prop = (GenomicProperty)itr.next();
          System.out.println("New Feat Prop: " + prop.debugString());
        }
        System.out.println("EXIT <= DoMakeNewGeneFromPromoted.executeWithNoUndo();");
      }
      setFocusEntity(newGene);
      this.timeofCommandExecution=new Date().toString();
    }

    protected OID getUndoFocusOID() {
      return promotedFeat.getOid();
    }

    protected OID getRedoFocusOID() {
      return newGene.getOid();
    }

    public CuratedFeature doMakeNewGeneFromPromoted(Feature fpromoted) throws Exception {
      //ClientStandardEntityFactory  clientFactory=(ClientStandardEntityFactory)((ModelMgr.getModelMgr()).getEntityFactory());
      GenomicEntityFactory  geFactory = (ModelMgr.getModelMgr()).getEntityFactory();
      ClientStandardEntityFactory  clientFactory=(ClientStandardEntityFactory)geFactory;
      if (!(fpromoted instanceof SingleAlignmentSingleAxis)) {
          // todo: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s).
          System.out.println("DoMakeNewGeneFromPromotedCommand: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s)");
      }
      GeometricAlignment promotedAlignment = ((SingleAlignmentSingleAxis)fpromoted).getOnlyGeometricAlignmentToOnlyAxis();
      if (promotedAlignment == null) {
          throw new CommandExecutionException(this, "Can not get ONLY alignment to ONLY axis on feature"
                                  + fpromoted.getEntityType().getEntityName()
                                  + ":" + fpromoted.getOid());
      }
      Alignment cgAlignment=(clientFactory.createFeatureFromPromoted(promotedAlignment));
      CuratedFeature cg=(CuratedFeature)cgAlignment.getEntity();
     // cg.setModifiedBy(System.getProperty("user.name"));
     // cg.setModifiedDate(Util.getDateTimeStringNow());

      // set the "replaces information"
      // Factory does this for us now.
      // setReplacesForPromoted(promotedFeat,cg);

      // to set the properties

      return cg;

    }
   /**
    * Walks the selected feature from the Gene node and sets the
    * replaces type for every proxy in the model as "unmodified"
    *
    */

   private void setReplacesForPromoted(Feature replacedFeat, CuratedFeature replacingcg){

     //CuratedGene cg;
     //CuratedTranscript ct=null;

     //if(replacedFeat instanceof CuratedExon){
     //  ct=(CuratedTranscript) replacedFeat.getSuperFeature();
     //  cg=(CuratedGene)ct.getSuperFeature();
     //}else if(replacedFeat instanceof CuratedTranscript){
     //  cg=(CuratedGene)replacedFeat.getSuperFeature();
     //}else{
     //  cg=(CuratedGene)replacedFeat;
     //}
   // Setting the "replaces" relationship for Gene
  // replacingcg.setReplaced(cg);
  // replacingcg.setScratchModifiedState(Feature.SCRATCH_MOD_STATE_UNMODIFIED);
  /**
   * @TODO: finish setting the replaces type once the Change Model gets implemented
   */
  /*
  while(geneSubFeatures.iterator().hasNext()&& replacingTranscripts.iterator().hasNext()){
   CuratedTranscript trscpt=(CuratedTranscript)geneSubFeatures.iterator().next();
   CuratedTranscript replacingct=(CuratedTranscript)replacingTranscripts.iterator().next();

   // Setting the "replaces" relationship for Transcript
   //replacingct.setReplaced(trscpt);
  // replacingct.setScratchModifiedState(FeaturePI.SCRATCH_MOD_STATE_UNMODIFIED);

   // Establish replaces relationship for Codons
   CuratedCodon replacedStartCodon=trscpt.getStartCodon();
   CuratedCodon replacedStopCodon=trscpt.getStopCodon();
   CuratedCodon replacingStartCodon=replacingct.getStartCodon();
   CuratedCodon replacingStopCodon=replacingct.getStopCodon();

   if(replacedStartCodon!=null && replacingStartCodon!=null ){
    //replacingStartCodon.setReplaced(replacedStartCodon);
    //replacingStartCodon.setScratchModifiedState(FeaturePI.SCRATCH_MOD_STATE_UNMODIFIED);
   }
   if(replacedStopCodon!=null && replacingStopCodon!=null ){
    //replacingStopCodon.setReplaced(replacedStopCodon);
    //replacingStopCodon.setScratchModifiedState(FeaturePI.SCRATCH_MOD_STATE_UNMODIFIED);
   }
   Collection transSubFeatures=trscpt.getSubFeatures();
   Collection replacingExons=replacingct.getSubFeatures();

   while(transSubFeatures.iterator().hasNext() && replacingExons.iterator().hasNext()){
     CuratedExon ce=(CuratedExon)transSubFeatures.iterator().next();
     CuratedExon replacingce=(CuratedExon)replacingExons.iterator().next();
     // Setting the "replaces" relationship for Exon
     //replacingce.setReplaced(ce);
     //replacingce.setScratchModifiedState(FeaturePI.SCRATCH_MOD_STATE_UNMODIFIED);
    }// inner while

  }// outer while
  */
}


    public String toString() {
        return cmdName;
    }



   /** This returns the Log message with the time stamp expalaning which entities
    * underwent change, of what kind
    *
    */
   public String getCommandLogMessage() {

     String promId=promotedFeat.getOid().toString();
     String promType=promotedFeat.getEntityType().toString();
     String wsGeneAcc=newGene.getProperty(GeneFacade.GENE_ACCESSION_PROP).getInitialValue();
     String wsGeneId=newGene.getOid().toString();
     this.actionStr="Cloned Promoted "+promType+ " Feature "+" with id= "+promId +" To Workspace Gene "+wsGeneAcc+" with id= "+wsGeneId;
     return(super.getCommandLogMessage());
   }

}
