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
package api.entity_model.model.fundtype;

import api.entity_model.access.observer.GenomicEntityObserver;
import api.entity_model.access.observer.GenomicEntityObserverAdapter;
import api.entity_model.access.observer.LoadRequestStatusObserverAdapter;
import api.entity_model.access.observer.ModelSynchronizationObserver;
import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.management.GenomicEntityFactory;
import api.entity_model.management.ModelMgr;
import api.entity_model.management.PropertyMgr;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.fundtype.GenomicEntityLoader;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.GenomicEntityAlias;
import api.stub.data.GenomicEntityComment;
import api.stub.data.GenomicProperty;
import api.stub.data.InvalidPropertyFormat;
import api.stub.data.NavigationNode;
import api.stub.data.NoData;
import api.stub.data.OID;
import shared.util.ThreadQueue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version $Id$
 *
 * This class is the base class for the all model entities.  It supports properties
 * and defines basic behaviorial requirements.  It also provides convienent static
 * mappings to many parts of the system, like handleException, getLoaderThreadQueue etc.
 */
public abstract class GenomicEntity implements java.io.Serializable, Cloneable {

   private static final long serialVersionUID = 4;

   /** Tracking of GenomicEntity(s) that are in the process of construction or destruction. */
   private static final Set entitiesUnderConstruction = new HashSet();

   /** Unique identifier for a Genomic Entity */
   private OID oid = null;

   /** Simple human readable name for this genomic entity */
   private String displayName = null;

   /** pad predicted storage requirements by this number */
   protected final float PREDICTION_PADDING_FACTOR = .15f;

   /** static map that points to sets of observers, key is this instance */
   private static final Map normalObserverMap = new HashMap();

   /**
    * static map that points to sets of model synchronization observers,
    * key is this instance
    */
   private static final Map modelSynchronizationObserverMap = new HashMap();

   // Types of actions that Observers will be notified of.
   //**Note: all actions here will call noteEntityDeatils changed when executed
   private static final int NOTE_ACTION_PROPERTIES = 0;
   private static final int NOTE_ACTION_ALIASES = 1;
   private static final int NOTE_ACTION_COMMENTS = 2;

   //For convience of sub-classes
   private static final ModelMgr modelMgr = ModelMgr.getModelMgr();
   private static final FacadeManagerBase loaderManager = FacadeManager.getFacadeManager();
   private static final ThreadQueue notificationQueue = modelMgr.getNotificationQueue();
   private static final ThreadQueue loaderThreadQueue = modelMgr.getLoaderThreadQueue();
   private static final GenomicEntityFactory factory = modelMgr.getEntityFactory();

   private GenomicEntityMutator mutator;

   //These three Lists are maintained as Sets (they enforce non-redundency on entry).
   //They are Lists for memory savings --PED 3/13/01
   private List properties;
   private List aliases;
   private List comments;

   private LoadRequest propertyLoadRequest;
   private LoadRequest commentLoadRequest;
   private LoadRequest aliasLoadRequest;

   /**
     * Some genomic entities share a class as they do not exibit different
     * behaviour and the only data differentces they have are simple
     * name / value pair properties whose retrieval is inherited
     * at the GenomicEntity level.
     */
   private final EntityType type;

   /**
    * Clients can decide to ask the genomic entity to use a specific facade manager
    * for fullfilling on demand requests that require retrieval from a
    * datasource. Not that possible datasources could be an xml file,
    * a set of xml files, an RDBMS or any combination thereof.
    */
   transient private FacadeManagerBase overrideLoaderManager = null;

   transient LoadRequest myLoadRequest;

   //****************************************
   //*  Public methods
   //****************************************

   /**
    * Normal Constructor.  Will tell this object to consult the FacadeManager to get it's loader
    * DisplayName should be null to have getDisplayName return the OID's identifier
    */
   public GenomicEntity(final EntityType entityType, final OID oid, final String displayName) {
      this(entityType, oid, displayName, null);
   }

   /**
    * Secondary constructor.  Will tell this object to only load data from the passed loader
    * DisplayName should be null to have getDisplayName return the OID's identifier   *
    */
   public GenomicEntity(final EntityType entityType, final OID oid, final String displayName, final FacadeManagerBase overrideLoaderManager) {
      this.oid = oid;
      this.displayName = displayName;
      this.overrideLoaderManager = overrideLoaderManager;
      this.type = entityType;
   }

   // ----------- Life Cycle Support ----------------------------

   /**
    * Check to see if this GenomicEntity is under construction / destruction.
    */
   public boolean isUnderConstruction() {
      return (entitiesUnderConstruction.contains(this));
   }

   /**
    * Provide the entity type...
    */
   public EntityType getEntityType() {
      return type;
   }

   /**
    * Access the public mutator for this class.
    *
    * @param requestor - the instance requesting access
    * @param callbackMethodName - the String name of the method for the callback, which must take
    *    a GenomicEntityMutator (or subclass) as it's only parameter
    */
   public void getMutator(final Object requestor, final String callbackMethodName) {
      final boolean access = modelMgr.getMutatorAccessController().allowAccessToMutator(requestor, this);
      if (!access)
         throw new IllegalAccessError("Requestor passed to getMutator is not allowed to access the mutator of this entity.");
      final GenomicEntityMutator mutator = getMutator();
      try {
         final Method callbackMethod = requestor.getClass().getMethod(callbackMethodName, new Class[] { GenomicEntityMutator.class });
         callbackMethod.invoke(requestor, new Object[] { mutator });
      }
      catch (InvocationTargetException inTgEx) {
         inTgEx.printStackTrace();
         throw new IllegalArgumentException(
            "The callbackMethod passed needs to take a "
               + "GenomicEntityMutator or subClass as a parameter OR an exception "
               + "occurred during invocation of the callback. Exception details "
               + "are: "
               + inTgEx.getMessage());
      }
      catch (IllegalAccessException ilAcEx) {
         throw new IllegalArgumentException(
                 "The callbackMethod passed needs to proper visibilty modifier (probably public)");
      }
      catch (NoSuchMethodException nsmEx) {
         throw new IllegalArgumentException(
                 "The callbackMethod passed needs to take a GenomicEntityMutator or subClass as a parameter");
      }
   }

