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
 *********************************************************************/
package client.gui.other.xml.xml_writer;

import api.entity_model.access.observer.ModifyManagerObserver;
import api.entity_model.access.observer.ModifyManagerObserverAdapter;
import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.management.ModelMgr;
import api.entity_model.management.ModifyManager;
import api.entity_model.model.annotation.Workspace;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.genetics.GenomeVersion;
import api.entity_model.model.genetics.Species;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;
import api.stub.data.OIDGeneratorListener;
import api.stub.data.Util;
import client.gui.framework.pref_controller.PrefController;
import client.gui.framework.session_mgr.SessionMgr;
import client.shared.file_chooser.FileChooser;

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
//import client.gui.other.xml.xml_promotion.GBWPromotionValidator;
/**
 * This class is a singleton that manages writing out the gbw workspace file.
 */
public class XMLWriter
{
    public static final String BACKUP_WORKSPACE_PROPERTY = "BackupWorkspace";

    private static final String WORKSPACE_EXTENSION = ".gbw";

    private Vector xmlWriterObservers = new Vector();
    private List axesList = new ArrayList();
    private boolean shouldBackup = true;
    private static XMLWriter xmlWriter = new XMLWriter();
    private XMLWorkSpaceBackup backup;
    private String backupfileName = null;
    private String savedXmlfile;

    private ModifyManagerObserver commandDoneObserver = new ModifyManagerObserverAdapter() {
            public void noteCommandDidFinish(String commandName, int commandKind) {
                backup.runOnce();
            }
        };


    private XMLWriter() {
        Boolean value = (Boolean)SessionMgr.getSessionMgr().getModelProperty(BACKUP_WORKSPACE_PROPERTY);
        if (value != null)
            shouldBackup = value.booleanValue();

        OIDGenerator.getOIDGenerator().addListener(new MyOIDGeneratorListener());
        String backupfilenamestr = (String)SessionMgr.getSessionMgr().getModelProperty(SessionMgr.USER_NEW_BACKUP_FILE_NAME);
        if (backupfilenamestr != null) {
            backupfileName = backupfilenamestr;
        }
        else { // defaut file name
            String fileSep = File.separator;
            backupfileName = System.getProperty("user.home") + fileSep + "x" + fileSep + "GenomeBrowser" + fileSep
                           + "workspace_backup" + WORKSPACE_EXTENSION;
            //also set to session mgr
            SessionMgr.getSessionMgr().setBackupFileName(backupfileName);
        }
    }

  static public XMLWriter getXMLWriter() {
     return xmlWriter;
  }

  public void addXMLWriterObserver(XMLWriterObserver xmlWriterObserver) {
    xmlWriterObservers.addElement(xmlWriterObserver);
  }

  public void removeXMLWriterObserver(XMLWriterObserver xmlWriterObserver) {
    xmlWriterObservers.removeElement(xmlWriterObserver);
  }

    public void setShouldBackupWorkspace(boolean shouldBackup) {
        this.shouldBackup = shouldBackup;
        if (!shouldBackup)
            stopBackup();
        else
            startBackup();
        SessionMgr.getSessionMgr().setModelProperty(BACKUP_WORKSPACE_PROPERTY, new Boolean(shouldBackup));
    }

    public boolean isBackingUpWorkspace () {
        return shouldBackup;
    }

    public void showBackupAdjustmentDialog() {
        PrefController.getPrefController().getPrefInterface("Workspace Backup",
                                                            SessionMgr.getSessionMgr().getActiveBrowser());
    }


    public String getWorkspaceBackupFileName() {
        return backupfileName;
    }

    public String getSavedXmlFileName(){
        return savedXmlfile;
    }

    public boolean getWorkspaceBackupFileExists() {
        File file = new File(getWorkspaceBackupFileName());
        return file.exists();
    }

    public boolean saveAsXML() {
        return saveAsXML(SessionMgr.getSessionMgr().getActiveBrowser());
    }


