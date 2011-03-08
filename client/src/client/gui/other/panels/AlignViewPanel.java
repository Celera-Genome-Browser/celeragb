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
package client.gui.other.panels;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

import client.gui.framework.pref_controller.PrefController;
import client.gui.framework.roles.PrefEditor;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.framework.view_pref_mgr.ColorInfo;
import client.gui.framework.view_pref_mgr.ViewPrefListenerAdapter;
import client.gui.framework.view_pref_mgr.ViewPrefMgr;
import client.gui.framework.view_pref_mgr.ViewPrefMgrListener;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

public class AlignViewPanel extends JPanel implements PrefEditor {
  private static String DEFAULT_DNA_CONSENSUS_COLOR     = "Blue";
  private static String DEFAULT_PROTEIN_CONSENSUS_COLOR = "Green";
  private static String DEFAULT_RESIDUE_COLOR           = "White";
  private static String DNA_CONSENSUS             = "DNA Consensus";
  private static String PROTEIN_CONSENSUS         = "Protein Consensus";
  private static String RESIDUE                   = "Residue";
  private static String DNA_CONSENSUS_PROP        = "AlignViewDNAConsensusColor";
  private static String PROTEIN_CONSENSUS_PROP    = "AlignViewProteinConsensusColor";
  private static String RESIDUE_PROP              = "AlignViewResidueColor";

  private final int ROW_HEIGHT = 15;
  private JComboBox colorComboBox = new JComboBox();
  private JScrollPane scrollpane;

  private String ITEM_NAME = "View Item";
  private String ITEM_COLOR = "Color";
  private boolean settingsChanged=false;
  private final String[] names={ITEM_NAME, ITEM_COLOR};
  private ViewPrefMgrListener viewPrefListener = new MyViewPrefListener();
  private TreeMap tmpColorInfos = (TreeMap)ViewPrefMgr.getViewPrefMgr().getColorCollection();
  private String dnaConsensusColor = new String();
  private String proteinConsensusColor = new String();
  private String residueColor = new String();
  private Vector data  = new Vector();
  private TreeMap valueMap = new TreeMap();
  JPanel mainPanel = new JPanel();
  private JTable table;
  private JScrollPane tableAggregate;
  JButton resetButton = new JButton();

