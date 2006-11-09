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
package client.shared.swing.genomic;

import java.awt.event.MouseEvent;
import java.awt.Component;

public class SequenceMouseEvent extends MouseEvent {
    private long beginLocation;
    private long endLocation;
    private long minAxisLocation;

    public SequenceMouseEvent(Component source, int id, long when, int modifiers,
                      int x, int y, int clickCount, boolean popupTrigger, long beginLocation,

                      long endLocation, long minAxisLocation) {
        super(source, id, when, modifiers, x, y, clickCount, popupTrigger);
        this.beginLocation = beginLocation;
        this.endLocation = endLocation;
        this.minAxisLocation = minAxisLocation;
    }

    public long getBeginLocation() {
        return beginLocation;
    }

    public long getEndLocation() {
        return endLocation;
    }

    public long getSequencePosition() {
        return Math.abs(minAxisLocation - beginLocation);
    }
}