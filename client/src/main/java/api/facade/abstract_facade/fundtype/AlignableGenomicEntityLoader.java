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

import api.entity_model.model.alignment.Alignment;
import api.stub.data.OID;

/**
 * Title:        GBAPI
 * Description:  Definition of an interface that describes a protocol
 *               independant mechanism for deferred loading of
 *               AlignableGenomicEntity instances internal state.
 * @author       James Baxendale
 * @version $Id$
 */
/**
 *  @todo rename to GenomicEntityLoader
 */

public interface AlignableGenomicEntityLoader extends GenomicEntityLoader {

  //--------------------------------------------------------------------------
  // Definition of property name constants that can be used in calls to
  // getProperty inherited from GenomicEntityLoaderLoader.
  //
  // NOTE: Changes to these constants must be duplicated in the PL/SQL
  // package API_PROP_NAME_PKG.
  //--------------------------------------------------------------------------
  public static final String AXIS_GA_PROP               = "axis_ga";
  public static final String AXIS_BEGIN_PROP            = "axis_begin";
  public static final String AXIS_END_PROP              = "axis_end";
  public static final String ENTITY_LENGTH_PROP         = "entity_length";
  public static final String GENOME_ID_PROP              = "genome_id";

  // This constant is not in CPW
  //public static final String ASSEMBLY_VERSION           = "assembly_version";

   /**
    * @todo need to work out which of these is being used and remove
    * one.
    */
   public static final String ENTITY_ORIENTATION_PROP    = "entity_orientation";
   public static final String ORIENTATION_PROP           = "orientation";

   public Alignment [] getAlignmentsToAxes( OID entityOID );
}
