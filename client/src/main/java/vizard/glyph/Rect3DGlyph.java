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

import vizard.GraphicContext;

import java.awt.*;
import java.awt.geom.Line2D;


/**
 * The outline of a rectangle with a 3D effect.
 */
public abstract class Rect3DGlyph extends Rectangular3DGlyph
{
    /**
     * Paint a rectangle outline with a 3D effect.
     */
    public static void paint(GraphicContext gc,
			     double x, double y, double w, double h,
			     boolean isRaised)
    {
	paint(gc, x, y, w, h, isRaised,
	      Constants.bright3DColor, Constants.shadow3DColor);
    }
    
    /**
     * Paint a rectangle outline with a 3D effect.
     */
    public static void paint(GraphicContext gc,
			     double x, double y, double w, double h,
			     boolean isRaised,
			     Color bright, Color shadow)
    {
	gc.setZeroLineWidth();

	Line2D.Double line = gc.tempLine();

	gc.setColor(isRaised ? bright : shadow);

	//top line
	line.x1 = x;
	line.x2 = x + w;
	line.y1 = line.y2 = y;
	gc.draw(line);

	//left line
	line.x2 = x;
	line.y2 = y + h;
	gc.draw(line);

	gc.setColor(isRaised ? shadow : bright);

	//right line
	line.x1 = line.x2 = x + w;
	gc.draw(line);

	//bottom line
	line.x1 = x;
	line.y1 = y + h;
	gc.draw(line);
    }

    /**
     * Paint the glyph.
     */
    public void paint(GraphicContext gc) {
	paint(gc, x(), y(), width(), height(), isRaised(),
	      brightColor(), shadowColor());
    }

    /**
     * The purpose of the Rect3DGlyph.Concrete class is to provide
     * a ready-to-use glyph to the application programmer.
     */
    public static class Concrete extends Rect3DGlyph
    {
	private double x, y, w, h;
	private Color bright, shadow;
	private boolean isRaised;

	public Concrete(double x, double y, double w, double h,
			boolean isRaised)
	{
	    this(x, y, w, h, isRaised,
		 Constants.bright3DColor, Constants.shadow3DColor);
	}

	public Concrete(double x, double y, double w, double h,
			boolean isRaised,
			Color bright, Color shadow)
	{
	    setRect(x, y, w, h);
	    this.bright = bright;
	    this.shadow = shadow;
	    this.isRaised = isRaised;
	}

	public double x() { return x; }
	public double y() { return y; }
	public double width() { return w; }
	public double height() { return h; }
	public boolean isRaised() { return isRaised; }
	public Color brightColor() { return bright; }
	public Color shadowColor() { return shadow; }

	public void setRect(double x, double y, double w, double h) {
	    repaint();
	    this.x = x;
	    this.y = y;
	    this.w = w;
	    this.h = h;
	    repaint();
	}

	public void setRaised(boolean raised) {
	    isRaised = raised;
	    repaint();
	}

	public void setBrightColor(Color c) {
	    bright = c;
	    repaint();
	}

	public void setShadowColor(Color c) {
	    shadow = c;
	    repaint();
	}
    }
}
