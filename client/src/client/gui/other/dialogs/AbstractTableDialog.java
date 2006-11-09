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
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/

package client.gui.other.dialogs;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.*;
import javax.swing.table.*;
import client.gui.framework.session_mgr.*;
import client.shared.text_component.*;

public abstract class AbstractTableDialog extends PropertyDialogBase {
  private TableModel tableModel;
  private int row,col;
  private JTextArea textArea;

  public AbstractTableDialog(BrowserModel browserModel, String propertyName) {
      super (browserModel,propertyName);
          try  {
               //get property with browserModel.getLastSelection().getProperty(propertyName);
               jbInit();
          }
          catch(Exception e) {
            SessionMgr.getSessionMgr().handleException(e);
          }
     }


  public AbstractTableDialog() {
    super (null,null);
    try  {
      jbInit();
    }
    catch(Exception e) {
        SessionMgr.getSessionMgr().handleException(e);
    }
  }

  private void jbInit() throws Exception {

    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
  //  getContentPane().setSize(300,30);
    tableModel=makeTableNonEditable(buildTableModel());
    JTable table = new JTable(tableModel);

    table.addComponentListener(new ComponentListener() {
         public void componentResized(ComponentEvent e){
            repaint();
         }
         public void componentMoved(ComponentEvent e){}
         public void componentShown(ComponentEvent e){}
         public void componentHidden(ComponentEvent e){}
    });

    JScrollPane subjectDefinitionSP=new JScrollPane();
    subjectDefinitionSP.getViewport().setLayout(new BoxLayout(subjectDefinitionSP.getViewport(),BoxLayout.X_AXIS));
    subjectDefinitionSP.getViewport().add(table);
    getContentPane().add(subjectDefinitionSP);

    textArea=new StandardTextArea();
    textArea.setPreferredSize(new Dimension(700,30));
    textArea.setSize(700, 30);
    textArea.setLineWrap(true);
    textArea.setEditable(false);
    getContentPane().add(textArea);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
    JButton dismissButton = new JButton("OK");
    dismissButton.addActionListener ( new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            dismissDialog();
          }
    });
    buttonPanel.add(dismissButton);
    getContentPane().add(buttonPanel);

    position();


    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    ListSelectionModel rowSM = table.getSelectionModel();
    rowSM.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                    if (!lsm.isSelectionEmpty()) {
                        row = lsm.getMinSelectionIndex();
                        updateText();
                    }
                }
      });

      table.setCellSelectionEnabled(true);
      table.setColumnSelectionAllowed(true);
      ListSelectionModel colSM = table.getColumnModel().getSelectionModel();
      colSM.addListSelectionListener(new ListSelectionListener() {
              public void valueChanged(ListSelectionEvent e) {
                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                    if (!lsm.isSelectionEmpty()) {
                        col = lsm.getMinSelectionIndex();
                       updateText();
                    }
                }
               });
  }

  private void updateText() {
      Object obj=tableModel.getValueAt(row,col);
      if (obj!=null) textArea.setText(obj.toString());

  }

  private TableModel makeTableNonEditable(TableModel tm){
     return new ReadOnlyTableModel(tm);
  }

  protected abstract TableModel buildTableModel();

  class ReadOnlyTableModel implements TableModel {
      TableModel tm;
      ReadOnlyTableModel(TableModel model) {
         tm=model;
      }

      public int getRowCount(){return tm.getRowCount();}
      public int getColumnCount(){return tm.getColumnCount();}
      public String getColumnName(int columnIndex){return tm.getColumnName(columnIndex);}
      public Class getColumnClass(int columnIndex){return tm.getColumnClass(columnIndex);}
      public boolean isCellEditable(int rowIndex, int columnIndex){return false;}
      public Object getValueAt(int rowIndex, int columnIndex){
         return tm.getValueAt(rowIndex,columnIndex);
      }
      public void setValueAt(Object aValue, int rowIndex, int columnIndex){}
      public void addTableModelListener(TableModelListener l){tm.addTableModelListener(l);}
      public void removeTableModelListener(TableModelListener l){tm.removeTableModelListener(l);}
  }
}