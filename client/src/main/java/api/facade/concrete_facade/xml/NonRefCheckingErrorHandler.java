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

import api.facade.concrete_facade.xml.sax_support.ElementContext;
import api.facade.concrete_facade.xml.sax_support.FeatureHandlerBase;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import java.util.Map;

/**
 * Title:        NonRefCheckingErrorHandler
 * Description:  ERROR handler class is required to trap errors coming from
 *               invalid XML files. This one does not record errors having to do
 *               with unresolvable id references.
 * @author Les Foster
 * @version $Id$
 */
public class NonRefCheckingErrorHandler implements ErrorHandler {
  public static final String TOO_MANY_ERRORS = "<<Too many errors to present at this time>>";
  public static final String BAD_IDREF_MESSAGE = "No element has an ID attribute with value";

  private static final int MAX_OUTPUT_LENGTH = 100000;

  private String mInputFile = null;
  private StringBuffer mOutputBuffer = null;
  private ElementContext mContext = null;
  private boolean mIncludeLineNumbersInContext = false;
  private final String LINE_SEPARATOR = System.getProperty("line.separator");

  /**
   * Constructor passes in pointer to input file, output buffer, and whether to
   * include the line numbers IF the context is provided.
   */
  public NonRefCheckingErrorHandler(String lInputFile, StringBuffer lOutputBuffer, boolean lIncludeLineNumbersInContext) {
    mInputFile = lInputFile;
    mOutputBuffer = lOutputBuffer;
    mIncludeLineNumbersInContext = lIncludeLineNumbersInContext;
  } // End constructor.

  /**
   * Constructor passes in pointer to input file, output buffer.
   */
  public NonRefCheckingErrorHandler(String lInputFile, StringBuffer lOutputBuffer) {
    this(lInputFile, lOutputBuffer, false);
  } // End constructor.

  /**
   * Set to help resolve loctions of errors.
   */
  public void setElementContext(ElementContext context) {
    mContext = context;
  } // End

  /** Receive notification of a recoverable error. */
  public void error(SAXParseException exception) {
    recordError(exception);
  } // End method: error

  /** Receive notification of a non-recoverable error. */
  public void fatalError(SAXParseException exception) {
    recordError(exception);
  } // End method: fatalError

  /** Receive notification of a warning. */
  public void warning(SAXParseException exception) {
    recordError(exception);
  } // End method: warning

  //----------------------------HELPER METHODS
  /**
   * Produces a very brief report based on line number.
   */
  private String reportLine(int lErrorLine) {
    return "Line "+(lErrorLine);
  } // End method: reportLine

  /**
   * Keeps track of the errors produced during file scan.
   */
  private void recordError(SAXParseException exception) {
    if (mOutputBuffer.length() >= MAX_OUTPUT_LENGTH)
      throw new ValidationOverflowException(mInputFile);

    if (! exception.getMessage().startsWith(BAD_IDREF_MESSAGE)) {
      if (mContext == null)
        mOutputBuffer.append(reportLine(exception.getLineNumber()));
      else
        enclosingTags();

      if (mIncludeLineNumbersInContext)
        mOutputBuffer.append(reportLine(exception.getLineNumber()));

      mOutputBuffer.append(" ["+exception.getMessage()+"]"+LINE_SEPARATOR);

    } // Got an error we wish to display.
  } // End method

  /**
   * Finds all start tags to establish tag hierarchy, around the current
   * tag or text.
   */
  private void enclosingTags() {
    String lNextAncestor = null;
    Map lNextAttributes = null;
    String lIdValue = null;
    for (int i = mContext.maxAncestor(); i >= 0; i--) {
      lNextAncestor = mContext.ancestorNumber(i);
      mOutputBuffer.append("<");
      mOutputBuffer.append(lNextAncestor);
      lNextAttributes = mContext.ancestorAttributesNumber(i);
      lIdValue = (String)lNextAttributes.get("id");
      if (lIdValue != null) {
        mOutputBuffer.append(" id=\"");
        mOutputBuffer.append(lIdValue);
        mOutputBuffer.append("\"");
      } // Got ID value.
      mOutputBuffer.append("...>");
    } // For all ancestors.
    mOutputBuffer.append("\n\t");
  } // End method

  //----------------------------INNER CLASSES
  public static class ParserForContext extends FeatureHandlerBase {
    /**
     * Called on subclass when element start tag encountered.
     */
    public void startElementTemplateMethod(ElementContext context) {
    } // Got start element -- do not care.

    /**
     * Called on subclass when element end tag was encountered.
     */
    public void endElementTemplateMethod(ElementContext context) {
    } // Got end element--do not care

    /**
     * Called on subclass for character content.  This one need not
     * be overridden.  If it is, implementation should make super call.
     */
    public void charactersTemplateMethod(char[] lCharacters, int lStart, int lLength,
        ElementContext lContext) {
    } // Got characters--do not care.

    /**
     * Returning the fill buffer map allows values to be assigned to buffers
     * local to current handler.
     */
    public java.util.Map getFillBufferMapTemplateMethod() {
      return java.util.Collections.EMPTY_MAP;
    } // Called for map

    //------------------------------IMPLEMENTATION OF ExceptionHandler
    public void handleException(Exception lException) {
    } // End method: handleException

  } // End class

  /** Special exception thrown as a preemptive measure. */
  public static class ValidationOverflowException extends RuntimeException {
    private String mBadFileName = null;
    public ValidationOverflowException(String lBadFileName) {
        super();
        mBadFileName = lBadFileName;
    } // End constructor
    public String getMessage() { return TOO_MANY_ERRORS; }
    public String getFailedFilename() { return mBadFileName; }
  } // End class

} // End class: NonRefCheckingErrorHandler
