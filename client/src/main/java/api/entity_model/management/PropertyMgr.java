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

/**
 * Title:        Genome Browser<p>
 * Description:  <p>
 * @author Todd Safford
 * @version $Id$
 */

package api.entity_model.management;

import api.entity_model.management.properties.PropertyCreationRule;
import api.entity_model.management.properties.PropertyElement;
import api.entity_model.model.annotation.CuratedExon;
import api.entity_model.model.annotation.CuratedGene;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.fundtype.AlignableGenomicEntity;
import api.entity_model.model.fundtype.GenomicEntity;
import api.facade.abstract_facade.annotations.ExonFacade;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.facade.abstract_facade.annotations.GeneFacade;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.*;
import shared.util.PropertyConfigurator;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

public class PropertyMgr {

  // Constant Flags in the PropertySettings file.
  public static final String DISPLAY_NAME =             "DISPLAY_NAME";
  public static final String ORDER =                    "ORDER";
  public static final String NEW_ENTITY =               "NEW";
  public static final String UPDATE_ENTITY =            "UPDATED";
  public static final String COPIED_ENTITY =            "COPIED";
  private static final String EDITABLE =                "EDITABLE";
  private static final String UPDATE_EDITABLE =         "UPDATE_EDITABLE";
  private static final String AVAILABLE =               "AVAILABLE";
  private static final String REPLACE_STRING =          "REPLACE_STRING";
  private static final String IS_TOOLTIP =              "IS_TOOLTIP";

  // List of valid possible return values.
  private static final String YES =             "Yes";
  private static final String NO =              "No";
  private static final String BLANK =           "Blank";
  private static final String CLONE =           "Clone";
  private static final String CALCULATE =       "Calculate";
  private static final String REMOVE =          "Remove";
  private static final String SETHIGH =         "SetHigh";
  private static final String SYSDATE =         "SysDate";
  private static final String SYSLOGIN =        "SysLogin";
  private static final String UNK =             "UNK";
  public static final String DROP_PROPERTY =   "Drop Property";

  private static PropertyMgr propMgr = new PropertyMgr();
  private Properties allProperties = new Properties();
  private Properties uniqueProperties = new Properties();
  private TreeMap propertyElements = new TreeMap();
  // The attribute below is used for the "pretty" display names.
  private HashMap replaceStringCollection = new HashMap();
  private static String fileSep=File.separator;
  private GenomicEntity.GenomicEntityMutator geMutator = null;
  private Set availableProperties = new HashSet();
  private Map propertyCreationRules = new HashMap();

  static {
    PropertyMgr.getPropertyMgr();
  }

  private PropertyMgr() {
    loadSettings();
  }
  // Singleton enforcement.

  public static PropertyMgr getPropertyMgr() { return propMgr; }

