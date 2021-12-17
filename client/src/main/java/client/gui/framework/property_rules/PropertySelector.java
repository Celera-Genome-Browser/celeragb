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

import api.entity_model.management.PropertyMgr;
import api.entity_model.model.fundtype.GenomicEntity;
import api.stub.data.GenomicProperty;
import client.gui.framework.session_mgr.BrowserModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies (peter.davies)
 * @version $Id$
 */

public class PropertySelector extends JDialog {
  JPanel panel1 = new JPanel();
  BrowserModel browserModel;
  JScrollPane jspPropertySelector = new JScrollPane();
  JList lstPropertySelector = new JList();
  JButton btnOK = new JButton();
  JButton btnCancel = new JButton();
  boolean isOK;
  TreeMap propertyMap = new TreeMap();

  public PropertySelector(JFrame parent, Point topLeftCorner, BrowserModel browserModel) {
    super(parent, "Property Selector", true);
    try {
      this.browserModel=browserModel;
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
    panel1.setPreferredSize(new Dimension(190, 150));
    panel1.setToolTipText("");
//    panel1.setBounds(new Rectangle(0, 0, 400, 300));
    jspPropertySelector.setBounds(new Rectangle(6, 13, 173, 85));
    btnOK.setEnabled(false);
    btnOK.setToolTipText("");
    btnOK.setText("OK");
    btnOK.setBounds(new Rectangle(7, 116, 79, 27));
    btnOK.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnOK_actionPerformed(e);
      }
    });
    btnCancel.setText("Cancel");
    btnCancel.setBounds(new Rectangle(98, 116, 79, 27));
    btnCancel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btnCancel_actionPerformed(e);
      }
    });
    lstPropertySelector.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        lstPropertySelector_valueChanged(e);
      }
    });
    getContentPane().add(panel1, null);
    panel1.add(jspPropertySelector, null);
    panel1.add(btnCancel, null);
    panel1.add(btnOK, null);
    jspPropertySelector.getViewport().add(lstPropertySelector, null);
    lstPropertySelector.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    GenomicEntity entity= browserModel.getCurrentSelection();
    Set properties=entity.getProperties();
    for (Iterator it=properties.iterator();it.hasNext();) {
      String prop = ((GenomicProperty)it.next()).getName();
      propertyMap.put(PropertyMgr.getPropertyMgr().getPropertyDisplayName(prop), prop);
    }
    lstPropertySelector.setListData(propertyMap.keySet().toArray());
  }

  /**
   * Helps to ensure good window placement.
   */
  private void positionDialog(Point topLeftCorner) {
    pack();
    setLocation(topLeftCorner);
  }

  public String getSelectedPropertyName() {
    return (String)propertyMap.get(lstPropertySelector.getSelectedValue());
  }

  public boolean wasOKPressed() {
    return isOK;
  }

  void btnOK_actionPerformed(ActionEvent e) {
    isOK=true;
    dispose();
    setVisible(false);
  }

  void btnCancel_actionPerformed(ActionEvent e) {
    dispose();
    setVisible(false);
  }

  void lstPropertySelector_valueChanged(ListSelectionEvent e) {
    btnOK.setEnabled(true);
  }


}