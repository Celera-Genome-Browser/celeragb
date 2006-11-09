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
package api.entity_model.model.genetics;


import api.entity_model.access.observer.GenomeVersionObserver;
import api.entity_model.access.observer.NavigationObserver;
import api.entity_model.access.observer.ReportObserver;
import api.entity_model.access.report.PropertyReportRequest;
import api.entity_model.access.report.Report;
import api.entity_model.access.report.ReportRequest;
import api.entity_model.management.ControlledVocabularyMgr;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.AlignmentNotAllowedException;
import api.entity_model.model.annotation.Workspace;
import api.entity_model.model.fundtype.AlignableGenomicEntity;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.fundtype.LoadFilter;
import api.entity_model.model.fundtype.LoadRequest;
import api.entity_model.model.fundtype.LoadRequestStatus;
import api.facade.abstract_facade.fundtype.GenomicEntityLoader;
import api.facade.abstract_facade.fundtype.NavigationConstants;
import api.facade.abstract_facade.genetics.GenomeVersionLoader;
import api.facade.concrete_facade.xml.XmlWorkspaceFacade;
import api.facade.concrete_facade.xml.XmlWorkspaceFacadeManager;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.ControlledVocabulary;
import api.stub.data.GenomeVersionInfo;
import api.stub.data.InvalidPropertyFormat;
import api.stub.data.NavigationNode;
import api.stub.data.NavigationPath;
import api.stub.data.OID;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Identifies a version of a Genome for a particular species. The version consists
 * of both a species and an assembly on that species.
 *
 * @author Peter Davies
 * @version 1.0
 */

public class GenomeVersion extends GenomicEntity {

   private static final long serialVersionUID=1L;
   private Species species;
   private GenomeVersionInfo genomeVersionInfo;

   /**
    * The unique genome identifier.
    */
   private int id;

   /**
    * Description of the genome version from the point of view of the person
    * that created it.
    */
   private String description;

   /**
    * Indicates if this GenomeVersion and by inference all objects within
    * the GenomeVersion are read only. This indicator can be used by clients
    * to determine if annotation against this version should be permitted.
    */
   private boolean readOnly;

   /**
    * Every GenomeVersion has one and only one Workspace that manages the
    * mutable workspace area within the GenomeVersion.
    */
   private transient Workspace workspace;

   /**
    * Hack to preclude a bug that sends the workspace construction into an infinate loop
    */
   private transient boolean constructingWorkspace=false;



   /**
    * Map of all local entities with OID as key
    */
   private Map localEntities=new HashMap(INITIAL_LOCAL_ENTITIES_MAP_SIZE);
   private static int INITIAL_LOCAL_ENTITIES_MAP_SIZE=100;

   // Types of actions that Observers will be notified of.
   private static final int NOTE_ENTITY_ADDED = 10;
   private static final int NOTE_ENTITY_REMOVED = 11;
   private static final int NOTE_WORKSPACE_CREATED = 12;
   private static final int NOTE_WORKSPACE_REMOVED = 13;

      //==================== Constructor Methods ==============================

   /**
    *
    * @param aSpecies instance of the species for which this genome version
    * is a container. Note that passing a null for this parameter implies that
    * there was some problem reconstructing this Genome Version from persistent
    * storage.
    * @param genomeVersionInfo The info object for this GenomeVersion
    */
   public GenomeVersion( OID oid, Species aSpecies, GenomeVersionInfo genomeVersionInfo ) {
      this( oid, aSpecies,genomeVersionInfo, "No description avail", true, null);
   }


   /**
    * @param aSpecies instance of the species for which this genome version
    * is a container. Note that passing a null for this parameter implies that
    * there was some problem reconstructing this Genome Version from persistent
    * storage.
    * @param genomeVersionInfo The info object for this GenomeVersion
    * @param description describes the circumstances surrounding the creation
    * of this version of the species genome.
    * @param readOnly is this version to be edited?
    * @level developer
    */
   public GenomeVersion( OID oid, Species aSpecies, GenomeVersionInfo genomeVersionInfo, String description, boolean readOnly, FacadeManagerBase readFacadeManager ) {

      super(EntityType.getEntityTypeForName("_GENOME_VERSION"), oid, description, readFacadeManager);

      this.id = genomeVersionInfo.getGenomeVersionId();
      this.species = aSpecies;
      this.description = description;
      this.readOnly = readOnly;
      this.genomeVersionInfo = genomeVersionInfo;

      if ( this.species != null ) {
         localEntities.put( this.species.getOid(), species );
      }
   }



