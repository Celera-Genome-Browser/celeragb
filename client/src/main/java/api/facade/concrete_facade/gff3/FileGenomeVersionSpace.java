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

import api.entity_model.model.genetics.GenomeVersion;
import api.entity_model.model.genetics.Species;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.GenomeVersionInfo;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;

import oss.model.builder.gff3.*;

import java.util.ArrayList;
import java.util.Collections;
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
	public void addLoader(DataLoader loader) {
		// For the sake of consistency, and because we are returning
		// collections elsewhere, this single loader will be stored
		// in a collection.
		if (loaderList.size() > 0)
			throw new IllegalStateException("Only one workspace file may be loaded during a session");
		loaderList.add(loader);
	} // End method



	public void removeLoader(DataLoader loader){
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
			DataLoader loader = (DataLoader)loaderList.get(0); // Expect only one.
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
	public DataLoader getLoaderForSpecies(OID speciesOID) {
		GenomeVersionInfo info = getGenomeVersionInfo();

		if (speciesOID.getGenomeVersionId() == info.getGenomeVersionId())
			return (DataLoader)loaderList.get(0);
		else
			return null;
	} // End method

	public Iterator getInputSourcesForAxisOID(OID axisOid) {
		if (loaderList.size() == 0)
			return EMPTY_ITERATOR;

		DataLoader loader = (DataLoader)loaderList.get(0);
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
		return !getSearchableInputSources(genomeVersionId).isEmpty();
	} // End method

	/** Returns all sources which may be searched that lie in the genome version. */
	@Override
	public List<DataLoader> getSearchableInputSources(int genomeVersionID) {
		return getGenomicAxisInputSourceList(genomeVersionID);
	} // End method

	/** Returns all sources for features aligning to an axis in the gv given. */
	public Iterator getGenomicAxisInputSources(int genomeVersionId) {
		return getGenomicAxisInputSourceList(genomeVersionId).iterator();
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

	private List<DataLoader> getGenomicAxisInputSourceList(int genomeVersionId) {
		if (loaderList.size() == 0)
			return Collections.EMPTY_LIST;

		//@todo: reconsider the amount of processing required to learn if the
		// genome version is relevant.

		// Expect one and only one loader in the list!
		DataLoader loader = (DataLoader)loaderList.get(0);
		if (loader == null)
			return new ArrayList();

		Set oidSet = loader.getReferencedOIDSet();
		if (oidSet != null) {
			OID nextOid = null;
			for (Iterator it = oidSet.iterator(); it.hasNext(); ) {
				nextOid = (OID)it.next();
				if (nextOid!=null) {
					if (nextOid.getGenomeVersionId() == genomeVersionId)
						return loaderList;
				}
			} // For all members of the set.
		} // Got set of referenced OIDs.

		return Collections.EMPTY_LIST;

	}

	/** Find vers. info. for the loaded file. */
	private GenomeVersionInfo getGenomeVersionInfo() {
		String fileName = getWorkspaceFileName();
		if (fileName == null)
			return null;

		//TODO bring in a DataLoader which wraps a GFF3 parsing tool.
		String landmarkId = null;
		List<ModelTreeNode> nodes = Gff3DataAssemblerCache.getTopLevelFeaturesFor( fileName, landmarkId );

		// These bland values will have to do until get real info out of file.
		int assemblyVersion = 1;
		String taxonString = fileName;
		String speciesName = fileName;

		for ( ModelTreeNode node: nodes ) {
			Gff3GenericModel dataModel = node.getModel();
			landmarkId = dataModel.getLandmarkId();
			if ( landmarkId != null ) {
				break;
			}
		}

		if ( landmarkId != null ) {
			taxonString = landmarkId;
			speciesName = landmarkId;
		}

		int genomeVersionId = GenomeVersionInfo.calcGenomeVersionId( taxonString, fileName, assemblyVersion );

		OID latestSpeciesOID = null;
		try {
			latestSpeciesOID = OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.API_GENERATED_NAMESPACE,
					genomeVersionId);
		}
		catch (Exception oidException) {
			FacadeManager.handleException(new Exception("Failed to Generate OID for Species "+oidException.getMessage()));
		} // End catch block for oid generation.



		Species species = new Species( latestSpeciesOID, speciesName );
		GenomeVersionInfo genomeVersionInfo = new GenomeVersionInfo( genomeVersionId, speciesName, assemblyVersion, fileName, GenomeVersionInfo.FILE_DATA_SOURCE);
		//    OID genomeVersionOID = null;
		//    try {
		//    	genomeVersionOID = OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.API_GENERATED_NAMESPACE, genomeVersionId);
		//    }
		//    catch (Exception oidException) {
		//    	FacadeManager.handleException(new Exception("Failed to Generate OID for GenomeVersion "+oidException.getMessage()));
		//    } // End catch block for oid generation.

		// KEEP HERE FOR REF
		// GenomeVersion genomeVersion = new GenomeVersion( genomeVersionOID, species, genomeVersionInfo );

		return genomeVersionInfo;
	} // End method

} // End class
