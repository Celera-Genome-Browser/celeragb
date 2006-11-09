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
 * The purpose of the FastLineGlyph is to provide a line glyph
 * that is very fast to render.
 */
public abstract class FastLineGlyph extends Glyph
{
    /**
     * Return the x coordinate of the first point.
     */
    public abstract double x1();

    /**
     * Return the y coordinate of the first point.
     */
    public abstract double y1();

    /**
     * Return the x coordinate of the second point.
     */
    public abstract double x2();

    /**
     * Return the y coordinate of the second point.
     */
    public abstract double y2();

    /**
     * Return the line color.
     */
    public abstract Color color();

    /**
     * Paint a line with the given parameters.
     */
    public static void paint(GraphicContext gc,
			     double x1, double y1, double x2, double y2,
			     Color color)
    {
        gc.setColor(color);
        gc.drawLine(x1, y1, x2, y2);
    }

    /**
     * Paint the line.
     */
    public void paint(GraphicContext gc) {
	paint(gc, x1(), y1(), x2(), y2(), color());
    }

    /**
     * This line glyph is picked if it has a non-empty intersection
     * with the given deviceRect.
     */
    public PickedList pick(GraphicContext gc, Rectangle deviceRect) {
	Line2D.Double shape = gc.tempLine(x1(), y1(), x2(), y2());

	if (gc.hit(deviceRect, shape, true))
	    return new PickedList(this, gc);

	return null;
    }

    /**
     * Add the bounds of this rectangle to the given rectangular bounds.
     */
    public void addBounds(Bounds bounds) {
	double x1 = x1(), y1 = y1();
	bounds.add(x1, y1, x2()-x1, y2()-y1);
    }

    /**
     * The purpose of the FastLineGlyph.Concrete class is to provide
     * a ready-to-use glyph to the application programmer.
     */
    public static class Concrete extends FastLineGlyph
    {
	private double x1, y1, x2, y2;
	private Color color;

	public Concrete(double x, double y, double w, double h,
			Color color)
	{
	    setLine(x1, y1, x2, y2);
	    this.color = color;
	}

	public double x1() { return x1; }
	public double y1() { return y1; }
	public double x2() { return x2; }
	public double y2() { return y2; }
	public Color color() { return color; }

	public void setLine(double x, double y, double w, double h) {
	    repaint();
	    //this.x1 = x1;
	    //this.y1 = y1;
	    //this.x2 = x2;
	    //this.y2 = y2;
	    repaint();
	}

	public void setColor(Color c) {
	    color = c;
	    repaint();
	}
    }
}
