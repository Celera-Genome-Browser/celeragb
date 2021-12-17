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

/**
 * Title:        Genome Browser<p>
 * Description:  Information sufficient to open a sequence alignment<p>
 * @author Les Foster
 * @version $Id$
 */
package api.facade.concrete_facade.gff3;

import api.facade.facade_mgr.FacadeManager;
import api.stub.geometry.MutableRange;
import api.stub.geometry.Range;
import api.stub.sequence.Sequence;
import api.stub.sequence.SequenceBuilder;
import api.stub.sequence.SubSequence;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * This 'class' has little behavior: it really just constrains input, and
 * tries to ensure that it is provided correctly, and then it provides that
 * as needed.  It is needed more as a "structure" than anything else.
 */
public class SequenceAlignment {

  private int startOnAxis;
  private int endOnAxis;
  private int startOnEntity;
  private int endOnEntity;
  private String filePath;
  private String seqID;
  private String genomicAxisID;
  private Sequence alignerSequence;
  private SequenceBuilder sequenceBuilder;

  /**
   * Constraining constructor.  Takes all info up front.
   */
  public SequenceAlignment(String start, String end, String entityStart,
                           String entityEnd, String filePath, String seqID,
                           String genomicAxisID) {
      // Let's store all this away.
      // First we do all number conversions.
      try {
          startOnAxis = Integer.parseInt(start);
          endOnAxis = Integer.parseInt(end);
          startOnEntity = Integer.parseInt(entityStart);
          endOnEntity = Integer.parseInt(entityEnd);
          if (startOnEntity > endOnEntity) {
              int temp = startOnEntity;
              startOnEntity = endOnEntity;
              endOnEntity = temp;

              // HANDLE an exception, but continue to process.  Not a deal-killer.
              FacadeManager.handleException(new IllegalArgumentException("Illegal to Reverse-Order an Entity Alignment in a seq_alignment XML Tag.  Reverting to ascending order."));

          } // Invert the order of start/end if they are not increasing over the entity.
      } catch (Exception nfe) {
          // Note that this picks up NFEs and any null pointers as well.
          throw new IllegalArgumentException("One or more of the following is non-numeric:("+
              start + "," + end + "," + entityStart + "," + entityEnd + ")");
      } // End catch block for int conversions.

      if (Math.abs(startOnAxis - endOnAxis) != Math.abs(startOnEntity - endOnEntity)) {
          throw new IllegalArgumentException("Error in sequence alignment.  Entity length does not match Axis length for "+
              this.toString());
      } // Lengths of alignments MUST match.

      this.filePath = filePath;
      this.seqID = seqID;
      this.genomicAxisID = genomicAxisID;

      // Verify that there truly exists such a file.
      //
      try {
          File file = new File(filePath);
          if (! file.canRead())
              throw new IOException();

      } catch (Exception ioe) {
          // Note that this picks up IOEs and any null pointers as well.
          throw new IllegalArgumentException("The file "+filePath+", given as a sequence alignment file, does not exist or cannot be opened");
      } // End catch block for file test.
  } // End constructor

  /**
   * This constructor does the work of pulling the attributes out of the map.
   */
  public SequenceAlignment(Map rawMap, String genomicAxisID) {

      this((String)rawMap.get("start"), (String)rawMap.get("end"), (String)rawMap.get("entity_start"),
           (String)rawMap.get("entity_end"), (String)rawMap.get("file_path"), (String)rawMap.get("seq_id"),
           genomicAxisID);

  } // End constructor


  //----------------------------------PUBLIC INTERFACE
  /** Allows external set of an efficient means of getting residues over range. */
  public void setSequenceBuilder(SequenceBuilder sequenceBuilder) {
      this.sequenceBuilder = sequenceBuilder;
  } // End method: setSequenceBuilder

  /**
   * Returns the DNA Sequence from the range given, that was provided
   * by the file which aligns this one.
   */
  public Sequence getAlignerSequence(Range rangeOnAxis) {
      if (rangeOnAxis == null)
          return null;

      if (sequenceBuilder != null) {
          // Return sub-range from finder.
          try {
              return sequenceBuilder.getSubSequence(rangeOnAxis.getMinimum(), rangeOnAxis.getMagnitude());
          } // Trying to find residues.
          catch (Exception ex) {
              FacadeManager.handleException(ex);
          } // Catch for find.
      } // Got a finder
      else if (alignerSequence != null) {
          // Return alignments on that range--but make sure they are always forward strand.
          return new SubSequence(alignerSequence, rangeOnAxis.getMinimum(), rangeOnAxis.getMaximum());
      } // Got a sequence.

      return null;

  } // End method: getAlignerSequence

