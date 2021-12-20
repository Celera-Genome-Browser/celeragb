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
 * Description:  Object to keep track of data comprising a simple feature.<p>
 * Company:       Inc.<p>
 * @author Les Foster
 * @version
 */
package api.facade.concrete_facade.shared.feature_bean;

import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.fundtype.GenomicEntity;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.OID;
import api.stub.geometry.Range;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Holds all data to model a simple feature, and can create its alignment
 * and its entity.
 */
public class SimpleFeatureBean extends FeatureBean implements Serializable {

  //-------------------------------------INSTANCE VARIABLES
  private byte[] subjectAlignment;
  private byte[] queryAlignment;
  private OID subjectSequenceOid;
  private List evidenceList = new ArrayList();      // Should contain OIDs.
  private Map outputMap = null;

  //-------------------------------------CONSTRUCTORS
  /**
   * Simple constructor takes the OID of the axis to which this will align.
   */
  public SimpleFeatureBean(OID simpleFeatureOID, OID axisOID,
      FacadeManagerBase readFacadeManager) {

      super(simpleFeatureOID, axisOID, readFacadeManager);

  } // End constructor.

  //-------------------------------------PUBLIC INTERFACE
  /**
   * Series of setters to populate this "model".
   */
  public void setQueryAlignment(String alignmentVal) { queryAlignment = alignmentVal == null ? null : alignmentVal.getBytes(); }
  public void setSubjectSequenceOid(OID sequenceOid) { subjectSequenceOid = sequenceOid; }
  public void setSubjectAlignment(String alignmentVal) { subjectAlignment = alignmentVal == null ? null : alignmentVal.getBytes(); }
  public void setOutputMap(Map outputMap) { this.outputMap = outputMap; }

  /**
   * Set the parent.  Parent must be set, but simple features must have
   * a non-null parent.
   */
  public void setParent(FeatureBean parentModel) {
      // Enforces an additional restriction required for this type of model.
      if (parentModel == null)
          throw new IllegalArgumentException("May not set parent of simple feature to null");
      super.setParent(parentModel);
  } // End method: setParent

  /**
   * Series of getters to directly pull contents.
   */
  public String getSubjectAlignment() { return new String(subjectAlignment != null ? subjectAlignment : new byte[0]); }
  public String getQueryAlignment() { return new String(queryAlignment != null ? queryAlignment : new byte[0]); }
  public OID getSubjectSequenceOid() { return subjectSequenceOid; }
  public String getOutput(String outputName) {
      if (outputMap == null)
          return null;
      else
          return (String)outputMap.get(outputName);
  } // End method: getOutput

  /**
   * Returns the evidence in a useable format.
   */
  public OID[] getEvidence() {
      OID[] returnArray = new OID[evidenceList.size()];
      evidenceList.toArray(returnArray);
      return returnArray;
  } // End method: getEvidence

  /**
   * Allow addition of evidence.
   */
  public void addEvidenceOID(OID oid) {
      evidenceList.add(oid);
  } // End method: addEvidenceOID

  /**
   * Allow addition of evidence.
   */
  public void addEvidenceList(List evidenceList) {
      for (Iterator it = evidenceList.iterator(); it.hasNext(); ) {
        this.evidenceList.add((OID)it.next());
      } // For all iterations.
  } // End method: addEvidenceOID

  /**
   * Allow the generation of an alignment, along with its entity, from the
   * contained info.
   */
  public Alignment alignFeature() {
    int spanLength = 0;

    spanLength = getEnd() - getStart();

    if (getDiscoveryEnvironment() == null)
      setDiscoveryEnvironment(getAnalysisType());  // Make up for not having one.
    GenomicEntity spanEntity = createFeatureEntity();

    //Make axisAlignment for entity
    Alignment spanAlignment = createAlignment(
      getStart(),
      spanLength,
      getAxisOfAlignment(),
      0,                            // Start on entity.
      spanLength,
      spanEntity);

    return spanAlignment;
  } // End method: alignFeature

  /**
   * Returns the range covered by this compound feature.
   */
  public Range calculateFeatureRange() {
    if (getStart() == getEnd())
      return new Range(getStart(), 0, Range.UNKNOWN_ORIENTATION);
    else
      return new Range(getStart(), getEnd());
  } // End method: calculateFeatureRange

} // End class: SimpleFeatureModel
