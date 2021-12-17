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
package vizard.genomics.glyph;

import vizard.GraphicContext;
import vizard.MultiplexerGlyph;
import vizard.genomics.model.GenomicAxisViewModel;
import vizard.glyph.HRulerGlyph;
import vizard.model.WorldViewModel;

import java.awt.*;


/**
 *
 */
public class AxisRulerGlyph extends GenomicGlyph
    implements WorldViewModel.Observer
{
    //@todo preferences
    public static int HEIGHT = 23;
    public static Color SELECTED_RANGE_COLOR = Color.red;

    private int selectionStart = -1;
    private int selectionEnd = -1;
    private GenomicAxisViewModel axisViewModel;

    public void setSelectedRange(int start, int end) {
	repaint();
	selectionStart = start;
	selectionEnd = end;
	repaint();
    }

    public int selectionStart() {
	return selectionStart();
    }

    public int selectionEnd() {
	return selectionEnd;
    }

    public int start() { return (int)axisViewModel.origin(); }
    public int end() { return (int)axisViewModel.viewEnd(); }

    public double x() { return start(); }
    public double y() { return 0; }
    public double width() { return end() - start(); }
    public double height() { return HEIGHT; }

    public AxisRulerGlyph(final GenomicAxisViewModel axisViewModel) {
	this.axisViewModel = axisViewModel;
	axisViewModel.observers.addObserver(this);

        MultiplexerGlyph multiplexer = new MultiplexerGlyph();
        addChild(multiplexer);
	multiplexer.addChild(new HRulerGlyph() {
		public int length() { return end() + 1; }
		public double height() { return AxisRulerGlyph.this.height(); }
		public double y() { return 0; }
		public void paint(GraphicContext gc) {
		    super.paint(gc);
		    if (selectionEnd >= selectionStart) {
			gc.setColor(SELECTED_RANGE_COLOR);
			AxisRulerGlyph a = AxisRulerGlyph.this;
			gc.fillRect(selectionStart, (int)(HRulerGlyph.K_TICS.intValue()/100. * a.height()),
				    selectionEnd - selectionStart,
				    (int)(HRulerGlyph.K_SHADE.intValue()/100. * a.height()));
		    }
		}
	    });
    }

    public MultiplexerGlyph multiplexer() {
        return (MultiplexerGlyph)child(0);
    }

    public HRulerGlyph ruler() {
	return (HRulerGlyph)multiplexer().child(0);
    }

    //WorldViewModel observer
    public void zoomCenterChanged(WorldViewModel model) {}
    public void modelChanged(WorldViewModel model) {
        repaint();
        boundsChanged();
    }

    //debug
    public boolean intersectsDirtyArea(GraphicContext gc) {
        return super.intersectsDirtyArea(gc);
    }
}
