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
package vizard.glyph;

import vizard.Glyph;
import vizard.ParentGlyph;

public class VerticalPacker extends Packer
{
    private int interval = 3;
    private boolean isExpanded = true;

    public int packedChildCount() {
        int count = 0;
        for(int i = 0; i < childCount(); ++i) {
            count += horizontalSorter(i).childCount();
        }
        return count;
    }

    public void packChild(Glyph child) {
        for(int i = 0; i < childCount(); ++i) {
            if (horizontalSorter(i).addChildIfNotIntersecting(child))
                return;
        }

        appendNewHorizontalSorter(child);
    }

    private TranslationGlyph.Concrete translation(int i) {
        return (TranslationGlyph.Concrete)child(i);
    }

    private MyHorizontalSorter horizontalSorter(int i) {
        return (MyHorizontalSorter)translation(i).child(0);
    }

    private void appendNewHorizontalSorter(Glyph child) {
        HorizontalSorter horizontalSorter = new MyHorizontalSorter();
        horizontalSorter.addChildIfNotIntersecting(child);
        TranslationGlyph.Concrete translation = new TranslationGlyph.Concrete(0, isExpanded ? tyAt(childCount()) : 0);
        translation.addChild(horizontalSorter);
        addChild(translation);
    }

    private double tyAt(int i) {
        if (!isExpanded)
            return 0.;
        return (i == 0) ? 0. : (translation(i-1).ty() + horizontalSorter(i-1).maxHeight);
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void expand() {
	if (!isExpanded) {
	    isExpanded = true;
	    repackChildren();
	}
    }

    public void collapse() {
	if (isExpanded) {
	    isExpanded = false;
	    repackChildren();
	}
    }

    public void unpackChild(Glyph child) {
        if(child.parent() instanceof MyHorizontalSorter){
	  MyHorizontalSorter horizontalSorter = (MyHorizontalSorter)child.parent();
          horizontalSorter.removeChild(child);
          if (!horizontalSorter.hasChildren())
            horizontalSorter.parent().delete();
        }else{
	    ParentGlyph parent =child.parent();
	    parent.removeChild(child);
	     if (!parent.hasChildren())
                parent.parent().delete();
        }
    }

    public Glyph packedChildAt(int n) {
        for(int i = 0;; ++i) {
            MyHorizontalSorter horizontalSorter = horizontalSorter(i);
            if (horizontalSorter.childCount() > n)
                return horizontalSorter.child(n);
            n -= horizontalSorter.childCount();
        }
    }

    public void handleBoundsChange() {
        super.handleBoundsChange();
	repackChildren();
    }

    private void repackChildren() {
	for(int i = 0; i < childCount(); ++i) {
            translation(i).setTranslation(0, tyAt(i));
        }
    }

    private double heightFor(Glyph glyph) {
        return glyph.getBounds().height + interval;
    }


    class MyHorizontalSorter extends HorizontalSorter
    {
        double maxHeight = 0;

        public void intersectingChildJustGotRemoved(Glyph previousChild) {
            packChild(previousChild);
        }

        //Notification about some children bounds change
        public void handleBoundsChange() {
            super.handleBoundsChange();
            recomputeMaxHeight();
        }

        private void recomputeMaxHeight() {
            maxHeight = 0;
            for(int i = 0; i < childCount(); ++i) {
                maxHeight = Math.max(maxHeight, heightFor(child(i)));
            }
        }
    }
}

