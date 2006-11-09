// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package api.entity_model.access.observer;

import api.entity_model.model.annotation.Feature;
import api.stub.data.ReplacementRelationship;

//import api.entity_model.model.annotation.CompositeFeature;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 *               All Rights Reserved
 * @author       Peter Davies
 * @version $Id$
 */


public interface FeatureObserver extends AlignableGenomicEntityObserver {
  // Feature Structure Notification...
  void noteSubFeatureAdded(Feature superFeature, Feature addedSubFeature);
  void noteSubFeatureRemoved(Feature previousSuperFeature, Feature removedSubFeature);
  // Feature Evidence Notification...
  void noteFeatureEvidenceAdded(Feature subjectFeature, Feature addedEvidenceFeature);
  void noteFeatureEvidenceRemoved(Feature subjectFeature, Feature removedEvidenceFeature);
  // Feature Replacement Notification...
  void noteWorkspaceReplacementStateChanged(Feature subjectFeature, ReplacementRelationship theRepRel);
  void notePromotedReplacementStateChanged(Feature subjectFeature, ReplacementRelationship theRepRel);
}
