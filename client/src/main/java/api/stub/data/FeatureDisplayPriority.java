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

import java.util.ResourceBundle;

public class FeatureDisplayPriority implements java.io.Serializable
{
    public static FeatureDisplayPriority LOW_PRIORITY;
    public static FeatureDisplayPriority HIGH_PRIORITY;
    public static FeatureDisplayPriority ALL;
    public static byte DEFAULT_PRIORITY = 0;

    private byte minPriority;
    private byte maxPriority;

    /**
     * Static initializer....
     */
    static {
       try {
            String propsFile=System.getProperty("x.genomebrowser.FeatureDisplayProperties");
            if (propsFile!=null) {
              ResourceBundle rb = ResourceBundle.getBundle(propsFile);
              HIGH_PRIORITY = new FeatureDisplayPriority(getMinDisplayPriority(rb,true),
                                                         getMaxDisplayPriority(rb,true));
              LOW_PRIORITY = new FeatureDisplayPriority(getMinDisplayPriority(rb,false),
                                                        getMaxDisplayPriority(rb,false));
              ALL = new FeatureDisplayPriority(Byte.MIN_VALUE, Byte.MAX_VALUE);
            }
        }
        catch(Exception ex) {
            System.err.println("problem in FeatureDisplayPriority static class initializer");
        }
    }


    /**
     * Static method to get which FeatureDisplayPriority includes the passed
     * display priority...
     * Will return ALL otherwise.
     * @todo: public static FeatureDisplayPriority featureDisplayPriorityFor(short priority);
     */
    public static FeatureDisplayPriority featureDisplayPriorityFor(byte testPriority) {
      // Check LOW.
      if (LOW_PRIORITY.contains(testPriority)) return LOW_PRIORITY;
      // Check HIGH.
      if (HIGH_PRIORITY.contains(testPriority)) return HIGH_PRIORITY;
      // Otherwise return all...
      return ALL;
    }


    /**
     * Constructor with no args.
     */
    public FeatureDisplayPriority()
    {
    }


    /**
     * Constructor with full args.
     */
    public FeatureDisplayPriority(byte aMinPriority, byte aMaxPriority)
    {
        minPriority = aMinPriority;
        maxPriority = aMaxPriority;
    }


    /**
     * Constructor with full args.
     */
    public FeatureDisplayPriority
        (byte minPriority,
        boolean minPriorityInclusive,
        byte maxPriority,
        boolean maxPriorityInclusive)
    {
        this((minPriorityInclusive ? minPriority : ++minPriority),
             (maxPriorityInclusive ? maxPriority : --maxPriority));
    }


    /**
     * Accessors
     */
    public byte getMinPriority() { return minPriority; }
    public byte getMaxPriority() { return maxPriority; }


    /**
     * Contianment test for a priority.
     * @returns true if this FeatureDisplayPriority contains the parameter
     * priority.
     */
    public boolean contains(byte priority) {
      // Check less than minimum...
      if (priority < minPriority) return false;
      // Check for greater than maximum...
      if (priority > maxPriority) return false;
      // This includes the priority.
      return true;
    }


    /**
     * Containment test against a another FeatureDisplayPriority.
     * @returns true if this FeatureDisplayPriority contains the parameter
     * otherDisplayPriority.
     */
    public boolean contains(FeatureDisplayPriority otherDisplayPriority) {
      // Check if this contains the minimum of the other...
      if (!this.contains(otherDisplayPriority.getMinPriority())) return false;
      // Check if this contains the maximum of the other...
      if (!this.contains(otherDisplayPriority.getMaxPriority())) return false;
      // This includes the other.
      return true;
    }


    /**
     * Intersection stest against another FeatureDisplayPriority.
     * @returns true if the parameter otherDisplayPriority instersects this
     * FeatureDisplayPriority.
     */
    public boolean intersects(FeatureDisplayPriority otherDisplayPriority) {
      // Check if this contains the minimum of the other...
      if (this.contains(otherDisplayPriority.getMinPriority())) return true;
      // Check if this contains the maximum of the other...
      if (this.contains(otherDisplayPriority.getMaxPriority())) return true;
      // This does not intersect the other.
      return false;
    }


    /**
     * Equals test.
     */
    public boolean equals(Object other) {
       if (!(other instanceof FeatureDisplayPriority)) return false;
       FeatureDisplayPriority otherDispayPriority = (FeatureDisplayPriority) other;
       if (minPriority == otherDispayPriority.minPriority &&
           maxPriority == otherDispayPriority.maxPriority) {
           return true;
       }
       return false;
    }


    private static byte getMaxDisplayPriority(ResourceBundle rb, boolean hiPriority) {
        String priority = null;
        if (hiPriority) priority = rb.getString("MaxHiPriority");
        else
            priority = rb.getString("MaxLowPriority");
        if (priority == null) return Byte.MAX_VALUE;
        try {
            return Byte.decode(priority).byteValue();
        }
        catch(Exception ex) {
            return Byte.MAX_VALUE;
        }
    }


    private static byte getMinDisplayPriority(ResourceBundle rb,boolean hiPriority) {
        String priority;
        if (hiPriority) priority = rb.getString("MinHiPriority");
        else
            priority = rb.getString("MinLowPriority");
        if (priority == null) return Byte.MIN_VALUE;
        try {
            return Byte.decode(priority).byteValue();
        }
        catch(Exception ex) {
            return Byte.MIN_VALUE;
        }
    }
}