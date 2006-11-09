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
package client.gui.components.annotation.transcript_translate_view;
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/
import api.entity_model.access.command.DoAddStartSite;
import api.entity_model.access.command.DoAddStopSite;
import api.entity_model.access.command.DoDeleteCuration;
import api.entity_model.management.Command;
import api.entity_model.management.CommandPreconditionException;
import api.entity_model.management.ModifyManager;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.CuratedCodon;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.EntityType;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.stub.geometry.MutableRange;
import api.stub.sequence.DNA;
import api.stub.sequence.SequenceUtil;
import client.gui.framework.browser.Browser;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.framework.session_mgr.SessionModelListener;
import client.shared.swing.GenomicSequenceViewer;
import client.shared.swing.genomic.SequenceMouseEvent;
import client.shared.swing.genomic.SequenceMouseListener;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

public class TranscriptTranslateCurationHandler {

  private static boolean debug_events = false;
  private static final String CURATION_ENABLED = "CurationEnabled";
  private Browser browser;
  private BrowserModel browserModel;
  private TranscriptTranslateView view;
  private GenomicSequenceViewer seqViewer;

  private JMenu modeMenu;
  private JCheckBoxMenuItem editModeMI;
  private JCheckBoxMenuItem selectionModeMI;

  private boolean editing_enabled = false;
  private boolean selection_enabled = true;

  private JPopupMenu curatePopup;
  private JPopupMenu promotedPopup;
  private JMenuItem curateInfoMI, setStartCodonMI, deleteStartFromDatabaseMI,
    removeStartMI, setTranslationStartCodonMI, setStopCodonMI;
  public JMenuItem editUndoMI;
  private JMenuItem setLongestORFMI;
  private JMenuItem setLongestAtgToStopMI;
  private long mouse_down_coord;

  private boolean isEditable = false;

  private MyMouseHandler mouseHandler = new MyMouseHandler();
  private MyActionListener actionListener = new MyActionListener();
  private SessionModelListener sessionModelListener = new MySessionModelListener();


  public TranscriptTranslateCurationHandler(Browser browser, TranscriptTranslateView view,
                           GenomicSequenceViewer seqViewer) {
    this.browser = browser;
    this.browserModel = browser.getBrowserModel();
    this.seqViewer = seqViewer;
    if (browserModel.getCurrentSelection()!=null)
      isEditable = !(browserModel.getMasterEditorEntity().getGenomeVersion().isReadOnly());
    SessionMgr.getSessionMgr().addSessionModelListener(sessionModelListener);
    this.view = view;
    setUpMenus();
  }

