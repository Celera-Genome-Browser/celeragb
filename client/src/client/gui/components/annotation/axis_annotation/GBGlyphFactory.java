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

import api.entity_model.access.observer.AxisObserver;
import api.entity_model.access.observer.AxisObserverAdapter;
import api.entity_model.access.observer.ModifyManagerObserver;
import api.entity_model.access.observer.ModifyManagerObserverAdapter;
import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.management.ModifyManager;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.*;
import api.entity_model.model.assembly.Contig;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.AlignableGenomicEntity;
import api.stub.geometry.Range;
import vizard.genomics.glyph.GenomicGlyph;
import vizard.genomics.glyph.TierGlyph;
import vizard.util.Assert;
import vizard.util.ObserverList;

import java.util.*;


//Dock/Undock should be fast

//View has to call factory.setIsReverseComplement (and factory has to implement it!)

//perhaps unalignment of an entity should be kept instead of pushing the rootEntity
//  (the connection with the root might have been lost)

//pref changes could be much faster now that gg.loadProperties is the standard
//  implement pref changes to call prefs (warning: prefs are per instance, that might be slow,
//  perhaps prefs per class would improve performances a lot (but perhaps not much in which case
//  it would be a waste of time to implement))

public class GBGlyphFactory
{
    private EntityToGlyphMap entityToGlyphMap = new EntityToGlyphMap();

    //WARNING: The postponedSet must not be a set: an unaligned entity is considered
    //         equal to a new instance entity as long as they share the same OID
    //         In such a case the set would not add a newly created instance.
    private ArrayList postponedSet = new ArrayList();

    public List debugGetPostponedSet() { return postponedSet; }

    private PostponedAWTInvoker postponedGlyphBuilder = new PostponedAWTInvoker(
            new Runnable() { public void run() { rebuildPostponedGlyphs(); }}
        );

    private GenomicAxis axis;
    private TierFinder tierFinder;

    private boolean isReverseComplement;

    private AxisObserver alignmentObserver = new AxisObserverAdapter() {
            public void noteAlignmentOfEntity(Alignment align) {
                alignmentChanged(align);
            }
            public void noteEntityAlignmentChanged(Alignment align) {
                alignmentChanged(align);
            }
            public void noteUnalignmentOfEntity(Alignment align) {
                alignmentChanged(align);
            }
        };

    private ModifyManagerObserver commandDoneObserver = new ModifyManagerObserverAdapter() {
            public void noteCommandDidFinish(String commandName, int commandKind) {
                rebuildWorkspaceGlyphs();
            }
        };

    //------------------------------------------------------------

    public interface TierFinder
    {
        TierGlyph tierGlyphFor(AlignableGenomicEntity entity);
        Collection workspaceTiers();
    }

    public interface BatchCreationObserver
    {
   void batchCreationDone(GBGlyphFactory glyphFactory);
    }

    public ObserverList observers = new ObserverList(BatchCreationObserver.class);

    //-------------------------------------------------------------

    public GBGlyphFactory(TierFinder tierFinder) {
   this.tierFinder = tierFinder;

        ModifyManager.getModifyMgr().addObserver(commandDoneObserver);
    }

    public void delete() {
        ModifyManager.getModifyMgr().removeObserver(commandDoneObserver);
        postponedGlyphBuilder.delete();
        if (axis != null)
          axis.removeAxisObserver(alignmentObserver);
    }

    public GenomicAxis axis() {
        return axis;
    }

    public boolean isReverseComplement() {
        return isReverseComplement;
    }

    public void setReverseComplement(boolean b) {
        isReverseComplement = b;
    }

    public boolean hasGlyphFor(AlignableGenomicEntity entity) {
        return getCurrentGlyphFor(entity) != null;
    }

    public List getGlyphCollection() {
        return entityToGlyphMap.allGenomicGlyphs();
    }

    public void setGenomicAxis(GenomicAxis newAxis) {
        if (newAxis == axis)
            return;

        //postponedGlyphBuilder.reset();

        if (axis != null) {
            axis.removeAxisObserver(alignmentObserver);
            deleteAllGlyphs();
        }

        axis = newAxis;
        if (axis != null)
            axis.addAxisObserver(alignmentObserver, true);
    }

