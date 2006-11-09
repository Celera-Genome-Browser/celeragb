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
package shared.util;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

public class PrintableComponent implements Printable{

  Component comp;
  double scaleFactor=0.0;

  public PrintableComponent(Component comp) {
    this.comp=comp;
  }

  public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
	         throws PrinterException {

     if (scaleFactor==0.0) {
        double scaleFactorX=0.0;
        double scaleFactorY=0.0;
        scaleFactorX=pageFormat.getImageableWidth()/comp.getWidth();
        scaleFactorY=pageFormat.getImageableHeight()/comp.getHeight();
        scaleFactor= (scaleFactorX < scaleFactorY) ? scaleFactorX : scaleFactorY;
     }
     graphics.translate((int)pageFormat.getImageableX(),(int)pageFormat.getImageableY());
     if (graphics instanceof Graphics2D) {
        ((Graphics2D)graphics).scale(scaleFactor,scaleFactor);
     }
     comp.printAll(graphics);
     if (pageIndex>=1) return NO_SUCH_PAGE;
     return PAGE_EXISTS;
  }
}