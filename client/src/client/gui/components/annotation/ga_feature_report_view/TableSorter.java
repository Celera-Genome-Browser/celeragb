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

package client.gui.components.annotation.ga_feature_report_view;

import client.gui.framework.session_mgr.SessionMgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;


//public class TableSorter implements TableModel, TableModelListener {
public class TableSorter extends AbstractTableModel implements TableModelListener {
    private List           indexes;
    private TableModel      model;
    private SortComparator  sortComparator = new SortComparator();

    public TableSorter(TableModel originalModel) {
      if (originalModel == null) {
          SessionMgr.getSessionMgr().handleException(new IllegalArgumentException("TableSorter passed null model."));
          return;
      }
      this.model = originalModel;
      this.model.addTableModelListener(this);
      allocateIndices();
    }

    /**
     * This method returns the index (the original index number for the feature
     * property report line item) that corresponds to the selected, sorted row.
     */
    public int getIndexForRow(int row) {
      return ((Integer)indexes.get(row)).intValue();
    }

    public Object getValueAt(int row, int column) {
      if (row > indexes.size()-1) return null;
      return model.getValueAt(((Integer)indexes.get(row)).intValue(), column);
    }

    public void setValueAt(Object theValue, int row, int column) {
      if (row > indexes.size()-1) return;
      model.setValueAt(theValue, ((Integer)indexes.get(row)).intValue(), column);
    }

    public void tableChanged(TableModelEvent e) {
      allocateIndices();
      this.fireTableChanged(e);
    }

    /** Returns sorted row number for wrapped data model position. */
    public int getObjectRow(Object o) {
      int unsortedRow = ((FeaturePropertyDataModel)model).getObjectRow(o);
      if (unsortedRow == -1)
        return unsortedRow;
      else
        return getIndexForRow(unsortedRow);
    } // End method

     public void sort(int col, boolean ascending) {
        sortComparator.setSortColumn(col);
        sortComparator.setAscending(ascending);
        Collections.sort(indexes, sortComparator);
//        for (Iterator it = indexes.iterator(); it.hasNext();) {
//          System.out.println("Index "+it.next());
//        }
     }


    public void allocateIndices() {
      int rc = getRowCount();
      indexes = new ArrayList(rc);
      for (int i=0; i < rc;  i++) {
        indexes.add(i, new Integer(i));
      }
    }

    //pass through methods
    public int getRowCount() { return model.getRowCount(); }
    public int getColumnCount() { return model.getColumnCount(); }
    public String getColumnName(int col) { return model.getColumnName(col); }
    public Class getColumnClass(int col) { return model.getColumnClass(col); }
    public boolean isCellEditable(int row, int col) { return model.isCellEditable(row, col); }
//    public void addTableModelListener(TableModelListener l) { model.addTableModelListener(l); }
//    public void removeTableModelListener(TableModelListener l) { model.removeTableModelListener(l); }

    /**
     * Comparator for sorting
     */
     private class SortComparator implements Comparator {

       private int sortColumn = 0;
       private boolean ascending = false;

       public void setSortColumn(int col) { this.sortColumn = col; }
       public void setAscending(boolean val) { this.ascending = val; }

       /**
        * Compare the values from rows i and j for the specified column.
        */
        public int compare(Object o1, Object o2) {

          int i = ((Integer) o1).intValue();
          int j = ((Integer) o2).intValue();

          Object obj_i = model.getValueAt(i, sortColumn);
          Object obj_j = model.getValueAt(j, sortColumn);

          if ((obj_i == null) && (obj_j == null)) return 0;
          if (obj_i == null) return -1;
          if (obj_j == null) return 1;

          //compare based on type
          if ((obj_i.getClass().getSuperclass() == java.lang.Number.class) &&
              (obj_j.getClass().getSuperclass() == java.lang.Number.class)) {

             Number n_i = (Number) obj_i;
             Number n_j = (Number) obj_j;

             double d_i = n_i.doubleValue();
             double d_j = n_j.doubleValue();

             if (d_i == d_j) return 0;

             if (ascending)
               return (d_i < d_j)? 1 : -1;

             //decending
             return (d_i > d_j)? 1 : -1;
          }

          //compare based on strings
          int c = obj_j.toString().compareTo(obj_i.toString());
          if (ascending) {
            return (c < 0) ? -1 :((c > 0) ? 1 : 0);
          }

          //descending
          return (c < 0) ? 1 : ((c > 0) ? -1 : 0);
        }
     }
}
