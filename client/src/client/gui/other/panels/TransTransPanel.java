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

public class TransTransPanel extends JPanel implements PrefEditor {
  private static String DEFAULT_START = "Green";
  private static String DEFAULT_STOP = "Red";
  private static String DEFAULT_FRAME1 = "White";
  private static String DEFAULT_FRAME2 = "Magenta";
  private static String DEFAULT_FRAME3 = "Cyan";
  private static String DEFAULT_HIGHLIGHT = "Blue";
  private static String DEFAULT_ODD_EXON_COLOR = "Yellow";
  private static String DEFAULT_EVEN_EXON_COLOR = "Green";

  private static String START_FILL = "Start Fill";
  private static String STOP_FILL = "Stop Fill";
  private static String HIGHLIGHT = "Highlight";
  private static String ODD_EXON_COLOR = "Odd Exon Color";
  private static String EVEN_EXON_COLOR = "Even Exon Color";
  private static String FRAME1_COLOR = "Frame 1 Codon Text";
  private static String FRAME2_COLOR = "Frame 2 Codon Text";
  private static String FRAME3_COLOR = "Frame 3 Codon Text";

  public static String START_FILL_PROP = "TransTransStartColor";
  public static String STOP_FILL_PROP = "TransTransStopColor";
  public static String FRAME1_COLOR_PROP = "TransTransFrame1Color";
  public static String FRAME2_COLOR_PROP = "TransTransFrame2Color";
  public static String FRAME3_COLOR_PROP = "TransTransFrame3Color";
  public static String HIGHLIGHT_PROP = "TransTransConsensusColor";
  public static String ODD_ALTERNATING_EXON_COLOR_PROP = "TransTransOddAlternateColor";
  public static String EVEN_ALTERNATING_EXON_COLOR_PROP = "TransTransEvenAlternateColor";

  private final int ROW_HEIGHT = 15;
  private JComboBox colorComboBox = new JComboBox();
  private JScrollPane scrollpane;

  private String ITEM_NAME = "View Item";
  private String ITEM_COLOR = "Color";
  private boolean settingsChanged=false;
  private final String[] names={ITEM_NAME, ITEM_COLOR};
  private ViewPrefMgrListener viewPrefListener = new MyViewPrefListener();
  private TreeMap tmpColorInfos = (TreeMap)ViewPrefMgr.getViewPrefMgr().getColorCollection();
  private String startColor = new String();
  private String stopColor = new String();
  private String frame1Color = new String();
  private String frame2Color = new String();
  private String frame3Color = new String();
  private String highlightColor = new String();
  private String oddAlternatingExonColor = new String();
  private String evenAlternatingExonColor = new String();
 
  private Vector data  = new Vector();
  private TreeMap valueMap = new TreeMap();
  JPanel mainPanel = new JPanel();
  private JTable table;
  private JScrollPane tableAggregate;
  JButton resetButton = new JButton();

