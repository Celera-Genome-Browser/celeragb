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
package vizard;

import vizard.util.ObjectWithPreferences;
import vizard.util.Preferences;

/**
 * If a swing component is to be used as a glyph container,
 * it must implement the GlyphContainer interface.
 *
 * @see GlyphComponent
 */
public interface GlyphContainer extends ObjectWithPreferences {
   /**
    * Return the root glyph associated to this component.
    */
   RootGlyph rootGlyph();

   /**
    * Notify this object that its preferences have changed.
    *
    * The concrete implementation is supposed at least to forward
    * the call to its root glyph.
    */
   void preferencesChanged(Preferences oldPrefs);

   /**
    * Ask this object (and all sub-objects) to add its preferences
    * to the given prefs.
    */
   void getPreferences(Preferences prefs);

   /**
    * Ask the glyph-container to delete itself.
    */
   void delete();
}
