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

import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.GenomeVersionInfo;
import api.stub.data.OID;
import shared.io.ExtensionFileFilter;

import java.awt.*;
import java.io.File;
/**
 *  Provides action handler to open a GAME XML file.
 *  Assumes that:
 *     XmlContigFacade is used for ContigFacade
 *  Acts as a collection of facilities for the use of its concrete subclasses.
 */
public abstract class XmlFileOpenHandlerBase extends Component /*implements ActionListener */ {

  //----------------------------CLASS MEMBER VARIABLES
  private static ExtensionFileFilter xml_filter =
    new ExtensionFileFilter("XML Data File (*.xml)", ".xml");
  private static String fileSep=System.getProperty("file.separator");
  private File directoryPrefFile=new File(System.getProperty("user.home")+fileSep+
         "x"+fileSep+"GenomeBrowser"+fileSep+"userPrefs.XMLDirectory");

  static private int contig_num = 0;
  private static int path_num = 0;
  protected FacadeManagerBase facadeManager;

  //----------------------------INSTANCE MEMBER VARIABLES
  private GenomeVersionInfo genomeVersionInfo = null;

  private OID contig_oid, path_oid;

  //----------------------------CONSTRUCTORS

  public XmlFileOpenHandlerBase() {

  } // End constructor

  public XmlFileOpenHandlerBase(FacadeManagerBase facadeManager) {
    this.facadeManager = facadeManager;
  } // End constructor

  //----------------------------INTERFACE METHODS
  /**
   * Loads the XML file by producing a loader, using that loader to
   * parse and save the file's XML data, and then setting that loader
   * for use from various facades.
   * FORMAT NOTES: requires the GAME XML flavor for use with internal
   *   contigs.
   *
   * @param String xml_file the name, fully-qualified, of the file to load.
   */
  public abstract void loadXmlFile(String xml_file) throws Exception;

  /** Getter for the manager-base of the relevant facade. */
  public FacadeManagerBase getFacadeManagerBase() { return facadeManager; }

  /**
   * Allow external setup of the version info chosen for the file to be
   * opened by this object.
   */
  public void setGenomeVersionInfo(GenomeVersionInfo info) {
    genomeVersionInfo = info;
  } // End method

  /** Getter for version. */
  public GenomeVersionInfo getVersionInfo() {
    return genomeVersionInfo;
  } // End method

  //----------------------------HELPER OR "FACILITY" METHODS
  /**
   * Getter to expose the path Object ID to subclasses.
   */
  protected OID pathOID() { return path_oid; }

  /**
   * Getter to expose the contig Object ID to subclasses.
   */
  protected OID contigOID() { return contig_oid; }

} // End class: XmlFileOpenHandlerBase

