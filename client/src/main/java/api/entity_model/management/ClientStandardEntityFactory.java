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

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Deepali Bhandari
 * @version $Id$
 */

import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.*;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.fundtype.MutableAlignment;
import api.entity_model.model.genetics.GenomeVersion;
import api.stub.data.GenomicEntityComment;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;

import java.util.*;

public class ClientStandardEntityFactory extends StandardEntityFactory {
   private CuratedFeature.CuratedFeatureMutator curatedFeatMutator = null;
   private Feature.FeatureMutator featMutator=null;



   public ClientStandardEntityFactory( Integer creationKey )
  {
    super(creationKey);
  }



 public GeometricAlignment createFeatureFromPromoted(Alignment promotedAlignment)throws Exception{

   CuratedGene promotedGene = null;
   CuratedTranscript promotedTrans = null;
   HashMap promotedGEToWorkspaceGE = new HashMap();
   HashMap workspaceGEToAlignment = new HashMap();
   ArrayList orderedPromotedGE = new ArrayList();

   // Get the promoted Gene...
   if(promotedAlignment.getEntity() instanceof CuratedExon ||(promotedAlignment.getEntity() instanceof CuratedCodon)) {
      promotedTrans = (CuratedTranscript)(((CuratedFeature)promotedAlignment.getEntity()).getSuperFeature());
      promotedGene = (CuratedGene)(promotedTrans.getSuperFeature());
   }
   else if(promotedAlignment.getEntity() instanceof CuratedTranscript) {
      promotedGene = (CuratedGene)(((CuratedFeature)promotedAlignment.getEntity()).getSuperFeature());
   }
   else {
      promotedGene = (CuratedGene)(promotedAlignment.getEntity());
   }



   CuratedGene newcg=(CuratedGene)makeScratchFeatureFromPromoted(promotedGene,
                                        null,
                                        promotedGEToWorkspaceGE,
                                        workspaceGEToAlignment,
                                        orderedPromotedGE);

   Collection geneSubFeatures=promotedGene.getSubFeatures();
   for (Iterator geneIter = geneSubFeatures.iterator();geneIter.hasNext();) {
      CuratedTranscript trscpt=(CuratedTranscript)(geneIter.next());
      CuratedTranscript newct=(CuratedTranscript)makeScratchFeatureFromPromoted(trscpt,
                                              newcg,
                                              promotedGEToWorkspaceGE,
                                              workspaceGEToAlignment,
                                              orderedPromotedGE);

      Collection transSubFeatures=trscpt.getSubFeatures();
      for(Iterator transcriptIter = transSubFeatures.iterator();transcriptIter.hasNext();) {
        CuratedExon ce=(CuratedExon)(transcriptIter.next());
        makeScratchFeatureFromPromoted(ce, newct, promotedGEToWorkspaceGE, workspaceGEToAlignment, orderedPromotedGE );
      }

     //attach the codon if any
      if(trscpt.getStopCodon()!=null){
        makeScratchFeatureFromPromoted(trscpt.getStopCodon(), newct, promotedGEToWorkspaceGE, workspaceGEToAlignment, orderedPromotedGE );
      }
      if(trscpt.getStartCodon()!=null){
        makeScratchFeatureFromPromoted(trscpt.getStartCodon(), newct, promotedGEToWorkspaceGE, workspaceGEToAlignment, orderedPromotedGE );
      }
   }  // End of the Gene's sub-features iterator...

   // Second pass, for each  needs to;
   CuratedFeature promotedFeature;
   CuratedFeature scratchFeature;
   CuratedFeature.CuratedFeatureMutator promotedFeatureMutator;
   Alignment theAlignment;
   // Do this in the Gene, Transcript, Exon order so the views are happy.
   for (Iterator promotedGEs = orderedPromotedGE.iterator(); promotedGEs.hasNext(); ) {
     // Get the promoted and scratch features we're working with...
     promotedFeature = (CuratedFeature)promotedGEs.next();
     scratchFeature = (CuratedFeature)promotedGEToWorkspaceGE.get(promotedFeature);
     // Call the property manager...
     PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.COPIED_ENTITY, scratchFeature, false);
     scratchFeature.getMutator(this,"acceptCuratedFeatureMutator");
     promotedFeatureMutator = curatedFeatMutator;  // Stash away the mutator before some other call back re-sets it.

     // Add the alignment after ALL modifications have been made.
     theAlignment = (Alignment)workspaceGEToAlignment.get(scratchFeature);
     if (theAlignment != null) {
       promotedFeatureMutator.addAlignmentToAxis(theAlignment);
     }
     else {
       System.out.println("Got a null Alignment.");
     }

     promotedFeatureMutator.setupReplacementOfPromoted(promotedFeature);
   }
   newcg.getMutator(this,"acceptCuratedFeatureMutator");
   promotedFeatureMutator = curatedFeatMutator;  // Stash away the mutator before some other call back re-sets it.
   promotedFeatureMutator.setFeatureStructureUnderConstruction(false);