  public void loadSettings() {
    Properties systemProps = PropertyConfigurator.getProperties();
    InputStream fileIn=this.getClass().getResourceAsStream(
      systemProps.getProperty("x.shared.PropertySettings"));
    try {
      allProperties.load(fileIn);
    }
    catch (Exception ex) {
      System.err.println(ex);
      ModelMgr.getModelMgr().handleException(ex);
      System.out.println("problem with parsing property settings");
    }
    // Parse the file for the unique Properties.
    for (Enumeration e = allProperties.propertyNames() ; e.hasMoreElements() ;) {
      String tempKey = new String ((String)e.nextElement());
      StringTokenizer mainToken = new StringTokenizer(tempKey,".");
      if (tempKey!=null && mainToken!=null && tempKey!="") {
        String uniqueKey = mainToken.nextToken();
        if (uniqueKey.equals(REPLACE_STRING)) {
          String beforeString = mainToken.nextToken();
          String afterString =  (String)allProperties.get(tempKey);
          replaceStringCollection.put(beforeString, afterString);
        }
        else uniqueProperties.setProperty(uniqueKey, uniqueKey);
      }
    }

    // This loop populates the PropertyElements collection
    for (Enumeration e2 = uniqueProperties.propertyNames(); e2.hasMoreElements();) {
      String tempName = new String ((String)e2.nextElement());
      String tempDisplayName = "";
      String tempNew = "";
      String tempNewEditable = "";
      String tempUpdate = "";
      String tempUpdateEditable = "";
      String tempAvailable = "";
      String tempOrder = "";
      String tempToolTipFlag = "";

      try {
        tempDisplayName = allProperties.getProperty(tempName+"."+DISPLAY_NAME);
        if (tempDisplayName==null) tempDisplayName = BLANK;
      }
      catch (Exception ex) { tempDisplayName = BLANK; }

      try {
        tempNew = allProperties.getProperty(tempName+"."+NEW_ENTITY);
        if (tempNew==null) tempNew = BLANK;
      }
      catch (Exception ex) { tempNew = BLANK; }

      try {
        tempNewEditable = allProperties.getProperty(tempName+"."+EDITABLE);
        if (tempNewEditable==null) tempNewEditable = NO;
      }
      catch (Exception ex) { tempNewEditable = NO; }

      try {
        tempUpdate = allProperties.getProperty(tempName+"."+UPDATE_ENTITY);
        if (tempUpdate==null) tempUpdate = CLONE;
      }
      catch (Exception ex) { tempUpdate = CLONE; }

      try {
        tempUpdateEditable = allProperties.getProperty(tempName+"."+UPDATE_EDITABLE);
        if (tempUpdateEditable==null) tempUpdateEditable = NO;
      }
      catch (Exception ex) { tempUpdateEditable = NO; }

      try {
        tempAvailable = allProperties.getProperty(tempName+"."+AVAILABLE);
        if (tempAvailable==null) tempAvailable = NO;
      }
      catch (Exception ex) { tempAvailable = NO; }

      try {
        tempOrder = allProperties.getProperty(tempName+"."+ORDER);
        if (tempOrder==null) tempOrder = "0";
      }
      catch (Exception ex) { tempOrder = "0"; }

      try {
        tempToolTipFlag = allProperties.getProperty(tempName+"."+IS_TOOLTIP);
        if (tempToolTipFlag==null) tempToolTipFlag = NO;
      }
      catch (Exception ex) { tempToolTipFlag = NO; }

      propertyElements.put(tempName,new PropertyElement(tempName, tempDisplayName,
        tempNew,tempNewEditable, tempUpdate,tempUpdateEditable, tempAvailable, tempOrder,
        tempToolTipFlag));

      // Populates the collection of Available properties.
      if (tempAvailable.equals(this.YES)) availableProperties.add(tempName);
    }

    // This is a debug loop.
    /*for (Enumeration e2 = uniqueProperties.propertyNames(); enum2.hasMoreElements();) {
      System.out.println(((PropertyElement)propertyElements.get(enum2.nextElement())).toString());
    }*/
  }


  private PropertyElement getPropertyElement(String propertyName) {
    // Init as "Not Available" just in case.
    if (propertyElements.get(propertyName)!=null)
      return (PropertyElement)propertyElements.get(propertyName);
    else {
      System.out.println("Property Manager does not have information about property "+propertyName);
      return new PropertyElement(propertyName, getPropertyDisplayName(propertyName),
        BLANK, NO, CLONE, NO, NO, "0", NO);
      // Just in case someone asks for a property that does not exist in the list.
    }
  }


  /**
   * This is the encompassing method for when an array is passed.
   */
   public void handleProperties(String useState, GenomicEntity testEntity, boolean handleChildrenAlso) {
      if (useState.equals(this.NEW_ENTITY) || useState.equals(this.COPIED_ENTITY)) {
        //  This area is to complete the Property collection for the entity.
        constructRemainingAvailableProperties(testEntity);
      }
      Set baseCollection= testEntity.getProperties();
      if (baseCollection==null) return;
      Set newCollection=new HashSet();
      Iterator baseCollectionIter=baseCollection.iterator();
      while(baseCollectionIter.hasNext()) {
        GenomicProperty testProperty = new GenomicProperty();
        testProperty = handleProperty((GenomicProperty)baseCollectionIter.next(), useState, testEntity);

        if (testProperty!=null) newCollection.add(testProperty);
      }
      testEntity.getMutator(this, "acceptGenomicEntityMutator");
      geMutator.setProperties(newCollection);
      if (!(testEntity instanceof Feature)) return;
      Feature tmpEntity = (Feature)testEntity;
      if (tmpEntity.getSubFeatureCount()>0 && handleChildrenAlso) {
        for (Iterator it = tmpEntity.getSubFeatures().iterator();it.hasNext();) {
          handleProperties(useState,(GenomicEntity)it.next(), handleChildrenAlso);
        }
      }
   }


