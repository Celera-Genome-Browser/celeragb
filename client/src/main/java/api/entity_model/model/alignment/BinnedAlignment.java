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

import api.entity_model.model.fundtype.AlignableGenomicEntity;
import api.entity_model.model.fundtype.Axis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version $Id$
 *
 *
 * Note: Constructing an instance of this class does NOT fully align the entity
 * to the axis.  In order to do this, you must pass the constructed Alignment
 * to the axis (addAlignmentToEntity on the mutator)
 */


public class BinnedAlignment extends Alignment {

  private static final long serialVersionUID=1;
  /**
   *@associates <{Bin}>
   * @supplierCardinality 0..*
   * @label bins
   */
  private List bins=new ArrayList(1);

  public BinnedAlignment (Axis axis, AlignableGenomicEntity entity) {
     super(axis,entity);
  }

  public Collection getBins() {
     return Collections.unmodifiableList(bins);
  }

  public Alignment cloneWithNewEntity(AlignableGenomicEntity newEntity){
     BinnedAlignment alignment= new BinnedAlignment(getAxis(),newEntity);
     alignment.bins=this.bins;
     return alignment;
  }

  /**
   * Should only be called from Bin
   */
  void addAlignmentToBin(Bin bin) {
     bins.add(bin);
  }

  /**
   * Should only be called from Bin
   */
  void removeAlignmentFromBin(Bin bin) {
     bins.remove(bin);
  }
}
