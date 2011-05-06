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
 * Title:        Feature Handler for Gene features<p>
 * Description:  Primary, or first-delegated, handler for SAX loading GenomicsExchangeFormat-Vx<p>
 * @author Les Foster
 * @version $Id$
 */
package api.facade.concrete_facade.gff3;

import api.entity_model.model.fundtype.GenomicEntity;
import api.facade.concrete_facade.shared.OIDParser;
import api.facade.concrete_facade.shared.feature_bean.CompoundFeatureBean;
import api.facade.concrete_facade.shared.feature_bean.FeatureBean;
import api.facade.concrete_facade.shared.feature_bean.GeneFeatureBean;
import api.facade.concrete_facade.shared.ContainsInDescriptionCriterion;
import api.facade.concrete_facade.shared.ContainsOidCriterion;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.GenomicEntityComment;
import api.stub.data.OID;
import api.stub.data.ReplacementRelationship;
import api.stub.geometry.Range;

import java.util.*;

/**
 * Builds feature beans from information found in GFF3 parse.
 * @See DrivingHandler
 */
public class Gff3DataSource implements FeatureListener {
	//------------------------------MEMBER VARIABLES
	// These collections are temporary and reflect the state during the
	// construction of a particular model.
	private Map mSequenceLengthMap = new HashMap();

	private boolean mRetainAllFeatures = false;

	// Feature caching criteria
	private Set mRangesToCache;
	private OID mAxisOidToCache;
	private boolean mCacheCurations;
	private boolean mCacheObsoleted;
	private boolean mKeepAllFeaturesFromParse = false;

	// Previous requests' cached Axis oid.
	private OID mPreviousAxisOidToCache;

	private OIDParser mOIDParser = null;

	// Final state of the thing.
	//

	// Used in some cases during creation of model objects.
	private Gff3FacadeManager mReadFacadeManager = null;
	private SequenceAlignment mSequenceAlignment = null;
	private Range mSequenceAlignmentRange = null;

	// Collections of root curated and precomputed features.
	// ???
	private Collection<OID> mRootCuratedFeatures = new HashSet<OID>();
	private Collection<OID> mRootPrecomputedFeatures = new HashSet<OID>();
	private Collection<OID> mContigFeatures = new HashSet<OID>();

	// Registry of all feature OIDs known to this loader.  It is also a
	// mapping of those OIDs to the models built for them.
	private Map<OID,FeatureBean> mFeatureOIDRegistry = new HashMap<OID,FeatureBean>();

	// Registry of transcript BACK to the gene which contains it.
	private Map<OID,OID> mTranscriptToGeneRegistry = new HashMap<OID,OID>();

	// Registries of all accession-bearing features, of their
	// accession numbers versus their models.
	private Map<String,OID> mGeneAccessionRegistry = new HashMap<String,OID>();
	private Map<String,OID> mInternalAccessionRegistry = new HashMap<String,OID>();

	// Cached input source.  Where the document is read from.
	private String mSource = null;

	// Unique set of all referenced OIDs from the file represented
	// by this loader.
	private Set<OID> mReferencedOIDSet = new HashSet<OID>();

	// Collection of OIDs of all subject sequence represented herein.
	private Collection<OID> mSubjectSeqOids = new HashSet<OID>();
	
	private FeatureFactory mFeatureFactory;
	
	private OID mCurrentlyCachedAxis = null;

	//------------------------------CONSTRUCTORS
	/**
	 * Constructor gets the first required data from the input file
	 * using a data scan.
	 */
	public Gff3DataSource(String lFileName, SequenceAlignment lSequenceAlignment, OIDParser lOIDParser) {
		this( lOIDParser );

		mSequenceAlignment = lSequenceAlignment;
		if (mSequenceAlignment != null)
			mSequenceAlignmentRange = new Range(mSequenceAlignment.getAxisStart(), mSequenceAlignment.getAxisEnd());
		mSource = lFileName;
		// ASSUMING:  will not need to run feature load for a sequence alignment.
		//runFeatureFactoryLoad( mSource, mAxisOidToCache );

	} // End constructor

