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

import api.entity_model.model.fundtype.EntityType;
import api.facade.abstract_facade.annotations.*;
import api.stub.data.NoData;
import api.stub.data.OID;
import api.stub.data.SubjectDefinition;


public class AggregateHitAlignmentDetailLoader extends AggregateFeatureFacade implements
  Sim4DetailFacade, LapDetailFacade, HSPFacade, GenewiseDetailLoader {

    private EntityType featureType;

    public AggregateHitAlignmentDetailLoader(EntityType featureType){
       super(true);  //Must make these two additional calls to ensure the features are setup in this instance
       this.featureType=featureType;
       getAggregateReturningMethod();  //second addition call
    }

    public String getQueryAlignedResidues(OID alignmentDetailOID) throws NoData {
        Object[] aggregates=getAggregates();
        String tmp;
        for (int i=0;i<aggregates.length;i++) {
            try {
                tmp=((HitAlignmentDetailLoader)aggregates[i]).getQueryAlignedResidues(alignmentDetailOID);
                if ((tmp != null) && (tmp.length() > 0))
                    return tmp;
            }
            catch (NoData ndEx) {
            }
        }
        throw new NoData();
    }

    public String getSubjectAlignedResidues(OID alignmentDetailOID) throws NoData {
        Object[] aggregates=getAggregates();
        String tmp;
        for (int i=0;i<aggregates.length;i++) {
            try {
                tmp=((HitAlignmentDetailLoader)aggregates[i]).getSubjectAlignedResidues(alignmentDetailOID);
                if ((tmp != null) && (tmp.length() > 0))
                    return tmp;
            }
            catch (NoData ndEx) {
            }
        }
        throw new NoData();
    }


    public SubjectDefinition [] getSubjectDefinitions( OID featureOID ) throws NoData {

      Object[]              aggregates  = getAggregates();
      SubjectDefinition []  tmp         = null;

      for ( int i = 0; i < aggregates.length; i++ ) {
        try {
          tmp = ( ( HitAlignmentDetailLoader )aggregates[ i ] ).getSubjectDefinitions( featureOID );
          if ( ( tmp != null ) && ( tmp.length > 0 ) ) {
            return tmp;
          }
        }
        catch ( NoData ndEx ) {
        }
      }
      throw new NoData();
    }

    protected String getMethodNameForAggregates() {
       return "getFacade";
    };

    protected Class[] getParameterTypesForAggregates() {
       return new Class[]{EntityType.class};
    }

    protected Object[] getParametersForAggregates() {
      return new Object[]{featureType};
    };


}
