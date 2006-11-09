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

import javax.swing.JTextField;
import javax.swing.text.Document;


/**
 * Title:        Standard Text Field to be Used in Genome Browser
 * Description:  Wherever in the GenomeBrowser you wish to use a text field widget,
 *               use this instead of JTextField.
 * @author Les Foster
 * @version $Id$
 */
public class StandardTextField extends JTextField {
    //---------------------------------------CONSTRUCTORS

    /** Constructors merely pass through all params to super, and run init steps. */
    public StandardTextField() {
        super();
        commonInitializer();
    } // End constructor

    public StandardTextField(Document lDoc, String lText, int lColumns) {
        super(lDoc, lText, lColumns);
        commonInitializer();
    } // End constructor

    public StandardTextField(String lText, int lColumns) {
        super(lText, lColumns);
        commonInitializer();
    } // End constructor

    public StandardTextField(String lText) {
        super(lText);
        commonInitializer();
    } // End constructor

    public StandardTextField(int lColumns) {
        super(lColumns);
        commonInitializer();
    } // End constructor

    //---------------------------------------HELPERS

    /** Initialization steps common to all constructors. */
    private void commonInitializer() {
        new DataTransferMouseListener(this);
    } // End method: commonInitializer
} // End class: StandardTextField
