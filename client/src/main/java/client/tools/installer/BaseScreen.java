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

/**
 * Title:
 * Description:
 * Company:
 * @author
 * @version 1.0
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class BaseScreen extends JPanel {
  String basetitle = null;
  WizardController master = null;
  static ImageIcon icon = BaseScreen.loadImage("/resource/client/images/gb_splash_logo_install.jpg");

  public BaseScreen( String title, WizardController controller ) {
    basetitle = title;
    master = controller;
    this.setLayout(new BorderLayout());
  }

  public String getArgValue(String key) {
  	return (String)master.passThruArgs.get(key);
  }

  public String getTitle() {
    return basetitle;
  }

  public void cancelButton_actionPerformed(ActionEvent e) {
    int result = JOptionPane.showConfirmDialog(this,"Do you want to cancel setup? Press No to resume, or press Yes to exit.","Cancel Setup?",JOptionPane.YES_NO_OPTION);
    if( result == JOptionPane.YES_OPTION ) {
      master.cancelInstall();
    }
  }

  protected void nextButton_actionPerformed(ActionEvent e) {
    master.showNext();
  }

  protected void backButton_actionPerformed(ActionEvent e) {
    master.showPrev();
  }

  static private ImageIcon loadImage( String file ) {
//    System.out.println("Extracting:  " + file);
    ImageIcon temp = null;
    try {
      InputStream is = null;
      URL url = BaseScreen.class.getResource(file);
      is = url.openStream();

      int BUFSIZ = 1024;
      byte buf[] = new byte[BUFSIZ];
      int n = 0;
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try {
        while( ( n = is.read(buf) ) > 0) {
          out.write( buf, 0, n );
        }
      }
      catch( IOException e ) {
        System.out.println("IOException 1");
        e.printStackTrace();
      }
      finally {
        try {
          //System.out.println("finally: Read in... " + out.size() + " bytes");
          is.close();
        }
        catch( IOException e ) {
          System.out.println("IOException 2");
          e.printStackTrace();
        }
      }
      temp = new ImageIcon(out.toByteArray());
    } catch(Exception e ){
      e.printStackTrace();
    }

    return temp;
  }

}