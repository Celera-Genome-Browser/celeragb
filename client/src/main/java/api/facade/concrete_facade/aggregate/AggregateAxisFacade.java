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
package api.facade.concrete_facade.aggregate;

import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.fundtype.LoadRequest;
import api.facade.abstract_facade.fundtype.AxisLoader;
import api.stub.data.OID;
import api.stub.geometry.Range;
import api.stub.sequence.Sequence;
import api.stub.sequence.UnknownSequence;

import java.util.ArrayList;


public abstract class AggregateAxisFacade extends AggregateGenomicFacade implements AxisLoader {

  public Alignment[] loadAlignmentsToEntities(OID entityOID, LoadRequest loadRequest)
  {
        Object[] aggregates=getAggregates();
        ArrayList rtnList = new ArrayList(aggregates.length);
        Alignment[] tmpArray = null;
        int finalSize = 0;
        for (int i = 0; i < aggregates.length; i++) {
          tmpArray=((AxisLoader)
                      aggregates[i]).loadAlignmentsToEntities(entityOID, loadRequest);
          if (tmpArray != null) {
              rtnList.add(tmpArray);
              finalSize += tmpArray.length;
          }
        }
        tmpArray = new Alignment[finalSize];
        int offset = 0;
        rtnList.trimToSize();
        for (int i = 0; i < rtnList.size(); i++) {
            System.arraycopy((Alignment[]) rtnList.get(i), 0, tmpArray, offset, ((Alignment[]) rtnList.get(i)).length);
            offset += ((Alignment[]) rtnList.get(i)).length;
        }
        return tmpArray;
  }

   public Sequence getNucleotideSeq(OID genomicOID, Range nucleotideRange, boolean gappedSequence) {
        Object[] aggregates=getAggregates();
        Sequence seq;
        for (int i = 0; i < aggregates.length; i++) {
           seq=((AxisLoader)aggregates[i]).
                getNucleotideSeq(genomicOID, nucleotideRange, gappedSequence);
           if (seq != null) return seq;
         }
         return new UnknownSequence(Sequence.KIND_DNA, nucleotideRange.getMagnitude());
    }

/**
 * These should not be here as it is abstract
 */
/*  protected String getMethodNameForAggregates(){
     return "getGenomicAxis";
  }

  protected Class[] getParameterTypesForAggregates(){
     return new Class[0];
  };

  protected  Object[] getParametersForAggregates(){
     return new Object[0];
  }*/
}