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
package client.gui.framework.navigation_tools;

import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.framework.session_mgr.SessionModelListener;
import shared.preferences.InfoObject;
import shared.preferences.PreferenceManager;

import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class SequenceAnalysisMgr extends PreferenceManager {

  private static SequenceAnalysisMgr analysisMgr;
  /**
   * The AnalysisType Info object is not meant to be in the master collection.
   * AnalysisType Infos are used to define sequence analysis types, but are not
   * defined in any way by the user; therefore, I have placed them in a separate
   * collection.
   */
  private TreeMap analysisTypeCollection = new TreeMap();
  private TreeMap sequenceAnalysisCollection = new TreeMap();

  static {
    SequenceAnalysisMgr.getSequenceAnalysisMgr();
  }

  private SequenceAnalysisMgr() {
    super();
    DEBUG = false;
    userFileDescription = "Genome Browser Sequence Analysis Definitions";
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
    this.filenameFilter  = "_SequenceAnalysis.properties";
    this.defaultFilename = "/"+System.getProperty("x.genomebrowser.SequenceAnalysisSettings");

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

    this.userFilename = userDirectory + "User_SequenceAnalysis.properties";
  }

  public static SequenceAnalysisMgr getSequenceAnalysisMgr() {
    if (analysisMgr==null) analysisMgr=new SequenceAnalysisMgr();
    return analysisMgr;
  }

  public TreeMap getSequenceAnalysisInfos() {
     return sequenceAnalysisCollection;
  }

  public TreeMap getMacrosForAnalysisType(String targetType) {
    TreeMap tmpMap = new TreeMap();
    for (Iterator it = sequenceAnalysisCollection.keySet().iterator(); it.hasNext(); ) {
      SequenceAnalysisInfo tmpInfo = (SequenceAnalysisInfo) sequenceAnalysisCollection.get(it.next());
      if (tmpInfo.getSequenceAnalysisType().equals(targetType)) {
        tmpMap.put(tmpInfo.getName(), tmpInfo);
      }
    }
    return tmpMap;
  }


  public SequenceAnalysisInfo getMacrosByName(String targetType) {
    for (Iterator it = sequenceAnalysisCollection.keySet().iterator(); it.hasNext(); ) {
      SequenceAnalysisInfo tmpInfo = (SequenceAnalysisInfo) sequenceAnalysisCollection.get(it.next());
      if (tmpInfo.getName().equals(targetType)) {
        return tmpInfo;
      }
    }
    return null;
  }


  public TreeMap getAnalysisTypes() { return analysisTypeCollection; }
  public AnalysisType getAnalysisType(String analysisTypeName) {
    return (AnalysisType)analysisTypeCollection.get(analysisTypeName);
  }

  public void addSequenceAnalysisInfo(SequenceAnalysisInfo analysisInfo) {
      sequenceAnalysisCollection.put(analysisInfo.getName(),analysisInfo);
      fireSequenceAnalysisObjectChanged();
  }

  public void deleteSequenceAnalysisInfo(SequenceAnalysisInfo analysisInfo) {
    String tmpName = analysisInfo.getName();
    deletedInfos.put(analysisInfo.getKeyName(), analysisInfo);
    sequenceAnalysisCollection.remove(tmpName);
  }

  /**
   * @todo Fill out this method for the Sequence Analysis mechanism.
   */
  public void handleDefaultKeyOverrideRequest() {}

  protected void handleOutputWriteError(){
    /**
     * @todo need to figure out what to do with write errors.
     */
  }

  public void fireSequenceAnalysisObjectChanged() {
   if (listeners!=null) {
     for (Iterator i=listeners.iterator();i.hasNext(); ){
      ((SequenceAnalysisListener)i.next()).analysisObjectChanged();
     }
   }
  }


  /**
   * This method will send all of its proprietary collections to the base class method
   * formatOutput(), one-by-one.
   */
  protected void writeOutAllCollections(FileWriter writer, String destinationFile) {
    addCollectionToOutput(writer, sequenceAnalysisCollection, destinationFile);
  }


  /**
   * This method is supposed to hierarchially add info objects to the final
   * collections.  Any deletion should be a cause for remerging; for example, if
   * a user defined info object is removed it could be replaced with a group
   * or default object.
   */
  protected void mergeIntoWorkingCollections(Map targetMasterCollection) {
    for (Iterator it = targetMasterCollection.keySet().iterator();it.hasNext();) {
      InfoObject tmpObject = (InfoObject)((InfoObject)targetMasterCollection.get((String)it.next())).clone();
      if (tmpObject instanceof SequenceAnalysisInfo) {
        sequenceAnalysisCollection.put(tmpObject.getName(), tmpObject);
      }
    }
  }


  /**
   * This method sifts through the Java property keys and puts them in sub-groups by
   * type.  It then passes those Properties objects
   * on to the build methods.  Remember this is for only one source,
   * Default or User, at a time.
   */
  protected void populateInfoObjects(Properties allProperties, Map targetMasterCollection,
                                   String sourceFile) {
    Properties allSequenceAnalysisProperties=new Properties();
    Properties allAnalysisTypeProperties = new Properties();

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
        if (firstToken.equals("SequenceAnalysis")) allSequenceAnalysisProperties.setProperty(remainingString, tmpValue);
        else if (firstToken.equals("AnalysisType")) allAnalysisTypeProperties.setProperty(remainingString, tmpValue);
        else if (DEBUG) System.out.println("This key is not known: "+firstToken);
      }
    }
    // This area constructs the Info Objects for this source and adds them to the
    // specific Master collection.
    buildAnalysisTypes(allAnalysisTypeProperties, targetMasterCollection, sourceFile);
    buildSequenceAnalysisInfos(allSequenceAnalysisProperties, targetMasterCollection, sourceFile);
  }


   /**
    * @todo Need to give the base class an abstract method to make
    * this work properly.
    */
  protected void commitChangesToSourceObjects() {
    // First remove the unwanteds.  Only need to check top of hierarchy on down.
    // If the object is found in the user map, bingo.
    super.commitChangesToSourceObjects();
    handleDirtyOrUnknownInfoObjects(sequenceAnalysisCollection);
    resetWorkingCollections();
  }


  /**
   * Clears the Working collections of Info Objects and rebuilds from the
   * Master collections; merging them into their usable state.
   * Clear out specific InfoObject collection(s) before calling super.
   */
  public void resetWorkingCollections() {
    sequenceAnalysisCollection = new TreeMap(new MyStringComparator());
    //  Call the superclass which will merge the collections.
    super.resetWorkingCollections();
  }


  /**
   * Builds the AnalysisTypeInfo objects and places them in a separate
   * collection.
   */
  private void buildAnalysisTypes(Properties analysisTypeProperties,
      Map targetMasterCollection, String sourceFile){
    Set uniqueAnalysisTypeProperties = getUniqueKeys(analysisTypeProperties);
    for (Iterator it = uniqueAnalysisTypeProperties.iterator();it.hasNext();) {
      String nameBase = (String)it.next();
      Properties tmpProperties = new Properties();
      for (Iterator it2 = analysisTypeProperties.keySet().iterator();it2.hasNext();) {
        String tmpKey = (String)it2.next();
        if (tmpKey.startsWith(nameBase)) {
          tmpProperties.put(tmpKey.substring(nameBase.length()+1), analysisTypeProperties.get(tmpKey));
        }
      }
      AnalysisType tmpAnalysisType = new AnalysisType(nameBase, tmpProperties);
      analysisTypeCollection.put(nameBase, tmpAnalysisType);
    }
  }


  /**
   * Builds the SequenceAnalysisInfo objects for the given Master collection.
   */
  private void buildSequenceAnalysisInfos(Properties analysisProperties, Map targetMasterCollection, String sourceFile) {
    Set uniqueAnalysisProperties = getUniqueKeys(analysisProperties);
    for (Iterator it = uniqueAnalysisProperties.iterator();it.hasNext();) {
      String nameBase = new String((String)it.next());
      Properties tmpProps = new Properties();
      for (Iterator it2 = analysisProperties.keySet().iterator(); it2.hasNext(); ) {
        String tmpKey = (String)it2.next();
        if (tmpKey.startsWith(nameBase)) {
          tmpProps.put(tmpKey, analysisProperties.getProperty(tmpKey));
        }
      }
      SequenceAnalysisInfo tmpSequenceAnalysisInfo = new SequenceAnalysisInfo(nameBase,
        tmpProps, sourceFile);
      targetMasterCollection.put("SequenceAnalysis."+nameBase, tmpSequenceAnalysisInfo);
    }
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
        fireSequenceAnalysisObjectChanged();
      }
    }
  }
}