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
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.OID;
import api.stub.geometry.MutableRange;

import java.util.Collection;
import java.util.Iterator;


public class HSPFeature extends HitAlignmentDetailFeature
    implements SubFeature, java.io.Serializable {
    /**
     * The adjacent HSP feature that comes before this HSP in the subject
     * sequence.
     */
    private OID previousHsp = null;
    private boolean isPreviousHspAdjacent = false;

    /**
     * The adjacent HSP feature that comes after this HSP in the subject
     * sequence.
     */
    private OID subsequentHsp = null;
    private boolean isSubsequentHspAdjacent = false;



    /**
     * Constructors...
     */
    public HSPFeature(final OID oid, final String displayName, final EntityType type,
                      final String discoveryEnvironment)
               throws InvalidFeatureStructureException {
        this(oid, displayName, type, discoveryEnvironment, null, null, 
             FeatureDisplayPriority.DEFAULT_PRIORITY);
    }

    public HSPFeature(final OID oid, final String displayName, final EntityType type,
                      final String discoveryEnvironment,
                      final FacadeManagerBase readFacadeManager, final Feature superFeature,
                      final byte displayPriority)
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
    public boolean willAcceptSuperFeature(final Feature newSuperFeature) {
        if (!super.willAcceptSuperFeature(newSuperFeature)) {
            return false;
        }

        // Must be some kind of Transcript
        return (newSuperFeature instanceof BlastHit);
    }

    /**
     * Determine if I will accept a Feature as a super feature of mine.
     * This is usually called right before a call to setSuperFeature on the
     * mutator class.
     * Subclasses can over-ride this method to make sure that there is
     * the correct hierarchy of containment... such as Gene, Transcript, Exon.
     * Subclasses should ALWAYS be more restrictive than thier super classes.
     */
    public boolean willAcceptSubFeature(final Feature newSubFeature) {
        if (!super.willAcceptSubFeature(newSubFeature)) {
            return false;
        }

        return false;
    }

    /**
     * get the frame of the query relative to a genomic axis
     */
    public int getQueryFrame(final Axis theAxis, final boolean gapped) {
        GeometricAlignment geomAlign;
        int frame;

        if (theAxis == null) {
            throw new IllegalStateException("HSPFeatPI passed null axis.");
        }

        // It's faster to ask the AlignableGenomicEntity.getAlignmentsToAxis(axis)
        // than it is to ask Axis.getAlignmentsToEntity(this);
        final Collection alignments = this.getAlignmentsToAxis(theAxis);

        // theAxis.getAlignmentsToEntities(new AlignmentsToAlignableFilter(this));
        // AlignmentRange[] ranges=theAxis.findAlignedRangesFor(this);
        if (alignments.size() < 1) {
            throw new IllegalStateException(
                    "Error no alignment found for feature = " + this + 
                    " on axis=" + theAxis);
        }

        final Iterator itr = alignments.iterator();
        final Object alignmentObject = itr.next();

        if (!(alignmentObject instanceof GeometricAlignment)) {
            throw new IllegalStateException(
                    "Error; non-geometric alignment found for feature=" + 
                    this + " on axis=" + theAxis);
        }

        geomAlign = (GeometricAlignment) alignmentObject;

        final MutableRange mr = new MutableRange(geomAlign.getRangeOnAxis());

        if (mr.isReversed()) {
            // frame = -1 * (((theAxis.getMagnitude(false) - mr.getStart()) % 3) + 1);
            frame = -1 * (((theAxis.getMagnitude() - mr.getStart()) % 3) + 1);
        } else {
            frame = (mr.getStart() % 3 + 1);
        }

        //System.out.println("HSPFeatPI: qetQueryFrame: range=" + mr + " frame=" + frame);
        return frame;
    }

    /**
     * Get the facade...
    private HSPFacade getHSPFacade() {
      return ( ( HSPFacade )getFacade() );
    }
     */
    /**
     * Accepts visitors that implement GenomicEntityVisitor. acceptVisitorForSelf
     * is specialized by every subclass of GenomicEntity to call the appropriate
     * "visit...(...)" function on the passed visitor.
     * @param theVisitor the visitor.
     */
    public void acceptVisitorForSelf(final GenomicEntityVisitor theVisitor) {
        try {
            theVisitor.visitHSPFeature(this);
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * Returns the adjacent HSP feature that comes before this HSP in the 
     * subject sequence.
     *
     * @return     Instance of OID referencing the previous adjacent HSP
     *             feature. Otherwise, null if there is no previously 
     *             adjacent HSP.
     */
    public OID getPreviousAdjacentHSP() {
        return (this.isPreviousHspAdjacent ? this.previousHsp : null);
    }

    /**
     * Sets the adjacent HSP feature that comes before this HSP in the subject
     * sequence.
     *
     * @param      aPreviousHSP   Instance of OID referencing the previous adjacent HSP
     *                            feature.
     */
    public void setPreviousAdjacentHSP(final OID aPreviousHSP) {
        this.previousHsp = aPreviousHSP;
        this.isPreviousHspAdjacent = true;
    }

    /**
     * Returns the adjacent HSP feature that comes after this HSP in the 
     * subject sequence.
     *
     * @return     Instance of OID referencing the subsequent adjacent HSP
     *             feature. Otherwise, null if there is no subsequently 
     *             adjacent HSP.
     */
    public OID getSubsequentAdjacentHSP() {
        return (this.isSubsequentHspAdjacent ? this.subsequentHsp : null);
    }

    /**
     * Sets the adjacent HSP feature that comes after this HSP in the subject
     * sequence.
     *
     * @param      aSubsequentHSP Instance of OID referencing the previous adjacent HSP
     *                            feature.
     */
    public void setSubsequentAdjacentHSP(final OID aSubsequentHSP) {
        this.subsequentHsp = aSubsequentHSP;
        this.isSubsequentHspAdjacent = true;
    }


    public OID getPreviousNonAdjacentHSP() {
        return (this.isPreviousHspAdjacent ? null : previousHsp);
    }

    public void setPreviousNonAdjacentHSP(final OID aPreviousHSP) {
        this.previousHsp = aPreviousHSP;
        this.isPreviousHspAdjacent = false;
    }

    public OID getSubsequentNonAdjacentHSP() {
        return (this.isSubsequentHspAdjacent ? null : subsequentHsp);
    }

    public void setSubsequentNonAdjacentHSP(final OID aSubsequentHSP) {
        this.subsequentHsp = aSubsequentHSP;
        this.isSubsequentHspAdjacent = false;
    }
}