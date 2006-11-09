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
import api.stub.data.FeatureIndex;
import api.stub.data.OID;
import api.stub.ejb.model.annotations.FeatureIndexLibrary;

import java.math.BigDecimal;

public class EJBFeatureIndexLibraryAdapter implements api.facade.abstract_facade.annotations.FeatureIndexLibrary {

  private RemoteInterfacePool aPool;
  private int ejbRetries;

  public EJBFeatureIndexLibraryAdapter( RemoteInterfacePool aPool, int ejbRetries ) {
    this.aPool = aPool;
    this.ejbRetries = ejbRetries;
  }

  public  FeatureIndex generateIndexForType( OID speciesOid, EntityType aType, BigDecimal assemblyVersion ) {
    FeatureIndexLibrary concrete = null;
    FeatureIndex data;
    int tries = 0;
    try {
      while ( true ) {
        try {
          concrete = ( FeatureIndexLibrary )aPool.getInterface();
          data=concrete.generateIndexForType( speciesOid, aType, assemblyVersion );
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


  public void setTranscriptOIDMapping( OID oid, String accession ) {
    throw new UnsupportedOperationException( "This type of facade manager does not support storing of oid to transcript accession number mappings" );
  }


  public void setGeneOIDMapping(OID oid, String accession) {
    throw new UnsupportedOperationException("This type of facade manager does not support storing of oid to gene accession number mappings" );
  }
}

