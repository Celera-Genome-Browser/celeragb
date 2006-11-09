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
package client.gui.framework.browser;

import javax.swing.*;
import client.gui.framework.session_mgr.*;
import java.awt.event.*;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

public class WindowMenu extends JMenu {

  private Browser browser;
  private JMenuItem menuFileNewBrowser,menuFileClose;

  public WindowMenu(Browser browser) {
     this.browser=browser;
     setText("Window");
     this.setMnemonic('W');
     menuFileNewBrowser=new JMenuItem("New Browser Window",'N');
     menuFileNewBrowser.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
          fileNewBrowser_actionPerformed(e);
        }
     });

     menuFileClose=new JMenuItem("Close Browser Window",'C');
     menuFileClose.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
          fileClose_actionPerformed(e);
        }
     });
     disableClose();
     browser.addBrowserObserver(new FileMenuBrowserObserver());
     add(menuFileNewBrowser);
     add(menuFileClose);
  }

  private void fileNewBrowser_actionPerformed(ActionEvent e) {
	JOptionPane.showMessageDialog(null, "The new Genome Browser window can only used to view data. It cannot be used to save annotation results.", "Open New Browser Window", JOptionPane.OK_OPTION); 
    SessionMgr.getSessionMgr().cloneBrowser(browser);
  }

  private void fileClose_actionPerformed(ActionEvent e) {
    SessionMgr.getSessionMgr().removeBrowser(browser);
  }

  private void disableClose() {
    menuFileClose.setEnabled(false);
  }

  private void enableClose() {
    menuFileClose.setEnabled(true);
  }

 class FileMenuBrowserObserver extends BrowserObserverAdapter {
     public void openBrowserCountChanged(int browserCount){
       if (browserCount>1) enableClose();
       else disableClose();
     }
  }

}