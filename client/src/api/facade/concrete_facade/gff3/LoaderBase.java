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
 * Title:        Genome Browser primary SAX handler<p>
 * Description:  Primary handler, which also acts as a base for file-type-specific
 *               loaders.<p>
 * Company:      []<p>
 * @author Les Foster
 * @version  CVS_ID:  $Id$
 */
package api.facade.concrete_facade.gff3;

import api.entity_model.access.report.SubjectSequenceReport;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.assembly.Contig;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.genetics.Species;
import api.facade.concrete_facade.shared.LoaderConstants;
import api.facade.concrete_facade.shared.OIDParser;
import api.facade.concrete_facade.shared.PropertySource;
import api.facade.concrete_facade.shared.feature_bean.CompoundFeatureBean;
import api.facade.concrete_facade.shared.feature_bean.FeatureBean;
import api.facade.concrete_facade.shared.feature_bean.GeneFeatureBean;
import api.facade.concrete_facade.shared.feature_bean.SimpleFeatureBean;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.GenomicEntityComment;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;
import api.stub.data.ReservedNameSpaceMapping;
import api.stub.geometry.Range;
import api.stub.sequence.Sequence;
import api.stub.sequence.SequenceBuilder;
import api.stub.sequence.SubSequence;
import shared.util.GANumericConverter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * This loader deals with opening files, and handling SAX events, which
 * it may delegate to other classes.  It also provides facilities to
 * its delegates as template methods to its subclasses.
 *
 * Note that subclasses must implement the OIDParser interface.
 */
public abstract class LoaderBase implements DataLoader, OIDParser {

	//------------------------------CONSTANTS
	private static final String DEFAULT_SPECIES = "Unknown Species";
	private static final String DEFAULT_GENOMIC_AXIS_DISPLAY_NAME = "Genomic Axis";

	//------------------------------MEMBER VARIABLES
	private List<String> mLoadedFileNames = new ArrayList<String>();

	private OID genomicAxisOid;

	private int axisLength = 0;

	// Species may be found in one of the tags.
	private Species species = null;
	private String assemblyVersion = null;  // For promotion code!

	private Alignment contigAlignment = null;
	private Alignment genomicAxisAlignment = null;

	// All residue base call letters kept here.
	private Sequence residues = null;
	private SequenceBuilder sequenceBuilder = null;

	private Gff3DataSource dataSource;

	// Flag to indicate whether the file was scanned for overview data.
	private boolean initialScanComplete = false;

	private SequenceAlignment sequenceAlignment = null;
	private GffIdentifierOidHandler gffIdOIDHandler = new GffIdentifierOidHandler();

	// Listeners list.  Listening for sequence alignment "events".
	private List<SequenceAlignmentListener> listeners = new ArrayList<SequenceAlignmentListener>();

	// This version ID must be set for all the OIDs produced by this loader.
	private int genomeVersionId = 0;

	//------------------------------CONSTRUCTORS
	/**
	 * Simplest constructor.
	 */
	public LoaderBase() {
	} // End constructor

	/**
	 * Use the passed OID for its genome version ID.
	 */
	public LoaderBase(OID lSpeciesOID) {
		genomeVersionId = lSpeciesOID.getGenomeVersionId();
	} // End constructor

	/**
	 * Set the genome version id directly.
	 */
	public LoaderBase(int lGenomeVersionId) {
		genomeVersionId = lGenomeVersionId;
	} // End constructor

	/**
	 * Pass in a sequence alignment to adjust alignments of features, etc.
	 */
	public LoaderBase(SequenceAlignment lSequenceAlignment, int lGenomeVersionId) {
		if (lSequenceAlignment == null)
			throw new IllegalArgumentException("Null sequence alignment object given");
		sequenceAlignment = lSequenceAlignment;
		genomeVersionId = lGenomeVersionId;
	} // End constructor

	//------------------------------IMPLEMENTATION OF DataLoader
	/**
	 * Adds the file whose name was given for later parsing.
	 */
	@Override
	public void loadGff3(String lFileName) {
		residues = null;

		// Register the file for later use.
		mLoadedFileNames.add(lFileName);

		return;
	} // End method: loadXml

	/** Never sourced from a stringbuffer. */
	@Override
	public boolean isLoadedFromStringBuffer() {
		return false;
	} // End method

