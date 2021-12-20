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
import api.facade.abstract_facade.annotations.HitAlignmentFacade;
import api.stub.data.NoData;
import api.stub.data.OID;
import api.stub.data.SubjectDefinition;
import api.stub.sequence.Sequence;
import api.stub.sequence.UnknownSequence;

import java.util.ArrayList;

public abstract class AggregateHitAlignmentFacade extends AggregateFeatureFacade implements HitAlignmentFacade {

   public SubjectDefinition[] getSubjectDefinitions(OID featureOID) throws NoData {
      Object[] aggregates=getAggregates();
      ArrayList rtnList = new ArrayList(aggregates.length);
      SubjectDefinition[] tmpArray = null;
      int finalSize = 0;
      for ( int i = 0; i < aggregates.length; i++ ) {
         try {
            tmpArray=((HitAlignmentFacade)
                      aggregates[i]).getSubjectDefinitions(featureOID);
         }
         catch ( NoData ndEx ) {
            // Reset its value so that items are not double-counted
            tmpArray = null;
         }
         if ( tmpArray != null ) {
            rtnList.add(tmpArray);
            finalSize += tmpArray.length;
         }
      }
      if ( finalSize==0 ) throw new NoData();
      tmpArray = new SubjectDefinition[finalSize];
      int offset = 0;
      rtnList.trimToSize();
      for ( int i = 0; i < rtnList.size(); i++ ) {
         System.arraycopy((SubjectDefinition[]) rtnList.get(i), 0, tmpArray, offset, ((SubjectDefinition[]) rtnList.get(i)).length);
         offset += ((SubjectDefinition[]) rtnList.get(i)).length;
      }
      return (tmpArray);
   }


   public Sequence getSubjectSequence( OID featureOID, EntityType entityType ) throws NoData {

      Object []   aggregates  = this.getAggregates();
      Sequence subjSeq     = null;

      for ( int aggregateNum = 0; aggregateNum < aggregates.length; aggregateNum++ ) {
         subjSeq = ( ( HitAlignmentFacade )aggregates[ aggregateNum ] ).getSubjectSequence( featureOID, entityType );
         if ( subjSeq != null ) {
            return ( subjSeq );
         }
      }
      return ( new UnknownSequence(Sequence.KIND_DNA, 0) );
   }
}
