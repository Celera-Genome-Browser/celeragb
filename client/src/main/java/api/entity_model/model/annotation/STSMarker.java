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
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.SingleAlignmentMultipleAxes;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.OID;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */
public class STSMarker extends ComputedFeature
    implements SingleAlignmentMultipleAxes {
    //== Construction Methods =========================================
    public STSMarker(OID oid, String displayName, EntityType type, 
                     String discoveryEnvironment)
              throws InvalidFeatureStructureException {
        this(oid, displayName, type, discoveryEnvironment, null, null, 
             FeatureDisplayPriority.DEFAULT_PRIORITY);
    }

    public STSMarker(OID oid, String displayName, EntityType type, 
                     String discoveryEnvironment, 
                     FacadeManagerBase readFacadeManager, Feature superFeature, 
                     byte displayPriority)
              throws InvalidFeatureStructureException {
        super(oid, displayName, type, discoveryEnvironment, readFacadeManager, 
              superFeature, displayPriority);
    }

    //== Alignment Methods =========================================

    /**
     * @return The single Alignment to one of the (many) Axes this entity is aligned to.
     */
    public Alignment getOnlyAlignmentToAnAxis(Axis anAxis) {
        return super.getOnlyAlignmentToAnAxis(anAxis);
    }

    /**
     * @return The single GeometricAlignment to one of the (many) Axes this entity is aligned to.
     * If the alignment is NOT a GeometricAlignment, this will return null.
     */
    public GeometricAlignment getOnlyGeometricAlignmentToAnAxis(Axis anAxis) {
        return super.getOnlyGeometricAlignmentToAnAxis(anAxis);
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
            theVisitor.visitSTSMarker(this);
        } catch (Exception ex) {
            handleException(ex);
        }
    }
}