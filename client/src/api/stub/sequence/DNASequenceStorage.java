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

import java.io.Serializable;

/**
 * DNASequenceStorage is a Sequence that provides its own
 * in-memory storage.
 * Memory requirements are minimized, as only 2 or 3 bits are used per base
 * (3 are necessary if the sequence has unknown values).
 */
public class DNASequenceStorage extends BitStorageSequence implements Serializable {
   /**
    * Return a Sequence whose bases are obtained by converting the
    * given array of bytes.
    */
   public static DNASequenceStorage create(final byte[] a) {
      return create(new Sequence() {
         public int kind() {
            return KIND_DNA;
         }
         public long length() {
            return a.length;
         }
         public int get(long i) {
            return (i < 0 || i >= a.length) ? UNKNOWN : DNA.charToBase((char) a[(int) i]);
         }
      });
   }

   public static DNASequenceStorage create(final String seq, String description) {
      return create(new Sequence() {
         public int kind() {
            return KIND_DNA;
         }
         public long length() {
            return seq.length();
         }
         public int get(long i) {
            return DNA.charToBase(seq.charAt((int) i));
         }
      });
   }

   public static DNASequenceStorage create(Sequence sequence) {
      try {
         return new DNASequenceStorage(sequence, true);
      }
      catch (UnknownBaseError ex) {
         return new DNASequenceStorage(sequence, false);
      }
   }

   /**
    * Creates a new DNASequenceStorage initialized with the given sequence.
    * If noUnknownBases is true, it means that this DNASequenceStorage will NEVER
    * be used to store unknown bases. If an attempt is made to set UNKNOWN, an
    * exception will be thrown. The advantage is that only 2 bits of storage are
    * required per base.
    */
   public DNASequenceStorage(Sequence sequence, boolean allBasesKnown) {
      super(sequence, allBasesKnown ? 2 : 3);
   }

   public DNASequenceStorage(int length, boolean allBasesKnown) {
      super(length, allBasesKnown ? 2 : 3);
   }

   public int kind() {
      return KIND_DNA;
   }

   public int get(long location) {
      int base = super.get(location);
      return (base < 4) ? base : UNKNOWN;
   }

   public void set(long index, int base) {
      if (base == UNKNOWN && bitsPerValue() <= 2)
         throw new UnknownBaseError();
      super.set(index, base);
   }

   public static class UnknownBaseError extends Error {
   }
}
