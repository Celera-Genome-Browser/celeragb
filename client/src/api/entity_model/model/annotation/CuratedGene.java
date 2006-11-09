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

import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.annotations.GeneFacade;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.GenomicProperty;
import api.stub.data.InvalidPropertyFormat;
import api.stub.data.OID;
import api.stub.data.PromotionReport;
import api.stub.data.ReplacementRelationship;

import java.sql.CallableStatement;
import java.sql.SQLException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class CuratedGene extends CuratedFeature implements SuperFeature {
    private static final String SCRATCH_GENE_ACCESSION_PREFIX = "WG";

    //****************************************
    //*  Public methods
    //****************************************
    public CuratedGene(OID oid, String displayName, EntityType type, 
                       String discoveryEnvironment)
                throws InvalidFeatureStructureException {
        this(oid, displayName, type, discoveryEnvironment, null, null, 
             FeatureDisplayPriority.DEFAULT_PRIORITY);
    }

    public CuratedGene(OID oid, String displayName, EntityType type, 
                       String discoveryEnvironment, 
                       FacadeManagerBase readFacadeManager, 
                       Feature superFeature, byte displayPriority)
                throws InvalidFeatureStructureException {
        super(oid, displayName, type, discoveryEnvironment, readFacadeManager, 
              superFeature, displayPriority);
    }

    public String toString() {
        // if (DEBUG_SCRATCH_STATES) return this.getFeatureType() + " Feature " + this.getOID();
        GenomicProperty prop = getProperty(GeneFacade.GENE_ACCESSION_PROP);
        String accession = null;

        if (prop != null) {
            accession = prop.getInitialValue();
        }

        if (accession == null) {
            return "No Accession:" + this.getOid();
        } else {
            return " " + accession + " :" + this.getOid();
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
        if (!super.willAcceptSubFeature(newSubFeature)) {
            return false;
        }

        // Must be some kind of Transcript
        return (newSubFeature instanceof CuratedTranscript);
    }

    /**
     * Accepts visitors that implement GenomicEntityVisitor. acceptVisitorForSelf
     * is specialized by every subclass of GenomicEntity to call the appropriate
     * "visit...(...)" function on the passed visitor.
     * @param theVisitor the visitor.
     */
    public void acceptVisitorForSelf(GenomicEntityVisitor theVisitor) {
        try {
            theVisitor.visitCuratedGene(this);
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    //== Replacement / Change Trace Methods =========================================

    /**
      * Public method used to get the "Derived" ReplacementRelationships for this Gene.
      * The Derived replacement relationships do NOT include the "explicit" replaced
      * promoted OIDs that the Gene has, instead it has the "derived" replacement
      * relationships that can be calculated by tracing through this Gene's sub-features
      * (Transcripts) and through thier replacement relationships to the promoted
      * Transcripts, and then through thier super-feature to a promoted Gene.
      *
      * This method calls the super.getReplacementRelationship();
      *
      * One short-coming of "deriving" a Gene's ReplacementRelationship from
      * it's Transcript's ReplacementRelationships... is that if ALL the original
      * Transcripts have been removed from the Gene, we can NOT recreate the
      * relationship with the original Promoted Gene.
      *
      * For now when deriving the replacement relationship for the Gene, we will
      * depend on the fact that Add Subfeature and Remove Subfeature have done
      * thier job properly and updated the replacement type.
      *
      * @returns a ReplacementRelationship for this Feature that is "derived" through
      * the ReplacementRelationships of it's Transcripts.
      */
    public ReplacementRelationship getDerivedReplacementRelationship() {
        ReplacementRelationship explicitRepRel = this.getReplacementRelationship();

        Set secondaryDerivedReplacedGeneSet = new HashSet();
        Set derivedReplacedGeneSet = this.getDerivedReplacedGeneSet(
                                             secondaryDerivedReplacedGeneSet);

        // Make sure that the relationship type... no way to "derive" it...
        // For now when deriving the replacement relationship for the Gene, we will
        // depend on the fact that Add Subfeature and Remove Subfeature have done
        // thier job properly and updated the replacement type.
        // explicitRepRel.setNewReplacementType(ReplacementRelationship.TYPE_MODIFIED);
        // Remove all the existing replacementOIDs...
        OID[] explicitOIDs = explicitRepRel.getReplacementOIDs();

        for (int i = 0; i < explicitOIDs.length; i++) {
            explicitRepRel.removeReplacementOID(explicitOIDs[i]);
        }

        // If the "primary" derived replaced gene set is null or empty, try to use
        // the secondary replaced gene set.
        if ((derivedReplacedGeneSet == null) || 
                (derivedReplacedGeneSet.isEmpty())) {
            derivedReplacedGeneSet = secondaryDerivedReplacedGeneSet;
        }

        // If we have no derived replaced gene OIDs, simply return the explicit relationship...
        if (derivedReplacedGeneSet == null) {
            return explicitRepRel;
        }

        // Now add the derived replaced gene set...
        for (Iterator oidItr = derivedReplacedGeneSet.iterator();
             oidItr.hasNext();) {
            CuratedGene derivedReplacedGene = (CuratedGene) oidItr.next();


            //System.out.println("Adding derived replacement OID: " + derivedReplacedGene.getOid());
            explicitRepRel.addReplacementOID(derivedReplacedGene.getOid());
        }

        //check if the Genes transcripts have split/merge replaces type.
        boolean isChildSplitMerge = false;

        for (Iterator iter = this.getSubFeatures().iterator(); iter.hasNext();) {
            CuratedTranscript ct = (CuratedTranscript) iter.next();

            if (ct.getReplacementRelationshipType()
                  .equals(ReplacementRelationship.getTypeString(
                                  ReplacementRelationship.SPLIT)) || 
                    ct.getReplacementRelationshipType()
                      .equals(ReplacementRelationship.getTypeString(
                                      ReplacementRelationship.MERGE))) {
                isChildSplitMerge = true;

                break;
            }
        }

        // Now make sure that we don't have a "new" replacement relationship,
        // with a list of replaced oids and that if a gene has split or merge
        // children then set its replaces type to modified- promotion utility compatibiliy
        if (explicitRepRel.isReplacementType(ReplacementRelationship.NEW) && 
                (explicitRepRel.getNumberOfReplacementOIDs() > 0) && 
                isChildSplitMerge) {
            explicitRepRel.setNewReplacementType(
                    ReplacementRelationship.TYPE_MODIFIED);
        }

        return explicitRepRel;
    }

    /**
     * Public method used to get the "Derived" promoted Gene OID Set for this Gene.
     * The "derived" replacement relationships are calculated by tracing through
     * this Gene's sub-features (Transcripts) and through thier replacement
     * relationships to the promoted Transcripts, and then through thier
     * super-feature to a promoted Gene.
     *
     * Generalized Rules;
     * If THIS Workspace Gene has any subfeature Transcripts that replace
     * promoted Transcripts, THIS workspace Gene must replace SOME gene.
     *
     * Returns a Set of this Gene's derived promoted Gene OID Set.
     */
    public Set getDerivedReplacedGeneSet(Set secondaryDerivedReplacedGeneSet) {
        Set derivedReplacedGeneSet = new HashSet();
        GenomeVersion myGenomeVersion = this.getGenomeVersion();

        if (myGenomeVersion == null) {
            return null;
        }

        Workspace myWorkspace = myGenomeVersion.getWorkspace();

        if (myWorkspace == null) {
            return null;
        }

        // Get this Gene's transcripts... if this Gene is "obsoleted" we need to
        // ask the workspace for the list of "obsoleted" features under...
        Collection thisGenesTranscripts;

        if (this.isObsoleted()) {
            thisGenesTranscripts = myWorkspace.getObsoletedSubFeatureOfSuperFeature(
                                           this.getOid());
        } else {
            thisGenesTranscripts = this.getSubFeatures();
        }

        // If we have no transcripts... there can be no "derived" replacement relationship.
        if ((thisGenesTranscripts == null) || 
                (thisGenesTranscripts.isEmpty())) {
            return null;
        }

        // Iterate through this Gene's transcripts...
        CuratedTranscript thisGenesTranscript;
        ReplacementRelationship transRepRel = null;

        for (Iterator transItr = thisGenesTranscripts.iterator();
             transItr.hasNext();) {
            thisGenesTranscript = (CuratedTranscript) transItr.next();


            // Get this Transcript's replaced Promoted Transcripts...
            transRepRel = thisGenesTranscript.getReplacementRelationship();

            if (transRepRel != null) {
                OID[] transReplacementOIDs = transRepRel.getReplacementOIDs();

                for (int i = 0; i < transReplacementOIDs.length; i++) {
                    GenomicEntity promotedGE = myGenomeVersion.getGenomicEntityForOid(
                                                       transReplacementOIDs[i]);

                    if ((promotedGE != null) && 
                            (promotedGE instanceof CuratedTranscript)) {
                        CuratedTranscript promotedTranscript = 
                                (CuratedTranscript) promotedGE;
                        Feature promotedSuperFeature = 
                                promotedTranscript.getSuperFeature();

                        if ((promotedSuperFeature != null) && 
                                (promotedSuperFeature instanceof CuratedGene)) {
                            CuratedGene promotedGene = 
                                    (CuratedGene) promotedSuperFeature;

                            //System.out.println("Checking promoted Gene(" + promotedGene.getOid() + ")...");
                            // See if this promoted gene is explicitly "replaced" in the Workspace...
                            if (!myWorkspace.isPromotedReplacedByScratch(
                                         promotedGene)) {
                                // If it's not explicitly replaced, then this gene should replace it
                                // through it's derived relationship.
                                derivedReplacedGeneSet.add(promotedGene);
                            } else {
                                // Add this gene to the secondary list, incase the primary list is empty...
                                secondaryDerivedReplacedGeneSet.add(
                                        promotedGene);

                                // or if the workspaceGene that replaces it has no sub-features...
                                boolean foundSubFeatures = false;
                                ChangeTrace derivedChangeTrace = 
                                        myWorkspace.getChangeForPromotedOid(
                                                promotedGene.getOid());
                                Set workspaceGeneOids = 
                                        derivedChangeTrace.getWorkspaceOids();

                                for (Iterator wsGeneOidItr = 
                                        workspaceGeneOids.iterator();
                                     wsGeneOidItr.hasNext();) {
                                    GenomicEntity wsGeneGE = 
                                            myGenomeVersion.getLoadedGenomicEntityForOid(
                                                    (OID) wsGeneOidItr.next());

                                    // Because we are deriving from scratch we need to include ourselves...
                                    if (wsGeneGE == this) {
                                        derivedReplacedGeneSet.add(promotedGene);
                                    }

                                    if ((wsGeneGE instanceof CuratedGene) && 
                                            (((CuratedGene) wsGeneGE).hasSubFeatures())) {
                                        foundSubFeatures = true;
                                    }
                                }

                                // If there were no subfeatures of the Gene,
                                // then the Gene is orphaned...
                                if (!foundSubFeatures) {
                                    // If it's not explicitly replaced, then this gene should replace it
                                    // through it's derived relationship.
                                    derivedReplacedGeneSet.add(promotedGene);
                                }
                            }
                        }
                    }
                }
            }
        }

        return derivedReplacedGeneSet;
    }

    public PromotionReport doBuildPromoteInstructions(Axis axis, 
                                                      String parentReplacesDirective)
                                               throws SQLException {
        // First need to determine for this Gene whether the action being performed
        // by promotion is:
        //  1. Create a new gene
        //  2. Update an existing gene
        //  3. Obsolete (effectively delete) a gene
        //  4. Too many changes to specify replace the existing gene
        //     with a new modified copy.
        //
        // Option 4 will be implemented first and if it turns out to be fast
        // enough then it may be the only option implemented
        PromotionReport report = new PromotionReport(true, "");
        boolean shouldUpdateChildAssociations = false;

        String replacesType = this.getReplacementRelationship()
                                  .getReplacementType();

        //System.out.println("Promoting gene with replaces of " + replacesType);
        String directiveToMyChildren = ReplacementRelationship.TYPE_UNMODIFIED;

        if ((replacesType.equals(ReplacementRelationship.TYPE_SPLIT)) || 
                (replacesType.equals(ReplacementRelationship.TYPE_MERGE))) {
            report.addFatalError("Gene " + this.getOid() + 
                                 " had been split or " + 
                                 " merged, thesse operations are only legal for transcripts");
        } else if (replacesType.equals(ReplacementRelationship.TYPE_NEW)) {
            addBaseCuratedFeatureRepresentation(axis, "gene");
            addNewGeneSpecificPromoteInstruction();
            shouldUpdateChildAssociations = true;
        } else if (replacesType.equals(ReplacementRelationship.TYPE_OBSOLETE)) {
            // Obsolete all alignments, relationship and feature_display
            // rows for the gene and then ask each child to specify it's own
            // obsolete requirements.
            addObsoleteInstructions(axis, "gene");
        } else if ((replacesType.equals(ReplacementRelationship.TYPE_DEEP_MOD)) || 
                       (replacesType.equals(
                               ReplacementRelationship.TYPE_MODIFIED)) || 
                       (parentReplacesDirective.equals(
                               ReplacementRelationship.TYPE_MODIFIED))) {
            // Throw everything away and start again...BUT keep the same accession
            // number
            addMakeDeepCopyPromoteInstructions(axis, "gene");
            addNewGeneSpecificPromoteInstruction();
            shouldUpdateChildAssociations = true;
            directiveToMyChildren = ReplacementRelationship.TYPE_MODIFIED;
        } else if (replacesType.equals(ReplacementRelationship.TYPE_UNMODIFIED)) {
            // Update the date_curated and curated_by field in Feature_Curation table
            // to keep a trace of the person who has touch the feature.
            updateFeatureCurationModified();
        } else {
            report.addFatalError(
                    "Encountered an unknown replaces type for gene " + 
                    this.getOid() + " assuming XML file is corrupt.");
        }

        // Children may be changed even if parent is not so go ahead and
        // ask then to add thier promotion instructions
        if (report.wasPromotable()) {
            report.incorperateSubReport(promoteChildrenOn(axis, 
                                                          directiveToMyChildren));
        }

        // Finally need to update representation of my associations to my children
        // as defined by the promotion. Need to do this as the last step because
        // the children need to have updated themselves before we start
        // referring to them
        if ((report.wasPromotable()) && shouldUpdateChildAssociations) {
            addNewChildAssociationsInstructions(axis);
        }

        return report;
    }

    private void addNewGeneSpecificPromoteInstruction()
                                               throws SQLException {
        CallableStatement cs = null;

        try {
            OID[] replacedOIDS = this.getReplacementRelationship()
                                     .getReplacementOIDs();

            // If this gene does not replace another then we will need a new
            // accession number
            int stmtIndex = 2;
            String accessionNumber = null;

            if (replacedOIDS.length == 0) {
                accessionNumber = this.generateNewAccessionNumber();
                cs = this.getRdbmsAccess()
                         .prepareCallableStatement("{ ? = call api_writeback_pkg.add_new_gene(?,?,?,?) }", 
                                                   this.getPromoteDatasourceName());

                cs.setBigDecimal(stmtIndex++, 
                                 this.getPromoteOID().toBigDecimal());
            } else {
                // If the gene has an accession that is not scratch then reuse it
                // otherwise get a new accession number
                accessionNumber = this.getProperty(
                                          GeneFacade.GENE_ACCESSION_PROP)
                                      .getInitialValue();

                if (accessionNumber.startsWith(SCRATCH_GENE_ACCESSION_PREFIX)) {
                    // A gene should not be marked modified but have a scratch accession
                    // if this happens stop the promotion by raising a RuntimeException
                    if (this.getReplacementRelationship().getReplacementType()
                            .equals(ReplacementRelationship.TYPE_MODIFIED)) {
                        // Allow a new accession number to be generated if any of my children
                        // were split or merged.
                        if (!anyChildSplitOrMerged()) {
                            throw new RuntimeException(
                                    "Invalid scratch accession number " + 
                                    " in promotion file for a MODIFIED gene of " + 
                                    accessionNumber + 
                                    " please fix the file and re-promote");
                        }
                    }

                    accessionNumber = generateNewAccessionNumber();
                }

                cs = this.getRdbmsAccess()
                         .prepareCallableStatement("{ ? = call api_writeback_pkg.add_replacement_gene(?,?,?,?,?) }", 
                                                   this.getPromoteDatasourceName());

                cs.setBigDecimal(stmtIndex++, 
                                 this.getPromoteOID().toBigDecimal());
                cs.setBigDecimal(stmtIndex++, replacedOIDS[0].toBigDecimal());
            }

            cs.setString(stmtIndex++, accessionNumber);

            GenomicProperty alterSpliceProp = this.getProperty(
                                                      GeneFacade.IS_ALTER_SPLICE_PROP);
            String spliceString = null;

            if (alterSpliceProp != null) {
                spliceString = alterSpliceProp.getInitialValue();
            }

            int spliceAsInt = 0;

            if (spliceString != null) {
                Boolean tempBool = new Boolean(spliceString);

                if (tempBool.booleanValue()) {
                    spliceAsInt = 1;
                }
            }

            GenomicProperty psuedoProp = this.getProperty(
                                                 GeneFacade.IS_PSEUDO_GENE_PROP);
            String pseudoString = null;

            if (psuedoProp != null) {
                pseudoString = psuedoProp.getInitialValue();
            }

            int pseudoAsInt = 0;

            if (pseudoString != null) {
                Boolean tempBool = new Boolean(pseudoString);

                if (tempBool.booleanValue()) {
                    pseudoAsInt = 1;
                }
            }

            cs.setInt(stmtIndex++, spliceAsInt);
            cs.setInt(stmtIndex++, pseudoAsInt);


            //cs.setString(stmtIndex++,this.rdbmsAccess.getRdbmsSecurityId());
            this.getRdbmsAccess()
                .executeUpdate(cs, this.getPromoteDatasourceName());


            // Update any comments that the gene has
            updateComments();
        } catch (Exception sqlEx) {
            //System.out.println("Got exception of " + sqlEx);
            // Log what happened to we can see why the promote failed
            ModelMgr.getModelMgr().handleException(sqlEx);

            // Rethrow so that caller gets indication of failure immediately
            throw new SQLException(sqlEx.getMessage());
        } finally {
            this.getRdbmsAccess()
                .executeComplete(cs, this.getPromoteDatasourceName());
        }
    }

    private PromotionReport promoteChildrenOn(Axis axis, 
                                              String parentReplacesDirective)
                                       throws SQLException {
        PromotionReport childrenReport = new PromotionReport(true, "");

        Collection children = this.getPromotionChildrenIncludingObsoletes();

        // Should not get this far if gene has no children because
        // checkPromotable would have failed so not checking again here
        CuratedFeature currentCuration = null;
        Iterator childIter = children.iterator();

        while (childIter.hasNext()) {
            currentCuration = (CuratedFeature) childIter.next();
            childrenReport.incorperateSubReport(
                    currentCuration.buildPromoteInstructions(
                            this.getPromotionGenomeVersion(), axis, 
                            parentReplacesDirective, this.getRdbmsAccess(), 
                            this.getReviewAuthority(), 
                            this.getDataLayerForObsolete(), 
                            this.getDataLayerForPromote()));
        }

        return childrenReport;
    }

    public PromotionReport doCheckPromotableOnAxis(Axis axis, 
                                                   PromotionReport report) {
        // First ensure that the Gene has at least one child, use the child
        // list that excludes obsoleted children
        Collection children = this.getSubStructure();

        if (children.size() == 0) {
            report.addFatalError("Invalid gene " + getOid() + 
                                 ". Every gene must " + 
                                 " define at least one child transcript ");
        }

        // Make sure the gene is not parented
        if (this.getSuperFeature() != null) {
            report.addFatalError("Invalid gene " + getOid() + 
                                 ". Genes cannot be " + 
                                 " parented. This gene is parented by feature " + 
                                 this.getSuperFeature().getOid());
        }

        // Check that all children are transcripts and that the transcripts
        // have an alignment to the axis that is withing the bounds of this gene
        CuratedTranscript currentTranscript = null;
        GeometricAlignment currentChildAlignment = null;
        Iterator childIter = children.iterator();
        GeometricAlignment myAlignment = (GeometricAlignment) this.getOnlyAlignmentToAnAxis(
                                                 axis);

        while (childIter.hasNext()) {
            try {
                CuratedFeature tempFeat = (CuratedFeature) childIter.next();
                currentTranscript = (CuratedTranscript) tempFeat;


                // Is the geometry of each child within the geometry of this gene
                // First off current rule is that each feature is only aligned once
                // to a given axis
                currentChildAlignment = (GeometricAlignment) currentTranscript.getOnlyAlignmentToAnAxis(
                                                axis);

                if ((currentChildAlignment != null) && 
                        (myAlignment != null)) {
                    // Check that all my children are in the same orientation as me
                    if ((myAlignment.orientationForwardOnAxis()) != (currentChildAlignment.orientationForwardOnAxis())) {
                        report.addFatalError("Transcript " + 
                                             currentTranscript.getOid() + 
                                             " cannot be in a different orientation than it's gene " + 
                                             " parent " + this.getOid() + 
                                             ".");
                    }

                    try {
                        if (!myAlignment.containsInAxisCoords(
                                     currentChildAlignment)) {
                            report.addFatalError("Transcript " + 
                                                 currentTranscript.getOid() + 
                                                 " does not fall geometrically within the bounds of it's gene " + 
                                                 " parent " + this.getOid() + 
                                                 ".");
                        }
                    } catch (IllegalArgumentException argEx) {
                        report.addFatalError("Transcript " + 
                                             currentTranscript.getOid() + 
                                             " could not be checked for geometric containment to it's " + 
                                             " gene parent " + this.getOid() + 
                                             " because: " + 
                                             argEx.getMessage());
                    }
                } else {
                    report.addFatalError("Gene " + this.getOid() + 
                                         " and all of it's transcripts are not each " + 
                                         " aligned only once to the promotion axis with axis OID: " + 
                                         axis.getOid());
                }


                // Ask each transcript to validate itself as ready for promotion
                report.incorperateSubReport(currentTranscript.checkPromotableOn(
                                                    this.getPromotionGenomeVersion(), 
                                                    axis));
            } catch (ClassCastException castEx) {
                report.addFatalError("Gene " + this.getOid() + 
                                     " has a child " + 
                                     currentTranscript.getOid() + 
                                     " that is not a Transcript. Gene can only have transcript as" + 
                                     " children");
            }
        }

        return report;
    }

    private boolean anyChildSplitOrMerged() {
        // Use getSubStructure as dont want to include obsoleted children in
        // this test
        Collection children = this.getSubStructure();
        CuratedFeature currentChild = null;
        boolean splitOrMergeChildFound = false;
        String currentReplType = null;
        Iterator childIter = children.iterator();

        while (childIter.hasNext()) {
            currentChild = (CuratedFeature) childIter.next();
            currentReplType = currentChild.getReplacementRelationship()
                                          .getReplacementType();

            if ((currentReplType.equals(ReplacementRelationship.TYPE_MERGE)) || 
                    (currentReplType.equals(ReplacementRelationship.TYPE_SPLIT))) {
                splitOrMergeChildFound = true;

                break;
            }
        }

        return splitOrMergeChildFound;
    }

    //****************************************
    //*  Protected methods
    //****************************************

    /**
     * Templete pattern
     * Sub-Classes should overide this to set the proper mutator
     */
    protected GenomicEntityMutator constructMyMutator() {
        return new CuratedGeneMutator();
    }

    /**
     * A more specific getMutator();
     */
    protected CuratedGeneMutator getCuratedGeneMutator() {
        return (CuratedGeneMutator) this.getMutator();
    }

    //****************************************
    //*  Private methods
    //****************************************
    //****************************************
    //*  Inner / Member classes
    //****************************************

    /**
     * The CuratedGeneMutator class is the only way you can change the state of Feature
     * instances.
     * The CuratedGeneMutator class is public, but it's constructor is private.
     */
    public class CuratedGeneMutator extends CuratedFeatureMutator {
        /**
         * Protected constructor for the mutator class...
         */
        protected CuratedGeneMutator() {
        }

        public void setProperty(String propertyName, String propertyValue)
                         throws InvalidPropertyFormat {
            super.setProperty(propertyName, propertyValue);

            // also make sure that whenever the property
            // name is Gene Accession Property for Gene then
            // all the transcripts must have that same gene
            // accession property
            if (GeneFacade.GENE_ACCESSION_PROP.equals(propertyName)) {
                for (Iterator iter = CuratedGene.this.getSubFeatures()
                                                     .iterator();
                     iter.hasNext();) {
                    CuratedTranscript ct = (CuratedTranscript) iter.next();
                    ct.getCuratedTranscriptMutator()
                      .setProperty(TranscriptFacade.GENE_ACCESSION_PROP, 
                                   propertyValue);
                }
            }
        }
    } // End of CuratedGeneMutator member class.
}