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

//Title:        Selector for Genome Versions found in .gba files.
//Version:
//Author:       Les Foster
//Company:      []
//Description:  *** Temporary Solution to Multiple In Use Problem ***

package client.gui.other.data_source_selectors;

import api.facade.facade_mgr.DataSourceSelector;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class XmlGenomeVersionSelector extends JFrame implements DataSourceSelector{
  JDialog mainDialog = new JDialog();
  JPanel panel1 = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel buttonPanel = new JPanel();
  XmlGenomeVersionSelectorModel model=new XmlGenomeVersionSelectorModel();
  JTable table=new JTable();
  JLabel label=new JLabel("Please select a Species/Assembly combination (XML)");
  JButton ok;

  public XmlGenomeVersionSelector() {
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

      String protocolToAdd = new String(
          FacadeManager.getProtocolForFacade(api.facade.concrete_facade.xml.XmlGenomicAxisFacadeManager.class));
      FacadeManager.addProtocolToUseList(protocolToAdd);
      model.init();

      table.setModel(model);
      if (model.getRowCount()==1) {  //Don't popup dialog if only 1
           model.setValue(0);
           dispose();
           return;
      }
      table.sizeColumnsToFit(-1);
      table.setRowHeight(table.getRowHeight()); //dumb trick to force a resize and repaint
      mainDialog.setVisible(true);
  }


  void jbInit() throws Exception {
    mainDialog = new JDialog(this);
    mainDialog.setTitle("Selection Species/Assembly");
    mainDialog.setModal(true);
    this.setIconImage((new ImageIcon(this.getClass().getResource(System.getProperty("x.genomebrowser.WindowCornerLogo"))).getImage()));
    table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    DefaultTableCellRenderer dcr= new DefaultTableCellRenderer();
    dcr.setHorizontalAlignment(SwingConstants.CENTER);

    table.setDefaultRenderer(Object.class,dcr);

    table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
           public void valueChanged(ListSelectionEvent e){
               ok.setEnabled(true);
               table.getSelectionModel().removeListSelectionListener(this);
           }
    });
    panel1.setLayout(new BoxLayout(panel1,BoxLayout.Y_AXIS));

    buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
    ok=new JButton("OK");
    ok.setEnabled(false);
    ok.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
           setVisible(false);
           String protocolToAdd = new String(
            FacadeManager.getProtocolForFacade(api.facade.concrete_facade.xml.XmlGenomicAxisFacadeManager.class));
           model.setValue(table.getSelectedRow());
           FacadeManager.addProtocolToUseList(protocolToAdd);
           dispose();
        }
    });
    buttonPanel.add(Box.createGlue());
    buttonPanel.add(ok);

    buttonPanel.add(Box.createHorizontalStrut(20));

    JButton cancel=new JButton("Cancel");
    cancel.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
           setVisible(false);
           String protocolToRemove = new String(
            FacadeManager.getProtocolForFacade(api.facade.concrete_facade.xml.XmlGenomicAxisFacadeManager.class));
           FacadeManager.removeProtocolFromUseList(protocolToRemove);
           dispose();
        }
    });
    buttonPanel.add(cancel);
    buttonPanel.add(Box.createGlue());
    panel1.add(Box.createVerticalStrut(15));
    JPanel labelPanel = new JPanel();
    labelPanel.add(label);
    panel1.add(labelPanel);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    table.setPreferredScrollableViewportSize(new Dimension((int)(screenSize.getWidth()*.9),150));
    panel1.add(Box.createVerticalStrut(15));
    JScrollPane jsp=new JScrollPane(table);
    table.validate();
    panel1.add(jsp);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    panel1.add(Box.createVerticalStrut(15));
    panel1.add(buttonPanel);
    panel1.add(Box.createVerticalStrut(15));
    mainDialog.getContentPane().add(panel1);
  }

}

