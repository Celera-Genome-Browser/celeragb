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
/**
 ********************************************************************
 * CVS_ID:  $Id$
 */

package client.gui.framework.outline;

import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.Axis;

import java.text.DecimalFormat;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class GenomicAxisTreeNode extends GenomicEntityTreeNode {
//	 JCVI LLF: 10/20/2006
	//	 RT 10/27/2006
    private static final ImageIcon targetIcon = new ImageIcon(Renderer.class.getResource("/resource/client/images/genomicaxis.gif"));
    public GenomicAxisTreeNode(GenomicAxis ga) {
        super(ga);
    }

    public boolean isLeaf() {
      return true;
    }

    public void loadChildren() {
    }

    public String toString() {
       return super.toString()+" (Len: "+convertToUnits(((Axis)getUserObject()).getMagnitude())+","+
                               " Pos: "+convertToUnits(((GenomicAxis)getUserObject()).getOrder())+")";
    }

    public Icon getNodeIcon() {
      return targetIcon;
    }

    private String convertToUnits(int value) {
      double endValue = (double) value;
      String suffix = new String("");
      if      (endValue >= 0.0     && endValue <  1000.0)
        suffix="b";
      else if (endValue >= 1000.0  && endValue <  1000000.0) {
        suffix = "kb";
        endValue = endValue/1000.0;
      }
      else if (endValue >= 1000000.0) {
        suffix = "Mb";
        endValue = endValue/1000000.0;
      }
      DecimalFormat tFormat = (DecimalFormat) DecimalFormat.getInstance();
      tFormat.applyPattern("0.##");
      String valueText = tFormat.format(endValue);
      return valueText+suffix;
    }

    void aboutToBeRemoved(){}
}

