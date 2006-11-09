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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.util.*;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import api.entity_model.access.filter.AlignmentCollectionFilter;
import api.entity_model.access.filter.FiltrationDevice;
import api.entity_model.access.observer.AxisObserver;
import api.entity_model.access.observer.SequenceAnalysisObserver;
import api.entity_model.access.observer.SequenceObserver;
import api.entity_model.access.report.BlastParameters;
import api.entity_model.access.report.PropertyReport;
import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.AlignmentNotAllowedException;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.annotation.HitAlignmentFeature;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.facade.abstract_facade.annotations.GenewiseFacade;
import api.facade.abstract_facade.annotations.HSPFacade;
import api.facade.abstract_facade.annotations.Sim4HitFacade;
import api.facade.abstract_facade.fundtype.AxisLoader;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.LoginProperties;
import api.stub.data.ArrayListWrapper;
import api.stub.data.OID;
import api.stub.ejb.model.fundtype.AxisRemote;
import api.stub.geometry.MutableRange;
import api.stub.geometry.Range;
import api.stub.http.HttpConnectionFactory;
import api.stub.sequence.DNASequenceCache;
import api.stub.sequence.Sequence;
import api.stub.sequence.SubSequence;
import shared.util.Assert;
import shared.util.FreeMemoryWatcher;
import shared.util.PropertyConfigurator;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version $Id$
 */
