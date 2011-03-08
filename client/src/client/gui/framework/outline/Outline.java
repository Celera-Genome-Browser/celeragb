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
 *		  Confidential -- Do Not Distribute                  *
 *********************************************************************
 CVS_ID:  $Id$
 *********************************************************************/

package client.gui.framework.outline;

import api.entity_model.access.observer.ModelMgrObserverAdapter;
import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.annotation.Workspace;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.genetics.GenomeVersion;
import api.entity_model.model.genetics.Species;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.FacadeManagerBase;
import client.gui.framework.browser.Browser;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.BrowserModelListenerAdapter;
import client.gui.framework.session_mgr.SessionMgr;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

public class Outline extends JScrollPane implements Cloneable{

  private Browser browser;
  private JTree tree=new JTree();
  private BrowserModel browserModel;
  private TreePath treeDrillDownPath,previousTreeSelectionPath;
  private SessionMgr sessionManager=SessionMgr.getSessionMgr();
  private FacadeManagerBase facadeManager=FacadeManager.getFacadeManager();
  private boolean emptyTreeModel;
  private BrowserModelObserver browserModelObserver;
  private DefaultTreeModel treeModel;
  private MutableTreeNode noInfoNode=new DefaultMutableTreeNode("No Information Service");

  public Outline(Browser browser) {
    this.browser=browser;
    if (ModelMgr.getModelMgr().isModelAvailable()) {
       buildTreeModel();
       tree.setModel(treeModel);
    }
    else {
       buildEmptyTreeModel();
       tree.setModel(treeModel);
       ModelMgr.getModelMgr().addModelMgrObserver(new OutlineModelMgrObserver());
    }
    browserModel=browser.getBrowserModel();
    browserModel.addBrowserModelListener(browserModelObserver=new BrowserModelObserver());
    tree.setCellRenderer(new Renderer(browserModel));
    tree.addTreeExpansionListener(new TreeExpansionListener() {
       public void treeExpanded(TreeExpansionEvent event){
             if (event.getPath().getLastPathComponent() instanceof GenomicEntityTreeNode)
                ((GenomicEntityTreeNode)event.getPath().getLastPathComponent()).loadChildren();
       }
       public void treeCollapsed(TreeExpansionEvent event){}
    });

    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setLargeModel(true);
    tree.setDoubleBuffered(true);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    setViewportView(tree);
    tree.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        handleMouseEvents(e);
      }
    });
  }

  public boolean nodesShowing() {
     return (treeModel.getChildCount(treeModel.getRoot())!=0);
  }

  private void handleMouseEvents(MouseEvent e) {
       TreePath treePath=tree.getSelectionPath();
       if (treePath==null) return;
       if ((e.getModifiers() & e.BUTTON3_MASK) >0 ) {
		   //System.out.println("Rt. button mouse pressed clicks: "+e.getClickCount()+" "+System.currentTimeMillis());
           if (treePath.getLastPathComponent() instanceof GenomicEntityTreeNode) {
               ((GenomicEntityTreeNode)treePath.getLastPathComponent()).receivedRightClick(Outline.this,e);
           }
        }
       else if (((e.getModifiers() & e.BUTTON1_MASK) >0) && e.getClickCount()==2 && treePath.getLastPathComponent() instanceof GenomicAxisTreeNode) {
		   //System.out.println("Left button mouse pressed clicks: "+e.getClickCount()+" "+System.currentTimeMillis());
           Outline.this.browser.drillDownToEntityUsingDefaultEditor(browserModel.getCurrentSelection());
           browser.setView(browser.isOutlineCollapsed());
           return;
       }
       java.lang.Object treeObj=treePath.getLastPathComponent();
       if (treeObj instanceof DefaultMutableTreeNode) {    //if not a DefaultMutableTreeNode, punt
            DefaultMutableTreeNode node=(DefaultMutableTreeNode)treeObj;
            Object userObj=node.getUserObject();
            if (!(userObj instanceof GenomicEntity)) { //if it's not a GenomicEntity, veto selection
                 tree.setSelectionPath(previousTreeSelectionPath);
                 tree.repaint();
                 return;
            }
            browserModel.setCurrentSelection((GenomicEntity)userObj);
            previousTreeSelectionPath=treePath;  //if validation passes, reset previousTreeSelectionPath to current
             if (treeObj instanceof GenomicEntityTreeNode) {
                 ((GenomicEntityTreeNode)treePath.getLastPathComponent()).receivedClick(Outline.this,e);
             }
       }
  }

  private void buildEmptyTreeModel() {
     DefaultMutableTreeNode root=new DefaultMutableTreeNode("Tree Root");
     root.add(noInfoNode);
     treeModel=new DefaultTreeModel(root,false);
     emptyTreeModel=true;
  }

  private void buildTreeModel() {
   emptyTreeModel=false;
   DefaultMutableTreeNode root=new DefaultMutableTreeNode("Tree Root");
   treeModel = new DefaultTreeModel(root);
   try {
    Set genomeVersions = ModelMgr.getModelMgr().getSelectedGenomeVersions();
    HeadNodeVisitor headNodeVisitor= new HeadNodeVisitor();
    GenomeVersion genomeVersion;
    //genomeVersion.getSpecies().acceptVisitorForSelf(headNodeVisitor);
    for (Iterator i=genomeVersions.iterator();i.hasNext();) {
      genomeVersion=(GenomeVersion)i.next();
      genomeVersion.getSpecies().acceptVisitorForSelf(headNodeVisitor);
    }
   }
   catch (Exception ex) {
      System.out.println("Outline: ERROR!! Tree received a data exception and cannot be built.");
      try {
        SessionMgr.getSessionMgr().handleException(ex);
      }
      catch (Exception ex1) {ex.printStackTrace();}
   }
  }

  private void addGenomeVersion(GenomeVersion genomeVersion) {
    if (emptyTreeModel) treeModel.removeNodeFromParent(noInfoNode);
    emptyTreeModel=false;
    try{
      HeadNodeVisitor headNodeVisitor= new HeadNodeVisitor();
      if (genomeVersion == null || genomeVersion.getSpecies()==null) return;
      genomeVersion.getSpecies().acceptVisitorForSelf(headNodeVisitor);
     }
    catch (Exception ex) {
        System.out.println("Outline: ERROR!! Tree received a data exception and cannot be built.");
        try {
          SessionMgr.getSessionMgr().handleException(ex);
        }
        catch (Exception ex1) {ex.printStackTrace();}
    }
  }

  private void removeGenomeVersion(GenomeVersion genomeVersion) {
     TreeNode root=(TreeNode)treeModel.getRoot();
     int rootChildren=treeModel.getChildCount(root);
     DefaultMutableTreeNode node;
     Object userObject;
     for (int i=0;i<rootChildren;i++) {
        node=(DefaultMutableTreeNode)treeModel.getChild(root,i);
        userObject=node.getUserObject();
        if (userObject.equals(genomeVersion)) {
           treeModel.removeNodeFromParent(node);
           break;
        }
     }
     if (treeModel.getChildCount(root)==0) emptyTreeModel=true;
  }


 private class HeadNodeVisitor extends GenomicEntityVisitor {

    public void visitGenomicEntity(GenomicEntity entity) {
        ((DefaultMutableTreeNode)treeModel.getRoot()).add(noInfoNode);
    }

    public void visitSpecies(Species species) {
        addHeadNodeToRoot(new SpeciesTreeNode(species));
    }

    private void addHeadNodeToRoot(GenomicEntityTreeNode headNode) {
         ((DefaultMutableTreeNode)treeModel.getRoot()).add(headNode);
         treeModel.nodesWereInserted((TreeNode)treeModel.getRoot(),new int[]{((TreeNode)treeModel.getRoot()).getChildCount()-1});
         headNode.addGenomicEntityTreeNodeListener(new GenomicEntityTreeNodeListener() {
            public void childrenAdded(TreeNode changedNode,int[] indicies){
               treeModel.nodesWereInserted(changedNode,indicies);
            }
            public void childrenRemoved(TreeNode changedNode,int[] indicies,Object[] children){
               treeModel.nodesWereRemoved(changedNode,indicies,children);
            }
         });
    }
 }


 private class BrowserModelObserver extends BrowserModelListenerAdapter {
    public void browserCurrentSelectionChanged(GenomicEntity newSelection) {
     // System.out.println("Heard Last Selection Change to: "+newSelection);
      if (newSelection==null) {
        Outline.this.tree.removeSelectionPaths(tree.getSelectionPaths());
        Outline.this.tree.repaint();
        return;
      }

      DefaultMutableTreeNode drillDownNode;
      if (treeDrillDownPath!=null)
        drillDownNode=(DefaultMutableTreeNode)treeDrillDownPath.getLastPathComponent();
      else drillDownNode=(DefaultMutableTreeNode)tree.getModel().getRoot();
      if (drillDownNode.isLeaf()) return;
      DefaultMutableTreeNode node;
      for (Enumeration e=drillDownNode.breadthFirstEnumeration();e.hasMoreElements();) { //check if node is shown in tree at the drilldown or below
        node=(DefaultMutableTreeNode)e.nextElement();
        if (newSelection.equals(node.getUserObject())) { //if it is, select it
          TreePath tp=new TreePath(node.getPath());
          tree.setSelectionPath(tp);
          //Fully left justify the root (GenomeVersion) nodes
          if (tp.getPathCount()>2) {
            tree.scrollPathToVisible(tp);
          }
          else {
            tree.makeVisible(tp);
            java.awt.Rectangle rec=tree.getPathBounds(tp);
            rec.x=0;
            tree.scrollRectToVisible(rec);
          }
          tree.repaint();
          return;
        }
      }
      previousTreeSelectionPath=null;
      tree.removeSelectionPaths(tree.getSelectionPaths());
      tree.repaint();
    }
  }


  class OutlineModelMgrObserver extends ModelMgrObserverAdapter{

       public void genomeVersionSelected(GenomeVersion genomeVersion) {
         addGenomeVersion(genomeVersion);
       }

       public void genomeVersionUnselected(GenomeVersion genomeVersion) {
         removeGenomeVersion(genomeVersion);
       }

       public void workSpaceCreated(GenomeVersion genomeVersion){
          repaint();
       }

        public void workSpaceRemoved(GenomeVersion genomeVersion, Workspace workspace){
          repaint();
         }

  }
}

