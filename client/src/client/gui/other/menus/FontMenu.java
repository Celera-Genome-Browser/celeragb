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
import javax.swing.*;
import java.util.Enumeration;

/** Font Menu, with Items */

public class FontMenu extends JMenu {

  /* FontSizes Menu */

  private ButtonGroup fontGroup = new ButtonGroup();
  private ButtonGroup sizeGroup = new ButtonGroup();

  public final JRadioButton defaultFont     = new JRadioButton("Default");
  public final JRadioButton timesRomanFont  = new JRadioButton("TimesRoman");
  public final JRadioButton helveticaFont   = new JRadioButton("Helvetica");
  public final JRadioButton courierFont     = new JRadioButton("Courier");
  public final JRadioButton dialogFont      = new JRadioButton("Dialog");
  public final JRadioButton dialogInputFont = new JRadioButton("DialogInput");

  public final JRadioButton smallFont       = new JRadioButton("Small");
  public final JRadioButton mediumFont      = new JRadioButton("Medium");
  public final JRadioButton largeFont       = new JRadioButton("Large");
  public final JCheckBox boldFont           = new JCheckBox("Bold");

  public FontMenu (ActionListener actionListener) {

    super ("Font");
    this.setMnemonic('n');
    // Add the buttons to the menu,

    add(defaultFont);
    add(timesRomanFont);
    add(helveticaFont);
    add(courierFont);
    add(dialogFont);
    add(dialogInputFont);

    add (new JSeparator());

    add(smallFont);
    add(mediumFont);
    add(largeFont);

    add (new JSeparator());

    add(boldFont);
    // Add the buttons to the groups,

    fontGroup.add(defaultFont);
    fontGroup.add(timesRomanFont);
    fontGroup.add(helveticaFont);
    fontGroup.add(courierFont);
    fontGroup.add(dialogFont);
    fontGroup.add(dialogInputFont);

    sizeGroup.add(smallFont);
    sizeGroup.add(mediumFont);
    sizeGroup.add(largeFont);

    // Register for event handling,

    defaultFont.addActionListener(actionListener);
    timesRomanFont.addActionListener(actionListener);
    helveticaFont.addActionListener(actionListener);
    courierFont.addActionListener(actionListener);
    dialogFont.addActionListener(actionListener);
    dialogInputFont.addActionListener(actionListener);

    smallFont.addActionListener(actionListener);
    mediumFont.addActionListener(actionListener);
    largeFont.addActionListener(actionListener);

    boldFont.addActionListener(actionListener);
  }

  public FontMenuSettings getFontMenuSettings() {
    AbstractButton tmp;
    String fontName=null;
    String fontSize=null;

    for (Enumeration e=fontGroup.getElements();e.hasMoreElements(); ){
       tmp=(AbstractButton)e.nextElement();
       if (tmp.isSelected()) {
          fontName=tmp.getText();
       }
       if (fontName!=null) break;
    }
    for (Enumeration e=sizeGroup.getElements();e.hasMoreElements(); ){
       tmp=(AbstractButton)e.nextElement();
       if (tmp.isSelected()) {
          fontSize=tmp.getText();
       }
       if (fontSize!=null) break;
    }
    return new FontMenuSettings(fontName,fontSize,boldFont.isSelected());
  }

  /**
   * Do the appropriate default clicks!  Call this only after
   * construction and after the listener's appropriate menu property
   * has been set.
   */

  public void clickDefaults(FontMenuSettings fontSettings) {
    if (fontSettings==null) {
      defaultFont.doClick();
      mediumFont.doClick();
      return;
    }
    AbstractButton tmp;
    boolean fontNameSet=false;
    boolean fontSizeSet=false;
    for (Enumeration e=fontGroup.getElements();e.hasMoreElements(); ){
       tmp=(AbstractButton)e.nextElement();
       if (tmp.getText().equals(fontSettings.getFontName())) {
          tmp.doClick();
          fontNameSet=true;
       }
       if (fontNameSet) break;
    }
    for (Enumeration e=sizeGroup.getElements();e.hasMoreElements(); ){
       tmp=(AbstractButton)e.nextElement();
       if (tmp.getText().equals(fontSettings.getSize())) {
          tmp.doClick();
          fontSizeSet=true;
       }
       if (fontSizeSet) break;
    }

    if (fontSettings.getBold()) boldFont.doClick();
  }

  public class FontMenuSettings implements java.io.Serializable {
    private String fontName;
    private String size;
    private boolean bold;

    FontMenuSettings (String fontName,String size, boolean bold) {
      this.fontName=fontName;
      this.size=size;
      this.bold=bold;
    }

    final String getFontName() {
      return fontName;
    }

    final String getSize() {
      return size;
    }

    final boolean getBold() {
      return bold;
    }
  }
}

/*
$Log$
Revision 1.7  2001/11/21 20:19:29  pdavies
Added code for:

8.1.9.2 USBAT to select a text preference option "Bold" for SubEditor
views Consensus Seq and TranscriptTranslate.
8.1.9.3 USBAT to save their text preference options so that the selected
options are invoked in following sessions.

Revision 1.6  2001/07/17 12:57:53  pdavies
Changed default font to Helvetica, 12 pt.  Changed Large Font to 14 pt.  Removed Bold.  Previously font defaulted to 14, and the small, med and large settings were 10,12 and 16?!?  Also fixed bug 108 - mouse-over tab resets view to top of scroll.

Revision 1.5  2000/05/30 13:47:41  tsaf
set mnemomics

Revision 1.4  2000/02/11 13:13:34  pdavies
Mods for new Package structure

Revision 1.3  1999/06/10 21:07:51  def
clickDefaults() methods for setting defaults.

Revision 1.2  1999/06/10 20:35:03  def
Made the members non-static

Revision 1.1  1999/06/09 22:31:01  def
The font menu!

*/
