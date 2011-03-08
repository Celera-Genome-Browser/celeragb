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

import api.entity_model.access.command.*;
import api.entity_model.access.observer.ModifyManagerObserver;
import api.entity_model.access.observer.ModifyManagerObserverAdapter;
import api.entity_model.management.*;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.*;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.AlignableGenomicEntity;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.EntityTypeSet;
import api.entity_model.model.fundtype.GenomicEntity;
import api.facade.abstract_facade.annotations.GeneFacade;
import api.stub.data.GenomicProperty;
import api.stub.data.ReplacementRelationship;
import api.stub.data.Util;
import api.stub.geometry.MutableRange;
import api.stub.geometry.Range;
import client.gui.framework.browser.Browser;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.BrowserModelListenerAdapter;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.framework.session_mgr.SessionModelListener;
import vizard.ParentGlyph;
import vizard.genomics.glyph.TierGlyph;
import vizard.util.Assert;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 *  XyzCurationHandlers are in charge of user creation and editing of curated annotations on Xyz editor/views
 *  GenomicAxisAnnotCurationHandler handles curation activity on GenomicAxisAnnotationView
 *  It listens to currentFeature Selection and pops up the right menu for it.
 *  It gets notified of the endFeatureDrag and initiates model changes.
 *  All command invocations occur here -(ie mutations to the model)
 */
public class GenomicAxisAnnotCurationHandler implements ActionListener
{
    private Browser browser;
    private BrowserModel browserModel;
    private static final String CURATION_ENABLED = "CurationEnabled";

    private static String REVIEWED_COMMAND = "Reviewed By Cascade";
    private GenomicEntity reviewedEntity;

    private GenomicAxisAnnotationEditor editor;
    private GenomicAxisAnnotationView view;
    private JMenu[] menus;

    private JMenuItem curateGeneMI;
    //List of popUpMenus for Exons
    //exons can be parented  and they
    //can come from promoted or created from
    //precomputes
    private ArrayList curatedExonMenuItems = new ArrayList();//this is for exons created from precomputes
    private ArrayList curatedExonFromPromotedMenuItems = new ArrayList();

    //menu items for exons from precomputes or scratch
    private JMenuItem exonInfoMI;
    private JMenuItem deleteExonMI;


   //menu items for exons from promoted exons
    private JMenuItem promotedExonInfoMI;
    private JMenuItem deleteExonFromPromotedMI;
    private JMenuItem obsoleteExonMI;


    private ArrayList curatedTranscriptMenuItems = new ArrayList();
    private JMenuItem cascadeReviewedByMI;
    private JMenuItem attachAllTranscriptsToGeneMI;
    private JMenuItem  attachAllTranscriptsToGenePromotedMI;
    private JMenuItem transcriptInfoMI;
    private JMenuItem deleteTranscriptMI;
    private JMenuItem splitTranscriptMI;
    private JMenuItem mergeTranscriptMI;
    private JMenuItem geneCurationTranscriptMI;
    private JMenuItem deleteStartCodonMI;
    private JMenuItem createNewGeneMI;
    private JMenuItem attachTranscriptToGeneMI;
    private JMenuItem detachTranscriptToGeneMI;
    private JMenuItem geneOptionsMenuItems;

    private ArrayList curatedTranscriptFromPromotedMenuItems = new ArrayList();
    //menu items for transcript created from promoted
    private JMenuItem cascadeReviewedByPromotedMI;
    private JMenuItem transcriptPromotedInfoMI;
    private JMenuItem deletePromotedTranscriptMI;

    private JMenuItem splitPromotedTranscriptMI;
    private JMenuItem mergePromotedTranscriptMI;
    private JMenuItem deleteStartCodonOnPromotedDragMI;

    private JMenuItem createNewGenePromotedMI;
    private JMenuItem attachTranscriptToGenePromotedMI;
    private JMenuItem detachTranscriptToGenePromotedMI;
    private JMenuItem geneOptionsPromotedMenuItems;

    private ArrayList evidenceMenuItems = new ArrayList();           // Menu which pops up over evidence of curation.
    private JMenuItem evidenceInfoMI;
    private JMenuItem setLeftEdgeMI;
    private JMenuItem setRightEdgeMI;
    private JMenuItem removeAsEvidenceMI;
    private SessionModelListener sessionModelListener = new MySessionModelListener();
    private ArrayList evidencesForAllExonsMenuItems = new ArrayList();           // Menu which pops up over evidences for transcript.
    private JMenuItem evidenceOptionsMI;
    private JMenuItem setAllExonsLeftEdgeMI;
    private JMenuItem setAllExonsRightEdgeMI;
    private JMenuItem swapEvidenceForWorkspaceMI;
    private Alignment selectedFeatureAlign;
    private Alignment current_curation;
    private Alignment currentEvidenceAlign;
    private Collection currentEvidencesForTranscript =null;
    private boolean curationEnabled;
    private int axisPosition;
    private AlignableGenomicEntity selectionWhenCommandStarted;

    private Controller dragFeatureController;
    private Controller glyphPopupMenuController;

    private ArrayList validTranscriptsTobeAttachedToGene;
    // This attribute maintains which entity was moused over in order to assist
    // in popup menu actions.
    private Feature popupFeature;


    private BrowserModelListenerAdapter browserModelListener = new BrowserModelListenerAdapter() {
            public void browserCurrentSelectionChanged(GenomicEntity selection) {
                if (!(selection instanceof Feature)) {
                    selectedFeatureAlign = null;
                    current_curation = null;
                    return;
                }

                Alignment alignment = findOnlyAlignment((AlignableGenomicEntity)selection);
                if (alignment == null) {
                    selectedFeatureAlign = null;
                    current_curation = null;
                    return;
                }

                selectedFeatureAlign = alignment;
                current_curation = alignment;

                Alignment featAlignment = alignment;
                // Testing for entity re-set, now that drag-down is over.
                // NOTE: before, this was up in the test loop with !isScratch.  This prevented
                // any workspace tier objects from becoming the current curation.  Out here, they can be. LLF
                if (featAlignment.getEntity() instanceof CuratedTranscript || featAlignment.getEntity() instanceof CuratedExon) {
                   current_curation = featAlignment;

                }
                else {
                    current_curation = null;
                }
            }
        };

    private ModifyManagerObserver commandPostconditionHook = new ModifyManagerObserverAdapter() {
            public void noteCommandDidFinish(String commandName, int commandKind) {
                if (Command.getFocusEntity() != null) {
                    view.setCurrentSelection((AlignableGenomicEntity)Command.getFocusEntity());
                }
                if (commandName.equals(REVIEWED_COMMAND)) {
                  browserModel.setCurrentSelection(reviewedEntity);
                }
            }
        };

    public GenomicAxisAnnotCurationHandler(Browser browser, GenomicAxisAnnotationEditor editor) {
        this.browser = browser;
        this.browserModel = browser.getBrowserModel();
        SessionMgr.getSessionMgr().addSessionModelListener(sessionModelListener);
        this.editor = editor;
        this.view = editor.getView();
        if (SessionMgr.getSessionMgr().getModelProperty(CURATION_ENABLED) == null)
            SessionMgr.getSessionMgr().setModelProperty(CURATION_ENABLED,
                                                          new Boolean(curationEnabled));
        else
            curationEnabled = ((Boolean)SessionMgr.getSessionMgr().getModelProperty(
                                    CURATION_ENABLED)).booleanValue();
        setUpMenus();

        browserModel.addBrowserModelListener(browserModelListener);
        ModifyManager.getModifyMgr().addObserver(commandPostconditionHook);

        setupControllers();
    }

    private void setupControllers() {
      TierGlyph forwardWorkspace = view.getTierGlyph("Workspace", true);
      TierGlyph reverseWorkspace = view.getTierGlyph("Workspace", false);
      if (Assert.debug) {
          Assert.vAssert(forwardWorkspace != null);
      }
      dragFeatureController = new DragFeatureController(this, forwardWorkspace, reverseWorkspace);

      glyphPopupMenuController = new GlyphPopupMenuController(this);
    }


