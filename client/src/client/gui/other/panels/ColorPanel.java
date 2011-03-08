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
package client.gui.other.panels;

import client.gui.framework.pref_controller.PrefController;
import client.gui.framework.roles.PrefEditor;
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

  public class ColorPanel extends JPanel implements PrefEditor {
    private static final String COLOR_NAME=new String("Name");
    private static final String COLOR=new String("Color");
    private ViewPrefMgrListener viewPrefListener = new MyViewPrefListener();
    private JTable      table;
    private boolean settingsChanged = false;
    private JScrollPane scrollpane;
    private JComboBox colorComboBox = new JComboBox();

    private JPanel      colorButtonPanel= new JPanel();
    private JScrollPane tableAggregate;

    private final int ROW_HEIGHT = 15;

    private final String[] names={COLOR_NAME, COLOR};
    private Vector data;
    private TreeMap tmpColorInfos = (TreeMap)ViewPrefMgr.getViewPrefMgr().getColorCollection();


  public ColorPanel(JFrame parentFrame) {
    ViewPrefMgr.getViewPrefMgr().registerPrefMgrListener(viewPrefListener);
    colorButtonPanel.setLayout(new BoxLayout(colorButtonPanel,BoxLayout.X_AXIS));
    JButton addColorButton = new JButton("Add Color");
    JButton deleteColorButton = new JButton("Delete Color");
    colorButtonPanel.setBorder(new javax.swing.border.TitledBorder("Options"));
    colorButtonPanel.add(Box.createVerticalStrut(50));
    colorButtonPanel.add(addColorButton);
    colorButtonPanel.add(Box.createHorizontalStrut(20));
    colorButtonPanel.add(deleteColorButton);
    colorButtonPanel.add(Box.createHorizontalGlue());
    colorButtonPanel.add(Box.createHorizontalStrut(20));
    addColorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addColorButtonActionPerformed();
      }
    });
    deleteColorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteColorButtonActionPerformed();
      }
    });
    updateData();
    this.setSize(505, 100);
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.add(Box.createVerticalStrut(10));
    this.add(colorButtonPanel);
    this.add(Box.createVerticalStrut(10));

    // Create the table.
    tableAggregate = createTable();
    this.add(tableAggregate);

    table.setAutoResizeMode(2);
    table.setSelectionMode(0);
    table.setRowSelectionAllowed(true); ;
    table.setColumnSelectionAllowed(false);
    table.getTableHeader().setReorderingAllowed(false);
    table.setRowHeight(ROW_HEIGHT);
    table.setShowVerticalLines(true);
    table.setShowHorizontalLines(true);
    this.setVisible(true);
   }


  public String getDescription() {
    return "Set the colors available to browser items.";
  }

  public String getPanelGroup() {
    return PrefController.GENOMIC_AXIS_ANNOTATION_VIEW_EDITOR;
  }

  private JScrollPane createTable() {
     // Create a model of the data.
     TableModel dataModel = new MyTableModel();

     // Create the table
     table = new JTable(dataModel);
     // Create the true/false renderer.
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
     TableColumn colorColumn = table.getColumn(COLOR);
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
    data=new Vector();
    for (Iterator it=tmpColorInfos.keySet().iterator();it.hasNext();) {
      data.add(tmpColorInfos.get(it.next()));
    }
    this.repaint();
  }


  private void addColorButtonActionPerformed() {
    Color newColor = JColorChooser.showDialog(this,"Select choose a new color.",Color.white);
    if (newColor==null) return;
    settingsChanged=true;
    ViewPrefMgr.getViewPrefMgr().createNewColor("New Color", newColor);
    updateData();
    ((MyTableModel)table.getModel()).fireTableDataChanged();
    for (int x=0; x<table.getRowCount();x++) {
      if (((String)table.getModel().getValueAt(x,0)).equals("New Color")) {
        table.setRowSelectionInterval(x,x);
        Rectangle rect = table.getCellRect(x, 0, true);
        table.scrollRectToVisible(rect);
        table.repaint();
        return;
      }
    }
  }


  private void deleteColorButtonActionPerformed() {
    int row = table.getSelectedRow();
    if (row<0 || row>table.getRowCount()) {
      JOptionPane.showConfirmDialog(this,"No row selected!","Color Deletion",JOptionPane.WARNING_MESSAGE);
      return;
    }
    String nukedColor = (String)table.getValueAt(row,0);
    int answer=JOptionPane.showConfirmDialog(this,"Deleting: "+nukedColor+"\nAre you sure?"
      ,"Color Deletion",JOptionPane.YES_NO_OPTION);
    if (answer==0) {
      ViewPrefMgr.getViewPrefMgr().deleteColor(nukedColor);
      settingsChanged=true;
      updateData();
      ((MyTableModel)table.getModel()).fireTableDataChanged();
    }
  }

  /**
   * These three methods are to provide hooks for the Controller in case
   * something panel-specific should happen when these buttons are pressed.
   */
  public void cancelChanges(){
    settingsChanged=false;
    ViewPrefMgr.getViewPrefMgr().commitChanges(false);
  }

  public boolean hasChanged() {
    if (table.isEditing()) {
      table.getCellEditor().stopCellEditing();
    }

    return settingsChanged;
  }

  public String[] applyChanges(){
    ViewPrefMgr.getViewPrefMgr().commitChanges(true);
    ViewPrefMgr.getViewPrefMgr().firePreferencesChangedEvent();
    settingsChanged=false;
    return NO_DELAYED_CHANGES;
  }

  /**
   * This should be used to force the panels to de-register themelves from the
   * PrefController.
   */
  public void dispose(){
    ViewPrefMgr.getViewPrefMgr().removePrefMgrListener(viewPrefListener);
  }

  public String getName() { return "Edit Colors"; }

  private class MyTableModel extends AbstractTableModel {
   public int getColumnCount() { return names.length; }
   public int getRowCount() { return data.size();}
   public String getColumnName(int column) {return names[column];}
   public Class getColumnClass(int c) {return getValueAt(0, c).getClass();}
   public boolean isCellEditable(int row, int col) {return true;}

   public Object getValueAt(int row, int col) {
    switch (col) {
      // Column Color Name
      case 0: { return ((ColorInfo)data.get(row)).getName();}
      // Column Color
      case 1: {
        return tmpColorInfos.get(((ColorInfo)data.get(row)).getName());
      }
    }
    return null;
   }

   public void setValueAt(Object aValue, int row, int column) {
    if (aValue==null) return;
    switch (column) {
      // Column Color Name
      case 0: {
        ViewPrefMgr.getViewPrefMgr().setColorName((ColorInfo)data.get(row), (String)aValue);
        settingsChanged=true;
        updateData();
        for (int x=0; x<table.getRowCount();x++) {
          if (((String)table.getModel().getValueAt(x,0)).equals(aValue)) {
            table.setRowSelectionInterval(x,x);
            Rectangle rect = table.getCellRect(x, 0, true);
            table.scrollRectToVisible(rect);
            table.repaint();
            return;
          }
        }
        break;
      }
      // Column Color
      case 1: {
        settingsChanged=true;
        ColorInfo tmpInfo = (ColorInfo)data.get(row);
        ViewPrefMgr.getViewPrefMgr().setColorForColorInfo(tmpInfo, ((ColorInfo)aValue).getColor());
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

}