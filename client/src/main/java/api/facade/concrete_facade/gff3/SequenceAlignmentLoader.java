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
package api.facade.concrete_facade.gff3;

import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.assembly.Contig;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.genetics.Species;
import api.facade.concrete_facade.shared.feature_bean.FeatureBean;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;
import api.stub.geometry.Range;
import api.stub.sequence.DNA;
import api.stub.sequence.Sequence;

import java.util.*;

/**
 * Loads files originating from sequence alignments.  Takes care of
 * realigning the referencing entities, but otherwise carries
 * out all methods for super class.
 */
public class SequenceAlignmentLoader extends GenomicAxisGff3Loader {

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
  public void loadGff() {
      loadGff3(sequenceAlignment.getFilePath());
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
      OID aligningAxisOid = parseContigOID(sequenceAlignment.getGenomicAxisID());
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

    parseContigOID(sequenceAlignment.getGenomicAxisID());

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
    FeatureBean model = super.getFeatureHandler().getOrLoadBeanForOid(featureOid);
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
    FeatureBean model = super.getFeatureHandler().getOrLoadBeanForOid(featureOid);
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
