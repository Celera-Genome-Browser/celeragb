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
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 ********************************************************************/

package api.entity_model.management;

import api.entity_model.access.filter.FiltrationDevice;
import api.entity_model.access.filter.GenomeVersionCollectionFilter;
import api.entity_model.access.observer.GenomeVersionObserverAdapter;
import api.entity_model.access.observer.ModelMgrObserver;
import api.entity_model.model.annotation.Workspace;
import api.entity_model.model.fundtype.ActiveThreadModel;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.genetics.GenomeLocatorFacade;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.InUseProtocolListener;
import api.facade.roles.ExceptionHandler;
import api.stub.data.NoData;
import shared.exception_handlers.PrintStackTraceHandler;
import shared.util.PropertyConfigurator;
import shared.util.ThreadQueue;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

public class ModelMgr {
  private static ModelMgr modelManager=new ModelMgr();
  private Vector exceptionHandlers;
  private boolean readOnly;
  private List modelMgrObservers;
  private boolean modelAvailable;
  private Set genomeVersions=new HashSet();
  private ThreadQueue threadQueue;
  private ThreadQueue notificationQueue;
  private ResourceBundle modelMgrResourceBundle;
  private GenomicEntityFactory entityFactory;
  private MutatorAccessController mutatorAccessController;
  private MutatorAccessController defaultMutatorAccessController=
     new DefaultMutatorAccessController();
  private HashSet selectedGenomeVersions=new HashSet();
  private Class factoryClass;
  private MyGenomeVersionObserver genomeVersionObserver = new MyGenomeVersionObserver();
  private boolean genomeVersionLookupNeeded=true;

  static {
    // Register an exception handler.
    ModelMgr.getModelMgr().registerExceptionHandler(new PrintStackTraceHandler());
  }

  {
    try {

       String propsFile = PropertyConfigurator.getProperties().getProperty("x.genomebrowser.ModelMgrProperties");

       modelMgrResourceBundle = ResourceBundle.getBundle(propsFile);
       String accessController=modelMgrResourceBundle.getString("MutatorAccessController");
       if (accessController!=null) {
         try {
          Class controllerClass=Class.forName(accessController);
          mutatorAccessController=(MutatorAccessController)controllerClass.newInstance();
         }
         catch (Exception ex) {} //if this fails, use teh default
       }
       String factory=modelMgrResourceBundle.getString("Factory");
       factoryClass=Class.forName(factory);
       if (!GenomicEntityFactory.class.isAssignableFrom(factoryClass)) {
          System.out.println("ModelMgr: Error! - The defined factory is not a GenomicEntityFactory!! Exiting..");
          System.exit(1);
       }
    }
    catch (java.util.MissingResourceException mre) {
      System.out.println("ModelMgr: Error! - Cannot find resource files.  Resource directory must be on the classpath!!  Exiting..");
      System.exit(1);
    }
    catch (ClassNotFoundException cnfEx) {
      System.out.println("ModelMgr: Error! - Cannot find genomic entity factory!!  Exiting..");
      System.exit(1);
    }
  }

  private ModelMgr() {
    FacadeManager.addInUseProtocolListener(new MyInUseProtocolListener());
  } //Singleton enforcement

  static final public ModelMgr getModelMgr() {return modelManager;}

  public MutatorAccessController getMutatorAccessController() {
     if (mutatorAccessController==null) return defaultMutatorAccessController;
     else return mutatorAccessController;
  }

  public void setMutatorAccessController(MutatorAccessController mac)
    throws IllegalStateException{

    if (mutatorAccessController !=null) throw new IllegalStateException(
       "The MutatorAccessController has already been set.  It cannot be set again.");
    else mutatorAccessController=mac;
  }

  public void addModelMgrObserver(ModelMgrObserver mml){
     if (modelMgrObservers==null) modelMgrObservers=new ArrayList();
     modelMgrObservers.add(mml);
  }

  public void removeModelMgrObserver(ModelMgrObserver mml){
     if (modelMgrObservers==null) return;
     modelMgrObservers.remove(mml);
  }


  public void registerExceptionHandler(ExceptionHandler handler) {
     FacadeManager.registerExceptionHandler(handler);
  }

  public void deregisterExceptionHandler(ExceptionHandler handler) {
     FacadeManager.deregisterExceptionHandler(handler);
  }

  /**
   * Override the read-only state to true for all GenomeVersions
   */

  public void makeReadOnly(){
     readOnly=true;
     if (genomeVersions!=null) {
        Set genomeVersions=getGenomeVersions();
        GenomeVersion gv;
        for (Iterator it=genomeVersions.iterator();it.hasNext();) {
           gv=(GenomeVersion)it.next();
           gv.makeReadOnly();
        }
     }
  }

