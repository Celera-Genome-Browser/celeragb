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
 * Title:        GenomeBrowser<p>
 * Description:  Model to house compound features produced from XML data.<p>
 * Company:      []<p>
 * @author Les Foster
 * @version
 */
package api.facade.concrete_facade.xml.model;

import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.annotation.CuratedCodon;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.fundtype.GenomicEntity;
import api.facade.concrete_facade.xml.XmlFacadeManager;
import api.stub.data.OID;
import api.stub.geometry.Range;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Simple model class for a compound feature.  Bridges the disconnect
 * between XML file and internal browser model.
 */
public class CompoundFeatureModel extends FeatureModel implements Serializable {

  //-------------------------------------INSTANCE VARIABLES
  private List children = new ArrayList(); // Simple model instances.
  private String annotationName;

  //-------------------------------------CONSTRUCTORS
  /**
   * Constructor which takes the oid of this model as well as the OID agains
   * which it will be aligned.
   */
  public CompoundFeatureModel(OID compoundFeatureOID, OID oidOfAlignment,
      XmlFacadeManager readFacadeManager) {

      super(compoundFeatureOID, oidOfAlignment, readFacadeManager);

  } // End constructor

  //-------------------------------------PUBLIC INTERFACE
  /*
   * Simple setters and getters.
   */
  public void setAnnotationName(String annotationName) { this.annotationName = annotationName; }
  public String getAnnotationName() { return annotationName; }

  /**
   * Allows external add of a simple feature.  Compound features are
   * "made up of" simple features.
   */
  public void addChild(FeatureModel featureChild) {
      children.add(featureChild);
  } // End method: addChild

  /**
   * Allow the generation of an alignment, along with its entity, from the
   * contained info.
   */
  public Alignment alignFeature() {

    // Work out the feature type for the entity to be produced.
    if (getAnalysisType() == null) {
      api.facade.facade_mgr.FacadeManager.handleException(new IllegalArgumentException("Unable to determine type to apply to a feature "+getOID()));
    }

    Alignment compoundAlignment = null;
    int start;
    int length;

    Range range = calculateFeatureRange();
    start = range.getStart();
    length = range.getEnd() - start;

    GenomicEntity compoundEntity = createFeatureEntity();

    // Make Alignment for set
    compoundAlignment = createAlignment(start,
        length,
        getAxisOfAlignment(),
        start,
        length,
        compoundEntity);

    // Align all child features.
    if (compoundEntity instanceof CuratedTranscript)
      alignChildrenOfTranscript(children);
    else
      alignChildren(children);

    if (compoundEntity instanceof api.entity_model.model.annotation.HitAlignmentFeature) {
      linkAdjacentHsps();
    } // Type needs adjacency linking.

    return compoundAlignment;
  } // End method: alignFeature

  /**
   * Returns the range covered by this compound feature.
   */
  public Range calculateFeatureRange() {
    Range childRange;
    int rangeMax = Integer.MIN_VALUE;   // Must be changed.
    int rangeMin = Integer.MAX_VALUE;   // Must be changed.
    boolean isReversed = false;
    boolean compoundFeatureIsObsolete = isObsolete();
    FeatureModel childFeature = null;

    for (Iterator it = children.iterator(); it.hasNext(); ) {
      // Getting range of child.
      childFeature = (FeatureModel)it.next();

      // Look at maxima and minima.  Once both are found, backfit
      // the values to start and end based on directionality.
      if ( (compoundFeatureIsObsolete && childFeature.isObsolete()) ||
           ((!compoundFeatureIsObsolete) && (!childFeature.isObsolete())) ) {

        childRange = childFeature.calculateFeatureRange();

        // Same directionality is assumed across all children.
        if (childRange.isReversed())
          isReversed = true;
        else
          isReversed = false;

        if (childRange.getMaximum() > rangeMax)
          rangeMax = childRange.getMaximum();
        if (childRange.getMinimum() < rangeMin)
          rangeMin = childRange.getMinimum();
      } // Matching obsolescence
    } // For all iterations.

    if (isReversed)
      return new Range(rangeMax, rangeMin);
    else
      return new Range(rangeMin, rangeMax);

  } // End method: calculateFeatureRange

  /**
   * Group of getters to from this model.
   */
  public List getChildren() { return children; }

