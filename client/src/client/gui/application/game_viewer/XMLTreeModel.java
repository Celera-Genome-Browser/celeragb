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

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */


public class XMLTreeModel extends DefaultTreeModel {

  private Document document;
  private XMLTreeNodeFactory factory=XMLTreeNodeFactory.getFactory();
  private List axes=new ArrayList();

  public XMLTreeModel() {
    super(new DefaultMutableTreeNode());
  }

  public void setDocument(Document document) {
     this.document=document;
     buildModel();
  }

  private void buildModel() {
    insertChildren((DefaultMutableTreeNode)this.getRoot(),document.getChildNodes().item(0));
    this.nodesWereInserted((DefaultMutableTreeNode)this.getRoot(),new int[]{0});
  }

  private void insertChildren (DefaultMutableTreeNode treeNode, Node domNode) {
      DefaultMutableTreeNode child=factory.buildTreeNode(domNode);
      if (child==null) return;
      if (child instanceof TaxonXMLTreeNode) setTaxon(child);
      if (child instanceof MiscXMLTreeNode && ((MiscXMLTreeNode)child).getAxisParent()!=null)
         attachToAxis((MiscXMLTreeNode)child);
      else
         treeNode.add(child);
      nodesWereInserted(treeNode,new int[]{treeNode.getChildCount()-1});
      NodeList nodeList=domNode.getChildNodes();
      for (int i=0;i<nodeList.getLength();i++) {
         insertChildren(child,nodeList.item(i));
      }
  }

  private void setTaxon(TreeNode taxon) {
     for (int i=0;i<taxon.getChildCount();i++) {
        axes.add(taxon.getChildAt(i));
     }
  }

  private void attachToAxis(MiscXMLTreeNode node) {
     for (int i=0;i<axes.size();i++) {
        if ( ((MiscXMLTreeNode)axes.get(i)) .getNodeID().equals(node.getAxisParent()) ) {
           ((MiscXMLTreeNode)axes.get(i)).add(node);
           break;
        }
     }
  }


}
