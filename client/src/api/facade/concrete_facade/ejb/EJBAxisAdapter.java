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
import api.entity_model.model.fundtype.LoadRequest;
import api.facade.abstract_facade.fundtype.AxisLoader;
import api.stub.data.FatalCommError;
import api.stub.data.OID;
import api.stub.ejb.model.fundtype.AxisRemote;
import api.stub.geometry.Range;
import api.stub.sequence.Sequence;


public abstract class EJBAxisAdapter extends EJBGenomicFacadeAdapter
 implements AxisLoader
{

  public EJBAxisAdapter(RemoteInterfacePool aPool,int ejbRetries)
  {
    super(aPool,ejbRetries);
  }

  public Alignment[] loadAlignmentsToEntities(OID entityOID, LoadRequest loadRequest)
  {
     if (!shouldMakeServerCall(entityOID)) return null;
     AxisRemote concrete = null;
     Alignment[] data;
     int tries=0;
     try {
        while (true) {
           try {
             concrete=(AxisRemote)aPool.getInterface();
             data=concrete.loadAlignmentsToEntities(entityOID, loadRequest);
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

  public Sequence getNucleotideSeq
    (OID genomicOID,
     Range nucleotideRange,
     boolean gappedSequence)
  {
     if (!shouldMakeServerCall(genomicOID)) return null;
     AxisRemote concrete = null;
     Sequence data;
     int tries=0;
     try {
        while (true) {
           try {
             concrete=(AxisRemote)aPool.getInterface();
             data=concrete.getNucleotideSeq(genomicOID, nucleotideRange, gappedSequence);
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
