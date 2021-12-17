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
package shared.util;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

import java.io.File;

/**
 * Static "bag o' utils" for file handling.
 */
public class FileUtilities {

  //------------------------------------------CONSTANTS
  private static final String DIRECTORY_READ_LOCK_MESSAGE = "Please Choose Another Directory: Access Forbidden";
  private static final String DIRECTORY_NOT_DIRECTORY = "Please Choose A Directory Only: Directory Not Chosen";
  private static final String NONEXISTENT_DIRECTORY_MESSAGE = "You have choosen a directory that does not exist";

  private static final String DIRECTORY_READ_LOCK_FAILED = "Access to Directory Forbidden";
  private static final String DIRECTORY_NOT_DIRECTORY_FAILED = "Not a Directory";
  private static final String NONEXISTENT_DIRECTORY_FAILED = "Directory Does Not Exist";

  //------------------------------------------CONSTRUCTORS [DEAD]
  private FileUtilities() {
    // No instance will EVER be made.
  } // End dead constructor

  //NOTE: if developers add utilities here, they are free to make constructors,
  // if any of them ever require state.  Cannot imagine why, but....

  //------------------------------------------CLASS METHODS
  /** Use this to open the directory, so that consistent checking can be done. */
  public static File openDirectoryFile(String directoryName) {
    File directoryFile = null;
    try {
      directoryFile = new File(directoryName);

      if (! directoryFile.exists())
        throw new IllegalArgumentException(NONEXISTENT_DIRECTORY_MESSAGE);

      if (! directoryFile.canRead())
        throw new IllegalArgumentException(DIRECTORY_READ_LOCK_MESSAGE);

      if (! directoryFile.isDirectory())
          throw new IllegalArgumentException(DIRECTORY_NOT_DIRECTORY);

    } catch (IllegalArgumentException iex) {
      directoryName = null;
      throw iex;
    } catch (Exception ex) {
      directoryName = null;
      throw new IllegalArgumentException(ex.getMessage());
    } // Null out directory to avoid repeated throws.

    return directoryFile;
  } // End method: openDirectoryFile

  /** Just test the directory. Aim is to see if it could be opened. */
  public static void testDirectoryFile(String directoryName) {
    File directoryFile = null;
    try {
      directoryFile = new File(directoryName);

      if (! directoryFile.exists())
        throw new IllegalArgumentException(NONEXISTENT_DIRECTORY_FAILED);

      if (! directoryFile.canRead())
        throw new IllegalArgumentException(DIRECTORY_READ_LOCK_FAILED);

      if (! directoryFile.isDirectory())
          throw new IllegalArgumentException(DIRECTORY_NOT_DIRECTORY_FAILED);

    } catch (IllegalArgumentException iex) {
      directoryName = null;
      throw iex;
    } catch (Exception ex) {
      directoryName = null;
      throw new IllegalArgumentException(ex.getMessage());
    } // Null out directory to avoid repeated throws.

  } // End method: testDirectoryFile

} // End class
