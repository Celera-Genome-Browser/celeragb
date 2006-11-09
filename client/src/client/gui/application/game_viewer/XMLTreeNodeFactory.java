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

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies
 * @version $Id$
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class XMLTreeNodeFactory {

  static private XMLTreeNodeFactory factory=new XMLTreeNodeFactory();

  private XMLTreeNodeFactory() {}

  public DefaultMutableTreeNode buildTreeNode(Node node) {
    if (node.getNodeName().equals("#text")) return null;
    if (node.getNodeName().equals("name")) return null;
    if (node.getNodeName().equals("type")) return null;
    if (node.getNodeName().equals("span")) return null;
    if (node.getNodeName().equals("property")) return null;
    if (node.getNodeName().equals("replaced")) return null;
    if (node.getNodeName().equals("evidence")) return null;
    if (node.getNodeName().equals("comments")) return null;
    if (node.getNodeName().equals("date")) return null;
    if (node.getNodeName().equals("program")) return null;
    if (node.getNodeName().equals("version")) return null;
    if (node.getNodeName().equals("seq_relationship")) return null;
    if (node.getNodeName().equals("game")) {
       XMLTreeNode taxon=new TaxonXMLTreeNode(node);
       NodeList seqs=((Element)node).getElementsByTagName("seq_relationship");
       Map axes=new HashMap();
       String value;
       for (int i=0;i<seqs.getLength();i++) {
          value=seqs.item(i).getAttributes().getNamedItem("id").getNodeValue();
          axes.put(value,seqs.item(i));
       }
       Set keySet=axes.keySet();
       Object[] keys=keySet.toArray();
       for (int i=0;i<keys.length;i++) {
          taxon.add(new AxisXMLTreeNode("Genomic Axis",(Node)axes.get(keys[i])));
       }
       return taxon;
    }
    if (node.getNodeName().equals("annotation")) return new MiscXMLTreeNode("Gene",node);
    if (node.getNodeName().equals("feature_set")) return new MiscXMLTreeNode("Transcript",node);
    if (node.getNodeName().equals("feature_span")) {
        NodeList childNodes = node.getChildNodes();
        Node typeNode=null;
        for (int i=0;i<childNodes.getLength();i++) {
           if (childNodes.item(i).getNodeName().equals("type")) {
              typeNode=childNodes.item(i);
              break;
           }
        }
        if (typeNode==null) return new MiscXMLTreeNode("Feature Span of Unknown Type",node);
        if (((Text)(typeNode.getChildNodes().item(0))).getData().equalsIgnoreCase("EXON")) return new MiscXMLTreeNode("Exon",node);
        if (((Text)(typeNode.getChildNodes().item(0))).getData().equalsIgnoreCase("START_CODON")) return new MiscXMLTreeNode("Start Codon",node);
        if (((Text)(typeNode.getChildNodes().item(0))).getData().equalsIgnoreCase("Start Codon")) return new MiscXMLTreeNode("Start Codon",node);
        if (((Text)(typeNode.getChildNodes().item(0))).getData().equalsIgnoreCase("STOP_CODON")) return new MiscXMLTreeNode("Stop Codon",node);
        if (((Text)(typeNode.getChildNodes().item(0))).getData().equalsIgnoreCase("Stop Codon")) return new MiscXMLTreeNode("Stop Codon",node);
        if (((Text)(typeNode.getChildNodes().item(0))).getData().equalsIgnoreCase("TRANSLATION_START_POSITION")) return new MiscXMLTreeNode("Translation Start Codon",node);
        if (((Text)(typeNode.getChildNodes().item(0))).getData().equalsIgnoreCase("Translation Start Position")) return new MiscXMLTreeNode("Translation Start Codon",node);

    }
    return new XMLTreeNode(node);
  }

  static public XMLTreeNodeFactory getFactory() {
     return factory;
  }
}
