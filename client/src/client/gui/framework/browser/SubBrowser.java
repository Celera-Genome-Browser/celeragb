// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package client.gui.framework.browser;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.event.*;
import java.awt.Component;
import client.gui.framework.session_mgr.*;
import client.gui.framework.roles.SubEditor;
import api.entity_model.model.fundtype.GenomicEntity;

public class SubBrowser extends JTabbedPane {

// JCVI LLF: 10/19/2006
//  static ImageIcon icon = null;
  
	// RT 10/27/2006
  static ImageIcon icon=new ImageIcon(
     SubBrowser.class.getResource("/resource/client/images/jclpopup.gif"));

  private List components=new ArrayList();
  private BrowserModel model;
  private boolean changingTabs;
  private SubEditor selectedEditor;


  public SubBrowser(BrowserModel model) {
    try  {
      this.model=model;
      model.addBrowserModelListener(new MyBrowserModelListener());
      getModel().addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e){
          updateViews();
        }
      });
      jbInit();
    }
    catch(Exception e) {
      try{
         client.gui.framework.session_mgr.SessionMgr.getSessionMgr().handleException(e);
      }
      catch (Exception ex1) {e.printStackTrace();}
    }
  }

  public Component add(Component component) {
      if (component instanceof SubEditor) {
         components.add(component);
         setSubViews(model.getCurrentSelection());
      }
      return component;
  }

  private void jbInit() throws Exception {
    this.addMouseListener(new MouseAdapter() {

      public void mouseClicked(MouseEvent e) {
        this_mouseClicked(e);
      }
    });
  }

  void this_mouseClicked(MouseEvent e) {
    if ((e.getModifiers()&MouseEvent.BUTTON3_MASK)==MouseEvent.BUTTON3_MASK) {
      if (getSelectedComponent() instanceof SubEditor) {
         JPopupMenu popup=new JPopupMenu();
         JMenuItem[] menus=((SubEditor)getSelectedComponent()).getMenus();
         if (menus==null) return;
         for (int i=0;i<menus.length;i++) {
           if (menus[i]!=null) {
            popup.add(menus[i]);
           }
         }
         popup.show(this,(int)e.getPoint().getX(),(int)e.getPoint().getY());
      }
    }
  }

  public void remove(Component component) {
    changingTabs=true;
    if (components.contains(component)) {
       ((SubEditor)component).dispose();
       super.remove( component);
    }
    changingTabs=false;
    updateViews();
  }

  public void removeAll() {
    changingTabs=true;
    SubEditor sub;
    for (Iterator it=components.iterator();it.hasNext();) {
       sub = (SubEditor) it.next();
       sub.dispose();
    }
    components.clear();
    super.removeAll();
    changingTabs=false;
    updateViews();
  }

  private void setSubViews (GenomicEntity entity) {
     changingTabs=true;
     Component oldComponent = getSelectedComponent();
     Component[] currentComponents=getComponents();
     for (int i=0;i< currentComponents.length;i++) {
       if (!((SubEditor)currentComponents[i]).canEditThisEntity(entity)) super.remove(currentComponents[i]);
     }
     currentComponents=getComponents();
     List currentComponentList=Arrays.asList(currentComponents);

     //  Add the tabs here, but make it alphabetical.
     for (int i=0;i<components.size();i++) {
        if ( ((SubEditor)components.get(i)).canEditThisEntity(entity) && !currentComponentList.contains(components.get(i)))
           addTab((SubEditor)components.get(i));
     }

     // Defaults to the defined SubView if no entity is selected or the current SubView
     // is removed.
     int defaultIndex = 0;
     String targetDefaultSubView = (String)model.getModelProperty("DefaultSubViewName");
     for (int x = 0; x<getTabCount(); x++) {
        if (getTitleAt(x).equals(targetDefaultSubView))
          defaultIndex=x;
     }
     if (entity == null && this.getTabCount()>0) this.setSelectedIndex(defaultIndex);
     if (!currentComponentList.contains(oldComponent) && this.getTabCount()>0)
      this.setSelectedIndex(defaultIndex);

     doLayout();
     changingTabs=false;
     updateViews();
  }


  private void addTab (SubEditor subEditor) {
    changingTabs=true;

    // Below tries to place the added tab alphabetically.
    int targetTabIndex = getTabCount();
    for (int x=0; x<getTabCount(); x++) {
      if (subEditor.getName().compareToIgnoreCase(getTitleAt(x))<0) targetTabIndex = x;
    }

    JMenuItem[] menus=subEditor.getMenus();
    if (menus!=null && (menus.length>0)) {
       super.insertTab(subEditor.getName(),icon,(Component)subEditor,
         "Right Mouse Click for "+subEditor.getName()+" Menu", targetTabIndex);
    }
    else super.add((Component)subEditor, targetTabIndex);
    changingTabs=false;
    updateViews();
  }

  private void updateViews() {
    Component selected=getSelectedComponent();
    if (selectedEditor==selected) return;
    if (!changingTabs && selected!=null) {
      //  Some of the subviews expect passivate of the old before activation of the new.
      if (selectedEditor!=null) {
        selectedEditor.passivate();
      }

      ((SubEditor)selected).activate();

      selectedEditor=(SubEditor)selected;
    }
  }


  private class MyBrowserModelListener extends BrowserModelListenerAdapter {
    public void browserCurrentSelectionChanged(GenomicEntity newSelection) {
        setSubViews(newSelection);
    }
  }
}