   /**
    *  To track changes in the this object over time, you must add
    *  yourself as an observer of the it. This will bring the
    *  observer up to date with the current state.
    *  @see GenomicEntityObserver
    */
   public void addGenomicEntityObserver(final GenomicEntityObserver observer) {
      addGenomicEntityObserver(observer, true);
   }

   /**
    *  To track changes in the this object over time, you must add
    *  yourself as an observer of the it. This will optionally bring the
    *  observer up to date with the current state.
    *  @see GenomicEntityObserver
    */
   public void addGenomicEntityObserver(final GenomicEntityObserver observer, final boolean bringUpToDate) {
      if (!getMyNormalObservers().contains(observer))
         addObserver(observer);
      if (observer instanceof GenomicEntityObserverAdapter) {
         ((GenomicEntityObserverAdapter) observer).addObservable(this);
      }
      if (bringUpToDate)
         observer.noteEntityDetailsChanged(this, false);
   }

   /**
    *  Remove a previously added observer
    */
   public void removeGenomicEntityObserver(final GenomicEntityObserver observer) {
      removeObserver(observer);
   }

   /**
    * Gets the OID of this object.
    */
   public OID getOid() {
      return oid;
   }

   /**
    * Check if this Genomic entity is scratch...
    */
   public boolean isScratch() {
      if (oid == null)
         return false;
      return oid.isScratchOID();
   }

   /**
   *  Note this should be overridden for any Composite Types that have multiple OIDs
   */
   public int hashCode() {
      try {
         if (!getOid().isNull())
            return getOid().hashCode();
         else
            return super.hashCode();
      }
      catch (NullPointerException npe) {
         return super.hashCode();
      }
   }

   public boolean equals(final Object obj) {
      if (obj instanceof GenomicEntity) {
         try {
            if (!getOid().isNull())
               return (getOid().equals(((GenomicEntity) obj).getOid()));
            else
               return super.equals(obj);
         }
         catch (NullPointerException npe) {
            return super.equals(obj);
         }
      }
      else
         return false;
   }

   /**
    * Intended to be overridden in Feature
    * @return toString
    */
   public String getDescriptiveText() {
      return toString();
   }

   public GenomeVersion getGenomeVersion() {
      return modelMgr.getGenomeVersionContaining(this);
   }

   /**
    * Accepts visitors that implement GenomicEntityVisitor. acceptVisitorForSelf
    * is specialized by every subclass of GenomicEntity to call the appropriate
    * "visit...(...)" function on the passed visitor.
    * @param theVisitor the visitor.
    */
   public void acceptVisitorForSelf(final GenomicEntityVisitor theVisitor) {
      try {
         theVisitor.visitGenomicEntity(this);
      }
      catch (Exception ex) {
         handleException(ex);
      }
   }

   /**
    * @return true if the OID is null;
    */
   public final boolean hasNullOID() {
      return getOid() == null || getOid().isNull();

   }

   /**
    * @return true if this instance refers to an object in "This Company's" Database
    */
   public final boolean isInternalDatabase() {
      return getOid().isInternalDatabaseOID();
   }

   /**
    * @return true if this instance refers to an object in the Workspace
    */
   public final boolean isWorkspace() {
      return getOid().isScratchOID();
   }

   /**
    * @return true if this instance refers to an object that was generated
    * in the client
    */
   public final boolean isClientGenerated() {
      return getOid().isClientGeneratedOID();
   }

   /**
    * @return true if this instance refers to an object that was generated
    * in the server
    */
   public final boolean isServerGenerated() {
      return getOid().isServerGeneratedOID();
   }

   /**
    * @return true if this instance refers to an object that was generated
    * in the api classes
    */
   public final boolean isAPIGenerated() {
      return getOid().isAPIGeneratedOID();
   }

   /**
    * @return the namespace of the current object
    */
   public final String getNameSpace() {
      return getOid().getNameSpaceAsString();
   }

   /**
   * getProperties will return a set of properties for this
   * GenomicEntityProxy, loading them if necessary
   *  Blocking call
   * @return Set of GenomicProperty
   */
   public Set getProperties() {
      if (propertyLoadRequest != null) {
          final LoadRequestState loadreqstat=propertyLoadRequest.getLoadRequestStatus().getLoadRequestState();
          if (LoadRequestStatus.COMPLETE.equals(loadreqstat)
            || LoadRequestStatus.LOADED.equals(loadreqstat)
            || LoadRequestStatus.NOTIFIED.equals(loadreqstat)) {
            return getLoadedProperties();
          }
      }
      final LoadRequestStatus status = loadPropertiesBackground();
      waitForLoading(status);
      return getLoadedProperties();
   }

   /**
   * getProperties will return a SortedSet of properties for this
   * GenomicEntityProxy, loading them if necessary
   *  Blocking call
   * @return SortedSet of GenomicProperty
   */
   public SortedSet getProperties(final Comparator comparator) {
      if (propertyLoadRequest != null) {
          final LoadRequestState loadRequestState = propertyLoadRequest.getLoadRequestStatus().getLoadRequestState();
          if (LoadRequestStatus.COMPLETE.equals(loadRequestState)) {
            return convertSetToSortedSet(getProperties(), comparator);
          }
      }
      final LoadRequestStatus status = loadPropertiesBackground();
      waitForLoading(status);
      return convertSetToSortedSet(getProperties(), comparator);
   }

