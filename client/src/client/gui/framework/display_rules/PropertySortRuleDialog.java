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
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class PropertySortRuleDialog extends JDialog {
  private boolean ruleChanged = false;
  private TreeMap displayRules = new TreeMap();
  private ActionListener myFGCheckBoxActionListener = new MyFGCheckBoxActionListener();
  private static Object [][] suggestedValueTable = new Object[PropertySortInfo.NUM_PROPS][PropertySortInfo.NUM_COL];
  JPanel mainPanel = new JPanel();
  JPanel rulePanel = new JPanel();
  Border border1;
  TitledBorder titledBorder1;
  JLabel jLabel1 = new JLabel();
  JComboBox ruleComboBox = new JComboBox();
  JButton newButton = new JButton();
  JButton saveButton = new JButton();
  JButton deleteButton = new JButton();
  JPanel ruleSettingsPanel = new JPanel();
  Border border2;
  TitledBorder titledBorder2;
  JRadioButton ascSortRadioButton = new JRadioButton();
  JRadioButton descSortRadioButton = new JRadioButton();
  JLabel sortOrderLabel = new JLabel();
  ButtonGroup orderButtonGroup = new ButtonGroup();
  JButton okButton = new JButton();
  JButton applyButton = new JButton();
  JButton resetViewButton = new JButton();
  JButton cancelButton = new JButton();
  JScrollPane jsp = new JScrollPane();
  JLabel featureGroupLabel = new JLabel();
  Browser browser;
  DefaultComboBoxModel ruleModel = new DefaultComboBoxModel();
  ArrayList fgList = new ArrayList();
  JPanel fgPanel = new JPanel();
  DefaultComboBoxModel fpModel= new DefaultComboBoxModel();
  JButton allFGButton = new JButton();
  JButton noFGButton = new JButton();
  JComboBox featurePropertyComboBox = new JComboBox();
  JLabel featurePropertyLabel = new JLabel();


  public PropertySortRuleDialog(Browser browser) {
    super(browser, "Property Sort Settings", false);
    this.browser=browser;
    displayRules = (TreeMap)DisplayFilterMgr.getDisplayFilterMgr().getPropertySortFilters();
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    border1 = BorderFactory.createEtchedBorder(new Color(231, 255, 241),new Color(113, 140, 118));
    titledBorder1 = new TitledBorder(border1,"Display Rule");
    border2 = BorderFactory.createEtchedBorder(new Color(231, 255, 241),new Color(113, 140, 118));
    titledBorder2 = new TitledBorder(border2,"Rule Settings");
    suggestedValueTable = PropertySortInfo.getPropertyInformation();
    for (int x = 0; x<suggestedValueTable.length; x++) {
      fpModel.addElement(PropertyMgr.getPropertyMgr().getPropertyDisplayName((String)suggestedValueTable[x][0]));
    }
    mainPanel.setLayout(null);
    mainPanel.setBorder(BorderFactory.createEtchedBorder());
    mainPanel.setBounds(new Rectangle(6, 5, 381, 480));
    rulePanel.setBorder(titledBorder1);
    rulePanel.setBounds(new Rectangle(13, 11, 353, 92));
    rulePanel.setLayout(null);
    jLabel1.setText("Rule:");
    jLabel1.setBounds(new Rectangle(14, 22, 50, 22));
    ruleComboBox.setBounds(new Rectangle(66, 21, 253, 24));
    ruleComboBox.setModel(ruleModel);
    newButton.setText("New");
    newButton.setBounds(new Rectangle(18, 55, 86, 26));
    newButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        newRuleButton_actionPerformed(e);
      }
    });

    saveButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveButton_actionPerformed();
      }
    });
    saveButton.setText("Save");
    saveButton.setBounds(new Rectangle(126, 55, 86, 26));
    deleteButton.setText("Delete");
    deleteButton.setBounds(new Rectangle(233, 55, 86, 26));
    deleteButton.addActionListener(new ActionListener(){
	  public void actionPerformed(ActionEvent e){
	    deleteRuleButton_actionPerformed(e);
	  }

    });

    ruleSettingsPanel.setBorder(titledBorder2);
    ruleSettingsPanel.setBounds(new Rectangle(13, 109, 353, 295));
    ruleSettingsPanel.setLayout(null);
    ascSortRadioButton.setSelected(true);
    ascSortRadioButton.setText("Ascending");
    ascSortRadioButton.setBounds(new Rectangle(17, 40, 103, 25));
    ascSortRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ruleChanged = true;
        saveButton.setEnabled(true);
      }
    });

    descSortRadioButton.setText("Descending");
    descSortRadioButton.setBounds(new Rectangle(136, 40, 103, 25));
    descSortRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ruleChanged = true;
        saveButton.setEnabled(true);
      }
    });

    sortOrderLabel.setText("Sort Order Type");
    sortOrderLabel.setBounds(new Rectangle(17, 17, 150, 25));
    okButton.setText("OK");
    okButton.setBounds(new Rectangle(42, 447, 95, 23));
    okButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okButton_actionPerformed(e);
      }
    });
    applyButton.setText("Apply Rule");
    applyButton.setBounds(new Rectangle(74, 414, 102, 25));
    applyButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
	    viewApplyButton_actionPerformed(e);

	}
    });

    resetViewButton.setText("Reset View");
    resetViewButton.setBounds(new Rectangle(194, 414, 102, 25));
    resetViewButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
	    viewResetButton_actionPerformed(e);

	}
    });

    cancelButton.setText("Cancel");
    cancelButton.setBounds(new Rectangle(249, 447, 95, 23));
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButton_actionPerformed(e);
      }
    });

    jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    jsp.setBounds(new Rectangle(17, 91, 243, 156));
    featureGroupLabel.setText("Feature Groups");
    featureGroupLabel.setBounds(new Rectangle(17, 70, 113, 17));
    allFGButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        ruleChanged=true;
        saveButton.setEnabled(true);
        for (int i = 0; i < fgList.size(); i++) {
          ((JCheckBox)fgList.get(i)).setSelected(true);
        }
      }
    });
    allFGButton.setBounds(new Rectangle(267, 131, 72, 23));
    allFGButton.setText("All");

    noFGButton.setText("None");
    noFGButton.setBounds(new Rectangle(268, 183, 72, 23));
    noFGButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        ruleChanged=true;
        saveButton.setEnabled(true);
        for (int i = 0; i < fgList.size(); i++) {
          ((JCheckBox)fgList.get(i)).setSelected(false);
        }
      }
    });

    fgPanel.setLayout(new BoxLayout(fgPanel,BoxLayout.Y_AXIS));
    featurePropertyComboBox.setModel(fpModel);
    featurePropertyComboBox.setBounds(new Rectangle(127, 261, 193, 21));
    featurePropertyLabel.setText("Feature Property:");
    featurePropertyLabel.setBounds(new Rectangle(17, 263, 108, 17));
    this.getContentPane().setLayout(null);
    orderButtonGroup.add(ascSortRadioButton);
    orderButtonGroup.add(descSortRadioButton);
    this.getContentPane().add(mainPanel, null);
    mainPanel.add(rulePanel, null);
    rulePanel.add(jLabel1, null);
    rulePanel.add(newButton, null);
    rulePanel.add(saveButton, null);
    rulePanel.add(deleteButton, null);
    rulePanel.add(ruleComboBox, null);
    mainPanel.add(ruleSettingsPanel, null);
    ruleSettingsPanel.add(ascSortRadioButton, null);
    ruleSettingsPanel.add(descSortRadioButton, null);
    ruleSettingsPanel.add(sortOrderLabel, null);
    ruleSettingsPanel.add(jsp, null);
    ruleSettingsPanel.add(featureGroupLabel, null);
    ruleSettingsPanel.add(noFGButton, null);
    ruleSettingsPanel.add(allFGButton, null);
    ruleSettingsPanel.add(featurePropertyLabel, null);
    ruleSettingsPanel.add(featurePropertyComboBox, null);
    mainPanel.add(resetViewButton, null);
    mainPanel.add(cancelButton, null);
    mainPanel.add(okButton, null);
    mainPanel.add(applyButton, null);
    jsp.getViewport().add(fgPanel, null);

    this.setSize(400, 528);
    resetComponentStates(false);
    this.setLocation(10, 10);
    this.setVisible(true);
  }

  private void newRuleButton_actionPerformed(ActionEvent e) {
    PropertySortRuleWizard newRuleWizard = new PropertySortRuleWizard(browser);
    displayRules = (TreeMap)DisplayFilterMgr.getDisplayFilterMgr().getPropertySortFilters();
    buildRuleModel(newRuleWizard.getRuleName());
  }

  private void okButton_actionPerformed(ActionEvent e) {
    saveButton_actionPerformed();
    this.dispose();
  }

  private void cancelButton_actionPerformed(ActionEvent e) {
    this.dispose();
  }

  private void saveButton_actionPerformed() {
    PropertySortInfo tmpRule = (PropertySortInfo)ruleComboBox.getSelectedItem();
    if (tmpRule==null) return;
    int selectedIndex = featurePropertyComboBox.getSelectedIndex();
    tmpRule.setTargetProperty((String)suggestedValueTable[selectedIndex][0]);

    ArrayList tmpFGList = new ArrayList();
    for (Iterator it = fgList.iterator(); it.hasNext(); ) {
      JCheckBox tmpBox = (JCheckBox)it.next();
      if (tmpBox.isSelected()) tmpFGList.add(tmpBox.getText());
    }
    tmpRule.setEffectedFGs(tmpFGList);

    if (ascSortRadioButton.isSelected()) tmpRule.setDisplayState(PropertySortInfo.ASCENDING);
    else if (descSortRadioButton.isSelected()) tmpRule.setDisplayState(PropertySortInfo.DESCENDING);

    displayRules.put(tmpRule.getName(),
      (PropertySortInfo)ruleComboBox.getSelectedItem());
    buildRuleModel(tmpRule.getName());
  }


  private void deleteRuleButton_actionPerformed(ActionEvent e) {
    if (ruleComboBox.getSelectedItem()!=null) {
      String targetName = ((PropertySortInfo)ruleComboBox.getSelectedItem()).getName();
      int answer = JOptionPane.showConfirmDialog(this, "Deleting rule: "+targetName+".\n"+
      "Are you sure?", "Rule Deletion", JOptionPane.YES_NO_OPTION);
      if (answer==JOptionPane.NO_OPTION) return;
      DisplayFilterMgr.getDisplayFilterMgr().deletePropertySortFilter(
        (PropertySortInfo)ruleComboBox.getSelectedItem());
      displayRules = DisplayFilterMgr.getDisplayFilterMgr().getPropertySortFilters();
      buildRuleModel("");
    }
    if (ruleComboBox.getItemCount()==0) {
      fgList.clear();
      fgPanel.removeAll();
      resetComponentStates(false);
    }
  }


 private void resetComponentStates(boolean enabledState) {
    sortOrderLabel.setEnabled(enabledState);
    ascSortRadioButton.setEnabled(enabledState);
    descSortRadioButton.setEnabled(enabledState);
    featureGroupLabel.setEnabled(enabledState);
    allFGButton.setEnabled(enabledState);
    noFGButton.setEnabled(enabledState);
    jsp.setEnabled(enabledState);
    applyButton.setEnabled(enabledState);
    resetViewButton.setEnabled(enabledState);
    deleteButton.setEnabled(enabledState);
    featurePropertyLabel.setEnabled(enabledState);
    featurePropertyComboBox.setEnabled(enabledState);
    // Clear the old check boxes.
    for (Iterator it = fgList.iterator(); it.hasNext();) {
      ((JCheckBox)it.next()).setSelected(false);
    }
    ruleChanged=false;
    saveButton.setEnabled(false);
  }


 private void viewApplyButton_actionPerformed(ActionEvent e) {
    saveButton_actionPerformed();
    PropertySortInfo p=  (PropertySortInfo)displayRules.get(this.ruleComboBox.getSelectedItem());
    browser.getBrowserModel().setModelProperty(browser.getBrowserModel().DISPLAY_FILTER_PROPERTY,
      p);
  }


  private void viewResetButton_actionPerformed(ActionEvent e){
     browser.getBrowserModel().setModelProperty("ResetVerticalPacking",
      "");

  }

  private void buildRuleModel(String currentRule) {
    ruleModel.removeAllElements();
    if (displayRules!=null && displayRules.size()>0) {
      for (Iterator it = displayRules.keySet().iterator();it.hasNext();) {
        ruleModel.addElement((PropertySortInfo)displayRules.get(it.next()));
      }
      if (ruleModel.getSize()==0) resetComponentStates(false);
      else resetComponentStates(true);
      if (!currentRule.equals("")) ruleComboBox.setSelectedItem((PropertySortInfo)displayRules.get(currentRule));
      else ruleComboBox.setSelectedIndex(0);
      setStates();
    }
  }


  private void setStates() {
    if (ruleComboBox.getItemCount()<=0 ||
      ruleComboBox.getSelectedItem()==null) return;
    PropertySortInfo tmpRule = (PropertySortInfo)ruleComboBox.getSelectedItem();

    featurePropertyComboBox.setSelectedItem(PropertyMgr.getPropertyMgr().getPropertyDisplayName(
      tmpRule.getTargetProperty()));
    featurePropertyComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ruleChanged = true;
        saveButton.setEnabled(true);
      }
    });

    if (tmpRule.getDisplayState()==PropertySortInfo.ASCENDING) ascSortRadioButton.setSelected(true);
    else if (tmpRule.getDisplayState()==PropertySortInfo.DESCENDING) descSortRadioButton.setSelected(true);

    jsp.getVerticalScrollBar().setValue(0);
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
    jsp.setViewportView(fgPanel);

    ruleChanged=false;
    saveButton.setEnabled(false);
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
}