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

/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/

import java.util.*;

/**
 * Creates a mapping of reserved name space internal to external names.  Maps
 * these in both directions.
 */
public class ReservedNameSpaceMapping {

  private static final String RESOURCE_FILE_NAME = "resource.shared.NameSpaceTranslations";

  // Maintenance note:  to remove mappings, add mappings, or otherwise
  // change mappings, do so only in the associated RESOURCE FILE.
  // The changes will automatically be reflected in the
  // inverted mapping.

  private static Map externalToInternal;
  private static Map internalToExternal;

  // NOTE: need a static initializer since no constructor will be called!
  static {

    try {
      // Section to define external versus internal mappings.
      externalToInternal = new HashMap();
      String mapping = null;
      try {
        ResourceBundle rb = ResourceBundle.getBundle(RESOURCE_FILE_NAME);
        try {
          mapping = rb.getString("Internal_NameSpace");
          externalToInternal.put(mapping, OID.INTERNAL_DATABASE_NAMESPACE);
        } catch (MissingResourceException mre) {
          externalToInternal.put("INTERNAL", OID.INTERNAL_DATABASE_NAMESPACE);
        } // End catch block for picking up resources.

        try {
          mapping = rb.getString("Scratch_NameSpace");
          externalToInternal.put(mapping, OID.SCRATCH_NAMESPACE);
        } catch (MissingResourceException mre) {
          externalToInternal.put("SCRATCH", OID.SCRATCH_NAMESPACE);
        } // End catch block for picking up resources.

        try {
          mapping = rb.getString("Unknown_NameSpace");
          externalToInternal.put(mapping, OID.UNKNOWN_NAMESPACE);
        } catch (MissingResourceException mre) {
          externalToInternal.put("UNKNOWN", OID.UNKNOWN_NAMESPACE);
        } // End catch block for picking up resources.

        try {
          mapping = rb.getString("ServerGenerated_NameSpace");
          externalToInternal.put(mapping, OID.SERVER_GENERATED_NAMESPACE);
        } catch (MissingResourceException mre) {
          externalToInternal.put("SERVER_GENERATED", OID.SERVER_GENERATED_NAMESPACE);
        } // End catch block for picking up resources.

      } catch (MissingResourceException e) {
        System.out.println("Error reading namespace mappings resource file "+RESOURCE_FILE_NAME);
        System.out.println("...internal/external namespace mappings may fail.");

      } // End catch block for reading up resources.

      // Section to define internal versus external mappings.
      // NOTE here: using this looping prevents mismatching the settings above,
      // and allows an easy add in one place (above).  However, if the
      // mappings ever STOP being one-to-one, this part will fail.
      internalToExternal = new HashMap();
      Iterator iterator = externalToInternal.keySet().iterator();
      String nextKey = null;
      while (iterator.hasNext()) {
        nextKey = (String)iterator.next();
        internalToExternal.put(externalToInternal.get(nextKey),
          nextKey);
      } // For all iterations.

    } catch (Exception ex) {
      // NOTE: we expect never to enter this block.  However, a failure
      // in a static initializer will cause the entire class load for this
      // class to fail in an ungainly manner.  May be hard to find.
      System.out.println("ERROR: failed to load ReservedNameSpaceMapping class");
      ex.printStackTrace();
    } // End catch block for whole initializer.
  } // End mapping initializer.

  /**
   * Privatized constructor enforces static "bag-o-functions" use.
   */
  private ReservedNameSpaceMapping() { }

  /**
   * Translation to map <emph>external</emph> names seen by the loader to
   * names used in the OID.
   *
   * @param String namespace loader-read namespace prefix.
   * @return String translated reserved string value.
   */
  public static String translateToReservedNameSpace(String namespace) {
    String returnVal;
    if (null != (returnVal = (String)externalToInternal.get(namespace)))
      return returnVal;
    else
      return namespace;

  } // End method: translateToReservedNameSpace

  /**
   * Translation to map <emph>internal<emph> names seen by the OID to
   * <emph>external</emph> ones like those written to files.
   *
   * @param String namespace OID's internal namespace prefix.
   * @return String translated external value as might be found in file.
   */
  public static String translateFromReservedNameSpace(String namespace) {
    String returnVal;
    if (null != (returnVal = (String)internalToExternal.get(namespace)))
      return returnVal;
    else
      return namespace;
  } // End method: translateFromReservedNameSpace

} // End class: ReservedNameSpaceMapping
