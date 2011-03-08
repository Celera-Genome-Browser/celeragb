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
package client.launcher;

/**
 * Title:
 * Description:
 * Company:
 * @author
 * @version 1.0
 */

import java.applet.Applet;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ResourceBundle;


public class LauncherApplet extends Applet implements MouseListener {
    Hashtable args = new Hashtable();
    Image runImage = null;
    Color background = Color.white;
    boolean launching = false;
    int maxFileCount = 10;
    int fileCount = 0;
    boolean isStandalone = false;

    public void init() {
        args.put("url", getParameter("url"));
        args.put("program", getParameter("program"));
        args.put("link", getParameter("link"));
        args.put("programexe", getParameter("programexe"));
        args.put("programcfg", getParameter("programcfg"));
        args.put("programico", getParameter("programico"));
        args.put("clientconfig", getParameter("clientconfig"));
        args.put("clientversion", getParameter("clientversion"));
        args.put("updatescript", getParameter("updatescript"));
        args.put("productname", getParameter("productname"));

        if (getParameter("originallink") != null) {
            args.put("originallink", getParameter("originallink"));
        } else {
            args.put("originallink", "Genome Browser40.lnk");
        }

        for (int i = 1; i <= maxFileCount; i++) {
            if (getParameter("file" + i) == null) {
                break;
            }

            fileCount = i;
            System.out.println("FILE LIST['" + "file" + i + "']=" + 
                               getParameter("file" + i));
            args.put("file" + i, getParameter("file" + i));
        }

        args.put("fileCount", Integer.toString(fileCount));

        //Chop up the document root because netscape includes the index.html in the path but IE doesn't
        String urlRoot = getDocumentBase().toString();

        if (urlRoot.lastIndexOf("/") != urlRoot.length()) {
            urlRoot = urlRoot.substring(0, urlRoot.lastIndexOf("/") + 1);
        }

        args.put("urlRoot", urlRoot);

        System.out.println("getDocumentBase().getPath() returned: " + 
                           getDocumentBase().getPath());

        String color = this.getParameter("Color");

        if (color != null) {
            background = new Color(Integer.parseInt(color.substring(0, 2), 16), 
                                   Integer.parseInt(color.substring(2, 4), 16), 
                                   Integer.parseInt(color.substring(4, 6), 16));
        }

        System.out.println("this.getDocumentBase() returned " + 
                           getDocumentBase());

        if (isStandalone) {
            mouseReleased(null);
        } else {
            //Html display
            runImage = getImage(getDocumentBase(), "genome_browser_btn1.gif");
        }

        addMouseListener(this);
    }

    public boolean runLauncher() {
        if (!launching) {
            launching = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));


            //Run Install Wizard
            client.tools.installer.Install.main(args);
            launching = false;
        }

        return true;
    }

    public void paint(Graphics g) {
        Dimension d = getSize();
        g.setColor(background);
        g.fillRect(0, 0, (int) d.getWidth(), (int) d.getHeight());
        g.drawImage(runImage, 0, 0, this);
    }

    /**
     * The user has clicked in the applet. Figure out where
     * and see if a legal move is possible. If it is a legal
     * move, respond with a legal move (if possible).
     */
    public void mouseReleased(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        try {
            Thread.sleep(130);
        } catch (Exception re) {
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        runLauncher();
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        if (!launching) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    public void mouseExited(MouseEvent e) {
        if (!launching) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public URL getDocumentBase() {
        String userDir = System.getProperty("user.dir");

        URL url = null;

        try {
            url = userDir.startsWith("http")?new URL(userDir):(new File(userDir)).toURI().toURL();
        } catch (Exception me) {
            me.printStackTrace();
        }

        return isStandalone ? url : super.getDocumentBase();
    }

    /**Get a parameter value*/
    public String getParameter(String key) {
        return isStandalone ? System.getProperty(key) : super.getParameter(key);
    }

    /**Main method*/
    public static void main(String[] args) {
        LauncherApplet applet = new LauncherApplet();
        applet.isStandalone = true;
		
		
		ResourceBundle myResources = ResourceBundle.getBundle("params");
		for( Enumeration e = myResources.getKeys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			System.setProperty(key,myResources.getString(key));
		}

        //Produce the file update script using the URL file:/
//       System.setProperty("codeBase", "");
//       System.setProperty("localBase", "");
//       System.setProperty("serverBase", "");

        applet.init();
        applet.start();
        System.exit(0);
    }
}