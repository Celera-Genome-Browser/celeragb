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
package client.gui.framework.inspector;

import client.gui.framework.session_mgr.SessionMgr;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;

public class ComboEditor extends DefaultCellEditor{
  static JComboBox comboBox=new JComboBox();
  boolean fontSet;

  public ComboEditor () {
     super(comboBox);
  	 comboBox.setFocusTraversalPolicy(null);
  	 comboBox.setTransferHandler(null);
  	 comboBox.setRequestFocusEnabled(false);
	 comboBox.setPopupVisible(false);
	 comboBox.setFocusable(false);
  }

  public Component getTableCellEditorComponent(JTable table, Object value,
                          boolean isSelected, int row, int column) {
	 
	 if (!fontSet) {
        fontSet=true;
        Rectangle rec=table.getCellRect(row,column,false);
        while (true) {
          Font currentFont=comboBox.getFont();
          FontMetrics fm=comboBox.getFontMetrics(currentFont);
          if (fm.getHeight()-1>rec.getHeight())
            comboBox.setFont(new Font(currentFont.getName(),currentFont.getStyle(),currentFont.getSize()-1));
          else
            break;
        }


     }
     comboBox.removeAllItems();
     SelectableProperty sp;
     if (value instanceof SelectableProperty) sp=(SelectableProperty)value;
     else return super.getTableCellEditorComponent(table, value,
                    isSelected, row, column);
     for (Iterator iter=sp.getSelectables().iterator();iter.hasNext();) {
        comboBox.addItem(iter.next());
     }
     return comboBox;
  }

    // implements javax.swing.CellEditor
    public void cancelCellEditing() {
       int response=JOptionPane.showConfirmDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
        "You have not finished editing the property value. "+
         "Would you like to save your entry?", "Please Confirm!!", JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
       if (response == JOptionPane.YES_OPTION) {
          stopCellEditing();
       }
       else super.cancelCellEditing();
    }

}