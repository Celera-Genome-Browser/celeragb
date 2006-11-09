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


public class GeometricAlignment extends Alignment {

     private static final long serialVersionUID=1;
     private Range rangeOnAxis;

  public GeometricAlignment (Axis axis, AlignableGenomicEntity entity, Range rangeOnAxis) {
     super(axis,entity);
     this.rangeOnAxis=rangeOnAxis;
  }

  public Range.Orientation getOrientationOnAxis() {
     return rangeOnAxis.getOrientation();
  }

  public boolean orientationForwardOnAxis() {
     return rangeOnAxis.isForwardOrientation();
  }

  public boolean orientationUnknownOnAxis() {
     return rangeOnAxis.isUnknownOrientation();
  }


  /**
   * Test if the orientation on the Axis reversed.
   */
  public boolean orientationReverseOnAxis() {
     return rangeOnAxis.isReversed();
  }


  /**
   * Get the range on Axis...
   */
  public Range getRangeOnAxis() {
     return rangeOnAxis;
  }

  /**
   * Returns true if this alignment is in the same orientation
   * as the other alignment and the range of the two alignments
   * intersects. Returns false otherwise
   * @throws IllegalArgumentException if the orientation of the alignments differ
   * OR either alignment has an unknown orientation.
   */
  public boolean intersectsInAxisCoords(GeometricAlignment otherAlignment)  throws IllegalArgumentException {
    if ((this.orientationForwardOnAxis()) != otherAlignment.orientationForwardOnAxis()
        ||
        (this.orientationUnknownOnAxis())
        ||
        (otherAlignment.orientationUnknownOnAxis())) {
      return false;
    }
    else {
      return this.getRangeOnAxis().intersects(otherAlignment.getRangeOnAxis());
    }
  }

  /**
   * Returns true if this alignment is in the same orientation
   * as the other alignment and the range of this alignment
   * contains the range of the other alignment. Returns false otherwise
   * @throws IllegalArgumentException if the orientation of the alignments differ
   * OR either alignment has an unknown orientation.
   */
  public boolean containsInAxisCoords(GeometricAlignment otherAlignment) throws IllegalArgumentException {
    if ((this.orientationForwardOnAxis()) != otherAlignment.orientationForwardOnAxis()
        ||
        (this.orientationUnknownOnAxis())
        ||
        (otherAlignment.orientationUnknownOnAxis())) {
      throw new IllegalArgumentException("Two Geometric Alignemnts in different " +
        "orientation cannot be compared in axis coordinates for containment");
    }
    else {
      return this.getRangeOnAxis().contains(otherAlignment.getRangeOnAxis());
    }
  }


  /**
   * Provide a clone of this Geometric Alignment but with a different GenomicEntity.
   */
  public Alignment cloneWithNewEntity(AlignableGenomicEntity newEntity){
     return new GeometricAlignment(
        getAxis(),newEntity,getRangeOnAxis());
  }


  /**
   * Set the range on Axis...
   */
  protected void setRangeOnAxis(Range range) {
     rangeOnAxis=range;
  }





  /**
   * Test for "equals".
   * The argument otherObj must be an instanceof GeometricAlignment.
   * The otherGeomAlign.axis.equals(this.axis); must be true,
   * and the otherGeomAlign.entity.equals(this.entity); must be true,
   * and the otherGeomAlign.rangeOnAxis.equals(this.rangeOnAxis); must be true.
   */
  public boolean equals(Object otherObj) {
     // Check class type...
     if (!(otherObj instanceof GeometricAlignment)) return false;
     // Test with super...
     if (!super.equals(otherObj)) return false;
     // Provide added comparison...
     GeometricAlignment otherGeomAlign = (GeometricAlignment)otherObj;
     // Return the test of rangeOnAxis...
     return otherGeomAlign.rangeOnAxis.equals(this.rangeOnAxis);
  }

  public String toString() {
    String str = "[axis:" + this.getAxis().getOid() + ", range: " +
      this.getRangeOnAxis();
    return str;
  }
}
