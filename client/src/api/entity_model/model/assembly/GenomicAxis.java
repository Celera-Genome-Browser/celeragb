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
package api.entity_model.model.assembly;

import api.entity_model.access.filter.AlignmentCollectionFilter;
import api.entity_model.access.filter.FiltrationDevice;
import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.management.ModelMgr;
import api.entity_model.management.StandardEntityFactory;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.alignment.AlignmentNotAllowedException;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.CodonFeature;
import api.entity_model.model.annotation.ComputedCodon;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.annotation.SpliceSite;
import api.entity_model.model.fundtype.*;
import api.entity_model.model.genetics.Chromosome;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.facade.abstract_facade.fundtype.GenomicEntityLoader;
import api.facade.concrete_facade.ejb.AbstractEJBFacadeManager;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;
import api.stub.ejb.model.fundtype.AxisRemote;
import api.stub.ejb.model.genomic_axis.GenomicAxisHome;
import api.stub.ejb.model.genomic_axis.GenomicAxisRemote;
import api.stub.geometry.Range;
import api.stub.sequence.DNA;
import api.stub.sequence.Sequence;
import api.stub.sequence.SubSequence;
import shared.tools.computation.StatisticalModel;

import javax.naming.Context;
import java.util.*;
/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version $Id$
 */



public class GenomicAxis extends Axis {

  private int order;
  private List defaultLoadFilters;
  static private EntityTypeSet transientFeatureTypes=
    EntityTypeSet.getEntityTypeSet("TransientFeatureTypes");
  static private EntityTypeSet transientSpliceTypes=
    EntityTypeSet.getEntityTypeSet("LocallyComputedTransientSpliceFeatureTypes");
  static private EntityTypeSet transientCodonTypes=
    EntityTypeSet.getEntityTypeSet("LocallyComputedTransientCodonFeatureTypes");


//****************************************
//*  Public methods
//****************************************

//*  Construction

  public GenomicAxis( OID oid, String displayName)
  {
    this(oid, displayName, 0, 0);
  }

  public GenomicAxis(OID oid, String displayName, int magnitude)
  {
    this(oid, displayName, magnitude, 0, null);
  }

  public GenomicAxis(OID oid, String displayName, int magnitude, int order)
  {
    this(oid, displayName, magnitude, order, null);
  }

  public GenomicAxis(OID oid, String displayName, int magnitude, FacadeManagerBase readFacadeManager)
  {
    this(oid, displayName, magnitude, 0, readFacadeManager);
  }

  public GenomicAxis(OID oid, String displayName, int magnitude, int order, FacadeManagerBase readFacadeManager)
  {
    super(EntityType.getEntityTypeForValue(EntityTypeConstants.Genomic_Axis), oid, displayName, magnitude, readFacadeManager);
    this.order=order;
  }

  /**
   * Get the Axis p to q order
   */
  public int getOrder() {
    return this.order;
  }


    /**
     * Accepts visitors that implement GenomicEntityVisitor. acceptVisitorForSelf
     * is specialized by every subclass of GenomicEntity to call the appropriate
     * "visit...(...)" function on the passed visitor.
     * @param theVisitor the visitor.
     */
    public void acceptVisitorForSelf(GenomicEntityVisitor theVisitor) {
        try {
                theVisitor.visitGenomicAxis(this);
        }
        catch (Exception ex) {
                handleException(ex);
        }
    }

    public Set getDefaultLoadFilters() {
       return new HashSet(getMyDefaultLoadFilters());
    }

    public LoadFilter getHighPriPreComputeLoadFilter() {
       return (LoadFilter)getMyDefaultLoadFilters().get(0);
    }

    public LoadFilter getLowPriPreComputeLoadFilter() {
       return (LoadFilter)getMyDefaultLoadFilters().get(1);
    }

    public LoadFilter getContigLoadFilter() {
       return (LoadFilter)getMyDefaultLoadFilters().get(2);
    }

    public LoadFilter getCurationLoadFilter() {
       return (LoadFilter)getMyDefaultLoadFilters().get(3);
    }

    public LoadFilter getLocallyComputedSpliceSiteLoadFilter(boolean forward) {
      return forward ?
        (LoadFilter)getMyDefaultLoadFilters().get(4) :
        (LoadFilter)getMyDefaultLoadFilters().get(7);
    }

    public LoadFilter getLocallyComputedStartCodonLoadFilter(boolean forward) {
      return forward ?
        (LoadFilter)getMyDefaultLoadFilters().get(5) :
        (LoadFilter)getMyDefaultLoadFilters().get(8);
    }

