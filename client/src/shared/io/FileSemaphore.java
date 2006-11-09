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
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/

package shared.io;

import java.io.File;

/**
* Implements a semaphore machanism that can be used by multiple processes
* that want read-write access to the same file on disk. Takes care
* of creating a deleting the semaphore that guards access to the named file
* and will cleanup the semaphore in cases that the calling processes
* crashes or exits in any sort of abnormal manner.
*
*  Initially Written by: James Baxendale
*
*/
public class FileSemaphore  {

  private File semaphoreFile = null;
  private long semaphoreTimeoutMillis = 300000; // 5 mins by default
  private boolean semaphoreAquired = false;
  private boolean readOnlyAccess = true;
  /**
   * Creates a semaphore that will control access to the named file. If the
   * semaphore has existed for more than the timeout specified it will be
   * removed (cleaned up) and a new semaphore created.
   *
   * @param filename whose access should be guarded by a semaphore
   * @param timeoutAfterMillis number of seconds after which the
   * semaphore file should be overriden - this exists to take care of
   * exception cases where one user of the semaphore never releases it.
   * This avoids deadlocks on access to files.
   * @param isReadOnly indicates whether the FileSemaphore should coordinate
   * access from a read only or a read-write perspective
   */
  public FileSemaphore(String fileToGuard, long timeoutAfterMillis, boolean isReadOnly) {
    semaphoreFile = new File(fileToGuard + ".sem");
    this.semaphoreTimeoutMillis = timeoutAfterMillis;
    this.readOnlyAccess = isReadOnly;

    init();

  }

  private void init() {
    // Try to create the semaphore file, if it cant be created because
    // it already exists then check to see if it is older than the
    // timeout, if it is then re-use it with an updated last modified
    // time.
    try {
      if (readOnlyAccess) {
        if (!semaphoreFile.exists()) {
          semaphoreAquired = true;
        }
      }
      else { // write access
        if (!semaphoreFile.createNewFile()) {
          long lastModified = semaphoreFile.lastModified();
          if (System.currentTimeMillis()-semaphoreTimeoutMillis > lastModified) {
            // Update the last modified time to reflect the current
            // time and re-use the semaphore file
            semaphoreFile.setLastModified(System.currentTimeMillis());
          }
        }
        else {
          semaphoreAquired = true;
        }
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
      semaphoreAquired = false;
    }
  }

  /**
   * Allows a client to determine if the semaphore could be
   * aquired.
   * @return true if the semaphore was aquired, false otherwise.
   */
  public boolean semaphoreAquired() {
    return semaphoreAquired;
  }

  /**
   * Allows a client to try and reaquire the semaphore in cases were
   * it could not be aquired opon creation. Note that this method only
   * needs to be called if semaphoreAquired() returns false after initial
   * FileSemaphore construction.
   * @return true if the semaphore was aquired, false otherwise
   */
  public boolean aquireSemaphore() {
    init();
    return this.semaphoreAquired;
  }

  /**
   * Releases the semaphore on the underlying guarded file so that other
   * processes that are guarding access to the same file using this same
   * semaphore class will be able to manipulate the file
   * @return true if the semaphore could be released, false otherwise.
   */
  public boolean releaseSemaphore() {
    // If the semaphore was not successfully aquired in the first
    // place then return true as it can certainly be released! also
    // if the semaphore was read only then there is no work to do to
    // release it
    if ((!semaphoreAquired) || (readOnlyAccess)) {
      return true;
    }

    boolean couldRelease = false;
    try {
      if (!semaphoreFile.delete()) {
        System.out.println("Unexpectedly could not delete semaphore file " +
          semaphoreFile.getName());
      }
      else {
        couldRelease = true;
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return couldRelease;
  }
}

