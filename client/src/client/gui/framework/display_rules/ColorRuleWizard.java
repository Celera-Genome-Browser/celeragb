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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;


public class ColorRuleWizard extends JDialog {
  private TreeMap displayRules = new TreeMap();
  private JFrame parentFrame;
  private Object [][] suggestedValueTable = ColorIntensityInfo.suggestedValueTable;
  JPanel mainPanel = new JPanel();
  JRadioButton stepColorRadioButton = new JRadioButton();
  JRadioButton logRadioButton = new JRadioButton();
  JRadioButton interpolateRadioButton = new JRadioButton();

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
  JLabel maxPointLabel = new JLabel();
  JTextField maxPointTextField = new StandardTextField();
  JLabel minPointLabel = new JLabel();
  JTextField minPointTextField = new StandardTextField();
  JLabel ruleNameLabel = new JLabel();
  JTextField nameTextField = new StandardTextField();
  JPanel jPanel1 = new JPanel();
  TitledBorder titledBorder4;
  JPanel jPanel2 = new JPanel();
  TitledBorder titledBorder6;
  JPanel jPanel3 = new JPanel();
  TitledBorder titledBorder7;
  JPanel jPanel4 = new JPanel();
  JPanel jPanel5 = new JPanel();
  TitledBorder titledBorder8;
  TitledBorder titledBorder9;
  TitledBorder titledBorder10;
  TitledBorder titledBorder11;
  TitledBorder titledBorder12;
  JButton allFGButton = new JButton();
  JButton noFGButton = new JButton();
  JScrollPane jScrollPane1 = new JScrollPane();
  // This is a list of the check boxes.
  ArrayList fgList = new ArrayList();

