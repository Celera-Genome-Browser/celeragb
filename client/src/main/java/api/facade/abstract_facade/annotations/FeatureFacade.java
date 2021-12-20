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
package api.facade.abstract_facade.annotations;

import api.facade.abstract_facade.fundtype.AlignableGenomicEntityLoader;
import api.stub.data.NoData;
import api.stub.data.OID;
import api.stub.data.ReplacementRelationship;

public interface FeatureFacade extends AlignableGenomicEntityLoader {

   //--------------------------------------------------------------------------
   // Definition of property name constants that can be used in calls to
   // getProperty inherited from GenomicFacade.
   //
   // NOTE: Changes to these constants must be duplicated in the PL/SQL
   // package API_PROP_NAME_PKG.
   //--------------------------------------------------------------------------

   public static final String DISPLAY_PRIORITY_PROP         = "display_priority";
   public static final String FEATURE_ID_PROP               = AlignableGenomicEntityLoader.ID_PROP;
   public static final String FEATURE_STATUS_PROP           = "feature_status";
   public static final String FEATURE_TYPE_PROP             = "feature_type";
   public static final String EVIDENCE_ID_PROP              = "evidence_id";
   public static final String GENOMIC_AXIS_ID_PROP          = "axis_id";
   public static final String GENOMIC_AXIS_NAME_PROP        = "axis_name";
   public static final String IS_CHILD_PROP                 = "is_child";
   public static final String IS_COMPOSITE_PROP             = "is_composite";
   public static final String IS_CURATED_PROP               = "is_child";
   public static final String CREATED_BY_PROP               = "created_by";
   public static final String DATE_CREATED_PROP             = "date_created";
   public static final String CURATED_BY_PROP               = "curated_by";
   public static final String DATE_CURATED_PROP             = "date_curated";
   public static final String NUM_CONTIG_LOCATIONS_PROP     = "num_contig_locations";
   public static final String NUM_ALIGNMENTS_PROP           = "alignments";
   public static final String OLD_FEATURE_ID_PROP           = "old_feature_id";
   public static final String PARENT_FEATURE_ID_PROP        = "parent_feature_id";
   public static final String REL_ASSEMBLY_VERSION_PROP     = "rel_assembly_version";
   public static final String REVIEWED_BY_PROP              = "reviewed_by";
   public static final String DATE_REVIEWED_PROP            = "date_reviewed";
   public static final String GROUP_TAG_PROP                = "group_tag";
   public static final String GROUP_TAG_ID_PROP             = "group_tag_id";
   public static final String RELEASE_STATUS_PROP           = "release_status";
   public static final String SUBJECT_LEFT_PROP             = "subject_left";
   public static final String SUBJECT_RIGHT_PROP            = "subject_right";
   public static final String GROUP_TAG_DESCRIPTION_PROP    = "group_tag_description";
   public static final String FEATURE_GROUP_NAME_PROP       = "feature_group_name";
   public static final String FEATURE_GROUP_DESC_PROP       = "feature_group_description";
   public static final String QUERY_DATASET_NAME_PROP       = "query_dataset_name";
   public static final String QUERY_DATASET_VERSION_PROP    = "query_dataset_version";
   public static final String SUBJECT_DATASET_NAME_PROP     = "subject_dataset_name";
   public static final String SUBJECT_DATASET_VERSION_PROP  = "subject_dataset_version";
   public static final String ALGORITHM_NAME_PROP           = "algorithm_name";
   public static final String ALGORITHM_VERSION_PROP        = "algorithm_version";
   public static final String ALGORITHM_PARAMETERS_PROP     = "algorithm_parameters";
   public static final String ORDER_NUM_PROP                = "order_num";
   public static final String TRANSCRIPT_ACCESSION_PROP     = "transcript_accession";
   public static final String GENE_ACCESSION_PROP           = "gene_accession";
   public static final String TRANSLATION_POSITION_ID_PROP  = "translation_position_id";

   /*
    * These constants are not in CPW, but it is here to allow the client code compile.
    */
   public static final String ENTITY_ODDS_PROP              = "odds";
   public static final String ENTITY_IS_FRAME_PROP          = "is_frame";

   // These constants are not used in CPW
   //public static final String LAST_VALID_ASSEMBLY_PROP   = "last_valid_assembly";

   OID [] retrieveEvidence(OID featureOID) throws NoData;

   ReplacementRelationship retrieveReplacedFeatures(OID featureOID, long assemblyVersionOfReplacedFeatures) throws NoData;
}