	/** Returns all filenames known to have been loaded via this parser. */
	@Override
	public String[] getLoadedFileNames() {
		String[] returnArray = new String[mLoadedFileNames.size()];
		mLoadedFileNames.toArray(returnArray);
		return returnArray;
	} // End method: getLoadedFileNames

	/** Tells what part of seq over the requested range this loader can provide. */
	@Override
	public Range getIntersectingSequenceRange(Range requestedRange) {
		loadInitialIfNeeded();
		if (genomicAxisOid == null) {
			return null;
		} // Nothing to report.
		else {
			// Expect THIS kind of loader to always have whole range, if ANY range.
			Range coveredRange = new Range(0, axisLength);
			// System.out.println("Reporting a covered range of "+coveredRange+" from "+this.getClass().getName()+
			// ", and an intersection of "+requestedRange.intersection(requestedRange, coveredRange));
			return Range.intersection(requestedRange, coveredRange);
		} // Return covered area.
	} // End method: getIntersectingSequenceRange

	/**
	 * Returns DNA residues found in the loaded file.  Should be called
	 * only after calling getIntersectingSequenceRange to get the relevant range.
	 */
	@Override
	public Sequence getDNASequence(Range sequenceRange) {
		loadInitialIfNeeded();
		if (sequenceRange == null) {
			System.out.println("Got null seq range");
			return null;
		} // aberrant range given.

		if (sequenceBuilder != null) {
			try {
				return sequenceBuilder.getSubSequence(sequenceRange.getMinimum(), sequenceRange.getMagnitude());
			}
			catch (Exception ex) {
				FacadeManager.handleException(ex);
			}

			return null;
		}
		else if (residues != null) {
			return new SubSequence(residues, sequenceRange.getMinimum(), sequenceRange.getMagnitude());
		}
		else {
			return null;
		}

	} // End method: getDNASequence

	/** Returns the OID of any genomic axis represented by this file. */
	@Override
	public OID getGenomicAxisOID() {
		loadInitialIfNeeded();
		return genomicAxisOid;
	} // End method

	/**
	 * Returns the length of a sequence given.
	 */
	@Override
	public String getSequenceLength(OID sequenceOID) {
		loadFeaturesIfNeeded();
		return null; //TODO mFeatureHandler.getSubjectSequenceLength(sequenceOID);
	} // End method

	/**
	 * Returns the alignment to a genomic axis.
	 * @param OID chromosomeOID used to obtain the genome version id.
	 * @return Alignment the alignment to the genomic axis, (and implicitly, an axis is created).
	 */
	@Override
	public Alignment getGenomicAxisAlignment() {
		loadInitialIfNeeded();
		if (genomicAxisAlignment == null) {
			if (genomicAxisOid != null) {
				String genAxDisplayName = genomicAxisOid.isInternalDatabaseOID() ?
						GANumericConverter.getConverter().getGANameForOIDSuffix(genomicAxisOid.getIdentifierAsString())
						: DEFAULT_GENOMIC_AXIS_DISPLAY_NAME;
						GenomicAxis genomicAxis = new GenomicAxis(genomicAxisOid,genAxDisplayName,axisLength);
						genomicAxisAlignment = new Alignment(null, genomicAxis);
			} // Non-null axis OID: can build the alignment.b
		} // Must build new one.

		return genomicAxisAlignment;

	} // End method: getGenomicAxis

	/**
	 * Returns the alignment of the genomic entity representing the contig of
	 * interest to this loaded file.
	 */
	@Override
	public Alignment getContigAlignment() {
		loadInitialIfNeeded();
		return contigAlignment;
	} // End method: getContigAlignment

	/** Returns species representing file. */
	@Override
	public Species getSpecies() {
		loadInitialIfNeeded();
		return species;
	} // End method: getSpecies

	/** Returns accession string for the gene whose OID is given. */
	@Override
	public String getGeneacc(OID oid) {
		loadFeaturesIfNeeded();
		GeneFeatureBean model = (GeneFeatureBean)dataSource.getOrLoadBeanForOid(oid);
		if (model != null)
			return model.getAnnotationName();
		return null;
	} // End method: getGeneacc

	/** Returns the transcript accession name. */
	@Override
	public String getTrscptacc(OID oid) {
		loadFeaturesIfNeeded();
		CompoundFeatureBean model = (CompoundFeatureBean)dataSource.getOrLoadBeanForOid(oid);
		if (model != null)
			return model.getAnnotationName();
		return null;
	} // End metod: getTrscptacc

