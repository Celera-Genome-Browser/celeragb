// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package client.gui.framework.view_pref_mgr;

import shared.preferences.InfoObject;
import shared.preferences.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class ViewInfo extends InfoObject{
  private TreeMap viewTierOrderInfos=new TreeMap(new MyStringComparator());
  private TreeMap viewFeatureMapInfos=new TreeMap(new MyStringComparator());

  private TreeMap featureToTierMapping=new TreeMap(new MyStringComparator());

  // In order to insert new tiers into the view.
  private int nextTierNumber=0;

  public ViewInfo(String keyBase, String name, String sourceFile) {
    this.keyBase=keyBase;
    this.sourceFile = sourceFile;
    this.name = name;
  }

  // This constructor should only be used for the clone.
  private ViewInfo(String name, TreeMap viewTierOrderInfos,
      TreeMap viewFeatureMapInfos, TreeMap featureToTierMapping,
      int nextTierNumber, String keyBase, String sourceFile) {
    this.name=name;
    this.viewTierOrderInfos=viewTierOrderInfos;
    this.viewFeatureMapInfos=viewFeatureMapInfos;
    this.featureToTierMapping=featureToTierMapping;
    this.nextTierNumber=nextTierNumber;
    this.keyBase=keyBase;
    this.sourceFile=sourceFile;
  }

  void addMapToCollections(FeatureMapInfo newInfo) {
    // Manage change impact on internal collections.
    viewFeatureMapInfos.put(newInfo.getName(), newInfo);
    featureToTierMapping.put(newInfo.getName(), newInfo.getTierLocation());
  }

  void addOrderToCollections(TierOrderInfo newInfo) {
    // Manage change impact on internal collections.
    viewTierOrderInfos.put(newInfo.getName(), newInfo);
    if (nextTierNumber<=newInfo.getTierNumber()) nextTierNumber=newInfo.getTierNumber()+1;
  }


  /**
   * @todo Do I really need to manage ALL the inner maps? Perhaps I only need to
   * keep up-to-date the Working featureMappingCollection. Have to see if I rebuild
   * everything inbetween, here.
   */
  void featureRenamed(String oldName, String newName) {
    String tmpValue = (String)featureToTierMapping.get(oldName);
    featureToTierMapping.put(newName, tmpValue);
    featureToTierMapping.remove(oldName);
    FeatureMapInfo oldMapping = (FeatureMapInfo)viewFeatureMapInfos.get(oldName);
    FeatureMapInfo newMapping = (FeatureMapInfo)((FeatureMapInfo)viewFeatureMapInfos.get(oldName)).clone();
    viewFeatureMapInfos.remove(oldName);
    newMapping.setName(newName);
    viewFeatureMapInfos.put(newName, newMapping);
    TreeMap tmpMapInfos = ViewPrefMgr.getViewPrefMgr().getFeatureMappingCollection();
    tmpMapInfos.put(newMapping.getKeyName(), newMapping);
    ViewPrefMgr.getViewPrefMgr().deleteFeatureMapping(oldMapping.getKeyName());
  }

  void tierRenamed(String oldTierName, String newTierName) {
    for (Iterator it=featureToTierMapping.keySet().iterator();it.hasNext();) {
      String featureName = (String)it.next();
      String tierName = (String)featureToTierMapping.get(featureName);
      if (tierName.equals(oldTierName)) {
        setMapForFeature(featureName, newTierName);
      }
    }
    TierOrderInfo oldOrderInfo = (TierOrderInfo)viewTierOrderInfos.get(oldTierName);
    TierOrderInfo newOrderInfo = (TierOrderInfo)((TierOrderInfo)viewTierOrderInfos.get(oldTierName)).clone();
    newOrderInfo.setName(newTierName);
    removeOrderFromCollections(oldTierName);
    addOrderToCollections(newOrderInfo);
    TreeMap tmpOrderInfos = ViewPrefMgr.getViewPrefMgr().getTierOrderCollection();
    tmpOrderInfos.put(newOrderInfo.getKeyName(), newOrderInfo);
    ViewPrefMgr.getViewPrefMgr().deleteTierOrder(oldOrderInfo.getKeyName());
  }

  void removeOrderFromCollections(String tierName) {
    InfoObject orderInfo = (InfoObject)viewTierOrderInfos.get(tierName);
    int removedTierNumber=((TierOrderInfo)orderInfo).getTierNumber();
    viewTierOrderInfos.remove(tierName);
    ViewPrefMgr.getViewPrefMgr().deleteTierOrder(orderInfo.getKeyName());
    packTierOrders(removedTierNumber);
    nextTierNumber--;
  }


  void removeMappingsFromCollections(String featureName) {
    InfoObject mapInfo = (InfoObject)viewFeatureMapInfos.get(featureName);
    viewFeatureMapInfos.remove(mapInfo.getName());
    featureToTierMapping.remove(mapInfo.getName());
    ViewPrefMgr.getViewPrefMgr().deleteFeatureMapping(mapInfo.getKeyName());
  }

  /**
   * ViewInfoObjects can only EVER be clean.  FeatureMapInfos and TierOrderInfos
   * can change and track their own cleanliness accordingly.  After all, users
   * cannot create their own views on the fly. (Yet?!) Overridden from base class.
   */
  public boolean hasChanged() { return false; }

  /**
   * This method is a no op, basically.  No one can rename a view.
   * Overridden from base class.
   */
  public void setName(String name)   { }

  public Object clone() {
    ViewInfo tmpInfo = new ViewInfo(new String(this.name),
      cloneOrderMap(viewTierOrderInfos),
      cloneFeatureMap(viewFeatureMapInfos),
      cloneStringMap(featureToTierMapping),
      nextTierNumber,
      new String(this.keyBase),
      new String(this.sourceFile));
    return tmpInfo;
  }

  /**
   * This method is designed to maintain the sequential ordering of tiers without
   * any gaps in the order allowed.
   */
  private void packTierOrders(int removedTierNumber) {
    for (Iterator it = viewTierOrderInfos.values().iterator();it.hasNext();) {
        TierOrderInfo tierOrderInfo = ( TierOrderInfo)it.next();
        if(tierOrderInfo.getTierNumber()> removedTierNumber){
          tierOrderInfo.setTierNumber(tierOrderInfo.getTierNumber()-1);
        }
  }
}

  private TreeMap cloneOrderMap(Map sourceMap) {
    TreeMap tmpMap = new TreeMap(new MyStringComparator());
    for (Iterator it = sourceMap.keySet().iterator();it.hasNext();) {
      String tmpKey = (String)it.next();
      tmpMap.put(new String(tmpKey), ((TierOrderInfo)sourceMap.get(tmpKey)).clone());
    }
    return tmpMap;
  }

  private TreeMap cloneFeatureMap(Map sourceMap) {
    TreeMap tmpMap = new TreeMap(new MyStringComparator());
    for (Iterator it = sourceMap.keySet().iterator();it.hasNext();) {
      String tmpKey = (String)it.next();
      tmpMap.put(new String(tmpKey), ((FeatureMapInfo)sourceMap.get(tmpKey)).clone());
    }
    return tmpMap;
  }

  private TreeMap cloneStringMap(Map sourceMap) {
    TreeMap tmpMap = new TreeMap(new MyStringComparator());
    for (Iterator it = sourceMap.keySet().iterator();it.hasNext();) {
      String tmpKey = (String)it.next();
      tmpMap.put(new String(tmpKey), new String((String)sourceMap.get(tmpKey)));
    }
    return tmpMap;
  }

  private TreeMap cloneValueMap(Map sourceMap) {
    TreeMap tmpMap = new TreeMap(new MyTierOrderComparator());
    for (Iterator it = sourceMap.keySet().iterator();it.hasNext();) {
      String tmpKey = (String)it.next();
      tmpMap.put(new String(tmpKey), new String((String)sourceMap.get(tmpKey)));
    }
    return tmpMap;
  }

  /**
   * This method is an overridden no op, basically.
   * No one can re-set the sourceFile of a view.
   */
  public void setSourceFile(String sourceFile) {}


  /**
   * @todo I think this method is only registering the changes because the setTierNumber
   * method is dirtying the order objects and since they are passed by reference
   * the view class they are modifying the same objects in viewTierOrderInfos as
   * in ViewPrefMgr tierOrderCollection.  This may not be good!
   */
  void swapTierOrder(String oldTierOrderString, String newTierOrderString) {
    if (oldTierOrderString.equals(newTierOrderString)) return;
    int startIndex,endIndex;
    String tmpTier=new String();
    int oldNumber = (new Integer(oldTierOrderString)).intValue();
    int newNumber = (new Integer(newTierOrderString)).intValue();
    if (oldNumber>=nextTierNumber || newNumber>=nextTierNumber) return;
    tmpTier=getTierForOrderValue(oldTierOrderString);
    startIndex=oldNumber;
    endIndex=newNumber;
    if (oldNumber<newNumber) {
      for (int index=startIndex+1; index <= endIndex; index++) {
        String value = Integer.toString(index);
        String name = getTierForOrderValue(value);
        ((TierOrderInfo)viewTierOrderInfos.get(name)).setTierNumber(index-1);
      }
      ((TierOrderInfo)viewTierOrderInfos.get(tmpTier)).setTierNumber(endIndex);
    }
    else {
      for (int index=startIndex-1; index >= endIndex; index--) {
        String value = Integer.toString(index);
        String name = getTierForOrderValue(value);
        ((TierOrderInfo)viewTierOrderInfos.get(name)).setTierNumber(index+1);
      }
      ((TierOrderInfo)viewTierOrderInfos.get(tmpTier)).setTierNumber(endIndex);
    }
  }

  void setMapForFeature(String featureName, String tierName) {
    ((FeatureMapInfo)viewFeatureMapInfos.get(featureName)).setTierLocation(tierName);
    featureToTierMapping.put(featureName, tierName);
  }

  public String getFeatureTier(String mapKey) {
    if (featureToTierMapping.containsKey(mapKey))
      return (String)featureToTierMapping.get(mapKey);
    else return null;
  }


  /**
   * View Infos should never get written out to a file; thus, no property output
   * is necessary.  What DO get output should be changes to FeatureMapInfos
   * and TierOrderInfos, which maintain their own "cleanliness".
   */
  public Properties getPropertyOutput() {
    return new Properties();
  }

  void createNewOrderDefinition(String newTierName) {
    Integer newInt = new Integer(nextTierNumber);
    TierOrderInfo tmpOrder = new TierOrderInfo(
      PreferenceManager.getKeyForName(newTierName, true),
      newTierName, newInt.toString(), name, "Unknown");
    TreeMap tmpOrderInfos = ViewPrefMgr.getViewPrefMgr().getTierOrderCollection();
    tmpOrderInfos.put(tmpOrder.getKeyName(), tmpOrder);
    addOrderToCollections(tmpOrder);
  }

  void createNewMappingDefinition(String newFeatureName, String destinationTier) {
    FeatureMapInfo newMap = new FeatureMapInfo(
      PreferenceManager.getKeyForName(newFeatureName, true),
      newFeatureName, destinationTier, name, "Unknown");
    TreeMap tmpMapInfos = ViewPrefMgr.getViewPrefMgr().getFeatureMappingCollection();
    tmpMapInfos.put(newMap.getKeyName(), newMap);
    addMapToCollections(newMap);
  }

  public String getTierForOrderValue(String targetValue) {
    int targetInt = Integer.parseInt(targetValue);
    for(Iterator it = viewTierOrderInfos.keySet().iterator(); it.hasNext();){
      TierOrderInfo tmpInfo = (TierOrderInfo)viewTierOrderInfos.get(it.next());
      if (tmpInfo.getTierNumber()==targetInt) return tmpInfo.getName();
    }
    return "Unknown";
  }

  public int getOrderValueForTier(String targetTier) {
    for(Iterator it = viewTierOrderInfos.keySet().iterator(); it.hasNext();){
      TierOrderInfo tmpInfo = (TierOrderInfo)viewTierOrderInfos.get(it.next());
      if (tmpInfo.getName().equals(targetTier)) return tmpInfo.getTierNumber();
    }
    return -1;
  }

  public ArrayList getTierOrderByValue() {
    ArrayList tmpList = new ArrayList();
    for (Iterator it = viewTierOrderInfos.keySet().iterator(); it.hasNext();) {
      tmpList.add(viewTierOrderInfos.get(it.next()));
    }
    Arrays.sort(tmpList.toArray(), new MyValueSortComparator());
    return tmpList;
  }

  public ArrayList getTierOrderByName() {
    ArrayList tmpList = new ArrayList();
    for (Iterator it = viewTierOrderInfos.keySet().iterator(); it.hasNext();) {
      tmpList.add(viewTierOrderInfos.get(it.next()));
    }
    Arrays.sort(tmpList.toArray(), new MyNameSortComparator());
    return tmpList;
  }

  public TreeMap getFeatureMappings() { return featureToTierMapping; }
  public TreeMap getTierOrderInfos(){ return viewTierOrderInfos;}
  public String getKeyName() { return keyBase; }

  private class MyTierOrderComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      int i1, i2;
      try {
        i1 = Integer.parseInt((String)o1);
        i2 = Integer.parseInt((String)o2);
        if (i1 == i2) return 0;
        if (i1 < i2) return -1;
      }
      catch (Exception ex) { return 0; }
      return 1;
    }
  }


  private class MyStringComparator implements Comparator {
    public int compare(Object key1, Object key2) {
      String keyName1, keyName2;
      try {
        keyName1 = (String)key1;
        keyName2 = (String)key2;
        if (keyName1==null || keyName2==null) return 0;
      }
      catch (Exception ex) { return 0; }
      return keyName1.compareToIgnoreCase(keyName2);
    }
  }

  private class MyValueSortComparator implements Comparator {
    public int compare(Object o1, Object o2) {
      int i1, i2;
      try {
        i1 = ((TierOrderInfo)o1).getTierNumber();
        i2 = ((TierOrderInfo)o2).getTierNumber();
        if (i1 == i2) return 0;
        if (i1 < i2) return -1;
      }
      catch (Exception ex) { return 0; }
      return 1;
    }
  }

  private class MyNameSortComparator implements Comparator {
    public int compare(Object key1, Object key2) {
      String keyName1, keyName2;
      try {
        keyName1 = ((TierOrderInfo)key1).getName();
        keyName2 = ((TierOrderInfo)key2).getName();
        if (keyName1==null || keyName2==null) return 0;
      }
      catch (Exception ex) { return 0; }
      return keyName1.compareToIgnoreCase(keyName2);
    }
  }
}

