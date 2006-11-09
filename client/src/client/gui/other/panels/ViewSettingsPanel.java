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
import client.gui.framework.view_pref_mgr.ViewPrefMgr;
import client.shared.file_chooser.FileChooser;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

public class ViewSettingsPanel extends JPanel implements PrefEditor {
  // This is the "dirty bit" that something has changed.
  private JFrame parentFrame;
  private final String DEFAULT_PREF_SAVE_FILE = "Main_View.properties";
  private JPanel internalPanel=new JPanel();
  private ArrayList fileList = new ArrayList();
  private String fileSep=File.separator;
  private String userDirectory = new String(System.getProperty("user.home")+fileSep+
         "x"+fileSep+"GenomeBrowser"+fileSep);
  TitledBorder titledBorder1;
  JPanel fileSelectionPanel = new JPanel();
  JLabel mainLabel = new JLabel();
  TitledBorder titledBorder2;
  JButton createNewFileButton = new JButton();
  JButton reloadPrefsButton = new JButton();
  JButton deleteFileButton = new JButton();
  JButton resetToDefaultButton = new JButton();
  JTextArea jTextArea1 = new JTextArea();
  JScrollPane jScrollPane1 = new JScrollPane();
  JLabel activeFileLabel = new JLabel();
  JLabel descriptionLabel = new JLabel();
  JButton changeFileButton = new JButton();

