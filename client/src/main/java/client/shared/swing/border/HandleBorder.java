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
package client.shared.swing.border;

import javax.swing.border.AbstractBorder;
import java.awt.*;

public class HandleBorder extends AbstractBorder {
		Color bgColor;
		Color handleColor;

    public HandleBorder(Color bgColor, Color handleColor){
      this.bgColor = bgColor;
      this.handleColor = handleColor;
    }

    public void setHandleColor(Color c){
      handleColor = c;
    }

    public Color getHandleColor(){
      return handleColor;
    }

    public void setBackground(Color bgColor){
      this.bgColor = bgColor;
    }

    public Color getBackground(){
      return bgColor;
    }
    public boolean isBorderOpaque(){
      return true;
    }

    public Insets getBorderInsets(Component c){
      return new Insets(1,8,1,1);
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height){
      Insets insets = getBorderInsets(c);
      g.setColor(bgColor);
      g.translate(x,y);
      //top
    	g.fillRect(0,0,width,insets.top);
    	//bottom
    	g.fillRect(0,height - insets.bottom, width, insets.bottom);
    	//left
    	g.fillRect(0,insets.top,insets.left, height - insets.top - insets.bottom);
    	//right
    	g.fillRect(width - insets.right, insets.top, insets.right, height - insets.top);

	    //handle
  	  g.setColor(handleColor);
   	 	g.fill3DRect(2,insets.top+2,insets.left-4, height - insets.top - insets.bottom -4 ,true);
    	g.translate(-x,-y);
  	}

}