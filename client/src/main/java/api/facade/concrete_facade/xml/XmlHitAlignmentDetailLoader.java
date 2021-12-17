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
import api.facade.abstract_facade.annotations.HSPFacade;
import api.facade.abstract_facade.annotations.HitAlignmentDetailLoader;
import api.stub.data.*;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class XmlHitAlignmentDetailLoader extends XmlFeatureFacade
  implements HitAlignmentDetailLoader {

  //------------------------------------INTERFACE METHODS
  /**
   * Returns residues of the subject alignment.  Not to be confused with the
   * sequence.
   */
  public String getSubjectAlignedResidues(OID id) throws NoData {
    //    System.err.println (this.getClass().getName()+" got request for subject sequence");
    XmlLoader featureLoader = null;
    Iterator it = getXmlLoadersForFeature(id);
    if (it.hasNext())
      featureLoader = (XmlLoader)it.next();
    else
      throw new NoData();
    return featureLoader.getSubjectAlignedResidues(id);
  } // End method: getSubjectAlignedResidues

  /**
   * Returns residues of the query alignment.  Not to be confused with the
   * sequence.
   */
  public String getQueryAlignedResidues(OID id) throws NoData {
    //    System.err.println (this.getClass().getName()+" got request for subject sequence");
    XmlLoader featureLoader = null;
    Iterator it = getXmlLoadersForFeature(id);
    if (it.hasNext())
      featureLoader = (XmlLoader)it.next();
    else
      throw new NoData();
    return featureLoader.getQueryAlignedResidues(id);
  } // End method: getQueryAlignemdResidues

  /**
   * Called when expansion takes place for 'num subj defns' (subject definitions).
   */
  public SubjectDefinition[] getSubjectDefinitions(OID featureOid) throws NoData {
    XmlLoader featureLoader = null;
    String description = null;
    SubjectDefinition definition = null;
    List accumulator = new ArrayList();
    for (Iterator it = getXmlLoadersForFeature(featureOid); it.hasNext(); ) {
      featureLoader = (XmlLoader)it.next();
      // NOTE: current defintion implies that the parent and child come from the same loader.
      //
      if (null != (description = featureLoader.getFeatureDescriptionForParent(featureOid))) {
        definition = makeSubjectDefinition(description);
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

  //------------------------------------HELPER METHODS
  /** Returns props which have been deduped by name. */
  protected Map inheritProperties(OID featureOID) {
    Map returnProperties = super.inheritProperties(featureOID);

    GenomicProperty assemblyVersionProp = null;
    GenomicProperty individualEValueProp = null;
    GenomicProperty subjectLeftProp = null;
    GenomicProperty subjectRightProp = null;
    GenomicProperty alignmentLengthProp = null;
    GenomicProperty subjectSequenceLengthProp = null;
    GenomicProperty bitScoreProp = null;
    GenomicProperty numGapsProp = null;
    GenomicProperty numIdenticalProp = null;
    GenomicProperty numSimOrPosProp = null;
    GenomicProperty queryFrameProp = null;
    GenomicProperty descriptionProp = null;
    GenomicProperty numSubjDefnsProp = null;
    GenomicProperty accessionProp = (GenomicProperty)returnProperties.get(HitAlignmentDetailLoader.ACCESSSION_NUM_PROP);

    List subjectDefinitions = new ArrayList();

    String assemblyVersion = getAssemblyVersion(featureOID);
    XmlLoader featureLoader = null;
    Iterator featureLoaders = getXmlLoadersForFeature(featureOID);
    AlignableGenomicEntity entity = null;
    Alignment featureAlignment = null;
    String alignmentLengthString = null;
    String subjectSequenceLengthString = null;
    String description = null;
    SubjectDefinition nextDefinition = null;

    // Obtain first value from any loader which delivers one, for each property named.
    if (! featureLoaders.hasNext())
      return returnProperties;

    while (featureLoaders.hasNext()) {
      featureLoader = (XmlLoader)featureLoaders.next();
      if (null == (featureAlignment = featureLoader.getAlignmentForFeature(featureOID)))
        continue;
      if (null == (entity = featureAlignment.getEntity()))
        continue;

      if (assemblyVersionProp == null) {
        assemblyVersionProp = createWithPropsMgr( HSPFacade.ASSEMBLY_VERSION_PROP,
                                                  entity,
                                                  assemblyVersion );
      } // Must check.

      if (individualEValueProp == null) {
        if (featureLoader.getIndividualExpect(featureOID) != null)
          individualEValueProp = createWithPropsMgr(HSPFacade.INDIVIDUAL_E_VAL_PROP,
                                                    entity,
                                                    featureLoader.getIndividualExpect(featureOID));
      } // Must check.

      if (subjectLeftProp == null) {
        subjectLeftProp = createWithPropsMgr( HSPFacade.SUBJECT_LEFT_PROP,
                                              entity,
                                              Integer.toString(featureLoader.getSubjectStart(featureOID)));
      } // Must check.

      if (subjectRightProp == null) {
        subjectRightProp = createWithPropsMgr(HSPFacade.SUBJECT_RIGHT_PROP,
                                              entity,
                                              Integer.toString(featureLoader.getSubjectEnd(featureOID)));
      } // Must check.

      if (alignmentLengthProp == null) {
        alignmentLengthString = getAlignmentLength(featureLoader, featureOID);
        if (alignmentLengthString != null)
          alignmentLengthProp = createWithPropsMgr( HSPFacade.ALIGNMENT_LENGTH_PROP,
                                                    entity,
                                                    alignmentLengthString);
      } // Must check.

      if (bitScoreProp == null) {
        if (featureLoader.getScore(featureOID) != null)
          bitScoreProp = createWithPropsMgr(HSPFacade.BIT_SCORE_PROP,
                                            entity,
                                            featureLoader.getScore(featureOID));
      } // Must check.

      if (numGapsProp == null) {
        if (featureLoader.getOutput(featureOID, "gaps") != null)
          numGapsProp = createWithPropsMgr( HSPFacade.NUM_GAPS_PROP,
                                            entity,
                                            featureLoader.getOutput(featureOID, "gaps"));
      } // Must check.

      if (numIdenticalProp == null) {
        if (featureLoader.getOutput(featureOID, "identical") != null)
          numIdenticalProp = createWithPropsMgr(  HSPFacade.NUM_IDENTICAL_PROP,
                                                  entity,
                                                  featureLoader.getOutput(featureOID, "identical"));
      } // Must check.

      if (numSimOrPosProp == null) {
        if (featureLoader.getOutput(featureOID, "positive") != null)
          numSimOrPosProp = createWithPropsMgr( HSPFacade.NUM_SIM_OR_POS_PROP,
                                                entity,
                                                featureLoader.getOutput(featureOID, "positive"));
      } // Must check.

      if (queryFrameProp == null) {
        if (featureLoader.getOutput(featureOID, "frame") != null)
          queryFrameProp = createWithPropsMgr(  HSPFacade.QUERY_FRAME_PROP,
                                                entity,
                                                featureLoader.getOutput(featureOID, "frame"));
      } // Must check.

      if (descriptionProp == null) {
        description = featureLoader.getFeatureDescriptionForParent(featureOID);
        if (description != null) {
          descriptionProp = new GenomicProperty(HitAlignmentDetailLoader.DESCRIPTION_PROP, getNullEditingClassString(),
                            description, false, ControlledVocabUtil.getNullVocabIndex());
          nextDefinition = makeSubjectDefinition(description);
          if (nextDefinition != null)
            subjectDefinitions.add(nextDefinition);
        } // Have description.

      } // Must check.

      if (accessionProp == null) {
        description = featureLoader.getFeatureDescriptionForParent(featureOID);
        if (description != null) {
          accessionProp = createAccProp(description, returnProperties);
        } // Have description.

      } // Must check.

      if (subjectSequenceLengthProp == null) {
        subjectSequenceLengthString = getSubjectSequenceLength(featureLoader, featureOID);
        if (subjectSequenceLengthString != null)
          subjectSequenceLengthProp = createDefaultSettingsProperty(    HitAlignmentDetailLoader.SUBJECT_SEQ_LENGTH_PROP,
                                                                        subjectSequenceLengthString);
      } // Must check.

    } // Found in this facade's loaders.

    // Catch all the "found" properties in a list.
    if (assemblyVersionProp != null)
      returnProperties.put(assemblyVersionProp.getName(), assemblyVersionProp);
    if (individualEValueProp != null)
      returnProperties.put(individualEValueProp.getName(), individualEValueProp);
    if (subjectLeftProp != null)
      returnProperties.put(subjectLeftProp.getName(), subjectLeftProp);
    if (subjectRightProp != null)
      returnProperties.put(subjectRightProp.getName(), subjectRightProp);
    if (alignmentLengthProp != null)
      returnProperties.put(alignmentLengthProp.getName(), alignmentLengthProp);
    if (subjectSequenceLengthProp != null)
      returnProperties.put(subjectSequenceLengthProp.getName(), subjectSequenceLengthProp);
    if (bitScoreProp != null)
      returnProperties.put(bitScoreProp.getName(), bitScoreProp);
    if (numGapsProp != null)
      returnProperties.put(numGapsProp.getName(), numGapsProp);
    if (numIdenticalProp != null)
      returnProperties.put(numIdenticalProp.getName(), numIdenticalProp);
    if (numSimOrPosProp != null)
      returnProperties.put(numSimOrPosProp.getName(), numSimOrPosProp);
    if (queryFrameProp != null)
      returnProperties.put(queryFrameProp.getName(), queryFrameProp);

    // Building a number of subject definitions, and adding all such defs
    // as child properties.
    SubjectDefinition[] definitionsArray = new SubjectDefinition[subjectDefinitions.size()];
    if (subjectDefinitions.size() > 0)
      subjectDefinitions.toArray(definitionsArray);

    if (subjectDefinitions.size() > 0) {
      numSubjDefnsProp =
        new GenomicProperty(
          HitAlignmentDetailLoader.NUM_SUBJ_DEFNS_PROP,
          "client.gui.other.dialogs.AlignmentSubjectDefinition",
          new Integer(definitionsArray.length).toString(),
          true,    // Editable.
          ControlledVocabUtil.getNullVocabIndex()
        );
    } // Full setup.
    else {
      numSubjDefnsProp = this.createDefaultSettingsProperty(HitAlignmentDetailLoader.NUM_SUBJ_DEFNS_PROP, "0");
    } // More limited property.
    returnProperties.put(numSubjDefnsProp.getName(), numSubjDefnsProp);

    GenomicProperty percentIdentityProp = null;
    percentIdentityProp = calcPercentIdentityFromExisting(  (GenomicProperty)returnProperties.get(HSPFacade.NUM_IDENTICAL_PROP),
                                                            (GenomicProperty)returnProperties.get(HSPFacade.ALIGNMENT_LENGTH_PROP));
    if (percentIdentityProp != null) {
      returnProperties.put(percentIdentityProp.getName(), percentIdentityProp);
    } // Got a null.

    return returnProperties;
  } // End method: inheritProperties

  /**
   * Returns alignment length property value, which it calculates from values
   * retreived from the loader given for the feature whose OID was given.
   */
  private String getAlignmentLength(XmlLoader featureLoader, OID featureOID) {
    String queryAlignmentString = featureLoader.getQueryAlignedResidues(featureOID);
    String subjectAlignmentString = featureLoader.getSubjectAlignedResidues(featureOID);

    if ((subjectAlignmentString != null) && (subjectAlignmentString.length() > 0)) {
      return new Integer(subjectAlignmentString.length()).toString();
    } // Got alignment for subject.
    else if ((queryAlignmentString != null) && (queryAlignmentString.length() > 0)) {
      return new Integer(queryAlignmentString.length()).toString();
    } // Got alignment for query.
    else {
      return null;
    } // Neither one found.

  } // End method: getAlignmentLength

  /** This helper will build one property by looking at two other properties. */
  private GenomicProperty calcPercentIdentityFromExisting(  GenomicProperty numIdenticalProp,
                                                            GenomicProperty alignmentLengthProp) {

    if ((alignmentLengthProp == null) || (numIdenticalProp == null))
      return null;

    try {
      // A number of things can throw an exception here:
      //  overflow if zero denom., etc...
      //  catch any of them and return empty prop.
      double alignmentLength = Double.parseDouble(alignmentLengthProp.getInitialValue());
      double numIdentical = Double.parseDouble(numIdenticalProp.getInitialValue());
      NumberFormat format = NumberFormat.getInstance();
      format.setMaximumFractionDigits(2);
      return createDefaultSettingsProperty( HSPFacade.PERCENT_IDENTITY_PROP,
                                            format.format(numIdentical / alignmentLength * 100.0));
    } catch (Exception ex) {
      return createDefaultSettingsProperty( HSPFacade.PERCENT_IDENTITY_PROP,
                                            "");
    } // End catch block.

  } // End method: calcPercentIdentityFromExisting

} // End class: XmlHitAlignmentFacade
