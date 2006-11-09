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

public interface GenePredictionDetailFacade extends FeatureFacade {

   //--------------------------------------------------------------------------
   // Definition of property name constants that can be user in calls to
   // getProperty inherited from GenomicFacade.
   //
   // NOTE: Changes to these constants must be duplicated in the PL/SQL
   // package API_PROP_NAME_PKG.
   //--------------------------------------------------------------------------
   public static final String ASSEMBLY_VERSION_PROP        = "assembly_version";
   public static final String CODING_REG_SCORE_PROP        = "coding_reg_score";
   public static final String DETAIL_TYPE_PROP             = "detail_type";
   public static final String DONOR_OR_TERM_SIG_SCORE_PROP = "donor_or_term_sig_score";
   public static final String EXON_PROB_PROP               = "exon_prob";
   public static final String EXON_SCORE_PROP              = "exon_score";
   public static final String GENE_FRAME_PROP              = "frame";
   public static final String INIT_OR_ACC_SCORE_PROP       = "init_or_acc_score";
}
