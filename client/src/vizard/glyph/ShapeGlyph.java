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

import vizard.*;
import java.awt.*;
import java.awt.geom.*;


/**
 * A glyph that paints an arbitrary shape.
 */
public abstract class ShapeGlyph extends Glyph
{
    /**
     * Return the shape.
     */
    public abstract Shape shape();

    public boolean hasBackground() { return true; }
    public Color outlineColor() { return null; }

    public Shape shape(GraphicContext gc) {
	return shape();
    }

    /**
     * A static method to paint the shape with all the given parameters.
     */
    public static void paint(GraphicContext gc, Shape shape,
			     boolean hasBackground, Color outlineColor)
    {
	if (hasBackground) {
	    gc.fill(shape);
	}

	if (outlineColor != null) {
	    gc.setColor(outlineColor);
	    gc.draw(shape);
	}
    }

    /**
     * Paint the shape.
     */
    public void paint(GraphicContext gc) {
	paint(gc, shape(gc), hasBackground(), outlineColor());
    }

    /**
     * If this shape has a non-empty intersection with the given
     * deviceRect, the PickThrowable is thrown
     */
    public PickedList pick(GraphicContext gc, Rectangle deviceRect) {
	Shape s = shape(gc);
	if (hasBackground() && gc.hit(deviceRect, s, false) ||
	    outlineColor() != null && gc.hit(deviceRect, s, true))
	{
	    return new PickedList(this, gc);
	}

	return null;
    }

    /**
     * Add the bounds of this shape to the given rectangular bounds.
     */
    public void addBounds(Bounds bounds) {
	Rectangle2D r = shape().getBounds2D();
	bounds.add(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * The purpose of the ShapeGlyph.Concrete class is to provide
     * a ready-to-use glyph to the application programmer.
     */
    public static class Concrete extends ShapeGlyph
    {
	private Shape shape;
	private Color background, outline;

	public Concrete(Shape shape, Color background, Color outline)
	{
	    this.shape = shape;
	    this.background = background;
	    this.outline = outline;
	}

	public Shape shape() { return shape; }
	public Color outlineColor() { return outline; }

	public void setShape(Shape shape) {
            repaint();
	    this.shape = shape;
            repaint();
	    boundsChanged();
	}

	public void setBackgroundColor(Color c) {
	    background = c;
	    repaint();
	}

	public void setOutlineColor(Color c) {
	    outline = c;
	    repaint();
	}

	public void paint(GraphicContext gc) {
	    gc.setColor(background);
	    super.paint(gc);
	}
    }
}
