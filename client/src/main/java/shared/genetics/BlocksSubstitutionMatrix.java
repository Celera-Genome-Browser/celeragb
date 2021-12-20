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
package shared.genetics;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 * parses substitution matrix files that come with BLAST programs.
 * Comment lines start with a hash character (#).
 * The first non-comment line contains matrix column headers,
 * each a single letter amino acid code.
 * The rest of the lines start with a row header
 * (single-letter amino acid code),
 * followed by integers for each matrix position.
 */
public class BlocksSubstitutionMatrix {

   static int[] proteinToBlosumPosition = new int[256];
   protected static char[] blosumPositionToProtein = new char[27];

   static {
      for (int i = 0; i < proteinToBlosumPosition.length; i++) {
         proteinToBlosumPosition[i] = -1;
      }
      proteinToBlosumPosition['A'] = 0; // Alanine
      proteinToBlosumPosition['R'] = 1; // Arginine
      proteinToBlosumPosition['N'] = 2; // Asparagine
      proteinToBlosumPosition['D'] = 3; // Aspartate
      proteinToBlosumPosition['C'] = 4; // Cysteine
      proteinToBlosumPosition['Q'] = 5; // Glutamine
      proteinToBlosumPosition['E'] = 6; // Glutamate
      proteinToBlosumPosition['G'] = 7; // Glycine
      proteinToBlosumPosition['H'] = 8; // Histidine
      proteinToBlosumPosition['I'] = 9; // Isoleucine
      proteinToBlosumPosition['L'] = 10; // Leucine
      proteinToBlosumPosition['K'] = 11; // Lysine
      proteinToBlosumPosition['M'] = 12; // Methionine
      proteinToBlosumPosition['F'] = 13; // Phenylalanine
      proteinToBlosumPosition['P'] = 14; // Proline
      proteinToBlosumPosition['S'] = 15; // Serine
      proteinToBlosumPosition['T'] = 16; // Threonine
      proteinToBlosumPosition['W'] = 17; // Tryptophan
      proteinToBlosumPosition['Y'] = 18; // Tyrosine
      proteinToBlosumPosition['V'] = 19; // Valine
      proteinToBlosumPosition['B'] = 20; // ???
      proteinToBlosumPosition['Z'] = 21; // ???
      proteinToBlosumPosition['X'] = 22; // unknown??
      proteinToBlosumPosition['*'] = 23; // gap? unknown? stop?
      proteinToBlosumPosition['-'] = 24; // gap / unknown
      proteinToBlosumPosition[' '] = 25; // gap / unknown
      proteinToBlosumPosition['?'] = 26; // gap / unknown

      proteinToBlosumPosition['a'] = 0; // Alanine
      proteinToBlosumPosition['r'] = 1; // Arginine
      proteinToBlosumPosition['n'] = 2; // Asparagine
      proteinToBlosumPosition['d'] = 3; // Aspartate
      proteinToBlosumPosition['c'] = 4; // Cysteine
      proteinToBlosumPosition['q'] = 5; // Glutamine
      proteinToBlosumPosition['e'] = 6; // Glutamate
      proteinToBlosumPosition['g'] = 7; // Glycine
      proteinToBlosumPosition['h'] = 8; // Histidine
      proteinToBlosumPosition['i'] = 9; // Isoleucine
      proteinToBlosumPosition['l'] = 10; // Leucine
      proteinToBlosumPosition['k'] = 11; // Lysine
      proteinToBlosumPosition['m'] = 12; // Methionine
      proteinToBlosumPosition['f'] = 13; // Phenylalanine
      proteinToBlosumPosition['p'] = 14; // Proline
      proteinToBlosumPosition['s'] = 15; // Serine
      proteinToBlosumPosition['t'] = 16; // Threonine
      proteinToBlosumPosition['w'] = 17; // Tryptophan
      proteinToBlosumPosition['y'] = 18; // Tyrosine
      proteinToBlosumPosition['v'] = 19; // Valine
      proteinToBlosumPosition['b'] = 20; // ???
      proteinToBlosumPosition['z'] = 21; // ???
      proteinToBlosumPosition['x'] = 22; // unknown??

      blosumPositionToProtein[0] = 'A'; // Alanine
      blosumPositionToProtein[1] = 'R'; // Arginine
      blosumPositionToProtein[2] = 'N'; // Asparagine
      blosumPositionToProtein[3] = 'D'; // Aspartate
      blosumPositionToProtein[4] = 'C'; // Cysteine
      blosumPositionToProtein[5] = 'Q'; // Glutamine
      blosumPositionToProtein[6] = 'E'; // Glutamate
      blosumPositionToProtein[7] = 'G'; // Glycine
      blosumPositionToProtein[8] = 'H'; // Histidine
      blosumPositionToProtein[9] = 'I'; // Isoleucine
      blosumPositionToProtein[10] = 'L'; // Leucine
      blosumPositionToProtein[11] = 'K'; // Lysine
      blosumPositionToProtein[12] = 'M'; // Methionine
      blosumPositionToProtein[13] = 'F'; // Phenylalanine
      blosumPositionToProtein[14] = 'P'; // Proline
      blosumPositionToProtein[15] = 'S'; // Serine
      blosumPositionToProtein[16] = 'T'; // Threonine
      blosumPositionToProtein[17] = 'W'; // Tryptophan
      blosumPositionToProtein[18] = 'Y'; // Tyrosine
      blosumPositionToProtein[19] = 'V'; // Valine
      blosumPositionToProtein[20] = 'B'; // ???
      blosumPositionToProtein[21] = 'Z'; // ???
      blosumPositionToProtein[22] = 'X'; // unknown??
      blosumPositionToProtein[23] = '*'; // gap? unknown?
      blosumPositionToProtein[24] = '-'; // gap / unknown
      blosumPositionToProtein[25] = ' '; // gap / unknown
      blosumPositionToProtein[26] = '?'; // gap / unknown
   }

   public BlocksSubstitutionMatrix() {
   }

   public Object importContent(InputStream theInput) throws IOException {
      BufferedReader dis;
 
      SubstitutionScoreMatrix substitutionScoreMatrix = new SubstitutionScoreMatrix();

      dis = new BufferedReader( new InputStreamReader( theInput ) );
      try {
         String line;
         String field, row_header;
         char column_headers[] = new char[24];
         char row_char, column_char;
         StringTokenizer tokens;
         int column = 0;
         boolean no_header_yet = true;
         double val;

         while (null != (line = dis.readLine())) {
            column = 0;

            // Ignore the comments
            if (line.startsWith("#")) {
               continue;
            }
            tokens = new StringTokenizer(line);

            // Read in the column names
            if (no_header_yet && tokens.hasMoreElements()) {
               int index = 0;
               while (tokens.hasMoreElements()) {
                  field = tokens.nextToken();
                  column_headers[index++] = field.charAt(0);
               }
               no_header_yet = false;
               continue;
            }

            // Ignore the row number
            if (tokens.hasMoreElements()) {
               row_header = tokens.nextToken();
            }
            else {
               // Ignore blank lines.
               continue;
            }

            row_char = row_header.charAt(0);

            while (tokens.hasMoreElements()) {
               field = tokens.nextToken();
               val = Double.valueOf(field).doubleValue();
               column_char = column_headers[column];
               substitutionScoreMatrix.put(row_char, column_char, val);
               column++;
            }
         }
      }
      finally {
         dis.close();
      }
      return substitutionScoreMatrix;
   }

   public static char getProteinForBlosumPosition(int proteinPosition) {
      try {
         return blosumPositionToProtein[proteinPosition];
      }
      catch (Exception ex) {
         System.out.println("Error asking for Blosum protein for position " + proteinPosition);
         return '?';
      }
   }

   public static int getBlosumPositionForProtein(char proteinCharacter) {
      try {
         return proteinToBlosumPosition[proteinCharacter];
      }
      catch (Exception ex) {
         System.out.println("Error asking for Blosum position for protein " + proteinCharacter);
         return 26;
      }
   }

}