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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * SAX handler.  Keeps context information, and delegates real treatment of
 * what it encounters to other classes.
 */
public class GenomicsExchangeHandler extends DefaultHandler {

    //----------------------------MEMBER VARIABLES
    private ElementStacker mElementStack = null;
    private FeatureHandlerBase mFeatureHandler = null;
    private List mPropertyStack = new ArrayList();   // Ancestry of properties.
                 // Vertical properties (above)
    private List mPropertySources = new ArrayList(); // Root sibling properties.
                 // Horizontal properties (above)--first level below container.

    private boolean DEBUG_CLASS = false;
    private ElementContext mElementContext = null;

    //----------------------------CONSTRUCTORS
    /**
     * This constructor does not build a parser.
     */
    public GenomicsExchangeHandler(ElementStacker lElementStack, FeatureHandlerBase lFeatureHandler) {

        this();
        mElementStack = lElementStack;
        mFeatureHandler = lFeatureHandler;
        mElementContext = new ElementContext(lElementStack);

    } // End constructor

    /**
     * Simple constructor.  For times when no element stack is immediately
     * available.
     */
    public GenomicsExchangeHandler() {
    } // End constructor

    //-----------------------------PUBLIC INTERFACE
    /**
     * Loading method.  Takes the file name and starts a SAX parse against it.
     */
    public void loadFile(String lFileName) {
        // System.out.println("Loading input file "+lFileName+" starting at "+new java.util.Date());
        // Loads the file via a SAX parser.
        try {
            XMLReader lReader = XMLReaderFactory.createXMLReader(CEFParseHelper.DEFAULT_PARSER_NAME);

            lReader.setContentHandler(this);

            String lFullFileName = (lFileName.indexOf(':') >= 2) ? lFileName : "file:"+lFileName;
            lReader.parse(lFullFileName);

        } catch (Exception ex) {
            mFeatureHandler.handleException(ex);
        } // End catch block for parse.
        // System.out.println(lFileName+" finishing at "+new java.util.Date());
    } // End method: loadFile

    /**
     * Loads contents of a string buffer, feeds it to a sax parser,
     * and reacts to its events.
     */
    public void loadStringBuffer(StringBuffer lContents) {
        try {
            XMLReader lReader = XMLReaderFactory.createXMLReader(CEFParseHelper.DEFAULT_PARSER_NAME);
            lReader.setContentHandler(this);

            // Parse the input.
            java.io.ByteArrayInputStream xmlStream
              = new java.io.ByteArrayInputStream(lContents.toString().getBytes());
            InputSource xmlInputSource = new InputSource(xmlStream);
            lReader.parse(xmlInputSource);

        } catch (Exception ex) {
            mFeatureHandler.handleException(ex);
        } // End catch block for parse.

    } // End method: loadStringBuffer

    /**
     * Allows external control of the element stack.  This stack
     * is required to keep track of the current element's
     * ancestry.
     */
    public void setElementStacker(ElementStacker lStacker) {
        mElementStack = lStacker;
        mElementContext = new ElementContext(lStacker);
    } // End method: setElementStacker

    /**
     * Deposits a delegate handler to process events.
     */
    public void setFeatureHandler(FeatureHandlerBase lFeatureHandler) {
        mFeatureHandler = lFeatureHandler;
    } // End method: setFeatureHandler

    /**
     * Returns element context for external use to parser.
     */
    public ElementContext getElementContext() {
        return mElementContext;
    } // End method: getElementContext

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
        try {
            if (DEBUG_CLASS) {
                System.err.print("<"+lName);
                for (int i = 0; i < lAttrs.getLength(); i++) {
                    System.err.print(" "+lAttrs.getLocalName(i)+"=\""+lAttrs.getValue(i)+"\"");
                } // For all attributes
                System.err.println(">");
            } // End debug statement.

            mElementStack.pushAncestry(lName, buildAttributesMap(lAttrs));

            // Allows delegation to subclass for its start element behavior.
            mFeatureHandler.startElement(mElementContext);

        } catch (Exception ex) {
            // NOTE: must catch here, otherwise, gets rethrown as SAX excep.
            mFeatureHandler.handleException(ex);
        } // End catch block.

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

        try {
            if (DEBUG_CLASS)
                System.err.println("</"+lName+">");

            // Allows delegation of behavior to subclass.
            mFeatureHandler.endElement(mElementContext);

            // Removes this element from the element stack.
            mElementStack.popAncestry();

        } catch (Exception ex) {
            // NOTE: must catch here, otherwise, gets rethrown as SAX excep.
            mFeatureHandler.handleException(ex);
        } // End catch block.

    } // End method: endElement

    /**
     * Called by parser when element end tag was encountered.
     *
     * @param String lName the element which just ended.
     */
    public void characters(char[] lCharacters, int lStart, int lLength) {

        try {
            if (DEBUG_CLASS)
                System.err.println(new String(lCharacters, lStart, lLength).trim());

            // Allows delegation to subclass.
            mFeatureHandler.characters(lCharacters, lStart, lLength, mElementContext);
        } catch (Exception ex) {
            // NOTE: must catch here, otherwise, gets rethrown as SAX excep.
            mFeatureHandler.handleException(ex);
        } // End catch block.

    } // End method: characters

    /**
     * If any whitespace which may safely be ignored is found,
     * it is sent to this routine.
     *
     * @param char[] lChars the characters found.
     * @param int lStart the starting pos.
     * @param int lLength how many were found.
     */
    public void ignorableWhitespace(char[] lChars, int lStart, int lLength) {

    } // End method: ignorableWhiteSpace

    /**
     * Any procesing instructions encountered will pass through this method.
     */
    public void processingInstruction(String s1, String s2) {

    } // End method: processingInstruction

    //-----------------------------------------HELPER METHODS
    /**
     * Given a list of attributes as provided to "startElement", create
     * a map out of it, so the values are not lost.
     */
    protected Map buildAttributesMap(Attributes lAttrs) {
        if (lAttrs.getLength() == 0)
            return Collections.EMPTY_MAP;

        HashMap returnMap = new HashMap();   // Guarantees SOMETHING always returns.
        for (int i = 0; i < lAttrs.getLength(); i++) {
            returnMap.put(lAttrs.getLocalName(i), lAttrs.getValue(i));
        } // For all input attributes

        return returnMap;
    } // End method: buildAttributesMap

} // End class: GenomicsExchangeHandler

