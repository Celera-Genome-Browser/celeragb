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
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/

package api.facade.concrete_facade.xml;

import api.entity_model.model.fundtype.EntityType;
import api.facade.abstract_facade.annotations.*;
import api.facade.abstract_facade.assembly.ContigFacade;
import api.facade.abstract_facade.assembly.GenomicAxisLoader;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.facade.abstract_facade.genetics.ControlledVocabService;
import api.facade.abstract_facade.genetics.GenomeVersionLoader;
import api.facade.abstract_facade.genetics.SpeciesLoader;
import api.facade.concrete_facade.xml.sax_support.PropertySource;
import api.facade.facade_mgr.FacadeManagerBase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base class for all XML-file facade managers.  Returns all types of API facades
 * and manages their creation.
 */
public abstract class XmlFacadeManager extends FacadeManagerBase {

   private static final String MATCH_PUB_CURATION_FLAG = "Match Pub Seq";
   private static final String IS_BICISTRONIC = "Is Bicistronic";
   private static final String IS_CHIMERIC = "Is Chimeric";
   private static final String NON_CODING_GENE = "Non-Coding Gene";
   private static final String IS_TRUNCATED_FLAG = "Is Truncated";
   private static final String KNOWN_GENE_FLAG = "Known Gene";
   private static final String PREDICTED_ORF_FLAG = "Predicted ORF";
   private static final String RETAINING_INTRON = "Retaining Intron";
   private static final String PREMATURE_TERMINATION = "Premature Termination";
   private static final String IS_PARTIAL_FLAG = "Is Partial";
   private static final String END_OF_SCAFFOLD_FLAG = "End Of Scaffold";
   private static final String IS_PSEUDOGENE_FLAG = "Is Pseudogene";
   private static final String WITH_GAPS_FLAG = "With Gaps";
   private static final String NO_FULL_LENGTH_CDNA_FLAG = "No Full Length cDNA";
   private static final String FRAMESHIFT_FLAG = "Frame Shift";
   private static final String CONSENSUS_ERROR_FLAG = "Consensus Error";
   private static final String INCORRECT_BOUNDARY_FLAG = "Incorrect Boundary";
   private static final String MATCH_PUBLIC_MRNA_FLAG = "Match Public mRNA";
   private static final String MATCH_INTERNAL_CDNA_FLAG = "Match Internal cDNA";
   private static final String IS_NOT_FULL_LENGTH_FLAG = "Is Not Full Length";
   private static final String TRANSLATION_EXCEPTION_FLAG = "Translation Exception";
   private static final String HAS_MULTI_EXON = "has_multi_exon";

