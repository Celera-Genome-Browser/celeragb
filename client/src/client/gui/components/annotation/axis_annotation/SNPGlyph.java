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
import api.entity_model.model.annotation.PolyMorphism;
import client.gui.other.widget.SnpColors;
import vizard.genomics.glyph.SNPPainter;
import vizard.genomics.model.SNPAdapter;

import java.awt.*;


public class SNPGlyph extends GBGenomicGlyph
    implements SNPAdapter
{
    private Color bodyColor;
    private Color headColor;
    private int kind;

    public SNPGlyph(GBGlyphFactory glyphFactory, GeometricAlignment alignment) {
   super(glyphFactory, alignment);
   loadProperties();
   addChild(new SNPPainter(this));
    }

    public PolyMorphism snp() {
   return (PolyMorphism)alignment.getEntity();
    }

    protected void loadProperties() {
   super.loadProperties();

   bodyColor = SnpColors.getSnpColors().getSnpHeadColor(snp().getValidationStatus());
   headColor = SnpColors.getSnpColors().getSnpHeadColor(snp().getHighestRankingFunctionalDomain());
   String polyType = snp().getPolyMutationType();

   if (polyType.equals(PolyMorphism.PolyMutationType.SUBSTITUTION))
       kind = SUBSTITUTION;
   else if (polyType.equals(PolyMorphism.PolyMutationType.INSERTION))
       kind = INSERTION;
   else
       kind = DELETION;

        height = 16;
    }

    public Color color() { return bodyColor; }
    public Color headColor() { return headColor; }
    public int kind() { return kind; }
}

