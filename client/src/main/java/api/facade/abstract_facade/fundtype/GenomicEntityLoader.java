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
package api.facade.abstract_facade.fundtype;

import api.entity_model.model.fundtype.EntityType;
import api.stub.data.*;

/**
 * Title:        GBAPI
 * Description:  Definition of an interface that describes a protocol
 *               independant mechanism for deferred loading of GenomicEntity
 *               internal state.
 * @author       James Baxendale
 * @version $Id$
 */
// GenomicEntityLoader
public interface GenomicEntityLoader {

   public static final String ID_PROP                     = "id";

   public static final String REMARK_PROP                 = "remark";

   public static final String ALIAS_NAME_PROP             = "alias_name";
   public static final String ALIAS_TYPE_PROP             = "alias_type";
   public static final String ALIAS_AUTHORITY_PROP        = "alias_authority";
   public static final String ALIAS_RANK_PROP             = "alias_rank";
   public static final String ALIAS_SOURCE_PROP           = "alias_source";
   public static final String ALIAS_STATUS_PROP           = "alias_status";

   public static final String NUM_COMMENTS_PROP           = "comments";
   public static final String NUM_ALIASES_PROP            = "aliases";
   public static final String LAST_ALIAS_PROP             = "last_alias";

// public static final String PRIMARY_NAME_PROP  = "primary_name";

   GenomicProperty[] getProperties
    (OID genomicOID,
     EntityType dyanmicType,
     boolean deepLoad);

  GenomicProperty[] expandProperty
    (OID genomicOID,
     String propertyName,
     EntityType dyanmicType,
     boolean deepLoad)
    throws NoData;

  GenomicEntityAlias[] getAliases( OID featureOID ) throws NoData;

  GenomicEntityComment[] getComments( OID featureOID ) throws NoData;
}
