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
package api.entity_model.access.observer;

import api.entity_model.access.report.PropertyReport;
import api.entity_model.model.fundtype.Axis;
import api.stub.geometry.Range;

public interface SequenceAnalysisObserver
{
  /**
   * Notifies that an analysis has completed on a particular axis and provides
   * the resulting features as they are located against <axis>
   *
   * @param axis - the axis that the analysis was performed against (i.e. the
   * axis that provided the subject sequence).
   *
   * @param range - range on axis that was was used to construct the subject
   * sequence
   *
   * @param report - a report of the analysis hits that were found on the
   * requested range of the axis.
   *
   * @param isDone - set to true when the query completes
   */
  void noteSequenceAnalysisCompleted(Axis axis, Range rangeOfAnalysis, PropertyReport report, boolean isDone);
}
