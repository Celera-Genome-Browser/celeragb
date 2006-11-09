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
package vizard.glyph;

import vizard.Bounds;
import vizard.GraphicContext;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;


/**
 * The purpose of the ClipperGlyph is to clip the paintings of its subtree
 * of glyph to an arbitrary shape.
 */
public abstract class ClipperGlyph extends BoundsGlyph
{
    /**
     * The clipping shape.
     */
    public abstract Shape shape();

    /**
     * Set the clipping shape, forward paint to its children,
     * and restores the previous clipping shape.
     */
    public void paint(GraphicContext gc) {
	if (intersectsDirtyArea(gc)) {
	    Shape save = gc.getClip();
	    gc.clip(shape());
	    paintChildren(gc);
	    gc.setClip(save);
	}
    }

    public void addBounds(Bounds bounds) {
	Bounds b = bounds.isEmpty() ? bounds : new Bounds();
	super.addBounds(b);
	double x = x(), y = y();
	double xmin = Math.max(x, b.x);
	double ymin = Math.max(y, b.y);
	double xmax = Math.min(x + width(), b.getMaxX());
	double ymax = Math.min(y + height(), b.getMaxY());
	b.setRect(xmin, ymin, xmax-xmin, ymax-ymin);
	if (b != bounds)
	    bounds.add(b);
    }

    /**
     * The purpose of the ClipperGlyph.Concrete class is to provide
     * a ready-to-use glyph to the application programmer.
     */
    public static class Concrete extends ClipperGlyph
    {
	private Shape shape;
	private double x, y, w, h;

	public Concrete(Shape shape) {
	    setShape(shape);
	}

	public Shape shape() { return shape; }
	public double x() { return x; }
	public double y() { return y; }
	public double width() { return w; }
	public double height() { return h; }

	public void setShape(Shape shape) {
            repaint();
	    this.shape = shape;
	    Rectangle2D b = shape.getBounds2D();
	    x = b.getX();
	    y = b.getY();
	    w = b.getWidth();
	    h = b.getHeight();
            repaint();
	    boundsChanged();
	}
    }
}
