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
import vizard.genomics.model.SpliceSiteAdapter;
import vizard.glyph.FastLineGlyph;
import vizard.glyph.FastRectGlyph;

import java.awt.Color;


/**
 * The purpose of SpliceSitePainter is to provide a standard rendering
 * for a genomic spliceSite.
 *
 * A GenomicGlyph that represents a spliceSite must create a new SpliceSitePainter
 * child.
 *
 * If an application prefers a different rendering for a splice site,
 * it must add as a child the painter glyph of its choice.
 */
public class SpliceSitePainter extends FastRectGlyph
{
    //@todo preferences
    public static int NUM_PIXELS = 5;

    protected SpliceSiteAdapter spliceSite;

    public SpliceSitePainter(SpliceSiteAdapter spliceSite) {
	this.spliceSite = spliceSite;
    }

    /**
     * Glyph specialization.
     */
    public double x() {
	return spliceSite.start();
    }

    /**
     * Glyph specialization.
     */
    public double y() {
	return 0;
    }

    /**
     * Glyph specialization.
     */
    public double width() {
	return spliceSite.end() - spliceSite.start();
    }

    /**
     * Glyph specialization.
     */
    public double height() {
	return spliceSite.height();
    }

    /**
     * Glyph specialization.
     */
    public Color backgroundColor() {
	return spliceSite.color();
    }

    /**
     * Glyph specialization.
     */
    public void paint(GraphicContext gc) {
	super.paint(gc);

	double height = spliceSite.height();
	double width = NUM_PIXELS * gc.pixelWidth();
	if (!spliceSite.isAcceptor()) {
	    double start = spliceSite.end();
	    FastLineGlyph.paint(gc, start, height, start + width, height,
				spliceSite.color());
	}
	else {
	    double end = spliceSite.start();
	    FastLineGlyph.paint(gc, end - width, height, end, height,
				spliceSite.color());
	}
    }

    public void addBounds(Bounds b) {
	b.add(x(), y(), width(), height());
        b.addLeftRightPixels(NUM_PIXELS, NUM_PIXELS);
    }
}
