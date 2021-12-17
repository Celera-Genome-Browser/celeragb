// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//Title:        SearchAndReplace
//Version:
//Author:       Peter Davies
//Company:      []
//Description:  This program allows for replacing one string with another in a text file
//Modification: LLF, 3/1/2000 -- moved to client.tools package

package client.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;

public class SearchAndReplace {

  public SearchAndReplace(String filename,String searchString, String replaceString) {
    boolean write=false;
    File file=new File(filename);
    RandomAccessFile randFile=null;
    try {
       randFile=new RandomAccessFile(file,"rw");
    }
    catch (Exception e) {
       System.out.println("Error!!  File not found");
       return;
    }

    String readString;
    try {
       byte[] bytes=new byte[(int)randFile.length()];
       randFile.readFully(bytes,0,(int)randFile.length());
       readString=new String(bytes);
       StringBuffer buf=new StringBuffer(readString);
       while (readString.indexOf(searchString)>0) {
          buf.replace(readString.indexOf(searchString),readString.indexOf(searchString)+searchString.length(),replaceString);
          readString=buf.toString();
          write=true;
       }

       randFile.close();
    }
    catch (Exception ex) {
       System.out.println("Error!!  File read error");
       return;
    }

    // Write the resulting, fully-adjusted string, back to a new version of the
    // input file.
    if (write) {
       try {
          FileWriter fw = new FileWriter(filename);
          fw.write(readString);
          fw.flush();
          fw.close();
       }
       catch  (Exception ex) {
          System.out.println("Error!!  File write error");
          return;
       }
    } // Writeback required.

    System.out.println("All occurances found and replaced");
  }

  public static void main(String[] args) {
    if (args.length!=3) {
       System.out.println("Usage: client.tools.SearchAndReplace <filename> <searchString> <replaceString>");
       System.out.println("This program will look for <searchString> in a text file and replace it with the <replaceString>.");
       return;
    }
    SearchAndReplace searchAndReplace = new SearchAndReplace(args[0],args[1],args[2]);
    searchAndReplace.invokedStandalone = true;
  }

  private boolean invokedStandalone = false;
}