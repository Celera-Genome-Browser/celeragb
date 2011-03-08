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
package vizard.genomics.glyph;

import vizard.Bounds;
import vizard.GraphicContext;
import vizard.PickedList;
import vizard.genomics.model.SequenceAdapter;
import vizard.glyph.Constants;
import vizard.glyph.GlyphAdapter;

import java.awt.*;
import java.awt.geom.Rectangle2D;


/**
 * The purpose of SequencePainter is to provide a standard rendering for
 * a genomic sequence.
 *
 * A GenomicGlyph that represents a sequence must create a new SequencePainter
 * child.
 *
 * If an application prefers a different rendering for a sequence,
 * it must add as a child the painter glyph of its choice.
 */
public class SequencePainter extends GlyphAdapter
    implements SequenceAdapter.SequenceReadyHandler
{
    //@todo preferences
    public static final Font FONT = Constants.cleanFont;
    public static final int MIN_PIXEL_SIZE = 8;

    protected SequenceAdapter sequence;
    protected GraphicContext gc;
    protected String readySequence;
    protected int readyStart;

    public SequencePainter(SequenceAdapter sequence) {
	this.sequence = sequence;
    }

    /**
     * Glyph specialization.
     */
    public void paint(GraphicContext gc) {
	if (1 / gc.pixelWidth() < MIN_PIXEL_SIZE)
	    return;

	this.gc = gc;
	sequence.getSequence((int)gc.dirtyBounds().getX(),
			     (int)Math.ceil(gc.dirtyBounds().getMaxX()),
			     this);
    }


    public PickedList pick(GraphicContext gc, Rectangle deviceRect) {
	Rectangle2D.Double shape = gc.tempRectangle();
	shape.x = sequence.start();
	shape.y = 0;
	shape.width = sequence.end()-sequence.start();
        if (shape.width == 0)
            shape.width = gc.pixelWidth();
	shape.height = sequence.height();
        if (shape.height == 0)
            shape.height = gc.pixelHeight();

	if (gc.hit(deviceRect, shape, false))
          return new PickedList(this, gc);

	return null;
    }


    /**
     * Glyph specialization
     */
    public void addBounds(Bounds bounds) {
	bounds.add(sequence.start(), 0,
		   sequence.end() - sequence.start(),
		   sequence.height());
    }

    /**
     * SequenceReadyHandler specialization.
     *
     * @todo this code should be in some "PixelSizedLabelGlyph" class.
     */
    public void handleNow(String string, int start) {
	gc.setColor(sequence.color());

        Rectangle2D dirtyBounds = gc.dirtyBounds();
        int myStart = (int)dirtyBounds.getX();
        int myEnd = (int)dirtyBounds.getMaxX();
        int firstIndex = Math.max(0, myStart-start);
        int lastIndex = Math.min(string.length(), myEnd-start+1);
        if (lastIndex<0 && firstIndex>=string.length()) return;
        string = string.substring(firstIndex, lastIndex);
        start += firstIndex;

	double[] p = gc.tempDoubles();
	char[] chars = string.toCharArray();
        if (!sequence.isForward()) {
            for(int i = 0; i < chars.length; i++) {
                chars[i] = reverseChar(chars[i]);
            }
        }

        double charWidth = gc.getStringWidth("A");
        double centeredStart = start + (1 - charWidth)/2;

        double y = sequence.height() * 1;
	for(int i = 0; i < chars.length; ++i) {
	    p[0] = centeredStart + i;
	    p[1] = y;

	    gc.drawChars(chars, i, 1, p[0], p[1]);
	}
    }

    protected char reverseChar(char c) {
        switch(c) {
        case 'A': return 'T';
        case 'a': return 't';
        case 'C': return 'G';
        case 'c': return 'g';
        case 'G': return 'C';
        case 'g': return 'c';
        case 'T': return 'A';
        case 't': return 'a';
        default:  return c;
        }
    }

    /**
     * SequenceReadyHandler specialization.
     */
    public void handleLater(String string, int start) {
	if (parent() != null) {
	    readySequence = string;
	    readyStart = start;
	    parent().inval(new Bounds(start, 0, string.length(), sequence.height()));
	}
    }
}
