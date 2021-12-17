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
 * Title:        Genome Versions from XML Files
 * Description:  Provide genome versions found by reading XML.
 * @author       Les Foster
 * @version $Id$
 */
import api.entity_model.model.genetics.GenomeVersion;
import api.entity_model.model.genetics.Species;
import api.stub.data.OID;

import java.util.Iterator;
import java.util.List;

/**
 * Produces genome versions from a given space or area.
 */
public interface GenomeVersionSpace {

	/** Supplies the array of genome versions. */
	public abstract GenomeVersion[] getGenomeVersions();

	/** Tells whether data exists in this space from loaded files. */
	public abstract boolean hasLoaders();

	/** Supplies the species which has the OID. */
	public abstract Species getSpeciesOf(OID speciesOID);

	/** 'Gets' the loader for a species OID. */
	public abstract DataLoader getLoaderForSpecies(OID speciesOID);

	/** Returns sources in this space, which have served data. */
	public abstract Iterator getOpenSources();

	/** Supplies all input sources with data on the OID. */
	public abstract Iterator getInputSourcesForAxisOID(OID axisOID);

	/**
	 * Supplies all input sources in a genome version by ID, referring to any GenAx.
	 * To be valid, any axis files must have already been queried for their data.
	 */
	public abstract Iterator getGenomicAxisInputSources(int genomeVersionID);

	/**
	 * Supplies all input sources in which a searchable quantity may reside.
	 * This may include unopened genomic axis / genome version sources.
	 */
	public abstract List getSearchableInputSources(int genomeVersionID);

	/**
	 * Tells if it is possible, with hopefully minimal time wastage, for the
	 * genome version space to have ANY sources worth searching in the genome version.
	 * Used to optimize searches against database, by limiting flatfile overhead.
	 */
	public abstract boolean hasSearchableInputSourcesInGenomeVersion(int genomeVersionId);

	/** Supplies all input sources which may represent a genomic axis. */
	public abstract Iterator getDataSourcesRepresentingGenomicAxes(int genomeVersionId);

	/** Allows registry of species info. */
	public abstract void registerSpecies(String sourceFileName, Species species);

} // End class: GenomeVersionSpace
