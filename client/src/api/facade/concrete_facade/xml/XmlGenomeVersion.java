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
 * Title:        Genome Browser<p>
 * Description:  XML Facade to source genome version<p>
 * Company:      []<p>
 * @author Peter Davies
 * @version $Id$
 */
package api.facade.concrete_facade.xml;

import api.entity_model.access.report.GenomeVersionAlignmentReport;
import api.entity_model.access.report.PropertyReport;
import api.entity_model.access.report.SubjectSequenceReport;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.annotation.HitAlignmentDetailFeature;
import api.entity_model.model.annotation.HitAlignmentFeature;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.genetics.Chromosome;
import api.entity_model.model.genetics.GenomeVersion;
import api.entity_model.model.genetics.Species;
import api.facade.abstract_facade.annotations.HitAlignmentDetailLoader;
import api.facade.abstract_facade.annotations.HitAlignmentFacade;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.facade.abstract_facade.fundtype.NavigationConstants;
import api.facade.abstract_facade.genetics.GenomeVersionLoader;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.*;
import api.stub.geometry.Range;

import java.util.*;


/**
 * A genome version with all its bound data.
 */
public class XmlGenomeVersion  extends XmlGenomicFacade implements GenomeVersionLoader {

   private String externalPrefix = null;

   public XmlGenomeVersion() {
      // Make an OID in the internal namespace, and then find its namepsace prefix.
      externalPrefix =
      ReservedNameSpaceMapping.translateFromReservedNameSpace(new OID(OID.INTERNAL_DATABASE_NAMESPACE, "1",
                                                                      OID.NULL_GENOME_VER_IDENTIFIER).getNameSpaceAsString());
   }


   /**
    * Provides the list of all chromosomes known to the genome version provided.
    */
   public Chromosome[] listChromosomes( long genomeVersion ) throws NoData {

      return (new Chromosome[0]);

   } // End method: listChromosomes

   /**
    * Builds a report of various data relating to the alignment of a specific OID
    */
   public GenomeVersionAlignmentReport generateAlignmentReportForEntity(OID entityOID) {
      try {

         // Build overall report object.
         GenomeVersionAlignmentReport report = new GenomeVersionAlignmentReport();

         // Declare an item.
         GenomeVersionAlignmentReport.AlignmentReportLineItem currentItem = null;

         // First locate the bizobj.
         XmlLoader nextLoader = null;
         GeometricAlignment alignment = null;
         OID axisOid = null;
         Range rangeOnAxis = null;
         String axisName = null;
         String axisEntityName = null;
         GenomicProperty accessionProperty = null;
         GenomicProperty altAccessionProperty = null;
         GenomicProperty descriptionProperty = null;
         int entityLength = -1;
         GenomicEntity featureEntity = null;

         String descriptionValue = null;
         String altAccessionValue = null;
         String accessionValue = null;

         for ( Iterator it = getXmlLoadersForFeature(entityOID); it.hasNext(); ) {
            nextLoader = (XmlLoader)it.next();
            alignment = (GeometricAlignment)nextLoader.getAlignmentForFeature(entityOID);
            if ( alignment != null ) {
               featureEntity = alignment.getEntity();
               if ( featureEntity == null )
                  continue;

               // Get required props.
               descriptionProperty = null;
               altAccessionProperty = null;
               accessionProperty = null;

               if ( featureEntity instanceof HitAlignmentDetailFeature ) {
                  descriptionProperty = featureEntity.getProperty(HitAlignmentDetailLoader.DESCRIPTION_PROP);
                  if ( descriptionProperty == null )
                     descriptionProperty = ((HitAlignmentDetailFeature)featureEntity).getSuperFeature().getProperty(HitAlignmentFacade.DESCRIPTION_PROP);

                  accessionProperty = featureEntity.getProperty(HitAlignmentDetailLoader.ACCESSSION_NUM_PROP);
                  if ( accessionProperty == null )
                     accessionProperty = ((HitAlignmentDetailFeature)featureEntity).getSuperFeature().getProperty(HitAlignmentFacade.ACCESSSION_NUM_PROP);

                  altAccessionProperty = featureEntity.getProperty(HitAlignmentDetailLoader.ALT_ACCESSION_PROP);
                  if ( altAccessionProperty == null )
                     altAccessionProperty = ((HitAlignmentDetailFeature)featureEntity).getSuperFeature().getProperty(HitAlignmentFacade.ALT_ACCESSION_PROP);

               } // Detail
               else if ( featureEntity instanceof HitAlignmentFeature ) {
                  descriptionProperty = featureEntity.getProperty(HitAlignmentFacade.DESCRIPTION_PROP);
                  accessionProperty = featureEntity.getProperty(HitAlignmentFacade.ACCESSSION_NUM_PROP);
                  altAccessionProperty = featureEntity.getProperty(HitAlignmentFacade.ALT_ACCESSION_PROP);
               } // Hit.
               else
                  System.out.println("Got feature of type "+featureEntity.getClass().getName());

               if ( descriptionProperty == null )
                  descriptionValue = "";
               else
                  descriptionValue = descriptionProperty.getInitialValue();

               if ( accessionProperty == null )
                  accessionValue = "";
               else
                  accessionValue = accessionProperty.getInitialValue();

               if ( altAccessionProperty == null )
                  altAccessionValue = "";
               else
                  altAccessionValue = altAccessionProperty.getInitialValue();

               axisOid = nextLoader.getAxisOidOfAlignment(entityOID);
               axisName = axisOid.toString();
               axisEntityName = "Genomic Axis"; // NOTE: must get this from??? The factory?

               rangeOnAxis = alignment.getRangeOnAxis();
               entityLength = rangeOnAxis.getMagnitude();
               currentItem = new GenomeVersionAlignmentReport.AlignmentReportLineItem( axisOid,
                                                                                       axisName,
                                                                                       axisEntityName,
                                                                                       entityOID,
                                                                                       featureEntity.getEntityType().toString(),
                                                                                       rangeOnAxis,
                                                                                       accessionValue,
                                                                                       altAccessionValue,
                                                                                       descriptionValue,
                                                                                       entityLength
                                                                                     );

               report.addLineItem( currentItem );
            } // Got an alignment.
         } // For all loaders.

         return (report);

      } // End try block.
      catch ( Exception ex ) {
         return( null );
      } // End catch block to avoid "bleeding" exceptions.
   } // End method