    public LoadFilter getLocallyComputedStopCodonLoadFilter(boolean forward) {
      return forward ?
        (LoadFilter)getMyDefaultLoadFilters().get(6) :
        (LoadFilter)getMyDefaultLoadFilters().get(9);
    }


    /**
     * This method computes if a splice exists in a certain location and then returns
     * true so.
     */
    public boolean spliceExistsAtEdge(Range testRange, Map statisticalModels) {
      if (calculateSplice(testRange, statisticalModels)) return true;
      return false;
    }

    /**
     * This method computes if a splice exists in a certain location and then returns
     * true so.  Uses the simplistic method, with no statistical models.
     */
    public boolean spliceExistsAtEdge(Range testRange, boolean calculateDonor) {
      if (calculateSplice(testRange, calculateDonor)) return true;
      return false;
    }

    /**
     * This method is used to determine if the range passed in (a feature's edge)
     * is actually a location where a splice site would be found.
     *
     * This calculation is simplified by its use of the dogmatic "GT..AG" rule.
     *
     */
    private boolean calculateSplice(Range testRange, boolean calculateDonor) {
        boolean isReverse = !testRange.isForwardOrientation();
        long location = testRange.getStart() + ((calculateDonor == isReverse) ? -2 : 0);
        Sequence seq = new SubSequence(getDNASequence(), location, 2);
        if (isReverse)
            seq = DNA.reverseComplement(seq);
        return DNA.isEqual(seq, calculateDonor ? "GT" : "AG");
    }

    /**
     * This method is used to determine if the range passed in (a feature's edge)
     * is actually a location where a splice site would be found.
     *
     * The calculation here and the calculation in the computeSpliceAlignments method should
     * be the same and need to be distilled out into a common method.
     */
    private boolean calculateSplice(Range testRange, Map statisticalModels) {
        StatisticalModel donorSpliceModel = (StatisticalModel)statisticalModels.get(StatisticalModel.DONOR);
        if (donorSpliceModel == null) {
            System.err.println("Error: Map statisticalModels does not contain DONOR in GenomicAxis.computeSpliceAlignments()");
            return false;
        }

        StatisticalModel acceptorSpliceModel = (StatisticalModel)statisticalModels.get(StatisticalModel.ACCEPTOR);
        if (acceptorSpliceModel == null) {
            System.err.println("Error: Map statisticalModels does not contain ACCEPTOR in GenomicAxis.computeSpliceAlignments()");
            return false;
        }

        StatisticalModel neitherSpliceModel = (StatisticalModel)statisticalModels.get(StatisticalModel.NEITHER);
        if (neitherSpliceModel == null) {
            System.err.println("Error: Map statisticalModels does not contain NEITHER in GenomicAxis.computeSpliceAlignments()");
            return false;
        }

        int windowSize = neitherSpliceModel.getWindowSize(); //assume all models have the same window size
        boolean isForward = testRange.isForwardOrientation();

        // Need to get the sequence for each.
        // The offset of "2" in the donor calculation is because AG GT are the splice bases looked for
        // and when they are found we need to shift the range by 2 nucleotides.
        // The splice is a feature in a pos. with 0 magnitude
        // while the calculation of splice location uses nucleotides to determine where the
        // splice should be.  Loading features into the view with the computeSpliceAlignment
        // method and then selecting those splices will show if this method's determination is correct.
        Range acceptorRange, donorRange;
        if (isForward) {
            acceptorRange = new Range(testRange.getStart() - acceptorSpliceModel.getExonOffset(),
                                      windowSize,
                                      testRange.getOrientation());
            donorRange = new Range(testRange.getEnd() - donorSpliceModel.getExonOffset() - 2,
                                   windowSize,
                                   testRange.getOrientation());
        }
        else {
            acceptorRange = new Range(testRange.getStart() + acceptorSpliceModel.getExonOffset(),
                                      windowSize,
                                      testRange.getOrientation());
            donorRange = new Range(testRange.getEnd() + donorSpliceModel.getExonOffset() + 2,
                                   windowSize,
                                   testRange.getOrientation());
        }

        Sequence acceptorSequence = getNucleotideSeq(acceptorRange);
        Sequence donorSequence = getNucleotideSeq(donorRange);
        if (!isForward) {
            acceptorSequence = DNA.reverseComplement(acceptorSequence);
            donorSequence = DNA.reverseComplement(donorSequence);
        }

        if (spliceExistsInWindow(acceptorSequence, isForward, statisticalModels, true))
            return true;
        if (spliceExistsInWindow(donorSequence, isForward, statisticalModels, false))
            return true;
        return false;
    }