   /**
   * getAliases will return a set of aliases for this
   * GenomicEntityProxy, loading them if necessary
   *  Blocking call
   * @return List of GenomicEntityAlias
   */
   public Set getAliases() {
      if (aliasLoadRequest != null) {
         final LoadRequestState loadRequestState = aliasLoadRequest.getLoadRequestStatus().getLoadRequestState();
         if (LoadRequestStatus.COMPLETE.equals(loadRequestState)
            || LoadRequestStatus.LOADED.equals(loadRequestState)
            || LoadRequestStatus.NOTIFIED.equals(loadRequestState)) {
            return getLoadedAliases();
         }
      }
      final LoadRequestStatus status = loadAliasesBackground();
      waitForLoading(status);
      return getLoadedAliases();
   }

   /**
   * getAliases will return a SortedSet of aliases for this
   * GenomicEntityProxy, loading them if necessary
   *  Blocking call
   * @return List of GenomicEntityAlias
   */
   public SortedSet getAliases(final Comparator comparator) {
      if (aliasLoadRequest != null) {
          final LoadRequestState loadReqState = aliasLoadRequest.getLoadRequestStatus().getLoadRequestState();
          if (LoadRequestStatus.COMPLETE.equals(loadReqState)) {
              return convertListToSortedSet(aliases, comparator);
          }
      }
      final LoadRequestStatus status = loadAliasesBackground();
      waitForLoading(status);
      return convertListToSortedSet(aliases, comparator);
   }

   /**
   * getComments will return a set of comments for this
   * GenomicEntityProxy, loading them if necessary
   *  Blocking call
   * @return List of GenomicEntityComment
   */
   public Set getComments() {
      if (commentLoadRequest != null) {
          final LoadRequestState loadRequestState = commentLoadRequest.getLoadRequestStatus().getLoadRequestState();
          if (LoadRequestStatus.COMPLETE.equals(loadRequestState)
            || LoadRequestStatus.LOADED.equals(loadRequestState)
            || LoadRequestStatus.NOTIFIED.equals(loadRequestState)) {
            return getLoadedComments();
          }
      }
      final LoadRequestStatus status = loadCommentsBackground();
      waitForLoading(status);
      return getLoadedComments();
   }

   /**
   * getComments will return a set of comments for this
   * GenomicEntityProxy, loading them if necessary
   *  Blocking call
   * @return List of GenomicEntityComment
   */
   public SortedSet getComments(final Comparator comparator) {
      if (commentLoadRequest != null) {
          final LoadRequestState loadRequestState = commentLoadRequest.getLoadRequestStatus().getLoadRequestState();
          if (LoadRequestStatus.COMPLETE.equals(loadRequestState)) {
              return convertListToSortedSet(comments, comparator);
          }
      }
      final LoadRequestStatus status = loadCommentsBackground();
      waitForLoading(status);
      return convertListToSortedSet(comments, comparator);
   }

   /**
   * getLoadedProperties will return a set of properties for this GenomicEntity
   *  Blocking call.
   * @return A DEEP Copy of the Set of GenomicProperty
   */
   public Set getLoadedProperties() {
      if (properties == null)
         return new HashSet(1);
      final GenomicProperty[] returnPropArray =
              (GenomicProperty[]) properties.toArray(new GenomicProperty[properties.size()]);
      final ArrayList returnPropCol = new ArrayList(properties.size());
      for (int i = 0; i < returnPropArray.length; i++) {
         returnPropCol.add(returnPropArray[i].clone());
      }
      return new HashSet(PropertyMgr.getPropertyMgr().addPropertiesFromRules(this, returnPropCol));
   }

   /**
   * getLoadedProperties will return a set of properties for this GenomicEntity
   *  Blocking call
   * @return SortedSet of GenomicProperty
   */
   public SortedSet getLoadedProperties(final Comparator comparator) {
      if (properties == null)
         return new TreeSet(comparator);
      return convertSetToSortedSet(getProperties(), comparator);
   }

   /**
   * getLoadedAliases will return a set of aliases for this GenomicEntity
   *  Blocking call
   * @return Set of GenomicEntityAlias
   */
   public Set getLoadedAliases() {
      if (aliases == null)
         return new HashSet(1);
      return new HashSet(aliases);
   }

   /**
   * getLoadedAliases will return a set of aliases for this GenomicEntity
   *  Blocking call
   * @return Set of GenomicEntityAlias
   */
   public SortedSet getLoadedAliases(final Comparator comparator) {
      if (aliases == null)
         return new TreeSet(comparator);
      return convertListToSortedSet(aliases, comparator);
   }

   /**
   * getLoadedComments will return a set of comments for this GenomicEntity
   *  Blocking call
   * @return Set of GenomicEntityComment
   */
   public Set getLoadedComments() {
      if (comments == null)
         return new HashSet(1);
      return new HashSet(comments);
   }

   /**
   * getLoadedComments will return a set of comments for this GenomicEntity
   *  Blocking call
   * @return Set of GenomicEntityComment
   */
   public SortedSet getLoadedComments(final Comparator comparator) {
      if (comments == null)
         return new TreeSet(comparator);
      return convertListToSortedSet(comments, comparator);
   }