  /**
   * Conversion routine for use in modifying ranges, or "realigning them"
   * as this seq alignment directs them to be.  Takes ranges of specified
   * against OLD axis and adjusts them to the new axis.
   */
  public Range adjustOldAxisRangeToNew(Range inputRange) {
      MutableRange outputRange = new MutableRange(inputRange);

      int sign = (getAxisEnd() - getAxisStart()) / (getEntityEnd() - getEntityStart());
      int distance = Math.min(getAxisEnd(), getAxisStart()) - Math.min(getEntityEnd(), getEntityStart());

      if (sign < 0) {
          // Algorithm:  first rotate about origin, as if the original vector of alignment
          // had been rotated to the opposite quadrant and retains its offset.
          // Next displace so that origin is offset by the length of the vector.
          outputRange.rotateAroundOrigin();
          outputRange.translate(Math.abs(getEntityEnd()-getEntityStart()));
      } // Must rotate.

      outputRange.translate(distance);
      // System.out.println("Just adjusted "+inputRange+" to "+outputRange);

      // NOTE: adjusted range may not be within that to which this sequence alignment
      // "aligns".  In that case, discard it.
      if (outputRange.intersects(new Range(getAxisStart(), getAxisEnd())))
          return outputRange;
      else
          return null;

  } // End method: adjustOldAxisRangeToNew

  /**
   * Conversion routine for use in modifying ranges, or "realigning them"
   * as this seq alignment directs them to be.  Takes ranges against
   * NEW axis and aligns them against the OLD axis.
   */
  public Range adjustNewAxisRangeToOld(Range inputRange) {
      MutableRange outputRange = new MutableRange(inputRange);

      int sign = (getAxisEnd() - getAxisStart()) / (getEntityEnd() - getEntityStart());
      int distance = 0;
      if (sign < 0) {
          // Algorithm:  first rotate about origin, as if the original vector of alignment
          // had been rotated to the opposite quadrant and retains its offset.
          // Next displace forward into the original quadrant.
          outputRange.rotateAroundOrigin();
          MutableRange axisTransRange = new MutableRange(getAxisStart(), getAxisEnd());
          axisTransRange.rotateAroundOrigin();
          MutableRange entityRange = new MutableRange(getEntityStart(), getEntityEnd());

          distance = Math.abs(axisTransRange.getMinimum())
                   + Math.abs(entityRange.getMinimum());
          outputRange.translate(distance);
      } // Must negate.
      else {
          distance = Math.min(getEntityEnd(), getEntityStart()) - Math.min(getAxisEnd(), getAxisStart());
          outputRange.translate(distance);
      } // Must leave direction alone.

      // System.out.println("Just adjusted "+inputRange+" to "+outputRange);
      // System.out.println("...where entity start is "+getEntityStart()+" and entity end is "+getEntityEnd());

      // NOTE: adjusted range may not be within that to which this sequence alignment
      // "aligns".  In that case, discard it.
      if (outputRange.intersects(new Range(getEntityStart(), getEntityEnd())))
          return outputRange;
      else
          return null;

  } // End method: adjustNewAxisRangeToOld

  /**
   * Adjusts the OID of alignment so that: if it matches the target OID,
   * it will be reset to the main (aligning) axis OID.
   */
  public String adjustID(String inputIDOfAlignment) {
      if (inputIDOfAlignment.equals(getSeqID()))
          return getGenomicAxisID();
      else
          return inputIDOfAlignment;
      // System.out.println("Modifying id "+inputIDOfAlignment+" to "+returnVal+": seq id is "+getSeqID()+" genax id is "+getGenomicAxisID());
      // return returnVal;
  } // End method: adjustID

  /**
   * Series of getters for the data.
   */
  public int getEntityStart() { return startOnEntity; }
  public int getEntityEnd() { return endOnEntity; }
  public int getAxisStart() { return startOnAxis; }
  public int getAxisEnd() { return endOnAxis; }
  public String getFilePath() { return filePath; }
  public String getSeqID() { return seqID; }
  public String getGenomicAxisID() { return genomicAxisID; }

  public String toString() {
      return "[Start on entity = "+getEntityStart()+" End on entity = "+getEntityEnd()
             +" start on axis = "+getAxisStart()+" end on axis = " +getAxisEnd()+" file = "+
             getFilePath() + " seq id = "+getSeqID();
  } // End method: toString

  /**
   * Sets a "default" DNA sequence to be used in case the
   * file being aligned fails to provide residues itself.
   * These residues may come from the file which "aligns"
   * this one.  Hence, the name "aligner".
   */
  public void setAlignerSequence(Sequence alignerSequence) {
      // Whose sequence is passed in, an a subsequence is extracted from it.
      this.alignerSequence = new SubSequence(alignerSequence,
                                             Math.min(startOnAxis, endOnAxis),
                                             Math.max(startOnAxis, endOnAxis));
  } // End method: setAlignerSequence

} // End class: SequenceAlignment
