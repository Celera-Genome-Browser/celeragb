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

package api.facade.concrete_facade.xml.sax_support;

import api.facade.concrete_facade.shared.OIDParser;
import api.facade.concrete_facade.shared.PropertySource;
import api.stub.data.GenomicEntityComment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Base class.  Supports ancestry stack, and some no-brainer actions for
 * encountered elements, as well as some element handling that will be
 * common to all subclasses.
 */
public abstract class FeatureHandlerBase implements ExceptionHandler {

    //----------------------------CONSTANTS
    protected final static String ID_ATTRIBUTE = "id";
    protected final static String LENGTH_ATTRIBUTE = "length";
    protected final static String IDS_ATTRIBUTE = "ids";
    protected final static String TYPE_ATTRIBUTE = "type";
    protected final static String NAME_ATTRIBUTE = "name";
    protected final static String VALUE_ATTRIBUTE = "value";
    protected final static String EDITABLE_ATTRIBUTE = "editable";
    protected final static String SUBJECT_SEQ_RELATIONSHIP = "subject";
    protected final static String SUBJECT_SEQ_RELATIONSHIP_ALT = "sbjct";
    protected final static String QUERY_SEQ_RELATIONSHIP = "query";
    protected final static String AUTHOR_ATTRIBUTE = "author";
    protected final static String DATE_ATTRIBUTE = "date";
    protected final static String RESULT_ATTRIBUTE = "result";

    // NOTE: these are also the default delimiters for StringTokenizer,
    // but explicitly coding them may prevent confusion if a move to
    // something other than StringTokenizer takes place later.
    protected final static String XML_WHITESPACE_SEPARATORS = " \t\n\r";

    protected final static String SUMMARY_EXPECT_OUTPUT = "Expect";
    protected final static String INDIVIDUAL_EXPECT_OUTPUT = "Individual_Expect";

    // State indicators for delegation testing.
    protected final static byte IN_START_OF_ELEMENT = 1;
    protected final static byte IN_END_OF_ELEMENT = 2;
    protected final static byte IN_TEXT = 3;

    //----------------------------MEMBER VARIABLES
    private OIDParser mOIDParser = null;
    private List mPropertyStack = new ArrayList();   // Ancestry of properties.
                 // Vertical properties (above)
    private List mPropertySources = new ArrayList(); // Root sibling properties.
                 // Horizontal properties (above)--first level below container.

    // Collection objects for replaced tags.
    private List mReplacedList = new ArrayList();
    private StringBuffer mReplacedText = new StringBuffer();

    private boolean DEBUG_CLASS = false;
    private FeatureHandlerBase mDelegate = null;     // Handoff to this.

    //----------------------------CONSTRUCTORS
    /**
     * This constructor does not build a parser.
     */
    public FeatureHandlerBase(OIDParser lOIDParser) {
        mOIDParser = lOIDParser;
    } // End constructor

    public FeatureHandlerBase() {
    } // End constructor

    //-----------------------------PUBLIC INTERFACE
    /**
     * Allows set of delegate for criteria to be established here.
     */
    public void setDelegate(FeatureHandlerBase lDelegate) {
        mDelegate = lDelegate;
    } // End method: setDelegate

    /**
     * Returns delegated handler in case crosstalk is needed.
     */
    public FeatureHandlerBase getDelegate() {
        return mDelegate;
    } // End method: getDelegate

    /**
     * Allows external set of OID generator.
     */
    public void setOIDParser(OIDParser lOIDParser) {
        mOIDParser = lOIDParser;
    } // End method: setOIDParser

    /**
     * Allows retrieval of the list of bases of property hierarchies.
     * Destroys the list on retrieval.
     * CAUTION: call this when all properties in scope have been
     * collected.  Usually at end of a tag containing properties.
     */
    public List destructivelyRetrievePropertySources() {
        List lSources = new ArrayList();
        lSources.addAll(mPropertySources);
        mPropertySources.clear();
        return lSources;
    } // End method: destructivelyRetrievePropertySources

    /**
     * Allows retrieval of the list replaced datas.
     * CAUTION: call this when all properties in scope have been
     * collected.  Usually at end of a tag containing replaced element(s).
     */
    public List destructivelyRetrieveReplaced() {
        List lReplaced = new ArrayList();
        lReplaced.addAll(mReplacedList);
        mReplacedList.clear();
        return lReplaced;
    } // End method: destructivelyRetrieveReplaced

