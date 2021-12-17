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

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Les Foster
 * @version $Id$
 */

import api.entity_model.management.ModelMgr;
import api.entity_model.model.genetics.GenomeVersion;
import api.entity_model.model.genetics.Species;
import api.facade.concrete_facade.shared.GenomeVersionFactory;
import api.facade.concrete_facade.shared.LoaderConstants;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.GenomeVersionInfo;
import api.stub.data.OID;
import shared.util.FileUtilities;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

/**
 * Create this with a directory path, and then use it to find all the
 * genome versions from files in that directory.
 */
public class DirectoryGenomeVersionSpace implements GenomeVersionSpace, SequenceAlignmentListener {

    //---------------------------------MEMBER VARIABLES
    private Map oidToSpecies = new HashMap();
    private Map speciesToLoader = new HashMap();
    private Map genomeVersionNumberVsFeatureSource = null;
    private Map liveAxisLoaders = new HashMap();

    private GenomeVersion[] allGenomeVersionsOriginatingHere = null;
    private Set directoryNames = null;
    private List recentlyOpenedGenomeVersions = null;
    private String datasourceName = null;
    private XmlFacadeManager xmlFacadeManager = null;
    private boolean speciesHasBeenQueried = false;
    private RecentGenomeVersionManager recentGenomeVersionManager = null;

    //---------------------------------CONSTRUCTORS
    /** A genome version space implementation that reads from a directory. */
    public DirectoryGenomeVersionSpace(Set directoryNames, RecentGenomeVersionManager recentGVMgr) {
        this.directoryNames = directoryNames;
        this.recentGenomeVersionManager = recentGVMgr;
        try {
            recentlyOpenedGenomeVersions = recentGenomeVersionManager.readMostRecentlyUsedGBAs();
        } // End try block to read GBAs recently used.
        catch (Exception ex) {
            // There will be no recently-used gbas added (sigh!)
            FacadeManager.handleException(ex);
        } // End catch block.

        List recentFiles = getFileNamesForRecentGenomeVersions(recentlyOpenedGenomeVersions.iterator());
        ValidationManager.getInstance().validateAll(directoryNames.iterator(), recentFiles.iterator(), createExtensionSet());

        StringBuffer datasources = new StringBuffer();
        String nextDirectoryName = null;
        for (Iterator it = this.directoryNames.iterator(); it.hasNext(); ) {
            nextDirectoryName = (String)it.next();
            datasources.append(nextDirectoryName);
            datasources.append(" ");
        } // For all directories.
        this.datasourceName = datasources.toString();

        // System.out.println("Got directories " + this.datasourceName);

    } // End constructor

    //---------------------------------IMPLEMENTATION OF GenomeVersionSpace
    /** Returns all genome versions found in the directory given. */
    public GenomeVersion[] getGenomeVersions() {
        if (allGenomeVersionsOriginatingHere != null)
            return allGenomeVersionsOriginatingHere;

        List accumulator = new ArrayList();

        // Handle "recently used" genome versions, which may or may not be in current
        // selected directories.
        accumulator.addAll(getGenomeVersions(recentlyOpenedGenomeVersions));

        // Handle genome versions defined by files in current selected
        // directories.
        String nextDirectory = null;
        for (Iterator it = directoryNames.iterator(); it.hasNext(); ) {
            nextDirectory = (String)it.next();
            accumulator.addAll(getGenomeVersions(nextDirectory));
        } // For all directories.

        GenomeVersion[] returnArray = new GenomeVersion[accumulator.size()];
        accumulator.toArray(returnArray);

        allGenomeVersionsOriginatingHere = returnArray;
        return returnArray;

    } // End method: getGenomeVersions

    /** Returns true if anything has been loaded. */
    public boolean hasLoaders() {
        return speciesToLoader.size() > 0;
    } // End method

    /**
     * Registers the species for later retrieval.
     * Expect this to be called as a result of parsing .gba files
     * in the directory for their genome versions.
     */
    public void registerSpecies(String fileName, Species species) {

        // NOTE: need to call up and notify species listeners here.
        OID speciesOID = species.getOid();

        oidToSpecies.put(speciesOID, species);

        if (fileName != null) {
            // Must create a loader and cache it in a map.
            GenomicAxisXmlLoader loader = new GenomicAxisXmlLoader(speciesOID);

            // Make this loader a listener for events.
            loader.addSequenceAlignmentListener(this);
            loader.loadXml(fileName);
            speciesToLoader.put(speciesOID, loader);

        } // Source found

    } // End method

