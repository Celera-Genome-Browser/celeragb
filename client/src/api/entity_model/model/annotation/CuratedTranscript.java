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
import api.entity_model.management.GenomicEntityFactory;
import api.entity_model.management.ModelMgr;
import api.entity_model.management.PropertyMgr;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.fundtype.MutableAlignment;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.GenomicProperty;
import api.stub.data.InvalidPropertyFormat;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;
import api.stub.data.PromotionReport;
import api.stub.data.ReplacementRelationship;
import api.stub.geometry.MutableRange;
import api.stub.geometry.Range;
import api.stub.sequence.DNA;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * Title:        Genome Browser<p>
 * Description:  <p>
 * @author Jay T. Schira
 * @version $Id$
 */
public class CuratedTranscript extends CuratedFeature implements SuperFeature,
                                                                 SubFeature {
    private static boolean DEBUG_CLASS = false;
    static String[] STOP_STRINGS = {
        new String("TAA"), new String("TGA"), new String("TAG")
    };
    static String START_STRING = "ATG";
    private static final String MOD_TYPE_INSERT = "I";
    private static final String MOD_TYPE_REPLACE = "R";
    private static final String MOD_TYPE_UPDATE = "U";
    private static final String MOD_TYPE_DELETE = "D";
    private static final String WRONG_ACCESSION_NUMBER = "hCT0";
    private static final String NULL_ACCESSION_NUMBER = "hCTnull";
    private static final String WORKSPACE_TRAN_PREFIX = "WT";
    CuratedCodon startCodon = null;
    CuratedCodon stopCodon = null;

    //****************************************
    //*  Public methods
    //****************************************
    //================================ Constructors ===============================
    public CuratedTranscript(OID oid, String displayName, EntityType type, 
                             String discoveryEnvironment)
                      throws InvalidFeatureStructureException {
        this(oid, displayName, type, discoveryEnvironment, null, null, 
             FeatureDisplayPriority.DEFAULT_PRIORITY);
    }

    public CuratedTranscript(OID oid, String displayName, EntityType type, 
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
        // We always alow null...
        if (newSuperFeature == null) {
            return true;
        }

        // Now check with super...
        if (!super.willAcceptSuperFeature(newSuperFeature)) {
            return false;
        }

        // Must be some kind of Gene
        return (newSuperFeature instanceof CuratedGene);
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

        // Must be some kind of Exon...
        return ((newSubFeature instanceof CuratedExon) || 
               (newSubFeature instanceof CuratedCodon));
    }

    //== Visitation Methods ==============================

    /**
     * Accepts visitors that implement GenomicEntityVisitor. acceptVisitorForSelf
     * is specialized by every subclass of GenomicEntity to call the appropriate
     * "visit...(...)" function on the passed visitor.
     * @param theVisitor the visitor.
     */
    public void acceptVisitorForSelf(GenomicEntityVisitor theVisitor) {
        try {
            theVisitor.visitCuratedTranscript(this);
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * Accepts visitors that implement GenomicEntityVisitor.
     * @param theVisitor the visitor.
     * @param directSubFeaturesOnly - if true will only walk to this features subFeatures
     *    if false, will walk to this features subFeaturesand any subFeatures of those features etc
     *    until no more features exist
     * This is misleading, in that start and stop codons are NOT sub-features, but
     * they will be visited.
     * @todo: Codons - CLEAN-UP naming from acceptVisitorForSubFeatures() to acceptVisitorForFeatureStructures
     */
    public void acceptVisitorForSubFeatures(GenomicEntityVisitor theVisitor, 
                                            boolean directSubFeaturesOnly) {
        try {
            // This will visit all the formal sub-features.
            super.acceptVisitorForSubFeatures(theVisitor, directSubFeaturesOnly);

            // Visit the start codon if we have one.
            if (startCodon != null) {
                startCodon.acceptVisitorForSelf(theVisitor);

                if (!directSubFeaturesOnly) {
                    startCodon.acceptVisitorForSubFeatures(theVisitor, false);
                }
            }

            // Visit the stop codon if we have one.
            if (stopCodon != null) {
                stopCodon.acceptVisitorForSelf(theVisitor);

                if (!directSubFeaturesOnly) {
                    stopCodon.acceptVisitorForSubFeatures(theVisitor, false);
                }
            }
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    public String toString() {
        // if (DEBUG_SCRATCH_STATES) return this.getFeatureType() + " Feature " + this.getOID();
        GenomicProperty prop = getProperty(
                                       TranscriptFacade.TRANSCRIPT_ACCESSION_PROP);
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

    /**
     * Get the Exon for a position.
     * Use the Axis in the only alignment of this CuratedTranscript
     */
    public CuratedExon getExonForPositionOnAxis(int positionOnAxis) {
        GeometricAlignment myAlign = this.getOnlyGeometricAlignmentToOnlyAxis();

        if (myAlign == null) {
            return null;
        }

        return (CuratedExon) this.getSubFeatureAtPositionOnAxis(
                       myAlign.getAxis(), positionOnAxis);
    }

    /**
     * Get the CuratedExon that contains the start codon's start position.
     * This method will return null under the following conditions;
     * - if there is no start codon
     * - if there is no geometric alignment for the start codon
     * - if there is no Exon that contains the start codon position
     */
    public CuratedExon getExonForStartCodon() {
        if (this.startCodon == null) {
            return null;
        }

        GeometricAlignment codonAlign = startCodon.getOnlyGeometricAlignmentToOnlyAxis();

        if (codonAlign == null) {
            return null;
        }

        return (CuratedExon) this.getSubFeatureAtPositionOnAxis(
                       codonAlign.getAxis(), 
                       codonAlign.getRangeOnAxis().getStart());
    }

    /**
     * Get the CuratedExon that contains the stop codon's start position.
     * This method will return null under the following conditions;
     * - if there is no stop codon
     * - if there is no geometric alignment for the stop codon
     * - if there is no Exon that contains the stop codon position
     */
    public CuratedExon getExonForStopCodon() {
        if (this.stopCodon == null) {
            return null;
        }

        GeometricAlignment codonAlign = stopCodon.getOnlyGeometricAlignmentToOnlyAxis();

        if (codonAlign == null) {
            return null;
        }

        return (CuratedExon) this.getSubFeatureAtPositionOnAxis(
                       codonAlign.getAxis(), 
                       codonAlign.getRangeOnAxis().getStart());
    }

    /**
     * A package visible method to set the start codon.
     * This is used only during construction.
     * After construction, the mutator methods should be used.
     */
    void setStartOrStopCodon(CuratedCodon newCodon) {
        CuratedCodon.CuratedCodonMutator codonMutator;

        if (newCodon == null) {
            return;
        }

        // Determine if it's a start or stop...
        if (newCodon.isStartCodon()) {
            // Have a previous start codon...
            if (startCodon != null) {
                codonMutator = startCodon.getCuratedCodonMutator();
                codonMutator.setHostTranscript(null);
                codonMutator.removeOnlyAlignmentToOnlyAxis();
            }

            startCodon = newCodon;
        } else if (newCodon.isStopCodon()) {
            // Have a previous start codon...
            if (stopCodon != null) {
                codonMutator = startCodon.getCuratedCodonMutator();
                codonMutator.setHostTranscript(null);
                codonMutator.removeOnlyAlignmentToOnlyAxis();
            }

            stopCodon = newCodon;
        } else {
            // Don't recognize this codon...
            return;
        }


        // Setup the new back references...
        codonMutator = newCodon.getCuratedCodonMutator();
        codonMutator.setHostTranscript(this);
    }

    /**
     * Get the start codon for the transcript. Return null if there is no start.
     */
    public CuratedCodon getStartCodon() {
        return startCodon;
    }

    /**
     * Should only be called DIRECTLY during construction, or should be called
     * from the mutator.
     * This method does NOT trigger notification or re-calcuations.
     */
    void protectedSetStartCodon(CuratedCodon newStartCodon) {
        this.startCodon = newStartCodon;

        if (newStartCodon != null) {
            newStartCodon.protectedSetHostTranscript(this);
        }
    }

    /**
     * Convenience method to check if we have a start codon.
     */
    public boolean hasStartCodon() {
        return (startCodon != null);
    }

    /**
     * Get the stop codon for the transcript. Return null if ther is no stop.
     * Not implemented yet.
     */
    public CuratedCodon getStopCodon() {
        return stopCodon;
    }

    /**
     * Should only be called DIRECTLY during construction, or should be called
     * from the mutator.
     * This method does NOT trigger notification or re-calcuations.
     */
    void protectedSetStopCodon(CuratedCodon newStopCodon) {
        this.stopCodon = newStopCodon;

        if (newStopCodon != null) {
            newStopCodon.protectedSetHostTranscript(this);
        }
    }

    /**
     * Convenience method to check if we have a stop codon.
     */
    public boolean hasStopCodon() {
        return (stopCodon != null);
    }

    /**
     * Provide a collection of sub structural features.
     * This "sub-structure" relationship is completely general and has no
     * implications other than whole-part.
     */
    public Collection getSubStructure() {
        Collection subStructure = super.getSubStructure();

        if (startCodon != null) {
            subStructure.add(startCodon);
        }

        if (stopCodon != null) {
            subStructure.add(stopCodon);
        }

        return subStructure;
    }

    /**
     * Check to see if the start codon will be left "naked" given a proposed
     * change to an existing exon's range.
     * Note: start and stop codons can be split across exons. For example for a
     * start, the AT can be in one exon and the G in another. The only restriction
     * is that the 1st position on the start must be in an exon.
     * Note that the stop codon position is recomputed when an exon boundary
     * changes so its position doesn't need to be checked for validity here.
     * If there is no current start codon, this method will return false.
     * If the exon is NOT a sub-feature of this transcript, this method will return false.
     * If the Transcript has no current alignment, thre is no start codon to invalidate
     * so this method will return false.
     * If the argument exon is not a sub-feature of this transcript, this method
     * will return false;
     */
    public boolean exonRangeChangeWillLeaveNakedStartCodon(CuratedExon existingExon, 
                                                           Range proposedExonRange) {
        if (DEBUG_CLASS) {
            System.out.println(
                    "exonRangeChangeWillLeaveNakedStartCodon(existingExon=" + 
                    existingExon + ", newExonRange=" + proposedExonRange);
        }

        // Check the args...
        if ((existingExon == null) || (proposedExonRange == null)) {
            return false;
        }

        // Check to see if this transcript has the exon as a sub-feature...
        if (!this.containsSubFeature(existingExon)) {
            return false;
        }

        // Get the start codon and check for null...
        CuratedCodon startCodon = this.getStartCodon();

        if (startCodon == null) {
            return false;
        }

        // Get the exon for the start Codon...
        CuratedExon startExon = this.getExonForStartCodon();

        if ((startCodon != null) && (startExon == existingExon)) {
            //make sure that the start codon is still contained in the exon
            GeometricAlignment startCodonAlignment = startCodon.getOnlyGeometricAlignmentToOnlyAxis();

            if (startCodonAlignment == null) {
                System.err.println(
                        "Error: No alignment found for start codon = " + 
                        startCodon);

                return false;
            }

            Range startCodonRange = startCodonAlignment.getRangeOnAxis();

            GeometricAlignment startExonAlignment = startExon.getOnlyGeometricAlignmentToOnlyAxis();

            if (startExonAlignment == null) {
                System.err.println(
                        "Error: No alignment found for start exon = " + 
                        startExon);

                return false;
            }

            // Build a range for the start codon, that is only 1 base pair long.
            int startCodonPosOffset = (startExonAlignment.getRangeOnAxis().isReversed())
                                      ? (-1) : 1;
            Range startCodonPosRange = new MutableRange(
                                               startCodonRange.getStart(), 
                                               startCodonRange.getStart() + 
                                               startCodonPosOffset);

            //System.out.println("CuratedTranscriptFeatPI.isStartCodonValid: srng=" + srng + " newRange=" + newRange);
            // See if the new exon range still contains the start codon...
            if (!proposedExonRange.contains(startCodonPosRange)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Given a start position within the spliced transcript, and a residue string for the spliced transcript,
     * returns position of first stop codon that is in same frame as start if no stop codon is found in frame, returns -1
     * Use transcript.getSplicedResidues().toString(); instead of Arg.
     */
    public static int getNextInFrameStopPosition(int in_frame_start, 
                                                 String spliced_residues) {
        int frame = in_frame_start % 3;
        int min_stop_index = -1;
        int pos;
        String stop_str;

        for (int i = 0; i < STOP_STRINGS.length; i++) {
            stop_str = STOP_STRINGS[i];

            int current_index = in_frame_start;

            while (current_index < spliced_residues.length()) {
                pos = spliced_residues.indexOf(stop_str, current_index);

                if (pos < 0) {
                    break;
                } // no more stop_str in spliced_residues

                if ((pos % 3) == frame) {
                    if ((min_stop_index == -1) || (pos < min_stop_index)) {
                        min_stop_index = pos;

                        break;
                    }
                }

                current_index = pos + 1;
            }
        }

        return min_stop_index;
    }

    /**
      * Given a stop position within the spliced transcript, and a residue string for the spliced transcript,
      * returns position of first start codon that is in same frame as stop(reading the residues left to right) if no start codon is found in frame, returns -1
      * Use transcript.getSplicedResidues().toString(); instead of Arg.
      *
      */
    public static int getNextInFrameStartPosition(int in_frame_stop, 
                                                  String spliced_residues) {
        int frame = in_frame_stop % 3;
        int max_start_index = -1;
        int pos;
        int stopPos;
        int current_index = in_frame_stop;

        while (current_index > 0) {
            pos = spliced_residues.substring(0, current_index)
                                  .lastIndexOf(START_STRING);

            if ((pos % 3) == frame) {
                //see if there are no stops in the substring between pos and current_index
                for (int i = 0; i < STOP_STRINGS.length; i++) {
                    stopPos = spliced_residues.substring(pos, current_index)
                                              .lastIndexOf(STOP_STRINGS[i]);

                    if ((pos < current_index) && (stopPos < 0)) {
                        max_start_index = pos;

                        return max_start_index;
                    }
                }
            }

            current_index = pos - 1;
        }

        return max_start_index;
    }

    /**
     * Given a stop position within the spliced transcript, and a residue string for the spliced transcript,
     * returns position of first stop codon that is in same frame as stop(reading the residues left to right) if no stop codon is found in frame, returns -1
     * Use transcript.getSplicedResidues().toString(); instead of Arg.
     *
     */
    public static int getNextInFrameStopPositionGivenStop(int in_frame_stop, 
                                                          String spliced_residues) {
        int frame = in_frame_stop % 3;
        int max_stop_index = -1;
        int pos = 0;
        int pos1;
        int pos2;
        int pos3;
        int stopPos;
        int current_index = in_frame_stop;

        while (current_index > 0) {
            pos1 = spliced_residues.substring(0, current_index)
                                   .lastIndexOf(STOP_STRINGS[0]);
            pos2 = spliced_residues.substring(0, current_index)
                                   .lastIndexOf(STOP_STRINGS[1]);
            pos3 = spliced_residues.substring(0, current_index)
                                   .lastIndexOf(STOP_STRINGS[2]);

            //find max od pos1, pos2, pos3
            if (pos1 < pos2) {
                if (pos2 < pos3) {
                    pos = pos3;
                } else {
                    pos = pos2;
                }
            } else if (pos2 < pos3) {
                if (pos3 < pos1) {
                    pos = pos1;
                } else {
                    pos = pos3;
                }
            } else if (pos3 < pos1) {
                if (pos1 < pos2) {
                    pos = pos2;
                } else {
                    pos = pos1;
                }
            }

            if ((pos % 3) == frame) {
                //see if there are no stops in the substring between pos and current_index
                for (int i = 0; i < STOP_STRINGS.length; i++) {
                    stopPos = spliced_residues.substring(pos, current_index)
                                              .lastIndexOf(STOP_STRINGS[i]);

                    if ((pos < current_index) && (stopPos < 0)) {
                        max_stop_index = pos;

                        return max_stop_index;
                    }
                }
            }

            current_index = pos - 1;
        }

        return max_stop_index;
    }

    /**
     * Overrides from Curated Feature that deal with promoting the state of
     * a curated feature to a persistent store.
     */
    public PromotionReport doCheckPromotableOnAxis(Axis axis, 
                                                   PromotionReport report) {
        // First ensure that the Transcript has at least one child and is parented
        // Use getSubStructure as dont want to include obsoleted children in this
        // test
        Collection children = this.getSubStructure();

        if (children.size() == 0) {
            report.addFatalError("Invalid transcript " + getOid() + 
                                 ". Every transcript must " + 
                                 " define at least one exon child ");
        }

        if (this.getSuperFeature() == null) {
            report.addFatalError("Invalid transcript " + getOid() + 
                                 ". Transcript must " + 
                                 " have a gene parent. ");
        }

        GeometricAlignment myAlignment = (GeometricAlignment) this.getOnlyAlignmentToAnAxis(
                                                 axis);

        // Check that all children are CuratedExon's or CuratedCodons and that each child
        // have an alignment to the axis that is withing the bounds of the alignment
        // of the transcript
        CuratedFeature currentChild = null;
        GeometricAlignment currentChildAlignment = null;
        int numCuratedExons = 0;
        int numStartCodons = 0;
        GeometricAlignment startCodonAlignment = null;
        int numStopCodons = 0;
        GeometricAlignment stopCodonAlignment = null;
        List exonAlignments = new ArrayList();
        String childReplacesType = null;
        Iterator childIter = children.iterator();

        while (childIter.hasNext()) {
            try {
                currentChild = (CuratedFeature) childIter.next();


                // If the currentChild feature is being obsoleted then exclude it
                // from all tests
                childReplacesType = currentChild.getReplacementRelationship()
                                                .getReplacementType();

                if (childReplacesType.equals(
                            ReplacementRelationship.TYPE_OBSOLETE)) {
                    continue;
                }


                // Is the geometry of each child within the geometry of this gene?
                currentChildAlignment = (GeometricAlignment) currentChild.getOnlyAlignmentToAnAxis(
                                                axis);

                try {
                    if (!myAlignment.containsInAxisCoords(currentChildAlignment)) {
                        report.addFatalError("\nChild " + 
                                             currentChild.getOid() + 
                                             " does not fall geometrically within the bounds of it's transcript " + 
                                             " parent " + this.getOid() + 
                                             ".");
                    }
                } catch (IllegalArgumentException argEx) {
                    report.addFatalError("Child " + currentChild.getOid() + 
                                         " could not be checked for geometric containment to it's " + 
                                         " transcript parent " + 
                                         this.getOid() + " because: " + 
                                         argEx.getMessage());
                }

                int typeValue = currentChild.getEntityType().value();

                switch (typeValue) {
                case EntityTypeConstants.Start_Codon_Start_Position:
                case EntityTypeConstants.Translation_Start_Position:
                {
                    ++numStartCodons;
                    startCodonAlignment = currentChildAlignment;

                    //}
                }

                break;

                case EntityTypeConstants.Exon:
                    ++numCuratedExons;
                    exonAlignments.add(currentChildAlignment);

                    break;

                case EntityTypeConstants.StopCodon:
                    ++numStopCodons;
                    stopCodonAlignment = currentChildAlignment;

                    break;
                }

                // Check that all my children are in the same orientation as me
                if ((myAlignment.orientationForwardOnAxis()) != (currentChildAlignment.orientationForwardOnAxis())) {
                    report.addFatalError("Child " + currentChild.getOid() + 
                                         " cannot be in a different orientation than it's transcript " + 
                                         " parent " + this.getOid() + ".");
                }


                // Ask each child entity to validate itself as ready for promotion
                report.incorperateSubReport(currentChild.checkPromotableOn(
                                                    this.getPromotionGenomeVersion(), 
                                                    axis));
            } catch (ClassCastException castEx) {
                report.addFatalError("Transcript " + this.getOid() + 
                                     " has a child " + currentChild.getOid() + 
                                     " that is not a CuratedFeature. Transcript can only have curated feautures as" + 
                                     " children");
            }
        }

        if (numStartCodons != 1) {
            report.addFatalError(
                    "\nTranscript must have only one start codon or" + 
                    " translation start position. Transcript " + 
                    this.getOid() + " contained " + numStartCodons + 
                    " start codons.");
        } else {
            // Also check that the alignment of the start codon falls within the
            // alignment of one of the exons. While we are at it check that
            // no exons overlap
            Iterator alignIter = exonAlignments.iterator();
            GeometricAlignment exonAlign = null;
            boolean startCodonInExonBounds = false;
            boolean noOverlappingExonFound = true;
            GeometricAlignment previousExonAlign = null;

            while ((alignIter.hasNext()) && noOverlappingExonFound) {
                exonAlign = (GeometricAlignment) alignIter.next();

                try {
                    if (exonAlign.intersectsInAxisCoords(startCodonAlignment)) {
                        startCodonInExonBounds = true;
                    }

                    if ((previousExonAlign != null) && 
                            (previousExonAlign.intersectsInAxisCoords(exonAlign))) {
                        noOverlappingExonFound = false;
                        report.addFatalError("Exon " + 
                                             previousExonAlign.getEntity()
                                                              .getOid() + 
                                             " has an alignment " + 
                                             previousExonAlign.toString() + 
                                             " that " + 
                                             " overlaps the with exon " + 
                                             exonAlign.getEntity().getOid() + 
                                             " alignment " + 
                                             exonAlign.toString());
                    }

                    previousExonAlign = exonAlign;
                } catch (IllegalArgumentException argEx) {
                    report.addFatalError("Start codon " + 
                                         startCodonAlignment.getEntity()
                                                            .getOid() + 
                                         " could not be checked for geometric containment to exon " + 
                                         exonAlign.getEntity().getOid() + 
                                         " because: " + argEx.getMessage());
                }
            }

            if (!startCodonInExonBounds) {
                report.addFatalError("Start codon " + 
                                     startCodonAlignment.getEntity().getOid() + 
                                     " does not fall within the " + 
                                     " boundaries of one of the transcripts exons");
            }
        }

        if (numStopCodons > 1) {
            report.addFatalError(
                    "Transcript can optionally specify only one stop " + 
                    " codon. Transcript " + this.getOid() + " specifies " + 
                    numStopCodons);
        }

        // The tracript allows 0 or 1 stop codon. If the stop codon is obsoleted,
        // no need to check whether it falls inside one of the exons.
        if (stopCodonAlignment != null) {
            CuratedCodon stopCodon = (CuratedCodon) stopCodonAlignment.getEntity();
            String replacesType = stopCodon.getReplacementRelationship()
                                           .getReplacementType();

            if (!replacesType.equals(ReplacementRelationship.TYPE_OBSOLETE)) {
                Iterator alignIter = exonAlignments.iterator();
                GeometricAlignment exonAlign = null;
                boolean codonInExonBounds = false;

                while ((alignIter.hasNext()) && (!codonInExonBounds)) {
                    exonAlign = (GeometricAlignment) alignIter.next();

                    try {
                        if (exonAlign.intersectsInAxisCoords(stopCodonAlignment)) {
                            codonInExonBounds = true;
                        }
                    } catch (IllegalArgumentException argEx) {
                        report.addFatalError("Stop codon " + 
                                             stopCodonAlignment.getEntity()
                                                               .getOid() + 
                                             " could not be checked for geometric containment to exon " + 
                                             exonAlign.getEntity().getOid() + 
                                             " because: " + 
                                             argEx.getMessage());
                    }
                }

                if (!codonInExonBounds) {
                    report.addFatalError("\nStop codon " + 
                                         stopCodonAlignment.getEntity()
                                                           .getOid() + 
                                         " does not fall within the " + 
                                         " boundaries of one of the transcripts exons");
                }
            }
        }

        return report;
    }

    public PromotionReport doBuildPromoteInstructions(Axis axis, 
                                                      String parentReplacesDirective)
                                               throws SQLException {
        // First need to determine for this Transcript whether the action being performed
        // by promotion is:
        //  1. Create a new transcript
        //  2. Update an existing transcript
        //  3. Obsolete (effectively delete) a trasncript
        //  4. Too many changes to specify replace the existing transcript
        //     with a new modified copy.
        //  5. Split a transcript
        //  6. Merge a transcript
        //
        // Options 4, 5 and 6 will be implemented first and if it turns out to be fast
        // enough then these may be the only options implemented
        PromotionReport report = new PromotionReport(true, "");
        boolean shouldUpdateChildAssociations = false;

        ReplacementRelationship replRel = this.getReplacementRelationship();
        String replacesType = replRel.getReplacementType();
        System.out.println("Promoting transcript with replaces of " + 
                           replacesType);

        String directiveToMyChildren = ReplacementRelationship.TYPE_UNMODIFIED;

        if ((replacesType.equals(ReplacementRelationship.TYPE_SPLIT)) || 
                (replacesType.equals(ReplacementRelationship.TYPE_MERGE))) {
            addReplaceTranscriptPromoteInstruction(axis);
            shouldUpdateChildAssociations = true;
            directiveToMyChildren = ReplacementRelationship.TYPE_MODIFIED;
        } else if (replacesType.equals(ReplacementRelationship.TYPE_NEW)) {
            addBaseCuratedFeatureRepresentation(axis, "transcript");
            addAuditTrailEntryInstruction(MOD_TYPE_INSERT, axis);

            addNewTranscriptSpecificPromoteInstruction(axis, true);
            shouldUpdateChildAssociations = true;
        } else if (replacesType.equals(ReplacementRelationship.TYPE_OBSOLETE)) {
            // Obsolete all alignments, relationship and feature_display
            // rows for the transcript we will let the children obsolete thier
            // own representations
            addObsoleteInstructions(axis, "transcript");
        } else if ((replacesType.equals(ReplacementRelationship.TYPE_DEEP_MOD)) || 
                       (replacesType.equals(
                               ReplacementRelationship.TYPE_MODIFIED)) || 
                       (parentReplacesDirective.equals(
                               ReplacementRelationship.TYPE_MODIFIED))) {
            // Throw everything away and start again...BUT keep the same accession
            // number
            addMakeDeepCopyPromoteInstructions(axis, "transcript");
            shouldUpdateChildAssociations = true;
            directiveToMyChildren = ReplacementRelationship.TYPE_MODIFIED;


            // Have to get the transcript specific instruction as well
            addNewTranscriptSpecificPromoteInstruction(axis, false);


            // Also have to record the fact that the transcript changed so that
            // publishers can pick up the right set of transcripts
            addAuditTrailEntryInstruction(MOD_TYPE_UPDATE, axis);
        } else if (replacesType.equals(ReplacementRelationship.TYPE_UNMODIFIED)) {
            // Update the date_curated and curated_by field in Feature_Curation table
            // to keep a trace of the person who has touch the feature.
            updateFeatureCurationModified();
        } else {
            report.addFatalError(
                    "Encountered an unknown replaces type for transcript " + 
                    this.getOid() + " assuming XML file is corrupt.");
        }

        // Children may be changed even if parent is not so go ahead and
        // ask them to add thier promotion instructions
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

        // If my parent indicated that it was not changed
        return report;
    }

    private PromotionReport promoteChildrenOn(Axis axis, 
                                              String parentReplacesDirective)
                                       throws SQLException {
        PromotionReport childrenReport = new PromotionReport(true, "");

        // This list should include obsoletes so that old version are
        // obsoleted correctly in the datastore
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

    private void addReplaceTranscriptPromoteInstruction(Axis axis)
                                                 throws SQLException {
        // Call addNew to establish this new transcript and add the transcript
        // specific instruction
        addBaseCuratedFeatureRepresentation(axis, "transcript");
        addNewTranscriptSpecificPromoteInstruction(axis, true);


        // Obsolete the old version
        addObsoleteInstructions(axis, "transcript");


        // Add the appropriate audit trail entry for a replace that being a
        // number of inserts and or a number of deletes depending on whether
        // this is a split or a merge. NOTE that deletes are handled in the
        // writeAuditTrailForObsolete override from curated feature becase
        // it keeps track of making sure dont try to obsolete the same
        // feature twice.
        addAuditTrailEntryInstruction(MOD_TYPE_INSERT, axis);

        addRecordOfReplacement();
    }

    /**
     * Creates a replacement record identifying the old obsoleted transcript
     * as being replaced by this transcript for feature tracking purposes.
     */
    protected void addRecordOfReplacement() throws SQLException {
        OID[] replacesOIDS = this.getReplacementRelationship()
                                 .getReplacementOIDs();

        String remarkString = null;
        GenomicProperty remarkProp = this.getProperty(
                                             TranscriptFacade.REMARK_PROP);

        if (remarkProp != null) {
            remarkString = remarkProp.getInitialValue();
        }

        CallableStatement cs = null;

        try {
            int stmtIndex = 2;

            for (int i = 0; i < replacesOIDS.length; i++) {
                cs = this.getRdbmsAccess()
                         .prepareCallableStatement("{ ? = call api_writeback_pkg.add_tran_repl_audit_entry(?,?,?,?,?) }", 
                                                   this.getPromoteDatasourceName());
                cs.setBigDecimal(stmtIndex++, 
                                 this.getPromoteOID().toBigDecimal());
                cs.setBigDecimal(stmtIndex++, replacesOIDS[i].toBigDecimal());


                // At the moment the old and new assembly are always the same
                cs.setBigDecimal(stmtIndex++, this.getPromoteAssemblyVer());
                cs.setBigDecimal(stmtIndex++, this.getPromoteAssemblyVer());

                if ((remarkString == null) || (remarkString.equals(""))) {
                    cs.setNull(stmtIndex++, Types.VARCHAR);
                } else {
                    cs.setString(stmtIndex++, remarkString);
                }

                this.getRdbmsAccess()
                    .executeUpdate(cs, this.getPromoteDatasourceName());
                stmtIndex = 2;
            }
        } catch (Exception sqlEx) {
            ModelMgr.getModelMgr().handleException(sqlEx);

            // Rethrow so that caller gets indication of failure immediately
            throw new SQLException(sqlEx.getMessage());
        } finally {
            this.getRdbmsAccess()
                .executeComplete(cs, this.getPromoteDatasourceName());
        }
    }

    /**
     * Override from CuratedFeature that allows a CuratedTRanscript to write
     * the correct audit trail information for an obsolete case
     */
    protected void writeAuditTrailForObsolete(OID obsoletedOID, Axis axis, 
                                              long assemblyVersion)
                                       throws SQLException {
        addAuditTrailEntryInstruction(MOD_TYPE_DELETE, axis);
    }

    private void addAuditTrailEntryInstruction(String action, Axis axis)
                                        throws SQLException {
        // Transcripts are tracked make sure to keep a history of the requested
        // changes so that obsoleted transcripts can be tracked forward to new
        // ones
        OID modOID = getNewFeatureOID();

        // For all replacement types except insert need to use the replaced OIDS
        // not a new OID
        OID[] entityOIDS = null;
        OID replacingOID = null;

        if ((action.equals(MOD_TYPE_INSERT)) || 
                (action.equals(MOD_TYPE_UPDATE))) {
            entityOIDS = new OID[1];
            entityOIDS[0] = this.getPromoteOID();
        } else {
            entityOIDS = this.getReplacementRelationship().getReplacementOIDs();

            // There should always be at least one replacement transcript
            // if there is not one then throw an exception and abort the
            // promotion process.
            if (entityOIDS.length == 0) {
                throw new RuntimeException(
                        "A transcript was MODIFIED, SPLIT or MERGED " + 
                        " but does not identify which transcript(s) it is replacing. Check " + 
                        " the .gbw file for transcript " + this.getOid() + 
                        " replacement oid(s)");
            }

            if (action.equals(MOD_TYPE_REPLACE)) {
                replacingOID = this.getPromoteOID();
            }
        }

        String modByString = this.getProperty(TranscriptFacade.CREATED_BY_PROP)
                                 .getInitialValue();

        CallableStatement cs = null;
        int stmtIndex = 2;

        try {
            for (int i = 0; i < entityOIDS.length; i++) {
                cs = this.getRdbmsAccess()
                         .prepareCallableStatement("{ ? = call api_writeback_pkg.add_tran_mod_audit_entry(?,?,?,?,?,?) }", 
                                                   this.getPromoteDatasourceName());
                cs.setBigDecimal(stmtIndex++, modOID.toBigDecimal());
                cs.setBigDecimal(stmtIndex++, entityOIDS[i].toBigDecimal());
                cs.setBigDecimal(stmtIndex++, this.getPromoteAssemblyVer());

                if (replacingOID == null) {
                    cs.setNull(stmtIndex++, Types.NUMERIC);
                } else {
                    cs.setBigDecimal(stmtIndex++, replacingOID.toBigDecimal());
                }

                cs.setString(stmtIndex++, action);

                if ((modByString == null) || (modByString.equals(""))) {
                    cs.setNull(stmtIndex++, Types.VARCHAR);
                } else {
                    cs.setString(stmtIndex++, modByString);
                }

                this.getRdbmsAccess()
                    .executeUpdate(cs, this.getPromoteDatasourceName());
                stmtIndex = 2;
                modOID = getNewFeatureOID();
            } // end for
        } catch (Exception sqlEx) {
            ModelMgr.getModelMgr().handleException(sqlEx);

            // Rethrow so that caller gets indication of failure immediately
            throw new SQLException(sqlEx.getMessage());
        } finally {
            this.getRdbmsAccess()
                .executeComplete(cs, this.getPromoteDatasourceName());
        }
    }

    private void addNewTranscriptSpecificPromoteInstruction(Axis axis, 
                                                            boolean genNewAccession)
                                                     throws SQLException {
        String multiExonString = this.getProperty(
                                         TranscriptFacade.HAS_MULTI_EXON_PROP)
                                     .getInitialValue();
        int multiExAsInt = 0;

        if (multiExonString != null) {
            Boolean tempBool = new Boolean(multiExonString);

            if (tempBool.booleanValue()) {
                multiExAsInt = 1;
            }
        }

        GeometricAlignment myAlignment = (GeometricAlignment) this.getOnlyAlignmentToAnAxis(
                                                 axis);
        OID genomicAxisOid = myAlignment.getAxis().getOid();
        String exonAxisInfo = buildExonAxisInfo(axis);

        //We need to fill in the column Transcript.na_seq.  The value that goes in
        //the column is derived from the sequence of the contigs at the coordinates
        //that are defined by the axis_alignments of the exons.  The sequence is
        //supposed to represent the spliced sequence of the transcript, which means
        //that it includes the sequence for each exon, but not the sequence in
        //between the exons. The sequence can be gotten by calling the GetSeq
        //package within the stored procedure call with the exon's axis_begin,
        // axis_end, and orientation.
        CallableStatement cs = null;
        int stmtIndex = 2;

        try {
            if (genNewAccession) {
                String accessionNumber = generateNewAccessionNumber();
                cs = this.getRdbmsAccess()
                         .prepareCallableStatement("{ ? = call api_writeback_pkg.add_new_transcript(?,?,?,?,?,?,?) }", 
                                                   this.getPromoteDatasourceName());
                cs.setLong(stmtIndex++, 
                           this.getPromotionGenomeVersion()
                               .getAssemblyVersion());
                cs.setBigDecimal(stmtIndex++, 
                                 this.getPromoteOID().toBigDecimal());
                cs.setString(stmtIndex++, accessionNumber);
            } else {
                OID[] replacedOIDS = this.getReplacementRelationship()
                                         .getReplacementOIDs();

                // This option should only occur when there is a single replaces so
                // check for this
                if (replacedOIDS.length == 1) {
                    String accessionNumber = this.getProperty(
                                                     TranscriptFacade.TRANSCRIPT_ACCESSION_PROP)
                                                 .getInitialValue();

                    // A transcript should not be marked modified but have a scratch accession
                    // if this happens stop the promotion by raising a RuntimeException
                    if (accessionNumber.startsWith(WORKSPACE_TRAN_PREFIX)) {
                        if (this.getReplacementRelationship()
                                .getReplacementType() == ReplacementRelationship.TYPE_MODIFIED) {
                            throw new RuntimeException(
                                    "Invalid scratch accession number " + 
                                    " in promotion file for a MODIFIED transcript of " + 
                                    accessionNumber + 
                                    " please fix the file and re-promote");
                        }

                        accessionNumber = generateNewAccessionNumber();
                    }

                    cs = this.getRdbmsAccess()
                             .prepareCallableStatement("{ ? = call api_writeback_pkg.add_replacement_tran(?,?,?,?,?,?,?) }", 
                                                       this.getPromoteDatasourceName());
                    cs.setBigDecimal(stmtIndex++, 
                                     this.getPromoteOID().toBigDecimal());
                    cs.setBigDecimal(stmtIndex++, 
                                     replacedOIDS[0].toBigDecimal());
                    cs.setString(stmtIndex++, accessionNumber);
                } else {
                    throw new IllegalArgumentException(
                            "Cannot ask to preserve accession " + 
                            " on a transcript that has more than one replaces OID");
                }
            }

            cs.setInt(stmtIndex++, multiExAsInt);
            cs.setBigDecimal(stmtIndex++, 
                             ((CuratedFeature) this.getSuperFeature()).getPromoteOID()
                                                                    .toBigDecimal());
            cs.setBigDecimal(stmtIndex++, genomicAxisOid.toBigDecimal());
            cs.setString(stmtIndex++, exonAxisInfo.toString());


            //cs.setString(stmtIndex++,this.rdbmsAccess.getRdbmsSecurityId());
            this.getRdbmsAccess()
                .executeUpdate(cs, this.getPromoteDatasourceName());


            // Update any comments that the transcript has
            updateComments();
        } catch (Exception sqlEx) {
            ModelMgr.getModelMgr().handleException(sqlEx);

            // Rethrow so that caller gets indication of failure immediately
            throw new SQLException(sqlEx.getMessage());
        } finally {
            this.getRdbmsAccess()
                .executeComplete(cs, this.getPromoteDatasourceName());
        }
    }

    /**
     * Specialized retrieval of children in the correct order for
     * promotion. Currently the order of promotion for exon children
     * should match the geometric alignment to an axis for exons followed
     * by the start codon and the stop codon if one exists.
     */
    protected GenomicEntity[] doGetChildrenInPromotionOrder(Axis axis) {
        GenomicEntity startCodon = null;
        GenomicEntity stopCodon = null;

        ExonAlignmentComparator exonAlignmentComparator = 
                new ExonAlignmentComparator();
        Set exonAlignments = new TreeSet(exonAlignmentComparator);

        Collection unorderedChildren = this.getSubStructure();
        CuratedFeature currentChild = null;
        GeometricAlignment currentAlignment;
        String childReplaceType = null;
        Iterator unorderChildIter = unorderedChildren.iterator();

        while (unorderChildIter.hasNext()) {
            currentChild = (CuratedFeature) unorderChildIter.next();


            // Dont include any feature that is obsoleted in the list of
            // children to be promoted
            childReplaceType = currentChild.getReplacementRelationship()
                                           .getReplacementType();

            if (childReplaceType.equals(ReplacementRelationship.TYPE_OBSOLETE)) {
                // Dont include this child
                continue;
            }

            currentAlignment = (GeometricAlignment) currentChild.getOnlyAlignmentToAnAxis(
                                       axis);

            if (currentChild instanceof CuratedExon) {
                exonAlignments.add(currentAlignment);
            }

            if (currentChild instanceof CuratedCodon) {
                CodonFeature codon = (CodonFeature) currentChild;

                if (codon.isStartCodon()) {
                    startCodon = currentChild;
                } else if (codon.isStopCodon()) {
                    stopCodon = currentChild;
                } else {
                    throw new RuntimeException(
                            "Found a CuratedCodon that was not " + 
                            " a start , translation frame or stop codon");
                }
            }
        }

        // Now extract the exons and then add the start followed by the stop,
        // if there is one.
        List orderedChildList = new ArrayList();
        Iterator iter = exonAlignments.iterator();

        while (iter.hasNext()) {
            currentAlignment = (GeometricAlignment) iter.next();
            orderedChildList.add(currentAlignment.getEntity());
        }

        if (startCodon != null) {
            orderedChildList.add(startCodon);
        }

        if (stopCodon != null) {
            orderedChildList.add(stopCodon);
        }

        GenomicEntity[] orderedChildren = new GenomicEntity[orderedChildList.size()];
        orderedChildList.toArray(orderedChildren);

        return orderedChildren;
    }

    private String buildExonAxisInfo(Axis axis) {
        StringBuffer buffer = new StringBuffer();

        // Use get sub structure to get children here as at present
        // this does not include obsoleted features.
        Collection children = this.getSubStructure();
        Iterator childIter = children.iterator();
        boolean isFirst = true;
        String childReplacesType = null;

        while (childIter.hasNext()) {
            CuratedFeature currentChild = (CuratedFeature) childIter.next();
            int typeValue = currentChild.getEntityType().value();


            // Be sure to exlcude any exon child that is being obsoleted
            // in the construction of the exon coordinates to be used to build
            // the na_seq for the transcript. Keep checking even though right
            // now should not be getting returned obsoleted children. The cost
            // of cleaning up the database after several thousand promotions if
            // obsoleted children are included far outweights the cost of the
            // redundant check
            childReplacesType = currentChild.getReplacementRelationshipType();

            if ((typeValue == EntityTypeConstants.Exon) && 
                    (!childReplacesType.equals(
                              ReplacementRelationship.TYPE_OBSOLETE))) {
                GeometricAlignment curr = (GeometricAlignment) currentChild.getOnlyAlignmentToAnAxis(
                                                  axis);

                if (!isFirst) {
                    buffer.append(",");
                } else {
                    isFirst = false;
                }

                buffer.append(curr.getRangeOnAxis().getStart());
                buffer.append(",");
                buffer.append(curr.getRangeOnAxis().getStart() + 
                              curr.getRangeOnAxis().getMagnitude());
                buffer.append(",");

                // The orientation is set to 1/-1 required by SQL GetSeq.axis() method call.
                if (curr.orientationForwardOnAxis()) {
                    buffer.append(1);
                } else {
                    buffer.append(-1);
                }
            }
        }

        return buffer.toString();
    }

    // Tranformations....

    /**
     * Returns start of translation position relative to the spliced transcript
     */
    public int getTranslationStart() {
        GeometricAlignment transcriptAlignment = this.getOnlyGeometricAlignmentToOnlyAxis();

        if (transcriptAlignment == null) {
            System.err.println(
                    "Error: either no or multiple alignments found for feature=" + 
                    CuratedTranscript.this);

            return -1;
        }

        int splicedStartPos;
        int axisStartPos;

        Range transcriptRng = transcriptAlignment.getRangeOnAxis();

        if (startCodon != null) {
            GeometricAlignment startAlignment = startCodon.getOnlyGeometricAlignmentToOnlyAxis();

            if (startAlignment == null) {
                System.err.println(
                        "Error: either no or multiple alignments found for feature=" + 
                        startCodon);

                return -1;
            }

            if (transcriptRng.isReversed()) {
                axisStartPos = startAlignment.getRangeOnAxis().getMaximum();
            } else {
                axisStartPos = startAlignment.getRangeOnAxis().getMinimum();
            }
        } else { //startCodon == null

            if (transcriptRng.isReversed()) {
                //startPos.change(transcriptRng.getMaximum(), transcriptRng.getMaximum());
                axisStartPos = transcriptRng.getMaximum();
            } else {
                //startPos.change(transcriptRng.getMinimum(), transcriptRng.getMinimum());
                axisStartPos = transcriptRng.getMinimum();
            }
        }

        splicedStartPos = this.transformAxisPositionToSplicedPosition(
                                  axisStartPos);

        //System.out.println("getTranslationStart: axisStartPos=" + axisStartPos + " splicedStartPos=" + splicedStartPos);
        return splicedStartPos;
    }

    /**
    * Returns start of translation position relative to the spliced transcript
    */
    public int getTranslationStop() {
        GeometricAlignment transcriptAlignment = this.getOnlyGeometricAlignmentToOnlyAxis();

        if (transcriptAlignment == null) {
            System.err.println(
                    "Error: either no or multiple alignments found for feature=" + 
                    CuratedTranscript.this);

            return -1;
        }

        int splicedStopPos;
        int axisStopPos;

        Range transcriptRng = transcriptAlignment.getRangeOnAxis();

        if (stopCodon != null) {
            GeometricAlignment stopAlignment = stopCodon.getOnlyGeometricAlignmentToOnlyAxis();

            if (stopAlignment == null) {
                System.err.println(
                        "Error: either no or multiple alignments found for feature=" + 
                        startCodon);

                return -1;
            }

            if (transcriptRng.isReversed()) {
                axisStopPos = stopAlignment.getRangeOnAxis().getMaximum();
            } else {
                axisStopPos = stopAlignment.getRangeOnAxis().getMinimum();
            }
        } else { //stopCodon == null

            if (transcriptRng.isReversed()) {
                axisStopPos = transcriptRng.getMaximum();
            } else {
                axisStopPos = transcriptRng.getMinimum();
            }
        }

        splicedStopPos = this.transformAxisPositionToSplicedPosition(
                                 axisStopPos);

        return splicedStopPos;
    }

    /**
     * Converts from a position on a spliced feature to a position on genomic axis.
     * This really only makes biological since for a Transcript or a Gene Prediction,
     * but the algorithm can be supported on any Feature.
     * @return return the axis position corresponding to the spliced position.
     */
    public int transformSplicedPositionToAxisPosition(int splicedPosition) {
        GeometricAlignment transcriptAlignment = this.getOnlyGeometricAlignmentToOnlyAxis();

        if (transcriptAlignment == null) {
            System.err.println(
                    "Error: either no or multiple alignments found for feature=" + 
                    CuratedTranscript.this);

            return -1;
        }

        boolean reversed = transcriptAlignment.getRangeOnAxis().isReversed();
        Axis transAxis = transcriptAlignment.getAxis();

        // Feature getSubFeatures();
        List orderedExons = this.getOrderedFeatureSet(this.getSubFeatures(), 
                                                      !reversed);

        // java.util.List orderedSubfeatures = Feature.getOrderedFeatureSet(this.getSubFeatures(), !compositeReversed);
        Iterator exonIter = orderedExons.iterator();

        CuratedExon exon;
        int pos = 0;
        int offset = 0;
        int axisPos = 0;
        int maxpos = 0;
        GeometricAlignment exonAlignment;

        for (Iterator i = exonIter; i.hasNext();) {
            exon = (CuratedExon) i.next();
            exonAlignment = exon.getOnlyGeometricAlignmentToOnlyAxis();

            // MIRROR RESULTING ALIGNMENT: axis.findOnlyAlignedRangeFor(exon, reversed);
            // if (reversed) mirror the alignment...
            if (exonAlignment == null) {
                System.err.println(
                        "Error: either no or multiple alignments found for feature=" + 
                        exon);

                return -1;
            }

            MutableRange exonRange = exonAlignment.getRangeOnAxis()
                                                  .toMutableRange();

            if (reversed) {
                exonRange.mirror(transAxis.getMagnitude());
            }

            if (splicedPosition >= (pos + exonRange.getMagnitude())) {
                pos += exonRange.getMagnitude();
                maxpos = exonRange.getMaximum();
            } else {
                offset = splicedPosition - pos;

                axisPos = exonRange.getMinimum() + offset;

                if (reversed) {
                    MutableRange mirroredPos = new MutableRange(axisPos, 
                                                                axisPos);
                    mirroredPos.mirror(transAxis.getMagnitude());

                    return mirroredPos.getStart();
                }

                return axisPos;
            }
        } //end for

        // If the spliced position equalled the last position, this would always return -1. Bad.
        if (splicedPosition == pos) {
            return maxpos;
        }
        //The spliced position is greater than the last exon position.
        else {
            return -1;
        }
    }

    /**
     * Converts from a position on a genomic axis to a position on the spliced feature.
     * This really only makes biological since for a Transcript or a Gene Prediction,
     * but the algorithm can be supported on any Feature.
     * @param the position on the axis of interest.
     * @return the position on the spliced transcript corresponding to the axisPosition.
     * Note that the return value is relative to the start of the transcript.
     */
    public int transformAxisPositionToSplicedPosition(int axisPosition) {
        CuratedExon exon = this.getExonForPositionOnAxis(axisPosition);

        if (exon == null) {
            return -1;
        }

        GeometricAlignment transcriptAlignment = this.getOnlyGeometricAlignmentToOnlyAxis();

        if (transcriptAlignment == null) {
            System.err.println(
                    "Error: either no or multiple alignemnts found for feature=" + 
                    this);

            return -1;
        }

        Axis transAxis = transcriptAlignment.getAxis();

        boolean reversed = transcriptAlignment.getRangeOnAxis().isReversed();

        //compute the position relative to the exon
        GeometricAlignment exonAlign = exon.getOnlyGeometricAlignmentToOnlyAxis();

        // MIRROR RESULTING ALIGNMENT: axis.findOnlyAlignedRangeFor(exon, reversed);
        // if (reversed) mirror the alignment...
        if (exonAlign == null) {
            System.err.println(
                    "Error: either no or multiple alignments found for feature=" + 
                    exon);

            return -1;
        }

        MutableRange exonAtPosRng = new MutableRange(exonAlign.getRangeOnAxis());

        if (reversed) {
            exonAtPosRng.mirror(transAxis.getMagnitude());
        }

        // Get the position relative to the exon...
        MutableRange relativeRng = new MutableRange(axisPosition, axisPosition);

        if (reversed) {
            relativeRng.mirror(transAxis.getMagnitude());
        }

        relativeRng.translate(-exonAtPosRng.getMinimum());

        // Now figure out the exon position relative to the transcript
        int offset = relativeRng.getMinimum();
        MutableRange exonRange = new MutableRange();
        CuratedExon subfeat;

        // Get the list of subfeatures in geometric order (reversed if needed)...
        List orderedExons = this.getOrderedFeatureSet(this.getSubFeatures(), 
                                                      !reversed);

        for (Iterator itr = orderedExons.iterator(); itr.hasNext();) {
            subfeat = (CuratedExon) itr.next();
            exonAlign = subfeat.getOnlyGeometricAlignmentToOnlyAxis();

            // MIRROR RESULTING ALIGNMENT: axis.findOnlyAlignedRangeFor(subfeat, reversed);
            // if (reversed) mirror the alignment...
            if (exonAlign == null) {
                System.err.println(
                        "Error: either no or multiple alignments found for feature=" + 
                        subfeat);

                return -1;
            }


            // Put the exon range in our mutable range...
            exonRange.change(exonAlign.getRangeOnAxis());

            // Reverse it if needed...
            if (reversed) {
                exonRange.mirror(transAxis.getMagnitude());
            }

            if (exonRange.getMinimum() < exonAtPosRng.getMinimum()) {
                offset += exonRange.getMagnitude();
            }
        }

        return offset;
    }

    //****************************************
    //*  Protected methods
    //****************************************

    /**
     * Templete pattern
     * Sub-Classes should overide this to set the proper mutator
     */
    protected GenomicEntityMutator constructMyMutator() {
        return new CuratedTranscriptMutator();
    }

    /**
     * A more specific getMutator();
     */
    protected CuratedTranscriptMutator getCuratedTranscriptMutator() {
        return (CuratedTranscriptMutator) this.getMutator();
    }

    //****************************************
    //*  Inner / Member classes
    //****************************************

    /**
     * The CuratedTranscriptMutator class is the only way you can change the state of Feature
     * instances.
     * The CuratedTranscriptMutator class is public, but it's constructor is private.
     */
    public class CuratedTranscriptMutator extends CuratedFeatureMutator {
        /**
         * Protected constructor for the mutator class...
         */
        protected CuratedTranscriptMutator() {
        }

        public void updatePropertiesBasedOnGeometricAlignment(GeometricAlignment geomAlign) {
            super.updatePropertiesBasedOnGeometricAlignment(geomAlign);

            Collection exonChildren = CuratedTranscript.this.getSubFeatures();
            ArrayList listOfStarts = new ArrayList();

            for (Iterator iter = exonChildren.iterator(); iter.hasNext();) {
                CuratedExon exon = (CuratedExon) iter.next();

                if (exon.getOnlyGeometricAlignmentToOnlyAxis() != null) {
                    int start = exon.getOnlyGeometricAlignmentToOnlyAxis()
                                    .getRangeOnAxis().getStart();
                    listOfStarts.add(new Integer(start));
                }
            }

            /*for(Iterator iter=exonChildren.iterator();iter.hasNext();){
              CuratedExon exon=(CuratedExon)iter.next();
              int orderOfExon= (sortBasedOnStart(exon, listOfStarts));
            
             try {
               GenomicProperty g=exon.getProperty(ExonFacade.ORDER_NUM_PROP);
               if(exon.getProperty(ExonFacade.ORDER_NUM_PROP)==null){
                GenomicProperty gp=new GenomicProperty(ExonFacade.ORDER_NUM_PROP,
                "",
                String.valueOf(orderOfExon),
                false,
                "");
                exon.getCuratedExonMutator().addProperty(gp);
               } else{
                exon.getCuratedExonMutator().setProperty(ExonFacade.ORDER_NUM_PROP,String.valueOf(orderOfExon));
               }
             }catch(Exception e){
             }
            }*/
        }

        public void setProperty(String propertyName, String propertyValue)
                         throws InvalidPropertyFormat {
            super.setProperty(propertyName, propertyValue);

            // also make sure that whenever the property
            // name is Gene Accession Property for Gene then
            // all the transcripts must have that same gene
            // accession property
            if (TranscriptFacade.GENE_ACCESSION_PROP.equals(propertyName)) {
                for (Iterator iter = CuratedTranscript.this.getSubFeatures()
                                                           .iterator();
                     iter.hasNext();) {
                    CuratedExon ce = (CuratedExon) iter.next();
                    ce.getCuratedExonMutator()
                      .setProperty(TranscriptFacade.GENE_ACCESSION_PROP, 
                                   propertyValue);
                }
            }
        }

        private int sortBasedOnStart(CuratedFeature feature, ArrayList starts) {
            int j = 0;
            int k;
            int temp;
            Integer first;
            Integer second;
            boolean condition;

            for (j = 0; j < starts.size(); j++) {
                for (k = j + 1; k < starts.size(); k++) {
                    first = (Integer) (starts.get(j));
                    second = (Integer) (starts.get(k));

                    if (feature.getOnlyGeometricAlignmentToOnlyAxis() == null) {
                        return -1;
                    }

                    if (feature.getOnlyGeometricAlignmentToOnlyAxis()
                               .orientationForwardOnAxis()) {
                        condition = (first.intValue()) > (second.intValue());
                    } else {
                        condition = (first.intValue()) < (second.intValue());
                    }

                    if (condition) {
                        // swap begins
                        temp = first.intValue();
                        starts.set(j, second);
                        starts.set(k, (new Integer(temp)));
                    }
                }
            }

            int i = 1;

            for (Iterator iter = starts.iterator(); iter.hasNext();) {
                Integer value = (Integer) iter.next();

                if ((value == null) || 
                        (feature.getOnlyGeometricAlignmentToOnlyAxis() == null)) {
                    return -1;
                }

                if (value.intValue() == feature.getOnlyGeometricAlignmentToOnlyAxis()
                                               .getRangeOnAxis().getStart()) {
                    break;
                } else {
                    i++;
                }
            }

            return i;
        }

        /**
         * Add evidence and create exon if necessary...
         * Check to see if there is an existing exon, if so just add evidence,
         * if not, create a new exon, align it to the axis (from the evidenceAlignment)
         * add it as a subfeature of this transcript and add the evidence.
         * This method will add it to the ALL Exon it comes to that "intersect"
         * the range of evidence Feature.
         * If there are multiple Exons that intersect the range, all the evidence
         * will be added to each of the insersecting Exons.
         * All newly created alignments will be a MutableAlignment.
         * Return the LAST exon that was used, either newly created or (one of)
         * the previously existing exon(s).
         * @todo: maybe this should return the alignment of the affected exon.
         * @todo: what should we do if the evidence alignment intersects multiple exons.
         */
        public Collection addEvidenceAndCreateExonIfNecessary(GeometricAlignment evidenceGeoAlign) {
            // Check for null arg...
            if (evidenceGeoAlign == null) {
                return null;
            }

            // Make sure the evidence alignment has both an entity and an axis...
            if ((evidenceGeoAlign.getAxis() == null) || 
                    (evidenceGeoAlign.getEntity() == null)) {
                return null;
            }

            // Check for non-Feature evidence...
            if (!(evidenceGeoAlign.getEntity() instanceof Feature)) {
                return null;
            }

            // Check if the evidence is api_generated, do not add it.
            if ((evidenceGeoAlign.getEntity().getOid().isAPIGeneratedOID())) {
                return null;
            }

            Axis evidenceAxis = evidenceGeoAlign.getAxis();
            Feature evidenceFeature = (Feature) evidenceGeoAlign.getEntity();
            CuratedExon exonForEvidence = null;

            // Add evidence only once, but use all alignments to try to find the exon
            Collection intersectingExons = CuratedTranscript.this.getSubFeaturesInRangeOnAxis(
                                                   evidenceGeoAlign.getRangeOnAxis());

            // exonForEvidence = CuratedTranscript.this.getExonForAxisPosition(genomicAxisPI,alignmentRanges[i].getRangeOnAxis().getStart());
            if (!intersectingExons.isEmpty()) {
                for (Iterator itr = intersectingExons.iterator();
                     itr.hasNext();) {
                    exonForEvidence = (CuratedExon) itr.next();
                    exonForEvidence.getFeatureMutator()
                                   .addEvidence(evidenceFeature);
                }

                return intersectingExons;
            }

            // No exon found, create a new exon...
            GenomicEntityFactory entityFactory = CuratedTranscript.this.getEntityFactory();
            CuratedExon newExon = (CuratedExon) entityFactory.create(
                                          OIDGenerator.getOIDGenerator()
                                                      .generateOIDInNameSpace(OID.SCRATCH_NAMESPACE, 
                                                                              CuratedTranscript.this.getGenomeVersion()
                                                                                                    .getID()), 
                                          "Exon", // displayname
                                          EntityType.getEntityTypeForValue(
                                                  EntityTypeConstants.Exon), // entityType
                                          "Curation" // String discoveryEnvironment.
                                         );

            newExon.getFeatureMutator().addEvidence(evidenceFeature);

            // Ensure orientation of the new Curated Exon matches the orientation of
            // the Curated Transcript that it is attached to.
            MutableRange mRange = evidenceGeoAlign.getRangeOnAxis()
                                                  .toMutableRange();
            GeometricAlignment tmpAlignment = (GeometricAlignment) CuratedTranscript.this.getOnlyAlignmentToOnlyAxis();

            if ((tmpAlignment != null) && 
                    !tmpAlignment.getOrientationOnAxis()
                                 .equals(evidenceGeoAlign.getOrientationOnAxis())) {
                if (!evidenceGeoAlign.getOrientationOnAxis()
                                     .equals(Range.UNKNOWN_ORIENTATION)) {
                    mRange.reverse();
                }
            }

            MutableAlignment newAlignment = new MutableAlignment(evidenceAxis, 
                                                                 newExon, 
                                                                 mRange);

            // Make sure that the new alitnment has the orientation of the transcript.
            // newAlignment.getOrientationOnAxis()... this should be supported at MutableAlignment.
            try {
                // Add the new exon as a subfeature of this transcript...
                this.addSubFeature(newExon);


                // Use the first alignment range of the evidence for new exon...
                newExon.getFeatureMutator().addAlignmentToAxis(newAlignment);
            } catch (Exception ex) {
                // Report the exception...
                CuratedTranscript.this.getModelMgr().handleException(ex);
            }

            Collection involvedExons = new ArrayList();
            involvedExons.add(newExon);

            return involvedExons;
        }

        /**
         * Adjust  for an added sub-feature.  This gives sub-classes, like Transcript,
         * to adjust for an added sub-feature.
         * This will be called by FeatureMutator.addSubFeature(Feature newSubFeature)
         * - After the newSubFeature has been added
         * - After the FeatureMutator.updateAlignmentBasedOnSubFeatures() is called,
         * - Before we postSubFeatureAdded(newSubFeature);
         */
        void adjustForAddedSubFeature(Feature newSubFeature) {
            // Call super first... (CuratedFeatureMutator will adjust the alignments properly).
            super.adjustForAddedSubFeature(newSubFeature);

            if (newSubFeature instanceof CuratedCodon && 
                    ((CuratedCodon) newSubFeature).isStopCodon()) {
                boolean b = this.adjustStartGivenStop();

                if (!b) {
                    System.out.println("could not find start for stop");
                }
            }
            //Donot adjust stop if there is a Stop codon on the Transcript and no Start Codon
            // the only time that can happen is when the User is using the Stop Codon to generate
            // the Start codon usally the Start is dragged and that may or maynot produce the Stop
            else if ((CuratedTranscript.this.getStopCodon() != null && 
                             CuratedTranscript.this.getStartCodon() == null)) {
                this.adjustTranslationStop();
            }

            this.adjustTranslationStop();
        }

        /**
         * Adjust  for an added sub-feature.  This gives sub-classes, like Transcript,
         * to adjust for an added sub-feature.
         * This will be called by FeatureMutator.addSubFeature(Feature newSubFeature)
         * - After the newSubFeature has been added
         * - Before we postSubFeatureAdded(newSubFeature);
         */
        void adjustForRemovedSubFeature(Feature oldSubFeature) {
            // Call super first... (CuratedFeatureMutator will adjust the alignments properly).
            super.adjustForRemovedSubFeature(oldSubFeature);

            // Check for startCodon removal.
            // See if there is still an exon at it's position...
            // this condition is unecessary because adjustTranslationStop
            // checks for null
            //  if (CuratedTranscript.this.startCodon != null) {
            // See if the Start Codon lost it's home Exon...
            if (CuratedTranscript.this.getExonForStartCodon() == null) {
                // There's no Exon for the start codon, so remove the start...
                this.removeStartOrStopCodon(CuratedTranscript.this.startCodon);
            }


            // I need to adjust translation stop.
            this.adjustTranslationStop();
        }

        /**
         * Set the start codon.
         * This will throw an InvalidFeatureStructureException.
         * The newStartCodon must be aligned to the same axis as this CuratedTranscript.
         * The newStartCodon's alignment must overlap an Exon.
         * @todo Codons - setStartCodon() should check for frame translation?
         * @todo Codons - check for old bug.
         * OLD BUG: CuratedTranscriptPI.setStartCodonAndAlignToAxis
         * In this method there is a call to getExonForAxisPosition.
         * The position passed was not correct for features on the reverse strand.
         * This fix may take care of some other bugs. You may want to do an update
         * before working on any codon related problems..
         */
        public void setStartCodon(CuratedCodon newStartCodon)
                           throws InvalidFeatureStructureException {
            // Check arges...
            if (newStartCodon == null) {
                return;
            }

            /*
             // do the check only if the codon and transcript originate from gbw, because
             // database probably returns
             int startOfCodon=newStartCodon.getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis().getStart();
             Iterator iter=transcript.getSubFeatures().iterator();
             while(iter.hasNext()){
               Range exonRange=((CuratedExon)iter.next()).getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis();
               if(exonRange.contains(startOfCodon)){
                 validtoexecute=true;
                 break;
               }
             }
             if(!validtoexecute){
               throw new InvalidFeatureStructureException("codon should lie in atleast one of the exons of the transcript");
             }
            
              */
            CuratedCodon.CuratedCodonMutator startCodonMutator;


            // Remove the existing Start if there is one...
            this.removeStartOrStopCodon(CuratedTranscript.this.startCodon);


            // Add the new Start...
            CuratedTranscript.this.startCodon = newStartCodon;
            startCodonMutator = newStartCodon.getCuratedCodonMutator();
            startCodonMutator.setHostTranscript(CuratedTranscript.this);


            // Adjust the translation stop...
            this.adjustTranslationStop();
        }

        /**
         * Set Stop Codon...
         */
        private void setStopCodon(CuratedCodon newStopCodon)
                           throws InvalidFeatureStructureException {
            // Check arges...
            if (newStopCodon == null) {
                return;
            }

            CuratedCodon.CuratedCodonMutator stopCodonMutator;


            // Remove the existing Stop if there is one...
            this.removeStartOrStopCodon(CuratedTranscript.this.stopCodon);


            // Add the new Start...
            CuratedTranscript.this.stopCodon = newStopCodon;
            stopCodonMutator = newStopCodon.getCuratedCodonMutator();
            stopCodonMutator.setHostTranscript(CuratedTranscript.this);
        }

        /**
         * A utility method that will;
         * - remove any references between this
         *
         * this.adjustTranslationStop will not be called.
         */
        public void removeStartOrStopCodon(CuratedCodon oldCodon) {
            // Check arges...
            if (oldCodon == null) {
                return;
            }

            // Remove references from this transcript...
            if (CuratedTranscript.this.startCodon == oldCodon) {
                CuratedTranscript.this.startCodon = null;
            } else if (CuratedTranscript.this.stopCodon == oldCodon) {
                CuratedTranscript.this.stopCodon = null;
            }

            CuratedCodon.CuratedCodonMutator codonMutator = 
                    oldCodon.getCuratedCodonMutator();


            // Remove the codon's reference to a host transcript...
            codonMutator.setHostTranscript(null);


            // Remove the codon's alignments...
            codonMutator.removeOnlyAlignmentToOnlyAxis();
        }

        /**
         * This method will compute the stop position for a transcript given a start, create a new stop codon if necessary and
         * set the alignment for the stop codon to the correct position.
         *
         * Returns true only when a new stop codon is calculated or an existing
         * stop codon is removed.
         *
         * @todo: Codons - CLEAN-UP "boolean adjustTranslationStop()"
         */
        public boolean adjustTranslationStop() {
            if (CuratedTranscript.this.isUnderConstruction()) {
                //System.out.println("Will not adjustTranslationStop(); for Transcript Under Construction.");
                return false;
            }

            // Get this transcript's only geometric alingment...
            GeometricAlignment transOnlyAlign = CuratedTranscript.this.getOnlyGeometricAlignmentToOnlyAxis();
            Axis transAxis = null;

            if (transOnlyAlign != null) {
                transAxis = transOnlyAlign.getAxis();
            }

            CuratedCodon startCodon;
            CuratedCodon previousStopCodon;
            startCodon = CuratedTranscript.this.startCodon;
            previousStopCodon = CuratedTranscript.this.stopCodon;

            // If the transcript doesn't have an alignment OR the no start codon,
            // then we should remove the stopCodon if we have one.
            if ((transOnlyAlign == null) || (startCodon == null)) {
                //if the start proxy is null then remove the stop
                if (previousStopCodon != null) {
                    this.removeStartOrStopCodon(previousStopCodon);

                    return true;
                }

                return false;
            }

            //start is non null so get its range
            GeometricAlignment startOnlyAlign = startCodon.getOnlyGeometricAlignmentToOnlyAxis();

            // MutableRange startrng = new MutableRange();
            if (startOnlyAlign == null) {
                return false;
            }

            // Get a mutable form of the startCodons range on Axis.
            MutableRange startRange = startOnlyAlign.getRangeOnAxis()
                                                    .toMutableRange();

            int startSplicedPos = CuratedTranscript.this.transformAxisPositionToSplicedPosition(
                                          startRange.getStart());

            // System.out.println("Axis pos: " + startRange.getStart() + " translated to Splice pos: " + startSplicedPos);
            if (startSplicedPos < 0) {
                return false; //axis position is not on the spliced transcript.
            }

            // Get the spliced residues...
            String splicedRes = DNA.toString(
                                        CuratedTranscript.this.getSplicedResidues());
            int stopSplicedPos = getNextInFrameStopPosition(startSplicedPos, 
                                                            splicedRes);

            // Check for no new stop found...
            if (stopSplicedPos < 0) {
                // no stop found remove the old stop and return
                if (previousStopCodon != null) {
                    this.removeStartOrStopCodon(previousStopCodon);

                    return true;
                }

                return false;
            }

            int stopAxisPos = CuratedTranscript.this.transformSplicedPositionToAxisPosition(
                                      stopSplicedPos);

            // Create tne new stop range...
            Range newStopRange = new Range(stopAxisPos, 3, 
                                           startRange.getOrientation());

            // Meed the property manger...
            PropertyMgr thePropMgr = PropertyMgr.getPropertyMgr();

            // If we didn't originally have a start codon... create a new one and align it
            if (previousStopCodon == null) {
                // We will need the Genomic Entity Factory...
                GenomicEntityFactory geFactory = (ModelMgr.getModelMgr())
                                                     .getEntityFactory();
                GenomeVersion genomeVersion = transAxis.getGenomeVersion();
                OID newOID = OIDGenerator.getOIDGenerator()
                                         .generateOIDInNameSpace(OID.SCRATCH_NAMESPACE, 
                                                                 genomeVersion.hashCode());

                CuratedCodon newStopCodon = (CuratedCodon) geFactory.create(
                                                    newOID, "StopCodon", 
                                                    EntityType.getEntityTypeForValue(
                                                            EntityTypeConstants.StopCodon), 
                                                    "Curation", null, 
                                                    CuratedTranscript.this, 
                                                    FeatureDisplayPriority.DEFAULT_PRIORITY);

                // StopCodonFeatPI stopCodon = (StopCodonFeatPI) fa.createFeaturePI(OID.SCRATCH_NAMESPACE,
                //                                                         0, 0, stopAxisPos, codonLen, "StopCodon", null, "Curation",
                //                                                         FeatureType._STOP_CODON);
                // rangeOnAxis should have orientation built in.
                GeometricAlignment newStopAlignment = new MutableAlignment(
                                                              startOnlyAlign.getAxis(), 
                                                              newStopCodon, 
                                                              newStopRange);

                // Try to apply the codon...
                try {
                    // Add the alignment...
                    CuratedCodon.CuratedCodonMutator newStopMutator = 
                            newStopCodon.getCuratedCodonMutator();
                    newStopMutator.addAlignmentToAxis(newStopAlignment);


                    // Add this stop codon to this transcript...
                    //       this.setStopCodon(newStopCodon);
                    // Set up the properites...
                    // geFactory.constructNonMandEditableProperties(newStopCodon);
                    thePropMgr.handleProperties(PropertyMgr.NEW_ENTITY, 
                                                newStopCodon, false);


                    // Update properties that are based on geometric alignment...
                    newStopMutator.updatePropertiesBasedOnGeometricAlignment(
                            newStopAlignment);


                    // Adopt properties off the old Stop Codon...
                    thePropMgr.handleProperties(PropertyMgr.UPDATE_ENTITY, 
                                                newStopCodon, false);
                } catch (Exception ex) {
                    this.removeStartOrStopCodon(newStopCodon);
                }
            }
            // Else, we had a previous stop codon, so just adjust it...
            else {
                // Adjust the position of the existing stop codon
                // previousStopCodon.getCuratedCodonMutator().changeRangeOnAlignment();
                CuratedCodon.CuratedCodonMutator previousStopMutator = 
                        previousStopCodon.getCuratedCodonMutator();
                GeometricAlignment stopCodonAlignment = 
                        previousStopCodon.getOnlyGeometricAlignmentToOnlyAxis();

                // Check for during load...
                if (stopCodonAlignment == null) {
                    return false;
                }

                // First check if it's the same...
                if (newStopRange.equals(stopCodonAlignment.getRangeOnAxis())) {
                    return false;
                }

                previousStopMutator.changeRangeOnAlignment(stopCodonAlignment, 
                                                           newStopRange);


                // Update properties that are based on geometric alignment...
                previousStopMutator.updatePropertiesBasedOnGeometricAlignment(
                        stopCodonAlignment);
                thePropMgr.handleProperties(PropertyMgr.UPDATE_ENTITY, 
                                            previousStopCodon, false);
            }

            return true;
        }

        /**
         * This method will compute the start position for a transcript given a stop,
         * set the alignment for the start codon to the correct position.
         *
         * Returns true only when a new start codon is calculated or an existing
         * start codon is removed.
         *
         * @todo: Codons - CLEAN-UP "boolean adjustTranslationStop()"
         */
        public boolean adjustStartGivenStop() {
            if (CuratedTranscript.this.isUnderConstruction()) {
                //System.out.println("Will not adjustStart; for Transcript Under Construction.");
                return false;
            }

            // Get this transcript's only geometric alignment...
            GeometricAlignment transOnlyAlign = CuratedTranscript.this.getOnlyGeometricAlignmentToOnlyAxis();
            Axis transAxis = null;

            if (transOnlyAlign != null) {
                transAxis = transOnlyAlign.getAxis();
            }

            //Case 2:
            //the transcript has no previous start codon and hence no previous stop
            //so calculate a new start given stop and return true
            if (CuratedTranscript.this.getStartCodon() == null) {
                // Get a mutable form of the stopCodons range on Axis.
                GeometricAlignment stopOnlyAlign = CuratedTranscript.this.stopCodon.getOnlyGeometricAlignmentToOnlyAxis();
                MutableRange stopRange = stopOnlyAlign.getRangeOnAxis()
                                                      .toMutableRange();
                int stopSplicedPos = CuratedTranscript.this.transformAxisPositionToSplicedPosition(
                                             stopRange.getStart());

                if (stopSplicedPos < 0) {
                    return false; //axis position is not on the spliced transcript.
                }

                // Get the spliced residues...
                String splicedRes = DNA.toString(
                                            CuratedTranscript.this.getSplicedResidues());

                int startSplicedPos = getNextInFrameStopPositionGivenStop(
                                              stopSplicedPos, splicedRes) + 3;

                int startAxisPos = CuratedTranscript.this.transformSplicedPositionToAxisPosition(
                                           startSplicedPos);

                //Create tne new start range...
                Range newStartRange = new Range(startAxisPos, 3, 
                                                stopRange.getOrientation());

                //Need the property manger...
                PropertyMgr thePropMgr = PropertyMgr.getPropertyMgr();

                // We will need the Genomic Entity Factory...
                GenomicEntityFactory geFactory = (ModelMgr.getModelMgr())
                                                     .getEntityFactory();
                GenomeVersion genomeVersion = transAxis.getGenomeVersion();
                OID newOID = OIDGenerator.getOIDGenerator()
                                         .generateOIDInNameSpace(OID.SCRATCH_NAMESPACE, 
                                                                 genomeVersion.hashCode());
                CuratedCodon newStartCodon;

                if (splicedRes.substring(startSplicedPos, startSplicedPos + 3)
                              .equals("ATG")) {
                    newStartCodon = (CuratedCodon) geFactory.create(newOID, 
                                                                    "StartCodon", 
                                                                    EntityType.getEntityTypeForValue(
                                                                            EntityTypeConstants.Start_Codon_Start_Position), 
                                                                    "Curation", 
                                                                    null, 
                                                                    CuratedTranscript.this, 
                                                                    FeatureDisplayPriority.DEFAULT_PRIORITY);
                } else {
                    newStartCodon = (CuratedCodon) geFactory.create(newOID, 
                                                                    "TranslationStartCodon", 
                                                                    EntityType.getEntityTypeForValue(
                                                                            EntityTypeConstants.Translation_Start_Position), 
                                                                    "Curation", 
                                                                    null, 
                                                                    CuratedTranscript.this, 
                                                                    FeatureDisplayPriority.DEFAULT_PRIORITY);
                }

                GeometricAlignment newStartAlignment = 
                        new MutableAlignment(stopOnlyAlign.getAxis(), 
                                             newStartCodon, newStartRange);

                //Try to apply the codon...
                try {
                    // Add the alignment...
                    CuratedCodon.CuratedCodonMutator newStartMutator = 
                            newStartCodon.getCuratedCodonMutator();
                    newStartMutator.addAlignmentToAxis(newStartAlignment);

                    thePropMgr.handleProperties(PropertyMgr.NEW_ENTITY, 
                                                newStartCodon, false);


                    // Update properties that are based on geometric alignment...
                    newStartMutator.updatePropertiesBasedOnGeometricAlignment(
                            newStartAlignment);


                    // Adopt properties off the old Start Codon...
                    thePropMgr.handleProperties(PropertyMgr.UPDATE_ENTITY, 
                                                newStartCodon, false);
                } catch (Exception ex) {
                    this.removeStartOrStopCodon(newStartCodon);
                }
            }

            return true;
        }

        //=======================Replacement Relationship mutation=================

        /**
         * Record that this CuratedFeature is the result of merging two previously
         * existing CuratedFeatures...
         * This method is called on the resulting merged CuratedFeature.
         * This will combing the replaces relationships of the two previous CuratedFeatures.
         * @todo: Replaces - Implement "recordIsResultOfMerged(CuratedFeature prevCuratedFeat1,CuratedFeature prevCuratedFeat2);"
         */
        public void recordIsResultOfMerged(CuratedTranscript preMergeTranscript1, 
                                           CuratedTranscript preMergeTranscript2) {
            // Message the workspace with the needed informaiton...
            Workspace myWorkspace = CuratedTranscript.this.getGenomeVersion()
                                                          .getWorkspace();

            if (myWorkspace != null) {
                myWorkspace.setupMergeOfPromoted(preMergeTranscript1, 
                                                 preMergeTranscript2, 
                                                 CuratedTranscript.this);
            }
        }

        /**
         * Record that this CuratedFeature was split into two new resulting
         * CuratedFeatures.
         * This method is called on the previously existing curated Feature that
         * was split.
         * The replaces relationships will be propogated to the resulting curated feat.
         */
        public void recordWasSplitInto(CuratedTranscript postSplitTranscript1, 
                                       CuratedTranscript postSplitTranscript2) {
            // Message the workspace with the needed informaiton...
            Workspace myWorkspace = postSplitTranscript1.getGenomeVersion()
                                                        .getWorkspace();

            if (myWorkspace != null) {
                myWorkspace.setupSplitOfPromoted(CuratedTranscript.this, 
                                                 postSplitTranscript1, 
                                                 postSplitTranscript2);
            }
        }
    } // End of CuratedExonMutator member class.

    // Put the alignments for children that are exons into a sorted list
    // so that they will get ordered geometrically
    static private class ExonAlignmentComparator implements java.util.Comparator {
        public int compare(Object o1, Object o2) {
            GeometricAlignment align1 = null;
            GeometricAlignment align2 = null;

            if ((o1 instanceof api.entity_model.model.alignment.GeometricAlignment) && 
                    (o2 instanceof api.entity_model.model.alignment.GeometricAlignment)) {
                align1 = (GeometricAlignment) o1;
                align2 = (GeometricAlignment) o2;
            } else {
                throw new IllegalArgumentException(
                        "ExonAlignmentComparator::compare " + 
                        " called with argument of invalid class. Class types were o1 " + 
                        o1.getClass() + " o2 " + o2.getClass() + 
                        ". Can only " + " be GeometricAlignment");
            }

            if ((align1.orientationForwardOnAxis()) != (align2.orientationForwardOnAxis())) {
                throw new IllegalArgumentException(
                        "ExonAlignmentComparator::compare " + 
                        " called for two alignments in different orientations. Orientation " + 
                        " isForward for align1: " + 
                        align1.orientationForwardOnAxis() + 
                        " orientation isForward for align2: " + 
                        align2.orientationForwardOnAxis());
            }

            boolean forwardOrientation = align1.orientationForwardOnAxis();

            if (align1.getAxis().getOid().equals(align2.getAxis().getOid())) {
                // Take care to make sure that handle reverse oriented geometry
                int align1LeftEdge = align1.getRangeOnAxis().getStart();
                int align2LeftEdge = align2.getRangeOnAxis().getStart();

                if (forwardOrientation) { // Want ordered from left to right

                    if (align1LeftEdge < align2LeftEdge) {
                        return -1;
                    } else if (align1LeftEdge == align2LeftEdge) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else { // Want ordered from right to left

                    if (align1LeftEdge > align2LeftEdge) {
                        return -1;
                    } else if (align1LeftEdge == align2LeftEdge) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            } else {
                throw new IllegalArgumentException(
                        "ExonAlignmentComparator::compare " + 
                        " called with AxisAlignments against different axes. align1 is on" + 
                        align1.getAxis().getOid() + " align2 " + 
                        align2.getAxis().getOid() + 
                        " . Can only compare alignments to the same axis");
            }
        }

        public boolean equals(Object obj) {
            if (obj instanceof ExonAlignmentComparator) {
                return true;
            } else {
                return false;
            }
        }
    } // End of ExonAlignmentComparator class
}