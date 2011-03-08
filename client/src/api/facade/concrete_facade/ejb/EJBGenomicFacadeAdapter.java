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

import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.fundtype.EntityType;
import api.facade.abstract_facade.fundtype.GenomicEntityLoader;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.*;
import api.stub.ejb.model.fundtype.GenomicFacade;

public abstract class EJBGenomicFacadeAdapter implements GenomicEntityLoader {
   protected RemoteInterfacePool aPool = null;
   protected int ejbRetries;

   public EJBGenomicFacadeAdapter(RemoteInterfacePool aPool, int ejbRetries) {
      this.aPool = aPool;
      this.ejbRetries = ejbRetries;
   }

   public Alignment[] getAlignmentsToAxes(OID entityOID) {
      GenomicFacade concrete = null;
      Alignment[] data;
      int tries = 0;
      try {
         while (true) {
            try {
               concrete = (GenomicFacade) aPool.getInterface();
               data = concrete.getAlignmentsToAxes(entityOID);
               break;
            }
            catch (java.rmi.RemoteException ex) {
               tries++;
               if (tries == ejbRetries)
                  throw ex;
               else {
                  aPool.returnBadInterface(concrete);
                  aPool.refreshInterfaces();
               }
            }
         }
         return data;
      }
      catch (java.rmi.RemoteException ex) {
         throw new FatalCommError(ex.getMessage());
      }
      finally {
         aPool.freeInterface(concrete);
      }
   }

   public GenomicProperty[] getProperties(OID genomicOID, EntityType dynamicType, boolean deepLoad) {
      if (!shouldMakeServerCall(genomicOID))
         return null;
      GenomicFacade concrete = null;
      GenomicProperty[] data;
      int tries = 0;
      try {
         while (true) {
            try {
               concrete = (GenomicFacade) aPool.getInterface();
               data = concrete.getProperties(genomicOID, dynamicType, deepLoad);
               break;
            }
            catch (java.rmi.RemoteException ex) {
               tries++;
               if (tries == ejbRetries)
                  throw ex;
               else {
                  aPool.returnBadInterface(concrete);
                  aPool.refreshInterfaces();
               }
            }
         }
         return data;
      }
      catch (java.rmi.RemoteException ex) {
         throw new FatalCommError(ex.getMessage());
      }
      finally {
         aPool.freeInterface(concrete);
      }
   }

   public GenomicProperty[] expandProperty(OID genomicOID, String propertyName, EntityType dynamicType, boolean deepLoad) throws NoData {
      if (!shouldMakeServerCall(genomicOID))
         return null;
      GenomicFacade concrete = null;
      GenomicProperty[] data;
      int tries = 0;
      try {
         while (true) {
            try {
               concrete = (GenomicFacade) aPool.getInterface();
               data = concrete.expandProperty(genomicOID, propertyName, dynamicType, deepLoad);
               break;
            }
            catch (java.rmi.RemoteException ex) {
               tries++;
               if (tries == ejbRetries)
                  throw ex;
               else {
                  aPool.returnBadInterface(concrete);
                  aPool.refreshInterfaces();
               }
            }
         }
         return data;
      }
      catch (java.rmi.RemoteException ex) {
         throw new FatalCommError(ex.getMessage());
      }
      finally {
         aPool.freeInterface(concrete);
      }
   }

   public GenomicEntityAlias[] getAliases(OID featureOID) throws NoData {
      if (!shouldMakeServerCall(featureOID))
         return null;
      GenomicFacade concrete = null;
      GenomicEntityAlias[] data;
      int tries = 0;
      try {
         while (true) {
            try {
               concrete = (GenomicFacade) aPool.getInterface();
               data = concrete.getAliases(featureOID);
               break;
            }
            catch (java.rmi.RemoteException ex) {
               tries++;
               if (tries == ejbRetries)
                  throw ex;
               else {
                  aPool.returnBadInterface(concrete);
                  aPool.refreshInterfaces();
               }
            }
         }
         return data;
      }
      catch (java.rmi.RemoteException ex) {
         throw new FatalCommError(ex.getMessage());
      }
      finally {
         aPool.freeInterface(concrete);
      }
   }

   public GenomicEntityComment[] getComments(OID featureOID) throws NoData {
      if (!shouldMakeServerCall(featureOID))
         return null;
      GenomicFacade concrete = null;
      GenomicEntityComment[] data;
      int tries = 0;
      try {
         while (true) {
            try {
               concrete = (GenomicFacade) aPool.getInterface();
               data = concrete.getComments(featureOID);
               break;
            }
            catch (java.rmi.RemoteException ex) {
               tries++;
               if (tries == ejbRetries)
                  throw ex;
               else {
                  aPool.returnBadInterface(concrete);
                  aPool.refreshInterfaces();
               }
            }
         }
         return data;
      }
      catch (java.rmi.RemoteException ex) {
         throw new FatalCommError(ex.getMessage());
      }
      finally {
         aPool.freeInterface(concrete);
      }
   }

   /**
    * only call server if proper OID.
    */
   protected boolean shouldMakeServerCall(OID oid) {
      if (oid.isScratchOID())
         return false;
      if (!oid.isInternalDatabaseOID())
         return false;
      GenomeVersionInfo versionForOid = FacadeManager.getGenomeVersionInfo(oid.getGenomeVersionId());
      if (versionForOid == null) {
         // Used to use this test to handle calls in the server where the
         // genome versions had not been "registered" with the facade manager.
         // Genome versions are now automatically registered at server statup.
         // The only other time this case might be hit is during annotation when
         // a new object is created but not yet assigned to a genome version. In
         // this case we definately should not be making a server side call so
         // return false.
         return false;
      }
      else {
         return versionForOid.isDatabaseDataSource();
      }
   }
}
