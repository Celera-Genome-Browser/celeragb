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
import api.entity_model.model.fundtype.EntityType;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.ControlledVocabUtil;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;
import api.stub.geometry.Range;

import java.util.HashSet;
import java.util.Set;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version      $Id$
 */
public class SpliceSite extends ComputedSingleAlignSingleAxisFeature
    implements SubFeature {
    private static final EntityType ACCEPTOR = EntityType.getEntityTypeForName(
                                                       "_ACCEPTOR_SPLICE_SITE");
    private static final EntityType DONOR = EntityType.getEntityTypeForName(
                                                    "_DONOR_SPLICE_SITE");
    private float score;
    private int position; // Position on genomic axis

    public SpliceSite(OID oid, String displayName, EntityType type, 
                      String discoveryEnvironment)
               throws InvalidFeatureStructureException {
        this(oid, displayName, type, discoveryEnvironment, null, 
             FeatureDisplayPriority.DEFAULT_PRIORITY);
    }

    public SpliceSite(OID oid, String displayName, EntityType type, 
                      String discoveryEnvironment, 
                      FacadeManagerBase readFacadeManager, byte displayPriority)
               throws InvalidFeatureStructureException {
        super(oid, displayName, type, discoveryEnvironment, readFacadeManager, 
              null, displayPriority);
    }

    public boolean isAcceptorSpliceSite() {
        return getEntityType().equals(ACCEPTOR);
    }

    public boolean isDonorSpliceSite() {
        return getEntityType().equals(DONOR);
    }

    public void setScore(float f) {
        this.score = f;
    }

    public float getScore() {
        return score;
    }

    /**
     * Get the position on the genomic axis of this splice site.
     */
    public int getPosition() {
        return position;
    }

    /** Override to allow display of local properties */
    public Set getProperties() {
        // Make sure this entity has it's calculated properties.
        Set props = new HashSet();
        props.add(new GenomicProperty("acceptor", "", 
                                      (new Boolean(isAcceptorSpliceSite())).toString(), 
                                      false, 
                                      ControlledVocabUtil.getNullVocabIndex()));
        props.add(new GenomicProperty("score", "", 
                                      (new Float(score)).toString(), false, 
                                      ControlledVocabUtil.getNullVocabIndex()));

        GeometricAlignment spliceAlignment = (GeometricAlignment) this.getOnlyAlignmentToOnlyAxis();
        Range range = spliceAlignment.getRangeOnAxis();
        this.position = range.getStart();
        props.add(new GenomicProperty("position", "", 
                                      (new Integer(position)).toString(), false, 
                                      ControlledVocabUtil.getNullVocabIndex()));

        /**
         * @todo Do we need to add orientation property?
         */
        return props;
    }

    //== Visitation Methods =========================================

    /**
     * Accepts visitors that implement GenomicEntityVisitor. acceptVisitorForSelf
     * is specialized by every subclass of GenomicEntity to call the appropriate
     * "visit...(...)" function on the passed visitor.
     * @param theVisitor the visitor.
     */
    public void acceptVisitorForSelf(GenomicEntityVisitor theVisitor) {
        try {
            theVisitor.visitSpliceSite(this);
        } catch (Exception ex) {
            handleException(ex);
        }
    }
}