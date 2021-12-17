/**
 *   Copyright Leslie L. Foster, 2011.
 *
 * This is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the 
 * Free Software Foundation; version 2.1 of the License.
 * 
 * This software is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this software; if not, write to the Free Software Foundation, Inc.
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 *
 */
package oss.model.builder.gff3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads the input file, one line at a time, and returns the next model
 * it created from it, in "iterator-like" fashion.
 * 
 * @author Leslie L Foster
 */
public class Gff3LineReader {
	private static final String ID_ATTRIB = "ID";
	private static final String NAME_ATTRIB = "Name";
	private static final String ALIAS_ATTRIB = "Alias";
	private static final String PARENT_ATTRIB = "Parent";
	private static final String TARGET_ATTRIB = "Target";
	private static final String GAP_ATTRIB = "Gap";
	private static final String DERIVEMENT_ATTRIB = "Derives_from";
	private static final String NOTE_ATTRIB = "Note";
	private static final String DBXREF_ATTRIB = "Dbxref";
	private static final String DBXREF_ATTRIB_ALT = "db_xref";  // Deviates from standard; used by NCBI.
	private static final String ONTOLOGY_ATTRIB = "Ontology_term";
	
	private File gffFile;
	private String sourceName;
	private BufferedReader rdr;
	private int lineNo;
	
	/** Configure with file-to-handle on construction. */
	public Gff3LineReader( File gffFile ) throws Gff3LineReaderException {
		this.gffFile = gffFile;
		this.sourceName = gffFile.toString();
	}
	
	public Gff3LineReader( String sourceName, InputStream is ) throws Gff3LineReaderException {
		try {
			this.sourceName = sourceName;
			
			rdr = new BufferedReader( new InputStreamReader( is ) );

		} catch ( Exception ex ) {
			throw new Gff3LineReaderException( ex );
		}
	}
	
	/** Call this when closed. */
	public void close() {
		try {
			if ( rdr != null )
				rdr.close();
			rdr = null;
		} catch ( IOException ex ) {
			// nothing.
		}
		
	}
	
	/** Tell what file this reader is working on. */
	public String getSourceName() { return sourceName; }

	/** One line of the input is worth one feature's model, or null if e-o-file. */
	public Gff3GenericModel nextLine() throws Gff3LineReaderException {
		lineNo  ++;   // For error messages.  First line is line 1.
		
		Gff3GenericModel rtnVal = null;
		prepareReader();  // Lazily-open.
		try {
			boolean done = false;
			String[] fields = null;
			do {
				String inputLine = rdr.readLine();
				if ( inputLine == null ) {
					done = true;
					rtnVal = null;
				}
				else if ( inputLine.startsWith( "#" )  ||  inputLine.startsWith( ">" ) ) {
					done = false;

				}
				else {
					// Parse up the columns.
					fields = inputLine.split( "\t" );

					if ( fields.length <= 1 ) {
						// Only one field implies this could be FASTA content to be skipped.  (sigh!)
						done = false;
					}
					else if ( fields.length < 8 ) {
						// 2-8 implies good intentions/bad results.
						throw new Gff3LineReaderException( "Line " + lineNo + " has only " + fields.length + " fields.  8-9 expected." );
					}
					else {
						// Next to parse the line as needed.
						rtnVal = convert( fields );
						done = true;
					}

				}

			} while ( ! done );

		} catch ( Exception ex ) {
			throw new Gff3LineReaderException( ex );
		}
		return rtnVal;
	}

	//----------------------------HELPER METHODS
	/** Ensure the reader has been opened. */
	private void prepareReader() throws Gff3LineReaderException {
		if ( rdr == null  &&  gffFile != null ) {
			try {
				rdr = new BufferedReader(
						new FileReader( gffFile )
				);
			} catch ( Exception ex ) {
				throw new Gff3LineReaderException( ex );
			}
		}
	}

