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

import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.genetics.GenomeVersion;
import api.stub.data.OID;
import api.stub.data.ReplacementRelationship;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 *
 * The ChangeTrace class is a record of change made to Promoted CuratedFeature
 * in the Workspace.
 * This is a record of what Workspace CuratedFeature(s) are intended to replace
 * what Promoted CuratedFeature(s), as well as an indication of how they were changed.
 * This is different from a ReplacementRelationship in that the ChangeTrace
 * supports tracking 0..N workspace features replacing 0..M promoted features,
 * while ReplacementRelationship supports 1 workspace feature to 0..M promoted features.
 *
 * @author Jay T. Schira
 * @version $Id$
 */
public class ChangeTrace implements java.io.Serializable {
    // First the class variables...
    private static boolean DEBUG_CHANGE_STATES = false;

    // State Pattern singleton instances
    private static NewState NEW_STATE;
    private static UnmodifiedState UNMODIFIED_STATE;
    private static ModifiedState MODIFIED_STATE;
    private static MergedState MERGED_STATE;
    private static SplitState SPLIT_STATE;
    private static ObsoletedState OBSOLETED_STATE;
    private static DeepModifiedState DEEPMODIFIED_STATE;

    static {
        NEW_STATE = new NewState();
        UNMODIFIED_STATE = new UnmodifiedState();
        MODIFIED_STATE = new ModifiedState();
        MERGED_STATE = new MergedState();
        SPLIT_STATE = new SplitState();
        OBSOLETED_STATE = new ObsoletedState();
        DEEPMODIFIED_STATE = new DeepModifiedState();
    }

    // Instance variables....
    transient Workspace workspace;
    HashSet workspaceOIDs = new HashSet();
    HashSet promotedOIDs = new HashSet();
    private ChangeTraceState changeTraceState;
    private byte type;
    private CuratedFeature obsoletedFeature = null;
    private Alignment obsoletedAlignment = null;
    private OID obsoletedAxisOid = null;
    private OID workspaceSuperFeatureOid = null;

    //================ Constructor Support ======================================

    /**
     * Construct a ChangeTrace with;
     * - a single workspace feature
     * - a Replacement Relationship.
     *
     * Keep the constructor package visible.
     */
    ChangeTrace(Workspace theWorkspace, CuratedFeature workspaceFeature, 
                ReplacementRelationship aReplacementRel) {
        this.workspace = theWorkspace;

        // Need to convert the entities to OIDs...
        if (workspaceFeature != null) {
            this.workspaceOIDs.add(workspaceFeature.getOid());
        }

        OID[] promotedOidArray = aReplacementRel.getReplacementOIDs();

        for (int i = 0; i < promotedOidArray.length; i++) {
            this.promotedOIDs.add(promotedOidArray[i]);
        }

        this.type = aReplacementRel.getReplacementTypeAsByte();
    }

    /**
     * Construct a ChangeTrace with;
     * - a set of workspace features
     * - a set of promoted features
     * - the change type
     *
     * Keep the constructor package visible.
     */
    ChangeTrace(Workspace theWorkspace, Set workspaceFeatures, 
                Set promotedFeatures, byte changeType) {
        this.workspace = theWorkspace;


        // Need to convert the entities to OIDs...
        this.fillOIDSetFromGESet(workspaceFeatures, workspaceOIDs);
        this.fillOIDSetFromGESet(promotedFeatures, promotedOIDs);
        this.type = changeType;
    }

    /**
     * Construct a ChangeTrace with;
     * - a workspace feature
     * - a promoted feature
     * - the change type
     *
     * Keep the constructor package visible.
     */
    ChangeTrace(Workspace theWorkspace, CuratedFeature workspaceFeature, 
                CuratedFeature promotedFeature, byte changeType) {
        this.workspace = theWorkspace;

        // Need to convert the entities to OIDs...
        if (workspaceFeature != null) {
            this.workspaceOIDs.add(workspaceFeature.getOid());
        }

        if (promotedFeature != null) {
            this.promotedOIDs.add(promotedFeature.getOid());
        }

        this.type = changeType;
    }

