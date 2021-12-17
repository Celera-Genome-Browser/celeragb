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
package client.gui.components.annotation.axis_annotation;

import vizard.GraphicContext;
import vizard.genomics.glyph.SequencePainter;
import vizard.genomics.model.SequenceAdapter;
import vizard.glyph.TranslationGlyph;
import vizard.model.WorldViewModel;

import java.awt.*;


public class SequenceGlyph extends TranslationGlyph
    implements SequenceAdapter,
               WorldViewModel.Observer
{
    private GenomicAxisAnnotationView view;
    private SequenceAdapter.SequenceReadyHandler handler;
    private int sequenceStart;
    private String sequence = "";
    private int requestedSequenceStart;
    private boolean requestActive;
    private boolean requestChanged;
    private int RADIUS = 500;
    private boolean isForward;

    public SequenceGlyph(GenomicAxisAnnotationView view, boolean isForward) {
        this.view = view;
        this.isForward = isForward;
	addChild(new SequencePainter(this));

        view.axisModel().observers.addObserver(this);
    }

    public void delete() {
        if (this == view.sequenceGlyph)
            view.sequenceGlyph = null;
        else if (this == view.reverseSequenceGlyph)
            view.reverseSequenceGlyph = null;
        view.axisModel().observers.removeObserver(this);
        super.delete();
    }

    public boolean isForward() { return isForward; }

    public static double maxScaleForDrawings() {
        return 1.0 / SequencePainter.MIN_PIXEL_SIZE;
    }

    //SequenceAdapter specialization

    public void getSequence(int start, int end, SequenceAdapter.SequenceReadyHandler handler) {
        this.handler = handler;
        if (start < 0)
            start = 0;
        if (end >= view.getMasterAxis().getMagnitude())
            end = view.getMasterAxis().getMagnitude();
        if (start >= end)
            return;

        if (start >= sequenceStart && end <= sequenceStart + sequence.length())
            handler.handleNow(sequence.substring(start - sequenceStart, end - sequenceStart),
			      start);
        else if (requestActive) {
            requestedSequenceStart = start;
            requestChanged = true;
        }
        else {
            requestActive = true;
            requestChanged = false;
	    view.getSequence(start - RADIUS, start + RADIUS);
	}
    }

    public void sequenceReady(int start, String seq) {
        if (handler == null)
            return;
	sequenceStart = start;
	sequence = seq;
        requestActive = false;
        handler.handleLater(seq, start);
        if (requestChanged) {
            requestChanged = false;
            requestActive = true;
            view.getSequence(requestedSequenceStart - RADIUS, requestedSequenceStart + RADIUS);
        }
    }

    public Color color() { return Color.white; }
    public int start() { return 0; }
    public int end() { return view.getMasterAxis().getMagnitude(); }
    public double height() { return 10; }

    // Glyph specialization
    public double tx() { return 0; }
    public double ty() { return 0; }

    //WorldViewModel observer
    public void zoomCenterChanged(WorldViewModel model) {}
    public void modelChanged(WorldViewModel model) {
        boundsChanged();
    }

    //to help debugging
    public void paint(GraphicContext gc) {
        super.paint(gc);
    }
}
