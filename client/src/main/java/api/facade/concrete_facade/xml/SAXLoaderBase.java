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
 * Title:        Genome Browser primary SAX handler<p>
 * Description:  Primary handler, which also acts as a base for file-type-specific
 *               loaders.<p>
 * Company:      []<p>
 * @author Les Foster
 * @version  CVS_ID:  $Id$
 */
package api.facade.concrete_facade.xml;

import api.entity_model.access.report.SubjectSequenceReport;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.assembly.Contig;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.genetics.Species;
import api.facade.concrete_facade.shared.LoaderConstants;
import api.facade.concrete_facade.shared.OIDParser;
import api.facade.concrete_facade.shared.feature_bean.CompoundFeatureBean;
import api.facade.concrete_facade.shared.feature_bean.FeatureBean;
import api.facade.concrete_facade.shared.feature_bean.GeneFeatureBean;
import api.facade.concrete_facade.shared.feature_bean.SimpleFeatureBean;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.GenomicEntityComment;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;
import api.stub.data.ReservedNameSpaceMapping;
import api.stub.geometry.Range;
import api.stub.sequence.Sequence;
import api.stub.sequence.SequenceBuilder;
import api.stub.sequence.SubSequence;
import shared.util.GANumericConverter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * This loader deals with opening files, and handling SAX events, which
 * it may delegate to other classes.  It also provides facilities to
 * its delegates as template methods to its subclasses.
 *
 * Note that subclasses must implement the OIDParser interface.
 */
public abstract class SAXLoaderBase implements XmlLoader, OIDParser {

  //------------------------------CONSTANTS
  private static final String DEFAULT_SPECIES = "Unknown Species";
  private static final String DEFAULT_GENOMIC_AXIS_DISPLAY_NAME = "Genomic Axis";

  //------------------------------MEMBER VARIABLES
  private DrivingHandler mFeatureHandler = null;
  private SequenceLoader mSequenceLoader = null;
  private List mLoadedFileNames = new ArrayList();

  private OID genomicAxisOid;

  private int axisLength = 0;

  // Species may be found in one of the tags.
  private Species species = null;
  private String taxon = null;
  private String assemblyVersion = null;  // For promotion code!

  private Alignment contigAlignment = null;
  private Alignment genomicAxisAlignment = null;

  // All residue base call letters kept here.
  private Sequence residues = null;
  private SequenceBuilder sequenceBuilder = null;

  // Flag to indicate whether the file was scanned for overview data.
  private boolean mInitialScanComplete = false;

  // Flag to indicate whether the fiel was scanned for obsolete features.
  private boolean mPreviouslyCachedObsoleteFeatures = false;

  private XmlFacadeManager mReadFacadeManager = null;
  private SequenceAlignment mSequenceAlignment = null;

  // Listeners list.  Listening for sequence alignment "events".
  private List listeners = new ArrayList();

  // This version ID must be set for all the OIDs produced by this loader.
  private int mGenomeVersionId = 0;

  // Keeps track of whether str-buf: special case.
  private boolean mStringBufferOrigin = false;

  //------------------------------CONSTRUCTORS
  /**
   * Simplest constructor.
   */
  public SAXLoaderBase() {
  } // End constructor

  /**
   * Use the passed OID for its genome version ID.
   */
  public SAXLoaderBase(OID lSpeciesOID) {
    mGenomeVersionId = lSpeciesOID.getGenomeVersionId();
  } // End constructor

  /**
   * Set the genome version id directly.
   */
  public SAXLoaderBase(int lGenomeVersionId) {
    mGenomeVersionId = lGenomeVersionId;
  } // End constructor

  /**
   * Pass in a sequence alignment to adjust alignments of features, etc.
   */
  public SAXLoaderBase(SequenceAlignment lSequenceAlignment, int lGenomeVersionId) {
    if (lSequenceAlignment == null)
      throw new IllegalArgumentException("Null sequence alignment object given");
    mSequenceAlignment = lSequenceAlignment;
    mGenomeVersionId = lGenomeVersionId;
  } // End constructor

  //------------------------------IMPLEMENTATION OF XmlLoader
  /**
   * Adds the file whose name was given for later parsing.
   */
  public void loadXml(String lFileName) {
    residues = null;

    // Register the file for later use.
    mLoadedFileNames.add(lFileName);

    String stringBufferDebugging = null;
    try {
      stringBufferDebugging = System.getProperty("x.genomebrowser.loadXmlAsStringBuffer");
    }
    catch (Exception ex) {} //shallow missing resource exception as it is expected

    // Test: wish to debug the file
    if ((stringBufferDebugging != null) && (stringBufferDebugging.equalsIgnoreCase("true"))) {
      try {
        StringBuffer lContents = new StringBuffer();
        BufferedReader lBR = new BufferedReader(new FileReader(lFileName));
        String lNextLine = null;
        while (null != (lNextLine = lBR.readLine())) {
          lContents.append(lNextLine);
        } // For all lines of input.
        lBR.close();
        loadXml(lContents);
      } // End try block
      catch (IOException ioe) {
        FacadeManager.handleException(ioe);
      } // End catch block for read.
    } // Go ahead and load.

    // System.out.println(lFileName);

    return;
  } // End method: loadXml

