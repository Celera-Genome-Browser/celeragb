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
import java.util.BitSet;


/**
 * @todo
 */
public abstract class BitStorageSequence
    implements Sequence,
               Serializable
{
    private final int bitsPerValue;
    private int length = 0;
    private BitSet bits = new BitSet();

    public static final long serialVersionUID = 3218235520419776303L;
    
    public BitStorageSequence(Sequence sequence, int bitsPerValue) {
        this.bitsPerValue = bitsPerValue;
        append(sequence);
    }

    protected BitStorageSequence(int length, int bitsPerValue) {
        this.bitsPerValue = bitsPerValue;
        ensureCapacity(length);
    }

    public long length() {
        return length;
    }

    public int get(long location) {
        if (location < 0 || location >= length)
            return UNKNOWN;

        int bit = (int)location * bitsPerValue;
        int value = 0;
        for(int i = 0; i < bitsPerValue; ++i) {
            if (bits.get(bit + i))
                value |= 1 << i;
        }
        return value;
    }

    public int bitsPerValue() {
        return bitsPerValue;
    }

    public void set(long location, int value) {
        if (Assert.debug) Assert.vAssert(location >= 0 && location < length);

        int bit = (int)location * bitsPerValue;
        for(int i = 0; i < bitsPerValue; ++i) {
            if ((value & (1 << i)) != 0)
                bits.set(bit + i);
            else
                bits.clear(bit + i);
        }
    }

    /**
     * Appends the given sequence at the end of this sequence.
     */
    public void append(Sequence sequence) {
        int start = length;
        int n = (int)sequence.length();
        ensureCapacity(start + n);
        for(int i = 0; i < n; ++i) {
            set(start + i, sequence.get(i));
        }
    }

    public void insert(long location, int value) {
        int n = this.length;
        ensureCapacity(length++);
        for (long i = n; i >= location; --i) {
            set(i+1, get(i));
        }
        set(location, value);
    }

    public void remove(long location) {
        int n = this.length;

        ensureCapacity(length--);

        long k=location;
        for (long i =0 ; i <n-1-k; i++) {
            set(location, get(location+1));
            location++;
        }
        length=length-1;
    }


    public void removeSelectedBases(long startLocation, long endLocation, int baseLength){
      int k=this.length;
      if ((int)endLocation==k-1){
         length=length-baseLength;
         return;
      }

      for(int i=0;i < k - 1 - endLocation;i++){
          set(startLocation+i, get(endLocation+1+i));
      }
      length=length-baseLength;
    }

    //The purpose of this method is to avoid that the underlying array gets
    //reallocated more than once.
    private void ensureCapacity(int n) {
        if (n > length) {
            length = n;
            int bit = length * bitsPerValue;
            bits.set(bit);
            bits.clear(bit);
        }
    }
}
