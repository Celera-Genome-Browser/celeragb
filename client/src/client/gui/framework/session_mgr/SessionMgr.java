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
 ********************************************************************/

package client.gui.framework.session_mgr;

import api.entity_model.management.ModelMgr;
import api.entity_model.management.PropertyMgr;
import api.entity_model.management.properties.PropertyCreationRule;
import api.facade.facade_mgr.FacadeManager;
import api.facade.roles.ExceptionHandler;
import api.stub.LoginProperties;
import client.gui.framework.browser.Browser;
import client.gui.framework.external_listener.ExternalListener;
import client.gui.framework.pref_controller.PrefController;
import client.gui.other.xml.xml_writer.XMLWriter;
import shared.util.PropertyConfigurator;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


public class SessionMgr {

  public static String DISPLAY_FREE_MEMORY_METER_PROPERTY="SessionMgr.DisplayFreeMemoryProperty";
  public static String DISPLAY_SUB_EDITOR_PROPERTY="SessionMgr.DisplaySubEditorProperty";

  public static String USER_NAME=LoginProperties.SERVER_LOGIN_NAME;
  public static String USER_PASSWORD=LoginProperties.SERVER_LOGIN_PASSWORD;


  public static String USER_NEW_BACKUP_FILE_NAME="x.genomebrowser.WorkSpaceBackupCurrentFileName";
  public static String USER_CURRENT_BACKUP_FILE_NAME="x.genomebrowser.WorkSpaceBackupLastFileName";

  public static String DISPLAY_LOOK_AND_FEEL="SessionMgr.DisplayLookAndFeel";

  private static String PROPERTY_CREATION_RULES="SessionMgr.PropertyCreationRules";
  private static ModelMgr modelManager=ModelMgr.getModelMgr();
  private static SessionMgr sessionManager=new SessionMgr();
  private SessionModel sessionModel=SessionModel.getSessionModel();
  private float browserSize=.8f;
  private String browserTitle;
  private ImageIcon browserImageIcon;
  private Component splashPanel;
  //private String releaseVersion="$date$";
  private ExternalListener externalListener;
  private File settingsFile;
  private String fileSep=File.separator;
  private String prefsDir=System.getProperty("user.home")+fileSep+
         "x"+fileSep+"GenomeBrowser"+fileSep;
  private String prefsFile=prefsDir+"GB_Settings4.2";
  private Map browserModelsToBrowser=new HashMap();
  private String backupFileName=null;
  private WindowListener myBrowserWindowListener=new MyBrowserListener();
  private Browser activeBrowser;
  private String appName,appVersion;
  private Date sessionCreationTime;

