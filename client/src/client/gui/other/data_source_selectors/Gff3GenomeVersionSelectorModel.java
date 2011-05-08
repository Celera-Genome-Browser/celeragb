// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package client.gui.other.data_source_selectors;

import api.entity_model.management.ModelMgr;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.facade_mgr.FacadeManagerBase;
import client.gui.framework.session_mgr.SessionMgr;

import java.util.Arrays;
import java.util.Collections;

class Gff3GenomeVersionSelectorModel extends AbstractGenomeVersionSelectorModel {

	private FacadeManagerBase base;

	public Gff3GenomeVersionSelectorModel() {
	}

	public void setFacadeManagerBase( FacadeManagerBase base ) {
		this.base = base;
	}

	public void init() {
		Object obj=SessionMgr.getSessionMgr().getModelProperty("ShowInternalDataSourceInDialogs");
		if (obj!=null && obj instanceof Boolean) {
			showDataSource=((Boolean)obj).booleanValue();
		}
		try {
			GenomeVersion[] genomeVersionArr = base.getGenomeLocator().getAvailableGenomeVersions();
			genomeVersionsList = Arrays.asList( genomeVersionArr ); 
			Collections.sort(genomeVersionsList, new GVComparator(this.sortCol,this.sortAsc));
			this.fireTableStructureChanged();
		} catch ( Exception ex ) {
			ModelMgr.getModelMgr().handleException( ex );
		}
	}

}
