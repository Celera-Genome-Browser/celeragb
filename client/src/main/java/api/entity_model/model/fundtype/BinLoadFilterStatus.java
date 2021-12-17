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

import java.util.HashSet;
import java.util.Set;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies
 * @version $Id$
 */

public class BinLoadFilterStatus extends LoadFilterStatus {

  private Set loadedBins=new HashSet();
  private Set completeSetOfBins=new HashSet();

  public BinLoadFilterStatus(LoadFilter loadFilter, Bin[] completeSetOfBins) {
     super(loadFilter);
  }

  public boolean isBinLoaded(Bin bin) {
     checkValidBin(bin);
     return loadedBins.contains(bin);
  }


  /**
   * This gets called by the LoadRequestStatus when the state changes to Loaded
   */
  void requestCompleted(LoadRequest request){
     if (!request.isBinRequest()) throw
        new IllegalStateException("The request made with the filter is not a"+
          " binned request, but the LoadFilterStatus is a BinLoadFilterStatus." );
     addBinAsLoaded(request.getBin());
  }

  private void checkValidBin(Bin bin){
      if (!completeSetOfBins.contains(bin))
         throw new IllegalArgumentException("Passed bin not in completeSetOfBins list.");
  }

  private void addBinAsLoaded(Bin bin) {
      checkValidBin(bin);
      loadedBins.add(bin);
      if (loadedBins.size()==completeSetOfBins.size()) setCompletelyLoaded(true);
  }


}