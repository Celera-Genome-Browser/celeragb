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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;


public class GenomeVersionSelector extends JFrame implements DataSourceSelector{
  JDialog mainDialog = new JDialog();
  JPanel panel1 = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel buttonPanel = new JPanel();
  GenomeVersionSelectorModel model=new GenomeVersionSelectorModel();
  JTable table=new JTable();
  JLabel label=new JLabel("Please select a Species/Assembly combination");
  JButton ok;
  Browser browser;

  // These two variables go hand-in-hand.  You cache the selected genome version
  // ONLY IF the boolean is set true.  The selected genome version is only
  // to be non-null during calls to selectGenomeVersion(...filter).
  private GenomeVersion selectedGenomeVersion;
  private boolean returnSelectedVersion = false;

  public GenomeVersionSelector() {
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

  public void setDataSource(FacadeManagerBase facade, Object dataSource){}

  public void selectDataSource(FacadeManagerBase facade) {

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

  /**
   * Given a filter, collect all matching genome versions, present them to
   * the user, and let them pick one.
   */
  public GenomeVersion selectDataSource(GenomeVersionCollectionFilter filter) {

      GenomeVersion returnVersion = null;
      returnSelectedVersion = true;

      // Build model with only filtered versions.
      model.init(filter);

      table.setModel(model);
      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      table.getTableHeader().addMouseListener(model.constructColumnHeaderMouseListener(table));
      table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

      if (model.getRowCount() == 1) {
        // No need for dialog.
        model.setValue(0);
        returnVersion = model.getGenomeVersion(0);
      } // Exactly one availabe GV matches filter.
      else if (model.getRowCount() == 0) {
        returnVersion = null;
      } // None
      else {
        TableColumnModel tcm=table.getColumnModel();
        for (int i=0;i<model.getColumnCount();i++) {
          FontMetrics fm=table.getFontMetrics(table.getFont());
          tcm.getColumn(i).setPreferredWidth(model.getColumnWidthForFont(i,fm));
        }
        table.doLayout();

        mainDialog.setVisible(true);

        returnVersion = selectedGenomeVersion;

        selectedGenomeVersion = null;
      } // multiple matches.

      dispose();

      returnSelectedVersion = false;
      return returnVersion;
  } // End method


  void jbInit() throws Exception {
    browser = SessionMgr.getSessionMgr().getActiveBrowser();
    mainDialog = new JDialog(browser, "Selection Species/Assembly", true);
    DefaultTableCellRenderer dcr= new DefaultTableCellRenderer();
    dcr.setHorizontalAlignment(SwingConstants.CENTER);

    table.setDefaultRenderer(Object.class,dcr);

    table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
           public void valueChanged(ListSelectionEvent e){
               int[] rows=table.getSelectedRows();
               for (int i=0;i<rows.length;i++) {
                if (!model.canSetValue(rows[i])) {
                  ok.setEnabled(false);
                  return;
                }
               }
               ok.setEnabled(true);
           }
    });

    table.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if(e.getClickCount() == 2) {
           if (model.canSetValue(table.getSelectedRow())) {
             mainDialog.setVisible(false);
             model.setValue(table.getSelectedRow());
             browser.getBrowserModel().setCurrentSelection(model.getGenomeVersion(table.getSelectedRow()).getSpecies());
             if (returnSelectedVersion)
               selectedGenomeVersion = model.getGenomeVersion(table.getSelectedRow());
             dispose();
           }
        }
      }
    });

    panel1.setLayout(new BorderLayout(5,15));

    buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
    ok=new JButton("OK");
    ok.setEnabled(false);
    ok.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
           mainDialog.setVisible(false);
           int[] rows=table.getSelectedRows();
           for (int i=0;i<rows.length;i++) {
              model.setValue(rows[i]);
              if (returnSelectedVersion)
                 selectedGenomeVersion = model.getGenomeVersion(table.getSelectedRow());
           }
           browser.getBrowserModel().setCurrentSelection(model.getGenomeVersion(rows[rows.length-1]).getSpecies());
           dispose();
        }
    });
    buttonPanel.add(Box.createGlue());
    buttonPanel.add(ok);

    buttonPanel.add(Box.createHorizontalStrut(20));

    JButton cancel=new JButton("Cancel");
    cancel.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
           mainDialog.setVisible(false);
           dispose();
        }
    });
    buttonPanel.add(cancel);
    buttonPanel.add(Box.createGlue());
    JPanel labelPanel = new JPanel();
    labelPanel.add(label);
    panel1.add(labelPanel,BorderLayout.NORTH);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    table.setPreferredScrollableViewportSize(new Dimension((int)(screenSize.getWidth()*.9),150));
    JScrollPane jsp=new JScrollPane(table);
    table.validate();
    panel1.add(jsp,BorderLayout.CENTER);
    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);;
    panel1.add(buttonPanel,BorderLayout.SOUTH);
    mainDialog.getContentPane().add(panel1);
    mainDialog.getRootPane().setDefaultButton(ok);
    table.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent e){
        if (e.getKeyChar()=='\n') {
          ok.doClick();
        }
      }
      public void keyPressed(KeyEvent e){}
      public void keyReleased(KeyEvent e){}
    });
  }

}
