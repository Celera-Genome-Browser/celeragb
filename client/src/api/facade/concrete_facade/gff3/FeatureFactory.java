package api.facade.concrete_facade.gff3;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import api.stub.data.GenomicEntityComment;
import api.stub.data.OID;

import oss.model.builder.gff3.Gff3GenericModel.Strand;
import oss.model.builder.gff3.ModelTreeNode;
import oss.model.builder.gff3.Gff3GenericModel;

import api.entity_model.model.genetics.GenomeVersion;
import api.facade.concrete_facade.shared.ConcreteFacadeConstants;
import api.facade.concrete_facade.shared.ConcreteOIDParser;
import api.facade.concrete_facade.shared.OIDParser;
import api.facade.concrete_facade.shared.PropertySource;
import api.facade.concrete_facade.shared.feature_bean.*;
import api.facade.facade_mgr.FacadeManagerBase;

/**
 * Builds features, given information constructed from a GFF3 read, by remarshalling those data
 * objects into ones usable by this package.
 * 
 * @author Leslie L Foster
 */
public class FeatureFactory {
	// TODO consider eliminating in favor of EntityType.properties read-up.
	private static final String GENE_TYPE_GFF = "gene";
	private static final String GENE_TYPE_GB = "_GENE";
	private static final String TRANSCRIPT_TYPE_GFF = "transcript";
	private static final String TRANSCRIPT_TYPE_GB = "_TRANSCRIPT";
	private static final String MRNA_TYPE_GFF = "mRNA";
	private static final String PROTEIN_TYPE_GFF = "protein";
	private static final String PROTEIN_TYPE_GB = "_PROTEIN";
	private static final String EXON_TYPE_GFF = "exon";
	private static final String EXON_TYPE_GB = "_EXON";
	private static final String TRANSLATION_POSITION_TYPE_GFF = "translation";
	private static final String TRANSLATION_POSITION_TYPE_GB = "_TRANSLATION_POSITON";
	private static final String START_CODON_TYPE_GFF = "start_codon";
	private static final String START_CODON_TYPE_GB = "_START_CODON";
	private static final String STOP_CODON_TYPE_GFF = "stop_codon";
	private static final String STOP_CODON_TYPE_GB = "_STOP_CODON";
	private static final String TRANSLATION_START_POS_TYPE_GFF = "translationStart";
	private static final String TRANSLATION_START_POS_TYPE_GB = "_TRANSLATION_START_POSITION";
	private static final String CDS_TYPE_GFF = "CDS";
	//_TRANSCRIPT,_PROTEIN,_EXON,_TRANSLATION_POSITON,_START_CODON,_STOP_CODON,_TRANSLATION_START_POSITION

	private static final String CONTIG_GB = "_CONTIG";
	public static final String CONTIG_GFF = "contig";
	
	public static final String CHROMOSOME_GFF = "chromosome";
	public static final String SOURCE_GFF = "source";

	private static Map<String,String> curatedGffToGBTypeMapping;
	
	static {
		curatedGffToGBTypeMapping = new HashMap<String,String>();
		curatedGffToGBTypeMapping.put(GENE_TYPE_GFF,                  GENE_TYPE_GB);
		curatedGffToGBTypeMapping.put(TRANSCRIPT_TYPE_GFF,            TRANSCRIPT_TYPE_GB);
		curatedGffToGBTypeMapping.put(CDS_TYPE_GFF,                   TRANSCRIPT_TYPE_GB);
		curatedGffToGBTypeMapping.put(MRNA_TYPE_GFF,                  TRANSCRIPT_TYPE_GB);
		curatedGffToGBTypeMapping.put(PROTEIN_TYPE_GFF,               PROTEIN_TYPE_GB);
		curatedGffToGBTypeMapping.put(EXON_TYPE_GFF,                  EXON_TYPE_GB);
		curatedGffToGBTypeMapping.put(TRANSLATION_POSITION_TYPE_GFF,  TRANSLATION_POSITION_TYPE_GB);
		curatedGffToGBTypeMapping.put(START_CODON_TYPE_GFF,           START_CODON_TYPE_GB);
		curatedGffToGBTypeMapping.put(STOP_CODON_TYPE_GFF,            STOP_CODON_TYPE_GB);
		curatedGffToGBTypeMapping.put(TRANSLATION_START_POS_TYPE_GFF, TRANSLATION_START_POS_TYPE_GB);
	}
	
