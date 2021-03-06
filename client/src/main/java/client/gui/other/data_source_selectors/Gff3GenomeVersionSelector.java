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

//Title:        Your Product Name
//Version:
//AuthsetValueor:       Peter Davies
//Description:  This is the main Browser in the System

package client.gui.other.data_source_selectors;

import api.entity_model.access.filter.GenomeVersionCollectionFilter;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.facade_mgr.DataSourceSelector;
import api.facade.facade_mgr.FacadeManagerBase;
import client.gui.framework.browser.Browser;
import client.gui.framework.session_mgr.SessionMgr;
import client.shared.swing.table.SortButtonRenderer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;


public class Gff3GenomeVersionSelector extends GenomeVersionSelector implements DataSourceSelector {
  // These two variables go hand-in-hand.  You cache the selected genome version
  // ONLY IF the boolean is set true.  The selected genome version is only
  // to be non-null during calls to selectGenomeVersion(...filter).
  private GenomeVersion selectedGenomeVersion;
  private boolean returnSelectedVersion = false;

  public Gff3GenomeVersionSelector() {

	mainDialog = new JDialog();
	panel1 = new JPanel();
	borderLayout1 = new BorderLayout();
	buttonPanel = new JPanel();
	model=new Gff3GenomeVersionSelectorModel();
	table=new JTable();
	label=new JLabel("Please select a Species/Assembly combination");
	  
   try  {
      jbInit();
      mainDialog.pack();

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = mainDialog.getSize();
      if (frameSize.height > screenSize.height)
        frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width)
        frameSize.width = screenSize.width;
      mainDialog.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  public void selectDataSource(FacadeManagerBase facade) {

	  model.setFacadeManagerBase( facade );
      model.init();

      table.setModel(model);

      table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      table.getTableHeader().addMouseListener(model.constructColumnHeaderMouseListener(table));

      TableColumnModel columnModel = table.getColumnModel();
      final SortButtonRenderer headerRenderer = new SortButtonRenderer();
      int q = model.getColumnCount();
      for (int j = 0; j < q; j++) {
          columnModel.getColumn(j).setHeaderRenderer(headerRenderer);
      }

      // Listen to mouse press on the headers
      final JTableHeader header = table.getTableHeader();
      header.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          int col = header.columnAtPoint(e.getPoint());
          header.getTable().convertColumnIndexToModel(col);
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

      if (model.getRowCount()==1) {  //Don't popup dialog if only 1
           model.setValue(0);
           browser.getBrowserModel().setCurrentSelection(model.getGenomeVersion(0).getSpecies());
           dispose();
           return;
      }
      table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
      TableColumnModel tcm=table.getColumnModel();
      for (int i=0;i<model.getColumnCount();i++) {
        FontMetrics fm=table.getFontMetrics(table.getFont());
        tcm.getColumn(i).setPreferredWidth(model.getColumnWidthForFont(i,fm));
      }
      table.doLayout();

      // Turn off the default action for Enter.  Do not want to toggle down list.
      InputMap tmpMap = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      tmpMap = tmpMap.getParent();
      tmpMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
      if (model.getRowCount()>0) table.setRowSelectionInterval(0,0);
      mainDialog.setVisible(true);
  }

}
