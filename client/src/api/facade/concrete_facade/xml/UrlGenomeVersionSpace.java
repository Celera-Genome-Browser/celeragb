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
import api.stub.data.GenomeVersionInfo;
import api.stub.data.OID;

import java.util.*;

/**
 * Title:        Space in which genome version from single input file resides.
 * Description:  Confines for a genome version that comes from a single input
 *               file.
 * @author Les Foster
 * @version $Id$
 */
public class UrlGenomeVersionSpace implements GenomeVersionSpace {

  //--------------------------------CONSTANTS
  private static final Iterator EMPTY_ITERATOR = new ArrayList().iterator();

  //--------------------------------MEMBER VARIABLES
  private Map loaderMap = new HashMap();

  //--------------------------------CONSTRUCTORS
  /** simplest constructor. */
  public UrlGenomeVersionSpace() {
  } // End constructor

  //--------------------------------PUBLIC INTERFACE
  /**
   * Loaders may be added as sources.  Their applicable genome version info
   * is set as they are added.
   */
  public void addLoader(XmlLoader loader, GenomeVersionInfo info) {
    loaderMap.put(loader, info);
  } // End method

  /** Tells what provides the workspace input. */
  public String[] getUrlStrings() {
    if (loaderMap.size() == 0) {
      return new String[0];
    } // Nothing to report
    else {
      String[] returnArray = new String[loaderMap.size()];
      int i = 0;
      for (Iterator it = loaderMap.keySet().iterator(); it.hasNext(); i++) {
        returnArray[i] = ((XmlLoader)it.next()).getLoadedFileNames()[0];
      } // For all loaders.
      return returnArray;
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

  /** Returns true if anything has been loaded. */
  public boolean hasLoaders() {
    return loaderMap.size() > 0;
  } // End method

  /** @Todo fill in these blanks. */
  public void registerSpecies(String filename, Species species) {
  } // End method

  /**
   * Gets the loader corresponding to the species OID.
   * At time of writing, species representation may not be
   * done from data given in a service URL.
   */
  public XmlLoader getLoaderForSpecies(OID speciesOID) {
    return null;
  } // End method

  /** No genomic axis' info other than alignments from URL... */
  public Iterator getInputSourcesForAxisOID(OID axisOid) {
    return EMPTY_ITERATOR;
  } // End method

  /** Returns all sources which may be searched that lie in the genome version. */
  public Iterator getSearchableInputSources(int genomeVersionId) {
    return getGenomicAxisInputSources(genomeVersionId);
  } // End method

  /**
   * Tells if it is possible, with hopefully minimal time wastage, for the
   * genome version space to have ANY sources worth searching in the genome version.
   * Used to optimize searches against database, by limiting flatfile overhead.
   */
  public boolean hasSearchableInputSourcesInGenomeVersion(int genomeVersionId) {
    return getSearchableInputSources(genomeVersionId).hasNext();
  } // End method

  /** Features aligning to a genomic axis may be sourced via a URL. */
  public Iterator getGenomicAxisInputSources(int genomeVersionId) {
    XmlLoader loader = null;
    GenomeVersionInfo info = null;
    List returnList = new ArrayList();
    for (Iterator it = loaderMap.keySet().iterator(); it.hasNext(); ) {
      loader = (XmlLoader)it.next();

      // During the scope of this particular load, the genome version id will be
      // that found in the axis oid.
      //
      ((ServiceXmlLoader)loader).setGenomeVersionId(genomeVersionId);

      info = (GenomeVersionInfo)loaderMap.get(loader);

      // Currently, the info is set to null, but if it ever does appear,
      // it will exclude/include.
      if (info == null)
        returnList.add(loader);
      else if (info.getGenomeVersionId() == genomeVersionId)
        returnList.add(loader);
    } // For all known URL sources.

    return returnList.iterator();

  } // End method

  /**
   * Returns NO sources representing axes.  At time of writing, no
   * sources registered with this type of space may represent a genomic axis.
   */
  public Iterator getDataSourcesRepresentingGenomicAxes(int genomeVersionId) {
    return EMPTY_ITERATOR;
  } // End method

  /** Here the open sources are ALL sources. */
  public Iterator getOpenSources() {
    return loaderMap.keySet().iterator();
  } // End method

} // End class
