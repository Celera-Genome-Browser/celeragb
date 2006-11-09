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

import api.stub.geometry.Range;
import vizard.genomics.glyph.AxisRulerGlyph;
import vizard.glyph.FastRectGlyph;
import vizard.glyph.HRulerGlyph;

import java.awt.Color;

public class AxisRulerSubViewRangeGlyph extends FastRectGlyph
{
    private AxisRulerGlyph ruler;
    private Range location = new Range();
    public static Color LIGHT = new Color(255, 255, 0, 90);

    public AxisRulerSubViewRangeGlyph(AxisRulerGlyph ruler) {
      this.ruler = ruler;
    }


    public void setLocation(Range location) {
	repaint();
        this.location = location;
	repaint();
    }


   public double x() {
    return location.getMinimum();
   }


   public double width() {
    return location.getMagnitude();
   }


   public double y() {
    return 0;
   }

   public double height() {
    return HRulerGlyph.K_TICS.intValue()/100. * ruler.height();
   }


  public Color backgroundColor() {
    return LIGHT;
  }

}
