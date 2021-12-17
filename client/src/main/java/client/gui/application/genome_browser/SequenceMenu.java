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
package client.gui.application.genome_browser;

import api.entity_model.access.observer.SequenceObserver;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.GenomicEntity;
import api.stub.geometry.Range;
import api.stub.sequence.DNA;
import api.stub.sequence.Sequence;
import client.gui.framework.browser.Browser;
import client.gui.framework.session_mgr.BrowserModel;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies (peter.davies)
 * @version $Id$
 */
public class SequenceMenu extends JMenu {
    static Clipboard clipboard = Toolkit.getDefaultToolkit()
                                        .getSystemClipboard();
    private BrowserModel model;
    private JMenuItem fwd;
    private JMenuItem rev;
    private Browser browser;

    public SequenceMenu(Browser browser) {
        this.browser = browser;
        setText("Copy Sequence");
        model = browser.getBrowserModel();
        fwd = new JMenuItem("Copy Forward Sequence");
        add(fwd);
        fwd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fwd.setEnabled(false);
                copySeq(true);
            }
        });
        rev = new JMenuItem("Copy Reverse Complement Sequence");
        add(rev);
        rev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rev.setEnabled(false);
                copySeq(false);
            }
        });
    }

    private void copySeq(boolean forward) {
        GenomicEntity entity = model.getMasterEditorEntity();

        if (!(entity instanceof GenomicAxis)) {
            return;
        }

        GenomicAxis axis = (GenomicAxis) entity;
        axis.loadNucleotideSeq(model.getMasterEditorSelectedRange(), 
                               new MySequenceObserver(forward));
    }

    private class MySequenceObserver implements SequenceObserver, ClipboardOwner {
        private boolean forward;

        private MySequenceObserver(boolean forward) {
            this.forward = forward;
        }

        public void lostOwnership(Clipboard clipboard, Transferable contents) {
        }

        public void noteSequenceArrived(Axis axis, Range rangeOfSequence, 
                                        Sequence sequence) {
            if (forward) {
                clipboard.setContents(
                        new StringSelection(DNA.toString(sequence)), this);
                SequenceMenu.this.fwd.setEnabled(true);
            } else {
                clipboard.setContents(
                        new StringSelection(DNA.toString(DNA.reverseComplement(
                                                                 sequence))), 
                        this);
                SequenceMenu.this.rev.setEnabled(true);
            }

            String direction;

            if (forward) {
                direction = "forward";
            } else {
                direction = "reverse complement";
            }

            JOptionPane.showMessageDialog(browser, 
                                          "The " + direction + 
                                          " sequence for the selected range has been placed on the clipboard", 
                                          "Copy Complete", 
                                          JOptionPane.INFORMATION_MESSAGE);
        }
    }
}