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

package client.gui.framework.session_mgr;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

/**
* The SessionModel manages BrowserModels, as well as providing the API
* for event registeration/deregistration for the BrowserModels.  This
* class follows the Singleton pattern (Gamma, et al.).  As such, you
* must get the instance using SessionModel.getSessionModel().
*
* Initially writted by: Peter Davies
*/

public class SessionModel extends GenericModel {
  private static SessionModel sessionModel=new SessionModel();
  private Vector browserModels=new Vector(10);

  private SessionModel() {
    super();
    browserModels=new Vector(10);
  }  //Singleton pattern enforcement --PED 5/13

  static SessionModel getSessionModel() {return sessionModel;} //Only the SessionManager should have direct access.

  BrowserModel addBrowserModel() {
     BrowserModel browserModel=new BrowserModel();
     browserModels.addElement(browserModel);
     fireBrowserAdded(browserModel);
     return browserModel;
  }

  void addBrowserModel(BrowserModel browserModel) {
     browserModels.addElement(browserModel);
     fireBrowserAdded(browserModel);
  }

  /**
  * Exit the application if the last browserModel is removed
  */

  public void removeBrowserModel(BrowserModel browserModel) {
    browserModels.removeElement(browserModel);
    browserModel.dispose();
    fireBrowserRemoved(browserModel);
    if (browserModels.isEmpty()) SessionMgr.getSessionMgr().systemExit();
  }

  /**
  * Exit the application with full notification
  */
  public void removeAllBrowserModels() {
    BrowserModel browserModel;
    for (Enumeration e=browserModels.elements();e.hasMoreElements(); ) {
      browserModel=(BrowserModel)e.nextElement();
      browserModel.dispose();
      fireBrowserRemoved(browserModel);
    }
  }

  public void addSessionListener(SessionModelListener sessionModelListener) {
    for (Enumeration e=browserModels.elements();e.hasMoreElements();)
       sessionModelListener.browserAdded((BrowserModel)e.nextElement());
    if (!modelListeners.contains(sessionModelListener)) modelListeners.add(sessionModelListener);
  }

  public void removeSessionListener(SessionModelListener sessionModelListener) {
    modelListeners.remove(sessionModelListener);
  }

  public int getNumberOfBrowserModels() {
    return this.browserModels.size();
  }

  public void systemWillExit() {
     removeAllBrowserModels();
     fireSystemExit();
  }

//  void loadProgressMeterStateChange(boolean on) {
//     fireLoadProgressStateChange(on);
//  }

  private void fireBrowserRemoved(BrowserModel browserModel) {
    for (Iterator e=modelListeners.iterator();e.hasNext();)
       ((SessionModelListener)e.next()).browserRemoved(browserModel);
  }

  private void fireBrowserAdded(BrowserModel browserModel) {
    for (Iterator e=modelListeners.iterator();e.hasNext();)
       ((SessionModelListener)e.next()).browserAdded(browserModel);
  }

  private void fireSystemExit() {
    for (Iterator e=modelListeners.iterator();e.hasNext();)
       ((SessionModelListener)e.next()).sessionWillExit();
  }

}
