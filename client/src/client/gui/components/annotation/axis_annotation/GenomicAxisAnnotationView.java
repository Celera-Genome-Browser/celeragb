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
/**
 * CVS_ID:  $Id$
 */

package client.gui.components.annotation.axis_annotation;

import api.entity_model.access.observer.ReportObserver;
import api.entity_model.access.observer.SequenceObserver;
import api.entity_model.access.report.LineItem;
import api.entity_model.access.report.PropertyReport;
import api.entity_model.access.report.PropertyReportRequest;
import api.entity_model.access.report.Report;
import api.entity_model.access.report.ReportRequest;
import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.management.ModifyManager;
import api.entity_model.management.PropertyMgr;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.CuratedFeature;
import api.entity_model.model.annotation.CuratedGene;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.annotation.HSPFeature;
import api.entity_model.model.annotation.HitAlignmentDetailFeature;
import api.entity_model.model.annotation.HitAlignmentFeature;
import api.entity_model.model.annotation.SuperFeature;
import api.entity_model.model.assembly.Contig;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.ActiveThreadModel;
import api.entity_model.model.fundtype.AlignableGenomicEntity;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.fundtype.LoadFilter;
import api.entity_model.model.fundtype.LoadRequest;
import api.entity_model.model.fundtype.LoadRequestStatus;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.facade.abstract_facade.annotations.HitAlignmentFacade;
import api.stub.data.OID;
import api.stub.geometry.MutableRange;
import api.stub.geometry.Range;
import api.stub.sequence.DNA;
import api.stub.sequence.Sequence;
import client.gui.framework.browser.Browser;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.BrowserModelListenerAdapter;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.framework.session_mgr.SessionModelListener;
import client.gui.framework.view_pref_mgr.TierInfo;
import client.gui.framework.view_pref_mgr.ViewInfo;
import client.gui.framework.view_pref_mgr.ViewPrefListenerAdapter;
import client.gui.framework.view_pref_mgr.ViewPrefMgr;
import client.gui.framework.view_pref_mgr.ViewPrefMgrListener;
import client.gui.other.menus.FilterMenu;
import client.gui.other.xml.xml_writer.XMLWriter;
import client.shared.text_component.StandardTextField;
import shared.util.FreeMemoryWatcher;
import vizard.Bounds;
import vizard.Glyph;
import vizard.ParentGlyph;
import vizard.RootGlyph;
import vizard.genomics.component.ForwardAndReverseTiersComponent;
import vizard.genomics.component.TiersComponent;
import vizard.genomics.glyph.FeaturePainter;
import vizard.genomics.glyph.TierGlyph;
import vizard.genomics.glyph.TiersColumnGlyph;
import vizard.genomics.model.FeatureAdapter;
import vizard.genomics.model.GenomicAxisViewModel;
import vizard.glyph.AdornmentGlyph;
import vizard.glyph.TranslationGlyph;
import vizard.model.WorldViewModel;
import vizard.util.Assert;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * View component of MVC for display of a annotations along a GenomicAxis
 * also maintains a AxisObserver that registers to observe the Axis
 * that is being displayed, and dynamically updates display when notified of changes
 */
public class GenomicAxisAnnotationView extends ForwardAndReverseTiersComponent {
   public static final String VIEW_NAME = "Genomic Axis Annotation";
   public static final String REV_TIER_SUFFIX = " (rev)";
   public static final String CURATION_TIER_NAME = "Workspace";
   public static final String SEQUENCE_TIER_NAME = "Consensus";
   public static final String CONTIG_TIER_NAME = "Contig";
   public static final String UNKNOWN_FEATURE_GROUP = "Unknown";
   private static final int LEGEND_WIDTH = 90;

   private static final Color COLOR_MAP_BACKGROUND = Color.black;
   private static final Color COLOR_LEGEND_BACKGROUND = Color.gray;
   private static final Color COLOR_RUBBER_BAND = Color.red;
   private static final Color COLOR_SELECT_DEFAULT = Color.red;
   private static final Color COLOR_SELECT_GENE = Color.green;
   private static final Color COLOR_SELECT_COMPOSITE = Color.yellow;
   private static final Color COLOR_HIGHLIGHT_EVIDENCE = Color.red;
   private static final Color COLOR_OBSOLETE = Color.lightGray;

   private Browser browser;
   private GBGlyphFactory glyphFactory;
   private GenomicAxis masterAxis;

   private boolean isMaster; //@todo check how is this boolean used
   private boolean displayingSubEditors =
      ((Boolean) SessionMgr.getSessionMgr().getModelProperty(SessionMgr.DISPLAY_SUB_EDITOR_PROPERTY)).booleanValue();

   private ArrayList hiddenTiers = new ArrayList();

   SequenceGlyph sequenceGlyph;
   SequenceGlyph reverseSequenceGlyph;

   private JPanel textualRangePanel;
   private JTextField searchStartPositionTextField;
   private JTextField searchEndPositionTextField;
   private JButton zoomButton;

   TierPopupMenu legendActionMenu; //not private because of TierPopupMenuController
   private OptionsMenu optionsMenu;
   private FilterMenu filterMenu;
   private JMenu dataMenu = new JMenu("Data Manipulation");
   private JPopupMenu mapActionMenu;

   private AxisRulerSubViewRangeGlyph subViewRangeGlyph;

   private HashSet controllers = new HashSet();

   private ArrayList adornmentGlyphs = new ArrayList();
   private GBGenomicGlyph currentGlyphSelection;
   private boolean wasSelectionGeneratedInThisView;
   private boolean isZoomToSelectionRequired;

   private ModifyManager modifyMgr = ModifyManager.getModifyMgr();

   private SessionModelListener sessionModelListener = new SessionModelListener() {
      public void browserAdded(BrowserModel browserModel) {
      }
      public void browserRemoved(BrowserModel browserModel) {
      }
      public void sessionWillExit() {
      }

      public void modelPropertyChanged(Object property, Object oldValue, Object newValue) {
         if (property.equals("HighlightEvidenceProperty")
            || property.equals("ShowEdgeMatchesProperty")
            || property.equals("ShowEvidenceEdgeMismatches")
            || property.equals("SpliceMismatchProperty")
            || property.equals("PercentIntronThickness")
            || property.equals("HSPIntronDisplayState")) {
            showCurrentSelectionAgain();
         }
         if (property.equals(SessionMgr.DISPLAY_SUB_EDITOR_PROPERTY)) {
            displayingSubEditors = (((Boolean) newValue).booleanValue());
         }
      }
   };

   private BrowserModelListenerAdapter browserModelListener = new BrowserModelListenerAdapter() {
      public void modelPropertyChanged(Object property, Object oldValue, Object newValue) {
         if (property.equals("ResetGBGenomicGlyphIntensities")) {
            resetGlyphIntensities();
         }
         else if (property.equals("NavigationComplete")) {
            if (newValue == null)
               return;
            if (newValue instanceof Feature) {
               Feature tmpFeature = (Feature) newValue;
               boolean zoomSuccess = false;
               int tries = 0;
               // Looping till zoom succeeds or 10 seconds have gone by.
               while (!zoomSuccess || tries >= 10) {
	               if (getGlyphFor(tmpFeature) == null) {
					  try {
						 Thread.sleep(1000);
					  	 tries++;
					  	 //System.out.println("No glyph for feature yet.  Will try to zoom again.  Tries: "+tries);
					  } catch (InterruptedException e) {
					  }
	               }	
	               else { 
	               	  zoomSuccess = zoomToAlignment(tmpFeature.getOnlyAlignmentToAnAxis(masterAxis));
	                  //System.out.println("Zoom succeeded.");
	               }
               }
            }
            else if (newValue instanceof Contig) {
               Contig tmpContig = (Contig) newValue;
               /**
                * @todo This block could be better as there may be more than one
                * alignment for this contig on this axis.
                */
               zoomToAlignment((Alignment) tmpContig.getAlignmentsToAxis(masterAxis).iterator().next());
            }

            //  Check if focus exists and is requested.
            if (SessionMgr.getSessionMgr().getModelProperty("FocusSubviewsUponNavigation") != null) {
               boolean tmpFocus = ((Boolean) SessionMgr.getSessionMgr().getModelProperty("FocusSubviewsUponNavigation")).booleanValue();
               if (tmpFocus) {
                  Range tmpRange = new Range((int) axisModel().origin(), (int) axisModel().viewEnd());
                  getBrowserModel().setSubViewFixedRange(tmpRange);
                  getBrowserModel().setMasterEditorSelectedRange(tmpRange);
               }
            }
         }

         else if (property.equals("ZoomToLocation")) {
            Range tmpRange = new Range(((Integer) newValue).intValue(), ((Integer) newValue).intValue() + 1);
            getBrowserModel().setMasterEditorSelectedRange(tmpRange);
            zoomToSelectedRegion();
            final MyFeatureAdapter highlightAdapter = new MyFeatureAdapter(tmpRange);
            final FeaturePainter nucleotideHighlightGlyph = new FeaturePainter(highlightAdapter);
            sequenceGlyph.addChild(nucleotideHighlightGlyph);
            sequenceGlyph.repaint();
            Thread highlightThread = new Thread() {
               public void run() {
                  try {
                     for (int x = 0; x < 10; x++) {
                        EventQueue.invokeLater(new Runnable() {
                           public void run() {
                              Color tmpColor = highlightAdapter.color();
                              float[] origRGB = new float[4];
                              tmpColor.getComponents(origRGB);
                              float intensity = origRGB[3];
                              intensity = intensity - 0.1f;
                              highlightAdapter.setColor(new Color(origRGB[0], origRGB[1], origRGB[2], intensity));
                              nucleotideHighlightGlyph.repaint();
                           }
                        });
                        Thread.currentThread().sleep(250);
                     }
                  }
                  catch (Exception ex) {
                     SessionMgr.getSessionMgr().handleException(ex);
                  }
                  finally {
                     EventQueue.invokeLater(new Runnable() {
                        public void run() {
                           if (sequenceGlyph == null)
                              return;
                           sequenceGlyph.removeChild(nucleotideHighlightGlyph);
                           sequenceGlyph.repaint();
                        }
                     });
                  }
               }
            };
            highlightThread.start();
         }
      }

      public void browserMasterEditorEntityChanged(GenomicEntity masterEditorEntity) {
         if (isMaster && !masterEditorEntity.equals(masterAxis))
            setMasterAxis((GenomicAxis) masterEditorEntity);
         // every time we change the axis we need to make sure
         // unload/load/setsubview menu items are grayed out
         for (int i = 0; i < dataMenu.getMenuComponentCount(); i++) {
            dataMenu.getMenuComponent(i).setEnabled(false);
         }
      }
      public void browserMasterEditorSelectedRangeChanged(Range newRange) {
         selectedRangeChanged(newRange);
         // if the range of the axis is not selected then donot
         // enable the unoad/load/setsubview range menu items
         if (!newRange.isNull() && newRange.getMagnitude() > 0) {
            for (int i = 0; i < dataMenu.getMenuComponentCount(); i++) {
               dataMenu.getMenuComponent(i).setEnabled(true);
            }
         }
      }
      public void browserCurrentSelectionChanged(GenomicEntity selection) {
         showSelection();
      }
      public void browserClosing() {
         dispose();
      }
   };

