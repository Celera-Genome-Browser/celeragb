// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package vizard.genomics.glyph;

import vizard.Bounds;
import vizard.GraphicContext;
import vizard.PickedList;
import vizard.glyph.Constants;
import vizard.glyph.LabelGlyph;

import java.awt.*;
import java.awt.geom.Rectangle2D;


public class TierNameGlyph extends LabelGlyph
{
    //@todo preferences
    private Font REGULAR_FONT = Constants.cleanFont;
    private Font BOLD_FONT = Constants.cleanBoldFont;

    private String name;
    private double height;

    public TierNameGlyph(String name, double height) {
	this.name = name;
	this.height = height;
    }

    void reset(String name, double height) {
	repaint();
	this.name = name;
	this.height = height;
	repaint();
    }

    /**
     * Return the corresponding tier glyph.
     */
    public TierGlyph correspondingTierGlyph() {
        TierNamesColumnGlyph column = (TierNamesColumnGlyph)parent().parent();
        return column.correspondingTierGlyph(this);
    }

    //Glyph specialization

    public double x() { return 5; }
    public double y() { return height / 2 + 4; }
    public String string() { return name; }
    public Color color() { return Color.white; }
    public Font font() {
		try {
			return correspondingTierGlyph().isExpanded() ? BOLD_FONT : REGULAR_FONT;
		}
		catch(Exception x) {
			System.out.println("Catching a font exception from within TierNameGlyph.  Need to fix underlying problem.");
			return REGULAR_FONT;
		}
    }

    public void addBounds(Bounds bounds) {
	bounds.add(0, -TiersColumnGlyph.INTERSPACE/2,
		   1000, height + TiersColumnGlyph.INTERSPACE);
    }

    public PickedList pick(GraphicContext gc, Rectangle deviceRect) {
	Rectangle2D.Double shape = gc.tempRectangle();
	shape.setRect(0, - TiersColumnGlyph.INTERSPACE/2,
		      1000, height + TiersColumnGlyph.INTERSPACE);
	return gc.hit(deviceRect, shape, false)
	    ? new PickedList(this, gc) : null;
    }
}
