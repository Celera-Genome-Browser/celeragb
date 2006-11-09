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
package client.gui.other.util;

/**
 * Title:        Your Product Name
 * Description:  This is the main Browser in the System
 * @author Peter Davies
 * @version
 */


import java.util.*;
import java.io.File;
import client.gui.framework.session_mgr.SessionMgr;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;

public class URLLauncher {
   static int os;
   static String browser;
   static String BROWSER_PROP=URLLauncher.class.getName()+":Browser Props";

   static {
      String osName=System.getProperty("os.name");
      if (osName.startsWith("Windows")) {
         os=10;
         if (osName.endsWith("XP"))     os+=7;
         if (osName.endsWith("2000"))   os+=6;
         if (osName.endsWith("NT"))     os+=5;
         if (osName.endsWith("ME"))     os+=3;
         if (osName.endsWith("98"))     os+=2;
         if (osName.endsWith("95"))     os+=1;
      }
      if (osName.startsWith("Unix")) os=20;
      if (getBrowserProps().containsKey(osName))
        browser=(String)getBrowserProps().get(osName);
   }

   public static void launchURL(String url) {
     try {
      if (url.toLowerCase().startsWith("http://") &&
       (url.toLowerCase().endsWith(".htm") || url.toLowerCase().endsWith(".html"))) {
          int index=url.lastIndexOf(".");
          url=url.substring(0,index)+"%2E"+url.substring(index+1,url.length());
      }
      if (browser!=null) {
        Runtime.getRuntime().exec( browser+" " +url);
        return;
      }

      switch (os) {
        case 17:
        case 16:
        case 15:
        case 13:
        case 12:
        case 11:
          Runtime.getRuntime().exec( "rundll32 url.dll,FileProtocolHandler " +url);
          break;
        default:
          try {
            Runtime.getRuntime().exec( "netscape " +url);
            break;
          }
          catch (Exception ex) {
            askForBrowser(url);
            break;
          }
      }
     }
     catch (Exception ex) {
        askForBrowser(url);
     }
   }

   private static void askForBrowser(final String url) {
      final JDialog dialog=new JDialog(SessionMgr.getSessionMgr().getActiveBrowser());
      final JFileChooser fileChooser=new JFileChooser(System.getProperty("user.home"));
      fileChooser.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
          if (e.getActionCommand().equals("CancelSelection")) {
            dialog.hide();
            dialog.dispose();
          }
          if (e.getActionCommand().equals("ApproveSelection")) {
            File file=fileChooser.getSelectedFile();
            browser=file.getAbsolutePath();
            launchURL(url);
            dialog.hide();
            dialog.dispose();
            int ans=JOptionPane.showConfirmDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
                "Shall I make your Web Browser Selection for this Operating System permanent?","Store Preference",
                JOptionPane.YES_NO_OPTION);
            if (ans==JOptionPane.YES_OPTION){
              Map bp=getBrowserProps();
              bp.put(System.getProperty("os.name"),browser);
              SessionMgr.getSessionMgr().setModelProperty(BROWSER_PROP,bp);
            }
          };
      }});
      dialog.getContentPane().add(fileChooser);
      dialog.setTitle("Please Select Your Web Browser Executable");
      dialog.setSize(new Dimension(600,400));
      dialog.pack();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = dialog.getSize();
      if (frameSize.height > screenSize.height)
        frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width)
        frameSize.width = screenSize.width;
      dialog.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

      dialog.show();
   }

   static private Map getBrowserProps() {
      Map bp=(Map)SessionMgr.getSessionMgr().getModelProperty(BROWSER_PROP);
      if (bp==null) return new HashMap();
      return bp;
   }

}