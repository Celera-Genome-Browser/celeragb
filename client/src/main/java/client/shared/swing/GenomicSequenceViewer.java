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
package client.shared.swing;

import api.stub.sequence.DNA;
import api.stub.sequence.Protein;
import api.stub.sequence.Sequence;
import api.stub.sequence.ShiftedSequence;
import client.shared.swing.genomic.*;

import java.util.Iterator;
import java.util.TreeMap;

public class GenomicSequenceViewer extends MultiSequenceViewer {
    final static public int ORF_3_DISPLAY       = 0;
    final static public int ORF_2_DISPLAY       = 1;
    final static public int ORF_1_DISPLAY       = 2;
    final static public int DNA_DISPLAY         = 3;
    final static public int COMPLEMENT_DNA_DISPLAY = 4;
    final static public int ORF_NEG1_DISPLAY    = 5;
    final static public int ORF_NEG2_DISPLAY    = 6;
    final static public int ORF_NEG3_DISPLAY    = 7;
    final static protected int REVERSE_COMPLEMENT_DNA_DISPLAY = 8;
    final static protected int MAX_DISPLAY      = 9;


    final static public int SINGLE_CHAR_TRANSLATION = 0;
    final static public int ABBREVIATED_TRANSLATION = 1;

    final static public int INSERT = 1;
    final static public int DELETE = -1;

    private int translationStyle = SINGLE_CHAR_TRANSLATION;
    private int dnaIndex = DNA_DISPLAY;

    protected ViewerSequence []sequenceArray = new ViewerSequence[MAX_DISPLAY];
    protected TreeMap indexMap = new TreeMap();
    protected SwingRange range;
    protected SwingRange translatedRange;

