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

import api.entity_model.model.fundtype.GenomicEntity;
import api.stub.geometry.Range;
import client.gui.framework.session_mgr.BrowserModelListener;
import vizard.EventDispatcher;
import vizard.Glyph;
import vizard.GraphicContext;
import vizard.glyph.FastRectGlyph;
import vizard.glyph.HRulerGlyph;
import vizard.interactor.EnterLeaveInteractor;
import vizard.interactor.MotionInteractor;

import java.awt.*;

public class ShadowBoxController extends Controller
    implements MotionInteractor.Adapter,
               EnterLeaveInteractor.Adapter,
	       BrowserModelListener
{
    //@todo preferences
    public static Color SHADOW_COLOR = new Color(0, 0, 255, 85);
    public static Color HIGHLIGHT_COLOR = Color.blue;

    private FastRectGlyph shadow;
    private double originalShadowStart;
    private MotionInteractor interactor;
    private EnterLeaveInteractor enterLeaveInteractor;
    private HRulerGlyph ruler;
    private boolean isStarted;
    private GenomicEntity masterEditorEntity;
    private boolean isHighlighted;
    private int shadowStart;
    private int shadowWidth = -1;

    public ShadowBoxController(final GenomicAxisAnnotationView view)
    {
	super(view);
	ruler = view.axisRulerGlyph().ruler();
        this.masterEditorEntity = view.getBrowserModel().getMasterEditorEntity();
        interactor = new MotionInteractor(this);
        enterLeaveInteractor = new EnterLeaveInteractor(this);

	shadow = new FastRectGlyph() {
		public double x() { return shadowStart; }
		public double y() { return ruler.y(); }
		public double width() { return shadowWidth; }
		public double height() { return HRulerGlyph.K_TICS.intValue()/100. * ruler.height(); }
		public Color backgroundColor() { return (isHighlighted || isStarted) ? HIGHLIGHT_COLOR : SHADOW_COLOR; }
		public Color outlineColor() { return (isHighlighted || isStarted) ? HIGHLIGHT_COLOR : SHADOW_COLOR; }
		public void paint(GraphicContext gc) {
		    if (shadowWidth >= 0)
			super.paint(gc);
		}
	    };

	ruler.parent().addChild(shadow);

	EventDispatcher.instance.addInteractor(shadow.getClass(), interactor,
            new EventDispatcher.Filter() {
              public boolean isValid(Glyph glyph) {
                if (glyph == null || glyph.getRootGlyph()==null) return false;
                Component comp = (Component)glyph.getRootGlyph().container();
                return EventDispatcher.hasAncestor(comp, ShadowBoxController.this.view);
              }
            });
	EventDispatcher.instance.addInteractor(shadow.getClass(), enterLeaveInteractor,
            new EventDispatcher.Filter() {
              public boolean isValid(Glyph glyph) {
                if (glyph == null || glyph.getRootGlyph()==null) return false;
                Component comp = (Component)glyph.getRootGlyph().container();
                return EventDispatcher.hasAncestor(comp, ShadowBoxController.this.view);
              }
            });

        view.getBrowserModel().addBrowserModelListener(this);
    }

    public void delete() {
	EventDispatcher.instance.removeInteractor(shadow.getClass(), interactor);
	EventDispatcher.instance.removeInteractor(shadow.getClass(), enterLeaveInteractor);
	shadow.delete();
        view.getBrowserModel().removeBrowserModelListener(this);

        super.delete();
    }


    //MotionInteractor adapter specialization

    public void motionStarted(MotionInteractor itor) {
        isStarted = true;
        originalShadowStart = shadowStart;
    }

    public void motionStopped(MotionInteractor itor) { isStarted = false; shadow.repaint(); }
    public void motionCancelled(MotionInteractor itor) { isStarted = false; shadow.repaint(); }

    public void move(MotionInteractor itor) {
	shadow.repaint();

        Range overallRange = (Range)view.getBrowserModel().getSubViewFixedRange();
        if (overallRange == null || overallRange.isNull()) {
	    shadowStart = shadowWidth = -1;
	    return;
	}

        double deltaX = itor.currentLocation().getX() - itor.startingLocation().getX();
	shadowStart = (int)(originalShadowStart + deltaX);
	if (shadowWidth > overallRange.getMagnitude())
	    shadowWidth = overallRange.getMagnitude();
	if (shadowStart < overallRange.getMinimum())
	    shadowStart = overallRange.getMinimum();
	else if (shadowStart + shadowWidth > overallRange.getMaximum())
	    shadowStart -= shadowStart + shadowWidth - overallRange.getMaximum();

	Range range = overallRange.isReversed()
	    ? new Range(shadowStart + shadowWidth, shadowStart)
	    : new Range(shadowStart, shadowStart + shadowWidth);
        view.getBrowserModel().setSubViewVisibleRange(range);

	shadowStart = range.getMinimum();
	shadowWidth = range.getMagnitude();

	shadow.repaint();
    }

    //EnterLeaveInteractor adapter specialization

    public void glyphEntered(EnterLeaveInteractor itor) {
        isHighlighted = true;
        shadow.repaint();
    }

    public void glyphExited(EnterLeaveInteractor itor) {
        isHighlighted = false;
        shadow.repaint();
    }

    //BrowserModelListener specialization
    public void browserSubViewVisibleRangeChanged(Range subViewVisibleRange) {
	shadow.repaint();
        // This means that the empty range was set and we do not want to see the glyph.
	if (subViewVisibleRange == null || (subViewVisibleRange.getMagnitude()==0 &&
            subViewVisibleRange.getStart()==0))
	    shadowStart = shadowWidth = -1;
	else {
	    shadowStart = subViewVisibleRange.getMinimum();
	    shadowWidth = subViewVisibleRange.getMagnitude();
	}
	shadow.repaint();
    }

    public void browserSubViewFixedRangeChanged(Range subViewFixedRange){}
    public void browserMasterEditorEntityChanged(GenomicEntity masterEditorEntity) {
      if (this.masterEditorEntity.equals(masterEditorEntity)) return;
      this.masterEditorEntity = masterEditorEntity;
      browserSubViewVisibleRangeChanged(new Range());
    }
    public void browserCurrentSelectionChanged(GenomicEntity newSelection) {}
    public void browserMasterEditorSelectedRangeChanged(Range masterEditorSelectedRange) {}
    public void browserClosing() {}
    public void modelPropertyChanged(Object key, Object oldValue, Object newValue){}
}