  private void setUpMenus() {
    modeMenu = new JMenu("Modes");
    selectionModeMI = new JCheckBoxMenuItem("Selection");
    selectionModeMI.setState(selection_enabled);
    selectionModeMI.addActionListener(actionListener);
    modeMenu.add(selectionModeMI);
    if ( isEditable ) {
      editModeMI = new JCheckBoxMenuItem("Curation Editing");
      if (SessionMgr.getSessionMgr().getModelProperty(CURATION_ENABLED)!=null) {
          editModeMI.setState(((Boolean)SessionMgr.getSessionMgr().getModelProperty(CURATION_ENABLED)).booleanValue());
          editing_enabled = ((Boolean)SessionMgr.getSessionMgr().getModelProperty(CURATION_ENABLED)).booleanValue();
      }
      else {
          editModeMI.setState(editing_enabled);
          SessionMgr.getSessionMgr().setModelProperty(CURATION_ENABLED,new Boolean(editing_enabled));
      }
      editModeMI.addActionListener(actionListener);
      // only listen for mouse events when editing is enabled
      if (editing_enabled) {
         seqViewer.addSequenceMouseListener(mouseHandler);
      }
      else {
        seqViewer.removeSequenceMouseListener(mouseHandler);
      }
      seqViewer.setEditable(editing_enabled);

      modeMenu.add(editModeMI);

      editUndoMI = new JMenuItem("Undo Curation Editing");
      editUndoMI.setEnabled(editing_enabled);
      // Let the view respond to this menu item... it's more cohesive with it's
      // other responsibilities.
      editUndoMI.addActionListener(view);
      modeMenu.add(editUndoMI);

      curateInfoMI = new JMenuItem("Curation Options:");
      setStartCodonMI = new JMenuItem("Set Start Codon");
      setStartCodonMI.addActionListener(actionListener);
      setStartCodonMI.setEnabled(true);

      setTranslationStartCodonMI = new JMenuItem("Set Translation Start");
      setTranslationStartCodonMI.addActionListener(actionListener);
      setTranslationStartCodonMI.setEnabled(true);

      setLongestORFMI = new JMenuItem("Set Longest Open Reading Frame");
      setLongestORFMI.addActionListener(actionListener);
      setLongestORFMI.setEnabled(true);

      setLongestAtgToStopMI = new JMenuItem("Set Longest ATG to Stop");
      setLongestAtgToStopMI.addActionListener(actionListener);
      setLongestAtgToStopMI.setEnabled(true);


      removeStartMI = new JMenuItem("Delete Start Codon");
      removeStartMI.addActionListener(actionListener);
      removeStartMI.setEnabled(true);

      deleteStartFromDatabaseMI = new JMenuItem("Delete Start Codon From Database");
      deleteStartFromDatabaseMI.addActionListener(actionListener);
      deleteStartFromDatabaseMI.setEnabled(true);


      setStopCodonMI = new JMenuItem("Set Stop Codon to Calculate ORF Upstream");
      setStopCodonMI.addActionListener(actionListener);
      setStopCodonMI.setEnabled(true);

      promotedPopup = new JPopupMenu();
      promotedPopup.add(new JMenuItem("Promoted Feature: No Curation Options"));

      curatePopup = new JPopupMenu();
      curatePopup.add(curateInfoMI);
      curatePopup.add(setStartCodonMI);
      curatePopup.add(setTranslationStartCodonMI);
      curatePopup.add(setLongestORFMI);
      curatePopup.add(setLongestAtgToStopMI);
      curatePopup.add(removeStartMI);
      curatePopup.add(deleteStartFromDatabaseMI);
      curatePopup.add(setStopCodonMI);
    }
  }

  public JMenu[] getMenus() {
    return new JMenu[]{modeMenu};
  }

  private void handleDeleteStartCodon() {
      Feature feature = view.getDisplayedFeature();
      if ( !(feature instanceof CuratedTranscript) || !feature.isWorkspace()) {
        System.err.println("Error: can only remove start on scratch curated transcripts!");
        return;
      }
      CuratedTranscript transcript = (CuratedTranscript)feature;
      CuratedCodon start=transcript.getStartCodon();
      if(start!=null){
        ModifyManager.getModifyMgr().doCommand(new DoDeleteCuration(start.getOnlyGeometricAlignmentToOnlyAxis()));
      }
  }

  private void handleDeleteStartCodonFromDatabase() {
      Feature feature = view.getDisplayedFeature();
      if ( !(feature instanceof CuratedTranscript) || !feature.isWorkspace()) {
        System.err.println("Error: can only remove start on scratch curated transcripts!");
        return;
      }
      CuratedTranscript transcript = (CuratedTranscript)feature;
      CuratedCodon startCodon = transcript.getStartCodon();
      if (startCodon != null)
        ModifyManager.getModifyMgr().doCommand(new DoDeleteCuration(startCodon.getOnlyGeometricAlignmentToOnlyAxis(),true));
  }

  void setRemoveStartMenuState(boolean enabled) {
    if(removeStartMI!=null){
    removeStartMI.setEnabled(enabled);
   }
  }

  void setDeleteStartFromDatabaseMenuState(boolean enabled) {
    if(deleteStartFromDatabaseMI!=null){
      deleteStartFromDatabaseMI.setEnabled(enabled);
    }
  }


  private void handleStartCodonSet(int spliced_position) {
     this.handleStartCodonSet(EntityType.getEntityTypeForValue(EntityTypeConstants.Start_Codon_Start_Position),spliced_position);
  }


  private void handleTranslationStartCodonSet(int spliced_position) {
     this.handleStartCodonSet(EntityType.getEntityTypeForValue(EntityTypeConstants.Translation_Start_Position) ,spliced_position);
  }