    private boolean spliceExistsInWindow(Sequence window,
                                         boolean calculateForwardStrand,
                                         Map statisticalModels,
                                         boolean lookingForAcceptor)
    {
        StatisticalModel donorSpliceModel = (StatisticalModel)statisticalModels.get(StatisticalModel.DONOR);
        StatisticalModel acceptorSpliceModel = (StatisticalModel)statisticalModels.get(StatisticalModel.ACCEPTOR);
        StatisticalModel neitherSpliceModel = (StatisticalModel)statisticalModels.get(StatisticalModel.NEITHER);

        float windowDonorPrior = donorSpliceModel.computeProbability(window);
        float windowAcceptorPrior = acceptorSpliceModel.computeProbability(window);
        float windowNeitherPrior = neitherSpliceModel.computeProbability(window);
        float donorModelPrior = donorSpliceModel.getModelPriorProb();
        float acceptorModelPrior = acceptorSpliceModel.getModelPriorProb();
        float neitherModelPrior = neitherSpliceModel.getModelPriorProb();

        float denom = windowDonorPrior * donorModelPrior +
                      windowAcceptorPrior * acceptorModelPrior +
                      windowNeitherPrior * neitherModelPrior;
        float donorPosteriorProb = (windowDonorPrior * donorModelPrior) / denom;
        float acceptorPosteriorProb = (windowAcceptorPrior * acceptorModelPrior) / denom;
        float neitherPosteriorProb = (windowNeitherPrior * neitherModelPrior) / denom;

        if (donorPosteriorProb > acceptorPosteriorProb && donorPosteriorProb >= neitherPosteriorProb)
            return !lookingForAcceptor;
        if (acceptorPosteriorProb > donorPosteriorProb && acceptorPosteriorProb >= neitherPosteriorProb)
            return lookingForAcceptor;
        return false;
    }



//****************************************
//*  Protected methods
//****************************************


    protected int getPredictedNumberOfAlignmentsToEntities() {
      return getPredictedNumberOfAlignmentsToEntities(new Range(0, this.getMagnitude()));
    }

    protected int getPredictedNumberOfAlignmentsToEntities(Range rangeOfInterest) {
      return 5000;
    }

    protected void willAcceptAlignmentToAxis(Alignment alignmentToAxis)
      throws AlignmentNotAllowedException{

       super.willAcceptAlignmentToAxis (alignmentToAxis);
       if (!(alignmentToAxis.getAxis() instanceof Chromosome)) throw
         new AlignmentNotAllowedException("GenomicAxis can only be aligned to a Chromosome axis",
         alignmentToAxis);
    }

    protected void willAcceptAlignmentToEntity(Alignment alignmentToEntity)
      throws AlignmentNotAllowedException{

       super.willAcceptAlignmentToEntity(alignmentToEntity);
       if (!(alignmentToEntity.getEntity() instanceof Feature ||
           alignmentToEntity.getEntity() instanceof Contig)) throw
         new AlignmentNotAllowedException("GenomicAxis can only be accept Features and Contigs as"+
           " aligned Entities",alignmentToEntity);
       if (alignmentToEntity instanceof GeometricAlignment) {
         GeometricAlignment geometricAlignment = (GeometricAlignment)alignmentToEntity;
         Range fullAxisRange = new Range(0, this.getMagnitude());
         if (! fullAxisRange.contains(geometricAlignment.getRangeOnAxis()))
           throw new AlignmentNotAllowedException("GenomicAxis can only accept alignments whose ranges "+
             "are fully contained within the GenomicAxis' range", alignmentToEntity);
       }
    }

    protected boolean willAcceptLoadRequestForAlignedEntities(LoadRequest loadRequest){
       if (!loadRequest.isRangeRequest() || !(loadRequest.getLoadFilter().getLoadFilterStatus()
            instanceof RangeLoadFilterStatus)) return false;
       return true;
    }

    protected GenomicEntityLoader getDataLoader() {
      try {
       return getLoaderManager().getGenomicAxis();
      }
      catch (Exception ex) {
        handleException(ex);
        return null;
      }
    }

    protected AxisRemote getEJBDataLoader() {
      AbstractEJBFacadeManager baseFacadeManager
        = (AbstractEJBFacadeManager)FacadeManager.getFacadeManager(FacadeManager.getEJBProtocolString());
      Context ejbContext = baseFacadeManager.getInitialContext();

      try {
        GenomicAxisHome axisHome = (GenomicAxisHome)ejbContext.lookup("GenomicAxisJNDI");
        GenomicAxisRemote axisRemote = axisHome.create();
        return axisRemote;
      }
      catch (Exception ex) {
        handleException(ex);
        return null;
      }
    }

