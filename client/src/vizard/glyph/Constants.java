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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Constants {
   public static final Color bright3DColor = Color.white;
   public static final Color shadow3DColor = Color.darkGray;

   public static final Color woodColor = new Color(219, 191, 174);
   public static final Color darkWoodColor = new Color(199, 171, 154);

   public static final Color dimGlassColor = new Color(104, 164, 134, 80);
   public static final Color darkGlassColor = new Color(84, 144, 114, 180);

   public static final Font cleanFont = Font.decode(null);
   public static final Font cleanBoldFont = cleanFont.deriveFont(Font.BOLD);

   public static final AffineTransform unitTransform = new AffineTransform();

   public static final BufferedImage woodTexture;

   static {
      // JCVI LLF: 10/20/2006
      // RT 10/28/2006
	  ImageIcon icon = new ImageIcon(Constants.class.getResource("/resource/client/images/wood.jpg"));

      woodTexture = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
      Graphics2D g2d = woodTexture.createGraphics();
      g2d.drawImage(icon.getImage(), 0, 0, null);
   }
}
