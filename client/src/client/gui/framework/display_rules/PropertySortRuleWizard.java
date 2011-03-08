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
import client.gui.framework.view_pref_mgr.ViewPrefMgr;
import client.shared.text_component.StandardTextField;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;


public class PropertySortRuleWizard extends JDialog {
  private TreeMap displayRules = new TreeMap();
  private JFrame parentFrame;
  private Object [][] suggestedValueTable = PropertySortInfo.suggestedValueTable;
  JPanel mainPanel = new JPanel();
  JRadioButton ascendingRadioButton = new JRadioButton();
  JRadioButton descendingRadioButton = new JRadioButton();

  JComboBox featurePropertyComboBox = new JComboBox();
  DefaultComboBoxModel fgModel= new DefaultComboBoxModel();
  JLabel featurePropertyLabel = new JLabel();

  TitledBorder titledBorder1;
  TitledBorder titledBorder2;
  TitledBorder titledBorder3;
  JButton okButton = new JButton();
  JButton cancelButton = new JButton();
  TitledBorder titledBorder5;
  ButtonGroup displayTypeButtonGroup = new ButtonGroup();
  JLabel ruleNameLabel = new JLabel();
  JTextField nameTextField = new StandardTextField();
  JPanel jPanel1 = new JPanel();
  TitledBorder titledBorder4;
  JPanel jPanel3 = new JPanel();
  TitledBorder titledBorder7;
  JPanel jPanel4 = new JPanel();
  JPanel jPanel5 = new JPanel();
  TitledBorder titledBorder9;
  TitledBorder titledBorder10;
  TitledBorder titledBorder11;
  TitledBorder titledBorder12;
  JButton allFGButton = new JButton();
  JButton noFGButton = new JButton();
  JScrollPane jScrollPane1 = new JScrollPane();
  // This is a list of the check boxes.
  ArrayList fgList = new ArrayList();

