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
import java.awt.geom.AffineTransform;


/**
 * The purpose of the TransformGlyph is to transform the paintings
 * of its subtree of children with an arbitrary AffineTransform.
 */
public abstract class TransformGlyph extends ParentGlyph
{
    /**
     * Return the transform.
     */
    public abstract AffineTransform transform();

    /**
     * Invalidate the transformed version of the given area.
     */
    public void inval(Bounds bounds) {
	GraphicContext.transformBounds(transform(), bounds, bounds);
	super.inval(bounds);
    }

    /**
     * Apply the transformation before forwarding the paint request
     * to its children.
     * Restore the original transform before returning.
     */
    public void paint(GraphicContext gc) {
	AffineTransform original = new AffineTransform(gc.getTransform());
	gc.transform(transform());
	super.paint(gc);
	gc.setTransform(original);
    }

    /**
     * Scale the graphic context, forward the pick to its children,
     * and restore the graphic context.
     */
    public PickedList pick(GraphicContext gc, Rectangle deviceRect) {
	AffineTransform original = new AffineTransform(gc.getTransform());

	gc.transform(transform());
	PickedList list = super.pick(gc, deviceRect);
	gc.setTransform(original);

	return list;
    }

    /**
     * Add the scaled children bounds to the given bounds.
     */
    public void addBounds(Bounds bounds) {
	Bounds b = bounds.isEmpty() ? bounds : new Bounds();
	super.addBounds(b);
	GraphicContext.transformBounds(transform(), b, b);
	if (b != bounds)
	    bounds.add(b);
    }

    /**
     * The purpose of the TransformGlyph.Concrete class is to provide
     * a ready-to-use glyph to the application programmer.
     *
     * @todo create a MyTransform subclass that will call repaint
     *       each time the transform is changed.
     */
    public static class Concrete extends TransformGlyph
    {
	protected AffineTransform transform;

	public Concrete(AffineTransform transform) {
	    this.transform = transform;
	}

	public Concrete() {
	    this(new AffineTransform());
	}

	public AffineTransform transform() {
	    return transform;
	}
    }
}