  private void handleStartCodonSet(EntityType entityType, int spliced_position) {
    Feature feature = view.getDisplayedFeature();
    if (!(feature instanceof CuratedTranscript) || !feature.isWorkspace()) {
      System.err.println("Error: can only set start of translation on scratch curated transcripts!");
      return;
    }
    GenomicAxis axis=(GenomicAxis)browserModel.getMasterEditorEntity();
    CuratedTranscript transcript = (CuratedTranscript)feature;
    int currIndex=transcript.transformSplicedPositionToAxisPosition(spliced_position);


    int codonLen = 3;

    // decide which direction codon is pointing
    Collection transcriptAlignments = transcript.getAlignmentsToAxis(axis);
    boolean useForwardStrand =!((GeometricAlignment)transcriptAlignments.iterator().next()).getRangeOnAxis().isReversed();

    int cpos, clen;

    cpos = currIndex;
    if (useForwardStrand) clen = codonLen;
    else clen = -codonLen;



    try{
      Command c=(new DoAddStartSite(axis,new MutableRange(cpos,cpos+clen),transcript,entityType));
      c.validatePreconditions();
      ModifyManager.getModifyMgr().doCommand(c);
     }
      catch(CommandPreconditionException c){
      JOptionPane messagePane=new JOptionPane();
        messagePane.showMessageDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
          c.getMessage(), "Command Precondition Error", JOptionPane.ERROR_MESSAGE);
      c=null;
    }
  }


 /**
  will return true if the base pairs contain atg string
 */
 private boolean canAddStart(CuratedTranscript t, int splicedPos){
    CuratedTranscript transcript = t;
    int currIndex=transcript.transformSplicedPositionToAxisPosition(splicedPos);
    int codonLen = 3;
    EntityType codonType = EntityType.getEntityTypeForValue(EntityTypeConstants.Start_Codon_Start_Position);
    GenomicAxis axis=(GenomicAxis)browserModel.getMasterEditorEntity();
    // decide which direction codon is pointing
    Collection transcriptAlignments = transcript.getAlignmentsToAxis(axis);
    boolean useForwardStrand =!((GeometricAlignment)transcriptAlignments.iterator().next()).getRangeOnAxis().isReversed();


    int cpos, clen;
    cpos = currIndex;
    if (useForwardStrand) clen = codonLen;
    else clen = -codonLen;

    try{
      Command c=(new DoAddStartSite(axis,new MutableRange(cpos,cpos+clen),transcript,codonType));
      c.validatePreconditions();
      return true;
    }
    catch(CommandPreconditionException c){
      c=null;
      return false;
    }
 }


  /**
  will return true if the base pairs contain atg string
 */

 private boolean canAddStop(CuratedTranscript t, int splicedPos){
    CuratedTranscript transcript = t;
    int currIndex=transcript.transformSplicedPositionToAxisPosition(splicedPos);
    int codonLen = 3;
    EntityType codonType = EntityType.getEntityTypeForValue(EntityTypeConstants.StopCodon);
    GenomicAxis axis=(GenomicAxis)browserModel.getMasterEditorEntity();
    // decide which direction codon is pointing
    Collection transcriptAlignments = transcript.getAlignmentsToAxis(axis);
    boolean useForwardStrand =!((GeometricAlignment)transcriptAlignments.iterator().next()).getRangeOnAxis().isReversed();


    int cpos, clen;
    cpos = currIndex;
    if (useForwardStrand) clen = codonLen;
    else clen = -codonLen;

    try{
      Command c=(new DoAddStopSite(axis,new MutableRange(cpos,cpos+clen),transcript,codonType));
      c.validatePreconditions();
      return true;
    }
    catch(CommandPreconditionException c){
     c=null;
     return false;
    }
 }



  private void handleSetStopCodon(int spliced_position) {
    Feature feature = view.getDisplayedFeature();

    if (!(feature instanceof CuratedTranscript) || !feature.isWorkspace()) {
      System.err.println("Error: can only set stops on scratch curated transcripts!");
      return;
    }
    GenomicAxis axis=(GenomicAxis)browserModel.getMasterEditorEntity();
    CuratedTranscript transcript = (CuratedTranscript)feature;
    int currIndex=transcript.transformSplicedPositionToAxisPosition(spliced_position);


    int codonLen = 3;
    EntityType codonType = EntityType.getEntityTypeForValue(EntityTypeConstants.StopCodon);

    // decide which direction codon is pointing
    Collection transcriptAlignments = transcript.getAlignmentsToAxis(axis);
    boolean useForwardStrand =!((GeometricAlignment)transcriptAlignments.iterator().next()).getRangeOnAxis().isReversed();

    int cpos, clen;

    cpos = currIndex;
    if (useForwardStrand) clen = codonLen;
    else clen = -codonLen;


    try{
      Command c=(new DoAddStopSite(axis,new MutableRange(cpos,cpos+clen),transcript,codonType));
      c.validatePreconditions();
      ModifyManager.getModifyMgr().doCommand(c);
     }
      catch(CommandPreconditionException c){
        JOptionPane messagePane=new JOptionPane();
        messagePane.showMessageDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
          c.getMessage(), "Command Precondition Error", JOptionPane.ERROR_MESSAGE);
        c=null;
    }
  }


  private void handleSetLongestORF() {
    handleSetLongestORF(false, false);
  }


  /**
   * Find the longest possible translation for a transcript between two in frame stops. This method sets a
   * translation start codon at the beginning of the orf and a stop codon if one exists at the end of the orf.
   * Note: this method only deals with ATG starts.
   */
   private void handleSetLongestORF(boolean onlyAtgStart, boolean setRealStart) {

     Feature feature = view.getDisplayedFeature();

     if ((feature instanceof CuratedTranscript) && (feature.isWorkspace())) {
       CuratedTranscript transcript = (CuratedTranscript) feature;
       String spliceRes = DNA.toString(transcript.getSplicedResidues());
       int[] stopLocations = SequenceUtil.getSequenceUtil().findPatternLocations(spliceRes, "TAG|TGA|TAA", 0);
       MutableRange currORF;
       MutableRange maxORF = new MutableRange(0,0);
       int maxStartIndex = -1;
       int stopPos;
       int[] startLocations = null;


       if (onlyAtgStart) {
         startLocations = SequenceUtil.getSequenceUtil().findPatternLocations(spliceRes, "ATG", 0);
       }
       else {
         startLocations = new int[stopLocations.length+3];

         for (int i=0; i < stopLocations.length; i++) {
            startLocations[i] = stopLocations[i] + 3;
         }

         startLocations[stopLocations.length] = 0;        //check from the beginning at frames 1,2,3
         startLocations[stopLocations.length+1] = 1;
         startLocations[stopLocations.length+2] = 2;
       }


       for (int i=0; i < startLocations.length; i++) {
         stopPos = transcript.getNextInFrameStopPosition(startLocations[i],spliceRes);
         if (stopPos < 0) stopPos = spliceRes.length()-1;
         currORF = new MutableRange(startLocations[i], stopPos);
         if (currORF.getMagnitude() > maxORF.getMagnitude()) {
              maxORF = currORF;
              maxStartIndex = i;
         }
       }

       if (maxStartIndex >= 0) {
         if (setRealStart) {
           handleStartCodonSet(startLocations[maxStartIndex]);
         }
         else {
           handleTranslationStartCodonSet(startLocations[maxStartIndex]);
         }
       }
      }
   }


