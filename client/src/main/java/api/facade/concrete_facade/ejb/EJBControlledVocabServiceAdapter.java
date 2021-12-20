// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package api.facade.concrete_facade.ejb;

import api.facade.facade_mgr.FacadeManager;
import api.stub.data.*;
import api.stub.ejb.model.genetics.ControlledVocabService;

public class EJBControlledVocabServiceAdapter implements api.facade.abstract_facade.genetics.ControlledVocabService
{
  private RemoteInterfacePool aPool;
  private int ejbRetries;

  public EJBControlledVocabServiceAdapter(RemoteInterfacePool aPool,int ejbRetries)
  {
    this.aPool = aPool;
    this.ejbRetries=ejbRetries;
  }

  public  ControlledVocabElement[] getControlledVocab
    (OID entityOID, String vocabIndex)
    throws NoData
  {
     ControlledVocabService concrete = null;
     ControlledVocabElement[] data;
     int tries=0;
     try {
        while (true) {
           try {
             concrete=(ControlledVocabService)aPool.getInterface();
             if (shouldUseOIDInServerCall(entityOID))
                data=concrete.getControlledVocab(entityOID, vocabIndex);
             else
                data=concrete.getControlledVocab(null, vocabIndex);
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

  protected boolean shouldUseOIDInServerCall (OID oid) {
     if (oid==null) return false;
     if (oid.isScratchOID()) return false;
     GenomeVersionInfo versionForOid = FacadeManager.getGenomeVersionInfo(oid.getGenomeVersionId());
     if (versionForOid == null)
     {
        // Used to use this test to handle calls in the server where the
        // genome versions had not been "registered" with the facade manager.
        // Genome versions are now automatically registered at server statup.
        // The only other time this case might be hit is during annotation when
        // a new object is created but not yet assigned to a genome version. In
        // this case we definately should not be making a server side call so
        // return false.
        return false;
     }
     else
     {
        return versionForOid.isDatabaseDataSource();
     }
  }
}
