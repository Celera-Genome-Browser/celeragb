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
import api.entity_model.management.ModifyManager;
import client.gui.framework.session_mgr.SessionMgr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class AnnotationLogViewer extends JDialog{
  private static int COLUMNS_SIZE=3;
  private Object[][] data;


  public AnnotationLogViewer(JFrame parentFrame){
    super(parentFrame, "Annotation Log For Current Session", true);
    data=new Object[ModifyManager.getModifyMgr().getCommandHistoryStringList().size()][COLUMNS_SIZE];
    populateData(ModifyManager.getModifyMgr().getCommandHistoryStringList());

    String[] columnNames = {"Command Type",
                                "Time Stamp",
                                "Action Description"
                             };

    final JTable table = new JTable(data, columnNames);
    table.setPreferredScrollableViewportSize(new Dimension(700, 70));
    table.setEnabled(false);

    //Create the scroll pane and add the table to it.
    JScrollPane scrollPane = new JScrollPane(table);

   //Add the scroll pane to this window.
    getContentPane().add(scrollPane, BorderLayout.CENTER);

    getContentPane().add(new JLabel("Session Log In Time: "+SessionMgr.getSessionMgr().getSessionCreationTime()),BorderLayout.NORTH);
    getContentPane().add(new JLabel("Session Log Out Time: "+new Date().toString()),BorderLayout.SOUTH);

    addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                AnnotationLogViewer.this.hide();
                AnnotationLogViewer.this.dispose();
            }
        });

    // position the view in the center of the parent Component.
    pack();
    this.setLocationRelativeTo(parentFrame);
  }



  private synchronized void populateData(List commandHistoryList){
    if(commandHistoryList!=null){
      for(int j=0;j<commandHistoryList.size();j++){
          String str=(String)commandHistoryList.get(j);
          StringTokenizer st=new StringTokenizer(str,"--");
          int k=0;
          while(st.hasMoreTokens()){
            this.data[j][k]=st.nextToken().trim();
            k++;
          }

       }
     }
  }


}