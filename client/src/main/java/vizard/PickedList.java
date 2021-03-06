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

import java.awt.geom.AffineTransform;
import java.util.ArrayList;


public class PickedList
{
    ArrayList list;

    public PickedList(Glyph g, GraphicContext gc) {
        list = new ArrayList();
	add(g, gc);
    }

    public PickedList(PickedList original) {
        list = new ArrayList(original.list);
    }

    public PickedList add(Glyph g, GraphicContext gc) {
	list.add(g);
	list.add(new AffineTransform(gc.getTransform()));
	return this;
    }

    public int count() {
	return list.size() / 2;
    }

    public Glyph glyph(int i) {
	return (Glyph)list.get(2 * i);
    }

    public AffineTransform transform(int i) {
	return (AffineTransform)list.get(2 * i + 1);
    }

    public RootGlyph lastGlyph() {
	return (RootGlyph)glyph(count() - 1);
    }
}
