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
package api.facade.concrete_facade.xml;

import api.facade.concrete_facade.xml.model.CompoundFeatureModel;
import api.facade.concrete_facade.xml.model.FeatureModel;
import api.facade.concrete_facade.xml.model.GeneFeatureModel;
import api.facade.concrete_facade.xml.sax_support.ArrayListElementStacker;
import api.facade.concrete_facade.xml.sax_support.CEFParseHelper;
import api.facade.concrete_facade.xml.sax_support.GenomicsExchangeHandler;
import api.facade.concrete_facade.xml.sax_support.ElementContext;
import api.facade.concrete_facade.xml.sax_support.ElementStacker;
import api.facade.concrete_facade.xml.sax_support.FeatureHandlerBase;
import api.facade.concrete_facade.xml.sax_support.OIDParser;
import api.facade.concrete_facade.xml.sax_support.ReplacedData;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.GenomicEntityComment;
import api.stub.data.OID;
import api.stub.data.ReplacementRelationship;
import api.stub.geometry.Range;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Delegated class to handle SAX events.  It in turn delegates to handlers
 * that are used to build models.  It also keeps the primary reference to the
 * element stacks.
 */
public class DrivingHandler extends FeatureHandlerBase {
    //------------------------------MEMBER VARIABLES
    // These collections are temporary and reflect the state during the
    // construction of a particular model.
    private List mCompoundModels = new ArrayList();
    private List mCommentList = new ArrayList();
    private List mGeneReplacedList = new ArrayList();
    private List mPropertySourceList = new ArrayList();
    private Map mSequenceLengthMap = new HashMap();

    // These members are also temporary.
    private String mResultSetType = null;
    private String mAnalysisSource = null;
    private String mAnnotationSource = null;
    private String mDescription = null;
    private String mAnnotationName = null;
    private String mCommentText = null;

    private Map mFillBufferMap = null;

    private boolean mDelegatedState = false;
    private boolean mInGene = false;
    private OID mGeneOID = null;

    private boolean mRetainAllFeatures = false;

    // Feature caching criteria
    private Set mRangesToCache;
    private OID mAxisOidToCache;
    private boolean mCacheCurations;
    private boolean mCacheObsoleted;
    private boolean mKeepAllFeaturesFromParse = false;

    // Previous requests' cached Axis oid.
    private OID mPreviousAxisOidToCache;

    // Context tracking and interpretation of OIDs.
    private OIDParser mOIDParser = null;
    private ElementStacker mElementStacker = new ArrayListElementStacker();

    // Final state of the thing.
    //

    // Used in some cases during creation of model objects.
    private XmlFacadeManager mReadFacadeManager = null;
    private SequenceAlignment mSequenceAlignment = null;
    private Range mSequenceAlignmentRange = null;

    // Collections of root curated and precomputed features.
    private Collection mRootCuratedFeatures = new HashSet();
    private Collection mRootPrecomputedFeatures = new HashSet();

    // Registry of all feature OIDs known to this loader.  It is also a
    // mapping of those OIDs to the models built for them.
    private Map mFeatureOIDRegistry = new HashMap();

    // Registry of transcript BACK to the gene which contains it.
    private Map mTranscriptToGeneRegistry = new HashMap();

    // Registries of all accession-bearing features, of their
    // accession numbers versus their models.
    private Map mGeneAccessionRegistry = new HashMap();
    private Map mInternalAccessionRegistry = new HashMap();

    // Cached input source.  Where the document is read from.
    private String mSource = null;

    // Unique set of all referenced OIDs from the file represented
    // by this loader.
    private Set mReferencedOIDSet = new HashSet();

    // Collection of OIDs of all subject sequence represented herein.
    private Collection mSubjectSeqOids = new HashSet();

    //------------------------------CONSTRUCTORS
    /**
     * Constructor gets the first required data from the input file
     * using a SAX element scan.
     */
    public DrivingHandler(String lFileName, SequenceAlignment lSequenceAlignment, OIDParser lOIDParser) {

      super(lOIDParser);

      mOIDParser = lOIDParser;
      mSequenceAlignment = lSequenceAlignment;
      if (mSequenceAlignment != null)
        mSequenceAlignmentRange = new Range(mSequenceAlignment.getAxisStart(), mSequenceAlignment.getAxisEnd());
      mSource = lFileName;

      // Build chain of delegates.
      FeatureHandlerBase lCompoundFeatureHandler = new CompoundFeatureHandler();
      lCompoundFeatureHandler.setOIDParser(mOIDParser);
      setDelegate(lCompoundFeatureHandler);

      FeatureHandlerBase lSimpleFeatureHandler = new SimpleFeatureHandler(mSequenceAlignment);
      lSimpleFeatureHandler.setOIDParser(mOIDParser);
      lCompoundFeatureHandler.setDelegate(lSimpleFeatureHandler);

    } // End constructor

    /**
     * Constructs handlers, but does not populate them.
     */
    public DrivingHandler(OIDParser lOIDParser) {

      super(lOIDParser);

      mOIDParser = lOIDParser;

      // Build chain of delegates.
      FeatureHandlerBase lCompoundFeatureHandler = new CompoundFeatureHandler();
      lCompoundFeatureHandler.setOIDParser(mOIDParser);
      setDelegate(lCompoundFeatureHandler);

      FeatureHandlerBase lSimpleFeatureHandler = new SimpleFeatureHandler((SequenceAlignment)null);
      lSimpleFeatureHandler.setOIDParser(mOIDParser);
      lCompoundFeatureHandler.setDelegate(lSimpleFeatureHandler);

    } // End constructor

