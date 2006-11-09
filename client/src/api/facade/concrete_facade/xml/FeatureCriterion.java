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

import api.facade.concrete_facade.xml.model.FeatureModel;

import java.util.List;

/**
 * Title:        Feature Criterion interface
 * Description:  Implement this to build a criterion for selecting of
 *               features during a search scan of an input file.
 * @author Les Foster
 * @version $Id$
 */
public interface FeatureCriterion {

   /**
    * Return true if the model passed matches the criterion of the implementing
    * class.
    */
   public abstract List allMatchingIn(FeatureModel lModel);

} // End interface
