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
package api.entity_model.management.properties;

import api.entity_model.model.fundtype.GenomicEntity;

import java.util.List;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies (peter.davies)
 * @version $Id$
 */

public class HTMLPropertyValueFormatter extends PropertyValueFormatter {

  private String formatRuleString;

  public static String getExampleString() {
    return "http://www.xxxxx.dom/?id=<id> (where the value of the "+
      "property id is inserted at the tag <id>)";
  }

  /**
   * This formatter will replace everything in the formatRuleString that is
   * tokenized by <property_name> with the value of that property.  For example:
   * a string of: http://www.test.com/?name=<collection_name> will return a value
   * of http://www.test.com/?name=chr17 if the collection_name property is chr17.
   */
  public HTMLPropertyValueFormatter(String formatRuleString) {
    this.formatRuleString=formatRuleString;
  }

  public String formatInitialValue(GenomicEntity entity, List originalProperties){
    StringBuffer formattedString=new StringBuffer(100);
    String propertyName;
    String tmpFormatRuleString=formatRuleString;
    int startTokenIndex=0;
    int endTokenIndex=0;
    while (tmpFormatRuleString.indexOf('<')>0 || tmpFormatRuleString.length()>0) {
      startTokenIndex=tmpFormatRuleString.indexOf('<');
      if (startTokenIndex==-1){
        formattedString.append(tmpFormatRuleString);
        break;
      }
      formattedString.append(tmpFormatRuleString.substring(0,startTokenIndex));
      endTokenIndex=tmpFormatRuleString.indexOf('>');
      propertyName=tmpFormatRuleString.substring(startTokenIndex+1,
        endTokenIndex);
      formattedString.append(getValueOfProperty(originalProperties,propertyName));
      tmpFormatRuleString=tmpFormatRuleString.substring(endTokenIndex+1);
    }
    return formattedString.toString();
  }

  public String getFormatRuleString(){
    return formatRuleString;
  }

}