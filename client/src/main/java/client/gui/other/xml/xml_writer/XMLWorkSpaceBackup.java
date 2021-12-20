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
package client.gui.other.xml.xml_writer;

/**
 * Title:        Your Product Name
 * Description:  This is the main Browser in the System
 * @author Peter Davies
 * @version
 */

import client.gui.other.annotation_log.AnnotationLogWriter;

public class XMLWorkSpaceBackup implements Runnable {

  private String fileName;
  private int intervalInMinutes;
  private boolean stop;

  public XMLWorkSpaceBackup(String fileName, int intervalInMinutes) {
     this.fileName=fileName;
     this.intervalInMinutes=intervalInMinutes;
  }

  public void run() {
    while (true) {
      try {
       Thread.currentThread().sleep(intervalInMinutes*1000*60);
      }
      catch (InterruptedException ex) {}
      if (stop) break;

      XMLWriter.getXMLWriter().saveToXML(fileName,false);
      AnnotationLogWriter.getAnnotationLogWriter().writeLog(XMLWriter.getXMLWriter().getWorkspaceBackupFileName(),false);
    }
  }

  public void runOnce() {
      XMLWriter.getXMLWriter().saveToXML(fileName,false);
      AnnotationLogWriter.getAnnotationLogWriter().writeLog(XMLWriter.getXMLWriter().getWorkspaceBackupFileName(),false);
  }

  public void stop() {
     stop=true;
  }
}