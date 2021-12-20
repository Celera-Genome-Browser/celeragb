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
package client.gui.framework.property_rules;

import api.entity_model.management.properties.FeaturePropertyCreationRule;
import api.entity_model.management.properties.HTMLPropertyValueFormatter;
import api.entity_model.management.properties.PropertyCreationRule;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.EntityTypeSet;
import api.entity_model.model.fundtype.GenomicEntity;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.framework.view_pref_mgr.ViewPrefMgr;
import client.shared.text_component.StandardTextArea;
import client.shared.text_component.StandardTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.*;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies (peter.davies)
 * @version      $Id$
 *
 * This dialog is for adding a property rule
 */

public class AddPropertyRuleDialog extends JDialog {
  JPanel panel1 = new JPanel();
  JPanel pnlDefineRule = new JPanel();
  JLabel lblDefineRule = new JLabel();
  JLabel lblConditions = new JLabel();
  JPanel lstEntityTypes = new JPanel();
  JLabel lblWhenEntityType = new JLabel();
  JButton btnAddAllEntityTypes = new JButton();
  JButton btnRemoveAllEntityTypes = new JButton();
  JScrollPane jspEntityTypes = new JScrollPane();
  JLabel lblCreateProperty = new JLabel();
  JLabel lblName = new JLabel();
  JTextField txtPropertyName = new StandardTextField();
  BrowserModel browserModel;
  JLabel lblCurrentSelection = new JLabel();
  JPanel pnlCurrentSelection = new JPanel();
  JLabel lblEntityType = new JLabel();
  JLabel lblDiscoveryEnv = new JLabel();
  JLabel lblSelection = new JLabel();
  JPanel pnlEntityType = new JPanel();
  JLabel lblWhenDiscoveryEnv = new JLabel();
  JButton btnRemoveAllDiscoveryEnvs = new JButton();
  JButton btnAddAllDiscoveryEnvs = new JButton();
  JScrollPane jspDiscoveryEnvironments = new JScrollPane();
  JPanel lstDiscoveryEnvironments = new JPanel();
  JLabel lblValue = new JLabel();
  JButton btnInsertProperty = new JButton();
  JTextArea txtExample = new StandardTextArea();
  JButton btnOK = new JButton();
  JButton btnCancel = new JButton();
  JTextArea txtPropertyValue = new StandardTextArea();
  JScrollPane jspExample = new JScrollPane();
  JScrollPane jspPropertyValue = new JScrollPane();
  JLabel Selection = new JLabel();
  JButton btnAddDiscoveryEnv = new JButton();
  java.util.List entityTypeCheckBoxes=new ArrayList();
  java.util.List discoveryEnvCheckBoxes=new ArrayList();
  Map checkBoxToEntityTypes=new HashMap();
  JTextField txtRuleName = new StandardTextField();
  JLabel lblRuleName = new JLabel();
  JLabel lblExample = new JLabel();
  JRadioButton rbAnd = new JRadioButton();
  JRadioButton rbOr = new JRadioButton();
  ButtonGroup btnGrpAndOr = new ButtonGroup();