   /**
   * loadProperties will load the properties.  It will return a LoadRequestStatus
   * which can be observed to tell when the properties are loaded and getProperties
   * can be called.
   *  Non-Blocking call
   * @return LoadRequestStatus
   */
   public LoadRequestStatus loadPropertiesBackground() {
      if (propertyLoadRequest != null)
         return propertyLoadRequest.getLoadRequestStatus();
      final LoadFilter propertyLoadFilter = new LoadFilter("Property");
      propertyLoadRequest = new LoadRequest(propertyLoadFilter);
      final LoadRequestStatus propertyLoadStatus = propertyLoadRequest.getLoadRequestStatus();
      if (propertyLoadFilter.getLoadFilterStatus().isCompletelyLoaded()) {
         //Create a fake load status and set it to complete to return if already
         //complete
         propertyLoadStatus.setLoadRequestState(LoadRequestStatus.COMPLETE);
         return propertyLoadStatus;
      }
      propertyLoadStatus.setPendingLoadRequestAndStateToWaiting(propertyLoadRequest);
      final PropertyLoader propertyLoader = new PropertyLoader(propertyLoadRequest);
      getLoaderThreadQueue().addQueue(propertyLoader);
      return propertyLoadStatus;
   }

   /**
   * loadComments will load the comments.  It will return a LoadRequestStatus
   * which can be observed to tell when the comments are loaded and getComments
   * can be called.
   *  Non-Blocking call
   * @return LoadRequestStatus
   */
   public LoadRequestStatus loadCommentsBackground() {
      if (commentLoadRequest != null)
         return commentLoadRequest.getLoadRequestStatus();
      final LoadFilter commentLoadFilter = new LoadFilter("Comment");
      commentLoadRequest = new LoadRequest(commentLoadFilter);
      final LoadRequestStatus commentLoadStatus = commentLoadRequest.getLoadRequestStatus();
      if (commentLoadFilter.getLoadFilterStatus().isCompletelyLoaded()) {
         //Create a fake load status and set it to complete to return if already
         //complete
         commentLoadStatus.setLoadRequestState(LoadRequestStatus.COMPLETE);
         return commentLoadStatus;
      }
      commentLoadStatus.setPendingLoadRequestAndStateToWaiting(commentLoadRequest);
      final CommentLoader commentLoader = new CommentLoader(commentLoadRequest);
      getLoaderThreadQueue().addQueue(commentLoader);
      return commentLoadStatus;
   }

   /**
   * loadAliases will load the aliases.  It will return a LoadRequestStatus
   * which can be observed to tell when the aliases are loaded and getComments
   * can be called.
   *  Non-Blocking call
   * @return LoadRequestStatus
   */
   public LoadRequestStatus loadAliasesBackground() {
      if (aliasLoadRequest != null)
         return aliasLoadRequest.getLoadRequestStatus();
      final LoadFilter aliasLoadFilter = new LoadFilter("Alias");
      aliasLoadRequest = new LoadRequest(aliasLoadFilter);
      final LoadRequestStatus aliasLoadStatus = aliasLoadRequest.getLoadRequestStatus();
      if (aliasLoadFilter.getLoadFilterStatus().isCompletelyLoaded()) {
         //Create a fake load status and set it to complete to return if already
         //complete
         aliasLoadStatus.setLoadRequestState(LoadRequestStatus.COMPLETE);
         return aliasLoadStatus;
      }
      aliasLoadStatus.setPendingLoadRequestAndStateToWaiting(aliasLoadRequest);
      final AliasLoader aliasLoader = new AliasLoader(aliasLoadRequest);
      getLoaderThreadQueue().addQueue(aliasLoader);
      return aliasLoadStatus;
   }

   /**
   * @return returns the displayName
   */
   public String toString() {
      return getDisplayName();
   }

   /**
   * will return a named property of this GenomicEntity or null
   *
   * @return property or null
   */

   public GenomicProperty getProperty(final String name) {
      final Set props = getProperties();
      for (Iterator it = props.iterator(); it.hasNext();) {
         final GenomicProperty currentProp = (GenomicProperty) it.next();
         if (currentProp.getName().equals(name))
            return currentProp;
      }
      return null;
   }

   /**
    * Explanation:
    * Depth first retrieval is used. First get all properties. Iterate through them one by
    * one, on each iteration if you dont find then recurse further till you reach leaf.
    *
    */
   public GenomicProperty getParentProperty(final String subPropName) {
      final Set props = this.getProperties();
      GenomicProperty retProp = null;
      for (Iterator i = props.iterator(); i.hasNext();) {
         final GenomicProperty prop = (GenomicProperty) i.next();
         retProp = getParentPropertyHelper(prop, subPropName);
         if (retProp != null) {
            break;
         }

      }
      return retProp;
   }

   private GenomicProperty getParentPropertyHelper(final GenomicProperty prop, final String subPropNm) {
      GenomicProperty retProp = null;
      final GenomicProperty[] subProps = prop.getSubProperties();
      if (subProps != null) {
         for (int j = 0; j < subProps.length; j++) {
            final String subPropName = (subProps[j]).getName();
            if (subPropNm.equals(subPropName)) {
               retProp = prop;
               break;
            } //if
            else {
               retProp = getParentPropertyHelper(subProps[j], subPropNm);
            }
         } //for
      } //if
      return retProp;
   }

   /**
    * Will cause all data that this object can cache to be loaded the objects
    * attributes where this has not already been done. Subclasses that
    * override this method MUST call super before loading any of thier own
    * cached data into instance variables
    */
   public void loadCachableData() {
      this.loadPropertiesBackground();
      this.loadAliasesBackground();
      this.loadCommentsBackground();
   }

   /**
    * @return displayName
    */
   public final String getDisplayName() {
      if (displayName == null)
         return getOid().getIdentifierAsString();
      return displayName;
   }

   //****************************************
   //*  Protected methods
   //****************************************

   /**
    * Template pattern
    * Sub-Classes should overide this to set the proper mutator
    */
   protected GenomicEntityMutator constructMyMutator() {
      return new GenomicEntityMutator();
   }

