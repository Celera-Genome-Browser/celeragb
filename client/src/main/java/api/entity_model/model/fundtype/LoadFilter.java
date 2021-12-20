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
package api.entity_model.model.fundtype;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies
 * @version $Id$
 */

import api.stub.geometry.Range;

public class LoadFilter implements java.io.Serializable{

  public static final byte REVERSE_STRAND=-1;
  public static final byte BOTH_STRANDS=0;
  public static final byte FORWARD_STRAND=1;

  private static final long serialVersionUID=2;
  private String name;
  private EntityTypeSet entityTypes = null;
  private boolean isStrandSpecific;

  private LoadFilterStatus loadFilterStatus;

  /**
   * Use this constructor for simplistic filters, like properties.
   */
  public LoadFilter(String name) {
     this.name=name;
     loadFilterStatus=new LoadFilterStatus(this);
  }

  /**
   * Use this constructor for filtering on entity type, where range will not be part
   * of the request
   *
   * @parameter name- name of filter
   * @parameter entityTypes- the set of entityTypes that will be returned
   */
  public LoadFilter(String name, EntityTypeSet entityTypes) {
     this(name);
     this.entityTypes = entityTypes;
  }

 /**
   * Use this constructor where range will be part of the request, but
   * entity type is not involved
   *
   * @parameter name- name of filter
   * @parameter entityTypes- the set of entityTypes that will be returned
   * @parameter range- the maximum possible range for requests (usually 0 to
   * axis magnitude), not the range to filter by
   */
  public LoadFilter(String name, Range range, boolean isStrandSpecific) {
     this.name=name;
     this.isStrandSpecific=isStrandSpecific;
     loadFilterStatus=new RangeLoadFilterStatus(this,range);
  }

 /**
   * Use this constructor where range will be part of the request, and
   * entity type is involved
   *
   * @parameter name- name of filter
   * @parameter range- the maximum possible range for requests (usually 0 to
   * axis magnitude), not the range to filter by
   */
  public LoadFilter(String name, EntityTypeSet entityTypes, Range range, boolean isStrandSpecific) {
     this(name,range,isStrandSpecific);
     this.entityTypes = entityTypes;
  }

  /*final void addEntityType(EntityType entityType) {
     entityTypes.add(entityType);
  }*/

  public final EntityTypeSet getEntityTypeSet() {
     //return Collections.unmodifiableCollection(entityTypes);
     return entityTypes;
  }

  public final EntityType[] getEntityTypesAsArray() {
     if (entityTypes == null) return new EntityType[0];
     return (EntityType[])entityTypes.toArray(new EntityType[0]);
  }

  public final byte getAffectedStrand() {
    if (!isStrandSpecific) return BOTH_STRANDS;
    else {
      if (loadFilterStatus instanceof RangeLoadFilterStatus) {
        if (((RangeLoadFilterStatus)loadFilterStatus).getBoundingRange().isForwardOrientation()) return FORWARD_STRAND;
        else return REVERSE_STRAND;
      }
      else return BOTH_STRANDS;
    }
  }

  public boolean isStrandSpecific() {
    return isStrandSpecific;
  }


  public final boolean isFilteringOnEntityType() {
     if (entityTypes == null) return false;
     return (entityTypes.size()>0);
  }

  public final String getFilterName() {
     return name;
  }

  public String toString() {
     return "LoadFilter: "+getFilterName();
  }

  public LoadFilterStatus getLoadFilterStatus() {
    return loadFilterStatus;
  }

}