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

/**
 * Title:        Genome Browser<p>
 * Description:  <p>
 * @author Todd Safford
 * @version $Id$
 */
package api.entity_model.management.properties;

public class PropertyElement {

  private String propName;
  private String propDisplayName;
  private String propNew;
  private String propNewEditable;
  private String propUpdate;
  private String propUpdateEditable;
  private String propAvailable;
  private String propOrder;
  private String propTooltipFlag;

  public PropertyElement() {}

  public PropertyElement(String propName, String propDisplayName, String propNew,
    String propNewEditable, String propUpdate, String propUpdateEditable,
    String propAvailable, String propOrder, String propTooltipFlag)
  {
     this.propName = propName;
     this.propDisplayName = propDisplayName;
     this.propNew = propNew;
     this.propNewEditable = propNewEditable;
     this.propUpdate = propUpdate;
     this.propUpdateEditable = propUpdateEditable;
     this.propAvailable = propAvailable;
     this.propOrder = propOrder;
     this.propTooltipFlag = propTooltipFlag;
  }


  public String getPropName() { return propName; }
  public String getPropDisplayName() { return propDisplayName; }
  public String getPropNew() { return propNew; }
  public String getPropNewEditable() { return propNewEditable; }
  public String getPropUpdate() { return propUpdate; }
  public String getPropUpdateEditable() { return propUpdateEditable; }
  public String getPropAvailable() { return propAvailable; }
  public String getPropOrder() { return propOrder; }
  public String getPropTooltipFlag() { return propTooltipFlag; }

  public void setPropName(String propName) { this.propName = propName; }
  public void setPropDisplayName(String propDisplayName) {
    this.propDisplayName = propDisplayName;
  }
  public void setPropNew(String propNew) { this.propNew = propNew; }
  public void setPropNewEditable(String propNewEditable) { this.propNewEditable = propNewEditable; }
  public void setPropUpdate(String propUpdate) { this.propUpdate = propUpdate; }
  public void setPropUpdateEditable(String propUpdateEditable) { this.propUpdateEditable = propUpdateEditable; }
  public void setPropAvailable(String propAvailable) { this.propAvailable = propAvailable; }
  public void setPropOrder(String propOrder) { this.propOrder = propOrder; }
  public void setPropTooltipFlag(String propTooltipFlag) { this.propTooltipFlag = propTooltipFlag; }

  public String toString() {
    return new String("Property: "+propName+"-> "+propDisplayName+", "+propNew+", "
      +propNewEditable+", "+propUpdate+", "+propUpdateEditable+", "+propAvailable+
      ", "+propOrder+", "+propTooltipFlag);
  }

}