  private boolean saveAsXML(Component parentForFileSaveDialog) {
      JFileChooser chooser=new FileChooser();
      try {
          File test = (File)SessionMgr.getSessionMgr().getModelProperty("XMLSaveDirectory");
          if (test==null) chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
          else chooser.setCurrentDirectory(test);
      }
      catch (Exception ex) {
        System.out.println("\nCannot have a null value.\n");
      } // File cannot be opened.
      chooser.setFileFilter(new MyFileFilter());
      int option=chooser.showSaveDialog(parentForFileSaveDialog);
      if (option==JFileChooser.CANCEL_OPTION) return false;
      File selected_file=chooser.getSelectedFile();
      if (selected_file==null) return false;

      // see if the selected_file has .gbw extension if not add it before proceeding
      String filePathWithGBWExtension=addFileExtension(selected_file.getAbsolutePath());
      selected_file=new File(filePathWithGBWExtension);

      while(selected_file.exists()){
        JOptionPane pane =new JOptionPane();
        int fileOverWriteable=pane.showConfirmDialog(chooser, new String("Do You want to Overwrite Existing File?"), new String("WARNING !"),JOptionPane.YES_NO_OPTION);
        // if file not to be overwritten then show file chooser again and repeat if
        // the new selected file exists and needs to overwritten or not
        if(fileOverWriteable==JOptionPane.NO_OPTION){
          option=chooser.showSaveDialog(parentForFileSaveDialog);
          if (option==JFileChooser.CANCEL_OPTION) return false;

          selected_file=chooser.getSelectedFile();

          filePathWithGBWExtension=addFileExtension(selected_file.getAbsolutePath());
          selected_file=new File(filePathWithGBWExtension);

        }else{
           break;
        }
       }//while

      SessionMgr.getSessionMgr().setModelProperty("XMLSaveDirectory", selected_file.getAbsoluteFile());
      String filePath=selected_file.getAbsolutePath();
      System.out.println("file Path "+filePath);
      return saveToXML(filePath,true);
 }


