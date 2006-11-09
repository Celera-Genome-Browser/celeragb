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
package shared.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Title:        DeflineParser
 * Description:  Parses deflines, or first-lines of info from FASTA data.
 * Format Notes: Parser is meant to handle _certain_ FASTA headers.
 *               The format is generally:
 *
 *               CRA|nnnnnnnnn /name1=multi word value /name2=other value
 *
 *               I have been assured by a resident domain expert, that def
 *               lines appearing in _certain_ XML files (a primary source of them)
 *               can be internal or non-internal.  Therefore, the prefix will be
 *               used to test whether this parsing utility should be carried out.
 *
 * @author Les Foster
 * @version $Id$
 */

public class DeflineParser {

  //----------------------------------CONSTANTS
  // Fields known to be contained frequently in CRA deflines.  Others exist?
  public final static String DATASET_NAME = "dataset";
  public final static String VERSION_NAME = "version";
  public final static String DATE_NAME = "date";
  public final static String ALTERNATE_ID_NAME = "altid";
  public final static String ORGANISM_NAME = "org";
  public final static String TAXON_NAME = "taxon";
  public final static String DEF_NAME = "def";

  private final static String XML_GT = ">";
  private final static String INTERNAL_PREFIX = "CRA|";
  private final static String ALT_INTERNAL_PREFIX = XML_GT + INTERNAL_PREFIX;

  //----------------------------------MEMBER DATA
  private String mDefline = null;
  private Map mFields = null;
  private String mInternalId = null;

  //----------------------------------UNIT-TEST CODE
  public static void main(String[] lArgs) {
    DeflineParser lParser =
      new DeflineParser("CRA|90000614537170 /dataset=mightyMouse /version=1.5 /date=Sep 18 2000 /altid=GA_53728256 /org=Mus musculus /taxon=10090");
    System.out.println("For the defline "+lParser.toString());
    System.out.println("Found internal ID of "+lParser.getInternalId());
    System.out.println("Got value of "+lParser.get("dataset")+" for dataset");
    System.out.println("Got value of "+lParser.get("version")+" for version");
    System.out.println("Got value of "+lParser.get("org")+" for the organism");
  } // End main
  //----------------------------------CONSTRUCTOR
  /**
   * Constructor takes the defline for parsing/returning data.
   */
  public DeflineParser(String lDefline) {
    // Allow null as defline, just as convenience.
    if (lDefline == null)
      mDefline = "";

    mDefline = lDefline;
  } // End constructor

  //----------------------------------PUBLIC INTERFACE
  /**
   * Getter for /nameI=value, whose "name" is given.
   */
  public String get(String lName) {
    if (mFields == null)
      prepareFields();
    return (String)mFields.get(lName);
  } // End method

  /**
   * Returns piece of defline BEFORE first slash.
   */
  public String getInternalId() {
    if (mFields == null)
      prepareFields();
    return mInternalId;
  } // End method

  /** Altid field may have multiple values. */
  public Iterator getAllAltIds() {
    if (mFields == null)
      prepareFields();
    List lTempList = new ArrayList();
    String lAltIdCombined = (String)get(ALTERNATE_ID_NAME);
    if (lAltIdCombined == null)
      return lTempList.iterator();
    StringTokenizer lStk = new StringTokenizer(lAltIdCombined, "|");
    while (lStk.hasMoreTokens()) {
      lTempList.add(lStk.nextToken().trim());
    } // For all tokens.
    return lTempList.iterator();
  } // End method

  /** The Def ID is really the alternative accession. */
  public String getDefId() {
    if (mFields == null)
      prepareFields();
    if ((String)get(DEF_NAME) != null)
      return ((String)get(DEF_NAME)).trim();
    else
      return null;
  } // End method

  /**
   * Returns original defline.
   */
  public String toString() {
    return mDefline;
  } // End method

  /**
   * True if the defline was really of a _certain_ origin.
   */
  public boolean isInternalDefline() {
    if (mDefline == null)
      return false;
    return mDefline.startsWith(INTERNAL_PREFIX) || mDefline.startsWith(ALT_INTERNAL_PREFIX);
  } // End method

  //----------------------------------HELPER METHODS
  /** Where parsing gets done. */
  private void prepareFields() {

    mFields = new HashMap();

    if (! isInternalDefline())
      return;

    StringTokenizer lStk = new StringTokenizer(mDefline, "/");

    // BEFORE first slash is the internal ID.
    //
    if (lStk.hasMoreTokens()) {
      mInternalId = lStk.nextToken();
    } // First exists?

    // AFTER first slash is a series of name/value pairs.
    //
    while (lStk.hasMoreTokens()) {
      parseNameAndValue(lStk.nextToken());
    } // For all subsequent.
  } // End method

  /**
   * Parses open the /name=value part.
   */
  private void parseNameAndValue(String lDeflinePart) {
    if (lDeflinePart == null)
      return;
    StringTokenizer lStk = new StringTokenizer(lDeflinePart, "=");
    if (lStk.countTokens() != 2)
      return; // Badly-formed.
    String lName = lStk.nextToken();
    String lValue = lStk.nextToken();

    mFields.put(lName, lValue);
  } // End method

} // End class
