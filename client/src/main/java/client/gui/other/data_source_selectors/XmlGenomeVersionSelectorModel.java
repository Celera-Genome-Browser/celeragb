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

import api.entity_model.management.ModelMgr;
import api.entity_model.model.genetics.GenomeVersion;

import javax.swing.table.AbstractTableModel;
import java.util.Collection;

class XmlGenomeVersionSelectorModel extends AbstractTableModel {

  final static String DATASOURCE_COLUMN_NAMES[]=new String[]{"Datasource","Species","Assembly","Detail","Purpose","Read Only"};
  private GenomeVersion[] genomeVersions;
  private int mode;

  public XmlGenomeVersionSelectorModel() {
  }

  public void init() {
     Collection genomeVersionsCollection=ModelMgr.getModelMgr().getGenomeVersions();
     genomeVersions = (GenomeVersion[])genomeVersionsCollection.toArray(new GenomeVersion[0]);
     this.fireTableStructureChanged();
  }

  public Object getValueAt(int row,int col){
       switch(col) {
          case 0: return genomeVersions[row].getDatasourceForVersion();
          case 1: return genomeVersions[row].getSpecies();
          case 2: return Long.toString(genomeVersions[row].getVersion());
          case 3: return genomeVersions[row].getDescription();
          case 4: return "XML File";
          // genomeVersions[row].getProductName();
          case 5: return "true";
          // new Boolean(genomeVersions[row].getReadOnly());
          default: return "No Column Defined";
       }

       //return null;
  }

  public void setValue(int index) {
     // facade.setServerConfig(serverConfigs[index]);
      ModelMgr.getModelMgr().addSelectedGenomeVersion(genomeVersions[index]);
  }

  public int getRowCount() {
     return genomeVersions.length;
  }

  public GenomeVersion getGenomeVersion(int rowNumber) {
    return genomeVersions[rowNumber];
  }

  public int getColumnCount() {
     return DATASOURCE_COLUMN_NAMES.length;
  }

 public String getColumnName(int column) {
     return DATASOURCE_COLUMN_NAMES[column];
  }

}