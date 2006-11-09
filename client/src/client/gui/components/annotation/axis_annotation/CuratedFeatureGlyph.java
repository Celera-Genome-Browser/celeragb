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
package client.gui.components.annotation.axis_annotation;

import api.entity_model.access.observer.FeatureObserverAdapter;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.CuratedFeature;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.annotation.CuratedCodon;
import api.entity_model.model.annotation.CuratedExon;
import api.entity_model.model.annotation.CuratedTranscript;
import api.stub.data.ReplacementRelationship;
import api.stub.geometry.Range;
import client.gui.framework.view_pref_mgr.ViewPrefMgr;
import vizard.util.Assert;
import java.awt.Color;


public class CuratedFeatureGlyph extends FeatureGlyph
{
    private static final Color COLOR_OBSOLETE = Color.lightGray;

    private FeatureObserverAdapter curatedFeatureObserver = new FeatureObserverAdapter() {
            public void noteWorkspaceReplacementStateChanged(Feature f, ReplacementRelationship r) {}
            public void notePromotedReplacementStateChanged(Feature f, ReplacementRelationship r) {
                promotedReplacementStateChanged(f, r);
            }};


    //--------------------------------------------------------------------------------------

    public CuratedFeatureGlyph(GBGlyphFactory glyphFactory, GeometricAlignment alignment) {
	super(glyphFactory, alignment, false);
	loadProperties();
    }

    public CuratedFeature curatedFeature() {
	return (CuratedFeature)alignment.getEntity();
    }

    public void delete() {
	curatedFeature().removeFeatureObserver(curatedFeatureObserver);
	super.delete();
    }


    //--------------------------------------------------------------------------------------

    protected void loadProperties() {
	super.loadProperties();

	if (curatedFeature() instanceof CuratedCodon)
	    determineCuratedCodonRange();

	curatedFeature().removeFeatureObserver(curatedFeatureObserver);
	curatedFeature().addFeatureObserver(curatedFeatureObserver);
    }

    private void promotedReplacementStateChanged(Feature feature, ReplacementRelationship replacement) {
	if (Assert.debug) Assert.vAssert(feature == alignment.getEntity());

	Color color;
        if (replacement.isReplacementType(ReplacementRelationship.OBSOLETE))
            color = COLOR_OBSOLETE;
        else {
            color = ViewPrefMgr.getViewPrefMgr().getColorForEntity(feature);
	    if (replacement.isReplacementType(ReplacementRelationship.MODIFIED)
		|| replacement.isReplacementType(ReplacementRelationship.MERGE)
		|| replacement.isReplacementType(ReplacementRelationship.SPLIT))
	    {
		color = color.brighter();
	    }
	}

	setColor(color);
    }

    private void determineCuratedCodonRange() {
	if (Assert.debug) Assert.vAssert(curatedFeature() instanceof CuratedCodon);

	//figure out the orientation from the parent transcript
	CuratedCodon codon = (CuratedCodon)curatedFeature();
	CuratedTranscript transcript = codon.getHostTranscript();
	if (transcript != null) {
	    Range codonRange = alignment.getRangeOnAxis();
	    CuratedExon targetExon = codon.isStartCodon()
		? transcript.getExonForStartCodon()
	        : transcript.getExonForStopCodon();

	    // Calculate the overlap of codon and exon to get the codon glyph width.
            Range intersectedRange = codonRange;
            if (targetExon != null) {
                Range exonRange = ((GeometricAlignment)targetExon.getOnlyAlignmentToOnlyAxis()).getRangeOnAxis();
                Range pseudoRange = new Range(codonRange.getStart(), 3, codonRange.getOrientation());
                intersectedRange = exonRange.intersection(exonRange, pseudoRange);
            }

	    checkReverseComplementAndSetRange(intersectedRange);
	}
    }
}
