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
package vizard.genomics.glyph;

import vizard.GraphicContext;
import vizard.PickedList;
import vizard.glyph.FastLineGlyph;
import vizard.model.WorldViewModel;

import java.awt.*;


public class TierSeparationBarGlyph extends FastLineGlyph
{
    //@todo properties
    public static Color COLOR = new Color(30, 30, 30);

    private WorldViewModel viewModel;

    public TierSeparationBarGlyph(WorldViewModel viewModel) {
        this.viewModel = viewModel;
    }

    //Glyph specialization
    public double x1() { return viewModel.origin(); }
    public double x2() { return viewModel.origin() + viewModel.viewSize(); }
    public double y1() { return -1 - TiersColumnGlyph.INTERSPACE / 2; }
    public double y2() { return -1 - TiersColumnGlyph.INTERSPACE / 2; }
    public Color color() { return COLOR; }
    public PickedList pick(GraphicContext gc, Rectangle rect) { return null; }

}