	private String inputSourceName;
	private OID axisOID;
	private FacadeManagerBase facadeMgr;
	private OIDParser oidParser;
	private FeatureListener featureListener;
	private GffIdentifierOidHandler oidHandler;

	//---------------------------------------------CONSTRUCTORS
	public FeatureFactory() {
		oidParser = new ConcreteOIDParser();
		oidHandler = new GffIdentifierOidHandler();
	}
	
	//---------------------------------------------PUBLIC INTERFACE
	/**
	 * Expects file to contain GFF3 data. Submits that to a parser, grabs its output for use by caller.
	 * 
	 * @param fileName
	 */
	public void loadFile( String fileName, OID axisOfInterest, FacadeManagerBase facadeMgr ) throws Exception {
		inputSourceName = fileName;
		axisOID = axisOfInterest;
		this.facadeMgr = facadeMgr;
		File inputFile = new File( fileName );
		if ( ! inputFile.canRead()  ||  ! inputFile.isFile() ) {
			throw new Exception( "Cannot read file " + fileName );
		}
		initializeContents( fileName );
	}
	
	/**
	 * Expects a filename with GFF3 data.  Scans it for "landmark" OIDs.
	 * 
	 * @param fileName what to look at.
	 * @param facadeMgr for use of error messaging.
	 * @return list of all OIDs.
	 */
	public List<OID> getReferencedAxisOIDs( String fileName ) {
		inputSourceName = fileName;
		List<Gff3GenericModel> axes = Gff3DataAssemblerCache.getAxesFor(fileName);
		List<OID> rtnList = new ArrayList<OID>();
		for ( Gff3GenericModel axis: axes ) {
			GenomeVersion gv = Gff3GenomeVersionFactory.createGenomeVersion(axis, fileName );
			int gvId = gv.getGenomeVersionInfo().getGenomeVersionId();
			rtnList.add( makeHashOID( axis.getLandmarkId(), gvId ) );
		}
		
		return rtnList;
	}
	
	/** Can use this to tell client whenever a feature is encountered at base level. */
	public void setFeatureListener( FeatureListener featureListener ) {
		this.featureListener = featureListener;
	}
	
	//---------------------------------------------HELPERS
	/**
	 * This calls up the Gff3 packages to get file contents into memory.  Set this before loading files.
	 */
	private void initializeContents( String fileName ) throws Exception {
		List<Gff3GenericModel> axes = Gff3DataAssemblerCache.getAxesFor( fileName );
		if ( axes.size() > 0 ) {
			Gff3GenericModel axisModel = axes.get( 0 );
    		GenomeVersion gv = Gff3GenomeVersionFactory.createGenomeVersion(axisModel, fileName );
			int gvId = gv.getGenomeVersionInfo().getGenomeVersionId();
			String axis = axisModel.getLandmarkId();
			List<ModelTreeNode> topLevelNodes = Gff3DataAssemblerCache.getTopLevelFeaturesFor( fileName, axis );
			// System.out.println("Found " + topLevelNodes.size() + " nodes for axis " + axis);
			
			// From here, can re-marshall those into internal data.
			for ( ModelTreeNode node: topLevelNodes ) {
				Gff3GenericModel model = node.getModel();
				if ( node.getChildren() != null  &&  node.getChildren().size() > 0 ) {
					if ( model != null ) {
						buildFeatureHierarchy( node, null, fileName, axisModel, gv );
					}
					else {
						System.out.println("Node " + node.getId() + " has null model." );
					}
				}
				else {
					// Make a non-hierarchical model.
					OID featureOID = makeHashOID( model.getId(), gvId );
					NonHierarchicalFeatureBean bean = new NonHierarchicalFeatureBean( featureOID, axisOID, facadeMgr );
					bean.setParent(null);
					setGenericBeanProperties( model, bean );
					featureListener.feature( bean );
				}

			}
			
		}
	}

