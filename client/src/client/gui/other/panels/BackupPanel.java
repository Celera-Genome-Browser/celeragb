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
import client.gui.other.xml.xml_writer.XMLWriter;
import client.shared.file_chooser.FileChooser;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

/**
 * Title:        Your Product Name
 * Description:  This is the main Browser in the System
 * @author Peter Davies
 * @version
 */

public class BackupPanel extends JPanel implements PrefEditor
{
    private boolean settingsChanged = false;
    JFileChooser backupFileNameForNextSession = new FileChooser();
    JFrame parentFrame;
    String userChosenLocation = null;

  public BackupPanel(JFrame parentFrame) {
    try {
      this.parentFrame=parentFrame;
      jbInit();
    }
    catch(Exception ex) {
       client.gui.framework.session_mgr.SessionMgr.getSessionMgr().handleException(ex);
    }
  }


    void jbInit() throws Exception {
        //--------- yes/no -----------------
        JPanel yesNoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        yesNoPanel.setBorder(new TitledBorder("Backup Workspace"));

        JRadioButton yesButton = new JRadioButton("Yes");
        JRadioButton noButton = new JRadioButton("No");
        if (XMLWriter.getXMLWriter().isBackingUpWorkspace())
            yesButton.setSelected(true);
        else
            noButton.setSelected(true);

        ButtonGroup yesNoGroup = new ButtonGroup();
        yesNoGroup.add(yesButton);
        yesNoGroup.add(noButton);
        yesButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    settingsChanged = true;
                    XMLWriter.getXMLWriter().setShouldBackupWorkspace(true);
                }});
        noButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    settingsChanged = true;
                    XMLWriter.getXMLWriter().setShouldBackupWorkspace(false);
                }});

        yesNoPanel.add(yesButton);
        yesNoPanel.add(noButton);

        //------ file name ----------------------
        JPanel fileNamePanel = new JPanel();
        fileNamePanel.setBorder(new TitledBorder("Backup File Name"));
        fileNamePanel.setLayout(new BoxLayout(fileNamePanel, BoxLayout.Y_AXIS));

        JLabel fileLabel = new JLabel(XMLWriter.getXMLWriter().getWorkspaceBackupFileName());

        JButton changeFileButton = new JButton("Change For Next Session");
        changeFileButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showChooser();
                }});

        fileNamePanel.add(Box.createVerticalStrut(8));
        fileNamePanel.add(fileLabel);
        fileNamePanel.add(Box.createVerticalStrut(8));
        fileNamePanel.add(changeFileButton);
        fileNamePanel.add(Box.createVerticalStrut(8));

        //----------- whole pane ---------------------
        JPanel wholePane = new JPanel();
        wholePane.setLayout(new GridLayout(0, 1, 16, 16));
        wholePane.setBorder(new TitledBorder("Backup Settings"));
        wholePane.add(yesNoPanel, BorderLayout.NORTH);
        wholePane.add(fileNamePanel, BorderLayout.CENTER);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalStrut(10));
        add(wholePane);
    }

  public String getDescription() {
    return "Set the Workspace backup parameters for frequency and file location.";
  }


  public String getPanelGroup() {
    return PrefController.SYSTEM_EDITOR;
  }


  private void showChooser() {
    JFileChooser chooser = null;
    chooser = new FileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    int returnVal = chooser.showOpenDialog(parentFrame);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      userChosenLocation = file.getAbsolutePath();
      settingsChanged=true;
    } // APPROVED

  } // End method




  /**
   * Method to supoprt the PrefEditor interface, invoked when the Pref
   * Controller's Cancel button is pressed.
   */
  public void cancelChanges() { settingsChanged=false;}

  public boolean hasChanged() { return settingsChanged; }

  /**
   * Method to supoprt the PrefEditor interface, invoked when the Pref
   * Controller's Apply or OK buttons are pressed.
   */
  public String[] applyChanges(){
      settingsChanged=false;
      if (userChosenLocation!=null &&
        !XMLWriter.getXMLWriter().getWorkspaceBackupFileName().equals(userChosenLocation)) {
        SessionMgr.getSessionMgr().setBackupFileName(userChosenLocation);
        return new String[]{"Change in Workspace Backup file location."};
      }
      return NO_DELAYED_CHANGES;

  }

  public void dispose(){}
  public String getName() { return "Workspace Backup"; }

}