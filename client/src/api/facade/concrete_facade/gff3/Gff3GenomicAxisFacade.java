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
package api.facade.concrete_facade.gff3;

import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.assembly.Contig;
import api.entity_model.model.assembly.GenomicAxisLoadFilter;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.EntityTypeSet;
import api.entity_model.model.fundtype.LoadRequest;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.assembly.GenomicAxisLoader;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.facade.concrete_facade.gff3.feature_facade.Gff3GenomicFacade;
import api.facade.concrete_facade.shared.feature_bean.CompoundFeatureBean;
import api.facade.concrete_facade.shared.feature_bean.FeatureBean;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;
import api.stub.data.PromotionReport;
import api.stub.geometry.Range;
import api.stub.sequence.Sequence;
import api.stub.sequence.ShiftedSequence;

import java.util.*;

/**
 * Provides information about a genomic axis gathered from an XML file.
 */
public class Gff3GenomicAxisFacade extends Gff3GenomicFacade implements GenomicAxisLoader {

	//-----------------------------------------------INTERFACE METHODS
	/**
	 * Given a set of criteria, return the alignments of features which meet it.
	 *
	 * @param OID oid the object ID of the axis against which to align features.
	 * @param LoadRequest loadRequest the 'criteria' for what to return from here.
	 * @return AxisAlignment[] the alignments to the features fitting criteria.
	 */
	@Override
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

		List<Alignment> returnList = null;

		// Run through all loaders, looking for interesting data within the range.
		Set<EntityType> entityTypeSet = filter.getEntityTypeSet();
		Set<Range> requestedRanges = loadRequest.getRequestedRanges();

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

			returnList = getContigs(entityOID, requestedRanges);

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

