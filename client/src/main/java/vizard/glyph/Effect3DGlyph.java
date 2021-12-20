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
import vizard.GraphicContext;
import vizard.PickedList;

import java.awt.*;


/**
 * The common superclass for glyphs providing a simple 3D effect.
 */
public abstract class Effect3DGlyph extends Glyph
{
    /**
     * Return whether the 3D effect should be raised or not.
     */
    public boolean isRaised() { return true; }

    /**
     * Return the color used for the bright side of the 3D effect.
     */
    public Color brightColor() { return Constants.bright3DColor; }

    /**
     * Return the color used for the shadow side of the 3D effect.
     */
    public Color shadowColor() { return Constants.shadow3DColor; }

    /**
     * A 3D-effect is never picked.
     */
    public PickedList pick(GraphicContext gc, Rectangle r) {
	return null;
    }
}