  public AddPropertyRuleDialog(JDialog parent, BrowserModel model ) {
    super(parent, "Add Property Rule", false);
    try {
      browserModel=model;
      jbInit();
      centerDialog();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  void jbInit() throws Exception {
    panel1.setLayout(null);
    panel1.setPreferredSize(new Dimension(524, 600));

    pnlCurrentSelection.setBorder(BorderFactory.createEtchedBorder());
    pnlCurrentSelection.setBounds(new Rectangle(2, 3, 517, 84));
    pnlCurrentSelection.setLayout(null);
    pnlDefineRule.setBorder(BorderFactory.createEtchedBorder());
    pnlDefineRule.setBounds(new Rectangle(2, 90, 518, 506));
    pnlDefineRule.setLayout(null);
    GenomicEntity currentEntity=browserModel.getCurrentSelection();
    if (currentEntity!=null) {
      lblCurrentSelection.setText("Current Selection: "+currentEntity);
      lblEntityType.setText("Entity Type: "+currentEntity.getEntityType());
    }
    else {
      lblCurrentSelection.setText("Current Selection: ");
      lblEntityType.setText("Entity Type: ");
    }
    lblCurrentSelection.setBounds(new Rectangle(35, 25, 351, 20));
    lblEntityType.setBounds(new Rectangle(72, 43, 314, 20));
    lblDefineRule.setFont(new java.awt.Font("Dialog", 1, 14));
    lblDefineRule.setText("Define Rule");
    lblDefineRule.setBounds(new Rectangle(5, 5, 106, 17));
    lblConditions.setFont(new java.awt.Font("Dialog", 1, 12));
    lblConditions.setText("Conditions");
    lblConditions.setBounds(new Rectangle(24, 59, 82, 17));
    lblWhenEntityType.setText("When Entity Type =");
    lblWhenEntityType.setBounds(new Rectangle(49, 81, 128, 17));
    btnAddAllEntityTypes.setText("Add All");
    btnAddAllEntityTypes.setBounds(new Rectangle(406, 81, 101, 27));
    btnAddAllEntityTypes.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnAddAllEntityTypes_actionPerformed(e);
      }
    });
    btnRemoveAllEntityTypes.setText("Remove All");
    btnRemoveAllEntityTypes.setBounds(new Rectangle(406, 117, 101, 27));
    btnRemoveAllEntityTypes.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnRemoveAllEntityTypes_actionPerformed(e);
      }
    });
    jspEntityTypes.setBorder(BorderFactory.createLoweredBevelBorder());
    jspEntityTypes.setBounds(new Rectangle(185, 79, 208, 225));
    this.setResizable(false);
    this.setModal(true);
    this.setTitle("Add Property Rule");
    lblCreateProperty.setFont(new java.awt.Font("Dialog", 1, 12));
    lblCreateProperty.setText("Create Property");
    lblCreateProperty.setBounds(new Rectangle(24, 304, 110, 17));
    lblName.setText("Name =");
    lblName.setBounds(new Rectangle(34, 339, 45, 17));
    txtPropertyName.setBorder(BorderFactory.createLoweredBevelBorder());
    txtPropertyName.setBounds(new Rectangle(87, 334, 310, 22));
    lblValue.setText("Value = ");
    lblValue.setBounds(new Rectangle(35, 365, 44, 17));
    btnInsertProperty.setToolTipText("Insert Property");
    btnInsertProperty.setText("Insert");
    btnInsertProperty.setBounds(new Rectangle(406, 366, 101, 27));
    btnInsertProperty.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        btnInsertProperty_mousePressed(e);
      }
    });
    txtExample.setMinimumSize(new Dimension(1, 1));
    txtPropertyValue.setWrapStyleWord(true);
    txtPropertyValue.setLineWrap(true);
    btnAddDiscoveryEnv.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        btnAddDiscoveryEnv_mouseClicked(e);
      }
    });
    jspPropertyValue.getViewport().add(txtPropertyValue);
    jspExample.getViewport().add(txtExample);
    jspExample.setBorder(BorderFactory.createLoweredBevelBorder());
    txtExample.setLineWrap(true);
    txtExample.setWrapStyleWord(true);
    txtExample.setText(HTMLPropertyValueFormatter.getExampleString());
    txtExample.setBackground(lblExample.getBackground());
    txtExample.setEditable(false);
    txtExample.setCaretPosition(0);
//    scrollRectToVisible(new Rectangle(new Point(0,0)));
//    txtExample.setVerticalAlignment(SwingConstants.TOP);
    jspExample.setBounds(new Rectangle(87, 422, 311, 45));
    btnOK.setText("OK");
    btnOK.setBounds(new Rectangle(153, 473, 79, 27));
    btnOK.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnOK_actionPerformed(e);
      }
    });
    btnCancel.setText("Cancel");
    btnCancel.setBounds(new Rectangle(287, 473, 79, 27));
    btnCancel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnCancel_actionPerformed(e);
      }
    });
    jspPropertyValue.setBorder(BorderFactory.createLoweredBevelBorder());
    jspPropertyValue.setBounds(new Rectangle(87, 365, 311, 47));
