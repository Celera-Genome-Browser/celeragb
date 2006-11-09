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
package client.gui.other.menus;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */
import api.entity_model.management.PropertyMgr;
import api.entity_model.model.fundtype.GenomicEntity;
import api.stub.data.GenomicProperty;
import client.gui.other.util.URLLauncher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class FeatureLinkMenu extends JMenu {
  private GenomicProperty currentLinksProperty;

  public FeatureLinkMenu(GenomicEntity selection) {
    super("Feature CDS Links");
    setMenuItems(selection);
  }


  private void setMenuItems(GenomicEntity selection){
    if (selection!=null &&
        selection.getProperty("CDS_links")!=null &&
        selection.getProperty("CDS_links").getSubProperties()!=null) {
      FeatureLinkMenu.this.setEnabled(true);
      currentLinksProperty = selection.getProperty("CDS_links");
      GenomicProperty[] subProps = currentLinksProperty.getSubProperties();
      for (int i = 0; i < subProps.length; i++) {
        GenomicProperty tmpProperty = (GenomicProperty)subProps[i];
        String buttonName = PropertyMgr.getPropertyMgr().getPropertyDisplayName(tmpProperty.getName());
        JMenuItem tmpLinkMI = new JMenuItem(buttonName);
        tmpLinkMI.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            launchURL(((JMenuItem)evt.getSource()).getText());
          }
        });
        this.add(tmpLinkMI);
      }
    }
    else {
      FeatureLinkMenu.this.removeAll();
      FeatureLinkMenu.this.setEnabled(false);
      currentLinksProperty = null;
    }
  }


  private void launchURL(String linkName) {
    GenomicProperty[] subProps = currentLinksProperty.getSubProperties();
    for (int i = 0; i < subProps.length; i++) {
      GenomicProperty tmpProperty = (GenomicProperty)subProps[i];
      if (PropertyMgr.getPropertyMgr().getPropertyDisplayName(
            tmpProperty.getName()).equals(linkName)) {
        URLLauncher tmpLauncher = new URLLauncher();
        tmpLauncher.launchURL(tmpProperty.getInitialValue());
      }
    }
  }
}