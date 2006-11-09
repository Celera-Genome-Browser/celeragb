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
 * Description:  Subject sequence reader of the XML file<p>
 * Company:      []<p>
 * @author Les Foster
 * @version  CVS_ID:  $Id$
 */
package api.facade.concrete_facade.xml;

import api.facade.concrete_facade.xml.sax_support.CEFParseHelper;
import api.facade.facade_mgr.FacadeManager;
import api.stub.sequence.DNASequenceParser;
import api.stub.sequence.DNASequenceStorage;
import api.stub.sequence.ProteinSequenceParser;
import api.stub.sequence.ProteinSequenceStorage;
import api.stub.sequence.Sequence;
import api.stub.sequence.SequenceList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A handler for XML events from a SAX parser, this class picks up a sequence
 * corresponding to a given sequence id.
 */
public class SequenceLoader extends DefaultHandler {

    //----------------------------CONSTANTS
    private final static String SUCCESS_MESSAGE = "SUCCESS!";
    private final static String AMINO_ACID_SEQ_TYPE = "AA";
    private final static String DNA_SEQ_TYPE = "DNA";

    //----------------------------MEMBER VARIABLES
    // All below are "in pass" settings.
    private String mTestSequenceId;

    private List mAncestry = new ArrayList();
    private List mAncestorAttributes = new ArrayList();
    private List mAlignmentsList = new ArrayList();

    private Sequence mGenomicSequence = null;
    private boolean mAllDataHasBeenCollected = false;
    private boolean mInTargetSeqTag = false;
    private String mSequenceType = null;
    private SequenceList mResiduesList = null;

    private String mSource = null;
    private StringBuffer mContents = null;

    //----------------------------CONSTRUCTORS
    /**
     * Minimal constructor.
     */
    public SequenceLoader() {
    } // End constructor

    /**
     * Constructor sets up a file based source.
     */
    public SequenceLoader(String lFileName) {
        mSource = lFileName;
    } // End constructor

    /**
     * Constructor sets up a stringbuffer to be read when needed.
     */
    public SequenceLoader(StringBuffer lContents) {
        mContents = lContents;
    } // End constructor

    //-----------------------------PUBLIC INTERFACE
    /** Return info captured by this pass over the data. */
    public Sequence getSequence(String lSearchId) {

        clearForNextPass();

        // Setup seq target, and choose between file name and str-buf.
        mTestSequenceId = lSearchId;
        if (mSource != null)
            searchForSequence();
        else if (mContents != null)
            searchForSequenceInStringBuffer();
        else
            throw new IllegalStateException("Must construct "+this.getClass().getName()+" with non-null file or StringBuffer");
        return mGenomicSequence;
    } // End method

    /** Return info captured by this read of the URL given. */
    public Sequence getSequence(String lSearchId, String lUrl) {

        clearForNextPass();

        // Setup seq target, and choose between file name and str-buf.
        mTestSequenceId = lSearchId;
        if (lUrl != null) {
            mSource = lUrl;
            searchForSequence();
        } // Got url.
        else
            throw new IllegalArgumentException("Must specify a URL to get sequnece in "+this.getClass().getName());
        return mGenomicSequence;
    } // End method

    //-----------------------------SAX HANDLER IMPLEMENTATION METHODS
    /**
     * Called by parser when element start tag encountered.
     *
     * @param lUri - The Namespace URI, or the empty string if the element
     *  has no Namespace URI or if Namespace processing is not being performed.
     * @param lLocal - The local name (without prefix), or the empty string if
     *  Namespace processing is not being performed.
     * @param lName - The qualified name (with prefix), or the empty string if
     *  qualified names are not available.
     * @param Attributes lAttrs the collection of tag attributes.
     */
    public void startElement(String lUri, String lLocal, String lName, Attributes lAttrs) {

        if (mAllDataHasBeenCollected)
            stopParsing();

        // Decode which element is requested, sans any string comparisons.
        int foundCode = CEFParseHelper.getCEFParseHelper().translateToElementCode(lName);
        if (foundCode == CEFParseHelper.UNINTERESTING_ELEMENT_CODE)
            return;

        Map lAttributesMap = buildAttributesMap(lAttrs);
        mAncestry.add(lName);
        mAncestorAttributes.add(lAttributesMap);

        if (foundCode == CEFParseHelper.RESIDUES_CODE) {
            Map lParentAttributes = (Map)mAncestorAttributes.get(mAncestorAttributes.size() - 2);
            String lSequenceID = (String)lParentAttributes.get("id");
            mSequenceType = (String)lParentAttributes.get("type");

            String lParentTag = (String)mAncestry.get(mAncestry.size() - 2);
            if (lParentTag.equals(CEFParseHelper.SEQ_ELEMENT) && (mTestSequenceId.equalsIgnoreCase(lSequenceID))) {
                // Prepare to save sequence letters.
                if (mSequenceType.equalsIgnoreCase(DNA_SEQ_TYPE))
                    mResiduesList = new SequenceList(Sequence.KIND_DNA);
                else if (mSequenceType.equalsIgnoreCase(AMINO_ACID_SEQ_TYPE))
                    mResiduesList = new SequenceList(Sequence.KIND_PROTEIN);
                mInTargetSeqTag = true;
            } // Must find parent id.
        } // Found residues tag

    } // End method: startElement

