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


public interface GeneFacade extends FeatureFacade {

   //--------------------------------------------------------------------------
   // Definition of property name constants that can be user in calls to
   // getProperty inherited from GenomicFacade.
   //
   // NOTE: Changes to these constants must be duplicated in the PL/SQL
   // package API_PROP_NAME_PKG.
   //--------------------------------------------------------------------------
   public static final String GENE_ACCESSION_PROP           = "gene_accession";
   public static final String IS_ALTER_SPLICE_PROP          = "is_alter_splice";
   public static final String IS_PSEUDO_GENE_PROP           = "is_pseudo_gene";

   public static final String NUM_ONTOLOGIES_PROP           = "num_ontologies";
   public static final String NUM_FAMILIES_PROP             = "num_families";

   public static final String ONTOLOGY_ID_PROP              = "ontology_id";
   public static final String ONTOLOGY_EVIDENCE_PROP        = "evidence";
   public static final String GENE_FAMILY_ID_PROP           = "gene_family_id";
   public static final String GENE_FAMILY_ACCESSION_PROP    = "gene_family_accession";
   public static final String GENE_FAMILY_AUTHORITY_PROP    = "gene_family_authority";
   public static final String REL_ASSEMBLY_VERSION_PROP     = "rel_assembly_version";
}


