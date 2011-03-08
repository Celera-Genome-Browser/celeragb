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

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.Date;

/**
 *  The <code>SortableTableModel</code> implements the <code>TableModel</code>
 *  interface.  This class is uses the delegate pattern to sort the
 *  <code>TableModel</code> actual data.  The data from the <code>TableModel</code>
 *  is never sorted, only the represenation of the data.
 *
 * @version $Id$
 * @author Douglas Mason
 */
public class SortableTableModel implements TableModel, TableModelListener {
    private int[] indexes;
    private TableModel model;

    /**
     * Constructor.  This method takes the real data model that is used by
     * the <code>JTable</code>
     *
     * @param model the real <code>TableModel</code>
     */
    public SortableTableModel(TableModel model) {
	this.model = model;
    }

    /**
     * Returns the number of rows in the model. A
     * <code>JTable</code> uses this method to determine how many rows it
     * should display.  This method calls the real data model.
     *
     * @return the number of rows in the model
     */
    public int getRowCount() {
	return model.getRowCount();
    }

    /**
     * Returns the number of columns in the model. A
     * <code>JTable</code> uses this method to determine how many columns it
     * should create and display by default.
     *
     * @return the number of columns in the model
     */
    public int getColumnCount() {
        return model.getColumnCount();
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     *
     * @param	rowIndex	the row whose value is to be queried
     * @param	columnIndex 	the column whose value is to be queried
     * @return	the value Object at the specified cell
     */
    public Object getValueAt(int row, int col) {
        int rowIndex = row;
        // Indexes to the sorted row
        if (indexes != null) {
            rowIndex = indexes[row];
        }
        return model.getValueAt(rowIndex, col);
    }

    /**
     * Sets the value in the cell at <code>columnIndex</code> and
     * <code>rowIndex</code> to <code>aValue</code>.
     *
     * @param	aValue		 the new value
     * @param	rowIndex	 the row whose value is to be changed
     * @param	columnIndex 	 the column whose value is to be changed
     */
    public void setValueAt(Object value, int row, int col) {
        int rowIndex = row;
        if (indexes != null) {
            rowIndex = indexes[row];
        }
        model.setValueAt(value, rowIndex, col);
    }

    /**
     * sorts the model based on the column and whether it should be ascenting
     * or decenting.
     *
     * @param column the column to sort
     * @param isAscent boolean parameter specifing if it ascenting
     */
    public void sortByColumn(int column, boolean isAscent) {
        sort(column, isAscent);
    }

    /**
     * Returns the name of the column at <code>columnIndex</code>.  This is used
     * to initialize the table's column header name.  Note: this name does
     * not need to be unique; two columns in a table can have the same name.
     *
     * @param	columnIndex	the index of the column
     * @return  the name of the column
     */
    public String getColumnName (int columnIndex) {
	return model.getColumnName(columnIndex);
    }

    /**
     * Returns the most specific superclass for all the cell values
     * in the column.  This is used by the <code>JTable</code> to set up a
     * default renderer and editor for the column.
     *
     * @param columnIndex  the index of the column
     * @return the common ancestor class of the object values in the model.
     */
    public Class getColumnClass (int columnIndex) {
	return model.getColumnClass(columnIndex);
    }

    /**
     * Returns true if the cell at <code>rowIndex</code> and
     * <code>columnIndex</code>
     * is editable.  Otherwise, <code>setValueAt</code> on the cell will not
     * change the value of that cell.
     *
     * @param	rowIndex	the row whose value to be queried
     * @param	columnIndex	the column whose value to be queried
     * @return	true if the cell is editable
     */
    public boolean isCellEditable (int rowIndex, int columnIndex) {
	return model.isCellEditable(rowIndex, columnIndex);
    }

    /**
     * Adds a listener to the list that is notified each time a change
     * to the data model occurs.
     *
     * @param	listener  the TableModelListener
     */
    public void addTableModelListener (TableModelListener listener) {
        model.addTableModelListener(listener);
    }

    /**
     * Removes a listener from the list that is notified each time a
     * change to the data model occurs.
     *
     * @param	listener  the TableModelListener
     */
    public void removeTableModelListener (TableModelListener listener) {
        model.removeTableModelListener(listener);
    }

    /**
     * Implementation of the <code>TableModelListener</code>.  This method
     * calls allocate to (re)allocate the size of the index table
     */
    public void tableChanged(TableModelEvent evt) {
	allocate();
    }

    /**
     * Performs the (re)allocation of the index array (Used to index to the
     * correct location in the real data model).
     */
    private void allocate() {
        indexes = new int[getRowCount()];

        for (int i = 0; i < indexes.length; ++i) {
            indexes[i] = i;
        }
    }

    /**
     * Performs the sorting of the data model.  The sorting is actually proform
     * on an index, rather than the actual real data model.
     */
    protected void sort(int column, boolean isAscent) {
        int n = SortableTableModel.this.getRowCount();
        int[] indexes = SortableTableModel.this.getIndexes();

        for (int i=0; i<n-1; i++) {
          int k = i;
          for (int j=i+1; j<n; j++) {
            if (isAscent) {
              if (compare(column, j, k) < 0) {
                k = j;
              }
            } else {
              if (compare(column, j, k) > 0) {
                k = j;
              }
            }
          }
          int tmp = indexes[i];
          indexes[i] = indexes[k];
          indexes[k] = tmp;
        }
      }

    /**
     * Returns the indexes
     */
    protected int[] getIndexes() {
        int n = getRowCount();
        if (indexes != null) {
            if (indexes.length == n) {
                return indexes;
            }
        }
        indexes = new int[n];
        for (int i=0; i<n; i++) {
            indexes[i] = i;
        }
        return indexes;
    }

    /**
     * Performs the comparations for a specified column and the rows
     */
    private int compare(int column, int row1, int row2) {
          Object o1 = SortableTableModel.this.getValueAt(row1, column);
          Object o2 = SortableTableModel.this.getValueAt(row2, column);
          if (o1 == null && o2 == null) {
            return  0;
          } else if (o1 == null) {
            return -1;
          } else if (o2 == null) {
            return  1;
          } else {
            Class type = SortableTableModel.this.getColumnClass(column);
            if (type.getSuperclass() == Number.class) {
              return compare((Number)o1, (Number)o2);
            } else if (type == String.class) {
              return ((String)o1).compareTo((String)o2);
            } else if (type == Date.class) {
              return compare((Date)o1, (Date)o2);
            } else if (type == Boolean.class) {
              return compare((Boolean)o1, (Boolean)o2);
            } else {
              return ((String)o1).compareTo((String)o2);
            }
          }
    }

    /**
     * Performs the real comparison for Numbers
     */
    private int compare(Number o1, Number o2) {
          double n1 = o1.doubleValue();
          double n2 = o2.doubleValue();
          if (n1 < n2) {
            return -1;
          } else if (n1 > n2) {
            return 1;
          } else {
            return 0;
          }
    }

    /**
     * Performs the real comparison for Dates
     */
    private int compare(Date o1, Date o2) {
          long n1 = o1.getTime();
          long n2 = o2.getTime();
          if (n1 < n2) {
            return -1;
          } else if (n1 > n2) {
            return 1;
          } else {
            return 0;
          }
    }

    /**
     * Performs the real comparison for Booleans
     */
    private int compare(Boolean o1, Boolean o2) {
          boolean b1 = o1.booleanValue();
          boolean b2 = o2.booleanValue();
          if (b1 == b2) {
            return 0;
          } else if (b1) {
            return 1;
          } else {
            return -1;
          }
    }
}
