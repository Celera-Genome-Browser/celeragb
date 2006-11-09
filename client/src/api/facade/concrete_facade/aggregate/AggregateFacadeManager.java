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
/**
 * CVS_ID:  $Id$
 */

package api.facade.concrete_facade.aggregate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import api.entity_model.model.fundtype.EntityType;
import api.facade.abstract_facade.annotations.AtalantaDetailFacade;
import api.facade.abstract_facade.annotations.AtalantaHitFacade;
import api.facade.abstract_facade.annotations.BlastHitFacade;
import api.facade.abstract_facade.annotations.ESTMapperHitFacade;
import api.facade.abstract_facade.annotations.ESTMapperDetailFacade;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.facade.abstract_facade.annotations.GeneFacade;
import api.facade.abstract_facade.assembly.ContigFacade;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.facade.abstract_facade.genetics.ControlledVocabService;
import api.facade.facade_mgr.FacadeManager;

/** This class provides the concrete implementation of FacadeManagerBase for the Aggregate Protocol */
public class AggregateFacadeManager extends api.facade.facade_mgr.FacadeManagerBase {

   private Map featureFacadesWithCommonInterface = new HashMap();
   private Map featureFacadesWithHitAlignDetailInterface = new HashMap();
   private AggregateGenomeVersionFacade genomeVersion;
   private AggregateContigFacade contigFacade;
   private AggregateSpeciesFacade speciesFacade;
   private AggregateChromosomeFacade chromosomeFacade;
   private AggregateGenomicAxisFacade genomicaxisFacade;
   private AggregateAtalantaHitFacade atalantaHitFacade;
   private AggregateAtalantaDetailFacade atalantaDetailFacade;
   private AggregateBlastHitFacade blastHitFacade;
   private AggregateESTMapperHitFacade estmapperHitFacade;
   private AggregateESTMapperDetailFacade estmapperDetailFacade;
   private AggregateGeneFacade geneFacade;
   private AggregateGenomeLocatorFacade genomeLocator;
   private api.entity_model.model.genetics.Species speciesForCurrentLocator;
   private AggregateControlledVocabService vocabService;
   private List inUseProtocols = new ArrayList();

   public AggregateFacadeManager() {
   }

   public String getDataSourceSelectorClass() {
      return "";
   }

   public Object[] getOpenDataSources() {
      List inUseProtocols = FacadeManager.getInUseProtocolStrings();
      String[] inUseProtocolArray = new String[inUseProtocols.size()];
      inUseProtocols.toArray(inUseProtocolArray);
      List dataSources = new ArrayList();
      Object[] tmpArray;
      for (int i = 0; i < inUseProtocolArray.length; i++) {
         tmpArray = FacadeManager.getFacadeManager(inUseProtocolArray[i]).getOpenDataSources();
         for (int j = 0; j < tmpArray.length; j++) {
            dataSources.add(tmpArray[j]);
         }
      }
      return dataSources.toArray();
   }

   /** Using Aggregate Version */
   public api.facade.abstract_facade.genetics.GenomeVersionLoader getGenomeVersion() throws Exception {
      if (genomeVersion == null)
         genomeVersion = new AggregateGenomeVersionFacade();
      return genomeVersion;
   }

   public api.facade.abstract_facade.genetics.ChromosomeLoader getChromosome() throws Exception {
      if (chromosomeFacade == null)
         chromosomeFacade = new AggregateChromosomeFacade();
      return chromosomeFacade;
   }

   public api.facade.abstract_facade.assembly.GenomicAxisLoader getGenomicAxis() throws Exception {
      if (genomicaxisFacade == null)
         genomicaxisFacade = new AggregateGenomicAxisFacade();
      return genomicaxisFacade;
   }

   public api.facade.abstract_facade.genetics.GenomeLocatorFacade getGenomeLocator() throws Exception {
      if (genomeLocator == null) {
         genomeLocator = new AggregateGenomeLocatorFacade();
      }
      return genomeLocator;
   }

   public api.facade.abstract_facade.genetics.SpeciesLoader getSpecies() throws Exception {
      if (speciesFacade == null) {
         speciesFacade = new AggregateSpeciesFacade();
      }
      return speciesFacade;
   }