   /**
    *  Access to the protected mutator
    *  calling constructMyMutator if necessary
    */
   protected final GenomicEntityMutator getMutator() {
      if (mutator == null)
         mutator = constructMyMutator();
      return mutator;
   }

   /**
    * Convience method for subclasses
    */
   protected final GenomicEntityFactory getEntityFactory() {
      return factory;
   }

   /**
    * Sub-Classes should always call this method in order to ensure
    * they get the proper LoaderManager
    */
   protected final FacadeManagerBase getLoaderManager() {
      if (overrideLoaderManager != null)
         return overrideLoaderManager;
      else
         return loaderManager;
   }

   /**
    * Convience method for subclasses
    */
   protected final static ThreadQueue getNotificationQueue() {
      return notificationQueue;
   }

   /**
    * Convience method for subclasses
    */
   protected final static ThreadQueue getLoaderThreadQueue() {
      return loaderThreadQueue;
   }

   /* returns the ModelMgr*/
   protected final static ModelMgr getModelMgr() {
      return modelMgr;
   }
   /**
    * convience method for sub-classes
    */
   protected final void handleException(final Throwable throwable) {
      modelMgr.handleException(throwable);
   }

   /**
    * Expected to be overridden. To fill in any necessary details
    */
   /*   protected void getNavigationPathHelper(Vector navNodes, GenomicEntityProxy startingInterval){
          navNodes.add(0,new NavigationNode(startingInterval.getOID(),startingInterval.getNavigationType(),startingInterval.toString(),null));
          if (startingInterval.getParent()!=null) startingInterval.getParent().getNavigationPathHelper(
             navNodes,startingInterval.getParent());
      }*/

   /**
    * Template pattern
    * Intended to be overidden to return the proper node type
    */
   protected int getNavigationType() {
      return NavigationNode.UNKNOWN;
   }

   /**
    * Returns the observer list, which will contain observers of the specified class
    * in the observation inheritance heirarchy
    *
    * @param observerClass the class of the observers
    * @return A Mutable Set
    */
   protected final Set getNormalObservers(final Class observerClass) {
      final Set observers = new HashSet();
      final GenomicEntityObserver[] observerArray = getNormalObserversAsArray();
      for (int i = 0; i < observerArray.length; i++) {
         if (observerClass.isInstance(observerArray[i]))
            observers.add(observerArray[i]);
      }
      return observers;
   }

   /**
    * Returns the observer list, which will contain observers of  specified class
    * in the observation inheritance heirarchy
    *
    * @param observerClass the class of the observers
    * @return A Mutable Set
    */
   protected final Set getModelSynchronizationObservers(final Class observerClass) {
      final Set observers = new HashSet();
      final GenomicEntityObserver[] observerArray = getModelSynchronizationObserversAsArray();
      for (int i = 0; i < observerArray.length; i++) {
         if (observerClass.isInstance(observerArray[i]))
            observers.add(observerArray[i]);
      }
      return observers;
   }

   /**
    * Setup initial observer list size
    */
   protected int getPredictedNumberOfObservers() {
      return 1;
   }

   /**
    * Must be implemented by concrete classes.
    */
   protected abstract GenomicEntityLoader getDataLoader();

   /**
    * Set the temporary load Request
    */

   protected void setMyLoadRequest(final LoadRequest loadRequest) {
      this.myLoadRequest = loadRequest;
   }

   /**
    * Get the temporary load request (only available while loading)
    */
   protected LoadRequest getMyLoadRequest() {
      return myLoadRequest;
   }

   /**
    * Wait for the loadRequestStatus to finish before continuing
    */
   protected void waitForLoading(final LoadRequestStatus ls) {
      if (ls.getLoadRequestState() == LoadRequestStatus.LOADING || ls.getLoadRequestState() == LoadRequestStatus.WAITING) {
         ls.addLoadRequestStatusObserver(new LoadObserver(Thread.currentThread()), false);
         synchronized (this) {
            try {
               wait(10000); //max of 10000 to avoid deadlock
            }
            catch (Exception ex) {
            } //expect inturrupted exception, do nothing
         }
      }
   }

   /**
    * Helper method to expose the package visable methods on LoadRequestStatus to the protected level
    */
   protected void setLoadRequestState(final LoadRequestStatus loadRequestStatus, final LoadRequestState state) {
      loadRequestStatus.setLoadRequestState(state);
   }

   /**
    * Helper method to expose the package visable methods on LoadRequestStatus to the protected level
    */
   protected void setLoadRequestToWaitingToLoad(final LoadRequestStatus loadRequestStatus, final LoadRequest loadRequest) {
      loadRequestStatus.setPendingLoadRequestAndStateToWaiting(loadRequest);
   }

   //****************************************
   //*  Package methods
   //****************************************

   //****************************************
   //*  Private methods
   //****************************************

   /**
    * Returns the observer list, which will contain observers of all classes
    * in the observation inheritance heirarchy as an GenomicEntityObserver[]
    */
   private final GenomicEntityObserver[] getNormalObserversAsArray() {
      final Set observers = getMyNormalObservers();
      if (observers == null)
         return new GenomicEntityObserver[0];
      return (GenomicEntityObserver[]) observers.toArray(new GenomicEntityObserver[observers.size()]);
   }

   private final GenomicEntityObserver[] getModelSynchronizationObserversAsArray() {
      final Set observers = getMyModelSyncObservers();
      if (observers == null)
         return new GenomicEntityObserver[0];
      return (GenomicEntityObserver[]) observers.toArray(new GenomicEntityObserver[observers.size()]);
   }

   private void addNewProperties(final GenomicProperty[] newProperties) {
      if (properties == null) {
         properties = new ArrayList(Arrays.asList(newProperties));
         return;
      }
      else {
         final Set set = new HashSet(properties);
         set.addAll(Arrays.asList(newProperties));
         properties = new ArrayList(set);
      }
   }

