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

import client.gui.framework.session_mgr.SessionMgr;
import client.gui.other.util.URLLauncher;
import client.shared.text_component.StandardTextArea;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;


/**
* This class provides a HelpMenu specific to the GenomeBrowser application.
* If desired, helpAbout_actionPerformed can be overridden to open a different
* AboutBox.
*/
public class HelpMenu extends JMenu {
    private URL emailURL;
    private String buildDate = new String("@@date@@");

    public HelpMenu() {
        setText("Help");
        this.setMnemonic('H');

        /*
        JMenuItem cdsLinkMI = new JMenuItem("Some Interesting Web Site", 'Y');
        cdsLinkMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
                                                        InputEvent.CTRL_MASK,
                                                        false));
        cdsLinkMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                URLLauncher tmpLauncher = new URLLauncher();
                String tmpUser = (String) SessionMgr.getSessionMgr()
                                                    .getModelProperty(SessionMgr.USER_NAME);
                String tmpPass = (String) SessionMgr.getSessionMgr()
                                                    .getModelProperty(SessionMgr.USER_PASSWORD);
                tmpLauncher.launchURL(
                        "http://www.xxxxx.com/servlet/startingPage?uid=" + tmpUser +
                            "&userPassword=" + tmpPass + "&page=ForThisBrowser");
            }
        });
        add(cdsLinkMI);
        */

        final String userManual = System.getProperty(
                                          "x.genomebrowser.UserManual");

        if (userManual != null) {
            JMenuItem menuUserManual = new JMenuItem("User Manual", 'U');
            menuUserManual.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    menuRun_actionPerformed(e, userManual);
                }
            });
            add(menuUserManual);
        }

        final String userBulletin = System.getProperty(
                                            "x.genomebrowser.UserBulletin");

        if (userBulletin != null) {
            JMenuItem menuUserBulletin = new JMenuItem("User Bulletin", 'B');
            menuUserBulletin.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    menuRun_actionPerformed(e, userBulletin);
                }
            });
            add(menuUserBulletin);
        }

        final String releaseNotes = System.getProperty(
                                            "x.genomebrowser.ReleaseNotes");

        if (releaseNotes != null) {
            JMenuItem menuReleaseNotes = new JMenuItem("Release Notes", 'R');
            menuReleaseNotes.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    menuRun_actionPerformed(e, releaseNotes);
                }
            });
            add(menuReleaseNotes);
        }

        JMenu menuReferenceGuides = new JMenu("Reference Guides");
        menuReferenceGuides.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //helpReferenceGuides_actionPerformed(e);
            }
        });
        add(menuReferenceGuides);

        final String snpRefGuide = System.getProperty(
                                           "x.genomebrowser.SNPReferenceGuide");
        JMenuItem menuRefGuideSNP = new JMenuItem("SNP Reference Guide");

        if (snpRefGuide != null) {
            menuRefGuideSNP.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    menuRun_actionPerformed(e, snpRefGuide);
                }
            });
            menuReferenceGuides.add(menuRefGuideSNP);
        }

        final String chgdGuide = System.getProperty(
                                         "x.genomebrowser.CHGDReferenceGuide");
        JMenuItem menuRefGuideCHGD = new JMenuItem("CHGD Data Reference Guide");

        if (chgdGuide != null) {
            menuRefGuideCHGD.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    menuRun_actionPerformed(e, chgdGuide);
                }
            });
            menuReferenceGuides.add(menuRefGuideCHGD);
        }

        final String cmgdGuide = System.getProperty(
                                         "x.genomebrowser.CMGDReferenceGuide");
        JMenuItem menuRefGuideCMGD = new JMenuItem("CMGD Data Reference Guide");

        if (cmgdGuide != null) {
            menuRefGuideCMGD.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    menuRun_actionPerformed(e, cmgdGuide);
                }
            });

            menuReferenceGuides.add(menuRefGuideCMGD);
        }

        JMenuItem menuHelpSuggestions = new JMenuItem("Email Feedback...", 'm');
        menuHelpSuggestions.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                helpSuggestions_actionPerformed(e);
            }
        });
        add(menuHelpSuggestions);

        JMenuItem menuHelpAbout = new JMenuItem("About...");
        menuHelpAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                helpAbout_actionPerformed(e);
            }
        });
        add(menuHelpAbout);
    }

    //Help | About action performed
    protected void helpAbout_actionPerformed(ActionEvent e) {
        AboutBox dlg = new AboutBox();
        dlg.show();
        this.getParent().repaint();
    }

    protected void menuRun_actionPerformed(ActionEvent e, String action) {
        URLLauncher.launchURL(action);
    }

    /**
     * This method will allow the user to email suggestions, comments, or other feedback
     * to this company about this software product, data, or whatever.
     */
    private void helpSuggestions_actionPerformed(ActionEvent e) {
        try {
            String emailFrom = null;

            while ((emailFrom == null) || emailFrom.equals("")) {
                emailFrom = JOptionPane.showInputDialog(getParentFrame(),
                                                        "Please enter your Internet email address.",
                                                        "E-Mail Address",
                                                        JOptionPane.QUESTION_MESSAGE);

                if (emailFrom == null) {
                    return;
                }
            }

            String desc = null;
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(new JLabel("Please enter your comments and/or suggestions below. "));
            panel.add(new JLabel("Thank you for your assistance."));
            panel.add(Box.createVerticalStrut(15));

            JTextArea textArea = new StandardTextArea(7, 20);
            panel.add(new JScrollPane(textArea));

            int ans = 0;

            while ((desc == null) || desc.equals("")) {
                ans = getOptionPane()
                          .showConfirmDialog(getParentFrame(), panel,
                                             "User Feedback",
                                             JOptionPane.OK_CANCEL_OPTION,
                                             JOptionPane.QUESTION_MESSAGE);

                if (ans == JOptionPane.CANCEL_OPTION) {
                    return;
                }

                desc = textArea.getText();
            }

            String line;
            URL url = getEmailURL();

            if (url == null) {
                showEMailFailureDialog();

                return;
            }

            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);

            PrintStream out = new PrintStream(connection.getOutputStream());
            out.print("emailFrom=" + URLEncoder.encode(emailFrom) +
                      "&problemDescription=" +
                      URLEncoder.encode(formMessage(emailFrom, desc)) +
                      "&subject=" +
                      URLEncoder.encode("Genome Browser User Feedback"));
            out.close();
            connection.getInputStream();

            // Now we read the response
            boolean success = false;
            BufferedReader stream = new BufferedReader(
                                            new InputStreamReader(
                                                    connection.getInputStream()));

            while (((line = stream.readLine()) != null) && !success) {
                if (line.indexOf("Message successfully sent") > -1) {
                    success = true;
                }
            }

            if (success) {
                getOptionPane()
                    .showMessageDialog(getParentFrame(),
                                       "Your message was sent " +
                                       "to our support staff.");
            } else {
                showEMailFailureDialog();
            }
        } catch (Exception ex) {
            showEMailFailureDialog();
        }
    }

    private void showEMailFailureDialog() {
        getOptionPane()
            .showMessageDialog(getParentFrame(),
                               "Your message was NOT able to be sent " +
                               "to the support staff.  Please contact your product support representative.");
    }

    private String formMessage(String emailFrom, String desc) {
        StringBuffer sb = new StringBuffer(10000);
        String lineSep = System.getProperty("line.separator");
        sb.append("User Comments: " + lineSep + desc + lineSep + lineSep);
        sb.append("Version: " +
                  SessionMgr.getSessionMgr().getApplicationName() + " v" +
                  SessionMgr.getSessionMgr().getApplicationVersion() +
                  lineSep);
        sb.append("Build Date: " + buildDate + lineSep);
        sb.append(lineSep + lineSep + "Have a nice day!!");

        return sb.toString();
    }

    private URL getEmailURL() {
        if (emailURL != null) {
            return emailURL;
        }

        String appServer = System.getProperty("x.genomebrowser.HttpServer");

        if (appServer != null) {
            appServer = "http://" + appServer;

            try {
                emailURL = new URL(appServer +
                                   "/broadcast/FeedbackMailer.jsp");

                return emailURL;
            } catch (Exception ex) {
                return null;
            } //cannot determine emailServer URL
        }

        return null;
    }

    private JOptionPane getOptionPane() {
        JFrame mainFrame = new JFrame();
        JFrame parent = getParentFrame();

        if (parent != null) {
            mainFrame.setIconImage(getParentFrame().getIconImage());
        }

        JOptionPane optionPane = new JOptionPane();
        mainFrame.getContentPane().add(optionPane);

        return optionPane;
    }

    private JFrame getParentFrame() {
        return SessionMgr.getSessionMgr().getActiveBrowser();
    }
}