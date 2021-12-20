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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class NavigationPath implements java.io.Serializable, Comparable
{
  private String            displayName;
  private List  nodeArray;

  public NavigationPath(String displayName,
                        NavigationNode[] nodeArray) {
    this.displayName    = displayName;
    this.nodeArray      = new LinkedList(Arrays.asList(nodeArray));
  }

  public String           getDisplayName()          { return displayName; }
  public NavigationNode[] getNavigationNodeArray()  { return
    (NavigationNode[]) nodeArray.toArray(new NavigationNode[nodeArray.size()]); }
  public List             getNavigationNodeList()     { return Collections.unmodifiableList(nodeArray); }

  public void setDisplayName(String newName) { this.displayName = newName; }

  public void appendNewNode(NavigationNode node) {
    nodeArray.add(node);
  }

  public void prependNewNode(NavigationNode node) {
    nodeArray.add(0,node);
  }

  /** @return a URL-like String that displays the path */
  public String toString() {
    StringBuffer retVal = new StringBuffer(128);
    retVal.append(displayName);
    retVal.append(":");
    for(int i=0; i<nodeArray.size(); ++i) {
      retVal.append("/");
      retVal.append(nodeArray.get(i).toString());
    }
    return retVal.toString();
  }

  public int compareTo(Object o2) {
    NavigationPath nav2 = (NavigationPath)o2;
    if (nav2 == null) return 1;
    Integer gV1 = new Integer(getNavigationNodeArray()[0].getOID().getGenomeVersionId());
    Integer gV2 = new Integer(nav2.getNavigationNodeArray()[0].getOID().getGenomeVersionId());
    int genomeVersionComparison = gV1.compareTo(gV2);
    return genomeVersionComparison;
  }

}