    /**
     * Construct a ChangeTrace with;
     * - a workspace feature
     * - a promoted feature
     * - the change type
     *
     * Keep the constructor package visible.
     */
    ChangeTrace(Workspace theWorkspace, OID workspaceOid, OID promotedOid, 
                byte changeType) {
        this.workspace = theWorkspace;

        // Need to convert the entities to OIDs...
        if (workspaceOid != null) {
            this.workspaceOIDs.add(workspaceOid);
        }

        if (promotedOid != null) {
            this.promotedOIDs.add(promotedOid);
        }

        this.type = changeType;
    }

    //================ Accessor Support ======================================

    /**
     * Print this change trace...
     */
    void print() {
        System.out.print("Workspace OID(s) = [");

        for (Iterator wsItr = workspaceOIDs.iterator(); wsItr.hasNext();) {
            System.out.print((OID) wsItr.next() + " , ");
        }

        System.out.print("] are '" + 
                         ReplacementRelationship.getTypeString(type) + 
                         "' from Promoted OID(s) = [");

        for (Iterator pItr = promotedOIDs.iterator(); pItr.hasNext();) {
            System.out.print((OID) pItr.next() + " , ");
        }

        System.out.print("] ");
        System.out.print(" superOID = [" + workspaceSuperFeatureOid + "]");

        if (obsoletedFeature != null) {
            System.out.println(" obsoletedFeature = " + 
                               obsoletedFeature.getOid());
        } else {
            System.out.println(" obsoletedFeature = NULL");
        }

        System.out.print(" obsoletedAlignment = (" + obsoletedAlignment + 
                         ")");
        System.out.println();
    }

    /**
     * Set the workspace for this changeTrace.
     * Package visibility.
     */
    void setWorkspace(Workspace aWorkspace) {
        this.workspace = aWorkspace;
    }

    /**
     * Set the replacement type...
     * Package visible.
     */
    void setChangeType(byte newChangeType) {
        this.type = newChangeType;
        this.postChangeTraceChange();
        workspace.postChangeTraceChanged(this);
    }

    /**
     * Notify of new change state...
     */
    void postChangeTraceChange() {
        OID anOid;
        Feature aFeature;
        Iterator oidItr;
        ReplacementRelationship theRepRel = this.getAsReplacementRelationship();

        // Notify on all the workspace entity(s)...
        for (oidItr = workspaceOIDs.iterator(); oidItr.hasNext();) {
            anOid = (OID) oidItr.next();
            aFeature = workspace.getFeatureForOid(anOid);

            if (aFeature != null) {
                aFeature.postWorkspaceReplacementStateChanged(aFeature, 
                                                              theRepRel);
            }
        }

        // Notify on all the promoted entity(s)...
        for (oidItr = promotedOIDs.iterator(); oidItr.hasNext();) {
            anOid = (OID) oidItr.next();
            aFeature = workspace.getFeatureForOid(anOid);

            if (aFeature != null) {
                aFeature.postPromotedReplacementStateChanged(aFeature, 
                                                             theRepRel);
            }
        }
    }

    /**
     * Get the replacment type...
     */
    public byte getChangeType() {
        return this.type;
    }

    /**
     * Returns the appropriate change trace state for the current change type.
     */
    private ChangeTraceState getChangeState() {
        return ChangeTrace.getChangeState(type);
    }

