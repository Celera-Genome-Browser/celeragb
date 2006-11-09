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

import client.gui.framework.pref_controller.PrefController;
import client.gui.framework.roles.PrefEditor;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.framework.view_pref_mgr.ColorInfo;
import client.gui.framework.view_pref_mgr.FeatureInfo;
import client.gui.framework.view_pref_mgr.TierInfo;
import client.gui.framework.view_pref_mgr.ViewPrefListenerAdapter;
import client.gui.framework.view_pref_mgr.ViewPrefMgr;
import client.gui.framework.view_pref_mgr.ViewPrefMgrListener;
import client.shared.swing.table.SortButtonRenderer;
import client.shared.text_component.StandardTextField;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class FeaturePanel extends JPanel implements PrefEditor {
   private static final String FEATURE_NAME = new String("Feature Group");
   private static final String COLOR = new String("Color");
   private static final String FEATURE_MAPPING = new String("Shown In Tier");
   private static final String GLYPH = new String("Glyph");
   private static final String HSP_INTRON_DISPLAY_STATE 		= "HSPIntronDisplayState";
   private static final String HSP_INTRON_STATE_ADJACENT 		= "Adjacent";
   private static final String HSP_INTRON_STATE_NON_ADJACENT 	= "NonAdjacent";
   private static final String HSP_INTRON_STATE_OFF 			= "Off";
   private static final String HSP_INTRON_THICKNESS_PERCENTAGE	= "PercentIntronThickness";
   
   private JTable table;
   private boolean settingsChanged = false;
   private JScrollPane scrollpane;
   private JComboBox colorComboBox = new JComboBox();
   private JComboBox mapComboBox = new JComboBox();
   private ViewPrefMgrListener viewPrefListener = new MyViewPrefListener();
   private StandardTextField intronWidthTextField = new StandardTextField();
   
   private JPanel featureButtonPanel = new JPanel();
   private JScrollPane tableAggregate;
   private int sortCol = 0;
   private boolean sortAsc = true;

   private final int ROW_HEIGHT = 15;

   private final String[] names = { FEATURE_NAME, FEATURE_MAPPING, COLOR /*, GLYPH*/
   };
   private Vector data;
   private TreeMap tmpFeatureInfos = (TreeMap) ViewPrefMgr.getViewPrefMgr().getFeatureCollection();
   private TreeMap tmpColorInfos = (TreeMap) ViewPrefMgr.getViewPrefMgr().getColorCollection();
   private TreeMap tmpTierInfos = (TreeMap) ViewPrefMgr.getViewPrefMgr().getTierCollection();
   private TreeMap tmpFeatureMaps = new TreeMap();

   private ButtonGroup hspIntronBG = new ButtonGroup();
   private JRadioButton offButton = new JRadioButton("Off");
   private JRadioButton adjacentHSPButton = new JRadioButton("Adjacent HSP's");
   private JRadioButton nonadjacentHSPButton = new JRadioButton("Non-Adjacent HSP's");
   
   
   public FeaturePanel(JFrame parentFrame) {
      JPanel intronPanel = new JPanel();
      intronPanel.setLayout(new BoxLayout(intronPanel, BoxLayout.X_AXIS));
      intronPanel.setBorder(new javax.swing.border.TitledBorder("HSP Linking Line Thickness:"));
	  JLabel intronLabel = new JLabel("Intron Height (%): ");
      offButton.addChangeListener(new ChangeListener(){
		public void stateChanged(ChangeEvent evt) {
			settingsChanged = true;				
		}
      });      
	  adjacentHSPButton.addChangeListener(new ChangeListener(){
	    public void stateChanged(ChangeEvent evt) {
	 	   settingsChanged = true;				
	    }
	  });      
	  nonadjacentHSPButton.addChangeListener(new ChangeListener(){
	    public void stateChanged(ChangeEvent evt) {
 		  settingsChanged = true;				
 	    }
  	  });      
      hspIntronBG.add(offButton);
      hspIntronBG.add(adjacentHSPButton);
      hspIntronBG.add(nonadjacentHSPButton);
      
	  intronPanel.add(Box.createHorizontalStrut(5));
      intronPanel.add(offButton);
	  intronPanel.add(Box.createHorizontalStrut(5));
      intronPanel.add(adjacentHSPButton);
	  intronPanel.add(Box.createHorizontalStrut(5));
      intronPanel.add(nonadjacentHSPButton);
      
      intronPanel.add(Box.createHorizontalStrut(15));
      intronPanel.add(intronLabel);
      intronPanel.add(Box.createHorizontalStrut(5));
      intronPanel.add(intronWidthTextField);
      intronPanel.add(Box.createHorizontalStrut(5));

      String tmpHSPIntronState = (String) SessionMgr.getSessionMgr().getModelProperty(HSP_INTRON_DISPLAY_STATE);
      if (tmpHSPIntronState==null || tmpHSPIntronState=="" || HSP_INTRON_STATE_OFF.equalsIgnoreCase(tmpHSPIntronState)) {
      	offButton.setSelected(true);
      }
      else if (HSP_INTRON_STATE_ADJACENT.equalsIgnoreCase(tmpHSPIntronState)) {
      	adjacentHSPButton.setSelected(true);
      }
      else if (HSP_INTRON_STATE_NON_ADJACENT.equalsIgnoreCase(tmpHSPIntronState)) {
      	nonadjacentHSPButton.setSelected(true);
      }
	  else {
	  	offButton.setSelected(true);
	  }
      
      String tmpIntronWidth = (String) SessionMgr.getSessionMgr().getModelProperty(HSP_INTRON_THICKNESS_PERCENTAGE);
      if (tmpIntronWidth == null || tmpIntronWidth.equals("")) { tmpIntronWidth = "30"; }
      
      intronWidthTextField.setMaximumSize(new Dimension(30, 20));
      intronWidthTextField.setColumns(10);
      intronWidthTextField.setText(tmpIntronWidth);
      intronWidthTextField.getDocument().addDocumentListener(new DocumentListener() {
         public void insertUpdate(DocumentEvent e) {
            settingsChanged = true;
         }
         public void removeUpdate(DocumentEvent e) {
            settingsChanged = true;
         }
         public void changedUpdate(DocumentEvent e) {
            settingsChanged = true;
         }
      });

      ViewPrefMgr.getViewPrefMgr().registerPrefMgrListener(viewPrefListener);
      featureButtonPanel.setLayout(new BoxLayout(featureButtonPanel, BoxLayout.X_AXIS));
      JButton addFeatureButton = new JButton("Add Feature");
      JButton deleteFeatureButton = new JButton("Delete Feature");
      featureButtonPanel.setBorder(new javax.swing.border.TitledBorder("Options"));
      featureButtonPanel.add(Box.createVerticalStrut(50));
      // If it turns out that people scream for this button, just comment this back in.
      //featureButtonPanel.add(addFeatureButton);
      featureButtonPanel.add(Box.createHorizontalGlue());
      featureButtonPanel.add(Box.createHorizontalStrut(5));
      featureButtonPanel.add(intronPanel);
      featureButtonPanel.add(Box.createHorizontalStrut(15));
      featureButtonPanel.add(deleteFeatureButton);
      featureButtonPanel.add(Box.createHorizontalGlue());
      featureButtonPanel.add(Box.createHorizontalStrut(20));
      addFeatureButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            addFeatureButtonActionPerformed();
         }
      });
      deleteFeatureButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            deleteFeatureButtonActionPerformed();
         }
      });
      updateData();
      this.setSize(505, 100);
      this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      this.add(Box.createVerticalStrut(10));
      this.add(featureButtonPanel);
      this.add(Box.createVerticalStrut(10));

      // Create the table.
      tableAggregate = createTable();
      this.add(tableAggregate);

      table.setAutoResizeMode(2);
      table.setSelectionMode(0);
      table.setRowSelectionAllowed(true);
      table.setColumnSelectionAllowed(false);
      table.getTableHeader().setReorderingAllowed(false);
      table.setRowHeight(ROW_HEIGHT);
      table.setShowVerticalLines(true);
      table.setShowHorizontalLines(true);
      MyComparator comp = new MyComparator(sortCol, sortAsc);
      Collections.sort(data, comp);
      this.setVisible(true);
   }

   public String getDescription() {
      return "Modify the attributes of Features.";
   }

   public String getPanelGroup() {
      return PrefController.GENOMIC_AXIS_ANNOTATION_VIEW_EDITOR;
   }

   private JScrollPane createTable() {
      // Create a model of the data.
      TableModel dataModel = new MyTableModel();
      // Create the table
      table = new JTable(dataModel);
      table.getTableHeader().addMouseListener(new ColumnListener(table));
      TableColumnModel columnModel = table.getColumnModel();
      final SortButtonRenderer headerRenderer = new SortButtonRenderer();
      int i = dataModel.getColumnCount();
      for (int j = 0; j < i; j++) {
         columnModel.getColumn(j).setHeaderRenderer(headerRenderer);
      }

      // Listen to mouse press on the headers
      final JTableHeader header = table.getTableHeader();
      header.addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent e) {
            int col = header.columnAtPoint(e.getPoint());
            header.getTable().convertColumnIndexToModel(col);
            headerRenderer.setPressedColumn(col);
            headerRenderer.setSelectedColumn(col);
            header.repaint();

            if (header.getTable().isEditing()) {
               header.getTable().getCellEditor().stopCellEditing();
            }
         }

         public void mouseReleased(MouseEvent e) {
            // Clear the selection.  Otherwise the header column will be
            // toggled down
            headerRenderer.setPressedColumn(-1);
            header.repaint();
         }
      });

      for (Iterator it = tmpTierInfos.keySet().iterator(); it.hasNext();) {
         mapComboBox.addItem((String) it.next());
      }
      TableColumn mapColumn = table.getColumn(FEATURE_MAPPING);
      // Use the combo box as the editor.
      mapColumn.setCellEditor(new DefaultCellEditor(mapComboBox));
      mapColumn.setCellRenderer(new DefaultTableCellRenderer());
      ((DefaultTableCellRenderer) mapColumn.getCellRenderer()).setHorizontalAlignment(JLabel.CENTER);

      // Show colors by rendering them in their own color.
      DefaultTableCellRenderer colorRenderer = new DefaultTableCellRenderer() {
         public void setValue(Object value) {
            if (value instanceof ColorInfo) {
               ColorInfo c = (ColorInfo) value;
               if (value instanceof ColorInfo && (!table.isEditing() || table.getEditingColumn() != 2)) {
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
            else
               super.setValue(value);
         }
      };

      ListCellRenderer colorListRenderer = new MyListCellRenderer();
      colorComboBox.setRenderer(colorListRenderer);
      for (Iterator it = tmpColorInfos.keySet().iterator(); it.hasNext();) {
         colorComboBox.addItem((ColorInfo) tmpColorInfos.get(it.next()));
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
      data = new Vector();
      tmpFeatureInfos = (TreeMap) ViewPrefMgr.getViewPrefMgr().getFeatureCollection();
      for (Iterator it = tmpFeatureInfos.keySet().iterator(); it.hasNext();) {
         data.add(tmpFeatureInfos.get(it.next()));
      }

      tmpTierInfos = (TreeMap) ViewPrefMgr.getViewPrefMgr().getTierCollection();
      tmpFeatureMaps = (TreeMap) ViewPrefMgr.getViewPrefMgr().getFeatureMapingsForView("Genomic Axis Annotation");

      this.repaint();
   }

   private void addFeatureButtonActionPerformed() {
      ViewPrefMgr.getViewPrefMgr().createNewFeature("New Feature", "Misc", "_MISC");
      updateData();
      ((MyTableModel) table.getModel()).fireTableDataChanged();
      settingsChanged = true;
      for (int x = 0; x < table.getRowCount(); x++) {
         if (((String) table.getModel().getValueAt(x, 0)).equals("New Feature")) {
            table.setRowSelectionInterval(x, x);
            Rectangle rect = table.getCellRect(x, 0, true);
            table.scrollRectToVisible(rect);
            table.repaint();
            break;
         }
      }
   }

   private void deleteFeatureButtonActionPerformed() {
      int row = table.getSelectedRow();
      if (row < 0 || row > table.getRowCount()) {
         JOptionPane.showConfirmDialog(this, "No row selected.", "Feature Deletion", JOptionPane.WARNING_MESSAGE);
         return;
      }
      String nukedFeature = (String) table.getValueAt(row, 0);
      int answer =
         JOptionPane.showConfirmDialog(this, "Deleting: " + nukedFeature + "\nAre you sure?", "Feature Deletion", JOptionPane.YES_NO_OPTION);
      if (answer == 0) {
         ViewPrefMgr.getViewPrefMgr().deleteFeature(nukedFeature);
         updateData();
         ((MyTableModel) table.getModel()).fireTableDataChanged();
         ((MyTableModel) table.getModel()).sortAndScrollToItem("");
      }
      settingsChanged = true;
   }

   /**
    * These three methods are to provide hooks for the Controller in case
    * something panel-specific should happen when these buttons are pressed.
    */
   public void cancelChanges() {
      settingsChanged = false;
      ViewPrefMgr.getViewPrefMgr().commitChanges(false);
   }

   public boolean hasChanged() {
      if (table.isEditing()) {
         table.getCellEditor().stopCellEditing();
      }

      return settingsChanged;
   }

   public String[] applyChanges() {
      String rowName = new String("");

  	  // Set the intron values in the Session.
	  SessionMgr.getSessionMgr().setModelProperty(HSP_INTRON_THICKNESS_PERCENTAGE, intronWidthTextField.getText().trim());
	  String tmpSelectedHSPState = "";
	  if (adjacentHSPButton.isSelected()) {
	    tmpSelectedHSPState = HSP_INTRON_STATE_ADJACENT;
	  }
	  else if (nonadjacentHSPButton.isSelected()) {
	    tmpSelectedHSPState = HSP_INTRON_STATE_NON_ADJACENT;
	  }
	  else {
		tmpSelectedHSPState = HSP_INTRON_STATE_OFF;
	  }
	      
	  SessionMgr.getSessionMgr().setModelProperty(HSP_INTRON_DISPLAY_STATE, tmpSelectedHSPState);


      String tmpIntronWidth = intronWidthTextField.getText().trim();
      double tmpIntronValue = Double.parseDouble(tmpIntronWidth);
      if (tmpIntronValue < 10.0 || tmpIntronValue > 100.0) {
         tmpIntronWidth = "30";
         JOptionPane.showConfirmDialog(
            this,
            "Intron percentage must be <= 100 and >=25\n Resetting to default value of 30%.",
            "Improper Value Entered",
            JOptionPane.WARNING_MESSAGE);
         intronWidthTextField.setText(tmpIntronWidth);
      }

      int targetRow = table.getSelectedRow();
      if (targetRow >= 0 && targetRow < table.getRowCount())
         rowName = ((FeatureInfo) data.get(targetRow)).getName();
      ((MyTableModel) table.getModel()).sortAndScrollToItem(rowName);
	  
	  ViewPrefMgr.getViewPrefMgr().commitChanges(true);
	  ViewPrefMgr.getViewPrefMgr().firePreferencesChangedEvent();
      settingsChanged = false;
      return NO_DELAYED_CHANGES;
   }

   /**
    * This should be used to force the panels to de-register themelves from the
    * PrefController.
    */
   public void dispose() {
      ViewPrefMgr.getViewPrefMgr().removePrefMgrListener(viewPrefListener);
   }

   public String getName() {
      return "Edit Features";
   }

   private class MyTableModel extends AbstractTableModel {
      public int getColumnCount() {
         return names.length;
      }
      public int getRowCount() {
         return data.size();
      }
      public String getColumnName(int column) {
         return names[column];
      }
      public Class getColumnClass(int c) {
         return getValueAt(0, c).getClass();
      }
      public boolean isCellEditable(int row, int col) {
         return true;
      }

      public Object getValueAt(int row, int col) {
         switch (col) {
            // Column Color Name
            case 0 :
               {
                  return ((FeatureInfo) data.get(row)).getName();
               }
            case 1 :
               {
                  String tmpName = ((FeatureInfo) data.get(row)).getName();
                  String tmpTier = (String) tmpFeatureMaps.get(tmpName);
                  if (tmpTier == null)
                     return "Unknown";
                  if (tmpTierInfos.containsKey(tmpTier))
                     return ((TierInfo) tmpTierInfos.get(tmpTier)).getName();
                  else
                     return "Unknown";
               }
               // Column Color
            case 2 :
               {
                  String tmpColorName = (String) ((FeatureInfo) data.get(row)).getFeatureColor();
                  if (tmpColorName != null && tmpColorInfos.containsKey(tmpColorName))
                     return tmpColorInfos.get(tmpColorName);
                  // The color was unknown or undefined.
                  else
                     return tmpColorInfos.get("White");
               }
               /*case 3: {
                 return "Square Glyph";
               }*/
         }
         return null;
      }

      public void setValueAt(Object aValue, int row, int column) {
         if (aValue == null)
            return;
         String rowName = ((FeatureInfo) data.get(row)).getName();
         switch (column) {
            // Column Feature Name
            case 0 :
               {
                  //  If the name is the same do nothing.
                  if (((String) aValue).equals(((FeatureInfo) data.get(row)).getName()))
                     return;
                  ViewPrefMgr.getViewPrefMgr().setFeatureName((FeatureInfo) data.get(row), (String) aValue);
                  updateData();
                  // Since it has a new name, scroll to that.
                  sortAndScrollToItem((String) aValue);
                  settingsChanged = true;
                  break;
               }
            case 1 :
               {
                  settingsChanged = true;
                  String tierName = (String) aValue;
                  String featureName = ((FeatureInfo) data.get(row)).getName();
                  ViewPrefMgr.getViewPrefMgr().setFeatureMapForView("Genomic Axis Annotation", featureName, tierName);
                  updateData();
                  sortAndScrollToItem(rowName);
                  break;
               }
               // Column Color
            case 2 :
               {
                  FeatureInfo tmpInfo = (FeatureInfo) data.get(row);
                  ViewPrefMgr.getViewPrefMgr().setFeatureColor(tmpInfo, ((ColorInfo) aValue).getName());
                  settingsChanged = true;
                  sortAndScrollToItem(rowName);
                  break;
               }
               /*case 3: {

                  * @todo Fill in the glyph selection code here.

               }*/
         }
      }

      public void sortAndScrollToItem(String rowName) {
         MyComparator comp = new MyComparator(sortCol, sortAsc);
         Collections.sort(data, comp);
         ((MyTableModel) table.getModel()).fireTableDataChanged();
         if (!rowName.equals("")) {
            for (int x = 0; x < table.getRowCount(); x++) {
               if (((String) table.getModel().getValueAt(x, 0)).equals(rowName)) {
                  table.setRowSelectionInterval(x, x);
                  Rectangle rect = table.getCellRect(x, 0, true);
                  table.scrollRectToVisible(rect);
                  table.repaint();
                  return;
               }
            }
         }
         table.repaint();
      }
   }

   private class MyViewPrefListener extends ViewPrefListenerAdapter {
      public void preferencesChanged() {
         colorComboBox.removeAllItems();
         tmpColorInfos = (TreeMap) ViewPrefMgr.getViewPrefMgr().getColorCollection();
         for (Iterator it = tmpColorInfos.keySet().iterator(); it.hasNext();) {
            colorComboBox.addItem((ColorInfo) tmpColorInfos.get(it.next()));
         }

         mapComboBox.removeAllItems();
         tmpTierInfos = (TreeMap) ViewPrefMgr.getViewPrefMgr().getTierCollection();
         for (Iterator it = tmpTierInfos.keySet().iterator(); it.hasNext();) {
            mapComboBox.addItem((String) it.next());
         }
         updateData();
         ((MyTableModel) table.getModel()).fireTableDataChanged();
      }
   }

   private class MyListCellRenderer extends JLabel implements ListCellRenderer {
      public MyListCellRenderer() {
         setOpaque(true);
         setHorizontalAlignment(CENTER);
         setVerticalAlignment(CENTER);
      }

      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
         if (value instanceof ColorInfo) {
            ColorInfo c = (ColorInfo) value;
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
         String rowName = new String("");
         int targetRow = table.getSelectedRow();
         if (targetRow >= 0 && targetRow < table.getRowCount())
            rowName = ((FeatureInfo) data.get(targetRow)).getName();

         TableColumnModel colModel = table.getColumnModel();
         int colModelIndex = colModel.getColumnIndexAtX(e.getX());
         int modelIndex = colModel.getColumn(colModelIndex).getModelIndex();
         if (modelIndex < 0)
            return;
         if (sortCol == modelIndex)
            sortAsc = !sortAsc;
         else
            sortCol = modelIndex;
         // Redraw Header
         for (int i = 0; i < names.length; i++) {
            TableColumn column = colModel.getColumn(i);
            column.setHeaderValue(table.getModel().getColumnName(column.getModelIndex()));
         }
         table.getTableHeader().repaint();
         ((MyTableModel) table.getModel()).sortAndScrollToItem(rowName);
      }
   }

   /**
    * Comparator to sort columns
    */
   class MyComparator implements Comparator {
      protected int sortCol;
      protected boolean sortAsc;

      public MyComparator(int sortCol, boolean sortAsc) {
         this.sortCol = sortCol;
         this.sortAsc = sortAsc;
      }

      public int compare(Object o1, Object o2) {
         Object compObj1, compObj2;
         int retVal;
         try {
            compObj1 = getValueAt((FeatureInfo) o1, sortCol);
            compObj2 = getValueAt((FeatureInfo) o2, sortCol);
            if (compObj1 == null || compObj2 == null)
               return 0;
            retVal = 0;
            if (compObj1 instanceof Comparable && compObj2 instanceof Comparable) {
               Comparable c1 = (Comparable) compObj1;
               Comparable c2 = (Comparable) compObj2;
               if (c1 instanceof String && c2 instanceof String) {
                  String s1 = (String) c1;
                  String s2 = (String) c2;
                  retVal = sortAsc ? s1.compareToIgnoreCase(s2) : s2.compareToIgnoreCase(s1);
               }
               else
                  retVal = sortAsc ? c1.compareTo(c2) : c2.compareTo(c1);
            }
            else if (compObj1 == null && compObj2 != null)
               retVal = sortAsc ? -1 : 1;
            else if (compObj2 == null && compObj1 != null)
               retVal = sortAsc ? 1 : -1;
         }
         catch (Exception ex) {
            return 0;
         }
         return retVal;
      }

      protected Object getValueAt(FeatureInfo fi, int col) {
         switch (col) {
            case 0 :
               return fi.getName();
            case 1 :
               return (String) tmpFeatureMaps.get(fi.getName());
            case 2 :
               return fi.getFeatureColor();
            default :
               return "No Column Defined";
         }
      }

   }
}