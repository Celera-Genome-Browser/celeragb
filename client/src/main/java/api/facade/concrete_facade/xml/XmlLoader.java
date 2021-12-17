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
package api.facade.concrete_facade.xml;

import api.entity_model.access.report.SubjectSequenceReport;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.genetics.Species;
import api.stub.data.GenomicEntityComment;
import api.stub.data.OID;
import api.stub.geometry.Range;
import api.stub.sequence.Sequence;

import java.util.List;
import java.util.Set;

/**
 * Implement this to provide a means of retrieving data required by various API XML facades
 * in referencing XML data.
 */
public interface XmlLoader {

  /**
   * Returns the gene annotation name for the OID given.
   */
  public abstract String getGeneacc(OID oid);

  /**
   * Retrieves the query residues for the OID in question.
   */
  public abstract String getQueryAlignedResidues(OID oid);

  /**
   * Retrieves the query residues for the OID in question.
   */
  public abstract String getSubjectAlignedResidues(OID oid);

  /**
   * Retrieves the subject sequence OID collection for the
   * feature whose OID was given.
   */
  public abstract Set getSubjectSequenceOids(OID oid);

  /**
   * Returns subject sequence of the subject sequence OID given.  Repeat:
   * this is for the subject sequence, not for a feature.
   * @See getSubjectSequenceOids(OID oid);
   */
  public abstract Sequence getSubjectSequence(OID subjectSequenceOid);

  /**
   * Retrieves a report object against a subject oid set
   * possibly created from a call like that above.
   */
  public void addToSubjSeqRpt(String subjectSeqId, OID genomeVersionOid, SubjectSequenceReport report);

  /**
   * Returns the Transcript annotation name for the OID given.
   */
  public abstract String getTrscptacc(OID oid);

  /**
   * Retrieves the feature description for the object id given.
   */
  public abstract String getFeatureDescription(OID oid);

  /**
   * Retrieves the feature description for parent of the OID given.
   */
  public abstract String getFeatureDescriptionForParent(OID oid);

  /**
   * Retrieves the score value for the object id given.
   */
  public abstract String getScore(OID oid);

  /**
   * Returns the expect value for the object id given.
   */
  public abstract String getIndividualExpect(OID oid);

  /**
   * Returns the expect value for the object id given.
   */
  public abstract String getSummaryExpect(OID oid);

  /**
   * Returns the output value associated with the name given for obj id given.
   */
  public abstract String getOutput(OID oid, String outputName);

  /**
   * Returns the start position of the sequence relationship whose
   * subject ID is given.
   */
  public abstract int getSubjectStart(OID oid);

  /**
   * Returns the end position of the sequence relationship whose
   * subject ID is given.
   */
  public abstract int getSubjectEnd(OID oid);

  /**
   * Gets the gene name for the OID given.
   */
  public abstract String getGene(OID oid);

  /**
   *  load GAME XML from named file.
   */
  public abstract void loadXml(String fileName);

  /**
   * Return the names of the loaded files
   */
  public abstract String[] getLoadedFileNames() ;

  /**
   * Returns whatever range (if any) of nucleotides intersects the range
   * requested.
   */
  public abstract Range getIntersectingSequenceRange(Range requestedRange);

  /**
   * Returns the sequence over the range given.  This is expected to be done
   * only <emph>after</emph> a call to getIntersectingSequenceRange.
   */
  public abstract Sequence getDNASequence(Range intersectingRange);

  /**
   * Returns the OID of the entity represented by this loader (or to which its
   * features, etc. apply).  This includes genomic axes.
   */
  public abstract Set getReferencedOIDSet();

  /**
   * Returns root features of specified type, in specified range.
   *
   * @param OID axisOID identifier of axis to which returned curations will align.
   * @param Set rangesOfInterest the ranges in which features returned will lie.
   * @param boolean humanCurated if true, gets human curations; false, precomputes
   */
  public abstract List getRootFeatures(OID axisOID, Set rangesOfInterest, boolean humanCurated);

