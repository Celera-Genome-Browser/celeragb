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

package client.gui.other.menus;

import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;

/** Transcription or Translation display menu, with Items */

public class TranslationMenu extends JMenu {

  public JCheckBoxMenuItem transOne      = new JCheckBoxMenuItem(" +1 ORF");
  public JCheckBoxMenuItem transTwo      = new JCheckBoxMenuItem(" +2 ORF");
  public JCheckBoxMenuItem transThree    = new JCheckBoxMenuItem(" +3 ORF");
  public JCheckBoxMenuItem transNegOne   = new JCheckBoxMenuItem(" -1 ORF");
  public JCheckBoxMenuItem transNegTwo   = new JCheckBoxMenuItem(" -2 ORF");
  public JCheckBoxMenuItem transNegThree = new JCheckBoxMenuItem(" -3 ORF");

  public JRadioButton noComp    = new JRadioButton("No Complement");
  public JRadioButton revComp   = new JRadioButton("Display Complement");

  public JRadioButton oneLetter   = new JRadioButton("One Letter Translation");
  public JRadioButton threeLetter = new JRadioButton("Three Letter Translation");

  protected ActionListener actionListener;

  public TranslationMenu (ActionListener aL) {

    super ("Show");
    this.setMnemonic('S');
    ButtonGroup transGroup = new ButtonGroup();
    ButtonGroup compGroup  = new ButtonGroup();

    actionListener = aL;

    // Add the buttons to the menu,

    add(transOne);
    add(transTwo);
    add(transThree);
    add(transNegOne);
    add(transNegTwo);
    add(transNegThree);

    add (new JSeparator());

    add(oneLetter);
    add(threeLetter);

    add (new JSeparator());

    add(noComp);
    add(revComp);

    // Register for event handling,

    noComp.addActionListener(actionListener);
    revComp.addActionListener(actionListener);

    oneLetter.addActionListener(actionListener);
    threeLetter.addActionListener(actionListener);

    transOne.addActionListener(actionListener);
    transTwo.addActionListener(actionListener);
    transThree.addActionListener(actionListener);
    transNegOne.addActionListener(actionListener);
    transNegTwo.addActionListener(actionListener);
    transNegThree.addActionListener(actionListener);

    // Set up the radio-ness of the complement buttons

    compGroup.add(noComp);
    compGroup.add(revComp);

    transGroup.add(oneLetter);
    transGroup.add(threeLetter);
  }

  /**
   * Do the appropriate default clicks!  Call this only after
   * construction and after the listener's appropriate menu property
   * has been set.
   */
  public void clickDefaults() {
    oneLetter.doClick();
    noComp.doClick();
  }
}
