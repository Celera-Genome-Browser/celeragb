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
package api.facade.abstract_facade.fundtype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NavigationConstants {

   // ControlledVocabulary Name
   public static final String NAVIGATION_VOCAB_INDEX     = "NAVIGATION_PATH_TYPE";

   // These should be the same as they are defined in the
   // PL-SQL Package api_navigation_pkg and should match
   // the definitions in the ControlledVocabulary
   // (resource.shared.ControlledVocabulary.properties)
   public static final int BAD_INDEX                     = -1;
   public static final int BAC_ACCESSION_INDEX           =  0;
   public static final int CHROMOSOME_NAME_INDEX         =  1;
   public static final int CONS_SEGMENT_ACCESSION_INDEX  =  2;
   public static final int CONTIG_OID_INDEX              =  3;
   public static final int FEATURE_OID_INDEX             =  4;
   public static final int GENE_ACCESSION_INDEX          =  5;
   public static final int GENE_ONTOLOGY_NAME_INDEX      =  6;
   public static final int GENOMIC_AXIS_NAME_INDEX       =  7;
   public static final int HIT_ALIGN_ACCESSION_INDEX     =  8;
   public static final int PROTEIN_ACCESSION_INDEX       =  9;
   public static final int REG_REGION_ACCESSION_INDEX    = 10;
   public static final int POLY_ACCESSION_INDEX          = 11;
   public static final int SPECIES_OID_INDEX             = 12;
   public static final int STS_NAME_INDEX                = 13;
   public static final int SUBSEQ_OID_INDEX              = 14;
   public static final int TRANSCRIPT_ACCESSION_INDEX    = 15;
   public static final int UNKNOWN_OID_INDEX             = 16;


   // Short Names
   public static final String BAC_ACCESSION_SN           = "bac_acc";
   public static final String CHROMOSOME_NAME_SN         = "chrom_name";
   public static final String CONS_SEGMENT_ACCESSION_SN  = "cons_segment_acc";
   public static final String CONTIG_OID_SN              = "contig_oid";
   public static final String FEATURE_OID_SN             = "feature_oid";
   public static final String GENE_ONTOLOGY_NAME_SN      = "gene_ont_name";
   public static final String GENE_ACCESSION_SN          = "gene_acc";
   public static final String GENOMIC_AXIS_NAME_SN       = "genomic_axis_name";
   public static final String HIT_ALIGN_ACCESSION_SN     = "hit_align_acc";
   public static final String PROTEIN_ACCESSION_SN       = "protein_acc";
   public static final String REG_REGION_ACCESSION_SN    = "reg_region_acc";
   public static final String POLY_ACCESSION_SN          = "poly_acc";
   public static final String SPECIES_OID_SN             = "species_oid";
   public static final String STS_NAME_SN                = "sts_name";
   public static final String SUBSEQ_OID_SN              = "sub_seq_oid";
   public static final String TRANSCRIPT_ACCESSION_SN    = "transcript_acc";
   public static final String UNKNOWN_OID_SN             = "unknown_oid";


   private static final Map navTargetTypeToInt;

   static {
      // This must match the settings for NAVIGATION_PATH_TYPE property in
      // resource.shared.ControlledVocabulary.properties
      HashMap tmpMap = new HashMap( 32 );
      tmpMap.put( BAC_ACCESSION_SN,          new Integer( BAC_ACCESSION_INDEX ) );
      tmpMap.put( CHROMOSOME_NAME_SN,        new Integer( CHROMOSOME_NAME_INDEX ) );
      tmpMap.put( CONS_SEGMENT_ACCESSION_SN, new Integer( CONS_SEGMENT_ACCESSION_INDEX ) );
      tmpMap.put( CONTIG_OID_SN,             new Integer( CONTIG_OID_INDEX ) );
      tmpMap.put( FEATURE_OID_SN,            new Integer( FEATURE_OID_INDEX ) );
      tmpMap.put( GENE_ONTOLOGY_NAME_SN,     new Integer( GENE_ONTOLOGY_NAME_INDEX ) );
      tmpMap.put( GENE_ACCESSION_SN,         new Integer( GENE_ACCESSION_INDEX ) );
      tmpMap.put( GENOMIC_AXIS_NAME_SN,      new Integer( GENOMIC_AXIS_NAME_INDEX ) );
      tmpMap.put( HIT_ALIGN_ACCESSION_SN,    new Integer( HIT_ALIGN_ACCESSION_INDEX ) );
      tmpMap.put( PROTEIN_ACCESSION_SN,      new Integer( PROTEIN_ACCESSION_INDEX ) );
      tmpMap.put( REG_REGION_ACCESSION_SN,   new Integer( REG_REGION_ACCESSION_INDEX ) );
      tmpMap.put( POLY_ACCESSION_SN,         new Integer( POLY_ACCESSION_INDEX ) );
      tmpMap.put( SPECIES_OID_SN,            new Integer( SPECIES_OID_INDEX ) );
      tmpMap.put( STS_NAME_SN,               new Integer( STS_NAME_INDEX ) );
      tmpMap.put( SUBSEQ_OID_SN,             new Integer( SUBSEQ_OID_INDEX ) );
      tmpMap.put( TRANSCRIPT_ACCESSION_SN,   new Integer( TRANSCRIPT_ACCESSION_INDEX ) );
      tmpMap.put( UNKNOWN_OID_SN,            new Integer( UNKNOWN_OID_INDEX ) );
      navTargetTypeToInt = Collections.unmodifiableMap( tmpMap );
   }

   public static int getNumberFromShortName( String shortName ) {
      Integer intStr = (Integer)navTargetTypeToInt.get( shortName );
      return ( ( intStr == null ) ? BAD_INDEX : intStr.intValue() );
   }
}
