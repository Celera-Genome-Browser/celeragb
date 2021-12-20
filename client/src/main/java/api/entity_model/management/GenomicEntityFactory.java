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
package api.entity_model.management;
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
*********************************************************************/

import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.GenomicEntity;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.OID;

/**
 *  Factory interface for creating specific Genomic Entity subtypes using
 *  a parameterized factory method (parameterized on EntityType).
 *  All products must share the GenomicEntity interface.
 */
public abstract class GenomicEntityFactory
{
  /**
   * Tries to ensure that the only class that creates an instance of a
   * GenomicEntityFactory is the ModelMgr.
   */
  public GenomicEntityFactory( Integer creationKey )
  {
    if (creationKey.intValue()!=ModelMgr.getModelMgr().hashCode()) throw new
      IllegalStateException(" You must get the EntityInterval Factory from the ModelMgr!! ");
  }

  abstract public GenomicEntity create
    (
      OID oid,
      String displayName,
      EntityType type,
      String discoveryEnvironment
    );

  abstract public GenomicEntity create
    (
      OID oid,
      String displayName,
      EntityType type,
      String discoveryEnvironment,
      String subClassification,
      GenomicEntity parent,
      byte displayPriority
    );

  abstract public GenomicEntity create
    (
      OID oid,
      String displayName,
      EntityType type,
      String discoveryEnvironment,
      String subClassification,
      GenomicEntity parent,
      byte displayPriority,
      FacadeManagerBase overrideLoaderManager
    );

}
