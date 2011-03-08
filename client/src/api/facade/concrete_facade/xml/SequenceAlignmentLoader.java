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
 * Description:  Data loader to handle new sequence alignments.<p>
 * @author Les Foster
 * @version $Id$
 */
package api.facade.concrete_facade.xml;

import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.assembly.Contig;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.genetics.Species;
import api.facade.concrete_facade.xml.model.FeatureModel;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;
import api.stub.geometry.Range;
import api.stub.sequence.DNA;
import api.stub.sequence.Sequence;

import java.util.*;

/**
 * Loads files originating from sequence alignments.  Takes care of
 * realigning the referrencing entities, but otherwise carries
 * out all methods for super class.
 */
public class SequenceAlignmentLoader extends GenomicAxisXmlLoader {

  private static final List EMPTY_ALIGNMENT_COLLECTION = new ArrayList();

  private SequenceAlignment sequenceAlignment = null;
  private Alignment contigAlignment = null;

  /** Keeps ref to the sequence alignment on which this loader works. */
  public SequenceAlignmentLoader(SequenceAlignment sequenceAlignment, int genomeVersionID) {
      super(sequenceAlignment, genomeVersionID);
      this.sequenceAlignment = sequenceAlignment;
  } // End constructor

  /** Returns all filenames known to have been loaded via this parser. */
  public String[] getLoadedFileNames() {
    String[] returnArray = new String[1];
    returnArray[0] = sequenceAlignment.getFilePath();
    return returnArray;
  } // End method

  /** The file's name is kept in the seq. alignment, so no args are needed. */
  public void loadXml() {
      loadXml(sequenceAlignment.getFilePath());
  } // End method

  /**
   * Checks range against that "claimed" by the sequence alignment, then
   * returns all root features aligned to the axis given as oid, of type requested.
   */
  public List getRootFeatures(OID axisOID, Set rangesOfInterest, boolean humanCurated) {

      return super.getRootFeatures(axisOID, findOverlappingRangeSet(rangesOfInterest), humanCurated);

  } // End method

  /**
   * Returns the alignment of the genomic entity representing the contig of
   * interest to this loaded file.
   */
  public Alignment getContigAlignment() {
      createContigAlignment();
      return contigAlignment;
  } // End method

  /**
   * Override referenced OID set return, to include the OID of the axis
   * being aligned by this.
   */
  public Set getReferencedOIDSet() {
      Set referencedOidSet = super.getReferencedOIDSet();
      OID aligningAxisOid = parseContigOIDTemplateMethod(sequenceAlignment.getGenomicAxisID());
      referencedOidSet.add(aligningAxisOid);
      return referencedOidSet;
  } // End method

  /** Tells what part of seq over the requested range this loader can provide. */
  public Range getIntersectingSequenceRange(Range requestedRange) {
    loadInitialIfNeeded();

    int minOfRange = Math.min(sequenceAlignment.getAxisStart(), sequenceAlignment.getAxisEnd());
    int maxOfRange = Math.max(sequenceAlignment.getAxisStart(), sequenceAlignment.getAxisEnd());
    Range coveredRange = new Range(minOfRange, maxOfRange);
    // System.out.println("Reporting a covered range of "+coveredRange+" from "+this.getClass().getName()+
    // ", and an intersection of "+requestedRange.intersection(requestedRange, coveredRange));
    return requestedRange.intersection(requestedRange, coveredRange);

  } // End method

  /**
   * Returns DNA residues found in the loaded file.  Should be called
   * only after calling getIntersectingSequenceRange to get the relevant range.
   */
  public Sequence getDNASequence(Range sequenceRange) {
    loadInitialIfNeeded();
    // System.out.println("Adjusted range for sequence from "+sequenceRange+" to "+sequenceAlignment.adjustNewAxisRangeToOld(sequenceRange));
    Range adjustedRange = sequenceAlignment.adjustNewAxisRangeToOld(sequenceRange);
    Sequence sequence = super.getDNASequence(adjustedRange);
    if (sequence == null)
      return sequenceAlignment.getAlignerSequence(sequenceRange);
    else {
      if (adjustedRange.isReversed()) {
        sequence = DNA.reverseComplement(sequence);
      } // Must invert residues order.
      return sequence;
    } // Using native sequence.
  } // End method