  public TransTransPanel(JFrame parentFrame) {
    ViewPrefMgr.getViewPrefMgr().registerPrefMgrListener(viewPrefListener);
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.add(Box.createVerticalStrut(10));
    startColor = (String)SessionMgr.getSessionMgr().getModelProperty(START_FILL_PROP);
    stopColor = (String)SessionMgr.getSessionMgr().getModelProperty(STOP_FILL_PROP);
    frame1Color = (String)SessionMgr.getSessionMgr().getModelProperty(FRAME1_COLOR_PROP);
    frame2Color = (String)SessionMgr.getSessionMgr().getModelProperty(FRAME2_COLOR_PROP);
    frame3Color = (String)SessionMgr.getSessionMgr().getModelProperty(FRAME3_COLOR_PROP);
    highlightColor = (String)SessionMgr.getSessionMgr().getModelProperty(HIGHLIGHT_PROP);
    oddAlternatingExonColor = (String)SessionMgr.getSessionMgr().getModelProperty(ODD_ALTERNATING_EXON_COLOR_PROP);
    evenAlternatingExonColor = (String)SessionMgr.getSessionMgr().getModelProperty(EVEN_ALTERNATING_EXON_COLOR_PROP);
    
    if (startColor==null) startColor = DEFAULT_START;
    if (stopColor==null) stopColor = DEFAULT_STOP;
    if (frame1Color==null) frame1Color = DEFAULT_FRAME1;
    if (frame2Color==null) frame2Color = DEFAULT_FRAME2;
    if (frame3Color==null) frame3Color = DEFAULT_FRAME3;
    if (highlightColor==null) highlightColor = DEFAULT_HIGHLIGHT;
    if (oddAlternatingExonColor==null) oddAlternatingExonColor = DEFAULT_ODD_EXON_COLOR;
    if (evenAlternatingExonColor==null) evenAlternatingExonColor = DEFAULT_EVEN_EXON_COLOR;

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
          startColor=DEFAULT_START;
          stopColor=DEFAULT_STOP;
          frame1Color=DEFAULT_FRAME1;
          frame2Color=DEFAULT_FRAME2;
          frame3Color=DEFAULT_FRAME3;
          highlightColor=DEFAULT_HIGHLIGHT;
          oddAlternatingExonColor = DEFAULT_ODD_EXON_COLOR;
          evenAlternatingExonColor = DEFAULT_EVEN_EXON_COLOR;
          
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
    data.addElement(HIGHLIGHT);
    data.addElement(FRAME1_COLOR);
    data.addElement(FRAME2_COLOR);
    data.addElement(FRAME3_COLOR);
    data.addElement(START_FILL);
    data.addElement(STOP_FILL);
    data.addElement(ODD_EXON_COLOR);
    data.addElement(EVEN_EXON_COLOR);
    valueMap.put(HIGHLIGHT,highlightColor);
    valueMap.put(FRAME1_COLOR,frame1Color);
    valueMap.put(FRAME2_COLOR,frame2Color);
    valueMap.put(FRAME3_COLOR,frame3Color);
    valueMap.put(START_FILL,startColor);
    valueMap.put(STOP_FILL,stopColor);
    valueMap.put(ODD_EXON_COLOR,oddAlternatingExonColor);
    valueMap.put(EVEN_EXON_COLOR,evenAlternatingExonColor);
  }


  public String getPanelGroup() {
    return PrefController.SUB_VIEW_EDITOR;
  }

  public String getDescription() {
    return "Modify colors of items in the Transcript Translation view.";
  }

  public String getName() { return "Edit Transcript Translation View Settings"; }
  public boolean hasChanged() {
    if (table.isEditing()) {
      table.getCellEditor().stopCellEditing();
    }

    return settingsChanged;
  }

  public String[] applyChanges(){
    if (settingsChanged) {
      SessionMgr.getSessionMgr().setModelProperty(START_FILL_PROP,
        valueMap.get(START_FILL));
      SessionMgr.getSessionMgr().setModelProperty(STOP_FILL_PROP,
        valueMap.get(STOP_FILL));
      SessionMgr.getSessionMgr().setModelProperty(FRAME1_COLOR_PROP,
        valueMap.get(FRAME1_COLOR));
      SessionMgr.getSessionMgr().setModelProperty(FRAME2_COLOR_PROP,
        valueMap.get(FRAME2_COLOR));
      SessionMgr.getSessionMgr().setModelProperty(FRAME3_COLOR_PROP,
        valueMap.get(FRAME3_COLOR));
      SessionMgr.getSessionMgr().setModelProperty(HIGHLIGHT_PROP,
        valueMap.get(HIGHLIGHT));
      SessionMgr.getSessionMgr().setModelProperty(ODD_ALTERNATING_EXON_COLOR_PROP,
        valueMap.get(ODD_EXON_COLOR));
      SessionMgr.getSessionMgr().setModelProperty(EVEN_ALTERNATING_EXON_COLOR_PROP,
        valueMap.get(EVEN_EXON_COLOR));
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
            highlightColor=tmpName;
            break;
          }
          case 1: {
            frame1Color=tmpName;
            break;
          }
          case 2: {
            frame2Color=tmpName;
            break;
            }
          case 3: {
            frame3Color=tmpName;
            break;
          }
          case 4: {
            startColor=tmpName;
            break;
          }
          case 5: {
            stopColor=tmpName;
            break;
          }
          case 6: {
          	oddAlternatingExonColor=tmpName;
          	break;
          }
          case 7: {
          	evenAlternatingExonColor=tmpName;
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