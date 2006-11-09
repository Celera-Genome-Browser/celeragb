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

import api.stub.sequence.Sequence;
import shared.genetics.BlocksSubstitutionMatrix;
import vizard.genomics.model.FeatureAdapter;
import vizard.genomics.model.SequenceAdapter;

import java.awt.Color;

/**
 * Title:
 * Description:
 * Company:
 * @author
 * @version 1.0
 */

public class ProteinComparisonGlyph extends SubjectAndQueryComparisonGlyph {

  public ProteinComparisonGlyph(FeatureAdapter fa, String subjectString,
      String queryString, SequenceAdapter subjectAdapter, SequenceAdapter queryAdapter,
      int queryFrame, Sequence baseSequence, int nucleotidePerSequenceScale,
      boolean isGlyphInQuerySpace) {

    super(fa, subjectString, queryString, subjectAdapter,
      queryAdapter, queryFrame, baseSequence, nucleotidePerSequenceScale, isGlyphInQuerySpace);
  }


  protected void setSequencePainters(){
    // Get the color matrix and compute the color per base.
    SubjectAndQueryComparisonHelper comparisonHelper = SubjectAndQueryComparisonHelper.getHelper();
    Color[][] comparisonArray = comparisonHelper.getProteinColorMatrix();
    colorSequence = new Color[subjectString.length()];
    int loop = Math.min(subjectString.length(), queryString.length());
    // Debug block. Should probably be left in.
//    if (subjectString.length() != queryString.length()) {
//      System.out.println("Protein Glyph Error! Subject residue length does not equal query length.");
//      System.out.println("Subject Error: "+subjectString);
//      System.out.println("Query Error: "+queryString);
//    }
    for (int i = 0; i < loop; i++) {
      int x = BlocksSubstitutionMatrix.getBlosumPositionForProtein(subjectString.charAt(i));
      int y = BlocksSubstitutionMatrix.getBlosumPositionForProtein(queryString.charAt(i));
      // The line below checks to see if anything other than known Proteins is returned.
      // If true, make the color dark Gray.
      try {
        if (x == Sequence.UNKNOWN || y == Sequence.UNKNOWN ||
            x >= comparisonArray.length || y >= comparisonArray.length) colorSequence[i] = Color.darkGray;
        else colorSequence[i] = comparisonArray[x][y];
      }
      catch (Exception ex) {
        System.out.println("X("+subjectString.charAt(i)+")= "+x+"     Y("+queryString.charAt(i)+")= "+y+
                           "\nSubject length= "+subjectString.length()+"   i= "+i+"\n");
        colorSequence[i] = Color.darkGray;
      }
    }

    // Define the painters for the sequence letters themselves.
    subjectPainter  = new ProteinSequencePainter(new AlignSequenceAdapter(
      subjectString, Color.black, start(), true), nucleotidePerSequenceScale);

    queryPainter    = new ProteinSequencePainter(new AlignSequenceAdapter(
      queryString, Color.black, start(), true), nucleotidePerSequenceScale);
  }

   public int getFrame() { return queryFrame; }

}