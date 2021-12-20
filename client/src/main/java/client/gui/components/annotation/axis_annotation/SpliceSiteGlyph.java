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

import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.SpliceSite;
import vizard.Bounds;
import vizard.genomics.glyph.SpliceSitePainter;
import vizard.genomics.model.SpliceSiteAdapter;


public class SpliceSiteGlyph extends GBGenomicGlyph
    implements SpliceSiteAdapter
{
    private boolean isAcceptor;

    public SpliceSiteGlyph(GBGlyphFactory glyphFactory, GeometricAlignment alignment) {
	super(glyphFactory, alignment);
        loadProperties();
	addChild(new SpliceSitePainter(this));
    }

    public SpliceSite spliceSite() {
	return (SpliceSite)alignment.getEntity();
    }

    protected void loadProperties() {
        super.loadProperties();

	height = Math.rint(spliceSite().getScore() * 33.3);
	isAcceptor = spliceSite().isAcceptorSpliceSite();
    }

    //SpliceSiteAdapter specialization
    public boolean isAcceptor() { return isAcceptor; }

    //Glyph specialization

    //@todo finding the extra-pixels should be automatic
    public void addBounds(Bounds bounds) {
        super.addBounds(bounds);
        bounds.addLeftRightPixels(SpliceSitePainter.NUM_PIXELS, SpliceSitePainter.NUM_PIXELS);
    }

    //@todo the paint method below is a very dirty optimization (because trillions of splice sites are too slow to draw)
    //      The optimization should belong to the SpliceSite tier instead.
/*    private static int lastDrawnPixelX;
    static boolean paintAlways;

    public void paint(GraphicContext gc) {
	double[] p = gc.tempDoubles();
	p[0] = start(); p[1] = y();
	gc.getTransform().transform(p, 0, p, 0, 1);
        int pixelX = (int)p[0];

        SpliceSiteGlyph first = (SpliceSiteGlyph)parent().child(0);

        if (this != first) {
            if (pixelX == lastDrawnPixelX && !paintAlways)
                return;
            lastDrawnPixelX = pixelX;
        }
        super.paint(gc);
    }*/
}
