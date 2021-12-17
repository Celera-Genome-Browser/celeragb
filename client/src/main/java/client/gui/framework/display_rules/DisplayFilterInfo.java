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

import api.facade.abstract_facade.annotations.GenePredictionDetailFacade;
import api.facade.abstract_facade.annotations.HSPFacade;
import api.facade.abstract_facade.annotations.RegulatoryRegionFacade;
import shared.preferences.InfoObject;
import shared.preferences.PreferenceManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This class is intended to be the base class for the Display Rule Info objects.
 * It maintains the base information like the target property to be worked upon
 * and the collection of feature groups that will have the rule applied to them.
 * This is the base class for the display filtering mechanism.
 */
public abstract class DisplayFilterInfo extends InfoObject {
  public static final int NUM_PROPS=9;
  public static final int NUM_COL=3;
  protected static Object [][] suggestedValueTable = new Object[NUM_PROPS][NUM_COL];
  protected static final String NAME              = "Name";
  protected static final String TARGET_PROPERTY   = "TargetProperty";
  protected static final String EFFECTED_FG       = "EffectedFeatureGroup";

  //  Property the rule is to act upon.
  protected String targetProperty = new String(HSPFacade.BIT_SCORE_PROP);
  //  This is a list of the selected feature group names.
  protected ArrayList effectedFGs = new ArrayList();


  public DisplayFilterInfo(String keyBase, Properties inputProperties, String sourceFile) {
    this.keyBase=keyBase;
    this.sourceFile=sourceFile;
    this.suggestedValueTable=getPropertyInformation();
    String tmpString = new String("");

    tmpString = (String)inputProperties.getProperty(keyBase+"."+NAME);
    if (tmpString!=null) name=tmpString;
    else name="Unknown";

    tmpString = (String)inputProperties.getProperty(keyBase+"."+TARGET_PROPERTY);
    if (tmpString!=null) targetProperty=tmpString;
    else targetProperty="Unknown";

    formatFeatureGroupCollection(inputProperties);
  }


  public DisplayFilterInfo(String name, String targetProperty, ArrayList effectedFGs) {
    this.name = name;
    this.targetProperty=targetProperty;
    this.effectedFGs = new ArrayList(effectedFGs);
    this.suggestedValueTable=getPropertyInformation();
  }

  /**
   * This method should take all of the specific feature groups that positively
   * apply to the display filter defined.
   */
  private void formatFeatureGroupCollection(Properties inputProperties) {
    for (Iterator it=inputProperties.keySet().iterator();it.hasNext();) {
      String tmpKey = (String)it.next();
      StringTokenizer mainToken = new StringTokenizer(tmpKey,".");
      String firstToken = mainToken.nextToken();
      tmpKey = tmpKey.substring(firstToken.length()+1);
      if (tmpKey!=null && mainToken!=null && tmpKey!="") {
        String secondToken = mainToken.nextToken();
        if (secondToken.equalsIgnoreCase(EFFECTED_FG))  {
          String remainingString = tmpKey.substring(secondToken.length()+1);
          effectedFGs.add(PreferenceManager.getKeyForName(remainingString,false));
        }
      }
    }
  }

  public String toString() { return getName(); }

  /**
   * Until such point that we have time, these properties are being hard coded
   * into the class.  Also, we just can't give the user a blank slate to try and
   * apply filters to the views based on any property, so for now they can
   * only choose these.
   */
  public static Object[][] getPropertyInformation() {
    // Columns: 0 = property name, 1 = min intensity value, 2 = max intensity value
    Object [][] suggestedValueTable = new Object[NUM_PROPS][NUM_COL];
    suggestedValueTable[0][0]=HSPFacade.BIT_SCORE_PROP;
    suggestedValueTable[0][1]=new Double(0);
    suggestedValueTable[0][2]=new Double(1e3);
    suggestedValueTable[1][0]=GenePredictionDetailFacade.CODING_REG_SCORE_PROP;
    suggestedValueTable[1][1]=new Double(0);
    suggestedValueTable[1][2]=new Double(1e5);
    suggestedValueTable[2][0]=GenePredictionDetailFacade.EXON_PROB_PROP;
    suggestedValueTable[2][1]=new Double(0);
    suggestedValueTable[2][2]=new Double(1e2);
    suggestedValueTable[3][0]=GenePredictionDetailFacade.EXON_SCORE_PROP;
    suggestedValueTable[3][1]=new Double(0);
    suggestedValueTable[3][2]=new Double(1e5);
    suggestedValueTable[4][0]=HSPFacade.INDIVIDUAL_E_VAL_PROP;
    suggestedValueTable[4][1]=new Double(1e-100);
    suggestedValueTable[4][2]=new Double(1e0);
    suggestedValueTable[5][0]=HSPFacade.NUM_SIM_OR_POS_PROP;
    suggestedValueTable[5][1]=new Double(0);
    suggestedValueTable[5][2]=new Double(1e2);
    suggestedValueTable[6][0]=HSPFacade.NUM_IDENTICAL_PROP;
    suggestedValueTable[6][1]=new Double(0);
    suggestedValueTable[6][2]=new Double(1e3);
    suggestedValueTable[7][0]=HSPFacade.PERCENT_IDENTITY_PROP;
    suggestedValueTable[7][1]=new Double(0);
    suggestedValueTable[7][2]=new Double(1e2);
    suggestedValueTable[8][0]=RegulatoryRegionFacade.RR_SCORE_PROP;
    suggestedValueTable[8][1]=new Double(0);
    suggestedValueTable[8][2]=new Double(-1e2);
    return suggestedValueTable;
  }

  public String getTargetProperty() { return targetProperty; }
  void setTargetProperty(String targetProperty) {
    isDirty=true;
    this.targetProperty=targetProperty;
  }

  public ArrayList getEffectedFGs() { return effectedFGs; }
  void setEffectedFGs(ArrayList effectedFGs) {
    isDirty=true;
    this.effectedFGs = new ArrayList(effectedFGs);
  }

  public Properties getPropertyOutput() {
    Properties outputProperties=new Properties();
    String key = getKeyName()+".";

    outputProperties.put(key+NAME,name);
    outputProperties.put(key+TARGET_PROPERTY,targetProperty);
    for (Iterator it = effectedFGs.iterator(); it.hasNext(); ) {
      String tmpValue = (String)it.next();
      String tmpKey = PreferenceManager.getKeyForName(tmpValue, true);
      outputProperties.put(key+EFFECTED_FG+"."+tmpKey, tmpValue);
    }
    return outputProperties;
  }

  /**
   * Essentially, as long as the range applied and property type do not change an
   * old property report can be reused.
   * We are purposefully ignoring the max, min, and state atts as they do not
   * prompt a call to the server even if they have changed.  They are merely
   * applied to the feature property values.
   */
  public boolean canReuseReport(DisplayFilterInfo targetInfo) {
    if (targetInfo == null) return false;
    return (targetInfo.getTargetProperty().equals(targetProperty) &&
            targetInfo.getEffectedFGs().equals(effectedFGs));
  }

  public abstract Object clone();
}