    /** Returns a loader for a species OID. */
    public XmlLoader getLoaderForSpecies(OID speciesOID) {
        // Look for a cached loader.
        speciesHasBeenQueried = true;

        // Returning a loader for a species implies that we need
        // to keep that loader on an "in-use" list for the genome version
        // whose species OID was given.
        Integer key = new Integer(speciesOID.getGenomeVersionId());
        XmlLoader loader = (XmlLoader)speciesToLoader.get(speciesOID);

        // Avoid adding a null to the live axis loaders list.
        if (loader == null)
            return null;

        List loadersInGenomeVersion = null;
        if (null == (loadersInGenomeVersion = (List)liveAxisLoaders.get(key) )) {
            loadersInGenomeVersion = new ArrayList();
            liveAxisLoaders.put(key, loadersInGenomeVersion);
        } // Have existing list.

        // Add this loader to the live list for the species, if it is not
        // already in.
        if (! loadersInGenomeVersion.contains(loader)) {
            loadersInGenomeVersion.add(loader);
            try {
                recentGenomeVersionManager.updateMostRecentlyUsedGBAs(loader.getLoadedFileNames()[0]);
            } // End try block to read GBAs recently used.
            catch (Exception ex) {
                // Recently-used not updated.
                FacadeManager.handleException(ex);
            } // End catch block.
        } // Adding one

        return loader;

    } // End method: getLoaderForSpeciesOID

    /** Given an OID, returns its species. */
    public Species getSpeciesOf(OID speciesOID) {
        return (Species)oidToSpecies.get(speciesOID);
    } // End method: getSpeciesOf

    /** Supplies all genomic axis source with data on OID. */
    public Iterator getInputSourcesForAxisOID(OID axisOID) {
        List returnList = new ArrayList();
        GenomicAxisXmlLoader loader = null;
        for (Iterator it = speciesToLoader.values().iterator(); it.hasNext(); ) {
            loader = (GenomicAxisXmlLoader)it.next();
            if ((null != loader) && (loader.getGenomeVersionId() == axisOID.getGenomeVersionId()) &&
                (loader.getGenomicAxisOID() != null) && loader.getGenomicAxisOID().equals(axisOID)) {
                returnList.add(loader);
            } // Got genax id.
        } // For all loaders.

        return returnList.iterator();

    } // End method

    /**
     * Tells if searching this genome version space is worth the time spent.
     */
    public boolean hasSearchableInputSourcesInGenomeVersion(int genomeVersionId) {
        // First, see if the GV ID originated here.
        // No need to dynamically check all GVs, because if it did not already
        // become 'live', its ID cannot have been used to make this query.
        GenomeVersion[] sourcedGenomeVersions = getGenomeVersions();
        if ((sourcedGenomeVersions != null) && (sourcedGenomeVersions.length > 0)) {
            for (int i = 0; i < sourcedGenomeVersions.length; i++) {
                if (sourcedGenomeVersions[i].getOid().getGenomeVersionId() == genomeVersionId)
                    return true;
            } // For all sourced GVs.
        } // Something was sourced here.

        // Next, see if it is represented by any feature files here.
        // Find the list corresponding to the genome version ID, or an empty iterator.
        List tempList = featureSourcesInGenomeVersion(genomeVersionId, true);
        if (tempList.size() > 0)
            return true;

        return false;

    } // End method

    /**
     * Supplies all input sources in which a searchable quantity may reside.
     * This may include unopened genomic axis / genome version sources.
     */
    public Iterator getSearchableInputSources(int genomeVersionId) {
        // Make collection into which to store returnables.
        List returnList = new ArrayList();

        // NOTE: only include the axis (gba) files, or axis loaders, if
        // they are where the genome version was calculated from.

        // Registry of species against various things, including the
        // loader, will be carried out by the time "getGenomeVersions"
        // returns.
        GenomeVersion[] genomeVersions = getGenomeVersions();
        int loaderGenomeVersionId = 0;
        GenomicAxisXmlLoader axisLoader = null;
        for (int i = 0; i < genomeVersions.length; i++) {
            loaderGenomeVersionId = genomeVersions[i].getGenomeVersionInfo().getGenomeVersionId();
            if (loaderGenomeVersionId == genomeVersionId) {
                OID speciesOid = genomeVersions[i].getSpecies().getOid();
                axisLoader = (GenomicAxisXmlLoader)speciesToLoader.get(speciesOid);
                axisLoader.scanForSequenceAlignments();
                returnList.add(axisLoader);
                break;
            } // Found one.
        } // For all gvs found in input directory.

        // Find the list corresponding to the genome version ID.
        returnList.addAll(featureSourcesInGenomeVersion(genomeVersionId, true));

        return returnList.iterator();
    } // End method

