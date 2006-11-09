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

/**
 *  Provides action handler to open a Contig GAME XML file.
 *  Assumes that:
 *     XmlContigFacade is used for ContigFacade
 */
public class WorkspaceXmlFileOpenHandler extends XmlFileOpenHandlerBase  {
  //----------------------------CONSTRUCTORS
  public WorkspaceXmlFileOpenHandler(){
   // doesnt do anything
  }

  public WorkspaceXmlFileOpenHandler(FacadeManagerBase facadeManager){
    super(facadeManager);
  }

  //----------------------------INTERFACE METHODS
  /**
   * Finds the loader (there should be only one) assembly version.
   */
  public GenomeVersionInfo findGenomeVersionInfo(String source) {
    GenomeVersionParser parser = new GenomeVersionParser();
    return parser.parseForGenomeVersionInfo(source);
  } // End method

  //----------------------------OVERRIDES TO XmlFileOpenHandlerBase
  /**
   * Loads the XML file by producing a loader, using that loader to
   * parse and save the file's XML data, and then setting that loader
   * for use from various facades.
   *
   * @param String xmlFile the name, fully-qualified, of the file to load.
   */
  public void loadXmlFile(String xmlFile) throws Exception  {

    // Build and apply the loader.
    //
    GenomeVersionInfo info = getVersionInfo();
    int genomeVersionId = -1;
    if (info != null)
      genomeVersionId = info.getGenomeVersionId();
    XmlLoader loader = new WorkspaceXmlLoader(genomeVersionId);

    loader.loadXml(xmlFile);

    FacadeManagerBase facadeManagerBase = getFacadeManagerBase();
    if(facadeManagerBase instanceof XmlWorkspaceFacadeManager){
      XmlWorkspaceFacadeManager wsFacadeManager = ((XmlWorkspaceFacadeManager) facadeManagerBase);
      GenomeVersionSpace genomeVersionSpace = wsFacadeManager.getGenomeVersionSpace();
      ((FileGenomeVersionSpace)genomeVersionSpace).addLoader(loader);
    }

  } // End method: loadXmlFile

} // End class: WorkspaceXmlFileOpenHandler
