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
package vizard.interactor;

import vizard.EventDispatcher;
import vizard.Glyph;
import vizard.Interactor;

import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;


/**
 * An interactor to move glyphs by following the mouse.
 *
 * The interactor is started by a left-button-press, and stopped
 * with a button-release.
 *
 * @todo cancel the interaction if the user hits the ESC key.
 */
public class MotionInteractor extends Interactor
{
    private Adapter adapter;
    private boolean started; //a tiny state machine
    private Glyph glyph;
    private Glyph currentGlyph;
    private AffineTransform transform;
    private Point2D.Double pStart = new Point2D.Double();
    private Point2D.Double pCurrent = new Point2D.Double();
    private Point2D.Double pPrevious = new Point2D.Double();
    private MouseEvent event;


    public static interface Adapter
    {
	void motionStarted(MotionInteractor itor);
	void move(MotionInteractor itor);
	void motionStopped(MotionInteractor itor);
	void motionCancelled(MotionInteractor itor);
    }

    public boolean startWithLeftButton = true;
    public boolean startWithShift = false;
    public boolean startWithControl = false;
    public boolean startWithAlt = false;

    public MotionInteractor(Adapter adapter) {
	this.adapter = adapter;
    }

    public Point2D startingLocation() { return pStart; }
    public Point2D currentLocation() { return pCurrent; }
    public Point2D previousLocation() { return pPrevious; }
    public Glyph glyph() { return glyph; }
    public Glyph currentGlyph() { return currentGlyph; }
    public AffineTransform transform() { return transform; }
    public boolean isStarted() { return started; }
    public MouseEvent event() { return event; }

    /**
     * A left-button-press starts the interaction and grabs all events.
     */
    public boolean mousePressed(Glyph g, AffineTransform t, MouseEvent e) {
	if (started) {
            started = false;
            EventDispatcher.instance.ungrab(this);
            adapter.motionCancelled(this);

	    return false;
        }

	int m = e.getModifiers();
	if (startWithLeftButton & (m & e.BUTTON1_MASK) == 0)
	    return false;
	if (!startWithLeftButton & (m & e.BUTTON3_MASK) == 0)
	    return false;
	if (startWithShift & (m & e.SHIFT_MASK) == 0)
	    return false;
	if (!startWithShift & (m & e.SHIFT_MASK) != 0)
	    return false;
	if (startWithControl & (m & e.CTRL_MASK) == 0)
	    return false;
	if (!startWithControl & (m & e.CTRL_MASK) != 0)
	    return false;
	if (startWithAlt & (m & e.ALT_MASK) == 0)
	    return false;
	if (!startWithAlt & (m & e.ALT_MASK) != 0)
	    return false;

	started = true;

	currentGlyph = glyph = g;
	transform = t;
	event = e;
	pStart.setLocation(e.getX(), e.getY());
	try { transform.inverseTransform(pStart, pStart); }
	catch(NoninvertibleTransformException ex) {}

	pCurrent.setLocation(pStart);
	pPrevious.setLocation(pStart);

	adapter.motionStarted(this);

	EventDispatcher.instance.grab(this);
	return true;
    }

    /**
     * Generate calls to translate.
     */
    public boolean mouseDragged(Glyph g, AffineTransform unusedT, MouseEvent e) {
	if (!started)
	    return false;
        currentGlyph = g;
	event = e;

	pPrevious.setLocation(pCurrent);
	pCurrent.setLocation(e.getX(), e.getY());
	try { transform.inverseTransform(pCurrent, pCurrent); }
	catch(NoninvertibleTransformException ex) {}

	adapter.move(this);

	return true;
    }

    /**
     * Stops the interaction.
     */
    public boolean mouseReleased(Glyph g, AffineTransform t, MouseEvent e) {
	if (!started)
	    return false;
        currentGlyph = g;
	event = e;
        started = false;
        EventDispatcher.instance.ungrab(this);

        adapter.motionStopped(this);

	return true;
    }
}
