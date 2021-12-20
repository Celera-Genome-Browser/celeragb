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
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.OID;

/**
 *  This loader will parse data for a set of features and store the data
 *  in class variables.  Data must be in GAME XML format.  This
 *  subclass enforces restrictions on what types of object IDs may be
 *  contained in a feature file.
 *
 *  Current assumptions:
 *     New instance of this class needs to be created for each XML file loaded in
 */
public class FeatureXmlLoader extends SAXLoaderBase {

  //---------------------------------CONSTRUCTORS
  /**
   * No-args constructor prepares for full parse.
   */
  public FeatureXmlLoader() {
  } // End constructor

  /**
   * OID constructor seeds super class for species settings.
   */
  public FeatureXmlLoader(int genomeVersionID) {
    super(genomeVersionID);
  } // End constructor.

  /**
   * If the FeatureXmlLoader is to be used in a system that
   * already has other facade managers loaded, then it may need to tell
   * each of the entity_model instances that it creates which facade manager
   * they should use for subsequent requests. This constructor allows the caller
   * to specify such a facade manager to be used by constructed objects.
   * This constructor is used in retrieving blast results, so care must
   * be taken!!!!
   *
   * @param readFacadeManager - facade manager that constructed entity model
   * classes will use to fullfil subsequent requests.
   * @param genomeVersionId for constructing OIDs in the proper gv.
   */
  public FeatureXmlLoader(XmlFacadeManager readFacadeManager, int genomeVersionId) {
    super(genomeVersionId);
    setFacadeManager(readFacadeManager);
  } // End constructor

  //---------------------------------INTERFACE METHODS
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
  public OID parseContigOID(String idstr) {

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
  public OID parseFeatureOID(String idstr) {

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
  public OID parseEvidenceOID(String idstr) {

    OID returnOID = parseOIDGeneric(idstr);

    // Enforce the restriction that the OID be non-scratch.
    if (returnOID.isScratchOID()) {
        returnOID = null;
        FacadeManager.handleException(
          new IllegalArgumentException("Illegal namespace evidence ID "+idstr+": entered in XML file."));
    } // Test for scratch

    return returnOID;
  } // End method: parseEvidenceOIDTemplateMethod

  /** Returns NO genomic axis OID. A signal that no axis is defined here. */
  public OID getGenomicAxisOID() { return null; }

  /** Force "no knowledge" of genomic axis alignment. */
  public Alignment getGenomicAxisAlignment() { return null; }

  //----------------------------OVERRIDES FOR INCLUSION IN SETS
  /** An equals test: compare loaded file names and gv id only. */
  public boolean equals(Object o) {
    if (o instanceof FeatureXmlLoader) {
      FeatureXmlLoader otherObject = (FeatureXmlLoader)o;
      if (otherObject.getGenomeVersionId() == getGenomeVersionId() &&
          otherObject.getLoadedFileNames().equals(getLoadedFileNames()))
        return true;
    } // Same type of object.

    return false;
  } // End method

  //----------------------------OVERRIDES FOR EFFICIENCY
  /**
   * Avoid doing an initial scan for data that a feature file does not
   * contain.
   */
  protected void loadInitialIfNeeded() { }  // NEVER needed.

  //----------------------------UNIT TEST CODE
  /**
   *  Unit-testing main routine.
   */
  public static void main(String[] args) {
    SAXLoaderBase loader = new FeatureXmlLoader();
    String inputFile = "\\cvsfiles\\client-devel\\bin\\resource\\client\\XMLdata\\all_features_305.gbf";

    System.out.println("Trying file load...");
    loader.loadXml(inputFile);
    java.util.Set returnSet = loader.getReferencedOIDSet();
    for (java.util.Iterator it = returnSet.iterator(); it.hasNext(); ) {
      System.out.println((OID)it.next());
    } // For all ref'd oids.

    System.out.println("");
    System.out.println("Trying string buffer load...");
    returnSet = loader.getReferencedOIDSet();
    try {
      java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(inputFile));
      StringBuffer collectedFileBuffer = new StringBuffer();
      String nextLine = null;
      while (null != (nextLine = reader.readLine())) {
        collectedFileBuffer.append(nextLine);
      } // For all in lines
      loader.loadXml(collectedFileBuffer);
    } // end
    catch (Exception ex) {
    } // End catch
    for (java.util.Iterator it = returnSet.iterator(); it.hasNext(); ) {
      System.out.println((OID)it.next());
    } // For all ref'd oids.

  } // End method: main
} // End class: FeatureXmlLoader
