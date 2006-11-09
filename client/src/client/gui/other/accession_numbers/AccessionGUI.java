// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//Title:       Accession Numbers Client
//Version:
//Author:      David C. Wu
//Description: Java client for Accession numbers server
package client.gui.other.accession_numbers;

import client.shared.text_component.StandardTextArea;
import client.shared.text_component.StandardTextField;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.math.BigInteger;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;


public class AccessionGUI extends JApplet {
    private static final int CG_ACCESSION = 1;
    private static final int CT_ACCESSION = 2;

    // static initializer for setting look & feel
    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
        }
    }

    private boolean isStandalone = false;
    private BorderLayout borderLayout1 = new BorderLayout();
    private JLabel titleLbl = new JLabel();
    private JPanel jPanel1 = new JPanel();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JLabel hostLbl = new JLabel();
    private JLabel portLbl = new JLabel();
    private JComboBox hostCombo = new JComboBox();
    private JComboBox portCombo = new JComboBox();
    private JLabel infoLbl = new JLabel();
    private JTextArea infoTextArea = new StandardTextArea();
    private JPanel instructionPanel = new UserGuide();
    private JLabel geneLbl = new JLabel();
    private JLabel tranLbl = new JLabel();
    private JTextField geneText = new StandardTextField();
    private JTextField tranText = new StandardTextField();
    private JButton getBtn = new JButton();
    private AccessionClient accession;
    JButton clrBtn = new JButton();
    JLabel statusLbl = new JLabel();

    //Construct the applet
    public AccessionGUI() {
    }

    //Initialize the applet
    public void init() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Component initialization
    private void jbInit() throws Exception {
        titleLbl.setFont(new java.awt.Font("DialogInput", 3, 18));
        titleLbl.setForeground(Color.blue);
        titleLbl.setBorder(BorderFactory.createEtchedBorder());
        titleLbl.setHorizontalAlignment(SwingConstants.CENTER);
        titleLbl.setText("Accession Numbers");
        this.setSize(new Dimension(506, 529));
        this.getContentPane().setLayout(borderLayout1);
        jPanel1.setBorder(BorderFactory.createEtchedBorder());
        jPanel1.setLayout(gridBagLayout1);
        hostLbl.setFont(new java.awt.Font("Dialog", 1, 12));
        hostLbl.setForeground(Color.blue);
        hostLbl.setText("Host name");
        portLbl.setFont(new java.awt.Font("Dialog", 1, 12));
        portLbl.setForeground(Color.blue);
        portLbl.setText("Port");
        infoLbl.setFont(new java.awt.Font("Dialog", 1, 12));
        infoLbl.setForeground(Color.blue);
        infoLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        infoLbl.setText("Selection Info");
        infoTextArea.setColumns(20);
        infoTextArea.setBorder(BorderFactory.createLoweredBevelBorder());
        infoTextArea.setText("Test Server:");
        infoTextArea.setEditable(false);
        instructionPanel.setBackground(Color.black);
        instructionPanel.setBorder(BorderFactory.createEtchedBorder());
        portCombo.setEditable(true);
        geneLbl.setFont(new java.awt.Font("Dialog", 1, 12));
        geneLbl.setForeground(Color.blue);
        geneLbl.setText("Gene #:");
        tranLbl.setFont(new java.awt.Font("Dialog", 1, 12));
        tranLbl.setForeground(Color.blue);
        tranLbl.setText("Transcript #:");
        geneText.setBackground(Color.lightGray);
        geneText.setBorder(BorderFactory.createLoweredBevelBorder());
        geneText.setEditable(false);
        geneText.setColumns(10);
        tranText.setBackground(Color.lightGray);
        tranText.setBorder(BorderFactory.createLoweredBevelBorder());
        tranText.setEditable(false);
        tranText.setColumns(10);
        getBtn.setFont(new java.awt.Font("DialogInput", 1, 12));
        getBtn.setBorder(BorderFactory.createRaisedBevelBorder());
        getBtn.setText(" Get ");
        getBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getBtn_actionPerformed(e);
            }
        });
        clrBtn.setFont(new java.awt.Font("Dialog", 1, 12));
        clrBtn.setBorder(BorderFactory.createRaisedBevelBorder());
        clrBtn.setText(" Clear ");
        clrBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clrBtn_actionPerformed(e);
            }
        });
        statusLbl.setFont(new java.awt.Font("Dialog", 1, 12));
        statusLbl.setForeground(Color.blue);
        statusLbl.setText("Connection: ");
        this.getContentPane().add(titleLbl, BorderLayout.NORTH);
        this.getContentPane().add(jPanel1, BorderLayout.CENTER);
        jPanel1.add(hostLbl, 
                    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.EAST, 
                                           GridBagConstraints.NONE, 
                                           new Insets(10, 0, 0, 0), 0, 0));
        jPanel1.add(portLbl, 
                    new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.EAST, 
                                           GridBagConstraints.NONE, 
                                           new Insets(10, 10, 0, 0), 0, 0));
        jPanel1.add(hostCombo, 
                    new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.CENTER, 
                                           GridBagConstraints.NONE, 
                                           new Insets(10, 5, 0, 0), 0, 0));
        jPanel1.add(portCombo, 
                    new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.CENTER, 
                                           GridBagConstraints.NONE, 
                                           new Insets(10, 5, 0, 0), 0, 0));
        jPanel1.add(infoLbl, 
                    new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, 
                                           GridBagConstraints.WEST, 
                                           GridBagConstraints.NONE, 
                                           new Insets(10, 20, 0, 0), 0, 0));
        jPanel1.add(infoTextArea, 
                    new GridBagConstraints(0, 2, 4, 1, 0.0, 0.0, 
                                           GridBagConstraints.EAST, 
                                           GridBagConstraints.BOTH, 
                                           new Insets(5, 20, 0, 20), 0, 0));
        jPanel1.add(instructionPanel, 
                    new GridBagConstraints(0, 6, 4, 1, 0.0, 1.0, 
                                           GridBagConstraints.CENTER, 
                                           GridBagConstraints.BOTH, 
                                           new Insets(0, 10, 10, 10), 0, 0));
        jPanel1.add(geneLbl, 
                    new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.EAST, 
                                           GridBagConstraints.NONE, 
                                           new Insets(5, 0, 0, 0), 0, 0));
        jPanel1.add(tranLbl, 
                    new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.EAST, 
                                           GridBagConstraints.NONE, 
                                           new Insets(5, 0, 0, 0), 0, 0));
        jPanel1.add(geneText, 
                    new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.WEST, 
                                           GridBagConstraints.NONE, 
                                           new Insets(5, 0, 0, 0), 0, 0));
        jPanel1.add(tranText, 
                    new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.WEST, 
                                           GridBagConstraints.NONE, 
                                           new Insets(5, 0, 0, 0), 0, 0));
        jPanel1.add(getBtn, 
                    new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.CENTER, 
                                           GridBagConstraints.NONE, 
                                           new Insets(10, 5, 0, 0), 0, 0));
        jPanel1.add(clrBtn, 
                    new GridBagConstraints(4, 4, 1, 1, 0.0, 0.0, 
                                           GridBagConstraints.CENTER, 
                                           GridBagConstraints.NONE, 
                                           new Insets(5, 0, 0, 0), 0, 0));
        jPanel1.add(statusLbl, 
                    new GridBagConstraints(0, 5, 5, 1, 0.0, 0.0, 
                                           GridBagConstraints.WEST, 
                                           GridBagConstraints.NONE, 
                                           new Insets(5, 0, 5, 0), 0, 0));
        setup();
    }

    public void setup() {
        hostCombo.addItem("dsc101a");
        portCombo.addItem("5007");
        portCombo.addItem("5008");
        portCombo.addItem("5009");
        portCombo.addItem("5010");
        portCombo.addItem("5011");
        portCombo.addItem("5012");
        portCombo.addItem("5013");
        portCombo.addItem("5014");

        infoTextArea.setText("Drosophila: \n" + 
                             " Gene (Test) - port 5007 \n" + 
                             " Transcript (Test) - port 5008 \n" + 
                             " Gene (Production) - port 5009 \n" + 
                             " Transcript (Production) - port 5010 \n\n" + 
                             "Human: \n" + " Gene (Test) - port 5011 \n" + 
                             " Transcript (Test) - port 5012 \n" + 
                             " Gene (Production) - port 5013 \n" + 
                             " Transcript (Production) - port 5014");
    }

    //Start the applet
    public void start() {
    }

    //Stop the applet
    public void stop() {
    }

    //Destroy the applet
    public void destroy() {
    }

    //Get Applet information
    public String getAppletInfo() {
        return "Applet Information";
    }

    //Get parameter info
    public String[][] getParameterInfo() {
        return null;
    }

    //Main method
    public static void main(String[] args) {
        AccessionGUI applet = new AccessionGUI();
        applet.isStandalone = true;

        JFrame frame = new JFrame();
        frame.setTitle("Applet Frame");
        frame.getContentPane().add(applet, BorderLayout.CENTER);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        applet.init();
        applet.start();
        frame.setSize(500, 600);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((d.width - frame.getSize().width) / 2, 
                          (d.height - frame.getSize().height) / 2);
        frame.setVisible(true);
    }

    void getBtn_actionPerformed(ActionEvent e) {
        int p = Integer.parseInt((String) portCombo.getSelectedItem());

        try {
            if ((p % 2) > 0) { //odd: gene port
                accession = new AccessionClient(CG_ACCESSION, 
                                                (String) hostCombo.getSelectedItem(), p);

                BigInteger number = accession.getUIDNumber();
                geneText.setText("CG" + number.toString());
            } else {
                accession = new AccessionClient(CT_ACCESSION, 
                                                (String) hostCombo.getSelectedItem(), p);

                BigInteger number = accession.getUIDNumber();
                tranText.setText("CT" + number.toString());
            }

            statusLbl.setText("Connection:  connected");
        } catch (Exception ex) {
            System.out.println("port p: " + ex.getMessage());
            statusLbl.setText("Connection:  " + ex.getMessage());
        }
    }

    void clrBtn_actionPerformed(ActionEvent e) {
        geneText.setText("");
        tranText.setText("");
    }
}