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


public interface SnpFacade extends FeatureFacade {
   public static final String POLY_ID_PROP                  = "poly_id";
   public static final String ACCESSION_ID_PROP             = "accession_id";
   public static final String VERSION_PROP                  = "poly_version";
   public static final String MUTATION_TYPE_NAME_PROP       = "mutation_type_name";
   public static final String VALIDATION_STATUS_ID_PROP     = "validation_status_id";
   public static final String VALIDATION_STATUS_NAME_PROP   = "validation_status_name";
   public static final String ALLELE_PROP                   = "allele";
   public static final String NUM_DOMAINS_PROP              = "num_domains";
   public static final String DOMAIN_TYPE_PROP              = "domain_type";
   public static final String DOMAIN_MRNA_NAME_PROP         = "mrna_name";
   public static final String NUM_METHODS_PROP              = "num_methods";
   public static final String METHOD_NAME_PROP              = "method_name";
   public static final String METHOD_NAME_INSTANCE_ID_PROP  = "instance_id";
}