   //==================== Notification of relevant facades (XMLWorkspaceFacade) Methods ==============================

   /**
    * Get explicit notification that this is relevant to us...
    */
   public void noteRelevantFacadeManager(FacadeManagerBase aFacadeManagerBase) {
      //System.out.println("In noteRelevantFacadeManager();");
      if ( aFacadeManagerBase instanceof XmlWorkspaceFacadeManager ) {
         XmlWorkspaceFacadeManager theXmlFacadeMan = (XmlWorkspaceFacadeManager)aFacadeManagerBase;
         this.setupWorkspace(theXmlFacadeMan.getXmlWorkspaceFacade());
      }
   }


   //==================== Accessor Methods ==============================

/**
 * @level developer
 */
   public static NavigationPath[] getNavigationPaths(String searchType, String searchString) throws InvalidPropertyFormat {
      try {
         NavigationPath[] foundPaths = FacadeManager.getFacadeManager().getGenomeVersion().getNavigationPath(searchType,searchString);
         return (ensureCompletenessOfNavigationPaths(foundPaths, null, false));
      }
      catch ( InvalidPropertyFormat ipfEx ) {
         throw ipfEx;
      }
      catch ( Exception ex ) {
         ModelMgr.getModelMgr().handleException(ex);
      }
      return (new NavigationPath[0]);
   }

   public static void getNavigationPathsBackground(String searchType,
                                                   String searchString, NavigationObserver callbackObserver) {

      NavigationPathLoader navigationLoader
      = new NavigationPathLoader(searchType,searchString,callbackObserver);
      getLoaderThreadQueue().addQueue(navigationLoader);
   }

/**
 * @level developer
 */
   public static NavigationPath[] getNavigationPathsToOID(OID oid) throws InvalidPropertyFormat {
      try {
         NavigationPath[] foundPaths = FacadeManager.getFacadeManager().getGenomeVersion().getNavigationPath( NavigationConstants.UNKNOWN_OID_SN, oid.toString() );
         return (ensureCompletenessOfNavigationPaths(foundPaths, null, false));
      }
      catch ( InvalidPropertyFormat ipfEx ) {
         throw ipfEx;
      }
      catch ( Exception ex ) {
         ModelMgr.getModelMgr().handleException(ex);
      }
      return (new NavigationPath[0]);
   }


/**
 * @level developer
 */
   public static ControlledVocabulary getNavigationSearchTypes() {
      try {
         return (ControlledVocabularyMgr.getMgr().getNavigationVocabulary());
      }
      catch ( Exception ex ) {
         ModelMgr.getModelMgr().handleException(ex);
      }
      return (null);
   }


/**
 * @level developer
 */
   public NavigationPath[] getNavigationPathsInThisGenomeVersion(String searchType, String searchString) throws InvalidPropertyFormat {
      NavigationPath[] foundPaths = getMyDataLoader().getNavigationPath(getOid(),searchType,searchString);
      return (ensureCompletenessOfNavigationPaths(foundPaths, this, true));
   }

   public  void getNavigationPathsInThisGenomeVersionBackground(String searchType,
                                                                String searchString, NavigationObserver callbackObserver) {

      NavigationPathLoader navigationLoader
      = new NavigationPathLoader(searchType,searchString,callbackObserver,this);
      getLoaderThreadQueue().addQueue(navigationLoader);
   }

/**
 * @level developer
 */
   public NavigationPath[] getNavigationPathsToOIDInThisGenomeVersion(OID oid) throws InvalidPropertyFormat {
      //System.out.println("Request: "+oid.toString());
      NavigationPath[] foundPaths = getMyDataLoader().getNavigationPath( getOid(), NavigationConstants.UNKNOWN_OID_SN, oid.toString() );
      return (ensureCompletenessOfNavigationPaths( foundPaths, this, true ));
   }

   public Species getSpecies() {
      return (species);
   }