	@Override
	public Alignment[] getAlignmentsToAxes(OID entityOID)
	{
		throw new RuntimeException("Gff3GenomicAxisFacade::getAlignmentsToAxes not " +
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
	@Override
	public Sequence getNucleotideSeq
	(OID axisOID,
			Range nucleotideRange,
			boolean gappedSequence) {

		// Look for all loaders with sequence in range.
		//  NOTE: it is implied that all loaders with sequence will originate
		//  in a single facade manager.  They should therefore all be in the
		//  list available to this facade, or none of them should be on it.

		final ArrayList<Sequence> allRawSequences = new ArrayList<Sequence>();

		for (DataLoader nextLoader: getGff3LoadersForGenomicAxis(axisOID) ) {
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
	 * @description Takes the given document, checks that the
	 * aligned features represented in the document are in a state this is
	 * considered promotable and if they are, promotes them into persistent
	 * storage.  Here, never can.
	 * @return true if promotion was successful, false otherwise
	 */
	public PromotionReport promoteAlignedFeatures(OID axisOID, int magnitude, StringBuffer xmlDocument) {
		return new PromotionReport(false, "Cant promote to GFF3");
	} // End method: promoteAlignedFeatures

	/**
	 * @description Takes the given Genomics Exchange format XML document, checks that the
	 * aligned features represented in the document are in a state that is
	 * considered promotable.
	 * @return a String indicating the details of whether the document is ready for
	 * promotion
	 */
	public PromotionReport checkAlignedFeaturesPromotable(OID axisOID, int magnitude, StringBuffer xmlDocument) {
		return new PromotionReport(false, "Cant promote to GFF3");
	} // End method: checkAlignedFeaturesPromotable

	/**
	 * Returns the properties array associated with the parent/child OID combination.
	 * At time-of-writing, the parent OID is ignored, and can even be null here.
	 *
	 * @param alignmentKey is unused.
	 * @param genomicOID the OID of the object for which to get properties.
	 * @param deepLoad flag: should expansion be done on server? Ignored here.
	 */
	@Override
	public GenomicProperty[] getProperties(OID genomicOID, EntityType dynamicType, boolean deepLoad) {
		return generatePropertiesForGenomicAxisOID(genomicOID);
	} // End method: getProperties

	//-----------------------------------------------HELPER METHODS
	/**
	 * Given the OID of a genomic axis, supply all known contigs aligning to it,
	 * and within the range given.
	 *
	 * @param OID axisOID oid to which contig must align to be returned here.
	 * @param Range rangeOfInterest contig must fall in this range.
	 */
	private List<Alignment> getContigs(OID axisOID, Set<Range> rangesOfInterest) {
		// Look for the contig feature.

		List<Alignment> accumulator = new ArrayList<Alignment>();
		//    for (Iterator it = getXmlLoadersForGenomicAxis(axisOID); it.hasNext(); ) {
		for (DataLoader nextLoader: getFeatureSourcesInGenomeVersion(axisOID.getGenomeVersionId()) ) {

			if (nextLoader == null) {
				System.out.println("Got null loader");
				continue;
			}

			List<FeatureBean> tmpList = nextLoader.getContigFeatures(axisOID, rangesOfInterest);
			if (tmpList!=null) {
				for ( FeatureBean bean: tmpList ) {
					accumulator.add( this.createContigAlignment(bean, axisOID));
				}
			}
			
		} // For all loaders.

		return accumulator;
	} // End method

	/**
	 * Create a contig out of a known contig bean.
	 */
	private Alignment createContigAlignment(FeatureBean bean, OID axisOID) {
		Contig contigEntity = new Contig(bean.getOID(), bean.getDescription(), bean.getEnd() - bean.getStart());
		return new GeometricAlignment(null, contigEntity, new Range(bean.getStart(), bean.getEnd()));
	} // End method: createContigEntity

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
	private List<Alignment> getFeatures(OID entityOID, Set<Range> rangesOfInterest, boolean humanCurated) {
		int genomeVersionId = entityOID.getGenomeVersionId();

		List<Alignment> accumulator = new ArrayList<Alignment>();
		List<FeatureBean> tmpList = null;
		for (DataLoader nextLoader: getFeatureSourcesInGenomeVersion(genomeVersionId) ) {
			if (nextLoader == null) {
				System.out.println("Got null loader");
				continue;
			}

			tmpList = nextLoader.getRootFeatures(entityOID, rangesOfInterest, humanCurated);

			if (tmpList!=null) {
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
	private List<Alignment> expandHierarchy(List<FeatureBean> allFeatures, Set<Range> rangesOfInterest) {
		List<Alignment> returnList = new ArrayList<Alignment>();

		// Traverse the input array, keeping only those features which fall within the specified range.
		FeatureBean rootFeature = null;
		for (Iterator<FeatureBean> it = allFeatures.iterator(); it.hasNext(); ) {

			rootFeature = it.next();
			if (rootFeature != null) {
				recursivelyAlignAndAdd(returnList, (FeatureBean)rootFeature);
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
	private List<FeatureBean> allButPreviouslyAlignedIn(List<FeatureBean> inputList, int genomeVersionId) {

		// Pete: Uncomment this to do further testing:
		// if (true == true) return inputList;
		GenomeVersion version = api.entity_model.management.ModelMgr.getModelMgr().getGenomeVersionById(genomeVersionId);

		List<FeatureBean> returnList = new ArrayList<FeatureBean>();
		FeatureBean model = null;
		for (Iterator<FeatureBean> it = inputList.iterator(); it.hasNext(); ) {
			model = it.next();
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
	 * @param FeatureBean feature a feature to be aligned and added to the output list, and which may have subfeatures to be added as well.
	 */
	private void recursivelyAlignAndAdd(List<Alignment> outputList, FeatureBean feature) {
		// Do not keep obsolete features.
		if (feature.isObsolete())
			return;

		if (feature instanceof CompoundFeatureBean) {
			for (Iterator<FeatureBean> it = ((CompoundFeatureBean)feature).getChildren().iterator(); it.hasNext(); ) {
				recursivelyAlignAndAdd(outputList, it.next());
			} // For all iterations
		} // Must descend still further.

		// DO this after visiting all children, so that nothing is repeated.
		outputList.add(feature.alignFeature());
	} // End method: recursivelyAdd

} // End class: XmlGenomicAxisFacade
