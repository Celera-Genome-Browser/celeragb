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
package client.shared.swing.table;

import javax.swing.JTable;
import javax.swing.table.*;
import java.awt.event.*;

/**
 * <code>SortableTable</code> extends the functionality of the standard
 * <code>JTable</code>.  This class performs sorting of the columns with the
 * the column header is selected.  The column header has an arrow indicating the
 * direction of the sorting.
 *
 * @version $Id$
 * @author Douglas Mason
 */
public class SortableTable extends JTable {

    /**
     * Constructs a sortable table with a DefaultTableModel.
     */
    public SortableTable() {
        this(new DefaultTableModel());
    }

    /**
     * Constructs a Sortable <code>JTable</code> that is initialized with
     * <code>model</code> as the data model and a default selection model.
     *
     * @param model        the data model for the table
     */
    public SortableTable(int numRows, int numColumns) {
        this(new DefaultTableModel(numRows, numColumns));
    }

    /**
     * Constructs a Sortable <code>JTable</code> to display the values in the two dimensional array,
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
     */
    public SortableTable(Object[][] rowData, Object[] columnNames) {
        this(new DefaultTableModel(rowData, columnNames));
    }

    /**
     * Constructs a Sortable <code>JTable</code> to display the values in the
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
    public SortableTable(java.util.Vector rowData, java.util.Vector columnNames) {
        this(new DefaultTableModel(rowData, columnNames));
    }

    /**
     * Constructs a Sortable <code>JTable</code> that is initialized with
     * <code>model</code> as the data model and a default selection model.
     *
     * @param model        the data model for the table
     */
    public SortableTable(TableModel model) {
        try {
            init(model);
        } catch (Exception ex) {
            String msg = "Unable to initialize SortableTable: " + ex;
            System.err.println(msg);
        }
    }

    /**
     * Initialize the <code>SortableTable</code>
     */
    protected void init(TableModel model) {
        // Initialize the sortable model
        SortableTableModel sortModel = new SortableTableModel(model);
        this.setModel(sortModel);

        // Set the column headers renderer.  This will "draw" an arrow
        // indicating the direction of the sort.
        TableColumnModel columnModel = this.getColumnModel();
        final SortButtonRenderer headerRenderer = new SortButtonRenderer();
        int i = sortModel.getColumnCount();
        for (int j = 0; j < i; j++) {
            columnModel.getColumn(j).setHeaderRenderer(headerRenderer);
        }

        // Listen to mouse press on the headers
        final JTableHeader header = this.getTableHeader();
        header.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
              int col = header.columnAtPoint(e.getPoint());
              int sortCol = header.getTable().convertColumnIndexToModel(col);
              headerRenderer.setPressedColumn(col);
              headerRenderer.setSelectedColumn(col);
              header.repaint();

              if (header.getTable().isEditing()) {
                header.getTable().getCellEditor().stopCellEditing();
              }

              boolean isAscent;
              if (SortButtonRenderer.DOWN == headerRenderer.getState(col)) {
                isAscent = true;
              } else {
                isAscent = false;
              }
              // peforms the sort on the delagated sort model class
              ((SortableTableModel)header.getTable().getModel()).sortByColumn(sortCol, isAscent);
            }

            public void mouseReleased(MouseEvent e) {
              // Clear the selection.  Otherwise the header column will be
              // toggled down
              headerRenderer.setPressedColumn(-1);
              header.repaint();
            }
        });
    }
}