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

import vizard.GraphicContext;
import vizard.PickedList;
import vizard.genomics.model.ContigAdapter;
import vizard.glyph.ShapeGlyph;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;


/**
 * The purpose of ContigPainter is to provide a standard internal rendering for
 * a contig.
 *
 * A GenomicGlyph that represents a contig must create a new ContigPainter child.
 *
 * If an application prefers a different rendering for a contig,
 * it must add as a child the painter glyph of its choice.
 */
public class ContigPainter extends ShapeGlyph
{
    protected ContigAdapter contig;

    public ContigPainter(ContigAdapter contig) {
	this.contig = contig;
    }

    /**
     * Glyph specialization.
     */
    public Shape shape(GraphicContext gc) {
        final float ARROW_IN_PIXELS = 8.5f;
        float pixh = (float)gc.pixelHeight();
	float arrowHeight = (float)(ARROW_IN_PIXELS * pixh);
	float arrowWidth = (float)(ARROW_IN_PIXELS * gc.pixelWidth());
	float rectHeight = arrowHeight / 2 - pixh;
	float yCenter = arrowHeight / 2;

        //The following setting of xmin and xmax works in conjonction with the translation
        //done in the paint() and pick() methods.
        //The reason for it is that GeneralPath stores locations as float, and with big genomic axes
        //float precision is not enough.
        //So we first make a translation (in doubles), and then do the conting drawing in floats around 0.
        boolean isForward = contig.isForward();
        float xmin, xmax;
        if (isForward) {
	    xmin = contig.start() - contig.end();
	    xmax = 0;
        }
        else {
            xmin = 0;
            xmax = contig.end() - contig.start();
        }

	GeneralPath path = new GeneralPath();
        if (isForward) {
            path.moveTo(xmin, yCenter - rectHeight/2 + pixh);
            path.lineTo(xmax - arrowWidth, yCenter - rectHeight/2 + pixh);
            path.lineTo(xmax - arrowWidth, 0);
            path.lineTo(xmax, yCenter);
            path.lineTo(xmax - arrowWidth, arrowHeight);
            path.lineTo(xmax - arrowWidth, yCenter + rectHeight/2 + 1);
            path.lineTo(xmin, yCenter + rectHeight/2 + 1);
        }
        else { //reverse
            path.moveTo(xmax, yCenter - rectHeight/2 + pixh);
            path.lineTo(xmin + arrowWidth, yCenter - rectHeight/2 + pixh);
            path.lineTo(xmin + arrowWidth, 0);
            path.lineTo(xmin, yCenter);
            path.lineTo(xmin + arrowWidth, arrowHeight);
            path.lineTo(xmin + arrowWidth, yCenter + rectHeight/2 + 1);
            path.lineTo(xmax, yCenter + rectHeight/2 + 1);
        }
	path.closePath();

	return path;
    }

    public Shape shape() {
	return new Rectangle2D.Double(contig.start(), 0,
				      contig.end() - contig.start(),
				      contig.height());
    }

    public void paint(GraphicContext gc) {
	gc.setColor(contig.color());
        double tx = contig.isForward() ? contig.end() : contig.start();
        gc.translate(tx, 0);
	super.paint(gc);
        gc.translate(-tx, 0);
    }

    public PickedList pick(GraphicContext gc, Rectangle deviceRect) {
        double tx = contig.isForward() ? contig.end() : contig.start();
        gc.translate(tx, 0);
	PickedList pickedList = super.pick(gc, deviceRect);
        gc.translate(-tx, 0);
        return pickedList;
    }
}