  public ViewSettingsPanel(JFrame parentFrame) {
    this.parentFrame=parentFrame;
    changeFileButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        changeFileButtonActionPerformed(evt);
      }
    });

    reloadPrefsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if (evt.getSource()==reloadPrefsButton)
          ViewPrefMgr.getViewPrefMgr().resetWorkingCollections();
      }
    });

    createNewFileButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        createNewFileButtonActionPerformed(evt);
      }
    });

    deleteFileButton.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent evt) {
        deleteFileButtonActionPerformed(evt);
      }
    });

    try {
      String tmpFile = null;
      if (SessionMgr.getSessionMgr().getModelProperty(ViewPrefMgr.VIEW_PREF_SAVE_FILE_PROPERTY)!=null) {
        tmpFile = ((File)SessionMgr.getSessionMgr().getModelProperty(ViewPrefMgr.VIEW_PREF_SAVE_FILE_PROPERTY)).getName();
      }

      if (tmpFile!=null) activeFileLabel.setText(tmpFile);
      else activeFileLabel.setText(DEFAULT_PREF_SAVE_FILE);
      jbInit();
      jTextArea1.setText(ViewPrefMgr.getViewPrefMgr().getUserFileDescription());
    }
    catch (Exception ex) { SessionMgr.getSessionMgr().handleException(ex); }
  }


  public String getDescription() {
    return "Save and reload different view setting files.";
  }

  private void changeFileButtonActionPerformed(ActionEvent evt) {
    if (evt.getSource()==changeFileButton) {
      JFileChooser fileChooser = new FileChooser(userDirectory);
      fileChooser.setFileSelectionMode(FileChooser.FILES_ONLY);
      fileChooser.setFileFilter(new MyFileFilter());
      fileChooser.setVisible(true);
      int returnVal = fileChooser.showDialog(parentFrame, "Select File");
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        String newFile = fileChooser.getSelectedFile().getName();
        String newAbsolutePath = fileChooser.getSelectedFile().getAbsolutePath();
        activeFileLabel.setText(newFile);
        ViewPrefMgr.getViewPrefMgr().setUserPreferenceFile(newAbsolutePath, jTextArea1.getText().trim());
        jTextArea1.setText(ViewPrefMgr.getViewPrefMgr().getUserFileDescription());
      }
    }
  }

  private void createNewFileButtonActionPerformed(ActionEvent evt) {
    if (evt.getSource()==createNewFileButton) {
      String newAbsolutePath = new String();
      String newFileName = JOptionPane.showInputDialog(parentFrame, "Enter a name for the new file.",
        "Create New View Setting File", JOptionPane.OK_OPTION);
      if (newFileName==null || newFileName.equals("")) return;
      if (!newFileName.endsWith(ViewPrefMgr.VIEW_PREF_FILE_SUFFIX))
        newFileName+=ViewPrefMgr.VIEW_PREF_FILE_SUFFIX;
      if (fileList.contains(newFileName)) {
        JOptionPane.showMessageDialog(parentFrame, "That file name already exists!!!",
          "Invalid Filename", JOptionPane.WARNING_MESSAGE);
        return;
      }
      newAbsolutePath = userDirectory + newFileName;
      try { (new File(newAbsolutePath)).createNewFile(); }
      catch (Exception ex) { SessionMgr.getSessionMgr().handleException(ex); }
      activeFileLabel.setText(newFileName);
      ViewPrefMgr.getViewPrefMgr().setUserPreferenceFile(newAbsolutePath, jTextArea1.getText().trim());
      jTextArea1.setText(ViewPrefMgr.getViewPrefMgr().getUserFileDescription());
    }
  }

  private void deleteFileButtonActionPerformed(ActionEvent evt) {
    if (evt.getSource()==deleteFileButton) {
      JFileChooser fileChooser = new FileChooser(userDirectory);
      fileChooser.setFileSelectionMode(FileChooser.FILES_ONLY);
      fileChooser.setFileFilter(new MyFileFilter());
      fileChooser.setVisible(true);
      int returnVal = fileChooser.showDialog(parentFrame, "Delete File");
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        String deletedFile = fileChooser.getSelectedFile().getName();
        String deletedAbsolutePath = fileChooser.getSelectedFile().getAbsolutePath();
        if (deletedFile.equals(DEFAULT_PREF_SAVE_FILE)) {
          JOptionPane.showMessageDialog(parentFrame, "The Default preference file cannot be deleted!!!",
            "Invalid File", JOptionPane.WARNING_MESSAGE);
          return;
        }
        int deleteValue = JOptionPane.showConfirmDialog(parentFrame, "Delete: "+deletedFile+"\nAre you sure?","Deleting File",
          JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (!deletedFile.endsWith(ViewPrefMgr.VIEW_PREF_FILE_SUFFIX)) {
          JOptionPane.showMessageDialog(parentFrame, "That is not a view property file!!!",
            "Invalid File", JOptionPane.WARNING_MESSAGE);
          return;
        }
        if (deleteValue==JOptionPane.OK_OPTION) {
          activeFileLabel.setText(DEFAULT_PREF_SAVE_FILE);
          ViewPrefMgr.getViewPrefMgr().setUserPreferenceFile(userDirectory+DEFAULT_PREF_SAVE_FILE,
            jTextArea1.getText().trim());
          jTextArea1.setText(ViewPrefMgr.getViewPrefMgr().getUserFileDescription());
          try { (new File(deletedAbsolutePath)).delete(); }
          catch (Exception ex) { SessionMgr.getSessionMgr().handleException(ex); }
        }
      }
    }
  }

  void jbInit() throws Exception {
    titledBorder1 = new TitledBorder("");
    titledBorder2 = new TitledBorder("Options");
    internalPanel.setBorder(new javax.swing.border.TitledBorder(""));
    internalPanel.setPreferredSize(new Dimension(32767, 32767));
    internalPanel.setBounds(new Rectangle(7, 8, 452, 149));
    internalPanel.setLayout(null);
    this.setLayout(null);
    mainLabel.setText("Current View Setting File:");
    mainLabel.setBounds(new Rectangle(13, 16, 166, 17));
    fileSelectionPanel.setLayout(null);
    fileSelectionPanel.setBounds(new Rectangle(7, 169, 452, 163));
    fileSelectionPanel.setPreferredSize(new Dimension(32767, 32767));
    fileSelectionPanel.setBorder(titledBorder2);

    createNewFileButton.setToolTipText("Create a new view settings file.");
    createNewFileButton.setText("New File");
    createNewFileButton.setBounds(new Rectangle(63, 71, 133, 27));
    reloadPrefsButton.setToolTipText("Removes any changes from current session.");
    reloadPrefsButton.setText("Reload");
    reloadPrefsButton.setBounds(new Rectangle(63, 116, 133, 27));
    deleteFileButton.setBounds(new Rectangle(253, 71, 133, 27));
    deleteFileButton.setToolTipText("Delete a User view setting file.");
    deleteFileButton.setText("Delete File");
    resetToDefaultButton.setToolTipText("Reset settings to the Default configuration only.");
    resetToDefaultButton.setText("Reset To Default");
    resetToDefaultButton.setBounds(new Rectangle(253, 116, 133, 27));
    resetToDefaultButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if (evt.getSource()==resetToDefaultButton) {
          int answer = JOptionPane.showConfirmDialog(parentFrame,
          "This action will remove all User-defined colors, features, and tiers.\n"+
          "It will also delete any custom tier orderings or feature-to-tier mappings.\n"+
          "Continue?", "Reset Confirmation",JOptionPane.OK_CANCEL_OPTION);
          if (answer==JOptionPane.OK_OPTION) {
            File tmpFile = new File(userDirectory+DEFAULT_PREF_SAVE_FILE);
              if (tmpFile.exists()) tmpFile.delete();
              ViewPrefMgr.getViewPrefMgr().initializeMasterInfoObjects();
              ViewPrefMgr.getViewPrefMgr().commitChanges(false);
              ViewPrefMgr.getViewPrefMgr().firePreferencesChangedEvent();
            }
          }
        }
      });
    descriptionLabel.setBounds(new Rectangle(13, 46, 116, 17));
    descriptionLabel.setText("Description:");
    jTextArea1.setBorder(titledBorder1);
    jTextArea1.setBounds(new Rectangle(13, 65, 58, 69));
    jTextArea1.setColumns(57);
    jTextArea1.setLineWrap(true);
    jTextArea1.setWrapStyleWord(true);
    jScrollPane1.setBounds(new Rectangle(13, 65, 425, 69));
    jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    activeFileLabel.setBounds(new Rectangle(178, 16, 166, 17));
    changeFileButton.setBounds(new Rectangle(127, 26, 187, 27));
    changeFileButton.setText("Change Active Setting File");
    changeFileButton.setToolTipText("Change the active setting file.");
    this.add(internalPanel);
    internalPanel.add(mainLabel, null);
    internalPanel.add(activeFileLabel, null);
    internalPanel.add(descriptionLabel, null);
    internalPanel.add(jScrollPane1, null);
    jScrollPane1.getViewport().add(jTextArea1, null);
    this.add(fileSelectionPanel, null);
    fileSelectionPanel.add(createNewFileButton, null);
    fileSelectionPanel.add(deleteFileButton, null);
    fileSelectionPanel.add(resetToDefaultButton, null);
    fileSelectionPanel.add(changeFileButton, null);
    fileSelectionPanel.add(reloadPrefsButton, null);
    createNewFileButton.repaint();
    reloadPrefsButton.repaint();
    deleteFileButton.repaint();
    changeFileButton.repaint();
  }

  public String getName() { return "View Preference File"; }

  public String getPanelGroup() {
    return PrefController.GENOMIC_AXIS_ANNOTATION_VIEW_EDITOR;
  }

  public void cancelChanges(){
  }

  public boolean hasChanged() {
    return !jTextArea1.getText().trim().equals(ViewPrefMgr.getViewPrefMgr().getUserFileDescription());
  }

  public String[] applyChanges(){
    if (hasChanged())
      ViewPrefMgr.getViewPrefMgr().setUserFileDescription(jTextArea1.getText().trim());
    return NO_DELAYED_CHANGES;
  }

  public void dispose(){}

  private class MyFileFilter extends javax.swing.filechooser.FileFilter {
    public boolean accept(File pathname) {
      if (pathname.toString().endsWith(ViewPrefMgr.VIEW_PREF_FILE_SUFFIX) ||
          pathname.isDirectory()) return true;
      else return false;
    }

    public String getDescription() { return "View Property Files"; }
  }

}