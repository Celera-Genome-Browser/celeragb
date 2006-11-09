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

/**
 * Title:        Your Product Name<p>
 * Description:  This is the main Browser in the System<p>
 * @author Peter Davies
 * @version
 */
package api.facade.concrete_facade.aggregate;

import api.facade.abstract_facade.genetics.ControlledVocabService;
import api.stub.data.ControlledVocabElement;
import api.stub.data.NoData;
import api.stub.data.OID;

import java.util.ArrayList;
import java.util.List;

public class AggregateControlledVocabService extends AggregateFacadeBase implements ControlledVocabService{

  public ControlledVocabElement[] getControlledVocab
    (OID entityOID, String vocabIndex) throws NoData {
        Object[] aggregates=getAggregates();
        ControlledVocabElement[] tmpArray = null;
        List returnValues=new ArrayList(32);
        for (int i = 0; i < aggregates.length; i++) {
           tmpArray=((ControlledVocabService)aggregates[i]).
                getControlledVocab(entityOID,vocabIndex);
           if (tmpArray != null) {
            // Add elements but preserve their order!
            // Cases of multiple aggregates contributing to a list have some
            // issues that are not dealt with very well here. There is no policy
            // defined for it.
            // Issue1: If more than one aggregate returns values for this vocab,
            //         then the order of the ControlledVocabElement objects in
            //         the list depends on the order of the aggregates from the
            //         getAggregates() call.
            // Issue2: If a subsequent aggregate has a ControlledVocabElement
            //         with the same key as one already added, that new one is
            //         discarded.
            for (int j=0; j<tmpArray.length; j++) {
              ControlledVocabElement tmp = tmpArray[j];
              if (!returnValues.contains(tmp)) {
                returnValues.add(tmp);
              }
            }
           }
         }
         return (ControlledVocabElement[])returnValues.
           toArray(new ControlledVocabElement[returnValues.size()]);
  }

  protected String getMethodNameForAggregates(){
     return "getControlledVocabService";
  }

  protected Class[] getParameterTypesForAggregates(){
     return new Class[0];
  };

  protected  Object[] getParametersForAggregates(){
     return new Object[0];
  }
}