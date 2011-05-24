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
 * Title:        Facade for Browser<p>
 * Description:  Object to keep track of data comprising a feature.<p>
 * Company:       Inc.<p>
 * @author Les Foster
 * @version $Id$
 */
package api.facade.concrete_facade.shared.feature_bean;

import api.entity_model.management.ModelMgr;
import api.entity_model.management.StandardEntityFactory;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.fundtype.*;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.facade.concrete_facade.shared.PropertySource;
import api.facade.concrete_facade.xml.sax_support.ReplacedData;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.GenomicEntityComment;
import api.stub.data.OID;
import api.stub.data.ReplacementRelationship;
import api.stub.geometry.Range;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Holds all data to model a feature, and acts as a base class for
 * other features.  Includes some abstract methods.
 */
public abstract class FeatureBean implements Serializable, Comparable {

  //-------------------------------------CONSTANTS
  private static String BAD_CURATED_TYPE = "Invalid Human Curated span type.  Please refer to the Genomics Exchange Format document for a list of designated Human Curated types.  Type Given Was ";
  private static String BAD_PRECOMPUTED_TYPE = "Invalid Precomputed type.  Please refer to the Genomics Exchange Format document for a list of designated Precomputed types.  Type Given Was ";

  //-------------------------------------STATIC VARIABLES
  private static StandardEntityFactory entityFactory = null;

  //-------------------------------------INSTANCE VARIABLES
  private OID featureOID;
  private OID axisOID;
  private int start;
  private int end;
  private String score;
  private String description;
  private String discoveryEnvironment;
  private String analysisType;
  private int subjectStart;
  private int subjectEnd;
  private List propertySources = new ArrayList(); // List of property sources.
  private List replacedList = new ArrayList();
  private String individualExpectValue;
  private String summaryExpectValue;
  private FacadeManagerBase readFacadeManager;
  private FeatureBean parentModel;
  private boolean curated;
  private GenomicEntityComment[] featureComments;

  private GenomicEntity cachedEntity = null;

  private boolean parentHasBeenSet = false; // Must set parent prior to entity creation.

  //-------------------------------------CONSTRUCTORS
  /**
   * Simple constructor takes the OID of the axis to which this will align.
   */
  public FeatureBean(OID featureOID, OID axisOID, FacadeManagerBase readFacadeManager) {

      this.readFacadeManager = readFacadeManager;
      this.featureOID = featureOID;
      this.axisOID = axisOID;

  } // End constructor.

  //-------------------------------------PUBLIC INTERFACE
  /**
   * Series of setters to populate this "model".
   */
  public void setSubjectStart(int val) { subjectStart = val; }
  public void setSubjectEnd(int val) { subjectEnd = val; }

  public void setStart(String startVal) {
    try {
      start = Integer.parseInt(startVal);
    } catch (NumberFormatException nfe) {
      FacadeManager.handleException(new Exception("Bad numeric start value for span: "+startVal));
    } // End catch block for conversion
  } // End method: setStart

  public void setEnd(String endVal) {
    try {
      end = Integer.parseInt(endVal);
    } catch (NumberFormatException nfe) {
      FacadeManager.handleException(new Exception("Bad numeric end value for span: "+endVal));
    } // End catch block for conversion
  } // End method: getEnd

  public void setStart(int startInt) {
    start = startInt;
  } // End method: setStart

  public void setEnd(int endInt) {
    end = endInt;
  } // End method: setEnd

  public void setCurated(boolean curated) {
    this.curated = curated;
  } // End method: setCurated

  /**
   * Sets up the feature comments applying to this entity.
   */
  public void setComments(GenomicEntityComment[] featureComments) {
    this.featureComments = featureComments;
  } // End method: setComments

  public void setDescription(String description) { this.description = description; }
  public void setScore(String scoreVal) { score = scoreVal; }
  public void setIndividualExpect(String expectVal) { individualExpectValue = expectVal; }
  public void setSummaryExpect(String expectVal) { summaryExpectValue = expectVal; }
  public void setAnalysisType(String analysisType) { this.analysisType = analysisType; }
  public void setDiscoveryEnvironment(String discoveryEnvironment) { this.discoveryEnvironment = discoveryEnvironment; }

