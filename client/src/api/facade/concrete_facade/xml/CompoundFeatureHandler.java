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
package api.facade.concrete_facade.xml;

import api.facade.concrete_facade.xml.model.CompoundFeatureModel;
import api.facade.concrete_facade.xml.model.FeatureModel;
import api.facade.concrete_facade.xml.model.NonHierarchicalFeatureModel;
import api.facade.concrete_facade.xml.model.SimpleFeatureModel;
import api.facade.concrete_facade.xml.sax_support.CEFParseHelper;
import api.facade.concrete_facade.xml.sax_support.ElementContext;
import api.facade.concrete_facade.xml.sax_support.FeatureHandlerBase;
import api.facade.concrete_facade.xml.sax_support.OIDParser;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.GenomicEntityComment;
import api.stub.data.OID;

import java.util.*;

/**
 * Deals with compound features--those that may contain other features.
 * Both precomputes and human curations.
 */
public class CompoundFeatureHandler extends FeatureHandlerBase {
    //-----------------------------------------MEMBER VARIABLES
    private String mComputationalAnalysisProgram = null;
    private OID mFeatureOID = null;
    private boolean mDelegatedState = false;
    private List mSubmodels = new ArrayList();
    private List mCommentList = new ArrayList();
    private Map mFillBufferMap = null;
    private String mFeatureSetType = null;
    private String mComputationalAnalysisType = null;
    private String mResultSetType = null;
    private String mAnnotationName = null;
    private String mAnnotationSource = null;
    private String mDescription = null;
    private String mScore = null;
    private String mOutputValue = null;
    private String mOutputName = null;
    private String mSummaryExpect = null;
    private String mCommentText = null;
    private String mIndividualExpect = null;
    private boolean mIncompleteCompoundFeature = false;

    private SequenceAlignment mSequenceAlignment = null;
    private XmlFacadeManager mReadFacadeManager = null;

    //-----------------------------------------CONSTRUCTORS
    /**
     * Builds this handler, and its next-level delegated handler.
     */
    public CompoundFeatureHandler() {

      super();

    } // End constructor.

    /**
     * Override constructor to allow provision of a refer-back facade manager.
     * Said manager is used in creational methods.
     */
    public CompoundFeatureHandler(XmlFacadeManager readFacadeManager) {
      super();

      mReadFacadeManager = readFacadeManager;

    } // End constructor.

    //-----------------------------------------PUBLIC INTERFACE
    /**
     * Used by precomputes for discovery environment.
     */
    public void setAnalysisSource(String lSource) {
        mComputationalAnalysisProgram = lSource;
    } // End method: setAnalysisSource

    /**
     * Allows set of analysis type of compound features.
     */
    public void setResultSetType(String lType) {
        mComputationalAnalysisType = lType;
    } // End method: setResultSetType

    /**
     * Allows external set of parser for OIDs.  OIDs need to
     * be parsed because of namespaces.
     */
    public void setOIDParser(OIDParser lOIDParser) {
        super.setOIDParser(lOIDParser);
    } // End method: setOIDParser

    /**
     * Builds the feature model from the "parts" available.
     */
    public CompoundFeatureModel createModel() {

        if (isHierarchyImpliedBySubmodels())
            return createHierarchicalModel();
        else
            return createNonHierarchicalModel();

    } // End method: createModel

    /**
     * Returns OID of the axis against which these features will align.
     */
    public OID getOIDOfAlignment() {
        // Must return same from suburdinate handler.
        SimpleFeatureHandler delegate = (SimpleFeatureHandler)getDelegate();
        if (delegate != null)
            return delegate.getOIDOfAlignment();
        else
            return null;

    } // End method: getOIDOfAlignment;

    //------------------------------IMPLEMENTATION OF ExceptionHandler
    /** Simply delegates to the facade manager. */
    public void handleException(Exception lException) {
      FacadeManager.handleException(lException);
    } // End method: handleException

