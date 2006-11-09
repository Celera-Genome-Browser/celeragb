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

import api.stub.geometry.Range;
import api.stub.geometry.RangeSet;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies
 * @version $Id$
 */

public class RangeLoadFilterStatus extends LoadFilterStatus {

  private static final long serialVersionUID=1;
  private RangeSet rangeSet;

  public Set getLoadedRanges() {
     return Collections.unmodifiableSet(rangeSet);
  }

  public Range getBoundingRange() {
    return rangeSet.getBoundingRange();
  }


  RangeLoadFilterStatus(LoadFilter loadFilter,Range boundingRange) {
     super(loadFilter);
     rangeSet=new RangeSet(boundingRange);
  }

  RangeSet processRangeRequest(Range requestRange) {
     return rangeSet.findGapSubSet(requestRange,true);
  }

  /**
   * This gets called by the LoadRequestStatus when the state changes to Loaded
   */
  void requestCompleted(LoadRequest request){
     if (!request.isRangeRequest()) throw
        new IllegalStateException("The request made with the filter is not a"+
          " range request, but the LoadFilterStatus is a RangeLoadFilterStatus." );
     if (!request.isUnloadRequest()) addRangeToComplete(request.getRequestedRanges());
     else {
       removeRanges(request.getRequestedRanges());
     }
  }

  private void addRangeToComplete(Set ranges) {
     for (Iterator it=ranges.iterator();it.hasNext();) {
        rangeSet.add((Range)it.next());
     }
     if (rangeSet.contains(rangeSet.getBoundingRange())) setCompletelyLoaded(true);
  }

  private void removeRanges(Set ranges) {
     for (Iterator it=ranges.iterator();it.hasNext();) {
        rangeSet.remove((Range)it.next());
     }
     if (isCompletelyLoaded()) setCompletelyLoaded(false);
  }


}