  /**
   * Set the parent.  Parent must be set, even if just to null.
   */
  public void setParent(FeatureBean parentModel) {
      this.parentModel = parentModel;
      parentHasBeenSet = true;
  } // End method: setParent

  /**
   * Series of getters to directly pull contents.
   */
  public int getStart() { return start; }
  public int getEnd() { return end; }
  public String getScore() { return score; }
  public OID getAxisOfAlignment() { return axisOID; }
  public OID getOID() { return featureOID; }
  public String getDescription() { return description; }
  public int getSubjectStart() { return subjectStart; }
  public int getSubjectEnd() { return subjectEnd; }
  public String getSummaryExpect() { return summaryExpectValue; }
  public String getIndividualExpect() { return individualExpectValue; }
  public List getReplacedList() { return replacedList; }
  public List getPropertySources() { return propertySources; }
  public String getDiscoveryEnvironment() { return discoveryEnvironment; }
  public String getAnalysisType() { return analysisType; }
  public boolean isCurated() { return curated; }
  public GenomicEntityComment[] getComments() { return featureComments; }

  public abstract OID[] getEvidence();

  /**
   * Adds one list of replaced OIDs to another.  Builds up list of replaced oids.
   */
  public void addReplacedList(List replacedOIDs) {
    this.replacedList.addAll(replacedOIDs);
  } // End method: addReplacedList

  /**
   * Adds an entire group of property sources to the list.
   */
  public void addPropertySources(List propertySources) {
      for (Iterator it = propertySources.iterator(); it.hasNext(); ) {
          addPropertySource((PropertySource)it.next());
      } // For all iterations.
  } // End method: addPropertySources

  /**
   * Convenience method to return the list of OIDs replaced by this feature,
   * as an array.
   */
  public OID[] getReplacedOIDs() {
      List returnList = new ArrayList();
      for (Iterator it = replacedList.iterator(); it.hasNext(); ) {
        returnList.addAll(((ReplacedData)it.next()).getOIDs());
      } // For all iterations.

      OID[] replacedOIDs = new OID[returnList.size()];

      returnList.toArray(replacedOIDs);

      return replacedOIDs;
  } // End method: getReplacedOIDs

  /**
   * Adds a new property source--the means of generating a property, and if
   * needed, expanding it.
   */
  public void addPropertySource(PropertySource propertySource) {
      propertySources.add(propertySource);
  } // End method: addPropertySource

  /**
   * Allow the generation of an alignment, along with its entity, from the
   * contained info.
   */
  public abstract Alignment alignFeature();

  /**
   * Returns the range covered by this feature.
   */
  public abstract Range calculateFeatureRange();

  /** Access the parent. */
  public FeatureBean getParent() { return this.parentModel; }

  public class LocalEntityFactory extends StandardEntityFactory {
    public LocalEntityFactory () {
      super(new Integer(ModelMgr.getModelMgr().hashCode()));
    } // End constructor
  } // End class