   static {
      // Entry point for this facade:  will prepopulate any and all
      // classes requiring static data initialization at this time.
      // Initialize static variables that are multiple steps to init.
      // Property source class needs mappings.

      try {
         Map<String,String> controlledMap = new HashMap<String, String>();

         // The curation flags...
         controlledMap.put(MATCH_PUB_CURATION_FLAG, "CURATION_FLAG");
         controlledMap.put(IS_TRUNCATED_FLAG, "CURATION_FLAG");
         controlledMap.put(CONSENSUS_ERROR_FLAG, "CURATION_FLAG");
         controlledMap.put(INCORRECT_BOUNDARY_FLAG, "CURATION_FLAG");
         controlledMap.put(MATCH_PUBLIC_MRNA_FLAG, "CURATION_FLAG");
         controlledMap.put(MATCH_INTERNAL_CDNA_FLAG, "CURATION_FLAG");
         controlledMap.put(IS_NOT_FULL_LENGTH_FLAG, "CURATION_FLAG");
         controlledMap.put(KNOWN_GENE_FLAG, "CURATION_FLAG");
		 controlledMap.put(IS_BICISTRONIC, "CURATION_FLAG");
		 controlledMap.put(IS_CHIMERIC, "CURATION_FLAG");
		 controlledMap.put(NON_CODING_GENE, "CURATION_FLAG");
         controlledMap.put(PREDICTED_ORF_FLAG, "CURATION_FLAG");
         controlledMap.put(RETAINING_INTRON, "CURATION_FLAG");
         controlledMap.put(PREMATURE_TERMINATION, "CURATION_FLAG");
         controlledMap.put(IS_PARTIAL_FLAG, "CURATION_FLAG");
         controlledMap.put(END_OF_SCAFFOLD_FLAG, "CURATION_FLAG");
         controlledMap.put(IS_PSEUDOGENE_FLAG, "CURATION_FLAG");
         controlledMap.put(WITH_GAPS_FLAG, "CURATION_FLAG");
         controlledMap.put(NO_FULL_LENGTH_CDNA_FLAG, "CURATION_FLAG");
         controlledMap.put(FRAMESHIFT_FLAG, "CURATION_FLAG");
         controlledMap.put(TRANSLATION_EXCEPTION_FLAG, "CURATION_FLAG");

         controlledMap.put(HAS_MULTI_EXON, "BOOLEAN");
         controlledMap.put(FeatureFacade.ENTITY_ORIENTATION_PROP, "ORIENTATION");
         //            controlledMap.put(FeatureFacade.IS_COMPOSITE_PROP, "BOOLEAN");
         controlledMap.put(FeatureFacade.DISPLAY_PRIORITY_PROP, "DISPLAY_PRIORITY");
         //            controlledMap.put(FeatureFacade.DATE_CREATED_PROP, "DATE_CONVERTER");
         //            controlledMap.put(FeatureFacade.DATE_CURATED_PROP, "DATE_CONVERTER");
         //            controlledMap.put(FeatureFacade.DATE_REVIEWED_PROP, "DATE_CONVERTER");
         controlledMap.put(GeneFacade.IS_ALTER_SPLICE_PROP, "BOOLEAN");
         controlledMap.put(GenePredictionFacade.IS_COMPLETE_GENE_PROP, "BOOLEAN");
         controlledMap.put(FeatureFacade.ORIENTATION_PROP, "ORIENTATION");

         Map<String, String> editingClassMap = new HashMap<String, String>();
         editingClassMap.put(HitAlignmentFacade.NUM_SUBJ_DEFNS_PROP, "client.gui.other.dialogs.AlignmentSubjectDefinition");
         editingClassMap.put(FeatureFacade.NUM_COMMENTS_PROP, "client.gui.other.dialogs.CommentsViewer");
         editingClassMap.put(FeatureFacade.NUM_ALIGNMENTS_PROP, "client.gui.other.dialogs.AlignmentDefinitionsDialog");

         Map<String, String> forcedValueMap = new HashMap<String, String>();
         forcedValueMap.put(FeatureFacade.NUM_COMMENTS_PROP, "0");

         PropertySource.setPropertyMaps(controlledMap, editingClassMap, forcedValueMap);

         Set<String> flaggedPropertySet = new HashSet<String>();
         flaggedPropertySet.add(TranscriptFacade.CURATION_FLAGS_PROP);
         PropertySource.setFlaggedPropertyTriggers(flaggedPropertySet);
      }
      catch (Exception ex) {
        // Do nothing: prevent hard-to-find load failure
      }
   } // End static initializer.

   private GenomeVersionLoader genomeVersion = null;

   private GenomeVersionSpace genomeVersionSpace = null;

   private GenomicAxisLoader genomicAxisFacade = null;

   private ContigFacade contigFacade = null;

   private FeatureFacade featureFacade = null;

   private HSPFacade hspFacade = null;
   private HitAlignmentDetailLoader hitAlignDetailLdr = null;

   private AtalantaHitFacade atalantaHitFacade = null;
   private AtalantaDetailFacade atalantaDetailFacade = null;
   private BlastHitFacade blastHitFacade = null;
   private ESTMapperHitFacade estmapperHitFacade = null;
   private ESTMapperDetailFacade estmapperDetailFacade = null;

   private HitAlignmentFacade hitAlignmentFacade = null;

   private GeneFacade geneFacade = null;
   private TranscriptFacade transcriptFacade = null;
   private ExonFacade exonFacade = null;
   private ControlledVocabService controlledVocabularyService = null;

   private DeCachingObserver cacheRelease;

   public abstract String getDataSourceSelectorClass();

   /** Tells what has been opened. */
   public abstract Object[] getOpenDataSources();

   /**
    * Make this default constructor to build a detector for GV entity
    * removals.
    */
   public XmlFacadeManager() {
      cacheRelease = new DeCachingObserver(this);
   } // End constructor

   /** Stores a genome version space. */
   protected void setGenomeVersionSpace(GenomeVersionSpace genomeVersionSpace) {
      this.genomeVersionSpace = genomeVersionSpace;
   } // End method: setGenomeVersionSpace

   /** Returns the genome version space for facade setting, etc. */
   public GenomeVersionSpace getGenomeVersionSpace() {
      return genomeVersionSpace;
   }

   /**
    * Called when system is about to close down, or when facade manager is
    * to be released.  Sort of like finalize (in an ideal world ;-)
    */
   public void prepareForSystemExit() {
      super.prepareForSystemExit();

      cacheRelease.unlisten();
      cacheRelease = null;

      genomicAxisFacade = null;

      contigFacade = null;

      featureFacade = null;

      hspFacade = null;
      hitAlignDetailLdr = null;

      blastHitFacade = null;

      hitAlignmentFacade = null;

      geneFacade = null;
      transcriptFacade = null;
      exonFacade = null;
      controlledVocabularyService = null;
   } // End method: prepareForSystemExit