 /**
  * This is the method that will utilize the PropertyMgr class so that property "rules"
  * and settings are applied on-the-fly.
  * useState = New or Updated
  */
 public GenomicProperty handleProperty(GenomicProperty baseProperty, String useState, GenomicEntity testEntity) {
      if (useState.equals(COPIED_ENTITY)) useState=UPDATE_ENTITY;
      String stateRule = new String("");
      String editRule = new String("");
      String finalValue = "Not Defined"; // Default value
      boolean editValue = false; // Default value
      PropertyElement propertyElement = new PropertyElement();
      propertyElement = propMgr.getPropertyElement(baseProperty.getName());

      // Set up the rules for the values to follow
      if (useState.equals(NEW_ENTITY)) {
        stateRule = propertyElement.getPropNew();
        editRule = propertyElement.getPropNewEditable();
      }
      else if (useState.equals(UPDATE_ENTITY)) {
        stateRule = propertyElement.getPropUpdate();
        editRule = propertyElement.getPropUpdateEditable();
      }

      // Check in this block to determine the initialValue for the GenomicProperty
      if (stateRule.equalsIgnoreCase(BLANK)) finalValue = "";

      else if (stateRule.equalsIgnoreCase(CALCULATE)) {
        finalValue = new String(calculateValue(baseProperty, testEntity));
        // The bottom line effectively removes this property from the collection.
        if (finalValue.equals(DROP_PROPERTY)) return null;
      }

      else if (stateRule.equalsIgnoreCase(CLONE)) {

        if((baseProperty.getInitialValue()==null || baseProperty.getInitialValue().equals(""))
	    &&( baseProperty.getName().equals(TranscriptFacade.CREATED_BY_PROP))){

	    finalValue=System.getProperties().getProperty("user.name");

        }else if
	  ((baseProperty.getInitialValue()==null || baseProperty.getInitialValue().equals(""))
	      &&( baseProperty.getName().equals(TranscriptFacade.DATE_CREATED_PROP))){
	    finalValue=Util.getDateTimeStringNow();

          }else{
	  finalValue = new String(baseProperty.getInitialValue());
         }

      }

      else if (stateRule.equalsIgnoreCase(SYSDATE)) {

        finalValue = Util.getDateTimeStringNow();
        }

      else if (stateRule.equalsIgnoreCase(SYSLOGIN))
          finalValue = System.getProperties().getProperty("user.name");

      else if (stateRule.equalsIgnoreCase(SETHIGH))
          finalValue = "High";

      else if (stateRule.equalsIgnoreCase(UNK)) {
          // Known property, but rule not defined.  Default action is to clone the old one.
          //System.out.println(baseCollection[x].getName() + " needs a known rule.");
          finalValue = new String(baseProperty.getInitialValue());
      }

      // Once again, the default action is to clone the initial value.
      else finalValue = new String(baseProperty.getInitialValue());

      // Check in this block to determine the Editable boolean
      // Any other values should be checked for in here.
      if (editRule.equalsIgnoreCase(YES)) editValue = true;
      else if (editRule.equalsIgnoreCase(NO)) editValue = false;

      if (!stateRule.equalsIgnoreCase(REMOVE)
                        && !editRule.equalsIgnoreCase(REMOVE))
        return new GenomicProperty (
          baseProperty.getName(),         // baseProperty.getGetMethod(),
        //  baseProperty.getSetMethod(),     baseProperty.getRenderingClass(),
          baseProperty.getEditingClass(),  finalValue,
          editValue,
          baseProperty.getVocabIndex(), baseProperty.getSubProperties(),
          baseProperty.isComputed());
      else {
     //   System.out.println(baseProperty.getName()+" got dropped.");
        return null;
      }
  }

  /**
   * This method is used to obtain and/or calculate the correct display name
   * for a given property.  This display name will be used for client display
   * purposes only.
   */
    public String getPropertyDisplayName(String targetProperty) {
      PropertyElement targetElement;
      String displayName = new String(BLANK);

      if (propertyElements.get(targetProperty)!=null) {
        targetElement = (PropertyElement)propertyElements.get(targetProperty);
        displayName = targetElement.getPropDisplayName();
      }
      if (displayName.equals(BLANK) || displayName==null) {
        displayName = targetProperty;
        displayName = displayName.replace('_',' ');
        displayName = capitalizeStringWords(displayName);
        for (Iterator it = replaceStringCollection.keySet().iterator(); it.hasNext();) {
          String tmpKey = (String) it.next();
          if (tmpKey!=null && !tmpKey.equals(""))
            displayName = replaceString(displayName, tmpKey,
              (String)allProperties.get(REPLACE_STRING+"."+tmpKey));
        }
      }
      return displayName;
    }


