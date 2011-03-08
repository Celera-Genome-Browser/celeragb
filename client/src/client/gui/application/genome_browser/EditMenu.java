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
package client.gui.application.genome_browser;

import api.entity_model.access.observer.ModifyManagerObserverAdapter;
import api.entity_model.management.ModifyManager;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.GenomicEntity;
import api.stub.geometry.Range;
import client.gui.framework.browser.Browser;
import client.gui.framework.pref_controller.PrefController;
import client.gui.framework.property_rules.PropertyRuleDialog;
import client.gui.framework.session_mgr.BrowserModelListenerAdapter;
import client.gui.framework.session_mgr.SessionMgr;
import client.shared.file_chooser.FileChooser;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;


/**
* This class provides a EditMenu specific to the FlyGraph application.
*
* Initially written by: Peter Davies
*
*/
public class EditMenu extends JMenu {
    private static String fileSep = File.separator;
    private static final String EXPORT_IMPORT_LOCATION = 
            "PreferenceExportImportLocation";
    private JMenuItem menuUnDo;
    private JMenuItem menuReDo;
    private JMenuItem menuPropertyRules;
    private JMenuItem menuCut;
    private JMenuItem menuCopy;
    private JMenuItem menuPaste;
    private JMenuItem menuPrefSystem;
    private JMenuItem menuPrefSubView;
    private JMenuItem menuPrefMainView;
    private JMenuItem menuPrefExport;
    private JMenuItem menuPrefImport;
    private JMenu menuSetPreferences;
    private String userHomeDir = System.getProperty("user.home") + fileSep + 
                                 "x" + fileSep + "GenomeBrowser";
    private final Browser browser;
    private Action copyAction;
    private Action cutAction;
    private Action pasteAction;
    private SequenceMenu sequenceMenu;

