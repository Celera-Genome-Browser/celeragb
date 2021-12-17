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

package client.gui.framework.inspector;

import api.entity_model.management.PropertyMgr;
import api.entity_model.model.fundtype.GenomicEntity;
import api.stub.data.GenomicProperty;
import client.gui.framework.ics_tabpane.ICSTabPane;
import client.gui.framework.roles.PropertyViewer;
import client.gui.other.util.ClipboardUtils;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
* This class has 1 key behavioural difference from the JTable.  The
* JTable is designed to have 1 editor per column using the "columnClass"
* to return the class of the column and thereby getting the proper editor.
* Since we want to display different classes within a column, this has been
* overridden by overridding the getCellEditor and getCellRenderer methods.
*
*/
public class Inspector extends JTable implements PropertyViewer,ClipboardOwner {

  PropertiesTableModel model;
  ICSTabPane icsTabPane;
  Inspector childInspector;
  JScrollPane jsp;

  public Inspector(ICSTabPane icsTabPane) {
      this.icsTabPane=icsTabPane;
      this.setTransferHandler(null);
      this.setFocusTraversalPolicy(null);
      super.setModel(model=new PropertiesTableModel());
      setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
      getTableHeader().setReorderingAllowed(false);
      this.setEditingColumn(1);
      this.setDefaultRenderer(URLProperty.class,new GenomicPropertyRenderer());
      this.setDefaultEditor(URLProperty.class,new URLEditor());
      this.setDefaultRenderer(GenomicProperty.class,new GenomicPropertyRenderer());
      this.setDefaultRenderer(String.class,new StringRenderer());
      this.setDefaultRenderer(SelectableProperty.class,new SelectableRenderer());
      this.setDefaultEditor(GenomicProperty.class,new GenomicPropertyEditor());
      this.setDefaultEditor(SelectableProperty.class,new ComboEditor());
      this.setDefaultEditor(ExternallyEditedProperty.class,new ExternalEditor(icsTabPane));
      this.setDefaultRenderer(ExternallyEditedProperty.class,new GenomicPropertyRenderer());
      this.setDefaultRenderer(ExpandableProperty.class,new GenomicPropertyRenderer());
      this.setDefaultEditor(ExpandableProperty.class,new ExpandableEditor());
      this.setCellSelectionEnabled(true);
      this.addKeyListener(new KeyListener() {
         public void keyTyped(KeyEvent e){
         }
         public void keyPressed(KeyEvent e){
             if (e.getKeyCode()==KeyEvent.VK_C && e.isControlDown()  && !isEditing()) {
               String tmpCellValue = new String(model.getStringValueAt(getSelectedRow(),getSelectedColumn()));
               ClipboardUtils.setClipboardContents(tmpCellValue.toString());
             }
         }
         public void keyReleased(KeyEvent e){
         }
      });
      icsTabPane.addInspector(this);
      model.setCurationState(icsTabPane.getCurationState());
}

  public void lostOwnership (Clipboard clip, Transferable trans) {}

  public Inspector addChildInspector(GenomicProperty property) {
     if (property==null) return null;
     removeChildInspector();
     removeEditor();
     childInspector=new Inspector(icsTabPane);
     JScrollPane inspectorPane=childInspector.getScrollPane();
     childInspector.setModel(model.getGenomicEntity(),property);
     String displayName = PropertyMgr.getPropertyMgr().getPropertyDisplayName(property.getName());
     icsTabPane.addTab(displayName,null,inspectorPane,
      displayName+" of "+model.getGenomicEntity());
     icsTabPane.setSelectedComponent(inspectorPane);
     return  childInspector;
  }

  //Overridden to return different editors per column
    public TableCellEditor getCellEditor(int row, int column) {
        // If a cell is selected, cancel the editing before moving on.
        TableCellEditor tmpEditor = this.getCellEditor();
        if (tmpEditor!=null) {
        	tmpEditor.cancelCellEditing();
        }
        TableColumn tableColumn = getColumnModel().getColumn(column);
        TableCellEditor editor = tableColumn.getCellEditor();
        if (editor == null) {
            editor = getDefaultEditor(model.getValueAt(row,column).getClass());
        }
        return editor;
    }

  //Overridden to return different renderers per column
    public TableCellRenderer getCellRenderer(int row, int column) {
        TableColumn tableColumn = getColumnModel().getColumn(column);
        TableCellRenderer renderer = tableColumn.getCellRenderer();
        if (renderer == null) {
            renderer = getDefaultRenderer(model.getValueAt(row,column).getClass());
        }
        return renderer;
    }

  /**
  * Recursive removal of child inspectors.
  */
  public void removeChildInspector() {
     if (childInspector!=null) {
       childInspector.removeChildInspector();
       icsTabPane.remove(childInspector.getScrollPane());
       icsTabPane.removeInspector(childInspector);
       childInspector=null;
     }
  }

  public JScrollPane getScrollPane() {
    if (jsp==null) jsp=new JScrollPane(this);
    return jsp;
  }

  public void setModel(Object model) {
    if (this.isEditing()) {
       cellEditor.cancelCellEditing();
    }
    if (!(model instanceof GenomicEntity) && model!=null) return;
    removeChildInspector();
    removeEditor();
    //This is ugly, but apparently the only way to modify a tool tip text
    icsTabPane.remove(this.getScrollPane());
    icsTabPane.validate();
    String inspectorString;
    if (model==null || model.toString().equals("")) inspectorString="No Entity Selected ";
    else inspectorString=model.toString();
    icsTabPane.addTab(inspectorString,null,this.getScrollPane(),inspectorString);

    GenomicEntity newSelection=(GenomicEntity) model;
    this.model.setGenomicEntity(newSelection);
    sizeColumnsToFit(-1);
    resizeAndRepaint();
  }

  /**
   * It would appear that this method is only used by the Inspector when it creates
   * a child Inspector for an expanded Genomic Property, like curation flags and comments.
   */
  private void setModel(GenomicEntity entity, GenomicProperty property) {
    this.model.setGenomicEntityAndProperty(entity,property);
    sizeColumnsToFit(-1);
    resizeAndRepaint();
  }

  public void tableChanged(TableModelEvent e) {
     TableCellEditor tmpEditor = this.getCellEditor();
     if (tmpEditor!=null) {
     	tmpEditor.cancelCellEditing();
     }
     sizeColumnsToFit(-1);
     super.tableChanged(e);
     resizeAndRepaint();
  }

  /**
   * Pass through method to the model so PropertyTableModel will know the curation state.
   */
  public void setCurationChanged(boolean state) {
    model.setCurationState(state);
  }
  
  // Overriding method for Java 1.4 focus traversal policy change
  public boolean isFocusCycleRoot() {
	  return true;
  }  
}