  /**
   * Returns obsolete features referencing axis that exist in entire file.
   * By definition, obsolete features are human curated.
   *
   * @param OID axisOID axis to which feature reference.
   * @return List list of entities that are obsolete and ref the axis.
   */
  public abstract List getObsoleteRootFeatures(OID axisOID);

  /**
   * Returns obsolete features UNDER the feature whose OID is given, and
   * referencing the axis given.
   *
   * @param OID axisOID axis to which feature reference.
   * @param OID parentOID feature containing features of interest.
   * @return List list of entities that are obsolete and ref the axis.
   */
  public abstract List getObsoleteNonRootFeatures(OID axisOID, OID parentOID);

  /**
   * Returns axis alignment for a given feature's OID.
   */
  public abstract Alignment getAlignmentForFeature(OID featureOid);

  /**
   * Returns analysis type for feature of oid given.
   */
  public abstract String getAnalysisTypeOfFeature(OID featureOid);

  /**
   * Returns discovery environment for feature of oid given.
   */
  public abstract String getDiscoveryEnvironmentOfFeature(OID featureOid);

  /**
   * Returns range on axis of feature given.
   */
  public abstract Range getRangeOnAxisOfFeature(OID featureOid);

  /**
   * Returns axis alignment, given an accession string and a type of accession.
   * Types should be those defined in this interface.
   */
  public abstract Alignment getAlignmentForAccession(String accession, int accessionType);

  /**
   * Returns list of alignments to features.  Said features will have description
   * text containing the search target given.
   */
  public abstract List getAlignmentsForDescriptionsWith(String searchTarget);

  /**
   * Tells whether a certain feature, designated by its OID, exists in this loader.
   */
  public abstract boolean featureExists(OID oid);

  /**
   * Removes a given feature's data from the feature cache.  Allows it to
   * be reloaded.
   */
  public abstract void removeFeature(OID oid);

  /**
   * Provides the full set of comments which have been made against the feature
   * whose OID is given.
   */
  public abstract GenomicEntityComment[] getComments(OID oid);

  /**
   * Returns the list of property sources associated in the loader with the OID given.
   */
  public abstract List getPropertySources(OID oid);

  /**
   * Returns species represented in the loaded file.
   */
  public abstract Species getSpecies();

  /**
   * Returns boolean of whether the feature of the OID which is given, was
   * curated or not.  If not, assumed to be precompute, or non-curated.
   */
  public abstract boolean isCurated(OID oid);

  /**
   * Getter for whatever data is replaced by a given object identified
   * by the OID given.  List should contain ReplacedData elements.
   *
   * @param OID the oid to lookup.
   */
  public abstract List getReplacedData(OID replacingOID);

  /**
   * Returns evidence OIDs for a given feature's OID.
   */
  public abstract OID[] getEvidence(OID featureOID);

  /**
   * Returns the alignment of contig entity contained by the loader.
   */
  public abstract Alignment getContigAlignment();

  /**
   * Returns the genomic axis described by this file, if that file _was_ an
   * axis file.
   */
  public abstract Alignment getGenomicAxisAlignment();

  /**
   * Returns the OID of the genomic axis represented _by_ a loader.
   */
  public abstract OID getGenomicAxisOID();

  /**
   * Returns the length of a sequence given.
   */
  public abstract String getSequenceLength(OID sequenceOID);

  /**
   * Returns relative order of feature among its siblings, IF it has a parent
   * feature.
   */
  public abstract int getSiblingPosition(OID featureOID);

  /** Returns OID of the genomic axis against which a feature aligns. */
  public abstract OID getAxisOidOfAlignment(OID featureOid);

  /** Returns true if str-buf was original source. */
  public abstract boolean isLoadedFromStringBuffer();

  /** Add/remove listeners for the appearance of sequence alignments in the file. */
  public abstract void addSequenceAlignmentListener(SequenceAlignmentListener listener);

  public abstract void removeSequenceAlignmentListener(SequenceAlignmentListener listener);

} // End interfacd: XmlLoader
