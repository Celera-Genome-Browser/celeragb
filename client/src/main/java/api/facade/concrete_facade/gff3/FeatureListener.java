package api.facade.concrete_facade.gff3;

import api.facade.concrete_facade.shared.feature_bean.CompoundFeatureBean;

/** Implement this to accept features as they are discovered/created. */
public interface FeatureListener {
	void feature( CompoundFeatureBean compoundFeature );
}
