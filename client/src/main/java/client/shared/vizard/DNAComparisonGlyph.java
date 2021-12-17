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

import api.stub.sequence.DNA;
import api.stub.sequence.Sequence;
import vizard.genomics.glyph.SequencePainter;
import vizard.genomics.model.FeatureAdapter;

import java.awt.*;

/**
 * Title:
 * Description:
 * Company:
 * @author
 * @version 1.0
 */

public class DNAComparisonGlyph extends SubjectAndQueryComparisonGlyph {

  public DNAComparisonGlyph(FeatureAdapter fa, String subjectString,
      String queryString, AlignSequenceAdapter subjectAdapter, AlignSequenceAdapter queryAdapter,
      int queryFrame, Sequence baseSequence, int nucleotidePerSequenceScale,
      boolean isGlyphInQuerySpace) {

    super(fa, subjectString, queryString, subjectAdapter,
      queryAdapter, queryFrame, baseSequence, nucleotidePerSequenceScale, isGlyphInQuerySpace);
  }


  protected void setSequencePainters() {
    colorSequence = new Color[subjectString.length()];
    for (int i = 0; i < subjectString.length(); i++) {
      int x = DNA.charToBase(subjectString.charAt(i));
      int y = DNA.charToBase(queryString.charAt(i));
      // The line below checks to see if anything other than A, C, T, G is returned.
      // If true, make the color blue.
      if (x < DNA.A || y < DNA.A || x > DNA.T || y > DNA.T) colorSequence[i] = Color.blue;
      if (x==y) colorSequence[i] = Color.cyan;
      else colorSequence[i] = Color.blue;
    }

    // Define the painters for the sequence letters themselves.
    subjectPainter  = new SequencePainter(subjectAdapter);
    queryPainter    = new SequencePainter(queryAdapter);
  }
}