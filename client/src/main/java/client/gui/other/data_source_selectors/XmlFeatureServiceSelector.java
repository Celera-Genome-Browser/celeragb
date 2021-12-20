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

/**
 * Title:        Browser
 * Description:  Lets user provide URLs as a source of feature data.
 * Company:      []
 * @author Les Foster
 * @version
 */

import api.entity_model.management.ModelMgr;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.concrete_facade.xml.FeatureXmlServiceOpenHandler;
import api.facade.facade_mgr.DataSourceSelector;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;
import client.gui.framework.session_mgr.SessionMgr;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

/**
 * GUI, callable via dynamic instantiation, that will let user enter a
 * URL to contact for feature data.
 */
public class XmlFeatureServiceSelector extends Component implements DataSourceSelector {

  private static final String[] NO_OPTIONS = new String[0];
  private static final String REJECTED_URL_MSG = "No Selected Genome Versions";

  /** Simplest constructor. */
  public XmlFeatureServiceSelector() {
  } // End constructor

  /** Allows set of pre-selected data source. */
  public void setDataSource(FacadeManagerBase facade, Object dataSource) {
    FeatureXmlServiceOpenHandler opener = new FeatureXmlServiceOpenHandler(facade);
    try {
      opener.loadXmlFile((String)dataSource);
    }
    catch(Exception e){
      client.gui.framework.session_mgr.SessionMgr.getSessionMgr().handleException(e);
    } // End catch block for load.
    setFacadeProtocol();
  } // End method: setDataSource

  /**
   * Implementation of DataSourceSelector: allows selection of URL.
   */
  public  void selectDataSource(FacadeManagerBase facade){
    // Get the user's choice.
    String urlString = askUserForURL();

    FeatureXmlServiceOpenHandler openHandler = new FeatureXmlServiceOpenHandler(facade);
    // If the user selected anything, register it.
    //
    if (urlString != null) {
      Set genomeVersions = ModelMgr.getModelMgr().getSelectedGenomeVersions();
      if (genomeVersions.size() > 1) {
        MatchingGenomeVersionSelector subSelector = new MatchingGenomeVersionSelector();
        subSelector.presentUserWithChooser((Collection)genomeVersions, urlString, facade,
          openHandler);
        return;
      } // Matches many
      else if (genomeVersions.size() == 1) {
        GenomeVersion genomeVersion = (GenomeVersion)genomeVersions.iterator().next();
        openHandler.setGenomeVersionInfo(genomeVersion.getGenomeVersionInfo());
        try {
          openHandler.loadXmlFile(urlString);
        }
        catch (Exception ex) {
          FacadeManager.handleException(ex);
        } // End catch block for url loading.
        setFacadeProtocol();
      } // Matches exactly one.
      else if (genomeVersions.size() == 0) {
        FacadeManager.removeProtocolFromUseList(FacadeManager.getProtocolForFacade(facade.getClass()));

        JOptionPane.showMessageDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
          "The URL "+urlString+", May not Be Loaded without a Genome Version",
          REJECTED_URL_MSG, JOptionPane.ERROR_MESSAGE);

      } // No match
    } // User presented a real file name.

  } // End method: selectDataSource

  /** Sets up the facade protocol so facade manager is queried for data. */
  protected void setFacadeProtocol() {
    String protocolToAdd
      = new String(FacadeManager.getProtocolForFacade(api.facade.concrete_facade.xml.XmlServiceFacadeManager.class));
    FacadeManager.addProtocolToUseList(protocolToAdd);
  } // End method: setFacadeProtocol

  /**
   * Returns user-entered string representing a URL.
   */
  protected String askUserForURL() {
    // Will present a text field.
    //
    boolean goodChoice = false;
    String urlString = null;
    while (! goodChoice) {
      urlString = (String)JOptionPane.showInputDialog( SessionMgr.getSessionMgr().getActiveBrowser(),
        "Please Enter a URL", "XML Feature Service", JOptionPane.PLAIN_MESSAGE,
        null, null, "");

      // Abort if user hit cancel.
      //
      if (urlString == null)
        return null;

      // Check the URL for 'well-formedness'.
      //
      try {
        new URL(urlString.trim());
        goodChoice = true;
      }
      catch (MalformedURLException mue) {
        JOptionPane.showMessageDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
          "Bad URL, Please Try Again.");
      } // End catch block for testing.
    } // Until user enters well-formed URL or cancels.

    return urlString;

  } // End method: askUserforURL

} // End class: XmlFeatureServiceSelector
