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

import vizard.genomics.model.FeatureAdapter;
import vizard.glyph.FastRectGlyph;
import java.awt.Color;


/**
 * The purpose of FeaturePainter is to provide a standard rendering
 * for a generic genomic feature.
 *
 * A GenomicGlyph that represents a feature must create a new FeaturePainter
 * child.
 *
 * If an application prefers a different rendering for a generic feature,
 * it must add as a child the painter glyph of its choice.
 */
public class FeaturePainter extends FastRectGlyph
{
    protected FeatureAdapter feature;

    public FeaturePainter(FeatureAdapter feature) {
	this.feature = feature;
    }

    /**
     * Glyph specialization.
     */
    public double x() {
	return feature.start();
    }

    /**
     * Glyph specialization.
     */
    public double y() {
	return 0;
    }

    /**
     * Glyph specialization.
     */
    public double width() {
	return feature.end() - feature.start();
    }

    /**
     * Glyph specialization.
     */
    public double height() {
	return feature.height();
    }

    /**
     * Glyph specialization.
     */
    public Color backgroundColor() {
	return feature.color();
    }
}