  public AlignViewPanel(JFrame parentFrame) {
    ViewPrefMgr.getViewPrefMgr().registerPrefMgrListener(viewPrefListener);
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.add(Box.createVerticalStrut(10));
    dnaConsensusColor = (String)SessionMgr.getSessionMgr().getModelProperty(DNA_CONSENSUS_PROP);
    proteinConsensusColor = (String)SessionMgr.getSessionMgr().getModelProperty(PROTEIN_CONSENSUS_PROP);
    residueColor = (String)SessionMgr.getSessionMgr().getModelProperty(RESIDUE_PROP);
    if (dnaConsensusColor==null) dnaConsensusColor = DEFAULT_DNA_CONSENSUS_COLOR;
    if (proteinConsensusColor==null) proteinConsensusColor = DEFAULT_PROTEIN_CONSENSUS_COLOR;
    if (residueColor==null) residueColor = DEFAULT_RESIDUE_COLOR;

    // Create the table.
    tableAggregate = createTable();
    this.add(tableAggregate);

    table.setAutoResizeMode(2);
    table.setSelectionMode(0);
    table.setRowSelectionAllowed(true); ;
    table.getTableHeader().setReorderingAllowed(false);
    table.setRowHeight(ROW_HEIGHT);
    table.setShowVerticalLines(true);
    table.setShowHorizontalLines(true);

    resetButton.setText("Reset Defaults");
    resetButton.setBounds(new Rectangle(16, 286, 128, 24));
    resetButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if (evt.getSource()==resetButton) {
          settingsChanged=true;
          dnaConsensusColor=DEFAULT_DNA_CONSENSUS_COLOR;
          proteinConsensusColor=DEFAULT_PROTEIN_CONSENSUS_COLOR;
          residueColor=DEFAULT_RESIDUE_COLOR;
          updateData();
          ((MyTableModel)table.getModel()).fireTableDataChanged();
        }
      }
    });
    updateData();
    this.add(Box.createVerticalStrut(10));
    this.add(resetButton);
    this.add(Box.createVerticalStrut(10));
    tableAggregate.setPreferredSize(new Dimension(400,200));
    tableAggregate.setSize(400,200);
    this.setPreferredSize(new Dimension(400, 200));
    this.setSize(400,200);
    this.setVisible(true);
  }

  public JScrollPane createTable() {
    // Create a model of the data.
    TableModel dataModel = new MyTableModel();
    // Create the table
    table = new JTable(dataModel);

     // Show colors by rendering them in their own color.
     DefaultTableCellRenderer colorRenderer = new DefaultTableCellRenderer() {
        public void setValue(Object value) {
          if (value instanceof ColorInfo) {
            ColorInfo c = (ColorInfo)value;
            if (value instanceof ColorInfo && !table.isEditing()) {
                setBackground(c.getColor());
                setForeground(c.getTextColor());
                setText(c.getName());
            }
            else {
              setBackground(Color.white);
              setForeground(Color.black);
              setText(c.getName());
           }
          }
          else super.setValue(value);
        }
     };

     ListCellRenderer colorListRenderer = new MyListCellRenderer();
     colorComboBox.setRenderer(colorListRenderer);
     for (Iterator it = tmpColorInfos.keySet().iterator();it.hasNext();) {
      colorComboBox.addItem((ColorInfo)tmpColorInfos.get(it.next()));
     }

     TableColumn colorColumn = table.getColumn(ITEM_COLOR);
     // Use the combo box as the editor.
     DefaultCellEditor editor = new DefaultCellEditor(colorComboBox);
     colorColumn.setCellEditor(editor);
     editor.addCellEditorListener(new CellEditorListener() {
      public void editingStopped(ChangeEvent e) {
        table.repaint();
      }
      public void editingCanceled(ChangeEvent e) {
        table.repaint();
      }
     });

       colorRenderer.setHorizontalAlignment(JLabel.CENTER);
       colorColumn.setCellRenderer(colorRenderer);

       scrollpane = new JScrollPane(table);
       return scrollpane;
     }

  private void updateData() {
    data = new Vector();
    valueMap = new TreeMap();
    data.addElement(DNA_CONSENSUS);
    data.addElement(PROTEIN_CONSENSUS);
    data.addElement(RESIDUE);
    valueMap.put(DNA_CONSENSUS,dnaConsensusColor);
    valueMap.put(PROTEIN_CONSENSUS,proteinConsensusColor);
    valueMap.put(RESIDUE,residueColor);
  }


  public String getPanelGroup() {
    return PrefController.SUB_VIEW_EDITOR;
  }

  public String getDescription() {
    return "Modify colors of items in the Query Alignments View.";
  }

  public String getName() { return "Edit Query Alignments View Settings"; }
  public boolean hasChanged() {
    if (table.isEditing()) {
      table.getCellEditor().stopCellEditing();
    }

    return settingsChanged;
  }

  public String[] applyChanges(){
    if (settingsChanged) {
      SessionMgr.getSessionMgr().setModelProperty(DNA_CONSENSUS_PROP,
        valueMap.get(DNA_CONSENSUS));
      SessionMgr.getSessionMgr().setModelProperty(PROTEIN_CONSENSUS_PROP,
        valueMap.get(PROTEIN_CONSENSUS));
      SessionMgr.getSessionMgr().setModelProperty(RESIDUE_PROP,
        valueMap.get(RESIDUE));
    }
    settingsChanged=false;
    return NO_DELAYED_CHANGES;
  }

  public void cancelChanges() {
    settingsChanged=false;
  }

  public void dispose(){
    ViewPrefMgr.getViewPrefMgr().removePrefMgrListener(viewPrefListener);
  }

  private class MyListCellRenderer extends JLabel implements ListCellRenderer {
    public MyListCellRenderer() {
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
    }

    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus)
    {
      if (value instanceof ColorInfo) {
        ColorInfo c = (ColorInfo)value;
        setBackground(c.getColor());
        setForeground(c.getTextColor());
        setText(c.getName());
      }
      else {
          setBackground(list.getBackground());
          setForeground(list.getForeground());
      }
      return this;
    }
  }


  private class MyTableModel extends AbstractTableModel {
   public int getColumnCount() { return names.length; }
   public int getRowCount() { return data.size();}
   public String getColumnName(int column) {return names[column];}
   public Class getColumnClass(int c) {return getValueAt(0, c).getClass();}
   public boolean isCellEditable(int row, int col) {
    if (col==0) return false;
    return true;
  }

   public Object getValueAt(int row, int col) {
    switch (col) {
      // Column Name
      case 0: { return (String)data.get(row);}
      // Column Color
      case 1: {
        return tmpColorInfos.get((String)valueMap.get((String)data.get(row)));
      }
    }
    return null;
   }

   public void setValueAt(Object aValue, int row, int column) {
    if (aValue==null) return;
    switch (column) {
      // Column Tier Name
      case 0: {
        return;
      }
      // Column Color
      case 1: {
        String tmpName = ((ColorInfo)aValue).getName();
        switch (row) {
          case 0: {
            dnaConsensusColor=tmpName;
            break;
          }
          case 1: {
            proteinConsensusColor=tmpName;
            break;
          }
          case 2: {
            residueColor=tmpName;
            break;
            }
        }
        updateData();
        settingsChanged=true;
        ((MyTableModel)table.getModel()).fireTableDataChanged();
        break;
      }
    }
  }
 }

   private class MyViewPrefListener extends ViewPrefListenerAdapter {
    public void preferencesChanged() {
     colorComboBox.removeAllItems();
     tmpColorInfos = (TreeMap)ViewPrefMgr.getViewPrefMgr().getColorCollection();
     for (Iterator it = tmpColorInfos.keySet().iterator();it.hasNext();) {
      colorComboBox.addItem((ColorInfo)tmpColorInfos.get(it.next()));
     }
     updateData();
     ((MyTableModel)table.getModel()).fireTableDataChanged();
    }
  }
}