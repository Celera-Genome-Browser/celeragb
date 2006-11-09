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

import api.entity_model.access.observer.AlignableGenomicEntityObserverAdapter;
import api.entity_model.access.observer.ModelMgrObserverAdapter;
import api.entity_model.access.observer.ModifyManagerObserverAdapter;
import api.entity_model.access.observer.SequenceObserver;
import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.management.ModelMgr;
import api.entity_model.management.ModifyManager;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.CuratedCodon;
import api.entity_model.model.annotation.CuratedExon;
import api.entity_model.model.annotation.CuratedFeature;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.annotation.SubFeature;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.EntityTypeSet;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.fundtype.SingleAlignmentSingleAxis;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;
import api.stub.data.SequenceAnalysisQueryParameters;
import api.stub.geometry.MutableRange;
import api.stub.geometry.Range;
import api.stub.sequence.DNA;
import api.stub.sequence.DNASequenceStorage;
import api.stub.sequence.Protein;
import api.stub.sequence.ProteinSequenceStorage;
import api.stub.sequence.Sequence;
import api.stub.sequence.SequenceHelper;
import api.stub.sequence.SequenceUtil;
import api.stub.sequence.SubSequence;
import client.gui.framework.browser.Browser;
import client.gui.framework.navigation_tools.SequenceAnalysisDialog;
import client.gui.framework.pref_controller.PrefController;
import client.gui.framework.roles.SubEditor;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.BrowserModelListenerAdapter;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.framework.session_mgr.SessionModelListener;
import client.gui.framework.view_pref_mgr.ViewPrefMgr;
import client.gui.other.fasta.FastaObject;
import client.gui.other.fasta.FastaWriter;
import client.gui.other.menus.TranscriptTranslateTranslationMenu;
import client.gui.other.util.ClipboardUtils;
import client.gui.other.panels.TransTransPanel;
import client.shared.swing.FontChooser;
import client.shared.swing.GenomicSequenceViewer;
import client.shared.swing.genomic.Adornment;
import client.shared.swing.genomic.SequenceAdjustmentEvent;
import client.shared.swing.genomic.SequenceAdjustmentListener;
import client.shared.swing.genomic.SequenceKeyEvent;
import client.shared.swing.genomic.SequenceKeyListener;
import client.shared.swing.genomic.SequenceMouseEvent;
import client.shared.swing.genomic.SequenceMouseListener;
import client.shared.swing.genomic.SequenceSearchDialog;
import client.shared.swing.genomic.SequenceSearchListener;
import client.shared.swing.genomic.SequenceSelectionEvent;
import client.shared.swing.genomic.SequenceSelectionListener;
import client.shared.swing.genomic.SwingRange;
import shared.util.GANumericConverter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;


/**
 * View component of MVC for display of a spliced transcript's residues at base-pair resolution
 *
 * also maintains a SpliceableObserver that registers to observe the Spliceable feature
 *   (must be both a FeatureGenomicEntity and a Spliceable), and observer dynamicly updates
 *   display when FeatureGenomicEntity notifies it of changes
 */
public class TranscriptTranslateView extends JPanel implements ActionListener, SubEditor {

   private GenomicEntity currentSelection;
   private GenomicAxis masterAxis;
   private GenomicSequenceViewer seqViewer= new GenomicSequenceViewer();
   private Feature currentFeature;
   private static final String FONT_SETTINGS="TransTransView.FontSettings";
   private static final int FASTA_CHAR_PER_LINE=80;

   private String startColor = "";
   private String stopColor = "";
   private String frame1Color = "";
   private String frame2Color = "";
   private String frame3Color = "";
   private String highlightColor = "";
   private String oddAlternateColorName = "";
   private String evenAlternateColorName = "";
   
   private TranscriptTranslateTranslationMenu tlMenu;
   private TranscriptTranslateCopyMenu copyMenu;
   private TranscriptTranslateCopyMenu copySeqToBlastMenu;
   private JMenuItem fontMI = new JMenuItem("Font...");
   private JMenuItem editColorsMI = new JMenuItem("Edit Colors");

   private MyMouseHandler mouseHandler = new MyMouseHandler();
   private BrowserModelListenerAdapter browserModelListener = new MyBrowserModelListenerAdapter();
   private MySessionModelListener sessionModelListener = new MySessionModelListener();
   private SelectionVisitor selectionVisitor = new SelectionVisitor();
   private MyAdjustmentListener myAdjustmentListener = new MyAdjustmentListener();
   private MySequenceKeyListener mySequenceKeyListener = new MySequenceKeyListener();
   private MyKeyListener myKeyListener = new MyKeyListener();
   protected MyModifyManagerObserver modifyMgrObserver = new MyModifyManagerObserver();
   private MyModelMgrObserver modelMgrObserver = new MyModelMgrObserver();
   private AlignableGenomicEntityObserverAdapter entityObserver = new MyAlignableGenomicEntityObserver();

   private SessionMgr sessionMgr = SessionMgr.getSessionMgr();

   MutableRange lastFixedRange = new MutableRange();
   MutableRange lastVisibleRange = new MutableRange();

   // Whether or not we're a master- or sub-editor.
   private boolean isMaster;
   private boolean selectionEnabled = true;
   /** Create a new view, painted on the given JPanel. */
   private Color startFill, stopFill;
   private Color snpColor = Color.yellow;
   private Color oddAlternateColor;
   private Color evenAlternateColor;
   private Vector frameOutlineColor = new Vector();

   /**
    * TextFields for displaying;
    * rangeLabel - the range of the current selection
    * lengthLabel - the length of the current selection
    * nucleLabel - the nucleotide coordinate of the mouse positon
    * aminoLabel - the amino acid coordinate of the mouse positon
    */
   private JLabel rangeLabel = new JLabel(" "), lengthLabel=new JLabel(" "), nucleLabel = new JLabel(" "), aminoLabel=new JLabel(" "), exonOrderLabel=new JLabel(" ");
   private MutableRange translatedRegion;
   private MutableRange currentORF;   //used to handle curation edits
   private ModifyManager modifyMgr = ModifyManager.getModifyMgr();

   private java.util.List transOneCodonAdornmentList=new ArrayList();
   private java.util.List transTwoCodonAdornmentList=new ArrayList();
   private java.util.List transThreeCodonAdornmentList=new ArrayList();

   // From Editor
   private Browser browser;
   private BrowserModel browserModel;
   private TranscriptTranslateCurationHandler curHandler;

   // Menu items I create & control
   private JMenuItem  findMI = new JMenuItem("Find...", 'F');
   boolean isEditable = false;
   private boolean disposing = false;

   private SequenceSearchDialog sequenceSearchDialog = null;
   private MySequenceSearchListener findListener = new MySequenceSearchListener();

   private JMenu saveToFastaMenu=new JMenu("Save As FASTA");
   private JMenuItem fastaNucMI= new JMenuItem("Nucleotide Sequence");

   private JMenu fastaProteinMenu= new JMenu("Amino Acid Sequence");

   private JMenuItem fastaProteinFrameOneMI=new JMenuItem("+1 ORF");
   private JMenuItem fastaProteinFrameTwoMI=new JMenuItem("+2 ORF");
   private JMenuItem fastaProteinFrameThreeMI=new JMenuItem("+3 ORF");

   private JDialog fastaDeflineDialog = new JDialog();

   private EntityTypeSet entityTypes=new EntityTypeSet(new EntityType[]{
                                                          EntityType.getEntityTypeForValue(EntityTypeConstants.FgenesH),
                                                          EntityType.getEntityTypeForValue(EntityTypeConstants.Otto),
                                                          EntityType.getEntityTypeForValue(EntityTypeConstants.Genscan_Feature),
                                                          EntityType.getEntityTypeForValue(EntityTypeConstants.NonPublic_Transcript),
                                                          EntityType.getEntityTypeForValue(EntityTypeConstants.GRAIL),
                                                          EntityType.getEntityTypeForValue(EntityTypeConstants.Exon)});


