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

import api.entity_model.management.ModelMgr;
import api.entity_model.model.genetics.GenomeVersion;
import api.entity_model.model.genetics.Species;
import api.stub.data.GenomeVersionInfo;
import api.stub.data.OID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Title:        MultiFileGenomeVersionSpace
 * Description:  Multiple feature-file source for genome version data.
 * @author Les Foster
 * @version $Id$
 */
public class MultiFileGenomeVersionSpace implements GenomeVersionSpace {

  //--------------------------------CONSTANTS
  Iterator EMPTY_ITERATOR = java.util.Collections.EMPTY_LIST.iterator();

  //--------------------------------MEMBER VARIABLES
  private Map mGenomeVersionNumberVsFeatureSource = null;
  private boolean mFileWasAdded = false;
  private Set mLoaderFileSet = null;

  //--------------------------------CONSTRUCTORS
  /** simplest constructor. */
  public MultiFileGenomeVersionSpace() {
    super();
  } // End constructor

  //--------------------------------PUBLIC INTERFACE
  /** Here, a file to be parsed against various genome versions may be registered. */
  public void addFeatureSource(String lFeatureSourceFileName) {

    // Update the loader file name list.
    if (mLoaderFileSet == null)
      mLoaderFileSet = new HashSet();
    if (! mLoaderFileSet.contains(lFeatureSourceFileName)) {
      mLoaderFileSet.add(lFeatureSourceFileName);
      // Update previously-read genome version IDs with new file name.
      updateGenomeVersionNumberVsFeatureSource(lFeatureSourceFileName);
    } // Need to add this one.

  } // End method

  //--------------------------------IMPLEMENTS GenomeVersionSpace
  /** Returns true if anything has been loaded. */
  public boolean hasLoaders() {
    return (mLoaderFileSet != null);
  } // End method

  /** Returns all sources for features aligning to an axis in the gv given. */
  public Iterator getGenomicAxisInputSources(int lGenomeVersionId) {

    if (! this.hasLoaders())
      return EMPTY_ITERATOR;

    // Examine list of file names for inclusion in genome versions sources.
    updateGenomeVersionNumberVsFeatureSource(lGenomeVersionId);

    Set lReturnSet = null;
    Set lGenomeVersionsLoaders = (Set)mGenomeVersionNumberVsFeatureSource.get(new Integer(lGenomeVersionId));
    if (lGenomeVersionsLoaders != null)
      lReturnSet = lGenomeVersionsLoaders;
    else
      lReturnSet = java.util.Collections.EMPTY_SET;
    return lReturnSet.iterator();

  } // End method

  /**
   * Tells if it is possible, with hopefully minimal time wastage, for the
   * genome version space to have ANY sources worth searching in the genome version.
   * Used to optimize searches against database, by limiting flatfile overhead.
   */
  public boolean hasSearchableInputSourcesInGenomeVersion(int lGenomeVersionId) {
    return getGenomicAxisInputSources(lGenomeVersionId).hasNext();
  } // End method

  /** Returns all sources which may be searched that lie in the genome version. */
  public Iterator getSearchableInputSources(int lGenomeVersionID) {
    return getGenomicAxisInputSources(lGenomeVersionID);
  } // End method

  /** Returns data sources in this space, which have served data. */
  public Iterator getOpenSources() {
    List lReturnList = new ArrayList();

    // Get the feature sources.
    if (mGenomeVersionNumberVsFeatureSource != null) {
      for (Iterator it = mGenomeVersionNumberVsFeatureSource.values().iterator(); it.hasNext(); ) {
        lReturnList.addAll((Set)it.next());
      } // Collecting all the feature sources.
    } // Something in the map.

    return lReturnList.iterator();
  } // End method

  // ---- This set of methods is all "stubbed" from the interface.

  /** Space not a source for independent genome versions. */
  public GenomeVersion[] getGenomeVersions() {
    return new GenomeVersion[0];
  } // End method

  /** Space not a source for species. */
  public String getSourceOf(OID lSpeciesOID) {
    return null;
  } // End method

  public Species getSpeciesOf(OID lSpeciesOID) {
    return null;
  } // End method

  public void registerSpecies(String lFilename, Species lSpecies) {
  } // End method

  /** Space is not a source for chromosomes. */
  public XmlLoader getLoaderForSpecies(OID lSpeciesOID) {
    return null;
  } // End method

  /** Space not a source for genomic axis definitions. */
  public Iterator getDataSourcesRepresentingGenomicAxes(int lGenomeVersionID) {
    return EMPTY_ITERATOR;
  } // End method

  /** Space not a source for genomic axes' meta data. */
  public Iterator getInputSourcesForAxisOID(OID lAxisOid) {
    return EMPTY_ITERATOR;
  } // End method

