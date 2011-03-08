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

import vizard.util.ObjectWithPreferences;
import vizard.util.Preferences;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * Glyph is the abstraction that a graphic object must implement
 * in order to be integrated to the Vizard framework.
 *
 * The four methods at the core of this abstraction are:
 *
 * paint      The glyph paints itself.
 * pick       The glyph checks its intersection with a given area.
 * addBounds  the glyph adds its own rectangular area to the given bounds.
 *
 * The base Glyph class only provide access to a parent, but the
 * general glyphs structure is a directed acyclic graph.
 *
 * @see ParentGlyph
 * @see MultiplexerGlyph
 */
public abstract class Glyph
    implements ObjectWithPreferences
{
    public static interface BoundsObserver
    {
	void handleBoundsChange();
    }

    private static HashMap glyphToBoundsObserversMap = new HashMap();
    ParentGlyph parent;
    protected int flags;

    private static final int READY_BIT = 1;
    private static final int OBSERVER_BIT = 2;
    protected static final int NEXT_FREE_BIT = 2 * OBSERVER_BIT;

    /**
     * Initialize the new glyph instance.
     */
    protected Glyph() {
    }

    /**
     * Return the parent of this glyph.
     *
     * @see ParentGlyph.addChild
     * @see ParentGlyph.removeChild
     */
    public ParentGlyph parent() {
	return parent;
    }

    /**
     * Return true if this glyph has children glyphs.
     */
    public boolean hasChildren() {
        return false;
    }

    /**
     * Ask this glyph to paint itself.
     */
    public abstract void paint(GraphicContext gc);

    /**
     * If this glyph has a non-empty intersection with the given
     * device-space rectangle a PickedList instance is returned.
     * With no intersection, the method returns null.
     *
     * The device-space rectangle is typically a small rectangular area
     * around the current mouse position (thus providing for various
     * sensibilities).
     *
     * It might seem surprising at first to pass a device-space parameter
     * instead of a user-space parameter.
     * There are two good reasons for that:
     * 1. Translating a device-space rectangle into a user-space rectangle
     *    might result in a loss of precision when the user-space is defined
     *    by an arbitrary affine transform.
     * 2. The graphic context (through the underlying Graphics2D) provides
     *    a convenient method to check for the intersection:
     *        gc.hit(deviceRect, shape...)
     */
    public abstract PickedList pick(GraphicContext gc,
				    Rectangle deviceRect);

    public Bounds getBounds() {
        Bounds b = new Bounds();
        addBounds(b);
        return b;
    }

    public RootGlyph getRootGlyph() {
      Glyph tmpGlyph = this;
      while (tmpGlyph.parent()!=null) {
        tmpGlyph = tmpGlyph.parent();
      }
      return (tmpGlyph instanceof RootGlyph) ? (RootGlyph)tmpGlyph : null;
    }

    /**
     * Ask this glyph to add its own rectangular area to the given bounds.
     */
    public abstract void addBounds(Bounds bounds);

    /**
     * Ask this glyph to notify its parent that it needs to be repainted.
     *
     * @see ParentGlyph.inval
     */
    public void repaint() {
	if (parent != null) {
	    Bounds b = new Bounds();
	    addBounds(b);
	    parent.inval(b);
	}
    }

    /**
     * Ask this glyph to delete itself.
     *
     * The default implementation of this method ensures that
     * after the call the glyph has no parent.
     */
    public void delete() {
	if (parent != null)
	    parent.removeChild(this);
    }

    /**
     * Return whether or not this glyph has the given ancestor.
     */
    public boolean hasAncestor(Glyph ancestor) {
	if (parent == ancestor)
	    return true;
	return (parent == null) ? false : parent.hasAncestor(ancestor);
    }

    /**
     * Return whether this glyph is ready or not.
     *
     * Uppon creation a glyph is not ready.
     * Adding a glyph as a child of a ready parent automatically makes the glyph ready.
     * The RootGlyph is always ready.
     *
     * The purpose of this flag is to avoid propagating repaint requests
     * while a subtree of glyphs is under construction (not only would these
     * repaint-requests be a waste, but some information needed by repaint
     * might not be available yet).
     */
    public boolean isReady() {
        return (flags & READY_BIT) != 0;
    }

    //package-level protection on purpose
    void setReady() {
	flags |= READY_BIT;
    }


    // ObjectWithPreferences specialization:

    /**
     * Notifies this glyph that some preferences have changed.
     *
     * The default implementation does not do anything.
     */
    public void preferencesChanged(Preferences oldPreferences) {
    }

    /**
     * Store in the given parameter the value of
     * this glyph's preferences.
     *
     * The default implementation does not do anything.
     */
    public void getPreferences(Preferences preferences) {
    }

    /**
     * Add a bounds-observer to this glyph.
     */
    public void addBoundsObserver(Glyph.BoundsObserver observer) {
	flags |= OBSERVER_BIT;
	ArrayList observerList = (ArrayList)glyphToBoundsObserversMap.get(this);
	if (observerList == null) {
	    observerList = new ArrayList();
	    glyphToBoundsObserversMap.put(this, observerList);
	}
	observerList.add(observer);
    }

    /**
     * Remove a bounds-observer from this glyph.
     */
    public void removeBoundsObserver(Glyph.BoundsObserver observer) {
	ArrayList observerList = (ArrayList)glyphToBoundsObserversMap.get(this);
        if (observerList != null) {
            observerList.remove(observer);
            if (observerList.isEmpty()) {
                flags &= ~OBSERVER_BIT;
                glyphToBoundsObserversMap.remove(this);
            }
        }
    }

    static HashSet activeBoundsObservers = new HashSet();

    /**
     * @todo
     */
    public void boundsChanged() {
	if (hasBoundsObservers())
            activeBoundsObservers.addAll((ArrayList)glyphToBoundsObserversMap.get(this));
        if (parent != null)
            activeBoundsObservers.add(parent);
    }

    private boolean hasBoundsObservers() {
	return (flags & OBSERVER_BIT) != 0;
    }
}

