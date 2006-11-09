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

import java.io.Serializable;


/**
 * ProteinSequenceStorage is a Sequence that provides its own
 * in-memory storage.
 */
public class ProteinSequenceStorage
    implements Sequence,
               Serializable
{
    private byte[] bytes;

    public ProteinSequenceStorage(final String seq, String description) {
        this(new Sequence() {
                public int kind() { return KIND_PROTEIN; }
                public long length() { return seq.length(); }
                public int get(long i) { return Protein.charToProtein(seq.charAt((int)i)); }
            });
    }

    public ProteinSequenceStorage(Sequence sequence) {
        if (Assert.debug) Assert.vAssert(sequence.kind() == Sequence.KIND_PROTEIN);

        bytes = new byte[(int)sequence.length()];
        for(int i = 0; i < bytes.length; ++i) {
            bytes[i] = (byte)sequence.get(i);
        }
    }

    public int kind() {
        return KIND_PROTEIN;
    }

    public long length() {
        return bytes.length;
    }

    public int get(long location) {
        return bytes[(int)location];
    }

    public void set(long location, int protein) {
        bytes[(int)location] = (byte)protein;
    }
}