  public PropertySortRuleWizard(JFrame parentFrame) {
    super(parentFrame, "Property Sort Rule Settings", true);
    this.parentFrame=parentFrame;
    displayRules = (TreeMap)DisplayFilterMgr.getDisplayFilterMgr().getColorIntensityFilters();
    suggestedValueTable=PropertySortInfo.getPropertyInformation();
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    titledBorder1 = new TitledBorder("Values");
    titledBorder2 = new TitledBorder("Create A New Property Sort Rule");
    titledBorder3 = new TitledBorder("Display Settings");
    titledBorder5 = new TitledBorder("Display Rule");
    titledBorder4 = new TitledBorder("1. Select Property To Trigger From");
    titledBorder7 = new TitledBorder("3. Choose Sort Order Type");
    titledBorder11 = new TitledBorder("2. Select Affected Feature Groups");
    titledBorder10 = new TitledBorder("5. Enter Rule Name");
    titledBorder12 = new TitledBorder("4. Enter The Rule Name");
    this.getContentPane().setLayout(null);
    for (int x=0; x<suggestedValueTable.length; x++) {
      fgModel.addElement(PropertyMgr.getPropertyMgr().getPropertyDisplayName(
        (String)suggestedValueTable[x][0]));
    }
    featurePropertyComboBox.setMinimumSize(new Dimension(182, 21));
    featurePropertyComboBox.setPreferredSize(new Dimension(182, 21));
    featurePropertyComboBox.setModel(fgModel);
    featurePropertyComboBox.setBounds(new Rectangle(135, 34, 182, 21));

    featurePropertyComboBox.setSize(182, 21);
    descendingRadioButton.setBorder(BorderFactory.createLineBorder(Color.black));
    descendingRadioButton.setText("Descending");
    descendingRadioButton.setBounds(new Rectangle(140, 30, 95, 19));
    //Create the scroll pane and add the table to it.

    //Add the scroll pane to this window.
    mainPanel.setBorder(titledBorder2);
    mainPanel.setBounds(new Rectangle(5, 5, 356, 415));
    mainPanel.setLayout(null);
    featurePropertyLabel.setText("Feature Property:");
    featurePropertyLabel.setBounds(new Rectangle(19, 33, 108, 17));
    ascendingRadioButton.setText("Ascending");
    ascendingRadioButton.setBounds(new Rectangle(24, 30, 100, 19));
    ascendingRadioButton.setBorder(BorderFactory.createLineBorder(Color.black));
    ascendingRadioButton.setSelected(true);
    ruleNameLabel.setText("Name:");
    ruleNameLabel.setBounds(new Rectangle(31, 24, 102, 24));
    nameTextField.setBounds(new Rectangle(131, 26, 164, 20));
    jPanel1.setBorder(titledBorder4);
    jPanel1.setBounds(new Rectangle(13, 33, 329, 70));
    jPanel1.setLayout(null);
    jPanel3.setBorder(titledBorder7);
    jPanel3.setBounds(new Rectangle(13, 242, 329, 66));
    jPanel3.setLayout(null);
    jPanel4.setLayout(null);
    jPanel4.setBorder(titledBorder11);
    jPanel4.setBounds(new Rectangle(13, 113, 329, 124));
    jPanel5.setBorder(titledBorder12);
    jPanel5.setBounds(new Rectangle(13, 310, 329, 60));
    jPanel5.setLayout(null);
    okButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okButton_actionPerformed(e);
      }
    });
    okButton.setBounds(new Rectangle(72, 377, 83, 27));
    okButton.setText("OK");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButton_actionPerformed(e);
      }
    });
    cancelButton.setBounds(new Rectangle(190, 377, 83, 27));
    cancelButton.setText("Cancel");
    allFGButton.setText("All");
    allFGButton.setBounds(new Rectangle(242, 28, 77, 31));
    allFGButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        for (int i = 0; i < fgList.size(); i++) {
          ((JCheckBox)fgList.get(i)).setSelected(true);
        }
      }
    });

    noFGButton.setBounds(new Rectangle(242, 74, 77, 31));
    noFGButton.setText("None");
    noFGButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        for (int i = 0; i < fgList.size(); i++) {
          ((JCheckBox)fgList.get(i)).setSelected(false);
        }

      }
    });

    jScrollPane1.setBounds(new Rectangle(8, 18, 230, 99));
    JPanel fgPanel = new JPanel();
    this.getContentPane().add(mainPanel);
    displayTypeButtonGroup.add(ascendingRadioButton);
    displayTypeButtonGroup.add(descendingRadioButton);
    mainPanel.add(jPanel1);
    jPanel1.add(featurePropertyLabel);
    jPanel1.add(featurePropertyComboBox);
    mainPanel.add(jPanel4);
    jPanel4.add(allFGButton);
    jPanel4.add(noFGButton);
    jPanel4.add(jScrollPane1);
    jScrollPane1.getViewport().add(fgPanel);
    mainPanel.add(okButton);
    mainPanel.add(cancelButton);
    mainPanel.add(jPanel3);
    jPanel3.add(ascendingRadioButton);
    jPanel3.add(descendingRadioButton);
    mainPanel.add(jPanel5);
    jPanel5.add(ruleNameLabel);
    jPanel5.add(nameTextField);
    featurePropertyComboBox.setSelectedIndex(0);

    fgPanel.setLayout(new BoxLayout(fgPanel,BoxLayout.Y_AXIS));
    Map map=ViewPrefMgr.getViewPrefMgr().getFeatureCollection();
    Set discoveryEnvironments=map.keySet();
    for (Iterator it=discoveryEnvironments.iterator();it.hasNext(); ) {
      Object next=it.next();
      JCheckBox tmpCheck = new JCheckBox(next.toString(), false);
      fgList.add(tmpCheck);
      fgPanel.add(tmpCheck);
    }
    jScrollPane1.getViewport().add(fgPanel);
    this.setSize(373, 454);
    setLocation(10, 10);
    this.setVisible(true);
    this.setResizable(false);
  }

  void okButton_actionPerformed(ActionEvent e) {
    if (featurePropertyComboBox.getSelectedItem()!=null &&
        displayTypeButtonGroup.getSelection()!=null &&
        nameTextField.getText()!=null && !nameTextField.getText().equals("")) {
          for (Iterator it = displayRules.keySet().iterator();it.hasNext();) {
            String keyName = (String)it.next();
            if (keyName.equalsIgnoreCase(nameTextField.getText().trim())) {
              int answer = JOptionPane.showConfirmDialog(parentFrame, "That name already exists.\n"+
              "Do you want to override the original?", "Data Entry Error!", JOptionPane.YES_NO_OPTION);
              if (answer==JOptionPane.NO_OPTION) return;
              else {
                displayRules.remove(keyName);
                break;
              }
            }
          }
          PropertySortInfo newInfo = createNewInfo();
          DisplayFilterMgr.getDisplayFilterMgr().addPropertySortFilter(newInfo);
          this.dispose();
    }
    else JOptionPane.showMessageDialog(parentFrame, "The information entered is incorrect.\n"+
      "Please check all fields and try again.", "Data Entry Error!", JOptionPane.WARNING_MESSAGE);
  }

  public String getRuleName() {
    return nameTextField.getText().trim();
  }

  void cancelButton_actionPerformed(ActionEvent e) {
    this.dispose();
  }


  private PropertySortInfo createNewInfo() {
    int selectedPropertyIndex = featurePropertyComboBox.getSelectedIndex();
    String currentName = nameTextField.getText().trim();
    String currentTargetProperty = (String)suggestedValueTable[selectedPropertyIndex][0];
    int currentDisplayState=0;

    // This is going to be a list of the selected feature group names.
    ArrayList tmpFGList = new ArrayList();
    for (Iterator it = fgList.iterator(); it.hasNext(); ) {
      JCheckBox tmpBox = (JCheckBox)it.next();
      if (tmpBox.isSelected()) {
        tmpFGList.add(tmpBox.getText());
      }
    }

    if (ascendingRadioButton.isSelected()) currentDisplayState=PropertySortInfo.ASCENDING;
    else if (descendingRadioButton.isSelected()) currentDisplayState=PropertySortInfo.DESCENDING;

    return new PropertySortInfo(DisplayFilterMgr.getKeyForName(currentName, true),
      currentName, "Unknown", currentTargetProperty, tmpFGList,
      currentDisplayState);
  }

}