    public GBGenomicGlyph getGlyphFor(AlignableGenomicEntity entity) {
   ensureGlyphsAreNotPostponedFor(entity);
   return getCurrentGlyphFor(entity);
    }

    public GBGenomicGlyph getCurrentGlyphFor(AlignableGenomicEntity entity) {
        return entityToGlyphMap.get(entity);
    }

    public void rebuildGlyphsFor(AlignableGenomicEntity entity) {
   if (postponedSet.add(entity))
       postponedGlyphBuilder.executeLater();
    }


    //-------------------------------------------------------

    void removeFromEntityToGlyphMap(GBGenomicGlyph gg) {
        entityToGlyphMap.remove(gg);

   if (Assert.debug) {
       if ((entityToGlyphMap.size() % 1000) == 0)
      System.out.println("Created " + entityToGlyphMap.size() + " glyphs.");
   }
    }

    void addToEntityToGlyphMap(AlignableGenomicEntity entity, GBGenomicGlyph gg) {
        entityToGlyphMap.put(entity, gg);

   if (Assert.debug) {
       if ((entityToGlyphMap.size() % 1000) == 0)
      System.out.println("Created " + entityToGlyphMap.size() + " glyphs.");
   }
    }


    //-------------------------------------------------------

    private void rebuildPostponedGlyphs() {
   if (postponedSet.isEmpty())
       return;

        final int MAX_IN_ONE_SHOT = 1024;
        if (postponedSet.size() < MAX_IN_ONE_SHOT) {
          ArrayList sortedGlyphs = new ArrayList(postponedSet);
          postponedSet.clear();

          Collections.sort(sortedGlyphs, new Comparator() {
            public int compare(Object o1, Object o2) {
              AlignableGenomicEntity e1 = (AlignableGenomicEntity)o1;
              AlignableGenomicEntity e2 = (AlignableGenomicEntity)o2;
              Collection aligns1 = e1.getAlignmentsToAxis(axis);
              Collection aligns2 = e2.getAlignmentsToAxis(axis);
              if (aligns1.size() == 0 || aligns2.size() == 0)
                return 0;
              GeometricAlignment a1 = (GeometricAlignment)aligns1.iterator().next();
              GeometricAlignment a2 = (GeometricAlignment)aligns2.iterator().next();
              Range r1 = a1.getRangeOnAxis();
              Range r2 = a2.getRangeOnAxis();
              if (r1.getMinimum() < r2.getMinimum())
                return -1;
              if (r2.getMinimum() < r1.getMinimum())
                return 1;
              if (r1.getMaximum() < r2.getMaximum())
                return -1;
              if (r2.getMaximum() < r1.getMaximum())
                return 1;
              return 0;
            }
          });
          for(int i = 0; i < sortedGlyphs.size(); ++i) {
            AlignableGenomicEntity entity = (AlignableGenomicEntity)sortedGlyphs.get(i);
            rebuildGlyphsNow(entity);
          }
        }
        else {
          int i = 0;
          while(!postponedSet.isEmpty()) {
              if (++i > MAX_IN_ONE_SHOT)
                  break;
              AlignableGenomicEntity entity = extractNextEntityFromPostponedSet();
              rebuildGlyphsNow(entity);
          }
        }

        if (postponedSet.isEmpty())
          notifyGlyphsBeingRebuilt();
        else
       postponedGlyphBuilder.executeLater();
    }

    private void alignmentChanged(Alignment align) {
        if (align instanceof GeometricAlignment) {
            GeometricAlignment geomAlign = (GeometricAlignment)align;
            //if (!geomAlign.getEntity().isWorkspace())
                rebuildGlyphsFor(geomAlign.getEntity());
        }
    }

    private void workspaceAlignmentChanged(Alignment align) {
        if (align instanceof GeometricAlignment) {
            GeometricAlignment geomAlign = (GeometricAlignment)align;
            rebuildGlyphsFor(geomAlign.getEntity());
        }
    }

    private void deleteAllGlyphs() {
        Iterator i = entityToGlyphMap.allGenomicGlyphs().iterator();
        while(i.hasNext()) {
            GBGenomicGlyph gg = (GBGenomicGlyph)i.next();
            gg.delete();
        }
    }

