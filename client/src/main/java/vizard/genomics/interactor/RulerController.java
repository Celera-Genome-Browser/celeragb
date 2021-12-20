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
import vizard.genomics.glyph.AxisRulerGlyph;
import vizard.genomics.model.GenomicAxisViewModel;
import vizard.interactor.MotionInteractor;


public class RulerController
    implements MotionInteractor.Adapter
{
    GenomicAxisViewModel viewModel;

    public RulerController(AxisRulerGlyph ruler,
			   GenomicAxisViewModel viewModel)
    {
	this.viewModel = viewModel;
	EventDispatcher.instance.addInteractor(ruler, new MotionInteractor(this));
    }

    public void motionStarted(MotionInteractor itor) {}
    public void motionStopped(MotionInteractor itor) {}
    public void motionCancelled(MotionInteractor itor) {}

    public void move(MotionInteractor itor) {
	double tx = itor.currentLocation().getX() - itor.previousLocation().getX();
	viewModel.setView(viewModel.origin() - tx,
			  viewModel.scale());
    }
}
