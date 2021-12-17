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

package api.facade.concrete_facade.xml;

import api.facade.concrete_facade.shared.feature_bean.CompoundFeatureBean;
import api.facade.concrete_facade.shared.feature_bean.FeatureBean;
import api.facade.concrete_facade.shared.feature_bean.NonHierarchicalFeatureBean;
import api.facade.concrete_facade.shared.feature_bean.SimpleFeatureBean;
import api.facade.concrete_facade.xml.sax_support.CEFParseHelper;
import api.facade.concrete_facade.xml.sax_support.ElementContext;
import api.facade.concrete_facade.xml.sax_support.FeatureHandlerBase;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.GenomicEntityComment;
import api.stub.data.OID;
import api.stub.geometry.Range;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deals with simple features (those which cannot contain children).  Does
 * so for both human curations and precomputes.
 */
public class SimpleFeatureHandler extends FeatureHandlerBase {

    //----------------------------CONSTANTS
    private static final String DEFAULT_SPAN_ANALYSIS_TYPE = "HSP";

    //----------------------------MEMBER VARIABLES
    private Map mFillBufferMap = null;

    // Keepers for building model.
    private String mQueryStart = null;
    private String mQueryEnd = null;
    private String mSubjectStart = null;
    private String mSubjectEnd = null;
    private String mSpanAnalysisType = null;
    private String mSubjectSequenceAlignment = null;
    private String mQuerySequenceAlignment = null;
    private String mOutputValue = null;
    private String mOutputName = null;
    private String mSummaryExpect = null;
    private String mIndividualExpect = null;
    private String mScore = null;
    private String mAnnotationSource = null;
    private String mCommentText = null;
    private SequenceAlignment mSequenceAlignment = null;
    private List mEvidenceOIDList = new ArrayList();
    private List mCommentList = new ArrayList();
    private List mOtherAlignmentTextList = new ArrayList();
    private List mOtherSeqRelTypeList = new ArrayList();
    private Map mOutputMap = new HashMap(); // Output name versus value.
    private OID mOIDOfAlignment = null;     // Axis referenced or 'aligned' by current feature.
    private OID mOIDOfSubjectSequence = null;
    private OID mFeatureOID = null;
    private boolean mIsHumanCurated = false;
    private String mSeqRelType = null; // <seq_relationship type='???'

    private XmlFacadeManager mReadFacadeManager = null;

    //----------------------------CONSTRUCTORS
    /**
     * This handler will remain extant during the entire scan process.
     */
    public SimpleFeatureHandler(SequenceAlignment lSequenceAlignment) {
      mSequenceAlignment = lSequenceAlignment;
    } // End constructor.

    /**
     * This handler will remain extant during the entire scan process.
     */
    public SimpleFeatureHandler(XmlFacadeManager readFacadeManager) {
      mReadFacadeManager = readFacadeManager;
    } // End constructor.

    //----------------------------PUBLIC INTERFACE
    /**
     * Returns OID of alignment of current feature.
     */
    public OID getOIDOfAlignment() {
      return mOIDOfAlignment;
    } // End method: getOIDofAlignemnt

    /**
     * Builds the model representing the current span.
     */
    public FeatureBean createModel() {
      if (isHierarchicalFeature())
        return createSimpleFeatureModel();
      else
        return createCompoundFeatureModel();
    } // End method: createModel

    //------------------------------IMPLEMENTATION OF ExceptionHandler
    /** Simply delegates to the facade manager. */
    public void handleException(Exception lException) {
      FacadeManager.handleException(lException);
    } // End method: handleException