    /**
     * Returns the appropriate change trace state for the argument change type.
     */
    private static ChangeTraceState getChangeState(byte aType) {
        switch (aType) {
        case ReplacementRelationship.NEW:

            if (DEBUG_CHANGE_STATES) {
                System.out.println("CT: Using NEW State");
            }

            return ChangeTrace.NEW_STATE;

        case ReplacementRelationship.UNMODIFIED:

            if (DEBUG_CHANGE_STATES) {
                System.out.println("CT: Using UNMODIFIED State");
            }

            return ChangeTrace.UNMODIFIED_STATE;

        case ReplacementRelationship.MODIFIED:

            if (DEBUG_CHANGE_STATES) {
                System.out.println("CT: Using MODIFIED State");
            }

            return ChangeTrace.MODIFIED_STATE;

        case ReplacementRelationship.MERGE:

            if (DEBUG_CHANGE_STATES) {
                System.out.println("CT: Using MERGE State");
            }

            return ChangeTrace.MERGED_STATE;

        case ReplacementRelationship.SPLIT:

            if (DEBUG_CHANGE_STATES) {
                System.out.println("CT: Using SPLIT State");
            }

            return ChangeTrace.SPLIT_STATE;

        case ReplacementRelationship.OBSOLETE:

            if (DEBUG_CHANGE_STATES) {
                System.out.println("CT: Using OBSOLETE State");
            }

            return ChangeTrace.OBSOLETED_STATE;

        case ReplacementRelationship.DEEP_MOD:

            if (DEBUG_CHANGE_STATES) {
                System.out.println("CT: Using DEEP_MOD State");
            }

            return ChangeTrace.DEEPMODIFIED_STATE;

        default:
            System.out.println("CT: UNKNOWN State:" + aType);

            return null;
        }
    }

    /**
     * A convenience methods to check for a particular replacement type...
     */
    public boolean isChangeType(byte testType) {
        if (this.type == testType) {
            return true;
        }

        return false;
    }

    /**
     * Get the workspace entity...
     */
    public Set getWorkspaceOids() {
        return (Set) this.workspaceOIDs.clone();
    }

    /**
     * Add a workpace entity for this ChangeTrace
     * Package visible.
     */
    void addWorkspaceOid(OID anOid) {
        workspaceOIDs.add(anOid);
    }

    void setWorkspaceOids(OID[] oidArray) {
        workspaceOIDs.clear();

        for (int i = 0; i < oidArray.length; i++) {
            if (oidArray[i] != null) {
                workspaceOIDs.add(oidArray[i]);
            }
        }
    }

    /**
     * Remove a workpace entity for this ChangeTrace
     * Package visible.
     */
    void removeWorkspaceOid(OID anOid) {
        workspaceOIDs.remove(anOid);
    }

    void removeWorkspaceOids(OID[] oidArray) {
        for (int i = 0; i < oidArray.length; i++) {
            if (oidArray[i] != null) {
                workspaceOIDs.remove(oidArray[i]);
            }
        }
    }

    /**
     * Check if we have any workspace OIDs.
     */
    public boolean hasWorkspaceOids() {
        return !workspaceOIDs.isEmpty();
    }

    /**
     * Set the OID of the workspace super feature...
     * This is needed for the XML write and promotion process...
     */
    void setWorkspaceSuperFeatureOid(OID anOID) {
        workspaceSuperFeatureOid = anOID;
    }

    /**
     * Get the OID of the workspace super feature...
     */
    public OID getWorkspaceSuperFeatureOid() {
        return workspaceSuperFeatureOid;
    }

    /**
     * Get the obsoleted CuratedFeature...
     */
    public CuratedFeature getObsoletedCuratedFeature() {
        return obsoletedFeature;
    }

    /**
     * Get the Alignment of the obsoleted Feature.
     * @todo: This only works for singly aligned.
     */
    public Alignment getObsoletedAlignment() {
        return obsoletedAlignment;
    }

    /**
     * Package visible set-er.
     */
    void setObsoletedAlignment(Alignment anAlignment) {
        obsoletedAlignment = anAlignment;
        obsoletedAxisOid = anAlignment.getAxis().getOid();
    }

