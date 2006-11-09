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

import java.awt.Component;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.event.MouseInputListener;


/**
 * The event dispatcher is one of the players involved in the translation
 * of raw user events (mouse moved, key pressed...) into semantic actions.
 *
 * The event dispatcher is a singleton that listens for swing events,
 * translates them into glyph events, and forwards them to the proper
 * interactors.
 *
 * Interactors can be added/removed per glyph-instance or per glyph-class.
 *
 * The event dispatcher generates glyph enter/leave events, either
 * because the mouse moved, or because of a call to the public method
 * resetCurrentPosition(component,x,y).
 */
public class EventDispatcher
    implements ObjectWithPreferences,
	       KeyListener,
	       MouseInputListener,
	       DropTargetListener
{
    private HashMap interactorMap = new HashMap();
    private RootGlyph root;
    private PickedList pickedList;
    private int x, y;
    private Glyph currentGlyph;
    private AffineTransform currentTransform;
    private HashSet grabbers = new HashSet();
    private MouseEvent lastMouseEvent;

    /**
     * The singleton instance.
     */
    public static final EventDispatcher instance = new EventDispatcher();

    /**
     * Preference: the sensibility in pixels.
     *
     * A glyph is considered to be picked if the mouse is less
     * than SENSIBILITY pixels away from it.
     */
    public static final String SENSIBILITY_PREF = "Mouse sensibility in pixels";
    public static Integer SENSIBILITY = new Integer(5);

    /**
     * Ask the event dispatcher to listen for swing events
     * in the given glyph container.
     */
    public void listen(GlyphContainer container) {
	Component component = (Component)container;

	component.addKeyListener(this);
	component.addMouseListener(this);
	component.addMouseMotionListener(this);
	/*
	new DropTarget(component,
		       DnDConstants.ACTION_COPY_OR_MOVE,
		       this, true, null);
	*/
    }

    public static interface Filter {
      boolean isValid(Glyph glyph);
    }

    public static final Filter TRUE_FILTER = new Filter() {
        public boolean isValid(Glyph glyph) { return true; }
      };

    private static class FilteredInteractor
    {
      Interactor itor;
      Filter filter;
      FilteredInteractor(Interactor itor, Filter filter) {
        this.itor = itor;
        this.filter = filter;
      }
      public boolean equals(Object o) {
        if (!(o instanceof FilteredInteractor))
          return false;
        FilteredInteractor fi = (FilteredInteractor)o;
        return fi.itor == itor && fi.filter == filter;
      }
    }

    /**
     * Return the set of interactors for the given glyph, or null.
     *
     * This includes the interactors for the gyph instance,
     * the interactors for the glyph class, and for all the glyph superclasses.
     */
    public Set interactorsFor(Glyph glyph) {
	HashSet set = null;

	Collection col = (Collection)interactorMap.get(glyph);
	if (col != null) {
	    if (set == null)
		set = new HashSet();
	    set.addAll(col);
	}

	for (Class c = glyph.getClass(); c != null; c = c.getSuperclass()) {
	    col = (Collection)interactorMap.get(c);
	    if (col != null) {
		if (set == null)
		    set = new HashSet();
                Iterator i = col.iterator();
		while(i.hasNext()) {
                  FilteredInteractor fi = (FilteredInteractor)i.next();
                  if (fi.filter.isValid(glyph))
                    set.add(fi.itor);
                }
	    }
	}

	return set;
    }

    /**
     * Add the given interactor to the given glyph instance.
     */
    public void addInteractor(Glyph glyph, Interactor interactor) {
	ArrayList list = (ArrayList)interactorMap.get(glyph);
	if (list == null) {
	    list = new ArrayList();
	    interactorMap.put(glyph, list);
	}
	list.add(interactor);
    }

    /**
     * r the given interactor from the given glyph instance.
     */
    public void removeInteractor(Glyph glyph, Interactor interactor) {
	ArrayList list = (ArrayList)interactorMap.get(glyph);
	list.remove(interactor);
    }

    public void addInteractor(Class glyphClass, Interactor interactor, Filter filter) {
	ArrayList list = (ArrayList)interactorMap.get(glyphClass);
	if (list == null) {
	    list = new ArrayList();
	    interactorMap.put(glyphClass, list);
	}
	list.add(new FilteredInteractor(interactor, filter));
    }

    /**
     * Add the given interactor to the given glyph class.
     */
    public void addInteractor(Class glyphClass, Interactor interactor) {
        addInteractor(glyphClass, interactor, TRUE_FILTER);
    }

    public static boolean hasAncestor(Component a, Component b) {
      if (a == null)
        return false;
      if (a.getParent() == b)
        return true;
      return hasAncestor(a.getParent(), b);
    }

    /**
     * Remove the given interactor from the given glyph class.
     */
    public void removeInteractor(Class glyphClass, Interactor interactor) {
      removeInteractor(glyphClass, interactor, TRUE_FILTER);
    }

    public void removeInteractor(Class glyphClass, Interactor interactor, Filter filter) {
	ArrayList list = (ArrayList)interactorMap.get(glyphClass);
	list.remove(new FilteredInteractor(interactor, filter));
    }

    /**
     * Return the current picked list
     */
    public PickedList pickedList() {
	return pickedList;
    }

    /**
     * Return the current x position in the window.
     */
    public int x() {
	return x;
    }

    /**
     * Return the current root glyph.
     */
    public RootGlyph root() {
        return root;
    }

    /**
     * Return the current y position in the window.
     */
    public int y() {
	return y;
    }

    /**
     * Changes the "current position".
     *
     * The event dispatcher automatically executes this public method
     * when the mouse moves.
     * The new top glyph is recomputed and, if different from the previous
     * one, the necessary glyph enter/leave events are dispatched.
     */
    public void resetCurrentPosition(GlyphContainer container,
				     int x, int y)
    {
	this.x = x;
	this.y = y;
	root = container.rootGlyph();
	dispatchEnterLeave();
    }

    /**
     * Return the current glyph.
     *
     * That is, the glyph that received the event.
     * (the returned value is undefined if called outside
     * of an interaction).
     */
    public Glyph currentGlyph() {
	return currentGlyph;
    }

    /**
     * Return the transform of the current glyph.
     *
     * The glyph's structure being a directed acyclic graph, there are
     * multiple paths to reach a glyph.
     * The current transform is the one that has been used to decide
     * that this glyph received the event.
     */
    public AffineTransform currentTransform() {
	return currentTransform;
    }

    public void grab(Interactor itor) {
	grabbers.add(itor);
    }

    public void ungrab(Interactor itor) {
	grabbers.remove(itor);
    }

    /**
     * Dispatch the given event.
     */
    public void keyPressed(KeyEvent e) {
	dispatchEvent(e, keyPressedCaller);
    }

    /**
     * Dispatch the given event.
     */
    public void keyReleased(KeyEvent e) {
	dispatchEvent(e, keyReleasedCaller);
    }

    /**
     * Dispatch the given event.
     */
    public void keyTyped(KeyEvent e) {
	dispatchEvent(e, keyTypedCaller);
    }

    /**
     * Dispatch the given event.
     */
    public void mouseClicked(MouseEvent e) {
	lastMouseEvent = e;
        dispatchEvent(e, mouseClickedCaller);
    }

    /**
     * Dispatch the given event.
     */
    public void mousePressed(MouseEvent e) {
	lastMouseEvent = e;
        dispatchEvent(e, mousePressedCaller);
    }

    /**
     * Dispatch the given event.
     */
    public void mouseReleased(MouseEvent e) {
	lastMouseEvent = e;
        dispatchEvent(e, mouseReleasedCaller);
    }

    /**
     * Reset the current position (and those dispatch the appropriate
     * glyph enter/leave events) and then dispatch the given event.
     */
    public void mouseMoved(MouseEvent e) {
	lastMouseEvent = e;
        resetCurrentPosition((GlyphContainer)e.getComponent(),
			     e.getX(), e.getY());
	dispatchEvent(e, mouseMovedCaller);
    }

    /**
     * Reset the current position (and those dispatch the appropriate
     * glyph enter/leave events) and then dispatch the given event.
     */
    public void mouseDragged(MouseEvent e) {
	lastMouseEvent = e;
        resetCurrentPosition((GlyphContainer)e.getComponent(),
			     e.getX(), e.getY());
	dispatchEvent(e, mouseDraggedCaller);
    }

    /**
     * Dispatch a glyph entered event.
     */
    public void mouseEntered(MouseEvent e) {
	lastMouseEvent = e;
        resetCurrentPosition((GlyphContainer)e.getComponent(),
			     e.getX(), e.getY());
    }

    /**
     * Dispatch a glyph exited event.
     */
    public void mouseExited(MouseEvent e) {
	lastMouseEvent = e;
        dispatchEvent(e, glyphExitedCaller);
	pickedList = null;
    }


    /**
     * Method to expose the last mouse event to the controllers.
     */
    public MouseEvent getLastMouseEvent() { return lastMouseEvent; }


    /**
     * Drop the given event.
     */
    public void dragEnter(DropTargetDragEvent e) {
    }

    /**
     * Drop the given event.
     */
    public void dragExit(DropTargetEvent e) {
    }

    /**
     * Reset the current position (and thus dispatch the appropriate
     * glyph enter/leave events) and then dispatches the given event.
     */
    public void dragOver(DropTargetDragEvent e) {
	Component component = e.getDropTargetContext().getComponent();
	resetCurrentPosition((GlyphContainer)component,
			     (int)e.getLocation().getX(),
			     (int)e.getLocation().getY());
	dispatchEvent(e, dragOverCaller);
    }

    /**
     * Dispatch the given event.
     */
    public void drop(DropTargetDropEvent e) {
	dispatchEvent(e, dropCaller);
    }

    /**
     * Dispatch the given event.
     */
    public void dropActionChanged(DropTargetDragEvent e) {
    }

    public void getPreferences(Preferences prefs) {
	prefs.add(this, SENSIBILITY_PREF, SENSIBILITY);
    }

    public void preferencesChanged(Preferences oldPreferences) {
    }

    /**
     * Ask the EventDispatcher to generate enter-glyph/leave-glyph events.
     *
     * The generation of enter/leave events is automatic whenever the mouse is moved.
     * But there is another case where enter/leave events should be generated, without the
     * mouse being moved: suppose the mouse is over glyph A, and the application deletes the glyph A.
     * Then the event-dispatcher should dispatch enter/leave events.
     * In such a case, the application should call eventDispatcher.dispatchEnterLeave().
     */
    public void dispatchEnterLeave() {
      PickedList newPickedList = root.pickTopGlyph(x, y, SENSIBILITY.intValue());

      if (pickedList != null &&
          newPickedList.glyph(0) == pickedList.glyph(0))
          return;

      if (pickedList != null)
          dispatchEvent(null, glyphExitedCaller);

      pickedList = newPickedList;
//      for (int x=0; x< pickedList.count();x++) {
//        System.out.println(pickedList.glyph(x).getClass());
//      }
//      System.out.println("");
      dispatchEvent(null, glyphEnteredCaller);
    }

    private void dispatchEvent(Object e, Caller caller) {
        if (pickedList == null)
            return;

	HashSet uncalledGrabbers = new HashSet(grabbers);

        PickedList myPickedCopy = new PickedList(pickedList);

	int count = myPickedCopy.count();
	for(int n = 0; n < count; ++n) {
	    boolean wasGlyphActivated = false;
	    currentGlyph = myPickedCopy.glyph(n);
            Set interactorSet = interactorsFor(currentGlyph);
            if (interactorSet != null) {
    	        Iterator i = interactorSet.iterator();
	        while(i.hasNext()) {
		    Interactor itor = (Interactor)i.next();
		    uncalledGrabbers.remove(itor);
                    currentGlyph = myPickedCopy.glyph(n);
                    currentTransform = myPickedCopy.transform(n);
		    if (caller.call(itor, e))
		        wasGlyphActivated = true;
	        }
            }
	    if (wasGlyphActivated)
		break;
	}

	if (!uncalledGrabbers.isEmpty()) {
	    Iterator i = uncalledGrabbers.iterator();
	    while(i.hasNext()) {
		Interactor itor = (Interactor)i.next();
                currentGlyph = myPickedCopy.glyph(0);
                currentTransform = myPickedCopy.transform(0);
		caller.call(itor, e);
	    }
	}
    }

    private EventDispatcher() {
    }

    private interface Caller
    {
	boolean call(Interactor i, Object e);
    }

    private static Caller keyPressedCaller = new Caller() {
	    public boolean call(Interactor i, Object e) {
		return i.keyPressed(EventDispatcher.instance.currentGlyph,
                                    EventDispatcher.instance.currentTransform,
                                    (KeyEvent)e);
	    }};
    private static Caller keyReleasedCaller = new Caller() {
	    public boolean call(Interactor i, Object e) {
		return i.keyReleased(EventDispatcher.instance.currentGlyph,
                                     EventDispatcher.instance.currentTransform,
                                     (KeyEvent)e);
	    }};
    private static Caller keyTypedCaller = new Caller() {
	    public boolean call(Interactor i, Object e) {
		return i.keyTyped(EventDispatcher.instance.currentGlyph,
                                  EventDispatcher.instance.currentTransform,
                                  (KeyEvent)e);
	    }};
    private static Caller mouseClickedCaller = new Caller() {
	    public boolean call(Interactor i, Object e) {
		return i.mouseClicked(EventDispatcher.instance.currentGlyph,
                                      EventDispatcher.instance.currentTransform,
                                      (MouseEvent)e);
	    }};
    private static Caller mousePressedCaller = new Caller() {
	    public boolean call(Interactor i, Object e) {
		return i.mousePressed(EventDispatcher.instance.currentGlyph,
                                      EventDispatcher.instance.currentTransform,
                                      (MouseEvent)e);
	    }};
    private static Caller mouseReleasedCaller = new Caller() {
	    public boolean call(Interactor i, Object e) {
		return i.mouseReleased(EventDispatcher.instance.currentGlyph,
                                       EventDispatcher.instance.currentTransform,
                                       (MouseEvent)e);
	    }};
    private static Caller mouseMovedCaller = new Caller() {
	    public boolean call(Interactor i, Object e) {
		return i.mouseMoved(EventDispatcher.instance.currentGlyph,
                                    EventDispatcher.instance.currentTransform,
                                    (MouseEvent)e);
	    }};
    private static Caller mouseDraggedCaller = new Caller() {
	    public boolean call(Interactor i, Object e) {
		return i.mouseDragged(EventDispatcher.instance.currentGlyph,
                                      EventDispatcher.instance.currentTransform,
                                      (MouseEvent)e);
	    }};
    private static Caller glyphEnteredCaller = new Caller() {
	    public boolean call(Interactor i, Object e) {
		return i.glyphEntered(EventDispatcher.instance.currentGlyph,
                                      EventDispatcher.instance.currentTransform);
	    }};
    private static Caller glyphExitedCaller = new Caller() {
	    public boolean call(Interactor i, Object e) {
		return i.glyphExited(EventDispatcher.instance.currentGlyph,
                                     EventDispatcher.instance.currentTransform);
	    }};
    private static Caller dragOverCaller = new Caller() {
	    public boolean call(Interactor i, Object e) {
		return i.dragOver(EventDispatcher.instance.currentGlyph,
                                  EventDispatcher.instance.currentTransform,
                                  (DropTargetDragEvent)e);
	    }};
    private static Caller dropCaller = new Caller() {
	    public boolean call(Interactor i, Object e) {
		return i.drop(EventDispatcher.instance.currentGlyph,
                              EventDispatcher.instance.currentTransform,
                              (DropTargetDropEvent)e);
	    }};
}