   private void addNewProperties(final Set newProperties) {
      addNewProperties((GenomicProperty[]) newProperties.toArray(new GenomicProperty[newProperties.size()]));
   }

   private void addNewComments(final GenomicEntityComment[] newComments) {
      if (comments == null) {
         comments = new ArrayList(Arrays.asList(newComments));
         return;
      }
      else {
         final Set set = new HashSet(comments);
         set.addAll(Arrays.asList(newComments));
         /*properties*/
         comments = new ArrayList(set);
      }
      // lastly update the num_comments property
      final GenomicProperty gp = this.getProperty(GenomicEntityLoader.NUM_COMMENTS_PROP);
      if (gp != null) {
         final String value = gp.getInitialValue();
         int numComments = Integer.parseInt(value);
         try {
            // Make sure to indicate this change is the result of an initial load
            // so that resulting notifications are properly flagged.
            this.getMutator().setProperty(GenomicEntityLoader.NUM_COMMENTS_PROP, String.valueOf(++numComments), true);
         }
         catch (Exception e) {
            ModelMgr.getModelMgr().handleException(e);
         }
      }
   }

   private void addNewComments(final Set comments) {
      addNewComments((GenomicEntityComment[]) comments.toArray(new GenomicEntityComment[comments.size()]));
   }

   private void addNewAliases(final GenomicEntityAlias[] newAliases) {
      if (aliases == null) {
         aliases = new ArrayList(Arrays.asList(newAliases));
         return;
      }
      else {
         final Set set = new HashSet(aliases);
         set.addAll(Arrays.asList(newAliases));
         aliases = new ArrayList(set);
      }
   }

   private void addNewAliases(final Set aliases) {
      addNewAliases((GenomicEntityAlias[]) aliases.toArray(new GenomicEntityAlias[aliases.size()]));
   }

   /**
    * Post notification of new details on  the notification queue.
    */
   private void postNotification(final GenomicEntity affectedInterval, final int notificationType, final boolean initialLoad) {
      //Run Model Sync notification on this thread
      new EntityNotificationObject(affectedInterval, notificationType, initialLoad, true).run();
      //Run other notification in the notification queue
      getNotificationQueue().addQueue(new EntityNotificationObject(affectedInterval, notificationType, initialLoad, false));
   }

   private static SortedSet convertListToSortedSet(final List list, final Comparator comparator) {
      final SortedSet set = new TreeSet(comparator);
      set.addAll(list);
      return set;
   }

   private static SortedSet convertSetToSortedSet(final Set set, final Comparator comparator) {
      final SortedSet netSet = new TreeSet(comparator);
      netSet.addAll(set);
      return netSet;
   }

   private Set getMyNormalObservers() {
      final Set observers = (Set) normalObserverMap.get(this);
      return observers == null ? new HashSet(0) : observers;
   }

   private Set getMyModelSyncObservers() {
      final Set observers = (Set) modelSynchronizationObserverMap.get(this);
      return observers == null ? new HashSet(0) : observers;
   }

   private void addObserver(final GenomicEntityObserver observer) {
      final Map mapToUse;
      if (observer instanceof ModelSynchronizationObserver) {
         mapToUse = modelSynchronizationObserverMap;
      }
      else {
         mapToUse = normalObserverMap;
      }
      if (mapToUse.containsKey(this)) {
         ((Set) mapToUse.get(this)).add(observer);
      }
      else {
         final int prediction = getPredictedNumberOfObservers();
         final Set observers = new HashSet((int) (prediction + (PREDICTION_PADDING_FACTOR * prediction)));
         observers.add(observer);
         mapToUse.put(this, observers);
      }
   }

   private void removeObserver(final GenomicEntityObserver observer) {
      final Map mapToUse;
      if (observer instanceof ModelSynchronizationObserver) {
         mapToUse = modelSynchronizationObserverMap;
      }
      else {
         mapToUse = normalObserverMap;
      }
      if (mapToUse.containsKey(this)) {
         final Set set = (Set) mapToUse.get(this);
         if (set == null)
            return;
         set.remove(observer);
         if (set.size() == 0)
            mapToUse.remove(this);
      }
   }

   //****************************************
   //*  Inner classes
   //****************************************

   // notification
   protected class EntityNotificationObject implements Runnable {
      private GenomicEntity changedEntity;
      private final int notedAction;
      private final boolean initialLoad;
      private final boolean notifyModelSyncObservers;

      protected EntityNotificationObject(final int notedAction, final boolean initialLoad, final boolean notifyModelSyncObservers) {
         this.notedAction = notedAction;
         this.initialLoad = initialLoad;
         this.notifyModelSyncObservers = notifyModelSyncObservers;
      }

      protected EntityNotificationObject(final GenomicEntity changedEntity, final int notedAction, final boolean initialLoad, final boolean notifyModelSyncObservers) {
         this(notedAction, initialLoad, notifyModelSyncObservers);
         this.changedEntity = changedEntity;
      }

      protected final int getNotedAction() {
         return notedAction;
      }

      protected final GenomicEntity getChangedEntity() {
         return changedEntity;
      }

      protected final Set getObserversToNotify() {
         final Class filteringClass = getObserverFilteringClass();
         if (filteringClass == null) {
            return notifyModelSyncObservers ? getMyModelSyncObservers() : getMyNormalObservers();
         }
         else {
            return notifyModelSyncObservers ? getModelSynchronizationObservers(filteringClass) : getNormalObservers(filteringClass);
         }
      }

      /**
       * Check if this notification is for the initial load.
       */
      protected final boolean isInitialLoad() {
         return initialLoad;
      }