    /**
     * Given control for start of element.
     */
    public void startElement(ElementContext context) {
        if (delegate(context, FeatureHandlerBase.IN_START_OF_ELEMENT) && (mDelegate != null)) {
            // NOTE: removed these calls to common elements to fix problem of props occurring on parent AND child.
            //      mDelegate.handleStartOfCommonElements(context);
            mDelegate.startElement(context);
        } // Delegating.
        else {
            this.handleStartOfCommonElements(context);
            this.startElementTemplateMethod(context);
        } // Not delegating.
    } // End method: startElement

    /**
     * Given control for end of element.
     */
    public void endElement(ElementContext context) {
        if (delegate(context, FeatureHandlerBase.IN_END_OF_ELEMENT) && (mDelegate != null)) {
            //    mDelegate.handleEndOfCommonElements(context);
            mDelegate.endElement(context);
        } // Delegating.
        else {
            this.handleEndOfCommonElements(context);
            this.endElementTemplateMethod(context);
        } // Not delegating.
    } // End method: endElement

    /**
     * Given control for characters encountered.
     *
     * @param char[] lCharacters the whole buffer being constructed.
     * @param int lStart the starting point within the buffer.
     * @param int lLength the ending point within the buffer.
     */
    public void characters(char[] lCharacters, int lStart, int lLength,
        ElementContext lContext) {

        if (delegate(lContext, FeatureHandlerBase.IN_TEXT) && (mDelegate != null)) {
            //      mDelegate.handleTextOfCommonElements(lCharacters, lStart, lLength, lContext);
            mDelegate.characters(lCharacters, lStart, lLength, lContext);
        } // Not delegating.
        else {
            // Stow the content to designated buffers as it occurs.
            Map lFillBufferMap = getFillBufferMapTemplateMethod();
            if ((lFillBufferMap != null) && (lFillBufferMap.containsKey(lContext.currentElement()))) {
                StringBuffer lTargetBuffer = (StringBuffer)lFillBufferMap.get(lContext.currentElement());
                if (lTargetBuffer == null) {
                    lTargetBuffer = new StringBuffer();
                    lFillBufferMap.put(lContext.currentElement(), lTargetBuffer);
                } // No buffer yet.
                lTargetBuffer.append(new String(lCharacters, lStart, lLength).trim());

                if (DEBUG_CLASS)
                    System.out.println("Appending /"+new String(lCharacters, lStart, lLength)+"/ to element "+lContext.currentElement());
            } // Need to collect current content.

            this.handleTextOfCommonElements(lCharacters, lStart, lLength, lContext);
            this.charactersTemplateMethod(lCharacters, lStart, lLength, lContext);
        } // Not delegating.

    } // End method: characters

    //-----------------------------------------TEMPATES FOR SUBCLASS METHODS
    /**
     * Called on subclass when element start tag encountered.
     *
     * @param String lName the name of the element.
     * @param AttributeList lAttrs the collection of tag attributes.
     */
    public abstract void startElementTemplateMethod(ElementContext context);

    /**
     * Called on subclass when element end tag was encountered.
     *
     * @param ElementContext context where the element fits in ancestry.
     */
    public abstract void endElementTemplateMethod(ElementContext context);

    /**
     * Called on subclass for character content.
     *
     * @param char[] lCharacters the whole buffer being constructed.
     * @param int lStart the starting point within the buffer.
     * @param int lLength the ending point within the buffer.
     */
    public abstract void charactersTemplateMethod(char[] lCharacters, int lStart, int lLength,
        ElementContext lContext);

    /**
     * Returns text of the current element -- if it has been mapped of interest.
     * Destroys references to that text, effectively making this a read-once.
     *
     * @param String lCurrentElement the element currently being handled.
     */
    public String textOfCurrentElement(String lCurrentElement) {
        String returnString = null;
        Map lFillBufferMap = getFillBufferMapTemplateMethod();
        if (lFillBufferMap == null)
            throw new IllegalArgumentException("Attempt to retrieve text from unregistered XML element");

        StringBuffer tempBuffer = (StringBuffer)lFillBufferMap.get(lCurrentElement);
        if (tempBuffer != null) {
            returnString = tempBuffer.toString();
            lFillBufferMap.put(lCurrentElement, null); // Clear buf/avoid mem leak.
            if (returnString.length() == 0)
                returnString = null; // NO empty strings returned.
        } // Current element is registered.
        return returnString;
    } // End method: textOfCurrentElement

    /**
     * Override to allow call with ONLY a context.
     *
     * @param ElementContext lContext the context WITH the current element
     */
    public String textOfCurrentElement(ElementContext lContext) {
        return textOfCurrentElement(lContext.currentElement());
    } // End method: textOfCurrentElement

