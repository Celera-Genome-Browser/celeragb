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

/**
 * Title:
 * Description:
 * Company:
 * @author
 * @version 1.0
 */
import java.io.File;
import java.io.FilenameFilter;

import java.util.StringTokenizer;
import java.util.Vector;


public class FileScanner {
    String[] defaultDrive = { "C:" };
    String[] defaultFilter = { "*.*" };
    Vector matchingFiles = new Vector();
    FilenameFilter searchFilter = null;
    int dirCount = 0;
    int fileCount = 0;
    boolean caseSensitiveSearch = true;
    long start = 0;
    boolean stopOnFirstHit = false;

    public FileScanner() {
        start = System.currentTimeMillis();
    }

    public void printSummary() {
        long time = System.currentTimeMillis() - start;
        int timeSec = 0;
        timeSec = ((time / 1000) == 0) ? 1 : (int) (time / 1000);
        System.out.print("Searched " + dirCount + " directories. ");
        System.out.println("(" + (dirCount / timeSec) + "/second)");
        System.out.print("Searched " + fileCount + " files. ");
        System.out.println("(" + (fileCount / timeSec) + "/second)");
        System.out.println("");
    }

    private void scan(String[] roots, final String[] patterns) {
        if (searchFilter == null) {
            searchFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    String origName = name;
                    boolean matched = true;
                    fileCount++;

                    for (int p = 0; p < patterns.length; p++) {
                        String currentRegExp = patterns[p];

                        if (!caseSensitiveSearch) {
                            currentRegExp = currentRegExp.toLowerCase();
                        }

                        for (StringTokenizer st = new StringTokenizer(
                                                          currentRegExp, "*");
                             st.hasMoreElements();) {
                            String pattern = st.nextToken();

                            if (!caseSensitiveSearch) {
                                name = name.toLowerCase();
                            }

                            if ((name.indexOf(pattern) >= 0) && matched) {
                                matched = true;
                            } else {
                                matched = false;
                            }
                        }

                        //For an exact match (without *'s)
                        if (name.equalsIgnoreCase(patterns[p])) {
                            matched = true;
                        }
                    }

                    File current = new File(dir, origName);

                    if (matched) {
                        matchingFiles.addElement(current);
                    }

                    if (current.isDirectory()) {
                        dirCount++;
                    }

                    return current.isDirectory(); //return matched;
                }
            };

        }

        //Search depth first
        for (int i = 0; i < roots.length; i++) {
            //each drive specified to search
            File cFile = new File(roots[i]);

            //Search only directories (only for initial roots)
            if (cFile.isDirectory()) {
                //Recurse into directories
                String[] mDirs = cFile.list(searchFilter);

                for (int rr = 0; rr < mDirs.length; rr++) {
                    mDirs[rr] = cFile.getAbsoluteFile() + 
                                System.getProperty("file.separator") + 
                                mDirs[rr];
                }

                if ((matchingFiles.size() > 0) && stopOnFirstHit) {
                    break;
                }

                scan(mDirs, patterns);
            }
        }
    }

    public File[] fileSearch(String filename) {
        scan(defaultDrive, defaultFilter);

        File[] fArray = new File[0];

        return (File[]) matchingFiles.toArray(fArray);
    }

    public void setCaseSensitive(boolean caseSS) {
        caseSensitiveSearch = caseSS;
    }

    public void setStopOnFirstHit(boolean hit) {
        stopOnFirstHit = hit;
    }

    public File[] fileSearch(String[] roots, String[] filters) {
        scan(roots, filters);

        File[] fArray = new File[0];

        return (File[]) matchingFiles.toArray(fArray);
    }

    private void search() {
    }

    static public void main(String[] argv) {
        String[] dirs = { "c:/" }; ///JavaSoft/JRE/
        String[] filter = { "rEA*txt" };

        System.out.println("Test without stop on first hit:");

        FileScanner fs = new FileScanner();
        fs.setCaseSensitive(false);

        File[] ret = fs.fileSearch(dirs, filter);
        fs.printSummary();

        for (int i = 0; i < ret.length; i++) {
            System.out.println(ret[i]);
        }

        System.out.println("\n\nTest with STOP on first hit:");

        FileScanner fs1 = new FileScanner();
        fs1.setCaseSensitive(false);
        fs1.setStopOnFirstHit(true);

        File[] ret1 = fs1.fileSearch(dirs, filter);
        fs1.printSummary();

        for (int i1 = 0; i1 < ret1.length; i1++) {
            System.out.println(ret[i1]);
        }
    }
}