//    lstDiscoveryEnvironments.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
//      public void valueChanged(ListSelectionEvent e) {
//        lstDiscoveryEnvironments_valueChanged(e);
//      }
//    });
    Selection.setFont(new java.awt.Font("Dialog", 1, 14));
    Selection.setText("Selection");
    Selection.setBounds(new Rectangle(6, 8, 70, 17));
    if (currentEntity instanceof Feature) pnlCurrentSelection.add(lblDiscoveryEnv, null);
    EntityType[] entityTypes= EntityType.allEntityTypes();
    EntityType currentEntityType=null;
    if (currentEntity!=null) currentEntityType=currentEntity.getEntityType();
    JCheckBox checkBox;
    lstEntityTypes.setLayout(new BoxLayout(lstEntityTypes,BoxLayout.Y_AXIS));
    Arrays.sort(entityTypes,new Comparator() {
      public int compare(Object o1, Object o2){
        return o1.toString().compareToIgnoreCase(o2.toString());
      }
    });
    boolean selected=false;
      for (EntityType entityType : entityTypes) {
          selected = (currentEntityType != null && currentEntityType.equals(entityType));
          checkBox = new JCheckBox(entityType.toString(), selected);
          entityTypeCheckBoxes.add(checkBox);
          checkBoxToEntityTypes.put(checkBox, entityType);
          lstEntityTypes.add(checkBox);
      }
    if (currentEntity==null || currentEntity instanceof Feature) {
        rbAnd.setSelected(true);
        rbAnd.setText("And");
        rbAnd.setBounds(new Rectangle(73, 157, 45, 25));
        rbOr.setText("Or");
        rbOr.setBounds(new Rectangle(73, 179, 52, 25));
        pnlDefineRule.add(rbAnd, null);
        pnlDefineRule.add(rbOr, null);
        btnGrpAndOr.add(rbAnd);
        btnGrpAndOr.add(rbOr);
        Map map=ViewPrefMgr.getViewPrefMgr().getFeatureCollection();
        Set discoveryEnvironments=map.keySet();
        String currentDiscoveryEnvironment=null;
        lstDiscoveryEnvironments.setLayout(new BoxLayout(lstDiscoveryEnvironments,BoxLayout.Y_AXIS));
        if (currentEntity!=null) currentDiscoveryEnvironment=((Feature)currentEntity).getEnvironment();
        Object next;
        for (Object discoveryEnvironment : discoveryEnvironments) {
            next = discoveryEnvironment;
            checkBox = new JCheckBox(next.toString(),
                    (currentDiscoveryEnvironment != null && currentDiscoveryEnvironment.equals(next.toString())));
            discoveryEnvCheckBoxes.add(checkBox);
            lstDiscoveryEnvironments.add(checkBox);
        }
        jspEntityTypes.setBounds(new Rectangle(185, 79, 208, 122));
        jspDiscoveryEnvironments.setBorder(BorderFactory.createLoweredBevelBorder());
        if (currentEntity!=null) {
          lblDiscoveryEnv.setText("Discovery Environment: "+((Feature)currentEntity).getEnvironment());
        }
        else {
          lblDiscoveryEnv.setText("Discovery Environment: ");
        }
        lblDiscoveryEnv.setBounds(new Rectangle(5, 61, 385, 20));
        jspDiscoveryEnvironments.setBounds(new Rectangle(185, 210, 208, 101));
        btnAddAllDiscoveryEnvs.setText("Add All");
        btnAddAllDiscoveryEnvs.setBounds(new Rectangle(406, 210, 101, 27));
        btnAddAllDiscoveryEnvs.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            btnAddAllDiscoveryEnvs_actionPerformed(e);
          }
        });
        btnRemoveAllDiscoveryEnvs.setText("Remove All");
        btnRemoveAllDiscoveryEnvs.setBounds(new Rectangle(406, 249, 101, 27));
        btnRemoveAllDiscoveryEnvs.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            btnRemoveAllDiscoveryEnvs_actionPerformed(e);
          }
        });
        lblWhenDiscoveryEnv.setText("When Discovery Env =");
        lblWhenDiscoveryEnv.setBounds(new Rectangle(32, 211, 126, 17));
        btnAddDiscoveryEnv.setText("Add");
        btnAddDiscoveryEnv.setBounds(new Rectangle(409, 286, 96, 27));
        jspDiscoveryEnvironments.getViewport().add(lstDiscoveryEnvironments, null);
    }
    txtRuleName.setBounds(new Rectangle(185, 30, 209, 20));
    lblRuleName.setText("Rule Name =");
    lblRuleName.setBounds(new Rectangle(78, 31, 101, 17));
    lblExample.setText("Example:");
    lblExample.setBounds(new Rectangle(25, 422, 54, 17));
    this.getContentPane().add(panel1, BorderLayout.NORTH);
    panel1.add(pnlCurrentSelection, null);
    pnlCurrentSelection.add(lblEntityType, null);
    pnlCurrentSelection.add(Selection, null);
    pnlCurrentSelection.add(lblCurrentSelection, null);
    panel1.add(pnlDefineRule, null);
    pnlDefineRule.add(lblDefineRule, null);
    pnlDefineRule.add(jspDiscoveryEnvironments, null);
    if (currentEntity!=null) pnlDefineRule.add(btnInsertProperty, null);
    pnlDefineRule.add(lblCreateProperty, null);
    pnlDefineRule.add(lblName, null);
    pnlDefineRule.add(lblValue, null);
    pnlDefineRule.add(txtPropertyName, null);
    pnlDefineRule.add(jspPropertyValue, null);
    pnlDefineRule.add(txtRuleName, null);
    pnlDefineRule.add(lblRuleName, null);
    pnlDefineRule.add(jspEntityTypes, null);
    pnlDefineRule.add(lblWhenEntityType, null);
    pnlDefineRule.add(btnRemoveAllEntityTypes, null);
    pnlDefineRule.add(lblConditions, null);
    pnlDefineRule.add(btnRemoveAllDiscoveryEnvs, null);
    pnlDefineRule.add(btnAddAllDiscoveryEnvs, null);
    pnlDefineRule.add(btnAddDiscoveryEnv, null);
    pnlDefineRule.add(btnOK, null);
    pnlDefineRule.add(btnCancel, null);
    pnlDefineRule.add(lblWhenDiscoveryEnv, null);
    pnlDefineRule.add(jspExample, null);
    pnlDefineRule.add(lblExample, null);
    pnlDefineRule.add(btnAddAllEntityTypes, null);
    jspEntityTypes.getViewport().add(lstEntityTypes, null);
  }


  /**
   * Helps to ensure good window placement.
   */
  private void centerDialog() {
    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int screenWidth = screenSize.width;
    int screenHeight = screenSize.height;
    pack();
    Dimension frameSize = getSize();
    if (frameSize.height > screenHeight) {
      frameSize.height = screenHeight;
    } // Adjust for screen height.

    if (frameSize.width > screenWidth) {
      frameSize.width = screenWidth;
    } // Adjust for screen width.
    setLocation((screenWidth - frameSize.width)/2,
                         (screenHeight - frameSize.height)/2);
  }


  void btnAddAllEntityTypes_actionPerformed(ActionEvent e) {
    Component[] components=lstEntityTypes.getComponents();
      for (Component component1 : components) {
          if (component1 instanceof JCheckBox) ((JCheckBox) component1).setSelected(true);
      }
  }

  void btnRemoveAllEntityTypes_actionPerformed(ActionEvent e) {
    Component[] components=lstEntityTypes.getComponents();
      for (Component component1 : components) {
          if (component1 instanceof JCheckBox) ((JCheckBox) component1).setSelected(false);
      }
  }

  void btnRemoveAllDiscoveryEnvs_actionPerformed(ActionEvent e) {
    Component[] components=lstDiscoveryEnvironments.getComponents();
      for (Component component1 : components) {
          if (component1 instanceof JCheckBox) ((JCheckBox) component1).setSelected(false);
      }
  }

  void btnAddAllDiscoveryEnvs_actionPerformed(ActionEvent e) {
    Component[] components=lstDiscoveryEnvironments.getComponents();
      for (Component component1 : components) {
          if (component1 instanceof JCheckBox) ((JCheckBox) component1).setSelected(true);
      }
  }

  void btnOK_actionPerformed(ActionEvent e) {
    if (txtPropertyName.getText().equals("")  || txtPropertyName.getText()==null) {
      JOptionPane.showMessageDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
      "You must name the property.", "Rule Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    Set rules=SessionMgr.getSessionMgr().getPropertyCreationRules();
      for (Object rule : rules) {
          if (((PropertyCreationRule) rule).getName().equals(txtRuleName.getText())) {
              int ans = JOptionPane.showConfirmDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
                      "The choosen rule name exists.  Choose OK to overwrite the old" +
                              " rule or Cancel to change this rule's name", "Duplicate Rule Name", JOptionPane.OK_CANCEL_OPTION);
              if (ans == JOptionPane.CANCEL_OPTION) return;
          }
      }
    EntityTypeSet selectedEntityTypes=new EntityTypeSet();
    JCheckBox checkBox;
      for (Object entityTypeCheckBoxe : entityTypeCheckBoxes) {
          checkBox = (JCheckBox) entityTypeCheckBoxe;
          if (checkBox.isSelected()) {
              selectedEntityTypes.add(checkBoxToEntityTypes.get(checkBox));
          }
      }
    java.util.List selectedDiscoveryEnvs=new ArrayList();
      for (Object discoveryEnvCheckBoxe : discoveryEnvCheckBoxes) {
          checkBox = (JCheckBox) discoveryEnvCheckBoxe;
          if (checkBox.isSelected()) {
              selectedDiscoveryEnvs.add(checkBox.getText());
          }
      }
    PropertyCreationRule newRule;
    if (selectedDiscoveryEnvs.isEmpty()) {
      newRule=new PropertyCreationRule(txtRuleName.getText(),selectedEntityTypes,txtPropertyName.getText(),
        new HTMLPropertyValueFormatter(txtPropertyValue.getText()));
    }
    else {
      String[] discEnvs=(String[])selectedDiscoveryEnvs.toArray(new String[selectedDiscoveryEnvs.size()]);
      newRule=new FeaturePropertyCreationRule(txtRuleName.getText(),selectedEntityTypes,discEnvs,
        txtPropertyName.getText(), new HTMLPropertyValueFormatter(txtPropertyValue.getText()),rbAnd.isSelected());
    }
    SessionMgr.getSessionMgr().addPropertyCreationRule(newRule);
    dispose();
    setVisible(false);
  }

  void btnCancel_actionPerformed(ActionEvent e) {
    dispose();
    setVisible(false);
  }

  void btnInsertProperty_mousePressed(MouseEvent e) {
     PropertySelector selector=new PropertySelector(SessionMgr.getSessionMgr().getActiveBrowser(),
       new Point(getLocationOnScreen().x+getSize().width,e.getComponent().getLocationOnScreen().y),browserModel);
     selector.setVisible(true);
     if (selector.wasOKPressed()) {
       txtPropertyValue.append("<"+selector.getSelectedPropertyName()+">");
     }
  }

  void btnAddDiscoveryEnv_mouseClicked(MouseEvent e) {
     DiscoveryEnvironment env=new DiscoveryEnvironment(SessionMgr.getSessionMgr().getActiveBrowser(),
       new Point(getLocationOnScreen().x+getSize().width,e.getComponent().getLocationOnScreen().y));
     env.setVisible(true);
     if (env.wasOKPressed()) {
       JCheckBox checkBox=new JCheckBox(env.getSelectedName(),true);
       lstDiscoveryEnvironments.add(checkBox,0);
       lstDiscoveryEnvironments.updateUI();
     }
  }

  public void show() {
    validateTree();
    JCheckBox checkBox;
      for (Object entityTypeCheckBoxe : entityTypeCheckBoxes) {
          checkBox = (JCheckBox) entityTypeCheckBoxe;
          if (checkBox.isSelected()) {
              lstEntityTypes.scrollRectToVisible(checkBox.getBounds());
              break;
          }
      }
      for (Object discoveryEnvCheckBoxe : discoveryEnvCheckBoxes) {
          checkBox = (JCheckBox) discoveryEnvCheckBoxe;
          if (checkBox.isSelected()) {
              lstDiscoveryEnvironments.scrollRectToVisible(checkBox.getBounds());
              break;
          }
      }
    super.setVisible(true);

 }

}