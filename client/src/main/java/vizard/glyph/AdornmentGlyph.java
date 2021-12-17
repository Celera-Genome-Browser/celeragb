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

import java.awt.*;


/**
 * The purpose of the AdornmentGlyph is to draw a, slightly bigger, rectangular outline
 * around the bounds of any glyph.
 *
 * The adornment-glyph automatically follows any changes in the bounds of the target glyph.
 */
public class AdornmentGlyph extends GlyphAdapter
    implements Glyph.BoundsObserver
{
    private Color color;
    private Bounds targetBounds;
    private Glyph target;
    private int pixelGap;

    /**
     * Initialize a new AdornmentGlyph for the given target-glyph.
     * The rectangular outline will be painted with the given color.
     */
    public AdornmentGlyph(Glyph target, Color color, int pixelGap) {
	this.target = target;
	this.color = color;
	this.pixelGap = pixelGap;
        target.addBoundsObserver(this);
        targetBounds = target.getBounds();
    }

    /**
     * Delete this glyph.
     */
    public void delete() {
	target.removeBoundsObserver(this);
	super.delete();
    }

    /**
     * Return the bounds of the target glyph.
     */
    public Bounds targetBounds() {
        return new Bounds(targetBounds);
    }

    /**
     * Paint the adornment.
     */
    public void paint(GraphicContext gc) {
	double pixw = gc.pixelWidth();
	double pixh = gc.pixelHeight();
	Bounds targetBounds = targetBounds();
	FastRectGlyph.paint
	    (gc,
	     targetBounds.x - pixw * pixelGap,
	     targetBounds.y - pixh * pixelGap,
	     targetBounds.width + pixw * (2*pixelGap-1),
	     targetBounds.height + pixh * (2*pixelGap-1),
	     null, color);
    }

    /**
     * Add the bounds of this rectangle to the given rectangular bounds.
     */
    public void addBounds(Bounds bounds) {
	Bounds targetBounds = targetBounds();
	extendTargetBounds(targetBounds);
	bounds.add(targetBounds);
    }

    private void extendTargetBounds(Bounds bounds) {
	bounds.addLeftRightPixels(pixelGap + bounds.leftPixels, (2*pixelGap) + bounds.rightPixels);
	bounds.addUpDownPixels(pixelGap + bounds.upPixels, (2*pixelGap) + bounds.downPixels);
    }

    // Glyph.BoundsObserver specialization
    public void handleBoundsChange() {
        Bounds newBounds = target.getBounds();
        if (!newBounds.equals(targetBounds)) {
            repaint();
            targetBounds = newBounds;
            repaint();
            //boundsChanged();
        }
    }
}
