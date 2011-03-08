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
package vizard.genomics.glyph;

import vizard.Glyph;
import vizard.ParentGlyph;
import vizard.genomics.model.EntityAdapter;
import vizard.glyph.BoundsGlyph;

import java.util.ArrayList;
import java.util.List;


/**
 * The purpose of GenomicGlyph is to implement the functionality
 * that is common to all genomic glyphs.
 *
 * A genomic glyph has a start, an end, and a height.
 * A genomic glyph can have genomic glyph children.
 *
 * Note that this framework encourages the rendering of a genomic
 * glyph to be performed by a painter child.
 */
public abstract class GenomicGlyph extends BoundsGlyph
    implements EntityAdapter
{
    /**
     * Add a genomic child to this genomic glyph.
     */
    public void addGenomicChild(GenomicGlyph child) {
	addChild(child);
    }

    /**
     * Remove a genomic child from this genomic glyph.
     */
    public void removeGenomicChild(GenomicGlyph child) {
	removeChild(child);
    }

    public List genomicChildren() {
        ArrayList list = new ArrayList();
        appendGenomicChildren(this, list);
        return list;
    }

    public GenomicGlyph genomicParent() {
        for(ParentGlyph p = parent(); p != null; p = p.parent()) {
            if (p instanceof GenomicGlyph)
                return (GenomicGlyph)p;
        }
        return null;
    }

    public TierGlyph tierAncestor() {
        for(ParentGlyph p = parent(); p != null; p = p.parent()) {
            if (p instanceof TierGlyph)
                return (TierGlyph)p;
        }
        return null;
    }

    //Glyph specialization
    public double x() { return start(); }
    public double y() { return 0; }
    public double width() { return end() - start(); }

    public void delete() {
        if (genomicParent() != null)
            genomicParent().removeGenomicChild(this);
        else {
            if (tierAncestor() != null)
                tierAncestor().removeGenomicChild(this);
        }

        super.delete();
    }

    public void setExpanded(boolean b) {
    }

    private static void appendGenomicChildren(Glyph glyph, ArrayList list) {
        if (!(glyph instanceof ParentGlyph))
            return;
        ParentGlyph parent = (ParentGlyph)glyph;
        for(int i = parent.childCount() - 1; i >= 0; --i) {
            Glyph child = parent.child(i);
            if (child instanceof GenomicGlyph)
                list.add(child);
            else
                appendGenomicChildren(child, list);
        }
    }
}
