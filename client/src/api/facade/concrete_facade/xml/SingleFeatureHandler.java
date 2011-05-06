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
 * Title:        Feature Handler to retrieve model for single OID--root level.<p>
 * Description:  <p>
 * @author Les Foster
 * @version $Id$
 */
package api.facade.concrete_facade.xml;

import api.facade.concrete_facade.shared.ConcreteFacadeConstants;
import api.facade.concrete_facade.shared.ContainsOidCriterion;
import api.facade.concrete_facade.shared.FeatureCriterion;
import api.facade.concrete_facade.shared.OIDParser;
import api.facade.concrete_facade.shared.feature_bean.CompoundFeatureBean;
import api.facade.concrete_facade.shared.feature_bean.FeatureBean;
import api.facade.concrete_facade.shared.feature_bean.GeneFeatureBean;
import api.facade.concrete_facade.xml.sax_support.*;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.GenomicEntityComment;
import api.stub.data.OID;
import api.stub.geometry.Range;

import java.util.*;

/**
 * Delegated class to handle SAX events.  It in turn delegates to handlers
 * that are used to build models.  This handler does file scans to locate
 * all data for a given OID.
 */
public class SingleFeatureHandler extends FeatureHandlerBase {
    //------------------------------MEMBER VARIABLES
    // These collections are temporary and reflect the state during the
    // construction of a particular model.
    private List mCompoundModels = new ArrayList();
    private List mCommentList = new ArrayList();
    private List mGeneReplacedList = new ArrayList();
    private List mPropertySourceList = new ArrayList();

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

    // Setup via constructor.
    private OIDParser mOIDParser = null;
    private ElementStacker mElementStacker = new LinkedListElementStacker();


    // Final state of the thing.
    //

    // Parameters for the scan of the input.
    private FeatureCriterion mFeatureCriterion = null;
    private String mSource = null;

    // Used in some cases during creation of model objects.
    private XmlFacadeManager mReadFacadeManager = null;
    private SequenceAlignment mSequenceAlignment = null;
    private Range mSequenceAlignmentRange = null;
    private List mReturnList = new ArrayList();

    //------------------------------STATIC METHODS
    /** Main method for unit testing. */
    public static void main(String[] lArgs) {
      OIDParser lOidParser = (OIDParser)new FeatureXmlLoader();
      SingleFeatureHandler lHandler = new SingleFeatureHandler("c:/gbtestfiles/test_standalone_feature_workspace/all_features_305.gbf", null, lOidParser);
      List lMatchList = lHandler.getModelsForCriterion(new ContainsOidCriterion(lOidParser.parseFeatureOID("INTERNAL:8000001247834")));
      if (lMatchList.size() == 0)
        System.out.println("No model found for oid INTERNAL:8000001247834");
      else {
        FeatureBean lModel = null;
        for (Iterator it = lMatchList.iterator(); it.hasNext(); ) {
          lModel = (FeatureBean)it.next();
          System.out.println("Analysis type "+lModel.getAnalysisType()+" for "+lModel.getClass().getName());
        } // For all models returned.
      } // Got something
    } // End main method

    //------------------------------CONSTRUCTORS
    /**
     * Constructor gets the first required data from the input file
     * using a SAX element scan.
     */
    public SingleFeatureHandler(String lFileName, SequenceAlignment lSequenceAlignment, OIDParser lOIDParser) {

      super(lOIDParser);
      mOIDParser = lOIDParser;
      mSequenceAlignment = lSequenceAlignment;
      if (mSequenceAlignment != null) {
        mSequenceAlignmentRange = new Range(mSequenceAlignment.getAxisStart(), mSequenceAlignment.getAxisEnd());
      } // Non-null alignment

      mSource = lFileName;

      // Build chain of delegates.
      FeatureHandlerBase lCompoundFeatureHandler = new CompoundFeatureHandler();
      lCompoundFeatureHandler.setOIDParser(mOIDParser);
      setDelegate(lCompoundFeatureHandler);

      FeatureHandlerBase lSimpleFeatureHandler = new SimpleFeatureHandler(mSequenceAlignment);
      lSimpleFeatureHandler.setOIDParser(mOIDParser);
      lCompoundFeatureHandler.setDelegate(lSimpleFeatureHandler);

    } // End constructor

