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

public interface SeqTableModel extends javax.swing.table.TableModel{
    public static final int FORWARD_LABEL_COUNT = 0;
    public static final int BACKWARD_LABEL_COUNT = 1;

    public long getSequenceSize();

    public void addSequence(int index, String label, ViewerSequence sequence, SwingRange range);
    public void setCountLabelDirection(int value);
    public Sequence getSequenceAt(int index);
    public void removeSequence(int index);
    public void removeAll();
    public int getSequenceCount();

    public void setColumnCount(int count);
    public void setRowCount(int count);

    public void addBase(int seqIndex, char base, long location);
    public void removeBase(int seqIndex, long location);

    public void setBaseAt(int seqIndex, char base, long location);

    public long cellToLocation(long row, long column);
    public long locationToRow(long location);
    public long locationToColumn(long location);

    public long getMinAxisLocation();
    public long getMaxAxisLocation();

    public String getMaxLabel();
}