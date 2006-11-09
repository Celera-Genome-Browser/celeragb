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

import api.entity_model.model.fundtype.EntityType;
import api.stub.data.NoData;
import api.stub.data.OID;
import api.stub.data.SubjectDefinition;
import api.stub.sequence.Sequence;

public interface HitAlignmentFacade extends FeatureFacade {

   //--------------------------------------------------------------------------
   // Definition of property name constants that can be user in calls to
   // getProperty inherited from GenomicFacade.
   //
   // NOTE: Changes to these constants must be duplicated in the PL/SQL
   // package API_PROP_NAME_PKG.
   //--------------------------------------------------------------------------
   public static final String ACCESSSION_NUM_PROP        = "accession_number";
   public static final String ALT_ACCESSION_PROP         = "alternate_accession";
   public static final String ALT_ACCESSIONS_PROP        = "alternate_accessions";
   public static final String DESCRIPTION_PROP           = "description";
   public static final String ISSUING_AUTHORITY_PROP     = "issuing_authority";
   public static final String KEYWORD_PROP               = "keyword";
   public static final String NUM_SUBJ_DEFNS_PROP        = "num_subj_defns";
   public static final String NUM_SUBJ_SEQ_HITS_PROP     = "num_subj_seq_hits";
   public static final String SPECIES_PROP               = "species";
   public static final String SUBJECT_SEQ_LENGTH_PROP    = "subject_seq_length";
   public static final String SUBJECT_SEQ_ID_PROP        = "subject_seq_id";
   public static final String FLAG_PROP                  = "flag";
   public static final String PERCENT_IDENTITY_PROP      = "percent_identity";
   public static final String PERCENT_HIT_IDENTITY_PROP  = "percent_hit_identity";
   public static final String PERCENT_LENGTH_PROP        = "percent_length";
   public static final String RANK_PROP                  = "rank";

   SubjectDefinition[] getSubjectDefinitions( OID featureOID ) throws NoData;
   Sequence getSubjectSequence( OID featureOID, EntityType entityType ) throws NoData;
}
