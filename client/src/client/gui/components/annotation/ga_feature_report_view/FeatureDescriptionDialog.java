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
package client.gui.components.annotation.ga_feature_report_view;

import client.gui.framework.session_mgr.SessionMgr;
import client.shared.text_component.StandardTextArea;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;


public class FeatureDescriptionDialog extends JDialog {
    JPanel panel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout(5, 5);
    JTextArea jTextArea1 = new StandardTextArea();
    JPanel jPanel1 = new JPanel();
    JButton okButton = new JButton();
    BorderLayout borderLayout2 = new BorderLayout(5, 5);
    JPanel jPanel2 = new JPanel();
    JPanel jPanel3 = new JPanel();
    JDialog dialog;

    public FeatureDescriptionDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        dialog = this;

        try {
            jbInit();
            pack();
        } catch (Exception ex) {
            SessionMgr.getSessionMgr().handleException(ex);
        }
    }

    public FeatureDescriptionDialog() {
        this(null, "", false);
    }

    void jbInit() throws Exception {
        panel1.setLayout(borderLayout1);
        jTextArea1.setLineWrap(true);
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setEditable(false);
        jPanel1.setLayout(borderLayout2);
        okButton.setActionCommand("okCommand");
        okButton.addActionListener(new OKActionListener());
        okButton.setText("OK");
        okButton.setPreferredSize(new Dimension(48, 24));
        panel1.setBorder(BorderFactory.createEtchedBorder());
        getContentPane().add(panel1);
        panel1.add(jTextArea1, BorderLayout.CENTER);
        panel1.add(jPanel1, BorderLayout.SOUTH);
        jPanel1.add(okButton, BorderLayout.CENTER);
        jPanel2.setPreferredSize(new Dimension(120, 24));
        jPanel1.add(jPanel2, BorderLayout.WEST);
        jPanel3.setPreferredSize(new Dimension(120, 24));
        jPanel1.add(jPanel3, BorderLayout.EAST);
    }

    public void setDescription(String des) {
        jTextArea1.setText(des);
    }

    class OKActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Container parent = dialog.getParent();
            dialog.setVisible(false);
            dialog.dispose();
            parent.repaint();
        }
    }

    /*    public void setVisible(boolean bVisible)
        {
    
        }
    */
}