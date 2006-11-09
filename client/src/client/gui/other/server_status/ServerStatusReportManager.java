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
package client.gui.other.server_status;

/**
 * Title:        Your Product Name
 * Description:  This is the main Browser in the System
 * @author Peter Davies
 * @version
 */

public class ServerStatusReportManager {

  private static ServerStatusReportManager serverStatusReportManager=new ServerStatusReportManager();
  private ServerStatusReportChecker checker;
  private int interval=5;

  private ServerStatusReportManager() {}

  public static ServerStatusReportManager getReportManager() {
     return serverStatusReportManager;
  }

  public void startCheckingForReport() {
     if (isCheckingForReport()) return;
     checker=new ServerStatusReportChecker(interval);
     Thread rptCheckingThread=new Thread(checker);
     rptCheckingThread.setDaemon(true);
     rptCheckingThread.setPriority(Thread.MIN_PRIORITY);
     rptCheckingThread.start();
  }

  public void stopCheckingForReport() {
     if (!isCheckingForReport()) return;
     checker.stopChecking();
     checker=null;
  }

  public boolean isCheckingForReport(){
     return checker!=null;
  }

  public void setCheckInterval(int minutes) {
     interval=minutes;
     if (isCheckingForReport()) checker.setInterval(interval);
  }


}