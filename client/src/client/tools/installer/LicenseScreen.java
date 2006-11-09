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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

/**
 * Title:
 * Description:
 * Company:
 * @author
 * @version 1.0
 */

public class LicenseScreen extends BaseScreen {
   JPanel mainPanel = new JPanel();
   BorderLayout borderLayout1 = new BorderLayout();
   static private final String title = "License Agreement";
   JPanel buttonPanel = new JPanel();
   JButton nextButton = new JButton();
   JButton cancelButton = new JButton();
   JButton backButton = new JButton();
   Component backScreen = null;

   public LicenseScreen(WizardController controller) {
      super(title, controller);
      try {
         jbInit();
         int width = 600, height = 400;
         this.setSize(width, height);
      }
      catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   void jbInit() throws Exception {
      mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

      Component strut = Box.createHorizontalStrut(15);

      //    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      //    String[] fontNames = ge.getAvailableFontFamilyNames();
      //    for (int i = 0; i < fontNames.length; i++)
      //      System.out.println(fontNames[i]);

      Font f = new Font("Times New Roman", Font.PLAIN, 12);

      JTextArea jt = new JTextArea();
      jt.setFont(f);
      jt.setEditable(false);
      jt.setHighlighter(null);
      jt.setCursor(null);
      jt.setBackground(this.getBackground());
      jt.setForeground(Color.black);
      jt.setText(
         "By installing the Browser software, you agree\n"
            + "to be bound by the terms and conditions of the Agreement that grants "
            + "access to the data for which this browser is to be used.\n\n\n\n"
            + "Do you accept all the terms of the preceding License\n"
            + "Agreement? If you choose No, Setup will close.\n"
            + "To continue Setup, you must accept this agreement.");
      jt.setLineWrap(true);
      jt.setColumns(34);

      //    ImageIcon icon = new ImageIcon("gb_splash_logo_sm.jpg");
      JLabel jl = new JLabel(icon);

      JPanel westSector = new JPanel();
      westSector.setLayout(new BorderLayout());
      westSector.add(jl, BorderLayout.NORTH);

      JPanel eastSector = new JPanel();
      eastSector.setLayout(new BorderLayout());
      eastSector.add(jt, BorderLayout.NORTH);

      mainPanel.add(Box.createHorizontalStrut(5));
      mainPanel.add(westSector);
      mainPanel.add(Box.createHorizontalStrut(10));
      mainPanel.add(eastSector);
      mainPanel.add(Box.createHorizontalStrut(5));
      mainPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
      this.add(mainPanel, BorderLayout.CENTER);

      backButton.setText("< Back");
      backButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(ActionEvent e) {
            backButton_actionPerformed(e);
         }
      });
      nextButton.setText("I Agree");
      nextButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(ActionEvent e) {
            nextButton_actionPerformed(e);
         }
      });
      cancelButton.setText("I Do Not Agree");
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
      buttonPanel.add(strut);
      buttonPanel.add(cancelButton);
   }

}