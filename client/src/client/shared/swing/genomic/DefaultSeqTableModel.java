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

import api.stub.sequence.Sequence;
import api.stub.sequence.SequenceHelper;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class is the default implementation of a <code>SequenceTableModel</code>,
 * and is used to model the sequence data shown in a <code>MultiSequenceViewer</code>.
 */
public class DefaultSeqTableModel extends AbstractTableModel implements Serializable, SeqTableModel {
    protected long numRows = 10;
    protected long numColumns = 10;
    protected long minRange = Long.MAX_VALUE;
    protected long maxRange = Long.MIN_VALUE;
    protected ArrayList dataVector = new ArrayList();
    protected ArrayList rangeVector = new ArrayList();
    protected ArrayList labelVector = new ArrayList();
    private int labelDirection = FORWARD_LABEL_COUNT;

    /**
     *  Constructs a default <code>DefaultSeqTableModel</code>
     *  which is a table of zero columns and zero rows.
     */
    public DefaultSeqTableModel() {}

    /**
     * Set the direction of the label counting
     */
    public void setCountLabelDirection(int value) {
        labelDirection = value;
    }

    /**
     * Adds a sequence to this <code>DefaultSeqTableModel</code>.
     * @param index The index, in the list, at which the sequence is to be inserted.
     * @param label The label for the sequence
     * @param sequence The <code>Sequence</code> to be inserted
     * @param range The range over which the sequence is applicable.
     * @throw java.lang.IndexOutOfBoundsException If the <code>index</code> is
     *        < 0, or > current number of sequences.
     */
    public void addSequence(int index, String label, ViewerSequence sequence, SwingRange range) {
        labelVector.add(index, label);
        dataVector.add(index, sequence);
        rangeVector.add(index, range);

        validateRange();

        //this.fireTableStructureChanged();
    }

    /**
     * Removes a sequence from this <code>DefaultSeqTableModel</code>.
     * @param index The index of the sequence to be removed
     * @throws java.lang.IndexOutOfBoundsException if <code>index</code> is < 0,
     *          or >= current number of sequences.
     */
    public void removeSequence(int index) {
        if (index > dataVector.size()) {
            throw new java.lang.IndexOutOfBoundsException("Index: " + index + ", Size: " + dataVector.size());
        }

        labelVector.remove(index);
        dataVector.remove(index);
        rangeVector.remove(index);

        validateRange();
    }

    /**
     * Get the Sequence at a specified <code>index</code>
     * @param index The index of the desired sequence.
     * @throws java.lang.IndexOutOfBoundsException if <code>index</code> is < 0,
     *         or >= current number of sequences.
     */
    public Sequence getSequenceAt(int index) {
        if (index > dataVector.size()) {
            throw new java.lang.IndexOutOfBoundsException("Index: " + index + ", Size: " + dataVector.size());
        }

        return (Sequence)dataVector.get(index);
    }

    /**
     * Removes all sequences from this <code>DefaultSeqTableModel</code>.
     */
    public void removeAll() {
        labelVector.clear();
        dataVector.clear();
        rangeVector.clear();

        minRange = Long.MAX_VALUE;
        maxRange = Long.MIN_VALUE;
    }

    /**
     * Returns the length of the Sequences
     * @return A <code>Long</code> representing the current length of the
     *          sequences.
     */
    public long getSequenceSize(){
        return Math.abs(maxRange - minRange) + 1;
    }

    /**
     * Get the minimum axis Location for this <code>DefaultSeqTableModel</code>.
     * @returns A <code>Long</code> representing the minimum axis location.
     * @see #getMaxAxisLocation
     */
    public long getMinAxisLocation() {
        return this.minRange;
    }

    /**
     * Get the maximum axis location for this <code>DefaultSeqTableModel</code>.
     * @returns A <code>Long</code> representing the maximum axis location.
     * @see #getMinAxisLocation
     */
    public long getMaxAxisLocation() {
        return this.maxRange;
    }

    /**
     * Inserts a base into the given sequence.
     * @param index The index of the sequence to be modified
     * @param base  The <code>char</code> of the base to be added.
     * @param location The location in the sequence at which the base is to be
     *        added.
     * @throws java.lang.IndexOutOfBoundsException If <code>index</code> < 0 or
     *         >= current number of sequences.
     * @see #removeBase
     */
    public void addBase(int seqIndex, char base, long location) {
        ViewerSequence seq = (ViewerSequence)dataVector.get(seqIndex);

        SwingRange range = (SwingRange)rangeVector.get(seqIndex);

        seq.insert(location, SequenceHelper.charToBase(seq, base));


       range.setEndRange(range.getEndRange() + 1);

        validateRange();

        int row = (int)locationToRow(minRange+location);
        this.fireTableRowsInserted(row, row);
    }


