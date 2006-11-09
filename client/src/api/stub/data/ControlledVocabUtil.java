// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package api.stub.data;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class ControlledVocabUtil
{

  private ControlledVocabUtil() {}

  public static void main(String[] args) {
    Map htbl = ControlledVocabUtil.getControlledVocabulariesFromResource();
    for (Iterator mapIter = htbl.keySet().iterator(); mapIter.hasNext(); ) {
      String index  = (String)mapIter.next();
      System.out.println("---------" + index + "---------");
      List mappings = (List)htbl.get(index);
      for (Iterator listIter = mappings.iterator(); listIter.hasNext(); ) {
        ControlledVocabElement element = (ControlledVocabElement)listIter.next();
        System.out.println(element.value + "=" + element.name);
      }
    }
  }

  public static Map getControlledVocabulariesFromResource() {
    return getControlledVocabulariesFromResource("resource.shared.ControlledVocab");
  }

  public static Map getControlledVocabulariesFromResource(String resourceName) {
    Map vocabularies = new TreeMap();
    ResourceBundle vocabBundle = ResourceBundle.getBundle(resourceName);

    // For each Vocab
    for (Enumeration enum = vocabBundle.getKeys(); enum.hasMoreElements(); ) {
      String index = (String)enum.nextElement();
      String mapValues = vocabBundle.getString(index);
      List newVocab = new ArrayList();
      // For each element in the Vocab
      for (StringTokenizer mapValueTokens = new StringTokenizer(mapValues, ";");
           mapValueTokens.hasMoreTokens(); ) {
        String mapElem = mapValueTokens.nextToken();

        // Get the value/name pair
        StringTokenizer mapElemTokens = new StringTokenizer(mapElem, ",");
        ControlledVocabElement newVocabElement = new ControlledVocabElement();
        newVocabElement.value = mapElemTokens.nextToken();
        newVocabElement.name  = mapElemTokens.nextToken();
        newVocab.add(newVocabElement);
      }
      vocabularies.put(index, newVocab);
    }

    return vocabularies;
  }

  public static ControlledVocabElement[] getControlledVocab(String vocabIndex, Map vocabularies)
    throws NoData
  {
    List ctrlElemVec = (List)vocabularies.get(vocabIndex);
    if (ctrlElemVec == null) {
      throw new NoData();
    }
    ControlledVocabElement[] retVal = new ControlledVocabElement[ctrlElemVec.size()];
    retVal = (ControlledVocabElement[])ctrlElemVec.toArray(retVal);
    return retVal;
  }

  private static String nullVocabString = "Null_Vocab";

  public static String getNullVocabIndex() {
    return nullVocabString;
  }

  public static boolean isNullVocabIndex(String vocabIndex) {
    return nullVocabString.equals(vocabIndex);
  }
}
