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

import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.fundtype.EntityType;
import api.facade.abstract_facade.fundtype.GenomicEntityLoader;
import api.stub.data.GenomicEntityAlias;
import api.stub.data.GenomicEntityComment;
import api.stub.data.GenomicProperty;
import api.stub.data.NoData;
import api.stub.data.OID;

import java.util.ArrayList;

public abstract class AggregateGenomicFacade extends AggregateFacadeBase implements GenomicEntityLoader {

    public AggregateGenomicFacade (boolean delayMethodRetrival) {
        super (delayMethodRetrival);
    }

    public AggregateGenomicFacade () {}

    public Alignment[] getAlignmentsToAxes(OID entityOID)
    {
      throw new RuntimeException("AggregateGenomicFacade::getAlignmentsToAxes(Not implemented)");
    }

    public GenomicProperty[] getProperties(OID genomicOID, EntityType dynamicType, boolean deepLoad) {
        Object[] aggregates=getAggregates();
        ArrayList rtnList = new ArrayList(aggregates.length);
        GenomicProperty[] tmpArray = null;
        int finalSize = 0;
        for (int i = 0; i < aggregates.length; i++) {

             tmpArray=((GenomicEntityLoader)
                      aggregates[i]).getProperties(genomicOID, dynamicType, deepLoad);
          if (tmpArray != null) {
              rtnList.add(tmpArray);
              finalSize += tmpArray.length;
              tmpArray = null;
          }
        }
      //  if (finalSize==0) throw new NoData(); //if all facades return NoData, throw it
        tmpArray = new GenomicProperty[finalSize];
        int offset = 0;
        rtnList.trimToSize();
        for (int i = 0; i < rtnList.size(); i++) {
            System.arraycopy((GenomicProperty[]) rtnList.get(i), 0, tmpArray, offset, ((GenomicProperty[]) rtnList.get(i)).length);
            offset += ((GenomicProperty[]) rtnList.get(i)).length;
        }
        return tmpArray;
    }

    public GenomicProperty[] expandProperty(OID genomicOID, String propertyName, EntityType dynamicType, boolean deepLoad) throws NoData {
        Object[] aggregates=getAggregates();
        ArrayList rtnList = new ArrayList(aggregates.length);
        GenomicProperty[] tmpArray = null;
        int finalSize = 0;
        for (int i = 0; i < aggregates.length; i++) {
          try {
             tmpArray=((GenomicEntityLoader)
                      aggregates[i]).expandProperty(genomicOID, propertyName, dynamicType, deepLoad);
          }
          catch (NoData ndEx) {
            tmpArray = null;
            //do nothing here as any 1 facade might throw a NoData
          }
          if (tmpArray != null) {
              rtnList.add(tmpArray);
              finalSize += tmpArray.length;
          }
        }
        if (finalSize==0) throw new NoData(); //if all facades return NoData, throw it
        tmpArray = new GenomicProperty[finalSize];
        int offset = 0;
        rtnList.trimToSize();
        for (int i = 0; i < rtnList.size(); i++) {
            System.arraycopy((GenomicProperty[]) rtnList.get(i), 0, tmpArray, offset, ((GenomicProperty[]) rtnList.get(i)).length);
            offset += ((GenomicProperty[]) rtnList.get(i)).length;
        }
        return tmpArray;
    }

    public GenomicEntityAlias[] getAliases(OID featureOID) throws NoData {
        Object[] aggregates=getAggregates();
        ArrayList rtnList = new ArrayList(aggregates.length);
        GenomicEntityAlias[] tmpArray = null;
        int finalSize = 0;
        for (int i = 0; i < aggregates.length; i++) {
          try {
            tmpArray=((GenomicEntityLoader)
                      aggregates[i]).getAliases(featureOID);
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
        tmpArray = new GenomicEntityAlias[finalSize];
        int offset = 0;
        rtnList.trimToSize();
        for (int i = 0; i < rtnList.size(); i++) {
            System.arraycopy((GenomicEntityAlias[]) rtnList.get(i), 0, tmpArray, offset, ((GenomicEntityAlias[]) rtnList.get(i)).length);
            offset += ((GenomicEntityAlias[]) rtnList.get(i)).length;
        }
        return tmpArray;
    }

    public GenomicEntityComment[] getComments(OID featureOID) throws NoData {
        Object[] aggregates=getAggregates();
        ArrayList rtnList = new ArrayList(aggregates.length);
        GenomicEntityComment[] tmpArray = null;
        int finalSize = 0;
        for (int i = 0; i < aggregates.length; i++) {
          try {
            tmpArray=((GenomicEntityLoader)
                      aggregates[i]).getComments(featureOID);
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
        tmpArray = new GenomicEntityComment[finalSize];
        int offset = 0;
        rtnList.trimToSize();
        for (int i = 0; i < rtnList.size(); i++) {
            System.arraycopy((GenomicEntityComment[]) rtnList.get(i), 0, tmpArray, offset, ((GenomicEntityComment[]) rtnList.get(i)).length);
            offset += ((GenomicEntityComment[]) rtnList.get(i)).length;
        }
        return tmpArray;
    }
}
