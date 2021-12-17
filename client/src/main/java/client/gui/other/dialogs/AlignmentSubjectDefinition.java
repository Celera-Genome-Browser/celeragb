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

package client.gui.other.dialogs;

import api.entity_model.management.PropertyMgr;
import api.entity_model.model.annotation.HitAlignmentDetailFeature;
import api.entity_model.model.annotation.HitAlignmentFeature;
import api.entity_model.model.fundtype.GenomicEntity;
import api.facade.abstract_facade.annotations.HitAlignmentDetailLoader;
import api.stub.data.SubjectDefinition;
import client.gui.framework.session_mgr.BrowserModel;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.Collection;
import java.util.Iterator;

public class AlignmentSubjectDefinition extends AbstractTableDialog {
    DefaultTableModel subjectTableModel;

    public AlignmentSubjectDefinition(BrowserModel browserModel, String propertyName) {
        super(browserModel, propertyName);
        this.setTitle(PropertyMgr.getPropertyMgr().getPropertyDisplayName(HitAlignmentDetailLoader.NUM_SUBJ_DEFNS_PROP));
        setVisible(true);
    }

    protected TableModel buildTableModel() {
        Object[] subjDefColNames = {
          PropertyMgr.getPropertyMgr().getPropertyDisplayName(HitAlignmentDetailLoader.ACCESSSION_NUM_PROP),
          PropertyMgr.getPropertyMgr().getPropertyDisplayName(HitAlignmentDetailLoader.ALT_ACCESSION_PROP),
          PropertyMgr.getPropertyMgr().getPropertyDisplayName(HitAlignmentDetailLoader.ISSUING_AUTHORITY_PROP),
          PropertyMgr.getPropertyMgr().getPropertyDisplayName(HitAlignmentDetailLoader.SPECIES_PROP),
          PropertyMgr.getPropertyMgr().getPropertyDisplayName(HitAlignmentDetailLoader.DESCRIPTION_PROP),
          PropertyMgr.getPropertyMgr().getPropertyDisplayName(HitAlignmentDetailLoader.KEYWORD_PROP)};
        subjectTableModel = new DefaultTableModel(subjDefColNames, 0);
        //Add Rows with:
        //subjectTableModel.addRow(Vector or Object[]);
        GenomicEntity selectedEntity = browserModel.getCurrentSelection();
        Collection defs;
        if ( selectedEntity instanceof HitAlignmentFeature ) {
           defs = ( ( HitAlignmentFeature )selectedEntity ).loadSubjectDefinitionsBlocking();
        }
        else if ( selectedEntity instanceof HitAlignmentDetailFeature ) {
           defs = ( ( HitAlignmentDetailFeature )selectedEntity ).loadSubjectDefinitionsBlocking();
        }
        else {
          return ( null );
        }

        for (Iterator it=defs.iterator();it.hasNext();) {
            SubjectDefinition tmpDef = (SubjectDefinition)it.next();
            subjectTableModel.addRow(new Object[] {
                tmpDef.accession, "", tmpDef.authority, tmpDef.species, tmpDef.description, ""
            });
        }
        return subjectTableModel;
    }
}
