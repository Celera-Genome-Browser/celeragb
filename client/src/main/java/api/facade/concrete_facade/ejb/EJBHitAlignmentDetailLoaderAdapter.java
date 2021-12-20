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

import api.stub.data.FatalCommError;
import api.stub.data.NoData;
import api.stub.data.OID;
import api.stub.data.SubjectDefinition;
import api.stub.ejb.model.annotations.HitAlignmentDetailRemote;

public class EJBHitAlignmentDetailLoaderAdapter extends EJBFeatureFacadeAdapter implements api.facade.abstract_facade.annotations.HitAlignmentDetailLoader {

  public EJBHitAlignmentDetailLoaderAdapter( RemoteInterfacePool aPool,int ejbRetries ) {
    super ( aPool, ejbRetries );
  }


  public  String getQueryAlignedResidues( OID alignmentChildOID ) throws NoData {
    
    HitAlignmentDetailRemote  concrete  = null;
    String                    data      = null;
    int                       tries     = 0;
    
    if ( !shouldMakeServerCall( alignmentChildOID ) ) {
      return ( null );
    }

    try {
      while ( true ) {
        try {
          concrete = ( HitAlignmentDetailRemote )aPool.getInterface();
          data = concrete.getQueryAlignedResidues( alignmentChildOID );
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


  public  String getSubjectAlignedResidues( OID alignmentChildOID ) throws NoData {

    HitAlignmentDetailRemote  concrete  = null;
    String                    data      = null;
    int                       tries     = 0;
    
    if ( !shouldMakeServerCall( alignmentChildOID ) ) {
      return ( null );
    }

    try {
      while ( true ) {
        try {
          concrete = ( HitAlignmentDetailRemote )aPool.getInterface();
          data = concrete.getSubjectAlignedResidues( alignmentChildOID );
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


  public SubjectDefinition [] getSubjectDefinitions( OID featureOID ) throws NoData {
    
    HitAlignmentDetailRemote  concrete  = null;
    SubjectDefinition []      data      = null;
    int                       tries     = 0;
    
    if ( !shouldMakeServerCall( featureOID ) ) {
      return ( null );
    }

    try {
      while ( true ) {
        try {
          concrete = ( HitAlignmentDetailRemote )aPool.getInterface();
          data = concrete.getSubjectDefinitions( featureOID );
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
