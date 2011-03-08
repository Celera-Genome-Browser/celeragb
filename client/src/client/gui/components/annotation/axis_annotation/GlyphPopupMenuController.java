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
package client.gui.components.annotation.axis_annotation;

import client.gui.framework.bookmark.BookmarkInfo;
import client.gui.framework.bookmark.BookmarkMgr;
import client.gui.other.menus.FeatureLinkMenu;
import client.gui.other.util.ClipboardUtils;
import vizard.EventDispatcher;
import vizard.Glyph;
import vizard.interactor.ClickInteractor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

public class GlyphPopupMenuController extends Controller
    implements ClickInteractor.Adapter
{
    private JMenuItem featureLinkItem = new JMenuItem("Copy Link To Feature");
    private JMenuItem bookmarkItem = new JMenuItem("Bookmark Feature");
    private JMenu cdsLinksMI = new JMenu("CDS Links");
    private GenomicAxisAnnotCurationHandler curationHandler;
    private ClickInteractor interactor;

    public GlyphPopupMenuController(GenomicAxisAnnotCurationHandler handler) {
	super(handler.getView());
        curationHandler = handler;

        interactor = new ClickInteractor(this);
        interactor.activeWithLeftButton = false;

	EventDispatcher.instance.addInteractor(GBGenomicGlyph.class, interactor,
            new EventDispatcher.Filter() {
              public boolean isValid(Glyph glyph) {
                if (glyph == null || glyph.getRootGlyph()==null) return false;
                Component comp = (Component)glyph.getRootGlyph().container();
                return EventDispatcher.hasAncestor(comp, curationHandler.getView());
              }
        });
        featureLinkItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String url = BookmarkInfo.getURLForEntity(curationHandler.getCurrentSelection());
            ClipboardUtils.setClipboardContents(url);
          }
        });

        bookmarkItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            BookmarkMgr.getBookmarkMgr().addBookmark(
              new BookmarkInfo(curationHandler.getCurrentSelection()));
          }
        });
    }

    public void delete() {
	EventDispatcher.instance.removeInteractor(GBGenomicGlyph.class, interactor);

        super.delete();
    }

    //ClickInteractor adapter specialization

    public void clicked(ClickInteractor itor) {
        JPopupMenu popupMenu = new JPopupMenu();
        GBGenomicGlyph gg = (GBGenomicGlyph)itor.glyph();
        JComponent component = (JComponent)EventDispatcher.instance.root().container();
	Point windowLocation = new Point(itor.event().getX(), itor.event().getY());

	Point2D p = new Point2D.Double(windowLocation.getX(), windowLocation.getY());
	try { itor.transform().inverseTransform(p, p); }
	catch(NoninvertibleTransformException ex) {}
	int axisLocation = (int)p.getX();

        // Gather all interested menu items
        ArrayList popupMenuItems = new ArrayList();

	// Check to see if curation handler has anything to add.
        popupMenuItems.addAll(curationHandler.getPopupMenuItems(component, gg, windowLocation, axisLocation));
        for (Iterator it = popupMenuItems.iterator(); it.hasNext(); ) {
          popupMenu.add((Component)it.next());
        }

        // Add Separator if necessary.
        if (popupMenu.getSubElements() != null && popupMenu.getSubElements().length != 0) {
          popupMenu.add(new JSeparator());
        }

        // Add default feature menu items.
        popupMenu.add(bookmarkItem);
        popupMenu.add(featureLinkItem);
        popupMenu.add(new JSeparator());

        // Set up CDS Link menu items.  Dynamically generated.
        cdsLinksMI = new FeatureLinkMenu(curationHandler.getCurrentSelection());
        popupMenu.add(cdsLinksMI);

        // Present options to user.
        popupMenu.show(component, windowLocation.x, windowLocation.y);
    }
}
