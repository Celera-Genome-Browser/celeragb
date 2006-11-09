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

import java.awt.Color;
import java.util.Properties;
import shared.preferences.*;

public class ColorInfo extends InfoObject {
  private Color color;

  /**
   * This is the constructor for ColorInfo's that do not come from a property file.
   */
  public ColorInfo(String name, Color color) {
    this.name=name;
    this.color=color;
    this.keyBase=PreferenceManager.getKeyForName(name,true);
  }

  // This constructor should only be used for the clone.
  private ColorInfo(String keyBase, String name, Color color, String sourceFile){
    this.keyBase=keyBase;
    this.color=color;
    this.name=name;
    this.sourceFile=sourceFile;
  }

  public ColorInfo(String keyBase, Properties inputProperties, String sourceFile) {
      this.keyBase=keyBase;
      this.sourceFile = sourceFile;
      int red, green, blue;
      name = (String)inputProperties.getProperty(keyBase+".Name");
      if (name==null) name="Unknown";

      try {
        red = Integer.parseInt((String)inputProperties.getProperty(keyBase+".Red"));
        if (red<0 || red>255) red=0;
      }
      catch (Exception ex) { red=0; }

      try {
        green = Integer.parseInt((String)inputProperties.getProperty(keyBase+".Green"));
        if (green<0 || green>255) green=0;
      }
      catch (Exception ex) { green=0; }

      try {
        blue = Integer.parseInt((String)inputProperties.getProperty(keyBase+".Blue"));
        if (blue<0 || blue>255) blue=0;
      }
      catch (Exception ex) { blue=0; }

      color = new Color(red, green, blue);
  }

  public Color getColor() { return color; }
  public String getKeyName() { return "Color."+keyBase; }

  public Color getTextColor() {
    int r = color.getRed();
    int g = color.getGreen();
    int b = color.getBlue();
    if((r + (g*2) + b)>500) {
        return Color.black;
    } else {
        return Color.white;
    }
  }

  public Properties getPropertyOutput() {
    Properties outputProperties=new Properties();
    String key = getKeyName()+".";
    outputProperties.put(key+"Name",name);
    outputProperties.put(key+"Red",Integer.toString(color.getRed()));
    outputProperties.put(key+"Green",Integer.toString(color.getGreen()));
    outputProperties.put(key+"Blue",Integer.toString(color.getBlue()));
    return outputProperties;
  }

  void setColor(Color color) {
    isDirty=true;
    this.color = color;
  }

  public Object clone() {
    ColorInfo newColorInfo = new ColorInfo(new String(this.keyBase), new String(this.name),
      new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue()),
      new String(this.sourceFile));
    return newColorInfo;
  }

  public String toString() { return name; }
}