	/** Build out features at least two levels deep, starting from a compound feature, and ending at one or more simple features.  Recursive. */
	private void buildFeatureHierarchy( ModelTreeNode node, CompoundFeatureBean parentBean, String fileName, Gff3GenericModel axisModel, GenomeVersion gv ) {
		Gff3GenericModel model = node.getModel();
		FeatureBean bean = null;
		int gvId = gv.getGenomeVersionInfo().getGenomeVersionId();
		OID featureOID = makeHashOID( model.getId(), gvId );

		boolean isGene = isCuratedType( model, GENE_TYPE_GB );
		if ( isGene  &&  hasNoChildren( node ) ) {
			// Must establish a Gene/Transcript/Exon hierarchy out of what is found.
			ModelTreeNode childNode = createGeneChildNode(node, model, TRANSCRIPT_TYPE_GFF);
			createGeneChildNode(childNode, childNode.getModel(), EXON_TYPE_GFF);
		}
		else if ( isCuratedType( model, TRANSCRIPT_TYPE_GB )  &&  hasNoChildren( node ) ) {
			// Must establish a Transcript/Exon hierarchy out of what is found.
			createGeneChildNode(node, model, EXON_TYPE_GFF);
		}
		else if ( isCuratedType( model, EXON_TYPE_GB )  &&  parentBean.getAnalysisType().equals( GENE_TYPE_GB ) ) {
			// Must ensure that parent is not directly a gene, but has a transcript intermediate.
			ModelTreeNode interParentNode = createGeneChildNode(node, model, TRANSCRIPT_TYPE_GFF );
			interParentNode.addChild( node );
			model.setParent( new String[] { interParentNode.getId() } );
		}

		if ( ! hasNoChildren( node ) ) {
			CompoundFeatureBean compoundBean;
			if ( isGene ) {
				compoundBean = createGeneBean( node, model, featureOID );
			}
			else {
				compoundBean = new CompoundFeatureBean( featureOID, axisOID, facadeMgr );
			}
			bean = compoundBean;

			setGenericBeanProperties( model, bean );
			
			for ( ModelTreeNode nextChildNode: node.getChildren() ) {

				// * * * Recursion * * *
				buildFeatureHierarchy( nextChildNode, compoundBean, fileName, axisModel, gv );

			}
			
			// This is where the stuff is 
			if ( parentBean == null ) {
				featureListener.feature( compoundBean );
			}

		}
		else {
			SimpleFeatureBean simpleBean = new SimpleFeatureBean( featureOID, axisOID, facadeMgr );
			bean = simpleBean;
			setGenericBeanProperties( model, bean );

		}
		
		if ( parentBean != null ) {
			// Take care of parent-up links.
			parentBean.addChild( bean );
			bean.setParent( parentBean );
		}
		
	}

	/** Special handling for genes.  */
	private CompoundFeatureBean createGeneBean(ModelTreeNode geneTreeNode, Gff3GenericModel geneModel, OID featureOID) {
		GeneFeatureBean geneBean = new GeneFeatureBean( featureOID, axisOID, facadeMgr );
		//System.out.println("GENE: " + geneModel.getStart()+"-"+geneModel.getEnd()+" " + geneTreeNode.getChildren().size() + " child nodes." );
		geneBean.setParent(null);
		
		geneBean.setAnnotationName( getAccessions( geneModel ) );
		return geneBean;
	}

