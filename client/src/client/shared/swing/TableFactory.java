// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package client.shared.swing;

import javax.swing.table.*;
import javax.swing.JTable;
//import shared.gui.table;

/**
 * Factory class for vending standard <code>JTable</code> objects.
 *
 *               All Rights Reserved
 * @version $Id$
 * @author Douglas Mason
 */
public class TableFactory {

    /** Don't let anyone instantiate this class */
    private TableFactory() {
    }

    //// SortableTable ///////////////////////////////////////////////////////////////
    /**
     * Creates a sortable table with a DefaultTableModel.
     *
     * @return the <code>JTable</code> object
     */
    public static JTable createSortableTable() {
        return new client.shared.swing.table.SortableTable();
    }
   /**
     * Constructs a Sortable <code>JTable</code> that is initialized with
     * <code>model</code> as the data model.
     *
     * @param model the data model for the table
     * @return the <code>JTable</code> object
     */
    public static JTable createSortableTable(TableModel model) {
        return new client.shared.swing.table.SortableTable(model);
    }

    /**
     * Creates a Sortable <code>JTable</code> with <code>numRows</code>
     * and <code>numColumns</code> of empty cells using
     * <code>DefaultTableModel</code>.  The columns will have
     * names of the form "A", "B", "C", etc.
     *
     * @param numRows           the number of rows the table holds
     * @param numColumns        the number of columns the table holds
     * @return the <code>JTable</code> object
     * @see javax.swing.table.DefaultTableModel
     */
    public static JTable createSortableTable(int numRows, int numColumns) {
        return new client.shared.swing.table.SortableTable(numRows, numColumns);
    }

    /**
     * Creates a Sortable <code>JTable</code> to display the values in the two dimensional array,
     * <code>rowData</code>, with column names, <code>columnNames</code>.
     * <code>rowData</code> is an array of rows, so the value of the cell at row 1,
     * column 5 can be obtained with the following code:
     * <p>
     * <pre> rowData[1][5]; </pre>
     * <p>
     * All rows must be of the same length as <code>columnNames</code>.
     * <p>
     * @param rowData           the data for the new table
     * @param columnNames       names of each column
     * @return the <code>JTable</code> object
     */
    public static JTable createSortableTable(Object[][] rowData, Object[] columnNames) {
        return new client.shared.swing.table.SortableTable(rowData, columnNames);
    }

    /**
     * Creates a Sortable <code>JTable</code> to display the values in the
     * <code>Vector</code> of <code>Vectors</code>, <code>rowData</code>,
     * with column names, <code>columnNames</code>.  The
     * <code>Vectors</code> contained in <code>rowData</code>
     * should contain the values for that row. In other words,
     * the value of the cell at row 1, column 5 can be obtained
     * with the following code:
     * <p>
     * <pre>((Vector)rowData.elementAt(1)).elementAt(5);</pre>
     * <p>
     * Each row must contain a value for each column or an exception
     * will be raised.
     * <p>
     * @param rowData           the data for the new table
     * @param columnNames       names of each column
     * @return the <code>JTable</code> object
     */
    public static JTable createSortableTable(java.util.Vector rowData, java.util.Vector columnNames) {
        return new client.shared.swing.table.SortableTable(rowData, columnNames);
    }
}