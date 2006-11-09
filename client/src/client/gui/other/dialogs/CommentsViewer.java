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
package client.gui.other.dialogs;

import api.entity_model.access.command.DoAddComment;
import api.entity_model.access.comparator.GenomicEntityCommentComparator;
import api.entity_model.management.CompositeCommand;
import api.entity_model.management.ModelMgr;
import api.entity_model.management.ModifyManager;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.fundtype.Axis;
import api.stub.data.GenomicEntityComment;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.SessionMgr;
import client.shared.text_component.StandardTextArea;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

/**
 * @author Peter Davies
 */

public class CommentsViewer extends JDialog {
  private JSplitPane jSplitPane1 = new JSplitPane();
  private JPanel newPanel = new JPanel();
  private JPanel existingPanel = new JPanel();
  private JTextArea existingCommentTA = new StandardTextArea();
  private JTextArea newCommentTA = new StandardTextArea();
  private JButton addCommentBtn = new JButton();
  private JPanel btnPanel = new JPanel();
  private JButton okBtn = new JButton();
  private JButton cancelBtn = new JButton();
  private boolean readOnly=false;
  private CompositeCommand compositeCommand = new CompositeCommand("Modify Comments");
  private Feature currentFeature;
  static private final String lineSep=System.getProperty("line.separator");


  public CommentsViewer(BrowserModel browserModel, String propertyName) {
      super(SessionMgr.getSessionMgr().getBrowserFor(browserModel),true);
      currentFeature = (Feature)browserModel.getCurrentSelection();
      if (!currentFeature.isWorkspace()) readOnly=true;
      try {
        jbInit();
        pack();
        setLocationRelativeTo(SessionMgr.getSessionMgr().getBrowserFor(browserModel));
      }
      catch(Exception e) {
        e.printStackTrace();
      }

      Set currentCommentsOnFeature = currentFeature.getComments(new GenomicEntityCommentComparator());
      for (Iterator it=currentCommentsOnFeature.iterator();it.hasNext();) {
         existingCommentTA.append(
            it.next().toString()+lineSep );
      }
      this.show();
  }


  private void jbInit() throws Exception {
    this.setTitle("Comments for "+currentFeature.toString());
   // this.setIconImage((new ImageIcon(this.getClass().getResource(System.getProperty("x.genomebrowser.WindowCornerLogo"))).getImage()));

    //OK-Cancel Button panel
    okBtn.setSelected(true);
    okBtn.setText("OK");
    cancelBtn.setText("Cancel");
    btnPanel.setLayout(new FlowLayout());

    if (!readOnly&&
      ((Boolean)SessionMgr.getSessionMgr().getModelProperty("CurationEnabled")).booleanValue()&&
      !currentFeature.getGenomeVersion().isReadOnly()) {   //if read-write, show add comments part of dialog
        //Outer split Panel
        jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setOneTouchExpandable(true);

        //Top of split panel
        newPanel.setBorder(new TitledBorder("New Comments"));
        newPanel.setLayout(new BorderLayout(5, 5));
        addCommentBtn.setText("Add Comment");
        addCommentBtn.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String newText=newCommentTA.getText().trim();
            if (!newText.equals("")) {
              GenomicEntityComment commentEntry = new GenomicEntityComment(newText);
              Set allAlignments = currentFeature.getAlignmentsToAxes();
     // Browser[] browsers=(Browser[])browserList.toArray(new Browser[0]);
              Alignment[] alignments = (Alignment[])allAlignments.toArray(new Alignment[0]);
              if (alignments.length < 1) {
                ModelMgr.getModelMgr().handleException(new Exception(
                              "Comments can not be set on an element without any alignments."));
              }
              else {
                Axis anAxis = alignments[0].getAxis();
                compositeCommand.addNextCommand(new DoAddComment(anAxis,
                                                                 currentFeature,
                                                                 commentEntry));
                existingCommentTA.append(commentEntry.toString() + "\n");
                newCommentTA.setText("");
              }
            }
          }
        });
        newCommentTA.setLineWrap(true);
        newCommentTA.setWrapStyleWord(true);
        newCommentTA.setRows(4);
        JScrollPane newCommentSP=new JScrollPane(newCommentTA);
        JPanel spacerPanel=new JPanel();
        spacerPanel.setLayout(new BoxLayout(spacerPanel,BoxLayout.X_AXIS));
        spacerPanel.add(addCommentBtn);
        spacerPanel.add(Box.createGlue());
        newPanel.add(spacerPanel, BorderLayout.NORTH);
        newPanel.add(newCommentSP, BorderLayout.CENTER);

        //Split Pane
        jSplitPane1.add(newPanel, JSplitPane.TOP);
        jSplitPane1.add(existingPanel, JSplitPane.BOTTOM);

        //add panels to ContentPane
        this.getContentPane().add(jSplitPane1, BorderLayout.CENTER);
        btnPanel.add(okBtn,null);
        btnPanel.add(Box.createHorizontalStrut(10));
        btnPanel.add(cancelBtn,null);
        okBtn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
              if (!newCommentTA.getText().equals("")) {
                JOptionPane.showMessageDialog(jSplitPane1,"A new comment was written, but not added to the comment list. "+
                  "\nPlease either clear the comment, or add the comment to the comment list using the button.",
                  "Problem with Comments",JOptionPane.ERROR_MESSAGE);
                return;
              }
               ModifyManager.getModifyMgr().doCommand(compositeCommand);
               CommentsViewer.this.hide();
               CommentsViewer.this.dispose();
            }
        });
        cancelBtn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
               CommentsViewer.this.hide();
               CommentsViewer.this.dispose();
            }
        });
    }
    else  {
        this.getContentPane().add(existingPanel, BorderLayout.CENTER);
        btnPanel.add(okBtn,null);
        okBtn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
               CommentsViewer.this.hide();
               CommentsViewer.this.dispose();
            }
        });
    }


    this.getContentPane().add(btnPanel, BorderLayout.SOUTH);

    //Bottom of split pane or whole dialog if readonly
    existingCommentTA.setLineWrap(true);
    existingCommentTA.setWrapStyleWord(true);
    existingCommentTA.setEnabled(false);
    existingCommentTA.setRows(4);
    JScrollPane existingCommentSP=new JScrollPane(existingCommentTA);
    existingCommentSP.setPreferredSize(new Dimension(400,150));
    existingPanel.setLayout(new BorderLayout(5, 5));
    existingPanel.add(existingCommentSP);
    existingPanel.setBorder(new TitledBorder("Existing Comments"));

  }
}
