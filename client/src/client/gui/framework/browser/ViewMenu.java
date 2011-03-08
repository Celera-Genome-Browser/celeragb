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

import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.genetics.Chromosome;
import api.entity_model.model.genetics.Species;
import client.gui.framework.session_mgr.SessionMgr;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
* This class provides the File menu for the Browser.  It has been
* externalized here to allow subclassing and overridding for modification.
* This can be currently done by subclassing Browser and overridding setFileMenu.
* Something cleaner should be worked out in the long run.
*
* Initially written by: Peter Davies
*/

public class ViewMenu extends JMenu {

  private static final String DISPLAY_SUB_EDITOR_LABEL = "Display SubViews When Available";

  private Browser browser;
  private List viewSelectionMenuItems=new ArrayList();

  private List propToggleMenuItems=new ArrayList();
  private ActionListener drillDownMenuListener=new DrillDownMenuActionListener();

  private ActionListener propToggleMenuListener=new PropToggleMenuActionListener();
  //private ActionListener editorMenuListener=new EditorMenuActionListener();

  private ButtonGroup editorMenuButtonGroup=new ButtonGroup();
  private JSeparator separator1=new JSeparator();

  private JMenuItem toggleOutlineMI=new JMenuItem("Toggle Outline",'O');
  private JCheckBoxMenuItem subViewMI = new JCheckBoxMenuItem(DISPLAY_SUB_EDITOR_LABEL);

  private JMenuItem mainViewMenu=new JMenuItem();

  public ViewMenu(Browser browser) {
    this.browser=browser;
    setText("Views");
    this.setMnemonic('V');
    browser.addBrowserObserver(new ViewMenuBrowserObserver());
    toggleOutlineMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK, false));
    toggleOutlineMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        boolean tmpOutlineState = ViewMenu.this.browser.isOutlineCollapsed();
        ViewMenu.this.browser.setView(!tmpOutlineState);
        setViewType();
      }
    });

    subViewMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK, false));
    this.addMenuListener(new javax.swing.event.MenuListener() {
      public void menuCanceled(MenuEvent e) {}
      public void menuDeselected(MenuEvent e) {}
      public void menuSelected(MenuEvent e) {
        this_menuSelected();
      }
    });
    establishFrameControlButtons();
    createPropToggleItems();
  }


  private void setViewType() {
    establishFrameControlButtons();
    createPropToggleItems();
  }


  private void selectCurrentEditor(String newMasterEditor) {
    for (int i=0;i<getItemCount();i++)
      if (getItem(i)!=null && getItem(i).getText().equals(newMasterEditor)) {
        getItem(i).setSelected(true);
      }
  }


  private void createPossibleDrillDownItems() {
    for (int i=0;i<viewSelectionMenuItems.size();i++) {
      remove((JMenuItem)viewSelectionMenuItems.get(i));
      editorMenuButtonGroup.remove((JMenuItem)viewSelectionMenuItems.get(i));
    }

    viewSelectionMenuItems.clear();
    remove(separator1);
    mainViewMenu.removeActionListener(drillDownMenuListener);

    Hashtable possibleDrillDownEditors = browser.getEditorNameToConstructorRegistry();
    if (possibleDrillDownEditors!=null || possibleDrillDownEditors.size()>0) {
      for (Iterator it = possibleDrillDownEditors.keySet().iterator();it.hasNext();) {
        String tmpName = (String)it.next();
        // should show the menu items in if the current selection is not Species or Chromosome
        if(tmpName.equals("Genomic Axis Annotation") && (!(browser.getBrowserModel().getCurrentSelection() instanceof Species)
            && !(browser.getBrowserModel().getCurrentSelection() instanceof Chromosome))
            && (browser.getBrowserModel().getCurrentSelection()!=null) ){
          mainViewMenu.setText(tmpName);
          viewSelectionMenuItems.add(mainViewMenu);
          mainViewMenu.addActionListener(drillDownMenuListener);
          add(mainViewMenu);
          add(separator1);
          add(toggleOutlineMI);
          mainViewMenu.setSelected(true);
          editorMenuButtonGroup.add(mainViewMenu);
         }
        else{
          mainViewMenu.setSelected(false);
          toggleOutlineMI.setSelected(false);
        }
      }
    }
    establishFrameControlButtons();
  }


  private void establishFrameControlButtons() {
    // Add the frame control buttons no matter what.
    add(toggleOutlineMI);
  }

  /** Establish property toggle menu items. */
  private void createPropToggleItems() {
    // Remove any OLD menu items.
    for (int i = 0; i < propToggleMenuItems.size(); i++) {
      remove((JMenuItem)propToggleMenuItems.get(i));
    }
    propToggleMenuItems.clear();

    // Establish a menu item that lets the user turn on or off the
    // sub menus property.
    Boolean oldSetting = (Boolean)SessionMgr.getSessionMgr().getModelProperty(SessionMgr.DISPLAY_SUB_EDITOR_PROPERTY);
    subViewMI.setSelected(oldSetting.booleanValue());

    subViewMI.addActionListener(propToggleMenuListener);
    add(subViewMI);
    propToggleMenuItems.add(subViewMI);
  }


  private void this_menuSelected() {
     createPossibleDrillDownItems();
     createPropToggleItems();
  }


  class ViewMenuBrowserObserver extends BrowserObserverAdapter {
    public void masterEditorChanged(String newMasterEditor, boolean subEditorAvailable){
      selectCurrentEditor(newMasterEditor);
    }
  }


  class EditorMenuActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
     browser.setMasterEditor(e.getActionCommand());
   }
  }


  class DrillDownMenuActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
       GenomicEntity ge=browser.getBrowserModel().getCurrentSelection();
       if (ge == null) return;
       browser.drillDownToEntityUsingThisEditor(mainViewMenu.getText(), ge);

       // This is a kludge to get the browser to properly repaint in JDK 1.4
       boolean isOutlineCollapsed = browser.isOutlineCollapsed();
       browser.setView(!isOutlineCollapsed);
       browser.setView(isOutlineCollapsed);

       browser.repaint();
   }
  }


  /**
   * Listener to property toggle menu item selects.  Such items
   * are presented so the user may change the value of a boolean
   * model property.
   */
  class PropToggleMenuActionListener implements ActionListener {
    public void actionPerformed(ActionEvent ae) {
      JMenuItem item = (JMenuItem)ae.getSource();
      if (item.getActionCommand().equals(DISPLAY_SUB_EDITOR_LABEL)) {
        Boolean newSetting = new Boolean(item.isSelected());
        SessionMgr.getSessionMgr().setModelProperty(SessionMgr.DISPLAY_SUB_EDITOR_PROPERTY, newSetting);
      }
       // Got sub editor
    } // End method
  } // End class
}