	/*
	 * Here, had to create a nominal child node for the empty parent gene.  Represents whole gene.
	 * TODO find out: CDS should be in this place?
	 */
	private ModelTreeNode createGeneChildNode(ModelTreeNode parentNode, Gff3GenericModel parentModel, String type) {
		Gff3GenericModel childModel = new Gff3GenericModel();
		childModel.setParent( new String[] { parentModel.getId() } );
		childModel.setEnd( parentModel.getEnd() );
		childModel.setStart( parentModel.getStart() );
		childModel.setLandmarkId( parentModel.getLandmarkId() );
		childModel.setName( "_child_of_" + parentModel.getId() );
		childModel.setNote( "This Gene Child added to complete gene.  Patterned after whole gene.  Not a/n " + type + "." );
		childModel.setType( type );
		childModel.setAttributes( Collections.EMPTY_MAP );
		childModel.setStrand( parentModel.getStrand() );
		ModelTreeNode childNode = new ModelTreeNode( childModel );
		childNode.addParent( parentNode );
		parentNode.addChild( childNode );
		return childNode;
	}

	/** Attempts to guess the accession for annotated features. */
	private String getAccessions(Gff3GenericModel geneModel) {
		StringBuilder rtnVal = new StringBuilder();
		// Try to get a gene accession.
		String[] dbxrefs = geneModel.getDbxref();
		Map<String,String[]> attributes = geneModel.getAttributes();
		String[] accessions = attributes.get("Accession");
		if ( accessions != null  &&  accessions.length > 0 ) {
			rtnVal.append( accessions[ 0 ] );
		}
		if ( dbxrefs != null ) {
			for ( String dbxref: dbxrefs ) {
				if ( dbxref.startsWith( "GeneID:" ) ) {
					if ( rtnVal.length() > 0 ) {
						rtnVal.append( ";" );
					}
					rtnVal.append( dbxref );
				}
			}
			
		}
		String[] locusTags = attributes.get( "locus_tag" );
		if ( locusTags != null  &&  locusTags.length > 0 ) {
			for ( String locusTag: locusTags ) {
				if ( rtnVal.length() > 0 ) {
					rtnVal.append( ";" );
				}
				rtnVal.append( locusTag );				
			}
		}
		
		return rtnVal.toString();
	}

	/**
	 * Call this method to move the props from the model to the bean, which will always be relevant
	 * regardless of the hierarchical position or type of feature bean.  Includes coordinate-system
	 * conversion.
	 * 
	 * @param model from this
	 * @param bean to this.
	 */
	private void setGenericBeanProperties(
			Gff3GenericModel model,
			FeatureBean bean) {

		if ( isNotEmpty( model.getNote() ) )
			bean.setComments( new GenomicEntityComment[] { new GenomicEntityComment( model.getNote() ) } );

		// In GFF, 1-based, and base-based coords.  In GB, 0-based and space-based coordinates.  Therefore subtracting one from start.
		if ( model.getStart() == 0 ) {
			throw new RuntimeException("Misrepresentation of GFF data as 0-based.  http://www.sequenceontology.org/gff3.shtml claims 1-based features.");
		}
		
		bean.setStart( startQueryOnStrand( model.getStart(), model.getEnd(), model.getStrand() ) );
		bean.setEnd( endQueryOnStrand( model.getStart(), model.getEnd(), model.getStrand() ) );
		bean.setDescription( model.getNote() );
		
		bean.setSubjectStart( model.getStart() - 1 );
		bean.setSubjectEnd( model.getEnd() );
		
		boolean isCurated = false;
		String analysisType = null;

		if ( curatedGffToGBTypeMapping.containsKey( model.getType().trim() ) ) {
			isCurated = true;
			analysisType = curatedGffToGBTypeMapping.get( model.getType() );
			bean.setDiscoveryEnvironment( ConcreteFacadeConstants.CURATION_DISCOVERY_ENVIRONMENT );
		}
		if ( analysisType == null ) {
			analysisType = model.getType();
		}
		bean.setAnalysisType( analysisType );

		bean.setCurated( isCurated );
		if ( ! isCurated ) {
			// TODO revisit discovery environment.  May wish to use some sort of tier mapping.
			String discoveryEnvironment = bean.getAnalysisType();
			FeatureBean ancestorBean = bean;
			while ( null != ( ancestorBean = ancestorBean.getParent() ) ) {
				discoveryEnvironment = ancestorBean.getAnalysisType();
			}
			bean.setDiscoveryEnvironment( discoveryEnvironment );
		}
		
		// Establish means of showing properties when the need arises.
		for ( String key: model.getAttributes().keySet() ) {
			String[] valueArr = model.getAttributes().get( key );
			for ( String value: valueArr ) {
				addNonNullProperty( bean, key, value );
			}
		}
		
		// Add these for a sanity check.
		addNonNullProperty( bean, "Analysis Type", analysisType );
		addNonNullProperty( bean, "GFF Type", model.getType() );
		addNonNullProperty( bean, "GFF Start", "" + model.getStart() );
		addNonNullProperty( bean, "GFF End", "" + model.getEnd() );
		addNonNullProperty( bean, "GFF Strand", "" + model.getStrand() );
		addNonNullProperty( bean, "GFF Source", model.getSource() );
		addNonNullProperty( bean, "GFF Score", "" + model.getScore() );

	}

