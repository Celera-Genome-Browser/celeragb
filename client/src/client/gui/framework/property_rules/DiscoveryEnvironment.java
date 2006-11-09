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
package client.gui.framework.property_rules;

import client.shared.text_component.StandardTextField;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Keymap;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies (peter.davies)
 * @version $Id$
 */

public class DiscoveryEnvironment extends JDialog {
  JPanel panel1 = new JPanel();
  JButton btnOK = new JButton();
  JButton btnCancel = new JButton();
  boolean isOK;
  JTextField txtDiscoveryEnvironment = new StandardTextField();

  //Modified the jTextField so that it will be able to trigger the default button
  static {
   JTextField f = new StandardTextField();
   KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
   Keymap map = f.getKeymap();
   map.removeKeyStrokeBinding(enter);
  }

  public DiscoveryEnvironment(JFrame parent, Point topLeftCorner) {
    super(parent, "Add Discovery Environment", true);
    try {
      jbInit();
      positionDialog(topLeftCorner);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }


  void jbInit() throws Exception {
    panel1.setLayout(null);
//    this.getContentPane().setLayout(null);
    panel1.setPreferredSize(new Dimension(245, 86));
    panel1.setToolTipText("");
//    panel1.setBounds(new Rectangle(0, 0, 400, 300));
    btnOK.setEnabled(false);
    btnOK.setToolTipText("");
    btnOK.setText("OK");
    btnOK.setBounds(new Rectangle(40, 51, 79, 27));
    btnOK.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnOK_actionPerformed(e);
      }
    });
    btnCancel.setText("Cancel");
    btnCancel.setBounds(new Rectangle(131, 51, 79, 27));
    btnCancel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnCancel_actionPerformed(e);
      }
    });
    txtDiscoveryEnvironment.setBounds(new Rectangle(38, 17, 172, 21));
    this.getContentPane().add(panel1, BorderLayout.CENTER);
    panel1.add(txtDiscoveryEnvironment, null);
    panel1.add(btnOK, null);
    panel1.add(btnCancel, null);

    txtDiscoveryEnvironment.getDocument().addDocumentListener(new DocumentListener(){
        public void insertUpdate(DocumentEvent e){
          if (!btnOK.isEnabled()) {
             btnOK.setEnabled(true);
             getRootPane().setDefaultButton(btnOK);
          }
        }
        public void removeUpdate(DocumentEvent e){
          if (e.getDocument().getLength()==0) {
           btnOK.setEnabled(false);
           getRootPane().setDefaultButton(btnCancel);
          }
        }
        public void changedUpdate(DocumentEvent e){}
    });
  }

  /**
   * Helps to ensure good window placement.
   */
  private void positionDialog(Point topLeftCorner) {
    pack();
    setLocation(topLeftCorner);
  }

  public String getSelectedName() {
    return txtDiscoveryEnvironment.getText();
  }

  public boolean wasOKPressed() {
    return isOK;
  }

  void btnOK_actionPerformed(ActionEvent e) {
    isOK=true;
    dispose();
    hide();
  }

  void btnCancel_actionPerformed(ActionEvent e) {
    dispose();
    hide();
  }

}