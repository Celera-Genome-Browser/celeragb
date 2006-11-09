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
package client.gui.other.annotation_log;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Deepali Bhandari
 * @version $Id$
 */
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;



public class GBWAnnotationLogViewer extends JDialog{
  private JFrame parentFrame;
  Object[][] data1;
  Object[][] data2;
  private static int COLUMNS_SIZE=3;
  private static String LOG_EXTENSION=".log";

  public  GBWAnnotationLogViewer(JFrame parentFrame, String gbwfilepath){
    super(parentFrame,"Annotation Log for Opened GBW File",true);
    this.parentFrame = parentFrame;
    ContainerWithBoxLayout yaxis=new ContainerWithBoxLayout(BoxLayout.Y_AXIS);
    String[] columnNames = {"Command Type",
                                "Time Stamp",
                                "Action Description"
                             };


    String logfilepath=removeGbwExtension(gbwfilepath);
    if (logfilepath==null) return;

    // parse the file and add it to the contatiner
    // file is parsed line by line
    // if the line doesnot begin with a Session or "***" then it
    // goes into a Object array for table construction, if it does
    // then it is simply added on to the yaxis

    try{
           FileReader fr=new FileReader(logfilepath);
           BufferedReader br=new BufferedReader(fr);
           String line;
           Object[][]data=null;
           List commandHistoryStringList=null;
           while((line=br.readLine())!=null){
             if(line==null || line.equals("")){continue;}
             if(line.startsWith("*")|| line.startsWith("Session Log In Time")){
               commandHistoryStringList=new ArrayList();
               JLabel jlabel=new JLabel(line);
               yaxis.add(jlabel);
             }else if(!line.startsWith("Session Log Out Time")){
               commandHistoryStringList.add(line);
             }
             if(line.startsWith("Session Log Out Time")){
               JLabel jlabel=new JLabel(line);
               yaxis.add(jlabel);
               //build the table
               data=new Object[commandHistoryStringList.size()][COLUMNS_SIZE];
               data=populateData(commandHistoryStringList);
               JTable table = new JTable(data, columnNames);
               table.setPreferredScrollableViewportSize(new Dimension(700, 70));
               table.setEnabled(false);
               //Create the scroll pane and add the table to it.
               JScrollPane scrollPane1 = new JScrollPane(table);
              //Add the scroll pane to this window.
               yaxis.add(scrollPane1);
               commandHistoryStringList=null;
             }
           }
        }catch(Exception e){}


    getContentPane().add(yaxis);
    addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                GBWAnnotationLogViewer.this.hide();
                GBWAnnotationLogViewer.this.dispose();
            }
        });


     // position the view in the center of the parent Component.
    pack();
    this.setLocationRelativeTo(parentFrame);
  }

  private synchronized Object[][] populateData(List commandHistoryStringList){
    Object[][] data=null;
    if(commandHistoryStringList!=null){
      data=new Object[commandHistoryStringList.size()][COLUMNS_SIZE];
      for(int j=0;j<commandHistoryStringList.size();j++){
          String str=(String)commandHistoryStringList.get(j);
          StringTokenizer st=new StringTokenizer(str,"--");
          int k=0;
          while(st.hasMoreTokens()){
            data[j][k]=st.nextToken().trim();
            k++;
          }

       }

     }
      return data;
  }


  private String removeGbwExtension(String gbwfileName){
     String retString=null;
     if (gbwfileName==null) return retString;
     if(gbwfileName.endsWith(".gbw")){
       retString=gbwfileName.substring(0,gbwfileName.lastIndexOf(".gbw"))+this.LOG_EXTENSION;

     }else retString=retString+this.LOG_EXTENSION;
     System.out.println("AnnotationLog file name "+retString);
     return retString;
   }


  class ContainerWithBoxLayout extends JPanel{
    public ContainerWithBoxLayout(int orientation){
      setLayout (new BoxLayout(this,orientation));
    }
  }


}