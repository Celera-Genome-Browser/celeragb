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

public class AxisUnit implements java.io.Serializable
{
    private int value;

    public static AxisUnit UNIT_BASE_PAIR=new AxisUnit(0);
    public static AxisUnit UNIT_cR_3000=new AxisUnit(1);
    public static AxisUnit UNIT_R_10000=new AxisUnit(2);
    public static AxisUnit UNIT_cM=new AxisUnit(3);
    public static AxisUnit UNIT_ORDER=new AxisUnit(4);
    public static AxisUnit UNIT_BIN=new AxisUnit(5);
    public static AxisUnit UNIT_cm=new AxisUnit(6);
    public static AxisUnit UNIT_10000xcR3000=new AxisUnit(7);
    public static AxisUnit UNIT_10000xcM=new AxisUnit(8);
    public static AxisUnit UNIT_10000xcm=new AxisUnit(9);

    public static String axisUnitNameForValue(int value)
    {
      String name;
      switch (value)
      {
        case 0:
          name = "bp";
          break;
        case 1:
          name = "cR3000";
          break;
        case 2:
          name = "R10000";
          break;
        case 3:
          name = "cM";
          break;
        case 4:
          name = "Order";
          break;
        case 5:
          name = "Bin";
          break;
        case 6:
          name = "cm";
          break;
        case 7:
          name = "10000xcR3000";
          break;
        case 8:
          name = "10000xcM";
          break;
        case 9:
          name = "10000xcm";
          break;
        default:
          name = "Error - Invalid Data";
          break;
      }
      return name;
    }

    public AxisUnit
        (int value)
    {
        this.value = value;
    }

    // Allows construction using reflection
    public AxisUnit()
    {
      value = -1;
    }

}

