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
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.SingleAlignmentSingleAxis;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.OID;

import java.util.Set;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */
public class ComputedSingleAlignSingleAxisFeature extends ComputedFeature
    implements SingleAlignmentSingleAxis {
    //== Construction Methods =========================================
    public ComputedSingleAlignSingleAxisFeature(OID oid, String displayName, 
                                                EntityType type, 
                                                String discoveryEnvironment)
        throws InvalidFeatureStructureException {
        this(oid, displayName, type, discoveryEnvironment, null, null, 
             FeatureDisplayPriority.DEFAULT_PRIORITY);
    }

    public ComputedSingleAlignSingleAxisFeature(OID oid, String displayName, 
                                                EntityType type, 
                                                String discoveryEnvironment, 
                                                FacadeManagerBase readFacadeManager, 
                                                Feature superFeature, 
                                                byte displayPriority)
        throws InvalidFeatureStructureException {
        super(oid, displayName, type, discoveryEnvironment, readFacadeManager, 
              superFeature, displayPriority);
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
}