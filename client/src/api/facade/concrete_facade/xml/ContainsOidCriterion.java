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
package api.facade.concrete_facade.xml;

import api.facade.concrete_facade.xml.model.CompoundFeatureModel;
import api.facade.concrete_facade.xml.model.FeatureModel;
import api.stub.data.OID;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Title:        Feature comparison criterion.  Helps to collect matching features.
 * Description:  Test to see if any OID in a given model's hierarchy is that being sought.
 * @author Les Foster
 * @version $Id$
 */
public class ContainsOidCriterion implements FeatureCriterion {

  private OID mTargetOid = null;

  /** Takes the comparison value as arg to constructor. */
  public ContainsOidCriterion(OID lTargetOid) {
    mTargetOid = lTargetOid;
  } // End constructor

  /**
   * Checks the model passed to see if it or one of its descendants
   * is the OID being targetted by this search.
   */
  public List allMatchingIn(FeatureModel lModel) {
    List lReturnList = new ArrayList();
    if (isTrueFor(lModel))
      lReturnList.add(lModel);
    return lReturnList;
  } // End method

  /**
   * Override for debug purposes.
   */
  public String toString() {
    return ""+mTargetOid.toString()+" GVId: "+mTargetOid.getGenomeVersionId();
  } // End method

  /**
   * Look all down the hierarchy, recursively, for OIDs that match,
   * and if they do, simply return true.
   */
  private boolean isTrueFor(FeatureModel lModel) {
    if (lModel.getOID().equals(mTargetOid)) {
      return true;
    } // Got it first try.
    else {
      if (lModel instanceof CompoundFeatureModel) {
        CompoundFeatureModel compoundModel = (CompoundFeatureModel)lModel;
        List subModels = compoundModel.getChildren();
        for (Iterator it = subModels.iterator(); it.hasNext(); ) {
          if (isTrueFor((FeatureModel)it.next()))
            return true;
        } // For all sub models
      } // May have child features.
    } // Look within.

    return false;
  } // End method

} // End class