	/**
	 * Constructs handlers, but does not populate them.
	 */
	public Gff3DataSource(OIDParser lOIDParser) {

		mOIDParser = lOIDParser;
		mFeatureFactory = new FeatureFactory();
		mFeatureFactory.setFeatureListener( this );

	} // End constructor

	//------------------------------FeatureListener IMPLEMENTATION
	public void feature( CompoundFeatureBean feature ) {
		recursivelyRegisterFeatures( feature, true );
	}
	
	//------------------------------PUBLIC INTERFACE
	/** Adds features over a given range, aligning to a given axis. */
	public synchronized void accumulateFeatures( String lSource, OID lAxisOidOfInterest,
			Set lRangesOfInterest, boolean lHumanCurated) {

		// Setup criteria for this load.
		mRangesToCache = lRangesOfInterest;
		mAxisOidToCache = lAxisOidOfInterest;
		mCacheCurations = lHumanCurated;
		mCacheObsoleted = false;

		if (! mRetainAllFeatures)
			jetisonFeaturesIfAxisChanged();

		runFeatureFactoryLoad( lSource, lAxisOidOfInterest );

	} // End method

	/** Picks up OIDs and other data to ACCESS features later. */
	public synchronized void accumulateFeatureReferenceData(String lSource) {
		// Clear any capture criteria
		mRangesToCache = null;
		mAxisOidToCache = null;

		cacheReferencedOIDs( lSource );
	} // End method

	/**
	 *  Hands back the set of all seq oids. It is assumed ok to have the
	 *  axis oid remain in this collection
	 */
	public Collection getSubjectSeqOids() {
		return mSubjectSeqOids;
	} // End method

	/**
	 * Returns length, as string, of sequence, if that seq is known to this
	 * loader.  If not known, returns null;
	 */
	public String getSubjectSequenceLength(OID sequenceOID) {
		Integer sequenceLengthInt = (Integer)mSequenceLengthMap.get(sequenceOID);
		if (sequenceLengthInt != null)
			return sequenceLengthInt.toString();
		else
			return null;
	} // End method

	/**
	 * Returns a feature bean, given the OID.
	 */
	public FeatureBean getOrLoadBeanForOid(OID featureOID) {
		try {

			FeatureBean returnModel = null;
			if (mFeatureOIDRegistry.containsKey(featureOID)) {
				returnModel = (FeatureBean)mFeatureOIDRegistry.get(featureOID);
				if ((returnModel == null) && (mSource != null)) {
					Range range = new Range(mSequenceAlignment.getAxisStart(), mSequenceAlignment.getAxisEnd());

					FeatureScanner featureScanner = new FeatureScanner(mSource, mReadFacadeManager, mAxisOidToCache, range, mOIDParser);
					List<FeatureBean> lFeatureList = featureScanner.getBeansForCriterion(new ContainsOidCriterion(featureOID));
					for ( FeatureBean rootModel: lFeatureList ) {
						while (rootModel.getParent() != null)
							rootModel = rootModel.getParent();
						this.recursivelyRegisterFeatures((CompoundFeatureBean)rootModel, true); // Only do this once per root hierarchy!
						returnModel = (FeatureBean)mFeatureOIDRegistry.get(featureOID); // Try again!
					} // For all found feature models.
				} // Need to load the model.
			} // Loader knows about feature

			return returnModel;

		} catch ( Exception ex ) {
			FacadeManager.handleException(new IllegalArgumentException("Failed to load feature identified as " + featureOID.toString(), ex ) );
			return null;
		}
	} // End method

	/**
	 * Drops feature out of cache.  After this, can be reloaded.  Assumes that
	 * the OID is for a root feature.  So call with root's OID!
	 */
	public void removeFeatureFromCache(OID oid) {
		CompoundFeatureBean model;
		if (null != (model = (CompoundFeatureBean)mFeatureOIDRegistry.get(oid))) {
			eliminateFromFeatureOidRegistry(model);
		} // Got it
	} // End method