   /**
    * Get the current value for the assembly version UID
    * THIS IS DIFFERENT THAN "int getID();"
    * todo: Rename "long getVersion();" to "long getAssemblyVersion();"
    */
   public long getVersion() {
      return (genomeVersionInfo.getAssemblyVersion());
   }


   /**
    * Get the ID of this Genome Version.
    * Don't confuse with getVersion (getAssemblyVersion).
    */
   public int getID() {
      return( this.id );
   }



   /**
    * Get the current value for the assembly version UID
    */
   public BigDecimal getAssemblyVersionAsBigDecimal() {
      String assemblyVer = genomeVersionInfo.getAssemblyVersionAsString();
      BigDecimal assemblyVersionBigDecimal;
      if ( assemblyVer != null ) {
         assemblyVersionBigDecimal = new BigDecimal(assemblyVer);
      }
      else {
         throw new RuntimeException("Call to GenomeVersion::getAssemblyVersionAsBigDecimal " +
                                    " when assembly version was null ");
      }
      return(assemblyVersionBigDecimal);
   }

   public long getAssemblyVersion() {
      return (genomeVersionInfo.getAssemblyVersion());
   }

   /**
    * Get the data source name for the species
    */
   public String getDatasourceForVersion() {
      return (genomeVersionInfo.getDataSource());
   }

   /**
    * Get the genome version info
    */
   public GenomeVersionInfo getGenomeVersionInfo() {
      return (genomeVersionInfo);
   }


/**
 * @level developer
 */
   public boolean containsLocally(OID oid) {
      return (localEntities.containsKey(oid));
   }

   /**
    * Get a GenomicEntity for an OID, if it has been loaded.
    * Returns null if there is none.
    * @todo implement this
    * @level developer
    */
   public boolean containsRemotely(OID oid) {
      throw new RuntimeException("Not yet implemented");
   }


   /**
    * Get a GenomicEntity for an OID, if it has been loaded.
    * Returns null if there is none.
    * @level developer
    */
   public GenomicEntity getLoadedGenomicEntityForOid(OID anOid) {
      return(GenomicEntity)localEntities.get(anOid);
   }

   /**
    * Get a GenomicEntity for an OID, and will force a load if it has not been loaded.
    * Returns null if there is none.
    * @todo Make GenomeVersion facade implementation of this
    * @level developer
    */
   public GenomicEntity getGenomicEntityForOid(OID anOid) {
      GenomicEntity entity=(GenomicEntity)localEntities.get(anOid);
      if ( entity!=null ) return (entity);
      /**
       * @todo The swap features for transcript command sometimes cannot find the
       * Workspace gene and/or transcript.  This needs to be fixed and is breaking
       * the focused upon entity.
       */
//    else {
//      System.out.println("Remote call Not yet implemented for OID "+anOid);
//    }
      return (null);
      // getMyDataLoader().getGenomicEntityForOid(OID anOid);
   }

//*  Observation

   /**
    *  To track changes in the this object over time, you must add
    *  yourself as an observer of the it.  NOTE: this will post
    *  notices of all existing alignments
    *  @see api.entity_model.access.observer.GenomeVersionObserver
    *  @level developer
    */
   public void addGenomeVersionObserver(GenomeVersionObserver observer) {
      addGenomeVersionObserver(observer, true);
   }

   /**
    *  To track changes in the this object over time, you must add
    *  yourself as an observer of the it.  NOTE: this will optionally post
    *  notices of all existing alignments
    *  @see api.entity_model.access.observer.GenomeVersionObserver
    *  @level developer
    */
   public void addGenomeVersionObserver(GenomeVersionObserver observer,boolean bringUpToDate) {
      addGenomicEntityObserver(observer,bringUpToDate);
      if ( bringUpToDate ) {
         if ( localEntities!=null ) {
            GenomicEntity[] entities=getContainedEntitiesArray();
            // System.out.println("Num notifications to expect: "+entities.length);
            for ( int i=0;i<entities.length;i++ ) {
               observer.noteEntityAddedToGenomeVersion(entities[i]);
            }
         }
      }
   }