  /**
   *  Create a Feature from this feature model.
   */
  public GenomicEntity createFeatureEntity() {

    // Only do this if it has not been done already.
    if (cachedEntity != null)
      return cachedEntity;

    // WARNING: Don't try this at home....
    // This is being built here instead of static initializer, because
    // author has no idea how many resources are spent building this factory,
    // and would like to avoid incurring their use at startup time!
    if (entityFactory == null)
      entityFactory = new LocalEntityFactory();

    // Enforces a restriction which cannot be easily made at instantiation time.
    if (! parentHasBeenSet)
      throw new IllegalStateException("Must set parent prior to creating entity.");

    // Decode the annotation type string into a feature type instance.
    EntityType annotationType = decodeEntityType(getAnalysisType());

    // Must not mix curated features and non-curated tag-sourced features.
    // A special tag hierarchy determines whether the feature was curated or
    // not.  But, it is possible to enter type strings which do not fall within
    // the designated set of curated features (or precomputed features in
    // the oposite case).
    if (isCurated() && (! EntityTypeSet.getEntityTypeSet("CuratedFeatureTypes").contains(annotationType))) {
      FacadeManager.handleException(new IllegalArgumentException(BAD_CURATED_TYPE + getAnalysisType()));
    } // Found precompute under curation.

    if ((! isCurated()) && (! EntityTypeSet.getEntityTypeSet("ComputedFeatureTypes").contains(annotationType))) {
      FacadeManager.handleException(new IllegalArgumentException(BAD_PRECOMPUTED_TYPE + getAnalysisType()));
    } // Found curation under precompute.

    GenomicEntity parentEntity = null;
    if ((getParent() != null) && (! isObsolete()))
      parentEntity = getParent().createFeatureEntity();
    else
      parentEntity = null;

    try { // TEMPORARY
    	
    GenomicEntity featureEntity = entityFactory.create(
        featureOID,                           // Feature's OID
        getAnalysisType(),                    // Display Name
        annotationType,                       // Entity Type
        getDiscoveryEnvironment(),            // Discovery Environment
        null,                                 // subclassification string
        parentEntity,                         // parent genomic entity
        readFacadeManager,                    // May want same facade manager
        FeatureDisplayPriority.DEFAULT_PRIORITY
                                               // All XML sourced are dflt prio
    );

    cachedEntity = featureEntity;

    return featureEntity;

    } catch ( Exception ex ) {
    	if ( parentEntity != null ) {
    		System.out.println( "PARENT: " + parentEntity.getDisplayName() + " " + parentEntity.getEntityType() + " " + parentEntity.toString());
    		System.out.println( "THIS: " + getAnalysisType() + " " + getDiscoveryEnvironment() + " " + annotationType );
    	}
    	return null;
    }
  } // End method: createFeatureEntity

  /**
   * Decode the annotation type string into a feature type instance.
   * NOTE: HSP means "High Scoring Pair".  MSSP means "Maximum Scoring Segment
   * Pair".  These amount to the same thing, according to the users.
   */
  public static EntityType decodeEntityType(String annotationTypeString) {

    EntityType annotationType = null;

    if (annotationTypeString == null)
      return EntityType.getEntityTypeForValue(EntityTypeConstants.Miscellaneous);

    if (annotationTypeString.equalsIgnoreCase("MSSP"))
      annotationTypeString = "HSP";

    // First lookup by possible integer value and discovery environment.
    try {
      if (Character.isDigit(annotationTypeString.charAt(0)))
        annotationType = EntityType.getEntityTypeForValue(Integer.parseInt(annotationTypeString));
      else
        annotationType = EntityType.getEntityTypeForDiscoveryEnvironment(annotationTypeString);
    } catch (IllegalArgumentException iae) {
      // Do nothing.System.out.println("discenv IAE of "+iae.getMessage());
    } // End catch

    // If that fails, try to lookup by name.
    if ((annotationType == null) || (annotationType.value() == EntityTypeConstants.Miscellaneous)) {
      try {
        annotationType = EntityType.getEntityTypeForName(annotationTypeString);
      } catch (IllegalArgumentException iae) {
        // Do nothingSystem.out.println("entityforname IAE of "+iae.getMessage());
      } // End catch
    } // Retry if by name.

    // If name files, try reformatting with underscores, and upper case, and lookup by name again.
    if ((annotationType == null) || (annotationType.value() == EntityTypeConstants.Miscellaneous)) {
      try {
        annotationType = EntityType.getEntityTypeForDiscoveryEnvironment(underscoreUpperCaseFormat(annotationTypeString));
      } catch (IllegalArgumentException iae) {
        // Do nothing System.out.println("disc-env IAE of "+iae.getMessage());
      } // End catch
    } // Retry by name w/alt formatting.

    // If name files, try reformatting with underscores, and upper case, and lookup by name again.
    if ((annotationType == null) || (annotationType.value() == EntityTypeConstants.Miscellaneous)) {
      try {
        annotationType = EntityType.getEntityTypeForName(underscoreUpperCaseFormat(annotationTypeString));
      } catch (IllegalArgumentException iae) {
        // Do nothing. System.out.println("entity for name IAE of "+iae.getMessage());
      } // End catch
    } // Retry by name w/alt formatting.

    // We give up!  We do not know what this is--user pays the price for invalid mapping.
    if (annotationType == null)
      annotationType = EntityType.getEntityTypeForValue(EntityTypeConstants.Miscellaneous);

    return annotationType;
  } // End method: decodeEntityType

