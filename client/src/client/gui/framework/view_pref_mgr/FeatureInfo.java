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

import java.util.Properties;

public class FeatureInfo extends InfoObject {
  // Default Feature Group
  private String FG_DEFAULT_COLOR                 ="Gray";
  private int FG_DEFAULT_HEIGHT                   =10;
  private String FG_DEFAULT_TIER                  ="Misc";

  private String featureColor;
  private int featureHeight=10;


 /**
  * This is the constructor for FeatureInfo's that do not come from a property file.
  */
  public FeatureInfo(String name, String color) {
    this.name=name;
    if (color==null || color.equals("")) featureColor=FG_DEFAULT_COLOR;
    else featureColor=color;
    featureHeight = FG_DEFAULT_HEIGHT;
    this.keyBase=PreferenceManager.getKeyForName(name,true);
  }

  // This constructor should only be used for the clone.
  private FeatureInfo(String keyBase, String name, int featureHeight,
    String featureColor, String sourceFile) {
    this.keyBase=keyBase;
    this.name=name;
    this.featureHeight=featureHeight;
    this.featureColor=featureColor;
    this.sourceFile=sourceFile;
  }

  public FeatureInfo(String keyBase, Properties inputProperties, String sourceFile) {
    this.keyBase=keyBase;
    this.sourceFile = sourceFile;
    String tmpString = new String("");

    tmpString = (String)inputProperties.getProperty(keyBase+".Name");
    if (tmpString!=null) name=tmpString;
    else name="Unknown";

    tmpString = (String)inputProperties.getProperty(keyBase+".Color");
    if (tmpString!=null) featureColor=tmpString;
    else featureColor=FG_DEFAULT_COLOR;

    tmpString = (String)inputProperties.getProperty(keyBase+".Height");
    if (tmpString!=null) featureHeight = Integer.parseInt(tmpString);
    else featureHeight = FG_DEFAULT_HEIGHT;
  }

  public String getKeyName() { return "FG."+keyBase; }

  public String getFeatureColor()    { return featureColor; }
  void setFeatureColor(String featureColor) {
    isDirty=true;
    this.featureColor = featureColor;
  }

  public int getFeatureHeight()             { return featureHeight; }
  void setFeatureHeight(int featureHeight)  {
    isDirty=true;
    this.featureHeight = featureHeight;
  }


  public Properties getPropertyOutput() {
    Properties outputProperties=new Properties();
    String key = getKeyName()+".";
    outputProperties.put(key+"Name",name);
    if (!featureColor.equals(FG_DEFAULT_COLOR)) outputProperties.put(key+"Color",featureColor);
    if (featureHeight!=FG_DEFAULT_HEIGHT) outputProperties.put(key+"Height",Integer.toString(featureHeight));
    return outputProperties;
  }


  public Object clone() {
    FeatureInfo newFeatureInfo = new FeatureInfo(new String(this.keyBase),
      new String(this.name), featureHeight, new String(this.featureColor),
      new String(this.sourceFile));
    return newFeatureInfo;
  }

}