   /**
    * Builds a feature property report, containing all information on all OIDs given.
    */
   public PropertyReport generatePropertyReport( OID genomeVerOID, OID [] featureOIDs, String [] propNames) {

      PropertyReport report = new PropertyReport();

      GenomicProperty[] properties = EMPTY_GENOMIC_PROPERTY_ARRAY;
      GenomicProperty[] finalProperties = EMPTY_GENOMIC_PROPERTY_ARRAY;

      List tempList = new ArrayList();
      Map nameMap = new HashMap();
      for ( int i = 0; i < propNames.length; i++ ) {
         nameMap.put(propNames[i], propNames[i]);
      } // For all property names.

      for ( int i = 0; i < featureOIDs.length; i++ ) {
         tempList.clear();
         properties = getAllPropsFor(featureOIDs[i]);
         for ( int j = 0; j < properties.length; j++ ) {
            if ( nameMap.containsKey(properties[j].getName()) ) {
               if ( ! properties[j].getName().equals("id") )
                  tempList.add(properties[j]);
            } // Keeper
         } // For all props returned.

         finalProperties = new GenomicProperty[tempList.size()];
         tempList.toArray(finalProperties);

         // Must only add something if properties were found for it.
         // Otherwise, may obscure real data from another facade manager.
         if ( finalProperties.length > 0 ) {
            PropertyReport.ReportLineItem item = new PropertyReport.ReportLineItem(featureOIDs[i]);
            for ( int j = 0; j < finalProperties.length; j++ ) {

               if ( (finalProperties[j] != null) && (finalProperties[j].getInitialValue() != null) )
                  item.addProperty(finalProperties[j].getName(), finalProperties[j].getInitialValue());
            } // For all found props.

            report.addLineItem(item);
         } // Something to add.

      } // For all oids.

      return (report);
   } // End method: generatePropertyReport

   /** Properties getter overridden to apply 'heuristic bail' */
   public GenomicProperty[] getProperties(OID genomicOID, EntityType dynamicType, boolean deepload) {

      // Test: did the genome version OID originate from
      // this facade instance's facade manager?
      int genomeVersionIdOfOid = genomicOID.getGenomeVersionId();
      if ( getSourcedGenomeVersionOidForId(genomeVersionIdOfOid) == null )
         return (EMPTY_GENOMIC_PROPERTY_ARRAY);
      else
         return (super.generatePropertiesForOID(genomicOID));
   } // End method: getProperties

