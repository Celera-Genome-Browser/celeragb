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

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */
import java.util.Properties;
import java.util.*;

import shared.preferences.PreferenceManager;

public class AnalysisType {
  public static final String TEXT_FIELD_EDITOR      = "Text Field";
  public static final String STRING_COMBO_EDITOR    = "String Combo Box";
  public static final String NUMBER_COMBO_EDITOR    = "Number Combo Box";
  private static final String ANALYSIS_TYPE         = "AnalysisType";

  private TreeMap arguments = new TreeMap();
  private String analysisName  = "";

  public AnalysisType(String analysisName, Properties inputProperties) {
    this.analysisName = analysisName;
    formatArgumentCollection(inputProperties);
  }


  public String getName() { return analysisName; }
  public TreeMap getArguments() { return arguments; }

  /**
   * What the user sees for sequence analysis settings do not translate directly
   * to the command line.  Display Names translate to switches and displayed
   * values translate into abbreviated/real values.
   */
  public String getSwitchForArgumentDisplayName(String argumentDisplayName) {
    AnalysisProperty tmpProperty = (AnalysisProperty)arguments.get(argumentDisplayName);
    if (tmpProperty!=null) return tmpProperty.getSwitch();
    else return "";
  }

  public String getValueForValueDisplayName(String argumentDisplayName, String valueDisplayName) {
    AnalysisProperty tmpProperty = (AnalysisProperty)arguments.get(argumentDisplayName);
    if (tmpProperty==null) return "";
    if (tmpProperty.getEditor().equals(this.TEXT_FIELD_EDITOR)) return valueDisplayName;
    if (tmpProperty!=null) return tmpProperty.getValueForOption(valueDisplayName);
    else return "";
  }


  /**
   * This method should take all of the specific properties and place them
   * in the collection with their values.  The properties, destined for the command
   * line, must be unique by nature.
   */
  private void formatArgumentCollection(Properties inputProperties) {
    Set uniqueProperties = PreferenceManager.getUniqueKeys(inputProperties);
    for (Iterator it=uniqueProperties.iterator();it.hasNext();) {
      String tmpKey = (String)it.next();
      Properties subProps = new Properties();
      for (Iterator it2 = inputProperties.keySet().iterator(); it2.hasNext(); ) {
        String tmpNext = (String)it2.next();
        if (tmpNext.startsWith(tmpKey)) {
          String tmpValue = ((String)inputProperties.get(tmpNext)).trim();
          subProps.put(tmpNext, tmpValue);
        }
      }
      AnalysisProperty tmpProperty = new AnalysisProperty(tmpKey, subProps);
      arguments.put(PreferenceManager.getKeyForName(tmpKey, false), tmpProperty);
    }
  }


  public class AnalysisProperty {
    private static final String NAME            = "Name";
    private static final String SWITCH          = "Switch";
    private static final String DEFAULT         = "Default";
    private static final String OPTION          = "Option";
    private static final String EDITOR          = "Editor";
    private String name="";
    private TreeMap optionToDisplayNameMap = new TreeMap();
    private String defaultPropertyValue = "";
    private String propertySwitch = "";
    private String editor = "";

    public AnalysisProperty(String propName, Properties props) {
      String tmpString = new String("");

      tmpString = props.getProperty(propName+"."+NAME);
      if (tmpString!=null) name=tmpString;
      else name="Unknown";

      tmpString = props.getProperty(propName+"."+DEFAULT);
      if (tmpString!=null) defaultPropertyValue=tmpString;
      else defaultPropertyValue="Unknown";

      tmpString = props.getProperty(propName+"."+SWITCH);
      if (tmpString!=null) propertySwitch=tmpString;
      else propertySwitch="Unknown";

      tmpString = props.getProperty(propName+"."+EDITOR);
      if (tmpString!=null) editor=tmpString;
      else editor="Unknown";

      // Finish the collection of the possible options for this property.
      for (Iterator it = props.keySet().iterator(); it.hasNext(); ) {
        String tmpOption = (String)it.next();
        if (tmpOption.startsWith(propName+"."+OPTION)) {
          String optionKey = tmpOption.substring(propName.length()+OPTION.length()+2, tmpOption.length());
          optionKey = PreferenceManager.getKeyForName(optionKey, false);
          String optionValue = props.getProperty(tmpOption).trim();
          optionToDisplayNameMap.put(optionKey, optionValue);
        }
      }
    }

    public String getName() { return name; }
    public String getEditor() { return editor; }
    public String getSwitch() { return propertySwitch; }
    public TreeMap getPropertyOptions() { return optionToDisplayNameMap; }
    public String getDefaultValue() { return defaultPropertyValue; }
    public String toString() { return getName(); }

    /**
     * Yields the actual value for the display name selected.
     */
    public String getValueForOption(String targetOption) {
      for (Iterator it = optionToDisplayNameMap.keySet().iterator(); it.hasNext(); ) {
        String tmpKey = (String)it.next();
        String tmpValue = (String)optionToDisplayNameMap.get(tmpKey);
        if (tmpValue.equals(targetOption)) return tmpKey;
      }
      return "";
    }
  }
}