  private SessionMgr() {
    settingsFile=new File(prefsFile);
      try {
         settingsFile.createNewFile();  //only creates if does not exist
      }
      catch (IOException ioEx) {
         try {
              new File(prefsDir).mkdirs();
              settingsFile.createNewFile();  //only creates if does not exist
         }
         catch (IOException ioEx1) {
            System.err.println("Cannot create settings file!! "+ioEx1.getMessage());
         }
      }

      readSettingsFile();
      if (getModelProperty(DISPLAY_FREE_MEMORY_METER_PROPERTY)==null)
        setModelProperty(DISPLAY_FREE_MEMORY_METER_PROPERTY,new Boolean(true));
      if (getModelProperty(DISPLAY_SUB_EDITOR_PROPERTY)==null)
        setModelProperty(DISPLAY_SUB_EDITOR_PROPERTY,new Boolean(true));
      if (getModelProperty(PROPERTY_CREATION_RULES)!=null) {
        Set rules= (Set)getModelProperty(PROPERTY_CREATION_RULES);
        for (Iterator it=rules.iterator();it.hasNext(); ){
          PropertyMgr.getPropertyMgr().addPropertyCreationRule((PropertyCreationRule)it.next());
        }
      }
      if (getModelProperty(DISPLAY_LOOK_AND_FEEL)!=null) {
        try {
          setLookAndFeel((String)getModelProperty(DISPLAY_LOOK_AND_FEEL));
        }
        catch (Exception ex) {
          handleException(ex);
        }
      }
      else {
         setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      String tempLogin = (String)getModelProperty(USER_NAME);
      String tempPassword = (String)getModelProperty(USER_PASSWORD);
      if (tempLogin!=null && tempPassword!=null) {
        PropertyConfigurator.getProperties().setProperty(USER_NAME, tempLogin);
        PropertyConfigurator.getProperties().setProperty(USER_PASSWORD, tempPassword);
      }
      sessionCreationTime=new Date();
  } //Singleton enforcement

  private void readSettingsFile() {
       JFrame mainFrame = new JFrame();
       JOptionPane optionPane = new JOptionPane();
       // JCVI LLF: removed 10/19/06
//     RT 10/27/2006
       mainFrame.setIconImage(new ImageIcon(this.getClass().getResource( System.getProperty("x.genomebrowser.WindowCornerLogo"))).getImage());
       mainFrame.getContentPane().add(optionPane);
       if (!settingsFile.canRead()) {
         optionPane.showMessageDialog(mainFrame,"Settings file cannot be opened.  "+
            "Settings were not read and recovered.","ERROR!",JOptionPane.ERROR_MESSAGE);
         settingsFile.renameTo(new File(prefsFile+".old"));

       }
      try {
        ObjectInputStream istream= new ObjectInputStream(new FileInputStream(settingsFile));
        switch (istream.readInt()) {
           case 1: {
             try {

              sessionModel.setModelProperties((TreeMap)istream.readObject());
              istream.close();
             }
             catch (Exception ex) {
              istream.close();
              optionPane.showMessageDialog(mainFrame,"Settings were not recovered into the session.","ERROR!",
                JOptionPane.ERROR_MESSAGE);
              File oldFile = new File(prefsFile+".old");
              oldFile.delete();
              settingsFile.renameTo(new File(prefsFile+".old"));
             }
             break;
           }
           default: {
           }
        }
     }
     catch (Exception ioEx) {
     } //new settingsFile
  }

  static public SessionMgr getSessionMgr() {return sessionManager;}

  public Object setModelProperty(Object key, Object value) {
     return sessionModel.setModelProperty(key,value);
  }

  public Object getModelProperty (Object key) {
    return sessionModel.getModelProperty(key);
  }

  public Iterator getModelPropertyKeys() {
    return sessionModel.getModelPropertyKeys();
  }


   public String getNewBrowserTitle(){
    return browserTitle;

  }

  public void registerPreferenceInterface(Object interfaceKey, Class interfaceClass) throws Exception {
    PrefController.getPrefController().registerPreferenceInterface(interfaceKey, interfaceClass);
  }


  public void removePreferenceInterface(Object interfaceKey) throws Exception {
    PrefController.getPrefController().deregisterPreferenceInterface(interfaceKey);
  }

  public void registerExceptionHandler(ExceptionHandler handler) {
     modelManager.registerExceptionHandler(handler);
  }

  public void setNewBrowserSize(float screenPercent) {
     browserSize=screenPercent;
  }

  public void setNewBrowserMenuBar(Class menuBarClass) {
    Browser.setMenuBarClass(menuBarClass);
  }

  public void setNewBrowserTitle (String title) {
     browserTitle=title;
  }

  public void setApplicationName (String name) {
    appName=name;
  }

  public String getApplicationName () {
    return appName;
  }

  public void setApplicationVersion (String version) {
    appVersion=version;
  }

  public String getApplicationVersion() {
    return appVersion;
  }

  public void setNewBrowserImageIcon(ImageIcon newImageIcon) {
     browserImageIcon = newImageIcon;
  }

  /**
   * Makes whole model read-only
   */
  public void makeReadOnly(){
     modelManager.makeReadOnly();
  }

  /**
  * Register an editor for a model type
  *
  */
  public void registerEditorForType(Class type, Class editor, String editorName, boolean defaultEditorForType) throws Exception {
    Browser.registerEditorForType(type,editor,editorName,defaultEditorForType);
  }


  /**
  * Register the editor for this type, but only under the specified protocol
  *
  */
  public void registerEditorForType(Class type, Class editor, String editorName, String protocol, boolean defaultEditorForType) throws Exception {
    if (FacadeManager.isProtocolRegistered(protocol))  Browser.registerEditorForType(type,editor,editorName,defaultEditorForType);
  }

  public void registerSubEditorForMainEditor(Class mainEditor, Class subEditor, String protocol) throws Exception {
    if (FacadeManager.isProtocolRegistered(protocol))  registerSubEditorForMainEditor(mainEditor, subEditor);
  }

  public void registerSubEditorForMainEditor(Class mainEditor, Class subEditor) throws Exception {
    Browser.registerSubEditorForMainEditor(mainEditor,subEditor);
  }

  public void handleException (Throwable throwable) {
     modelManager.handleException(throwable);
  }

  public Browser newBrowser(){
    Browser browser = new Browser(browserSize,sessionModel.addBrowserModel());
    browser.addWindowListener(myBrowserWindowListener);
    if (browserTitle!=null) {
      browser.setTitle(browserTitle);
    }
    if (browserImageIcon!=null) {
        browser.setBrowserImageIcon(browserImageIcon);
    }
    browser.setVisible(true);
    browserModelsToBrowser.put(browser.getBrowserModel(),browser);
    return browser;
  }

  public void removeBrowser(Browser browser) {
    browserModelsToBrowser.remove(browser.getBrowserModel());
    sessionModel.removeBrowserModel(browser.getBrowserModel());
  }

  public void useFreeMemoryWatcher(boolean use) {
      this.setModelProperty("FreeMemoryViewer",new Boolean(use));
      //freeMemoryWatcher=use;
  }
/*
  public boolean isUsingMemoryWatcher() {
     return freeMemoryWatcher;
  }
*/

  public void cloneBrowser (Browser browser) {
    Browser newBrowser=(Browser)browser.clone();
    newBrowser.addWindowListener(myBrowserWindowListener);
    sessionModel.addBrowserModel(newBrowser.getBrowserModel());
    newBrowser.setVisible(true);
    browserModelsToBrowser.put(newBrowser.getBrowserModel(),newBrowser);
  }


  public void systemExit() {
    systemExit(0);
  }

  public void systemExit(int errorlevel) {
    sessionModel.systemWillExit();
    writeSettings(); // Saves user preferences.
    sessionModel.removeAllBrowserModels();
    System.err.println("Memory in use at exit: "+(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1000000f+" MB");
    System.err.flush();
    modelManager.prepareForSystemExit();
    System.exit(errorlevel);
  }

  public void addSessionModelListener(SessionModelListener sessionModelListener) {
    sessionModel.addSessionListener(sessionModelListener);
  }

  public void removeSessionModelListener(SessionModelListener sessionModelListener) {
    sessionModel.removeSessionListener(sessionModelListener);
  }

  public void setSplashPanel(Component panel){
      splashPanel=panel;
  }

  public Component getSplashPanel(){
      return splashPanel;
  }

  /**
   * This method will be used to write out the Session Log in
   * time in the annotationLog file. Note the SessionLog in time
   * will be same if multiple browsers are open.
   */
  public Date getSessionCreationTime(){
    return sessionCreationTime;
  }


  public int getNumberOfOpenBrowsers() {
    return sessionModel.getNumberOfBrowserModels();
  }

  public void setLookAndFeel(String lookAndFeelClassName) {
    try {
      UIManager.setLookAndFeel(lookAndFeelClassName);
      Set browserModels=browserModelsToBrowser.keySet();
      Object obj;
      for (Iterator it=browserModels.iterator();it.hasNext();) {
         obj=browserModelsToBrowser.get(it.next());
         if (obj!=null) {
           SwingUtilities.updateComponentTreeUI((JFrame)obj);
           ((JFrame)obj).repaint();
         }
      }
      setModelProperty(DISPLAY_LOOK_AND_FEEL,lookAndFeelClassName);
    }
    catch (Exception ex) {
      handleException(ex);
    }
  }

  public Browser getActiveBrowser() {
     return activeBrowser;
  }

  public void startExternalListener(int port) {
     if (externalListener==null) externalListener=new ExternalListener(port);
  }

  public void resetSession() {
     Set keys=browserModelsToBrowser.keySet();
     List browserList=new ArrayList(keys.size());
     for (Iterator i=keys.iterator();i.hasNext(); ) {
        browserList.add(i.next());
     }
     Browser[] browsers=(Browser[])browserList.toArray(new Browser[0]);
     for (int i=0;i<browsers.length;i++) {
       browsers[i].closeAllViews();
       browsers[i].getBrowserModel().reset();
     }
     ModelMgr.getModelMgr().removeAllGenomeVersions();
     FacadeManager.resetFacadeManager();
  }

  public void stopExternalListener() {
     if (externalListener!=null) {
        externalListener.stop();
        externalListener=null;
     }
  }

  public Browser getBrowserFor(BrowserModel model) {
     return (Browser)browserModelsToBrowser.get(model);
  }

  public void addPropertyCreationRule(PropertyCreationRule rule) {
    PropertyMgr.getPropertyMgr().addPropertyCreationRule(rule);
    Set rules=(Set)getModelProperty(PROPERTY_CREATION_RULES);
    if (rules==null) rules = new HashSet();
    rules.add(rule);
    setModelProperty(PROPERTY_CREATION_RULES,rules);
  }

  public Set getPropertyCreationRules() {
    return PropertyMgr.getPropertyMgr().getPropertyCreationRules();
  }

  public void removePropertyCreationRule(String ruleName){
    PropertyMgr.getPropertyMgr().removePropertyCreationRule(ruleName);
    Set rules=(Set)getModelProperty(PROPERTY_CREATION_RULES);
    PropertyCreationRule rule;
    for (Iterator it=rules.iterator();it.hasNext();){
      rule=(PropertyCreationRule)it.next();
      if (rule.getName().equals(ruleName)) it.remove();
    }
  }

  private void writeSettings() {
    try {
     settingsFile.delete();
     ObjectOutputStream ostream= new ObjectOutputStream(new FileOutputStream(settingsFile));
     ostream.writeInt(1);  //stream format
     if(backupFileName!=null){
       setModelProperty(USER_NEW_BACKUP_FILE_NAME,backupFileName);
     }else{
     //  setModelProperty(USER_BACKUP_FILE_NAME,XMLWriter.getXMLWriter().getWorkspaceBackupFileName());
     }
     setModelProperty(USER_CURRENT_BACKUP_FILE_NAME,XMLWriter.getXMLWriter().getWorkspaceBackupFileName());

     ostream.writeObject(sessionModel.getModelProperties());
     ostream.flush();
     ostream.close();
   }
   catch (IOException ioEx) {
    SessionMgr.getSessionMgr().handleException (ioEx);
   }
  }


  public void setBackupFileName(String userChosenLocation){
   backupFileName=userChosenLocation;
  }

  class MyBrowserListener extends WindowAdapter {
      public void windowClosed(WindowEvent e) {
        e.getWindow().removeWindowListener(this);
      }
      public void windowActivated(WindowEvent e){
        activeBrowser=(Browser)e.getWindow();
      }
  }

}
