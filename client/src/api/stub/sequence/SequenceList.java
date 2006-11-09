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

import java.util.ArrayList;


/**
 * The purpose of SequenceList is to catenate multiple sequences
 * without allocating new memory for the elements:
 * the elements are retrieved from the sequences composing the list.
 */
public class SequenceList implements Sequence
{
    private int kind;
    private ArrayList list = new ArrayList();
    private ShiftedSequence lastUsed;

    public SequenceList(int kind) {
        this.kind = kind;
    }

    public int kind() {
        return kind;
    }

    public long length() {
        return (list.size() == 0) ? 0 : lastSequence().length();
    }

    public int get(long location) {
        if (!isAvailableInLastUsed(location))
            lastUsed = sequence(indexFor(location));
        return lastUsed.get(location);
    }

    /**
     * Append the given sequence at the end of this sequence.
     */
    public void append(Sequence sequence) {
        list.add(new ShiftedSequence(length(), sequence));
    }

    /**
     * Insert the given sequence at the given position.
     */
    public void insert(long location, Sequence sequence) {
        int i = indexFor(location);
        if (Assert.debug)
            Assert.vAssert(i == list.size() || location < sequence(i).locationInReferencedSequence());
        list.add(i, new ShiftedSequence(location, sequence));
    }

    private int indexFor(long location) {
        int min = 0;
        int max = list.size();
        while(min + 1 < max) {
            int i = (min + max) / 2;
            if (location < sequence(i).length())
                max = i;
            else
                min = i+1;
        }
        return (min == max || location < sequence(min).length())
                        ? min : max;
    }

    private boolean isAvailableInLastUsed(long location) {
        return lastUsed != null &&
               location >= lastUsed.locationInReferencedSequence() &&
               location < lastUsed.length();
    }

    ShiftedSequence sequence(int i) {
        return (ShiftedSequence)list.get(i);
    }

    private ShiftedSequence lastSequence() {
        return sequence(list.size() - 1);
    }
}
