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

import api.entity_model.access.filter.AlignmentCollectionFilter;
import api.entity_model.access.filter.FiltrationDevice;
import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.fundtype.SingleAlignmentSingleAxis;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.GenomicEntityComment;
import api.stub.data.GenomicProperty;
import api.stub.data.NoData;
import api.stub.data.OID;
import api.stub.data.PromotionReport;
import api.stub.data.ReplacementRelationship;
import api.stub.data.Util;
import api.stub.geometry.Range;

import shared.db.rdbms.RdbmsAccess;
import shared.util.AccessionClient;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Title:        Genome Browser<p>
 * Description:  <p>
 * @author Jay T. Schira
 * @version $Id$
 *
 * Curated Features are editable and promotable.
 * See the BizObj CuratedFeature for strong behavioral differences.
 */
public abstract class CuratedFeature extends Feature
    implements SingleAlignmentSingleAxis {
    // A manual serial version UID... need to update this when we affect the interface.
    private static final long serialVersionUID = 1; // Unique identifier for a Feature
    private static final int PARENT_CHILD_ASSOCIATION = 0;
    private static final int EVIDENCE_ASSOCIATION = 2;
    private static final int CURATED_GROUP_TAG_ID = 25;
    private static final String WRONG_TRAN_ACCESSION_NUMBER = "T0";
    private static final String WRONG_GENE_ACCESSION_NUMBER = "G0";
    private static final String NULL_TRAN_ACCESSION_NUMBER = "Tnull";
    private static final String NULL_GENE_ACCESSION_NUMBER = "Gnull";
    private static final String WORKSPACE_TRAN_PREFIX = "WT";
    private static final String WORKSPACE_GENE_PREFIX = "WG";

    // Used for efficiency during promotion to make sure that promote instructions
    // to obsolete the same OID are not issued. Splitting a transcript can
    // result in the both new transcripts from the split trying to obsolete the
    // same old transcript.
    transient static List alreadyObsoletedOIDS = new ArrayList();

    // During the promotion process the OID of an object is morphed between
    // that of a SCRATCH oid and a real internal oid. This attribute of
    // CuratedFeature keeps track of that new OID during promotion.
    transient private OID promotedOID = null;

    // Also during the promotion process Curated Features need access to
    // basic RDBMS facilities to prepare statements and determine
    // accession numbers
    transient private RdbmsAccess rdbmsAccess = null;

    // Once more during the promotion process Curated Features need access to
    // an object that can determine if the promoted feature has been
    // reviewed and if so by whom
    transient private ReviewAuthority reviewAuth = null;

    // Used to determine which data layer obsoleted features will be moved
    // to and which data layers new features (or replacements for the obsoleted
    // feature will be moved to
    transient int obsoleteDataLayerId;
    transient int promoteDataLayerId;

    // Used to track against which Genome Version promotion is proceeding
    transient GenomeVersion promoteGenomeVer = null;

    //===================Constructor support============================
    public CuratedFeature(OID oid, String displayName, EntityType type, 
                          String discoveryEnvironment)
                   throws InvalidFeatureStructureException {
        this(oid, displayName, type, discoveryEnvironment, null, null, 
             FeatureDisplayPriority.DEFAULT_PRIORITY);
    }

    public CuratedFeature(OID oid, String displayName, EntityType type, 
                          String discoveryEnvironment, 
                          FacadeManagerBase readFacadeManager, 
                          Feature superFeature, byte displayPriority)
                   throws InvalidFeatureStructureException {
        super(oid, displayName, type, discoveryEnvironment, readFacadeManager, 
              superFeature, displayPriority);
    }

    //== Replacement / Change Trace Methods =========================================

    /**
     * Public method used to get the ReplacementRelationships for this Feature.
     * This method will try to call the Workspace to see if it has one.
     *
     * Returns a ReplacementRelationship for this Feature.
     */
    public ReplacementRelationship getReplacementRelationship() {
        ReplacementRelationship replacementRelationship = null;

        if (!this.getGenomeVersion().hasWorkspace()) {
            return replacementRelationship; //do not create one
        }

        Workspace myWorkspace = this.getGenomeVersion().getWorkspace();

        if (myWorkspace != null) {
            replacementRelationship = myWorkspace.getReplacementRelationForWorkspaceFeature(
                                              this);
        }

        return replacementRelationship;
    }

    /**
     * Load a replacement relationship from the facade...
     * Package level visibility...
     */
    ReplacementRelationship getReplacementRelationshipFromFacade() {
        ReplacementRelationship replacementRelationship = null;

        // try to get it from the workspace (the authoritative source)...
        FeatureFacade myFeatureFacade = this.getFeatureFacade();

        if (myFeatureFacade != null) {
            try {
                // Get the evedence from the facade as an array...
                replacementRelationship = myFeatureFacade.retrieveReplacedFeatures(
                                                  this.getOid(), 
                                                  this.getGenomeVersion()
                                                      .getAssemblyVersion());

                // Should tell the Workspace to update with my data...
            } catch (NoData ndEx) {
                ; // Nothing to do just allow mechaism to show no evidence
            }
        }

        return replacementRelationship;
    }

    /**
     * Get the Replacement Type of this curated feature.
     */
    public String getReplacementRelationshipType() {
        ReplacementRelationship replacementRelationship = 
                this.getReplacementRelationship();

        if (replacementRelationship == null) {
            replacementRelationship = new ReplacementRelationship(new OID[0]);
        }

        if (replacementRelationship == null) {
            return null;
        }

        return replacementRelationship.getReplacementType();
    }

    /**
     * Check to make sure that the argument is a valid feature to be assigned as
     * being replaced by this PI.
     * Currently, this simply means checking the class type.
     * Could also check the name space.
     * Subclasses can also over-ride this method to add further criteria.
     */
    public boolean isValidReplacementOfPromoted(Feature promotedFeature) {
        // This is not always the case...
        // StartCodonFeatPI can be replaced by TranslationStartCodonFeatPI
        // and vice versa... there may be other examples...
        // if (this.getClass() != replacedPI.getClass()) return false;
        // For now... just check scratch versus non-scratch...
        return (this.isScratch() && !promotedFeature.isScratch());
    }

    /**
     * Check if this feature is a scratch feature that replaces a promoted feature...
     */
    public boolean isScratchReplacingPromoted() {
        ReplacementRelationship theRepRel = this.getReplacementRelationship();

        if (!this.isScratch() || (theRepRel == null)) {
            return false;
        }

        return !theRepRel.isReplacementType(ReplacementRelationship.NEW);
    }

    /**
     * Check if this feature is a promoted feature that is replaced by a scratch feature...
     */
    public boolean isPromotedReplacedByScratch() {
        if (this.isScratch()) {
            return false;
        }

        if (!this.getGenomeVersion().hasWorkspace()) {
            return false;
        }

        Workspace myWorkspace = this.getGenomeVersion().getWorkspace();

        if (myWorkspace == null) {
            return false;
        }

        return myWorkspace.isPromotedReplacedByScratch(this);
    }

    /**
     * Is this Feature considered obsolete either directly (a scratch feature that
     * has been set to "obsolete"), or indirectly (a promoted feature that is replaced
     * by a scratch feature that is "obsolete").
     */
    public boolean isObsoleted() {
        return this.isReplacementType(ReplacementRelationship.OBSOLETE);
    }

    protected int unloadIfPossible(Alignment alignment, boolean checkForRoot) {
        if ((checkForRoot && this.hasSuperFeature())) {
            return 0;
        }

        ((CuratedFeatureMutator) this.getMutator()).removeSubStructureAndRemoveAlignments(
                true);
        ;

        // First unload this feature's sub-features...
        // get a clone of the sub-features...
        Collection subFeatures = this.getSubStructure();

        if (subFeatures == null) {
            return 1;
        }

        Feature subFeature;
        int counter = 1;
        Set subFeatureAlignments;

        if (!(alignment instanceof GeometricAlignment)) {
            return counter;
        }

        for (Iterator subFeatItr = subFeatures.iterator();
             subFeatItr.hasNext();) {
            subFeature = (Feature) subFeatItr.next();

            subFeatureAlignments = subFeature.getAlignmentsToAxis(
                                           alignment.getAxis());

            if (subFeatureAlignments.size() == 0) {
                continue;
            }

            if (subFeatureAlignments.size() == 1) {
                counter += subFeature.unloadIfPossible(
                                   (Alignment) subFeatureAlignments.iterator()
                                                                   .next(), 
                                   false);
            } else {
                System.out.println("Entered range of concern code");

                Range rangeOfConcern = ((GeometricAlignment) alignment).getRangeOnAxis();
                AlignmentCollectionFilter filter = AlignmentCollectionFilter.createAlignmentCollectionFilter(
                                                           rangeOfConcern);
                List filteredAlignments = FiltrationDevice.getDevice()
                                                          .executeAlignmentFilter(subFeatureAlignments, 
                                                                                  filter);

                if (filteredAlignments.size() == 0) {
                    throw new IllegalStateException(
                            "When unloading a subFeature," + 
                            " the subFeature was not found within the range of it's parent.");
                }

                if (filteredAlignments.size() > 1) {
                    throw new IllegalStateException(
                            "When unloading a subFeature," + 
                            " the subFeature was found multiple times within the range of it's parent.");
                }

                counter += subFeature.unloadIfPossible(
                                   (Alignment) subFeatureAlignments.iterator()
                                                                   .next(), 
                                   false);
            }
        }

        return counter;
    }

    /**
     * Is this Feature a Promoted Feature that has been replaced by an
     * Obsoleted Workspace feature.
     */
    public boolean isObsoletedByWorkspace() {
        ReplacementRelationship replacementRelationship = null;

        if (!this.getGenomeVersion().hasWorkspace()) {
            return false; //Do not create one here
        }

        Workspace myWorkspace = this.getGenomeVersion().getWorkspace();

        if (myWorkspace != null) {
            replacementRelationship = myWorkspace.getReplacementRelationForPromotedOid(
                                              this.getOid());

            if ((replacementRelationship != null) && 
                    (replacementRelationship.isReplacementType(
                            ReplacementRelationship.OBSOLETE))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check to see if this CuratedFeature is in particular replacement state.
     */
    public boolean isReplacementType(byte testReplacementType) {
        // If this is a scratch... return if it is...
        if (this.getReplacementRelationship() == null) {
            return false;
        } else {
            return this.getReplacementRelationship()
                       .isReplacementType(testReplacementType);
        }
    }

    //=================== Clone support============================

    /**
     * Clone this Feature and return an EXACT copy...
     * - OID should be the same (the clones are never put in the GenomeVersion so
     * duplicate OIDs are not a problem)
     * - Subfeature & SuperFeatures should map to the cloned equivalents.
     * - Evidence is clones as an OID reference.
     * - Alignments are cloned, though the cloned alignments are "DRAFT" and not
     * accepted or seen by the Axis
     * - Comments are cloned exactly (those loaded)
     * - Properties are cloned exactly (those loaded)
     * - Replacement Type and Relationship should be the same
     * - and finally the load state of everything should be the same (ie don't load
     * anything that isn't loaded).
     *
     * All references that cross Gene boundaries must be done with OIDs.
     *
     * @todo: UNDO - Clone feature Structure prototype this puppy.
     */
    protected CuratedFeature cloneFeatureStructure(Axis axis) {
        /*
        CuratedFeature clonedFeature = super.cloneFeatureStructure(Axis axis);
        clonedFeature.xxx = this.xxx.clone();
        clonedFeature.yyy = this.yyy.clone();
        clonedFeature.zzz = this.zzz.clone();
        return clonedFeature;
        */
        return this;
    }

    //===================Alignment support============================

    /**
     * @return Collection of Alignments to the one Axis this entity is aligned to.
     * All Alignments returned in the collection are guaranteed to be to the same
     * axis.
     * Support for MultipleAlignmentsSingleAxis interface.
     */
    public Set getAllAlignmentsToOnlyAxis() {
        return super.getAllAlignmentsToOnlyAxis();
    }

    /**
     * @return Collection of GeometricAlignments to the one Axis this entity is aligned to.
     * All Alignments returned in the collection are guaranteed to be to the same
     * axis and are guaranteed to be Geometric alignments.
     * Support for MultipleAlignmentsSingleAxis interface.
     */
    public Set getAllGeometricAlignmentsToOnlyAxis() {
        return super.getAllGeometricAlignmentsToOnlyAxis();
    }

    /**
     * @return The single Alignment to the one Axis this entity is aligned to.
     * Support for SingleAlignmentSingleAxis interface.
     */
    public Alignment getOnlyAlignmentToOnlyAxis() {
        return super.getOnlyAlignmentToOnlyAxis();
    }

    /**
     * @return The single GeometricAlignment to the one Axis this entity is aligned to.
     * If the alignment is NOT a GeometricAlignment, this will return null.
     * Support for SingleAlignmentSingleAxis interface.
     */
    public GeometricAlignment getOnlyGeometricAlignmentToOnlyAxis() {
        return super.getOnlyGeometricAlignmentToOnlyAxis();
    }

    //===================Visitor support============================

    /**
    * Accepts visitors that implement GenomicEntityVisitor. acceptVisitorForSelf
    * is specialized by every subclass of GenomicEntity to call the appropriate
    * "visit...(...)" function on the passed visitor.
    * @param theVisitor the visitor.
    */
    public void acceptVisitorForSelf(GenomicEntityVisitor theVisitor) {
        try {
            theVisitor.visitCuratedFeature(this);
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    //===================Promotion support============================

    /**
     * Checks that the CuratedFeature is currently in a state that could be
     * promoted to persistent storage in the context of a given axis
     *
     * @param ver - the OID of the version
     * (Species, Assembly Version and Database)
     * against which this feature should be assessed for it's promotability
     *
     * @param axis - the axis to whom the curated feature is aligned and in
     * whose geometry (alignmetns) the curated feature should be assessed for
     * it's promotability
     *
     * @return an instance of PromotionReport that indicates whether the
     * object was in a promotable state.
     */
    public PromotionReport checkPromotableOn(GenomeVersion ver, Axis axis) {
        // Remember the genome version for when database checks are added to
        // check promotable code
        this.promoteGenomeVer = ver;

        PromotionReport report = new PromotionReport(true, 
                                                     "General checks " + 
                                                     this.getOid());

        // First check the replacement type of the object, if it is
        // to be obsoleted then no need to proceed as we dont really care
        // what state it is in other than that is has a replaces relationship
        // with some other feature
        ReplacementRelationship replRel = this.getReplacementRelationship();

        if (replRel.getReplacementTypeAsByte() == ReplacementRelationship.OBSOLETE) {
            int numObjsReplaced = replRel.getNumberOfReplacementOIDs();

            if (numObjsReplaced >= 1) {
                return report;
            } else {
                report.addFatalError("Feature" + this.getOid() + 
                                     " is marked as obsoleted " + 
                                     " but does not identify which feature to obsolete through its replaced relationship");
            }
        } else {
            // If the feature is NOT itself obsolete then make sure it does not have
            // any parent that IS obsolete as this would make for an inconsistent
            // model
            CuratedFeature currentFeature = this;
            CuratedFeature parentFeature = null;
            ReplacementRelationship parentReplacementRel = null;

            while (currentFeature.hasSuperFeature()) {
                parentFeature = (CuratedFeature) currentFeature.getSuperFeature();
                parentReplacementRel = parentFeature.getReplacementRelationship();

                if (parentReplacementRel.getReplacementTypeAsByte() == ReplacementRelationship.OBSOLETE) {
                    report.addFatalError("Feature" + currentFeature.getOid() + 
                                         " has a replacement relationship of  " + 
                                         currentFeature.getReplacementRelationship()
                                                       .getReplacementType() + 
                                         " but has a parent" + 
                                         parentFeature.getOid() + 
                                         " that has a " + 
                                         " replacement relationship of OBSOLETE. This is not a valid " + 
                                         " state between child and parent feature");
                }

                currentFeature = parentFeature;
            }
        }

        // Next make sure the feature only has one alignment to the
        // axis in question
        if (this.getAlignmentsToAxis(axis).size() != 1) {
            report.addFatalError("Feature " + this.getOid() + " has " + 
                                 this.getAlignmentsToAxis(axis).size() + 
                                 " alignments to axis " + axis.getOid() + 
                                 ".Promotion only allows a single alignment for any type " + 
                                 " of feature");
        }

        // Template pattern override point for subclasses to provide thier
        // specific steps to check promotablility.
        return doCheckPromotableOnAxis(axis, report);
    }

    /**
     * Template pattern hook in point for subclasses. Allows a subclass to
     * provide specific checks when determining if it is a promotable state.
     * @param ver is the genome version within which the check for promotability
     * should be made
     * @param axis the genomic axis against which the feature should be aligned
     * and whose alignment information should be verified as promotable
     * @
     */
    abstract protected PromotionReport doCheckPromotableOnAxis(Axis axis, 
                                                               PromotionReport report);

    /**
     * Issues the appropriate instructions to make a persistent record of this
     * feature into an RDBMS. Takes into account obsoletion of older copies of
     * this feature and deals with the generation of new unique identifiers
     * for the replacing version. IF this feature has sub features it will
     * coordinate the promotion of those children and the representation of
     * the relationships between them as well. Note however that the children
     * are ultimately responsible for generating there own instructions to the
     * RDBMS, the parent only instructs them to do so at the appropriate stage
     * in promotion.
     *
     * @param ver is the Genome Version (Species, Assembly Version and Database)
     * into which this feature should store itself.
     *
     * @paran axis the GenomicAxis against which this feature
     *
     * @param parentReplacesDirective a parents modification may force modification
     * of a child due to semantic constraints that must be enforced between
     * them. A parent can provide a directive to the child for this purpose
     * during promotion.
     *
     * @param rdbmsAccess the CuratedFeature needs basic rdbms services to prepare
     * promote instructions especially if it decides to use prepared statements
     * or other such facilities. A CuratedFeature cannot promote without the help
     * of an object that implements the RdbmsAccess interface.
     *
     * @param reviewAuth an interface that can answer the question of whether
     * the person requesting the promotion of a feature to a specific database
     * actually has the right to do so.
     *
     * @param obsoleteDataLayerId obsoletion of a feature is controlled through
     * the concept of data layers
     * @return an instance of PromotionReport that indicates whether the
     * object was in the correct state to prepare the promotion instructions.
     */
    public PromotionReport buildPromoteInstructions(GenomeVersion ver, 
                                                    Axis axis, 
                                                    String parentReplacesDirective, 
                                                    RdbmsAccess rdbmsAccess, 
                                                    ReviewAuthority reviewAuth, 
                                                    int obsoleteDataLayerId, 
                                                    int promoteDataLayerId)
                                             throws SQLException {
        // Save the genome version identity
        this.promoteGenomeVer = ver;


        // Save the data layer information
        this.obsoleteDataLayerId = obsoleteDataLayerId;
        this.promoteDataLayerId = promoteDataLayerId;


        // Convert this CuratedFeature to start using its new promoted
        // OID.
        convertToPromotedOID();


        // Remember the rdbmsAcess instance so this object can access it easily
        // without having to pass it as an argument to internal methods
        this.rdbmsAccess = rdbmsAccess;


        // Remember the review authority so this object can determine if it
        // needs to mark the promotion work as reviewed
        this.reviewAuth = reviewAuth;

        // A NULL parent indicates that this feature had no parent so it is the
        // start in the chain of determining parentReplacesDirective
        String parentDirective = parentReplacesDirective;
        String myDirective = this.getReplacementRelationshipType();

        if ((!this.hasSuperFeature()) && 
                (myDirective.equals(ReplacementRelationship.TYPE_UNMODIFIED)) && 
                (anyChildModified(this.getPromotionGenomeVersion()
                                      .getAssemblyVersion()))) {
            // The initial approach is that if anything about a gene or its children
            // changes then make a completely new copy the only sticking point
            // being whether a new accession number is required or not for those
            // features that have accession numbers. This decision is left to
            // each child.
            parentDirective = ReplacementRelationship.TYPE_MODIFIED;
        }

        return doBuildPromoteInstructions(axis, parentDirective);
    }

    //****************************************
    //*  Protected methods
    //****************************************

    /**
     * Templete pattern
     * Sub-Classes should overide this to set the proper mutator
     */
    protected GenomicEntityMutator constructMyMutator() {
        return new CuratedFeatureMutator();
    }

    /**
     *
     */
    private void addAlignmentToAxisPromoteInstruction(Axis axis, 
                                                      GeometricAlignment alignment, 
                                                      String errorOnFailure, 
                                                      boolean mandatory)
                                               throws SQLException {
        // NOTE: Orientation values are -1 (reverse), 0 (unknown) or 1 (forward)
        int orientation = 0;

        // Coordiantes are stored in the database with start before end plus
        // an orientation indicator.
        int startOnAxis;

        // Coordiantes are stored in the database with start before end plus
        // an orientation indicator.
        int endOnAxis;

        // Coordiantes are stored in the database with start before end plus
        // an orientation indicator.
        int startOnEntity;

        // Coordiantes are stored in the database with start before end plus
        // an orientation indicator.
        int endOnEntity;
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        BigDecimal currentTimeBigDec = new BigDecimal(currentTimeSeconds);

        // WARNING: This call MUST come before bogusAlignmentAdjustmentForPromotion
        // call below due to backward compatibility issues. We need to use an
        // alignment with a length to determine orientation correctly BUT
        // want to use an alignment of zero length in some cases when
        // written to the database
        Range.Orientation rangeOrientation = alignment.getRangeOnAxis()
                                                      .getOrientation();

        if (rangeOrientation == Range.REVERSE_ORIENTATION) {
            orientation = -1;
        } else {
            orientation = 1;
        }

        // Allow sublcasses to adjust the alignment information before
        // promotion proceeds to allow for some backward compatability issues
        // with .gbw files needing a length to determine orientation
        GeometricAlignment promoteAlignment = bogusAlignmentAdjustmentForPromotion(
                                                      alignment);

        // Range is a mathematical vector like representation of the alignment on
        // axis which means that start and end may not always be in the same order.
        // However, for consistency the database stores start ALWAYS before end
        // with a separate orientation indicator. Inspect the values returned
        // from the getRangeOnAxis call to alignment and adjust accordingly
        Range rangeOnAxis = promoteAlignment.getRangeOnAxis();
        int rangeStart = rangeOnAxis.getStart();
        int rangeEnd = rangeOnAxis.getEnd();
        startOnAxis = Math.min(rangeStart, rangeEnd);
        endOnAxis = Math.max(rangeStart, rangeEnd);


        // Assuming that startOnEntity will always be before end on entity
        // so can ignore the question of orientation. Additionally expecting
        // that startOnEntity will always be 0. Also assuming all alignments
        // are complete alignments, that is, if the range on the axis is
        // 100 then the entity was 100 in length and aligned exactly at that
        // position on the axis, thus can use the magnitude of the range on the
        // axis as the endOnEntity value
        startOnEntity = 0;
        endOnEntity = rangeOnAxis.getMagnitude();

        CallableStatement cs = null;

        try {
            cs = this.rdbmsAccess.prepareCallableStatement(
                         "{ ? = call api_writeback_pkg.add_feature_to_axis_alignment(?,?,?,?,?,?,?,?,?,?,?) }", 
                         getPromoteDatasourceName());

            int stmtIndex = 2;
            cs.setBigDecimal(stmtIndex++, this.getPromoteOID().toBigDecimal());
            cs.setInt(stmtIndex++, this.getEntityType().value());
            cs.setBigDecimal(stmtIndex++, this.getPromoteAssemblyVer());
            cs.setBigDecimal(stmtIndex++, currentTimeBigDec);
            cs.setBigDecimal(stmtIndex++, axis.getOid().toBigDecimal());
            cs.setInt(stmtIndex++, startOnAxis);
            cs.setInt(stmtIndex++, endOnAxis);
            cs.setInt(stmtIndex++, startOnEntity);
            cs.setInt(stmtIndex++, endOnEntity);
            cs.setInt(stmtIndex++, orientation);
            cs.setInt(stmtIndex++, 1);


            //cs.setString(stmtIndex++,this.rdbmsAccess.getRdbmsSecurityId());
            this.rdbmsAccess.executeUpdate(cs, getPromoteDatasourceName());
        } catch (Exception sqlEx) {
            ModelMgr.getModelMgr().handleException(sqlEx);

            // Rethrow so that caller gets indication of failure immediately
            throw new SQLException(sqlEx.getMessage());
        } finally {
            this.rdbmsAccess.executeComplete(cs, getPromoteDatasourceName());
        }
    }

    /**
     * Returns the name of the datasource into which, promotion should occur.
     * A feature needs to know into which of the possible set of datasources a
     * server could be configred for, it should promote itself.
     */
    protected String getPromoteDatasourceName() {
        return promoteGenomeVer.getDatasourceForVersion();
    }

    /**
     * Returns the version of the genome into which, promotion should occur.
     * A feature needs to know ito which of the possible versions of a genome
     * it should attempt to update itself into.
     */
    protected BigDecimal getPromoteAssemblyVer() {
        return promoteGenomeVer.getAssemblyVersionAsBigDecimal();
    }

    /**
     *
     */
    protected OID getNewFeatureOID() throws java.lang.RuntimeException {
        // Use the genome version of this feature to determine which
        // uid server to request new OIDs from
        GenomeVersion myGenomeVer = this.getGenomeVersion();

        String oidHostname = myGenomeVer.getGenomeVersionInfo()
                                        .getOidHostname();

        if (oidHostname == null) {
            throw new java.lang.RuntimeException(
                    "Cant generate new feature OIDs " + 
                    " because oid hostname is null for genome version " + 
                    myGenomeVer);
        }

        short oidPort = myGenomeVer.getGenomeVersionInfo().getOidPort();

        if (oidPort == 0) {
            throw new java.lang.RuntimeException(
                    "Cant generate new feature OIDs " + 
                    " because oid port number is not specified for genome version " + 
                    myGenomeVer);
        }


        // DEBUG: Should be removed...
        System.out.println("Creating accession client with: " + " hostname " + 
                           oidHostname + " and port " + oidPort);

        AccessionClient uidClient = new AccessionClient(oidHostname, oidPort);

        // REVIIST: Dont know why have to code it this way just do
        // apparently???
        BigInteger[] interval = new BigInteger[4];
        uidClient.SetUIDSize(BigInteger.valueOf(1));

        BigInteger uid = null;
        OID featureOID = null;

        try {
            uidClient.GetNewUIDInterval(interval);
            uid = uidClient.GetNextUID();
            featureOID = new OID(uid.toString(), getGenomeVersion().getID());
        } catch (Exception ex) {
            ModelMgr.getModelMgr().handleException(ex);
            featureOID = new OID(); // null oid
        }

        return featureOID;
    }

    /**
     * Creates a persitent representation of this curated feature using the
     * the object supporting the RdbmsAccess interface supplied to this
     * object at the time buildPromoteInstructions was called on this
     * instance. After a call to this method a representation of the
     * base class CuratedFeatures attributes only, have been written.
     * @see also buildPromoteInstructions
     */
    private void insertBaseCuratedFeature(String msgOnFail, int featureLength)
                                   throws SQLException {
        CallableStatement cs = null;

        try {
            cs = this.rdbmsAccess.prepareCallableStatement(
                         "{ ? = call api_writeback_pkg.add_base_curated_feature(?,?,?,?,?,?,?,?,?,?,?,?,?,?) }", 
                         this.getPromoteDatasourceName());

            int stmtIndex = 2;
            cs.setBigDecimal(stmtIndex++, this.getPromoteOID().toBigDecimal());
            cs.setInt(stmtIndex++, this.getEntityType().value());
            cs.setBigDecimal(stmtIndex++, this.getPromoteAssemblyVer());
            bindPropertyValueToStatementAsString(cs, stmtIndex++, 
                                                 FeatureFacade.CREATED_BY_PROP);
            bindPropertyValueToStatementAsBigDecimalEncodedDate(cs, stmtIndex++, 
                                                                FeatureFacade.DATE_CREATED_PROP);
            bindPropertyValueToStatementAsString(cs, stmtIndex++, 
                                                 FeatureFacade.CURATED_BY_PROP);
            bindPropertyValueToStatementAsBigDecimalEncodedDate(cs, stmtIndex++, 
                                                                FeatureFacade.DATE_CURATED_PROP);

            GenomicProperty reviewedByProp = this.getProperty(
                                                     FeatureFacade.REVIEWED_BY_PROP);
            String thisFeatureReviewName = null;

            if (this.reviewAuth.isReviewed()) {
                if ((reviewedByProp != null) && 
                        ((thisFeatureReviewName = reviewedByProp.getInitialValue()) != null) && 
                        (!(thisFeatureReviewName.equals(""))) && 
                        (this.reviewAuth.isReviewer())) {
                    bindPropertyValueToStatementAsString(cs, stmtIndex++, 
                                                         FeatureFacade.REVIEWED_BY_PROP);
                    bindPropertyValueToStatementAsBigDecimalEncodedDate(cs, 
                                                                        stmtIndex++, 
                                                                        FeatureFacade.DATE_REVIEWED_PROP);
                } else {
                    // This means that the person initiating the review was authorized
                    // but either the name given in the XML file was blank or the name
                    // given as the reviewer was not authorized. Throw a runtime
                    // exception to indicate the security violation
                    throw new java.lang.IllegalArgumentException("User " + 
                                                                 thisFeatureReviewName + 
                                                                 " not authorized for review " + 
                                                                 " or the reviewer name in the .gbw file was blank");
                }
            } else {
                cs.setNull(stmtIndex++, Types.VARCHAR);
                cs.setNull(stmtIndex++, Types.NUMERIC);
            }

            bindPropertyValueToStatementAsInteger(cs, stmtIndex++, 
                                                  FeatureFacade.RELEASE_STATUS_PROP);

            cs.setInt(stmtIndex++, Math.abs(featureLength));


            // Curated features always supply an exact length
            cs.setInt(stmtIndex++, 1);
            cs.setString(stmtIndex++, this.rdbmsAccess.getRdbmsSecurityId());


            // Now identify to which data layer features should be promoted
            cs.setInt(stmtIndex++, this.getDataLayerForPromote());
            this.rdbmsAccess.executeUpdate(cs, this.getPromoteDatasourceName());


            // Now if there are any curation flags changes or comment changes/additions
            // write them out.
            updateCurationFlags();
        } catch (Exception sqlEx) {
            ModelMgr.getModelMgr().handleException(sqlEx);

            // Rethrow so that caller gets indication of failure immediately
            throw new SQLException(sqlEx.getMessage());
        } finally {
            this.rdbmsAccess.executeComplete(cs, 
                                             this.getPromoteDatasourceName());
        }
    }

    /**
     * Creates a persitent representation of the curation flags for this feature
     * using the object supporting the RdbmsAccess interface supplied to this
     * object at the time buildPromoteInstructions was called on this
     * instance
     * @see also buildPromoteInstructions
     */
    private void updateCurationFlags() throws SQLException, Exception {
        CallableStatement flagCs = null;

        try {
            GenomicProperty numCurFlagsProp = this.getProperty(
                                                      TranscriptFacade.CURATION_FLAGS_PROP);

            if (numCurFlagsProp != null) {
                GenomicProperty[] flagProps = numCurFlagsProp.getSubProperties();
                String flagName = null;
                int flagVal = 0;
                int stmtIndex = 2;

                for (int i = 0; i < flagProps.length; i++) {
                    flagName = flagProps[i].getName();
                    flagVal = Integer.parseInt(flagProps[i].getInitialValue());
                    flagCs = this.rdbmsAccess.prepareCallableStatement(
                                     "{ ? = call api_writeback_pkg.update_curation_flag(?,?,?,?) }", 
                                     this.getPromoteDatasourceName());
                    flagCs.setBigDecimal(stmtIndex++, 
                                         this.getPromoteOID().toBigDecimal());
                    flagCs.setString(stmtIndex++, flagName);
                    flagCs.setInt(stmtIndex++, flagVal);
                    flagCs.setBigDecimal(stmtIndex++, 
                                         this.getPromoteAssemblyVer());
                    this.rdbmsAccess.executeUpdate(flagCs, 
                                                   this.getPromoteDatasourceName());
                    stmtIndex = 2;
                }
            }
        } catch (Exception sqlEx) {
            ModelMgr.getModelMgr().handleException(sqlEx);

            // Rethrow so that caller gets indication of failure immediately
            throw new SQLException(sqlEx.getMessage());
        } finally {
            this.rdbmsAccess.executeComplete(flagCs, 
                                             this.getPromoteDatasourceName());
        }
    }

    protected void addObsoleteInstructions(Axis axis, String objTypeString)
                                    throws SQLException {
        // Obsolete every alignment to the given axis involving the curated features
        // that this curated feature indicates that it replaces
        ReplacementRelationship replRel = this.getReplacementRelationship();
        replRel.getReplacementOIDs();

        OID[] replacesOIDS = replRel.getReplacementOIDs();

        CallableStatement cs = null;

        try {
            int stmtIndex = 2;

            for (int i = 0; i < replacesOIDS.length; i++) {
                if (!alreadyObsoletedOIDS.contains(replacesOIDS[i])) {
                    cs = this.rdbmsAccess.prepareCallableStatement(
                                 "{ ? = call api_writeback_pkg.obsolete_base_curated_feature(?,?,?,?,?) }", 
                                 this.getPromoteDatasourceName());

                    cs.setBigDecimal(stmtIndex++, 
                                     replacesOIDS[i].toBigDecimal());
                    cs.setBigDecimal(stmtIndex++, axis.getOid().toBigDecimal());
                    cs.setBigDecimal(stmtIndex++, this.getPromoteAssemblyVer());
                    cs.setString(stmtIndex++, 
                                 this.rdbmsAccess.getRdbmsSecurityId());


                    // Specify the data layer to which obsoleted features should be
                    // moved
                    cs.setInt(stmtIndex++, this.getDataLayerForObsolete());
                    this.rdbmsAccess.executeUpdate(cs, 
                                                   this.getPromoteDatasourceName());
                    stmtIndex = 2;


                    // Hook in that may be optionally overriden by a sublcass to allow
                    // it to write an audit trail of the obsoletion.
                    writeAuditTrailForObsolete(replacesOIDS[i], axis);


                    // Record the fact that the feature with replacesOIDS[i] was
                    // obsoleted so that multiple children of the original dont
                    // try to cause multiple obsolete instructions
                    alreadyObsoletedOIDS.add(replacesOIDS[i]);
                }
            }
        } catch (Exception sqlEx) {
            ModelMgr.getModelMgr().handleException(sqlEx);

            // Rethrow so that caller gets indication of failure immediately
            throw new SQLException(sqlEx.getMessage());
        } finally {
            this.rdbmsAccess.executeComplete(cs, 
                                             this.getPromoteDatasourceName());
        }
    }

    /**
     * Provides a hook in option for crating an audit trail of an obsoletion
     * but leaves it up to subclasses to decide if this is necessary or not.
     * Default implementation is intentially a NOP.
     */
    protected void writeAuditTrailForObsolete(OID candidateForObsoletion, 
                                              Axis axis)
                                       throws SQLException {
    }

    /**
     * By default the natural order of children as stored in this feature
     * is returned, subclasses have the opportunity to alter this
     * behaviour by overriding this method to meet order requirements in
     * the persistent store.
     */
    protected GenomicEntity[] doGetChildrenInPromotionOrder(Axis axis) {
        // Use getSubStructure here as we dont want to include obsoletes
        Collection subFeatCol = this.getSubStructure();
        GenomicEntity[] subFeats = new GenomicEntity[subFeatCol.size()];
        subFeatCol.toArray(subFeats);

        return subFeats;
    }

    /**
     * From the point of view of promotion we need to know about ALL
     * features, this includes features that are obosolete as the job of
     * promotion is to record this fact. Subclasses can use this method
     * during promotion to get a list of all children
     */
    protected Collection getPromotionChildrenIncludingObsoletes() {
        // Get all non obsoleted children
        Collection subFeatCol = this.getSubStructure();

        // Now get all obsoleted children of this feature so they too are
        // considered during promotion
        Workspace myWorkspace = this.getGenomeVersion().getWorkspace();
        Collection obsoletedFeatCol = myWorkspace.getObsoletedSubFeatureOfSuperFeature(
                                              this.getOid());


        // Return a collection of the two lists
        subFeatCol.addAll(obsoletedFeatCol);

        return subFeatCol;
    }

    protected void addNewChildAssociationsInstructions(Axis axis)
                                                throws SQLException {
        // Create new child records for the children of this CuratedFeature
        // Let subclasses define an order for the promoted children
        // if required.
        GenomicEntity[] children = doGetChildrenInPromotionOrder(axis);
        CuratedFeature currentChildFeat = null;
        CallableStatement cs = null;

        // Check explicitly that we still have at least one child if not
        // raise an exception and stop. Have seen one unexplained case
        // where the parent child relationships were not created so adding
        // some additional sanity checks. This was due to a bizzare chain of events
        // including DB constraints being turned off and duplicate transcripts
        // in a gbw file
        if (children.length == 0) {
            throw new RuntimeException(
                    "Got zero children back when trying to add " + 
                    " child associations for feature " + this.getOid() + 
                    " with promote id " + " of " + this.getPromoteOID());
        }

        try {
            int stmtIndex = 2;

            for (int i = 0; i < children.length; i++) {
                currentChildFeat = (CuratedFeature) children[i];

                String replacesType = currentChildFeat.getReplacementRelationship()
                                                      .getReplacementType();

                if (!replacesType.equals(ReplacementRelationship.TYPE_OBSOLETE)) {
                    cs = this.rdbmsAccess.prepareCallableStatement(
                                 "{ ? = call api_writeback_pkg.add_child_to_parent_feature(?,?,?,?,?,?) }", 
                                 this.getPromoteDatasourceName());
                    cs.setBigDecimal(stmtIndex++, 
                                     this.getPromoteOID().toBigDecimal());
                    cs.setBigDecimal(stmtIndex++, 
                                     ((CuratedFeature) currentChildFeat).getPromoteOID()
                                                                      .toBigDecimal());
                    cs.setInt(stmtIndex++, this.getEntityType().value());
                    cs.setInt(stmtIndex++, 
                              currentChildFeat.getEntityType().value());
                    cs.setInt(stmtIndex++, i);
                    cs.setBigDecimal(stmtIndex++, this.getPromoteAssemblyVer());


                    //cs.setString(stmtIndex++,this.rdbmsAccess.getRdbmsSecurityId());
                    this.rdbmsAccess.executeUpdate(cs, 
                                                   this.getPromoteDatasourceName());
                    stmtIndex = 2;
                }
            }
        } catch (Exception sqlEx) {
            ModelMgr.getModelMgr().handleException(sqlEx);

            // Rethrow so that caller gets indication of failure immediately
            throw new SQLException(sqlEx.getMessage());
        } finally {
            this.rdbmsAccess.executeComplete(cs, 
                                             this.getPromoteDatasourceName());
        }
    }

    protected void addBaseCuratedFeatureRepresentation(Axis axis, 
                                                       String objType)
                                                throws SQLException {
        // Assuming check promotable has been called at this point, still
        // check for a null return and throw an exception if no alignment
        // found BUT, this is not the primary check of promotablility.
        Alignment anAlign = this.getOnlyAlignmentToAnAxis(axis);

        if (anAlign == null) {
            throw new RuntimeException(
                    "Failed to add base curated feature representation " + 
                    " of feature " + this.getOid() + " on axis " + 
                    axis.getOid() + " because " + 
                    " no alignment for the feature to the axis was found");
        }

        GeometricAlignment geoAlign = (GeometricAlignment) anAlign;
        insertBaseCuratedFeature("Could not create Feature representation of " + 
                                 objType + this.getOid(), 
                                 geoAlign.getRangeOnAxis().getMagnitude());


        // Next add the required alignment to the axis for promotion. Relying
        // on checkPromotable to ensuring Curated Feature is in correct state
        addAlignmentToAxisPromoteInstruction(axis, geoAlign, 
                                             "Could not create an alignment of " + 
                                             objType + " " + this.getOid() + 
                                             " to " + " axis " + 
                                             axis.getOid(), true);


        // If this Curated Feature has evidence then add that as well.
        addEvidencePromoteInstructions(
                "Could not create Entity_Relationship evidence relationships for " + 
                objType + " " + this.getOid());
    }

    /**
     *
     */
    private void addEvidencePromoteInstructions(String errorString) {
        Collection evidenceOIDS = this.getEvidenceOids();

        if ((evidenceOIDS == null) || (!supportsDirectEvidence())) {
            return;
        }

        CallableStatement cs = null;

        try {
            // The only type of features that can be evidenced are exons. If any
            // other type turns up as having evidence then throw and exception
            // and terminate the promotion process
            if (this.getEntityType().value() == EntityTypeConstants.Exon) {
                Iterator itr = evidenceOIDS.iterator();
                int stmtIndex = 2;
                OID evidenceOID = null;

                for (int i = 0; itr.hasNext(); i++) {
                    evidenceOID = (OID) itr.next();
                    cs = this.rdbmsAccess.prepareCallableStatement(
                                 "{ ? = call api_writeback_pkg.add_evidence_for_feature(?,?,?,?,?,?) }", 
                                 this.getPromoteDatasourceName());
                    cs.setBigDecimal(stmtIndex++, 
                                     this.getPromoteOID().toBigDecimal());
                    cs.setBigDecimal(stmtIndex++, evidenceOID.toBigDecimal());
                    cs.setInt(stmtIndex++, this.getEntityType().value());
                    cs.setInt(stmtIndex++, i);
                    cs.setBigDecimal(stmtIndex++, this.getPromoteAssemblyVer());
                    cs.setString(stmtIndex++, 
                                 this.rdbmsAccess.getRdbmsSecurityId());
                    this.rdbmsAccess.executeUpdate(cs, 
                                                   this.getPromoteDatasourceName());
                    stmtIndex = 2;
                }
            } else {
                throw new RuntimeException(errorString + "Feature " + 
                                           this.getOid() + " of type " + 
                                           this.getEntityType().getEntityName() + 
                                           " listed as having evidence. " + 
                                           "Only exons can be evidenced, promotion aborted");
            }
        } catch (Exception sqlEx) {
            // Any exception should terminate promotion
            throw new RuntimeException(sqlEx.getMessage());
        }
    }

    protected void addMakeDeepCopyPromoteInstructions(Axis axis, String objType)
                                               throws SQLException {
        // Perform obsolete step. The modified gene should not be
        // obsoleted, otherwise the transcripts for the same gene
        // but from a diferent gbw file may fail
        // 4-23-2001 Talked with Marian all changes to a gene will
        // be confined to a single gbw file JB
        //    if ( !objType.equalsIgnoreCase("gene") )
        //    {
        //      addObsoleteInstructions(axis, assemblyVersion, objType);
        //    }
        addObsoleteInstructions(axis, objType);


        // Add a new gene logic of add will look at replaces information and
        // reuse the accession number if necessary
        addBaseCuratedFeatureRepresentation(axis, objType);
    }

    /**
     * Will request a new accession number for this feature using this features
     * type and identifier as keys to determine what form the accession number
     * should take. The CuratedFeature then request a new accession number
     * that will be unique within the combination of GenomeVersion and feature
     * type from the accession number server.
     * @throws RuntimeException if the accession number server cannot generate
     * a new accession number for whatever reason.
     */
    protected String generateNewAccessionNumber() throws RuntimeException {
        // REVISIT: Cant do this any more, inter component calls are not working
        // since the move to a statefull server

        /*FeatureIndexLibrary indexLibrary
          = FacadeManager.getFacadeManager().getGloballUniqueIndexLibraryFacade();
        FeatureIndex index = indexLibrary.generateIndexForType(this.getEntityType());
        return index.getIndexVal();*/
        String accession = "NULL";

        if (rdbmsAccess != null) {
            accession = rdbmsAccess.generateAccessionNumberFor(this.getOid(), 
                                                               this.getEntityType(), 
                                                               this.getPromoteAssemblyVer());
        }

        if ((accession.equals("")) || (accession.equals("NULL")) || 
                (accession.equals(WRONG_TRAN_ACCESSION_NUMBER)) || 
                (accession.equals(WRONG_GENE_ACCESSION_NUMBER)) || 
                (accession.equals(NULL_TRAN_ACCESSION_NUMBER)) || 
                (accession.equals(NULL_GENE_ACCESSION_NUMBER)) || 
                (accession.equals(WORKSPACE_TRAN_PREFIX)) || 
                (accession.equals(WORKSPACE_GENE_PREFIX))) {
            throw new RuntimeException(
                    "Failed to generate valid accession number " + 
                    " required to promote feature " + this.getOid() + 
                    ". Possible reasons are \n" + 
                    "1. The accession server is not running or the machine it is on cannot " + 
                    " be reached \n" + 
                    "2. The accession number server ran out of accession numbers for this " + 
                    " type of feature whose type is = " + 
                    this.getEntityType().getEntityName());
        }

        return accession;
    }

    /**
     * Generates and returns a unique OID for a feature if possible
     * on failure it returns the null OID. If the resources
     * it needs are not avaialble the it raises MissingResourceException
     * for that resource.
     */
    private OID convertToPromotedOID() {
        return getPromoteOID();
    }

    /**
     * If a globally unique identifier has not yet been generated for this
     * feature to be used during promtion of a new version of the feature
     * to the database, then generate one now, otherwise return the existing
     * value.
     */
    protected OID getPromoteOID() {
        if (promotedOID == null) {
            promotedOID = getNewFeatureOID();
        }

        return promotedOID;
    }

    protected int getDataLayerForObsolete() {
        return obsoleteDataLayerId;
    }

    protected int getDataLayerForPromote() {
        return promoteDataLayerId;
    }

    abstract protected PromotionReport doBuildPromoteInstructions(Axis axis, 
                                                                  String parentReplacesDirective)
        throws SQLException;

    /**
     * If any child is modified in any way other than NEW or OBSOLOTE
     * this method will return true otherwise if will return false
     */
    protected boolean anyChildModified(long assemblyVersion) {
        boolean modifiedChildFound = false;

        // Use get subStructure here as dont want to consider
        // obsoleted children in this test
        Collection children = this.getSubStructure();
        Iterator childIter = children.iterator();

        if (children.size() > 0) {
            CuratedFeature currentFeature = null;
            int i = 0;

            while ((childIter.hasNext()) && (modifiedChildFound == false)) {
                currentFeature = (CuratedFeature) childIter.next();

                // Leave the redundant check against TYPE_OBSOLETE so that future
                // changes do not include obsoletes in the test. Cost of database
                // cleanup is greater than cost of redundant checks
                if (!(currentFeature.getReplacementRelationshipType().equals(ReplacementRelationship.TYPE_UNMODIFIED)) && 
                        !(currentFeature.getReplacementRelationshipType().equals(ReplacementRelationship.TYPE_NEW)) && 
                        !(currentFeature.getReplacementRelationshipType().equals(ReplacementRelationship.TYPE_OBSOLETE))) {
                    modifiedChildFound = true;
                } else {
                    modifiedChildFound = currentFeature.anyChildModified(
                                                 assemblyVersion);
                }

                ++i;
            }
        }

        return modifiedChildFound;
    }

    protected RdbmsAccess getRdbmsAccess() {
        return this.rdbmsAccess;
    }

    protected ReviewAuthority getReviewAuthority() {
        return this.reviewAuth;
    }

    protected GenomeVersion getPromotionGenomeVersion() {
        return this.promoteGenomeVer;
    }

    protected void bindPropertyValueToStatementAsString(CallableStatement cs, 
                                                        int index, 
                                                        String propName)
                                                 throws SQLException {
        GenomicProperty aProperty = this.getProperty(propName);
        String propValue = null;

        if ((aProperty == null) || 
                ((propValue = aProperty.getInitialValue()) == null)) {
            cs.setNull(index, Types.VARCHAR);
        } else {
            cs.setString(index, propValue);
        }
    }

    protected void bindPropertyValueToStatementAsBigDecimalEncodedDate(CallableStatement cs, 
                                                                       int index, 
                                                                       String propName)
        throws SQLException {
        GenomicProperty aProperty = this.getProperty(propName);
        String propValue = null;

        if ((aProperty == null) || 
                ((propValue = aProperty.getInitialValue()) == null) || 
                (propValue.equals(""))) {
            cs.setNull(index, Types.NUMERIC);
        } else {
            try {
                BigDecimal aBigDecimal = new BigDecimal(
                                                 Util.convertDateTimeAsStringToSecondsSince1970(
                                                         propValue));
                cs.setBigDecimal(index, aBigDecimal);
            } catch (Exception efEx) {
                cs.setNull(index, Types.NUMERIC);
                ModelMgr.getModelMgr().handleException(efEx);
            }
        }
    }

    protected void bindPropertyValueToStatementAsInteger(CallableStatement cs, 
                                                         int index, 
                                                         String propName)
                                                  throws SQLException {
        GenomicProperty aProperty = this.getProperty(propName);
        String propValue = null;

        if (aProperty == null) {
            cs.setNull(index, Types.INTEGER);
        } else {
            propValue = aProperty.getInitialValue();

            if ((propValue == null) || (propValue.length() == 0)) {
                cs.setNull(index, Types.INTEGER);
            } else {
                try {
                    int anInt = Integer.parseInt(propValue);
                    cs.setInt(index, anInt);
                } catch (NumberFormatException efEx) {
                    cs.setNull(index, Types.INTEGER);
                    ModelMgr.getModelMgr().handleException(efEx);
                }
            }
        }
    }

    protected void updateFeatureCurationModified() throws SQLException {
        CallableStatement cs = null;

        ReplacementRelationship replRel = this.getReplacementRelationship();

        if (replRel == null) {
            throw new RuntimeException("Feature " + this.getOid() + 
                                       " had a null " + 
                                       " replacement relationship so could not determine which feature curation " + 
                                       " rows for which the modified date should be set. Aborting promotion");
        }

        try {
            OID[] replacedOIDS = replRel.getReplacementOIDs();

            for (int i = 0; i < replacedOIDS.length; i++) {
                cs = this.rdbmsAccess.prepareCallableStatement(
                             "{ ? = call api_writeback_pkg.update_feature_curation_modify(?,?,?) }", 
                             this.getPromoteDatasourceName());

                int stmtIndex = 2;
                cs.setBigDecimal(stmtIndex++, replacedOIDS[i].toBigDecimal());
                bindPropertyValueToStatementAsString(cs, stmtIndex++, 
                                                     FeatureFacade.CURATED_BY_PROP);
                bindPropertyValueToStatementAsBigDecimalEncodedDate(cs, 
                                                                    stmtIndex++, 
                                                                    FeatureFacade.DATE_CURATED_PROP);


                //cs.setString(stmtIndex++,this.rdbmsAccess.getRdbmsSecurityId());
                this.rdbmsAccess.executeUpdate(cs, 
                                               this.getPromoteDatasourceName());
            }
        } catch (Exception sqlEx) {
            ModelMgr.getModelMgr().handleException(sqlEx);

            // Rethrow so that caller gets indication of failure immediately
            throw new SQLException(sqlEx.getMessage());
        } finally {
            this.rdbmsAccess.executeComplete(cs, 
                                             this.getPromoteDatasourceName());
        }
    }

    protected void updateComments() throws Exception {
        System.out.println("Getting comments for feature " + this.getOid() + 
                           " of type " + this.getEntityType());

        Set comments = this.getComments();
        System.out.println("Got " + comments.size() + " comments");

        Iterator commentIter = comments.iterator();
        CallableStatement commentCs = null;

        try {
            GenomicEntityComment currentComment = null;
            int stmtIndex = 2;
            String dateAsString = null;

            while (commentIter.hasNext()) {
                currentComment = (GenomicEntityComment) commentIter.next();
                commentCs = this.getRdbmsAccess()
                                .prepareCallableStatement("{ ? = call api_writeback_pkg.update_feature_comment(?,?,?,?) }", 
                                                          this.getPromoteDatasourceName());
                commentCs.setBigDecimal(stmtIndex++, 
                                        this.getPromoteOID().toBigDecimal());
                commentCs.setString(stmtIndex++, currentComment.getCreatedBy());

                dateAsString = currentComment.getCreationDateAsString();

                if ((dateAsString == null) || (dateAsString.equals(""))) {
                    dateAsString = Util.getDateTimeStringNow();
                }

                commentCs.setBigDecimal(stmtIndex++, 
                                        new BigDecimal(
                                                Util.convertDateTimeAsStringToSecondsSince1970(
                                                        dateAsString)));
                commentCs.setString(stmtIndex++, currentComment.getComment());
                this.getRdbmsAccess()
                    .executeUpdate(commentCs, this.getPromoteDatasourceName());
                stmtIndex = 2;
            }
        } catch (Exception sqlEx) {
            ModelMgr.getModelMgr().handleException(sqlEx);

            // Rethrow so that caller gets indication of failure immediately
            throw new SQLException(sqlEx.getMessage());
        } finally {
            this.rdbmsAccess.executeComplete(commentCs, 
                                             this.getPromoteDatasourceName());
        }
    }

    public void clearAlreadyObsoletedOIDList() {
        alreadyObsoletedOIDS.clear();
    }

    /** Allows sublcasses to adjust thier alignment information before it
      * is written to persistent storage. By default no adjustment is made.
      * Need to allow for this because of some backward compatibility issues with
      * DTD format not having an attribute for storing orientation
      * conflicting with the requirement of storing some feature types with a
      * zero length alignment.
      */
    protected GeometricAlignment bogusAlignmentAdjustmentForPromotion(GeometricAlignment origAlignment) {
        return origAlignment;
    }

    /**
     * Template override that allows sub features to indicate if they
     * support direct evidence relationships. This will allow evidence rels
     * to be written to the database by changing this override for a class
     * should a decision be made to change against which subtypes they are
     * recorded
     */
    protected boolean supportsDirectEvidence() {
        return false;
    }

    /**
     * A more specific getMutator();
     */
    protected CuratedFeatureMutator getCuratedFeatureMutator() {
        return (CuratedFeatureMutator) this.getMutator();
    }

    //****************************************
    //*  Inner / Member classes
    //****************************************

    /**
     * The CuratedFeatureMutator class is the only way you can change the state of Feature
     * instances.
     * The CuratedFeatureMutator class is public, but it's constructor is private.
     */
    public class CuratedFeatureMutator extends FeatureMutator {
        /**
         * Protected constructor for the mutator class...
         */
        protected CuratedFeatureMutator() {
        }

        //=================== Responding to Feature structure change ===========

        /**
         * Adjust  for an added sub-feature.  This gives sub-classes, like Transcript,
         * to adjust for an added sub-feature.
         * This will be called by FeatureMutator.addSubFeature(Feature newSubFeature)
         * - After the newSubFeature has been added
         * - Before we postSubFeatureAdded(newSubFeature);
         */
        void adjustForAddedSubFeature(Feature newSubFeature) {
            super.adjustForAddedSubFeature(newSubFeature);


            // Update my alignment...
            this.updateAllSuperFeatureGeomAlignmentsBasedOnSubFeatures();
        }

        /**
         * Adjust  for an added sub-feature.  This gives sub-classes, like Transcript,
         * to adjust for an added sub-feature.
         * This will be called by FeatureMutator.addSubFeature(Feature newSubFeature)
         * - After the newSubFeature has been added
         * - Before we postSubFeatureAdded(newSubFeature);
         */
        void adjustForRemovedSubFeature(Feature oldSubFeature) {
            super.adjustForRemovedSubFeature(oldSubFeature);


            // Now update my alignment.
            this.updateAllSuperFeatureGeomAlignmentsBasedOnSubFeatures();
        }

        //====================== Alignment Mutation ============================

        /**
         * Remove only alignment.
         * Since Curated Feature is SingleAlignmentSingleAxis... we can do this.
         * This does not initiate an "adjustForRemovedSubFeature()" call.
         */
        void removeOnlyAlignmentToOnlyAxis() {
            Alignment onlyAlignment = CuratedFeature.this.getOnlyAlignmentToOnlyAxis();

            if (onlyAlignment != null) {
                super.removeAlignmentToAxis(onlyAlignment);
            }
        }

        /**
         * Remove the ONLY alignment for this CuratedFeature all it's substructure.
         * This method will remove the this CuratedFeature from it's super feature
         * if it has a super feature.
         * Only initiate "adjustForRemovedSubFeature()" on the super feature when
         * everything has been removed.
         * @returns the collection of sub structure features that were removed.
         */
        public Collection removeSubStructureAndRemoveAlignments(boolean deleteFromWorkspace) {
            Workspace theWorkspace = null;
            GenomeVersion theGenomeVersion = CuratedFeature.this.getGenomeVersion();

            if (theGenomeVersion != null) {
                theWorkspace = theGenomeVersion.getWorkspace();
            }

            return this.removeSubStructureAndRemoveAlignments(
                           deleteFromWorkspace, theWorkspace);
        }

        public Collection removeSubStructureAndRemoveAlignments(boolean deleteFromWorkspace, 
                                                                Workspace theWorkspace) {
            return this.removeSubStructureAndRemoveAlignments(
                           deleteFromWorkspace, theWorkspace, true);
        }

        /**
         * Remove the ONLY alignment for this CuratedFeature all it's substructure.
         * This method will remove the this CuratedFeature from it's super feature
         * if it has a super feature and the argument updateSuperGeometry is true.
         * Only initiate "adjustForRemovedSubFeature()" on the super feature when
         * everything has been removed.
         * @returns the collection of sub structure features that were removed.
         * @todo: removeSubStructureAndRemoveAlignments() move Transcript & Codon
         * specific portions to subclass over-ride methods.
         */
        private Collection removeSubStructureAndRemoveAlignments(boolean deleteFromWorkspace, 
                                                                 Workspace theWorkspace, 
                                                                 boolean updateSuperGeometry) {
            Collection removedFeatures = new ArrayList();

            // First iterate across all Sub Structure elements...
            if (CuratedFeature.this instanceof SuperFeature) {
                Collection subFeatures = CuratedFeature.this.getSubStructure();
                Collection obsoletingFeatures = theWorkspace.getObsoletedSubFeatureOfSuperFeature(
                                                        CuratedFeature.this.getOid());
                subFeatures.addAll(obsoletingFeatures);

                CuratedFeature subFeature = null;
                CuratedFeatureMutator subMutator = null;

                for (Iterator itr = subFeatures.iterator(); itr.hasNext();) {
                    subFeature = (CuratedFeature) itr.next();
                    subMutator = subFeature.getCuratedFeatureMutator();
                    removedFeatures.addAll(
                            subMutator.removeSubStructureAndRemoveAlignments(
                                    deleteFromWorkspace, theWorkspace, false));
                }
            }

            // Remove as a sub-feature...
            CuratedFeature superFeature = null;

            if (updateSuperGeometry || 
                    CuratedFeature.this.isReplacementType(
                            ReplacementRelationship.NEW)) {
                // This should be done in CuratedTranscriptMutator (override of this method).
                if (CuratedFeature.this instanceof CuratedCodon) {
                    CuratedTranscript hostTrans = ((CuratedCodon) CuratedFeature.this).getHostTranscript();

                    if (hostTrans != null) {
                        superFeature = hostTrans;
                        hostTrans.getCuratedTranscriptMutator()
                                 .removeStartOrStopCodon((CuratedCodon) CuratedFeature.this);
                    }
                } else {
                    superFeature = (CuratedFeature) CuratedFeature.this.getSuperFeature();

                    if (superFeature != null) {
                        superFeature.getCuratedFeatureMutator()
                                    .removeSubFeature(CuratedFeature.this);
                    }
                }
            }


            // Remove the only alignment...
            this.removeOnlyAlignmentToOnlyAxis();

            if (deleteFromWorkspace || 
                    CuratedFeature.this.isReplacementType(
                            ReplacementRelationship.NEW)) {
                this.deleteFromWorkspace(theWorkspace);
            }

            removedFeatures.add(CuratedFeature.this);

            // Update Super Geometry?
            if (updateSuperGeometry && (superFeature != null)) {
                superFeature.getCuratedFeatureMutator()
                            .adjustForRemovedSubFeature(CuratedFeature.this);
            }

            return removedFeatures;
        }

        //====================== Replacement Relationship mutation==============

        /**
         * This needs to be called explicitely instead of observing.
         * The creational process includes many "mutations"
         */
        public void addToWorkspaceAndTrack() {
            // If I'm not a workspace feature don't do anything...
            System.out.println("CurFeatMut.addToWorkspaceAndTrack(); called.");

            if (!CuratedFeature.this.isWorkspace()) {
                return;
            }

            Workspace myWorkspace = CuratedFeature.this.getGenomeVersion()
                                                       .getWorkspace();

            if (myWorkspace != null) {
                myWorkspace.addToWorkspaceAndTrack(CuratedFeature.this.getOid());
            }
        }

        /**
         * Set up the initial replacement relationship as replacing a promoted item.
         * This will set up an "unmodified" relationship.
         * This is called AFTER the CuratedFeature is alinged to the axis.
         */
        public void setupReplacementOfPromoted(CuratedFeature promotedFeature) {
            // If I'm not a workspace feature don't do anything...
            if (!CuratedFeature.this.isWorkspace() || 
                    (promotedFeature == null)) {
                System.out.println("Not needed!");

                return;
            }

            Workspace myWorkspace = CuratedFeature.this.getGenomeVersion()
                                                       .getWorkspace();

            if (myWorkspace != null) {
                myWorkspace.setupReplacementOfPromoted(
                        CuratedFeature.this.getOid(), promotedFeature.getOid());
            } else {
                System.out.println("No Workspace!");
            }
        }

        /**
         * Set up the "Obsoleted" information.
         * Need to store off the Alignments.
         * This is called AFTER CuratedFeature has been unaligned from the axis.
         * @todo RepRel - obsolete decide how much the command does, and how much the curation mutator does.
         */
        public void obsoleteInWorkspace() {
            // Remove from Super Feature... (doing this first minimizes the automatic recalculation...
            // For each sub-feature Obsolete sub-feature...
            // Unalign from ALL axes... remove all alignments to all axes (but save them)
            // Record obsoleted information with workspace... alignments, this obs entity.
            Workspace myWorkspace = CuratedFeature.this.getGenomeVersion()
                                                       .getWorkspace();

            if (myWorkspace != null) {
                myWorkspace.obsoleteInWorkspace(CuratedFeature.this.getOid(), 
                                                CuratedFeature.this);
            }
        }

        /**
         * Explicit call to set the ReplacementRelationship type...
         */
        public void setReplacementRelationshipType(byte newReplacementType) {
            if (CuratedFeature.this.getGenomeVersion() == null) {
                return;
            }

            Workspace myWorkspace = CuratedFeature.this.getGenomeVersion()
                                                       .getWorkspace();

            if (myWorkspace != null) {
                myWorkspace.setReplacementRelationshipType(
                        CuratedFeature.this.getOid(), newReplacementType);
            }
        }

        /**
         * Explicit call to set the ReplacementRelationship...
         */
        public void setReplacementRelationship(ReplacementRelationship newRepRel) {
            if (CuratedFeature.this.getGenomeVersion() == null) {
                return;
            }

            Workspace myWorkspace = CuratedFeature.this.getGenomeVersion()
                                                       .getWorkspace();

            if (myWorkspace != null) {
                myWorkspace.setReplacementRelationship(
                        CuratedFeature.this.getOid(), newRepRel);
            }
        }

        /**
         * Delete the CuratedFeature from the Workspace entirely.
         */
        public void deleteFromWorkspace() {
            GenomeVersion theGenomeVersion = CuratedFeature.this.getGenomeVersion();

            if (theGenomeVersion == null) {
                return;
            }

            this.deleteFromWorkspace(theGenomeVersion.getWorkspace());
        }

        /**
         * Delete the CuratedFeature from the passed Workspace entirely.
         * This form is primarily used when the CuratedFeature has already be
         * un-aligned from the axis, and left out of the GenomeVersion.
         */
        public void deleteFromWorkspace(Workspace workspace) {
            if (workspace != null) {
                workspace.deleteFeatureFromWorkspace(CuratedFeature.this);
            }
        }
    } // End of CuratedFeatureMutator member class.
}