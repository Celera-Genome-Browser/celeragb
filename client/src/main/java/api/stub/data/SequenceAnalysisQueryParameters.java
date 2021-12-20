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
package api.stub.data;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies (peter.davies)
 * @version $Id$
 *
 * This class is designed to be used for intercomponent communication between
 * SequenceAnalysis and any view that would like to pass attributes to it using
 * the GenericModel mechanism of the framework
 * @see client.gui.framework.session_mgr.BrowserModel
 *
 */

import api.entity_model.model.fundtype.Axis;
import api.stub.geometry.Range;
import api.stub.sequence.Sequence;


public class SequenceAnalysisQueryParameters {

  public static String PARAMETERS_PROPERTY_KEY="SequenceAnalysisQueryParameters.Parameters";

  private Range range;
  private Sequence subjectSeq;
  private Axis axis;
  private Sequence querySeq;

  public SequenceAnalysisQueryParameters(Range range, Axis axis, Sequence bioSeq, Sequence qSeq) {
    this.range=range;
    this.axis=axis;
    this.subjectSeq=bioSeq;
    this.querySeq=qSeq;
  }

  public boolean isInternalSequence() {
    return range!=null;
  }

  public Range getRange() {
    return range;
  }

  public Sequence getSubjectSequence () {
    return subjectSeq;
  }
  
  public Sequence getQuerySequence() {
  	return querySeq;
  }

  public Axis getAxis() {
    return axis;
  }
}