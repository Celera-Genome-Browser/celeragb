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

import java.util.List;
import java.util.Set;

/**
 *  This loader will parse data for a workspace file and store that data
 *  in class variables for retrieval.  Data must be in GAME XML
 *  format.  This is done through the super class, and this subclass
 *  simply enforces rules of what form OIDs may take in the input file.
 *
 *  Current assumptions:
 *     New Loader instance needs to be created for each XML file
 */
public class WorkspaceXmlLoader extends SAXLoaderBase {

  /**
   * No-args constructor prepares for full parse.
   */
  public WorkspaceXmlLoader() {
    super();
  } // End constructor

  /**
   * No-args constructor prepares for full parse.
   */
  public WorkspaceXmlLoader(int versionId) {
    super(versionId);
  } // End constructor

  /**
   * If the WorkspaceXmlDomLoader is to be used in a system that
   * already has other facade managers loaded, then it may need to tell
   * each of the bizobj instances that it creates which facade manager they should
   * use for subsequent requests
   */
  public WorkspaceXmlLoader(XmlFacadeManager readFacadeManager) {
    setFacadeManager(readFacadeManager);
  } // End constructor

  /**
   * If the WorkspaceXmlDomLoader is to be used in a system that
   * already has other facade managers loaded, then it may need to tell
   * each of the bizobj instances that it creates which facade manager they should
   * use for subsequent requests
   */
  public WorkspaceXmlLoader(XmlFacadeManager readFacadeManager, int versionId) {
    super(versionId);
    setFacadeManager(readFacadeManager);
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
  public OID parseContigOID(String idstr) {

    OID returnOID = parseOIDorGA(idstr);

    // Enforce the restriction that the OID be non-scratch.
    if (returnOID.isScratchOID()) {
        returnOID = null;
        FacadeManager.handleException(
          new IllegalArgumentException("Illegal namespace contig ID "+idstr+": entered in XML file."));
    } // Test for scratch

    return returnOID;
  } // End method

  /**
   * Builds a contig OID with all restrictions and translations required by
   * this contig file DOM loader.
   *
   * Restrictions: features must ALWAYS be in the scratch namespace.
   *
   * This method is used by the superclass, but implemented here in the
   * subclass.
   *
   * @param String idstr of format "PREFIX:dddddddddd"
   *   where the d's represent a decimal (long) number.
   * @return OID.
   */
  public OID parseFeatureOID(String idstr) {

    OID returnOID = parseOIDGeneric(idstr);

    // Enforce the restriction that the OID be ALWAYS scratch.
    if (! returnOID.isScratchOID()) {
        returnOID = null;
        FacadeManager.handleException(
          new IllegalArgumentException("Illegal namespace feature ID "+idstr+": entered in XML file."));
    } // Test for scratch

    return returnOID;
  } // End method

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
   * @return OID.
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
  } // End method

  /** Returns NO genomic axis OID. A signal that no axis is defined here. */
  public OID getGenomicAxisOID() { return null; }

  /** Override of getter, which sets up genome version first. */
  public List getRootFeatures(OID axisOid, Set rangesOfInterest, boolean humanCurated) {
    setGenomeVersionId(axisOid.getGenomeVersionId());
    return super.getRootFeatures(axisOid, rangesOfInterest, humanCurated);
  } // End method

  /** Override of getter, to increase visibility for this particular subclass. */
  public int getGenomeVersionId() {
    return super.getGenomeVersionId();
  } // End method

  /** Return the assembly version for the promotion code. */
  public String getAssemblyVersion() {
    return super.getAssembly();
  } // End method

  /** Force "no knowledge" of genomic axis alignment. */
  public Alignment getGenomicAxisAlignment() { return null; }

  //------------------------------------------UNIT TEST CODE
  public static void main(String[] args) {
    try {
      WorkspaceXmlLoader loader = new WorkspaceXmlLoader();
      StringBuffer buffer = new StringBuffer();
      java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader("E:/gbtestfiles/test_area/cdk_hCG14894_gb4.gbw"));
      String inLine = null;
      while (null != (inLine = br.readLine())) {
        buffer.append(inLine);
      } // For all lines of input.
      loader.loadXml(buffer);
      System.out.println(loader.getAssembly());

      // Now try to force feature loading.
      loader.loadFeaturesIfNeeded();
    } // End try block to unit-test
    catch (Exception ex) {
      // NOTE: this is unit-test code only.  This main routine will not
      // be called from production.  Therefore the print stack trace is
      // not circumventing normal exception handling.
      ex.printStackTrace();
    } // End catch bloc: all exceptions
  } // End main

} // End class: WorkspaceXmlLoader
