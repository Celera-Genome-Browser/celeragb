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
package client.gui.application.game_viewer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

public class PropertiesTableModel extends AbstractTableModel {

  private List properties;

  public PropertiesTableModel() {
  }

  public void setDOMObject(Object node) {
    if (!(node instanceof Node)) return;
    Node xmlNode=(Node)node;
    properties=new ArrayList();
    NodeList nodeList=xmlNode.getChildNodes();
    for (int i=0;i<nodeList.getLength();i++) {
       if (nodeList.item(i).getNodeName().equals("property")) properties.add(nodeList.item(i));
    }
    fireTableRowsInserted(0,nodeList.getLength());
    fireTableStructureChanged();
    fireTableDataChanged();
  }

   public String getColumnName(int column) {
	switch (column) {
          case 0:
            return "Prop Name";
          case 1:
            return "Prop Value";
          case 2:
            return "Prop Editable";
        }
        return null;
   }

   public int getRowCount() {
      if (properties==null) return 0;
      return properties.size();
   }

   public int getColumnCount() {
      return 3;
   }

   public Object getValueAt(int row, int col) {
     try {
      switch (col) {
        case 0:
          return ((Node)properties.get(row)).getAttributes().getNamedItem("name").getNodeValue();
        case 1:
          return ((Node)properties.get(row)).getAttributes().getNamedItem("value").getNodeValue();
        case 2:
          return ((Node)properties.get(row)).getAttributes().getNamedItem("editable").getNodeValue();
      }
     }
     catch(Exception ex) {
      return "N/A";
     }
      return null;
   }

}
