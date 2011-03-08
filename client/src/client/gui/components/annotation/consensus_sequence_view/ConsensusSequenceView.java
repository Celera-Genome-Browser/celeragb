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

/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/

import api.entity_model.access.observer.*;
import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.management.ModelMgr;
import api.entity_model.management.ModifyManager;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.*;
import api.entity_model.model.assembly.Contig;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.AlignableGenomicEntity;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.fundtype.SingleAlignmentSingleAxis;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.stub.data.SequenceAnalysisQueryParameters;
import api.stub.geometry.MutableRange;
import api.stub.geometry.Range;
import api.stub.sequence.*;
import client.gui.framework.browser.Browser;
import client.gui.framework.navigation_tools.SequenceAnalysisDialog;
import client.gui.framework.pref_controller.PrefController;
import client.gui.framework.roles.SubEditor;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.BrowserModelListenerAdapter;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.framework.session_mgr.SessionModelListener;
import client.gui.framework.view_pref_mgr.ViewPrefMgr;
import client.gui.other.menus.TranslationMenu;
import client.gui.other.util.ClipboardUtils;
import client.shared.swing.FontChooser;
import client.shared.swing.GenomicSequenceViewer;
import client.shared.swing.genomic.*;
import client.shared.text_component.StandardTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
/**
 * View component of MVC for display of a contig's consensus residues at base-pair resolution.
 * Also shows selection if selection is a Feature on the displayed contig. Currently does
 * NOT register an observer for the Contig that is being displayed.
 */
public class ConsensusSequenceView extends JPanel implements ActionListener, SubEditor {

   // Heavily used debug flag.
   private boolean DEBUG = false;
   private static final String NOT_APPLICABLE_TEXT = "N/A";

   // The main objects of this component:
   // This string is used by the SubBrowser in order to figure out which SubView to
   // default focus on.
   public static final String DEFAULT_NAME = "Consensus Sequence";
   private static GenomicAxis axis;
   private GenomicSequenceViewer seqViewer;
   private Browser browser;
   private BrowserModel browserModel;


   private MyBrowserModelListener browserModelListener = new MyBrowserModelListener(); ;
   private MySessionModelListener sessionModelListener = new MySessionModelListener();
   private MyGenomicEntityVisitor selectionVisitor = new MyGenomicEntityVisitor();
   private MyEntityObserver entityObserver;
   private ModifyManagerObserver modifyMgrObserver = new MyModifyManagerObserver();
   private ModelMgrObserver modelMgrObserver = new MyModelMgrObserver();

   private MySequenceSearchListener findListener = new MySequenceSearchListener();
   private MyMouseHandler mouseHandler;
   private MySequenceKeyListener mySequenceKeyListener = new MySequenceKeyListener();
   private MyKeyListener myKeyListener = new MyKeyListener();
   private MySequenceSelectionListener mySelectionListener = new MySequenceSelectionListener();
   private MyAdjustmentListener myAdjustmentListener = new MyAdjustmentListener();

   private ConsensusSequenceCurationHandler curHandler;

   // Supporting atts
   private Color highlightColor, consensusColor, featureColor;
   private String consensusColorName = "";
   private String highlightColorName = "";
   private JTextField selectedTextStartPos, selectedTextEndPos;
   private JButton selectTextButton;
   private JLabel nucleLabel, featureOrderLabel;

   // The atts below keep track of the SubViewVisible and SubViewFixed ranges
   private MutableRange lastFixedRange = new MutableRange();
   private MutableRange lastVisibleRange = new MutableRange();

   //  These help to cache the state of the view and prevent unnecessary processing
   //  if nothing has changed.
   private GenomicEntity lastSelectionGE = null;
   private boolean knownRevCompState = false;
   private boolean disposing = false;

   // And the menus:
   private JMenuItem findMI = new JMenuItem("Find...", 'F');
   private TranslationMenu tlMenu;
   private ConsensusSequenceCopyMenu copyMenu;
   private JMenuItem fontMI = new JMenuItem("Font...");
   private JMenuItem editSettingsMI = new JMenuItem("Edit SubView Settings...");
   private static final String FONT_SETTINGS="ConsensusSequenceView.FontSettings";
   private JMenu copySeqToSeqAnalysis;

   // For curation reasons need to track the Entities displayed in the view.
   protected HashMap viewFeatures = new HashMap();

   boolean isEditable = false;
   private SequenceSearchDialog sequenceSearchDialog;


