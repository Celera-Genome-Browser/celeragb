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

import api.entity_model.model.genetics.GenomeVersion;
import api.stub.data.FatalCommError;

public class EJBGenomeLocatorAdapter
  implements api.facade.abstract_facade.genetics.GenomeLocatorFacade
{
  private RemoteInterfacePool aPool = null;
  private int ejbRetries;

  public EJBGenomeLocatorAdapter(RemoteInterfacePool aPool,int ejbRetries)
  {
    this.aPool = aPool;
    this.ejbRetries=ejbRetries;
  }


  public GenomeVersion[] getAvailableGenomeVersions()
  {
     api.stub.ejb.model.genetics.GenomeLocatorRemote concrete = null;
     GenomeVersion[] data;
     int tries=0;
     try {
        while (true) {
           try {
             concrete=(api.stub.ejb.model.genetics.GenomeLocatorRemote)aPool.getInterface();
             data=concrete.getAvailableGenomeVersions();
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


  public GenomeVersion latestGenomeForSpecies(String speciesName)
  {
     api.stub.ejb.model.genetics.GenomeLocatorRemote concrete = null;
     GenomeVersion data;
     int tries=0;
     try {
        while (true) {
           try {
             concrete=(api.stub.ejb.model.genetics.GenomeLocatorRemote)aPool.getInterface();
             data=concrete.latestGenomeForSpecies(speciesName);
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

  public GenomeVersion getNthGenomeForSpecies(String speciesName, long versionNumber)
  {
     api.stub.ejb.model.genetics.GenomeLocatorRemote concrete = null;
     GenomeVersion data;
     int tries=0;
     try {
        while (true) {
           try {
             concrete=(api.stub.ejb.model.genetics.GenomeLocatorRemote)aPool.getInterface();
             data=concrete.getNthGenomeForSpecies(speciesName, versionNumber);
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
