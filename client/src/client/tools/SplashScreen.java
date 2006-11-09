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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
* This class gives a SplashScreen for the application.  *
*
* Initially writted by: Peter Davies
*
*/

public class SplashScreen extends JWindow implements ProgressDisplayer {

  JPanel outerPanel=new JPanel();
  JProgressBar progressBar = null;
  JLabel progressLabel = null;
  int lastVal = 0;

  public SplashScreen() {
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try  {
      jbInit();
    }
    catch (Exception e) {
      try {
        client.gui.framework.session_mgr.SessionMgr.getSessionMgr().handleException(e);
      }
      catch (Exception ex1) {e.printStackTrace();}
    }
    pack();
  }

  private JPanel getProgressBar() {
    JPanel progress = new JPanel();
    progressLabel = new JLabel("          ");
    Font font = new Font("Arial", Font.PLAIN, 10);
    progressLabel.setFont(font);
    progress.setLayout(new BorderLayout());
    progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100) {
	    public Dimension getPreferredSize() {
		return new Dimension(300, super.getPreferredSize().height);
	    }
	};
    progress.add(progressBar, BorderLayout.CENTER);
    progress.add(progressLabel, BorderLayout.SOUTH);
    progressBar.setStringPainted(true);
    progressBar.setMaximumSize(new Dimension(250,8));
    progressBar.setPreferredSize(new Dimension(250,8));
    progressBar.setMinimumSize(new Dimension(250,8));
    progressBar.setForeground(Color.yellow);
    progressBar.setBackground(Color.black);
    progress.setBackground(Color.white);

    return progress;
  }


  private void jbInit() throws Exception  {
    getContentPane().setBackground(Color.white);
    outerPanel.setLayout(new BorderLayout());
    outerPanel.add(new SplashPanel(),BorderLayout.CENTER);
    outerPanel.setBorder(new BevelBorder(BevelBorder.RAISED,Color.lightGray,Color.darkGray));
    outerPanel.add(getProgressBar(),BorderLayout.SOUTH);
    getContentPane().add(outerPanel);
    pack();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = getSize();
    if (frameSize.height > screenSize.height)
       frameSize.height = screenSize.height;
    if (frameSize.width > screenSize.width)
       frameSize.width = screenSize.width;
    setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
  }


  protected void processWindowEvent(WindowEvent e) {
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      cancel();
    }
    super.processWindowEvent(e);
  }

  void cancel() {
    dispose();
  }

  public void setLabel(String txt) {
    progressLabel.setText(txt);
  }
  public void setProgress(int cur, int max) {
    int val = (int)(((float)cur/max)*100.0);
    for(int i = lastVal; i <= val; i++) {
      progressBar.setValue(i);
      try { Thread.sleep(2); } catch(Exception e){}
    }
    lastVal = val;
  }
}

