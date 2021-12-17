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

import vizard.Glyph;
import vizard.Interactor;

import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;


/**
 * @todo doc
 */
public class ClickInteractor extends Interactor
{
    private Adapter adapter;
    private Glyph glyph;
    private AffineTransform transform;
    private MouseEvent event;

    public static interface Adapter
    {
	void clicked(ClickInteractor itor);
    }

    public boolean activatedOnButtonRelease = true;
    public boolean activeWithLeftButton = true;
    public boolean activeWithShift = false;
    public boolean activeWithControl = false;
    public boolean activeWithAlt = false;

    public ClickInteractor(Adapter adapter) {
	this.adapter = adapter;
    }

    public Glyph glyph() { return glyph; }
    public AffineTransform transform() { return transform; }
    public MouseEvent event() { return event; }


    public boolean mousePressed(Glyph g, AffineTransform t, MouseEvent e) {
        if (activatedOnButtonRelease)
            return false;
        return commonStart(g, t, e);
    }

    public boolean mouseClicked(Glyph g, AffineTransform t, MouseEvent e) {
        if (!activatedOnButtonRelease)
            return false;
        return commonStart(g, t, e);
    }

    private boolean commonStart(Glyph g, AffineTransform t, MouseEvent e) {
	int m = e.getModifiers();

	if (activeWithLeftButton & (m & e.BUTTON1_MASK) == 0)
	    return false;
	if (!activeWithLeftButton & (m & e.BUTTON3_MASK) == 0)
	    return false;
	if (activeWithShift & (m & e.SHIFT_MASK) == 0)
	    return false;
	if (!activeWithShift & (m & e.SHIFT_MASK) != 0)
	    return false;
	if (activeWithControl & (m & e.CTRL_MASK) == 0)
	    return false;
	if (!activeWithControl & (m & e.CTRL_MASK) != 0)
	    return false;
	if (activeWithAlt & (m & e.ALT_MASK) == 0)
	    return false;
	if (!activeWithAlt & (m & e.ALT_MASK) != 0)
	    return false;

	glyph = g;
	transform = t;
	event = e;

	adapter.clicked(this);

	return true;
    }
}
