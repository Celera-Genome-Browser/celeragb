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
import api.entity_model.model.fundtype.EntityType;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.OID;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version      $Id$
 */
public class ComputedCodon extends ComputedSingleAlignSingleAxisFeature
    implements CodonFeature, SubFeature {
    private static final EntityType START_CODON = EntityType.getEntityTypeForName(
                                                          "_COMPUTED_START_CODON");
    private static final EntityType STOP_CODON = EntityType.getEntityTypeForName(
                                                         "_COMPUTED_STOP_CODON");

    //== Construction Methods =========================================
    public ComputedCodon(OID oid, String displayName, EntityType type, 
                         String discoveryEnvironment)
                  throws InvalidFeatureStructureException {
        this(oid, displayName, type, discoveryEnvironment, null, 
             FeatureDisplayPriority.DEFAULT_PRIORITY);
    }

    public ComputedCodon(OID oid, String displayName, EntityType type, 
                         String discoveryEnvironment, 
                         FacadeManagerBase readFacadeManager, 
                         byte displayPriority)
                  throws InvalidFeatureStructureException {
        super(oid, displayName, type, discoveryEnvironment, readFacadeManager, 
              null, displayPriority);
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

    //== Sub-Typing Methods =========================================
    public boolean isStartCodon() {
        return getEntityType().equals(START_CODON);
    }

    public boolean isStopCodon() {
        return getEntityType().equals(STOP_CODON);
    }

    public boolean isTranslationStartCodon() {
        return false;
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
            theVisitor.visitComputedCodon(this);
        } catch (Exception ex) {
            handleException(ex);
        }
    }
}