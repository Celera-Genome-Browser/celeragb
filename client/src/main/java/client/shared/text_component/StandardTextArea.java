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
package client.shared.text_component;

import javax.swing.*;
import javax.swing.text.Document;

/**
 * Title:        Standard Text Area to be Used in Genome Browser
 * Description:  Wherever in the Genome Browser you wish to place a text area,
 *               use this instead of JTextArea
 * @author Les Foster
 * @version $Id$
 */

public class StandardTextArea extends JTextArea {

  //---------------------------------------UNIT-TEST CODE
  /** Presents an instance of this widget to play with. */
  public static void main(String[] lArgs) {
    try {
      javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ex) {}
    JTextArea lArea = new StandardTextArea();
    lArea.setText("Much ado about nothing");
    lArea.setEditable(false);
    javax.swing.JFrame lFrame = new javax.swing.JFrame("Try Right-Click");
    lFrame.setSize(400, 400);
    lFrame.setLocation(100, 100);
    lFrame.getContentPane().add(lArea);
    lFrame.setVisible(true);
    lFrame.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent we) {
        System.exit(0);
      }
    });
  } // End main

  //---------------------------------------CONSTRUCTORS
  /** Constructors simply call standard initialization. */
  public StandardTextArea() {
    super();
    commonInitializer();
  } // End constructor

  public StandardTextArea(String lText) {
    super(lText);
    commonInitializer();
  } // End constructor

  public StandardTextArea(Document lDoc) {
    super(lDoc);
    commonInitializer();
  } // End constructor

  public StandardTextArea(Document lDoc, String lText, int lRows, int lColumns) {
    super(lDoc, lText, lRows, lColumns);
    commonInitializer();
  } // End constructor

  public StandardTextArea(int lRows, int lColumns) {
    super(lRows, lColumns);
    commonInitializer();
  } // End constructor

  public StandardTextArea(String lText, int lRows, int lColumns) {
    super(lText, lRows, lColumns);
    commonInitializer();
  } // End constructor

  //---------------------------------------HELPERS
  /** Initialization steps common to all constructors. */
  private void commonInitializer() {
    new DataTransferMouseListener(this);
  } // End method: commonInitializer

} // End class: StandardTextArea