   private LoadRequestStatus loadAlignments(Axis axis, Range range, LoadFilter loadFilter) {
      // If the filter is NOT strand specific, we need to always have a forward range...
      if (!loadFilter.isStrandSpecific() && range.isReversed()) {

         range = range.toReverse();

      }

      return axis.loadAlignmentsToEntitiesBackground(new LoadRequest(range, loadFilter, false));

   }

   private WorldViewModel.Observer sequenceMapper = new WorldViewModel.Observer() {
      public void zoomCenterChanged(WorldViewModel model) {
      }
      public void modelChanged(WorldViewModel model) {
         if (model.scale() >= SequenceGlyph.maxScaleForDrawings())
            hideSequenceTier();
         else
            showSequenceTier();
      }
   };

   private ViewPrefMgrListener myPrefListener = new ViewPrefListenerAdapter() {
      public void preferencesChanged() {
         resetPreferences();
      }
      public void tierStateChanged(TierInfo info) {
         resetTierGlyphState(info);
      }
      public void tierAdded(TierInfo info) {
      }
      public void tierRemoved(TierInfo info) {
         TiersColumnGlyph[] allColumns = { forwardColumn(), reverseColumn(), axisColumn()};
         for (int x = 0; x < 3; ++x) {
            TiersColumnGlyph column = allColumns[x];
            boolean found = false;
            for (int i = 0; i < column.tierCount(); i++) {
               TierGlyph tier = column.tier(i);
               TierInfo tinfo = getTierInfo(tier.name());
               String infoName = info.getName();
               String tinfoName = tinfo.getName();
               if (tinfoName.equals(infoName)) {
                  Collection genomicChildren = tier.genomicChildren();
                  if (genomicChildren != null) {
                     JOptionPane.showMessageDialog(browser, "Cannot delete " + infoName + " tier because it has features loaded");
                     found = true;
                     break;
                  }
               }
               if (found)
                  break;
            }
         }
         //preferencesChanged();
      }

   };

   //@todo: temporary, see AutoNavigator
   static GenomicAxisAnnotationView mainView;

   //-----------------------------------------------
   // Constructor
   //-----------------------------------------------

   public GenomicAxisAnnotationView(Browser browser, boolean isM) {

      super(new GenomicAxisViewModel(1000));
      if (mainView == null)
         mainView = this;

      this.browser = browser;

      glyphFactory = new GBGlyphFactory(new GBGlyphFactory.TierFinder() {
         public TierGlyph tierGlyphFor(AlignableGenomicEntity entity) {
            return getTierGlyphFor(entity);
         }
         public Collection workspaceTiers() {
            ArrayList list = new ArrayList();
            if (getForwardCurationTier() != null)
               list.add(getForwardCurationTier());
            if (getReverseCurationTier() != null)
               list.add(getReverseCurationTier());
            return list;
         }
      });
      glyphFactory.observers.addObserver(new GBGlyphFactory.BatchCreationObserver() {
         public void batchCreationDone(GBGlyphFactory glyphFactory) {
            showSelection();
         }
      });

      ViewPrefMgr.getViewPrefMgr().registerPrefMgrListener(myPrefListener);
      optionsMenu = new OptionsMenu(this);
      filterMenu = new FilterMenu(browser);

      BrowserModel browserModel = browser.getBrowserModel();
      browserModel.setModelProperty(BrowserModel.REV_COMP_PROPERTY, Boolean.FALSE);
      isMaster = isM;

      forwardTable().tiersComponent().setBackground(COLOR_MAP_BACKGROUND);
      reverseTable().tiersComponent().setBackground(COLOR_MAP_BACKGROUND);
      axisTable().tiersComponent().setBackground(COLOR_MAP_BACKGROUND);

      forwardTable().tierNamesComponent().setBackground(COLOR_LEGEND_BACKGROUND);
      reverseTable().tierNamesComponent().setBackground(COLOR_LEGEND_BACKGROUND);
      axisTable().tierNamesComponent().setBackground(COLOR_LEGEND_BACKGROUND);

      forwardTable().tierNamesComponent().setPreferredSize(new Dimension(LEGEND_WIDTH, 100));
      reverseTable().tierNamesComponent().setPreferredSize(new Dimension(LEGEND_WIDTH, 100));
      axisTable().tierNamesComponent().setPreferredSize(new Dimension(LEGEND_WIDTH, 100));

      dataMenu.addMenuListener(new MyMenuListener());

      for (Iterator it = getDataMenuItems().iterator(); it.hasNext();) {
         dataMenu.add((JComponent) it.next());
      }
      dataMenu.setText("Data Manipulation");
      dataMenu.setMnemonic('D');

      browserModel.addBrowserModelListener(browserModelListener);
      SessionMgr.getSessionMgr().addSessionModelListener(sessionModelListener);
      subViewRangeGlyph = new AxisRulerSubViewRangeGlyph(axisRulerGlyph());
      axisRulerGlyph().ruler().parent().addChild(subViewRangeGlyph);
      initializeControllers();
      axisModel().observers.addObserver(sequenceMapper);
   }

   public GBGlyphFactory glyphFactory() {
      return glyphFactory;
   }

   public void addController(Controller controller) {
      controllers.add(controller);
   }

   public void removeController(Controller controller) {
      controllers.remove(controller);
   }

   private void deleteControllers() {
      while (!controllers.isEmpty()) {
         Controller c = (Controller) controllers.iterator().next();
         c.delete();
      }
   }

   private void initializeControllers() {
      new RubberSelectionController(this, COLOR_RUBBER_BAND);
      new ResizeTierNamesController(this);
      new HighlightController(this);
      new TierPopupMenuController(this);
      new ClickExpandTierController(this);
      new DragTierController(this);
      new MagnifyingGlassController(this);
      new AxisPopupMenuController(this);
      new RulerSubViewRangeController(subViewRangeGlyph, getBrowserModel());
      new ShadowBoxController(this);
      new AxisSessionBookMarkGlyphController(this, axisRulerGlyph(), browser);
      new DisplayRuleController(this, browser);
      //@todo new ToolTipController(this);
   }

   /** Used for checking if is in reverse complement axis mode */
   public boolean isReverseComplement() {
      Boolean isRevComp = (Boolean) getBrowserModel().getModelProperty(BrowserModel.REV_COMP_PROPERTY);
      if (isRevComp == null)
         throw new IllegalStateException("Model property GAAnnotRevComped not found.");
      return isRevComp.booleanValue();
   }

   private void ensureFeatureInfoExistFor(GBGenomicGlyph glyph) {
      AlignableGenomicEntity entity = (AlignableGenomicEntity) glyph.alignment().getEntity();
      String entityName;
      if (entity instanceof Contig)
         entityName = CONTIG_TIER_NAME;
      else {
         Assert.vAssert(entity instanceof Feature);
         entityName = ((Feature) entity).getEnvironment();
         if (entityName == null)
            entityName = UNKNOWN_FEATURE_GROUP;
      }
      ViewPrefMgr prefs = ViewPrefMgr.getViewPrefMgr();
      if (prefs.getFeatureInfo(entityName) == null) {
         String rootName = (entity instanceof Feature) ? ((Feature) entity).getRootFeature().getEntityType().getEntityName() : entityName;
         prefs.createNewFeature(entityName, entityName, rootName);
      }
   }

   public void resetPreferences() {
      setUpTiers();

      Iterator i = glyphFactory.getGlyphCollection().iterator();
      while (i.hasNext()) {
         GBGenomicGlyph glyph = (GBGenomicGlyph) i.next();
         ensureFeatureInfoExistFor(glyph);
         glyph.propertiesChanged();

         if (glyph.genomicParent() == null) {
            TierGlyph oldTierGlyph = glyph.tierAncestor();
            if (oldTierGlyph == null)
               return;
            TierGlyph newTierGlyph = getTierGlyphFor(glyph.alignment().getEntity());
            if (newTierGlyph != oldTierGlyph) {
               oldTierGlyph.removeGenomicChild(glyph);
               newTierGlyph.addGenomicChild(glyph);
            }
         }
      }

      recreateTiersHoldingGenes();
   }

   private void recreateTiersHoldingGenes() {
      // @todo
      // Genes "height" is poorly implemented:
      //   1- it is available the height data member of its GBGenomicGlyph superclass, and
      //   2- it can be retrieved by calling getBounds().
      // When a Gene has overlapping transcripts, these will be shifted down, and getBounds()
      // will return the real new bounds. But the "height" data field does not get updated.
      // This needs a redesign for how the hight of a genomic glyph is computed.
      // The following is a quick fix: if we rebuild a tier from scratch, it always takes the
      // correct height into account.
      //jojic
   }