    //------------------------------PUBLIC INTERFACE
    /**
     * Returns a feature model, given criteria to match.
     */
    public List getModelsForCriterion(FeatureCriterion lFeatureCriterion) {
      mFeatureCriterion = lFeatureCriterion;

      // Construct and invoke an event handler which does a SAX parse and
      // reacts to events generated by the input file.
      GenomicsExchangeHandler eventHandler = new GenomicsExchangeHandler( new LinkedListElementStacker(),
                                                                      (FeatureHandlerBase)this);



      eventHandler.loadFile(mSource);
      return mReturnList;
    } // End method

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
        mGeneOID = mOIDParser.parseFeatureOID(
            (String)(lContext.ancestorAttributesNumber(0).get(ID_ATTRIBUTE)));
        mCompoundModels.clear();
        mInGene = true;
      } // Pickup gene's attributes of interest.
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
      List lMatchingList = null;

      // If the end of a set occurred, we emerge from delegated,
      // and collect the set model.
      if ((lFoundCode == CEFParseHelper.RESULT_SET_CODE) || (lFoundCode == CEFParseHelper.FEATURE_SET_CODE)) {
        // Collect set model.
        if (lFoundCode == CEFParseHelper.RESULT_SET_CODE) {
          ((CompoundFeatureHandler)getDelegate()).setAnalysisSource(mAnalysisSource);
          ((CompoundFeatureHandler)getDelegate()).setResultSetType(mResultSetType);
        } // Got a result set.
        CompoundFeatureBean model = ((CompoundFeatureHandler)getDelegate()).createModel();

        if (mInGene)
          mCompoundModels.add(model);
        else if (lFoundCode == CEFParseHelper.FEATURE_SET_CODE) {
          if (null != (lMatchingList = listOfMatchingFeaturesIn(model)))
            mReturnList.addAll(lMatchingList);
          model.setParent(null);
        } // Root feature set.
        else if (lFoundCode == CEFParseHelper.RESULT_SET_CODE) {
          if (null != (lMatchingList = listOfMatchingFeaturesIn(model)))
            mReturnList.addAll(lMatchingList);
          model.setParent(null);
        } // Root precompute.
      } // End of a set element in the delegate.
      else if (lFoundCode == CEFParseHelper.ANNOTATION_CODE) {
        // Retrieve certain info that applies to the element.
        mGeneReplacedList = destructivelyRetrieveReplaced();
        mPropertySourceList = destructivelyRetrievePropertySources();

        // Time to build the annotation model.
        OID lGenomicAxisOID = ((CompoundFeatureHandler)getDelegate()).getOIDOfAlignment();
        GeneFeatureBean model = new GeneFeatureBean(mGeneOID, lGenomicAxisOID, mReadFacadeManager);

        model.setAnnotationName(mAnnotationName);
        mAnnotationName = null;
        model.setDescription(mDescription);
        mDescription = null;

        if (mAnnotationSource != null)
          model.setDiscoveryEnvironment(mAnnotationSource);
        else {
          if (model.getOID().isScratchOID()){
            model.setDiscoveryEnvironment(ConcreteFacadeConstants.CURATION_DISCOVERY_ENVIRONMENT);
          } else{
            model.setDiscoveryEnvironment("Promoted");
          } // Not scratch.
        } // No annotation source.
        mAnnotationSource = null;

        // Add all children to the model which have been encountered here.
        CompoundFeatureBean childModel = null;
        for (Iterator it = mCompoundModels.iterator(); it.hasNext(); ) {
          childModel = (CompoundFeatureBean)it.next();
          model.addChild(childModel);
          childModel.setParent(model);
        } // For all contained models.

        GenomicEntityComment[] comments = new GenomicEntityComment[mCommentList.size()];
        mCommentList.toArray(comments);
        model.setComments(comments);
        comments = null;

        model.addReplacedList(mGeneReplacedList);

        model.addPropertySources(mPropertySourceList);

        model.setCurated(true);  // Genes are always curated features.

        if (null != (lMatchingList = listOfMatchingFeaturesIn(model)))
          mReturnList.addAll(lMatchingList);

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

    //----------------------------HELPER METHODS
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
     * Find list of features that match the "fit" criteria.  This
     * could include the model itself, or any of its descendants.
     */
    private List listOfMatchingFeaturesIn(FeatureBean model) {
      List returnList = new ArrayList();

      List candidateList = mFeatureCriterion.allMatchingIn(model);
      if (candidateList != null) {
        if (this.mSequenceAlignmentRange == null) {
          returnList.addAll(candidateList);
        } // No alignemnt.
        else {
          FeatureBean nextModel = null;
          for (Iterator it = candidateList.iterator(); it.hasNext(); ) {
            nextModel = (FeatureBean)it.next();
            if (mSequenceAlignmentRange.contains(nextModel.calculateFeatureRange())) {
              returnList.add(nextModel);
            } // Agrees with alignment.
          } // For all iterations
        } // Non-null alignment
      } // Got real list back.

      return returnList;
    } // End method

} // End class: SingleFeatureHandler

/*
 $Log$
 Revision 1.2  2011/03/08 16:16:39  saffordt
 Java 1.6 changes

 Revision 1.1  2006/11/09 21:35:56  rjturner
 Initial upload of source

 Revision 1.6  2002/11/07 18:38:54  lblick
 Removed obsolete imports and unused local variables.

 Revision 1.5  2002/04/05 19:48:44  lfoster
 Removed refs to FacadeManager from sax support classes.  Wrapped facademanager handleexception calls in instance method calls.

 Revision 1.4  2002/04/05 19:06:57  lfoster
 Moved 8 classes from xml down to xml.sax_support.  Removed dep of PropertySource on abstract facades.

 Revision 1.3  2001/07/12 21:18:47  lfoster
 Search fixed for Sequence Alignment files.  Full-batery tested for search, bookmark and load.

 Revision 1.2  2001/05/31 15:32:09  lfoster
 New search types supported for feature / axis file combination.  Service will be next?

 Revision 1.1  2001/05/30 22:44:58  lfoster
 Adding implementation for first-cut search for new feature type searching.

 Revision 1.1  2001/05/23 14:11:23  lfoster
 New handler to scan input file for root model in which a specific OID was found.

 */
