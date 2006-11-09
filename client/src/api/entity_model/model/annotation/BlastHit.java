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
 * Title:        Genome Browser<p>
 * Description:  <p>
 * @author Peter Davies
 * @version $Id$
 *
 * A Blast Hit feature.
 */
package api.entity_model.model.annotation;

import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.model.fundtype.EntityType;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.OID;


public class BlastHit extends HitAlignmentFeature implements SuperFeature {
    public BlastHit(OID oid, String displayName, EntityType type, 
                    String discoveryEnvironment)
             throws InvalidFeatureStructureException {
        this(oid, displayName, type, discoveryEnvironment, null, null, 
             FeatureDisplayPriority.DEFAULT_PRIORITY);
    }

    public BlastHit(OID oid, String displayName, EntityType type, 
                    String discoveryEnvironment, 
                    FacadeManagerBase readFacadeManager, Feature superFeature, 
                    byte displayPriority)
             throws InvalidFeatureStructureException {
        super(oid, displayName, type, discoveryEnvironment, readFacadeManager, 
              superFeature, displayPriority);
    }

    /**
     * Get the raw alignment text...
     * @ToDo: Need facade...
    public String getRawAlignmentText
      (OID featureOID)
      throws BlastHitError, NoData
    {
      api.facade.abstract_facade.annotations.BlastHitFacade counterpart
        = (api.facade.abstract_facade.annotations.BlastHitFacade)getServerSideCounterpart();
    
      String notCachedText = null;
      try
      {
        notCachedText = counterpart.getRawAlignmentText(getOid());
      }
      catch (Exception ex)
      {
        api.bizobj.fundtype.BizObjLoggerAndExceptionHandler.getSingleton().handleException(ex);
        counterpart = null;
      }
      if (notCachedText == null)
      {
        notCachedText = "Raw alignment unavailable";
      }
      return notCachedText;
    }
     */
    /**
     * Get the server side counterpart...
    protected GenomicFacade getServerSideCounterpart()
    {
      FacadeManagerBase facadeMgr = FacadeManager.getFacadeManager();
      api.facade.abstract_facade.annotations.BlastHitFacade
        counterpart = null;
      try
      {
        counterpart = (api.facade.abstract_facade.annotations.BlastHitFacade)
                           facadeMgr.getFacade(getFeatureType());
      }
      catch (Exception ex)
      {
        api.bizobj.fundtype.BizObjLoggerAndExceptionHandler.getSingleton().handleException(ex);
      }
      return counterpart;
    }
     */

    //== Feature Parent Child Management Methods ==============================

    /**
     * Determine if I will accept a Feature as a super feature of mine.
     * This is usually called right before a call to setSuperFeature on the
     * mutator class.
     * Subclasses can over-ride this method to make sure that there is
     * the correct hierarchy of containment... such as Gene, Transcript, Exon.
     * Subclasses should ALWAYS be more restrictive than thier super classes.
     */
    public boolean willAcceptSuperFeature(Feature newSuperFeature) {
        if (!super.willAcceptSuperFeature(newSuperFeature)) {
            return false;
        }

        // Can't have a parent.
        return false;
    }

    /**
     * Determine if I will accept a Feature as a super feature of mine.
     * This is usually called right before a call to setSuperFeature on the
     * mutator class.
     * Subclasses can over-ride this method to make sure that there is
     * the correct hierarchy of containment... such as Gene, Transcript, Exon.
     * Subclasses should ALWAYS be more restrictive than thier super classes.
     */
    public boolean willAcceptSubFeature(Feature newSubFeature) {
        if (!super.willAcceptSubFeature(newSubFeature)) {
            return false;
        }

        // Must be some kind of Transcript
        return (newSubFeature instanceof HSPFeature);
    }

    /**
     * Accepts visitors that implement GenomicEntityVisitor. acceptVisitorForSelf
     * is specialized by every subclass of GenomicEntity to call the appropriate
     * "visit...(...)" function on the passed visitor.
     * @param theVisitor the visitor.
     */
    public void acceptVisitorForSelf(GenomicEntityVisitor theVisitor) {
        try {
            theVisitor.visitBlastHit(this);
        } catch (Exception ex) {
            handleException(ex);
        }
    }
}