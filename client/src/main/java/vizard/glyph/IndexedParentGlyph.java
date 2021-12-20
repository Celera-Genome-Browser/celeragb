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
import vizard.util.Assert;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 *
 * The IndexedParentGlyph extends the ParentGlyph by allowing you to assign a
 * "index" to the children.  Children will be drawn from the lowest index value
 * to the highest index value.
 * The minimun and maximum allowable values for the index are reserved and as
 * the FOREGROUND and BACKGROUND.
 * In between the BACKGROUND and FOREGROUND, child glyphs are indexed by any integer between
 * Integer.MIN_VALUE+1 and Integer.MAX_VALUE-1, where child glyphs have relative
 * ordering.
 *
 * @author       Jay T. Schira
 * @version $Id$
 */
public class IndexedParentGlyph extends ParentGlyph
    implements Comparator
{
    // Static defines...
    public static int FOREGROUND = java.lang.Integer.MAX_VALUE;
    public static int BACKGROUND = java.lang.Integer.MIN_VALUE;
    public static int UNSPECIFIED = FOREGROUND - 1;  // index to give glyphs added with addChild(Glyph);
    public static int MAX_INDEX = FOREGROUND - 1;
    public static int MIN_INDEX = BACKGROUND + 1;
    private HashMap childToIndexMap;  // mapping from children (Glyph instances) to thier indexes (Integer instances)

    /**
     * Constructor...
     */
    public IndexedParentGlyph() {
	childToIndexMap = new HashMap();
    }

    /**
     * Override the ParentGlyph of addChild(Glyph);
     * Redirects to addChild(Glyph,int); with UNSPECIFIED.
     */
    public void addChildAt(int index, Glyph child) {
	addChildAtIndex(child, UNSPECIFIED);
    }

    /**
     * Override the ParentGlyph of removeChild(Glyph);
     */
    public void removeChildAt(int index) {
	childToIndexMap.remove(child(index));
	super.removeChildAt(index);
    }


    /**
     * Add a child glyph at an order index...
     */
    public void addChildAtIndex(Glyph child, int orderIndex) {
	super.addChildAt(childCount(), child);
	moveChildToOrderIndex(child, orderIndex);
    }

    /**
     * Move a child to an order index...
     *
     * All other ordering methods call this one.
     * The purpose is that subclasses only need to override this method.
     */
    public void moveChildToOrderIndex(Glyph child, int orderIndex) {
	if (Assert.debug)
	    Assert.vAssert(hasChild(child));

	childToIndexMap.put(child, new Integer(orderIndex));
	Collections.sort(children, this);
	child.repaint();
    }

    /**
     * Move a child in FRONT of another child.
     * This method trys to satify two competing concepts... the RELATIVE indexing
     * of children (in-front-of, in-back-of), and the ABSOLUTE positioning of
     * If you try to order a child "in-front-of" a reference child that is indexed to FOREGROUND,
     * the child will only go to FOREGROUND but is NOT guaranteed to be in front of the
     * reference child (which is also in the FOREGROUND).
     */
    public void moveChildInFrontOf(Glyph child, Glyph referenceChild) {
	if (Assert.debug)
	    Assert.vAssert(hasChild(child) && hasChild(referenceChild));

	int childIndex = getOrderIndexOfChild(child);
	int referenceChildIndex = getOrderIndexOfChild(referenceChild);
	if (childIndex <= referenceChildIndex)
	    moveChildToOrderIndex(child, Math.min(referenceChildIndex+1, FOREGROUND));
    }

    /**
     * Move a child in BACK of another child...
     * This does NOT work for Glyphs that are already indexed to BACK.
     */
    public void moveChildInBackOf(Glyph child, Glyph referenceChild) {
	if (Assert.debug)
	    Assert.vAssert(hasChild(child) && hasChild(referenceChild));

	int childIndex = getOrderIndexOfChild(child);
	int referenceChildIndex = getOrderIndexOfChild(referenceChild);
	if (childIndex >= referenceChildIndex)
	    moveChildToOrderIndex(child, Math.max(referenceChildIndex-1, BACKGROUND));
    }

    /**
     * Get the index of a Child.
     * Returns the order-index of the child
     */
    public int getOrderIndexOfChild(Glyph child) {
	if (!children.contains(child))
	    return -1;
	Integer indexRef = (Integer)childToIndexMap.get(child);
	if (Assert.debug)
	    Assert.vAssert(indexRef != null);
	return indexRef.intValue();
    }

    /**
     * Test is a child is in FOREGROUND.
     */
    public boolean isChildInForeground(Glyph child) {
	if (!children.contains(child)) return false;
	return (FOREGROUND == this.getOrderIndexOfChild(child));
    }


    /**
     * Test is a child is in BACKGROUND.
     */
    public boolean isChildInBackground(Glyph child) {
	if (!children.contains(child)) return false;
	return (BACKGROUND == this.getOrderIndexOfChild(child));
    }


    /**
     * Implement the comparator...
     */
    public int compare(Object o1, Object o2) {
	Integer indexRef1 = (Integer)childToIndexMap.get(o1);
	Integer indexRef2 = (Integer)childToIndexMap.get(o2);
	if ((indexRef1 == null) || (indexRef2 == null)) {
	    throw new IllegalArgumentException("IndexedParentGlyph does not have an Index for a valid child.");
	}
	return indexRef1.compareTo(indexRef2);
    }
}
