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
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/
package client.gui.application.genome_browser;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
* This class gives a SplashScreen for the Browser application.
*
* Initially written by: Peter Davies
*
*/
public final class SplashScreen extends JWindow {
    private final JPanel outerPanel = new JPanel();
    private final JLabel statusLabel = new JLabel("");

    public SplashScreen() {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        jbInit();
        pack();
    }

    public void setStatusText(final String text) {
        statusLabel.setText(text);
    }

    private void jbInit() {
        getContentPane().setBackground(Color.white);
        outerPanel.setLayout(new BorderLayout(10, 0));


        //new BoxLayout(outerPanel,BoxLayout.Y_AXIS));
        outerPanel.add(new SplashPanel(), BorderLayout.NORTH);

        //    outerPanel.setBackground(Color.white);
        final JPanel rightsPanel = new JPanel();
        rightsPanel.setLayout(new BoxLayout(rightsPanel, BoxLayout.Y_AXIS));
        rightsPanel.setBackground(Color.white);
        rightsPanel.add(new JLabel("An Extremely Powerful Biochemical Browser"));
        rightsPanel.add(Box.createVerticalStrut(10));
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        final String currentYear = sdf.format(new Date());
        rightsPanel.add(
                new JLabel("Rights to this program are owned by Applera corporation."));
        rightsPanel.add(Box.createVerticalStrut(10));
        rightsPanel.add(
                new JLabel("Applera grants you free use of this tool per licenses accompanying the source."));
        // Tack on Status text field to report on application initialization
        rightsPanel.add(Box.createVerticalStrut(10));
        rightsPanel.add(statusLabel);

        final JPanel outerRightsPanel = new JPanel();
        outerRightsPanel.setLayout(
                new BoxLayout(outerRightsPanel, BoxLayout.X_AXIS));
        outerRightsPanel.add(Box.createHorizontalStrut(25));
        outerRightsPanel.setBackground(Color.white);
        outerRightsPanel.add(rightsPanel);
        outerPanel.add(outerRightsPanel, BorderLayout.SOUTH);

        outerPanel.setBorder(
                new BevelBorder(BevelBorder.RAISED, Color.lightGray,
                                Color.darkGray));
        getContentPane().add(outerPanel);
        pack();

        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension frameSize = getSize();

        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }

        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }

        setLocation((screenSize.width - frameSize.width) / 2,
                    (screenSize.height - frameSize.height) / 2);
    }

    protected void processWindowEvent(final WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            dispose();
        }

        super.processWindowEvent(e);
    }
}
