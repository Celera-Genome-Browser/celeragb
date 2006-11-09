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


/**
* Interface for objects which want to track the addition and
*   destruction of browser windows.
*
* Initially writted by: Peter Davies
*/
public interface SessionModelListener extends GenericModelListener {
    /**
    * The browserModel sent will be the Model associated with the
    * new browser.
    */
    public void browserAdded(BrowserModel browserModel);

    /**
    * The browserModel sent will be the Model associated with the
    * disposed Browser
    */
    public void browserRemoved(BrowserModel browserModel);

    /**
     * Called when the session is about to exit
     */
    public void sessionWillExit();
}
