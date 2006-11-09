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
package client.gui.application.genome_browser;

import client.gui.framework.browser.Browser;
import client.gui.framework.browser.BrowserMenuBar;
import client.gui.other.menus.BookmarkMenu;
import client.gui.other.menus.SearchMenu;

import javax.swing.ImageIcon;
import javax.swing.JMenu;


public class GenomeBrowserMenuBar extends BrowserMenuBar {
    private JMenu editMenu;
    private JMenu helpMenu;
    private JMenu searchMenu;
    private JMenu bookmarkMenu;

    public GenomeBrowserMenuBar(Browser browser) {
        super(browser);

// JCVI LLF: 10/19/2006
        // RT 10/28/2006
		this.setAnimationIcon(
                new ImageIcon(GenomeBrowserMenuBar.class.getResource(
                                      "/resource/client/images/dna2Animation_trans.gif")));
        this.setStaticIcon(
                new ImageIcon(GenomeBrowserMenuBar.class.getResource(
                                      "/resource/client/images/dna2_trans.gif")));
        constructMenus();
        addMenus();
    }

    private void constructMenus() {
        editMenu = new EditMenu(browser);
        helpMenu = new HelpMenu();
        searchMenu = new SearchMenu(browser);
        bookmarkMenu = new BookmarkMenu(browser);
    }

    private void addMenus() {
        add(editMenu, AFTER_FILE);
        add(searchMenu, AFTER_FILE);
        add(bookmarkMenu, AFTER_FILE);
        add(helpMenu, AFTER_EDITOR_SPECIFIC);
    }
}