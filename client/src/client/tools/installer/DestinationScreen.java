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

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Title:
 * Description:
 * Company:
 * @author
 * @version 1.0
 */

public class DestinationScreen extends BaseScreen {
  JPanel mainPanel = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  static private final String title = "Choose Destination Location";
  JPanel buttonPanel = new JPanel();
  JButton nextButton = new JButton();
  JButton cancelButton = new JButton();
  JButton backButton = new JButton();
  Component backScreen = null;
  String hotDirectory = "C:"+System.getProperty("file.separator")+"Genomics_Genomics"+System.getProperty("file.separator");
  JLabel dir = null;

  public DestinationScreen(WizardController controller) {
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

//    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//    String[] fontNames = ge.getAvailableFontFamilyNames();
//    for (int i = 0; i < fontNames.length; i++)
//      System.out.println(fontNames[i]);

    Font f = new Font("Times New Roman", Font.PLAIN, 12);

    master.setInstallDirectory(hotDirectory);

    JTextArea jt = new JTextArea();
    jt.setFont(f);
    jt.setEditable(false);
    jt.setHighlighter(null);
    jt.setCursor(null);
    jt.setBackground(this.getBackground());
    jt.setForeground(Color.black);
    jt.setText("Setup will install the Application in the Destination Directory.\n\n" +
               "To install in the Destination Directory, choose the Next >"+"\n"+"Button.  To install in a different" +
               " directory, choose the Browse..."+"\n"+"Button");
    jt.setLineWrap(true);
    jt.setColumns(34);

    JLabel jl = new JLabel(icon);

    JPanel westSector = new JPanel();
    westSector.setLayout(new BorderLayout());
    westSector.add(jl, BorderLayout.NORTH);

    JPanel eastSector = new JPanel();
    eastSector.setLayout(new BorderLayout());
    eastSector.add(jt, BorderLayout.NORTH);

    JPanel destPanel = new JPanel();
    destPanel.setLayout(new BorderLayout());
    dir = new JLabel(hotDirectory);
    dir.setForeground(Color.black);
    dir.setFont(f);
    destPanel.add(dir, BorderLayout.WEST);
    JButton browseButton = new JButton();
    browseButton.setFont(f);
    destPanel.add(browseButton, BorderLayout.EAST);
    destPanel.setBorder(BorderFactory.createTitledBorder("Destination Directory"));
    browseButton.setText("Browse...");
    browseButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        browseButton_actionPerformed(e);
      }
    });

    eastSector.add(vstrut,BorderLayout.CENTER);
    eastSector.add(destPanel,BorderLayout.SOUTH);
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
    nextButton.setText("Next >");
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

  void browseButton_actionPerformed(ActionEvent e) {
    JFileChooser fileChooser = new DirFileChooser();
    fileChooser.setCurrentDirectory(new File(hotDirectory)); //"C:" + System.getProperty("file.separator")
    fileChooser.setMultiSelectionEnabled(false);
    fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setDialogTitle("Select Directory");
    fileChooser.setVisible(true);

    int returnVal = fileChooser.showDialog(this,"SELECT");
    if( returnVal == JFileChooser.APPROVE_OPTION ) {
      hotDirectory=fileChooser.getSelectedFile().toString() + System.getProperty("file.separator");
      dir.setText(this.shrinkDir(fileChooser.getSelectedFile()) + System.getProperty("file.separator") );
    }
    master.setInstallDirectory(hotDirectory);
  }

  private String shrinkDir( File file ) {
    String result = file.toString();

//    if( file.toString().length() > 33 ) {
      String parent = file.getParent();
//      System.getProperty()
      int drive = parent.indexOf(System.getProperty("file.separator"));
      String selectedDir = file.getName();
      System.out.println("Drive="+parent.substring(0,drive+1));
      System.out.println("Dir= "+ selectedDir );
      result = parent.substring(0,drive+1) + "..."+System.getProperty("file.separator") + selectedDir;

  //  }

    return result;
  }
}