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

/**
 *  Provides action handler to open a Feature GAME XML file.
 *  Assumes that:
 *     XmlContigFacade is used for ContigFacade
 */
public class FeatureXmlServiceOpenHandler extends XmlFileOpenHandlerBase {
  //----------------------------INSTANCE VARIABLES
  private FacadeManagerBase facadeManagerBase;

  //----------------------------CONSTRUCTORS
  public FeatureXmlServiceOpenHandler(FacadeManagerBase facadeManagerBase){
    if (facadeManagerBase == null)
      throw new IllegalArgumentException("Non-null facade manager required in "+this.getClass().getName()+" constructor");
    this.facadeManagerBase = facadeManagerBase;
  } // End constructor

  //----------------------------INTERFACE METHODS
  /**
   * Registers a URL with an XML loader.  This URL is later "consulted"
   * for features on demand of the normal region-select-then-load mechanism.
   *
   * @param String urlString the left part of a URL which handles parameters
   *    given to it after the fact.
   */
  public void loadXmlFile(String urlString) throws Exception  {

    // Build and apply the loader.
    //
    ServiceXmlLoader loader = null;

    loader = new ServiceXmlLoader();
    loader.setURL(urlString);

    if (facadeManagerBase instanceof XmlServiceFacadeManager){
      XmlServiceFacadeManager serviceFacadeManager = ((XmlServiceFacadeManager) facadeManagerBase);
      UrlGenomeVersionSpace genomeVersionSpace = (UrlGenomeVersionSpace)serviceFacadeManager.getGenomeVersionSpace();
      genomeVersionSpace.addLoader(loader, getVersionInfo());
    } // Right kind of facade manager.

  } // End method

} // End class: FeatureXmlServiceOpenHandler
