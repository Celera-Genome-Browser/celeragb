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
package client.gui.framework.navigation_tools;

import api.entity_model.access.observer.NavigationObserver;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.genetics.GenomeVersion;
import api.stub.data.ControlledVocabulary;
import api.stub.data.NavigationPath;
import client.gui.framework.browser.Browser;
import client.gui.framework.session_mgr.SessionMgr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

public class AutoNavigationMgr {

  private static AutoNavigationMgr autoNavMgr;
  public static final String PATH_DISCOVERED   = "Path Discovered";
  public static final String SEARCH_INITIATED  = "Searching...";
  public static final String SEARCH_COMPLETE   = "Search Complete";

  private String lastSearchType = new String("");
  private String lastSearchString = new String("");

  private static final String NAV_COMPLETE_KEY="AutoNavigationMgr.NavigationComplete";

  private AutoNavigationMgr() {  }

  public static AutoNavigationMgr getAutoNavigationMgr() {
    if (autoNavMgr==null) autoNavMgr=new AutoNavigationMgr();
    return autoNavMgr;
  }

  public void showNavigationCompleteMsgs(boolean show) {
    SessionMgr.getSessionMgr().setModelProperty(NAV_COMPLETE_KEY,new Boolean(show));
  }

  public boolean isShowingNavigationCompleteMsgs() {
     Boolean bool=(Boolean)SessionMgr.getSessionMgr().getModelProperty(NAV_COMPLETE_KEY);
     if (bool==null) return false;
     return bool.booleanValue();
  }


  // Returns a boolean about successful path discovery.
  public void findEntityInSelectedGenomeVersions(Browser browser, String searchType, String searchString) {
    Set selectedGenomeVersions=ModelMgr.getModelMgr().getSelectedGenomeVersions();
    GenomeVersion genomeVersion;
    NavigationObserver observer=new MyNavigationObserver(browser,selectedGenomeVersions.size());
    lastSearchType = searchType;
    lastSearchString = searchString;
    browser.getBrowserModel().setModelProperty(SEARCH_INITIATED, null);
    for (Iterator it=selectedGenomeVersions.iterator(); it.hasNext(); ){
      genomeVersion=(GenomeVersion)it.next();
      try{
        genomeVersion.getNavigationPathsInThisGenomeVersionBackground(
          searchType,searchString,observer);
      }
      catch (Exception ex) {
        SessionMgr.getSessionMgr().handleException(ex);
      }
    }
    browser.getBrowserModel().setModelProperty(SEARCH_COMPLETE, null);
  }

  // Returns a boolean about successful path discovery.
  public void findEntityInGenomeVersion(Browser browser, String searchType,
          String searchString, GenomeVersion genomeVersion) {
    lastSearchType = searchType;
    lastSearchString = searchString;
    browser.getBrowserModel().setModelProperty(SEARCH_INITIATED, null);
    try {
      genomeVersion.
        getNavigationPathsInThisGenomeVersionBackground(searchType,
          searchString,new MyNavigationObserver(browser,1));
    }
    catch (Exception ex) {
      SessionMgr.getSessionMgr().handleException(ex);
    }
  }

  // Returns a boolean about successful path discovery.
  public void findEntity(Browser browser, String searchType, String searchString) {
    lastSearchType = searchType;
    lastSearchString = searchString;
    browser.getBrowserModel().setModelProperty(SEARCH_INITIATED, null);
    try{
      GenomeVersion.getNavigationPathsBackground(searchType,searchString,
        new MyNavigationObserver(browser,1));
    }
    catch (Exception ex) {
      SessionMgr.getSessionMgr().handleException(ex);
    }
  }


  public void navigate(Browser browser,NavigationPath path) {
    if (path!=null) {
        AutoNavigator av=new AutoNavigator(browser);
        av.autoNavigate(path,isShowingNavigationCompleteMsgs());
    }
  }


  public void navigate (NavigationPath path) {
     navigate(SessionMgr.getSessionMgr().newBrowser(),path);
  }

  public ControlledVocabulary getNavigationSearchTypes() {
    return GenomeVersion.getNavigationSearchTypes();
  }

  /* Opens dialog, and gets search info from user.  Will then autoNavigate*/
  public void queryUserForSearchThenNavigate(Browser browser) {
      //pop up user query
      SearchManager.getSearchManager().showSearchDialog(browser, false);
  }

  private class MyNavigationObserver implements NavigationObserver {
    Browser browser;
    int expectedNotifications=0;
    int receivedNotifications=0;
    List navigationPathList=new ArrayList();

    private MyNavigationObserver(Browser browser,int expectedNotifications){
      this.browser=browser;
      this.expectedNotifications=expectedNotifications;
    }


    public void noteNavigationPathsArrived(NavigationPath[] navigationPaths,
                                            String searchType, String searchString){
      receivedNotifications++;

      //  Broadcast hits.
      browser.getBrowserModel().setModelProperty(PATH_DISCOVERED, navigationPaths);

      navigationPathList.addAll(Arrays.asList(navigationPaths));
      if (receivedNotifications==expectedNotifications) {
        browser.getBrowserModel().setModelProperty(SEARCH_COMPLETE, null);
      }
    }

    public void noteNavigationError(String errorMessage){
        receivedNotifications++;
        JOptionPane.showMessageDialog(SearchManager.getSearchManager().getSearchDialog(),
          "The server returned the message: \n"+errorMessage,
          "Not Found", JOptionPane.INFORMATION_MESSAGE);
    }
  }

}