	/**
	 * Returns relative order of feature among its siblings, IF it has a parent
	 * feature.
	 */
	@Override
	public int getSiblingPosition(OID oid) {
		loadFeaturesIfNeeded();
		FeatureBean model = dataSource.getOrLoadBeanForOid(oid);
		FeatureBean nextChild = null;
		CompoundFeatureBean parentModel = (CompoundFeatureBean)model.getParent();

		// Decode the annotation type string into a feature type instance.
		EntityType entityType = FeatureBean.decodeEntityType(model.getAnalysisType());
		EntityType childEntityType = null;

		int returnValue = -1;
		int count = -1;
		if (parentModel != null) {
			// Copy the list for sorting, but leave original undisturbed.
			List children = new ArrayList(parentModel.getChildren());
			Collections.sort(children);
			if (children != null) {
				for (int i = 0; (returnValue == -1) && (i < children.size()); i++) {
					nextChild = (FeatureBean)children.get(i);
					if (nextChild == null)
						continue;

					// NOTE: only count children of same type as siblings.
					childEntityType = nextChild.decodeEntityType(nextChild.getAnalysisType());
					if (childEntityType == entityType)
						count++;

					if (nextChild.equals(model)) {
						returnValue = count;
					} // Got model.
				} // For all children.
			} // Has children?  Must if it is a parent.
		} // Has parent

		return returnValue;

	} // End method

	/** Returns output associated with string, for feature whose OID was given. */
	public String getOutput(OID oid, String outputName) {
		loadFeaturesIfNeeded();
		SimpleFeatureBean model = (SimpleFeatureBean)dataSource.getOrLoadBeanForOid(oid);
		if (model != null)
			return model.getOutput(outputName);
		return null;
	} // End method: getOutput

	/** Return either query or subject aligned residues for simple feature. */
	public String getQueryAlignedResidues(OID oid) {
		loadFeaturesIfNeeded();
		SimpleFeatureBean model = (SimpleFeatureBean)dataSource.getOrLoadBeanForOid(oid);
		if (model != null)
			return model.getQueryAlignment();
		return null;
	} // End method: getQueryAlignedResidues

	public String getSubjectAlignedResidues(OID oid) {
		loadFeaturesIfNeeded();
		SimpleFeatureBean model = (SimpleFeatureBean)dataSource.getOrLoadBeanForOid(oid);
		if (model != null)
			return model.getSubjectAlignment();
		return null;
	} // End method: getSubjectAlignedResidues

	/**
	 * Returns subject sequence of the subject sequence OID given.  Repeat:
	 * this is for the subject sequence, not for a feature.
	 * @See getSubjectSequenceOids(OID oid);
	 */
	public Sequence getSubjectSequence(OID subjectSequenceOid) {
		// FOR NOW: no subject sequence sourced for GFF3.
		//    loadFeaturesIfNeeded();
		//    if (getFeatureHandler().getSubjectSeqOids().contains(subjectSequenceOid)) {
		//      if (mSequenceLoader == null)
		//        mSequenceLoader = new SequenceLoader(getLoadedFileNames()[0]);
		//      return mSequenceLoader.getSequence(subjectSequenceOid.toString());
		//    } // Must scan for it.
		return null;
	} // End method: getSubjectSequence

	/**
	 * Retrieves the subject sequence OID collection for the
	 * feature whose OID was given.  Expect this to be
	 * called against blast-level (compound) features, but
	 * do not rule out HSP-level (simple) features.
	 */
	public Set<OID> getSubjectSequenceOids(OID oid) {
		loadFeaturesIfNeeded();

		Set<OID> returnSet = new HashSet<OID>();
		FeatureBean model = dataSource.getOrLoadBeanForOid(oid);
		if (model != null) {
			recursivelyFindSubject(model, returnSet);
		} // Found feature.

		return returnSet;

	} // End method

