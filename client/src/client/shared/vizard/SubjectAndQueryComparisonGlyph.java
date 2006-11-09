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

import vizard.glyph.*;
import vizard.genomics.glyph.*;
import vizard.genomics.model.*;
import api.stub.sequence.*;
import java.util.Arrays;
import java.awt.Color;
import java.util.ArrayList;
import vizard.GraphicContext;
import shared.util.WhiteSpaceUtils;

/**
 * Title:
 * Description:
 * Company:
 * @author
 * @version 1.0
 */

public abstract class SubjectAndQueryComparisonGlyph extends GenomicGlyph {
  protected Color[] colorSequence;
  private FeatureAdapter featureAdapter;
  private VerticalPacker combinationGlyph = new VerticalPacker();
  protected String subjectString;
  protected String queryString;
  protected SequenceAdapter subjectAdapter;
  protected SequenceAdapter queryAdapter;
  protected SequencePainter queryPainter;
  protected SequencePainter subjectPainter;
  protected int queryFrame;
  protected Sequence baseSequence;
  protected int nucleotidePerSequenceScale = 1;
  // This boolean controls which spaces/gaps are removed from the sequences.
  protected boolean isGlyphInQuerySpace = true;


  public SubjectAndQueryComparisonGlyph(FeatureAdapter featureAdapter, String subjectString,
      String queryString, SequenceAdapter subjectAdapter, SequenceAdapter queryAdapter,
      int queryFrame, Sequence baseSequence, int nucleotidePerSequenceScale,
      boolean isGlyphInQuerySpace) {

    this.featureAdapter             = featureAdapter;
    this.subjectString              = subjectString.toUpperCase();
    this.queryString                = queryString.toUpperCase();
    this.subjectAdapter             = subjectAdapter;
    this.queryAdapter               = queryAdapter;
    this.queryFrame                 = queryFrame;
    this.baseSequence               = baseSequence;
    this.nucleotidePerSequenceScale = nucleotidePerSequenceScale;
    this.isGlyphInQuerySpace        = isGlyphInQuerySpace;

    checkResidueStrings();
    formatResidues();
    setSequencePainters();
    combinationGlyph.packChild(subjectPainter);
    combinationGlyph.packChild(queryPainter);
    this.addChild(combinationGlyph);
  }


  protected abstract void setSequencePainters();

  protected void formatResidues() {
    boolean debugMethod = false;
    // Initial sanity check on what is being worked on.
    if (debugMethod) {
      System.out.println("Query   String: "+queryString);
      System.out.println("Subject String: "+subjectString);
      System.out.println("Query   Length: "+queryString.length());
      System.out.println("Subject Length: "+subjectString.length());
      System.out.println("Feature Length: "+((end()-start())/nucleotidePerSequenceScale)+"\n");
      System.out.println("In Query Space: "+isGlyphInQuerySpace+"\n");
    }

    SubjectAndQueryComparisonHelper comparisonHelper = SubjectAndQueryComparisonHelper.getHelper();
    // Remove all offending whitespaces.
    if (subjectString != null) {
        subjectString = WhiteSpaceUtils.stripNonResidueStartEndChar(subjectString);
    }
    if (queryString != null) {
        queryString = WhiteSpaceUtils.stripNonResidueStartEndChar(queryString);
    }

    String targetString;
    if (isGlyphInQuerySpace) targetString = queryString;
    else targetString = subjectString;

    // Find gap locations of characters ' ' and '-'
    if ((queryString != null) && (subjectString != null)) {
        ArrayList gap_locations = new ArrayList();
        if (targetString.indexOf('-') >= 0) {
            gap_locations.addAll(comparisonHelper.getCharLocations('-', targetString));
        }
        if (targetString.indexOf(' ') >= 0) {
            gap_locations.addAll(comparisonHelper.getCharLocations(' ', targetString));
        }

        if (gap_locations != null) {
          ArrayList delete_locations = new ArrayList(gap_locations.size());
          // Remove the gaps from the sequences.
          subjectString = comparisonHelper.removeLocations(gap_locations, subjectString, delete_locations);
          queryString   = comparisonHelper.removeLocations(gap_locations, queryString, delete_locations);

          // Place the deletion markers.
          // If query is reversed, the deletion marker locations should reflect the reversal:
          if (queryFrame < 0) {
            int gapLocLength = gap_locations.size();
            ArrayList delLocRev = new ArrayList(gapLocLength);
            int residueLength = targetString.length();
            // lastLocation because if gaps are sequential (pos. 3,4,5...) then
            // the returned locations are (pos. 3,3,3...)
            int lastLocation = -99;  // Initialize to some arbitrary negative number
            for (int i = 0; i < gapLocLength; i++) {
                if (((Integer)delete_locations.get(i)).intValue() != lastLocation) {
                    lastLocation = ((Integer)delete_locations.get(i)).intValue();
                }
                delLocRev.add(new Integer(residueLength - lastLocation));
            }
            comparisonHelper.addDeletionMarkers(this, delLocRev);
          }
          else {
            comparisonHelper.addDeletionMarkers(this, delete_locations);
          }
      }
    }

    // Format residues for proper orientation.
    if (subjectString != null) {
        if (queryFrame < 0) {
            StringBuffer revbuf = new StringBuffer(subjectString);
            revbuf = revbuf.reverse();
            subjectString = revbuf.toString();
        }
//        if (!Character.isLetterOrDigit(subjectString.charAt(0))) {
//        	System.out.println("Removing char(0) subject non-letter or non-digit");
//        	subjectString = subjectString.substring(1);
//        } 
    }
    if (queryString != null) {
        if (queryFrame < 0) {
            StringBuffer revbuf = new StringBuffer(queryString);
            revbuf = revbuf.reverse();
            queryString = revbuf.toString();
        }
//        if (!Character.isLetterOrDigit(queryString.charAt(0))) {
//			System.out.println("Removing char(0) query non-letter or non-digit");
//			queryString = queryString.substring(1);
//        } 
    }

    // Finally update the adapters to show the new sequence. Always have to keep this class'
    // sequence and the adapter sequence in sync.  They should be using the same strings.
    if (subjectString.length()>1) ((AlignSequenceAdapter)subjectAdapter).setSequence(subjectString);
    if (queryString.length()>1)   ((AlignSequenceAdapter)queryAdapter).setSequence(queryString);
    setSequencePainters();
  }


  public void paint(GraphicContext gc) {
    long start = start();
    for (int i = 0; i < subjectString.length(); i++) {
      FastRectGlyph.paint( gc, ( i * nucleotidePerSequenceScale ) + start, y(), nucleotidePerSequenceScale, height(), colorSequence[ i ], null );
    }
    super.paint( gc );
  }


  private void checkResidueStrings() {
    if (queryString == null) {
      char[] res = new char[end()-start()];
      Arrays.fill(res, '?');
      queryString = new String(res);
    }
    if (subjectString == null) {
      char[] res = new char[end()-start()];
      Arrays.fill(res, '?');
      subjectString = new String(res);
    }
 }


  public int getNucleotidePerSequenceScale() { return nucleotidePerSequenceScale; }

  public double height() {
    return featureAdapter.height();
  }

  public int end() {
    return featureAdapter.end();
  }

  public int start() {
    return featureAdapter.start();
  }

  public void setNewFrame(int newFrame) {
    if (queryFrame != newFrame) queryFrame = newFrame;
    setSequencePainters();
  }
}