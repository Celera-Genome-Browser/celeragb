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


import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 *
 * This interface represents AlignableGenomicEntity (subtypes) which;
 * - may be aligned to only one axis.
 * - may be aligned only once to that axis.
 */
public interface SingleAlignmentSingleAxis extends
      MultipleAlignmentsSingleAxis, SingleAlignmentMultipleAxes {
  /**
   * @return The single Alignment to the one Axis this entity is aligned to.
   */
  public Alignment getOnlyAlignmentToOnlyAxis();

  /**
   * @return The single GeometricAlignment to the one Axis this entity is aligned to.
   * If the alignment is NOT a GeometricAlignment, this will return null.
   */
  public GeometricAlignment getOnlyGeometricAlignmentToOnlyAxis();
}