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
package vizard.genomics.glyph;

import vizard.ParentGlyph;
import vizard.glyph.TranslationGlyph;
import vizard.model.WorldViewModel;
import vizard.util.Assert;


/**
 * The purpose of the TierNamesColumnGlyph is to provide the visualization
 * of a list of tier names.
 *
 * A list of tier names does not make much sense without a corresponding list
 * of tiers. Therefore, the TierNameListGlyph constructor requires a
 * TiersColumnGlyph.
 * The name creations, destructions, locations, and heights are _automatically_
 * synchronized with the corresponding TiersColumnGlyph tiers.
 */
public class TierNamesColumnGlyph extends ParentGlyph
{
    private TiersColumnGlyph tiersColumn;
    private WorldViewModel horizontalModel;

    /**
     * Initialize the TierNamesColumnGlyph with the given TiersColumnGlyph.
     *
     * The name creations, destructions, locations, and heights are _automatically_
     * synchronized with the corresponding TiersColumnGlyph tiers.
     */
    public TierNamesColumnGlyph(TiersColumnGlyph tiersColumn,
                                WorldViewModel horizontalModel)
    {
        this.horizontalModel = horizontalModel;
	this.tiersColumn = tiersColumn;
	tiersColumn.setTierNamesColumn(this);
	somethingChangedFrom(0);
    }

    /**
     * Return the tierGlyph corresponding to the given tierNameGlyph.
     */
    public TierGlyph correspondingTierGlyph(TierNameGlyph tierNameGlyph) {
        for(int i = childCount()-1; i >= 0; --i) {
            if (null!=child(i) && ((ParentGlyph)child(i)).child(0) == tierNameGlyph && null!=tiersColumn)
                return tiersColumn.tier(i);
        }
        Assert.vAssert(false);
        return null;
    }

    /**
     * Return the tierGlyph corresponding to the given tierNameGlyph.
     */
    public TierNameGlyph correspondingTierNameGlyph(TierGlyph tierGlyph) {
        for(int i = 0; i < tiersColumn.tierCount(); i++) {
            if (tiersColumn.tier(i).equals(tierGlyph)) {
//              System.out.println(tiersColumn.tier(i).name()+"=="+tierGlyph.name());
//              System.out.println("Returning TierNameGlyph "+((TierNameGlyph)((ParentGlyph)child(i)).child(0)).string());
              return (TierNameGlyph)((ParentGlyph)child(i)).child(0);
            }
        }
//        System.out.println("Found no match for "+tierGlyph.name());
        Assert.vAssert(false);
        return null;
    }

    //Synchronization method.
    //Called by tiersColumn whenever required.
    void somethingChangedFrom(int index) {
	int tiersCount = tiersColumn.tierCount();
	int tierNamesCount = childCount();

	for(; index < tiersCount; ++index) {
	    String name = tiersColumn.tier(index).name();
	    double y = tiersColumn.yForIndex(index);
	    double height = tiersColumn.tier(index).tierBounds().height;
	    if (index >= tierNamesCount)
		addName(name, y, height);
	    else
		resetName(index, name, y, height);
	}

	while (childCount() > tiersCount)
	    if (null!=lastChild()) {
	    	lastChild().delete();
	    } 
    }

    private void addName(String name, double y, double height) {
	TranslationGlyph tr = new TranslationGlyph.Concrete(0, y);
	tr.addChild(new TierNameGlyph(name, height));
	if (childCount() > 0)
	    tr.addChild(new TierSeparationBarGlyph(horizontalModel));

	addChild(tr);
    }

    private void resetName(int i, String name, double y, double height) {
	TranslationGlyph.Concrete tr = (TranslationGlyph.Concrete)child(i);
	tr.setTranslation(0, y);
	TierNameGlyph nameGlyph = (TierNameGlyph)tr.child(0);
	nameGlyph.reset(name, height);
    }
}