    /**
     * Re-constitute the Obsoleted Alignment with the given GenomeVersion.
     * This is called after the alignment has been serialized out and back in.
     */
    void reconstituteObsoletedAlignment(GenomeVersion aGenomeVersion) {
        if ((aGenomeVersion != null) && (obsoletedAlignment != null)) {
            if (obsoletedAxisOid != null) {
                GenomicEntity axisEntity = aGenomeVersion.getGenomicEntityForOid(
                                                   obsoletedAxisOid);

                if ((axisEntity != null) && (axisEntity instanceof Axis)) {
                    obsoletedAlignment.setAxisIfNull((Axis) axisEntity);
                } else {
                    System.out.println(
                            "ChangeTrace: Error unable to reconstitute Axis for OID:" + 
                            obsoletedAxisOid);
                }
            } else {
                System.out.println(
                        "ChangeTrace: Error trying to reconstitute obsoletedAlignment " + 
                        "with NULL obsoletedAxisOid");
            }
        }
    }

    /**
     * Get the promoted entities...
     * Public so make a clone.
     */
    public Set getPromotedOids() {
        return (Set) this.promotedOIDs.clone();
    }

    /**
     * Add a workpace entity for this ChangeTrace
     * Package visible.
     */
    void addPromotedOid(OID anOid) {
        promotedOIDs.add(anOid);
    }

    /**
     * Add a workpace entity for this ChangeTrace
     * Package visible.
     */
    void addAllPromotedOids(Set oidSet) {
        if (oidSet == null) {
            return;
        }

        for (Iterator itr = oidSet.iterator(); itr.hasNext();) {
            this.addPromotedOid((OID) itr.next());
        }
    }

    /**
     * Remove a promoted entity for this ChangeTrace
     * Package visible.
     */
    void removePromotedOid(OID anOid) {
        promotedOIDs.remove(anOid);
    }

    void setPromotedOids(OID[] oidArray) {
        promotedOIDs.clear();

        for (int i = 0; i < oidArray.length; i++) {
            if (oidArray[i] != null) {
                promotedOIDs.add(oidArray[i]);
            }
        }
    }

    /**
     * Check if have any promoted OIDs.
     */
    public boolean hasPromotedOids() {
        return !promotedOIDs.isEmpty();
    }

    //==================== Transition Methods =====================================

    /**
     * Modify State Transition
     */
    boolean willModify() {
        return this.getChangeState().willModify(this);
    }

    boolean modify() {
        return this.getChangeState().modify(this);
    }

    /**
     * Merge State Transition
     */
    boolean willMerge(ChangeTrace otherChangeTrace) {
        return this.getChangeState().willMerge(this, otherChangeTrace);
    }

    boolean merge(ChangeTrace theChangeTrace, ChangeTrace otherChangeTrace) {
        return this.getChangeState().merge(this, otherChangeTrace);
    }

    /**
     * Split State Transition
     */
    boolean willSplit() {
        return this.getChangeState().willSplit(this);
    }

    boolean split() {
        return this.getChangeState().split(this);
    }

    /**
     * Obsolete State Transition
     */
    boolean willObsolete() {
        return this.getChangeState().willObsolete(this);
    }

    boolean obsolete(CuratedFeature theObsoletedFeature) {
        this.obsoletedFeature = theObsoletedFeature;

        return this.getChangeState().obsolete(this);
    }

    //==================== Private utility Methods ================================

    /**
     * Utility to take all the GenomicEntity(s) from one Set and put the OID in another.
     */
    private void fillOIDSetFromGESet(Set genomicEntitySet, Set oidSet) {
        if ((genomicEntitySet == null) || (oidSet == null)) {
            return;
        }

        GenomicEntity aGenomicEntity;

        for (Iterator geItr = genomicEntitySet.iterator(); geItr.hasNext();) {
            aGenomicEntity = (GenomicEntity) geItr.next();
            oidSet.add(aGenomicEntity.getOid());
        }
    }

    /**
     * Get ReplacementRelationship form...
     */
    ReplacementRelationship getAsReplacementRelationship() {
        return new ReplacementRelationship(
                       ReplacementRelationship.getTypeString(type), 
                       (OID[]) promotedOIDs.toArray(new OID[0]));
    }