    //-----------------------------TEMPLATE METHOD IMPLEMENTATIONS
    /**
     * Called by superclass when element start tag encountered.
     *
     * @param String lName the name of the element.
     * @param AttributeList lAttrs the collection of tag attributes.
     */
    public void startElementTemplateMethod(ElementContext lContext) {

      String lIDStr = null;

      // Decode which element is requested, sans any string comparisons.
      int foundCode = CEFParseHelper.getCEFParseHelper().translateToElementCode(lContext.currentElement());
      if (foundCode == CEFParseHelper.UNINTERESTING_ELEMENT_CODE)
        return;

      if (foundCode == CEFParseHelper.SEQ_RELATIONSHIP_CODE) {
        mSeqRelType = (String)lContext.ancestorAttributesNumber(0).get(TYPE_ATTRIBUTE);
        if ((mSeqRelType != null) && (mSeqRelType.equals(SUBJECT_SEQ_RELATIONSHIP) ||
                                     mSeqRelType.equals(SUBJECT_SEQ_RELATIONSHIP_ALT))) {

          lIDStr = (String)lContext.ancestorAttributesNumber(0).get(ID_ATTRIBUTE);
          if (lIDStr != null)
            mOIDOfSubjectSequence = getOIDParser().parseFeatureOID(lIDStr);

        } // The subject sequence.
        else if (mSeqRelType != null) {
          // ANY other sequence relationship type (query, some polymorphism, etc.)
          // will be construed as a relationship to an Axis-of-alignment.
          lIDStr = (String)lContext.ancestorAttributesNumber(0).get(ID_ATTRIBUTE);
          if (mSequenceAlignment != null)
            mOIDOfAlignment = getOIDParser().parseContigOID(mSequenceAlignment.adjustID(lIDStr));
          else
            mOIDOfAlignment = getOIDParser().parseContigOID(lIDStr);

        } // The query IS the axis sequence.

      } // Get the OID of alignment.
      else if (foundCode == CEFParseHelper.EVIDENCE_CODE) {
        String evidenceIDStr = (String)lContext.ancestorAttributesNumber(0).get(RESULT_ATTRIBUTE);
        if (evidenceIDStr != null) {
          OID evidenceOID = getOIDParser().parseEvidenceOID(evidenceIDStr);
          mEvidenceOIDList.add(evidenceOID);
        } // Got evidence id.
      } // Got an evidence.
      else if (foundCode == CEFParseHelper.FEATURE_SPAN_CODE || foundCode == CEFParseHelper.RESULT_SPAN_CODE) {
        lIDStr = (String)lContext.ancestorAttributesNumber(0).get(ID_ATTRIBUTE);
        if (lIDStr != null)
          mFeatureOID = getOIDParser().parseFeatureOID(lIDStr);

        if (foundCode == CEFParseHelper.FEATURE_SPAN_CODE)
          mIsHumanCurated = true;
        else
          mIsHumanCurated = false;
      } // Get the OID of the feature which WILL be aligned to another OID.

    } // End method: startElementTemplateMethod

