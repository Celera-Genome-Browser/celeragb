package api.facade.concrete_facade.gff3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import api.facade.concrete_facade.shared.FeatureCriterion;
import api.facade.concrete_facade.shared.OIDParser;
import api.facade.concrete_facade.shared.feature_bean.CompoundFeatureBean;
import api.facade.concrete_facade.shared.feature_bean.FeatureBean;
import api.facade.facade_mgr.FacadeManagerBase;
import api.stub.data.OID;
import api.stub.geometry.Range;

public class FeatureScanner implements FeatureListener {

	private String source;
	private OIDParser oidParser;
	private FacadeManagerBase facadeMgr;
	private OID axisOfAlignment;
	private List<FeatureBean> featureListFromScan;
    private Range sequenceAlignmentRange = null;
	
	public FeatureScanner( 
			String source,
			FacadeManagerBase facadeMgr,
			OID axisOfAlignment,
			Range sequenceAlignmentRange, 
			OIDParser oidParser ) {
		this.source = source;
		this.oidParser = oidParser;
		this.sequenceAlignmentRange = sequenceAlignmentRange;
		this.axisOfAlignment = axisOfAlignment;
		this.facadeMgr = facadeMgr;
	}
	
	/**
	 * Satisfy the "contract" represented by the criterion object.  Find everything in the
	 * source whose name is given, by parsing the file.  Fill the results into the returned
	 * list. 
	 * 
	 * @param criterion logic to select among all the beans in the input source.
	 * @return list of fits.
	 */
	public List<FeatureBean> getBeansForCriterion( FeatureCriterion criterion ) throws Exception {
		featureListFromScan = new ArrayList<FeatureBean>();
		FeatureFactory factory = new FeatureFactory();
		factory.setFeatureListener( this );
		factory.loadFile( source, axisOfAlignment, facadeMgr );
		List<FeatureBean> returnList = new ArrayList<FeatureBean>();
		for ( FeatureBean bean: returnList ) {
			returnList.addAll(
					listOfMatchingFeaturesIn( 
							criterion.allMatchingIn( bean ) )
					);
		}
		featureListFromScan = null;
		return returnList;
	}
	
	//--------------------------------IMPLEMENT FeatureListener
	public void feature( CompoundFeatureBean compoundFeature ) {
		featureListFromScan.add( compoundFeature );
	}
	
    /**
     * Find list of features that match the "fit" criteria.  This
     * could include the model itself, or any of its descendants.
     */
	private List<FeatureBean> listOfMatchingFeaturesIn( List<FeatureBean> candidateList ) {
		List<FeatureBean> returnList = new ArrayList<FeatureBean>();

		if (candidateList != null) {
			if (this.sequenceAlignmentRange == null) {
				returnList.addAll(candidateList);
			} // No alignment.
			else {
				for ( FeatureBean nextModel : candidateList ) {
					if (sequenceAlignmentRange.contains(nextModel.calculateFeatureRange())) {
						returnList.add(nextModel);
					} // Agrees with alignment.
				} // For all iterations
			} // Non-null alignment
		} // Got real list back.

		return returnList;
    }

}
