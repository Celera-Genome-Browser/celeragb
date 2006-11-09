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
package client.gui.components.annotation.axis_annotation;

import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.assembly.Contig;
import api.stub.geometry.Range;
import vizard.Bounds;
import vizard.genomics.glyph.ContigPainter;
import vizard.genomics.model.ContigAdapter;


public class ContigGlyph extends GBGenomicGlyph
    implements ContigAdapter
{
    public ContigGlyph(GBGlyphFactory glyphFactory, GeometricAlignment alignment) {
	super(glyphFactory, alignment);
	loadProperties();
	addChild(new ContigPainter(this));
    }

    public Contig contig() {
	return (Contig)alignment.getEntity();
    }

    public void addBounds(Bounds bounds) {
        double y = y();
        double h = height();
	bounds.add(x(), y - h/2, width(), h);
    }

    public boolean isForward() {
        Range range = alignment.getRangeOnAxis();
        return  range.isReversed() &&  glyphFactory.isReverseComplement() ||
               !range.isReversed() && !glyphFactory.isReverseComplement();
    }

    protected void loadProperties() {
        super.loadProperties();
        setY(5);
        setIsForward(isForward());
    }
}