    /**
     * Called by superclass when element end tag was encountered.
     *
     * @param String lName the element which just ended.
     */
    public void endElementTemplateMethod(ElementContext lContext) {

        // Decode which element is requested, sans any string comparisons.
        int foundCode = CEFParseHelper.getCEFParseHelper().translateToElementCode(lContext.currentElement());
        if (foundCode == CEFParseHelper.UNINTERESTING_ELEMENT_CODE)
            return;

        // Elements of interest are no more than two levels below seq_rel..
        // String lTypeOfSeqRel = containingSeqRelType(2, lContext);
        if (mSeqRelType != null) {
            // Deal with the contents needed from a seq relationship.
            if (foundCode == CEFParseHelper.START_CODE) {

                // Subject start or Query start.
                if (mSeqRelType.equals(SUBJECT_SEQ_RELATIONSHIP) ||
                         mSeqRelType.equals(SUBJECT_SEQ_RELATIONSHIP_ALT))
                    mSubjectStart = textOfCurrentElement(lContext.currentElement());
                else
                    mQueryStart = textOfCurrentElement(lContext.currentElement());
            } // Got start element.
            else if (foundCode == CEFParseHelper.END_CODE) {

                String lName = lContext.currentElement();

                // Query end or subject end.
                if (mSeqRelType.equals(SUBJECT_SEQ_RELATIONSHIP) ||
                         mSeqRelType.equals(SUBJECT_SEQ_RELATIONSHIP_ALT))
                    mSubjectEnd = textOfCurrentElement(lName);
                else
                    mQueryEnd = textOfCurrentElement(lName);

            } // GOt an end element.
            else if (foundCode == CEFParseHelper.ALIGNMENT_CODE) {

                String lName = lContext.currentElement();

                // Query or subject- seq alignment.
                if (mSeqRelType.equals(QUERY_SEQ_RELATIONSHIP))
                    mQuerySequenceAlignment = textOfCurrentElement(lName);
                else if (mSeqRelType.equals(SUBJECT_SEQ_RELATIONSHIP) ||
                         mSeqRelType.equals(SUBJECT_SEQ_RELATIONSHIP_ALT))
                    mSubjectSequenceAlignment = textOfCurrentElement(lName);
                else {
                    mOtherAlignmentTextList.add(textOfCurrentElement(lName));
                    mOtherSeqRelTypeList.add(mSeqRelType);
                } // Not subject, nor query sequence relationship type.

            } // Got an alignment.
            else if (foundCode == CEFParseHelper.SEQ_RELATIONSHIP_CODE) {
                // Must null out this collector to avoid "slop over" to
                // subsequent seq relationship--in case the attribute is
                // not provided.
                mSeqRelType = null;
            } // End of seq relationship
        } // In a seq relationship.
        else if (foundCode == CEFParseHelper.TYPE_CODE) {

            String lName = lContext.currentElement();

            if (lContext.ancestorNumber(1).equals(CEFParseHelper.FEATURE_SPAN_ELEMENT))
                mSpanAnalysisType = textOfCurrentElement(lName);
            else if (lContext.ancestorNumber(1).equals(CEFParseHelper.OUTPUT_ELEMENT))
                mOutputName = textOfCurrentElement(lName);

        } // Got span type for human curated.
        else if (foundCode == CEFParseHelper.SPAN_TYPE_CODE)
            mSpanAnalysisType = textOfCurrentElement(lContext.currentElement());
        else if (foundCode == CEFParseHelper.SCORE_CODE)
            mScore = textOfCurrentElement(lContext.currentElement());
        else if (foundCode == CEFParseHelper.VALUE_CODE) {
            if (lContext.ancestorNumber(1).equals(CEFParseHelper.OUTPUT_ELEMENT))
                mOutputValue = textOfCurrentElement(lContext.currentElement());

        } // Found a value.
        else if (foundCode == CEFParseHelper.OUTPUT_CODE) {
            // All necessary stuff8 is back from output.
            if (mOutputName != null) {
                if (mOutputName.equals(SUMMARY_EXPECT_OUTPUT))
                    mSummaryExpect = mOutputValue;
                else if (mOutputName.equals(INDIVIDUAL_EXPECT_OUTPUT))
                    mIndividualExpect = mOutputValue;
                else
                    mOutputMap.put(mOutputName, mOutputValue);
            } // Got an output name.

            // Clear the output stuff.
            mOutputName = null;
            mOutputValue = null;
        } // End of output.
        else if (foundCode == CEFParseHelper.ANNOTATION_SOURCE_CODE)
            mAnnotationSource = textOfCurrentElement(lContext.currentElement());
        else if (foundCode == CEFParseHelper.COMMENTS_CODE) {

            mCommentText = textOfCurrentElement(lContext.currentElement());

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
        clearMappedBuffer(lContext.currentElement());

    } // End method: endElementTemplateMethod

    /**
     * Pickup characters as needed.
     */
    public void charactersTemplateMethod(char[] lCharacters, int lStart, int lLength,
        ElementContext lContext) {

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
    } // End method: getFillBufferMapTemplateMethd

    //----------------------------HELPER METHODS
    /**
     * NOTE: by no means complete: this way requires excessive string comparisons,
     * and does everything here.  There should be some support for detecting
     * when something is non-hierarchical elsewhere.  But where?
     */
    private boolean isHierarchicalFeature() {
      if (mSpanAnalysisType == null)
        return true;

      if (mSpanAnalysisType.toUpperCase().endsWith("POLYMORPHISM"))
        return false;

      return true;
    } // End method

    /**
     * Creates a simple feature, or a feature which belongs in a hierarchy.
     */
    private SimpleFeatureBean createSimpleFeatureModel() {
        SimpleFeatureBean model = new SimpleFeatureBean(mFeatureOID, mOIDOfAlignment, mReadFacadeManager);
        model.setScore(mScore);
        mScore = null;
        model.setSubjectSequenceOid(mOIDOfSubjectSequence);
        mOIDOfSubjectSequence = null;

        if ((mQueryStart != null) && (!mQueryStart.equals("")))
            model.setStart(mQueryStart);
        if ((mQueryEnd != null) && (!mQueryEnd.equals("")))
            model.setEnd(mQueryEnd);

        // If this data comes from a sequence alignment, adjust it accordingly.
        if (mSequenceAlignment != null) {
            Range adjustedRange = mSequenceAlignment.adjustOldAxisRangeToNew(new Range(model.getStart(), model.getEnd()));

            // Checks that feature is within aligned range.
            if (adjustedRange == null)
                return null;

            model.setStart(adjustedRange.getStart());
            model.setEnd(adjustedRange.getEnd());
        } // Readjust the start and end points.

        // Retrieving from data "catchers", and clearing them.
        if ((mSubjectStart != null) && (!mSubjectStart.equals(""))) {
            model.setSubjectStart(Integer.parseInt(mSubjectStart));
            model.setSubjectEnd(Integer.parseInt(mSubjectEnd));
        } // Only set numeric values if data contained.

        if (mOutputMap.size() > 0) {
            model.setOutputMap(mOutputMap);
            mOutputMap = new HashMap();
        } // If there are outputs to capture, place them into the model.

        // Clear out starts and ends.
        mQueryStart = null;
        mQueryEnd = null;
        mSubjectStart = null;
        mSubjectEnd = null;

        model.setSubjectAlignment(mSubjectSequenceAlignment);
        mSubjectSequenceAlignment = null;
        model.setQueryAlignment(mQuerySequenceAlignment);
        mQuerySequenceAlignment = null;

        if (mSpanAnalysisType == null)
            mSpanAnalysisType = DEFAULT_SPAN_ANALYSIS_TYPE;
        model.setAnalysisType(mSpanAnalysisType);
        mSpanAnalysisType = null;

        model.setCurated(mIsHumanCurated);

        // Pickup any specific discovery environment.
        if (mAnnotationSource != null)
            model.setDiscoveryEnvironment(mAnnotationSource);
        mAnnotationSource = null;

        // Capture the expect values.
        model.setIndividualExpect(mIndividualExpect);
        mIndividualExpect = null;
        model.setSummaryExpect(mSummaryExpect);
        mSummaryExpect = null;

        // Set and discard evidence OIDs.
        model.addEvidenceList(mEvidenceOIDList);
        mEvidenceOIDList.clear();

        // Set and discard replaced data and property sources.
        model.addReplacedList(destructivelyRetrieveReplaced());

        model.addPropertySources(destructivelyRetrievePropertySources());

        // Add the comments to the model.
        GenomicEntityComment[] comments = new GenomicEntityComment[mCommentList.size()];
        mCommentList.toArray(comments);
        model.setComments(comments);
        comments = null;
        mCommentList.clear();

        return model;
    } // End method

    /**
     * Creates a non-hierarchical feature, or a feature which can stand alone
     * with no parent, but also has no children.
     */
    private CompoundFeatureBean createCompoundFeatureModel() {
        NonHierarchicalFeatureBean model = new NonHierarchicalFeatureBean(mFeatureOID, mOIDOfAlignment, mReadFacadeManager);
        model.setScore(mScore);
        mScore = null;

        if ((mQueryStart != null) && (!mQueryStart.equals("")))
            model.setStart(mQueryStart);
        if ((mQueryEnd != null) && (!mQueryEnd.equals("")))
            model.setEnd(mQueryEnd);

        // If this data comes from a sequence alignment, adjust it accordingly.
        if (mSequenceAlignment != null) {
            Range adjustedRange = mSequenceAlignment.adjustOldAxisRangeToNew(new Range(model.getStart(), model.getEnd()));

            // Checks that feature is within aligned range.
            if (adjustedRange == null)
                return null;

            model.setStart(adjustedRange.getStart());
            model.setEnd(adjustedRange.getEnd());
        } // Readjust the start and end points.

        // Retrieving from data "catchers", and clearing them.
        if ((mSubjectStart != null) && (!mSubjectStart.equals(""))) {
            model.setSubjectStart(Integer.parseInt(mSubjectStart));
            model.setSubjectEnd(Integer.parseInt(mSubjectEnd));
        } // Only set numeric values if data contained.

        // Clear out starts and ends.
        mQueryStart = null;
        mQueryEnd = null;
        mSubjectStart = null;
        mSubjectEnd = null;

        mSubjectSequenceAlignment = null; // Clear for subsequent features.
        mQuerySequenceAlignment = null; // Clear for subsequent features.

        if (mSpanAnalysisType == null)
            mSpanAnalysisType = DEFAULT_SPAN_ANALYSIS_TYPE;
        model.setAnalysisType(mSpanAnalysisType);
        mSpanAnalysisType = null;

        model.setCurated(mIsHumanCurated);

        // Pickup any specific discovery environment.
        if (mAnnotationSource != null)
            model.setDiscoveryEnvironment(mAnnotationSource);
        mAnnotationSource = null;

        // Capture the expect values.
        model.setIndividualExpect(mIndividualExpect);
        mIndividualExpect = null;
        model.setSummaryExpect(mSummaryExpect);
        mSummaryExpect = null;

        // Set and discard evidence OIDs.
        model.addEvidenceList(mEvidenceOIDList);
        mEvidenceOIDList.clear();

        // Set and discard replaced data and property sources.
        model.addReplacedList(destructivelyRetrieveReplaced());

        model.addPropertySources(destructivelyRetrievePropertySources());

        // Add the comments to the model.
        GenomicEntityComment[] comments = new GenomicEntityComment[mCommentList.size()];
        mCommentList.toArray(comments);
        model.setComments(comments);
        comments = null;
        mCommentList.clear();

        String nextAlignmentText = null;
        String nextSeqRelType = null;
        for (int i = 0; i < mOtherAlignmentTextList.size(); i++) {
            nextAlignmentText = (String)mOtherAlignmentTextList.get(i);
            nextSeqRelType = (String)mOtherSeqRelTypeList.get(i);
            try {
                model.addPolymorphismAlleleText(nextSeqRelType, nextAlignmentText);
            } // End try block to add an allele.
            catch (Exception ex) {
                // NOT constructing any model.
                //  The inclusion of this null-set implies this must be at
                //  the end of the method, AFTER all model.xxx() calls.
                model = null;
            } // End catch block for add an allele.
        } // For all other alignment texts.

        mOtherAlignmentTextList.clear();
        mOtherSeqRelTypeList.clear();

        return model;
    } // End method

    /**
     * Returns the type of seq relationship we are in, or null if we are not.
     */
    public String containingSeqRelType(int lLimit, ElementContext lContext) {
      String returnString = null;
      // We trace back up the ancestry stack until we find it, meet the stated
      // limit (for efficiency) or meet the run-time limit (of nested elements).
      for (int i = 0; (returnString == null) && (i <= lLimit) &&
           (i <= lContext.maxAncestor()) && (lContext.ancestorNumber(i) != null); i++) {

         if (lContext.ancestorNumber(i).equals(CEFParseHelper.SEQ_RELATIONSHIP_ELEMENT))
            returnString = (String)lContext.ancestorAttributesNumber(i).get(TYPE_ATTRIBUTE);

      } // For all ancestors until found.
      return returnString;
    } // End method: containingSeqRelType

    /**
     * Sets up the mapping of element names to buffers to be filled
     * with their contents.
     */
    private void initializeFillBufferMap() {
      mFillBufferMap = new HashMap();

      mFillBufferMap.put(CEFParseHelper.START_ELEMENT, null);
      mFillBufferMap.put(CEFParseHelper.END_ELEMENT, null);
      mFillBufferMap.put(CEFParseHelper.ALIGNMENT_ELEMENT, null);
      mFillBufferMap.put(CEFParseHelper.SCORE_ELEMENT, null);

      // Precomputes only.
      mFillBufferMap.put(CEFParseHelper.SPAN_TYPE_ELEMENT, null);

      // Human curations only.
      mFillBufferMap.put(CEFParseHelper.TYPE_ELEMENT, null);
      mFillBufferMap.put(CEFParseHelper.VALUE_ELEMENT, null);
      mFillBufferMap.put(CEFParseHelper.COMMENTS_ELEMENT, null);

    } // End method: initializeFillBufferMap

} // End class: SimpleFeatureHandler

/*
  $Log$
  Revision 1.1  2006/11/09 21:35:56  rjturner
  Initial upload of source

  Revision 1.56  2002/11/07 16:06:14  lblick
  Removed obsolete imports and unused local variables.

  Revision 1.55  2002/10/28 19:40:13  lblick
  Resolved merge conflicts.

  Revision 1.54.2.1  2002/10/23 16:46:32  lfoster
  Moved inconsistent OID test for query axis from simple handler (which does not have knowledge of sibling features), out to the compound handler, which should and does.

  Revision 1.54  2002/05/10 22:11:21  lfoster
  Fixed "follow on subject sequence OID" bug.

  Revision 1.53  2002/04/05 19:56:52  lfoster
  Removed unused sax package imports.

  Revision 1.52  2002/04/05 19:48:43  lfoster
  Removed refs to FacadeManager from sax support classes.  Wrapped facademanager handleexception calls in instance method calls.

  Revision 1.51  2002/04/05 19:06:57  lfoster
  Moved 8 classes from xml down to xml.sax_support.  Removed dep of PropertySource on abstract facades.

  Revision 1.50  2002/03/25 22:53:42  lfoster
  Removed unneccessary dependencies.

  Revision 1.49  2002/03/21 15:44:36  lfoster
  Supporting polymorphisms.

  Revision 1.48  2002/01/21 19:18:26  lfoster
  Switched from Linked List to Array List for element stack.  Caching the seq relationship
  type attribute rather than prowling the element stack looking for it.

  Revision 1.47  2001/07/12 21:18:47  lfoster
  Search fixed for Sequence Alignment files.  Full-batery tested for search, bookmark and load.

  Revision 1.46  2001/05/14 22:34:22  lfoster
  Fixed up bad assumption about always having a <span_type tag.

  Revision 1.45  2001/05/14 03:21:28  lfoster
  Modified comments handling to use constructors with intended default behavior.

  Revision 1.44  2001/05/13 04:15:36  lfoster
  Changed handling of comments so no date->no comment.

  Revision 1.43  2001/05/13 03:54:04  lfoster
  Fixed some uses of unspecified attributes, that did not first test them for null.

  Revision 1.42  2001/05/13 03:48:33  lfoster
  Fixed some uses of unspecified attributes, that did not first test them for null.

  Revision 1.41  2001/05/11 21:23:11  lfoster
  Eliminated constants defined in FeatureHandlerBase in favor of those in CEFParseHelper.  Converted more loaders/handlers to use the hashmap/code method to identify elements rather than string comparisons.

  Revision 1.40  2001/05/09 15:23:24  lfoster
  Refitted Simple Feature Handler to use map lookup instead of string comparisons to resolve element state.

  Revision 1.39  2001/04/24 21:33:48  lfoster
  Closer to real values.  Must confirm accuracy with Marian.

  Revision 1.38  2001/04/24 20:40:17  lfoster
  Now have skelatal report being issued for subject sequence cref.

  Revision 1.37  2001/04/20 22:09:04  lfoster
  First attempt at implementing reports.  NOT BEING CALLED!

  Revision 1.36  2001/03/14 15:14:34  pdavies
  Made comments handle a date
*/