   /**
    * Remove an genomeVersionObserver
    * @level developer
    */
   public void removeGenomeVersionObserver(GenomeVersionObserver observer) {
      removeGenomicEntityObserver(observer);
   }


/**
 * @level developer
 */
   public void removeEntityFromLocalModelIfNoLongerAligned(AlignableGenomicEntity entity) {
      if ( entity.getAlignmentsToAxes().isEmpty() ) {
         OID oid=entity.getOid();
         localEntities.remove(oid);
         // oid.setGenomeVersionIdToNull();  // JTS 5/2/01: We don't want to do this.
         postContainerChanged(entity,NOTE_ENTITY_REMOVED);
      }

   }

   /**
    * Ensures uniqueness in the model, if uniquenes checking turned on.
    * If uniqueness checking off, will simply return the alignment as passed
    * If uniqueness checking on, may return a new alignment, which should
    * be used after the call
    * Uniqueness Checking is set in the ModelMgr
    *
    * @throws AlignmentNotAllowedException if the GenomeVersionID of the entity does not
    * match this GenomeVersion
    * @level developer
    */
   public Alignment checkForPreviouslyLoadedEntityAndAddToLocalModel(
                                                                    Alignment alignment)
   throws AlignmentNotAllowedException
   {

      OID entityOID=alignment.getEntity().getOid();
      if ( entityOID.getGenomeVersionId()!=getID() ) {
         if ( !entityOID.isNullGenomeVersionId() ) {
            entityOID.setGenomeVersionIdIfNull(getID());
         }
         else throw new AlignmentNotAllowedException("The entity of the alignment has "+
                                                     " an OID that points to a different GenomeVersion than this one.",alignment);
      }
      GenomicEntity entity=alignment.getEntity();
      GenomicEntity priorEntity=getLoadedGenomicEntityForOid(entity.getOid());
      if ( priorEntity==null ) {
         localEntities.put(entity.getOid(),entity);
         postContainerChanged(entity,NOTE_ENTITY_ADDED);
         return (alignment);
      }

      if ( getModelMgr().modelsDoUniquenessChecking() ) {
         //Checking for the same instance, should not use .equals as it is overridden
         if ( entity==priorEntity ) return (alignment);
         //Otherwise, clone the alignment, pointing at the prior entity and return it
         return (alignment.cloneWithNewEntity((AlignableGenomicEntity)priorEntity));
      }
      return (alignment);  //Models do not do uniqueness
   }



   public String getDescription()
   {
      return (description);
   }

   public boolean isAvailable() {
      return (species!=null);
   }

/**
 * @level developer
 */
   public boolean isReadOnly()
   {
      return (readOnly);
   }

/**
 * @level developer
 */
   public void makeReadOnly() {
      readOnly=true;
   }


/**
 * @level developer
 */
   public Report generateReportBlocking( ReportRequest request, ReportObserver observer) {
      ReportLoader loader = new ReportLoader(request);
      loader.run();
      return (loader.getReport());
   }

/**
 * @level developer
 */
   public void generateReportBackground( ReportRequest request, ReportObserver observer) {
      LoadRequest loadRequest=new LoadRequest(new LoadFilter("Report"));
      LoadRequestStatus loadStatus=loadRequest.getLoadRequestStatus();
      setLoadRequestToWaitingToLoad(loadStatus,loadRequest);
      ReportLoader loader = new ReportLoader(request, observer, loadStatus);
      getLoaderThreadQueue().addQueue(loader);
   }


   public String toString()
   {
      String retStr = "";
      String speciesDisplayName = "Unknown due to DB outage";
      String speciesOIDAsString = "Unknown due to DB outage";
      if ( species != null ) {
         speciesDisplayName = species.getDisplayName();
         speciesOIDAsString = "" + species.getOid();
      }

      retStr += "Species: " + speciesDisplayName +
                " Species OID: " + speciesOIDAsString +
                " assembly: " + getAssemblyVersion() +
                " datasource: " + getDatasourceForVersion();
      return (retStr);
   }

   public int hashCode()
   {
      return (genomeVersionInfo.getGenomeVersionId());
   }


   /**
    * Get the workspace and force the creation of one if there isn't yet.
    * @level developer
    */
   public Workspace getWorkspace() {
      this.setupWorkspace(null);
      return (workspace);
   }


