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

public class SequenceAdjustmentEvent extends java.awt.event.AdjustmentEvent {
    private long visibleBeginLocation = Long.MIN_VALUE;
    private long visibleEndLocation = Long.MIN_VALUE;

    public SequenceAdjustmentEvent(java.awt.Adjustable source, int id, int type,
                                    int value, long visibleBeginLocation,
                                    long visibleEndLocation) {
        super(source, id, type, value);
        this.visibleBeginLocation = visibleBeginLocation;
        this.visibleEndLocation = visibleEndLocation;
    }
    public long getVisibleBeginLocation() {
        return this.visibleBeginLocation;
    }

    public long getVisibleEndLocation() {
        return this.visibleEndLocation;
    }
}