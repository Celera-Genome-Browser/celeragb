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
package client.gui.components.annotation.consensus_sequence_view;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

import api.entity_model.access.command.DoMakeCurationFromScratch;
import api.entity_model.access.command.DoModifyEntityEdges;
import api.entity_model.management.ModifyManager;
import api.entity_model.model.annotation.CuratedExon;
import api.entity_model.model.annotation.CuratedFeature;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.GenomicEntity;
import api.stub.geometry.MutableRange;
import client.gui.framework.browser.Browser;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.framework.session_mgr.SessionModelListener;
import client.shared.swing.GenomicSequenceViewer;
import client.shared.swing.genomic.Adornment;
import client.shared.swing.genomic.SequenceMouseEvent;
import client.shared.swing.genomic.SequenceMouseListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class ConsensusSequenceCurationHandler implements ActionListener {
    private static final boolean DEBUG_CLASS = false;
    private static final String CURATION_ENABLED = "CurationEnabled";
    private Browser browser;
    private BrowserModel browserModel;
    private ConsensusSequenceView editor;
    private GenomicSequenceViewer seqViewer;
    private JMenu modeMenu;
    JCheckBoxMenuItem editModeMI;
    JCheckBoxMenuItem selectionModeMI;
    JMenuItem forTransFromSelectionMI, revTransFromSelectionMI, editUndoMI;
    boolean editing_enabled = false;
    boolean selection_enabled = true;
    boolean isEditable = false;
    private SessionModelListener sessionModelListener = new MySessionModelListener();
    private MySequenceMouseListener mouseHandler = new MySequenceMouseListener();
    private GenomicEntity curatedEntity = null;
    private Adornment curatedAdornment = null;



    public ConsensusSequenceCurationHandler(Browser browser, ConsensusSequenceView editor,
          GenomicSequenceViewer seqViewer) {
        this.browser = browser;
        this.browserModel = browser.getBrowserModel();
        isEditable = !(browserModel.getMasterEditorEntity().getGenomeVersion().isReadOnly());
        SessionMgr.getSessionMgr().addSessionModelListener(sessionModelListener);
        this.editor = editor;
        this.seqViewer = seqViewer;
        setUpMenus();
    }

    void setUpMenus() {
        modeMenu = new JMenu("Modes");
        modeMenu.setMnemonic('M');
        selectionModeMI = new JCheckBoxMenuItem("Selection");
        if (isEditable) {
            editModeMI = new JCheckBoxMenuItem("Curation Editing");
            forTransFromSelectionMI = new JMenuItem("Make Forward Transcript");
            revTransFromSelectionMI = new JMenuItem("Make Reverse Transcript");
            if (SessionMgr.getSessionMgr().getModelProperty(CURATION_ENABLED)!=null) {
              editModeMI.setState(((Boolean)SessionMgr.getSessionMgr().getModelProperty(CURATION_ENABLED)).booleanValue());
              editing_enabled = ((Boolean)SessionMgr.getSessionMgr().getModelProperty(CURATION_ENABLED)).booleanValue();
            }
            else {
              editModeMI.setState(editing_enabled);
              SessionMgr.getSessionMgr().setModelProperty(CURATION_ENABLED,new Boolean(editing_enabled));
            }
            forTransFromSelectionMI.setEnabled(editing_enabled);
            revTransFromSelectionMI.setEnabled(editing_enabled);

            // only listen for mouse events when editing is enabled
            if (editing_enabled) {
                 seqViewer.addSequenceMouseListener(mouseHandler);
            }
            else {
                 seqViewer.removeSequenceMouseListener(mouseHandler);
            }
            seqViewer.setEditable(editing_enabled);
            editModeMI.addActionListener(this);

            forTransFromSelectionMI.addActionListener(this);
            revTransFromSelectionMI.addActionListener(this);
            modeMenu.add(editModeMI);
            modeMenu.add(forTransFromSelectionMI);
            modeMenu.add(revTransFromSelectionMI);

            editUndoMI = new JMenuItem("Undo Curation Editing");
            editUndoMI.setEnabled(editing_enabled);
            // Let the view respond to this menu item... it's more cohesive with it's
            // other responsibilities.
            editUndoMI.addActionListener(editor);
            modeMenu.add(editUndoMI);
        }
        selectionModeMI.setState(selection_enabled);
        selectionModeMI.addActionListener(this);
        modeMenu.add(selectionModeMI);
    }  // end void setUpMenus()

    public JMenu[] getMenus() {
        return new JMenu[] { modeMenu };
    }

     /** Used for checking if is in reverse complement axis mode */
    private boolean isReverseComplement()  {
      Boolean isRevComp = (Boolean)browserModel.getModelProperty(BrowserModel.REV_COMP_PROPERTY);
      if (isRevComp == null) throw new IllegalStateException("Model property GAAnnotRevComped not found.");
      return isRevComp.booleanValue();
    }


    /**
     * Check a transcript that is about to have its child exon modified to see if the modification will cause
     * any exon overlaps
     * @param transcript the transcript that is the parent of the exon being modified
     * @param exon the exon being modified
     * @param newExonRange the new range for the exon being modified
     * @return true if no exons overlap, false if there are overlapping exons
     */
    private boolean checkExonOverlapConditionOK(GenomicAxis axis, CuratedTranscript transcript, CuratedExon exon, api.stub.geometry.Range newExonRange) {
      CuratedExon sibExonFeat;
      api.stub.geometry.Range sibRange = null;
      if (DEBUG_CLASS) System.out.println("Check for overlap of exon: " + exon + " Proposed new range: " + newExonRange);

      Collection featureCollection=transcript.getSubFeatures();
      if (featureCollection == null) {
          System.err.println("Error: ConResCurationHandler.checkExonOverlapConditionOK(): transcript " + transcript + " has no sub-features on axis " + axis);
          return true;
      }
      Iterator featureCollectionIterator = featureCollection.iterator();
      while (featureCollectionIterator.hasNext())  {
        if (DEBUG_CLASS) System.out.println("In exon sibling loop.");

        sibExonFeat = (CuratedExon)featureCollectionIterator.next();
        if (sibExonFeat == exon) {
          if (DEBUG_CLASS) System.out.println("This is me: " + sibExonFeat);
          continue;
        }

        // Get alignment for sibling
        sibRange = sibExonFeat.getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis();
        if (sibRange == null) {
            System.err.println("Error: No range found from feature " + sibExonFeat + " on axis " + axis);
            continue;
        }

        if (DEBUG_CLASS) System.out.println("Testing overlap with sibling exon: " + sibExonFeat + " Sibling range: " + sibRange);
        // Check if the modified exon will intersect with this sibling.
        if (newExonRange.intersects(sibRange)) {
          if (DEBUG_CLASS) System.out.println("Found overlap with Sibling exon: " + sibExonFeat + " Sibling range: " + sibRange);
          return false;  //overlap condition not ok
          // delete other exon?
          // but add evidence from other exon?
          // set exonrng to union of exonrng and sibrng
          // OLD Range.union(exonrng, sibrng, exonrng);
        }  // end if (newExonRange.intersects(sibRange))
      }  // while (featureCollectionIterator.hasNext())
      return true; // No exon overlap
    }  // end private boolean checkExonOverlapConditionOK(GenomicAxis axis, CuratedTranscript transcript, CuratedExon exon, api.stub.geometry.Range newExonRange)

    public void actionPerformed(ActionEvent evt) {
        Object src = evt.getSource();
        if (src == selectionModeMI) {
          //      System.out.println("selection mode = " + selectionModeMI.getState());
          selection_enabled = selectionModeMI.getState();
          seqViewer.setSelectable(selection_enabled);
          // make sure to clear previous highlighting
          seqViewer.clearSelection();
        }
        else if (isEditable) {
          if (src == editModeMI) {
            editing_enabled = editModeMI.getState();
            SessionMgr.getSessionMgr().setModelProperty(CURATION_ENABLED,new Boolean(editModeMI.getState()));
            // only listen for mouse events when editing is enabled
            if (editing_enabled) {
                seqViewer.addSequenceMouseListener(mouseHandler);
            }
            else {
                seqViewer.removeSequenceMouseListener(mouseHandler);
            }
          }
          else if (src == forTransFromSelectionMI) {
              handleTranscriptCreation(true);
          }
          else if (src == revTransFromSelectionMI) {
              handleTranscriptCreation(false);
          }
        }
    }  // end public void actionPerformed(ActionEvent evt)


    /**
     * Handle the Transcript Creation...
     */
    protected void handleTranscriptCreation(boolean forward) {
        boolean DEBUG_METHOD = false;
        if (!isEditable) return;
        if (DEBUG_METHOD) System.out.println("ConsResCurHand.handleTranscriptCreation(); forward = " + forward);
        long min = seqViewer.getSelectionBegin();
        // adding +1 to convert from position-based to this browser's space-based coordinates.
        long max = seqViewer.getSelectionEnd() + 1;
        if (DEBUG_METHOD) System.out.println("Selected Range = (" + min + " - " + max + ")");
        // Make sure we have a real range...
        if (min < 0 || max < 0) {
            //System.out.println("must selection a range of bases first!");
            return;
        }
        // Default is forward...
        MutableRange range = new MutableRange((int)min, (int)max);

        // Get the Genomic Axis...
        GenomicEntity masterEditorGE = this.browserModel.getMasterEditorEntity();
        // Make sure it is a GenomicAxis.
        if (!(masterEditorGE instanceof GenomicAxis)) {
          System.out.println("GA- ConsResCurationHandler; trying to get Genomic Axis from BrowserModel... got a " + masterEditorGE);
          return;
        }

        // Mirror the range if the view is reverse complimented...
        GenomicAxis theGenomicAxis = (GenomicAxis)masterEditorGE;
        if (isReverseComplement()) range.mirror(theGenomicAxis.getMagnitude());

        // Reverse the range if it's orientation is not the same as we want...
        // This should be done AFTER any possible "mirror" operation due to reverse complimenting.
        if (forward != range.isForwardOrientation()) {
          if (DEBUG_METHOD) System.out.print("ConsResCurHand.handleTranscriptCreation(); reversing range = " + range);
          range.reverse();
          if (DEBUG_METHOD) System.out.println(" to range = " + range);
        }

        if (DEBUG_METHOD) {
         System.out.println("GA- ConsResCurationHandler; seems to have gotten a Genomic Axis from BrowserModel; " + theGenomicAxis);
         System.out.println("Building a DoMakeCurationFromScratch command using range: " + range);
        }

        DoMakeCurationFromScratch command = new DoMakeCurationFromScratch((GenomicAxis)theGenomicAxis, range);
        ModifyManager.getModifyMgr().doCommand(command);

    }  // end protected void handleTranscriptCreation(boolean forward)

  /**
   * Private inner class for handling model property events.
   */
   private class MySessionModelListener implements SessionModelListener {
    public void browserAdded(BrowserModel browserModel){}
    public void browserRemoved(BrowserModel browserModel){}
    public void sessionWillExit(){}
    public void modelPropertyChanged(Object property,Object oldValue, Object newValue) {
      if (property.equals(CURATION_ENABLED)&&isEditable) {
        editModeMI.setState(((Boolean)newValue).booleanValue());
        editing_enabled = editModeMI.getState();
        // Set state for Make For / Rev Transcript button enable.
        forTransFromSelectionMI.setEnabled(editing_enabled);
        revTransFromSelectionMI.setEnabled(editing_enabled);
        // only listen for mouse events when editing is enabled
        if (editing_enabled) seqViewer.addSequenceMouseListener(mouseHandler);
        else seqViewer.removeSequenceMouseListener(mouseHandler);
      } // End of If statement.
    }
  }

   /**
    * inner class for handling mouse events
    */
   private class MySequenceMouseListener implements SequenceMouseListener {
	 public void mouseMoved(SequenceMouseEvent e){}
	 public void mousePressed(SequenceMouseEvent e){}
	 public void mouseSelectedRange(SequenceMouseEvent e){}
	 public void mouseReleased(SequenceMouseEvent e){
        // If not a curated feature, forget about mouse actions
        if (null==browserModel.getCurrentSelection() || !(browserModel.getCurrentSelection() instanceof CuratedFeature)) {
        	return;
        }
        
        // todo All the edge boundary checking should probably be done in the command object only
        long selStart = Math.min(e.getBeginLocation(),e.getEndLocation());
        long selEnd   = Math.max(e.getBeginLocation(),e.getEndLocation());
		//System.out.println("****************************");
        // Check to see that this selection actually affects an entity
        if(!doesSelectedRangeIntersectAnEntity(selStart) && !doesSelectedRangeIntersectAnEntity(selEnd)){
	   		//System.out.println("Returning immediately.");
	   		return;
		}
		long newEntityBegin = 0;
        long newEntityEnd   = 0;
		
		// case 1:
		// when selectedStart is equal to the adornment begin and
		// and selected End lies towards the right of the  selected start
	    if (selStart==curatedAdornment.getBeginLocation()) {
		    //System.out.println("Case 1");
		    if (!doesSelectedRangeIntersectAnEntity(selStart)) {
	            return;
	        }
		    newEntityBegin = selEnd;
	        newEntityEnd   = curatedAdornment.getEndLocation() + 1;
        }
		// case 2:
		// selected start is equal to the adornment end and the
		// selected end lies beyond the end of the adornment 's
		// end to the right
	        else if (selStart==curatedAdornment.getEndLocation()) {
				//System.out.println("Case 2");
	
		  if (!doesSelectedRangeIntersectAnEntity(selStart)) {
	            return;
	          }
		  newEntityBegin = curatedAdornment.getBeginLocation();
	          newEntityEnd   = selEnd + 1;
	        }
		// case 3:
		// when selected End is equal to the adornment begin and
		// selected start lies outside of the adornment range to the
		// left of the selected End
		else if (selEnd ==curatedAdornment.getBeginLocation() ){
			//System.out.println("Case 3");
		    if(!doesSelectedRangeIntersectAnEntity(selEnd)) return;
		    newEntityBegin = selStart;
		    newEntityEnd = curatedAdornment.getEndLocation() + 1;
		}
	
		// case 4:
		// when selected end is equal to the adornment end and the
		// selected start is to the left of the selected end
		else if (selEnd == curatedAdornment.getEndLocation()){
			//System.out.println("Case 4");
		    if (!doesSelectedRangeIntersectAnEntity(selEnd)) {
	              return;
	            }
		    newEntityBegin = curatedAdornment.getBeginLocation();
		    newEntityEnd = selStart + 1;
		}
	    else return;
	
       if (curatedEntity==null || curatedAdornment==null) return;
       CuratedExon exonFeat = (CuratedExon)curatedEntity;
        // Get the context of the geometry change...
        GenomicAxis genomicAxis = null;
        if (browserModel.getMasterEditorEntity() != null &&
            browserModel.getMasterEditorEntity() instanceof GenomicAxis) {
          genomicAxis = (GenomicAxis)browserModel.getMasterEditorEntity();
        }
        else {
          System.out.println("Error: ConsResCurationHandle Master Editor Entity is not a GenomicAxis.");
        }

        // Find the current range and direction...
        // Expecting 1 and only 1...
        api.stub.geometry.Range oldRange = exonFeat.getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis();

        // checking for exon overlap with sibling exons --
        // if new edges cause overlap of a sibling exon, flag it...
        // Gets range on GenomicAxis...
        CuratedTranscript transcript = (CuratedTranscript)exonFeat.getSuperFeature();
        MutableRange exonRange = new MutableRange((int)newEntityBegin, (int)newEntityEnd);  // Needs to be mutable if we plan to absorb or mirror.
        //System.out.println(" proposed newExonRange=" + exonRange);

        if (isReverseComplement()) {
            // Mirror it with respect to GenomicAxis magnitude
            exonRange.mirror(genomicAxis.getMagnitude());
            //System.out.println("mirrored newExonRange=" + exonRange);
        }

        // Check if we had any overlapping...
        if (!checkExonOverlapConditionOK(genomicAxis, transcript, exonFeat, exonRange)) {
            GenomicEntity ge = browserModel.getCurrentSelection();
            if (ge instanceof Feature) {
              editor.displayFeature((Feature)ge, false);
            }
            JOptionPane.showMessageDialog(browser, "The specified adjustment would cause an exon overlap.",
                "Invalid Adjustment", JOptionPane.WARNING_MESSAGE);
            return;
        }  // End if modified exon will overlap.

        // reset start and end to reflect possible exon merges
        // Don't see anything here athat does the exon merges...


        // This precondition checking can be deleted, and let the
        // DoModifyEntityEdges.validatePrecondition(); do the work.
        if (!transcript.exonRangeChangeWillLeaveNakedStartCodon(exonFeat, exonRange))  {
          if (!exonRange.equals(oldRange)) {
            ModifyManager.getModifyMgr().doCommand(
              new DoModifyEntityEdges(exonFeat, exonRange.getStart(), exonRange.getEnd()));
            /**
             * @todo Probably want to clear the selected region after command object completes
             * or the user releases the mouse.
             */
            //seqViewer.clearSelection();
          }
        }  // end if -- start codon is valid
        else {
            BrowserModel b = browser.getBrowserModel();
            GenomicEntity ge = b.getCurrentSelection();
            // Reselect the last thing selected to refresh the view
            if (ge instanceof Feature) {
              editor.displayFeature((Feature)ge, false);
            }

            JOptionPane.showMessageDialog(browser, "The specified adjustment would cause an invalid start codon.",
                "Invalid Adjustment", JOptionPane.WARNING_MESSAGE);
            return;
        }  // end else -- start codon not valid
      }

      /**
       * Helper method to determine if the mouse drag started at a viable curated
       * feature.  If no, nothing is done.  Tests all adornments in case more than
       * one feature shares the edge; example, stop codon on end of exon.
       */
      private boolean doesSelectedRangeIntersectAnEntity(long testPosition) {
        HashMap entityToGlyphMap = editor.getFeatureToAdornmentMap();
        for (Iterator it = entityToGlyphMap.keySet().iterator();it.hasNext();) {
          GenomicEntity tmpEntity = (GenomicEntity)it.next();
          Adornment tmpAdornment = (Adornment)entityToGlyphMap.get(tmpEntity);
          // The transcript feature in the data model has no glyph.
          if (tmpAdornment!=null) {
            //System.out.println("TestPos = "+testPosition+", Begin = "+tmpAdornment.getBeginLocation()+
            //  ", End = "+tmpAdornment.getEndLocation());
            if (tmpAdornment.getBeginLocation()==testPosition ||
                tmpAdornment.getEndLocation()==testPosition){
                if (tmpEntity instanceof CuratedExon && ((CuratedExon)tmpEntity).isScratch()) {
                  curatedEntity = tmpEntity;
                  curatedAdornment = tmpAdornment;
                  //System.out.println("Adornment not null. Intersection true.\n\n");
                  return true;
                }
                else if (tmpEntity instanceof CuratedExon && !((CuratedExon)tmpEntity).isScratch()){
                  JFrame messageFrame = new JFrame();
                  JOptionPane messagePane = new JOptionPane();
                  messageFrame.getContentPane().add(messagePane);
                  messagePane.showMessageDialog(browser,"Edge modification disallowed for Promoted Features ","Warning",JOptionPane.CANCEL_OPTION);
				  //System.out.println("Adornment not nulll. Intersection false.\n\n");
                  return false;
                }
            }
          }
        }
        //System.out.println("Default false.  Intersection false.\n\n");
        return false;
      }
    }
}
