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
import java.lang.StrictMath;


public class ColorIntensityInfo extends DisplayFilterInfo {
  public static final int STEP                    = 0;
  public static final int INTERPOLATION           = 1;
  public static final int LOG                     = 2;
  public static final String DISPLAY_STATE        = "DisplayState";
  public static final String MAX                  = "Max";
  public static final String MIN                  = "Min";

  private int displayState = 0;
  private double infoMax = 0.0;
  private double infoMin = 0.0;

  public ColorIntensityInfo(String keyBase, Properties inputProperties, String sourceFile) {
    super(keyBase, inputProperties, sourceFile);
    String tmpString = new String("");

    tmpString = (String)inputProperties.getProperty(keyBase+"."+DISPLAY_STATE);
    if (tmpString!=null) displayState=Integer.parseInt(tmpString);
    else displayState=STEP;

    tmpString = (String)inputProperties.getProperty(keyBase+"."+MAX);
    if (tmpString!=null) infoMax=Double.parseDouble(tmpString);
    else infoMax=0l;

    tmpString = (String)inputProperties.getProperty(keyBase+"."+MIN);
    if (tmpString!=null) infoMin=Double.parseDouble(tmpString);
    else infoMin=0l;
  }


  public ColorIntensityInfo(String keyBase, String name, String sourceFile, String targetProperty, ArrayList effectedFGs,
                     int displayState, double infoMax, double infoMin) {
    super(name, targetProperty, effectedFGs);
    this.keyBase = keyBase;
    this.sourceFile = sourceFile;
    this.displayState=displayState;
    this.infoMax = infoMax;
    this.infoMin = infoMin;
  }


  public int getDisplayState() { return displayState; }
  void setDisplayState(int displayState) {
    this.displayState=displayState;
    isDirty = true;
  }

  public String getKeyName() { return "ColorIntensityFilter."+keyBase; }

  public double getMaxValue() { return infoMax; }
  void setMaxValue(double newMax) {
    infoMax = newMax;
    isDirty = true;
  }

  public double getMinValue() { return infoMin; }
  void setMinValue(double newMin) {
    infoMin = newMin;
    isDirty = true;
  }


  public float getIntensityForValue(double propertyValue) {
    if (displayState==STEP){
      // Positive and zero slope case.
      if (infoMax>=infoMin) {
        if (propertyValue>=infoMax) return 1.0f;
      }
      // Negative slope case.
      else if (infoMax<infoMin) {
        if (propertyValue<=infoMin) return 0.0f;
      }
      return 0.0f;
    }

    // Find the two value points that a property falls within, get their slope and
    //  interpolate the intensity for the given property.
    else if (displayState==INTERPOLATION) {
      // Max intensity for those who qualify according to the slope.
      if (infoMax>=infoMin) {
        if (propertyValue>=infoMax)
          return 1.0f;
      }
      else {
        if (propertyValue<=infoMin)
          return 0.0f;
      }

      double x2 = infoMax;
      double x1 = infoMin;

      float y2 = 1.0f;
      float y1 = 0.0f;
      float slope = (new Float((y2-y1)/(x2-x1))).floatValue();
      float intensity = (new Float(slope*(propertyValue-x1)+y1)).floatValue();
      return intensity;
    }
    // Take the max and min values and log all in between.
    else if (displayState==LOG) {
      if (infoMax==infoMin) return 1.0f;
      try {
        // Find the offset so that the range is positive and apply.
        double offset = 0.0;
        if (infoMax<=infoMin) {
          if (infoMax<0) offset = -1 * infoMax;
        }
        else {
          if (infoMin<0) offset = -1 * infoMin;
        }
        infoMax = infoMax + offset;
        infoMin = infoMin + offset;
        propertyValue = propertyValue + offset;

        // Clean up the zeroes or those close.  Will always be >=0 now.
        if (infoMax<1e-320 && infoMax>=0) {
          infoMax=(double)1e-320;
        }
        if (infoMin<1e-320 && infoMin>=0) {
          infoMin=(double)1e-320;
        }
        if (propertyValue<1e-320 && propertyValue>=0) {
          propertyValue=1e-320;
        }

        // Now take the logs and place into the line equation for scaling of I.
        //  The log below is actually Natural Log: ln.
        double x2 = StrictMath.log(infoMax) / StrictMath.log(10);
        double x1 = StrictMath.log(infoMin) / StrictMath.log(10);
        double xValue = StrictMath.log(propertyValue)/StrictMath.log(10);
        double rise = (x2>=x1) ? 1.0 : -1.0;
        double run = (x2>=x1) ? x2-x1 : x1-x2;

        // Return values for those that fall out of the range.
        if (((x2 >= x1) && xValue>x2) || ((x2<x1) && xValue>x1)) return 1.0f;
        if (((x2 >= x1) && xValue<x1) || ((x2<x1) && xValue<x2)) return 0.0f;

        // Regardless of slope find the right range.
        // The line equation will take care of itself.
        if (((x2 >= x1) && (xValue <  x2) && (xValue >= x1)) ||
            ((x2 <  x1) && (xValue >= x2) && (xValue <  x1)))
        {
          double slope = rise/run;
          double yIntercept = 1-slope*x2;
          float intensity = (new Float(slope*xValue+yIntercept)).floatValue();
          return intensity;
        }
        else return 1.0f;
      }
      catch (Exception ex) { return 1.0f; }
    }
    else return 1.0f;
  }


  public Object clone() {
   return new ColorIntensityInfo(this.keyBase, this.name, this.sourceFile, targetProperty,
    (ArrayList)effectedFGs.clone(), displayState, infoMax, infoMin);
  }


  /**
   * Essentially, as long as the range applied and property type do not change an
   * old property report can be reused.
   * We are purposefully ignoring the max, min, and state atts as they do not
   * prompt a call to the server even if they have changed.  They are merely
   * applied to the feature property values.
   */
  public boolean canReuseReport(ColorIntensityInfo targetInfo) {
    if (targetInfo == null) return false;
    return (super.canReuseReport(targetInfo));
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
    outputProperties.put(key+MAX, Double.toString(infoMax));
    outputProperties.put(key+MIN, Double.toString(infoMin));

    return outputProperties;
  }
}