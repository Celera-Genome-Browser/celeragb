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
package client.shared.swing.genomic;

import java.awt.*;

/**
 * Class used to visually represent a range along a sequence
 */
public class Adornment implements RangeSearchable {
    private java.awt.Component component;
    private Color bkColor;
    private Color fgColor;
    private java.awt.Color selectedBackgroundColor;
    private java.awt.Color selectedForegroundColor;
    private SwingRange range;
    private int zOrder;

    /**
     * Construct an <code>Adornment</code> with the given settings.
     * @param beginLocation The Location at which this <code>Adornment</code>
     * 		begins.
     * @param endLocation The Location at which this <code>Adornment</code> ends.
     * @param bkColor	The Background color of this <code>Adornment</code>.
     * @param fgColor	The Foreground color of this <code>Adornment</code>.
     */
    public Adornment(long beginLocation, long endLocation, Color bkColor, Color fgColor) {
        this.range = new SwingRange(beginLocation, endLocation);

        this.bkColor = bkColor;
        this.fgColor = fgColor;
        // set's selected colors to default to the complement of the
        // foreground and background color's
	if(bkColor == null){
	     this.selectedBackgroundColor = Color.yellow;
	}else{
           this.selectedBackgroundColor = new Color( ~bkColor.getRGB() );
	}
	this.selectedForegroundColor = new Color( ~fgColor.getRGB() );
        zOrder = 0;
    }

    /**
     * Get the Z-order for this adornment, the Z-order dictates vertical
     * stacking positions for overlapping adornments.
     */
     public int getZOrder() {return this.zOrder;}

    /**
     * Set the Z-order for this adornment, the Z-order dictates vertical
     * stacking positions for overlapping adornments.
     */
     public void setZOrder(int zOrder) {this.zOrder = zOrder;}

    /**
     * Get the <code>java.awt.Component</code> associated with this
     * <code>Adornment</code>.
     * @return The associated <code>Component</code>.
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Set the <code>java.awt.Component</code> associated with this
     * <code>Adornment</code>.
     * @return The associated <code>Component</code>.
     */
    public void setComponent(java.awt.Component component) {
        this.component = component;
    }

    /**
     * Set the Background color for this Adornment
     * @param backgroundColor The background color for this adornment
     * @see #getBackground
     */
    public void setBackground(java.awt.Color backgroundColor) {
        bkColor = backgroundColor;
    }

    /**
     * Returns this Adornment background color
     * @see #setBackground
     */
    public java.awt.Color getBackground() {
        return bkColor;
    }
    /**
     * Set the Foreground color for this Adornment
     * @param foregroundColor The foreground color for this adornment
     * @see #getForeground
     */
    public void setForeground(java.awt.Color foregroundColor) {
        fgColor = foregroundColor;
    }

    /**
     * Get the Foreground color for this Adornment
     * @see #setForeground
     */
    public java.awt.Color getForeground() {
        return fgColor;
    }

    /**
     * Set the Background color for this Adornment when selected
     * @see #getSelectedBackground
     */
    public void setSelectedBackground(java.awt.Color newSelectedBackgroundColor) {
        selectedBackgroundColor = newSelectedBackgroundColor;
    }

    /**
     * Get the Background color for this Adornment, when selected
     * @return The Adornment's selected background color
     * @see #setSelectedBackground
     */
    public java.awt.Color getSelectedBackground() {
        return selectedBackgroundColor;
    }

    /**
     * Set the Foreground color for this Adornment, when selected
     * @see #getSelectedForeground
     */
    public void setSelectedForeground(java.awt.Color newSelectedForegroundColor) {
        selectedForegroundColor = newSelectedForegroundColor;
    }

    /**
     * Get the Foreground color for this Adornment when selected
     * @return The Adornment's selected foreground color
     * @see #setSelectedForeground
     */
    public java.awt.Color getSelectedForeground() {
        return selectedForegroundColor;
    }

    /**
     * Sets the range over which this adornment is active
     * @param beginLocation The position, in the sequence,
     *        at which this adornment starts
     * @param endLocation The position, in the sequence,
     *        at which this adornment ends
     * @see #getBeginLocation
     * @see #getEndLocation
     */
    public void setRange(long beginLocation, long endLocation) {
        range.setStartRange(beginLocation);
        range.setEndRange(endLocation);
    }

    /**
     * Get the location at which this <code>Adornment</code> begins
     * @return The <code>Long</code> representing the position, in the
     *          sequence, at which this <code>Adornment</code> begins
     * @see #getEndLocation
     * @see #setRange
     */
    public long getBeginLocation() {
        return range.getStartRange();
    }
    /**
     * Get the location at which this <code>Adornment</code> ends
     * @return The <code>Long</code> representing the position, in the
     *          sequence, at which this <code>Adornment</code> ends
     * @see #getBeginLocation
     * @see #setRange
     */
    public long getEndLocation() {
        return range.getEndRange();
    }

    /**
     * Test whether this <code>Adornment</code> contains a given sequence location.
     * @param location The <code>Long</code> representing the location to be
     *        tested.
     * @return  <code>true</code> if the location falls in between the begin and
     *          end locations of this <code>Adornment</code>.
     * @see #getBeginLocation
     * @see #getEndLocation
     */
    public boolean containsLocation(long location) {
        return range.containsLocation(location);
    }

    /**
     * Reeturns the key for the given keyType
     */
    public long getKey( int side ){
	if( side == RangeSearchable.BEGIN )
	    return Math.min(range.getStartRange(),range.getEndRange());
	else if ( side == RangeSearchable.END )
	    return Math.max(range.getStartRange(),range.getEndRange());
	else
	    return -1;

    }
    /**
     * Test for <code>Adornment</code> equality
     * @return <code>true</code> if the <code>obj</code> represents the same data as
     * 		this <code>Adornment</code>.
     */
    public boolean equals (Object obj) {
        if (!(obj instanceof Adornment)) {
            return false;
        }
        Adornment toComp = (Adornment)obj;
        boolean isEqual;
        if( component==null)
          isEqual = (toComp.component == null);
        else
          isEqual = component.equals(toComp.component);
        if( bkColor == null)
          isEqual = isEqual && (toComp.bkColor == null);
        else
          isEqual = isEqual && bkColor.equals(toComp.bkColor);

        if ( isEqual && range.equals(toComp.range)  &&
            fgColor.equals(toComp.fgColor)  &&
            zOrder == toComp.zOrder &&
            selectedBackgroundColor.equals(toComp.selectedBackgroundColor) &&
            selectedForegroundColor.equals(toComp.selectedForegroundColor) ){
            return true;
        }
        else {
            return false;
        }
    }
}