   /**
    * Creates report on features all sharing the sequence alignment of the
    * entity given.
    */
   public SubjectSequenceReport generateSubjectSequenceReportForEntity( OID entityOID ) {
      // Build subj seq rpt to fill with relevant lines.
      SubjectSequenceReport report = new SubjectSequenceReport();

      // Get data on feature.
      ModelMgr mgr = ModelMgr.getModelMgr();
      GenomeVersion gv = mgr.getGenomeVersionById(entityOID.getGenomeVersionId());
      String subjectSeqId = null;
      if ( gv != null ) {
         //System.out.println("Seeing report request for entity of type "+gv.getGenomicEntityForOid(entityOID).getClass().getName());
         if ( ! (gv.getGenomicEntityForOid(entityOID) instanceof Feature) )
            return (report);

         Feature indexEntity = (Feature)gv.getGenomicEntityForOid(entityOID);
         if ( indexEntity != null ) {
            GenomicProperty prop = indexEntity.getProperty(HitAlignmentFacade.SUBJECT_SEQ_ID_PROP);
            if ( prop != null )
               subjectSeqId = prop.getInitialValue();
         } // Found in model.
      } // Got the GV

      // Any found?  May not be, if the entity whose oid was given did not
      // have a subject seq defined.
      if ( subjectSeqId == null )
         return (report);

      // Use sequence oid found above, to find all OTHER features which share one or more of them.
      //
      XmlLoader nextLoader = null;
      for ( Iterator it = getFeatureSourcesInGenomeVersion(entityOID.getGenomeVersionId()); it.hasNext(); ) {
         nextLoader = (XmlLoader)it.next();
         nextLoader.addToSubjSeqRpt(subjectSeqId, gv.getOid(), report);
      } // For all loaders in the genome version of the index entity.

      // Return the completed report.
      return (report);
   } // End method


   public String getNavigationVocabIndex()
   {
      return (NavigationConstants.NAVIGATION_VOCAB_INDEX);
   }

   /**
    * Returns the set of paths which can be used to retrieve/focus on
    * locations matching the criteria given.
    *
    * @param OID speciesOID the OID of the species (containing genome version id)
    *    against which all searching is to be done.
    * @param String targetType the type of object for which to find locator.
    * @param String target the value to match on the type of lookup given.
    */
   public NavigationPath[] getNavigationPath
   (OID speciesOID,
    String targetType,
    String target)
   throws InvalidPropertyFormat {

      // Test: could the genome version have any features in
      // this facade?  See if iterator is empty.
      int genomeVersionIdOfOid = speciesOID.getGenomeVersionId();
      if ( ! isGenomeVersionAvailableToSearch(genomeVersionIdOfOid) )
         return (new NavigationPath[0]);

      NavigationPath[] returnPath = new NavigationPath[0];

      // Must decode the input.
      int targetTypeNumber = NavigationConstants.getNumberFromShortName(targetType);

      if ( (targetTypeNumber == NavigationConstants.STS_NAME_INDEX) ||
           (targetTypeNumber == NavigationConstants.HIT_ALIGN_ACCESSION_INDEX) ||
           (targetTypeNumber == NavigationConstants.POLY_ACCESSION_INDEX) ||
           (targetTypeNumber == NavigationConstants.SUBSEQ_OID_INDEX) ||
           (targetTypeNumber == NavigationConstants.REG_REGION_ACCESSION_INDEX) ||
           (targetTypeNumber == NavigationConstants.GENE_ONTOLOGY_NAME_INDEX) ) {

         List hitList = searchDescriptions(target, speciesOID.getGenomeVersionId());

         // Empty return data->no data.
         if ( hitList.size() == 0 ) {

            return (new NavigationPath[0]);

         } // Nothing to report.
         else {

            int i = 0;
            returnPath = new NavigationPath[hitList.size()];
            NavigationNode[] nodeArray = null;
            String narrowestDescription = null;
            List nodeList = null;
            for ( Iterator it = hitList.iterator(); it.hasNext(); ) {
               nodeList = (List)it.next();
               nodeArray = new NavigationNode[nodeList.size()];

               nodeList.toArray(nodeArray);
               narrowestDescription = nodeArray[nodeArray.length - 1].getDisplayname();
               returnPath[i++] = new NavigationPath(narrowestDescription, nodeArray);

            } // For all hits found.

         } // Found something.

      } // Looking for one of a number of types, all treated the same.
      else {
         // Check for contig and above.
         List nodeList = new ArrayList();
         if ( targetTypeNumber == NavigationConstants.UNKNOWN_OID_INDEX ) {
            try {
               nodeList = searchFeature(targetTypeNumber, target, speciesOID.getGenomeVersionId());
               if ( nodeList.size() > 0 ) {
                  ensureGenomeVersionIsSelected(speciesOID.getGenomeVersionId());
               } // Found something.
            }
            catch ( Exception ex ) {
            }

            if ( nodeList.size() == 0 )
               nodeList = searchContig(target, speciesOID.getGenomeVersionId());
            if ( nodeList.size() == 0 )
               nodeList = searchAboveFeature(targetTypeNumber, target, speciesOID.getGenomeVersionId());
         } // Requires universal search of OID.
         else if ( targetTypeNumber == NavigationConstants.CONTIG_OID_INDEX )
            nodeList = searchContig(target, speciesOID.getGenomeVersionId());
         else if ( ( targetTypeNumber == NavigationConstants.SPECIES_OID_INDEX )       ||
                   ( targetTypeNumber == NavigationConstants.CHROMOSOME_NAME_INDEX )   ||
                   ( targetTypeNumber == NavigationConstants.GENOMIC_AXIS_NAME_INDEX ) )
            nodeList = searchAboveFeature(targetTypeNumber, target, speciesOID.getGenomeVersionId());
         else
            nodeList = searchFeature(targetTypeNumber, target, speciesOID.getGenomeVersionId());

         // Empty return data->no data.
         if ( nodeList.size() == 0 )
            return (new NavigationPath[0]);

         returnPath = new NavigationPath[1];
         NavigationNode[] nodeArray = new NavigationNode[nodeList.size()];

         nodeList.toArray(nodeArray);

         String narrowestDescription = nodeArray[nodeArray.length - 1].getDisplayname();
         returnPath[0] = new NavigationPath(narrowestDescription, nodeArray);
      } // Guaranteed to be only a single navigation path.

      return (returnPath);
   } // End method: getNavigationPath

