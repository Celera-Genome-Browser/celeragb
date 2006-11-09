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

import vizard.util.Preferences;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.HashSet;


/**
 * The purpose of the RootGlyph is to connect a tree of glyphs to
 * a Swing component.
 *
 * The RootGlyph is a middle-man: Swing paint requests are forwarded
 * to the children glyphs, and glyphs repaint requests are forwarded
 * back to the Swing component.
 *
 * Note that a Swing component referenced by the root glyph must
 * implement the GlyphContainer interface.
 */
public class RootGlyph extends ParentGlyph
{
    //note: the "slow threshold" algorithm is disabled for now
    public static String SLOW_THRESHOLD_PREF =
	"Threshold in milliseconds when the drawings are to be considered slow";
    public static Integer SLOW_THRESHOLD = new Integer(250000); //millisec

    GlyphContainer container;
    private HashSet repaintLaterSet = new HashSet();
    private long startTime;
    private boolean isSlow;
    private boolean processingRepaintLater;
    private boolean isNeverSlow = false;
    private Rectangle repaintLaterBounds;
    private boolean gotSomeRepaintRequest;

    /**
     * Initializes a root glyph with the given glyph container.
     *
     * Note that a root-glyph is always ready.
     */
    public RootGlyph(GlyphContainer container) {
	this.container = container;
        setReady();
    }

    /**
     * Return the glyph container
     */
    public GlyphContainer container() {
	return container;
    }

    /**
     * Forwards the paint request to its children glyphs.
     */
    public void paint(Graphics2D g2d) {
	GraphicContext gc = new GraphicContext(this, g2d);
        startTime = System.currentTimeMillis();
        isSlow = false;

        isNeverSlow = false;
        if (repaintLaterBounds != null) {
            Shape clipShape = g2d.getClip();
            isNeverSlow = repaintLaterBounds.equals(clipShape.getBounds());
        }
        try {
            paint(gc);
        }
        catch(Error ex) {
            throw ex; //useful to set a breakpoint
        }
        repaintLaterBounds = null;

	if (!repaintLaterSet.isEmpty()) {
            Glyph g = (Glyph)repaintLaterSet.iterator().next();
            repaintLaterSet.remove(g);
            try {
                processingRepaintLater = true;
                g.repaint();
                ((Component)container).repaint(100,
                        repaintLaterBounds.x,
                        repaintLaterBounds.y,
                        repaintLaterBounds.width,
                        repaintLaterBounds.height);
            }
            finally {
                processingRepaintLater = false;
            }
        }

        gotSomeRepaintRequest = false;
        int loopCounterAvoidsInfinity = 0;
        final int MAX_TIMES_WITHOUT_REPAINT = 32;
        while(!activeBoundsObservers.isEmpty()) {
            if (++loopCounterAvoidsInfinity > MAX_TIMES_WITHOUT_REPAINT) {
                if (!gotSomeRepaintRequest)
                    ((Component)container).repaint();
                break;
            }
            ArrayList boundsObserversSnapshot = new ArrayList(activeBoundsObservers);
            activeBoundsObservers.clear();
            for(int i = 0; i < boundsObserversSnapshot.size(); ++i) {
                if ((Glyph.BoundsObserver)boundsObserversSnapshot.get(i)!=null) {
                	((Glyph.BoundsObserver)boundsObserversSnapshot.get(i)).handleBoundsChange();
                } 
            }
        }
    }

    public boolean isSlow() {
        if (isNeverSlow)
            return false;
        if (!isSlow)
            isSlow = System.currentTimeMillis() > startTime + SLOW_THRESHOLD.intValue();
        return isSlow;
    }

    public void repaintLater(Glyph glyph) {
        repaintLaterSet.add(glyph);
    }

    /*
     * Return the top most glyph that contains the given pixel position
     * with the given sensibility.
     *
     * The glyphs structure being a directed acyclic graph, a glyph
     * can be reached through more than one path. Thus, a glyph can
     * have more than one user-space to device-space mapping.
     * The transform parameter is set to the mapping that is valid
     * for the path that actually reached the glyph.
     */
    public PickedList pickTopGlyph(int x, int y, int sensibility)
    {
	Rectangle deviceRect = new Rectangle(x - sensibility/2,
					     y - sensibility/2,
					     sensibility,
					     sensibility);

	Graphics2D g2d = (Graphics2D)
	    ((Component)container).getGraphics();
	GraphicContext gc = new GraphicContext(this, g2d);

	return pick(gc, deviceRect);
    }

    /**
     * A root glyph is always picked.
     */
    public PickedList pick(GraphicContext gc, Rectangle deviceRect) {
	PickedList list = super.pick(gc, deviceRect);
	return (list != null) ? list : new PickedList(this, gc);
    }

    /**
     * Forward the dirty area to the Swing component.
     */
    public void inval(Bounds b) {
        final int EXTRA_DUE_TO_ADORNMENT_BUG = 5;
	int xmin = (int)(b.x - b.leftPixels - EXTRA_DUE_TO_ADORNMENT_BUG);
	int ymin = (int)(b.y - b.upPixels - EXTRA_DUE_TO_ADORNMENT_BUG);
	int xmax = 1 + (int)(b.x + b.width + b.rightPixels + EXTRA_DUE_TO_ADORNMENT_BUG);
	int ymax = 1 + (int)(b.y + b.height + b.downPixels + EXTRA_DUE_TO_ADORNMENT_BUG);
	//We add 1 pixel due to integer rounding errors.

	//We call repaint on the parent container to avoid
	//"unsynchronized repaints" (not a bug, just a bad look&feel)
	//when multiple GlyphContainers are in the same frame.
	Component comp = (Component)container;
        xmin = Math.max(xmin, 0);
        ymin = Math.max(ymin, 0);
        xmax = Math.min(xmax, comp.getWidth());
        ymax = Math.min(ymax, comp.getHeight());

        if (!gotSomeRepaintRequest)
            gotSomeRepaintRequest = xmax > xmin && ymax > ymin;

        if (!processingRepaintLater)
            comp.repaint(0, xmin, ymin,
                         xmax - xmin,
                         ymax - ymin);
        else {
            if (repaintLaterBounds == null)
                repaintLaterBounds = new Rectangle(xmin, ymin, xmax - xmin, ymax - ymin);
            else {
                if (repaintLaterBounds.x < xmin) xmin = repaintLaterBounds.x;
                if (repaintLaterBounds.y < ymin) ymin = repaintLaterBounds.y;
                int boundsXMax = repaintLaterBounds.x + repaintLaterBounds.width;
                if (boundsXMax > xmax) xmax = boundsXMax;
                int boundsYMax = repaintLaterBounds.y + repaintLaterBounds.height;
                if (boundsYMax > ymax) ymax = boundsYMax;
            }
        }
    }


    // ObjectWithPreferences specialization:

    /**
     * Notifies this glyph that some preferences have changed.
     */
    public void preferencesChanged(Preferences oldPreferences) {
    }

    /**
     * Store in the given parameter the value of
     * this glyph's preferences.
     *
     * The default implementation does not do anything.
     */
    public void getPreferences(Preferences prefs) {
	super.getPreferences(prefs);
	prefs.add(this, SLOW_THRESHOLD_PREF, SLOW_THRESHOLD);
    }

    public void handleBoundsChange() {
        inval(new Bounds(0, 0, 10000, 10000));
    }
}
