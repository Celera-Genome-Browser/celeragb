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
 * The purpose of TranscriptPainter is to provide a standard rendering for
 * a transcript.
 *
 * A GenomicGlyph that represents a transcript must create a new TranscriptPainter
 * child.
 *
 * If an application prefers a different rendering for a transcript,
 * it must add as a child the painter glyph of its choice.
 */
public class TranscriptPainter extends FastRectGlyph
{
    protected FeatureAdapter transcript;
    protected boolean highlighted;
    protected static double intronHeight = 0.2;

    public TranscriptPainter(FeatureAdapter transcript) {
	this(transcript, false);
    }

    public TranscriptPainter(FeatureAdapter transcript, boolean highlighted) {
	this.transcript = transcript;
	this.highlighted = highlighted;
    }

    public FeatureAdapter transcript() {
        return transcript;
    }

    /**
     * Glyph specialization.
     */
    public double x() {
	return transcript.start();
    }

    /**
     * Glyph specialization.
     */
    public double y() {
	return transcript.height() * (1 - heightCoeff()) / 2;
    }

    /**
     * Glyph specialization.
     */
    public double width() {
	return transcript.end() - transcript.start();
    }

    /**
     * Glyph specialization.
     */
    public double height() {
	return transcript.height() * heightCoeff();
    }

    /**
     * Glyph specialization.
     */
    public Color backgroundColor() {
	return transcript.color();
    }

    private double heightCoeff() {
        return highlighted ? intronHeight : 0.1;
    }
}