	/**
	 * Returns list of models to features.  Said features will have description
	 * text containing the search target given.
	 */
	public List getModelsForDescriptionsWith(String searchTarget) {
		try {
			List<FeatureBean> lReturnList = null;
			Range range = new Range(mSequenceAlignment.getAxisStart(), mSequenceAlignment.getAxisEnd());
			FeatureScanner featureScanner = new FeatureScanner(mSource, mReadFacadeManager, mAxisOidToCache, range, mOIDParser);
			lReturnList = featureScanner.getBeansForCriterion(new ContainsInDescriptionCriterion(searchTarget));
			return lReturnList;

		} catch ( Exception ex ) {
			FacadeManager.handleException(new IllegalArgumentException("Failed to load feature identified as " + searchTarget, ex ) );
			return null;
		}
	} // End method

	/**
	 * Returns a gene feature model, given an accession number.
	 */
	public GeneFeatureBean getModelForGeneAccession(String geneAccession) {
		try {
			OID geneOID = mGeneAccessionRegistry.get(geneAccession);
			if (geneOID != null)
				return (GeneFeatureBean)getOrLoadBeanForOid(geneOID);
			else
				return null;
		} catch (ClassCastException cce) {
			FacadeManager.handleException(new IllegalArgumentException("Requested gene accession for non-gene feature"));
		} // End catch block for getter.

		return null;
	} // End method: getModelForGeneAccession

	/**
	 * Returns the gene accession string for the gene containing the transcript
	 * whose oid was given.
	 */
	public String getGeneForTranscriptOID(OID oid) {
		String returnGeneName = null;
		OID geneOID = mTranscriptToGeneRegistry.get(oid);
		if (geneOID != null) {
			GeneFeatureBean model = (GeneFeatureBean)getOrLoadBeanForOid(geneOID);
			if (model != null)
				returnGeneName = model.getAnnotationName();
		} // Found a gene for the transcript.

		return returnGeneName;
	} // End method: getGeneForTranscriptOID

	/**
	 * Returns a transcript model, given accession number.
	 */
	public CompoundFeatureBean getModelForInternalAccession(String accession) {
		try {
			OID featureOID = mInternalAccessionRegistry.get(accession);
			if (featureOID != null)
				return (CompoundFeatureBean)getOrLoadBeanForOid(featureOID);
			else
				return null;
		} catch (ClassCastException cce) {
			FacadeManager.handleException(new IllegalArgumentException("Requested transcript accession for non-transcript feature", cce));
		} // End catch block for getter.

		return null;
	} // End method: getModelForInternalAccession

	/**
	 * Returns the unique set of OIDs referenced by features whose models are contained.
	 */
	public Set<OID> getReferencedOIDSet() {
		return mReferencedOIDSet;
	} // End method: getReferencedOIDSet

	//------------------------------IMPLEMENTATION OF ExceptionHandler
	/** Simply delegates to the facade manager. */
	public void handleException(Exception lException) {
		FacadeManager.handleException(lException);
	} // End method: handleException


	/**
	 * Find all registered human curated features which align to the
	 * axis whose oid is given.
	 */
	List<FeatureBean> findHumanCuratedFeaturesOnAxis(OID axisOID, Set<Range> rangesOfInterest) {
		return findFeaturesOnAxis(mRootCuratedFeatures, axisOID, rangesOfInterest);
	} // End method: findHumanCuratedFeaturesOnAxis

	/**
	 * Find all registered precomputed features which align to the
	 * axis whose oid is given.
	 */
	List<FeatureBean> findPrecomputedFeaturesOnAxis(OID axisOID, Set<Range> rangesOfInterest) {
		return findFeaturesOnAxis(mRootPrecomputedFeatures, axisOID, rangesOfInterest);
	} // End method: findPrecomputedFeaturesOnAxis

