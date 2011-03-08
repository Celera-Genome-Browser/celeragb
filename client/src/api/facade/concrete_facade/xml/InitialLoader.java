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
 * Description:  First pass reader of the XML file for whole-file applications<.<p>
 * Company:      []<p>
 * @author Les Foster
 * @version  CVS_ID:  $Id$
 */
package api.facade.concrete_facade.xml;

import api.facade.concrete_facade.xml.sax_support.CEFParseHelper;
import api.facade.facade_mgr.FacadeManager;
import api.stub.sequence.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.util.*;

/**
 * A handler for XML events from a SAX parser, this class picks up all info
 * required initially when opening an XML file.  This includes size of the
 * axis, the axis OID, and header info for whole file.
 */
public class InitialLoader extends DefaultHandler {

    private String SUCCESS_MESSAGE = "SUCCESS!";
    private final static int LENGTH_NOT_FOUND = -1;

    private int mSequenceCount = 0;

    private List mAncestry = new ArrayList();
    private List mAncestorAttributes = new ArrayList();
    private List mAlignmentsList = new ArrayList();

    private String mSpecies = null;
    private String mAssembly = null;
    private String mGenomicAxisID = null;
    private Sequence mGenomicSequence = null;
    private StringBuffer mTempSequence = null;
    private int mSeqLength = LENGTH_NOT_FOUND;
    private boolean mAllDataHasBeenCollected = false;
    private boolean mInFinalSeqTag = false;
    private SequenceList mResiduesList = null;
    private SequenceBuilder mSequenceBuilder = null;

    //----------------------------CONSTRUCTORS
    /**
     * Constructor gets the first required data from the input file
     * using a SAX element scan.
     */
    public InitialLoader(String lFileName) {
        // Loads the file via a SAX parser.
        try {
            XMLReader lReader = XMLReaderFactory.createXMLReader(CEFParseHelper.DEFAULT_PARSER_NAME);
            lReader.setContentHandler(this);

            String lFullFileName = (lFileName.indexOf(':') >= 2) ? lFileName : "file:"+lFileName;
            lReader.parse(lFullFileName);

        } catch (Exception ex) {
            if ((ex.getMessage() == null) ||
                (! ex.getMessage().equals(SUCCESS_MESSAGE)))
                FacadeManager.handleException(new Exception(ex.getMessage()+" for file "+lFileName));
        } // End catch block for parse.

    } // End constructor

    /**
     * Constructor gets the first required data from the input
     * using a SAX element scan.  This override provides the facility to
     * do this for a stringbuffer with full contents, not just a file.
     */
    public InitialLoader(StringBuffer lContents) {

        try {
            XMLReader lReader = XMLReaderFactory.createXMLReader(CEFParseHelper.DEFAULT_PARSER_NAME);
            lReader.setContentHandler(this);

            // Parse the input.
            java.io.ByteArrayInputStream xmlStream
              = new java.io.ByteArrayInputStream(lContents.toString().getBytes());
            InputSource xmlInputSource = new InputSource(xmlStream);
            lReader.parse(xmlInputSource);

        } catch (Exception ex) {
            FacadeManager.handleException(ex);
        } // End catch block for parse.

    } // End constructor

    //-----------------------------PUBLIC INTERFACE
    /**
     * These getters return all info captured by this
     * pass over the data.
     */
    public String getSpecies() { return mSpecies; }
    public String getAssembly() { return mAssembly; }
    public int getAxisLength() {
        if (mSeqLength > 0)
            return mSeqLength;
        else
            return (mResiduesList == null) ? 0 : (int)mResiduesList.length();
    } // End method: getAxisLength
    public String getGenomicAxisID() { return mGenomicAxisID; }
    public Sequence getSequence() { return mGenomicSequence; }
    public List getSequenceAlignments() { return mAlignmentsList; }
    public SequenceBuilder getSequenceBuilder() { return mSequenceBuilder; }

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

