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
 *  A class to filter out all file names in FileChooser, except those
 *  that end with a given extension
 */
public class ExtensionFileFilter extends javax.swing.filechooser.FileFilter {
    String extension_str;
    String description;

    public ExtensionFileFilter(String filterDescription, String extension_str) {
        this.extension_str = extension_str.toLowerCase();
        description = filterDescription;
    }

    public boolean accept(File fl) {
        if (fl.isDirectory()) {
            return true;
        }

        String fname = fl.getName().toLowerCase();

        if (fname.endsWith(extension_str)) {
            return true;
        }

        return false;
    }

    public String getDescription() {
        return description;
    }
}