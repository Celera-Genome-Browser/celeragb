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
package api.entity_model.model.assembly;

import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.AlignmentNotAllowedException;
import api.entity_model.model.alignment.PartialEntityAlignment;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.LoadRequest;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.facade.abstract_facade.fundtype.GenomicEntityLoader;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.OID;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version $Id$
 */


public class Contig extends Axis {

//****************************************
//*  Public methods
//****************************************

//*  Construction
  public Contig(OID oid, String displayName)
  {
    this(oid, displayName, 0);
  }

  public Contig( OID oid, String displayName, int magnitude)
  {
    this(oid, displayName, magnitude, null);
  }

  public Contig( OID oid, String displayName, int magnitude, FacadeManagerBase readFacadeManager)
  {
    super(EntityType.getEntityTypeForValue(EntityTypeConstants.Contig), oid, displayName, magnitude, readFacadeManager);
  }

    /**
     * Accepts visitors that implement GenomicEntityVisitor. acceptVisitorForSelf
     * is specialized by every subclass of GenomicEntity to call the appropriate
     * "visit...(...)" function on the passed visitor.
     * @param theVisitor the visitor.
     */
    public void acceptVisitorForSelf(GenomicEntityVisitor theVisitor) {
        try {
                theVisitor.visitContig(this);
        }
        catch (Exception ex) {
                handleException(ex);
        }
    }



//****************************************
//*  Protected methods
//****************************************

    protected void willAcceptAlignmentToAxis(Alignment alignmentToAxis)
      throws AlignmentNotAllowedException{

      if (!(alignmentToAxis.getAxis() instanceof GenomicAxis)) throw
        new AlignmentNotAllowedException("Contigs can only be aligned to a GenomicAxis",
         alignmentToAxis);

      if (!(alignmentToAxis instanceof PartialEntityAlignment)) {
         try {
            super.willAcceptAlignmentToAxis (alignmentToAxis);
         }
         catch (AlignmentNotAllowedException anaEx) {
            anaEx.addReason("The alignment passed must be a PartialEntityAlignment to "+
             "align a Contig multiple times to the same axis");
             throw anaEx;
         }
      }
    }

    protected void willAcceptAlignmentToEntity(Alignment alignmentToEntity)
       throws AlignmentNotAllowedException{
       throw new AlignmentNotAllowedException("Contigs currently cannot accept aligned Entities",
         alignmentToEntity);
    }

    protected int getPredictedNumberOfAlignmentsToEntities() {
      return 0;
    }

    /**
     * @todo Code this
     */
    protected boolean willAcceptLoadRequestForAlignedEntities(LoadRequest loadRequest){
       return true;
    }

    protected GenomicEntityLoader getDataLoader() {
      try {
       return getLoaderManager().getContigFacade();
      }
      catch (Exception ex) {
        handleException(ex);
        return null;
      }
    }

//****************************************
//*  Package methods
//****************************************



}
