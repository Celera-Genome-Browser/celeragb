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

import java.awt.geom.AffineTransform;


/**
 * @todo doc
 */
public class EnterLeaveInteractor extends Interactor
{
    private Adapter adapter;
    private Glyph glyph;

    public static interface Adapter
    {
	void glyphEntered(EnterLeaveInteractor itor);
        void glyphExited(EnterLeaveInteractor itor);
    }

    public EnterLeaveInteractor(Adapter adapter) {
	this.adapter = adapter;
    }

    public Glyph glyph() { return glyph; }

    /**
     *
     */
    public boolean glyphEntered(Glyph g, AffineTransform unused) {
        glyph = g;
        adapter.glyphEntered(this);
        return true;
    }

    /**
     *
     */
    public boolean glyphExited(Glyph g, AffineTransform unused) {
        glyph = g;
        adapter.glyphExited(this);
        return true;
    }
}
