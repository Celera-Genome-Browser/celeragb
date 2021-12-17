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
package client.shared.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/**
 * Chooser class for setting preferences for a <code>GenomicSequenceViewer</code>.
 * can be used with or without reference to a <code>GenomicSequenceViewer</code> object,
 * but if used without the reference, <code>applySequenceSelctions()</code> and
 * <code>initCurrentSequence</code> won't do anything.
 */
public class GenomicOptionsChooser extends JPanel {
    protected JCheckBox orf1CB;
    protected JCheckBox orf2CB;
    protected JCheckBox orf3CB;
    protected JCheckBox orfNeg1CB;
    protected JCheckBox orfNeg2CB;
    protected JCheckBox orfNeg3CB;
    protected JCheckBox complementCB;
    protected JRadioButton oneLetterRB;
    protected JRadioButton threeLetterRB;
    protected GenomicSequenceViewer sequenceViewer;

    /**
     * Default Constructor, lays out all internal components and set's
     * it's internal <code>GenomicSequenceViewer</code> to <code>null</code>.
     */
    public GenomicOptionsChooser() {
        sequenceViewer = null;
        init();
    }

    /**
     * Creates a <code>GenomicOptionsChooser</code> panel that represents
     * the viewer settings for a <code>GenomicSequenceViewer</code>. If
     * <code>sequenceViewer</code> is <code>null</code> the panel's components
     * will be disabled.
     */
    public GenomicOptionsChooser(GenomicSequenceViewer sequenceViewer) {
        this.sequenceViewer = sequenceViewer;
        init();

        if (sequenceViewer != null) {
            initCurrentSequence();
        } else {
            disableAll();
        }
    }

    /**
     * Set's the chooser's associated <code>GenomicSequenceViewer</code>.
     */
    public void setGenomicSequenceViewer(GenomicSequenceViewer viewer) {
        sequenceViewer = viewer;
    }

    /**
     * Get's the chooser's associated <code>GenomicSequenceViewer</code>.
     */
    public GenomicSequenceViewer getGenomicSequenceViewer() {
        return sequenceViewer;
    }

    /**
     * Returns the state of the ORF_1 checkBox
     */
    public boolean getORF1Selected() {
        return orf1CB.isSelected();
    }

    /**
     * Returns the state of the ORF_2 checkBox
     */
    public boolean getORF2Selected() {
        return orf1CB.isSelected();
    }

    /**
     * Returns the state of the ORF_3 checkBox
     */
    public boolean getORF3Selected() {
        return orf1CB.isSelected();
    }

    /**
     * Sets the selection state for the ORF_1 checkBox
     */
    public void setORF1Selected(boolean isSelected) {
        orf1CB.setSelected(isSelected);
    }

    /**
     * Sets the selection state for the ORF_2 checkBox
     */
    public void setORF2Selected(boolean isSelected) {
        orf2CB.setSelected(isSelected);
    }

    /**
     * Sets the selection state for the ORF_3 checkBox
     */
    public void setORF3Selected(boolean isSelected) {
        orf3CB.setSelected(isSelected);
    }

    /**
     * Returns the state of the NegORF_1 checkBox
     */
    public boolean getNegORF1Selected() {
        return orfNeg1CB.isSelected();
    }

    /**
     * Returns the state of the NegORF_2 checkBox
     */
    public boolean getNegORF2Selected() {
        return orfNeg1CB.isSelected();
    }

    /**
     * Returns the state of the NegORF_3 checkBox
     */
    public boolean getNegORF3Selected() {
        return orfNeg1CB.isSelected();
    }

    /**
     * Sets the selection state for the NegORF_1 checkBox
     */
    public void setNegORF1Selected(boolean isSelected) {
        orfNeg1CB.setSelected(isSelected);
    }

    /**
     * Sets the selection state for the NegORF_2 checkBox
     */
    public void setNegORF2Selected(boolean isSelected) {
        orfNeg2CB.setSelected(isSelected);
    }

    /**
     * Sets the selection state for the NegORF_3 checkBox
     */
    public void setNegORF3Selected(boolean isSelected) {
        orfNeg3CB.setSelected(isSelected);
    }

