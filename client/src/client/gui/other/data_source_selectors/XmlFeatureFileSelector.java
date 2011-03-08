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

package client.gui.other.data_source_selectors;

import api.facade.concrete_facade.xml.FeatureXmlFileOpenHandler;
import api.facade.facade_mgr.DataSourceSelector;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;
import shared.io.ExtensionFileFilter;

import java.io.File;

/**
 * Allows user to select an XML feature file and pass it to an open handler.
 */
public class XmlFeatureFileSelector extends XmlFileSelector implements DataSourceSelector{
  private ExtensionFileFilter xmlFilter =
    new ExtensionFileFilter("Genome Browser Feature File (*.gbf)", ".gbf");

  /**
   * Implementation of DataSourceSelector: allows selection of a file of specific
   * type.
   */

  public void setDataSource(FacadeManagerBase facade, Object dataSource){
    FeatureXmlFileOpenHandler g=new FeatureXmlFileOpenHandler(facade);
    try {
      g.loadXmlFile((String)dataSource);
    }
    catch(Exception e){
      e.printStackTrace();
    }
    setFacadeProtocol();
  }

  public  void selectDataSource(FacadeManagerBase manager){

    String fileName=null;

    // Get the user's choice.
    File selectedFile=askUserForFile(this.xmlFilter);

    // Bug found in JBuilder3.5 debugger.  Get failure when using JFileChooser.  If you
    // uncomment the below and comment out the "askUserForFile", you can get past this...
    //File selectedFile=new File("c:\\cvsfiles\\client-devel\\bin\\resource\\client\\XMLdata\\sample2.gbf");

    // If the user selected anything, load it up.
    if (selectedFile != null) {
      fileName = selectedFile.getAbsolutePath();
      FeatureXmlFileOpenHandler openHandler = new FeatureXmlFileOpenHandler(manager);

      try{
        openHandler.loadXmlFile(fileName);
        setFacadeProtocol();
      } catch(Exception e){
        e.printStackTrace();
      } // End catch block for file load.
    } // User presented a real file name.

  } // End method: selectDataSource

  //This method is defined for all sub classes of XmlFileSelector.
  protected void setFacadeProtocol() {
    String protocolToAdd = new String(
            FacadeManager.getProtocolForFacade(api.facade.concrete_facade.xml.XmlInSessionLoadFacadeManager.class));
    FacadeManager.addProtocolToUseList(protocolToAdd);
  }

} // End class: XmlFeatureFileSelector
