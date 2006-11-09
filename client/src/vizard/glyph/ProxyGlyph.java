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

import vizard.Bounds;
import vizard.Glyph;
import vizard.GraphicContext;
import vizard.PickedList;

import java.awt.Rectangle;


/**
 * The purpose of the ProxyGlyph is to be the proxy of another glyph.
 */
public class ProxyGlyph extends Glyph
{
    Glyph delegate;

    /**
     * Initialize a new ProxyGlyph with the given delegate.
     */
    public ProxyGlyph(Glyph delegateGlyph) {
	delegate = delegateGlyph;
    }

    /**
     * Delegate paints.
     */
    public void paint(GraphicContext gc) {
	delegate.paint(gc);
    }

    /**
     * Delegate addBounds.
     */
    public void addBounds(Bounds bounds) {
	delegate.addBounds(bounds);
    }

    /**
     * delegate pick.
     */
    public PickedList pick(GraphicContext gc, Rectangle deviceRect) {
	return delegate.pick(gc, deviceRect);
    }
}
