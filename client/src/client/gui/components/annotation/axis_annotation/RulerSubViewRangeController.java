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
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.BrowserModelListener;
import vizard.EventDispatcher;
import vizard.interactor.MotionInteractor;


public class RulerSubViewRangeController
    implements MotionInteractor.Adapter, BrowserModelListener

{
    AxisRulerSubViewRangeGlyph rangeGlyph;
    BrowserModel browserModel;
    GenomicEntity masterEditorEntity;


    public RulerSubViewRangeController(AxisRulerSubViewRangeGlyph rangeGlyph,
			    BrowserModel browserModel)
    {
	this.rangeGlyph = rangeGlyph;
	this.browserModel = browserModel;

	this.masterEditorEntity = browserModel.getMasterEditorEntity();
        rangeGlyph.setLocation(browserModel.getSubViewVisibleRange());
	browserModel.addBrowserModelListener(this);

	EventDispatcher.instance.addInteractor(rangeGlyph, new MotionInteractor(this));
    }

    public void motionStarted(MotionInteractor itor) {
    }

    public void motionStopped(MotionInteractor itor) {
    }

    public void move(MotionInteractor itor) {
      // Do nothing on mouse move.  This range can only be changed via the
      // "Set SubView Range" button.
    }

    public void motionCancelled(MotionInteractor itor) {
    }

    public void browserSubViewFixedRangeChanged(Range subViewFixedRange){
      rangeGlyph.repaint();
      // This means that the empty range was set and we do not want to see the glyph.
      if (subViewFixedRange == null || (subViewFixedRange.getMagnitude()==0 &&
          subViewFixedRange.getMinimum()==0))
          rangeGlyph.setLocation(new Range(-1, -1));
      else {
        rangeGlyph.setLocation(subViewFixedRange);
      }
      rangeGlyph.repaint();
    }

    public void browserSubViewVisibleRangeChanged(Range subViewVisibleRange){}
    public void browserMasterEditorEntityChanged(GenomicEntity masterEditorEntity){
      if (this.masterEditorEntity.equals(masterEditorEntity)) return;
      this.masterEditorEntity = masterEditorEntity;
      browserSubViewFixedRangeChanged(new Range());
    }
    public void browserCurrentSelectionChanged(GenomicEntity newSelection){}
    public void browserMasterEditorSelectedRangeChanged(Range masterEditorSelectedRange){}
    public void browserClosing(){}
    public void modelPropertyChanged(Object key, Object oldValue, Object newValue){}
}
