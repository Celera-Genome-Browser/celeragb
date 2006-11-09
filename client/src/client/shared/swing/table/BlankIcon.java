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
package client.shared.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 * Produces an Blank Icon representation.  This is used to "blank" out the arrow
 * in the SortButtonRenderer
 *
 * @version $Id$
 * @author Douglas Mason
 * @see Icon
 */
public class BlankIcon implements Icon {
    private Color fillColor;
    private int size;

    /**
     * Default Constructor
     */
    public BlankIcon() {
        this(null, 11);
    }

    /**
     * Constructor.
     */
    public BlankIcon(Color color, int size) {
        fillColor = color;
        this.size = size;
    }

   /**
    * Implements the Icon Interface. Draw the arrow icon at the specified location.
    */
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (fillColor != null) {
            g.setColor(fillColor);
            g.drawRect(x, y, size-1, size-1);
        }
    }
    /**
     * Implements the Icon Interface. Returns the icon's width.
     *
     * @return an int specifying the fixed width of the icon.
     */
    public int getIconWidth() {
        return size;
    }

    /**
     * Implements the Icon Interface. Returns the icon's height.
     *
     * @return an int specifying the fixed height of the icon.
     */
    public int getIconHeight() {
        return size;
    }
}