	/** Find all contigs aligning to the axis, over the range(s) given. */
	List<FeatureBean> findContigFeaturesOnAxis(OID axisOID, Set<Range> rangesOfInterest) {
		return findFeaturesOnAxis(mContigFeatures, axisOID, rangesOfInterest);
	}
	
	/**
	 * Find all obsoleted root features which refer to the axis whose OID is given.
	 */
	List<GenomicEntity> findObsoleteEntitiesOnAxis(OID axisOID) {
		List<GenomicEntity> features = new ArrayList<GenomicEntity>();
		OID featureAxisOID = null;
		FeatureBean validFeature = null;

		// looking for only those axis alignments that have the same axis oid as the input
		// axisOID
		for ( OID featureOID: mRootCuratedFeatures ) {

			// Will take advantage of fact that applicable features are already IN
			// memory.
			if (null == (validFeature = getModelForOidIfPreviouslyLoaded(featureOID)))
				continue;

			featureAxisOID = validFeature.getAxisOfAlignment();

			if (axisOID.equals(featureAxisOID)) {
				if (validFeature.isObsolete())
					features.add(validFeature.createFeatureEntity());
			} // Same axis

		} // For all iterations.

		return features;

	} // End method: findObsoleteFeaturesOnAxis

	//----------------------------HELPER METHODS
	/** Only collect the OID's of the referenced axes. */
	private void cacheReferencedOIDs( String source ) {
		if ( mReferencedOIDSet.isEmpty()) {
			mReferencedOIDSet.addAll( mFeatureFactory.getReferencedAxisOIDs( source ) );
		}
	}
	
	/** Prepare the feature factory for the OID given. */
	private void runFeatureFactoryLoad( String source, OID axisOID ) {
		// DEBUG CODE....
		if ( axisOID == null ) {
			System.out.println("Already cached " + mCurrentlyCachedAxis + ", not killing features with null axis.");
			if ( mCurrentlyCachedAxis == null ) {
				new Exception().printStackTrace();
			}
			return;
		}

		if ( mCurrentlyCachedAxis == null  ||  ! mCurrentlyCachedAxis.equals( axisOID ) ) {
			mCurrentlyCachedAxis = axisOID;
		}

		try {
			mFeatureFactory.loadFile( source, axisOID, mReadFacadeManager );

		} catch ( Exception ex ) {
			FacadeManager.handleException(new IllegalArgumentException( "Failed to load data from " + mSource, ex ));
		}			
	}
	
	/**
	 * If the axis given is not the one currently being cached, flush the
	 * old cached feature models, and the given axis WILL be cached instead
	 * of the old one.
	 */
	private void jetisonFeaturesIfAxisChanged() {
		if ((mAxisOidToCache != null) && mAxisOidToCache.equals(mPreviousAxisOidToCache))
			return;

		mPreviousAxisOidToCache = mAxisOidToCache;

		List<FeatureBean> lJetisonList = new ArrayList<FeatureBean>();
		lJetisonList.addAll(mFeatureOIDRegistry.values());
		for ( FeatureBean lNextModel: mFeatureOIDRegistry.values() ) {
			eliminateFromFeatureOidRegistry(lNextModel);
		} // For all root features currently loaded.

	} // End method

	/**
	 * Scan a list, testing the parent OID of its children, to see if they match
	 * an axis OID given as parameter.  Return the list of those that DO match.
	 */
	private List<FeatureBean> findFeaturesOnAxis(Collection<OID> containing, OID axisOID, Set<Range> rangesOfInterest) {
		List<FeatureBean> features = new ArrayList<FeatureBean>();
		OID featureAxisOID = null;
		FeatureBean validFeature = null;
		Range nextRange = null;
		boolean featureAdded = false;

		// looking for only those axis alignments that have the same axis oid as the input
		// axisOID
		for ( OID featureOID : containing ) {

			// Will take advantage of fact that applicable features are already IN
			// memory.
			if (null == (validFeature = getModelForOidIfPreviouslyLoaded(featureOID)))
				continue;

			featureAxisOID = validFeature.getAxisOfAlignment();

			if (axisOID.equals(featureAxisOID)) {
				featureAdded = false;
				for (Iterator<Range> rangeIterator = rangesOfInterest.iterator(); (!featureAdded) && rangeIterator.hasNext(); ) {
					nextRange = rangeIterator.next();
					if (nextRange.intersects(validFeature.calculateFeatureRange())) {
						features.add(validFeature);
						featureAdded = true; // Add this feature only once.
					} // Located the axis
				} // For all ranges in set.
			} // Same axis

		} // For all iterations.
		return features;
	} // End method: findFeaturesOnAxis

