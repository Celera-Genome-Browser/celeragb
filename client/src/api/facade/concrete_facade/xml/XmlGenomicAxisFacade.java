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
 * Description:  Genomic Axis implementation for XML path<p>
 * Company:      []<p>
 * @author Unknown
 * @version
 */
package api.facade.concrete_facade.xml;

import api.entity_model.management.ModelMgr;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.assembly.GenomicAxisLoadFilter;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.EntityTypeSet;
import api.entity_model.model.fundtype.LoadRequest;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.assembly.GenomicAxisLoader;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.facade.concrete_facade.xml.model.CompoundFeatureModel;
import api.facade.concrete_facade.xml.model.FeatureModel;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;
import api.stub.data.PromotionReport;
import api.stub.geometry.Range;
import api.stub.sequence.Sequence;
import api.stub.sequence.ShiftedSequence;
import api.stub.sequence.UnknownSequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Provides information about a genomic axis gathered from an XML file.
 */
public class XmlGenomicAxisFacade extends XmlGenomicFacade implements GenomicAxisLoader {

  //-----------------------------------------------CLASS VARIABLES
  private static FeatureDisplayPriority hiDisplayPriority;
  private static short minimumHighPriority = Short.MAX_VALUE;
  private static short maximumHighPriority = Short.MIN_VALUE;
  static private ResourceBundle annotationResourceBundle;

  //-----------------------------------------------INTERFACE METHODS
  /**
   * Given a set of criteria, return the alignments of features which meet it.
   *
   * @param OID oid the object ID of the axis against which to align features.
   * @param LoadRequest loadRequest the 'criteria' for what to return from here.
   * @return AxisAlignment[] the alignments to the features fitting criteria.
   */
  public Alignment[] loadAlignmentsToEntities(OID entityOID, LoadRequest loadRequest) {

    // Rule out irrelevant requests.
    if (! loadRequest.isRangeRequest())
      return new Alignment[0];

    // Determine the category of entity being requested
    GenomicAxisLoadFilter filter = (GenomicAxisLoadFilter)loadRequest.getLoadFilter();
    if (!filter.isFilteringOnEntityType()) {
      throw new java.lang.UnsupportedOperationException(
        "GenomicAxis does not yet support requests for alignments to all " +
        "entity types simultaneously");
    } // Reject unsupported request.

    List returnList = null;

    // Run through all loaders, looking for interesting data within the range.
    Set entityTypeSet = filter.getEntityTypeSet();
    Set requestedRanges = loadRequest.getRequestedRanges();

    if (entityTypeSet.equals(EntityTypeSet.getEntityTypeSet("ComputedFeatureTypes"))) {
      // Exclude low-priority requests.  By convention, all XML features are high priority.
      if (filter.getFeatureDisplayPriority() != null && filter.getFeatureDisplayPriority().equals(FeatureDisplayPriority.LOW_PRIORITY))
        return new Alignment[0];

      returnList = getFeatures(entityOID, requestedRanges, false);

    } // Precomputes
    else if (entityTypeSet.equals(EntityTypeSet.getEntityTypeSet("CuratedFeatureTypes"))) {

      returnList = getFeatures(entityOID, requestedRanges, true);

    } // Human curations.
    else if (entityTypeSet.contains(EntityType.getEntityTypeForValue(EntityTypeConstants.Contig))) {

      Range rangeOfInterest = null;
      for (Iterator rangeIterator = requestedRanges.iterator(); rangeIterator.hasNext(); ) {
        rangeOfInterest = (Range)rangeIterator.next();

        returnList = getContigs(entityOID, rangeOfInterest);
      } // Contig alignments

    } // contigs
    else {
      System.out.println(this.getClass().getName()+":loadAlignmentsToEntities:has unknown entity type set in request "+entityTypeSet.toString());
    } // Unknown

    Alignment[] returnAlignments = null;
    if((returnList != null) && (returnList.size()!=0)) {
      returnAlignments = new Alignment[returnList.size()];
      returnList.toArray(returnAlignments);
    } // Have alignments to return.
    else {
      returnAlignments = new Alignment[0];
    } // Got no results back.

    return (returnAlignments);

  } // End method: loadAlignmentsToEntities

  public Alignment[] getAlignmentsToAxes(OID entityOID)
  {
    throw new RuntimeException("XmlGenomicAxisFacade::getAlignmentsToAxes not " +
      " implemented");
  }

