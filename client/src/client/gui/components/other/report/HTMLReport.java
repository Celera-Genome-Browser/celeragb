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

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version      $Id$
 */

import client.gui.framework.session_mgr.SessionMgr;
import api.entity_model.access.report.Report;
import client.gui.other.util.URLLauncher;
import java.io.*;
import java.util.Date;

public class HTMLReport {

  private static String HTML_START_TAG="<HTML>";
  private static String HTML_END_START_TAG="</HTML>";
  private static String HTML_HEAD_TAG="<HEAD>";
  private static String HTML_END_HEAD_TAG="</HEAD>";
  private static String HTML_TITLE_TAG="<TITLE>";
  private static String HTML_END_TITLE_TAG="</TITLE>";
  private static String HTML_BODY_TAG="<BODY>";
  private static String HTML_END_BODY_TAG="</BODY>";
  private Report report;

  public HTMLReport(Report report) {
    this.report=report;
  }

  /**
   * Will create an HTML File with the passed filename
   *
   * @throws IllegalArgumentException if the passed fileName cannot be written to
   */
  public void createFile(String fileName, String title){
     File file=new File(fileName);
     if (!file.canWrite()) throw new IllegalArgumentException("Specified file cannot be written to");
     String html=buildHTMLDocument(title);
     try {
       PrintWriter pw=new PrintWriter(new FileOutputStream(file),true);
       pw.write(html);
       pw.close();
     }
     catch (Exception ex) {
       SessionMgr.getSessionMgr().handleException(ex);
     }
  }

  /**
   * Will create an HTML File by prompting for the filename
   */
  public void createTempFileAndShow(String title) throws IOException {
    File file=File.createTempFile("GB-",".htm");
    createFile(file.getAbsolutePath(),title);
    URLLauncher.launchURL(file.getAbsolutePath());
    file.deleteOnExit();
  }

  private String buildHTMLDocument(String title) {
     String caption="<H2>"+title+"</H2> generated on "+new Date();
     return buildHeader(title)+HTML_BODY_TAG+report.getHTMLTable(caption)+
       HTML_END_BODY_TAG+HTML_END_START_TAG;
  }

  private String buildHeader(String title) {
     StringBuffer sb=new StringBuffer(300);
     sb.append(HTML_START_TAG);
     sb.append(HTML_HEAD_TAG);
     sb.append(HTML_TITLE_TAG);
     sb.append(title);
     sb.append(HTML_END_TITLE_TAG);
     sb.append(HTML_END_HEAD_TAG);
     return sb.toString();
  }

}