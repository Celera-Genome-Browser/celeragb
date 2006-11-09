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

/**
 * Utility class to represent a range between two long values (inclusive)
 */
public class SwingRange {
    private long startRange;
    private long endRange;

    /**
     * Constructs a <code>SwingRange</code> that is initialized with
     * the given start and end locations.
     */
    public SwingRange(long start, long end) {
        startRange = start;
        endRange = end;
    }

    /**
     * Get the start location of the <code>SwingRange</code>
     * @see #setStartRange
     */
    public long getStartRange() {
        return startRange;
    }

    /**
     * Set the start location of the <code>SwingRange</code>
     * @see #getStartRange
     */
    public void setStartRange(long newStartRange) {
        startRange = newStartRange;
    }

    /**
     * Set the end location of the <code>SwingRange</code>
     * @see #getEndRange
     */
    public void setEndRange(long newEndRange) {
        endRange = newEndRange;
    }

    /**
     * Get the end location of the <code>SwingRange</code>
     * @see #setEndRange
     */
    public long getEndRange() {
        return endRange;
    }

    /**
     * Tests whether the argument location is contained by this
     * <code>SwingRange</code> object.
     * @return <code>true</code> if and only if <code>location</code>
     *         lies between the start and end range, inclusive
     */
    public boolean containsLocation(long location) {
        if (location >= Math.min(startRange, endRange) && location <= Math.max(startRange, endRange)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Compares two SwingRanges for equality.
     * @return <code>true</code> if and only if the argument is a <code>SwingRange</code>
     * 		object that represents the same range as this object.
     */
    public boolean equals (Object obj) {
        if (!(obj instanceof SwingRange)) {
            return false;
        }
        SwingRange objRange = (SwingRange)obj;
        if (this.startRange == objRange.startRange &&
            this.endRange == objRange.endRange) {
            return true;
        }
        else {
            return false;
        }
    }
}