   /**
    * Iterates over all known genome versions, carrying out the required
    * search, via the overloaded method.
    *
    * @param String targetType the type of object for which to find locator.
    * @param String target the value to match on the type of lookup given.
    */
   public NavigationPath[] getNavigationPath
   (String targetType,
    String target)
   throws InvalidPropertyFormat {

      // Testing for state.
      if ( ModelMgr.getModelMgr() == null )
         throw new IllegalStateException("No model manager established");

      // Iterating over genome versions known to this facade.
      Set allGenomeVersions = ModelMgr.getModelMgr().getGenomeVersions();
      if ( allGenomeVersions == null )
         throw new IllegalStateException("No genome version set");

      // Iterating over all genome versions.
      GenomeVersion genomeVersion = null;
      NavigationPath[] tempArray = null;
      List navigationList = new ArrayList();
      for ( Iterator it = allGenomeVersions.iterator(); it.hasNext(); ) {
         genomeVersion = (GenomeVersion)it.next();
         if ( genomeVersion.getSpecies() != null ) {
            tempArray = getNavigationPath(genomeVersion.getSpecies().getOid(), targetType, target);
            if ( tempArray != null ) {
               for ( int i = 0; i < tempArray.length; i++ ) {
                  navigationList.add(tempArray[i]);
               } // For all results
            } // Non-null return
         } // Species contained.
      } // For all iterations

      NavigationPath[] returnArray = new NavigationPath[navigationList.size()];
      navigationList.toArray(returnArray);

      return (returnArray);

   } // End method


