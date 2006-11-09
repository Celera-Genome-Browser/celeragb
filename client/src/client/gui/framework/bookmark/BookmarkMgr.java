// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package client.gui.framework.bookmark;

import java.util.*;
import java.io.*;
import javax.swing.*;

import client.gui.framework.navigation_tools.AutoNavigationMgr;
import client.gui.framework.session_mgr.*;

import shared.preferences.*;

import api.stub.data.*;

public class BookmarkMgr extends PreferenceManager{

  private static BookmarkMgr bookmarkMgr;
  private TreeMap bookmarkCollection = new TreeMap();

  private BookmarkMgr() {
    super();
    DEBUG = false;
    userFileDescription = "Genome Browser Bookmarks";
    // Set up the necessary attributes.
    setFileParameters();

    // This listener is to hear when the client session is exiting and save.
    SessionMgr.getSessionMgr().addSessionModelListener(new MySessionModelListener());
    initializeMasterInfoObjects();
    resetWorkingCollections();
  }


  /**
   * Inputs the required values to make the mechanism work.
   */
  public void setFileParameters() {
    this.filenameFilter  = "_Bookmarks.properties";
    this.defaultFilename = "";

    // Check for the existence of the group directory property and/or set values.
    String groupDir = "";
    if (SessionMgr.getSessionMgr().getModelProperty(GROUP_DIR)==null) {
      SessionMgr.getSessionMgr().setModelProperty(GROUP_DIR, groupDir);
    }
    else groupDir = (String)SessionMgr.getSessionMgr().getModelProperty(GROUP_DIR);
    setGroupPreferenceDirectory(groupDir);

    // Establish the initial "default" user file.
    this.userDirectory = new String(System.getProperty("user.home")+File.separator+
      "x"+File.separator+"GenomeBrowser"+File.separator);

    this.userFilename = userDirectory + "User_Bookmarks.properties";
  }

  public void addBookmarkListener(BookmarkListener listener) {
     if (listeners==null) listeners=new ArrayList();
     listeners.add(listener);
  }

  public void removeBookmarkListener(BookmarkListener listener) {
     if (listeners==null) return;
     listeners.remove(listener);
     if (listeners.isEmpty()) listeners=null;
  }

  public static BookmarkMgr getBookmarkMgr() {
    if (bookmarkMgr==null) bookmarkMgr=new BookmarkMgr();
    return bookmarkMgr;
  }

  public TreeMap getBookmarks() {
     return bookmarkCollection;
  }

