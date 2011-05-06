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
package api.facade.concrete_facade.gff3.feature_facade;

import api.entity_model.model.alignment.Alignment;
import api.facade.concrete_facade.gff3.FileGenomeVersionSpace;
import api.stub.data.OID;

import java.util.Collections;
import java.util.Set;

/**
 * Title:        Gff3 Workspace Facade
 * Description:  Stubbed implementation: no workspace data held in GFF3 at time-of-writing.
 * @author	Les Foster
 */

public class Gff3WorkspaceFacade {

  //-----------------------------------------CONSTRUCTORS
  /** Construct with space to be queried for all data. */
  public Gff3WorkspaceFacade(FileGenomeVersionSpace genomeVersionSpace) {
  } // End constructor

  //-----------------------------------------INTERFACE METHODS
  /**
   * Stubbed: returns empty set.
   *
   * @returns the collection of "obsoleted" workspace features that have no
   * super features.
   */
  public Set getObsoletedRootFeatures(OID axisOid) {
    return Collections.EMPTY_SET;
  } // End method

  /**
   * Stubbed: returns empty set.
   *
   * @param OID axisOid axis referenced by features.
   * @param OID featureOid root feature of obsoleted ones.
   * @returns a Set of CuratedFeature(s).
   */
  public Set getObsoletedSubStructureOfFeature(OID axisOid, OID featureOid) {
	  return Collections.EMPTY_SET;
  } // End method

  /**
   * Stubbed.  Returns null.
   * @returns an Alignment
   */
  public Alignment getObsoletedAlignmentForWorkspaceOid(OID axisOid, OID featureOid) {
    Alignment returnAlignment = null;
    return returnAlignment;
  } // End method

} // End class
