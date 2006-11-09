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
import javax.swing.JScrollBar;
import javax.swing.BoundedRangeModel;


/**
 * A scroll bar that observs a WorldViewModel.
 */
public class GScrollBar extends JScrollBar
    implements WorldViewModel.Observer
{
    public GScrollBar(WorldViewModel model) {
	this(model, VERTICAL);
    }

    public GScrollBar(WorldViewModel model, int direction) {
	super(direction);
	model.observers.addObserver(this);
	modelChanged(model);
	setModel(model.scrollModel());
    }

    public void zoomCenterChanged(WorldViewModel model) {}

    public void modelChanged(WorldViewModel model) {
	BoundedRangeModel m = model.scrollModel();
	setValues(m.getValue(), m.getExtent(),
		  m.getMinimum(), m.getMaximum());
	setBlockIncrement(m.getExtent() * 9 / 10);
	setUnitIncrement(m.getExtent() / 10);
    }
}

