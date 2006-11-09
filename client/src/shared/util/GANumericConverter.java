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
 * Description:  Converter utility to translate both ways between internal OIDs and GA #s<p>
 * Company:      []<p>
 * @author Les Foster
 * @version
 */
package shared.util;

public class GANumericConverter {

  //----------------------CONSTANTS
  // Constant for use by clients.
  public static final String GA_PREFIX = "GA_x";
  private static final String POSITIONAL_CONVERSION_MAP = "0123456789ABCDEFGHJKLMNPQRSTUVWYZ";

  //----------------------CONSTRUCTORS
  /** Singleton */
  private static GANumericConverter converter = null;
  private GANumericConverter() {
  } // End constructor

  /** Returns the one-and-only converter. */
  public static GANumericConverter getConverter() {
      if (converter == null)
          converter = new GANumericConverter();
      return converter;
  } // End method: getConverter

  //----------------------PUBLIC INTERFACE
  /** Converts a GA Name to an OID numeric suffix. */
  public long getOIDValueForGAName(String gaName) {

      // Check: starts with prefix?
      //  This converter assumes that if the prefix is not there, this
      //  input string is already an OID.
      if (gaName.startsWith(GA_PREFIX))
          return gaSuffixToOIDLong(gaName.substring(GA_PREFIX.length()));
      else
          return Long.parseLong(gaName);

  } // End method: getOIDSringForGAName

  /** Converts a GA Name, with or without prefix, to an OID numeric suffix. */
  public long getOIDValueForGA(String gaName) {

      // Check: starts with prefix?
      //  This converter assumes that if the prefix is not there, this
      //  input string is still a GA name.
      if (gaName.startsWith(GA_PREFIX))
          gaName = gaName.substring(GA_PREFIX.length());
      return gaSuffixToOIDLong(gaName);

  } // End method: getOIDSringForGA

  /** Converts an oid suffix to a GA name. */
  public String getGANameForOIDSuffix(String oidSuffix) {
      long tempUID = 0L;
      int digit = 0;
      String returnString = "";
      try {
          tempUID = Long.parseLong(oidSuffix);
      } catch (NumberFormatException nfe) {
          throw new IllegalArgumentException("Bad OID Suffix "+oidSuffix+
             " Passed to "+this.getClass().getName());
      } // End catch block for numeric conversion.

      // Iterate by powers of 32.
      while (tempUID != 0) {
          digit = (int)(tempUID % 32L);
          returnString = POSITIONAL_CONVERSION_MAP.charAt(digit) + returnString;
          tempUID = tempUID / 32L;
      } // Until all powers of 32 dealt with.

      return (GA_PREFIX + returnString);
  } // End method: getGANameForOIDSuffix

  //----------------------HELPER METHODS
  /**
   * Helper method which carries out conversion of a known GA number
   * suffix part into an OID number.
   */
  private long gaSuffixToOIDLong(String gaSuffix) {
      int value = 0;
      long oidNumber = 0L;
      for (int i = 0; i < gaSuffix.length(); i++) {
          value = POSITIONAL_CONVERSION_MAP.indexOf(gaSuffix.charAt(i));
          oidNumber *= 32;
          oidNumber += value;
      } // For all charactes of input.

      return oidNumber;
  } // End method: gaSuffixToOIDLong

  //----------------------UNIT-TEST CODE
  /** Main method makes standalone application for unit test. */
  public static void main(String[] args) {
      // Test cases.
      String s1 = "GA_xL0";
      System.out.println("Converting "+s1+" "+GANumericConverter.getConverter().getOIDValueForGAName(s1));

      String s2 = "33521";
      System.out.println("Converting "+s2+" "+GANumericConverter.getConverter().getOIDValueForGA(s2));

      String s3 = "335001033774513";
      System.out.println("Converting "+s3+" "+GANumericConverter.getConverter().getGANameForOIDSuffix(s3));

      String s4 = "17000017770305";
      System.out.println("Converting "+s4+" "+GANumericConverter.getConverter().getGANameForOIDSuffix(s4));

      String s5 = "285347054";
      System.out.println("Converting "+s5+" "+GANumericConverter.getConverter().getGANameForOIDSuffix(s5));

  } // End main program
} // End class: GANumericConverter
