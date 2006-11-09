// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package api.stub.sequence;

import shared.util.Assert;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class DNA
{
    public static final int NUM_NUCLEOTIDES = 4;

    public static final int A = 0;
    public static final int C = 1;
    public static final int G = 2;
    public static final int T = 3;
    //if you want to change this value look at baseToChar
    public static final int NOT_LOADED = Sequence.UNKNOWN - 1;
    /**
     * Return the character corresponding to a given base.
     * A is converted to 'A', etc.
     */
    public static char baseToChar(int base) {
        return ".NACGT".charAt(base+2);
    }

    /**
     * Return the base corresponding to a given character.
     * 'a' and 'A' are converted to the base A, etc.
     */
    public static int charToBase(char c) {
	switch(c) {
	  case 'a': case 'A': return A;
	  case 't': case 'T': return T;
	  case 'c': case 'C': return C;
	  case 'g': case 'G': return G;
	  default:            return Sequence.UNKNOWN;
	}
    }

    public static int complement(int base) {
        switch(base) {
            case A:  return T;
            case T:  return A;
            case C:  return G;
            case G:  return C;
	    case NOT_LOADED : return NOT_LOADED;
            default: return Sequence.UNKNOWN;
        }
    }

    public static String toString(Sequence seq) {
        if (Assert.debug) Assert.vAssert(seq.kind() == seq.KIND_DNA);

        int length = (int)seq.length();
        StringBuffer s = new StringBuffer(length);
        for(int i = 0; i < length; ++i) {
            s.append(baseToChar(seq.get(i)));
        }
        return s.toString();
    }

    public static Sequence reverseComplement(final Sequence seq) {
        return new Sequence() {
                private long length = seq.length();
                public int kind() { return KIND_DNA; }
                public long length() { return length; }
                public int get(long i) { return complement(seq.get(length - 1 - i)); }
            };
    }

    public static boolean isEqual(Sequence seq, String s) {
        for(int i = 0; i < s.length(); ++i) {
            if (seq.get(i) != charToBase(s.charAt(i)))
                return false;
        }
        return true;
    }

    public static void toFASTA(Sequence seq, String defLine, OutputStream unbufferedOut) throws IOException {
        if (Assert.debug) Assert.vAssert(seq.kind() == seq.KIND_DNA);

        BufferedOutputStream out = new BufferedOutputStream(unbufferedOut, 4 * 1024);

        final int LINE_LENGTH = 80;

        out.write(defLine.getBytes());
        out.write('\n');

        final int seqLength = (int)seq.length();
        int lineChars = 0;
        for(int i = 0; i < seqLength; ++i) {
            out.write(baseToChar(seq.get(i)));
            if (++lineChars == LINE_LENGTH || i + 1 == seqLength) {
                lineChars = 0;
                out.write('\n');
            }
        }

        out.flush();
    }
}









