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
import vizard.genomics.model.SNPAdapter;
import vizard.glyph.FastRectGlyph;

import java.awt.Color;


/**
 * The purpose of SNPPainter is to provide a standard rendering
 * for a genomic SNP.
 *
 * A GenomicGlyph that represents a SNP must create a new SNPPainter
 * child.
 *
 * If an application prefers a different rendering for a SNP,
 * it must add as a child the painter glyph of its choice.
 */
public class SNPPainter extends FastRectGlyph
{
    //@todo preferences
    public static int PIXELS = 3;

    protected SNPAdapter snp;

    public SNPPainter(SNPAdapter snp) {
	this.snp = snp;
    }

    /**
     * Glyph specialization.
     */
    public double x() {
	return snp.start();
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
	return snp.end() - snp.start();
    }

    /**
     * Glyph specialization.
     */
    public double height() {
	return (int)snp.height();
    }

    /**
     * Glyph specialization.
     */
    public Color backgroundColor() {
	return snp.color();
    }

    /**
     * Glyph specialization.
     */
    public void paint(GraphicContext gc) {
	double pixh = gc.pixelHeight();
        paint(gc, x(), y()+pixh*6, width(), height()-6*pixh,
	      backgroundColor(), outlineColor());

	gc.setColor(snp.headColor());
	//@todo the drawing depends on the SNP type
	double pixw = gc.pixelWidth();

        double m = (snp.start()+snp.end())/2.0;

        if (snp.kind() == snp.SUBSTITUTION) {
            gc.drawLine(m-pixw,   -pixh,  m+pixw,   -pixh);
            gc.drawLine(m-2*pixw, 0,      m+2*pixw, 0);
            gc.drawLine(m-3*pixw, pixh,   m+3*pixw, pixh);
            gc.drawLine(m-4*pixw, 2*pixh, m+4*pixw, 2*pixh);
            gc.drawLine(m-3*pixw, 3*pixh, m+3*pixw, 3*pixh);
            gc.drawLine(m-2*pixw, 4*pixh, m+2*pixw, 4*pixh);
            gc.drawLine(m-pixw,   5*pixh, m+pixw,   5*pixh);
        }
        else if (snp.kind() == snp.DELETION) {
            gc.drawLine(m-5*pixw, 3*pixh, m+5*pixw, 3*pixh);
            gc.drawLine(m-4*pixw, 2*pixh, m+4*pixw, 2*pixh);
            gc.drawLine(m-3*pixw, pixh,   m+3*pixw, pixh);
            gc.drawLine(m-2*pixw, 0,      m+2*pixw, 0);
            gc.drawLine(m-pixw,   -pixh,  m+pixw,   -pixh);
        }
        else { //snp.kind == INSERTION
            gc.drawLine(m-5*pixw, -pixh,  m+5*pixw, -pixh);
            gc.drawLine(m-4*pixw, 0,      m+4*pixw, 0);
            gc.drawLine(m-3*pixw, pixh,   m+3*pixw, pixh);
            gc.drawLine(m-2*pixw, 2*pixh, m+2*pixw, 2*pixh);
            gc.drawLine(m-pixw,   3*pixh, m+pixw,   3*pixh);
        }
    }

    public void addBounds(Bounds b) {
        double w = width();
        if (w == 0) w = 0.01;
	b.add(x(), y(), width(), height());
	b.leftPixels = Math.max(b.leftPixels, PIXELS);
	b.rightPixels = Math.max(b.rightPixels, PIXELS);
	b.upPixels = Math.max(b.upPixels, 1);
    }


}
