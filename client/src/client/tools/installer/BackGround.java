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
package client.tools.installer;

import java.awt.Color;
import java.awt.Dimension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;


/**
 * Title:
 * Description:
 * Company:
 * @author
 * @version 1.0
 */
public class BackGround extends JFrame {
    private static ImageIcon installIcon = BackGround.loadImage("/GenomeBrowser.ico");

    public BackGround(String productname) {
        setIconImage(installIcon.getImage());

        Dimension screenBounds = this.getToolkit().getScreenSize();
        setBounds(0, 0, screenBounds.width, screenBounds.height);
        setResizable(false);
        setTitle("Amazing - " + productname + " Installation Wizard");
        getContentPane().setForeground(new Color(0, 0, 177));
        getContentPane().setBackground(new Color(0, 0, 177));

        setVisible(true);
    }

    private static ImageIcon loadImage(String file) {
        ImageIcon temp = null;

        try {
            InputStream is = null;
            URL url = BaseScreen.class.getResource(file);
            is = url.openStream();

            int BUFSIZ = 1024;
            byte[] buf = new byte[BUFSIZ];
            int n = 0;
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try {
                while ((n = is.read(buf)) > 0) {
                    out.write(buf, 0, n);
                }
            } catch (IOException e) {
                System.out.println("IOException 1");
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    System.out.println("IOException 2");
                    e.printStackTrace();
                }
            }

            temp = new ImageIcon(out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return temp;
    }
}