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

import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.OID;
import api.stub.data.PromotionReport;

import java.sql.SQLException;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 *
 * A MiscCuratedFeature can be both a SuperFeature and a SubFeature.
 */
public class MiscCuratedFeature extends CuratedFeature
    implements SuperFeature, SubFeature {
    /**
     * Constructor...
     */
    public MiscCuratedFeature(OID oid, String displayName, EntityType type, 
                              String discoveryEnvironment)
                       throws InvalidFeatureStructureException {
        this(oid, displayName, type, discoveryEnvironment, null, null, 
             FeatureDisplayPriority.DEFAULT_PRIORITY);
    }

    /**
     * Constructor...
     */
    public MiscCuratedFeature(OID oid, String displayName, EntityType type, 
                              String discoveryEnvironment, 
                              FacadeManagerBase readFacadeManager, 
                              Feature superFeature, byte displayPriority)
                       throws InvalidFeatureStructureException {
        super(oid, displayName, type, discoveryEnvironment, readFacadeManager, 
              superFeature, displayPriority);
    }

    /**
     * Convenience method to determine if this Feature is "Simple".
     * The algorithm for determining this is different for "Misc" features.
     * Because a Misc feature always has the potential of having children, our
     * best guess is based on if it actually has children.
     */
    public boolean isSimple() {
        return !this.hasSubFeatures();
    }

    /**
     *
     */
    public PromotionReport doBuildPromoteInstructions(Axis axis, 
                                                      String parentReplacesDirective)
                                               throws SQLException {
        throw new RuntimeException(this.getClass() + 
                                   "::doBuildPromoteInstructions needs " + 
                                   " to be ported from the existing BRA_CTr30b4 code");
    }

    public PromotionReport doCheckPromotableOnAxis(Axis axis, 
                                                   PromotionReport report) {
        throw new RuntimeException(this.getClass() + 
                                   "::doCheckPromotableOnAxis needs " + 
                                   " to be ported from the existing BRA_CTr30b4 code");
    }
}