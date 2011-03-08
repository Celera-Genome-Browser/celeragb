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

import client.launcher.InstallException;
import client.tools.ProgressDisplayer;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Title:
 * Description:
 * Company:
 * @author
 * @version 1.0
 */

public class CompleteScreen extends BaseScreen {
  private JPanel mainPanel = new JPanel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private static final String title = "Setup Complete";
  private JPanel buttonPanel = new JPanel();
  private JButton nextButton = new JButton();
  private JButton cancelButton = new JButton();
  private JButton backButton = new JButton();
  private Component backScreen = null;
  private JCheckBox launchCheckBox = null;
  private final int LAUNCH_NOW = 0;
  private final int EXIT_NOW = 1;


  public CompleteScreen(WizardController controller) {
    super(title, controller);
    try {
      jbInit();
      int width = 600, height = 400;
      this.setSize(width,height);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  void jbInit() throws Exception {
    mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

    Component hstrut = Box.createHorizontalStrut(40);
    Component vstrut = Box.createVerticalStrut(50);

    Font f = new Font("Times New Roman", Font.PLAIN, 12);

    JTextArea jt = new JTextArea();
    jt.setFont(f);
    jt.setEditable(false);
    jt.setHighlighter(null);
    jt.setCursor(null);
    jt.setBackground(this.getBackground());
    jt.setForeground(Color.black);
    jt.setText("Select whether you would like the application to launch after" + "\n" +
               "the installation.  After this install is completed the application" + "\n" +
               "will be available through a link on your desktop." + "\n" +
               "\nClick the Finish button to complete this Setup.");
    jt.setLineWrap(true);
    jt.setColumns(34);

    JLabel jl = new JLabel(icon);

    JPanel progress = new ProgressMeter();
    master.setProgressMeter( (ProgressDisplayer) progress);
    progress.setVisible(false);

    JPanel westSector = new JPanel();
    westSector.setLayout(new BorderLayout());
    westSector.add(jl, BorderLayout.NORTH);
    westSector.add(progress,BorderLayout.SOUTH);

    JPanel eastSector = new JPanel();
    eastSector.setLayout(new BorderLayout());
    eastSector.add(jt, BorderLayout.NORTH);

    master.setLaunch(false);
    JPanel optionsPanel = new JPanel();
    optionsPanel.setLayout(new GridLayout(1,1));

    eastSector.add(vstrut,BorderLayout.CENTER);
    eastSector.add(optionsPanel,BorderLayout.SOUTH);

    mainPanel.add(Box.createHorizontalStrut(5));
    mainPanel.add(westSector);
    mainPanel.add(Box.createHorizontalStrut(10));
    mainPanel.add(eastSector);
    mainPanel.add(Box.createHorizontalStrut(5));

    //Initialize mainPanel
    mainPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    this.add(mainPanel, BorderLayout.CENTER);

    backButton.setText("< Back");
    backButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        backButton_actionPerformed(e);
      }
    });
    nextButton.setText("Finish");
    nextButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        nextButton_actionPerformed(e);
      }
    });
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButton_actionPerformed(e);
      }
    });

    //Initialize button panel
    buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    this.add(buttonPanel, BorderLayout.SOUTH);
    buttonPanel.add(backButton);
    buttonPanel.add(nextButton);
    buttonPanel.add(hstrut);
    buttonPanel.add(cancelButton);
  }

  public void nextButton_actionPerformed(ActionEvent e) {
    this.setCursor( new Cursor(Cursor.WAIT_CURSOR) );
    //Call the installer
    Runnable run = new Runnable() {
      public void run() {
        try {
          master.performInstall();
          finalLaunchDialog();
        }
        catch( InstallException ie ) {
          showLaunchErrorDialog(ie.getMessage());
        }
      }
    };
    Thread t = new Thread(run);
    t.start();
  }

  private void showLaunchErrorDialog( String msg ) {
    String composedMsg = msg+"\n\n"+"Please contact our support team about this issue."+"\n"+
                         "See the User Guide for Technical Support contact information.";

    JOptionPane.showMessageDialog(this,composedMsg,"Installation Error!",JOptionPane.ERROR_MESSAGE);
    master.cancelInstall();
  }

  public void finalLaunchDialog() {
    Object[] message = new Object[1];
    message[0] = "Installation Complete.  Would you like to"+"\n"+"launch the Application?";

    String[] buttons = {"Launch Now!","Exit without Launching"};
    int result = JOptionPane.showOptionDialog(
                    this,                            // the parent that the dialog blocks
                    message,                         // the dialog message array
                    "Launch option",                 // the title of the dialog window
                    JOptionPane.DEFAULT_OPTION,      // option type
                    JOptionPane.INFORMATION_MESSAGE, // message type
                    null,                            // optional icon, use null to use the default icon
                    buttons,                         // options string array, will be made into buttons
                    null                             // option that should be made into a default button
                );

    System.out.println("result of 'Launch Now' Dialog box: "+ result);
    master.setLaunch(false);

    //now launch if selected...
    if( result == LAUNCH_NOW ) {
      master.setLaunch(true);
      try {
        master.performInstall();
      }
      catch( InstallException ie ) {
        this.showLaunchErrorDialog(ie.getMessage());
      }
    }

    master.setVisible(false);
    master.dispose();
    master.background.setVisible(false);
    master.background.dispose();
  }
}

class ProgressMeter extends JPanel implements ProgressDisplayer {

  private JProgressBar progressBar = null;
  private JLabel progressLabel = null;
  private int lastVal = 0;
  private Font f = new Font("Times New Roman", Font.PLAIN, 10);

//  public void setVisible(boolean true1)
  public void setLabel(String txt) {
    progressLabel.setText(txt);
  }

  public void setProgress(int cur, int max) {
    int val = (int)(((float)cur/max)*100.0);
    for(int i = lastVal; i <= val; i++) {
      progressBar.setValue(i);
      try { Thread.sleep(3); } catch(Exception e){}
    }
    lastVal = val;
  }

  public void dispose() {
    this.setVisible(false);
  }

  public ProgressMeter() {
    super();
    init();
  }

  private void init() {
    progressLabel = new JLabel("          ");
    progressLabel.setFont(f);
    setLayout(new BorderLayout());
    progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100) {
        public Dimension getPreferredSize() {
            return new Dimension(200, super.getPreferredSize().height);
        }
    };
    add(progressBar, BorderLayout.CENTER);
    add(progressLabel, BorderLayout.SOUTH);
    progressBar.setStringPainted(true);
    progressBar.setMaximumSize(new Dimension(200,8));
    progressBar.setPreferredSize(new Dimension(200,8));
    progressBar.setMinimumSize(new Dimension(200,8));
    progressBar.setForeground(Color.yellow);
    progressBar.setBackground(Color.black);
    setBackground(this.getBackground());
  }
}