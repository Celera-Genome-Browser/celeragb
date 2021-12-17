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
package client.gui.other.dialogs;

import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.SessionMgr;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

public class PropertyDialogBase extends JDialog {

  protected BrowserModel browserModel;
  protected String propertyName;



  public PropertyDialogBase(BrowserModel browserModel, String propertyName) {
    super(SessionMgr.getSessionMgr().getBrowserFor(browserModel),true);
    this.browserModel=browserModel;
    this.propertyName=propertyName;
//    this.setIconImage((new ImageIcon(this.getClass().getResource(System.getProperty("x.genomebrowser.WindowCornerLogo"))).getImage()));
  }

  protected void position() {
    pack();

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = getSize();
    if (frameSize.height > screenSize.height)
        frameSize.height = screenSize.height;
    if (frameSize.width > screenSize.width)
        frameSize.width = screenSize.width;
    setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

  }

 /**
 * Overriden so we can dispose resouces just like the OK button
 */
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      dismissDialog();
    }
  }

  public void dismissDialog()
  {
    setVisible(false);
    dispose();
  }
}