	/**
	 * Read the next line into a model.  Here's the format breakdown from 
	 *   http://www.eu-sol.net/science/bioinformatics/standards-documents/gff3-format-description
	 *   
		Column 1: "seqid"
		 The ID of the landmark used to establish the coordinate system for the current feature. IDs may contain any characters, but must
		 escape any characters not in the set [a-zA-Z0-9.:^*$@!+_?-|]. In particular, IDs may not contain unescaped whitespace and must 
		 not begin with an unescaped ">".
		 To escape a character in this, or any of the other GFF3 fields, replace it with the percent sign followed by its hexadecimal 
		 representation. For example, ">" becomes "%E3". See URL Encoding (or: 'What are those "%20" codes in URLs?') for details.
		
		Column 2: "source"
		 The source is a free text qualifier intended to describe the algorithm or operating procedure that generated this feature. Typically 
		 this is the name of a piece of software, such as "Genescan" or a database name, such as "Genbank." In effect, the source is used to 
		 extend the feature ontology by adding a qualifier to the type creating a new composite type that is a subclass of the type in the type 
		 column. It is not necessary to specify a source. If there is no source, put a "." (a period) in this field.
		
		Column 3: "type"
		 The type of the feature (previously called the "method"). This is constrained to be either: (a) a term from the "lite" sequence ontology,
		 SOFA; or (b) a SOFA accession number. The latter alternative is distinguished using the syntax SO:000000. This field is required.
		
		Columns 4 & 5: "start" and "end"
		 The start and end of the feature, in 1-based integer coordinates, relative to the landmark given in column 1. Start is always less than 
		 or equal to end.
		 For zero-length features, such as insertion sites, start equals end and the implied site is to the right of the indicated base in the 
		 direction of the landmark. These fields are required.
		
		Column 6: "score"
		 The score of the feature, a floating point number. As in earlier versions of the format, the semantics of the score are ill-defined. It 
		 is strongly recommended that E-values be used for sequence similarity features, and that P-values be used for ab initio gene prediction 
		 features. If there is no score, put a "." (a period) in this field.
		
		Column 7: "strand"
		 The strand of the feature. + for positive strand (relative to the landmark), - for minus strand, and . for features that are not stranded. 
		 In addition, ? can be used for features whose strandedness is relevant, but unknown.
		
		Column 8: "phase"
		 For features of type "CDS", the phase indicates where the feature begins with reference to the reading frame. The phase is one of the 
		 integers 0, 1, or 2, indicating the number of bases that should be removed from the beginning of this feature to reach the first base 
		 of the next codon. In other words, a phase of "0" indicates that the next codon begins at the first base of the region described by the 
		 current line, a phase of "1" indicates that the next codon begins at the second base of this region, and a phase of "2" indicates that 
		 the codon begins at the third base of this region. This is NOT to be confused with the frame, which is simply start modulo 3. If there 
		 is no phase, put a "." (a period) in this field.
		 For forward strand features, phase is counted from the start field. For reverse strand features, phase is counted from the end field.
		 The phase is required for all CDS features.
		
		Column 9: "attributes"
		 A list of feature attributes in the format tag=value. Multiple tag=value pairs are separated by semicolons. URL escaping rules are used 
		 for tags or values containing the following characters: ",=;". Spaces are allowed in this field, but tabs must be replaced with the %09 
		 URL escape. This field is not required.

		Column 9 Tags
		Column 9 tags have predefined meanings:
		
		ID: Indicates the unique identifier of the feature. IDs must be unique within the scope of the GFF
		 file.
		
		Name: Display name for the feature. This is the name to be displayed to the user. Unlike IDs, there
		 is no requirement that the Name be unique within the file.
		
		Alias: A secondary name for the feature. It is suggested that this tag be used whenever a secondary
		 identifier for the feature is needed, such as locus names and accession numbers. Unlike ID, there is
		 no requirement that Alias be unique within the file.
		
		Parent: Indicates the parent of the feature. A parent ID can be used to group exons into transcripts,
		 transcripts into genes, and so forth. A feature may have multiple parents. Parent can *only* be used
		  to indicate a partof relationship.
		
		Target: Indicates the target of a nucleotide-to-nucleotide or protein-to-nucleotide alignment. The 
		format of the value is "target_id start end [strand]", where strand is optional and may be "+" or
		 "-". If the target_id contains spaces, they must be escaped as hex escape %20.
		
		Gap: The alignment of the feature to the target if the two are not collinear (e.g. contain gaps).
		 The alignment format is taken from the CIGAR format described in the Exonerate documentation.
		 http://cvsweb.sanger.ac.uk/cgi-bin/cvsweb.cgi/exonerate?cvsroot=Ensembl). See the GFF3 specification 
		 for more information.
		
		Derives_from: Used to disambiguate the relationship between one feature and another when the
		 relationship is a temporal one rather than a purely structural "part of" one. This is needed for
		 polycistronic genes. See the GFF3 specification for more information.
		
		Note: A free text note.
		
		Dbxref: A database cross reference. See the GFF3 specification for more information.
		
		Ontology_term: A cross reference to an ontology term. See the GFF3 specification for more information. 
	 */
	private Gff3GenericModel convert( String[] fields ) throws Gff3LineReaderException {
		Gff3GenericModel model = new Gff3GenericModel();
		try {
			model.setLandmarkId( unescapeUrl( fields[ 0 ] ) );

			model.setSource( unescapeUrl( fields[ 1 ] ) );
			String modelType = fields[ 2 ];
			if ( modelType == null ) {
				modelType = "Unknown";
System.out.println("Model at line " + lineNo + " has odd model type /" + modelType + "/");
			}
			model.setType( modelType );   //todo compare with the SOFA.
			
			String startStr = fields[ 3 ];
			String endStr = fields[ 4 ];
			try {
				model.setStart( Integer.parseInt( startStr ) );
				model.setEnd( Integer.parseInt( endStr ) );
			} catch ( NumberFormatException nfe ) {
				System.out.println(
						"WARNING: start or end not an integer. Forcing to 0,0. Start = " + startStr + ", End = " + endStr + " see line " + lineNo );
				model.setStart( 0 );
				model.setEnd( 0 );
			}
			
			String score = fields[ 5 ];
			if ( score.trim().length() > 0  &&  score.charAt( 0 ) != '.' )
				model.setScore( Double.parseDouble( score ) );
			else
				model.setScore( 0.0 );
			
			model.setStrand( decodeStrand( fields[ 6 ] ) );
			
			model.setPhase( interpretPhase( fields[ 7 ] ) );

			// Maintenance note: of the attributes which may be used, several are "known" to the specification, and have
			// special purposes.  Of these, they may not have the same ,-to-multiple cardinality as user attributes.
			// Also, some may require different treatment of URL-escape (%-hex-value) characters.  These exceptions
			// have been applied where know.  However, if more cases should arise, follow the patterns used below
			// to correct them.
			if ( fields.length >= 9 ) {
				Map<String,String[]> attributes = parseAttributes( fields[ 8  ] );
				model.setAttributes( attributes );

				model.setDerivesFrom( getFirstAttribOrNull( attributes, DERIVEMENT_ATTRIB ));
				model.setTargetOfAlignment( getFirstAttribOrNull( attributes, TARGET_ATTRIB) );
				model.setGap( getFirstAttribOrNull( attributes, GAP_ATTRIB) );
				model.setId( getFirstAttribOrNull( attributes, ID_ATTRIB) );
				model.setName( unescapeUrl( getFirstAttribOrNull( attributes, NAME_ATTRIB) ) );
				model.setNonUniqueAlias( getFirstAttribOrNull( attributes, ALIAS_ATTRIB) );
				model.setNote( getFirstAttribOrNull( attributes, NOTE_ATTRIB) );
				model.setOntologyTerm( getFirstAttribOrNull( attributes, ONTOLOGY_ATTRIB) );

				// Known multiple cardinality.
				model.setParent( attributes.get( PARENT_ATTRIB ) );
				model.setDbxref( attributes.get( DBXREF_ATTRIB ) );
				if ( model.getDbxref() == null ) {
					model.setDbxref( attributes.get( DBXREF_ATTRIB.toLowerCase() )); // settling for what Saccharomyces cerevisiae S288C genome has.
					if ( model.getDbxref() == null ) {
						model.setDbxref( attributes.get( DBXREF_ATTRIB_ALT ) );  // settling for NCBI's version.
					}
				}
				
			}
		} catch ( Exception ex ) {
			throw new Gff3LineReaderException( ex );
		}
		
		return model;
	}
	