  /**
   * Returns the evidence from sub-features.  Concats into one array.
   */
  public OID[] getEvidence() {
      OID[] returnArray = null;

      List evidenceList = new ArrayList();
      OID[] childEvidence = null;
      int totalSize = 0;
      for (Iterator it = children.iterator(); it.hasNext(); ) {
        childEvidence = ((FeatureModel)it.next()).getEvidence();
        evidenceList.add(childEvidence);
        totalSize += childEvidence.length;
      } // For all iterations

      returnArray = new OID[totalSize];
      int nextOutputOID = 0;
      for (Iterator it = evidenceList.iterator(); it.hasNext(); ) {
        childEvidence = (OID[])it.next();
        for (int i = 0; i < childEvidence.length; i++) {
          returnArray[nextOutputOID++] = childEvidence[i];
        } // For all evidence oids
      } // For all child evidence.
      return returnArray;
  } // End method: getEvidence

  /**
   * Get Start/end values.  Allows this object, a composite of others,
   * to calcuate its start and end based on its contents, rather than
   * attempting to store a static value.
   */
  public int getStart() {
    return this.calculateFeatureRange().getStart();
  } // End method: getStart

  public int getEnd() {
    return this.calculateFeatureRange().getEnd();
  } // End method: getEnd

  //-------------------------------------HELPERS
  /**
   * Align all child features in the list.
   * @parameter List children features to be aligned.
   */
  protected void alignChildren(List children) {
    FeatureModel nextChild = null;
    for (Iterator it = children.iterator(); it.hasNext(); ) {
      nextChild = (FeatureModel)it.next();
      nextChild.alignFeature();

    } // Do sub-features.
  } // End method: alignChildren

  /**
   * Align all child features in the list, adding the start/stop codons last.
   *
   * @parameter List children features to be aligned.
   */
  protected void alignChildrenOfTranscript(List children) {
    FeatureModel nextChild = null;
    List codonList = null;

    // This method simply adds the codons AFTER all the EXONs.  This
    // makes it possible for constraints to be added to the model, which
    // can ask whether the appropriate relationships exists between
    // codons and exons.

    // Scan features: if any are codons, save them for subsequent addition.
    for (int i = 0; i < children.size(); i++) {
      nextChild = (FeatureModel)children.get(i);
      // NOTE: will trigger caching of the entity!
      if (nextChild.createFeatureEntity() instanceof CuratedCodon) {
        if (codonList == null)
          codonList = new ArrayList();
        codonList.add(nextChild);
      } // Found a codon
      else {
        nextChild.alignFeature();
      } // Not a codon

    } // For all children.

    // Simply adds the codons last.
    if (codonList != null) {
      for (Iterator it = codonList.iterator(); it.hasNext(); ) {
        nextChild = (FeatureModel)it.next();
        nextChild.alignFeature();
      } // Do sub-features.
    } // Found codons.

  } // End method: alignChildrenOfTranscript

  /**
   * Establish links between HSPs whose subject positions touch.
   * This should be called only if this feature is a Blast Hit feature.
   */
  private void linkAdjacentHsps() {
    int numberOfHsps = getChildren().size();
    for (int i = 0; i < numberOfHsps; i++) {
      for (int j = i+1; j < numberOfHsps; j++) {
        SimpleFeatureModel outerDetail = (SimpleFeatureModel)getChildren().get(i);
        SimpleFeatureModel innerDetail = (SimpleFeatureModel)getChildren().get(j);

        if ((outerDetail.getSubjectStart() == innerDetail.getSubjectEnd()) ||
            (outerDetail.getSubjectEnd() == innerDetail.getSubjectStart())) {


          try {
            api.entity_model.model.annotation.HSPFeature outerFeature =
              (api.entity_model.model.annotation.HSPFeature)outerDetail.alignFeature().getEntity();
            api.entity_model.model.annotation.HSPFeature innerFeature =
              (api.entity_model.model.annotation.HSPFeature)innerDetail.alignFeature().getEntity();

            outerFeature.setPreviousAdjacentHSP(innerDetail.getOID());
            innerFeature.setSubsequentAdjacentHSP(outerDetail.getOID());
          } // End try block around casting.
          catch (Exception ex) {
            // NOTE: expect here, the exception will be class cast, or
            // null pointer.  In either case, we assume user error in building
            // invalid XML.
          } // End catch block.

        } // Start/end match.

      } // For all children, inner
    } // For all children, outer.
  } // End method

} // End class: CompoundFeatureModel
