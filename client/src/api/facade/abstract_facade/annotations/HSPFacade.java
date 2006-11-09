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

public interface HSPFacade extends HitAlignmentDetailLoader {

   //--------------------------------------------------------------------------
   // Definition of property name constants that can be user in calls to
   // getProperty inherited from GenomicFacade.
   //
   // NOTE: Changes to these constants must be duplicated in the PL/SQL
   // package API_PROP_NAME_PKG.
   //--------------------------------------------------------------------------
   public static final String PERCENT_IDENTITY_PROP  = "percent_identity";
   public static final String ALIGNMENT_LENGTH_PROP  = "alignment_length";
   public static final String BIT_SCORE_PROP         = "bit_score";
   public static final String E_VAL_PROP             = "e_val";

   public static final String NUM_MISMATCHES_PROP    = "num_mismatches";
   public static final String NUM_IDENTICAL_PROP     = "num_identical";
   public static final String NUM_SIM_OR_POS_PROP    = "num_sim_or_pos";

   // The following constants are not exist in CPW. There are here just to make XML code to compile
   public static final String ASSEMBLY_VERSION_PROP  = "assembly_version";
   public static final String NUM_GAPS_PROP          = "num_gaps";
   public static final String SUM_E_VAL_PROP         = "summary_e_val";
   public static final String INDIVIDUAL_E_VAL_PROP  = "ind_e_val";
   public static final String QUERY_FRAME_PROP       = "query_frame";
   public static final String SUBJECT_FRAME_PROP     = "subject_frame";
}
