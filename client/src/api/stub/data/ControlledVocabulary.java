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
package api.stub.data;

import java.util.*;

public class ControlledVocabulary implements java.io.Serializable
{
  private Map forwardMap = new HashMap();
  private Map reverseMap = new HashMap();
  private List keyOrder = null;

  public ControlledVocabulary(List valuesInOrder) {
    keyOrder = valuesInOrder;
  }

  public ControlledVocabulary() {}

  public void addEntry(String value, String name) {
    forwardMap.put(value, name);
    reverseMap.put(name, value);
  }

  /**
   * return the string associated with the input value
   * If there is not a name associated with the string, the input value is returned
   */
  public String lookup(String value) {
    String mapVal = (String)forwardMap.get(value);
    return (mapVal == null) ? value : mapVal;
  }

  public String reverseLookup(String name) {
    String mapVal = (String)reverseMap.get(name);
    return (mapVal == null) ? name : mapVal;
  }

  public Collection getNames() {
    if (keyOrder != null) {
      List retVal = new ArrayList(keyOrder.size());
      for (Iterator iter = keyOrder.iterator(); iter.hasNext(); ) {
        retVal.add(forwardMap.get(iter.next()));
      }
      return retVal;
    }
    return reverseMap.keySet();
  }

  public String[] getValues() {
    String[] retVal = null;
    if (keyOrder != null) {
      retVal = new String[keyOrder.size()];
      retVal = (String[])keyOrder.toArray(retVal);
      return retVal;
    }
    else {
      retVal = new String[forwardMap.size()];
      retVal = (String[])forwardMap.keySet().toArray(retVal);
    }
    return retVal;
  }

}