    /**
     * Called by parser when element end tag was encountered.
     *
     * @param lUri - The Namespace URI, or the empty string if the element has
     *  no Namespace URI or if Namespace processing is not being performed.
     * @param lLocal - The local name (without prefix), or the empty string if
     *  Namespace processing is not being performed.
     * @param lName - The qualified XML 1.0 name (with prefix), or the empty
     *  string if qualified names are not available.
     */
    public void endElement(String lUri, String lLocal, String lName) {

        if (mAllDataHasBeenCollected)
          stopParsing();

        // Decode which element is requested, sans any string comparisons.
        int foundCode = CEFParseHelper.getCEFParseHelper().translateToElementCode(lName);
        if (foundCode == CEFParseHelper.UNINTERESTING_ELEMENT_CODE)
            return;

         if (foundCode == CEFParseHelper.RESIDUES_CODE) {
            if (mInTargetSeqTag) {
                mGenomicSequence = mResiduesList;
            } // Convert/cleanup
        } // In residues "state"
        else if (foundCode == CEFParseHelper.SEQ_CODE) {
            if (mInTargetSeqTag) {
                mInTargetSeqTag = false;
                mAllDataHasBeenCollected = true;
            } // In seq with last of data.
        } // End of a seq element.

        int lLastAncestor = mAncestry.size() - 1;
        mAncestry.remove(lLastAncestor);
        mAncestorAttributes.remove(lLastAncestor);

    } // End method: endElement

    /**
     * Called by the parser when character data is encountered.
     * Note that a collection of characters "sent" here could be
     * part of a larger whole, within the rules of SAX.
     *
     * @param char[] lChars the array of character data found.
     * @param int lStart where it starts in the bigger collection.
     * @param int lLength how many characters found.
     */
    public void characters(char[] lChars, int lStart, int lLength) {
        if (mAllDataHasBeenCollected)
            stopParsing();

        String lCurrentElement = (String)mAncestry.get(mAncestry.size() - 1);
        int mapValue = CEFParseHelper.getCEFParseHelper().translateToElementCode(lCurrentElement);
        if (mapValue == CEFParseHelper.UNINTERESTING_ELEMENT_CODE)
            return;

        if (mapValue == CEFParseHelper.RESIDUES_CODE) {
            if (mInTargetSeqTag) {
                if (mSequenceType.equalsIgnoreCase(DNA_SEQ_TYPE)) {
                    Sequence lSeq = new DNASequenceParser(lChars, lStart, lLength);
                    mResiduesList.append(DNASequenceStorage.create(lSeq));
                } // DNA
                else if (mSequenceType.equalsIgnoreCase(AMINO_ACID_SEQ_TYPE)) {
                    Sequence lSeq = new ProteinSequenceParser(lChars, lStart, lLength);
                    ProteinSequenceStorage storage = new ProteinSequenceStorage(lSeq);
                    mResiduesList.append(storage);
                } // Amino Acid
            } // First residues
        } // In residues "state"

    } // End method: characters

    //-----------------------------------------HELPER METHODS
    /** Eliminates all state data so that a new run over file/new url may be made. */
    private void clearForNextPass() {
        mAncestry.clear();
        mAncestorAttributes.clear();
        mAlignmentsList.clear();
        mGenomicSequence = null;
        mAllDataHasBeenCollected = false;
        mInTargetSeqTag = false;
        mResiduesList = null;
        mSequenceType = null;
    } // End method: clearForNextPass

    /** Launch a scan to find the sequence given. */
    private void searchForSequence() {
        // Loads the file via a SAX parser.
        try {
            XMLReader lReader = XMLReaderFactory.createXMLReader(CEFParseHelper.DEFAULT_PARSER_NAME);
            lReader.setContentHandler(this);

            String lFullName = (mSource.indexOf(':') >= 2) ? mSource : "file:"+mSource;
            lReader.parse(lFullName);

        } catch (Exception ex) {
            if ((ex.getMessage() == null) ||
                (! ex.getMessage().equals(SUCCESS_MESSAGE)))
                FacadeManager.handleException(new Exception(ex.getMessage()+" for file "+mSource));
        } // End catch block for parse.
    } // End method: searchForSequence

    /** Launch a scan to find the sequence given.  Use this if constructed with string buffer. */
    private void searchForSequenceInStringBuffer() {
        try {
            XMLReader lReader = XMLReaderFactory.createXMLReader(CEFParseHelper.DEFAULT_PARSER_NAME);
            lReader.setContentHandler(this);

            // Parse the input.
            java.io.ByteArrayInputStream xmlStream
              = new java.io.ByteArrayInputStream(mContents.toString().getBytes());
            InputSource xmlInputSource = new InputSource(xmlStream);
            lReader.parse(xmlInputSource);

        } catch (Exception ex) {
            FacadeManager.handleException(ex);
        } // End catch block for parse.
    } // End method: searchForSequenceInStringBuffer

    /**
     * Given a list of attributes as provided to "startElement", create
     * a map out of it, so the values are not lost.
     */
    private Map buildAttributesMap(Attributes lAttrs) {
        Map returnMap = new HashMap();   // Guarantees SOMETHING always returns.
        for (int i = 0; i < lAttrs.getLength(); i++) {
            returnMap.put(lAttrs.getLocalName(i), lAttrs.getValue(i));
        } // For all input attributes

        return returnMap;
    } // End method: buildAttributesMap

    /**
     * Kills parse whenever it is called, by throwing an exception.
     * This is the only way SAX provides to discontinue file reads.
     */
    private void stopParsing() throws RuntimeException {
        throw new RuntimeException(SUCCESS_MESSAGE);
    } // ENd method

} // End class: SequenceLoader