  public GenomicEntityFactory getEntityFactory() {
    if (entityFactory==null) {
      try {
       Constructor cons=factoryClass.getConstructor(new Class[]{Integer.class});
       entityFactory = (GenomicEntityFactory)cons.newInstance(new Object[]{new Integer(this.hashCode())});
      }
      catch (Exception ex) {
        handleException(ex);
      }
    }
    return entityFactory;
  }

  public boolean isMultiThreaded() {
    String mt = modelMgrResourceBundle.getString("MultiThreadedServerCalls");
    if (mt !=null && mt.equalsIgnoreCase("TRUE")) return true;
    else return false;
  }


  public void registerFacadeManagerForProtocol(String protocol, Class facadeClass, String displayName) {
    FacadeManager.registerFacade(protocol,facadeClass,displayName);
  }

  public ThreadQueue getLoaderThreadQueue() {
      if (threadQueue==null)
        if (isMultiThreaded()) threadQueue=new ThreadQueue(6,"LoaderGroup",Thread.MIN_PRIORITY,true);
        else threadQueue=new ThreadQueue(0,"LoaderGroup",Thread.NORM_PRIORITY,true);
      return threadQueue;
  }

  public ThreadQueue getNotificationQueue() {
      if (notificationQueue==null)
        if (isMultiThreaded())  notificationQueue=new ThreadQueue(1,"NotificationThreads",Thread.MIN_PRIORITY,false);
        else notificationQueue=new ThreadQueue(0,"NotificationThreads",Thread.NORM_PRIORITY,false);
      return notificationQueue;
  }


  public ActiveThreadModel getActiveThreadModel() {
     return ActiveThreadModel.getActiveThreadModel();
  }

  public void handleException (Throwable throwable) {
     if (throwable instanceof NoData) return;
     FacadeManager.handleException(throwable);
  }

  public void removeAllGenomeVersions() {
     genomeVersions=null;
  }

  public Set getGenomeVersions() {
     if (genomeVersionLookupNeeded) {
       GenomeLocatorFacade locator=null;
       try{
         locator=
          FacadeManager.getFacadeManager().getGenomeLocator();
       }
       catch (Exception ex) {
          handleException(ex);
          return new HashSet(0);
       }
       GenomeVersion[] versions=locator.getAvailableGenomeVersions();
       Set localGenomeVersions=new HashSet(versions.length);
       for (int i=0;i<versions.length;i++) {
          if (readOnly && !versions[i].isReadOnly())versions[i].makeReadOnly();
          localGenomeVersions.add(versions[i]);
          genomeVersionObserver.addGenomeVersionToObserve(versions[i]);
          FacadeManager.addAvailableGenomeVersionInfo(versions[i].getGenomeVersionInfo());
          if (modelMgrObservers!=null) {
             Object[] listeners=modelMgrObservers.toArray();
             for (int j=0;j<listeners.length;j++) {
               ((ModelMgrObserver)listeners[j]).genomeVersionAdded(versions[i]);
             }
          }
       }
       genomeVersions.addAll(localGenomeVersions);
       genomeVersionLookupNeeded=false;
     }
     return new HashSet(genomeVersions);
  }

  /**
   * Returns all GenomeVersions that are marked isAvailable
   */
  public Set getAvailableGenomeVersions() {
    Set versions=getGenomeVersions();
    Set rtnVersions=new HashSet();
    GenomeVersion version;
    for (Iterator it=versions.iterator();it.hasNext(); ){
      version=(GenomeVersion)it.next();
      if (version.isAvailable()) rtnVersions.add(version);
    }
    return rtnVersions;
  }

  /**
  * Will NOT Force load of Genome Versions
  */
  public int getNumberOfLoadedGenomeVersions() {
    if (genomeVersions==null) return 0;
    return genomeVersions.size();
  }


  public List getGenomeVersions(GenomeVersionCollectionFilter filter) {
     Collection versions=getGenomeVersions();
     return Collections.unmodifiableList(FiltrationDevice.getDevice().
        executeGenomeVersionFilter(versions,filter));
  }

  public GenomeVersion getGenomeVersionById(int genomeVersionId) {
      Collection gvCollection=getGenomeVersions();
      GenomeVersion[] gvArray=(GenomeVersion[])gvCollection.toArray(new GenomeVersion[0]);
      for (int i=0;i<gvArray.length;i++) {
         if (gvArray[i].getID()==genomeVersionId) return gvArray[i];
      }
      return null;  //none found
  }