    //=========== Inner Classes ==================================================

    /**
     * Inner class that is used to observer the Features...
     */
    static interface ChangeTraceState {
        /**
         * Modify State Transition
         */
        boolean willModify(ChangeTrace theChangeTrace);

        boolean modify(ChangeTrace theChangeTrace);

        /**
         * Merge State Transition
         */
        boolean willMerge(ChangeTrace theChangeTrace, 
                          ChangeTrace otherChangeTrace);

        boolean merge(ChangeTrace theChangeTrace, ChangeTrace otherChangeTrace);

        /**
         * Split State Transition
         */
        boolean willSplit(ChangeTrace theChangeTrace);

        boolean split(ChangeTrace theChangeTrace);

        /**
         * Obsolete State Transition
         */
        boolean willObsolete(ChangeTrace theChangeTrace);

        boolean obsolete(ChangeTrace theChangeTrace);
    }

    /**
     * Inner class that is used to observer the Features...
     */
    static abstract class AbstractChangeTraceState implements ChangeTraceState {
        /**
         * Modify State Transition
         */
        public boolean willModify(ChangeTrace theChangeTrace) {
            return true;
        }

        public boolean modify(ChangeTrace theChangeTrace) {
            if (ChangeTrace.DEBUG_CHANGE_STATES) {
                System.out.println("ChangeTraceState:" + 
                                   this.getClass().getName() + 
                                   " <modify> transition called.");
            }

            return this.willModify(theChangeTrace);
        }

        /**
         * Merge State Transition
         */
        public boolean willMerge(ChangeTrace theChangeTrace, 
                                 ChangeTrace otherChangeTrace) {
            return true;
        }

        public boolean merge(ChangeTrace theChangeTrace, 
                             ChangeTrace otherChangeTrace) {
            if (ChangeTrace.DEBUG_CHANGE_STATES) {
                System.out.println("ChangeTraceState:" + 
                                   this.getClass().getName() + 
                                   " <merge> transition called.");
            }

            return this.willMerge(theChangeTrace, otherChangeTrace);
        }

        /**
         * Split State Transition
         */
        public boolean willSplit(ChangeTrace theChangeTrace) {
            return true;
        }

        public boolean split(ChangeTrace theChangeTrace) {
            if (ChangeTrace.DEBUG_CHANGE_STATES) {
                System.out.println("ChangeTraceState:" + 
                                   this.getClass().getName() + 
                                   " <split> transition called.");
            }

            return this.willSplit(theChangeTrace);
        }

        /**
         * Obsolete State Transition
         */
        public boolean willObsolete(ChangeTrace theChangeTrace) {
            return true;
        }

        public boolean obsolete(ChangeTrace theChangeTrace) {
            if (ChangeTrace.DEBUG_CHANGE_STATES) {
                System.out.println("ChangeTraceState:" + 
                                   this.getClass().getName() + 
                                   " <obsolete> transition called.");
            }

            return this.willObsolete(theChangeTrace);
        }
    }

    /**
     * The state of being "New".
     * This is new and does not have any promoted features that it is a change of.
     * Concrete State subclass in State Pattern.
     *
     * Rules governing this state;
     *
     * Transitions that are allowed & resulting state:
     * - modify(); results in NewState, no change.
     * - merge(); is only allowed with other "new" entities and results in NewState.
     * - split(); results in two in NewState.
     *
     * Transitions that are NOT allowed:
     * - obsolete(); is not allowed because it implies that this replaces a promoted item.
     */
    static class NewState extends AbstractChangeTraceState {
        /**
         * Merge State Transition
         * is only allowed with other "new" entities and results in NewState.
         */
        public boolean willMerge(ChangeTrace theChangeTrace, 
                                 ChangeTrace otherChangeTrace) {
            return (theChangeTrace.isChangeType(ReplacementRelationship.NEW) && 
                   otherChangeTrace.isChangeType(ReplacementRelationship.NEW));
        }

