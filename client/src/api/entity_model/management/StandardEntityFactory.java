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
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
*********************************************************************/

import api.entity_model.model.annotation.BlastHit;
import api.entity_model.model.annotation.ComputedCodon;
import api.entity_model.model.annotation.CuratedCodon;
import api.entity_model.model.annotation.CuratedExon;
import api.entity_model.model.annotation.CuratedGene;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.annotation.HSPFeature;
import api.entity_model.model.annotation.HitAlignmentFeature;
import api.entity_model.model.annotation.InvalidFeatureStructureException;
import api.entity_model.model.annotation.MiscComputedFeature;
import api.entity_model.model.annotation.PolyMorphism;
import api.entity_model.model.annotation.SpliceSite;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.GenomicEntity;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.OID;

/**
 *  Standard factory for creating specific Genomic Entity subtypes using
 *  a parameterized factory method (parameterized on EntityType).
 *  All products must share the GenomicEntity interface.
 */
public class StandardEntityFactory extends GenomicEntityFactory {
   public StandardEntityFactory(Integer creationKey) {
      super(creationKey);
   }

   public GenomicEntity create(OID oid, String displayName, EntityType type, String discoveryEnvironment) {
      return create(oid, displayName, type, discoveryEnvironment, null, null, FeatureDisplayPriority.DEFAULT_PRIORITY);
   }

   public GenomicEntity create(OID oid, String displayName, EntityType type, String discoveryEnvironment, String subClassification, GenomicEntity parent, byte displayPriority) {
      return create(oid, displayName, type, discoveryEnvironment, subClassification, parent, displayPriority, null);
   }

   public GenomicEntity create(OID oid, String displayName, EntityType type, String discoveryEnvironment, String subClassification, GenomicEntity parent, byte displayPriority, FacadeManagerBase overrideLoaderManager) {
      return create(oid, displayName, type, discoveryEnvironment, subClassification, parent, overrideLoaderManager, displayPriority);
   }

   public GenomicEntity create(OID oid, String displayName, EntityType type, String discoveryEnvironment, String subClassification, GenomicEntity parent, FacadeManagerBase overrideLoaderManager, byte featureDisplayPriority) {
      GenomicEntity newEntity = null;
      /**
       * @todo  call constructor for each type of entity ...
       */
      int entityTypeValue = type.value();

      Feature featureParent = null;
      if (parent != null) {
         featureParent = (Feature)parent;
      }
      try {
         switch (entityTypeValue) {
            case EntityTypeConstants.BlastX_Hit :
            case EntityTypeConstants.BlastN_Hit :
            case EntityTypeConstants.tBlastN :
            case EntityTypeConstants.tBlastX :
               // Create a new BlastHit class
               newEntity = new BlastHit(oid, displayName, type, discoveryEnvironment, overrideLoaderManager, featureParent, featureDisplayPriority);
               break;

            case EntityTypeConstants.Atalanta_Hit :
            case EntityTypeConstants.ESTMapper_Hit :
            case EntityTypeConstants.Sim4_Hit :
            case EntityTypeConstants.Genewise_Peptide_Hit :
               // Create a new HitAlignment class
               newEntity = new HitAlignmentFeature(oid, displayName, type, discoveryEnvironment, overrideLoaderManager, featureParent, featureDisplayPriority);
               break;

            case EntityTypeConstants.NonPublic_Gene :
               // Create a curated gene type:
               newEntity = new CuratedGene(oid, displayName, type, discoveryEnvironment, overrideLoaderManager, featureParent, featureDisplayPriority);
               break;

            case EntityTypeConstants.NonPublic_Transcript :
               // Create a curated transcript type:
               newEntity = new CuratedTranscript(oid, displayName, type, discoveryEnvironment, overrideLoaderManager, featureParent, featureDisplayPriority);
               break;

            case EntityTypeConstants.Exon :
               // Create a curated feature type:
               newEntity = new CuratedExon(oid, displayName, type, discoveryEnvironment, overrideLoaderManager, featureParent, featureDisplayPriority);
               break;

            case EntityTypeConstants.Start_Codon_Start_Position :
            case EntityTypeConstants.StopCodon :
            case EntityTypeConstants.Translation_Start_Position :
               newEntity = new CuratedCodon(oid, displayName, type, discoveryEnvironment, overrideLoaderManager, (CuratedTranscript)featureParent, featureDisplayPriority);
               break;

            case EntityTypeConstants.High_Scoring_Pair :
            case EntityTypeConstants.Sim4_Feature_Detail :
            case EntityTypeConstants.Genewise_Peptide_Hit_Part :
            case EntityTypeConstants.Atalanta_Feature_Detail :
            case EntityTypeConstants.ESTMapper_Feature_Detail :
               newEntity = new HSPFeature(oid, displayName, type, discoveryEnvironment, overrideLoaderManager, featureParent, featureDisplayPriority);
               break;

            case EntityTypeConstants.SNP :
               newEntity = new PolyMorphism(oid, displayName, type, discoveryEnvironment, overrideLoaderManager, featureParent, featureDisplayPriority);
               break;

            case -3 : //_COMPUTED_STOP_CODON
            case -2 : //_COMPUTED_START_CODON
               newEntity = new ComputedCodon(oid, displayName, type, discoveryEnvironment, overrideLoaderManager, featureDisplayPriority);
               break;

            case -7 : //_ACCEPTOR_SPLICE_SITE
            case -8 : //_DONOR_SPLICE_SITE
               newEntity = new SpliceSite(oid, displayName, type, discoveryEnvironment, overrideLoaderManager, featureDisplayPriority);
               break;

            default :
               // For all other types create a base Feature instance
               newEntity = new MiscComputedFeature(oid, displayName, type, discoveryEnvironment, overrideLoaderManager, featureParent, featureDisplayPriority);
               break;
         }
      }
      catch (InvalidFeatureStructureException fcEx) {
         FacadeManager.handleException(fcEx);
         newEntity = null;
      }

      return newEntity;
   }

}