  /** Override to avoid problems to do with not representing an axis here. */
  public GenomicAxis getGenomicAxis() { return null; }

  public Species getSpecies() { return null; }

  /**
   * Given a user request set of ranges, find that subset which overlaps the
   * range set indicated by this sequence alignment.
   */
  private Set findOverlappingRangeSet(Set rangesOfInterest) {
    Range nextRange = null;
    Set overlappingRangeSet = new HashSet();

    for (Iterator it = rangesOfInterest.iterator(); it.hasNext(); ) {
      nextRange = (Range)it.next();
      if (nextRange.intersects(new Range(sequenceAlignment.getAxisStart(), sequenceAlignment.getAxisEnd()))) {
        overlappingRangeSet.add(nextRange);
      } // Found an applicable range.
    } // For all ranges

    return overlappingRangeSet;
  } // End method

  /** Make a contig out of the sequence alignment's values. */
  private void createContigAlignment() {
    // Order-independent calling.  If already made, do not make it twice.
    if (contigAlignment != null)
      return;

    OID contigOID = OIDGenerator.getOIDGenerator().generateOIDInNameSpace(  OID.API_GENERATED_NAMESPACE,
                                                                              getGenomeVersionId());
    Contig contigEntity = new Contig(
      contigOID,
      "Contig ["+contigOID.toString()+"]"+sequenceAlignment.getFilePath(),
      Math.abs(sequenceAlignment.getAxisEnd() - sequenceAlignment.getAxisStart())
    );

    parseContigOIDTemplateMethod(sequenceAlignment.getGenomicAxisID());

    // Getting the axis alignment
    contigAlignment = new GeometricAlignment(
      null,
      contigEntity,
      new Range(sequenceAlignment.getAxisStart(), sequenceAlignment.getAxisEnd())
    );

  } // End method

  /** Returns OID of contig represented in this file. */
  public OID getContigOID() {
    createContigAlignment();
    return contigAlignment.getEntity().getOid();
  } // End method

  /** Returns alignment for the feature whose OID was given. */
  public Alignment getAlignmentForFeature(OID featureOid) {
    super.loadFeaturesIfNeeded();
    FeatureModel model = super.getFeatureHandler().getOrLoadModelForOid(featureOid);
    if (model == null)
      return null;
    Range featureRange = model.calculateFeatureRange();
    Range sequenceAlignmentRange = new Range(sequenceAlignment.getAxisStart(), sequenceAlignment.getAxisEnd());
    if (sequenceAlignmentRange.contains(featureRange))
      return (Alignment)super.getAlignmentForFeature(featureOid);
    else
      return null;

  } // End method

  /** Returns flag of whether the feature whose OID was given is known to this loader. */
  public boolean featureExists(OID featureOid) {
    super.loadFeaturesIfNeeded();
    FeatureModel model = super.getFeatureHandler().getOrLoadModelForOid(featureOid);
    Range featureRange = super.getRangeOnAxisOfFeature(featureOid);
    Range sequenceAlignmentRange = new Range(sequenceAlignment.getAxisStart(), sequenceAlignment.getAxisEnd());
    return (model != null) && (sequenceAlignmentRange.contains(featureRange));
  } // End method