        /**
         * Obsolete State Transition
         * ...does not make sence from the new state.
         */
        public boolean willObsolete(ChangeTrace theChangeTrace) {
            return false;
        }
    }

    /**
     * The state of being "Unmodified"
     * This is feature replaces a promoted feature, but no change has been made to this.
     * Concrete State subclass in State Pattern
     *
     * Rules governing this state;
     *
     * Transitions that are allowed & resulting state:
     * - modify(); results in ModifiedState.
     * - merge(); is only allowed with other "non-new" entities and results in NewState.
     * - split(); results in two in SplitState.
     * - obsolete(); results in ObsoletedState.
     *
     * Transitions that are NOT allowed:
     */
    static class UnmodifiedState extends AbstractChangeTraceState {
        /**
         * Modify State Transition...
         */
        public boolean modify(ChangeTrace theChangeTrace) {
            if (ChangeTrace.DEBUG_CHANGE_STATES) {
                System.out.println("UnmodifiedState.modify(); theChangeTrace=");
                theChangeTrace.print();
            }

            theChangeTrace.setChangeType(ReplacementRelationship.MODIFIED);

            return true;
        }

        /**
         * Merge State Transition
         */
        public boolean merge(ChangeTrace theChangeTrace, 
                             ChangeTrace otherChangeTrace) {
            if (ChangeTrace.DEBUG_CHANGE_STATES) {
                System.out.println("UnmodifiedState.merge(); theChangeTrace=");
                theChangeTrace.print();
                otherChangeTrace.print();
            }


            // Merge the promoted items... to the first argument...
            theChangeTrace.setChangeType(ReplacementRelationship.MERGE);

            return true;
        }

        /**
         * Split State Transition
         */
        public boolean split(ChangeTrace theChangeTrace) {
            if (ChangeTrace.DEBUG_CHANGE_STATES) {
                System.out.println("UnmodifiedState.split(); theChangeTrace=");
                theChangeTrace.print();
            }


            // Need to generate a new item...
            theChangeTrace.setChangeType(ReplacementRelationship.SPLIT);

            return true;
        }

        /**
         * Obsolete State Transition
         */
        public boolean obsolete(ChangeTrace theChangeTrace) {
            if (ChangeTrace.DEBUG_CHANGE_STATES) {
                System.out.println(
                        "UnmodifiedState.obsolete(); theChangeTrace=");
                theChangeTrace.print();
            }


            // Need to obsolete this item...
            theChangeTrace.setChangeType(ReplacementRelationship.OBSOLETE);

            return true;
        }
    }

    /**
     * The state of being "Modified".
     * This has been changed in some why from the
     * Properties, alignments, add / remove sub-features or such may have been changed.
     * Concrete State subclass in State Pattern.
     *
     * Rules governing this state;
     *
     * Transitions that are allowed & resulting state:
     * - modify(); results in ModifiedState, no change.
     * - merge(); is only allowed with other "non-new" entities and results in NewState.
     * - split(); results in two in SplitState.
     * - obsolete(); results in ObsoletedState.
     *
     * Transitions that are NOT allowed:
     */
    static class ModifiedState extends AbstractChangeTraceState {
        /**
         * Modify State Transition
         * Default (inherited), allow with no transition...
         */
        /**
         * Merge State Transition
         */
        public boolean merge(ChangeTrace theChangeTrace, 
                             ChangeTrace otherChangeTrace) {
            if (ChangeTrace.DEBUG_CHANGE_STATES) {
                System.out.println("ModifiedState:" + 
                                   this.getClass().getName() + 
                                   " <merge> transition called.");
            }


            // Merge the promoted items... to the first argument...
            theChangeTrace.setChangeType(ReplacementRelationship.MERGE);

            return true;
        }

        /**
         * Split State Transition
         */
        public boolean split(ChangeTrace theChangeTrace) {
            // Need to generate a new item...
            theChangeTrace.setChangeType(ReplacementRelationship.SPLIT);

            return true;
        }