    /**
     * Now our favorite God class needs to help format the tooltip text.
     */
    public String getToolTipForEntity(AlignableGenomicEntity entity) {
      if (entity==null) {
        System.out.println("No tooltip for null entity.");
        return "";
      }

      HashSet tmpProperties = (HashSet)entity.getProperties();
      ArrayList finalList = new ArrayList();

      // Get the properties that should be tool tip'able and sort by order value.
      for (Iterator it = tmpProperties.iterator(); it.hasNext();) {
        GenomicProperty tmpProperty = (GenomicProperty)it.next();
        if (isPropertyIncludedInTooltip(tmpProperty.getName()))
          finalList.add(tmpProperty);
      }
      Object[] test = finalList.toArray();
      Arrays.sort(test, new MyTooltipComparator());
      finalList = new ArrayList(Arrays.asList(test));

      // Format the tooltip text and return.
      StringBuffer tooltipMessage = new StringBuffer("<html>"+
        "<body><table width=\"200\" border=\"1\" cellspacing=\"0\" cellpadding=\"2\"><tbody>"+
        "<tr><td valign=\"top\" bgcolor=\"#c0c0c0\" align=\"center\" nowrap=\"true\"><font size=\"-1\">"+
        "<b>"+entity.getOid().toString()+"</b></font></td></tr>");
      if (!finalList.isEmpty()) {
        tooltipMessage.append("<tr><td valign=\"top\">"+
          "<table cellpadding=\"2\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tbody>");

        for (Iterator it = finalList.iterator(); it.hasNext(); ) {
          GenomicProperty tmpProperty = (GenomicProperty)it.next();
          String tmpName = getPropertyDisplayName(tmpProperty.getName());
          String tmpValue = tmpProperty.getInitialValue();
          tooltipMessage.append("<tr><td valign=\"top\" nowrap=\"true\"><font size=\"-1\">"+
            tmpName+"</font></td><td valign=\"top\" nowrap=\"true\"><font size=\"-1\">"+
            tmpValue+"</font></td></tr>");
        }
        tooltipMessage.append("</tbody></table></td>");
      }
      tooltipMessage.append("</tr></tbody></table></body></html>");
      return tooltipMessage.toString();
    }


  /**
   * Method checks the setting file and returns a boolean whether the property is
   * tooltip'able.  This starts to stretch the reusability of the PropertySettings.properties file.
   */
  private boolean isPropertyIncludedInTooltip(String targetProperty) {
    PropertyElement targetElement;
    if (propertyElements.get(targetProperty)!=null) {
      targetElement = (PropertyElement)propertyElements.get(targetProperty);
      return targetElement.getPropTooltipFlag().equalsIgnoreCase(YES);
    }
    return false;
  }

    /**
     * This method is primarily used by the PropertiesTableModel to order the Properties
     * in the Inspector.
     */
    public Integer getPropertyOrderValue(String targetProperty) {
      PropertyElement targetElement;
      Integer orderValue = new Integer(0);
      if (propertyElements.get(targetProperty)!=null) {
        targetElement = (PropertyElement)propertyElements.get(targetProperty);
        orderValue = new Integer(targetElement.getPropOrder());
      }
      return orderValue;
    }


    /**
     * Helper method for the reformatting the display name.  This should go into
     * a shared helper class under a static method.  This assumes that the string
     * passed in can be tokenized by a space.
     */
    private String replaceString(String target, String beforeString, String afterString) {
      StringBuffer finalString = new StringBuffer("");
      StringTokenizer tokenizer = new StringTokenizer(target," ");
      while (tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken();
        if (token==null) break;
        // Check for beforeString and if found replace.
        if (token.equalsIgnoreCase(beforeString)) token = afterString;
        finalString.append(token.toString()+" ");
      }
      return finalString.toString().trim();
    }

    /**
     * Helper method for the reformatting the display name.  This should also go into
     * a shared helper class under a static method.  This assumes that the string
     * passed in can be tokenized by a space and will capitalize the first letter of
     * every word.
     */
    private String capitalizeStringWords(String target) {
      StringBuffer finalString = new StringBuffer("");
      StringTokenizer tokenizer = new StringTokenizer(target," ");
      while (tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken();
        if (token==null) break;
        //Capitalize the first letter.
        StringBuffer formattedToken = new StringBuffer(token);
        formattedToken = formattedToken.replace(0,1,formattedToken.substring(0,1).toUpperCase());
        finalString.append(formattedToken.toString()+" ");
      }
      return finalString.toString().trim();
    }

