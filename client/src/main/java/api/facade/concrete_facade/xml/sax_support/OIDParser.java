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

/**
 * Title:        Genomic Browser<p>
 * Description:  Implementer will parse OIDs and apply constraints to their being
 *     accepted by the application.<p>
 * Company:      []<p>
 * @author Les Foster
 * @version
 */
package api.facade.concrete_facade.xml.sax_support;

import api.stub.data.OID;

public interface OIDParser {

  //------------------------------BEGIN ABSTRACT METHODS
  /**
   * Builds a contig OID with all restrictions and translations required by the
   * sub class (of this class)' for the OID of a contig.
   *
   * This is an example of a 'Template Method' pattern (Gamma, et al).
   * It is used in the super class, but implemented in the subclass.
   *
   * @param String idstr a string with PREFIX:ddddddddd
   *     where the d's represent a decimal, integer (long) number.
   * @return OID an OID with whatever translations and restrictions
   *   are required for the sublassed loader.
   */
  public abstract OID parseContigOIDTemplateMethod(String idstr);

  /**
   * Builds an evidence OID with all restrictions and translations required by the
   * sub class (of this class)' for the OID of an evidence reference OID.
   *
   * This is an example of a 'Template Method' pattern (Gamma, et al).
   * It is used in the super class, but implemented in the subclass.
   *
   * @param String idstr a string with PREFIX:ddddddddd
   *     where the d's represent a decimal, integer (long) number.
   * @return OID an OID with whatever translations and restrictions
   *   are required for the sublassed loader.
   */
  public abstract OID parseEvidenceOIDTemplateMethod(String idstr);

  /**
   * Builds a feature OID with all restrictions and translations required by the
   * sub class (of this class)' for the OID of a feature.
   *
   * This is an example of a 'Template Method' pattern (Gamma, et al).
   * It is used in the super class, but implemented in the subclass.
   *
   * @param String idstr a string with PREFIX:ddddddddd
   *     where the d's represent a decimal, integer (long) number.
   * @return OID an OID with whatever translations and restrictions
   *   are required for the sublassed loader.
   */
  public abstract OID parseFeatureOIDTemplateMethod(String idstr);

} // End interface: OIDParser
