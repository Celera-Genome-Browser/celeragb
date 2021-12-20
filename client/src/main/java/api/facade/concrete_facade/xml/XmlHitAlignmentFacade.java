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
import api.entity_model.model.fundtype.EntityType;
import api.facade.abstract_facade.annotations.HSPFacade;
import api.facade.abstract_facade.annotations.HitAlignmentFacade;
import api.stub.data.ControlledVocabUtil;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;
import api.stub.data.SubjectDefinition;
import api.stub.sequence.Sequence;

import java.util.*;

public class XmlHitAlignmentFacade extends XmlFeatureFacade  implements HitAlignmentFacade {

  //-----------------------------------------------INTERFACE METHODS
  /**
   * Called when expansion takes place for 'num subj defns' (subject definitions).
   */
  public SubjectDefinition[] getSubjectDefinitions(OID featureOid) {
    XmlLoader featureLoader = null;
    String description = null;
    SubjectDefinition definition = null;
    List accumulator = new ArrayList();
    for (Iterator it = getXmlLoadersForFeature(featureOid); it.hasNext(); ) {
      featureLoader = (XmlLoader)it.next();
      if (null != (description = featureLoader.getFeatureDescription(featureOid))) {
        definition = makeSubjectDefinition(description);
        if (definition != null)
          accumulator.add(definition);
      } // Found a descript.
    } // For all loaders

    SubjectDefinition[] returnArray = null;
    if (accumulator.size() > 0) {
      returnArray = new SubjectDefinition[accumulator.size()];
      accumulator.toArray(returnArray);
    } // Got defs.
    else
      returnArray = new SubjectDefinition[0];

    return returnArray;

  } // End method: getSubjectDefinitions

  /**
   * Pull in subject sequence for subject sequence id associated
   * with given feature.
   */
  public Sequence getSubjectSequence(OID featureOid, EntityType entityType) {
    Sequence returnSequence = null;
    // NOTE: ignoring the entity type for now.
    if (featureOid != null) {

      // First find all subject seq oids from all loaders known to this facade.
      Set allSubjectSeqOids = new HashSet();
      for (Iterator it = getXmlLoadersForFeature(featureOid); it.hasNext(); ) {
        XmlLoader nextLoader = (XmlLoader)it.next();
        allSubjectSeqOids.addAll(nextLoader.getSubjectSequenceOids(featureOid));
      } // For all loaders.

      // Now iterate over all those loaders, and iterate over all those
      // subject sequence OIDs, looking for the subject sequence.  Stop on
      // the FIRST subject sequence found.
      for (Iterator it = getXmlLoadersForFeature(featureOid); returnSequence == null && it.hasNext(); ) {
        XmlLoader nextLoader = (XmlLoader)it.next();
        for (Iterator subjIt = allSubjectSeqOids.iterator(); returnSequence == null && subjIt.hasNext(); ) {
          OID subjectSequenceOid = (OID)subjIt.next();
          returnSequence = nextLoader.getSubjectSequence(subjectSequenceOid);
        } // For all subject sequence OIDs found.
      } // For all loaders.

    } // Non-null args.

    return returnSequence;

  } // End method: getSubjectSequence

