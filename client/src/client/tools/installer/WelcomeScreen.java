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
public class WelcomeScreen extends BaseScreen {
    static private final String title = "Welcome";
    JPanel mainPanel = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel buttonPanel = new JPanel();
    JButton nextButton = new JButton();
    JButton cancelButton = new JButton();
    JButton backButton = new JButton();

    public WelcomeScreen(WizardController controller) {
        super(title, controller);

        try {
            jbInit();

            int width = 600;
            int height = 400;
            this.setSize(width, height);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        Component strut = Box.createHorizontalStrut(40);

        Font f = new Font("Times New Roman", Font.PLAIN, 12);

        JTextArea jt = new JTextArea();
        jt.setFont(f);
        jt.setEditable(false);
        jt.setHighlighter(null);
        jt.setCursor(null);
        jt.setBackground(this.getBackground());
        jt.setForeground(Color.black);
        jt.setText("Welcome to the " + getArgValue("productname") + 
                   " Setup Program.\nThis" + 
                   "program will install software on your system.\n\n\n" + 
                   "Click Cancel to quit Setup.  Click Next to continue with Setup.\n\n\n\n");
        jt.setLineWrap(true);
        jt.setColumns(34);

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
        mainPanel.setBorder(BorderFactory.createEtchedBorder(
                                    EtchedBorder.LOWERED));
        this.add(mainPanel, BorderLayout.CENTER);

        backButton.setText("< Back");
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                backButton_actionPerformed(e);
            }
        });
        backButton.setEnabled(false);
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
        buttonPanel.add(strut);
        buttonPanel.add(cancelButton);
    }
}
