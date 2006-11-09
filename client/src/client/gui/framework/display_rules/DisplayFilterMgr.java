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
package client.gui.framework.display_rules;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.framework.session_mgr.SessionModelListener;
import shared.preferences.InfoObject;
import shared.preferences.PreferenceManager;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;


public class DisplayFilterMgr extends PreferenceManager {
  private static DisplayFilterMgr displayFilterMgr;
  private TreeMap intensityCollection = new TreeMap();
  private TreeMap propertySortCollection = new TreeMap();

  static {
    DisplayFilterMgr.getDisplayFilterMgr();
  }

  private DisplayFilterMgr() {
    DEBUG = false;
    userFileDescription = "Display Filters";
    // Set up the necessary attributes.
    setFileParameters();

    // This listener is to hear when the client session is exiting and save.
    SessionMgr.getSessionMgr().addSessionModelListener(new MySessionModelListener());
    initializeMasterInfoObjects();
    resetWorkingCollections();
  }


  /**
   * Inputs the required values to make the mechanism work.
   */
  public void setFileParameters() {
    this.filenameFilter  = "_DisplayFilters.properties";
    this.defaultFilename = "";

    // Check for the existence of the group directory property and/or set values.
    String groupDir = "";
    if (SessionMgr.getSessionMgr().getModelProperty(GROUP_DIR)==null) {
      SessionMgr.getSessionMgr().setModelProperty(GROUP_DIR, groupDir);
    }
    else groupDir = (String)SessionMgr.getSessionMgr().getModelProperty(GROUP_DIR);
    setGroupPreferenceDirectory(groupDir);

    // Establish the initial "default" user file.
    this.userDirectory = new String(System.getProperty("user.home")+File.separator+
      "x"+File.separator+"GenomeBrowser"+File.separator);

    this.userFilename = userDirectory + "User_DisplayFilters.properties";
  }


  public static DisplayFilterMgr getDisplayFilterMgr() {
    if (displayFilterMgr==null) displayFilterMgr=new DisplayFilterMgr();
    return displayFilterMgr;
  }


  public void addPropertySortFilter(PropertySortInfo sortFilter) {
      propertySortCollection.put(sortFilter.getName(),sortFilter);
      firePropertySortFiltersChanged();
  }

  public void deletePropertySortFilter(PropertySortInfo sortFilter) {
    String tmpName = sortFilter.getName();
    deletedInfos.put(sortFilter.getKeyName(), sortFilter);
    propertySortCollection.remove(tmpName);
    firePropertySortFiltersChanged();
  }


  public void addDisplayFilterListener(DisplayFilterListener listener) {
     if (listeners==null) listeners=new ArrayList();
     listeners.add(listener);
  }

  public void removeDisplayFilterListener(DisplayFilterListener listener) {
     if (listeners==null) return;
     listeners.remove(listener);
     if (listeners.isEmpty()) listeners=null;
  }

  public void addColorIntensityFilter(ColorIntensityInfo colorFilter) {
      intensityCollection.put(colorFilter.getName(),colorFilter);
      fireColorIntensityFiltersChanged();
  }

  public void deleteColorIntensityFilter(ColorIntensityInfo colorFilter) {
    String tmpName = colorFilter.getName();
    deletedInfos.put(colorFilter.getKeyName(), colorFilter);
    intensityCollection.remove(tmpName);
    fireColorIntensityFiltersChanged();
  }


  /**
   * Returns the object that contains the settings that should be used when a
   * filter is applied.
   */
  public PropertySortInfo getPropertySortInfo(String filterName) {
    if (filterName == null) return null;
    if (propertySortCollection.get(filterName)!=null)
      return (PropertySortInfo)propertySortCollection.get(filterName);
    else {
      if (DEBUG) System.out.println("That PropertySortInfo "+filterName+" does not exist.");
      return null;
    }
  }


