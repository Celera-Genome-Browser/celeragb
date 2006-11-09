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

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */




/**
 * The purpose of this class is to give the user the proper sequence string
 * via determining Sequence type for them.
 */
public class SequenceHelper {

  public SequenceHelper() {
  }

  public static String toString(Sequence sequence){
    if (sequence.kind()==Sequence.KIND_DNA) {
      return DNA.toString(sequence);
    }
    else if (sequence.kind()==Sequence.KIND_PROTEIN) {
      return Protein.toString(sequence);
    }
    return "";
  }

  public static char baseToChar(Sequence sequence, long location) {
    int base = sequence.get(location);
    if (sequence.kind()==Sequence.KIND_DNA) {
      return DNA.baseToChar(base);
    }
    else if (sequence.kind()==Sequence.KIND_PROTEIN) {
      return Protein.proteinToChar(base);
    }
    return ' ';
  }

  public static int charToBase(Sequence sequence, char base) {
    if (sequence.kind()==Sequence.KIND_DNA) {
      return DNA.charToBase(base);
    }
    else if (sequence.kind()==Sequence.KIND_PROTEIN) {
      return Protein.charToProtein(base);
    }
    return ' ';
  }

    public static Sequence sequenceToStorageDNA(Sequence seq) {
        boolean nothingKnown = true;
        for(long i = 0; i < seq.length(); ++i) {
            if (seq.get(i) != seq.UNKNOWN) {
                nothingKnown = false;
                break;
            }
        }

        return nothingKnown
                ? (Sequence)new UnknownSequence(seq.KIND_DNA, seq.length())
                : (Sequence)DNASequenceStorage.create(seq);
    }

    public static Sequence toReverseComplement(final Sequence seq) {
        return new Sequence() {
                private long length = seq.length();
                public int kind() { return KIND_DNA; }
                public long length() { return length; }
                public int get(long i) { return DNA.complement(seq.get(length - 1 - i)); }
            };
    }
}