  /**
   * Invokes the complete parse of the file whose name was given, and population
   * of model.
   */
  public void loadXml(StringBuffer lContents) {
    mStringBufferOrigin = true;
    residues = null;

    mLoadedFileNames.add("String Buffer");
    mInitialScanComplete = true;

    InitialLoader initialLoader = new InitialLoader(lContents);
    assemblyVersion = initialLoader.getAssembly(); // For promotion code!

    // Initial load may not pickup anything of value.  In that case, this
    // file is probably NOT an assembly file, and there is nothing more to collect.
    if (initialLoader.getGenomicAxisID() != null) {

      String taxon = initialLoader.getSpecies();

      // Pull species information.
      if (taxon == null || taxon.length() == 0)
        taxon = DEFAULT_SPECIES;

      //species = new Species();
      species = new Species(  OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.API_GENERATED_NAMESPACE,
                                                                                    getGenomeVersionId()),
                              taxon);

      axisLength = initialLoader.getAxisLength();
      genomicAxisOid = this.parseContigOID(initialLoader.getGenomicAxisID());
      sequenceBuilder = initialLoader.getSequenceBuilder();
      if (sequenceBuilder == null)
        residues = initialLoader.getSequence();

      // Sequence alignments provide contigs when they appear.  When they do
      // not, one contig is assumed with length same as the axis and forward.
      if (initialLoader.getSequenceAlignments().size() == 0)
        createContigAlignment(axisLength);
    } // Must build things based on residues length and species information.

    // Now load the features.
    mFeatureHandler = new DrivingHandler(lContents, mReadFacadeManager, (OIDParser)this);

