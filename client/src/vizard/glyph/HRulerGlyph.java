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
import vizard.PickedList;
import vizard.util.Assert;
import vizard.util.Preferences;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


/**
 * A horizontal ruler glyph.
 */
public abstract class HRulerGlyph extends BoundsGlyph
{
    /**
     * Preferences... @todo doc
     */

    public static String MIN_TIC_SPACING_PREF =
	"Minimum number of pixels between two consecutive major tics on a ruler";
    public static Integer MIN_TIC_SPACING = new Integer(70);

    public static String K_TICS_PREF =
	"Percent of total ruler height that is reserved for drawing tics";
    public static Integer K_TICS = new Integer(30);

    public static String K_SHADE_PREF =
	"Percent of total ruler height that is reserved for drawing the horizontal shade";
    public static Integer K_SHADE = new Integer(10);

    public static String K_LABELS_PREF =
	"Position of labels on the ruler, in percent of the total ruler height";
    public static Integer K_LABELS = new Integer(80);

    public static String TIC_CANDIDATES_PREF =
	"This list defines what values the labels on the ruler can have";
    public static int TIC_CANDIDATES[] = { 10, 20, 50 };

    /**
     * Return the length of the ruler.
     *
     * Note that the first value on this ruler is always 0.
     */
    public abstract int length();

    double start;
    double end;

    /**
     * Initializes the ruler.
     */
    protected HRulerGlyph() {
	addChild(new ShapeGlyph() {
		Rectangle2D.Double shape = new Rectangle2D.Double();
		public Shape shape() {
		    shape.setRect(start, y(), end-start, height());
		    return shape;
		}
		public void paint(GraphicContext gc) {
		    Paint save = gc.getPaint();
		    gc.setPaint(gc.createTexture(Constants.woodTexture));
                    super.paint(gc);
		    gc.setPaint(save);
		    Rect3DGlyph.paint(gc, start, y(), end-start, height(), true);
		}
	    });
	addChild(new ShapeGlyph() {
		Rectangle2D.Double shape = new Rectangle2D.Double();
		public Shape shape() {
		    shape.setRect(start, y() + K_TICS.intValue()/100. * height(),
				  end-start, K_SHADE.intValue()/100. * height());
		    return shape;
		}
		public void paint(GraphicContext gc) {
		    //gc.setColor(Constants.darkWoodColor);
		    super.paint(gc);
		    Rect3DGlyph.paint(gc, start, y() + K_TICS.intValue()/100. * height(),
				      end-start, K_SHADE.intValue()/100. * height(),
				      false);
		}
	    });
	addChild(new TicsAndLabels());
    }

    /**
     * Specialization of the BoundsGlyph superclass.
     * Return 0.
     */
    public double x() { return 0; }

    /**
     * Specialization of the BoundsGlyph superclass.
     * Return the length of the ruler.
     */
    public double width() { return length(); }

    /**
     * Paint this ruler.
     */
    public void paint(GraphicContext gc) {
	if (intersectsDirtyArea(gc)) {
	    Rectangle2D dirtyBounds = gc.dirtyBounds();
	    double pixw = gc.pixelWidth();
	    start = Math.max(0, dirtyBounds.getX() - pixw);
	    end = Math.min(length(), dirtyBounds.getMaxX() + pixw);
	    paintChildren(gc);
	}
    };

    public PickedList pick(GraphicContext gc, Rectangle rect) {
	Rectangle2D.Double bounds = gc.tempRectangle();
	bounds.setRect(x(), y(),
		       width() + gc.pixelWidth(),
		       height() + gc.pixelHeight());

	return gc.hit(rect, bounds, false)
	    ? new PickedList(this, gc)
	    : null;
    }

    /**
     * Notifies this glyph that some preferences have changed.
     */
    public void preferencesChanged(Preferences oldPreferences) {
    }

    /**
     * Store in the given parameter the value of
     * this glyph's preferences.
     *
     * The default implementation does not do anything.
     */
    public void getPreferences(Preferences prefs) {
	prefs.add(this, MIN_TIC_SPACING_PREF, MIN_TIC_SPACING);
	prefs.add(this, K_TICS_PREF, K_TICS);
	prefs.add(this, K_SHADE_PREF, K_SHADE);
	prefs.add(this, K_LABELS_PREF, K_LABELS);
	prefs.add(this, TIC_CANDIDATES_PREF, TIC_CANDIDATES);
    }