        /**
         * Obsolete State Transition
         */
        public boolean obsolete(ChangeTrace theChangeTrace) {
            // Need to obsolete this item...
            theChangeTrace.setChangeType(ReplacementRelationship.OBSOLETE);

            return true;
        }
    }

    /**
     * The state of being "DeepModified".
     * This is the state where the exact modifications to this entity and it's
     * sub-features could not be tracked or expressed.
     * Concrete State subclass in State Pattern.
     *
     * Rules governing this state;
     *
     * Transitions that are allowed & resulting state:
     * - modify(); results in DeepModifiedState.
     * - obsolete(); results in ObsoletedState.
     *
     * Transitions that are NOT allowed:
     * - merge(); is only allowed with other "non-new" entities and results in NewState.
     * - split(); results in two in SplitState.
     */
    static class DeepModifiedState extends AbstractChangeTraceState {
        /**
         * Merge State Transition
         */
        public boolean willMerge(ChangeTrace theChangeTrace, 
                                 ChangeTrace otherChangeTrace) {
            return false;
        }

        /**
         * Split State Transition
         */
        public boolean willSplit(ChangeTrace theChangeTrace) {
            return false;
        }

        /**
         * Obsolete State Transition
         */
        public boolean obsolete(ChangeTrace theChangeTrace) {
            // Need to obsolete this item...
            theChangeTrace.setChangeType(ReplacementRelationship.OBSOLETE);

            return true;
        }
    }

    /**
     * The state of being "Merged".
     * This is the state where this workspace entity is the result of merging
     * multiple copies of promoted entitys.  This one workspace entity is intended
     * to replace 2 or more promoted entities.
     * Concrete State subclass in State Pattern.
     *
     * Rules governing this state;
     *
     * Transitions that are allowed & resulting state:
     * - modify(); results in MergedState, no change.
     * - obsolete(); results in ObsoletedState.
     * - merge(); is only allowed with other "non-new" entities and results in NewState.
     *
     * Transitions that are NOT allowed:
     * - split(); Splits are not allowed on a merged entity.
     */
    static class MergedState extends AbstractChangeTraceState {
    }

    /**
     * The state of being "Split".
     * This is the state where this workspace entity is the result of splitting
     * a copies of promoted entity.  Two or more workspace entities are intended
     * to replace 1 promoted entity.
     * Concrete State subclass in State Pattern.
     *
     * Rules governing this state;
     *
     * Transitions that are allowed & resulting state:
     * - modify(); results in SplitState, no change.
     * - obsolete(); results in ObsoletedState.
     * - split(); results in multiple Splits.
     *
     * Transitions that are NOT allowed:
     * - merge(); Merges are not allowed on a Split entity.
     */
    static class SplitState extends AbstractChangeTraceState {
    }

    /**
     * The state of being "Obsoleted".
     * This is the state where this workspace entity is intended to delete
     * a promoted entity (without a replacement) on promotion
     * Concrete State subclass in State Pattern.
     *
     * Rules governing this state;
     *
     * Transitions that are allowed & resulting state:
     *
     * Transitions that are NOT allowed:
     * - modify(); results in SplitState.
     * - obsolete(); results in ObsoletedState.
     * - split(); results in multiple Splits.
     * - merge(); Merges are not allowed on a Split entity.
     */
    static class ObsoletedState extends AbstractChangeTraceState {
        /**
         * Modify State Transition
         */
        public boolean willModify(ChangeTrace theChangeTrace) {
            return false;
        }

        /**
         * Merge State Transition
         */
        public boolean willMerge(ChangeTrace theChangeTrace, 
                                 ChangeTrace otherChangeTrace) {
            return false;
        }

        /**
         * Split State Transition
         */
        public boolean willSplit(ChangeTrace theChangeTrace) {
            return false;
        }

        /**
         * Obsolete State Transition
         */
        public boolean willObsolete(ChangeTrace theChangeTrace) {
            return false;
        }
    }
}