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
package vizard.glyph;

import vizard.Bounds;
import vizard.Glyph;
import vizard.GraphicContext;
import vizard.PickedList;

import java.awt.*;

/**
 * The purpose of the GlyphAdapter is to provide no-op implementation
 * for the abstract glyph methods and thus allowing subclasses to
 * specialize only those method that actually do something.
 */
public abstract class GlyphAdapter extends Glyph {
   /**
    * Nothing is painted.
    */
   public void paint(GraphicContext gc) {
   }

   /**
    * No bounds are added.
    */
   public void addBounds(Bounds bounds) {
   }

   /**
    * The GlyphAdapter is not picked.
    */
   public PickedList pick(GraphicContext gc, Rectangle deviceRect) {
      return null;
   }
}