   /** Used for setting the MasterAxis, effectively reinitiallizing the component */
   private void setMasterAxis(GenomicAxis genomicAxis) {
      //We want to rebuild all glyphs because something "big" has changed.
      //But it's the same axis and the glyph factory would not do anything
      //if we do not first set it to null.
      //(and if it's not the same axis, it does not harm to set it to null first)
      //glyphFactory.setGenomicAxis(null);

      boolean activelyLoadingOnPriorAxis = false;
      if (masterAxis != null) {
         //            System.out.println("Number of threads: "+ActiveThreadModel.getActiveThreadModel().getActiveThreadCount());
         LoadRequestStatus[] activeLoadRequests = ActiveThreadModel.getActiveThreadModel().getActiveLoadRequestStatusObjects();
         Set axisLoadFilters = masterAxis.getDefaultLoadFilters();
         for (int i = 0; i < activeLoadRequests.length; i++) {
            if (axisLoadFilters.contains(activeLoadRequests[i].getLoadFilter())) {
               activelyLoadingOnPriorAxis = true;
               break;
            }
         }
      }

      boolean sameAxes = (genomicAxis == masterAxis);

      if (!sameAxes && masterAxis != null && masterAxis.getNumberOfAlignmentsToAxis() > 0 && !activelyLoadingOnPriorAxis) {
         int ans1 = 0;
         int ans =
            JOptionPane.showConfirmDialog(
               browser,
               "Would you like to remove all non-Workspace data from prior axis?",
               "Unload Data?",
               JOptionPane.YES_NO_OPTION,
               JOptionPane.QUESTION_MESSAGE);
         if (ans == JOptionPane.YES_OPTION) {

            if (masterAxis.getGenomeVersion().getWorkspace().getWorkspaceOids().size() > 0)
               // user should be warned that the the workspace is about to be unloaded
               // ask him if he wants to save it or not.
               // the reason workspace features are unloaded is because their promoted counterparts are
               // being unloaded
               {
               ans1 =
                  JOptionPane.showConfirmDialog(
                     browser,
                     "The Workspace is about to be unloaded,\nincluding Workspace features on all axes. \nWould you like to save it?",
                     "Save Workspace?",
                     JOptionPane.YES_NO_OPTION);
               if (ans1 == JOptionPane.YES_OPTION) {
                  XMLWriter.getXMLWriter().saveAsXML();
               }

               ModifyManager.getModifyMgr().flushStacks();
               masterAxis.getGenomeVersion().unloadWorkspace();
            }
            masterAxis.unloadCachedSequence();
            Set filters = masterAxis.getDefaultLoadFilters();
            LoadFilter filter;
            Range masterRange = new Range(0, masterAxis.getMagnitude());
            Range reverseMasterRange = new Range(masterAxis.getMagnitude(), 0);
            for (Iterator it = filters.iterator(); it.hasNext();) {
               filter = (LoadFilter) it.next();
               if (filter.getAffectedStrand() == filter.BOTH_STRANDS || filter.getAffectedStrand() == filter.FORWARD_STRAND) {
                  masterAxis.unloadAlignmentsToEntitiesBackground(new LoadRequest(masterRange, filter, true));
               }
               else {
                  masterAxis.unloadAlignmentsToEntitiesBackground(new LoadRequest(reverseMasterRange, filter, true));
               }
            }
         }
      }

      optionsMenu.resetMenus();
      if (!sameAxes && masterAxis != null)
         axisRulerGlyph().setSelectedRange(-1, -2);

      // set axis being viewed
      masterAxis = genomicAxis;

      reset();
      deleteHiddenTiers();

      setUpTiers();
      if (!sameAxes) {
         axisModel().setWorld(0, masterAxis.getMagnitude());
         axisModel().showEverything();
      }

      glyphFactory.setGenomicAxis(genomicAxis);

      if (!sameAxes)
         axisModel().setZoomCenter(axisModel().worldOrigin() + axisModel().worldSize() / 2);
   }

   /** handle any cleanup prior to getting rid of a GenomicAxisAnnotationView */
   public void dispose() {
      glyphFactory.delete();

      getBrowserModel().removeBrowserModelListener(browserModelListener);

      deleteControllers();
      deleteHiddenTiers();

      axisModel().observers.removeObserver(sequenceMapper);

      delete();
   }

   /** Return an array of JMenus that should be setup for this editor. */
   public JMenu[] getMenus() {
      return new JMenu[] { filterMenu, optionsMenu, dataMenu };
   }

   private void selectedRangeChanged(Range range) {

      if (range == null || range.isNull()) {
         axisRulerGlyph().setSelectedRange(-1, -2);
         //optionsMenu.disableMatchingSubjectSeq();
      }
      else {
         if (isReverseComplement()) {
            range = range.toMutableRange();
            ((MutableRange) range).mirror(masterAxis.getMagnitude());
         }
         axisRulerGlyph().setSelectedRange(range.getMinimum(), range.getMaximum());

      }

   }

   //***********************************************************************
   //***********************************************************************
   //*                            Protected Methods                        *
   //***********************************************************************
   //***********************************************************************

   public GenomicAxis getMasterAxis() {
      return masterAxis;
   }

   public Range getSelectedRange() {
      MutableRange range = getBrowserModel().getMasterEditorSelectedRange().toMutableRange();
      if (range != null && isReverseComplement())
         range.mirror(masterAxis.getMagnitude());
      return range;
   }

   void zoomToSelectedRegion() {
      if (getBrowserModel().getMasterEditorSelectedRange() == null || getBrowserModel().getMasterEditorEntity() == null)
         return;

      MutableRange rng = new MutableRange(getBrowserModel().getMasterEditorSelectedRange());
      if (isReverseComplement())
         rng.mirror(masterAxis.getMagnitude());
      zoomToRange(rng);
   }

   void selectVisibleRegion() {
      selectRange((int) axisModel().origin(), (int) axisModel().viewEnd());
   }

   private void selectRange(int start, int end) {
      if (start > end) {
         int temp = start;
         start = end;
         end = temp;
      }
      if (start < 0)
         start = 0;
      if (end > masterAxis.getMagnitude())
         end = masterAxis.getMagnitude();

      MutableRange selectedRange = new MutableRange(start, end);
      if (isReverseComplement())
         selectedRange.mirror(masterAxis.getMagnitude());
      getBrowserModel().setMasterEditorSelectedRange(selectedRange);
   }

   public Browser getBrowser() {
      return browser;
   }

   public BrowserModel getBrowserModel() {
      return browser.getBrowserModel();
   }