    //-----------------------------------------IMPLEMENTATIONS OF TEMPLATE METHODS
    /**
     * Establish criteria for delegation to other handler.  This method decides
     * _before_ the template method is called, whether that template method will
     * even be called, or this processing will be done in the delegated handler.
     */
    public boolean delegate(ElementContext lContext, byte lProcessState) {
        String lName = lContext.currentElement();
        if (lProcessState == FeatureHandlerBase.IN_START_OF_ELEMENT) {
            if (lName.equals(CEFParseHelper.FEATURE_SPAN_ELEMENT) || lName.equals(CEFParseHelper.RESULT_SPAN_ELEMENT))
                mDelegatedState = true;

        } // Starting an element.
        else if (lProcessState == FeatureHandlerBase.IN_END_OF_ELEMENT) {
            if (lName.equals(CEFParseHelper.FEATURE_SPAN_ELEMENT) || lName.equals(CEFParseHelper.RESULT_SPAN_ELEMENT))
                mDelegatedState = false;

        } // Ending an element

        return mDelegatedState;
    } // End method: delegate

    /**
     * Called on subclass when element start tag encountered.
     *
     * @param ElementContext lContext all that is known about current element..
     */
    public void startElementTemplateMethod(ElementContext lContext) {

        // Decode which element is requested, sans any string comparisons.
        int foundCode = CEFParseHelper.getCEFParseHelper().translateToElementCode(lContext.currentElement());
        if (foundCode == CEFParseHelper.UNINTERESTING_ELEMENT_CODE)
            return;

        if ((foundCode == CEFParseHelper.FEATURE_SET_CODE) || (foundCode == CEFParseHelper.RESULT_SET_CODE)) {
            mIncompleteCompoundFeature = false;
            String idStr = (String)(lContext.ancestorAttributesNumber(0).get(ID_ATTRIBUTE));
            if (idStr != null)
                mFeatureOID = getOIDParser().parseFeatureOIDTemplateMethod(idStr);
        } // Found start of a set.

    } // End method: startElementTemplateMethod

