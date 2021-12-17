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

package api.facade.concrete_facade.xml;

import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.fundtype.AlignableGenomicEntity;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.facade.abstract_facade.annotations.HitAlignmentFacade;
import api.facade.concrete_facade.shared.feature_bean.FeatureBean;
import api.facade.concrete_facade.xml.sax_support.ReplacedData;
import api.stub.data.*;
import api.stub.geometry.Range;
import shared.util.GANumericConverter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * API Facade returned by the facade manager, to represent a feature.
 */
public class XmlFeatureFacade extends XmlGenomicFacade
  implements FeatureFacade {

  private static String NULL_EDITING_CLASS_STRING = "";
  private static boolean DEBUG_CLASS = false;
  private static final String SCORE_PROPERTY_NAME = "score";
  // NOTE: eliminate this in favor of to-be-defined ENTITY_LENGTH_PROP on feature facade abstract.
  private static final String ENTITY_LENGTH_PROP = "entity_length";

  /**
   * Returns the list of OIDS of evidence for this feature.
   */
  public OID[] retrieveEvidence(OID featureOID) throws NoData {
    XmlLoader loader = null;

    // NOTE: may wish to change getevidence loader interface to return list!
    //
    OID[] tempArray = null;
    List oidList = new ArrayList();
    for (Iterator it = getXmlLoadersForFeature(featureOID); it.hasNext(); ) {
      loader = (XmlLoader)it.next();
      tempArray = loader.getEvidence(featureOID);
      for (int i = 0; i < tempArray.length; i++)
        oidList.add(tempArray[i]);
    } // For all loaders with info on feature.
    tempArray = new OID[oidList.size()];
    oidList.toArray(tempArray);

    return tempArray;

  } // End method: retrieveEvidence

  /**
   * Returns the set of comments associated with the gene whose oid
   * is given.  If a loader for the comments is found, return its
   * associated comments.  If none is found, return an empty set.
   */
  public GenomicEntityComment[] getComments (OID featureOID) throws NoData {
      List commentsList = new ArrayList();
      GenomicEntityComment[] tempArray;

      //NOTE: should probably change the getComments interface to return collection.
      for (Iterator it = getXmlLoadersForFeature(featureOID); it.hasNext(); ) {
          tempArray = ((XmlLoader)it.next()).getComments(featureOID);
          for (int i = 0; i < tempArray.length; i++)
              commentsList.add(tempArray[i]);
      } // For all loaders

      GenomicEntityComment[] returnArray = new GenomicEntityComment[commentsList.size()];
      commentsList.toArray(returnArray);

      return returnArray;
  } // End method: getComments

  /**
   * Hand back properties in a way that can be called from subclasses,
   * and which is more flexible than the interface methods "array" returns.
   */
  protected Map inheritProperties(OID featureOID) {
    Map returnMap = super.inheritProperties(featureOID);

    XmlLoader featureLoader = null;
    String description = null;
    String scoreStr = null;
    String featureGroup = null;
    Alignment featureAlignment = null;
    String assemblyVersion = null;
    Range featureRange = null;
    String analysisType = null;
    OID axisOidOfAlignment = null;
    AlignableGenomicEntity entity = null;

    // Multiple loaders may exist per feature.  This loop
    // will try and take the first instance of any given property,
    // by name, and will discard any additional values.  In this
    // way, it maximizes chances of getting data from _somewhere_.
    for (Iterator it = getXmlLoadersForFeature(featureOID); it.hasNext(); ) {
      featureLoader = (XmlLoader)it.next();

      /** @Todo fix this dirty little hack (gasp!) to allow use of props mgr, but preclude its use with human curations. */
      if ((! featureLoader.isCurated(featureOID)) && (null != (featureAlignment = featureLoader.getAlignmentForFeature(featureOID))))
        entity = featureAlignment.getEntity();
      else
        continue;

      if (featureGroup == null) {
        featureGroup = featureLoader.getDiscoveryEnvironmentOfFeature(featureOID);
        if (featureGroup != null) {
          returnMap.put(GROUP_TAG_PROP, createWithPropsMgr(GROUP_TAG_PROP, entity, featureGroup));
        } // Have feature group.
      } // No feature group pref.

      if (description == null) {
        description = featureLoader.getFeatureDescription(featureOID);
        if (description != null) {
          // NOTE: this hack to support Mark Yandell's request that description properties
          // be collected and displayed wherever it is found, makes a dependency downward
          // between this facade and the abstract HitAlignmentFacade.  Later, a better
          // solution should be forthcoming. LLF.
          returnMap.put(HitAlignmentFacade.DESCRIPTION_PROP,
            createWithPropsMgr(  HitAlignmentFacade.DESCRIPTION_PROP,
            entity, description));
        } // Have description.

      } // No description prev.

      if (scoreStr == null) {
        // Add a score property.
        scoreStr = featureLoader.getScore(featureOID);
        if (scoreStr != null) {
          returnMap.put(SCORE_PROPERTY_NAME, createWithPropsMgr(SCORE_PROPERTY_NAME, entity, scoreStr));
        } // Got a score.

      } // No score prev.

      // Add other props generic to any feature which has an alignment.
      if (featureRange == null) {
        featureRange = featureLoader.getRangeOnAxisOfFeature(featureOID);
        if (featureRange != null)
          addPropertyListToMap(generatePropertiesForRange(featureRange), returnMap);
      } // No feature range prev.

      if (axisOidOfAlignment == null) {
        axisOidOfAlignment = featureLoader.getAxisOidOfAlignment(featureOID);
        if (axisOidOfAlignment != null)
          addPropertyListToMap(generatePropertiesForAxisOidOfAlignment(axisOidOfAlignment), returnMap);
      } // No axis oid prev.

      if (analysisType == null) {
        analysisType = featureLoader.getAnalysisTypeOfFeature(featureOID);
        if (analysisType != null)
          returnMap.put(FeatureFacade.FEATURE_TYPE_PROP,
            createWithPropsMgr( FeatureFacade.FEATURE_TYPE_PROP,
                                entity,
                                FeatureBean.decodeEntityType(analysisType).getEntityName()));

      } // No analysis type prev.

      // Add a property for the source from which the feature originated.
      if (assemblyVersion == null) {
        assemblyVersion = getAssemblyVersion(featureOID);
        if (assemblyVersion != null) {
          GenomicProperty assemblyVersionProp = createWithPropsMgr( FeatureFacade.REL_ASSEMBLY_VERSION_PROP,
                                                                    entity,
                                                                    assemblyVersion);
          if (assemblyVersionProp != null)
            returnMap.put(FeatureFacade.REL_ASSEMBLY_VERSION_PROP, assemblyVersionProp);
        } // Now have one.
      } // No assemblyVersion prev.

    } // For all loaders.

    // The comment getter call does all its own loader iterating.
    // Therefore it will be a single call.
    try {
      GenomicEntityComment[] featureComments = getComments(featureOID);
      if ((featureComments != null) && (featureComments.length > 0)) {

        // Make a comment count property.
        returnMap.put(FeatureFacade.NUM_COMMENTS_PROP,
          new GenomicProperty(  FeatureFacade.NUM_COMMENTS_PROP,  "client.gui.other.dialogs.CommentsViewer",
                                new Integer(featureComments.length).toString(),
                                true,     // Editable.
                                ControlledVocabUtil.getNullVocabIndex()));
      } // Something needs to be done.
    } // End try block.
    catch (NoData nd) {
    } // Do noting--add no props.

    return returnMap;

  } // End method: inheritProperties

  /**
   * Given a range, certain properties can be made for the feature
   * to which it applies.  This method builds all of them.
   */
  protected List generatePropertiesForRange(Range featureRange) {
    List returnList = new ArrayList();
    returnList.add(createDefaultSettingsProperty(FeatureFacade.AXIS_BEGIN_PROP,
      new Integer(featureRange.getStart()).toString()));

    returnList.add(createDefaultSettingsProperty(FeatureFacade.AXIS_END_PROP,
      new Integer(featureRange.getEnd()).toString()));

    returnList.add(createDefaultSettingsProperty(FeatureFacade.ENTITY_ORIENTATION_PROP,
      featureRange.getOrientation() == featureRange.FORWARD_ORIENTATION ? "forward" : "reverse"));

    // Always high priority.
    returnList.add(createDefaultSettingsProperty(FeatureFacade.DISPLAY_PRIORITY_PROP, "high"));

    String magnitude = new Integer(Math.abs(featureRange.getMagnitude())).toString();
    returnList.add(createDefaultSettingsProperty(ENTITY_LENGTH_PROP, magnitude));

    return returnList;
  } // End method

  /**
   * Return properties based on oid of alignment.
   */
  protected List generatePropertiesForAxisOidOfAlignment(OID axisOid) {
    List returnList = new ArrayList();

    returnList.add(createDefaultSettingsProperty(FeatureFacade.GENOMIC_AXIS_ID_PROP,
      axisOid.toString()));

    // Internal axis OIDs have a GA_NAME that is preferred on display.  Externals should use just the OID number.
    if (axisOid.isInternalDatabaseOID()) {
      returnList.add(createDefaultSettingsProperty(AXIS_NAME_PROP,
        GANumericConverter.getConverter().getGANameForOIDSuffix(axisOid.getIdentifierAsString())));
    } // Internal.

    return returnList;
  } // End method: generatePropertiesForAlignment
  private static final String AXIS_NAME_PROP = "axis_name";

  /**
   * Retrieve the replacement relationships...
   */
  public ReplacementRelationship retrieveReplacedFeatures
    (OID featureOID, long assemblyVersionOfReplacedFeatures)
    throws NoData
  {
    // Diagnostic...
    if (DEBUG_CLASS) System.out.println("In XmlFeatureFacade.retrieveReplacedFeatures(OID="
                                          +featureOID+",long="+assemblyVersionOfReplacedFeatures+");");
    // Set up an initial relationship...
    ReplacementRelationship replacementRel = new ReplacementRelationship(ReplacementRelationship.TYPE_NEW);

    List listOfReplacedDatas = new ArrayList();
    boolean hadAnyLoaders = false;
    for (Iterator it = getXmlLoadersForFeature(featureOID); it.hasNext(); ) {
      listOfReplacedDatas.addAll(getReplacedOIDsForLoader((XmlLoader)it.next(), featureOID));
      hadAnyLoaders = true;
    } // Go through all loaders.
    if (!hadAnyLoaders) {
      throw new NoData();
    }

    // Now build the ReplacementRelationship...
    // If there are more than one replacedDatas, use the replacement type of the first...
    ReplacedData aReplaceData = null;
    boolean setTypeOfFirst = true;
    for (Iterator itr = listOfReplacedDatas.iterator(); itr.hasNext(); ) {
      aReplaceData = (ReplacedData)itr.next();
      if (DEBUG_CLASS) {
        System.out.println("ReplacementRelationship[] type=("
                           +aReplaceData.getType()+") Number of OIDs=("+aReplaceData.getOIDs().size()+").");
        for (Iterator it = aReplaceData.getOIDs().iterator(); it.hasNext(); ) {
          System.out.println("   Next OID="+(OID)it.next());
        } // For all iterations
      } // End DEBUG

      if (setTypeOfFirst) {
        replacementRel.setNewReplacementType(aReplaceData.getType());
        setTypeOfFirst = false;
      }
      for (Iterator it = aReplaceData.getOIDs().iterator(); it.hasNext(); ) {
        replacementRel.addReplacementOID((OID)it.next());
      } // For all oids in list.
    } // FOr all replaced datas.

    return replacementRel;

  } // End method: retrieveReplacedFeatures

  /** Returns order among siblings. */
  protected GenomicProperty getOrderNumProperty(Alignment alignment) {
    // Proposed solution: get alignment from loader.
    // Then get entity from alignment, and parent from entity.
    // Then query parent for children, to see which position the
    // entity is in.

    // NOTE: may have to do something with ordering the inputs, to ensure
    // that we arfe not presenting the load order, rather than the axis
    // alignment order.
    return null;
  } // End method

  /** Given a specific loader with known target data, grab its replaceed OIDs. */
  private List getReplacedOIDsForLoader(XmlLoader loader, OID featureOID) {
    // Get the list of ReplacedData from the loader...
    List listOfReplacedDatas = loader.getReplacedData(featureOID);
    if (DEBUG_CLASS) System.out.println("Have " + listOfReplacedDatas.size()
                                        + " items in the listOfReplacedDatas for OID:" + featureOID);
    return listOfReplacedDatas;

  } // End method: getReplacedDatasForLoader

  public Alignment[] getAlignmentsToAxes(OID entityOID)
  {
    throw new UnsupportedOperationException("getAlignmentsToAxes(OID)");
  }

} // End class: XmlFeatureFacade
