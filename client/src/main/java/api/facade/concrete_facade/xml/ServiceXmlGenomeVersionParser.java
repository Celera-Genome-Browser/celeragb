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
 * Created on May 7, 2003
 */
package api.facade.concrete_facade.xml;

import api.entity_model.model.genetics.GenomeVersion;
import api.entity_model.model.genetics.Species;
import api.facade.concrete_facade.xml.sax_support.CEFParseHelper;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.GenomeVersionInfo;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;
import api.stub.data.ReservedNameSpaceMapping;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import shared.util.GANumericConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Parser for high-level genomic objects encountered from "axis-sourcing" XML service
 * implementations.
 * @author FosterLL
 */
public class ServiceXmlGenomeVersionParser extends DefaultHandler {

	//-------------------------------CONSTANTS
    private final boolean DEBUG_CLASS = false;
	private final String GENOME_VERSIONS_ELEMENT = "genome_versions".intern(); 
    private final String GENOME_VERSION_ELEMENT = "genome_version".intern(); 
	private final String SPECIES_ELEMENT = "species".intern(); 
	private final String AXIS_ELEMENT = "axis".intern();
	private final String ASSEMBLY_VERSION_ATTRIBUTE = "assembly_version".intern(); 	 
	private final String LENGTH_ATTRIBUTE = "length".intern();
	private final String ID_ATTRIBUTE = "id".intern(); 
	private final String ORG_ATTRIBUTE = "org".intern(); 	 
	private final String CHROMOSOME_ATTRIBUTE = "chr".intern(); 	 
	private static final boolean READ_ONLY_STATUS = false;

	//-------------------------------MEMBER VARIABLES
	private List genomeVersions = new ArrayList();
	/*
	 * <genome_versions
  xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:noNamespaceSchemaLocation='genome_versions.xsd'
>
      <genome_version assembly_version="20030506171440">
            <species org="Homo sapiens">
                  <axis id="INTERNAL:55155" length="3700000" chr="1"/>
                  <axis id="INTERNAL:55156" length="2400000" chr="X"/>
                  <axis id="INTERNAL:55157" length="1000000" chr="3"/>
            </species>
      </genome_version>

	 */
	private String mAssemblyVersion;
	private String mOrg;
	private String mLocationalString;
	private int mGenomeVersionID;

	//-------------------------------UNIT TEST CODE
	public static void main(String[] args) {
	}

	//-------------------------------INTERFACE METHODS
	/**
	 * @param String targetUrl the location of a service, fully qualified with params.
	 * @return the full set of genome version objects constructed from response to 
	 *   URL above.
	 */
	public GenomeVersion[] parseForGenomeVersion(String targetUrl) {
		mLocationalString = targetUrl;
		try {
			genomeVersions.clear();
			XMLReader lReader = XMLReaderFactory.createXMLReader(CEFParseHelper.DEFAULT_PARSER_NAME);
			lReader.setContentHandler(this);
			lReader.parse(targetUrl);

		} catch (IOException ioe) {
			FacadeManager.handleException(ioe);
		} catch (SAXException saxe) {
			FacadeManager.handleException(saxe);
		} // End catch block for parse.

		return (GenomeVersion[])genomeVersions.toArray();
	} // End method

