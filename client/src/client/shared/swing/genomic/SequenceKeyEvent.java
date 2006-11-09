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

import java.awt.event.KeyEvent;

public class SequenceKeyEvent extends KeyEvent {
        private long location;
        private long minAxisLocation;

        public SequenceKeyEvent(java.awt.Component component, int id, long when,
                                int modifiers, int keyCode, char keyChar, long location,
                                long minAxisLocation) {

            super(component, id, when, modifiers, keyCode, keyChar);
            this.location = location;
            this.minAxisLocation = minAxisLocation;
        }

	public char getBase() {
            return this.getKeyChar();
        }

	public long getLocation() {
            return location;
        }

        public long getAbsolutePosition() {
        return Math.abs(minAxisLocation - location);
    }
}
