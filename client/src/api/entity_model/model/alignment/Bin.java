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
package api.entity_model.model.alignment;

import api.entity_model.model.fundtype.Axis;
import api.stub.geometry.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies
 * @version $Id$
 */

public class Bin implements java.io.Serializable {

  private static final long serialVersionUID=1;
  private String name;
  private Range range;
  private Axis axis;
  List alignments=new ArrayList();

  public Bin(String name, Range range, Axis axis) {
     this.name=name;
     this.range=range;
     this.axis=axis;
  }

  public void addAlignmentAsLastInBin(BinnedAlignment alignment) {
     if (getPositionInBin(alignment)==-1) {
       alignments.add(alignment);
       alignment.addAlignmentToBin(this);
     }
     else {
       throw new IllegalArgumentException("Alignment already in bin, try moveAlignment");
     }
  }

  public void addAlignmentToBin(BinnedAlignment alignment,int order) {
     if (getPositionInBin(alignment)==-1) {
        alignments.add(order,alignment);
        alignment.addAlignmentToBin(this);
     }
     else {
       throw new IllegalArgumentException("Alignment already in bin, try moveAlignment");
     }
  }

  public void removeAlignmentFromBin(BinnedAlignment alignment) {
     if (alignments.remove(alignment)) {
        alignment.removeAlignmentFromBin(this);
     }
  }

  public List getAllAlignmentsInBin() {
     return Collections.unmodifiableList(alignments);
  }

  public int getPositionInBin(BinnedAlignment alignment) {
     return alignments.indexOf(alignment);
  }

  public void moveAlignmentInBin(BinnedAlignment alignment, int newPosition) {
     int index=getPositionInBin(alignment);
     if (index==-1) {
        alignments.remove(index);
        addAlignmentToBin(alignment,newPosition);
     }
     else {
       throw new IllegalArgumentException("Alignment not in bin, try addAlignment");
     }
  }

  public String getBinName() {
    return name;
  }

  public Range getBinRange() {
    return range;
  }

  public Axis getBinAxis() {
    return axis;
  }

}