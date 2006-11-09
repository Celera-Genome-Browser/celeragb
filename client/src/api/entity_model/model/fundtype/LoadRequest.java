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

import api.entity_model.model.alignment.Bin;
import api.stub.geometry.Range;
import api.stub.geometry.RangeSet;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies
 * @version $Id$
 */

public class LoadRequest implements java.io.Serializable{

  private static final long serialVersionUID=2;
  private RangeSet ranges;
  private Bin bin;
  private LoadFilter loadFilter;
  private LoadRequestStatus loadRequestStatus;
  private boolean isUnloadRequest;


  public LoadRequest(LoadFilter loadFilter) {
    this.loadFilter=loadFilter;
    loadRequestStatus=new LoadRequestStatus(loadFilter);
  }

  public LoadRequest(Range range,LoadFilter loadFilter) {
     this(range,loadFilter,false);
  }

  public LoadRequest(Range range,LoadFilter loadFilter,boolean isUnloadRequest) {
     this(loadFilter);
     this.isUnloadRequest=isUnloadRequest;
     LoadFilterStatus lfStatus=loadFilter.getLoadFilterStatus();
     if (!isUnloadRequest && lfStatus instanceof RangeLoadFilterStatus) {
           RangeLoadFilterStatus rlfStatus=(RangeLoadFilterStatus)lfStatus;
           ranges=rlfStatus.processRangeRequest(range);
     }
     else {
       ranges=new RangeSet();
       ranges.add(range);
     }
  }

  public LoadRequest(Bin bin,LoadFilter loadFilter) {
     this(loadFilter);
     this.bin=bin;
  }

  public boolean isRangeRequest() {
     return ranges!=null;
  }

  public boolean isUnloadRequest() {
    return isUnloadRequest;
  }

  public boolean isBinRequest() {
     return bin!=null;
  }

  public Bin getBin() {
     return bin;
  }

  public Set getRequestedRanges() {
    if (ranges==null) return new TreeSet();
    return Collections.unmodifiableSet(ranges);
  }

  public LoadFilter getLoadFilter() {
     return loadFilter;
  }

  public LoadRequestStatus getLoadRequestStatus() {
     return loadRequestStatus;
  }
}