      protected final boolean isNotifingModelSyncObservers() {
         return notifyModelSyncObservers;
      }

      /**
       * Override this method to filter the observer list
       */
      protected Class getObserverFilteringClass() {
         return null;
      }

      protected final List getObserversToNotifyAsList() {
         return new ArrayList(getObserversToNotify());
      }

      public void run() {
         sendGenomicEntityChangedMessage();
      }

      private void sendGenomicEntityChangedMessage() {
         final Set observersToNotify = getObserversToNotify();
         final GenomicEntityObserver[] observers = (GenomicEntityObserver[]) observersToNotify.toArray(new GenomicEntityObserver[observersToNotify.size()]);
         GenomicEntityObserver observer;
         for (int i = 0; i < observers.length; i++) {
            if (observers[i] instanceof GenomicEntityObserver) {
               observer = observers[i];
               switch (notedAction) {
                  case NOTE_ACTION_ALIASES :
                     {
                        observer.noteAliasesChanged(changedEntity, initialLoad);
                        observer.noteEntityDetailsChanged(changedEntity, initialLoad);
                        break;
                     }
                  case NOTE_ACTION_COMMENTS :
                     {
                        observer.noteCommentsChanged(changedEntity, initialLoad);
                        observer.noteEntityDetailsChanged(changedEntity, initialLoad);
                        break;
                     }
                  case NOTE_ACTION_PROPERTIES :
                     {
                        observer.notePropertiesChanged(changedEntity, initialLoad);
                        observer.noteEntityDetailsChanged(changedEntity, initialLoad);
                        break;
                     }
                  default :
                     {
                        throw new RuntimeException("Message " + notedAction + " not defined in GenomicEnitity");
                     }
               }

            }
         }
      }
   }

   final class AliasLoader extends LoaderThreadBase {

      public AliasLoader(final LoadRequest request) {
         super(request);
      }

      public void run() {
         loadRequestStatus.setLoadRequestState(LoadRequestStatus.LOADING);
         GenomicEntityAlias[] newAliases;
         try {
            newAliases = getDataLoader().getAliases(getOid());
         }
         catch (NoData ndEx) {
            newAliases = new GenomicEntityAlias[0];
         }
         addNewAliases(newAliases);
         loadRequestStatus.setLoadRequestState(LoadRequestStatus.LOADED);
         postNotification(GenomicEntity.this, NOTE_ACTION_ALIASES, true);
         loadRequestStatus.setLoadRequestState(LoadRequestStatus.NOTIFIED);
         loadRequestStatus.setLoadRequestState(LoadRequestStatus.COMPLETE);
      }
   }

   final class CommentLoader extends LoaderThreadBase {

      public CommentLoader(final LoadRequest request) {
         super(request);
      }

      public void run() {
         loadRequestStatus.setLoadRequestState(LoadRequestStatus.LOADING);
         GenomicEntityComment[] newComments;
         try {
            newComments = getDataLoader().getComments(getOid());
            addNewComments(newComments);
         }
         catch (NoData ndEx) {
            newComments = new GenomicEntityComment[0];
            if (comments == null)
               addNewComments(newComments);
         }
         loadRequestStatus.setLoadRequestState(LoadRequestStatus.LOADED);
         postNotification(GenomicEntity.this, NOTE_ACTION_COMMENTS, true);
         loadRequestStatus.setLoadRequestState(LoadRequestStatus.NOTIFIED);
         loadRequestStatus.setLoadRequestState(LoadRequestStatus.COMPLETE);
      }
   }

   final class PropertyLoader extends LoaderThreadBase {

      boolean monitorState = true;
      public PropertyLoader(final LoadRequest request) {
         super(request);
      }

      public PropertyLoader(final LoadRequest request, final boolean monitorState) {
         super(request);
         this.monitorState = monitorState;
      }

      public void run() {
         if (monitorState) {
            loadRequestStatus.setLoadRequestState(LoadRequestStatus.LOADING);
         }
         GenomicProperty[] newProperties = new GenomicProperty[0];
         try {
            newProperties = getDataLoader().getProperties(getOid(), getEntityType(), true);
         }
         catch (Exception ex) {
            handleException(ex);
         }
         addNewProperties(newProperties);

         if (monitorState) {
            loadRequestStatus.setLoadRequestState(LoadRequestStatus.LOADED);
            postNotification(GenomicEntity.this, NOTE_ACTION_PROPERTIES, true);
            loadRequestStatus.setLoadRequestState(LoadRequestStatus.NOTIFIED);
            loadRequestStatus.setLoadRequestState(LoadRequestStatus.COMPLETE);
         }

      }
   }

   static final class LoadObserver extends LoadRequestStatusObserverAdapter {
      final Thread waitingThread;

      public LoadObserver(final Thread waitingThread) {
         this.waitingThread = waitingThread;
      }

      public void stateChanged(final LoadRequestStatus loadRequestStatus, final LoadRequestState newState) {
         if (loadRequestStatus.getLoadRequestState() == LoadRequestStatus.LOADED) {
            loadRequestStatus.removeLoadRequestStatusObserver(this);
            waitingThread.interrupt();
         }
      }
   }

   /**
    * The Mutator class for GenomicEntity
    */
   public class GenomicEntityMutator implements java.io.Serializable {
      private static final long serialVersionUID = 2L;

      protected GenomicEntityMutator() {
      }

      /**
       * Set that this GenomicEntity is under construction / destruction.
       * This should suspend any active validation behavior.
       */
      public void setUnderConstruction(final boolean isUnderConstruction) {
         synchronized (entitiesUnderConstruction) {
            if (isUnderConstruction)
               entitiesUnderConstruction.add(GenomicEntity.this);
            else
               entitiesUnderConstruction.remove(GenomicEntity.this);
         }
      }

