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
import vizard.genomics.glyph.GenomicGlyph;
import vizard.glyph.VerticalPacker;

public class GeneGlyph extends GBGenomicGlyph {
   private VerticalPacker packer = new VerticalPacker();

   public GeneGlyph(GBGlyphFactory glyphFactory, GeometricAlignment alignment) {
      super(glyphFactory, alignment);
      addChild(packer);
      loadProperties();
   }

   public int genomicChildCount() {
      return packer.packedChildCount();
   }

   public GBGenomicGlyph genomicChild(int i) {
      return (GBGenomicGlyph) packer.packedChildAt(i);
   }

   public void setExpanded(boolean isExpanded) {
      if (isExpanded)
         packer.expand();
      else
         packer.collapse();
   }

   public void handleBoundsChange() {
      if (packer.height() != height) {
         repaint();
         height = packer.height();
         repaint();
         boundsChanged();
      }
   }

   public void addGenomicChild(GenomicGlyph child) {
      packer.packChild(child);
   }

   public void removeGenomicChild(GenomicGlyph child) {
      packer.unpackChild(child);
   }

   protected void loadProperties() {
      double savedHeight = height;
      super.loadProperties();
      height = savedHeight;

      handleBoundsChange();
   }
}