	/** Returns a report of all features with the subject sequence of OID given. */
	public void addToSubjSeqRpt(String subjectSeqId, OID genomeVersionOID, SubjectSequenceReport report) {
		// Stubbed for first pass:  returns info for only one sub-view.
		//    try {
		//      if (subjectSeqId.indexOf(":") == -1)
		//        subjectSeqId = "INTERNAL:" + subjectSeqId;
		//
		//      OID subjectSeqOid = parseFeatureOID(subjectSeqId);
		//      SubjSeqRptHandler handler = new SubjSeqRptHandler(  getLoadedFileNames()[0],
		//                                                          mSequenceAlignment,
		//                                                          (OIDParser)this);
		//      Set subjSeqOids = new HashSet();
		//      subjSeqOids.add(subjectSeqOid);
		//      handler.getRptLines(subjSeqOids, report);
		//    } // End try block
		//    catch (Exception ex) {
		//      FacadeManager.handleException(ex);
		//    } // End catch
	} // End method

	/**
	 * Returns list of alignments to features.  Said features will have description
	 * text containing the search target given.
	 */
	public List<Alignment> getAlignmentsForDescriptionsWith(String searchTarget) {
		loadFeaturesIfNeeded();
		List<FeatureBean> featureList = dataSource.getModelsForDescriptionsWith(searchTarget);

		List<Alignment> returnList = new ArrayList<Alignment>();
		for ( FeatureBean nextFeature: featureList ) {
			returnList.add(nextFeature.alignFeature());
		} // For all models found.

		return returnList;

	} // End method

	/** Returns description associated with model whose OID is given. */
	public String getFeatureDescription(OID oid) {
		loadFeaturesIfNeeded();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(oid);
		if (model != null)
			return model.getDescription();
		return null;
	} // End method: getFeatureDescription

	/** Returns description associated with model whose OID is given. */
	public String getFeatureDescriptionForParent(OID oid) {
		loadFeaturesIfNeeded();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(oid);
		if (model != null) {
			if (model.getParent() != null) {
				return model.getParent().getDescription();
			} // Has a parent.
		} // Has a model.
		return null;
	} // End method: getFeatureDescription

	/** Returns score string for model whose oid was given. */
	public String getScore(OID oid) {
		loadFeaturesIfNeeded();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(oid);
		if (model != null)
			return model.getScore();
		return null;
	} // End method: getScore

	/** Return either individual or summary expect value for model whose oid was given. */
	public String getIndividualExpect(OID oid) {
		loadFeaturesIfNeeded();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(oid);
		if (model != null)
			return model.getIndividualExpect();
		return null;
	} // End method: getIndividualExpect

	public String getSummaryExpect(OID oid) {
		loadFeaturesIfNeeded();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(oid);
		if (model != null)
			return model.getSummaryExpect();
		return null;
	} // End method: getSummaryExpect

	/** Return either subject start or end value associated with OID given. */
	public int getSubjectStart(OID oid) {
		loadFeaturesIfNeeded();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(oid);
		if (model != null)
			return model.getSubjectStart();
		return 0;
	} // End method: getSubjectStart

	public int getSubjectEnd(OID oid) {
		loadFeaturesIfNeeded();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(oid);
		if (model != null)
			return model.getSubjectEnd();
		return 0;
	} // End method: getSubjectEnd

	/** Returns the unique set of OIDs to which features found in the file refer. */
	public Set<OID> getReferencedOIDSet() {
		loadFeaturesIfNeeded();

		// Wish to add OID referenced by any contig.
		Set<OID> lReferencedSet = dataSource.getReferencedOIDSet();
		if (this.contigAlignment != null)
			lReferencedSet.add(genomicAxisOid);

		return lReferencedSet;
	} // End method: getReferencedOIDSet

	/** Returns all pre-computed root features aligned to the axis given as oid. */
	@Override
	public List<FeatureBean> getRootFeatures(OID axisOID, Set<Range> rangesOfInterest, boolean humanCurated) {
		loadAxisOverRangeSet(axisOID, rangesOfInterest, humanCurated);
		if (humanCurated)
			return dataSource.findHumanCuratedFeaturesOnAxis(axisOID, rangesOfInterest);
		else
			return dataSource.findPrecomputedFeaturesOnAxis(axisOID, rangesOfInterest);
	} // End method: getPrecomputedRootFeatures

	  /**
	   * Returns contig feature beans in specified range.
	   *
	   * @param OID axisOID identifier of axis to which returned curations will align.
	   * @param Set rangesOfInterest the ranges in which features returned will lie.
	   */
	  public List<FeatureBean> getContigFeatures(OID axisOID, Set<Range> rangesOfInterest) {
		  return dataSource.findContigFeaturesOnAxis(axisOID, rangesOfInterest);
	  }

