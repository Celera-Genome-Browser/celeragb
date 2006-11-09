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

package client.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This class gives a SplashScreen for the application.  *
 *
 * Initially writted by: Peter Davies
 *
 */

public class SplashPanel extends JPanel {
  JPanel panel1 = new JPanel();
  JPanel panel2 = new JPanel();
  static ImageIcon logo = new ImageIcon(SplashScreen.class.getResource("/resource/client/images/gb_splash_logo_sm.jpg"));
  JLabel logoImageControl = new JLabel(logo);

  public SplashPanel() {
    try  {
      jbInit();
    }
    catch (Exception e) {
      try {
        client.gui.framework.session_mgr.SessionMgr.getSessionMgr().handleException(e);
      }
      catch (Exception ex1) {e.printStackTrace();}
    }
    logoImageControl.setIcon(logo);
  }

  private void jbInit() throws Exception  {
    panel1.setLayout(new BoxLayout(panel1,BoxLayout.X_AXIS));
    panel1.setBackground(Color.white);
    panel2.setLayout(new BoxLayout(panel2,BoxLayout.X_AXIS));
    panel2.setBackground(Color.white);
    setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    panel2.add(logoImageControl);
    setBackground(Color.white);
    add(panel1);
    add(panel2);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = getSize();
    if (frameSize.height > screenSize.height)
       frameSize.height = screenSize.height;
    if (frameSize.width > screenSize.width)
       frameSize.width = screenSize.width;
    setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
  }
}

