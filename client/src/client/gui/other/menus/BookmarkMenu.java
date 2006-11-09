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

import api.entity_model.model.fundtype.GenomicEntity;
import client.gui.framework.bookmark.BookmarkInfo;
import client.gui.framework.bookmark.BookmarkListener;
import client.gui.framework.bookmark.BookmarkMgr;
import client.gui.framework.bookmark.BookmarkTableDialog;
import client.gui.framework.browser.Browser;
import client.gui.framework.session_mgr.BrowserModelListenerAdapter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;


public class BookmarkMenu extends JMenu implements BookmarkListener {

  public Browser browser;
  private MyBrowserListener browserListener = new MyBrowserListener();
  private GenomicEntity lastSelection;
  private JMenuItem addBookmarkMI, bookmarkMI;

  public BookmarkMenu(Browser b) {
    this.browser=b;
    browser.getBrowserModel().addBrowserModelListener(browserListener);
    BookmarkMgr.getBookmarkMgr().registerPrefMgrListener(this);
    setText("Bookmark");
    this.setMnemonic('B');

    addBookmarkMI=new JMenuItem("Bookmark Current Selection", 'B');
    addBookmarkMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK, false));
    addBookmarkMI.setEnabled(false);
    addBookmarkMI.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
        BookmarkMgr.getBookmarkMgr().addBookmark(new BookmarkInfo(lastSelection));
      }
    });

    bookmarkMI=new JMenuItem("Select Bookmark...", 'r');
    bookmarkMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK, false));
    bookmarkMI.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
         new BookmarkTableDialog(browser, BookmarkMgr.getBookmarkMgr().getBookmarks());
      }
    });
    createMenu();
  }

  public void bookmarksChanged () {
    createMenu();
  }
  public void preferencesChanged() {}

  private void createMenu() {
    removeAll();
    add(addBookmarkMI);
    add(bookmarkMI);
   }


  public void dispose() {
    browser.getBrowserModel().removeBrowserModelListener(browserListener);
  }


  class MyBrowserListener extends BrowserModelListenerAdapter {
    public void browserCurrentSelectionChanged(GenomicEntity newSelection){
      lastSelection=newSelection;
      if (lastSelection!=null && addBookmarkMI!=null && !newSelection.hasNullOID()) addBookmarkMI.setEnabled(true);
      if (lastSelection!=null && addBookmarkMI!=null && newSelection.hasNullOID()) addBookmarkMI.setEnabled(false);
      if (lastSelection==null && addBookmarkMI!=null) addBookmarkMI.setEnabled(false);
    }
  }
}
