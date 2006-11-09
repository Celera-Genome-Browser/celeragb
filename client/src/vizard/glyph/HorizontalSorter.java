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
import vizard.ParentGlyph;
import vizard.Bounds;


/**
 * HorizontalSorter sorts its children based on their position along the X-axis.
 * Also, it refuses to have overalapping children along the X-axis.
 *
 * This very specialized glyph is used by VerticalPacker.
 */
public abstract class HorizontalSorter extends ParentGlyph
{
    public boolean addChildIfNotIntersecting(Glyph potentialChild) {
	Bounds newBounds = potentialChild.getBounds();
	int i = indexFor(newBounds);
	if (i > 0 && intersectHorizontally(newBounds, child(i - 1).getBounds()))
            return false;
	if (i < childCount() && intersectHorizontally(newBounds, child(i).getBounds()))
            return false;

	addChildAt(i, potentialChild);
	return true;
    }

    //Notification about some children bounds change
    public void handleBoundsChange() {
	for(int i = 1; i < childCount(); ++i) {
	    if (intersectHorizontally(child(i-1).getBounds(), child(i).getBounds())) {
		Glyph glyph = child(i);
                removeChildAt(i);
                intersectingChildJustGotRemoved(glyph);
		--i;
	    }
	}
        super.handleBoundsChange();
    }

    //Subclasses can override this method to do something with the child
    //that is being removed.
    public abstract void intersectingChildJustGotRemoved(Glyph glyph);

    //Use a binary search to retrieve the index for the given bounds
    private int indexFor(Bounds newBounds) {
	int min = 0;
	int max = childCount();
	while(min < max) {
	    int i = (min + max) / 2;
	    double x = child(i).getBounds().x;
	    if (newBounds.x < x)
		max = i;
	    else if (newBounds.x == x)
		return i;
	    else
		min = i + 1;
	}
	return min;
    }

    private boolean intersectHorizontally(Bounds a, Bounds b) {
	return !a.isEmpty() && !b.isEmpty() && a.getMaxX() >= b.x && b.getMaxX() >= a.x;
    }
}
