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
/**
 * Title:        Genome Version Parser
 * Description:  SAX parser to capture info from header line of GAME format files.
 * @author Les Foster
 * @version $Id$
 */
package api.facade.concrete_facade.xml;

import api.entity_model.model.genetics.GenomeVersion;
import api.entity_model.model.genetics.Species;
import api.facade.concrete_facade.xml.sax_support.CEFParseHelper;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.GenomeVersionInfo;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;

/** This SAX parser will read the input, and only capture the first line. */
public class GenomeVersionParser extends DefaultHandler {

  //------------------------------------------CONSTANTS
  private static final boolean READ_ONLY_STATUS = false;

  //------------------------------------------MEMBER VARIABLES
  private XMLReader reader = null;
  private String SUCCESS_MESSAGE = "SUCCESS!";
  private String taxonString;
  private long assemblyVersion;
  private OID latestSpeciesOID;
  private OID genomeVersionOID;
  private String fileName;
  private int versionCount = 1;
  private String justFileName;
  private int genomeVersionId = -1;
  private GenomeVersionSpace genomeVersionSpace;

  /** Simplest constructor.  Assumes no datasource name or space. */
  public GenomeVersionParser() {
    this(null, null);
  } // End constructor

  /** Builds a parser for use across multple files. */
  public GenomeVersionParser(GenomeVersionSpace genomeVersionSpace, String datasourceName) {
    try {
      // Setup the parse.
      this.genomeVersionSpace = genomeVersionSpace;
      reader = XMLReaderFactory.createXMLReader(CEFParseHelper.DEFAULT_PARSER_NAME);
      reader.setContentHandler(this);
    }
    catch (Exception parserBuildException) {
      FacadeManager.handleException(parserBuildException);
    } // End catch block for parse.
  } // End constructor.

  /** Scans the input file and finds its genome version data. */
  public GenomeVersion parseForGenomeVersion(String filename) {

    openAndParse(filename);
    Species latestSpecies = new Species(latestSpeciesOID,taxonString);
    if (genomeVersionSpace != null)
      genomeVersionSpace.registerSpecies(fileName, latestSpecies);

    return createGenomeVersion(latestSpecies);
  } // End method: parseForGenomeVersion

  /** Scans the input file and finds its genome version info data. */
  public GenomeVersionInfo parseForGenomeVersionInfo(String filename) {

    openAndParse(filename);
    return createGenomeVersionInfo();

  } // End method: parseForGenomeVersion

  /** Scans the input file, finds header and keeps version id. */
  public int parseForGenomeVersionId(String filename) {

    openAndParse(filename);
    return genomeVersionId;

  } // End method: parseForGenomeVersionId

  /**
   * Called by parser when element start tag encountered.
   *
   * @param lUri - The Namespace URI, or the empty string if the element
   *  has no Namespace URI or if Namespace processing is not being performed.
   * @param lLocal - The local name (without prefix), or the empty string if
   *  Namespace processing is not being performed.
   * @param lName - The qualified name (with prefix), or the empty string if
   *  qualified names are not available.
   * @param Attributes lAttrs the collection of tag attributes.
   */
  public void startElement(String uri, String local, String elementName, Attributes attributes) {

    if (elementName.equals(CEFParseHelper.ROOT_ELEMENT)) {

      // Build genome version from attributes.
      String assemblyVersionString = attributes.getValue("assembly_version");
      try {
        if ((assemblyVersionString == null) || (assemblyVersionString.length() == 0))
          assemblyVersion = versionCount++;
        else
          assemblyVersion = Long.parseLong(assemblyVersionString);
      }
      catch (NumberFormatException nfe) {
        FacadeManager.handleException(new IllegalArgumentException("Invalid assembly version in "+fileName));
      } // End catch block for version parsing

      taxonString = standardizeTaxon(attributes.getValue("taxon"));
      if (taxonString == null)
        taxonString = CEFParseHelper.DEFAULT_SPECIES_NAME;


      genomeVersionId = GenomeVersionInfo.calcGenomeVersionId( taxonString, fileName, assemblyVersion );

      try {
        genomeVersionOID = OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.API_GENERATED_NAMESPACE, genomeVersionId);
      }
      catch (Exception oidException) {
        FacadeManager.handleException(new Exception("Failed to Generate OID for GenomeVersion "+oidException.getMessage()));
      } // End catch block for oid generation.

      try {
        latestSpeciesOID = OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.API_GENERATED_NAMESPACE,
          genomeVersionId);
      }
      catch (Exception oidException) {
        FacadeManager.handleException(new Exception("Failed to Generate OID for Species "+oidException.getMessage()));
      } // End catch block for oid generation.

      try {
        justFileName = new File(fileName).getName();
      }
      catch (Exception filenameException) {
        FacadeManager.handleException(filenameException);
        justFileName = fileName;
      } // End catch block for filename grab.

      // Stop parsing.
      //   currently, element of interest is first element,
      //   and we do not wish to waste time scanning the
      //   entire document.  We DO, however, wish to take
      //   advantage of SAX parsing to cut down on grunge string
      //   parsing, and to avoid having to maintain for unforeseen
      //   XML standard changes.
      throw new RuntimeException(SUCCESS_MESSAGE);

    } // Found game element.

  } // End method: startElement

  /** Build genome version object from collected data. */
  private GenomeVersion createGenomeVersion(Species latestSpecies) {
    GenomeVersion latestGenomeVersion = null;
    GenomeVersionInfo genomeVersionInfo = createGenomeVersionInfo();
    try {
      latestGenomeVersion = new GenomeVersion(
        genomeVersionOID,
        latestSpecies,       // Important: must match one used in making genome vers. OID
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

  /** Build genome version object from collected data. */
  private GenomeVersionInfo createGenomeVersionInfo() {
    GenomeVersionInfo genomeVersionInfo = null;
    try {
      genomeVersionId = GenomeVersionInfo.calcGenomeVersionId( taxonString, fileName, assemblyVersion );
      genomeVersionInfo = new GenomeVersionInfo(
        genomeVersionId,  // Genome version identifier
        taxonString,      // Species
        assemblyVersion,  // Assy Ver: must match that used by OID generator above!
        fileName,         // Datasource
        GenomeVersionInfo.FILE_DATA_SOURCE  // Datasource type
      );

    }
    catch (Exception ex) {
      FacadeManager.handleException(new IllegalStateException("Failed to Create Genome Version for "+fileName));
    } // End catch block for genome version generation.
    return genomeVersionInfo;
  } // End method

  /** Given a filename, open it and parse it. */
  private void openAndParse(String filename) {
    try {
      this.fileName = filename;
      reader.parse("file:"+filename);
    }
    catch (Exception ex) {
      if ((ex.getMessage() == null) ||
        (! ex.getMessage().equals(SUCCESS_MESSAGE)))
        FacadeManager.handleException(new Exception(ex.getMessage()+" for file "+filename));
    } // End catch block for scan.
  } // End method

  /**
   * Given a taxon string which may be in any number of different case
   * combinations, and have trailing spaces, etc., convert it to the form
   *  Firstword secondword thirdword...
   */
  private String standardizeTaxon(String taxonString) {
    if (taxonString == null || taxonString.length() == 0)
      return null;

    StringBuffer collector = new StringBuffer(taxonString.length());
    collector.append(Character.toUpperCase(taxonString.charAt(0)));
    collector.append(taxonString.substring(1).trim().toLowerCase());

    return collector.toString();
  } // End method: standardizeTaxon

} // End class: GenomeVersionParser
