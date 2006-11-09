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
package client.gui.framework.bookmark;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import java.awt.event.*;

import client.gui.framework.bookmark.BookmarkInfo;
import client.shared.text_component.*;
/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

public class BookmarkPropertyDialog extends JDialog {
  Border border1;
  JLabel oidLabel = new JLabel();
  JLabel searchLabel = new JLabel();
  JLabel typeLabel = new JLabel();
  JLabel speciesLabel = new JLabel();
  JLabel urlLabel = new JLabel();
  JLabel commentsLabel = new JLabel();
  JPanel bookmarkPanel = new JPanel();
  Border border2;
  TitledBorder titledBorder2;
  JButton okButton = new JButton();
  JButton cancelButton = new JButton();
  StandardTextField oidTextField = new StandardTextField();
  StandardTextField searchTextField = new StandardTextField();
  StandardTextField typeTextField = new StandardTextField();
  StandardTextField speciesTextField = new StandardTextField();
  StandardTextField urlTextField = new StandardTextField();
  private BookmarkInfo targetInfo;
  private JFrame parentFrame;
  JScrollPane jScrollPane1 = new JScrollPane();
  StandardTextArea commentsTextArea = new StandardTextArea();

  public BookmarkPropertyDialog(JFrame owner, String title, boolean modal,
      BookmarkInfo targetInfo) {
    super(owner, title, modal);
    this.parentFrame = owner;
    this.targetInfo = targetInfo;
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    setUpValues();
    this.show();
  }


  private void setUpValues() {
    oidTextField.setText(targetInfo.getOid().toString());
    searchTextField.setText(targetInfo.getSearchValue());
    typeTextField.setText(targetInfo.getBookmarkType());
    speciesTextField.setText(targetInfo.getSpecies());
    urlTextField.setText(targetInfo.getBookmarkURLText());
    urlTextField.setCaretPosition(0);
    commentsTextArea.setText(targetInfo.getComments());
  }


  private void jbInit() throws Exception {
    border2 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(134, 134, 134));
    titledBorder2 = new TitledBorder(border2,"Bookmark Properties");
    oidLabel.setText("OID:");
    oidLabel.setBounds(new Rectangle(19, 28, 94, 27));
    border1 = new EtchedBorder(EtchedBorder.RAISED,Color.white,new Color(134, 134, 134));
    this.getContentPane().setLayout(null);
    searchLabel.setBounds(new Rectangle(19, 65, 94, 27));
    searchLabel.setText("Search Value:");
    typeLabel.setBounds(new Rectangle(19, 103, 94, 27));
    typeLabel.setText("Type:");
    speciesLabel.setBounds(new Rectangle(19, 139, 94, 27));
    speciesLabel.setText("Species:");
    urlLabel.setBounds(new Rectangle(19, 174, 94, 27));
    urlLabel.setText("URL:");
    commentsLabel.setText("Comments:");
    commentsLabel.setBounds(new Rectangle(19, 205, 94, 27));
    commentsTextArea.setLineWrap(true);
    commentsTextArea.setWrapStyleWord(true);
    commentsTextArea.setRows(4);

    bookmarkPanel.setBorder(titledBorder2);
    bookmarkPanel.setBounds(new Rectangle(6, 5, 324, 316));
    bookmarkPanel.setLayout(null);
    okButton.setText("OK");
    okButton.setBounds(new Rectangle(42, 329, 99, 31));
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        targetInfo.setComments(commentsTextArea.getText().trim());
        BookmarkPropertyDialog.this.dispose();
      }
    });
    cancelButton.setBounds(new Rectangle(185, 329, 99, 31));
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        BookmarkPropertyDialog.this.dispose();
      }
    });
    oidTextField.setEditable(false);
    oidTextField.setBounds(new Rectangle(118, 28, 191, 27));
    searchTextField.setEditable(false);
    searchTextField.setBounds(new Rectangle(118, 65, 191, 27));
    typeTextField.setEditable(false);
    typeTextField.setBounds(new Rectangle(118, 103, 191, 27));
    speciesTextField.setEditable(false);
    speciesTextField.setBounds(new Rectangle(118, 139, 191, 27));
    urlTextField.setEditable(false);
    urlTextField.setBounds(new Rectangle(118, 174, 191, 27));
    jScrollPane1.setBounds(new Rectangle(18, 228, 293, 79));
    this.getContentPane().add(bookmarkPanel, null);
    bookmarkPanel.add(speciesLabel, null);
    bookmarkPanel.add(searchLabel, null);
    bookmarkPanel.add(typeLabel, null);
    bookmarkPanel.add(oidLabel, null);
    bookmarkPanel.add(urlLabel, null);
    bookmarkPanel.add(oidTextField, null);
    bookmarkPanel.add(searchTextField, null);
    bookmarkPanel.add(typeTextField, null);
    bookmarkPanel.add(speciesTextField, null);
    bookmarkPanel.add(urlTextField, null);
    bookmarkPanel.add(commentsLabel, null);
    bookmarkPanel.add(jScrollPane1, null);
    jScrollPane1.getViewport().add(commentsTextArea, null);
    this.getContentPane().add(okButton, null);
    this.getContentPane().add(cancelButton, null);
    this.setSize(345, 397);
    this.setLocationRelativeTo(parentFrame);
  }
}