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
package client.tools;

import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class VersionChecker {

  public VersionChecker() {
    String version=System.getProperty("java.version");
    if (version.startsWith("1.3"))  System.exit(0);
    JFrame mainFrame = new JFrame();
    JOptionPane optionPane = new JOptionPane();
//    mainFrame.setIconImage((new ImageIcon(this.getClass().getResource("/resource/client/images/window_icon.gif")).getImage()));
    mainFrame.getContentPane().add(optionPane);

    if (!version.startsWith("1.3")) {
         if (version.startsWith("1.2")) {   //bad - 1.2, but not 1.2.2
             Object[] messages = new String[3];
             messages[0]="The system has detected that you are running Java version "+version;
             messages[1]="This system has been tested with version 1.3 and may have some unpredictable results under "+version;
             messages[2]="There are several known bugs in versions older than 1.3 that will negatively effect this program";
             optionPane.showMessageDialog(mainFrame, messages, "Warning!!", JOptionPane.WARNING_MESSAGE);
             System.exit(0);
         }
         else {  //really bad not 1.2
             new Dialog1(version).show();
         }
    }
  }

public class Dialog1 extends Dialog {
  Panel panel1 = new Panel();
  Label label1 = new Label();
  Label label2 = new Label();
  Label label3 = new Label();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  String version;

  public Dialog1(String version) {
    super(new Frame(), "ERROR!!", true);
    this.version=version;
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try  {
      jbInit();
      add(panel1);
      pack();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = getSize();
      if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;
      setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  void jbInit() throws Exception {
    panel1.setLayout(gridBagLayout1);
    GridBagConstraints c = new GridBagConstraints();
    label3.setText("This system REQUIRES java version 1.3 or newer in order to run properly.");
    label2.setText("This system has been tested with version 1.3.");
    label1.setText("The system has detected that you are running Java version "+version+".");
    Button button=new Button("OK");
    button.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent p0) {
             System.exit(1);
          }
    });
    c.gridwidth = GridBagConstraints.REMAINDER; //end row
    gridBagLayout1.setConstraints(label1, c);
    panel1.add(label1);
    c.gridwidth = GridBagConstraints.REMAINDER; //end row
    gridBagLayout1.setConstraints(label2, c);
    panel1.add(label2);
    c.gridwidth = GridBagConstraints.REMAINDER; //end row
    gridBagLayout1.setConstraints(label3, c);
    panel1.add(label3);
    c.gridwidth = GridBagConstraints.REMAINDER; //end row
    gridBagLayout1.setConstraints(button, c);
    panel1.add(button);
  }

  protected void processWindowEvent(WindowEvent e) {
    if(e.getID() == WindowEvent.WINDOW_CLOSING) {
      cancel();
    }
    super.processWindowEvent(e);
  }

  void cancel() {
    dispose();
  }
}

  public static void main(String[] args) {
    VersionChecker versionChecker = new VersionChecker();
    versionChecker.invokedStandalone = true;
  }
  private boolean invokedStandalone = false;
}