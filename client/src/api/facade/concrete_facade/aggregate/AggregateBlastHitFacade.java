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
 *
 * Used to be called AggregateBlastHitFacade
 */

package api.facade.concrete_facade.aggregate;

import api.entity_model.model.fundtype.EntityType;
import api.facade.abstract_facade.annotations.BlastHitFacade;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;

public class AggregateBlastHitFacade extends AggregateHitAlignmentFacade implements BlastHitFacade {
   static private Object[] parameters=new Object[] {EntityType.getEntityTypeForValue(EntityTypeConstants.BlastN_Hit)};

    /*public String getRawAlignmentText(OID featureOID) throws BlastHitError, NoData {
        Object[] aggregates=getAggregates();
        String tmp = null;
         for (int i = 0; i < aggregates.length; i++) {
           try {
             tmp=((BlastHitFacade)aggregates[i]).
                  getRawAlignmentText(featureOID);
           }
           catch (Exception ex) {}
           if (tmp != null && !tmp.equals("")) return tmp;
         }
         throw new NoData();
    }*/

    protected String getMethodNameForAggregates() {
       return "getFacade";
    };

    protected Class[] getParameterTypesForAggregates() {
       return new Class[]{EntityType.class};
    }

    protected Object[] getParametersForAggregates() {
      return parameters;
    };

}
