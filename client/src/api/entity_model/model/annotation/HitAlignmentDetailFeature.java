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
 * Title:        Genome Browser<p>
 * Description:  <p>
 * @author Peter Davies
 * @version $Id$
 */
package api.entity_model.model.annotation;

import api.entity_model.access.observer.HitAlignmentDetailFeatureObserver;
import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.model.fundtype.EntityType;
import api.facade.abstract_facade.annotations.HitAlignmentDetailLoader;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.OID;
import api.stub.data.SubjectDefinition;

import java.util.ArrayList;
import java.util.Collection;


public class HitAlignmentDetailFeature
    extends ComputedSingleAlignSingleAxisFeature implements SubFeature {
    static private final short QUERY_RESIDUES = 0;
    static private final short SUBJECT_RESIDUES = 1;
    private String queryAlignedResidues;
    private String subjectAlignedResidues;
    private ArrayList subjectDefinitions;

    /**
     * Constructor...
     */
    public HitAlignmentDetailFeature(OID oid, String displayName, 
                                     EntityType type, 
                                     String discoveryEnvironment)
                              throws InvalidFeatureStructureException {
        this(oid, displayName, type, discoveryEnvironment, null, null, 
             FeatureDisplayPriority.DEFAULT_PRIORITY);
    }

    public HitAlignmentDetailFeature(OID oid, String displayName, 
                                     EntityType type, 
                                     String discoveryEnvironment, 
                                     FacadeManagerBase readFacadeManager, 
                                     Feature superFeature, byte displayPriority)
                              throws InvalidFeatureStructureException {
        super(oid, displayName, type, discoveryEnvironment, readFacadeManager, 
              superFeature, displayPriority);
    }

    /**
     * Loads all subject definitions for this HitAlignment each time it is called.
     * Blocks the caller until all subject definitions are known and then
     * immediately returns the subject definitions as an array to the caller.
     *
     * @return a homogenous collection of SubjectDefinition instances.
     */
    public String loadQueryAlignedResiduesBlocking() {
        if (queryAlignedResidues == null) {
            AlignedResiduesLoader loader = new AlignedResiduesLoader(
                                                   QUERY_RESIDUES);
            loader.run();
        }

        return queryAlignedResidues;
    }

    /**
     * Loads the subject sequence definitions of this HitAlignmentFeature in the
     * background, immediately returning to the caller. Once the subject sequnce
     * definitions are loaded the observer will be given the appropriate callback.
     */
    public void loadQueryAlignedResiduesBackground(HitAlignmentDetailFeatureObserver observer) {
        boolean notifyOnly = false;

        if (queryAlignedResidues != null) {
            notifyOnly = true;
        }

        AlignedResiduesLoader loader = new AlignedResiduesLoader(QUERY_RESIDUES, 
                                                                 observer, 
                                                                 notifyOnly);
        getLoaderThreadQueue().addQueue(loader);
    }

    /**
     * Loads all subject definitions for this HitAlignment each time it is called.
     * Blocks the caller until all subject definitions are known and then
     * immediately returns the subject definitions as an array to the caller.
     *
     * @return a homogenous collection of SubjectDefinition instances.
     */
    public String loadSubjectAlignedResiduesBlocking() {
        if (subjectAlignedResidues == null) {
            AlignedResiduesLoader loader = new AlignedResiduesLoader(
                                                   SUBJECT_RESIDUES);
            loader.run();
        }

        return subjectAlignedResidues;
    }

    /**
     * Loads the subject sequence definitions of this HitAlignmentFeature in the
     * background, immediately returning to the caller. Once the subject sequnce
     * definitions are loaded the observer will be given the appropriate callback.
     */
    public void loadSubjectAlignedResiduesBackground(HitAlignmentDetailFeatureObserver observer) {
        boolean notifyOnly = false;

        if (subjectAlignedResidues != null) {
            notifyOnly = true;
        }

        AlignedResiduesLoader loader = new AlignedResiduesLoader(
                                               SUBJECT_RESIDUES, observer, 
                                               notifyOnly);
        getLoaderThreadQueue().addQueue(loader);
    }

    /**
     * Loads all subject definitions for this HitAlignment detail each time it
     * is called. Blocks the caller until all subject definitions are known and then
     * immediately returns the subject definitions as an array to the caller.
     *
     * @return a homogenous collection of SubjectDefinition instances.
     */
    public Collection loadSubjectDefinitionsBlocking() {
        if (subjectDefinitions == null) {
            SubjectDefinitionLoader loader = new SubjectDefinitionLoader();
            loader.run();

            // NOTE: The loader will populate subjectDefinitions
        }

        return (Collection) subjectDefinitions.clone();
    }

    /**
     * Loads the subject sequence definitions of this HitAlignmentFeature detail
     * in the background, immediately returning to the caller. Once the subject sequnce
     * definitions are loaded the observer will be given the appropriate callback.
     */
    public void loadSubjectDefinitionsBackground(HitAlignmentDetailFeatureObserver observer) {
        boolean notifyOnly = false;

        if (subjectDefinitions != null) {
            notifyOnly = true;
        }

        SubjectDefinitionLoader loader = new SubjectDefinitionLoader(observer, 
                                                                     notifyOnly);
        getLoaderThreadQueue().addQueue(loader);
    }

    /**
     * Override from Feature to support the pre-population of
     * any data that can be cached into attributes of this Feature
     */
    public void loadCachableData() {
        super.loadCachableData();
        this.loadQueryAlignedResiduesBlocking();
        this.loadSubjectAlignedResiduesBlocking();
    }

    /**
     * Accepts visitors that implement GenomicEntityVisitor. acceptVisitorForSelf
     * is specialized by every subclass of GenomicEntity to call the appropriate
     * "visit...(...)" function on the passed visitor.
     * @param theVisitor the visitor.
     */
    public void acceptVisitorForSelf(GenomicEntityVisitor theVisitor) {
        try {
            theVisitor.visitHitAlignmentDetailFeature(this);
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    class AlignedResiduesLoader implements Runnable, java.io.Serializable {
        private HitAlignmentDetailFeatureObserver observer;
        private boolean notifyOnly;
        private short loadType;

        public AlignedResiduesLoader(short loadType) {
            this.loadType = loadType;
            this.observer = null;
        }

        public AlignedResiduesLoader(short loadType, 
                                     HitAlignmentDetailFeatureObserver observer, 
                                     boolean notifyOnly) {
            this.loadType = loadType;
            this.observer = observer;
            this.notifyOnly = notifyOnly;
        }

        public void run() {
            try {
                if (!notifyOnly) {
                    HitAlignmentDetailLoader loader = (HitAlignmentDetailLoader) getDataLoader();

                    if (loadType == QUERY_RESIDUES) {
                        queryAlignedResidues = loader.getQueryAlignedResidues(
                                                       getOid());
                    } else {
                        subjectAlignedResidues = loader.getSubjectAlignedResidues(
                                                         getOid());
                    }
                }

                if (observer != null) {
                    getNotificationQueue()
                        .addQueue(new AlignedResiduesNotifier(observer, 
                                                              loadType));
                }
            } catch (Exception ex) {
                handleException(ex);
            }
        }
    }

    private class AlignedResiduesNotifier implements Runnable {
        private HitAlignmentDetailFeatureObserver observer;
        private int loadType;

        private AlignedResiduesNotifier(HitAlignmentDetailFeatureObserver observer, 
                                        int loadType) {
            this.observer = observer;
            this.loadType = loadType;
        }

        public void run() {
            switch (loadType) {
            case QUERY_RESIDUES:
                observer.noteQueryAlignedResiduesLoaded(
                        HitAlignmentDetailFeature.this, queryAlignedResidues);

                break;

            case SUBJECT_RESIDUES:
                observer.noteSubjectAlignedResiduesLoaded(
                        HitAlignmentDetailFeature.this, subjectAlignedResidues);

                break;
            }
        }
    }

    class SubjectDefinitionLoader implements Runnable, java.io.Serializable {
        private HitAlignmentDetailFeatureObserver observer;
        private boolean notifyOnly;

        public SubjectDefinitionLoader() {
            this.observer = null;
        }

        public SubjectDefinitionLoader(HitAlignmentDetailFeatureObserver observer, 
                                       boolean notifyOnly) {
            this.observer = observer;
            this.notifyOnly = notifyOnly;
        }

        public void run() {
            try {
                if (!notifyOnly) {
                    HitAlignmentDetailLoader loader = (HitAlignmentDetailLoader) getDataLoader();
                    SubjectDefinition[] loaderDefs = loader.getSubjectDefinitions(
                                                             getOid());


                    // Always re-initialize the outer classes subjectDefinitions
                    // in case called multiple times to avoid getting duplicates
                    subjectDefinitions = new ArrayList();

                    for (int i = 0; i < loaderDefs.length; i++) {
                        subjectDefinitions.add(loaderDefs[i]);
                    }
                }

                if (observer != null) {
                    getNotificationQueue()
                        .addQueue(new SubjectDefinitionNotifier(observer, 
                                                                (Collection) subjectDefinitions.clone()));
                }
            } catch (Exception ex) {
                handleException(ex);
            }
        }
    }

    private class SubjectDefinitionNotifier implements Runnable {
        private Collection subjectDefinitions;
        private HitAlignmentDetailFeatureObserver observer;

        private SubjectDefinitionNotifier(HitAlignmentDetailFeatureObserver observer, 
                                          Collection subjectDefinitions) {
            this.observer = observer;
            this.subjectDefinitions = subjectDefinitions;
        }

        public void run() {
            observer.noteSubjectDefsLoaded(HitAlignmentDetailFeature.this, 
                                           subjectDefinitions);
        }
    }
}