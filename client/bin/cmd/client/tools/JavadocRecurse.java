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
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class JavadocRecurse {
    static PrintWriter writer;

    public static void main(String[] args) {

        if (args.length < 2) {
            System.out.println("First command line argument is the destination file");
            System.out.println("where the package names will be stored");
            System.out.println("Rest of the arguments are directory paths to the packages");
            System.out.println("Example:");
            System.out.println(
                "java JavadocRecurse packages.txt rootdir1 rootdir2 rootdirN");
            return;
        }

        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(args[0])));
            for (int i=1; i<args.length; i++) {
                File root = new File(args[i]);
                if (root.isDirectory()) {
                    writeDirs(root, root);
                }
            }
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void writeDirs(File root, File dir) {
        String files[] = dir.list();
        boolean fileFound = true;
        for (int i=0; i<files.length; i++) {
            File file = new File(dir,files[i]);
            if (file.isDirectory()) {
                writeDirs(root,file);
            } else if (fileFound && (files[i].endsWith(".class")

                    || files[i].endsWith(".java"))) {
                fileFound = false;
                if (root.equals(dir)) {
                    //writer.println(".");   This was incorrect assumption about Javadoc
                } else {

writer.println(dir.getPath().substring(root.getPath().length()+1).replace(File.separatorChar,'.'));
                }
            }
        }
    }
}