	/** This is where the  */
	private int startQueryOnStrand( int start, int end, Strand strand ) {
		int retVal = 0;
		if ( strand == Strand.negative ) {
			retVal = end;
		}
		else {
			// Any of a host of non-informative strand values, or positive, go here.
			retVal = start - 1;
		}
		return retVal;
	}

	private int endQueryOnStrand( int start, int end, Strand strand ) {
		int retVal = 0;
		if ( strand == Strand.negative ) {
			retVal = start - 1;
		}
		else {
			// Any of a host of non-informative strand values, or positive, go here.
			retVal = end;
		}
		return retVal;
		
	}

	/** Add a sourced property to the bean, with name/value given, and not to be editable.  Only if non-null name/value. */
	private void addNonNullProperty( FeatureBean bean, String name, String value ) {
		if ( value != null  &&  name != null )
		    bean.addPropertySource( new PropertySource( name, value, false ) );
	}
	
	/** Make an object identifier from the input string. */
	private OID makeHashOID( String id, int genomeVersionId ) {
		OID rtnOid = null;
		if ( id == null ) {
			id = "" + new Double( Math.random() * 100000 ).longValue();
			//System.out.println("Random id generated for feature...");
		}
		int pos = id.indexOf( ":" );
		if ( pos > -1 ) {
			// Already has prefix.
			String prefix = id.substring( 0, pos + 1 );
			String suffix = id.substring( pos + 1 );
			long numericPart = suffix.hashCode();
			if ( numericPart < 0 ) {
				numericPart += Long.MAX_VALUE;
			}
			rtnOid = oidParser.parseFeatureOID( prefix + numericPart );
		}
		else {
			long numericPart = id.hashCode();
			if ( numericPart < 0 ) {
				numericPart += Long.MAX_VALUE;
			}
			rtnOid = oidParser.parseFeatureOID( Gff3_constants.NAME_SPACE_PREFIX + ":" + numericPart );
			
		}
		if ( genomeVersionId == -1 ) {
			throw new IllegalStateException("Cannot get a valid genome version ID for OID creation.");
		}
		rtnOid.setGenomeVersionIdIfNull( genomeVersionId );
		return rtnOid;
	}

	private boolean isCuratedType( Gff3GenericModel model, String type ) {
		String modelType = model.getType();
		if ( modelType != null ) {
			modelType = modelType.trim();
		}
		else {
			System.out.println("ERROR: null model type for " + model.getId() );
		}
		String curatedTypeMapping = curatedGffToGBTypeMapping.get( modelType );
		return curatedTypeMapping != null  &&  curatedTypeMapping.equalsIgnoreCase( type );
	}
	
	private boolean hasNoChildren( ModelTreeNode node ) {
		return node.getChildren() == null || node.getChildren().size() == 0 ;
	}
	
	private boolean isNotEmpty( String s ) { return s != null && s.trim().length() > 0; }
}