    protected void init() {
        // set up check Boxes
        orf1CB = new JCheckBox("+1 ORF");
        orf1CB.setHorizontalAlignment(SwingConstants.CENTER);
        orf2CB = new JCheckBox("+2 ORF");
        orf2CB.setHorizontalAlignment(SwingConstants.CENTER);
        orf3CB = new JCheckBox("+3 ORF");
        orf3CB.setHorizontalAlignment(SwingConstants.CENTER);
        orfNeg1CB = new JCheckBox("-1 ORF");
        orfNeg1CB.setHorizontalAlignment(SwingConstants.CENTER);
        orfNeg2CB = new JCheckBox("-2 ORF");
        orfNeg2CB.setHorizontalAlignment(SwingConstants.CENTER);
        orfNeg3CB = new JCheckBox("-3 ORF");
        orfNeg3CB.setHorizontalAlignment(SwingConstants.CENTER);
        complementCB = new JCheckBox("Display Complement");

        JPanel orfPanel = new JPanel();
        orfPanel.setLayout(new GridLayout(2, 3));
        orfPanel.setBorder(BorderFactory.createTitledBorder(
                                   "Open Reading Frames"));
        orfPanel.add(orf1CB);
        orfPanel.add(orf2CB);
        orfPanel.add(orf3CB);
        orfPanel.add(orfNeg1CB);
        orfPanel.add(orfNeg2CB);
        orfPanel.add(orfNeg3CB);


        // set up radio buttons
        oneLetterRB = new JRadioButton("One Letter");
        oneLetterRB.setHorizontalAlignment(SwingConstants.CENTER);
        threeLetterRB = new JRadioButton("Three Letter");
        threeLetterRB.setHorizontalAlignment(SwingConstants.CENTER);

        ButtonGroup bGroup = new ButtonGroup();
        bGroup.add(oneLetterRB);
        bGroup.add(threeLetterRB);

        JPanel rbPanel = new JPanel();
        rbPanel.setBorder(BorderFactory.createTitledBorder("Translation Style"));
        rbPanel.setLayout(new BoxLayout(rbPanel, BoxLayout.Y_AXIS));
        rbPanel.add(oneLetterRB);
        rbPanel.add(threeLetterRB);
        complementCB = new JCheckBox("Display Complement");

        GridBagLayout gridbag = new GridBagLayout();
        setLayout(gridbag);
        add(orfPanel, 
            new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, 
                                   GridBagConstraints.NORTH, 
                                   GridBagConstraints.HORIZONTAL, 
                                   new Insets(4, 2, 4, 2), 0, 0));
        add(rbPanel, 
            new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, 
                                   GridBagConstraints.NORTH, 
                                   GridBagConstraints.HORIZONTAL, 
                                   new Insets(4, 2, 4, 2), 10, 0));
        add(complementCB, 
            new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 
                                   GridBagConstraints.NORTHWEST, 
                                   GridBagConstraints.NONE, 
                                   new Insets(4, 2, 4, 2), 0, 0));
    }

    /**
     * This method applies the chooser's settings to it's associated
     * <code>GenomicSequenceViewer</code>. It returns the adjusted
     * <code>GenomicSequenceViewer</code>. If it's associated viewer is
     * null, this method does nothing and returns null.
     */
    public GenomicSequenceViewer applySequenceSelections() {
        if (sequenceViewer != null) {
            // ORF Selections
            sequenceViewer.setSequenceVisible(
                    GenomicSequenceViewer.ORF_1_DISPLAY, orf1CB.isSelected());
            sequenceViewer.setSequenceVisible(
                    GenomicSequenceViewer.ORF_2_DISPLAY, orf2CB.isSelected());
            sequenceViewer.setSequenceVisible(
                    GenomicSequenceViewer.ORF_3_DISPLAY, orf3CB.isSelected());
            sequenceViewer.setSequenceVisible(
                    GenomicSequenceViewer.ORF_NEG1_DISPLAY, 
                    orfNeg1CB.isSelected());
            sequenceViewer.setSequenceVisible(
                    GenomicSequenceViewer.ORF_NEG2_DISPLAY, 
                    orfNeg2CB.isSelected());
            sequenceViewer.setSequenceVisible(
                    GenomicSequenceViewer.ORF_NEG3_DISPLAY, 
                    orfNeg3CB.isSelected());


            // Protein Translations
            sequenceViewer.setTranslationStyle(oneLetterRB.isSelected()
                                               ? GenomicSequenceViewer.SINGLE_CHAR_TRANSLATION
                                               : GenomicSequenceViewer.ABBREVIATED_TRANSLATION);


            // Display complement
            sequenceViewer.setSequenceVisible(
                    GenomicSequenceViewer.COMPLEMENT_DNA_DISPLAY, 
                    complementCB.isSelected());
        }

        return sequenceViewer;
    }

    /**
     * This method initializes the chooser's components to reflect the settings of
     * its associated <code>GenomicSequenceViewer</code>. If it's associated viewer is
     * null, this method does nothing.
     */
    public void initCurrentSequence() {
        if (sequenceViewer == null) {
            return;
        }


        // ORF Selections
        orf1CB.setSelected(sequenceViewer.isSequenceVisible(
                                   GenomicSequenceViewer.ORF_1_DISPLAY));
        orf2CB.setSelected(sequenceViewer.isSequenceVisible(
                                   GenomicSequenceViewer.ORF_2_DISPLAY));
        orf3CB.setSelected(sequenceViewer.isSequenceVisible(
                                   GenomicSequenceViewer.ORF_3_DISPLAY));
        orfNeg1CB.setSelected(sequenceViewer.isSequenceVisible(
                                      GenomicSequenceViewer.ORF_NEG1_DISPLAY));
        orfNeg2CB.setSelected(sequenceViewer.isSequenceVisible(
                                      GenomicSequenceViewer.ORF_NEG2_DISPLAY));
        orfNeg3CB.setSelected(sequenceViewer.isSequenceVisible(
                                      GenomicSequenceViewer.ORF_NEG3_DISPLAY));

        // Protein Translations
        boolean oneLet = sequenceViewer.getTranslationStyle() == GenomicSequenceViewer.SINGLE_CHAR_TRANSLATION;
        oneLetterRB.setSelected(oneLet);
        threeLetterRB.setSelected(!oneLet);


        // complement selection
        complementCB.setSelected(sequenceViewer.isSequenceVisible(
                                         GenomicSequenceViewer.COMPLEMENT_DNA_DISPLAY));
    }

    /**
     * Disables all checkboxes and buttons
     */
    protected void disableAll() {
        orf1CB.setEnabled(false);
        orf2CB.setEnabled(false);
        orf3CB.setEnabled(false);
        orfNeg1CB.setEnabled(false);
        orfNeg2CB.setEnabled(false);
        orfNeg3CB.setEnabled(false);
        complementCB.setEnabled(false);
        oneLetterRB.setEnabled(false);
        threeLetterRB.setEnabled(false);
    }

    /**
     * Creates and returns a new dialog containing the specified
     * <code>GenomicOptionsChooser</code> along with "OK", "Cancel", and "Apply"
     * buttons. If the "OK" or "Cancel" buttons are pressed, the dialog is
     * automatically hidden (but not disposed).  If the "Apply"
     * button is pressed, the genomic options chooser's values will be applied to
     * the <code>GenomicSequenceViewer</code> it's associated with.
     *
     * @param c              the parent component for the dialog
     * @param title          the title for the dialog
     * @param modal          a boolean. When true, the remainder of the program
     *                       is inactive until the dialog is closed.
     * @param genoptionsChooser    the genomicoptions-chooser to be placed inside the dialog
     * @param okListener     the ActionListener invoked when "OK" is pressed
     * @param cancelListener the ActionListener invoked when "Cancel" is pressed
     */
    public static JDialog createDialog(Component c, String title, boolean modal, 
                                       GenomicOptionsChooser genoptionsChooser, 
                                       ActionListener okListener, 
                                       ActionListener cancelListener) {
        return new GenomicOptionsChooserDialog(c, title, modal, 
                                               genoptionsChooser, okListener, 
                                               cancelListener);
    }

    /**
     * Shows a modal genomicoptions-chooser dialog and blocks until the
     * dialog is hidden.  If the user presses the "OK" button, then
     * this method hides/disposes the dialog and configures and returns a reference
     * to the <code>GenomicSequenceViewer</code> it's associated with.
     * If the user presses the "Cancel" button or closes the
     * dialog without pressing "OK", then this method hides/disposes the dialog
     * and returns <code>null</code>. if the user presses apply, it will apply the
     * values, but not dispose of the dialog.
     *
     * @param component    the parent <code>Component</code> for the dialog
     * @param title        the String containing the dialog's title
     * @param seqView      the associated <code>GenomicSequenceViewer</code>
     */
    public static GenomicSequenceViewer showDialog(Component parentComponent, 
                                                   String title, 
                                                   GenomicSequenceViewer seqView) {
        final GenomicOptionsChooser seqChoose = new GenomicOptionsChooser(
                                                        seqView);
        GenOptionsTracker genOpTracker = new GenOptionsTracker(seqChoose);
        JDialog dialog = createDialog(parentComponent, title, true, seqChoose, 
                                      genOpTracker, null);
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Window w = e.getWindow();
                w.setVisible(false);
            }
        });
        dialog.addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                Window w = (Window) e.getComponent();
                w.dispose();
            }
        });
        dialog.setVisible(true);

        return genOpTracker.getViewer();
    }
}

