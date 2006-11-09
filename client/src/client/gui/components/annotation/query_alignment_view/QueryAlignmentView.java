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
package client.gui.components.annotation.query_alignment_view;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import api.entity_model.access.observer.HitAlignmentDetailFeatureObserver;
import api.entity_model.access.observer.HitAlignmentDetailFeatureObserverAdapter;
import api.entity_model.access.observer.SequenceObserver;
import api.entity_model.access.report.AlignmentReport;
import api.entity_model.access.report.Report;
import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.annotation.HSPFeature;
import api.entity_model.model.annotation.HitAlignmentDetailFeature;
import api.entity_model.model.annotation.HitAlignmentFeature;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.AlignableGenomicEntity;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.GenomicEntity;
import api.facade.abstract_facade.annotations.HSPFacade;
import api.facade.abstract_facade.annotations.HitAlignmentFacade;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;
import api.stub.data.SequenceAnalysisQueryParameters;
import api.stub.geometry.MutableRange;
import api.stub.geometry.Range;
import api.stub.sequence.DNASequenceStorage;
import api.stub.sequence.Protein;
import api.stub.sequence.Sequence;
import api.stub.sequence.SequenceHelper;
import api.stub.sequence.SubSequence;
import client.gui.components.other.report.HTMLReportMenuItem;
import client.gui.components.other.report.HTMLViewable;
import client.gui.framework.browser.Browser;
import client.gui.framework.navigation_tools.SequenceAnalysisDialog;
import client.gui.framework.pref_controller.PrefController;
import client.gui.framework.roles.SubEditor;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.BrowserModelListener;
import client.gui.framework.session_mgr.BrowserModelListenerAdapter;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.framework.session_mgr.SessionModelListener;
import client.gui.framework.view_pref_mgr.ViewPrefMgr;
import client.gui.other.panels.AlignViewPanel;
import client.gui.other.util.ClipboardUtils;
import client.shared.vizard.AlignSequenceAdapter;
import client.shared.vizard.AlignmentDNAGlyph;
import client.shared.vizard.DNAComparisonGlyph;
import client.shared.vizard.MagnifyingGlassController;
import client.shared.vizard.ProteinComparisonGlyph;
import client.shared.vizard.ResizeTierNamesController;
import client.shared.vizard.SubjectAndQueryComparisonAdapter;
import client.shared.vizard.SubjectAndQueryComparisonGlyph;
import vizard.Glyph;
import vizard.genomics.component.ForwardAndReverseTiersComponent;
import vizard.genomics.glyph.GenomicGlyph;
import vizard.genomics.glyph.TierGlyph;
import vizard.genomics.glyph.TiersColumnGlyph;
import vizard.genomics.model.GenomicAxisViewModel;
import vizard.genomics.model.SequenceAdapter;
import vizard.glyph.AdornmentGlyph;
import vizard.model.WorldViewModel;