    /**
     * Establish criteria for delegation to other handler.  This method decides
     * _before_ the template method is called, whether that template method will
     * even be called, or this processing will be done in the delegated handler.
     *
     * @param ElementContext lContext element ancestry for relative doc state.
     * @param byte to decode whether this is start of element, end of element\
     *    or in characters.
     */
    public boolean delegate(ElementContext lContext, byte lProcessState) {
        return false; // Never delegate unless this method overridden.
    } // End method: delegate

    /**
     * Returning the fill buffer map allows values to be assigned to buffers
     * local to current handler.
     */
    public abstract Map getFillBufferMapTemplateMethod();

    /**
     * Returns the OID parser, so that id values may be turned into
     * OIDs.
     */
    public OIDParser getOIDParser() {
        return mOIDParser;
    } // End method: getOIDParser

    /**
     * Returns a properly-defaulted comment object.
     */
    protected GenomicEntityComment createGenomicEntityComment(String lAuthor, String lDate, String lText) {
        GenomicEntityComment returnComment = null;
        try {
            if (lText != null) {
                if ((lAuthor != null) && (lDate != null)) {
                    returnComment = new GenomicEntityComment(lAuthor, lDate, lText);
                } // Both given
                else if ((lAuthor == null) && (lDate == null)) {
                    returnComment = new GenomicEntityComment(lText);
                } // No author nor date
                else if (lDate == null) {
                    returnComment = new GenomicEntityComment(lAuthor, lText);
                } // No date
                else if (lAuthor == null) {
                    returnComment = new GenomicEntityComment(System.getProperty("user.name"), lDate, lText);
                } // No author
            } // Something to build
        } // End try
        catch (java.text.ParseException ex) {
            handleException(ex);
        } // End catch
        return returnComment;

    } // End method

    /**
     * A facility for use by subclasses, which allows elements common
     * to certain elements, and appearing at different places in the
     * hierarchy, to be handled in the same way everywhere.
     */
    protected void handleStartOfCommonElements(ElementContext lContext) {

        // Decode which element is requested, sans any string comparisons.
        int lFoundCode = CEFParseHelper.getCEFParseHelper().translateToElementCode(lContext.currentElement());

        // Test for certain tags that will be handled here.
        if (lFoundCode == CEFParseHelper.PROPERTY_CODE) {

            // Create a property source.
            Map lAttributesMap = lContext.ancestorAttributesNumber(0);

            String lPropertyName = (String)lAttributesMap.get(NAME_ATTRIBUTE);
            if (lPropertyName == null)
                lPropertyName = "";

            String lPropertyValue = (String)lAttributesMap.get(VALUE_ATTRIBUTE);
            if (lPropertyValue == null)
                lPropertyValue = "";

            String lEditableAttribute = (String)lAttributesMap.get(EDITABLE_ATTRIBUTE);

            PropertySource lPropertySource = new PropertySource(
                         lPropertyName,
                         lPropertyValue,
                         (lEditableAttribute != null) && lEditableAttribute.equalsIgnoreCase("true") );

            // Make it the child of last-in-stack.
            if (mPropertyStack.size() > 0) {
                PropertySource lParentSource = (PropertySource)mPropertyStack.get(mPropertyStack.size() - 1);
                lParentSource.addChildSource(lPropertySource);
            } // This is a child property.

            // Push it ONTO the stack.
            mPropertyStack.add(lPropertySource);

        } // Found start of a property element

    } // End method: handleStartOfCommonElements

    /**
     * A facility for use by subclasses, which allows elements common
     * to certain elements, and appearing at different places in the
     * hierarchy, to be handled in the same way everywhere.
     */
    protected void handleEndOfCommonElements(ElementContext lContext) {
        // Decode which element is requested, sans any string comparisons.
        int lFoundCode = CEFParseHelper.getCEFParseHelper().translateToElementCode(lContext.currentElement());

        // Test for certain tags that will be handled here.
        if (lFoundCode == CEFParseHelper.PROPERTY_CODE) {
            // Pop it from the stack.
            if (mPropertyStack.size() > 0) {
                if (mPropertyStack.size() == 1) {
                    mPropertySources.add(mPropertyStack.get(0));
                } // Removing last parent.

                mPropertyStack.remove(mPropertyStack.size() - 1);

            } // Remove from the running as a parent of other properties.

        } // Got property.
        else if (lFoundCode == CEFParseHelper.REPLACED_CODE) {

            // Gather the data from replaced.
            Map lAttributesMap = lContext.ancestorAttributesNumber(0);
            String lAllIDs = (String)lAttributesMap.get(IDS_ATTRIBUTE);
            if (lAllIDs != null) {
                StringTokenizer stk = new StringTokenizer(lAllIDs, XML_WHITESPACE_SEPARATORS);

                List lAllOIDs = new ArrayList();
                while (stk.hasMoreTokens()) {
                    lAllOIDs.add(getOIDParser().parseEvidenceOID(stk.nextToken()));
                } // For all tokens.

                String lType = (String)lAttributesMap.get(TYPE_ATTRIBUTE);
                ReplacedData lReplacedData = new ReplacedData(lAllOIDs, lType, mReplacedText.toString());
                mReplacedList.add(lReplacedData);
            } // Got replaced OIDs.
            mReplacedText.setLength(0);

        } // Got replaced data.
    } // End method: handleEndOfCommonElements

