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
/*
 */
package api.facade.concrete_facade.gff3;

import api.facade.abstract_facade.assembly.GenomicAxisLoader;
import api.facade.abstract_facade.genetics.ChromosomeLoader;
import api.facade.abstract_facade.genetics.SpeciesLoader;
import api.facade.facade_mgr.ConnectionStatus;
import shared.util.FileUtilities;
import api.facade.concrete_facade.gff3.DataLoader;
import api.facade.concrete_facade.gff3.feature_facade.Gff3SpeciesFacade;
import api.facade.concrete_facade.gff3.feature_facade.Gff3ChromosomeFacade;

import java.io.*;
import java.util.*;

/**
 * Extend xml facade manager to layer-on behavior specific to genomic
 * axes.  Particularly, much of the high-level facade implementations.
 */
public class Gff3GenomicAxisFacadeManager extends Gff3FacadeManager implements RecentGenomeVersionManager {

	//------------------------------------------CLASS CONSTANTS
	GenomicAxisLoader genomicAxisFacade=null;
	private Gff3GenomeLocator genomeLocator;
	private ChromosomeLoader chromosome;
	private SpeciesLoader speciesFacade;

	private static final int MAX_LATEST_GBA_NUMBER = 5;
	private static String fileSep=System.getProperty("file.separator");
	// TODO multiple problems with this: it appears identically defined in multiple compilation units, and it is mapped same for xml and gff. LLF
	private static final String MOST_RECENT_GV_PROP_NAME = "RecentlyUsedXmlGenomeVersions";
	private static final File MOST_RECENT_SET_OF_GFFs = new File(System.getProperty("user.home")+fileSep+
			"x"+fileSep+"GenomeBrowser"+fileSep+"userPrefs."+MOST_RECENT_GV_PROP_NAME);

	//------------------------------------------CLASS MEMBER VARIABLES
	/** @todo localize "getExistingLocation" from both here and from other.panels down in xml,and refer down from other.panels. */
	private static final String LOCATION_PROP_NAME = "Gff3GenomeVersionLocation";

	protected File directoryPrefFile=new File(System.getProperty("user.home")+fileSep+
			"x"+fileSep+"GenomeBrowser"+fileSep+"userPrefs."+LOCATION_PROP_NAME);

	//------------------------------------------RecentGenomeVersionManager IMPLEMENTATION
	/** Update queue for recency of use. */
	public void updateMostRecentlyUsedGFFs(Object latestValue) throws Exception {
		List list = readMostRecentlyUsedGFFs();

		// ALGORITHM: to maintain list of most-recently-used .gff files.
		// Remove the latest value from the collection if it is there, and then
		// replace it.  This has the effect of moving it to the head of the list.
		// If the latest value is not in the list, remove the last entry from
		// the queue, and add the latest value to the beginning of the collection.
		if (list.contains(latestValue)) {
			list.remove(latestValue);
		} // In the queue
		else {
			if (list.size() >= MAX_LATEST_GBA_NUMBER)
				list.remove(MAX_LATEST_GBA_NUMBER - 1);
		} // Not in queue
		list.add(0, latestValue);

		writeMostRecentlyUsedGBAs(list);
	} // End method: updateQueue

