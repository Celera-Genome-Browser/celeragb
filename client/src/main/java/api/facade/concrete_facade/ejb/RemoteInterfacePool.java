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

import api.stub.data.FatalCommError;
import api.stub.data.SystemError;
import shared.util.MultiHash;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import java.lang.reflect.Method;
import java.util.Vector;

public class RemoteInterfacePool {
  private Vector availStandardInterfaces;
  private Vector inUseStandardInterfaces;
  private Vector inUseOverflowInterfaces;
  private Vector markedForDeleteInterfaces;
  private MultiHash overflowInterfaces;
  private String componentName;
  private EJBHome homeInterface;
  private Method createMethod;
  private Object[] argumentList;
  private int maxInterfacesAllowed;
  private int numStandardInterfaces;
  private boolean allowSharingOfInterfaces;
  private boolean tuning=false;  //change to true to see debug output
//  private EJBObject iface;

  public RemoteInterfacePool(String componentName, EJBHome homeInterface, Method createMethod, Object[] argumentList,
                             int minInterfaces,int maxInterfacesAllowed) {
     if (tuning) System.out.println("Creating pool for: "+componentName);
     this.componentName=componentName;
     this.homeInterface=homeInterface;
     this.createMethod=createMethod;
     this.argumentList=argumentList;
     this.maxInterfacesAllowed=maxInterfacesAllowed;
     this.numStandardInterfaces=minInterfaces;
     availStandardInterfaces=new Vector(minInterfaces,0);
     inUseStandardInterfaces=new Vector(minInterfaces,0);
  }

  public RemoteInterfacePool(Class componentInterfaceClass, EJBHome homeInterface, Method createMethod, Object[] argumentList,
                             int minInterfaces,int maxInterfacesAllowed) {
     this(componentInterfaceClass.getName(),homeInterface,createMethod,argumentList,minInterfaces,maxInterfacesAllowed);
  }

  public RemoteInterfacePool(String componentName, EJBHome homeInterface, Method createMethod, Object[] argumentList,
                             int minInterfaces,int maxInterfacesAllowed, boolean allowSharingOfInterfaces) {

     this(componentName, homeInterface,createMethod, argumentList, minInterfaces, maxInterfacesAllowed);
     this.allowSharingOfInterfaces=allowSharingOfInterfaces;

  }

  public RemoteInterfacePool(Class componentInterfaceClass, EJBHome homeInterface, Method createMethod, Object[] argumentList,
                             int minInterfaces,int maxInterfacesAllowed,boolean allowSharingOfInterfaces) {
     this(componentInterfaceClass.getName(),homeInterface,createMethod,argumentList,minInterfaces,maxInterfacesAllowed,allowSharingOfInterfaces);
  }

  private synchronized void addStandardInterface () {
   try {
          EJBObject remoteInterface= (EJBObject)createMethod.invoke(homeInterface, argumentList);
          availStandardInterfaces.add(remoteInterface);
          if (tuning) System.out.println("Creating Standard Interface for "+componentName);
   }
   catch (Exception ex) {
       throw new FatalCommError(ex.getMessage());
   }
  }

  private synchronized EJBObject getOverflowInterface () {
   try {
       EJBObject remoteInterface= (EJBObject)createMethod.invoke(homeInterface, argumentList);
       if (inUseOverflowInterfaces==null) {
          inUseOverflowInterfaces=new Vector(5);
          inUseOverflowInterfaces.add(remoteInterface);
       }
       if (tuning) System.out.println("Creating Overflow Interface for "+componentName);
       return remoteInterface;
   }
   catch (Exception ex) {
       throw new FatalCommError(ex.getMessage());
   }

  }

  public synchronized EJBObject getInterface(){
/*    if (iface!=null) {
       System.out.println("Returning inuse interface");
       return iface;
    }*/
    if (tuning) System.out.println("Interface retrieved for "+componentName);
    if (allowSharingOfInterfaces) {
       if (availStandardInterfaces.size()==0) addStandardInterface();
       return (EJBObject)availStandardInterfaces.lastElement();
    }
    if (availStandardInterfaces.size()==0 && inUseStandardInterfaces.size()<numStandardInterfaces)
       addStandardInterface();
    if (availStandardInterfaces.size()>0 ) {
       EJBObject interfaceToUse= (EJBObject)availStandardInterfaces.lastElement();
       availStandardInterfaces.remove(interfaceToUse);
       inUseStandardInterfaces.add(interfaceToUse);
       return interfaceToUse;
    }
    EJBObject availInterface = getOverflowInterface();
    return availInterface;
  }

