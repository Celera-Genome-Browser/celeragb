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

import java.util.ArrayList;


/**
 * The purpose of the multiplexer glyph is to have multiple parent glyphs
 * (and thus a glyph tree actually becomes a directed acyclic graph)
 *
 * The subtree of glyphs under the multiplexer is made visible in all parents.
 * Suppose you have a complex drawing (eg, a tree) that has to
 * be repeated in many different places (eg, a forest), then the multiplexer
 * glyph makes it possible to render the forest with a unique instance
 * of a tree.
 *
 * The multiplexer also allows the same drawing to be visible in two
 * Swing containers.
 *
 * Multiplexer does not provide methods for adding or removing a parent.
 * The parent's addChild or removeChild methods must be used instead.
 */
public class MultiplexerGlyph extends ParentGlyph
{
    ArrayList parents = new ArrayList();

    /**
     * Initialize a new multiplexer glyph.
     */
    public MultiplexerGlyph() {
    }

    /**
     * Convenient constructor that prepars for the visualization of the given glyph
     * in multiple places.
     *
     * This multiplex is inserted between the glyph's parent and the glyph itself.
     * Adding other parents to the multiplex will make the given glyph visible
     * in those new parents as well.
     */
    public MultiplexerGlyph(Glyph glyph) {
	ParentGlyph p = glyph.parent();
	p.removeChild(glyph);
	addChild(glyph);
	p.addChild(this);
    }

    /**
     * Return the number of parents.
     */
    public int parentCount() { return parents.size(); }

    /**
     * Return the parent at the given index.
     */
    public ParentGlyph parent(int i) {
	return (ParentGlyph)parents.get(i);
    }

    /**
     * Specializes the parent method to return the first parent or null.
     */
    public ParentGlyph parent() {
	return parents.isEmpty() ? null : (ParentGlyph)parents.get(0);
    }

    /**
     * Return the last parent or null.
     */
    public ParentGlyph lastParent() {
	return parents.isEmpty()
	    ? null : (ParentGlyph)parents.get(parents.size() - 1);
    }

    public void repaint() {
	if (parentCount() > 0) {
	    Bounds bounds = new Bounds();
	    addBounds(bounds);
	    inval(bounds);
	}
    }

    /**
     * Forwards the inval request to all its parents.
     */
    public void inval(Bounds bounds) {
	int count = parentCount();
	if (count == 1) {
	    parent(0).inval(bounds);
	    return;
	}
	Bounds saved = new Bounds(bounds);
	for(int i = 0; i < count; ++i) {
	    parent(i).inval(bounds);
	    bounds.set(saved);
	}
    }

    /**
     * Specializes the hasAncestor method so that all the parents
     * are used during the atempt of finding the given ancestor.
     */
    public boolean hasAncestor(Glyph ancestor) {
	int count = parentCount();
	for(int i = 0; i < count; ++i) {
	    if (parent(i) == ancestor)
		return true;
	    if (parent(i).hasAncestor(ancestor))
		return true;
	}
	return false;
    }

    /**
     * Specializes the delete method by first removing itself from all
     * its parents.
     */
    public void delete() {
	while(!parents.isEmpty()) {
	    lastParent().removeChild(this);
	}
	super.delete();
    }

    public void boundsChanged() {
        super.boundsChanged();
        for(int i = 0; i < parentCount(); ++i) {
            activeBoundsObservers.add(parent(i));
        }
    }
}
