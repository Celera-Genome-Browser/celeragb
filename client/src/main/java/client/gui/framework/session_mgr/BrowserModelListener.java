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

/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/

package client.gui.framework.session_mgr;

import api.entity_model.model.fundtype.GenomicEntity;
import api.stub.geometry.Range;

public interface BrowserModelListener extends GenericModelListener {

    /** The axis of the master editor
    */
    void browserMasterEditorEntityChanged(GenomicEntity masterEditorEntity);

    /**
     * Notification that the SubView's fixed range has changed.  This is the
     * whole range that all SubViews will work with.  This will not change often.
     */
    void browserSubViewFixedRangeChanged(Range subViewFixedRange);

    /**
     * Notification that the SubView's visible range has changed.  This is the
     * visible range of the SubView.  It denotes where the user is looking within
     * the fixed range and will probably change often.
     */
    void browserSubViewVisibleRangeChanged(Range subViewVisibleRange);

    /**
     * Notification that the current system seleciton has changed
     */
    void browserCurrentSelectionChanged(GenomicEntity newSelection);

    /**
     * The selected range on the masterAxis, if any
     */
    public void browserMasterEditorSelectedRangeChanged(Range masterEditorSelectedRange);

    /*
     * Notification that the observed browser is going away...
     */
    public void browserClosing();
}
