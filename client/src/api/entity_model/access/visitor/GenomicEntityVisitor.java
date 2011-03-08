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
package api.entity_model.access.visitor;

import api.entity_model.model.annotation.*;
import api.entity_model.model.assembly.Contig;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.AlignableGenomicEntity;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.genetics.Chromosome;
import api.entity_model.model.genetics.Species;

public abstract class GenomicEntityVisitor {

    public void visitGenomicEntity(GenomicEntity entity){}


    public void visitAlignableGenomicEntity(AlignableGenomicEntity alignableEntity){
       visitGenomicEntity(alignableEntity);
    }

    public void visitAxis(Axis axis){
       visitAlignableGenomicEntity(axis);
    }

    public void visitSpecies(Species species){
       visitAxis(species);
    }

    public void visitGenomicAxis(GenomicAxis genomicAxis){
       visitAxis(genomicAxis);
    }

    public void visitContig(Contig contig){
       visitAxis(contig);
    }

    public void visitChromosome(Chromosome chromosome){
       visitAxis(chromosome);
    }

    public void visitFeature(Feature feature) {
       visitAlignableGenomicEntity(feature);
    }

    public void visitComputedFeature(ComputedFeature computedFeature) {
       visitFeature(computedFeature);
    }

    public void visitCuratedFeature(CuratedFeature curatedFeature) {
       visitFeature(curatedFeature);
    }

    public void visitCuratedGene(CuratedGene curatedGene){
       visitCuratedFeature(curatedGene);
    }

    public void visitCuratedTranscript(CuratedTranscript curatedTranscript){
       visitCuratedFeature(curatedTranscript);
    }

    public void visitCuratedExon(CuratedExon curatedExon){
       visitCuratedFeature(curatedExon);
    }

    public void visitCuratedCodon(CuratedCodon curatedCodon){
       visitCuratedFeature(curatedCodon);
    }

    public void visitComputedCodon(ComputedCodon computedCodon){
       visitComputedFeature(computedCodon);
    }

    public void visitSpliceSite(SpliceSite spliceSite){
       visitComputedFeature(spliceSite);
    }

    public void visitHitAlignmentFeature(HitAlignmentFeature hitAlignmentFeature){
       visitComputedFeature(hitAlignmentFeature);
    }

    public void visitHitAlignmentDetailFeature(HitAlignmentDetailFeature hitAlignmentDetailFeature){
       visitComputedFeature(hitAlignmentDetailFeature);
    }

    public void visitHSPFeature(HSPFeature hspFeature){
       visitHitAlignmentDetailFeature(hspFeature);
    }

    public void visitBlastHit(BlastHit blastHit){
       visitHitAlignmentFeature(blastHit);
    }

    public void visitSTSMarker(STSMarker stsMarker){
       visitComputedFeature(stsMarker);
    }

    public void visitPolyMorphism(PolyMorphism polyMorphism) {
      visitComputedFeature(polyMorphism);
    }
}