    /**
     * Facility for use by subclasses, for collecting text of elements
     * appearing at multiple places in the element hierarchy.
     */
    protected void handleTextOfCommonElements(char[] lCharacters, int lStart, int lLength,
        ElementContext lContext) {

        // Decode which element is requested, sans any string comparisons.
        int lFoundCode = CEFParseHelper.getCEFParseHelper().translateToElementCode(lContext.currentElement());

        if (lFoundCode == CEFParseHelper.REPLACED_CODE) {
            mReplacedText.append(lCharacters, lStart, lLength);
        } // Found replaced.

    } // End method: handleTextOfCommonElements

    /**
     * Clears out contents of text buffers setup to hold text of elements.
     */
    protected void clearMappedBuffer(String lName) {
        Map lBufferMap = this.getFillBufferMapTemplateMethod();
        if ((lBufferMap != null) && lBufferMap.containsKey(lName)) {
            lBufferMap.put(lName, null);
        } // Got the key.
    } // End method: clearMappedBuffer

} // End class: FeatureHandlerBase

/*
  $Log$
  Revision 1.1  2006/11/09 21:36:16  rjturner
  Initial upload of source

  Revision 1.5  2002/11/07 16:06:58  lblick
  Removed obsolete imports and unused local variables.

  Revision 1.4  2002/07/11 13:43:29  lfoster
  Added code to support a property "subject_seq_length".

  Revision 1.3  2002/04/15 04:27:42  lfoster
  Inaccurate comment excised.

  Revision 1.2  2002/04/05 19:48:45  lfoster
  Removed refs to FacadeManager from sax support classes.  Wrapped facademanager handleexception calls in instance method calls.

  Revision 1.1  2002/04/05 19:06:58  lfoster
  Moved 8 classes from xml down to xml.sax_support.  Removed dep of PropertySource on abstract facades.

  Revision 1.17  2001/08/29 20:46:09  lfoster
  Fixed non-creation problem when both author and date supplied for comments.

  Revision 1.16  2001/05/14 17:53:08  lfoster
  Removed redundant handle...common calls which caused props to be applied at multiple levels.

  Revision 1.15  2001/05/14 03:21:29  lfoster
  Modified comments handling to use constructors with intended default behavior.

  Revision 1.14  2001/05/13 03:48:33  lfoster
  Fixed some uses of unspecified attributes, that did not first test them for null.

  Revision 1.13  2001/05/12 03:26:18  lfoster
  Removed dead commented code.

  Revision 1.12  2001/05/11 21:56:22  lfoster
  Using hashmap lookup to resolve element name, rather than string comparison.

  Revision 1.11  2001/05/11 21:23:12  lfoster
  Eliminated constants defined in FeatureHandlerBase in favor of those in CEFParseHelper.  Converted more loaders/handlers to use the hashmap/code method to identify elements rather than string comparisons.

  Revision 1.10  2001/04/17 16:17:54  lfoster
  Fixed a nullptr; forced null-return response to genomic axis alignment request on all-level loaders.

  Revision 1.9  2001/03/20 20:23:03  lfoster
  Migrated all mention of default parser class name string into a helper class, along with some shared constants.

  Revision 1.8  2001/01/12 17:02:56  lfoster
  No longer calling "textOfCurrentElement" with context: using name of element to avoid forcing calls to "currentElement()" within the method.

  Revision 1.7  2001/01/11 20:27:27  lfoster
  Optimizations for feature loading--first round.

  Revision 1.6  2001/01/08 05:06:08  lfoster
  Fixed bug introduced by refactoring.  But affected feature typeing of curated features.

  Revision 1.5  2001/01/04 23:22:16  lfoster
  New delegation mechanism in place.

  Revision 1.4  2000/11/16 23:38:03  lfoster
  Covering a null possibility.

  Revision 1.3  2000/11/02 23:13:15  lfoster
  Further on with seq alignments.  Fixed bugs in obtaining properties.

  Revision 1.2  2000/10/27 22:34:40  lfoster
  Corrected problem with setting of replaced OIDs to string rather than real OIDs

  Revision 1.1  2000/10/26 13:56:19  lfoster
  No longer implementing SAX handler in all delegated model-building classes.

*/
