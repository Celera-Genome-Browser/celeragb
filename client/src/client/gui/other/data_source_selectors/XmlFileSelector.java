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

package client.gui.other.data_source_selectors;

import api.facade.concrete_facade.xml.ValidationManager;
import api.facade.facade_mgr.FacadeManagerBase;
import client.gui.framework.session_mgr.SessionMgr;
import client.shared.file_chooser.FileChooser;
import client.shared.text_component.StandardTextArea;
import shared.io.ExtensionFileFilter;
import shared.util.FreeMemoryWatcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URL;

/**
 * Base class for selecting XML data sources.
 */
public abstract class XmlFileSelector extends Component {
  private ExtensionFileFilter xml_filter =
    new ExtensionFileFilter("XML Data File (*.xml)", ".xml");
  private static String fileSep=System.getProperty("file.separator");
  protected File directoryPrefFile=new File(System.getProperty("user.home")+fileSep+
         "x"+fileSep+"GenomeBrowser"+fileSep+"userPrefs.XMLDirectory");

  public abstract void selectDataSource(FacadeManagerBase f);

  private JDialog validationOutputDialog = null;
  private JTextArea validationTA = null;
  private FileChooser chooser = null;

  /**
   * Pops a file select widget, returns the file object chosen by
   * the user.
   */
  protected File askUserForFile(ExtensionFileFilter xmlFilter) {
    // Create a dialog which user can manipulate to get a file name.
    // It will have an accessory to validate files.
    //
    ValidatingAccessory accessory = new ValidatingAccessory();
    chooser = new FileChooser(accessory);
    chooser.setFileFilter(xmlFilter);
    File lastXMLFileSaved=(File)SessionMgr.getSessionMgr().getModelProperty("XMLSaveDirectory");
    if (lastXMLFileSaved!=null) {
      chooser.addShortcutButton("Last Saved Workspace",lastXMLFileSaved);
    }
    String backupFile=(String)SessionMgr.getSessionMgr().getModelProperty(SessionMgr.USER_CURRENT_BACKUP_FILE_NAME);
    if (backupFile!=null) {
      chooser.addShortcutButton("Workspace Backup File",new File(backupFile));
    }

    // Set the default directory from a preset preference if possible.
    //
    try {

      if (directoryPrefFile.canRead()) {
        ObjectInputStream istream= new ObjectInputStream(new FileInputStream(directoryPrefFile));
        chooser.setCurrentDirectory((File)(istream.readObject()));
        istream.close();

      } // Permission granted.
      else {
        // Obtain the directory in which data probably lies, using classpath
        // as a guide.
        //
        directoryPrefFile.createNewFile();
        URL dir_url=this.getClass().getResource("/resource/client/XMLdata");
        chooser.setCurrentDirectory(new File(dir_url.getFile()));

      } // Permission denied.

    } catch (Exception ex) {
      System.out.println("Problem setting directory to look in, accepting default");
    } // End catch block for pref file open exceptions.

    chooser.addActionListener(accessory);
    chooser.addPropertyChangeListener(accessory);
    chooser.setAccessory(accessory);

    int option = chooser.showOpenDialog(SessionMgr.getSessionMgr().getActiveBrowser());

    // Clean up the accessory.
    chooser.removeActionListener(accessory);
    chooser.removePropertyChangeListener(accessory);

    if (option== chooser.CANCEL_OPTION)
      return null;

    File selected_file = chooser.getSelectedFile();

    // Now attempt to writeback the user's currently-selected directory as the
    // new preference for reading XML files.
    //
    try {
      if (directoryPrefFile.canWrite() && selected_file.getAbsoluteFile()!=null) {
        ObjectOutputStream ostream= new ObjectOutputStream(new FileOutputStream(directoryPrefFile));
        ostream.writeObject(selected_file.getAbsoluteFile());
        ostream.close();
      } // Permission granted.
    } catch (Exception ex) {
       System.err.println("XML Directory Prefs file cannot be written");
    } // End catch block for writeback of preferred directory.

    URL dir_url = this.getClass().getResource("/resource/client/XMLdata");
    if (dir_url != null) {
      try {
        chooser.setCurrentDirectory(new File(dir_url.getFile()));
        // System.out.println("URL "+dir_url.getFile());
      } catch (Exception ex) {
        System.out.println("Problem setting directory to look in, accepting default");
      } // End catch block for setting current directory in chooser.
    } // Non null directory URL.

    // String dir_name = selected_dir.getName();
    // String dir_path=selected_dir.getAbsolutePath();
    // Upon OPEN selected, register this facade as In Use.  Aggregation will track this.


    return selected_file;

  } // End method: askUserForFileName


