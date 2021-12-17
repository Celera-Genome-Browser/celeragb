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


/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/

package client.gui.framework.browser;

import api.entity_model.access.observer.*;
import api.entity_model.management.ModelMgr;
import api.entity_model.management.ModifyManager;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.annotation.Workspace;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.fundtype.MutableAlignment;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.concrete_facade.xml.WorkspaceXmlFileCloseHandler;
import api.facade.facade_mgr.DataSourceSelector;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;
import api.facade.facade_mgr.InUseProtocolListener;
import client.gui.application.game_viewer.GameViewer;
import client.gui.framework.pref_controller.PrefController;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.BrowserModelListenerAdapter;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.framework.session_mgr.SessionModelListener;
import client.gui.other.annotation_log.AnnotationLogViewer;
import client.gui.other.annotation_log.AnnotationLogWriter;
import client.gui.other.annotation_log.GBWAnnotationLogViewer;
import client.gui.other.xml.xml_writer.XMLWriter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.*;
/**
* This class provides the File menu for the Browser.  It has been
* externalized here to allow subclassing and overridding for modification.
* This can be currently done by subclassing Browser and overridding setFileMenu.
* Something cleaner should be worked out in the long run.
*
* Initially written by: Peter Davies
*/

public class FileMenu extends JMenu {

   Browser browser;
   JMenuItem menuFileExit;
   JMenuItem menuFilePrint;
   JMenuItem menuListOpen;
   JMenuItem menuItemSaveAsXML;
   JMenuItem menuItemDeleteWorkspace;
   JMenuItem menuCloseDataSources;
   JMenuItem menuViewWorkspaceFile;
   JMenuItem menuOpenGenomeVersion;
   JMenuItem menuOpenGff3;
   JMenuItem menuCEFViewer;
   JMenuItem menuOpenWorkSpace, menuCloseWorkSpace, menuOpenFeatureFile, setLoginMI, menuOpenAnnotationLog, menuOpenGBWAnnotationLog;
   ArrayList<JMenuItem> addedMenus = new ArrayList<JMenuItem>();
   private boolean workSpaceHasBeenSaved = false;
   private boolean isworkspaceDirty = false;
   private JDialog openDataSourceDialog = new JDialog();
   private MyWorkSpaceObserver workSpaceObserver;
   GenomeVersion workspaceGenomeVersion;
   private AxisObserver myAxisObserver = new MyAxisObserver();

