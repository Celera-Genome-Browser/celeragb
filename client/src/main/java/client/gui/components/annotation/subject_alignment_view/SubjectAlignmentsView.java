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
package client.gui.components.annotation.subject_alignment_view;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

import api.entity_model.access.observer.HitAlignmentDetailFeatureObserver;
import api.entity_model.access.observer.HitAlignmentDetailFeatureObserverAdapter;
import api.entity_model.access.observer.HitAlignmentFeatureObserver;
import api.entity_model.access.report.AlignmentReport;
import api.entity_model.access.report.Report;
import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.annotation.HSPFeature;
import api.entity_model.model.annotation.HitAlignmentDetailFeature;
import api.entity_model.model.annotation.HitAlignmentFeature;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.GenomicEntity;
import api.facade.abstract_facade.annotations.HSPFacade;
import api.facade.abstract_facade.annotations.HitAlignmentFacade;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;
import api.stub.geometry.Range;
import api.stub.sequence.DNASequenceStorage;
import api.stub.sequence.Protein;
import api.stub.sequence.Sequence;
import api.stub.sequence.SequenceHelper;
import client.gui.components.other.report.HTMLReportMenuItem;
import client.gui.components.other.report.HTMLViewable;
import client.gui.framework.browser.Browser;
import client.gui.framework.roles.SubEditor;
import client.gui.framework.session_mgr.*;
import client.gui.framework.view_pref_mgr.ViewPrefMgr;
import client.shared.vizard.*;
import vizard.Glyph;
import vizard.genomics.component.ForwardAndReverseTiersComponent;
import vizard.genomics.glyph.GenomicGlyph;
import vizard.genomics.glyph.TierGlyph;
import vizard.genomics.glyph.TiersColumnGlyph;
import vizard.genomics.model.GenomicAxisViewModel;
import vizard.genomics.model.SequenceAdapter;
import vizard.glyph.AdornmentGlyph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.List;

