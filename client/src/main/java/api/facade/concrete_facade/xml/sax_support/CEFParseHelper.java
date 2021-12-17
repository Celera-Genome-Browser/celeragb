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
package api.facade.concrete_facade.xml.sax_support;

import java.util.HashMap;
import java.util.Map;

/**
 * Title:        Genomics Exchange Format Parse Helper
 * Description:  Helper methods and constants for different kinds of parsers/handlers
 *               which use Genomics Exchange Format XML data.  Defines constants,
 *               sharable utility methods, and sharable static variables.
 * @author Les Foster
 * @version $Id$
 */
public class CEFParseHelper {

    //---------------------------------------------CONSTANTS
    // Define the set of all "interesting" element/tag names.
    public final static String ROOT_ELEMENT = "game";
    public final static String START_ELEMENT = "start";
    public final static String END_ELEMENT = "end";
    public final static String ALIGNMENT_ELEMENT = "alignment";
    public final static String SCORE_ELEMENT = "score";
    public final static String SPAN_TYPE_ELEMENT = "span_type";
    public final static String TYPE_ELEMENT = "type";
    public final static String NAME_ELEMENT = "name";
    public final static String EVIDENCE_ELEMENT = "evidence";
    public final static String ANNOTATION_SOURCE_ELEMENT = "annotation_source";
    public final static String SEQ_ELEMENT = "seq";
    public final static String AXIS_SEQ_ELEMENT = "axis_seq";
    public final static String RESIDUES_ELEMENT = "residues";
    public final static String COMMENTS_ELEMENT = "comments";
    public final static String SPAN_ELEMENT = "span";
    public final static String SEQ_RELATIONSHIP_ELEMENT = "seq_relationship";
    public final static String SEQ_ALIGNMENT_ELEMENT = "seq_alignment";
    public final static String DESCRIPTION_ELEMENT = "description";
    public final static String FEATURE_SPAN_ELEMENT = "feature_span";
    public final static String FEATURE_SET_ELEMENT = "feature_set";
    public final static String RESULT_SPAN_ELEMENT = "result_span";
    public final static String RESULT_SET_ELEMENT = "result_set";
    public final static String ANNOTATION_ELEMENT = "annotation";
    public final static String ANNOTATION_OBSOLETION_ELEMENT = "annotation_obsoletion";
    public final static String COMPUTATIONAL_ANALYSIS_ELEMENT = "computational_analysis";
    public final static String PROGRAM_ELEMENT = "program";
    public final static String PROPERTY_ELEMENT = "property";
    public final static String REPLACED_ELEMENT = "replaced";
    public final static String OUTPUT_ELEMENT = "output";
    public final static String VALUE_ELEMENT = "value";

    // Define the set of "interesting" attribute names.
    public final static String ID_ATTRIBUTE = "id";
    public final static String IDS_ATTRIBUTE = "ids";
    public final static String TYPE_ATTRIBUTE = "type";
    public final static String NAME_ATTRIBUTE = "name";
    public final static String VALUE_ATTRIBUTE = "value";
    public final static String EDITABLE_ATTRIBUTE = "editable";
    public final static String SUBJECT_SEQ_RELATIONSHIP = "subject";
    public final static String SUBJECT_SEQ_RELATIONSHIP_ALT = "sbjct";
    public final static String QUERY_SEQ_RELATIONSHIP = "query";
    public final static String AUTHOR_ATTRIBUTE = "author";
    public final static String DATE_ATTRIBUTE = "date";
    public final static String RESULT_ATTRIBUTE = "result";

    // Define codes for all of the element names listed above, for use in an
    // optimization step.
    public static final int UNINTERESTING_ELEMENT_CODE = -1; // Code for element not included in table.
    private static int codeCounter = 0;
    public static final int GAME_CODE = ++codeCounter;
    public static final int START_CODE = ++codeCounter;
    public static final int END_CODE = ++codeCounter;
    public static final int ALIGNMENT_CODE = ++codeCounter;
    public static final int SCORE_CODE = ++codeCounter;
    public static final int SPAN_TYPE_CODE = ++codeCounter;
    public static final int TYPE_CODE = ++codeCounter;
    public static final int NAME_CODE = ++codeCounter;
    public static final int EVIDENCE_CODE = ++codeCounter;
    public static final int ANNOTATION_SOURCE_CODE = ++codeCounter;
    public static final int SEQ_CODE = ++codeCounter;
    public static final int AXIS_SEQ_CODE = ++codeCounter;
    public static final int RESIDUES_CODE = ++codeCounter;
    public static final int COMMENTS_CODE = ++codeCounter;
    public static final int SPAN_CODE = ++codeCounter;
    public static final int SEQ_RELATIONSHIP_CODE = ++codeCounter;
    public static final int SEQ_ALIGNMENT_CODE = ++codeCounter;
    public static final int DESCRIPTION_CODE = ++codeCounter;
    public static final int FEATURE_SPAN_CODE = ++codeCounter;
    public static final int FEATURE_SET_CODE = ++codeCounter;
    public static final int RESULT_SPAN_CODE = ++codeCounter;
    public static final int RESULT_SET_CODE = ++codeCounter;
    public static final int ANNOTATION_CODE = ++codeCounter;
    public static final int ANNOTATION_OBSOLETION_CODE = ++codeCounter;
    public static final int COMPUTATIONAL_ANALYSIS_CODE = ++codeCounter;
    public static final int PROGRAM_CODE = ++codeCounter;
    public static final int PROPERTY_CODE = ++codeCounter;
    public static final int REPLACED_CODE = ++codeCounter;
    public static final int OUTPUT_CODE = ++codeCounter;
    public static final int VALUE_CODE = ++codeCounter;

