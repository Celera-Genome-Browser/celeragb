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
 * A slider for the horizontal ruler.
 */
public abstract class HRulerSliderGlyph extends Glyph
{
    public static String WIDTH_PREF = "The width in pixels of the ruler's slider";
    public static Integer WIDTH = new Integer(50); //pixels
    
    public static String LIGHT_PREF = "The color of the ruler's slider";
    public static Color LIGHT = new Color(153, 213, 194, 80);

    HRulerGlyph ruler;

    public abstract double location();

    double xmin, xmax;
    double ymin, ymax;

    public HRulerSliderGlyph(HRulerGlyph ruler) {
	this.ruler = ruler;
    }

    public void paint(GraphicContext gc) {
	recomputeSize(gc);

	Rectangle2D.Double r = gc.tempRectangle(xmin, ymin, xmax - xmin, ymax - ymin);

	gc.setColor(LIGHT);
	gc.fill(r);

	Rect3DGlyph.paint(gc, xmin - gc.pixelWidth(), ymin,
			  xmax-xmin, ymax-ymin, true);

	gc.setColor(Color.black);
	double pixh = gc.pixelHeight();
	gc.drawLine((int)location(), (int)(ymin + 4*pixh),
		    (int)location(), (int)(ymax - 4*pixh));
    }

    void recomputeSize(GraphicContext gc) {
	double pixw = gc.pixelWidth();
	double pixh = gc.pixelHeight();
	double x = location();
	xmin = x - WIDTH.intValue()/2 * pixw;
	xmax = x + (1 + WIDTH.intValue()/2) * pixw;
	double h = (1 - (HRulerGlyph.K_TICS.intValue()/100. + HRulerGlyph.K_SHADE.intValue()/100.)) * ruler.height();
	double y = ruler.y() + ruler.height() - h;
	ymin = y - 2 * pixh;
	ymax = y + h + 3 * pixh;
    }

    public PickedList pick(GraphicContext gc, Rectangle r) {
	recomputeSize(gc);
	Rectangle2D.Double shape = gc.tempRectangle(xmin, ymin, xmax - xmin, ymax - ymin);
	if (gc.hit(r, shape, false))
	    return new PickedList(this, gc);

	return null;
    }

    public void addBounds(Bounds bounds) {
	double x = location();
	bounds.add(x, ruler.y(), 0, ruler.height());
	bounds.leftPixels = Math.max(bounds.leftPixels, 1 + WIDTH.intValue()/2);
	bounds.rightPixels = Math.max(bounds.rightPixels, 1 + WIDTH.intValue()/2);
	bounds.upPixels = Math.max(bounds.upPixels, 2);
	bounds.downPixels = Math.max(bounds.downPixels, 2);
    }
}
