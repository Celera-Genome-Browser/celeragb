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

import vizard.ParentGlyph;
import vizard.glyph.HRulerSliderGlyph;

public class AxisRulerSliderGlyph extends ParentGlyph {
   private AxisRulerGlyph ruler;
   private double location;

   public int start() {
      return (int) location;
   }
   public int end() {
      return (int)location;
   }
   public double height() {
      return ruler.height();
   }

   public AxisRulerSliderGlyph(AxisRulerGlyph ruler) {
      this.ruler = ruler;
      addChild(new HRulerSliderGlyph(ruler.ruler()) {
         public double location() {
            return location;
         }
      });
   }

   public HRulerSliderGlyph slider() {
      return (HRulerSliderGlyph) child(0);
   }

   public void setLocation(int location) {
      repaint();
      this.location = location;
      repaint();
   }
}
