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
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/
package client.shared.swing.genomic;

import api.stub.sequence.DNA;
import api.stub.sequence.Protein;
import api.stub.sequence.Sequence;
import api.stub.sequence.SequenceHelper;
import client.gui.other.util.PairedLayout;
import client.shared.text_component.StandardTextArea;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class SequenceSearchDialog extends JDialog implements ActionListener, DocumentListener {
  public static final String NUCLEOTIDE = "Nucleotide";
  public static final String AMINO_ACID = "Amino Acid";
  private long sequenceOffset = 0;
  JPanel mainPanel, eastPanel, northPanel, southPanel, nsouthPanel, northCenterPanel,
    northBottomPanel, centerPanel, groupPanel1, groupPanel2, groupPanel3;
  JScrollPane scrollPane;

  JCheckBox matchCaseCheckBox;
  JButton findNextButton, findAllButton, resetButton, cancelButton;
  JLabel findWhatLabel = null;
  JLabel resultLabel = null;
  JTextArea findWhatTextArea = null;
  JLabel lookInLabel = null;
  JComboBox lookInCombo= null;
  ButtonGroup gp1, gp2, gp3;
  JRadioButton upRadioButton, downRadioButton;
  JRadioButton oneLetterRadioButton, threeLetterRadioButton;
  JTable locationTable;
  AbstractTableModel tableModel;

  String cmdLookIn = "LookIn";
  String cmdFindNext = "Find Next";
  String cmdFindAll = "Find All >>";
  String cmdCancel = "Cancel";
  String cmdReset = "New Search";
  String cmdUp = "Up";
  String cmdDown = "Down";
  String cmdOneLetter = "1";
  String cmdThreeLetter = "3";
  String cmdMatchCase = "MatchCase";

  String cmdNucleotideReverse = "Nucleotide Reverse Complement";
  String cmdAminoAcidFrameOne = " ORF +1";
  String cmdAminoAcidFrameTwo = " ORF +2";
  String cmdAminoAcidFrameThree= " ORF +3";
  String cmdAminoAcidFrameAll= " All ORF's";

  String scope = NUCLEOTIDE;
  String direction = cmdDown;
  boolean bReverseComplement = false;
  int frameType = Protein.FRAME_PLUS_ONE;

  java.util.List frameList=new ArrayList();
  Vector listeners = new Vector();
  Vector allLocations = new Vector();

  int translation = 1;

  String[] lookInSelections = { NUCLEOTIDE, cmdNucleotideReverse, cmdAminoAcidFrameOne,
    cmdAminoAcidFrameTwo, cmdAminoAcidFrameThree,cmdAminoAcidFrameAll };

  private Sequence baseSequence;
  private String targetSearchString;
  private int prevStartPos = 0;
  private int prevEndPos = 0;
  private String cmdNucleotide = "Nucleotide";
  private String cmdAminoAcid = "Amino Acid";


  public SequenceSearchDialog(JFrame parentFrame, String title, boolean modal) {
    super(parentFrame, title, modal);
    try {
      jbInit();
      initialize();
      pack();
      this.setLocationRelativeTo(parentFrame);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  void jbInit() throws Exception {
    mainPanel = new JPanel(new BorderLayout(5, 5));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    mainPanel.setMinimumSize(new Dimension(420, 190));
    mainPanel.setPreferredSize(new Dimension(420, 190));

    centerPanel = new JPanel(new BorderLayout());

    northPanel = new JPanel(new BorderLayout(5, 5));

    northCenterPanel = new JPanel(new PairedLayout(5, 5));
    findWhatLabel = new JLabel("Find what:");
    findWhatLabel.setPreferredSize(new Dimension(60, 24));
    findWhatLabel.setMinimumSize(new Dimension(60, 24));
    findWhatTextArea = new StandardTextArea(4, 32);
    findWhatTextArea.setLineWrap(true);
    scrollPane = new JScrollPane(findWhatTextArea);
    scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
    findWhatTextArea.getDocument().addDocumentListener ( this );
    northCenterPanel.add(PairedLayout.LABEL, findWhatLabel);
    northCenterPanel.add(PairedLayout.FIELD, scrollPane);
    northPanel.add(northCenterPanel, BorderLayout.CENTER);

    northBottomPanel = new JPanel(new PairedLayout(5, 5));
    lookInLabel = new JLabel("Look in:");
    lookInLabel.setPreferredSize(new Dimension(60, 24));
    lookInLabel.setMinimumSize(new Dimension(60, 24));
    lookInCombo = new JComboBox();
    for ( int i = 0;  i < lookInSelections.length; i++ ) {
      lookInCombo.addItem(lookInSelections[i]);
    }
    lookInCombo.setSelectedItem(lookInSelections[0]);
    lookInCombo.addActionListener(this);
    lookInCombo.setActionCommand(cmdLookIn);
    northBottomPanel.add(PairedLayout.LABEL, lookInLabel);
    northBottomPanel.add(PairedLayout.FIELD, lookInCombo);
    northPanel.add(northBottomPanel, BorderLayout.SOUTH);

    centerPanel.add(northPanel, BorderLayout.NORTH);

    eastPanel = new JPanel();
    BoxLayout boxLayout = new BoxLayout(eastPanel, BoxLayout.Y_AXIS);
    eastPanel.setLayout(boxLayout);
    findNextButton = new JButton(cmdFindNext);
    findNextButton.setMnemonic('F');
    findNextButton.addActionListener(this);
    findNextButton.setActionCommand(cmdFindNext);
    findNextButton.setEnabled(false);
    findNextButton.setMaximumSize(new Dimension(102, 26));
    findNextButton.setMinimumSize(new Dimension(102, 26));

    findAllButton = new JButton(cmdFindAll);
    findAllButton.setMnemonic('A');
    findAllButton.addActionListener(this);
    findAllButton.setActionCommand(cmdFindAll);
    findAllButton.setEnabled(false);
    findAllButton.setMaximumSize(new Dimension(102, 26));
    findAllButton.setMinimumSize(new Dimension(102, 26));

    resetButton = new JButton(cmdReset);
    resetButton.setMnemonic('w');
    resetButton.addActionListener(this);
    resetButton.setActionCommand(cmdReset);
    resetButton.setMaximumSize(new Dimension(102, 26));
    resetButton.setMinimumSize(new Dimension(102, 26));

    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(this);
    cancelButton.setActionCommand(cmdCancel);
    cancelButton.setMaximumSize(new Dimension(102, 26));
    cancelButton.setMinimumSize(new Dimension(102, 26));

    eastPanel.add(findNextButton);
    eastPanel.add(Box.createVerticalStrut(5));
    eastPanel.add(findAllButton);
    eastPanel.add(Box.createVerticalStrut(5));
    eastPanel.add(resetButton);
    eastPanel.add(Box.createVerticalStrut(5));
    eastPanel.add(cancelButton);

    groupPanel1 = new JPanel();
    boxLayout = new BoxLayout(groupPanel1, BoxLayout.Y_AXIS);
    groupPanel1.setLayout(boxLayout);
    groupPanel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Direction", TitledBorder.LEFT, TitledBorder.CENTER));
    gp1 = new ButtonGroup();
    upRadioButton = new JRadioButton("Up");
    upRadioButton.setActionCommand(cmdUp);
    upRadioButton.addActionListener(this);
    downRadioButton = new JRadioButton("Down");
    downRadioButton.setActionCommand(cmdDown);
    downRadioButton.addActionListener(this);
    downRadioButton.setSelected(true);
    groupPanel1.add(upRadioButton);
    groupPanel1.add(downRadioButton);
    gp1.add(upRadioButton);
    gp1.add(downRadioButton);
    centerPanel.add(groupPanel1, BorderLayout.WEST);

    groupPanel2 = new JPanel();
    boxLayout = new BoxLayout(groupPanel2, BoxLayout.Y_AXIS);
    groupPanel2.setLayout(boxLayout);
    groupPanel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Translation", TitledBorder.LEFT, TitledBorder.CENTER));
    gp2 = new ButtonGroup();
    oneLetterRadioButton = new JRadioButton("One Letter");
    oneLetterRadioButton.setActionCommand(cmdOneLetter);
    oneLetterRadioButton.addActionListener(this);
    oneLetterRadioButton.setSelected(true);
    threeLetterRadioButton = new JRadioButton("Three Letter");
    threeLetterRadioButton.setActionCommand(cmdThreeLetter);
    threeLetterRadioButton.addActionListener(this);
    groupPanel2.add(oneLetterRadioButton);
    groupPanel2.add(threeLetterRadioButton);
    gp2.add(oneLetterRadioButton);
    gp2.add(threeLetterRadioButton);
    centerPanel.add(groupPanel2, BorderLayout.CENTER);
    setTranslationEnable(false);

    groupPanel3 = new JPanel();
    boxLayout = new BoxLayout(groupPanel3, BoxLayout.Y_AXIS);
    groupPanel3.setLayout(boxLayout);
    groupPanel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Options", TitledBorder.LEFT, TitledBorder.CENTER));
    matchCaseCheckBox = new JCheckBox("Case Sensitive");
    groupPanel3.add(matchCaseCheckBox);
    centerPanel.add(groupPanel3, BorderLayout.EAST);

    southPanel = new JPanel(new BorderLayout(5,5));
    nsouthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
    resultLabel = new JLabel("Number of hit:");
    nsouthPanel.add(resultLabel);
    southPanel.add(nsouthPanel, BorderLayout.NORTH);

    locationTable = new JTable();
    locationTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
    locationTable.setAutoscrolls(true);
    locationTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    locationTable.addMouseListener( new MouseAdapter() {
      public void mouseReleased(MouseEvent evt) {
        Point pt = evt.getPoint();
        int rowIndex = locationTable.rowAtPoint(pt);
        int fromIndex = Integer.parseInt((tableModel.getValueAt(rowIndex, 1)).toString());
        int toIndex = Integer.parseInt((tableModel.getValueAt(rowIndex, 2)).toString());
        fireFocusOnSearchTarget(new SwingRange(fromIndex, toIndex));
      }

      public void mouseClicked(MouseEvent evt) {
        Point pt = evt.getPoint();
        int rowIndex = locationTable.rowAtPoint(pt);
        int fromIndex = Integer.parseInt((tableModel.getValueAt(rowIndex, 1)).toString());
        int toIndex = Integer.parseInt((tableModel.getValueAt(rowIndex, 2)).toString());
        fireFocusOnSearchTarget(new SwingRange(fromIndex, toIndex));
      }
    });
    southPanel.add( new JScrollPane( locationTable ), BorderLayout.CENTER );
    southPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Search Result", TitledBorder.LEFT, TitledBorder.CENTER));

    mainPanel.add(eastPanel, BorderLayout.EAST);
    mainPanel.add(centerPanel, BorderLayout.CENTER);

    this.getContentPane().add(mainPanel);

    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        cancelFindActions();
      }
    });

    tableModel = new AbstractTableModel() {
      String[] columnNames = {"Item", "From", "To", "ORF" };

      public int getRowCount() { return allLocations.size(); }

      public int getColumnCount() { return columnNames.length; }

      public Object getValueAt(int row, int col) {
        Point point = (Point)allLocations.elementAt(row);
        if ( col == 0 )
          return (new Integer(row+1));
        else if ( col == 1) {
          if (!bReverseComplement)
            return (new Long((long)point.getX() + sequenceOffset));
          else {
            /**
             * When searching the reverse complement of the sequence we need to
             * pass back the coordinates wrt the forward sequence.
             */
            long min = (sequenceOffset+baseSequence.length()) - (long)point.getY() - 1;
            return ( new Long( min ));
          }
        }
        else if(col==2) {
          if (!bReverseComplement)
            return (new Long((long)point.getY() + sequenceOffset));
          else {
            /**
             * When searching the reverse complement of the sequence we need to
             * pass back the coordinates wrt the forward sequence.
             */
            long max = (sequenceOffset+baseSequence.length()) - (long)point.getX() - 1;
            return ( new Long( max ));
          }
        }
        else if(col==3) {
         if (frameList!=null && !frameList.isEmpty()) return frameList.get(row);
        }
        return "";
       }

      public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
      }

      public Class getColumnClass(int columnIndex) {
        return allLocations.elementAt(0).getClass();
      }
    };

    locationTable.setModel(tableModel);
    TableColumnModel tcm = locationTable.getColumnModel();
    for ( int i = 0; i < tcm.getColumnCount(); i++ ) {
      TableColumn col = tcm.getColumn(i);
      if ( i == 0 ) {
        col.setMinWidth(40);
        col.setMaxWidth(40);
        col.setWidth(40);
      }
      else {
        col.setMinWidth(120);
        col.setMaxWidth(200);
        col.setWidth(160);
      }
    }
  }


  private void showFindAllResultPanel(Vector locs) {
    allLocations = new Vector(locs);

    locationTable.setRowSelectionInterval(0, 0);

    Point pt = (Point)allLocations.elementAt(0);
    fireFocusOnSearchTarget(new SwingRange((int)pt.getX() + sequenceOffset,
      (int)pt.getY() + sequenceOffset));

    locationTable.revalidate();
    southPanel.setMinimumSize(new Dimension(420, 200));
    southPanel.setPreferredSize(new Dimension(420, 200));
    this.getContentPane().removeAll();
    this.getContentPane().add(mainPanel, BorderLayout.NORTH);
    this.getContentPane().add(southPanel, BorderLayout.CENTER);
    this.pack();
    findWhatTextArea.requestFocus();
    findWhatTextArea.setCaretPosition(0);
    findWhatTextArea.select(0, 0);
    resultLabel.setText("Number of hits: "+ allLocations.size());
  }

  public void showSearchDialog(Sequence baseSequence, long sequenceOffset) {
    this.sequenceOffset = sequenceOffset;
    this.baseSequence = baseSequence;
    findWhatTextArea.requestFocus();
    findWhatTextArea.setCaretPosition(0);
    findWhatTextArea.setEditable(true);
    setVisible(true);
  }


  private void initialize() {
    this.getContentPane().removeAll();
    this.getContentPane().add(mainPanel);
    this.pack();
    allLocations.clear();
    frameList.clear();
    findWhatTextArea.setCaretPosition(0);
    findWhatTextArea.removeAll();
    findWhatTextArea.repaint();
    findWhatTextArea.revalidate();
  }


  public void dispose() {
    listeners.clear();
  }


  private void setTranslationEnable(boolean bEnable) {
    oneLetterRadioButton.setEnabled(bEnable);
    threeLetterRadioButton.setEnabled(bEnable);
  }


  public void addSequenceSearchListener(SequenceSearchListener listener) {
    if ( !listeners.contains(listener) )
      listeners.addElement(listener);
  }


  public void removeSequenceSearchListener(SequenceSearchListener listener) {
    if ( listeners.contains(listener) )
      listeners.removeElement(listener);
  }


  public void insertUpdate(DocumentEvent e) {
    if (findWhatTextArea.getText().length() > 0) {
      findNextButton.setEnabled(true);
      findAllButton.setEnabled(true);
    }
    else {
      findNextButton.setEnabled(false);
      findAllButton.setEnabled(false);
    }
  }


  public void removeUpdate(DocumentEvent e) {
    if ( findWhatTextArea.getText().length() > 0 ) {
      findNextButton.setEnabled(true);
      findAllButton.setEnabled(true);
    }
    else {
      findNextButton.setEnabled(false);
      findAllButton.setEnabled(false);
    }
  }


  public void changedUpdate(DocumentEvent e) {
    if (findWhatTextArea.getText().length() > 0 ) {
      findNextButton.setEnabled(true);
      findAllButton.setEnabled(true);
    }
    else {
      findNextButton.setEnabled(false);
      findAllButton.setEnabled(false);
    }
  }


  public void actionPerformed(ActionEvent evt) {
    String cmd = evt.getActionCommand();
    if ( cmd ==  cmdFindNext) {
      boolean allAAFrameSelected=((String)lookInCombo.getSelectedItem()).equals(cmdAminoAcidFrameAll);
      if(!allAAFrameSelected){
        findNext(frameType);
      }
      else {
        //search in all frames one by one
        boolean searchFrame;
        searchFrame=findNext(Protein.FRAME_PLUS_ONE);
        if(searchFrame) displayMessageWhichFrameFound(Protein.FRAME_PLUS_ONE);

        // if not found in frame one continue looking in 2,3 frames
        if(!searchFrame) {
          searchFrame=findNext(Protein.FRAME_PLUS_TWO);
          if(searchFrame)displayMessageWhichFrameFound(Protein.FRAME_PLUS_TWO);
        }

        if(!searchFrame) {
          searchFrame=findNext(Protein.FRAME_PLUS_THREE);
          if(searchFrame)displayMessageWhichFrameFound(Protein.FRAME_PLUS_THREE);
        }
      }
    }

    else if ( cmd == cmdFindAll ) {
      this.getContentPane().removeAll();
      this.getContentPane().add(mainPanel);
      this.pack();
      allLocations.clear();
      frameList.clear();
      findWhatTextArea.repaint();
      findWhatTextArea.revalidate();

      boolean allAAFrameSelected=((String)lookInCombo.getSelectedItem()).equals(cmdAminoAcidFrameAll);
      Vector locations=new Vector();
      if(!allAAFrameSelected){
        locations = findAll(frameType);
        if (locations!=null) storeFramesForLocations(locations, frameType);
      }
      else {
        Vector locations1=new Vector();
        Vector locations2=new Vector();
        Vector locations3=new Vector();
        locations1 = findAll(Protein.FRAME_PLUS_ONE);
        //continue search with the frame 2
        locations2 = findAll(Protein.FRAME_PLUS_TWO);

        locations3 = findAll(Protein.FRAME_PLUS_THREE);

        //if search for frame 1 was successful, shift the offset to size list of locations1
         if(locations1!=null && !locations1.isEmpty()) {
           storeFramesForLocations(locations1, Protein.FRAME_PLUS_ONE);
         }

         if(locations2!=null && !locations2.isEmpty()) {
           storeFramesForLocations(locations2, Protein.FRAME_PLUS_TWO);
         }

         if(locations3!=null && !locations3.isEmpty()) {
           storeFramesForLocations(locations3, Protein.FRAME_PLUS_THREE);
        }

        locations.addAll(locations1);
        locations.addAll(locations2);
        locations.addAll(locations3);
     }


      if (locations != null ) {
        if (!locations.isEmpty() )
          showFindAllResultPanel(locations);
      }
    }

    else if ( cmd == cmdReset ) {
      initialize();
      fireFocusOnSearchTarget(new SwingRange(sequenceOffset,sequenceOffset));
    }

    else if ( cmd == cmdCancel ) {
      cancelFindActions();
    }

    else if ( cmd == cmdLookIn ) {
      String cmdSelected = (String)((JComboBox)evt.getSource()).getSelectedItem();
      if ( cmdSelected.equals(NUCLEOTIDE) ) {
        setTranslationEnable(false);
        scope = NUCLEOTIDE;
        bReverseComplement = false;
        frameType = 0;
      }

      else if ( cmdSelected.equals(cmdNucleotideReverse) ) {
        setTranslationEnable(false);
        scope = NUCLEOTIDE;
        bReverseComplement = true;
        frameType = 0;
      }

      else if ( cmdSelected.equals(cmdAminoAcidFrameOne) ) {
        setTranslationEnable(true);
        scope = AMINO_ACID;
        bReverseComplement = false;
        frameType = Protein.FRAME_PLUS_ONE;
      }

      else if ( cmdSelected.equals(cmdAminoAcidFrameTwo) ) {
        setTranslationEnable(true);
        scope = AMINO_ACID;
        bReverseComplement = false;
        frameType = Protein.FRAME_PLUS_TWO;
      }

      else if ( cmdSelected.equals(cmdAminoAcidFrameThree) ) {
        setTranslationEnable(true);
        scope = AMINO_ACID;
        bReverseComplement = false;
        frameType = Protein.FRAME_PLUS_THREE;
      }

      else if ( cmdSelected.equals(cmdAminoAcidFrameAll) ) {
        setTranslationEnable(true);
        scope = AMINO_ACID;
        bReverseComplement = false;
      }
    }

    else if ( cmd == cmdDown || cmd == cmdUp ) {
      direction = gp1.getSelection().getActionCommand();
    }

    else if ( cmd == cmdOneLetter || cmd == cmdThreeLetter ) {
      translation = Integer.parseInt(gp2.getSelection().getActionCommand());
    }
  }


  private void cancelFindActions() {
    this.setVisible(false);
  }

  private void storeFramesForLocations(Vector targets, int frame){
    for(int i=0; i<targets.size(); i++){
      frameList.add(new Integer(frame));
    }
  }


  private void displayMessageWhichFrameFound(int frame){
    JOptionPane.showMessageDialog(this, "Search Successful in Amino Acid Frame "+frame);
  }


