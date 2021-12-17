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
package client.shared.vizard;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

import api.stub.sequence.Protein;
import shared.genetics.BlocksSubstitutionMatrix;
import shared.genetics.SubstitutionScoreMatrix;

import java.awt.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class SubjectAndQueryComparisonHelper {
  private static SubjectAndQueryComparisonHelper helper = new SubjectAndQueryComparisonHelper();
  private static Color[][] proteinColorMatrix;


  private SubjectAndQueryComparisonHelper() {
    proteinColorMatrix = makeProteinColorMatrix("resource/client/BLAST_matrices/BLOSUM62.50");
  }

  public static SubjectAndQueryComparisonHelper getHelper() { return helper; }

  public Color[][] getProteinColorMatrix() { return proteinColorMatrix; }

  /**
   * Returns the locations as an ArrayList of Integer locations.
   */
  public ArrayList getCharLocations(char ch, String str) {
      int numgaps = 0;
      if (str == null) return new ArrayList();
      int loc = str.indexOf(ch);
      while (loc >= 0) {
          numgaps++;
          loc = str.indexOf(ch, loc + 1);
      }
      ArrayList gaps = new ArrayList(numgaps);
      loc = str.indexOf(ch);
      while (loc >= 0) {
        gaps.add(new Integer(loc));
        loc = str.indexOf(ch, loc + 1);
      }
      return gaps;
  }


  public int getTrailingSpaceCount(String str) {
      char ch;
      int i;
      if (str == null) return 0;
      int count = 0;
      for (i = str.length() - 1; i >= 0; i--) {
          ch = str.charAt(i);
          if (ch != ' ') {
              break;
          }
          count++;
      }
      return count;
  }


  public int getLeadingSpaceCount(String str) {
      char ch;
      int i;
      if (str == null) return 0;
      for (i = 0; i < str.length(); i++) {
          ch = str.charAt(i);
          if (ch != ' ') {
              break;
          }
      }
      return i;
  }


  /**
   *  gap_locs is array of integers specifying location in residues string that are gaps
   * delete_locs is array of integers returned giving "space" locations where deletions
   * ocurred in returned align_residues
   */
  public String removeLocations(ArrayList gap_locs, String align_residues,
      ArrayList delete_locs) {
    StringBuffer buf = new StringBuffer();
    int last_loc = -1;
    int current_loc;
    char ch;
    for (int i = 0; i < gap_locs.size(); i++) {
        current_loc = ((Integer)gap_locs.get(i)).intValue();
        for (int j = last_loc + 1; j < current_loc; j++) {
            try {
              ch = align_residues.charAt(j);
              buf.append(ch);
            }
            catch (Exception ex) {
              System.out.println("Subject residue failure for entity in SubjectAndQueryComparisonGlyph.");
            }
        }
        last_loc = current_loc;
        delete_locs.add(new Integer(buf.length()));
    }
    for (int k = last_loc + 1; k < align_residues.length(); k++) {
        ch = align_residues.charAt(k);
        buf.append(ch);
    }
    return buf.toString();
  }


  public void addDeletionMarkers(SubjectAndQueryComparisonGlyph parentGlyph,
    ArrayList delete_locations) {
      for (int i = 0; i < delete_locations.size(); i++) {
        int tmpLoc = ((Integer)delete_locations.get(i)).intValue()*parentGlyph.getNucleotidePerSequenceScale();
        DeletionAdapter da;
        da = new DeletionAdapter(parentGlyph.start()+tmpLoc, parentGlyph.start()+tmpLoc, 26);
        DeletionGlyph marker = new DeletionGlyph(da);
        parentGlyph.addGenomicChild(marker);
      }
  }


  /**
   *  Inserts a dash (-) where a space indicates a purposeful re-alignment.
   *  If you normally have: "F  G  K  S  ", and could have: "F  G     K  S  ",
   *  then this method returns: "F  G  -  K  S  ". Note that I found at least
   *  one example of "F   G  K"..., i.e., an extra space.
   */
  public String insertDashForInsertedSpace(String str) {
    StringBuffer buf = new StringBuffer();
    char ch,
         lastChar = '\b';  // Initialize to some arbitrary value
    int spaceCount = 0;

    for (int i = 0; i < str.length(); i++) {
      ch = str.charAt(i);
      if (ch == ' ') {
        if (spaceCount == 0) {
          spaceCount++;
        }
        else if (lastChar == ' ') {
          spaceCount++;
        }
        if (spaceCount == 3) {
          // If third space in erroneously
          if (str.charAt(i+1) != ' ') {
            spaceCount = 0;
            lastChar = '\b';  // Initialize to some arbitrary value
            continue;
          }
          buf.append('-');
          spaceCount = 0;  // reset
        }
        else {
          buf.append(ch);
          lastChar = ch;
        }
      }  // end if (ch == ' ')
      else {
            buf.append(ch);
        spaceCount = 0;  // reset
      }
      lastChar = ch;
    }  // end for

    return buf.toString();
  }  // end private String insertDashForInsertedSpace(String str)


  private Color[][] makeProteinColorMatrix(String filelocation) {
      Color[][] idmatrix = new Color[0][0];
      SubstitutionScoreMatrix subst_matrix = new SubstitutionScoreMatrix();
      boolean notloaded = true;
      String prefix = "../../../../";
      InputStream instr;
      try {
          instr = this.getClass().getResourceAsStream("/" + filelocation);
          BlocksSubstitutionMatrix p = new BlocksSubstitutionMatrix();
          subst_matrix = (SubstitutionScoreMatrix)p.importContent(instr);
          notloaded = false;
      }
      catch(Exception ex) {
          System.err.println("Couldn't load " + filelocation + " will try as " + prefix + filelocation);
          System.err.println(ex.getMessage());
      }
      if (notloaded) {
          try {
              instr = new FileInputStream(prefix + filelocation);
              BlocksSubstitutionMatrix p = new BlocksSubstitutionMatrix();
              subst_matrix = (SubstitutionScoreMatrix)p.importContent(instr);
              subst_matrix.toString();
          } catch(Exception ex) {
              System.err.println(filelocation +
                  " could not be loaded.  Please put it in the 'resource\\BLAST_matrices' directory.  An identity matrix is now being used instead.");
              System.err.println(ex.getMessage());
              return makeIdentityMatrix();
          }
      }
      idmatrix = getColorMatrix(subst_matrix);
      return idmatrix;
  }

  private Color[][] getColorMatrix(SubstitutionScoreMatrix score_matrix) {
    double score_thresholds[] = { 4, 0, -4 };
    // trying monochrome green scheme
    Color colors[] = {  new Color(0, 255, 0),
                        new Color(0, 155, 0),
                        new Color(0,  50, 0),
                        Color.yellow };
    int matrix_size = Protein.NUM_PROTEINS;
    Color color_matrix[][] = new Color[matrix_size][matrix_size];
    double score;
    for (int i = 0; i < matrix_size; i++) {
        char a = BlocksSubstitutionMatrix.getProteinForBlosumPosition(i);
        for (int j = 0; j < matrix_size; j++) {
            char b = BlocksSubstitutionMatrix.getProteinForBlosumPosition(j);
            // intervening to deal with gap characters ('-'), since
            // not in substitution matrices, and ProteinUtils reports a wrong score (0)
            // when trying to get substitution score for a pair that isn't there
            if (a == '-' || b == '-') {
                color_matrix[i][j] = Color.darkGray;
            }
            else {
                score = score_matrix.get(a, b);
                for (int k = 0; k < score_thresholds.length; k++) {
                    if (score >= score_thresholds[k]) {
                        //System.out.println("Score "+score+" set for i,j "+a+","+b);
                        color_matrix[i][j] = colors[k];
                        break;
                    }
                    color_matrix[i][j] = colors[score_thresholds.length];
                }
            }
        }
    }
    return color_matrix;
  }


  private Color[][] makeIdentityMatrix() {
      Color[][] idmatrix = new Color[24][24];
      for (int i = 0; i < idmatrix.length; i++) {
          for (int j = 0; j < idmatrix.length; j++) {
              if (i == j) {
                  idmatrix[i][j] = Color.magenta;
              }
              else {
                  idmatrix[i][j] = Color.green;
              }
          }
      }
      return idmatrix;
  }
}