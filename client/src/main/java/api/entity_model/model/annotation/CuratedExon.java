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
 * @author Jay T. Schira
 * @version $Id$
 */
package api.entity_model.model.annotation;

import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.facade.abstract_facade.annotations.ExonFacade;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.OID;
import api.stub.data.PromotionReport;
import api.stub.data.ReplacementRelationship;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;


public class CuratedExon extends CuratedFeature implements SubFeature {
    //****************************************
    //*  Public methods
    //****************************************
    public CuratedExon(OID oid, String displayName, EntityType type, 
                       String discoveryEnvironment)
                throws InvalidFeatureStructureException {
        this(oid, displayName, type, discoveryEnvironment, null, null, 
             FeatureDisplayPriority.DEFAULT_PRIORITY);
    }

    public CuratedExon(OID oid, String displayName, EntityType type, 
                       String discoveryEnvironment, 
                       FacadeManagerBase readFacadeManager, 
                       Feature superFeature, byte displayPriority)
                throws InvalidFeatureStructureException {
        super(oid, displayName, type, discoveryEnvironment, readFacadeManager, 
              superFeature, displayPriority);
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
        if (!super.willAcceptSuperFeature(newSuperFeature)) {
            return false;
        }

        // Must be some kind of Transcript
        return (newSuperFeature instanceof CuratedTranscript);
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

    /**
     * Accepts visitors that implement GenomicEntityVisitor. acceptVisitorForSelf
     * is specialized by every subclass of GenomicEntity to call the appropriate
     * "visit...(...)" function on the passed visitor.
     * @param theVisitor the visitor.
     */
    public void acceptVisitorForSelf(GenomicEntityVisitor theVisitor) {
        try {
            theVisitor.visitCuratedExon(this);
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    public PromotionReport doBuildPromoteInstructions(Axis axis, 
                                                      String parentReplacesDirective)
                                               throws SQLException {
        // First need to determine for this Exon whether the action being performed
        // by promotion is:
        //  1. Create a new exon
        //  2. Update an existing exon
        //  3. Obsolete (effectively delete) a exon
        //  4. Too many changes to specify replace the existing exon
        //     with a new modified copy.
        //
        // Option 4 will be implemented first and if it turns out to be fast
        // enough then it may be the only option implemented
        PromotionReport report = new PromotionReport(true, "");

        String replacesType = this.getReplacementRelationship()
                                  .getReplacementType();
        System.out.println("Promoting exon with replaces of " + replacesType);

        if ((replacesType.equals(ReplacementRelationship.TYPE_SPLIT)) || 
                (replacesType.equals(ReplacementRelationship.TYPE_MERGE))) {
            report.addFatalError("Exon " + this.getOid() + 
                                 " had been split or " + 
                                 " merged, thesse operations are only legal for transcripts");
        } else if (replacesType.equals(ReplacementRelationship.TYPE_NEW)) {
            addBaseCuratedFeatureRepresentation(axis, "exon");
            addNewExonSpecificPromoteInstruction();
        } else if (replacesType.equals(ReplacementRelationship.TYPE_OBSOLETE)) {
            // Obsolete all alignments, relationship and feature_display
            // rows for the gene and then ask each child to specify it's own
            // obsolete requirements.
            addObsoleteInstructions(axis, "exon");

            // This call is added to avoid to a parent-child id search in the sql.
            //obsoleteEvidences(axis, assemblyVersion, "evidence");
        } else if ((replacesType.equals(ReplacementRelationship.TYPE_DEEP_MOD)) || 
                       (replacesType.equals(
                               ReplacementRelationship.TYPE_MODIFIED)) || 
                       (parentReplacesDirective.equals(
                               ReplacementRelationship.TYPE_MODIFIED))) {
            // Throw everything away and start again...BUT keep the same accession
            // number
            addMakeDeepCopyPromoteInstructions(axis, "exon");
            addNewExonSpecificPromoteInstruction();
        } else if (replacesType.equals(ReplacementRelationship.TYPE_UNMODIFIED)) {
            // Update the date_curated and curated_by field in Feature_Curation table
            // to keep a trace of the person who has touch the feature.
            updateFeatureCurationModified();
        } else {
            report.addFatalError(
                    "Encountered an unknown replaces type for exon " + 
                    this.getOid() + " assuming XML file is corrupt.");
        }

        return report;
    }

    public PromotionReport doCheckPromotableOnAxis(Axis axis, 
                                                   PromotionReport report) {
        // Only rules for an exon are that it is parented AND that it has
        // an alignment of size >= 1
        if (this.getSuperFeature() == null) {
            report.addFatalError("Exon " + this.getOid() + 
                                 " cannot be promoted " + 
                                 " when it has no transcript parent");
        }

        GeometricAlignment exonAlignment = (GeometricAlignment) this.getOnlyAlignmentToAnAxis(
                                                   axis);

        if (exonAlignment == null) {
            report.addFatalError("Exon " + this.getOid() + 
                                 " is not aligned to axis, " + 
                                 " cannot be promoted");
        } else {
            if (Math.abs(exonAlignment.getRangeOnAxis().getMagnitude()) < 1) {
                report.addFatalError("Exon " + this.getOid() + 
                                     " alignment length cannot be less than 1. " + 
                                     "Length on axis " + axis.getOid() + 
                                     " is " + 
                                     exonAlignment.getRangeOnAxis()
                                                  .getMagnitude());
            }
        }

        return report;
    }

    //****************************************
    //*  Protected methods
    //****************************************
    private void addNewExonSpecificPromoteInstruction()
                                               throws SQLException {
        CallableStatement cs = null;

        try {
            String confidenceString = this.getProperty(
                                              ExonFacade.CONFIDENCE_PROP)
                                          .getInitialValue();
            int stmtIndex = 2;
            cs = this.getRdbmsAccess()
                     .prepareCallableStatement("{ ? = call api_writeback_pkg.add_new_exon(?,?,?) }", 
                                               this.getPromoteDatasourceName());
            cs.setBigDecimal(stmtIndex++, this.getPromoteOID().toBigDecimal());

            if ((confidenceString.equals("")) || (confidenceString == null)) {
                cs.setNull(stmtIndex++, Types.NUMERIC);
            } else {
                try {
                    cs.setFloat(stmtIndex++, Float.parseFloat(confidenceString));
                } catch (NumberFormatException nfEx) {
                    cs.setNull(stmtIndex++, Types.NUMERIC);
                }
            }

            cs.setBigDecimal(stmtIndex++, 
                             ((CuratedFeature) this.getSuperFeature()).getPromoteOID()
                                                                    .toBigDecimal());
            this.getRdbmsAccess()
                .executeUpdate(cs, this.getPromoteDatasourceName());
        } catch (Exception sqlEx) {
            ModelMgr.getModelMgr().handleException(sqlEx);

            // Rethrow so that caller gets indication of failure immediately
            throw new SQLException(sqlEx.getMessage());
        } finally {
            this.getRdbmsAccess()
                .executeComplete(cs, this.getPromoteDatasourceName());
        }
    }

    protected boolean supportsDirectEvidence() {
        return true;
    }

    /**
     * Templete pattern
     * Sub-Classes should overide this to set the proper mutator
     */
    protected GenomicEntityMutator constructMyMutator() {
        return new CuratedExonMutator();
    }

    /**
     * A more specific getMutator();
     */
    protected CuratedExonMutator getCuratedExonMutator() {
        return (CuratedExonMutator) this.getMutator();
    }

    //****************************************
    //*  Inner / Member classes
    //****************************************

    /**
     * The CuratedExonMutator class is the only way you can change the state of Feature
     * instances.
     * The CuratedExonMutator class is public, but it's constructor is private.
     */
    public class CuratedExonMutator extends CuratedFeatureMutator {
        /**
         * Protected constructor for the mutator class...
         */
        protected CuratedExonMutator() {
        }
    } // End of CuratedExonMutator member class.
}