    protected Alignment[] addLocallyComputedAlignmentsToEntities(LoadRequest loadRequest) {
       Set entityTypes=loadRequest.getLoadFilter().getEntityTypeSet();
       Set tmpTransientFeatureTypes=new HashSet(transientFeatureTypes);
       tmpTransientFeatureTypes.retainAll(entityTypes);
       if (tmpTransientFeatureTypes.size()>0) {
         Set tmpTransientCodonTypes=new HashSet(transientCodonTypes);
         tmpTransientCodonTypes.retainAll(entityTypes);
         Alignment[] codonAlignments=new Alignment[0];
         Alignment[] spliceAlignments=new Alignment[0];
         if (tmpTransientCodonTypes.size()>0) {
           codonAlignments=computeCodonAlignments(tmpTransientCodonTypes,
                loadRequest.getRequestedRanges() );
         }
         Set tmpTransientSpliceTypes=new HashSet(transientSpliceTypes);
         tmpTransientSpliceTypes.retainAll(entityTypes);
         if (tmpTransientSpliceTypes.size()>0) {
           spliceAlignments=computeSpliceAlignments(
              getGenomeVersion().getSpecies().getSpliceTypeStatisticalModels(),
              tmpTransientSpliceTypes, loadRequest.getRequestedRanges() );
         }
         Alignment[] rtnAlignments=new Alignment[
           codonAlignments.length+spliceAlignments.length];
         System.arraycopy(codonAlignments,0,rtnAlignments,0,codonAlignments.length);
         System.arraycopy(spliceAlignments,0,rtnAlignments,
          codonAlignments.length,spliceAlignments.length);
         return rtnAlignments;
       }
       else return new Alignment[0];
    }

    /**
     * Adds the ability for subClasses of the axis to further filter out
     * which alignments should be unloaded.
     */
    protected Collection removeAlignmentsThatShouldNotBeUnloaded(
      LoadRequest loadRequest, Collection alignmentsForUnloading, boolean deleteWorkSpaceEntities) {

      alignmentsForUnloading=super.removeAlignmentsThatShouldNotBeUnloaded(loadRequest, alignmentsForUnloading,deleteWorkSpaceEntities);
      if (loadRequest.getLoadFilter() instanceof GenomicAxisLoadFilter) {
        GenomicAxisLoadFilter loadFilter=(GenomicAxisLoadFilter)loadRequest.getLoadFilter();
        if (loadFilter.isFilteringOnDisplayPriority()) {
          AlignmentCollectionFilter filter=AlignmentCollectionFilter.
            createAlignmentCollectionFilter(loadFilter.getFeatureDisplayPriority(),true);
          alignmentsForUnloading=FiltrationDevice.getDevice().executeAlignmentFilter(alignmentsForUnloading,filter);
          return alignmentsForUnloading;
        }
        else return alignmentsForUnloading;
      }
      else return alignmentsForUnloading;
    }

//****************************************
//*  Package methods
//****************************************

//****************************************
//*  Private methods
//****************************************