   /** Returns the genome version facade. */
   public GenomeVersionLoader getGenomeVersion() throws Exception {
      if (genomeVersion == null) {
         XmlGenomeVersion xmlGenomeVersion = new XmlGenomeVersion();
         xmlGenomeVersion.setGenomeVersionSpace(getGenomeVersionSpace());
         genomeVersion = xmlGenomeVersion;
      } // Need to create it.
      return genomeVersion;
   } // End method: getGenomeVersion */

   /** Override this in subclass to allow this facade manager to be an origin of chromosome info. */
   protected boolean isOriginOfChromosome() {
      return false;
   }

   // Currently these API facades are either unimplemented in XML, or
   // they are not implemented in ALL types of XML managers.
   public SpeciesLoader getSpecies() throws Exception {
      return null;
   }

   public api.facade.abstract_facade.genetics.ChromosomeLoader getChromosome() throws Exception {
      return null;
   }

   public api.facade.abstract_facade.genetics.GenomeLocatorFacade getGenomeLocator() throws Exception {
      return null;
   }

   /**
    * Returns name of server to satisfy the facade manager requirement.
    */
   public String getServerName() {
      return "XML";
   } // End method: getServerName

   /**
    * Returns new-or-cached transcript API facade.
    */
   private TranscriptFacade getTranscriptFacade() throws Exception {

      if (transcriptFacade == null) {
         transcriptFacade = new XmlTranscriptFacade();
         ((XmlTranscriptFacade)transcriptFacade).setGenomeVersionSpace(getGenomeVersionSpace());
      } // Must create one.

      return transcriptFacade;

   } // End method: getTranscriptFacade

   /**
    * Returns the (or a new) exon facade.
    */
   protected ExonFacade getExonFacade() throws Exception {

      if (exonFacade == null) {
         exonFacade = new XmlExonFacade();
         ((XmlExonFacade)exonFacade).setGenomeVersionSpace(getGenomeVersionSpace());
      } // Must create one.

      return exonFacade;

   } // End method: getExonFacade

   /**
    * Returns a new or cached Gene Facade.
    */
   private GeneFacade getGeneFacade() throws Exception {
      if (geneFacade == null) {
         geneFacade = new XmlGeneFacade();
         ((XmlGeneFacade)geneFacade).setGenomeVersionSpace(getGenomeVersionSpace());
      } // Must create one.

      return geneFacade;

   } // End method: getGeneFacade

   /**
    * Returns a new or cached HitAlignmentFacade.
    */
   private HitAlignmentFacade getHitAlignmentFacade() throws Exception {
      if (hitAlignmentFacade == null) {
         hitAlignmentFacade = new XmlHitAlignmentFacade();

         ((XmlHitAlignmentFacade)hitAlignmentFacade).setGenomeVersionSpace(getGenomeVersionSpace());
      } // Must create one.
      return hitAlignmentFacade;
   } // End method: getHitAlignmentFacade

   private AtalantaHitFacade getAtalantaHitFacade() throws Exception {
      if (atalantaHitFacade == null) {
         atalantaHitFacade = new XmlAtalantaHitFacade();
         ((XmlAtalantaHitFacade)atalantaHitFacade).setGenomeVersionSpace(getGenomeVersionSpace());
      }
      return (atalantaHitFacade);
   }

   /**
    * Returns a new or cached BlastHitFacade.
    */
   private BlastHitFacade getBlastHitFacade() throws Exception {
      if (blastHitFacade == null) {
         blastHitFacade = new XmlBlastHitFacade();
         ((XmlBlastHitFacade)blastHitFacade).setGenomeVersionSpace(getGenomeVersionSpace());
      } // Must create one.

      return blastHitFacade;

   } // End method: getBlastHitFacade

   private ESTMapperHitFacade getESTMapperHitFacade() throws Exception {
      if (estmapperHitFacade == null) {
         estmapperHitFacade = new XmlESTMapperHitFacade();
         ((XmlESTMapperHitFacade)estmapperHitFacade).setGenomeVersionSpace(getGenomeVersionSpace());
      }
      return (estmapperHitFacade);
   }

   private ESTMapperDetailFacade getESTMapperDetailFacade() throws Exception {
      if (estmapperDetailFacade == null) {
         estmapperDetailFacade = new XmlESTMapperDetailFacade();
         ((XmlESTMapperDetailFacade)estmapperDetailFacade).setGenomeVersionSpace(getGenomeVersionSpace());
      }
      return (estmapperDetailFacade);
   }