   /**
    * Check to see if this GenomeVersion has a workspace.
    * @level developer
    */
   public boolean hasWorkspace() {
      return(workspace != null);
   }

   /**
    * Allow clients to determine if promotion against this GenomeVersion
    * is enabled.
    */
   public boolean promotionEnabled() {
      return (this.getGenomeVersionInfo().hasPromoteServerInfo());
   }

   /**
    * Clear the entire model below the Species of this Genome Version.
    * WARNING!!!!!!! At present this method will only work when called in
    * the server. Currently no attempt has been made to include notification
    * to views that the model has been removed from this GenomeVersions
    * species
    * @todo: generate notifications to views of the models removal.
    */
   public void unloadModel() {
      this.unloadWorkspace();
      Species mySpecies = this.getSpecies();
      mySpecies.unloadAlignmentsToEntitiesBlocking(mySpecies.getChromosomeLoadRequest());
      this.localEntities = new HashMap(INITIAL_LOCAL_ENTITIES_MAP_SIZE);
      this.localEntities.put(this.getOid(), this);
      this.localEntities.put(mySpecies.getOid(), mySpecies);
   }


   /**
    * Clear the workspace.
    * This is intended to be used on the client when moving from one Workspace
    * file to the next, and in the promotion utility when moving from one
    * @todo: Need to postWorkspaceUnloaded();
    * @todo: Need to make sure that there aren't any hanging references to the old workspace... may be better to "unload" the same workspace instance.
    */
   public void unloadWorkspace() {
      if ( workspace != null && !constructingWorkspace ) {
         List mapEntries= new ArrayList(localEntities.entrySet());
         GenomicEntity entity;
         Axis axis;
         for ( Iterator it=mapEntries.iterator();it.hasNext(); ) {
            entity=(GenomicEntity) ((Map.Entry)it.next()).getValue();
            if ( entity instanceof Axis ) {
               axis= (Axis)entity;
               axis.unloadWorkspaceEntities();
            }
         }
         Workspace tmpWorkspace=workspace;
         constructingWorkspace=true;
         workspace = null;
         tmpWorkspace.unload();
         constructingWorkspace=false;
         postWorkspaceRemoved(tmpWorkspace);
      }
   }

/**
 * @level developer
 */
   protected GenomicEntityLoader getDataLoader()
   {
      try {
         return (getLoaderManager().getGenomeVersion());
      }
      catch ( Exception ex ) {
         handleException(ex);
         return (null);
      }
   }


   /**
    * If we don't already have a workspace, set one up.
    * Should be called by getWorkspace, and
    */
   private void setupWorkspace(XmlWorkspaceFacade xmlWorkspaceFacade) {
      if ( workspace == null && !constructingWorkspace ) {
         constructingWorkspace=true;
         workspace = new Workspace(this, xmlWorkspaceFacade);
         constructingWorkspace=false;
         postWorkspaceCreated();
      }
   }





   private GenomeVersionLoader getMyDataLoader(){
      return(GenomeVersionLoader)getDataLoader();
   }


   /**
    * Post an observable action.
    * First message the action directly to the workspace.
    * Then put the notification to the observers on the queue.
    */
   private void postContainerChanged(GenomicEntity entity, int notedAction) {
      // If the entity action is workpace related... we need to setup a workspace
      // now because the workspace will be observing these actions.
      if ( entity.isWorkspace() ) {
         setupWorkspace(null);
      }

      //Message all model syn observers on this thread
      new GenomeVersionNotificationObject(
                                         entity,notedAction,true).run();
      // Post it to the notification queue...
      getNotificationQueue().addQueue(new GenomeVersionNotificationObject(
                                                                         entity,notedAction,false));
   }

   /**
    * Post an observable action.
    */
   private void postWorkspaceCreated() {
      //Message all model syn observers on this thread
      new GenomeVersionNotificationObject(
                                         this,NOTE_WORKSPACE_CREATED,true).run();
      // Post it to the notification queue...
      getNotificationQueue().addQueue(new GenomeVersionNotificationObject(
                                                                         this,NOTE_WORKSPACE_CREATED,false));
   }

