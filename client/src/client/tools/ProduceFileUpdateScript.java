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
package client.tools;

import java.io.File;


public class ProduceFileUpdateScript {
    String serverBase;
    String localBase;
    File codeBase;
    String exclude;
    private boolean excludeEnabled = false;
    private boolean invokedStandalone = false;

    public ProduceFileUpdateScript() {
        try {
            serverBase = new String(System.getProperty("serverBase"));
            localBase = new String(System.getProperty("localBase"));
            exclude = new String(System.getProperty("exclusion"));

            if ((System.getProperty("exclusion") != null) && 
                    (exclude.length() > 0)) {
                excludeEnabled = true;
            }

            if (System.getProperty("codeBase") == null) {
                codeBase = new File(System.getProperty("serverBase"));
            } else {
                codeBase = new File(System.getProperty("codeBase"));
            }
        } catch (Exception ex) {
            System.out.println(
                    "Usage: java -DcodeBase=\\\\serverX\\file-exchange\\Visualization\\GenomicBrowser " + 
                    "-DlocalBase=c:\\GenomeBrowser -DserverBase=http:\\\\daviespe.xxxxx.com\\eap tools.ProduceFileUpdateScript > c:\\updateScript.scr");
            System.exit(1);
        }

        parsePaths(codeBase);
    }

    private void parsePaths(File rootFile) {
        if (rootFile.isDirectory()) {
            String lineOut = localBase + 
                             getRelPath(codeBase.getAbsolutePath(), 
                                        rootFile.getAbsolutePath()) + 
                             ",,dir";

            if (excludeEnabled && !(lineOut.indexOf(exclude) >= 0) && 
                    !(lineOut.indexOf("CVS") >= 0)) {
                System.out.println(lineOut);
            } else if (!excludeEnabled && !(lineOut.indexOf("CVS") >= 0)) {
                System.out.println(lineOut);
            }

            File[] files = rootFile.listFiles();

            for (int i = 0; i < files.length; i++) {
                parsePaths(files[i]);
            }
        } else {
            String lineOut = localBase + 
                             getRelPath(codeBase.getAbsolutePath(), 
                                        rootFile.getAbsolutePath()) + "," + 
                             serverBase + 
                             getRelPath(codeBase.getAbsolutePath(), 
                                        rootFile.getAbsolutePath()) + "," + 
                             rootFile.lastModified();

            if (excludeEnabled && !(lineOut.indexOf(exclude) >= 0) && 
                    !(lineOut.indexOf("CVS") >= 0)) {
                System.out.println(lineOut);
            } else if (!excludeEnabled && !(lineOut.indexOf("CVS") >= 0)) {
                System.out.println(lineOut);
            }
        }
    }

    private String getRelPath(String basePath, String file) {
        if (file.startsWith(basePath)) {
            return file.substring(basePath.length(), file.length());
        } else {
            return null;
        }
    }

    public static void main(String[] args) {
        ProduceFileUpdateScript produceFileUpdateScript = 
                new ProduceFileUpdateScript();
        produceFileUpdateScript.invokedStandalone = true;
    }
}