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
package client.gui.framework.roles;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version      $Id$
 *
 * Must be implemeted by any View that is used as a subEditor
 */


public interface SubEditor extends Editor {

  /**
   * A notice sent to the component that it has been brought to the top of the tabs.
   *
   * This method is necessary because it has been shown that componentHidden and
   * componentShown are not reliably called when components are dynamically put
   * on a tab pane and can move between tabs.  One specific case is if you have
   * 6 tabs and number 4 is selected.  You then remove 4-6, and add a new
   * tab, resulting in a new tab 4.  If this is done in conjunction with calls
   * to setVisible(false) and setVisible(true) to hide the transition effect, the
   * model selection is not changed (the index is still 4)
   * and as a side effect, the componentShown is
   * not called on the new tab.  This was one of several noted cases where using
   * componentHidden/componentShown was problematic.
   */
  public void activate();

  /**
   * A notice sent to the component that it has been removed from the top of the tabs.]
   *
   * This method is necessary because it has been shown that componentHidden and
   * componentShown are not reliably called when components are dynamically put
   * on a tab pane and can move between tabs.  One specific case is if you have
   * 6 tabs and number 4 is selected.  You then remove 4-6, and add a new
   * tab, resulting in a new tab 4.  If this is done in conjunction with calls
   * to setVisible(false) and setVisible(true) to hide the transition effect, the
   * model selection is not changed (the index is still 4)
   * and as a side effect, the componentShown is
   * not called on the new tab.  This was one of several noted cases where using
   * componentHidden/componentShown was problematic.
   */
  public void passivate();
}