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
import java.util.*;


public class PropertySortInfo extends DisplayFilterInfo {

  public static final int ASCENDING            = 0;
  public static final int DESCENDING           = 1;
  public static final String DISPLAY_STATE        = "DisplayState";

  private int displayState = 0;

  public PropertySortInfo(String keyBase, Properties inputProperties, String sourceFile) {
    super(keyBase, inputProperties, sourceFile);
    String tmpString = new String("");

    tmpString = (String)inputProperties.getProperty(keyBase+"."+DISPLAY_STATE);
    if (tmpString!=null) displayState=Integer.parseInt(tmpString);
    else displayState=ASCENDING;
  }


  public PropertySortInfo(String keyBase, String name, String sourceFile,
    String targetProperty, ArrayList effectedFGs, int displayState) {
    super(name, targetProperty, effectedFGs);
    this.keyBase = keyBase;
    this.sourceFile = sourceFile;
    this.displayState=displayState;
  }


  public int getDisplayState() { return displayState; }

  void setDisplayState(int displayState) {
    this.displayState=displayState;
  }



  public boolean equals(PropertySortInfo testInfo) {
    if (testInfo==null) return false;

    if (testInfo.getDisplayState()==displayState        &&
        testInfo.getEffectedFGs().equals(effectedFGs)     &&
        testInfo.getName().equals(name)                 &&
        testInfo.getTargetProperty().equals(targetProperty)) {
          // Need to check the datapoints separately.

          return true;
    }
    else return false;
  }


  public String getKeyName() { return "PropertySortFilter."+keyBase; }

  public Object clone(){
    return new PropertySortInfo(this.keyBase, this.name, this.sourceFile, targetProperty,
    (ArrayList)effectedFGs.clone(), displayState);
  }

  public Properties getPropertyOutput() {
    Properties outputProperties=new Properties();
    Properties superProperties = super.getPropertyOutput();
    for (Iterator it = superProperties.keySet().iterator(); it.hasNext(); ) {
      Object tmpKey = it.next();
      outputProperties.put(tmpKey, superProperties.get(tmpKey));
    }
    String key = getKeyName()+".";

    outputProperties.put(key+DISPLAY_STATE, Integer.toString(displayState));

    return outputProperties;
  }
}