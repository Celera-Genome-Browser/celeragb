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
/*********************************************************************
  *********************************************************************
    CVS_ID:  $Id$
  *********************************************************************/
package api.facade.concrete_facade.gff3.feature_facade;

import api.facade.abstract_facade.annotations.ExonFacade;
import api.facade.concrete_facade.gff3.DataLoader;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;

import java.util.Iterator;
import java.util.Map;

public class Gff3ExonFacade extends Gff3FeatureFacade implements ExonFacade {

  /**
   * Override of inheritProperties to source up only those properties that
   * are specific to exons.
   */
  protected Map inheritProperties(OID featureOID) {
    Map returnMap = super.inheritProperties(featureOID);

    DataLoader featureLoader = null;
    int siblingPosition = -1;
    String siblingPositionStr = null;
    GenomicProperty orderNumberProperty = null;

    // Multiple loaders may exist per feature.  This loop
    // will try and take the first instance of any given property,
    // by name, and will discard any additional values.  In this
    // way, it maximizes chances of getting data from _somewhere_.
    for (Iterator<DataLoader> it = getGff3LoadersForFeature(featureOID).iterator(); (orderNumberProperty == null) && it.hasNext(); ) {
      featureLoader = it.next();

      siblingPosition = featureLoader.getSiblingPosition(featureOID);
      if (siblingPosition != -1) {
        siblingPositionStr = new Integer(siblingPosition+1).toString();
        orderNumberProperty = createDefaultSettingsProperty(ExonFacade.ORDER_NUM_PROP, siblingPositionStr);
        returnMap.put(orderNumberProperty.getName(), orderNumberProperty);
      } // Found position.
    } // For all loaders.

    return returnMap;

  } // End method: inheritProperties

} // End class: XmlExonFacade

