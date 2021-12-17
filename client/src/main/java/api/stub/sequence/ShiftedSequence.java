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
package api.stub.sequence;

import shared.util.Assert;


/**
 * The purpose of ShiftedSequence is to provide a shifted
 * sequence without allocating new memory for the elements.
 *
 * THIS CLASS IS NOT NATIVELY RELATED TO GENOMICS
 * BUT ONLY RAW SEQUENCES OF BITS
 */
public class ShiftedSequence implements Sequence
{
    private long location;
    private Sequence sequence;

    /**
     * Creates a new ShiftedSequence.
     * No memory is allocated. Elements are accessed from the original
     * sequence.
     *
     * Examples:
     *     - ATCGAT with location == 1  gives  XATCGAT
     *     - ATCGAT with location == -1 gives TCGAT
     */
    public ShiftedSequence(long location, Sequence sequence) {
        if (Assert.debug)
            Assert.vAssert(location + sequence.length() > 0);
	this.location = location;
	this.sequence = sequence;
    }

    public int kind() {
        return sequence.kind();
    }

    public long length() {
	return location + sequence.length();
    }

    public int get(long i) {
        if (Assert.debug)
            Assert.vAssert(i >= 0);
	return (i < location) ? UNKNOWN : sequence.get(i - location);
    }

    public Sequence referencedSequence() {
        return sequence;
    }

    public long locationInReferencedSequence() {
        return location;
    }
}
