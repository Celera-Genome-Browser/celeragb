package api.facade.concrete_facade.gff3;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import oss.model.builder.gff3.Gff3GenericModel;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.concrete_facade.shared.ConcreteOIDParser;
import api.facade.concrete_facade.shared.OIDParser;
import api.stub.data.GenomeVersionInfo;
import api.stub.sequence.Sequence;
import api.stub.sequence.SequenceBuilder;
import api.stub.sequence.SequenceFromFastaBuilder;

/**
 * Loads the axis parts of the GFF3 file.
 * 
 * @author Leslie L Foster
 */
public class Gff3AxisLoader {

	private String inputSourceName;
	private OIDParser oidParser = new ConcreteOIDParser();
	private String genomeAxisId;
	private int axisLength;
	private String species;
	private String assembly;
	private SequenceBuilder sequenceBuilder;

	public Gff3AxisLoader( String source ) {
		this.inputSourceName = source;
		initialize();
	}
	
	public String getAssembly() {
		return assembly;
	}
	
	public String getGenomicAxisID() {
		return genomeAxisId;
	}
	
	public String getSpecies() {
		return species;
	}
	
	public int getAxisLength() {
		return axisLength;
	}
	
	public Collection<SequenceAlignment> getSequenceAlignments() {
		return Collections.EMPTY_LIST;
	}
	
	public SequenceBuilder getSequenceBuilder() {
		return sequenceBuilder;
	}
	
	public Sequence getSequence() {
		return null;
	}

	/** Prepare the loading classes that can make use of the input source to supply data. */
	private void initialize() {
		List<Gff3GenericModel> models = Gff3DataAssemblerCache.getAxesFor( inputSourceName );
		if ( models.size() > 0 ) {
			Gff3GenericModel model = models.get( 0 );
			genomeAxisId = model.getLandmarkId();
			axisLength = Math.max(model.getEnd(), model.getStart());  // NOTE: do not want a non-zero-positioned axis. Therefore do not subtract ends.
		}

		Gff3GenomeVersionFactory gvf = new Gff3GenomeVersionFactory( inputSourceName );
		List<GenomeVersion> gvs = gvf.getGenomeVersions(inputSourceName);
		List<GenomeVersionInfo> gvis = gvf.getGenomeVersionInfos( gvs );
		if ( gvis.size() > 0 ) {
			GenomeVersionInfo info = gvis.get( 0 );
			species = info.getSpeciesName();
			assembly = info.getAssemblyVersionAsString();

		}

		try {
			File putativeFile = new File( inputSourceName + SequenceFromFastaBuilder.NCBI_CHROMO_FASTA_EXTENSION );
			if ( putativeFile.exists() ) {
				sequenceBuilder = new SequenceFromFastaBuilder( putativeFile.getAbsolutePath() );
			}
		} catch ( Exception ex ) {
			sequenceBuilder = null;
			ModelMgr.getModelMgr().handleException( ex );
		}
		
	}
	
}
