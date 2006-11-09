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

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import client.gui.framework.session_mgr.SessionMgr;
import client.gui.framework.pref_controller.PrefController;
import client.gui.framework.roles.PrefEditor;
import client.shared.file_chooser.*;

public class SequenceAnalysisResultsPanel extends JPanel implements PrefEditor{
  private static final String   DISABLE_CHUNK_WARNING   = "SequenceAnalysisDisableChunkWarning";
  private static final String   CACHE_SEQUENCE_ANALYSIS = "CacheSequenceAnalysis";
  private static final String   CACHE_DIRECTORY         = "SequenceAnalysisCacheDirectory";
  private boolean settingsChanged=false;
  JPanel mainPanel = new JPanel();
  Border border1;
  TitledBorder titledBorder1;
  JCheckBox chunkSizeWarningCheckBox = new JCheckBox();
  JPanel analysisSavePanel = new JPanel();
  Border border2;
  TitledBorder titledBorder2;
  JCheckBox cacheAnalysisCheckBox = new JCheckBox();
  JButton cacheDirButton = new JButton();
  JLabel dirLabel = new JLabel();
  JTextField cacheDirectoryTextField = new JTextField();
  JButton clearCacheButton = new JButton();
  private String defaultDir = new String(System.getProperty("user.home")+File.separator+
         "x"+File.separator+"GenomeBrowser"+File.separator+"AnalysisCache");