    /**
     * Creates a <code>GenomicSequenceViewer</code>
     */
    public GenomicSequenceViewer() {
        try {
            initGenomicViewer();
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    /**
     * Initializes the <code>Genomic Viewer</code>, adding the necessary listener
     * objects.
     */
    protected void initGenomicViewer() {
        super.addSequenceKeyListener(new SequenceKeyListener() {
            public void keyTyped(SequenceKeyEvent e) {
                int base = DNA.charToBase(e.getBase());
                int index = getDisplayIndex(DNA_DISPLAY);

                long begin = getSelectionBegin();
                long end = getSelectionEnd();

                if(base==Sequence.UNKNOWN && e.getKeyChar()!='\b'){
                  java.awt.Toolkit.getDefaultToolkit().beep();
                }

                 else if (e.getKeyChar()=='\b' && begin==Long.MIN_VALUE) {
                  long loc=getCursorLocation();
                  long transLoc = Math.abs(loc - seqModel.getMinAxisLocation());
                  if(transLoc < 1)// we're at the front of the model
                    return;
                  seqModel.removeBase(index, transLoc -1);

                  locationRenderer.adjustAdornments(loc-1,loc -1,
                                                    GenomicSequenceViewer.DELETE );

                  GenomicSequenceViewer.super.setSelectionInterval(loc-1,loc-1,false);

                 }
                 else if(e.getKeyChar()=='\b' && begin!=Long.MIN_VALUE){
                    long transBegin = Math.abs(begin - seqModel.getMinAxisLocation() );
                    long transEnd = Math.abs(end - seqModel.getMinAxisLocation() );
                    ((DefaultSeqTableModel)seqModel).removeSelectedBases(index,
                                                      transBegin, transEnd,
                                                      (int)((end-begin)+1) );
                   long cursor = Math.max(begin-1,seqModel.getMinAxisLocation());
                   locationRenderer.adjustAdornments(begin, end,
                                                     GenomicSequenceViewer.DELETE );
                   GenomicSequenceViewer.super.setSelectionInterval(cursor, cursor,false);
                 }

                 else{
                  if( begin!=Long.MIN_VALUE){
                    long transBegin = Math.abs(begin - seqModel.getMinAxisLocation() );
                    long transEnd = Math.abs(end - seqModel.getMinAxisLocation() );
                    ((DefaultSeqTableModel)seqModel).removeSelectedBases(index,
                                                      transBegin, transEnd,
                                                      (int)((end-begin)+1) );
                    long loc = Math.min(begin,end);
                    locationRenderer.adjustAdornments(begin,end,
                                                      GenomicSequenceViewer.DELETE );
                    GenomicSequenceViewer.super.setSelectionInterval(loc, loc,false);
                  }
                  long currLoc=getCursorLocation();
                  seqModel.addBase(index, e.getBase(), Math.abs(currLoc-seqModel.getMinAxisLocation()));
                  locationRenderer.adjustAdornments(currLoc,currLoc,
                                                    GenomicSequenceViewer.INSERT );

                  long cursor = Math.min(currLoc+1,seqModel.getMaxAxisLocation());
                  GenomicSequenceViewer.super.setSelectionInterval(cursor, cursor,false);
                }

              long finalCursor = getCursorLocation();
              int row = (int)seqModel.locationToRow(finalCursor);
              int col = (int)seqModel.locationToColumn(finalCursor);
              int scrollDirection = 0;
              if( finalCursor < getVisibleBeginLocation() )
                scrollDirection = -1;
              else if( finalCursor > getVisibleEndLocation() )
                scrollDirection = 1;
              row = (scrollDirection > 0) ? row + seqModel.getSequenceCount() - 1 : row;
              if( ! seqTable.isCellVisible(row,col)){
                scrollOneUnit(scrollDirection);
              }
            }
        });
    }

    /**
     * Toggles the visibility of the various sequences
     * @param seqDisplay The desired display type
     * @param visibility <code>true</code> if sequence should be visible
     * @throws java.security.InvalidParameterException if <code>seqDisplay</code>
     * 		is set to <code>DNA_DISPLAY</code> and <code>visibility</code>
     * 		is <code>false</code>
     */
    public void setSequenceVisible (int seqDisplay, boolean visiblity) {

	if (seqDisplay == dnaIndex && visiblity == false) {
            throw new java.security.InvalidParameterException("Unable to remove DNA Sequence");
        }


        if (visiblity) {
            indexMap.put(new Integer(seqDisplay), new Integer(seqDisplay));
        } else {
            indexMap.remove(new Integer(seqDisplay));
        }
        long previousLocation = seqModel.cellToLocation(getTopVisibleRow(),0);
        seqModel.removeAll();

        int pos = 0;
        Iterator iterator = indexMap.values().iterator();
        while (iterator.hasNext()) {
            int seqIndex = ((Integer)iterator.next()).intValue();
            if (seqIndex == dnaIndex || seqIndex == this.COMPLEMENT_DNA_DISPLAY) {
                seqModel.addSequence(pos++, ((GenomicSequence)sequenceArray[seqIndex]).getLabel(), sequenceArray[seqIndex], range);
            } else {
                seqModel.addSequence(pos++, ((GenomicSequence)sequenceArray[seqIndex]).getLabel(), sequenceArray[seqIndex], translatedRange);
            }
        }

        super.fireSequenceViewerChanged(previousLocation);
    }

    /**
     * Returns the visibility of a given sequence
     * @param seqDisplay The desired display type
     * @return <code>true</code> if sequence is visible
     */
    public boolean isSequenceVisible(int seqDisplay) {
       return indexMap.containsKey(new Integer(seqDisplay));
    }

    /**
     * Acessor method for Sequences shown in a given display
     * @param seqDisplay The desired display type
     * @return <code>Sequence</code> that represents given display
     *
     */
    public Sequence getSequenceAt(int seqDisplay) {
        return seqModel.getSequenceAt(getDisplayIndex(seqDisplay));
    }

    public void setSequenceReversed(boolean value) {
//	int oldSequence = dnaIndex;
//	setSequenceVisible (dnaIndex, false);
        if (value) {
            dnaIndex = this.REVERSE_COMPLEMENT_DNA_DISPLAY;
            seqModel.setCountLabelDirection(SeqTableModel.BACKWARD_LABEL_COUNT);
        } else {
            dnaIndex = this.DNA_DISPLAY;
            seqModel.setCountLabelDirection(SeqTableModel.FORWARD_LABEL_COUNT);
        }

//	setSequenceVisible (oldSequence, false);
//	setSequenceVisible(dnaIndex, true);
        fireSequenceViewerChanged();
    }

    public boolean isSequenceReversed() {
        if (dnaIndex == this.REVERSE_COMPLEMENT_DNA_DISPLAY) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets up the DNASequence and all of the offset sequences, and makes the
     * DNASequence visible.
     * @param sequence the DNA Sequence
     * @param startLocation the starting location for the DNA sequence
     */
    public void setDNASequence(Sequence sequence, long startLocation) {
        range = new SwingRange(startLocation, (sequence.length() + startLocation - 1));
        translatedRange = range;

        sequenceArray[DNA_DISPLAY] = new DNA_Sequence(null, sequence);
	sequenceArray[REVERSE_COMPLEMENT_DNA_DISPLAY] = new DNA_Reverse_Complement(null, sequenceArray[this.DNA_DISPLAY]);
        sequenceArray[ORF_NEG3_DISPLAY] = new ORF_minus_3(" Frame -3 ", sequenceArray[dnaIndex]);
        sequenceArray[ORF_NEG2_DISPLAY] = new ORF_minus_2(" Frame -2 ", sequenceArray[dnaIndex]);
        sequenceArray[ORF_NEG1_DISPLAY] = new ORF_minus_1(" Frame -1 ", sequenceArray[dnaIndex]);
        sequenceArray[COMPLEMENT_DNA_DISPLAY] = new DNA_Complement("", sequenceArray[dnaIndex]);
        sequenceArray[ORF_3_DISPLAY] = new ORF_plus_3(" Frame +3 ", sequenceArray[dnaIndex]);
        sequenceArray[ORF_2_DISPLAY] = new ORF_plus_2(" Frame +2 ", sequenceArray[dnaIndex]);
        sequenceArray[ORF_1_DISPLAY] = new ORF_plus_1(" Frame +1 ", sequenceArray[dnaIndex]);

        if(dnaIndex != this.REVERSE_COMPLEMENT_DNA_DISPLAY)
	   setSequenceVisible (dnaIndex, true);

        this.scrollToLocation(startLocation);
    }

    public Sequence getDNASequence() {
        return sequenceArray[dnaIndex];
    }

    /**
     * Resets the translated range for this <code>GenomicSequenceViewer</code>.
     * Applies the new settings to all contained sequences.
     * @param startLocation the new starting location
     * @param endLocation the new end location
     */
    public void setTranslatedRange(long startLocation, long endLocation) {
        translatedRange = new SwingRange(startLocation, endLocation);
        long previousLocation = seqModel.cellToLocation(getTopVisibleRow(),0);
        seqModel.removeAll();

        int pos = 0;
        Iterator iterator = indexMap.values().iterator();
        while (iterator.hasNext()) {
            int seqIndex = ((Integer)iterator.next()).intValue();
            if (seqIndex == dnaIndex || seqIndex == this.COMPLEMENT_DNA_DISPLAY) {
                seqModel.addSequence(pos++, ((GenomicSequence)sequenceArray[seqIndex]).getLabel(), sequenceArray[seqIndex], range);
            } else {
                seqModel.addSequence(pos++, ((GenomicSequence)sequenceArray[seqIndex]).getLabel(), sequenceArray[seqIndex], translatedRange);
            }
        }

        super.fireSequenceViewerChanged(previousLocation);
    }

    /**
     * Sets the Translation Style for this <code>GenomicSequenceViewer</code>.
     * @param style the desired translation style
     * @see #getTranslationStyle
     */
    public void setTranslationStyle(int style) {
        translationStyle = style;

        super.fireSequenceViewerChanged();
    }

    /**
     * Gets the Translation Style for this <code>GenomicSequenceViewer</code>.
     * @return the translation style used
     * @see #setTranslationStyle
     */
    public int getTranslationStyle() {
        return translationStyle;
    }

    /**
     * Get the Display index for a given sequence
     * @return The display index for the desired sequence, or <code>-1</code>
     *         if the sequence is not found
     */
    protected int getDisplayIndex(int seqDisplay) {
        int index = -1;
        Iterator iterator = indexMap.values().iterator();
        while (iterator.hasNext()) {
            index++;
            int value = ((Integer)iterator.next()).intValue();
            if (value == seqDisplay) {
                return index;
            }
        }
        return -1;
    }



    /**
     * The Genomic Sequence interface provides the ability
     * to get a label for a desired sequence.
     */
    interface GenomicSequence extends Sequence {
        public String getLabel();
    }

    /**
     * Inner class used to model a DNA_Sequence
     */
    class DNA_Sequence extends ViewerSequence implements GenomicSequence {
        private String label;

        public DNA_Sequence(String label, Sequence sequence) {
            super(sequence);

            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    class DNA_Complement extends ViewerSequence implements GenomicSequence {
        private String label;

        public DNA_Complement(String label, Sequence sequence) {
            super(sequence);

            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public long length() {
            return sequenceArray[dnaIndex].length();
        }

        public int get(long location) {
            return DNA.complement(super.get(location));
        }
    }

    class DNA_Reverse_Complement extends ViewerSequence implements GenomicSequence {
        private String label;

        public DNA_Reverse_Complement(String label, Sequence sequence) {
            super(DNA.reverseComplement(sequence));

            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public long length() {
            return sequenceArray[DNA_DISPLAY].length();
        }

        public int get(long location) {
            return super.get(location);
        }
    }

    class ORF_plus_1 extends ViewerSequence implements GenomicSequence {
        private String label;

        public ORF_plus_1(String label, Sequence sequence) {
            super(Protein.convertDNASequence(sequence));
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public long length() {
            return super.length();
        }

        public int get(long location) {
            if (((location+3) % 3 == 0) && (location/3 < length())) {
                return super.get(location/3);
            } else {
                return -1;
            }
        }

        public char baseToChar(long location) {
            if (translationStyle == SINGLE_CHAR_TRANSLATION) {
                int value = get(location);
                if (value < 0) {
                    return ' ';
                }
                return Protein.proteinToChar(value);
            } else {
                long realLocation = location/3;
                long charLocation = location%3;

                int value = sequenceArray[dnaIndex].get(realLocation);
                if (value < 0) {
                    return ' ';
                }
     //            if ((location+3) % 3 == 0 && realLocation < super.length()) {
                return Protein.proteinToAbbreviatedName(value).charAt((int)charLocation);
     //            }
            }
        }
    }

    class ORF_plus_2 extends ViewerSequence implements GenomicSequence {
        private String label;

        public ORF_plus_2(String label, Sequence sequence) {
            super(Protein.convertDNASequence(new ShiftedSequence(-1, sequence)));
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public long length() {
            return sequenceArray[dnaIndex].length();
        }

        public int get(long location) {

            if (((location+2) % 3 == 0)) {// && location/3 < sequence.length()) {
                return super.get(location/3);
            } else {
                return -1;
            }
        }

        public char baseToChar(long location) {
           if (translationStyle == SINGLE_CHAR_TRANSLATION) {
                int value = get(location);
                if (value < 0) {
                    return ' ';
                }
                 return Protein.proteinToChar(value);
            } else {
                location--;
                long realLocation = location/3;
                long charLocation = location%3;

                int value = super.get(realLocation);
                if (value < 0 || (seqModel.getMinAxisLocation() - location == 0) ||
                    charLocation < 0) {
                    return ' ';
                }
                return Protein.proteinToAbbreviatedName(value).charAt((int)charLocation);
            }
        }
    }

    class ORF_plus_3 extends ViewerSequence implements GenomicSequence {
        private String label;

        public ORF_plus_3(String label, Sequence sequence) {
            super(Protein.convertDNASequence(new ShiftedSequence(-2, sequence)));
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public long length() {
            return sequenceArray[dnaIndex].length();
        }

        public int get(long location) {
            if ((location+1) % 3 == 0) { //&& location/3 < sequence.length()) {
                return super.get(location/3);
            } else {
                return -1;
            }
        }

        public char baseToChar(long location) {
           if (translationStyle == SINGLE_CHAR_TRANSLATION) {
                int value = get(location);
                if (value < 0) {
                    return ' ';
                }
                 return Protein.proteinToChar(value);
            } else {
                location -= 2;
                long realLocation = location/3;
                long charLocation = location%3;

                int value = super.get(realLocation);
                if (value < 0 || (seqModel.getMinAxisLocation() - location == 0) ||
                    charLocation < 0) {
                    return ' ';
                }
                return Protein.proteinToAbbreviatedName(value).charAt((int)charLocation);
            }
        }
    }

    class ORF_minus_1 extends ViewerSequence implements GenomicSequence {
        private String label;

        public ORF_minus_1(String label, Sequence sequence) {
            super(Protein.convertDNASequence(DNA.reverseComplement(sequence)));
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public long length() {
            return sequenceArray[dnaIndex].length();
        }

        public int get(long location) {
            int pos = (int)Math.abs(location - length());
            if (pos % 3 == 0) {
        //        this.sequence = Protein.convertDNASequence(DNA.reverseComplement(sequenceArray[dnaIndex]));
                return super.get((Math.abs(location - length()))/3-1);
            } else {
                return -1;
            }
        }

        public char baseToChar(long location) {
            if (translationStyle == SINGLE_CHAR_TRANSLATION) {
                int value = get(location);
                if (value < 0) {
                    return ' ';
                }
                return Protein.proteinToChar(value);
            } else {
                long realLocation = location/3;
                long charLocation = location%3;

                int value = super.get(realLocation);
                if (value < 0) {
                    return ' ';
                }
                return Protein.proteinToAbbreviatedName(value).charAt((int)charLocation);
            }
        }
    }

    class ORF_minus_2 extends ViewerSequence implements GenomicSequence {
        private String label;

        public ORF_minus_2(String label, Sequence sequence) {
            super(Protein.convertDNASequence(new ShiftedSequence(-1, DNA.reverseComplement(sequence))));
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public long length() {
            return sequenceArray[dnaIndex].length();
        }

        public int get(long location) {
            int pos = (int)Math.abs(location - length())-1;

        //    this.sequence = Protein.convertDNASequence(new ShiftedSequence(-1, DNA.reverseComplement(dnaSequence)));
            if (pos % 3 == 0 && location/3 < super.length()) {
                return super.get((Math.abs(location - length())-1)/3-1);
            } else {
                return -1;
            }
        }

        public char baseToChar(long location) {
           if (translationStyle == SINGLE_CHAR_TRANSLATION) {
                int value = get(location);
                if (value < 0) {
                    return ' ';
                }
                return Protein.proteinToChar(value);
            } else {
                int pos = (int)Math.abs(location - length())-2;
                long realLocation = pos/3;
                long charLocation = Math.abs(pos%3 - 2);
                location = (Math.abs(location - length())-1)/3-1;

                int value = super.get(realLocation);
                if (value < 0 || (seqModel.getMinAxisLocation() - location == 0) ||
                    charLocation > 2) {
                    return ' ';
                }
                return Protein.proteinToAbbreviatedName(value).charAt((int)charLocation);
            }
        }
    }

    class ORF_minus_3 extends ViewerSequence implements GenomicSequence {
        private String label;

        public ORF_minus_3(String label, Sequence sequence) {
            super(Protein.convertDNASequence(new ShiftedSequence(-2, DNA.reverseComplement(sequence))));
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public long length() {
            return sequenceArray[dnaIndex].length();
        }

        public int get(long location) {
            int pos = (int)Math.abs(location - length())+1;
           // this.sequence = Protein.convertDNASequence(new ShiftedSequence(-2, DNA.reverseComplement(dnaSequence)));
            if (pos % 3 == 0 && location/3 < super.length()) {
               return super.get((Math.abs(location - length())-1)/3-1);
            } else {
                return -1;
            }
        }

        public char baseToChar(long location) {
           if (translationStyle == SINGLE_CHAR_TRANSLATION) {
                int value = get(location);
                if (value < 0) {
                    return ' ';
                }
                return Protein.proteinToChar(value);
            } else {
                int pos = (int)Math.abs(location - length())-3;
                long realLocation = pos/3;
                long charLocation = Math.abs(pos%3 - 2);
                location = (Math.abs(location - length())-1)/3-1;

                int value = super.get(realLocation);
                if (value < 0 || (seqModel.getMinAxisLocation() - location == 0) ||
                    charLocation > 2) {
                    return ' ';
                }
                return Protein.proteinToAbbreviatedName(value).charAt((int)charLocation);
            }
        }
    }
}
