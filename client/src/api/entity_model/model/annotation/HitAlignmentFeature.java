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

import api.entity_model.access.observer.HitAlignmentFeatureObserver;
import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.model.fundtype.EntityType;
import api.facade.abstract_facade.annotations.HitAlignmentFacade;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;
import api.stub.data.SubjectDefinition;
import api.stub.sequence.Sequence;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;


public class HitAlignmentFeature extends ComputedSingleAlignSingleAxisFeature
    implements SuperFeature {
    //private OID subjectSeqOID = null;
    private Sequence subjectSequence = null;
    private ArrayList subjectDefinitions = null;

    /**
     * Constructor...
     */
    public HitAlignmentFeature(OID oid, String displayName, EntityType type, 
                               String discoveryEnvironment)
                        throws InvalidFeatureStructureException {
        this(oid, displayName, type, discoveryEnvironment, null, null, 
             FeatureDisplayPriority.DEFAULT_PRIORITY);
    }

    public HitAlignmentFeature(OID oid, String displayName, EntityType type, 
                               String discoveryEnvironment, 
                               FacadeManagerBase readFacadeManager, 
                               Feature superFeature, byte displayPriority)
                        throws InvalidFeatureStructureException {
        super(oid, displayName, type, discoveryEnvironment, readFacadeManager, 
              superFeature, displayPriority);
    }

    /**
     * Get the descriptive text.
     * @ToDo: Need HitAlignmentFacade.
     */
    public String getDescriptiveText() {
        GenomicProperty descriptionProp = getProperty(
                                                  HitAlignmentFacade.DESCRIPTION_PROP);

        if (descriptionProp == null) {
            return (super.getDescriptiveText());
        }

        return (descriptionProp.getInitialValue());
    }

    /**
     * Loads all subject definitions for this HitAlignment each time it is called.
     * Blocks the caller until all subject definitions are known and then
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
     * Loads the subject sequence definitions of this HitAlignmentFeature in the
     * background, immediately returning to the caller. Once the subject sequnce
     * definitions are loaded the observer will be given the appropriate callback.
     */
    public void loadSubjectDefinitionsBackground(HitAlignmentFeatureObserver observer) {
        boolean notifyOnly = false;

        if (subjectDefinitions != null) {
            notifyOnly = true;
        }

        SubjectDefinitionLoader loader = new SubjectDefinitionLoader(observer, 
                                                                     notifyOnly);
        getLoaderThreadQueue().addQueue(loader);
    }

    /**
     * Loads the subject sequence for this HitAlignmentFeature each time it is
     * called. Blocks the caller until the subject sequence is known and then
     * immediately returns the subject sequence to the caller.
     *
     * @return an instance of DNASequence.
     */
    public Sequence loadSubjectSequenceBlocking() {
        if (subjectSequence == null) {
            SubjectSequenceLoader loader = new SubjectSequenceLoader();
            loader.run();

            // NOTE: The loader will populate subjectSequence
        }

        return subjectSequence;
    }

    /**
     * Loads the subject sequence for this HitAlignmentFeature in the background,
     * immediately returning to the caller. Once the subject sequnce is loaded
     * the observer will be given the appropriate callback.
     */
    public void loadSubjectSequenceBackground(HitAlignmentFeatureObserver observer) {
        boolean notifyOnly = false;

        if (subjectSequence != null) {
            notifyOnly = true;
        }

        SubjectSequenceLoader loader = new SubjectSequenceLoader(observer, 
                                                                 notifyOnly);
        this.getLoaderThreadQueue().addQueue(loader);
    }

    /**
     * Override from Feature to support the pre-population of
     * any data that can be cached into attributes of this Feature
     */
    public void loadCachableData() {
        super.loadCachableData();
        this.loadSubjectDefinitionsBlocking();
        this.loadSubjectSequenceBlocking();
    }

    /**
     * Accepts visitors that implement GenomicEntityVisitor. acceptVisitorForSelf
     * is specialized by every subclass of GenomicEntity to call the appropriate
     * "visit...(...)" function on the passed visitor.
     * @param theVisitor the visitor.
     */
    public void acceptVisitorForSelf(GenomicEntityVisitor theVisitor) {
        try {
            theVisitor.visitHitAlignmentFeature(this);
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    class SubjectDefinitionLoader implements Runnable, java.io.Serializable {
        private HitAlignmentFeatureObserver observer;
        private boolean notifyOnly;

        public SubjectDefinitionLoader() {
            this.observer = null;
        }

        public SubjectDefinitionLoader(HitAlignmentFeatureObserver observer, 
                                       boolean notifyOnly) {
            this.observer = observer;
            this.notifyOnly = notifyOnly;
        }

        public void run() {
            try {
                if (!notifyOnly) {
                    HitAlignmentFacade loader = (HitAlignmentFacade) getDataLoader();
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
        private HitAlignmentFeatureObserver observer;

        private SubjectDefinitionNotifier(HitAlignmentFeatureObserver observer, 
                                          Collection subjectDefinitions) {
            this.observer = observer;
            this.subjectDefinitions = subjectDefinitions;
        }

        public void run() {
            observer.noteSubjectDefsLoaded(HitAlignmentFeature.this, 
                                           subjectDefinitions);
        }
    }

    class SubjectSequenceLoader implements Runnable, Serializable {
        private HitAlignmentFeatureObserver observer;
        private boolean notifyOnly;

        public SubjectSequenceLoader() {
            this.observer = null;
        }

        public SubjectSequenceLoader(HitAlignmentFeatureObserver observer, 
                                     boolean notifyOnly) {
            this.observer = observer;
            this.notifyOnly = notifyOnly;
        }

        public void run() {
            try {
                if (!notifyOnly) {
                    HitAlignmentFacade loader = (HitAlignmentFacade) getDataLoader();
                    Sequence loaderSeq = loader.getSubjectSequence(getOid(), 
                                                                   getEntityType());
                    subjectSequence = loaderSeq;
                }

                if (observer != null) {
                    getNotificationQueue()
                        .addQueue(new SubjectSequenceNotifier(observer, 
                                                              subjectSequence));
                }
            } catch (Exception ex) {
                handleException(ex);
            }
        }
    }

    private class SubjectSequenceNotifier implements Runnable {
        private Sequence subjectSequence;
        private HitAlignmentFeatureObserver observer;

        private SubjectSequenceNotifier(HitAlignmentFeatureObserver observer, 
                                        Sequence subjectSequence) {
            this.observer = observer;
            this.subjectSequence = subjectSequence;
        }

        public void run() {
            observer.noteSubjectSequenceLoaded(HitAlignmentFeature.this, 
                                               subjectSequence);
        }
    }
}