	private String getFirstAttribOrNull( Map<String,String[]> attributes, String attribName ) {
		String[] values = attributes.get( attribName );
		if ( values != null  &&  values.length > 0 ) {
			return values[ 0 ];
		}
		else {
			return null;
		}
	}

	/** Extract the attributes, treating special ones differently. */
	private Map<String,String[]> parseAttributes( String combinedAttributes ) {
		Map<String,String[]> attributes = new HashMap<String,String[]>();
		//A list of feature attributes in the format tag=value. Multiple tag=value pairs are separated by semicolons. URL escaping rules are used 
		// for tags or values containing the following characters: ",=;". Spaces are allowed in this field, but tabs must be replaced with the %09 
		// URL escape. This field is not required.
		String trimmedAttributes = combinedAttributes.trim();
		if ( trimmedAttributes.length() > 0 ) {
			String[] settings = trimmedAttributes.split( ";" );
			for ( String setting: settings ) {
				String[] nameValuePair = setting.split( "=" );
				String key = unescapeUrl( nameValuePair[ 0 ] );

				// For NOTE, which is free text, cutting up by comma makes no sense.
				if ( key.equals( NOTE_ATTRIB ) ) {

					attributes.put( key, new String[] { unescapeUrl( nameValuePair[ 1 ] ) } );

				}
				else {
					if ( nameValuePair.length < 2 ) {
						attributes.put( key, new String[ 0 ] );
						//System.out.println("WARNING: malformed attribute setting /" + setting + "/");
					}
					else {
						String[] values = nameValuePair[ 1 ].split( "," );
						for ( int i = 0; i < values.length; i++ ) {
							// NOTE: for target attribute, need the URL-escape characters in place.
							if ( ! key.equals( TARGET_ATTRIB ) ) {
								values[ i ] = unescapeUrl( values[ i ] );
							}
						}
						
						attributes.put( key, values );
						
					}

				}
				
			}
		}
		return attributes;
	}
	
