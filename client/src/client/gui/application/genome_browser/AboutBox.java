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
package client.gui.application.genome_browser;

import client.gui.framework.session_mgr.SessionMgr;
import client.shared.text_component.StandardTextArea;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;


/**
* Initially writted by: Peter Davies
*/
public class AboutBox extends JDialog {
    static String lineSep = System.getProperty("line.separator");
    JPanel buttonPanel = new JPanel();
    JPanel bottomPanel = new JPanel();
    JPanel outerPanel = new JPanel();
    JPanel rightsPanel = new JPanel();
    JPanel buildPanel = new JPanel();
    JButton closeButton = new JButton("Close");
    JLabel buildLabel = new JLabel("  Build Date:  @@date@@");

    public AboutBox() {
        super(SessionMgr.getSessionMgr().getActiveBrowser(), 
              "About the Browser", true);
        SessionMgr.getSessionMgr().getActiveBrowser().repaint();

        try {
            jbInit();
        } catch (Exception e) {
            try {
                client.gui.framework.session_mgr.SessionMgr.getSessionMgr()
                                                      .handleException(e);
            } catch (Exception ex1) {
                e.printStackTrace();
            }
        }

        pack();
    }


    private void jbInit() throws Exception {
        this.setBackground(Color.white);
        outerPanel.setLayout(new BorderLayout(0, 10));
        outerPanel.add(new SplashPanel(), BorderLayout.NORTH);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == closeButton) {
                    // disposes the view
                    dispose();
                }
            }
        });

        this.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
                checkKey(e);
            }

            public void keyPressed(KeyEvent e) {
                checkKey(e);
            }

            public void keyReleased(KeyEvent e) {
                checkKey(e);
            }

            private void checkKey(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    dispose();
                }
            }
        });

        buildPanel.setBackground(Color.white);
        buildPanel.add(buildLabel);

        rightsPanel.setBackground(Color.white);
        rightsPanel.setLayout(
                new BoxLayout(rightsPanel, BoxLayout.Y_AXIS));
        rightsPanel.add(Box.createVerticalStrut(10));

        JTextArea rightsText = new StandardTextArea();
        JScrollPane jsp = new JScrollPane(rightsText);
        jsp.setRequestFocusEnabled(false);
        jsp.setEnabled(false);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setPreferredSize(new Dimension(250, 100));

        rightsText.setLineWrap(true);
        rightsText.setWrapStyleWord(true);
        rightsText.setEnabled(false);
        rightsText.setEditable(false);
        rightsText.append("Genome Browser" + lineSep + lineSep);
        rightsText.append(
                "Rights to this application belong to Applera Corporation." + 
                lineSep + lineSep);
        rightsText.append(
"Applera provides this to you as free software; you can use it, redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.");
        rightsText.setCaretPosition(0);
        rightsPanel.add(jsp);

        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(Color.white);
        bottomPanel.add(buildPanel);
        bottomPanel.add(Box.createHorizontalGlue());

        JPanel outerRightsPanel = new JPanel();
        outerRightsPanel.setLayout(
                new BoxLayout(outerRightsPanel, BoxLayout.X_AXIS));
        outerRightsPanel.add(Box.createHorizontalStrut(15));
        outerRightsPanel.add(rightsPanel);
        outerRightsPanel.add(Box.createHorizontalStrut(15));
        outerRightsPanel.setBackground(Color.white);
        bottomPanel.add(outerRightsPanel);
        outerPanel.add(bottomPanel, BorderLayout.CENTER);

        JPanel botPanel = new JPanel();
        botPanel.setBackground(new Color(0, 0, 0, 0));
        botPanel.setLayout(new BoxLayout(botPanel, BoxLayout.X_AXIS));
        botPanel.add(Box.createHorizontalGlue());
        botPanel.add(closeButton);
        botPanel.add(Box.createHorizontalGlue());

        buttonPanel.setBackground(Color.white);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(botPanel);
        buttonPanel.add(Box.createVerticalStrut(8));
        outerPanel.add(buttonPanel, BorderLayout.SOUTH);
        outerPanel.setBackground(Color.white);
        outerPanel.setBorder(
                new BevelBorder(BevelBorder.RAISED, Color.lightGray, 
                                Color.darkGray));
        this.getContentPane().add(outerPanel);
        closeButton.requestDefaultFocus();
        closeButton.requestFocus();
        validate();
        pack();
        setLocationRelativeTo(SessionMgr.getSessionMgr().getActiveBrowser());
    }
}
