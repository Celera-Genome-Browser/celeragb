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
package vizard.glyph;

import vizard.model.WorldViewModel;

import java.awt.geom.AffineTransform;


/**
 * @todo doc
 */
public class WorldViewTransformGlyph extends TransformGlyph.Concrete
    implements WorldViewModel.Observer
{
    private WorldViewModel horizontalModel;
    private WorldViewModel verticalModel;

    public WorldViewTransformGlyph(WorldViewModel horizontalModel,
				   WorldViewModel verticalModel)
    {
	this.horizontalModel = horizontalModel;
	this.verticalModel = verticalModel;

	horizontalModel.observers.addObserver(this);
	verticalModel.observers.addObserver(this);

	resetTransform();
    }

    public void delete() {
        super.delete();
	horizontalModel.observers.removeObserver(this);
	verticalModel.observers.removeObserver(this);
    }


    //Glyph specialization
    public AffineTransform transform() { return transform; }

    //WorldViewModel observer

    public void zoomCenterChanged(WorldViewModel model) {}

    public void modelChanged(WorldViewModel model) {
	resetTransform();
    }

    private void resetTransform() {
	//no repaint on purpose: changes in the way the world is viewed
	//impact every glyph. Better to let the glyph-component take care
	//of it.

	transform.setToIdentity();
	transform.scale(1 / horizontalModel.scale(),
			1 / verticalModel.scale());
	transform.translate(- horizontalModel.origin(),
			    - verticalModel.origin());
    }
}
