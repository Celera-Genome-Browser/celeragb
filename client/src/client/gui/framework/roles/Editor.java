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

package client.gui.framework.roles;

import api.entity_model.model.fundtype.GenomicEntity;

import javax.swing.*;

/**
* This interface must be implemented by all Editors that will be displayed
* by the browser.  In addition, all Editors must also be some sub-class
* of java.awt.Component (as tested by instanceof).  This will allow them to be
* displayed.  Should the Editor not pass this test, an exception will be
* thrown.  Editors must also have a constructor that accepts a client.gui.framework.browser.Browser
* and a Boolean (class) indicating whether they are the master browser.
* Again, this will be forced by exception. This constructor should query the Browser for
* it's BrowserModel as it will not be passed any other way.
*
* Editors probably will want to implement BrowserModelListener as well.
* but since it is possible that they may not want to, this interface was left
* separate. For usage information, look at test.access.test_editor.TestEditor
* as an example.
*
*
* Initially written by: Peter Davies
*/
public interface Editor {

  /**
   * Must return true for the classes that can be viewed/edited by this subEditor.  This
   * is used to determine which views can view the system selection.
   */
  public boolean canEditThisEntity(GenomicEntity entity);

  /**
  * This method will be called when removing an Editor from a browser, except when the
  * Browser as a whole closes (the Editor will receive a browserClosing callback if listening
  * to the BrowserModel in this case).  This allows Editors to do any clean-up work. Note:  if
  * you are registered with the BrowserModel, you must deregister.
  */
  public void dispose();

  /**
  *  Return an array of JMenus that should be setup for this editor.  Return null to
  * indicate no menus
  */
  public JMenuItem[] getMenus();

  /**
   * Implemented by Component, but also needed here
   */
  public String getName();

}