   /**
    * Post an observable action.
    */
   private void postWorkspaceRemoved(Workspace workspace) {
      //Message all model syn observers on this thread
      new GenomeVersionNotificationObject(
                                         this,NOTE_WORKSPACE_REMOVED,true,workspace).run();
      // Post it to the notification queue...
      getNotificationQueue().addQueue(new GenomeVersionNotificationObject(
                                                                         this,NOTE_WORKSPACE_REMOVED,false,workspace));
   }

   /** Checks all paths to see if they need to be complemented and then does so. */
   private static NavigationPath[] ensureCompletenessOfNavigationPaths(  NavigationPath[] foundPaths,
                                                                         GenomeVersion genomeVersion,
                                                                         boolean callOnPassedGenomeVersion){
      List complementedPathsList = new ArrayList();
      NavigationPath[] expandedArray = null;
      for ( int i = 0; i < foundPaths.length; i++ ) {
         if ( foundPaths[i].getNavigationNodeArray()[0].getNodeType() == NavigationNode.ROOT_NODE_TYPE ) {
            complementedPathsList.add(foundPaths[i]);
         } // Rooted
         else {
            expandedArray = buildAllNavigationPathsTo(foundPaths[i], genomeVersion, callOnPassedGenomeVersion);
            for ( int j = 0; j < expandedArray.length; j++ ) {
               complementedPathsList.add(expandedArray[j]);
            } // For all expanded paths.
         } // Not rooted at root node.
      } // For all paths

      NavigationPath[] returnArray = new NavigationPath[complementedPathsList.size()];
      complementedPathsList.toArray(returnArray);

      return (returnArray);

   } // End method

   /** Runs a search against an axis from an axis rooted search, and builds multiple properly rooted paths. */
   private static NavigationPath[] buildAllNavigationPathsTo(  NavigationPath featurePath,
                                                               GenomeVersion genomeVersion,
                                                               boolean callOnPassedGenomeVersion) {

      NavigationPath[] headPathArray = null;

      // Do Unknown oid search.  This is because rootOID is most likely an axis OID not a feature.
      // Do not want to use the initial "Feature" searchType here to get the complement path.
      OID oldBaseNodeOID = featurePath.getNavigationNodeArray()[0].getOID();
      if ( oldBaseNodeOID!=null ) {
         if ( callOnPassedGenomeVersion ) {
            try {
               //          headPathArray = genomeVersion.getNavigationPathsToOIDInThisGenomeVersion(oldBaseNodeOID);
               headPathArray = genomeVersion.getMyDataLoader().getNavigationPath( oldBaseNodeOID, NavigationConstants.UNKNOWN_OID_SN, oldBaseNodeOID.toString() );
            } // End try block.
            catch ( InvalidPropertyFormat ipf ) {
               headPathArray = new NavigationPath[0];
            } // End catch block
         }
         else {
            try {
               GenomeVersion oldBaseNodeGenomeVersion = ModelMgr.getModelMgr().getGenomeVersionById(oldBaseNodeOID.getGenomeVersionId());
               headPathArray = oldBaseNodeGenomeVersion.getMyDataLoader().getNavigationPath( oldBaseNodeOID, NavigationConstants.UNKNOWN_OID_SN, oldBaseNodeOID.toString() );
               //          headPathArray = getNavigationPathsToOID(oldBaseNodeOID);
            } // End try block.
            catch ( Exception ex ) {
               headPathArray = new NavigationPath[0];
            } // End catch block
         }
      }
      else headPathArray = new NavigationPath[0];
      // Build a new complement from each head path.
      NavigationPath[] completedPathArray = new NavigationPath[headPathArray.length];
      for ( int i = 0; i < headPathArray.length; i++ ) {
         completedPathArray[i] = complementNavigationPath(headPathArray[i], featurePath);
      } // For all head paths generated from search.

      return (completedPathArray);

   } // End method

