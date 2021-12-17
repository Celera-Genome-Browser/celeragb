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
package vizard.genomics.interactor;

import vizard.EventDispatcher;
import vizard.genomics.glyph.AxisRulerSliderGlyph;
import vizard.genomics.glyph.VerticalZoomBarGlyph;
import vizard.genomics.model.GenomicAxisViewModel;
import vizard.interactor.MotionInteractor;
import vizard.model.WorldViewModel;


public class SliderController
    implements MotionInteractor.Adapter,
	       WorldViewModel.Observer
{
    AxisRulerSliderGlyph slider;
    GenomicAxisViewModel viewModel;
    VerticalZoomBarGlyph zoomBar;

    public SliderController(AxisRulerSliderGlyph slider,
			    GenomicAxisViewModel viewModel,
                            VerticalZoomBarGlyph zoomBar)
    {
	this.slider = slider;
	this.viewModel = viewModel;
        this.zoomBar = zoomBar;

	slider.setLocation((int)viewModel.zoomCenter());
	viewModel.observers.addObserver(this);

	EventDispatcher.instance.addInteractor(slider, new MotionInteractor(this));
    }

    public void motionStarted(MotionInteractor itor) {
	zoomBar.show();
    }

    public void motionStopped(MotionInteractor itor) {
	zoomBar.hide();
    }

    public void move(MotionInteractor itor) {
	viewModel.setZoomCenter((int)itor.currentLocation().getX());
    }

    public void motionCancelled(MotionInteractor itor) {
        zoomBar.hide();
    }

    public void zoomCenterChanged(WorldViewModel unused) {
	slider.setLocation((int)viewModel.zoomCenter());
    }

    public void modelChanged(WorldViewModel unused) {}
}
