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
package vizard.genomics.component;

import vizard.MultiplexerGlyph;
import vizard.component.WorldViewGlyphComponent;
import vizard.genomics.glyph.BackgroundSensorGlyph;
import vizard.genomics.glyph.TierGlyph;
import vizard.genomics.glyph.TiersColumnGlyph;
import vizard.genomics.model.GenomicAxisViewModel;
import vizard.model.WorldViewModel;

import java.awt.*;


/**
 * The purpose of the TiersComponent class is to provide
 * a reusable swing component for viewing a single column of tiers
 *
 * A tier is a grouping of genomic entities with the purpose of
 * displaying them in the same row.
 *
 * An application is more likely to use the TierTableComponent class which,
 * in addition to the column of tiers, also displays a column of tier names.
 */
public class TiersComponent extends WorldViewGlyphComponent
{
    private TiersColumnGlyph tiersColumn;
    private MultiplexerGlyph multiplexer;
    private BackgroundSensorGlyph backgroundSensor = new BackgroundSensorGlyph();

    /**
     * Initializes the component with the given genomic-axis-view-model.
     *
     * If multiple tiers-components use the same axis-view-model, they
     * will automatically be synchronized for scrolling, zooming, etc.,
     * along the genomic axis.
     */
    public TiersComponent(GenomicAxisViewModel axisViewModel) {
	super(axisViewModel, null);
	setBackground(Color.black);

	//we want the tiers to be visible in multiple views
	// ==> we need a multiplexer parent of the viewTransform
	multiplexer = new MultiplexerGlyph(viewTransform());

	tiersColumn = new TiersColumnGlyph(axisViewModel, verticalModel());
	viewTransform().addChild(tiersColumn);

	horizontalModel().showEverything();
        verticalModel().mode = WorldViewModel.GLUE_START;

        rootGlyph().addChild(backgroundSensor);
        rootGlyph().shuffleChild(backgroundSensor, 0);
    }

    public void reset() {
	while(tiersColumn.tierCount() > 0) {
	    TierGlyph lastTier = tiersColumn.tier(tiersColumn.tierCount()-1);
	    lastTier.delete();
	}
    }

    /**
     * Return the "column of tiers" glyph that is displayed in this component.
     */
    public TiersColumnGlyph tiersColumn() {
	return tiersColumn;
    }

    /**
     * Return the multiplexer-glyph, parent of the tier-column-glyph.
     *
     * The multiplexer-glyph can have multiple parents, and therefore
     * can be used to visualize the column of tiers in some other place.
     */
    public MultiplexerGlyph multiplexer() {
	return multiplexer;
    }

    public BackgroundSensorGlyph backgroundSensor() {
        return backgroundSensor;
    }

    /**
     * Return the axis-view-model that this component uses to
     * synchronize its view along the genomic axis.
     */
    public GenomicAxisViewModel axisViewModel() {
	return (GenomicAxisViewModel)horizontalModel();
    }
}


