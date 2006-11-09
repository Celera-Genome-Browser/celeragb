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

import client.shared.swing.GenomicSequenceViewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * The class used to render individual location contents in a
 * <code>MultiSequenceViewer</code>
 * @see client.shared.swing.MultiSequenceViewer
 * @see client.shared.swing.genomic.SeqTableModel
 */
public class DefaultLocationRenderer extends DefaultTableCellRenderer
            implements TableModelListener {
        private long numRows;
        private long numColumns;
        private long visibleCount;
        private long totalLocations;
        private long minRange;
        private long maxRange;
        private int bandInterval = 10;
        private Color bkgndColor;
        private Color fgndColor = Color.yellow;
        private Font font = getFont();
        private Color bandBkColor = new Color(65, 65, 66);

	private RangeSearchTree adornmentsTree = new RangeSearchTree();
  private static final Comparator zOrderComparator = new ZOrderComparator();
	/**
	 * Default Constructor, sets the background color to Black, and sets
	 * the foregound color to Yellow
	 */
        public DefaultLocationRenderer() {

            this(Color.black, Color.yellow);
        }

	/**
	 * Constructs a DefaultLocationRenderer with specified background and foreground
	 * colors
	 * @param bkgnd The desired background color
	 * @param foregnd The desired foreground color
	 */
        public DefaultLocationRenderer(Color bkgnd, Color foregnd) {
            super();
            bkgndColor = bkgnd;
            fgndColor = foregnd;
        }

	/**
	 * Set the rendering font for this <code>DefaultLocationRenderer</code>
	 * @see #getRendererFont()
	 */
        public void setRendererFont(Font font) {
            this.font = font;
        }

	/**
	 * Get the rendering font for this <code>DefaultLocationRenderer</code>
	 * @see #getRendererFont()
	 */
        public Font getRendererFont() {
            return font;
        }

	/**
	 * Set the BandInterval for this <code>DefaultLocationRenderer</code>
	 * @param interval The number of columns the band spans
	 * @see #getBandInterval
	 */
        public void setBandInterval(int interval) {
            bandInterval = interval;
        }

	/**
	 * Set the BandInterval for this <code>DefaultLocationRenderer</code>
	 * @param interval The number of columns the band spans
	 * @see #setBandInterval
	 */
        public int getBandInterval() {
            return bandInterval;
        }

	/**
	 * Set the background color for this <code>DefaultLocationRenderer</code>
	 * @see #getBackgroundColor
	 */
        public void setBackgroundColor(Color color) {
           bkgndColor = color;
        }

	/**
	 * Set the background color for this <code>DefaultLocationRenderer</code>
	 * @see #setBackgroundColor
	 */
        public Color getBackgroundColor() {
            return bkgndColor;
        }

	/**
	 * Set the Foreground color for this <code>DefaultLocationRenderer</code>
	 * @see #getForeroundColor
	 */
        public void setForegroundColor(Color color) {
            fgndColor = color;
        }

	/**
	 * Set the Foreground color for this <code>DefaultLocationRenderer</code>
	 * @see #setForegroundColor
	 */
        public Color getForegroundColor() {
            return fgndColor;
        }

	/**
	 * Set the Background color for the Bands in this <code>DefaultLocationRenderer</code>
	 * @set #getBandBackground
	 */
        public void setBandBackground(Color color) {
            bandBkColor = color;
        }

	/**
	 * Set the Background color for the Bands in this <code>DefaultLocationRenderer</code>
	 * @set #getBandBackground
	 */
        public Color getBandBackground() {
            return bandBkColor;
        }

	/**
	 * Add an <code>Adornment</code> to this <code>DefaultLocationRenderer</code>
	 * @param adornment The <code>Adornment</code> to be added.
	 * @see #removeAdornment
	 * @see #clearAll
	 */
        public void addAdornment(Adornment adornment) {
            adornmentsTree.add(adornment);
        }

	/**
	 * Remove an <code>Adornment</code> from this <code>DefaultLocationRenderer</code>
	 * @param adornment The <code>Adornment</code> to be removed.
	 * @see #addAdornment
	 * @see #clearAll
	 */
        public void removeAdornment(Adornment adornment) {
            adornmentsTree.remove(adornment);
        }

	/**
	 * Remove all  <code>Adornment</code>'s from this <code>DefaultLocationRenderer</code>
	 * @see #addAdornment
	 * @see #removeAdornment
	 */
        public void clearAll() {
            adornmentsTree.clearAll();
        }

	/**
	 * Returns the default table cell renderer
	 */
        public Component getTableCellRendererComponent(JTable table,
                                            Object value, boolean isSelected,
        	                            boolean hasFocus, int row, int column){
            long cur_location = cellToLocation(row,column);
            ArrayList adornments = adornmentsTree.findRange(Long.MIN_VALUE,cur_location,cur_location,Long.MAX_VALUE);
            if(adornments.size()!=0){
              if(adornments.size()>1)
                Collections.sort(adornments,zOrderComparator);
              for(int i=0; i < adornments.size(); i++){
                Adornment adornment = (Adornment)adornments.get(i);
                if(isSelected){
                  setBackground(adornment.getSelectedBackground());
                  setForeground(adornment.getSelectedForeground());
                }
                else{
                  setBackground(adornment.getBackground());
                  setForeground(adornment.getForeground());
                }
              }
            }
            else{ // no adornments
              if(isSelected){
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
              }
              else{
                setForeground(fgndColor);
                setBackground(calcColumnColor(column));
              }
            }
            setText((value == null) ? "" : value.toString());
            setFont(font);
            return this;
        }


    /**
     * Return the <code>Color</code> for the given column number
     * @param column The index of the Column.
     */
    protected Color calcColumnColor(int column) {
        Color color;
        // The interval is determined by the column 1 (2nd column); the
        // first column is used as a label.  The 2 is the number of band
        // colors (currently on 2.
        int intervals = ((column-1) / bandInterval) % 2;
        if (intervals == 0) {
            color = this.bkgndColor;
        } else {
            color = this.bandBkColor;
        }

        return color;
    }

    /**
     * Returns the location represented by the given cell
     * @param row The row index of the cell
     * @param column The column index of the cell
     * @return The <code>Long</code> value of the location, in the sequence,
     * 		that the cell represents.  If no sequences are visible, it will return
     * 		<code>Long.MIN_VALUE</code>
     * @see #locationToRow
     * @see #locationToColumn
     */
    protected long cellToLocation(long row, long column) {
        long curColumn = column == 0 ? 1 : column;

        if (visibleCount == 0) {
            return Long.MIN_VALUE;
        }
        long value = minRange + ((numColumns-1) * ((int)Math.floor(row/visibleCount))) + (curColumn-1);
        if (value > maxRange) {
            return Long.MIN_VALUE;
        } else {
            return value;
        }
    }

    /**
     * Get the index for the cell row for a given location
     * @param location The <code>Long</code> value representing the desired sequence
     * 		location
     * @return The row index for the given location
     * @see #locationToColumn
     * @see #cellToLocation
     */
    protected long locationToRow(long location) {
        return location/totalLocations;
    }

     /**
     * Get the index for the cell column for a given location
     * @param location The <code>Long</code> value representing the desired sequence
     * 		location
     * @return The column index for the given location
     * @see #locationToRow
     * @see #cellToLocation
     */
    protected long locationToColumn(long location) {
        return location%totalLocations;
    }

    /**
     * This fine grain notification tells listeners the exact range of cells, rows, or columns that changed.
     */
    public void tableChanged (TableModelEvent evt) {
         SeqTableModel source = (SeqTableModel)evt.getSource();

         numColumns = source.getColumnCount();
         numRows =  source.getRowCount();
         visibleCount = source.getSequenceCount();
         minRange = source.getMinAxisLocation();
         maxRange = source.getMaxAxisLocation();
         totalLocations = ((SeqTableModel)source).getSequenceSize();
    }

    /**
     * Helper function used to adjust adornments if the table data has had
     * insertions or deletions.
     * @param beginLocation The begin location of the table modification
     * @param newMaxLocation The end location of the table modication
     * @param type the type of change ie: INSERT or DELETE
     */
    public void adjustAdornments(long beginLocation, long endLocation, int type) {
      ArrayList adornments = adornmentsTree.toArrayList();
      for (int i = 0; i < adornments.size(); i++) {
        Adornment adornment = ((Adornment)adornments.get(i));
        long adornBegin = adornment.getBeginLocation();
        long adornEnd = adornment.getEndLocation();
        if(type == GenomicSequenceViewer.INSERT){
          if(adornEnd < endLocation){// we're in front of insert
            // do nothing for now
          }
          else if(adornBegin > beginLocation){//we're after insert
            adornment.setRange(adornBegin+1, adornEnd+1);
          }
          else{//inserted inside this adornment
            adornment.setRange(adornBegin, adornEnd+1);
          }
        }
        else if(type == GenomicSequenceViewer.DELETE){
          if(adornEnd < beginLocation && adornBegin < beginLocation){//we're in front of change
            // do nothing for now
          }
          else if(adornBegin > endLocation){//we're behind change
            adornment.setRange(adornBegin-1, adornEnd-1);
          }
          else{//change crosses a boundry
            if(adornBegin >= beginLocation && adornEnd <= endLocation){
              //deleted entire adornment
              adornments.remove(i);
              adornmentsTree.remove(adornment);
            }
            else if(adornBegin < beginLocation && adornEnd <= endLocation ){
              // shrink adornment ending
              long offset = Math.abs(beginLocation - adornEnd);
              adornment.setRange(adornBegin, adornEnd - offset -1);
            }
            else if(adornBegin > beginLocation && adornEnd > endLocation){
              // shrink adornment begining
              long offset = Math.abs(beginLocation - endLocation);
              adornment.setRange(beginLocation,adornEnd - offset - 1);
            }
            else{
              // delete occurred inside adornment
              adornment.setRange(adornBegin,
                                 adornEnd - Math.abs(beginLocation - endLocation) - 1);
            }
          }
        }
      }
      adornmentsTree.restoreTree();
    }

}

final class ZOrderComparator implements Comparator {

  public int compare(Object o1, Object o2){
    if(!(o1 instanceof Adornment) || !(o2 instanceof Adornment))
      throw new ClassCastException();
    int z1 = ((Adornment)o1).getZOrder();
    int z2 = ((Adornment)o2).getZOrder();
    if(z1 < z2)
      return -1;
    else if(z1 > z2)
      return 1;
    else return 0;
  }

}