	//-------------------------------OVERRIDES TO DefaultHandler
    /**
     * Called by parser when element start is encountered.
	 * @param lUri - The Namespace URI, or the empty string if the element has
	 *  no Namespace URI or if Namespace processing is not being performed.
	 * @param lLocal - The local name (without prefix), or the empty string if
	 *  Namespace processing is not being performed.
	 * @param lName - The qualified XML 1.0 name (with prefix), or the empty
	 *  string if qualified names are not available.
	 * @param lAttrs - The collection of attributes of this element
     */
	public void startElement(String lUri, String lLocal, String lName, Attributes lAttrs) {
		try {
			if (lName == GENOME_VERSION_ELEMENT)
			    mAssemblyVersion = lAttrs.getValue(ASSEMBLY_VERSION_ATTRIBUTE);
			else if (lName == SPECIES_ELEMENT) {
	    		mOrg = lAttrs.getValue(ORG_ATTRIBUTE);
				OID genomeVersionOID = null;
				OID speciesOID = null;			
				long assemblyVersion;
				try {
					assemblyVersion = Long.parseLong(mAssemblyVersion);
				} catch (NumberFormatException nfe) {
					FacadeManager.handleException(nfe);
					assemblyVersion = -1;
				} // Not a true number.
	
				mGenomeVersionID = GenomeVersionInfo.calcGenomeVersionId( mOrg, mLocationalString, assemblyVersion );
				try {
				    genomeVersionOID = OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.API_GENERATED_NAMESPACE, mGenomeVersionID);
				    speciesOID = OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.API_GENERATED_NAMESPACE, mGenomeVersionID);

					Species species = new Species(speciesOID, mOrg);
					GenomeVersion gv = createGenomeVersion(species, genomeVersionOID);
                    genomeVersions.add(gv);
				}
				catch (Exception oidException) {
				    FacadeManager.handleException(new Exception("Failed to Generate OID for GenomeVersion "+oidException.getMessage()));
				} // End catch block for oid generation.

			} // Have enough info to create an assembly version.
			else if (lName == AXIS_ELEMENT) {
				String id = lAttrs.getValue(ID_ATTRIBUTE);
				String lengthString = lAttrs.getValue(LENGTH_ATTRIBUTE);
				String chromosomeName = lAttrs.getValue(CHROMOSOME_ATTRIBUTE);
				OID axisOID = null;
				OID chromosomeOID = null; 
				int axisLength;
				try {
					axisLength = Integer.parseInt(lengthString);
				} catch (NumberFormatException nfe) {
					FacadeManager.handleException(nfe);
					axisLength = 1; 
				} // End catch block for length

				try {
                    // Need to figure out if chromo has been dealt with before...
                    // ....
					chromosomeOID = OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.API_GENERATED_NAMESPACE, mGenomeVersionID);
					axisOID = parseOIDForAxis(id);

// At this point, COULD produce the chromosome and the axis.
				} catch (Exception oidEx) {
					FacadeManager.handleException(oidEx);
				} // Could not make the OID

			} // Have axis OID identifier, axis length, and the chromosome.

		} catch (Exception ex) {
			// NOTE: must catch here, otherwise, gets rethrown as SAX excep.
			FacadeManager.handleException(ex);
		} // End catch block.

	} // End method: startElement

	/**
	 * Called by parser when element end tag was encountered.
	 *
	 * @param lUri - The Namespace URI, or the empty string if the element has
	 *  no Namespace URI or if Namespace processing is not being performed.
	 * @param lLocal - The local name (without prefix), or the empty string if
	 *  Namespace processing is not being performed.
	 * @param lName - The qualified XML 1.0 name (with prefix), or the empty
	 *  string if qualified names are not available.
	 */
	public void endElement(String lUri, String lLocal, String lName) {

		try {
			if (DEBUG_CLASS)
				System.err.println("</"+lName+">");


		} catch (Exception ex) {
			// NOTE: must catch here, otherwise, gets rethrown as SAX excep.
			FacadeManager.handleException(ex);
		} // End catch block.

	} // End method: endElement

	//-------------------------------FACTORY METHODS TO CONSTRUCT GENOMIC ENTITIES
	/** Build genome version object from collected data. */
	private GenomeVersion createGenomeVersion(Species latestSpecies, OID genomeVersionOID) {
	  GenomeVersion latestGenomeVersion = null;
	  GenomeVersionInfo genomeVersionInfo = createGenomeVersionInfo();
	  try {
		latestGenomeVersion = new GenomeVersion(
		  genomeVersionOID,
		  latestSpecies,       // Important: must match one used in making genome vers. OID
		  genomeVersionInfo,
		  mLocationalString,
		  READ_ONLY_STATUS,
		  null
		);

	  }
	  catch (Exception ex) {
		FacadeManager.handleException(new IllegalStateException("Failed to Create Genome Version for "+mLocationalString));
	  } // End catch block for genome version generation.
	  return latestGenomeVersion;
	} // End method

	/** Build genome version object from collected data. */
	private GenomeVersionInfo createGenomeVersionInfo() {
	  GenomeVersionInfo genomeVersionInfo = null;
	  try {
	  	long assemblyVersionLong = Long.parseLong(mAssemblyVersion);
		int genomeVersionId = GenomeVersionInfo.calcGenomeVersionId( mOrg, mLocationalString, assemblyVersionLong );
		genomeVersionInfo = new GenomeVersionInfo(
		  genomeVersionId,  // Genome version identifier
		  mOrg,             // Species
		  assemblyVersionLong,
		                    // Assy Ver: must match that used by OID generator above!
		  mLocationalString,// Datasource
		  GenomeVersionInfo.FILE_DATA_SOURCE  // Datasource type
		);

	  }
	  catch (Exception ex) {
		FacadeManager.handleException(new IllegalStateException("Failed to Create Genome Version for "+mLocationalString));
	  } // End catch block for genome version generation.
	  return genomeVersionInfo;
	} // End method


    /* Special helpers for OID parsing. */

	/**
	 * Builds an OID for an axis.
	 *
	 * @param String idstr of format "PREFIX:dddddddddd"
	 *   where the d's represent a decimal (long) number.
	 * @return OID an OID in either the specified or an unknown namespace.
	 */
	public OID parseOIDForAxis(String idstr) {

	  String[] strArrayForId = null;
	  OID returnOID = null;

	  char startchr = idstr.charAt(0);

	  // Two possible formats: id="CCCC:ddddddddddd" or
	  //    id="ddddddd".  First contains a namespace prefix,
	  //    and the second is just the digits.
	  //
	  if(Character.isLetter(startchr)){
		strArrayForId=processIdStringForPrefix(idstr);
		String oid = strArrayForId[1];
		String namespacePrefix =
		   ReservedNameSpaceMapping.translateToReservedNameSpace(strArrayForId[0]);
		returnOID = new OID(namespacePrefix, oid, mGenomeVersionID);

		// Need a running counter for certain kinds of OIDs.
		if (returnOID.isScratchOID()) {
		  // Test this read OID against the current highest OID, and make it the
		  // new highest if it is higher.
		  try {
			OIDGenerator.getOIDGenerator().setInitialValueForNameSpace
				  (OID.SCRATCH_NAMESPACE, Long.toString(1 + returnOID.getIdentifier()));
		  } // End try block
		  catch (IllegalArgumentException iae) {
			// Ignoring here: we only wish to seed the generator with highest known value.
		  } // End catch block for seed.
		} // Found another scratch.

	  } // Proper alphabetic prefix.
	  //else if (Character.isDigit(startchr)) {
		//oidlong = Long.parseLong(idstr);
		//returnOID = new OID(OID.UNKNOWN_NAMESPACE,Long.toString(oidlong), getGenomeVersionId());
	  //} // Found digit as first character.
	  else {
		if (idstr.indexOf(':') >= 0) {
		  FacadeManager.handleException( new IllegalArgumentException( "This application is expecting a namespace prefix beginning with an alphabetic character in its XML IDs.\nYou specified '"
											  +idstr+"'."));
		} // Prefix invalid
		else {
		  // NOTE: as of 5/7/2001, we found that the EJB/db are getting confused
		  //       about non-internal OIDs.  TO fix this, we are precluding non-
		  //       internal database OIDs from being sent there.  Unfortunately,
		  //       this also requires no-prefix OIDs no longer be accepted.
		  FacadeManager.handleException( new IllegalArgumentException( "This application is expecting a namespace prefix in its XML IDs.\nYou specified '"
											  +idstr+"'.\nIf this is an internal database ID, please change that to 'INTERNAL:"
											  +idstr+"'.\nIf not, prefix it with a namespace of your own."
											  ));
		} // No prefix at all.
		// oidlong=Long.parseLong(idstr);
		// returnOID=new OID(OID.UNKNOWN_NAMESPACE,Long.toString(oidlong));
		// throw new IllegalArgumentException("Invalid OID "+idstr+": expecting a letter as first character.  Example:");
	  } // Found unexpected character as first character.

	  return returnOID;
	} // End method: parseOIDGeneric

	/**
	 * Builds an OID with no special restrictions or translations,
	 * and can translate a GA number into an OID.
	 *
	 * @param String idstr of format "PREFIX:dddddddddd"
	 *   where the d's represent a decimal (long) number.
	 * @return OID an OID in either the specified or an unknown namespace.
	 */
	protected OID parseOIDorGA(String idstr) {

	  String[] strArrayForId = null;
	  long oidLong = 0L;
	  OID returnOID = null;

	  char startchr = idstr.charAt(0);

	  // Two possible formats: id="CCCC:ddddddddddd" or
	  //    id="ddddddd".  First contains a namespace prefix,
	  //    and the second is just the digits.
	  //
	  if(Character.isLetter(startchr)){
		strArrayForId=processIdStringForPrefix(idstr);

		if (strArrayForId[1].startsWith(GANumericConverter.GA_PREFIX))
		  oidLong = GANumericConverter.getConverter().getOIDValueForGAName(strArrayForId[1]);
		else
		  oidLong = Long.parseLong(strArrayForId[1]);

		String namespacePrefix =
		   ReservedNameSpaceMapping.translateToReservedNameSpace(strArrayForId[0]);

		returnOID = new OID(namespacePrefix,Long.toString(oidLong), mGenomeVersionID);

		// Need a running counter for certain kinds of OIDs.
		if (returnOID.isScratchOID()) {
		  // Test this read OID against the current highest OID, and make it the
		  // new highest if it is higher.
		  try {
			OIDGenerator.getOIDGenerator().setInitialValueForNameSpace(OID.SCRATCH_NAMESPACE,Long.toString(++oidLong));
		  } // End try block.
		  catch (IllegalArgumentException iae) {
			// Ignoring here: we only wish to seed the generator with highest known value.
		  } // End catch block for seed.
		} // Found another scratch.

	  } // Proper alphabetic prefix.
	  else if (Character.isDigit(startchr)) {
		oidLong = Long.parseLong(idstr);
		returnOID = new OID(OID.UNKNOWN_NAMESPACE,Long.toString(oidLong), mGenomeVersionID);
	  } // Found digit as first character.
	  else {
		 // System.err.println("ERROR: unexpected initial character in input OID "+idstr);
		 throw new IllegalArgumentException("Invalid OID "+idstr+": this protocol expects a letter as first character");
	  } // Found unexpected character as first character.
	  return returnOID;
	} // End method: parseOIDorGA

	/**
	 * Primarily a 'facility' for use of a parseXxxxOIDTemplateMethod,
	 * this method breaks up an id= attribute value into the
	 * pieces needed to build an Object ID.  Should remain protected
	 * visibility so that implementing subclasses of this abstract
	 * class, can call this method!
	 *
	 * @param String idstr The id= attribute value
	 * @return String[] first member is namespace prefix,
	 *   the second member is the set of digits, which should
	 *   make the OID unique in the name space.
	 */
	protected String[] processIdStringForPrefix(String idstr){
	  StringTokenizer s=new StringTokenizer(idstr,":");
	  String[] nameSpaceIdarray=new String[2];
	  nameSpaceIdarray[0]=s.nextToken();
	  nameSpaceIdarray[1]=s.nextToken();
	  return nameSpaceIdarray;

	} // End method: processIdStringForPrefix

} // End class