   public ConsensusSequenceView(Browser browser, Boolean isMaster) {
      this.browser = browser;
      browserModel = browser.getBrowserModel();
      mouseHandler = new MyMouseHandler();
      // The intent with this registry is that we refresh the view after every
      // command finishes.
      axis = (GenomicAxis)browserModel.getMasterEditorEntity();
      isEditable = !( browserModel.getMasterEditorEntity().getGenomeVersion().isReadOnly() );
      seqViewer = new GenomicSequenceViewer();
      curHandler = new ConsensusSequenceCurationHandler(browser, this, seqViewer);
      seqViewer.setSelectable(true);
      sequenceSearchDialog = new SequenceSearchDialog(browser, "Find in Consensus Residues View", false);
      // Make a label and text field contained in another panel:
      JLabel descLabel = new JLabel("Selected:");
      descLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      selectedTextStartPos = new StandardTextField(8);
      selectedTextStartPos.setMaximumSize(new Dimension(50, 20));
      selectedTextEndPos = new StandardTextField(8);
      selectedTextEndPos.setMaximumSize(new Dimension(50, 20));
      selectTextButton = new JButton("Select");
      selectTextButton.addActionListener(this);

      editSettingsMI.addActionListener(new ActionListener(){
                                          public void actionPerformed(ActionEvent evt) {
                                             if ( evt.getSource()==editSettingsMI ) {
                                                PrefController.getPrefController().getPrefInterface(client.gui.other.panels.ConsSeqViewPanel.class,
                                                                                                    SessionMgr.getSessionMgr().getActiveBrowser());
                                                SessionMgr.getSessionMgr().getActiveBrowser().repaint();
                                             }
                                          }
                                       });
      tlMenu    = new TranslationMenu(this);
      tlMenu.clickDefaults();
      if ( SessionMgr.getSessionMgr().getModelProperty(FONT_SETTINGS)!=null )
         seqViewer.setFont((Font)SessionMgr.getSessionMgr().getModelProperty(FONT_SETTINGS));
      fontMI.addActionListener(this);
      copyMenu  = new ConsensusSequenceCopyMenu("Copy Selected Sequence To Clipboard",this);
      //This menu activated only if the sequence has not been edited
      copySeqToSeqAnalysis=new ConsensusSequenceCopyMenu("Copy Selected Sequence To Sequence Analysis", this);
      copySeqToSeqAnalysis.remove(((ConsensusSequenceCopyMenu)copySeqToSeqAnalysis).aminoAcidCopyMenu);
      findMI.addActionListener(this);
      findMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK, false));

      this.registerKeyboardAction(this,
                                  KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK, false),
                                  JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      consensusColorName = (String)SessionMgr.getSessionMgr().getModelProperty("ConsSeqViewConsensusColor");
      highlightColorName = (String)SessionMgr.getSessionMgr().getModelProperty("ConsSeqViewHighlightColor");
      if ( consensusColorName==null ) consensusColorName = "Yellow";
      if ( highlightColorName==null ) highlightColorName = "Red";
      consensusColor = ViewPrefMgr.getViewPrefMgr().getColor(consensusColorName);
      highlightColor = ViewPrefMgr.getViewPrefMgr().getColor(highlightColorName);
      seqViewer.setSelectionBackground(highlightColor);
      seqViewer.setForeground(consensusColor);

      JPanel bottomPanel = new JPanel();
      bottomPanel.setLayout(new BoxLayout(bottomPanel,BoxLayout.Y_AXIS));

      JPanel searchPanel = new JPanel();
      searchPanel.setLayout(new BoxLayout(searchPanel,BoxLayout.X_AXIS));
      searchPanel.add(Box.createHorizontalStrut(10));
      searchPanel.add(descLabel);
      searchPanel.add(Box.createHorizontalStrut(10));
      searchPanel.add(selectedTextStartPos);
      searchPanel.add(Box.createHorizontalStrut(10));
      searchPanel.add(selectedTextEndPos);
      searchPanel.add(Box.createHorizontalStrut(10));
      searchPanel.add(selectTextButton);
      searchPanel.add(Box.createHorizontalGlue());

      // Create a bottom panel to hold the informational text fields...
      JPanel statisticsPanel = new JPanel();
      statisticsPanel.setLayout(new BoxLayout(statisticsPanel,BoxLayout.X_AXIS));

      statisticsPanel.add(Box.createHorizontalStrut(10));

      JPanel panel1 = new JPanel();
      panel1.setLayout(new BoxLayout(panel1,BoxLayout.Y_AXIS));
      panel1.add(new JLabel("Nucleotide Coordinate:"));
      statisticsPanel.add(panel1);

      statisticsPanel.add(Box.createHorizontalStrut(5));

      nucleLabel = new JLabel(NOT_APPLICABLE_TEXT);
      JPanel panel2 = new JPanel();
      panel2.setLayout(new BoxLayout(panel2,BoxLayout.Y_AXIS));
      panel2.add(nucleLabel);
      panel2.add(Box.createHorizontalStrut(5));
      statisticsPanel.add(panel2);
      statisticsPanel.add(Box.createHorizontalStrut(30));

      featureOrderLabel=new JLabel(NOT_APPLICABLE_TEXT);
      JPanel panel3 = new JPanel();
      panel3.setLayout(new BoxLayout(panel3,BoxLayout.Y_AXIS));
      panel3.add(Box.createVerticalGlue());
      panel3.add(new JLabel("Feature Order Number:"));
      statisticsPanel.add(panel3);
      statisticsPanel.add(Box.createHorizontalStrut(5));

      JPanel panel4 = new JPanel();
      panel4.setLayout(new BoxLayout(panel4,BoxLayout.Y_AXIS));
      panel4.add(Box.createVerticalGlue());
      panel4.add(featureOrderLabel);
      statisticsPanel.add(panel4);
      statisticsPanel.add(Box.createHorizontalGlue());

      bottomPanel.add(Box.createVerticalStrut(5));
      bottomPanel.add(searchPanel);
      bottomPanel.add(Box.createVerticalStrut(5));
      bottomPanel.add(statisticsPanel);
      bottomPanel.add(Box.createVerticalStrut(5));

      this.setLayout(new BorderLayout());
      this.add(seqViewer, BorderLayout.CENTER);
      this.add(bottomPanel, BorderLayout.SOUTH);
   }

  /**
   * Do what needs to be done when the view is active
   */
  public void activate() {
    //System.out.println("Activating CS");
    ModifyManager.getModifyMgr().addObserver( modifyMgrObserver);
    SessionMgr.getSessionMgr().addSessionModelListener(sessionModelListener);
    ModelMgr.getModelMgr().addModelMgrObserver(modelMgrObserver);
    seqViewer.addKeyListener(myKeyListener);
    seqViewer.addSequenceKeyListener(mySequenceKeyListener);
    seqViewer.addSequenceSelectionListener(mySelectionListener);
    seqViewer.addSequenceAdjustmentListener(myAdjustmentListener);
    seqViewer.addSequenceMouseListener(mouseHandler);


    seqViewer.requestFocus();

    sequenceSearchDialog.addSequenceSearchListener(findListener);
    GenomicEntity masterEntity = browserModel.getMasterEditorEntity();
    if (masterEntity != null)  {
        masterEntity.addGenomicEntityObserver(entityObserver, false);
    }
    if (knownRevCompState!=isReverseComplement()) {
      lastVisibleRange.mirror(axis.getMagnitude());
      knownRevCompState=isReverseComplement();
    }
    if (lastVisibleRange!=null && lastVisibleRange.getMagnitude()!=0)
      browserModel.setSubViewVisibleRange(lastVisibleRange);

    browserModel.addBrowserModelListener(browserModelListener, true);
    if(isConnectedToDatabase()){
	copySeqToSeqAnalysis.setEnabled(true);
    }else{
	copySeqToSeqAnalysis.setEnabled(false);
    }
  }


  /**
   * Stop listening when the view is no longer the visible, active one.
   */
  public void passivate() {
    //System.out.println("Passivating CS");
    if (disposing) {
      return;
    }
    ModifyManager.getModifyMgr().removeObserver(modifyMgrObserver);
    browserModel.removeBrowserModelListener(browserModelListener);
    SessionMgr.getSessionMgr().removeSessionModelListener(sessionModelListener);
    seqViewer.removeKeyListener(myKeyListener);
    seqViewer.removeSequenceSelectionListener(mySelectionListener);
    seqViewer.removeSequenceKeyListener(mySequenceKeyListener);
    seqViewer.removeSequenceAdjustmentListener(myAdjustmentListener);
    seqViewer.removeSequenceMouseListener(mouseHandler);
    sequenceSearchDialog.removeSequenceSearchListener(findListener);
    // One way to remove observer. Note that an observer can observe multiple entities,
    // but not in this case.
    GenomicEntity masterEntity = browserModel.getMasterEditorEntity();
    if (masterEntity != null)  {
        masterEntity.removeGenomicEntityObserver(entityObserver);
    }

    lastVisibleRange = new MutableRange(browserModel.getSubViewVisibleRange());
    knownRevCompState = isReverseComplement();
    browserModel.setSubViewVisibleRange(new Range());
  }

   public boolean canEditThisEntity(GenomicEntity entity) {
      // This view should always be visible.
      return (true);
   }


   public String getName() { return (DEFAULT_NAME);}


   public void dispose () {
      //System.out.println("Disposing CS");
      disposing = true;
      browserModel.removeBrowserModelListener(browserModelListener);
      SessionMgr.getSessionMgr().removeSessionModelListener(sessionModelListener);
      seqViewer.removeKeyListener(myKeyListener);
      seqViewer.removeSequenceSelectionListener(mySelectionListener);
      seqViewer.removeSequenceKeyListener(mySequenceKeyListener);
      seqViewer.removeSequenceAdjustmentListener(myAdjustmentListener);
      seqViewer.removeSequenceMouseListener(mouseHandler);
      sequenceSearchDialog.removeSequenceSearchListener(findListener);
      // One way to remove observer. Note that an observer can observe multiple entities,
      // but not in this case.
      GenomicEntity masterEntity = browserModel.getMasterEditorEntity();
      if ( masterEntity != null ) {
         masterEntity.removeGenomicEntityObserver(entityObserver);
      }

      browserModel = null;
      ModifyManager.getModifyMgr().removeObserver(modifyMgrObserver);
      ModelMgr.getModelMgr().removeModelMgrObserver(modelMgrObserver);

      SessionMgr.getSessionMgr().setModelProperty(FONT_SETTINGS,seqViewer.getFont());
      curHandler = null;
   }


   public JMenuItem[] getMenus() {
      JMenu[] curation_menus = curHandler.getMenus();
      ArrayList menuCollection = new ArrayList();
      JMenuItem[] menus = new JMenuItem[]{};
      menuCollection.add(tlMenu);
      menuCollection.add(findMI);
      if ( isEditable ) {
         for ( int i=0; i<curation_menus.length; i++ ) {
            menuCollection.add(curation_menus[i]);
         }
      }
      menuCollection.add(editSettingsMI);
      menuCollection.add(fontMI);
      menuCollection.add(copyMenu);
      menuCollection.add(copySeqToSeqAnalysis);
      menus = (JMenuItem[])menuCollection.toArray(menus);
      return (menus);
   }


   /** Used for checking if is in reverse complement axis mode */
   private boolean isReverseComplement()  {
      Boolean isRevComp = (Boolean)browserModel.getModelProperty(BrowserModel.REV_COMP_PROPERTY);
      if ( isRevComp == null ) throw new IllegalStateException("Model property GAAnnotRevComped not found.");
      return (isRevComp.booleanValue());
   }


   private void notifyReverseComplement(boolean reverse)  {
      if ( seqViewer==null ) return;
      Sequence seq;
      try {
         seq=seqViewer.getSequenceAt(GenomicSequenceViewer.DNA_DISPLAY);
      }
      catch ( Exception e ) {
         return;
      }

      Sequence sequence = DNA.reverseComplement(seq);
      MutableRange mRng = new MutableRange(lastFixedRange);
      mRng.mirror(axis.getMagnitude());
      // NOTE: forcing an adjustment event for ALL complement toggles.
      seqViewer.scrollToLocation(mRng.getMaximum());
      seqViewer.setDNASequence(DNASequenceStorage.create(sequence),mRng.getMinimum());
      displayAndHighlightSequence(lastSelectionGE, true);
   }


   /**
    * Get the axis alignment for the specified feature.
    */
   private MutableRange getRange(GenomicEntity ge)  {
      Range entityRange = null;
      MutableRange mrng = null;
      Alignment aa=null;
      if ( ge == null || (ge instanceof GenomicAxis) )
         return (null);
      try {
         if ( ge instanceof Contig ) {
            entityRange = getRangeForContig( (Contig)ge );
         }
         else if ( ge instanceof Feature ) {
            if ( !(ge instanceof SingleAlignmentSingleAxis) ) {
               // todo: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s).
               //System.out.println("ConsensusResiduesView: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s)");
               aa=((Feature)ge).getOnlyAlignmentToAnAxis(axis);
            }
            else {
               aa = ((SingleAlignmentSingleAxis)ge).getOnlyGeometricAlignmentToOnlyAxis();
            }
            if ( aa == null ) return (null);
            entityRange = ((GeometricAlignment)aa).getRangeOnAxis();
         }
         if ( entityRange == null ) {
            System.err.println("Error: No range found from GenomicEntity " + ge + " on axis=" + axis);
            return (null);
         }

         mrng = new MutableRange(entityRange);
         if ( isReverseComplement() ) {
            mrng.mirror(axis.getMagnitude());
         }
      }
      catch ( IllegalStateException ex ) {
         mrng = null;
         try {
            SessionMgr.getSessionMgr().handleException(ex);
         }
         catch ( Exception e ) {
            ex.printStackTrace();
         }
      }
      return (mrng);
   }

   private Range getRangeForContig(Contig contig)  {
      Collection contigAlignmentsOnAxis = contig.getAlignmentsToAxis(axis);
      if ( contigAlignmentsOnAxis.isEmpty() ) {
         //System.out.println("ConsensusResiduesView: Error no aligned ranges found for contig=" + contig);
         return (null);
      }
      /** @todo  Need to handle multiple alignments. Probably need to return multiple ranges here... */
      return((GeometricAlignment)contigAlignmentsOnAxis.iterator().next()).getRangeOnAxis();
   }


   private String getSelectedDNAString() {
      long start = seqViewer.getSelectionBegin();
      long end = seqViewer.getSelectionEnd()+1;
      long worldBegin = seqViewer.getWorldBeginLocation();
      Sequence tmpSequence = seqViewer.getSequenceAt(GenomicSequenceViewer.DNA_DISPLAY);
      return (SequenceHelper.toString(tmpSequence).substring((int)(start-worldBegin), (int)(end-worldBegin)));
   }

   private String getSelectedTranslationForFrame(int frame) {
      long start = seqViewer.getSelectionBegin();
      long end = seqViewer.getSelectionEnd()+1;
      long worldBegin = seqViewer.getWorldBeginLocation();
      Sequence baseSequence = new SubSequence(seqViewer.getSequenceAt(GenomicSequenceViewer.DNA_DISPLAY),start-worldBegin, end-start);
      return (SequenceHelper.toString(Protein.convertDNASequenceToProteinORF(baseSequence, frame)));
   }


   public void writeObject(ObjectOutputStream out) {
      System.out.println("Someone is trying to serialize the CR view.");
   }


   /** ActionListener Implementation */
   public void actionPerformed(ActionEvent e) {
      Object theItem = e.getSource();
      if ( theItem == curHandler.editUndoMI ) {
         // System.out.println("CS-actionPerformed(); Got an Undo action...");
         // Reset the seqViewer's sequence to that from the entity...
         // Look to currentSelection for the sequence...currentSelection = newSelection;

         // Go ahead and disable the undo... typing (keyReleased) will re-enable it.
         curHandler.editUndoMI.setEnabled(false);

         if ( lastSelectionGE != null ) {
            loadRangeSequence(lastFixedRange);
         }
      }

      if ( theItem == selectTextButton ) {

         int selStart;
         int selEnd;

         try {
            selStart = Integer.parseInt(selectedTextStartPos.getText());
            selEnd = Integer.parseInt(selectedTextEndPos.getText());
         }
         catch ( java.lang.NumberFormatException ex ) {
            selectedTextStartPos.setText("");
            selectedTextEndPos.setText("");
            return;
         }

         MutableRange selRange = new MutableRange(selStart, selEnd);
         MutableRange tmpViewRange = new MutableRange((int)seqViewer.getWorldBeginLocation(), (int)seqViewer.getWorldEndLocation());
         if ( tmpViewRange.contains(selRange) ) {
            seqViewer.setSelectionInterval(selStart, selEnd);
         }
      }

      if ( copyMenu != null ) {
         if ( theItem == copyMenu.nucleotideMI ) {
            String selRes = getSelectedDNAString();
            if ( (selRes == null) || (selRes.length() == 0) ) return;
            ClipboardUtils.setClipboardContents(selRes);
         }
         else if ( theItem == copyMenu.AAPlus1MI ) {
            String trans = getSelectedTranslationForFrame(Protein.FRAME_PLUS_ONE);
            ClipboardUtils.setClipboardContents(trans);
         }
         else if ( theItem == copyMenu.AAPlus2MI ) {
            String trans = getSelectedTranslationForFrame(Protein.FRAME_PLUS_TWO);
            ClipboardUtils.setClipboardContents(trans);
         }
         else if ( theItem == copyMenu.AAPlus3MI ) {
            String trans = getSelectedTranslationForFrame(Protein.FRAME_PLUS_THREE);
            ClipboardUtils.setClipboardContents(trans);
         }
      }

      if ( copySeqToSeqAnalysis != null ) {
         if ( theItem == ((ConsensusSequenceCopyMenu)copySeqToSeqAnalysis).nucleotideMI ) {
            String selRes = getSelectedDNAString();
            if ( (selRes == null) || (selRes.length() == 0) ) return;
            Sequence querySequence = seqViewer.getSequenceAt(GenomicSequenceViewer.DNA_DISPLAY);
            Range r=new Range((int)seqViewer.getSelectionBegin(),(int)seqViewer.getSelectionEnd());
            SequenceAnalysisQueryParameters ibp =
            new SequenceAnalysisQueryParameters(r,axis, DNASequenceStorage.create(selRes, ""), DNASequenceStorage.create(querySequence));

            browserModel.setModelProperty(SequenceAnalysisQueryParameters.PARAMETERS_PROPERTY_KEY,ibp);
            SequenceAnalysisDialog.getSequenceAnalysisDialog().showSearchDialog();
         }
         else if ( theItem == copyMenu.AAPlus1MI ) {
            String trans = getSelectedTranslationForFrame(Protein.FRAME_PLUS_ONE);
            ClipboardUtils.setClipboardContents(trans);
         }
         else if ( theItem == copyMenu.AAPlus2MI ) {
            String trans = getSelectedTranslationForFrame(Protein.FRAME_PLUS_TWO);
            ClipboardUtils.setClipboardContents(trans);
         }
         else if ( theItem == copyMenu.AAPlus3MI ) {
            String trans = getSelectedTranslationForFrame(Protein.FRAME_PLUS_THREE);
            ClipboardUtils.setClipboardContents(trans);
         }
      }


      // Transcription menu:
      if ( tlMenu != null ) {
         if ( theItem == tlMenu.transOne ) {
            JCheckBoxMenuItem jcbmi = (JCheckBoxMenuItem)theItem;
            seqViewer.setSequenceVisible(GenomicSequenceViewer.ORF_1_DISPLAY, jcbmi.getState());
         }
         else if ( theItem == tlMenu.transTwo ) {
            JCheckBoxMenuItem jcbmi = (JCheckBoxMenuItem)theItem;
            seqViewer.setSequenceVisible(GenomicSequenceViewer.ORF_2_DISPLAY, jcbmi.getState());
         }
         else if ( theItem == tlMenu.transThree ) {
            JCheckBoxMenuItem jcbmi = (JCheckBoxMenuItem)theItem;
            seqViewer.setSequenceVisible(GenomicSequenceViewer.ORF_3_DISPLAY, jcbmi.getState());
         }
         else if ( theItem == tlMenu.transNegOne ) {
            JCheckBoxMenuItem jcbmi = (JCheckBoxMenuItem)theItem;
            seqViewer.setSequenceVisible(GenomicSequenceViewer.ORF_NEG1_DISPLAY, jcbmi.getState());
         }
         else if ( theItem == tlMenu.transNegTwo ) {
            JCheckBoxMenuItem jcbmi = (JCheckBoxMenuItem)theItem;
            seqViewer.setSequenceVisible(GenomicSequenceViewer.ORF_NEG2_DISPLAY, jcbmi.getState());
         }
         else if ( theItem == tlMenu.transNegThree ) {
            JCheckBoxMenuItem jcbmi = (JCheckBoxMenuItem)theItem;
            seqViewer.setSequenceVisible(GenomicSequenceViewer.ORF_NEG3_DISPLAY, jcbmi.getState());
         }
         else if ( theItem == tlMenu.noComp ) {
            seqViewer.setSequenceVisible(GenomicSequenceViewer.COMPLEMENT_DNA_DISPLAY, false);
         }
         else if ( theItem == tlMenu.revComp ) {
            seqViewer.setSequenceVisible(GenomicSequenceViewer.COMPLEMENT_DNA_DISPLAY, true);
         }
         else if ( theItem == tlMenu.oneLetter ) {
            seqViewer.setTranslationStyle(GenomicSequenceViewer.SINGLE_CHAR_TRANSLATION);
         }
         else if ( theItem == tlMenu.threeLetter ) {
            seqViewer.setTranslationStyle(GenomicSequenceViewer.ABBREVIATED_TRANSLATION);
         }
      }

      if ( theItem == findMI ) {
         Sequence residues = seqViewer.getSequenceAt(GenomicSequenceViewer.DNA_DISPLAY);
         if ( (residues==null) || (residues.length()==0) ) return;
         if ( !sequenceSearchDialog.isVisible() ) {
            sequenceSearchDialog.setLocationRelativeTo(browser);
            sequenceSearchDialog.showSearchDialog(residues, seqViewer.getWorldBeginLocation());
         }
      }

      if ( theItem == fontMI ) {
         // Start the chooser and init with current.
         Font oldFont = seqViewer.getFont();
         FontChooser chooser = new FontChooser();
         Font newFont = chooser.showDialog(browser, "Consensus Sequence Font", oldFont);
         if ( newFont == null ) return;
         if ( !newFont.equals(oldFont) ) {
            seqViewer.setFont(newFont);
            SessionMgr.getSessionMgr().setModelProperty(FONT_SETTINGS, newFont);
         }
      }
   }


   private void loadRangeSequence(Range loadRange)  {
      MutableRange axisRange = loadRange.toMutableRange();
      if ( DEBUG ) System.out.println("loadRangeSequence: requesting sequence for range: " + loadRange + " mag=" + loadRange.getMagnitude());
      if ( isReverseComplement() ) {
         axisRange.mirror(axis.getMagnitude());
      }
      axis.loadNucleotideSeq(axisRange, new MySequenceObserver());
   }


   private void highlightCompositeFeature(Feature compositeFeature)  {
      Collection subFeatures = compositeFeature.getSubFeatures();
      if ( subFeatures == null ) {
         return;
      }
      Iterator subFeatureIterator = subFeatures.iterator();
      Feature feature;
      viewFeatures.put(compositeFeature, null);

      while ( subFeatureIterator.hasNext() ) {
         feature = (Feature)subFeatureIterator.next();
         highlightFeatureRange(feature, false);
      }

      if ( compositeFeature instanceof CuratedTranscript ) {
         CuratedTranscript transcript = (CuratedTranscript)compositeFeature;
         CuratedCodon startCodon = transcript.getStartCodon();
         CuratedCodon stopCodon = transcript.getStopCodon();
         if ( startCodon != null ) highlightFeatureRange(startCodon, false);
         if ( stopCodon != null ) highlightFeatureRange(stopCodon, false);
      }
   }

   /**
    * If an entity changed that is related to the view then redraw the view
    */
   private void updateViewIfNecessary(Feature feature, boolean detach) {
      boolean debugMethod = false;
      if ( debugMethod ) System.out.println("ConsensusResiduesView - updateViewIfNecessary");
      if ( feature == null ) return;
      HashMap tmpViewFeatures = new HashMap(viewFeatures);  // necessary to prevent concurrent modification.
      GenomicEntity selection = browserModel.getCurrentSelection();
      Feature selectedFeature;
      if ( selection instanceof Feature ) {
         selectedFeature = (Feature)selection;
         if ( (feature.getRootFeature() == selectedFeature.getRootFeature()) ||
              (tmpViewFeatures.containsKey(feature)) ) {
            if ( debugMethod ) System.out.println("updateViewIfNecessary - redisplaying feature.");
            //update the view
            this.clearSelections();
            if ( detach ) {
               Feature f;
               for ( Iterator i=tmpViewFeatures.keySet().iterator(); i.hasNext(); ) {
                  f = (Feature)i.next();
                  if ( (f.isSimple()) && (f != feature) ) {
                     displayFeature(f, false);
                     break;
                  }
               }
            }
            else {
               displayFeature(feature, false);
            }
         }
      }
   }

   private void displayAndHighlightSequence(GenomicEntity ge, boolean doScroll) {
      //Clear all text color annotation first
      this.clearSelections();
      viewFeatures.clear();

      if ( DEBUG ) System.out.println("In displayAndHighlightSequence");
      if ( ge == null ) return;

      if ( ge instanceof Feature ) {
         Feature selectedFeature = (Feature)ge;

         // Composite feature check.
         if ( selectedFeature.hasSubFeatures() ) {
            // Highlight ranges
            highlightCompositeFeature(selectedFeature);
            //Scroll to selectedFeature
            if ( doScroll ) seqViewer.scrollToLocation(getScrollLocation(selectedFeature));
         }
         //  Else must be a simple feature.
         else {
            Feature superFeature = selectedFeature.getSuperFeature();
            if ( superFeature == null )
               // No parent, just highlight itself
               highlightFeatureRange(selectedFeature, true);
            else {
               highlightCompositeFeature(superFeature);
            }

            // Scroll to selectedFeature
            if ( doScroll ) seqViewer.scrollToLocation(getScrollLocation(selectedFeature));
         }
      }
      else if ( ge instanceof Contig ) {
         highlightContigRange((Contig)ge);
      }
   }

   private int getScrollLocation(Feature selectedFeature) {
      int scrollLocation;
      if ( isReverseComplement() ) {
         Range featureRange = ((GeometricAlignment)selectedFeature.getOnlyAlignmentToAnAxis(axis)).getRangeOnAxis();
         MutableRange mutableRange = new MutableRange(featureRange);
         mutableRange.mirror(axis.getMagnitude());
         scrollLocation = mutableRange.getEnd();
      }
      else
         scrollLocation = ((GeometricAlignment)selectedFeature.getOnlyAlignmentToAnAxis(axis)).getRangeOnAxis().getStart();
      return (scrollLocation);
   }

   /** Extract a sequence from the entity, and set the SeqViewer's residues to it. */
   protected void displayContig(Contig contig)  {
      Range range = getRangeForContig(contig);
      if ( range == null ) {
         System.err.println("Error: No range found from contig=" + contig + " on axis=" + axis);
         return;
      }

      MutableRange contigAxisRange = new MutableRange(range);

      if ( isReverseComplement() ) {
         contigAxisRange.mirror(axis.getMagnitude());
      }
      MutableRange tmpViewRange = new MutableRange((int)seqViewer.getWorldBeginLocation(), (int)seqViewer.getWorldEndLocation());
      if ( tmpViewRange.contains(contigAxisRange) ) {
         displayAndHighlightSequence(contig, true);
      }
      else {
         //loadRangeSequence(contigRange);
         System.err.println("Error: contig " + contig + " not contained within axis " + axis);
         return;
      }
   }

   /**
    * Display a feature.
    * @param doScroll scroll to the feature?
    */
   protected void displayFeature(Feature feature, boolean doScroll)  {
      Alignment aa=null;
      if ( feature == null ) return;
      //  Ensure that the user can copy internal seq to Blast.
      // copySeqToSeqAnalysis.setEnabled(true);
      if ( !(feature instanceof SingleAlignmentSingleAxis) ) {
         aa=feature.getOnlyGeometricAlignmentToAnAxis(axis);
      }
      if ( feature instanceof SingleAlignmentSingleAxis ) {
         SingleAlignmentSingleAxis fproxy;
         if ( feature.hasSuperFeature() ) {
            fproxy = (SingleAlignmentSingleAxis)feature.getRootFeature();
            if ( fproxy == null ) {
               fproxy = (SingleAlignmentSingleAxis)feature;
            }
         }
         else {
            fproxy = (SingleAlignmentSingleAxis)feature;
         }
         aa = fproxy.getOnlyAlignmentToOnlyAxis();
      }

      if ( aa == null ) return;
      Range featureRange = ((GeometricAlignment)aa).getRangeOnAxis();
      if ( featureRange == null ) {
         System.err.println("Error: No range found from feature " + feature + " on axis " + axis);
         return;
      }

      MutableRange featureAxisRange = featureRange.toMutableRange();
      if ( isReverseComplement() ) {
         featureAxisRange.mirror(axis.getMagnitude());
      }
      MutableRange tmpViewRange = new MutableRange((int)seqViewer.getWorldBeginLocation(), (int)seqViewer.getWorldEndLocation());
      if ( tmpViewRange.contains(featureAxisRange) ) {
         displayAndHighlightSequence(feature, doScroll);
      }
   }


   private void highlightContigRange(Contig contig) {
      if ( DEBUG ) System.out.println("....highlightContigRange");
      if ( contig == null ) return;

      MutableRange contigRange = getRange(contig);
      if ( contigRange == null ) return;
      if ( DEBUG ) System.out.println("........contigRange min="+contigRange.getMinimum(Range.ZERO_BASED_INDEXING)+" max="+contigRange.getMaximum(Range.ZERO_BASED_INDEXING));
      clearSelections();
      Color contigColor = Color.white;

      int start = Math.min(contigRange.getMinimum(Range.ZERO_BASED_INDEXING), contigRange.getMaximum(Range.ZERO_BASED_INDEXING));
      int end = Math.max(contigRange.getMinimum(Range.ZERO_BASED_INDEXING), contigRange.getMaximum(Range.ZERO_BASED_INDEXING));

      // Add the annotation to the GenomicseqViewer, and keep track of it internally
      seqViewer.addAdornment(new Adornment(start, end, contigColor, Color.black));  // here
   }


   /** Reset any highlighted regions. */
   private void clearSelections() {
      seqViewer.clearSelection();
      seqViewer.clearAllAdornments();
   }


   private boolean isConnectedToDatabase(){
      boolean dbConnected = false;
      ArrayList openGenomeVersions = new ArrayList(ModelMgr.getModelMgr().getSelectedGenomeVersions());
      for ( Iterator it = openGenomeVersions.iterator();it.hasNext(); ) {
         if ( ((GenomeVersion)it.next()).getGenomeVersionInfo().isDatabaseDataSource() ) {
            dbConnected = true;
            break;
         }
      }
      return (dbConnected);
   }

   /**
    * This method will allow the ConsSeq Curation Handler access to the adornments
    * so that it can directly modify the curated adornment.  The view component no
    * longer maintains the data model.
    */
   public HashMap getFeatureToAdornmentMap() { return (viewFeatures);}


   /**
    * retruns 1 if the feature is forward, -1 if it is reverse, and 0 if it has unknown orientation
    * the returned orientation takes into account the reverse complement mode of the editor.
    */
   private int getFeatureOrientation(Feature feature) {

      GeometricAlignment aa = feature.getOnlyGeometricAlignmentToAnAxis(axis);
      if ( aa==null ) {
         System.err.println("Error: no or multiple alignments found for feature=" + feature + " on axis=" + axis);
         return (0);
      }

      Range rangeOnAxis = aa.getRangeOnAxis();

      if ( rangeOnAxis.getMagnitude() > 0 ) {
         if ( rangeOnAxis.isReversed() ^ isReverseComplement() ) return (-1);
         else return (1);
      }
      else {
         //try to figure out the orientation from the super feature if there is one
         Feature superFeature = feature.getSuperFeature();
         if ( superFeature != null ) {
            return (getFeatureOrientation(superFeature));
         }
      }
      return (0);
   }


   /**
    * Highlight in the GenomicseqViewer the range of the given feature.  Use the
    * singleton parameter to indicate if it's to be the only range highlighted
    */
   private void highlightFeatureRange(Feature feature, boolean singleton)  {
      if ( feature == null ) return;
      MutableRange featureRange = getRange(feature);
      if ( featureRange == null ) return;

      // Remove the old selection, when appropriate
      if ( singleton ) clearSelections();

      // Add the annotation to the GenomicseqViewer, and keep track of it internally
      int start = featureRange.getMinimum(Range.ZERO_BASED_INDEXING);
      int end = featureRange.getMaximum(Range.ZERO_BASED_INDEXING);
      featureColor = ViewPrefMgr.getViewPrefMgr().getColorForEntity(feature);
      if ( feature instanceof CodonFeature ) {
         if ( featureRange.getMagnitude()==0 ) {
            int orientation = getFeatureOrientation(feature);

            if ( orientation == 1 ) {
               end += 3;
            }
            else if ( orientation == -1 ) {
               start -= 3;
            }
         }
      }

      if ( DEBUG ) System.out.println("In highlightFeatureRange");
      // Draw Splice Sites the old fashioned way.
      if ( feature instanceof SpliceSite ) {
         // Acceptors on one side.
         if ( ((SpliceSite)feature).isAcceptorSpliceSite() ) {
            if ( featureRange.isForwardOrientation() ) {
               start = featureRange.getStart() - 2;
               end   = featureRange.getStart() - 1;
            }
            else {
               start = featureRange.getStart();
               end   = featureRange.getStart() + 1;
            }
         }
         // Donors on the other.
         else {
            if ( featureRange.isForwardOrientation() ) {
               start = featureRange.getStart();
               end   = featureRange.getStart() + 1;
            }
            else {
               start = featureRange.getStart() - 2;
               end   = featureRange.getStart() - 1;
            }
         }
      }
      Adornment tmpAdornment = new Adornment(start, end, featureColor, Color.white);
      viewFeatures.put(feature, tmpAdornment);
      if ( DEBUG ) System.out.println("Adding an adornment for "+feature.getOid());
      seqViewer.addAdornment(tmpAdornment);
   }


   private class MyGenomicEntityVisitor extends GenomicEntityVisitor {
      public void visitContig(Contig contig)  {
         displayContig(contig);
      }

      public void visitFeature(Feature feature)  {
         displayFeature(feature, true);
      }
   }


   /**
    * Inner class for handling browser model events
    */
   private class MyBrowserModelListener extends BrowserModelListenerAdapter {
      /** The axis of the master editor */

      public void browserMasterEditorEntityChanged(GenomicEntity masterEditorEntity) {
         // This is invoked when anyone calls browserModel.setMasterEditorEntity().
         //System.out.println("ConsensusResiduesView - browserMasterEditorEntityChanged masterEditorEntity = "+masterEditorEntity);

         if ( (masterEditorEntity == null) || !(masterEditorEntity instanceof GenomicAxis) ) {
            seqViewer.clearAll();
            return;
         }

         if ( axis != masterEditorEntity ) {
            axis = (GenomicAxis)masterEditorEntity;
            seqViewer.clearAll();
         }
      }


      public void browserSubViewFixedRangeChanged(Range subViewFixedRange) {
         if ( subViewFixedRange==null ) return;
         MutableRange tmpRange = subViewFixedRange.toMutableRange();
         if ( lastFixedRange.equals(tmpRange) ) return;
         else lastFixedRange = tmpRange;
         //System.out.println("Setting fixed to "+lastFixedRange);
         if ( lastFixedRange.getMagnitude()==0 ) return;
         lastSelectionGE = null;
         clearSelections();
         loadRangeSequence(lastFixedRange);
         if ( isConnectedToDatabase() ) {
            copySeqToSeqAnalysis.setEnabled(true);
         }
         else {
            copySeqToSeqAnalysis.setEnabled(false);
         }
      }


      /** The axis of the sub editor */
      public void browserSubViewVisibleRangeChanged(Range subViewVisibleRange) {
         if ( subViewVisibleRange==null || subViewVisibleRange.getMagnitude()==0 ) return;
         if ( lastVisibleRange.equals(subViewVisibleRange) ) return;
         GenomicEntity subEditorEntity = browserModel.getMasterEditorEntity();
         // This only uses the entity for the axis also.
         if ( DEBUG ) System.out.println("ConsensusResiduesView - browserSubViewVisibleRangeChanged subEditorEntity = " + subEditorEntity);
         // This is invoked when anyone calls browserModel.setSubEditorState().
         MutableRange tmpRange = subViewVisibleRange.toMutableRange();
         if ( isReverseComplement() ) {
            tmpRange.mirror(axis.getMagnitude());
         }
         lastVisibleRange = tmpRange;
         // And empty axis means clear the GenomicSequenceViewer
         if ( subEditorEntity == null ) {
            seqViewer.clearAllAdornments();
            seqViewer.clearSelection();
            return;
         }

         if ( axis != subEditorEntity ) {
            if ( subEditorEntity instanceof GenomicAxis ) {
               axis = (GenomicAxis)subEditorEntity;
            }
         }

         // Assume that the shadow was dragged in the master editor --
         // scroll the GenomicSequenceViewer to match.
         seqViewer.scrollToLocation(subViewVisibleRange.getMinimum(Range.ZERO_BASED_INDEXING));
      }


      /** The last selection in the browser, regardless of the source of selection */
      public void browserCurrentSelectionChanged(GenomicEntity newSelection)  {
         if ( DEBUG ) System.out.println("ConsensusResiduesView - browserCurrentSelectionChanged");
         if ( lastSelectionGE!=null && lastSelectionGE.equals(newSelection) ) return;
         // Block duplicate notifications
         if ( newSelection == null ) {
            clearSelections();
            lastSelectionGE = null;
            return;
         }
         MutableRange tmpRange = new MutableRange();
         if ( newSelection instanceof Contig ) {
            tmpRange = ((GeometricAlignment)
                        ((Contig)newSelection).getAlignmentsToAxes().iterator().next()).getRangeOnAxis().toMutableRange();
         }
         else if ( newSelection instanceof Feature ) {
            if ( DEBUG ) System.out.println("Seeing feature");
            Feature tmpRootFeature = ((Feature)newSelection).getRootFeature();
            if (null==tmpRootFeature) {
				clearSelections();
				lastSelectionGE = null;
				return;            	
            }
            GeometricAlignment tmpAlignment = (GeometricAlignment)tmpRootFeature.getOnlyAlignmentToAnAxis(axis);
            if (null==tmpAlignment) {
				clearSelections();
				lastSelectionGE = null;
				return;            	
            }
            tmpRange = tmpAlignment.getRangeOnAxis().toMutableRange();
            if ( isReverseComplement() ) tmpRange.mirror(axis.getMagnitude());
         }
         if ( !lastFixedRange.contains(tmpRange) ) {
            clearSelections();
            if ( DEBUG ) System.out.println("tmpRange not witin last fixed range");
            return;
         }
         lastSelectionGE = newSelection;

         // Only Workspace features as current selection should let the sequence be edited
         if ( lastSelectionGE.isScratch() ) seqViewer.setEditable(true);
         else seqViewer.setEditable(false);

         newSelection.acceptVisitorForSelf(selectionVisitor);
      }

      public void browserClosing() { browser = null;}
      public void modelPropertyChanged(Object property,Object oldValue, Object newValue)  {
         if ( property.equals(BrowserModel.REV_COMP_PROPERTY) ) {
            boolean rcVal = ((Boolean) newValue).booleanValue();
            notifyReverseComplement(rcVal);
            lastFixedRange.mirror(axis.getMagnitude());
            lastVisibleRange.mirror(axis.getMagnitude());
         }
      }
   }


   /**
    * Private inner class for handling Session model property changes
    */
   private class MySessionModelListener implements SessionModelListener {
      public void browserAdded(BrowserModel browserModel){}
      public void browserRemoved(BrowserModel browserModel){}
      public void sessionWillExit(){}
      public void modelPropertyChanged(Object property,Object oldValue, Object newValue)  {
         if ( property.equals("ConsSeqViewConsensusColor") ||
              property.equals("ConsSeqViewHighlightColor") ) {
            consensusColorName = (String)SessionMgr.getSessionMgr().getModelProperty("ConsSeqViewConsensusColor");
            highlightColorName = (String)SessionMgr.getSessionMgr().getModelProperty("ConsSeqViewHighlightColor");
            if ( consensusColorName==null ) consensusColorName = "Yellow";
            if ( highlightColorName==null ) highlightColorName = "Red";
            consensusColor = ViewPrefMgr.getViewPrefMgr().getColor(consensusColorName);
            highlightColor = ViewPrefMgr.getViewPrefMgr().getColor(highlightColorName);
            seqViewer.setForeground(consensusColor);
            seqViewer.setSelectionBackground(highlightColor);
         }
      }
   }


   /**
    * Inner class for handling consensus loading
    */
   private class MySequenceObserver implements SequenceObserver {
      public MySequenceObserver() {}

      public void noteSequenceArrived(Axis axis, Range rangeOfSequence, Sequence sequence)  {
         //  If the sequenceLength is wrong for the fixed range, do nothing.
         if ( sequence.length() != lastFixedRange.getMagnitude() ) return;
         if ( isReverseComplement() )
            sequence = DNA.reverseComplement(sequence);
         //System.out.println("Range: "+lastFixedRange.toString());
         //System.out.println("Sequence Length: "+sequence.length());
         seqViewer.setDNASequence(DNASequenceStorage.create(sequence), lastFixedRange.getMinimum());
         MutableRange defaultRange = new MutableRange((int)seqViewer.getVisibleBeginLocation(),
                                                      (int)seqViewer.getVisibleEndLocation(), Range.ZERO_BASED_INDEXING);

         browserModel.setSubViewVisibleRange(defaultRange);

         lastSelectionGE = browserModel.getCurrentSelection();
         if ( lastSelectionGE != null && lastSelectionGE instanceof Feature ) {
            GeometricAlignment tmpAlignment =
            (GeometricAlignment)((Feature)lastSelectionGE).getRootFeature().getOnlyAlignmentToAnAxis(axis);
            if ( tmpAlignment==null ) {
               return;
            }

            MutableRange tmpRange = tmpAlignment.getRangeOnAxis().toMutableRange();
            if ( isReverseComplement() ) tmpRange.mirror(axis.getMagnitude());
            if ( lastFixedRange.contains(tmpRange) )
               displayAndHighlightSequence(lastSelectionGE, true);
         }
      }
   }


   /**
    * This class listens to the SequenceViewer and reports when the user has scrolled
    * to a new visible region.  Reports such to the BrowserModel.
    */
   private class MyAdjustmentListener implements SequenceAdjustmentListener {
      public void adjustmentValueChange(SequenceAdjustmentEvent e) {
         long start = e.getVisibleBeginLocation();
         long end   = e.getVisibleEndLocation();

         MutableRange tmpRange = new MutableRange((int)start, (int)end);
         lastVisibleRange = tmpRange;
         browserModel.setSubViewVisibleRange(tmpRange);
      }
   }


   private class MySequenceSelectionListener implements SequenceSelectionListener {
      public void selectionChanged(SequenceSelectionEvent e) {
         String tmpMin = Long.toString(Math.min(e.getBeginLocation(),e.getEndLocation()));
         String tmpMax = Long.toString(Math.max(e.getBeginLocation(),e.getEndLocation()));
         selectedTextStartPos.setText(tmpMin);
         selectedTextEndPos.setText(tmpMax);
         formatStatisticsForDisplay(e.getEndLocation());
      }
   }


   private class MyMouseHandler implements SequenceMouseListener {
      public void mouseSelectedRange(SequenceMouseEvent e){
         if ( lastFixedRange == null || lastFixedRange.getMagnitude()==0 ) return;
         long nucleotidePos = lastFixedRange.getMinimum() + e.getSequencePosition();
         formatStatisticsForDisplay(nucleotidePos);
      }
      public void mousePressed(SequenceMouseEvent e){}
      public void mouseReleased(SequenceMouseEvent e){}
      /**
      * Get mouse moved notification...
      * Get the residue coordinates of the mouse position and update the
      * approrpriate text fields.
      */
      public void mouseMoved(SequenceMouseEvent e) {
         // Set the Nucleotide label.
         if ( lastFixedRange == null || lastFixedRange.getMagnitude()==0 ) return;
         long nucleotidePos = lastFixedRange.getMinimum() + e.getSequencePosition();
         formatStatisticsForDisplay(nucleotidePos);
      }
   } //end inner class MyMouseHandler

   /**
    * This helper method updates the nucleotide coordinate and feature order number
    * when the user MOUSES around the view or uses the ARROW KEYS to navigate
    * the view.  This method assumes that leaf children and their direct parent are drawn
    * the same way.  Whether an exon/hsp is selected or a Transcript/BlastN is selected
    * the view draws the same.  A feature higher in the hierarchy, like a gene,
    * still works fine too, as it draws differently with the entire transcript colored in.
    * May not make complete sense but better than no explanation.
    */
   private void formatStatisticsForDisplay(long mousePosition) {
      nucleLabel.setText (Integer.toString((int)mousePosition));

      // Get and set the Feature Order label.
      if ( !(lastSelectionGE instanceof Feature) ) {
         featureOrderLabel.setText(NOT_APPLICABLE_TEXT);
         return;
      }
      Feature feature = (Feature)lastSelectionGE;
      if ( feature.getSuperFeature()!=null && !(feature instanceof CuratedTranscript) ) {
         feature=(Feature)feature.getSuperFeature();
      }
      Feature subfeature=feature.getSubFeatureAtPositionOnAxis((Axis)browserModel.getMasterEditorEntity(),
                                                               (int)mousePosition);
      if ( subfeature!=null && subfeature.getProperty(FeatureFacade.ORDER_NUM_PROP)!=null ) {
         featureOrderLabel.setText(subfeature.getProperty(FeatureFacade.ORDER_NUM_PROP).getInitialValue());
      }
      else {
         featureOrderLabel.setText(NOT_APPLICABLE_TEXT);
      }
   }


   private class MySequenceKeyListener implements SequenceKeyListener {
      public void keyTyped(SequenceKeyEvent e) {
         curHandler.editUndoMI.setEnabled(true);

      }
   }

   private class MyKeyListener implements KeyListener {
      public void keyTyped(KeyEvent p0) {
      }
      public void keyPressed(KeyEvent p0) {

         int keyCode = p0.getKeyCode();
         System.out.println((long)lastFixedRange.toRange().getStart());

         if ( p0.isControlDown() && keyCode==KeyEvent.VK_F ) sequenceSearchDialog.showSearchDialog(seqViewer.getSequenceAt(GenomicSequenceViewer.DNA_DISPLAY), (long)lastFixedRange.toRange().getStart());
      }
      /**
       * As soon as the seq is edited the copy to IB should be disabled.  The user
       * should only be able to Blast (Search) internal data for alignment reasons.
       */
      public void keyReleased(KeyEvent p0) {

         int keyCode = p0.getKeyCode();
         if ( (keyCode != KeyEvent.VK_PAGE_DOWN) && (keyCode != KeyEvent.VK_PAGE_UP) )
            copySeqToSeqAnalysis.setEnabled(false);
      }
   }


   /**
    *  This class watches the ModifyMgr and updates the information in the
    *  view without changing the scroll position.
    */
   private class MyModifyManagerObserver extends ModifyManagerObserverAdapter {
      public void noteCommandDidFinish(String commandName, int unused) {
         if ( browserModel.getCurrentSelection() instanceof Feature ) {
            displayFeature((Feature)browserModel.getCurrentSelection(),false);
         }
      }
   }


   private class MyModelMgrObserver extends ModelMgrObserverAdapter {

      public void genomeVersionSelected(GenomeVersion genomeVersion){
         if ( genomeVersion.getGenomeVersionInfo().isDatabaseDataSource() ) {
            copySeqToSeqAnalysis.setEnabled(true);
         }
      }
   }


   /**
    * Private inner class for handling alignment events.
    */
   private class MyEntityObserver extends AxisObserverAdapter {

      public void noteAlignmentOfEntity(Alignment addedAlignment)  {
         AlignableGenomicEntity alignedFeature = addedAlignment.getEntity();
         if ( alignedFeature instanceof Feature ) {
            updateViewIfNecessary((Feature)alignedFeature, false);
         }
      }

      public void noteUnalignmentOfEntity(Alignment removedAlignment)  {
         AlignableGenomicEntity removedFeature = removedAlignment.getEntity();
         if ( removedFeature instanceof Feature )
            updateViewIfNecessary((Feature)removedFeature, true);
      }

      public void noteEntityAlignmentChanged(Alignment changedAlignment)  {
         AlignableGenomicEntity changedFeature = changedAlignment.getEntity();
         if ( changedFeature instanceof Feature )
            updateViewIfNecessary((Feature)changedFeature, false);
      }

      public void noteEntityDetailsChanged(GenomicEntity entity, boolean initialLoad)  {
         if ( entity instanceof Feature )
            updateViewIfNecessary((Feature)entity, false);
      }
   }


   private class MySequenceSearchListener implements SequenceSearchListener {
      public void focusOnSearchTarget(SwingRange targetRange) {
         seqViewer.setSelectionInterval(targetRange.getStartRange(),targetRange.getEndRange());
         seqViewer.scrollToLocation(targetRange.getStartRange());
      }
   }
}