    /**
     * Called on subclass when element end tag was encountered.
     *
     * @param ElementContext lContext all that is known about current element..
     */
    public void endElementTemplateMethod(ElementContext lContext) {

        String lName = lContext.currentElement();

        // Decode which element is requested, sans any string comparisons.
        int foundCode = CEFParseHelper.getCEFParseHelper().translateToElementCode(lContext.currentElement());
        if (foundCode == CEFParseHelper.UNINTERESTING_ELEMENT_CODE)
            return;

        if ((foundCode == CEFParseHelper.FEATURE_SPAN_CODE) || (foundCode == CEFParseHelper.RESULT_SPAN_CODE)) {
            // Collect span model.
            FeatureModel lModel = ((SimpleFeatureHandler)getDelegate()).createModel();
            if (lModel != null)
                mSubmodels.add(lModel);
        } // Found end of a span.
        else if (foundCode == CEFParseHelper.TYPE_CODE) {
            if (lContext.ancestorNumber(1).equals(CEFParseHelper.FEATURE_SET_ELEMENT))
                mFeatureSetType = textOfCurrentElement(lName);
            else if (lContext.ancestorNumber(1).equals(CEFParseHelper.OUTPUT_ELEMENT))
                mOutputName = textOfCurrentElement(lName);
            else if (lContext.ancestorNumber(1).equals(CEFParseHelper.RESULT_SET_ELEMENT))
                mResultSetType = textOfCurrentElement(lName);
        } // Found feature set type.
        else if (foundCode == CEFParseHelper.DESCRIPTION_CODE) {
            mDescription = textOfCurrentElement(lName);
        } // Got a description tag.
        else if (foundCode == CEFParseHelper.SCORE_CODE) {
            mScore = textOfCurrentElement(lName);
        } // Got a score
        else if (foundCode == CEFParseHelper.NAME_CODE) {
            if (lContext.ancestorNumber(1).equals(CEFParseHelper.FEATURE_SET_ELEMENT))
                mAnnotationName = textOfCurrentElement(lName);
        } // Found a name.
        else if (foundCode == CEFParseHelper.VALUE_CODE) {
            if (lContext.ancestorNumber(1).equals(CEFParseHelper.OUTPUT_ELEMENT))
                mOutputValue = textOfCurrentElement(lName);
        } // Found a value.
        else if (foundCode == CEFParseHelper.ANNOTATION_SOURCE_CODE)
            mAnnotationSource = textOfCurrentElement(lName);
        else if (foundCode == CEFParseHelper.OUTPUT_CODE) {
            // All necessary stuff is back from output.
            if (mOutputName != null) {
                if (mOutputName.equals(SUMMARY_EXPECT_OUTPUT))
                    mSummaryExpect = mOutputValue;
                else if (mOutputName.equals(INDIVIDUAL_EXPECT_OUTPUT))
                    mIndividualExpect = mOutputValue;
            } // Got an output name.

            // Clear the output stuff.
            mOutputName = null;
            mOutputValue = null;
        } // End of output.
        else if (foundCode == CEFParseHelper.COMMENTS_CODE) {
            mCommentText = textOfCurrentElement(lName);

            // Now have comment text and comment attributes.
            Map lAttrs = lContext.ancestorAttributesNumber(0);
            String lCommentAuthor = (String)lAttrs.get(AUTHOR_ATTRIBUTE);
            String lCommentDate = (String)lAttrs.get(DATE_ATTRIBUTE);
            GenomicEntityComment comment = createGenomicEntityComment(  lCommentAuthor,
                                                                        lCommentDate,
                                                                        mCommentText);
            if (comment != null)
                mCommentList.add(comment);

            mCommentText = null;

        } // End of a comment element.

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
    public void charactersTemplateMethod(char[] lCharacters, int lStart, int lLength,
        ElementContext lContext) {

        // DO nothing.

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

    //--------------------------------------------HELPER METHODS
    /** Tests sub models of this one, to see if they imply true parent/child. */
    private boolean isHierarchyImpliedBySubmodels() {
        boolean returnValue = true;

        // Iterate over submodels, looking for any that are non-hierarchical.
        FeatureModel nextModel = null;
        for (Iterator it = this.mSubmodels.iterator(); it.hasNext(); ) {
            nextModel = (FeatureModel)it.next();
            if (nextModel instanceof NonHierarchicalFeatureModel) {
                returnValue = false;
            } // Not a hierarchy.
            else if (returnValue == false) {
                throw new IllegalArgumentException("ERROR: "+mFeatureOID+" has an invalid set of span features.  Some are 'Details', and some are not.");
            } // Mixed.  Error.
        } // For all submodels.

        if ((! returnValue) && (mSubmodels.size() != 1))
            throw new IllegalArgumentException("ERROR: "+mFeatureOID+" has wrong number of span features for a non-hierarchical feature.  It should contain only one.");

        return returnValue;
    } // End method

    /**
     * Builds an XML model object based on assumed parent/child hierarchy.
     */
    private CompoundFeatureModel createHierarchicalModel() {
        // If any child feature was not complete, this one is not either.
        if (mIncompleteCompoundFeature)
            return null;
/*
        OID lPreviousAlignment = mOIDOfAlignment;

          if ((lPreviousAlignment != null) && (! mOIDOfAlignment.equals(lPreviousAlignment))) {
          } // Different OID.
*/

        CompoundFeatureModel model = new CompoundFeatureModel(mFeatureOID, getOIDOfAlignment(), mReadFacadeManager);
        model.setAnnotationName(mAnnotationName);
        mAnnotationName = null; // Avoid concatenation multiple instances.
        model.setDescription(mDescription);
        mDescription = null;

        // We expect the first condition to be met when we process
        // human curations, and the second condition when we process precomputes.
        if (mComputationalAnalysisProgram == null) {
            if (mAnnotationSource != null)
                model.setDiscoveryEnvironment(mAnnotationSource);
            else {
                if (model.getOID().isScratchOID()){
                    model.setDiscoveryEnvironment("Curation");
               } else{
                    model.setDiscoveryEnvironment("Promoted");
               } // Not scratch.
            } // No annotation source.
            model.setAnalysisType(mFeatureSetType);
            model.setCurated(true);
        } // No analysis type
        else {

            model.setAnalysisType(decideResultSetType());
            model.setDiscoveryEnvironment(mComputationalAnalysisProgram);
            model.setCurated(false);

            mResultSetType = null;  // Clear for next pass.  This value can
                                    //   be specified for each new compound feature.

        } // Got analysis type

        // Add the comments to the model.
        GenomicEntityComment[] comments = new GenomicEntityComment[mCommentList.size()];
        mCommentList.toArray(comments);
        model.setComments(comments);
        comments = null;
        mCommentList.clear();

        // Capture the expect values.
        model.setIndividualExpect(mIndividualExpect);
        mIndividualExpect = null;
        model.setSummaryExpect(mSummaryExpect);
        mSummaryExpect = null;

        model.setScore(mScore);
        mScore = null;

        SimpleFeatureModel subModel = null;
        OID previousAxis = null;
        for (Iterator it = mSubmodels.iterator(); it.hasNext(); ) {
            subModel = (SimpleFeatureModel)it.next();
            if (previousAxis != null && (! subModel.getAxisOfAlignment().equals(previousAxis))) {
                handleException(new IllegalArgumentException("ERROR: seq relationship id "+
                    subModel.getAxisOfAlignment()+" differs from previous one of "+previousAxis+
                    " discarding sub feature "+subModel.getOID()));
                continue;
            } // Inconsistency in models.
            previousAxis = subModel.getAxisOfAlignment();

            model.addChild(subModel);

            // Discovery environment is what links things at the HSP level
            // to the compound feature, when it comes to display, so it
            // is vital to set them them same at compound and simple level.
            if (subModel.isCurated()) {
                if (subModel.getOID().isScratchOID())
                    subModel.setDiscoveryEnvironment("Curation");
                else
                    subModel.setDiscoveryEnvironment("Promoted");
            } // Got curated.
            else if (subModel.getDiscoveryEnvironment() == null)
                subModel.setDiscoveryEnvironment(mComputationalAnalysisProgram);

            subModel.setParent(model);
        } // For all collected simple feature models.

        mSubmodels.clear();

        model.addReplacedList(destructivelyRetrieveReplaced());

        model.addPropertySources(destructivelyRetrievePropertySources());

        return model;
    } // End method

    /**
     * Builds an XML model object based on assumed parent/child hierarchy.
     */
    private CompoundFeatureModel createNonHierarchicalModel() {
        // If any child feature was not complete, this one is not either.
        if (mIncompleteCompoundFeature)
            return null;

        NonHierarchicalFeatureModel subModel = (NonHierarchicalFeatureModel)mSubmodels.get(0);

        if (subModel.isCurated()) {
            if (subModel.getOID().isScratchOID())
                subModel.setDiscoveryEnvironment("Curation");
            else
                subModel.setDiscoveryEnvironment("Promoted");
        } // Got curated.
        else if (subModel.getDiscoveryEnvironment() == null)
            subModel.setDiscoveryEnvironment(mComputationalAnalysisProgram);

        // Clear for next run.
        mAnnotationName = null;
        mDescription = null;
        mResultSetType = null;
        mCommentList.clear();
        mIndividualExpect = null;
        mSummaryExpect = null;
        mScore = null;
        mSubmodels.clear();

        return subModel;
    } // End method

    /**
     * Returns the type string of the result set being produced.  This
     * string will ultimately be used to decide what the entity type
     * of the produced entity will become.
     */
    private String decideResultSetType() {
        // Handling of result set type:
        //  1. Look for type under result set being built.
        //  2. Look for type passed in from computational analysis to which
        //     the result set belongs.
        //  3. Just use the analysis program passed in from the comp anal.
        //
        String returnValue = null;
        if (mResultSetType != null)
            returnValue = mResultSetType;
        else {
            if (mComputationalAnalysisType != null)
                returnValue = mComputationalAnalysisType;
            else
                returnValue = mComputationalAnalysisProgram;
        } // No type for THIS result set.

        return returnValue;
    } // End method

    /**
     * Sets up the mapping of element names to buffers to be filled
     * with their contents.
     */
    private void initializeFillBufferMap() {

        mFillBufferMap = new HashMap();

        // For precomputes...
        mFillBufferMap.put(CEFParseHelper.SCORE_ELEMENT, null);
        mFillBufferMap.put(CEFParseHelper.VALUE_ELEMENT, null);

        // For human curations...
        mFillBufferMap.put(CEFParseHelper.TYPE_ELEMENT, null);
        mFillBufferMap.put(CEFParseHelper.ANNOTATION_SOURCE_ELEMENT, null);
        mFillBufferMap.put(CEFParseHelper.COMMENTS_ELEMENT, null);

        // For either...
        mFillBufferMap.put(CEFParseHelper.NAME_ELEMENT, null);
        mFillBufferMap.put(CEFParseHelper.DESCRIPTION_ELEMENT, null);

    } // End method: initializeFillBufferMap

} // End class: CompoundFeatureHandler