 /**
  * This method is called by the entity to apply user defined rules to
  * create new properties.  It should be called by the GenomicEntity before
  * handling back any properties in response to a getProperty request.
  * The properties returned from this method will be the original properties
  * and any user defined properties.  As such, what is returned from
  * this method should be returned to the user.
  */
 public List addPropertiesFromRules(
  GenomicEntity entity, List originalProperties) {

   PropertyCreationRule rule;
   if (propertyCreationRules.size()==0) return originalProperties;
   List newProperties=new ArrayList(originalProperties);
   GenomicProperty newProperty;
   for (Iterator it=propertyCreationRules.entrySet().iterator();it.hasNext(); ){
    rule=(PropertyCreationRule)(((Map.Entry)it.next()).getValue());
    newProperty=rule.processEntity(entity,originalProperties);
    if (newProperty!=null) newProperties.add(newProperty);
   }
   return newProperties;
 }

 public void addPropertyCreationRule(PropertyCreationRule rule) {
   propertyCreationRules.put(rule.getName(),rule);
 }

 /**
  * @return Set of PropertyCreationRules
  */
 public Set getPropertyCreationRules() {
  Set rules=new HashSet();
  for (Iterator it=propertyCreationRules.keySet().iterator();it.hasNext();) {
    rules.add(propertyCreationRules.get(it.next()));
  }
  return rules;
 }

  public Set getPropertyNamesForEntity(GenomicEntity entity) {
    Set entityPropertyNames=new HashSet();
    Class c = FeatureFacade.class;
    //Determine which facade to use
    FacadeManagerBase fmb = FacadeManager.getFacadeManager();
    try {
      c = fmb.getFacade(entity.getEntityType()).getClass();
    }
    catch (Exception ex) {
      ModelMgr.getModelMgr().handleException(ex);
    }
    // Get all the statically defined properties for the entity.
    Field[] fields = c.getFields();
    for (int i=0; i<fields.length; ++i) {
      Field field = fields[i];
      String fieldName = field.getName();
      // assuming that the prop names are static, so we can pass a null arg
      if(fieldName.endsWith("_PROP") ){
        Object o=null;
        try {
          o=field.get(null);
        }
        catch (Exception ex) {
          ModelMgr.getModelMgr().handleException(ex);
        }
        String fieldValueStr=o.toString();
          //System.out.println("field name "+fieldValueStr);
          entityPropertyNames.add(fieldValueStr);
     }
   }
   return entityPropertyNames;
  }



 public void removePropertyCreationRule(String ruleName){
  propertyCreationRules.remove(ruleName);
 }

