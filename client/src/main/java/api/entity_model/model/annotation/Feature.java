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
package api.entity_model.model.annotation;

import api.entity_model.access.comparator.SingleGeoAlignSingleAxisComparator;
import api.entity_model.access.filter.AlignmentCollectionFilter;
import api.entity_model.access.filter.FiltrationDevice;
import api.entity_model.access.observer.FeatureObserver;
import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.AlignmentNotAllowedException;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.*;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.facade.abstract_facade.fundtype.GenomicEntityLoader;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.NoData;
import api.stub.data.OID;
import api.stub.data.ReplacementRelationship;
import api.stub.geometry.MutableRange;
import api.stub.geometry.Range;
import api.stub.sequence.*;

import java.util.*;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 *
 * Features
 * - are AlignableGenomicEntity(s)
 * - have evidence
 * - exist within a Super-Feature to Sub-Feature structure.
 * - has an environment.
 *
 * @author       Jay T. Schira
 * @version $Id$
 */
public abstract class Feature extends AlignableGenomicEntity
    implements SingleAlignmentMultipleAxes {
    // A manual serial version UID... need to update this when we affect the interface.
    private static final long serialVersionUID = 11; // Unique identifier for a Feature

    // Feature notification types...
    private static final int NOTE_SUBFEATURE_ADDED = 30;
    private static final int NOTE_SUBFEATURE_REMOVED = 31;
    private static final int NOTE_EVIDENCE_ADDED = 32;
    private static final int NOTE_EVIDENCE_REMOVED = 33;
    private static final int NOTE_WORKSPACE_REPLACEMENT_STATE_CHANGED = 34;
    private static final int NOTE_PROMOTED_REPLACEMENT_STATE_CHANGED = 35;
    private static Map discoveryEnvironmentNumberToName = new HashMap();
    private static Map discoveryEnvironmentNameToNumber = new HashMap();

    /**
     * Static initializer to fill discoveryEnvMap...
     */
    static {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(
                                            "resource.shared.DataLayerToFeatureGroup");

            if (bundle != null) {
                Enumeration e = bundle.getKeys();
                Short tmpNumber;
                String tmpString;
                String tmpEnv;

                while (e.hasMoreElements()) {
                    try {
                        tmpString = (String) e.nextElement();
                        tmpEnv = bundle.getString(tmpString);
                        tmpNumber = new Short(tmpString);
                        discoveryEnvironmentNumberToName.put(tmpNumber, tmpEnv);
                        discoveryEnvironmentNameToNumber.put(tmpEnv, tmpNumber);
                    } catch (Exception ex) {
                        System.out.println(
                                "problem in resource.shared.DataLayerToFeatureGroup");
                    } //continue on with any single exception
                }
            } else {
                System.err.println(
                        "No resource.shared.DataLayerToFeatureGroup file found");
            }
        } catch (Exception ex) {
            System.err.println(
                    "No resource.shared.DataLayerToFeatureGroup file found or problem with the file");
        }
    }

    /**
     * @label superFeature
     * @supplierCardinality 1
     */
    private Feature superFeature; // the feature that contains this feature.

    /**
     * The sub-features of this feature.
     * @associates <{Feature}>
     * @supplierCardinality 0..*
     * @label subFeatures
     */
    private HashSet subFeatures; // a set of feature that this feature contains.

    /**
     *@associates <{Oid}>
     * @supplierCardinality 0..*
     * @label evidence oids
     */
    private HashSet evidenceOids; // a collection of OIDs of GenomicEntities that are evidence for this feature.

    /**
     * A simple string that a scientist can use to determine under what
     * conditions a feature was discovered.
     */
    private String discoveryEnvironment;
    private short discoveryEnvironmentNumber = -1;

    /**
     * The Feature display priority of this feature.
     */
    byte displayPriority;

    //*  Construction

    /**
     * Constructor...
     */
    public Feature(OID oid, String displayName, EntityType type, 
                   String discoveryEnvironment)
            throws InvalidFeatureStructureException {
        this(oid, displayName, type, discoveryEnvironment, null, null, 
             FeatureDisplayPriority.DEFAULT_PRIORITY);
    }

    /**
     * Constructor with full args.
     */
    public Feature(OID oid, String displayName, EntityType type, 
                   String discoveryEnvironment, 
                   FacadeManagerBase readFacadeManager, Feature superFeature, 
                   byte displayPriority) throws InvalidFeatureStructureException {
        super(type, oid, displayName, readFacadeManager);

        Short discoveryEnvShort = (Short) discoveryEnvironmentNameToNumber.get(
                                          discoveryEnvironment);

        if (discoveryEnvShort != null) {
            discoveryEnvironmentNumber = discoveryEnvShort.shortValue();
        } else {
            this.discoveryEnvironment = discoveryEnvironment;
        }

        if (superFeature != null) {
            this.superFeature = superFeature;

            // Also need to add this as a sub feature to the parent
            if (superFeature != null) {
                superFeature.protectedAddSubFeature(this);
            }
        }

        this.displayPriority = displayPriority;
    }

    //****************************************
    //*  Public methods
    //****************************************
    // Static utilities...

    /**
     * Given a set of features return the corresponding ordered set.
     * Ordering is based on the position of the Feature on the axis.
     * If the argument features collection is null, this method will return null.
     * @param features a collection of Feature(s)
     * @param ascending order features with smaller axis positions first
     */
    public static List getOrderedFeatureSet(Collection featuresToSort, 
                                            boolean ascending) {
        ArrayList orderedFeatureSet = new ArrayList();

        if (featuresToSort == null) {
            return null;
        }

        java.util.SortedSet sortedFeatureSet = new java.util.TreeSet(
                                                       new SingleGeoAlignSingleAxisComparator(
                                                               ascending));

        for (Iterator itr = featuresToSort.iterator(); itr.hasNext();) {
            sortedFeatureSet.add(itr.next());
        }

        for (Iterator itr = sortedFeatureSet.iterator(); itr.hasNext();) {
            orderedFeatureSet.add(itr.next());
        }

        return orderedFeatureSet;
    }

    //== Support for alignments ==============================

    /**
     * @return The single Alignment to one of the (many) Axes this entity is aligned to.
     * Support for SingleAlignmentsMultipleAxes interface.
     */
    public Alignment getOnlyAlignmentToAnAxis(Axis anAxis) {
        return super.getOnlyAlignmentToAnAxis(anAxis);
    }

    /**
     * @return The single GeometricAlignment to one of the (many) Axes this entity is aligned to.
     * If the alignment is NOT a GeometricAlignment, this will return null.
     * Support for SingleAlignmentsMultipleAxes interface.
     */
    public GeometricAlignment getOnlyGeometricAlignmentToAnAxis(Axis anAxis) {
        return super.getOnlyGeometricAlignmentToAnAxis(anAxis);
    }

    //== Support for facade ==============================

    /**
     * Get the feature facade for this feature.
     */
    protected FeatureFacade getFeatureFacade() {
        FacadeManagerBase theFacadeManger = this.getLoaderManager();
        FeatureFacade theFeatureFacade = null;

        // theFacadeManager.
        try {
            theFeatureFacade = (FeatureFacade) theFacadeManger.getFacade(
                                       getEntityType());
        } catch (Exception ex) {
            // Don't really want to do anything.
        }

        return theFeatureFacade;
    }

    //== Feature Parent Child Management Methods ==============================

    /**
     * Determine if I will accept a SuperFeature as a super feature of mine.
     * This is usually called right before a call to setSuperFeature on the
     * mutator class.
     * Subclasses can over-ride this method to make sure that there is
     * the correct hierarchy of containment... such as Gene, Transcript, Exon.
     * Subclasses should ALWAYS be more restrictive than thier super classes.
     */
    public boolean willAcceptSuperFeature(Feature newSuperFeature) {
        if (newSuperFeature == null) {
            return true;
        }

        return (this instanceof SubFeature);
    }

    /**
     * Determine if I will accept a SubFeature as a sub feature of mine.
     * This is usually called right before a call to addSubFeature on the
     * mutator class.
     * Subclasses can over-ride this method to make sure that there is
     * the correct hierarchy of containment... such as Gene, Transcript, Exon.
     * Subclasses should ALWAYS be more restrictive than thier super classes.
     */
    public boolean willAcceptSubFeature(Feature newSubFeature) {
        return (this instanceof SuperFeature);
    }

    /**
     * Get the "Root" feature.  Go up through the SuperFeature hierarchy until
     * you are at the top.
     */
    public Feature getRootFeature() {
        // Go up to the "root" feature...
        Feature rootFeature = Feature.this;

        while (rootFeature.getSuperFeature() != null)
            rootFeature = rootFeature.getSuperFeature();

        return rootFeature;
    }

    /**
     * Convenience method to determine if this Feature is "Simple".
     * The algorithm for determining this is different for "Misc" features.
     */
    public boolean isSimple() {
        return !(this instanceof SuperFeature);
    }

    /**
     * Method takes the edge desired, computes splices, and sees if that edge
     * corresponds to a splice site.  The boolean passed in tells the Feature
     * if the desired edge is the start or end edge.
     */
    public boolean hasSpliceEdge(Range testRange, boolean calculateDonor) {
        GeometricAlignment alignment = getOnlyGeometricAlignmentToOnlyAxis();
        GenomicAxis axis = (GenomicAxis) alignment.getAxis();

        return axis.spliceExistsAtEdge(testRange, calculateDonor);

        /**
         *  The return below was commented out so the users only employ the basic AG-GT
         *  splice check yielded by the equation above.
         *  The lines below use the same rigorous calculation for splice sites that the
         *  model uses to compute them for a region.
         *  @todo We should allow the user to tailor the level of splice edge calculation:
         *  AG-GT check-only as above or the full out calc. of all possible w/maybe a filter
         *  based on probability score.
         */

        // return axis.spliceExistsAtEdge(testRange,
        //   getGenomeVersion().getSpecies().getSpliceTypeStatisticalModels());
    }

    /**
     * Provide the super feature that contains this feature.
     */
    public Feature getSuperFeature() {
        return superFeature;
    }

    /**
     * Convenience check to see if this feature has a super-features...
     */
    public boolean hasSuperFeature() {
        if (this.getSuperFeature() == null) {
            return false;
        }

        return true;
    }

    /**
     * Convenience check to see if this feature has any sub-features...
     */
    public boolean hasSubFeatures() {
        if (subFeatures == null) {
            return false;
        }

        return !subFeatures.isEmpty();
    }

    /**
     * Provide a copy of the collection of sub features
     * contained by this feature.
     * The "subFeature" relationship is special and implies certain
     * restrictions of alignments of super features and sub-features.
     */
    public Collection getSubFeatures() {
        if (subFeatures == null) {
            return new HashSet();
        }

        return (Collection) subFeatures.clone();
    }

    /**
     * Should only be called DIRECTLY during construction, or should be called
     * from the mutator.
     * This method does NOT trigger notification or re-calcuations.
     */
    void protectedAddSubFeature(Feature newSubFeature) {
        // Make sure that we have a non-null subFeatures collection...
        if (this.subFeatures == null) {
            this.subFeatures = new HashSet();
        }
        // if we already had one, make sure we don't already have this subfeature...
        else if (this.subFeatures.contains(newSubFeature)) {
            return;
        }


        // Add the new sub-feature to this Feature's sub-features...
        this.subFeatures.add(newSubFeature);
    }

    /**
     * Provide a collection of sub structural features.
     * This "sub-structure" relationship is completely general and has no
     * implications other than whole-part.
     */
    public Collection getSubStructure() {
        return this.getSubFeatures();
    }

    /**
     * Provide the count of the sub features contained by this feature.
     */
    public int getSubFeatureCount() {
        if (subFeatures == null) {
            return 0;
        }

        return subFeatures.size();
    }

    /**
     * Check to see if a given feature is contained by this feature.
     */
    public boolean containsSubFeature(Feature aSubFeature) {
        // Check for null argument...
        if (aSubFeature == null) {
            return false;
        }

        // Check that we have a collection...
        if (subFeatures == null) {
            return false;
        }

        // Check in the collection...
        return subFeatures.contains(aSubFeature);
    }

    /**
     * Check to see if all of a collection of feature are contained by this feature.
     */
    public boolean containsAllSubFeatures(Collection someSubFeatures) {
        if (subFeatures == null) {
            return false;
        }

        return subFeatures.containsAll(someSubFeatures);
    }

    /**
     * Get any sub-features that have GeometricAlignment(s) that INTERSECT
     * a range on the axis.
     */
    public Collection getSubFeaturesInRangeOnAxis(Range aRangeOnAxis) {
        aRangeOnAxis.getStart();

        ArrayList subFeaturesInRange = new ArrayList();
        Feature aSubFeature = null;
        GeometricAlignment subFeatGeoAlign;

        if (subFeatures != null) {
            for (Iterator subItr = subFeatures.iterator(); subItr.hasNext();) {
                aSubFeature = (Feature) subItr.next();
                subFeatGeoAlign = aSubFeature.getOnlyGeometricAlignmentToOnlyAxis();

                if (subFeatGeoAlign != null) {
                    if (aRangeOnAxis.intersects(
                                subFeatGeoAlign.getRangeOnAxis())) {
                        // Add it to the list...
                        subFeaturesInRange.add(aSubFeature);
                    }
                }
            }
        }

        return Collections.unmodifiableCollection(subFeaturesInRange);
    }

    /**
     * Get the first sub-feature that has a GeometricAlignment that intersect
     * a position on the axis.
     */
    public Feature getSubFeatureAtPositionOnAxis(Axis theAxis, 
                                                 int positionOnAxis) {
        Feature subFeatureAtPos = null;
        Feature aSubFeature = null;
        GeometricAlignment subFeatGeoAlign;

        if (subFeatures != null) {
            for (Iterator subItr = subFeatures.iterator(); subItr.hasNext();) {
                aSubFeature = (Feature) subItr.next();
                subFeatGeoAlign = aSubFeature.getOnlyGeometricAlignmentToOnlyAxis();

                if (subFeatGeoAlign != null) {
                    if (subFeatGeoAlign.getRangeOnAxis()
                                       .contains(positionOnAxis)) {
                        subFeatureAtPos = aSubFeature;

                        break;
                    }
                }
            }
        }

        return subFeatureAtPos;
    }

    //== Display Priority Methods =========================================

    /**
     * Get the display priority.
     */
    public byte getDisplayPriority() {
        return displayPriority;
    }

    /**
     * Get the FeatureDisplayPriority that this Feature is in.
     */
    public FeatureDisplayPriority getFeatureDisplayPriority() {
        return FeatureDisplayPriority.featureDisplayPriorityFor(displayPriority);
    }

    //== Alignment Management Methods =========================================

    /**
     * Calculate alignment based on subfeatures.
     * Returns a new GeometricAlignment.
     * Will return NULL if;
     * - there are no sub-feature
     * - none of the sub-features have a geometric alignment.
     * @todo: Should be able to do a union on a mutable range wo new range.
     * @todo: Where should I get an axis if I'm not aligned?
     */
    public GeometricAlignment getTotalGeomAlignmentOfSubFeatures() {
        // Make sure I can have sub-features...
        if (!(this instanceof SuperFeature)) {
            return null;
        }

        // Check for no sub-features...
        if (subFeatures == null) {
            return null;
        }

        GeometricAlignment totalGeomAlignment = null;
        Range totalRange = null;
        GeometricAlignment aGeomAlignment;
        Axis theAxis = null;
        GeometricAlignment thisAlignment = this.getOnlyGeometricAlignmentToOnlyAxis();

        if (thisAlignment != null) {
            theAxis = thisAlignment.getAxis();
        }

        // Span across all the sub-features...
        for (Iterator subFeatItr = subFeatures.iterator();
             subFeatItr.hasNext();) {
            aGeomAlignment = ((Feature) subFeatItr.next()).getOnlyGeometricAlignmentToOnlyAxis();

            if (aGeomAlignment != null) {
                if (theAxis == null) {
                    theAxis = aGeomAlignment.getAxis();
                }

                if (totalRange == null) {
                    totalRange = new MutableRange(
                                         aGeomAlignment.getRangeOnAxis());
                } else {
                    totalRange = Range.union(totalRange, 
                                             aGeomAlignment.getRangeOnAxis());
                }
            }
        }

        if (totalRange != null) {
            totalGeomAlignment = new GeometricAlignment(theAxis, this, 
                                                        totalRange);
        }

        return totalGeomAlignment;
    }

    /**
     * Determing if I will accept an Alignment to an Axis...
     */
    public void willAcceptAlignmentToAxis(Alignment alignmentToAxis)
                                   throws AlignmentNotAllowedException {
        super.willAcceptAlignmentToAxis(alignmentToAxis);

        if (!(alignmentToAxis.getAxis() instanceof GenomicAxis)) {
            throw new AlignmentNotAllowedException(
                    "Features can only be aligned to a" + " GenomicAxis ", 
                    alignmentToAxis);
        }
    }

    //== Evidence Management Methods =========================================

    /**
     * Private internal method used to make sure we've gotten the evidence OIDs
     * from the Facade and put them in a collection.
     *
     * Returns the collection of OIDs that are evidence.
     */
    private Set getEvidenceOidsFromFacade() {
        // FIRST, we need our evidenceOids collection filled...
        // If we don't have the evidence OID's yet, get them from the facade...
        // OID[] evidenceOIDs = FeatureFacade.retrieveEvidence( OID featureOID ) throws NoData;
        if ((evidenceOids == null) || (evidenceOids.size() == 0)) {
            // Allocate a new collection.
            this.evidenceOids = new HashSet();

            try {
                FeatureFacade myFeatureFacade = this.getFeatureFacade();

                // Get the evedence from the facade as an array...
                OID[] evidenceOidArray = myFeatureFacade.retrieveEvidence(
                                                 this.getOid());

                // Fill the collection...
                for (int i = 0; i < evidenceOidArray.length; i++) {
                    if (evidenceOidArray[i] != null) {
                        evidenceOids.add(evidenceOidArray[i]);
                    }
                }
            } catch (NoData ndEx) {
                ; // Nothing to do just allow mechanism to show no evidence
            }
        }

        return evidenceOids;
    }

    /**
     * Get all the GenomicEntity(s) that are evidence for this Feature and
     * have been loaded into the model.
     * If this Feature has evidence that has NOT been loaded into the model,
     * that evidence will NOT be returned.
     *
     * @returns A collection of GenomicEntity(s) that are evidence of this feature.
     */
    public Collection getEvidenceOids() {
        return Collections.unmodifiableCollection(
                       this.getEvidenceOidsFromFacade());
    }

    /**
     * Get all the GenomicEntity(s) that are evidence for this Feature and all
     * of it's sub-features, forcing a load into the model.
     * If this Feature has evidence that has NOT been loaded into the model,
     * that evidence will be returned.
     *
     * @returns A collection of GenomicEntity(s) that are evidence of this feature.
     */
    public Collection getDeepEvidence(boolean forceLoad) {
        Collection accumulatedEvidence;
        accumulatedEvidence = this.getEvidence(forceLoad);

        if (this.hasSubFeatures()) {
            for (Iterator itr = this.getSubFeatures().iterator();
                 itr.hasNext();) {
                Feature subFeat = (Feature) itr.next();
                accumulatedEvidence.addAll(subFeat.getDeepEvidence(forceLoad));
            }
        }

        return accumulatedEvidence;
    }

    /**
     * Get all the GenomicEntity(s) that are evidence for this Feature and
     * force a load into the model.
     * If this Feature has evidence that has NOT been loaded into the model,
     * that evidence will be returned.
     *
     * @returns A collection of GenomicEntity(s) that are evidence of this feature.
     */
    public Collection getEvidence() {
        return this.getEvidence(true);
    }

    /**
     * Get the GenomicEntity(s) that are evidence for this Feature and
     * have ALREADY been loaded into the model.
     * If this Feature has evidence that has NOT been loaded into the model,
     * that evidence will NOT be returned.
     *
     * @returns A collection of GenomicEntity(s) that are evidence of this feature.
     */
    public Collection getLoadedEvidence() {
        return this.getEvidence(false);
    }

    /**
     * Private form to get the GenomicEntity(s) that are evidence for this Feature and
     * either force a load, or not.
     *
     * @returns A collection of GenomicEntity(s) that are evidence.
     */
    private Collection getEvidence(boolean forceLoad) {
        // NEXT, we need to convert our OID list to instances of GenomicEntity...
        Collection evidenceGEs = new ArrayList();
        GenomeVersion theGenomicVersion = this.getGenomeVersion();
        GenomicEntity aGE = null;

		if( evidenceOids != null ) {
	        for (Iterator oidEvItr = evidenceOids.iterator(); oidEvItr.hasNext();) {
	            if (forceLoad) {
	                aGE = theGenomicVersion.getGenomicEntityForOid(
	                              (OID) oidEvItr.next());
	            } else {
	                aGE = theGenomicVersion.getLoadedGenomicEntityForOid(
	                              (OID) oidEvItr.next());
	            }
	
	            if (aGE != null) {
	                evidenceGEs.add(aGE);
	            }
	        }
		}

        // Return it as immutable, just so there's no misunderstanding.
        return evidenceGEs;
    }

    /**
     * Provide the count of the Evidence of this feature.
     * This method does NOT care if the evidence GEs have been loaded.
     */
    public int getEvidenceCount() {
        // Check for non-null evidenceOids and evidenceOids.isEmpty()
        // so we don't have to create a bogus GE collection.
        if (evidenceOids == null) {
            return 0;
        }

        return evidenceOids.size();
    }

    /**
     * Convenience check to see if this feature has any Evidence.
     * This method does NOT care if the evidence GEs have been loaded.
     */
    public boolean hasEvidence() {
        // Check for non-null evidenceOids and evidenceOids.isEmpty()
        // so we don't have to create a bogus GE collection.
        if (evidenceOids == null) {
            return false;
        }

        return !(evidenceOids.isEmpty());
    }

    /**
     * Override from Genomic Entity to support the pre-population of
     * any data that can be cached into attributes of this Feature
     */
    public void loadCachableData() {
        super.loadCachableData();
        this.getEvidence();
    }

    //== Unload Support Methods =========================================

    /**
     * Used for unloading.  Will be called on all features within the specified
     * range.
     *
     * @return number of entities this call unaligned (should be 1 except for features)
     *  Scratch features and non-root features should return 0.  Root features
     *  should return the number of features that were unloaded under them, as well
     *  as themselves.
     */
    protected int unloadIfPossible(Alignment alignment) {
        this.getFeatureMutator().setFeatureStructureUnderConstruction(true);

        int returnVal = unloadIfPossible(alignment, true);
        this.getFeatureMutator().setFeatureStructureUnderConstruction(false);

        return returnVal;
    }

    protected int unloadIfPossible(Alignment alignment, boolean checkForRoot) {
        // If this is scratch (aka workspace feature) it can't be unloaded.
        // If this has a super feature, it can't be unloaded (other than through the super).
        if ((checkForRoot && this.hasSuperFeature())) {
            return 0;
        }

        ((AlignableGenomicEntityMutator) getMutator()).removeAlignmentToAxis(
                alignment);

        // First unload this feature's sub-features...
        // get a clone of the sub-features...
        Collection subFeatures = this.getSubStructure();

        if (subFeatures == null) {
            return 1;
        }

        Feature subFeature;
        int counter = 1;
        Set subFeatureAlignments;

        if (!(alignment instanceof GeometricAlignment)) {
            return counter;
        }

        for (Iterator subFeatItr = subFeatures.iterator();
             subFeatItr.hasNext();) {
            subFeature = (Feature) subFeatItr.next();

            subFeatureAlignments = subFeature.getAlignmentsToAxis(
                                           alignment.getAxis());

            if (subFeatureAlignments.size() == 0) {
                continue;
            }

            if (subFeatureAlignments.size() == 1) {
                counter += subFeature.unloadIfPossible(
                                   (Alignment) subFeatureAlignments.iterator()
                                                                   .next(), 
                                   false);
            } else {
                System.out.println("Entered range of concern code");

                Range rangeOfConcern = ((GeometricAlignment) alignment).getRangeOnAxis();
                AlignmentCollectionFilter filter = AlignmentCollectionFilter.createAlignmentCollectionFilter(
                                                           rangeOfConcern);
                List filteredAlignments = FiltrationDevice.getDevice()
                                                          .executeAlignmentFilter(subFeatureAlignments, 
                                                                                  filter);

                if (filteredAlignments.size() == 0) {
                    throw new IllegalStateException(
                            "When unloading a subFeature," + 
                            " the subFeature was not found within the range of it's parent.");
                }

                if (filteredAlignments.size() > 1) {
                    throw new IllegalStateException(
                            "When unloading a subFeature," + 
                            " the subFeature was found multiple times within the range of it's parent.");
                }

                counter += subFeature.unloadIfPossible(
                                   (Alignment) subFeatureAlignments.iterator()
                                                                   .next(), 
                                   false);
            }
        }

        return counter;
    }

    //== Observer Management Methods =========================================

    /**
      *  To track changes in the this object over time, you must add
      *  yourself as an observer of the it.  NOTE: this will post
      *  notices of all existing subFeatures
      *  @see FeatureObserver.java
      */
    public void addFeatureObserver(FeatureObserver observer) {
        addFeatureObserver(observer, true);
    }

    /**
     * Add a FeatureObserver to the feature observer list.
     * @todo: We need to make sure that the Feature observer list is kept separate
     * from the GenomicEntity observer list.
     * @todo: noteEvidenceAdded...
     */
    public void addFeatureObserver(FeatureObserver observer, 
                                   boolean bringUpToDate) {
        addAlignableGenomicEntityObserver(observer, false);

        if (bringUpToDate) {
            if (subFeatures != null) {
                Feature[] subFeatureArray = getSubFeatureArray();

                for (int i = 0; i < subFeatureArray.length; i++) {
                    observer.noteSubFeatureAdded(this, subFeatureArray[i]);
                }
            }

            if (evidenceOids != null) {
                GenomeVersion theGenomicVersion = this.getGenomeVersion();
                GenomicEntity aGE = null;

                for (Iterator oidEvItr = evidenceOids.iterator();
                     oidEvItr.hasNext();) {
                    aGE = theGenomicVersion.getLoadedGenomicEntityForOid(
                                  (OID) oidEvItr.next());

                    if ((aGE != null) && (aGE instanceof Feature)) {
                        observer.noteFeatureEvidenceAdded(this, (Feature) aGE);
                    }
                }
            }
        }
    }

    /**
     * Remove a FeatureObserver from the feature observer list.
     */
    public void removeFeatureObserver(FeatureObserver observer) {
        removeAlignableGenomicEntityObserver(observer);
    }

    //== Translation Methods =========================================

    /**
     * Get the spliced residues associated with a composite feature.
     * This method will take the single geometric alignment of this
     * composite feature, and use it's Axis as the Axis to get Sequence
     * against for all it's sub-features.  Any sub-feature that does not
     * have a geometric alignment to this axis will not contribute sequence.
     * @todo Test getSplicedResidues()! 3/2/01
     * @todo getSplicedResidues() should ONLY work for SingleAlignmentSingleAxis, move to Transcript.
     */
    public Sequence getSplicedResidues() {
        if (!(this instanceof SuperFeature)) {
            return null;
        }

        // Make sure we have a geometric alignment...
        GeometricAlignment align = getOnlyGeometricAlignmentToOnlyAxis();

        if (align == null) {
            return null;
        }

        Sequence axisSeq = align.getAxis().getDNASequence();

        SequenceList splicedSeq = new SequenceList(Sequence.KIND_DNA);
        Iterator i = Feature.getOrderedFeatureSet(getSubFeatures(), true)
                            .iterator();

        while (i.hasNext()) {
            Feature subFeature = (Feature) i.next();

            if (subFeature instanceof CodonFeature) {
                continue; //make sure that codons are not counted if they are ever added as subfeatures.
            }

            GeometricAlignment subAlign = subFeature.getOnlyGeometricAlignmentToOnlyAxis();

            if (subAlign == null) {
                continue;
            }

            Range subRange = subAlign.getRangeOnAxis();
            splicedSeq.append(
                    new SubSequence(axisSeq, subRange.getMinimum(), 
                                    subRange.getMagnitude()));
        }

        return (align.getRangeOnAxis().isReversed())
               ? DNA.reverseComplement(splicedSeq) : splicedSeq;
    }

    /**
     * Converts from a position on a spliced feature to a position on genomic axis.
     */
    public int transformSplicedPositionToAxisPosition(int splicedPosition) {
        Range range = getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis();

        if (!(this instanceof SuperFeature)) {
            return range.isReversed()
                   ? (range.getMaximum() - splicedPosition)
                   : range.getMinimum() + splicedPosition;
        }

        if (range.isReversed()) {
            splicedPosition = determineSplicedSize() - splicedPosition;
        }

        Iterator i = getOrderedFeatureSet(getSubFeatures(), true).iterator();

        while (i.hasNext()) {
            Feature subFeature = (Feature) i.next();

            if (subFeature instanceof CodonFeature) {
                continue;
            }

            Range subRange = subFeature.getOnlyGeometricAlignmentToOnlyAxis()
                                       .getRangeOnAxis();
            splicedPosition -= subRange.getMagnitude();

            if (splicedPosition <= 0) {
                return subRange.getMaximum() + splicedPosition;
            }
        }

        return range.getMaximum();
    }

    /**
     * Convert from a position on a genomic axis to a position on the spliced feature.
     * Return 0 if the given axisPosition is left of this feature,
     * and the maximum spliced location if the given axisPosition is right of the given feature.
     */
    public int transformAxisPositionToSplicedPosition(int axisPosition) {
        Range range = getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis();

        int splicedOverallSize = 0;
        int splicedPosition = 0;

        if (!(this instanceof SuperFeature)) {
            splicedOverallSize = range.getMagnitude();
            splicedPosition = axisPosition - range.getMinimum();
        } else {
            boolean alreadyFound = false;
            Iterator i = getOrderedFeatureSet(getSubFeatures(), true)
                             .iterator();

            while (i.hasNext()) {
                Feature subFeature = (Feature) i.next();

                if (subFeature instanceof CodonFeature) {
                    continue;
                }

                Range subRange = subFeature.getOnlyGeometricAlignmentToOnlyAxis()
                                           .getRangeOnAxis();
                splicedOverallSize += subRange.getMagnitude();

                if (axisPosition > subRange.getMaximum()) {
                    splicedPosition += subRange.getMagnitude();
                } else {
                    if (!alreadyFound) {
                        alreadyFound = true;

                        if (axisPosition > subRange.getMinimum()) {
                            splicedPosition += (axisPosition - subRange.getMinimum());
                        }
                    }
                }
            }
        }

        if (splicedPosition < 0) {
            splicedPosition = 0;
        } else if (splicedPosition > splicedOverallSize) {
            splicedPosition = splicedOverallSize;
        }

        return range.isReversed()
               ? (splicedOverallSize - splicedPosition) : splicedPosition;
    }

    /**
     * Return the spliced size for this feature.
     * If this feature is not a composite, return its magnitude.
     */
    public int determineSplicedSize() {
        Range range = getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis();

        if (!(this instanceof SuperFeature)) {
            return range.getMagnitude();
        }

        int splicedSize = 0;
        Iterator i = getOrderedFeatureSet(getSubFeatures(), true).iterator();

        while (i.hasNext()) {
            Feature subFeature = (Feature) i.next();

            if (subFeature instanceof CodonFeature) {
                continue;
            }

            Range subRange = subFeature.getOnlyGeometricAlignmentToOnlyAxis()
                                       .getRangeOnAxis();
            splicedSize += subRange.getMagnitude();
        }

        return splicedSize;
    }

    /**
     * Get the translated region for a sequence starting a startPosition.
     * @param sequence the spliced sequence
     * @param startPosition the start position.
     * @todo Port getSplicedResidues() client.model.proxy_interval.SequenceUtil
     */
    public String getTranslatedRegion(Axis theAxis, int startPos) {
        /*
        if (theAxis==null) return null;
        
        String seq = splice(theAxis, compositeFeature).toString();
        
        return getTranslatedRegion(seq, startPos);
        */
        return "";
    }

    /**
     * Get the translated region for a sequence starting a startPosition.
     * @param startPosition the start position.
     * @todo TEST getTranslatedRegion(int startPos)
     */
    public String getTranslatedRegion(int startPos) {
        String seq = DNA.toString(this.getSplicedResidues());

        return SequenceUtil.getSequenceUtil()
                           .getTranslatedRegion(seq, startPos);
    }

    /**
     * Get the translation for the spliced residues corresponding to a composite feature.
     * Assumes that the translation should start at the beginning of the feature
     */
    public String getTranslatedRegion() {
        return getTranslatedRegion(0);
    }

    /**
     * Return the discovery environment translated to its internal id.
     */
    public String getEnvironment() {
        if (discoveryEnvironment != null) {
            return discoveryEnvironment;
        }

        return (String) discoveryEnvironmentNumberToName.get(
                       new Short(discoveryEnvironmentNumber));
    }

    /**
     * Return the discovery environment.
     */
    public String getDiscoveryEnvironment() {
        return discoveryEnvironment;
    }

    /**
     * Let's provide a decent toString()...
     */
    public String toString() {
        // if (DEBUG_SCRATCH_STATES) return this.getFeatureType() + " Feature " + this.getOID();
        if (getEnvironment() == null) {
            return "No Environment:" + this.getOid();
        } else {
            return getEnvironment() + ":" + this.getOid();
        }
    }

    //****************************************
    //*  Protected methods
    //****************************************

    /**
     * Templete pattern
     * Sub-Classes should overide this to set the proper mutator
     */
    protected GenomicEntityMutator constructMyMutator() {
        return new FeatureMutator();
    }

    /**
     * A more specific getMutator();
     */
    protected FeatureMutator getFeatureMutator() {
        return (FeatureMutator) this.getMutator();
    }

    //== Observation / Notification Methods =========================================

    /**
     * Post a noteSubFeatureAdded onto the Queue.
     */
    protected void postSubFeatureAdded(Feature addedFeature) {
        new FeatureNotificationObject(this, addedFeature, NOTE_SUBFEATURE_ADDED, 
                                      true).run();


        // Set up a notifcation with this as the primary feature, and
        // the added sub feature as the secondary feature.
        getNotificationQueue()
            .addQueue(new FeatureNotificationObject(this, addedFeature, 
                                                    NOTE_SUBFEATURE_ADDED, 
                                                    false));
    }

    /**
     * Post a noteSubFeatureRemoved onto the Queue.
     */
    protected void postSubFeatureRemoved(Feature removedFeature, 
                                         Feature previousParentFeature) {
        new FeatureNotificationObject(previousParentFeature, removedFeature, 
                                      NOTE_SUBFEATURE_REMOVED, true).run();
        getNotificationQueue()
            .addQueue(new FeatureNotificationObject(previousParentFeature, 
                                                    removedFeature, 
                                                    NOTE_SUBFEATURE_REMOVED, 
                                                    false));
    }

    /**
     * Post a noteFeatureEvidenceAdded onto the Queue.
     */
    protected void postFeatureEvidenceAdded(Feature subjectFeature, 
                                            Feature addedEvidenceFeature) {
        new FeatureNotificationObject(subjectFeature, addedEvidenceFeature, 
                                      NOTE_EVIDENCE_ADDED, true).run();
        getNotificationQueue()
            .addQueue(new FeatureNotificationObject(subjectFeature, 
                                                    addedEvidenceFeature, 
                                                    NOTE_EVIDENCE_ADDED, false));
    }

    /**
     * Post a noteFeatureEvidenceRemoved onto the Queue.
     */
    protected void postFeatureEvidenceRemoved(Feature subjectFeature, 
                                              Feature removedEvidenceFeature) {
        new FeatureNotificationObject(subjectFeature, removedEvidenceFeature, 
                                      NOTE_EVIDENCE_REMOVED, true).run();
        getNotificationQueue()
            .addQueue(new FeatureNotificationObject(subjectFeature, 
                                                    removedEvidenceFeature, 
                                                    NOTE_EVIDENCE_REMOVED, 
                                                    false));
    }

    /**
     * Post a noteWorkspaceReplacementStateChanged onto the Queue.
     */
    protected void postWorkspaceReplacementStateChanged(Feature aFeature, 
                                                        ReplacementRelationship theRepRel) {
        new FeatureNotificationObject(aFeature, 
                                      NOTE_WORKSPACE_REPLACEMENT_STATE_CHANGED, 
                                      theRepRel, true).run();
        getNotificationQueue()
            .addQueue(new FeatureNotificationObject(aFeature, 
                                                    NOTE_WORKSPACE_REPLACEMENT_STATE_CHANGED, 
                                                    theRepRel, false));
    }

    /**
     * Post a notePromotedReplacementStateChanged onto the Queue.
     */
    protected void postPromotedReplacementStateChanged(Feature aFeature, 
                                                       ReplacementRelationship theRepRel) {
        new FeatureNotificationObject(aFeature, 
                                      NOTE_PROMOTED_REPLACEMENT_STATE_CHANGED, 
                                      theRepRel, true).run();
        getNotificationQueue()
            .addQueue(new FeatureNotificationObject(aFeature, 
                                                    NOTE_PROMOTED_REPLACEMENT_STATE_CHANGED, 
                                                    theRepRel, false));
    }

    protected GenomicEntityLoader getDataLoader() {
        try {
            return getLoaderManager().getFacade(this.getEntityType());
        } catch (Exception ex) {
            handleException(ex);

            return null;
        }
    }

    //== Visitation Methods =========================================

    /**
     * Accepts visitors that implement GenomicEntityVisitor. acceptVisitorForSelf
     * is specialized by every subclass of GenomicEntity to call the appropriate
     * "visit...(...)" function on the passed visitor.
     * @param theVisitor the visitor.
     */
    public void acceptVisitorForSelf(GenomicEntityVisitor theVisitor) {
        try {
            theVisitor.visitFeature(this);
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * Accepts visitors that implement GenomicEntityVisitor.
     * @param theVisitor the visitor.
     * @param directSubFeaturesOnly - if true will only walk to this features subFeatures
     *    if false, will walk to this features subFeaturesand any subFeatures of those features etc
     *    until no more features exist
     */
    public void acceptVisitorForSubFeatures(GenomicEntityVisitor theVisitor, 
                                            boolean directSubFeaturesOnly) {
        try {
            Feature[] subFeatures = getSubFeatureArray();
            Feature feature;

            for (int i = 0; i < subFeatures.length; i++) {
                feature = subFeatures[i];
                feature.acceptVisitorForSelf(theVisitor);

                if (!directSubFeaturesOnly) {
                    feature.acceptVisitorForSubFeatures(theVisitor, false);
                }
            }
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    /**
     * Accepts visitors that implement GenomicEntityVisitor.
     * @param theVisitor the visitor.
     * @param directSubStructureOnly - if true will only walk to this features sub-structure
     *    if false, will walk to this features sub sub-structure  and any sub-structure of those features etc
     *    until no more features exist
     */
    public void acceptVisitorForSubStructure(GenomicEntityVisitor theVisitor, 
                                             boolean directSubStructureOnly) {
        try {
            Feature[] subStructure = getSubStructureArray();
            Feature feature;

            for (int i = 0; i < subStructure.length; i++) {
                feature = subStructure[i];
                feature.acceptVisitorForSelf(theVisitor);

                if (!directSubStructureOnly) {
                    feature.acceptVisitorForSubFeatures(theVisitor, false);
                }
            }
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    //****************************************
    //*  Package methods
    //****************************************
    //****************************************
    //*  Private methods
    //****************************************

    /**
     * Return a "snap-shot" of the sub-feature collection as an array.
     */
    private Feature[] getSubFeatureArray() {
        if (subFeatures == null) {
            return new Feature[0];
        }

        Feature[] subFeatureArray;
        synchronized (this) {
            subFeatureArray = new Feature[subFeatures.size()];
            subFeatures.toArray(subFeatureArray);
        }

        return subFeatureArray;
    }

    /**
     * Return a "snap-shot" of the sub-feature collection as an array.
     */
    private Feature[] getSubStructureArray() {
        Collection subStructure = this.getSubStructure();

        if (subStructure == null) {
            return new Feature[0];
        }

        Feature[] subStructureArray;
        synchronized (this) {
            subStructureArray = new Feature[subStructure.size()];
            subStructure.toArray(subStructureArray);
        }

        return subStructureArray;
    }

    //****************************************
    //*  Inner classes
    //****************************************

    /**
     * The FeatureMutator class is the only way you can change the state of Feature
     * instances.
     * The FeatureMutator class is public, but it's constructor is private.
     */
    public class FeatureMutator extends AlignableGenomicEntityMutator {
        /**
         * Protected constructor for the mutator class...
         */
        protected FeatureMutator() {
        }

        /**
         * set discoveryEnvironment (i.e., Group Tag Name)
         *
         * @parameter p_discoveryEnvironment a String with the
         * discoveryEnvironment/group_tag_name for this object
         */
        public void setDiscoveryEnvironment(String p_discoveryEnvironment) {
            discoveryEnvironment = p_discoveryEnvironment;
        }

        /**
         * Set entire feature structure "Under Construction" flag.
         */
        public void setFeatureStructureUnderConstruction(boolean isUnderConstruction) {
            this.setUnderConstruction(isUnderConstruction);

            Collection subStructure = Feature.this.getSubStructure();

            for (Iterator itr = subStructure.iterator(); itr.hasNext();) {
                ((Feature) itr.next()).getFeatureMutator()
                    .setFeatureStructureUnderConstruction(isUnderConstruction);
            }
        }

        /**
         * Should be called to align an entity to this axis.
         * Should NOT be called if calling from AlignableEntity
         */
        public void addAlignmentToAxis(Alignment alignment)
                                throws AlignmentNotAllowedException {
            if (alignment == null) {
                return;
            }

            super.addAlignmentToAxis(alignment);

            Feature superFeature = Feature.this.getSuperFeature();

            if ((superFeature != null) && 
                    (!Feature.this.isUnderConstruction())) {
                superFeature.getFeatureMutator()
                            .adjustForAddedSubFeature(Feature.this);
            }
        }

        public void removeAlignmentToAxis(Alignment alignment) {
            if (alignment == null) {
                return;
            }

            super.removeAlignmentToAxis(alignment);

            if ((superFeature != null) && 
                    (!Feature.this.isUnderConstruction())) {
                Feature.this.superFeature.getFeatureMutator()
                         .adjustForRemovedSubFeature(Feature.this);
            }
        }

        /**
         * Change the Range of a GeometricAlignment for this AlignableGenomicEntity.
         * Will only change the range, if it is one of this AlignableGenomicEntity.
         * Sends a notification of Alignment changed to BOTH the observers of the
         * AlignableGenomicEntity and the observers of the Axis.
         * This method delegates to the same method on Axis.
         */
        public void changeRangeOnAlignment(GeometricAlignment geoAlignment, 
                                           Range newRange) {
            if ((geoAlignment == null) || (newRange == null)) {
                return;
            }

            super.changeRangeOnAlignment(geoAlignment, newRange);

            if ((superFeature != null) && 
                    (!Feature.this.isUnderConstruction())) {
                Feature.this.superFeature.getFeatureMutator()
                         .adjustForAddedSubFeature(Feature.this);
            }
        }

        /**
         * Set the super feature that contains this feature...
         * No one should be able to set the parent except the Composite on addChild
         * This should throw an IllegalFeatureContainmentException...
         */
        private void setSuperFeature(Feature newSuperFeature)
                              throws InvalidFeatureStructureException {
            // Check that the classes can play the roles of sub-feature and super-feature
            // and that the instances will accept the sub-feature super-feature relationship.
            if (!(Feature.this instanceof SubFeature) || 
                    ((newSuperFeature != null) && 
                        !(newSuperFeature instanceof SuperFeature)) || 
                    !Feature.this.willAcceptSuperFeature(newSuperFeature) || 
                    ((newSuperFeature != null) && 
                        !newSuperFeature.willAcceptSubFeature(Feature.this))) {
                String superFeatureName;

                if (newSuperFeature == null) {
                    superFeatureName = "NULL";
                } else {
                    superFeatureName = newSuperFeature.getClass().getName();
                }

                throw new InvalidFeatureStructureException(
                        "Can not make Feature(" + 
                        Feature.this.getClass().getName() + ":" + 
                        Feature.this + ") a Sub-Feature of Feature(" + 
                        superFeatureName + ":" + newSuperFeature + ").");
            }

            Feature.this.superFeature = newSuperFeature;
        }

        /**
         * Add a sub feature that this feature contains.
         * Order is not maintained or meaningful.
         * Sub-Features must be unique, sub features will not be added more than once.
         * Returns "true" if the subFeature was added, otherwise returns "false".
         * This should throw an IllegalFeatureContainmentException...
         */
        public void addSubFeature(Feature newSubFeature)
                           throws InvalidFeatureStructureException {
            // Check for null argument...
            if (newSubFeature == null) {
                return;
            }

            // Check that the classes can play the roles of sub-feature and super-feature
            // and that the instances will accept the sub-feature super-feature relationship.
            if (!(Feature.this instanceof SuperFeature) || 
                    !(newSubFeature instanceof SubFeature) || 
                    !Feature.this.willAcceptSubFeature(newSubFeature) || 
                    !newSubFeature.willAcceptSuperFeature(Feature.this)) {
                throw new InvalidFeatureStructureException(
                        "Can not make Feature(" + 
                        Feature.this.getClass().getName() + ":" + 
                        Feature.this + ") a Super-Feature of Feature(" + 
                        newSubFeature.getClass().getName() + ":" + 
                        newSubFeature + ").");
            }

            // Remove it from it's old super-feature (as long as it's not me...
            Feature oldSuperFeat = newSubFeature.getSuperFeature();

            if ((oldSuperFeat != null) && (oldSuperFeat != Feature.this)) {
                oldSuperFeat.getFeatureMutator()
                            .removeSubFeature(newSubFeature);
            }

            // Make sure that we have a non-null subFeatures collection...
            if (Feature.this.subFeatures == null) {
                Feature.this.subFeatures = new HashSet();
            }
            // if we already had one, make sure we don't already have this subfeature...
            else if (Feature.this.subFeatures.contains(newSubFeature)) {
                return;
            }


            // Add the new sub-feature to this Feature's sub-features...
            Feature.this.subFeatures.add(newSubFeature);

            ((FeatureMutator) newSubFeature.getMutator()).setSuperFeature(
                    Feature.this);


            // Give subclasses a change to react to adjust for SubFeatureAdded
            this.adjustForAddedSubFeature(newSubFeature);


            // Post notification...
            Feature.this.postSubFeatureAdded(newSubFeature);
        }

        /**
         * Adjust  for an added sub-feature.  This gives sub-classes, like Transcript,
         * to adjust for an added sub-feature.
         * This will be called by FeatureMutator.addSubFeature(Feature newSubFeature)
         * - After the newSubFeature has been added
         * - Before we postSubFeatureAdded(newSubFeature);
         */
        void adjustForAddedSubFeature(Feature newSubFeature) {
        }

        /**
         * Add a collection of sub features...
         * A notification will be posted for each subFeatureAdded.
         * If InvalidFeatureStructureException is thrown, some subFeatures MAY
         * have been added, and some will have NOT been added.
         * This should throw an IllegalFeatureContainmentException.
         */
        public void addAllSubFeatures(Collection newSubFeatures)
                               throws InvalidFeatureStructureException, 
                                      IllegalArgumentException {
            // Check for null argument...
            if (newSubFeatures == null) {
                return;
            }

            // Check that the classes can play the roles of sub-feature and super-feature
            // and that the instances will accept the sub-feature super-feature relationship.
            if (!(Feature.this instanceof SuperFeature)) {
                throw new InvalidFeatureStructureException(
                        "Can not make Feature(" + Feature.this + 
                        ") a Super-Feature.");
            }

            Object nextObject;

            for (Iterator itr = newSubFeatures.iterator(); itr.hasNext();) {
                nextObject = itr.next();

                if (nextObject instanceof Feature) {
                    this.addSubFeature((Feature) nextObject);
                } else {
                    throw new IllegalArgumentException(
                            "Objects in Collection must be instances of Feature.");
                }
            }
        }

        /**
         * Remove a sub feature that this feature contains.
         * Order is not maintained or meaningful.
         * The removed feature will NOT be un-aligned from the axis.
         * This method will automatically call updateAlignmentBasedOnSubFeatures();
         * if it successfully adds the sub-feature.
         */
        public void removeSubFeature(Feature oldSubFeature) {
            // Check for null argument...
            if (oldSubFeature == null) {
                return;
            }

            // Make Features can play thier respective roles...
            if (!(Feature.this instanceof SuperFeature)) {
                return;
            }

            if (!(oldSubFeature instanceof SubFeature)) {
                return;
            }

            // Make sure we have any sub-features...
            if (Feature.this.subFeatures == null) {
                return;
            }

            // Check to make sure it is a subFeature of ours, if not we don't
            // want to null out it's parent reference...
            if (!Feature.this.containsSubFeature(oldSubFeature)) {
                return;
            }


            // Remove the sub-feature from the subFeature collection...
            Feature.this.subFeatures.remove(oldSubFeature);

            // Set up the parent feature reference...
            // need to get the newChild's mutator...
            try {
                ((FeatureMutator) oldSubFeature.getMutator()).setSuperFeature(
                        null);
            } catch (InvalidFeatureStructureException e1) {
                // Willing to ignore this... we are just setting the super feature to null.
            }

            // Now check to see if the sub-feature collection is empty...
            if (Feature.this.subFeatures.isEmpty()) {
                Feature.this.subFeatures = null;
            }


            // Give subclasses a change to adjust for SubFeatureRemoved
            this.adjustForRemovedSubFeature(oldSubFeature);


            // Post notification...
            Feature.this.postSubFeatureRemoved(oldSubFeature, Feature.this);
        }

        /**
         * Adjust  for an added sub-feature.  This gives sub-classes, like Transcript,
         * to adjust for an added sub-feature.
         * This will be called by FeatureMutator.addSubFeature(Feature newSubFeature)
         * - After the newSubFeature has been added
         * - Before we postSubFeatureAdded(newSubFeature);
         */
        void adjustForRemovedSubFeature(Feature oldSubFeature) {
        }

        /**
         * Remove all sub features from collection that this feature contains.
         * Order is not maintained or meaningful.
         * This should throw an IllegalFeatureContainmentException...
         * @Todo need to make proper notifications...
         */
        public void removeAllSubFeatures(Collection someSubFeatures) {
            // Make sure we have sub-features...
            if (Feature.this.subFeatures == null) {
                return;
            }

            Object nextObject;

            if (someSubFeatures == null) {
                return;
            }

            for (Iterator itr = someSubFeatures.iterator(); itr.hasNext();) {
                nextObject = itr.next();

                if (nextObject instanceof Feature) {
                    this.removeSubFeature((Feature) nextObject);
                }
            }
        }

        /**
         * Clear all sub features this feature contains.
         * A notification will be posted for each sub-feature that is removed.
         */
        public void clearSubFeatures() {
            // Check that the classes can play the roles of sub-feature and super-feature
            // and that the instances will accept the sub-feature super-feature relationship.
            if (!(Feature.this instanceof SuperFeature)) {
                return;
            }

            // Make sure we have sub-features...
            if (Feature.this.subFeatures == null) {
                return;
            }
            synchronized (this) {
                Feature[] subFeatureArray = Feature.this.getSubFeatureArray();
                Feature.this.subFeatures = null;

                for (int i = 0; i < subFeatureArray.length; i++) {
                    try {
                        ((FeatureMutator) subFeatureArray[i].getMutator()).setSuperFeature(
                                null);
                    } catch (InvalidFeatureStructureException e1) {
                        // Willing to ignore this... we are just setting the super feature to null.
                    }
                }
            }
        }

        /**
         * Add the newEvidenceFeature to the evidence of this feature.
         * This method will not alow the same feature to be added more than once as evidence.
         * If the evidenceFeature is already evidence for this feature, nothing will be done.
         * If the evidence is changed, postEntityDetailsChanged() will be invoked.
         */
        public void addEvidence(Feature newEvidenceFeature) {
            // Add the new OID to this Feature's evidence...
            // Add the new OID onlly if it is not in Server GenerateName space
            // to prevent interactive blast features to be used as evidence for
            // workspace transcripts
            if (!(newEvidenceFeature.getOid().isServerGeneratedOID())) {
                this.addEvidenceOid(newEvidenceFeature.getOid());


                // Post the notification...
                Feature.this.postFeatureEvidenceAdded(Feature.this, 
                                                      newEvidenceFeature);
            }
        }

        /**
         * Add a "Oid" to the to the evidence of this feature.
         * This method does NOT post notifcation of evidence added.
         */
        public void addEvidenceOid(OID anOid) {
            // Get the evidence (forcing a load from Facade if null)...
            Collection theEvidenceOids = Feature.this.getEvidenceOidsFromFacade();

            // Make sure we don't already have this OID as evidence...
            if (theEvidenceOids.contains(anOid)) {
                return;
            }


            // Add the new OID to this Feature's evidence...
            theEvidenceOids.add(anOid);
        }

        /**
         * Add a collection of Oids as evidence of this Feature.
         * This method does NOT post notifcation of evidence added.
         */
        public void addAllEvidenceOids(Collection collectionOfOids) {
            Object evidenceOid;

            for (Iterator itr = collectionOfOids.iterator(); itr.hasNext();) {
                evidenceOid = itr.next();

                if (evidenceOid instanceof OID) {
                    this.addEvidenceOid((OID) evidenceOid);
                }
            }
        }

        /**
         * Remove the oldEvidenceFeature from the evidence of this feature.
         * If the evidenceFeature is NOT currently evidence for this feature, nothing will be done.
         * If the evidence is changed, postEntityDetailsChanged() will be invoked.
         */
        public void removeEvidence(Feature oldEvidenceFeature) {
            // Check for null argument...
            if (oldEvidenceFeature == null) {
                return;
            }

            // Get the evidence (forcing a load from Facade if null)...
            Collection theEvidenceOids = Feature.this.getEvidenceOidsFromFacade();

            // Make sure we don't already have this Feature as evidence...
            if (!theEvidenceOids.contains(oldEvidenceFeature.getOid())) {
                return;
            }


            // Remove the sub-feature from the subFeature collection...
            Feature.this.evidenceOids.remove(oldEvidenceFeature.getOid());


            // Post the notification...
            Feature.this.postFeatureEvidenceRemoved(Feature.this, 
                                                    oldEvidenceFeature);
        }

        /**
         * Update the root feature's alignment and all it's sub-features' alignments.
         * The method marches up through the "super feature" hierachy until we find the
         * root feature (that which has no super feature.
         * Once at the root feture.
         * Returns true if any changes have been made to any of the alignments.
         * Returns false if no changes have been made to any of the alignments.
         * Notification of alignment change will occur for each alignment changed.
         * This method is NOT optimized for minimal update calculation.
         */
        public boolean updateAllGeomAlignmentsFromRootFeatureBasedOnSubFeatures() {
            // Go up to the "root" feature...
            Feature rootFeature = Feature.this;

            while (rootFeature.getSuperFeature() != null)
                rootFeature = rootFeature.getSuperFeature();

            // Now update the root feature and all it's sub-features.
            return rootFeature.getFeatureMutator()
                              .updateAllSubFeatureGeomAlignmentsBasedOnSubFeatures();
        }

        /**
         * Update this features geometric alignments and all it's sub-features
         * (recursively) down the sub-feature hierarchy based on the sub-feature alignments.
         * 1. If this feature has sub-features, recursively call this method on each of them.
         * 2. Update this feature's alignment based on sub-features' alignments.
         * Returns if ANY changes have been made to any alignments for this feature
         * or below in the sub-feature hierarchy.
         * Notification of alignment change will occur for each alignment changed.
         * This method is NOT optimized for minimal update calculation.
         */
        public boolean updateAllSubFeatureGeomAlignmentsBasedOnSubFeatures() {
            // If I'm NOT a super feature return false;
            if (!(Feature.this instanceof SuperFeature)) {
                return false;
            }

            // Need to track if there was change...
            boolean subChanged = false;
            boolean subTotalChanged = false;
            Object aSubFeature = null;
            FeatureMutator subFeatureMutator = null;

            // Update the sub-features...
            if (Feature.this.subFeatures != null) {
                for (Iterator subFeatItr = Feature.this.subFeatures.iterator();
                     subFeatItr.hasNext();) {
                    aSubFeature = subFeatItr.next();

                    // Only call the update, if this sub-feature can have sub-features.
                    if (aSubFeature instanceof SuperFeature) {
                        subFeatureMutator = ((Feature) aSubFeature).getFeatureMutator();
                        subChanged = subFeatureMutator.updateAllSubFeatureGeomAlignmentsBasedOnSubFeatures();
                        subTotalChanged = subChanged || subTotalChanged;
                    }
                }
            }

            // Update this feature...
            boolean thisChanged = this.updateGeomAlignmentBasedOnSubFeatures();

            // Return if any have changed.
            return (subChanged || thisChanged);
        }

        /**
         * Update this features geometric alignments and all it's super-features
         * (recursively) up the super-feature hierarchy based on the sub-feature alignments.
         * 1. Update this feature's alignment based on sub-features' alignments.
         * 2. If this feature has a super-features, call this method on it.
         * Returns if ANY changes have been made to any alignments for this feature
         * or above it in the super-feature hierarchy.
         * Notification of alignment change will occur for each alignment changed.
         * This method is NOT optimized for minimal update calculation.
         */
        public boolean updateAllSuperFeatureGeomAlignmentsBasedOnSubFeatures() {
            // If I'm NOT a super feature return false;
            if (!(Feature.this instanceof SuperFeature)) {
                return false;
            }

            // Update this feature...
            boolean thisChanged = this.updateGeomAlignmentBasedOnSubFeatures();

            // Need to track if there was change...
            boolean superChanged = false;

            FeatureMutator superFeatureMutator = null;

            // Update the sub-features...
            if (Feature.this.superFeature != null) {
                superFeatureMutator = Feature.this.superFeature.getFeatureMutator();
                superChanged = superFeatureMutator.updateAllSubFeatureGeomAlignmentsBasedOnSubFeatures();
            }

            // Return if any have changed.
            return (superChanged || thisChanged);
        }

        /**
         * Update the geometric alignment of this feature based on the total
         * of it's sub-features' geometric aligments.
         */
        public boolean updateGeomAlignmentBasedOnSubFeatures() {
            // If I'm NOT a super feature return false;
            if (!(Feature.this instanceof SuperFeature)) {
                return false;
            }

            // So I can have sub-features... so calculate
            GeometricAlignment subFeatureTotalGeomAlign = 
                    Feature.this.getTotalGeomAlignmentOfSubFeatures();

            // Need my own alignment...
            GeometricAlignment myGeomAlign = Feature.this.getOnlyGeometricAlignmentToOnlyAxis();

            // If the total geom alignment is null... I should remove my own alignment.
            if (subFeatureTotalGeomAlign == null) {
                if (myGeomAlign != null) {
                    this.removeAlignmentToAxis(myGeomAlign);

                    return true;
                }

                // Didn't have one before, don't have one now.
                return false;
            }

            // If this feature didn't have an alignment before, align it with the new one.
            if (myGeomAlign == null) {
                try {
                    this.addAlignmentToAxis(
                            new MutableAlignment(
                                    subFeatureTotalGeomAlign.getAxis(), 
                                    Feature.this, 
                                    subFeatureTotalGeomAlign.getRangeOnAxis()));

                    return true;
                } catch (AlignmentNotAllowedException ex) {
                    System.out.println(
                            "Feature wouldn't accept the sub-feature calculated alignment.");

                    return false;
                }
            }

            // See if they are equals, don't change anything and return false;
            if (myGeomAlign.equals(subFeatureTotalGeomAlign)) {
                return false;
            }


            // Update my geometric alignment to be consistent with totalGeomAlign...
            this.changeRangeOnAlignment(myGeomAlign, 
                                        subFeatureTotalGeomAlign.getRangeOnAxis());


            // Update the properties...
            this.updatePropertiesBasedOnGeometricAlignment(myGeomAlign);

            return true;
        }

        /**
         * Update some of the properties based on a geometric alignment...
         * @todo: support for "partial alignments".
         */
        public void updatePropertiesBasedOnGeometricAlignment(GeometricAlignment theGeomAlignment) {
            GeometricAlignment onlyGeoAlignment = Feature.this.getOnlyGeometricAlignmentToOnlyAxis();

            if (onlyGeoAlignment == null) {
                return;
            }

            Range rangeOnAxis = onlyGeoAlignment.getRangeOnAxis();

            try {
                if (!rangeOnAxis.isReversed()) { //This looks wierd, but it is how Marian wants it --PED 12/15/00
                    setProperty(FeatureFacade.AXIS_BEGIN_PROP, 
                                String.valueOf(rangeOnAxis.getStart()));
                    setProperty(FeatureFacade.AXIS_END_PROP, 
                                String.valueOf(rangeOnAxis.getEnd()));
                }
                else {
                    setProperty(FeatureFacade.AXIS_BEGIN_PROP, 
                                String.valueOf(rangeOnAxis.getEnd()));
                    setProperty(FeatureFacade.AXIS_END_PROP, 
                                String.valueOf(rangeOnAxis.getStart()));
                }

                // Set the property for the entity orientation...
                if (rangeOnAxis.getMagnitude() == 0) {
                    setProperty(FeatureFacade.ENTITY_ORIENTATION_PROP, 
                                "Unknown");
                } else if (rangeOnAxis.isReversed()) {
                    setProperty(FeatureFacade.ENTITY_ORIENTATION_PROP, 
                                "Reverse");
                } else {
                    setProperty(FeatureFacade.ENTITY_ORIENTATION_PROP, 
                                "Forward");
                }


                // Set the property for the genomic axis (aligned to)...
                setProperty(FeatureFacade.GENOMIC_AXIS_ID_PROP, 
                            onlyGeoAlignment.getAxis().getOid().toString());
            } catch (Exception e1) {
            }
        }
    } // End of FeatureMutator inner class.

    // notification

    /**
     * The FeatureNotificationQueueObject inner class...
     */
    protected class FeatureNotificationObject
        extends AlignableEntityNotificationObject {
        private Feature subjectFeature;
        private Feature secondaryFeature;
        private ReplacementRelationship theRepRel;

        protected FeatureNotificationObject(Feature subjectFeature, 
                                            int notedAction, 
                                            boolean notifyModelSyncObservers) {
            super(notedAction, notifyModelSyncObservers);
            this.subjectFeature = subjectFeature;
        }

        protected FeatureNotificationObject(Feature subjectFeature, 
                                            int notedAction, 
                                            ReplacementRelationship theRepRel, 
                                            boolean notifyModelSyncObservers) {
            super(notedAction, notifyModelSyncObservers);
            this.subjectFeature = subjectFeature;
            this.theRepRel = theRepRel;
        }

        protected FeatureNotificationObject(Feature subjectFeature, 
                                            Feature secondaryFeature, 
                                            int notedAction, 
                                            boolean notifyModelSyncObservers) {
            this(subjectFeature, notedAction, notifyModelSyncObservers);
            this.secondaryFeature = secondaryFeature;
        }

        protected Class getObserverFilteringClass() {
            return FeatureObserver.class;
        }

        public void run() {
            switch (getNotedAction()) {
            case NOTE_SUBFEATURE_ADDED:
            {
                sendSubFeatureAddedMessage();

                break;
            }

            case NOTE_SUBFEATURE_REMOVED:
            {
                sendSubFeatureRemovedMessage();

                break;
            }

            case NOTE_EVIDENCE_ADDED:
            {
                sendFeatureEvidenceAddedMessage();

                break;
            }

            case NOTE_EVIDENCE_REMOVED:
            {
                sendFeatureEvidenceRemovedMessage();

                break;
            }

            case NOTE_WORKSPACE_REPLACEMENT_STATE_CHANGED:
            {
                sendWorkspaceReplacementStateChangedMessage();

                break;
            }

            case NOTE_PROMOTED_REPLACEMENT_STATE_CHANGED:
            {
                sendPromotedReplacementStateChangedMessage();

                break;
            }

            default:
                super.run();
            }
        }

        private void sendSubFeatureAddedMessage() {
            FeatureObserver observer;
            List observers = getObserversToNotifyAsList();

            for (int i = 0; i < observers.size(); i++) {
                observer = (FeatureObserver) observers.get(i);
                observer.noteSubFeatureAdded(subjectFeature, secondaryFeature);
            }
        }

        private void sendSubFeatureRemovedMessage() {
            FeatureObserver observer;
            List observers = getObserversToNotifyAsList();

            for (int i = 0; i < observers.size(); i++) {
                observer = (FeatureObserver) observers.get(i);
                observer.noteSubFeatureRemoved(subjectFeature, secondaryFeature);
            }
        }

        private void sendFeatureEvidenceAddedMessage() {
            FeatureObserver observer;
            List observers = getObserversToNotifyAsList();

            for (int i = 0; i < observers.size(); i++) {
                observer = (FeatureObserver) observers.get(i);
                observer.noteFeatureEvidenceAdded(subjectFeature, 
                                                  secondaryFeature);
            }
        }

        private void sendFeatureEvidenceRemovedMessage() {
            FeatureObserver observer;
            List observers = getObserversToNotifyAsList();

            for (int i = 0; i < observers.size(); i++) {
                observer = (FeatureObserver) observers.get(i);
                observer.noteFeatureEvidenceRemoved(subjectFeature, 
                                                    secondaryFeature);
            }
        }

        private void sendWorkspaceReplacementStateChangedMessage() {
            FeatureObserver observer;
            List observers = getObserversToNotifyAsList();

            for (int i = 0; i < observers.size(); i++) {
                observer = (FeatureObserver) observers.get(i);
                observer.noteWorkspaceReplacementStateChanged(subjectFeature, 
                                                              theRepRel);
            }
        }

        private void sendPromotedReplacementStateChangedMessage() {
            FeatureObserver observer;
            List observers = getObserversToNotifyAsList();

            /*
            System.out.println(">>>There are " + observers.size() +
                               " observers of promoted feature with OID:" +
                               subjectFeature.getOid() +
                               " new replacement state = " + theRepRel.getReplacementType());
            */
            for (int i = 0; i < observers.size(); i++) {
                observer = (FeatureObserver) observers.get(i);
                observer.notePromotedReplacementStateChanged(subjectFeature, 
                                                             theRepRel);
            }
        }
    } // End of FeatureNotificationQueueObject inner class.
}