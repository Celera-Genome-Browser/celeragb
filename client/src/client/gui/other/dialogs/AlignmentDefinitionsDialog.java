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
import api.entity_model.model.fundtype.AlignableGenomicEntity;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.stub.data.ControlledVocabUtil;
import api.stub.data.GenomicProperty;
import client.gui.framework.session_mgr.BrowserModel;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class AlignmentDefinitionsDialog extends AbstractTableDialog {
    DefaultTableModel tableModel;
    private static final GenomicProperty EMPTY_PROP = new GenomicProperty("","","",false,ControlledVocabUtil.getNullVocabIndex());

    public AlignmentDefinitionsDialog(BrowserModel browserModel, String propertyName) {
        super(browserModel, propertyName);
        this.setTitle("Alignment Definitions");
        show();
    }

    public AlignmentDefinitionsDialog() {
        super(null, null);
        this.setTitle("Alignment Definitions");
    }

    protected TableModel buildTableModel() {
        Object[] colNames = {
          PropertyMgr.getPropertyMgr().getPropertyDisplayName(FeatureFacade.GENOMIC_AXIS_ID_PROP),
          PropertyMgr.getPropertyMgr().getPropertyDisplayName(FeatureFacade.GENOMIC_AXIS_NAME_PROP),
          PropertyMgr.getPropertyMgr().getPropertyDisplayName(FeatureFacade.AXIS_BEGIN_PROP),
          PropertyMgr.getPropertyMgr().getPropertyDisplayName(FeatureFacade.AXIS_END_PROP),
          PropertyMgr.getPropertyMgr().getPropertyDisplayName(FeatureFacade.ENTITY_LENGTH_PROP),
          PropertyMgr.getPropertyMgr().getPropertyDisplayName(FeatureFacade.ENTITY_ORIENTATION_PROP),
          PropertyMgr.getPropertyMgr().getPropertyDisplayName(FeatureFacade.ENTITY_ODDS_PROP),
          PropertyMgr.getPropertyMgr().getPropertyDisplayName(FeatureFacade.ENTITY_IS_FRAME_PROP)};
        tableModel = new DefaultTableModel(colNames, 0);
        AlignableGenomicEntity selectedEntity = (AlignableGenomicEntity)browserModel.getCurrentSelection();
        GenomicProperty[] defs = selectedEntity.getProperty(FeatureFacade.NUM_ALIGNMENTS_PROP).getSubProperties();

        for (int x=0; x< defs.length;x=x+colNames.length) {
          GenomicProperty tmpID           = EMPTY_PROP;
          GenomicProperty tmpName         = EMPTY_PROP;
          GenomicProperty tmpAxisBegin    = EMPTY_PROP;
          GenomicProperty tmpAxisEnd      = EMPTY_PROP;
          GenomicProperty tmpEntityBegin  = EMPTY_PROP;
          GenomicProperty tmpEntityEnd    = EMPTY_PROP;
          GenomicProperty tmpOrientation  = EMPTY_PROP;
          GenomicProperty tmpOdds         = EMPTY_PROP;
          GenomicProperty tmpIsFrame      = EMPTY_PROP;
          try {
            tmpID           = defs[x];
            tmpName         = defs[x+1];
            tmpAxisBegin    = defs[x+2];
            tmpAxisEnd      = defs[x+3];
            tmpEntityBegin  = defs[x+4];
            tmpEntityEnd    = defs[x+5];
            tmpOrientation  = defs[x+6];
            tmpOdds         = defs[x+7];
            tmpIsFrame      = defs[x+8];
          }
          catch (Exception ex) {}

          //Add Rows with:
         //tableModel.addRow(Vector or Object[]);
          tableModel.addRow(
            new Object[] {
              tmpID.getInitialValue(),
              tmpName.getInitialValue(),
              tmpAxisBegin.getInitialValue(),
              tmpAxisEnd.getInitialValue(),
              tmpEntityBegin.getInitialValue(),
              tmpEntityEnd.getInitialValue(),
              tmpOrientation.getInitialValue(),
              tmpOdds.getInitialValue(),
              tmpIsFrame.getInitialValue()});
        }
        return tableModel;
    }

}