   /** Takes a secondary search and complements an axis-rooted search with it. */
   private static NavigationPath complementNavigationPath(NavigationPath headPath, NavigationPath featurePath) {

      // No dice.  Nothing to prepend.
      if ( headPath == null )
         return (featurePath);

      // *** TEMPORARY ***  Getting the genome version id and stuffing it into
      // the OIDs.
      //OID o;
      //o.setGenomeVersionIdToNull();
      //o.setGenomeVersionIdIfNull(7);
      /*
      int overridingGenomeVersionId = -1;
      if (headPath.getNavigationNodeArray() != null) {
        if (headPath.getNavigationNodeArray()[0].getOID() != null)
          overridingGenomeVersionId = headPath.getNavigationNodeArray()[0].getOID().getGenomeVersionId();
      } // Got nodes.
      if (overridingGenomeVersionId == -1)
        return featurePath;
       */
      List combinedNodes = new ArrayList();

      // Start with the complement path which is from the Species down to the VALID axis.
      for ( int i=0; i < headPath.getNavigationNodeArray().length; i++ ) {
         combinedNodes.add(headPath.getNavigationNodeArray()[i]);
      }
      // X starts at element 1 to throw out the dummy axis of the orphaned feature.
      for ( int x=1; x < featurePath.getNavigationNodeArray().length; x++ ) {
         combinedNodes.add(featurePath.getNavigationNodeArray()[x]);
         // *** TEMPORARY ***  Getting the genome version id and stuffing it into
         // the OIDs.
         /*
         featurePath.getNavigationNodeArray()[x].getOID().setGenomeVersionIdToNull();
         featurePath.getNavigationNodeArray()[x].getOID().setGenomeVersionIdIfNull(overridingGenomeVersionId);
         */
      }

      // Now construct the final Path.
      NavigationNode[] finalArray = new NavigationNode[combinedNodes.size()];
      finalArray = (NavigationNode[])(combinedNodes.toArray(finalArray));
      NavigationPath finalPath = new NavigationPath(featurePath.getDisplayName(), finalArray);

      return (finalPath);
   } // End method

   private GenomicEntity[] getContainedEntitiesArray(){
      if ( localEntities==null ) return (new GenomicEntity[0]);
      Set entries=localEntities.entrySet();
      GenomicEntity[] array=new GenomicEntity[entries.size()];
      int i=0;
      for ( Iterator it=entries.iterator();it.hasNext(); ) {
         array[i]=(GenomicEntity)((Map.Entry)it.next()).getValue();
         i++;
      }
      return (array);
   }

//****************************************
//*  Inner classes
//****************************************


   // notification
/**
 * @level developer
 */
   protected class GenomeVersionNotificationObject extends
   EntityNotificationObject {

      Workspace removedWorkspace;

      protected GenomeVersionNotificationObject (GenomicEntity changedEntity,
                                                 int notedAction, boolean notifyModelSyncObservers) {
         super(changedEntity,notedAction,false,notifyModelSyncObservers);
      }

      protected GenomeVersionNotificationObject (GenomicEntity changedEntity,
                                                 int notedAction, boolean notifyModelSyncObservers,Workspace removedWorkspace) {
         super(changedEntity,notedAction,false,notifyModelSyncObservers);
         this.removedWorkspace=removedWorkspace;
      }
/**
 * @level developer
 */
      protected Class getObserverFilteringClass() {
         return (GenomeVersionObserver.class);
      }

      public void run() {
         GenomeVersionObserver observer;
         List observers=getObserversToNotifyAsList();
         for ( int i= 0; i< observers.size(); i++ ) {
            observer=(GenomeVersionObserver)observers.get(i);
            // Send the notification, see if the action was not recognized
            // and was handled by the super class (therefore we don't want to
            // continue with the loop.
            if ( !sendNotificationMessage(observer) ) break;
         }
      }


      /**
       * send the notification to a single observer...
       */
      private boolean sendNotificationMessage(GenomeVersionObserver observer) {
         switch ( getNotedAction() ) {
            case NOTE_ENTITY_ADDED: {
                  observer.noteEntityAddedToGenomeVersion(getChangedEntity());
                  break;
               }
            case NOTE_ENTITY_REMOVED: {
                  observer.noteEntityRemovedFromGenomeVersion(getChangedEntity());
                  break;
               }
            case NOTE_WORKSPACE_CREATED: {
                  observer.noteWorkspaceCreated((GenomeVersion)getChangedEntity(),workspace);
                  break;
               }
            case NOTE_WORKSPACE_REMOVED: {
                  observer.noteWorkspaceRemoved((GenomeVersion)getChangedEntity(),removedWorkspace);
                  break;
               }
            default: {
                  super.run();
                  return (false);
               }
         }
         return (true);
      }
   }

