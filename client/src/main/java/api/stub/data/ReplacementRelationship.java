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
 * Title:        Your Product Name<p>
 * Description:  This is the main Browser in the System<p>
 * @author Peter Davies
 * @version
 */
package api.stub.data;


import java.util.HashSet;
import java.util.Set;

public class ReplacementRelationship implements java.io.Serializable {

  private byte type;
  private Set replacementOidSet = new HashSet();

  // Modification "states" that scratch proxy intervals will go through.
  public static final String TYPE_UNMODIFIED = "unmodified";
  public static final String TYPE_MODIFIED = "modified";
  public static final String TYPE_OBSOLETE = "obsolete";
  public static final String TYPE_NEW = "new";
  public static final String TYPE_SPLIT = "split";
  public static final String TYPE_MERGE = "merge";
  public static final String TYPE_DEEP_MOD = "deep-modified";

  private static final byte MIN_TYPE_BYTE=1;
  private static final byte MAX_TYPE_BYTE=7;

  public static final byte OBSOLETE=1;
  public static final byte UNMODIFIED=2;
  public static final byte NEW=3;
  public static final byte SPLIT=4;
  public static final byte MERGE=5;
  public static final byte DEEP_MOD=6;
  public static final byte MODIFIED=7;

  public ReplacementRelationship(String type) {
     this.type=getTypeCode(type);
  }

  public ReplacementRelationship(OID replacementOID) {
     this(ReplacementRelationship.TYPE_UNMODIFIED,replacementOID);
  }

  public ReplacementRelationship(OID[] replacementOIDs) {
     this(ReplacementRelationship.TYPE_UNMODIFIED,replacementOIDs);
  }

  public ReplacementRelationship(String type, OID[] replacementOIDs) {
     this.type=getTypeCode(type);
     for (int i=0;i<replacementOIDs.length;i++) {
        this.replacementOidSet.add(replacementOIDs[i]);
    }
  }

  public ReplacementRelationship(String type, OID replacementOID) {
     this.type=getTypeCode(type);
     this.replacementOidSet.add(replacementOID);
  }

  public OID[] getReplacementOIDs() {
     return (OID[])replacementOidSet.toArray(new OID[0]);
  }

  public int getNumberOfReplacementOIDs() {
    return replacementOidSet.size();
  }

  public String getReplacementType() {
     return ReplacementRelationship.getTypeString(type);
  }

  public byte getReplacementTypeAsByte() {
    return type;
  }


  /**
   * Test for a particular type...
   */
  public boolean isReplacementType(byte testType) {
    return (type == testType);
  }


  public void addReplacementOID(OID newOID){
     replacementOidSet.add(newOID);
  }

  public boolean removeReplacementOID(OID removeOID){
     return replacementOidSet.remove(removeOID);
  }


  /**
   * Set the replacement type via String...
   */
  public void setNewReplacementType(String type){
     this.type=getTypeCode(type);
  }


  /**
   * Get the replacement type via byte...
   */
  public static byte getTypeCode(String name) {
     if (name.equalsIgnoreCase(ReplacementRelationship.TYPE_OBSOLETE)) return OBSOLETE;
     if (name.equalsIgnoreCase(ReplacementRelationship.TYPE_UNMODIFIED)) return UNMODIFIED;
     if (name.equalsIgnoreCase(ReplacementRelationship.TYPE_NEW)) return NEW;
     if (name.equalsIgnoreCase(ReplacementRelationship.TYPE_SPLIT)) return SPLIT;
     if (name.equalsIgnoreCase(ReplacementRelationship.TYPE_MERGE)) return MERGE;
     if (name.equalsIgnoreCase(ReplacementRelationship.TYPE_DEEP_MOD)) return DEEP_MOD;
     if (name.equalsIgnoreCase(ReplacementRelationship.TYPE_MODIFIED)) return MODIFIED;
     throw new IllegalArgumentException("Name passed to "+
       " ReplacementRelationship.getTypeCode is invalid");
  }


  /**
   * Get the replacement string via byte...
   */
  public static String getTypeString(byte type) {
     switch (type) {
        case OBSOLETE: return ReplacementRelationship.TYPE_OBSOLETE;
        case UNMODIFIED: return ReplacementRelationship.TYPE_UNMODIFIED;
        case NEW: return ReplacementRelationship.TYPE_NEW;
        case SPLIT: return ReplacementRelationship.TYPE_SPLIT;
        case MERGE: return ReplacementRelationship.TYPE_MERGE;
        case DEEP_MOD: return ReplacementRelationship.TYPE_DEEP_MOD;
        case MODIFIED: return ReplacementRelationship.TYPE_MODIFIED;
        default : throw new IllegalStateException("Type code set in ReplacementRelationship is not known");
     }
   }
}