    private void ensureGlyphsAreNotPostponedFor(AlignableGenomicEntity entity) {
        if (postponedSet.contains(entity))
            postponedGlyphBuilder.executeNow();
    }

    private AlignableGenomicEntity extractNextEntityFromPostponedSet() {
        int size = postponedSet.size();
        if (size == 0)
            return null;
        return (AlignableGenomicEntity)postponedSet.remove(size - 1);
    }

    private void rebuildGlyphsNow(AlignableGenomicEntity entity) {
        rebuildGlyphsNow(entity, getCurrentGlyphFor(entity));
    }

    private void rebuildGlyphsNow(AlignableGenomicEntity entity, GBGenomicGlyph glyph) {
        if (!isEntityAligned(entity)) {
            if (glyph != null) {
                reparentGenomicChildrenToProperTier(glyph);
                glyph.delete();
            }
        }
        else { //entity is aligned
            if (glyph != null)
                removeLostChildren(glyph);
            else {
                ensureEntityPreferencesReady(entity);
                glyph = createGlyphFor(entity);
            }
            reparentExistingGenomicChildren(glyph);
            reparentToProperParent(glyph);
            glyph.propertiesChanged();
        }
    }

    private void removeLostChildren(GBGenomicGlyph parent) {
        Iterator i = parent.genomicChildren().iterator();
        while(i.hasNext()) {
            GBGenomicGlyph child = (GBGenomicGlyph)i.next();
            reparentToProperParent(child);
        }
    }

    private void ensureEntityPreferencesReady(AlignableGenomicEntity entity) {
        //@todo
        //Well... I know that a side effect of the method call below is to ensure
        //that the preferences for the given entity will be created if they do not
        //exist yet.
        //But implementing the proper direct calls to the ViewPrefMgr would definitely
        //be cleaner.
        //O Reader, if you feel like it, please do the change.
        tierFinder.tierGlyphFor(entity);
    }

    private void reparentGenomicChildrenToProperTier(GBGenomicGlyph glyph) {
        List list = glyph.genomicChildren();

        Iterator i = list.iterator();
        while(i.hasNext()) {
            GBGenomicGlyph genomicChild = (GBGenomicGlyph)i.next();
            glyph.removeGenomicChild(genomicChild);
        }

        TierGlyph tierGlyph = null;
        boolean tierAlreadyFound = false;
        i = list.iterator();
        while(i.hasNext()) {
            GBGenomicGlyph genomicChild = (GBGenomicGlyph)i.next();
            if (!tierAlreadyFound) {
                tierGlyph = tierFinder.tierGlyphFor(genomicChild.alignment().getEntity());
                tierAlreadyFound = true;
            }
            if (tierGlyph != null)
                tierGlyph.addGenomicChild(genomicChild);
        }
    }

    private void reparentExistingGenomicChildren(GBGenomicGlyph glyph) {
        Iterator i = getSubFeatures(glyph.alignment().getEntity()).iterator();
        while(i.hasNext()) {
            AlignableGenomicEntity subEntity = (AlignableGenomicEntity)i.next();
            GBGenomicGlyph genomicChild = getCurrentGlyphFor(subEntity);
            if (genomicChild != null)
                reparentGenomicGlyph(genomicChild, glyph);
        }
    }

    private Collection getSubFeatures(AlignableGenomicEntity entity) {
        if (!(entity instanceof Feature))
            return Collections.EMPTY_LIST;
        Feature feature = (Feature)entity;
      Collection subFeatures = feature.getSubFeatures();
   if (feature instanceof CuratedTranscript) {
       CuratedTranscript transcript = (CuratedTranscript)feature;
       Feature startCodon = transcript.getStartCodon();
       Feature stopCodon = transcript.getStopCodon();
       if (startCodon != null || stopCodon != null) {
      subFeatures = new HashSet(subFeatures);
      if (startCodon != null)
          subFeatures.add(startCodon);
      if (stopCodon != null)
          subFeatures.add(stopCodon);
       }
   }
        return subFeatures;
    }

    private Feature getSuperFeature(AlignableGenomicEntity entity) {
        if (!(entity instanceof Feature))
            return null;
        return ((Feature)entity).getSuperFeature();
    }

