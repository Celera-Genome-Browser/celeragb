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
import api.stub.geometry.Range;


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


public class Alignment implements java.io.Serializable {

     private static final long serialVersionUID=2;
     /**
      * @label aligned axis
      * @supplierCardinality 1
      */
     private transient Axis axis;

     /**
      * @label aligned entity
      * @supplierCardinality 1
      */
     private AlignableGenomicEntity entity;


  public Alignment (Axis axis, AlignableGenomicEntity entity) {
     this.axis=axis;
     this.entity=entity;
  }


  public Range.Orientation getOrientationOnAxis() {
     return Range.UNKNOWN_ORIENTATION;
  }


  public boolean orientationForwardOnAxis() {
     return false;
  }


  public boolean orientationUnknownOnAxis() {
     return true;
  }


  public boolean orientationReverseOnAxis() {
     return false;
  }



  public Axis getAxis() {
     return axis;
  }


  public AlignableGenomicEntity getEntity() {
     return entity;
  }


  /**
   * Warning - this method only exists so the object can be properly serialized
   * It will throw an exception if called for any other purpose.
   */
  public void setAxisIfNull(Axis axis) {
     if (this.axis==null) this.axis=axis;
     else throw new IllegalStateException("Axis cannot be changed");
  }


  public Alignment cloneWithNewEntity(AlignableGenomicEntity newEntity){
     return new Alignment(this.axis,newEntity);
  }


  void setAxis(Axis axis) {
     this.axis=axis;
  }


  void setEntity(AlignableGenomicEntity entity) {
    System.out.println("Set entity called setting entity to " + entity);
     this.entity=entity;
  }


  /**
   * Hashcode to help with putting Alignments in HashSet's and such.
   * Alignments between the same Axis and AlignableGenomicEntity
   *


   * should result in the same Hashcode.
   */
  public int hashCode() {
    int returnHash = 0;
    if (axis != null) returnHash += axis.hashCode();
    if (entity != null) returnHash += entity.hashCode();
    return returnHash;
  }


  /**
   * Test for "equals".
   * The argument otherObj must be an instanceof Alignment.
   * Finally, the otherGeomAlign.rangeOnAxis.equals(this.rangeOnAxis); must be true.
   */
  public boolean equals(Object otherObj) {
     // Check class type...
     if (!(otherObj instanceof Alignment)) return false;
     // Provide added comparison...
     Alignment otherAlign = (Alignment)otherObj;
     // Return the test of rangeOnAxis...
     if ((axis == null) || (otherAlign.axis == null)) {
        return (otherAlign.entity.equals(this.entity) && otherAlign.axis == null && axis == null );
     }
     return (otherAlign.axis.equals(this.axis) && otherAlign.entity.equals(this.entity));
  }

}
