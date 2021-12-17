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

package client.gui.framework.ics_tabpane;

import api.entity_model.model.fundtype.GenomicEntity;
import client.gui.framework.browser.Browser;
import client.gui.framework.inspector.Inspector;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.BrowserModelListenerAdapter;
import client.gui.framework.session_mgr.SessionMgr;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ICSTabPane extends JTabbedPane {

  BrowserModel browserModel;
  List inspectors = new ArrayList();
  JScrollPane inspectorPane;
  boolean curationState=true;
  String watchedProperty = "";

  public ICSTabPane(Browser browser) {
     Inspector inspector = new Inspector(this);
     inspectorPane=inspector.getScrollPane();
     inspectors.add(inspector);
     browserModel=browser.getBrowserModel();
     try {
      watchedProperty = (String) SessionMgr.getSessionMgr().getModelProperty("PropertyForInspectorToWatch");
     }
     catch (Exception ex) { watchedProperty="";}
     add("No Selection",inspectorPane);
     browserModel.addBrowserModelListener(new BrowserModelObserver());
  }

  public boolean getCurationState() {
    return curationState;
  }

  public void addInspector(Inspector inspector) {
    if (!inspectors.contains(inspector)) inspectors.add(inspector);
  }

  public void removeInspector(Inspector inspector) {
    inspectors.remove(inspector);
  }

  public BrowserModel getBrowserModel() {
     return browserModel;
  }

  private class BrowserModelObserver extends BrowserModelListenerAdapter {
    public void browserCurrentSelectionChanged(GenomicEntity newSelection) {
         for (int x=0;x<inspectors.size();x++) {
            ((Inspector)inspectors.get(x)).setModel(newSelection);
          }
    }

    public void modelPropertyChanged(Object key, Object oldValue, Object newValue){
      if (key.equals(watchedProperty)) {
        boolean value = ((Boolean)newValue).booleanValue();
        curationState=value;
        for (int x=0; x<inspectors.size();x++) {
          ((Inspector)inspectors.get(x)).setCurationChanged(value);
        }
      }
    }
  }
}