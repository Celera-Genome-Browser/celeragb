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
package api.entity_model.model.annotation;

import api.entity_model.access.observer.FeatureObserver;
import api.entity_model.access.observer.FeatureObserverAdapter;
import api.entity_model.access.observer.GenomeVersionObserver;
import api.entity_model.access.observer.GenomeVersionObserverAdapter;
import api.entity_model.access.observer.ModelSynchronizationObserver;
import api.entity_model.access.observer.WorkspaceObserver;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.AlignableGenomicEntity;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.concrete_facade.xml.XmlWorkspaceFacade;
import api.stub.data.OID;
import api.stub.data.ReplacementRelationship;

import shared.util.ThreadQueue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 *
 * This class is the "scratch" workspace where modifications are made.
 * Modifications are kept in the workpspace until later promoted.
 *
 * This class also manages the ChangeTrace(s) between the
 * GenomicEntities in the Workspace (model) and those in the Promoted (model)
 *
 * Change Trace Rules;
 * A workspace entity can only participate in a single ReplacementRelationship.
 * A promoted entity can only participate in a single ReplacementRelationship.
 *
 * If a promoted entity is split into multple workspace entities, then
 * there is a single ReplacementRelationship pointing to mutiple workspace entities
 * and only one promoted entity.
 *
 * If multiple promoted entities were merged, then there is a single
 * ReplacementRelationship pointing to a single workspace entity and
 * multiple promoted entities.
 *
 * @author   Jay T. Schira
 * @version $Id$
 */