/**
 * Dialog Class used to present the GenomicOptionsChooser dialog
 */
class GenomicOptionsChooserDialog extends JDialog {
    private GenomicOptionsChooser genoptionsChooser;
    private JButton okButton;
    private JButton cancelButton;
    private JButton applyButton;

    protected GenomicOptionsChooserDialog() {
    }

    /**
     * Creates a new <code>Dialog</code> containing a genomicoptions-chooser.
     * @param c the parent for the dialog
     * @param title          the title string for the dialog
     * @param modal          a boolean. When true, the remainder of the program
     *                       is inactive until the dialog is closed.
     * @param genoptionsChooser    the genomicoptions-chooser to be placed inside the dialog
     * @param okListener     the ActionListener invoked when "OK" is pressed
     * @param cancelListener the ActionListener invoked when "Cancel" is pressed
     */
    public GenomicOptionsChooserDialog(Component c, String title, boolean modal, 
                                       GenomicOptionsChooser genoptionschooser, 
                                       ActionListener okListener, 
                                       ActionListener cancelListener) {
        super(JOptionPane.getFrameForComponent(c), title, modal);
        this.genoptionsChooser = genoptionschooser;

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        Box box = Box.createHorizontalBox();
        applyButton = new JButton("Apply");
        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                genoptionsChooser.applySequenceSelections();
            }
        });
        okButton = new JButton("Ok");
        getRootPane().setDefaultButton(okButton);
        okButton.setActionCommand("OK");

        if (okListener != null) {
            okButton.addActionListener(okListener);
        }

        okButton.addActionListener(new ActionListener() { // Default Hide Action
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("CANCEL");

        if (cancelListener != null) {
            cancelButton.addActionListener(cancelListener);
        }

        cancelButton.addActionListener(new ActionListener() { // Default Hide Action
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        box.add(Box.createHorizontalStrut(4));
        box.add(applyButton);
        box.add(Box.createHorizontalGlue());
        box.add(okButton);
        box.add(Box.createHorizontalStrut(8));
        box.add(cancelButton);
        box.add(Box.createHorizontalStrut(4));

        Box mainBox = Box.createVerticalBox();
        mainBox.add(genoptionsChooser);
        mainBox.add(Box.createVerticalStrut(10));
        mainBox.add(box);
        cp.add(mainBox, BorderLayout.CENTER);
        this.pack();
        setLocationRelativeTo(c);
        this.setResizable(false);
    }
}

/**
 * Action Listener used to apply the settings in a genomic options chooser to
 * a <code>GenomicSequenceViewer</code> and keep a reference to that viewer
 */
class GenOptionsTracker implements ActionListener {
    GenomicOptionsChooser opchooser;
    GenomicSequenceViewer seq;

    GenOptionsTracker(GenomicOptionsChooser c) {
        opchooser = c;
    }

    public void actionPerformed(ActionEvent e) {
        seq = opchooser.applySequenceSelections();
    }

    public GenomicSequenceViewer getViewer() {
        return seq;
    }
}