    return;
  } // End method: loadXml

  /** Returns true if original source of data was a stringbuffer. */
  public boolean isLoadedFromStringBuffer() {
    return mStringBufferOrigin;
  } // End method

  /** Returns all filenames known to have been loaded via this parser. */
  public String[] getLoadedFileNames() {
    String[] returnArray = new String[mLoadedFileNames.size()];
    mLoadedFileNames.toArray(returnArray);
    return returnArray;
  } // End method: getLoadedFileNames

  /** Tells what part of seq over the requested range this loader can provide. */
  public Range getIntersectingSequenceRange(Range requestedRange) {
    loadInitialIfNeeded();
    if (genomicAxisOid == null) {
      return null;
    } // Nothing to report.
    else {
      // Expect THIS kind of loader to always have whole range, if ANY range.
      Range coveredRange = new Range(0, axisLength);
      // System.out.println("Reporting a covered range of "+coveredRange+" from "+this.getClass().getName()+
      // ", and an intersection of "+requestedRange.intersection(requestedRange, coveredRange));
      return requestedRange.intersection(requestedRange, coveredRange);
    } // Return covered area.
  } // End method: getIntersectingSequenceRange

  /**
   * Returns DNA residues found in the loaded file.  Should be called
   * only after calling getIntersectingSequenceRange to get the relevant range.
   */
  public Sequence getDNASequence(Range sequenceRange) {
    loadInitialIfNeeded();
    if (sequenceRange == null) {
      System.out.println("Got null seq range");
      return null;
    } // abberant range given.

    if (sequenceBuilder != null) {
      try {
        return sequenceBuilder.getSubSequence(sequenceRange.getMinimum(), sequenceRange.getMagnitude());
      } // Trying to find residues.
      catch (Exception ex) {
        FacadeManager.handleException(ex);
      } // Catch for find.

      return null;
    } // Got a finder.
    else if (residues != null) {
      return new SubSequence(residues, sequenceRange.getMinimum(), sequenceRange.getMagnitude());
    } // Got seq.
    else {
      return null;
    } // Neither.

  } // End method: getDNASequence

  /** Returns the OID of any genomic axis represented by this file. */
  public OID getGenomicAxisOID() {
    loadInitialIfNeeded();
    return genomicAxisOid;
  } // End method

  /**
   * Returns the length of a sequence given.
   */
  public String getSequenceLength(OID sequenceOID) {
    loadFeaturesIfNeeded();
    return mFeatureHandler.getSubjectSequenceLength(sequenceOID);
  } // End method

  /**
   * Returns the alignment to a genomic axis.
   * @param OID chromosomeOID used to obtain the genome version id.
   * @return Alignment the alignment to the genomic axis, (and implicitly, an axis is created).
   */
  public Alignment getGenomicAxisAlignment() {
    loadInitialIfNeeded();
    if (genomicAxisAlignment == null) {
      if (genomicAxisOid != null) {
        String genAxDisplayName = genomicAxisOid.isInternalDatabaseOID() ?
                 GANumericConverter.getConverter().getGANameForOIDSuffix(genomicAxisOid.getIdentifierAsString())
               : DEFAULT_GENOMIC_AXIS_DISPLAY_NAME;
        GenomicAxis genomicAxis = new GenomicAxis(genomicAxisOid,genAxDisplayName,axisLength);
        genomicAxisAlignment = new Alignment(null, genomicAxis);
      } // Non-null axis OID: can build the alignment.b
    } // Must build new one.

    return genomicAxisAlignment;

  } // End method: getGenomicAxis

  /**
   * Returns the alignment of the genomic entity representing the contig of
   * interest to this loaded file.
   */
  public Alignment getContigAlignment() {
    loadInitialIfNeeded();
    return contigAlignment;
  } // End method: getContigAlignment

  /** Returns species representing file. */
  public Species getSpecies() {
    loadInitialIfNeeded();
    return species;
  } // End method: getSpecies

  /** Returns accession string for the gene whose OID is given. */
  public String getGeneacc(OID oid) {
    loadFeaturesIfNeeded();
    GeneFeatureBean model = (GeneFeatureBean)mFeatureHandler.getOrLoadModelForOid(oid);
    if (model != null)
      return model.getAnnotationName();
    return null;
  } // End method: getGeneacc

  /** Returns the transcript accession name. */
  public String getTrscptacc(OID oid) {
    loadFeaturesIfNeeded();
    CompoundFeatureBean model = (CompoundFeatureBean)mFeatureHandler.getOrLoadModelForOid(oid);
    if (model != null)
      return model.getAnnotationName();
    return null;
  } // End metod: getTrscptacc

  /**
   * Returns relative order of feature among its siblings, IF it has a parent
   * feature.
   */
  public int getSiblingPosition(OID oid) {
    loadFeaturesIfNeeded();
    FeatureBean model = mFeatureHandler.getOrLoadModelForOid(oid);
    FeatureBean nextChild = null;
    CompoundFeatureBean parentModel = (CompoundFeatureBean)model.getParent();

    // Decode the annotation type string into a feature type instance.
    EntityType entityType = model.decodeEntityType(model.getAnalysisType());
    EntityType childEntityType = null;

    int returnValue = -1;
    int count = -1;
    if (parentModel != null) {
      // Copy the list for sorting, but leave original undisturbed.
      List children = new ArrayList(parentModel.getChildren());
      Collections.sort(children);
      if (children != null) {
        for (int i = 0; (returnValue == -1) && (i < children.size()); i++) {
          nextChild = (FeatureBean)children.get(i);
          if (nextChild == null)
            continue;

          // NOTE: only count children of same type as siblings.
          childEntityType = nextChild.decodeEntityType(nextChild.getAnalysisType());
          if (childEntityType == entityType)
            count++;

          if (nextChild.equals(model)) {
            returnValue = count;
          } // Got model.
        } // For all children.
      } // Has children?  Must if it is a parent.
    } // Has parent

    return returnValue;

  } // End method

  /** Returns output associated with string, for feature whose OID was given. */
  public String getOutput(OID oid, String outputName) {
    loadFeaturesIfNeeded();
    SimpleFeatureBean model = (SimpleFeatureBean)mFeatureHandler.getOrLoadModelForOid(oid);
    if (model != null)
      return model.getOutput(outputName);
    return null;
  } // End method: getOutput

  /** Return either query or subject aligned residues for simple feature. */
  public String getQueryAlignedResidues(OID oid) {
    loadFeaturesIfNeeded();
    SimpleFeatureBean model = (SimpleFeatureBean)mFeatureHandler.getOrLoadModelForOid(oid);
    if (model != null)
      return model.getQueryAlignment();
    return null;
  } // End method: getQueryAlignedResidues

  public String getSubjectAlignedResidues(OID oid) {
    loadFeaturesIfNeeded();
    SimpleFeatureBean model = (SimpleFeatureBean)mFeatureHandler.getOrLoadModelForOid(oid);
    if (model != null)
      return model.getSubjectAlignment();
    return null;
  } // End method: getSubjectAlignedResidues

  /**
   * Returns subject sequence of the subject sequence OID given.  Repeat:
   * this is for the subject sequence, not for a feature.
   * @See getSubjectSequenceOids(OID oid);
   */
  public Sequence getSubjectSequence(OID subjectSequenceOid) {
    loadFeaturesIfNeeded();
    if (getFeatureHandler().getSubjectSeqOids().contains(subjectSequenceOid)) {
      if (mSequenceLoader == null)
        mSequenceLoader = new SequenceLoader(getLoadedFileNames()[0]);
      return mSequenceLoader.getSequence(subjectSequenceOid.toString());
    } // Must scan for it.
    return null;
  } // End method: getSubjectSequence

  /**
   * Retrieves the subject sequence OID collection for the
   * feature whose OID was given.  Expect this to be
   * called against blast-level (compound) features, but
   * do not rule out HSP-level (simple) features.
   */
  public Set getSubjectSequenceOids(OID oid) {
    loadFeaturesIfNeeded();

    Set returnSet = new HashSet();
    FeatureBean model = mFeatureHandler.getOrLoadModelForOid(oid);
    if (model != null) {
      recursivelyFindSubject(model, returnSet);
    } // Found feature.

    return returnSet;

  } // End method

  /** Returns a report of all features with the subject sequence of OID given. */
  public void addToSubjSeqRpt(String subjectSeqId, OID genomeVersionOID, SubjectSequenceReport report) {
    try {
      if (subjectSeqId.indexOf(":") == -1)
        subjectSeqId = "INTERNAL:" + subjectSeqId;

      OID subjectSeqOid = parseFeatureOID(subjectSeqId);
      SubjSeqRptHandler handler = new SubjSeqRptHandler(  getLoadedFileNames()[0],
                                                          mSequenceAlignment,
                                                          (OIDParser)this);
      Set subjSeqOids = new HashSet();
      subjSeqOids.add(subjectSeqOid);
      handler.getRptLines(subjSeqOids, report);
    } // End try block
    catch (Exception ex) {
      FacadeManager.handleException(ex);
    } // End catch
  } // End method

  /**
   * Returns list of alignments to features.  Said features will have description
   * text containing the search target given.
   */
  public List getAlignmentsForDescriptionsWith(String searchTarget) {
    loadFeaturesIfNeeded();
    List featureList = mFeatureHandler.getModelsForDescriptionsWith(searchTarget);

    FeatureBean nextFeature = null;
    List returnList = new ArrayList();
    for (Iterator it = featureList.iterator(); it.hasNext(); ) {
      nextFeature = (FeatureBean)it.next();
      returnList.add(nextFeature.alignFeature());
    } // For all models found.

    return returnList;

  } // End method

  /** Returns description associated with model whose OID is given. */
  public String getFeatureDescription(OID oid) {
    loadFeaturesIfNeeded();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(oid);
    if (model != null)
      return model.getDescription();
    return null;
  } // End method: getFeatureDescription

  /** Returns description associated with model whose OID is given. */
  public String getFeatureDescriptionForParent(OID oid) {
    loadFeaturesIfNeeded();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(oid);
    if (model != null) {
      if (model.getParent() != null) {
        return model.getParent().getDescription();
      } // Has a parent.
    } // Has a model.
    return null;
  } // End method: getFeatureDescription

  /** Returns score string for model whose oid was given. */
  public String getScore(OID oid) {
    loadFeaturesIfNeeded();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(oid);
    if (model != null)
      return model.getScore();
    return null;
  } // End method: getScore

  /** Return either individual or summary expect value for model whose oid was given. */
  public String getIndividualExpect(OID oid) {
    loadFeaturesIfNeeded();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(oid);
    if (model != null)
      return model.getIndividualExpect();
    return null;
  } // End method: getIndividualExpect

  public String getSummaryExpect(OID oid) {
    loadFeaturesIfNeeded();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(oid);
    if (model != null)
      return model.getSummaryExpect();
    return null;
  } // End method: getSummaryExpect

  /** Return either subject start or end value associated with OID given. */
  public int getSubjectStart(OID oid) {
    loadFeaturesIfNeeded();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(oid);
    if (model != null)
      return model.getSubjectStart();
    return 0;
  } // End method: getSubjectStart

  public int getSubjectEnd(OID oid) {
    loadFeaturesIfNeeded();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(oid);
    if (model != null)
      return model.getSubjectEnd();
    return 0;
  } // End method: getSubjectEnd

  /** Returns the unique set of OIDs to which features found in the file refer. */
  public Set getReferencedOIDSet() {
    loadFeaturesIfNeeded();

    // Wish to add OID referenced by any contig.
    Set lReferencedSet = mFeatureHandler.getReferencedOIDSet();
    if (this.contigAlignment != null)
      lReferencedSet.add(genomicAxisOid);

    return lReferencedSet;
  } // End method: getReferencedOIDSet

  /** Returns all precomputed root features aligned to the axis given as oid. */
  public List getRootFeatures(OID axisOID, Set rangesOfInterest, boolean humanCurated) {
    loadAxisOverRangeSet(axisOID, rangesOfInterest, humanCurated);
    if (humanCurated)
      return mFeatureHandler.findHumanCuratedFeaturesOnAxis(axisOID, rangesOfInterest);
    else
      return mFeatureHandler.findPrecomputedFeaturesOnAxis(axisOID, rangesOfInterest);
  } // End method: getPrecomputedRootFeatures

  /**
   * Returns obsolete features referencing axis that exist in entire file.
   * By definition, obsolete features are human curated.
   *
   * @param OID axisOID axis to which feature reference.
   * @return List list of entities that are obsolete and ref the axis.
   */
  public List getObsoleteRootFeatures(OID axisOID) {
    // ALGORITHM: run load, and keep only curated/obsolete root features
    //  in memory.  Make a complete list of the features created from these
    //  models, and return it.
    loadAxisObsoleteFeatures(axisOID);
    return mFeatureHandler.findObsoleteFeaturesOnAxis(axisOID);
  } // End method

  /**
   * Returns obsolete features UNDER the feature whose OID is given, and
   * referencing the axis given.
   *
   * @param OID axisOID axis to which feature reference.
   * @param OID parentOID feature containing features of interest.
   * @return List list of entities that are obsolete and ref the axis.
   */
  public List getObsoleteNonRootFeatures(OID axisOID, OID parentOID) {
    // ALGORITHM: look at the model corresponding to the feature whose OID was
    //  given.  Traverse its hierarchy of subfeatures, and find all those
    //  models at next lower level that are obsolete.
    loadFeaturesIfNeeded();
    List returnList = new ArrayList();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(parentOID);
    if ((model != null) && (model.getAxisOfAlignment().equals(axisOID))) {

      boolean parentIsObsolete = false;
      if (model.getParent() != null)
        parentIsObsolete = model.getParent().isObsolete();

      FeatureBean subModel = null;
      if (model instanceof CompoundFeatureBean) {
        for (Iterator it = ((CompoundFeatureBean)model).getChildren().iterator(); it.hasNext(); ) {
          subModel = (FeatureBean)it.next();
          if (subModel.isObsolete())
            returnList.add(subModel.createFeatureEntity());
          else if (parentIsObsolete)
            throw new IllegalStateException(  "Parent feature is obsolete, but contains a non-obsolete child"+
                                              " parent OID: "+model.getParent().getOID()+
                                              " child OID: "+model.getOID());
        } // For all children.
      } // Has descendants.

    } // Refers to right axis and is obsolete.
    return returnList;
  } // End method

  /** Returns flag of whether the feature whose OID was given is known to this loader. */
  public boolean featureExists(OID oid) {
    loadFeaturesIfNeeded();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(oid);
    return (model != null);
  } // End method: featureExists

  /** Drops the feature whose OID is given, from feature cache. */
  public void removeFeature(OID oid) {
    if (mFeatureHandler == null)
      return; // Nothing to remove.
    mFeatureHandler.removeFeatureFromCache(oid);
  } // End method: removeFeature

  /** Returns flag of whether the OID in question is human curated. */
  public boolean isCurated(OID oid) {
    loadFeaturesIfNeeded();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(oid);
    if (model != null)
      return model.isCurated();

    // Arbitrarily default to non-curated.
    return false;

  } // End method: isCurated

  /**
   * Returns analysis type for feature of oid given.
   */
  public String getAnalysisTypeOfFeature(OID featureOid) {
    loadFeaturesIfNeeded();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(featureOid);
    if (model == null)
      return null;
    return model.getAnalysisType();
  } // End method

  /**
   * Return discovery environment of feature given.
   */
  public String getDiscoveryEnvironmentOfFeature(OID featureOid) {
    loadFeaturesIfNeeded();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(featureOid);
    if (model == null)
      return null;
    return model.getDiscoveryEnvironment();
  } // End method

  /**
   * Returns range on axis of feature given.
   */
  public Range getRangeOnAxisOfFeature(OID featureOid) {
    loadFeaturesIfNeeded();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(featureOid);
    if (model == null)
      return null;
    return model.calculateFeatureRange();
  } // End method

  /** Returns OID of alignment for feature given. */
  public OID getAxisOidOfAlignment(OID featureOid) {
    loadFeaturesIfNeeded();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(featureOid);
    if (model == null)
      return null;
    return model.getAxisOfAlignment();
  } // End method

  /** Returns alignment for the feature whose OID was given. */
  public Alignment getAlignmentForFeature(OID featureOID) {
    loadFeaturesIfNeeded();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(featureOID);
    if (model != null)
      return model.alignFeature();
    return null;
  } // End method: getAlignmentForFeature

  /** Returns alignment for the feature whose accession string was given. */
  public Alignment getAlignmentForAccession(String accnum, int accessionType) {
    loadFeaturesIfNeeded();
    FeatureBean model = null;
    if (accessionType == LoaderConstants.GENE_ACCESSION_TYPE) {
      model = (GeneFeatureBean)mFeatureHandler.getModelForGeneAccession(accnum);
    } // Return a gene.
    else if (accessionType == LoaderConstants.NONPUBLIC_ACCESSION_TYPE) {
      model = (FeatureBean)mFeatureHandler.getModelForInternalAccession(accnum);
    } // Return a transcript.

    // Work out what to return.
    if (model == null)
      return null;
    else
      return model.alignFeature();

  } // End method: getAlignmentForAccession

  /** Gets the gene for the OID given.  Gets annotation name for gene containing compound feature (transcript). */
  public String getGene(OID oid) {
    loadFeaturesIfNeeded();
    return mFeatureHandler.getGeneForTranscriptOID(oid);
  } // End method: getGene

  /** Returns the comments associated with a gene. */
  public GenomicEntityComment[] getComments(OID oid) {
    loadFeaturesIfNeeded();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(oid);
    if (model != null)
      return model.getComments();
    return new GenomicEntityComment[0];
  } // End method: getComments

  /**
   * Returns property sources associated with OID.  These sources are necessary because
   * properties may be recursive in nature.
   */
  public List getPropertySources(OID oid) {
    loadFeaturesIfNeeded();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(oid);
    if (model != null)
      return model.getPropertySources();
    return new ArrayList();
  } // End method: getPropertySources

  /**
   * Returns replaced data objects.  These are necessary because replaced data
   * is somewhat complex in nature.
   */
  public List getReplacedData(OID oid) {
    loadFeaturesIfNeeded();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(oid);
    if (model != null)
      return model.getReplacedList();
    return new ArrayList();
  } // End method: getReplacedData

  /** Returns OIDs of evidence of a curation. */
  public OID[] getEvidence(OID oid) {
    loadFeaturesIfNeeded();
    FeatureBean model = (FeatureBean)mFeatureHandler.getOrLoadModelForOid(oid);
    if (model != null)
      return model.getEvidence();
    return new OID[0];
  } // End method: getEvidence

  /** Add/remove listeners for the appearance of sequence alignments in the file. */
  public void addSequenceAlignmentListener(SequenceAlignmentListener listener) {
    listeners.add(listener);
  } // End method: addSequenceAlignmentListener

  public void removeSequenceAlignmentListener(SequenceAlignmentListener listener) {
    if (listeners.contains(listener))
      listeners.remove(listener);
  } // End method: removeSequenceAlignmentListener

  //---------------------------------------OTHER PUBLIC METHODS
  /**
   * Builds an OID with no special restrictions or translations.
   *
   * If no special restrictions are required in a subclass for
   * a particular type of OID handling, this method may be called
   * by the parseXxxxxxOIDTemplateMethod implementation.
   *
   * @param String idstr of format "PREFIX:dddddddddd"
   *   where the d's represent a decimal (long) number.
   * @return OID an OID in either the specified or an unknown namespace.
   */
  public OID parseOIDGeneric(String idstr) {

    String[] strArrayForId = null;
    OID returnOID = null;

    char startchr = idstr.charAt(0);

    // Two possible formats: id="CCCC:ddddddddddd" or
    //    id="ddddddd".  First contains a namespace prefix,
    //    and the second is just the digits.
    //
    if(Character.isLetter(startchr)){
      strArrayForId=processIdStringForPrefix(idstr);
      String oid = strArrayForId[1];
      String namespacePrefix =
         ReservedNameSpaceMapping.translateToReservedNameSpace(strArrayForId[0]);
      returnOID = new OID(namespacePrefix, oid, getGenomeVersionId());

      // Need a running counter for certain kinds of OIDs.
      if (returnOID.isScratchOID()) {
        // Test this read OID against the current highest OID, and make it the
        // new highest if it is higher.
        try {
          OIDGenerator.getOIDGenerator().setInitialValueForNameSpace
                (OID.SCRATCH_NAMESPACE, Long.toString(1 + returnOID.getIdentifier()));
        } // End try block
        catch (IllegalArgumentException iae) {
          // Ignoring here: we only wish to seed the generator with highest known value.
        } // End catch block for seed.
      } // Found another scratch.

    } // Proper alphabetic prefix.
    //else if (Character.isDigit(startchr)) {
      //oidlong = Long.parseLong(idstr);
      //returnOID = new OID(OID.UNKNOWN_NAMESPACE,Long.toString(oidlong), getGenomeVersionId());
    //} // Found digit as first character.
    else {
      if (idstr.indexOf(':') >= 0) {
		FacadeManager.handleException( new IllegalArgumentException( "This application is expecting a namespace prefix beginning with an alphabetic character in its XML IDs.\nYou specified '"
                                            +idstr+"'."));
      } // Prefix invalid
      else {
        // NOTE: as of 5/7/2001, we found that the EJB/db are getting confused
        //       about non-internal OIDs.  TO fix this, we are precluding non-
        //       internal database OIDs from being sent there.  Unfortunately,
        //       this also requires no-prefix OIDs no longer be accepted.
          returnOID = new OID(OID.INTERNAL_DATABASE_NAMESPACE, idstr, getGenomeVersionId());
//        JCVI LLF, 10/20/2006
//        FacadeManager.handleException( new IllegalArgumentException( "This application is expecting a namespace prefix in its XML IDs.\nYou specified '"
//                                            +idstr+"'.\nIf this is an internal database ID, please change that to 'INTERNAL:"
//                                            +idstr+"'.\nIf not, prefix it with a namespace of your own."
//                                            +((getLoadedFileNames().length >= 0) ? "\nSee "+getLoadedFileNames()[0] : "")
//                                            ));
      } // No prefix at all.
      // oidlong=Long.parseLong(idstr);
      // returnOID=new OID(OID.UNKNOWN_NAMESPACE,Long.toString(oidlong));
      // throw new IllegalArgumentException("Invalid OID "+idstr+": expecting a letter as first character.  Example:");
    } // Found unexpected character as first character.

    return returnOID;
  } // End method: parseOIDGeneric

  /**
   * Builds an OID with no special restrictions or translations,
   * and can translate a GA number into an OID.
   *
   * @param String idstr of format "PREFIX:dddddddddd"
   *   where the d's represent a decimal (long) number.
   * @return OID an OID in either the specified or an unknown namespace.
   */
  protected OID parseOIDorGA(String idstr) {

    String[] strArrayForId = null;
    long oidLong = 0L;
    OID returnOID = null;

    char startchr = idstr.charAt(0);

    // Two possible formats: id="CCCC:ddddddddddd" or
    //    id="ddddddd".  First contains a namespace prefix,
    //    and the second is just the digits.
    //
    if(Character.isLetter(startchr)){
      strArrayForId=processIdStringForPrefix(idstr);

      if (strArrayForId[1].startsWith(GANumericConverter.GA_PREFIX))
        oidLong = GANumericConverter.getConverter().getOIDValueForGAName(strArrayForId[1]);
      else
        oidLong = Long.parseLong(strArrayForId[1]);

      String namespacePrefix =
         ReservedNameSpaceMapping.translateToReservedNameSpace(strArrayForId[0]);

      returnOID = new OID(namespacePrefix,Long.toString(oidLong), getGenomeVersionId());

      // Need a running counter for certain kinds of OIDs.
      if (returnOID.isScratchOID()) {
        // Test this read OID against the current highest OID, and make it the
        // new highest if it is higher.
        try {
          OIDGenerator.getOIDGenerator().setInitialValueForNameSpace(OID.SCRATCH_NAMESPACE,Long.toString(++oidLong));
        } // End try block.
        catch (IllegalArgumentException iae) {
          // Ignoring here: we only wish to seed the generator with highest known value.
        } // End catch block for seed.
      } // Found another scratch.

    } // Proper alphabetic prefix.
    else if (Character.isDigit(startchr)) {
      oidLong = Long.parseLong(idstr);
      returnOID = new OID(OID.UNKNOWN_NAMESPACE,Long.toString(oidLong), getGenomeVersionId());
    } // Found digit as first character.
    else {
       // System.err.println("ERROR: unexpected initial character in input OID "+idstr);
       throw new IllegalArgumentException("Invalid OID "+idstr+": this protocol expects a letter as first character");
    } // Found unexpected character as first character.
    return returnOID;
  } // End method: parseOIDorGA

  /**
   * Allows external set of the facade manager to use in creating
   * models.
   */
  public void setFacadeManager(XmlFacadeManager lReadFacadeManager) {
    mReadFacadeManager = lReadFacadeManager;
  } // End method: setFacadeManager

  /** Returns an identifying string for this loader. */
  public String toString() {
    StringBuffer returnBuffer = new StringBuffer(1000);
    for (int i = 0; i < getLoadedFileNames().length; i++) {
      returnBuffer.append(getLoadedFileNames()[i]);
    } // For all loaded file names

    return returnBuffer.toString();
  } // End method: toString

  //------------------------------HELPER METHODS
  /** Allows subclass to set the feature handler that is used in grabbing feat.*/
  protected void setFeatureHandler(DrivingHandler lHandler) {
    mFeatureHandler = lHandler;
  } // End method

  /** Gets the feature handler for subclasses. */
  protected DrivingHandler getFeatureHandler() {
    return mFeatureHandler;
  } // End method

  /** Returns assembly version to subclass.  Used by workspace loader for promotion */
  protected String getAssembly() {
    loadInitialIfNeeded();
    return assemblyVersion;
  } // End method

  /** Finds all subject sequence oids from all children, descending, of given model. */
  protected void recursivelyFindSubject(FeatureBean model, Set returnSet) {
    // Check non-null params
    if ((returnSet == null) || (model == null))
      return;

    if (model instanceof SimpleFeatureBean) {
      OID subjectSequenceOid = null;
      subjectSequenceOid = ((SimpleFeatureBean)model).getSubjectSequenceOid();
      if (subjectSequenceOid != null)
        returnSet.add(subjectSequenceOid);
      return;
    } // Got simple model.

    // Get child list for model.
    List childrenOfModel = ((CompoundFeatureBean)model).getChildren();
    if (model == null)
      return;

    // Iterate over all children.
    FeatureBean childModel = null;
    for (Iterator it = childrenOfModel.iterator(); it.hasNext(); ) {
      childModel = (FeatureBean)it.next();
      if (childModel != null)
        recursivelyFindSubject(childModel, returnSet);
    } // For all children.

  } // End method

  /**
   * Calling this method triggers the initial scan of the file for information
   * related to axis and contig and residues.  Overridden here to lend this
   * behavior to genomic axis file reads ONLY.
   */
  protected synchronized void loadInitialIfNeeded() {
    if (mInitialScanComplete)
      return;

    // new RuntimeException("Running initial load for "+getLoadedFileNames()[0]).printStackTrace();
    mInitialScanComplete = true;

    InitialLoader initialLoader = new InitialLoader(getLoadedFileNames()[getLoadedFileNames().length - 1]);
    assemblyVersion = initialLoader.getAssembly();

    // Initial load may not pickup anything of value.  In that case, this
    // file is probably NOT an assembly file, and there is nothing more to collect.
    if (initialLoader.getGenomicAxisID() == null)
      return;

    // Pull species information.
    String taxon = initialLoader.getSpecies();
    if (taxon == null || taxon.length() == 0)
      taxon = DEFAULT_SPECIES;

    species = new Species(
      OIDGenerator.getOIDGenerator().generateOIDInNameSpace(  OID.API_GENERATED_NAMESPACE, getGenomeVersionId()),
                                                              taxon);

    axisLength = initialLoader.getAxisLength();
    genomicAxisOid = this.parseContigOID(initialLoader.getGenomicAxisID());

    sequenceBuilder = initialLoader.getSequenceBuilder();
    if (sequenceBuilder == null)
      residues = initialLoader.getSequence();

    if (initialLoader.getSequenceAlignments().size() == 0)
      createContigAlignment(axisLength);
    else {
      SequenceAlignment nextSequenceAlignment = null;
      SequenceAlignmentListener nextListener = null;
      for (Iterator it = initialLoader.getSequenceAlignments().iterator(); it.hasNext(); ) {
        nextSequenceAlignment = (SequenceAlignment)it.next();
        // System.out.println("Alignment found "+nextSequenceAlignment);

        // Now send off event to listeners.
        for (Iterator listenerToIt = listeners.iterator(); listenerToIt.hasNext(); ) {
          nextListener = (SequenceAlignmentListener)listenerToIt.next();
          nextListener.foundSequenceAlignment(nextSequenceAlignment, getGenomeVersionId());
        } // For all listeners.
      } // For all alignments

      // Now that all are handled, notify listener(s) that no more remain.
      SequenceAlignmentListener[] listenerArr = new SequenceAlignmentListener[listeners.size()];
      listeners.toArray(listenerArr);
      for (int i = 0; i < listenerArr.length; i++) {
        listenerArr[i].noMoreAlignments(this);
      } // For all listeners.
      listenerArr = null;

    } // Handle those alignments.

  } // End method: loadInitialIfNeeded

  /**
   * Invokes next-level load.  This is done on an "as-needed" basis
   * to avoid up-front load time and to keep the memory usage as low
   * as possible.
   */
  protected synchronized void loadFeaturesIfNeeded() {
    try {
      // Chain in all other loads to avoid bypassing order dependency.
      loadInitialIfNeeded();

      if (mFeatureHandler != null)
        return;

      /*
      System.gc();
      System.out.println("Loading from file "+getLoadedFileNames()[0]);
      System.out.println("Memory in use before feature load: "+
        (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
      */

      mFeatureHandler = new DrivingHandler(getLoadedFileNames()[0], mSequenceAlignment, (OIDParser)this);

      // Call to cache feature references, but not data FOR features.
      mFeatureHandler.accumulateFeatureReferenceData(getLoadedFileNames()[0]);

      /*
      System.gc();
      System.out.println("Memory in use after feature load: "+
        (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
      */
    } // End try block
    catch (Throwable throwable) {
      FacadeManager.handleException(throwable);
    } // End catch block.
  } // End method

  /**
   * Forces load, as well as caching of data, for axis data over the
   * set of ranges given. Returns what it has found.
   */
  protected synchronized void loadAxisOverRangeSet(OID axisOID, Set rangesOfInterest, boolean humanCurated) {
    try {
      // Chain in other loads to avoid bypassing order dependency.
      loadInitialIfNeeded();

      if (mFeatureHandler == null)
        mFeatureHandler = new DrivingHandler(getLoadedFileNames()[0], mSequenceAlignment, (OIDParser)this);

      /*
      System.gc();
      System.out.println("Loading range from file "+getLoadedFileNames()[0]);
      System.out.println("Memory in use before ranged feature load: "+
        (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
      */

      // Call to cache features over given range, aligning to given axis.
      // [String buffer-loaded features are already permanently cached.]
      if (! isLoadedFromStringBuffer())
        mFeatureHandler.accumulateFeatures(getLoadedFileNames()[0], axisOID, rangesOfInterest, humanCurated);

      /*
      System.gc();
      System.out.println("Memory in use after ranged feature load: "+
        (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
      */
    } // End try block
    catch (Throwable throwable) {
      FacadeManager.handleException(throwable);
    } // End catch block.

  } // End method

  /**
   * Forces load and caching of obsoleted features for an entire axis.
   */
  protected synchronized void loadAxisObsoleteFeatures(OID axisOID) {
    try {
      // Chain in other loads to avoid bypassing order dependency.
      loadInitialIfNeeded();

      if (mFeatureHandler == null)
        mFeatureHandler = new DrivingHandler(getLoadedFileNames()[0], mSequenceAlignment, (OIDParser)this);

      // Call to cache features over given range, aligning to given axis.
      // [String buffer-loaded features are already permanently cached.]
      if ((! isLoadedFromStringBuffer()) && (! mPreviouslyCachedObsoleteFeatures)) {
        mFeatureHandler.accumulateObsoleteFeatures(getLoadedFileNames()[0], axisOID);
        mPreviouslyCachedObsoleteFeatures = true;
      } // Must load the features.

    } // End try block
    catch (Throwable throwable) {
      FacadeManager.handleException(throwable);
    } // End catch block.

  } // End method

  /**
   * Primarily a 'facility' for use of a parseXxxxOIDTemplateMethod,
   * this method breaks up an id= attribute value into the
   * pieces needed to build an Object ID.  Should remain protected
   * visibility so that implementing subclasses of this abstract
   * class, can call this method!
   *
   * @param String idstr The id= attribute value
   * @return String[] first member is namespace prefix,
   *   the second member is the set of digits, which should
   *   make the OID unique in the name space.
   */
  protected String[] processIdStringForPrefix(String idstr){
    StringTokenizer s=new StringTokenizer(idstr,":");
    String[] nameSpaceIdarray=new String[2];
    nameSpaceIdarray[0]=s.nextToken();
    nameSpaceIdarray[1]=s.nextToken();
    return nameSpaceIdarray;

  } // End method: processIdStringForPrefix

  /**
   * Given a length, create the contig entity and its alignment to the axis for
   * the loaded file.  Contigs are not the "focus" of XML files, axes are.  So
   * the contig is made here.
   */
  private void createContigAlignment(int length) {
    OID coid = OIDGenerator.getOIDGenerator().generateOIDInNameSpace( OID.API_GENERATED_NAMESPACE,
                                                                      getGenomeVersionId());
    Contig contigEntity = new Contig(coid, "GA_"+coid.getIdentifierAsString(), length);

    contigAlignment = new GeometricAlignment(null, contigEntity, new Range(0, length-1));
  } // End method: createContigEntity

  /**
   * Returns genome version id.  May be overridden from subclass.
   */
  protected int getGenomeVersionId() {
    return mGenomeVersionId;
  } // End method

  /**
   * Allows set of genome version Id from subclass.
   */
  protected void setGenomeVersionId(int lGenomeVersionId) {
    mGenomeVersionId = lGenomeVersionId;
  } // End method

} // End class: SAXLoaderBase

