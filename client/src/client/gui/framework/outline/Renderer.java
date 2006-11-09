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

package client.gui.framework.outline;

import api.entity_model.model.fundtype.GenomicEntity;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.BrowserModelListenerAdapter;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

public class Renderer extends BrowserModelListenerAdapter implements TreeCellRenderer {

  DefaultTreeCellRenderer defaultRenderer=new  DefaultTreeCellRenderer();
  DefaultTreeCellRenderer editorRenderer=new  DefaultTreeCellRenderer();
  GenomicEntity masterEntity;

  public Renderer(BrowserModel browserModel) {
      browserModel.addBrowserModelListener(this);
      editorRenderer.setClosedIcon(null);
      defaultRenderer.setLeafIcon(null);
  }

  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    if ((((DefaultMutableTreeNode)value).getUserObject()!=null)&&
        ((DefaultMutableTreeNode)value).getUserObject().equals(masterEntity)) {
       if (value instanceof GenomicEntityTreeNode) {
         editorRenderer.setTextNonSelectionColor(((GenomicEntityTreeNode)value).getNonSelectedRenderTextColor());
         editorRenderer.setTextSelectionColor(((GenomicEntityTreeNode)value).getSelectedRenderTextColor());
       }
       return editorRenderer.getTreeCellRendererComponent(tree,value,selected,true,true,row,hasFocus);
    }
    else {
       if (value instanceof GenomicEntityTreeNode) {
         defaultRenderer.setTextNonSelectionColor(((GenomicEntityTreeNode)value).getNonSelectedRenderTextColor());
         defaultRenderer.setTextSelectionColor(((GenomicEntityTreeNode)value).getSelectedRenderTextColor());
         defaultRenderer.setLeafIcon(((GenomicEntityTreeNode)value).getNodeIcon());
         defaultRenderer.setOpenIcon(((GenomicEntityTreeNode)value).getNodeIcon());
         defaultRenderer.setClosedIcon(((GenomicEntityTreeNode)value).getNodeIcon());
       }
       return defaultRenderer.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
    }
  }

  public void browserMasterEditorEntityChanged(GenomicEntity masterEditorEntity) {
    masterEntity=masterEditorEntity;
  }

}