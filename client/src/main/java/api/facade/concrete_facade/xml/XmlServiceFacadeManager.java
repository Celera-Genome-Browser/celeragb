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
package api.facade.concrete_facade.xml;

/**
 * Title:        GV Loader for the Genome Browser
 * Description:  Facade manager for XML feature (URL) service.
 * Company:      []
 * @author Les Foster
 * @version
 */

import api.facade.abstract_facade.genetics.GenomeVersionLoader;
import api.facade.facade_mgr.ConnectionStatus;
import api.facade.facade_mgr.FacadeManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

/**
 * This facade manager provides all facades to field requests for data
 * from a registered XML service.
 */
public class XmlServiceFacadeManager extends XmlFacadeManager {

  //-------------------------------------------CONSTANTS

  private static String fileSep=File.separator;
  private static final String LOCATION_PROP_NAME = "XmlURLs";

  public static final String URL_SETTING_PREFIX = "XMLService.URL.".intern();

  public static final File URLS_PREF_FILE=new File(System.getProperty("user.home")+fileSep+
         "x"+fileSep+"GenomeBrowser"+fileSep+LOCATION_PROP_NAME+".properties");

  //-------------------------------------------MEMBER VARIABLES
  private GenomeVersionLoader genomeVersion = null;

  //-------------------------------------------CONSTRUCTORS
  /** Simplest constructor. */
  public XmlServiceFacadeManager() {
    super();
    // Setting the genome version space to one that deals with
    // an XML service URL.
    setGenomeVersionSpace(new UrlGenomeVersionSpace());
    setupGenomeVersionSpace();
  } // End constructor

  //-------------------------------------------INTERFACE CLASS METHODS
  /** Gets old or current URL locations for services. */
  public static List getExistingLocations() {
    List returnList = new ArrayList();
    try {

      if (URLS_PREF_FILE.canRead()) {
        FileInputStream fis = new FileInputStream(URLS_PREF_FILE);
        Properties urlProperties = new Properties();
        urlProperties.load(fis);
        // NOTE: assumption here that the properties file has only props of the
        // name of interest plus .*  If large numbers of properties wind up
        // in this property file, this method will become inefficient.
        for (Enumeration e = urlProperties.propertyNames(); e.hasMoreElements(); ) {
          String nextName = (String)e.nextElement();
          if (nextName.startsWith(URL_SETTING_PREFIX)) {
            returnList.add(((String)urlProperties.get(nextName)).trim());
          } // Got a url setting.
        } // For all enumerations.
      } // Permission granted.
      else {
        if (URLS_PREF_FILE.exists())
          FacadeManager.handleException(new IllegalArgumentException("Service URL Settings File "+URLS_PREF_FILE.getAbsolutePath()
            +" exists but is not readable by the Genome Browser.  Please change its permissions."));
        returnList = Collections.EMPTY_LIST;
      } // Not granted.

    } // End try
    catch (Exception ex) {
      throw new IllegalStateException(ex.getMessage());
    } // End catch block for pref file open exceptions.

    return returnList;
  } // End method

  /**
   * Allows the new URL locations in the list given, to be written back to the
   * disk for later use.
   */
  public static void setNewLocations(List newLocs) {
    // Now attempt to writeback the user's currently-selected directory as the
    // new preference for reading XML files.
    //
    try {
      if (newLocs != null) {
        ArrayList outputList = new ArrayList(newLocs);
        PrintWriter writer = new PrintWriter(new FileWriter(XmlServiceFacadeManager.URLS_PREF_FILE));
        int i = 0;
        for (Iterator it = outputList.iterator(); it.hasNext(); ) {
          String nextLocation = ((String)it.next()).trim();
          writer.println(XmlServiceFacadeManager.URL_SETTING_PREFIX + ++i + "=" + nextLocation);
        } // For all new locations.
        writer.close();
      } // Permission granted.
      else {
        FacadeManager.handleException(new IllegalArgumentException("XML Service URL List is null or Cannot Write "+URLS_PREF_FILE.getAbsoluteFile()+". Failed to save changes"));
      } // Not granted
    } // End try block.
    catch (Exception ex) {
      FacadeManager.handleException(new IllegalArgumentException("XML URL Preferences File "+URLS_PREF_FILE.getAbsoluteFile()+" Cannot be Written.  Failed to save changes."));
    } // End catch block for writeback of preferred directory.
  } // End method

  //-------------------------------------------FACADE MGR BASE OVERRIDES
  /**
   * This method will be called to initialize the FacadeManager instance.
   */
  public ConnectionStatus initiateConnection() {
    try {
      List locations=getExistingLocations();
      if (locations == null || locations.isEmpty() )
        return CONNECTION_STATUS_NO_DEFINED_INFORMATION_SERVICE;
      else
        return CONNECTION_STATUS_OK;
    } // End try for connection/
    catch (IllegalStateException ise) {
      return CONNECTION_STATUS_CANNOT_CONNECT;
    } // End catch for defined-but-not-usable
  } // End method

  /**
   * Returns an indicator of whether additional sources may be added to
   * this factory.
   */
  public boolean canAddMoreDataSources() {
    // Many URLs may be added during the course of a session.
    return true;
  } // End method: canAddMoreDataSources

  /**
   * Returns a class name to be instantiated to return a data source for this
   * factory.
   */
  public String getDataSourceSelectorClass() {
    return (null);
    //    return ("client.gui.other.data_source_selectors.XmlFeatureServiceSelector");
  } // End method

  /** Tells what is being used. */
  public Object[] getOpenDataSources() {
    return ((UrlGenomeVersionSpace)getGenomeVersionSpace()).getUrlStrings();
  } // End method

  /** Returns the genome version facade. */
  public GenomeVersionLoader getGenomeVersion() throws Exception{
    if (genomeVersion == null) {
      XmlServiceGenomeVersion xmlServiceGenomeVersion = new XmlServiceGenomeVersion();
      xmlServiceGenomeVersion.setGenomeVersionSpace(getGenomeVersionSpace());
      genomeVersion = xmlServiceGenomeVersion;
    } // Need to create it.
    return genomeVersion;
  } // End method: getGenomeVersion */

  //-------------------------------------------HELPERS
  /** Goes out and grabs the group of pre-set URLs to make this. */
  private void setupGenomeVersionSpace() {
    UrlGenomeVersionSpace genomeVersionSpace = (UrlGenomeVersionSpace)getGenomeVersionSpace();
    List urlList = getExistingLocations();
    ServiceXmlLoader loader = null;
    String nextUrl = null;
    for (Iterator it = urlList.iterator(); it.hasNext(); ) {
      nextUrl = (String)it.next();
      loader = new ServiceXmlLoader();
      loader.setURL(nextUrl);
      genomeVersionSpace.addLoader(loader, null); // Leaving the info setting to null for now.
    } // For all iterations.
  } // End method

} // End class: XmlServiceFacadeManager