  public DisplayFilterInfo getDisplayFilterInfo(String filterName){
     DisplayFilterInfo displayInfo=getPropertySortInfo(filterName);
     if(displayInfo==null){
      return(getColorIntensityInfo(filterName));
     }else{
        return displayInfo;
     }
  }

  /**
   * Returns the object that contains the settings that should be used when a
   * filter is applied.
   */
  public ColorIntensityInfo getColorIntensityInfo(String filterName) {
    if (filterName == null) return null;
    if (intensityCollection.get(filterName)!=null)
      return (ColorIntensityInfo)intensityCollection.get(filterName);
    else {
      if (DEBUG) System.out.println("That ColorIntensityInfo "+filterName+" does not exist.");
      return null;
    }
  }


  /**
   * This method is a no op to this filter class as there are no default filters
   * yet.
   */
  public void handleDefaultKeyOverrideRequest() {}

  protected void handleOutputWriteError(){
    System.out.println("Error writing back to file.");
    /**
     * @todo need to figure out what to do with write errors.
     */
  }


  /**
   * This method will send all of its proprietary collections to the base class method
   * formatOutput(), one-by-one.
   */
  protected void writeOutAllCollections(FileWriter writer, String destinationFile) {
    addCollectionToOutput(writer, intensityCollection, destinationFile);
    addCollectionToOutput(writer, propertySortCollection, destinationFile);
  }


  /**
   * This method is supposed to hierarchially add info objects to the final
   * collections.  Any deletion should be a cause for remerging; for example, if
   * a user defined info object is removed it could be replaced with a group
   * or default object.  Currently, this manager knows about ColorIntensityInfo
   * and PropertySortInfo objects.
   */
  protected void mergeIntoWorkingCollections(Map targetMasterCollection) {
    for (Iterator it = targetMasterCollection.keySet().iterator();it.hasNext();) {
      InfoObject tmpObject = (InfoObject)((InfoObject)targetMasterCollection.get((String)it.next())).clone();
      if (tmpObject instanceof ColorIntensityInfo) {
        intensityCollection.put(tmpObject.getName(), tmpObject);
      }
      else if (tmpObject instanceof PropertySortInfo) {
        propertySortCollection.put(tmpObject.getName(), tmpObject);
      }
    }
  }


  /**
   * This method sifts through the Java property keys and puts them in sub-groups by
   * type.  It then passes those Properties objects on to the build methods.
   * Remember this is for only one source, Default or User, at a time.
   */
  protected void populateInfoObjects(Properties allProperties, Map targetMasterCollection,
                                   String sourceFile) {
    Properties allIntensityProperties=new Properties();
    Properties allPropertySortProperties=new Properties();
    //Separate all properties into the separate categories
    for (Enumeration enum=allProperties.propertyNames();enum.hasMoreElements();) {
      String tempKey = new String ((String)enum.nextElement());
      StringTokenizer mainToken = new StringTokenizer(tempKey,".");
      if (tempKey!=null && mainToken!=null && tempKey!="") {
        String firstToken = new String (mainToken.nextToken());
        String tmpValue = (String)allProperties.get(tempKey);
        String remainingString;
        if (!mainToken.hasMoreTokens()) remainingString = "";
        else remainingString = tempKey.substring(firstToken.length()+1);
        if (firstToken.equals("ColorIntensityFilter")) allIntensityProperties.setProperty(remainingString, tmpValue);
        else if (firstToken.equals("PropertySortFilter")) allPropertySortProperties.setProperty(remainingString, tmpValue);
        else if (DEBUG) System.out.println("This key is not known: "+firstToken);
      }
    }
    // This area constructs the Info Objects for this source and adds them to the
    // specific Master collection.
    buildIntensityFilters(allIntensityProperties, targetMasterCollection, sourceFile);
    buildPropertySortFilters(allPropertySortProperties, targetMasterCollection, sourceFile);
  }