	/** Read whole queue of data. */
	public List readMostRecentlyUsedGFFs() throws Exception {
		List returnList = new ArrayList();
		List tempList = null;
		String nextFileName = null;
		File nextFile = null;
		if (MOST_RECENT_SET_OF_GFFs.canRead()) {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(MOST_RECENT_SET_OF_GFFs));
			tempList = (List)ois.readObject();

			// It is possible, with dyanmic drive mappings, that a recently-used
			// file is not available at this time, so exclude those that are not.
			for (Iterator it = tempList.iterator(); it.hasNext(); ) {
				nextFileName = (String)it.next();
				nextFile = new File(nextFileName);
				// NOTE: no restrictions per ownership.
				returnList.add(nextFileName);
			} // For all iterations
			ois.close();
		} // Can read it.
		return returnList;
	} // End method: readQueue

	/** Write whole queue of data. */
	public void writeMostRecentlyUsedGBAs(List list) throws Exception {
		ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(MOST_RECENT_SET_OF_GFFs));
		ois.writeObject(list);
		ois.flush();
		ois.close();
	} // End method: readQueue

	//------------------------------------------INTERFACE METHODS
	/** Tell what class should be associated with this one as a UI to load data. */
	public String getDataSourceSelectorClass() {
		return ("client.gui.other.data_source_selectors.Gff3GenomeVersionSelector");
	} // End method: getDataSourceSelectorClass

	/** Tells what the directory is from which files were read. */
	public Object[] getOpenDataSources() {
		if (getGenomeVersionSpace() == null) {
			return getDirectorySourcesAndRecentOpens();
		} // Got space.
		else {
			DirectoryGenomeVersionSpace space = (DirectoryGenomeVersionSpace)getGenomeVersionSpace();
			Iterator<DataLoader> allLoaders = space.getOpenSources();
			DataLoader nextLoader = null;
			List<String> returnList = new ArrayList<String>();
			String[] loadersFileNames = null;
			while ( allLoaders.hasNext() ) {
				loadersFileNames = nextLoader.getLoadedFileNames();
				for (int i = 0; (loadersFileNames != null) && (i < loadersFileNames.length); i++) {
					returnList.add(loadersFileNames[i]);
				} // For all loaded file names.
			} // For all loaders
			if (returnList.size() == 0)
				return getDirectorySourcesAndRecentOpens();
			else
				return returnList.toArray();
		} // Already have a genome version space.
	} // End method

	/**
	 * Returns an indicator of whether additional sources may be added to
	 * this factory.
	 */
	public boolean canAddMoreDataSources() {
		return true;
	} // End method: canAddMoreDataSources

	/** Called when system exiting or data sources closed. */
	public void prepareForSystemExit() {
		super.prepareForSystemExit();

		genomicAxisFacade = null;
		genomeLocator = null;
		chromosome = null;
	} // End method: prepareForSystemExit

	/**
	 * This method will be called to initialize the FacadeManager instance.
	 */
	public ConnectionStatus initiateConnection() {
		try {
			if (getDirectorySourcesAndRecentOpens().length == 0)
				return CONNECTION_STATUS_NO_DEFINED_INFORMATION_SERVICE;
			else {
				String nextDirectory = null;
				for (Iterator it = getExistingLocations().iterator(); it.hasNext(); ) {
					try {
						nextDirectory = (String)it.next();
						FileUtilities.testDirectoryFile(nextDirectory);
					} // End try to test locations.
					catch (IllegalArgumentException iae) {
						return  new ConnectionStatus( "\nThe defined XML Directory: "+
								nextDirectory+
								" \nnot usable because "+
								iae.getMessage(),true);
					} // End catch block for test locations.
				} // For all locations.
				return CONNECTION_STATUS_OK;
			}
		} // End try for connection/
		catch (IllegalStateException ise) {
			return CONNECTION_STATUS_CANNOT_CONNECT;
		} // End catch for defined-but-not-usable
	} // End method

	/**
	 * Genome locator has species already attached.
	 */
	public api.facade.abstract_facade.genetics.GenomeLocatorFacade getGenomeLocator()
	throws Exception {

		// If a locator must be produced, then create one.
		if (genomeLocator == null) {
			genomeLocator = new Gff3GenomeLocator(getGenomeVersionSpace());
		} // No cached one.

		return genomeLocator;

	} // End method: getGenomeLocator

	/**
	 * Returns the species 'loader' for this manager.
	 */
	public SpeciesLoader getSpecies() {
		if (speciesFacade == null) {
			speciesFacade = new Gff3SpeciesFacade(getGenomeVersionSpace());
		} // Must create the facade.
		return speciesFacade;
	} // End method: getSpecies

	/**
	 * Returns the chromosome facade.  This is only done in Genomic Axis FM of all XML FMs.
	 */
	public ChromosomeLoader getChromosome() throws Exception {
		if (chromosome == null) {
			chromosome = new Gff3ChromosomeFacade(getGenomeVersionSpace());
		} // Need to create it.

		return chromosome;
	} // End method: getChromosome

	/**
	 * Returns the 'space' from which all info applying to GVs relevant to the
	 * preset load area(s) will be sourced.
	 */
	public GenomeVersionSpace getGenomeVersionSpace() {
		setupGenomeVersionSpace();
		return super.getGenomeVersionSpace();
	} // End method: getGenomeVersionSpace

	//------------------------------------------HELPER METHODS
	/**
	 * Returns locations of genome version (.gba) files.  This method will be
	 * responsible for ensuring that the correct paths are found.
	 */
	private Set getGenomeVersionLocations() {
		return getExistingLocations();
	} // End method: getGenomeVersionLocation

	/** Returns locations as an array. */
	private String[] getDirectorySourcesAndRecentOpens() {
		Set locSet = getExistingLocations();
		try {
			locSet.addAll(readMostRecentlyUsedGFFs());
		} // End try block to read recent GBAs.
		catch (Exception ex) {
			// Do not care.
		} // End catch block
		String[] returnArray = new String[locSet.size()];
		locSet.toArray(returnArray);
		return returnArray;
	} // End method: getGenomeVersionLocation

	/** Convenience method for setting GVS from various places. */
	private void setupGenomeVersionSpace() {
		if (super.getGenomeVersionSpace() == null)
			super.setGenomeVersionSpace(new DirectoryGenomeVersionSpace(  getGenomeVersionLocations(), this ));
	} // End method

	/** Gets the old setting. */
	private Set getExistingLocations() {
		/** @todo when possible change this to use Model Property implementation. */
		// Set the default directory from a preset preference if possible.
		//
		Set returnSet = new HashSet();
		try {

			if (directoryPrefFile.canRead() && directoryPrefFile.exists()) {
				ObjectInputStream istream= new ObjectInputStream(new FileInputStream(directoryPrefFile));
				String nextLocation = null;
				while (null != (nextLocation = (String)istream.readObject())) {
					returnSet.add(nextLocation);
				} // For all locations in file.
				istream.close();

			} // Permission granted.

		} // End try
		catch (Exception ex) {
			if ( ex instanceof java.io.EOFException ) {
				System.out.println("END of serialized locations encountered.");  
			}
			else {
				ex.printStackTrace();    	  
			}
		} // End catch block for pref file open exceptions.

		return returnSet;
	} // End method

	/** Allow this facade manager to be an origin of chromosome info. */
	protected boolean isOriginOfChromosome() { return true; }

} // End class: XmlGenomicAxisFacadeManager
