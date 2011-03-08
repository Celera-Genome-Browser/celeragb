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
package vizard;

import java.awt.geom.Rectangle2D;


/**
 * The purpose of the Bounds class is to allow the definition of a rectangular
 * area that depends both on user space coordinates and on pixel coordinates.
 *
 * For example, for a vertical line that has a constant width of one pixel:
 *      line location:          x (user space)
 *      line width:             0 (user space)
 *      line extra pixel width: 1 (pixels)
 */
public class Bounds extends Rectangle2D.Double
{
    /**
     * The number of extra pixels on the left side of the user-space rectangle.
     */
    public int leftPixels;

    /**
     * The number of extra pixels on the right side of the user-space rectangle.
     */
    public int rightPixels = 1;

    /**
     * The number of extra pixels on the up side of the user-space rectangle.
     */
    public int upPixels;

    /**
     * The number of extra pixels on the down side of the user-space rectangle.
     */
    public int downPixels = 1;

    /**
     * Initialize a new Bounds instance with the given user space coordinates.
     * The extra pixel sizes are all initialized to 0.
     */
    public Bounds(double x, double y, double w, double h) {
	setRect(x, y, w, h);
    }

    /**
     * Initialize a new Bounds instance to be equal to the given bounds.
     */
    public Bounds(Bounds b) {
	set(b);
    }

    /**
     * Initialize a new, empty, Bounds instance.
     */
    public Bounds() {
	reset();
    }

    /**
     * Return true if these bounds are equal to the given bounds.
     */
    public boolean equals(Object o) {
	if (!(o instanceof Bounds))
	    return false;
	Bounds b = (Bounds)o;
	return (x == b.x && y == b.y && width == b.width && height == b.height &&
		leftPixels == b.leftPixels && rightPixels == b.rightPixels &&
		upPixels == b.upPixels && downPixels == b.downPixels);
    }

    /**
     * Make these bounds empty.
     */
    public void reset() {
	width = height = -1;
	resetPixels();
    }

    /**
     * Set the up- and left- extra pixels to 0
     * and the down- and right- extra pixels to 1.
     */
    public void resetPixels() {
	leftPixels = upPixels = 0;
	rightPixels = downPixels = 1;
    }

    /**
     * Set these bounds to be equal to the given bounds.
     */
    public void set(Bounds b) {
	setRect(b);
	leftPixels = b.leftPixels;
	rightPixels = b.rightPixels;
	upPixels = b.upPixels;
	downPixels = b.downPixels;
    }

    /**
     * Set these user-space bounds to be equal to the given rectangle.
     *
     * The extra pixels are reset to (0, 0, 1, 1).
     */
    public void setRect(Rectangle2D.Double rect) {
	setRect(rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Set these user-space bounds to be equal to the given rectangle.
     *
     * The extra pixels are reset to (0, 0, 1, 1).
     */
    public void setRect(double x, double y, double w, double h) {
	super.setRect(x, y, w, h);
	//NO!!! resetPixels();
    }

    /**
     * Return true if these bounds are empty.
     *
     * Note that a zero-width or zero-height bounds are not empty
     * if the extra pixels are greater than 0.
     */
    public boolean isEmpty() {
	return width < 0 || height < 0;
    }

    /**
     * Add the given bounds to these bounds.
     *
     * The extra pixels are not added but set to the max between
     * these bounds extra pixels and the added bounds extra pixels.
     */
    public void add(Bounds b) {
	if (b.isEmpty())
	    return;
	if (isEmpty()) {
	    set(b);
	    return;
	}
	super.add(b);
	leftPixels = Math.max(leftPixels, b.leftPixels);
	rightPixels = Math.max(rightPixels, b.rightPixels);
	upPixels = Math.max(upPixels, b.upPixels);
	downPixels = Math.max(downPixels, b.downPixels);
    }

    /**
     * Add the given rectangular area to these bounds.
     *
     * The extra pixels are not changed.
     */
    public void add(double x, double y, double w, double h) {
	if (isEmpty()) {
	    setRect(x, y, w, h);
	    return;
	}
	add(x, y);
	add(x+w, y+h);
    }

    /**
     * The current left (right) extra pixel is replaced with the max
     * between the current and the given.
     */
    public void addLeftRightPixels(int left, int right) {
        if (left > leftPixels)
            leftPixels = left;
        if (right > rightPixels)
            rightPixels = right;
    }

    /**
     * The current up (down) extra pixel is replaced with the max
     * between the current and the given.
     */
    public void addUpDownPixels(int up, int down) {
        if (up > upPixels)
            upPixels = up;
        if (down > downPixels)
            downPixels = down;
    }

    public boolean intersects(Bounds b) {
        return !isEmpty() && !b.isEmpty() &&
               !(getMaxX() < b.x || b.getMaxX() < x) &&
               !(getMaxY() < b.y || b.getMaxY() < y);
    }

    /**
     * Return true if these bounds intersect the given device rectangle.
     * The user space transform is available in the given graphic context.
     *
     * This method does not only intersect user space rectangles, but also
     * takes into account the extra pixels.
     */
    public boolean intersects(GraphicContext gc, Rectangle2D deviceRect) {
        double pixw = gc.pixelWidth();
        if (deviceRect.getMinX() >= getMaxX() + rightPixels * pixw ||
            getMinX() - leftPixels * pixw >= deviceRect.getMaxX())
            return false;

        double pixh = gc.pixelHeight();
        if (deviceRect.getMinY() >= getMaxY() + downPixels * pixh ||
            getMinY() - upPixels * pixh >= deviceRect.getMaxY())
            return false;

        return true;
    }

    /**
     * Return true if these bounds are totally inside the given "bigger" bounds.
     *
     * Totally-inside implies that the bounds bounds do NOT share the same boundary.
     */
    public boolean totallyInside(Bounds bigger) {
	return (x > bigger.x && y > bigger.y &&
		getMaxX() < bigger.getMaxX() && getMaxY() < bigger.getMaxY());
    }
}
