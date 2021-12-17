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
package api.entity_model.model.assembly;
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
*********************************************************************/


public class AxisType implements java.io.Serializable
{
    private int value;

    /**
     * This method should be used when constructing an AxisType from
     * a persistent storage mechanism (assuming that mechanism stores)
     * the internal state of the object as an int
     */
    public static AxisType axisTypeForValue(int value)
      throws java.lang.IllegalArgumentException
    {
      AxisType instanceForType = null;
      switch (value)
      {
        case 0:
          instanceForType = HR_GB4;
          break;
        case 1:
          instanceForType = HR_G3;
          break;
        case 2:
          instanceForType = HR_INTEGRATED_NCBI;
          break;
        case 3:
          instanceForType = GENETIC;
          break;
        case 4:
          instanceForType = STS_CONTENT_YAC;
          break;
        case 5:
          instanceForType = STS_CONTENT_BAC;
          break;
        case 6:
          instanceForType = BAC_TILING_EXTERNAL_FPC;  //fingerprint BAC
          break;
        case 7:
          instanceForType = BAC_TILING_EXTERNAL_SEQUENCE_HOMOLOGY;
          break;
        case 8:
          instanceForType = BAC_TILING_EXTERNAL_HYBRIDIZATION;
          break;
        case 9:
          instanceForType = BAC_TILING_EXTERNAL_COMPOSITE;
          break;
        case 10:
          instanceForType = BAC_TILING_INTERNAL_OVERLAY;
          break;
        case 11:
          instanceForType = BAC_TILING_INTERNAL_GRANDE;
          break;
        case 12:
          instanceForType = BAC_TILING_INTERNAL_COMPOSITE;
          break;
        case 13:
          instanceForType = CYTOGENETIC;
          break;
        case 14:
          instanceForType = CYTOGENETIC_WITH_GENES_AND_OTHER_MARKERS;
          break;
        case 15:
          instanceForType = INTERNAL;
          break;
        case 16:
          instanceForType = CLONE;
          break;
        case 1000:
          throw new java.lang.IllegalArgumentException("Value " +
            value + " not a valid AxisType");
        default:
          System.out.println("Value " + value + " not a valid AxisType");
          //throw new java.lang.IllegalArgumentException("Value " +
          //  value + " not a valid AxisType");
          instanceForType = UNDEFINED;
      }
      return instanceForType;
    }


    public static AxisType HR_GB4 = new AxisType(0);
    public static AxisType HR_G3 = new AxisType(1);
    public static AxisType HR_INTEGRATED_NCBI = new AxisType(2);
    public static AxisType GENETIC = new AxisType(3);
    public static AxisType STS_CONTENT_YAC = new AxisType(4);
    public static AxisType STS_CONTENT_BAC = new AxisType(5);
    public static AxisType BAC_TILING_EXTERNAL_FPC= new AxisType(6);
    public static AxisType BAC_TILING_EXTERNAL_SEQUENCE_HOMOLOGY = new AxisType(7);
    public static AxisType BAC_TILING_EXTERNAL_HYBRIDIZATION = new AxisType(8);
    public static AxisType BAC_TILING_EXTERNAL_COMPOSITE = new AxisType(9);
    public static AxisType BAC_TILING_INTERNAL_OVERLAY = new AxisType(10);
    public static AxisType BAC_TILING_INTERNAL_GRANDE = new AxisType(11);
    public static AxisType BAC_TILING_INTERNAL_COMPOSITE = new AxisType(12);
    public static AxisType CYTOGENETIC = new AxisType(13);
    public static AxisType CYTOGENETIC_WITH_GENES_AND_OTHER_MARKERS = new AxisType(14);
    public static AxisType INTERNAL = new AxisType(15);
    public static AxisType CLONE = new AxisType(16);
    public static AxisType UNDEFINED = new AxisType(-1);

    // Allows creation of instances using reflection
    public AxisType()
    {
      value = -1;
    }
    protected AxisType
        (int value)
    {
        this.value = value;
    }

    public int value()
    {
      return value;
    }

    public String toString()
    {
      return nameForType(value);
    }

    public static String nameForType(int value)
    {
      String str = null;
      switch (value)
      {
        case 0:
          str = "HR_GB4";
          break;
        case 1:
          str = "HR_G3";
          break;
        case 2:
          str = "HR_INTEGRATED_NCBI";
          break;
        case 3:
          str = "GENETIC";
          break;
        case 4:
          str = "STS_CONTENT_YAC";
          break;
        case 5:
          str = "STS_CONTENT_BAC";
          break;
        case 6:
          str = "BAC_TILING_EXTERNAL_FPC";
          break;
        case 7:
          str = "BAC_TILING_EXTERNAL_SEQUENCE_HOMOLOGY";
          break;
        case 8:
          str = "BAC_TILING_EXTERNAL_HYBRIDIZATION";
          break;
        case 9:
          str = "BAC_TILING_EXTERNAL_COMPOSITE";
          break;
        case 10:
          str = "BAC_TILING_INTERNAL_OVERLAY";
          break;
        case 11:
          str = "BAC_TILING_INTERNAL_GRANDE";
          break;
        case 12:
          str = "BAC_TILING_INTERNAL_COMPOSITE";
          break;
        case 13:
          str = "CYTOGENETIC";
          break;
        case 14:
          str = "CYTOGENETIC_WITH_GENES_AND_OTHER_MARKERS";
          break;
        case 15:
          str = "INTERNAL";
          break;
        case 16:
          str = "CLONE";
          break;
        default:
          str = "Unknown";
      }
      return str;
    }

    public boolean equals(Object otherType)
    {
      if (!(otherType instanceof AxisType))
      {
        return false;
      }
      else
      {
        return this.value == ((AxisType)otherType).value;
      }
    }

  public int hashCode() {
    return value;
  }
}