  public void addBookmark(BookmarkInfo bookmark) {
      if (bookmark.getOid().isScratchOID()) {
        JOptionPane.showMessageDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
          "Bookmarking Workspace features is not allowed.",
          "Unable To Create Bookmark",JOptionPane.ERROR_MESSAGE);
        return;
      }
      bookmarkCollection.put(bookmark.getName(),bookmark);
      fireBookmarksChanged();
  }

  public void deleteBookmark(BookmarkInfo bookmark) {
    String tmpName = bookmark.getName();
    deletedInfos.put(bookmark.getKeyName(), bookmark);
    bookmarkCollection.remove(tmpName);
  }

  /**
   * This method is a no op to this bookmark class as there are no default bookmarks.
   */
  public void handleDefaultKeyOverrideRequest() {}

  protected void handleOutputWriteError(){
    /**
     * @todo need to figure out what to do with write errors.
     */
  }

  public void fireBookmarksChanged() {
   if (listeners!=null) {
     for (Iterator i=listeners.iterator();i.hasNext(); ){
        ((BookmarkListener)i.next()).bookmarksChanged();
     }
   }
  }


  /**
   * This method is used by the Bookmark menu and designates the display name
   * to be the selection switch.
   */
  public void selectBookmark(String bookmarkDisplayName) {
    for (Iterator it = bookmarkCollection.keySet().iterator(); it.hasNext(); ) {
      BookmarkInfo tmpInfo = (BookmarkInfo)bookmarkCollection.get(it.next());
      if (tmpInfo.getDisplayName()!=null && tmpInfo.getDisplayName().equals(bookmarkDisplayName)) {
        selectBookmark((BookmarkInfo)bookmarkCollection.get(tmpInfo.getName()));
        return;
      }
    }
    System.out.println("The bookmark cannot be found.");
  }


  public void selectBookmark(BookmarkInfo bookmark) {

     NavigationPath navPath;
     try {
       navPath=bookmark.getNavigationPath();
     }
     catch (InvalidPropertyFormat ipfEx) {
        JOptionPane.showMessageDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
          ipfEx.getMessage(),"Information",JOptionPane.INFORMATION_MESSAGE);
        return;
     }
     if (navPath==null) {
        if (bookmark.getOid().isScratchOID())
            JOptionPane.showMessageDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
              "This bookmark for "+bookmark.getOid()+
              " is for a Workspace feature.\nNavigation is not allowed.",
              "Unable To Navigate",JOptionPane.ERROR_MESSAGE);
        else
            JOptionPane.showMessageDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
              "This bookmark cannot be found.\nPerhaps the data source is no longer available.",
              "Unable To Navigate",JOptionPane.ERROR_MESSAGE);
        return;
     }
     AutoNavigationMgr.getAutoNavigationMgr().navigate(SessionMgr.getSessionMgr().getActiveBrowser(),
       navPath);
  }


  /**
   * This method will send all of its proprietary collections to the base class method
   * formatOutput(), one-by-one.
   */
  protected void writeOutAllCollections(FileWriter writer, String destinationFile) {
    addCollectionToOutput(writer, bookmarkCollection, destinationFile);
  }


  /**
   * This method is supposed to hierarchially add info objects to the final
   * collections.  Any deletion should be a cause for remerging; for example, if
   * a user defined info object is removed it could be replaced with a group
   * or default object.  Currently, this manager only knows about BookmarkInfo
   * objects.
   */
  protected void mergeIntoWorkingCollections(Map targetMasterCollection) {
    for (Iterator it = targetMasterCollection.keySet().iterator();it.hasNext();) {
      InfoObject tmpObject = (InfoObject)((InfoObject)targetMasterCollection.get((String)it.next())).clone();
      if (tmpObject instanceof BookmarkInfo) {
        bookmarkCollection.put(tmpObject.getName(), tmpObject);
      }
    }
  }


  /**
   * This method sifts through the Java property keys and puts them in sub-groups by
   * type; in this case only Bookmarks.  It then passes those Properties objects
   * on to the build methods.  Remember this is for only one source,
   * Default or User, at a time.
   */
  protected void populateInfoObjects(Properties allProperties, Map targetMasterCollection,
                                   String sourceFile) {
    Properties allBookmarkProperties=new Properties();
    //Separate all properties into the separate categories
    for (Enumeration enum=allProperties.propertyNames();enum.hasMoreElements();) {
      String tempKey = new String ((String)enum.nextElement());
      StringTokenizer mainToken = new StringTokenizer(tempKey,".");
      if (tempKey!=null && mainToken!=null && tempKey!="") {
        String firstToken = new String (mainToken.nextToken());
        String tmpValue = (String)allProperties.get(tempKey);
        String remainingString;
        if (!mainToken.hasMoreTokens()) remainingString = "";
        else remainingString = tempKey.substring(firstToken.length()+1);
        if (firstToken.equals("Bookmark")) allBookmarkProperties.setProperty(remainingString, tmpValue);
        else if (DEBUG) System.out.println("This key is not known: "+firstToken);
      }
    }
    // This area constructs the Info Objects for this source and adds them to the
    // specific Master collection.
    buildBookmarks(allBookmarkProperties, targetMasterCollection, sourceFile);
  }


   /**
    * @todo Need to give the base class an abstract method to make
    * this work properly.
    */
  protected void commitChangesToSourceObjects() {
    // First remove the unwanteds.  Only need to check top of hierarchy on down.
    // If the object is found in the user map, bingo.
    super.commitChangesToSourceObjects();
    handleDirtyOrUnknownInfoObjects(bookmarkCollection);
    resetWorkingCollections();
  }


  /**
   * Clears the Working collections of Info Objects and rebuilds from the
   * Master collections; merging them into their usable state.
   * Clear out specific InfoObject collection(s) before calling super.
   */
  public void resetWorkingCollections() {
    bookmarkCollection = new TreeMap(new MyStringComparator());
    //  Call the superclass which will merge the collections.
    super.resetWorkingCollections();
  }


  /**
   * Builds the BookmarkInfo objects for the given Master collection.
   */
  private void buildBookmarks(Properties bookmarkProperties, Map targetMasterCollection, String sourceFile) {
    Set uniqueBookmarks = getUniqueKeys(bookmarkProperties);
    for (Iterator it = uniqueBookmarks.iterator();it.hasNext();) {
      String nameBase = new String((String)it.next());
      BookmarkInfo tmpBookmarkInfo = new BookmarkInfo(nameBase, bookmarkProperties, sourceFile);
      targetMasterCollection.put("Bookmark."+nameBase, tmpBookmarkInfo);
    }
  }


  private class MySessionModelListener implements SessionModelListener {
    public void browserAdded(BrowserModel browserModel) {}
    public void browserRemoved(BrowserModel browserModel){}
    /**
     * This method first commits any changes to be safe, and then sets all dirty
     * infos to the selected writeback file.  Then for each selection in the
     * writebackFileCollection (sources that have had thier infos changed)
     * the method calls writeOutToDestinationFile.
     */
    public void sessionWillExit() {
      closeOutCurrentUserFile();
    }
    public void modelPropertyChanged(Object key, Object oldValue, Object newValue){
      if (key.equals(GROUP_DIR)) {
        String groupDirectory = "";
        if (newValue !=null) groupDirectory = (String)newValue + File.separator;
        // Make the change, flush and reset the preferences.
        setGroupPreferenceDirectory(groupDirectory);
        fireBookmarksChanged();
      }
    }
  }
}
