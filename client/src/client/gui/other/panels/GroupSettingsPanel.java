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
import client.shared.file_chooser.FileChooser;
import shared.preferences.PreferenceManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

public class GroupSettingsPanel extends JPanel implements PrefEditor {
  // This is the "dirty bit" that something has changed.
  private boolean groupDirChange=false;
  private boolean fileChange=false;
  private JFrame parentFrame;
  private JPanel internalPanel=new JPanel();
  private ArrayList fileList = new ArrayList();
  private String fileSep=File.separator;
  private JLabel mainLabel1 = new JLabel("There are three levels of view preferences: Default, Group, User");
  private String userDirectory = new String(System.getProperty("user.home")+fileSep+
         "x"+fileSep+"GenomeBrowser"+fileSep);
  private String groupDirectory=new String("");
  JTextField groupTextField = new JTextField(30);
  JButton groupButton = new JButton();
  JCheckBox groupCheckBox = new JCheckBox("Use Group Preference Files");
  TitledBorder titledBorder1;
  TitledBorder titledBorder2;
  JLabel jLabel1 = new JLabel();
  JLabel jLabel2 = new JLabel();

  public GroupSettingsPanel(JFrame parentFrame) {
    this.parentFrame = parentFrame;
    groupCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent ev) {
        Object src = ev.getSource();
        if (src == groupCheckBox) {
          boolean isSelected = groupCheckBox.isSelected();
          if (!isSelected) {
            groupTextField.setText("");
          }
          groupButton.setEnabled(isSelected);
          groupTextField.setEditable(isSelected);
          groupTextField.setEnabled(isSelected);
          internalPanel.repaint();
          groupDirChange=true;
        }
      }
    });

    groupButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        groupButtonActionPerformed(ev);
      }
    });

    String tmpDir =
      (String)SessionMgr.getSessionMgr().getModelProperty(PreferenceManager.GROUP_DIR);
    boolean groupDirUsed = false;
    if (tmpDir==null) {
      tmpDir = "";
      SessionMgr.getSessionMgr().setModelProperty(PreferenceManager.GROUP_DIR, tmpDir);
    }
    if (!tmpDir.equals("")) groupDirUsed = true;
    groupCheckBox.setSelected(groupDirUsed);
    groupButton.setEnabled(groupDirUsed);
    groupTextField.setEnabled(groupDirUsed);
    groupTextField.setEditable(groupDirUsed);

    groupDirectory=(String)SessionMgr.getSessionMgr().getModelProperty(PreferenceManager.GROUP_DIR);
    if (groupDirectory==null) groupDirectory="";
    try {
      jbInit();
    }
    catch (Exception ex) {
      SessionMgr.getSessionMgr().handleException(ex);
    }
  }


  public String getDescription() {
    return "Set the location of the User and Group preference files";
  }


  private void groupButtonActionPerformed(ActionEvent ev) {
    Object src = ev.getSource();
    if (src==groupButton) {
      JFileChooser groupChooser = new FileChooser(groupTextField.getText());
      groupChooser.setVisible(true);
      groupChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int returnVal = groupChooser.showOpenDialog(parentFrame);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File directory = groupChooser.getSelectedFile();
        groupTextField.setText(directory.getAbsolutePath());
        groupDirChange=true;
      }
    }
  }

  public String getPanelGroup() {
    return PrefController.SYSTEM_EDITOR;
  }


  void jbInit() throws Exception {
    titledBorder1 = new TitledBorder("");
    titledBorder2 = new TitledBorder("Save Location");
    groupCheckBox.setBorder(null);
    groupCheckBox.setBounds(new Rectangle(11, 106, 178, 25));
    groupButton.setBounds(new Rectangle(10, 134, 167, 23));
    groupButton.setText("Choose Group Directory");
    groupTextField.setBounds(new Rectangle(187, 134, 231, 23));
    groupTextField.setText(groupDirectory);
    internalPanel.setBorder(new javax.swing.border.TitledBorder("Preference Locations"));
    internalPanel.setPreferredSize(new Dimension(32767, 32767));
    internalPanel.setBounds(new Rectangle(5, 8, 442, 170));
    internalPanel.setLayout(null);
    this.setLayout(null);
    mainLabel1.setToolTipText("");
    mainLabel1.setText("There are three levels of preferences: Default, Group, User");
    mainLabel1.setBounds(new Rectangle(11, 27, 426, 17));
    jLabel1.setText("To receive preferences designed for Group users check below");
    jLabel1.setBounds(new Rectangle(11, 46, 421, 20));
    jLabel2.setText("and choose the proper directory.");
    jLabel2.setBounds(new Rectangle(11, 67, 364, 21));
    this.add(internalPanel);
    internalPanel.add(jLabel1, null);
    internalPanel.add(mainLabel1, null);
    internalPanel.add(jLabel2, null);
    internalPanel.add(groupTextField, null);
    internalPanel.add(groupCheckBox, null);
    internalPanel.add(groupButton, null);
    groupButton.repaint();
  }

  public String getName() { return "Group Preference Settings"; }

  public void cancelChanges(){
    groupDirChange=false;
    fileChange=false;
  }

  public boolean hasChanged() { return (groupDirChange || fileChange); }

  public String[] applyChanges(){
    if (groupDirChange) {
      groupDirChange=false;
      SessionMgr.getSessionMgr().setModelProperty(PreferenceManager.GROUP_DIR,
        groupTextField.getText());
    }
    return NO_DELAYED_CHANGES;
  }

  public void dispose(){}

}