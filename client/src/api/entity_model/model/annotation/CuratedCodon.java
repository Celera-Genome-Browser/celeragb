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

import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.OID;
import api.stub.data.PromotionReport;
import api.stub.data.ReplacementRelationship;
import api.stub.geometry.Range;

import java.sql.SQLException;


/**
 * Title:        Genome Browser<p>
 * Description:  The CuratedCodon class represents Start Codons, Stop Codons
 * and Translation Start Positions on a Transcript.<p>
 * @author Jay T. Schira
 * @version $Id$
 */
public class CuratedCodon extends CuratedFeature implements SubFeature,
                                                            CodonFeature {
    // Static variables...
    private static final EntityType START_CODON = EntityType.getEntityTypeForValue(
                                                          EntityTypeConstants.Start_Codon_Start_Position);
    private static final EntityType STOP_CODON = EntityType.getEntityTypeForValue(
                                                         EntityTypeConstants.StopCodon);
    private static final EntityType TRANSLATION_START_CODON = 
            EntityType.getEntityTypeForValue(
                    EntityTypeConstants.Translation_Start_Position);

    //
    private CuratedTranscript hostTranscript;

    //****************************************
    //*  Public methods
    //****************************************
    public CuratedCodon(OID oid, String displayName, EntityType type, 
                        String discoveryEnvironment)
                 throws InvalidFeatureStructureException {
        this(oid, displayName, type, discoveryEnvironment, null, null, 
             FeatureDisplayPriority.DEFAULT_PRIORITY);
    }

    public CuratedCodon(OID oid, String displayName, EntityType type, 
                        String discoveryEnvironment, 
                        FacadeManagerBase readFacadeManager, 
                        CuratedTranscript hostTranscript, byte displayPriority)
                 throws InvalidFeatureStructureException {
        super(oid, displayName, type, discoveryEnvironment, readFacadeManager, 
              null, displayPriority);

        if (hostTranscript != null) {
            if (this.isStartCodon()) {
                hostTranscript.protectedSetStartCodon(this);
            } else if (this.isStopCodon()) {
                hostTranscript.protectedSetStopCodon(this);
            } else {
                System.out.println("Unrecognized codon type!");
            }
        }
    }

    /**
     * Return the "host" transcript that this codon is on.
     */
    public CuratedTranscript getHostTranscript() {
        return hostTranscript;
    }

    /**
     * Return the "host" transcript that this codon is on.
     */
    public Feature getSuperFeature() {
        return getHostTranscript();
    }

    /**
     * Should only be called during construction, and by the CuratedTranscript.
     * This method does NOT trigger notification or re-calcuations.
     */
    void protectedSetHostTranscript(CuratedTranscript newHostTranscript) {
        this.hostTranscript = newHostTranscript;
    }

    /**
     * Get the "Root" feature.  Go up through the SuperFeature hierarchy until
     * you are at the top.
     * In the case of the codon, make sure you traverse the "hostTranscript",
     * structural relationship.
     */
    public Feature getRootFeature() {
        // Go up to the "root" feature...
        if (hostTranscript != null) {
            return hostTranscript.getRootFeature();
        }

        return this;
    }

    /**
     * Accepts visitors that implement GenomicEntityVisitor. acceptVisitorForSelf
     * is specialized by every subclass of GenomicEntity to call the appropriate
     * "visit...(...)" function on the passed visitor.
     * @param theVisitor the visitor.
     */
    public void acceptVisitorForSelf(GenomicEntityVisitor theVisitor) {
        try {
            theVisitor.visitCuratedCodon(this);
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    //== Feature Parent Child Management Methods ==============================

    /**
     * Determine if I will accept a Feature as a super feature of mine.
     * This is usually called right before a call to setSuperFeature on the
     * mutator class.
     * Subclasses can over-ride this method to make sure that there is
     * the correct hierarchy of containment... such as Gene, Transcript, Exon.
     * Subclasses should ALWAYS be more restrictive than thier super classes.
     */
    public boolean willAcceptSuperFeature(Feature newSuperFeature) {
        return false;
    }

    /**
     * Determine if I will accept a Feature as a super feature of mine.
     * This is usually called right before a call to setSuperFeature on the
     * mutator class.
     * Subclasses can over-ride this method to make sure that there is
     * the correct hierarchy of containment... such as Gene, Transcript, Exon.
     * Subclasses should ALWAYS be more restrictive than thier super classes.
     */
    public boolean willAcceptSubFeature(Feature newSubFeature) {
        return false;
    }

    //== Promotion Methods ==============================
    public PromotionReport doCheckPromotableOnAxis(Axis axis, 
                                                   PromotionReport report) {
        // Only rules for a codon are that it is parented AND that it has
        // an alignment of size == 0
        if (this.getSuperFeature() == null) {
            report.addFatalError("Codon " + this.getOid() + 
                                 " cannot be promoted " + 
                                 " when it has no transcript parent");
        }

        GeometricAlignment codonAlignment = (GeometricAlignment) this.getOnlyAlignmentToAnAxis(
                                                    axis);

        if (codonAlignment == null) {
            report.addFatalError("Codon " + this.getOid() + 
                                 " is not aligned to axis " + axis.getOid() + 
                                 " or is aligned more than once");
        } else {
            if (Math.abs(codonAlignment.getRangeOnAxis().getMagnitude()) < 1) {
                report.addFatalError("Codon " + this.getOid() + 
                                     " alignment length cannot be less than 1. " + 
                                     "Length on axis " + axis.getOid() + 
                                     " is " + 
                                     codonAlignment.getRangeOnAxis()
                                                   .getMagnitude());
            }
        }

        return report;
    }

    // Codons should be stored with axis_start = axis_end even though
    // in the .gbw file that was the source of this object the start and
    // end are not equal (because the file format does not have a place
    // to store orientation for a zero length object). Therefore adjust the
    // returned alignment object to reflect this fact so that promotion
    // creates codons with start = end
    protected GeometricAlignment bogusAlignmentAdjustmentForPromotion(GeometricAlignment origAlignment) {
        GeometricAlignment adjustedAlignment = new GeometricAlignment(
                                                       origAlignment.getAxis(), 
                                                       origAlignment.getEntity(), 
                                                       new Range(origAlignment.getRangeOnAxis()
                                                                              .getStart(), 0, 
                                                                 origAlignment.getRangeOnAxis()
                                                                              .getOrientation()));

        return adjustedAlignment;
    }

    public boolean isStartCodon() {
        return (getEntityType().equals(START_CODON) || 
               getEntityType().equals(TRANSLATION_START_CODON));
    }

    public boolean isStopCodon() {
        return getEntityType().equals(STOP_CODON);
    }

    public boolean isTranslationStartCodon() {
        return getEntityType().equals(TRANSLATION_START_CODON);
    }

    public PromotionReport doBuildPromoteInstructions(Axis axis, 
                                                      String parentReplacesDirective)
                                               throws SQLException {
        // First need to determine for this Codon whether the action being performed
        // by promotion is:
        //  1. Create a new Codon
        //  2. Update an existing Codon
        //  3. Obsolete (effectively delete) a Codon
        //  4. Too many changes to specify replace the existing codon
        //     with a new modified copy.
        //
        // Option 4 will be implemented first and if it turns out to be fast
        // enough then it may be the only option implemented
        PromotionReport report = new PromotionReport(true, "");

        String replacesType = this.getReplacementRelationship()
                                  .getReplacementType();
        System.out.println("Promoting codon with replaces of " + 
                           replacesType);

        if ((replacesType.equals(ReplacementRelationship.TYPE_SPLIT)) || 
                (replacesType.equals(ReplacementRelationship.TYPE_MERGE))) {
            report.addFatalError("Codon " + this.getOid() + 
                                 " had been split or " + 
                                 " merged, thesse operations are only legal for transcripts");
        } else if (replacesType.equals(ReplacementRelationship.TYPE_NEW)) {
            addBaseCuratedFeatureRepresentation(axis, "codon");
        } else if (replacesType.equals(ReplacementRelationship.TYPE_OBSOLETE)) {
            // Obsolete all alignments, relationship and feature_display
            // rows for the gene and then ask each child to specify it's own
            // obsolete requirements.
            addObsoleteInstructions(axis, "codon");
        } else if ((replacesType.equals(ReplacementRelationship.TYPE_DEEP_MOD)) || 
                       (replacesType.equals(
                               ReplacementRelationship.TYPE_MODIFIED)) || 
                       (parentReplacesDirective.equals(
                               ReplacementRelationship.TYPE_MODIFIED))) {
            // Throw everything away and start again...BUT keep the same accession
            // number
            addMakeDeepCopyPromoteInstructions(axis, "codon");
        } else if (replacesType.equals(ReplacementRelationship.TYPE_UNMODIFIED)) {
            // Update the date_curated and curated_by field in Feature_Curation table
            // to keep a trace of the person who has touch the feature.
            updateFeatureCurationModified();
        } else {
            report.addFatalError(
                    "Encountered an unknown replaces type for codon " + 
                    this.getOid() + " assuming XML file is corrupt.");
        }

        return report;
    }

    //****************************************
    //*  Protected methods
    //****************************************

    /**
     * Templete pattern
     * Sub-Classes should overide this to set the proper mutator
     */
    protected GenomicEntityMutator constructMyMutator() {
        return new CuratedCodonMutator();
    }

    /**
     * A more specific getMutator();
     */
    protected CuratedCodonMutator getCuratedCodonMutator() {
        return (CuratedCodonMutator) this.getMutator();
    }

    //****************************************
    //*  Inner / Member classes
    //****************************************

    /**
     * The CuratedCodonMutator class is the only way you can change the state of Feature
     * instances.
     * The CuratedCodonMutator class is public, but it's constructor is private.
     */
    public class CuratedCodonMutator extends CuratedFeatureMutator {
        /**
         * Protected constructor for the mutator class...
         */
        protected CuratedCodonMutator() {
        }

        /**
         * Set the "host" transcript that this codon is on.
         * This method should not be called directly,
         * This method should be called by CuratedTranscriptMutator.setStartCodon(CuratedCodon newStartCodon)
         * This method should be "package" visible so
         * CuratedTranscriptMutator.setStartCodon(CuratedCodon newStartCodon);
         */
        void setHostTranscript(CuratedTranscript newHostTranscript) {
            CuratedCodon.this.hostTranscript = newHostTranscript;
        }
    } // End of CuratedCodonMutator member class.
}