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
package client.gui.other.data_source_selectors;

import api.entity_model.access.filter.GenomeVersionCollectionFilter;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.GenomeVersionInfo;
import client.gui.framework.session_mgr.SessionMgr;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

abstract class AbstractGenomeVersionSelectorModel extends AbstractTableModel {

  final static String DEFAULT_NAME_DATABASE_DATA_SOURCE  = "Internal Database";
  final static String DEFAULT_NAME_FILE_DATA_SOURCE      = "Local XML File";
  final static String DEFAULT_NAME_URL_DATA_SOURCE       = "HTTP URL Service";

//  final static String ASSEMBLY_COLUMN_NAMES[]=new String[]{"Species","Assembly","Description","Read Only"};
  final static String DATASOURCE_COLUMN_NAMES[]=new String[]{"Species","Assembly","Description","Read Only","Available","Data Type","Data Source"};
  protected java.util.List genomeVersionsList;
  protected boolean showDataSource;
  protected int sortCol = 1; // Initially sort on Assembly
  protected boolean sortAsc = false;

  public AbstractGenomeVersionSelectorModel() {
  }

  public void setFacadeManagerBase( FacadeManagerBase facade ) {}

  public abstract void init();  // Template method: need to implement this in subclasses.

  public void init(GenomeVersionCollectionFilter filter) {
     genomeVersionsList = ModelMgr.getModelMgr().getAvailableGenomeVersions(filter);
     this.fireTableStructureChanged();
  }

  public Object getValueAt(int row,int col){
    return this.getValueAt((GenomeVersion)genomeVersionsList.get(row),col);
  }

  protected Object getValueAt(GenomeVersion gv, int col) {
    switch(col) {
      case 0: return getDisplayValueSpecies(gv);
      case 1: return getDisplayValueAssembly(gv);
      case 2: return getDisplayValueDescription(gv);
      case 3: return getDisplayValueReadOnly(gv);
      case 4: return getDisplayValueAvailable(gv);
      case 5: return getDisplayValueDataSourceType(gv);
      case 6: return getDisplayValueDataSource(gv);
      default: return "No Column Defined";
    }
  }

  protected Object getDisplayValueSpecies(GenomeVersion gv) {
    return gv.getGenomeVersionInfo().getSpeciesName();
  }

  protected Object getDisplayValueAssembly(GenomeVersion gv) {
    return new Long(gv.getGenomeVersionInfo().getAssemblyVersionAsString());
  }

  protected Object getDisplayValueDescription(GenomeVersion gv) {
    return gv.getDescription();
  }

  protected Object getDisplayValueReadOnly(GenomeVersion gv) {
    return new Boolean(gv.isReadOnly());
  }

  protected Object getDisplayValueAvailable(GenomeVersion gv) {
    return new Boolean(gv.isAvailable());
  }

  protected Object getDisplayValueDataSourceType(GenomeVersion gv) {
    switch (gv.getGenomeVersionInfo().getDataSourceType()) {
      case GenomeVersionInfo.DATABASE_DATA_SOURCE: return DEFAULT_NAME_DATABASE_DATA_SOURCE;
      case GenomeVersionInfo.FILE_DATA_SOURCE: return DEFAULT_NAME_FILE_DATA_SOURCE;
      case GenomeVersionInfo.URL_DATA_SOURCE:
      default:  return DEFAULT_NAME_URL_DATA_SOURCE;
    }
  }

  protected Object getDisplayValueDataSource(GenomeVersion gv) {
    return (showDataSource||(gv.getGenomeVersionInfo().getDataSourceType()!=GenomeVersionInfo.DATABASE_DATA_SOURCE))?
            gv.getDatasourceForVersion():DEFAULT_NAME_DATABASE_DATA_SOURCE;
  }

  public void setValue(int index) {
      ModelMgr.getModelMgr().addSelectedGenomeVersion((GenomeVersion)genomeVersionsList.get(index));
  }

  public boolean canSetValue(int index) {
    return ((GenomeVersion)genomeVersionsList.get(index)).isAvailable();
  }

  public int getRowCount() {
     return genomeVersionsList.size();
  }

  public GenomeVersion getGenomeVersion(int rowNumber) {
    return (GenomeVersion)genomeVersionsList.get(rowNumber);
  }

  public int getColumnWidthForFont(int column, FontMetrics fontMetrics) {
    FontMetrics fm=fontMetrics;
    int max=0;
    int tmp=0;
    for (int i=0;i<getRowCount();i++) {
       tmp= fm.stringWidth(getValueAt(i,column).toString()+" ");
       if (tmp>max) max=tmp;
    }
    tmp= fm.stringWidth(" "+getColumnName(column)+" ");

    if (tmp>max) max=tmp;
    return max;
  }

  public int getColumnCount() {
     return DATASOURCE_COLUMN_NAMES.length;
  }

  public String getColumnName(int column) {
     return DATASOURCE_COLUMN_NAMES[column];
  }

    public MouseListener constructColumnHeaderMouseListener(JTable jTable) {
      return new ColumnListener(jTable);
    }

    /**
     * MouseListener for mouse clicks on column headers to sort the table
     */
    class ColumnListener extends MouseAdapter {
      protected JTable table;

      public ColumnListener(JTable table) {
        this.table = table;
      }

      public void mouseClicked(MouseEvent e) {
        // Figure out which column header was clicked and sort on that column
        TableColumnModel colModel = table.getColumnModel();
        int colModelIndex = colModel.getColumnIndexAtX(e.getX());
        int modelIndex = colModel.getColumn(colModelIndex).getModelIndex();
        if (modelIndex < 0)
          return;
        if (sortCol==modelIndex)
          sortAsc = !sortAsc;
        else
          sortCol = modelIndex;
        // Redraw Header
        for (int i=0; i<DATASOURCE_COLUMN_NAMES.length; i++) {
          TableColumn column = colModel.getColumn(i);
          column.setHeaderValue(getColumnName(column.getModelIndex()));
        }
        table.getTableHeader().repaint();
        GVComparator comp = new GVComparator(sortCol,sortAsc);
        Collections.sort(genomeVersionsList,comp);
        table.tableChanged(new TableModelEvent(AbstractGenomeVersionSelectorModel.this));
        table.repaint();
      }
    }

    /**
     * Comparator to sort columns
     */
    class GVComparator implements Comparator {
      protected int sortCol;
      protected boolean sortAsc;

      public GVComparator(int sortCol, boolean sortAsc) {
        this.sortCol = sortCol;
        this.sortAsc = sortAsc;
      }

      public int compare(Object o1, Object o2) {
        Object compObj1 = getValueAt((GenomeVersion)o1,sortCol);
        Object compObj2 = getValueAt((GenomeVersion)o2,sortCol);
        int retVal = 0;
        if (compObj1 instanceof Comparable && compObj2 instanceof Comparable) {
          Comparable c1 = (Comparable)compObj1;
          Comparable c2 = (Comparable)compObj2;
          retVal = sortAsc ? c1.compareTo(c2) : c2.compareTo(c1);
        }
        else if (compObj1 == null && compObj2 != null)
            retVal = sortAsc ? -1 : 1;
        else if (compObj2 == null && compObj1 != null)
            retVal = sortAsc ? 1 : -1;
        else {
          retVal = sortAsc ? compObj1.toString().compareTo(compObj2.toString()) :
            compObj2.toString().compareTo(compObj1.toString());
        }
        return retVal;
      }
  }

}
