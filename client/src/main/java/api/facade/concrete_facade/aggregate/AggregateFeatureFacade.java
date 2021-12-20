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
 * CVS_ID:  $Id$
 */

package api.facade.concrete_facade.aggregate;

import api.facade.abstract_facade.annotations.FeatureFacade;
import api.facade.abstract_facade.assembly.ContigFacade;
import api.stub.data.CurationUpdateError;
import api.stub.data.NoData;
import api.stub.data.OID;
import api.stub.data.ReplacementRelationship;

import java.util.ArrayList;

public abstract class AggregateFeatureFacade extends AggregateGenomicFacade implements FeatureFacade {
    private static boolean DEBUG_CLASS = false;

    public AggregateFeatureFacade (boolean delayMethodRetrival) {
        super (delayMethodRetrival);
    }

    public AggregateFeatureFacade () {}

    public ContigFacade getContig(OID featureOID, OID contigOID) {
        return null;
    }

    public void delete(OID featureOID) throws CurationUpdateError {
        //TODO: Implement this api.facade.abstract_facade.annotations.FeatureFacade method
    }

    public void deleteEvidence(OID featureOID, OID evidenceCurationOID) throws CurationUpdateError {
        //TODO: Implement this api.facade.abstract_facade.annotations.FeatureFacade method
    }

    public OID[] retrieveEvidence(OID featureOID) throws NoData {
        Object[] aggregates=getAggregates();
        ArrayList rtnList = new ArrayList(aggregates.length);
        OID[] tmpArray = null;
        int finalSize = 0;
        for (int i = 0; i < aggregates.length; i++) {
          try {
            tmpArray=((FeatureFacade)
                      aggregates[i]).retrieveEvidence(featureOID);
          }
          catch (NoData ndEx) {
            tmpArray = null;
          }
          if (tmpArray != null) {
              rtnList.add(tmpArray);
              finalSize += tmpArray.length;
          }
        }
        if (finalSize==0) throw new NoData();
        tmpArray = new OID[finalSize];
        int offset = 0;
        rtnList.trimToSize();
        for (int i = 0; i < rtnList.size(); i++) {
            System.arraycopy((OID[]) rtnList.get(i), 0, tmpArray, offset, ((OID[]) rtnList.get(i)).length);
            offset += ((OID[]) rtnList.get(i)).length;
        }
        return tmpArray;
    }

  public ReplacementRelationship retrieveReplacedFeatures
    (OID featureOID, long assemblyVersionOfReplacedFeatures)
    throws NoData
  {
        if (DEBUG_CLASS) System.out.println("In AggregateFeatureFacade.retrieveReplacedFeatures(OID, long);");
        Object[] aggregates=getAggregates();
//        ArrayList rtnList = new ArrayList(aggregates.length);
        ReplacementRelationship replRltnp = null;
//        ReplacementRelationship[] replRltnpArray = null;
//        int finalSize = 0;
        for (int i = 0; i < aggregates.length; i++) {
          if (DEBUG_CLASS) System.out.println("Aggregate call [" + i + "]..retrieveReplacedFeatures(featureOID,assemblyVersion);");
          try {
            replRltnp =((FeatureFacade) aggregates[i]).retrieveReplacedFeatures(featureOID,assemblyVersionOfReplacedFeatures);
          }
          catch (NoData ndEx) {}
          //Stop on the first successful call (one that returns data)
          if (replRltnp != null) {
            break;
          }
        }

        if (replRltnp == null) throw new NoData();
        //replRltnpArray = new ReplacementRelationship[finalSize];
//        replRltnpArray = (ReplacementRelationship[]) rtnList.toArray();
/*        if (finalSize==0) throw new NoData();
        replRltnpArray = new ReplacementRelationship[finalSize];
        int offset = 0;
        rtnList.trimToSize();
        for (int i = 0; i < rtnList.size(); i++) {
            System.arraycopy((ReplacementRelationship[]) rtnList.get(i), 0, tmpArray, offset, ((ReplacementRelationship[]) rtnList.get(i)).length);
            offset += ((OID[]) rtnList.get(i)).length;
        }*/

        return replRltnp;
  }

}