	/**
	 * Returns obsolete features referencing axis that exist in entire file.
	 * By definition, obsolete features are human curated.
	 *
	 * @param OID axisOID axis to which feature reference.
	 * @return List list of entities that are obsolete and ref the axis.
	 */
	public List getObsoleteRootFeatures(OID axisOID) {
		// ALGORITHM: run load, and keep only curated/obsolete root features
		//  in memory.  Make a complete list of the features created from these
		//  models, and return it.
		loadAxisObsoleteFeatures(axisOID);
		return dataSource.findObsoleteEntitiesOnAxis(axisOID);
	} // End method

	/**
	 * Returns obsolete features UNDER the feature whose OID is given, and
	 * referencing the axis given.
	 *
	 * @param OID axisOID axis to which feature reference.
	 * @param OID parentOID feature containing features of interest.
	 * @return List list of entities that are obsolete and ref the axis.
	 */
	public List<GenomicEntity> getObsoleteNonRootFeatures(OID axisOID, OID parentOID) {
		// ALGORITHM: look at the model corresponding to the feature whose OID was
		//  given.  Traverse its hierarchy of subfeatures, and find all those
		//  models at next lower level that are obsolete.
		loadFeaturesIfNeeded();
		List<GenomicEntity> returnList = new ArrayList<GenomicEntity>();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(parentOID);
		if ((model != null) && (model.getAxisOfAlignment().equals(axisOID))) {

			boolean parentIsObsolete = false;
			if (model.getParent() != null)
				parentIsObsolete = model.getParent().isObsolete();

			FeatureBean subModel = null;
			if (model instanceof CompoundFeatureBean) {
				for (Iterator it = ((CompoundFeatureBean)model).getChildren().iterator(); it.hasNext(); ) {
					subModel = (FeatureBean)it.next();
					if (subModel.isObsolete())
						returnList.add(subModel.createFeatureEntity());
					else if (parentIsObsolete)
						throw new IllegalStateException(  "Parent feature is obsolete, but contains a non-obsolete child"+
								" parent OID: "+model.getParent().getOID()+
								" child OID: "+model.getOID());
				} // For all children.
			} // Has descendants.

		} // Refers to right axis and is obsolete.
		return returnList;
	} // End method

	/** Returns flag of whether the feature whose OID was given is known to this loader. */
	public boolean featureExists(OID oid) {
		loadFeaturesIfNeeded();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(oid);
		return (model != null);
	} // End method: featureExists

	/** Drops the feature whose OID is given, from feature cache. */
	public void removeFeature(OID oid) {
		if (dataSource == null)
			return; // Nothing to remove.
		dataSource.removeFeatureFromCache(oid);
	} // End method: removeFeature

	/** Returns flag of whether the OID in question is human curated. */
	public boolean isCurated(OID oid) {
		loadFeaturesIfNeeded();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(oid);
		if (model != null)
			return model.isCurated();

		// Arbitrarily default to non-curated.
		return false;

	} // End method: isCurated

	/**
	 * Returns analysis type for feature of oid given.
	 */
	public String getAnalysisTypeOfFeature(OID featureOid) {
		loadFeaturesIfNeeded();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(featureOid);
		if (model == null)
			return null;
		return model.getAnalysisType();
	} // End method

	/**
	 * Return discovery environment of feature given.
	 */
	public String getDiscoveryEnvironmentOfFeature(OID featureOid) {
		loadFeaturesIfNeeded();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(featureOid);
		if (model == null)
			return null;
		return model.getDiscoveryEnvironment();
	} // End method

	/**
	 * Returns range on axis of feature given.
	 */
	public Range getRangeOnAxisOfFeature(OID featureOid) {
		loadFeaturesIfNeeded();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(featureOid);
		if (model == null)
			return null;
		return model.calculateFeatureRange();
	} // End method

	/** Returns OID of alignment for feature given. */
	public OID getAxisOidOfAlignment(OID featureOid) {
		loadFeaturesIfNeeded();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(featureOid);
		if (model == null)
			return null;
		return model.getAxisOfAlignment();
	} // End method