   /**
    * Carry out searches down to axis.  Note that any such search only needs to
    * query all loaders for specific data.  Different types of searches may have
    * to aggregate a path by putting together different parts from different loaders.
    */
   private List searchAboveFeature(int targetTypeNumber, String target,
                                   int genomeVersionID) throws InvalidPropertyFormat {
      List nodeList = new ArrayList();
      XmlLoader loader = null;
      Species species = null;
      Chromosome chromosome = null;

      // Go through all loaders, searching for necessary info.
      for ( Iterator it = getFeatureSourcesInGenomeVersion(genomeVersionID); it.hasNext(); ) {

         loader = (XmlLoader)it.next();
         if ( species == null )
            species = getSpeciesForGenomeVersion(genomeVersionID);
         chromosome = getChromosomeForSpecies(loader, species);

         if ( ( targetTypeNumber == NavigationConstants.SPECIES_OID_INDEX ) ||
              ( targetTypeNumber == NavigationConstants.UNKNOWN_OID_INDEX ) ) {

            if ( null != species ) {

               // Test: if user wanted species, this species must match.
               if ( target.equalsIgnoreCase(species.getDisplayName()) ||
                    convertToOID(target).equalsIgnoreCase(species.getOid().toString()) ) {
                  addGenomeVersionNode(nodeList, genomeVersionID);
                  addSpeciesNode(nodeList, species);
                  break;
               } // Found matching species.

            } // Found a species.
         } // Requested species node.

         if ( ( targetTypeNumber == NavigationConstants.CHROMOSOME_NAME_INDEX ) ||
              ( targetTypeNumber == NavigationConstants.UNKNOWN_OID_INDEX ) ) {

            if ( loader.getGenomicAxisAlignment() != null ) {
               if ( target.equalsIgnoreCase(chromosome.getDisplayName()) ||
                    convertToOID(target).equalsIgnoreCase(chromosome.getOid().toString()) ) {
                  addGenomeVersionNode(nodeList, genomeVersionID);
                  addSpeciesNode(nodeList, species);
                  addChromosomeNode(nodeList, chromosome);
                  break;
               } // Found a chromosome.
            } // Chromosome will exist where genomic axis exists.
         } // Requested chromosome node.

         if ( ( targetTypeNumber == NavigationConstants.GENOMIC_AXIS_NAME_INDEX ) ||
              ( targetTypeNumber == NavigationConstants.UNKNOWN_OID_INDEX ) ) {
            // Look for a genomic axis.
            // System.out.println("Getting genomic axis");
            if ( loader.getGenomicAxisAlignment() != null ) {
               GenomicAxis genomicAxis = (GenomicAxis)loader.getGenomicAxisAlignment().getEntity();
               // System.out.println("Looking at genomic axis "+genomicAxis);
               // System.out.println("Display name of axis entity is "+genomicAxis.getDisplayName());
               // System.out.println("Target is "+target);
               // System.out.println("String version of genaxis oid is "+genomicAxis.getOid().toString());
               if ( (genomicAxis != null) &&
                    ( genomicAxis.getDisplayName().equalsIgnoreCase(target) ||
                      genomicAxis.getOid().toString().equalsIgnoreCase(convertToOID(target)) ) ) {

                  // Build the list.
                  addGenomeVersionNode(nodeList, genomeVersionID);
                  addSpeciesNode(nodeList, species);
                  addChromosomeNode(nodeList, chromosome);
                  addGenomicAxisNode(nodeList, loader);

               } // Found target axis.
            } // Got a genomic axis
         } // Requested genomic axis.

      } // For all loaders

      return (nodeList);

   } // End method: searchAboveFeature.

   /**
    * Searches a contig, which is a feature, but is represented differently
    * in the loader than other features are.
    */
   private List searchContig(String target, int genomeVersionID) throws InvalidPropertyFormat {
      XmlLoader loader = null;
      List nodeList = new ArrayList();
      for ( Iterator it = getSearchSourcesInGenomeVersion(genomeVersionID); it.hasNext(); ) {
         loader = (XmlLoader)it.next();

         try {
            // Look for a contig.
            if ( (loader.getContigAlignment() != null) &&
                 (
                 convertToOID(target).equalsIgnoreCase(loader.getContigAlignment().getEntity().getOid().toString()) ||
                 target.equalsIgnoreCase(loader.getContigAlignment().getEntity().getDisplayName())
                 )
               ) {

               addPathAboveFeature(nodeList, loader, genomeVersionID);

               // If no such path exists, it is because the loader did not "know"
               // about any genomic axis.
               if ( nodeList.size() == 0 )
                  FacadeManager.handleException(new IllegalArgumentException("Attempt to return contig not associated with a root path"));

               addContigNode(nodeList, loader);
            } // Got a contig.

         }
         catch ( Exception ex ) {
            // No contig node will be added.
            FacadeManager.handleException(ex);
         } // End catch block.
      } // For all loaders.

      return (nodeList);

   } // End method: searchContig

