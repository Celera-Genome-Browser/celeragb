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

import api.entity_model.management.properties.PropertyCreationRule;
import client.gui.framework.browser.Browser;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.SessionMgr;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Set;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies (peter.davies)
 * @version $Id$
 */

public class PropertyRuleDialog extends JDialog {

  private BrowserModel browserModel;
  JButton btnAdd = new JButton();
  JButton btnRemove = new JButton();
  JButton btnOK = new JButton();
  JLabel lblCurrentRules = new JLabel();
  JScrollPane jScrollPane = new JScrollPane();
  DefaultListModel model=new DefaultListModel();
  JList lstRules = new JList(model);

  public PropertyRuleDialog(Browser browser) {
    super(browser,"Property Rules",true);
    this.browserModel=browser.getBrowserModel();
    jbInit();
    centerDialog();
  }

  private void jbInit() {
//    lstRules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    btnAdd.setText("Add");
    btnAdd.setBounds(new Rectangle(165, 8, 84, 28));
    btnRemove.setEnabled(false);
    btnRemove.setText("Remove");
    btnRemove.setBounds(new Rectangle(279, 8, 84, 28));
    btnRemove.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnRemove_actionPerformed(e);
      }
    });
    btnOK.setText("OK");
    btnOK.setBounds(new Rectangle(59, 8, 84, 28));
    JPanel pnlButton = new JPanel();

    pnlButton.setLayout(null);
    pnlButton.setBounds(new Rectangle(10, 217, 397, 42));
    lblCurrentRules.setText("Current Rules:");
    lblCurrentRules.setBounds(new Rectangle(16, 12, 97, 22));
    jScrollPane.setBounds(new Rectangle(12, 61, 401, 142));
    /**
     * @todo Due to an NT jdk1.3.0.X bug the image icon will not show if isResizable is false.
     * When moving to a new JDK make it false again.
     */
    this.setResizable(true);
    this.setModal(true);
    JPanel outerPanel=new JPanel();
    outerPanel.setLayout(null);
    lstRules.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        lstRules_valueChanged(e);
      }
    });
    outerPanel.add(pnlButton);
    pnlButton.add(btnOK);
    pnlButton.add(btnAdd);
    pnlButton.add(btnRemove, null);
    outerPanel.setPreferredSize(new Dimension(430,275));
    getContentPane().add(outerPanel);
    outerPanel.add(jScrollPane, null);
    outerPanel.add(lblCurrentRules, null);
    jScrollPane.getViewport().add(lstRules);

    Set rules=SessionMgr.getSessionMgr().getPropertyCreationRules();
    for (Iterator it=rules.iterator();it.hasNext();) {
      model.addElement(it.next());
    }
    btnOK.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
          dispose();
        }
    });

    btnAdd.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
          new AddPropertyRuleDialog(PropertyRuleDialog.this,browserModel).setVisible(true);
          model.clear();
          Set rules=SessionMgr.getSessionMgr().getPropertyCreationRules();
          for (Iterator it=rules.iterator();it.hasNext();) {
            model.addElement(it.next());
          }
        }
    });
  }


  /**
   * Helps to ensure good window placement.
   */
  private void centerDialog() {
    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int screenWidth = screenSize.width;
    int screenHeight = screenSize.height;
    pack();
    Dimension frameSize = getSize();
    if (frameSize.height > screenHeight) {
      frameSize.height = screenHeight;
    } // Adjust for screen height.

    if (frameSize.width > screenWidth) {
      frameSize.width = screenWidth;
    } // Adjust for screen width.
    setLocation((screenWidth - frameSize.width)/2,
                         (screenHeight - frameSize.height)/2);
  }

  void lstRules_valueChanged(ListSelectionEvent e) {
    btnRemove.setEnabled(true);
  }

  void btnRemove_actionPerformed(ActionEvent e) {
    int ans=JOptionPane.showConfirmDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
      "This will permanently delete the rule.  \nAre you sure you want to do this?",
      "Please Confirm",JOptionPane.YES_NO_OPTION);
    if (ans==JOptionPane.NO_OPTION) return;
    Object[] objs=lstRules.getSelectedValues();
    for (int i=0;i<objs.length;i++) {
      if (objs[i] instanceof PropertyCreationRule) {
        SessionMgr.getSessionMgr().removePropertyCreationRule(((PropertyCreationRule)objs[i]).getName());
      }
    }
    int[] indecies=lstRules.getSelectedIndices();
    lstRules.clearSelection();
    for (int i=0;i<indecies.length;i++) {
      model.removeElementAt(indecies[i]);
    }
    btnRemove.setEnabled(false);
  }
}