	/** Returns alignment for the feature whose OID was given. */
	public Alignment getAlignmentForFeature(OID featureOID) {
		loadFeaturesIfNeeded();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(featureOID);
		if (model != null)
			return model.alignFeature();
		return null;
	} // End method: getAlignmentForFeature

	/** Returns alignment for the feature whose accession string was given. */
	public Alignment getAlignmentForAccession(String accnum, int accessionType) {
		loadFeaturesIfNeeded();
		FeatureBean model = null;
		if (accessionType == LoaderConstants.GENE_ACCESSION_TYPE) {
			model = (GeneFeatureBean)dataSource.getModelForGeneAccession(accnum);
		} // Return a gene.
		else if (accessionType == LoaderConstants.NONPUBLIC_ACCESSION_TYPE) {
			model = (FeatureBean)dataSource.getModelForInternalAccession(accnum);
		} // Return a transcript.

		// Work out what to return.
		if (model == null)
			return null;
		else
			return model.alignFeature();

	} // End method: getAlignmentForAccession

	/** Gets the gene for the OID given.  Gets annotation name for gene containing compound feature (transcript). */
	public String getGene(OID oid) {
		loadFeaturesIfNeeded();
		return dataSource.getGeneForTranscriptOID(oid);
	} // End method: getGene

	/** Returns the comments associated with a gene. */
	public GenomicEntityComment[] getComments(OID oid) {
		loadFeaturesIfNeeded();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(oid);
		if (model != null  &&  model.getComments() != null ) {
			return model.getComments();
		}
		return new GenomicEntityComment[0];
	} // End method: getComments

	/**
	 * Returns property sources associated with OID.  These sources are necessary because
	 * properties may be recursive in nature.
	 */
	public List<PropertySource> getPropertySources(OID oid) {
		loadFeaturesIfNeeded();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(oid);
		if (model != null)
			return model.getPropertySources();
		return new ArrayList<PropertySource>();
	} // End method: getPropertySources

	/**
	 * Returns replaced data objects.  These are necessary because replaced data
	 * is somewhat complex in nature.
	 */
	public List getReplacedData(OID oid) {
		loadFeaturesIfNeeded();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(oid);
		if (model != null)
			return model.getReplacedList();
		return new ArrayList();
	} // End method: getReplacedData

	/** Returns OIDs of evidence of a curation. */
	public OID[] getEvidence(OID oid) {
		loadFeaturesIfNeeded();
		FeatureBean model = (FeatureBean)dataSource.getOrLoadBeanForOid(oid);
		if (model != null)
			return model.getEvidence();
		return new OID[0];
	} // End method: getEvidence

	/** Add/remove listeners for the appearance of sequence alignments in the file. */
	public void addSequenceAlignmentListener(SequenceAlignmentListener listener) {
		listeners.add(listener);
	} // End method: addSequenceAlignmentListener

	public void removeSequenceAlignmentListener(SequenceAlignmentListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	} // End method: removeSequenceAlignmentListener

	//---------------------------------------OTHER PUBLIC METHODS
	/**
	 * Builds an OID with no special restrictions or translations.
	 *
	 * If no special restrictions are required in a subclass for
	 * a particular type of OID handling, this method may be called
	 * by the parseXxxxxxOIDTemplateMethod implementation.
	 *
	 * @param String idstr of format "PREFIX:dddddddddd"
	 *   where the d's represent a decimal (long) number.
	 * @return OID an OID in either the specified or an unknown namespace.
	 */
	public OID parseOIDGeneric(String idstr) {

		String[] strArrayForId = null;
		OID returnOID = null;

		char startchr = idstr.charAt(0);

		// Two possible formats: id="CCCC:ddddddddddd" or
		//    id="ddddddd".  First contains a namespace prefix,
		//    and the second is just the digits.
		//
		if(Character.isLetter(startchr)){
			strArrayForId = this.gffIdOIDHandler.processIdStringForPrefix(idstr);
			String oid = strArrayForId[1];
			String namespacePrefix =
				ReservedNameSpaceMapping.translateToReservedNameSpace(strArrayForId[0]);
			returnOID = new OID(namespacePrefix, oid, getGenomeVersionId());

			// Need a running counter for certain kinds of OIDs.
			if (returnOID.isScratchOID()) {
				// Test this read OID against the current highest OID, and make it the
				// new highest if it is higher.
				try {
					OIDGenerator.getOIDGenerator().setInitialValueForNameSpace
					(OID.SCRATCH_NAMESPACE, Long.toString(1 + returnOID.getIdentifier()));
				} // End try block
				catch (IllegalArgumentException iae) {
					// Ignoring here: we only wish to seed the generator with highest known value.
				} // End catch block for seed.
			} // Found another scratch.

		} // Proper alphabetic prefix.
		else {
			if (idstr.indexOf(':') >= 0) {
				FacadeManager.handleException( new IllegalArgumentException( "This application is expecting a namespace prefix beginning with an alphabetic character in its XML IDs.\nYou specified '"
						+idstr+"'."));
			} // Prefix invalid
			else {
				// NOTE: as of 5/7/2001, we found that the EJB/db are getting confused
				//       about non-internal OIDs.  TO fix this, we are precluding non-
				//       internal database OIDs from being sent there.  Unfortunately,
				//       this also requires no-prefix OIDs no longer be accepted.
				returnOID = new OID(OID.INTERNAL_DATABASE_NAMESPACE, idstr, getGenomeVersionId());
			} // No prefix at all.

		} // Found unexpected character as first character.

		return returnOID;
	} // End method: parseOIDGeneric

