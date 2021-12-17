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
package api.facade.concrete_facade.xml;

import api.entity_model.model.alignment.Alignment;
import api.stub.data.OID;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Title:        Xml Workspace Facade
 * Description:  Workspace-only specific facade.  Time of writing: this includes
 *               obsoleted features, which are unavailable in other facades.
 * @author
 * @version $Id$
 */

public class XmlWorkspaceFacade {

  //-----------------------------------------INSTANCE VARIABLES
  private FileGenomeVersionSpace genomeVersionSpace;

  //-----------------------------------------CONSTRUCTORS
  /** Construct with space to be queried for all data. */
  public XmlWorkspaceFacade(FileGenomeVersionSpace genomeVersionSpace) {
    if (genomeVersionSpace == null)
      throw new IllegalArgumentException("Constructor restriction violated: must not construct XmlWorkspaceFacade with null FileGenomeVersionSpace");
    this.genomeVersionSpace = genomeVersionSpace;
  } // End constructor

  //-----------------------------------------INTERFACE METHODS
  /**
   * Get the "obsoleted" workspace CuratedFeature(s) that do not have any
   * super features (ie are root features) for the given axis OID.
   *
   * @returns the collection of "obsoleted" workspace features that have no
   * super features.
   */
  public Set getObsoletedRootFeatures(OID axisOid) {
    Set returnSet = new HashSet();
    WorkspaceXmlLoader nextLoader = null;
    for (Iterator it = genomeVersionSpace.getOpenSources(); it.hasNext(); ) {
      nextLoader = (WorkspaceXmlLoader)it.next();
      if (nextLoader == null)
        continue;
      if (nextLoader.getReferencedOIDSet().contains(axisOid))
        returnSet.addAll(nextLoader.getObsoleteRootFeatures(axisOid));
    } // For all loaders.

    return returnSet;
  } // End method

  /**
   * Get the "obsoleted" workspace CuratedFeature(s) that are
   * sub-structure of the given feature OID for the given axis OID.
   *
   * @param OID axisOid axis referenced by features.
   * @param OID featureOid root feature of obsoleted ones.
   * @returns a Set of CuratedFeature(s).
   */
  public Set getObsoletedSubStructureOfFeature(OID axisOid, OID featureOid) {
    Set returnSet = new HashSet();
    WorkspaceXmlLoader nextLoader = null;
    for (Iterator it = genomeVersionSpace.getOpenSources(); it.hasNext(); ) {
      nextLoader = (WorkspaceXmlLoader)it.next();
      if (nextLoader == null)
        continue;
      if (nextLoader.getReferencedOIDSet().contains(axisOid))
        returnSet.addAll(nextLoader.getObsoleteNonRootFeatures(axisOid, featureOid));
    } // For all loaders.

    return returnSet;
  } // End method

  /**
   * Get the Alignment of the given "obsoleted" workspace CuratedFeature OID
   * for the given axis OID.
   * null If the argument CuratedFeature does not exist in the Workspace.
   *
   * @returns an Alignment
   */
  public Alignment getObsoletedAlignmentForWorkspaceOid(OID axisOid, OID featureOid) {
    Alignment returnAlignment = null;
    XmlLoader nextLoader = null;

    for (Iterator it = genomeVersionSpace.getOpenSources(); it.hasNext() && (returnAlignment == null); ) {
      nextLoader = (XmlLoader)it.next();
      if (nextLoader == null)
        continue;
      if (nextLoader.getReferencedOIDSet().contains(axisOid)) {
        if (nextLoader.featureExists(featureOid) && nextLoader.getAxisOidOfAlignment(featureOid).equals(axisOid))
          returnAlignment = nextLoader.getAlignmentForFeature(featureOid);
      } // Any refs to axis?
    } // For all loaders.

    return returnAlignment;
  } // End method

} // End class
