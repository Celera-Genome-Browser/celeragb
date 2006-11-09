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
package vizard.component;

import vizard.EventDispatcher;
import vizard.GlyphContainer;
import vizard.RootGlyph;
import vizard.util.Preferences;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;


/**
 * GlyphComponent is a JPanel that is also a glyph container.
 */
public class GlyphComponent extends JPanel
    implements GlyphContainer
{
    RootGlyph root;

    /**
     * Initialize a new GlyphComponent.
     */
    public GlyphComponent() {
	root = new RootGlyph(this);
	EventDispatcher.instance.listen(this);
    }

    /**
     * Return the root glyph.
     */
    public RootGlyph rootGlyph() {
	return root;
    }

    /**
     * "Delete" this component.
     *
     * The component forwards the delete to its root-glyph.
     */
    public void delete() {
	root.delete();
    }

    /**
     * Delegates the swing paint request to its root glyph.
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
		try {
			root.paint((Graphics2D)g);
		}
		catch (Exception cme) {
			//todo Fix this problem!!!  For right now, eat it.
			System.out.println(cme.getMessage());
		}
    }


    // ObjectWithPreferences specialization

    public void preferencesChanged(Preferences oldPrefs) {
    }

    public void getPreferences(Preferences prefs) {
	root.getPreferences(prefs);
    }
}
