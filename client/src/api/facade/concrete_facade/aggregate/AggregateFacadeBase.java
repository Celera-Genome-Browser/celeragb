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

import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;
import api.facade.facade_mgr.InUseProtocolListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class AggregateFacadeBase implements InUseProtocolListener {
    private Method methodForAggregates;
    private List aggregateList=new ArrayList();

    public AggregateFacadeBase() {
      getAggregateReturningMethod();
    }

    public AggregateFacadeBase(boolean delayMethodRetrival) {
      if (!delayMethodRetrival) getAggregateReturningMethod();
    }

    protected void getAggregateReturningMethod() {
      try {
//        System.out.println("Constructing new AggFacBase");
        methodForAggregates=findMethodToGetAggregates(getMethodNameForAggregates(),
           getParameterTypesForAggregates());
//           System.out.println("Got the method: "+methodForAggregates.getName());
      }
      catch (NoSuchMethodException nosEx) {
         FacadeManager.handleException(nosEx);
      }
      FacadeManager.addInUseProtocolListener(this);
    }

    protected final int numAggregates() {
        return aggregateList.size();
    }

    protected final Object[] getAggregates() {
      return aggregateList.toArray();
    }

    protected abstract String getMethodNameForAggregates();
    protected abstract Class[] getParameterTypesForAggregates();
    protected abstract Object[] getParametersForAggregates();

    public void protocolAddedToInUseList(String protocol) {
//       System.out.println(this.getClass()+"Heard about: "+protocol);
       if (protocol==FacadeManager.getAggregateProtocolString()) return;
       Object aggregate = null;
       try {
           aggregate=findMyAggregateForProtocol(methodForAggregates,
               getParametersForAggregates(),protocol);
//           System.out.println("Found Aggregate: "+aggregate);
       }
       catch (Exception ex) {
         FacadeManager.handleException(ex);
       }
       if (aggregate!=null) aggregateList.add(aggregate);
    }


    public void protocolRemovedFromInUseList(String protocol) {
      // System.out.println(this.getClass()+"Heard about: "+protocol);
       Object aggregate = null;
       try {
        aggregate = findMyAggregateForProtocol(methodForAggregates, getParametersForAggregates(),protocol);
       }
       catch (Exception ex) {
        FacadeManager.handleException(ex);
       }
       if (aggregate!=null) aggregateList.remove(aggregate);
    }

    private final Object findMyAggregateForProtocol(Method findMethod, Object[] parameters, String protocol)
      throws InvocationTargetException,IllegalAccessException
    {
//        System.out.println(this.getClass()+"Method: "+findMethod.getName()+" Prot: "+protocol);
        Object tmpObject= findMethod.invoke(FacadeManager.getFacadeManager(protocol),parameters);
        return tmpObject;
    }


    private final Method findMethodToGetAggregates(String methodName, Class[] parameterTypes)
      throws NoSuchMethodException {
        return FacadeManagerBase.class.getDeclaredMethod(methodName,parameterTypes);
    }

}