  public List getAvailableGenomeVersions(GenomeVersionCollectionFilter filter) {
     Collection versions=getGenomeVersions();
     return Collections.unmodifiableList(FiltrationDevice.getDevice().
        executeGenomeVersionFilter(versions,filter));
  }


  public GenomeVersion getGenomeVersionContaining(GenomicEntity nodeInModel) {
      Collection gvCollection=getGenomeVersions();
      GenomeVersion[] gvArray=(GenomeVersion[])gvCollection.toArray(new GenomeVersion[0]);
      int genomeVersionID=nodeInModel.getOid().getGenomeVersionId();
      for (int i=0;i<gvArray.length;i++) {
         if ( ( ( GenomeVersion )gvArray[i] ).getID()==genomeVersionID) return gvArray[i];
      }
      return null;  //none found
  }

  /**
   * @return Set of genomeVersions
   */
  public Set getSelectedGenomeVersions() {
     return (Set)selectedGenomeVersions.clone();
  }

  public void addSelectedGenomeVersion(GenomeVersion version) {
     if (selectedGenomeVersions.add(version)) {
       modelAvailable=true;
       if (modelMgrObservers!=null) {
         Object[] listeners=modelMgrObservers.toArray();
         for (int i=0;i<listeners.length;i++) {
           ((ModelMgrObserver)listeners[i]).genomeVersionSelected(version);
         }
       }
     }
  }

  public void unSelectGenomeVersion(GenomeVersion genomeVersion) {
    if (selectedGenomeVersions.remove(genomeVersion)){
       if (modelMgrObservers!=null) {
         Object[] listeners=modelMgrObservers.toArray();
         for (int i=0;i<listeners.length;i++) {
           ((ModelMgrObserver)listeners[i]).genomeVersionUnselected(genomeVersion);
         }
       }
     }
    if (selectedGenomeVersions.size()==0) modelAvailable=false;
  }

  public void prepareForSystemExit() {
     FacadeManager.getFacadeManager().prepareForSystemExit();
  }


  public boolean modelsDoUniquenessChecking() {
     String uc = modelMgrResourceBundle.getString("UniquenessCheckingOfEntities");
     if (uc!=null && uc.equalsIgnoreCase("TRUE")) return true;
     else return false;
  }

 public boolean isModelAvailable() {
    return modelAvailable;
 }

  private void workSpaceWasCreated(GenomeVersion genomeVersion) {
    Set genomeVersions=getGenomeVersions();
    GenomeVersion gv;
    for (Iterator it=genomeVersions.iterator();it.hasNext();) {
       gv=(GenomeVersion)it.next();
       if (!genomeVersion.equals(gv)) gv.makeReadOnly();
    }
    FacadeManager.setGenomeVersionWithWorkSpaceId(genomeVersion.getID());
    if (modelMgrObservers!=null) {
       Object[] listeners=modelMgrObservers.toArray();
       for (int j=0;j<listeners.length;j++) {
         ((ModelMgrObserver)listeners[j]).workSpaceCreated(genomeVersion);
       }
    }
  }

  private void workSpaceWasRemoved(GenomeVersion genomeVersion,Workspace workspace) {
    FacadeManager.setGenomeVersionWithWorkSpaceId(0);
    if (modelMgrObservers!=null) {
       Object[] listeners=modelMgrObservers.toArray();
       for (int j=0;j<listeners.length;j++) {
         ((ModelMgrObserver)listeners[j]).workSpaceRemoved(genomeVersion,workspace);
       }
    }
  }

  class MyGenomeVersionObserver extends GenomeVersionObserverAdapter {
       private Set observedGenomeVersions=new HashSet();

       void addGenomeVersionToObserve(GenomeVersion genomeVersion) {
          if (observedGenomeVersions.contains(genomeVersion)) return;
          observedGenomeVersions.add(genomeVersion);
          genomeVersion.addGenomeVersionObserver(this);
       }

       public void noteWorkspaceCreated(GenomeVersion genomeVersion, Workspace workspace){
          workSpaceWasCreated(genomeVersion);
       }

       public void noteWorkspaceRemoved(GenomeVersion genomeVersion, Workspace workspace){
          workSpaceWasRemoved(genomeVersion,workspace);
       }
    }

  class MyInUseProtocolListener implements InUseProtocolListener {
     public void protocolAddedToInUseList(String protocol){
       genomeVersionLookupNeeded=true;
     }
     public void protocolRemovedFromInUseList(String protocol){

     }
  }


}