public class SubjectAlignmentsView extends ForwardAndReverseTiersComponent
implements SubEditor, ActionListener {

   static boolean gapped = false;
   private BrowserModel browserModel;
   private GenomicAxis axis;
   // Number of times to mult the nucleotide range to get mem usage
   private static final int POPULATING_MULTIPLE = 100;
   private static final int DNA_FORWARD_FRAME = 4;
   private static final int DNA_REVERSE_FRAME = -4;
   private static final int LEGEND_WIDTH = 90;
   private static final Color COLOR_MAP_BACKGROUND = Color.black;
   private static final Color COLOR_LEGEND_BACKGROUND = Color.gray;
   private List selectionAdornments = new ArrayList();
   private GenomicEntity lastSelectedEntity;
   private Sequence baseSequence;
   //private MutableRange lastFixedRange = new MutableRange(0,0, Range.ZERO_BASED_INDEXING);
   private Color dnaConsensusColor, proteinConsensusColor, residueColor;
   private String dnaConsensusColorName, proteinConsensusColorName, residueColorName;
   private SelectionVisitor selectionVisitor = new SelectionVisitor();
   private BrowserModelListener browserModelListener = new MyBrowserModelListener();
   private SessionModelListener sessionModelListener = new MySessionModelListener();
   private JMenu showFrameMenu, refAxisMenu;
   private JCheckBoxMenuItem frame1MI, frame2MI, frame3MI, frameNeg1MI, frameNeg2MI, frameNeg3MI;
   private JCheckBoxMenuItem dnaForwardMI, dnaReverseMI, internalSequenceMI, subjectSequenceMI;
   private TierGlyph sequenceTier, forDNAGlyph;

   private JPopupMenu moveToFramePopup = new JPopupMenu("Move to frame");
   private JMenuItem moveToFrame1MI, moveToFrame2MI, moveToFrame3MI, moveToFrameNeg1MI, moveToFrameNeg2MI, moveToFrameNeg3MI;
   private ButtonGroup modeGroup = new ButtonGroup();
   private JMenuItem clearMI, editSettingsMI;
   private JCheckBoxMenuItem zoomToMainViewSelectionMI;
   private HTMLReportMenuItem htmlReportMenuItem;
   private Hashtable entityGlyphCollection = new Hashtable();
   private Hashtable glyphEntityCollection = new Hashtable();
   private AlignmentDNAGlyph sequenceHighlight;
   private SubjectAndQueryComparisonGlyph moveTarget;
   private int numberToLoad = 0;
   private AlignmentDNAGlyph sequenceGlyph;
   private String lastSeqID = "";

   public SubjectAlignmentsView(Browser browser, Boolean isMaster) {
      super(new GenomicAxisViewModel(1000));
      browserModel = browser.getBrowserModel();
      dnaConsensusColorName = (String)SessionMgr.getSessionMgr().getModelProperty("AlignViewDNAConsensusColor");
      proteinConsensusColorName = (String)SessionMgr.getSessionMgr().getModelProperty("AlignViewProteinConsensusColor");
      residueColorName = (String)SessionMgr.getSessionMgr().getModelProperty("AlignViewResidueColor");
      if ( dnaConsensusColorName==null ) dnaConsensusColorName          = "Blue";
      if ( proteinConsensusColorName==null ) proteinConsensusColorName  = "Green";
      if ( residueColorName==null ) residueColorName                    = "White";
      dnaConsensusColor = ViewPrefMgr.getViewPrefMgr().getColor(dnaConsensusColorName);
      proteinConsensusColor = ViewPrefMgr.getViewPrefMgr().getColor(proteinConsensusColorName);
      residueColor = ViewPrefMgr.getViewPrefMgr().getColor(residueColorName);

      forwardTable().tiersComponent().setBackground(COLOR_MAP_BACKGROUND);
      reverseTable().tiersComponent().setBackground(COLOR_MAP_BACKGROUND);
      axisTable().tiersComponent().setBackground(COLOR_MAP_BACKGROUND);

      forwardTable().tierNamesComponent().setBackground(COLOR_LEGEND_BACKGROUND);
      reverseTable().tierNamesComponent().setBackground(COLOR_LEGEND_BACKGROUND);
      axisTable().tierNamesComponent().setBackground(COLOR_LEGEND_BACKGROUND);

      forwardTable().tierNamesComponent().setPreferredSize(new Dimension(LEGEND_WIDTH, 100));
      reverseTable().tierNamesComponent().setPreferredSize(new Dimension(LEGEND_WIDTH, 100));
      axisTable().tierNamesComponent().setPreferredSize(new Dimension(LEGEND_WIDTH, 100));

      new MagnifyingGlassController(this);
      new ResizeTierNamesController(this);
      new SubjectHighlightController(this);
      new SubjectClickSelectionController(this);
      new SubjectNucleotideHighlightController(this);
      setUpTiers();
      setUpMenu();
   }


   private void setUpTiers() {
      sequenceTier=new TierGlyph("Subject Sequence",axisModel(), TierGlyph.COLLAPSED);

      axisColumn().addTier(sequenceTier,-1);
      forDNAGlyph   = addTier("Alignments",  true, 1);
   }


   private TierGlyph addTier(String tierName, boolean isForwardColumnTier, int tierOrderNumber) {
      TiersColumnGlyph targetColumn;
      if ( isForwardColumnTier ) targetColumn = forwardColumn();
      else targetColumn = reverseColumn();

      TierGlyph tierGlyph = new TierGlyph(tierName, axisModel(), TierGlyph.HIDDEN);
      targetColumn.addTier(tierGlyph,tierOrderNumber);
      // Must always expand to show all features within.
      tierGlyph.expandTier();
      return (tierGlyph);
   }


   /**
    * Move the glyph from its current tier to the specified tier
    */
   private void moveToFrame(int frame, SubjectAndQueryComparisonGlyph glyph) {
      TierGlyph currentParent = (TierGlyph)glyph.tierAncestor();
      TierGlyph tier = getTierForFrame(frame);
      if ( currentParent == tier ) return;

      currentParent.removeGenomicChild(glyph);

      glyph.setNewFrame(frame);
      tier.addGenomicChild(glyph);

      if ( tier.getState() != TierGlyph.EXPANDED ) {
         showFrame(frame, true);
      }
   }


   public void glyphPopup(JComponent comp, SubjectAndQueryComparisonGlyph gg, Point windowLocation, int axisLocation) {
      moveTarget = gg;
      moveToFramePopup.show(SubjectAlignmentsView.this, windowLocation.x, windowLocation.y);
   }


   private void setUpMenu() {
      //System.out.println("Setting up components");
      clearMI = new JMenuItem("Clear");
      editSettingsMI = new JMenuItem("Edit SubView Settings...");
      editSettingsMI.addActionListener(this);
      zoomToMainViewSelectionMI = new JCheckBoxMenuItem("Zoom to Annotation View Selection");
      zoomToMainViewSelectionMI.addActionListener(this);
      refAxisMenu = new JMenu("Reference Axis");
      internalSequenceMI = new JCheckBoxMenuItem("Internal");
      internalSequenceMI.addActionListener(this);
      subjectSequenceMI = new JCheckBoxMenuItem("Subject");
      subjectSequenceMI.addActionListener(this);
      modeGroup = new ButtonGroup();
      modeGroup.add(internalSequenceMI);
      modeGroup.add(subjectSequenceMI);
      internalSequenceMI.setSelected(true);
      refAxisMenu.add(internalSequenceMI);
      refAxisMenu.add(subjectSequenceMI);
      htmlReportMenuItem = new HTMLReportMenuItem(new AlignmentReportGenerator());
      showFrameMenu = new JMenu("Show Tier");
      dnaForwardMI = new JCheckBoxMenuItem("DNA forward");
      dnaForwardMI.addActionListener(this);
      dnaReverseMI = new JCheckBoxMenuItem("DNA Reverse");
      dnaReverseMI.addActionListener(this);
      frame1MI = new JCheckBoxMenuItem("+1 Tier");
      frame1MI.addActionListener(this);
      frame2MI = new JCheckBoxMenuItem("+2 Tier");
      frame2MI.addActionListener(this);
      frame3MI = new JCheckBoxMenuItem("+3 Tier");
      frame3MI.addActionListener(this);
      frameNeg1MI = new JCheckBoxMenuItem("-1 Tier");
      frameNeg1MI.addActionListener(this);
      frameNeg2MI = new JCheckBoxMenuItem("-2 Tier");
      frameNeg2MI.addActionListener(this);
      frameNeg3MI = new JCheckBoxMenuItem("-3 Tier");
      frameNeg3MI.addActionListener(this);
      showFrameMenu.add(dnaForwardMI);
      showFrameMenu.add(dnaReverseMI);
      showFrameMenu.add(frame1MI);
      showFrameMenu.add(frame2MI);
      showFrameMenu.add(frame3MI);
      showFrameMenu.add(frameNeg1MI);
      showFrameMenu.add(frameNeg2MI);
      showFrameMenu.add(frameNeg3MI);
      clearMI.addActionListener(this);

      //setup the popup menu
      moveToFrame1MI = new JMenuItem("Move to +1 Frame");
      moveToFrame1MI.addActionListener(this);

      moveToFrame2MI = new JMenuItem("Move to +2 Frame");
      moveToFrame2MI.addActionListener(this);

      moveToFrame3MI = new JMenuItem("Move to +3 Frame");
      moveToFrame3MI.addActionListener(this);

      moveToFrameNeg1MI = new JMenuItem("Move to -1 Frame");
      moveToFrameNeg1MI.addActionListener(this);

      moveToFrameNeg2MI = new JMenuItem("Move to -2 Frame");
      moveToFrameNeg2MI.addActionListener(this);

      moveToFrameNeg3MI = new JMenuItem("Move to -3 Frame");
      moveToFrameNeg3MI.addActionListener(this);

      moveToFramePopup.add(moveToFrame1MI);
      moveToFramePopup.add(moveToFrame2MI);
      moveToFramePopup.add(moveToFrame3MI);
      moveToFramePopup.add(moveToFrameNeg1MI);
      moveToFramePopup.add(moveToFrameNeg2MI);
      moveToFramePopup.add(moveToFrameNeg3MI);

      this.doLayout();
   }


   private JCheckBoxMenuItem getCheckBoxForFrame(int frame) {
      switch ( frame ) {
         case DNA_FORWARD_FRAME: return (dnaForwardMI);
         case DNA_REVERSE_FRAME: return (dnaReverseMI);
         case 1: return (frame1MI);
         case 2: return (frame2MI);
         case 3: return (frame3MI);
         case -1: return (frameNeg1MI);
         case -2: return (frameNeg2MI);
         case -3: return (frameNeg3MI);
      }
      return (null);
   }

   private TierGlyph getTierForFrame(int frame) {
      return (forDNAGlyph);
   }


   public void writeObject(ObjectOutputStream out) {
      System.out.println("Someone is trying to serialize the Subject Alignments View.");
   }

   /**
    * Methods to support the SubEditor interface.
    */
   public void activate() {
      //System.out.println("Activating");
      browserModel.setSubViewVisibleRange(new Range());
      SessionMgr.getSessionMgr().addSessionModelListener(sessionModelListener);
      browserModel.addBrowserModelListener(browserModelListener);
   }

   public void passivate() {
      SessionMgr.getSessionMgr().removeSessionModelListener(sessionModelListener);
      browserModel.removeBrowserModelListener(browserModelListener);
      browserModel.setSubViewVisibleRange(new Range());
      numberToLoad = 0;
   }

   public boolean canEditThisEntity(GenomicEntity entity){
      if ( (entity instanceof HitAlignmentFeature) || (entity instanceof HitAlignmentDetailFeature) ) {
         return (true);
      }
      return (false);
   }

   public void dispose(){
      browserModel.removeBrowserModelListener(browserModelListener);
   }

   public JMenuItem[] getMenus(){
      return (new JMenuItem[] { clearMI, /*showFrameMenu,*/ zoomToMainViewSelectionMI,
                /*editSettingsMI,*/ htmlReportMenuItem});
   }

   public String getName(){ return ("Subject Sequence Alignments");}


   /** ComponentListener implementation -- conquering heavyweight components (JScrollBar) in Swing container issues */
   public void actionPerformed(ActionEvent evt) {
      Object src = evt.getSource();
      if ( src == clearMI ) {
         clearFeatures();
         lastSelectedEntity = null;
         dnaForwardMI.setState(false);
         dnaReverseMI.setState(false);
         frame1MI.setState(false);
         frame2MI.setState(false);
         frame3MI.setState(false);
         frameNeg1MI.setState(false);
         frameNeg2MI.setState(false);
         frameNeg3MI.setState(false);
         showCurrentSelection();
      }
      else if ( src == zoomToMainViewSelectionMI ) {
         showCurrentSelection();
      }
      else if ( src == dnaForwardMI ) {
         showFrame(DNA_FORWARD_FRAME, dnaForwardMI.getState());
      }
      else if ( src == dnaReverseMI ) {
         showFrame(DNA_REVERSE_FRAME, dnaReverseMI.getState());
      }
      else if ( src == frame1MI ) {
         showFrame(1, frame1MI.getState());
      }
      else if ( src == frame2MI ) {
         showFrame(2, frame2MI.getState());
      }
      else if ( src == frame3MI ) {
         showFrame(3, frame3MI.getState());
      }
      else if ( src == frameNeg1MI ) {
         showFrame(-1, frameNeg1MI.getState());
      }
      else if ( src == frameNeg2MI ) {
         showFrame(-2, frameNeg2MI.getState());
      }
      else if ( src == frameNeg3MI ) {
         showFrame(-3, frameNeg3MI.getState());
      }
      else if ( src == moveToFrame1MI ) {
         moveToFrame(1, moveTarget);
         //System.out.println("Move feature to frame 1");
      }
      else if ( src == moveToFrame2MI ) {
         //System.out.println("Move feature to frame 2");
         moveToFrame(2, moveTarget);
      }
      else if ( src == moveToFrame3MI ) {
         //System.out.println("Move feature to frame 3");
         moveToFrame(3, moveTarget);
      }
      else if ( src == moveToFrameNeg1MI ) {
         //System.out.println("Move feature to frame neg 1");
         moveToFrame(-1, moveTarget);
      }
      else if ( src == moveToFrameNeg2MI ) {
         //System.out.println("Move feature to frame neg 2");
         moveToFrame(-2, moveTarget);
      }
      else if ( src == moveToFrameNeg3MI ) {
         //System.out.println("Move feature to frame neg 3");
         moveToFrame(-3, moveTarget);
      }
      else if ( src == editSettingsMI ) {
         /**
          * @todo Give SAV a Pref Panel?
          */
         //          PrefController.getPrefController().getPrefInterface(AlignViewPanel.class,
//            SessionMgr.getSessionMgr().getActiveBrowser());
      }
   }


   private void clearFeatures() {
      entityGlyphCollection.clear();
      glyphEntityCollection.clear();
      for ( int x = 0; x < forwardColumn().tierCount(); x++ ) {
         Collection tmpCollection = forwardColumn().tier(x).genomicChildren();
         for ( Iterator it = tmpCollection.iterator(); it.hasNext(); ) {
            forwardColumn().tier(x).removeGenomicChild((GenomicGlyph)it.next());
         }
      }
      for ( int x = 0; x < reverseColumn().tierCount(); x++ ) {
         Collection tmpCollection = reverseColumn().tier(x).genomicChildren();
         for ( Iterator it = tmpCollection.iterator(); it.hasNext(); ) {
            reverseColumn().tier(x).removeGenomicChild((GenomicGlyph)it.next());
         }
      }
      this.repaint();
   }


   private String getTextForTooltip(Feature entity) {
      if ( entity==null ) {
         System.out.println("No tooltip for null entity.");
         return ("");
      }
      Feature parent;
      GenomicProperty subjLeftProp = null;
      GenomicProperty subjRightProp = null;
      GenomicProperty subjLen = null;
      GenomicProperty subjId = null;
      String tipText= " ";
      parent = entity.getSuperFeature();
      if ( parent != null ) {
         if ( parent instanceof HitAlignmentFeature ) {
            subjLen = parent.getProperty(HitAlignmentFacade.SUBJECT_SEQ_LENGTH_PROP);
            subjId  = parent.getProperty(HitAlignmentFacade.SUBJECT_SEQ_ID_PROP);
         }
      }

      if ( entity instanceof HSPFeature ) {
         subjLeftProp  = entity.getProperty(HSPFacade.SUBJECT_LEFT_PROP);
         subjRightProp = entity.getProperty(HSPFacade.SUBJECT_RIGHT_PROP);
      }

      if ( (subjLeftProp != null) && (subjRightProp != null) ) {
         tipText = "Subject [" + subjLeftProp.getInitialValue() + ", "
                   + subjRightProp.getInitialValue() + "]";
      }
      if ( subjLen != null ) {
         tipText += " of " + subjLen.getInitialValue();
      }
      if ( subjId != null ) {
         tipText += " seq_id=" + subjId.getInitialValue();
      }
      //System.out.println("Tooltip = "+tipText+"\n(for OID="+entity.getOid()+")\n");
      return (tipText);
   }


   /**
    * Make the specified frame show in the view.
    * @param frame is 1,2,3,-1,-2,-3
    * @param showFrame true means show the frame false means hide the frame.
    */
   private void showFrame(int frame, boolean showFrame) {
      TierGlyph tier = getTierForFrame(frame);
      JCheckBoxMenuItem checkbox = getCheckBoxForFrame(frame);
      if ( checkbox.getState() != showFrame ) checkbox.setState(showFrame);
      if ( showFrame ) {
         if ( tier.getState() == TierGlyph.HIDDEN ) tier.setState(TierGlyph.EXPANDED);
      }
      else tier.setState(TierGlyph.HIDDEN);
   }


   protected void setNucleotideSelection(Integer nucleotidePosition) {
      int tmpInt = ((Feature)lastSelectedEntity).
                   transformSplicedPositionToAxisPosition(nucleotidePosition.intValue());
      browserModel.setModelProperty( "ZoomToLocation", new Integer(tmpInt));
   }


   /**
    * Helper method to check if the reverse complement state is set.
    */
   private boolean isAxisRevComp() {
      boolean revcomp = false;
      try {
         Boolean test = (Boolean)browserModel.getModelProperty(BrowserModel.REV_COMP_PROPERTY);
         if ( test == null ) throw new NullPointerException();
         else revcomp = test.booleanValue();
      }
      catch ( Exception ex ) {
         System.err.println("Error!!! A GAAnnotRevComped does not have a value or it is NULL!");
      }
      return (revcomp);
   }


   /**
    * This method assumes that the range passed in, is in the Forward position only.
    * The subeditor always shows a Forward range.
    */
   private boolean setView(HitAlignmentFeature hit) {
      hit.loadSubjectSequenceBackground(new MyHitAlignmentSequenceObserver());
      axisModel().zoom(100);
      this.repaint();
      return (false);
   }


   private class MyHitAlignmentSequenceObserver implements HitAlignmentFeatureObserver {
      public void noteSubjectSequenceLoaded( HitAlignmentFeature entity, Sequence subjSequence ){
         populateView(null, subjSequence);
      }
      public void noteSubjectDefsLoaded( HitAlignmentFeature entity, Collection defs ){}
   }


   private void populateView(Range targetRange, Sequence seq) {
      baseSequence = seq;
      if ( sequenceGlyph!=null ) sequenceTier.removeGenomicChild(sequenceGlyph);
      SequenceAdapter subjectAdapter=new AlignSequenceAdapter(SequenceHelper.toString(baseSequence),
                                                              Color.white, 0, false);
      sequenceGlyph=new AlignmentDNAGlyph(subjectAdapter);
      sequenceTier.addGenomicChild(sequenceGlyph);
   }


   /** Private inner class for handling browser model events */
   private class MyBrowserModelListener extends BrowserModelListenerAdapter {
      public void modelPropertyChanged(Object property,Object oldValue, Object newValue) {}

      public void browserSubViewFixedRangeChanged(Range subViewFixedRange){
//         if ( (subViewFixedRange==null) || (subViewFixedRange.getMagnitude() == 0) ) return;
//         if ( lastFixedRange.equals(subViewFixedRange) ) return;
//         lastFixedRange = subViewFixedRange.toMutableRange();
//         lastSelectedEntity = null;
//         clearFeatures();
//         browserCurrentSelectionChanged(browserModel.getCurrentSelection());
      }


      // Don't care about this.  The Subject-space axis visible range means nothing
      // to the Query-space axes.
      public void browserSubViewVisibleRangeChanged(Range subViewVisibleRange) {}


      public void browserCurrentSelectionChanged(GenomicEntity entity) {
         if ( entity==null || !(entity instanceof Feature) ) return;
         if ( lastSelectedEntity==entity ) {
         	return;
         } 
         else {
         	clearFeatures();
         	lastSelectedEntity = entity;
         } 

         // Make sure the feature desired is worthy.
         Feature tmpEntity = (Feature)entity;
         lastSelectedEntity = entity;
         HitAlignmentFeature tmpHitAlignment;
         if ( entity instanceof HitAlignmentDetailFeature ) {
         	tmpHitAlignment =
            (HitAlignmentFeature)tmpEntity.getSuperFeature();
         }
         else if ( entity instanceof HitAlignmentFeature ) {
         	tmpHitAlignment =
            (HitAlignmentFeature)tmpEntity;
         }
         else return;
         
         GenomicProperty subjId  = tmpHitAlignment.getProperty(HitAlignmentFacade.SUBJECT_SEQ_ID_PROP);
         GenomicProperty subjLength  = tmpHitAlignment.getProperty(HitAlignmentFacade.SUBJECT_SEQ_LENGTH_PROP);
         // Make sure the info exists to be able to proceed.
         if ( !propertyExists(tmpHitAlignment, HitAlignmentFacade.SUBJECT_SEQ_ID_PROP) ||
              !propertyExists(tmpHitAlignment, HitAlignmentFacade.SUBJECT_SEQ_LENGTH_PROP) ) {
            JOptionPane.showMessageDialog(SessionMgr.getSessionMgr().getActiveBrowser(), "The data does not exist to display this feature.",
                                          "Unable to Render Feature", JOptionPane.OK_OPTION);
            clearFeatures();
            return;
         }

         if (null==subjId || null==subjId.getInitialValue()) {
			JOptionPane.showMessageDialog(SessionMgr.getSessionMgr().getActiveBrowser(), "The data does not exist to display this feature.",
										  "Unable to Render Feature", JOptionPane.OK_OPTION);
         	clearFeatures();
         	return;
         }
         String tmpID = subjId.getInitialValue();
         if ( !lastSeqID.equals(tmpID) ) {
            lastSeqID = tmpID;
            SubjectAlignmentsView.this.setAxisRange(0, Double.parseDouble(subjLength.getInitialValue()));
            setView(tmpHitAlignment);
         }

         if ( !glyphEntityCollection.containsValue(entity) )
            tmpEntity.acceptVisitorForSelf(new SelectionVisitor());
         else showCurrentSelection();
      }

      public void browserMasterEditorEntityChanged(GenomicEntity masterEntity) {
         /**
          * @todo make sure the axis sequence glyph is nuked here.
          */
         if ( masterEntity == null ) return;
         if ( !(masterEntity instanceof GenomicAxis) ) return;
         if ( axis != masterEntity ) {
            axis = (GenomicAxis) masterEntity;
            clearFeatures();
         }
      }

      public void browserClosing() { dispose();}
   }


   private boolean propertyExists(GenomicEntity tmpEntity, String propertyName) {
      GenomicProperty tmpProperty = tmpEntity.getProperty(propertyName);
      if ( tmpProperty==null || 
           tmpProperty.getInitialValue()==null ||
           tmpProperty.getInitialValue().equals("") ) return false;
      else return true;
   }



   /** Private inner class for handling selections */
   private class SelectionVisitor extends GenomicEntityVisitor {
      public void visitHitAlignmentFeature(HitAlignmentFeature hitAlignment) {
         HitAlignmentDetailFeature nextSubFeature = null;
         numberToLoad=0;
         SubjectAlignmentsView.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         for ( Iterator it = hitAlignment.getSubFeatures().iterator(); it.hasNext(); ) {
            nextSubFeature = (HitAlignmentDetailFeature)it.next();
            if ( nextSubFeature==null ) return;
            numberToLoad++;
            HitAlignmentDetailFeatureObserver featureObserver = new MyHitAlignmentDetailFeatureObserver(nextSubFeature);
            nextSubFeature.loadQueryAlignedResiduesBackground(featureObserver);
            nextSubFeature.loadSubjectAlignedResiduesBackground(featureObserver);
         }
      }

      public void visitHSPFeature(HSPFeature hsp) {
         numberToLoad++;
         SubjectAlignmentsView.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         HitAlignmentDetailFeatureObserver hspObserver = new MyHitAlignmentDetailFeatureObserver(hsp);
         hsp.loadSubjectAlignedResiduesBackground(hspObserver);
         hsp.loadQueryAlignedResiduesBackground(hspObserver);
      }
   }


   /**
    * Handles for user selection of one HSP and returns GenomicGlyph produced by handling it.
    */
   private GenomicGlyph handleHSPSelection(HSPFeature entity, String subjectResidues, String queryResidues) {
      GenomicGlyph gl = null;
      // Check if already being displayed, then don't create another one.
      GenomicGlyph prev_glyph = getGlyphForEntity(entity);
      if ( prev_glyph != null ) {
         return (prev_glyph);
      }

      EntityType hit_type = entity.getSuperFeature().getEntityType();
      // Some features have AA subject/query strings and nt subject seq.
      // We need to flip the scaling factor accordingly.
      int scalingFactor = 1;
      boolean isProteinEntity = false;
      switch ( hit_type.value() ) {
         // These three are for nt features.  I guess scaling factor is good.
         case EntityTypeConstants.BlastN_Hit: {
               break;
            }
         case EntityTypeConstants.tBlastN:{
               break;
            }
         case EntityTypeConstants.Sim4_Hit:{
               break;
            }
         case EntityTypeConstants.Atalanta_Hit: {
               break;
            }
         case EntityTypeConstants.ESTMapper_Hit: {
               break;
            }
            // These are for aa features.  Scaling factor may vary.
         case EntityTypeConstants.BlastX_Hit:{
               isProteinEntity = true;
               break;
            }
         case EntityTypeConstants.tBlastX: {
               isProteinEntity = true;
               scalingFactor = 3;
               break;
            }
            // In case Dros ever comes back.
            //case EntityTypeConstants.LAP_Hit:
         case EntityTypeConstants.Genewise_Peptide_Hit:{
               isProteinEntity = true;
               // Convert query string to match aa Subject sequence format.
               Sequence tmpSequence = DNASequenceStorage.create(queryResidues, "");
               queryResidues = SequenceHelper.toString(Protein.convertDNASequence(tmpSequence));
               break;
            }
         default: {
               System.out.println("Not Displaying: HSP's composite feature not one of " + "BLASTN_HIT, BLASTX_HIT, SIM4_HIT, or LAP_HIT");
               return (gl);
            }
      }
      if ( !isProteinEntity ) gl = noteNewDNAHSP(entity, subjectResidues, queryResidues);
      else gl = (ProteinComparisonGlyph)noteNewProteinHSP(entity, subjectResidues,
                                                          queryResidues, scalingFactor);
      return (gl);
   }


   private void addGlyphForEntity(SubjectAndQueryComparisonGlyph newGlyph, GenomicEntity newEntity) {
      entityGlyphCollection.put(newEntity, newGlyph);
      glyphEntityCollection.put(newGlyph, newEntity);
   }

   private GenomicGlyph getGlyphForEntity(GenomicEntity entity) {
      return(GenomicGlyph)entityGlyphCollection.get(entity);
   }

   private GenomicEntity getEntityForGlyph(GenomicGlyph glyph) {
      return(GenomicEntity)glyphEntityCollection.get(glyph);
   }

   private GenomicGlyph noteNewProteinHSP(HSPFeature entity, String subjectResidues,
                                          String queryResidues, int scalingFactor) {
      GeometricAlignment alignment = entity.getOnlyGeometricAlignmentToOnlyAxis();
      if ( alignment == null ) {
         System.err.println("Error: either no or multiple alignments found for feature=" + entity + " on axis=" + axis);
         return (null);
      }
      int queryFrame = entity.getQueryFrame(axis, gapped);
      int tmpStart = 0;
      int tmpEnd = 0;
      GenomicProperty tmpProperty = entity.getProperty(HitAlignmentFacade.SUBJECT_LEFT_PROP);
      if ( tmpProperty!=null ) {
         tmpStart = Integer.parseInt(tmpProperty.getInitialValue());
      }
      tmpProperty = entity.getProperty(HitAlignmentFacade.SUBJECT_RIGHT_PROP);
      if ( tmpProperty!=null ) {
         tmpEnd = Integer.parseInt(tmpProperty.getInitialValue());
      }
      int tmpMin = Math.min(tmpStart, tmpEnd);
      int tmpMax = Math.max(tmpStart, tmpEnd);

      SubjectAndQueryComparisonAdapter sqAdapter = new SubjectAndQueryComparisonAdapter(tmpMin, tmpMax);
      SequenceAdapter sa=new AlignSequenceAdapter(subjectResidues, Color.black, tmpMin, true);
      SequenceAdapter qa=new AlignSequenceAdapter(queryResidues, Color.black, tmpMin, true);
      ProteinComparisonGlyph proteinGlyph=new ProteinComparisonGlyph(sqAdapter,
                                                                     subjectResidues, queryResidues, sa, qa, queryFrame, baseSequence,
                                                                     scalingFactor, false);
      addGlyphForEntity(proteinGlyph, entity);

      TierGlyph tier;
      tier = getTierForFrame(proteinGlyph.getFrame());
      tier.addGenomicChild(proteinGlyph);
      if ( tier.getState() == TierGlyph.HIDDEN ) {
         tier.setState(TierGlyph.EXPANDED);
         getCheckBoxForFrame(proteinGlyph.getFrame()).setState(true);
      }
      return (proteinGlyph);
   }


   private GenomicGlyph noteNewDNAHSP(HSPFeature entity, String subjectResidues, String queryResidues) {
      GeometricAlignment alignment = entity.getOnlyGeometricAlignmentToOnlyAxis();
      if ( alignment == null ) {
         System.err.println("Either no or multiple alignments found for feature=" + entity);
         return (null);
      }
      int queryFrame = entity.getQueryFrame(axis, gapped);

      TierGlyph tier;
      tier = forDNAGlyph;
      int tmpStart = 0;
      int tmpEnd = 0;
      GenomicProperty tmpProperty = entity.getProperty(HitAlignmentFacade.SUBJECT_LEFT_PROP);
      if ( tmpProperty!=null ) {
         tmpStart = Integer.parseInt(tmpProperty.getInitialValue());
      }
      tmpProperty = entity.getProperty(HitAlignmentFacade.SUBJECT_RIGHT_PROP);
      if ( tmpProperty!=null ) {
         tmpEnd = Integer.parseInt(tmpProperty.getInitialValue());
      }
      // Check for min and max!!!
      int tmpMin  = Math.min(tmpStart,tmpEnd);
      int tmpMax  = Math.max(tmpStart, tmpEnd);

      SubjectAndQueryComparisonAdapter sqAdapter = new SubjectAndQueryComparisonAdapter(tmpMin, tmpMax);
      AlignSequenceAdapter sa=new AlignSequenceAdapter(subjectResidues, Color.black, tmpMin, false);
      AlignSequenceAdapter qa=new AlignSequenceAdapter(queryResidues, Color.black, tmpMin, false);
      DNAComparisonGlyph subjectGlyph=new DNAComparisonGlyph(sqAdapter, subjectResidues, queryResidues, sa, qa, queryFrame, baseSequence, 1, false);
      addGlyphForEntity(subjectGlyph, entity);

      tier.addGenomicChild(subjectGlyph);
      if ( (tier.childCount() >= 1) && (tier.getState() == TierGlyph.HIDDEN) ) {
         tier.setState(TierGlyph.EXPANDED);
         if ( tier == forDNAGlyph ) {
            dnaForwardMI.setState(true);
         }
         else {
            dnaReverseMI.setState(true);
         }
      }
      return (subjectGlyph);
   }


   public void glyphEntered(GenomicGlyph sqGlyph) {
  	  // Set the tooltip text for the feature
	  if ( sqGlyph instanceof GenomicGlyph && null!=sqGlyph) {
	     ((JComponent)sqGlyph.getRootGlyph().container()).setToolTipText(getTextForTooltip((Feature)glyphEntityCollection.get(sqGlyph)));
	  }

      // Check to see if the base sequence can be highlighted.
      if ( baseSequence != null && baseSequence.length() > 0 ) {
         String shadowResidue = SequenceHelper.toString(baseSequence);
         if (null==shadowResidue || 0>shadowResidue.length()) {
         	return;
         }
         //System.out.println("ShadowResidue = "+shadowResidue.length());
 		 //System.out.println("Start Glyph = "+sqGlyph.start());
		 //System.out.println("End Glyph = "+sqGlyph.end());
         // The axis sequence should ALWAYS be larger than the feature sequence!!!!!  Check for this.
         if (shadowResidue.length() < sqGlyph.start() || shadowResidue.length() < sqGlyph.end()) {
         	System.out.println("ERROR!  The axis sequence (subj seq) is smaller than the sequence of the feature!!!  Not displaying red overlay glyph.");
         	return;
         }
         shadowResidue = shadowResidue.substring(sqGlyph.start(), sqGlyph.end());
         SequenceAdapter queryAdapter=new AlignSequenceAdapter(shadowResidue, Color.red, sqGlyph.start(), false);
         sequenceHighlight=new AlignmentDNAGlyph(queryAdapter);
         sequenceTier.addGenomicChild(sequenceHighlight);
      }

      this.repaint();
   }

   public void glyphExited(GenomicGlyph sqGlyph) {
      // If the base sequence has been highlighted, clear the glyph.
      if ( baseSequence != null && baseSequence.length() > 0 ) {
         if ( sequenceHighlight != null ) sequenceHighlight.delete();
      }

      // Clear out the feature's tooltip text.
      if ( sqGlyph != null && sqGlyph instanceof GenomicGlyph )
         ((JComponent)sqGlyph.getRootGlyph().container()).setToolTipText("");
      this.repaint();
   }


   public void selectGlyph(SubjectAndQueryComparisonGlyph picked, boolean withShift, boolean withControl) {
      if ( getEntityForGlyph(picked)==null || !(getEntityForGlyph(picked) instanceof Feature) ) return;
      Feature newSelection = null;
      if ( picked != null ) {
         GeometricAlignment align = (GeometricAlignment)((Feature)getEntityForGlyph(picked)).getOnlyAlignmentToAnAxis(axis);
         newSelection = (align == null) ? null : (Feature)align.getEntity();

         if ( newSelection instanceof Feature ) {
            Feature feature = (Feature)newSelection;
            if ( withControl ) {
               while ( feature.getSuperFeature() != null ) {
                  feature = feature.getSuperFeature();
               }
            }
            else if ( withShift ) {
               if ( !feature.hasSubFeatures() && feature.getSuperFeature() != null )
                  feature = feature.getSuperFeature();
            }

            newSelection = feature;
         }
      }
      if ( newSelection != browserModel.getCurrentSelection() ) {
         lastSelectedEntity = newSelection;
         browserModel.setCurrentSelection(newSelection);
      }
   }

   private void showCurrentSelection() {
      if ( lastSelectedEntity==null || !(lastSelectedEntity instanceof Feature) ) return;
      Feature entity = (Feature)lastSelectedEntity;
      double featureStart = 0;
      if ( propertyExists(entity, HitAlignmentFacade.SUBJECT_LEFT_PROP) &&
           propertyExists(entity, HitAlignmentFacade.SUBJECT_RIGHT_PROP) )
         featureStart = Math.min(Double.parseDouble(lastSelectedEntity.getProperty(HitAlignmentFacade.SUBJECT_LEFT_PROP).getInitialValue()),
                                 Double.parseDouble(lastSelectedEntity.getProperty(HitAlignmentFacade.SUBJECT_RIGHT_PROP).getInitialValue()));
      else {
         // Assumes that the HitAlignment can't exist without children.
         try {
            Feature tmpEntity = (Feature)entity.getSubFeatures().iterator().next();
            if ( propertyExists(tmpEntity, HitAlignmentFacade.SUBJECT_LEFT_PROP) &&
                 propertyExists(tmpEntity, HitAlignmentFacade.SUBJECT_RIGHT_PROP) )
               featureStart = Math.min(Double.parseDouble(tmpEntity.getProperty(HitAlignmentFacade.SUBJECT_LEFT_PROP).getInitialValue()),
                                       Double.parseDouble(tmpEntity.getProperty(HitAlignmentFacade.SUBJECT_RIGHT_PROP).getInitialValue()));
         }
         catch ( Exception ex ) {
            SessionMgr.getSessionMgr().handleException(ex);
         }
      }
      double viewStart = featureStart-axisModel().viewSize()*0.1;
      axisModel().setOrigin(viewStart);
      this.setZoomLocation(featureStart);
      if ( zoomToMainViewSelectionMI.isSelected() ) {
         axisModel().zoom(0.0);
      }

      // Clear out the old selection adornments (boxes).
      for ( Iterator it = selectionAdornments.iterator(); it.hasNext(); ) {
         ((Glyph)it.next()).delete();
      }
      selectionAdornments.clear();

      List adornmentList = new ArrayList();
      // Find all of the glyphs that exist for this feature.
      if ( getGlyphForEntity(entity)!=null ) adornmentList.add(getGlyphForEntity(entity));
      for ( Iterator it = entity.getSubFeatures().iterator(); it.hasNext(); ) {
         Feature tmpEntity = (Feature)it.next();
         if ( getGlyphForEntity(tmpEntity)!=null ) adornmentList.add(getGlyphForEntity(tmpEntity));
      }

      // Cycle through the glyphs and add the adornments.
      for ( int i = 0; i < adornmentList.size(); i++ ) {
         Glyph adornment = new AdornmentGlyph((SubjectAndQueryComparisonGlyph)adornmentList.get(i),
                                              Color.red, 2);
         ((SubjectAndQueryComparisonGlyph)adornmentList.get(i)).addChild(adornment);
         selectionAdornments.add(adornment);
      }
      this.repaint();
   }


   /**
    * Private inner class for handling model property events.
    */
   private class MySessionModelListener implements SessionModelListener {
      public void browserAdded(BrowserModel browserModel){}
      public void browserRemoved(BrowserModel browserModel){}
      public void sessionWillExit(){}
      public void modelPropertyChanged(Object property,Object oldValue, Object newValue) {
         if ( property.equals("AlignViewDNAConsensusColor") ||
              property.equals("AlignViewProteinConsensusColor") ||
              property.equals("AlignViewResidueColor") ) {
            dnaConsensusColorName = (String)SessionMgr.getSessionMgr().getModelProperty("AlignViewDNAConsensusColor");
            proteinConsensusColorName = (String)SessionMgr.getSessionMgr().getModelProperty("AlignViewProteinConsensusColor");
            residueColorName = (String)SessionMgr.getSessionMgr().getModelProperty("AlignViewResidueColor");
            if ( dnaConsensusColorName==null )     dnaConsensusColorName      = "Blue";
            if ( proteinConsensusColorName==null ) proteinConsensusColorName  = "Green";
            if ( residueColorName==null )          residueColorName           = "White";
            dnaConsensusColor = ViewPrefMgr.getViewPrefMgr().getColor(dnaConsensusColorName);
            proteinConsensusColor = ViewPrefMgr.getViewPrefMgr().getColor(proteinConsensusColorName);
            residueColor = ViewPrefMgr.getViewPrefMgr().getColor(residueColorName);
            /**
             * @todo Need to reintegrate the user-customizable color control.
             */
            //          resetConsensusColors();
         }
      }
   }


   /**
    * Private inner class for the non-blocking load of subject and query residues.
    */
   private class MyHitAlignmentDetailFeatureObserver extends HitAlignmentDetailFeatureObserverAdapter
   implements HitAlignmentDetailFeatureObserver {
      private String queryAlignedResidues = null;
      private String subjectAlignedResidues = null;
      private boolean queryHasBeenLoaded = false;
      private boolean subjectHasBeenLoaded = false;
      private OID targetOid = null;
      private boolean isTopStrandFeature = true;

      /** Setup count-down variable for loading. */
      public MyHitAlignmentDetailFeatureObserver(HitAlignmentDetailFeature targetFeature) {
         isTopStrandFeature = targetFeature.getOnlyAlignmentToOnlyAxis().orientationForwardOnAxis();
         targetOid = targetFeature.getOid();
      }

      /** Called when query aligned residues are available. */
      public void noteQueryAlignedResiduesLoaded(HitAlignmentDetailFeature feature,String queryAlignedResidues) {
         // If the query is for this feature then proceed.
         if ( targetOid != null && feature.getOid().equals(targetOid) ) {
            this.queryAlignedResidues = queryAlignedResidues;
            queryHasBeenLoaded = true;
            handleFeatureIfBothSequencesHaveArrived((HSPFeature)feature);
         }
      }

      /** Called when subject aligned residues are available. */
      public void noteSubjectAlignedResiduesLoaded(HitAlignmentDetailFeature feature,String subjectAlignedResidues){
         // If the subject is for this feature then proceed.
         if ( targetOid != null && feature.getOid().equals(targetOid) ) {
            this.subjectAlignedResidues = subjectAlignedResidues;
            subjectHasBeenLoaded = true;
            handleFeatureIfBothSequencesHaveArrived((HSPFeature)feature);
         }
      }

      /**
       * Called when either type of residues are available. Once both arrive
       * we may now proceed with the creation of glyphs.
       */
      private void handleFeatureIfBothSequencesHaveArrived(HSPFeature feature) {
         if ( ! (queryHasBeenLoaded && subjectHasBeenLoaded) ) return;
         //System.out.println("Feature "+feature.getOid().toString());
         //System.out.println("Subj "+subjectAlignedResidues);
         //System.out.println("Query "+queryAlignedResidues);

         numberToLoad--;
         handleSequenceOrientation();
         handleHSPSelection(feature, subjectAlignedResidues, queryAlignedResidues);
         // Try to determine if all features have arrived.
         if ( numberToLoad <= 0 ) {
            numberToLoad=0;
            SubjectAlignmentsView.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            showCurrentSelection();
         }
      }

      /**
       * It would appear that the database formats the subject and query sequence
       * based on the feature orientation on the strand.  Need to compensate
       * when applying to the Subject Sequence "axis".
       * I really hope the Subject Sequence ID does not have an orientation flag!
       */
      private void handleSequenceOrientation() {
         // Reverse the sequence order if on the bottom strand.
         if ( !isTopStrandFeature ) {
            StringBuffer subjBuffer = new StringBuffer(subjectAlignedResidues);
            subjectAlignedResidues = subjBuffer.reverse().toString();
            StringBuffer queryBuffer = new StringBuffer(queryAlignedResidues);
            queryAlignedResidues = queryBuffer.reverse().toString();
         }
      }
   }


   /** Builds a report given knowledge of sequences in the outer class. */
   private class AlignmentReportGenerator implements HTMLViewable {
      public String getCurrentReportName() {
         return (getName()+" Report");
      }

      /**
       * Builds HTML report based on current HSP collection.
       */
      public Report getCurrentReport() {
         AlignmentReport returnReport = new AlignmentReport();
         Collection entities = Arrays.asList(entityGlyphCollection.keySet().toArray());
         returnReport.createLineItemsFrom(entities);
         return (returnReport);
      }
   }
}