  /**
   * Returns the sequence of nucleotides covering the range given, and associated
   * with the genomic axis OID given.  ASSUMPTIONS: sequence is not distributed
   * between multiple different files (multiple loaders).  All sequence is in
   * one loader.  Also, we assume here that the sequence is assigned to the
   * genomic axis and not one or more contigs!
   *
   * @param OID genomicOID the sequence returned will be covering this OID.
   * @param Range nucleotideRange the sequence will extend ONLY to include this linear range.
   * @param boolean gappedSequence (ignored) true->use gapped seq./false->use ungapped.
   *    Does not interpret one way or the other.  Implication is that the gapped sequence HAS no gaps.
   */
  public Sequence getNucleotideSeq
    (OID axisOID,
     Range nucleotideRange,
     boolean gappedSequence) {

    // Look for all loaders with sequence in range.
    //  NOTE: it is implied that all loaders with sequence will originate
    //  in a single facade manager.  They should therefore all be in the
    //  list available to this facade, or none of them should be on it.

    final ArrayList allRawSequences = new ArrayList();

    for (Iterator it = getXmlLoadersForGenomicAxis(axisOID); it.hasNext(); ) {
      XmlLoader nextLoader = (XmlLoader)it.next();
      Range intersectingRange = nextLoader.getIntersectingSequenceRange(nucleotideRange);
      if (intersectingRange != null) {
        Sequence rawSequence = nextLoader.getDNASequence(intersectingRange);
        if (rawSequence != null && rawSequence.length() > 0) {
            int rawSeqLocation = nucleotideRange.getMinimum() - intersectingRange.getMinimum();
            if (rawSeqLocation != 0) {
              rawSequence = new ShiftedSequence(rawSeqLocation, rawSequence);
            }

            allRawSequences.add(rawSequence);
        }
      } // Has some overlap.
    } // For all loaders.

    if (allRawSequences.isEmpty())
      return null;
    else if (allRawSequences.size() == 1)
      return (Sequence)allRawSequences.get(0);
    else {
      final int length = nucleotideRange.getMagnitude();
      final int numSeq = allRawSequences.size();
      final Sequence lastSeq = (Sequence)allRawSequences.get(numSeq - 1);
      return new Sequence() {
            public int kind() { return Sequence.KIND_DNA; }
            public long length() { return length; }
            public int get(long location) {
                int base = lastSeq.get(location);
                if (base != UNKNOWN)
                    return base;
                for(int i = numSeq - 2; i >= 0; --i) {
                    Sequence seq = (Sequence)allRawSequences.get(i);
                    base = seq.get(location);
                    if (base != UNKNOWN)
                        return base;
                }
                return UNKNOWN;
            }
        };
    }
  } // End method: getNucleotideSeq

  /**
   * @description Takes the given Genomics Exchange format XML document, checks that the
   * aligned features represented in the document are in a state this is
   * considered promotable and if they are, promotes them into persistent
   * storage.
   * @return true if promotion was successfull, false otherwise
   */
  public PromotionReport promoteAlignedFeatures(OID axisOID, int magnitude, StringBuffer xmlDocument) {
    return new PromotionReport(false, "Cant promote to xml");
  } // End method: promoteAlignedFeatures

  /**
   * @description Takes the given Genomics Exchange format XML document, checks that the
   * aligned features represented in the document are in a state that is
   * considered promotable.
   * @return a String indicating the details of whether the document is ready for
   * promotion
   */
  public PromotionReport checkAlignedFeaturesPromotable(OID axisOID, int magnitude, StringBuffer xmlDocument) {
    return new PromotionReport(false, "Cant promote to xml");
  } // End method: checkAlignedFeaturesPromotable

  /**
   * Returns the properties array associated with the parent/child OID combination.
   * At time-of-writing, the parent OID is ignored, and can even be null here.
   *
   * @param alignmentKey is unused.
   * @param genomicOID the OID of the object for which to get properties.
   * @param deepLoad flag: should expansion be done on server? Ignored here.
   */
  public GenomicProperty[] getProperties(OID genomicOID, EntityType dynamicType, boolean deepLoad) {
    return generatePropertiesForGenomicAxisOID(genomicOID);
  } // End method: getProperties

  //-----------------------------------------------HELPER METHODS
  /**
   * Make a set of unknown bases to return for residues request.
   *
   * @param int startPos start of seq request
   * @param int endPos end of seq request.
   */
  private Sequence createUnknownSequence(int startPos, int endPos) {
    int length = Math.abs(endPos - startPos) + 1;
    return new UnknownSequence(Sequence.KIND_DNA, length);
  } // End method: createUnknownSequence

  /**
   * Given the OID of a genomic axis, supply all known contigs aligning to it,
   * and within the range given.
   *
   * @param OID axisOID oid to which contig must align to be returned here.
   * @param Range rangeOfInterest contig must fall in this range.
   */
  private List getContigs(OID axisOID, Range rangeOfInterest) {
    // Look for the contig feature.
    XmlLoader nextLoader = null;

    int genomeVersionId = axisOID.getGenomeVersionId();
    GenomeVersion version = ModelMgr.getModelMgr().getGenomeVersionById(genomeVersionId);

    List accumulator = new ArrayList();
    GeometricAlignment contigAlignment = null;
    //    for (Iterator it = getXmlLoadersForGenomicAxis(axisOID); it.hasNext(); ) {
    for (Iterator it = getFeatureSourcesInGenomeVersion(axisOID.getGenomeVersionId()); it.hasNext(); ) {
      nextLoader = (XmlLoader)it.next();

      // Get the alignment applicable to this loader's contig, if there is one,
      // and then test its range to see if it falls withing the desired range.
      if (null != (contigAlignment = (GeometricAlignment)nextLoader.getContigAlignment())) {

        if (contigAlignment.getRangeOnAxis().intersects(rangeOfInterest) &&
            (null == version.getLoadedGenomicEntityForOid(contigAlignment.getEntity().getOid())))
          accumulator.add(contigAlignment);

      } // Loader had a contig.

    } // For all loaders.

    return accumulator;
  } // End method

