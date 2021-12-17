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

import client.gui.framework.browser.Browser;
import client.gui.framework.display_rules.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;

public class FilterMenu extends JMenu {

  private JMenuItem resetIntensityMI = new JMenuItem("Reset Intensities");
  private JMenu applyColorFilterMenu = new JMenu("Apply Intensity Filter");
  private TreeMap intensityFilters = new TreeMap(new MyStringComparator());
  private JMenuItem colorFilter = new JMenuItem("Color Intensity Filter...",'C');

  private JMenuItem featureOrderFilter = new JMenuItem("Feature Ordering Filter...",'O');
  private JMenuItem resetOrderMI = new JMenuItem("Reset Feature Order");
  private JMenu applyOrderFilterMenu = new JMenu("Apply Order Filter");
  private TreeMap orderFilters = new TreeMap(new MyStringComparator());

  private Browser browser;

  public FilterMenu(Browser browser) {
    this.setText("Filters");
    this.setMnemonic('t');
    this.browser = browser;
    DisplayFilterMgr.getDisplayFilterMgr().addDisplayFilterListener(new MyDisplayFilterListener());
    colorFilter.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        establishColorRuleDialog();
      }
    });
    featureOrderFilter.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        establishPropertySortDialog();
      }
    });
    intensityFilters = DisplayFilterMgr.getDisplayFilterMgr().getColorIntensityFilters();
    orderFilters = DisplayFilterMgr.getDisplayFilterMgr().getPropertySortFilters();

    establishDisplayFilterButtons(intensityFilters, applyColorFilterMenu);
    establishDisplayFilterButtons(orderFilters, applyOrderFilterMenu);

    add(colorFilter);
    add(featureOrderFilter);
    add(new JSeparator());
    add(applyColorFilterMenu);
    add(applyOrderFilterMenu);
  }

  private void establishColorRuleDialog() {
    browser.repaint();
    ColorRuleDialog colorRuleDialog = new ColorRuleDialog(browser);
    colorRuleDialog.setVisible( true );
  }

  private void establishPropertySortDialog() {
    browser.repaint();
    PropertySortRuleDialog sortRuleDialog = new PropertySortRuleDialog(browser);
    sortRuleDialog.setVisible( true );
  }


  private void establishDisplayFilterButtons(TreeMap filterInfoCollection,
      JMenu targetFilterMenu) {
    JMenuItem tmpMI = new JMenuItem();
    String tmpKey = new String();

    if (filterInfoCollection!=null && filterInfoCollection.size()!=0) {
      targetFilterMenu.removeAll();
      for (Iterator it = filterInfoCollection.keySet().iterator(); it.hasNext();) {
        tmpKey = (String)it.next();
        tmpMI = new JMenuItem(tmpKey);
        tmpMI.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent evt) {
            DisplayFilterInfo  displayInfo=(DisplayFilterInfo)(DisplayFilterMgr.
              getDisplayFilterMgr().getDisplayFilterInfo(((JMenuItem)evt.getSource()).getText()));
	    browser.getBrowserModel().setModelProperty(
              browser.getBrowserModel().DISPLAY_FILTER_PROPERTY,displayInfo);
          }
        });
        targetFilterMenu.add(tmpMI);
      }
      if (targetFilterMenu.getItemCount()==0) {
        JMenuItem emptyMenuItem = new JMenuItem("(No Filters Defined)");
        emptyMenuItem.setEnabled(false);
        targetFilterMenu.add(emptyMenuItem);
      }
      else {
        targetFilterMenu.addSeparator();
        resetIntensityMI.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            if (evt.getSource()==resetIntensityMI) {
              browser.getBrowserModel().setModelProperty("ResetGBGenomicGlyphIntensities", "");
            }
          }
        });
        targetFilterMenu.add(resetIntensityMI);
      }
    }
    else {
      JMenuItem emptyMenuItem = new JMenuItem("(No Rules Defined)");
      emptyMenuItem.setEnabled(false);
      targetFilterMenu.add(emptyMenuItem);
    }
  }


  private class MyStringComparator implements Comparator {
    public int compare(Object key1, Object key2) {
          String keyName1 = (String)key1;
          String keyName2 = (String)key2;
          return keyName1.compareToIgnoreCase(keyName2);
    }
  }


  private class MyDisplayFilterListener extends DisplayFilterListenerAdapter {
    public void propertySortFiltersChanged(){
      establishDisplayFilterButtons(orderFilters, applyOrderFilterMenu);
    }
    public void colorIntensityFiltersChanged(){
      establishDisplayFilterButtons(intensityFilters, applyColorFilterMenu);
    }
  }
}