    /**
     * Overridding constructor allows load of contents of a string buffer
     * containing XML.
     */
    public DrivingHandler(StringBuffer lContents, XmlFacadeManager readFacadeManager, OIDParser lOIDParser) {

      super(lOIDParser);

      mRetainAllFeatures = true;

      mOIDParser = lOIDParser;
      mReadFacadeManager = readFacadeManager;

      // Build chain of delegates.
      FeatureHandlerBase lCompoundFeatureHandler = new CompoundFeatureHandler(readFacadeManager);
      lCompoundFeatureHandler.setOIDParser(mOIDParser);
      setDelegate(lCompoundFeatureHandler);

      FeatureHandlerBase lSimpleFeatureHandler = new SimpleFeatureHandler(readFacadeManager);
      lSimpleFeatureHandler.setOIDParser(mOIDParser);
      lCompoundFeatureHandler.setDelegate(lSimpleFeatureHandler);

      // Construct and invoke an event handler which does a SAX parse and
      // reacts to events generated by the input file.
      GenomicsExchangeHandler eventHandler = new GenomicsExchangeHandler(mElementStacker, (FeatureHandlerBase)this);

      mKeepAllFeaturesFromParse = true;
      eventHandler.loadStringBuffer(lContents);
      mKeepAllFeaturesFromParse = false;
    } // End constructor

    //------------------------------PUBLIC INTERFACE
    /** Adds features over a given range, aligning to a given axis. */
    public synchronized void accumulateFeatures( String lSource, OID lAxisOidOfInterest,
                                    Set lRangesOfInterest, boolean lHumanCurated) {

      // Construct and invoke an event handler which does a SAX parse and
      // reacts to events generated by the input file.
      GenomicsExchangeHandler eventHandler = new GenomicsExchangeHandler(mElementStacker,
                                                (FeatureHandlerBase)this);

      // Setup criteria for this load.
      mRangesToCache = lRangesOfInterest;
      mAxisOidToCache = lAxisOidOfInterest;
      mCacheCurations = lHumanCurated;
      mCacheObsoleted = false;

      if (! mRetainAllFeatures)
        jetisonFeaturesIfAxisChanged();

      eventHandler.loadFile(lSource);

    } // End method

    /** Picks up OIDs and other data to ACCESS features later. */
    public synchronized void accumulateFeatures(String lSource) {
      // Construct and invoke an event handler which does a SAX parse and
      // reacts to events generated by the input file.
      GenomicsExchangeHandler eventHandler = new GenomicsExchangeHandler(mElementStacker,
                                                (FeatureHandlerBase)this);

      // Clear any capture criteria
      mRangesToCache = null;
      mAxisOidToCache = null;
      mCacheObsoleted = false;

      mKeepAllFeaturesFromParse = true;
      eventHandler.loadFile(lSource);
      mKeepAllFeaturesFromParse = false;

    } // End method

    /** Adds features aligning to a given axis, which are obsolete roots. */
    public synchronized void accumulateObsoleteFeatures(  String lSource, OID lAxisOidOfInterest) {

      // Construct and invoke an event handler which does a SAX parse and
      // reacts to events generated by the input file.
      GenomicsExchangeHandler eventHandler = new GenomicsExchangeHandler(mElementStacker,
                                                (FeatureHandlerBase)this);

      // Setup criteria for this load.
      mRangesToCache = null;
      mAxisOidToCache = lAxisOidOfInterest;
      mCacheCurations = true;
      mCacheObsoleted = true;

      if (! mRetainAllFeatures)
        jetisonFeaturesIfAxisChanged();

      eventHandler.loadFile(lSource);

    } // End method

    /** Picks up OIDs and other data to ACCESS features later. */
    public synchronized void accumulateFeatureReferenceData(String lSource) {
      // Construct and invoke an event handler which does a SAX parse and
      // reacts to events generated by the input file.
      GenomicsExchangeHandler eventHandler = new GenomicsExchangeHandler(mElementStacker,
                                                (FeatureHandlerBase)this);

      // Clear any capture criteria
      mRangesToCache = null;
      mAxisOidToCache = null;

      eventHandler.loadFile(lSource);

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
    } // Ene method

    /**
     * Returns a feature model, given the OID.
     */
    public FeatureModel getOrLoadModelForOid(OID featureOID) {
      FeatureModel returnModel = null;
      if (mFeatureOIDRegistry.containsKey(featureOID)) {
        returnModel = (FeatureModel)mFeatureOIDRegistry.get(featureOID);
        if ((returnModel == null) && (mSource != null)) {

          SingleFeatureHandler singleFeatureHandler = new SingleFeatureHandler(mSource, mSequenceAlignment, getOIDParser());
          List lFeatureList = singleFeatureHandler.getModelsForCriterion(new ContainsOidCriterion(featureOID));
          FeatureModel rootModel = null;
          for (Iterator it = lFeatureList.iterator(); it.hasNext(); ) {
            rootModel = (FeatureModel)it.next();
            while (rootModel.getParent() != null)
              rootModel = rootModel.getParent();
            this.recursivelyRegisterFeatures((CompoundFeatureModel)rootModel, true); // Only do this once per root hierarchy!
            returnModel = (FeatureModel)mFeatureOIDRegistry.get(featureOID); // Try again!
          } // For all found feature models.
        } // Need to load the model.
      } // Loader knows about feature
      return returnModel;
    } // End method

