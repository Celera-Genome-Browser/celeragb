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

/**
 * Title:        Genome Browser<p>
 * Description:  <p>
 * @author Peter Davies
 * @version $Id$
 */

package client.gui.framework.view_pref_mgr;

import api.entity_model.model.annotation.CuratedFeature;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.assembly.Contig;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.GenomicEntity;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.framework.session_mgr.SessionModelListener;
import shared.preferences.InfoObject;
import shared.preferences.PreferenceManager;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.swing.JOptionPane;

public class ViewPrefMgr extends PreferenceManager {

  /**
   * Changing the keyname below and the expected default pref file is a way to force
   * a reset of the prefs while allowing forward compatibility.
   */
  public final static String VIEW_PREF_SAVE_FILE_PROPERTY = "VIEW_PREFERENCE_SAVE_FILE";
  public final static String VIEW_PREF_FILE_SUFFIX  = "_View.properties";
  private static ViewPrefMgr viewPrefMgr = new ViewPrefMgr();

  //  The collection below is used to link color to feature type.
  private TreeMap entityTypeColorMap = new TreeMap(new MyStringComparator());

  /**
   * These are the Working collections of Info Objects.  All these maps have String keys
   * that are the names of the Info Objects.  These keys are sorted in the
   * collections alphabetically, ignoring case.
   */
  /**
   * The four collections below use the regular Info Object name for the key,
   * with the Info Object being the value in the map.
   */
  private TreeMap colorCollection=new TreeMap();
  private TreeMap featureCollection=new TreeMap();
  private TreeMap tierCollection=new TreeMap();
  private TreeMap viewCollection=new TreeMap();
  /**
   * As these two collections are view-specific it is important to note that their
   * key is the KeyName for the InfoObject while the value is the object itself.
   */
  private TreeMap tierOrderCollection=new TreeMap();
  private TreeMap featureMappingCollection=new TreeMap();

  static {
    ViewPrefMgr.getViewPrefMgr();
  }

  private ViewPrefMgr() {
    super();
    DEBUG = false;
    // Set up the necessary attributes.
    setFileParameters();
    // Look for custom GB user-defined pref file.
    try {
       Object tmpObject = SessionMgr.getSessionMgr().getModelProperty(VIEW_PREF_SAVE_FILE_PROPERTY);
       String tmpFile = new String();
       if (tmpObject instanceof String)  {
        tmpFile = (String)tmpObject;
        SessionMgr.getSessionMgr().setModelProperty(VIEW_PREF_SAVE_FILE_PROPERTY, new File(tmpFile));
       }
       else if (tmpObject instanceof File) tmpFile = ((File)tmpObject).getAbsolutePath();
       else tmpFile=null;

       if (tmpFile!=null && (new File(tmpFile)).exists()) {
        userFilename = tmpFile;
       }
       else {
        SessionMgr.getSessionMgr().setModelProperty(VIEW_PREF_SAVE_FILE_PROPERTY, new File(userFilename));
       }
    }
    catch (Exception ex) { SessionMgr.getSessionMgr().handleException(ex); }

    // This listener is to hear when the client session is exiting and save.
    SessionMgr.getSessionMgr().addSessionModelListener(new MySessionModelListener());
    populateEntityTypeColors();
    initializeMasterInfoObjects();
    resetWorkingCollections();
  }


  /**
   * Inputs the required values to make the mechanism work.
   */
  public void setFileParameters() {
    this.filenameFilter  = VIEW_PREF_FILE_SUFFIX;
    this.defaultFilename = "/"+System.getProperty("x.genomebrowser.DefaultViewPrefs");

    // Establish the initial "default" user file.
    this.userDirectory = new String(System.getProperty("user.home")+File.separator+
      "x"+File.separator+"GenomeBrowser"+File.separator);

    this.userFilename = userDirectory + "Main_View.properties";
  }