    /**
     * Find only aligned range for a given axis
     */
    private Alignment findOnlyAlignment(AlignableGenomicEntity entity) {
        if (entity == null) return null;

        GenomicAxis axis = (GenomicAxis) browserModel.getMasterEditorEntity();
        if (axis == null) return null;
        Collection alignments = entity.getAlignmentsToAxis(axis);

        if (alignments.size() != 1) {
          System.out.println("WARNING: Entity " + entity + " has " + alignments.size() + " alignments on axis " + axis);
          return null;
        }
        return (Alignment) alignments.iterator().next();
    }


    public GenomicAxisAnnotationView getView() {
        return view;
    }


    /**
     * After a feature is dragged and released, figure out what (if any) curational action should be taken Possibilities:
     * if computational result was dragged if drag was into curation tier if into empty space --> a) create a new curation
     * b) each leaf of dragged entity becomes evidence for leaf of new curation if dragged onto existing curation
     * if dragged entity is leaf if only overlaps single leaf of curation --> a) add as evidence b) edge determination (?)
     * if dragged entity is composite see addSpliceSite() method for logic when predicted splice sites are dragged to curations
     */
    public void endFeatureDrag(GBGenomicGlyph draggedGlyph, TierGlyph destinationTier) {
        boolean forwardCurationTier = destinationTier == view.getForwardCurationTier();
        boolean reverseCurationTier = destinationTier == view.getReverseCurationTier();
        if (!forwardCurationTier && !reverseCurationTier)
            return;

        selectedFeatureAlign = draggedGlyph.alignment();
        Feature selectedFeatEntity = (Feature)selectedFeatureAlign.getEntity();


        EntityTypeSet transientTypes = EntityTypeSet.getEntityTypeSet("TransientFeatureTypes");

        if (transientTypes != null && transientTypes.contains(selectedFeatEntity.getEntityType())) {
            return;  //can't use a transient type to make a new curation
        }

        if (!(selectedFeatEntity instanceof CuratedTranscript)&&
            !(selectedFeatEntity instanceof CuratedExon) &&
            !(selectedFeatEntity instanceof CuratedCodon) &&
            !(selectedFeatEntity instanceof CuratedGene))
        {
            ModifyManager.getModifyMgr().doCommand(new DoCreateNewCurationAndAlign(selectedFeatureAlign));
        }
        else {
            GeometricAlignment featureAlignment = (GeometricAlignment)selectedFeatureAlign;
            Range featureRange = featureAlignment.getRangeOnAxis();
            Boolean axisRevComp = (Boolean)browser.getBrowserModel().getModelProperty(BrowserModel.REV_COMP_PROPERTY);
            if (axisRevComp == null)
                throw new IllegalStateException("Model property GAAnnotRevComped is not set");
            //return if drag across axis
            if (!axisRevComp.booleanValue() && !(forwardCurationTier  == !featureRange.isReversed()))
                return;
            if (axisRevComp.booleanValue() && (forwardCurationTier  == !featureRange.isReversed()))
                return;
            ModifyManager.getModifyMgr().doCommand(new DoMakeNewGeneFromPromoted(
                       selectedFeatureAlign.getAxis(), (Feature)selectedFeatureAlign.getEntity()));
        }
    }

