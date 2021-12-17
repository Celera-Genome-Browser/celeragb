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
 * @author Peter Davies
 * @version $Id$
 */
package client.gui.framework.roles;

public interface PrefEditor {

  public static final String APPLICATION_SETTINGS="Application Settings";
  public static final String[] NO_DELAYED_CHANGES=new String[0];

  /**
   * @return String indicating while panel group this panel belongs to
   */
  public String getPanelGroup();

  /**
   * return the name that this panel should be called
   */
  public String getName();

  /**
   * These three methods are to provide hooks for the Controller in case
   * something panel-specific should happen when these buttons are pressed.
   */
  public void cancelChanges();

  /**
   * @return An array of strings indicating which changes will take effect next session
   */
  public String[] applyChanges();


  /**
   * This method helps the Pref Controller to know if the panel needs to have it's
   * changes applied or not.
   */
  public boolean hasChanged();

  /**
   * @return a Description of this panel to be used as a tool tip on the tab
   */
  public String getDescription();

  /**
   * This should be used to force the panels to de-register themelves from the
   * PrefController.
   */
  public void dispose();

}