  /**
   * This method loads a resource file that maps Entity Types to default colors.
   * If the Mgr does not know the color associated with a specific feature group
   * it will refer to the Entity Type of that feature and return the mapped value.
   */
  private void populateEntityTypeColors() {
    Properties targetProperties = new Properties();
    InputStream targetFile;
    try {
      targetFile = this.getClass().getResourceAsStream("/resource/client/EntityTypeColor.properties");
      if (DEBUG) System.out.println("Loading Preferences from EntityTypeColor.properties");
      targetProperties.load(targetFile);
    }
    catch (Exception ex) { SessionMgr.getSessionMgr().handleException(ex); }
    for (Enumeration enum=targetProperties.propertyNames();enum.hasMoreElements();) {
      String tempKey = new String ((String)enum.nextElement());
      StringTokenizer mainToken = new StringTokenizer(tempKey,".");
      if (tempKey!=null && mainToken!=null && tempKey!="") {
        String firstToken = new String (mainToken.nextToken());
        String tmpValue = (String)targetProperties.get(tempKey);
        String remainingString = tempKey.substring(firstToken.length()+1);
        if (firstToken.equals("EntityTypeColor")) entityTypeColorMap.put(remainingString, tmpValue);
        else if (DEBUG) System.out.println("This key is not known: "+firstToken);
      }
    }
  }