   /** @return the search panel was shown */
   boolean showSearch(boolean show) {
      final String SEARCH_PANEL_TEXT = "Zoom To Axis Position: ";

      if (show) {
         textualRangePanel = new JPanel(new FlowLayout()); //The search Panel
         textualRangePanel.add(new JLabel(SEARCH_PANEL_TEXT));
         searchStartPositionTextField = new StandardTextField(15);
         textualRangePanel.add(searchStartPositionTextField);
         searchEndPositionTextField = new StandardTextField(15);
         textualRangePanel.add(searchEndPositionTextField);
         zoomButton = new JButton("Zoom");
         zoomButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
               selectRangeFromTextualUI();
            }
         });
         searchStartPositionTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
               if (e.getKeyCode() == e.VK_ENTER) {
                  selectRangeFromTextualUI();
               }
            }
         });
         searchEndPositionTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
               if (e.getKeyCode() == e.VK_ENTER) {
                  selectRangeFromTextualUI();
               }
            }
         });
         textualRangePanel.add(zoomButton);
         textualRangePanel.doLayout();
         add(textualRangePanel, BorderLayout.NORTH);
         searchStartPositionTextField.requestFocus();
         searchStartPositionTextField.select(0, searchStartPositionTextField.getText().length());
      }
      else {
         remove(textualRangePanel);
      }
      invalidate();
      validate();
      return show;
   }

   /**
    * Does the range of the specified featureAlignment intersect the master editor selected
    * range?
    */
   private boolean isFeatureInSelectedRange(GeometricAlignment featureAlignment) {
      Range selRng = getBrowserModel().getMasterEditorSelectedRange();
      return (selRng == null) ? false : selRng.intersects(featureAlignment.getRangeOnAxis());
   }

   public Collection getAllGBGenomicGlyphs() {
      return glyphFactory.getGlyphCollection();
   }

   private void getHitAlignmentFeaturesInView(ArrayList hitAlignmentFeatures) {
      Collection items = getAllGBGenomicGlyphs();
      for (Iterator i = items.iterator(); i.hasNext();) {
         GBGenomicGlyph gl = (GBGenomicGlyph) i.next();
         GeometricAlignment featureAlignment = gl.alignment();
         GenomicEntity entity = featureAlignment.getEntity();
         if (entity instanceof HitAlignmentFeature) {
            HitAlignmentFeature hitAlignmentFeature = (HitAlignmentFeature) entity;
            if (isFeatureInSelectedRange(featureAlignment))
               hitAlignmentFeatures.add(hitAlignmentFeature);
         }
      }
   }

   /**
    * Get alignments for all HSPFeatures in view.
    */
   private HSPFeature[] getHSPFeaturesInView(boolean onlyInSelectedRange) {
      //get the features in the view from the hash table

      //return an array of those that are hit alignment features
      Collection items = getAllGBGenomicGlyphs();
      ArrayList alignmentDetailFeatures = new ArrayList();
      GBGenomicGlyph gl;
      HitAlignmentDetailFeature feature;
      GeometricAlignment featureAlignment;
      AlignableGenomicEntity entity;
      for (Iterator i = items.iterator(); i.hasNext();) {
         gl = (GBGenomicGlyph) i.next();
         entity = gl.alignment().getEntity();
         if (entity instanceof HSPFeature) {
            feature = (HSPFeature) entity;
            featureAlignment = feature.getOnlyGeometricAlignmentToOnlyAxis();
            if (onlyInSelectedRange) {
               //check to make sure that the feature falls in the selected range
               if (isFeatureInSelectedRange(featureAlignment))
                  alignmentDetailFeatures.add(feature);
            }
            else {
               alignmentDetailFeatures.add(feature);
            }
         }
      }

      HSPFeature[] features = new HSPFeature[alignmentDetailFeatures.size()];
      alignmentDetailFeatures.toArray(features);
      return features;
   }

   /** Tests range length.  Checks if too much memory would be used. */
   private boolean checkFreeMemoryAndLetUserOptOut(int numFeatures) {
      final long REPORT_LOAD_FACTOR = 5000;

      // Is there enough memory remaining to do this?
      if (numFeatures * REPORT_LOAD_FACTOR > FreeMemoryWatcher.getFreeMemoryWatcher().getFreeMemory()) {
         int choice =
            JOptionPane.showConfirmDialog(
               browser,
               "The number of features in the selected region may cause the browser "
                  + "to run out of memory.  To continue, click 'OK' or click 'Cancel' to "
                  + "select a smaller region and try again.",
               "Available Memory Low",
               JOptionPane.OK_CANCEL_OPTION);

         // User choose not to incur the risk.
         if (choice == JOptionPane.CANCEL_OPTION)
            return false;

      } // Memory is low.

      return true;
   }

   public boolean okToFindMatchingSubjectSeq() {
      ArrayList faList = new ArrayList();
      HitAlignmentFeature hit = getSelectedHitAlignmentAndInView(faList);
      return hit != null && faList.size() >= 1;
   }

   private HitAlignmentFeature getSelectedHitAlignmentAndInView(ArrayList faList) {
      GenomicEntity currentSelection = getBrowserModel().getCurrentSelection();
      HitAlignmentFeature selectedHitAlignment = null;
      if (currentSelection instanceof HitAlignmentFeature) {
         selectedHitAlignment = (HitAlignmentFeature) currentSelection;
      }
      else if (currentSelection instanceof Feature) {
         Feature superFeature = ((Feature) currentSelection).getSuperFeature();
         if (superFeature instanceof HitAlignmentFeature) {
            selectedHitAlignment = (HitAlignmentFeature) superFeature;
         }
      }
      if (selectedHitAlignment == null)
         return null;

      getHitAlignmentFeaturesInView(faList);
      return selectedHitAlignment;
   }

   /**
    * Find and highlight features with subject sequence id matching the specified id
    */
   void findMatchingSubjectSeq() {
      ArrayList faList = new ArrayList();
      HitAlignmentFeature selectedHitAlignment = getSelectedHitAlignmentAndInView(faList);
      Feature[] fa = new Feature[faList.size()];
      faList.toArray(fa);

      //make sure that there is at least one feature in the search area
      if (fa.length < 1) {
         //show a warning message and return
         JOptionPane.showMessageDialog(
            browser,
            "Either there is no selected range on the axis or there are " + "no loaded alignment features in the selected range.",
            "No features to search",
            JOptionPane.INFORMATION_MESSAGE);
         return;
      }

      if (!checkFreeMemoryAndLetUserOptOut(fa.length))
         return;

      String[] propName = new String[1];
      propName[0] = HitAlignmentFacade.SUBJECT_SEQ_ID_PROP;
      masterAxis.getGenomeVersion().generateReportBackground(
         new PropertyReportRequest(fa, propName),
         new MySubjectSeqPropListener(selectedHitAlignment));
   }

   public void resetGlyphIntensities() {
      Collection items = getAllGBGenomicGlyphs();
      for (Iterator it = items.iterator(); it.hasNext();) {
         GBGenomicGlyph targetGlyph = (GBGenomicGlyph) it.next();
         Color oldColor = targetGlyph.color();
         targetGlyph.setColor(new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue()));
      }
      repaint();
   }

   boolean reverseComplementAxis(boolean reverse) {
      BrowserModel browserModel = getBrowserModel();
      //Saves where the views are looking:
      double zoom = axisModel().scale();
      double end = axisModel().origin() + axisModel().viewSize();
      //the origin is always toward the central axis:
      WorldViewModel model = forwardTable().tiersComponent().verticalModel();
      double forwardDelta = model.worldEnd() - model.viewEnd();
      model = reverseTable().tiersComponent().verticalModel();
      double reverseDelta = model.origin() - model.worldOrigin();

      Boolean revBoolean = new Boolean(reverse);
      browserModel.setModelProperty(BrowserModel.REV_COMP_PROPERTY, revBoolean);

      //  Set the Total range for the subeditor.
      MutableRange subeditorAxisRange = browserModel.getSubViewFixedRange().toMutableRange();
      if (subeditorAxisRange != null && subeditorAxisRange.getMagnitude() > 0) {
         subeditorAxisRange.mirror(masterAxis.getMagnitude());
         browserModel.setSubViewFixedRange(subeditorAxisRange);
      }

      glyphFactory.setReverseComplement(reverse);
      resetPreferences();

      Range selectedRange = getSelectedRange();
      if (selectedRange != null)
         axisRulerGlyph().setSelectedRange(selectedRange.getMinimum(), selectedRange.getMaximum());

      axisModel().setView(axisModel().worldSize() - end, zoom);
      model = forwardTable().tiersComponent().verticalModel();
      model.setViewEnd(model.worldEnd() - reverseDelta);
      model = reverseTable().tiersComponent().verticalModel();
      model.setOrigin(model.worldOrigin() + forwardDelta);

      showCurrentSelectionAgain();

      repaint();
      return reverse;
   }

   //***********************************************************************
   //***********************************************************************
   //*                            Private Methods                          *
   //***********************************************************************
   //***********************************************************************

   private boolean zoomToRange(Range range) {
      if (range == null)
         return false;
      double extraWidth = 0.04 * range.getMagnitude();
      axisModel().setViewMinMax(range.getMinimum() - extraWidth / 2, range.getMaximum() + extraWidth / 2);

      axisModel().setZoomCenter((range.getMinimum() - extraWidth / 2 + range.getMaximum() + extraWidth / 2) / 2);
      return true;
   }

   private void centerOnRange(Range range) {
      if (range == null)
         return;
      int center = (range.getMinimum() + range.getMaximum()) / 2;
      axisModel().setViewCenter(center);

   }

   private boolean zoomToAlignment(Alignment alignment) {
      if (alignment == null)
         return false;
      GeometricAlignment geoAlignment = (GeometricAlignment) alignment;
      MutableRange zoomrng = geoAlignment.getRangeOnAxis().toMutableRange();
      if (isReverseComplement()) {
         zoomrng.mirror(masterAxis.getMagnitude());
      }
      boolean rangeZoom = zoomToRange(zoomrng);
      if (!rangeZoom) {
      	return false;
      }
      // The check below was added as they don't want the workspace centering
      // upon selection.
      if (!wasSelectionGeneratedInThisView)
         centerFeatureInVertical(alignment);
      return true;
   }

   public GBGenomicGlyph getGlyphFor(AlignableGenomicEntity entity) {
      return glyphFactory.getGlyphFor(entity);
   }

   private void centerFeatureInVertical(Alignment alignment) {
      if (alignment == null)
         return;

      GBGenomicGlyph featureGlyph = getGlyphFor(alignment.getEntity());
      if (featureGlyph == null)
         return;

      TierGlyph tier = featureGlyph.tierAncestor();
      ensureTierGlyphIsNotHidden(tier);
      TiersColumnGlyph tierColumn = tier.tierColumn();
      double y = tierColumn.yForTier(tier) + determineVerticalTierTranslationFor(featureGlyph);
      ParentGlyph p;
      for (p = tierColumn.parent(); p != null; p = p.parent()) {
         if (p instanceof RootGlyph)
            break;
      }
      if (p != null) {
         TiersComponent comp = (TiersComponent) ((RootGlyph) p).container();
         comp.verticalModel().setViewCenter(y);
      }
   }

   protected ArrayList getDataMenuItems() {
      ArrayList items = new ArrayList();
      AnnotationMenu annotationLoadingMenu, annotationUnloadingMenu;

      BrowserModel browserModel = getBrowserModel();
      annotationLoadingMenu = new AnnotationMenu(browserModel, this, true);
      annotationUnloadingMenu = new AnnotationMenu(browserModel, this, false);

      items.add(annotationLoadingMenu);
      items.add(annotationUnloadingMenu);
      /**
       * If the subviews are gone, disable the button.
       * If the red range is the same as the yellow range, disable.
       */
      //  setSubviewRangeMI.setEnabled(displayingSubEditors &&
      //    !browserModel.getMasterEditorSelectedRange().equals(browserModel.getSubViewFixedRange()));

      items.add(new JSeparator());
      items.add(getSubviewRangeMI());

      legendActionMenu = new TierPopupMenu(this);
      mapActionMenu = new JPopupMenu("MapPop");
      mapActionMenu.add(new JMenuItem("TestPopup"));
      return items;
   }

   private JMenuItem getSubviewRangeMI() {
      JMenuItem setSubviewRangeMI = new JMenuItem("Set SubView Range", 'S');
      setSubviewRangeMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK, false));
      setSubviewRangeMI.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            BrowserModel browserModel = getBrowserModel();
            MutableRange tmpRange = browserModel.getMasterEditorSelectedRange().toMutableRange();
            if (isReverseComplement()) {
               tmpRange.mirror(masterAxis.getMagnitude());
            }
            browserModel.setSubViewFixedRange(tmpRange);
         }
      });
      return setSubviewRangeMI;
   }

   /**
    * Find only aligned range for a given axis. If there is more than one alignment then only the
    * first alignment is returned.
    */
   public GeometricAlignment findOnlyAlignment(AlignableGenomicEntity entity) {
      GenomicAxis axis = (GenomicAxis) getBrowserModel().getMasterEditorEntity();
      Collection alignments = entity.getAlignmentsToAxis(axis);

      Iterator i = alignments.iterator();

      if (i.hasNext())
         return (GeometricAlignment) i.next();

      return null;
   }

   void rubberSelectionDone(int start, int end) {
      selectRange(start, end);
   }

   public void showEdgeMatches(Feature feature) {
      if (!feature.hasSubFeatures()) {
         if (findOnlyAlignment(feature) == null)
            return;
         masterAxis.acceptVisitorForAlignedEntities(new EdgeMatchVisitor(findOnlyAlignment(feature)), true);
      }
      else {
         for (Iterator i = feature.getSubFeatures().iterator(); i.hasNext();) {
            Feature subFeature = (Feature) i.next();
            showEdgeMatches(subFeature);
         }
      }
   }

   public void showEvidenceEdgeMismatches(Feature feature) {
      try {
         if (feature == null)
            return;
         for (Iterator featureIt = feature.getSubFeatures().iterator(); featureIt.hasNext();) {
            showEvidenceEdgeMismatches((Feature) featureIt.next());
         }
         ArrayList evidenceFeatures = new ArrayList(feature.getEvidence());
         for (Iterator it = evidenceFeatures.iterator(); it.hasNext();) {
            Feature tmpFeature = (Feature) it.next();
            if (findOnlyAlignment(tmpFeature) != null) {
               applyEdgeCheck(findOnlyAlignment(feature), findOnlyAlignment(tmpFeature), false);
            }
         }
      }
      catch (Exception ex) {
         /**
          *   @todo The evidence may not be loaded yet.  Do nothing.  Should load in evidence.
          */
         System.out.println("Not all evidence is loaded in the model for evidence edge mismatch to work.");
      }
   }

   private void applyEdgeCheck(GeometricAlignment targetAlignment, GeometricAlignment testAlignment, boolean showMatch) {
      Range rangeToMatch = targetAlignment.getRangeOnAxis();
      Range testRange = testAlignment.getRangeOnAxis();
      //make sure the entity range intersection with the entity to match is at least 1 base
      Range intersectionRng = Range.intersection(testRange, rangeToMatch);
      if (intersectionRng != null && intersectionRng.getMagnitude() >= 0) {
         if (isShowEdgeMatchesOn() || isShowEvidenceEdgeMismatchesOn()) {
            if ((testRange.getStart() == rangeToMatch.getStart() || testRange.getStart() == rangeToMatch.getEnd()) && showMatch) {
               highlightEdge(testAlignment, true, Color.yellow, 1);
            }
            else if (!(testRange.getStart() == rangeToMatch.getStart() || testRange.getStart() == rangeToMatch.getEnd()) && !showMatch) {
               highlightEdge(testAlignment, true, Color.yellow, 1);
            }

            if ((testRange.getEnd() == rangeToMatch.getStart() || testRange.getEnd() == rangeToMatch.getEnd()) && showMatch) {
               highlightEdge(testAlignment, false, Color.yellow, 1);
            }
            if (!(testRange.getEnd() == rangeToMatch.getStart() || testRange.getEnd() == rangeToMatch.getEnd()) && !showMatch) {
               highlightEdge(testAlignment, false, Color.yellow, 1);
            }
         }
      }
   }

   private void showSpliceMismatches(Feature feature) {
      if (!feature.hasSubFeatures()) {
         GeometricAlignment featureAlignment = feature.getOnlyGeometricAlignmentToAnAxis(masterAxis);
         Range featureRange = featureAlignment.getRangeOnAxis();
         // Calculate splice sites here.
         Range startRange = new Range(featureRange.getStart(), 1, featureRange.getOrientation());
         Range endRange = new Range(featureRange.getEnd(), 1, featureRange.getOrientation());
         // Checks the Start edge.
         if (!feature.hasSpliceEdge(startRange, false))
            highlightEdge(featureAlignment, true, Color.cyan, 2);
         // Checks the End edge.
         if (!feature.hasSpliceEdge(endRange, true))
            highlightEdge(featureAlignment, false, Color.cyan, 2);
      }
      else {
         for (Iterator i = feature.getSubFeatures().iterator(); i.hasNext();) {
            Feature subFeature = (Feature) i.next();
            showSpliceMismatches(subFeature);
         }
      }
   }

   private boolean isShowEdgeMatchesOn() {
      SessionMgr s = SessionMgr.getSessionMgr();
      Boolean b = (Boolean) s.getModelProperty("ShowEdgeMatchesProperty");
      return b != null && b.booleanValue();
   }

   private boolean isShowEvidenceEdgeMismatchesOn() {
      SessionMgr s = SessionMgr.getSessionMgr();
      Boolean b = (Boolean) s.getModelProperty("ShowEvidenceEdgeMismatches");
      return b != null && b.booleanValue();
   }

   private boolean isShowSpliceMismatchesOn() {
      SessionMgr s = SessionMgr.getSessionMgr();
      Boolean b = (Boolean) s.getModelProperty("SpliceMismatchProperty");
      return b != null && b.booleanValue();
   }

   private boolean isHighlightEvidenceOn() {
      SessionMgr s = SessionMgr.getSessionMgr();
      Boolean b = (Boolean) s.getModelProperty("HighlightEvidenceProperty");
      return b != null && b.booleanValue();
   }

   /**
    *  Highlight glyphs representing FeaturePIs used as evidence for CuratedFeatureI curation
    * Currently highlights by using selection mechanism
    */
   public void highlightEvidence(Feature curation) {
      Collection evidenceFeatures = curation.getDeepEvidence(false);
      Iterator evidenceItr = evidenceFeatures.iterator();
      while (evidenceItr.hasNext()) {
         Feature evidenceFeature = (Feature) evidenceItr.next();
         Collection featureAlignments = evidenceFeature.getAllGeometricAlignmentsToAxis(masterAxis);
         for (Iterator alignItr = featureAlignments.iterator(); alignItr.hasNext();) {
            GeometricAlignment geoAlign = (GeometricAlignment) alignItr.next();
            if (geoAlign != null)
               addAdornment(getGlyphFor(geoAlign.getEntity()), COLOR_HIGHLIGHT_EVIDENCE, 2);
         }
      }
   }

   public void getSequence(int start, int end) {
      int size = end - start;
      int axisSize = masterAxis.getMagnitude();
      Range requestRange;
      if (isReverseComplement()) {
         if (start < 0.0) {
            MutableRange muteRange = new MutableRange(0, size);
            muteRange.mirror(axisSize);
            requestRange = muteRange.toRange();
         }
         else if (start + size > axisSize) {
            MutableRange muteRange = new MutableRange(axisSize - size, axisSize);
            muteRange.mirror(axisSize);
            requestRange = muteRange.toRange();
         }
         else {
            MutableRange muteRange = new MutableRange(start, start + size);
            muteRange.mirror(axisSize);
            requestRange = muteRange.toRange();
         }
      }
      else {
         if (start < 0.0)
            requestRange = new Range(0, size);
         else if (start + size > axisSize)
            requestRange = new Range(axisSize - size, axisSize);
         else
            requestRange = new Range(start, start + size);
      }

      masterAxis.loadNucleotideSeq(requestRange, new MySequenceLoadObserver(requestRange));
   }

   public void writeObject(ObjectOutputStream out) {
      System.out.println("Someone is trying to serialize the Genomic Axis Annotation View.");
   }

   private void selectRangeFromTextualUI() {
      String startStr = searchStartPositionTextField.getText().trim();
      String endString = searchEndPositionTextField.getText().trim();
      double start = -1;
      double end = -1;
      try {
         if (startStr.endsWith("m") || startStr.endsWith("M")) {
            start = 1000 * 1000 * Double.parseDouble(startStr.substring(0, startStr.length() - 1));
         }
         if (startStr.endsWith("k") || startStr.endsWith("K")) {
            start = 1000 * Double.parseDouble(startStr.substring(0, startStr.length() - 1));
         }
         if (start == -1)
            start = Double.parseDouble(startStr);
         if (endString.endsWith("m") || endString.endsWith("M")) {
            end = 1000 * 1000 * Double.parseDouble(endString.substring(0, endString.length() - 1));
         }
         if (endString.endsWith("k") || endString.endsWith("K")) {
            end = 1000 * Double.parseDouble(endString.substring(0, endString.length() - 1));
         }
         if (end == -1)
            end = Double.parseDouble(endString);

         int magnitude = masterAxis.getMagnitude();
         if (start < 0 || start > magnitude)
            throw new Exception();
         if (end < 0 || end > magnitude)
            throw new Exception();
         Range zoomrng = new Range((int) start, (int) end);
         MutableRange selectedRange = zoomrng.toMutableRange();
         if (isReverseComplement())
            selectedRange.mirror(magnitude);
         getBrowserModel().setMasterEditorSelectedRange(selectedRange);
         zoomToSelectedRegion();
      }
      catch (Exception ex) {
         Object[] messages = new String[1];
         messages[0] = "Start and end must be numbers between 0 and " + masterAxis.getMagnitude();
         // currentSelection.getMagnitude();
         JOptionPane.showMessageDialog(SessionMgr.getSessionMgr().getActiveBrowser(), messages, "ERROR!!", JOptionPane.ERROR_MESSAGE);
      }
   }

   //---------------------------------------------------------------

   /**
    * Listen for sequence
    */
   private class MySequenceLoadObserver implements SequenceObserver {
      private Range requestRange;

      public MySequenceLoadObserver(Range requestRange) {
         this.requestRange = requestRange;
      }

      public void noteSequenceArrived(Axis axis, Range rangeOfSequence, Sequence sequence) {
         if (sequence == null)
            return;

         // Figuring the location of the arrow and the residues to
         // display is a function of both the rev-compedness of the
         // scaffold, and the forward-ness of the contig.
         if (requestRange.isReversed()) {
            sequence = DNA.reverseComplement(sequence);
            MutableRange muteRange = requestRange.toMutableRange();
            muteRange.mirror(masterAxis.getMagnitude());
            requestRange = muteRange.toRange();
         }
         String residues = DNA.toString(sequence);
         if (sequenceGlyph != null)
            sequenceGlyph.sequenceReady(requestRange.getMinimum(), residues);
         if (reverseSequenceGlyph != null)
            reverseSequenceGlyph.sequenceReady(requestRange.getMinimum(), residues);
      }
   }

   /**
    * Inner class used by the highlight edge and splice mismatches matches options
    */
   private class EdgeMatchVisitor extends GenomicEntityVisitor {
      private GeometricAlignment alignmentToMatch;

      public EdgeMatchVisitor(GeometricAlignment alignmentToMatch) {
         this.alignmentToMatch = alignmentToMatch;
      }

      public void visitFeature(Feature feature) {
         if (feature instanceof CuratedGene)
            return;
         GeometricAlignment featureAlignment = feature.getOnlyGeometricAlignmentToAnAxis(masterAxis);
         applyEdgeCheck(alignmentToMatch, featureAlignment, true);
      }
   }

   private class MyMenuListener implements javax.swing.event.MenuListener {
      public void menuCanceled(javax.swing.event.MenuEvent e) {
      }
      public void menuDeselected(javax.swing.event.MenuEvent e) {
      }
      public void menuSelected(javax.swing.event.MenuEvent e) {
         dataMenu.removeAll();
         for (Iterator it = getDataMenuItems().iterator(); it.hasNext();) {
            dataMenu.add((JComponent) it.next());
         }
      }
   }

   /**
    * Inner class for handling subject sequence match requests
    */
   private class MySubjectSeqPropListener implements ReportObserver {
      private HitAlignmentFeature hitAlignmentFeature;

      public MySubjectSeqPropListener(HitAlignmentFeature feature) {
         this.hitAlignmentFeature = feature;
      }

      public void reportArrived(GenomicEntity entityThatReportWasRequestedFrom, ReportRequest request, Report report) {
         highlightSubjectSeqMatches(report);
      }

      private long getSubjectSeqId(OID featureOID, Report report) {
         LineItem[] items = report.getLineItems();
         PropertyReport.ReportLineItem reportLineItem;
         long subjectSeqId;
         String subjectSeqIdStr;
         for (int i = 0; i < items.length; i++) {
            reportLineItem = (PropertyReport.ReportLineItem) items[i];
            if (reportLineItem.getOid().equals(featureOID)) {
               //System.out.println("getSubjectSeqId found a match!");
               subjectSeqIdStr = (String) reportLineItem.getValue(HitAlignmentFacade.SUBJECT_SEQ_ID_PROP);
               subjectSeqId = Long.parseLong(subjectSeqIdStr);
               return subjectSeqId;
            }
         }
         return -1l;
      }

      private Feature[] getMatchingFeatures(Report report) {
         long seqIdToMatch = getSubjectSeqId(hitAlignmentFeature.getOid(), report);
         long featSubjSeqId;
         Feature feature;
         GenomeVersion genomeVersion = masterAxis.getGenomeVersion();
         java.util.List featureList = new ArrayList();
         LineItem[] items = report.getLineItems();
         PropertyReport.ReportLineItem reportLineItem;
         //System.out.println("getMatchingFeatures searching num_items=" + items.length);
         for (int i = 0; i < items.length; i++) {
            reportLineItem = (PropertyReport.ReportLineItem) items[i];
            featSubjSeqId = Long.parseLong((String) reportLineItem.getValue(HitAlignmentFacade.SUBJECT_SEQ_ID_PROP));
            if (featSubjSeqId == seqIdToMatch) {
               if (!reportLineItem.getOid().equals(hitAlignmentFeature.getOid())) {
                  feature = (Feature) genomeVersion.getLoadedGenomicEntityForOid(reportLineItem.getOid());
                  featureList.add(feature);
               }
            }
         }
         Feature[] fa = new Feature[featureList.size()];
         featureList.toArray(fa);
         return fa;
      }

      private void highlightSubjectSeqMatches(Report report) {
         Feature[] fa = getMatchingFeatures(report);
         for (int i = 0; i < fa.length; i++) {
            addAdornment(getGlyphFor(fa[i]), COLOR_SELECT_DEFAULT, 4);
         }
      }
   }

   //--- Tier management @todo should be in its own file ---

   public static String getTierNameForPreferences(String tierName) {
      if (tierName.endsWith(REV_TIER_SUFFIX))
         tierName = tierName.substring(0, tierName.length() - REV_TIER_SUFFIX.length());
      return tierName;
   }

   public static TierInfo getTierInfo(String tierName) {
      return ViewPrefMgr.getViewPrefMgr().getTierInfo(getTierNameForPreferences(tierName));
   }

   public TierGlyph getTierGlyph(String tierName, boolean forward) {
      if (!forward)
         tierName += REV_TIER_SUFFIX;

      Object tierObj = forward ? forwardColumn().tierForName(tierName) : reverseColumn().tierForName(tierName);

      if (tierObj == null)
         tierObj = axisColumn().tierForName(tierName);

      if (tierObj == null)
         for (int i = 0; i < hiddenTiers.size(); ++i) {
            if (tierName.equals(((TierGlyph) hiddenTiers.get(i)).name())) {
               tierObj = hiddenTiers.get(i);
               break;
            }
         }

      return (TierGlyph) tierObj;
   }

   public TierGlyph getOrCreateTierGlyph(String tierName, boolean forward) {
      TierGlyph tierGlyph = getTierGlyph(tierName, forward);
      if (tierGlyph == null)
         tierGlyph = createTierGlyph(tierName, forward);
      return tierGlyph;
   }

   public TierGlyph createTierGlyph(String tierName, boolean isForward) {
      return createTierGlyph(getTierInfo(tierName), isForward);
   }

   public TierGlyph getForwardCurationTier() {
      return getTierGlyph(CURATION_TIER_NAME, true);
   }

   public TierGlyph getReverseCurationTier() {
      return getTierGlyph(CURATION_TIER_NAME, false);
   }

   public void reorderTierGlyphs() {
      reorderTiers(forwardColumn());
      reorderTiers(reverseColumn());
      reorderTiers(axisColumn());
   }

   public void hideTier(TierGlyph tierGlyph) {
      changeTierStateInPreferences(tierGlyph, TierInfo.TIER_HIDDEN);
   }

   public void collapseTier(TierGlyph tierGlyph) {
      changeTierStateInPreferences(tierGlyph, TierInfo.TIER_COLLAPSED);
   }

   public void expandTier(TierGlyph tierGlyph) {
      changeTierStateInPreferences(tierGlyph, TierInfo.TIER_EXPANDED);
   }

   public void undockTier(TierGlyph tierGlyph) {
      changeTierDockStateInPreferences(tierGlyph, false);
   }

   public void dockTier(TierGlyph tierGlyph) {
      changeTierDockStateInPreferences(tierGlyph, true);
   }

   private TierGlyph createTierGlyph(TierInfo info, boolean isForward) {
      String tierName = info.getName();
      if (Assert.debug)
         Assert.vAssert(!tierName.equals(AXIS_TIER_NAME));
      if (Assert.slowDebug)
         Assert.vAssert(getTierGlyph(tierName, isForward) == null);
      if (!isForward)
         tierName += REV_TIER_SUFFIX;

      TierGlyph tierGlyph = new TierGlyph(tierName, axisModel(), TierGlyph.COLLAPSED);
      resetTierGlyphPreferences(tierGlyph, info, true);

      //--- Special case: sequence ---
      if (info.getName().equals(SEQUENCE_TIER_NAME)) {
         if (isForward) {
            if (sequenceGlyph != null)
               sequenceGlyph.delete();
            sequenceGlyph = new SequenceGlyph(this, true);
            tierGlyph.addChild(sequenceGlyph);
         }
         else {
            if (reverseSequenceGlyph != null)
               reverseSequenceGlyph.delete();
            reverseSequenceGlyph = new SequenceGlyph(this, false);
            tierGlyph.addChild(reverseSequenceGlyph);
         }
      }
      return tierGlyph;
   }

   private void resetTierGlyphPreferences(TierGlyph tierGlyph, TierInfo tierInfo, boolean justCreated) {
      tierGlyph.setBackground(ViewPrefMgr.getViewPrefMgr().getColor(tierInfo.getBackgroundColor()));
      if (tierGlyph == null)
         return;
      if (tierGlyph.name().equals(AXIS_TIER_NAME))
         return;

      //--- Hide when empty ---
      if (!tierInfo.getHideWhenEmpty())
         tierGlyph.shouldDeleteWhenEmpty(false);
      else {
         if (!tierGlyph.name().equals(AXIS_TIER_NAME)) {
            if (justCreated || tierGlyph.hasGenomicChildren())
               tierGlyph.shouldDeleteWhenEmpty(true);
            else {
               deleteTierGlyph(tierGlyph);
               return;
            }
         }
      }

      //--- Misc ---
      tierGlyph.INTERSPACE = tierInfo.getGlyphSpacer();
      tierGlyph.MINIMUM_HEIGHT = 10;

      int state = tierInfo.getState();

      //--- Special case: Axis ---
      if (state == TierInfo.TIER_HIDDEN && tierGlyph.name().equals(AXIS_TIER_NAME))
         state = TierInfo.TIER_FIXED_SIZE;

      //--- Hidden ---
      if (state == TierInfo.TIER_HIDDEN) {
         if (hiddenTiers.contains(tierGlyph))
            return;
         if (tierGlyph.tierColumn() != null)
            tierGlyph.tierColumn().removeTier(tierGlyph);
         hiddenTiers.add(tierGlyph);
         return;
      }

      //--- Not hidden ---
      if (hiddenTiers.contains(tierGlyph))
         hiddenTiers.remove(tierGlyph);

      //--- Proper column and order ---
      boolean isForward = !tierGlyph.name().endsWith(REV_TIER_SUFFIX);
      int newOrder = ViewPrefMgr.getViewPrefMgr().getViewInfo(VIEW_NAME).getOrderValueForTier(tierInfo.getName());
      if (isForward)
         newOrder = -newOrder;
      TiersColumnGlyph newColumn = tierInfo.getDocked() ? axisColumn() : isForward ? forwardColumn() : reverseColumn();

      TiersColumnGlyph oldColumn = tierGlyph.tierColumn();
      if (oldColumn == newColumn)
         newColumn.setTierOrderIndex(tierGlyph, newOrder);
      else {
         if (oldColumn != null)
            oldColumn.removeTier(tierGlyph);
         newColumn.addTier(tierGlyph, newOrder);
      }

      //--- Special states ---
      if (state == TierInfo.TIER_EXPANDED)
         tierGlyph.expandTier();
      else if (state == TierInfo.TIER_COLLAPSED)
         tierGlyph.collapseTier();
      else if (state == TierInfo.TIER_FIXED_SIZE); //@todo
   }

   private void deleteHiddenTiers() {
      ArrayList hiddenTiersCopy = new ArrayList(hiddenTiers);
      Iterator i = hiddenTiersCopy.iterator();
      while (i.hasNext()) {
         TierGlyph tierGlyph = (TierGlyph) i.next();
         deleteTierGlyph(tierGlyph);
      }

      if (Assert.debug)
         Assert.vAssert(hiddenTiers.isEmpty());
   }

   private void reorderTiers(TiersColumnGlyph column) {
      ViewInfo viewInfo = ViewPrefMgr.getViewPrefMgr().getViewInfo(VIEW_NAME);

      int count = column.tierCount();
      for (int i = 0; i < count; ++i) {
         TierGlyph tier = column.tier(i);
         String tierName = tier.name();
         boolean isForward = true;
         if (tierName.endsWith(REV_TIER_SUFFIX)) {
            isForward = false;
            tierName = tierName.substring(0, tierName.length() - REV_TIER_SUFFIX.length());
         }
         int newOrderIndex = viewInfo.getOrderValueForTier(tierName);
         if (isForward)
            newOrderIndex = -newOrderIndex;
         column.setTierOrderIndex(tier, newOrderIndex);
      }
   }

   private void showSequenceTier() {
      getOrCreateTierGlyph(SEQUENCE_TIER_NAME, true);
      if (getTierInfo(SEQUENCE_TIER_NAME).getMirror())
         getOrCreateTierGlyph(SEQUENCE_TIER_NAME, false);
   }

   private void hideSequenceTier() {
      if (getTierInfo(SEQUENCE_TIER_NAME).getHideWhenEmpty()) {
         deleteTierGlyph(SEQUENCE_TIER_NAME, true);
         deleteTierGlyph(SEQUENCE_TIER_NAME, false);
      }
   }

   private void deleteTierGlyph(String tierName, boolean isForward) {
      if (tierName.equals(AXIS_TIER_NAME))
         return;
      TierGlyph tierGlyph = getTierGlyph(tierName, isForward);
      if (tierGlyph != null)
         deleteTierGlyph(tierGlyph);
   }

   protected void deleteTierGlyph(TierGlyph tierGlyph) {

      if (hiddenTiers.contains(tierGlyph))
         hiddenTiers.remove(tierGlyph);

      if (tierGlyph.name().equals(SEQUENCE_TIER_NAME))
         sequenceGlyph = null;
      else if (tierGlyph.name().equals(SEQUENCE_TIER_NAME + REV_TIER_SUFFIX))
         reverseSequenceGlyph = null;

      tierGlyph.delete();
   }

   public TierGlyph getTierGlyphFor(AlignableGenomicEntity entity) {
      return (entity instanceof Contig) ? getTierGlyphFor((Contig) entity) : getTierGlyphFor((Feature) entity);
   }

   private TierGlyph getTierGlyphFor(Contig contig) {
      String tierName = ViewPrefMgr.getViewPrefMgr().getTierLocationForFeature(VIEW_NAME, CONTIG_TIER_NAME);
      if (tierName == null)
         tierName = CONTIG_TIER_NAME;

      return getOrCreateTierGlyph(tierName, true);
   }

   private TierGlyph getTierGlyphFor(Feature feature) {
      GeometricAlignment align = (GeometricAlignment) findOnlyAlignment(feature);
      if (align == null)
         return null;
      Range range = align.getRangeOnAxis();

      boolean isForward = range.isUnknownOrientation();
      if (!isForward) {
         isForward = range.isForwardOrientation();
         if (isReverseComplement())
            isForward = !isForward;
      }

      return getTierGlyphFor(feature, isForward);
   }

   private TierGlyph getTierGlyphFor(Feature feature, boolean isForward) {
      ViewPrefMgr viewPrefMgr = ViewPrefMgr.getViewPrefMgr();

      String featureGroup = feature.getEnvironment();
      if (featureGroup == null) {
         JOptionPane.showMessageDialog(
            this,
            "Features are attempting to load that have no "
               + PropertyMgr.getPropertyMgr().getPropertyDisplayName(FeatureFacade.GROUP_TAG_PROP)
               + ".\n"
               + "Please check data source or contact our support.",
            "Data Error",
            JOptionPane.WARNING_MESSAGE);
         featureGroup = UNKNOWN_FEATURE_GROUP;
      }
      String tierName = viewPrefMgr.getTierLocationForFeature(VIEW_NAME, featureGroup);
      if (tierName == null || viewPrefMgr.getTierInfo(tierName) == null) {
         tierName = featureGroup;
         if (viewPrefMgr.getTierInfo(tierName) == null)
            viewPrefMgr.createNewFeatureAndTier(tierName, feature.getRootFeature().getEntityType().getEntityName());
      }

      if (Assert.debug)
         Assert.vAssert(viewPrefMgr.getTierInfo(tierName) != null);

      return getOrCreateTierGlyph(tierName, isForward);
   }

   private void changeTierStateInPreferences(TierGlyph tierGlyph, int state) {
      String tierName = getTierNameForPreferences(tierGlyph.name());
      ViewPrefMgr viewPrefMgr = ViewPrefMgr.getViewPrefMgr();
      viewPrefMgr.setTierState(tierName, state);
      viewPrefMgr.commitChanges(true);
      viewPrefMgr.fireTierStateChangeEvent(getTierInfo(tierName));
   }

   private void changeTierDockStateInPreferences(TierGlyph tierGlyph, boolean isDocked) {
      String tierName = getTierNameForPreferences(tierGlyph.name());
      TierInfo tierInfo = getTierInfo(tierName);
      ViewPrefMgr viewPrefMgr = ViewPrefMgr.getViewPrefMgr();
      viewPrefMgr.setTierDocked(tierInfo, isDocked);
      viewPrefMgr.commitChanges(true);
      viewPrefMgr.fireTierStateChangeEvent(tierInfo);
   }

   private void resetTierGlyphState(TierInfo info) {
      String tierName = info.getName();
      boolean forceCreation = !info.getHideWhenEmpty();

      TierGlyph forwardTierGlyph = forceCreation ? getOrCreateTierGlyph(tierName, true) : getTierGlyph(tierName, true);
      TierGlyph reverseTierGlyph = null;
      if (info.getMirror())
         reverseTierGlyph = forceCreation ? getOrCreateTierGlyph(tierName, false) : getTierGlyph(tierName, false);

      if (forwardTierGlyph != null)
         resetTierGlyphPreferences(forwardTierGlyph, info, forceCreation);
      if (reverseTierGlyph != null)
         resetTierGlyphPreferences(reverseTierGlyph, info, forceCreation);
   }

   private void ensureTierGlyphIsNotHidden(TierGlyph tierGlyph) {
      if (getTierInfo(tierGlyph.name()).getState() == TierInfo.TIER_HIDDEN) {
         collapseTier(tierGlyph);
      }
   }

   private void ensureAllTierInfoExist() {
      //@todo what about hiddenTiers???
      TiersColumnGlyph[] allColumns = { forwardColumn(), reverseColumn(), axisColumn()};
      for (int x = 0; x < 3; ++x) {
         TiersColumnGlyph column = allColumns[x];
         for (int i = 0; i < column.tierCount(); i++) {
            TierGlyph tier = column.tier(i);
            if (getTierInfo(tier.name()) == null) {
               Collection genomicChildren = tier.genomicChildren();
               if (genomicChildren.isEmpty())
                  deleteTierGlyph(tier);
               else {
                  GBGenomicGlyph glyph = (GBGenomicGlyph) genomicChildren.iterator().next();
                  getTierGlyphFor(glyph.alignment().getEntity());
               }
            }
         }
      }
   }

   private void setUpTiers() {
      ensureAllTierInfoExist();

      ArrayList orderedTierInfos = ViewPrefMgr.getViewPrefMgr().getOrderedTierInfos(VIEW_NAME);
      for (Iterator it = orderedTierInfos.iterator(); it.hasNext();) {
         TierInfo info = (TierInfo) it.next();
         resetTierGlyphState(info);
      }
   }

   //--- Selection ------------------------------------------------------------

   public GeometricAlignment getAlignmentForCurrentSelection() {
      AlignableGenomicEntity entity = (AlignableGenomicEntity) getBrowserModel().getCurrentSelection();
      return (entity == null) ? null : findOnlyAlignment(entity);
   }

   private void showCurrentSelectionAgain() {
      removeAllAdornments();
      wasSelectionGeneratedInThisView = true;
      showSelection();
   }

   void setCurrentSelection(AlignableGenomicEntity entity) {
      if (entity == null)
         return;
      if (entity != getBrowserModel().getCurrentSelection()) {
         wasSelectionGeneratedInThisView = true;
         if (Assert.debug && entity != null) {
            Set set = entity.getAlignmentsToAxis(masterAxis);
            Assert.vAssert(set != null && !set.isEmpty());
         }
         getBrowserModel().setCurrentSelection(entity);
      }
   }

   void selectGlyph(GBGenomicGlyph picked, boolean withShift, boolean withControl, boolean requestZoom) {
      AlignableGenomicEntity newSelection = null;

      if (picked != null) {
         GeometricAlignment align = picked.alignment();
         newSelection = (align == null) ? null : align.getEntity();

         if (newSelection instanceof Feature) {
            Feature feature = (Feature) newSelection;
            if (withControl) {
               while (feature.getSuperFeature() != null) {
                  feature = feature.getSuperFeature();
               }
            }
            else if (withShift) {
               if (!feature.hasSubFeatures() && feature.getSuperFeature() != null)
                  feature = feature.getSuperFeature();
            }

            newSelection = feature;
         }
      }

      setCurrentSelection(newSelection);
      if (requestZoom)
         zoomToSelection();
   }

   public void showSelectionAndZoom() {
      isZoomToSelectionRequired = true;
      showSelection();
   }

   private void showSelection() {
      adaptMenusToCurrentSelection();

      GBGenomicGlyph glyph = genomicGlyphForSelection();
      if (glyph == null) {
         removeAllAdornments();
         currentGlyphSelection = null;
         return; //the corresponding genomic glyph has not been created yet
      }

      if (glyph == currentGlyphSelection) {
         isZoomToSelectionRequired = false;
         return;
      }

      showGlyphAsSelected(glyph);
      if (!wasSelectionGeneratedInThisView)
         ensureSelectionIsVisible();

      if (wasSelectionGeneratedInThisView)
         wasSelectionGeneratedInThisView = false;
      else {
         if (optionsMenu.getZoomToSubviewSelectionState())
            zoomToSelection();
      }

      if (isZoomToSelectionRequired) {
         zoomToSelection();
         isZoomToSelectionRequired = false;
      }
   }

   private GBGenomicGlyph genomicGlyphForSelection() {
      return glyphFactory.getCurrentGlyphFor((AlignableGenomicEntity) getBrowserModel().getCurrentSelection());
   }

   private void removeAllAdornments() {
      Iterator i = adornmentGlyphs.iterator();
      while (i.hasNext()) {
         ((Glyph) i.next()).delete();
      }
      adornmentGlyphs.clear();

      currentGlyphSelection = null;
   }

   private void showGlyphAsSelected(GBGenomicGlyph glyph) {
      if (glyph == currentGlyphSelection)
         return;
      removeAllAdornments();
      currentGlyphSelection = glyph;
      addAdornment(glyph, 4);
      showSecondaryAdornmentsForSelection();
   }

   private void adaptMenusToCurrentSelection() {
      AlignableGenomicEntity selection = (AlignableGenomicEntity) getBrowserModel().getCurrentSelection();
      GeometricAlignment alignment = (selection == null) ? null : findOnlyAlignment(selection);
      if (alignment == null) {
         optionsMenu.disableLockToSelection();

      }
      else {
         optionsMenu.enableLockToSelection();

      }
   }

   private void showSecondaryAdornmentsForSelection() {
      AlignableGenomicEntity selection = (AlignableGenomicEntity) getBrowserModel().getCurrentSelection();
      if (selection instanceof Feature) {
         if (isHighlightEvidenceOn())
            highlightEvidence((Feature) selection);
         if (isShowEdgeMatchesOn())
            showEdgeMatches((Feature) selection);
         if (isShowEvidenceEdgeMismatchesOn())
            showEvidenceEdgeMismatches((Feature) selection);
         // Be sure to remove the instanceof when testing the splice calculation.
         //  Select a calculated splice and of course the edges should be splice sites.
         if (isShowSpliceMismatchesOn() && (selection instanceof CuratedFeature))
            showSpliceMismatches((Feature) selection);
      }
   }

   private void addAdornment(ParentGlyph glyph, Color color, int gap) {
      if (glyph != null) {
         Glyph adornment = new AdornmentGlyph(glyph, color, gap);
         glyph.addChild(adornment);
         adornmentGlyphs.add(adornment);
      }
   }

   private void addAdornment(GBGenomicGlyph glyph, int gap) {
      if (glyph == null)
         return;
      GenomicEntity entity = glyph.alignment().getEntity();
      Color color = COLOR_SELECT_DEFAULT;
      if (entity instanceof CuratedGene) {
         CuratedGene geneEntity = (CuratedGene) entity;
         if (geneEntity.isObsoletedByWorkspace())
            color = COLOR_OBSOLETE;
         else
            color = COLOR_SELECT_GENE;
      }
      else if (entity instanceof SuperFeature)
         color = COLOR_SELECT_COMPOSITE;
      addAdornment(glyph, color, gap);
   }

   void zoomToSelection() {
      GeometricAlignment alignment = getAlignmentForCurrentSelection();
      if (alignment != null) {
         zoomToAlignment(alignment);
      }
   }

   private void centerOnSelection() {
      GeometricAlignment alignment = getAlignmentForCurrentSelection();
      if (alignment == null)
         return;
      MutableRange range = alignment.getRangeOnAxis().toMutableRange();
      if (isReverseComplement())
         range.mirror(masterAxis.getMagnitude());
      centerOnRange(range);
      centerFeatureInVertical(alignment);
   }

   private void ensureSelectionIsVisible() {
      if (currentGlyphSelection == null)
         return;

      TierGlyph tier = currentGlyphSelection.tierAncestor();
      if (tier == null)
         return;
      TiersColumnGlyph column = tier.tierColumn();
      //  The block below exposes a hidden tier when a person tries to navigate
      //  to a feature within it.
      if (column == null) {
         String forwardName = getTierNameForPreferences(tier.name());
         TierInfo tmpInfo = ViewPrefMgr.getViewPrefMgr().getTierInfo(forwardName);
         ViewPrefMgr.getViewPrefMgr().setTierState(forwardName, TierGlyph.COLLAPSED);
         ViewPrefMgr.getViewPrefMgr().fireTierStateChangeEvent(tmpInfo);
         column = tier.tierColumn();
      }
      TiersComponent component =
         (column == forwardColumn())
            ? forwardTable().tiersComponent()
            : (column == reverseColumn())
            ? reverseTable().tiersComponent()
            : axisTable().tiersComponent();
      ensureComponentIsVisible(component);

      boolean isSelectionVisible = true;
      Bounds bounds = currentGlyphSelection.getBounds();

      WorldViewModel horizontalModel = axisModel();
      if (bounds.getMaxX() < horizontalModel.origin() || bounds.getMinX() > horizontalModel.viewEnd())
         isSelectionVisible = false;
      if (isSelectionVisible) {
         WorldViewModel verticalModel = component.verticalModel();
         double ty = column.yForTier(tier) + determineVerticalTierTranslationFor(currentGlyphSelection);
         if (ty + bounds.getMaxY() < verticalModel.origin() || ty + bounds.getMinY() > verticalModel.viewEnd())
            isSelectionVisible = false;
         //@todo not just y > viewEnd but y + MINSIZE > viewEnd
      }

      if (!isSelectionVisible)
         centerOnSelection();
   }

   private double determineVerticalTierTranslationFor(GBGenomicGlyph glyph) {
      double ty = 0;
      for (ParentGlyph parent = glyph.parent(); parent != null && !(parent instanceof TierGlyph); parent = parent.parent()) {
         if (parent instanceof TranslationGlyph)
            ty += ((TranslationGlyph) parent).ty();
      }
      return ty;
   }

   boolean lockToSelection(boolean lock) {
      GeometricAlignment alignment = getAlignmentForCurrentSelection();
      if (lock) {
         if (alignment != null) {
            MutableRange range = alignment.getRangeOnAxis().toMutableRange();
            if (isReverseComplement())
               range.mirror(masterAxis.getMagnitude());
            int min = range.getMinimum();
            int max = range.getMaximum();
            int extra = (max - min) / 10;
            min = Math.max(0, min - extra / 2);
            max = Math.min(masterAxis.getMagnitude(), max + extra / 2);
            axisModel().setWorld(min, max - min);
            axisModel().showEverything();
         }
      }
      else {
         axisModel().setWorld(0, masterAxis.getMagnitude());
         if (alignment == null)
            optionsMenu.disableLockToSelection();
      }

      return lock;
   }

   private void highlightEdge(final GeometricAlignment featureAlignment, final boolean highlightStart, Color col, int pixelGap) {
      final GBGenomicGlyph featureGlyph = getGlyphFor(featureAlignment.getEntity());
      if (featureGlyph == null)
         return;

      AdornmentGlyph adornment = new AdornmentGlyph(featureGlyph, col, pixelGap) {
         public Bounds targetBounds() {
            Bounds b = super.targetBounds();
            MutableRange featureRange = new MutableRange(featureAlignment.getRangeOnAxis());
            if (isReverseComplement())
               featureRange.mirror(masterAxis.getMagnitude());
            b.x = (highlightStart ? featureRange.getStart() : featureRange.getEnd());
            b.width = 0;
            return b;
         }
      };

      featureGlyph.addChild(adornment);
      adornmentGlyphs.add(adornment);
   }

   private class MyFeatureAdapter implements FeatureAdapter {
      private Color fadeColor = new Color(255, 255, 0);
      private int myStart = 0;
      private int myEnd = 0;

      public MyFeatureAdapter(Range myRange) {
         myStart = myRange.getStart();
         myEnd = myRange.getEnd();
      }

      public int start() {
         return myStart;
      }
      public int end() {
         return myEnd;
      }
      public double height() {
         return 10;
      }
      public Color color() {
         return fadeColor;
      }
      public void setColor(Color newColor) {
         this.fadeColor = newColor;
      }
   }
}
