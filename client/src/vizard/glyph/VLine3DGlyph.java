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

import java.awt.Color;
import java.awt.geom.Line2D;


/**
 * A vertical line with a 3D effect.
 */
public abstract class VLine3DGlyph extends Effect3DGlyph
{
    /**
     * Return the line x coordinate.
     */
    public abstract double x();

    /**
     * Return the line first y coordinate.
     */
    public abstract double ymin();

    /**
     * Return the line second y coordinate.
     */
    public abstract double ymax();

    /**
     * Paint a vertical line with a 3D effect with the given parameters.
     */
    public static void paint(GraphicContext gc,
			     double x, double ymin, double ymax,
			     boolean isRaised,
			     Color bright, Color shadow)
    {
	gc.setZeroLineWidth();
	double pixw = gc.pixelWidth();

	Line2D.Double line = gc.tempLine();
	line.x1 = line.x2 = x - pixw;
	line.y1 = ymin;
	line.y2 = ymax;

	gc.setColor(isRaised ? bright : shadow);
	gc.draw(line);

	gc.setColor(isRaised ? shadow : bright);
	line.x1 += pixw;
	line.x2 += pixw;
	gc.draw(line);
    }

    /**
     * Paint the glyph.
     */
    public void paint(GraphicContext gc) {
	paint(gc, x(), ymin(), ymax(), isRaised(),
	      brightColor(), shadowColor());
    }

    /**
     * Adds the bounds of this line to the given rectangular bounds.
     */
    public void addBounds(Bounds bounds) {
	bounds.add(x(), ymin(), 0, ymax() - ymin());
	bounds.leftPixels = Math.max(bounds.leftPixels, 1);
	bounds.rightPixels = Math.max(bounds.rightPixels, 1);
    }

    /**
     * The purpose of the VLine3DGlyph.Concrete class is to provide
     * a ready-to-use glyph to the application programmer.
     */
    public static class Concrete extends VLine3DGlyph
    {
	private double x, ymin, ymax;
	private Color bright, shadow;
	private boolean isRaised;

	public Concrete(double x, double ymin, double ymax,
			boolean isRaised)
	{
	    this(x, ymin, ymax, isRaised,
		 Constants.bright3DColor, Constants.shadow3DColor);
	}

	public Concrete(double x, double ymin, double ymax,
			boolean isRaised,
			Color bright, Color shadow)
	{
	    setLine(x, ymin, ymax);
	    this.bright = bright;
	    this.shadow = shadow;
	    this.isRaised = isRaised;
	}

	public double x() { return x; }
	public double ymin() { return ymin; }
	public double ymax() { return ymax; }
	public boolean isRaised() { return isRaised; }
	public Color brightColor() { return bright; }
	public Color shadowColor() { return shadow; }

	public void setLine(double x, double ymin, double ymax) {
	    this.repaint();
	    this.x = x;
	    this.ymin = ymin;
	    this.ymax = ymax;
	    this.repaint();
	}

	public void setRaised(boolean raised) {
	    isRaised = raised;
	    this.repaint();
	}

	public void setBrightColor(Color c) {
	    bright = c;
	    this.repaint();
	}

	public void setShadowColor(Color c) {
	    shadow = c;
	    this.repaint();
	}
    }
}
