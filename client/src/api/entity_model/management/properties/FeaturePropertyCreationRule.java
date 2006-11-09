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
package api.entity_model.management.properties;

import api.entity_model.model.annotation.Feature;
import api.entity_model.model.fundtype.EntityTypeSet;
import api.entity_model.model.fundtype.GenomicEntity;
import api.stub.data.GenomicProperty;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies (peter.davies)
 * @version $Id$
 *
 * This class extends the PropertyCreationRule class to allow rules that
 * filter based on DiscoveryEnvironment.
 */


public class FeaturePropertyCreationRule extends PropertyCreationRule {

  private Set discoveryEnvironments;
  private boolean andFilterCriteria;


  /**
   * This constructor will filter on discoverEnvironment, regardless of EntityType
   */
  public FeaturePropertyCreationRule(String ruleName, String[] discoveryEnvironments,
    String propertyName, PropertyValueFormatter propertyValueFormatter) {

    super(ruleName,null,propertyName,propertyValueFormatter);
    this.discoveryEnvironments=new HashSet(Arrays.asList(discoveryEnvironments));
  }

  /**
   * This constructor will filter on discoverEnvironment, in addition to EntityType
   *
   * @parameter andFilterCriteria - true if both entity type and discovery env needo to match
   *  false if only one has to match (or)
   */
  public FeaturePropertyCreationRule(String ruleName,
    EntityTypeSet entityTypes, String[] discoveryEnvironments,
    String propertyName, PropertyValueFormatter propertyValueFormatter, boolean andFilterCriteria) {

    super(ruleName,entityTypes,propertyName,propertyValueFormatter);
    this.andFilterCriteria=andFilterCriteria;
    this.discoveryEnvironments=new HashSet(Arrays.asList(discoveryEnvironments));
  }

  public GenomicProperty processEntity(GenomicEntity entity,
    List originalProperties){

    if (andFilterCriteria) {
      if (!isCorrectEntityType(entity)) return null;
      if (!(entity instanceof Feature)) return null;
      Feature feature=(Feature)entity;
      if (discoveryEnvironments!=null && !discoveryEnvironments.contains(
        feature.getEnvironment())) return null;
    }
    else {
      if (!isCorrectEntityType(entity) && (discoveryEnvironments==null || !(entity instanceof Feature) ||
        !discoveryEnvironments.contains(((Feature)entity).getEnvironment()))) {
          return null;
      }
    }
    return createNewProperty(entity,originalProperties);
  }
}