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

import java.awt.event.*;
import java.awt.geom.*;
import java.awt.dnd.*;


/**
 * Interactor is one of the players involved in the translation of raw
 * user events (mouse moved, key pressed...) into semantic actions.
 *
 * The previous player is the EventDispatcher.
 * The event dispatcher asks the interactor to handle a
 * <glyph+event> combination.
 *
 * The interactor itself only acts as an adapter.
 *
 * The next player is the concrete Interactor subclass, responsible
 * for the implementation of the various event handling methods.
 */
public abstract class Interactor
{
    /**
     * Handles a key pressed event.
     */
    public boolean keyPressed(Glyph glyph, AffineTransform t, KeyEvent e) {
	return false;
    }

    /**
     * Handles a key released event.
     */
    public boolean keyReleased(Glyph glyph, AffineTransform t, KeyEvent e) {
	return false;
    }

    /**
     * Handles a key typed event.
     */
    public boolean keyTyped(Glyph glyph, AffineTransform t, KeyEvent e) {
	return false;
    }

    /**
     * Handles a mouse clicked event.
     */
    public boolean mouseClicked(Glyph glyph, AffineTransform t, MouseEvent e) {
	return false;
    }

    /**
     * Handles a mouse pressed event.
     */
    public boolean mousePressed(Glyph glyph, AffineTransform t, MouseEvent e) {
	return false;
    }

    /**
     * Handles a mouse released event.
     */
    public boolean mouseReleased(Glyph glyph, AffineTransform t, MouseEvent e) {
	return false;
    }

    /**
     * Handles a mouse dragged event.
     */
    public boolean mouseDragged(Glyph glyph, AffineTransform t, MouseEvent e) {
	return false;
    }

    /**
     * Handles a mouse moved event.
     */
    public boolean mouseMoved(Glyph glyph, AffineTransform t, MouseEvent e) {
	return false;
    }

    /**
     * Handles a glyph entered event.
     */
    public boolean glyphEntered(Glyph glyph, AffineTransform t) {
	return false;
    }

    /**
     * Handles a glyph exited event.
     */
    public boolean glyphExited(Glyph glyph, AffineTransform t) {
	return false;
    }

    /**
     * Handles a drag over event.
     */
    public boolean dragOver(Glyph glyph, AffineTransform t, DropTargetDragEvent e) {
	return false;
    }

    /**
     * Handles a drop event.
     */
    public boolean drop(Glyph glyph, AffineTransform t, DropTargetDropEvent e) {
	return false;
    }
}



