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
//*============================================================================
//* $Author$
//*
//* $Date$
//*
//* $Header$r: /cm/cvs/enterprise/src/client/gui/components/annotation/axis_annotation/AxisSessionBookmarkGlyph.java,v 1.8 2004/02/19 07:41:59 tsaf Exp $
//*============================================================================
package client.gui.components.annotation.axis_annotation;

import api.stub.data.OID;
import api.stub.geometry.Range;
import vizard.Bounds;
import vizard.GraphicContext;
import vizard.genomics.glyph.AxisRulerGlyph;
import vizard.glyph.FastRectGlyph;

import java.awt.*;


public class AxisSessionBookmarkGlyph extends FastRectGlyph {
   /** DOCUMENT ME! */
   public static Color    glyphColor   = Color.cyan;
   /** DOCUMENT ME! */
   public static int      PIXELSW      = 10;
   /** DOCUMENT ME! */
   public static int      PIXELSH      = 10;
   /** DOCUMENT ME! */
   private AxisRulerGlyph ruler;
   /** DOCUMENT ME! */
   private int   location;
   /** DOCUMENT ME! */
   private OID   axisOid;
   /** DOCUMENT ME! */
   private int   glyphNumber;
   /** DOCUMENT ME! */
   private Range visibleRange;

   /**
    * Creates a new AxisSessionBookmarkGlyph object.
    *
    * @param	ruler		DOCUMENT ME!
    * @param	axisOid		DOCUMENT ME!
    * @param	number		DOCUMENT ME!
    * @param	visibleRange		DOCUMENT ME!
    */
   public AxisSessionBookmarkGlyph( AxisRulerGlyph ruler, OID axisOid, int number, Range visibleRange ) {
      this.ruler        = ruler;
      this.axisOid      = axisOid;
      glyphNumber       = number;
      this.visibleRange = visibleRange;
   }

   /**
    * DOCUMENT ME!
    *
    * @return 	DOCUMENT ME!
    */
   public OID getAxisOid() {
      return axisOid;
   }


   /**
    * DOCUMENT ME!
    *
    * @return 	DOCUMENT ME!
    */
   public int getGlyphNumber() {
      return glyphNumber;
   }


   /**
    * DOCUMENT ME!
    *
    * @param       location    DOCUMENT ME!
    */
   public void setLocation( int location ) {
      repaint();
      this.location = location;
      repaint();
   }


   /**
    * DOCUMENT ME!
    *
    * @return 	DOCUMENT ME!
    */
   public Range getVisibleRange() {
      return visibleRange;
   }


   /**
    * DOCUMENT ME!
    *
    * @param       b    DOCUMENT ME!
    */
   public void addBounds( Bounds b ) {
      double w = width();
      if ( w == 0 ) {
         w = 0.01;
      }
      b.add( x(), y(), width(), height() );
      b.leftPixels  = Math.max( b.leftPixels, PIXELSW );
      b.rightPixels = Math.max( b.rightPixels, PIXELSW );
      b.upPixels    = Math.max( b.upPixels, PIXELSH );
   }


   /**
    * DOCUMENT ME!
    *
    * @return 	DOCUMENT ME!
    */
   public Color backgroundColor() {
      return glyphColor;
   }


   /**
    * DOCUMENT ME!
    *
    * @param       arg    DOCUMENT ME!
    *
    * @return 	DOCUMENT ME!
    */
   public boolean equals( AxisSessionBookmarkGlyph arg ) {
      if ( this.axisOid.equals( arg.getAxisOid() ) && ( this.glyphNumber == ( arg.getGlyphNumber() ) )) {
         return true;
      }
      else {
         return false;
      }
   }


   /**
    * DOCUMENT ME!
    *
    * @return 	DOCUMENT ME!
    */
   public double height() {
      return PIXELSH;
   }


   /**
    * Glyph specialization.
    * @param	gc		DOCUMENT ME!
    */
   public void paint( GraphicContext gc ) {
      double pixw = gc.pixelWidth();
      gc.setColor( glyphColor );
      gc.fillRect( x(), y(), pixw * PIXELSW, PIXELSH );
      gc.setColor( Color.red );
      //System.out.println( gc.getFont().getSize() );
      Font font = new Font( String.valueOf( glyphNumber ), Font.BOLD, 10 );
      gc.setFont( font );
      //System.out.println( gc.getFont().getSize() );
      gc.drawString( String.valueOf( glyphNumber ), x() - .6, y() + 8 );
      gc.setColor( glyphColor );
   }


   /**
    * DOCUMENT ME!
    *
    * @return 	DOCUMENT ME!
    */
   public double width() {
      return 0;
   }


   /**
    * DOCUMENT ME!
    *
    * @return 	DOCUMENT ME!
    */
   public double x() {
      return location;
   }


   /**
    * DOCUMENT ME!
    *
    * @return 	DOCUMENT ME!
    */
   public double y() {
      return 0;
   }
}