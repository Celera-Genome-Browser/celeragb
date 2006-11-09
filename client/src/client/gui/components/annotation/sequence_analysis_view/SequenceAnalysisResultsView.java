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
/*
 */
package client.gui.components.annotation.sequence_analysis_view;

import api.entity_model.access.report.PropertyReport;
import api.entity_model.access.report.Report;
import api.entity_model.management.PropertyMgr;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.GenomicEntity;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.facade.abstract_facade.annotations.HSPFacade;
import api.stub.data.OID;
import api.stub.geometry.Range;
import client.gui.components.other.report.HTMLReportMenuItem;
import client.gui.components.other.report.HTMLViewable;
import client.gui.framework.browser.Browser;
import client.gui.framework.pref_controller.PrefController;
import client.gui.framework.roles.SubEditor;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.BrowserModelListener;
import client.gui.framework.session_mgr.BrowserModelListenerAdapter;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.other.xml.xml_writer.HitAlignmentGBFWriter;
import client.shared.swing.table.SortButtonRenderer;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class SequenceAnalysisResultsView extends JPanel implements SubEditor {
  private static final int TABLE_WIDTH = 150;

  private Browser browser;
  private BrowserModel browserModel;
  private BrowserModelListener browserModelListener = new MyBrowserModelListener();
  private JMenuItem clearTableMI = new JMenuItem("Clear Table");
  private HTMLReportMenuItem htmlReportMI;
  private JMenuItem editSettingsMI = new JMenuItem("Edit SubView Settings...");
  private JTable table;
  private MyTableModel model = new MyTableModel();
  private Range featureRange = null;
  private int sortCol = 0;
  private boolean sortAsc = true;

  // Model Items
  private PropertyReport propertyReport;
  private ArrayList lineItems = new ArrayList();
  private HashMap OIDToGBFMap = new HashMap();

  private int reportNumber = 0;
  private ArrayList reportNumbers = new ArrayList();
  private ArrayList tierNames = new ArrayList();
  private Map reportNumberVsTierName = new HashMap();
  private Vector data;
  private final String[] names={FeatureFacade.GENOMIC_AXIS_ID_PROP,
    FeatureFacade.GENOMIC_AXIS_NAME_PROP, FeatureFacade.AXIS_BEGIN_PROP,
    FeatureFacade.AXIS_END_PROP, HSPFacade.SUM_E_VAL_PROP,
    HSPFacade.BIT_SCORE_PROP, HSPFacade.INDIVIDUAL_E_VAL_PROP,
    HSPFacade.PERCENT_IDENTITY_PROP, "Tier"};


  //----------------------------------CONSTRUCTORS
  public SequenceAnalysisResultsView(Browser browser, Boolean masterBrowser) {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.browser = browser;
    browserModel = browser.getBrowserModel();
    this.setName("Sequence Analysis Results");
    // Place input and output components onto a scrollpane, and
    // add it to the view.
    table = new JTable(model);
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setViewportView(table);
    add(scrollPane);
    refreshTable();
    clearTableMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        model.clear();
        table.removeAll();
        refreshTable();
        SequenceAnalysisResultsView.this.browser.repaint();
      }
    });

    htmlReportMI = new HTMLReportMenuItem(new AlignmentReportGenerator());
    editSettingsMI.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PrefController.getPrefController().getPrefInterface(
          client.gui.other.panels.SequenceAnalysisResultsPanel.class,
          SequenceAnalysisResultsView.this.browser);
          SequenceAnalysisResultsView.this.browser.repaint();
      }
    });
    browserModel.addBrowserModelListener(browserModelListener);

    // Setup selection detection.
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.getTableHeader().addMouseListener(new ColumnListener(table));
    TableColumnModel columnModel = table.getColumnModel();
    final SortButtonRenderer headerRenderer = new SortButtonRenderer();
    int i = model.getColumnCount();
    for (int j = 0; j < i; j++) {
        columnModel.getColumn(j).setHeaderRenderer(headerRenderer);
    }

    // Listen to mouse press on the headers
    final JTableHeader header = table.getTableHeader();
    header.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        int col = header.columnAtPoint(e.getPoint());
        headerRenderer.setPressedColumn(col);
        headerRenderer.setSelectedColumn(col);
        header.repaint();

        if (header.getTable().isEditing()) {
          header.getTable().getCellEditor().stopCellEditing();
        }
      }

      public void mouseReleased(MouseEvent e) {
        // Clear the selection.  Otherwise the header column will be
        // toggled down
        headerRenderer.setPressedColumn(-1);
        header.repaint();
      }
    });


    ListSelectionModel selectionModel = table.getSelectionModel();
    if (selectionModel != null) {
      selectionModel.addListSelectionListener(new ReportSelectionListener());
    } // Got a selection model.
    table.setShowHorizontalLines(true);
    MyComparator comp = new MyComparator(sortCol,sortAsc);
    SequenceAnalysisResultsView.this.updateData();
    Collections.sort(data,comp);
    ((MyTableModel)table.getModel()).fireTableDataChanged();
    table.repaint();
  }


  //----------------------------------IMPLEMENTATION of Editor INTERFACE
  public JMenuItem[] getMenus() {
    return new JMenuItem[] { clearTableMI, htmlReportMI, editSettingsMI };
  }

  public void dispose() {
    browserModel.removeBrowserModelListener(browserModelListener);
  }

  private void updateData() {
    data=new Vector();
    for (Iterator it=lineItems.iterator();it.hasNext();) {
        data.add(it.next());
    }
    this.repaint();
  }


  //----------------------------------IMPLEMENTATION of SubEditor INTERFACE
  /** Is the entity something we can reasonably act on. */
  public boolean canEditThisEntity(GenomicEntity entity) { return true; }
  /**
   * Want the model property listener always on to hear about sequence analysis
   * results.
   */
  public void activate() {}
  public void passivate() { }


  private void refreshTable() {
    table.sizeColumnsToFit(-1);
    table.revalidate();
    table.repaint();
  }


  /** Picks up user selections of table row and sets browser selection. */
  class ReportSelectionListener implements ListSelectionListener {
    /** Called by table when user changes what they select. */
    public void valueChanged(ListSelectionEvent lse) {
      //Ignore extra messages.
      if (lse.getValueIsAdjusting()) return;

      ListSelectionModel lsm = (ListSelectionModel)lse.getSource();
      if (! lsm.isSelectionEmpty()) {
        int selectedRow = lsm.getMinSelectionIndex();
        if (model != null) {
          GenomicAxis tmpAxis = (GenomicAxis)browserModel.getMasterEditorEntity();
          PropertyReport.ReportLineItem tmpLineItem = (PropertyReport.ReportLineItem)lineItems.get(selectedRow);
          GenomicEntity feature = (GenomicEntity)tmpAxis.getGenomeVersion().getLoadedGenomicEntityForOid(tmpLineItem.getOid());
          if (feature != null) {
            // Try to move the browser to the selected item.
            browserModel.setCurrentSelection(feature);
          }
        }
      }
    }
  }


  private class MyBrowserModelListener extends BrowserModelListenerAdapter {
    public void modelPropertyChanged(Object key, Object oldValue, Object newValue) {
      if (key.equals("SequenceAnalysisResults")) {
        model.addAnalysisReport((PropertyReport)newValue);
        refreshTable();
      }
    }
  }


  private class MyFileFilter extends javax.swing.filechooser.FileFilter {
    public boolean accept(File pathname) {
      if (pathname.toString().endsWith(".gbf") ||
          pathname.isDirectory()) return true;
      else return false;
    }

    public String getDescription() { return "Genome Browser Feature Files"; }
  }

  /**
   * MouseListener for mouse clicks on column headers to sort the table
   */
  class ColumnListener extends MouseAdapter {
    protected JTable table;

    public ColumnListener(JTable table) {
      this.table = table;
    }

    public void mouseClicked(MouseEvent e) {
      // Figure out which column header was clicked and sort on that column
      String rowName = new String("");
      int targetRow = table.getSelectedRow();
      if (targetRow >= 0 && targetRow < table.getRowCount())
        rowName = ((PropertyReport.ReportLineItem)lineItems.get(targetRow)).getOid().toString();

      TableColumnModel colModel = table.getColumnModel();
      int colModelIndex = colModel.getColumnIndexAtX(e.getX());
      int modelIndex = colModel.getColumn(colModelIndex).getModelIndex();
      if (modelIndex < 0)
        return;
      if (sortCol==modelIndex)
        sortAsc = !sortAsc;
      else
        sortCol = modelIndex;
      // Redraw Header
      for (int i=0; i<names.length; i++) {
        TableColumn column = colModel.getColumn(i);
        column.setHeaderValue(table.getModel().getColumnName(column.getModelIndex()));
      }
      table.getTableHeader().repaint();
      ((MyTableModel)table.getModel()).sortAndScrollToItem(rowName);
    }
  }

  /**
   * Comparator to sort columns
   */
  class MyComparator implements Comparator {
    protected int sortCol;
    protected boolean sortAsc;

    public MyComparator(int sortCol, boolean sortAsc) {
      this.sortCol = sortCol;
      this.sortAsc = sortAsc;
    }

    public int compare(Object o1, Object o2) {
      Object compObj1, compObj2;
      int retVal;
      try {
        compObj1 = getValueAt((PropertyReport.ReportLineItem)o1,sortCol);
        compObj2 = getValueAt((PropertyReport.ReportLineItem)o2,sortCol);
        if (compObj1==null || compObj2==null) return 0;
        retVal = 0;
        if (compObj1 instanceof Comparable && compObj2 instanceof Comparable) {
          Comparable c1 = (Comparable)compObj1;
          Comparable c2 = (Comparable)compObj2;
          if (c1 instanceof String && c2 instanceof String) {
            String s1 = (String)c1;
            String s2 = (String)c2;
            retVal = sortAsc ? s1.compareToIgnoreCase(s2) : s2.compareToIgnoreCase(s1);
          }
          else retVal = sortAsc ? c1.compareTo(c2) : c2.compareTo(c1);
        }
        else if (compObj1 == null && compObj2 != null)
            retVal = sortAsc ? -1 : 1;
        else if (compObj2 == null && compObj1 != null)
            retVal = sortAsc ? 1 : -1;
      }
      catch (Exception ex) {
        System.out.println("Exception in Comparator");
        return 0;
      }
      return retVal;
    }

    protected Object getValueAt(PropertyReport.ReportLineItem lineItem, int col) {
      //System.out.println("getValueAt...numRows="+getRowCount()+" row="+row+" col="+col);

      if (col >= names.length) return null;

      Collection props = (lineItem).getFields();
      if (col < props.size()) {
        for (Iterator it = props.iterator();it.hasNext();) {
          String tmpProps = (String)it.next();
          if (tmpProps.equals(names[col])) {
            String propVal = (String)lineItem.getValue(tmpProps);
            if (propVal==null) return new String("");
            try { //try as an integer
              //System.out.println("parseing " + propVal + " as Integer");
              Integer val = Integer.valueOf(propVal);
              return val;
            }
            catch (NumberFormatException e_int) {
              try {
                Double val = Double.valueOf(propVal);
                //System.out.println("parsed " + propVal + " as Double");
                if (col == 7) {
                  return new java.lang.Float(propVal);
                }
                return val;
              } catch (NumberFormatException e_double) { }
            }
            //System.out.println("parsed " + propVal + " as String");
            return propVal;
          }
        } //end for
      }
      return null;
     }
  }

  private class MyTableModel extends AbstractTableModel {
    /** Accumulates report data. */
    public void addAnalysisReport(PropertyReport report) {
      reportNumber++;
      Integer reportInteger = new Integer(reportNumber);
      /**
       * @todo Need to fix the tier name for these features that are returned
       */
      //reportNumberVsTierName.put(reportInteger, currentTierName);

      for (int i = 0; i < report.getLineItems().length; i++) {
        lineItems.add(report.getLineItems()[i]);
        reportNumbers.add(reportInteger);
      } // For all report line items.
      updateData();
      fireTableDataChanged();

      if (SessionMgr.getSessionMgr().getModelProperty("CacheSequenceAnalysis") != null &&
        ((Boolean)SessionMgr.getSessionMgr().getModelProperty("CacheSequenceAnalysis")).booleanValue()) {
          if (report.getLineItems() == null || report.getLineItems().length == 0) return;
          try {
            GenomicAxis masterAxis = (GenomicAxis)browserModel.getMasterEditorEntity();
            String tmpSpecies = masterAxis.getGenomeVersion().getSpecies().toString();
            long assemblyVersion = masterAxis.getGenomeVersion().getVersion();
            String tmpDir = (String)SessionMgr.getSessionMgr().getModelProperty("SequenceAnalysisCacheDirectory");
            if (tmpDir==null || tmpDir.equals("")) {
              tmpDir = new String(System.getProperty("user.home")+File.separator+
                "x"+File.separator+"GenomeBrowser"+File.separator+"AnalysisCache");
            }

            File tmpDirFile = new File(tmpDir);
            if (!tmpDirFile.exists()) tmpDirFile.createNewFile();

            String userName = (String)SessionMgr.getSessionMgr().getModelProperty(SessionMgr.USER_NAME);
            String timestamp = (new Long(System.currentTimeMillis())).toString();
            String fileName = tmpDir + File.separator + userName + timestamp + ".gbf";
            // Get all of the composite features.  Some will be shared so
            // the same key will be overridden instead of
            ArrayList tmpParentFeatures = new ArrayList();

            for (int i = 0; i < report.getLineItems().length; i++ ) {
              OID tmpOID = ((PropertyReport.ReportLineItem)report.getLineItem(i)).getOid();
              Feature tmpFeat = (Feature)masterAxis.getGenomeVersion().getGenomicEntityForOid(tmpOID);
              if (!tmpParentFeatures.contains(tmpFeat.getRootFeature()))
                tmpParentFeatures.add(tmpFeat.getRootFeature());
            }
            new HitAlignmentGBFWriter(tmpSpecies, assemblyVersion, fileName, tmpParentFeatures);
        }
        catch (Exception ex) {
          SessionMgr.getSessionMgr().handleException(ex);
        }
      }
    } // End method


    /**
     * This method is to help clear out the table upon user demand.
     */
    public void clear() {
      data.clear();
      lineItems.clear();
      reportNumbers.clear();
      tierNames.clear();
      reportNumberVsTierName.clear();
    }

    public int getRowCount() {
      return lineItems.size();
    }

    public int getColumnCount() { return names.length; }

    public String getColumnName(int columnIndex) {
      String retVal = PropertyMgr.getPropertyMgr().getPropertyDisplayName(names[columnIndex]);
      return retVal;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }

    public Object getValueAt(int rowIndex, int columnIndex) {
      try {
        PropertyReport.ReportLineItem reportLineItem = (PropertyReport.ReportLineItem)lineItems.get(rowIndex);
        Integer reportNumberOfItem = (Integer)reportNumbers.get(rowIndex);
        if (rowIndex == 8) return reportNumberVsTierName.get(reportNumberOfItem);
        return reportLineItem.getValue(names[columnIndex]);
      }
      catch (Exception ex) {
        return "Exception";
      }
    }

    public void sortAndScrollToItem(String rowName) {
      MyComparator comp = new MyComparator(sortCol,sortAsc);
      Collections.sort(data,comp);
      ((MyTableModel)table.getModel()).fireTableDataChanged();
      if (!rowName.equals("")) {
        for (int x=0; x<table.getRowCount();x++) {
          if (((String)table.getModel().getValueAt(x,0)).equals(rowName)) {
            table.setRowSelectionInterval(x,x);
            Rectangle rect = table.getCellRect(x, 0, true);
            table.scrollRectToVisible(rect);
            table.repaint();
            return;
          }
        }
      }
      table.repaint();
    }
  }

    /** Builds a report given knowledge of sequences in the outer class. */
    private class AlignmentReportGenerator implements HTMLViewable {
        public String getCurrentReportName() {
            return getName()+" Report";
        }

        /**
         * Builds HTML report.
         */
        public Report getCurrentReport() {
          PropertyReport tmpReport = new PropertyReport();
          for (Iterator it = lineItems.iterator(); it.hasNext(); ) {
            tmpReport.addLineItem((PropertyReport.ReportLineItem)it.next());
          }
          return tmpReport;
        }
    }

} // End class
