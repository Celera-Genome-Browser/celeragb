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

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.geom.Arc2D;


/**
 * The outline of an ellipse with a 3D effect.
 */
public abstract class Ellipse3DGlyph extends Rectangular3DGlyph
{
    /**
     * Paint an ellipse outline with a 3D effect.
     */
    public static void paint(GraphicContext gc,
			     double x, double y, double w, double h,
			     boolean isRaised)
    {
	paint(gc, x, y, w, h, isRaised,
	      Constants.bright3DColor, Constants.shadow3DColor);
    }
    
    /**
     * Paint an ellipse outline with a 3D effect.
     */
    public static void paint(GraphicContext gc,
			     double x, double y, double w, double h,
			     boolean isRaised,
			     Color bright, Color shadow)
    {
	Paint saved = gc.getPaint();
	Arc2D.Double arc = gc.tempArc();

	gc.setPaint(new GradientPaint((float)x, (float)y,
				      isRaised ? bright : shadow,
				      (float)(x+w), (float)(y+h),
				      isRaised ? shadow : bright,
				      false));
	arc.x = x;
	arc.y = y;
	arc.width = w;
	arc.height = h;
	arc.start = 45;
	arc.extent = 180;
	arc.setArcType(Arc2D.OPEN);
	gc.draw(arc);

	arc.start = 180+45;
	gc.draw(arc);

	gc.setPaint(saved);
    }

    /**
     * Paint the glyph.
     */
    public void paint(GraphicContext gc) {
	paint(gc, x(), y(), width(), height(), isRaised(),
	      brightColor(), shadowColor());
    }

    /**
     * The purpose of the Ellipse3DGlyph.Concrete class is to provide
     * a ready-to-use glyph to the application programmer.
     */
    public static class Concrete extends Ellipse3DGlyph
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
	    setEllipse(x, y, w, h);
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

	public void setEllipse(double x, double y, double w, double h) {
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
