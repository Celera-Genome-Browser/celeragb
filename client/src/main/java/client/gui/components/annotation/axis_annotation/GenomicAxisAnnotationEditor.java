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
/**
 *
 * CVS_ID:  $Id$
 */

package client.gui.components.annotation.axis_annotation;

import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.GenomicEntity;
import client.gui.framework.browser.Browser;
import client.gui.framework.roles.Editor;
import client.gui.framework.session_mgr.BrowserModel;

import javax.swing.*;
import java.awt.*;

public class GenomicAxisAnnotationEditor extends JPanel implements Editor {

    protected BrowserModel browserModel;
    protected GenomicAxisAnnotationView saView;
    protected boolean isMaster = false;
    protected GenomicAxisAnnotCurationHandler curHandler;
    //private SessionMgr sessionManager = SessionMgr.getSessionMgr();

    // editor now IS the controller
    public GenomicAxisAnnotationEditor(Browser browser, Boolean bool) {
      isMaster = bool.booleanValue();
      saView = new GenomicAxisAnnotationView(browser, isMaster);
      browserModel = browser.getBrowserModel();
      this.setLayout(new BorderLayout());
      this.add("Center", saView);
      this.setName("Genomic Axis Annotation");

      curHandler = new GenomicAxisAnnotCurationHandler(browser, this);
    }

    // Editor implementation
    public void dispose() {
        if (curHandler != null) { curHandler.dispose(); }
        if (saView != null) { saView.dispose(); }
        browserModel = null;
        saView = null;
        curHandler = null;
    }

    public JMenuItem[] getMenus() {
        JMenu[] view_menus = saView.getMenus();
        JMenu[] editor_menus = new JMenu[view_menus.length];
        int index = 0;
        for (int i = 0; i < view_menus.length; i++) {
            editor_menus[index] = view_menus[i];
            index++;
        }
        return editor_menus;
    }


    public boolean canEditThisEntity(GenomicEntity entity) {
      if (entity instanceof GenomicAxis) return true;
      else return false;
    }

    GenomicAxisAnnotationView getView() {
        return saView;
    }
}
