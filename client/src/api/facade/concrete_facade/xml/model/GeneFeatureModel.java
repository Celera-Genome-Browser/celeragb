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
 * Description:  Model of a gene as it comes from the XML files.<p>
 * Company:      []<p>
 * @author Les Foster
 * @version
 */
package api.facade.concrete_facade.xml.model;

import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.annotation.CuratedGene;
import api.facade.concrete_facade.xml.XmlFacadeManager;
import api.stub.data.OID;
import api.stub.geometry.Range;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Simple model class to represent a Gene or annotation.
 */
public class GeneFeatureModel extends CompoundFeatureModel implements Serializable {

  /**
   * Constructor sets up all identifiers to resolve what this model
   * "is" and what it is referring to (the axis against which it will align).
   */
  public GeneFeatureModel(OID geneOID, OID genomicAxisOID,
    XmlFacadeManager readFacadeManager) {

    super(geneOID, genomicAxisOID, readFacadeManager);
    super.setParent(null);    // Must set the parent, but it WILL be null, so set it here.
    setAnalysisType("GENE");
    setDiscoveryEnvironment("");
  } // End constructor.

  /**
   * Produce the axis alignment for this feature, and produce the feature
   * model itself.
   */
  public Alignment alignFeature() {
    // Create the curated gene.
    //
    CuratedGene geneEntity = (CuratedGene)createFeatureEntity();

    // Get this (gene) feature's range.
    Range geneRange = calculateFeatureRange();

    // Create an axis alignment for the gene's genomic entity.  Align it against
    // the same axis found in the sub features of the gene.  Subfeature is the
    // Chrom Team term for the transcripts belonging to a gene.
    int startOnEntity = (geneRange.getMagnitude() < 0) ? geneRange.getMagnitude() : 0;

    Alignment geneAlignment = createAlignment(
      geneRange.getStart(), geneRange.getMagnitude(),
      getAxisOfAlignment(), startOnEntity, geneRange.getMagnitude(), geneEntity);

    // Align all children.
    FeatureModel nextChild = null;
    for (Iterator it = getChildren().iterator(); it.hasNext(); ) {
      nextChild = (FeatureModel)it.next();
      nextChild.alignFeature();
    } // Do sub-features.

    return geneAlignment;
  } // End method: alignFeature

  /**
   * Debugging dump.
   */
  public String toString() {
    StringBuffer returnVal = new StringBuffer(400);
    if (this.getChildren() != null) {
      for (Iterator it = this.getChildren().iterator(); it.hasNext(); ) {
        returnVal.append("   "+((FeatureModel)it.next()).toString());
      } // For all iterations
    } // Has children.

    return returnVal.toString();
  } // End method: toString

} // End class: GeneFeatureModel