    public EditMenu(Browser browser) {
        setText("Edit");
        this.browser = browser;
        sequenceMenu = new SequenceMenu(browser);
        browser.getBrowserModel().addBrowserModelListener(new BrowserModelListenerAdapter() {
            public void browserMasterEditorEntityChanged(GenomicEntity masterEditorEntity) {
                if (masterEditorEntity instanceof GenomicAxis && 
                            !EditMenu.this.browser.getBrowserModel()
                                                  .getMasterEditorSelectedRange()
                                                  .isNull()) {
                    sequenceMenu.setEnabled(true);
                } else {
                    sequenceMenu.setEnabled(false);
                }
            }

            public void browserMasterEditorSelectedRangeChanged(Range masterEditorSelectedRange) {
                if (EditMenu.this.browser.getBrowserModel()
                                         .getMasterEditorEntity() instanceof GenomicAxis && 
                            !masterEditorSelectedRange.isNull()) {
                    sequenceMenu.setEnabled(true);
                } else {
                    sequenceMenu.setEnabled(false);
                }
            }
        });
        this.setMnemonic('E');
        menuUnDo = new JMenuItem("Undo", 'U');
        menuUnDo.setHorizontalTextPosition(SwingConstants.RIGHT);
        menuUnDo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 
                                                       InputEvent.CTRL_MASK, 
                                                       false));
        menuUnDo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                unDo_actionPerformed(e);
            }
        });
        menuUnDo.setEnabled(false);
        add(menuUnDo);

        menuReDo = new JMenuItem("Redo", 'R');
        menuReDo.setHorizontalTextPosition(SwingConstants.RIGHT);
        menuReDo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, 
                                                       InputEvent.CTRL_MASK, 
                                                       false));

        menuReDo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reDo_actionPerformed(e);
            }
        });
        menuReDo.setEnabled(false);
        add(menuReDo);
        add(new JSeparator());
        cutAction = new MyCutAction();
        cutAction.putValue(Action.NAME, "Cut");
        menuCut = new JMenuItem(cutAction);
        add(menuCut);

        copyAction = new MyCopyAction();
        copyAction.putValue(Action.NAME, "Copy");
        menuCopy = new JMenuItem(copyAction);
        add(menuCopy);

        pasteAction = new MyPasteAction();
        pasteAction.putValue(Action.NAME, "Paste");
        menuPaste = new JMenuItem(pasteAction);
        add(menuPaste);

        add(sequenceMenu);
        sequenceMenu.setEnabled(false);
        add(new JSeparator());

        menuPropertyRules = new JMenuItem("Property Rules...", 't');
        menuPropertyRules.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                establishRuleDialog();
            }
        });
        add(menuPropertyRules);

        menuSetPreferences = new JMenu("Preferences");
        menuSetPreferences.setMnemonic('P');
        add(menuSetPreferences);

        menuPrefMainView = new JMenuItem("Genomic Axis Annotation View...", 'M');
        menuPrefMainView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 
                                                               InputEvent.CTRL_MASK, 
                                                               false));
        menuPrefMainView.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                establishPrefController(
                        PrefController.GENOMIC_AXIS_ANNOTATION_VIEW_EDITOR);
            }
        });
        menuSetPreferences.add(menuPrefMainView);

        menuPrefSubView = new JMenuItem("SubViews...", 'V');
        menuPrefSubView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 
                                                              InputEvent.CTRL_MASK, 
                                                              false));
        menuPrefSubView.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                establishPrefController(PrefController.SUB_VIEW_EDITOR);
            }
        });
        menuSetPreferences.add(menuPrefSubView);

        menuPrefSystem = new JMenuItem("System...", 'S');
        menuPrefSystem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 
                                                             InputEvent.CTRL_MASK, 
                                                             false));
        menuPrefSystem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                establishPrefController(PrefController.SYSTEM_EDITOR);
            }
        });
        menuSetPreferences.add(menuPrefSystem);

        menuPrefExport = new JMenuItem("Export Preference File...", 'x');
        menuPrefExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String targetDir = userHomeDir;

                    if (SessionMgr.getSessionMgr()
                                  .getModelProperty(EXPORT_IMPORT_LOCATION) != null) {
                        targetDir = (String) SessionMgr.getSessionMgr()
                                                       .getModelProperty(EXPORT_IMPORT_LOCATION);
                    }

                    FileChooser tmpExportChooser = new FileChooser(userHomeDir);
                    tmpExportChooser.setDialogTitle("Select File To Export");

                    int ans = tmpExportChooser.showDialog(EditMenu.this.browser, 
                                                          "OK");

                    if (ans == FileChooser.CANCEL_OPTION) {
                        return;
                    }

                    File targetToExport = tmpExportChooser.getSelectedFile();

                    if (targetToExport == null) {
                        return;
                    }

                    FileChooser tmpDestChooser = new FileChooser(targetDir);
                    tmpDestChooser.setDialogTitle("Select File Destination");
                    tmpDestChooser.setFileSelectionMode(
                            FileChooser.DIRECTORIES_ONLY);
                    ans = tmpDestChooser.showDialog(EditMenu.this.browser, "OK");

                    if (ans == FileChooser.CANCEL_OPTION) {
                        return;
                    }

                    // Copy file to targetDir here.
                    String destDir = tmpDestChooser.getSelectedFile()
                                                   .getAbsolutePath();

                    if ((destDir == null) || destDir.equals("")) {
                        return;
                    }

                    File newFile = new File(destDir + fileSep + 
                                                targetToExport.getName());
                    copyFile(targetToExport, newFile);

                    /**
                     * Save preference if the user has changed export/import directory.
                     * Assuming that exports and imports occur in from the same directory.
                     */
                    if ((destDir != null) && !destDir.equals(targetDir)) {
                        SessionMgr.getSessionMgr()
                                  .setModelProperty(EXPORT_IMPORT_LOCATION, 
                                                    destDir);
                    }
                } catch (Exception ex) {
                    SessionMgr.getSessionMgr().handleException(ex);
                }
            }
        });
        menuSetPreferences.add(menuPrefExport);

        menuPrefImport = new JMenuItem("Import Preference File...", 'I');
        menuPrefImport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String targetDir = userHomeDir;

                    if (SessionMgr.getSessionMgr()
                                  .getModelProperty(EXPORT_IMPORT_LOCATION) != null) {
                        targetDir = (String) SessionMgr.getSessionMgr()
                                                       .getModelProperty(EXPORT_IMPORT_LOCATION);
                    }

                    FileChooser tmpImportChooser = new FileChooser(targetDir);
                    tmpImportChooser.setDialogTitle("Select File To Import");

                    int ans = tmpImportChooser.showDialog(EditMenu.this.browser, 
                                                          "OK");

                    if (ans == FileChooser.CANCEL_OPTION) {
                        return;
                    }

                    File targetToImport = tmpImportChooser.getSelectedFile();

                    if (targetToImport == null) {
                        return;
                    }

                    String destDir = userHomeDir + fileSep;
                    File newFile = new File(destDir + targetToImport.getName());
                    copyFile(targetToImport, newFile);

                    /**
                     * Save preference if the user has changed export/import directory.
                     * Assuming that exports and imports occur in from the same directory.
                     */
                    String newDir = tmpImportChooser.getCurrentDirectory()
                                                    .getAbsolutePath();

                    if ((newDir != null) && !newDir.equals(targetDir)) {
                        SessionMgr.getSessionMgr()
                                  .setModelProperty(EXPORT_IMPORT_LOCATION, 
                                                    newDir);
                    }
                } catch (Exception ex) {
                    SessionMgr.getSessionMgr().handleException(ex);
                }
            }
        });
        menuSetPreferences.add(menuPrefImport);

        ModifyManager.getModifyMgr().addObserver(new CommandObserver());
    }

    public void setPopupMenuVisible(boolean b) {
        menuCut.setEnabled(cutAction.isEnabled());
        menuCopy.setEnabled(copyAction.isEnabled());
        menuPaste.setEnabled(pasteAction.isEnabled());
        super.setPopupMenuVisible(b);
    }

    private void establishPrefController(String prefLevel) {
        browser.repaint();
        PrefController.getPrefController().getPrefInterface(prefLevel, browser);
    }

    private void establishRuleDialog() {
        browser.repaint();
        new PropertyRuleDialog(browser).setVisible(true);
    }

    private void unDo_actionPerformed(ActionEvent e) {
        //  try{
        ModifyManager.getModifyMgr().undoCommand();

        /*
                            }catch(Exception ex){
              JOptionPane.showMessageDialog(browser,
              ex.getMessage() ,
              "Warning!", JOptionPane.PLAIN_MESSAGE);
        }
        */
    }

    private void reDo_actionPerformed(ActionEvent e) {
        // try{
        ModifyManager.getModifyMgr().redoCommand();

        /*
        }catch(Exception ex){
         JOptionPane.showMessageDialog(browser,
         ex.getMessage() ,
         "Warning!", JOptionPane.PLAIN_MESSAGE);
        
        }
        */
    }

    /**
     * This method exists to help the pref file export and import actions.
     */
    private void copyFile(File oldFile, File newFile) {
        try {
            FileReader in = new FileReader(oldFile);
            FileWriter out = new FileWriter(newFile);
            int c;

            while ((c = in.read()) != -1)
                out.write(c);

            in.close();
            out.close();
        } catch (Exception ex) {
            SessionMgr.getSessionMgr().handleException(ex);
        }
    }

    class CommandObserver extends ModifyManagerObserverAdapter {
        public void noteCanUndo(String undoString) {
            if (undoString != null) {
                menuUnDo.setText("Undo " + undoString);
            } else {
                menuUnDo.setText("Undo");
            }

            menuUnDo.setEnabled(true);
        }

        public void noteCanRedo(String redoString) {
            if (redoString != null) {
                menuReDo.setText("Redo " + redoString);
            } else {
                menuReDo.setText("Redo");
            }

            menuReDo.setEnabled(true);
        }

        public void noteNoUndo() {
            menuUnDo.setText("Undo");
            menuUnDo.setEnabled(false);
        }

        public void noteNoRedo() {
            menuReDo.setText("Redo");
            menuReDo.setEnabled(false);
        }
    }

    class MyCopyAction extends DefaultEditorKit.CopyAction {
        public boolean isEnabled() {
            return (super.isEnabled() && getFocusedComponent() != null);
        }
    }

    class MyPasteAction extends DefaultEditorKit.PasteAction {
        public boolean isEnabled() {
            return (super.isEnabled() && (getFocusedComponent() != null) && 
                   getFocusedComponent().isEditable());
        }
    }

    class MyCutAction extends DefaultEditorKit.CutAction {
        public boolean isEnabled() {
            return (super.isEnabled() && (getFocusedComponent() != null) && 
                   getFocusedComponent().isEditable());
        }
    }
}