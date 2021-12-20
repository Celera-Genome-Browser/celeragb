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
import java.util.*;

public class ModelMgr {
  private static ModelMgr modelManager=new ModelMgr();
  private boolean readOnly;
  private List<ModelMgrObserver> modelMgrObservers;
  private boolean modelAvailable;
  private Set<GenomeVersion> genomeVersions=new HashSet<GenomeVersion>();
  private ThreadQueue threadQueue;
  private ThreadQueue notificationQueue;
  private ResourceBundle modelMgrResourceBundle;
  private GenomicEntityFactory entityFactory;
  private MutatorAccessController mutatorAccessController;
  private MutatorAccessController defaultMutatorAccessController=
     new DefaultMutatorAccessController();
  private HashSet<GenomeVersion> selectedGenomeVersions=new HashSet<GenomeVersion>();
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
         catch (Exception ex) {
            //if this fails, use the default
         }
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

  static public ModelMgr getModelMgr() {return modelManager;}

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
     if (modelMgrObservers==null) modelMgrObservers=new ArrayList<ModelMgrObserver>();
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
         for (Object genomeVersion : genomeVersions) {
             gv = (GenomeVersion) genomeVersion;
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
      return mt != null && mt.equalsIgnoreCase("TRUE");
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

  public Set<GenomeVersion> getGenomeVersions() {
     if (genomeVersionLookupNeeded) {
       GenomeLocatorFacade locator;
       try{
         locator=
          FacadeManager.getFacadeManager().getGenomeLocator();
       }
       catch (Exception ex) {
          handleException(ex);
          return new HashSet<GenomeVersion>(0);
       }
       GenomeVersion[] versions=locator.getAvailableGenomeVersions();
       Set<GenomeVersion> localGenomeVersions=new HashSet<GenomeVersion>(versions.length);
         for (GenomeVersion version : versions) {
             if (readOnly && !version.isReadOnly()) version.makeReadOnly();
             localGenomeVersions.add(version);
             genomeVersionObserver.addGenomeVersionToObserve(version);
             FacadeManager.addAvailableGenomeVersionInfo(version.getGenomeVersionInfo());
             if (modelMgrObservers != null) {
                 Object[] listeners = modelMgrObservers.toArray();
                 for (Object listener : listeners) {
                     ((ModelMgrObserver) listener).genomeVersionAdded(version);
                 }
             }
         }
       genomeVersions.addAll(localGenomeVersions);
       genomeVersionLookupNeeded=false;
     }
     return new HashSet<GenomeVersion>(genomeVersions);
  }

  /**
   * Returns all GenomeVersions that are marked isAvailable
   */
  public Set getAvailableGenomeVersions() {
    Set<GenomeVersion> versions=getGenomeVersions();
    Set<GenomeVersion> rtnVersions=new HashSet<GenomeVersion>();
    GenomeVersion version;
      for (Object version1 : versions) {
          version = (GenomeVersion) version1;
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


  public List<GenomeVersion> getGenomeVersions(GenomeVersionCollectionFilter filter) {
     Collection versions=getGenomeVersions();
     return Collections.unmodifiableList(FiltrationDevice.getDevice().
        executeGenomeVersionFilter(versions,filter));
  }

  public GenomeVersion getGenomeVersionById(int genomeVersionId) {
      Collection gvCollection=getGenomeVersions();
      GenomeVersion[] gvArray=(GenomeVersion[])gvCollection.toArray(new GenomeVersion[0]);
      for (GenomeVersion aGvArray : gvArray) {
          if (aGvArray.getID() == genomeVersionId) return aGvArray;
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
      for (GenomeVersion aGvArray : gvArray) {
          if (((GenomeVersion) aGvArray).getID() == genomeVersionID) return aGvArray;
      }
      return null;  //none found
  }

  /**
   * @return Set of genomeVersions
   */
  public Set<GenomeVersion> getSelectedGenomeVersions() {
     return (Set<GenomeVersion>)selectedGenomeVersions.clone();
  }

  public void addSelectedGenomeVersion(GenomeVersion version) {
     if (selectedGenomeVersions.add(version)) {
       modelAvailable=true;
       if (modelMgrObservers!=null) {
         Object[] listeners=modelMgrObservers.toArray();
           for (Object listener : listeners) {
               ((ModelMgrObserver) listener).genomeVersionSelected(version);
           }
       }
     }
  }

  public void unSelectGenomeVersion(GenomeVersion genomeVersion) {
    if (selectedGenomeVersions.remove(genomeVersion)){
       if (modelMgrObservers!=null) {
         Object[] listeners=modelMgrObservers.toArray();
           for (Object listener : listeners) {
               ((ModelMgrObserver) listener).genomeVersionUnselected(genomeVersion);
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
      for (Object genomeVersion1 : genomeVersions) {
          gv = (GenomeVersion) genomeVersion1;
          if (!genomeVersion.equals(gv)) gv.makeReadOnly();
      }
    FacadeManager.setGenomeVersionWithWorkSpaceId(genomeVersion.getID());
    if (modelMgrObservers!=null) {
       Object[] listeners=modelMgrObservers.toArray();
        for (Object listener : listeners) {
            ((ModelMgrObserver) listener).workSpaceCreated(genomeVersion);
        }
    }
  }

  private void workSpaceWasRemoved(GenomeVersion genomeVersion,Workspace workspace) {
    FacadeManager.setGenomeVersionWithWorkSpaceId(0);
    if (modelMgrObservers!=null) {
       Object[] listeners=modelMgrObservers.toArray();
        for (Object listener : listeners) {
            ((ModelMgrObserver) listener).workSpaceRemoved(genomeVersion, workspace);
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


