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
package api.entity_model.access.observer;

/**
 * Title:        Genome Browser Client
 * Description:  Interface that defines a set of callbacks that can be used
 *               to effect a non blocking call on an instance of HitAlignmentFeature for
 *               any method that accepts a HitAlignmentFeatureObserver instance as a parameter.
 *               The HitAlignmentFeature will make a callback to the appropriate method on
 *               this interface when the load has been performed.
 * @author       Peter Davies
 * @version $Id$
 *
 * Note: Must subclass this and override at least one of these.
 */

import api.entity_model.model.annotation.HitAlignmentDetailFeature;

import java.util.Collection;

public abstract class HitAlignmentDetailFeatureObserverAdapter {

  public void noteQueryAlignedResiduesLoaded(HitAlignmentDetailFeature feature,String queryAlignedResidues) {}
  public void noteSubjectAlignedResiduesLoaded(HitAlignmentDetailFeature feature,String subjectAlignedResidues){}
  public void noteSubjectDefsLoaded(HitAlignmentDetailFeature entity, Collection defs) {}
}
