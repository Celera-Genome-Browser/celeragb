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
package api.entity_model.access.report;

/**
 * Title:        Alignment Report
 * Description:  Report object with line items to represent alignment of query
 *               and subject text.
 * @author Les Foster
 * @version $Id$
 */

import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.HSPFeature;
import api.entity_model.model.annotation.HitAlignmentFeature;
import api.facade.abstract_facade.annotations.HitAlignmentDetailLoader;
import api.facade.abstract_facade.annotations.HitAlignmentFacade;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;
import shared.util.WhiteSpaceUtils;

import java.util.*;

/**
 * Report object with items for subject and query.
 */
public class AlignmentReport extends AbstractReport {

    // Note: prefix of c if class variable, m if member of instance, and l if local variable.

    //---------------------------------------CONSTANTS
    private static final String CHOSEN_FONT = "courier";

    private static final String HEADER_STYLE = "style=\"font: 12pt\"";

    private static final String FIXED_FONT_START = "<SPAN style=\"font: 10pt "+CHOSEN_FONT+"\">";
    private static final String FIXED_FONT_END = "</SPAN>";

    private static final String QUERY_LINE_ITEM = FIXED_FONT_START+"Query"+FIXED_FONT_END;
    private static final String SUBJECT_LINE_ITEM = FIXED_FONT_START+"Subject"+FIXED_FONT_END;

    private static final String PARENT_HDR = "Parent Id";
    private static final String FEATURE_HDR = "Feature Id";
    private static final String SUBJECT_HDR = "Subject Seq Id";
    private static final String TYPE_HDR = "Type";
    private static final String ALIGNMENT_HDR = "Alignment Text";
    private static final String ALTERNATE_ACCESSION_HDR = "Alternate Accession";
    private static final String ACCESSION_HDR = "Accession";
    private static final String DESCRIPTION_HDR = "Description";

    //----------------------------------------CONSTRUCTORS
    /** Simple constructor, for adding lines individually. */
    public AlignmentReport() {
    } // End constructor

    //----------------------------------------INTERFACE METHODS
    /** Uses a collection of entities to produce line items for the report. */
    public void createLineItemsFrom(Collection lEntities) {
        HSPFeature lFeature = null;
        OID lParentOid = null;
        HitAlignmentFeature lParentFeature = null;
        OID lFeatureOid = null;
        String lSubjSeqId = null;
        String lAccession = null;
        String lAltAccession = null;
        String lDescription = null;
        String lQueryResidues = null;
        String lSubjectResidues = null;
        LineItem lQueryLineItem = null;
        LineItem lSubjectLineItem = null;
        java.util.TreeMap lCollectionSorter = new java.util.TreeMap();
        String lKey = null;
        for (Iterator it = lEntities.iterator(); it.hasNext(); ) {
            // Collection contains only HSP features.
            lFeature = (HSPFeature)it.next();
            if (lFeature == null)
                continue;

            lParentFeature = (HitAlignmentFeature)lFeature.getSuperFeature();
            if (lParentFeature == null)
                lParentOid = null;
            else
                lParentOid = lParentFeature.getOid();

            lFeatureOid = lFeature.getOid();

            lQueryResidues = lFeature.loadQueryAlignedResiduesBlocking();
            if (lQueryResidues == null) {
                char[] lRes = new char[((GeometricAlignment)lFeature.getOnlyAlignmentToOnlyAxis()).getRangeOnAxis().getMagnitude()];
                                       Arrays.fill(lRes, '?');
                lQueryResidues = new String(lRes);
            } // Second source for query res.

            lSubjectResidues = lFeature.loadSubjectAlignedResiduesBlocking();
            if (lSubjectResidues == null) {
                char[] lRes = new char[((GeometricAlignment)lFeature.getOnlyAlignmentToOnlyAxis()).getRangeOnAxis().getMagnitude()];
                Arrays.fill(lRes, '?');
                lSubjectResidues = new String(lRes);
            } // Second source for subj res.

            GenomicProperty lFeatureProperty = null;

            lFeatureProperty = lParentFeature.getProperty(HitAlignmentFacade.SUBJECT_SEQ_ID_PROP);
            if (lFeatureProperty != null)
                lSubjSeqId = lFeatureProperty.getInitialValue();
            else
                lSubjSeqId = "";

            lFeatureProperty = lFeature.getProperty(HitAlignmentDetailLoader.ACCESSSION_NUM_PROP);
            if (lFeatureProperty != null)
                lAccession = lFeatureProperty.getInitialValue();
            else {
                lFeatureProperty = lParentFeature.getProperty(HitAlignmentFacade.ACCESSSION_NUM_PROP);
                if (lFeatureProperty != null)
                    lAccession = lFeatureProperty.getInitialValue();
                else
                    lAccession = "";
            } // Null.

            lFeatureProperty = lFeature.getProperty(HitAlignmentDetailLoader.ALT_ACCESSION_PROP);
            if (lFeatureProperty != null)
                lAltAccession = lFeatureProperty.getInitialValue();
            else
                lAltAccession = "";

            lFeatureProperty = lFeature.getProperty(HitAlignmentDetailLoader.DESCRIPTION_PROP);
            if (lFeatureProperty != null)
                lDescription = lFeatureProperty.getInitialValue();
            else {
                lFeatureProperty = lParentFeature.getProperty(HitAlignmentFacade.DESCRIPTION_PROP);
                if (lFeatureProperty != null)
                    lDescription = lFeatureProperty.getInitialValue();
                else
                    lDescription = "";
            } // No prop on feature.

            lQueryLineItem = new AlignmentReport.AlignmentReportLineItem("Query", formatResidues(lQueryResidues), lParentOid, lFeatureOid, lAccession, lAltAccession, lDescription, lSubjSeqId);
            lSubjectLineItem = new AlignmentReport.AlignmentReportLineItem("Subject", formatResidues(lSubjectResidues), lParentOid, lFeatureOid, lAccession, lAltAccession, lDescription, lSubjSeqId);

            lKey = lParentOid.toString() + ":" + lFeatureOid.toString() + "Query";
            lCollectionSorter.put(lKey, lQueryLineItem);
            lKey = lParentOid.toString() + ":" + lFeatureOid.toString() + "Subject";
            lCollectionSorter.put(lKey, lSubjectLineItem);
        } // For all entities in collection

        for (Iterator it = lCollectionSorter.values().iterator(); it.hasNext(); ) {
            addLineItem((LineItem)it.next());
        } // For all line items in collection.

    } // End method: createLineItemsFrom