    private void reparentGenomicGlyph(GBGenomicGlyph glyph, GBGenomicGlyph parent) {
        if (parent != glyph.genomicParent()) {
            removeFromGenomicParent(glyph);
            parent.addGenomicChild(glyph);
        }
    }

    private void removeFromGenomicParent(GBGenomicGlyph glyph) {
        if (glyph.parent() != null) {
            GenomicGlyph previousParent = glyph.genomicParent();
            if (previousParent != null)
                previousParent.removeGenomicChild(glyph);
            else {
                TierGlyph tierGlyph = glyph.tierAncestor();
                if (tierGlyph != null)
                    tierGlyph.removeGenomicChild(glyph);
            }
        }
        if (Assert.debug) Assert.vAssert(glyph.parent() == null);
    }

    private void reparentToProperParent(GBGenomicGlyph glyph) {
        Feature superFeature = getSuperFeature(glyph.alignment().getEntity());
        if (superFeature != null) {
            GBGenomicGlyph parentGlyph = getCurrentGlyphFor(superFeature);
            if (glyph.genomicParent() != parentGlyph) {
                removeFromGenomicParent(glyph);
                if (parentGlyph != null)
                    parentGlyph.addGenomicChild(glyph);
            }
        }
        else {
            removeFromGenomicParent(glyph);
            TierGlyph tierGlyph = tierFinder.tierGlyphFor(glyph.alignment().getEntity());
            if (tierGlyph != null)
                tierGlyph.addGenomicChild(glyph);
        }
    }

    private boolean isEntityAligned(AlignableGenomicEntity entity) {
        return !entity.getAlignmentsToAxis(axis).isEmpty();
    }

    public GeometricAlignment findAlignment(AlignableGenomicEntity entity) {
        Collection alignments = entity.getAlignmentsToAxis(axis);
   if (Assert.debug) Assert.vAssert(alignments.size() == 1);
        return (GeometricAlignment)alignments.iterator().next();
    }

    private GBGenomicGlyph createGlyphFor(AlignableGenomicEntity entity) {
        if (Assert.debug) Assert.vAssert(getCurrentGlyphFor(entity) == null);

        class GlyphCreationVisitor extends GenomicEntityVisitor
        {
            GBGenomicGlyph createdGlyph;

            public void visitSpliceSite(SpliceSite spliceSite) {
                createdGlyph = new SpliceSiteGlyph(GBGlyphFactory.this, findAlignment(spliceSite));
            }
            public void visitContig(Contig contig) {
                createdGlyph = new ContigGlyph(GBGlyphFactory.this, findAlignment(contig));
            }
            public void visitCuratedFeature(CuratedFeature curatedFeature) {
      createdGlyph = new CuratedFeatureGlyph(GBGlyphFactory.this, findAlignment(curatedFeature));
            }
            public void visitFeature(Feature feature) {
                //Temporary: visitPolyMorphism is not implemented yet
                if (feature instanceof PolyMorphism && isPolyMorphismAnSNP((PolyMorphism)feature)) {
                    visitPolyMorphism((PolyMorphism)feature);
                    return;
                }
                createdGlyph = new FeatureGlyph(GBGlyphFactory.this, findAlignment(feature));
            }
            public void visitCuratedGene(CuratedGene gene) {
                createdGlyph = new GeneGlyph(GBGlyphFactory.this, findAlignment(gene));
            }
            public void visitPolyMorphism(PolyMorphism polyMorphism) {
      if (!isPolyMorphismAnSNP(polyMorphism)) {
          visitFeature(polyMorphism);
          return;
      }
      createdGlyph = new SNPGlyph(GBGlyphFactory.this, findAlignment(polyMorphism));
       }
       public void visitHSPFeature(HSPFeature hspFeature) {
      createdGlyph = new HSPGlyph(GBGlyphFactory.this, findAlignment(hspFeature));
       }
   }

        GlyphCreationVisitor visitor = new GlyphCreationVisitor();
        entity.acceptVisitorForSelf(visitor);

        return visitor.createdGlyph;
    }

    private boolean isPolyMorphismAnSNP(PolyMorphism polyMorphism) {
        String polyValue = polyMorphism.getPolyMutationType();

        return polyValue != null &&
               (polyValue.equals(PolyMorphism.PolyMutationType.SUBSTITUTION) ||
                polyValue.equals(PolyMorphism.PolyMutationType.INSERTION) ||
                polyValue.equals(PolyMorphism.PolyMutationType.DELETION));
    }