    /**
     * Removes a base from the given sequence.
     * @param index The index of the sequence to be modified
     * @param location The location in the sequence from which the base is to be
     *        removed.
     * @throws java.lang.IndexOutOfBoundsException If <code>index</code> < 0 or
     *         >= current number of sequences.
     * @see #addBase
     */
    public void removeBase(int seqIndex, long location){
        ViewerSequence seq = (ViewerSequence)dataVector.get(seqIndex);
        SwingRange range = (SwingRange)rangeVector.get(seqIndex);
        seq.remove(location);
        range.setEndRange(range.getEndRange() - 1);

        validateRange();

        int row = (int)locationToRow(minRange+location);
        this.fireTableRowsDeleted(row, row);
    }

     /**
      * Removes the selected bases from a given sequence, in this
      *  <code>DefaultSeqTableMode</code>.
      * @param seqIndex the index of the sequence from which to remove bases.
      * @param startLocation the start location from which the bases are to
      *         be removed.
      * @param endLocation the end location of the bases to be removed.
      * @param baseLength the length of the selected bases
      * @throws java.lang.IndexOutOfBoundsException If <code>index</code> < 0 or
      *         >= current number of sequences.
      * @todo Ask about baseLength?
      */
     public void removeSelectedBases(int seqIndex, long startLocation, long endLocation, int baseLength){
        ViewerSequence seq = (ViewerSequence)dataVector.get(seqIndex);
        SwingRange range = (SwingRange)rangeVector.get(seqIndex);
        seq.removeSelectedBases(startLocation, endLocation, baseLength);
        range.setEndRange(range.getEndRange() - baseLength);
        int row1 = (int)locationToRow(minRange+startLocation);
        int row2 = (int)locationToRow(minRange+endLocation);
        validateRange();
        this.fireTableRowsDeleted(row1, row2);
    }

    /**
     * Sets a base, in the given sequence, to another value.
     * @param index The index of the sequence to be modified
     * @param base  The <code>char</code> of the value for the base.
     * @param location The location in the sequence at which the base is to be
     *        modified.
     * @throws java.lang.IndexOutOfBoundsException If <code>index</code> < 0 or
     *         >= current number of sequences.
     * @see #removeBase
     * @see #addBase
     */
    public void setBaseAt(int seqIndex, char base, long location){
        ViewerSequence seq = (ViewerSequence)dataVector.get(seqIndex);
        seq.set(location, SequenceHelper.charToBase(seq, base));

        int row = (int)locationToRow(location);
        this.fireTableCellUpdated(row, row);
    }

    /**
     *  Equivalent to <code>fireTableChanged</code>.
     *
     * @param event  the change event
     *
     */
    public void newDataAvailable(TableModelEvent event) {
        fireTableChanged(event);
    }

    /**
     *  Sets the number of rows in the model.  If the new size is greater
     *  than the current size, new rows are added to the end of the model
     *  If the new size is less than the current size, all
     *  rows at index <code>rowCount</code> and greater are discarded. <p>
     *
     *  @see #setColumnCount
     */
    public void setRowCount(int rowCount) {
        numRows = rowCount;

        this.fireTableStructureChanged();
    }

    /**
     *  Sets the number of columns in the model.  If the new size is greater
     *  than the current size, new columns are added to the end of the model
     *  with <code>null</code> cell values.
     *  If the new size is less than the current size, all columns at index
     *  <code>columnCount</code> and greater are discarded.
     *
     *  @param columnCount  the new number of columns in the model
     *
     *  @see #setColumnCount
     */
    public void setColumnCount(int columnCount) {
        if (columnCount == 0) columnCount = 2;

        numRows = (getSequenceSize() / columnCount) + (getSequenceSize() % columnCount);
        numColumns = columnCount;

        this.fireTableStructureChanged();
    }

    /**
     * Returns the number of rows in this data table.
     * @return the number of rows in the model
     */
    public int getRowCount() {
        return (int)this.numRows;
    }

    /**
     * Returns the number of columns in this data table.
     * @return the number of columns in the model
     */
    public int getColumnCount() {
        return (int)numColumns;
    }

    /**
     * Returns the current number of sequences
     * @return the number of sequences in the model
     */
    public int getSequenceCount() {
        return dataVector.size();
    }

