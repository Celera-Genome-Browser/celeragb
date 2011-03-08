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
package client.gui.components.other.evidence_report;

import api.entity_model.access.observer.LoadRequestStatusObserverAdapter;
import api.entity_model.access.observer.ReportObserver;
import api.entity_model.access.report.LineItem;
import api.entity_model.access.report.PropertyReport.ReportLineItem;
import api.entity_model.access.report.PropertyReportRequest;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.CuratedCodon;
import api.entity_model.model.annotation.CuratedFeature;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.*;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.stub.geometry.Range;
import client.gui.components.other.report.BaseReportView;
import client.gui.framework.browser.Browser;
import client.gui.framework.navigation_tools.SearchManager;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.SessionMgr;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class EvidenceReport extends BaseReportView {

   private JMenuItem navMenu;
   private String[] fields = { FeatureFacade.GROUP_TAG_PROP, FeatureFacade.ID_PROP, FeatureFacade.AXIS_BEGIN_PROP, FeatureFacade.AXIS_END_PROP };

   public EvidenceReport(Browser browser, Boolean masterBrowser) {
      super(browser, masterBrowser);
      navMenu = new JMenuItem("Navigate To Selected Row");
      navMenu.setEnabled(false);
      navMenu.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            LineItem item = getSelectedLineItem();
            if (item == null)
               return;
            ReportLineItem reportLineItem = (api.entity_model.access.report.PropertyReport.ReportLineItem) item;
            SearchManager.getSearchManager().launchSearch("Feature ID", reportLineItem.getOid().toString());
         }
      });
   }

   public boolean canEditThisEntity(GenomicEntity entity) {
      return true;
   }

   public void writeObject(ObjectOutputStream out) {
      System.out.println("Someone is trying to serialize the ER view.");
   }

   protected void selectedRowChanged(int newRow) {
      LineItem item = getLineItemForRow(newRow);
      if (item == null) {
         return;
      }
      api.entity_model.access.report.PropertyReport.ReportLineItem reportLineItem =
         (api.entity_model.access.report.PropertyReport.ReportLineItem) item;
      int gvId = reportLineItem.getOid().getGenomeVersionId();
      GenomeVersion gv = ModelMgr.getModelMgr().getGenomeVersionById(gvId);
      GenomicEntity entity = gv.getLoadedGenomicEntityForOid(reportLineItem.getOid());
      if (entity != null) {
         getBrowser().getBrowserModel().setCurrentSelection(entity);
      }
      else {
         SearchManager.getSearchManager().launchSearch("Feature ID", reportLineItem.getOid().toString());
      }
   }

   protected String getExplainationLabelText() {
      return "Statistics for current selection's evidence.";
   }

   public String getCurrentReportName() {
      return "Evidence Report";
   }

   public JMenuItem[] getConcreteMenus() {
      if (getSelectedLineItem() == null)
         navMenu.setEnabled(false);
      else
         navMenu.setEnabled(true);
      return new JMenuItem[] { navMenu };
   }

   /**
    * Override method in order to get desired behavior.  Only re-acquire report
    * on Curated Feature change.  For now at least...
    */
   protected void handleBrowserCurrentSelectionChanged(GenomicEntity entity) {
      if (entity == currentEntity)
         return;
      if (entity instanceof CuratedFeature) {
         currentEntity = entity;
         newEntityForReport(entity);
      }
   }

   protected boolean generateReport(GenomicEntity entity, ReportObserver observer) {
      loadEvidenceIfNecessary();
      return acquireReport(entity);
   }

   private boolean acquireReport(GenomicEntity entity) {
      if (!(entity instanceof Feature))
         return false;
      Feature tmpFeature = (Feature) entity;
      // The line below uses ONLY ALREADY LOADED evidence features!!!
      ArrayList tmpEntities = new ArrayList(tmpFeature.getDeepEvidence(false));
      GenomicEntity[] geArray = new GenomicEntity[tmpEntities.size()];
      tmpEntities.toArray(geArray);
      if (geArray.length == 0) {
         //System.out.println("No evidence entities for the report.");
         return false;
      }
      entity.getGenomeVersion().generateReportBackground(new PropertyReportRequest(geArray, fields), observer);
      return true;
   }

   private void loadEvidenceIfNecessary() {
      BrowserModel browserModel = SessionMgr.getSessionMgr().getActiveBrowser().getBrowserModel();

      GenomicEntity tmpEntity = browserModel.getCurrentSelection();
      if (tmpEntity instanceof CuratedFeature && !(tmpEntity instanceof CuratedCodon)) {
         GenomicAxis axis = (GenomicAxis) browserModel.getMasterEditorEntity();
         Range tmpRange = ((GeometricAlignment) ((CuratedFeature) tmpEntity).getOnlyAlignmentToAnAxis(axis)).getRangeOnAxis();

         LoadFilter hiPriFilter = axis.getHighPriPreComputeLoadFilter();
         LoadRequestStatus status = loadAlignments(axis, tmpRange, hiPriFilter);
         status.addLoadRequestStatusObserver(new MyLoadStatusObserver(tmpEntity), true);
      }
   }

   public LoadRequestStatus loadAlignments(Axis axis, Range range, LoadFilter loadFilter) {
      // If the filter is NOT strand specific, we need to always have a forward range...
      if (!loadFilter.isStrandSpecific() && range.isReversed()) {
         range = range.toReverse();
      }
      return axis.loadAlignmentsToEntitiesBackground(new LoadRequest(range, loadFilter, false));
   }

   private class MyLoadStatusObserver extends LoadRequestStatusObserverAdapter {
      GenomicEntity entity;
      public MyLoadStatusObserver(GenomicEntity tmpEntity) {
         this.entity = tmpEntity;
      }

      public void stateChanged(LoadRequestStatus loadRequestStatus, LoadRequestState newState) {
         if (newState == LoadRequestStatus.COMPLETE) {
            acquireReport(entity);
            loadRequestStatus.removeLoadRequestStatusObserver(this);
         }
      }
   } //end LoadStatusObserverForEdgeMatch
}
