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
package api.facade.concrete_facade.shared;

import api.facade.concrete_facade.shared.feature_bean.FeatureBean;
import api.stub.data.OID;

import java.util.ArrayList;
import java.util.List;

/**
 * Title:        Obsolete-on-Axis Criterion
 * Description:  Keep/discard criterion for feature models.  Keeps those
 *               which refer (or align to) the axis set in the constructor,
 *               which are root features (no parent), and which are obsolete.
 * @author  Les Foster
 * @version $Id$
 */

public class ObsoleteOnAxisCriterion implements FeatureCriterion {

  private OID mAxisOid;

  /**
   * Constructor with all comparison data for the criterion.
   *
   * @param OID lAxisOid the OID of the axis to which feature must align.
   */
  public ObsoleteOnAxisCriterion(OID lAxisOid) {
    mAxisOid = lAxisOid;
  } // End constructor

  /**
   * In the given model, find all that adhere to the rule: is obsolete.
   *
   * @param FeatureBean lModel the model in which to look for obsolete features.
   */
  public List allMatchingIn(FeatureBean lModel) {

    List lReturnList = new ArrayList();
    if (lModel.getAxisOfAlignment().equals(mAxisOid) && (lModel.getParent() == null) && lModel.isObsolete()) {
      lReturnList.add(lModel);
    } // Got it

    return lReturnList;

  } // End method: allMatchingIn

  /*
      Keep this recursive visiting mechanism handy...just in case.
  public boolean isTrueFor(FeatureModel lModel) {
    if () {
    }
    else {
      if (lModel instanceof CompoundFeatureModel) {
        CompoundFeatureModel lCompoundModel = (CompoundFeatureModel)lModel;
        List lSubModels = lCompoundModel.getChildren();
        for (Iterator it = lSubModels.iterator(); it.hasNext(); ) {
          if (isTrueFor((FeatureModel)it.next()))
            return true;
        } // For all sub models
      } // May have child features.
    } // Look within.
  } // End method: isTrueFor
  */

} // End class