public abstract class Axis extends AlignableGenomicEntity
        implements MessageListener {
    // Types of actions that Observers will be notified of.
    private static final int NOTE_ALIGNMENT_OF_ENTITY = 20;
    private static final int NOTE_UNALIGNMENT_OF_ENTITY = 21;
    private static final int NOTE_ENTITY_ALIGNMENT_CHANGED = 22;
    private static final int NOTE_SEQUENCE_ARRIVED = 23;
    private static final long serialVersionUID = 2;

    /**
     * @todo Store observers by correlation id or some other way to
     * determine which observer to notify as there could be more than
     * one blast in progress per user when convert the code to multi
     * threaded load.
     */
    private SequenceAnalysisObserver observer;

    /**
     * Classes required to make a JMS topic subscription for
     * retrieving sequence analysis results when published
     * from the server.
     */
    private TopicConnectionFactory topicConnFactory;
    private TopicConnection topicConn;
    private TopicSession topicSes;
    private TopicSubscriber topicSubscriber;
    private Topic topic;

    /**
     *@associates <{Alignment}>
     * @supplierCardinality 0..*
     * @label entity alignments
     */
    private HashSet alignmentsToEntities; //Alignment
    private Map currentLoadFilters; //LoadFilters
    private int magnitude;
    private boolean done = false; // used to keep open JMS session until done
    private transient DNASequenceCache sequenceCache;


    /**
     * @see GenomicEntity
     */
    public Axis(EntityType entityType, OID oid, String displayName,
                int magnitude) {
        this(entityType, oid, displayName, magnitude, null);
    }

    /**
     * @see GenomicEntity
     */
    public Axis(EntityType entityType, OID oid, String displayName,
                int magnitude, FacadeManagerBase overrideDataLoader) {
        super(entityType, oid, displayName, overrideDataLoader);
        this.magnitude = magnitude;
        sequenceCache = new DNASequenceCache(createLoader());
    }

    //* Visitiation

    /**
     * Accepts visitors that implement GenomicEntityVisitor.  Will vist all alignments
     *
     * @param theVisitor the visitor.
     * @param directAlignmentsOnly - if true will only walk to this axis' children
     *    if false, will walk to this axis children and any children of those children etc
     *    until no more axes exist
     */
    public void acceptVisitorForAlignedEntities(GenomicEntityVisitor theVisitor,
                                                boolean directAlignmentsOnly) {
        try {
            Alignment[] alignments = getAlignmentsToEntitiesArray();
            GenomicEntity entity;

            for (int i = 0; i < alignments.length; i++) {
                entity = alignments[i].getEntity();
                entity.acceptVisitorForSelf(theVisitor);

                if (!directAlignmentsOnly && entity instanceof Axis) {
                    ((Axis) entity).acceptVisitorForAlignedEntities(theVisitor,
                            false);
                }
            }
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    //*  Observation

    /**
     *  To track changes in the this object over time, you must add
     *  yourself as an observer of the it.  NOTE: this will post
     *  notices of all existing alignments
     *  @see AxisObserver
     */
    public void addAxisObserver(AxisObserver observer) {
        addAxisObserver(observer, true);
    }

    /**
     *  To track changes in the this object over time, you must add
     *  yourself as an observer of the it.  NOTE: this will optionally post
     *  notices of all existing alignments
     *  @see AxisObserver
     */
    public void addAxisObserver(AxisObserver observer, boolean bringUpToDate) {
        addAlignableGenomicEntityObserver(observer, bringUpToDate);

        if (bringUpToDate) {
            if (alignmentsToEntities != null) {
                Alignment[] alignments = getAlignmentsToEntitiesArray();

                for (int i = 0; i < alignments.length; i++) {
                    observer.noteAlignmentOfEntity(alignments[i]);
                }
            }
        }
    }

    /**
     * Remove an axisObserver
     */
    public void removeAxisObserver(AxisObserver observer) {
        removeAlignableGenomicEntityObserver(observer);
    }

    /**
     * Accepts visitors that implement GenomicEntityVisitor. acceptVisitorForSelf
     * is specialized by every subclass of GenomicEntity to call the appropriate
     * "visit...(...)" function on the passed visitor.
     * @param theVisitor the visitor.
     */
    public void acceptVisitorForSelf(GenomicEntityVisitor theVisitor) {
        try {
            theVisitor.visitAxis(this);
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * @return a Set of Alignments to the passed entity
     */
    public Set getLoadedAlignmentsToEntity(AlignableGenomicEntity entity) {
        Set entityAlignments = entity.getAlignmentsToAxes();
        Set rtnCollection = new HashSet();
        Alignment tmpAlignment;

        for (Iterator it = entityAlignments.iterator(); it.hasNext();) {
            tmpAlignment = (Alignment) it.next();

            if (tmpAlignment.getAxis() == this) {
                rtnCollection.add(tmpAlignment);
            }
        }

        return rtnCollection;
    }

    /**
     * Gets only those alignments that have currently been loaded to this
     * axis by previous calls to either of loadAlignmentsToEntitiesBackground
     * or loadAlignmentsToEntitiesBlocking.
     */
    public Set getLoadedAlignmentsToEntities() {
        //synchronized because alignmentsToEntities could go null after first line --PED 6/4/01
        synchronized (this) {
            if (alignmentsToEntities == null) {
                return Collections.EMPTY_SET;
            }

            return (Set) alignmentsToEntities.clone();
        }
    }

    /**
     * This method will return a subSet, sortedSet or sortedSubSet of alignments
     * to entities depending on the CollectionFilter.
     *
     * @see api.entity_model.access.filter.AlignmentCollectionFilter
     */
    public List getLoadedAlignmentsToEntities(AlignmentCollectionFilter filter) {
        return FiltrationDevice.getDevice()
                .executeAlignmentFilter(getLoadedAlignmentsToEntities(),
                        filter);
    }

    /**
     * Intended to be overridded on each axis to return it's loadFilters
     */
    public Set getDefaultLoadFilters() {
        return Collections.EMPTY_SET;
    }

    /**
     * Intended to allow addition of new LoadFilters (non-preDefined
     */
    public LoadFilter getLoadFilterNamed(String name) {
        if (currentLoadFilters == null) {
            currentLoadFilters = new HashMap();

            Collection defaults = getDefaultLoadFilters();
            LoadFilter filter;

            for (int i = 0; i < defaults.size(); i++) {
                filter = (LoadFilter) defaults;
                currentLoadFilters.put(filter.getFilterName(), filter);
            }
        }

        return (LoadFilter) currentLoadFilters.get(name);
    }

    /**
     * Loads the alignments for the entities that match the laodRequests filter
     * and that are aligned to this axis. The loading is done in the background
     * which means the client can continue processing other events and will need
     * to be an observer of this axis to receive notification of the "attachment"
     * of the returned alignments to the axis.
     *
     * @param loadRequest serves the purose of tracking the different load states
     * and of providing the requested set of filter options to restrict the set
     * of returned entity types by genomic entity type and axis range, amoung
     * other things.
     */
    public LoadRequestStatus loadAlignmentsToEntitiesBackground(LoadRequest loadRequest) {
        if (!willAcceptLoadRequestForAlignedEntities(loadRequest)) {
            throw new IllegalArgumentException("The LoadRequest passed to " +
                    this.toString() +
                    " cannot be used");
        }

        LoadFilter loadFilter = loadRequest.getLoadFilter();
        LoadRequestStatus status = loadRequest.getLoadRequestStatus();

        if (loadFilter.getLoadFilterStatus().isCompletelyLoaded()) {
            return status;
        }

        status.setPendingLoadRequestAndStateToWaiting(loadRequest);
        getLoaderThreadQueue()
                .addQueue(new AlignmentToEntityLoader(loadRequest, true));

        return status;
    }

    /**
     * UnLoads and returns the alignments for the entities that match
     * the loadRequests filter and that are aligned to this axis.
     * The unloading is done on the current thread and the alignments that
     * match the load request are returned as a collection from this method.
     *
     * @param loadRequest serves the purose of tracking the different load states
     * and of providing the requested set of filter options to restrict the set
     * of returned entity types by genomic entity type and axis range, amoung
     * other things.
     */
    public LoadRequestStatus unloadAlignmentsToEntitiesBackground(LoadRequest loadRequest) {
        if (!willAcceptLoadRequestForAlignedEntities(loadRequest)) {
            throw new IllegalArgumentException("The LoadRequest passed to " +
                    this.toString() +
                    " cannot be used");
        }

        LoadRequestStatus status = loadRequest.getLoadRequestStatus();
        status.addLoadRequestStatusObserver(FreeMemoryWatcher.getFreeMemoryWatcher()
                .getLoadStatusObserver(),
                true);
        status.setPendingLoadRequestAndStateToWaiting(loadRequest);
        getLoaderThreadQueue()
                .addQueue(new AlignmentToEntityUnLoader(loadRequest, true));

        return status;
    }

    /**
     * Loads and returns the alignments for the entities that match
     * the loadRequests filter and that are aligned to this axis.
     * The loading is done on the current thread and the alignments that
     * match the load request are returned as a collection from this method.
     *
     * @param loadRequest serves the purose of tracking the different load states
     * and of providing the requested set of filter options to restrict the set
     * of returned entity types by genomic entity type and axis range, amoung
     * other things.
     */
    public Set loadAlignmentsToEntitiesBlocking(LoadRequest loadRequest) {
        if (!willAcceptLoadRequestForAlignedEntities(loadRequest)) {
            throw new IllegalArgumentException("The LoadRequest passed to " +
                    this.toString() +
                    " cannot be used");
        }

        LoadFilter loadFilter = loadRequest.getLoadFilter();

        if (loadFilter.getLoadFilterStatus().isCompletelyLoaded()) {
            final Iterator it = loadRequest.getRequestedRanges().iterator();

            return new HashSet(getLoadedAlignmentsToEntities(
                    new AlignmentCollectionFilter() {
                        public boolean addAlignmentToReturnCollection(Alignment alignment) {
                            if (alignment instanceof GeometricAlignment) {
                                Range requestRange;

                                for (; it.hasNext();) {
                                    requestRange = (Range) it.next();

                                    if (requestRange.intersects(
                                            ((GeometricAlignment) alignment).getRangeOnAxis())) {
                                        return true;
                                    }
                                }
                            }

                            return false;
                        }
                    }));
        }

        AlignmentToEntityLoader loader = new AlignmentToEntityLoader(
                loadRequest, false);
        loader.run();

        return loader.getAlignments();
    }

    public void unloadWorkspaceEntities() {
        Alignment[] alignments = getAlignmentsToEntitiesArray();
        AlignableGenomicEntity entity;

        for (int i = 0; i < alignments.length; i++) {
            entity = alignments[i].getEntity();

            if (entity.isWorkspace()) {
                entity.unloadIfPossible(alignments[i]);
            }
        }
    }

    public void unloadCachedSequence() {
        sequenceCache.clear();
    }

    /**
     * UnLoads and returns the alignments for the entities that match
     * the loadRequests filter and that are aligned to this axis.
     * The unloading is done on the current thread and the alignments that
     * match the load request are returned as a collection from this method.
     *
     * @param loadRequest serves the purose of tracking the different load states
     * and of providing the requested set of filter options to restrict the set
     * of returned entity types by genomic entity type and axis range, amoung
     * other things.
     */
    public Set unloadAlignmentsToEntitiesBlocking(LoadRequest loadRequest) {
        if (!willAcceptLoadRequestForAlignedEntities(loadRequest)) {
            throw new IllegalArgumentException("The LoadRequest passed to " +
                    this.toString() +
                    " cannot be used");
        }

        AlignmentToEntityUnLoader loader = new AlignmentToEntityUnLoader(
                loadRequest, false);
        loader.run();

        return loader.getAlignments();
    }

    public Sequence getDNASequence() {
        return sequenceCache;
    }

    /**
     * A blocking request to get a nucleotide sequence
     */
    public Sequence getNucleotideSeq(Range range) {
        range = ensureRangeDoesNotExceedAxis(range);

        return new SubSequence(sequenceCache, range.getMinimum(),
                range.getMagnitude());
    }

    /**
     * A threaded request to get a nucleotide sequence
     */
    public void loadNucleotideSeq(Range range, SequenceObserver observer) {
        range = ensureRangeDoesNotExceedAxis(range);

        if (sequenceCache.isSequenceAvailable(range.getMinimum(),
                range.getMagnitude())) {
            observer.noteSequenceArrived(this, range,
                    new SubSequence(sequenceCache,
                            range.getMinimum(),
                            range.getMagnitude()));

            return;
        }

        LoadRequest request = new LoadRequest(new LoadFilter("Sequence"));
        request.getLoadRequestStatus()
                .setPendingLoadRequestAndStateToWaiting(request);

        SequenceLoader sequenceLoader = new SequenceLoader(range, request, observer);
        getLoaderThreadQueue().addQueue(sequenceLoader);
    }

    /**
     * A threaded request to get a nucleotide sequence
     */
    public LoadRequestStatus loadNucleotideSeq(Range range) {
        Assert.vAssert(false);
        range = ensureRangeDoesNotExceedAxis(range);

        LoadRequest request = new LoadRequest(new LoadFilter("Sequence"));
        LoadRequestStatus sequenceLoadStatus = request.getLoadRequestStatus();
        sequenceLoadStatus.setLoadRequestState(LoadRequestStatus.COMPLETE);

        return sequenceLoadStatus;

        /* @todo
               if (nucleotideRange.getMagnitude() == 0) {
                 sequenceLoadStatus.setLoadRequestState(LoadRequestStatus.COMPLETE);
                 return sequenceLoadStatus;
               }
               sequenceLoadStatus.setPendingLoadRequestAndStateToWaiting(request);
               SequenceLoader sequenceLoader = new SequenceLoader(nucleotideRange,request, true);
               getLoaderThreadQueue().addQueue(sequenceLoader);
               return sequenceLoadStatus;*/
    }

    /**
     * get the magnitude of this axis
     */
    public int getMagnitude() {
        return magnitude;
    }

    /**
     * @return the current number of alignments to the axis
     */
    public int getNumberOfAlignmentsToAxis() {
        synchronized (this) {
            if (alignmentsToEntities == null) {
                return 0;
            }

            return alignmentsToEntities.size();
        }
    }

    /**
     * Called by the JMS framework whenever a message for a topic that this
     * Axis subscribes to, arrives.
     */
    public void onMessage(Message msg) {
        try {
            // try sending my own ACK to eliminate duplicates
            msg.acknowledge();

            // Assure the correct ObjectMessage is received
            if (msg instanceof ObjectMessage == false) {
                handleException(
                        new RuntimeException(
                                "Axis received a JMS message " +
                        " of an unexpected message type. Expects ObjectMessage got " +
                        msg.getClass()));
            }

            // Handle the results
            final ObjectMessage objMsg = (ObjectMessage) msg;
            final ArrayListWrapper wrapper = (ArrayListWrapper) objMsg.getObject();
            this.handleBlastResults(wrapper);

            // Clean up the subscription to blast result publication
            // from the server.
            // NOTE: If decide to allow more than one blast at a time
            // then this will need to reference count the number of pending
            // blasts or wait for a "quit" message before cleaning up the topic
            if (done) {
                try {
                    closeBlastSubscription();
                } catch (Exception ex) {
                    handleException(ex);
                }
            }

        } catch (JMSException jmse) {
            handleException(jmse);
        }

    }

    /**
     * Handle returned Blast results
     * @param pWrapper contains the BLAST result information
     */
    protected void handleBlastResults(ArrayListWrapper pWrapper) {

        final List alignments = pWrapper.getArrayList();

        // Attach the aligned objects to the model. Should be ok to
        // do this here as we are not on the AWT-Event queue

        // Turn the array list into an array
        final Alignment[] alignArray = new Alignment[alignments.size()];
        alignments.toArray(alignArray);
        final Set alignmentsCollection = attachAlignmentsToModel(alignArray);

        // NOTE: Need to build the report using the returned set of
        // alignments as it is possible duplicates were detected and removed
        // by the call to attachAlignmentsToModel
        final PropertyReport report = buildSequenceAnalysisReport(alignmentsCollection);

        observer.noteSequenceAnalysisCompleted(this,
                new Range(0,
                        this.getMagnitude()),
                report, pWrapper.isDone());
    }

    /**
     * Performs an asychronous blast of the supplied <querySeq> against
     * the sequence within <rangeOnAxis> of this axis.
     * Upon completion notifies <observer> of any results.
     *
     * @param rangeOnAxis range of the axis that will be used to retrieve
     * sequence to act as the subject of the blast
     * @param querySeq query that will be blasted agaisnt the subject.
     * @param observer that will be notified of the results
     */
    public void runSequenceAnalysis(Range rangeOnAxis, Sequence subjectSeq,
                                    Sequence querySeq,
                                    SequenceAnalysisObserver observer,
                                    String groupTag,
                                    BlastParameters blastParameters) {
        this.observer = observer;

        // As this method will always resolve through EJB land currently it
        // uses only the raw EJB home and remote.
        AxisRemote axisRemote = getEJBDataLoader();

        //JmsBlastRunner blastRunner =
        //        new JmsBlastRunner(rangeOnAxis, subjectSeq, querySeq, axisRemote, groupTag, blastParameters, this);

        // todo: replace above JmsBlastRunner with below HttpBlastRunner
        HttpBlastRunner blastRunner =
                new HttpBlastRunner(rangeOnAxis, subjectSeq, querySeq, axisRemote, groupTag, blastParameters);

        getLoaderThreadQueue().addQueue(blastRunner);
    }

    /**
     * Performs an asychronous search of the supplied <querySeq> against
     * the sequence of this axis.
     * Upon completion notifies <observer> of any results.
     *
     * sequence to act as the subject of the blast
     * @param querySeq query that will be searched against the subject.
     * @param observer that will be notified of the results
     * @param groupTag logical name for grouping each feature found
     *        from the search execution
     * @param blastParameters object that represents the clients desired
     *        settings for effecting the characteristics of how the search
     *        algorithm is applied.
     */
    public void runSequenceAnalysis(Sequence subjectSeq, Sequence querySeq,
                                    SequenceAnalysisObserver observer,
                                    String groupTag,
                                    BlastParameters blastParameters) {
        runSequenceAnalysis(new Range(0, this.getMagnitude()), subjectSeq, querySeq,
                observer, groupTag, blastParameters);
    }

    /**
     * Returns a list of BlastAlgorithm instances that are
     * available to the currently logged in user.
     */
    public List getAvailableBlastAlgorithms() {
        // As this method will always resolve through EJB land currently it
        // uses only the raw EJB home and remote.
        AxisRemote axisRemote = getEJBDataLoader();
        BlastParameters.BlastAlgorithm[] algorithmsArray =
                new BlastParameters.BlastAlgorithm[0];

        try {
            algorithmsArray = axisRemote.getAvailableBlastAlgorithms(
                    this.getOid());
        } catch (Exception ex) {
            handleException(ex);
        }

        List algorithmsList = new ArrayList(algorithmsArray.length);
        algorithmsList = Arrays.asList(algorithmsArray);

        return algorithmsList;
    }

    //****************************************
    //*  Protected methods
    //****************************************

    /**
     * Close JMS objects.
     */
    protected void closeBlastSubscription() throws JMSException {
        topicSubscriber.close();
        topicSubscriber = null;
        topicSes.close();
        topicSes = null;
        topicConn.close();
        topicConn = null;
    }

    protected GenomicEntityMutator constructMyMutator() {
        return new AxisMutator();
    }

    /**
     * Post notification that an entity was aligned to this axis.
     */
    protected final void postAlignmentOfEntity(Alignment newAlignment) {
        new AxisNotificationObject(newAlignment, NOTE_ALIGNMENT_OF_ENTITY, true).run();
        getNotificationQueue()
                .addQueue(new AxisNotificationObject(newAlignment,
                        NOTE_ALIGNMENT_OF_ENTITY,
                        false));
    }

    /**
     * Post notification that an entity was unaligned to this axis.
     */
    protected final void postUnalignmentOfEntity(Alignment oldAlignment) {
        new AxisNotificationObject(oldAlignment, NOTE_UNALIGNMENT_OF_ENTITY,
                true).run();
        getNotificationQueue()
                .addQueue(new AxisNotificationObject(oldAlignment,
                        NOTE_UNALIGNMENT_OF_ENTITY,
                        false));
    }

    /**
     * Post notification that an alignment to an entity changed.
     */
    protected final void postEntityAlignmentChanged(Alignment changedAlignment) {
        new AxisNotificationObject(changedAlignment,
                NOTE_ENTITY_ALIGNMENT_CHANGED, true).run();
        getNotificationQueue()
                .addQueue(new AxisNotificationObject(changedAlignment,
                        NOTE_ENTITY_ALIGNMENT_CHANGED,
                        false));
    }

    /**
     *
     */

    /*
    protected final void postSequenceLoaded ( Range range, Sequence sequence ) {
     new AxisNotificationObject(
        range,sequence,NOTE_SEQUENCE_ARRIVED,
        true).run();
     getNotificationQueue().addQueue(new AxisNotificationObject(
        range,sequence,NOTE_SEQUENCE_ARRIVED,
        false));
    }*/

    /**
     * Aligned Parent Child Validation.   AxisProxies
     * MUST override, calling super using the class of the passed
     * AlignableGenomicEntityProxy to determine
     * if the alignment is valid
     */
    protected void willAcceptAlignmentToEntity(Alignment alignmentToEntity)
            throws AlignmentNotAllowedException {
        if ((alignmentsToEntities != null) &&
                alignmentsToEntities.contains(alignmentToEntity)) {
            throw new AlignmentNotAllowedException(true, alignmentToEntity);
        }
    }

    /**
     * Ask if load request can be accepted by the concrete class
     */
    protected abstract boolean willAcceptLoadRequestForAlignedEntities(LoadRequest loadRequest);

    /**
     * Return a predicted number of alignments to entities so initial size of
     * storage attribute can be set with some intelligence.  Give an accurate
     * prediction. Padding is added to prevent growth doubling.
     */
    protected abstract int getPredictedNumberOfAlignmentsToEntities();

    /**
     * This is a template pattern implementation that will be called whenever
     * the loadAlignmentsToEntities methods are called (blocking and background).
     * It allows a concrete class to calculate additional alignments that should
     * be returned.  It is used in the case of locally computed entity types,
     * such as start and stop codons and splice sites.
     */
    protected Alignment[] addLocallyComputedAlignmentsToEntities(LoadRequest loadRequest) {
        return new Alignment[0];
    }

    /**
     * Adds the ability for subClasses of the axis to further filter out
     * which alignments should be unloaded.
     */
    protected Collection removeAlignmentsThatShouldNotBeUnloaded(LoadRequest loadRequest,
                                                                 Collection alignmentsForUnloading,
                                                                 boolean deleteWorkSpaceEntities) {
        GenomicEntity entity;

        for (Iterator it = alignmentsForUnloading.iterator(); it.hasNext();) {
            entity = ((Alignment) it.next()).getEntity();

            if (!loadRequest.getLoadFilter().getEntityTypeSet()
                    .contains(entity.getEntityType())) {
                it.remove();
            }

            if (!deleteWorkSpaceEntities && entity.isWorkspace()) {
                it.remove();
            }
        }

        return alignmentsForUnloading;
    }

    /**
     * Axis subtypes must provide a way for getting a specific axis loader
     * subtype for the EJB protocol as the axis class performs operations that
     * are never performed under the other protocols.
     * @todo for expediency put in a default implementation. make abstract
     * again once all classes override
     */
    protected AxisRemote getEJBDataLoader() {
        throw new UnsupportedOperationException("Class " + this.getClass() +
                " does not yet override getEJBDataLoader");
    }

    //****************************************
    //*  Package methods
    //****************************************
    //****************************************
    //*  Private methods
    //****************************************
    private Range ensureRangeDoesNotExceedAxis(Range range) {
        Range tmpRange = range;

        if (range.isReversed()) {
            tmpRange = range.toReverse();
        }

        int mag = getMagnitude();

        if ((tmpRange.getEnd() > mag) || (tmpRange.getStart() < 0)) {
            MutableRange mutableTmpRange = tmpRange.toMutableRange();

            if (tmpRange.getEnd() > mag) {
                mutableTmpRange.change(tmpRange.getStart(), (mag - 1));
            }

            if (tmpRange.getStart() < 0) {
                mutableTmpRange.change(0, tmpRange.getEnd());
            }

            return mutableTmpRange;
        }

        return tmpRange;
    }

    private synchronized void ensureStorageForAlignmentsToEntitiesAvailable() {
        if (alignmentsToEntities == null) {
            int prediction = getPredictedNumberOfAlignmentsToEntities();
            int initialSize = (int) (prediction +
                    (PREDICTION_PADDING_FACTOR * prediction));
            alignmentsToEntities = new HashSet(initialSize);
        }
    }

    private synchronized void ensureStorageForAlignmentsToEntitiesRemoved() {
        if (alignmentsToEntities.size() == 0) {
            alignmentsToEntities = null;
        }
    }

    private synchronized Alignment[] getAlignmentsToEntitiesArray() {
        if (alignmentsToEntities == null) {
            return new Alignment[0];
        }

        return (Alignment[]) alignmentsToEntities.toArray(
                new Alignment[alignmentsToEntities.size()]);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        /*
         * Take care of this class's fields first by calling
         * defaultReadObject
         */
        in.defaultReadObject();


        /*
         * Since deserialization does not call the default contstructor or
        * instance initializer make sure here that any transient fields that
        * need initilization.
         */
        sequenceCache = new DNASequenceCache(createLoader());
    }

    /**
     * NOTE: This method should only be used when attaching objects to the
     * model that were NOT loaded using the standard LoadRequest
     * LoadRequestStatus mechanism
     */
    private Set attachAlignmentsToModel(Alignment[] alignments) {
        return attachAlignmentsToModel(alignments, false, false, null, null);
    }

    private Set attachAlignmentsToModel(Alignment[] alignments,
                                        boolean monitorState,
                                        boolean wasLoadRequestLoad,
                                        LoadRequest loadRequest,
                                        LoadRequestStatus loadRequestStatus) {
        Set alignmentsCollection = new HashSet();
        Alignment alignment;
        AxisMutator mutator = (AxisMutator) getMutator();

        for (int i = 0; i < alignments.length; i++) {
            alignment = alignments[i];

            try {
                //           alignment=gv.checkForPreviouslyLoadedEntityAndAddToLocalModel(alignment);
                alignment.setAxisIfNull(Axis.this);

                if (monitorState == true) {
                    alignment.getEntity().setMyLoadRequest(loadRequest);
                }

                mutator.addAlignmentToEntity(alignment);


                // WARNING: Order dependency!
                //
                // At this point everything is knitted back together. It is
                // important that the addition to the alignmentsCollection is
                // made AFTER the preceding calls in this loop
                alignmentsCollection.add(alignments[i]);
            } catch (AlignmentNotAllowedException anaEx) {
                if (wasLoadRequestLoad) {
                    loadRequestStatus.decrementNumberLoaded();

                    if (anaEx.isDuplcateAlignment()) {
                        //               System.out.println("Ignoring duplicate alignments.");
                    } else {
                        this.handleException(anaEx);
                    }
                }
            }
        }

        return alignmentsCollection;
    }

    /**
     * Method that assists in the construction of a report that conveys the results
     * of a sequence analysis.
     */
    private PropertyReport buildSequenceAnalysisReport(Set alignments) {
        PropertyReport report = new PropertyReport();

        // Create a map of alignments to hsp child alignments
        // that will be used to create each line item in the report
        Iterator analysisIter = alignments.iterator();
        Feature currentFeature = null;
        Feature aChildFeature = null;
        GeometricAlignment currentAlignment = null;
        PropertyReport.ReportLineItem aLineItem = null;
        Collection children = null;
        Iterator childIter = null;

        while (analysisIter.hasNext()) {
            currentAlignment = (GeometricAlignment) analysisIter.next();
            currentFeature = (Feature) currentAlignment.getEntity();

            if (currentFeature instanceof HitAlignmentFeature) {
                children = currentFeature.getSubFeatures();
                childIter = children.iterator();

                while (childIter.hasNext()) {
                    aChildFeature = (Feature) childIter.next();

                    GeometricAlignment tmpAlignment = (GeometricAlignment) aChildFeature.getOnlyAlignmentToAnAxis(
                            this);
                    aLineItem = new PropertyReport.ReportLineItem(
                            aChildFeature.getOid());
                    aLineItem.addProperty(FeatureFacade.GENOMIC_AXIS_ID_PROP,
                            this.getOid().toString());
                    aLineItem.addProperty(FeatureFacade.GENOMIC_AXIS_NAME_PROP,
                            this.getDisplayName());
                    aLineItem.addProperty(FeatureFacade.AXIS_BEGIN_PROP,
                            Integer.toString(
                                    tmpAlignment.getRangeOnAxis()
                            .getStart()));
                    aLineItem.addProperty(FeatureFacade.AXIS_END_PROP,
                            Integer.toString(
                                    tmpAlignment.getRangeOnAxis()
                            .getEnd()));

                    try {
                        if (aChildFeature.getEntityType().value() == EntityTypeConstants.High_Scoring_Pair) {
                            aLineItem.addProperty(HSPFacade.SUM_E_VAL_PROP,
                                    getPropertyForReport(
                                            aChildFeature,
                                            HSPFacade.SUM_E_VAL_PROP));
                            aLineItem.addProperty(HSPFacade.BIT_SCORE_PROP,
                                    getPropertyForReport(
                                            aChildFeature,
                                            HSPFacade.BIT_SCORE_PROP));
                            aLineItem.addProperty(
                                    HSPFacade.INDIVIDUAL_E_VAL_PROP,
                                    getPropertyForReport(aChildFeature,
                                            HSPFacade.INDIVIDUAL_E_VAL_PROP));
                            aLineItem.addProperty(
                                    HSPFacade.PERCENT_IDENTITY_PROP,
                                    getPropertyForReport(aChildFeature,
                                            HSPFacade.PERCENT_IDENTITY_PROP));
                        } else if (aChildFeature.getEntityType().value() == EntityTypeConstants.Genewise_Peptide_Hit_Part) {
                            aLineItem.addProperty(
                                    GenewiseFacade.PERCENT_HIT_IDENTITY_PROP,
                                    getPropertyForReport(aChildFeature,
                                            GenewiseFacade.PERCENT_HIT_IDENTITY_PROP));
                            aLineItem.addProperty(GenewiseFacade.BITS_PROP,
                                    getPropertyForReport(
                                            aChildFeature,
                                            GenewiseFacade.BITS_PROP));
                            aLineItem.addProperty(
                                    GenewiseFacade.FRAMESHIFTS_PROP,
                                    getPropertyForReport(aChildFeature,
                                            GenewiseFacade.FRAMESHIFTS_PROP));
                            aLineItem.addProperty(
                                    GenewiseFacade.PERCENT_LENGTH_PROP,
                                    getPropertyForReport(aChildFeature,
                                            GenewiseFacade.PERCENT_LENGTH_PROP));
                            aLineItem.addProperty(
                                    GenewiseFacade.SUBJECT_SEQ_LENGTH_PROP,
                                    getPropertyForReport(aChildFeature,
                                            GenewiseFacade.SUBJECT_SEQ_LENGTH_PROP));
                        } else if (aChildFeature.getEntityType().value() == EntityTypeConstants.Sim4_Feature_Detail) {
                            aLineItem.addProperty(
                                    Sim4HitFacade.PERCENT_HIT_IDENTITY_PROP,
                                    getPropertyForReport(aChildFeature,
                                            Sim4HitFacade.PERCENT_HIT_IDENTITY_PROP));
                            aLineItem.addProperty(
                                    Sim4HitFacade.SUBJECT_SEQ_ID_PROP,
                                    getPropertyForReport(aChildFeature,
                                            Sim4HitFacade.SUBJECT_SEQ_ID_PROP));
                            aLineItem.addProperty(
                                    Sim4HitFacade.PERCENT_LENGTH_PROP,
                                    getPropertyForReport(aChildFeature,
                                            Sim4HitFacade.PERCENT_LENGTH_PROP));
                            aLineItem.addProperty(
                                    Sim4HitFacade.SUBJECT_SEQ_LENGTH_PROP,
                                    getPropertyForReport(aChildFeature,
                                            Sim4HitFacade.SUBJECT_SEQ_LENGTH_PROP));
                        }
                    } catch (Exception ex) {
                        handleException(ex);
                    }

                    report.addLineItem(aLineItem);
                }
            }
        }

        return report;
    }

    /**
     * Helper method that does all the appropriate checks when building the report
     * properties.
     */
    private String getPropertyForReport(GenomicEntity entity, String property) {
        if (entity == null) {
            return "";
        }

        if ((entity.getProperty(property) == null) ||
                (entity.getProperty(property).getInitialValue() == null)) {
            return "N/A";
        } else {
            return entity.getProperty(property).getInitialValue();
        }
    }

    private DNASequenceCache.SequenceLoader createLoader() {
        return new DNASequenceCache.SequenceLoader() {
            public long axisLength() {
                return magnitude;
            }

            public Sequence load(long location, int length) {
                Range range = new Range((int) location,
                        (int) (location + length));
                Sequence bioSeq = ((AxisLoader) getDataLoader()).getNucleotideSeq(
                        getOid(), range, true);

                return bioSeq;
            }
        };
    }

    //****************************************
    //*  Inner classes
    //****************************************
    public class AxisMutator extends AlignableGenomicEntityMutator {
        private static final long serialVersionUID = 1L;

        protected AxisMutator() {
        }

        /**
         * Should be called to align an entity to this axis.
         * Should NOT be called if calling from AlignableEntity
         */
        public void addAlignmentToEntity(Alignment alignment)
                throws AlignmentNotAllowedException {
            // First check for previously loaded...
            alignment = getGenomeVersion()
                    .checkForPreviouslyLoadedEntityAndAddToLocalModel(alignment);

            AlignableGenomicEntity entity = alignment.getEntity();


            //           if (entity instanceof Feature)
            //             System.out.println("Alignment of OID "+entity.getOid()+" in the model.");
            willAcceptAlignmentToEntity(alignment);

            if (alignment.getAxis() != Axis.this) {
                throw new AlignmentNotAllowedException(
                        "The alignment passed to " + Axis.this +
                        " Axis for addition is refering" + "to axis " +
                        alignment.getAxis(), alignment);
            }

            AlignableGenomicEntityMutator entityMutator =
                    (AlignableGenomicEntityMutator) entity.getMutator();
            entityMutator.protectedAddAlignmentToAxis(alignment);
            ensureStorageForAlignmentsToEntitiesAvailable();
            synchronized (Axis.this) {
                alignmentsToEntities.add(alignment);
            }

            if (entity.getMyLoadRequest() != null) {
                entity.getMyLoadRequest().getLoadRequestStatus()
                        .incrementNumberAligned();
            }

            postAlignmentOfEntity(alignment);
            entity.postAlignedToAxis(alignment);
        }

        /**
         * Should be called to unalign an entity to this axis
         * Should NOT be called if using Entity API
         */
        public void removeAlignmentToEntity(Alignment alignment) {
            AlignableGenomicEntity entity = null;
            synchronized (Axis.this) {
                if (alignmentsToEntities == null) {
                    return;
                }

                entity = alignment.getEntity();

                AlignableGenomicEntityMutator entityMutator =
                        (AlignableGenomicEntityMutator) entity.getMutator();
                entityMutator.protectedRemoveAlignmentToAxis(alignment);
                postUnalignmentOfEntity(alignment);
                alignmentsToEntities.remove(alignment);
            }

            ensureStorageForAlignmentsToEntitiesRemoved();

            if (entity != null) {
                entity.postUnalignedFromAxis(alignment);
            }

            getGenomeVersion()
                    .removeEntityFromLocalModelIfNoLongerAligned(alignment.getEntity());
        }

        /**
         * Change the Range of a GeometricAlignment on this Axis.
         * Will only change the range if the GeometricAlignment is on this Axis.
         * Posts a notification of Alignment changed to the Axis observers,
         * as well as the observers of the AlignableGenomicEntity.
         */
        public void changeRangeOnAlignment(GeometricAlignment geoAlignment,
                                           Range newRange) {
            // Make sure it's one of my
            if (Axis.this != geoAlignment.getAxis()) {
                return;
            }

            // Alignment must be mutable...
            if (!(geoAlignment instanceof MutableAlignment)) {
                System.out.println(
                        "AxisMutator.changeRangeOnAlignment(); Attempted to change NON-MutableAlignment.");
                System.out.println("===> Alignment between Axis and " +
                        geoAlignment.getEntity().getEntityType()
                        .getEntityName());

                return;
            }

            ((MutableAlignment) geoAlignment).setNewRangeOnAxis(newRange);


            // Post the notification...
            postEntityAlignmentChanged(geoAlignment);

            AlignableGenomicEntity entity = geoAlignment.getEntity();

            if (entity != null) {
                entity.postAlignmentToAxisChanged(geoAlignment);
            }
        }

        /**
         * Change the Range of a GeometricAlignment on this Axis.
         * Will only change the range if the GeometricAlignment is on this Axis.
         * No notification is sent.
         */
        protected void changeRangeOnAlignmentNoNotification(GeometricAlignment geoAlignment,
                                                            Range newRange) {
            // Make sure it's one of my
            if (Axis.this != geoAlignment.getAxis()) {
                return;
            }

            // Alignment must be mutable...
            if (!(geoAlignment instanceof MutableAlignment)) {
                return;
            }

            ((MutableAlignment) geoAlignment).setNewRangeOnAxis(newRange);
        }
    }

    // notification
    protected class AxisNotificationObject
            extends AlignableEntityNotificationObject {
        private Range range;
        private Sequence sequence;

        protected AxisNotificationObject(Alignment changedAlignment,
                                         int notedAction,
                                         boolean notifyModelSyncObservers) {
            super(changedAlignment, notedAction, notifyModelSyncObservers);
        }

        protected AxisNotificationObject(Range range, Sequence sequence,
                                         int notedAction,
                                         boolean notifyModelSyncObservers) {
            super(notedAction, notifyModelSyncObservers);
            this.range = range;
            this.sequence = sequence;
        }

        protected Class getObserverFilteringClass() {
            return AxisObserver.class;
        }

        public void run() {
            switch (getNotedAction()) {
                case NOTE_ALIGNMENT_OF_ENTITY:
                    {
                        sendAlignmentOfEntityMessage();

                        break;
                    }

                case NOTE_UNALIGNMENT_OF_ENTITY:
                    {
                        sendUnalignmentOfEntityMessage();

                        break;
                    }

                case NOTE_ENTITY_ALIGNMENT_CHANGED:
                    {
                        sendEntityAlignmentChangedMessage();

                        break;
                    }

                case NOTE_SEQUENCE_ARRIVED:
                    {
                        sendSequenceArrivedMessage();

                        break;
                    }

                default:
                    super.run();
            }
        }

        private void sendSequenceArrivedMessage() {
            AxisObserver observer;
            List observers = getObserversToNotifyAsList();

            for (int i = 0; i < observers.size(); i++) {
                observer = (AxisObserver) observers.get(i);
                observer.noteSequenceArrived(Axis.this, range, sequence);
            }
        }

        private void sendAlignmentOfEntityMessage() {
            AxisObserver observer;
            List observers = getObserversToNotifyAsList();
            ;

            for (int i = 0; i < observers.size(); i++) {
                observer = (AxisObserver) observers.get(i);
                observer.noteAlignmentOfEntity(getChangedAlignment());
            }

            LoadRequest lr = getChangedAlignment().getEntity()
                    .getMyLoadRequest();

            if ((lr != null) && !isNotifingModelSyncObservers()) {
                lr.getLoadRequestStatus().incrementNumberNotified();
                getChangedAlignment().getEntity().setMyLoadRequest(null);
            }
        }

        private void sendUnalignmentOfEntityMessage() {
            AxisObserver observer;
            List observers = getObserversToNotifyAsList();

            for (int i = 0; i < observers.size(); i++) {
                observer = (AxisObserver) observers.get(i);
                observer.noteUnalignmentOfEntity(getChangedAlignment());
            }

            LoadRequest lr = getChangedAlignment().getEntity()
                    .getMyLoadRequest();

            if ((lr != null) && !isNotifingModelSyncObservers()) {
                lr.getLoadRequestStatus().incrementNumberNotified();
                getChangedAlignment().getEntity().setMyLoadRequest(null);
            }
        }

        private void sendEntityAlignmentChangedMessage() {
            AxisObserver observer;
            List observers = getObserversToNotifyAsList();

            for (int i = 0; i < observers.size(); i++) {
                observer = (AxisObserver) observers.get(i);
                observer.noteEntityAlignmentChanged(getChangedAlignment());
            }
        }
    }

    class AlignmentToEntityLoader extends LoaderThreadBase {
        private Set alignmentsCollection = null;
        private boolean monitorState;

        public AlignmentToEntityLoader(LoadRequest loadRequest,
                                       boolean monitorState) {
            super(loadRequest);
            this.monitorState = monitorState;
        }

        public void run() {
            if (monitorState == true) {
                loadRequestStatus.setLoadRequestState(LoadRequestStatus.LOADING);
            }

            try {
                AxisLoader loader = (AxisLoader) getDataLoader();

                //             long t1=System.currentTimeMillis();
                Alignment[] alignments = loader.loadAlignmentsToEntities(
                        getOid(), loadRequest);
                Alignment[] moreAlignments = addLocallyComputedAlignmentsToEntities(
                        loadRequest);

                if (moreAlignments.length > 0) {
                    Alignment[] allAlignments = new Alignment[alignments.length +
                            moreAlignments.length];
                    System.arraycopy(alignments, 0, allAlignments, 0,
                            alignments.length);

                    if (alignments.length != 0) {
                        System.arraycopy(moreAlignments, 0, allAlignments,
                                alignments.length + 1,
                                moreAlignments.length);
                    } else { // User only selected client-side computed features...
                        System.arraycopy(moreAlignments, 0, allAlignments,
                                alignments.length,
                                moreAlignments.length);
                    }

                    alignments = allAlignments;
                }

                //              long t2=System.currentTimeMillis();
                //              System.out.println("Num features: "+alignments.length + " in "+((t2-t1)/1000f)+" seconds");
                if (monitorState == true) {
                    loadRequestStatus.setNumberLoaded(alignments.length);
                    loadRequestStatus.setLoadRequestState(
                            LoadRequestStatus.LOADED);
                } else {
                    loadRequest.getLoadFilter().getLoadFilterStatus()
                            .requestCompleted(loadRequest);
                }

                alignmentsCollection = attachAlignmentsToModel(alignments,
                        monitorState,
                        true,
                        loadRequest,
                        loadRequestStatus);
            } catch (Exception ex) {
                handleException(ex);
            }
        }

        public Set getAlignments() {
            return alignmentsCollection;
        }
    }

    class AlignmentToEntityUnLoader extends LoaderThreadBase {
        private Set alignmentsCollection = new HashSet();
        private boolean monitorState;

        public AlignmentToEntityUnLoader(LoadRequest loadRequest,
                                         boolean monitorState) {
            super(loadRequest);
            this.monitorState = monitorState;
        }

        public void run() {
            if (monitorState == true) {
                loadRequestStatus.setLoadRequestState(
                        LoadRequestStatus.UNLOADING);
            }

            try {
                AlignmentCollectionFilter filter = AlignmentCollectionFilter.createAlignmentCollectionFilter(
                        loadRequest.getLoadFilter()
                        .getEntityTypeSet(),
                        loadRequest.getRequestedRanges(),
                        loadRequest.getLoadFilter()
                        .isStrandSpecific());
                Collection filteredSet = getLoadedAlignmentsToEntities(filter);
                filteredSet = removeAlignmentsThatShouldNotBeUnloaded(
                        loadRequest, filteredSet, false);

                GeometricAlignment geoAlignment;
                AlignableGenomicEntity entity;
                int numberUnloaded = 0;

                for (Iterator alignmentItr = filteredSet.iterator();
                     alignmentItr.hasNext();) {
                    geoAlignment = (GeometricAlignment) alignmentItr.next();
                    entity = geoAlignment.getEntity();
                    entity.setMyLoadRequest(loadRequest);

                    if (entity.unloadIfPossible(geoAlignment) > 0) {
                        numberUnloaded++;
                    } else {
                        entity.setMyLoadRequest(null);
                    }
                }

                if (monitorState == true) {
                    loadRequestStatus.setNumberUnloaded(numberUnloaded);
                    loadRequestStatus.setLoadRequestState(
                            LoadRequestStatus.UNLOADED);

                    if (numberUnloaded == 0) {
                        loadRequestStatus.setLoadRequestState(
                                LoadRequestStatus.COMPLETE);
                    }
                }
            } catch (Exception ex) {
                handleException(ex);
            }
        }

        public Set getAlignments() {
            return alignmentsCollection;
        }
    }

    class SequenceLoader extends LoaderThreadBase {
        private Range requestedRange;
        private Sequence returnedSequence;
        private SequenceObserver observer;

        public SequenceLoader(Range range, LoadRequest request) {
            super(request);
            requestedRange = range;
        }

        public SequenceLoader(Range range, LoadRequest request,
                              SequenceObserver observer) {
            this(range, request);
            this.observer = observer;
        }

        public void run() {
            if (loadRequestStatus != null) {
                loadRequestStatus.setLoadRequestState(LoadRequestStatus.LOADING);
            }

            sequenceCache.ensureDataAvailability(requestedRange.getMinimum(),
                    requestedRange.getMagnitude());
            returnedSequence = new SubSequence(sequenceCache,
                    requestedRange.getMinimum(),
                    requestedRange.getMagnitude());

            if (loadRequestStatus != null) {
                loadRequestStatus.setLoadRequestState(LoadRequestStatus.LOADED);


                //postSequenceLoaded(requestedRange,returnedSequence); what's that???
                loadRequestStatus.setLoadRequestState(
                        LoadRequestStatus.NOTIFIED);
                loadRequestStatus.setLoadRequestState(
                        LoadRequestStatus.COMPLETE);
            }

            if (observer != null) {
                getNotificationQueue()
                        .addQueue(new SequenceNotification(observer, requestedRange,
                                returnedSequence));
            }
        }

        Sequence getReturnedSequence() {
            return returnedSequence;
        }
    }

    private class SequenceNotification implements Runnable {
        private SequenceObserver observer;
        private Range request;
        private Sequence sequence;

        private SequenceNotification(SequenceObserver observer, Range request,
                                     Sequence sequence) {
            this.observer = observer;
            this.request = request;
            this.sequence = sequence;
        }

        public void run() {
            observer.noteSequenceArrived(Axis.this, request, sequence);
        }
    }

    private final class HttpBlastRunner implements Runnable {
        private final Range rangeOnAxis;
        private final Sequence subjectSeq;
        private final Sequence querySeq;
        private final AxisRemote axisRemote;
        private final String groupTag;
        private final BlastParameters blastParams;

        private HttpBlastRunner(Range request,
                                Sequence subjectSeq, Sequence querySeq, AxisRemote axisRemote,
                                String groupTag, BlastParameters blastParameters) {
            this.rangeOnAxis = request;
            this.subjectSeq = subjectSeq;
            this.querySeq = querySeq;
            this.axisRemote = axisRemote;
            this.groupTag = groupTag;
            blastParams = blastParameters;
        }

        public void run() {
            try {
                // Submit the blast request
                String key = axisRemote.runBlast(Axis.this.getOid(), rangeOnAxis, subjectSeq, querySeq,
                        groupTag, blastParams);

                // Register results-polling Thread
                getLoaderThreadQueue().addQueue(new HttpBlastResultPoller(key));
            } catch (Exception ex) {
                handleException(ex);
            }
        }
    }

    /** Base URL string to query BLAST results from the server via http */
    private static String BLAST_RESULTS_URL_STRING;

    static {
        //This code is only relevant on the Client so it won't find the server connection properties file
        try {
			StringBuffer sbuf = new StringBuffer(64);
			sbuf.append("http://");
			String propsFile = PropertyConfigurator.getProperties().getProperty("x.genomebrowser.ServerConnectionProperties");
			ResourceBundle serverResourceBundle;
			serverResourceBundle = ResourceBundle.getBundle(propsFile);
			String serverHttpURL = serverResourceBundle.getString("HttpServer");
			sbuf.append(serverHttpURL);
			sbuf.append("/manage");
			sbuf.append("/BlastResultsPickup?id=");
			BLAST_RESULTS_URL_STRING = sbuf.toString();
        }
        catch(MissingResourceException mrEx) {
			BLAST_RESULTS_URL_STRING = "http://localhost:88";
            // Avoids the class not being initialized when prop is missing
            //serverHttpURL = "localhost:80";
            //mrEx.printStackTrace();
        }

        
        //System.out.println("httpServer="+BLAST_RESULTS_URL_STRING);
    }

    private final class HttpBlastResultPoller implements Runnable {
        private static final long WAIT_TIME_MS = 10000;
        private static final long INIT_WAIT_TIME_MS = 5000;
		private String key = null;

		public HttpBlastResultPoller(String uniqkey) {
			key = uniqkey;
		}


        public void run() {
            HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory();

            // Get server login information
            final Properties props = PropertyConfigurator.getProperties();
            final String serverLoginName = props.getProperty(LoginProperties.SERVER_LOGIN_NAME);
            final String serverLoginPassword = props.getProperty(LoginProperties.SERVER_LOGIN_PASSWORD);
            httpConnectionFactory.setServerLoginName(serverLoginName);
            httpConnectionFactory.setServerLoginPassword(serverLoginPassword);

            // Compose the URL
            final String urlSpec = BLAST_RESULTS_URL_STRING + key;
            httpConnectionFactory.setUrlSpec(urlSpec);

            // Poll for Results
            for (boolean doneFlag = false; doneFlag == false;) {
                try {
					//System.out.print("Waiting 5 seconds...");
					Thread.sleep(INIT_WAIT_TIME_MS);//Thread.currentThread().wait(INIT_WAIT_TIME_MS);
                	//System.out.println("Done");
                    try {                    	
                        final HttpURLConnection urlConn = httpConnectionFactory.generateHttpURLConnection();
                        final InputStream inStrm = urlConn.getInputStream();
                        //System.out.println("Connected to remote server");
                        final BufferedInputStream bInStrm = new BufferedInputStream(inStrm);
                        final ObjectInputStream objInStr = new ObjectInputStream(bInStrm);
                        final Object obj = objInStr.readObject();
                        if (obj instanceof ArrayListWrapper) {
                            doneFlag = true; // trigger loop exit before possible exception in processing
                            final ArrayListWrapper wrapper = (ArrayListWrapper) obj;
                            Axis.this.handleBlastResults(wrapper);
                        }
                        objInStr.close();
                        bInStrm.close();
                        inStrm.close();
                    } catch (Exception ex) {
                        ex.printStackTrace(); // continue with loop
                    }

                    // If the response is not ready, wait and retry
                    if (doneFlag == false) {
                    	//System.out.print("Waiting 10 seconds...");
						Thread.sleep(WAIT_TIME_MS); //Thread.currentThread().wait(WAIT_TIME_MS);
                        //System.out.println("done");
                    }
                } catch (InterruptedException intEx) {
                    // continue with loop
                }
            }
        }
    }

    private final class JmsBlastRunner implements Runnable {
        public final static String JNDI_FACTORY = "jrun.naming.JRunContextFactory";
        public final static String JMS_FACTORY = "jms/rmi-TopicConnectionFactory";
        private Range rangeOnAxis;
        private Sequence subjectSeq;
        private Sequence querySeq;
        private AxisRemote axisRemote;
        private String groupTag;
        private BlastParameters blastParams;
        private MessageListener listener;

        private JmsBlastRunner(Range request,
                               Sequence subjectSeq, Sequence querySeq, AxisRemote axisRemote,
                               String groupTag, BlastParameters blastParameters, MessageListener l) {
            this.rangeOnAxis = request;
            this.subjectSeq = subjectSeq;
            this.querySeq = querySeq;
            this.axisRemote = axisRemote;
            this.groupTag = groupTag;
            this.listener = l;
            blastParams = blastParameters;
        }

        public void run() {
            InitialContext jmsCtx = null;

            try {
                // Now set up a JMS topic that will be used to monitor the return results
                // from the blast
                // see if already initialized
                if (topicConn == null) {
                    Hashtable env = new Hashtable();
                    env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);

                    String serverURL = FacadeManager.getFacadeManager(
                            FacadeManager.getEJBProtocolString())
                            .getServerName();
                    env.put(Context.PROVIDER_URL, serverURL);
                    env.put(Context.SECURITY_PRINCIPAL, "jms_user");
                    env.put(Context.SECURITY_CREDENTIALS, "jms_user");

                    jmsCtx = new InitialContext(env);

                    Object o = jmsCtx.lookup(JMS_FACTORY);
                    //System.out.println("Found JMS_FACTORY: " + o.getClass().getName());

                    /*System.out.println("**********************************\nEnvironment:");
                    Hashtable ht = jmsCtx.getEnvironment();
                    for (Enumeration i = ht.keys(); i.hasMoreElements();) {
                        String key = (String) i.nextElement();
                        Object o1 = ht.get(key);
                        System.out.println(o1.toString());
                    }*/
                    System.out.println("topicConnFactory= " + jmsCtx.lookup(JMS_FACTORY).getClass().getName());
                    topicConnFactory = (TopicConnectionFactory) jmsCtx.lookup(
                            JMS_FACTORY);


                    topicConn = topicConnFactory.createTopicConnection();
                    topicSes = topicConn.createTopicSession(false,
                            Session.AUTO_ACKNOWLEDGE);

                    String topicName = "jms/queue/" + PropertyConfigurator.getProperties()
                            .getProperty(LoginProperties.SERVER_LOGIN_NAME);


                    // NOTE: Must convert the username to all lowercase to match what
                    // the eRights security realm implementation does
                    topicName = topicName.toLowerCase();
                    subscribeToTopic(jmsCtx, topicName);
                } // end of getting new subscription to topic


                // Run the blast request
                axisRemote.runBlast(Axis.this.getOid(), rangeOnAxis, subjectSeq, querySeq,
                        groupTag, blastParams);
            } catch (Exception ex) {
                handleException(ex);
            }
        }

        protected void subscribeToTopic(InitialContext jmsCtx, String topicName)
                throws JMSException, NamingException {
            try {
                topic = (Topic) jmsCtx.lookup(topicName);
            } catch (NamingException ne) {
                topic = topicSes.createTopic(topicName);
                jmsCtx.bind(topicName, topic);
            }

            topicSubscriber = topicSes.createSubscriber(topic);
            topicSubscriber.setMessageListener(listener);
            topicConn.start();
        }
    }
}