 // This method is just to break out the calculation determination.
 private String calculateValue(GenomicProperty baseProperty, GenomicEntity testEntity) {
  //System.out.println("Calculating: "+baseProperty.getName());
  String test = baseProperty.getName();
 // if(test==null){return "";}
  if (test.equalsIgnoreCase(GeneFacade.GENE_ACCESSION_PROP) && testEntity instanceof CuratedGene) {
    String initialValue=testEntity.getProperty(test).getInitialValue();
    if (initialValue!=null &&
        initialValue!=""&& !initialValue.equals("null"))
      return initialValue;
  return AccessionGenerator.getAccessionGenerator().generateAccessionString("WG");
  }

  else if (test.equalsIgnoreCase(TranscriptFacade.GENE_ACCESSION_PROP) &&
    (testEntity instanceof CuratedTranscript ||
     testEntity instanceof CuratedExon)) {
    Feature parentFeat=((Feature)testEntity).getSuperFeature();
    if (parentFeat!=null && parentFeat.getProperty(GeneFacade.GENE_ACCESSION_PROP).getInitialValue()!=null)
      return parentFeat.getProperty(GeneFacade.GENE_ACCESSION_PROP).getInitialValue();
    // Return if no parent set.
    return "";
  }

  else if (test.equalsIgnoreCase(FeatureFacade.NUM_COMMENTS_PROP)) {
    if (testEntity.getProperty(test).getInitialValue()==null || testEntity.getProperty(test).getInitialValue()=="")
      return "0";
    return testEntity.getProperty(test).getInitialValue();
  }


  else if (test.equalsIgnoreCase(TranscriptFacade.CURATION_FLAGS_PROP)) {
    // Make sure that only Gene and Transcripts have curation flags
    int testEntityType = ((Feature)testEntity).getEntityType().value();
    // If below, do nothing.  Else return and drop property from list.
    if (testEntityType==EntityTypeConstants.NonPublic_Gene  ||
        testEntityType==EntityTypeConstants.NonPublic_Transcript) ;
    else return DROP_PROPERTY;
    if (testEntity.getProperty(test).getInitialValue()==null || testEntity.getProperty(test).getInitialValue()=="")
      return "2";
    return testEntity.getProperty(test).getInitialValue();
  }


  else if (test.equalsIgnoreCase(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP)) {
    if (testEntity instanceof CuratedExon) {
     Feature parentFeat=((Feature)testEntity).getSuperFeature();
      if (parentFeat!=null && parentFeat.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP).getInitialValue()!=null)
        return ((Feature)testEntity).getSuperFeature().getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP).getInitialValue();
      // Return if no parent set.
      return "";

    }
    else if (testEntity instanceof CuratedTranscript) {
      if (testEntity.getProperty(test).getInitialValue()!=null &&
          testEntity.getProperty(test).getInitialValue()!="")
        return testEntity.getProperty(test).getInitialValue();
      else return AccessionGenerator.getAccessionGenerator().generateAccessionString("WT");
    }
  }

  else if (test.equalsIgnoreCase(TranscriptFacade.HAS_MULTI_EXON_PROP)) {
    // Assuming that if it gets this far, must be a transcript.
    if(((CuratedTranscript)testEntity).getSubFeatureCount()<=1)
      return "false";
    else return "true";
  }

  else if (test.equalsIgnoreCase(GeneFacade.IS_ALTER_SPLICE_PROP)) {
    if(((CuratedGene)testEntity).getSubFeatureCount()<=1)
      return "false";
    else
      return "true";
  }

  else if (test.equalsIgnoreCase(GeneFacade.IS_PSEUDO_GENE_PROP)) {
    if (testEntity.getProperty(test).getInitialValue()==null)
      return "false";
    else if (testEntity.getProperty(test).getInitialValue()=="true")
      return "true";
    return "false";
  }

  else if (test.equalsIgnoreCase(FeatureFacade.GROUP_TAG_PROP)) {
    if (((Feature)testEntity).getEnvironment()==null){return "Curation";}
    else if (((Feature)testEntity).getEnvironment()=="") {
      return (((Feature)testEntity).getEntityType().toString());
    }
    return ((Feature)testEntity).getEnvironment();
  }

  else if (test.equalsIgnoreCase(FeatureFacade.FEATURE_TYPE_PROP)) {
     return (((Feature)testEntity).getEntityType().toString());
  }

   else if (test.equalsIgnoreCase(FeatureFacade.PARENT_FEATURE_ID_PROP)) {
    Feature parentFeat=((Feature)testEntity).getSuperFeature();
    if(parentFeat!=null)return (parentFeat.getOid().toString());
    return "";
   }
  // This block needs to be defined properly for NEW instance.  These are basically clones.
  // exon.updatePropertiesBasedOnOnlyAlignment(genomicAxis);
  // Above is being used elsewhere.
  else if (test.equalsIgnoreCase(FeatureFacade.AXIS_BEGIN_PROP)) {
    return ((Feature)testEntity).getProperty(FeatureFacade.AXIS_BEGIN_PROP).getInitialValue();
  }
  else if (test.equalsIgnoreCase(FeatureFacade.AXIS_END_PROP)) {
    return ((Feature)testEntity).getProperty(FeatureFacade.AXIS_END_PROP).getInitialValue();
  }
  else if (test.equalsIgnoreCase(FeatureFacade.ENTITY_LENGTH_PROP)) {
    return ((Feature)testEntity).getProperty(FeatureFacade.ENTITY_LENGTH_PROP).getInitialValue();
  }
  else if (test.equalsIgnoreCase(FeatureFacade.ENTITY_ORIENTATION_PROP)) {
    return ((Feature)testEntity).getProperty(FeatureFacade.ENTITY_ORIENTATION_PROP).getInitialValue();
  }
  else if (test.equalsIgnoreCase(FeatureFacade.GENOMIC_AXIS_ID_PROP)) {
    return ((Feature)testEntity).getProperty(FeatureFacade.GENOMIC_AXIS_ID_PROP).getInitialValue();
  }
  else if (test.equalsIgnoreCase(FeatureFacade.ORIENTATION_PROP)) {
    return ((Feature)testEntity).getProperty(FeatureFacade.ORIENTATION_PROP).getInitialValue();
  }
  else {
    //System.out.println(test+" has no Calculation.");
  }
  // This return is for the default case only.  Basically should not get this far.
  return baseProperty.getInitialValue();
 }






  private void constructRemainingAvailableProperties(GenomicEntity entity) {
     Set availablePropertyNamesForEntity = getAvailablePropertyNamesForEntity(entity);
     Set currentPropertyNamesForEntity = new HashSet();
     Set remainingAvailableGenomicProperties = new HashSet();
     // Populate collection of property names the entity already has.
     if (entity.getProperties()!=null) {
       for (Iterator it = entity.getProperties().iterator();it.hasNext();) {
          currentPropertyNamesForEntity.add(((GenomicProperty)it.next()).getName());
       }
     }
     entity.getMutator(this, "acceptGenomicEntityMutator");
     for (Iterator it = availablePropertyNamesForEntity.iterator();it.hasNext();){
        String tmpPropertyName=(String)it.next();
        if (!currentPropertyNamesForEntity.contains(tmpPropertyName))
          remainingAvailableGenomicProperties.add(constructProperty(tmpPropertyName));
     }
     geMutator.addProperties(remainingAvailableGenomicProperties);
  }


  /**
   *
   */
  private Set getAvailablePropertyNamesForEntity(GenomicEntity entity) {
    Set entityPropertyNames=new HashSet();
    Set availablePropertyNamesForEntity = new HashSet();
    Class c;
    //Determine which facade to use
    if(entity instanceof CuratedGene) c=GeneFacade.class;
    else if(entity instanceof CuratedTranscript) c=TranscriptFacade.class;
    else c=ExonFacade.class;

    // Get all the statically defined properties for the entity.
    Field[] fields = c.getFields();
    for (int i=0; i<fields.length; ++i) {
      Field field = fields[i];
      String fieldName = field.getName();
      // assuming that the prop names are static, so we can pass a null arg
      if(fieldName.endsWith("_PROP") ){
        Object o=null;
        try {
          o=field.get(null);
        }
        catch (Exception ex) {
          ModelMgr.getModelMgr().handleException(ex);
        }
        String fieldValueStr=o.toString();
          //System.out.println("field name "+fieldValueStr);
          entityPropertyNames.add(fieldValueStr);
     }
   }
   // Compare the property name collection to the Availability rules.
   for (Iterator it = entityPropertyNames.iterator(); it.hasNext();) {
    String tmpProperty = (String)it.next();
    if (availableProperties.contains(tmpProperty))
      availablePropertyNamesForEntity.add(tmpProperty);
   }
   return availablePropertyNamesForEntity;
  }


  /**
   * This method is used to construct a GenomicProperty object for a given entity
   * property.  It prepares the attributes based on generic or special cases
   * and assigns the initial value to this new GP.
   */
  public GenomicProperty constructPropertyFromScratch(String propName, String initialValue) {
    if (propName==null) propName="";
    if (initialValue==null) initialValue="";
    GenomicProperty newProperty = constructProperty(propName);
    newProperty.setInitialValue(initialValue);
    return newProperty;
  }

  private GenomicProperty constructProperty(String propName){
      GenomicProperty gp;
      // properties with ControlVocab

      if(propName.equals("display_priority")){
        gp=new GenomicProperty(propName,
        "","",false,"DISPLAY_PRIORITY",true);
      }else
      if(propName.equals("axis_begin")){
        gp=new GenomicProperty(propName,
        "","",false,ControlledVocabUtil.getNullVocabIndex(),true);
      }else
       if(propName.equals("axis_end")){
        gp=new GenomicProperty(propName,
        "","",false,ControlledVocabUtil.getNullVocabIndex(),true);
      }else
       if(propName.equals("entity_length")){
        gp=new GenomicProperty(propName,
        "","",false,ControlledVocabUtil.getNullVocabIndex(),true);
      }else
       if(propName.equals("genomic_axis_id")){
        gp=new GenomicProperty(propName,
        "","",false,ControlledVocabUtil.getNullVocabIndex(),true);
      }
      else
       if(propName.equals("id")){
        gp=new GenomicProperty(propName,
        "","",false,ControlledVocabUtil.getNullVocabIndex(),true);
      }


      else if(propName.equals("orientation")){
        gp=new GenomicProperty(propName,
        "","",false,"orientation");
      }
      // property is expandable
       else if(propName.equals("curation_flags")){
        gp=new GenomicProperty(propName,
        "","0",true,
        ControlledVocabUtil.getNullVocabIndex());
        constructCurationFlagsIfAbsent(gp);
       }

       else if(propName.equals("comments")){
        gp=new GenomicProperty(propName,
        "client.gui.other.dialogs.CommentsViewer",
        "",true,ControlledVocabUtil.getNullVocabIndex());
       }

       else if(propName.equals("gene_accession")){
           gp=new GenomicProperty(propName,
           /*"client.gui.components.annotation.ga_gene_curation.GeneCuration"*/"",
           "",false,ControlledVocabUtil.getNullVocabIndex());
        }

       // default case
       else{
        gp=new GenomicProperty(propName,"","",
        true,ControlledVocabUtil.getNullVocabIndex());
       }
       return gp;
   }


  private void constructCurationFlagsIfAbsent(GenomicProperty curationFlagProperty){
    GenomicProperty[] curationFlagsSubProps = curationFlagProperty.getSubProperties();
    if(curationFlagsSubProps==null || curationFlagsSubProps.length==0){
      GenomicProperty[] flags=new FlaggedGenomicProperty[18];

      flags[0]=new FlaggedGenomicProperty("Consensus Error",
         "","2",true,"CURATION_FLAG");

      flags[1]=new FlaggedGenomicProperty("Incorrect Boundary",
      "","2",true,"CURATION_FLAG");

      flags[2]=new FlaggedGenomicProperty("Is Partial",
      "","2",true,"CURATION_FLAG");

      flags[3]=new FlaggedGenomicProperty("End Of Scaffold",
      "","2",true,"CURATION_FLAG");

      flags[4]=new FlaggedGenomicProperty("Match Organization-Internal cDNA",
      "","2",true,"CURATION_FLAG");

      flags[5]=new FlaggedGenomicProperty("Known Gene",
      "","2",true,"CURATION_FLAG");

      flags[6]=new FlaggedGenomicProperty("Match Public mRNA",
      "","2",true,"CURATION_FLAG");

      flags[7]=new FlaggedGenomicProperty("Is Pseudogene",
      "","2",true,"CURATION_FLAG");

      flags[8]=new FlaggedGenomicProperty("With Gaps",
      "","2",true,"CURATION_FLAG");

      flags[9]=new FlaggedGenomicProperty("No Full Length cDNA",
       "","2",true,"CURATION_FLAG");

      flags[10]=new FlaggedGenomicProperty("Frame Shift",
       "","2",true,"CURATION_FLAG");

       flags[11]=new FlaggedGenomicProperty("Translation Exception",
       "","2",true,"CURATION_FLAG");

       flags[12]=new FlaggedGenomicProperty("Predicted ORF",
       "","2",true,"CURATION_FLAG");

       flags[13]=new FlaggedGenomicProperty("Retaining Intron",
       "","2",true,"CURATION_FLAG");

       flags[14]=new FlaggedGenomicProperty("Premature Termination",
       "","2",true,"CURATION_FLAG");

	   flags[15]=new FlaggedGenomicProperty("Is Bicistronic",
	   "","2",true,"CURATION_FLAG");

	   flags[16]=new FlaggedGenomicProperty("Is Chimeric",
	   "","2",true,"CURATION_FLAG");

	   flags[17]=new FlaggedGenomicProperty("Non-Coding Gene",
	   "","2",true,"CURATION_FLAG");

      curationFlagProperty.setSubProperties(flags);
    }
  }


  /**
  * getMutator() call back for genomic entities.....
  */
  public void acceptGenomicEntityMutator(GenomicEntity.GenomicEntityMutator gemutator){
   if(gemutator instanceof GenomicEntity.GenomicEntityMutator){
     this.geMutator=(GenomicEntity.GenomicEntityMutator)gemutator;
   }
  }


  /**
   * Comparator to assist in the ordering of tooltip properties.
   */
  private class MyTooltipComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      GenomicProperty gp1 = (GenomicProperty)o1;
      GenomicProperty gp2 = (GenomicProperty)o2;
      Integer i1 = getPropertyOrderValue(gp1.getName());
      if (i1.intValue()==0) i1 = new Integer(Integer.MAX_VALUE);
      Integer i2 = getPropertyOrderValue(gp2.getName());
      if (i2.intValue()==0) i2 = new Integer(Integer.MAX_VALUE);
      return i1.compareTo(i2);
    }
  }

}