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
package client.tools.installer;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.Hashtable;
import client.tools.ProgressDisplayer;
import client.launcher.Launcher;
import client.launcher.InstallException;

/**
 * Title:
 * Description:
 * Company:
 * @author
 * @version 1.0
 */

public class WizardController extends JDialog {
  private int currentScreen = 0;
  private int totalScreens = 0;
  private String userName = null;
  private String password = null;
  private boolean installDocumentationBool = false;
  private boolean installXmlDemoFilesBool = false;
  private String installDir = null;
  private boolean launch = false;
  Vector screens = new Vector();
  CardLayout layout = null;
  Frame background = null;
  public Hashtable passThruArgs = null;
  ProgressDisplayer progressBar = null;

  public WizardController( Frame bg ) {
    super(bg,true);
    background = bg;
    init();
  }

  private void init() {
    layout = new CardLayout();
    this.getContentPane().setLayout(layout);
    int width = 600, height = 400;
    Dimension screenBounds = this.getToolkit().getScreenSize();
    this.setLocation((screenBounds.width/2)-(width/2), (screenBounds.height/2)-(height/2));
    this.setSize(width,height);
    this.setResizable(false);
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    this.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        BaseScreen bs = (BaseScreen)screens.elementAt(currentScreen);
        bs.cancelButton_actionPerformed(null);
      }
    });

  }

  private void addScreen( BaseScreen screen ) {
    String title = screen.getTitle();
    System.out.println("Title[" + totalScreens +"] = '" + title +"'");
    screens.addElement(screen);
    this.getContentPane().add(screen,title);
    this.totalScreens++;
  }

  public void showFirst() {
    BaseScreen bs = (BaseScreen)screens.firstElement();
    this.setTitle(bs.getTitle());
    layout.show(this.getContentPane(),bs.getTitle());
  }

  public void showNext() {
    currentScreen++;
    BaseScreen bs = (BaseScreen)screens.elementAt(currentScreen);
    this.setTitle(bs.getTitle());
    layout.show(this.getContentPane(),bs.getTitle());
  }

  public void showPrev() {
    currentScreen--;
    BaseScreen bs = (BaseScreen)screens.elementAt(currentScreen);
    this.setTitle(bs.getTitle());
    layout.show(this.getContentPane(),bs.getTitle());
  }

  public void cancel() {
  }

  public void setUserLoginAndPassword(String userName, String password){
    this.password = password;
    this.userName = userName;
  }

  public void setDownloadDoc( boolean docs ) {
    this.installDocumentationBool = docs;
  }

  public void setDownloadXml( boolean xml ) {
    this.installXmlDemoFilesBool = xml;
  }

  public void setLaunch( boolean launch ) {
    this.launch = launch;
  }

  public boolean getLaunch() {
    return launch;
  }


  public void setInstallDirectory( String dir ) {
    installDir = dir;
  }

  public void setProgressMeter( ProgressDisplayer pd ) {
    progressBar = pd;
  }

  public void cancelInstall() {
    setVisible(false);
    dispose();
    background.setVisible(false);
    background.dispose();
  }

  public void runWizard() {
    addScreen(new WelcomeScreen(this));
    addScreen(new LicenseScreen(this));
    addScreen(new DestinationScreen(this));
    //addScreen(new ComponentScreen(this));
    //addScreen(new UserInfoScreen(this));
    //Installing screen??
    addScreen(new CompleteScreen(this));

    showFirst();

    setVisible(true);
  }

  public void setPassThruArgs( Hashtable args ) {
    passThruArgs = args;
  }


  public void performInstall() throws InstallException {
    System.out.println("Performing Installation...\n");
    System.out.println("installDir="+installDir+"\n"+
                       "userName="+userName+"\n"+
                       "password="+password+"\n"+
                       "documentOption="+installDocumentationBool+"\n"+
                       "xmlOption="+installXmlDemoFilesBool+
                       "\n\nLaunch="+launch);

    System.out.println("");

    //Add the known parameters to the args list...
    passThruArgs.put("installdir", installDir);
    passThruArgs.put("username",(userName==null?"":userName));
    passThruArgs.put("password",(password==null?"":password));
    passThruArgs.put("documentoption",installDocumentationBool?"true":"false");
    passThruArgs.put("xmloption",installXmlDemoFilesBool?"true":"false");
    passThruArgs.put("launch",launch?"true":"false");
    passThruArgs.put("progressbar",progressBar);

    //Execute the tools.jar code
    Launcher launcher = new Launcher();

    launcher.launch(passThruArgs);
  }
}