   /**
    * Method build a path for features, found in loaders.
    */
   private List searchFeature(int targetTypeNumber, String target,
                              int genomeVersionID) throws InvalidPropertyFormat {

      List featureList = new ArrayList();

      int returnType = 0;

      XmlLoader loader = null;
      for ( Iterator it = getSearchSourcesInGenomeVersion(genomeVersionID); it.hasNext(); ) {
         loader = (XmlLoader)it.next();

         Alignment alignment = null;
         if ( targetTypeNumber == NavigationConstants.GENE_ACCESSION_INDEX ) {
            alignment = loader.getAlignmentForAccession(target, XmlLoader.GENE_ACCESSION_TYPE);
         } // Looking for the accession of gene
         else if ( targetTypeNumber == NavigationConstants.TRANSCRIPT_ACCESSION_INDEX ) {
            alignment = loader.getAlignmentForAccession(target, XmlLoader.NONPUBLIC_ACCESSION_TYPE);
         } // Looking for the accession of transcript
         else if ( targetTypeNumber == NavigationConstants.FEATURE_OID_INDEX ) {
            OID featureOID = ((SAXLoaderBase)loader).parseOIDGeneric(convertToOID(target));
            alignment = loader.getAlignmentForFeature(featureOID);
         } // Looking for feature OID.
         else if ( targetTypeNumber == NavigationConstants.UNKNOWN_OID_INDEX ) {
            OID featureOID = ((SAXLoaderBase)loader).parseOIDGeneric(convertToOID(target));
            alignment = loader.getAlignmentForFeature(featureOID);
         } // Looking for unknown feature's ID.

         GeometricAlignment geometricAlignment = (GeometricAlignment)alignment;
         if ( geometricAlignment != null ) {

            // Also need to piece together the preceding path.
            addPathAboveFeature(featureList, loader, genomeVersionID);

            // If no such path exists, it is because the loader did not "know"
            // about any genomic axis.
            OID oidOfAlignmentAxis = loader.getAxisOidOfAlignment(geometricAlignment.getEntity().getOid());
            if ( featureList.size() == 0 )
               featureList.add( new NavigationNode( oidOfAlignmentAxis, NavigationConstants.GENOMIC_AXIS_NAME_INDEX, "", new Range( 0, 0 ) ) );

            returnType = loader.isCurated(alignment.getEntity().getOid()) ?
                         NavigationNode.CURATED : NavigationNode.PRECOMPUTE_HIGH_PRI;

            featureList.add(
                           new NavigationNode(
                                             geometricAlignment.getEntity().getOid(),
                                             returnType,
                                             geometricAlignment.getEntity().getDisplayName(),
                                             geometricAlignment.getRangeOnAxis()
                                             )
                           );

            break; // End iterations.

         } // Got alignment

      } // For all iterations
      return (featureList);
   } // End method: searchFeature

   /**
    * Builds paths for features, whose descriptions happen to contain the target
    * string.
    */
   private List searchDescriptions(  String target,
                                     int genomeVersionID) throws InvalidPropertyFormat {

      List featureList = new ArrayList();

      int returnType = 0;

      XmlLoader loader = null;
      List alignmentList = null;
      GeometricAlignment geometricAlignment = null;
      List nodeList = new ArrayList();

      for ( Iterator it = getSearchSourcesInGenomeVersion(genomeVersionID); it.hasNext(); ) {
         loader = (XmlLoader)it.next();

         alignmentList = loader.getAlignmentsForDescriptionsWith(target);

         for ( Iterator alignmentIterator = alignmentList.iterator(); alignmentIterator.hasNext(); ) {
            geometricAlignment = (GeometricAlignment)alignmentIterator.next();

            // Also need to piece together the preceding path.
            addPathAboveFeature(nodeList, loader, genomeVersionID);

            // If no such path exists, it is because the loader did not "know"
            // about any genomic axis.
            OID oidOfAlignmentAxis = loader.getAxisOidOfAlignment(geometricAlignment.getEntity().getOid());
            if ( nodeList.size() == 0 )
               nodeList.add(  new NavigationNode(oidOfAlignmentAxis, NavigationConstants.GENOMIC_AXIS_NAME_INDEX, "", new Range( 0, 0 ) ) );

            returnType =  loader.isCurated(geometricAlignment.getEntity().getOid()) ?
                          NavigationNode.CURATED : NavigationNode.PRECOMPUTE_HIGH_PRI;

            nodeList.add(
                        new NavigationNode(
                                          geometricAlignment.getEntity().getOid(),
                                          returnType,
                                          geometricAlignment.getEntity().getDisplayName(),
                                          geometricAlignment.getRangeOnAxis()
                                          )
                        );

            featureList.add(new ArrayList(nodeList));
            nodeList.clear();
         } // Got alignment

      } // For all iterations
      return (featureList);
   } // End method: searchDescriptions

   /**
    * Look for chromo associated with the species, in the loader.
    */
   private Chromosome getChromosomeForSpecies(XmlLoader loader, Species species) {
      Chromosome returnChromosome = null;
      if ( loader instanceof GenomicAxisXmlLoader ) {
         Alignment alignment = ((GenomicAxisXmlLoader)loader).getAlignmentForChromosome(species);
         if ( alignment != null )
            returnChromosome = (Chromosome)alignment.getEntity();
      } // Right type of loader
      return (returnChromosome);

   } // End method

