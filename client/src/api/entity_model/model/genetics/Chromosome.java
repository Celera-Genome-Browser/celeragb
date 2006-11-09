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
package api.entity_model.model.genetics;

import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.AlignmentNotAllowedException;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.EntityTypeSet;
import api.entity_model.model.fundtype.LoadFilter;
import api.entity_model.model.fundtype.LoadFilterStatus;
import api.entity_model.model.fundtype.LoadRequest;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.facade.abstract_facade.fundtype.GenomicEntityLoader;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.OID;

import java.util.HashSet;
import java.util.Set;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version $Id$
 */


public class Chromosome extends Axis {

   private LoadFilter genomicAxisLoadFilter;

//****************************************
//*  Public methods
//****************************************
/**
 * @level developer
 */
  public Chromosome(OID oid, String displayName)
  {
    this(oid, displayName, 0);
  }

/**
 * @level developer
 */
  public Chromosome(OID oid, String displayName, int magnitude)
  {
    this(oid, displayName, magnitude, null);
  }

/**
 * @level developer
 */
  public Chromosome(OID oid, String displayName, int magnitude, FacadeManagerBase readFacadeManager)
  {
    super(EntityType.getEntityTypeForName("_CHROMOSOME"),oid, displayName, magnitude, readFacadeManager);
  }

    /**
     * Accepts visitors that implement GenomicEntityVisitor. acceptVisitorForSelf
     * is specialized by every subclass of GenomicEntity to call the appropriate
     * "visit...(...)" function on the passed visitor.
     * @param theVisitor the visitor.
     * @level developer
     */
    public void acceptVisitorForSelf(GenomicEntityVisitor theVisitor) {
        try {
                theVisitor.visitChromosome(this);
        }
        catch (Exception ex) {
                handleException(ex);
        }
    }


/**
 * @level developer
 */
    public Set getDefaultLoadFilters() {
       Set set= new HashSet(1);
       set.add(getGenomicAxisLoadFilter());
       return set;
    }

/**
 * @level developer
 */
    public LoadFilter getGenomicAxisLoadFilter() {
       if (genomicAxisLoadFilter==null) {
          genomicAxisLoadFilter=new LoadFilter("Genomic Axes on "+
            toString(),
            new EntityTypeSet
              (
                new EntityType[]{EntityType.getEntityTypeForValue(EntityTypeConstants.Genomic_Axis)}
              ));
       }
       return genomicAxisLoadFilter;
    }

/**
 * @level developer
 */
    public LoadRequest getGenomicAxisLoadRequest() {
       return new LoadRequest(getGenomicAxisLoadFilter());
    }

//****************************************
//*  Protected methods
//****************************************

    /*protected Chromosome () {
    }*/

    protected GenomicEntityMutator constructMyMutator(){
       return new ChromosomeMutator();
    }

    protected int getPredictedNumberOfAlignmentsToEntities() {
      return 50;
    }

    protected void willAcceptAlignmentToAxis(Alignment alignmentToAxis)
       throws AlignmentNotAllowedException {

       super.willAcceptAlignmentToAxis (alignmentToAxis);
       if (!(alignmentToAxis.getAxis() instanceof Species))
          throw new AlignmentNotAllowedException("Chromosomes cannot be aligned"+
          " to an axis of any type other than Species",alignmentToAxis);
       if (!(alignmentToAxis.getClass() == Alignment.class))
          throw new AlignmentNotAllowedException("Chromosomes cannot be aligned"+
          " to an axis with a geometric alignment",alignmentToAxis);

    }

    protected void willAcceptAlignmentToEntity(Alignment alignmentToEntity)
       throws AlignmentNotAllowedException {
       super.willAcceptAlignmentToEntity(alignmentToEntity);
       if (!(alignmentToEntity.getEntity() instanceof GenomicAxis)) throw
          new AlignmentNotAllowedException("Chromosomes cannot accept an"+
          " alignment to and entity of any type other than GenomicAxis",alignmentToEntity);
    }

    protected boolean willAcceptLoadRequestForAlignedEntities(LoadRequest loadRequest){
       if (loadRequest.isRangeRequest() || loadRequest.isBinRequest() ||
           !(loadRequest.getLoadFilter().getLoadFilterStatus().getClass()==LoadFilterStatus.class))
           return false;
       return true;
    }

    protected GenomicEntityLoader getDataLoader() {
      try {
       return getLoaderManager().getChromosome();
      }
      catch (Exception ex) {
        handleException(ex);
        return null;
      }
    }
//****************************************
//*  Package methods
//****************************************


//****************************************
//*  Inner Classes
//****************************************

     public class ChromosomeMutator extends AxisMutator{
        protected ChromosomeMutator() {}

    }





}
