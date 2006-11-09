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
 * CVS_ID:  $Id$
 */

package api.facade.concrete_facade.aggregate;

import api.entity_model.model.fundtype.EntityType;
import api.facade.abstract_facade.annotations.ExonFacade;
import api.facade.abstract_facade.annotations.GenePredictionDetailFacade;
import api.facade.abstract_facade.annotations.GenePredictionFacade;
import api.facade.abstract_facade.annotations.RepeatMaskerHitFacade;
import api.facade.abstract_facade.annotations.STSFacade;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.facade.abstract_facade.annotations.TrnaScanFacade;

/**
 * Note: You MUST add to the implements list ANY Facade you intend this to represent.
 * ALL of the facades this represents MUST have no methods other than those they inherit from FeatureFacade.
 */
public class AggregateTypeSpecificFeatureFacade extends AggregateFeatureFacade implements
    TrnaScanFacade, GenePredictionFacade,
    TranscriptFacade, ExonFacade, RepeatMaskerHitFacade,
    GenePredictionDetailFacade, STSFacade {

    EntityType featureType;

    public AggregateTypeSpecificFeatureFacade(EntityType featureType){
       super(true);  //Must make these two additional calls to ensure the features are setup in this instance
       this.featureType=featureType;
       getAggregateReturningMethod();  //second addition call
    }

    protected String getMethodNameForAggregates() {
       return "getFacade";
    };

    protected Class[] getParameterTypesForAggregates() {
       return new Class[]{EntityType.class};
    }

    protected Object[] getParametersForAggregates() {
      return new Object[]{featureType};
    };

}
