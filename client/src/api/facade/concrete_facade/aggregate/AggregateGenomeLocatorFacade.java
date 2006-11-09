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

import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.genetics.GenomeLocatorFacade;
import api.facade.facade_mgr.FacadeManager;

import java.util.ArrayList;

public class AggregateGenomeLocatorFacade extends AggregateFacadeBase implements GenomeLocatorFacade {


    /**
     * Keeps the array of facade managers available for later consultation.
     */
    public AggregateGenomeLocatorFacade() {
        super(true);
        super.getAggregateReturningMethod();
    } // End constructor

//  /**
//   * Instructs the genome locator to cache all known genome versions
//   */
//  public void cacheGenomeVersions(boolean cacheDefaultGenVerOnly) throws java.rmi.RemoteException
//  {
//       Object[] aggregates=getAggregates();
//        for (int i=0;i<aggregates.length;i++) {
//           ((GenomeLocatorFacade)aggregates[i]).cacheGenomeVersions(cacheDefaultGenVerOnly);
//        }
//  }
//
//  /**
//   * Insructs the genome locator to flush all cashed genome version.
//   */
//  public void flushCachedGenomeVersions() throws java.rmi.RemoteException
//  {
//    Object[] aggregates=getAggregates();
//    for (int i=0;i<aggregates.length;i++) {
//      ((GenomeLocatorFacade)aggregates[i]).flushCachedGenomeVersions();
//    }
//  }

    /**
     * Returns an array of GenomeVersion objects that define the available
     * genome versions for the user.
     */
    public GenomeVersion[] getAvailableGenomeVersions() /*throws java.rmi.RemoteException*/ {
        Object[] aggregates=getAggregates();
        ArrayList rtnList = new ArrayList(aggregates.length);
        GenomeVersion[] tmpArray = null;
        int finalSize = 0;
        for (int i = 0; i < aggregates.length; i++) {
          tmpArray=((GenomeLocatorFacade)
                      aggregates[i]).getAvailableGenomeVersions();
          if (tmpArray != null) {
              rtnList.add(tmpArray);
              finalSize += tmpArray.length;
          }
        }
        tmpArray = new GenomeVersion[finalSize];
        int offset = 0;
        rtnList.trimToSize();
        for (int i = 0; i < rtnList.size(); i++) {
            System.arraycopy((GenomeVersion[]) rtnList.get(i), 0, tmpArray, offset, ((GenomeVersion[]) rtnList.get(i)).length);
            offset += ((GenomeVersion[]) rtnList.get(i)).length;
        }
        return tmpArray;
    }

    /**
     * Returns the latest genome version for the "registered" species.
     */
    public GenomeVersion latestGenomeForSpecies(String speciesName) /*throws java.rmi.RemoteException*/ {
        Object[] aggregates=getAggregates();
        GenomeVersion genomeVersion;
        for (int i=0;i<aggregates.length;i++) {
           genomeVersion=((GenomeLocatorFacade)aggregates[i]).latestGenomeForSpecies(speciesName);
           if (genomeVersion!=null) return genomeVersion;
        }
        return null;
    }

    /**
     * Returns the genome whose number is given as input.
     */
    public GenomeVersion getNthGenomeForSpecies(String speciesName, long versionNumber) /*throws java.rmi.RemoteException */{
        Object[] aggregates=getAggregates();
        GenomeVersion genomeVersion;
        for (int i=0;i<aggregates.length;i++) {
           genomeVersion=((GenomeLocatorFacade)aggregates[i]).getNthGenomeForSpecies(speciesName, versionNumber);
           if (genomeVersion!=null) return genomeVersion;
        }
        return null;
    } // End method: getNthGenomeForSpecies

    protected String getMethodNameForAggregates(){
       return "getGenomeLocator";
    }

  protected Class[] getParameterTypesForAggregates(){
     return new Class[0];
  };

  protected  Object[] getParametersForAggregates(){
     return new Object[0];
  }

    /**
     * Overridden as if the number of protocols drops to 0, we need a
     * new species.  Therefore we become invalid and stop listening.
     */
    public void protocolRemovedFromInUseList(String protocol) {
       super.protocolRemovedFromInUseList(protocol);
       if (numAggregates()==0) FacadeManager.removeInUseProtocolListener(this);
    }
} // End class: AggregateGenomeLocatorFacade
