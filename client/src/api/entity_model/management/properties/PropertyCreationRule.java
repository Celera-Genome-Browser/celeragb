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

import api.entity_model.model.fundtype.EntityTypeSet;
import api.entity_model.model.fundtype.GenomicEntity;
import api.stub.data.ControlledVocabUtil;
import api.stub.data.GenomicProperty;

import java.util.List;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 *
 * This class is designed as the base class for PropertyCreationRules.  It
 * will be used by the PropertyMgr to create new properties based on rules.
 * The rule itself does the filtering to see if a property should be added.
 * It than creates the property if necessary, but delagates the propery value
 * formatting to the PropertyValueFormatter class.  This is somewhat akin to
 * Date and DateFormatter.  This class will only filter on Entity Types.  Others
 * can be defined to filter on other attributes of the entity.
 *
 * WARNING: DO NOT CALL getProperties on the passed entity!  The properties
 * passed in to processEntity are the properties.  Calling getProperties
 * will through the design into an infinate loop!
 *
 * @author Peter Davies (peter.davies)
 * @version $Id$
 *
 */


public class PropertyCreationRule implements java.io.Serializable {

  private EntityTypeSet entityTypes;
  private String ruleName;
  private String newPropertyName;
  private PropertyValueFormatter propertyValueFormatter;

  /**
   * @param ruleName - MUST be unique to all rules.
   * @param entityTypes - a set of entity types that this rule will be applied on
   * @param newPropertyName - the name of the property that will be constructed
   * @param propertyValueFormatter - the formatter for the value of the property
   */
  public PropertyCreationRule(String ruleName, EntityTypeSet entityTypes,
    String newPropertyName, PropertyValueFormatter propertyValueFormatter) {

    this.ruleName=ruleName;
    this.entityTypes=entityTypes;
    this.propertyValueFormatter=propertyValueFormatter;
    this.newPropertyName=newPropertyName;
  }

  /**
   * This constructor will create a rule that will run against all entity types
   */

  public PropertyCreationRule(String ruleName, String newPropertyName,
    PropertyValueFormatter propertyValueFormatter) {
     this(ruleName,null,newPropertyName,propertyValueFormatter);
  }
  /**
   * @return the name of this rule.
   */
  public String getName() {
    return ruleName;
  }

  /**
   * @return the formatting rule that is being applied
   */
  public String getFormatRuleString() {
    return propertyValueFormatter.getFormatRuleString();
  }

  /**
   * This method is called by the PropertyMgr for every entity that
   * properties are requested on.
   *
   * @param originalProperties - the stored properties of the entity
   * @parameter entity - the entity that needs to know if any properties are
   * to be added
   *
   * WARNING: DO NOT CALL getProperties on the passed entity!  The properties
   * passed in to processEntity are the properties.  Calling getProperties
   * will through the design into an infinate loop!
   *
   *
   * @return a new property that should be added by this rule or null
   *
   */
  public GenomicProperty processEntity(GenomicEntity entity,
    List originalProperties){

    if (!isCorrectEntityType(entity)) return null;
    return createNewProperty(entity,originalProperties);
  }

  public String toString() {
    return getName()+" : "+getFormatRuleString();
  }

  /**
   * Method designed for use by sub-classes to create the actual property,
   * once filtering has determined that a property should be created.
   */
  protected final GenomicProperty createNewProperty(GenomicEntity entity,
    List originalProperties){

    String initialValue=propertyValueFormatter.formatInitialValue(entity,originalProperties);
    GenomicProperty newProperty=new GenomicProperty(newPropertyName,"",initialValue,
      false,ControlledVocabUtil.getNullVocabIndex(),true);
    return newProperty;
  }

  /**
   * Method designed for use by sub-classes in their processEntity methods
   * to determine if the entity is of the the correct entity type
   */
  protected final boolean isCorrectEntityType(GenomicEntity entity) {
    if (entityTypes!=null && !entityTypes.contains(entity.getEntityType()))
      return false;
    return true;
  }
}