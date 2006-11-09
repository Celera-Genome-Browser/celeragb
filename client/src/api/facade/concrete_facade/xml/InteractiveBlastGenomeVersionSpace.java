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

import api.entity_model.model.genetics.GenomeVersion;
import api.entity_model.model.genetics.Species;
import api.stub.data.OID;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Title:        Single-file genome version space, which always assumes
 *               that the query is relevant.
 * Description:  Confines for a genome version that comes from a single input
 *               file.
 * @author Les Foster
 * @version $Id$
 */
public class InteractiveBlastGenomeVersionSpace implements GenomeVersionSpace {

  //--------------------------------CONSTANTS
  private static final Iterator EMPTY_ITERATOR = new ArrayList().iterator();

  //--------------------------------MEMBER VARIABLES
  private List loaderList = new ArrayList();

  //--------------------------------CONSTRUCTORS
  /** simplest constructor. */
  public InteractiveBlastGenomeVersionSpace() {
  } // End constructor

  //--------------------------------PUBLIC INTERFACE
  /** Here is the where the ONE and only ONE loader may be added as a source */
  public void addLoader(XmlLoader loader) {
    // For the sake of consistency, and because we are returning
    // collections elsewhere, this single loader will be stored
    // in a collection.
    if (loaderList.size() > 0)
      throw new IllegalStateException("Only one workspace file may be loaded during a session");
    loaderList.add(loader);
  } // End method

  /** Tells which file provides the workspace input. */
  public String getWorkspaceFileName() {
    if (loaderList.size() == 0) {
      return null;
    } // Nothing to report
    else {
      XmlLoader loader = (XmlLoader)loaderList.get(0); // Expect only one.
      return loader.getLoadedFileNames()[0];
    } // Find loader's file name.
  } // End method

  //--------------------------------IMPLEMENTS GenomeVersionSpace
  /** This space is not a source for independent genome versions. */
  public GenomeVersion[] getGenomeVersions() {
    return new GenomeVersion[0];
  } // End method

  public String getSourceOf(OID speciesOID) {
    return null;
  } // End method

  /** No species is known. */
  public Species getSpeciesOf(OID speciesOID) {
    return null;
  } // End method

  /**
   * Tells if it is possible, with hopefully minimal time wastage, for the
   * genome version space to have ANY sources worth searching in the genome version.
   * Used to optimize searches against database, by limiting flatfile overhead.
   */
  public boolean hasSearchableInputSourcesInGenomeVersion(int genomeVersionId) {
    return getSearchableInputSources(genomeVersionId).hasNext();
  } // End method

  /** Returns true if anything has been loaded. */
  public boolean hasLoaders() {
    return loaderList.size() > 0;
  } // End method

  /** @Todo fill in these blanks. */
  public void registerSpecies(String filename, Species species) {
  } // End method

  /**
   * This space will have one loader for its source of input, and it will return
   * that loader always.
   */
  public XmlLoader getLoaderForSpecies(OID speciesOID) {
    return (XmlLoader)loaderList.get(0);
  } // End method

  public Iterator getInputSourcesForAxisOID(OID axisOid) {
    if (loaderList.size() == 0)
      return EMPTY_ITERATOR;

    XmlLoader loader = (XmlLoader)loaderList.get(0);
    if (loader == null)
      return EMPTY_ITERATOR;

    Set oidSet = loader.getReferencedOIDSet();
    if (oidSet != null) {
      OID nextOid = null;
      for (Iterator it = oidSet.iterator(); it.hasNext(); ) {
        nextOid = (OID)it.next();
        if (nextOid.equals(axisOid))
          return loaderList.iterator();
      } // For all members of the set.
    } // Got set of referenced OIDs.

    return EMPTY_ITERATOR;
  } // End method

  /** Returns sources ready for use. */
  public Iterator getOpenSources() {
    return loaderList.iterator();
  } // End method

  /** Returns all sources which may be searched that lie in the genome version. */
  public Iterator getSearchableInputSources(int genomeVersionId) {
    return getGenomicAxisInputSources(genomeVersionId);
  } // End method

  /** Returns all sources which can provide alignments against an axis in the genome version. */
  public Iterator getGenomicAxisInputSources(int genomeVersionId) {
    if (loaderList.size() == 0)
      return new ArrayList().iterator();

    // Expect one and only one loader in the list!
    XmlLoader loader = (XmlLoader)loaderList.get(0);
    if (loader == null)
      return new ArrayList().iterator();

    Set oidSet = loader.getReferencedOIDSet();
    if (oidSet != null) {
      OID nextOid = null;
      for (Iterator it = oidSet.iterator(); it.hasNext(); ) {
        nextOid = (OID)it.next();
        if (nextOid.getGenomeVersionId() == genomeVersionId)
          return loaderList.iterator();
      } // For all members of the set.
    } // Got set of referenced OIDs.

    return EMPTY_ITERATOR;

  } // End method

  /**
   * Returns all data sources within the genome version whose ID is given,
   * and which represent a genomic axis.
   */
  public Iterator getDataSourcesRepresentingGenomicAxes(int genomeVersionID) {
    return EMPTY_ITERATOR;
  } // End method

} // End class