/*
  $Log$
  Revision 1.3  2002/11/07 16:06:58  lblick
  Removed obsolete imports and unused local variables.

  Revision 1.2  2002/04/05 19:48:45  lfoster
  Removed refs to FacadeManager from sax support classes.  Wrapped facademanager handleexception calls in instance method calls.

  Revision 1.1  2002/04/05 19:06:58  lfoster
  Moved 8 classes from xml down to xml.sax_support.  Removed dep of PropertySource on abstract facades.

  Revision 1.15  2002/02/27 22:14:12  lfoster
  Made it possible to check context for error reporting during validation.

  Revision 1.14  2002/01/21 19:17:39  lfoster
  Switched from Linked List to Array List for element stack.  Caching the seq relationship
  type attribute rather than prowling the element stack looking for it.

  Revision 1.13  2002/01/21 19:11:53  jojicon
  Redesign of DNASequence and DNASequenceCache.

  Revision 1.12  2001/11/15 14:58:07  lfoster
  Commented debug info.

  Revision 1.11  2001/08/28 13:26:02  lfoster
  Switched over to SAX version 2.0 in all parsers.

  Revision 1.10  2001/06/28 20:27:02  lfoster
  Commented out JAXP import.

  Revision 1.9  2001/06/28 19:30:30  lfoster
  Commented JAXP-using method.  Uncomment, and change the attributes by removing the boolean parameter, and you can try out JAXP!

  Revision 1.8  2001/05/03 17:37:47  lfoster
  Toggled  debug setting off.

  Revision 1.7  2001/05/03 16:14:24  jbaxenda
  Turning debug on.

  Revision 1.6  2001/03/20 20:23:03  lfoster
  Migrated all mention of default parser class name string into a helper class, along with some shared constants.

  Revision 1.5  2001/01/12 17:02:56  lfoster
  No longer calling "textOfCurrentElement" with context: using name of element to avoid forcing calls to "currentElement()" within the method.

  Revision 1.4  2001/01/11 21:28:26  lfoster
  Relocated some things so as 1)not to expose lists to clients; and 2)to allow single-stack implementation of element stacker.

  Revision 1.3  2001/01/04 23:22:15  lfoster
  New delegation mechanism in place.

  Revision 1.2  2000/11/08 06:05:04  lfoster
  Preliminary changes to deal with adjustment of ranges of features.

  Revision 1.1  2000/10/26 13:47:58  lfoster
  No longer implementing SAX handler in all delegated model-building classes.

  Revision 1.16  2000/10/24 14:01:19  lfoster
  Corrected a comment.

  Revision 1.15  2000/10/23 18:07:50  lfoster
  Fixed discrepancies in side-by-side testing versus previous (DOM) trunk version.

  Revision 1.14  2000/10/23 01:41:17  lfoster
  Second attempt at fixing memory leak.  Moved away from over-
  dependence on StringBuffer.

  Revision 1.13  2000/10/20 22:41:20  lfoster
  Normalized some naming, conventionalized on handling of element text.

  Revision 1.12  2000/10/19 20:02:01  lfoster
  First attempt at fixing memory leak.

  Revision 1.11  2000/10/17 15:40:55  lfoster
  Added code for storing replaces and properties information in models being created.

  Revision 1.10  2000/10/17 14:06:27  lfoster
  Added code for retrieving replaced tag data.

  Revision 1.9  2000/10/16 22:19:27  lfoster
  Added code for retrieving evidence, property, comments, and a start on replaced,
  but so far no hookup or test.

  Revision 1.8  2000/10/16 16:16:03  lfoster
  Implemented more functionality for retrieving models.

  Revision 1.7  2000/10/16 04:14:57  lfoster
  Got past some more bugs in model collecting.

  Revision 1.6  2000/10/13 22:24:14  lfoster
  Fixed delegation of character handling.

  Revision 1.5  2000/10/12 04:10:03  lfoster
  Now collecting to target buffer at encounter instead of end element.

  Revision 1.4  2000/10/10 22:01:28  lfoster
  Partially-debugged.

  Revision 1.3  2000/10/10 19:28:06  lfoster
  Now have compiling, debuggable implementation of feature-collector in SAX.

*/