    /** Supplies all input sources known to this space, with axis refs, and in the GV whose ID is given. */
    public Iterator getGenomicAxisInputSources(int genomeVersionID) {
        // Make collection into which to store returnables.
        List returnList = new ArrayList();

        // Make sure the mapping is made for the genome version ID in
        // question, vs its list of applicable sources.
        returnList.addAll(featureSourcesInGenomeVersion(genomeVersionID, false));

        // NOTE: only include the axis (gba) files, or axis loaders, if
        // they were the source for the genome version.
        List loaderList = null;
        if (thisSpaceIsGVOrigin()) {
            if (liveAxisLoaders.size() > 0) {
                if (null != (loaderList = (List)liveAxisLoaders.get(new Integer(genomeVersionID)))) {
                    returnList.addAll(loaderList);
                } // Got live loaders.
            } // There are live axis loaders.
            else {
                returnList.addAll(speciesLoadersInGenomeVersion(genomeVersionID));
            } // No live axis loaders.
        } // Origin in space.
        return returnList.iterator();
    } // End method

    /**
     * Returns all data sources within the genome version whose ID is given,
     * and which represent a genomic axis.
     */
    public Iterator getDataSourcesRepresentingGenomicAxes(int genomeVersionID) {
        List returnList = new ArrayList();
        if (thisSpaceIsGVOrigin()) {
            List liveList = (List)liveAxisLoaders.get(new Integer(genomeVersionID));
            if (liveList != null)
              returnList.addAll(liveList);
        } // Must source data for axes.

        return returnList.iterator();
    } // End method

    /** Returns data sources in this space, which have served data. */
    public Iterator getOpenSources() {
        List returnList = new ArrayList();

        // Get the feature sources.
        if (genomeVersionNumberVsFeatureSource != null) {
            for (Iterator it = genomeVersionNumberVsFeatureSource.values().iterator(); it.hasNext(); ) {
                returnList.addAll((List)it.next());
            } // Collecting all the feature sources.
        } // Something in the map.

        // Get the axis sources.
        returnList.addAll(speciesToLoader.values());

        return returnList.iterator();
    } // End method

    //----------------------------IMPLEMENTATION OF SequenceAlignmentListener
    /** Called when alignment found.  */
    public void foundSequenceAlignment(SequenceAlignment sequenceAlignment, int genomeVersionId) {
        // Alternative entry point for file loading.

        // Create a loader equiped to handle sequence alignment-originating files.
        SequenceAlignmentLoader loader = new SequenceAlignmentLoader(sequenceAlignment, genomeVersionId);

        // Carry out any user-requested validations.
        ValidationManager.getInstance().validateAndReportInputFile(sequenceAlignment.getFilePath());

        // Add the loader, and make it available to respond to various requests.
        // Make sure that there is somewhere to store the gv/source mapping.
        if (! featureSourcesRegisteredForGenomeVersion(genomeVersionId))
            buildGenomeVersionNumberVsFeatureSource(genomeVersionId);
        addToGenomeVersionNumberVsFeatureSource(genomeVersionId, (XmlLoader)loader);

    } // End method

    /** Called when it is permissible for this listener to stop listening to a given source. */
    public void noMoreAlignments(Object source) {
        // Stop listening.
        GenomicAxisXmlLoader nextLoader = null;
        for (Iterator it = speciesToLoader.values().iterator(); it.hasNext(); ) {
            nextLoader = (GenomicAxisXmlLoader)it.next();
            if (nextLoader == source)
              nextLoader.removeSequenceAlignmentListener(this);
        } // For all axis/species loaders.
    } // End method: noMoreAlignments

