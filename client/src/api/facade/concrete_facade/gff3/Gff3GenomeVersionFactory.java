/**
 * 
 */
package api.facade.concrete_facade.gff3;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import api.entity_model.model.genetics.GenomeVersion;
import api.entity_model.model.genetics.Species;
import api.facade.concrete_facade.shared.GenomeVersionFactory;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.GenomeVersionInfo;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;

import oss.model.builder.gff3.Gff3GenericModel;

/**
 * This implementation will create genome versions, infos, and ID's for a given input file.  GFF3 files may have
 * multiple genome versions being sourced.
 *  
 * @author Leslie L Foster
 */
public class Gff3GenomeVersionFactory implements GenomeVersionFactory {

	private static final int ASSEMBLY_VERSION = 1;  // Always 1, here, until find a better source.

	// Datasource name is for logging purposes, only.
	private String datasourceName;
	private GenomeVersionSpace genomeVersionSpace;

	public Gff3GenomeVersionFactory(
			String datasourceName) {
		this.datasourceName = datasourceName;
	}
	
	public void setGenomeVersionSpace( GenomeVersionSpace genomeVersionSpace ) {
		this.genomeVersionSpace = genomeVersionSpace;
	}

	/* (non-Javadoc)
	 * @see api.facade.concrete_facade.shared.GenomeVersionFactory#parseForGenomeVersion(java.lang.String)
	 */
	@Override
	public List<GenomeVersion> getGenomeVersions(String filename) {
		if ( filename == null )
			filename = datasourceName;

		List<GenomeVersion> rtnList = new ArrayList<GenomeVersion>();
		
		if ( new File( filename ).exists() ) {
			List<Gff3GenericModel> axisModels = Gff3DataAssemblerCache.getAxesFor( filename );
			for ( Gff3GenericModel model : axisModels ) {
				GenomeVersion genomeVersion = createGenomeVersion( model, filename );
				rtnList.add( genomeVersion );
				if (genomeVersionSpace != null) {
					genomeVersionSpace.registerSpecies(filename, genomeVersion.getSpecies() );
				}
			}
			
		}
		
		return rtnList;
	}

	/* (non-Javadoc)
	 * @see api.facade.concrete_facade.shared.GenomeVersionFactory#parseForGenomeVersionInfo(java.lang.String)
	 */
	@Override
	public List<GenomeVersionInfo> getGenomeVersionInfos(String filename) {
		List<GenomeVersion> genomeVersions = getGenomeVersions( filename );
		List<GenomeVersionInfo> gvis = getGenomeVersionInfos(genomeVersions);
		
		return gvis;
	}

	/**
	 * If getGV's is called first, this one can use its results rather than scanning the file again.
	 */
	public List<GenomeVersionInfo> getGenomeVersionInfos(
			List<GenomeVersion> genomeVersions) {
		List<GenomeVersionInfo> gvis = new ArrayList<GenomeVersionInfo>();
		
		for ( GenomeVersion gv : genomeVersions ) {
			gvis.add( gv.getGenomeVersionInfo() );
		}
		return gvis;
	}

	//-----------------------------------STATIC INTERFACE and HELPERS
	public static GenomeVersion createGenomeVersion( Gff3GenericModel model, String fileName ) {
		String taxonString = getTaxon( model );
		OID genomeVersionOID = null;
		int genomeVersionId = GenomeVersionInfo.calcGenomeVersionId( taxonString, fileName, ASSEMBLY_VERSION );
		try {
			genomeVersionOID = OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.API_GENERATED_NAMESPACE, genomeVersionId);
		}
		catch (Exception oidException) {
			FacadeManager.handleException(new Exception("Failed to Generate OID for GenomeVersion "+oidException.getMessage()));
		} // End catch block for oid generation.

		Species species = createSpecies( model, taxonString, genomeVersionId );
		
		GenomeVersion latestGenomeVersion = null;
		GenomeVersionInfo genomeVersionInfo = createGenomeVersionInfo( model, fileName );
		try {
			File file = new File( fileName );
			String justFileName = file.getName();
			latestGenomeVersion = new GenomeVersion(
					genomeVersionOID,
					species,       // Important: must match one used in making genome vers. OID
					genomeVersionInfo,
					justFileName,
					READ_ONLY_STATUS,
					null
			);

		}
		catch (Exception ex) {
			FacadeManager.handleException(new IllegalStateException("Failed to Create Genome Version for "+fileName));
		} // End catch block for genome version generation.
		return latestGenomeVersion;
	} // End method

	private static Species createSpecies(Gff3GenericModel model, String taxon, int genomeVersionId) {
        OID speciesOID = OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.API_GENERATED_NAMESPACE,
                genomeVersionId);
		Species species = new Species( speciesOID, taxon );
		return species;
	}
	
	/** Build genome version object from collected data. */
	private static GenomeVersionInfo createGenomeVersionInfo( Gff3GenericModel model, String fileName ) {
		GenomeVersionInfo genomeVersionInfo = null;
		String taxonString = getTaxon(model);

		try {
			int genomeVersionId = GenomeVersionInfo.calcGenomeVersionId( taxonString, fileName, ASSEMBLY_VERSION );
			genomeVersionInfo = new GenomeVersionInfo(
					genomeVersionId,  // Genome version identifier
					taxonString,      // Species
					ASSEMBLY_VERSION, // Assy Ver: must match that used by OID generator above!
					fileName,         // Datasource
					GenomeVersionInfo.FILE_DATA_SOURCE  // Datasource type
			);

		}
		catch (Exception ex) {
			FacadeManager.handleException(new IllegalStateException("Failed to Create Genome Version for "+fileName));
		} // End catch block for genome version generation.

		return genomeVersionInfo;
		
	} // End method

	private static String getTaxon(Gff3GenericModel model) {
		String taxonString = null;
		String modelName = model.getName();
		if ( modelName != null ) {
			taxonString = model.getName().replace( '_', ' ' );			
			int spaceInx = taxonString.lastIndexOf( ' ' );
			if ( spaceInx > -1 ) {
				if ( model.getType() != null  && 
				     model.getType().equalsIgnoreCase( "chromosome" )  &&
				     taxonString.toLowerCase().indexOf( "chr", spaceInx ) == spaceInx + 1 ) {

					taxonString = taxonString.substring( 0, spaceInx );				

				}

			}
		}
		else {
			Map<String,String[]> attributes = model.getAttributes();
			String[] organismNames = attributes.get( "organism" );
			if ( organismNames == null || organismNames.length == 0 ) {
				taxonString = "Unknown";
			}
			else {
				taxonString = organismNames[ 0 ];				
			}
		}
		return taxonString;
	}

}