/**
   * Find the longest possible translation for a transcript between two in frame stops.
   * translation start codon at the beginning of the orf
   */
   private void handleSetORFGivenStop(int splicedStopPos) {

     Feature feature = view.getDisplayedFeature();

     if ((feature instanceof CuratedTranscript) && (feature.isWorkspace())) {
       CuratedTranscript transcript = (CuratedTranscript) feature;
       String spliceRes = DNA.toString(transcript.getSplicedResidues());
       transcript.getNextInFrameStopPosition(splicedStopPos,spliceRes);
    }
   }


  /**
   * handle any cleanup prior to getting rid of a TransTransCurationHandler
   */
  void dispose() {
      SessionMgr.getSessionMgr().removeSessionModelListener(sessionModelListener);
      if (this.editing_enabled)
          seqViewer.removeSequenceMouseListener(mouseHandler);
  }


  /**
   * Private inner class for handling model property events.
   */
  private class MySessionModelListener implements SessionModelListener {
    public void browserAdded(BrowserModel browserModel){}
    public void browserRemoved(BrowserModel browserModel){}
    public void sessionWillExit(){}
    public void modelPropertyChanged(Object property,Object oldValue, Object newValue) {
      if (property.equals(CURATION_ENABLED)) {
        editModeMI.setState(((Boolean)newValue).booleanValue());
        editing_enabled = editModeMI.getState();
        seqViewer.setEditable(editing_enabled);
        // only listen for mouse events when editing is enabled
        if (editing_enabled) {
          seqViewer.addSequenceMouseListener(mouseHandler);
        }
        else {
          seqViewer.removeSequenceMouseListener(mouseHandler);
        }
      }
    }
  }


  /**
   * inner class for handling mouse events
   */
   private class MyMouseHandler implements SequenceMouseListener {
      public void mouseMoved(SequenceMouseEvent e){}
      public void mouseSelectedRange(SequenceMouseEvent e){}
      public void mouseReleased(SequenceMouseEvent e){}

      public void mousePressed(SequenceMouseEvent e) {
        if (!editing_enabled) { return; }
        // handling event if ocurring on annotation map
        if (e instanceof SequenceMouseEvent) {
          SequenceMouseEvent sevt = (SequenceMouseEvent) e;
          mouse_down_coord = sevt.getBeginLocation();

          // If it was a right-mouse:
          if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {
            Feature feature = view.getDisplayedFeature();
            if ((feature==null) || (!feature.isWorkspace())) {
              promotedPopup.show((Component)sevt.getSource(), sevt.getX(), sevt.getY());
              return;
            }

            if(feature instanceof CuratedTranscript && canAddStart((CuratedTranscript)feature,(int)mouse_down_coord)){
              setStartCodonMI.setEnabled(true);
            }else{
              setStartCodonMI.setEnabled(false);
            }
            if(feature instanceof CuratedTranscript && canAddStop((CuratedTranscript)feature,(int)mouse_down_coord)){
              setStopCodonMI.setEnabled(true);
            }else{
              setStopCodonMI.setEnabled(false);
            }
            curatePopup.show((Component)sevt.getSource(), sevt.getX(), sevt.getY());
          }
        }
      }
   }

   /**
    * inner class for action listening
    */
    private class MyActionListener implements ActionListener {

      public void actionPerformed(ActionEvent evt) {
        Object src = evt.getSource();
        if (src == editModeMI) {
          //      System.out.println("edit mode = " + editModeMI.getState());
          editing_enabled = editModeMI.getState();
          SessionMgr.getSessionMgr().setModelProperty(CURATION_ENABLED,
            new Boolean(editModeMI.getState()));
          // only listen for mouse events when editing is enabled
          if (editing_enabled) {
            seqViewer.addSequenceMouseListener(mouseHandler);
          }
          else {
            seqViewer.removeSequenceMouseListener(mouseHandler);
          }
          // <JTS>
          seqViewer.setEditable(editing_enabled);
        }
        else if (src == selectionModeMI) {
          //      System.out.println("selection mode = " + selectionModeMI.getState());
          selection_enabled = selectionModeMI.getState();
          seqViewer.setSelectable(selection_enabled);
          // make sure to clear previous highlighting
          seqViewer.clearSelection();
          view.setSelectionEnabledState(selection_enabled);
        }
        else {
          long base_position = mouse_down_coord;
          if (src == setStartCodonMI && isEditable ) {
            handleStartCodonSet((int)base_position);
          }
          else if (src == setTranslationStartCodonMI && isEditable) {
                handleTranslationStartCodonSet((int)base_position);
          }
          else if (src == removeStartMI && isEditable) {
            handleDeleteStartCodon();
          }
          else if (src == deleteStartFromDatabaseMI && isEditable) {
            handleDeleteStartCodonFromDatabase();
          }
          else if (src == setLongestORFMI && isEditable) {
            //System.out.println("calling handleSetLongestORF");
            handleSetLongestORF();
          }

          else if (src == setLongestAtgToStopMI && isEditable) {
            handleSetLongestORF(true, true);
          }

          else if (src == setStopCodonMI && isEditable) {
            handleSetStopCodon((int)base_position);
          }
        }
    }
  }
}
