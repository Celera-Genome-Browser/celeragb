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
import api.entity_model.model.fundtype.AlignableGenomicEntity;
import api.stub.geometry.MutableRange;
import api.stub.geometry.Range;
import client.gui.framework.view_pref_mgr.ViewPrefMgr;
import vizard.Bounds;
import vizard.genomics.glyph.GenomicGlyph;
import vizard.util.Assert;

import java.awt.Color;

public class GBGenomicGlyph extends GenomicGlyph {
   //@todo preferences
   public static int DEFAULT_HEIGHT = 10;

   protected GBGlyphFactory glyphFactory;

   protected GeometricAlignment alignment;
   protected int start;
   protected int end;
   protected Color color;
   protected boolean isForward;
   protected double y;
   protected double height;

   public GBGenomicGlyph(GBGlyphFactory glyphFactory, GeometricAlignment alignment) {
      if (Assert.debug)
         Assert.vAssert(!glyphFactory.hasGlyphFor(alignment.getEntity()));

      this.glyphFactory = glyphFactory;
      this.alignment = alignment;

      glyphFactory.addToEntityToGlyphMap((AlignableGenomicEntity) alignment.getEntity(), this);
   }

   public void delete() {
      glyphFactory.removeFromEntityToGlyphMap(this);
      super.delete();
   }

   public GeometricAlignment alignment() {
      return alignment;
   }

   public final void propertiesChanged() {
      Bounds oldBounds = getBounds();

      repaint();
      loadProperties();
      repaint();

      if (!oldBounds.equals(getBounds()))
         boundsChanged();
   }

   protected void loadProperties() {
      color = ViewPrefMgr.getViewPrefMgr().getColorForEntity(alignment.getEntity());
      checkReverseComplementAndSetRange(alignment.getRangeOnAxis());
      isForward = !alignment.getRangeOnAxis().isReversed();
      height = DEFAULT_HEIGHT;

      //if (genomicParent == null || alignment.getEntity().isWorkspace())
      y = 0;
      /*else {
          double yCenter = genomicParent.height() / 2;
          y = yCenter - height / 2;
      }*/
   }

   protected void checkReverseComplementAndSetRange(Range range) {
      if (glyphFactory.isReverseComplement()) {
         MutableRange m = range.toMutableRange();
         m.mirror(glyphFactory.axis().getMagnitude());
         range = m;
      }
      start = range.getMinimum();
      end = range.getMaximum();
   }

   //EntityAdapter specialization
   public int start() {
      return start;
   }
   public int end() {
      return end;
   }
   public double height() {
      return height;
   }
   public Color color() {
      return color;
   }
   public boolean isForward() {
      return isForward;
   }
   public double y() {
      return y;
   }
   /*
       public void setRange(int start, int end) {
           if (start > end)
               { int temp = start; start = end; end = temp; }
           if (start != this.start || end != this.end) {
               repaint();
               Bounds oldBounds = getBounds();
               this.start = start;
               this.end = end;
               isForward = true;
               notifyBoundsChange(oldBounds);
               repaint();
           }
       }
   */
   public void setY(double y) {
      if (y != this.y) {
         repaint();
         this.y = y;
         repaint();
         boundsChanged();
      }
   }

   public void setHeight(double height) {
      if (height != this.height) {
         repaint();
         this.height = height;
         repaint();
         boundsChanged();
      }
   }

   public void setColor(Color color) {
      if (color != this.color) {
         this.color = color;
         repaint();
      }
   }

   public void setIsForward(boolean b) {
      if (b != isForward) {
         isForward = b;
         repaint();
      }
   }

}