        if (foundCode == CEFParseHelper.GAME_CODE) {
            mSpecies = lAttrs.getValue("taxon");
            mAssembly = lAttrs.getValue("assembly_version");
            if ((mAssembly == null) || (mAssembly.length() == 0))
                mAssembly = CEFParseHelper.DEFAULT_ASSEMBLY_VERSION_STRING;
        } // Found game tag
        else if (foundCode == CEFParseHelper.AXIS_SEQ_CODE) {

            mGenomicAxisID = lAttrs.getValue("id");
            if (mGenomicAxisID == null)
                throw new IllegalArgumentException(CEFParseHelper.AXIS_SEQ_ELEMENT+" not given required id attribute");

            String lFastaPath = lAttrs.getValue("fasta_path");
            if (lFastaPath == null)
                throw new IllegalArgumentException(CEFParseHelper.AXIS_SEQ_ELEMENT+" not given required fasta_path attribute");

            try {
                mSequenceBuilder = new SequenceFromFastaBuilder(lFastaPath);
                // This should provide all info to build the axis, and obtain residues.
                if (lAttrs.getValue("length") != null)
                    mSeqLength = Integer.parseInt(lAttrs.getValue("length"));
                else
                    mSeqLength = (int)mSequenceBuilder.getLength();

            } // Trying to make finder.
            catch (Exception ex) {
                FacadeManager.handleException(ex);
            } // End catch block for finder build.
        } // Found axis seq tag.
        else if (foundCode == CEFParseHelper.SEQ_CODE) {
            // Capture the seq length attribute.  Will be needed if
            // the seq contains no residues.
            if ((mSeqLength < 0) && (mSequenceCount < 1)) {
                String lSeqLengthAttribute = lAttrs.getValue("length");
                if (lSeqLengthAttribute != null) {
                    try {
                        mSeqLength = Integer.parseInt(lSeqLengthAttribute);
                    } catch (NumberFormatException nfe) {
                        mSeqLength = LENGTH_NOT_FOUND;
                    } // End catch block for seq conv.
                    mGenomicAxisID = lAttrs.getValue("id");
                } // Got attribute.
            } // Attribute not found previously.
        } // Found seq tag.
        else if (foundCode == CEFParseHelper.RESIDUES_CODE) {
            String lParentTag = (String)mAncestry.get(mAncestry.size() - 2);
            if (lParentTag.equals(CEFParseHelper.SEQ_ELEMENT) && (mSequenceCount < 1)) {
                // Prepare to save sequence letters.
                mResiduesList = new SequenceList(Sequence.KIND_DNA);

                // Find the parent ID attribute.  That attribute value is for
                // the axis to be constructed on demand.
                Map lParentAttributes = (Map)mAncestorAttributes.get(mAncestorAttributes.size() - 2);
                mGenomicAxisID = (String)lParentAttributes.get("id");
            } // Must find parent id.
        } // Found residues tag
        else if (foundCode == CEFParseHelper.SEQ_ALIGNMENT_CODE) {
            String lParentTag = (String)mAncestry.get(mAncestry.size() - 2);
            if (lParentTag.equals(CEFParseHelper.SEQ_ELEMENT) || lParentTag.equals(CEFParseHelper.AXIS_SEQ_ELEMENT)) {
                Map lParentAttributes = (Map)mAncestorAttributes.get(mAncestorAttributes.size() - 2);
                mGenomicAxisID = (String)lParentAttributes.get("id");
                mAlignmentsList.add(new SequenceAlignment(lAttributesMap, mGenomicAxisID));
            } // Sanity check.
        } // Found seq_alignment tag

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
            if (mSequenceCount < 1) {
                mGenomicSequence = mResiduesList;
                mInFinalSeqTag = true;  // Optimization: no testing after interesting elements seen.
            } // Convert/cleanup
        } // In residues "state"
        else if (foundCode == CEFParseHelper.SEQ_CODE) {
            mSequenceCount ++;
            if (mInFinalSeqTag) {
                mInFinalSeqTag = false;
                mAllDataHasBeenCollected = true;
            } // In seq with last of data.
        } // End of a seq element.
        else if (foundCode == CEFParseHelper.AXIS_SEQ_CODE) {
            // NOTE: at this point, any sequence alignments contained in the
            // axis seq, will have been examined.
            mAllDataHasBeenCollected = true;
        } // End of the axis seq.

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
            if (mSequenceCount < 1) {
                Sequence lSeq = new DNASequenceParser(lChars, lStart, lLength);
                mResiduesList.append(DNASequenceStorage.create(lSeq));
            } // First residues
        } // In residues "state"

    } // End method: characters

    //-----------------------------------------HELPER METHODS
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
     * It may be that (if this gba file aligns others), there is a need to
     * provide its sequence to the sequence alignment objects it aligns.
     * If so, that will be handled here.
     */
    private void applyAlignerSequence() {
        if (mAlignmentsList.size() > 0) {
            SequenceAlignment lAlignment = null;
            for (Iterator it = mAlignmentsList.iterator(); it.hasNext(); ) {
                lAlignment = (SequenceAlignment)it.next();
                lAlignment.setAlignerSequence(mGenomicSequence);
                lAlignment.setSequenceBuilder(mSequenceBuilder);
            } // FOr all seq alignments

            // Once the alignments have access to the residues, remove them
            // from this "aligner" loader.
            mGenomicSequence = null;
            mSequenceBuilder = null;
        } // Have alignments.
    } // End method: applyAlignerSequence
    /**
     * Kills parse whenever it is called, by throwing an exception.
     * This is the only way SAX provides to discontinue file reads.
     */
    private void stopParsing() throws RuntimeException {
        applyAlignerSequence();
        throw new RuntimeException(SUCCESS_MESSAGE);
    } // ENd method

} // End class: InitialLoader
