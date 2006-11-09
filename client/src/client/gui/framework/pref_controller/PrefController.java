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
 * Title:        Genome Browser<p>
 * Description:  <p>
 * @author Todd Safford
 * @version $Id$
 */
package client.gui.framework.pref_controller;

import javax.swing.*;
import java.util.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import client.gui.framework.roles.PrefEditor;
import client.gui.framework.session_mgr.SessionMgr;

/**
 * This is designed so that anyone who introduces a class and needs a user interface
 * to update settings for that class, may define a UI panel and register it with the
 * SessionMgr.  The SessionMgr will then turn around and call the registry method
 * of this Controller.  The developer may then bring up the Preferences frame
 * directly to their specific interface or use the default one.
 *
 * Upon registration, the name of the tab will be the string used as the key.
 */

public class PrefController {

  private static PrefController prefController = new PrefController();
  // This holds them pre-Construction.
  private Hashtable prefEditorsMap = new Hashtable();
  // This holds them ordered post-Construction.
  private Map orderedEditorMap = new TreeMap(new MyComparator());
  private HashMap classToNameMap = new HashMap();

  private static final String DEFAULT="Default";
  // List and offer the Panel Categories
  public static final String GENOMIC_AXIS_ANNOTATION_VIEW_EDITOR = "Genomic Axis Annotation View";
  public static final String SUB_VIEW_EDITOR  = "SubViews";
  public static final String SYSTEM_EDITOR   = "System";
  private JFrame parentFrame = null;
  private JDialog mainDialog;
  private JButton cancelButton = new JButton();
  private JTabbedPane tabPane = new JTabbedPane();
  private JButton applyButton = new JButton();
  private int screenWidth, screenHeight;
  JButton okButton = new JButton();
  JPanel dummyPanel = new JPanel();
  JPanel buttonPanel = new JPanel();
  JPanel tabPanePanel = new JPanel();
  private static int MIN_DIALOG_WIDTH=550;