  //-----------------------------------------------HELPER METHODS
  protected Map inheritProperties(OID featureOID) {
    Map returnMap = super.inheritProperties(featureOID);

    GenomicProperty summaryEValProp = null;
    GenomicProperty numSubjDefnsProp = null;

    // NOTE: if the subject seq id was manually coded as a prop, do not get
    // it from anywhere else.
    GenomicProperty subjectSequenceOidProp = (GenomicProperty)returnMap.get(HitAlignmentFacade.SUBJECT_SEQ_ID_PROP);

    // And the accession.
    GenomicProperty accessionProp = (GenomicProperty)returnMap.get(HitAlignmentFacade.ACCESSSION_NUM_PROP);

    boolean subjectDefsComplete = false;

    XmlLoader featureLoader = null;
    Iterator it = getXmlLoadersForFeature(featureOID);

    if (! it.hasNext())
      return returnMap;

    String description = null;
    List subjectDefinitions = new ArrayList();
    SubjectDefinition nextDefinition = null;
    String summaryExpectString = null;
    OID subjectSeqOid = null;
    Set subjectSeqOids = null;
    AlignableGenomicEntity entity = null;
    Alignment featureAlignment = null;

    while (it.hasNext()) {
      featureLoader = (XmlLoader)it.next();

      // NOTE: caution should be taken not to use this technique for curated
      // or other features where the adding of properties is done prior to
      // the entity factory's returning of its entity!  Otherwise, could
      // get into an endless loop.
      if (null == (featureAlignment = featureLoader.getAlignmentForFeature(featureOID)))
        continue;
      if (null == (entity = featureAlignment.getEntity()))
        continue;

      description = featureLoader.getFeatureDescription(featureOID);
      if (description != null) {

        if (! subjectDefsComplete) {
          nextDefinition = makeSubjectDefinition(description);
          if (nextDefinition != null) {
            subjectDefinitions.add(nextDefinition);
            subjectDefsComplete = true;
          } // Got a definition.
        } // No subject defs yet.

        // Deal with accession prop too.
        if (accessionProp == null) {
          accessionProp = createAccProp(description, returnMap);
        } // Not seen yet.

      } // Have description.

      summaryExpectString = featureLoader.getSummaryExpect(featureOID);
      if (summaryExpectString != null && summaryEValProp == null) {
        summaryEValProp = createWithPropsMgr( HSPFacade.SUM_E_VAL_PROP,
                                              entity,
                                              featureLoader.getSummaryExpect(featureOID));
        returnMap.put(summaryEValProp.getName(), summaryEValProp);
      } // Found a summary expect.

      subjectSeqOids = featureLoader.getSubjectSequenceOids(featureOID);
      if (subjectSeqOids != null) {
        for (Iterator subSeqIt = subjectSeqOids.iterator(); (subjectSequenceOidProp == null) && subSeqIt.hasNext(); ) {
          subjectSeqOid = (OID)subSeqIt.next();
          if (subjectSeqOid != null) {
            subjectSequenceOidProp = createDefaultSettingsProperty(HitAlignmentFacade.SUBJECT_SEQ_ID_PROP, subjectSeqOid.toString());
            returnMap.put(subjectSequenceOidProp.getName(), subjectSequenceOidProp);

            // Get property if not already set.
            String subjectSeqLen = null;
            GenomicProperty subjectSeqLenProp = (GenomicProperty)returnMap.get(HitAlignmentFacade.SUBJECT_SEQ_LENGTH_PROP);
            if ((subjectSeqLenProp == null) && (null != (subjectSeqLen = featureLoader.getSequenceLength(subjectSeqOid)))) {
                subjectSeqLenProp = createDefaultSettingsProperty(HitAlignmentFacade.SUBJECT_SEQ_LENGTH_PROP, subjectSeqLen);
                returnMap.put(HitAlignmentFacade.SUBJECT_SEQ_LENGTH_PROP, subjectSeqLenProp);
            } // Need prop, have value for prop.

          } // Got oid.
        } // For all iterations, till break.
      } // Found set of subj seq oids.

    } // For all loaders for feature.

    // Building a number of subject definitions, and adding all such defs
    // as child properties.
    SubjectDefinition[] definitionsArray = new SubjectDefinition[subjectDefinitions.size()];
    if (subjectDefinitions.size() > 0)
      subjectDefinitions.toArray(definitionsArray);

    if (subjectDefinitions.size() > 0) {
      numSubjDefnsProp =
        new GenomicProperty(
          HitAlignmentFacade.NUM_SUBJ_DEFNS_PROP,
          "client.gui.other.dialogs.AlignmentSubjectDefinition",
          new Integer(definitionsArray.length).toString(),
          true,    // Editable.
          ControlledVocabUtil.getNullVocabIndex()
        );

    } // Full setup.
    else {
      numSubjDefnsProp = createDefaultSettingsProperty(HitAlignmentFacade.NUM_SUBJ_DEFNS_PROP, "0");
    } // More limited property.

    returnMap.put(HitAlignmentFacade.NUM_SUBJ_DEFNS_PROP, numSubjDefnsProp);

    return returnMap;

  } // End method: inheritMap

} // End class: XmlHitAlignmentFacade