      /**
       * Get the GenomicEntity that this mutator will mutate.
       */
      public GenomicEntity getGenomicEntity() {
         return GenomicEntity.this;
      }

      /**
       * Add a property to this entity.
       */
      public void addProperty(final GenomicProperty newProperty) {
         final Set set = new HashSet(1);
         set.add(newProperty);
         addProperties(set);
      }

      /**
       * Add a comment to this entity.
       */
      public void addComment(final GenomicEntityComment comment) {
         final Set set = new HashSet(1);
         set.add(comment);
         addComments(set);
      }

      /**
       * Add multiple comments to this entity.
       *
       * @param comments Set of GenomicEntityComments
       * @throws ClassCastException if the Set contains anything other than GenomicEntityComments
       */
      public void addComments(final Set comments) {
         addNewComments(comments);
         GenomicEntity.this.postNotification(GenomicEntity.this, NOTE_ACTION_COMMENTS, false);
      }

      /**
       * Add an alias to this entity.
       */
      public void addAlias(final GenomicEntityAlias alias) {
         final Set set = new HashSet(1);
         set.add(alias);
         addAliases(set);
      }

      /**
       * Add multiple comments to this entity.
       *
       * @param aliases Set of GenomicEntityAlises
       * @throws ClassCastException if the set contains anything other than GenomicEntityAliases
       */
      public void addAliases(final Set aliases) {
         addNewAliases(aliases);
         GenomicEntity.this.postNotification(GenomicEntity.this, NOTE_ACTION_ALIASES, false);
      }

      /**
       * Add the Properties to this GenomicEntity.
       * This method gets called by the StandardPIFactory and processes the array of
       * Genomic Properties as a whole, adding to any existing properties.
       *
       * @param genomicProperties Set of GenomicProperties
       * @throws ClassCastException if the Set contains anything other than GenomicProperties
       */
      public void addProperties(final Set genomicProperties) {
         addNewProperties(genomicProperties);
         GenomicEntity.this.postNotification(GenomicEntity.this, NOTE_ACTION_PROPERTIES, false);
      }

      /**
       * This method will allow the modification of any singular property
       * that is held within the PI GenomicEntity.
       *
       * @param propertyName the name of the property
       * @param propertyValue the new Value
       */
      public void setProperty(final String propertyName, final String propertyValue) throws InvalidPropertyFormat {
         this.setProperty(propertyName, propertyValue, false);
      }

      /**
       * This method will allow the modification of any singular property
       * that is held within the PI GenomicEntity.
       *
       * @param propertyName the name of the property
       * @param propertyValue the new Value
       * @param initialLoad
       */
      private void setProperty(final String propertyName, final String propertyValue, final boolean initialLoad) throws InvalidPropertyFormat {

         // Find the property and set it's value...
         boolean propFound = false;
         if (properties != null) {
            for (int i = 0; i < properties.size(); i++) {
               if (((GenomicProperty) properties.get(i)).getName().equals(propertyName)) {
                  ((GenomicProperty) properties.get(i)).setInitialValue(propertyValue);
                  propFound = true;
                  break;
               } // End if prop.name equals propertyName
               else {
                  final GenomicProperty gp = searchForProperty((GenomicProperty) properties.get(i), propertyName);
                  if (gp != null) {
                     gp.setInitialValue(propertyValue);
                     propFound = true;
                     break;
                  }
               }
            } // End for properies.length
         } // End if properties != null

         // See if we found the property...
         if (propFound == false) {
            throw new InvalidPropertyFormat(
               "Property "
                  + propertyName
                  + " is not "
                  + " a property of entity "
                  + GenomicEntity.this.getClass().getName()
                  + ":"
                  + GenomicEntity.this.oid);
         }

         GenomicEntity.this.postNotification(GenomicEntity.this, NOTE_ACTION_PROPERTIES, initialLoad);
      }

      /**
       * recursive method to look for property
       */

      private GenomicProperty searchForProperty(final GenomicProperty gp, final String propName) {
         GenomicProperty retProp = null;
         if (gp.getName().equals(propName)) {
            retProp = gp;
         }
         else {
            final GenomicProperty[] subProps = gp.getSubProperties();
            if (subProps != null && subProps.length > 0) {
               for (int i = 0; i < subProps.length; i++) {
                  retProp = searchForProperty(subProps[i], propName);
                  if (retProp != null) {
                     break;
                  }
               }
            }

         }

         if (retProp != null) {
            return retProp;
         }
         else {
            return null;
         }

      }

      /**
       * This method REPLACES the GenomicProperty array for a given GenomicEntity.
       * Mechanism used so that we can set properties en masse without a notification
       * for each property being set.
       *
       * This method now assumes that the passed set is complete.  No calls to the
       * facades will made for properties after making this call.
       *
       * @throws ClassCastException if Set contains anything other than GenomicProperties
       */
      public void setProperties(final Set genomicProperties) {
         genomicProperties.toArray(new GenomicProperty[0]); //throw classCast
         properties = new ArrayList(genomicProperties);

         // Note that all the properties for this feature are loaded so that
         // subsequent requests dont cause an unecessary load.
         final LoadFilter propertyLoadFilter = new LoadFilter("Property");
         propertyLoadRequest = new LoadRequest(propertyLoadFilter);
         final LoadRequestStatus propertyLoadStatus = propertyLoadRequest.getLoadRequestStatus();
         propertyLoadStatus.setPendingLoadRequestAndStateToWaiting(propertyLoadRequest);
         propertyLoadStatus.setLoadRequestState(LoadRequestStatus.COMPLETE);

         GenomicEntity.this.postNotification(GenomicEntity.this, NOTE_ACTION_PROPERTIES, false);
      }

   }

}
