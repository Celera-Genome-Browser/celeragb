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
package client.gui.other.fasta;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

import client.gui.framework.session_mgr.SessionMgr;
import client.shared.file_chooser.FileChooser;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;



public class FastaWriter {
   private static FastaWriter fastaWriter = new FastaWriter();
   private static final String FASTA_EXTENSION=".fasta";


   static public FastaWriter getFastaWriter() {
     return fastaWriter;
  }


  public void printFastaFile(FastaObject f){
      try{

          File selectedfile =chooseFile();
          if (selectedfile==null) return;

            FileWriter fw = new FileWriter(selectedfile);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(f.getFastaDefline());
            String str=f.getFastaSeqString();

           if(str.length()<=f.FASTA_CHAR_PER_LINE){
            pw.println(str);
          }else{
            int numRows=str.length()/f.FASTA_CHAR_PER_LINE;
            int remainingChars=str.length()%f.FASTA_CHAR_PER_LINE;
            int lastIndex=0;
            for(int i=0;i<numRows;i++){
                   pw.println(str.substring(i*f.FASTA_CHAR_PER_LINE,(i+1)*f.FASTA_CHAR_PER_LINE));
                   lastIndex=((i+1)*f.FASTA_CHAR_PER_LINE);
            }
            pw.println(str.substring(lastIndex,lastIndex+remainingChars));
          }

          fw.close();
          pw.close();
          }catch(IOException ex){ex.printStackTrace();}

  }



  private File chooseFile(){
    JFileChooser chooser=new FileChooser();
    chooser.setFileFilter(new MyFileFilter());
        try {
          chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        }catch (Exception ex) {
           ex.printStackTrace();
        }
        int option=chooser.showSaveDialog(SessionMgr.getSessionMgr().getActiveBrowser());
        if (option==JFileChooser.CANCEL_OPTION) return null;
        File selectedfile=chooser.getSelectedFile();

        while(selectedfile !=null && selectedfile.exists()){
             JOptionPane pane =new JOptionPane();
             int fileOverWriteable=pane.showConfirmDialog(chooser, new String("Do You want to Overwrite Existing File?"), new String("WARNING !"),JOptionPane.YES_NO_OPTION);
             // if file not to be overwritten then show file chooser again and repeat if
            // the new selected file exists and needs to overwritten or not
             if(fileOverWriteable==JOptionPane.NO_OPTION){
                 option=chooser.showSaveDialog(SessionMgr.getSessionMgr().getActiveBrowser());
                if (option==JFileChooser.CANCEL_OPTION) return null;
                 selectedfile=chooser.getSelectedFile();
                }else{
                   break;
                }
             }//while
             return selectedfile;

  }

  class MyFileFilter extends javax.swing.filechooser.FileFilter {
         public boolean accept(File pathname){
            if (pathname.getPath().toLowerCase().endsWith(FASTA_EXTENSION) ||
               pathname.isDirectory()) return true;
            return false;
         }

         public String getDescription(){
           return "(*"+FASTA_EXTENSION+") FASTA Format Files";
         }
  }



}