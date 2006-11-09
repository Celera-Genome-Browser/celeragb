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
import client.gui.framework.view_pref_mgr.TierInfo;
import client.gui.framework.view_pref_mgr.ViewPrefListenerAdapter;
import client.gui.framework.view_pref_mgr.ViewPrefMgr;
import client.gui.framework.view_pref_mgr.ViewPrefMgrListener;
import client.shared.swing.table.SortButtonRenderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;


  public class TierPanel extends JPanel implements PrefEditor {
    private static final String TIER_NAME=new String("Name");
    private static final String TIER_ORDER_NUMBER=new String("Position");
    private static final String TIER_STATE=new String("Visible State");
    private static final String HIDE_WHEN_EMPTY=new String("Hide Empty");
    private static final String BACKGROUND_COLOR=new String("Background");
    private static final String DOCKED_STATE=new String("Is Docked");
    private static final int RESERVED_TIERS=5;
    private ViewPrefMgrListener viewPrefListener = new MyViewPrefListener();

    private JTable      table;
    private boolean settingsChanged = false;
    private JScrollPane scrollpane;
    private JFrame parentFrame;
    private JPanel      tierButtonPanel= new JPanel();
    private JScrollPane tableAggregate;
    private JComboBox colorComboBox = new JComboBox();
    private int sortCol = 0;
    private boolean sortAsc = true;
    private final int ROW_HEIGHT = 15;
    private final String[] names={TIER_NAME, TIER_ORDER_NUMBER, TIER_STATE,
      HIDE_WHEN_EMPTY, DOCKED_STATE, BACKGROUND_COLOR};
    private Vector data;
    private TreeMap tmpColorInfos = (TreeMap)ViewPrefMgr.getViewPrefMgr().getColorCollection();
    private ArrayList orderedList = ViewPrefMgr.getViewPrefMgr().getViewInfo("Genomic Axis Annotation").getTierOrderByName();
    private TierInfo deleteTierInfo = null ;

  public TierPanel(JFrame parentFrame) {
    ViewPrefMgr.getViewPrefMgr().registerPrefMgrListener(viewPrefListener);
    this.parentFrame=parentFrame;
    tierButtonPanel.setLayout(new BoxLayout(tierButtonPanel,BoxLayout.X_AXIS));
    JButton addTierButton = new JButton("Add Tier");
    JButton deleteTierButton = new JButton("Delete Tier");
    JButton stateResetButton = new JButton("Make All Visible");
    tierButtonPanel.setBorder(new javax.swing.border.TitledBorder("Options"));
    tierButtonPanel.add(Box.createVerticalStrut(50));
    tierButtonPanel.add(addTierButton);
    tierButtonPanel.add(Box.createHorizontalStrut(20));
    tierButtonPanel.add(deleteTierButton);
    tierButtonPanel.add(Box.createHorizontalStrut(20));
    tierButtonPanel.add(stateResetButton);
    tierButtonPanel.add(Box.createHorizontalGlue());
    tierButtonPanel.add(Box.createHorizontalStrut(20));
    addTierButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addTierButtonActionPerformed();
      }
    });
    deleteTierButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteTierButtonActionPerformed();
      }
    });
    stateResetButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stateResetButtonActionPerformed();
      }
    });

    updateData();
    this.setSize(505, 100);
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.add(Box.createVerticalStrut(10));
    this.add(tierButtonPanel);
    this.add(Box.createVerticalStrut(10));

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
    MyComparator comp = new MyComparator(sortCol,sortAsc);
    Collections.sort(data,comp);
    ((MyTableModel)table.getModel()).fireTableDataChanged();
    table.repaint();
    this.setVisible(true);
   }


  public String getDescription() {
    return "Modify attributes of Tiers.";
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

     TableColumn nameColumn = table.getColumn(TIER_ORDER_NUMBER);
     // Use the combo box as the editor.
     DefaultTableCellRenderer nameRenderer = new DefaultTableCellRenderer();
     nameRenderer.setHorizontalAlignment(JLabel.CENTER);
     nameColumn.setCellRenderer(nameRenderer);

     // Create the true/false renderer.
     DefaultTableCellRenderer booleanRenderer = new DefaultTableCellRenderer() {
      public void setValue(Object value) {
        if (value instanceof Boolean) {
          Boolean b = (Boolean)value;
          setText(b.toString());
        }
        else { super.setValue(value); }
      }
     };
     JComboBox booleanComboBox = new JComboBox();
     booleanComboBox.addItem(Boolean.TRUE);
     booleanComboBox.addItem(Boolean.FALSE);
     TableColumn hideWhenEmptyColumn = table.getColumn(HIDE_WHEN_EMPTY);
     // Use the combo box as the editor.
     hideWhenEmptyColumn.setCellEditor(new DefaultCellEditor(booleanComboBox));

     booleanRenderer.setHorizontalAlignment(JLabel.CENTER);
     hideWhenEmptyColumn.setCellRenderer(booleanRenderer);

     TableColumn isDockedColumn = table.getColumn(DOCKED_STATE);
     // Use the combo box as the editor.
     isDockedColumn.setCellEditor(new DefaultCellEditor(booleanComboBox));

     booleanRenderer.setHorizontalAlignment(JLabel.CENTER);
     isDockedColumn.setCellRenderer(booleanRenderer);

     JComboBox stateComboBox = new JComboBox();
     stateComboBox.addItem("Expanded");
     stateComboBox.addItem("Collapsed");
     stateComboBox.addItem("Fixed");
     stateComboBox.addItem("Hidden");
     TableColumn initialStateColumn = table.getColumn(TIER_STATE);
     // Use the combo box as the editor.
     initialStateColumn.setCellEditor(new DefaultCellEditor(stateComboBox));
     initialStateColumn.setCellRenderer(new DefaultTableCellRenderer());
     ((DefaultTableCellRenderer)initialStateColumn.getCellRenderer()).setHorizontalAlignment(JLabel.CENTER);

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

     TableColumn colorColumn = table.getColumn(BACKGROUND_COLOR);
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
    TreeMap tmpMap = (TreeMap)ViewPrefMgr.getViewPrefMgr().getTierCollection();
    orderedList = ViewPrefMgr.getViewPrefMgr().getViewInfo("Genomic Axis Annotation").getTierOrderByName();
    for (Iterator it=tmpMap.keySet().iterator();it.hasNext();) {
        data.add(tmpMap.get(it.next()));
    }
    this.repaint();
  }


  private void addTierButtonActionPerformed() {
    ViewPrefMgr.getViewPrefMgr().createNewTier("New Tier");
    updateData();
    ((MyTableModel)table.getModel()).fireTableDataChanged();
    ((MyTableModel)table.getModel()).sortAndScrollToItem("New Tier");
    settingsChanged=true;
  }


  private void deleteTierButtonActionPerformed() {
    int row = table.getSelectedRow();
    if (row<0 || row>table.getRowCount()) {
      JOptionPane.showConfirmDialog(this,"No row selected!","Tier Deletion",JOptionPane.WARNING_MESSAGE);
      return;
    }
    String nukedTier = (String)table.getValueAt(row,0);
    // donot allow deletes of tiers that are default like Axis, Workspace etc.
     TierInfo tmpObject = (TierInfo)ViewPrefMgr.getViewPrefMgr().getTierCollection().get(nukedTier);

     String targetName = tmpObject.getKeyName();

    if(ViewPrefMgr.getViewPrefMgr().getDefaultTierMap().containsKey(targetName)){

      ViewPrefMgr.getViewPrefMgr().handleDefaultKeyOverrideRequest();
      return;
    }

    int answer=JOptionPane.showConfirmDialog(this,"Deleting: "+nukedTier+"\nAre you sure?"
      ,"Tier Deletion",JOptionPane.YES_NO_OPTION);
    if (answer==0) {
      ViewPrefMgr.getViewPrefMgr().deleteTier(nukedTier);
      updateData();

      ((MyTableModel)table.getModel()).fireTableDataChanged();
      ((MyTableModel)table.getModel()).sortAndScrollToItem("");
      deleteTierInfo = tmpObject;
    }
    settingsChanged=true;
  }

  private void stateResetButtonActionPerformed() {
    TreeMap tmpInfos = ViewPrefMgr.getViewPrefMgr().getTierCollection();
    for (Iterator it = tmpInfos.keySet().iterator(); it.hasNext();) {
      TierInfo tmpInfo = (TierInfo)tmpInfos.get(it.next());
      if (tmpInfo.getState()==TierInfo.TIER_FIXED_SIZE || tmpInfo.getState()==TierInfo.TIER_HIDDEN)
        ViewPrefMgr.getViewPrefMgr().setTierState(tmpInfo.getName(), TierInfo.TIER_COLLAPSED);
    }
    ((MyTableModel)table.getModel()).sortAndScrollToItem("");
    settingsChanged=true;
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
    if(deleteTierInfo !=null) ViewPrefMgr.getViewPrefMgr().fireTierRemovedEvent(deleteTierInfo);

    String rowName = new String("");
    int targetRow = table.getSelectedRow();
    if (targetRow >= 0 && targetRow < table.getRowCount())
      rowName = ((TierInfo)data.get(targetRow)).getName();
    ((MyTableModel)table.getModel()).sortAndScrollToItem(rowName);
    settingsChanged=false;
    return NO_DELAYED_CHANGES;
  }

   private String getStateString(int value) {
    if (value==TierInfo.TIER_EXPANDED) return new String ("Expanded");
    else if (value==TierInfo.TIER_COLLAPSED) return new String ("Collapsed");
    else if (value==TierInfo.TIER_FIXED_SIZE) return new String ("Fixed");
    else if (value==TierInfo.TIER_HIDDEN) return new String ("Hidden");
    else return "Unknown";
   }

  /**
   * This should be used to force the panels to de-register themelves from the
   * PrefController.
   */
  public void dispose(){
    ViewPrefMgr.getViewPrefMgr().removePrefMgrListener(viewPrefListener);
  }

  public String getName() { return "Edit Tiers"; }

  public String getPanelGroup() {
    return PrefController.GENOMIC_AXIS_ANNOTATION_VIEW_EDITOR;
  }

  private class MyTableModel extends AbstractTableModel {
   public int getColumnCount() { return names.length; }
   public int getRowCount() { return data.size();}
   public String getColumnName(int column) {
     return names[column];
   }
   public Class getColumnClass(int c) {return getValueAt(0, c).getClass();}
   public boolean isCellEditable(int row, int col) {return true;}

   public Object getValueAt(int row, int col) {
    switch (col) {
      // Column Tier Name
      case 0: { return ((TierInfo)data.get(row)).getName();}
      case 1: {
        return new Integer(ViewPrefMgr.getViewPrefMgr().getTierOrderValue("Genomic Axis Annotation",
          ((TierInfo)data.get(row)).getName()));
      }
      // Column Initial State
      case 2: {
        int state = ((TierInfo)data.get(row)).getState();
        return getStateString(state);
      }
      //Column Hide When Empty
      case 3: {
        boolean hide=((TierInfo)data.get(row)).getHideWhenEmpty();
        if (hide) return new Boolean(true);
        else return new Boolean(false);
      }
      case 4: {
        boolean docked=((TierInfo)data.get(row)).getDocked();
        if (docked) return new Boolean(true);
        else return new Boolean(false);
      }
      // Column Color
      case 5: {
        return tmpColorInfos.get(((TierInfo)data.get(row)).getBackgroundColor());
      }
    }
    return null;
   }

   public void setValueAt(Object aValue, int row, int column) {
    if (aValue==null) return;
    String rowName = ((TierInfo)data.get(row)).getName();
    switch (column) {
      // Column Tier Name
      case 0: {
        if (((TierInfo)data.get(row)).getName().equals((String)aValue)) return;
        ViewPrefMgr.getViewPrefMgr().setTierName((TierInfo)data.get(row), (String)aValue);
        updateData();
        // Since it has a new name, scroll to that.
        sortAndScrollToItem((String)aValue);
        settingsChanged=true;
        break;
      }
      case 1: {
        try {
          settingsChanged=true;
          TierInfo oldTier = (TierInfo)data.get(row);
          Integer oldOrderValue = new Integer(ViewPrefMgr.getViewPrefMgr().
            getTierOrderValue("Genomic Axis Annotation", oldTier.getName()));
          Integer newOrderValue = (Integer)aValue;
          ViewPrefMgr.getViewPrefMgr().swapTierOrder("Genomic Axis Annotation",
            oldOrderValue.toString(),
            newOrderValue.toString());
          sortAndScrollToItem(rowName);
        }
        catch (Exception ex) {
          JOptionPane.showMessageDialog(parentFrame, "Invalid data.","Error!",JOptionPane.WARNING_MESSAGE);
        }
        break;
      }
      // Column Initial State
      case 2: {
        String state = (String)aValue;
        String tmpName = ((TierInfo)data.get(row)).getName();
        // These values are determined by the MapTierGlyph states.
        if (state.equals("Expanded"))
          ViewPrefMgr.getViewPrefMgr().setTierState(tmpName, TierInfo.TIER_EXPANDED);
        else if (state.equals("Collapsed"))
          ViewPrefMgr.getViewPrefMgr().setTierState(tmpName, TierInfo.TIER_COLLAPSED);
        else if (state.equals("Fixed"))
          ViewPrefMgr.getViewPrefMgr().setTierState(tmpName, TierInfo.TIER_FIXED_SIZE);
        else if (state.equals("Hidden"))
          ViewPrefMgr.getViewPrefMgr().setTierState(tmpName, TierInfo.TIER_HIDDEN);
        sortAndScrollToItem(rowName);
        settingsChanged=true;
        break;
      }
      //Column Hide When Empty
      case 3: {
        TierInfo tmpInfo = (TierInfo)data.get(row);
        if (((Boolean)aValue).equals(Boolean.TRUE))
          ViewPrefMgr.getViewPrefMgr().setHideWhenEmptyForTier(tmpInfo, Boolean.TRUE);
        else if (((Boolean)aValue).equals(Boolean.FALSE))
          ViewPrefMgr.getViewPrefMgr().setHideWhenEmptyForTier(tmpInfo, Boolean.FALSE);
        sortAndScrollToItem(rowName);
        settingsChanged=true;
        break;
      }
      //Column Is Docked
      case 4: {
        TierInfo tmpInfo = (TierInfo)data.get(row);
        if (((Boolean)aValue).equals(Boolean.TRUE))
          ViewPrefMgr.getViewPrefMgr().setTierDocked(tmpInfo, true);
        else if (((Boolean)aValue).equals(Boolean.FALSE))
          ViewPrefMgr.getViewPrefMgr().setTierDocked(tmpInfo, false);
        sortAndScrollToItem(rowName);
        settingsChanged=true;
        break;
      }
      // Column Color
      case 5: {
        TierInfo tmpInfo = (TierInfo)data.get(row);
        ViewPrefMgr.getViewPrefMgr().setTierBackgroundColor(tmpInfo, ((ColorInfo)aValue).getName());
        sortAndScrollToItem(rowName);
        settingsChanged=true;
        break;
      }
    }
  }

  public void sortAndScrollToItem(String rowName) {
    MyComparator comp = new MyComparator(sortCol,sortAsc);
    Collections.sort(data,comp);
    ((MyTableModel)table.getModel()).fireTableDataChanged();
    if (!rowName.equals("")) {
      for (int x=0; x<table.getRowCount();x++) {
        if (((String)table.getModel().getValueAt(x,0)).equals(rowName)) {
          table.setRowSelectionInterval(x,x);
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
          rowName = ((TierInfo)data.get(targetRow)).getName();

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
        for (int i=0; i<names.length; i++) {
          TableColumn column = colModel.getColumn(i);
          column.setHeaderValue(table.getModel().getColumnName(column.getModelIndex()));
        }
        table.getTableHeader().repaint();
        ((MyTableModel)table.getModel()).sortAndScrollToItem(rowName);
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
          compObj1 = getValueAt((TierInfo)o1,sortCol);
          compObj2 = getValueAt((TierInfo)o2,sortCol);
          if (compObj1==null || compObj2==null) return 0;
          retVal = 0;
          if (compObj1 instanceof Comparable && compObj2 instanceof Comparable) {
            Comparable c1 = (Comparable)compObj1;
            Comparable c2 = (Comparable)compObj2;
            if (c1 instanceof String && c2 instanceof String) {
              String s1 = (String)c1;
              String s2 = (String)c2;
              retVal = sortAsc ? s1.compareToIgnoreCase(s2) : s2.compareToIgnoreCase(s1);
            }
            else retVal = sortAsc ? c1.compareTo(c2) : c2.compareTo(c1);
          }
          else if (compObj1 == null && compObj2 != null)
              retVal = sortAsc ? -1 : 1;
          else if (compObj2 == null && compObj1 != null)
              retVal = sortAsc ? 1 : -1;
        }
        catch (Exception ex) { return 0; }
        return retVal;
      }

  protected Object getValueAt(TierInfo ti, int col) {
    switch(col) {
      case 0: return ti.getName();
      case 1: return new Integer(ViewPrefMgr.getViewPrefMgr().
                getTierOrderValue("Genomic Axis Annotation", ti.getName()));
      case 2: return getStateString(ti.getState());
      case 3: {
        if (ti.getHideWhenEmpty()) return "True";
        else return "False";
      }
      case 4: {
        if (ti.getDocked()) return "True";
        else return "False";
      }
      case 5: return ti.getBackgroundColor();
      default: return "No Column Defined";
    }
  }

  }

}