    //----------------------------------------TEMPLATE OVERRIDES
    /** Makes a table data tag better suited for this report. */
    protected String getHtmlTableDataTag(int lRow, int lColumn) {
        if (lColumn == AlignmentReportLineItem.getColumnNumberFor(TYPE_HDR))
            return "<TD align=\"center\">";
        else
            return "<TD align=\"left\">";
    } // End method: getHtmlTableDataTag

    /** Makes a table header appropriate for this report. */
    protected String getHtmlTableHeaderTag(int lColumn) {
        return "<TH "+HEADER_STYLE+">";
    } // End method: getHtmlTableHeaderTag

    //----------------------------------------HELPER METHODS
    /**
     * Wraps font-setting tags around the alignment text and returns same.
     */
    static String present(Object lPresentable) {
        return FIXED_FONT_START + lPresentable.toString() + FIXED_FONT_END;
    } // End method: presetFont

    /** Helper to carry out consistent formatting of residues text. */
    private String formatResidues(String lResidues) {
        if (lResidues != null) {
            lResidues = WhiteSpaceUtils.stripNonResidueStartEndChar(lResidues);
        } // Formatting the residues
        return lResidues;
    } // End method: formatResidues

    //----------------------------------------INNER CLASSES
    /**
     * Line items to be used in the alignment report.
     */
    static public class AlignmentReportLineItem implements LineItem {

        //------------------------------------CLASS DATA
        protected static List cFields=new ArrayList();

        static {
           cFields.add(PARENT_HDR);
           cFields.add(FEATURE_HDR);
           cFields.add(SUBJECT_HDR);
           cFields.add(ACCESSION_HDR);
           cFields.add(ALTERNATE_ACCESSION_HDR);
           cFields.add(DESCRIPTION_HDR);
           cFields.add(TYPE_HDR);
           cFields.add(ALIGNMENT_HDR);
        } // End fields def.

        //------------------------------------CLASS METHODS
        /**
         * Returns column offset for the named field.
         */
        public static int getColumnNumberFor(Object lFieldName) {
            return cFields.indexOf(lFieldName);
        } // End method: getColumnNumberFor

        //------------------------------------INSTANCE DATA
        private String mAlignmentType;
        private String mAlignmentText;
        private String mParentFeature, mFeature;
        private String mAccession;
        private String mAltAccession;
        private String mDescription;
        private String mSubjSeq;

        //------------------------------------CONSTRUCTORS
        /** Constructor takes all fields' values. */
        public AlignmentReportLineItem( String lAlignmentType, String lAlignmentText,
                                        OID lParentFeature, OID lFeature,
                                        String lAccession, String lAltAccession,
                                        String lDescription, String lSubjSeq) {
            mAlignmentType = present(lAlignmentType);
            mAlignmentText = present(lAlignmentText);
            mParentFeature = present(lParentFeature);
            mAccession = present(lAccession);
            mAltAccession = present(lAltAccession);
            mDescription = present(lDescription);
            mFeature = present(lFeature);
            mSubjSeq = present(lSubjSeq);
        } // End constructor

        //------------------------------------IMPLEMENTATION OF LineItem INTERFACE
        /** Returns field defs. */
        public List getFields() { return Collections.unmodifiableList(cFields); }

        /** Returns keyed field value. */
        public Object getValue(Object lField) {
            Object lReturnVal = null;

            int lSwitchNum = cFields.indexOf(lField);
            switch (lSwitchNum) {
                case 0:
                    lReturnVal = mParentFeature;
                    break;
                case 1:
                    lReturnVal = mFeature;
                    break;
                case 2:
                    lReturnVal = mSubjSeq;
                    break;
                case 3:
                    lReturnVal = mAccession;
                    break;
                case 4:
                    lReturnVal = mAltAccession;
                    break;
                case 5:
                    lReturnVal = mDescription;
                    break;
                case 6:
                    lReturnVal = mAlignmentType;
                    break;
                case 7:
                    lReturnVal = mAlignmentText;
                    break;
                default:
                    lReturnVal = null;
                    break;
            } // End switch on field number

            return lReturnVal;
        } // End method: getValue

    } // End class: AlignmentReportLineItem

} // End class: AlignmentReport