    public final static String DEFAULT_ASSEMBLY_VERSION_STRING = "0";
    public final static String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
    public final static String DEFAULT_SPECIES_NAME = "Unknown species";

    //---------------------------------------------INSTANCE VARIABLES
    private static Map elementLookup;
    private static CEFParseHelper helper;

    //---------------------------------------------INTERFACE METHODS
    /** Gets the only instance.  The "restricted opening" for getting singleton instance. */
    public static CEFParseHelper getCEFParseHelper() {
        if (helper == null)
            helper = new CEFParseHelper();
        return helper;
    } // End method: getCEFParseHelper

    /**
     * Given an element string, translate it to a code using
     * a hash lookup.
     */
    public int translateToElementCode(String elementName) {
       Integer codeObject = (Integer)elementLookup.get(elementName);
       if (codeObject == null)
           return UNINTERESTING_ELEMENT_CODE;
       else
           return codeObject.intValue();

    } // End method: translateToElementCode
    //---------------------------------------------CONSTRUCTORS
    /**
     * Constructor will build the string-to-numeric map for optizing away
     * string comparisons.
     */
    private CEFParseHelper() {
        elementLookup = new HashMap();
        populateElementLookup();  // Optimization to avoid string comparisons.
    } // End constructor

    /**
     * Builds map for use in checking element names.  Intended to reduce
     * string comparison overhead.
     */
    private void populateElementLookup() {
        elementLookup.put(CEFParseHelper.ROOT_ELEMENT, new Integer(GAME_CODE));
        elementLookup.put(CEFParseHelper.START_ELEMENT, new Integer(START_CODE));
        elementLookup.put(CEFParseHelper.END_ELEMENT, new Integer(END_CODE));
        elementLookup.put(CEFParseHelper.ALIGNMENT_ELEMENT, new Integer(ALIGNMENT_CODE));
        elementLookup.put(CEFParseHelper.SCORE_ELEMENT, new Integer(SCORE_CODE));
        elementLookup.put(CEFParseHelper.SPAN_TYPE_ELEMENT, new Integer(SPAN_TYPE_CODE));
        elementLookup.put(CEFParseHelper.TYPE_ELEMENT, new Integer(TYPE_CODE));
        elementLookup.put(CEFParseHelper.NAME_ELEMENT, new Integer(NAME_CODE));
        elementLookup.put(CEFParseHelper.EVIDENCE_ELEMENT, new Integer(EVIDENCE_CODE));
        elementLookup.put(CEFParseHelper.ANNOTATION_SOURCE_ELEMENT, new Integer(ANNOTATION_SOURCE_CODE));
        elementLookup.put(CEFParseHelper.SEQ_ELEMENT, new Integer(SEQ_CODE));
        elementLookup.put(CEFParseHelper.AXIS_SEQ_ELEMENT, new Integer(AXIS_SEQ_CODE));
        elementLookup.put(CEFParseHelper.RESIDUES_ELEMENT, new Integer(RESIDUES_CODE));
        elementLookup.put(CEFParseHelper.COMMENTS_ELEMENT, new Integer(COMMENTS_CODE));
        elementLookup.put(CEFParseHelper.SPAN_ELEMENT, new Integer(SPAN_CODE));
        elementLookup.put(CEFParseHelper.SEQ_RELATIONSHIP_ELEMENT, new Integer(SEQ_RELATIONSHIP_CODE));
        elementLookup.put(CEFParseHelper.SEQ_ALIGNMENT_ELEMENT, new Integer(SEQ_ALIGNMENT_CODE));
        elementLookup.put(CEFParseHelper.DESCRIPTION_ELEMENT, new Integer(DESCRIPTION_CODE));
        elementLookup.put(CEFParseHelper.FEATURE_SPAN_ELEMENT, new Integer(FEATURE_SPAN_CODE));
        elementLookup.put(CEFParseHelper.FEATURE_SET_ELEMENT, new Integer(FEATURE_SET_CODE));
        elementLookup.put(CEFParseHelper.RESULT_SPAN_ELEMENT, new Integer(RESULT_SPAN_CODE));
        elementLookup.put(CEFParseHelper.RESULT_SET_ELEMENT, new Integer(RESULT_SET_CODE));
        elementLookup.put(CEFParseHelper.ANNOTATION_ELEMENT, new Integer(ANNOTATION_CODE));
        elementLookup.put(CEFParseHelper.ANNOTATION_OBSOLETION_ELEMENT, new Integer(ANNOTATION_OBSOLETION_CODE));
        elementLookup.put(CEFParseHelper.COMPUTATIONAL_ANALYSIS_ELEMENT, new Integer(COMPUTATIONAL_ANALYSIS_CODE));
        elementLookup.put(CEFParseHelper.PROGRAM_ELEMENT, new Integer(PROGRAM_CODE));
        elementLookup.put(CEFParseHelper.PROPERTY_ELEMENT, new Integer(PROPERTY_CODE));
        elementLookup.put(CEFParseHelper.REPLACED_ELEMENT, new Integer(REPLACED_CODE));
        elementLookup.put(CEFParseHelper.OUTPUT_ELEMENT, new Integer(OUTPUT_CODE));
        elementLookup.put(CEFParseHelper.VALUE_ELEMENT, new Integer(VALUE_CODE));
    } // End method: populateElementLookup

} // End class: CEFParseHelper