  /**
   * This method sifts through the Java property keys and puts them in sub-groups by
   * type: color, feature, tier, and view.  It then passes those Properties objects
   * on to the build methods.  Remember this is for only one source,
   * Default or User, at a time.
   */
  protected void populateInfoObjects(Properties allProperties, Map targetMasterCollection,
                                   String sourceFile) {
    Properties allColorProperties=new Properties();
    Properties allFeatureGroupProperties=new Properties();
    Properties allTierProperties=new Properties();
    Properties allViewProperties=new Properties();
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
        if (firstToken.equals("Color")) allColorProperties.setProperty(remainingString, tmpValue);
        else if (firstToken.equals("View")) allViewProperties.setProperty(remainingString, tmpValue);
        else if (firstToken.equals("Tier")) allTierProperties.setProperty(remainingString, tmpValue);
        else if (firstToken.equals("FG")) allFeatureGroupProperties.setProperty(remainingString, tmpValue);
        else if (DEBUG) System.out.println("This key is not known: "+firstToken);
      }
    }
    // This area constructs the Info Objects for this source and adds them to the
    // specific Master collection.
    buildColors(allColorProperties, targetMasterCollection, sourceFile);
    buildTiers(allTierProperties, targetMasterCollection, sourceFile);
    buildFeatureGroups(allFeatureGroupProperties, targetMasterCollection, sourceFile);
    buildViews(allViewProperties, targetMasterCollection, sourceFile);
  }


  /**
   * Builds the Color Info objects for the given Master collection.
   */
  private void buildColors(Properties colorProperties, Map targetMasterCollection, String sourceFile) {
    Set uniqueColors = getUniqueKeys(colorProperties);
    for (Iterator it = uniqueColors.iterator();it.hasNext();) {
      String nameBase = new String((String)it.next());
      ColorInfo tmpColorInfo = new ColorInfo(nameBase, colorProperties, sourceFile);
      targetMasterCollection.put("Color."+nameBase, tmpColorInfo);
    }
  }


  /**
   * Builds the Tier Info objects for the given Master collection.
   */
  private void buildTiers(Properties tierProperties, Map targetMasterCollection, String sourceFile) {
    Set uniqueTiers = getUniqueKeys(tierProperties);
    for (Iterator it=uniqueTiers.iterator();it.hasNext();) {
      String nameBase = new String((String)it.next());
      TierInfo tmpTierInfo = new TierInfo(nameBase, tierProperties, sourceFile);
      targetMasterCollection.put("Tier."+nameBase,tmpTierInfo);
    }
  }


  /**
   * Builds the Feature Info objects for the given Master collection.
   */
  private void buildFeatureGroups(Properties featureGroupProperties, Map targetMasterCollection, String sourceFile) {
    Set uniqueFeatureGroups = getUniqueKeys(featureGroupProperties);
    for (Iterator it=uniqueFeatureGroups.iterator();it.hasNext();) {
      String nameBase = new String((String)it.next());
      FeatureInfo tmpFeatureInfo = new FeatureInfo(nameBase, featureGroupProperties, sourceFile);
      targetMasterCollection.put("FG."+nameBase,tmpFeatureInfo);
    }
  }


  /**
   * Builds the View Info objects for the given Master collection.  This then passes
   * on the remaining view Properties to the method that will construct the specific
   * tierOrder and featureMapping Info Objects for this Master collection.
   */
  private void buildViews(Properties viewProperties, Map targetMasterCollection,
                          String sourceFile) {
    Set uniqueViews = getUniqueKeys(viewProperties);
    for (Iterator it=uniqueViews.iterator();it.hasNext();) {
      String viewName = (String)it.next();
      Properties tmpProperties = new Properties();
      for (Iterator it2 = viewProperties.keySet().iterator();it2.hasNext();) {
        String tmpKey = (String)it2.next();
        if (tmpKey.startsWith(viewName)) tmpProperties.put(tmpKey, viewProperties.get(tmpKey));
      }

      ViewInfo tmpViewInfo = (ViewInfo)targetMasterCollection.get("View."+viewName);
      if (tmpViewInfo==null) {
        if (DEBUG) System.out.println("  Adding "+viewName+" from "+sourceFile+" to targetMasterCollection.");
        String nameBase = new String(viewName);
        tmpViewInfo = new ViewInfo(nameBase, getKeyForName(viewName,false), sourceFile);
        targetMasterCollection.put("View."+nameBase,tmpViewInfo);
      }
      buildViewSpecificObjects(tmpProperties, targetMasterCollection, viewName, sourceFile);
    }
  }


  /**
   * Constructs the tierOrder or featureMapping Infos for the passed-in Master
   * collection.
   */
  private void buildViewSpecificObjects(Properties inputProperties, Map targetMasterCollection,
                          String viewName, String sourceFile) {
    viewName = getKeyForName(viewName, false);
    for (Iterator it=inputProperties.keySet().iterator();it.hasNext();) {
      String tmpKey = (String)it.next();
      String tmpValue = (String)inputProperties.get(tmpKey);
      StringTokenizer mainToken = new StringTokenizer(tmpKey,".");
      String firstToken = mainToken.nextToken();
      tmpKey = tmpKey.substring(firstToken.length()+1);
      if (tmpKey!=null && mainToken!=null && tmpKey!="") {
        String secondToken = mainToken.nextToken();
        String remainingString = tmpKey.substring(secondToken.length()+1);
        String tmpName = getKeyForName(remainingString, false);
        InfoObject tmpObject;
        if (secondToken.equalsIgnoreCase("Map"))  {
          tmpObject = new FeatureMapInfo(remainingString, tmpName, tmpValue, viewName, sourceFile);
        }
        else if (secondToken.equalsIgnoreCase("Order")) {
          tmpObject = new TierOrderInfo(remainingString, tmpName, tmpValue, viewName, sourceFile);
        }
        else tmpObject = null;
        targetMasterCollection.put(tmpObject.getKeyName(), tmpObject);
      }
    }
  }



  /**
   * Clears the Working collections of Info Objects and rebuilds from the
   * Master collections; merging them into their usable state.
   */
  public void resetWorkingCollections() {
    featureMappingCollection = new TreeMap(new MyStringComparator());
    tierOrderCollection = new TreeMap(new MyStringComparator());
    deletedInfos=new TreeMap(new MyStringComparator());
    colorCollection = new TreeMap(new MyStringComparator());
    featureCollection = new TreeMap(new MyStringComparator());
    tierCollection = new TreeMap(new MyStringComparator());
    viewCollection = new TreeMap(new MyStringComparator());
    //  Call the superclass which will merge the collections.
    super.resetWorkingCollections();
    assignViewSpecificObjectsToViews();
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
      if (tmpObject instanceof ColorInfo) {
        colorCollection.put(tmpObject.getName(), tmpObject);
      }
      else if (tmpObject instanceof TierInfo) {
        tierCollection.put(tmpObject.getName(), tmpObject);
      }
      else if (tmpObject instanceof FeatureInfo) {
        featureCollection.put(tmpObject.getName(), tmpObject);
      }
      else if (tmpObject instanceof ViewInfo) {
        viewCollection.put(tmpObject.getName(), tmpObject);
      }
      else if (tmpObject instanceof FeatureMapInfo) {
        featureMappingCollection.put(tmpObject.getKeyName(), tmpObject);
      }
      else if (tmpObject instanceof TierOrderInfo) {
        tierOrderCollection.put(tmpObject.getKeyName(), tmpObject);
      }
    }
  }


  /**
   * This method assigns the orders and mappings to their views.  This is the
   * last step of building the Working collections.
   */
  private void assignViewSpecificObjectsToViews() {
    for (Iterator it = viewCollection.keySet().iterator();it.hasNext();) {
      ViewInfo tmpViewInfo = (ViewInfo)viewCollection.get((String)it.next());
      String tmpName = tmpViewInfo.getName();
      for (Iterator itMap = featureMappingCollection.keySet().iterator();itMap.hasNext();) {
        FeatureMapInfo tmpMap = (FeatureMapInfo)featureMappingCollection.get((String)itMap.next());
        if (tmpName.equals(tmpMap.getViewName())) tmpViewInfo.addMapToCollections(tmpMap);
      }
      for (Iterator itOrder = tierOrderCollection.keySet().iterator();itOrder.hasNext();) {
        TierOrderInfo tmpOrder = (TierOrderInfo)tierOrderCollection.get((String)itOrder.next());
        if (tmpName.equals(tmpOrder.getViewName())) tmpViewInfo.addOrderToCollections(tmpOrder);
      }
    }
  }


  /**
   * This area begins methods (actions) that act upon info objects.  Developers
   * should have all changes to view objects go through here to determine which
   * changes should fire off a working collection reset or the firing of a preferences
   * changed event.
   */

  /**
   * Returns the Color object that is associated with this name.  If the name
   * is not known it returns white.
   */
  public Color getColor(String colorName) {
    if (colorName==null) {
      if (DEBUG) System.out.println("Needed color is null!");
      return Color.white;
    }
    if (colorCollection.get(colorName)!=null) {
      return ((ColorInfo)colorCollection.get(colorName)).getColor();
    }
    else {
      if (DEBUG) System.out.println("Color "+colorName+" is unknown.");
      return Color.white;
    }
  }


  /**
   * Returns the color used to represent the feature group (or environment) of
   * the genomic entity passed.
   */
  public Color getColorForEntity(GenomicEntity targetEntity) {
    if (!(targetEntity instanceof GenomicEntity)) {
      if (DEBUG) System.out.println("Must be a Genomic Entity!");
      return Color.white;
    }

    String tmpEnvironment= new String();
    if (targetEntity instanceof Feature) {
      tmpEnvironment = ((Feature)targetEntity).getEnvironment();
      EntityType entityType = targetEntity.getEntityType();

      //special cases
      if (targetEntity instanceof CuratedFeature) {
        if (((CuratedFeature)targetEntity).isObsoletedByWorkspace()) return Color.lightGray;
      }

      if (entityType.value() == EntityType.getEntityTypeForValue(EntityTypeConstants.StopCodon).value()) {
        return Color.red;
      }
      if (entityType.value() == EntityType.getEntityTypeForValue(EntityTypeConstants.Start_Codon_Start_Position).value()) {
        return Color.green;
      }
      if (entityType.value() == EntityType.getEntityTypeForValue(EntityTypeConstants.Translation_Start_Position).value()) {
        return Color.orange;
      }

      if (tmpEnvironment==null) {
        if (DEBUG) System.out.println("Environment is null for "+targetEntity.toString());
        tmpEnvironment="Misc";
      }
    }
    else if (targetEntity instanceof Contig) {
      tmpEnvironment="Contig";
    }
    else tmpEnvironment="Misc";

    FeatureInfo tmpInfo = (FeatureInfo)featureCollection.get(tmpEnvironment);
    if (tmpInfo==null) {
      if (DEBUG) System.out.println("There is no FeatureInfo for "+tmpEnvironment);
      return Color.white;
    }
    String colorForTarget = tmpInfo.getFeatureColor();
    return getColor(colorForTarget);
  }


  /**
   * Method returns the int which represents the order the tier should be placed
   * in the view. The axis is zero and numbers increase away from the axis.
   */
  public int getTierOrderValue(String viewName, String tierName) {
    ViewInfo tmpViewInfo = (ViewInfo)viewCollection.get(viewName);
    if (tmpViewInfo!=null) return tmpViewInfo.getOrderValueForTier(tierName);
    else {
      if (DEBUG) System.out.println("The view "+viewName+" does not exist.");
      return -1;
    }
  }


  /**
   * Relates the information of which tier a feature glyph should be located.
   */
  public String getTierLocationForFeature(String viewName, String targetFeatureName) {
    if (targetFeatureName == null) return null;
    ViewInfo tmpViewInfo = (ViewInfo)viewCollection.get(viewName);
    if (tmpViewInfo!=null) {
      String tmpTierInfo = tmpViewInfo.getFeatureTier(targetFeatureName);
      if (tmpTierInfo!=null) return tmpTierInfo;
      else return null;
    }
    else {
      if (DEBUG) System.out.println("The view "+viewName+" does not exist.");
      return null;
    }
  }


  /**
   * Returns the object that contains the settings that should be used when a
   * specific tier glyph is created.
   */
  public TierInfo getTierInfo(String tierName) {
    if (tierCollection.get(tierName)!=null) return (TierInfo)tierCollection.get(tierName);
    else {
      if (DEBUG) System.out.println("That TierInfo "+tierName+" does not exist.");
      return null;
    }
  }


  /**
   * Returns the object that contains the settings that should be used when a
   * specific feature glyph is created.
   */
  public FeatureInfo getFeatureInfo(String featureName) {
    if (featureName == null) return null;
    if (featureCollection.get(featureName)!=null)
      return (FeatureInfo)featureCollection.get(featureName);
    else {
      if (DEBUG) System.out.println("That FeatureInfo "+featureName+" does not exist.");
      return null;
    }
  }


  /**
   * Returns the object that contains the settings that should be used when a
   * specific view is created. This object also holds the tier orderings and
   * feature mappings.
   */
  public ViewInfo getViewInfo(String viewName) {
    if (viewCollection.get(viewName)!=null) return (ViewInfo)viewCollection.get(viewName);
    else {
      if (DEBUG) System.out.println("That ViewInfo "+viewName+" does not exist.");
      return null;
    }
  }


  /**
   * Remove the tier from the tierCollection.  Change feature mappings to Misc that
   * were dependent upon this exiting tier.  Remove any tier order objects that
   * views had for this tier.  Commit the changes to the Master collection and
   * notify everyone of the changes.
   */
  public void deleteTier(String tierName) {

    InfoObject tmpObject = (InfoObject)tierCollection.get(tierName);
    deletedInfos.put(tmpObject.getKeyName(), tmpObject);
    tierCollection.remove(tierName);
    for (Iterator it=viewCollection.keySet().iterator();it.hasNext();) {
      ViewInfo tmpViewInfo = (ViewInfo)viewCollection.get(it.next());
      tmpViewInfo.removeOrderFromCollections(tierName);
    }
  }


  /**
   * Removes the tier order object from the Working collection, commits, and
   * notifies all of the change.
   */
  public void deleteTierOrder(String orderName) {
    //Put this guy in the deleted collection.
    InfoObject tmpObject = (InfoObject)tierOrderCollection.get(orderName);
    if (tmpObject == null) return;
    deletedInfos.put(tmpObject.getKeyName(), tmpObject);
    tierOrderCollection.remove(orderName);
  }


  /**
   * Removes the Color Info object from the Working collection.  Goes through
   * the tiers and features and changes anyone using this color to the default
   * color.  Commits and notifies.
   */
  public void deleteColor(String colorName) {
    //Put this guy in the deleted collection.
    InfoObject tmpObject = (InfoObject)colorCollection.get(colorName);
    deletedInfos.put(tmpObject.getKeyName(), tmpObject);
    colorCollection.remove(colorName);
    for (Iterator it2 = tierCollection.keySet().iterator();it2.hasNext();){
      TierInfo tmpTierInfo = (TierInfo)tierCollection.get(it2.next());
      if (tmpTierInfo.getBackgroundColor().equals(colorName))
        tmpTierInfo.setBackgroundColor("Gray");
      }
    for (Iterator it2 = featureCollection.keySet().iterator();it2.hasNext();){
      FeatureInfo tmpFeatureInfo = (FeatureInfo)featureCollection.get(it2.next());
      if (tmpFeatureInfo.getFeatureColor().equals(colorName))
        tmpFeatureInfo.setFeatureColor("Gray");
    }
  }


  /**
   * Removes the feature info from the Working collection and goes through the
   * views removing the mappings for this feature.  Commits and notifies others.
   */
  public void deleteFeature(String featureName) {
    InfoObject tmpObject = (InfoObject)featureCollection.get(featureName);
    deletedInfos.put(tmpObject.getKeyName(), tmpObject);
    featureCollection.remove(featureName);
    for (Iterator it=viewCollection.keySet().iterator();it.hasNext();) {
      ViewInfo tmpViewInfo = (ViewInfo)viewCollection.get(it.next());
      tmpViewInfo.removeMappingsFromCollections(featureName);
    }
  }


  /**
   * Removes the feature mapping object from the Working collection, commits, and
   * notifies all of the change.
   */
  public void deleteFeatureMapping(String featureKeyName) {
    //Put this guy in the deleted collection.
    InfoObject tmpObject = (InfoObject)featureMappingCollection.get(featureKeyName);
    if (tmpObject==null) return;
    deletedInfos.put(tmpObject.getKeyName(), tmpObject);
    featureMappingCollection.remove(tmpObject.getKeyName());
  }


  /**
   * Clone the tier, remove the Working info with the old name, rename the clone,
   * go through the Views and rename the tier in ordering and feature collections.
   */
  public void setTierName(TierInfo tmpInfo, String newName) {
    String oldName = tmpInfo.getName();
    TierInfo newTierInfo = (TierInfo)tmpInfo.clone();
    deletedInfos.put(oldName, tierCollection.get(oldName));
    tierCollection.remove(oldName);
    newTierInfo.setName(newName);
    tierCollection.put(newTierInfo.getName(), newTierInfo);
    for (Iterator it2=viewCollection.keySet().iterator();it2.hasNext();) {
      ViewInfo tmpViewInfo = (ViewInfo)viewCollection.get(it2.next());
      tmpViewInfo.tierRenamed(oldName, newName);
    }
    //  Commit and Notification happens in the panels that use this method.
  }


  /**
   * Clone the color info, remove the original, rename the clone.  Look through
   * the tier and feature Working collections and update anyone who used to use
   * this color.  Point them to the new name.
   */
  public void setColorName(ColorInfo tmpInfo, String newName) {
    String key = tmpInfo.getName();
    ColorInfo newColorInfo = (ColorInfo)tmpInfo.clone();
    deletedInfos.put(key, colorCollection.get(key));
    colorCollection.remove(key);
    newColorInfo.setName(newName);
    for (Iterator it2 = tierCollection.keySet().iterator();it2.hasNext();){
      TierInfo tmpTierInfo = (TierInfo)tierCollection.get(it2.next());
      if (tmpTierInfo.getBackgroundColor().equals(key))
        tmpTierInfo.setBackgroundColor(newName);
    }
    for (Iterator it2 = featureCollection.keySet().iterator();it2.hasNext();){
      FeatureInfo tmpFeatureInfo = (FeatureInfo)featureCollection.get(it2.next());
      if (tmpFeatureInfo.getFeatureColor().equals(key))
        tmpFeatureInfo.setFeatureColor(newName);
    }
    colorCollection.put(newColorInfo.getName(), newColorInfo);
  }


  /**
   * Clone the feature info, remove the original, rename the clone, go through
   * the Views and rename the mappings to match.
   */
  public void setFeatureName(FeatureInfo tmpInfo, String newName) {
    String oldName = tmpInfo.getName();
    FeatureInfo newFeatureInfo = (FeatureInfo)tmpInfo.clone();
    deletedInfos.put(oldName, featureCollection.get(oldName));
    featureCollection.remove(oldName);
    newFeatureInfo.setName(newName);
    featureCollection.put(newFeatureInfo.getName(), newFeatureInfo);
    for (Iterator it2=viewCollection.keySet().iterator();it2.hasNext();) {
      ViewInfo tmpViewInfo = (ViewInfo)viewCollection.get(it2.next());
      tmpViewInfo.featureRenamed(oldName, newName);
    }
    //  Commit and Notification happens in the panels that use this method.
  }


  /**
  * This method is used to set the color of features.  Direct set access to the
  * Info Object is prohibited.
  */
  public void setFeatureColor(FeatureInfo targetInfo, String colorName) {
    targetInfo.setFeatureColor(colorName);
  }


  /**
   * This method is used to set the color that correspponds to a given name.
   * Direct set access to the Info Object is prohibited.
   */
  public void setColorForColorInfo(ColorInfo targetInfo, Color targetColor) {
    targetInfo.setColor(targetColor);
  }


  /**
   * This method is used to set the tier background color.  Direct set access
   * to the Info Object is prohibited.
   */
  public void setTierBackgroundColor(TierInfo targetInfo, String colorName) {
    targetInfo.setBackgroundColor(colorName);
  }


  /**
   * This method is used to toggle the hide when empty state of a tier.
   */
  public void setHideWhenEmptyForTier(TierInfo targetInfo, Boolean newBoolean) {
    targetInfo.setHideWhenEmpty(newBoolean);
  }

  public void fireTierAddedEvent(TierInfo info) {
    for (Iterator it=listeners.iterator();it.hasNext();) {
      ((ViewPrefMgrListener)it.next()).tierAdded(info);
    }
  }

  public void fireTierRemovedEvent(TierInfo info) {
    for (Iterator it=listeners.iterator();it.hasNext();) {
      ((ViewPrefMgrListener)it.next()).tierRemoved(info);
    }
  }

  public void fireTierStateChangeEvent(TierInfo info) {
    for (Iterator it=listeners.iterator();it.hasNext();) {
      ((ViewPrefMgrListener)it.next()).tierStateChanged(info);
    }
  }


  /**
   * This method changes the view preference file being used by the program.
   */
  public void setUserPreferenceFile(String newFilename, String oldDescription) {
    if (!newFilename.equals(userFilename)) {
      super.setUserPreferenceFile(newFilename, oldDescription);
      SessionMgr.getSessionMgr().setModelProperty(VIEW_PREF_SAVE_FILE_PROPERTY, new File(newFilename));
    }
  }


  /**
   * Adds a new color to the working collection.  The Color Panel will determine
   * if the commit and notification occur.
   */
  public ColorInfo createNewColor(String newName, Color newColor) {
    if (colorCollection.get(newName)!=null) {
      if (DEBUG) System.out.println("Tried to add a ColorInfo that already exists!!!");
      return (ColorInfo)colorCollection.get(newName);
    }
    else {
      ColorInfo newColorInfo = new ColorInfo(newName, newColor);
      colorCollection.put(newColorInfo.getName(), newColorInfo);
      return newColorInfo;
    }
  }


  /**
   * Create a new tier and add it to the Working collection.
   * Then create an order object for this tier in all views.
   * The Tier Panel or view will determine commit and notification.
   */
  public TierInfo createNewTier(String newName) {
    //  Test to see if Tier is already defined.
    if (tierCollection.get(newName)!=null) {
      if (DEBUG) System.out.println("Tried to add a TierInfo named "+newName+" that already exists!!!");
      return (TierInfo)tierCollection.get(newName);
    }
    else {
      TierInfo newTierInfo = new TierInfo(newName);
      tierCollection.put(newTierInfo.getName(), newTierInfo);
      // Give an order value for each view this tier could apply to.
      for(Iterator it=viewCollection.keySet().iterator();it.hasNext();) {
        ViewInfo tmpViewInfo = (ViewInfo)viewCollection.get(it.next());
        tmpViewInfo.createNewOrderDefinition(newName);
      }
      return newTierInfo;
    }
  }


  /**
   * Create a new Feature Info, add it to the Working collection, then create
   * a new mapping for this feature in each view.  The Entity Type is used to
   * determine the color that should be associated with the feature.
   */
  public FeatureInfo createNewFeature(String newName, String destinationTier, String entityType) {
    if (featureCollection.get(newName)!=null) {
      if (DEBUG) System.out.println("Tried to add a FeatureInfo that already exists!!!");
      return (FeatureInfo)featureCollection.get(newName);
    }
    else {
      String newColor = (String)entityTypeColorMap.get(entityType);
      FeatureInfo newFeatureInfo = new FeatureInfo(newName, newColor);
      featureCollection.put(newFeatureInfo.getName(), newFeatureInfo);
      // Define a mapping for the new feature the destination tier.
      for(Iterator it=viewCollection.keySet().iterator();it.hasNext();) {
        ViewInfo tmpViewInfo = (ViewInfo)viewCollection.get(it.next());
        tmpViewInfo.createNewMappingDefinition(newName, destinationTier);
      }
      return newFeatureInfo;
    }
  }


  /**
   * This method is used primarily by the views in dynamic feature and tier
   * definition.  The commit and notification is automatic only for this dynamic
   * situation.
   */
  public TierInfo createNewFeatureAndTier(String newName, String entityType) {
    TierInfo newTierInfo = createNewTier(newName);
    createNewFeature(newName, newName, entityType);
    commitChanges(true);
    fireTierAddedEvent(newTierInfo);
    return newTierInfo;
  }


  /**
   * Returns a list of TierInfo objects, ordered by their mapping value for
   * this view.  This assumes that every defined Tier Info has an Order Info.
   */
  public ArrayList getOrderedTierInfos(String viewName) {
    ArrayList tmpTierInfos = new ArrayList();
    ViewInfo viewInfo = (ViewInfo)viewCollection.get(viewName);
    ArrayList tierMap = viewInfo.getTierOrderByValue();
    for (Iterator it=tierMap.iterator();it.hasNext();) {
      String originalName = ((TierOrderInfo)it.next()).getName();
      TierInfo tmpInfo = (TierInfo)tierCollection.get(originalName);
      if (tmpInfo!=null) tmpTierInfos.add(tmpInfo);
    }
    return tmpTierInfos;
  }


  /**
   * Returns all the feature-to-tier mappings for the given tier.
   */
  public TreeMap getFeatureMapingsForView(String viewName){
    ViewInfo viewInfo = (ViewInfo)viewCollection.get(viewName);
    return viewInfo.getFeatureMappings();
  }

  public TreeMap getFeatureCollection() { return featureCollection; }
  public TreeMap getTierCollection() { return tierCollection; }
  public TreeMap getColorCollection() { return colorCollection; }
  public TreeMap getViewCollection() { return viewCollection; }
  TreeMap getFeatureMappingCollection() { return featureMappingCollection; }
  TreeMap getTierOrderCollection() { return tierOrderCollection; }


  public TreeMap getDefaultTierMap(){
    return defaultMasterCollection;
  }


  public static ViewPrefMgr getViewPrefMgr() { return viewPrefMgr; }

  /**
   * The FeaturePanel uses this method to toggle the tier that features get mapped
   * to.  Commit and notification gets enacted by the panel, if desired.
   */
  public void setFeatureMapForView(String viewName, String featureName, String tierName) {
    ViewInfo viewInfo = (ViewInfo)viewCollection.get(viewName);
    viewInfo.setMapForFeature(featureName,tierName);
  }


  /**
   * This method is used to toggle the view state of a given tier.  It is employed
   * by the TierPopupMenu, GA View, and TierPanel.
   */
  public void setTierState(String targetTier, int targetState) {
    TierInfo tI = getTierInfo(targetTier);
    tI.setState(targetState);
  }


  /**
   * This method is used to toggle the docking state of the tier to the centered static
   * region of a view.  It is used by GA View.
   */
  public void setTierDocked(TierInfo targetTier, boolean dockTier) {
    targetTier.setDocked(new Boolean(dockTier));
  }

  /**
   * This method sets off a reaction in the view to reorder the tiers for the
   * user.
   */
  public void swapTierOrder(String viewName, String oldTierOrderString, String newTierOrderString) {
    ViewInfo viewInfo = (ViewInfo)viewCollection.get(viewName);
    viewInfo.swapTierOrder(oldTierOrderString, newTierOrderString);
  }


  /**
   * This is the private method that does all the real committing work.
   * First, Info Objects added to the deletedInfo collection attempt to be
   * removed from the Master collection that has influence in the hierarchy.
   * Deletion checks uncommitted Info's first.  Then User->Default.
   * As it proceeds, it keeps track of which sourcefile will need to be written
   * back to because of deletion.  After this, it calls
   * handleDirtyOrUnknownInfoObjects() on the Working collections.
   * Once done, it resets the Working collections.
   */
   /**
    * @todo It could be that the deletion of default infos popup should not
    * happen as a row could be deleted in the Feature Panel; the map could be
    * deleted to the default setting while the feature itself could not get deleted.
    * The popup would be confusing.
    */
  protected void commitChangesToSourceObjects() {
    // First remove the unwanteds.  Only need to check top of hierarchy on down.
    // If the object is found in the user map, bingo.
    super.commitChangesToSourceObjects();

    handleDirtyOrUnknownInfoObjects(colorCollection);
    handleDirtyOrUnknownInfoObjects(tierCollection);
    handleDirtyOrUnknownInfoObjects(featureCollection);
    handleDirtyOrUnknownInfoObjects(tierOrderCollection);
    handleDirtyOrUnknownInfoObjects(featureMappingCollection);

    resetWorkingCollections();
  }



  /**
   * This method is overridden to notify the user that the default keys cannot be deleted.
   */
  public void handleDefaultKeyOverrideRequest() {
    JOptionPane.showMessageDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
      "Default view parameters cannot be modified in this manner.", "Attention!", JOptionPane.WARNING_MESSAGE);
  }


  protected void handleOutputWriteError() {
    /**
     * @todo Need to fill this in in case we need to alert user to a write error.
     * Maybe they tried to delete or change a default InfoObject.
     */
  }


  /**
   * This method is intended to be overridden.  This class will
   * send all of its proprietary collections to the base class method
   * formatOutput(), one-by-one.
   */
  protected void writeOutAllCollections(FileWriter writer, String destinationFile) {
    addCollectionToOutput(writer, colorCollection, destinationFile);
    addCollectionToOutput(writer, tierCollection, destinationFile);
    addCollectionToOutput(writer, featureCollection, destinationFile);
    addCollectionToOutput(writer, tierOrderCollection, destinationFile);
    addCollectionToOutput(writer, featureMappingCollection, destinationFile);
  }


//  private class MyFilenameFilter implements FilenameFilter {
//    public boolean accept(File dir, String name) {
//      if (name.endsWith(filenameFilter)) return true;
//      else return false;
//    }
//  }
//
//
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
    public void modelPropertyChanged(Object key, Object oldValue, Object newValue){}
  }
}