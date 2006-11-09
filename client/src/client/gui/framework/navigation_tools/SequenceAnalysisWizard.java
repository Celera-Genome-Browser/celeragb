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

/**
 * Title:        Your Product Name<p>
 * Description:  This is the main Browser in the System<p>
 * @author Peter Davies
 * @version
 */
package client.gui.framework.navigation_tools;

import client.shared.text_component.StandardTextField;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EventObject;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;


public class SequenceAnalysisWizard extends JDialog {
  private String[] names = {"Argument", "Value"};
  private TreeMap displayRules = new TreeMap();
  private JDialog parentDialog;
  private String analysisType = "";
  private ArrayList arguments = new ArrayList();
  private ArrayList argumentValues = new ArrayList();
  JPanel mainPanel = new JPanel();

  TitledBorder titledBorder2;
  JButton okButton = new JButton();
  JButton cancelButton = new JButton();
  JLabel ruleNameLabel = new JLabel();
  JTextField nameTextField = new StandardTextField();
  JPanel macroTypePanel = new JPanel();
  TitledBorder titledBorder4;
  JPanel settingPanel = new JPanel();
  JPanel namePanel = new JPanel();
  TitledBorder titledBorder11;
  TitledBorder titledBorder12;
  JScrollPane settingsScrollPane = new JScrollPane();
  JTable settingsTable = new JTable();
  JLabel analysisTypeLabel = new JLabel();

