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

import api.entity_model.model.genetics.GenomeVersion;
import api.facade.concrete_facade.xml.XmlFileOpenHandlerBase;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.GenomeVersionInfo;
import client.gui.framework.session_mgr.SessionMgr;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * Allows user to select an XML feature file and pass it to an open handler.
 */
public class MatchingGenomeVersionSelector {

  /** Shows user the collec. of genome versions which matched the input file, as a table. */
  public void presentUserWithChooser(Collection matchingVersions, String selectedSource, FacadeManagerBase facade, XmlFileOpenHandlerBase opener) {
    JTable versionChooser = new JTable(new LocalGenomeVersionModel(matchingVersions));
    versionChooser.clearSelection();
    JFrame chooserFrame = new JFrame("Choose a Genome Version to Associate with "+selectedSource);
    chooserFrame.getContentPane().setLayout(new java.awt.BorderLayout());
    JPanel tablePanel = new JPanel();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//    versionChooser.setPreferredScrollableViewportSize(new java.awt.Dimension((int)(screenSize.getWidth()*.9),150));
    tablePanel.add(versionChooser);
    chooserFrame.getContentPane().add(tablePanel, java.awt.BorderLayout.CENTER);
    JButton okButton = new JButton("OK");
    JButton cancelButton = new JButton("Cancel");
    LocalOkListener okListener = new LocalOkListener();
    okListener.init(selectedSource, versionChooser, matchingVersions, chooserFrame, facade, opener);

    LocalCancelListener cancelListener = new LocalCancelListener();
    cancelListener.init(chooserFrame);

    okButton.addActionListener(okListener);
    cancelButton.addActionListener(cancelListener);

    Dimension frameSize = new Dimension(510, 400);
    chooserFrame.setSize(frameSize);
    if (frameSize.height > screenSize.height)
      frameSize.height = screenSize.height;
    if (frameSize.width > screenSize.width)
      frameSize.width = screenSize.width;
    chooserFrame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new java.awt.FlowLayout());
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    chooserFrame.getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

    versionChooser.sizeColumnsToFit(-1);
    chooserFrame.pack();
    chooserFrame.setVisible(true);
  } // End method

  //This method is defined for all sub classes of XmlFileSelector.
  protected void setFacadeProtocol(FacadeManagerBase facade) {
    String protocolToAdd = new String(
            FacadeManager.getProtocolForFacade(facade.getClass()));
    FacadeManager.addProtocolToUseList(protocolToAdd);
  }

  //-------------------------------------INNER CLASSES
  /** Table model implementation to help user pick a genome version. */
  public class LocalGenomeVersionModel extends AbstractTableModel {
    private final String DATASOURCE_COLUMN_NAMES[]=new String[]{"Species","Assembly","Description","Read Only","Data Type","Data Source"};
    private Collection genomeVersions;
    private boolean showDataSource;

    /** Source table from collec. of GVs */
    public LocalGenomeVersionModel(Collection matchingGenomeVersions) {
      Object obj=SessionMgr.getSessionMgr().getModelProperty("ShowInternalDataSourceInDialogs");
      if (obj!=null && obj instanceof Boolean) {
        showDataSource=((Boolean)obj).booleanValue();
      } // Got value to set.

      genomeVersions = matchingGenomeVersions;
    } // End constructor

    /** Show method for cells in table. */
    public Object getValueAt(int row,int col){
      GenomeVersion[] versionArray = new GenomeVersion[genomeVersions.size()];
      genomeVersions.toArray(versionArray);
      GenomeVersion selectedVersion = versionArray[row];
      GenomeVersionInfo info = selectedVersion.getGenomeVersionInfo();
        switch(col) {
          case 0: return info.getSpeciesName();
          case 1: return info.getAssemblyVersionAsString();
          case 2: return selectedVersion.getDescription();
          case 3: return new Boolean(selectedVersion.isReadOnly());
          case 4: switch (info.getDataSourceType()) {
                    case GenomeVersionInfo.DATABASE_DATA_SOURCE: return "Internal Database";
                    case GenomeVersionInfo.FILE_DATA_SOURCE: return "Local XML File";
                    case GenomeVersionInfo.URL_DATA_SOURCE: return "HTTP URL Service";
                  } // End source type switch.
          case 5: return (showDataSource||(info.getDataSourceType()!=GenomeVersionInfo.DATABASE_DATA_SOURCE))?
                    selectedVersion.getDatasourceForVersion():"Internal Database";
          default: return "No Column Defined";
        } // End column switch
    } // End method

    public void setValue(int index) {
      // Tell which was selected.
    } // End method

    public int getRowCount() {
      return genomeVersions.size();
    } // End method

    public int getColumnCount() {
      return DATASOURCE_COLUMN_NAMES.length;
    } // End method

    public String getColumnName(int column) {
       return DATASOURCE_COLUMN_NAMES[column];
    } // End method

  } // End class: LocalGenomeVersionModel

  /** Listen for user to click OK, and respond by opening the input file. */
  public class LocalOkListener implements ActionListener {

    private String selectedSource;
    private JTable versionChooser;
    private Collection matchingVersions;
    private FacadeManagerBase facade;
    private JFrame chooserFrame;
    private XmlFileOpenHandlerBase opener;

    /** Callback to this method indicates user has settled on a genome version. */
    public void actionPerformed(ActionEvent ae) {
      int selectedRow = versionChooser.getSelectedRow();
      if (selectedRow < 0)
        return; // Nothing selected.

      GenomeVersion[] versionArray = new GenomeVersion[matchingVersions.size()];
      matchingVersions.toArray(versionArray);
      GenomeVersion version = versionArray[selectedRow];
      GenomeVersionInfo info = version.getGenomeVersionInfo();
      try {
        opener.setGenomeVersionInfo(info);
        opener.loadXmlFile(selectedSource);
        setFacadeProtocol(facade);
        // Notify the GenomeVersion...
        version.noteRelevantFacadeManager(facade);
        chooserFrame.dispose();
      } // End try block.
      catch (Exception ex) {
        FacadeManager.handleException(ex);
      } // End catch block.
    } // End method

    /** Initialize */
    public void init(String selectedSource, JTable versionChooser, Collection matchingVersions, JFrame chooserFrame, FacadeManagerBase facade,
      XmlFileOpenHandlerBase opener) {

      this.selectedSource = selectedSource;
      this.versionChooser = versionChooser;
      this.matchingVersions = matchingVersions;
      this.facade = facade;
      this.chooserFrame = chooserFrame;
      this.opener = opener;
    } // End method

  } // End class

  /** Listen for user to press cancel button, and respond by disposing of the frame. */
  public class LocalCancelListener implements ActionListener {
    private JFrame chooserFrame = null;
    /** Callback to this method indicates the user has chosen not to choose. */
    public void actionPerformed(ActionEvent ae) {
      chooserFrame.dispose();
    } // End method

    public void init(JFrame chooserFrame) {
      this.chooserFrame = chooserFrame;
    } // End method
  } // End class

} // End class
