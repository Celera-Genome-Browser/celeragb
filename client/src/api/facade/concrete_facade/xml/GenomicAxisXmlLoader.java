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
import api.entity_model.model.genetics.Chromosome;
import api.entity_model.model.genetics.Species;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;

/**
 *  This loader will parse data for a genomic axis through its superclass,
 *  and store it for later use.  This subclass enforces restrictions on
 *  the form of OIDs that may be placed in a genomic axis XML game file.
 *
 *  Current assumptions:OO
 *     One genomic axis per GAME XML file -- the first seq
 *     A fake contig will be created that is the lenth of the axis
 *     Genomic Axis (first seq) includes (ungapped) residue info
 *     All result sets are relative to contig as "query" seq
 *     New instance of this class must be created for each XML file.
 */
public class GenomicAxisXmlLoader extends SAXLoaderBase {

  private static String CHROMOSOME_DISPLAY_NAME = "Unknown Chromosome";

  private Alignment chromosomeAlignment = null;
  private Alignment genomicAxisAlignment = null;
  private OID speciesOID = null;
  private boolean initialLoadIsComplete = false;

  /**
   * No-args constructor prepares for full parse.
   */
  public GenomicAxisXmlLoader() {
  } // End constructor

  /**
   * No-args constructor prepares for full parse.
   */
  public GenomicAxisXmlLoader(OID speciesOID) {
    super(speciesOID);
  } // End constructor

  /**
   * This constructor adds a sequence alignment to be handed off to
   * delegates.
   */
  public GenomicAxisXmlLoader(SequenceAlignment sequenceAlignment, int genomeVersionID) {
    super(sequenceAlignment, genomeVersionID);
  } // End constructor

  /**
   * Builds a genomic axis OID with all restrictions and translations required by
   * this loader. (misnamed method)
   *
   * Restrictions: contig OIDs may not be in the SCRATCH namespace!
   *
   * This method is used by the superclass, but implemented here in the
   * subclass.
   *
   * @param String idstr of format "PREFIX:dddddddddd"
   *   where the d's represent a decimal (long) number.
   * @return OID a Contig OID.
   */
  public OID parseContigOIDTemplateMethod(String idstr) {

    OID returnOID = parseOIDorGA(idstr);

    // Enforce the restriction that the OID be non-scratch.
    if (returnOID.isScratchOID()) {
        returnOID = null;
        FacadeManager.handleException(
          new IllegalArgumentException("Illegal namespace contig ID "+idstr+": entered in XML file."));
    } // Test for scratch

    return returnOID;
  } // End method: parseContigOIDTemplateMethod

  /**
   * Builds a contig OID with all restrictions and translations required by
   * this contig file DOM loader.
   *
   * Restrictions: contig OIDs may not be in the SCRATCH namespace!
   *
   * This method is used by the superclass, but implemented here in the
   * subclass.
   *
   * @param String idstr of format "PREFIX:dddddddddd"
   *   where the d's represent a decimal (long) number.
   * @return OID a Contig OID.
   */
  public OID parseFeatureOIDTemplateMethod(String idstr) {

    OID returnOID = parseOIDGeneric(idstr);

    // Enforce the restriction that the OID be non-scratch.
    if (returnOID.isScratchOID()) {
        returnOID = null;
        FacadeManager.handleException(
          new IllegalArgumentException("Illegal namespace feature ID "+idstr+": entered in XML file."));
    } // Test for scratch

    return returnOID;
  } // End method: parseFeatureOIDTemplateMethod

  /**
   * Builds a contig OID with all restrictions and translations required by
   * this contig file DOM loader.
   *
   * Restrictions: contig OIDs may not be in the SCRATCH namespace!
   *
   * This method is used by the superclass, but implemented here in the
   * subclass.
   *
   * @param String idstr of format "PREFIX:dddddddddd"
   *   where the d's represent a decimal (long) number.
   * @return OID a Contig OID.
   */
  public OID parseEvidenceOIDTemplateMethod(String idstr) {

    OID returnOID = parseOIDGeneric(idstr);

    // Enforce the restriction that the OID be non-scratch.
    if (returnOID.isScratchOID()) {
        returnOID = null;
        FacadeManager.handleException(
          new IllegalArgumentException("Illegal namespace evidence ID "+idstr+": entered in XML file."));
    } // Test for scratch

    return returnOID;
  } // End method: parseEvidenceOIDTemplateMethod

  /**
   * Produces a new Chromosome, and a new alignment, if not already
   * built.
   */
  public Alignment getAlignmentForChromosome(Species species) {

    if (chromosomeAlignment == null) {
        // Build OID with same genomic version id as aligning entity.
        OID chromosomeOID = OIDGenerator.getOIDGenerator().generateOIDInNameSpace(
            OID.API_GENERATED_NAMESPACE, species.getOid().getGenomeVersionId()
        );

        Chromosome chromosome = new Chromosome(chromosomeOID, CHROMOSOME_DISPLAY_NAME);
        chromosomeAlignment = new Alignment(null, chromosome);
    } // Have no alignment

    return chromosomeAlignment;

  } // End method: getAlignmentForChromosome

  /**
   * Calling this forces response to sequence alignment listener.
   */
  public void scanForSequenceAlignments() {
    loadInitialIfNeeded();
  } // End method

  /**
   * Override the initial load call, so that can keep track of what data
   * has been sourced from this loader.
   */
  protected synchronized void loadInitialIfNeeded() {
    this.initialLoadIsComplete = true;
    super.loadInitialIfNeeded();
  } // End method

} // End class: GenomicAxisXmlLoader