   return newcg.getOnlyGeometricAlignmentToOnlyAxis();
 }


   /**
    * Takes the promoted Feature's comments and clones them for the
    * new workspace feature
    */
    private void cloneComments(Feature promotedFeat, Feature newWorkspaceFeat){
      Set oldComments =promotedFeat.getComments();
      if (oldComments!=null) {
        for (Iterator iter=oldComments.iterator();iter.hasNext();) {
          newWorkspaceFeat.getMutator(this,"acceptFeatureMutator");
          featMutator.addComment((GenomicEntityComment)iter.next());
        }
      }
    }



    /**
     * Clone the property(s) of the promoted featur and add them to the
     * new workspace feature.
     */
    private void cloneProperties(Feature promotedFeat, Feature newWorkspaceFeat){
      newWorkspaceFeat.getMutator(this,"acceptFeatureMutator");
      Set promotedFprops=promotedFeat.getProperties();
      featMutator.addProperties(promotedFprops);
    }


    /**
     * Call back method to accept a CuratedFeatureMutator...
     * Puts it in the instance variable this.curatedFeatureMutator.
     */
    public void acceptCuratedFeatureMutator(GenomicEntity.GenomicEntityMutator mutator){
      if(mutator instanceof CuratedFeature.CuratedFeatureMutator){
        this.curatedFeatMutator=(CuratedFeature.CuratedFeatureMutator)mutator;
      }
    }


    /**
     * Call back method to accept a FeatureMutator...
     * Puts it in the instance variable this.featMutator.
     */
    public void acceptFeatureMutator(GenomicEntity.GenomicEntityMutator mutator){
      if(mutator instanceof Feature.FeatureMutator){
           this.featMutator=(Feature.FeatureMutator)mutator;
      }
    }



  /**
   * Create a scratch feature from a promoted feature.
   * Adds the evidence.
   * Puts the scratch feature and promoted feature into the promotedGEToWorkspaceGE hashmap.
   * Puts a copy of the alignment into workspaceGEToAlignment.
   * Puts the promotedFeat into orderedPromotedGE.
   * Returns the newly created scratch feature.
   */
  private GenomicEntity makeScratchFeatureFromPromoted(CuratedFeature promotedFeat,
                                            CuratedFeature scratchSuperFeature,
                                            HashMap promotedGEToWorkspaceGE,
                                            HashMap workspaceGEToAlignment,
                                            ArrayList orderedPromotedGE){
    Feature newScratchFeat;
    Collection evidences=promotedFeat.getEvidenceOids();
    GenomeVersion gv=promotedFeat.getOnlyGeometricAlignmentToOnlyAxis().getAxis().getGenomeVersion();
    OID featoid=OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.SCRATCH_NAMESPACE,gv.getID());
    newScratchFeat=(Feature)create(featoid,
                                    featoid.toString(),
                                    promotedFeat.getEntityType(),
                                    "Curation",
                                    null,
                                    scratchSuperFeature,
                                    promotedFeat.getDisplayPriority()
                                    );
    // Get the mutator...
    newScratchFeat.getMutator(this, "acceptFeatureMutator");

    // Set the feature "Under Construction"...
    featMutator.setUnderConstruction(true);

    // Add the promoted evidences to the new scratch feature...
    featMutator.addAllEvidenceOids(evidences);

    // Clone the comments over...
    cloneComments(promotedFeat,newScratchFeat);

    // Clone the properties over...
    cloneProperties(promotedFeat,newScratchFeat);




    // Clone the alignment, and stash the alignment away for the second pass...
    // New alignment must be Mutable...
    GeometricAlignment promotedAlignment = promotedFeat.getOnlyGeometricAlignmentToOnlyAxis();
    GeometricAlignment newScratchAlignment = new MutableAlignment(promotedAlignment.getAxis(),
                            newScratchFeat,promotedAlignment.getRangeOnAxis());
    workspaceGEToAlignment.put(newScratchFeat,newScratchAlignment);

    // Drop them into the hashmap...
    promotedGEToWorkspaceGE.put(promotedFeat,newScratchFeat);

    // Drop the promoted feature into the ordered list...
    orderedPromotedGE.add(promotedFeat);

    return newScratchFeat;

  }

}