    //----------------------------------------------CLASS-SPECIFIC INSTANCE METHODS
    /** Adds a filename originating ANYWHERE. */
    public void addFeatureSource(String featureSource) {
        System.out.println("Setting file for "+featureSource);
    } // End method

    //----------------------------------------------HELPER METHODS
    /** Creates list of feature file names that can apply to GVs recently opened. */
    private List getFileNamesForRecentGenomeVersions(Iterator baseGenomeVersionFiles) {
        List returnList = new ArrayList();
        String nextPath = null;
        GenomeVersionFactory parser = new GenomeVersionParser(this, datasourceName);
        GenomeVersionInfo nextAxisVersionInfo = null;
        File nextFile = null;
        String nextDirectory = null;
        File nextDirectoryFile = null;
        String[] nextDirectoryListing;
        GenomeVersionInfo nextFeatureGVInfo = null;
        String nextParseablePath = null;
        while (baseGenomeVersionFiles.hasNext()) {
            // Get next file on list: remember that with the dynamic nature
            // of mounted disk drives, etc., it is possible the a recently
            // used file will now be inaccessible.
            nextDirectoryListing = null;
            nextPath = (String)baseGenomeVersionFiles.next();
            nextFile = new File(nextPath);
            if (nextFile.exists() && nextFile.canRead()) {
                returnList.add(nextPath);
                nextAxisVersionInfo = parser.getGenomeVersionInfos(nextPath).get( 0 );
                if (nextFile != null) {
                    nextDirectory = nextFile.getParent();
                    if (nextDirectory != null) {
                        nextDirectoryFile = FileUtilities.openDirectoryFile(nextDirectory);
                        nextDirectoryListing = nextDirectoryFile.list(new FilenameFilter() {
                            public boolean accept(File parentFile, String justName) {
                                String fullPath = new File(parentFile, justName).getAbsolutePath();
                                if (fullPath != null  &&  fullPath.endsWith(LoaderConstants.XML_FEATURE_FILE_EXTENSION))
                                    return true;
                                else
                                    return false;
                            } // End accept method
                        });
                    } // Got directory

                    // Parse all feature files for GVIds.  Did they match?
                    if (nextDirectoryListing != null) {
                        for (int i = 0; i < nextDirectoryListing.length; i++) {
                            nextParseablePath = new File(nextFile.getParent(), nextDirectoryListing[i]).getAbsolutePath();
                            nextFeatureGVInfo = parser.getGenomeVersionInfos(nextParseablePath).get( 0 );
                            if ((nextFeatureGVInfo.getAssemblyVersion() == nextAxisVersionInfo.getAssemblyVersion()) &&
                                (nextFeatureGVInfo.getSpeciesName().equals(nextAxisVersionInfo.getSpeciesName()))) {
                                returnList.add(nextParseablePath);
                                // System.out.println("..."+nextParseablePath);
                            } // Adding to return list.
                        } // For all in directory
                    } // Got dir list

                } // Found a file.

            } // Can read the path.

        } // For all GV files.

        return returnList;
    } // End method