   public String getServerName() {
      return "";
   }

   /**
    * Returns a gene facade.  Gene facade is used in curating genes.  It
    * provides information such as comments, etc.
    */
   private GeneFacade getGeneFacade() throws Exception {
      if (geneFacade == null)
         geneFacade = new AggregateGeneFacade();
      return geneFacade;
   } // End method: getGeneFacade

   private BlastHitFacade getBlastHitFacade() throws Exception {
      if (blastHitFacade == null)
         blastHitFacade = new AggregateBlastHitFacade();
      return blastHitFacade;
   }

   private AtalantaHitFacade getAtalantaHitFacade() throws Exception {
      if (atalantaHitFacade == null) {
         atalantaHitFacade = new AggregateAtalantaHitFacade();
      }
      return (atalantaHitFacade);
   }

   private AtalantaDetailFacade getAtalantaDetailFacade(EntityType featureType) throws Exception {
      if (atalantaDetailFacade == null) {
         atalantaDetailFacade = new AggregateAtalantaDetailFacade(featureType);
      }
      return (atalantaDetailFacade);
   }
   
   private ESTMapperHitFacade getESTMapperHitFacade() throws Exception {
      if (estmapperHitFacade == null) {
         estmapperHitFacade = new AggregateESTMapperHitFacade();
      }
      return (estmapperHitFacade);
   }

   private ESTMapperDetailFacade getESTMapperDetailFacade(EntityType featureType) throws Exception {
      if (estmapperDetailFacade == null) {
         estmapperDetailFacade = new AggregateESTMapperDetailFacade(featureType);
      }
      return (estmapperDetailFacade);
   }

   public FeatureFacade getFacade(EntityType featureType) throws Exception {
      //Facades listed explicitly have some methods and therefore need to be coded
      //All others simply inherit Feature Facade and therefore can use the comman Interface
      switch (featureType.value()) {
         case EntityTypeConstants.BlastN_Hit :
         case EntityTypeConstants.BlastX_Hit :
         case EntityTypeConstants.tBlastN :
         case EntityTypeConstants.Genewise_Peptide_Hit :
         case EntityTypeConstants.Sim4_Hit :
         case EntityTypeConstants.tBlastX :
            return (this.getBlastHitFacade());

         case EntityTypeConstants.Sim4_Feature_Detail :
         case EntityTypeConstants.High_Scoring_Pair :
         case EntityTypeConstants.Genewise_Peptide_Hit_Part :
         {
            if (featureFacadesWithHitAlignDetailInterface.containsKey(featureType)) {
               return ((FeatureFacade)featureFacadesWithHitAlignDetailInterface.get(featureType));
            }
            AggregateHitAlignmentDetailLoader facade = new AggregateHitAlignmentDetailLoader(featureType);
            featureFacadesWithHitAlignDetailInterface.put(featureType, facade);
            return (facade);
         }
         case EntityTypeConstants.Atalanta_Hit :
         {
            return (this.getAtalantaHitFacade());
         }
         case EntityTypeConstants.Atalanta_Feature_Detail :
         {
            return (this.getAtalantaDetailFacade(featureType));
         }
         case EntityTypeConstants.ESTMapper_Hit :
         {
            return (this.getESTMapperHitFacade());
         }
         case EntityTypeConstants.ESTMapper_Feature_Detail :
         {
            return (this.getESTMapperDetailFacade(featureType));
         }
         default :
         {
            if (featureFacadesWithCommonInterface.containsKey(featureType)) {
               return (FeatureFacade)featureFacadesWithCommonInterface.get(featureType);
            }
            AggregateTypeSpecificFeatureFacade facade = new AggregateTypeSpecificFeatureFacade(featureType);
            featureFacadesWithCommonInterface.put(featureType, facade);
            return (facade);
         }
      }
   }

   public ContigFacade getContigFacade() {
      if (contigFacade == null)
         contigFacade = new AggregateContigFacade();
      return contigFacade;
   }

   public ControlledVocabService getControlledVocabService() throws Exception {
      if (vocabService == null)
         vocabService = new AggregateControlledVocabService();
      return vocabService;
   }

}
