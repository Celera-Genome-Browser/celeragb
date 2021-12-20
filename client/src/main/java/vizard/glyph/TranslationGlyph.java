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
 * The purpose of the translation glyph is to translate the paintings
 * of its subtree of children.
 */
public abstract class TranslationGlyph extends ParentGlyph
{
    /**
     * Return the translation along the X axis.
     */
    public abstract double tx();

    /**
     * Return the translation along the Y axis.
     */
    public abstract double ty();

    /**
     * Invalidate the translated version of the given area.
     */
    public void inval(Bounds b) {
	b.x += tx();
	b.y += ty();
	super.inval(b);
    }

    /**
     * Translate the graphic context, forward the paint to its children,
     * and restore the graphic context.
     */
    public void paint(GraphicContext gc) {
	double tx = tx(), ty = ty();

	if (tx == 0 && ty == 0)
	    super.paint(gc);
	else {
	    gc.translate(tx, ty);
	    super.paint(gc);
	    gc.translate(-tx, -ty);
	}
    }

    /**
     * Translate the graphic context, forward the pick to its children,
     * and restore the graphic context.
     */
    public PickedList pick(GraphicContext gc, Rectangle deviceRect) {
	double tx = tx(), ty = ty();

	gc.translate(tx, ty);
	PickedList list = super.pick(gc, deviceRect);
	gc.translate(-tx, -ty);

	return list;
    }

    /**
     * Add the translated children bounds to the given bounds.
     */
    public void addBounds(Bounds bounds) {
	Bounds b = bounds.isEmpty() ? bounds : new Bounds();
	super.addBounds(b);
	b.x += tx();
	b.y += ty();
	if (b != bounds)
	    bounds.add(b);
    }

    /**
     * The purpose of the TranslationGlyph.Concrete class is to provide
     * a ready-to-use glyph to the application programmer.
     */
    public static class Concrete extends TranslationGlyph
    {
	private double tx, ty;

	public Concrete(double tx, double ty) {
	    setTranslation(tx, ty);
	}

	public double tx() { return tx; }
	public double ty() { return ty; }

	public void setTranslation(double tx, double ty) {
            if (tx != this.tx || ty != this.ty) {
                repaint();
                this.tx = tx;
                this.ty = ty;
                repaint();
                boundsChanged();
            }
	}
    }
}
