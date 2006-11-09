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
package client.gui.components.other.subj_seq_report;

import api.entity_model.access.observer.ReportObserver;
import api.entity_model.access.report.GeneralReportRequest;
import api.entity_model.access.report.LineItem;
import api.entity_model.access.report.ReportRequest;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.annotation.HitAlignmentFeature;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.genetics.GenomeVersion;
import client.gui.components.other.report.BaseReportView;
import client.gui.framework.browser.Browser;
import client.gui.framework.navigation_tools.SearchManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectOutputStream;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class SubjectSequenceReport extends BaseReportView {

    private JMenuItem navMenu;

    public SubjectSequenceReport(Browser browser, Boolean masterBrowser) {
        super(browser,masterBrowser);
        navMenu=new JMenuItem("Navigate to Selected Row");
        navMenu.setEnabled(false);
        navMenu.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e){
              LineItem item=getSelectedLineItem();
              if (item==null) return;
              api.entity_model.access.report.SubjectSequenceReport.SubjectSequenceReportLineItem
                reportLineItem=(api.entity_model.access.report.SubjectSequenceReport.SubjectSequenceReportLineItem)item;
              SearchManager.getSearchManager().launchSearch("Feature ID",reportLineItem.getOid().toString());
          }
        });
    }

    public boolean canEditThisEntity(GenomicEntity entity) {
      return entity != null && (entity instanceof HitAlignmentFeature);
    }

    public void writeObject(ObjectOutputStream out) {
      System.out.println("Someone is trying to serialize the SSR view.");
    }

    protected void selectedRowChanged(int newRow){
      LineItem item=getLineItemForRow(newRow);
      if (item==null) {
        return;
      }
      api.entity_model.access.report.SubjectSequenceReport.SubjectSequenceReportLineItem reportLineItem=
      (api.entity_model.access.report.SubjectSequenceReport.SubjectSequenceReportLineItem)item;
      if (reportLineItem.getAxisOid().equals(getBrowser().getBrowserModel().getMasterEditorEntity().getOid())) {
          int gvId=reportLineItem.getOid().getGenomeVersionId();
          GenomeVersion gv=ModelMgr.getModelMgr().getGenomeVersionById(gvId);
          GenomicEntity entity=gv.getLoadedGenomicEntityForOid(reportLineItem.getOid());
          if (entity!=null) {
             getBrowser().getBrowserModel().setCurrentSelection(entity);
          }
          else {
            SearchManager.getSearchManager().launchSearch("Feature ID",reportLineItem.getOid().toString());
          }
      }
      else {
          int ans=JOptionPane.showConfirmDialog(getBrowser(),
            "The selected row refers to a feature that is not on this axis.  \nWould you like"+
            " to leave this axis and navigate to it?","Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
          if (ans==JOptionPane.YES_OPTION) {
            SearchManager.getSearchManager().launchSearch("Feature ID",reportLineItem.getOid().toString());
          }
      }
    }

    protected String getExplainationLabelText() {
       return "The selected entity has the same subject sequence as the following"+
        " entities in this Genome Version.";
    }

    public String getCurrentReportName(){
      return "Subject Sequence Report";
    }

    public JMenuItem[] getConcreteMenus(){
      if (getSelectedLineItem()==null) navMenu.setEnabled(false);
      else navMenu.setEnabled(true);
      return new JMenuItem[]{navMenu};
    }

    protected boolean generateReport(GenomicEntity entity, ReportObserver observer){
      entity.getGenomeVersion().generateReportBackground(
        new GeneralReportRequest(ReportRequest.SUBJECT_SEQUENCE_REPORT,
          new GenomicEntity[]{entity}),observer);
      return true;
    }
}
