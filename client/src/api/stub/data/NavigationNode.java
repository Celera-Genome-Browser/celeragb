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

import api.stub.geometry.Range;

public class NavigationNode implements java.io.Serializable
{
  public static final int UNKNOWN = 0;
  public static final int SPECIES = 1;
  public static final int CHROMOSOME = 2;
  public static final int AXIS = 3;
  public static final int CONTIG = 4;
  public static final int SCAFFOLD = 5;
  public static final int PRECOMPUTE_HIGH_PRI = 6;
  public static final int PRECOMPUTE_LOW_PRI = 7;
  public static final int CURATED = 8;
  public static final int NON_CURATED = 9;
  public static final int GENOME = 10;

  public static final int ROOT_NODE_TYPE = GENOME;

  private OID     oid;
  private int     nodeType;
  private String  displayName;
  private Range   rangeOnParent;

  static final long serialVersionUID =-5110958201352939191L;

  public NavigationNode(OID oid, int nodeType, String displayName, Range rangeOnParent)
  {
    this.oid            = oid;
    this.nodeType       = nodeType;
    this.displayName    = displayName;
    this.rangeOnParent  = rangeOnParent;
  }

  public OID    getOID()            { return oid; }
  public int    getNodeType()       { return nodeType; }
  public String getDisplayname()    { return displayName; }
  public Range  getRangeOnParent()  { return rangeOnParent; }

  public String toString()
  {
    StringBuffer retVal = new StringBuffer(64);
    retVal.append("(");
    retVal.append(nodeType);
    retVal.append("),");
    retVal.append(oid.toString());
    retVal.append(",");
    retVal.append(rangeOnParent);
    return retVal.toString();
  }
}