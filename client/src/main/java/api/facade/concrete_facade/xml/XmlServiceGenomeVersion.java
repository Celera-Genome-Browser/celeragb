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
package api.facade.concrete_facade.xml;

import api.facade.abstract_facade.fundtype.NavigationConstants;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.InvalidPropertyFormat;
import api.stub.data.NavigationNode;
import api.stub.data.NavigationPath;
import api.stub.data.OID;
import api.stub.geometry.Range;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Title:        XmlServiceGenomeVersion
 * Description:  XML Service version of the genome version.  For searches.
 * @author Les Foster
 * @version $Id$
 */
public class XmlServiceGenomeVersion extends XmlGenomeVersion {

  //----------------------------------CONSTRUCTORS
  public XmlServiceGenomeVersion() {
  } // End constructor

  //----------------------------------OVERRIDES
  /**
   * Returns the set of paths which can be used to retrieve/focus on
   * locations matching the criteria given.
   * @param targetType the type of object for which to find locator.
   * @param target the value to match on the type of lookup given.
   */
  public NavigationPath[] getNavigationPath(OID speciesOid,
     String targetType,
     String target)
    throws InvalidPropertyFormat {

    // Must decode the input.
    int targetTypeNumber = NavigationConstants.getNumberFromShortName(targetType);

    List<NavigationPath> pathList = new ArrayList<NavigationPath>();
    if ( ( targetTypeNumber == NavigationConstants.STS_NAME_INDEX )             ||
         ( targetTypeNumber == NavigationConstants.HIT_ALIGN_ACCESSION_INDEX )  ||
         ( targetTypeNumber == NavigationConstants.REG_REGION_ACCESSION_INDEX ) ||
         ( targetTypeNumber == NavigationConstants.POLY_ACCESSION_INDEX )       ||
         ( targetTypeNumber == NavigationConstants.SUBSEQ_OID_INDEX )           ||
         ( targetTypeNumber == NavigationConstants.GENE_ONTOLOGY_NAME_INDEX )   ||
         ( targetTypeNumber == NavigationConstants.UNKNOWN_OID_INDEX )          ||
         ( targetTypeNumber == NavigationConstants.FEATURE_OID_INDEX )          ||
         ( targetTypeNumber == NavigationConstants.PROTEIN_ACCESSION_INDEX )    ||
         ( targetTypeNumber == NavigationConstants.TRANSCRIPT_ACCESSION_INDEX ) ||
         ( targetTypeNumber == NavigationConstants.GENE_ACCESSION_INDEX ) ) {

      try {
        pathList = searchFeature( targetTypeNumber, target, speciesOid );
      } // End try
      catch (Exception ex) {
          // DO nothing
      }

    } // Requires feature search.

    // Empty return data->no data.
    if (pathList.size() == 0)
      return new NavigationPath[0];

    NavigationPath[] returnPath = new NavigationPath[pathList.size()];
    pathList.toArray(returnPath);

    return returnPath;
  } // End method: getNavigationPath

  //----------------------------------HELPERS
  /**
   * Method build a path for features, found in loaders.
   */
  private List<NavigationPath> searchFeature(int targetTypeNumber, String target, OID speciesOid) {
    List<NavigationPath> returnList = new ArrayList<NavigationPath>();

    XmlLoader loader;
    String[] foundItems;
    ServiceXmlLoader serviceLoader;

    // Since no genome version or assembly version info is available,
    // must search ALL.
    Iterator it = this.getAllSourcesForNavigation();
    while (it.hasNext()) {

      loader = (XmlLoader)it.next();
      if (! (loader instanceof ServiceXmlLoader))
        continue;

      serviceLoader = (ServiceXmlLoader)loader;
      serviceLoader.setGenomeVersionId(speciesOid.getGenomeVersionId());
      foundItems = serviceLoader.searchFor(targetTypeNumber, target, speciesOid);

      returnList.addAll(packIntoPathList(foundItems, serviceLoader));

    } // For all iterations

    return returnList;
  } // End method: searchFeature

  /** Make a list of navigation paths from the array of found items. */
  private List<NavigationPath> packIntoPathList(String[] foundItems, ServiceXmlLoader serviceLoader) {
    List<NavigationPath> returnList = new ArrayList<NavigationPath>();
    NavigationNode[] nodeArray;
    String[] csvFields;

    // Format/example of a found item:
    //  'INTERNAL:17000017770305','Amgen:8000001247836','clustered BlastX nr.aa precomputes','5509','6316','0'
    //  'axis oid             ','feature oid        ','display name                      ','strt','end' ,'human=1/precompute=0'
      for (String foundItem : foundItems) {
          csvFields = parseCSV(foundItem, 6);
          try {
              nodeArray = new NavigationNode[2];
              nodeArray[0] = new NavigationNode(
                      serviceLoader.parseContigOID(csvFields[0]),
                      NavigationConstants.GENOMIC_AXIS_NAME_INDEX,
                      "",
                      new Range(0, 0)
              );

              nodeArray[1] = new NavigationNode(
                      serviceLoader.parseFeatureOID(csvFields[1]),
                      csvFields[5].equals("1") ? NavigationNode.CURATED : NavigationNode.NON_CURATED,
                      csvFields[2],
                      new Range(Integer.parseInt(csvFields[3]),
                              Integer.parseInt(csvFields[4]))
              );

              returnList.add(new NavigationPath(nodeArray[1].getDisplayname(), nodeArray));

          }
          catch (Exception ex) {
              FacadeManager.handleException(ex);
          } // End catch block.
      } // For all found items.

    return returnList;
  } // End method

  /** Parses comma-sep/variable formatted string into an array. */
  String[] parseCSV(String inputLine, int numFields) {
    String[] returnFields = new String[numFields];

    int nextPos = 0;
    int startPos;
    int endPos;
    for (int i = 0; i < numFields; i++) {
      startPos = inputLine.indexOf('\'', nextPos);
      endPos = inputLine.indexOf('\'', startPos+1);

      returnFields[i] = inputLine.substring(startPos+1, endPos);

      nextPos = endPos + 1;
    } // Up until found.

    return returnFields;

  } // End method: parseCSV

  /**
   * Returns all loaders.  Navigation requires this, since its searches are
   * very broad, and hence may apply to any genome version.
   */
  protected Iterator getAllSourcesForNavigation() {
    return this.getGenomeVersionSpace().getOpenSources();
  } // End method

} // End class