 public synchronized boolean saveToXML(String fpath, boolean notifyUser) {

    String openFile=findOriginalGBWFileName();
    if (openFile!=null && openFile.equals(fpath) && openFile.equals(getWorkspaceBackupFileName())) {
       return false;  //do not backup over an open file.  Will crash app.
    }
    if (openFile!=null && openFile.equals(fpath)) {
       SessionMgr.getSessionMgr().handleException(new IllegalStateException("You cannot save a workspace file with the same filename as "+
          "the currently open workspace file.  Save rejected."));
       return false;  //do not backup over an open file.  Will crash app.
    }
    XMLdumper xmldumper=null;
    Workspace workspace=null;
    GenomeVersion model=null;
    for(Iterator i=ModelMgr.getModelMgr().getSelectedGenomeVersions().iterator();i.hasNext();){
      model=(GenomeVersion)i.next();
      if(model.hasWorkspace()){
        workspace= model.getWorkspace();
        break;
      }
    }

    // If we don't have a workspace, we shouldn't be writing anything...
    if (workspace == null) return false;

    // Need the workspace to get the replaces relationships...
    //Workspace workspace = model.getWorkspace();
    // Debug the workspace replaces relationships... what do we have?
    workspace.printWorkspaceStatistics();

    GenomicEntity root=model.getSpecies();
    XmlSpeciesEntityVisitor speciesVisitor =new XmlSpeciesEntityVisitor();
    try{
      ((Species)root).acceptVisitorForAlignedEntities(speciesVisitor,false);
      if (axesList.size()<1) return false;  //bail if no axes
      long assembly_version=0;
      String speciesName=null;
      speciesName=root.toString();
      assembly_version=((Species)root).getGenomeVersion().getVersion();
      XMLCopier xmlCopier=new XMLCopier(fpath,axesList,true,speciesName,assembly_version);
      xmlCopier.copyFileIfNecessary();
      /*if (!copied)*/ createGameFile(fpath,speciesName,assembly_version);
      String fPathWithExtension=addFileExtension(fpath);
      xmldumper=new XMLdumper(fPathWithExtension,true, notifyUser);
      if(axesList !=null && axesList.size()!=0){
        for(int i=0; i<axesList.size(); i++){
         GenomicEntity p=(GenomicEntity)axesList.get(i);
         xmldumper.xmlVisit(p);
        }//for
       }//if
       xmldumper.endWrite();
       endGameFile(fpath);
       // Let the workspace know it's been saved and now "clean".
       workspace.setWasSaved();
       // Notify the user...
       if (notifyUser) {
         JOptionPane.showMessageDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
          "Workspace was saved as XML file to: \n"+fPathWithExtension+".\nFile has "+
            xmldumper.getNumWrittenGenes()+" gene(s) and "+
            xmldumper.getNumWrittenUnparentedTranscripts()+" unparented transcript(s).",
            "Save Complete", JOptionPane.PLAIN_MESSAGE);
       }
      //Lastly record the saved out XML file's name
      savedXmlfile=fpath;


     }catch (Exception e) {
       SessionMgr.getSessionMgr().handleException(e);
     }
     return true;
  }

    private void startBackup() {
        stopBackup();
        if (!shouldBackup)
            return;

        backup = new XMLWorkSpaceBackup(getWorkspaceBackupFileName(), 1);
        ModifyManager.getModifyMgr().addObserver(commandDoneObserver);
    }

    private void stopBackup() {
        if (backup != null) {
            backup.stop();
            ModifyManager.getModifyMgr().removeObserver(commandDoneObserver);
            backup=null;
        }
    }

  private void createGameFile(String path, String species, long assemblyVersion) throws IOException{
       FileWriter writer=new FileWriter(path);
       writer.write("<game version="+"\""+"3.0"+"\""+" assembly_version="+"\""+assemblyVersion+
           "\""+" taxon="+"\""+species+"\""+">");


      Calendar cal = new GregorianCalendar();
      String date = Util.getDateTimeStringNow();
      System.out.println( "date " + date );
      int year = cal.get( Calendar.YEAR );
      int day = cal.get( Calendar.DAY_OF_MONTH );

      String month = date.substring( 0, date.indexOf( "/" ) );
      writer.write( "  <date day=" + "\"" + day + "\"" + " year=" + "\"" + year + "\"" + " month=" + "\"" + month + "\"" + ">" + "</date>" );
      String[] progNameVersion=getProgramNameAndVersion();
      writer.write("  <program>"+progNameVersion[0]+"</program>");
      writer.write("  <version>"+progNameVersion[1]+"</version>");


       writer.flush();
       writer.close();
  }


 // method to get the Genome Browser product Name and its version
  private String[] getProgramNameAndVersion(){

    String[] ret=new String[2];
    ret[0]=SessionMgr.getSessionMgr().getApplicationName();
    ret[1]=SessionMgr.getSessionMgr().getApplicationVersion();
    return ret;
  }




  private void endGameFile(String path) throws IOException {
       FileWriter writer=new FileWriter(path,true);
       writer.write("</game>");
       writer.flush();
       writer.close();

  }


 private String getFilepath(String fstr){
    int count=0;
    for(int i=0;i<fstr.length();i++){
      if((fstr.charAt(i))=='\\'&& i!=fstr.lastIndexOf(fstr,0)){
        count=i;
      }
    }
    return(fstr.substring(0,count));
  }

 /**
  * If the file name has the required extension, leave
  * it alone.  Otherwise, append that as an extension.
  */
 private String addFileExtension(String existingFpath){
   if(! existingFpath.endsWith(WORKSPACE_EXTENSION)){
     return(existingFpath.concat(WORKSPACE_EXTENSION));
   }
   else{
     return existingFpath;
   }
 }


  class MyFileFilter extends javax.swing.filechooser.FileFilter {
         public boolean accept(File pathname){
            if (pathname.getPath().toLowerCase().endsWith(WORKSPACE_EXTENSION) ||
               pathname.isDirectory()) return true;
            return false;
         }

         public String getDescription(){
           return "(*"+WORKSPACE_EXTENSION+") Genomics Exchange Format Files";
         }
  }




  /** Call this method to change state of XMLWriterObservers.  In the case
   * of the Save To XML menuItem, passing true will turn on the menuItem,
   * passing false will turn it off.
   */

  private void postCanSaveAsXML(boolean canSave) {
    //System.out.println("entering post can save as xml");
    for (Enumeration e=xmlWriterObservers.elements();e.hasMoreElements(); ){
      ((XMLWriterObserver)e.nextElement()).canSaveAsXML(canSave);
    }
  }

  private String findOriginalGBWFileName() {
    Object[] dataSources=FacadeManager.getFacadeManager().getOpenDataSources();
    for (int i=0;i<dataSources.length;i++) {
       if (dataSources[i] != null) {
         if (dataSources[i].toString().toLowerCase().endsWith(WORKSPACE_EXTENSION)) return dataSources[i].toString();
       }
    }
    return null;
  }

  private class XmlSpeciesEntityVisitor extends GenomicEntityVisitor {
      public XmlSpeciesEntityVisitor() {
        axesList.clear();
      }

      public void visitGenomicAxis(GenomicAxis entity) {
        axesList.add(entity);
      }
  }

   private class MyOIDGeneratorListener implements OIDGeneratorListener {
      public void newOIDGenerated(String type) {
         if (type.equals(OID.SCRATCH_NAMESPACE)) {
           postCanSaveAsXML(true);
           startBackup();
           OIDGenerator.getOIDGenerator().removeListener(this);
         }
      }

      public void newInitialValueForNameSpace(String type, BigInteger value) {
         if (type.equals(OID.SCRATCH_NAMESPACE)) {
           postCanSaveAsXML(true);
           startBackup();
           OIDGenerator.getOIDGenerator().removeListener(this);
         }
      }
   }

}
