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

import vizard.Bounds;
import vizard.Glyph;
import vizard.GraphicContext;
import vizard.ParentGlyph;
import vizard.PickedList;
import vizard.genomics.model.GenomicAxisViewModel;
import vizard.glyph.BoundsGlyph;
import vizard.glyph.Packer;
import vizard.glyph.PropertySortedPacker;
import vizard.glyph.VerticalPacker;
import vizard.model.WorldViewModel;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;


/**
 * The purpose of the TierGlyph is to display one or more genomic-glyphs
 * in the same row.
 *
 * The TierGlyph provides a pack/unpack capability.
 * Unpack displaces along the Y axis genomic-glyphs that would otherwise
 * intersect.
 * Pack moves back all the genomic-glyphs on the same row.
 *
 * Note that the TierGlyph automatically adjusts its height when new glyphs
 * are added (eg, if they are "taller" than the current height) or when
 * the whole tier is packed or unpacked.
 */
public class TierGlyph extends BoundsGlyph
    implements WorldViewModel.Observer
{

    public static int HIDDEN    = 100;
    public static int COLLAPSED = 101;
    public static int EXPANDED  = 102;
    public static int FIXED     = 103;
    public static int PROPERTY_SORTED = 104;

    //@todo preferences
    public int MINIMUM_HEIGHT = 10;
    public int INTERSPACE = 3;

    public static Color SEPARATION_BAR_COLOR = new Color(30, 30, 30);

    private String name;
    private GenomicAxisViewModel axisModel;
    private int state;
    private double previousBaseCount;
    public static String DEFAULT_PACKER= "Vertical Packer";
    public static String PROPERTY_SORTED_PACKER= "Property Sorted Packer";
    private String packerType=DEFAULT_PACKER;
   // private String packerType=PROPERTY_SORTED_PACKER;

    private Packer packer;

    boolean isHighlighted;
    Color backgroundColor = Color.black;
    private boolean deleteWhenEmpty;

    /**
     * Initialize a new TierGlyph with the given name and for the
     * given axis model.
     */
    public TierGlyph(String tierName, GenomicAxisViewModel axisModel, int visibleState) {
	this.name = tierName;
        this.axisModel = axisModel;
        this.state = visibleState;
        previousBaseCount = axisModel.baseCount();

        if(packerType.equals(DEFAULT_PACKER)){
          packer=new VerticalPacker();
          setPackerType(DEFAULT_PACKER);

        }else if(packerType.equals(PROPERTY_SORTED_PACKER)){

          packer =new PropertySortedPacker();
          setPackerType(PROPERTY_SORTED_PACKER);
      }
	addChild(packer);

        axisModel.observers.addObserver(this);
    }

    public String getPackerType(){
       return packerType;
    }

    public void setPackerType(String s){
      packerType=s;

    }


    public void removePacker(){
	if(packer!=null){
	    this.removeChild(this.packer);
	}
    }


    public void addPacker(Packer packer){
	this.packer=packer;
	this.addChild(packer);
    }


    public double x() {
        return axisModel.worldOrigin();
    }

    public double y() {
        return 0;
    }

    public double width() {
        return axisModel.worldSize();
    }

    public double height() {
        return Math.max(MINIMUM_HEIGHT, packer.height());
    }

    public void delete() {
        axisModel.observers.removeObserver(this);

        if (tierColumn() != null)
            tierColumn().removeTier(this);
        super.delete();
    }

    public void shouldDeleteWhenEmpty(boolean b) {
        deleteWhenEmpty = b;
        //If it is empty right now, and deleteWhenEmpty is true,
        //should still NOT be delete now
        //(otherwise it becomes impossible to create an instance)
    }

    /**
     * Return the name of this tier.
     */
    public String name() {
	return name;
    }

    /**
     * Returns the visible state of the tier.  Hidden, Collapsed, Expanded, or Fixed.
     */
    public int getState() {
      return state;
    }

    /**
     * Allows someone to control the display state of the glyph.
     */
    public void setState(int visibleState) {
      this.state = visibleState;
    }

    public TiersColumnGlyph tierColumn() {
	for(ParentGlyph p = parent(); p != null; p = p.parent()) {
	    if (p instanceof TiersColumnGlyph)
                return (TiersColumnGlyph)p;
        }
        return null;
    }

    public void highlight(boolean b) {
        if (isHighlighted != b) {
            isHighlighted = b;
            repaint();
        }
    }

    public Color background() {
        return backgroundColor;
    }

    public void setBackground(Color c) {
	if (!c.equals(backgroundColor)) {
	    backgroundColor = c;
	    child(0).repaint();
	}
    }

    public Collection genomicChildren() {
        ArrayList list = new ArrayList();
        appendGenomicChildren(this, list);
        return list;
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

    /**
     * Add the given genomic glyph to this tier.
     *
     * The tier height might be increased.
     * Child added in case of Feature Glyph
     * represents the Root feature.
     */


    public void addGenomicChild(GenomicGlyph g) {


        g.setExpanded(isExpanded());
	properPackerFor(g).packChild(g);

    }

    public Packer getPacker(){
        return packer;
    }
    private Packer properPackerFor(GenomicGlyph g) {


         if (isExpanded())
          packer.expand();
        else
           packer.collapse();

       return packer;
      }




    private Packer packerParentOf(GenomicGlyph gg) {
        for(ParentGlyph p = gg.parent();; p = p.parent()) {
            if (p instanceof Packer)
                return (Packer)p;
        }
    }

    public void removeGenomicChild(GenomicGlyph gg) {
        Packer packer = packerParentOf(gg);
	packer.unpackChild(gg);
        if (!packer.hasChildren()) {
          if (deleteWhenEmpty && !hasGenomicChildren())
                delete();
        }
    }

    public boolean hasGenomicChildren() {
      return packer.packedChildCount() > 0;
    }

    public boolean isExpanded() {
        return state==EXPANDED;
    }

    public boolean isCollapsed() {
        return state==COLLAPSED;
    }

    public boolean isHidden() {
        return state==HIDDEN;
    }

    public boolean isFixed() {
        return state==FIXED;
    }

    private void notifyGenomicGlyphsAboutPackingChange() {
	notifyGenomicGlyphsAboutPackingChange(this);
    }

    private void notifyGenomicGlyphsAboutPackingChange(Glyph glyph) {
        if (glyph instanceof GenomicGlyph)
	    ((GenomicGlyph)glyph).setExpanded(isExpanded());
	else if (glyph instanceof ParentGlyph) {
	    ParentGlyph parent = (ParentGlyph)glyph;
	    for(int i = 0; i < parent.childCount(); ++i) {
		notifyGenomicGlyphsAboutPackingChange(parent.child(i));
	    }
	}
    }

    /*
     * Collapse the genomicGlyphs in this tier on the same row.
     */
    public void collapseTier() {
	if (!isCollapsed()) {
            state=COLLAPSED;
            notifyGenomicGlyphsAboutPackingChange();
            repaintCorrespondingTierName();
            packer.collapse();
	}
    }

    /**
     * Expand along the vertical direction the overlapping genomic glyphs.
     */
    public void expandTier() {
	if (!isExpanded()) {
            state=EXPANDED;
	    notifyGenomicGlyphsAboutPackingChange();
            repaintCorrespondingTierName();
            (packer).expand();


	}
    }

    private void repaintCorrespondingTierName() {
        tierColumn().tierNamesColumn().repaint();
    }

    public String getTierName(){
      return name;
    }
    public PickedList pick(GraphicContext gc, Rectangle deviceRect) {
        PickedList list = super.pick(gc, deviceRect);

        if (list == null) {
            Rectangle2D.Double shape = gc.tempRectangle();
            shape.setRect(x(), y(), width(), height());
            if (gc.hit(deviceRect, shape, false))
                list = new PickedList(this, gc);
        }

        return list;
    }

    public Bounds tierBounds() {
        return getBounds();
    }

    //WorldViewModel observer
    public void zoomCenterChanged(WorldViewModel model) {}
    public void modelChanged(WorldViewModel model) {
        if (axisModel.baseCount() != previousBaseCount) {
            previousBaseCount = axisModel.baseCount();
            handleBoundsChange();
        }
    }

    //to help debugging
    public void handleBoundsChange() {
        super.handleBoundsChange();
    }
    public void paint(GraphicContext gc) {
        super.paint(gc);
    }
}
