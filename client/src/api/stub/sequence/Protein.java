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
import java.util.HashMap;


public class Protein
{
    // Provide the number of Proteins known to the utility.
    public static final int NUM_PROTEINS = 24;
    public static final int FRAME_PLUS_ONE = 1;
    public static final int FRAME_PLUS_TWO = 2;
    public static final int FRAME_PLUS_THREE = 3;
    public static final int FRAME_NEGATIVE_ONE = -1;
    public static final int FRAME_NEGATIVE_TWO = -2;
    public static final int FRAME_NEGATIVE_THREE = -3;

    private static final int BASE_CHAR  = 0;
    private static final int THREE_CHAR = 1;
    private static final int FULL_NAME  = 2;
    final static public int ONE_LETTER_TRANSLATION = 1;
    final static public int THREE_LETTER_TRANSLATION = 3;

    public static final int G = 'G';
    public static final int A = 'A';
    public static final int V = 'V';
    public static final int L = 'L';
    public static final int I = 'I';
    public static final int S = 'S';
    public static final int T = 'T';
    public static final int F = 'F';
    public static final int Y = 'Y';
    public static final int W = 'W';
    public static final int K = 'K';
    public static final int R = 'R';
    public static final int H = 'H';
    public static final int D = 'D';
    public static final int E = 'E';
    public static final int N = 'N';
    public static final int Q = 'Q';
    public static final int C = 'C';
    public static final int M = 'M';
    public static final int P = 'P';
    public static final int STOP = '*';

    //@todo check spelling
    public static final int GLYCINE = G;
    public static final int ALANINE = A;
    public static final int VALINE = V;
    public static final int LEUCINE = L;
    public static final int ISOLEUCINE = I;
    public static final int SERINE = S;
    public static final int THREONINE = T;
    public static final int PHENYLALANINE = F;
    public static final int TYROSINE = Y;
    public static final int TRIPTOPHAN = W;
    public static final int LYSINE = K;
    public static final int ARGININE = R;
    public static final int HISTIDINE = H;
    public static final int ASPAR = D;
    public static final int GLUTAM = E;
    public static final int ASN = N;
    public static final int GLN = Q;
    public static final int CYSTEIN = C;
    public static final int METHIONINE = M;
    public static final int PROLYNE = P;
    public static final int STOP_CODON = STOP;

    /**
     * Maps that allow for fast lookup of protein names based on what is currently
     * available.  ie. singleLetterMap has a single letter key and value of "row" in
     * the proteinArray.  The developer can then get whatever translation of the
     * protein they want.  The other maps work in the same way but key off of the
     * three-letter or full name respectively.
     */
    private static HashMap singleLetterMap = new HashMap(70, 10);
    private static HashMap threeLetterMap = new HashMap(70, 10);
    private static HashMap fullNameMap = new HashMap(70, 10);


    private static int[] basesToProtein = {
      K, Q, E, STOP, T, P, A, S, R, R, G, STOP, I, L, V, L, N, H,
      D, Y, T, P, A, S, S, R, G, C, I, L, V, F, K, Q, E, STOP, T,
      P, A, S, R, R, G, W, M, L, V, L, N, H, D, Y, T, P, A, S, S,
      R, G, C, I, L, V, F};



