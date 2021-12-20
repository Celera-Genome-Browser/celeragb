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
package client.gui.framework.view_pref_mgr;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

import shared.preferences.InfoObject;
import shared.preferences.PreferenceManager;

import java.util.Properties;

public class FeatureMapInfo extends InfoObject {

  private String view = new String();
  private String tierLocation = new String();

  public FeatureMapInfo(String keyBase, String name, String tierLocation,
                        String view, String sourceFile) {
      this.keyBase=keyBase;
      this.name = name;
      this.view = view;
      this.tierLocation = tierLocation;
      this.sourceFile = sourceFile;
  }

  public String getKeyName() {
    return "View."+PreferenceManager.getKeyForName(view, true)+".Map."+ keyBase;
  }

  public String getTierLocation() { return tierLocation; }
  void setTierLocation(String tierLocation) {
    isDirty = true;
    this.tierLocation = tierLocation;
  }

  public String getViewName() { return view; }

  public Object clone() {
    FeatureMapInfo newMapInfo = new FeatureMapInfo(new String(this.keyBase),
      new String(this.name), new String(this.tierLocation),
      new String(this.view), new String(this.sourceFile));
    return newMapInfo;
  }

  public Properties getPropertyOutput() {
    Properties outputProperties=new Properties();
    String key = getKeyName();
    outputProperties.put(key, tierLocation);
    return outputProperties;
  }

}