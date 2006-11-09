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
package api.facade.concrete_facade.ejb;

import api.entity_model.model.fundtype.EntityType;
import api.stub.data.FatalCommError;
import api.stub.data.NoData;
import api.stub.data.OID;
import api.stub.data.SubjectDefinition;
import api.stub.ejb.model.annotations.HitAlignmentFacade;
import api.stub.sequence.Sequence;


public class EJBHitAlignmentFacadeAdapter extends EJBFeatureFacadeAdapter implements api.facade.abstract_facade.annotations.HitAlignmentFacade {

   public EJBHitAlignmentFacadeAdapter( RemoteInterfacePool aPool,int ejbRetries ) {
      super ( aPool, ejbRetries );
   }

   public  SubjectDefinition[] getSubjectDefinitions( OID featureOID ) throws NoData {
      if ( !shouldMakeServerCall(featureOID) ) return (null);
      HitAlignmentFacade concrete = null;
      SubjectDefinition[] data;
      int tries=0;
      try {
         while ( true ) {
            try {
               concrete=(HitAlignmentFacade)aPool.getInterface();
               data=concrete.getSubjectDefinitions(featureOID);
               break;
            }
            catch ( java.rmi.RemoteException ex ) {
               tries++;
               if ( tries==ejbRetries ) throw ex;
               else {
                  aPool.returnBadInterface(concrete);
                  aPool.refreshInterfaces();
               }
            }
         }
         return (data);
      }
      catch ( java.rmi.RemoteException ex ) {
         throw new FatalCommError(ex.getMessage());
      }
      finally {
         aPool.freeInterface(concrete);
      }
   }

   public Sequence getSubjectSequence( OID featureOID, EntityType entityType ) throws NoData {

      if ( !shouldMakeServerCall( featureOID ) ) {
         return ( null );
      }
      HitAlignmentFacade concrete = null;
      Sequence data;
      int tries = 0;
      try {
         while ( true ) {
            try {
               concrete = ( HitAlignmentFacade )aPool.getInterface();
               data = concrete.getSubjectSequence( featureOID, entityType );
               break;
            }
            catch ( java.rmi.RemoteException ex ) {
               tries++;
               if ( tries == ejbRetries ) {
                  throw ex;
               }
               else {
                  aPool.returnBadInterface( concrete );
                  aPool.refreshInterfaces();
               }
            }
         }
         return ( data );
      }
      catch ( java.rmi.RemoteException ex ) {
         throw new FatalCommError( ex.getMessage() );
      }
      finally {
         aPool.freeInterface( concrete );
      }
   }
}