  private PrefController() {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  // Singleton enforcement.

  /**
   * Getter for the Singleton.
   */
  public static PrefController getPrefController() { return prefController; }


  /**
   * Call to bring up the Preferences frame on a specific interface.
   */
  public void getPrefInterface(Object key, JFrame parentFrame) {
    String keyName=new String("");
    String prefLevel = new String("");
    this.parentFrame = parentFrame;
    tabPane.removeAll();
    if (mainDialog==null) {
      mainDialog = new JDialog(parentFrame,true);
      buttonPanel.validate();
      buttonPanel.setPreferredSize(new Dimension(MIN_DIALOG_WIDTH,(int)buttonPanel.getPreferredSize().getHeight()));
      mainDialog.getContentPane().setLayout(new BorderLayout());
      mainDialog.getContentPane().add(dummyPanel,BorderLayout.NORTH);
      mainDialog.getContentPane().add(tabPanePanel,BorderLayout.CENTER);
      mainDialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }
    Component component;
    Constructor constructor=null;
    try {
      if (!(key instanceof String)) {
        // Get the prefLevel of the panel requested.
        constructor=(Constructor)prefEditorsMap.get(key);
        component=(Component)constructor.newInstance(new Object[]{parentFrame});
        prefLevel = ((PrefEditor)component).getPanelGroup();
        keyName=((PrefEditor)component).getName();
      }
      else prefLevel = (String)key;
      // Go through the panels registered and pull out all those that belong
      // to the target prefLevel.
      for (Iterator it=prefEditorsMap.keySet().iterator();it.hasNext();) {
        Object handle = it.next();
        constructor=(Constructor)prefEditorsMap.get(handle);
        component=(Component)constructor.newInstance(new Object[]{parentFrame});
        if (component.getName()==null) {component.setName(handle.toString());}
        if (((PrefEditor)component).getPanelGroup().equals(prefLevel)) {
          orderedEditorMap.put(((PrefEditor)component).getName(),component);
          classToNameMap.put(handle, ((PrefEditor)component).getName());
        }
      }
    }
    catch (Exception ex) {SessionMgr.getSessionMgr().handleException(ex);}
    if (keyName.equals("")) keyName = DEFAULT;
    redrawTabs(keyName, prefLevel);
    mainDialog.pack();
    centerDialog();
    mainDialog.getRootPane().setDefaultButton(okButton);
    mainDialog.show();
  }

  private void redrawTabs(String selectedKeyName,String selectedTabGroup) {
    String tmpName;
    Component tmpComponent;
    tabPane.removeAll();
    for (Iterator it = orderedEditorMap.keySet().iterator();it.hasNext();) {
      tmpName=(String)it.next();
      tmpComponent=(Component)orderedEditorMap.get(tmpName);
      if (((PrefEditor)tmpComponent).getPanelGroup().equals(selectedTabGroup)) {
        tabPane.addTab(tmpName,null,tmpComponent,
          ((PrefEditor)tmpComponent).getDescription());
      }
    }
    if (!selectedKeyName.equals(DEFAULT)) {
      for (int x=0; x<tabPane.getTabCount(); x++) {
        if (tabPane.getTitleAt(x).equalsIgnoreCase(selectedKeyName)) {
          tabPane.setSelectedIndex(x);
          break;
        }
      }
    }
    else tabPane.setSelectedIndex(0);
    mainDialog.setTitle("Preferences: " + ((PrefEditor)tabPane.getSelectedComponent()).getPanelGroup());
  }

  /**
   * Method to register your interface with the controller.
   */
  public void registerPreferenceInterface(Object interfaceKey, Class interfaceClass) throws Exception {
    if (validatePrefEditorClass(interfaceClass)) {
      prefEditorsMap.put(interfaceKey,interfaceClass.getConstructor(new Class[]{JFrame.class}));
    }
    else throw new Exception ("Class passed for PrefEditor is not acceptable");
  }


  /**
   * Enforces PrefEditor role for interfaces.  Anything that is not a PrefEditor
   * interface will not be registered.
   */
  private boolean validatePrefEditorClass(Class prefEditor) {
    Class[] interfaces=prefEditor.getInterfaces();
    boolean editorSupported=false;
    for (int i=0;i<interfaces.length;i++) {
      if (interfaces[i]==client.gui.framework.roles.PrefEditor.class) {
        editorSupported=true;
        break;
      }
    }
    if (!editorSupported) {
      System.out.println("ERROR! - PrefEditor passed ("+prefEditor+")is not a PrefController editor!");
      return false;
    }

    Class testClass=prefEditor;
    while (testClass!=java.lang.Object.class && testClass!=java.awt.Component.class) {
      testClass=testClass.getSuperclass();
    }
    if (testClass==java.lang.Object.class) {
      System.out.println("ERROR! - PrefEditor passed ("+prefEditor+") is not a java.awt.Component!");
      return false;
    }

    try {
      prefEditor.getConstructor(new Class[]{JFrame.class});
    }
    catch (NoSuchMethodException nsme) {
      System.out.println("ERROR! - PrefEditor passed ("+prefEditor+") does not have a constructor that takes a JFrame.");
      return false;
    }
    return true;
  }


  /**
   * Removes the tab from the pane and also removes the item from the
   * registry.
   */
  public void deregisterPreferenceInterface(Object interfaceKey) {
    prefEditorsMap.remove(interfaceKey);
    String tmpString = (String)classToNameMap.get((Class)interfaceKey);
    if (tmpString!=null) {
      tabPane.remove((Component)orderedEditorMap.get(tmpString));
      classToNameMap.remove((Class)interfaceKey);
      orderedEditorMap.remove(tmpString);
    }
    tabPane.repaint();
  }


  private void jbInit() throws Exception {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    screenWidth = screenSize.width;
    screenHeight = screenSize.height;
    dummyPanel.setLayout(new BoxLayout(dummyPanel,BoxLayout.X_AXIS));
    dummyPanel.add(Box.createVerticalStrut(10));

    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButton_actionPerformed(e);
      }
    });
    applyButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        applyButton_actionPerformed(e);
      }
    });
    applyButton.setText("Apply");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okButton_actionPerformed(e);
      }
    });
    okButton.setText("OK");
    okButton.setDefaultCapable(true);
    okButton.setRequestFocusEnabled(true);
    buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
    tabPanePanel.setLayout(new BoxLayout(tabPanePanel,BoxLayout.X_AXIS));
    tabPanePanel.add(Box.createHorizontalStrut(10));
    tabPanePanel.add(tabPane);
    tabPanePanel.add(Box.createHorizontalStrut(10));
    buttonPanel.add(Box.createVerticalStrut(50));
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(okButton);
    buttonPanel.add(Box.createHorizontalStrut(10));
    buttonPanel.add(cancelButton);
    buttonPanel.add(Box.createHorizontalStrut(10));
    buttonPanel.add(applyButton);
    buttonPanel.add(Box.createHorizontalStrut(10));
    tabPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e){
        okButton.requestFocus();
      }
    });
  }


  /**
   * Tell all interfaces that their possible changes were cancelled, remove the
   * tabs, and close the frame.
   */
  private void cancelButton_actionPerformed(ActionEvent e) {
    cancelDialog();
  }


  /**
   * Tell the interface that it's possible changes were applied
   * and keep the frame open.
   */
  private void applyButton_actionPerformed(ActionEvent e) {
    propagateApplyChanges();
  }

  /**
   * Tell the interface that it's possible changes were applied, remove the tabs,
   * and close the frame.
   */
  private void okButton_actionPerformed(ActionEvent e) {
    propagateApplyChanges();
    tabPane.removeAll();
    // This will clear out the panels and nuke the listeners.
    for (Iterator it = orderedEditorMap.values().iterator(); it.hasNext();) {
      ((PrefEditor)it.next()).dispose();
    }
    orderedEditorMap = new TreeMap(new MyComparator());
    mainDialog.hide();
    parentFrame.repaint();
  }


  /**
   * Go through each interface and call applyChanges for each one.
   */
  private void propagateApplyChanges() {
    String[] delayedApplication;

    for (int x=0; x<tabPane.getComponentCount();x++) {
       try {
         if (((PrefEditor)tabPane.getComponentAt(x)).hasChanged()) {
           delayedApplication=((PrefEditor)tabPane.getComponentAt(x)).applyChanges();
           if (delayedApplication!=null && delayedApplication.length>0) {
             java.util.List msgList=new ArrayList();
             msgList.add("The following changes from "+
               tabPane.getComponentAt(x).getName()+
               " will not take effect until the next session:");
             msgList.addAll(Arrays.asList(delayedApplication));
             JOptionPane.showMessageDialog(mainDialog,msgList.toArray());
           }
        }
      }
      catch (Exception ex) {SessionMgr.getSessionMgr().handleException(ex);};
    }
  }


  /**
   * Tell all interfaces that their possible changes were cancelled, remove the
   * tabs, and close the frame.
   */
  private void cancelDialog() {
    for (int x=0; x<tabPane.getComponentCount();x++) {
      if (((PrefEditor)tabPane.getComponentAt(x)).hasChanged())
        ((PrefEditor)tabPane.getComponentAt(x)).cancelChanges();
    }
    tabPane.removeAll();
    // This will clear out the panels and nuke the listeners.
    for (Iterator it = orderedEditorMap.values().iterator(); it.hasNext();) {
      ((PrefEditor)it.next()).dispose();
    }
    orderedEditorMap = new TreeMap(new MyComparator());
    mainDialog.hide();
    parentFrame.repaint();
  }


  /**
   * Helps to ensure good window placement.
   */
  private void centerDialog() {
    //Center the window
    Dimension frameSize = mainDialog.getSize();
    if (frameSize.height > screenHeight) {
      frameSize.height = screenHeight;
    } // Adjust for screen height.

    if (frameSize.width > screenWidth) {
      frameSize.width = screenWidth;
    } // Adjust for screen width.

    mainDialog.setLocation((screenWidth - frameSize.width)/2,
                         (screenHeight - frameSize.height)/2);
  }


  private class MyComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        return ((String)o1).compareTo((String)o2);
    }
  }

}