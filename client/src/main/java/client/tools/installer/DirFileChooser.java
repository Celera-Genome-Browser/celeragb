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
package client.tools.installer;

import javax.swing.*;
import java.io.File;

public class DirFileChooser extends JFileChooser {
  private DirectoryFileFilter dirFileFilter=new DirectoryFileFilter();

  public DirFileChooser() {}

  /**
   * @throws IllegalArguementException -
   *  if a File shortcut has been added and DIRECTORIES_ONLY has been ser
   */
  public void setFileSelectionMode(int mode) {
    if (mode==DIRECTORIES_ONLY) {
      setApproveButtonText("Select Directory");
    }
    if (mode==FILES_AND_DIRECTORIES) setApproveButtonText("Select");
    if (mode==FILES_ONLY) setApproveButtonText("Select File");

    super.setFileSelectionMode(mode);
    //Must be done after the super call.
    if (mode==DIRECTORIES_ONLY) {
      //Ensure we are not in a directory with no subDirectories.
      setCurrentDirectory(getCurrentDirectory());
    }
  }

  /**
   * Override to prevent the user from getting into a directory that has
   * no subDirectories if the mode if DIRECTORIES_ONLY
   */
  public void setCurrentDirectory(File dir) {
    if (dir!=null) {
      if (getFileSelectionMode()==JFileChooser.DIRECTORIES_ONLY) {
        File[] subFiles=dir.listFiles(dirFileFilter);
        if (subFiles==null || subFiles.length==0) {
           setSelectedFile(dir);
           return;
        }
      }
    }
    super.setCurrentDirectory(dir);
  }

  class DirectoryFileFilter implements java.io.FileFilter {
      public boolean accept(File f){
        return f.isDirectory();
      }
  }


}