/**
 * Code from the views
 */
  private Vector findAll(int frameType) {
    Vector locations = new Vector();
    if ( (locations = findAllCoordinates(frameType)).isEmpty() ) {
      // Put a warnning dialog here
       JOptionPane.showMessageDialog(this, getFormattedSearchString()+ " not found in Frame "+frameType+".  ", "Sequence Not Found", JOptionPane.WARNING_MESSAGE);
    }
    targetSearchString = getFormattedSearchString();
    return locations;
  }


  private Vector findAllCoordinates(int frameType) {
    String searchString = getFormattedSearchString();
    String residues = getEntireDNAString();
    Vector allLocations = new Vector();
    frameList.clear();
    tableModel.fireTableDataChanged();

    if (scope.equals(cmdNucleotide) ) {
      if ( bReverseComplement )
        residues = SequenceHelper.toString(DNA.reverseComplement(baseSequence));
    }
    else if ( scope.equals(cmdAminoAcid) ) {
      Sequence tmpSequence = Protein.convertDNASequenceToProteinORF(baseSequence, frameType);
      // For 3 letter translation search, convert residue to 3 letter sequence.
      if (translation == 3) {
        residues = SequenceHelper.toString(
          Protein.convertProteinSequenceStyle(tmpSequence, Protein.THREE_LETTER_TRANSLATION));
      }
      else residues = SequenceHelper.toString(tmpSequence);
    }

    if (!matchCaseCheckBox.isSelected()) residues = residues.toUpperCase();

    int oldStartPos = 0;
    int oldEndPos = -1;
    int newStartPos = 0;

    String subResidues = residues;
    while ( subResidues.length() >= searchString.length() ) {
      if ( ( oldStartPos == oldEndPos ) || (oldEndPos > 0) ) {
        subResidues = residues.substring(oldEndPos+1);
      }

      newStartPos = subResidues.indexOf(searchString);
      if ( newStartPos == -1 ) {
          return allLocations;
      }
      newStartPos = ( oldEndPos >= 0 && searchString != null) ?  (newStartPos+oldEndPos+1) : newStartPos;
      oldStartPos = newStartPos;
      oldEndPos = newStartPos + searchString.length()-1;

      int tmpStart=0, tmpEnd=0;
      if ( scope.equals(cmdNucleotide) ) {
        tmpStart=newStartPos;
        tmpEnd=newStartPos + searchString.length()-1;
        if (bReverseComplement) {
          int tmp = residues.length() - tmpStart - 1;
          tmpStart = residues.length() - tmpEnd - 1;
          tmpEnd = tmp;
        }
      }
      else if (scope.equals(cmdAminoAcid) ) {
        if ( translation == 1 ) {
          tmpStart=newStartPos*3+frameType-1;
          tmpEnd=newStartPos*3+(searchString.length()*3)+frameType-2;
        }
        else {
          tmpStart=newStartPos+frameType-1;
          tmpEnd=newStartPos + searchString.length()+frameType-2;
        }
      }
      allLocations.add(new Point(tmpStart, tmpEnd));
    }
    return allLocations;
  }


  private boolean findNext(int frameType) {
    boolean retBool= findNextStartPos(frameType);
    if ( !retBool ) JOptionPane.showMessageDialog(this, getFormattedSearchString()
      + " not found in Frame "+frameType+".  ", "Sequence Not Found", JOptionPane.WARNING_MESSAGE);

    return retBool;
  }


  private boolean findNextStartPos(int frameType) {
    String searchString = getFormattedSearchString();
    String residues = getEntireDNAString();
    frameList.clear();

    if (  scope.equals(cmdNucleotide) ) {
      if ( bReverseComplement )
        residues = SequenceHelper.toString(DNA.reverseComplement(baseSequence));
    }
    else if ( scope.equals(cmdAminoAcid) ) {
      residues = SequenceHelper.toString(
        Protein.convertDNASequenceToProteinORF(baseSequence, frameType));
    }

    if (!matchCaseCheckBox.isSelected()) residues = residues.toUpperCase();

    int newStartPos = 0;
    int firstStartPos = residues.indexOf(searchString);
    int lastStartPos = residues.lastIndexOf(searchString);

    if ( direction.toLowerCase().equals("up") ) {
      if ( prevStartPos <= firstStartPos ) {
        prevStartPos = 0;
        prevEndPos = -1;
        targetSearchString = "";
      }
      if ( prevStartPos > 0 ) {
        residues = residues.substring(0, prevStartPos);
      }

      newStartPos = residues.lastIndexOf(searchString);
      if ( newStartPos == -1 ) {
        if ( prevStartPos <= firstStartPos ) {
          prevStartPos = 0;
          prevEndPos = -1;
          targetSearchString = "";
        }
        return false;
      }
      targetSearchString = searchString;
      prevStartPos = newStartPos;
      prevEndPos = newStartPos + searchString.length()-1;
    }
    else if ( direction.toLowerCase().equals("down") ) {
      if ( prevEndPos >= lastStartPos ) {
        prevStartPos = 0;
        prevEndPos = -1;
        targetSearchString = "";
      }
      if ( (prevStartPos == prevEndPos) || (prevEndPos > 0) )
        residues = residues.substring(prevEndPos+1);
      newStartPos = residues.indexOf(searchString);
      if ( newStartPos == -1 ) {
          if ( prevEndPos >= lastStartPos ) {
            prevStartPos = 0;
            prevEndPos = -1;
            targetSearchString = "";
          }
          return false;
      }
      newStartPos = ( prevEndPos >= 0 && targetSearchString != null) ?  (newStartPos+prevEndPos+1) : newStartPos;
      targetSearchString = searchString;
      prevStartPos = newStartPos;
      prevEndPos = newStartPos + searchString.length()-1;
    }
    long tmpStart=0, tmpEnd=0;
    if ( scope.equals(cmdNucleotide) ) {
      tmpStart=newStartPos;
      tmpEnd=newStartPos + searchString.length()-1;
    }
    else if (scope.equals(cmdAminoAcid) ) {
      if ( translation == 1 ) {
        tmpStart=newStartPos*3+frameType-1;
        tmpEnd=newStartPos*3+(searchString.length()*3)+frameType-2;
      }
      else {
        tmpStart=newStartPos+frameType-1;
        tmpEnd=newStartPos + searchString.length()+frameType-2;
      }
    }
    if (bReverseComplement)
      fireFocusOnSearchTarget(new SwingRange(residues.length() - tmpEnd+sequenceOffset - 1, residues.length() - tmpStart+sequenceOffset - 1));
    else
      fireFocusOnSearchTarget(new SwingRange(tmpStart+sequenceOffset, tmpEnd+sequenceOffset));
    return true;
  }


  private void fireFocusOnSearchTarget(SwingRange targetRange) {
    for (Iterator it = listeners.iterator(); it.hasNext(); ) {
      ((SequenceSearchListener)it.next()).focusOnSearchTarget(targetRange);
    }
  }

  private String getEntireDNAString() {
    return SequenceHelper.toString(baseSequence);
  }

  private String getFormattedSearchString() {
    if (matchCaseCheckBox.isSelected()) return findWhatTextArea.getText().trim();
    else return findWhatTextArea.getText().toUpperCase().trim();
  }

}
