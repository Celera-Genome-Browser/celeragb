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

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class GenomicEntityTreeNode extends DefaultMutableTreeNode {

  private ArrayList listeners;
  protected boolean childrenLoaded;  //intended to be modified by sub-classes
  private final static Color nonSelectedRenderTextColor = Color.black;
  private final static Color selectedRenderTextColor = Color.white;

  public GenomicEntityTreeNode (GenomicEntity pi) {
     super(pi);
  }

  public Color getSelectedRenderTextColor() {
    return selectedRenderTextColor;
  }

  public Color getNonSelectedRenderTextColor() {
    return nonSelectedRenderTextColor;
  }

  public abstract Icon getNodeIcon();

  public boolean isLeaf() {       //if the children are not loaded always appear as branch
    if (!childrenLoaded) return false;
    return super.isLeaf();
  }

  public void addGenomicEntityTreeNodeListener(GenomicEntityTreeNodeListener listener) {
     if (listeners==null) listeners=new ArrayList();
     listeners.add(listener);
  }

  public void removeGenomicEntityTreeNodeListener(GenomicEntityTreeNodeListener listener) {
     if (listeners==null) return;
     listeners.remove(listener);
  }


  protected void postChildrenAdded(TreeNode changedNode, int[] indicies) {
     if (listeners!=null)
       for (Iterator i=listeners.iterator();i.hasNext(); )
          ((GenomicEntityTreeNodeListener)i.next()).childrenAdded(changedNode,indicies);
     if (parent !=null && parent instanceof GenomicEntityTreeNode) ((GenomicEntityTreeNode)parent).postChildrenAdded(changedNode,indicies);
  }

  protected void postChildrenRemoved(TreeNode changedNode, int[] indicies, Object[] children) {
     if (listeners!=null)
       for (Iterator i=listeners.iterator();i.hasNext(); )
          ((GenomicEntityTreeNodeListener)i.next()).childrenRemoved(changedNode,indicies,children);
     if (parent !=null && parent instanceof GenomicEntityTreeNode) ((GenomicEntityTreeNode)parent).postChildrenRemoved(changedNode,indicies,children);
  }

  public void removeAllChildren() {
     int numChildren=getChildCount();
     Object obj;
     for (int i=0;i<numChildren;i++) {
        obj=getChildAt(i);
        if (obj instanceof GenomicEntityTreeNode) {
          ((GenomicEntityTreeNode)obj).aboutToBeRemoved();
        }
     }
     super.removeAllChildren();
  }


  //This must be implemented in sub-classes to load children types that should be shown in the tree
  public abstract void loadChildren();

  /**
   * Override this to get Right Mouse Click notification
   */
  void receivedRightClick (JComponent component,MouseEvent e) {}

  /**
   * Override this to get Mouse Click notification
   */
  void receivedClick (JComponent component,MouseEvent e) {}

  //This must be implemented to remove all model observers
  abstract void aboutToBeRemoved();
}

