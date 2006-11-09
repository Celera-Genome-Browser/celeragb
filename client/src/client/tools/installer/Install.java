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

import java.util.Hashtable;

import javax.swing.JPanel;


/**
 * Title:
 * Description:
 * Company:
 * @author
 * @version 1.0
 */
public class Install {
    JPanel jPanel1 = new JPanel();
    Hashtable args = null;
    WizardController wc = null;

    public Install(Hashtable passThruArgs) {
        args = passThruArgs;

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(Hashtable args) {
        try {
            Install installer = new Install(args);
            installer.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() throws Exception {
        wc.runWizard();
    }

    private void jbInit() throws Exception {
        BackGround bgFrame = new BackGround((String) args.get("productname"));
        wc = new WizardController(bgFrame);
        wc.setPassThruArgs(args);
    }
}