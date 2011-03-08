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
import vizard.Glyph;
import vizard.GraphicContext;
import vizard.PickedList;

import java.awt.*;
import java.awt.geom.Rectangle2D;


/**
 * The purpose of the FastRectGlyph is to provide a rectangle glyph
 * that is very fast to render.
 *
 * FastRectGlyph does not use the rectangle shape.
 */
public abstract class FastRectGlyph extends Glyph
{
    /**
     * Return the rectangle x coordinate.
     */
    public abstract double x();

    /**
     * Return the rectangle y coordinate.
     */
    public abstract double y();

    /**
     * Return the rectangle width.
     */
    public abstract double width();

    /**
     * Return the rectangle height.
     */
    public abstract double height();

    /**
     * Return the rectangle background color.
     *
     * If it is null, the rectangle is not filled.
     */
    public abstract Color backgroundColor();

    /**
     * Return the rectangle outline color.
     *
     * If it is null, the rectangle is not outlined.
     */
    public Color outlineColor() { return null; }

    /**
     * Paint the rectangle.
     */
    public void paint(GraphicContext gc) {
	paint(gc, x(), y(), width(), height(),
	      backgroundColor(), outlineColor());
    }

    /**
     * Paint a rectangle with the given parameters.
     */
    public static void paint(GraphicContext gc,
			     double x, double y, double w, double h,
			     Color background, Color outline)
    {
	if (background != null) {
	    gc.setColor(background);
	    gc.fillRect(x, y, w, h);
	}
	if (outline != null) {
	    gc.setZeroLineWidth();
	    gc.setColor(outline);
	    gc.drawRect(x, y, w, h);
	}
    }

    /**
     * If this rectangle has a non-empty intersection with the
     * given deviceRect, the PickThrowable is thrown
     */
    public PickedList pick(GraphicContext gc, Rectangle deviceRect) {
	Rectangle2D.Double shape = gc.tempRectangle();
	shape.x = x();
	shape.y = y();
	shape.width = width();
        if (shape.width == 0)
            shape.width = gc.pixelWidth();
	shape.height = height();
        if (shape.height == 0)
            shape.height = gc.pixelHeight();

	if (backgroundColor() != null && gc.hit(deviceRect, shape, false) ||
	    outlineColor() != null && gc.hit(deviceRect, shape, true))
	    return new PickedList(this, gc);

	return null;
    }

    /**
     * Add the bounds of this rectangle to the given rectangular bounds.
     */
    public void addBounds(Bounds bounds) {
	bounds.add(x(), y(), width(), height());
    }

    /**
     * The purpose of the FastRectGlyph.Concrete class is to provide
     * a ready-to-use glyph to the application programmer.
     */
    public static class Concrete extends FastRectGlyph
    {
	private double x, y, w, h;
	private Color background, outline;

	public Concrete(double x, double y, double w, double h,
			Color background, Color outline)
	{
	    setRect(x, y, w, h);
	    this.background = background;
	    this.outline = outline;
	}

	public double x() { return x; }
	public double y() { return y; }
	public double width() { return w; }
	public double height() { return h; }
	public Color backgroundColor() { return background; }
	public Color outlineColor() { return outline; }

	public void setRect(double x, double y, double w, double h) {
	    repaint();
	    this.x = x;
	    this.y = y;
	    this.w = w;
	    this.h = h;
	    repaint();
	}

	public void setBackgroundColor(Color c) {
	    background = c;
	    repaint();
	}

	public void setOutlineColor(Color c) {
	    outline = c;
	    repaint();
	}
    }
}
