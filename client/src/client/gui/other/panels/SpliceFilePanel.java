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
import api.entity_model.access.observer.ModelMgrObserverAdapter;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.genetics.GenomeVersion;
import api.entity_model.model.genetics.Species;
import client.gui.framework.pref_controller.PrefController;
import client.gui.framework.roles.PrefEditor;
import client.shared.file_chooser.FileChooser;
import client.shared.text_component.StandardTextField;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SpliceFilePanel extends JPanel implements PrefEditor {

  private static final String SPLICE_FILE="Splice_File";
  private static final String SPLICE_FILE_DIRECTORY="Splice_File_Directory";

  private String acceptorFile = new String();
  private String donorFile = new String();
  private String neitherFile = new String();

  // This variable maintains the last selected directory to aid in subsequent
  // file selections.
  private String hotDirectory = new String();
  private JFrame parentFrame;
  private boolean settingsChanged = false;
  JPanel mainPanel = new JPanel();
  TitledBorder titledBorder1;
  JPanel jPanel1 = new JPanel();
  JButton fileNeitherSelectionButton = new JButton();
  JButton fileDonorSelectionButton = new JButton();
  static JTextField spliceNeitherTextField = new StandardTextField();
  JButton fileAcceptorSelectionButton = new JButton();
  static JTextField spliceAcceptorTextField = new StandardTextField();
  static JTextField spliceDonorTextField = new StandardTextField();
  TitledBorder titledBorder2;
  static JCheckBox overrideCheckBox1 = new JCheckBox();


  /**
   * This class overrides the splice profiles for ALL species that
   * have been loaded into the browser.
   */
  public SpliceFilePanel(JFrame parentFrame) {
    try {
      this.parentFrame=parentFrame;
      ModelMgr.getModelMgr().addModelMgrObserver(new ModelMgrObserverAdapter() {
        public void genomeVersionSelected(GenomeVersion genomeVersion){
          genomeVersionSelectedActionPerformed(genomeVersion);
        }
      });
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }


  public String getDescription() {
    return "Set the Splice Profiles for all species in the current session.";
  }

  public String getPanelGroup() {
    return PrefController.SYSTEM_EDITOR;
  }

  public String getName() { return "Splice Profiles"; }

  public boolean hasChanged() { return settingsChanged; }

  /**
   * Tell all available species that it is time to reload the splice calculation
   * parameters.  Does not need to notify anyone indirectly.
   */
  public String[] applyChanges() {
    settingsChanged=false;
    Set gvSet = ModelMgr.getModelMgr().getSelectedGenomeVersions();
    if (overrideCheckBox1.isSelected()) {
      File acceptorFile = new File(spliceAcceptorTextField.getText().trim());
      File donorFile = new File(spliceDonorTextField.getText().trim());
      File neitherFile = new File(spliceNeitherTextField.getText().trim());
      if (!acceptorFile.exists() || !donorFile.exists() || !neitherFile.exists()) {
        JOptionPane.showMessageDialog(parentFrame, "There is an error in the profile definitions.\nOverride is canceled.",
              "Invalid File Defined", JOptionPane.WARNING_MESSAGE);
        overrideCheckBox1.setSelected(false);
        setGroupItemsEnabled(overrideCheckBox1.isSelected());
        return NO_DELAYED_CHANGES;
      }
      for (Iterator it = gvSet.iterator();it.hasNext();) {
        Species tmpSpecies = ((GenomeVersion)it.next()).getSpecies();
        tmpSpecies.overrideDefaultSpliceProfiles(acceptorFile, donorFile, neitherFile);
      }
    }
    else {
      for (Iterator it = gvSet.iterator();it.hasNext();) {
        Species tmpSpecies = ((GenomeVersion)it.next()).getSpecies();
        tmpSpecies.loadDefaultSpliceProperties();
      }
    }
    return NO_DELAYED_CHANGES;
  }

  // Do nothing.
  public void cancelChanges() {
    settingsChanged=false;
  }

  public void dispose() {}
  private void jbInit() throws Exception {
    setGroupItemsEnabled(overrideCheckBox1.isSelected());
    spliceAcceptorTextField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e){ settingsChanged=true; }
      public void removeUpdate(DocumentEvent e){ settingsChanged=true; }
      public void changedUpdate(DocumentEvent e) { settingsChanged=true; }
    });
    spliceDonorTextField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e){ settingsChanged=true; }
      public void removeUpdate(DocumentEvent e){ settingsChanged=true; }
      public void changedUpdate(DocumentEvent e) { settingsChanged=true; }
    });
    spliceNeitherTextField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e){ settingsChanged=true; }
      public void removeUpdate(DocumentEvent e){ settingsChanged=true; }
      public void changedUpdate(DocumentEvent e) { settingsChanged=true; }
    });
    titledBorder1 = new TitledBorder("Splice File Selection");
    titledBorder2 = new TitledBorder("Manually Select Splice Profiles");
    mainPanel.setLayout(null);
    this.setLayout(null);
    mainPanel.setBorder(titledBorder1);
    mainPanel.setBounds(new Rectangle(5, 6, 493, 202));
    jPanel1.setBorder(titledBorder2);
    jPanel1.setBounds(new Rectangle(14, 23, 466, 169));
    jPanel1.setLayout(null);
    fileNeitherSelectionButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if (evt.getSource()==fileNeitherSelectionButton) {
          JFileChooser fileChooser = new FileChooser(hotDirectory);
          fileChooser.setVisible(true);
          fileChooser.setFileSelectionMode(FileChooser.FILES_ONLY);
          fileChooser.setFileFilter(new MyFileFilter());
          int returnVal = fileChooser.showDialog(parentFrame, "Select File");
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            neitherFile = fileChooser.getSelectedFile().getAbsolutePath();
            hotDirectory=fileChooser.getCurrentDirectory().toString();
            spliceNeitherTextField.setText(neitherFile);
          }
        }
      }
    });
    fileNeitherSelectionButton.setText("Choose \"Neither\" File");
    fileNeitherSelectionButton.setBounds(new Rectangle(8, 130, 166, 23));
    fileDonorSelectionButton.setBounds(new Rectangle(8, 96, 166, 23));
    fileDonorSelectionButton.setText("Choose \"Donor\" File");
    fileDonorSelectionButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if (evt.getSource()==fileDonorSelectionButton) {
          JFileChooser fileChooser = new FileChooser(hotDirectory);
          fileChooser.setVisible(true);
          fileChooser.setFileSelectionMode(FileChooser.FILES_ONLY);
          fileChooser.setFileFilter(new MyFileFilter());
          int returnVal = fileChooser.showDialog(parentFrame, "Select File");
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            donorFile = fileChooser.getSelectedFile().getAbsolutePath();
            hotDirectory=fileChooser.getCurrentDirectory().toString();
            spliceDonorTextField.setText(donorFile);
          }
        }
      }
    });
    spliceNeitherTextField.setBounds(new Rectangle(179, 130, 276, 23));
    fileAcceptorSelectionButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if (evt.getSource()==fileAcceptorSelectionButton) {
          JFileChooser fileChooser = new FileChooser(hotDirectory);
          fileChooser.setVisible(true);
          fileChooser.setFileSelectionMode(FileChooser.FILES_ONLY);
          fileChooser.setFileFilter(new MyFileFilter());
          int returnVal = fileChooser.showDialog(parentFrame, "Select File");
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            acceptorFile = fileChooser.getSelectedFile().getAbsolutePath();
            hotDirectory=fileChooser.getCurrentDirectory().toString();
            spliceAcceptorTextField.setText(acceptorFile);
          }
        }
      }
    });
    fileAcceptorSelectionButton.setText("Choose \"Acceptor\" File");
    fileAcceptorSelectionButton.setBounds(new Rectangle(8, 61, 166, 23));
    spliceAcceptorTextField.setBounds(new Rectangle(179, 61, 276, 23));
    spliceDonorTextField.setBounds(new Rectangle(179, 96, 276, 23));
    overrideCheckBox1.setText("Override Default Splice Profiles");
    overrideCheckBox1.setBounds(new Rectangle(18, 29, 206, 26));
    overrideCheckBox1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        overrideCheckBox1_actionPerformed(e);
      }
    });
    //mainPanel.setSize(505, 390);
    this.add(mainPanel, null);
    mainPanel.add(jPanel1, null);
    jPanel1.add(overrideCheckBox1, null);
    jPanel1.add(spliceAcceptorTextField, null);
    jPanel1.add(fileAcceptorSelectionButton, null);
    jPanel1.add(fileDonorSelectionButton, null);
    jPanel1.add(spliceDonorTextField, null);
    jPanel1.add(fileNeitherSelectionButton, null);
    jPanel1.add(spliceNeitherTextField, null);
    this.setSize(505, 390);
  }

  void overrideCheckBox1_actionPerformed(ActionEvent e) {
    if (e.getSource()==overrideCheckBox1) {
      settingsChanged=true;
      setGroupItemsEnabled(overrideCheckBox1.isSelected());
    }
  }

  private void setGroupItemsEnabled(boolean tmpBoolean) {
    fileAcceptorSelectionButton.setEnabled(tmpBoolean);
    fileDonorSelectionButton.setEnabled(tmpBoolean);
    fileNeitherSelectionButton.setEnabled(tmpBoolean);
    spliceAcceptorTextField.setEnabled(tmpBoolean);
    spliceDonorTextField.setEnabled(tmpBoolean);
    spliceNeitherTextField.setEnabled(tmpBoolean);
  }

  private void genomeVersionSelectedActionPerformed(GenomeVersion genomeVersion) {
    //System.out.println("Applying to new GenomeVersion");
    if (overrideCheckBox1.isSelected()) {
      File acceptorFile = new File(spliceAcceptorTextField.getText().trim());
      File donorFile = new File(spliceDonorTextField.getText().trim());
      File neitherFile = new File(spliceNeitherTextField.getText().trim());
      if (!acceptorFile.exists() || !donorFile.exists() || !neitherFile.exists()) {
        JOptionPane.showMessageDialog(parentFrame, "There is an error in the profile definitions.\nOverride is canceled.",
              "Invalid File Defined", JOptionPane.WARNING_MESSAGE);
        overrideCheckBox1.setSelected(false);
        setGroupItemsEnabled(overrideCheckBox1.isSelected());
        return;
      }
      Species tmpSpecies = genomeVersion.getSpecies();
      tmpSpecies.overrideDefaultSpliceProfiles(acceptorFile, donorFile, neitherFile);
    }
  }

  private class MyFileFilter extends javax.swing.filechooser.FileFilter {
    public boolean accept(File pathname) {
      if (pathname.toString().endsWith(".csv") ||
          pathname.isDirectory()) return true;
      else return false;
    }

    public String getDescription() { return "Splice Profile Files"; }

  }
}