    private void notifyGlyphsBeingRebuilt() {
   observers.notify(new ObserverList.Caller() {
      public void call(Object o) {
          ((BatchCreationObserver)o).batchCreationDone(GBGlyphFactory.this);
      }});
    }

    private void rebuildWorkspaceGlyphs() {
        Iterator i = tierFinder.workspaceTiers().iterator();
        while(i.hasNext()) {
            TierGlyph workspaceTier = (TierGlyph)i.next();
            Iterator j = workspaceTier.genomicChildren().iterator();
            while(j.hasNext()) {
                GBGenomicGlyph gg = (GBGenomicGlyph)j.next();
                gg.delete();
            }
        }

        AxisObserver curatedAlignmentObserver = new AxisObserverAdapter() {
            public void noteAlignmentOfEntity(Alignment align) {
                if (align.getEntity().isWorkspace())
                    workspaceAlignmentChanged(align);
            }};
        axis.addAxisObserver(curatedAlignmentObserver);
        axis.removeAxisObserver(curatedAlignmentObserver);
        rebuildPostponedGlyphs();
    }
}


//This class is used to map a glyph to an entity.
//Java maps use entity.hash() and entity.equals() to find the proper glyph.
//This is not good for us: when some command is executed that deletes an old transcript and
//creates a new one, the new and the old share the same hash code and are considered equal.
//What we need is a map at the instance level. This class implements it.
class EntityToGlyphMap
{
    private HashMap map = new HashMap();

    public GBGenomicGlyph get(AlignableGenomicEntity entity) {
        Object o = map.get(entity);
        if (o == null)
            return null;
        return (o instanceof ArrayList)
                    ? getFromList((ArrayList)o, entity)
                    : getFromGlyph((GBGenomicGlyph)o, entity);
    }

    public void put(AlignableGenomicEntity entity, GBGenomicGlyph glyph) {
        if (Assert.debug) Assert.vAssert(get(entity) == null);

        Object o = map.get(entity);
        if (o == null)
            map.put(entity, glyph);
        else if (o instanceof ArrayList)
            ((ArrayList)o).add(glyph);
        else {
            if (Assert.debug) Assert.vAssert(o instanceof GBGenomicGlyph);
            ArrayList list = new ArrayList();
            list.add(o);
            list.add(glyph);
            map.put(entity, list);
        }
    }

    public void remove(GBGenomicGlyph glyph) {
        AlignableGenomicEntity entity = glyph.alignment().getEntity();

        Object o = map.get(entity);
        if (o == null)
          return;
        if (o instanceof GBGenomicGlyph) {
            if (Assert.debug) Assert.vAssert(o == glyph);
            map.remove(entity);
        }
        else {
            ArrayList list = (ArrayList)o;
            for(int i = list.size()-1; i >= 0; --i) {
                if (list.get(i) == glyph) {
                    list.remove(i);
                    return;
                }
            }
            if (Assert.debug) Assert.vAssert(false);
        }
    }

    public List allGenomicGlyphs() {
        ArrayList allGlyphs = new ArrayList();
        Iterator i = map.values().iterator();
        while(i.hasNext()) {
            Object o = i.next();
            if (o instanceof GBGenomicGlyph)
                allGlyphs.add(o);
            else {
                ArrayList list = (ArrayList)o;
                for(int j = list.size()-1; j >= 0; --j) {
                    allGlyphs.add(list.get(j));
                }
            }
        }
        return allGlyphs;
    }

    public int size() {
        return map.size();
    }

    private GBGenomicGlyph getFromGlyph(GBGenomicGlyph glyph, AlignableGenomicEntity entity) {
        return (glyph.alignment().getEntity() == entity) //NOT equals()!
                    ? glyph : null;
    }

    private GBGenomicGlyph getFromList(ArrayList list, AlignableGenomicEntity entity) {
        for(int i = list.size()-1; i >= 0; --i) {
            GBGenomicGlyph glyph = getFromGlyph((GBGenomicGlyph)list.get(i), entity);
            if (glyph != null)
                return glyph;
        }
        return null;
    }
}
