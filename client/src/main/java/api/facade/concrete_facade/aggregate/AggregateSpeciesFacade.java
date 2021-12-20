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
package api.facade.concrete_facade.aggregate;

import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.genetics.SpeciesLoader;
import api.stub.data.NoData;
import api.stub.data.OID;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

public class AggregateSpeciesFacade extends AggregateAxisFacade implements SpeciesLoader {

  public GenomeVersion[] getGenomeVersions(OID oid) throws NoData {
    /**@todo: Implement this api.facade.abstract_facade.genetics.Species method*/
    throw new java.lang.UnsupportedOperationException("Method getGenomeVersions() not yet implemented.");
  }

  protected String getMethodNameForAggregates(){
     return "getSpecies";
  }

  protected Class[] getParameterTypesForAggregates(){
     return new Class[0];
  };

  protected  Object[] getParametersForAggregates(){
     return new Object[0];
  }
}