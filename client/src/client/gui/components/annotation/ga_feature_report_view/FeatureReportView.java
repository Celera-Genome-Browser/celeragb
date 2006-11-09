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
package client.gui.components.annotation.ga_feature_report_view;

import api.entity_model.access.report.PropertyReport;
import api.entity_model.access.report.Report;
import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.annotation.HSPFeature;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.AlignableGenomicEntity;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.fundtype.SingleAlignmentSingleAxis;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.stub.data.GenomicProperty;
import api.stub.geometry.MutableRange;
import api.stub.geometry.Range;
import client.gui.components.other.report.HTMLReportMenuItem;
import client.gui.components.other.report.HTMLViewable;
import client.gui.framework.browser.Browser;
import client.gui.framework.roles.SubEditor;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.BrowserModelListenerAdapter;
import client.gui.framework.session_mgr.SessionMgr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class FeatureReportView extends javax.swing.JPanel implements SubEditor,
  TableModelListener, HTMLViewable {
    private boolean DEBUG = false;
    private JPanel thisPanel;
    private Browser browser;
    private BrowserModel browserModel;
    private FeaturePropertyDataModel featurePropModel = new FeaturePropertyDataModel();
    private JTable featurePropTable;
    private TableSorter sorter;
    private JTabbedPane tabbedPane;
    private JMenuItem populateMI;
    private JMenuItem clearMenu;
    private Feature selectedFeature;
    private int numTabs = 0;
    public static final int COLUMN_MARGIN = 5;

    //private ComponentListener componentListener;
    private boolean bViewActive = false;
    private GenomicAxis masterAxis;
    private EntityVisitor entityVisitor;
    private BrowserModelListenerAdapter browserModelListener;
    private ActionListener descriptionActionListener;
    private ActionListener copyActionListener;
    private ListSelectionListener rowSMListener;
    private MouseListener featurePropTableMouseListener;
    private ChangeListener tabbedPaneChangeListener;

    private int currentSelectedTab;
    private boolean setBrowserLastSelectionMode = true;

    private JLabel loadingLabel = new JLabel("Table data is loading.  Please wait.", JLabel.HORIZONTAL);
    private JLabel welcomeLabel = new JLabel("Please use right mouse click for populating the feature report!", JLabel.HORIZONTAL);
    private JLabel noDataLabel =  new JLabel("Set the SubView range from the Data Manipulation menu.", JLabel.HORIZONTAL);
    private JPanel loadingPanel;
    private ButtonGroup reportGroup;
    private JPanel welcomePanel;
    private JPanel noDataPanel;
    private HTMLReportMenuItem htmlReportMenuItem;
    private MutableRange lastFixedRange = new MutableRange();

    public FeatureReportView(Browser browser, Boolean masterBrowser) {
      setName("Feature Report");
      thisPanel = this;
      this.browser = browser;
      browserModel = browser.getBrowserModel();

      browserModelListener = new MyBrowserModelListener();
      setupComponents();
    }


   /**
    * Do what needs to be done when the view becomes active
    */
   public void activate() {
     bViewActive = true;
     if (DEBUG) System.out.println("FRV: activate");
     browserModel.addBrowserModelListener(browserModelListener, true);
   }


   /**
    * Do what needs to be done when the view becomes passive
    */
   public void passivate() {
     if (DEBUG) System.out.println("FRV: passivate");
     bViewActive = false;
     browserModel.removeBrowserModelListener(browserModelListener);
   }


    /**
     * Return a minimum size from this component, so that it will not be
     * determined by any of its tabbed panes or their contents.
     */
    public Dimension getMinimumSize() {
      // Min width need not be specified.  Min height is
      // to provide pixels for: one tab row, one table row.
      return new Dimension(0, 30);
    } // End method


    private void setupComponents() {
      setLayout(new BorderLayout());
      loadingLabel.setFont(new Font("serif", Font.BOLD, 16));
      loadingLabel.setVerticalTextPosition(JLabel.BOTTOM);
      loadingLabel.setHorizontalTextPosition(JLabel.CENTER);
      sorter = new TableSorter(featurePropModel);
      sorter.addTableModelListener(this);
//      featurePropModel.addTableModelListener(this);
      featurePropTable = new JTable(sorter);
      featurePropTable.setDefaultRenderer(Double.class, new DoubleRenderer());

      welcomePanel = new JPanel();
      welcomeLabel.setFont(new Font("serif", Font.BOLD, 16));
      welcomePanel.add(welcomeLabel);
      noDataPanel = new JPanel();
      noDataLabel.setFont(new Font("serif", Font.BOLD, 16));
      noDataPanel.add(noDataLabel);

      add(welcomePanel);

      //featurePropTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
      featurePropTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      tabbedPane = new JTabbedPane();
      tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);

      JTableHeader hdr = featurePropTable.getTableHeader();

      hdr.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
            TableColumnModel tcm = featurePropTable.getColumnModel();
            int vc = tcm.getColumnIndexAtX(e.getX());
            int mc = featurePropTable.convertColumnIndexToModel(vc);
            if (e.isShiftDown())
              sorter.sort(mc, false);
            else
              sorter.sort(mc, true);
          }
      });


      tabbedPaneChangeListener = new ChangeListener()
      {
          public void stateChanged(ChangeEvent e)
          {
            JTabbedPane tp = (JTabbedPane)e.getSource();

            if ( tp == tabbedPane )
            {
              if (DEBUG) System.out.println("tabbedPane......setSelectedTab");
              Component c = tabbedPane.getComponentAt(currentSelectedTab);
              if ( c == null ) return;
              if (c instanceof JScrollPane)
              {
                  JScrollPane sp = (JScrollPane)c;
                  sp.setViewport(new JViewport());
              }
              setSelectedTab(tp.getSelectedIndex());
            }
          }
      };
      tabbedPane.addChangeListener(tabbedPaneChangeListener);

      htmlReportMenuItem = new HTMLReportMenuItem(this);
      populateMI = new JMenuItem("Populate");
      populateMI.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          clearView();
          populateView();
        }
      });

      clearMenu = new JMenuItem("Clear");
      clearMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          clearView();
        }
      });

      featurePropTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      featurePropTableMouseListener = new MyMouseHandler();
      featurePropTable.addMouseListener(featurePropTableMouseListener);

      this.doLayout();
    }

    private void setupTabPanes(ArrayList featureGroupList) {
        if (featureGroupList == null) return;
        String featureName;
        for ( Iterator i = featureGroupList.iterator(); i.hasNext(); ) {
          featureName = (String)i.next();
          if (DEBUG) System.out.println("setupTabPanes...featureName = "+featureName);
          JScrollPane sp = new JScrollPane();
          tabbedPane.add(featureName, sp);
          numTabs++;
        }
        this.add(tabbedPane, BorderLayout.CENTER);
        this.validateTree();
    }


    public void writeObject(ObjectOutputStream out) {
      System.out.println("Someone is trying to serialize the FR view.");
    }


    public void tableChanged(TableModelEvent e) {
        if(DEBUG) System.out.println("tableChanged........Get fireTableStructureChaned from model");
        Component c = tabbedPane.getComponentAt(currentSelectedTab);
        if (c instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane)c;
            sp.setViewportView(featurePropTable);
        }
        tabbedPane.validate();
        this.validateTree();
        //featurePropTable.sizeColumnsToFit(0);
        featurePropTable.repaint();

        int row = -1;
        if (selectedFeature!=null) {
            row = sorter.getObjectRow(selectedFeature);
        }

        featurePropTable.clearSelection();
        if ( row >= 0 ) {
          featurePropTable.setRowSelectionInterval(row, row);
          Rectangle rect = featurePropTable.getCellRect(row, 0, true);
          featurePropTable.scrollRectToVisible(rect);
        }
    }


    /** Fill in the tab panel when it has been selected. */
    public void setSelectedTab(int index) {
      if (DEBUG) System.out.println("setSelectedTab index = "+ index+ " numTabs ="+numTabs);
      if ((index < 0) || (index > numTabs)) return;

      if (tabbedPane.getSelectedIndex() != index)
        tabbedPane.setSelectedIndex(index);

      currentSelectedTab = index;
      Component c = tabbedPane.getComponentAt(currentSelectedTab);
      if ( c!= null && c instanceof JScrollPane) {
          JScrollPane sp = (JScrollPane)c;
          loadingPanel = new JPanel();
          loadingPanel.add(loadingLabel);
          sp.setViewportView(loadingPanel);
      }
      if (DEBUG && selectedFeature != null) System.out.println("........selectedFeature = "+selectedFeature.toString());

      // Format and get the report for features of the tab that was selected.
      String tmpTitle = tabbedPane.getTitleAt(index);

      featurePropModel.setVisibleType(masterAxis.getGenomeVersion(), tmpTitle,
        (Integer)entityVisitor.getFeatureGroupToModeMap().get(tmpTitle));
    }


    private int getPreferredWidthForColumn(TableColumn col) {
      int hw = columnHeaderWidth(col);
      int cw = widestCellInColumn(col);
      return hw > cw ? hw : cw;
    }


    private int columnHeaderWidth(TableColumn col) {
      TableCellRenderer renderer = col.getHeaderRenderer();
      Component comp = renderer.getTableCellRendererComponent(featurePropTable, col.getHeaderValue(), false, false, 0, 0);
      return comp.getPreferredSize().width;
    }


    private int widestCellInColumn(TableColumn col) {
      int c = col.getModelIndex(), width = 0, maxw = 0;
      for (int r = 0; r < featurePropTable.getRowCount(); ++r) {
          TableCellRenderer renderer = featurePropTable.getCellRenderer(r, c);
          Component comp = renderer.getTableCellRendererComponent(featurePropTable, featurePropTable.getValueAt(r, c), false, false, r, c);
          if (comp instanceof JTextArea) {
              String st = ((JTextArea)comp).getText();
              FontMetrics fm = featurePropTable.getFontMetrics(featurePropTable.getFont());
              width = fm.stringWidth(st);
              maxw = width > maxw ? width : maxw;
          }
      }
      return maxw;
    }


    private int getMaxRowHeightForColumn(TableColumn column) {
      int height = 0, maxh = 0, c = column.getModelIndex();
      for (int r = 0; r < featurePropTable.getRowCount(); ++r) {
        TableCellRenderer renderer = featurePropTable.getCellRenderer(r, c);
        Component comp = renderer.getTableCellRendererComponent(featurePropTable, featurePropTable.getValueAt(r, c), false, false, r, c);
        height = comp.getMaximumSize().height;
        maxh = height > maxh ? height : maxh;
      }
      //System.out.println("getMaxRowHeightForColumn index=" + c + " returning=" + maxh);
      return maxh;
    }


    private int getMaxRowHeight() {
      int columnCount = featurePropTable.getColumnCount(), h = 0, maxh = 0;
      for (int i = 0; i < columnCount; i++) {
          TableColumn column = featurePropTable.getColumnModel().getColumn(i);
          h = getMaxRowHeightForColumn(column);
          maxh = h > maxh ? h : maxh;
      }
      //System.out.println("getMaxRowHeight returning: " + maxh);
      return maxh;
    }


    public Report getCurrentReport() { return featurePropModel.getPropertyReport(); }


    public String getCurrentReportName() { return "Feature Report"; }


    private void populateView() {
      if (DEBUG) System.out.println("populateView........");
      clearView();

      this.remove(welcomePanel);
      this.remove(noDataPanel);
      entityVisitor = new EntityVisitor();
      masterAxis.acceptVisitorForAlignedEntities(entityVisitor, false);

      Set entitySet = entityVisitor.getReportSet();
      ArrayList featureGroupList = new ArrayList(entityVisitor.getReportFeatureGroupSet());
      Collections.sort(featureGroupList);
      if (entitySet.isEmpty()) {
          this.add(noDataPanel);
      }
      else {
          featurePropModel.loadModel(entitySet);
          sorter = new TableSorter(featurePropModel);
          featurePropTable.setModel(sorter);
          setupTabPanes(featureGroupList);
          setSelectedTab(0);
      }
      browserModelListener.browserCurrentSelectionChanged(browserModel.getCurrentSelection());
    }


    private void clearView() {
      add(welcomePanel);
      numTabs = 0;
      selectedFeature=null;
      featurePropModel.clear();
      featurePropTable.removeAll();
      tabbedPane.removeAll();
      this.repaint();
      this.doLayout();
      currentSelectedTab = 0;
    }


    public void dispose() {
      browserModel.removeBrowserModelListener(browserModelListener);
    }


    public boolean canEditThisEntity(GenomicEntity entity) {
      return true ;
    }


    public JMenuItem[] getMenus() {
      return new JMenuItem[] {populateMI, clearMenu, htmlReportMenuItem};
    }


    /**
     * Private inner class for handling mouse events
     */
     private class MyMouseHandler extends MouseAdapter {
       private void showPopup(String text) {
         FeatureDescriptionDialog dialog = new FeatureDescriptionDialog(browser," Value", false);
         dialog.setDescription(text);
         dialog.setSize(320, 160);
         Point panelLocation = thisPanel.getLocationOnScreen();
         Dimension panelSize = thisPanel.getSize();
         Dimension dialogSize = dialog.getSize();
         if (dialogSize.height > panelSize.height)
            dialogSize.height = panelSize.height;
         if (dialogSize.width > panelSize.width)
            dialogSize.width = panelSize.width;
         dialog.setLocation((panelLocation.x+ ((panelSize.width - dialogSize.width) / 2)),
                     (panelLocation.y+ ((panelSize.height - dialogSize.height) / 2)));
         dialog.setVisible(true);
       }

        public void mouseReleased(MouseEvent e) {
         //System.out.println("MyMouseHandler: mouseReleased e=" + e + " popupTrigger=" + e.isPopupTrigger());
         if (e.isPopupTrigger()) {
           Point pt = e.getPoint();
           int rowIndex = featurePropTable.rowAtPoint(pt);
           int columnIndex = featurePropTable.columnAtPoint(pt);
           Object obj = featurePropTable.getValueAt(rowIndex, columnIndex);
           if (obj != null) showPopup(obj.toString());
         }
        }


      public void mouseClicked(MouseEvent e) {
        //System.out.println("MyMouseHandler: mouseClicked e=" + e + " numClicks=" + e.getClickCount());
        Point pt = e.getPoint();
        int rowIndex = featurePropTable.rowAtPoint(pt);

        if (featurePropModel.isLoaded()) {
           int lineItemIndexForRow = sorter.getIndexForRow(rowIndex);
           PropertyReport.ReportLineItem li = (PropertyReport.ReportLineItem)(featurePropModel.getPropertyReport().getLineItem(lineItemIndexForRow));

           Feature feature = (Feature)masterAxis.getGenomeVersion().getLoadedGenomicEntityForOid(li.getOid());
           if (feature != null) {
              if (e.isShiftDown())
                selectedFeature = selectedFeature.getRootFeature();
              else
                selectedFeature = feature;
              //System.out.println("rowSMListener: got valueChanged setBrowserLastSelectionMode=" + setBrowserLastSelectionMode + " event=" + e );                     if (setBrowserLastSelectionMode) {
              //System.out.println("rowSMListener: setting last selection to: " + selectedFeature.getDebugString() + " e.source=" + e.getSource());
              browserModel.setCurrentSelection(selectedFeature);
           }
        }
      }
    }


    /**
     * Private inner class for handling browser model events
     */
     private class MyBrowserModelListener extends BrowserModelListenerAdapter {
      public void browserMasterEditorEntityChanged(GenomicEntity masterEditorEntity) {
        if (DEBUG) System.out.println("FRV - browserMasterEditorEntityChanged masterEditorEntity = "+masterEditorEntity);
        if (masterEditorEntity == null) return;

        if ( masterAxis != masterEditorEntity ) {
          if (masterEditorEntity instanceof GenomicAxis) {
            masterAxis = (GenomicAxis)masterEditorEntity;
            clearView();
          }
        }
      }

      public void browserCurrentSelectionChanged(GenomicEntity newSelection) {
        if (newSelection==null) return;
        if (newSelection.equals(selectedFeature)) return; //the feature has already been selected

        boolean rowSelected = false;
        setBrowserLastSelectionMode = false;
        //System.out.println("FRV: browserLastSelectionChanged, newSelection = " + newSelection + "  setBrowserLastSelectionMode=" + setBrowserLastSelectionMode);

        if ( entityVisitor == null ) return;

        if (newSelection instanceof Feature) {
          //check the model map and select the row if found.
          Feature feature = (Feature)newSelection;
          selectedFeature = feature;
          int tabIndex = 0;
          for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(feature.getEnvironment())) {
              tabIndex = i;
              break;
            }
          }


          if (DEBUG) System.out.println("FRV: browserLastSelectionChanged, tabIndex = " + tabIndex);
          if ( tabIndex >= 0 ) {
              if (tabbedPane.getSelectedIndex() != tabIndex  && tabbedPane.getTabCount()!=0) {
                setSelectedTab(tabIndex);
                tabbedPane.setSelectedIndex(tabIndex);
              }
              int row = -1;
              try {
                row = sorter.getObjectRow(selectedFeature);
              }
              catch (Exception ex) {
                // The selectedFeature could not be found or there was some other problem
                // getting the row.
                row = -1;
              }
              if (DEBUG) System.out.println("FRV: browserLastSelectionChanged, row = " + row);
              if ( row >= 0 ) {
                if (row < featurePropTable.getRowCount()) {
                  featurePropTable.setRowSelectionInterval(row, row);
                  Rectangle rect = featurePropTable.getCellRect(row, 0, true);
                  featurePropTable.scrollRectToVisible(rect);
                  rowSelected = true;
                }
              }
          }
        }
        if (!rowSelected) featurePropTable.clearSelection();
      }


      public void browserSubViewFixedRangeChanged(Range newRange) {
        if (!lastFixedRange.equals(newRange)) {
          populateView();
          lastFixedRange=newRange.toMutableRange();
        }
      }


      public void browserClosing() {
        clearView();
        dispose();
      }
    }


      /**
      * Get the axis alignment for the specified feature.
      */
      private MutableRange getRange(AlignableGenomicEntity entity) {
        MutableRange mrng = null;
        if ( entity == null )
          return null;
        try {
          if (!(entity instanceof SingleAlignmentSingleAxis)) {
            // todo: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s).
            System.out.println("FeatureReportView: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s)");
            return null;
          }
          GeometricAlignment range = ((SingleAlignmentSingleAxis)entity).getOnlyGeometricAlignmentToOnlyAxis();
          if ( range == null ) {
            System.out.println("GenomicAxisAnnotationView: error no aligned ranges found for contig=" + entity);
            return null;
          }
          mrng = new MutableRange(range.getRangeOnAxis());
          //System.out.println("getRange.....mrng1 = "+mrng);
        }
        catch(IllegalStateException ex) {
          mrng = null;
          try { SessionMgr.getSessionMgr().handleException(ex); }
          catch (Exception e) { }
        }
        return mrng;
      }

    private class EntityVisitor extends GenomicEntityVisitor {
      private Set reportSet = new HashSet();
      private Set reportFeatureGroupSet = new HashSet();
      private HashMap featureGroupToModeMap = new HashMap();

      private void addFeature(Feature feature) {
        MutableRange subViewRange = browserModel.getSubViewFixedRange().toMutableRange();
        if (((Boolean)browserModel.getModelProperty(BrowserModel.REV_COMP_PROPERTY)).booleanValue())
          subViewRange.mirror(masterAxis.getMagnitude());
        MutableRange mrng = getRange(feature);
        if (mrng!=null && subViewRange.contains(mrng)) {
          reportSet.add(feature);
          reportFeatureGroupSet.add(feature.getEnvironment());
        }
      }

      public void visitHSPFeature(HSPFeature hspFeature){
        // For these features get the properties of the children.
        if (hspFeature.getEntityType().value() == EntityTypeConstants.Genewise_Peptide_Hit_Part) {
              featureGroupToModeMap.put(hspFeature.getEnvironment(), FeaturePropertyDataModel.GENEWISE_MODE);
              addFeature(hspFeature);
        }
        else if (hspFeature.getEntityType().value() == EntityTypeConstants.Sim4_Feature_Detail) {
              featureGroupToModeMap.put(hspFeature.getEnvironment(), FeaturePropertyDataModel.SIM4_MODE);
              addFeature(hspFeature);
        }
        else if (hspFeature.getEntityType().value() == EntityTypeConstants.High_Scoring_Pair) {
          featureGroupToModeMap.put(hspFeature.getEnvironment(), FeaturePropertyDataModel.BLAST_MODE);
          addFeature(hspFeature);
        }
		else if (hspFeature.getEntityType().value() == EntityTypeConstants.ESTMapper_Feature_Detail) {
		  featureGroupToModeMap.put(hspFeature.getEnvironment(), FeaturePropertyDataModel.EST_MAPPER_MODE);
		  addFeature(hspFeature);
		}
		else if (hspFeature.getEntityType().value() == EntityTypeConstants.Atalanta_Feature_Detail) {
		  featureGroupToModeMap.put(hspFeature.getEnvironment(), FeaturePropertyDataModel.ATALANTA_MODE);
		  addFeature(hspFeature);
		}
      }

      public Set getReportSet() { return reportSet; }

      public Set getReportFeatureGroupSet() { return reportFeatureGroupSet; }

      public HashMap getFeatureGroupToModeMap() { return featureGroupToModeMap; }

      public void printReportSet() {
          Set props;
          Feature feature;
          Object tmpObj;
          for (Iterator i = reportSet.iterator(); i.hasNext(); ) {
              tmpObj = i.next();
              if (tmpObj instanceof Feature) {
                  feature = (Feature)tmpObj;
                  props = feature.getProperties();
                  for (Iterator iter = props.iterator();iter.hasNext();) {
                      GenomicProperty tmpProperty = (GenomicProperty)iter.next();
                      System.out.print("Feature type=" + feature.getEnvironment() + "  (" + tmpProperty.getName() + ", " + tmpProperty.getInitialValue() + ")");
                  }
                  System.out.println("");
              }
              else {
                  System.out.println("printReportSet: Error object not a Feature obj=" + tmpObj);
              }
          } //end for
          System.out.flush();
      } //end method
    }

    private class DoubleRenderer extends DefaultTableCellRenderer {
      public DoubleRenderer() {
        super();
        setHorizontalAlignment(JLabel.RIGHT);
      }

       public void setValue(Object value) {
          if (value instanceof Double) {
             DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
             format.applyPattern("0.#####");
             super.setValue((value == null) ? "" : format.format(value));
          }
          else {
             super.setValue(value.toString());
          }
       }
    }
}