	/** Put strand into its standard enum form. */
	private Gff3GenericModel.Strand decodeStrand( String strandStr ) {
		if ( strandStr == null  ||  strandStr.length() == 0 ) {
			return Gff3GenericModel.Strand.misSpecified;
		}
		switch ( strandStr.charAt( 0 ) ) {
		    case '+' : return Gff3GenericModel.Strand.positive;
		    case '-' : return Gff3GenericModel.Strand.negative;
		    case '.' : return Gff3GenericModel.Strand.none;
		    case '?' : return Gff3GenericModel.Strand.unknown;
		    default  : return Gff3GenericModel.Strand.misSpecified;
		}
	}
	
	/** Decide what kind of phase we deal with. */
	private Integer interpretPhase( String phaseStr ) {
		Integer rtnVal = null;
		if ( phaseStr != null  &&  phaseStr.length() > 0 ) {
			if ( phaseStr.trim().length() > 0 ) {
				try {
					int phaseInt = Integer.parseInt( phaseStr );
					if ( phaseInt >= 0  &&  phaseInt <= 3 ) {
						rtnVal = phaseInt;
					}
					else {
						rtnVal = null;
					}
				} catch ( NumberFormatException nfe ) {
					rtnVal = null;
				}
			}
			
		}
		
		return rtnVal;
	}
	
	/** Some values can have non-printable characters (or in-value delimiters) which must be escaped.  This method un-translates them. */
	private String unescapeUrl( String value ) {
		// Here, look for URL-like "escape sequences", starting with percent-sign, and followed by two hex characters.
		if ( value != null  &&  value.contains( "%" ) ) {
			int nextPos = 0;
			int pos = 0;
			StringBuilder builder = new StringBuilder();
			while ( -1 != ( pos = value.indexOf( '%', nextPos ) ) ) {
				builder.append( value.substring( nextPos,  pos ) );    // Take care of part before match.
				if ( pos + 2 > value.length() - 1 ) {
					System.out.println(
					   "WARNING: value " + value + " at line " + lineNo + 
					   " has URL-escape that extends past end-of-string.  Truncating to " +
					   builder.toString() );
					return builder.toString();
				}
				
				String hexPart = value.substring( pos + 1, pos + 3 );
				builder.append( (char)Integer.parseInt( hexPart, 16 ) );
				
				nextPos = pos + 3;
			}
			
			builder.append( value.substring( nextPos ) );              // Take care of part after last match.
			
			value = builder.toString();
		}
		return value;
	}
	
	/** Exception for any failures in this line read. */
	static public class Gff3LineReaderException extends Exception {
		public Gff3LineReaderException( Exception ex) { super( ex ); }
		public Gff3LineReaderException( String message ) { super( message ); }
	}
}

