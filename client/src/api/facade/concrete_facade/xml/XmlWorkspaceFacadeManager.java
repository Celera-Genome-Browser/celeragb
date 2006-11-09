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
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/
package api.facade.concrete_facade.xml;


/**
 * Factory of API Facades.  Manages feature file loaders as sources.
 * Returns all "api facades" to allow the model layer to read workspace
 * XML files.
 */
public class XmlWorkspaceFacadeManager extends XmlFacadeManager {

  // Keep this workspace-only facade as state data.
  private XmlWorkspaceFacade workspaceFacade;

  /**
   * Constructor will enable the genome version space to be used by this
   * type of facade.
   */
  public XmlWorkspaceFacadeManager() {
    super();
    // Setting the genome version space to one that deals with
    // a single loader/file.  Later that loader will be set to
    // a loader representing the workspace file.
    setGenomeVersionSpace(new FileGenomeVersionSpace());
  } // End constructor

  /**
   * Returns an indicator of whether additional sources may be added to
   * this factory.
   */
  public boolean canAddMoreDataSources() {
    // Only one workspace file per session.  Otherwise, it is impossible to
    // track OIDs of things.  They become non-unique across multiple scratch
    // files.
    return (! getGenomeVersionSpace().hasLoaders());
  } // End method: canAddMoreDataSources

  /** Tells what file is open. */
  public Object[] getOpenDataSources() {
    if (getGenomeVersionSpace().hasLoaders()) {
      FileGenomeVersionSpace space = (FileGenomeVersionSpace)getGenomeVersionSpace();
      String file=space.getWorkspaceFileName() ;
      if (file==null) return new Object[0];
      else return new Object[] { space.getWorkspaceFileName() };
    } // Lookup filename from space.
    else {
      return new Object[0];
    } // Nothing to report.

  } // End method

  /**
   * Returns a class name to be instantiated to return a data source for this
   * factory.
   */
  public String getDataSourceSelectorClass() {
    return ("client.gui.other.data_source_selectors.XmlWorkspaceFileSelector");
  } // End method: getDataSourceSelectorClass

  /**
   * Builds/returns a special facade for populating the workspace.
   * @returns an XmlWorkspaceFacade.
   */
  public XmlWorkspaceFacade getXmlWorkspaceFacade() {
    if (workspaceFacade == null)
      workspaceFacade = new XmlWorkspaceFacade((FileGenomeVersionSpace)getGenomeVersionSpace());
    return workspaceFacade;
  } // End method: getXmlWorkspaceFacade

} // End class: XmlWorkspaceFacadeManager
