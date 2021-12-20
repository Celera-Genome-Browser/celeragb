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
package api.stub.sequence;

/**
 * Title:        SequenceFromFastaBuilder
 * Description:  This project is for JBuilder 4.0
 * @author Les Foster
 * @version $Id$
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

/**
 * Given locator string, objects of this class will locate sequence
 * at given ranges on demand.
 */
public class SequenceFromFastaBuilder implements SequenceBuilder {
  //----------------------------------------CONSTANTS
  public static final String NCBI_CHROMO_FASTA_EXTENSION = ".fna";
  private String CR_LF = "\r\n";
  private String LF = "\n";
  private String READ_ONLY_MODE = "r";

  //----------------------------------------INSTANCE VARIABLES
  private String mPath = null;
  private long mDeflineLen;
  private long mEndOfLineLen;
  private long mLineLen;
  private long mLengthInResidues;
  private String mLineTerminator;

  //----------------------------------------UNIT TEST CODE
  public static void main(String[] args) {
    try {
      if (args.length == 0)
        throw new IllegalArgumentException("Must add file path to test.");
      SequenceFromFastaBuilder finder = new SequenceFromFastaBuilder(args[0]);
      Sequence foundSeq = finder.getSubSequence(0/*5*60*/, 1*60 + 3);
      System.out.println("Got these residues "+DNA.toString(foundSeq));
      System.out.println("Total number residues is "+finder.getLength());
    } // End try to test
    catch (Exception ex) {
      ex.printStackTrace();
    } // End catch from attempted test.
  } // End method: main

  //----------------------------------------CONSTRUCTORS
  /** Presets the path in which to look for sequence. */
  public SequenceFromFastaBuilder(String path) throws Exception {
    // Open as a file to deduce metadata.
    mPath = path;
    File fastaFile = new File(path);
    if (! fastaFile.canRead())
      throw new IllegalArgumentException("Cannot open FASTA file "+path+" which has an absolute path of "+fastaFile.getAbsolutePath());
    if (! fastaFile.isFile())
      throw new IllegalArgumentException("Path "+path+", given as FASTA path, is not a file");

    long lengthOfFile = fastaFile.length();

    // Read file...
    FileInputStream fastaInputStream = new FileInputStream(fastaFile);
    int inChar = 0;
    int prevChar = -1;
    boolean foundBeforeEnd = false;
    int charCount = 0;
    while (-1 != (inChar = fastaInputStream.read())) {
      if (inChar == '\n') {
        foundBeforeEnd = true;
        break;
      } // Got a definite terminator.

      prevChar = inChar;
      charCount ++;
    } // For all chars until E-O-File or break.

    if (! foundBeforeEnd) {
      throw new IllegalArgumentException("Path "+path+", whose absolute path is "+fastaFile.getAbsolutePath()+" does not have \n or \r\n line breaks");
    } // Not there.

    char firstTerminator = 0;
    if (prevChar == '\r') {
      firstTerminator = (char)prevChar;
      mLineTerminator = CR_LF;
      mEndOfLineLen = 2;
    } // cr-lf
    else {
      firstTerminator = (char)inChar;
      mLineTerminator = LF;
      mEndOfLineLen = 1;
    } // just lf

    mDeflineLen = charCount - mEndOfLineLen + 1;

    // Now read the second line--judged to be typical of all remaining lines
    // in the file--to see how long it is.
    charCount = 0;
    while (-1 != (inChar = fastaInputStream.read())) {
      if (inChar == firstTerminator) {
        break;
      } // Found the first terminating character.
      charCount ++;
    } // For all chars until E-O-File or break.

    mLineLen = charCount;

    // Stop reading the file.
    fastaInputStream.close();

    // How many residues total?
    long numberOfWholeLines = (lengthOfFile - mDeflineLen - mEndOfLineLen) / (mLineLen + mEndOfLineLen);
    long remainderLineLength = (lengthOfFile - mDeflineLen - mEndOfLineLen) % (mLineLen + mEndOfLineLen);
    mLengthInResidues = (numberOfWholeLines * mLineLen) + remainderLineLength
         - ((remainderLineLength == 0) ? 0 : mEndOfLineLen);

    //System.out.println("Defline length = "+this.mDeflineLen+" end-of-line len is "+this.mEndOfLineLen+" Line len is "+this.mLineLen);
  } // End constructor

  //----------------------------------------INTERFACE METHODS
  /**
   * Return the sequence object for the range given.
   */
  public Sequence getSubSequence(long startPosInResidues, long numberOfCharsToRead) throws Exception {
    RandomAccessFile randFile = new RandomAccessFile(mPath, READ_ONLY_MODE);
    randFile.seek(getSeekPointTo(startPosInResidues));
    String inbuf = null;

    long numberOfCharsAlreadyRead = 0L;
    SequenceList sequenceList = new SequenceList(SequenceList.KIND_DNA);
    Sequence nextSequence = null;
    long diffInChars = 0L;
    long keepLength = 0L;

    while (null != (inbuf = randFile.readLine())) {
      numberOfCharsAlreadyRead += inbuf.length();
      diffInChars = numberOfCharsAlreadyRead - numberOfCharsToRead;
      if (diffInChars > 0L)
        keepLength = inbuf.length() - diffInChars;
      else
        keepLength = inbuf.length();

      // Append to list.
      nextSequence = new DNASequenceParser(inbuf.toCharArray(), 0, (int)keepLength);
      sequenceList.append(DNASequenceStorage.create(nextSequence));

      if (diffInChars >= 0L) {
        break;
      } // Found termination condition.  All requested chars have been read.
    } // For all lines of input or until break.

    return sequenceList;

  } // End method: getSubSequence

  /** Returns number of residues (not bytes in file). */
  public long getLength() { return mLengthInResidues; }

  //------------------------------------------------HELPER METHODS
  /** Find out where to go in the FASTA file to find the location in residues. */
  private long getSeekPointTo(long thisLocationInResidues) {
    return thisLocationInResidues + mDeflineLen + mEndOfLineLen +
      ((thisLocationInResidues / mLineLen) * mEndOfLineLen);
  } // End method: getSeekPointTo

} // End class: SequenceFromFastaBuilder
