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
package client.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

public class FileUpdater {

   File updateScript;
   URL updateScriptURL;
   boolean urlLoading;
   ProgressDisplayer splash = null;
   static private boolean invokedStandalone = false;
   String localRootDir = null;

   public FileUpdater(String url, ProgressDisplayer pd, String lrd) {
      System.out.println("FileUpdater(" + lrd + ")");
      splash = pd;
      localRootDir = lrd;
      System.setProperty("updateScript", url);

      if (splash == null)
         splash = new SplashScreen();
//      splash.setVisible(true);
      splash.setLabel("Checking for updates ... ");
   }

   public FileUpdater() {
      init();
   }

   public void init() {
      try {
         String updateFile = System.getProperty("updateScript");

         if (localRootDir == null) {
            localRootDir = System.getProperty("localRootDir");
         }

         if (updateFile.toLowerCase().startsWith("http:") || updateFile.toLowerCase().startsWith("file:")) {
            updateScriptURL = new URL(updateFile);
            urlLoading = true;
         }
         else {
            updateScript = new File(updateFile);
         }
      }
      catch (Exception ex) {
         System.out.println("Usage: java -DupdateScript=http:\\\\localhost\\updateScript.scr tools.FileUpdater");
         System.exit(1);
      }

      update();

      if (splash != null) {
         splash.setProgress(100, 100);
         splash.setLabel("Starting Genome Browser ...");
         splash.dispose();
      }

      if (invokedStandalone)
         System.exit(0);
   }

   private void showMessage(String updatedFile, int cur, int max) {
      if (splash == null)
         splash = new SplashScreen();
      splash.setVisible(true);
      splash.setLabel("Updating ... " + updatedFile);
      splash.setProgress(cur, max);
   }

   private void update() {
      Vector fileUpdates = new Vector();
      BufferedReader reader = null;
      try {
         if (urlLoading) {
            reader = new BufferedReader(new InputStreamReader(updateScriptURL.openStream()));
         }
         else {
            reader = new BufferedReader(new FileReader(updateScript));
         }
      }
      catch (FileNotFoundException fnfEx) {
         System.out.println("Update Script not found... Exiting");
         System.exit(1);
      }
      catch (IOException ioEx) {
         System.out.println("IOException reading script file... Exiting");
         System.exit(1);
      }

      while (true) {
         try {
            String str = reader.readLine();
            if (str == null)
               break;
            fileUpdates.add(new FileUpdate(str, localRootDir));
         }
         catch (IOException ioEx) {
            break;
         }
      }
      int numUpdatedFiles = 0;
      for (int i = 0; i < fileUpdates.size(); i++) {
         if (((FileUpdate) fileUpdates.elementAt(i)).processUpdate(i, fileUpdates.size()))
            numUpdatedFiles++;
      }
      System.out.println("Updated " + numUpdatedFiles + " files.");
   }

   public static void main(String[] args) {
      if (args.length == 1) {
         //Set standalone
         FileUpdater.invokedStandalone = false;
         System.setProperty("updateScript", args[0]);
      }
      else {
         FileUpdater.invokedStandalone = true;
         FileUpdater fileUpdater = new FileUpdater(System.getProperty("updateScript"),null,System.getProperty("localRootDir"));
         fileUpdater.init();
      }
   }

   class FileUpdate {
      File localFile;
      File remoteFile;
      URL remoteURL;
      long remoteFileTime;
      boolean delete;
      boolean dir;
      boolean urlLoading;
      String rootDir = "";

      public FileUpdate(String updateMessage, String root) {
         rootDir = root;
         parseMsg(updateMessage);
      }

      public FileUpdate(String updateMessage) {
         parseMsg(updateMessage);
      }

      private void parseMsg(String updateMessage) {
         localFile = new File(rootDir + updateMessage.substring(0, updateMessage.indexOf(',')));
         System.out.println("Writing to: " + localFile.toString());
         String file = updateMessage.substring(updateMessage.indexOf(',') + 1, updateMessage.lastIndexOf(','));
         
         System.out.println("parseMsg("+updateMessage+") file="+ file);
         
         if (file.toLowerCase().startsWith("http:") || file.toLowerCase().startsWith("file:")) {
            urlLoading = true;
            try {
               remoteURL = new URL(updateMessage.substring(updateMessage.indexOf(',') + 1, updateMessage.lastIndexOf(',')).replace('\\', '/'));
            }
            catch (MalformedURLException mURL) {
               System.out.println("Error! Bad URL in update file");
               return;
            }
         }
         else {
         	//Use the updateScript.scr URI (dynamic address)
			String updateFile = System.getProperty("updateScript");
            urlLoading = true;
            try {
               remoteURL = new URL( (updateFile.substring(0,updateFile.lastIndexOf('/')+1) + file).replace('\\', '/'));
            }
            catch (MalformedURLException mURL) {
               System.out.println("Error! Bad URL in update file");
               return;
            }
         }

         String remoteString = updateMessage.substring(updateMessage.lastIndexOf(',') + 1, updateMessage.length());
         if (remoteString.equalsIgnoreCase("delete") || remoteString.equalsIgnoreCase("del")) {
            delete = true;
            return;
         }
         if (remoteString.equalsIgnoreCase("dir") || remoteString.equalsIgnoreCase("directory")) {
            dir = true;
            return;
         }
         remoteFileTime = Long.parseLong(remoteString);
      }

      public boolean processUpdate(int current, int max) {
         if (delete) {
            localFile.delete();
            System.out.println("Deleting: " + localFile.getPath());
            return true;
         }
         if (dir) {
            if (localFile.exists())
               return false;
            localFile.mkdirs();
            return true;
         }
         if (localFile.exists() && (localFile.lastModified() > remoteFileTime - 5000 && localFile.lastModified() < remoteFileTime + 5000))
            return false; //up to date

         System.out.println("Updating: " + localFile.getPath() + " from " + remoteURL);
         showMessage(localFile.getName(), current, max);
         //System.out.println("Localfile: "+new Date(localFile.lastModified())+"  Remote: "+new Date(remoteFileTime));
         byte[] fileBuf = new byte[1024 * 128];
         int bytesRead = 0;
         InputStream inStream;
         try {
            if (urlLoading)
               inStream = remoteURL.openStream();
            else
               inStream = new FileInputStream(remoteFile);
         }
         catch (FileNotFoundException fne) {
            if (urlLoading)
               System.out.println("Error!! Remote file (" + remoteURL.getRef() + ") not found and must be updated!!");
            else
               System.out.println("Error!! Remote file (" + remoteFile.getAbsolutePath() + ") not found and must be updated!!");
            return false;
         }
         catch (IOException ioEx) {
            System.out.println("Error!! Remote file not found and must be updated!!");
            return false;
         }
         if (!localFile.exists()) {
            try {
               if (!localFile.createNewFile()) {
                  System.out.println("Error!! There is a remote file that cannot be created locally");
                  System.exit(1);
               }
            }
            catch (IOException ioEx) {
               System.out.println("Error!! There is a remote file that cannot be created locally");
               System.exit(1);
            }
         }
         try {
            FileOutputStream outStream = new FileOutputStream(localFile);
            while (bytesRead != -1) {
               bytesRead = inStream.read(fileBuf);
               if (bytesRead != -1)
                  outStream.write(fileBuf, 0, bytesRead);
            }
            outStream.flush();
            outStream.close();
            localFile.setLastModified(remoteFileTime);
         }
         catch (IOException ioEx) {
            System.out.println("Error!! IOException during copy: " + ioEx.getMessage());
            System.exit(1);
         }
         return true;
      }
   }
}