public class QueryAlignmentView extends ForwardAndReverseTiersComponent
implements SubEditor, ActionListener {

   static boolean gapped = false;
   private Browser browser;
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
   private MutableRange lastFixedRange = new MutableRange(0,0, Range.ZERO_BASED_INDEXING);
   private MutableRange lastVisibleRange = new MutableRange(0,0, Range.ZERO_BASED_INDEXING);
   private boolean knownRevCompState = false;
   private boolean subViewRangePopulated = false;
   private Sequence baseSequence;
   private SelectionVisitor selectionVisitor = new SelectionVisitor();
   private Color dnaConsensusColor, proteinConsensusColor, residueColor;
   private String dnaConsensusColorName, proteinConsensusColorName, residueColorName;
   private BrowserModelListener browserModelListener = new MyBrowserModelListener();
   private SessionModelListener sessionModelListener = new MySessionModelListener();
   private JMenu showFrameMenu, refAxisMenu;
   private JCheckBoxMenuItem frame1MI, frame2MI, frame3MI, frameNeg1MI, frameNeg2MI, frameNeg3MI;
   private JCheckBoxMenuItem dnaForwardMI, dnaReverseMI, internalSequenceMI, subjectSequenceMI;
   private TierGlyph sequenceTier, forDNAGlyph, revDNAGlyph, forProtGlyph1, revProtGlyph1,
   forProtGlyph2, revProtGlyph2, forProtGlyph3, revProtGlyph3;

   private JPopupMenu moveToFramePopup = new JPopupMenu("Move to frame");
   private JPopupMenu popupMenu = new JPopupMenu();
   private JMenuItem moveToFrame1MI, moveToFrame2MI, moveToFrame3MI, moveToFrameNeg1MI, moveToFrameNeg2MI, moveToFrameNeg3MI;
   private JMenuItem copyConsensusToClipboard, copyConsensusToSeqAnalysis;
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

   private WorldViewModel.Observer visibleRangeObserver = new WorldViewModel.Observer() {
       public void zoomCenterChanged(WorldViewModel model) {}
       public void modelChanged(WorldViewModel model) {
           double visibleRangeStart  = axisModel().origin();
           double visibleRangeEnd    = axisModel().viewEnd();
           Range newRange = new Range((int)visibleRangeStart, (int)visibleRangeEnd);
           lastVisibleRange = newRange.toMutableRange();
           knownRevCompState = isReverseComplement();
           if (isReverseComplement()) {
               MutableRange mr = newRange.toMutableRange();
               mr.mirror(axis.getMagnitude());
               newRange = mr.toRange();
           }
           browserModel.setSubViewVisibleRange(newRange);
       }
   };

   public QueryAlignmentView(Browser browser, Boolean isMaster) {
      super(new GenomicAxisViewModel(1000));
      this.browser = browser;
      browserModel = browser.getBrowserModel();
      axis=(GenomicAxis)browserModel.getMasterEditorEntity();
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

      new AlignGlyphPopupMenuController(this);
      new ResizeTierNamesController(this);
      new AlignHighlightController(this);
      new AlignClickSelectionController(this);
      new MagnifyingGlassController(this);
      new AlignNucleotideHighlightController(this);
      setUpTiers();
      setUpMenu();
   }


   private void setUpTiers() {
      sequenceTier=new TierGlyph("Query Sequence",axisModel(), TierGlyph.COLLAPSED);

      axisColumn().addTier(sequenceTier,-1);
      forDNAGlyph   = addTier("DNA +",  true, 3);
      forProtGlyph1 = addTier("+1",     true, 2);
      forProtGlyph2 = addTier("+2",     true, 1);
      forProtGlyph3 = addTier("+3",     true, 0);

      revDNAGlyph   = addTier("DNA -",  false, 0);
      revProtGlyph1 = addTier("-1",     false, 1);
      revProtGlyph2 = addTier("-2",     false, 2);
      revProtGlyph3 = addTier("-3",     false, 3);
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


   public void glyphPopup(JComponent comp, SubjectAndQueryComparisonGlyph gg, Point windowLocation) {
      moveTarget = gg;
      popupMenu.show(comp, windowLocation.x, windowLocation.y);
      //moveToFramePopup.show(QueryAlignmentView.this, windowLocation.x, windowLocation.y);
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
      copyConsensusToClipboard = new JMenuItem("Copy Feature Consensus Sequence To Clipboard");
      copyConsensusToClipboard.addActionListener(new ActionListener() {
                                                    public void actionPerformed(ActionEvent evt) {
                                                       // Get the base seq that is under the glyph.
                                                       String shadowResidue = SequenceHelper.toString(baseSequence);
                                                       shadowResidue = shadowResidue.substring(moveTarget.start()-lastFixedRange.getStart(),
                                                                                               moveTarget.end()-lastFixedRange.getStart());
                                                       ClipboardUtils.setClipboardContents(shadowResidue);
                                                    }
                                                 });

      copyConsensusToSeqAnalysis = new JMenuItem("Copy Feature Consensus Sequence To Sequence Analysis");
      copyConsensusToSeqAnalysis.addActionListener(new ActionListener() {
                                                      public void actionPerformed(ActionEvent evt) {
                                                         // Get the base seq that is under the glyph.
                                                         Range tmpFeatureRange = new Range(moveTarget.start()-lastFixedRange.getStart(),
                                                                                           moveTarget.end()-lastFixedRange.getStart());
                                                         String shadowResidue = SequenceHelper.toString(baseSequence);
                                                         shadowResidue = shadowResidue.substring(moveTarget.start()-lastFixedRange.getStart(),
                                                                                                 moveTarget.end()-lastFixedRange.getStart());
											             Sequence querySequence = baseSequence;

                                                         SequenceAnalysisQueryParameters ibp =
                                                         new SequenceAnalysisQueryParameters(tmpFeatureRange, axis, DNASequenceStorage.create(shadowResidue, ""), DNASequenceStorage.create(querySequence));
                                                         browserModel.setModelProperty(SequenceAnalysisQueryParameters.PARAMETERS_PROPERTY_KEY,ibp);
                                                         SequenceAnalysisDialog.getSequenceAnalysisDialog().showSearchDialog();
                                                      }
                                                   });

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

      popupMenu.add(copyConsensusToClipboard);
      popupMenu.add(copyConsensusToSeqAnalysis);
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
      switch ( frame ) {
         case DNA_FORWARD_FRAME: return (forDNAGlyph);
         case DNA_REVERSE_FRAME: return (revDNAGlyph);
         case 1: return (forProtGlyph1);
         case 2: return (forProtGlyph2);
         case 3: return (forProtGlyph3);
         case -1: return (revProtGlyph1);
         case -2: return (revProtGlyph2);
         case -3: return (revProtGlyph3);
      }
      return (null);
   }


   public void writeObject(ObjectOutputStream out) {
      System.out.println("Someone is trying to serialize the Query Alignments View.");
   }


   /**
    * Methods to support the SubEditor interface.
    */
   public void activate() {
    //System.out.println("Activating");
      /**
       * @todo Check to see if the disposing flag is a smart thing to do.
       */
    this.axisModel().observers.addObserver(visibleRangeObserver);
       SessionMgr.getSessionMgr().addSessionModelListener(sessionModelListener);
       browserModel.addBrowserModelListener(browserModelListener);
       if (knownRevCompState!=isReverseComplement()) {
           lastVisibleRange.mirror(((GenomicAxis)browserModel.getMasterEditorEntity()).getMagnitude());
           knownRevCompState=isReverseComplement();
       }
       browserModel.setSubViewVisibleRange(lastVisibleRange);
   }

   public void passivate() {
       this.axisModel().observers.removeObserver(visibleRangeObserver);
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
      this.axisModel().observers.removeObserver(visibleRangeObserver);
      browserModel.removeBrowserModelListener(browserModelListener);
   }

   public JMenuItem[] getMenus(){
      return (new JMenuItem[] { clearMI, showFrameMenu, zoomToMainViewSelectionMI,
                /*editSettingsMI,*/ htmlReportMenuItem});
   }

   public String getName(){ return ("Genomic Axis Alignments");}


   /** ComponentListener implementation -- conquering heavyweight components (JScrollBar) in Swing container issues */
   public void actionPerformed(ActionEvent evt) {
      Object src = evt.getSource();
      if ( src == clearMI ) {
         clearFeatures();
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
         PrefController.getPrefController().getPrefInterface(AlignViewPanel.class,
                                                             SessionMgr.getSessionMgr().getActiveBrowser());
      }
   }


   private void clearView() {
      clearFeatures();
      if ( lastSelectedEntity!=null ) setView(lastVisibleRange);
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


   /**
    * Helper method to check if the reverse complement state is set.
    */
   private boolean isReverseComplement() {
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
   private void setView(Range range) {
      if ( axis == null || range == null ) return;
      if ( lastVisibleRange.equals(range) ) return;
      else lastVisibleRange = range.toMutableRange();
      MutableRange tmpYellowBoxRange = range.toMutableRange();
      if ( isReverseComplement() ) tmpYellowBoxRange.mirror(axis.getMagnitude());
      lastFixedRange = tmpYellowBoxRange;

      browserModel.setSubViewVisibleRange(tmpYellowBoxRange);
      subViewRangePopulated = false;
      axis.loadNucleotideSeq(lastVisibleRange, new MySequenceObserver());
      axisModel().zoom(100);
   }


   private class MySequenceObserver implements SequenceObserver {
      public void noteSequenceArrived(Axis axis, Range rangeOfSequence, Sequence sequence) {
         populateView(rangeOfSequence, sequence);
      }

   }


   private void populateView(Range targetRange, Sequence seq) {
      if ( (targetRange == null) || (seq == null) ) return;
      clearView();
      baseSequence = seq;
      if ( sequenceGlyph!=null ) sequenceTier.removeGenomicChild(sequenceGlyph);
      SequenceAdapter queryAdapter=new AlignSequenceAdapter(SequenceHelper.toString(baseSequence),
                                                            Color.white, lastFixedRange.getStart(), false);
      sequenceGlyph=new AlignmentDNAGlyph(queryAdapter);
      sequenceTier.addGenomicChild(sequenceGlyph);


      subViewRangePopulated = true;
      GenomicEntity firstEntity = browserModel.getCurrentSelection();
      if ( firstEntity!=null && firstEntity instanceof AlignableGenomicEntity ) {
         firstEntity.acceptVisitorForSelf(selectionVisitor);
         showCurrentSelection();
      }
   }


   /** Private inner class for handling browser model events */
   private class MyBrowserModelListener extends BrowserModelListenerAdapter {
      public void modelPropertyChanged(Object property,Object oldValue, Object newValue)  {
        if (property.equals(BrowserModel.REV_COMP_PROPERTY)) {
          lastFixedRange.mirror(axis.getMagnitude());
          lastVisibleRange.mirror(axis.getMagnitude());
          knownRevCompState = isReverseComplement();
          browserModel.setSubViewVisibleRange(lastVisibleRange);
        }
      }

      public void browserSubViewFixedRangeChanged(Range subViewFixedRange){
         if ( (subViewFixedRange==null) || (subViewFixedRange.getMagnitude() == 0) ) return;
         if ( lastFixedRange.equals(subViewFixedRange) ) return;
         browserModel.setSubViewVisibleRange(subViewFixedRange);
         if ( isReverseComplement() ) {
            MutableRange tmpRange = subViewFixedRange.toMutableRange();
            tmpRange.mirror(axis.getMagnitude());
            lastFixedRange = tmpRange;
         }
         else lastFixedRange = subViewFixedRange.toMutableRange();
         QueryAlignmentView.this.setAxisRange(lastFixedRange.getMinimum(),
                                              lastFixedRange.getMagnitude());
         clearView();
         setView(lastFixedRange);
      }

      public void browserCurrentSelectionChanged(GenomicEntity entity) {
         if ( entity==null || !(entity instanceof Feature) ) return;
         /**
          * Check that the root fits within the fixed range.  This not absolutely
          * necessary for a view that lives in Subject-space but it is consistent
          * with the operation of all other subviews.
          */
         MutableRange tmpRange = ((GeometricAlignment)
                                  ((Feature)entity).getRootFeature().getOnlyAlignmentToAnAxis(axis)).getRangeOnAxis().toMutableRange();
         if ( isReverseComplement() ) tmpRange.mirror(axis.getMagnitude());
         if ( lastFixedRange==null || !lastFixedRange.contains(tmpRange) ) return;

         if ( lastSelectedEntity!=null && lastSelectedEntity.equals(entity) ) return;
         else lastSelectedEntity = entity;
         if ( !glyphEntityCollection.containsValue(entity) )
            entity.acceptVisitorForSelf(selectionVisitor);
         showCurrentSelection();
      }


      public void browserMasterEditorEntityChanged(GenomicEntity masterEntity) {
         if ( masterEntity == null ) return;
         if ( !(masterEntity instanceof GenomicAxis) ) return;
         if ( axis != masterEntity ) {
            axis = (GenomicAxis) masterEntity;
            clearView();
         }
      }

      public void browserSubViewVisibleRangeChanged(Range subViewVisibleRange) {
         if ( axis == null ) return;
         if ( lastVisibleRange.equals(subViewVisibleRange) || subViewVisibleRange.getMagnitude()==0 ) return;
         MutableRange tmpRange = subViewVisibleRange.toMutableRange();
         if ( isReverseComplement() ) tmpRange.mirror(axis.getMagnitude());
         lastVisibleRange = tmpRange;
         axisModel().setViewMinMax((double)tmpRange.getMinimum(), (double)tmpRange.getMaximum());
      }

      public void browserClosing() { dispose();}
   }


   /** Private inner class for handling selections */
   private class SelectionVisitor extends GenomicEntityVisitor {
      public void visitHitAlignmentFeature(HitAlignmentFeature hitAlignment) {
         HitAlignmentDetailFeature nextSubFeature = null;
         numberToLoad = 0;
         for ( Iterator it = hitAlignment.getSubFeatures().iterator(); it.hasNext(); ) {
            nextSubFeature = (HitAlignmentDetailFeature)it.next();
            if ( nextSubFeature==null ) return;
            numberToLoad++;
            QueryAlignmentView.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            HitAlignmentDetailFeatureObserver featureObserver = new MyHitAlignmentDetailFeatureObserver(nextSubFeature);
            nextSubFeature.loadQueryAlignedResiduesBackground(featureObserver);
            nextSubFeature.loadSubjectAlignedResiduesBackground(featureObserver);
         }
      }

      public void visitHSPFeature(HSPFeature hsp) {
         numberToLoad++;
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
         case EntityTypeConstants.tBlastN: {
               break;
            }
         case EntityTypeConstants.Sim4_Hit: {
               break;
            }
         case EntityTypeConstants.Atalanta_Hit: {
               break;
            }
        
         case EntityTypeConstants.ESTMapper_Hit: {
               break;
            }
            // These are for aa features.  Scaling factor may vary.
         case EntityTypeConstants.BlastX_Hit: {
               isProteinEntity = true;
               scalingFactor = 3;
               break;
            }
         case EntityTypeConstants.tBlastX: {
               isProteinEntity = true;
               scalingFactor = 3;
               break;
            }
         case EntityTypeConstants.Genewise_Peptide_Hit:{
               isProteinEntity = true;
               scalingFactor = 3;
               // Convert query string to match aa Subject sequence format.
               Sequence tmpSequence = DNASequenceStorage.create(queryResidues, "");
               queryResidues = SequenceHelper.toString(Protein.convertDNASequence(tmpSequence));
               break;
            }
         default: {
               System.out.println("Not Displaying: HSP's composite feature not one of " + "BLASTN_HIT, BLASTX_HIT, SIM4_HIT, ATALANTA_HIT, or ESTMAPPER_HIT");
               return (gl);
            }
      }
      if ( !isProteinEntity ) gl = noteNewDNAHSP(entity, subjectResidues, queryResidues, scalingFactor);
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

   private GenomicGlyph noteNewProteinHSP(HSPFeature entity, String subjectResidues, String queryResidues, int scalingFactor) {
      GeometricAlignment alignment = entity.getOnlyGeometricAlignmentToOnlyAxis();
      if ( alignment == null ) {
         System.err.println("Error: either no or multiple alignments found for feature=" + entity + " on axis=" + axis);
         return (null);
      }
      MutableRange range = new MutableRange(alignment.getRangeOnAxis());
      int queryFrame = entity.getQueryFrame(axis, gapped);

      SubjectAndQueryComparisonAdapter sqAdapter = new SubjectAndQueryComparisonAdapter(range.getMinimum(), range.getMaximum());
      SequenceAdapter sa=new AlignSequenceAdapter(subjectResidues, Color.black, alignment.getRangeOnAxis().getMinimum(), true);
      SequenceAdapter qa=new AlignSequenceAdapter(queryResidues, Color.black, alignment.getRangeOnAxis().getMinimum(), true);
      ProteinComparisonGlyph proteinGlyph=new ProteinComparisonGlyph(sqAdapter,
                                                                     subjectResidues, queryResidues, sa, qa, queryFrame, baseSequence,
                                                                     scalingFactor, true);
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


   private GenomicGlyph noteNewDNAHSP(HSPFeature entity, String subjectResidues, String queryResidues, int scalingFactor) {
      GeometricAlignment alignment = entity.getOnlyGeometricAlignmentToOnlyAxis();
      if ( alignment == null ) {
         System.err.println("Either no or multiple alignments found for feature=" + entity);
         return (null);
      }
      MutableRange range = new MutableRange(alignment.getRangeOnAxis());
      int queryFrame = entity.getQueryFrame(axis, gapped);

      TierGlyph tier;
      if ( queryFrame > 0 ) {
         tier = forDNAGlyph;
      }
      else {
         tier = revDNAGlyph;
      }
      // Don't forget to add the Sequence Glyphs
      /**
       * @todo This does not take in to account REVERSE COMPLEMENTING!!!!!!!
       * Just trying to give the right geometry to the sequence glyph.
       */
      SubjectAndQueryComparisonAdapter sqAdapter = new SubjectAndQueryComparisonAdapter(range.getMinimum(), range.getMaximum());
      AlignSequenceAdapter sa=new AlignSequenceAdapter(subjectResidues, Color.black, alignment.getRangeOnAxis().getMinimum(), false);
      AlignSequenceAdapter qa=new AlignSequenceAdapter(queryResidues, Color.black, alignment.getRangeOnAxis().getMinimum(), false);
      DNAComparisonGlyph subjectGlyph=new DNAComparisonGlyph(sqAdapter, subjectResidues, queryResidues, sa, qa, queryFrame, baseSequence, scalingFactor, true);
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
      //NOTES on inputs:
      // 1. The base Sequence is always from the forward strand.
      // 2. The coordinates of the sq glyph are always forward strand.
      // 3. The 'little axis' in this view is forward strand.
      // 4. The last fixed range may be either forward or reverse, and matches
      //    the axis' reverse complement state.
      if ( baseSequence != null && baseSequence.length() > 0 ) {
         MutableRange tmpFixedRange = lastFixedRange.toMutableRange();
         if (isReverseComplement()) tmpFixedRange.mirror(axis.getMagnitude());
         long startOfSubSeq = Math.abs(Math.min(sqGlyph.start(),sqGlyph.end()) - tmpFixedRange.getMinimum());
         long lengthOfSubSeq = Math.abs(sqGlyph.end() - sqGlyph.start());
         Sequence childSequence = new SubSequence(baseSequence, startOfSubSeq, lengthOfSubSeq);
         String shadowResidue = SequenceHelper.toString(childSequence);
         SequenceAdapter queryAdapter=new AlignSequenceAdapter(shadowResidue, Color.red, sqGlyph.start(), false);
         sequenceHighlight=new AlignmentDNAGlyph(queryAdapter);
         sequenceTier.addGenomicChild(sequenceHighlight);
      }

      // Set the tooltip text for the feature
      if ( sqGlyph instanceof GenomicGlyph )
         ((JComponent)sqGlyph.getRootGlyph().container()).setToolTipText(getTextForTooltip((Feature)glyphEntityCollection.get(sqGlyph)));
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


   protected void setNucleotideSelection(Integer nucleotidePosition) {
      browserModel.setModelProperty( "ZoomToLocation", nucleotidePosition);
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
      setCurrentSelection(newSelection);
   }

   private void setCurrentSelection(Feature entity) {
      if ( entity != browserModel.getCurrentSelection() ) {
         browserModel.setCurrentSelection(entity);
      }
      if ( entity!=null ) showCurrentSelection();
   }

   private void showCurrentSelection() {
      if ( lastSelectedEntity==null || !(lastSelectedEntity instanceof Feature) ) return;
      Feature entity = (Feature)lastSelectedEntity;
      GeometricAlignment alignment = (GeometricAlignment)((Feature)lastSelectedEntity).getOnlyAlignmentToAnAxis(axis);
      if ( alignment==null ) return;
      Range range = alignment.getRangeOnAxis();
      double viewStart = range.getMinimum()-axisModel().viewSize()*0.1;
      axisModel().setOrigin(viewStart);
      this.setZoomLocation(range.getMinimum());
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

      /** Setup count-down variable for loading. */
      public MyHitAlignmentDetailFeatureObserver(HitAlignmentDetailFeature targetFeature) {
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

         if ( subViewRangePopulated ) {
            numberToLoad--;
            handleHSPSelection(feature, subjectAlignedResidues, queryAlignedResidues);
            // Try to determine if all features have arrived.
            if ( numberToLoad <= 0 ) {
               numberToLoad=0;
               QueryAlignmentView.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
               showCurrentSelection();
            }
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