    /**
     * Drops feature out of cache.  After this, can be reloaded.  Assumes that
     * the OID is for a root feature.  So call with root's OID!
     */
    public void removeFeatureFromCache(OID oid) {
      CompoundFeatureModel model;
      if (null != (model = (CompoundFeatureModel)mFeatureOIDRegistry.get(oid))) {
        eliminateFromFeatureOidRegistry(model);
      } // Got it
    } // End method

    /**
     * Returns list of models to features.  Said features will have description
     * text containing the search target given.
     */
    public List getModelsForDescriptionsWith(String searchTarget) {
      List lReturnList = new ArrayList();
      SingleFeatureHandler singleFeatureHandler = new SingleFeatureHandler(mSource, mSequenceAlignment, getOIDParser());
      lReturnList = singleFeatureHandler.getModelsForCriterion(new ContainsInDescriptionCriterion(searchTarget));

      return lReturnList;
    } // End method

    /**
     * Returns a gene feature model, given an accession number.
     */
    public GeneFeatureModel getModelForGeneAccession(String geneAccession) {
      try {
        OID geneOID = (OID)mGeneAccessionRegistry.get(geneAccession);
        if (geneOID != null)
          return (GeneFeatureModel)getOrLoadModelForOid(geneOID);
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
      OID geneOID = (OID)mTranscriptToGeneRegistry.get(oid);
      if (geneOID != null) {
        GeneFeatureModel model = (GeneFeatureModel)getOrLoadModelForOid(geneOID);
        if (model != null)
          returnGeneName = model.getAnnotationName();
      } // Found a gene for the transcript.

      return returnGeneName;
    } // End method: getGeneForTranscriptOID

    /**
     * Returns a transcript model, given accession number.
     */
    public CompoundFeatureModel getModelForInternalAccession(String accession) {
      try {
        OID featureOID = (OID)mInternalAccessionRegistry.get(accession);
        if (featureOID != null)
          return (CompoundFeatureModel)getOrLoadModelForOid(featureOID);
        else
          return null;
      } catch (ClassCastException cce) {
        FacadeManager.handleException(new IllegalArgumentException("Requested transcript accession for non-transcript feature"));
      } // End catch block for getter.

      return null;
    } // End method: getModelForInternalAccession

    /**
     * Returns the unique set of OIDs referenced by features whose models are contained.
     */
    public Set getReferencedOIDSet() {
      return mReferencedOIDSet;
    } // End method: getReferencedOIDSet

    //------------------------------IMPLEMENTATION OF ExceptionHandler
    /** Simply delegates to the facade manager. */
    public void handleException(Exception lException) {
      FacadeManager.handleException(lException);
    } // End method: handleException

    //------------------------------IMPLEMENTATION OF Template Methods FOR FeatureHandlerBase
    /**
     * Establish criteria for delegation to other handler.  This method decides
     * _before_ the template method is called, whether that template method will
     * even be called, or this processing will be done in the delegated handler.
     */
    public boolean delegate(ElementContext lContext, byte lProcessState) {
      String lName = lContext.currentElement();
      if (lProcessState == FeatureHandlerBase.IN_START_OF_ELEMENT) {
        if (lName.equals(CEFParseHelper.RESULT_SET_ELEMENT) || lName.equals(CEFParseHelper.FEATURE_SET_ELEMENT)) {
          mDelegatedState = true;
        } // Handle in delegate.
      } // Starting an element.
      else if (lProcessState == FeatureHandlerBase.IN_END_OF_ELEMENT) {
        if (lName.equals(CEFParseHelper.RESULT_SET_ELEMENT) || lName.equals(CEFParseHelper.FEATURE_SET_ELEMENT))
          mDelegatedState = false;
      } // Ending an element
      return mDelegatedState;
    } // End method: delegate

    /**
     * Called on subclass when element start tag encountered.
     *
     * @param String lName the name of the element.
     * @param AttributeList lAttrs the collection of tag attributes.
     */
    public void startElementTemplateMethod(ElementContext lContext) {

      // Decode which element is requested, sans any string comparisons.
      int lFoundCode = CEFParseHelper.getCEFParseHelper().translateToElementCode(lContext.currentElement());
      if (lFoundCode == CEFParseHelper.UNINTERESTING_ELEMENT_CODE)
        return;

      if (lFoundCode == CEFParseHelper.ANNOTATION_CODE) {
        mGeneOID = mOIDParser.parseFeatureOIDTemplateMethod(
            (String)(lContext.ancestorAttributesNumber(0).get(ID_ATTRIBUTE)));
        mCompoundModels.clear();
        mInGene = true;
      } // Pickup gene's attributes of interest.
      else if (lFoundCode == CEFParseHelper.ANNOTATION_OBSOLETION_CODE) {
        // All information required to build the annotation obsoletion
        // "gene" is present in start tag.

        String lIdStr = (String)(lContext.ancestorAttributesNumber(0).get(CEFParseHelper.ID_ATTRIBUTE));
        String lObsoletedIdStr = (String)(lContext.ancestorAttributesNumber(0).get("obsoleted_id"));
        String lQuerySeqRelIdStr = (String)(lContext.ancestorAttributesNumber(0).get("query_seq_relationship_id"));
        if ((lIdStr == null) || (lObsoletedIdStr == null) || (lQuerySeqRelIdStr == null)) {
          FacadeManager.handleException(new IllegalArgumentException("Required attributes not supplied for "
            +lContext.currentElement()
            +" continuing with other tags"));
          return;  // Carry on processing with other workspace tags.
        } // IDs not given.

        // id="the workspace thing that obsoletes something NOT in ws."
        OID lObsoletingOid = mOIDParser.parseFeatureOIDTemplateMethod(lIdStr);

        // obsoleted_id="the non-workspace thing (promoted) that gets obsoleted"
        //   That makes this OID an evidence.
        OID lObsoletedOid = mOIDParser.parseEvidenceOIDTemplateMethod(lObsoletedIdStr);

        // query_seq_relationship_id="the axis to which obsoleting and obsoleted should align"
        //   That makes this an axis OID (previously: contig OID).
        OID lGenomicAxisOid = mOIDParser.parseContigOIDTemplateMethod(lQuerySeqRelIdStr);

        if (lObsoletingOid.isScratchOID() && (! lObsoletedOid.isScratchOID())) {
          GeneFeatureModel model = new GeneFeatureModel(lObsoletingOid, lGenomicAxisOid, mReadFacadeManager);
          model.setDescription("GENE WITH NO TRANSCRIPTS");
          model.setDiscoveryEnvironment("Curation");
          model.setCurated(true);
//BIG ASSUMPTION HERE>
          // Range is 0-10.
          model.setStart(0);
          model.setEnd(10);

          // NOTE: need to get this fixed properly....
          List replacedDatas = new ArrayList();
          List replacedOids = new ArrayList();
          replacedOids.add(lObsoletedOid);

          String type = ReplacementRelationship.TYPE_OBSOLETE;
          ReplacedData replacement = new ReplacedData(replacedOids, type, "No transcripts on gene, but gene is to be obsoleted");
          replacedDatas.add(replacement);
          model.addReplacedList(replacedDatas);
// NOTE: assuming cache criteria is picked up for obsoleted features pass.
          // Include model in registry regardless of range, since range is
          // nonsense here.
          recursivelyRegisterFeatures(model, meetsCacheCriteria(model));

          // Register this gene as a root curated feature.
          mRootCuratedFeatures.add(model.getOID());

        } // Correct namespace.
        else {
          FacadeManager.handleException(new IllegalStateException(lContext.currentElement()+" "+lObsoletingOid+" not in scratch namespace.  Ignored."));
        } // Incorrect namespace.
      } // Pickup obsoleter's attributes of interest.
      else if (lFoundCode == CEFParseHelper.SEQ_CODE) {
        // Will add the length of sequence, indexed by the OID of the sequence.
        String seqLength = (String)(lContext.ancestorAttributesNumber(0).get(LENGTH_ATTRIBUTE));
        String seqId = (String)(lContext.ancestorAttributesNumber(0).get(ID_ATTRIBUTE));
        if (seqId != null && seqLength != null) {
          OID seqOid = mOIDParser.parseFeatureOIDTemplateMethod(seqId);
          try {
            Integer lengthOfSeq = new Integer(seqLength);
            mSequenceLengthMap.put(seqOid, lengthOfSeq);
          } catch (NumberFormatException nfe) {
            // Do nothing: the data was not avail, so do not add to map.
          } // End catch block.
        } // Got sufficent info.
      } // Get info from seq's start tag.
      else if (lFoundCode == CEFParseHelper.RESIDUES_CODE) {
        String seqId = (String)(lContext.ancestorAttributesNumber(1).get(ID_ATTRIBUTE));
        if (seqId != null) {
          OID seqOid = mOIDParser.parseFeatureOIDTemplateMethod(seqId);
          mSubjectSeqOids.add(seqOid);
        } // Got seq id.
      } // Pickup sequence oid.

    } // End method: startElementTemplateMethod

    /**
     * Called on subclass when element end tag was encountered.
     *
     * @param String lName the element which just ended.
     */
    public void endElementTemplateMethod(ElementContext lContext) {

      // Decode which element is requested, sans any string comparisons.
      int lFoundCode = CEFParseHelper.getCEFParseHelper().translateToElementCode(lContext.currentElement());
      if (lFoundCode == CEFParseHelper.UNINTERESTING_ELEMENT_CODE)
        return;

      String lName = lContext.currentElement();

      // If the end of a set occurred, we emerge from delegated,
      // and collect the set model.
      if ((lFoundCode == CEFParseHelper.RESULT_SET_CODE) || (lFoundCode == CEFParseHelper.FEATURE_SET_CODE)) {
        // Collect set model.
        if (lFoundCode == CEFParseHelper.RESULT_SET_CODE) {
          ((CompoundFeatureHandler)getDelegate()).setAnalysisSource(mAnalysisSource);
          ((CompoundFeatureHandler)getDelegate()).setResultSetType(mResultSetType);
        } // Got a result set.
        CompoundFeatureModel model = ((CompoundFeatureHandler)getDelegate()).createModel();

        if (withinRequiredRange(model)) {
          recursivelyRegisterFeatures(model, meetsCacheCriteria(model));

          // Either save this as just another compound model if it
          // is part of a gene;
          // or save it as its own root, if it is a gene-less
          // human curated feature;
          // or save it as its own root, if it is a compound
          // precomputed feature.
          if (mInGene)
            mCompoundModels.add(model);
          else if (lFoundCode == CEFParseHelper.FEATURE_SET_CODE) {
            mRootCuratedFeatures.add(model.getOID());
            model.setParent(null);
          } // Root feature set.
          else if (lFoundCode == CEFParseHelper.RESULT_SET_CODE) {
            mRootPrecomputedFeatures.add(model.getOID());
            model.setParent(null);
          } // Root precompute.

        } // Within the range?
      } // End of a set element in the delegate.
      else if (lFoundCode == CEFParseHelper.ANNOTATION_CODE) {
        // Retrieve certain info that applies to the element.
        mGeneReplacedList = destructivelyRetrieveReplaced();
        mPropertySourceList = destructivelyRetrievePropertySources();

        // Time to build the annotation model.
        OID lGenomicAxisOID = ((CompoundFeatureHandler)getDelegate()).getOIDOfAlignment();
        GeneFeatureModel model = new GeneFeatureModel(mGeneOID, lGenomicAxisOID, mReadFacadeManager);

        model.setAnnotationName(mAnnotationName);
        mAnnotationName = null;
        model.setDescription(mDescription);
        mDescription = null;

        if (mAnnotationSource != null)
          model.setDiscoveryEnvironment(mAnnotationSource);
        else {
          if (model.getOID().isScratchOID()){
            model.setDiscoveryEnvironment("Curation");
          } else{
            model.setDiscoveryEnvironment("Promoted");
          } // Not scratch.
        } // No annotation source.
        mAnnotationSource = null;

        // Add all children to the model which have been encountered here.
        CompoundFeatureModel childModel = null;
        for (Iterator it = mCompoundModels.iterator(); it.hasNext(); ) {
          childModel = (CompoundFeatureModel)it.next();
          model.addChild(childModel);
          childModel.setParent(model);
          mTranscriptToGeneRegistry.put(childModel.getOID(), mGeneOID);
        } // For all contained models.

        GenomicEntityComment[] comments = new GenomicEntityComment[mCommentList.size()];
        mCommentList.toArray(comments);
        model.setComments(comments);
        comments = null;

        model.addReplacedList(mGeneReplacedList);

        model.addPropertySources(mPropertySourceList);

        model.setCurated(true);  // Genes are always curated features.

        if (withinRequiredRange(model)) {
          recursivelyRegisterFeatures(model, meetsCacheCriteria(model));

          // Register this gene as a root curated feature.
          mRootCuratedFeatures.add(model.getOID());
        } // Keep if within required range.

        model = null;
        mPropertySourceList.clear();
        mGeneReplacedList.clear();
        mCompoundModels.clear();
        mCommentList.clear();

        mInGene = false;
      } // End of a gene element.
      else if (lFoundCode == CEFParseHelper.COMMENTS_CODE) {

        mCommentText = textOfCurrentElement(lName);

        // Now have comment text and comment attributes.
        if (mInGene) {
          Map lAttrs = lContext.ancestorAttributesNumber(0);
          String lCommentAuthor = (String)lAttrs.get(AUTHOR_ATTRIBUTE);
          String lCommentDate = (String)lAttrs.get(DATE_ATTRIBUTE);
          GenomicEntityComment comment = createGenomicEntityComment(  lCommentAuthor,
                                                                      lCommentDate,
                                                                      mCommentText);
          if (comment != null)
            mCommentList.add(comment);

        } // Within gene, collect comments.
        mCommentText = null;

      } // End of a comment element.
      else if ((lFoundCode == CEFParseHelper.PROGRAM_CODE) && lContext.ancestorNumber(1).equals(CEFParseHelper.COMPUTATIONAL_ANALYSIS_ELEMENT))
        mAnalysisSource = textOfCurrentElement(lName);
      else if ((lFoundCode == CEFParseHelper.TYPE_CODE) && lContext.ancestorNumber(1).equals(CEFParseHelper.COMPUTATIONAL_ANALYSIS_ELEMENT))
        mResultSetType = textOfCurrentElement(lName);
      else if (lFoundCode == CEFParseHelper.COMPUTATIONAL_ANALYSIS_CODE) {
        ((CompoundFeatureHandler)getDelegate()).setAnalysisSource(null);
        ((CompoundFeatureHandler)getDelegate()).setResultSetType(null);  // Clear this for next computational analysis pass.
        mAnalysisSource = null;
        mResultSetType = null;
      } // End of a computational analysis.
      else if (lFoundCode == CEFParseHelper.NAME_CODE) {
        if (lContext.ancestorNumber(1).equals(CEFParseHelper.ANNOTATION_ELEMENT) || lContext.ancestorNumber(1).equals(CEFParseHelper.FEATURE_SET_ELEMENT))
          mAnnotationName = textOfCurrentElement(lName);
      } // End of Name element.
      else if (lFoundCode == CEFParseHelper.DESCRIPTION_CODE)
        mDescription = textOfCurrentElement(lName);
      else if (lFoundCode == CEFParseHelper.ANNOTATION_SOURCE_CODE)
        mAnnotationSource = textOfCurrentElement(lName);
      else if (lFoundCode == CEFParseHelper.GAME_CODE) {

        // Build registry of models by traversing model hierarchy.
        buildModelRegistry();

      } // End of whole document.

      // Clear the buffer mapped to this element name, if there is one.
      clearMappedBuffer(lName);

    } // End method: endElementTemplateMethod

    /**
     * Called on subclass for character content.
     *
     * @param char[] lCharacters the whole buffer being constructed.
     * @param int lStart the starting point within the buffer.
     * @param int lLength the ending point within the buffer.
     */
    public void charactersTemplateMethod(char[] lCharacters, int lStart, int lLength, ElementContext lContext) {
      // Do nothing.
    } // End method: charactersTemplateMethod

    /**
     * Returning the fill buffer map allows values to be assigned to buffers
     * local to this handler.
     */
    public Map getFillBufferMapTemplateMethod() {
      if (mFillBufferMap == null)
        initializeFillBufferMap();

      return mFillBufferMap;
    } // End method: getFillBufferMapTemplateMethod

    /**
     * Find all registered human curated features which align to the
     * axis whose oid is given.
     */
    List findHumanCuratedFeaturesOnAxis(OID axisOID, Set rangesOfInterest) {
      return findFeaturesOnAxis(mRootCuratedFeatures, axisOID, rangesOfInterest);
    } // End method: findHumanCuratedFeaturesOnAxis

    /**
     * Find all registered precomputed features which align to the
     * axis whose oid is given.
     */
    List findPrecomputedFeaturesOnAxis(OID axisOID, Set rangesOfInterest) {
      return findFeaturesOnAxis(mRootPrecomputedFeatures, axisOID, rangesOfInterest);
    } // End method: findPrecomputedFeaturesOnAxis

    /**
     * Find all obsoleted root features which refer to the axis whose OID is given.
     */
    List findObsoleteFeaturesOnAxis(OID axisOID) {
      List features = new ArrayList();
      OID featureAxisOID = null;
      OID featureOID = null;
      FeatureModel validFeature = null;

      // looking for only those axis alignments that have the same axis oid as the input
      // axisOID
      for(Iterator it = mRootCuratedFeatures.iterator(); it.hasNext(); ) {
        featureOID = (OID)it.next();

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

      /*
            List lReturnList = new ArrayList();
            SingleFeatureHandler singleFeatureHandler = new SingleFeatureHandler(mSource, mSequenceAlignment, getOIDParser());
            lReturnList = singleFeatureHandler.getModelsForCriterion(new ObsoleteOnAxisCriterion(axisOID));
            return lReturnList;
      */
    } // End method: findObsoleteFeaturesOnAxis

    //----------------------------HELPER METHODS
    /**
     * If the axis given is not the one currently being cached, flush the
     * old cached feature models, and the given axis WILL be cached instead
     * of the old one.
     */
    private void jetisonFeaturesIfAxisChanged() {
      if ((mAxisOidToCache != null) && mAxisOidToCache.equals(mPreviousAxisOidToCache))
        return;

      mPreviousAxisOidToCache = mAxisOidToCache;

      FeatureModel lNextModel = null;
      List lJetisonList = new ArrayList();
      lJetisonList.addAll(mFeatureOIDRegistry.values());
      for (Iterator it = lJetisonList.iterator(); it.hasNext(); ) {
        lNextModel = (FeatureModel)it.next();
        eliminateFromFeatureOidRegistry(lNextModel);
      } // For all root features currently loaded.

    } // End method

    /**
     * Scan a list, testing the parent OID of its children, to see if they match
     * an axis OID given as parameter.  Return the list of those that DO match.
     */
    private List findFeaturesOnAxis(Collection containing, OID axisOID, Set rangesOfInterest) {
      List features = new ArrayList();
      OID featureAxisOID = null;
      OID featureOID = null;
      FeatureModel validFeature = null;
      Range nextRange = null;
      boolean featureAdded = false;

      // looking for only those axis alignments that have the same axis oid as the input
      // axisOID
      for(Iterator it = containing.iterator(); it.hasNext(); ) {
        featureOID = (OID)it.next();

        // Will take advantage of fact that applicable features are already IN
        // memory.
        if (null == (validFeature = getModelForOidIfPreviouslyLoaded(featureOID)))
          continue;

        featureAxisOID = validFeature.getAxisOfAlignment();

        if (axisOID.equals(featureAxisOID)) {
          featureAdded = false;
          for (Iterator rangeIterator = rangesOfInterest.iterator(); (!featureAdded) && rangeIterator.hasNext(); ) {
            nextRange = (Range)rangeIterator.next();
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
     * Sets up the mapping of element names to buffers to be filled
     * with their contents.
     */
    private void initializeFillBufferMap() {
      mFillBufferMap = new HashMap();

      // For precomputes...
      mFillBufferMap.put(CEFParseHelper.PROGRAM_ELEMENT, null);
      mFillBufferMap.put(CEFParseHelper.TYPE_ELEMENT, null);

      // For human curations...
      mFillBufferMap.put(CEFParseHelper.ANNOTATION_SOURCE_ELEMENT, null);
      mFillBufferMap.put(CEFParseHelper.NAME_ELEMENT, null);
      mFillBufferMap.put(CEFParseHelper.DESCRIPTION_ELEMENT, null);
      mFillBufferMap.put(CEFParseHelper.COMMENTS_ELEMENT, null);

    } // End method: initializeFillBufferMap

    /**
     * Creates mappings of OIDs and accession numbers so that features
     * may be retrieved later.
     */
    private void buildModelRegistry() {
      // First, look at root human curated features.
      OID featureOID = null;
      CompoundFeatureModel compoundModel = null;
      boolean registerModels = false;
      for (Iterator it = mRootCuratedFeatures.iterator(); it.hasNext(); ) {
        featureOID = (OID)it.next();
        compoundModel = (CompoundFeatureModel)mFeatureOIDRegistry.get(featureOID);
        if (compoundModel != null) {
          registerModels = meetsCacheCriteria(compoundModel);
          recursivelyRegisterFeatures(compoundModel, registerModels);
        } // Must register.
      } // For all curated root features.

      // Next look at precomputes.  There are no special accession numbers
      // to deal with here.
      for (Iterator it = mRootPrecomputedFeatures.iterator(); it.hasNext(); ) {
        featureOID = (OID)it.next();
        compoundModel = (CompoundFeatureModel)mFeatureOIDRegistry.get(featureOID);
        if (compoundModel != null) {
          registerModels = meetsCacheCriteria(compoundModel);
          recursivelyRegisterFeatures(compoundModel, registerModels);
        } // Must register
      } // For all precomputed root features.

    } // End method: buildModelRegistry

    /**
     * Given a compound feature model, register its children as features, for
     * lookup by their oids.  It also keeps track of OIDs referenced BY these
     * features.
     */
    private void recursivelyRegisterFeatures(CompoundFeatureModel compoundModel, boolean registerModels) {

      String annotationName = compoundModel.getAnnotationName();
      if (compoundModel instanceof GeneFeatureModel) {
        if (! mGeneAccessionRegistry.containsKey(annotationName))
          mGeneAccessionRegistry.put(annotationName, compoundModel.getOID());
      } // Got a gene.
      else if (compoundModel instanceof CompoundFeatureModel) {
        if (! mInternalAccessionRegistry.containsKey(annotationName))
          mInternalAccessionRegistry.put(annotationName, compoundModel.getOID());
      } // Got a transcript

      // Register for retrieval by OID.
      registerOidAndOrModel(compoundModel, registerModels);

      // Register OID of reference.
      mReferencedOIDSet.add(compoundModel.getAxisOfAlignment());

      FeatureModel model = null;

      for (Iterator it = compoundModel.getChildren().iterator(); it.hasNext(); ) {
        model = (FeatureModel)it.next();
        registerOidAndOrModel(model, registerModels);

        if (model instanceof CompoundFeatureModel)
          recursivelyRegisterFeatures((CompoundFeatureModel)model, registerModels);
        else
          // Register referenced OID, so that all applicable axes may be tracked.
          mReferencedOIDSet.add(model.getAxisOfAlignment());

      } // For all children

    } // End method: recursivelyRegisterFeatures

    /**
     * Tests whether the model provided falls within the range to which
     * this handle applies.  That range could be the entire axis, or
     * it could be just an aligned portion.
     */
    private boolean withinRequiredRange(FeatureModel model) {
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
    private void registerOidAndOrModel(FeatureModel lModel, boolean lRegisterModels) {
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
    private FeatureModel getModelForOidIfPreviouslyLoaded(OID featureOID) {
      return (FeatureModel)mFeatureOIDRegistry.get(featureOID);
    } // End method

    /** Tests input model.  Is it one to cache this time through load? */
    private boolean meetsCacheCriteria(FeatureModel lModel) {

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
    private void eliminateFromFeatureOidRegistry(CompoundFeatureModel lModel) {
      // Recurse for child objects.
      for (Iterator it = lModel.getChildren().iterator(); it.hasNext(); ) {
        eliminateFromFeatureOidRegistry((FeatureModel)it.next());
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
    private void eliminateFromFeatureOidRegistry(FeatureModel lModel) {
      if (lModel == null)
        return;

      // Recurse for child objects.
      if (lModel instanceof CompoundFeatureModel) {
        for (Iterator it = ((CompoundFeatureModel)lModel).getChildren().iterator(); it.hasNext(); ) {
          eliminateFromFeatureOidRegistry((FeatureModel)it.next());
        } // For all children.
      } // May have children

      // Remove the old model ref, but keep the key in the registry.
      if (mFeatureOIDRegistry.containsKey(lModel.getOID())) {
        mFeatureOIDRegistry.remove(lModel.getOID());
        mFeatureOIDRegistry.put(lModel.getOID(), null);
      } // Found ref.
    } // End method

} // End class: DrivingHandler

/*
 $Log$
 Revision 1.63  2002/11/07 16:06:20  lblick
 Removed obsolete imports and unused local variables.

 Revision 1.62  2002/09/30 15:19:49  lblick
 Merge BRA_GB_R4-2 branch with HEAD.

 Revision 1.61.2.3  2002/09/24 19:03:34  lfoster
 ~2 seconds for 600K of features.

 Revision 1.61.2.2  2002/09/24 17:31:23  lfoster
 40 seconds for 600K of features.

 Revision 1.61.2.1  2002/09/23 22:41:03  lfoster
 First cut at de-caching mechanism.  NOT TESTED.  Does not break basic load/unload.

 Revision 1.61  2002/07/11 13:28:04  lfoster
 Added code to support a property "subject_seq_length".

 Revision 1.60  2002/06/24 21:02:35  lfoster
 Moved the ArrayListElementStacker down to sax_support package.

 Revision 1.59  2002/05/21 15:09:56  lfoster
 Only add subj seq id to driving handler if there is a residues tag with it.

 Revision 1.58  2002/05/21 14:25:49  lfoster
 Pull sequence back for subject.

 Revision 1.57  2002/04/05 19:48:43  lfoster
 Removed refs to FacadeManager from sax support classes.  Wrapped facademanager handleexception calls in instance method calls.

 Revision 1.56  2002/04/05 19:06:55  lfoster
 Moved 8 classes from xml down to xml.sax_support.  Removed dep of PropertySource on abstract facades.

 Revision 1.55  2002/03/25 22:53:42  lfoster
 Removed unneccessary dependencies.

 Revision 1.54  2002/01/21 19:18:03  lfoster
 Switched from Linked List to Array List for element stack.  Caching the seq relationship
 type attribute rather than prowling the element stack looking for it.

 Revision 1.53  2002/01/16 21:50:48  lfoster
 Converted ArrayList collections to HashSet's to eliminate time-expensive "contains" operation.

 Revision 1.52  2001/10/15 14:56:31  lfoster
 Logging in read change for obsoletion of empty gene.

 Revision 1.51  2001/09/10 16:57:15  lfoster
 No longer testing axis OID when  deciding whether to cache obsoleted features.

 Revision 1.50  2001/09/06 18:19:15  lfoster
 Altered fix for returning entities instead of models.  Moved it back down to the feature loader.

 Revision 1.49  2001/09/06 15:23:58  lfoster
 Fixed redundant addition of curated features.

 Revision 1.48  2001/09/06 14:51:23  lfoster
 Fixed null-pointer problem with cached axis not having been set.

 Revision 1.47  2001/09/05 21:26:16  lfoster
 No obsoleted features returned through Genomic Axis loader; fleshed out implementation of new Workspace Facade.

 Revision 1.46  2001/08/28 13:26:02  lfoster
 Switched over to SAX version 2.0 in all parsers.

 Revision 1.45  2001/08/14 18:48:18  jbaxenda
 Bug fix to set OID parser in path used by promotion .gbw load.

 Revision 1.44  2001/07/24 15:21:05  lfoster
 Changed DTD version to reflect browser version number.

 Revision 1.43  2001/07/12 21:18:47  lfoster
 Search fixed for Sequence Alignment files.  Full-batery tested for search, bookmark and load.

 Revision 1.42  2001/07/06 15:47:47  lfoster
 Driving Handler being reverted to one with changes for navigation.

 Revision 1.40  2001/07/03 22:09:57  lfoster
 Fixing nav-to-HSP/Exon bug.

 Revision 1.39  2001/07/03 14:19:02  lfoster
 Registering referenced axis OID, regardless whether models are collected or not

 Revision 1.38  2001/07/02 22:49:58  lfoster
 Keeping all features resulting from ANY string buffer load.

 Revision 1.37  2001/06/28 22:42:57  lfoster
 Fixed bug whereby no optimization took place for navigation loading.

 Revision 1.36  2001/06/09 21:03:06  lfoster
 Removed jetisoning of features in event of load from StringBuffer; placed output code for unknown request type in genomic axis facade.

 Revision 1.35  2001/06/09 20:20:18  lfoster
 Once more.  Comitting the changes with the model dumping.

 Revision 1.30  2001/06/06 22:49:03  lfoster
 Clearing features from "other" axes, whenever an axis is loaded.

 Revision 1.29  2001/05/30 22:44:57  lfoster
 Adding implementation for first-cut search for new feature type searching.

 Revision 1.28  2001/05/30 13:56:35  lfoster
 Finally: comitting change to optimize feature loading by only retaining models from range loaded.

 Revision 1.27  2001/05/23 18:46:35  lfoster
 Eliminated unused references.

 Revision 1.26  2001/05/23 18:25:28  lfoster
 All "gets" from the OID registry are through a method now.  Method checks for NULL and then calls to populate model via a scan if necessary.

 Revision 1.25  2001/05/22 21:22:43  lfoster
 Factored out the LinkedListElementStacker so that the element stacker would not be multiply defined as an inner class in many other classes.

 Revision 1.24  2001/05/22 18:54:53  lfoster
 Moved some genomic axis facade code down into the loader.

 Revision 1.23  2001/05/14 03:21:29  lfoster
 Modified comments handling to use constructors with intended default behavior.

 Revision 1.22  2001/05/13 04:15:37  lfoster
 Changed handling of comments so no date->no comment.

 Revision 1.21  2001/05/13 03:48:33  lfoster
 Fixed some uses of unspecified attributes, that did not first test them for null.

 Revision 1.20  2001/05/11 21:23:12  lfoster
 Eliminated constants defined in FeatureHandlerBase in favor of those in CEFParseHelper.  Converted more loaders/handlers to use the hashmap/code method to identify elements rather than string comparisons.

 */
