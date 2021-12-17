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
package vizard.component;

import vizard.model.WorldViewModel;

import javax.swing.*;
import java.awt.*;


/**
 * A slider that sets and observs a WorldViewModel.
 */
public class GSlider extends JSlider
    implements WorldViewModel.Observer
{
    public GSlider(WorldViewModel model) {
	super(JSlider.VERTICAL);
	model.observers.addObserver(this);
	modelChanged(model);
	setModel(model.zoomModel());
    }

    public void zoomCenterChanged(WorldViewModel model) {}

    public void modelChanged(WorldViewModel model) {
	BoundedRangeModel m = model.zoomModel();
	setValue(m.getValue());
	setExtent(0);
	setMinimum(m.getMinimum());
	setMaximum(m.getMaximum());
	//@todo set tick spacing
    }

    /**
     * This is a hack to fix swing bug in JSlider, for some reason the component
     * doesnot recompute its geometry every time it resizes. This will force
     * that.
     */

    private boolean firstTime = true;
    protected void paintComponent(Graphics g) {
      if (firstTime) {
        firstTime = false;
        int saved = getOrientation();
        setOrientation((saved == JSlider.HORIZONTAL) ? JSlider.VERTICAL : JSlider.HORIZONTAL);
        setOrientation(saved);
      }
      super.paintComponent(g);

    }
}
