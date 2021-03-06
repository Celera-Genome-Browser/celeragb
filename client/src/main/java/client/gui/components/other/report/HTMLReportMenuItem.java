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
package client.gui.components.other.report;

import api.entity_model.access.report.Report;
import client.gui.framework.session_mgr.SessionMgr;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies
 * @version $Id$
 */

public class HTMLReportMenuItem extends JMenuItem {

  private HTMLViewable htmlViewable;

  public HTMLReportMenuItem(HTMLViewable htmlViewable) {
     setText("Show HTML Report...");
     this.htmlViewable = htmlViewable;
     this.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e){
          Report report=HTMLReportMenuItem.this.htmlViewable.getCurrentReport();
          if (report!=null) {
            try {
             new HTMLReport(report).createTempFileAndShow("Genome Browser "+
                HTMLReportMenuItem.this.htmlViewable.getCurrentReportName());
            }
            catch(Exception ex) {
               SessionMgr.getSessionMgr().handleException(ex);
            }
          }
       }
     });
  }
}