    /**
     * Returns false regardless of parameter values.
     *
     * @param   row             the row whose value is to be queried
     * @param   column          the column whose value is to be queried
     * @return                  false.  This is to allow for sequence editing without
     *                          using the internal (table) editor.
     * @see #setValueAt
     */
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    /**
     * Returns an attribute value for the cell at <code>row</code>
     * and <code>column</code>.
     *
     * @param   row             the row whose value is to be queried
     * @param   column          the column whose value is to be queried
     * @return                  the value Object at the specified cell
     * @exception  ArrayIndexOutOfBoundsException  if an invalid row or
     *               column was given
     */
    public Object getValueAt(int row, int column) {
        long location = cellToLocation(row, column);
        int index = (int)rowToDataIndex(row);

        if ((location >= minRange && location <= maxRange) && index != -1) {
            try {
                // get the row labels
                if (column == 0) {
                   return getRowLabelString(row);
                }
                if (((SwingRange)rangeVector.get(index)).containsLocation(location)) {
                    ViewerSequence seq = (ViewerSequence)dataVector.get((int)rowToDataIndex(row));
                    return "" + seq.baseToChar(location - minRange);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return "";
    }

    /**
     * Get the label for a given row
     * @param row the row whose label is needed
     * @throws  java.lang.IndexOutOfBoundsException if the row isn't valid in this
     *        <code>DefaultSeqTableModel</code>
     */
    protected String getRowLabelString(int row) {
        int index = (int)rowToDataIndex(row);

        if (labelVector.size() > 0) {
            String label = (String)labelVector.get(index);
            if (label == null) {
                long minRow = minRange + (((int)Math.floor(row/dataVector.size())) * (this.numColumns-1));

                // Return the range label
                if (this.labelDirection == FORWARD_LABEL_COUNT) {
                    return  " " + Long.toString(minRow)+ " ";
                } else {
                     return " " + Long.toString(maxRange - minRow) + " ";
                }
            } else {
                return label;
            }
        }
        return "";
    }

    /**
     * Adjusts this <code>DefaultSeqTableModel</code>'s min and max ranges,
     * after the table data has been modified.
     */
    protected void validateRange() {
        minRange = Long.MAX_VALUE;
        maxRange = Long.MIN_VALUE;
        for (int i = 0; i < rangeVector.size(); i++) {
            SwingRange range = (SwingRange)rangeVector.get(i);
            if (range.getStartRange() < minRange) {
                minRange = range.getStartRange();
            }
            if (range.getEndRange() > maxRange) {
                maxRange = range.getEndRange();
            }
        }
    }

    /**
     * Returns the location represented by a given cell.
     * @param row the cell's row index
     * @param column the cell's column index
     * @return the location displayed by the cell. Or Long.MIN_VALUE if
     *         the cells coordinates are invalid.
     */
    public long cellToLocation(long row, long column) {
        long curColumn = column == 0 ? 1 : column;

        if(row < 0 || row > numRows || column < 0 || column > numColumns)
          return Long.MIN_VALUE;

        if (dataVector.size() == 0) {
            return Long.MIN_VALUE;
        }
        long value = minRange + ((numColumns-1) * ((int)Math.floor(row/dataVector.size()))) + (curColumn-1);
        if (value > maxRange) {
            return Long.MIN_VALUE;
        } else {
            return value;
        }
    }

    /**
     * Returns the row index for a given location
     * @param location the axis location
     * @return the row index in this <code>DefaultSeqTableModel</code>
     *        for the cells that represent the given location
     * @see #locationToColumn
     */
    public long locationToRow(long location) {
        return (long)Math.floor((location - minRange)/(numColumns-1))*(dataVector.size());
    }

    /**
     * Returns the column index for a given location
     * @param location the axis location
     * @return the column index in this <code>DefaultSeqTableModel</code>
     *        for the cells that represent the given location.
     * @see #locationToRow
     */
    public long locationToColumn(long location) {
     return ( (location - minRange)%(numColumns-1) + 1);
    }

    /**
     * Returns the index of the sequence displayed in the given row
     * @param row the row in question
     * @return the index of the sequence displayed in Row <code>row</code>.
     */
    protected long rowToDataIndex(long row) {
        if (dataVector.size() == 0) {
            return -1;
        }
        if (row < dataVector.size()) {
            return row;
        }
        return row % dataVector.size();
    }

    /**
     * Returns the longest sequence label
     */
    public String getMaxLabel(){
      String max = "";
      if(labelVector != null)
        for(int i=0; i < 20 && i < labelVector.size(); i++){
          String label = (String)labelVector.get(i);
          if(label != null)
            max = label.length() > max.length() ? label : max;
        }
      if(minRange != Long.MAX_VALUE || maxRange != Long.MIN_VALUE){
        String maxAxisStr = Long.toString(maxRange);
        max = maxAxisStr.length() > max.length() ? maxAxisStr : max;
        String minAxisStr = Long.toString(minRange);
        max = maxAxisStr.length() > max.length() ? minAxisStr : max;
      }
      return max;
    }
}