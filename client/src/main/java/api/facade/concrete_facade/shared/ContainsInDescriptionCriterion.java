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

import api.facade.concrete_facade.shared.feature_bean.CompoundFeatureBean;
import api.facade.concrete_facade.shared.feature_bean.FeatureBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Title:        Feature comparison criterion.  Helps to collect matching features.
 * Description:  Test models to see if they have data in their description tags
 *               that match the value that has been passed.
 * @author Les Foster
 * @version $Id$
 */
public class ContainsInDescriptionCriterion implements FeatureCriterion {

  private String mTargetString = null;
  private List mReturnList = null;

  /** Takes the comparison value as arg to constructor. */
  public ContainsInDescriptionCriterion(String lTargetString) {
    mTargetString = lTargetString.toUpperCase();
    mReturnList = new ArrayList();
  } // End constructor

  /**
   * Checks the model passed to see if it or one of its descendants
   * has a description containing the text of the target value.
   */
  public List allMatchingIn(FeatureBean lModel) {
    mReturnList.clear();
    findMatchingModels(lModel);
    return mReturnList;
  } // End method

  /**
   * Finds all at this level or below.
   */
  private void findMatchingModels(FeatureBean lModel) {
    // Accumulate matches at both parent and child levels.
    String lDescription = lModel.getDescription();
    if ((lDescription != null) && (lDescription.toUpperCase().indexOf(mTargetString) >= 0)) {
      mReturnList.add(lModel);
    } // Got it first try.

    if (lModel instanceof CompoundFeatureBean) {
      CompoundFeatureBean compoundModel = (CompoundFeatureBean)lModel;
      List subModels = compoundModel.getChildren();
      for (Iterator it = subModels.iterator(); it.hasNext(); ) {
        findMatchingModels((FeatureBean)it.next());
      } // For all submodels.
    } // May have child features.

    return;
  } // End method

} // End class
