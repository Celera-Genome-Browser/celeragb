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

import vizard.component.WorldViewGlyphComponent;
import vizard.genomics.glyph.TierNamesColumnGlyph;

import java.awt.Color;
import java.awt.Dimension;


/**
 * The purpose of the TierNamesComponent class is to provide
 * a reusable swing component for viewing a single column of tier names.
 *
 * A tier is a grouping of genomic entities with the purpose of
 * displaying them in the same row.
 *
 * An application is more likely to use the TierTableComponent class which,
 * in addition to the column of tier names, also displays a column of graphical tiers.
 *
 * A TierNamesComponent cannot be used without a corresponding TiersComponent.
 * The position and height of each name is automatically constrained by
 * the position and height of the corresponding TiersComponent tier.
 */
public class TierNamesComponent extends WorldViewGlyphComponent
{
    //@todo preferences
    public static int WIDTH = 120;

    private TierNamesColumnGlyph tierNamesColumn;
    private TiersComponent tiersComponent;

    /**
     * Initializes the component with the given TiersComponent.
     *
     * The position and height of each name is automatically constrained by
     * the position and height of the corresponding TiersComponent tier.
     */
    public TierNamesComponent(TiersComponent tiersComponent)
    {
	super(null, tiersComponent.verticalModel());
	setBackground(Color.darkGray);

	tierNamesColumn = new TierNamesColumnGlyph(tiersComponent.tiersColumn(),
                                                   horizontalModel());
	viewTransform().addChild(tierNamesColumn);

	setPreferredSize(new Dimension(WIDTH, 500));
    }

    /**
     * Return the "column of tier names" glyph that is displayed in this component.
     *
     * Note that the names are _automatically_ added to the column through
     * the synchronization with the tiersComponent.
     */
    public TierNamesColumnGlyph tierNamesColumn() {
	return tierNamesColumn;
    }

    /**
     * Return the tiersComponent that is used to sinchronize
     * the position and height of each name.
     */
    public TiersComponent tiersComponent() {
	return tiersComponent;
    }
}