  public SequenceAnalysisResultsPanel(JFrame parentFrame) {
    File tmpFile = new File(defaultDir);
    if (!tmpFile.exists()) tmpFile.mkdir();
    // Check for Preferences
    if (SessionMgr.getSessionMgr().getModelProperty(DISABLE_CHUNK_WARNING)==null) {
      SessionMgr.getSessionMgr().setModelProperty(DISABLE_CHUNK_WARNING, Boolean.FALSE);
    }
    if (SessionMgr.getSessionMgr().getModelProperty(CACHE_SEQUENCE_ANALYSIS)==null) {
      SessionMgr.getSessionMgr().setModelProperty(CACHE_SEQUENCE_ANALYSIS, Boolean.FALSE);
    }
    if (SessionMgr.getSessionMgr().getModelProperty(CACHE_DIRECTORY)==null) {
      SessionMgr.getSessionMgr().setModelProperty(CACHE_DIRECTORY, defaultDir);
    }
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  public String getPanelGroup() { return PrefController.SUB_VIEW_EDITOR; }
  public String getName() { return "Edit Sequence Analysis Results View Settings"; }

  public void cancelChanges(){}
  public String[] applyChanges(){
    if (settingsChanged) {
      SessionMgr.getSessionMgr().setModelProperty(DISABLE_CHUNK_WARNING,
        new Boolean(chunkSizeWarningCheckBox.isSelected()));
      SessionMgr.getSessionMgr().setModelProperty(CACHE_SEQUENCE_ANALYSIS,
        new Boolean(cacheAnalysisCheckBox.isSelected()));
      SessionMgr.getSessionMgr().setModelProperty(CACHE_DIRECTORY,
        cacheDirectoryTextField.getText().trim());
    }
    return NO_DELAYED_CHANGES;
  }

  public boolean hasChanged() { return settingsChanged; }
  public String getDescription() { return "Modify attributes of the Sequence Analysis Results view."; }
  public void dispose(){}

  private void jbInit() throws Exception {
    border2 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(149, 142, 130));
    titledBorder2 = new TitledBorder(border2,"Sequence Analysis Results");
    chunkSizeWarningCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        settingsChanged = true;
      }
    });
    cacheAnalysisCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        settingsChanged = true;
        establishComponentStates();
      }
    });
    Boolean warningBoolean = (Boolean)SessionMgr.getSessionMgr().getModelProperty(DISABLE_CHUNK_WARNING);
    chunkSizeWarningCheckBox.setSelected(warningBoolean.booleanValue());
    Boolean cacheBoolean = (Boolean)SessionMgr.getSessionMgr().getModelProperty(CACHE_SEQUENCE_ANALYSIS);
    cacheAnalysisCheckBox.setSelected(cacheBoolean.booleanValue());
    String tmpDir = (String)SessionMgr.getSessionMgr().getModelProperty(CACHE_DIRECTORY);
    if (tmpDir!=null) cacheDirectoryTextField.setText(tmpDir);
    border1 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(148, 145, 140));
    titledBorder1 = new TitledBorder(border1,"Search Notification");
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    mainPanel.setBorder(titledBorder1);
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
    chunkSizeWarningCheckBox.setText("Disable Search Magnitiude Warning");
    chunkSizeWarningCheckBox.setBounds(new Rectangle(17, 27, 280, 25));
    analysisSavePanel.setBorder(titledBorder2);
    analysisSavePanel.setLayout(new BoxLayout(analysisSavePanel, BoxLayout.Y_AXIS));
    cacheAnalysisCheckBox.setText("Cache Analysis Results");
    cacheAnalysisCheckBox.setBounds(new Rectangle(18, 26, 304, 25));
    cacheDirButton.setText("Choose Cache Directory");
    cacheDirButton.setBounds(new Rectangle(49, 99, 198, 31));
    cacheDirButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        FileChooser chooser = new FileChooser(cacheDirectoryTextField.getText().trim());
        chooser.setFileSelectionMode(FileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose Analysis Cache Directory");
        int ans = chooser.showDialog(SequenceAnalysisResultsPanel.this, "Ok");
        if (ans != FileChooser.CANCEL_OPTION) {
          cacheDirectoryTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
      }
    });
    dirLabel.setToolTipText("");
    dirLabel.setText("Directory:  ");
    dirLabel.setBounds(new Rectangle(18, 60, 73, 24));
    cacheDirectoryTextField.setMaximumSize(new Dimension(352, 20));
    clearCacheButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        int ans = JOptionPane.showConfirmDialog(SequenceAnalysisResultsPanel.this,
          "This will remove all GBF's in the cache directory.\nAre you sure?",
          "Removing Sequence Analysis Cache", JOptionPane.OK_CANCEL_OPTION);
        if (ans == JOptionPane.OK_OPTION) {
          File tmpDir = new File(cacheDirectoryTextField.getText().trim());
          File[] tmpFiles = tmpDir.listFiles();
          for (int i = 0; i < tmpFiles.length; i++) {
            File deleteFile = tmpFiles[i];
            if (deleteFile.toString().endsWith(".gbf")) deleteFile.delete();
          }
        }
      }
    });
    clearCacheButton.setBounds(new Rectangle(273, 99, 128, 31));
    clearCacheButton.setText("Clear Cache");
    mainPanel.add(Box.createHorizontalStrut(5));
    mainPanel.add(chunkSizeWarningCheckBox);
    mainPanel.add(Box.createHorizontalGlue());

    JPanel cachePanel = new JPanel();
    cachePanel.setLayout(new BoxLayout(cachePanel, BoxLayout.X_AXIS));
    cachePanel.add(Box.createHorizontalStrut(5));
    cachePanel.add(cacheAnalysisCheckBox);
    cachePanel.add(Box.createHorizontalGlue());
    analysisSavePanel.add(cachePanel);
    analysisSavePanel.add(Box.createVerticalStrut(10));

    JPanel dirPanel = new JPanel();
    dirPanel.setLayout(new BoxLayout(dirPanel, BoxLayout.X_AXIS));
    dirPanel.add(Box.createHorizontalStrut(5));
    dirPanel.add(dirLabel);
    dirPanel.add(Box.createHorizontalStrut(2));
    dirPanel.add(cacheDirectoryTextField);
    dirPanel.add(Box.createHorizontalGlue());
    analysisSavePanel.add(dirPanel);
    analysisSavePanel.add(Box.createVerticalStrut(10));

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(cacheDirButton);
    buttonPanel.add(Box.createHorizontalStrut(10));
    buttonPanel.add(clearCacheButton);
    buttonPanel.add(Box.createHorizontalGlue());
    analysisSavePanel.add(buttonPanel);
    analysisSavePanel.add(Box.createVerticalStrut(10));

    establishComponentStates();
    this.add(mainPanel);
    this.add(Box.createVerticalStrut(2));
    this.add(analysisSavePanel);
    this.add(Box.createVerticalGlue());
    this.setMinimumSize(new Dimension(500,300));
    this.setSize(new Dimension(500,300));
  }


  private void establishComponentStates() {
    boolean state = cacheAnalysisCheckBox.isSelected();
    cacheDirButton.setEnabled(state);
    cacheDirectoryTextField.setEnabled(state);
    dirLabel.setEnabled(state);
  }

}