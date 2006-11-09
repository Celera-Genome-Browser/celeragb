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
package client.gui.framework.display_rules;

import api.entity_model.management.PropertyMgr;
import client.gui.framework.browser.Browser;
import client.gui.framework.view_pref_mgr.ViewPrefMgr;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ColorRuleDialog extends JDialog {
  private String[] names = {"Value", "Intensity", "Example"};
  private boolean positiveDataSlope=true;
  private boolean viewExists = false;
  private TreeMap displayRules = new TreeMap(new MyStringComparator());
  private Browser browser;
  private static Object [][] suggestedValueTable = new Object[ColorIntensityInfo.NUM_PROPS][ColorIntensityInfo.NUM_COL];
  private ActionListener myPropertyComboListener = new MyPropertyActionListener();
  private ActionListener myFGCheckBoxActionListener = new MyFGCheckBoxActionListener();
  boolean ruleChanged = false;
  JPanel mainPanel = new JPanel();
  JRadioButton logRadioButton = new JRadioButton();
  JRadioButton interpolateRadioButton = new JRadioButton();
  JRadioButton stepColorRadioButton = new JRadioButton();
  JComboBox featurePropertyComboBox = new JComboBox();
  DefaultComboBoxModel ruleModel = new DefaultComboBoxModel();
  DefaultComboBoxModel groupModel = new DefaultComboBoxModel();
  DefaultComboBoxModel fpModel= new DefaultComboBoxModel();
  JLabel featurePropertyLabel = new JLabel();
  TitledBorder titledBorder2;
  JPanel displaySettingsPanel = new JPanel();
  TitledBorder titledBorder3;
  JButton okButton = new JButton();
  JButton cancelButton = new JButton();
  JPanel displayRulePanel = new JPanel();
  TitledBorder titledBorder5;
  JComboBox ruleComboBox = new JComboBox();
  JLabel ruleLabel = new JLabel();
  JButton newRuleButton = new JButton();
  JButton deleteRuleButton = new JButton();
  JButton saveButton = new JButton();
  ButtonGroup displayTypeButtonGroup = new ButtonGroup();
  Border border1;
  JButton viewApplyButton = new JButton();
  JButton viewRefreshButton = new JButton();
  JScrollPane jScrollPane1 = new JScrollPane();
  JPanel fgPanel = new JPanel();
  JButton allFGButton = new JButton();
  JButton noFGButton = new JButton();
  ArrayList fgList = new ArrayList();
  JLabel featureGroupLabel = new JLabel();
  JLabel calculationTypeLabel = new JLabel();
  JLabel maxIntLabel = new JLabel();
  JLabel minIntensityLabel = new JLabel();
  JTextField maxIntensityTextField = new JTextField();
  JTextField minIntensityTextField = new JTextField();

  public ColorRuleDialog(Browser browser) {
    super(browser, "Color Rule Settings", false);
    this.browser=browser;
    if (browser.getMasterEditor()!=null) viewExists=true;
    displayRules = (TreeMap)DisplayFilterMgr.getDisplayFilterMgr().getColorIntensityFilters();
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    titledBorder2 = new TitledBorder("");
    titledBorder3 = new TitledBorder("Rule Settings");
    titledBorder5 = new TitledBorder("Display Rule");
    this.getContentPane().setLayout(null);
    suggestedValueTable = ColorIntensityInfo.getPropertyInformation();
    for (int x = 0; x<suggestedValueTable.length; x++) {
      fpModel.addElement(PropertyMgr.getPropertyMgr().getPropertyDisplayName((String)suggestedValueTable[x][0]));
    }
    featurePropertyComboBox.setModel(fpModel);
    featurePropertyComboBox.setBounds(new Rectangle(118, 244, 164, 21));
    TreeMap featureMap = ViewPrefMgr.getViewPrefMgr().getFeatureCollection();
    for (Iterator it = featureMap.keySet().iterator();it.hasNext();) {
      groupModel.addElement((String)it.next());
    }
    featurePropertyComboBox.setSize(190, 21);
    featurePropertyComboBox.addActionListener(myPropertyComboListener);
    interpolateRadioButton.setBorder(BorderFactory.createLineBorder(Color.black));
    interpolateRadioButton.setText("Interpolate");
    interpolateRadioButton.setBounds(new Rectangle(104, 49, 94, 19));
    interpolateRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if (evt.getSource()==interpolateRadioButton) {
          enableTextFields(true);
          ruleChanged=true;
          saveButton.setEnabled(true);
        }
      }
    });
    logRadioButton.setBorder(BorderFactory.createLineBorder(Color.black));
    logRadioButton.setText("Log Scale");
    logRadioButton.setBounds(new Rectangle(216, 49, 88, 19));
    logRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if (evt.getSource()==logRadioButton) {
          enableTextFields(true);
          ruleChanged=true;
          saveButton.setEnabled(true);
        }
      }
    });
     //Create the scroll pane and add the table to it.

    //Add the scroll pane to this window.
    mainPanel.setBorder(titledBorder2);
    mainPanel.setBounds(new Rectangle(6, 6, 340, 561));
    mainPanel.setLayout(null);
    featurePropertyLabel.setText("Feature Property:");
    featurePropertyLabel.setBounds(new Rectangle(14, 246, 97, 17));
    stepColorRadioButton.setText("Step");
    stepColorRadioButton.setBounds(new Rectangle(15, 49, 70, 19));
    stepColorRadioButton.setBorder(BorderFactory.createLineBorder(Color.black));
    stepColorRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if (evt.getSource()==stepColorRadioButton) {
          enableTextFields(false);
          ruleChanged=true;
          saveButton.setEnabled(true);
        }
      }
    });
    displaySettingsPanel.setBorder(titledBorder3);
    displaySettingsPanel.setBounds(new Rectangle(12, 116, 321, 359));
    displaySettingsPanel.setLayout(null);
    viewApplyButton.setToolTipText("Apply rule to view.");
    viewApplyButton.setText("Apply Rule");
    viewApplyButton.setBounds(new Rectangle(43, 484, 106, 27));
    viewApplyButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        viewApplyButton_actionPerformed(e);
      }
    });
    viewRefreshButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        viewRefreshButton_actionPerformed(e);
      }
    });
    viewRefreshButton.setBounds(new Rectangle(178, 484, 106, 27));
    viewRefreshButton.setToolTipText("Resets view intensities.");
    viewRefreshButton.setText("Reset View");
    okButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okButton_actionPerformed(e);
      }
    });
    okButton.setBounds(new Rectangle(17, 523, 106, 27));
    okButton.setText("OK");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButton_actionPerformed(e);
      }
    });
    cancelButton.setBounds(new Rectangle(215, 523, 106, 27));
    cancelButton.setText("Cancel");
    ruleComboBox.setModel(ruleModel);
    ruleComboBox.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt) {
        if (evt.getSource()==ruleComboBox) {
          setStates();
        }
      }});
    displayRulePanel.setBorder(titledBorder5);
    displayRulePanel.setBounds(new Rectangle(10, 10, 321, 105));
    displayRulePanel.setLayout(null);
    ruleLabel.setText("Rule:");
    ruleLabel.setBounds(new Rectangle(16, 27, 42, 24));
    ruleComboBox.setBounds(new Rectangle(56, 29, 235, 21));
    newRuleButton.setToolTipText("Add a new Display Rule.");
    newRuleButton.setText("New");
    newRuleButton.setBounds(new Rectangle(16, 66, 72, 27));
    newRuleButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        newRuleButton_actionPerformed(e);
      }
    });
    deleteRuleButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteRuleButton_actionPerformed(e);
      }
    });
    deleteRuleButton.setBounds(new Rectangle(233, 66, 72, 27));
    deleteRuleButton.setText("Delete");
    deleteRuleButton.setToolTipText("Delete selected Display Rule.");
    saveButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveButton_actionPerformed();
      }
    });
    saveButton.setBounds(new Rectangle(124, 66, 72, 27));
    saveButton.setText("Save");
    saveButton.setToolTipText("Save the current Display Rule.");
    jScrollPane1.setBounds(new Rectangle(15, 98, 203, 131));
    allFGButton.setText("All");
    allFGButton.setBounds(new Rectangle(231, 125, 75, 27));
    allFGButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        ruleChanged=true;
        saveButton.setEnabled(true);
        for (int i = 0; i < fgList.size(); i++) {
          ((JCheckBox)fgList.get(i)).setSelected(true);
        }
      }
    });

    noFGButton.setBounds(new Rectangle(231, 171, 75, 27));
    noFGButton.setText("None");
    noFGButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        ruleChanged=true;
        saveButton.setEnabled(true);
        for (int i = 0; i < fgList.size(); i++) {
          ((JCheckBox)fgList.get(i)).setSelected(false);
        }
      }
    });

    featureGroupLabel.setText("Feature Groups");
    featureGroupLabel.setBounds(new Rectangle(15, 78, 121, 18));
    calculationTypeLabel.setBounds(new Rectangle(15, 26, 121, 18));
    calculationTypeLabel.setText("Calculation Type");
    maxIntLabel.setText("Max Intensity At:");
    maxIntLabel.setBounds(new Rectangle(14, 288, 125, 24));
    minIntensityLabel.setText("Min Intensity At:");
    minIntensityLabel.setBounds(new Rectangle(14, 323, 121, 24));
    maxIntensityTextField.setBounds(new Rectangle(142, 288, 138, 24));
    maxIntensityTextField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e){ textModified(); }
      public void removeUpdate(DocumentEvent e){ textModified(); }
      public void changedUpdate(DocumentEvent e){ textModified(); }
    });
    minIntensityTextField.setBounds(new Rectangle(142, 323, 138, 24));
    minIntensityTextField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e){ textModified(); }
      public void removeUpdate(DocumentEvent e){ textModified(); }
      public void changedUpdate(DocumentEvent e){ textModified(); }
    });
    displayTypeButtonGroup.add(stepColorRadioButton);
    displayTypeButtonGroup.add(interpolateRadioButton);
    displayTypeButtonGroup.add(logRadioButton);

    fgPanel.setLayout(new BoxLayout(fgPanel,BoxLayout.Y_AXIS));
    this.getContentPane().add(mainPanel, null);
    mainPanel.add(displayRulePanel, null);
    displayRulePanel.add(ruleLabel, null);
    displayRulePanel.add(saveButton, null);
    displayRulePanel.add(deleteRuleButton, null);
    displayRulePanel.add(newRuleButton, null);
    displayRulePanel.add(ruleComboBox, null);
    mainPanel.add(displaySettingsPanel, null);
    displaySettingsPanel.add(logRadioButton, null);
    displaySettingsPanel.add(stepColorRadioButton, null);
    displaySettingsPanel.add(interpolateRadioButton, null);
    displaySettingsPanel.add(calculationTypeLabel, null);
    displaySettingsPanel.add(featureGroupLabel, null);
    displaySettingsPanel.add(jScrollPane1, null);
    displaySettingsPanel.add(noFGButton, null);
    displaySettingsPanel.add(allFGButton, null);
    displaySettingsPanel.add(maxIntLabel, null);
    displaySettingsPanel.add(maxIntensityTextField, null);
    displaySettingsPanel.add(minIntensityLabel, null);
    displaySettingsPanel.add(minIntensityTextField, null);
    displaySettingsPanel.add(featurePropertyLabel, null);
    displaySettingsPanel.add(featurePropertyComboBox, null);
    mainPanel.add(cancelButton, null);
    mainPanel.add(viewApplyButton, null);
    mainPanel.add(viewRefreshButton, null);
    mainPanel.add(okButton, null);
    jScrollPane1.getViewport().add(fgPanel, null);

    this.setSize(360, 600);
    resetComponentStates(false);
    buildRuleModel("");
    setLocation(10, 10);
    /**
     * @todo Due to an NT jdk1.3.0.X bug the image icon will not show if isResizable is false.
     * When moving to a new JDK make it false again.
     */
    this.setResizable(true);
  }


  private void enableTextFields(boolean isStepType) {
    minIntensityLabel.setEnabled(isStepType);
    minIntensityTextField.setEnabled(isStepType);
  }

  /**
   * Helper method that handles when text is typed into the max or min fields.
   */
  private void textModified() {
    ruleChanged=true;
    saveButton.setEnabled(true);
  }


  private void setStates() {
    if (ruleComboBox.getItemCount()<=0 ||
      ruleComboBox.getSelectedItem()==null) return;
    ColorIntensityInfo tmpRule = (ColorIntensityInfo)ruleComboBox.getSelectedItem();
    double maxIntensityValue = (double)tmpRule.getMaxValue();
    double minIntensityValue = (double)tmpRule.getMinValue();

    positiveDataSlope = (maxIntensityValue>minIntensityValue);
    featurePropertyComboBox.removeActionListener(myPropertyComboListener);
    featurePropertyComboBox.setSelectedItem(PropertyMgr.getPropertyMgr().getPropertyDisplayName(
      tmpRule.getTargetProperty()));

    featurePropertyComboBox.addActionListener(myPropertyComboListener);

    maxIntensityTextField.setText(Double.toString(tmpRule.getMaxValue()));
    minIntensityTextField.setText(Double.toString(tmpRule.getMinValue()));
    jScrollPane1.getVerticalScrollBar().setValue(0);
    fgList.clear();
    fgPanel.removeAll();
    Map map=ViewPrefMgr.getViewPrefMgr().getFeatureCollection();
    Set discoveryEnvironments=map.keySet();
    for (Iterator it=discoveryEnvironments.iterator();it.hasNext(); ) {
      Object next=it.next();
      JCheckBox tmpCheck = new JCheckBox(next.toString(), false);
      tmpCheck.addActionListener(myFGCheckBoxActionListener);
      fgList.add(tmpCheck);
      fgPanel.add(tmpCheck);
    }
    ArrayList featureGroups = tmpRule.getEffectedFGs();
    // Set the checked state of in this feature group name list.
    for (Iterator it = featureGroups.iterator(); it.hasNext();) {
      String targetFG = (String)it.next();
      boolean foundCorrespondingBox = false;
      //  Look through all of the known checkboxes to find the target rule Feature Group
      //  If yes check the box, if no add it.
      for (Iterator it2 = fgList.iterator(); it2.hasNext();) {
        JCheckBox tmpBox = (JCheckBox)it2.next();
        if (tmpBox.getText().equals(targetFG)) {
          tmpBox.setSelected(true);
          foundCorrespondingBox = true;
        }
      }
      if (!foundCorrespondingBox) {
        JCheckBox newBox = new JCheckBox(targetFG);
        newBox.setSelected(true);
        newBox.addActionListener(myFGCheckBoxActionListener);
        fgList.add(newBox);
        fgPanel.add(newBox);
        // Should probably sort here.  Use an already sorted collection!!!!!!
      }
      else foundCorrespondingBox = false;
    }
    jScrollPane1.setViewportView(fgPanel);

    int tmpType = tmpRule.getDisplayState();
    if (tmpType==ColorIntensityInfo.STEP) {
      enableTextFields(false);
      stepColorRadioButton.setSelected(true);
    }
    else if (tmpType==ColorIntensityInfo.INTERPOLATION) {
      enableTextFields(true);
      interpolateRadioButton.setSelected(true);
    }
    else if (tmpType==ColorIntensityInfo.LOG) {
      enableTextFields(true);
      logRadioButton.setSelected(true);
    }
    ruleChanged=false;
    saveButton.setEnabled(false);
  }

  private void resetComponentStates(boolean enabledState) {
    stepColorRadioButton.setEnabled(enabledState);
    featurePropertyComboBox.removeActionListener(myPropertyComboListener);
    featurePropertyComboBox.setSelectedIndex(0);
    featurePropertyComboBox.addActionListener(myPropertyComboListener);
    featureGroupLabel.setEnabled(enabledState);
    stepColorRadioButton.setEnabled(enabledState);
    interpolateRadioButton.setEnabled(enabledState);
    logRadioButton.setEnabled(enabledState);
    maxIntLabel.setEnabled(enabledState);
    maxIntensityTextField.setEnabled(enabledState);
    minIntensityLabel.setEnabled(enabledState);
    minIntensityTextField.setEnabled(enabledState);
    calculationTypeLabel.setEnabled(enabledState);
    allFGButton.setEnabled(enabledState);
    noFGButton.setEnabled(enabledState);
    jScrollPane1.setEnabled(enabledState);

    if (viewExists) {
      viewApplyButton.setEnabled(enabledState);
      viewRefreshButton.setEnabled(enabledState);
    }
    else {
      viewApplyButton.setEnabled(false);
      viewRefreshButton.setEnabled(false);
    }
    deleteRuleButton.setEnabled(enabledState);
    featurePropertyLabel.setEnabled(enabledState);
    featurePropertyComboBox.setEnabled(enabledState);
    // Clear the old check boxes.
    for (Iterator it = fgList.iterator(); it.hasNext();) {
      ((JCheckBox)it.next()).setSelected(false);
    }
    ruleChanged=false;
    saveButton.setEnabled(false);
  }


  private void resetSettings() {
    ColorIntensityInfo tmpRule = (ColorIntensityInfo)ruleComboBox.getSelectedItem();
    int propertyIndex = featurePropertyComboBox.getSelectedIndex();
    double max = ((Double)suggestedValueTable[propertyIndex][2]).doubleValue();
    double min = ((Double)suggestedValueTable[propertyIndex][1]).doubleValue();

    tmpRule.setTargetProperty((String)suggestedValueTable[propertyIndex][0]);
    tmpRule.setMaxValue((long)max);
    tmpRule.setMinValue((long)min);
  }


  private void buildRuleModel(String currentRule) {
    ruleModel.removeAllElements();
    if (displayRules!=null && displayRules.size()>0) {
      for (Iterator it = displayRules.keySet().iterator();it.hasNext();) {
        ruleModel.addElement((ColorIntensityInfo)displayRules.get(it.next()));
      }
      if (ruleModel.getSize()==0) resetComponentStates(false);
      else resetComponentStates(true);
      if (!currentRule.equals("")) ruleComboBox.setSelectedItem((ColorIntensityInfo)displayRules.get(currentRule));
      else ruleComboBox.setSelectedIndex(0);
      setStates();
    }
  }


  private void okButton_actionPerformed(ActionEvent e) {
    saveButton_actionPerformed();
    this.dispose();
  }

  private void cancelButton_actionPerformed(ActionEvent e) {
    this.dispose();
  }

  private void newRuleButton_actionPerformed(ActionEvent e) {
    ColorRuleWizard newRuleWizard = new ColorRuleWizard(browser);
    displayRules = (TreeMap)DisplayFilterMgr.getDisplayFilterMgr().getColorIntensityFilters();
    buildRuleModel(newRuleWizard.getRuleName());
  }

  private void deleteRuleButton_actionPerformed(ActionEvent e) {
    if (ruleComboBox.getSelectedItem()!=null) {
      String targetName = ((ColorIntensityInfo)ruleComboBox.getSelectedItem()).getName();
      int answer = JOptionPane.showConfirmDialog(this, "Deleting rule: "+targetName+".\n"+
      "Are you sure?", "Rule Deletion", JOptionPane.YES_NO_OPTION);
      if (answer==JOptionPane.NO_OPTION) return;
      DisplayFilterMgr.getDisplayFilterMgr().deleteColorIntensityFilter(
        (ColorIntensityInfo)ruleComboBox.getSelectedItem());
      displayRules = DisplayFilterMgr.getDisplayFilterMgr().getColorIntensityFilters();
      buildRuleModel("");
    }
    if (ruleComboBox.getItemCount()==0) {
      fgList.clear();
      fgPanel.removeAll();
      resetComponentStates(false);
    }
  }

  private void saveButton_actionPerformed() {
    ColorIntensityInfo tmpRule = (ColorIntensityInfo)ruleComboBox.getSelectedItem();
    if (tmpRule==null) return;
    int selectedIndex = featurePropertyComboBox.getSelectedIndex();
    tmpRule.setTargetProperty((String)suggestedValueTable[selectedIndex][0]);

    ArrayList tmpFGList = new ArrayList();
    for (Iterator it = fgList.iterator(); it.hasNext(); ) {
      JCheckBox tmpBox = (JCheckBox)it.next();
      if (tmpBox.isSelected()) tmpFGList.add(tmpBox.getText());
    }
    tmpRule.setEffectedFGs(tmpFGList);

    if (stepColorRadioButton.isSelected()) tmpRule.setDisplayState(ColorIntensityInfo.STEP);
    else if (interpolateRadioButton.isSelected()) tmpRule.setDisplayState(ColorIntensityInfo.INTERPOLATION);
    else if (logRadioButton.isSelected()) tmpRule.setDisplayState(ColorIntensityInfo.LOG);

    tmpRule.setMaxValue(Double.parseDouble(maxIntensityTextField.getText().trim()));
    tmpRule.setMinValue(Double.parseDouble(minIntensityTextField.getText().trim()));

    displayRules.put(tmpRule.getName(),
      (ColorIntensityInfo)ruleComboBox.getSelectedItem());
    buildRuleModel(tmpRule.getName());
  }


  private class MyPropertyActionListener implements ActionListener {
      public void actionPerformed(ActionEvent evt) {
        if (evt.getSource()==featurePropertyComboBox) {
          int answer = JOptionPane.showConfirmDialog(ColorRuleDialog.this, "Would you like to use the default values\n"+
            "for this property?", "Property Change", JOptionPane.YES_NO_OPTION);
          if (answer==JOptionPane.YES_OPTION) {
            resetSettings();
            setStates();
          }
          ruleChanged=true;
          saveButton.setEnabled(true);
        }
      }
    }

  private class MyFGCheckBoxActionListener implements ActionListener {
      public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() instanceof JCheckBox) {
          JCheckBox tmpBox = (JCheckBox)evt.getSource();
          if (fgList.contains(tmpBox)) {
            ruleChanged=true;
            saveButton.setEnabled(true);
          }
        }
      }
    }

  private class MyStringComparator implements Comparator {
    public int compare(Object key1, Object key2) {
      String keyName1, keyName2;
      try{
        keyName1 = (String)key1;
        keyName2 = (String)key2;
        if (keyName1==null || keyName2==null) return 0;
      }
      catch (Exception ex) { return 0; }
      return keyName1.compareToIgnoreCase(keyName2);
    }
  }


  /**
   * These are browser level properties being thrown around and they will not
   * be saved out with the session.  For this reason and not to create circular
   * dependencies, I am broadcasting a message to the GA view to apply the
   * "value" color rule.
   */
  private void viewApplyButton_actionPerformed(ActionEvent e) {
    saveButton_actionPerformed();
    browser.getBrowserModel().setModelProperty(browser.getBrowserModel().DISPLAY_FILTER_PROPERTY,
      this.ruleComboBox.getSelectedItem());
  }


  /**
   * Here, I am broadcasting a message to the GA view to reset the intensities.
   */
  private void viewRefreshButton_actionPerformed(ActionEvent e) {
    browser.getBrowserModel().setModelProperty("ResetGBGenomicGlyphIntensities", "");
  }
}