public class Workspace extends GenomeVersionObserverAdapter
    implements GenomeVersionObserver, ModelSynchronizationObserver {
    private static final boolean DEBUG_CLASS = false;

    /**
     * A handle to the shared notifciation queue of the model.
     */
    private static ThreadQueue notificationQueue = ModelMgr.getModelMgr()
                                                           .getNotificationQueue();
    private static final int NOTE_DIRTY_STATE_CHANGED = 0;
    private static final int NOTE_CHANGE_TRACE_CHANGED = 1;
    private int counter = 0;

    /**
     * The baseGenomeVersion is the the GenomeVersion that this Workspace is a modification of.
     *
     * @associates <{GenomeVersion}>
     * @supplierCardinality 1
     * @label base GenomeVersion
     */
    private GenomeVersion baseGenomeVersion;

    /**
     * We need to track if the workspace was modified since last saved.
     */
    private boolean isDirty = false;

    /**
     * The collection of WorkspaceObservers observing this workspace.
     */
    private transient List swingEventThreadObservers;
    private transient List anyThreadObservers;

    /**
     * Changes to the promoted GV is a collection of ChangeTrace(s)...
     * These changes represent the changes made in the Workspace from when the
     * GenomicEntity(s) were copied into the workspace from the promoted GenomeVersion.
     * These ChangeTraces capture additions to, deletions from, and modifications
     * of promoted GV entities.
     *
     * @associates <{ChangeTrace}>
     * @supplierCardinality 0..*
     * @label change traces
     */
    private ArrayList allChangesToPromotedGV = new ArrayList();

    /**
     * Hash from OID to the ChangeTrace(s)
     */
    private HashMap oidToChangeTrace = new HashMap();

    /**
     * Set of OIDs that have been deleted during this session.
     * This needs to be tracked, because we only have a partial load of the XML
     * Workspace file, and we need to distinguish between;
     * 1. a GenomicEntity that was loaded then deleted and should not be writen
     * out on the next save.
     * 2. a GenomicEntity that was never loaded and should be written out on the next save.
     *
     * This is different from the ChangeTraces, because it represents a deletion
     * from the previous Workspace model archive NOT a deletion from the promoted
     * GenomeVersion.
     *
     * @associates <{OID}>
     * @supplierCardinality 0..*
     * @label deleted OIDs
     */
    private HashSet oidsDeletedThisSession = new HashSet();

    /**
     * A FeatureObserver used to observe the Workspace CuratedFeature(s).
     */
    private FeatureObserver myFeatureObserver;

    /**
     * A reference to the XmlWorkspaceFacade if there is one.
     */
    private XmlWorkspaceFacade xmlWorkspaceFacade;
    private ArrayList haveLoadedObsoletedRootFeaturesOnAxes = new ArrayList();

    //===========Constructor support===============================================

    /**
     * Construct a workspace with a GenomeVersion...
     */
    public Workspace(GenomeVersion genomeVersion) {
        this(genomeVersion, null);
    }

    /**
     * Construct a workspace with a GenomeVersion...
     */
    public Workspace(GenomeVersion genomeVersion, 
                     XmlWorkspaceFacade anXmlWorkspaceFacade) {
        if (DEBUG_CLASS) {
            System.out.println("New Workspace(GenomeVersion = " + 
                               genomeVersion.getDescription() + 
                               ", XmlWorkspaceFacade = " + 
                               anXmlWorkspaceFacade);
        }

        this.baseGenomeVersion = genomeVersion;
        this.xmlWorkspaceFacade = anXmlWorkspaceFacade;


        // Add myself as a genomeVersion observer...
        // we don't need to get up to date on existing because we've been instanciated
        // before notification of any workspace items has been sent out.
        genomeVersion.addGenomeVersionObserver(this, false);
        myFeatureObserver = new MyFeatureObserver();
    }

    /**
     * Deactivate this workspace.
     * This is primarily an issue in the promotion code, where it will load and use a
     * workspace, then dispose of the workspace, and load another... all with the
     * same GenomeVersion.
     */
    public void unload() {
        baseGenomeVersion.removeGenomeVersionObserver(this);
    }

    //=========== Load from Workspace Facade =========================================
    private boolean haveLoadedObsoletedRootFeaturesOnAxis(Axis axis) {
        if (axis == null) {
            return false;
        }

        return haveLoadedObsoletedRootFeaturesOnAxes.contains(axis.getOid());
    }

    private void noteHaveLoadedObsoletedRootFeaturesOnAxis(Axis axis) {
        if (axis == null) {
            return;
        }

        haveLoadedObsoletedRootFeaturesOnAxes.add(axis.getOid());
    }

    /**
     * Load Obosleted Root Features from the XmlWorkspaceFacade...
     */
    private void loadObsoletedRootFeaturesFromFacade(Axis axis) {
        if (this.haveLoadedObsoletedRootFeaturesOnAxis(axis)) {
            return;
        }

        // Check the arguments...
        if (xmlWorkspaceFacade == null) {
            if (DEBUG_CLASS) {
                System.out.println(
                        "loadObsoletedRootFeaturesFromFacade Bad args..." + 
                        " xmlWorkspaceFacade = " + xmlWorkspaceFacade);
            }

            return;
        }


        // Note that we've loaded obsoleted root features...
        this.noteHaveLoadedObsoletedRootFeaturesOnAxis(axis);

        // First, get all the root "obsoleted"
        Object nextObject;
        CuratedFeature anObsoletedFeature;

        // System.out.println("Loading obsoleted ROOT features from Facade for axis:" + axis.getOid());
        Set rootObsoletedFeatures = xmlWorkspaceFacade.getObsoletedRootFeatures(
                                            axis.getOid());

        // System.out.println("Loaded " + rootObsoletedFeatures.size() + " oboleted root features...");
        for (Iterator itr = rootObsoletedFeatures.iterator(); itr.hasNext();) {
            nextObject = itr.next();

            if (nextObject instanceof CuratedFeature) {
                anObsoletedFeature = (CuratedFeature) nextObject;


                // Load the replacement relationship for this feature...
                this.loadObsoletedReplacementInfoFromFacade(anObsoletedFeature, 
                                                            axis);


                // Now see if there are any obsoleted sub-features...
                this.loadObsoletedSubStructureFromFacade(anObsoletedFeature, 
                                                         axis);
            } else {
                if (DEBUG_CLASS) {
                    System.out.println("Workspace.loadObsoletedRootFeatures - " + 
                                       "XmlWorkspaceFacade.getObsoletedRootFeatures(); returned a non CuratedFeature! " + 
                                       "Set element is of type: " + 
                                       nextObject.getClass().getName());
                }
            }
        }
    }

    /**
     * Recursively load the "obsoleted" structure from the XmlWorkspaceFacade.  Anything that
     * is in turn "obsoleted" will recursively load.
     */
    private void loadObsoletedSubStructureFromFacade(CuratedFeature aCuratedFeature, 
                                                     Axis axis) {
        // Check the arguments...
        if ((xmlWorkspaceFacade == null) || (aCuratedFeature == null) || 
                (axis == null)) {
            if (DEBUG_CLASS) {
                System.out.println(
                        "loadObsoletedSubStructureFromFacade Bad args..." + 
                        " xmlWorkspaceFacade = " + xmlWorkspaceFacade + 
                        " aCuratedFeature = " + aCuratedFeature + " axis = " + 
                        axis);
            }

            return;
        }


        // First, make sure that we've loaded the root "obsoleted" features...
        this.loadObsoletedRootFeaturesFromFacade(axis);

        // If this CuratedFeature is NOT scratch... then we don't need to go any further...
        if (!aCuratedFeature.isScratch()) {
            return;
        }

        // Now we can preceed...
        Object nextObject;
        CuratedFeature anObsoletedFeature;

        // System.out.println("Loading obsoleted sub-structure of feature " + aCuratedFeature.getOid()
        //                     + "  for axis:" + axis.getOid());
        Set obsoletedSubstructure = xmlWorkspaceFacade.getObsoletedSubStructureOfFeature(
                                            axis.getOid(), 
                                            aCuratedFeature.getOid());

        // System.out.println("Loaded " + obsoletedSubstructure.size() + " oboleted substructure...");
        for (Iterator itr = obsoletedSubstructure.iterator(); itr.hasNext();) {
            nextObject = itr.next();

            if (nextObject instanceof CuratedFeature) {
                anObsoletedFeature = (CuratedFeature) nextObject;
                this.loadObsoletedReplacementInfoFromFacade(anObsoletedFeature, 
                                                            axis);

                ChangeTrace theChangeTrace = this.getChangeForWorkspaceOid(
                                                     anObsoletedFeature.getOid());
                theChangeTrace.setWorkspaceSuperFeatureOid(
                        aCuratedFeature.getOid());


                // Now see if there are any obsoleted sub-features...
                this.loadObsoletedSubStructureFromFacade(anObsoletedFeature, 
                                                         axis);
            } else {
                if (DEBUG_CLASS) {
                    System.out.println(
                            "Workspace.loadObsoletedSubStructureFromFacade - " + 
                            "XmlWorkspaceFacade.getObsoletedSubStructureOfFeature(); returned a non CuratedFeature!" + 
                            "Set element is of type: " + 
                            nextObject.getClass().getName());
                }
            }
        }
    }

    /**
     * Load the ReplacementRelationship for an "obsoleted" feature from XmlWorkspaceFacade.
     */
    private void loadObsoletedReplacementInfoFromFacade(CuratedFeature anObsoletedFeature, 
                                                        Axis axis) {
        // Check the arguments...
        if ((xmlWorkspaceFacade == null) || (anObsoletedFeature == null)) {
            if (DEBUG_CLASS) {
                System.out.println(
                        "loadObsoletedReplacementInfoFromFacade Bad args..." + 
                        " xmlWorkspaceFacade = " + xmlWorkspaceFacade + 
                        " anObsoletedFeature = " + anObsoletedFeature + 
                        " axis = " + axis);
            }

            return;
        }

        // System.out.println("Loading replacement info for obsoleted feature " + anObsoletedFeature.getOid());
        // See if we already have a ChangeTrace for this one...
        ChangeTrace theChangeTrace = this.getChangeForWorkspaceOid(
                                             anObsoletedFeature.getOid());

        // If we don't have one... we should make one...
        if (theChangeTrace == null) {
            if (DEBUG_CLASS) {
                System.out.println("We don't have a pre-existing...");
            }

            // See if the CuratedFeature can get one from the facade...
            ReplacementRelationship loadedReplacementRel = 
                    anObsoletedFeature.getReplacementRelationshipFromFacade();

            if (loadedReplacementRel != null) {
                if (DEBUG_CLASS) {
                    System.out.println(
                            "Got replacement from curatedFeature of type: " + 
                            loadedReplacementRel.getReplacementType());
                }


                // Create the ChangeTrace...
                theChangeTrace = new ChangeTrace(this, anObsoletedFeature, 
                                                 loadedReplacementRel);


                // Need to add it to the look ups...
                this.addNewChangeTrace(theChangeTrace);


                // Save off the Feature in the ChangeTrace...
                theChangeTrace.obsolete(anObsoletedFeature);

                // Save off the Alignment into the ChangeTrace...
                Alignment obsAlign = xmlWorkspaceFacade.getObsoletedAlignmentForWorkspaceOid(
                                             axis.getOid(), 
                                             anObsoletedFeature.getOid());

                if (obsAlign != null) {
                    obsAlign.setAxisIfNull(axis);
                }

                theChangeTrace.setObsoletedAlignment(obsAlign);


                // Post the change, so the view has a change to affect the color of replaced Promoted features...
                theChangeTrace.postChangeTraceChange();
            }
        }
    }

    //=========== Undo / Redo support==================================================

    /**
     * Get a tokenized form of the workspace...
     */
    public WorkspaceToken getUndoToken() {
        WorkspaceToken workspaceToken = new WorkspaceToken();


        // Back up the isDirty...
        workspaceToken.isDirty = this.isDirty;


        // Back up the changeTraces...
        // Need to set the workspace of each of these when read in.
        workspaceToken.allChangesToPromotedGVInBytes = this.byteArrayFromSerializedObject(
                                                               allChangesToPromotedGV);


        // Back up the oidToChangeTrace map...
        // This will be re-created.
        // public HashMap oidToChangeTrace;  // snap shot of.
        // Back up the oidsDeletedThisSession...
        workspaceToken.oidsDeletedThisSessionInBytes = this.byteArrayFromSerializedObject(
                                                               oidsDeletedThisSession);

        // Return the token.
        return workspaceToken;
    }

    /**
     * Get a byte array for a root object.
     */
    private byte[] byteArrayFromSerializedObject(Object rootObject) {
        if (DEBUG_CLASS) {
            System.out.println(
                    "ENTER >> Workspace.byteArrayFromSerializedObject(Object);");
        }

        byte[] byteArray = null;

        try {
            // Set up a ByteArrayOutputStream with an initial size...
            ByteArrayOutputStream baOutStream = new ByteArrayOutputStream(100);

            // Set up an ObjectOutputStream...
            ObjectOutputStream objOutStream = new ObjectOutputStream(
                                                      baOutStream);


            // Write out the source root entity to the ObjectOutputStream...
            objOutStream.writeObject(rootObject);


            // Get the byte buffer...
            byteArray = baOutStream.toByteArray();


            // Close the
            objOutStream.close();

            // Return byte array
        } catch (Exception ex) {
            ModelMgr.getModelMgr().handleException(ex);
        }

        if (DEBUG_CLASS) {
            if (byteArray != null) {
                System.out.println("RETURNING byte[] of length = " + 
                                   byteArray.length);
            }

            System.out.println(
                    "EXIT << Workspace.byteArrayFromSerializedObject(Object);");
        }

        return byteArray;
    }

    private Object objectFromDeserializedByteArray(byte[] byteArray) {
        if (DEBUG_CLASS) {
            System.out.println(
                    "EXIT << Workspace.objectFromDeserializedByteArray(byte[]);");
        }

        Object rootObject = null;

        try {
            // Get the ByteArrayInputStream...
            ByteArrayInputStream baInputStream = new ByteArrayInputStream(
                                                         byteArray);

            // Get the ObjectInputStream...
            ObjectInputStream objInputStream = new ObjectInputStream(
                                                       baInputStream);


            // Read back the root Entity
            rootObject = objInputStream.readObject();


            // Close the object input stream...
            objInputStream.close();
        } catch (Exception ex) {
            ModelMgr.getModelMgr().handleException(ex);
        }

        if (DEBUG_CLASS) {
            if (rootObject != null) {
                System.out.println("RETURNING root object = " + 
                                   rootObject.getClass().getName());
            } else {
                System.out.println("RETURNING root entity = NULL");
            }

            System.out.println(
                    "EXIT << Workspace.objectFromDeserializedByteArray(byte[]);");
        }

        return rootObject;
    }

    /**
     * Roll the Workspace back / forward to support undo / redo.
     * Must affect the proper notifications.
     * Used for Undo / Redo.
     * @todo: applyUndoToken() figure out what notification should be triggered.
     */
    public void applyUndoToken(WorkspaceToken workspaceToken) {
        // Undo to Back up of isDirty...
        this.isDirty = workspaceToken.isDirty;


        // Undo to Back up of changeTraces...
        // Need to set the workspace of each of these when read in.
        // This will be re-create the oidToChangeTrace HashMap
        this.allChangesToPromotedGV = (ArrayList) this.objectFromDeserializedByteArray(
                                              workspaceToken.allChangesToPromotedGVInBytes);

        ChangeTrace aChangeTrace;
        this.oidToChangeTrace.clear();

        ChangeTrace[] changeTraces = (ChangeTrace[]) allChangesToPromotedGV.toArray(
                                               new ChangeTrace[0]);

        for (int i = 0; i < changeTraces.length; i++) {
            aChangeTrace = changeTraces[i];

            if (aChangeTrace != null) {
                aChangeTrace.setWorkspace(this);
                aChangeTrace.reconstituteObsoletedAlignment(baseGenomeVersion);
                this.addOidToChangeTraceRefs(aChangeTrace.getWorkspaceOids(), 
                                             aChangeTrace);
                this.addOidToChangeTraceRefs(aChangeTrace.getPromotedOids(), 
                                             aChangeTrace);
            }
        }


        // Undo to Back up of oidsDeletedThisSession...
        this.oidsDeletedThisSession = (HashSet) this.objectFromDeserializedByteArray(
                                              workspaceToken.oidsDeletedThisSessionInBytes);


        // As a last action... trigger notifications...
        this.postWorkspaceDirtyStateChanged(this.isDirty);

        for (int i = 0; i < changeTraces.length; i++) {
            aChangeTrace = changeTraces[i];

            if (aChangeTrace != null) {
                aChangeTrace.setChangeType(aChangeTrace.getChangeType());
            }
        }
    }

    //=========== XXX  support==================================================

    /**
     * Get the GenomicEntity for an OID
     * Package visible...
     */
    Feature getFeatureForOid(OID anOid) {
        GenomicEntity genomicEntity = baseGenomeVersion.getLoadedGenomicEntityForOid(
                                              anOid);

        if (genomicEntity instanceof Feature) {
            return (Feature) genomicEntity;
        }

        return null;
    }

    public List getWorkspaceCuratedTranscripts() {
        ArrayList a = new ArrayList();
        Set woids = getWorkspaceOids();

        for (Iterator iter = woids.iterator(); iter.hasNext();) {
            OID o = (OID) iter.next();
            Feature f = getFeatureForOid(o);

            if (f instanceof CuratedTranscript) {
                a.add(f);
            }
        }

        return a;
    }

    //=========== GenomeVersionObserver support==================================================

    /**
     * Get notified that a GenomicEntity was added...
     * This method should ONLY be called from the GenomeVersion.
     * This method will be invoked DIRECTLY, and not be queued like the other observers.
     * This is so we can get immediately notified, before the Views, and then be
     * messaged directly by the commands.
     */
    public void noteEntityAddedToGenomeVersion(GenomicEntity entity) {
        if (DEBUG_CLASS) {
            System.out.println((counter++) + 
                               " - Workspace.noteEntityAddedToGenomeVersion(entity=[" + 
                               entity.getClass().getName() + "." + 
                               entity.getDisplayName() + "." + 
                               entity.getOid() + "]); was called.");
        }

        // If we just got an axis aligned... try to get obsoleted features....
        // Re-construct obsoleted information from the xmlWorkspaceFacade... if we have one.
        if ((xmlWorkspaceFacade != null) && (entity instanceof GenomicAxis)) {
            // System.out.println("Re-construct obsoleted information from the xmlWorkspaceFacade...");
            // this.loadObsoletedRootFeaturesFromFacade((GenomicAxis)entity);
        }

        // The workspace only cares about scratch CuratedFeature(s) that are added...
        if ((entity == null) || (!entity.isWorkspace()) || 
                (!(entity instanceof CuratedFeature))) {
            return;
        }

        // Probably also need to check if this entity is part of THIS workspace.
        if (entity.getGenomeVersion() != this.baseGenomeVersion) {
            return;
        }

        // Cast it down to a curated feature...
        CuratedFeature newCuratedFeature = (CuratedFeature) entity;

        // See if we already have a ChangeTrace for this one...
        ChangeTrace theChangeTrace = this.getChangeForWorkspaceOid(
                                             newCuratedFeature.getOid());

        if (DEBUG_CLASS) {
            System.out.println((counter++) + " - Curated Feature has " + 
                               newCuratedFeature.getSubStructure().size() + 
                               " substructure.");
        }

        /*
        // If the new feature is a CuratedCodon, let's try to merge this back with the
        // appropriate obsoleted codons
        if ((theChangeTrace == null) && (newCuratedFeature instanceof CuratedCodon)) {
          CuratedCodon newCodon = (CuratedCodon)newCuratedFeature;
          boolean newCodonIsStart = newCodon.isStartCodon();
          CuratedTranscript newCodonsTrans = newCodon.getHostTranscript();
          // This is the find all CTs with super OID approach...
          if (newCodonsTrans != null) {
            ChangeTrace transCT = this.getChangeForOid(newCodonsTrans.getOid());
            // Only care if the transcript is not "new"...
            if ((transCT != null) && (!transCT.isChangeType(ReplacementRelationship.NEW))) {
              Set promotedTransOidSet = transCT.getPromotedOids();
              Set promotedCodonOidSet = new HashSet();
              for (Iterator promoTransOidItr = promotedTransOidSet.iterator(); promoTransOidItr.hasNext(); ) {
                OID anOid = (OID)promoTransOidItr.next();
                CuratedTranscript promotedTrans = (CuratedTranscript)this.getFeatureForOid(anOid);
                CuratedCodon promotedCodon = null;
                if (newCodonIsStart) promotedCodon = promotedTrans.getStartCodon();
                else promotedCodon = promotedTrans.getStopCodon();
                if (promotedCodon != null) promotedCodonOidSet.add(promotedCodon.getOid());
              }
              // If the promoted Codon Oid Set is not empty we need to set this up as
              // something other than a new CT...
              if (!promotedCodonOidSet.isEmpty()) {
              }
            }
            // If we found aChangeTrace... convert it...
            if (theChangeTrace != null) {
              // Get the old Workspace set...
              Set oldWorkspaceOidSet = theChangeTrace.getWorkspaceOids();
              OID oldOidArray[] = (OID[])oldWorkspaceOidSet.toArray(new OID[0]);
              theChangeTrace.removeWorkspaceOids(oldOidArray);
              // Remove the traces...
              // Add the new OID...
              // Add the traces...
              // Change the type to "modified".
            }
          }
        }
        */

        // If we don't have one... we should make one...
        if (theChangeTrace == null) {
            if (DEBUG_CLASS) {
                System.out.println("We don't have a pre-existing...");
            }

            // See if the CuratedFeature can get one from the facade...
            ReplacementRelationship loadedReplacementRel = 
                    newCuratedFeature.getReplacementRelationshipFromFacade();

            if (loadedReplacementRel != null) {
                if (DEBUG_CLASS) {
                    System.out.println(
                            "Got replacement from curatedFeature of type: " + 
                            loadedReplacementRel.getReplacementType());
                }

                theChangeTrace = new ChangeTrace(this, newCuratedFeature, 
                                                 loadedReplacementRel);


                // Need to add it to the look ups...\
                this.addNewChangeTrace(theChangeTrace);

                // Special case of reading back an "obsoleted"
                // <JTS: Need to decide how to properly handle retrieving "obsoleted" from a GBW file...>

                /*
                if (theChangeTrace.isChangeType(ReplacementRelationship.OBSOLETE)) {
                  System.out.println("Workspace intercepting an OBSOLETED feature!");
                  // Need the mutators...
                  CuratedFeature.CuratedFeatureMutator newCurFeatMutator = newCuratedFeature.getCuratedFeatureMutator();
                  CuratedFeature superFeature = (CuratedFeature)newCuratedFeature.getSuperFeature();
                  CuratedFeature.CuratedFeatureMutator superFeatureMutator = null;
                  if (superFeature != null) superFeatureMutator = superFeature.getCuratedFeatureMutator();
                  // Save off a reference to the feature in the Change Trace...
                  theChangeTrace.obsolete(newCuratedFeature);
                  // Should un-align this newCuratedFeature,
                  Alignment onlyAlignment = newCuratedFeature.getOnlyAlignmentToOnlyAxis();
                  theChangeTrace.setObsoletedAlignment(onlyAlignment);
                  newCurFeatMutator.removeAlignmentToAxis(onlyAlignment);
                  // Should make sure the newCuratedFeature is not a "sub-feature" of it's super.
                  if (superFeatureMutator != null) {
                    if (newCuratedFeature instanceof CuratedCodon) {
                      ((CuratedTranscript.CuratedTranscriptMutator)superFeatureMutator).removeStartOrStopCodon((CuratedCodon)newCuratedFeature);
                    }
                    else {
                      superFeatureMutator.removeSubFeature(newCuratedFeature);
                    }
                  }
                }
                */
            } else {
                if (DEBUG_CLASS) {
                    System.out.println("Have to create our own...");
                }

                theChangeTrace = new ChangeTrace(this, newCuratedFeature, null, 
                                                 ReplacementRelationship.NEW);


                // Need to add it to the look ups...\
                this.addNewChangeTrace(theChangeTrace);
            }
        }
        // Actually already had a ChangeTrace.
        // ... probably the case of removeAlignment, addAlignment...
        // @todo: HACK... need to encapsulate Merge command inside CTMutator method.
        else {
            //don't change the state if theChangeTrace is NEW
            if (!theChangeTrace.isChangeType(ReplacementRelationship.NEW)) {
                theChangeTrace.setChangeType(ReplacementRelationship.MODIFIED);
            }
        }

        // We should set up the super feature OID reference if it's available...
        // We also need to look both up and down, because we have no guarantee of
        // the order we will be notified... super features first or last.
        // The super feature OID is used for XML Write out...
        if (entity instanceof CuratedFeature) {
            CuratedFeature feature = (CuratedFeature) entity;

            // Look for this feature's super feature...
            CuratedFeature superFeature = null;

            if (feature instanceof CuratedCodon) {
                superFeature = ((CuratedCodon) feature).getHostTranscript();
            } else {
                superFeature = (CuratedFeature) feature.getSuperFeature();
            }

            if (theChangeTrace != null) {
                if (superFeature != null) {
                    theChangeTrace.setWorkspaceSuperFeatureOid(
                            superFeature.getOid());
                } else {
                    theChangeTrace.setWorkspaceSuperFeatureOid(null);
                }
            }

            // Look for this feature's sub-structure (includes codons etc.)...
            Collection subStructureColl = feature.getSubStructure();
            CuratedFeature subFeature = null;
            ChangeTrace subChangeTrace = null;

            for (Iterator itr = subStructureColl.iterator(); itr.hasNext();) {
                subFeature = (CuratedFeature) itr.next();
                subChangeTrace = this.getChangeForWorkspaceOid(
                                         subFeature.getOid());

                if (subChangeTrace != null) {
                    subChangeTrace.setWorkspaceSuperFeatureOid(feature.getOid());
                }
            }
        }

        // Now see if there are any obsoleted sub-structure from the facade...
        Alignment onlyAlign = newCuratedFeature.getOnlyAlignmentToOnlyAxis();

        if (onlyAlign != null) {
            this.loadObsoletedSubStructureFromFacade(newCuratedFeature, 
                                                     onlyAlign.getAxis());
        }


        // Now that we're tracking this feature, we need to observer it, we don't want to be
        // brought up to date on this feature.
        newCuratedFeature.addFeatureObserver(myFeatureObserver, false);

        // Debug Diagnostics...
        if (DEBUG_CLASS) {
            System.out.println(
                    "EXIT: Workspace.noteEntityAddedToGenomeVersion();");
            this.printChangeTraces();
        }
    }

    /**
     * Get notified that a GenomicEntity was removed...
     * This method should ONLY be called from the GenomeVersion.
     * This method will be invoked DIRECTLY, and not be queued like the other observers.
     * This is so we can get immediately notified, before the Views, and then be
     * messaged directly by the commands.
     */
    public void noteEntityRemovedFromGenomeVersion(GenomicEntity entity) {
        if (DEBUG_CLASS) {
            String entityStr;

            if (entity == null) {
                entityStr = "NULL";
            } else {
                entityStr = entity.getClass().getName() + "." + 
                            entity.getDisplayName() + "." + entity.getOid();
            }

            System.out.println(
                    "Workspace.noteEntityRemovedFromGenomeVersion(entity=[" + 
                    entityStr + "]); was called.");
        }

        // The workspace only cares about scratch CuratedFeature(s) that are added...
        if ((entity == null) || (!entity.isWorkspace()) || 
                (!(entity instanceof CuratedFeature))) {
            if (DEBUG_CLASS) {
                System.out.println("Entity[" + entity.getOid() + 
                                   "] ignored.");
            }

            return;
        }

        CuratedFeature oldCuratedFeature = (CuratedFeature) entity;


        // Probably also need to check if this entity is part of THIS workspace.
        // The removed entity's getGenomeVersion will return null.
        // if (entity.getGenomeVersion() != this.baseGenomeVersion) return;
        // Interpret this as an "obsolete" action.
        // Delete from workspace actions will be followed up by an explicit call
        // to Workspace.deleteOidFromWorkspace(OID);
        Workspace.this.transitionObsoleted(oldCuratedFeature);


        // Now that we're tracking this feature, we need to observer it, we don't want to be
        // brought up to date on this feature.
        oldCuratedFeature.removeFeatureObserver(myFeatureObserver);

        // Debug Diagnostics...
        if (DEBUG_CLASS) {
            System.out.println(
                    "EXIT: Workspace.noteEntityRemovedFromGenomeVersion(entity=" + 
                    entity.getOid() + ");");
            this.printChangeTraces();
        }
    }

    //=========== Explicite methods to be called by commands =====================
    //====== These should only be called from the CuratedTranscriptMutator  ======

    /**
     * Add the item to the workspace and track it for future modifications...
     */
    void addToWorkspaceAndTrack(OID workspaceOid) {
        // Debug Diagnostics...
        if (DEBUG_CLASS) {
            System.out.println(
                    "ENTER: Workspace.addToWorkspaceAndTrack(workspaceOID=" + 
                    workspaceOid + ");");
            this.printChangeTraces();
        }

        // Check args...
        if (workspaceOid == null) {
            if (DEBUG_CLASS) {
                System.out.println("Bad Args!  EXIT");
            }

            return;
        }

        // See if we already have a change trace for the workspace OID...
        ChangeTrace theChangeTrace = this.getChangeForWorkspaceOid(workspaceOid);

        // If we have one already, add the promoted Oid...
        if (theChangeTrace == null) {
            newWorkspaceToPromotedChangeTrace(workspaceOid, null);


            // New item added to the workspace, mark the workspace as dirty...
            this.setWasDirtied();
        }

        // Debug Diagnostics...
        if (DEBUG_CLASS) {
            System.out.println(
                    "EXIT: Workspace.addToWorkspaceAndTrack(workspaceOID=" + 
                    workspaceOid + ");");
            this.printChangeTraces();
        }
    }

    /**
     * Set up the initial
     * The standard usage of this would be after a promoted feature was dragged into
     * the workspace.
     * The change type will be "unmodified".
     * This should be "package" visible and only called from the CuratedFeatureMutator.
     *
     * @todo: implement "initializeChangeTrace(OID workspaceOid, OID promotedOid, byte type);"
     */
    void setupReplacementOfPromoted(OID workspaceOid, OID promotedOid) {
        // Debug Diagnostics...
        if (DEBUG_CLASS) {
            System.out.println(
                    "ENTER: Workspace.setupReplacementOfPromoted(workspaceOID=" + 
                    workspaceOid + ", promotedOID=" + promotedOid + ");");

            // this.printChangeTraces();
        }

        // Check args...
        if ((workspaceOid == null) || (promotedOid == null)) {
            if (DEBUG_CLASS) {
                System.out.println("Bad Args!  EXIT");
            }

            return;
        }

        // See if we already have a change trace for the workspace OID...
        ChangeTrace theChangeTrace = this.getChangeForWorkspaceOid(workspaceOid);

        // If we have one already, add the promoted Oid...
        if (theChangeTrace != null) {
            theChangeTrace.addPromotedOid(promotedOid);
            theChangeTrace.setChangeType(ReplacementRelationship.UNMODIFIED);
        }
        // Otherwise create one...
        else {
            theChangeTrace = this.newWorkspaceToPromotedChangeTrace(
                                     workspaceOid, promotedOid);
            this.addNewChangeTrace(theChangeTrace);
        }

        // Need to add this to the changeTraceRefs...
        HashSet tempSet = new HashSet();
        tempSet.add(promotedOid);
        this.addOidToChangeTraceRefs(tempSet, theChangeTrace);


        // Item changed in the workspace, mark the workspace as dirty...
        this.setWasDirtied();

        // Debug Diagnostics...
        if (DEBUG_CLASS) {
            // System.out.println("EXIT: Workspace.setupReplacementOfPromoted(workspaceOID="
            //                     + workspaceOid + ", promotedOID=" + promotedOid + ");");
            // this.printChangeTraces();
        }
    }

    /**
     * Set up the replacement relationships for a split transcript.
     * The standard usage of this would be after a promoted feature was dragged into
     * the workspace.
     * The change type will be "unmodified".
     * This should be "package" visible and only called from the CuratedFeatureMutator.
     */
    void setupSplitOfPromoted(CuratedTranscript preSplitWorkspaceTranscript, 
                              CuratedTranscript postSplitWorkspaceTranscript1, 
                              CuratedTranscript postSplitWorkspaceTranscript2) {
        // Check the args...
        if ((preSplitWorkspaceTranscript == null) || 
                (postSplitWorkspaceTranscript1 == null) || 
                (postSplitWorkspaceTranscript2 == null)) {
            return;
        }

        // Grab the oids...
        OID preSplitOid = preSplitWorkspaceTranscript.getOid();
        OID postSplitOid1 = postSplitWorkspaceTranscript1.getOid();
        OID postSplitOid2 = postSplitWorkspaceTranscript2.getOid();

        // Debug Diagnostics...
        if (DEBUG_CLASS) {
            System.out.println(
                    "ENTER: Workspace.setupSplitOfPromoted(preSplitWorkspaceTranscriptOid = " + 
                    preSplitOid + ", postSplitWorkspaceTransOid1 = " + 
                    postSplitOid1 + ", postSplitWorkspaceTransOid2 = " + 
                    postSplitOid2 + ");");
            this.printWorkspaceStatistics();
        }

        ChangeTrace preSplitChangeTrace;
        preSplitChangeTrace = this.getChangeForOid(preSplitOid);

        // If the Pre-Split ChangeTrace is New...
        // Keep the PostSplit ChangeTrace(s), and remove the PreSplit ChangeTrace...
        if (preSplitChangeTrace.isChangeType(ReplacementRelationship.NEW)) {
            this.removeChangeTrace(preSplitChangeTrace);
        }
        // If the Pre-Split ChangeTrace is not NEW...
        // Re-use the Pre-Split ChangeTrace and remove the PostSplit ChangeTraces...
        else {
            // Should remove any reference to the postSplitOids if they already got here from alignments...
            ChangeTrace theChangeTrace = this.getChangeForOid(postSplitOid1);
            this.removeChangeTrace(theChangeTrace);
            theChangeTrace = this.getChangeForOid(postSplitOid2);
            this.removeChangeTrace(theChangeTrace);


            // Need to replace any reference to the preSplitWorkspaceTranscriptOid, with.
            preSplitChangeTrace = this.getChangeForOid(preSplitOid);


            // Add the preso
            preSplitChangeTrace.removeWorkspaceOid(preSplitOid);
            preSplitChangeTrace.addWorkspaceOid(postSplitOid1);
            preSplitChangeTrace.addWorkspaceOid(postSplitOid2);
            this.oidToChangeTrace.put(postSplitOid1, preSplitChangeTrace);
            this.oidToChangeTrace.put(postSplitOid2, preSplitChangeTrace);
            preSplitChangeTrace.setChangeType(ReplacementRelationship.SPLIT);


            // Remove the preSplitWorkspaceTranscriptOID
            this.oidToChangeTrace.remove(preSplitOid);

            // Need to make sure the exon's ChangeTrace tracks it's parent OID properly.
        }

        // Move any obsoleted features that used to be under the old CT's...
        Set subChangeTraces = this.getChangeTracesWithSuperOid(preSplitOid);

        for (Iterator subCTItr = subChangeTraces.iterator();
             subCTItr.hasNext();) {
            ChangeTrace aSubChangeTrace = (ChangeTrace) subCTItr.next();


            // Check which range to decide which to put it on?
            // For now just put it on the first of the post split transcripts...
            aSubChangeTrace.setWorkspaceSuperFeatureOid(postSplitOid1);
        }

        // Debug Diagnostics...
        if (DEBUG_CLASS) {
            System.out.println(
                    "EXIT: Workspace.setupSplitOfPromoted(preSplitWorkspaceTranscriptOid = " + 
                    preSplitOid + ", postSplitWorkspaceTransOid1 = " + 
                    postSplitOid1 + ", postSplitWorkspaceTransOid2 = " + 
                    postSplitOid2 + ");");
            this.printWorkspaceStatistics();
        }
    }

    /**
     * Set up the replacement relationships for a merged transcript.
     */
    void setupMergeOfPromoted(CuratedTranscript preMergeWorkspaceTranscript1, 
                              CuratedTranscript preMergeWorkspaceTranscript2, 
                              CuratedTranscript postMergeWorkspaceTranscript) {
        boolean debugMethod = false;

        // Check the args...
        if ((preMergeWorkspaceTranscript1 == null) || 
                (preMergeWorkspaceTranscript2 == null) || 
                (postMergeWorkspaceTranscript == null)) {
            if (DEBUG_CLASS) {
                System.out.println(
                        "Workspace.setupMergeOfPromoted(); called with bad args.");
            }

            return;
        }

        // Grab the oids...
        OID preMergeOid1 = preMergeWorkspaceTranscript1.getOid();
        OID preMergeOid2 = preMergeWorkspaceTranscript2.getOid();
        OID postMergeOid = postMergeWorkspaceTranscript.getOid();

        // Debug Diagnostics...
        if (DEBUG_CLASS || debugMethod) {
            System.out.println(
                    "ENTER: Workspace.setupMergeOfPromoted(preMergeWorkspaceTranscript1 = " + 
                    preMergeOid1 + ", preMergeWorkspaceTranscript2 = " + 
                    preMergeOid2 + ", postMergeWorkspaceTranscript = " + 
                    postMergeOid + ");");

            this.checkPointRepRel("ENTER setupMergeOfPromoted();", preMergeOid1, 
                                  true);
            this.checkPointRepRel("ENTER setupMergeOfPromoted();", preMergeOid2, 
                                  true);
            this.checkPointRepRel("ENTER setupMergeOfPromoted();", postMergeOid, 
                                  true);
        }

        // Need to replace any reference to the preSplitWorkspaceTranscriptOid, with.
        ChangeTrace preChangeTrace1 = this.getChangeForOid(preMergeOid1);
        ChangeTrace preChangeTrace2 = this.getChangeForOid(preMergeOid2);
        ChangeTrace postChangeTrace = this.getChangeForOid(postMergeOid);


        // Keep postChangeTrace, remove the preChangeTrace1 and preChangeTrace2
        this.removeChangeTrace(preChangeTrace1);
        this.removeChangeTrace(preChangeTrace2);

        postChangeTrace.removeWorkspaceOid(preMergeOid1);
        postChangeTrace.removeWorkspaceOid(preMergeOid2);


        // Remove the preMergeOid1 and preMergeOid2 references
        this.oidToChangeTrace.remove(preMergeOid1);
        this.oidToChangeTrace.remove(preMergeOid2);


        // Make sure the pre-merge Transcript Oids are listed as deleted this session.
        this.oidsDeletedThisSession.add(preMergeOid1);
        this.oidsDeletedThisSession.add(preMergeOid2);

        postChangeTrace.addWorkspaceOid(postMergeOid);
        postChangeTrace.addAllPromotedOids(preChangeTrace1.getPromotedOids());
        postChangeTrace.addAllPromotedOids(preChangeTrace2.getPromotedOids());
        this.addOidToChangeTraceRefs(postChangeTrace.getWorkspaceOids(), 
                                     postChangeTrace);
        this.addOidToChangeTraceRefs(postChangeTrace.getWorkspaceOids(), 
                                     postChangeTrace);

        // See if the postCT is in the...
        if (DEBUG_CLASS) {
            System.out.println("PostCT in WS storage: " + 
                               this.allChangesToPromotedGV.contains(
                                       postChangeTrace));
        }

        if (DEBUG_CLASS || debugMethod) {
            this.checkPointRepRel("AFTER ChangeTrace Merge", postMergeOid, true);
        }

        // If they were both NOT NEW the result should be MERGE...
        if (!preChangeTrace1.isChangeType(ReplacementRelationship.NEW) && 
                !preChangeTrace2.isChangeType(ReplacementRelationship.NEW)) {
            if (DEBUG_CLASS) {
                System.out.println("Both previous ChangeTypes were non-NEW");
            }

            postChangeTrace.setChangeType(ReplacementRelationship.MERGE);
        }
        // If they were both NEW the result should be NEW...
        else if (preChangeTrace1.isChangeType(ReplacementRelationship.NEW) && 
                     preChangeTrace2.isChangeType(ReplacementRelationship.NEW)) {
            if (DEBUG_CLASS) {
                System.out.println("Both previous ChangeTypes were NEW");
            }

            postChangeTrace.setChangeType(ReplacementRelationship.NEW);
        }
        // If only one was NOT NEW the result should be MODIFIED
        // been modified...
        else {
            if (DEBUG_CLASS) {
                System.out.println("One of the Change Types was non-NEW");
            }

            postChangeTrace.setChangeType(ReplacementRelationship.MODIFIED);

            if (DEBUG_CLASS) {
                System.out.println("Post ChangeTrace was set to:" + 
                                   postChangeTrace.getChangeType());
            }
        }

        // Move any obsoleted features that used to be under the old CT's...
        Set subChangeTraces = this.getChangeTracesWithSuperOid(preMergeOid1);
        subChangeTraces.addAll(this.getChangeTracesWithSuperOid(preMergeOid2));

        for (Iterator subCTItr = subChangeTraces.iterator();
             subCTItr.hasNext();) {
            ChangeTrace aSubChangeTrace = (ChangeTrace) subCTItr.next();
            aSubChangeTrace.setWorkspaceSuperFeatureOid(postMergeOid);
        }

        // Debug Diagnostics...
        if (DEBUG_CLASS || debugMethod) {
            System.out.println(
                    "EXIT: Workspace.setupMergeOfPromoted(preMergeWorkspaceTranscript1 = " + 
                    preMergeOid1 + ", preMergeWorkspaceTranscript2 = " + 
                    preMergeOid2 + ", postMergeWorkspaceTranscript = " + 
                    postMergeOid + ");");
            this.checkPointRepRel("EXIT setupMergeOfPromoted();", preMergeOid1, 
                                  true);
            this.checkPointRepRel("EXIT setupMergeOfPromoted();", preMergeOid2, 
                                  true);
            this.checkPointRepRel("EXIT setupMergeOfPromoted();", postMergeOid, 
                                  true);
        }
    }

    private void checkPointRepRel(String tag, OID anOid, boolean alwaysTag) {
        ChangeTrace testChangeTrace = this.getChangeForOid(anOid);

        if (testChangeTrace == null) {
            if (DEBUG_CLASS) {
                System.out.println("*** Check Point " + tag + " for OID: " + 
                                   anOid + "restRepRel is NULL! ***");
            }
        } else if (alwaysTag) {
            if (DEBUG_CLASS) {
                System.out.print("=== Check Point " + tag + " for OID: " + 
                                 anOid + "restRepRel =");
            }

            testChangeTrace.print();
        }
    }

    /**
     * Obsolete this Oid from the workspace.
     * Save off a reference to the obsoleted CuratedFeature
     * Save off the alignments...
     */
    void obsoleteInWorkspace(OID workspaceOid, CuratedFeature obsoletedFeature) {
        // Debug Diagnostics...
        if (DEBUG_CLASS) {
            System.out.println(
                    "ENTER: Workspace.obsoleteInWorkspace(workspaceOID=" + 
                    workspaceOid + ", obsoletedFeature=" + obsoletedFeature + 
                    ");");
            this.printChangeTraces();
        }

        // Check args...
        if ((workspaceOid == null) || (obsoletedFeature == null)) {
            if (DEBUG_CLASS) {
                System.out.println("Bad Args!  EXIT");
            }

            return;
        }

        // See if we already have a change trace for the workspace OID...
        ChangeTrace theChangeTrace = this.getChangeForWorkspaceOid(workspaceOid);

        // If we have one already, add the promoted Oid...
        if (theChangeTrace != null) {
            theChangeTrace.obsolete(obsoletedFeature);


            // Item obsoleted in the workspace, mark the workspace as dirty...
            this.setWasDirtied();
        }
        // Otherwise?
        else {
            if (DEBUG_CLASS) {
                System.out.println("Did not find a ChangeTrace for OID!");
            }
        }

        // Debug Diagnostics...
        if (DEBUG_CLASS) {
            System.out.println(
                    "EXIT: Workspace.obsoleteInWorkspace(workspaceOID=" + 
                    workspaceOid + ", obsoletedFeature=" + obsoletedFeature + 
                    ");");
            this.printChangeTraces();
        }
    }

    /**
     * Set the ReplacementRelationship type explicitly...
     */
    void setReplacementRelationshipType(OID workspaceOid, 
                                        byte newReplacementType) {
        // Debug Diagnostics...
        if (DEBUG_CLASS) {
            System.out.println(
                    "ENTER: Workspace.setReplacementRelationshipType(workspaceOID=" + 
                    workspaceOid + ", newReplacementType=" + 
                    newReplacementType + ");");
            this.printChangeTraces();
        }

        // Check args...
        if (workspaceOid == null) {
            if (DEBUG_CLASS) {
                System.out.println("Bad Args!  EXIT");
            }

            return;
        }

        // See if we already have a change trace for the workspace OID...
        ChangeTrace theChangeTrace = this.getChangeForWorkspaceOid(workspaceOid);

        // If we have one already, add the promoted Oid...
        if (theChangeTrace != null) {
            theChangeTrace.setChangeType(newReplacementType);


            // Item changed in the workspace, mark the workspace as dirty...
            this.setWasDirtied();
        }

        // Debug Diagnostics...
        if (DEBUG_CLASS) {
            System.out.println(
                    "EXIT: Workspace.setReplacementRelationshipType(workspaceOID=" + 
                    workspaceOid + ", newReplacementType=" + 
                    newReplacementType + ");");
            this.printChangeTraces();
        }
    }

    /**
     * Set the ReplacementRelationship type explicitly...
     */
    void setReplacementRelationship(OID workspaceOid, 
                                    ReplacementRelationship newRepRel) {
        // Debug Diagnostics...
        if (DEBUG_CLASS) {
            System.out.println(
                    "ENTER: Workspace.setReplacementRelationship(workspaceOID=" + 
                    workspaceOid + ", newRepRel=" + newRepRel + ");");
            this.printChangeTraces();
        }

        // Check args...
        if (workspaceOid == null) {
            if (DEBUG_CLASS) {
                System.out.println("Bad Args!  EXIT");
            }

            return;
        }

        // See if we already have a change trace for the workspace OID...
        ChangeTrace theChangeTrace = this.getChangeForWorkspaceOid(workspaceOid);

        // If we have one already, add the promoted Oid...
        if (theChangeTrace != null) {
            theChangeTrace.setPromotedOids(newRepRel.getReplacementOIDs());
            theChangeTrace.setChangeType(newRepRel.getReplacementTypeAsByte());


            // Item changed in the workspace, mark the workspace as dirty...
            this.setWasDirtied();
        }

        // Debug Diagnostics...
        if (DEBUG_CLASS) {
            System.out.println(
                    "EXIT: Workspace.setReplacementRelationshipType(workspaceOID=" + 
                    workspaceOid + ", newRepRel=" + newRepRel + ");");
            this.printChangeTraces();
        }
    }

    /**
     * Delete this Oid from the workspace entirely.
     * Remove any change tracking for this workspace OID.
     * This call allows us to distinguish between;
     * a) something deleted from the workspace, and to be obsoleted from promoted
     * b) something removed from consideration from the workspace.
     */
    void deleteFeatureFromWorkspace(Feature workspaceFeature) {
        // Check args...
        if (workspaceFeature == null) {
            if (DEBUG_CLASS) {
                System.out.println(
                        "Workspace.deleteOidFromWorkspace(workspaceFeature) Bad Args!  EXIT");
            }

            return;
        }

        OID workspaceOid = workspaceFeature.getOid();

        // Debug Diagnostics...
        if (DEBUG_CLASS) {
            System.out.println(
                    "ENTER: Workspace.deleteOidFromWorkspace(workspaceFeature=" + 
                    workspaceFeature.getOid() + "); called.");
            this.printChangeTraces();
        }

        // See if we already have a change trace for the workspace OID...
        ChangeTrace theChangeTrace = this.getChangeForWorkspaceOid(workspaceOid);
        workspaceFeature.removeFeatureObserver(this.myFeatureObserver);

        if (theChangeTrace != null) {
            // Remove it from the change trace...
            theChangeTrace.removeWorkspaceOid(workspaceOid);


            // Remove the reference from the hash...
            oidToChangeTrace.remove(workspaceOid);

            // If we no longer have any workspace OIDs... remove it...
            if (!theChangeTrace.hasWorkspaceOids()) {
                // First trigger a notification
                theChangeTrace.setChangeType(ReplacementRelationship.UNMODIFIED);


                // Remove it.
                allChangesToPromotedGV.remove(theChangeTrace);
                this.removeChangeTrace(theChangeTrace);
            } else {
                if (DEBUG_CLASS) {
                    System.out.println("Partial delete from workspace!");
                }
            }


            // Mark the Feature as deleted this session... (for XML write out).
            this.markWorkspaceFeatureAsDeletedThisSession(workspaceOid);


            // Item deleted from the workspace, mark the workspace as dirty...
            this.setWasDirtied();
        } else {
            if (DEBUG_CLASS) {
                System.out.println("No ChagneTrace for WorspaceOID!  EXIT");
            }

            return;
        }

        // Debug Diagnostics...
        if (DEBUG_CLASS) {
            System.out.println("EXIT: Workspace.deleteOidFromWorkspace(OID=" + 
                               workspaceOid + "); called.");
            this.printChangeTraces();
        }
    }

    /**
     * Explicite request to merge two ChangeTraces into a single.
     * This will transition the change trace to "modified".
     * This should be "package" visible and only called from the CuratedFeatureMutator.
     */
    void mergeChanges(OID sourceOid1, OID sourceOid2, OID resultingOID) {
        // New item added to the workspace, mark the workspace as dirty...
        this.setWasDirtied();
    }

    //=========== Workspace Observer support==================================================

    /**
     * Add a workspace observer.
     */
    public void addObserver(WorkspaceObserver observerToAdd, 
                            boolean swingThreadNotificationOnly) {
        this.addObserver(observerToAdd, swingThreadNotificationOnly, true);
    }

    /**
     * Add a workspace observer.
     */
    public void addObserver(WorkspaceObserver observerToAdd, 
                            boolean swingThreadNotificationOnly, 
                            boolean bringUpToDate) {
        // Add to the list...
        if (swingThreadNotificationOnly) {
            if (swingEventThreadObservers == null) {
                swingEventThreadObservers = new ArrayList();
            }

            swingEventThreadObservers.add(observerToAdd);
        } else {
            if (anyThreadObservers == null) {
                anyThreadObservers = new ArrayList();
            }

            anyThreadObservers.add(observerToAdd);
        }

        // Bring up to date if requested...
        if (bringUpToDate) {
            // Notify the dirty state...
            observerToAdd.noteWorkspaceDirtyStateChanged(this, this.isDirty);

            // Notify for each of the ChangeTraces...
            ArrayList allCTs = (ArrayList) allChangesToPromotedGV.clone();

            for (Iterator ctItr = allCTs.iterator(); ctItr.hasNext();) {
                observerToAdd.noteChangeTraceChanged(this, 
                                                     (ChangeTrace) ctItr.next());
            }
        }
    }

    /**
     * Get the load
     */
    private WorkspaceObserver[] getObservers(boolean swingThread) {
        WorkspaceObserver[] emptyArray = new WorkspaceObserver[0];

        if (swingThread) {
            if (swingEventThreadObservers != null) {
                return (WorkspaceObserver[]) swingEventThreadObservers.toArray(
                                 emptyArray);
            } else {
                return emptyArray;
            }
        } else {
            if (anyThreadObservers != null) {
                return (WorkspaceObserver[]) anyThreadObservers.toArray(
                                 emptyArray);
            } else {
                return emptyArray;
            }
        }
    }

    /**
     * Remove an observer.
     */
    public void removeObserver(WorkspaceObserver observerToRemove) {
        if (swingEventThreadObservers != null) {
            swingEventThreadObservers.remove(observerToRemove);

            if (swingEventThreadObservers.size() == 0) {
                swingEventThreadObservers = null;
            }
        }

        if (anyThreadObservers != null) {
            anyThreadObservers.remove(observerToRemove);

            if (anyThreadObservers.size() == 0) {
                anyThreadObservers = null;
            }
        }
    }

    /**
     * Post notification for Workspace Dirty State Changed...
     */
    private void postWorkspaceDirtyStateChanged(boolean newDirtyState) {
        WorkspaceObserver[] anyThreadObserverArry = getObservers(false);

        if (anyThreadObserverArry.length > 0) {
            new WorkspaceNotification(anyThreadObserverArry, newDirtyState).run();
        }

        WorkspaceObserver[] swingThreadObserverArry = getObservers(true);

        if (swingThreadObserverArry.length > 0) {
            notificationQueue.addQueue(
                    new WorkspaceNotification(swingThreadObserverArry, 
                                              newDirtyState));
        }
    }

    /**
     * Post notification for Change Trace State Changed...
     * @todo: do the noticiation queue thang.
     */
    void postChangeTraceChanged(ChangeTrace theChangeTrace) {
        WorkspaceObserver[] anyThreadObserverArry = getObservers(false);

        if (anyThreadObserverArry.length > 0) {
            new WorkspaceNotification(anyThreadObserverArry, theChangeTrace).run();
        }

        WorkspaceObserver[] swingThreadObserverArry = getObservers(true);

        if (swingThreadObserverArry.length > 0) {
            notificationQueue.addQueue(
                    new WorkspaceNotification(swingThreadObserverArry, 
                                              theChangeTrace));
        }
    }

    //=======Document Dirty support==================================================

    /**
     * Check if the Workspace is dirty (aka... needs to be saved, has been changed
     * since it was saved.)
     * The "Dirty" state indicates if the workspace has changed since it was last
     * saved (Workspace.isDirty() == true) or not.
     */
    public boolean isDirty() {
        return (isDirty && getWorkspaceOids() != null);
    }

    /**
     * Set that something has been "dirtied" in the workspace...
     */
    private void setWasDirtied() {
        //if there is nothing in Workspace then there is
        // nothing to save hence dirty bit should be false
        if (getWorkspaceOids().size() == 0) {
            isDirty = false;
            this.postWorkspaceDirtyStateChanged(isDirty);

            return;
        } else {
            if (isDirty) {
                return;
            }

            isDirty = true;


            // Fire off Notification...
            this.postWorkspaceDirtyStateChanged(isDirty);

            return;
        }
    }

    /**
     * Set that the Workspace was saved... and should be marked as not dirty.
     */
    public void setWasSaved() {
        // Make sure we're changing states...
        if (!isDirty) {
            return;
        }

        isDirty = false;


        // Fire off Notification...
        this.postWorkspaceDirtyStateChanged(isDirty);
    }

    //===========ChangeTrace Management ===========================================

    /**
     * Add a new Change trace and set up has references...
     */
    private void addNewChangeTrace(ChangeTrace newChangeTrace) {
        // Add references for the workspace oids...
        this.addOidToChangeTraceRefs(newChangeTrace.workspaceOIDs, 
                                     newChangeTrace);


        // Add references for the promoted oids...
        this.addOidToChangeTraceRefs(newChangeTrace.promotedOIDs, 
                                     newChangeTrace);


        // Add it to the list of all changes...
        this.allChangesToPromotedGV.add(newChangeTrace);
    }

    /**
     * Add a set of OID references to a change trace.
     */
    private void addOidToChangeTraceRefs(Set anOidSet, ChangeTrace aChangeTrace) {
        for (Iterator oidItr = anOidSet.iterator(); oidItr.hasNext();) {
            this.oidToChangeTrace.put(oidItr.next(), aChangeTrace);
        }
    }

    /**
     * Remove any references from any of the OIDs in ChangeTrace
     */
    private void removeChangeTrace(ChangeTrace aChangeTrace) {
        if (aChangeTrace == null) {
            return;
        }


        // Remove references for the workspace oids...
        this.removeAllOidToChangeTraceRefs(aChangeTrace);


        // Add it to the list of all changes...
        this.allChangesToPromotedGV.remove(aChangeTrace);
    }

    /**
     * Remove oidToChangeTrace hash table references for the list of Oids
     */
    private void removeOidToChangeTraceRefs(Set anOidSet) {
        if (anOidSet == null) {
            return;
        }

        for (Iterator setItr = anOidSet.iterator(); setItr.hasNext();) {
            oidToChangeTrace.remove(setItr.next());
        }
    }

    /**
     * Remove all references to a ChangeTrace...
     */
    private void removeAllOidToChangeTraceRefs(ChangeTrace aChangeTrace) {
        if (!oidToChangeTrace.containsValue(aChangeTrace)) {
            return;
        }

        Set allKeys = oidToChangeTrace.keySet();

        // Find all the keys that reference this ChangeTrace...
        Set keysToChangeTrace = new HashSet();

        for (Iterator allKeyItr = allKeys.iterator(); allKeyItr.hasNext();) {
            Object key = allKeyItr.next();
            Object value = oidToChangeTrace.get(key);

            if (value == aChangeTrace) {
                keysToChangeTrace.add(key);
            }
        }

        // Remove the key references...
        for (Iterator badKeyItr = keysToChangeTrace.iterator();
             badKeyItr.hasNext();) {
            oidToChangeTrace.remove(badKeyItr.next());
        }
    }

    /**
     * Print the Workspace Statistics.
     */
    public void printWorkspaceStatistics() {
        System.out.println("Workspace Stats...");
        System.out.println("Workspace isDirty = " + isDirty);
        this.printChangeTraces();
        this.printOidsDeletedThisSession();
    }

    /**
     * Print the change traces...
     * Package visible.
     */
    public void printChangeTraces() {
        if (!DEBUG_CLASS) {
            return;
        }

        ChangeTrace aChange = null;
        System.out.println(
                "==============================================================================");
        System.out.println("Workspace has (" + allChangesToPromotedGV.size() + 
                           ") change trace(s) (aka Replacement Relationships)...");

        for (Iterator changeItr = allChangesToPromotedGV.iterator();
             changeItr.hasNext();) {
            aChange = (ChangeTrace) changeItr.next();
            aChange.print();
        }

        System.out.println(
                "==============================================================================");
    }

    /**
     * Print the oids deleted this session...
     */
    public void printOidsDeletedThisSession() {
        if (!DEBUG_CLASS) {
            return;
        }

        System.out.print("Workspace has (" + oidsDeletedThisSession.size() + 
                         ") OIDs deleted this session... [");

        OID anOid;

        for (Iterator oidItr = oidsDeletedThisSession.iterator();
             oidItr.hasNext();) {
            anOid = (OID) oidItr.next();
            System.out.print(anOid.toString() + ",");
        }

        System.out.println("]");
    }

    /**
     * Get a replacement relationship for a workspaceFeature.
     * Returns null if the argument is not workspace feature or there is no
     * ReplacementRelationship available.
     */
    public ReplacementRelationship getReplacementRelationForWorkspaceFeature(CuratedFeature aWSFeature) {
        // Check args...
        if (aWSFeature == null) {
            return null;
        }

        // Return based on OID...
        return this.getReplacementRelationForWorkspaceOid(aWSFeature.getOid());
    }

    /**
     * Get a replacement relationship for a promoted Feature.
     * Returns null if the argument is not promoted feature or there is no
     * ReplacementRelationship available.
     */
    public ReplacementRelationship getReplacementRelationForPromotedOid(OID anOid) {
        // Check args...
        if ((anOid == null) || (anOid.isScratchOID())) {
            return null;
        }

        ChangeTrace theChangeTrace = this.getChangeForPromotedOid(anOid);

        // Get the list of OIDs and the Replacement type...
        if (theChangeTrace != null) {
            return theChangeTrace.getAsReplacementRelationship();
        }

        return null;
    }

    /**
     * Get a replacement relationship for a workspace OID.
     * Returns null if the argument is not workspace OID or there is no
     * ReplacementRelationship available.
     */
    public ReplacementRelationship getReplacementRelationForWorkspaceOid(OID anOid) {
        // Check args...
        if ((anOid == null) || (!anOid.isScratchOID())) {
            return null;
        }

        ReplacementRelationship theReplacementRel = null;
        ChangeTrace theChangeTrace = this.getChangeForWorkspaceOid(anOid);

        // Get the list of OIDs and the Replacement type...
        if (theChangeTrace != null) {
            theReplacementRel = theChangeTrace.getAsReplacementRelationship();
        }

        if (theReplacementRel == null) {
            if (DEBUG_CLASS) {
                System.out.println(
                        "Workspace.getReplacementRelationForWorkspaceOid() returning null RepRel for OID: " + 
                        anOid);
            }
        }

        return theReplacementRel;
    }

    /**
     * Get the workspace super feature oid for workspace oid.
     */
    public OID getWorkspaceSuperFeatureOid(OID anOid) {
        OID superFeatOid = null;
        ChangeTrace theChangeTrace = this.getChangeForOid(anOid);

        if (theChangeTrace != null) {
            superFeatOid = theChangeTrace.getWorkspaceSuperFeatureOid();
        }

        return superFeatOid;
    }

    /**
     * Get the obsoleted workspace curated feature for workspace oid.
     */
    public CuratedFeature getObsoletedWorkspaceFeatureForOid(OID anOid) {
        CuratedFeature obsoletedFeat = null;
        ChangeTrace theChangeTrace = this.getChangeForOid(anOid);

        if (theChangeTrace != null) {
            obsoletedFeat = theChangeTrace.getObsoletedCuratedFeature();
        }

        return obsoletedFeat;
    }

    /**
     * Get the obsoleted alignment for a workspace feature oid.
     */
    public Alignment getObsoletedAlignmentForWorkspaceOid(OID anOid) {
        Alignment obsoletedAlign = null;
        ChangeTrace theChangeTrace = this.getChangeForOid(anOid);

        if (theChangeTrace != null) {
            obsoletedAlign = theChangeTrace.getObsoletedAlignment();
        }

        return obsoletedAlign;
    }

    /**
     * Get the Curated features that were obsoleted and were Subfeatures
     * of a given workspace oid.
     */
    public Set getObsoletedSubFeatureOfSuperFeature(OID superFeatOid) {
        Set obsSubFeats = new HashSet();

        if (superFeatOid == null) {
            return obsSubFeats;
        }

        // Get all the obsoleted change traces...
        Set subChangeTraces = this.getChangeTracesWithSuperOid(superFeatOid);
        ChangeTrace aChangeTrace;

        for (Iterator ctItr = subChangeTraces.iterator(); ctItr.hasNext();) {
            aChangeTrace = (ChangeTrace) ctItr.next();

            if (aChangeTrace.isChangeType(ReplacementRelationship.OBSOLETE)) {
                if (aChangeTrace.getObsoletedCuratedFeature() == null) {
                    if (DEBUG_CLASS) {
                        System.out.println(
                                "ERROR in Change Trace Obsolete Tracking!  Null obsoleted feature.");
                    }

                    aChangeTrace.print();
                } else {
                    obsSubFeats.add(aChangeTrace.getObsoletedCuratedFeature());
                }
            }
        }

        return obsSubFeats;
    }

    /**
     * Get the Obsoleted ChangeTraces that were obsoleted and were Subfeatures
     * of a given workspace oid.
     */
    public Set getChangeTracesWithSuperOid(OID superFeatOid) {
        Set subChangeTraces = new HashSet();

        if (superFeatOid == null) {
            return subChangeTraces;
        }

        // Run through the CT's and check
        ChangeTrace aChangeTrace;

        for (Iterator ctItr = allChangesToPromotedGV.iterator();
             ctItr.hasNext();) {
            aChangeTrace = (ChangeTrace) ctItr.next();

            if (superFeatOid.equals(aChangeTrace.getWorkspaceSuperFeatureOid())) {
                subChangeTraces.add(aChangeTrace);
            }
        }

        return subChangeTraces;
    }

    /**
     * Get the Curated features that were obsoleted and had no super-feature.
     * This method gives a chance to make sure the load from facade has been done.
     * This is primarily used in the Promotion tool.
     */
    public Set getRootObsoletedFeatures(GenomicAxis axis) {
        this.loadObsoletedRootFeaturesFromFacade(axis);

        return this.getRootObsoletedFeatures();
    }

    /**
     * Get the Curated features that were obsoleted and had no super-feature.
     */
    public Set getRootObsoletedFeatures() {
        Set obsRootFeats = new HashSet();

        // Run through the CT's and check
        ChangeTrace aChangeTrace;

        for (Iterator ctItr = allChangesToPromotedGV.iterator();
             ctItr.hasNext();) {
            aChangeTrace = (ChangeTrace) ctItr.next();

            if (aChangeTrace.isChangeType(ReplacementRelationship.OBSOLETE) && 
                    (aChangeTrace.getWorkspaceSuperFeatureOid() == null)) {
                obsRootFeats.add(aChangeTrace.getObsoletedCuratedFeature());
            }
        }

        return obsRootFeats;
    }

    /**
     * Check if the argument feature is replaced by
     */
    public boolean isPromotedReplacedByScratch(CuratedFeature aPromoFeature) {
        ChangeTrace theChangeTrace = this.getChangeForPromotedOid(
                                             aPromoFeature.getOid());

        if (theChangeTrace != null) {
            return true;
        }

        return false;
    }

    /**
     * Get the set of workspace Oids that have been loaded into the workspace.
     * This will return workspace oids of all types; new, unmodified, modified,
     * and obsoleted oids.
     * All the returned oids can be used to call
     */
    public Set getWorkspaceOids() {
        HashSet returnSet = new HashSet();
        Set oidSet = oidToChangeTrace.keySet();
        OID anOid;

        for (Iterator itr = oidSet.iterator(); itr.hasNext();) {
            anOid = (OID) itr.next();

            if (anOid.isScratchOID()) {
                returnSet.add(anOid);
            }
        }

        return returnSet;
    }

    /**
     * Get a ChangeTrace for an OID...
     * Will return null.
     * Does not care how the OID participates, either as the promoted or workspace OID.
     */
    private ChangeTrace getChangeForOid(OID anOid) {
        return (ChangeTrace) oidToChangeTrace.get(anOid);
    }

    /**
     * Get a ChangeTrace for a Workspace OID...
     * Will return NULL if this OID does not participate as the workspace OID in any changes.
     */
    private ChangeTrace getChangeForWorkspaceOid(OID anOid) {
        ChangeTrace aChange = getChangeForOid(anOid);

        if ((aChange != null) && 
                (aChange.getWorkspaceOids().contains(anOid))) {
            return aChange;
        }

        return null;
    }

    /**
     * Get a ChangeTrace for a Promoted OID...
     * Will return NULL if this OID does not participate as the promoted OID in any changes.
     */
    public ChangeTrace getChangeForPromotedOid(OID anOid) {
        ChangeTrace aChange = getChangeForOid(anOid);

        if ((aChange != null) && 
                (aChange.getPromotedOids().contains(anOid))) {
            return aChange;
        }

        return null;
    }

    /**
     * Check to make sure that the argument is a valid PI to be assigned as
     * being replaced by this PI.
     * Currently, this simply means checking the class type.
     * Could also check the name space.
     * Subclasses can also over-ride this method to add further criteria.
    public boolean isValidReplacement(ProxyInterval replacedPI) {
      if (this.getClass() != replacedPI.getClass()) return false;
      return true;
    }
     */
    /**
     * Set up the initial "replaces relationship" between curated workspace feature
     * and a promoted feature (could be null).
     * This method should probably return an existing ChangeTrace if
     * either the workspaceFeature or the promotedFeature already participate in
     * a change trace.
     * @todo: Need to check for many to many (not allowed).
     */
    ChangeTrace newWorkspaceToPromotedChangeTrace(OID workspaceOid, 
                                                  OID promotedOid) {
        // Validate arguments...
        if (workspaceOid == null) {
            return null;
        }

        if (promotedOid == null) {
            if (DEBUG_CLASS) {
                System.out.println("Creating NEW change trace for WS-OID:" + 
                                   workspaceOid);
            }

            return new ChangeTrace(this, workspaceOid, promotedOid, 
                                   ReplacementRelationship.NEW);
        }

        if (DEBUG_CLASS) {
            System.out.println("Creating UNMODIFIED for WS-OID:" + 
                               workspaceOid + " and P-OID:" + promotedOid);
        }

        return new ChangeTrace(this, workspaceOid, promotedOid, 
                               ReplacementRelationship.UNMODIFIED);
    }

    /**
     * Set up the initial "replaces relationship" between.
     */
    ChangeTrace newWorkspaceToPromotedChangeTrace(Set workspaceEntities, 
                                                  Set promotedEntities, 
                                                  byte changeType) {
        // Validate arguments...
        if (workspaceEntities == null) {
            return null;
        }

        // Make sure we don't already have a replacement relationship for any of these
        // Create the new relationship...
        ChangeTrace newChangeTrace;

        if (promotedEntities == null) {
            newChangeTrace = new ChangeTrace(this, workspaceEntities, 
                                             promotedEntities, changeType);
        } else {
            newChangeTrace = new ChangeTrace(this, workspaceEntities, 
                                             promotedEntities, changeType);
        }


        // Add
        allChangesToPromotedGV.add(newChangeTrace);


        // Update the proper hashmap...
        putGEOidsToMap(oidToChangeTrace, workspaceEntities, newChangeTrace);
        putGEOidsToMap(oidToChangeTrace, promotedEntities, newChangeTrace);

        // Return the new replacement relationship...
        return newChangeTrace;
    }

    /**
     * Add a vector of keys with value of theTrace.
     */
    private void putGEOidsToMap(HashMap theMap, Set entities, 
                                ChangeTrace theChangeTrace) {
        // Check the arguments...
        if ((theMap == null) || (entities == null) || 
                (theChangeTrace == null)) {
            return;
        }

        // Add the key value mappings...
        GenomicEntity aGenomicEntity;

        for (Iterator entityItr = entities.iterator(); entityItr.hasNext();) {
            aGenomicEntity = (GenomicEntity) entityItr.next();
            theMap.put(aGenomicEntity.getOid(), theChangeTrace);
        }
    }

    /**
     * Check for any ChangeTraces that the participate in...
     */
    private Collection getValuesForGEKeysFromMap(HashMap theMap, 
                                                 GenomicEntity[] entities) {
        Collection theValues = new ArrayList();
        Object aValue;

        // Check the arguments...
        if ((theMap == null) || (entities == null)) {
            return theValues;
        }

        // Add the key value mappings...
        for (int i = 0; i < entities.length; i++) {
            aValue = theMap.get(entities[i].getOid());

            if (aValue != null) {
                theValues.add(aValue);
            }
        }

        // Return the values found...
        return theValues;
    }

    /**
     * Get all the replacement relationship for a given workspace entity.
    public ReplacementRelationship getReplacementReplationshipForWorkspaceEntity(GenomicEntity workspaceEntity) {
    }
     */
    /**
     * Methods that determine of an action is allowed on a workspace entity.
     * If the workspace entity has been split, it can not be merged.
     */
    public boolean canModifyWorkspaceEntity(GenomicEntity workspaceEntity) {
        return true;
    }

    public boolean canSplitWorkspaceEntity(GenomicEntity workspaceEntity) {
        return true;
    }

    public boolean canMergeWorkspaceEntity(GenomicEntity workspaceEntity) {
        return true;
    }

    public boolean canObsoleteWorkspaceEntity(GenomicEntity workspaceEntity) {
        return true;
    }

    //===========OIDs Deleted This Session support==================================================

    /**
     * Mark a feature (of this workspace) as being deleted this session.
     * This may end up being a private method once we wire up the observation of entities.
     */
    private void markWorkspaceFeatureAsDeletedThisSession(OID workspaceOid) {
        if (!workspaceOid.isScratchOID()) {
            throw new IllegalArgumentException(
                    "Feature did not come from Workspace");
        }

        oidsDeletedThisSession.add(workspaceOid);
    }

    /**
     * Remove this feature (of this workspace) from the "deleted this session" list.
     * This may end up being a private method once we wire up the observation of entities.
     */
    private void unmarkWorkspaceFeatureAsDeletedThisSession(OID workspaceOid) {
        oidsDeletedThisSession.remove(workspaceOid);
    }

    /**
     * Get the set of OIDs of all the Features that have been
     * deleted from this workspace this session (since opening the workspace).
     */
    public Set getOIDsOfFeaturesDeletedThisSession() {
        return (Set) oidsDeletedThisSession.clone();
    }

    //=========== Transion Methods ================================================

    /**
     * Track the "modified" transition of a change trace.
     */
    private void transitionModified(CuratedFeature entity) {
        ChangeTrace aChangeTrace = this.getChangeForWorkspaceOid(
                                           entity.getOid());

        if (aChangeTrace == null) {
            return;
        }

        aChangeTrace.modify();
        this.setWasDirtied();
    }

    /**
     * Track the "obsoleted" transition of a change trace.
     */
    private void transitionObsoleted(CuratedFeature entity) {
        if (DEBUG_CLASS) {
            System.out.println("Transition Obsolete called.");
        }

        ChangeTrace aChangeTrace = this.getChangeForWorkspaceOid(
                                           entity.getOid());

        if (aChangeTrace == null) {
            return;
        }

        aChangeTrace.obsolete(entity);
        this.setWasDirtied();
    }

    //=========== Inner Classes ==================================================

    /**
     * Inner class for notification...
     */
    private class WorkspaceNotification implements Runnable {
        int type;
        boolean newDirtyState;
        ChangeTrace subjectChangeTrace;
        private WorkspaceObserver[] observers;

        WorkspaceNotification(WorkspaceObserver[] observers, 
                              boolean aDirtyState) {
            type = NOTE_DIRTY_STATE_CHANGED;
            newDirtyState = aDirtyState;
            this.observers = observers;
        }

        WorkspaceNotification(WorkspaceObserver[] observers, 
                              ChangeTrace aChangeTrace) {
            type = NOTE_CHANGE_TRACE_CHANGED;
            subjectChangeTrace = aChangeTrace;
            this.observers = observers;
        }

        public void run() {
            // Work through the observers...
            for (int i = 0; i < observers.length; i++) {
                switch (type) {
                case NOTE_DIRTY_STATE_CHANGED:
                    observers[i].noteWorkspaceDirtyStateChanged(Workspace.this, 
                                                                newDirtyState);

                    break;

                case NOTE_CHANGE_TRACE_CHANGED:
                    observers[i].noteChangeTraceChanged(Workspace.this, 
                                                        subjectChangeTrace);

                    break;
                }
            }
        }
    } // End of inner class: WorkspaceNotification

    /**
     * Inner class that is used to observer the Features...
     */
    private class MyFeatureObserver extends FeatureObserverAdapter
        implements ModelSynchronizationObserver {
        /**
         * Protected constructor for the mutator class...
         */
        protected MyFeatureObserver() {
        }

        //========= GenomicEntity Notification ==============================

        /**
         * Notification that some detail was changed on a Genomic Entity.
         * This will invoke the modified transition on the Genomic Entity.
         * @see GenomicEntityObserver.noteEntityDetailsChanged();
         */
        public void noteEntityDetailsChanged(GenomicEntity entity, 
                                             boolean initialLoad) {
            if (DEBUG_CLASS) {
                System.out.println(
                        "Workspace.MyFeatureObserver.noteEntityDetailsChanged()");
            }

            // Ignore this if it's the initial load (delayed threaded load).
            if (initialLoad) {
                return;
            }

            if ((entity != null) && (entity instanceof CuratedFeature)) {
                Workspace.this.transitionModified((CuratedFeature) entity);

                if (!(entity instanceof SuperFeature)) {
                    Feature f = ((CuratedFeature) entity).getSuperFeature();
                    Workspace.this.transitionModified((CuratedFeature) f);

                    if (f != null) {
                        Feature rootFeature = f.getSuperFeature();

                        if (rootFeature != null) {
                            Workspace.this.transitionModified(
                                    (CuratedFeature) rootFeature);
                        }
                    }
                }
            }
        }

        //========= AlignableEntity Notification ==============================

        /**
         * Watch for alignments so we can trigger a load of obsoleted sub-structure.
         */
        public void noteAlignedToAxis(Alignment alignment) {
            if (DEBUG_CLASS) {
                System.out.println("Workspace noteAlignedToAxis of Entity:" + 
                                   alignment.getEntity().getOid() + 
                                   " to Axis:" + 
                                   alignment.getAxis().getOid());
            }

            Workspace.this.loadObsoletedSubStructureFromFacade(
                    (CuratedFeature) alignment.getEntity(), alignment.getAxis());
        }

        /**
         * Notification that the axis alignment of a Alignable Genomic Entity has changed.
         * This will invoke the modified transition on the Genomic Entity.
         */
        public void noteAxisAlignmentChanged(Alignment changedAlignment) {
            if (DEBUG_CLASS) {
                System.out.println(
                        "Workspace noteAxisAlignmentChanged of Entity:" + 
                        changedAlignment.getEntity().getOid() + " to Axis:" + 
                        changedAlignment.getAxis().getOid());
            }

            AlignableGenomicEntity entity = changedAlignment.getEntity();

            if ((entity != null) && (entity instanceof CuratedFeature)) {
                Workspace.this.transitionModified((CuratedFeature) entity);
            }
        }

        public void noteUnalignedFromAxis(Alignment alignment) {
            if (alignment == null) {
                return;
            }

            if (DEBUG_CLASS) {
                System.out.println("Workspace noteUnalignment of Entity:" + 
                                   alignment.getEntity().getOid() + 
                                   " from Axis:" + 
                                   alignment.getAxis().getOid());
            }

            OID anOid = alignment.getEntity().getOid();
            ChangeTrace theChangeTrace = Workspace.this.getChangeForWorkspaceOid(
                                                 anOid);

            if (DEBUG_CLASS) {
                System.out.println("Workspace noteUnalignment from: " + 
                                   alignment.getAxis().getOid());
            }

            if (theChangeTrace != null) {
                theChangeTrace.setObsoletedAlignment(alignment);
            }
        }

        //========= Feature Notification ==============================

        /**
         * Notification that a sub-feature was added to a super feature.
         * This will invoke the modified transition on the super feature.
         */
        public void noteSubFeatureAdded(Feature superFeature, 
                                        Feature addedSubFeature) {
            if ((superFeature == null) || (addedSubFeature == null)) {
                if (DEBUG_CLASS) {
                    System.out.println(
                            "Workspace.MyFeatureObserver.noteSubFeatureAdded(super=NULL" + 
                            ",sub=NULL)");
                }

                return;
            }

            if (DEBUG_CLASS) {
                System.out.println(
                        "Workspace.MyFeatureObserver.noteSubFeatureAdded(super=" + 
                        superFeature.getOid() + ",sub=" + 
                        addedSubFeature.getOid() + ")");
            }

            ChangeTrace theChangeTrace = Workspace.this.getChangeForOid(
                                                 addedSubFeature.getOid());

            if (theChangeTrace != null) {
                theChangeTrace.setWorkspaceSuperFeatureOid(
                        superFeature.getOid());
            }

            if (superFeature instanceof CuratedFeature/*&& !(((CuratedFeature)superFeature).getReplacementRelationshipType().equals(ReplacementRelationship.TYPE_NEW))*/
            ) {
                Workspace.this.transitionModified((CuratedFeature) superFeature);
            }
        }

        /**
         * Notification that a sub-feature was removed from a super feature.
         * If the sub-feature was NOT "new" this will invoke the modified transition
         * on the super feature.
         */
        public void noteSubFeatureRemoved(Feature previousSuperFeature, 
                                          Feature removedSubFeature) {
            if (DEBUG_CLASS) {
                System.out.println(
                        "Workspace.MyFeatureObserver.noteSubFeatureRemoved()");
            }

            // Get the ChangeTrace for the removed subfeature...
            ChangeTrace subFeatCT = Workspace.this.getChangeForWorkspaceOid(
                                            removedSubFeature.getOid());

            if (subFeatCT != null) {
                // Also need to keep up with the superfeature tracking...
                // subFeatCT.setWorkspaceSuperFeatureOid(null);
            }

            // If the ChangeTrace of the removed subfeature is non-null AND not New...
            // transition the super feature...
            if ((subFeatCT != null) && 
                    (!subFeatCT.isChangeType(ReplacementRelationship.NEW)) && 
                    (previousSuperFeature instanceof CuratedFeature)) {
                Workspace.this.transitionModified(
                        (CuratedFeature) previousSuperFeature);
            }
        }

        // Feature Structure Notification ---------------------------------------

        /**
         * Notification that an evidence feature was added to a subject feature's evidence.
         * This will invoke the modified transition on the subject feature.
         */
        public void noteFeatureEvidenceAdded(Feature subjectFeature, 
                                             Feature addedEvidenceFeature) {
            if (DEBUG_CLASS) {
                System.out.println(
                        "Workspace.MyFeatureObserver.noteFeatureEvidenceAdded()");
            }

            if (subjectFeature instanceof CuratedFeature) {
                Workspace.this.transitionModified(
                        (CuratedFeature) subjectFeature);
            }
        }

        /**
         * Notification that an evidence feature was removed from a subject feature's evidence.
         * This will invoke the modified transition on the subject feature.
         */
        public void noteFeatureEvidenceRemoved(Feature subjectFeature, 
                                               Feature removedEvidenceFeature) {
            if (DEBUG_CLASS) {
                System.out.println(
                        "Workspace.MyFeatureObserver.noteFeatureEvidenceRemoved()");
            }

            if (subjectFeature instanceof CuratedFeature) {
                Workspace.this.transitionModified(
                        (CuratedFeature) subjectFeature);
            }
        }
    } // End of inner class: MyFeatureObserver
}