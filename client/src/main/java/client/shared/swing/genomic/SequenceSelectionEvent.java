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
package client.shared.swing.genomic;

import java.awt.*;

public class SequenceSelectionEvent {
    private long beginLocation;
    private long endLocation;
    private boolean selected;

    private Component source;

    public SequenceSelectionEvent(Component source, long beginLocation, long endLocation, boolean selected){
      this.source = source;
      this.beginLocation = beginLocation;
      this.endLocation = endLocation;
      this.selected = selected;
    }

    public Component getSource(){
      return source;
    }

    public long getBeginLocation(){
      return beginLocation;
    }

    public long getEndLocation(){
      return endLocation;
    }

    public boolean hasSelection(){
      return selected;
    }

}