    class TicsAndLabels extends GlyphAdapter
    {
	//Glyph specialization
	public void paint(GraphicContext gc) {
	    double pixw = gc.pixelWidth();

	    int majorTicSpacing = computeTicSpacing(pixw);

	    gc.setZeroLineWidth();

	    gc.translate(-pixw * 1.01, 0);
	    gc.setColor(Constants.shadow3DColor);
	    paintMinorTics(gc, majorTicSpacing);
	    paintMajorTics(gc, majorTicSpacing);
	    gc.translate(pixw * 1.01, 0);

	    gc.setColor(Constants.bright3DColor);
	    paintMinorTics(gc, majorTicSpacing);
	    paintMajorTics(gc, majorTicSpacing);

	    paintLabels(gc, majorTicSpacing);
	}

	private int computeTicSpacing(double pixw) {
	    double minTicSpacing = MIN_TIC_SPACING.intValue() * pixw;
	    int biggestCandidate = TIC_CANDIDATES[TIC_CANDIDATES.length - 1];
	    int k = 1;
	    for(; k * biggestCandidate < minTicSpacing; k *= 10);
	    for(int i = 0; i < TIC_CANDIDATES.length; ++i) {
		if (k * TIC_CANDIDATES[i] >= minTicSpacing)
		    return k * TIC_CANDIDATES[i];
	    }

	    Assert.vAssert(false);
	    return 0;
	}

	private void paintMinorTics(GraphicContext gc, int majorTicSpacing) {
	    int firstTic = firstTic(majorTicSpacing);
	    int ticSpacing = Math.max(1, majorTicSpacing / 10);
	    double normalHeight = y() + height() * K_TICS.intValue()/100. * 0.3;
	    double biggerHeight = y() + height() * K_TICS.intValue()/100. * 0.6;
	    Line2D.Double line = gc.tempLine(firstTic, y(), firstTic, 0);
	    while(line.x1 < end) {
		line.y2 = biggerHeight;
		gc.draw(line);
		line.x2 = (line.x1 += ticSpacing);
		line.y2 = normalHeight;
		for(int i = 0; i < 4; ++i) {
		    gc.draw(line);
		    line.x2 = (line.x1 += ticSpacing);
		}
	    }
	}

	private void paintMajorTics(GraphicContext gc, int ticSpacing) {
	    int firstTic = firstTic(ticSpacing);

	    Line2D.Double line = gc.tempLine(firstTic, y(), firstTic,
					     y() + height() * K_TICS.intValue()/100. * 0.8);
	    while(line.x1 < end) {
		gc.draw(line);
		line.x2 = (line.x1 += ticSpacing);
	    }
	}

	private void paintLabels(GraphicContext gc, int ticSpacing) {
	    gc.setColor(Color.white);
	    gc.setFont(Constants.cleanFont);

	    int firstTic = firstTic(ticSpacing);
	    if (firstTic < 0)
		firstTic += ticSpacing;

            double pixw = gc.pixelWidth();
            double pixh = gc.pixelHeight();

	    Point2D.Double p = gc.tempPoint(firstTic, y() + height() * K_LABELS.intValue()/100.);
	    double limit = Math.min(length(), end + ticSpacing/2);

            if (firstTic == 0) {
                gc.drawString(getTicLabel((int)p.x, ticSpacing),
                              p.x + 2*pixw, p.y + 3*pixh);
                p.x += ticSpacing;
            }

	    while(p.x < limit) {
		gc.drawString(getTicLabel((int)p.x, ticSpacing),
			      p.x - 5*pixw, p.y + 3*pixh);
		p.x += ticSpacing;
	    }
	}

	private String getTicLabel(int value, int ticSpacing) {
	    String label;
	    if (value < 1000 || rest(ticSpacing, 100) > 0)
		label = Integer.toString(value);
	    else if (rest(ticSpacing, 1000) > 0)
		label = (Integer.toString(value/1000) + '.' +
			 (rest(value,1000) / 100) + 'K');
	    else if (value < 1000000 || rest(ticSpacing, 1000000) > 0)
		label = Integer.toString(value/1000) + 'K';
	    else
		label = Integer.toString(value/1000000) + 'M';

	    return label;
	}

	public int firstTic(int ticSpacing) {
	    int first = dropRest((int)start, ticSpacing);
	    if (first >= start)
		first -= ticSpacing;
	    return first;
	}

	private int dropRest(int n, int d) {
	    return n / d * d;
	}

	private int rest(int n, int d) {
	    return n - n / d * d;
	}
    }
}
