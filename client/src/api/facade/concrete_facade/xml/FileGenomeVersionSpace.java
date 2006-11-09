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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Title:        Space in which genome version from single input file resides.
 * Description:  Confines for a genome version that comes from a single input
 *               file.
 * @author Les Foster
 * @version $Id$
 */
public class FileGenomeVersionSpace implements GenomeVersionSpace {

  //--------------------------------CONSTANTS
  private static final Iterator EMPTY_ITERATOR = new ArrayList().iterator();

  //--------------------------------MEMBER VARIABLES
  private List loaderList = new ArrayList();

  //--------------------------------CONSTRUCTORS
  /** simplest constructor. */
  public FileGenomeVersionSpace() {
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



  public void removeLoader(XmlLoader loader){
      if (loaderList!=null && loaderList.size()!=0 && loaderList.contains(loader)){
          loaderList.remove(loader);
      }
  }


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

  /** Returns true if anything has been loaded. */
  public boolean hasLoaders() {
    return loaderList.size() > 0;
  } // End method

  /** @Todo fill in these blanks. */
  public void registerSpecies(String filename, Species species) {
  } // End method

  /**
   * This space will have one loader for its one file, and it will return
   * that loader if the correct OID for it is requested.
   */
  public XmlLoader getLoaderForSpecies(OID speciesOID) {
    GenomeVersionInfo info = getGenomeVersionInfo();

    if (speciesOID.getGenomeVersionId() == info.getGenomeVersionId())
      return (XmlLoader)loaderList.get(0);
    else
      return null;
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

  /** Returns sources in use. */
  public Iterator getOpenSources() {
    return loaderList.iterator();
  } // End method

  /**
   * Tells if it is possible, with hopefully minimal time wastage, for the
   * genome version space to have ANY sources worth searching in the genome version.
   * Used to optimize searches against database, by limiting flatfile overhead.
   */
  public boolean hasSearchableInputSourcesInGenomeVersion(int genomeVersionId) {
    return getSearchableInputSources(genomeVersionId).hasNext();
  } // End method

  /** Returns all sources which may be searched that lie in the genome version. */
  public Iterator getSearchableInputSources(int genomeVersionID) {
    return getGenomicAxisInputSources(genomeVersionID);
  } // End method

  /** Returns all sources for features aligning to an axis in the gv given. */
  public Iterator getGenomicAxisInputSources(int genomeVersionId) {
    if (loaderList.size() == 0)
      return new ArrayList().iterator();

    //@todo: reconsider the amount of processing required to learn if the
    // genome version is relevant.

    // Expect one and only one loader in the list!
    XmlLoader loader = (XmlLoader)loaderList.get(0);
    if (loader == null)
      return new ArrayList().iterator();

    Set oidSet = loader.getReferencedOIDSet();
    if (oidSet != null) {
      OID nextOid = null;
      for (Iterator it = oidSet.iterator(); it.hasNext(); ) {
        nextOid = (OID)it.next();
        if (nextOid!=null) {
          if (nextOid.getGenomeVersionId() == genomeVersionId)
            return loaderList.iterator();
        }
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

  /** Returns the list of loaders, for subclass convenience */
  protected List getLoaderList() {
    return loaderList;
  } // End method

  /** Find vers. info. for the loaded file. */
  private GenomeVersionInfo getGenomeVersionInfo() {
    String fileName = getWorkspaceFileName();
    if (fileName == null)
      return null;
    GenomeVersionParser parser = new GenomeVersionParser(this, fileName);
    return parser.parseForGenomeVersionInfo(fileName);
  } // End method

} // End class
