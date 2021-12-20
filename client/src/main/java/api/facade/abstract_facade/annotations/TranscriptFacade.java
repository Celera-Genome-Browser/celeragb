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


public interface TranscriptFacade extends FeatureFacade {

   //--------------------------------------------------------------------------
   // Definition of property name constants that can be user in calls to
   // getProperty inherited from GenomicFacade.
   //
   // NOTE: Changes to these constants must be duplicated in the PL/SQL
   // package API_PROP_NAME_PKG.
   //--------------------------------------------------------------------------
   public static final String GENE_ACCESSION_PROP         = "gene_accession";
   public static final String HAS_MULTI_EXON_PROP         = "has_multi_exon";
   public static final String IS_CURATED_PROP             = "is_curated";
   public static final String CURATION_FLAGS_PROP         = "curation_flags";
   public static final String NA_SEQ_PROP                 = "na_seq";
   public static final String TRANSCRIPT_ACCESSION_PROP   = "transcript_accession";
   public static final String PROTEIN_ACCESSION_PROP      = "protein_accession";

}
