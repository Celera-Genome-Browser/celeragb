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
package vizard.glyph;

import vizard.Bounds;
import vizard.GraphicContext;

import java.awt.Color;
import java.awt.geom.Line2D;

/**
 * A horizontal line with a 3D effect.
 */
public abstract class HLine3DGlyph extends Effect3DGlyph {
   /**
    * Return the line first x coordinate.
    */
   public abstract double xmin();

   /**
    * Return the line second x coordinate.
    */
   public abstract double xmax();

   /**
    * Return the line y coordinate.
    */
   public abstract double y();

   /**
    * Paint a horizontal line with a 3D effect with the given parameters.
    */
   public static void paint(GraphicContext gc, double xmin, double xmax, double y, boolean isRaised, Color bright, Color shadow) {
      gc.setZeroLineWidth();
      double pixh = gc.pixelHeight();

      Line2D.Double line = gc.tempLine();
      line.x1 = xmin;
      line.x2 = xmax;
      line.y1 = line.y2 = y - pixh;

      gc.setColor(isRaised ? bright : shadow);
      gc.draw(line);

      gc.setColor(isRaised ? shadow : bright);
      line.y1 += pixh;
      line.y2 += pixh;
      gc.draw(line);
   }

   /**
    * Paint the glyph.
    */
   public void paint(GraphicContext gc) {
      paint(gc, xmin(), xmax(), y(), isRaised(), brightColor(), shadowColor());
   }

   /**
    * Adds the bounds of this line to the given rectangular bounds.
    */
   public void addBounds(Bounds bounds) {
      bounds.add(xmin(), y(), xmax() - xmin(), 0);
      bounds.upPixels = Math.max(bounds.upPixels, 1);
      bounds.downPixels = Math.max(bounds.downPixels, 1);
   }

   /**
    * The purpose of the HLine3DGlyph.Concrete class is to provide
    * a ready-to-use glyph to the application programmer.
    */
   public static class Concrete extends HLine3DGlyph {
      private double xmin, xmax, y;
      private Color bright, shadow;
      private boolean isRaised;

      public Concrete(double xmin, double xmax, double y, boolean isRaised) {
         this(xmin, xmax, y, isRaised, Constants.bright3DColor, Constants.shadow3DColor);
      }

      public Concrete(double xmin, double xmax, double y, boolean isRaised, Color bright, Color shadow) {
         setLine(xmin, xmax, y);
         this.bright = bright;
         this.shadow = shadow;
         this.isRaised = isRaised;
      }

      public double xmin() {
         return xmin;
      }
      public double xmax() {
         return xmax;
      }
      public double y() {
         return y;
      }
      public boolean isRaised() {
         return isRaised;
      }
      public Color brightColor() {
         return bright;
      }
      public Color shadowColor() {
         return shadow;
      }

      public void setLine(double xmin, double xmax, double y) {
         repaint();
         this.xmin = xmin;
         this.xmax = xmax;
         this.y = y;
         repaint();
         boundsChanged();
      }

      public void setRaised(boolean raised) {
         isRaised = raised;
         repaint();
      }

      public void setBrightColor(Color c) {
         bright = c;
         repaint();
      }

      public void setShadowColor(Color c) {
         shadow = c;
         repaint();
      }
   }
}
