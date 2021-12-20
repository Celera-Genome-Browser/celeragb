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
import vizard.ParentGlyph;
import vizard.PickedList;

import java.awt.*;


/**
 * The purpose of the scale glyph is to scale the paintings
 * of its subtree of children.
 */
public abstract class ScaleGlyph extends ParentGlyph
{
    /**
     * Return the scale along the X axis.
     */
    public abstract double sx();

    /**
     * Return the scale along the Y axis.
     */
    public abstract double sy();

    /**
     * Invalidate the scaled version of the given area.
     */
    public void inval(Bounds b) {
	double sx = sx();
	double sy = sy();
	b.x *= sx;
	b.y *= sy;
	b.width *= sx;
	b.height *= sy;
	super.inval(b);
    }

    /**
     * Scale the graphic context, forward the paint to its children,
     * and restore the graphic context.
     */
    public void paint(GraphicContext gc) {
	double sx = sx(), sy = sy();

	gc.scale(sx, sy);
	super.paint(gc);
	gc.scale(1/sx, 1/sy);
    }

    /**
     * Scale the graphic context, forward the pick to its children,
     * and restore the graphic context.
     */
    public PickedList pick(GraphicContext gc, Rectangle deviceRect) {
	double sx = sx(), sy = sy();

	gc.scale(sx, sy);
	PickedList list = super.pick(gc, deviceRect);
	gc.scale(1/sx, 1/sy);

	return list;
    }

    /**
     * Add the scaled children bounds to the given bounds.
     */
    public void addBounds(Bounds bounds) {
	Bounds b = bounds.isEmpty() ? bounds : new Bounds();
	super.addBounds(b);
	scaleBounds(b);

	if (b != bounds)
	    bounds.add(b);
    }

    private void scaleBounds(Bounds b) {
	double sx = sx(), sy = sy();
	b.x *= sx;
	b.y *= sy;
	b.width *= sx;
	b.height *= sy;
    }

    /**
     * The purpose of the ScaleGlyph.Concrete class is to provide
     * a ready-to-use glyph to the application programmer.
     */
    public static class Concrete extends ScaleGlyph
    {
	private double sx, sy;

	public Concrete(double sx, double sy) {
	    setScale(sx, sy);
	}

	public double sx() { return sx; }
	public double sy() { return sy; }

	public void setScale(double sx, double sy) {
            repaint();
	    this.sx = sx;
	    this.sy = sy;
            repaint();
	    boundsChanged();
	}
    }
}
