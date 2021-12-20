// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package client.gui.components.annotation.axis_annotation;

import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.*;
import client.gui.framework.session_mgr.SessionMgr;
import vizard.*;
import vizard.genomics.component.TiersComponent;
import vizard.genomics.glyph.TierGlyph;
import vizard.glyph.ProxyGlyph;
import vizard.glyph.TranslationGlyph;
import vizard.interactor.MotionInteractor;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class DragFeatureController extends Controller
    implements MotionInteractor.Adapter
{
    private TranslationGlyph shadow;
    private GBGenomicGlyph featureGlyph;
    private MotionInteractor interactor, shiftInteractor, controlInteractor;
    //private EnterLeaveInteractor enterLeaveInteractor;
    private double deltaY; //in user-space between mouse and top of dragged glyph
    private TiersComponent currentComponent;
    private int currentX, currentY;
    private double ty;

    private TierGlyph forwardWorkspace;
    private TierGlyph reverseWorkspace;

    private TierGlyph destinationTier;
    private GenomicAxisAnnotCurationHandler curationHandler;


    public DragFeatureController(GenomicAxisAnnotCurationHandler handler,
				 TierGlyph forwardWorkspace,
				 TierGlyph reverseWorkspace)
    {
	super(handler.getView());
        curationHandler = handler;
	this.forwardWorkspace = forwardWorkspace;
	this.reverseWorkspace = reverseWorkspace;

        interactor = new MotionInteractor(this);
	EventDispatcher.instance.addInteractor(GBGenomicGlyph.class, interactor,
          new EventDispatcher.Filter() {
            public boolean isValid(Glyph glyph) {
              if (glyph == null || glyph.getRootGlyph()==null) return false;
              Component comp = (Component)glyph.getRootGlyph().container();
              return EventDispatcher.hasAncestor(comp, curationHandler.getView());
            }
        });

        shiftInteractor = new MotionInteractor(this);
        shiftInteractor.startWithShift = true;
	EventDispatcher.instance.addInteractor(GBGenomicGlyph.class, shiftInteractor,
          new EventDispatcher.Filter() {
            public boolean isValid(Glyph glyph) {
              if (glyph == null || glyph.getRootGlyph()==null) return false;
              Component comp = (Component)glyph.getRootGlyph().container();
              return EventDispatcher.hasAncestor(comp, curationHandler.getView());
            }
        });

        controlInteractor = new MotionInteractor(this);
        controlInteractor.startWithControl = true;
	EventDispatcher.instance.addInteractor(GBGenomicGlyph.class, controlInteractor,
          new EventDispatcher.Filter() {
            public boolean isValid(Glyph glyph) {
              if (glyph == null || glyph.getRootGlyph()==null) return false;
              Component comp = (Component)glyph.getRootGlyph().container();
              return EventDispatcher.hasAncestor(comp, curationHandler.getView());
            }
        });
    }

    public void delete() {
        motionCancelled(null);

	EventDispatcher.instance.removeInteractor(GBGenomicGlyph.class, interactor);
	EventDispatcher.instance.removeInteractor(GBGenomicGlyph.class, shiftInteractor);
	EventDispatcher.instance.removeInteractor(GBGenomicGlyph.class, controlInteractor);

        super.delete();
    }


    // MotionInteractor adapter specialization

    public void motionStarted(MotionInteractor itor) {
	boolean requestZoom = false;
        if (itor.event().getClickCount()>=2) requestZoom = true;
        view.selectGlyph((GBGenomicGlyph)itor.glyph(),
                         itor == shiftInteractor,
			 itor == controlInteractor,
                         requestZoom);
        GeometricAlignment selection = view.getAlignmentForCurrentSelection();
        if (selection == null || !(selection.getEntity() instanceof Feature))
            return;

        Feature feature = (Feature)selection.getEntity();
        featureGlyph = view.getGlyphFor(feature);
        if (featureGlyph == null)
            return;

        Boolean b = (Boolean)SessionMgr.getSessionMgr().getModelProperty("CurationEnabled");
	if (b == null || !b.booleanValue())
	    return;
	if (view.getMasterAxis().getGenomeVersion().isReadOnly())
	    return;
	if (feature.isClientGenerated())
	    return;

	if (!(feature instanceof CuratedFeature)
	    || (checkIfHasGene(feature) &&
		!feature.getOid().isScratchOID() &&
		!((CuratedFeature)feature).isPromotedReplacedByScratch()))
	{
            currentComponent = (TiersComponent)itor.event().getComponent();
            currentX = itor.event().getX();
            currentY = itor.event().getY();

            shadow = new TranslationGlyph() {
		            public double tx() { return 0; }
		            public double ty() { return ty; }
	             };
	    shadow.addChild(new ProxyGlyph(featureGlyph) {
                    public PickedList pick(GraphicContext gc, Rectangle deviceRect) {
                        return null;
                    }
                    public void addBounds(Bounds bounds) {
                        super.addBounds(bounds);
                        //@todo uggly (when a genomic glyph is selected, it has some extra pixel for the outline)
                        bounds.addLeftRightPixels(5, 5);
                        bounds.addUpDownPixels(5, 5);
                    }});

            deltaY = itor.currentLocation().getY() - featureGlyph.y();
            currentComponent.viewTransform().addChild(shadow);
            setTranslation();

            //jojic SpliceSiteGlyph.paintAlways = true; //@todo uggly
	}
    }

    public void move(MotionInteractor itor) {
	if (shadow == null)
	    return;

        resetCurrent(itor);
	setTranslation();

        if (destinationTier != null) {
            destinationTier.highlight(false);
            destinationTier = null;
        }
        Glyph dst = determineDestinationGlyph();
        if (dst != null && dst instanceof TierGlyph && ((TierGlyph)dst).name().startsWith("Workspace"))
            destinationTier = (TierGlyph)dst;
        if (destinationTier != null)
            destinationTier.highlight(true);
    }

    private Glyph determineDestinationGlyph() {
	PickedList pickedList = currentComponent.rootGlyph().pickTopGlyph
                (currentX, currentY, EventDispatcher.SENSIBILITY.intValue());

        for(int i = 0; i < pickedList.count(); ++i) {
            Glyph glyph = pickedList.glyph(i);
            if (glyph instanceof GBGenomicGlyph || glyph instanceof TierGlyph)
                return glyph;
        }

        return null;
    }

    public void motionStopped(MotionInteractor itor) {
	if (shadow == null)
	    return;

        //jojic SpliceSiteGlyph.paintAlways = false;
        shadow.delete();
        shadow = null;

        Glyph glyph = determineDestinationGlyph();

        if (glyph != null && !(featureGlyph==glyph)) {
            if (glyph instanceof GBGenomicGlyph && !(featureGlyph==((GBGenomicGlyph)glyph).genomicParent()))
            {
              curationHandler.endFeatureDrag(featureGlyph, (GBGenomicGlyph)glyph);

           }
           else if (glyph instanceof TierGlyph)
                curationHandler.endFeatureDrag(featureGlyph, (TierGlyph)glyph);
        }

        if (destinationTier != null)
            destinationTier.highlight(false);
    }

    public void motionCancelled(MotionInteractor itor) {
	if (shadow == null)
	    return;

        //jojic SpliceSiteGlyph.paintAlways = false;
        shadow.delete();
        shadow = null;

        if (destinationTier != null)
            destinationTier.highlight(false);
    }

    private boolean checkIfHasGene(Feature feature) {
	CuratedGene  gene = null;

	if (feature instanceof CuratedExon) {
	    CuratedExon exon = (CuratedExon)feature;
	    CuratedTranscript transcript = (CuratedTranscript)exon.getSuperFeature();
	    if (transcript != null)
		gene = (CuratedGene)transcript.getSuperFeature();
	}
	else if (feature instanceof CuratedTranscript) {
	    CuratedTranscript transcript = (CuratedTranscript)feature;
	    if (transcript != null)
		gene = (CuratedGene)transcript.getSuperFeature();
	}
	else if (feature instanceof CuratedCodon) {
	    CuratedCodon codon = (CuratedCodon) feature;
	    CuratedTranscript transcript = codon.getHostTranscript();
	    if (transcript != null)
		gene = (CuratedGene)transcript.getSuperFeature();
	}
	else if (feature instanceof CuratedGene) {
	    gene = (CuratedGene)feature;
	}

	return gene != null;
    }

    private void resetCurrent(MotionInteractor itor) {
        TiersComponent previous = currentComponent;
        resetCurrent2(itor);
        if (currentComponent != previous) {
            shadow.parent().removeChild(shadow);
            currentComponent.viewTransform().addChild(shadow);
        }
    }

    private void resetCurrent2(MotionInteractor itor) {
        Component comp = itor.event().getComponent();
        int x = itor.event().getX();
        int y = itor.event().getY();

        if (y >= 0 && y < comp.getHeight()) {
            currentComponent = (TiersComponent)comp;
            currentX = x;
            currentY = y;
            return;
        }

        TiersComponent[] dstComps = { view.forwardTable().tiersComponent(),
                                      view.reverseTable().tiersComponent(),
                                      view.axisTable().tiersComponent() };
        for(int i = 0; i < dstComps.length; ++i) {
            if (comp != dstComps[i]) {
                Point p = SwingUtilities.convertPoint(comp, new Point(x, y), dstComps[i]);
                if (p.y >= 0 && p.y < dstComps[i].getHeight()) {
                    currentComponent = dstComps[i];
                    currentX = p.x;
                    currentY = p.y;
                    return;
                }
            }
        }

        if (currentComponent == comp) {
            currentX = x;
            currentY = y;
        }
        else {
            Point p = SwingUtilities.convertPoint(comp, new Point(x, y), currentComponent);
            currentX = p.x;
            currentY = p.y;
        }
    }

    private void setTranslation() {
        AffineTransform viewTransform = currentComponent.viewTransform().transform();
        double shouldBeY = 0;
        try {
            shouldBeY = -deltaY +
                viewTransform.inverseTransform(new Point(currentX, currentY), null).getY();
        }
        catch(Throwable t) {}

        shadow.repaint();
        ty = shouldBeY;
        shadow.repaint();
    }
}
