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
package client.gui.framework.browser;

import java.awt.*;
import java.io.Serializable;

/**
 * @author grahamkj
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
class BrowserPosition implements Serializable {
     private Dimension browserSize;
     private Dimension screenSize;
     private Point browserLocation;
     private int verticalDividerLocation, horizontalDividerLocation;

     void setVerticalDividerLocation(int location) {
        verticalDividerLocation=location;
     }

     void setHorizontalDividerLocation(int location) {
        horizontalDividerLocation=location;
     }

     int getVerticalDividerLocation() {
        return verticalDividerLocation;
     }

     int getHorizontalDividerLocation() {
        return horizontalDividerLocation;
     }

     Dimension getScreenSize() {
        return screenSize;
     }

     void setScreenSize(Dimension dimension) {
        screenSize=dimension;
     }

     Dimension getBrowserSize() {
        return browserSize;
     }

     Point getBrowserLocation() {
        return browserLocation;
     }

     void setBrowserLocation(Point location) {
       browserLocation=location;
     }

     void setBrowserSize(Dimension dimension) {
        browserSize=dimension;
     }

}
