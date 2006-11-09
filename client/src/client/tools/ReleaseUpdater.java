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
//Title:        Release Updater
//Version:
//Author:       Peter Davies
//Company:      []
//Description:  This program allows for replacing $date$ with the current date in a text file
//Modifications:  LLF, 3/1/2000 -- Moved to "client.tools package
package client.tools;

import java.io.*;
import java.util.*;

public class ReleaseUpdater {

  public ReleaseUpdater(String filename) {
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
     String date=new Date().toString();
     try {
         byte[] bytes=new byte[(int)randFile.length()];
         randFile.readFully(bytes,0,(int)randFile.length());
         readString=new String(bytes);
         StringBuffer buf=new StringBuffer(readString);
         while (readString.indexOf("$Date$")>0) {
           buf.replace(readString.indexOf("$Date$"),readString.indexOf("$Date$")+6,date);
           readString=buf.toString();
           write=true;
         }
         while (readString.indexOf("$date$")>0) {
           buf.replace(readString.indexOf("$date$"),readString.indexOf("$date$")+6,date);
           readString=buf.toString();
           write=true;
         }
         while (readString.indexOf("$DATE$")>0) {
           buf.replace(readString.indexOf("$DATE$"),readString.indexOf("$DATE$")+6,date);
           readString=buf.toString();
           write=true;
         }
     }
     catch (Exception ex) {
       System.out.println("Error!!  File read error");
       return;
     }
     try {
       randFile.seek(0);
       if (write) randFile.writeBytes(readString);
     }
     catch  (Exception ex) {
       System.out.println("Error!!  File write error");
       return;
     }
     System.out.println("All occurances found and replaced");
  }

  public static void main(String[] args) {
    if (args.length!=1) {
       System.out.println("Usage: tools.ReleaseUpdater <filename>");
       System.out.println("This program will look for $date$ in a text file and replace it with the current date.");
       return;
    }
    ReleaseUpdater releaseUpdater = new ReleaseUpdater(args[0]);
    releaseUpdater.invokedStandalone = true;
  }

  private boolean invokedStandalone = false;
}