    private static String[][] proteinArray = {
        {"K", "Lys", "Lysine"},
        {"Q", "Gln", "Glutamine"},
        {"E", "Glu", "Glutamate"},
        {"*", "***", "***"},
        {"T", "Thr", "Threonine"},
        {"P", "Pro", "Proline"},
        {"A", "Ala", "Alanine"},
        {"S", "Ser", "Serine"},
        {"R", "Arg", "Arginine"},
        {"R", "Arg", "Arginine"},
        {"G", "Gly", "Glycine"},
        {"*", "***", "***"},
        {"I", "Ile", "Isoleucine"},
        {"L", "Leu", "Leucine"},
        {"V", "Val", "Valine"},
        {"L", "Leu", "Leucine"},
        {"N", "Asn", "Asparagin"},
        {"H", "His", "Histadine"},
        {"D", "Asp", "Aspartate"},
        {"Y", "Tyr", "Tyrosine"},
        {"T", "Thr", "Threonine"},
        {"P", "Pro", "Proline"},
        {"A", "Ala", "Alanine"},
        {"S", "Ser", "Serine"},
        {"S", "Ser", "Serine"},
        {"R", "Arg", "Arginine"},
        {"G", "Gly", "Glycine"},
        {"C", "Cys", "Cysteine"},
        {"I", "Ile", "Isoleucine"},
        {"L", "Leu", "Leucine"},
        {"V", "Val", "Valine"},
        {"F", "Phe", "Phenylalanine"},
        {"K", "Lys", "Lysine"},
        {"Q", "Gln", "Glutamine"},
        {"E", "Glu", "Glutamate"},
        {"*", "***", "***"},
        {"T", "Thr", "Threonine"},
        {"P", "Pro", "Proline"},
        {"A", "Ala", "Alanine"},
        {"S", "Ser", "Serine"},
        {"R", "Arg", "Arginine"},
        {"R", "Arg", "Arginine"},
        {"G", "Gly", "Glycine"},
        {"W", "Trp", "Tryptophan"},
        {"M", "Met", "Methionine"},
        {"L", "Leu", "Leucine"},
        {"V", "Val", "Valine"},
        {"L", "Leu", "Leucine"},
        {"N", "Asn", "Asparagin"},
        {"H", "His", "Histadine"},
        {"D", "Asp", "Aspartate"},
        {"Y", "Tyr", "Tyrosine"},
        {"T", "Thr", "Threonine"},
        {"P", "Pro", "Proline"},
        {"A", "Ala", "Alanine"},
        {"S", "Ser", "Serine"},
        {"S", "Ser", "Serine"},
        {"R", "Arg", "Arginine"},
        {"G", "Gly", "Glycine"},
        {"C", "Cys", "Cysteine"},
        {"I", "Ile", "Isoleucine"},
        {"L", "Leu", "Leucine"},
        {"V", "Val", "Valine"},
        {"F", "Phe", "Phenylalanine"}
    };


    /**
     * Inits some maps for fast acquiring of protein translations whether they be
     * one-letter, three-letter, or full named.
     */
    static {
        for (int i = 0; i < proteinArray.length; i++) {
            singleLetterMap.put(new String(proteinArray[i][BASE_CHAR])  , new Integer(i));
            threeLetterMap.put (new String(proteinArray[i][THREE_CHAR]) , new Integer(i));
            fullNameMap.put(new String(proteinArray[i][FULL_NAME])      , new Integer(i));
        }
    }


    public static char proteinToChar(int p) {
        return (char)p;
    }

    public static String proteinToAbbreviatedName(int p) {
        if (p == Sequence.UNKNOWN) return "???";
        int index = ((Integer)singleLetterMap.get(new Character((char)p).toString().toUpperCase())).intValue();
	return proteinArray[index][THREE_CHAR];
    }

    public static String proteinToFullName(int p) {
        if ( p == Sequence.UNKNOWN) return "Unknown";
        int index = ((Integer)singleLetterMap.get(new Character((char)p).toString().toUpperCase())).intValue();
	return proteinArray[index][FULL_NAME];
    }

    public static String abbreviatedNameToProtein(String p) {
        if (threeLetterMap.get(p)==null) return "?";
        int index = ((Integer)threeLetterMap.get(p)).intValue();
	return proteinArray[index][BASE_CHAR];
    }

    public static String abbreviatedNameToFullName(String p) {
        if (threeLetterMap.get(p)==null) return "Unknown";
        int index = ((Integer)threeLetterMap.get(p)).intValue();
	return proteinArray[index][FULL_NAME];
    }

    public static String fullNameToProtein(String p) {
        if (fullNameMap.get(p)==null) return "?";
        int index = ((Integer)fullNameMap.get(p)).intValue();
	return proteinArray[index][BASE_CHAR];
    }

    public static String fullNameToAbbreviatedName(String p) {
        if (fullNameMap.get(p)==null) return "Unknown";
        int index = ((Integer)fullNameMap.get(p)).intValue();
	return proteinArray[index][THREE_CHAR];
    }


    public static int charToProtein(char c) {
        return c;
    }

    public static String toString(Sequence seq) {
        if (Assert.debug) Assert.vAssert(seq.kind() == seq.KIND_PROTEIN);

        int length = (int)seq.length();
        StringBuffer s = new StringBuffer(length);
        for(int i = 0; i < length; ++i) {
            s.append(proteinToChar(seq.get(i)));
        }
        return s.toString();
    }

