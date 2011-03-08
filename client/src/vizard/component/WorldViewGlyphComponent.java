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

import vizard.glyph.WorldViewTransformGlyph;
import vizard.model.WorldViewModel;

import java.awt.*;


/**
 * The purpose of the WorldViewGlyphComponent is to provide a component
 * that displays a rectangular view of a WorldViewModel. The view can be scrolled
 * and zoomed.
 */
public class WorldViewGlyphComponent extends GlyphComponent
    implements WorldViewModel.Observer
{
    private WorldViewModel xModel;
    private WorldViewModel yModel;
    private WorldViewTransformGlyph viewTransform;

    /**
     * Initialize a new WorldViewGlyphComponent with the given models.
     *
     * If either model is null, the component will create it.
     */
    public WorldViewGlyphComponent(WorldViewModel xModel,
				   WorldViewModel yModel)
    {
	if (xModel == null)
	    xModel = new WorldViewModel(true, 0, 800, 1, 0.001);
	if (yModel == null)
	    yModel = new WorldViewModel(false, 0, 600, 1, 1.0);

	this.xModel = xModel;
	this.yModel = yModel;

	xModel.observers.addObserver(this);
	yModel.observers.addObserver(this);

	xModel.setWindow(this);
	yModel.setWindow(this);

	//debugging phase: the black background helps in seeing
	//the extent of the component
	setBackground(Color.black);

	viewTransform = new WorldViewTransformGlyph(xModel, yModel);
	rootGlyph().addChild(viewTransform);

	modelChanged(null);
    }

    public WorldViewModel horizontalModel() {
	return xModel;
    }

    public WorldViewModel verticalModel() {
	return yModel;
    }

    public WorldViewTransformGlyph viewTransform() {
	return viewTransform;
    }

    //WorldViewModel observer
    public void zoomCenterChanged(WorldViewModel model) {}

    public void modelChanged(WorldViewModel unused) {
	if (getParent() != null)
	    getParent().repaint();
    }
}