	/**
	 * Builds an OID with no special restrictions or translations,
	 * and can translate a GA number into an OID.
	 *
	 * @param String idstr of format "PREFIX:dddddddddd"
	 *   where the d's represent a decimal (long) number.
	 * @return OID an OID in either the specified or an unknown namespace.
	 */
	protected OID parseOIDorAlphaNum(String idstr) {
		return this.gffIdOIDHandler.parseOIDorAlphaNum(idstr, getGenomeVersionId());
	} // End method: parseOIDorGA

	/**
	 * Allows external set of the facade manager to use in creating
	 * models.
	 */
	public void setFacadeManager(Gff3FacadeManager lReadFacadeManager) {
		// Ignore
	} // End method: setFacadeManager

	/** Returns an identifying string for this loader. */
	public String toString() {
		StringBuffer returnBuffer = new StringBuffer(1000);
		for (int i = 0; i < getLoadedFileNames().length; i++) {
			returnBuffer.append(getLoadedFileNames()[i]);
		} // For all loaded file names

		return returnBuffer.toString();
	} // End method: toString

	//------------------------------HELPER METHODS
	/** Allows subclass to set the feature handler that is used in grabbing feat.*/
	protected void setFeatureHandler(Gff3DataSource lHandler) {
		dataSource = lHandler;
	} // End method

	/** Gets the feature handler for subclasses. */
	protected Gff3DataSource getFeatureHandler() {
		return dataSource;
	} // End method

	/** Returns assembly version to subclass.  Used by workspace loader for promotion */
	protected String getAssembly() {
		loadInitialIfNeeded();
		return assemblyVersion;
	} // End method

	/** Finds all subject sequence oids from all children, descending, of given model. */
	protected void recursivelyFindSubject(FeatureBean model, Set<OID> returnSet) {
		// Check non-null params
		if ((returnSet == null) || (model == null))
			return;

		if (model instanceof SimpleFeatureBean) {
			OID subjectSequenceOid = null;
			subjectSequenceOid = ((SimpleFeatureBean)model).getSubjectSequenceOid();
			if (subjectSequenceOid != null)
				returnSet.add(subjectSequenceOid);
			return;
		} // Got simple model.

		// Get child list for model.
		List childrenOfModel = ((CompoundFeatureBean)model).getChildren();
		if (model == null)
			return;

		// Iterate over all children.
		FeatureBean childModel = null;
		for (Iterator it = childrenOfModel.iterator(); it.hasNext(); ) {
			childModel = (FeatureBean)it.next();
			if (childModel != null)
				recursivelyFindSubject(childModel, returnSet);
		} // For all children.

	} // End method