  /**
   * NEVER return an axis for a sequence alignment.  Returning null here
   * deliberately masks the genomic axis from the aligned file(s).
   */
  public Alignment getGenomicAxisAlignment() { return null; }

} // End class: SequenceAlignmentLoader
/*
 $Log$
 Revision 1.1  2006/11/09 21:35:56  rjturner
 Initial upload of source

 Revision 1.29  2002/11/07 18:38:53  lblick
 Removed obsolete imports and unused local variables.

 Revision 1.28  2002/11/07 16:06:20  lblick
 Removed obsolete imports and unused local variables.

 Revision 1.27  2002/04/04 16:18:39  tsaf
 Moving Sequence classes to api.stub.sequence

 Revision 1.26  2002/02/08 19:29:53  lfoster
 Placed absolute call around the magnitude given to contigs.

 Revision 1.25  2002/01/21 19:11:53  jojicon
 Redesign of DNASequence and DNASequenceCache.

 Revision 1.24  2001/10/22 05:32:08  lfoster
 Fixed missing referenced OID for just the axis and fixed reversal of adjusted and non-adjusted range for returning sequence found in aligned file.

 Revision 1.23  2001/08/24 17:20:32  tsaf
 Changes from getComplement to getReverseComplement.
 This affected an XML file and the calculation of splices, starts,
 and stops on the reverse scaffold.

 Revision 1.22  2001/07/16 16:55:57  lfoster
 Now multiple sequence aliging files can be loaded.  Seq fetch for reverse-aligned sequence alignments is much improved.

 Revision 1.21  2001/07/12 21:18:47  lfoster
 Search fixed for Sequence Alignment files.  Full-batery tested for search, bookmark and load.

 Revision 1.20  2001/06/09 20:20:17  lfoster
 Once more.  Comitting the changes with the model dumping.

 Revision 1.16  2001/06/08 16:28:57  lfoster
 Eliminated getHumanCuratedRootFeatures and getPrecomputedRootFeatures in favor of a single method.

 Revision 1.15  2001/06/04 20:58:10  lfoster
 Fixed redundant contig OID problem arrising when same .gba file used in multiple sequence alignments.

 Revision 1.14  2001/05/22 18:54:58  lfoster
 Moved some genomic axis facade code down into the loader.

 Revision 1.13  2001/05/17 21:40:34  lfoster
 Fixed problem whereby Sequence ALignment facade was returning multiple alignments to single features.

 Revision 1.12  2001/03/13 17:29:30  jbaxenda
 Removing bizobj classes and any references to them from server and client code.

 Revision 1.11  2001/03/05 19:01:45  lfoster
 Got seq_alignments re-working.

 Revision 1.10  2001/02/12 23:14:13  lfoster
 Latest for trunk.  Drills in, but alignment fails due to mutator access refusal.

 Revision 1.9  2001/02/06 23:01:04  tsaf
 Here we go...

 Revision 1.8.2.1  2001/01/26 06:22:25  jbaxenda
 Attempting to make the start of changes over to the new
 entity_model based abstract facades. Compiling but by no means
 ready to run.

 Revision 1.8  2001/01/12 21:36:25  lfoster
 Optimizing for case where selected range for feature load does not overlap that of a "registered" sequence alignment loader.

 Revision 1.7  2001/01/02 23:17:18  lfoster
 Added new class, which was taken FROM an inner class.  Moved locations of some methods.

 Revision 1.6  2000/12/20 21:42:45  lfoster
 Changed the sequence alignment handling so that reversed entity start/end is precluded in construction of alignment.  Reverse complementing sequence if axis order is reversed.

 Revision 1.5  2000/12/19 22:07:39  lfoster
 Changed "adjust" method of SequenceAlignment to more descriptive name, and added a second adjusting method for converting ranges from main axis range to aligned sub-axis range.

 Revision 1.4  2000/12/04 17:51:56  lfoster
 Supporting residues pass-in to aligned sequences.  Tested against "testGene.gba",
 not for the actual use case (it doesn't break anything).

 Revision 1.3  2000/11/08 19:21:20  lfoster
 Now discards models not-in-range.  Also, tested-to-work with three-part alignment, with features.  Residues untested.
 Does alignment properly (barring further test results) if range of seq alignment is translated and rotated.

 */
