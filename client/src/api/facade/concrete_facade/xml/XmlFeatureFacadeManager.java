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


/**
 * Returns all "api facades" to allow the model layer to read feature-only
 * XML files.
 */
public class XmlFeatureFacadeManager extends XmlFacadeManager {

  /**
   * Constructor will enable the genome version space to be used by this
   * type of facade.
   */
  public XmlFeatureFacadeManager() {
    // Setting the genome version space to one that deals with
    // a single loader/file.  Later that loader will be set to
    // a loader representing the workspace file.
    setGenomeVersionSpace(new InteractiveBlastGenomeVersionSpace());
  } // End constructor.

  /**
   * Returns an indicator of whether additional sources may be added to
   * this factory.
   */
  public boolean canAddMoreDataSources() {
    // Many feature files may be added during the course of a session.
    return true;
  } // End method: canAddMoreDataSources

  /** No data sources at this time.  Class will probably go away. */
  public Object[] getOpenDataSources() { return new Object[0]; }

  /**
   * Returns a class name to be instantiated to return a data source for this
   * factory.
   */
  public String getDataSourceSelectorClass() {
    return ("client.gui.other.data_source_selectors.XmlFeatureFileSelector");
  } // End method: getDataSourceSelectorClass

}

