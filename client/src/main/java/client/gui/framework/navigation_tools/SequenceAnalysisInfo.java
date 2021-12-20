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

import shared.preferences.InfoObject;
import shared.preferences.PreferenceManager;

import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class SequenceAnalysisInfo extends InfoObject {
  private static final String NAME            = "Name";
  private static final String ANALYSIS_TYPE   = "AnalysisType";
  private static final String ARGUMENT        = "Argument";

  private TreeMap arguments = new TreeMap();
  private String analysisType  = "";

  public SequenceAnalysisInfo(String keyBase, Properties inputProperties, String sourceFile) {
    this.keyBase=keyBase;
    this.sourceFile=sourceFile;
    String tmpString = new String("");

    tmpString = (String)inputProperties.getProperty(keyBase+"."+NAME);
    if (tmpString!=null) name=tmpString;
    else name="Unknown";

    tmpString = (String)inputProperties.getProperty(keyBase+"."+ANALYSIS_TYPE);
    if (tmpString!=null) analysisType=tmpString;
    else analysisType="Unknown";

    formatArgumentCollection(inputProperties);
  }


  public SequenceAnalysisInfo(String name, String analysisType, TreeMap arguments) {
    this.keyBase=PreferenceManager.getKeyForName(name,true);
    this.sourceFile="Unknown";
    this.name = name;
    this.analysisType = analysisType;
    this.arguments = arguments;
  }


  /**
   * This method should take all of the specific arguments and place them
   * in the collection with their values.  The arguments, destined for the command
   * line, must be unique by nature.
   */
  private void formatArgumentCollection(Properties inputProperties) {
    for (Iterator it=inputProperties.keySet().iterator();it.hasNext();) {
      String tmpKey = (String)it.next();
      String tmpValue = ((String)inputProperties.get(tmpKey)).trim();
      StringTokenizer mainToken = new StringTokenizer(tmpKey,".");
      String firstToken = mainToken.nextToken();
      tmpKey = tmpKey.substring(firstToken.length()+1);
      if (tmpKey!=null && mainToken!=null && tmpKey!="") {
        String secondToken = mainToken.nextToken();
        if (secondToken.equalsIgnoreCase(ARGUMENT))  {
          String remainingString = tmpKey.substring(secondToken.length()+1);
          arguments.put(PreferenceManager.getKeyForName(remainingString,false), tmpValue);
        }
      }
    }
  }


  // This constructor should only be used for the clone.
  private SequenceAnalysisInfo(String keyBase, String name, String sourceFile,
      String analysisType, TreeMap arguments){
    this.keyBase = keyBase;
    this.name = name;
    this.sourceFile = sourceFile;
    this.arguments = arguments;
    this.analysisType = analysisType;
  }


  public String toString() {
     return name;
  }


  public Object clone() {
    TreeMap tmpArguments = new TreeMap();
    for (Iterator it = arguments.keySet().iterator(); it.hasNext(); ) {
      String tmpKey = (String)it.next();
      String tmpValue = (String)arguments.get(tmpKey);
      tmpArguments.put(tmpKey, tmpValue);
    }
    SequenceAnalysisInfo tmpInfo = new SequenceAnalysisInfo(this.keyBase, this.name,
      this.sourceFile, this.analysisType, tmpArguments);
    return tmpInfo;
  }


  public String getKeyName(){
    return "SequenceAnalysis." + keyBase;
  }


  public String getSequenceAnalysisType() { return analysisType; }
  public TreeMap getArgumentCollection() { return arguments; }

  /**
   * This method is so the object will provide the formatted properties
   * for the writeback mechanism.
   */
  public Properties getPropertyOutput(){
    Properties outputProperties=new Properties();
    String key = getKeyName()+".";

    outputProperties.put(key+NAME, name);
    outputProperties.put(key+ANALYSIS_TYPE, analysisType);

    for (Iterator it = arguments.keySet().iterator(); it.hasNext(); ) {
      String tmpKey = (String)it.next();
      String tmpValue = (String)arguments.get(tmpKey);
      outputProperties.put(key+ARGUMENT+"."+PreferenceManager.getKeyForName(tmpKey, true), tmpValue);
    }
    return outputProperties;
  }
}