   class ReportLoader implements Runnable, java.io.Serializable {

      private ReportRequest request;
      private ReportObserver observer;
      private Report report;
      private LoadRequestStatus loadStatus;

      public ReportLoader(ReportRequest reportRequest) {
         this.request = reportRequest;
         this.observer = null;
      }

      public ReportLoader(ReportRequest reportRequest, ReportObserver observer, LoadRequestStatus loadStatus) {
         this.loadStatus=loadStatus;
         this.request = reportRequest;
         this.observer = observer;
      }

      public void run() {
         try {
            if ( loadStatus!=null ) setLoadRequestState(loadStatus,LoadRequestStatus.LOADING);
            GenomeVersionLoader loader = getMyDataLoader();
            switch ( request.getRequestedReportType() ) {
               case ReportRequest.ENTITY_ALIGNMENT_REPORT:
                  report = loader.generateAlignmentReportForEntity
                           (
                           request.getRequestedOids()[0]
                           );
                  break;
               case ReportRequest.SUBJECT_SEQUENCE_REPORT:
                  report = loader.generateSubjectSequenceReportForEntity
                           (
                           request.getRequestedOids()[0]
                           );
                  break;
               case ReportRequest.PROPERTY_REPORT:
                  if ( !(request instanceof PropertyReportRequest) )
                     throw new IllegalArgumentException("The class of the passed report "+
                                                        "request does not match the declared type.");
                  report = loader.generatePropertyReport
                           (
                           GenomeVersion.this.getOid(),
                           request.getRequestedOids(),
                           ((PropertyReportRequest)request).getRequestedPropertyNames()
                           );
                  break;
               default:
                  if ( loadStatus!=null ) setLoadRequestState(loadStatus,LoadRequestStatus.COMPLETE);
                  throw new UnsupportedOperationException(
                                                         "The passed report request type is unknown.");
            }
            if ( loadStatus!=null ) setLoadRequestState(loadStatus,LoadRequestStatus.LOADED);
            if ( observer != null ) {
               getNotificationQueue().addQueue(
                                              new ReportNotifier(request,report,observer));
            }
            if ( loadStatus!=null ) setLoadRequestState(loadStatus,LoadRequestStatus.NOTIFIED);
            if ( loadStatus!=null ) setLoadRequestState(loadStatus,LoadRequestStatus.COMPLETE);
         }
         catch ( Exception ex ) {
            handleException(ex);
         }
      }

      public Report getReport()
      {
         return (report);
      }
   }



   private class ReportNotifier implements Runnable {
      private ReportRequest request;
      private Report report;
      private ReportObserver observer;

      private ReportNotifier (ReportRequest request, Report report,
                              ReportObserver observer) {

         this.report=report;
         this.request=request;
         this.observer=observer;
      }

      public void run() {
         observer.reportArrived(GenomeVersion.this, request, report);
      }
   }

   static class NavigationPathLoader implements Runnable {
      private String searchType;
      private String searchString;
      private NavigationObserver observer;
      private GenomeVersion gv;

      public NavigationPathLoader (String searchType,
                                   String searchString, NavigationObserver observer) {

         this.searchType = searchType;
         this.searchString = searchString;
         this.observer = observer;
      }

      public NavigationPathLoader (String searchType,
                                   String searchString, NavigationObserver observer,GenomeVersion gv) {

         this(searchType,searchString,observer);
         this.gv=gv;
      }

      public void run () {
         NavigationPath[] paths=null;
         try {
            if ( gv==null )
               paths=getNavigationPaths(searchType, searchString);
            else
               paths=gv.getNavigationPathsInThisGenomeVersion(searchType,searchString)   ;
         }
         catch ( InvalidPropertyFormat ipf ) {
            observer.noteNavigationError(ipf.getMessage());
            return;
         }
         catch ( Exception ex ) {
            ModelMgr.getModelMgr().handleException(ex);
         }
         observer.noteNavigationPathsArrived(paths,searchType,searchString);
      }

   }

}

