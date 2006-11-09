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


public interface Report extends java.io.Serializable {

   static String HTML_TABLE_TAG="<TABLE border=\"2\" cellpadding=\"3\" cellspacing=\"3\" Align=\"center\">";
   static String HTML_END_TABLE_TAG="</TABLE>";
   static String HTML_ROW_TAG="<TR>";
   static String HTML_END_ROW_TAG="</TR>";
   static String HTML_TABLE_DATA_TAG="<TD align=\"center\">";
   static String HTML_END_TABLE_DATA_TAG="</TD>";
   static String HTML_TABLE_HEADER_TAG="<TH>";
   static String HTML_END_TABLE_HEADER_TAG="</TH>";
   static String HTML_CAPTION_TAG="<CAPTION>";
   static String HTML_END_CAPTION_TAG="</CAPTION>";
   static String HTML_NO_DATA="&nbsp";
   /**
    * Will add all line items from one report to this report
    */
   void addAllLineItems(Report report);

   /**
    * Will add a single LineItem to this report
    */
   void addLineItem(LineItem lineItem);

   /**
    * Returns the number of LineItems
    */
   int getNumberOfLineItems();

   /**
    * Gets an of all lineItems
    */
   LineItem [] getLineItems();

   /**
    * Gets a single line item
    */
   LineItem getLineItem(int number);

   /**
    * Gets a two dimensional array of data from the report.  This array
    * is suitable to construct a javax.swing.table.DefaultTableModel with, when
    * used conjunction with getFields()
    */
    Object[][] getReportData();

   /**
    * Returns a List of Objects which represent the names of the fields.
    * This will be an aggregate of the LineItem's fields, to be used for column headings
    */
   Object[] getFields();

   /**
    * Returns the value for a particular field, in a particular lineItem
    */
   Object getValue(Object field, int lineItemNumber);

   /**
    * Returns a HTML style table of the data in this report
    */
   String getHTMLTable();

   /**
    * Returns a HTML style table of the data in this report
    */
   String getHTMLTable(String caption);

}