  //----------------------------------------------HELPER METHODS
  /** Synchronizes the new file with any existing genome versions. */
  private synchronized void updateGenomeVersionNumberVsFeatureSource(String lFeatureSourceFileName) {
    // If collection not instantiated or empty, there is nothing to be updated.
    if ((mGenomeVersionNumberVsFeatureSource != null) && (mGenomeVersionNumberVsFeatureSource.size() > 0)) {
      GenomeVersionInfo[] lFileGenomeVersionInfos = getGenomeVersionInfosOf(new String[] { lFeatureSourceFileName });
      GenomeVersionInfo lFileGenomeVersionInfo = lFileGenomeVersionInfos[0];
      Integer lNextKey = null;
      GenomeVersion lModelGenomeVersion = null;
      GenomeVersionInfo lModelGenomeVersionInfo = null;
      Set lGenomeVersionsSet = null;
      FeatureXmlLoader lNewLoader = null;

      // Look at all genome version IDs in the map.  Determine whether the
      // new file applies to any of them.
      for (Iterator it = mGenomeVersionNumberVsFeatureSource.keySet().iterator(); it.hasNext(); ) {
        // Find next key's GV data.
        lNextKey = (Integer)it.next();
        lModelGenomeVersion = ModelMgr.getModelMgr().getGenomeVersionById(lNextKey.intValue());
        if (lModelGenomeVersion != null) {
          lModelGenomeVersionInfo = lModelGenomeVersion.getGenomeVersionInfo();
          if (lModelGenomeVersionInfo != null) {
            if (compatibleWithGenomeVersion(lModelGenomeVersionInfo, lFileGenomeVersionInfo)) {
              // Make a loader:
              lNewLoader = new FeatureXmlLoader(lNextKey.intValue());
              lNewLoader.loadXml(lFeatureSourceFileName);

              // Add loader to the set.
              lGenomeVersionsSet = (Set)mGenomeVersionNumberVsFeatureSource.get(lNextKey);
              lGenomeVersionsSet.add(lNewLoader);
            } // Needs to be added
          } // Got the GVInfo
        } // Got the genome version
      } // For all keys.
    } // If collection was null, no synchronizing to do.
  } // End method

  /** Syncronizes a genome version info against files, if gv ID not queried before. */
  private synchronized void updateGenomeVersionNumberVsFeatureSource(int lGenomeVersionId) {

    // First check: if no input files, then no need to update anything.
    // Subsequent requests will call this again, and if new files have been
    // added since....
    if (mLoaderFileSet == null  ||  mLoaderFileSet.size() == 0)
      return;

    // Tests: this is where the registry is created-on-demand.
    if (mGenomeVersionNumberVsFeatureSource == null)
      mGenomeVersionNumberVsFeatureSource = new HashMap();

    // The registry entry for a genome version ID is created on demand.
    Set lLoaderSet = (Set)mGenomeVersionNumberVsFeatureSource.get(new Integer(lGenomeVersionId));
    if (lLoaderSet == null) {
      lLoaderSet = new HashSet();
      mGenomeVersionNumberVsFeatureSource.put(new Integer(lGenomeVersionId), lLoaderSet);

      // Now need to populate the set.
      String[] lFileArray = new String[mLoaderFileSet.size()];
      mLoaderFileSet.toArray(lFileArray);

      GenomeVersionInfo[] lFileInfos = getGenomeVersionInfosOf(lFileArray);
      GenomeVersionInfo lModelInfo = ModelMgr.getModelMgr().getGenomeVersionById(lGenomeVersionId).getGenomeVersionInfo();
      FeatureXmlLoader lNewLoader = null;
      for (int i = 0; i < lFileInfos.length; i++) {
        if (compatibleWithGenomeVersion(lModelInfo, lFileInfos[i])) {
          lNewLoader = new FeatureXmlLoader(lGenomeVersionId);
          lNewLoader.loadXml(lFileArray[i]);
          lLoaderSet.add(lNewLoader);
        } // Need to add a loader.
      } // For all GVSIs.

    } // Need to add this GVId.

  } // End method

  /**
   * Tests whether the file GV info indicates that the model's GV info should be
   * sourced from the file.
   */
  private boolean compatibleWithGenomeVersion(GenomeVersionInfo lModelInfo, GenomeVersionInfo lFileInfo) {

    boolean lReturnValue = false;
    if (lModelInfo != null && lFileInfo != null) {
      if (lModelInfo.getAssemblyVersion() == lFileInfo.getAssemblyVersion()) {
        if (lModelInfo.getSpeciesName().equals(lFileInfo.getSpeciesName()))
          lReturnValue = true;
      } // Same assy ver
    } // Non-null

    return lReturnValue;

  } // End method

  /** Given a set of files found in the directory, find vers. info for each such file. */
  private GenomeVersionInfo[] getGenomeVersionInfosOf(String[] files) {
    // Look at each file.
    GenomeVersionParser parser = new GenomeVersionParser(this, "UTTER NONSENSE--SHOULD REMOVE");
    List infoList = new ArrayList();

    GenomeVersionInfo nextInfo = null;

    for (int i = 0; i < files.length; i++) {
      // Run a SAX parse that will bail as soon as it has its required info.
      //
      nextInfo = parser.parseForGenomeVersionInfo(files[i]);

      infoList.add(nextInfo);
    } // For all files in directory

    GenomeVersionInfo[] returnArray = new GenomeVersionInfo[infoList.size()];
    infoList.toArray(returnArray);

    return returnArray;
  } // End method: getGenomeVersionInfosOf

} // End class