   /**
    * Lookup the species from among the selected genome versions.
    */
   private Species getSpeciesForGenomeVersion(int genomeVersionId) {
      Set genomeVersions = ModelMgr.getModelMgr().getGenomeVersions();
      GenomeVersion nextVersion = null;
      Species returnSpecies = null;
      for ( Iterator it = genomeVersions.iterator(); (returnSpecies == null) && it.hasNext(); ) {
         nextVersion = (GenomeVersion)it.next();
         if ( nextVersion.getGenomeVersionInfo().getGenomeVersionId() == genomeVersionId )
            returnSpecies = nextVersion.getSpecies();
      } // For all gv's active.

      return (returnSpecies);
   } // End method

   /**
    * Given an existing loader that contains the "hit", make a
    * path down to the last point (genomic axis) above where
    * features begin.
    */
   private void addPathAboveFeature(List featureList, XmlLoader loader,
                                    int genomeVersionID) {

      // First attempt to find a path above the feature that was found
      // in the loader.  This would include all nodes down to a genomic
      // axis.
      Alignment axisAlignment = loader.getGenomicAxisAlignment();
      if ( axisAlignment != null ) {

         try {
            featureList.addAll( searchAboveFeature( NavigationConstants.GENOMIC_AXIS_NAME_INDEX, axisAlignment.getEntity().getDisplayName(), genomeVersionID ) );
         }
         catch ( InvalidPropertyFormat ex ) {
         }

      } // Found a genAx in the loader.

   } // End method: addPathAboveFeature

   /**
    * The following methods are convenience--to add nodes of various types to
    * the list.
    */
   private void addGenomeVersionNode(List nodeList, int genomeVersionID) throws InvalidPropertyFormat {
      // Only add the node IF the gv OID was sourced here in XML
      OID gvOid = getSourcedGenomeVersionOidForId(genomeVersionID);
      if ( gvOid != null )
         nodeList.add(new NavigationNode(gvOid, NavigationNode.GENOME, "Genome Version", new Range(0,0)));
   } // End method

   private void addSpeciesNode(List nodeList, Species species) throws InvalidPropertyFormat {
      // Cover case where no species is listed.
      if ( species == null )
         throw new InvalidPropertyFormat();

      nodeList.add(new NavigationNode(species.getOid(), NavigationNode.SPECIES, species.getDisplayName(), new Range(0, 0)));
   } // End method: addSpeciesNode

   private void addChromosomeNode(List nodeList, Chromosome chromosome) throws InvalidPropertyFormat {
      nodeList.add(new NavigationNode(chromosome.getOid(), NavigationNode.CHROMOSOME, chromosome.getDisplayName(), new Range(0, 0)));
   } // End method: addChromosomeNode

   private void addGenomicAxisNode(List nodeList, XmlLoader loader) throws InvalidPropertyFormat {
      GenomicAxis genomicAxis = (GenomicAxis)loader.getGenomicAxisAlignment().getEntity();
      if ( genomicAxis == null )
         throw new InvalidPropertyFormat();
      else
         nodeList.add(new NavigationNode(genomicAxis.getOid(), NavigationNode.AXIS, genomicAxis.getDisplayName(), new Range(0,0)));
   } // End method: addGenomicAxisNode

   private void addContigNode(List nodeList, XmlLoader loader) throws InvalidPropertyFormat {
      if ( loader.getContigAlignment().getEntity() == null )
         throw new InvalidPropertyFormat();

      OID contigOID = loader.getContigAlignment().getEntity().getOid();
      if ( contigOID == null ) {
         throw new InvalidPropertyFormat();
      } // No contig
      else {
         nodeList.add(new NavigationNode(contigOID, NavigationNode.CONTIG, contigOID.toString(), new Range(0, 0)));
      } // Got it
   } // End method: addContigNode

   /**
    * Helper to convert a target search string into an OID string--with required
    * prefix change.
    */
   private String convertToOID(String searchString) {

      if ( Character.isDigit(searchString.charAt(0)) ) {
         return (externalPrefix + ":" + searchString);
      } // add a prefix
      else {
         return (searchString);
      } // Check the prefix.
   } // End method: covnertToOID

