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
package client.gui.application.game_viewer;

import client.shared.file_chooser.FileChooser;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;

public class MainFrame extends JFrame {
  JPanel contentPane;
  JMenuBar jMenuBar1 = new JMenuBar();
  JMenu jMenuFile = new JMenu();
  JMenu jGenomeBrowser = new JMenu();
  JMenuItem jMenuFileExit = new JMenuItem();
  JMenu jMenuHelp = new JMenu();
  JMenuItem jMenuHelpAbout = new JMenuItem();
  JLabel statusBar = new JLabel();
  JSplitPane jSplitPane1 = new JSplitPane();
  JScrollPane jScrollPane1 = new JScrollPane();
  XMLTreeModel treeModel=new XMLTreeModel();
  XMLTableModel xmlTableModel=new XMLTableModel();
  PropertiesTableModel propertiesTableModel=new PropertiesTableModel();
  JTree tree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode()));
  JMenuItem openMenuItem = new JMenuItem();
  JMenuItem navigateMenuItem = new JMenuItem();
  JMenuItem closeMenuItem = new JMenuItem();
  JSplitPane jSplitPane2 = new JSplitPane();
  JScrollPane jScrollPane2 = new JScrollPane();
  JScrollPane jScrollPane3 = new JScrollPane();
  JTable xmlTable = new JTable(xmlTableModel);
  JTable propertiesTable = new JTable(propertiesTableModel);
  File currentDirectory;
  boolean gbIntegration;

  /**Construct the frame*/
  public MainFrame() {
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**Construct the frame*/
  public MainFrame(String fileName, boolean gbIntegration) {
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
      this.gbIntegration=gbIntegration;
      jbInit();
      /**
       * If this is called from the GB and the file is null then do not throw
       * an exception.  Let the user open any Game XML file.
       */
      if (fileName==null) {
        return;
      }
      else openFile(new File(fileName));
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**Component initialization*/
  private void jbInit() throws Exception  {

// JCVI LLF: 10/19/2006
		// RT 10/27/2006
	this.setIconImage(new ImageIcon(MainFrame.class.getResource("/resource/client/images/small_gb.gif")
      ).getImage());
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setCellRenderer(new TreeRenderer());
    contentPane = (JPanel) this.getContentPane();
    this.setSize(new Dimension(765, 365));
    this.setTitle("Genomics Exchange File Viewer");
    statusBar.setText(" ");
    statusBar.setBounds(new Rectangle(0, 276, 400, 24));
    jMenuFile.setText("File");
    jMenuFileExit.setText("Exit");
    jMenuFileExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(88, java.awt.event.KeyEvent.ALT_MASK, false));
    jMenuFileExit.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
        jMenuFileExit_actionPerformed(e);
      }
    });

    jGenomeBrowser.setText("Genome Browser");
    navigateMenuItem.setText("Navigate");
    navigateMenuItem.setEnabled(false);
    navigateMenuItem.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
       navigate_actionPerformed(e);
      }
    });

    jMenuHelp.setText("Help");
    jMenuHelpAbout.setText("About");
    jMenuHelpAbout.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
        jMenuHelpAbout_actionPerformed(e);
      }
    });
    jSplitPane1.setBounds(new Rectangle(3, 3, 393, 268));
    openMenuItem.setText("Open");
    openMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openMenuItem_actionPerformed(e);
      }
    });
    closeMenuItem.setText("Close");
    closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closeMenuItem_actionPerformed(e);
      }
    });
    closeMenuItem.setEnabled(false);
    tree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        tree_valueChanged(e);
      }
    });
    jSplitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
    jSplitPane2.setLastDividerLocation(150);
    jMenuFile.add(openMenuItem);
    jMenuFile.add(closeMenuItem);
    jMenuFile.addSeparator();

    jMenuFile.add(jMenuFileExit);
    jMenuHelp.add(jMenuHelpAbout);
    jGenomeBrowser.add(navigateMenuItem);
    jMenuBar1.add(jMenuFile);
    if (gbIntegration) {
       jMenuBar1.add(jGenomeBrowser);
       tree.addTreeSelectionListener(new MyTreeSelectionListener());
    }
    jMenuBar1.add(jMenuHelp);
    this.setJMenuBar(jMenuBar1);
    contentPane.add(statusBar, null);
    contentPane.add(jSplitPane1, null);
    jSplitPane1.add(jScrollPane1, JSplitPane.LEFT);
    jSplitPane1.add(jSplitPane2, JSplitPane.RIGHT);
    jSplitPane2.add(jScrollPane2, JSplitPane.TOP);
    jScrollPane2.getViewport().add(xmlTable, null);
    jSplitPane2.add(jScrollPane3, JSplitPane.BOTTOM);
    jScrollPane3.getViewport().add(propertiesTable, null);
    jScrollPane1.getViewport().add(tree, null);
    jSplitPane1.setDividerLocation(300);
    jSplitPane2.setDividerLocation(120);
  }
  /**File | Exit action performed*/
  public void jMenuFileExit_actionPerformed(ActionEvent e) {
    close();
  }
  /**Help | About action performed*/
  public void jMenuHelpAbout_actionPerformed(ActionEvent e) {
    MainFrame_AboutBox dlg = new MainFrame_AboutBox(this);
    Dimension dlgSize = dlg.getPreferredSize();
    Dimension frmSize = getSize();
    Point loc = getLocation();
    dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
    dlg.setModal(true);
    dlg.setVisible(true);
  }
  /**Overridden so we can exit when window is closed*/
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      jMenuFileExit_actionPerformed(null);
    }
  }

  void navigate_actionPerformed(ActionEvent e){
    String oid=getCurrentOid();
    if (oid==null || oid.equals("")) {
       JOptionPane.showMessageDialog(this,"The Node selected does not have an OID to send to the Genome Browser");
       return;
    }
    try {
       URL url=new URL("http://localhost:30000/?action=search&unknown_oid="+oid+"&redir=204");
       url.getContent();
    }
    catch (Exception ex) {
      JOptionPane.showMessageDialog(this,"Error: Could not send URL to Genome Browser");
    }
  }

  void openMenuItem_actionPerformed(ActionEvent e) {
     JFileChooser chooser;
     if (currentDirectory!=null) chooser= new FileChooser(currentDirectory);
     else chooser=new FileChooser();
     chooser.setFileFilter(new MyFileFilter());
     int answer=chooser.showOpenDialog(this);
     if (answer==JFileChooser.CANCEL_OPTION) return;
     File file=chooser.getSelectedFile();
     currentDirectory=chooser.getCurrentDirectory();
     openFile(file);
  }

  void openFile(File file) {
     XMLParser parser=new XMLParser(file);
     treeModel=new XMLTreeModel();
     try {
         treeModel.setDocument(parser.getDocument());
         tree.setModel(treeModel);
     }
     catch (Exception ex) {
        String [] messages=new String[3];
        messages[0]="This application cannot parse the .gbw file choosen.";
        messages[1]= "Exception class: "+ex.getClass().getName();
        if (ex.getMessage()!=null) {
           messages[2]="Exception Message: "+ex.getMessage();
        }
        JOptionPane.showMessageDialog(this,messages,
           "Error!!",JOptionPane.ERROR_MESSAGE);
        if (!gbIntegration) reset();
        this.repaint();
        return;
     }
     openMenuItem.setEnabled(false);
     closeMenuItem.setEnabled(true);
     this.repaint();
  }

  void tree_valueChanged(TreeSelectionEvent e) {
     DefaultMutableTreeNode selected=((DefaultMutableTreeNode)e.getPath().getLastPathComponent());
     if (selected instanceof AxisXMLTreeNode) {
       xmlTableModel.setDOMObject (null);
       propertiesTableModel.setDOMObject (null);
     }
     xmlTableModel.setDOMObject (selected.getUserObject());
     propertiesTableModel.setDOMObject (selected.getUserObject());
  }

  void closeMenuItem_actionPerformed(ActionEvent e) {
    reset();
  }

  void reset () {
     treeModel=new XMLTreeModel();
     tree.setModel(treeModel);
     xmlTableModel=new XMLTableModel();
     xmlTable.setModel(xmlTableModel);
     propertiesTableModel=new PropertiesTableModel();
     propertiesTable.setModel(propertiesTableModel);
     openMenuItem.setEnabled(true);
     closeMenuItem.setEnabled(false);
  }

  void close() {
     if (!gbIntegration) System.exit(0);
     else {
        this.setVisible(false);
        this.dispose();
     }
  }

  String getCurrentOid() {
     if (tree.isSelectionEmpty()) return "";
     Object selection=tree.getSelectionPath().getLastPathComponent();
     if (selection ==null || !(selection instanceof XMLTreeNode)) return "";
     return ((XMLTreeNode)selection).getOid();
  }

  class MyFileFilter extends javax.swing.filechooser.FileFilter {
         public boolean accept(File pathname){
            if (pathname.getPath().toLowerCase().endsWith(".gbw") ||
               pathname.isDirectory()) return true;
            return false;
         }

         public String getDescription(){
           return "(*.gbw) Genomics Exchange Format Files";
         }
  }

  class MyTreeSelectionListener implements TreeSelectionListener {
      public void valueChanged(TreeSelectionEvent e){
         if (e.getNewLeadSelectionPath() == null) return;
         Object object=e.getNewLeadSelectionPath().getLastPathComponent();
         if (object instanceof XMLTreeNode) {
            String oid=((XMLTreeNode)object).getOid();
            if (oid==null || oid=="") navigateMenuItem.setEnabled(false);
            else navigateMenuItem.setEnabled(true);
         }
         else navigateMenuItem.setEnabled(false);
      }
  }
}