   public FileMenu(Browser browser) {
      super("File");
      this.setMnemonic('F');
      this.browser = browser;
      SessionMgr.getSessionMgr().addSessionModelListener(new MySessionModelListener());
      //This puts login and password info into the browser properties.  Checking the login
      // save checkbox writes out to the session-persistent collection object.
      browser.getBrowserModel().setModelProperty("LOGIN", SessionMgr.getSessionMgr().getModelProperty("LOGIN"));
      browser.getBrowserModel().setModelProperty("PASSWORD", SessionMgr.getSessionMgr().getModelProperty("PASSWORD"));

      menuOpenGff3 = new JMenuItem("Open Genome Version from GFF3 File...", '3');
      menuOpenGff3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_MASK, false));
      menuOpenGff3.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            fileOpen_actionPerformed(e, "gff", null);
         }
      });

      menuOpenGenomeVersion = new JMenuItem("Open Genome Version from XML File...", 'G');
      menuOpenGenomeVersion.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK, false));
      menuOpenGenomeVersion.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            fileOpen_actionPerformed(e, "xmlgenomicaxis", null);
         }
      });

      menuCloseDataSources = new JMenuItem("Close Genome Version and Workspace", 'C');
      menuCloseDataSources.setEnabled(false);
      menuCloseDataSources.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            menuCloseDataSources_actionPerformed(e);
         }
      });

      menuOpenFeatureFile = new JMenuItem("Open Feature File...", 't');
      menuOpenFeatureFile.setEnabled(true);
      menuOpenFeatureFile.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            fileOpen_actionPerformed(e, "xmlfeature", null);

         }
      });

      menuOpenWorkSpace = new JMenuItem("Open Workspace File...", 'k');
      menuOpenWorkSpace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK, false));
      menuOpenWorkSpace.setEnabled(true);
      menuOpenWorkSpace.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            fileOpen_actionPerformed(e, "xmlworkspace", null);
            if (findGBWFileName() != null) {
               menuCloseWorkSpace.setEnabled(true);
            }
         }
      });

      menuCloseWorkSpace = new JMenuItem("Close Workspace File");
      menuCloseWorkSpace.setEnabled(false);
      menuCloseWorkSpace.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            gbwFileClose_actionPerformed();

         }
      });

      menuViewWorkspaceFile = new JMenuItem("View Opened Workspace File...", 'i');
      FacadeManager.addInUseProtocolListener(new MyInUseProtocolListener("xmlworkspace", menuViewWorkspaceFile, true));
      menuViewWorkspaceFile.setEnabled(false);
      menuViewWorkspaceFile.setHorizontalTextPosition(SwingConstants.RIGHT);
      menuViewWorkspaceFile.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            String file = findGBWFileName();
            if (file != null && file != "")
               new GameViewer(file, true);
         }
      });

      menuItemSaveAsXML = new JMenuItem("Save Workspace...", 'S');
      menuItemSaveAsXML.setEnabled(false);
      menuItemSaveAsXML.setHorizontalTextPosition(SwingConstants.RIGHT);
      menuItemSaveAsXML.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            boolean savedWorkspace = saveAsXML();
            if (savedWorkspace) {
               writeAnnotationLog();
               workSpaceHasBeenSaved = true;
               deleteWorkSpace("Would you like to delete the workspace? \n(This is not undoable)");
            }
         }
      });

      menuItemDeleteWorkspace = new JMenuItem("Delete Workspace", 'D');
      menuItemDeleteWorkspace.setEnabled(false);
      menuItemDeleteWorkspace.setHorizontalTextPosition(SwingConstants.RIGHT);
      menuItemDeleteWorkspace.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (workSpaceHasBeenSaved)
               deleteWorkSpace(null);
            else
               deleteWorkSpace("The workspace has not been saved.  \nAre you sure you would like to delete" + " the workspace and everything in it?");

         }
      });

      menuOpenAnnotationLog = new JMenuItem("View Current Annotation Log...", 'C');
      menuOpenAnnotationLog.setEnabled(false);
      menuOpenAnnotationLog.setHorizontalTextPosition(SwingConstants.RIGHT);
      menuOpenAnnotationLog.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            AnnotationLogViewer frame = new AnnotationLogViewer(FileMenu.this.browser);
            frame.pack();
            frame.setVisible(true);

         }
      });

      menuOpenGBWAnnotationLog = new JMenuItem("View Opened Workspace File Annotation Log...", 'A');
      menuOpenGBWAnnotationLog.setEnabled(false);
      menuOpenGBWAnnotationLog.setHorizontalTextPosition(SwingConstants.RIGHT);
      menuOpenGBWAnnotationLog.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
            GBWAnnotationLogViewer frame = new GBWAnnotationLogViewer(FileMenu.this.browser, FileMenu.this.findGBWFileName());
            frame.pack();
            frame.setVisible(true);

         }
      });

      menuFileExit = new JMenuItem("Exit", 'x');
      menuFileExit.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            fileExit_actionPerformed();
         }
      });

      setLoginMI = new JMenuItem("Set Login...", 'o');
      setLoginMI.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            setLogin();
         }
      });

      menuListOpen = new JMenuItem("List Open Data Sources...", 'L');
      menuListOpen.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            menuListOpen_actionPerformed();
         }
      });

      menuCEFViewer = new JMenuItem("View Genomics Exchange File...", 'n');
      menuCEFViewer.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            new GameViewer(null, true);
         }
      });

      menuFilePrint = new JMenuItem("Print Screen...", 'P');
      menuFilePrint.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            filePrint_actionPerformed();
         }
      });
      addMenuItems();

      ModelMgr.getModelMgr().addModelMgrObserver(new MyModelManagerObserver());
      ModifyManager.getModifyMgr().addObserver(new MyModifyManagerObserver());
      browser.getBrowserModel().addBrowserModelListener(new MyBrowserModelListenerAdapter());

   }

   private void setLogin() {
      PrefController.getPrefController().getPrefInterface(client.gui.other.panels.DataSourceSettings.class, browser);
   }

   private boolean saveAsXML() {
      return XMLWriter.getXMLWriter().saveAsXML();
   }

   private void writeAnnotationLog() {
      AnnotationLogWriter.getAnnotationLogWriter().writeLog(XMLWriter.getXMLWriter().getSavedXmlFileName(), true);
   }

   void deleteWorkSpace(String confirmText) {
      int ans = JOptionPane.YES_OPTION;
      if (confirmText != null) {
         ans = JOptionPane.showConfirmDialog(FileMenu.this.browser, confirmText, "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
      }
      if (ans == JOptionPane.YES_OPTION) {
         ModifyManager.getModifyMgr().flushStacks();
         if (workspaceGenomeVersion != null) {
            workspaceGenomeVersion.unloadWorkspace();
         }
      }
   }

   public void addMenu(JMenuItem menuItem) {
      addedMenus.add(menuItem);
      addMenuItems();
   }

   public void removeMenu(JMenuItem menuItem) {
      addedMenus.remove(menuItem);
      addMenuItems();
   }

   private void addMenuItems() {
      removeAll();

      add(menuOpenGff3);
      add(menuOpenGenomeVersion);

      add(menuOpenWorkSpace);
      add(menuCloseWorkSpace);
      add(menuOpenFeatureFile);
      add(setLoginMI);
      add(new JSeparator());
      add(menuItemSaveAsXML);
      add(menuItemDeleteWorkspace);
      add(new JSeparator());
      add(menuCEFViewer);
      add(menuOpenAnnotationLog);
      add(menuViewWorkspaceFile);
      add(menuOpenGBWAnnotationLog);
      add(new JSeparator());
      add(menuListOpen);
      add(menuFilePrint);
      if (addedMenus.size() > 0)
         add(new JSeparator());
       for (JMenuItem addedMenu : addedMenus) {
           add(addedMenu);
       }
      add(new JSeparator());
      add(menuFileExit);

   }

   //File | Exit action performed

   private void fileExit_actionPerformed() {
      SessionMgr.getSessionMgr().systemExit();
   }

   //File | Open action performed

   private void fileOpen_actionPerformed(ActionEvent e, String protocol, Object dataSource) {
      browser.repaint();
// JCVI LLF, 10/23/2006
//      if (SessionMgr.getSessionMgr().getModelProperty(SessionMgr.USER_NAME) == null
//         || SessionMgr.getSessionMgr().getModelProperty(SessionMgr.USER_NAME).equals("")
//         && ModelMgr.getModelMgr().getNumberOfLoadedGenomeVersions() == 0) {
//         int answer =
//            JOptionPane.showConfirmDialog(browser, "Please enter your CDS login information.", "Information Required", JOptionPane.OK_CANCEL_OPTION);
//         if (answer == JOptionPane.CANCEL_OPTION)
//            return;
//         PrefController.getPrefController().getPrefInterface(client.gui.other.panels.DataSourceSettings.class, browser);
//      }
//      // Double check.  Exit if still empty or not useful.
//      if (SessionMgr.getSessionMgr().getModelProperty(SessionMgr.USER_NAME) == null
//         || SessionMgr.getSessionMgr().getModelProperty(SessionMgr.USER_NAME).equals("")
//         && ModelMgr.getModelMgr().getNumberOfLoadedGenomeVersions() == 0) {
//         return;
//      }
      DataSourceSelector dss = FacadeManager.getDataSourceSelectorForProtocol(protocol);
      FacadeManagerBase facadeManager = FacadeManager.getFacadeManager(protocol);
      if (dataSource == null)
         dss.selectDataSource(facadeManager);
      else
         dss.setDataSource(facadeManager, dataSource);
      ((JMenuItem) e.getSource()).setEnabled(//disable the menu if the protocol cannot support multiple datasources
      FacadeManager.canProtocolAddMoreDataSources(protocol));
   }

   private void gbwFileClose_actionPerformed() {

      if (workspaceGenomeVersion != null && !workspaceGenomeVersion.getWorkspace().getWorkspaceOids().isEmpty()) {
         int ans;
         ans =
            JOptionPane.showConfirmDialog(
               FileMenu.this.browser,
               "Workspace will be deleted. \nWould you like to save it",
               "Confirm",
               JOptionPane.YES_NO_OPTION,
               JOptionPane.QUESTION_MESSAGE);

         if (ans == JOptionPane.YES_OPTION) {
            // before closing the file
            // save the workspace
            boolean savedWorkspace = saveAsXML();
            if (savedWorkspace) {
               writeAnnotationLog();
               workSpaceHasBeenSaved = true;
            }
         }
      }
      // unload the file from the list of open data sources
      String gbwfile = findGBWFileName();
      FacadeManagerBase facadeManager = FacadeManager.getFacadeManager("xmlworkspace");
      WorkspaceXmlFileCloseHandler handler = new WorkspaceXmlFileCloseHandler(facadeManager);
      handler.unLoadXmlFile(gbwfile);

      //delete the workspace so that new .gbw files can be opened.
      //the reason the delete the workspace is for avoiding id clashes.
      //entities across .gbw files can have same oids.
      ModifyManager.getModifyMgr().flushStacks();
      if (workspaceGenomeVersion != null) {
         workspaceGenomeVersion.unloadWorkspace();
      }

      menuOpenWorkSpace.setEnabled(true);
      menuOpenGBWAnnotationLog.setEnabled(false);
      menuViewWorkspaceFile.setEnabled(false);
   }

   private void filePrint_actionPerformed() {
      browser.printBrowser();
   }

   private void menuListOpen_actionPerformed() {
      /**
       * There is no good way to get this information so I need to "splice" together
       * open genome versions and available GBW, GBF's.  No pun intended.
       * This implies refactoring of the Facades which is not scheduled for now
       * and going through each feature in the data model is probably the most accurate
       * but not the best way to get this information.
       */
      ArrayList<GenomeVersion> openGenomeVersions = new ArrayList<GenomeVersion>(ModelMgr.getModelMgr().getSelectedGenomeVersions());
      ArrayList allDataSources = new ArrayList(Arrays.asList(FacadeManager.getFacadeManager().getOpenDataSources()));
      ArrayList<String> finalDataSources = new ArrayList<String>();
      for (Object openGenomeVersion : openGenomeVersions) {
           finalDataSources.add(((GenomeVersion) openGenomeVersion).getDescription());
       }
      Collections.sort(finalDataSources);
      for (Object allDataSource : allDataSources) {
           String tmpSource = ((String) allDataSource).trim();
           if (tmpSource.toLowerCase().endsWith(".gbf") || tmpSource.toLowerCase().endsWith(".gbw")) {
               StringTokenizer tok = new StringTokenizer(tmpSource, File.separator);
               while (tok.hasMoreTokens()) {
                   tmpSource = tok.nextToken();
               }
               finalDataSources.add(tmpSource);
           }
       }

      if (finalDataSources.size() == 0) {
         finalDataSources.add("No Sources Opened.");
      }
      openDataSourceDialog = new JDialog(browser, "Open Data Sources", true);
      openDataSourceDialog.setSize(400, 190);
      openDataSourceDialog.setResizable(false);
      JPanel mainPanel = new JPanel();
      JPanel topPanel = new JPanel();
      JPanel buttonPanel = new JPanel();
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
      mainPanel.setSize(400, 190);
      topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
      topPanel.setSize(400, 160);
      DefaultListModel listModel = new DefaultListModel();
      JList sources = new JList(listModel);
      sources.setRequestFocusEnabled(false);
       for (Object finalDataSource : finalDataSources) {
           listModel.addElement((String) finalDataSource);
       }
      JScrollPane sp = new JScrollPane();
      sp.setSize(380, 140);
      sp.getViewport().setView(sources);
      JButton okButton = new JButton("OK");
      okButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            openDataSourceDialog.dispose();
         }
      });
      okButton.setSize(40, 40);
      topPanel.add(sp);
      buttonPanel.add(okButton);
      mainPanel.add(topPanel);
      mainPanel.add(buttonPanel);
      openDataSourceDialog.getContentPane().add(mainPanel);
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = openDataSourceDialog.getSize();
      if (frameSize.height > screenSize.height)
         frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width)
         frameSize.width = screenSize.width;
      openDataSourceDialog.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
      mainPanel.getRootPane().setDefaultButton(okButton);
      openDataSourceDialog.setVisible(true);
   }

   private void menuCloseDataSources_actionPerformed(ActionEvent e) {
      int answer =
         JOptionPane.showConfirmDialog(
            browser,
            "Are you sure you want to unload all data, " + "close all data sources and information services and reset the browser?",
            "Confirm",
            JOptionPane.YES_NO_OPTION);
      if (answer == JOptionPane.YES_OPTION) {
         SessionMgr.getSessionMgr().resetSession();
         //  buildOpenInfoServiceMenu();
         menuCloseDataSources.setEnabled(false);
      }
   }

   private String findGBWFileName() {
      Object[] dataSources = FacadeManager.getFacadeManager().getOpenDataSources();
       for (Object dataSource : dataSources) {
           if (dataSource.toString().toLowerCase().endsWith(".gbw"))
               return dataSource.toString();
       }
      return null;
   }

   class ProtocolMenuItemListener implements ActionListener {
      private String protocol;
      private Object dataSource;
      private boolean displayBackupWarning;

      ProtocolMenuItemListener(String protocol) {
         this.protocol = protocol;
      }

      ProtocolMenuItemListener(String protocol, Object dataSource) {
         this(protocol);
         this.dataSource = dataSource;
      }

      ProtocolMenuItemListener(String protocol, Object dataSource, boolean displayBackupWarning) {
         this(protocol);
         this.dataSource = dataSource;
         this.displayBackupWarning = displayBackupWarning;
      }

      public void actionPerformed(ActionEvent e) {
         fileOpen_actionPerformed(e, protocol, dataSource);

         if (displayBackupWarning) {
            String[] msg = new String[3];
            msg[0] = "You are re-opening the backup file.  This disables automated backups. It is";
            msg[1] = "suggested that you save your work to a new file, restart the browser and";
            msg[2] = "begin working with the new file, so that automated backups can occur.";
            JOptionPane.showMessageDialog(browser, msg, "Warning!!", JOptionPane.WARNING_MESSAGE);
         }
      }
   }

   class MyWorkSpaceObserver extends WorkspaceObserverAdapter {
      public void noteWorkspaceDirtyStateChanged(Workspace theWorkspace, boolean newDirtyState) {
         // menu item should always be true irrespective of the dirty state
         // should be allowed to save workspace even if it is empty
         menuItemSaveAsXML.setEnabled(true);
         //  menuItemSaveAsXML.setEnabled(newDirtyState);
         isworkspaceDirty = newDirtyState;

      }
   }

   class MyModifyManagerObserver extends ModifyManagerObserverAdapter {

      public void noteCommandStringHistoryListNonEmpty() {
         menuOpenAnnotationLog.setEnabled(true);
      }

      public void noteCommandStringHistoryListEmpty() {
         menuOpenAnnotationLog.setEnabled(false);
      }
   }

   class MyAxisObserver extends AxisObserverAdapter {
      public void noteAlignmentOfEntity(Alignment addedAlignment) {
         if (addedAlignment instanceof MutableAlignment) {
            menuItemDeleteWorkspace.setEnabled(true);
         }

      }
   }

   class MyBrowserModelListenerAdapter extends BrowserModelListenerAdapter {
      public void browserMasterEditorEntityChanged(GenomicEntity masterEditorEntity) {
         if (masterEditorEntity != null) {
            masterEditorEntity.removeGenomicEntityObserver(myAxisObserver);
            masterEditorEntity.addGenomicEntityObserver(myAxisObserver);
         }
      }
   }

   class MyModelManagerObserver extends ModelMgrObserverAdapter {

      public void workSpaceCreated(GenomeVersion genomeVersion) {
         workSpaceObserver = new MyWorkSpaceObserver();
         genomeVersion.getWorkspace().addObserver(workSpaceObserver, true, true);
         menuOpenWorkSpace.setEnabled(false);

         if (workspaceGenomeVersion != null && !workspaceGenomeVersion.getWorkspace().getWorkspaceOids().isEmpty()) {
            menuItemDeleteWorkspace.setEnabled(true);
         }
         if (findGBWFileName() != null && !findGBWFileName().equals("")) {
            menuCloseWorkSpace.setEnabled(true);
         }
         else
            menuCloseWorkSpace.setEnabled(false);

         workspaceGenomeVersion = genomeVersion;
      }

      public void workSpaceRemoved(GenomeVersion genomeVersion, Workspace workspace) {
         workspace.removeObserver(workSpaceObserver);
         workSpaceObserver = null;
         if (findGBWFileName() == null || findGBWFileName().equals("")) {
            menuOpenWorkSpace.setEnabled(true);
         }
         menuItemDeleteWorkspace.setEnabled(false);
         if (findGBWFileName() != null && !findGBWFileName().equals("")) {
            menuCloseWorkSpace.setEnabled(true);
         }
         else
            menuCloseWorkSpace.setEnabled(false);
         workspaceGenomeVersion = null;
         workSpaceHasBeenSaved = false;
      }

      public void genomeVersionSelected(GenomeVersion genomeVersion) {
         super.genomeVersionSelected(genomeVersion);

         //Donot enable the menu item if the Workspace has been already cr
         Workspace workspace = null;
          for (Object o : ModelMgr.getModelMgr().getSelectedGenomeVersions()) {
              GenomeVersion model = (GenomeVersion) o;
              if (model.hasWorkspace()) {
                  workspace = model.getWorkspace();
                  break;
              }
          }
         if (workspace == null) {
            menuOpenWorkSpace.setEnabled(true);
            menuCloseWorkSpace.setEnabled(false);
         }
         else {
            menuOpenWorkSpace.setEnabled(false);
            menuCloseWorkSpace.setEnabled(true);
         }

      }

   }

   //  class XMLObserver implements XMLWriterObserver {
   //     public void canSaveAsXML(boolean canSave) {
   //           GenomicEntity selectedEntity = browser.getBrowserModel().getCurrentSelection();
   //           if (selectedEntity != null) {
   //               menuItemSaveAsXML.setEnabled(
   //                  !selectedEntity.getGenomeVersion().isReadOnly() && canSave);
   //           } // Selection already made.
   //           else {
   //               menuItemSaveAsXML.setEnabled(canSave);
   //           } // No selection as yet
   //     }
   //  }

   class MyInUseProtocolListener implements InUseProtocolListener {
      JMenuItem menuItem;
      String protocolOfInterest;
      boolean turnOnInUse;

      private MyInUseProtocolListener(String protocol, JMenuItem menuItem, boolean turnOnInUse) {
         this.menuItem = menuItem;
         this.protocolOfInterest = protocol;
         this.turnOnInUse = turnOnInUse;
      }

      public void protocolAddedToInUseList(String protocol) {
         if (protocol.equals(protocolOfInterest))
            menuItem.setEnabled(turnOnInUse);
         if (protocol.equals("xmlworkspace")) {
            //if opened dataSource is gbw file then toggle on the
            //menu option to view its log
            FileMenu.this.menuOpenGBWAnnotationLog.setEnabled(true);

         }
      }
      public void protocolRemovedFromInUseList(String protocol) {
         if (protocol.equals(protocolOfInterest))
            menuItem.setEnabled(turnOnInUse);
      }
   }

   class MySessionModelListener implements SessionModelListener {
      public void browserAdded(BrowserModel browserModel) {
      }
      public void browserRemoved(BrowserModel browserModel) {
      }

      public void sessionWillExit() {
         // saveLastDataSources();
         if (/*menuItemSaveAsXML.isEnabled() &&*/
            SessionMgr.getSessionMgr().getNumberOfOpenBrowsers() < 2 && /*!workSpaceHasBeenSaved*/
            isworkspaceDirty) {
            int answer =
               JOptionPane.showConfirmDialog(browser, "Would you like to save the workspace before closing?", "Save?", JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
               saveAsXML();
               writeAnnotationLog();
            }
         }
      }
      public void modelPropertyChanged(Object key, Object oldValue, Object newValue) {
      }
   }

}
