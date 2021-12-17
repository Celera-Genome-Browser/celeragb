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

public class TierOrderInfo extends InfoObject {

  private String view = new String();
  private String tierNumber = new String("-1");

  public TierOrderInfo(String keyBase, String name, String tierNumber,
                       String view, String sourceFile) {
    this.keyBase=keyBase;
    this.name = name;
    this.view = view;
    this.tierNumber = tierNumber;
    this.sourceFile = sourceFile;
  }

  public String getKeyName() {
    return "View."+PreferenceManager.getKeyForName(view, true)+".Order."+ keyBase;
  }

  public String getViewName() { return view; }

  public int getTierNumber() { return Integer.parseInt(tierNumber); }
  void setTierNumber(int tierNumber) {
    isDirty=true;
    this.tierNumber=Integer.toString(tierNumber);
  }

  public Object clone() {
    TierOrderInfo newOrderInfo = new TierOrderInfo( new String(this.keyBase),
      new String(this.name), new String(this.tierNumber),
      new String(this.view), new String(this.sourceFile));
    return newOrderInfo;
  }

  public Properties getPropertyOutput() {
    Properties outputProperties=new Properties();
    String key = getKeyName();
    outputProperties.put(key, tierNumber);
    return outputProperties;
  }
}