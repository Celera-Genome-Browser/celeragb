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

package client.gui.other.data_source_selectors;

import api.entity_model.access.filter.GenomeVersionCollectionFilter;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.concrete_facade.xml.WorkspaceXmlFileOpenHandler;
import api.facade.facade_mgr.DataSourceSelector;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.GenomeVersionInfo;
import client.gui.framework.session_mgr.SessionMgr;
import shared.io.ExtensionFileFilter;

import java.io.File;
import java.util.Comparator;

import javax.swing.JOptionPane;

/**
 * Allows user to select an XML feature file and pass it to an open handler.
 */
public class XmlWorkspaceFileSelector extends XmlFileSelector implements DataSourceSelector{
  private static String REJECTED_WORKSPACE_MSG = "Workspace File Contents Not Loaded";
  private ExtensionFileFilter xmlFilter =
    new ExtensionFileFilter("Genome Browser Workspace (*.gbw)", ".gbw");

  /**
   * Allow external set of already-chosen workspace file.
   */
  public void setDataSource(FacadeManagerBase facade, Object dataSource){
    // Test the file, and if it meets criteria, add it to usable sources of data.
    buildLoaderIfCanSelectGenomeVersion(facade, (String)dataSource);
  } // End method

  /**
   * Display a gui so user may choose correct data source to open.
   */
  public  void selectDataSource(FacadeManagerBase manager){

    String fileName = null;

    // Get the user's choice.
    File selectedFile = askUserForFile(this.xmlFilter);

    // If the user selected anything, load it up.
    if (selectedFile != null) {
      fileName = selectedFile.getAbsolutePath();

      // Test the file, and if it meets criteria, add it to usable sources of data.
      buildLoaderIfCanSelectGenomeVersion(manager, fileName);
    } // User presented a real file name.

  } // End method: selectDataSource

  /**
   * Given the filename and facade manager base, test the file's contents to
   * see if any available genome versions can match it.  If they can, handle
   * cases of 1, or more.
   *
   * @param FacadeManagerBase facade
   * @param String workspaceFile
   */
  private void buildLoaderIfCanSelectGenomeVersion(FacadeManagerBase facade, String workspaceFile) {
    try {

      // Do this initially so that other steps will not have conflicts.
      // If it turns out to be ill-advised, counteract by removing.
      //  CAUTION: order dependency!  Do this first!
      setFacadeProtocol();

      // Get the entire set of genome versions that have been selected.
      //
      WorkspaceXmlFileOpenHandler openHandler = new WorkspaceXmlFileOpenHandler(facade);
      GenomeVersionInfo fileInfo = openHandler.findGenomeVersionInfo(workspaceFile);
      String workspaceSpecies = fileInfo.getSpeciesName();
      long workspaceAssemblyVersion = fileInfo.getAssemblyVersion();
      Comparator workspaceComparator = new GenomeVersionComparator();

      GenomeVersionCollectionFilter workspaceFilter =
        new MatchesWorkspaceFileGenomeVersionCollectionFilter(  workspaceComparator,
                                                                workspaceAssemblyVersion,
                                                                workspaceSpecies);

      GenomeVersionSelector subSelector = new GenomeVersionSelector();
      GenomeVersion selectedVersion = subSelector.selectDataSource(workspaceFilter);
      if (selectedVersion == null) {
        FacadeManager.removeProtocolFromUseList(FacadeManager.getProtocolForFacade(facade.getClass()));

        JOptionPane.showMessageDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
          "The chosen file: "+workspaceFile+",\ndoes not match any available genome version",
          REJECTED_WORKSPACE_MSG, JOptionPane.ERROR_MESSAGE);
      } // Nothing selected.  So no association will be made.
      else {
        openHandler.setGenomeVersionInfo(selectedVersion.getGenomeVersionInfo());
        openHandler.loadXmlFile(workspaceFile);
        // Notify the GenomeVersion...
        selectedVersion.noteRelevantFacadeManager(facade);
      } // Need to associate the GV with the GBW.

    } // End try block.
    catch(Exception ex){
      ex.printStackTrace();
    } // End catch block.

  } // End method

  //This method is defined for all sub classes of XmlFileSelector.
  protected void setFacadeProtocol() {
    String protocolToAdd = new String(
            FacadeManager.getProtocolForFacade(api.facade.concrete_facade.xml.XmlWorkspaceFacadeManager.class));
    FacadeManager.addProtocolToUseList(protocolToAdd);
  }

  /** Comparator for sorting the genome versions. */
  private class GenomeVersionComparator implements Comparator {

     /** Tells high/low/same. */
     public int compare(Object o1, Object o2) {
       int retVal = 0;

       // Approach: any comparison that cannot be made, will be flagged
       // as an equality.
       GenomeVersion gv1 = null;
       GenomeVersion gv2 = null;
       if ((o1 instanceof GenomeVersion) && (o2 instanceof GenomeVersion)) {
         gv1 = (GenomeVersion)o1;
         gv2 = (GenomeVersion)o2;
         if ((gv1 == null) || (gv2 == null))
           retVal = 0;
         else {
           if ((gv1.getSpecies() == null) || (gv2.getSpecies() == null))
             retVal = 0;
           else if (gv1.getSpecies().getDisplayName() == null || gv2.getSpecies().getDisplayName() == null)
             retVal = 0;
           else if (! gv1.getSpecies().getDisplayName().equals(gv2.getSpecies().getDisplayName())) {
             retVal = (int)(gv1.getAssemblyVersion() - gv2.getAssemblyVersion());
           } // Same species?
           else {
             retVal = gv1.getSpecies().getDisplayName().compareTo(gv2.getSpecies().getDisplayName());
           } // Different species?
         } // Non-null.
       } // Can do this comparison.

       return retVal;

     } // End method

  } // End class

  /**
   * Filter for genome versions, that discards available genome versions that do
   * not have the same species and assembly version as the gbw or workspace file
   * that the user has selected to open.
   */
  private class MatchesWorkspaceFileGenomeVersionCollectionFilter extends GenomeVersionCollectionFilter {
    private String species;
    private long assemblyVersion;

    /**
     * Constructor keeps all the criteria needed for accept/reject.
     */
    MatchesWorkspaceFileGenomeVersionCollectionFilter(Comparator comparator, long assemblyVersion, String species) {
      super(comparator);
      this.assemblyVersion = assemblyVersion;
      this.species = species;
    } // End constructor

    /**
     * Filtering on match of assembly version and species.
     *
     * @param GenomeVersion genomeVersion the candidate to add to the collection.
     */
    public boolean addGenomeVersionToReturnCollection(GenomeVersion genomeVersion) {
      boolean retval = true;
      if (genomeVersion.getAssemblyVersion() == assemblyVersion) {
        if (genomeVersion.getGenomeVersionInfo() == null)
          retval = false;
        if (! genomeVersion.getGenomeVersionInfo().getSpeciesName().equalsIgnoreCase(species))
          retval = false;
      } // Got same assembly version.
      else
        retval = false;

      return retval;

    } // End method

  } // End class

} // End class: XmlWorkspaceFileSelector