	/**
	 * Given a compound feature model, register its children as features, for
	 * lookup by their oids.  It also keeps track of OIDs referenced BY these
	 * features.
	 */
	private void recursivelyRegisterFeatures(CompoundFeatureBean compoundModel, boolean registerModels) {

		String annotationString = compoundModel.getAnnotationName();
		if (compoundModel instanceof GeneFeatureBean) {
			// Genes from GFF source may have multiple "annotation names", semicolon delimited.
			if ( annotationString != null ) {
				String[] annotationNames = annotationString.split( ";" );
				for ( String nextAnnotationName: annotationNames ) {
					if (! mGeneAccessionRegistry.containsKey( nextAnnotationName ) ) {
						mGeneAccessionRegistry.put( nextAnnotationName, compoundModel.getOID() );
					}					
				}
			}
			mRootCuratedFeatures.add( compoundModel.getOID() );
		}
		else {
			// Ignore contigs and chromosomes, here.
			String analysisType = compoundModel.getAnalysisType();
			if ( analysisType.equalsIgnoreCase( FeatureFactory.CONTIG_GFF ) ) {
				mContigFeatures.add( compoundModel.getOID() );
			}
			else if ( ! (analysisType.equalsIgnoreCase( FeatureFactory.CHROMOSOME_GFF ) ||
					    ( analysisType.equalsIgnoreCase( FeatureFactory.SOURCE_GFF ) ) ) ) {
				if ( annotationString != null ) {
					if (! mInternalAccessionRegistry.containsKey( annotationString ) )
						mInternalAccessionRegistry.put( annotationString, compoundModel.getOID() );					
				}
				mRootPrecomputedFeatures.add( compoundModel.getOID() );

			}
		}			

		// Register for retrieval by OID.
		registerOidAndOrModel(compoundModel, registerModels);

		// Register OID of reference.
		mReferencedOIDSet.add(compoundModel.getAxisOfAlignment());

		FeatureBean model = null;

		for (Iterator it = compoundModel.getChildren().iterator(); it.hasNext(); ) {
			model = (FeatureBean)it.next();
			registerOidAndOrModel(model, registerModels);
			if ( model.getAnalysisType().equalsIgnoreCase("transcript") ) {
				mTranscriptToGeneRegistry.put( model.getOID(), compoundModel.getOID() );
			}

			if (model instanceof CompoundFeatureBean) {
				recursivelyRegisterFeatures((CompoundFeatureBean)model, registerModels);
			}
			else {
				mReferencedOIDSet.add(model.getAxisOfAlignment());
			}

		} // For all children

	} // End method: recursivelyRegisterFeatures

	/**
	 * Tests whether the model provided falls within the range to which
	 * this handle applies.  That range could be the entire axis, or
	 * it could be just an aligned portion.
	 */
	private boolean withinRequiredRange(FeatureBean model) {
		// No sequence alignment implies that entire axis of features is required.
		if (mSequenceAlignmentRange == null) {
			return true;
		} // No range of restriction or alignment given.
		else {
			// Test that range contains the feature.
			return mSequenceAlignmentRange.contains(model.calculateFeatureRange());
		} // Must discard features outside of range.
	} // End method

