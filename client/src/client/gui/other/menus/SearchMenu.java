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

package client.gui.other.menus;

import api.entity_model.management.ModelMgr;
import api.entity_model.model.fundtype.GenomicEntity;
import api.stub.geometry.Range;
import client.gui.framework.browser.Browser;
import client.gui.framework.navigation_tools.AutoNavigationMgr;
import client.gui.framework.navigation_tools.SequenceAnalysisDialog;
import client.gui.framework.pref_controller.PrefController;
import client.gui.framework.session_mgr.BrowserModelListenerAdapter;
import client.gui.framework.session_mgr.SessionMgr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
*
* Initially writted by: Peter Davies
*
*/
public class SearchMenu extends JMenu {

  private Browser browser;
  private MyBrowserListener browserListener = new MyBrowserListener();
  private GenomicEntity lastSelection;
  private JMenuItem seqAnalysis=new JMenuItem("Sequence Analysis...", 'y');
  private JMenuItem knownFeatures=new JMenuItem("Features...", 'a');

  public SearchMenu(Browser browser) {
    this.browser=browser;
    browser.getBrowserModel().addBrowserModelListener(browserListener);
    setText("Search");
    this.setMnemonic('S');

    knownFeatures.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK, false));
    knownFeatures.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
        search_actionPerformed(e);
      }
    });
    add(knownFeatures);

    seqAnalysis.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK, false));
    seqAnalysis.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
        SequenceAnalysisDialog.getSequenceAnalysisDialog().showSearchDialog();
      }
    });
    add(seqAnalysis);
    if (browser.getBrowserModel().getMasterEditorEntity()==null) seqAnalysis.setEnabled(false);
   }


  public void dispose() {
    browser.getBrowserModel().removeBrowserModelListener(browserListener);
  }




  private void search_actionPerformed(ActionEvent e) {
// JCVI LLF, 10/23/2006
//    if (SessionMgr.getSessionMgr().getModelProperty(SessionMgr.USER_NAME)==null ||
//      SessionMgr.getSessionMgr().getModelProperty(SessionMgr.USER_NAME).equals("") &&
//      ModelMgr.getModelMgr().getNumberOfLoadedGenomeVersions()==0) {
//      int answer = JOptionPane.showConfirmDialog(browser,"Please enter your CDS login information.",
//        "Information Required", JOptionPane.OK_CANCEL_OPTION);
//      if (answer==JOptionPane.CANCEL_OPTION) return;
//      PrefController.getPrefController().getPrefInterface(client.gui.other.panels.DataSourceSettings.class, browser);
//    }
//    // Double check.  Exit if still empty or not useful.
//    if (SessionMgr.getSessionMgr().getModelProperty(SessionMgr.USER_NAME)==null ||
//      SessionMgr.getSessionMgr().getModelProperty(SessionMgr.USER_NAME).equals("") &&
//      ModelMgr.getModelMgr().getNumberOfLoadedGenomeVersions()==0) {
//      return;
//    }
    AutoNavigationMgr.getAutoNavigationMgr().queryUserForSearchThenNavigate(browser);
  }


  class MyBrowserListener extends BrowserModelListenerAdapter {
    public void browserSubViewFixedRangeChanged(Range subViewFixedRange) {
      if (subViewFixedRange == null || subViewFixedRange.getMagnitude()==0)
        seqAnalysis.setEnabled(false);
      else seqAnalysis.setEnabled(true);
    }

    public void browserMasterEditorEntityChanged(GenomicEntity masterEditorEntity) {
      if (masterEditorEntity != null) seqAnalysis.setEnabled(true);
      else seqAnalysis.setEnabled(false);
    }

    public void browserCurrentSelectionChanged(GenomicEntity newSelection){
      lastSelection=newSelection;
    }
  }
}