  public synchronized void freeInterface(EJBObject interfaceToFree){
    if (tuning) System.out.println("Interface freed for "+componentName);
    if (allowSharingOfInterfaces) {
        if (!interfaceToFree.equals(availStandardInterfaces.lastElement()))
            throw new SystemError ("Interface "+interfaceToFree+" passed to freeInterface of "+
              componentName+" did not come from this pool.");
        return;
    }
    int index;
    index=inUseStandardInterfaces.indexOf(interfaceToFree);
    if (index>-1) {
       inUseStandardInterfaces.remove(index);
       availStandardInterfaces.add(interfaceToFree);
       return;
    }

    if (inUseOverflowInterfaces!=null) {
      index=inUseOverflowInterfaces.indexOf(interfaceToFree);
      if (index>-1) {
         inUseOverflowInterfaces.remove(index);
         try {
            interfaceToFree.remove();
            if (tuning) System.out.println("Removed overflow remote interface from server for "+componentName);
         }
         catch (Exception ex) {
           System.err.println("Remote interface was not properly removed from the Server for "+
               componentName+" "+ex.getMessage());
         }
         return;
      }
    }

    if (markedForDeleteInterfaces!=null) {
      index=markedForDeleteInterfaces.indexOf(interfaceToFree);
      if (index>-1) {
         markedForDeleteInterfaces.remove(index);
         try {
            interfaceToFree.remove();
            if (tuning) System.out.println("Removed overflow remote interface from server for "+componentName);
         }
         catch (Exception ex) {
            System.err.println("Remote interface was not properly removed from the Server for "+
               componentName+" "+ex.getMessage());
         }
         return;
      }
    }
    throw new SystemError ("Interface "+interfaceToFree+" passed to freeInterface of "+
        componentName+" did not come from this pool.");
   }

  public synchronized void returnBadInterface(EJBObject interfaceToFree){
    System.err.println("Interface returned as bad for "+componentName);
    if (allowSharingOfInterfaces) {
        if (!interfaceToFree.equals(availStandardInterfaces.lastElement()))
            throw new SystemError ("Interface "+interfaceToFree+" passed to freeInterface of "+
              componentName+" did not come from this pool.");
        return;
    }
    int index;
    index=inUseStandardInterfaces.indexOf(interfaceToFree);
    if (index>-1) {
       inUseStandardInterfaces.remove(index);
         try {
            interfaceToFree.remove();
            if (tuning) System.out.println("Removed bad remote interface from server for "+componentName);
         }
         catch (Exception ex) {
            System.err.println("Remote interface, returned as bad, "+
              "was not properly removed from the Server for "+componentName);
         }
         return;
    }

    if (inUseOverflowInterfaces!=null) {
      index=inUseOverflowInterfaces.indexOf(interfaceToFree);
      if (index>-1) {
         inUseOverflowInterfaces.remove(index);
         try {
            interfaceToFree.remove();
            if (tuning) System.out.println("Removed bad overflow remote interface from server for "+componentName);
         }
         catch (Exception ex) {
            System.err.println("Remote interface, returned as bad, "+
               "was not properly removed from the Server for "+componentName);
         }
         return;
      }
    }

    if (markedForDeleteInterfaces!=null) {
      index=markedForDeleteInterfaces.indexOf(interfaceToFree);
      if (index>-1) {
         markedForDeleteInterfaces.remove(index);
         try {
            interfaceToFree.remove();
            if (tuning) System.out.println("Removed bad overflow remote interface from server for "+componentName);
         }
         catch (Exception ex) {
            System.err.println("Remote interface, returned as bad, "+
               "was not properly removed from the Server for "+componentName);
         }
         return;
      }
    }

    throw new SystemError ("Interface "+interfaceToFree.getClass()+" passed to returnBadInterface of "+
        componentName+" did not come from this pool.");

   }

   public synchronized void refreshInterfaces() {
       removeInterfaces(availStandardInterfaces);
       markedForDeleteInterfaces = inUseStandardInterfaces;
       inUseStandardInterfaces=new Vector(numStandardInterfaces,0);
   }

   public synchronized void removeAllInterfaces() {
       removeInterfaces(availStandardInterfaces);
       removeInterfaces(inUseStandardInterfaces);
       if (inUseOverflowInterfaces!= null)
          removeInterfaces(inUseOverflowInterfaces);
   }

   private void removeInterfaces(Vector interfaces) {
      for (int i=0;i<interfaces.size();i++) {
         try {
           ((EJBObject)interfaces.elementAt(i)).remove();
            if (tuning) System.out.println("Removed remote interface from server for "+componentName);
         }
         catch (Exception ex) {
           System.out.println("Remote interface was not properly removed from the Server - "+ex);
         }
      }

   }

}