	/**
	 * Places the OID of the model in the registry.  If it meets caching criteria,
	 * place the model into the registry as the OID's value.
	 */
	private void registerOidAndOrModel(FeatureBean lModel, boolean lRegisterModels) {
		OID lKey = lModel.getOID();
		if (lRegisterModels) {
			if (mFeatureOIDRegistry.containsKey(lKey))
				mFeatureOIDRegistry.remove(lKey);
			mFeatureOIDRegistry.put(lKey, lModel);
		} // Must register OID vs model.
		else {
			if (! mFeatureOIDRegistry.containsKey(lKey))
				mFeatureOIDRegistry.put(lKey, null);
		} // Must register just the OID.

		if (! mReferencedOIDSet.contains(lModel.getAxisOfAlignment())) {
			mReferencedOIDSet.add(lModel.getAxisOfAlignment());
		} // Must register the referenced axis OID.
	} // End method

	/** If the model was in the registry, return it.  Otherwise, do not force load. */
	private FeatureBean getModelForOidIfPreviouslyLoaded(OID featureOID) {
		return mFeatureOIDRegistry.get(featureOID);
	} // End method

	/** Tests input model.  Is it one to cache this time through load? */
	private boolean meetsCacheCriteria(FeatureBean lModel) {

		// Keep all models, regardless of range in which they appear.
		if (mKeepAllFeaturesFromParse)
			return true;

		// Keep models that are obsolete, if obsoleted models are required.
		//  Keep ALL models, regardless of their axis of ref, so that repeated
		//  trips to load obsoleted features do not bog down the application.
		if (mCacheObsoleted && lModel.isObsolete())
			return true;

		// Un-set criteria==no criteria!  Keep NONE.
		if (mRangesToCache == null)
			return false;

		if (mCacheCurations) {
			if (! lModel.isCurated()) {
				return false;
			} // Got a curated model.
		} // Only Human Curated.
		else {
			if (lModel.isCurated()) {
				return false;
			} // Got noncurated.
		} // Only precomputes

		if (! lModel.getAxisOfAlignment().equals(mAxisOidToCache))
			return false;

		boolean lReturnFlag = false;
		Range lNextRange = null;
		for (Iterator it = mRangesToCache.iterator(); (! lReturnFlag) && it.hasNext(); ) {
			lNextRange = (Range)it.next();
			if (lModel.calculateFeatureRange().intersects(lNextRange))
				lReturnFlag = true;
		} // For all ranges to be cached.

		return lReturnFlag;
	} // End method

	/**
	 * Takes the model given and all its descendants and eliminates ref
	 * from feature OID registry.  This override avoids costly casting and
	 * instance-of tests.
	 */
	private void eliminateFromFeatureOidRegistry(CompoundFeatureBean lModel) {
		// Recurse for child objects.
		for (Iterator it = lModel.getChildren().iterator(); it.hasNext(); ) {
			eliminateFromFeatureOidRegistry((FeatureBean)it.next());
		} // For all children.

		// Remove the old model ref, but keep the key in the registry.
		if (mFeatureOIDRegistry.containsKey(lModel.getOID())) {
			mFeatureOIDRegistry.remove(lModel.getOID());
			mFeatureOIDRegistry.put(lModel.getOID(), null);
		} // Found ref.
	} // End method

	/**
	 * Takes the model given and all its descendants and eliminates ref
	 * from feature OID registry.
	 */
	private void eliminateFromFeatureOidRegistry(FeatureBean lModel) {
		if (lModel == null)
			return;

		// Recurse for child objects.
		if (lModel instanceof CompoundFeatureBean) {
			for (Iterator it = ((CompoundFeatureBean)lModel).getChildren().iterator(); it.hasNext(); ) {
				eliminateFromFeatureOidRegistry((FeatureBean)it.next());
			} // For all children.
		} // May have children

		// Remove the old model ref, but keep the key in the registry.
		if (mFeatureOIDRegistry.containsKey(lModel.getOID())) {
			mFeatureOIDRegistry.remove(lModel.getOID());
			mFeatureOIDRegistry.put(lModel.getOID(), null);
		} // Found ref.
	} // End method

} // End class: DrivingHandler