  //This method is to be defined for all sub classes. This abstract does nothing.
  protected abstract void setFacadeProtocol();


  //-------------------------------INNER CLASSES
  /**
   * This class acts as an accessory to a JFileChooser.  This "embellishment"
   * allows the user to pre-validate the file that is under the selection
   * line on the JFileChooser's tree of files.
   */
  class ValidatingAccessory extends JPanel implements PropertyChangeListener,
    ActionListener {

    // Defining the DTD here may be changed later on, for flexibility.
    private final String DTD_NAME = "/resource/client/XMLdata/dtd/GenomicsExchangeFormat-V4.dtd";
    private final float IN_MEMORY_FACTOR = 1.5F;  // This many times as much mem. to rep an XML file.

    private final String DTD_ROOT = "game";
    private StringBuffer outputBuffer = new StringBuffer();

    private File lastSelectedFile = null;
    private JButton dtdValidationButton = null;
    private JButton schemaValidationButton = null;
    private JTextArea sizeErrorLabel = null;

    /**
     * Constructor will build the button by which user may activate the
     * validation.
     */
    public ValidatingAccessory() {

      setLayout(new BorderLayout());

      Insets buttonInsets = new Insets(0, 3, 0, 3);

      dtdValidationButton = new JButton("DTD Validate");
      dtdValidationButton.setMargin(buttonInsets);

      schemaValidationButton = new JButton("XML Schema Validate");
      schemaValidationButton.setMargin(buttonInsets);

      Insets areaInsets = new Insets(0, 1, 0, 1);
      sizeErrorLabel = new StandardTextArea("");
      sizeErrorLabel.setMargin(areaInsets);
      sizeErrorLabel.setForeground(Color.blue);
      sizeErrorLabel.setBackground(this.getBackground());

      JPanel validationPanel = new JPanel();
      validationPanel.setLayout(new GridLayout(2, 1));

      validationPanel.add(dtdValidationButton);
      validationPanel.add(schemaValidationButton);

      add(validationPanel, BorderLayout.NORTH);

      if (lastSelectedFile == null) {
        dtdValidationButton.setEnabled(false);
        schemaValidationButton.setEnabled(false);
      } // Nothing selected.
      setSize(100, 50);

      // Setup the validating response.
      dtdValidationButton.addActionListener(new ActionListener() {

        /**
         * This method is called when user asks to validate a file.
         */
        public void actionPerformed(ActionEvent event) {
          ValidationManager.getInstance().revalidateAndReportInputFile(lastSelectedFile.getPath(), ValidationManager.VALIDATE_WITH_DTD);
        } // End method: actionPerformed

      }); // Picks up button clicks.

      // Setup the validating response.
      schemaValidationButton.addActionListener(new ActionListener() {

        /**
         * This method is called when user asks to validate a file.
         */
        public void actionPerformed(ActionEvent event) {
          ValidationManager.getInstance().revalidateAndReportInputFile(lastSelectedFile.getPath(), ValidationManager.VALIDATE_WITH_XML_SCHEMA);
        } // End method: actionPerformed

      }); // Picks up button clicks.

    } // End constructor

    //----------------------------IMPLEMENTATION OF ActionListener
    /**
     * Once user has selected or cancelled, we should eliminate any
     * extraneous components.
     */
    public void actionPerformed(ActionEvent event) {
      if (validationOutputDialog != null) {
        validationOutputDialog.dispose();
        validationOutputDialog = null;
      } // Remove frame.

      if (validationTA != null) {
        validationTA = null;
      } // Remove text area

    } // End method: actionPerformed
    //----------------------------IMPLEMENTATION OF PropertyChangeListener
    /**
     * Receives notice of changes of states of properties.
     */
    public void propertyChange (PropertyChangeEvent event) {
      String propertyName = event.getPropertyName();
      if (propertyName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
        // Change the selected file here.
        lastSelectedFile = ((JFileChooser)event.getSource()).getSelectedFile();

        // Enable the buttons.
        dtdValidationButton.setEnabled(true);
        schemaValidationButton.setEnabled(true);

        // Test for available size.
        try {
          long fileSize = lastSelectedFile.length();
          if (((long)(float)fileSize * IN_MEMORY_FACTOR) > FreeMemoryWatcher.getFreeMemoryWatcher().getFreeMemory()) {
            // What to do about it?
            sizeErrorLabel.setText("Selected\nFile May\nRequire\nMore Than\nAvailable\nMemory");
          } // Not enough memory.
          else {
            sizeErrorLabel.setText("");
          } // Memory is available
        } catch (Exception ex) {}

      } // File selection changed
    } // End method: propertyChange

  } // End class: ValidatingAccessory

} // End class: XmlFileSelector

