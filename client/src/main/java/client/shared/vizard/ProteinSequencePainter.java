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

import vizard.genomics.glyph.SequencePainter;
import vizard.genomics.model.SequenceAdapter;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;

  /**
   * This class is designed to show Protein sequence in nucleotide space; therefore,
   * i protein base = 3 units on the axis.
   */
 public class ProteinSequencePainter extends SequencePainter {

    private int nucleotidePerSequenceScale = 3;
    public ProteinSequencePainter(SequenceAdapter sequence, int nuceotidePerSequenceScale) {
      super(sequence);
      this.nucleotidePerSequenceScale = nuceotidePerSequenceScale;
    }

    /**
     * Most of this code is from the base class.  I only need to space out the
     * drawing of protein sequence.
     */
    public void handleNow(String string, int start) {
        Rectangle2D dirtyBounds = gc.dirtyBounds();
        int myStart = (int)dirtyBounds.getX();
        int myEnd = (int)dirtyBounds.getMaxX();
        int firstIndex = Math.max(0, (myStart-start)/nucleotidePerSequenceScale);
        int lastIndex = Math.min(string.length(), (myEnd-start)/nucleotidePerSequenceScale+1);
        if (lastIndex<0 || firstIndex<0 || firstIndex>=string.length()
            || string.length()<=0) return;
        string = string.substring(firstIndex, lastIndex);
        start += nucleotidePerSequenceScale*firstIndex;

	double[] p = gc.tempDoubles();
	int spacesToFill = nucleotidePerSequenceScale-1;
        int midpoint = spacesToFill/2;
        char[] chars = string.toCharArray();
        char[] prefixSpaces = new char[midpoint];
        char[] suffixSpaces = new char[spacesToFill-midpoint];
        Arrays.fill(prefixSpaces, 0, midpoint, ' ');
        Arrays.fill(suffixSpaces, 0, spacesToFill-midpoint, ' ');
        // Set up the frame of the protein.
        StringBuffer relativeString = new StringBuffer();
        for (int x=0; x < chars.length; x++) {
          //  Two spaces in order to space it relative to neucleotide bases.
          relativeString.append(prefixSpaces).append(chars[x]).append(suffixSpaces);
        }
        chars = relativeString.toString().toCharArray();

        if (!sequence.isForward()) {
            for(int i = 0; i < chars.length; i++) {
                chars[i] = reverseChar(chars[i]);
            }
        }

        double charWidth = gc.getStringWidth("A");
        double centeredStart = start + (1 - charWidth)/2 ; //@todo + proteinFrame;

        double y = sequence.height() * 1;
  	gc.setColor(sequence.color());
	for(int i = 0; i < chars.length; ++i) {
            p[0] = centeredStart + i;
	    p[1] = y;
	    gc.drawChars(chars, i, 1, p[0], p[1]);
	}
    }

}