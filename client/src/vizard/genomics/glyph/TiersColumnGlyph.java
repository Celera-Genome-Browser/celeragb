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

import vizard.Glyph;
import vizard.ParentGlyph;
import vizard.genomics.model.GenomicAxisViewModel;
import vizard.glyph.FastRectGlyph;
import vizard.glyph.IndexedParentGlyph;
import vizard.glyph.TranslationGlyph;
import vizard.model.WorldViewModel;

import java.awt.Color;


public class TiersColumnGlyph extends ParentGlyph
{
    //@todo preferences
    public static final int INTERSPACE = 6;

    private GenomicAxisViewModel axisModel;
    private WorldViewModel verticalModel;
    private TierNamesColumnGlyph tierNamesColumn;

    public TiersColumnGlyph(GenomicAxisViewModel axisModel,
                            WorldViewModel verticalModel)
    {
        this.axisModel = axisModel;
        this.verticalModel = verticalModel;
        addChild(new ParentGlyph());
        addChild(new IndexedParentGlyph());
	notify(0);
    }

    private ParentGlyph backgroundParent() {
        return (ParentGlyph)child(0);
    }

    private IndexedParentGlyph tiersParent() {
        return (IndexedParentGlyph)child(1);
    }

    public TierNamesColumnGlyph tierNamesColumn() {
        return tierNamesColumn;
    }

    private TranslationGlyph.Concrete translation(int i) {
        return (TranslationGlyph.Concrete)tiersParent().child(i);
    }

    public int tierCount() {
        return tiersParent().childCount();
    }

    public TierGlyph tier(int index) {
        return (TierGlyph)translation(index).child(0);
    }

    public TierGlyph tierForName(String name) {
	for(int i = tierCount() - 1; i >= 0; --i) {
	    if (name.equals(tier(i).name()))
		return tier(i);
	}
	return null;
    }

    public GenomicAxisViewModel axisModel() {
        return axisModel;
    }

    public WorldViewModel verticalModel() {
        return verticalModel;
    }

    //Glyph specialization

    public void show(Glyph g) {
	addTier((TierGlyph)g, tiersParent().UNSPECIFIED);
    }

    public void hide(Glyph g) {
	removeTier((TierGlyph)g);
    }

    public void addTier(TierGlyph tier, int order) {
	TranslationGlyph tr = new TranslationGlyph.Concrete(0, yForIndex(tierCount()));
	tr.addChild(tier);
        tiersParent().addChildAtIndex(tr, order);

        final int myTierIndex = tierCount() - 1;
        backgroundParent().addChild(new FastRectGlyph() {
            private TierGlyph myTier() { return tier(myTierIndex); }
            public double x() { return myTier().x(); }
            public double y() { return yForIndex(myTierIndex) - INTERSPACE/2; }
            public double width() { return myTier().width(); }
            public double height() { return yForIndex(myTierIndex + 1) - y(); }
            public Color backgroundColor() {
                return myTier().isHighlighted ? myTier().backgroundColor.brighter() : myTier().backgroundColor;
            }
            public Color outlineColor() { return TierGlyph.SEPARATION_BAR_COLOR; }
        });

        repositionTiersFrom(0);
    }

    public void removeTier(TierGlyph tier) {
        backgroundParent().lastChild().delete();

	int i = tierIndex(tier);
        TranslationGlyph tr = translation(i);
        tr.removeChild(tier);
	tr.delete();

	repositionTiersFrom(i);
    }

    public void delete() {
      super.delete();
    }

    //Called by the TierNamesColumnGlyph
    void setTierNamesColumn(TierNamesColumnGlyph tierNamesColumn) {
	this.tierNamesColumn = tierNamesColumn;
    }

    public void handleBoundsChange() {
	repositionTiersFrom(0);
        super.handleBoundsChange();
    }

    public double height() {
	return yForIndex(tierCount());
    }

    public double yForTier(TierGlyph tier) {
	return yForIndex(tierIndex(tier));
    }

    public double yForIndex(int i) {
	double y = INTERSPACE/2;
	if (i > 0) {
	    TranslationGlyph tr = translation(i - 1);
	    TierGlyph tier = (TierGlyph)tr.child(0);
	    y = tr.ty() + tier.tierBounds().height + INTERSPACE;
	}
	return y;
    }

    public int tierIndex(TierGlyph tier) {
	return tiersParent().childIndex(tier.parent());
    }

    public int tierOrderIndex(TierGlyph tier) {
        return tiersParent().getOrderIndexOfChild(tier.parent());
    }

    public void setTierOrderIndex(TierGlyph tier, int orderIndex) {
	int childIndex = tierIndex(tier);
	ParentGlyph tierParent = (ParentGlyph)tiersParent().child(childIndex);
	int previousOrderIndex = tiersParent().getOrderIndexOfChild(tierParent);
	if (previousOrderIndex != orderIndex) {
	    tiersParent().moveChildToOrderIndex(tierParent, orderIndex);
	    repositionTiersFrom(0);
	}
    }

    private void repositionTiersFrom(int starti) {
	int count = tierCount();
	for(int i = starti; i < count; ++i) {
	    TranslationGlyph.Concrete tr = translation(i);
	    tr.setTranslation(tr.tx(), yForIndex(i));
	}

	notify(starti);
    }

    private void notify(int row) {
	if (tierNamesColumn != null)
	    tierNamesColumn.somethingChangedFrom(row);

	double worldSize = yForIndex(tierCount());
        if (worldSize > INTERSPACE/2)
            worldSize -= INTERSPACE/2;
        verticalModel.setWorld(0, worldSize);
    }
}