  public SequenceAnalysisWizard(JDialog parentDialog, Object targetAnalysisType) {
    super(parentDialog, "New Macro", true);
    this.parentDialog=parentDialog;
    this.analysisType = targetAnalysisType.toString();
    displayRules = (TreeMap)SequenceAnalysisMgr.getSequenceAnalysisMgr().getSequenceAnalysisInfos();
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    // Format the analysis type items
    TreeMap typeMap = SequenceAnalysisMgr.getSequenceAnalysisMgr().getAnalysisTypes();
    JPanel typePanel = new JPanel();
    ButtonGroup typeGroup = new ButtonGroup();
    typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.Y_AXIS));
    for (Iterator it = typeMap.keySet().iterator(); it.hasNext(); ) {
      JCheckBox tmpBox = new JCheckBox((String)it.next());
      typeGroup.add(tmpBox);
      typePanel.add(tmpBox);
    }

    titledBorder2 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(149, 142, 130)),"Create A New Sequence Analysis Macro");
    titledBorder4 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(149, 142, 130)),"1. Selected Analysis Type");
    titledBorder11 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(149, 142, 130)),"2. Choose Analysis Settings");
    titledBorder12 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(149, 142, 130)),"3. Enter The Macro Name");
    this.getContentPane().setLayout(null);

    mainPanel.setBorder(titledBorder2);
    mainPanel.setBounds(new Rectangle(5, 5, 345, 408));
    mainPanel.setLayout(null);
    ruleNameLabel.setText("Name:");
    ruleNameLabel.setBounds(new Rectangle(31, 24, 102, 24));
    nameTextField.setBounds(new Rectangle(131, 26, 164, 20));
    macroTypePanel.setBorder(titledBorder4);
    macroTypePanel.setBounds(new Rectangle(13, 25, 319, 56));
    macroTypePanel.setLayout(null);
    settingPanel.setLayout(null);
    settingPanel.setBorder(titledBorder11);
    settingPanel.setBounds(new Rectangle(13, 91, 319, 197));
    namePanel.setBorder(titledBorder12);
    namePanel.setBounds(new Rectangle(13, 297, 319, 60));
    namePanel.setLayout(null);
    okButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okButton_actionPerformed(e);
      }
    });
    okButton.setBounds(new Rectangle(72, 370, 83, 27));
    okButton.setText("OK");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButton_actionPerformed(e);
      }
    });
    cancelButton.setBounds(new Rectangle(190, 370, 83, 27));
    cancelButton.setText("Cancel");

    JPanel fgPanel = new JPanel();
    settingsScrollPane.setBounds(new Rectangle(12, 25, 293, 161));
    analysisTypeLabel.setText(analysisType);
    analysisTypeLabel.setBounds(new Rectangle(25, 23, 129, 23));
    this.getContentPane().add(mainPanel, null);
    mainPanel.add(macroTypePanel, null);
    macroTypePanel.add(analysisTypeLabel, null);
    mainPanel.add(settingPanel, null);
    settingPanel.add(settingsScrollPane, null);
    mainPanel.add(namePanel, null);
    namePanel.add(ruleNameLabel, null);
    namePanel.add(nameTextField, null);
    mainPanel.add(okButton, null);
    mainPanel.add(cancelButton, null);
    settingsTable = new JTable(new MyTableModel());
    settingsTable.setBorder(BorderFactory.createLineBorder(Color.black));
    settingsTable.setPreferredScrollableViewportSize(new Dimension(185, 145));
    settingsTable.setColumnSelectionAllowed(false);
    settingsTable.getTableHeader().setReorderingAllowed(false);
    settingsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    settingsScrollPane.getViewport().add(settingsTable, null);

    fgPanel.setLayout(new BoxLayout(fgPanel,BoxLayout.Y_AXIS));
    setStates();
    settingsTable.setDefaultEditor(settingsTable.getModel().getColumnClass(1), new MyTableCellEditor());
    this.setSize(362, 455);
    this.setLocation(10, 10);
    this.setResizable(false);
  }

  void okButton_actionPerformed(ActionEvent e) {
    if (settingsTable.isEditing()) {
      settingsTable.getCellEditor().stopCellEditing();
    }

    if (analysisType!=null && nameTextField.getText()!=null && !nameTextField.getText().equals("")) {
      TreeMap tmpMap = SequenceAnalysisMgr.getSequenceAnalysisMgr().getSequenceAnalysisInfos();
      for (Iterator it = tmpMap.keySet().iterator();it.hasNext();) {
        String keyName = (String)it.next();
        if (keyName.equalsIgnoreCase(nameTextField.getText().trim())) {
          int answer = JOptionPane.showConfirmDialog(parentDialog, "That name already exists.\n"+
          "Do you want to override the original?", "Data Entry Error!", JOptionPane.YES_NO_OPTION);
          if (answer==JOptionPane.NO_OPTION) return;
          else {
            SequenceAnalysisMgr.getSequenceAnalysisMgr().
              deleteSequenceAnalysisInfo((SequenceAnalysisInfo)tmpMap.get(keyName));
            break;
          }
        }
      }

      // Get the settings for the new macro.
      TreeMap newSettings = new TreeMap();
      for (int i = 0; i < arguments.size(); i++) {
        newSettings.put(arguments.get(i), argumentValues.get(i));
      }

      SequenceAnalysisInfo newInfo = new SequenceAnalysisInfo(nameTextField.getText().trim(),
        analysisType, newSettings);
      SequenceAnalysisMgr.getSequenceAnalysisMgr().addSequenceAnalysisInfo(newInfo);
      this.dispose();
    }
    else JOptionPane.showMessageDialog(parentDialog, "The information entered is incorrect.\n"+
      "Please check all fields and try again.", "Data Entry Error!", JOptionPane.WARNING_MESSAGE);
  }

  public String getRuleName() {
    return nameTextField.getText().trim();
  }

  void cancelButton_actionPerformed(ActionEvent e) {
    this.dispose();
  }


  private void setStates() {
    AnalysisType tmpType = SequenceAnalysisMgr.getSequenceAnalysisMgr().getAnalysisType(analysisType);
    // Get the argument names according to the Analysis Type.
    TreeMap tmpTypeArgs = tmpType.getArguments();

    // Base the table according to the args defines in the Analysis Type object.
    // If the current macro knows the args, it sets them; otherwise, insert the default.
    for (Iterator it = tmpTypeArgs.keySet().iterator(); it.hasNext(); ) {
      AnalysisType.AnalysisProperty tmpProperty = (AnalysisType.AnalysisProperty)tmpTypeArgs.get(it.next());
      String tmpArgName = tmpProperty.getName();
      arguments.add(tmpArgName);
      argumentValues.add(tmpProperty.getDefaultValue());
    }

    settingsScrollPane.getVerticalScrollBar().setValue(0);
    updateData();
  }


  private void updateData() {
    ((MyTableModel)settingsTable.getModel()).fireTableDataChanged();
  }


  private class MyTableModel extends AbstractTableModel {
   public int getColumnCount() { return names.length; }
   public int getRowCount() { return arguments.size();}
   public String getColumnName(int column) {return names[column];}
   public Class getColumnClass(int c) {return getValueAt(0, c).getClass();}
   public boolean isCellEditable(int row, int col) {
    if (col==1) return true;
    else return false;
   }

   public Object getValueAt(int row, int col) {
    switch (col) {
      case 0: {
        return arguments.get(row);
      }
      case 1: {
        return argumentValues.get(row);
      }
    }
    return null;
   }

   public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
     if (aValue==null) return;
     argumentValues.set(rowIndex, aValue);
   }
 }

 private class MyTableCellEditor extends DefaultCellEditor {
    public MyTableCellEditor() {
      super(new JComboBox());
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
      boolean isSelected, int row, int column) {
        delegate.setValue(value);
	return getSpecialEditorComponent(row, column);
    }

    private Component getSpecialEditorComponent(int row, int column) {
      // The column should always be 1.  The real important part is the row.
      // That will determine the possible values to be set.
      AnalysisType tmpType = SequenceAnalysisMgr.getSequenceAnalysisMgr().getAnalysisType(analysisType);
      // Get the argument names according to the Analysis Type.
      TreeMap tmpTypeArgs = tmpType.getArguments();
      String targetArgument = (String)arguments.get(row);
      AnalysisType.AnalysisProperty tmpProperty =
        (AnalysisType.AnalysisProperty)tmpTypeArgs.get(targetArgument);

      if (tmpProperty.getEditor().equals(AnalysisType.STRING_COMBO_EDITOR)) {
        editorComponent = new JComboBox();
        JComboBox tmpBox = (JComboBox) editorComponent;
        delegate = new EditorDelegate() {
            public void setValue(Object value) {
		((JComboBox)editorComponent).setSelectedItem(value);
            }

	    public Object getCellEditorValue() {
		return ((JComboBox)editorComponent).getSelectedItem();
	    }

            public boolean shouldSelectCell(EventObject anEvent) {
                if (anEvent instanceof MouseEvent) {
                    MouseEvent e = (MouseEvent)anEvent;
                    return e.getID() != MouseEvent.MOUSE_DRAGGED;
                }
                return true;
            }
        };
        TreeMap options = tmpProperty.getPropertyOptions();
        for (Iterator it = options.keySet().iterator(); it.hasNext(); ) {
          String tmpValue = (String)options.get(it.next());
          tmpBox.addItem(tmpValue);
        }
        tmpBox.addActionListener(delegate);
        return tmpBox;
      }
      else if (tmpProperty.getEditor().equals(AnalysisType.NUMBER_COMBO_EDITOR)) {
        editorComponent = new JComboBox();
        JComboBox tmpBox = (JComboBox) editorComponent;
        delegate = new EditorDelegate() {
            public void setValue(Object value) {
		((JComboBox)editorComponent).setSelectedItem(value);
            }

	    public Object getCellEditorValue() {
		return ((JComboBox)editorComponent).getSelectedItem();
	    }

            public boolean shouldSelectCell(EventObject anEvent) {
                if (anEvent instanceof MouseEvent) {
                    MouseEvent e = (MouseEvent)anEvent;
                    return e.getID() != MouseEvent.MOUSE_DRAGGED;
                }
                return true;
            }
        };
        TreeMap options = tmpProperty.getPropertyOptions();
        ArrayList boxItems = new ArrayList();

        for (Iterator it = options.keySet().iterator(); it.hasNext(); ) {
          String tmpValue = (String)options.get(it.next());
          boxItems.add(tmpValue);
        }

        Object[] test = boxItems.toArray();
        Arrays.sort(test, new MyStringNumberComparator());
        boxItems = new ArrayList(Arrays.asList(test));

        for (Iterator it = boxItems.iterator(); it.hasNext(); ) {
          tmpBox.addItem(it.next());
        }
        tmpBox.addActionListener(delegate);
        return tmpBox;
      }
      // Default to the Text Field Editor
      else {
        JTextField tmpField = new JTextField((String)settingsTable.getValueAt(row, column));
        editorComponent = tmpField;
        delegate = new EditorDelegate() {
            public void setValue(Object value) {
		((JTextField)editorComponent).setText((value != null) ? value.toString() : "");
            }

	    public Object getCellEditorValue() {
		return ((JTextField)editorComponent).getText();
	    }
        };
        tmpField.addActionListener(delegate);
        return editorComponent;
      }
    }
 }


  private class MyStringNumberComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      Double d1 = new Double((String)o1);
      Double d2 = new Double((String)o2);
      return d1.compareTo(d2);
    }
  }
}