    public static int basesToProtein(int b1, int b2, int b3) {
        if (b1 == Sequence.UNKNOWN || b2 == Sequence.UNKNOWN || b3 == Sequence.UNKNOWN)
            return Sequence.UNKNOWN;
        return basesToProtein[b1 | (b2 << 2) | (b3 << 4)];
    }

    public static Sequence convertDNASequence(final Sequence dna) {
        return new Sequence() {
                public int kind() { return KIND_PROTEIN; }
                public long length() { return dna.length() / 3; }
                public int get(long i) {
                    long j = 3 * i;
                    // Return a "gap" character if the conversion is unknown or gapped.
                    if (basesToProtein(dna.get(j), dna.get(j+1), dna.get(j+2))==Sequence.UNKNOWN)
                      return '-';
                    else return basesToProtein(dna.get(j), dna.get(j+1), dna.get(j+2));
                }
            };
    }


    /**
     * This method is supposed to alleviate the need for the developer to navigate
     * the sequence classes to go from DNA to specific ORF's on their own.   The original
     * DNA sequence is passed as there is no way to go from a Protein sequence down
     * to DNA sequence.  The wobble nucleotide base means that 1 protein has >= 1
     * combination of bases to create it.  Returns null if there is a problem with the
     * dnaSequence or desired ORF passed in.
     */
    public static Sequence convertDNASequenceToProteinORF(final Sequence dnaSequence, int desiredOpenReadingFrame) {
      if (dnaSequence == null) return null;
      Sequence shiftedDNASequence = dnaSequence;
      switch (desiredOpenReadingFrame) {
        case FRAME_PLUS_ONE: {
          shiftedDNASequence = dnaSequence;
          break;
        }
        case FRAME_PLUS_TWO: {
          shiftedDNASequence = new ShiftedSequence(-1, dnaSequence);
          break;
        }
        case FRAME_PLUS_THREE: {
          shiftedDNASequence = new ShiftedSequence(-2, dnaSequence);
          break;
        }
        case FRAME_NEGATIVE_ONE: {
          shiftedDNASequence = DNA.reverseComplement(dnaSequence);
          break;
        }
        case FRAME_NEGATIVE_TWO: {
          shiftedDNASequence = new ShiftedSequence(-1, DNA.reverseComplement(dnaSequence));
          break;
        }
        case FRAME_NEGATIVE_THREE: {
          shiftedDNASequence = new ShiftedSequence(-2, DNA.reverseComplement(dnaSequence));
          break;
        }
        default: {
          return null;
        }
      }
      return Protein.convertDNASequence(shiftedDNASequence);
    }


    /**
     * This method assists in the bulk transformation from one protein style to
     * another.  Returns a blank sequence if not 1 or 3 letter translation asked for.
     */
    public static Sequence convertProteinSequenceStyle(Sequence residue, int translationStyle) {
      StringBuffer tmpBuffer = new StringBuffer("");

      // Convert from implied Three to Single letter translation of anino acid
      if (translationStyle == ONE_LETTER_TRANSLATION) {
        String tmpString = SequenceHelper.toString(residue);
        for (long x = 0; x < residue.length();) {
          tmpBuffer.append(Protein.abbreviatedNameToProtein(tmpString.substring((int)(x*3), (int)(x*3)+2)));
          x+=3;
        }
      }

      // Convert from implied Single to Three letter translation of anino acid
      else if (translationStyle == THREE_LETTER_TRANSLATION) {
        for (long x = 0; x < residue.length(); x++) {
          System.out.println("Residue: "+residue.get(x));
          tmpBuffer.append(Protein.proteinToAbbreviatedName(residue.get(x)));
        }
      }
      return new ProteinSequenceStorage(tmpBuffer.toString(), "");
    }


    public static void toFASTA(Sequence seq, String defLine, OutputStream unbufferedOut) throws IOException {
        if (Assert.debug) Assert.vAssert(seq.kind() == seq.KIND_PROTEIN);

        BufferedOutputStream out = new BufferedOutputStream(unbufferedOut, 4 * 1024);

        final int LINE_LENGTH = 80;

        out.write(defLine.getBytes());
        out.write('\n');

        final int seqLength = (int)seq.length();
        int lineChars = 0;
        for(int i = 0; i < seqLength; ++i) {
            out.write(proteinToChar(seq.get(i)));
            if (++lineChars == LINE_LENGTH || i + 1 == seqLength) {
                lineChars = 0;
                out.write('\n');
            }
        }

        out.flush();
    }
}