    public void endFeatureDrag(GBGenomicGlyph draggedGlyph, GBGenomicGlyph destinationGlyph) {
        selectedFeatureAlign = draggedGlyph.alignment();

        TierGlyph destinationTier = destinationGlyph.tierAncestor();
        boolean forwardCurationTier = destinationTier == view.getForwardCurationTier();
        boolean reverseCurationTier = destinationTier == view.getReverseCurationTier();
        if (!forwardCurationTier && !reverseCurationTier){

           //only show this message if user is trying to drag evidence on the promoted feature
           if(!(draggedGlyph.alignment().getEntity() instanceof CuratedFeature) && destinationGlyph.alignment().getEntity() instanceof CuratedFeature
              &&  !(destinationGlyph.alignment().getEntity().isWorkspace())){
             JOptionPane.showMessageDialog(browser, "Drag evidence on workspace features only!",
                    "End Feature Drag Warning", JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        // "CuratedTranscript" is the only useful destination
        boolean foundTranscript = false;
        for(ParentGlyph p = destinationGlyph; p != null; p = p.parent()) {
            if (!(p instanceof GBGenomicGlyph))
                continue;
            GBGenomicGlyph gg = (GBGenomicGlyph)p;
            GeometricAlignment aa = gg.alignment();
            if (aa.getEntity() instanceof CuratedTranscript) {
                foundTranscript = true;
                destinationGlyph = gg;
                break;
            }
        }
        if (!foundTranscript)
            return;

        GeometricAlignment aa = destinationGlyph.alignment();

        if ((aa!=null) && (aa.getEntity() instanceof CuratedTranscript)) {
            CuratedTranscript transcript = (CuratedTranscript)aa.getEntity();
            Feature fe = (Feature) selectedFeatureAlign.getEntity();
            if ((fe.getEntityType() == EntityType.getEntityTypeForName("_DONOR_SPLICE_SITE")) ||
                (fe.getEntityType() == EntityType.getEntityTypeForName("_ACCEPTOR_SPLICE_SITE"))) {
                addSpliceSite(selectedFeatureAlign, transcript);
            }
            else if (fe.getEntityType() == EntityType.getEntityTypeForName("_COMPUTED_START_CODON")) {
                if (transcript.isWorkspace()) {
                    ModifyManager.getModifyMgr().doCommand(new
                    DoAddStartSite(selectedFeatureAlign.getAxis(),
                                   (ComputedCodon)selectedFeatureAlign.getEntity(),
                                   transcript));

                }
            }
            else if (fe.getEntityType() == EntityType.getEntityTypeForName("_COMPUTED_STOP_CODON")) {
                if (transcript.isWorkspace()) {
                    ModifyManager.getModifyMgr().doCommand(new
                    DoAddStopSite(selectedFeatureAlign.getAxis(),
                                   (ComputedCodon)selectedFeatureAlign.getEntity(),
                                   transcript));

                }
            }
            else {
                if (selectedFeatureAlign.getEntity() instanceof Feature){}


                   ModifyManager.getModifyMgr().doCommand(new DoAddEvidenceAndCreateExonIfNecessary(
                        selectedFeatureAlign.getAxis(), (Feature)selectedFeatureAlign.getEntity(), transcript));

            }
        }
    }


    /**
     * Definitions for splice sites: upstream == smaller numbers == farther 5'  (on forward strand)
     * downstream == larger numbers == farther 3' (on forward strand)
     * acceptor site == splice site at 5' edge of all but first exon ==>
     * canonical AG immediately upstream of actual splice site donor site == splice site at 3' edge of all but last exon ==>
     * canonical GT immediately downstream of splice site Rules for curating with predicted splice sites:
     * parent of predicted splice site and curated exon must be same contig entity(no longer true with GA)
     * if site orientation and transcript orientation on site's parent disagree, then can't use this splice site
     * if site overlaps an exon, it is adjusting that exon's 3' edge (if donor) or 5' edge (if acceptor)
     * otherwise it is expanding nearest upstream exon's 3' edge (if donor) or
     * downstream exon's 5' edge (if acceptor), iff the exon shares same parent contig as predicted splice sites edge cases:
     * if acceptor and downstream of last exon, no change if donor and upstream of first exon, no change
     * PredictedSpliceSiteFeatPI still under development -- assuming they are all on forward strand for now
     */
    void addSpliceSite(Alignment site, CuratedTranscript transcript) {
         //NEEDS TO BE REIMPLEMENTED FOR THE ENTITY MODEL
        // Get the Genomic Axis...
        GenomicEntity entity = this.browserModel.getMasterEditorEntity();
        if ( ( entity != null ) && !( entity instanceof GenomicAxis ) ) {
           System.out.println("Error: GenomicAxisAnnotCurationHandler.addSpliceSite(); Master Editor Entity is not a GenomicAxis.");
           return;
        }

        // get range and parent of splice site
        // Find the current range and direction...
        // Expecting 1 and only 1...
        GeometricAlignment siteAlign = (GeometricAlignment) site;
        Range siteAxisRange = siteAlign.getRangeOnAxis();

        Feature siteFeature = (Feature) site.getEntity();
        boolean is_acceptor = (siteFeature.getEntityType()== EntityType.getEntityTypeForName("_ACCEPTOR_SPLICE_SITE"));
        boolean forward_site = siteAxisRange.isForwardOrientation();
        Collection transcriptExons = transcript.getSubFeatures();
        Iterator exonIter=transcriptExons.iterator();
        CuratedExon exon;
        boolean forward_exon;
        CuratedExon containing_exon = null;
        CuratedExon nearest_exon = null;

        Range exonAxisRange = null;
        MutableRange nearestAxisRange = null;

        boolean contained_in_nearest_exon = false;
        while (exonIter.hasNext()) {
            exon = (CuratedExon)exonIter.next();

            Alignment exonAlignment =exon.getOnlyAlignmentToOnlyAxis();
            if (exonAlignment == null) {
               System.err.println("GenomicAxisAnnotCurationHandler ERROR: Expecting 1 previous Alignment for Exon, have zero or more than 1.");
               return;
            }
            exonAxisRange = ((GeometricAlignment)exonAlignment).getRangeOnAxis();
            forward_exon = (exonAxisRange.getStart() <= exonAxisRange.getEnd());
            // site and exon must be same orientation
            // (and if one exon is not, they'll _all_ be like that, so terminate
            if ((forward_exon && !forward_site) || (!forward_exon && forward_site)) {
                    System.out.println("splice site and transcript are on different strands -- " + "can't use splice site to adjust exon edges");
                    nearest_exon = null;
                    break;
            }

            if (exonAxisRange.contains(siteAxisRange)) {
                    contained_in_nearest_exon = true;
                    nearest_exon = exon;
                    break;
            }
            // if acceptor and on forward strand (or donor and on reverse strand),
            // keep track of nearest downstream exon (compare 5' ends)
            if ((is_acceptor && forward_site) || (!is_acceptor && !forward_site)) {
                    // if exon is not downstream, continue to next one
                    if (exonAxisRange.getStart() < siteAxisRange.getEnd()) { continue; }
                    // otherwise, if no nearest_exon yet, use this one
                    if (nearest_exon == null) {
                        nearest_exon = exon;
                        nearestAxisRange = new MutableRange(exonAxisRange.getStart(), exonAxisRange.getEnd());
                    }
                    // if already have a candidate for nearest exon, compare to this one
                    else if (exonAxisRange.getStart() < nearestAxisRange.getStart()) {
                        nearest_exon = exon;
                        nearestAxisRange.change(exonAxisRange.getStart(), exonAxisRange.getEnd());
                    }
            }
            // else donor and on forward strand (or acceptor and on reverse strand),
            // keep track of nearest upstream exon (compare 3' ends)
            else {
                    // if exon is not upstream, continue to next one
                    if (exonAxisRange.getEnd() > siteAxisRange.getStart()) { continue; }
                    // otherwise, if no nearest_exon yet, use this one
                    if (nearest_exon == null) {
                        nearest_exon = exon;
                        nearestAxisRange = new MutableRange(exonAxisRange.getStart(), exonAxisRange.getEnd());
                    }
                    // if already have a candidate for nearest exon, compare to this one
                    else if (exonAxisRange.getEnd() > nearestAxisRange.getEnd()) {
                        nearest_exon = exon;
                        nearestAxisRange.change(exonAxisRange.getStart(), exonAxisRange.getEnd());
                    }
            }
        }
        if (contained_in_nearest_exon) {
            System.out.println("splice landed in exon: " + containing_exon + ", " + exonAxisRange + ", applying splice");
        }
        else {
            System.out.println("trying to apply site to nearest exon: acceptor = " + is_acceptor + ", " + nearest_exon + ", " + nearestAxisRange);
        }
        if (nearest_exon != null) {
            DoModifyEntityEdges com;
            int start, end;
            // Make sure it's in the scratch tier...
            if (!nearest_exon.isScratch()) {
                JOptionPane.showMessageDialog(browser, "Changes must be made in the Scratch Tier.",
                    "Invalid Adjustment", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // OLD: nearest_exon.getRangeOnAncestor(nearest_exon.getParent(), false, exonAxisRange);
            Alignment nearestExonAlignment =nearest_exon.getOnlyAlignmentToOnlyAxis();
            if (nearestExonAlignment== null) {
               System.err.println("GenomicAxisAnnotCurationHandler ERROR: Expecting 1 previous Alignment for Nearest Exon, have zero or more than 1.");
               return;
            }
            exonAxisRange = ((GeometricAlignment)nearestExonAlignment).getRangeOnAxis();
            if (is_acceptor) {
                start = siteAxisRange.getStart();
                end = exonAxisRange.getEnd();
            }
            else {
                start = exonAxisRange.getStart();
                end = siteAxisRange.getEnd();
            }
             /**
              * @todo: Fix when the method on transcript is added
              */
             //if (transcript.isStartCodonValid(genomicAxisPI, nearest_exon, newRng)) {
                com = new DoModifyEntityEdges(nearest_exon, start, end);
                ModifyManager.getModifyMgr().doCommand(com);

            return;
        }
        else {
            System.out.println("couldn't find any containing exon or nearest exon appropriate " + "for splice type");
        }

    }


    /**
     *  Adjust left or right edge of curation _curentity_ based on edge
     * of evidence _featentity_  Note: we use left and right instead of 5' 3' because this
     * operation independent of strand. So for example, we can set the left edge of a feature on the
     * forward strand with evidence from the reverse strand.
     */
    private Command adjustCurationEdge(GeometricAlignment exonAlignment, GeometricAlignment evidenceAlignment, boolean setLeftEdge) {
        // Check for valid arguments...
        if (exonAlignment == null || evidenceAlignment == null) {
            return null;
        }
        if (!(exonAlignment.getEntity() instanceof CuratedExon)) {
          System.out.println("Do not have an Exon.");
        }
        CuratedExon theExon = (CuratedExon)exonAlignment.getEntity();

        // Need to figure out if we are adjusting the start or end...
        boolean adjustStart = setLeftEdge;

        // Check if the display is reverse complimented and roll it into the adjustStart flag...
        Boolean scaffRevComp = (Boolean)browser.getBrowserModel().getModelProperty(BrowserModel.REV_COMP_PROPERTY);
        if (scaffRevComp == null) throw new IllegalStateException("Model property GAAnnotRevComped is not set");
        if (scaffRevComp.booleanValue()) {
          adjustStart = !adjustStart;
        }

        // Check to see if the exon is reversed and roll it into the adjustStart flag...
        if (exonAlignment.getRangeOnAxis().isReversed()) {
          adjustStart = !adjustStart;
        }


        boolean evidenceDiffOrientation = false;
        if (exonAlignment.getRangeOnAxis().isReversed() != evidenceAlignment.getRangeOnAxis().isReversed()) evidenceDiffOrientation = true;

        // Build the new desired range...
        int newStart = exonAlignment.getRangeOnAxis().getStart();
        int newEnd = exonAlignment.getRangeOnAxis().getEnd();
        if (adjustStart) {
          if(evidenceDiffOrientation==false){
          newStart = evidenceAlignment.getRangeOnAxis().getStart();
         }else{
           newStart =  evidenceAlignment.getRangeOnAxis().getEnd();
         }
        }
        else {
          if(evidenceDiffOrientation==false){
          newEnd = evidenceAlignment.getRangeOnAxis().getEnd();
         }else{
             newEnd = evidenceAlignment.getRangeOnAxis().getStart();
         }
        }

        // Build and execute the command...
        /*ModifyManager.getModifyMgr().doCommand*/ return(new DoModifyEntityEdges(theExon, newStart, newEnd));
    }

    /** Helper method to show the curated transcript popup with "wrapped" behavior.
     * This method tests the state of the transcript, depending on whether it is derived
     * from promoted or scratch or if it has codon or its parented by gene it will disable/
     * enable the relevant menu items.
     */
    private ArrayList getCuratedTranscriptMenuItems(Component comp, Point p) {
        //Case1: transcript is made from promoted
        if (((CuratedTranscript)current_curation.getEntity()).isScratchReplacingPromoted())
           {

             CuratedCodon c=((CuratedTranscript)current_curation.getEntity()).getStartCodon();
             if(c==null){
               deleteStartCodonOnPromotedDragMI.setEnabled(false);
               deleteStartCodonMI.setEnabled(false);
             }
             if(c!=null && c.isScratchReplacingPromoted()){
               deleteStartCodonOnPromotedDragMI.setEnabled(true);
             }else if(c!=null && c.isScratch()){
               deleteStartCodonMI.setEnabled(true);
             }
             if(((Feature)current_curation.getEntity()).getSuperFeature() instanceof CuratedGene){
               geneOptionsPromotedMenuItems.setEnabled(true);
               cascadeReviewedByPromotedMI.setEnabled(true);
               boolean b=canBulkAttachTranscriptsToGene((CuratedGene)((Feature)current_curation.getEntity()).getSuperFeature());

               if(b){
                  attachAllTranscriptsToGenePromotedMI.setEnabled(true);
               }else{
                   attachAllTranscriptsToGenePromotedMI.setEnabled(false);
               }

            }else{
               geneOptionsPromotedMenuItems.setEnabled(false);
               cascadeReviewedByPromotedMI.setEnabled(false);
               attachAllTranscriptsToGenePromotedMI.setEnabled(false);
            }

            // Delegate to the DetachTranscriptFromGeneCommand to prevalidate and
            // decide if the menuitem detachTranscriptFromgene should be grayed or not
            Command command=new DoDetachTranscriptFromGene(
                          current_curation.getAxis(),
                          (CuratedTranscript)current_curation.getEntity());
            try{
              command.validatePreconditions();
              detachTranscriptToGenePromotedMI.setEnabled(true);
            }catch(CommandPreconditionException ex){
              detachTranscriptToGenePromotedMI.setEnabled(false);
            }
             command=null;
            // Delegate to the DoCreateNewGene to prevalidate and
            // decide if the menuitem create new gene should be grayed or not
            Command commandnewgene=new DoCreateNewGene(
                          current_curation.getAxis(),
                          (CuratedTranscript)current_curation.getEntity());
            try{
              commandnewgene.validatePreconditions();
              createNewGenePromotedMI.setEnabled(true);
            }catch(CommandPreconditionException ex){
              createNewGenePromotedMI.setEnabled(false);
            }
            commandnewgene=null;

            Command commandsplit=new DoSplitCurationAt(
                            current_curation.getAxis(),
                            (CuratedTranscript)current_curation.getEntity(),
                            axisPosition);
            // donot allow split if transcript has only one exon
             try{
              commandsplit.validatePreconditions();
              splitPromotedTranscriptMI.setEnabled(true);
            }catch(CommandPreconditionException ex){
              splitPromotedTranscriptMI.setEnabled(false);
            }
            commandsplit=null;

            if(selectedFeatureAlign!=null && current_curation!=null && selectedFeatureAlign.getEntity() instanceof CuratedGene
                && current_curation.getEntity() instanceof CuratedTranscript && ((CuratedTranscript)current_curation.getEntity()).getSuperFeature()==null){
               attachTranscriptToGenePromotedMI.setEnabled(true);
            }else{
                 attachTranscriptToGenePromotedMI.setEnabled(false);
            }
            if(selectedFeatureAlign!=null && current_curation!=null && selectedFeatureAlign.getEntity() instanceof CuratedTranscript
                && current_curation.getEntity() instanceof CuratedTranscript){
             Command commandmerge=new DoMergeTranscripts(selectedFeatureAlign.getAxis(),
                                          (CuratedTranscript)current_curation.getEntity(), (CuratedTranscript)selectedFeatureAlign.getEntity());
             try{
               commandmerge.validatePreconditions();
               mergePromotedTranscriptMI.setEnabled(true);
             }catch(CommandPreconditionException ex){
                mergePromotedTranscriptMI.setEnabled(false);
            }
            }else{
              mergePromotedTranscriptMI.setEnabled(false);
            }


            return curatedTranscriptFromPromotedMenuItems;
        }
       //Case2: transcript is from Scratch
       else{
          // check if the current selection is transcript and has a start codon
          if(current_curation.getEntity() instanceof CuratedTranscript && ((CuratedTranscript)current_curation.getEntity()).getStartCodon()!=null ){
            deleteStartCodonMI.setEnabled(true);
          }else{deleteStartCodonMI.setEnabled(false);}

          if(((Feature)current_curation.getEntity()).getSuperFeature() instanceof CuratedGene){
           geneOptionsMenuItems.setEnabled(true);
           cascadeReviewedByMI.setEnabled(true);
           boolean b=canBulkAttachTranscriptsToGene((CuratedGene)((Feature)current_curation.getEntity()).getSuperFeature());
               if(b){
                  attachAllTranscriptsToGeneMI.setEnabled(true);
               }else{
                   attachAllTranscriptsToGeneMI.setEnabled(false);
               }

          }else{
            geneOptionsMenuItems.setEnabled(false);
            cascadeReviewedByMI.setEnabled(false);
            attachAllTranscriptsToGeneMI.setEnabled(false);
          }

           // Delegate to the DetachTranscriptFromGeneCommand to prevalidate and
            // decide if the menuitem detachTranscriptFromgene should be grayed or not
            Command command=new DoDetachTranscriptFromGene(
                          current_curation.getAxis(),
                          (CuratedTranscript)current_curation.getEntity());
            try{
              command.validatePreconditions();
              detachTranscriptToGeneMI.setEnabled(true);
            }catch(CommandPreconditionException ex){
              detachTranscriptToGeneMI.setEnabled(false);
            }
            command=null;
            // Delegate to the DoCreateNewGene to prevalidate and
            // decide if the menuitem create new gene should be grayed or not
            Command commandnewgene=new DoCreateNewGene(
                          current_curation.getAxis(),
                          (CuratedTranscript)current_curation.getEntity());
            try{
              commandnewgene.validatePreconditions();
              createNewGeneMI.setEnabled(true);
            }catch(CommandPreconditionException ex){
              createNewGeneMI.setEnabled(false);
            }
            commandnewgene=null;

            // donot allow split on transcript if it has only one subfeature
            Command commandsplit=new DoSplitCurationAt(
                            current_curation.getAxis(),
                            (CuratedTranscript)current_curation.getEntity(),
                            axisPosition);

             try{
              commandsplit.validatePreconditions();
              splitTranscriptMI.setEnabled(true);
            }catch(CommandPreconditionException ex){
              splitTranscriptMI.setEnabled(false);
            }
            commandsplit=null;


             if(selectedFeatureAlign!=null && current_curation!=null && selectedFeatureAlign.getEntity() instanceof CuratedGene && current_curation.getEntity() instanceof CuratedTranscript
               && ((CuratedTranscript)current_curation.getEntity()).getSuperFeature()==null){
               attachTranscriptToGeneMI.setEnabled(true);
            }else{
                 attachTranscriptToGeneMI.setEnabled(false);
            }
            if(selectedFeatureAlign!=null && current_curation!=null && selectedFeatureAlign.getEntity() instanceof CuratedTranscript
                && current_curation.getEntity() instanceof CuratedTranscript){
             Command commandmerge=new DoMergeTranscripts(selectedFeatureAlign.getAxis(),
                                          (CuratedTranscript)current_curation.getEntity(), (CuratedTranscript)selectedFeatureAlign.getEntity());
             try{
               commandmerge.validatePreconditions();
               mergeTranscriptMI.setEnabled(true);
             }catch(CommandPreconditionException ex){
                mergeTranscriptMI.setEnabled(false);
            }
            }else{
              mergeTranscriptMI.setEnabled(false);
            }
          return curatedTranscriptMenuItems;
       }
    } // End method: showcuratedTranscriptMenuItems




 private boolean canBulkAttachTranscriptsToGene(CuratedGene gene){
     validTranscriptsTobeAttachedToGene=new ArrayList();
     if (!(browserModel.getCurrentSelection() instanceof CuratedGene)) return false;

     int numValidTranscripts=0; // transcripts that lie in the selected region
                                // and also dont have any Gene parent. This no.
                                // should be atleast 1 for return condition to be
                                // true.
     Range selectedRange=browserModel.getMasterEditorSelectedRange();
     java.util.List workspacetranscripts=browserModel.getMasterEditorEntity().getGenomeVersion().getWorkspace().getWorkspaceCuratedTranscripts();
     Collection geneSubFeatures =gene.getSubFeatures();
     ArrayList workspaceTrNotAttachedToGene =new ArrayList(workspacetranscripts);

     workspaceTrNotAttachedToGene.removeAll(geneSubFeatures);
     if (workspaceTrNotAttachedToGene.size()==0) return false;
     for(Iterator iter=workspaceTrNotAttachedToGene.iterator();iter.hasNext();){
        CuratedTranscript t=(CuratedTranscript)iter.next();
        if(selectedRange.contains(t.getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis()) && !t.hasSuperFeature()){
           validTranscriptsTobeAttachedToGene.add(t);
           numValidTranscripts=numValidTranscripts+1;

        }


     }
      if(numValidTranscripts>=1) return true;
      else return false;
     }


    private GBGenomicGlyph selectedAncestorIfAny(GBGenomicGlyph gg) {
        AlignableGenomicEntity currentSelection = (AlignableGenomicEntity)browserModel.getCurrentSelection();
        if (currentSelection != null) {
            GBGenomicGlyph selectedGlyph = view.glyphFactory().getCurrentGlyphFor(currentSelection);
            if (selectedGlyph != null && gg.hasAncestor(selectedGlyph)) {
                while (gg != selectedGlyph && !(gg.alignment().getEntity() instanceof CuratedTranscript)) {
                    gg = (GBGenomicGlyph)gg.genomicParent();
                }
            }
        }
        return gg;
    }


    /**
     * This method serves out the current selection so that other classes do
     * not need to know about the SessionMgr and/or the Browser classes.
     */
    public GenomicEntity getCurrentSelection() {
      return browserModel.getCurrentSelection();
    }


    public ArrayList getPopupMenuItems(JComponent comp, GBGenomicGlyph gg, Point windowLocation, int axisLocation) {
        axisPosition = axisLocation;
        GeometricAlignment featureAlignment = gg.alignment();
        popupFeature = (Feature)featureAlignment.getEntity();
        boolean isCuration = (popupFeature instanceof CuratedExon) ||
          (popupFeature instanceof CuratedTranscript);

        if (isCuration) {
          gg = selectedAncestorIfAny(gg);
          featureAlignment = gg.alignment();
          popupFeature = (Feature)featureAlignment.getEntity();

          // Do not want curation enabled menu options for Promoted features.
          if (!featureAlignment.getEntity().isScratch()){
             ArrayList tmpList = new ArrayList();
             JMenuItem tmpItem = new JMenuItem("Promoted Feature: No Curation Options");
             tmpList.add(tmpItem);
             return tmpList;
          }
          current_curation = featureAlignment;
          Feature current_curation_ent = (Feature)featureAlignment.getEntity();
          Feature current_curation_parent_ent = current_curation_ent.getSuperFeature();

          // if over transcript, show transcript curation popup
          if (current_curation_ent instanceof CuratedTranscript)
              return getCuratedTranscriptMenuItems(comp, windowLocation);

          else if ((selectedFeatureAlign.getEntity() instanceof CuratedTranscript ||
                    selectedFeatureAlign.getEntity() instanceof CuratedGene) &&
                    current_curation_parent_ent.getSubFeatureCount() == 1) {
              // if over singleton exon, then assume really want to
              // do popups for its enclosing transcript
              current_curation = findOnlyAlignment(current_curation_parent_ent);
              return getCuratedTranscriptMenuItems(comp, windowLocation);
          }

          else {
            // popUp menus will be different depending if the selected exon is
            // created from precompute or from promoted and exons made from promoted will
            // be paretned by genes via transcripts

            if (((CuratedFeature)current_curation_ent).isScratchReplacingPromoted()){
              if(((CuratedExon)current_curation_ent).getSuperFeature().getSubFeatureCount()==1){
                obsoleteExonMI.setEnabled(false);
              }
              else{
                obsoleteExonMI.setEnabled(true);
              }

              return curatedExonFromPromotedMenuItems;
            }
            else return curatedExonMenuItems;
          }
        }
        else {
          // if no curation hit, then try to find evidence hit (if current selection is curation)
          // can only do this if current_curation is a leaf curation
          // doing a null check
          if (current_curation == null) return new ArrayList();

          Feature current_feature = (Feature) current_curation.getEntity();
          if (current_feature != null && (!(current_feature.hasSubFeatures()))) {
              Alignment model = gg.alignment();
              Feature modelFeat = (Feature) model.getEntity();
              // only leaf feature can be used as evidence...
              if (!(modelFeat.hasSubFeatures())) {
                  featureAlignment = (GeometricAlignment)model;
                  currentEvidenceAlign = null;
                  // check for whether entity is evidence for current_curation
                  Feature current_feat = (Feature) current_curation.getEntity();
                  Feature ev;
                  Iterator evidenceItr = current_feat.getDeepEvidence(false).iterator();
                  while (evidenceItr.hasNext()) {
                      ev = (Feature)evidenceItr.next();
                      if (ev == modelFeat) {
                        currentEvidenceAlign = featureAlignment;
                        break;
                      }
                  }
                  // If the curation at which evidence is pointing lies in the
                  // scratch tier, popup a menu to modify that curation.
                  if ((currentEvidenceAlign != null) && (current_feature.isScratch())) {
                    return evidenceMenuItems;
                  }
              }
          }
          else if(current_feature != null && ((current_feature.hasSubFeatures()))){
            Alignment model = gg.alignment();
            Feature modelFeat = (Feature) model.getEntity();
            Collection evidences=null;
            if ((modelFeat.hasSubFeatures())) {
               Feature current_feat = (Feature) current_curation.getEntity();
               evidences = current_feat.getDeepEvidence(false);
               currentEvidencesForTranscript=modelFeat.getSubFeatures();
            }

            // If the curation at which evidence is pointing lies in the
            // scratch tier, popup a menu to modify that curation.
            if ((current_feature.isScratch())&& evidences!=null) {
              return evidencesForAllExonsMenuItems;
            }
          }
        }
        return new ArrayList();
    }


    /** return menus that ScaffAnnotCurationHandler wants placed in a popup when left-click on a CuratedFeature */
    //  public JMenuItem[] getCurationPopupMenus() {
    //    return curationPopupItems;
    //  }
    void setUpMenus() {
      // menu items for exons from precomputes or scratch
      exonInfoMI = new JMenuItem("Exon Options:");
      deleteExonMI = new JMenuItem("Remove Exon From WorkSpace");
      deleteExonMI.addActionListener(this);


      curatedExonMenuItems.add(exonInfoMI);
      curatedExonMenuItems.add(deleteExonMI);


      // menuitems for exons from promoted
      promotedExonInfoMI=new JMenuItem("Exon Options:");
      deleteExonFromPromotedMI=new JMenuItem("Remove Gene From WorkSpace ");
      deleteExonFromPromotedMI.addActionListener(this);
      obsoleteExonMI = new JMenuItem("Delete Exon From Database");
      obsoleteExonMI.addActionListener(this);


      curatedExonFromPromotedMenuItems.add(promotedExonInfoMI);
      curatedExonFromPromotedMenuItems.add(deleteExonFromPromotedMI);
      curatedExonFromPromotedMenuItems.add(obsoleteExonMI);

      // menu items for transcripts created from precomputes
      transcriptInfoMI = new JMenuItem("Transcript Options:");
      deleteTranscriptMI = new JMenuItem("Remove Transcript From WorkSpace");
      deleteTranscriptMI.addActionListener(this);
      splitTranscriptMI = new JMenuItem("Split Transcript");
      splitTranscriptMI.addActionListener(this);
      mergeTranscriptMI = new JMenuItem("Merge With Selected Transcript");
      mergeTranscriptMI.addActionListener(this);

      createNewGeneMI=new JMenuItem("Create New Gene");
      createNewGeneMI.addActionListener(this);
      attachTranscriptToGeneMI=new JMenuItem("Attach Transcript To Selected Gene");
      attachTranscriptToGeneMI.addActionListener(this);
      detachTranscriptToGeneMI=new JMenuItem("Detach Transcript From Its Gene");
      detachTranscriptToGeneMI.addActionListener(this);
      geneOptionsMenuItems=new JMenuItem("Gene Options:");
      geneOptionsMenuItems.setEnabled(false);
      deleteStartCodonMI = new JMenuItem("Delete Start Codon From WorkSpace");
      deleteStartCodonMI.addActionListener(this);


      curatedTranscriptMenuItems.add(transcriptInfoMI);
      curatedTranscriptMenuItems.add(deleteTranscriptMI);
      curatedTranscriptMenuItems.add(splitTranscriptMI);
      curatedTranscriptMenuItems.add(mergeTranscriptMI);
      curatedTranscriptMenuItems.add(deleteStartCodonMI);

      curatedTranscriptMenuItems.add(createNewGeneMI);
      curatedTranscriptMenuItems.add(attachTranscriptToGeneMI);
      curatedTranscriptMenuItems.add(detachTranscriptToGeneMI);
      curatedTranscriptMenuItems.add(new JSeparator());
      curatedTranscriptMenuItems.add(geneOptionsMenuItems);

      // menuitems for transcript created from promoted
      transcriptPromotedInfoMI = new JMenuItem("Transcript Options:");
      deletePromotedTranscriptMI = new JMenuItem("Delete Gene From WorkSpace");
      deletePromotedTranscriptMI.addActionListener(this);

      splitPromotedTranscriptMI = new JMenuItem("Split Transcript");
      splitPromotedTranscriptMI.addActionListener(this);
      mergePromotedTranscriptMI = new JMenuItem("Merge With Selected Transcript");
      mergePromotedTranscriptMI.addActionListener(this);
      deleteStartCodonOnPromotedDragMI = new JMenuItem("Delete Start Codon From Database");
      deleteStartCodonOnPromotedDragMI.addActionListener(this);
      createNewGenePromotedMI=new JMenuItem("Create New Gene");
      createNewGenePromotedMI.addActionListener(this);
      attachTranscriptToGenePromotedMI=new JMenuItem("Attach Transcript To Selected Gene");
      attachTranscriptToGenePromotedMI.addActionListener(this);
      detachTranscriptToGenePromotedMI=new JMenuItem("Detach Transcript From Its Gene");
      detachTranscriptToGenePromotedMI.addActionListener(this);
      geneOptionsPromotedMenuItems=new JMenuItem("Gene Options:");
      geneOptionsPromotedMenuItems.setEnabled(false);

      // menu for transcript copied from promoted has addtional
      // menuitem that allows obsoletion
      curatedTranscriptFromPromotedMenuItems.add(transcriptPromotedInfoMI);
      curatedTranscriptFromPromotedMenuItems.add(deletePromotedTranscriptMI);

      curatedTranscriptFromPromotedMenuItems.add(splitPromotedTranscriptMI);
      curatedTranscriptFromPromotedMenuItems.add(mergePromotedTranscriptMI);
      curatedTranscriptFromPromotedMenuItems.add(deleteStartCodonOnPromotedDragMI);
      curatedTranscriptFromPromotedMenuItems.add(createNewGenePromotedMI);
      curatedTranscriptFromPromotedMenuItems.add(attachTranscriptToGenePromotedMI);
      curatedTranscriptFromPromotedMenuItems.add(detachTranscriptToGenePromotedMI);
      curatedTranscriptFromPromotedMenuItems.add(new JSeparator());
      curatedTranscriptFromPromotedMenuItems.add(geneOptionsPromotedMenuItems);


      evidenceInfoMI = new JMenuItem("Evidence Options:");
      setLeftEdgeMI = new JMenuItem("Use To Set Left Edge Of Current Curation");
      setLeftEdgeMI.addActionListener(this);
      setRightEdgeMI = new JMenuItem("Use To Set Right Edge Of Current Curation");
      setRightEdgeMI.addActionListener(this);
      removeAsEvidenceMI = new JMenuItem("Remove As Evidence For Current Curation");
      removeAsEvidenceMI.addActionListener(this);

      evidenceMenuItems.add(evidenceInfoMI);
      evidenceMenuItems.add(setLeftEdgeMI);
      evidenceMenuItems.add(setRightEdgeMI);
      evidenceMenuItems.add(removeAsEvidenceMI);

      //menu items for modifying Exon boundaries all at once to
      // conform to an evidence feature
      evidenceOptionsMI = new JMenuItem("Evidence Options:");
      setAllExonsLeftEdgeMI = new JMenuItem("Use To Set Left Edges Of All Exons In The Selected Transcript");
      setAllExonsLeftEdgeMI.addActionListener(this);
      setAllExonsRightEdgeMI = new JMenuItem("Use To Set Right Edges Of All Exons In The Selected Transcript");
      setAllExonsRightEdgeMI.addActionListener(this);
      swapEvidenceForWorkspaceMI = new JMenuItem("Replace Transcript Features With Evidence");
      swapEvidenceForWorkspaceMI.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          // Execute the command.
          ModifyManager.getModifyMgr().doCommand(new DoSwapFeaturesForTranscript((GenomicAxis)current_curation.getAxis(),
            (Feature)browserModel.getCurrentSelection(), popupFeature));
        }
      });

      evidencesForAllExonsMenuItems.add(evidenceOptionsMI);
      evidencesForAllExonsMenuItems.add(setAllExonsLeftEdgeMI);
      evidencesForAllExonsMenuItems.add(setAllExonsRightEdgeMI);
      evidencesForAllExonsMenuItems.add(swapEvidenceForWorkspaceMI);

      // menu items for Gene
      cascadeReviewedByMI=new JMenuItem("Cascade Reviewed By And Date For Whole Gene");
      cascadeReviewedByMI.addActionListener(this);
      curatedTranscriptMenuItems.add(cascadeReviewedByMI);

      attachAllTranscriptsToGeneMI=new JMenuItem("Attach All Transcripts In Selected Region To Selected Gene");
      attachAllTranscriptsToGeneMI.addActionListener(this);
      curatedTranscriptMenuItems.add(attachAllTranscriptsToGeneMI);


      cascadeReviewedByPromotedMI=new JMenuItem("Cascade Reviewed By And Date For Whole Gene");
      cascadeReviewedByPromotedMI.addActionListener(this);
      curatedTranscriptFromPromotedMenuItems.add(cascadeReviewedByPromotedMI);

      attachAllTranscriptsToGenePromotedMI=new JMenuItem("Attach All Transcripts In Selected Region To Selected Gene");
      attachAllTranscriptsToGenePromotedMI.addActionListener(this);
      curatedTranscriptFromPromotedMenuItems.add(attachAllTranscriptsToGenePromotedMI);
    }


    public void actionPerformed(ActionEvent evt){
        Object src = evt.getSource();
        if (src == splitTranscriptMI || src==splitPromotedTranscriptMI) {
            if (!current_curation.getEntity().isWorkspace()) {

                JOptionPane.showMessageDialog(browser,
                    "The Transcript is not not modfiable... is not Workpspace transcript.", "Split Aborted", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ... can't Split something that has been merged...

            if (((CuratedFeature)current_curation.getEntity()).isReplacementType(ReplacementRelationship.MERGE)) {

                JOptionPane.showMessageDialog(browser,
                    "Transcripts that have been previously MERGED can not be SPLIT again without being Promoted.", "Split Aborted", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (current_curation.getEntity() instanceof CuratedTranscript) {
              // Check if the view is mirrored and if so, mirror the coordinate...
              Boolean axisRevComp = (Boolean)browser.getBrowserModel().getModelProperty(BrowserModel.REV_COMP_PROPERTY);
              if (axisRevComp == null) throw new IllegalStateException("Model property GAAnnotRevComped is not set");
              if (axisRevComp.booleanValue()) {
                  MutableRange range = new MutableRange(axisPosition, axisPosition);
                  // Mirror around ungapped length...
                  range.mirror(current_curation.getAxis().getMagnitude());
                  axisPosition = range.getEnd();
              }

              ModifyManager.getModifyMgr().doCommand(new DoSplitCurationAt(
                            current_curation.getAxis(),
                            (CuratedTranscript)current_curation.getEntity(),
                            axisPosition));
              }
              else {
              //warning can only split transcripts....
              JOptionPane.showMessageDialog(browser,
                    "Split operation can only be used on Transcripts.", "Split Aborted", JOptionPane.WARNING_MESSAGE);
              return;
            }

        }
        else if (src == mergeTranscriptMI || src == mergePromotedTranscriptMI) {

            //Should be checking for error conditions here
            Alignment tmpSelectedFeat, tmpCurrentFeat, tmpParentFeat;
            tmpSelectedFeat = selectedFeatureAlign;
            tmpCurrentFeat = current_curation;

            // If either of them are not Transcripts, check to see if their parents are transcripts...
            // This handles the case where you have a single exon transcript that you are trying
            // to merge and the only thing to select is the exon.
            if (!(tmpSelectedFeat.getEntity() instanceof CuratedTranscript)) {
              tmpParentFeat = findOnlyAlignment(((Feature) tmpSelectedFeat.getEntity()).getSuperFeature());
              if ((tmpParentFeat != null) && (tmpParentFeat.getEntity() instanceof CuratedTranscript))
                tmpSelectedFeat = tmpParentFeat;
            }
            if (!(tmpCurrentFeat.getEntity() instanceof CuratedTranscript)) {
              tmpParentFeat = findOnlyAlignment(((Feature)tmpCurrentFeat.getEntity()).getSuperFeature());
              if ((tmpParentFeat != null) && (tmpParentFeat.getEntity() instanceof CuratedTranscript))
                tmpCurrentFeat = tmpParentFeat;
            }

            // Make sure they are both transcripts...
            if (!(tmpSelectedFeat.getEntity() instanceof CuratedTranscript && current_curation.getEntity() instanceof CuratedTranscript)) {

                JOptionPane.showMessageDialog(browser,
                    "Both curations must be Transcripts in order to be merged.", "Merge Aborted", JOptionPane.WARNING_MESSAGE);
              return;
            }
            CuratedTranscript selectedTranscript = (CuratedTranscript)tmpSelectedFeat.getEntity();
            CuratedTranscript currentTranscript = (CuratedTranscript)tmpCurrentFeat.getEntity();

            // Make sure they have the same parent...
            Feature selectedFeatureParent = selectedTranscript.getSuperFeature();
            Feature currentCurationParent = currentTranscript.getSuperFeature();
            //if (selectedFeatureParent != currentCurationParent) {
            if ((selectedFeatureParent != null) && (currentCurationParent != null) && (selectedFeatureParent != currentCurationParent)) {
                //warning can't merge curations with different parents
                JOptionPane.showMessageDialog(browser,
                    "Transcripts must have the same parent Gene in order to be merged.", "Merge Aborted", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Make sure the source transcripts are in Scratch Modified States that allow us to Merge...

            if (!selectedTranscript.isScratch() || !currentTranscript.isScratch()) {

                JOptionPane.showMessageDialog(browser,
                    "Both Transcripts should be in Workspace for Merge", "Merge Aborted", JOptionPane.WARNING_MESSAGE);
                return;
            }


            // ... can't Merge something that has been split...
            if (selectedTranscript.isReplacementType(ReplacementRelationship.SPLIT)  ||
                currentTranscript.isReplacementType(ReplacementRelationship.SPLIT)) {
                //warning can't merge curations with different parents
                JOptionPane.showMessageDialog(browser,
                    "Transcripts that have been previously SPLIT can not be MERGED again without being Promoted.", "Merge Aborted", JOptionPane.WARNING_MESSAGE);
                return;
            }

            DoMergeTranscripts mergeCommand = new DoMergeTranscripts(tmpSelectedFeat.getAxis(),
                                          currentTranscript, selectedTranscript);
            StringBuffer messageString = new StringBuffer();
            if (!mergeCommand.validatePreconditions(messageString)) {
              JOptionPane.showMessageDialog(browser, messageString,
                          "Merge Aborted", JOptionPane.WARNING_MESSAGE);
              return;
           }

           ModifyManager.getModifyMgr().doCommand(mergeCommand);
        }
        else if (src == deleteTranscriptMI || src == deleteExonMI || src==deleteExonFromPromotedMI || src==deletePromotedTranscriptMI) {
          ModifyManager.getModifyMgr().doCommand(new DoDeleteCuration(current_curation));

        }
        else if ( src==obsoleteExonMI) {
            ModifyManager.getModifyMgr().doCommand(new DoDeleteCuration(current_curation,true));
        }

        else if (src == deleteStartCodonMI) {
             if (current_curation.getEntity() instanceof CuratedTranscript){
               CuratedTranscript ct=(CuratedTranscript)current_curation.getEntity();
               CuratedCodon codon=ct.getStartCodon();
               if(codon!=null){
                 ModifyManager.getModifyMgr().doCommand(new DoDeleteCuration(codon.getOnlyGeometricAlignmentToOnlyAxis()));
               }

           }
        }
         else if (src ==deleteStartCodonOnPromotedDragMI) {
             if (current_curation.getEntity() instanceof CuratedTranscript){
               CuratedTranscript ct=(CuratedTranscript)current_curation.getEntity();
               CuratedCodon codon=ct.getStartCodon();
               if(codon!=null){
                 ModifyManager.getModifyMgr().doCommand(new DoDeleteCuration(codon.getOnlyGeometricAlignmentToOnlyAxis(),true));
               }

           }
        }

        else if (src == setLeftEdgeMI) {
            if ((selectedFeatureAlign instanceof GeometricAlignment) &&
                (currentEvidenceAlign instanceof GeometricAlignment)) {

              Command c=adjustCurationEdge((GeometricAlignment)selectedFeatureAlign, (GeometricAlignment)currentEvidenceAlign, true);
              ModifyManager.getModifyMgr().doCommand(c);
              currentEvidenceAlign = null;
            }
            else {
              System.out.println("Have non Geometric Alignments.");
            }
        }
        else if (src == setRightEdgeMI) {
            if ((selectedFeatureAlign instanceof GeometricAlignment) &&
                (currentEvidenceAlign instanceof GeometricAlignment)) {

              Command c=adjustCurationEdge((GeometricAlignment)selectedFeatureAlign, (GeometricAlignment)currentEvidenceAlign, false);
              ModifyManager.getModifyMgr().doCommand(c);
              currentEvidenceAlign = null;
            }
            else {
              System.out.println("Have non Geometric Alignments.");
            }
        }
        else if (src == removeAsEvidenceMI) {
          ModifyManager.getModifyMgr().doCommand(new DoRemoveEvidenceFromCuratedFeature(view.getMasterAxis(),
           (Feature)currentEvidenceAlign.getEntity(), (CuratedFeature)selectedFeatureAlign.getEntity()));
        }

        else if (src == setAllExonsLeftEdgeMI) {
           // if (DEBUG_CLASS) System.out.println("Here in GenomicAxisAnnotCurtionHandler.actionPerformed(); setAllExonsLeftEdgeMI!");
            CompositeCommand cc=new CompositeCommand("Modifying All Exons Left edges");
            Feature selectedFeature=(Feature)selectedFeatureAlign.getEntity();

            for(Iterator subFeaturesIter=selectedFeature.getSubFeatures().iterator();subFeaturesIter.hasNext();){
              Feature subFeature=(Feature)subFeaturesIter.next();
              Alignment exonAlignment=subFeature.getOnlyGeometricAlignmentToAnAxis(selectedFeatureAlign.getAxis());
              for(Iterator evidenceIter=currentEvidencesForTranscript.iterator();evidenceIter.hasNext();){
                Feature evidenceForExon=(Feature)evidenceIter.next();
                Alignment evidenceAlign=(evidenceForExon).getOnlyGeometricAlignmentToAnAxis(selectedFeatureAlign.getAxis());

                if((exonAlignment instanceof GeometricAlignment) &&
                ( evidenceAlign instanceof GeometricAlignment) && subFeature.hasEvidence()&& subFeature.getEvidenceOids().contains(evidenceForExon.getOid())) {
                Command c=adjustCurationEdge((GeometricAlignment)exonAlignment, (GeometricAlignment)evidenceAlign, true);
                cc.addNextCommand(c);
                }
               }
             }
             ModifyManager.getModifyMgr().doCommand(cc);
        }

         else if (src == setAllExonsRightEdgeMI) {
          //  if (DEBUG_CLASS) System.out.println("Here in GenomicAxisAnnotCurtionHandler.actionPerformed(); setAllExonsLeftEdgeMI!");
              CompositeCommand cc=new CompositeCommand("Modifying All Exons Right edges");
            Feature selectedFeature=(Feature)selectedFeatureAlign.getEntity();
            for(Iterator subFeaturesIter=selectedFeature.getSubFeatures().iterator();subFeaturesIter.hasNext();){
              Feature subFeature=(Feature)subFeaturesIter.next();
              Alignment exonAlignment=subFeature.getOnlyGeometricAlignmentToAnAxis(selectedFeatureAlign.getAxis());
              for(Iterator evidenceIter=currentEvidencesForTranscript.iterator();evidenceIter.hasNext();){
                Feature evidenceForExon=(Feature)evidenceIter.next();
                Alignment evidenceAlign=(evidenceForExon).getOnlyGeometricAlignmentToAnAxis(selectedFeatureAlign.getAxis());
                if((exonAlignment instanceof GeometricAlignment) &&
                ( evidenceAlign instanceof GeometricAlignment) && subFeature.hasEvidence()&& subFeature.getEvidenceOids().contains(evidenceForExon.getOid())) {
                Command c=adjustCurationEdge((GeometricAlignment)exonAlignment, (GeometricAlignment)evidenceAlign, false);
                cc.addNextCommand(c);
                }
              }
             }
             ModifyManager.getModifyMgr().doCommand(cc);
        }
        else if(src==createNewGeneMI||src==createNewGenePromotedMI){
          // donot create a gene if the transcript is already attached to gene
          if(((Feature)current_curation.getEntity()).getSuperFeature() instanceof CuratedGene){
            JOptionPane.showMessageDialog(browser, "Transcript already has a gene",
                    " New Gene Creation Warnning", JOptionPane.WARNING_MESSAGE);
            return;
          }
          else {
             ModifyManager.getModifyMgr().doCommand(new DoCreateNewGene(
              current_curation.getAxis(), (CuratedTranscript)current_curation.getEntity()));
          }
        }
        else if(src==attachTranscriptToGeneMI||src==attachTranscriptToGenePromotedMI){
          if(!(((Feature)selectedFeatureAlign.getEntity())instanceof CuratedGene)){
            JOptionPane.showMessageDialog(browser, "Select A Gene First",
                    "Attach Transcript To Gene Warning", JOptionPane.WARNING_MESSAGE);
            return;
           }
           ArrayList transcripts=new ArrayList();
           transcripts.add((CuratedTranscript)current_curation.getEntity());
           ModifyManager.getModifyMgr().doCommand(new DoAttachTranscriptToGene(
                                                selectedFeatureAlign.getAxis(),
                                                (CuratedGene)selectedFeatureAlign.getEntity(),
                                                transcripts));
        }
         else if(src==attachAllTranscriptsToGeneMI||src==attachAllTranscriptsToGenePromotedMI){

           ModifyManager.getModifyMgr().doCommand(new DoAttachTranscriptToGene(
                                                selectedFeatureAlign.getAxis(),
                                                (CuratedGene)selectedFeatureAlign.getEntity(),
                                              validTranscriptsTobeAttachedToGene));
           validTranscriptsTobeAttachedToGene=null;
        }

         else if(src==detachTranscriptToGeneMI||src==detachTranscriptToGenePromotedMI){

            Command command=new DoDetachTranscriptFromGene(
                          current_curation.getAxis(),
                          (CuratedTranscript)current_curation.getEntity());
            try{
             ((DoDetachTranscriptFromGene)command).validateWorkFlow();
             // ModifyManager.getModifyMgr().doCommand(command);
            }catch(CommandException ex){
              JOptionPane.showMessageDialog(browser,
             ex.getMessage() ,
            "Warning!", JOptionPane.PLAIN_MESSAGE);
            }

           ModifyManager.getModifyMgr().doCommand(command);
        }

        else if(src==cascadeReviewedByMI||src==cascadeReviewedByPromotedMI){
          reviewedEntity = (CuratedFeature)browserModel.getCurrentSelection();

          CuratedTranscript ct= (CuratedTranscript)current_curation.getEntity();
          CuratedGene cg=(CuratedGene)ct.getSuperFeature();
          CompositeCommand compositeCommand = new CompositeCommand(REVIEWED_COMMAND);
          cascadeReviewedBy(cg,compositeCommand);
          ModifyManager.getModifyMgr().doCommand(compositeCommand);
        }

    }


    private void cascadeReviewedBy(CuratedGene gene, CompositeCommand compositeCommand){
      String userName=System.getProperty("user.name");
      String date=Util.getDateTimeStringNow();
      cascadeReviewedByHelper(gene,userName,date,compositeCommand);
      // Since the action is a composite command and recursive, we need to track and reinstate
      // the current selection.  This is a little clunky but necessary.
    }


    private void cascadeReviewedByHelper(Feature feature, String newUserName, String newDate, CompositeCommand compositeCommand){
      GenomicProperty oldReviewedBy=feature.getProperty(GeneFacade.REVIEWED_BY_PROP);

       if (oldReviewedBy!=null) compositeCommand.addNextCommand( new DoModifyProperty(feature,GeneFacade.REVIEWED_BY_PROP,oldReviewedBy.getName(),newUserName));

       if (feature instanceof SuperFeature) {
         Iterator it= ((SuperFeature)feature).getSubFeatures().iterator();
         while (it.hasNext()) {
           cascadeReviewedByHelper((Feature)it.next(),newUserName,newDate,compositeCommand);
          }
        }
        if (feature instanceof CuratedTranscript) {
           Feature start=((CuratedTranscript)feature).getStartCodon();
           if (start!=null) cascadeReviewedByHelper(start,newUserName,newDate,compositeCommand);
           Feature stop=((CuratedTranscript)feature).getStopCodon();
           if (stop!=null) cascadeReviewedByHelper(stop,newUserName,newDate,compositeCommand);
      }
    }


    /** handle any cleanup prior to getting rid of a ScaffAnnotCurationHandler */
    public void dispose() {
        browserModel.removeBrowserModelListener(browserModelListener);
        dragFeatureController.delete();
        glyphPopupMenuController.delete();
        ModifyManager.getModifyMgr().removeObserver(commandPostconditionHook);
    }


    /**
    * Private inner class for handling model property events.
    */
   private class MySessionModelListener implements SessionModelListener {
     public void browserAdded(BrowserModel browserModel){}
     public void browserRemoved(BrowserModel browserModel){}
     public void sessionWillExit(){}
     public void modelPropertyChanged(Object property,Object oldValue, Object newValue) {
       if (property.equals(CURATION_ENABLED))
         curationEnabled = ((Boolean)newValue).booleanValue();
      }
   }
}
