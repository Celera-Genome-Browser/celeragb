package api.facade.concrete_facade.gff3;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oss.model.builder.gff3.Gff3DataAssembler;
import oss.model.builder.gff3.Gff3GenericModel;
import oss.model.builder.gff3.ModelTreeNode;

/**
 * Acts as a repository for Gff3DataAssembler's.  Keeping them here helps to avoid repeated
 * scanning for small amounts of data.
 * 
 * @author Leslie L Foster
 */
public class Gff3DataAssemblerCache {
	private static Map<String,Gff3DataAssembler> sourceToAssembler = new HashMap<String,Gff3DataAssembler>();
	private static Map<String,List<Gff3GenericModel>> sourceToAxes = new HashMap<String,List<Gff3GenericModel>>();
	
	/**
	 * Want the features belonging to a data source?  Come here. The assembler could be cached.  Not caching
	 * the top level models here--too bulky.
	 */
	public static List<ModelTreeNode> getTopLevelFeaturesFor( String dataSourceName, String landMarkId ) {
		String key = normalizeFilenameKey(dataSourceName);
		Gff3DataAssembler assembler = getDataAssemblerFor( key );
		assembler.prepareModels( landMarkId );
		return assembler.getTopLevelFeatures();
	}
	
	/**
	 * If you need the exis models, go here.  They may be cached.
	 */
	public static List<Gff3GenericModel> getAxesFor( String dataSourceName ) {
		String key = normalizeFilenameKey(dataSourceName);
		List<Gff3GenericModel> rtnVal = sourceToAxes.get( key );
		if ( rtnVal == null ) {
			Gff3DataAssembler assembler = getDataAssemblerFor( key );
			rtnVal = assembler.getAxisModels();
			sourceToAxes.put( key, rtnVal );
		}
		return rtnVal;
	}

	private static String normalizeFilenameKey(String dataSourceName) {
		String key = new File( dataSourceName ).getAbsolutePath();
		return key;
	}
	
	private static Gff3DataAssembler createGffAssembler( String inputSourceName ) {
		Gff3DataAssembler assembler = new Gff3DataAssembler( inputSourceName );
		assembler.setMultiParentedFeaturesAcceptable( false );  // TODO re-examine later.
		return assembler;
	}

	/**
	 * Will return a (new?) data assembler, that can handle the data source given.  Do not use this merely
	 * to get the axes.
	 * 
	 * @param dataSourceName expected to be a file.
	 */
	private static Gff3DataAssembler getDataAssemblerFor( String dataSourceName ) {
		Gff3DataAssembler rtnVal = sourceToAssembler.get( dataSourceName );
		if ( rtnVal == null ) {
			rtnVal = createGffAssembler( dataSourceName );
		}
		
		return rtnVal;
	}

}