/*
 $Log$
 Revision 1.1  2006/11/09 21:36:21  rjturner
 Initial upload of source

 Revision 1.36  2002/11/08 14:13:36  lblick
 Moved from the package client.shared.file_chooser.

 Revision 1.35  2002/11/07 20:06:46  lblick
 Moved package shared.text_component to package client.shared.text_component.

 Revision 1.34  2002/11/07 19:47:21  lblick
 Moved shared.file_chooser package to client.shared.file_chooser package.

 Revision 1.33  2002/11/07 16:10:25  lblick
 Removed obsolete imports and unused local variables.

 Revision 1.32  2002/08/13 23:01:04  tsaf
 Reworked the GenericModel and GenericModelListener classes.
 Collapsed listener collections down and also combined the listeners into
 the proper inheritance hierarchy.

 Revision 1.31  2002/07/18 22:18:26  tsaf
 Moved ExtensionFileFilter from api.concrete_facade.xml into the
 x.shared.file_chooser package so I coule use it.
 Updated all dependent files.

 Revision 1.30  2002/04/02 14:38:35  lfoster
 Added a "revalidate" mechanism to the validation manager to allow users to iteratively parse gbfs.  Hooked up same to the file selector.

 Revision 1.29  2002/02/15 18:51:45  lfoster
 Enabled validation of individual file as part of gbf open dialog.  Allowed preemptive
 bail when too many exceptions are thrown (currently: 100Kb of output).

 Revision 1.28  2002/02/11 21:44:22  lfoster
 First working cut of integrated schema validation.

 Revision 1.27  2001/08/28 21:49:27  lfoster
 Changed all constructors of JTextArea to StandardTextArea, and all constructors of JTextField to StandardTextField, to support 7.1.5.1

 Revision 1.26  2001/08/28 13:29:04  lfoster
 Validating with OTHER version of DTD.

 Revision 1.25  2001/07/16 19:41:01  tsaf
 Removal of JFrames that were used only to put the image icon
 on the components.

 Now, all of the JOptionPanes get passed the active browser component
 directly from the SessionMgr.
 This does not create any circular dependencies.

 Revision 1.24  2001/06/04 16:41:44  pdavies
 Moved FileChooser to shared

 Revision 1.23  2001/05/13 23:22:16  BhandaDn
 more meaningful static string names for  back up files

 Revision 1.22  2001/05/11 16:49:57  pdavies
 Using other backup file name

 Revision 1.21  2001/05/11 16:04:15  pdavies
 Added buttons for last workspace and the backup file

 Revision 1.20  2001/05/11 14:27:13  pdavies
 Changed all JFileChoosers to framework.file_chooser.FileChooser

 Revision 1.19  2001/05/06 04:56:57  lfoster
 Setup an exception handler for parse errors.

 Revision 1.18  2001/05/04 19:14:01  lfoster
 Excised the error handler to an XML class.

 Revision 1.17  2000/11/13 16:40:21  tsaf
 Made setFacadeProtocol() abstract to force sub-class override at compile-time.

 Revision 1.16  2000/11/13 14:12:25  pdavies
 Chnaged order of setting the protocol to be after the loader is set

 Revision 1.15  2000/11/06 14:40:48  lfoster
 Fixed freeMemory calculation so it uses the Free Memory Watcher.

 Revision 1.14  2000/11/03 23:13:53  lfoster
 Checking memory, and presenting an advisory in the validating accessory.

 */
