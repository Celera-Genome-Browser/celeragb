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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Title:        Genome Browser Client
 * Description:  Facade Manager for feature files loaded in mid-session.
 * @author Les Foster
 * @version $Id$
 */
public class XmlInSessionLoadFacadeManager extends XmlFacadeManager {

  /** Constructor to build the genome version space. */
  public XmlInSessionLoadFacadeManager() {
    super.setGenomeVersionSpace(new MultiFileGenomeVersionSpace());
  } // End Constructor

  /** Returns all the open sources found. */
  public Object[] getOpenDataSources() {
    Iterator iterator = getGenomeVersionSpace().getOpenSources();

    List returnList = new ArrayList();
    XmlLoader nextFileLoader = null;
    while (iterator.hasNext()) {
      nextFileLoader = (XmlLoader)iterator.next();
      returnList.add(nextFileLoader);
    } // For all iterations

    Object[] returnArray = new Object[returnList.size()];
    returnList.toArray(returnArray);
    return returnArray;

  } // End method

  /**
   * Adds a loader for sourcing data.
   */
  public void addSource(String featureFileName) {
    if (featureFileName != null) {
      MultiFileGenomeVersionSpace genomeVersionSpace = (MultiFileGenomeVersionSpace)getGenomeVersionSpace();
      genomeVersionSpace.addFeatureSource(featureFileName);
    } // Got a real loader
  } // End method

  /**
   * Returns an indicator of whether additional sources may be added to
   * this factory.
   */
  public boolean canAddMoreDataSources() {
    // Many feature files may be added during the course of a session.
    return true;
  } // End method: canAddMoreDataSources

  /**
   * Returns string name of some class implementing DataSourceSelector.
   */
  public String getDataSourceSelectorClass() {
    return "client.gui.other.data_source_selectors.XmlFeatureFileSelector";
  } // End method

} // End class