  protected void commitChangesToSourceObjects() {
    // First remove the unwanteds.  Only need to check top of hierarchy on down.
    // If the object is found in the user map, bingo.
    super.commitChangesToSourceObjects();
    handleDirtyOrUnknownInfoObjects(intensityCollection);
    handleDirtyOrUnknownInfoObjects(propertySortCollection);
    resetWorkingCollections();
  }


  /**
   * Clears the Working collections of Info Objects and rebuilds from the
   * Master collections; merging them into their usable state.
   * Clear out specific InfoObject collection(s) before calling super.
   */
  public void resetWorkingCollections() {
    intensityCollection = new TreeMap(new MyStringComparator());
    propertySortCollection = new TreeMap(new MyStringComparator());
    //  Call the superclass which will merge the collections.
    super.resetWorkingCollections();
  }


  /**
   * Builds the ColorIntensityInfo objects for the given Master collection.
   */
  private void buildIntensityFilters(Properties intensityProperties, Map targetMasterCollection, String sourceFile) {
    Set uniqueIntensityProps = getUniqueKeys(intensityProperties);
    for (Iterator it = uniqueIntensityProps.iterator();it.hasNext();) {
      String nameBase = new String((String)it.next());
      Properties tmpProperties = new Properties();
      for (Iterator it2 = intensityProperties.keySet().iterator();it2.hasNext();) {
        String tmpKey = (String)it2.next();
        if (tmpKey.startsWith(nameBase)) {
          tmpProperties.put(tmpKey, intensityProperties.get(tmpKey));
        }
      }
      ColorIntensityInfo tmpIntensityInfo = new ColorIntensityInfo(nameBase, tmpProperties, sourceFile);
      targetMasterCollection.put("ColorIntensityFilter."+nameBase, tmpIntensityInfo);
    }
  }


  /**
   * Builds the PropertySortInfo objects for the given Master collection.
   */
  private void buildPropertySortFilters(Properties sortProperties, Map targetMasterCollection, String sourceFile) {
    Set uniqueSortFilters = getUniqueKeys(sortProperties);
    for (Iterator it = uniqueSortFilters.iterator();it.hasNext();) {
      String nameBase = new String((String)it.next());
      PropertySortInfo tmpSortInfo = new PropertySortInfo(nameBase, sortProperties, sourceFile);
      targetMasterCollection.put("PropertySortFilter."+nameBase, tmpSortInfo);
    }
  }


  public void firePropertySortFiltersChanged() {
   if (listeners!=null) {
     for (Iterator i=listeners.iterator();i.hasNext(); ){
        ((DisplayFilterListener)i.next()).propertySortFiltersChanged();
     }
   }
  }


  public void fireColorIntensityFiltersChanged() {
   if (listeners!=null) {
     for (Iterator i=listeners.iterator();i.hasNext(); ){
        ((DisplayFilterListener)i.next()).colorIntensityFiltersChanged();
     }
   }
  }


  /**
   * Give the collection of ColorIntensityInfo Objects to those who need them.
   */
  public TreeMap getColorIntensityFilters() {
    return intensityCollection;
  }

  /**
   * Give the collection of PropertySort Objects to those who need them.
   */
  public TreeMap getPropertySortFilters() {
    return propertySortCollection;
  }


  private class MySessionModelListener implements SessionModelListener {
    public void browserAdded(BrowserModel browserModel) {}
    public void browserRemoved(BrowserModel browserModel){}
    /**
     * This method first commits any changes to be safe, and then sets all dirty
     * infos to the selected writeback file.  Then for each selection in the
     * writebackFileCollection (sources that have had thier infos changed)
     * the method calls writeOutToDestinationFile.
     */
    public void sessionWillExit() {
      closeOutCurrentUserFile();
    }
    public void modelPropertyChanged(Object key, Object oldValue, Object newValue){
      if (key.equals(GROUP_DIR)) {
        String groupDirectory = "";
        if (newValue !=null) groupDirectory = (String)newValue + File.separator;
        // Make the change, flush and reset the preferences.
        setGroupPreferenceDirectory(groupDirectory);
        firePreferencesChangedEvent();
      }
    }
  }
}