	/**
	 * Calling this method triggers the initial scan of the file for information
	 * related to axis and contig and residues.  Overridden here to lend this
	 * behavior to genomic axis file reads ONLY.
	 */
	protected synchronized void loadInitialIfNeeded() {
		if (initialScanComplete)
			return;

		// new RuntimeException("Running initial load for "+getLoadedFileNames()[0]).printStackTrace();
		initialScanComplete = true;

		Gff3AxisLoader axisLoader = new Gff3AxisLoader(getLoadedFileNames()[getLoadedFileNames().length - 1]);
		assemblyVersion = axisLoader.getAssembly();
		// Initial load may not pickup anything of value.  In that case, this
		// file is probably NOT an assembly file, and there is nothing more to collect.
		if (axisLoader.getGenomicAxisID() == null)
			return;
		// Pull species information.
		String taxon = axisLoader.getSpecies();
		if (taxon == null || taxon.length() == 0)
			taxon = DEFAULT_SPECIES;

		species = new Species(
				OIDGenerator.getOIDGenerator().generateOIDInNameSpace(  OID.API_GENERATED_NAMESPACE, getGenomeVersionId()),
				taxon);

		axisLength = axisLoader.getAxisLength();
		genomicAxisOid = this.parseContigOID(axisLoader.getGenomicAxisID());

		sequenceBuilder = axisLoader.getSequenceBuilder();
		if (sequenceBuilder == null)
			residues = axisLoader.getSequence();

		if (axisLoader.getSequenceAlignments().size() > 0) {
			SequenceAlignment nextSequenceAlignment = null;
			SequenceAlignmentListener nextListener = null;
			for (Iterator it = axisLoader.getSequenceAlignments().iterator(); it.hasNext(); ) {
				nextSequenceAlignment = (SequenceAlignment)it.next();
				// System.out.println("Alignment found "+nextSequenceAlignment);

				// Now send off event to listeners.
				for (Iterator<SequenceAlignmentListener> listenerToIt = listeners.iterator(); listenerToIt.hasNext(); ) {
					nextListener = listenerToIt.next();
					nextListener.foundSequenceAlignment(nextSequenceAlignment, getGenomeVersionId());
				} // For all listeners.
			} // For all alignments

			// Now that all are handled, notify listener(s) that no more remain.
			SequenceAlignmentListener[] listenerArr = new SequenceAlignmentListener[listeners.size()];
			listeners.toArray(listenerArr);
			for (int i = 0; i < listenerArr.length; i++) {
				listenerArr[i].noMoreAlignments(this);
			} // For all listeners.
			listenerArr = null;

		} // Handle those alignments.

	} // End method: loadInitialIfNeeded

	/**
	 * Invokes next-level load.  This is done on an "as-needed" basis
	 * to avoid up-front load time and to keep the memory usage as low
	 * as possible.
	 */
	protected synchronized void loadFeaturesIfNeeded() {
		try {
			// Chain in all other loads to avoid bypassing order dependency.
			loadInitialIfNeeded();

			if (dataSource != null)
				return;

			dataSource = new Gff3DataSource(getLoadedFileNames()[0], sequenceAlignment, (OIDParser)this);

			// Call to cache feature references, but not data FOR features.
			dataSource.accumulateFeatureReferenceData(getLoadedFileNames()[0]);

		} // End try block
		catch (Throwable throwable) {
			FacadeManager.handleException(throwable);
		} // End catch block.
	} // End method

	/**
	 * Forces load, as well as caching of data, for axis data over the
	 * set of ranges given. Returns what it has found.
	 */
	protected synchronized void loadAxisOverRangeSet(OID axisOID, Set<Range> rangesOfInterest, boolean humanCurated) {
		try {
			// Chain in other loads to avoid bypassing order dependency.
			loadInitialIfNeeded();

			if (dataSource == null)
				dataSource = new Gff3DataSource(getLoadedFileNames()[0], sequenceAlignment, (OIDParser)this);

			// Call to cache features over given range, aligning to given axis.
			dataSource.accumulateFeatures(getLoadedFileNames()[0], axisOID, rangesOfInterest, humanCurated);

		} // End try block
		catch (Throwable throwable) {
			FacadeManager.handleException(throwable);
		} // End catch block.

	} // End method

	/**
	 * Forces load and caching of obsoleted features for an entire axis.
	 */
	protected synchronized void loadAxisObsoleteFeatures(OID axisOID) {
		return;  // Not supported for this facade.
	} // End method

	/**
	 * Returns genome version id.  May be overridden from subclass.
	 */
	protected int getGenomeVersionId() {
		return genomeVersionId;
	} // End method

	/**
	 * Allows set of genome version Id from subclass.
	 */
	protected void setGenomeVersionId(int lGenomeVersionId) {
		genomeVersionId = lGenomeVersionId;
	} // End method

} // End class: SAXLoaderBase