  /**
   * Load features that align to the axis whose entity OID is given, if they
   * fall into one of the ranges given, and are appropriately curated or precomp.
   * Returns a list of features' alignments at all levels: root and below.
   *
   * @param OID entityOID object id of axis.
   * @param Set rangesOfInterest set of ranges into which all feats must align.
   * @param boolean humanCurated flag: curated or precompute?
   * @return List alignments to root features found.
   */
  private List getFeatures(OID entityOID, Set rangesOfInterest, boolean humanCurated) {
    int genomeVersionId = entityOID.getGenomeVersionId();

    List accumulator = new ArrayList();
    List tmpList = null;
    XmlLoader nextLoader = null;
    for (Iterator it = getFeatureSourcesInGenomeVersion(genomeVersionId); it.hasNext(); ) {
      nextLoader = (XmlLoader)it.next();
      if (nextLoader == null) {
        System.out.println("Got null loader");
        continue;
      }

      tmpList = nextLoader.getRootFeatures(entityOID, rangesOfInterest, humanCurated);

      if (tmpList!=null) {
        if (nextLoader.isLoadedFromStringBuffer())
          accumulator.addAll(expandHierarchy(tmpList, rangesOfInterest));
        else
          accumulator.addAll(expandHierarchy(allButPreviouslyAlignedIn(tmpList, genomeVersionId), rangesOfInterest));
      } // Got root features.
    } // For all iterations.

    return accumulator;
  } // End method

  /**
   * Takes the list of inputs as root-level, and creates a new list containing
   * not only THEM, but all their descendants.
   *
   * @param List allFeatures list of feature modesls.
   * @param Set rangesOfInterest set of ranges.  Exclude models NOT in range(s).
   * @return List alignments of features at root level and all sub levels.
   */
  private List expandHierarchy(List allFeatures, Set rangesOfInterest) {
    List returnList = new ArrayList();

    // Traverse the input array, keeping only those features which fall within the specified range.
    FeatureModel rootFeature = null;
    for (Iterator it = allFeatures.iterator(); it.hasNext(); ) {

      rootFeature = (FeatureModel)it.next();
      if (rootFeature != null) {
        recursivelyAdd(returnList, (FeatureModel)rootFeature);
      } // Found it.

    } // For all alignments

    return returnList;
  } // End method

  /**
   * Runs through list of root features.  Cull any that were previously aligned
   * from the output.
   *
   * @param List inputList list of features that may be known to entity model.
   * @param int genomeVersionId id of genome version to check for feature inclusion.
   * @return List list of all features not aligned previously in genome version
   *   whose ID was given.
   */
  private List allButPreviouslyAlignedIn(List inputList, int genomeVersionId) {

    // Pete: Uncomment this to do further testing:
    // if (true == true) return inputList;
    GenomeVersion version = api.entity_model.management.ModelMgr.getModelMgr().getGenomeVersionById(genomeVersionId);

    List returnList = new ArrayList();
    FeatureModel model = null;
    for (Iterator it = inputList.iterator(); it.hasNext(); ) {
      model = (FeatureModel)it.next();
      // Check: does the genome version have an entity for this OID already?
      if (null == version.getLoadedGenomicEntityForOid(model.getOID()))
        returnList.add(model);
    } // Iterate over all alignments in the input list.

    return returnList;
  } // End method

  /**
   * Recursive method to build alignments of all descendents and add them to the output list.
   * Excludes all features which have been obsoleted.
   *
   * @param List outputList where to add the alignments.
   * @param FeatureModel feature a feature to be aligned and added to the output list, and which may have subfeatures to be added as well.
   */
  private void recursivelyAdd(List outputList, FeatureModel feature) {
    // Do not keep obsolete features.
    if (feature.isObsolete())
      return;

    if (feature instanceof CompoundFeatureModel) {
      for (Iterator it = ((CompoundFeatureModel)feature).getChildren().iterator(); it.hasNext(); ) {
        recursivelyAdd(outputList, (FeatureModel)it.next());
      } // For all iterations
    } // Must descend still further.

    // DO this after visiting all children, so that nothing is repeated.
    outputList.add(feature.alignFeature());
  } // End method: recursivelyAdd

} // End class: XmlGenomicAxisFacade
