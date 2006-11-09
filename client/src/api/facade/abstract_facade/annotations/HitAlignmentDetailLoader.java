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
package api.facade.abstract_facade.annotations;

import api.stub.data.NoData;
import api.stub.data.OID;
import api.stub.data.SubjectDefinition;

public interface HitAlignmentDetailLoader extends FeatureFacade {

  public static final String ZIPPED_ALIGNMENT_PROP    = "zipped_alignment";
  public static final String SUBJECT_SEQ_LENGTH_PROP  = "subject_seq_length";
  public static final String NUM_SUBJ_DEFNS_PROP      = "num_subj_defns";
  public static final String ACCESSSION_NUM_PROP      = "accession_number";
  public static final String ALT_ACCESSION_PROP       = "alternate_accession";
  public static final String ALT_ACCESSIONS_PROP      = "alternate_accessions";
  public static final String ISSUING_AUTHORITY_PROP   = "issuing_authority";
  public static final String SPECIES_PROP             = "species";
  public static final String DESCRIPTION_PROP         = "description";
  public static final String KEYWORD_PROP             = "keyword";


  String getQueryAlignedResidues( OID alignmentDetailOID ) throws NoData;
  String getSubjectAlignedResidues( OID alignmentDetailOID ) throws NoData;
  SubjectDefinition [] getSubjectDefinitions( OID featureOID ) throws NoData;
}
