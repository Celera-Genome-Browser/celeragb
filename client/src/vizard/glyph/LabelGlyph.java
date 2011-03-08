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
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;


/**
 * A label glyph.
 */
public abstract class LabelGlyph extends Glyph
{
    private static FontRenderContext defaultContext = new FontRenderContext(null, false, false);

    /**
     * Return the label x coordinate.
     */
    public double x() { return 0; }

    /**
     * Return the label y coordinate.
     */
    public double y() { return 0; }

    /**
     * Return the string being displayed.
     */
    public abstract String string();

    /**
     * Return the font.
     */
    public Font font() { return Constants.cleanFont; }

    /**
     * Return the color.
     */
    public Color color() { return Color.black; }

    /**
     * Return the background color.
     *
     * If it is null, the label does not paint a background.
     */
    public Color backgroundColor() { return null; }

    /**
     * Paint a label with the given parameters.
     */
    public static void paint(GraphicContext gc, double x, double y,
			     String s, Font f,
			     Color c, Color background)
    {
        double k = gc.getTransform().getScaleY();
        if (k != 1) {
            f = f.deriveFont((float)(k * f.getSize()));
        }

	gc.setFont(f);

	if (background != null) {
	    Rectangle2D r = f.getStringBounds(s, gc.getFontRenderContext());
	    gc.setColor(background);
	    gc.fill(r);
	}

	gc.setColor(c);
	gc.drawString(s, (float)x, (float)y);
    }

    /**
     * Paint the label.
     */
    public void paint(GraphicContext gc) {
	paint(gc, x(), y(), string(), font(), color(), backgroundColor());
    }

    /**
     * If this label intersects the given deviceRect,
     * a PickThrowable is thrown.
     */
    public PickedList pick(GraphicContext gc, Rectangle deviceRect) {
	Font f = font();
	gc.setFont(f);
	Rectangle2D r = f.getStringBounds(string(), gc.getFontRenderContext());

	gc.translate(x(), y());
	if (gc.hit(deviceRect, r, false))
	    return new PickedList(this, gc);

	return null;
    }

    public void addBounds(Bounds bounds) {
	//Bug: only approximate without knowing the transform
	Rectangle2D r = font().getStringBounds(string(), defaultContext);
	bounds.add(r);
    }

    /**
     * The purpose of the LabelGlyph.Concrete class is to provide
     * a ready-to-use glyph to the application programmer.
     */
    public static class Concrete extends LabelGlyph
    {
	private double x, y;
	private String string;
	private Font font;
	private Color color, background;

	public Concrete(double x, double y, String s, Font f,
			Color c, Color b)
	{
	    this.x = x;
	    this.y = y;
	    string = s;
	    font = f;
	    color = c;
	    background = b;
	}

	public double x() { return x; }
	public double y() { return y; }
	public String string() { return string; }
	public Font font() { return font; }
	public Color color() { return color; }
	public Color backgroundColor() { return background; }

	public void setPosition(double x, double y) {
	    repaint();
	    this.x = x;
	    this.y = y;
	    repaint();
	}

	public void setString(String s) {
	    repaint();
	    string = s;
	    repaint();
	}

	public void setFont(Font f) {
	    repaint();
	    font = f;
	    repaint();
	}

	public void setColor(Color c) {
	    color = c;
	    repaint();
	}

	public void setBackgroundColor(Color c) {
	    background = c;
	    repaint();
	}
    }
}
