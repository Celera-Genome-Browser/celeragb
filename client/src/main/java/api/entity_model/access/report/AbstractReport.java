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
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */
import api.entity_model.management.ModelMgr;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractReport implements Report {
    /**
     * List of LineItems
     */
    private List lineItems = new ArrayList();

    /**
     * List of Fields
     */
    private List fields = new ArrayList();

    /**
     * Returns the number of line items in the report.
     */
    public int getNumberOfLineItems() {
        return (lineItems.size());
    }

    /**
     * Adds a new line item to the collection of line items in the report.
     */
    public void addLineItem(LineItem lineItem) {
        if (lineItem == null) {
            ModelMgr.getModelMgr()
                    .handleException(new IllegalArgumentException(
                                             "Argument to addLineItem cannot be null!"));

            return;
        }

        lineItems.add(lineItem);

        //Add any new fields to the field list
        List lineItemFields = new ArrayList(lineItem.getFields());
        lineItemFields.removeAll(fields);
        fields.addAll(lineItemFields);
    }

    /**
     * Returns an array of report line items.
     */
    public LineItem[] getLineItems() {
        return (LineItem[]) lineItems.toArray(new LineItem[lineItems.size()]);
    }

    /**
     * Returns a single line item
     */
    public LineItem getLineItem(int lineItemNumber) {
        return (LineItem) lineItems.get(lineItemNumber);
    }

    /**
     * Gets a two dimensional array of data from the report.  This array
     * is suitable to construct a javax.swing.table.DefaultTableModel with, when
     * used conjunction with getFields()
     */
    public Object[][] getReportData() {
        Object[][] report = new Object[lineItems.size()][fields.size()];

        for (int i = 0; i < lineItems.size(); i++) {
            for (int j = 0; j < fields.size(); j++) {
                report[i][j] = getLineItem(i).getValue(fields.get(j));
            }
        }

        return report;
    }

    /**
     * Returns a List of Objects which represent the names of the fields.
     * This will be an aggregate of the LineItem's fields, to be used for column headings.
     * This array is suitable to construct a javax.swing.table.DefaultTableModel with, when
     * used conjunction with getReportData()
     */
    public Object[] getFields() {
        return fields.toArray();
    }

    /**
     * Copies all data from one report to another
     */
    public void addAllLineItems(Report report) {
        int numLineItems = report.getNumberOfLineItems();

        for (int i = 0; i < numLineItems; i++) {
            addLineItem(report.getLineItem(i));
        }
    }

    /**
     * Returns the value for a particular field, in a particular lineItem
     */
    public Object getValue(Object field, int lineItemNumber) {
        LineItem item = getLineItem(lineItemNumber);

        if (item == null) {
            return null;
        }

        return item.getValue(field);
    }

    public String getHTMLTable(String caption) {
        StringBuffer sb = new StringBuffer(1000);
        sb.append(getHtmlTableTag());
        sb.append(getHtmlCaptionTag());
        sb.append(caption);
        sb.append(getHtmlEndCaptionTag());
        sb.append(getHTMLTableHeader());
        sb.append(getHTMLTableData());
        sb.append(getHtmlEndTableTag());

        return sb.toString();
    }

    public String getHTMLTable() {
        StringBuffer sb = new StringBuffer(1000);
        sb.append(getHtmlTableTag());
        sb.append(getHTMLTableHeader());
        sb.append(getHTMLTableData());
        sb.append(getHtmlEndTableTag());

        return sb.toString();
    }

    private String getHTMLTableHeader() {
        StringBuffer sb = new StringBuffer(1000);
        Object[] fieldNames = getFields();
        sb.append(getHtmlRowTag(0));

        for (int i = 0; i < fieldNames.length; i++) {
            sb.append(getHtmlTableHeaderTag(i));
            sb.append(fieldNames[i].toString());
            sb.append(getHtmlEndTableHeaderTag(i));
        }

        sb.append(getHtmlEndRowTag(0));

        return sb.toString();
    }

    private String getHTMLTableData() {
        StringBuffer sb = new StringBuffer(1000);
        Object[][] tableData = getReportData();

        for (int row = 0; row < tableData.length; row++) {
            sb.append(getHtmlRowTag(row));

            Object obj;

            for (int col = 0; col < tableData[0].length; col++) {
                sb.append(getHtmlTableDataTag(row, col));
                obj = tableData[row][col];

                if (obj != null) {
                    sb.append(obj.toString());
                } else {
                    sb.append(getHtmlNoData(row, col));
                }

                sb.append(getHtmlEndTableDataTag(row, col));
            }

            sb.append(getHtmlEndRowTag(row));
        }

        return sb.toString();
    }

    /**
     * Template methods, with default implementations.  To be overridden
     * by specific reports if needed.
     */
    protected String getHtmlTableTag() {
        return HTML_TABLE_TAG;
    }

    protected String getHtmlCaptionTag() {
        return HTML_CAPTION_TAG;
    }

    // Note: probably useless to change an end tag,
    // but including this makes for symmetry, and lets
    // users append things before the end.
    protected String getHtmlEndCaptionTag() {
        return HTML_END_CAPTION_TAG;
    }

    protected String getHtmlEndTableTag() {
        return HTML_END_TABLE_TAG;
    }

    protected String getHtmlTableHeaderTag(int col) {
        return HTML_TABLE_HEADER_TAG;
    }

    protected String getHtmlEndTableHeaderTag(int col) {
        return HTML_END_TABLE_HEADER_TAG;
    }

    protected String getHtmlTableDataTag(int row, int col) {
        return HTML_TABLE_DATA_TAG;
    }

    protected String getHtmlEndTableDataTag(int row, int col) {
        return HTML_END_TABLE_DATA_TAG;
    }

    protected String getHtmlRowTag(int row) {
        return HTML_ROW_TAG;
    }

    protected String getHtmlEndRowTag(int row) {
        return HTML_END_ROW_TAG;
    }

    protected String getHtmlNoData(int row, int col) {
        return HTML_NO_DATA;
    }
}