  public ColorRuleWizard(JFrame parentFrame) {
    super(parentFrame, "Color Rule Settings", true);
    this.parentFrame=parentFrame;
    displayRules = (TreeMap)DisplayFilterMgr.getDisplayFilterMgr().getColorIntensityFilters();
    suggestedValueTable=ColorIntensityInfo.getPropertyInformation();
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    titledBorder1 = new TitledBorder("Values");
    titledBorder2 = new TitledBorder("Create A New Color Rule");
    titledBorder3 = new TitledBorder("Display Settings");
    titledBorder5 = new TitledBorder("Display Rule");
    titledBorder4 = new TitledBorder("1. Select Property To Trigger From");
    titledBorder6 = new TitledBorder("5. Enter Rule Name");
    titledBorder7 = new TitledBorder("3. Choose Calculation Type");
    titledBorder8 = new TitledBorder("4. Enter Range Information");
    titledBorder11 = new TitledBorder("2. Select Affected Feature Groups");
    titledBorder10 = new TitledBorder("6. Enter Rule Name");
    titledBorder12 = new TitledBorder("5. Enter The Rule Name");
    this.getContentPane().setLayout(null);
    for (int x=0; x<suggestedValueTable.length; x++) {
      fgModel.addElement(PropertyMgr.getPropertyMgr().getPropertyDisplayName(
        (String)suggestedValueTable[x][0]));
    }
    featurePropertyComboBox.setMinimumSize(new Dimension(182, 21));
    featurePropertyComboBox.setPreferredSize(new Dimension(182, 21));
    featurePropertyComboBox.setModel(fgModel);
    featurePropertyComboBox.setBounds(new Rectangle(135, 34, 182, 21));
    featurePropertyComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        int propertyValue = featurePropertyComboBox.getSelectedIndex();
        DecimalFormat dFormat = (DecimalFormat) DecimalFormat.getInstance();
        dFormat.applyPattern("0.##E0");
        minPointTextField.setText(dFormat.format((Double)suggestedValueTable[propertyValue][1]));
        maxPointTextField.setText(dFormat.format((Double)suggestedValueTable[propertyValue][2]));
      }
    });

    featurePropertyComboBox.setSize(182, 21);
    interpolateRadioButton.setBorder(BorderFactory.createLineBorder(Color.black));
    interpolateRadioButton.setText("Interpolate");
    interpolateRadioButton.setBounds(new Rectangle(113, 30, 95, 19));
    interpolateRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        enableTextFields(true);
      }
    });
    logRadioButton.setBorder(BorderFactory.createLineBorder(Color.black));
    logRadioButton.setText("Log Scale");
    logRadioButton.setBounds(new Rectangle(225, 30, 90, 19));
    logRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        enableTextFields(true);
      }
    });
    //Create the scroll pane and add the table to it.

    //Add the scroll pane to this window.
    mainPanel.setBorder(titledBorder2);
    mainPanel.setBounds(new Rectangle(5, 5, 356, 525));
    mainPanel.setLayout(null);
    featurePropertyLabel.setText("Feature Property:");
    featurePropertyLabel.setBounds(new Rectangle(19, 33, 108, 17));
    stepColorRadioButton.setText("Step");
    stepColorRadioButton.setBounds(new Rectangle(24, 30, 71, 19));
    stepColorRadioButton.setBorder(BorderFactory.createLineBorder(Color.black));
    stepColorRadioButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        enableTextFields(false);
      }
    });
    stepColorRadioButton.setSelected(true);
    minPointLabel.setEnabled(false);
    minPointTextField.setEnabled(false);
    maxPointLabel.setText("Max Intensity At Value:");
    maxPointLabel.setBounds(new Rectangle(14, 25, 131, 24));
    maxPointTextField.setBounds(new Rectangle(147, 27, 59, 20));
    minPointLabel.setText("Min Intensity At Value:");
    minPointLabel.setBounds(new Rectangle(14, 58, 132, 24));
    minPointTextField.setBounds(new Rectangle(147, 60, 59, 20));
    ruleNameLabel.setText("Name:");
    ruleNameLabel.setBounds(new Rectangle(31, 24, 102, 24));
    nameTextField.setBounds(new Rectangle(131, 26, 164, 20));
    jPanel1.setBorder(titledBorder4);
    jPanel1.setBounds(new Rectangle(13, 33, 329, 70));
    jPanel1.setLayout(null);
    jPanel2.setBorder(titledBorder8);
    jPanel2.setBounds(new Rectangle(13, 316, 329, 94));
    jPanel2.setLayout(null);
    jPanel3.setBorder(titledBorder7);
    jPanel3.setBounds(new Rectangle(13, 242, 329, 66));
    jPanel3.setLayout(null);
    jPanel4.setLayout(null);
    jPanel4.setBorder(titledBorder11);
    jPanel4.setBounds(new Rectangle(13, 113, 329, 124));
    jPanel5.setBorder(titledBorder12);
    jPanel5.setBounds(new Rectangle(13, 419, 329, 60));
    jPanel5.setLayout(null);
    okButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okButton_actionPerformed(e);
      }
    });
    okButton.setBounds(new Rectangle(72, 487, 83, 27));
    okButton.setText("OK");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButton_actionPerformed(e);
      }
    });
    cancelButton.setBounds(new Rectangle(190, 487, 83, 27));
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
    this.getContentPane().add(mainPanel, null);
    displayTypeButtonGroup.add(stepColorRadioButton);
    displayTypeButtonGroup.add(interpolateRadioButton);
    displayTypeButtonGroup.add(logRadioButton);
    mainPanel.add(jPanel1, null);
    jPanel1.add(featurePropertyLabel, null);
    jPanel1.add(featurePropertyComboBox, null);
    mainPanel.add(jPanel4, null);
    jPanel4.add(allFGButton, null);
    jPanel4.add(noFGButton, null);
    jPanel4.add(jScrollPane1, null);
    jScrollPane1.getViewport().add(fgPanel, null);
    mainPanel.add(okButton, null);
    mainPanel.add(cancelButton, null);
    mainPanel.add(jPanel3, null);
    jPanel3.add(logRadioButton, null);
    jPanel3.add(stepColorRadioButton, null);
    jPanel3.add(interpolateRadioButton, null);
    mainPanel.add(jPanel5, null);
    jPanel5.add(ruleNameLabel, null);
    jPanel5.add(nameTextField, null);
    mainPanel.add(jPanel2, null);
    jPanel2.add(maxPointLabel, null);
    jPanel2.add(minPointLabel, null);
    jPanel2.add(minPointTextField, null);
    jPanel2.add(maxPointTextField, null);
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
    jScrollPane1.getViewport().add(fgPanel, null);
    this.setSize(373, 564);
    setLocation(10, 10);
    this.setVisible(true);
    this.setResizable(false);
  }

  private void enableTextFields(boolean isStepType) {
    minPointLabel.setEnabled(isStepType);
    minPointTextField.setEnabled(isStepType);
  }

  void okButton_actionPerformed(ActionEvent e) {
    if (featurePropertyComboBox.getSelectedItem()!=null &&
        displayTypeButtonGroup.getSelection()!=null &&
        minPointTextField.getText()!=null && !minPointTextField.getText().equals("") &&
        maxPointTextField.getText()!=null && !maxPointTextField.getText().equals("") &&
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
          ColorIntensityInfo newInfo = createNewInfo();
          DisplayFilterMgr.getDisplayFilterMgr().addColorIntensityFilter(newInfo);
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


  private ColorIntensityInfo createNewInfo() {
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

    if (stepColorRadioButton.isSelected()) currentDisplayState=ColorIntensityInfo.STEP;
    else if (interpolateRadioButton.isSelected()) currentDisplayState=ColorIntensityInfo.INTERPOLATION;
    else if (logRadioButton.isSelected()) currentDisplayState=ColorIntensityInfo.LOG;

    double maxIntensityValue = Double.parseDouble(maxPointTextField.getText().trim());
    double minIntensityValue = Double.parseDouble(minPointTextField.getText().trim());

    return new ColorIntensityInfo(DisplayFilterMgr.getKeyForName(currentName, true),
      currentName, "Unknown", currentTargetProperty, tmpFGList,
      currentDisplayState, (long)maxIntensityValue, (long)minIntensityValue);
  }

}