    /** Returns all genome versions found in the directory given. */
    private Collection getGenomeVersions(String directoryName) {
        List returnList = new ArrayList();

        try {
            // Check for nonexistent directory name.  Means nothing to
            // return.
            if (directoryName == null)
                return returnList;

            // Open the directory.
            File directoryFile = FileUtilities.openDirectoryFile(directoryName);

            // Get list of appropriately-named files.
            String[] filesInDirectory = directoryFile.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    if (name.endsWith(LoaderConstants.XML_ASSEMBLY_FILE_EXTENSION)) {
                        if (XmlPermission.getXmlPermission().canReadFile(new File(dir.getAbsolutePath(), name)))
                            return true;
                        else
                            return false;
                    } // Proper extension.
                    else
                        return false;
                } // End method: accept
            });

            // Now have an array of "okay" filenames.
            returnList.addAll(getGenomeVersionsOf(filesInDirectory, directoryName));

        } // End try block.
        catch (Exception ex) {
            FacadeManager.handleException(ex);
        } // End catch block.

        return returnList;

    } // End method: getGenomeVersions

    /** Finds genome versions of all files in input collection. */
    private Collection getGenomeVersions(Collection pathsToRead) {
        List returnList = new ArrayList();
        String nextPath = null;
        File nextFile = null;
        String nextLocation = null;
        GenomeVersionFactory parser = new GenomeVersionParser(this, datasourceName);
        GenomeVersion nextVersion = null;

        for (Iterator it = pathsToRead.iterator(); it.hasNext(); ) {
            // Check path: is it in a currently-registered directory?
            nextPath = (String)it.next();
            nextFile = new File(nextPath);
            nextLocation = nextFile.getParent();
            if ((directoryNames == null) || (! directoryNames.contains(nextLocation))) {
               nextVersion = parser.getGenomeVersions(nextPath).get( 0 );
                if (! isGenomeVersionPreviouslyRegistered(nextVersion, returnList))
                   returnList.add(nextVersion);

            } // Must look at path here.

        } // For all paths

        return (Collection)returnList;
    } // End method: getGenomeVersions

    /** Test method to decide whether to populate a map. */
    private boolean featureSourcesRegisteredForGenomeVersion(int genomeVersionId) {
        if (genomeVersionNumberVsFeatureSource == null)
            return false;
        if (genomeVersionNumberVsFeatureSource.get(new Integer(genomeVersionId)) == null)
            return false;
        return true;
    } // End metho

    /**
     * Returns all loader sources that could contain features that align
     * to axes that are in the genome version whose id is given.
     */
    private List featureSourcesInGenomeVersion(int genomeVersionId, boolean updateFeatureListForGenomeVersion) {
        if (! featureSourcesRegisteredForGenomeVersion(genomeVersionId))
            buildGenomeVersionNumberVsFeatureSource(genomeVersionId);
        else if (updateFeatureListForGenomeVersion)
            updateGenomeVersionNumberVsFeatureSource(genomeVersionId);

        if (genomeVersionNumberVsFeatureSource == null)
            return Collections.EMPTY_LIST;

        List returnList = (List)genomeVersionNumberVsFeatureSource.get(new Integer(genomeVersionId));
        if (returnList == null)
            return java.util.Collections.EMPTY_LIST;
        else
            return returnList;
    } // End method

    /** Builds unique collection of file extension of value here. */
    private Set createExtensionSet() {
        Set extensionSet = new HashSet();
        extensionSet.add(LoaderConstants.XML_ASSEMBLY_FILE_EXTENSION);
        extensionSet.add(LoaderConstants.XML_FEATURE_FILE_EXTENSION);
        return extensionSet;
    } // End method

    /** Can call this to find out if this GenomeVersionSpace has sourced a genome version so far in this session. */
    private boolean thisSpaceIsGVOrigin() {
        boolean foundSelectedVersionInThisSpace = false;

        // If species has not been queried, then we should not source anything.
        if (speciesHasBeenQueried)
            foundSelectedVersionInThisSpace = true;
        else if (genomeVersionNumberVsFeatureSource != null) {
            Set selectedGenomeVersions = ModelMgr.getModelMgr().getSelectedGenomeVersions();

            GenomeVersion version = null;
            int selectedVersionId = 0;
            GenomeVersion[] allGenomeVersionsOriginatingInSpace = getGenomeVersions();
            for (Iterator it = selectedGenomeVersions.iterator(); (! foundSelectedVersionInThisSpace) && it.hasNext(); ) {
                version = (GenomeVersion)it.next();
                selectedVersionId = version.getID();

                // Now check if selected.
                for (int i = 0; i < allGenomeVersionsOriginatingInSpace.length; i++) {
                    if (allGenomeVersionsOriginatingInSpace[i].getID() == selectedVersionId)
                        foundSelectedVersionInThisSpace = true;
                } // For all known genome versions.
            } // For all selected gvs.
        } // Must check versions.

        return foundSelectedVersionInThisSpace;

    } // End method

    /** Given a set of files found in the directory, produce genome versions for each such file. */
    private Collection getGenomeVersionsOf(String[] files, String directoryName) {
        // Look at each file.
        GenomeVersionFactory parser = new GenomeVersionParser(this, datasourceName);
        List versionList = new ArrayList();

        File directoryFile = FileUtilities.openDirectoryFile(directoryName);
        String nextFile = null;
        GenomeVersion nextVersion = null;
        File tmpFile;

        for (int i = 0; i < files.length; i++) {
            // Run a SAX parse that will bail as soon as it has its required info.
            //
            //System.out.println("Parsing file "+files[i]+" for genome version");
            tmpFile= new File(directoryFile, files[i]);
            nextFile = tmpFile.getAbsolutePath();
            nextVersion = parser.getGenomeVersions(nextFile).get( 0 );

            if (! isGenomeVersionPreviouslyRegistered(nextVersion, versionList)) {
                versionList.add(nextVersion);
            } // Only register once.

        } // For all files in directory

        return versionList;
    } // End method: getGenomeVersionsOf

    /** Test: was this genome version already encountered? */
    boolean isGenomeVersionPreviouslyRegistered(GenomeVersion nextVersion, List versionList) {
        boolean previouslyRegisteredGenomeVersion = false;
        int nextId = 0;
        int nextRegisteredId = 0;
        GenomeVersion nextRegisteredVersion = null;
        for (Iterator it = versionList.iterator(); it.hasNext();  ) {
            nextId = nextVersion.getGenomeVersionInfo().getGenomeVersionId();
            nextRegisteredVersion = (GenomeVersion)it.next();
            nextRegisteredId = nextRegisteredVersion.getGenomeVersionInfo().getGenomeVersionId();
            if (nextId == nextRegisteredId) {
                FacadeManager.handleException(  new IllegalArgumentException("Genome Version "+nextVersion.getGenomeVersionInfo().getAssemblyVersion()+":"+nextVersion.getGenomeVersionInfo().getSpeciesName()+":"+nextVersion.getGenomeVersionInfo().getDataSource()+" already registered"));
                previouslyRegisteredGenomeVersion = true;
            } // GV ID already there.
        } // For all species in map.
        return previouslyRegisteredGenomeVersion;
    } // End method: isGenomeVersionPreviouslyRegistered

    /** Given a set of files found in the directory, find vers. info for each such file. */
    private GenomeVersionInfo[] getGenomeVersionInfosOf(String[] files, String directoryName) {
        // Look at each file.
        GenomeVersionFactory parser = new GenomeVersionParser(this, datasourceName);
        List infoList = new ArrayList();

        // Should not happen once we get to here, but just as a precaution...
        if (directoryName == null)
            return new GenomeVersionInfo[0];

        File directoryFile = FileUtilities.openDirectoryFile(directoryName);
        String nextFile = null;
        GenomeVersionInfo nextInfo = null;

        for (int i = 0; i < files.length; i++) {
            // Run a SAX parse that will bail as soon as it has its required info.
            //
            //System.out.println("Parsing file "+files[i]+" for genome version");
            nextFile = new File(directoryFile, files[i]).getAbsolutePath();
            nextInfo = parser.getGenomeVersionInfos(nextFile).get( 0 );

            //System.out.println("next version is "+nextVersion);
            infoList.add(nextInfo);
        } // For all files in directory

        GenomeVersionInfo[] returnArray = new GenomeVersionInfo[infoList.size()];
        infoList.toArray(returnArray);
        return returnArray;
    } // End method: getGenomeVersionInfosOf

    /** Return list of all species loaders matching the genome version */
    private List speciesLoadersInGenomeVersion(int genomeVersionID) {
        List returnList = new ArrayList();
        if (speciesToLoader == null)
            return returnList;

        OID speciesOID = null;
        for (Iterator it = speciesToLoader.keySet().iterator(); it.hasNext(); ) {
            //
            speciesOID = (OID)it.next();
            if (speciesOID.getGenomeVersionId() == genomeVersionID)
                returnList.add(speciesToLoader.get(speciesOID));
        } // For all species loaders.

        return returnList;
    } // End method

    /**
     * Builds a mapping of genome version id versus list of source feature
     * input sources.  Provides a filter for checking ALL files with
     * the proper extension.
     */
    private void buildGenomeVersionNumberVsFeatureSource(int genomeVersionId) {
        buildGenomeVersionNumberVsFeatureSource(genomeVersionId, new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.endsWith(LoaderConstants.XML_FEATURE_FILE_EXTENSION)) {
                    return true;
                } // Got extension.
                else
                    return false;
            } // End method: accept
        });
    } // End method

    /**
     * Updates the mapping of genome version id versus list of source feature
     * input sources.  Provides a filter for checking files that are not
     * already known, but have the right extension.
     */
    private void updateGenomeVersionNumberVsFeatureSource(int genomeVersionId) {

        final int finalGenomeVersionId = genomeVersionId;
        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                boolean returnValue = false;
                if (name.endsWith(LoaderConstants.XML_FEATURE_FILE_EXTENSION)) {
                    if (genomeVersionNumberVsFeatureSource == null)
                        returnValue = true;
                    else if (genomeVersionNumberVsFeatureSource.size() == 0)
                        returnValue = true;
                    else {
                        List mappedToThisGV = (List)genomeVersionNumberVsFeatureSource.get(new Integer(finalGenomeVersionId));
                        if (mappedToThisGV == null)
                            returnValue = true;
                        else {
                            // Must see if the file has been loaded previously.
                            String testFileFullName = dir.getAbsolutePath() +
                                System.getProperty("file.separator") + name;

                            XmlLoader nextLoader = null;
                            for (Iterator it = mappedToThisGV.iterator(); (returnValue == false) && it.hasNext(); ) {
                                nextLoader = (XmlLoader)it.next();
                                if (nextLoader.getLoadedFileNames()[0].equals(testFileFullName))
                                    returnValue = false;
                                else
                                    returnValue = true;
                            } // For all iterations

                        } // one.
                    } // Look at what's in the collection now.
                } // Got the extension.

                return returnValue;

            } // End method

        };

        buildGenomeVersionNumberVsFeatureSource(genomeVersionId, filter);

    } // End method

    /**
     * Builds a mapping of genome version id versus list of source feature
     * input sources, given a filter for finding candidate files to examine.
     */
    private void buildGenomeVersionNumberVsFeatureSource(int genomeVersionID, FilenameFilter filenameFilter) {
        // First handle currently-registered directories.
        String nextDirectory = null;
        for (Iterator it = directoryNames.iterator(); it.hasNext(); ) {
            nextDirectory = (String)it.next();
            buildGenomeVersionNumberVsFeatureSource(genomeVersionID, filenameFilter, nextDirectory);
        } // For all directories known to this space.

        // Next look at directories implied by inclusion of "hot 5" genome versions.
        String nextPath = null;
        String nextLocation = null;
        File nextFile = null;
        for (Iterator it = recentlyOpenedGenomeVersions.iterator(); it.hasNext(); ) {
            // Avoid adding directory IF the recently-used GV is in a current directory.
            nextPath = (String)it.next();
            nextFile = new File(nextPath);
            nextLocation = nextFile.getParent();
            if (! directoryNames.contains(nextLocation))
                buildGenomeVersionNumberVsFeatureSource(genomeVersionID, filenameFilter, nextLocation);
        } // For all recently-opened known.

    } // End method

    /**
     * Builds a mapping of genome version id versus list of source feature
     * input sources in a given directory, given a filter.
     */
    private void buildGenomeVersionNumberVsFeatureSource(int genomeVersionID, FilenameFilter filenameFilter, String directoryName) {
        int addedLoaderCount = 0;

        // Make the map to add entries.
        if (genomeVersionNumberVsFeatureSource == null)
            genomeVersionNumberVsFeatureSource = new HashMap();

        // Guard against no data found in XML area.
        if (directoryName == null)
            return;

        // Will open the directory and read for .gbf files.
        //
        try {
            // Open the directory/confirm it can be opened.
            File directoryFile = FileUtilities.openDirectoryFile(directoryName);

            // Get list of appropriately-named files.
            String[] filesInDirectory = directoryFile.list(filenameFilter);

            // Now have an array of "okay" filenames.
            GenomeVersionInfo[] featureVersionInfos = getGenomeVersionInfosOf(filesInDirectory, directoryName);

            // Now have an array of feature versions.
            // Note that there will be one genome version item for each file item,
            // so we run through each with the same count loop.
            FeatureXmlLoader featureLoader = null;
            GenomeVersionInfo genomeVersionInfo = FacadeManager.getGenomeVersionInfo(genomeVersionID);

            for (int i = 0; i < featureVersionInfos.length; i++) {
                if (! compatibleWithGenomeVersion(genomeVersionInfo, featureVersionInfos[i], directoryName))
                    continue;

                // NOTE: must add using the genome version ID passed in.
                // Cannot add with the GV ID from the feature version info.
                // The feature version info is only a dummy, created to
                // obtain values to check against.
                featureLoader = new FeatureXmlLoader(genomeVersionID);
                featureLoader.loadXml(directoryFile.getAbsolutePath() +
                    System.getProperty("file.separator") +
                    filesInDirectory[i]);

                addToGenomeVersionNumberVsFeatureSource(genomeVersionID, (XmlLoader)featureLoader);
                addedLoaderCount ++;

            } // For all g-v's in the array.

        } catch (Exception ex) {
            FacadeManager.handleException(ex);
        } finally {
        } // End catch block for directory read.

        if (addedLoaderCount == 0) {
            Integer key = new Integer(genomeVersionID);
            if (genomeVersionNumberVsFeatureSource.get(key) == null)
                genomeVersionNumberVsFeatureSource.put(key, new ArrayList());
        } // No new loaders.

    } // End method: buildGenomeVersionNumberVsFeatureSource

    /** Adds a loader into the map of feature-sourcing loaders, keyed on version number of genome. */
    private void addToGenomeVersionNumberVsFeatureSource(int genomeVersionNumber, XmlLoader loader) {
        if (genomeVersionNumberVsFeatureSource == null)
            genomeVersionNumberVsFeatureSource = new HashMap();  // Should be done prior to call!

        List valueList = null;
        Integer versionInteger = new Integer(genomeVersionNumber);
        if (genomeVersionNumberVsFeatureSource.containsKey(versionInteger)) {
            valueList = (List)genomeVersionNumberVsFeatureSource.get(versionInteger);
        } // Locate the list.
        else {
            valueList = new ArrayList();
            genomeVersionNumberVsFeatureSource.put(versionInteger, valueList);
        } // Make the list.

        // Now add to the value list, if it is not already in there.
        // Test this by checking to see if the first loaded file name is the same.
        // Do not worry about genome versions, because that SHOULD already be
        // covered by the placement in the list itself.
        XmlLoader nextLoader = null;
        boolean alreadyRegistered = false;
        for (Iterator it = valueList.iterator(); (! alreadyRegistered) && it.hasNext(); ) {
            nextLoader = (XmlLoader)it.next();
            if (loader.getLoadedFileNames()[0].equals(nextLoader.getLoadedFileNames()[0])) {
                if (! (loader instanceof SequenceAlignmentLoader))
                    alreadyRegistered = true;
            } // Matches file.
        } // For all members of value list.
        if (! alreadyRegistered)
            valueList.add(loader); // New loader.

    } // End method

    /** Test of compatibility of feature file with genome version in model. */
    private boolean compatibleWithGenomeVersion(  GenomeVersionInfo genomeVersionInfo,
                                                  GenomeVersionInfo featureVersionInfo,
                                                  String directoryName) {

        boolean returnValue = true;

        // Eliminate loaders which do not have the appropriate assembly version,
        // or species.
        if (featureVersionInfo.getAssemblyVersion()
            != genomeVersionInfo.getAssemblyVersion())
            returnValue = false;
        else if (! featureVersionInfo.getSpeciesName().equals(genomeVersionInfo.getSpeciesName()))
            returnValue = false;
        else {
            int genomeVersionId = genomeVersionInfo.getGenomeVersionId();
            returnValue = thisDirectoryShouldSourceFeaturesForGenomeVersion(directoryName, genomeVersionId);
        } // Crucial "in directory" test.

        return returnValue;

    } // End method

    /** Returns true if gv originated here, or if gv is from database. */
    private boolean thisDirectoryShouldSourceFeaturesForGenomeVersion(String directoryName, int genomeVersionId) {
        boolean returnValue = false;

        GenomeVersion genomeVersion = ModelMgr.getModelMgr().getGenomeVersionById(genomeVersionId);
        if (genomeVersion == null)
            FacadeManager.handleException(new IllegalArgumentException("Got request genome version unknown to model"));

        GenomeVersionInfo genomeVersionInfo = genomeVersion.getGenomeVersionInfo();

        // Source info for any GVID which came from the directory
        // whose name was given, or which came from the database.
        if (genomeVersionInfo.isFileDataSource()) {
            if (genomeVersionInfo.getDataSource().startsWith(directoryName))
               returnValue = true;
        } // From XML facade.
        else {
            // If not from XML facades, there is no restriction on whether we
            // can source features for it.
            returnValue = true;
        } // From internal database.

        return returnValue;

    } // End method

} // End class: DirectoryGenomeVersionSpace
