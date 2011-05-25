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
package api.facade.concrete_facade.gff3;

/**
 * Title:        Genome Browser Client
 * Description:  Sources up Genome Versions representing whole directories of GFF3 files.
 * @author Les Foster
 * @version $Id$
 */

import api.entity_model.management.ModelMgr;
import api.entity_model.model.genetics.GenomeVersion;
import api.entity_model.model.genetics.Species;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.GenomeVersionInfo;
import api.facade.concrete_facade.shared.GenomeVersionFactory;
import api.facade.concrete_facade.shared.LoaderConstants;
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
	private Map<OID,Species> oidToSpecies = new HashMap<OID,Species>();
	private Map<OID,GenomicAxisGff3Loader> speciesToLoader = new HashMap<OID,GenomicAxisGff3Loader>();
	private Map<Integer,List<GenomicAxisGff3Loader>> genomeVersionNumberVsFeatureSource = null;
	private Map<Integer,List<GenomicAxisGff3Loader>> liveAxisLoaders = new HashMap<Integer,List<GenomicAxisGff3Loader>>();

	private GenomeVersion[] allGenomeVersionsOriginatingHere = null;
	private Set<String> directoryNames = null;
	private List recentlyOpenedGenomeVersions = null;
	private String datasourceName = null;
	private Gff3FacadeManager gff3FacadeManager = null;
	private boolean speciesHasBeenQueried = false;
	private RecentGenomeVersionManager recentGenomeVersionManager = null;

	//---------------------------------CONSTRUCTORS
	/** A genome version space implementation that reads from a directory. */
	public DirectoryGenomeVersionSpace(Set<String> directoryNames, RecentGenomeVersionManager recentGVMgr) {
		this.directoryNames = directoryNames;
		this.recentGenomeVersionManager = recentGVMgr;
		try {
			recentlyOpenedGenomeVersions = recentGenomeVersionManager.readMostRecentlyUsedGFFs();
		} // End try block to read GBAs recently used.
		catch (Exception ex) {
			// There will be no recently-used gbas added (sigh!)
			FacadeManager.handleException(ex);
		} // End catch block.

		StringBuffer datasources = new StringBuffer();
		String nextDirectoryName = null;
		for (Iterator it = this.directoryNames.iterator(); it.hasNext(); ) {
			nextDirectoryName = (String)it.next();
			datasources.append(nextDirectoryName);
			datasources.append(" ");
		} // For all directories.
		this.datasourceName = datasources.toString();

	} // End constructor

	//---------------------------------IMPLEMENTATION OF GenomeVersionSpace
	/** Returns all genome versions found in the directory given. */
	public GenomeVersion[] getGenomeVersions() {
		if (allGenomeVersionsOriginatingHere != null)
			return allGenomeVersionsOriginatingHere;

		List<GenomeVersion> accumulator = new ArrayList<GenomeVersion>();

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
	 * Expect this to be called as a result of parsing .gff files
	 * in the directory for their genome versions.
	 */
	public void registerSpecies(String fileName, Species species) {

		// NOTE: need to call up and notify species listeners here.
		OID speciesOID = species.getOid();

		oidToSpecies.put(speciesOID, species);

		if (fileName != null) {
			// Must create a loader and cache it in a map.
			GenomicAxisGff3Loader loader = new GenomicAxisGff3Loader(speciesOID);

			// Make this loader a listener for events.
			loader.addSequenceAlignmentListener(this);
			loader.loadGff3(fileName);
			speciesToLoader.put(speciesOID, loader);

		} // Source found

	} // End method

	/** Returns a loader for a species OID. */
	public DataLoader getLoaderForSpecies(OID speciesOID) {
		// Look for a cached loader.
		speciesHasBeenQueried = true;

		// Returning a loader for a species implies that we need
		// to keep that loader on an "in-use" list for the genome version
		// whose species OID was given.
		Integer key = new Integer(speciesOID.getGenomeVersionId());
		GenomicAxisGff3Loader loader = speciesToLoader.get(speciesOID);

		// Avoid adding a null to the live axis loaders list.
		if (loader == null)
			return null;

		List<GenomicAxisGff3Loader> loadersInGenomeVersion = null;
		if (null == (loadersInGenomeVersion = liveAxisLoaders.get(key) )) {
			loadersInGenomeVersion = new ArrayList<GenomicAxisGff3Loader>();
			liveAxisLoaders.put(key, loadersInGenomeVersion);
		} // Have existing list.

		// Add this loader to the live list for the species, if it is not
		// already in.
		if (! loadersInGenomeVersion.contains(loader)) {
			loadersInGenomeVersion.add(loader);
			try {
				recentGenomeVersionManager.updateMostRecentlyUsedGFFs(loader.getLoadedFileNames()[0]);
			}
			catch (Exception ex) {
				// Recently-used not updated.
				FacadeManager.handleException(ex);
			}
		}

		return loader;

	} // End method: getLoaderForSpeciesOID

	/** Given an OID, returns its species. */
	public Species getSpeciesOf(OID speciesOID) {
		return (Species)oidToSpecies.get(speciesOID);
	} // End method: getSpeciesOf

	/** Supplies all genomic axis source with data on OID. */
	public Iterator getInputSourcesForAxisOID(OID axisOID) {
		List<LoaderBase> returnList = new ArrayList<LoaderBase>();
		GenomicAxisGff3Loader loader = null;
		for (Iterator it = speciesToLoader.values().iterator(); it.hasNext(); ) {
			loader = (GenomicAxisGff3Loader)it.next();
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
	@Override
	public List getSearchableInputSources(int genomeVersionId) {
		// Make collection into which to store returnables.
		List returnList = new ArrayList();

		// NOTE: only include the axis (gba) files, or axis loaders, if
		// they are where the genome version was calculated from.

		// Registry of species against various things, including the
		// loader, will be carried out by the time "getGenomeVersions"
		// returns.
		/*
		GenomeVersion[] genomeVersions = getGenomeVersions();
		int loaderGenomeVersionId = 0;
		GenomicAxisGff3Loader axisLoader = null;
		for (int i = 0; i < genomeVersions.length; i++) {
			loaderGenomeVersionId = genomeVersions[i].getGenomeVersionInfo().getGenomeVersionId();
			if (loaderGenomeVersionId == genomeVersionId) {
				OID speciesOid = genomeVersions[i].getSpecies().getOid();
				axisLoader = (GenomicAxisGff3Loader)speciesToLoader.get(speciesOid);
				axisLoader.scanForSequenceAlignments();
				returnList.add(axisLoader);
				break;
			} // Found one.
		} // For all gvs found in input directory.
		*/
		// Find the list corresponding to the genome version ID.
		returnList.addAll(featureSourcesInGenomeVersion(genomeVersionId, true));

		return returnList;
	} // End method

	/** Supplies all input sources known to this space, with axis refs, and in the GV whose ID is given. */
	public Iterator<GenomicAxisGff3Loader> getGenomicAxisInputSources(int genomeVersionID) {
		// Make collection into which to store returnables.
		List<GenomicAxisGff3Loader> returnList = new ArrayList<GenomicAxisGff3Loader>();

		// Make sure the mapping is made for the genome version ID in
		// question, vs its list of applicable sources.
		returnList.addAll(featureSourcesInGenomeVersion(genomeVersionID, false));

		// NOTE: only include the axis (gba) files, or axis loaders, if
		// they were the source for the genome version.
		List<GenomicAxisGff3Loader> loaderList = null;
		if (thisSpaceIsGVOrigin()) {
			if (liveAxisLoaders.size() > 0) {
				if (null != (loaderList = liveAxisLoaders.get(new Integer(genomeVersionID)))) {
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
	public Iterator<GenomicAxisGff3Loader> getDataSourcesRepresentingGenomicAxes(int genomeVersionID) {
		List<GenomicAxisGff3Loader> returnList = new ArrayList<GenomicAxisGff3Loader>();
		if (thisSpaceIsGVOrigin()) {
			List<GenomicAxisGff3Loader> liveList = (List<GenomicAxisGff3Loader>)liveAxisLoaders.get(new Integer(genomeVersionID));
			if (liveList != null)
				returnList.addAll(liveList);
		} // Must source data for axes.

		return returnList.iterator();
	} // End method

	/** Returns data sources in this space, which have served data. */
	public Iterator<DataLoader> getOpenSources() {
		List<DataLoader> returnList = new ArrayList<DataLoader>();

		// Get the feature sources.
		if (genomeVersionNumberVsFeatureSource != null) {
			//<Integer,List<DataLoader>>
			for ( List<GenomicAxisGff3Loader> loaderList: genomeVersionNumberVsFeatureSource.values() ) {
				returnList.addAll( loaderList );
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

		// Create a loader equipped to handle sequence alignment-originating files.
		SequenceAlignmentLoader loader = new SequenceAlignmentLoader(sequenceAlignment, genomeVersionId);

		// Carry out any user-requested validations.
		//ValidationManager.getInstance().validateAndReportInputFile(sequenceAlignment.getFilePath());

		// Add the loader, and make it available to respond to various requests.
		// Make sure that there is somewhere to store the gv/source mapping.
		if (! featureSourcesRegisteredForGenomeVersion(genomeVersionId))
			buildGenomeVersionNumberVsFeatureSource(genomeVersionId);
		addToGenomeVersionNumberVsFeatureSource(genomeVersionId, (DataLoader)loader);

	} // End method

	/** Called when it is permissible for this listener to stop listening to a given source. */
	public void noMoreAlignments(Object source) {
		// Stop listening.
		GenomicAxisGff3Loader nextLoader = null;
		for (Iterator it = speciesToLoader.values().iterator(); it.hasNext(); ) {
			nextLoader = (GenomicAxisGff3Loader)it.next();
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
					return name.endsWith( LoaderConstants.GFF_FILE_EXTENSION );
				} // End method: accept
			});

			// Now have an array of "okay" filenames.
			returnList.addAll(
					getGenomeVersionsOf(filesInDirectory, directoryName)
			);

		} // End try block.
		catch (Exception ex) {
			FacadeManager.handleException(ex);
		} // End catch block.

		return returnList;

	} // End method: getGenomeVersions

	/** Finds genome versions of all files in input collection. */
	private Collection<GenomeVersion> getGenomeVersions(Collection<String> pathsToRead) {
		List<GenomeVersion> returnList = new ArrayList<GenomeVersion>();
		File nextFile = null;
		String nextLocation = null;
		Gff3GenomeVersionFactory parser = new Gff3GenomeVersionFactory( datasourceName );
		parser.setGenomeVersionSpace( this );  // NOTE: may wish to change this interface to reduce inter-coupling.

		for ( String nextPath : pathsToRead ) {
			if ( nextPath.endsWith( LoaderConstants.GFF_FILE_EXTENSION ) ) {
				// Check path: is it in a currently-registered directory?
				nextFile = new File(nextPath);
				nextLocation = nextFile.getParent();
				if ((directoryNames == null) || (! directoryNames.contains(nextLocation))) {
					List<GenomeVersion> nextVersionList = parser.getGenomeVersions(nextPath);
					for ( GenomeVersion nextVersion: nextVersionList ) {
						if (! isGenomeVersionPreviouslyRegistered(nextVersion, returnList)) {
							returnList.add(nextVersion);                	
						}
					}
				} // Must look at path here.        		
			}

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
	private List<GenomicAxisGff3Loader> featureSourcesInGenomeVersion(int genomeVersionId, boolean updateFeatureListForGenomeVersion) {
		if (! featureSourcesRegisteredForGenomeVersion(genomeVersionId))
			buildGenomeVersionNumberVsFeatureSource(genomeVersionId);
		else if (updateFeatureListForGenomeVersion)
			updateGenomeVersionNumberVsFeatureSource(genomeVersionId);

		if (genomeVersionNumberVsFeatureSource == null)
			return Collections.EMPTY_LIST;

		List<GenomicAxisGff3Loader> returnList = genomeVersionNumberVsFeatureSource.get(new Integer(genomeVersionId));
		if (returnList == null)
			return java.util.Collections.EMPTY_LIST;
		else
			return returnList;
	} // End method

	/** Builds unique collection of file extension of value here. */
	private Set createExtensionSet() {
		Set extensionSet = new HashSet();
		extensionSet.add(LoaderConstants.GFF_FILE_EXTENSION);
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
	private Collection<GenomeVersion> getGenomeVersionsOf(String[] files, String directoryName) {
		// Look at each file.
		Gff3GenomeVersionFactory parser = new Gff3GenomeVersionFactory(datasourceName);
		parser.setGenomeVersionSpace( this );
		List<GenomeVersion> versionList = new ArrayList<GenomeVersion>();

		File directoryFile = FileUtilities.openDirectoryFile(directoryName);
		String nextFile = null;
		File tmpFile;

		for (int i = 0; i < files.length; i++) {
			tmpFile= new File(directoryFile, files[i]);
			nextFile = tmpFile.getAbsolutePath();
			List<GenomeVersion> nextVersionList = parser.getGenomeVersions(nextFile);
			for ( GenomeVersion nextVersion: nextVersionList ) {            	
				if (! isGenomeVersionPreviouslyRegistered(nextVersion, versionList)) {
					versionList.add(nextVersion);
				} // Only register once.
			}

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
		Gff3GenomeVersionFactory parser = new Gff3GenomeVersionFactory( datasourceName );
		parser.setGenomeVersionSpace( this );
		List<GenomeVersionInfo> infoList = new ArrayList<GenomeVersionInfo>();

		// Should not happen once we get to here, but just as a precaution...
		if (directoryName == null)
			return new GenomeVersionInfo[0];

		File directoryFile = FileUtilities.openDirectoryFile(directoryName);
		String nextFile = null;

		for (int i = 0; i < files.length; i++) {
			// Run a SAX parse that will bail as soon as it has its required info.
			//
			//System.out.println("Parsing file "+files[i]+" for genome version");
			nextFile = new File(directoryFile, files[i]).getAbsolutePath();
			List<GenomeVersionInfo> nextInfoList = parser.getGenomeVersionInfos(nextFile);
			infoList.addAll( nextInfoList );

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
				if (name.endsWith( LoaderConstants.GFF_FILE_EXTENSION )) {
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
				if (name.endsWith(LoaderConstants.GFF_FILE_EXTENSION)) {
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

							DataLoader nextLoader = null;
							for (Iterator it = mappedToThisGV.iterator(); (returnValue == false) && it.hasNext(); ) {
								nextLoader = (DataLoader)it.next();
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
			FeatureGff3Loader featureLoader = null;
			GenomeVersionInfo genomeVersionInfo = FacadeManager.getGenomeVersionInfo(genomeVersionID);

			for (int i = 0; i < featureVersionInfos.length; i++) {
				if (! compatibleWithGenomeVersion(genomeVersionInfo, featureVersionInfos[i], directoryName))
					continue;

				// NOTE: must add using the genome version ID passed in.
				// Cannot add with the GV ID from the feature version info.
				// The feature version info is only a dummy, created to
				// obtain values to check against.
				featureLoader = new FeatureGff3Loader(genomeVersionID);
				featureLoader.loadGff3(directoryFile.getAbsolutePath() +
						System.getProperty("file.separator") +
						filesInDirectory[i]);

				addToGenomeVersionNumberVsFeatureSource(genomeVersionID, (DataLoader)featureLoader);
				addedLoaderCount ++;

			} // For all g-v's in the array.

		} catch (Exception ex) {
			FacadeManager.handleException(ex);
		} finally {
		} // End catch block for directory read.

		if (addedLoaderCount == 0) {
			Integer key = new Integer(genomeVersionID);
			if (genomeVersionNumberVsFeatureSource.get(key) == null)
				genomeVersionNumberVsFeatureSource.put(key, new ArrayList<GenomicAxisGff3Loader>());
		} // No new loaders.

	} // End method: buildGenomeVersionNumberVsFeatureSource

	/** Adds a loader into the map of feature-sourcing loaders, keyed on version number of genome. */
	private void addToGenomeVersionNumberVsFeatureSource(int genomeVersionNumber, DataLoader loader) {
		if (genomeVersionNumberVsFeatureSource == null)
			genomeVersionNumberVsFeatureSource = new HashMap<Integer,List<GenomicAxisGff3Loader>>();  // Should be done prior to call!

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
		DataLoader nextLoader = null;
		boolean alreadyRegistered = false;
		for (Iterator it = valueList.iterator(); (! alreadyRegistered) && it.hasNext(); ) {
			nextLoader = (DataLoader)it.next();
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
