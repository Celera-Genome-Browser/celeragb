// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package vizard.genomics.glyph;

import vizard.GraphicContext;
import vizard.MultiplexerGlyph;
import vizard.ParentGlyph;
import vizard.PickedList;
import vizard.glyph.FastLineGlyph;
import vizard.model.WorldViewModel;

import java.awt.*;
import java.util.ArrayList;


public class VerticalZoomBarGlyph extends MultiplexerGlyph
    implements WorldViewModel.Observer
{
    public static final boolean ALWAYS_VISIBLE = true;

    //@todo preferences
    public static Color COLOR = Color.lightGray;

    public WorldViewModel axisViewModel;
    ArrayList viewerParents = new ArrayList();
    long zoomCenter;

    public VerticalZoomBarGlyph(WorldViewModel axisViewModel) {
	this.axisViewModel = axisViewModel;
	zoomCenter = (long)axisViewModel.zoomCenter();

	addChild(new FastLineGlyph() {
		public double x1() { return zoomCenter; }
		public double y1() { return -400000; }
		public double x2() { return zoomCenter; }
		public double y2() { return 400000; }
		public Color color() { return COLOR; }
                public PickedList pick(GraphicContext gc, Rectangle r) { return null; }
	    });

	axisViewModel.observers.addObserver(this);
    }

    public void delete() {
	axisViewModel.observers.removeObserver(this);
        super.delete();
    }

    public void showUnder(ParentGlyph parent) {
	viewerParents.add(parent);
        if (ALWAYS_VISIBLE)
            parent.addChild(this);
    }

    public void hideFrom(ParentGlyph parent) {
	viewerParents.remove(parent);
        if (ALWAYS_VISIBLE)
            parent.removeChild(this);
    }

    public void show() {
        if (ALWAYS_VISIBLE)
            return;

	int count = viewerParents.size();
	for(int i = 0; i < count; ++i) {
	    ParentGlyph p = (ParentGlyph)viewerParents.get(i);
	    p.addChild(this);
	}
    }

    public void hide() {
        if (ALWAYS_VISIBLE)
            return;

        int count = viewerParents.size();
        for(int i = 0; i < count; ++i) {
            ParentGlyph p = (ParentGlyph)viewerParents.get(i);
            p.removeChild(this);
        }
    }


    //axis-view-model observer

    public void modelChanged(WorldViewModel m) {}

    public void zoomCenterChanged(WorldViewModel m) {
	if (m.zoomCenter() != zoomCenter) {
	    repaint();
	    zoomCenter = (long)m.zoomCenter();
	    repaint();
	}
    }
}

