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

/**
 * Title:        Your Product Name<p>
 * Description:  This is the main Browser in the System<p>
 * @author Peter Davies
 * @version
 */
package client.gui.framework.outline;

import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

public class ChromosomePopUpMenu extends JPopupMenu {

  private JRadioButtonMenuItem ascendingLength,descendingLength;
  private JRadioButtonMenuItem ascendingOrder,descendingOrder;
  private JMenuItem setBounds;
  private JMenuItem removeBounds;
  private ButtonGroup group=new ButtonGroup();
  private byte selectedMenu;

  public static final byte ASCENDING_LENGTH=0;
  public static final byte DESCENDING_LENGTH=1;
  public static final byte ASCENDING_ORDER=2;
  public static final byte DESCENDING_ORDER=3;

  public ChromosomePopUpMenu(byte selectedMenu, boolean filterOn, boolean allowOrder) {
      this.selectedMenu = selectedMenu;
      ascendingLength = new JRadioButtonMenuItem("Length Sort: Ascending");
      descendingLength = new JRadioButtonMenuItem("Length Sort: Descending");
      ascendingOrder = new JRadioButtonMenuItem("Position Sort: Ascending Within Chromosome");
      descendingOrder = new JRadioButtonMenuItem("Position Sort: Descending Within Chromosome");

      setBounds = new JMenuItem ("Set Filter");
      removeBounds = new JMenuItem ("Remove Filter");
      removeBounds.setEnabled(filterOn);
      group.add(ascendingLength);
      group.add(descendingLength);
      if (allowOrder) {
        group.add(ascendingOrder);
        group.add(descendingOrder);
      }
      switch (selectedMenu) {
        case ASCENDING_LENGTH:
          ascendingLength.setSelected(true);
          break;
        case DESCENDING_LENGTH :
          descendingLength.setSelected(true);
          break;
        case ASCENDING_ORDER:
          ascendingOrder.setSelected(true);
          break;
        case DESCENDING_ORDER :
          descendingOrder.setSelected(true);
          break;
        default:
          ascendingLength.setSelected(true);
          break;
      }
      add (setBounds);
      add (removeBounds);
      add (new JSeparator());
      add (ascendingLength);
      add (descendingLength);
      if (allowOrder) {
        add (ascendingOrder);
        add (descendingOrder);
      }
  }

  public void addAscendingLengthActionListener(ActionListener listener) {
     ascendingLength.addActionListener(listener);
  }

  public void addDecendingLengthActionListener(ActionListener listener) {
     descendingLength.addActionListener(listener);
  }

  public void addAscendingOrderActionListener(ActionListener listener) {
     ascendingOrder.addActionListener(listener);
  }

  public void addDecendingOrderActionListener(ActionListener listener) {
     descendingOrder.addActionListener(listener);
  }

  public void addSetBoundsActionListener(ActionListener listener) {
     setBounds.addActionListener(listener);
  }

  public void removeSetBoundsActionListener(ActionListener listener) {
     setBounds.removeActionListener(listener);
  }

  public void addRemoveBoundsActionListener(ActionListener listener) {
     removeBounds.addActionListener(listener);
  }

  public void removeRemoveBoundsActionListener(ActionListener listener) {
     removeBounds.removeActionListener(listener);
  }

  public void removeAscendingLengthActionListener(ActionListener listener) {
     ascendingLength.removeActionListener(listener);
  }

  public void removeDecendingLengthActionListener(ActionListener listener) {
     descendingLength.removeActionListener(listener);
  }

  public void removeAscendingOrderActionListener(ActionListener listener) {
     ascendingOrder.removeActionListener(listener);
  }

  public void removeDecendingOrderActionListener(ActionListener listener) {
     descendingOrder.removeActionListener(listener);
  }

  public void setFilterOn(boolean on) {
      removeBounds.setEnabled(on);
  }




}