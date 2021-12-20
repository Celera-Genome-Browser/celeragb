package api.facade.concrete_facade.shared;

import java.util.List;
import java.util.Map;

import api.entity_model.model.genetics.GenomeVersion;
import api.stub.data.GenomeVersionInfo;

public interface GenomeVersionFactory {
	//------------------------------------------CONSTANTS
	static final boolean READ_ONLY_STATUS = false;

	/** Finds all genome versions within this file. */
	List<GenomeVersion> getGenomeVersions(String filename); // End method: parseForGenomeVersion

	/** Finds all genome version info data within this file.  Keys each by its species. */
	List<GenomeVersionInfo> getGenomeVersionInfos(String filename); // End method: parseForGenomeVersion

}