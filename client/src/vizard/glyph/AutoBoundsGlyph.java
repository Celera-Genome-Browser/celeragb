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
import vizard.util.Assert;


/**
 * The purpose of the AutoBoundsGlyph is to store and keep up-to-date
 * the rectangular bounds of its subtree of glyphs.
 *
 * These up-to-date bounds are then used to optimize calls to paint and pick
 * (quick intersection with these bounds before forwarding the request to children)
 */
public class AutoBoundsGlyph extends BoundsGlyph
{
    protected Bounds overallBounds = new Bounds();

    /**
     * Return the x location of the rectangular bounds.
     */
    public double x() { return overallBounds.x; }

    /**
     * Return the y location of the rectangular bounds.
     */
    public double y() { return overallBounds.y; }

    /**
     * Return the width of the rectangular bounds.
     */
    public double width() { return overallBounds.width; }

    /**
     * Return the height of the rectangular bounds.
     */
    public double height() { return overallBounds.height; }

    /**
     * Add the bounds of this glyph to the given bounds.
     */
    public void addBounds(Bounds bounds) {
	bounds.add(overallBounds);
    }

    /**
     * Notification that the given glyph had its bounds changed.
     * The method ensures that the overall bounds are kept up-to-date.
     */
    public void handleBoundsChange() {
        recomputeBounds();
    }

    public void recomputeBounds() {
        Bounds oldBounds = new Bounds(overallBounds);
        overallBounds.reset();
        for(int i = childCount()-1; i >= 0; --i) {
            child(i).addBounds(overallBounds);
        }
        if (!oldBounds.equals(overallBounds))
            boundsChanged();
    }

    public void checkBounds() {
        Bounds copy = new Bounds(overallBounds);
        recomputeBounds();
        Assert.vAssert(copy.equals(overallBounds));
    }
}