  /**
   * Single-point method for creating axis alignments for features.
   */
  protected Alignment createAlignment(  int start, int length,
                                        OID axisOID, int entityStart,
                                        int entityLength,
                                        GenomicEntity feature) {

    if (isCurated() && this.getOID().isScratchOID())
      return new MutableAlignment(null, (AlignableGenomicEntity)feature, calculateFeatureRange());
    else
      return new GeometricAlignment(null, (AlignableGenomicEntity)feature, calculateFeatureRange());

  } // End method: createAlignment

  /**
   * Set up the replacement relationships from replaced data.
   */
  private ReplacementRelationship getReplacementRelationship() {
    String replacementType = null;
    if (replacedList.size() > 0) {
      replacementType = ((ReplacedData)replacedList.get(0)).getType();
    } // Can obtain the replacenment type.
    else {
      replacementType = ReplacementRelationship.TYPE_NEW;
    } // Cannot obtain a replacement type.
    return new ReplacementRelationship(replacementType, getReplacedOIDs());
  } // End method: getReplacementRelationship

  /**
   * Debugging dump.
   */
  public String toString() {
    Alignment al = this.alignFeature();
    StringBuffer returnVal = new StringBuffer(400);
    returnVal.append(this.getClass().getName());
    returnVal.append(" "+this.analysisType);
    returnVal.append(" [OID OF ALIGNMENT]"+this.axisOID);
    returnVal.append(" "+this.getDescription());
    returnVal.append(" "+al.toString());

    return returnVal.toString();
  } // End method: toString

  /** Checks replaced info to see if THIS object is obsolete. */
  public boolean isObsolete() {
    // Quick heuristic: only curated features may be obsolete.
    if (! isCurated())
      return false;

    boolean returnFlag = false;
    List replacedList = getReplacedList();
    if (replacedList != null) {
      ReplacedData nextData = null;
      for (Iterator it = replacedList.iterator(); (! returnFlag) && it.hasNext(); ) {
        nextData = (ReplacedData)it.next();
        if (nextData.getType().equalsIgnoreCase("obsolete"))
          returnFlag = true;
      } // For all replaced datas.
    } // Got replaced datas.
    return returnFlag;
  } // End method

  //-------------------------------------IMPLEMENTATION OF Comparable
  /** Compares with another object. */
  public int compareTo(Object other) {

    // Set less than if wrong type.
    if (! (other instanceof FeatureBean))
      return -1;

    FeatureBean otherModel = (FeatureBean)other;

    int retval = 0;

    // ASSUMPTIONS: here, we assume that if the other model has the
    // opposite orientation, we judge start/end based on the orientation
    // of THIS model, and do not bother to check.  Further, at time of
    // writing, it is assumed that this method will only ever be used
    // to compare models that are children of the same parent.  [So opp
    // orientation should not happen anyway]
    if (getStart() < getEnd()) {
      retval = this.getStart() - otherModel.getStart();
    } // Forward orientation
    else {
      retval = otherModel.getStart() - this.getStart();
    } // Reverse orientation

    return retval;

  } // End method

  //-------------------------------------HELPERS
  /** Formats strings of type "Word1 Word2 Word3..." to _WORD1_WORD2_WORD3... */
  private static String underscoreUpperCaseFormat(String inString) {
    return "_"+inString.toUpperCase().replace(' ', '_');
  } // End method

} // End class: FeatureModel