    /**
     * Compute Splice Sites
     *
     * @param statisticalModels - Map - keys- StatisticalModel.Type constants
     *                                  values - StatisticalModels
     * @param entityTypes - Set - contains _ACCEPTOR_SPLICE_SITE,
     *                                  _DONOR_SPLICE_SITE or both
     * @param requestedRanges - Set - set of requested Ranges
     *
     * @return an array of alignments.  The alignment will have a reference
     *  to the entity, but it's axis reference will be null (constructed by
     *  passing null).  addAlignmentToXXX will not have been called on either
     *  the axis or the entity.  This is the same format as if they are returned
     *  from the server.
     *
     * The calculation here and the calculation in the calculateSplice method should
     * be the same and need to be distilled out into a common method.
     *
     */
    private Alignment[] computeSpliceAlignments(
              Map statisticalModels,
              Set entityTypes,
              Set requestedRanges) {

        Range range;                      // MasterAxisSelectedRange(s)
        Sequence dnaSequence;          // DNA sequence of selected range
        Sequence sequenceOfRange;           // Sequence of range as a String
        boolean calculateForwardStrand;   // true when forward
        List retAlignedSpliceSites = new ArrayList();   // Eventually the returned array of GeometricAlignments
                                                        // (but not yet added to axis -- see javadoc comments)
        StandardEntityFactory standardEntityFactory = (StandardEntityFactory)(ModelMgr.getModelMgr()).getEntityFactory();
        EntityType acceptorEntityType = EntityType.getEntityTypeForName("_ACCEPTOR_SPLICE_SITE");
        EntityType donorEntityType = EntityType.getEntityTypeForName("_DONOR_SPLICE_SITE");
        SpliceSite spliceSiteEntity = null;
        StatisticalModel donorSpliceModel = null;
        StatisticalModel acceptorSpliceModel = null;
        StatisticalModel neitherSpliceModel = null;

        // Get StatisticalModels:
        if (statisticalModels.isEmpty()) {
            System.err.println("Error: Map statisticalModels is empty in GenomicAxis.computeSpliceAlignments()");
            return new Alignment[0];
        }
        if (null == (donorSpliceModel = (StatisticalModel)statisticalModels.get(StatisticalModel.DONOR))) {
            System.err.println("Error: Map statisticalModels does not contain DONOR in GenomicAxis.computeSpliceAlignments()");
            return new Alignment[0];
        }
        if (null == (acceptorSpliceModel = (StatisticalModel)statisticalModels.get(StatisticalModel.ACCEPTOR))) {
            System.err.println("Error: Map statisticalModels does not contain ACCEPTOR in GenomicAxis.computeSpliceAlignments()");
            return new Alignment[0];
        }
        if (null == (neitherSpliceModel = (StatisticalModel)statisticalModels.get(StatisticalModel.NEITHER))) {
            System.err.println("Error: Map statisticalModels does not contain NEITHER in GenomicAxis.computeSpliceAlignments()");
            return new Alignment[0];
        }

        for (Iterator rangeIterator=requestedRanges.iterator(); rangeIterator.hasNext(); ) {
            range = (Range)rangeIterator.next();
            if (range == null) {
                System.err.println("Error: range " + range + " null in GenomicAxis.computeSpliceAlignments()");
                continue;
            }
            dnaSequence = getNucleotideSeq(range);
            if (dnaSequence == null) {
                System.err.println("Error: dnaSequence of range " + range + " null in GenomicAxis.computeSpliceAlignments()");
                continue;
            }
            // Check orientation of range:
            if (range.isForwardOrientation()) {   // Forward
                calculateForwardStrand = true;
                sequenceOfRange = dnaSequence;
            }
            else {                                // Reverse
                calculateForwardStrand = false;
                // Note: range was reversed in AnnotationMenu.java to signal the
                // orientation desired for the calculations.
                // Use reverse complemented strand
                sequenceOfRange = DNA.reverseComplement(dnaSequence);
            }

            // See note at bottom of this for loop (about this for loop).
            for (Iterator entityTypeIterator=entityTypes.iterator(); entityTypeIterator.hasNext(); ) {
                // Move window over the consensus
                int windowSize = neitherSpliceModel.getWindowSize();  // Assume all models have the same window size
                float windowDonorPrior, windowAcceptorPrior, windowNeitherPrior;
                float donorModelPrior, acceptorModelPrior, neitherModelPrior;
                float denom, donorPosteriorProb, acceptorPosteriorProb, neitherPosteriorProb;
                long donorLoc, acceptorLoc, axisLoc;
                Range ssAxisRange;

                // Calculate the "window" as it traverses the sequence:
                for (int i = 0; i <= (sequenceOfRange.length() - windowSize); i++) {
                    // Fill the window:
                    Sequence window = new SubSequence(sequenceOfRange, i, windowSize);

                    // Compute prior for each model
                    windowDonorPrior = donorSpliceModel.computeProbability(window);
                    windowAcceptorPrior = acceptorSpliceModel.computeProbability(window);
                    windowNeitherPrior = neitherSpliceModel.computeProbability(window);
                    donorModelPrior = donorSpliceModel.getModelPriorProb();
                    acceptorModelPrior = acceptorSpliceModel.getModelPriorProb();
                    neitherModelPrior = neitherSpliceModel.getModelPriorProb();
                    // Compute posterior prob for each model
                    // Note: if the models have different priors then use ??
                    denom = (windowDonorPrior * donorModelPrior) + (windowAcceptorPrior * acceptorModelPrior) +
                        (windowNeitherPrior * neitherModelPrior);
                    donorPosteriorProb = (windowDonorPrior * donorModelPrior) / denom;
                    acceptorPosteriorProb = (windowAcceptorPrior * acceptorModelPrior) / denom;
                    neitherPosteriorProb = (windowNeitherPrior * neitherModelPrior) / denom;

                    // _DONOR_SPLICE_SITE found:
                    if ((donorPosteriorProb > acceptorPosteriorProb) && (donorPosteriorProb >= neitherPosteriorProb)) {
                        if (calculateForwardStrand) {
                            donorLoc = i + donorSpliceModel.getExonOffset();
                        }
                        else {
                            donorLoc = sequenceOfRange.length() - i - donorSpliceModel.getExonOffset();
                        }

                        // Create entity:
                        spliceSiteEntity = (SpliceSite)standardEntityFactory.create(
                            OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.API_GENERATED_NAMESPACE,this.getGenomeVersion().hashCode()),
                            donorEntityType.toString(),
                            donorEntityType,
                            "Splice Donor");
                        if (spliceSiteEntity == null) {
                            System.err.println("Error: StandardEntityFactory returned null in GenomicAxis.computeSpliceAlignments()");
                            continue;
                        }
                        spliceSiteEntity.setScore(donorPosteriorProb);

                        // Create alignment:
                        if (calculateForwardStrand) {
                            axisLoc = range.getMinimum() + donorLoc;
                            ssAxisRange = new Range((int)axisLoc+1, 0, range.getOrientation());
                            //spliceSiteEntity.setPosition(axisLoc);  // Disabled for now
                        }
                        else {
                            axisLoc = range.getMinimum() + donorLoc;
                            // Make range.start larger than range.end to load on reverse axis.
                            ssAxisRange = new Range((int)axisLoc-1, 0, range.getOrientation());
                        }
                        // Pass null for axis per specs in comments to this method
                        retAlignedSpliceSites.add(new GeometricAlignment(null, spliceSiteEntity, ssAxisRange));
                    }  // end if ((donorPosteriorProb > acceptorPosteriorProb) && ...)
                    // _ACCEPTOR_SPLICE_SITE found:
                    else if ((acceptorPosteriorProb > donorPosteriorProb) && (acceptorPosteriorProb >= neitherPosteriorProb)) {
                        if (calculateForwardStrand) {
                            acceptorLoc = i + acceptorSpliceModel.getExonOffset();
                        }
                        else {
                            acceptorLoc = sequenceOfRange.length() - i - acceptorSpliceModel.getExonOffset();
                        }

                        // Create entity:
                        spliceSiteEntity = (SpliceSite)standardEntityFactory.create(
                            OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.API_GENERATED_NAMESPACE,this.getGenomeVersion().hashCode()),
                            acceptorEntityType.toString(),
                            acceptorEntityType,
                            "Splice Acceptor");
                        if (spliceSiteEntity == null) {
                            System.err.println("Error: StandardEntityFactory returned null in GenomicAxis.computeSpliceAlignments()");
                            continue;
                        }
                        spliceSiteEntity.setScore(acceptorPosteriorProb);

                        // Create alignment:
                        if (calculateForwardStrand) {
                            axisLoc = range.getMinimum() + acceptorLoc;
                            ssAxisRange = new Range((int)axisLoc, 0, range.getOrientation());
                        }
                        else {
                            axisLoc = range.getMinimum() + acceptorLoc;
                            // Make range.start larger than range.end to load on reverse axis.
                            ssAxisRange = new Range((int)axisLoc, 0, range.getOrientation());
                        }
                        // Pass null for axis per specs in comments to this method
                        retAlignedSpliceSites.add(new GeometricAlignment(null, spliceSiteEntity, ssAxisRange));
                    }  // end else if ((acceptorPosteriorProb > donorPosteriorProb) && ...)
                }  // end for (int i = 0; i <= (sequenceOfRange.length() - windowSize); i++)

                // Break here because both of the two splice site entity types
                // (_ACCEPTOR_SPLICE_SITE,_DONOR_SPLICE_SITE) are actually
                // determined by one single pass through the range.
                //
                /** @todo  Need to test for number of entities. If plural, check for both. If only
                 *  acceptor or donor, just calculate for that. */
                break;
            }  // end for (Iterator entityTypeIterator=entityTypes.iterator();entityTypeIterator.hasNext();)
        }  // end for (Iterator rangeIterator=requestedRanges.iterator();rangeIterator.hasNext();)

        Alignment[] retAligned = new Alignment[0];
        if (!retAlignedSpliceSites.isEmpty()) {
            retAligned = (Alignment[])retAlignedSpliceSites.toArray(retAligned);
        }
        return retAligned;
    }  // end private Alignment[] computeSpliceAlignments()

    /**
     * Compute Codons
     *
     * @param entityTypes - Set - contains _START_CODON,
     *                                  _STOP_CODON or both
     * @param requestedRanges - Set - set of requested Ranges
     *
     * @return an array of alignments.  The alignment will have a reference
     *  to the entity, but it's axis reference will be null (constructed by
     *  passing null).  addAlignmentToXXX will not have been called on either
     *  the axis or the entity.  This is the same format as if they are returned
     *  from the server.
     */
     private Alignment[] computeCodonAlignments(
              Set entityTypes,
              Set requestedRanges) {

       StandardEntityFactory standardEntityFactory = (StandardEntityFactory)(ModelMgr.getModelMgr()).getEntityFactory();
       EntityType startCodonEntityType = EntityType.getEntityTypeForName("_COMPUTED_START_CODON");
       EntityType stopCodonEntityType = EntityType.getEntityTypeForName("_COMPUTED_STOP_CODON");
       EntityType codonToComputeEntityType;
       Range computeRange;
       String sequence;
       String[] searchStrings;
       Sequence dnaSequence;
       long seqLen;
       String codonSeq;
       boolean calculateForwardStrand;
       ComputedCodon codonEntity = null;
       List retAlignedCodons = new ArrayList();

       for (Iterator rangeIterator=requestedRanges.iterator();rangeIterator.hasNext();){
         computeRange = (Range) rangeIterator.next();

         calculateForwardStrand = computeRange.isForwardOrientation();

//         if (!calculateForwardStrand) {
//           computeRange = new Range(computeRange.getStart(), computeRange.getEnd());
//         }

         dnaSequence = getNucleotideSeq(computeRange);

         if (dnaSequence == null) {
           System.out.println("Error: could not get sequence for axis=" + this + " range=" + computeRange);
           continue;
         }

         if (!calculateForwardStrand)
           dnaSequence = DNA.reverseComplement(dnaSequence);

         sequence = DNA.toString(dnaSequence);

         for (Iterator entityTypeIterator=entityTypes.iterator();entityTypeIterator.hasNext();) {
           codonToComputeEntityType = (EntityType)entityTypeIterator.next();
           if (codonToComputeEntityType.equals(startCodonEntityType)) {
               searchStrings = CodonFeature.forwardStartCodonStrings;
           }
           else if (codonToComputeEntityType.equals(stopCodonEntityType)) {
               searchStrings = CodonFeature.forwardStopCodonStrings;
           }
           else continue; //Allow for other entity types just in case --PED 2/23/01

           try {
             seqLen = sequence.length();

             for (int i = 0; i < searchStrings.length; i++) {

                codonSeq = searchStrings[i];

                int lastIndex = 0;
                int currIndex = 0;
                int endIndex = (int)seqLen - 1;
                short codonAxisFrame;

                Range codonAxisRange;
                int clen, cpos, axisLoc;

                while ((currIndex != -1) && (currIndex <= endIndex)) {
                  currIndex = sequence.indexOf(codonSeq, lastIndex);

                  if (currIndex != -1) {
                    clen = 0;  // This matches what comes back from the db and fixes Edge Matching in the views.
                    cpos = currIndex;

                    if (calculateForwardStrand) {
                      axisLoc = computeRange.getStart() + cpos;
                      codonAxisRange = new Range(axisLoc, clen, Range.FORWARD_ORIENTATION);
                    }
                    else {
                      axisLoc = computeRange.getStart() - cpos;
                      codonAxisRange = new Range(axisLoc, clen, Range.REVERSE_ORIENTATION);
                    }

                    codonAxisFrame = (short)((codonAxisRange.getStart()%3) + 1);
                    if (codonAxisRange.isReversed()) codonAxisFrame *= -1;

                    if (codonToComputeEntityType.equals(startCodonEntityType)) {
                      // Create entity:
                      codonEntity = (ComputedCodon)standardEntityFactory.create(OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.API_GENERATED_NAMESPACE,this.getGenomeVersion().hashCode()),
                                        startCodonEntityType.toString(),
                                        startCodonEntityType,
                                        "start:" + Math.abs(codonAxisFrame));
                      if (codonEntity == null) {
                        System.err.println("Error: StandardEntityFactory returned null in GenomicAxis.computeCodonAlignments()");
                        lastIndex = currIndex + 1;
                        continue;
                      }
                    }
                    else if (codonToComputeEntityType.equals(stopCodonEntityType)) {
                      codonEntity = (ComputedCodon)standardEntityFactory.create(
                                        OIDGenerator.getOIDGenerator().generateOIDInNameSpace(OID.API_GENERATED_NAMESPACE,this.getGenomeVersion().hashCode()),
                                        stopCodonEntityType.toString(),
                                        stopCodonEntityType,
                                        "stop:" + Math.abs(codonAxisFrame));
                      if (codonEntity == null) {
                        System.err.println("Error: StandardEntityFactory returned null in GenomicAxis.computeCodonAlignments()");
                        lastIndex = currIndex + 1;
                        continue;
                      }
                    }

                    retAlignedCodons.add(new GeometricAlignment(null, codonEntity, codonAxisRange));
                  } //  end if (currIndex != -1)
                  lastIndex = currIndex + 1;
                }  // end while ((currIndex != -1) && (currIndex <= endIndex))
              }  // end for (int i = 0; i < codonList.length; i++)
            }  // end try
            catch(Exception e) {
                try {
                  handleException(e);
                }
                catch(Exception ex1) {
                  ex1.printStackTrace();
                }
            }
          }  // end for (Iterator entityTypeIterator=entityTypes.iterator();entityTypeIterator.hasNext();)
        }  // end for (Iterator rangeIterator=requestedRanges.iterator();rangeIterator.hasNext();)

        Alignment[] retAligned = new Alignment[0];
        if (!retAlignedCodons.isEmpty()) {
            retAligned = (Alignment[])retAlignedCodons.toArray(retAligned);
        }
        return retAligned;
     }

    private List getMyDefaultLoadFilters() {
       if (defaultLoadFilters==null) {
          defaultLoadFilters=new ArrayList();
          defaultLoadFilters.add(
             new GenomicAxisLoadFilter("High Priority Pre-Computes",
               EntityTypeSet.getEntityTypeSet("ComputedFeatureTypes"),
               FeatureDisplayPriority.HIGH_PRIORITY,new Range(0,getMagnitude()),false));
          defaultLoadFilters.add(
             new GenomicAxisLoadFilter("Low Priority Pre-Computes",
               EntityTypeSet.getEntityTypeSet("ComputedFeatureTypes"),
               FeatureDisplayPriority.LOW_PRIORITY,new Range(0,getMagnitude()),false));
          defaultLoadFilters.add(
             new GenomicAxisLoadFilter("Contigs",
               new EntityTypeSet(new EntityType[]{EntityType.getEntityTypeForValue(EntityTypeConstants.Contig)}),
               FeatureDisplayPriority.ALL,new Range(0,getMagnitude()),false));
          defaultLoadFilters.add(
             new GenomicAxisLoadFilter("Curations",
               EntityTypeSet.getEntityTypeSet("CuratedFeatureTypes"),
               FeatureDisplayPriority.ALL,new Range(0,getMagnitude()),false));
          defaultLoadFilters.add(
             new GenomicAxisLoadFilter("Forward Computed Splice Sites",
               EntityTypeSet.getEntityTypeSet("LocallyComputedTransientSpliceFeatureTypes"),
               FeatureDisplayPriority.ALL,new Range(0,getMagnitude()),true));
          defaultLoadFilters.add(
             new GenomicAxisLoadFilter("Forward Computed Start Codons",
               new EntityTypeSet(new EntityType[]{EntityType.getEntityTypeForName("_COMPUTED_START_CODON")}),
               FeatureDisplayPriority.ALL,new Range(0,getMagnitude()),true));
          defaultLoadFilters.add(
             new GenomicAxisLoadFilter("Forward Computed Stop Codons",
               new EntityTypeSet(new EntityType[]{EntityType.getEntityTypeForName("_COMPUTED_STOP_CODON")}),
               FeatureDisplayPriority.ALL,new Range(0,getMagnitude()),true));
          defaultLoadFilters.add(
             new GenomicAxisLoadFilter("Reverse Computed Splice Sites",
               EntityTypeSet.getEntityTypeSet("LocallyComputedTransientSpliceFeatureTypes"),
               FeatureDisplayPriority.ALL,new Range(getMagnitude(),0),true));
          defaultLoadFilters.add(
             new GenomicAxisLoadFilter("Reverse Computed Start Codons",
               new EntityTypeSet(new EntityType[]{EntityType.getEntityTypeForName("_COMPUTED_START_CODON")}),
               FeatureDisplayPriority.ALL,new Range(getMagnitude(),0),true));
          defaultLoadFilters.add(
             new GenomicAxisLoadFilter("Reverse Computed Stop Codons",
               new EntityTypeSet(new EntityType[]{EntityType.getEntityTypeForName("_COMPUTED_STOP_CODON")}),
               FeatureDisplayPriority.ALL,new Range(getMagnitude(),0),true));
       }
       return defaultLoadFilters;
    }


//****************************************
//*  Inner Classes
//***************************************


}  // end public class GenomicAxis extends Axis
