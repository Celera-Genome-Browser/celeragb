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
package client.gui.components.annotation.axis_annotation;

import client.gui.framework.pref_controller.PrefController;
import vizard.genomics.glyph.TierGlyph;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;


public class TierPopupMenu extends JPopupMenu
{
    private GenomicAxisAnnotationView view;
    private TierGlyph currentTier;

    /** One of the included-by-default menu items: */
    public final JMenuItem expandMI   = new JMenuItem("Expand");

    /** One of the included-by-default menu items: */
    public final JMenuItem collapseMI = new JMenuItem("Collapse");

    /** One of the included-by-default menu items: */
    public final JMenuItem dockMI     = new JMenuItem("Dock");

    /** One of the included-by-default menu items: */
    public final JMenuItem undockMI     = new JMenuItem("Undock");

    /** One of the included-by-default menu items: */
    public final JMenuItem hideMI     = new JMenuItem("Hide");

    public final JMenuItem editTierMI     = new JMenuItem("Edit Tiers");

    public final JMenuItem editFeatureMI     = new JMenuItem("Edit Features");

    private JSeparator menuSpearator = new JSeparator();
    /** Create a new instance of the popup menu, for the given
    ActionListener.  All default menu items are added. */

    public TierPopupMenu (GenomicAxisAnnotationView aView) {
        super ("Tier Action");
        this.view = aView;

	expandMI.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) { view.expandTier(currentTier); }});
	collapseMI.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) { view.collapseTier(currentTier); }});
	dockMI.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) { view.dockTier(currentTier); }});
	undockMI.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) { view.undockTier(currentTier); }});
	hideMI.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) { view.hideTier(currentTier); }});
	editTierMI.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    PrefController.getPrefController().getPrefInterface
			(client.gui.other.panels.TierPanel.class, view.getBrowser());
		}});
	editFeatureMI.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    PrefController.getPrefController().getPrefInterface
			(client.gui.other.panels.FeaturePanel.class, view.getBrowser());
		}});

        addMI(collapseMI);
        addMI(expandMI);
        addMI(dockMI);
        addMI(undockMI);
        addMI(hideMI);
        add(menuSpearator);
        addMI(editFeatureMI);
        addMI(editTierMI);
    }

    /**
     * Remove a given menu item from the popup menu
     */
    public void removeMI (JMenuItem toRemove) {
        remove (toRemove);
        validate();
    }

    /**
     * Add a given menu item to the popup menu
     */
    public void addMI(JMenuItem toAdd) {
        add(toAdd);
        validate();
    }

    public void show(TierGlyph mapTier, Component invoker, int x_loc, int y_loc) {
        buildMenu(mapTier);
        super.show(invoker, x_loc, y_loc);
    }

    private void buildMenu(TierGlyph mapTier) {
        currentTier = mapTier;

        remove(menuSpearator);
        removeMI(collapseMI);
        removeMI(expandMI);
        removeMI(dockMI);
        removeMI(undockMI);
        removeMI(hideMI);
        removeMI(editFeatureMI);
        removeMI(editTierMI);

        if (!mapTier.name().equals(view.AXIS_TIER_NAME)) {
            if (mapTier.isExpanded())
                addMI(collapseMI);
            else
                addMI(expandMI);

            if (GenomicAxisAnnotationView.getTierInfo(mapTier.name()).getDocked())
                addMI(undockMI);
            else
                addMI(dockMI);

            addMI(hideMI);
        }

        add(menuSpearator);
        addMI(editFeatureMI);
        addMI(editTierMI);
    }
}