   /**
    * It may be that this facade is called to navigate to a bookmark and
    * the genome version of origin has not yet been opened.  If not,
    * this method opens it.
    */
   private void ensureGenomeVersionIsSelected(int genomeVersionId) {

      boolean genomeVersionIsSelected = false;

      // Testing for state.
      if ( ModelMgr.getModelMgr() == null )
         throw new IllegalStateException("No model manager established");

      Set selectedGenomeVersions = ModelMgr.getModelMgr().getSelectedGenomeVersions();
      if ( selectedGenomeVersions == null )
         throw new IllegalStateException("No genome versions active");

      // Iterating over all genome versions.
      GenomeVersion genomeVersion = null;
      for ( Iterator it = selectedGenomeVersions.iterator(); (! genomeVersionIsSelected) && it.hasNext(); ) {
         genomeVersion = (GenomeVersion)it.next();
         if ( genomeVersion.getGenomeVersionInfo().getGenomeVersionId() == genomeVersionId ) {
            genomeVersionIsSelected = true;
         } // Found it
      } // For all selected versions.

      if ( ! genomeVersionIsSelected ) {
         // Check: GV came from a file source?  If so, okay to select it.
         GenomeVersion localGenomeVersion = ModelMgr.getModelMgr().getGenomeVersionById(genomeVersionId);
         if ( localGenomeVersion.getGenomeVersionInfo().getDataSourceType() == GenomeVersionInfo.FILE_DATA_SOURCE )
            ModelMgr.getModelMgr().addSelectedGenomeVersion(localGenomeVersion);
      } // Not "fired up"

   } // End method

   /**
    * Helper to pull props from all XML loaders known to this
    * facade, for the given OID.  This includes props from
    * its parent feature.
    */
   private GenomicProperty[] getAllPropsFor(OID featureOID) {
      GenomicProperty[] properties = EMPTY_GENOMIC_PROPERTY_ARRAY;
      Set propertySet = null;
      Feature feature = null;
      XmlLoader nextLoader = null;
      Map propMap = new HashMap();
      try {
         // Pull properties from all loaders.
         Iterator featureLoaders = getXmlLoadersForFeature(featureOID);
         if ( featureLoaders.hasNext() ) {

            while ( featureLoaders.hasNext() ) {
               nextLoader = (XmlLoader)featureLoaders.next();
               feature = (Feature)nextLoader.getAlignmentForFeature(featureOID).getEntity();
               propertySet = feature.getProperties();
               propertySet.addAll(addParentProps((HitAlignmentDetailFeature)feature));
               for ( Iterator it = propertySet.iterator(); it.hasNext(); ) {
                  GenomicProperty nextProp = (GenomicProperty)it.next();
                  if ( ! propMap.containsKey(nextProp.getName()) )
                     propMap.put(nextProp.getName(), nextProp);
               } // For all props in current set.

            } // For all loaders with info on feature.

         } // Must query loaders.

         properties = new GenomicProperty[propMap.size()];
         propMap.values().toArray(properties);
      }
      catch ( Exception ex ) {
      } // End catch block for props.

      return (properties);

   } // End method


   /**
    * Some properties should and can be obtained from the parent feature.
    */
   private Set addParentProps(HitAlignmentDetailFeature detail) {
      HitAlignmentFeature superFeature = (HitAlignmentFeature)detail.getSuperFeature();
      if ( superFeature == null )
         return (Collections.EMPTY_SET);

      Set returnSet = new HashSet();
      addParentProp(HitAlignmentFacade.DESCRIPTION_PROP, returnSet, superFeature);
      addParentProp(HitAlignmentFacade.ACCESSSION_NUM_PROP, returnSet, superFeature);
      addParentProp(HitAlignmentFacade.ALT_ACCESSION_PROP, returnSet, superFeature);
      addParentProp(HitAlignmentFacade.SUBJECT_SEQ_LENGTH_PROP, returnSet, superFeature);

      if ( superFeature.getEntityType().value() == EntityTypeConstants.Sim4_Hit ) {

         addParentProp(HitAlignmentFacade.PERCENT_HIT_IDENTITY_PROP, returnSet, superFeature);
         addParentProp(HitAlignmentFacade.PERCENT_LENGTH_PROP, returnSet, superFeature);

      } // Sim4 or Genewise

      return (returnSet);

   } // End method

   /** Add just one prop to the set, if it exists in the feature. */
   private void addParentProp(String propName, Set propSet, HitAlignmentFeature parent) {
      GenomicProperty property = parent.getProperty(propName);
      if ( property != null )
         propSet.add(property);
   } // End method

} // End class: XmlGenomeVersion

