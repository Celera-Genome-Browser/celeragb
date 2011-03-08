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

import vizard.Glyph;
import vizard.GraphicContext;
import vizard.ParentGlyph;
import vizard.PickedList;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * A magnifying glass glyph.
 */
public abstract class MagnifyingGlassGlyph extends ParentGlyph
{
    public static Color glassColor = new Color(144, 204, 174);
    private Glyph viewed;

    /**
     * The x coordinate of the magnifying glass center.
     */
    public abstract double x();

    /**
     * The y coordinate of the magnifying glass center.
     */
    public abstract double y();

    /**
     * The diameter of the magnifying glass.
     */
    public double diameter() { return 250; }

    /**
     * The scale factor that is applied by the magnifying glass
     * along the X axis.
     */
    public abstract double scaleX();

    /**
     * The scale factor that is applied by the magnifying glass
     * along the Y axis.
     */
    public abstract double scaleY();
    
    /**
     * Initialize a new magnifying glass that is looking at "viewed".
     */
    protected MagnifyingGlassGlyph(Glyph viewed) {
	this.viewed = viewed;

	addEllipses();
	ParentGlyph clipper = addClipper();
	clipper.addChild(addTranslationAndScale());
    }

    public void delete() {
	if (hasChild(viewed))
	    removeChild(viewed);
	super.delete();
    }

    double innerX() { return x() + 10; }
    double innerY() { return y() + 10; }
    double innerDiameter() { return diameter() - 20; }
    double clipExtra() { return 1; }

    private void addEllipses() {
	final Ellipse2D.Double shape = new Ellipse2D.Double();
	addChild(new ShapeGlyph() {
		public Shape shape() {
		    double d = diameter();
		    shape.setFrame(x(), y(), d, d);
		    return shape;
		}
		public void paint(GraphicContext gc) {
		    Paint save = gc.getPaint();
		    gc.setPaint(gc.createTexture(Constants.woodTexture));
		    super.paint(gc);
		    gc.setPaint(save);
		    double d = diameter();
		    Ellipse3DGlyph.paint(gc, x(), y(), d, d, true);
		}
	    });
	addChild(new ShapeGlyph() {
		public Shape shape() {
		    double d = innerDiameter();
		    shape.setFrame(innerX(), innerY(), d, d);
		    return shape;
		}
		public void paint(GraphicContext gc) {
		    gc.setColor(glassColor);
		    super.paint(gc);
		    double d = innerDiameter();
		    Ellipse3DGlyph.paint(gc, innerX(), innerY(), d, d, false);
		}
	    });
    }

    private ParentGlyph addClipper() {
	ParentGlyph clipper = (new ClipperGlyph() {
		Ellipse2D.Double shape = new Ellipse2D.Double();
		public double x() { return innerX(); }
		public double y() { return innerY(); }
		public double width() { return innerDiameter(); }
		public double height() { return innerDiameter(); }
		public Shape shape() {
		    double ex = clipExtra();
		    double d = innerDiameter() - 2*ex;
		    shape.setFrame(innerX() + ex, innerY() + ex, d, d);
		    return shape;
		}
	    });
	addChild(clipper);
	return clipper;
    }
    
    private ParentGlyph addTranslationAndScale() {
	ParentGlyph tr = new TranslationGlyph() {
		MagnifyingGlassGlyph m = MagnifyingGlassGlyph.this;
		public double tx() {
		    return (1 - m.scaleX()) * (m.x() + m.diameter()/2);
		}
		public double ty() {
		    return (1 - m.scaleY()) * (m.y() + m.diameter()/2);
		}
	    };

	ParentGlyph scale = new ScaleGlyph() {
		MagnifyingGlassGlyph m = MagnifyingGlassGlyph.this;
		public double sx() { return m.scaleX(); }
		public double sy() { return m.scaleY(); }
	    };
	scale.addChild(viewed);

	tr.addChild(scale);

	return tr;
    }
    
    //No interaction through the magnifier, just for visualization
    //@todo the above comment and the below code seem to contradict
    //      but... when I run it, the comment is right!
    //      I leave it the way it is for now, but I have to understand later.
    public PickedList pick(GraphicContext gc, Rectangle r) {
	PickedList list = child(0).pick(gc, r);
	return (list != null) ? list.add(this, gc) : null;
    }

    /**
     * The purpose of the MagnifyingGlassGlyph.Concrete class is to provide
     * a ready-to-use glyph to the application programmer.
     */
    public static class Concrete extends MagnifyingGlassGlyph
    {
	private double x, y, d, sx, sy;

	public Concrete(double x, double y, double diameter,
			double scaleX, double scaleY,
			Glyph viewed)
	{
	    super(viewed);
	    this.x = x; this.y = y;
	    d = diameter;
	    sx = scaleX; sy = scaleY;
	}

	public double x() { return x; }
	public double y() { return y; }
	public double diameter() { return d; }
	public double scaleX() { return sx; }
	public double scaleY() { return sy; }

	public void setCenter(double x, double y) {
	    repaint();
	    this.x = x;
	    this.y = y;
	    repaint();
	}

	public void setDiameter(double diameter) {
	    repaint();
	    d = diameter;
	    repaint();
	}

	public void setScale(double sx, double sy) {
	    repaint();
	    this.sx = sx;
	    this.sy = sy;
	    repaint();
	}
    }
}
