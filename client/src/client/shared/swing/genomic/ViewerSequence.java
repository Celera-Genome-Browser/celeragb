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

import api.stub.sequence.BitStorageSequence;
import api.stub.sequence.Sequence;
import api.stub.sequence.SequenceHelper;

/**
 * ViewerSequence is a Wrapper class used internally by the SeqTableModel to return the visual
 * representation for each location.
 *
 * @author Douglas Mason
 * @version $Id$
 */

public class ViewerSequence implements Sequence {
    private Sequence sequence;

    public ViewerSequence(Sequence sequence) {
        this.sequence = sequence;
     }

    public int kind() {
        return sequence.kind();
    }

    public long length() {
        return sequence.length();
    }

    /**
     * returns the sequence representation as a string (i.e. a list of
     * ATCATC...)
     */
    public String toString(){
        return SequenceHelper.toString(sequence);
    }

    public char baseToChar(long location) {
        return SequenceHelper.baseToChar(this, location);
    }

    public int charToBase(char base) {
        return SequenceHelper.charToBase(sequence, base);
    }

    public int get(long location) {
        return sequence.get(location);
    }

    public void remove(long location) {
      ((BitStorageSequence)sequence).remove(location);
    }

    public void removeSelectedBases(long startLocation, long endLocation, int baseLength){
      ((BitStorageSequence)sequence).removeSelectedBases(startLocation,endLocation,baseLength);
    }

    public void insert(long location, int value) {
      ((BitStorageSequence)sequence).insert(location,value);
    }

    public void set(long location, int value) {
      ((BitStorageSequence)sequence).set(location,value);
    }
}