   /**
    * The constructor...
    * Creates the seqViewer and sets it up.
    */
   public TranscriptTranslateView (Browser browser, Boolean isMaster) {
      // Keep track if we are the master...
      this.isMaster = isMaster.booleanValue();
      this.browser = browser;
      this.browserModel = browser.getBrowserModel();
      curHandler = new TranscriptTranslateCurationHandler(browser, this, seqViewer);
      isEditable = !( browserModel.getMasterEditorEntity().getGenomeVersion().isReadOnly() );
      masterAxis = (GenomicAxis)browserModel.getMasterEditorEntity();

      startColor = (String)sessionMgr.getModelProperty("TransTransStartColor");
      stopColor = (String)sessionMgr.getModelProperty("TransTransStopColor");
      frame1Color = (String)sessionMgr.getModelProperty("TransTransFrame1Color");
      frame2Color = (String)sessionMgr.getModelProperty("TransTransFrame2Color");
      frame3Color = (String)sessionMgr.getModelProperty("TransTransFrame3Color");
      highlightColor = (String)sessionMgr.getModelProperty(TransTransPanel.HIGHLIGHT_PROP);
      oddAlternateColorName = (String)sessionMgr.getModelProperty(TransTransPanel.ODD_ALTERNATING_EXON_COLOR_PROP);
      evenAlternateColorName = (String)sessionMgr.getModelProperty(TransTransPanel.EVEN_ALTERNATING_EXON_COLOR_PROP);
      
      if ( startColor==null ) startColor = "Green";
      if ( stopColor==null ) stopColor = "Red";
      if ( frame1Color==null ) frame1Color = "White";
      if ( frame2Color==null ) frame2Color = "Magenta";
      if ( frame3Color==null ) frame3Color = "Cyan";
      if ( highlightColor==null ) highlightColor = "Blue";
      if ( oddAlternateColorName==null) oddAlternateColorName="Yellow";
      if (evenAlternateColorName==null) evenAlternateColorName="Green";
      
      startFill = ViewPrefMgr.getViewPrefMgr().getColor(startColor);
      stopFill = ViewPrefMgr.getViewPrefMgr().getColor(stopColor);
      frameOutlineColor.add(ViewPrefMgr.getViewPrefMgr().getColor(frame1Color));
      frameOutlineColor.add(ViewPrefMgr.getViewPrefMgr().getColor(frame2Color));
      frameOutlineColor.add(ViewPrefMgr.getViewPrefMgr().getColor(frame3Color));
      oddAlternateColor = ViewPrefMgr.getViewPrefMgr().getColor(oddAlternateColorName);
      evenAlternateColor = ViewPrefMgr.getViewPrefMgr().getColor(evenAlternateColorName);
      
      seqViewer.setSelectionBackground (ViewPrefMgr.getViewPrefMgr().getColor(highlightColor));
      if ( sessionMgr.getModelProperty(FONT_SETTINGS)!=null )
         seqViewer.setFont((Font)sessionMgr.getModelProperty(FONT_SETTINGS));
      fontMI.addActionListener(this);

      // Set up the panel...
      this.setLayout(new BorderLayout(3, 3));
      this.add(seqViewer, BorderLayout.CENTER);

      // Create a bottom panel to hold the informational text fields...
      JPanel bottomPanel = new JPanel();
      bottomPanel.setLayout(new BoxLayout(bottomPanel,BoxLayout.X_AXIS));

      bottomPanel.add(Box.createHorizontalStrut(10));

      JPanel panel1 = new JPanel();
      panel1.setLayout(new BoxLayout(panel1,BoxLayout.Y_AXIS));
      panel1.add(new JLabel("Nucleotide Coordinate:"));
      panel1.add(new JLabel("Selected Range:"));
      panel1.add(new JLabel("Exon Order Number:"));
      bottomPanel.add(panel1);

      bottomPanel.add(Box.createHorizontalStrut(5));

      nucleLabel = new JLabel();
      rangeLabel = new JLabel();
      exonOrderLabel=new JLabel();
      JPanel panel2 = new JPanel();
      panel2.setLayout(new BoxLayout(panel2,BoxLayout.Y_AXIS));
      panel2.add(nucleLabel);
      panel2.add(rangeLabel);
      panel2.add(exonOrderLabel);
      panel2.add(Box.createHorizontalStrut(15));
      bottomPanel.add(panel2);
      bottomPanel.add(Box.createHorizontalStrut(10));

      JPanel panel3 = new JPanel();
      panel3.setLayout(new BoxLayout(panel3,BoxLayout.Y_AXIS));
      panel3.add(Box.createVerticalGlue());
      panel3.add(new JLabel("Amino acid Coordinate:"));
      panel3.add(new JLabel("Selected Amino Acid Length:"));
      bottomPanel.add(panel3);

      bottomPanel.add(Box.createHorizontalStrut(5));

      aminoLabel = new JLabel();
      lengthLabel = new JLabel(" ");
      JPanel panel4 = new JPanel();
      panel4.setLayout(new BoxLayout(panel4,BoxLayout.Y_AXIS));
      panel4.add(Box.createVerticalGlue());
      panel4.add(aminoLabel);
      panel4.add(lengthLabel);
      bottomPanel.add(panel4);
      bottomPanel.add(Box.createHorizontalStrut(10));
      this.add(bottomPanel, BorderLayout.SOUTH);

      browserModel = browser.getBrowserModel();
      masterAxis = (GenomicAxis)browserModel.getMasterEditorEntity();
      masterAxis.addGenomicEntityObserver(entityObserver, false);

      // Add the menu to the browser.
      // Create the menus passing in the action listener...
      editColorsMI = new JMenuItem("Edit SubView Settings...");
      tlMenu    = new TranscriptTranslateTranslationMenu(this);
      tlMenu.clickDefaults();
      copyMenu  = new TranscriptTranslateCopyMenu("Copy To Clipboard",this);
      copySeqToBlastMenu=new TranscriptTranslateCopyMenu("Copy Transcript Sequence To Sequence Analysis",this);
      // And link the menus & view.
      editColorsMI.addActionListener(this);
      findMI.addActionListener(this);
      findMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK, false));

      fastaNucMI.addActionListener(this);

      fastaProteinFrameOneMI.addActionListener(this);
      fastaProteinFrameTwoMI.addActionListener(this);
      fastaProteinFrameThreeMI.addActionListener(this);
      fastaProteinMenu.add(fastaProteinFrameOneMI);
      fastaProteinMenu.add(fastaProteinFrameTwoMI);
      fastaProteinMenu.add(fastaProteinFrameThreeMI);

      saveToFastaMenu.add(fastaNucMI);
      saveToFastaMenu.add(fastaProteinMenu);

      this.setName ("Transcript Translation");

      this.registerKeyboardAction(this,
                                  KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK, false),
                                  JComponent.WHEN_IN_FOCUSED_WINDOW);
      sequenceSearchDialog = new SequenceSearchDialog(browser,"Find in Transcript Translation View", false);
   }


   // Only the browserModel methods should try to do any mirror'ing of ranges due to
   // reverse complement of the view.  Always pass what the browserModel has and then make
   // a determination accordingly.
   public void activate() {
      //System.out.println("Activating TT");
      // The intent with this registry is that we refresh the view after every
      // command finish
      ModifyManager.getModifyMgr().addObserver(modifyMgrObserver);
      ModelMgr.getModelMgr().addModelMgrObserver(modelMgrObserver);
      sessionMgr.addSessionModelListener(sessionModelListener);
      browserModel.addBrowserModelListener(browserModelListener, false);
      seqViewer.addKeyListener(myKeyListener);
      seqViewer.addSequenceKeyListener(mySequenceKeyListener);
      seqViewer.addSequenceAdjustmentListener(myAdjustmentListener);
      seqViewer.addSequenceMouseListener(mouseHandler);
      seqViewer.addSequenceSelectionListener( new MySequenceSelectionListener());
      seqViewer.requestFocus();
      sequenceSearchDialog.addSequenceSearchListener(findListener);
      GenomicEntity masterEntity = browserModel.getMasterEditorEntity();
      if ( browserModel.getSubViewFixedRange()==null ||
           browserModel.getSubViewFixedRange().getMagnitude()==0 ) return;
      if ( !masterAxis.equals(masterEntity) ) {
         browserModelListener.browserMasterEditorEntityChanged(masterEntity);
      }
      MutableRange tmpRange = browserModel.getSubViewFixedRange().toMutableRange();
      if ( tmpRange!=null && !lastFixedRange.equals(tmpRange) )
         browserModelListener.browserSubViewFixedRangeChanged(tmpRange);
      if ( lastVisibleRange!=null && lastVisibleRange.getMagnitude()!=0 )
         browserModel.setSubViewVisibleRange(lastVisibleRange);
      browserModelListener.browserCurrentSelectionChanged(browserModel.getCurrentSelection());
   }

   public void passivate() {
      //System.out.println("Passivating TT");
      if ( disposing ) {
         return;
      }
      ModifyManager.getModifyMgr().removeObserver(modifyMgrObserver);
      ModelMgr.getModelMgr().removeModelMgrObserver(modelMgrObserver);
      browserModel.removeBrowserModelListener(browserModelListener);
      sessionMgr.removeSessionModelListener(sessionModelListener);
      seqViewer.removeKeyListener(myKeyListener);
      seqViewer.removeSequenceKeyListener(mySequenceKeyListener);
      seqViewer.removeSequenceAdjustmentListener(myAdjustmentListener);
      seqViewer.removeSequenceMouseListener(mouseHandler);
      sequenceSearchDialog.removeSequenceSearchListener(findListener);
      Collection observedEntities = entityObserver.getCurrentObservables();
      for ( Iterator itr = observedEntities.iterator(); itr.hasNext(); ) {
         GenomicEntity observedEntity = (GenomicEntity)itr.next();
         if ( observedEntity != null )
            observedEntity.removeGenomicEntityObserver(entityObserver);
      }
      browserModel.setSubViewVisibleRange(new MutableRange());
   }


   /**
    * Toggle to either display or not display the translation for the entire transcript
    * including across start and stop codons.
    */
   private void setTranslateEntireTranscript(boolean translateEntireTranscript) {
      resetAdornmentLists();
      for ( Iterator iter=tlMenu.getFrameNumbersForMenuItemsSelected().iterator();iter.hasNext(); ) {
         int frame=((Integer)iter.next()).intValue();
         annotateCodingRegions(getEntireDNAString(), true, frame);
      }
      if ( translateEntireTranscript )
         seqViewer.setTranslatedRange(seqViewer.getWorldBeginLocation(), seqViewer.getWorldEndLocation());
      else seqViewer.setTranslatedRange((long)translatedRegion.getStart(), (long)translatedRegion.getEnd());
   }



   /**
    * handle any cleanup prior to getting rid of a TranscriptTranslateView
    */
   public void dispose() {
      clear();
      // Remove all listeners.
      seqViewer.removeSequenceMouseListener(mouseHandler);

      modifyMgr.removeObserver(modifyMgrObserver);
      // Call dispose operations on contained widgets.
      seqViewer.removeAll();

      disposing = true;

      browserModel.removeBrowserModelListener (browserModelListener);
      masterAxis.removeGenomicEntityObserver(entityObserver);
      seqViewer.removeSequenceKeyListener(mySequenceKeyListener);
      seqViewer.removeSequenceAdjustmentListener(myAdjustmentListener);
      seqViewer.removeKeyListener(myKeyListener);
      sequenceSearchDialog.removeSequenceSearchListener(findListener);
      sessionMgr.removeSessionModelListener(sessionModelListener);
      browserModel = null;
      curHandler.dispose();
      curHandler = null;
      sessionMgr.setModelProperty(FONT_SETTINGS,seqViewer.getFont());
      this.fastaDeflineDialog.dispose();
   }


   /**
    * If a entity changed that is related to the view then redraw the view
    */
   void updateViewIfNecessary(Feature feature, boolean detach) {
      if ( feature == null ) return;
      Feature viewFeature = getDisplayedFeature();
      if ( viewFeature == null ) return;

      if ( (feature.getRootFeature() == viewFeature.getRootFeature()) || detach ) {
         clear();
         viewFeature.acceptVisitorForSelf(selectionVisitor);
      }
   }


   private int frameToSeqFrame(int frame) {
      switch ( frame ) {
         case 1: return (seqViewer.ORF_1_DISPLAY);
         case 2: return (seqViewer.ORF_2_DISPLAY);
         case 3: return (seqViewer.ORF_3_DISPLAY);
         case -1: return (seqViewer.ORF_NEG1_DISPLAY);
         case -2: return (seqViewer.ORF_NEG2_DISPLAY);
         case -3: return (seqViewer.ORF_NEG3_DISPLAY);
         default: throw new IllegalStateException("Error: frameToNeoFrame passed bad value=" + frame);
      }
   }

   private int seqFrameToFrame(int frame) {
      if ( frame==seqViewer.ORF_1_DISPLAY ) return (1);
      else if ( frame==seqViewer.ORF_1_DISPLAY ) return (1);
      else if ( frame==seqViewer.ORF_2_DISPLAY ) return (2);
      else if ( frame==seqViewer.ORF_3_DISPLAY ) return (3);
      else if ( frame==seqViewer.ORF_NEG1_DISPLAY ) return (-1);
      else if ( frame==seqViewer.ORF_NEG2_DISPLAY ) return (-2);
      else if ( frame==seqViewer.ORF_NEG3_DISPLAY ) return (-3);
      else throw new IllegalStateException("Error: seqFrameToFrame passed bad value=" + frame);
   }

   /**
    *  show just one frame in seqViewer, turn all others off
    *  Useful for showing just the translation frame, based on start codon / frame info
    */
   private void showOneFrame(int frame_type) {
      tlMenu.transOne.setState(false);
      tlMenu.transTwo.setState(false);
      tlMenu.transThree.setState(false);
      tlMenu.transNegOne.setState(false);
      tlMenu.transNegTwo.setState(false);
      tlMenu.transNegThree.setState(false);

      seqViewer.setSequenceVisible(seqViewer.ORF_1_DISPLAY,     false);
      seqViewer.setSequenceVisible(seqViewer.ORF_2_DISPLAY,     false);
      seqViewer.setSequenceVisible(seqViewer.ORF_3_DISPLAY,     false);
      seqViewer.setSequenceVisible(seqViewer.ORF_NEG1_DISPLAY,  false);
      seqViewer.setSequenceVisible(seqViewer.ORF_NEG2_DISPLAY,  false);
      seqViewer.setSequenceVisible(seqViewer.ORF_NEG3_DISPLAY,  false);

      JCheckBoxMenuItem mi = null;
      if ( frame_type == seqViewer.ORF_1_DISPLAY ) {
         mi = tlMenu.transOne;
      }
      else if ( frame_type == seqViewer.ORF_2_DISPLAY ) {
         mi = tlMenu.transTwo;
      }
      else if ( frame_type == seqViewer.ORF_3_DISPLAY ) {
         mi = tlMenu.transThree;
      }
      else if ( frame_type == seqViewer.ORF_NEG1_DISPLAY ) {
         mi = tlMenu.transNegOne;
      }
      else if ( frame_type == seqViewer.ORF_NEG2_DISPLAY ) {
         mi = tlMenu.transNegTwo;
      }
      else if ( frame_type == seqViewer.ORF_NEG3_DISPLAY ) {
         mi = tlMenu.transNegThree;
      }
      if ( mi != null ) {
         mi.setState(true);
         seqViewer.setTranslatedRange((long)translatedRegion.getStart(), (long)translatedRegion.getEnd());
         seqViewer.setSequenceVisible(frame_type, true);
      }
   }


   public void actionPerformed (ActionEvent e) {
      Object theItem = e.getSource();

      if ( theItem == curHandler.editUndoMI ) {
         // System.out.println("TTV-actionPerformed(); Got an Undo action...");
         // Reset the seqViewer's sequence to that from the entity...
         // Look to currentSelection for the sequence...currentSelection = newSelection;

         // Go ahead and disable the undo... typing (keyReleased) will re-enable it.
         curHandler.editUndoMI.setEnabled(false);

         if ( currentSelection != null ) {
            currentSelection.acceptVisitorForSelf(selectionVisitor);
         }
      }

      // Handle the TranscripLation menu:
      if ( copyMenu != null ) {
         if ( theItem == copyMenu.selectedRegionNT_MI ) {
            String selRes = getSelectedDNAString();
            if ( (selRes == null) || (selRes.length() == 0) ) return;
            ClipboardUtils.setClipboardContents(selRes);
         }
         else if ( theItem == copyMenu.selectedRegionAA1_MI ) {
            String trans = getSelectedTranslationForFrame(Protein.FRAME_PLUS_ONE);
            ClipboardUtils.setClipboardContents(trans);
         }
         else if ( theItem == copyMenu.selectedRegionAA2_MI ) {
            String trans = getSelectedTranslationForFrame(Protein.FRAME_PLUS_TWO);
            ClipboardUtils.setClipboardContents(trans);
         }
         else if ( theItem == copyMenu.selectedRegionAA3_MI ) {
            String trans = getSelectedTranslationForFrame(Protein.FRAME_PLUS_THREE);
            ClipboardUtils.setClipboardContents(trans);
         }
         else if ( theItem == copyMenu.entireTranscriptNT_MI ) {
            String allRes = getEntireDNAString();
            ClipboardUtils.setClipboardContents(allRes);
         }
         else if ( theItem == copyMenu.entireTranscriptAA1_MI ) {
            String entireTranscriptAA1s = getEntireTranslationForFrame(Protein.FRAME_PLUS_ONE);
            ClipboardUtils.setClipboardContents(entireTranscriptAA1s);
         }
         else if ( theItem == copyMenu.entireTranscriptAA2_MI ) {
            String entireTranscriptAA2s = getEntireTranslationForFrame(Protein.FRAME_PLUS_TWO);
            ClipboardUtils.setClipboardContents(entireTranscriptAA2s);
         }
         else if ( theItem == copyMenu.entireTranscriptAA3_MI ) {
            String entireTranscriptAA3s = getEntireTranslationForFrame(Protein.FRAME_PLUS_THREE);
            ClipboardUtils.setClipboardContents(entireTranscriptAA3s);
         }
         else if ( theItem == copyMenu.transcriptORFAA_MI ) {
            String allRes = getEntireDNAString();
            if ( (allRes == null) || (allRes.length() == 0) ) return;
            String initSeqString = allRes.substring (translatedRegion.getStart(), translatedRegion.getEnd()+1);
            String translatedAAs = Protein.toString(Protein.convertDNASequence(DNASequenceStorage.create(initSeqString, "")));

            //don't copy the trailing * to the clipboard
            if ( translatedAAs.endsWith("*") )
               ClipboardUtils.setClipboardContents(translatedAAs.substring(0, translatedAAs.length()-1));
            else
               ClipboardUtils.setClipboardContents(translatedAAs);
         }
         else if ( theItem == copyMenu.transcriptORFNT_MI ) {
            String allRes = getEntireDNAString();
            if ( (allRes == null) || (allRes.length() == 0) ) return;
            String orfRes = allRes.substring (translatedRegion.getStart(), translatedRegion.getEnd()+1);
            ClipboardUtils.setClipboardContents(orfRes);
         }
      }


      if ( copySeqToBlastMenu!=null ) {
         if ( theItem == copySeqToBlastMenu.selectedRegionNT_MI ) {
            String selRes = getSelectedDNAString();
            if ( (selRes == null) || (selRes.length() == 0) ) return;
            sendBlastParameters(selRes, false);
         }
         else if ( theItem == copySeqToBlastMenu.selectedRegionAA1_MI ) {
            String selectedAA1s = getSelectedTranslationForFrame(Protein.FRAME_PLUS_ONE);
            sendBlastParameters(selectedAA1s, true);
         }
         else if ( theItem == copySeqToBlastMenu.selectedRegionAA2_MI ) {
            String selectedAA2s = getSelectedTranslationForFrame(Protein.FRAME_PLUS_TWO);
            sendBlastParameters(selectedAA2s, true);
         }
         else if ( theItem == copySeqToBlastMenu.selectedRegionAA3_MI ) {
            String selectedAA3s = getSelectedTranslationForFrame(Protein.FRAME_PLUS_THREE);
            sendBlastParameters(selectedAA3s, true);
         }
         else if ( theItem == copySeqToBlastMenu.entireTranscriptNT_MI ) {
            String allRes = getEntireDNAString();
            if ( (allRes == null) || (allRes.length() == 0) ) return;
            sendBlastParameters(allRes, false);
         }
         else if ( theItem == copySeqToBlastMenu.entireTranscriptAA1_MI ) {
            String entireTranscriptAA1s = getEntireTranslationForFrame(Protein.FRAME_PLUS_ONE);
            sendBlastParameters(entireTranscriptAA1s, true);
         }
         else if ( theItem == copySeqToBlastMenu.entireTranscriptAA2_MI ) {
            String entireTranscriptAA2s = getEntireTranslationForFrame(Protein.FRAME_PLUS_TWO);
            sendBlastParameters(entireTranscriptAA2s, true);

         }
         else if ( theItem == copySeqToBlastMenu.entireTranscriptAA3_MI ) {
            String entireTranscriptAA3s = getEntireTranslationForFrame(Protein.FRAME_PLUS_THREE);
            sendBlastParameters(entireTranscriptAA3s, true);
         }
         else if ( theItem == copySeqToBlastMenu.transcriptORFNT_MI ) {
            String allRes = getEntireDNAString();
            if ( (allRes == null) || (allRes.length() == 0) ) return;
            String orfRes = allRes.substring (translatedRegion.getStart(), translatedRegion.getEnd()+1);
            sendBlastParameters(orfRes, false);
         }
         else if ( theItem == copySeqToBlastMenu.transcriptORFAA_MI ) {
            String allRes = getEntireDNAString();
            if ( (allRes == null) || (allRes.length() == 0) ) return;
            String orfRes = allRes.substring (translatedRegion.getStart(), translatedRegion.getEnd()+1);
            String translatedAAs = Protein.toString(Protein.convertDNASequence(DNASequenceStorage.create(orfRes, "")));
            //don't copy the trailing * to the clipboard
            if ( translatedAAs.endsWith("*") )
               translatedAAs = translatedAAs.substring(0, translatedAAs.length()-1);

            sendBlastParameters(translatedAAs, true);
         }
      }

      if ( tlMenu != null ) {
         if ( theItem == tlMenu.transOne ) {
            JCheckBoxMenuItem jcbmi = (JCheckBoxMenuItem) theItem;
            seqViewer.setSequenceVisible(seqViewer.ORF_1_DISPLAY, jcbmi.getState());
         }
         else if ( theItem == tlMenu.transTwo ) {
            JCheckBoxMenuItem jcbmi = (JCheckBoxMenuItem) theItem;
            seqViewer.setSequenceVisible(seqViewer.ORF_2_DISPLAY, jcbmi.getState());
         }
         else if ( theItem == tlMenu.transThree ) {
            JCheckBoxMenuItem jcbmi = (JCheckBoxMenuItem) theItem;
            seqViewer.setSequenceVisible(seqViewer.ORF_3_DISPLAY, jcbmi.getState());
         }
         else if ( theItem == tlMenu.transNegOne ) {
            JCheckBoxMenuItem jcbmi = (JCheckBoxMenuItem) theItem;
            seqViewer.setSequenceVisible(seqViewer.ORF_NEG1_DISPLAY, jcbmi.getState());
         }
         else if ( theItem == tlMenu.transNegTwo ) {
            JCheckBoxMenuItem jcbmi = (JCheckBoxMenuItem) theItem;
            seqViewer.setSequenceVisible(seqViewer.ORF_NEG2_DISPLAY, jcbmi.getState());
         }
         else if ( theItem == tlMenu.transNegThree ) {
            JCheckBoxMenuItem jcbmi = (JCheckBoxMenuItem) theItem;
            seqViewer.setSequenceVisible(seqViewer.ORF_NEG3_DISPLAY, jcbmi.getState());
         }
         else if ( theItem == tlMenu.noComp ) {
            seqViewer.setSequenceVisible(seqViewer.COMPLEMENT_DNA_DISPLAY, false);
         }
         else if ( theItem == tlMenu.revComp ) {
            seqViewer.setSequenceVisible(seqViewer.COMPLEMENT_DNA_DISPLAY, true);
         }
         else if ( theItem == tlMenu.oneLetter ) {
            seqViewer.setTranslationStyle(seqViewer.SINGLE_CHAR_TRANSLATION);
         }
         else if ( theItem == tlMenu.threeLetter ) {
            seqViewer.setTranslationStyle(seqViewer.ABBREVIATED_TRANSLATION);
         }
         else if ( theItem == tlMenu.translateEntireTranscriptMI ) {
            setTranslateEntireTranscript(tlMenu.translateEntireTranscriptMI.getState());
         }
         else if ( theItem == tlMenu.transOneToggleStartStopHighlightMI ) {
            annotateCodingRegions(getEntireDNAString(),tlMenu.transOneToggleStartStopHighlightMI.getState(),1);
         }
         else if ( theItem == tlMenu.transTwoToggleStartStopHighlightMI ) {
            annotateCodingRegions(getEntireDNAString(),tlMenu.transTwoToggleStartStopHighlightMI.getState(),2);
         }
         else if ( theItem == tlMenu.transThreeToggleStartStopHighlightMI ) {
            annotateCodingRegions(getEntireDNAString(),tlMenu.transThreeToggleStartStopHighlightMI.getState(),3);
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

      if ( theItem == fastaNucMI ) {
         printFastaFile(false, 0);// dummy frame
      }

      if ( theItem == fastaProteinFrameOneMI ) {
         printFastaFile(true, 1);
      }
      if ( theItem == fastaProteinFrameTwoMI ) {
         printFastaFile(true, 2);
      }

      if ( theItem == fastaProteinFrameThreeMI ) {
         printFastaFile(true, 3);
      }

      if ( theItem == editColorsMI ) {
         PrefController.getPrefController().getPrefInterface(client.gui.other.panels.TransTransPanel.class, browser);
      }

      if ( theItem == fontMI ) {
         // Start the chooser and init with current.
         Font oldFont = seqViewer.getFont();
         FontChooser chooser = new FontChooser();
         Font newFont = chooser.showDialog(browser, "Transcript Translation Font", oldFont);
         if ( newFont == null ) return;
         if ( !newFont.equals(oldFont) ) {
            seqViewer.setFont(newFont);
            sessionMgr.setModelProperty(FONT_SETTINGS, newFont);
         }
      }
   }



   private void printFastaFile(boolean isAminoAcidStr, int frame){

      String str=null;
      CuratedTranscript t=null;
      if ( currentSelection instanceof CuratedTranscript ) {
         t=(CuratedTranscript)currentSelection;
      }
      else if ( currentSelection instanceof CuratedExon ) {
         t=(CuratedTranscript)((CuratedExon)currentSelection).getSuperFeature();
      }
      if ( isAminoAcidStr ) {

         int startPos=t.getTranslationStart();
         int stopPos=0;
         Sequence s=null;
         Sequence baseSequence = seqViewer.getSequenceAt(GenomicSequenceViewer.DNA_DISPLAY);
         if ( t.hasStopCodon() ) {
            stopPos= t.getTranslationStop();
            s=new SubSequence(baseSequence,(long)startPos,(stopPos-startPos)+3);
         }
         else {
            s=new SubSequence(baseSequence,(long)startPos,baseSequence.length());
         }

         str=SequenceHelper.toString(Protein.convertDNASequenceToProteinORF(s, frame));

      }
      else {
         Sequence s=t.getSplicedResidues();
         str=DNA.toString(s);

      }


      String defline=buildDefline(t, str);

      //show the default defline to the user
      fastaDeflineDialog=new JDialog(browser,"Default Fasta Defline", true);
      fastaDeflineDialog.addWindowListener(new MyWindowListener());

      JTextArea deflinetext=new JTextArea();
      deflinetext.setText(defline);

      JScrollPane scrollPane = new JScrollPane(deflinetext);
      fastaDeflineDialog.getContentPane().setLayout(new BorderLayout());
      fastaDeflineDialog.getContentPane().add(scrollPane, BorderLayout.CENTER);
      fastaDeflineDialog.setSize(500, 100);
      fastaDeflineDialog.setLocationRelativeTo(browser);
      fastaDeflineDialog.show();
      FastaObject f=new FastaObject(str,defline);
      FastaWriter.getFastaWriter().printFastaFile(f);

   }



   private class MyWindowListener extends WindowAdapter {
      public void windowClosing(WindowEvent e) {
         if ( fastaDeflineDialog !=null ) {
            fastaDeflineDialog.hide();
         }
      }
   }



   private String buildDefline(CuratedTranscript t, String str){
      String defline =">";
      //id, accession
      GenomicProperty accgp =t.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP);
      String trscptAccno="";
      if ( accgp!=null ) {
         trscptAccno=accgp.getInitialValue();
      }
      defline=defline+"/id="+trscptAccno;

      //org

      GenomicEntity root=t.getGenomeVersion().getSpecies();
      defline=defline+"/org="+root.toString();

      // description
      defline =defline+"/description=";


      //evidence
      String evidences="";
      for ( Iterator iter=t.getSubFeatures().iterator();iter.hasNext(); ) {

         Feature fp = ((Feature)(iter.next()));
         for ( Iterator iter2=fp.getEvidenceOids().iterator();iter2.hasNext(); ) {
            String oid=((OID)iter2.next()).getIdentifierAsString();
            evidences=evidences+oid+",";
         }
      }
      if ( evidences!=null ) {
         defline=defline+"/evidence="+evidences.substring(0,evidences.length()-1);
      }
      //length
      defline=defline+"/length="+str.length();

      //ga_name
      String ganame= GANumericConverter.getConverter().getGANameForOIDSuffix(masterAxis.getOid().getIdentifierAsString()) ;
      defline =defline +"/ga_name="+ganame;

      //ga_uid
      String gauid=masterAxis.getOid().getIdentifierAsString();
      defline=defline+"/ga_uid="+gauid;

      //alignment- collection of exon alignments
      String alignment="";
      for ( Iterator iter=t.getSubFeatures().iterator();iter.hasNext(); ) {
         GeometricAlignment ga =(GeometricAlignment)((CuratedFeature)iter.next()).getOnlyGeometricAlignmentToOnlyAxis();
         int start=ga.getRangeOnAxis().getStart();
         int end =ga.getRangeOnAxis().getEnd();
         alignment=alignment+start+"-"+end+";";

      }
      defline=defline+"/alignment=("+alignment.substring(0,alignment.length()-1)+")";
      return (defline);
   }


   private String getSelectedTranslationForFrame(int frame) {
      long start = seqViewer.getSelectionBegin();
      long end = seqViewer.getSelectionEnd()+1;
      long worldBegin = seqViewer.getWorldBeginLocation();
      Sequence baseSequence = new SubSequence(seqViewer.getSequenceAt(GenomicSequenceViewer.DNA_DISPLAY),start-worldBegin, end-start);
      return (SequenceHelper.toString(Protein.convertDNASequenceToProteinORF(baseSequence, frame)));
   }


   private String getEntireTranslationForFrame(int frame) {
      Sequence baseSequence = seqViewer.getSequenceAt(GenomicSequenceViewer.DNA_DISPLAY);
      return (SequenceHelper.toString(Protein.convertDNASequenceToProteinORF(baseSequence, frame)));
   }


   public boolean canEditThisEntity(GenomicEntity entity) {
      return (entity != null && entityTypes.contains(entity.getEntityType()));
   }


   /**
    * Provide access to my menus...
    * ...this is part of the Editor interface responsibilities that I implement.
    * My menu is both what I created, and what was created by curHandler.
    */
   public JMenuItem[] getMenus() {
      JMenuItem[] curation_menus = curHandler.getMenus();

      ArrayList menus=new ArrayList();

      menus.add(tlMenu);
      menus.add(findMI);
      if ( isEditable ) {
         for ( int i=0; i<curation_menus.length; i++ ) {
            menus.add(curation_menus[i]);
         }
      }
      menus.add(editColorsMI);
      menus.add(fontMI);
      menus.add(copyMenu);
      menus.add(copySeqToBlastMenu);
      CuratedTranscript t=null;
      if ( currentSelection !=null && currentSelection instanceof CuratedTranscript ) {
         t=(CuratedTranscript)currentSelection;
         if ( t.hasStartCodon() ) {
            fastaProteinMenu.setEnabled(true);
         }
         else {
            fastaProteinMenu.setEnabled(false);
         }
      }
      else if ( currentSelection !=null && currentSelection instanceof CuratedExon ) {
         t=(CuratedTranscript)((CuratedExon)currentSelection).getSuperFeature();
         if ( t.hasStartCodon() ) {
            fastaProteinMenu.setEnabled(true);
         }
         else {
            fastaProteinMenu.setEnabled(false);
         }

      }

      saveToFastaMenu.setEnabled(true);
      menus.add(saveToFastaMenu);

      JMenuItem[] menuArray=new JMenuItem[]{};
      return((JMenuItem[])menus.toArray(menuArray));
   }


   public void writeObject(ObjectOutputStream out) {
      System.out.println("Someone is trying to serialize the TT view.");
   }

   private String getSelectedDNAString() {
      long start = seqViewer.getSelectionBegin();
      long end = seqViewer.getSelectionEnd()+1;
      long worldBegin = seqViewer.getWorldBeginLocation();
      Sequence tmpSequence = seqViewer.getSequenceAt(GenomicSequenceViewer.DNA_DISPLAY);
      return (SequenceHelper.toString(tmpSequence).substring((int)(start-worldBegin), (int)(end-worldBegin)));
   }


   private String getEntireDNAString() {
      try {
         Sequence tmpSequence = seqViewer.getSequenceAt(GenomicSequenceViewer.DNA_DISPLAY);
         return (SequenceHelper.toString(tmpSequence));
      }
      catch ( Exception ex ) {
         return ("");
      }
   }


   /**
    * Annotate the start and stop codons.
    */
   private void annotateCodingRegions(String residues, boolean toggleStartStopHighlight, int frame) {
      if ( toggleStartStopHighlight ) {
         annotateSubseq(residues, "ATG", startFill,frame);
         annotateSubseq(residues, "TAA", stopFill,frame);
         annotateSubseq(residues, "TGA", stopFill,frame);
         annotateSubseq(residues, "TAG", stopFill,frame);
      }
      else {
         if ( frame==1 ) {
            for ( Iterator i=transOneCodonAdornmentList.iterator();i.hasNext(); ) {
               seqViewer.removeAdornment((Adornment)(i.next()));
            }
            transOneCodonAdornmentList.clear();
         }
         else if ( frame==2 ) {
            for ( Iterator i=transTwoCodonAdornmentList.iterator();i.hasNext(); ) {
               seqViewer.removeAdornment((Adornment)(i.next()));
            }
            transTwoCodonAdornmentList.clear();
         }
         else if ( frame==3 ) {
            for ( Iterator i=transThreeCodonAdornmentList.iterator();i.hasNext(); ) {
               seqViewer.removeAdornment((Adornment)(i.next()));
            }
            transThreeCodonAdornmentList.clear();
         }
      }
   }


   /**
    * Given a string to be handed off to interactive blast, build a parameters
    * object from it, and send it by setting a model property.
    *
    * @param String messageString the DNA or AA residues to send.
    */
   private void sendBlastParameters(String messageString, boolean isProteinSequence) {
      // Create a model property.
      GeometricAlignment ga=currentFeature.getOnlyGeometricAlignmentToAnAxis(masterAxis);
      Sequence blastSequence;
      if ( isProteinSequence ) blastSequence = new ProteinSequenceStorage(messageString, "");
      else blastSequence = DNASequenceStorage.create(messageString, "");
      
      // Use the Main View Yellow Box range as the query sequence.
      Range qRange = browserModel.getSubViewFixedRange();
      Sequence querySequence = masterAxis.getNucleotideSeq(qRange);
      SequenceAnalysisQueryParameters ibp = new SequenceAnalysisQueryParameters(ga.getRangeOnAxis(),
                                                                                masterAxis,
                                                                                blastSequence,
                                                                                DNASequenceStorage.create(querySequence));
      browserModel.setModelProperty(SequenceAnalysisQueryParameters.PARAMETERS_PROPERTY_KEY,ibp);
      SequenceAnalysisDialog.getSequenceAnalysisDialog().showSearchDialog();
   } // End method

   /**
   * Annotate the snps.
   */
//   private void annotateSnpRegions(String residues) {
   // annotateSubseq(residues, "A", startFill);
   // annotateSubseq(residues, "T", stopFill);
   // annotateSnpInSubseq(residues, "G", snpColor);
   // annotateSnpInSubseq(residues, "C", snpColor);
//   }

   /**
   * Will highlight in the residues all occurences of the search sequnece.
   */
//    private void annotateSnpInSubseq(String residues, String searchseq, Color highlightColor) {
//      if (residues == null) return;
//       String res = residues.toUpperCase();
//       String searchStr = searchseq.toUpperCase();
//
//       int ind=0;
//       int i=0;
//       int frame = 0;
//
//       while (i < res.length()) {
//         ind = res.indexOf(searchStr, i);
//
//         if (ind >=0) {
//           frame = ind%3+1;
//           // passing null for snp type for the time being
//           ((seqViewerSnp)seqViewer).addAnnotationForSnp(ind, ind+2, highlightColor,null);
//            i = ind + 1;
//         }
//         else break;
//       }
//
//    }


   /**
    * Will highlight in the residues all occurences of the search sequnece.
    */
   private void annotateSubseq(String residues, String searchseq, Color highlightColor,int f) {
      if ( residues == null ) return;
      String res = residues.toUpperCase();
      String searchStr = searchseq.toUpperCase();
      //search for starts
      int ind=0;
      int i=0;
      int frame = 0;

      while ( i < res.length() ) {
         ind = res.indexOf(searchStr, i);

         if ( ind >=0 ) {

            frame = ind%3 + 1;
            if ( frame==f ) {
               if ( frame==1 ) {
                  Adornment tmpCodonAdornment = new Adornment(ind, ind+2, highlightColor, (Color)frameOutlineColor.get(frame-1));
                  seqViewer.addAdornment(tmpCodonAdornment);
                  transOneCodonAdornmentList.add(tmpCodonAdornment);
               }
               else if ( frame==2 ) {
                  Adornment tmpCodonAdornment = new Adornment(ind, ind+2, highlightColor, (Color)frameOutlineColor.get(frame-1));
                  seqViewer.addAdornment(tmpCodonAdornment);
                  transTwoCodonAdornmentList.add(tmpCodonAdornment);
               }
               else if ( frame==3 ) {
                  Adornment tmpCodonAdornment = new Adornment(ind, ind+2, highlightColor, (Color)frameOutlineColor.get(frame-1));
                  seqViewer.addAdornment(tmpCodonAdornment);
                  transThreeCodonAdornmentList.add(tmpCodonAdornment);
               }
            }
            i = ind + 1;
         }
         else break;
      }
   }

   /**
    * Load the sequence for a composite feature. The simpleFeature is passed to the sequence observer so that
    * the view can scroll to it when the sequence is loaded.
    */
   private void loadSequence(Feature compositeFeature, Feature simpleFeature) {

      if ( compositeFeature == null ) return;
      if ( masterAxis == null ) return;

      if ( !(compositeFeature instanceof SingleAlignmentSingleAxis) ) {
         // todo: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s).
         System.out.println("TranscriptTranslateView: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s)");
         return;
      }

      GeometricAlignment range = ((SingleAlignmentSingleAxis)compositeFeature).getOnlyGeometricAlignmentToOnlyAxis();
      if ( range == null ) {
         System.err.println("Error: either no or multiple alignments found for feature=" + compositeFeature);
         return;
      }

      MutableRange featureRange = new MutableRange(range.getRangeOnAxis());
      masterAxis.loadNucleotideSeq(featureRange,new MySequenceObserver(compositeFeature,simpleFeature));
   }


   /**
    * Get the axis alignment for the specified feature.
    * Warning: this method assumes that the feature has only one alignment to the master axis.
    */
   private MutableRange getAxisAlignmentForEntity(CuratedFeature entity) {
      if ( entity==null ) return (null);
      MutableRange mr = null;

      try {
         GeometricAlignment entityga=entity.getOnlyGeometricAlignmentToOnlyAxis();
         mr = new MutableRange(entityga.getRangeOnAxis());
      }
      catch ( IllegalStateException ex ) {
         try {
            sessionMgr.handleException(ex);
         }
         catch ( Exception e ) {
            ex.printStackTrace();
         }
      }
      return (mr);
   }


   /**
    * Given a TranscriptGenomicEntity, distill and display a sequence.
    */
   private void displayFeature(Axis axis, Feature compositeFeature, Feature simpleFeature, Sequence sequence) {
      if ( (sequence == null) || (compositeFeature == null) ) return;

      // Pre condition save-back, for possible restoration of locational
      // state after operation.
      Range oldRange = browserModel.getSubViewVisibleRange();
      Feature oldDisplayedFeature = getDisplayedFeature();

      String spliceRes = null;

      setDisplayedFeature(compositeFeature);

      if ( compositeFeature.getSplicedResidues()==null ) {
         return;
      }

      spliceRes = DNA.toString(compositeFeature.getSplicedResidues());

      if ( spliceRes.length()==0 ) return;
      seqViewer.clearSelection();
      seqViewer.clearAllAdornments();
      seqViewer.setDNASequence(DNASequenceStorage.create(spliceRes, ""), 0);
      Range featureRange;
      if ( compositeFeature instanceof CuratedFeature ) {
         featureRange = getAxisAlignmentForEntity((CuratedFeature)compositeFeature);
      }
      else {
         featureRange = compositeFeature.getOnlyGeometricAlignmentToAnAxis(axis).getRangeOnAxis();//getAxisAlignmentForEntity((CuratedFeature)compositeFeature);
      }

      // If same feature is being displayed as before, do not alter the
      // range.
      MutableRange tempRange = null;
      if ( ! getDisplayedFeature().equals(oldDisplayedFeature) ) {
         tempRange = featureRange.toMutableRange();
         if ( isReverseComplement() )
            tempRange.mirror(axis.getMagnitude());
      }
      else {
         if ( oldRange != null )
            tempRange = new MutableRange(oldRange);
      }
      browserModel.setSubViewVisibleRange(tempRange);

      int startPos = 0;
      if ( compositeFeature instanceof CuratedTranscript ) {
         CuratedTranscript transcript = (CuratedTranscript) compositeFeature;
         startPos = transcript.getTranslationStart();

         curHandler.setDeleteStartFromDatabaseMenuState(false);
         curHandler.setRemoveStartMenuState(false);

         if ( transcript.getStartCodon() != null ) {

            // Enable the menu item for removing start codon from
            // the workspace transcript, if the transcript is not
            // a replacement for a promoted one.
            // Enable the menu item for deleting the start codon from
            // the database, if the transcript DOES replace a promoted one.
            if ( transcript.getStartCodon().isScratchReplacingPromoted() )
               curHandler.setDeleteStartFromDatabaseMenuState(true);
            else
               curHandler.setRemoveStartMenuState(true);
         } // Transcript has a start codon.
      } // Composite feature is a transcript.

      if ( startPos < 0 ) {
         System.err.println("Error: (TranscriptTranslateView(): invalid starPos returned from getTranslationStart using 0!");
         startPos = 0;
      }

      int frame = (startPos%3) + 1;
      int stopPos = SequenceUtil.getSequenceUtil().findFirstInFrameStop(spliceRes, startPos);
      int endPos = (stopPos >=0)? stopPos+2 : spliceRes.length()-1;
      translatedRegion = new MutableRange(startPos, endPos);

      currentORF = new MutableRange(startPos, endPos);
      showOneFrame(frameToSeqFrame(frame));

      //
      // The code below sets the solid backgrounds that alternate with the exons.
      //
      if ( getDisplayedFeature() instanceof Feature ) {
         java.util.SortedSet subfeatureSet = new java.util.TreeSet(new PositionComparator(!featureRange.isReversed()));
         Feature cf = (Feature) getDisplayedFeature();
         Feature feature;
         for ( Iterator iter =cf.getSubFeatures().iterator();iter.hasNext(); ) {
            feature = (Feature) iter.next();
            subfeatureSet.add(feature);
         }
         boolean color_this_one = false;
         Feature subfeature;
         MutableRange subfeatureRange;
         int currentStart = 0;
         int subfeatureEnd;

         try {
            for (Iterator i=subfeatureSet.iterator(); i.hasNext(); ) {
               subfeature = (Feature)i.next();
               subfeatureRange = new MutableRange(subfeature.getOnlyGeometricAlignmentToAnAxis(axis).getRangeOnAxis());
               if (featureRange.isReversed()) subfeatureRange.mirror(featureRange.getMagnitude());
                  subfeatureEnd = currentStart + subfeatureRange.getMagnitude() - 1;
               if (color_this_one) {
                  Adornment a = new Adornment(currentStart, subfeatureEnd,
                     Color.BLACK, oddAlternateColor);
                  a.setSelectedBackground(ViewPrefMgr.getViewPrefMgr().getColor(highlightColor));
                  a.setSelectedForeground(Color.yellow);
                  a.setZOrder(-1);
                  seqViewer.addAdornment(a);
               }
               else {
                  Color  featureColor = ViewPrefMgr.getViewPrefMgr().getColorForEntity(subfeature);
                  Adornment a = new Adornment(currentStart, subfeatureEnd,
                    Color.BLACK, evenAlternateColor != null ? evenAlternateColor : featureColor);
	          a.setSelectedBackground(ViewPrefMgr.getViewPrefMgr().getColor(highlightColor));
                  a.setZOrder(-1);
	          seqViewer.addAdornment(a);
               }
               color_this_one = !color_this_one;
               currentStart += subfeatureRange.getMagnitude();
            }
         } catch (ClassCastException cce) {return; }
      }
      // Establish adornments for start/stop codons.
      for ( Iterator iter=tlMenu.getFrameNumbersForMenuItemsSelected().iterator();iter.hasNext(); ) {
         int f=((Integer)iter.next()).intValue();
         annotateCodingRegions(getEntireDNAString(), true, f);
      }
      //annotateSnpRegions(getEntireDNAString());
   }


   protected Feature getDisplayedFeature() {
      return (currentFeature);
   }

   private boolean isReverseComplement() {
      Boolean isRevComp = (Boolean)browserModel.getModelProperty(BrowserModel.REV_COMP_PROPERTY);
      return (isRevComp != null && isRevComp.booleanValue());
   }

   private void setDisplayedFeature(Feature feature) {
      this.currentFeature = feature;
   }

   private void clear() {
      setDisplayedFeature(null);
      resetAdornmentLists();
      if ( curHandler.editUndoMI!=null ) {
         curHandler.editUndoMI.setEnabled(false);
      }
      seqViewer.clearAll();
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


   private void resetAdornmentLists() {
      transOneCodonAdornmentList=new ArrayList();
      transTwoCodonAdornmentList=new ArrayList();
      transThreeCodonAdornmentList=new ArrayList();
      seqViewer.clearAllAdornments();
   }


   /**
    * This class listens to the SequenceViewer and reports when the user has scrolled
    * to a new visible region.  Reports such to the BrowserModel.
    */
   private class MyAdjustmentListener implements SequenceAdjustmentListener {
      public void adjustmentValueChange(SequenceAdjustmentEvent e) {
         /**
          * @todo This needs to report the proper axis coordinates and not the spliced coordinates.
          */
         long start = e.getVisibleBeginLocation();
         long end   = e.getVisibleEndLocation();
         int axisStart = ((Feature)currentSelection).transformSplicedPositionToAxisPosition((int)start);
         int axisEnd = ((Feature)currentSelection).transformSplicedPositionToAxisPosition((int)end);
         if ( axisStart < 0 || axisEnd < 0 ) return;
         MutableRange tmpRange = new MutableRange((int)axisStart, (int)axisEnd);
         if ( isReverseComplement() )
            tmpRange.mirror(masterAxis.getMagnitude());
         lastVisibleRange = tmpRange;
         browserModel.setSubViewVisibleRange(tmpRange);
      }
   }


   private class MyKeyListener implements KeyListener {
      public void keyTyped(KeyEvent p0) {}

      public void keyPressed(KeyEvent p0) {
         int keyCode = p0.getKeyCode();
         if ( p0.isControlDown() && keyCode==KeyEvent.VK_F ) {

            Sequence tmpSequence = seqViewer.getSequenceAt(GenomicSequenceViewer.DNA_DISPLAY);
            sequenceSearchDialog.showSearchDialog(tmpSequence, (long)0);
         }
      }


      /**
       * As soon as the seq is edited the copy to IB should be disabled.  The user
       * should only be able to Blast (Search) internal data for alignment reasons.
       */
      public void keyReleased(KeyEvent p0) {
         //    int keyCode = p0.getKeyCode();
         //  if ((keyCode != KeyEvent.VK_PAGE_DOWN) && (keyCode != KeyEvent.VK_PAGE_UP))
         //copySeqToSeqAnalysis.setEnabled(false);
      }
   }




   private class MySequenceKeyListener implements SequenceKeyListener {

      /**
       * Get notification of key released on SequenceViewer...
       * if it's editable, need to clearAnnotations and reclaculate them...
       */
      public void keyTyped(SequenceKeyEvent p0) {
         /**
          * Below is code from the old Trans Trans Controller and activates the arrow
          * keys and the find dialog.
          */
         int keyCode = p0.getKeyCode();
         if ( (keyCode != KeyEvent.VK_PAGE_DOWN) && (keyCode != KeyEvent.VK_PAGE_UP) )
            copySeqToBlastMenu.setEnabled(false);
         if ( p0.isControlDown() && keyCode==KeyEvent.VK_F )
            sequenceSearchDialog.show();



         // System.out.println("<JTS> Enter TranscriptTranslateView keyReleased()");
         // System.out.println("String has J at: " + seqViewer.getResidues().indexOf("J"));
         String residues = getEntireDNAString().trim();
         //System.out.println("keyReleased: residues=" + residues);
         if ( (residues==null) || (residues.length()==0) ) return;
         if ( translatedRegion == null ) return;
         seqViewer.clearSelection();
         seqViewer.clearAllAdornments();
         //need to clear widget to get rid of all the old orf specs that could have been
         seqViewer.setDNASequence(DNASequenceStorage.create(residues, ""),0);
         //been added by other key press events and then reset the residues.

         //start code added to get edited sequence to translate all the way to the next stop
         //get the orf specs keep the start position but recompute the stop position and set the orf specs
         //remember to reset it.
         int start = translatedRegion.getStart();
         int frame = (start%3) + 1;
         int stopPos = SequenceUtil.getSequenceUtil().findFirstInFrameStop(residues, start);
         int end = (stopPos >=0)? stopPos+2 : residues.length()-1;
         currentORF = new MutableRange(start, end);

         showOneFrame(frameToSeqFrame(frame));
         //end code added to get the transcript translation view to translate all the way to the next stop when the sequence
         //is edited


         resetAdornmentLists();
         // Looks like we are one event early here.  We're getting called before seqViewer
         // processes the key and inserts it... we could force key upper / lower.
         for ( Iterator iter=tlMenu.getFrameNumbersForMenuItemsSelected().iterator();iter.hasNext(); ) {
            int f=((Integer)iter.next()).intValue();
            annotateCodingRegions(getEntireDNAString(),true,f);
         }
         // Go ahead and enable the undo... the undo-action will disable it.
         curHandler.editUndoMI.setEnabled(true);
      }
/*
    public void keyReleased(SequenceKeyEvent e){
    }

    public void keyPressed(SequenceKeyEvent e){}
  */
   }


   /**
    * Inner class for handling mouse events.
    */
   private class MyMouseHandler implements SequenceMouseListener {
      public void mouseReleased(SequenceMouseEvent e){}
      public void mousePressed (SequenceMouseEvent e)  {
         //calculateRangeValuesForDisplay();
      }

      /** MouseMotionListener Implementation */
      /**
       * Get mouse dragged notification...
       * Get the selection range in residue coordinates.
       * Update the range text filed.
       */
      public void mouseSelectedRange(SequenceMouseEvent e){
         calculateRangeValuesForDisplay();
      }


      /**
       * Get mouse moved notification...
       * Get the residue coordinates of the mouse position and update the
       * approrpriate text fields.
       */
      public void mouseMoved(SequenceMouseEvent e)
      {
         long xPos = e.getSequencePosition();
         long nucleotidePos = xPos + 1;
         nucleLabel.setText (Integer.toString((int)nucleotidePos));
         aminoLabel.setText (Integer.toString((int)(xPos/3)));
         Feature feature = (Feature)browserModel.getCurrentSelection();

         if ( (browserModel.getCurrentSelection() instanceof SubFeature) && !(feature instanceof CuratedTranscript) ) {
            feature=((Feature)browserModel.getCurrentSelection()).getRootFeature();
         }

         int axisPosition=feature.transformSplicedPositionToAxisPosition((int)xPos);

         //CuratedExon ce=((CuratedTranscript) feature).getExonForPositionOnAxis(axisPosition);
         Feature subfeature=feature.getSubFeatureAtPositionOnAxis((Axis)browserModel.getMasterEditorEntity(),
                                                                  axisPosition);
         if ( subfeature!=null ) {
            if ( subfeature.getProperty(FeatureFacade.ORDER_NUM_PROP)!=null ) {
               //      System.out.println("order number"+subfeature.getProperty(FeatureFacade.ORDER_NUM_PROP).getInitialValue());
               exonOrderLabel.setText(subfeature.getProperty(FeatureFacade.ORDER_NUM_PROP).getInitialValue());
            }
            else {
				exonOrderLabel.setText("No Feature Property");
            }
         }

         if ( currentORF != null ) {
            long startPos = currentORF.getStart();
            long endPos = currentORF.getEnd();
            long transPos = xPos - startPos;
            String aminoAcidCoord = "";
            if ( (transPos >= 0) && (xPos <= endPos) )
               aminoAcidCoord = Long.toString((transPos/3) + 1); //amino acid coords start from 1
            aminoLabel.setText(aminoAcidCoord);
         }
         // Add a blank to length text field if there is NOTHING in it,
         // and if (as there is now) something is IN the amino acid text field.
         //    Otherwise, the value JLabel for the amino acid coordinate may
         // wind up next to the label JLabel for the selected amino acid length.
         if ((lengthLabel.getText() == null) || ("".equals(lengthLabel.getText()))) {
            lengthLabel.setText(" ");
         }
         if ( (rangeLabel.getText() == null) || ("".equals(rangeLabel.getText()))) {
		   rangeLabel.setText(" ");
         }
      }
   } //end inner class MyMouseHandler


   /**
    * Returns a list of the frames showing in the view.
    */
   private ArrayList getFramesShowing() {
      ArrayList frameList = new java.util.ArrayList(6);

      if ( seqViewer.isSequenceVisible(GenomicSequenceViewer.ORF_1_DISPLAY) )
         frameList.add(new Integer(GenomicSequenceViewer.ORF_1_DISPLAY));
      if ( seqViewer.isSequenceVisible(GenomicSequenceViewer.ORF_2_DISPLAY) )
         frameList.add(new Integer(GenomicSequenceViewer.ORF_2_DISPLAY));
      if ( seqViewer.isSequenceVisible(GenomicSequenceViewer.ORF_3_DISPLAY) )
         frameList.add(new Integer(GenomicSequenceViewer.ORF_3_DISPLAY));
      if ( seqViewer.isSequenceVisible(GenomicSequenceViewer.ORF_NEG1_DISPLAY) )
         frameList.add(new Integer(GenomicSequenceViewer.ORF_NEG1_DISPLAY));
      if ( seqViewer.isSequenceVisible(GenomicSequenceViewer.ORF_NEG2_DISPLAY) )
         frameList.add(new Integer(GenomicSequenceViewer.ORF_NEG2_DISPLAY));
      if ( seqViewer.isSequenceVisible(GenomicSequenceViewer.ORF_NEG3_DISPLAY) )
         frameList.add(new Integer(GenomicSequenceViewer.ORF_NEG3_DISPLAY));

      return (frameList);
   }


   public void setSelectionEnabledState(boolean selectionEnabled) {
      this.selectionEnabled = selectionEnabled;
      calculateRangeValuesForDisplay();
   }


   private void calculateRangeValuesForDisplay() {
      // Get the selection coordinates.  Report nucleotide positions starting from 1
      long start = seqViewer.getSelectionBegin()+1;
      long end = seqViewer.getSelectionEnd()+1;
      if ( (start < 0) || (end < 0) ) return;

      // Update the text field.
      if ( selectionEnabled ) rangeLabel.setText (start + " : " + end);
      else rangeLabel.setText("N/A");

      // Report amino acid range.
      String residues = getEntireDNAString();
      if ( (residues==null) || (residues.length()==0) ) return;

      ArrayList frameList = getFramesShowing();
      if ( frameList.size()<=0 ) return;
      int frameShowing = ((Integer)frameList.get(0)).intValue();
      if ( (frameList.size() > 1) || (frameShowing==GenomicSequenceViewer.ORF_NEG1_DISPLAY) ||
           (frameShowing==GenomicSequenceViewer.ORF_NEG2_DISPLAY) ||
           (frameShowing==GenomicSequenceViewer.ORF_NEG3_DISPLAY) ||
           !selectionEnabled )
         lengthLabel.setText("N/A");
      else {
         lengthLabel.setText (Long.toString((end-start+1)/3));
      }
   }


   /**
   * Inner class for handling consensus loading
   */
   private class MySequenceObserver implements SequenceObserver {

      private Feature compositeFeature;
      private Feature simpleFeature;

      public MySequenceObserver(Feature compositeFeature, Feature simpleFeature) {
         this.compositeFeature = compositeFeature;
         this.simpleFeature = simpleFeature;
      }

      public void noteSequenceArrived(Axis axis, Range rangeOfSequence, Sequence sequence) {
         //System.out.println("sequenceArrived: seq=" + sequence);
         displayFeature(axis,compositeFeature, simpleFeature, sequence);
      }

   }


   /**
    * Inner class for handling browser model events.
    */
   private class MyBrowserModelListenerAdapter extends BrowserModelListenerAdapter {
      /**
       * Get notification of browser master editor entity interval changed.
       * Part of the BrowserModelListener.
       *
       * The axis of the sub editor.
       */
      public void browserMasterEditorEntityChanged(GenomicEntity masterEditorEntity) {
         //System.out.println("TranscriptTranslateView: browserMasterEditorGenomicEntityChanged: p=" + masterEditorGenomicEntity);
         if ( masterAxis == masterEditorEntity ) return;

         if ( masterEditorEntity instanceof GenomicAxis ) {
            masterAxis = (GenomicAxis) masterEditorEntity;
         }
         masterAxis.removeGenomicEntityObserver(entityObserver);
         browserModelListener.browserMasterEditorEntityChanged(masterEditorEntity);
         masterEditorEntity.addGenomicEntityObserver(entityObserver);
         masterAxis = (GenomicAxis)masterEditorEntity;

         // This is invoked when anyone calls browserModel.setMasterEditorAxis().
         if ( !isMaster ) {
            clear();
            return;
         }
      }

      // Must leave the Fixed Range Forward. 5' to 3'.  It does not exactly
      // equal the fixed range in the Genomic Axis Annotation View.
      public void browserSubViewFixedRangeChanged(Range subViewFixedRange) {
         if ( subViewFixedRange==null ) return;
         MutableRange tmpRange = subViewFixedRange.toMutableRange();
         if ( isReverseComplement() ) tmpRange.mirror(masterAxis.getMagnitude());
         if ( lastFixedRange.equals(tmpRange) ) return;
         else lastFixedRange = tmpRange;
         clear();
         currentSelection = null;
         browserModel.setSubViewVisibleRange(new MutableRange());
         GenomicEntity tmpEntity = browserModel.getCurrentSelection();
         if ( tmpEntity instanceof Feature ) {
            Alignment tmpAlignment = ((Feature)tmpEntity).getOnlyAlignmentToAnAxis(masterAxis);
            if ( tmpAlignment==null ) return;
            if ( lastFixedRange.contains(((GeometricAlignment)tmpAlignment).getRangeOnAxis()) )
               browserModelListener.browserCurrentSelectionChanged(tmpEntity);
         }
         if ( isConnectedToDatabase() ) {
            copySeqToBlastMenu.setEnabled(true);
         }
         else {
            copySeqToBlastMenu.setEnabled(false);
         }
      }

      /**
       * Get notification of browser subeditor state changed.
       * Part of the BrowserModelListener.
       *
       * The axis of the sub editor.
       */
      public void browserSubViewVisibleRangeChanged(Range subViewVisibleRange) {
         // This could be redone to get the current selection.  This does not really need the entity passed.
         if ( subViewVisibleRange==null || subViewVisibleRange.getMagnitude()==0 ) return;
         if ( lastVisibleRange.equals(subViewVisibleRange) ) return;
         GenomicEntity subEditorEntity = browserModel.getCurrentSelection();
         boolean isReverseComplement = isReverseComplement();
         Axis axis = (Axis)browserModel.getMasterEditorEntity();

         MutableRange selectedRange = subViewVisibleRange.toMutableRange();
         if ( isReverseComplement )
            selectedRange.mirror(axis.getMagnitude());

         int start = ((Feature)subEditorEntity).transformAxisPositionToSplicedPosition(selectedRange.getStart());
         lastVisibleRange = selectedRange;
         Feature feature = (Feature)subEditorEntity;
         if ( getEntireDNAString().length()<=0 ) return;
         long beg = seqViewer.getVisibleBeginLocation();
         long end = seqViewer.getVisibleEndLocation();
         int axisStart = feature.transformSplicedPositionToAxisPosition((int)beg);
         int axisEnd = feature.transformSplicedPositionToAxisPosition((int)end);
         if ( axisStart < 0 || axisEnd < 0 ) return;
         MutableRange tempRange = new MutableRange(axisStart, axisEnd);
         if ( isReverseComplement )
            tempRange.mirror(axis.getMagnitude());
         seqViewer.scrollToLocation(start);
         lastVisibleRange = tempRange;
      }

      /**
       * Get notification of the last selection in the browser,
       * regardless of the source of selection
       * Part of the BrowserModelListener.
       */
      public void browserCurrentSelectionChanged(GenomicEntity newSelection) {
         if ( currentSelection!=null && currentSelection.equals(newSelection) ) return;
         //System.out.println("TTV.browserLastSelectionChanged() newSelection=" + newSelection + " currentSelection=" + currentSelection);
         if ( newSelection == null ) {
            browserModel.setSubViewVisibleRange(new MutableRange());
            currentSelection = null;
            clear();
            return;
         }
         if ( !(newSelection instanceof Feature) ) {
            browserModel.setSubViewVisibleRange(new MutableRange());
            currentSelection = null;
            clear();
            return;
         }
         Feature tmpFeature = (Feature)newSelection;
         Range tmpRange = ((GeometricAlignment)tmpFeature.getRootFeature().
                           getOnlyAlignmentToAnAxis(masterAxis)).getRangeOnAxis();
         if ( !lastFixedRange.contains(tmpRange) ) {
            browserModel.setSubViewVisibleRange(new MutableRange());
            currentSelection = null;
            clear();
            return;
         }
         currentSelection = tmpFeature;
         currentSelection.acceptVisitorForSelf(selectionVisitor);
      }
   }  //end inner class MyBrowserModelListener


   /**
    * Inner class for handling entity interval selections
    */
   private class SelectionVisitor extends GenomicEntityVisitor {
      public void visitCuratedTranscript(CuratedTranscript curatedTranscript) {
         if ( !((curHandler.editUndoMI.isEnabled()) && (getDisplayedFeature() == curatedTranscript)) ) {
            clear();
            loadSequence(curatedTranscript, null);
         }
      }

      public void visitCuratedExon(CuratedExon exon) {
         CuratedTranscript curatedTranscript = (CuratedTranscript) exon.getSuperFeature();
         if ( !((curHandler.editUndoMI.isEnabled()) && (getDisplayedFeature() == curatedTranscript)) ) {
            clear();
            loadSequence(curatedTranscript, exon);
         }
      }

      public void visitFeature(Feature f) {
         EntityType entityType=f.getEntityType();
         if ( !((curHandler.editUndoMI.isEnabled()) && (getDisplayedFeature() == f)&&
                ( canEditThisEntity(f))&&
                (entityType.equals(EntityType.getEntityTypeForValue(EntityTypeConstants.FgenesH)))) ) {
            clear();
            loadSequence(f, null);
         }
      }

      public void visitCuratedCodon(CuratedCodon codon) {
         GenomicEntity parent = codon.getSuperFeature();
         if ( parent != null )
            parent.acceptVisitorForSelf(selectionVisitor);
         else
            visitGenomicEntity(codon);
      }

      public void visitGenomicEntity(GenomicEntity entity) {
         clear();
      }
   }


   /**
      * My observer of the modification manager.
      * Recieves notificaitons regarding change in the ModifyManger state,
      * specifically the Start and Finish of Commands.
      */
   private class MyModifyManagerObserver extends ModifyManagerObserverAdapter {
      private boolean debugClass = false;

      public void noteCommandDidStart(String commandName) {
         if ( debugClass ) {
            System.out.println("MyModifyManagerObserver.noteCommandDidStart commandName=" + commandName);
         }

      }
      public void noteCommandDidFinish(String commandName, int unused) {
         if ( debugClass ) {
            System.out.println("MyModifyManagerObserver.noteCommandDidFinish commandName=" + commandName);
         }

         // Cannot display a feature if none has been selected.
         if ( masterAxis==null || currentFeature==null )  return;
         displayFeature(masterAxis, currentFeature, null,
                        currentFeature.getSplicedResidues());
      }
   }


   private class MyModelMgrObserver extends ModelMgrObserverAdapter {

      public void genomeVersionSelected(GenomeVersion genomeVersion){
         if ( genomeVersion.getGenomeVersionInfo().isDatabaseDataSource() ) {
            copySeqToBlastMenu.setEnabled(true);
         }
      }
   }


   private class PositionComparator implements Comparator {
      private boolean ascending;

      public PositionComparator(boolean ascending) {
         this.ascending = ascending;
      }

      /**
       * This method simple determines if one object starts before another.
       */
      public int compare(Object obj1, Object obj2) {
         if ( (obj1 instanceof GenomicEntity) && (obj2 instanceof GenomicEntity) ) {
            GenomicEntity p1 = (GenomicEntity)obj1;
            GenomicEntity p2 = (GenomicEntity)obj2;
            Range r1;
            Range r2;
            if ( p1 instanceof CuratedFeature ) {
               r1= getAxisAlignmentForEntity((CuratedFeature)p1);
            }
            else {
               r1=((Feature)p1).getOnlyGeometricAlignmentToAnAxis(masterAxis).getRangeOnAxis();
            }if ( p2 instanceof CuratedFeature ) {
               r2= getAxisAlignmentForEntity((CuratedFeature)p2);
            }
            else {
               r2=((Feature)p2).getOnlyGeometricAlignmentToAnAxis(masterAxis).getRangeOnAxis();
            }


            if ( r1.equals(r2) ) return (0);
            if ( ascending ) {
               if ( r1.getMinimum() < r2.getMinimum() ) return (-1);
               else return (1);
            }
            else { //descending
               if ( r1.getMaximum() > r2.getMaximum() ) return (-1);
               else return (1);
            }
         }
         return (0);
      }
   } //end inner class PositionComparator


   /**
    * Private inner class for handling Session-specific model property events.
    */
   private class MySessionModelListener implements SessionModelListener {
      public void browserAdded(BrowserModel browserModel){}
      public void browserRemoved(BrowserModel browserModel){}
      public void sessionWillExit(){}
      public void modelPropertyChanged(Object property, Object oldValue, Object newValue) {
         if ( property.equals(TransTransPanel.START_FILL_PROP) ||
              property.equals(TransTransPanel.STOP_FILL_PROP) ||
              property.equals(TransTransPanel.FRAME1_COLOR_PROP) ||
              property.equals(TransTransPanel.FRAME2_COLOR_PROP) ||
              property.equals(TransTransPanel.FRAME3_COLOR_PROP) ||
              property.equals(TransTransPanel.HIGHLIGHT_PROP) ||
              property.equals(TransTransPanel.ODD_ALTERNATING_EXON_COLOR_PROP) ||
              property.equals(TransTransPanel.EVEN_ALTERNATING_EXON_COLOR_PROP)) {
            frameOutlineColor.removeAllElements();
            startColor = (String)sessionMgr.getModelProperty(TransTransPanel.START_FILL_PROP);
            stopColor = (String)sessionMgr.getModelProperty(TransTransPanel.STOP_FILL_PROP);
            frame1Color = (String)sessionMgr.getModelProperty(TransTransPanel.FRAME1_COLOR_PROP);
            frame2Color = (String)sessionMgr.getModelProperty(TransTransPanel.FRAME2_COLOR_PROP);
            frame3Color = (String)sessionMgr.getModelProperty(TransTransPanel.FRAME3_COLOR_PROP);
            highlightColor = (String)sessionMgr.getModelProperty(TransTransPanel.HIGHLIGHT_PROP);
            oddAlternateColorName = (String)sessionMgr.getModelProperty(TransTransPanel.ODD_ALTERNATING_EXON_COLOR_PROP);
            evenAlternateColorName = (String)sessionMgr.getModelProperty(TransTransPanel.EVEN_ALTERNATING_EXON_COLOR_PROP);
            
            if ( startColor==null ) startColor = "Green";
            if ( stopColor==null ) stopColor = "Red";
            if ( frame1Color==null ) frame1Color = "White";
            if ( frame2Color==null ) frame2Color = "Magenta";
            if ( frame3Color==null ) frame3Color = "Cyan";
            if ( highlightColor==null ) highlightColor = "Blue";
            if (oddAlternateColorName==null) oddAlternateColorName="Yellow";
            if (evenAlternateColorName==null) evenAlternateColorName="Green";
            
            startFill = ViewPrefMgr.getViewPrefMgr().getColor(startColor);
            stopFill = ViewPrefMgr.getViewPrefMgr().getColor(stopColor);
            frameOutlineColor.add(ViewPrefMgr.getViewPrefMgr().getColor(frame1Color));
            frameOutlineColor.add(ViewPrefMgr.getViewPrefMgr().getColor(frame2Color));
            frameOutlineColor.add(ViewPrefMgr.getViewPrefMgr().getColor(frame3Color));
            seqViewer.setSelectionBackground (ViewPrefMgr.getViewPrefMgr().getColor(highlightColor));
			oddAlternateColor = ViewPrefMgr.getViewPrefMgr().getColor(oddAlternateColorName);
			evenAlternateColor = ViewPrefMgr.getViewPrefMgr().getColor(evenAlternateColorName);
			
            if ( currentSelection !=null ) {
               currentSelection.acceptVisitorForSelf(selectionVisitor);
            }
         }
      }
   }


   private class MyAlignableGenomicEntityObserver extends AlignableGenomicEntityObserverAdapter {

      public void noteGenomicEntityAttach( int level, GenomicEntity entity ) {
         if ( entity instanceof Feature ) {
            Feature feature = (Feature) entity;
            updateViewIfNecessary(feature, false);
         }
      }

      public void noteGenomicEntityDetach( int level, GenomicEntity entity ){
         if ( entity instanceof Feature ) {
            Feature feature = (Feature) entity;
            updateViewIfNecessary(feature, true);
         }
      }

      public void noteNewGeometry( int level, GenomicEntity entity) {
         if ( entity instanceof Feature ) {
            Feature feature = (Feature) entity;
            updateViewIfNecessary(feature, false);
         }
      }


      public void noteNewDetails( int level, GenomicEntity entity) {
         if ( entity instanceof Feature ) {
            Feature feature = (Feature) entity;
            updateViewIfNecessary(feature, false);
         }
      }
   }


   private class MySequenceSelectionListener implements SequenceSelectionListener {
      public void selectionChanged(SequenceSelectionEvent e) {

         nucleLabel.setText (Integer.toString((int)e.getEndLocation()));
         String tmpMin = Long.toString(Math.min(e.getBeginLocation(),e.getEndLocation()) + 1);
         String tmpMax = Long.toString(Math.max(e.getBeginLocation(),e.getEndLocation()) + 1);
         rangeLabel.setText(tmpMin +":"+tmpMax);


         Feature feature = (Feature)browserModel.getCurrentSelection();
         if ( (browserModel.getCurrentSelection() instanceof SubFeature) && !(feature instanceof CuratedTranscript) ) {
            feature=((Feature)browserModel.getCurrentSelection()).getRootFeature();
         }
         long xPos = e.getEndLocation();
         int axisPosition=feature.transformSplicedPositionToAxisPosition((int)xPos);

         Feature subfeature=feature.getSubFeatureAtPositionOnAxis((Axis)browserModel.getMasterEditorEntity(),
                                                                  axisPosition);
         if ( subfeature!=null ) {
            if ( subfeature.getProperty(FeatureFacade.ORDER_NUM_PROP)!=null ) {
               exonOrderLabel.setText(subfeature.getProperty(FeatureFacade.ORDER_NUM_PROP).getInitialValue());
            }
			else {
				exonOrderLabel.setText("No Feature Property");
			}
         }

      }

   }


   private class MySequenceSearchListener implements SequenceSearchListener {
      public void focusOnSearchTarget(SwingRange targetRange) {
         seqViewer.setSelectionInterval(targetRange.getStartRange(),targetRange.getEndRange());
         seqViewer.scrollToLocation(targetRange.getStartRange());
      }
   }
}
