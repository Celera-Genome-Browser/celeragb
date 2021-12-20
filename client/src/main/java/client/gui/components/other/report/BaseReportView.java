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
package client.gui.components.other.report;

import api.entity_model.access.observer.ReportObserver;
import api.entity_model.access.report.LineItem;
import api.entity_model.access.report.Report;
import api.entity_model.access.report.ReportRequest;
import api.entity_model.management.PropertyMgr;
import api.entity_model.model.fundtype.GenomicEntity;
import client.gui.framework.browser.Browser;
import client.gui.framework.roles.Editor;
import client.gui.framework.roles.SubEditor;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.BrowserModelListener;
import client.gui.framework.session_mgr.BrowserModelListenerAdapter;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class BaseReportView extends JPanel implements
    Editor,SubEditor,HTMLViewable {

    private BrowserModel browserModel;
    private BrowserModelListener browserModelListener;
    private Browser browser;
    private JLabel explainationLabel=new JLabel();
    private JTable table = new JTable();
    private Report currentReport;
    protected GenomicEntity currentEntity;
    private JMenuItem[] menus;
    protected ReportObserver observer=new MyReportObserver();
    private TableModel loadingTm=new ReadOnlyTableModel(
      new Object[]{"Data is Loading"},0);
    private TableModel noReportTm=new ReadOnlyTableModel(
      new Object[]{"No Report Returned"},0);

    public BaseReportView(Browser browser, Boolean masterBrowser) {
        menus=new JMenuItem[]{new HTMLReportMenuItem(this)};
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e){
                 if (!e.getValueIsAdjusting() && table.getSelectedRow()>=0) selectedRowChanged(table.getSelectedRow());
            }
        });
        this.browser=browser;
        browserModel = browser.getBrowserModel();
        browserModelListener = new MyBrowserModelListener();
        explainationLabel.setText(getExplainationLabelText());
        setName(getCurrentReportName());
        JScrollPane jsp=new JScrollPane();
        jsp.setViewportView(table);
        refreshTable();
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        add(Box.createVerticalStrut(10));
        add(explainationLabel);
        add(Box.createVerticalStrut(10));
        add(jsp);
        add(Box.createVerticalStrut(10));
    }

    public void activate(){
      browserModel.addBrowserModelListener(browserModelListener);
    }
    public void passivate(){
      browserModel.removeBrowserModelListener(browserModelListener);
    }

    public String toString() {
      return getCurrentReportName();
    }

    protected abstract String getExplainationLabelText();
    public abstract String getCurrentReportName();
    protected abstract boolean generateReport(GenomicEntity entity,ReportObserver observer);
    protected abstract JMenuItem[] getConcreteMenus();

    //Override to receive notifications of the selected row changing.
    protected void selectedRowChanged(int newRow){}

    public JMenuItem[] getMenus() {
      java.util.List list=new ArrayList();
      list.addAll(Arrays.asList(menus));
      list.addAll(Arrays.asList(getConcreteMenus()));
      return (JMenuItem[]) list.toArray(new JMenuItem[list.size()]);
    }

    //editor interface
    public void dispose() {
      browserModel.removeBrowserModelListener(browserModelListener);
      closeComponent();
    }


    public Report getCurrentReport() {
       return currentReport;
    }


    protected Browser getBrowser() {
      return browser;
    }

    protected LineItem getSelectedLineItem() {
      if (table.getSelectedRowCount()==0) return null;
      return getCurrentReport().getLineItem(table.getSelectedRow());
    }

    protected LineItem getLineItemForRow(int row) {
      return  getCurrentReport().getLineItem(row);
    }

    protected void newEntityForReport(GenomicEntity entity) {
      table.setModel(loadingTm);
      currentReport=null;
      boolean requestSuccessful = generateReport(entity,observer);
      if (!requestSuccessful) table.setModel(noReportTm);
    }


    private void closeComponent() {
        browserModel.removeBrowserModelListener(browserModelListener);
    }

    private void refreshTable() {
        table.sizeColumnsToFit(-1);
        table.revalidate();
        table.repaint();
    }


    class MyBrowserModelListener extends BrowserModelListenerAdapter {
        public void browserCurrentSelectionChanged(GenomicEntity entity) {
          handleBrowserCurrentSelectionChanged(entity);
        }

        public void browserClosing() {
            closeComponent();
        }
    }


    protected void handleBrowserCurrentSelectionChanged(GenomicEntity entity) {
      if(entity==currentEntity)return;
      currentEntity=entity;
      newEntityForReport(entity);
    }

    class MyReportObserver implements ReportObserver {
      public void reportArrived(GenomicEntity entityThatReportWasRequestedFrom,
          ReportRequest request, Report report) {
          setReport(report);
          currentReport=report;
          if (report==null) currentEntity=null;
      }
    }

    protected void setReport(Report report) {
      DefaultTableModel tm= new DefaultTableModel();
      if (report == null || report.getLineItems().length==0) tm = (DefaultTableModel)noReportTm;
      else {
        ArrayList tmpList = new ArrayList();
        Object[] tmpFields = report.getFields();
        for (int x = 0; x < tmpFields.length; x++) {
          tmpList.add(PropertyMgr.getPropertyMgr().getPropertyDisplayName((String)tmpFields[x]));
        }
        tm=(DefaultTableModel) new ReadOnlyTableModel(report.getReportData(),tmpList.toArray());
      }
      table.setModel(tm);
    }

    class ReadOnlyTableModel extends DefaultTableModel{
      private Object[] columnNames;

      private ReadOnlyTableModel(Object[] columnNames, int numRows) {
        super(columnNames,numRows);
      }

      private ReadOnlyTableModel(Object[][] data, Object[] columnNames) {
        super(data,columnNames);
      }

      public boolean isCellEditable(int row, int column) {
          return false;
      }
    }
}
