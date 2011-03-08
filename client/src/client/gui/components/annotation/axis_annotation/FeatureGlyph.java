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

/**
 * Title:        Feature Glyph
 * Description:  Painter object for features displayed on the main view.
 * @author JojicOn (derived from--?)
 * @version $Id$
 */

import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.ComputedCodon;
import api.entity_model.model.annotation.CuratedCodon;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.fundtype.AlignableGenomicEntity;
import api.entity_model.model.fundtype.EntityTypeSet;
import api.stub.geometry.MutableRange;
import api.stub.geometry.Range;
import vizard.Glyph;
import vizard.ParentGlyph;
import vizard.genomics.glyph.FeaturePainter;
import vizard.genomics.glyph.GenomicGlyph;
import vizard.genomics.glyph.TranscriptPainter;
import vizard.genomics.model.FeatureAdapter;

import java.util.Set;

/**
 * This class is the Axis Annotation-specific glyph for drawing all kinds of
 * features.
 */
public class FeatureGlyph extends GBGenomicGlyph implements FeatureAdapter {
   public FeatureGlyph(GBGlyphFactory glyphFactory, GeometricAlignment alignment) {
      this(glyphFactory, alignment, true);
   }

   protected FeatureGlyph(GBGlyphFactory glyphFactory, GeometricAlignment alignment, boolean doLoadProperties) {
      super(glyphFactory, alignment);
      if (doLoadProperties)
         loadProperties();

      Feature feature = (Feature) alignment.getEntity();
      if (!feature.hasSubFeatures())
         addChild(new FeaturePainter(this));
      else {
         ParentGlyph backgroundParent = new ParentGlyph();
         ParentGlyph bottomParent = new ParentGlyph();
         ParentGlyph topParent = new ParentGlyph();
         addChild(backgroundParent);
         addChild(bottomParent);
         addChild(topParent);
		 //  A child must be added for drawing the line between HSPs/Introns.
		 //  This is carried out by a background-bound painter.
		 if (!featureApplicableToVariedLinePainting(feature)) {
		    backgroundParent.addChild(new TranscriptPainter(this, false));
		 }
      }
   }

   public Feature feature() {
      return (Feature) alignment.getEntity();
   }

   public ParentGlyph bottomParent() {
      return (ParentGlyph) child(1);
   }

   public ParentGlyph topParent() {
      return (ParentGlyph) child(2);
   }

   public ParentGlyph backgroundParent() {
      return (ParentGlyph) child(0);
   }

   public void addGenomicChild(GenomicGlyph glyph) {
		if (! visualizeAdjacentHSPFor(glyph)) {
			if (shouldBeOnTop(glyph)) {	topParent().addChild(glyph); }
	 		else { bottomParent().addChild(glyph); }
      	}
      	else {
         	if (shouldBeOnTop(glyph)) { bottomParent().addChild(glyph); }
	 		else { backgroundParent().addChild(glyph); } 
      	}
   }

   public void removeGenomicChild(GenomicGlyph glyph) {
	 for(ParentGlyph p = glyph.parent();; p = p.parent()) {
		if (p == null)
		   break;
		 if (p == topParent() || p == bottomParent() || p == backgroundParent())
		 	p.removeChild(glyph);
	 }
   }

   //-----------------------------------------------------------------

   protected void loadProperties() {
      super.loadProperties();
      if (feature() instanceof ComputedCodon)
         determineComputedCodonRange();
   }

   private void determineComputedCodonRange() {
      Range range = alignment.getRangeOnAxis();
      if (glyphFactory.isReverseComplement()) {
         MutableRange m = range.toMutableRange();
         m.mirror(glyphFactory.axis().getMagnitude());
         range = m;
      }

      final int CODON_WIDTH = 3;
      if (range.isForwardOrientation()) {
         start = range.getStart();
         end = start + CODON_WIDTH;
      }
      else {
         start = range.getStart() - CODON_WIDTH;
         end = range.getStart();
      }
   }

   private boolean shouldBeOnTop(GenomicGlyph glyph) {
      return ((GBGenomicGlyph) glyph).alignment.getEntity() instanceof CuratedCodon;
   }

   private boolean visualizeAdjacentHSPFor(GenomicGlyph glyph) {
      // If feature is not an HSP, leave
      if (!(glyph instanceof HSPGlyph)) {
      	return false;
      }
      
      // If feature is not the right Entity Type, leave
      if (! featureApplicableToVariedLinePainting(alignment.getEntity())) {
         return false;
      }
      
      HSPGlyph hsp = (HSPGlyph)glyph;
      Glyph painter = hsp.getHSPIntronGlyphIfNecessary();
      if (painter != null) {
         backgroundParent().addChild(painter);
         return true;
      }
      return false;
   }

   /**
	* Test of whether to apply line thickness variations to a given parent
	* entity or not.
	*/
   private boolean featureApplicableToVariedLinePainting(AlignableGenomicEntity feature) {
		Set types = EntityTypeSet.getEntityTypeSet("VariableLineThicknessFeatureTypes");
		if (types == null || types.size() == 0)
			return false;
		return types.contains(feature.getEntityType());
   }


}
