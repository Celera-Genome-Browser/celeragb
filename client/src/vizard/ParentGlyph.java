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

import vizard.util.Assert;
import vizard.util.Preferences;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;


/**
 * The purpose of the ParentGlyph is to have children glyphs.
 *
 * The list of children is ordered. This order defines the drawing
 * order and the picking order: the last child is drawn last (that is,
 * it is drawn on top of its previous brothers) and thus picked first
 * (that is, because it is on top of its previous brothers).
 */
public class ParentGlyph extends Glyph
    implements Glyph.BoundsObserver
{
    protected ArrayList children = new ArrayList();

    /**
     * Return the number of children.
     */
    public int childCount() { return children.size(); }

    /**
     * Return the child glyph at the given index.
     */
    public Glyph child(int i) { return (Glyph)children.get(i); }

    /**
     * Return an unmodifiable list of children glyphs.
     */
    public java.util.List children() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Return an array of the child glyphs.
     */
    public Glyph[] getChildrenAsArray() {
      return (Glyph[])children.toArray(new Glyph[children.size()]);
    }


    /**
     * Return the last child.
     */
    public Glyph lastChild() {
	return child(childCount() - 1);
    }


    /**
     * Return whether or not this glyph has any children.
     */
    public boolean hasChildren() {
      return !children.isEmpty();
    }

    /**
     * Return true if the given glyph is a child.
     */
    public boolean hasChild(Glyph g) {
	return children.contains(g);
    }

    /**
     * Return the index of the given child glyph,
     * or -1 if the given glyph is not a child.
     */
    public int childIndex(Glyph g) {
	return children.indexOf(g);
    }

    /**
     * Add the given child to the list of children.
     * The new child will be drawn on top of its brothers.
     *
     * This method calls addChildAt.
     * This method is final to emphasize that subclasses should
     * specialize addChildAt instead of addChild.
     */
    public final void addChild(Glyph child) {
	addChildAt(children.size(), child);
    }

    /**
     * Add all the glyphs in the given array to the list of children.
     */
    public void addChildren(Glyph[] glyphArray) {
        for(int i = 0; i < glyphArray.length; ++i) {
            addChild(glyphArray[i]);
        }
    }

    /**
     * Insert the given child glyph at the given index in the list of children.
     */
    public void addChildAt(int index, Glyph child) {
	if (Assert.debug) {
            Assert.vAssert(child != null);
	    Assert.vAssert(child != this);
	    Assert.vAssert(!(child instanceof RootGlyph));
	}
	if (Assert.slowDebug) {
	    Assert.vAssert(!child.hasAncestor(this));
	    Assert.vAssert(!hasAncestor(child));
	}

	children.add(index, child);

	if (child instanceof MultiplexerGlyph)
	    ((MultiplexerGlyph)child).parents.add(this);
	else {
	    if (Assert.debug) Assert.vAssert(child.parent == null);
	    child.parent = this;
	}

	if (isReady()) {
	    child.setReady();
	    child.repaint();
	}

        if (!(child instanceof vizard.glyph.AdornmentGlyph))
            child.boundsChanged();
    }

    /**
     * Remove the given child from the list of children.
     *
     * This method calls removeChildAt.
     * This method is final to emphasize that subclasses should
     * specialize removeChildAt instead of removeChild.
     */
    public final void removeChild(Glyph child) {
	removeChildAt(childIndex(child));
    }

    /**
     * Remove the child at the given index.
     */
    public void removeChildAt(int index) {
	Glyph child = child(index);

        if (!(child instanceof vizard.glyph.AdornmentGlyph))
            child.boundsChanged();

	if (isReady())
	    child.repaint();

	if (child instanceof MultiplexerGlyph)
	    ((MultiplexerGlyph)child).parents.remove(this);
	else
	    child.parent = null;
	children.remove(child);
    }

    /**
     * Remove all the children between the firstIndex and lastIndex included.
     */
    public void removeChildren(int firstIndex, int lastIndex) {
        for(int i = lastIndex; i >= firstIndex; --i) {
            removeChildAt(i);
        }
    }

    /**
     * Move the given child at a new position in the list of children.
     *
     * Note that the given glyph must already be a child.
     */
    public void shuffleChild(Glyph child, int newIndex) {
	int index = childIndex(child);
	if (index < newIndex)
	    --newIndex;
	children.remove(index);
	children.add(newIndex, child);
	child.repaint();
    }

    /**
     * Move the given child at the end of the list of children.
     *
     * Note that the given glyph must already be a child.
     */
    public void putOnTop(Glyph child) {
	shuffleChild(child, childCount() - 1);
    }

    /**
     * Ask this parent glyph to invalidate the given rectangular area
     * (and thus having it repainted at some later time).
     *
     * The parent glyph delegates the request to its own parent.
     */
    public void inval(Bounds bounds) {
	if (parent != null)
	    parent.inval(bounds);
    }

    /**
     * Forward the paint request to its children.
     *
     * The last child is called last and thus drawn on top of its previous
     * brothers.
     */
    public void paint(GraphicContext gc) {
	paintChildren(gc);
    }

    /**
     * Forward the paint request to its children.
     *
     * The last child is called last and thus drawn on top of its previous
     * brothers.
     */
    protected void paintChildren(GraphicContext gc) {
 	int count = children.size();
	for(int i = 0; i < count; ++i) {
	    child(i).paint(gc);
	}
    }

    /**
     * Forward the pick request to its children.
     *
     * The last child is asked first, and then the previous brothers.
     * If any child returns a non-null picked-list, this parent appends
     * itself to the list.
     */
    public PickedList pick(GraphicContext gc, Rectangle deviceRect)
    {
	for(int i = children.size() - 1; i >= 0; --i) {
	    PickedList list = child(i).pick(gc, deviceRect);
	    if (list != null)
		return list.add(this, gc);
	}
	return null;
    }

    /**
     * Invalidates the union of the bounds of all its children.
     */
    public void repaint() {
		try {
			if (parent != null) {
			    Bounds bounds = new Bounds();
			    addBounds(bounds);
			    if (parent!=null) {
			    	parent.inval(bounds);
			    } 
			}
		}
		catch(Exception ex) {
			// todo Fix synchronization problem.  For now, do not repaint after null exception
			return;
		}
    }

    /**
     * Forward the addBounds request to its children.
     */
    public void addBounds(Bounds bounds) {
	int count = children.size();
	for(int i = 0; i < count; ++i) {
	    child(i).addBounds(bounds);
	}
    }

    /**
     * Delete this parent glyph along with all its children.
     */
    public void delete() {
        super.delete();
        boolean hasMultiplexerChild = false;
        for(int i = children.size()-1; i >= 0; --i) {
            if (child(i) instanceof MultiplexerGlyph) {
                hasMultiplexerChild = true;
                break;
            }
        }

        if (!hasMultiplexerChild) {
            for(int i = children.size()-1; i >= 0; --i) {
                child(i).parent = null;
                child(i).delete();
            }
            children.clear();
        }
        else {
            while(hasChildren()) {
                Glyph child = lastChild();
                if (!(child instanceof MultiplexerGlyph))
                    child.delete();
                else {
                    MultiplexerGlyph m = (MultiplexerGlyph)child;
                    if (m.parentCount() == 1)
                        m.delete();
                    else
                        removeChild(m);
                }
            }
        }
    }

    /**
     * Notify this glyph that its preferences have changed.
     *
     * The parent-glyph implementation forwards the call
     * to its children.
     */
    public void preferencesChanged(Preferences oldPrefs) {
	super.preferencesChanged(oldPrefs);
    }

    /**
     * Add the preferences of this glyph to the given preferences object.
     * The parent-glyph implementation forwards the call to its children.
     */
    public void getPreferences(Preferences prefs) {
	int count = children.size();
	for(int i = 0; i < count; ++i) {
	    child(i).getPreferences(prefs);
	}
    }

    /**
     * @todo
     */
    public void handleBoundsChange() {
        boundsChanged();
    }

    void setReady() {
        if (!isReady()) {
            super.setReady();
            for(int i = childCount() - 1; i >= 0; --i) {
                child(i).setReady();
            }
        }
    }
}