   /**
    * This is a request-decoder method.  All requests for "api facades"
    * should come through this method.
    */
   public FeatureFacade getFacade(EntityType featureType) throws Exception {

      switch (featureType.value()) {
         case EntityTypeConstants.BlastN_Hit :
         case EntityTypeConstants.BlastX_Hit :
         case EntityTypeConstants.tBlastN :
         case EntityTypeConstants.tBlastX :
            return (this.getBlastHitFacade());

         case EntityTypeConstants.Sim4_Hit :
         case EntityTypeConstants.Genewise_Peptide_Hit :
            return (this.getHitAlignmentFacade());

         case EntityTypeConstants.Atalanta_Hit :
            return (this.getAtalantaHitFacade());

         case EntityTypeConstants.Atalanta_Feature_Detail :
            return (this.getAtalantaDetailFacade());

         case EntityTypeConstants.ESTMapper_Hit :
            return (this.getESTMapperHitFacade());

         case EntityTypeConstants.ESTMapper_Feature_Detail :
            return (this.getESTMapperDetailFacade());

         case EntityTypeConstants.High_Scoring_Pair :
            return (this.getHSPFacade());

         case EntityTypeConstants.Sim4_Feature_Detail :
         case EntityTypeConstants.Genewise_Peptide_Hit_Part :
            return (this.getHitAlignmentDetailLoader());

         case EntityTypeConstants.NonPublic_Gene :
            return (this.getGeneFacade());

         case EntityTypeConstants.NonPublic_Transcript :
            return (this.getTranscriptFacade());

         case EntityTypeConstants.Exon :
            return (this.getExonFacade());

         default :
            return (this.getFeatureFacade());
      }
   } // End method: getFacade

   /**
    * Returns a new or cached genomic axis facade.
    */
   public GenomicAxisLoader getGenomicAxis() throws Exception {

      if (genomicAxisFacade == null) {
         genomicAxisFacade = new XmlGenomicAxisFacade();
         ((XmlGenomicAxisFacade)genomicAxisFacade).setGenomeVersionSpace(getGenomeVersionSpace());
      } // Must create a new one.

      return genomicAxisFacade;

   } // End method: getGenomicAxisFacade

   /**
    * Returns a new or cached contig facade.
    */
   public ContigFacade getContigFacade() {

      if (contigFacade == null) {
         contigFacade = new XmlContigFacade();
         ((XmlContigFacade)contigFacade).setGenomeVersionSpace(getGenomeVersionSpace());
      } // Must create a new one.

      return contigFacade;
   } // End method: getContigFacade

   /**
    *  no longer in FacadeManager interface, but needed for now in XmlFileOpenHandler
    */
   public FeatureFacade getFeatureFacade() {
      if (featureFacade == null) {
         featureFacade = new XmlFeatureFacade();
         ((XmlFeatureFacade)featureFacade).setGenomeVersionSpace(getGenomeVersionSpace());
      } // Must create a new one.

      return featureFacade;
   } // End method: getFeatureFacade

   /**
    *  no longer in FacadeManager interface, but needed for now in XmlFileOpenHandler
    */
   private HSPFacade getHSPFacade() {
      if (hspFacade == null) {
         hspFacade = new XmlHSPFacade();
         ((XmlHSPFacade)hspFacade).setGenomeVersionSpace(getGenomeVersionSpace());
      } // Must create a new one.

      return hspFacade;
   } // End method: getHSPFacade

   private HitAlignmentDetailLoader getHitAlignmentDetailLoader() {
      if (hitAlignDetailLdr == null) {
         hitAlignDetailLdr = new XmlHitAlignmentDetailLoader();
         ((XmlHitAlignmentDetailLoader)hitAlignDetailLdr).setGenomeVersionSpace(getGenomeVersionSpace());
      } // Must create a new one.
      return hitAlignDetailLdr;
   }

   private AtalantaDetailFacade getAtalantaDetailFacade() {
      if (atalantaDetailFacade == null) {
         atalantaDetailFacade = new XmlAtalantaDetailFacade();
         ((XmlAtalantaDetailFacade)atalantaDetailFacade).setGenomeVersionSpace(getGenomeVersionSpace());
      }
      return (atalantaDetailFacade);
   }

   /** Return new or cached controlled vocab service. */
   public ControlledVocabService getControlledVocabService() throws Exception {
      if (controlledVocabularyService == null)
         controlledVocabularyService = new XmlControlledVocabService();

      return controlledVocabularyService;
   } // End method: getControlledVocabService

} // End class: XmlFacadeManager
