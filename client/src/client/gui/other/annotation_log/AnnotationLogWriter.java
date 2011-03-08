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

import api.entity_model.management.ModifyManager;
import api.facade.facade_mgr.FacadeManager;
import client.gui.framework.session_mgr.SessionMgr;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class AnnotationLogWriter {
    private static AnnotationLogWriter instance = new AnnotationLogWriter();
    private static final String LOG_EXTENSION = ".log";

    private AnnotationLogWriter() {
    }

    public static AnnotationLogWriter getAnnotationLogWriter() {
        return instance;
    }

    public void writeLog(String filepathOfgbw, boolean showOverWriteWarning) {
        try {
            if (filepathOfgbw == null) {
                return;
            }

            String logfilename = removeGbwExtension(filepathOfgbw);

            //check if we are overwriting an opened gbw file
            if ((findOpenedGBWFileName() != null) && 
                    filepathOfgbw.equals(findOpenedGBWFileName())) {
                SessionMgr.getSessionMgr()
                          .handleException(new IllegalStateException(
                                                   "You cannot save a workspace file with the same filename as " + "the currently open workspace file.  Save rejected."));

                return;
            }

            //check if we are overwriting an open log file
            if ((findOpenedLogFileName() != null) && 
                    findOpenedLogFileName().equals(logfilename)) {
                SessionMgr.getSessionMgr()
                          .handleException(new IllegalStateException(
                                                   "You cannot save a log file with the same filename as " + "the currently open log file.  Save rejected."));

                return;
            }

            //provide a Warning to the user that the log file will be overriten
            //if if already exists and is not equal to the opened log file.
            // Donot want to show warning when during back up as the thread is
            // invoked after evey few minutes- warning message can be annoying
            // for the user
            if (new File(logfilename).exists() && showOverWriteWarning) {
                showWarningMessage(logfilename);
            }

            FileWriter logfw = new FileWriter(logfilename);
            PrintWriter logpw = new PrintWriter(logfw);


            //copy from existing Log file if a .gbw file is open
            copyFromOpenedLogFile(logfw);

            Date sessionLogInTime = SessionMgr.getSessionMgr()
                                              .getSessionCreationTime();


            //First print the Title for the AnnotationLog File
            logpw.println("**************" + "AnnotationLog File For " + 
                          filepathOfgbw + "*************");


            //Second print the Session Log in time
            logpw.println("Session Log In Time: " + 
                          sessionLogInTime.toString() + "\n");


            //Third print the Command Log
            writeExecutedCommandsLog(logpw);


            //Last print the SessionLogOutTime
            logpw.println("Session Log Out Time: " + (new Date()).toString() + 
                          "\n");
            logpw.close();
            logfw.close();
        } catch (Exception e) {
            SessionMgr.getSessionMgr().handleException(e);
        }
    }

    private synchronized void writeExecutedCommandsLog(PrintWriter pw) {
        List commandHistoryStringList = ModifyManager.getModifyMgr()
                                                     .getCommandHistoryStringList();

        if (!commandHistoryStringList.isEmpty()) {
            for (Iterator iter = commandHistoryStringList.iterator();
                 iter.hasNext();) {
                String commandStr = (String) iter.next();
                pw.println(commandStr);
            }
        }
    }

    /**
     * need to find if a .gbw file is open, will determine if previous
     * session's log file needs copying or not.
     */
    private String findOpenedGBWFileName() {
        Object[] dataSources = FacadeManager.getFacadeManager()
                                            .getOpenDataSources();

        for (int i = 0; i < dataSources.length; i++) {
            if (dataSources[i] != null) {
                if (dataSources[i].toString().toLowerCase().endsWith(".gbw")) {
                    return dataSources[i].toString();
                }
            }
        }

        return null;
    }

    private String removeGbwExtension(String gbwfileName) {
        String retString = null;

        if (gbwfileName.endsWith(".gbw")) {
            retString = gbwfileName.substring(0, 
                                              gbwfileName.lastIndexOf(".gbw")) + 
                        this.LOG_EXTENSION;
        } else {
            retString = retString + this.LOG_EXTENSION;
        }

        System.out.println("AnnotationLog file name " + retString);

        return retString;
    }

    private String findOpenedLogFileName() {
        String opengbw = findOpenedGBWFileName();
        String retString = null;

        if (opengbw != null) {
            String openlog = removeGbwExtension(opengbw);
            retString = openlog;
        }

        return retString;
    }

    private void copyFromOpenedLogFile(FileWriter outfw) {
        if (findOpenedLogFileName() == null) {
            return;
        }

        try {
            File inputFile = new File(findOpenedLogFileName());

            FileReader in = new FileReader(inputFile);
            FileWriter out = outfw;
            int c;

            while ((c = in.read()) != -1)
                out.write(c);

            in.close();
        } catch (Exception e) {
            SessionMgr.getSessionMgr().handleException(e);
        }
    }

    private void showWarningMessage(String filename) {
        JOptionPane.showMessageDialog(SessionMgr.getSessionMgr()
                                                .getActiveBrowser(), 
                                      filename + " was overwritten.", 
                                      "File Overwritten", 
                                      JOptionPane.WARNING_MESSAGE);
    }
}