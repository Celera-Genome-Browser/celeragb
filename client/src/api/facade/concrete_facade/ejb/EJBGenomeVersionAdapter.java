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

import api.entity_model.access.report.GenomeVersionAlignmentReport;
import api.entity_model.access.report.PropertyReport;
import api.entity_model.access.report.SubjectSequenceReport;
import api.entity_model.model.genetics.Chromosome;
import api.facade.abstract_facade.genetics.GenomeVersionLoader;
import api.stub.data.FatalCommError;
import api.stub.data.InvalidPropertyFormat;
import api.stub.data.NavigationPath;
import api.stub.data.NoData;
import api.stub.data.OID;


public class EJBGenomeVersionAdapter extends EJBGenomicFacadeAdapter
  implements GenomeVersionLoader {

   public EJBGenomeVersionAdapter(RemoteInterfacePool aPool, int ejbRetries) {
    super(aPool, ejbRetries);
   }

   public Chromosome[] listChromosomes(long genomeVersion) throws NoData
   {
      api.stub.ejb.model.genetics.GenomeVersion concrete = null;
      Chromosome[] data;
      int tries=0;
      try {
         while ( true ) {
            try {
               concrete=(api.stub.ejb.model.genetics.GenomeVersion)aPool.getInterface();
               data=concrete.listChromosomes(genomeVersion);
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

   public GenomeVersionAlignmentReport generateAlignmentReportForEntity(OID entityOID) {
      if (!shouldMakeServerCall(entityOID)) return null;
      api.stub.ejb.model.genetics.GenomeVersion concrete = null;
      GenomeVersionAlignmentReport data;
      int tries=0;
      try {
         while ( true ) {
            try {
               concrete=(api.stub.ejb.model.genetics.GenomeVersion)aPool.getInterface();
               data=concrete.generateAlignmentReportForEntity(entityOID);
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

   public PropertyReport generatePropertyReport(OID genomeVerOID, OID [] featureOIDs, String [] propNames ) {
      if (!shouldMakeServerCall(featureOIDs[0])) return null;

      api.stub.ejb.model.genetics.GenomeVersion concrete = null;
      PropertyReport fpr;
      int tries = 0;
      try {
         while ( true ) {
            try {
               concrete=( api.stub.ejb.model.genetics.GenomeVersion )aPool.getInterface();
               fpr = concrete.generatePropertyReport( genomeVerOID, featureOIDs, propNames );
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
         return ( fpr );
      }
      catch ( java.rmi.RemoteException ex ) {
         throw new FatalCommError( ex.getMessage() );
      }
      finally {
         aPool.freeInterface( concrete );
      }
   }


   public SubjectSequenceReport generateSubjectSequenceReportForEntity( OID entityOID ) {
      if (!shouldMakeServerCall(entityOID)) return null;

      api.stub.ejb.model.genetics.GenomeVersion concrete = null;
      SubjectSequenceReport fpr;
      int tries = 0;
      try {
         while ( true ) {
            try {
               concrete=( api.stub.ejb.model.genetics.GenomeVersion )aPool.getInterface();
               fpr = concrete.generateSubjectSequenceReportForEntity( entityOID );
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
         return ( fpr );
      }
      catch ( java.rmi.RemoteException ex ) {
         throw new FatalCommError( ex.getMessage() );
      }
      finally {
         aPool.freeInterface( concrete );
      }
   }

  public  NavigationPath[] getNavigationPath
    (OID speciesOID,
     String targetType,
     String target)
    throws InvalidPropertyFormat
  {
     api.stub.ejb.model.genetics.GenomeVersion concrete = null;
     NavigationPath[] data;
     int tries=0;
     try {
        while (true) {
           try {
             concrete=(api.stub.ejb.model.genetics.GenomeVersion)aPool.getInterface();
             data=concrete.getNavigationPath(speciesOID, targetType, target);
             break;
           }
           catch (java.rmi.RemoteException ex) {
             tries++;
             if (tries==ejbRetries) throw ex;
             else {
               aPool.returnBadInterface(concrete);
               aPool.refreshInterfaces();
             }
           }
        }
        return data;
     }
     catch (InvalidPropertyFormat ipfEx) {throw ipfEx; }
     catch (java.rmi.RemoteException ex) {
        throw new FatalCommError(ex.getMessage());
     }
     finally {
       aPool.freeInterface(concrete);
     }
  }

  public  NavigationPath[] getNavigationPath
    (String targetType,
     String target)
    throws InvalidPropertyFormat
  {
     api.stub.ejb.model.genetics.GenomeVersion concrete = null;
     NavigationPath[] data;
     int tries=0;
     try {
        while (true) {
           try {
             concrete=(api.stub.ejb.model.genetics.GenomeVersion)aPool.getInterface();
             data=concrete.getNavigationPath(targetType, target);
             break;
           }
           catch (java.rmi.RemoteException ex) {
             tries++;
             if (tries==ejbRetries) throw ex;
             else {
               aPool.returnBadInterface(concrete);
               aPool.refreshInterfaces();
             }
           }
        }
        return data;
     }
     catch (InvalidPropertyFormat ipfEx) {throw ipfEx; }
     catch (java.rmi.RemoteException ex) {
        throw new FatalCommError(ex.getMessage());
     }
     finally {
       aPool.freeInterface(concrete);
     }
  }

  public  String getNavigationVocabIndex()
  {
     api.stub.ejb.model.genetics.GenomeVersion concrete = null;
     String data;
     int tries=0;
     try {
        while (true) {
           try {
             concrete=(api.stub.ejb.model.genetics.GenomeVersion)aPool.getInterface();
             data=concrete.getNavigationVocabIndex();
             break;
           }
           catch (java.rmi.RemoteException ex) {
             tries++;
             if (tries==ejbRetries) throw ex;
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
}
