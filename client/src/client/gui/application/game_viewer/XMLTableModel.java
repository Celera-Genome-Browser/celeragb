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

import javax.swing.table.AbstractTableModel;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

public class XMLTableModel extends AbstractTableModel {

  private NamedNodeMap map;
//  private Node alignment;
  private List additionalRows;

  public XMLTableModel() {
  }

  public void setDOMObject(Object node) {
    if (node == null) {
       map=null;
       additionalRows=null;
    }
    else {
      if (!(node instanceof Node)) return;
      map=((Node)node).getAttributes();
      buildAdditionalRows((Node)node);
      fireTableRowsInserted(0,map.getLength());
    }
    fireTableStructureChanged();
    fireTableDataChanged();
  }

   public String getColumnName(int column) {
        switch (column) {
          case 0:
            return "XML Attribute";
          case 1:
            return "XML Value";
        }
        return null;
   }

   public int getRowCount() {
      if (map==null) return 0;
      return map.getLength()+additionalRows.size();
   }

   public int getColumnCount() {
      return 2;
   }

   public Object getValueAt(int row, int col) {
      switch (col) {
        case 0:
          if (row<map.getLength()) return map.item(row).getNodeName();
          else return ((KeyValue)additionalRows.get(row-map.getLength())).key;
        case 1:/*
          if (alignment!=null && map.getLength()<=row) {
             if (row==map.getLength()) {
                Node span=((Element)alignment).getElementsByTagName("span").item(0);
                Node start=((Element)span).getElementsByTagName("start").item(0);
                return ((Text)(start.getChildNodes().item(0))).getData();
             }
             if (row==map.getLength()+1) {
                Node span=((Element)alignment).getElementsByTagName("span").item(0);
                Node end=((Element)span).getElementsByTagName("end").item(0);
                return ((Text)(end.getChildNodes().item(0))).getData();
             }
          }*/
          if (row<map.getLength()) return map.item(row).getNodeValue();
          else return ((KeyValue)additionalRows.get(row-map.getLength())).value;
      }
      return null;
   }

   private void buildAdditionalRows(Node node) {
       additionalRows=new ArrayList();
       Node replaced=getChildByName(node,"replaced");
       if (replaced!=null) {
          additionalRows.add(new KeyValue("replaced feature ids",replaced.getAttributes().getNamedItem("ids").getNodeValue()));
          additionalRows.add(new KeyValue("replacement type",replaced.getAttributes().getNamedItem("type").getNodeValue()));
       }
       Node[] evidence=getChildrenByName(node,"evidence");
       if (evidence!=null && evidence.length!=0) {
         for (int i=0;i<evidence.length;i++) {
           additionalRows.add(new KeyValue("evidence ("+(i+1)+")",evidence[i].getAttributes().getNamedItem("result").getNodeValue()));
         }
       }
       Node[] comments=getChildrenByName(node,"comments");
       String text;
       if (comments!=null && comments.length!=0) {
         for (int i=0;i<comments.length;i++) {
           text=comments[i].getChildNodes().item(0).getNodeValue();
           text=text.trim();
           additionalRows.add(new KeyValue("comment ("+(i+1)+"): "+
              comments[i].getAttributes().getNamedItem("author").getNodeValue()+" "+
              comments[i].getAttributes().getNamedItem("date").getNodeValue(),
              text));
         }
       }
       Node[] name=getChildrenByName(node,"name");
       if (name!=null && name.length!=0) {
           text=name[0].getChildNodes().item(0).getNodeValue();
           text=text.trim();
           additionalRows.add(new KeyValue("accession name",
              name[0].getChildNodes().item(0).getNodeValue()));
       }

       if (node.getNodeName().equals("feature_span")) {
          Node seqRel=getChildByName(node,"seq_relationship");
          additionalRows.add(new KeyValue("genomic axis id",seqRel.getAttributes().getNamedItem("id").getNodeValue()));
          Node span=getChildByName(seqRel,"span");
          Node start=getChildByName(span,"start");
          Node end=getChildByName(span,"end");
          additionalRows.add(new KeyValue("start on axis",((Text)(start.getChildNodes().item(0))).getData()));
          additionalRows.add(new KeyValue("end on axis",((Text)(end.getChildNodes().item(0))).getData()));
       }
       if (node.getNodeName().equals("game")) {
          Node program=getChildByName(node,"program");
          if (program != null)
            additionalRows.add(new KeyValue("program",((Text)(program.getChildNodes().item(0))).getData()));
          Node version=getChildByName(node,"version");
          if (version != null)
            additionalRows.add(new KeyValue("version",((Text)(version.getChildNodes().item(0))).getData()));
          Node date=getChildByName(node,"date");
          if (date != null)
            additionalRows.add(new KeyValue("date",(
              date.getAttributes().getNamedItem("month").getNodeValue()+"/"+
              date.getAttributes().getNamedItem("day").getNodeValue()+"/"+
              date.getAttributes().getNamedItem("year").getNodeValue())));
       }
   }

   private Node getChildByName (Node parent,String name) {
       NodeList children=parent.getChildNodes();
       for (int i=0;i<children.getLength();i++) {
          if (children.item(i).getNodeName().equalsIgnoreCase(name)) return children.item(i);
       }
       return null;
   }

   private Node[] getChildrenByName (Node parent,String name) {
       NodeList children=parent.getChildNodes();
       ArrayList childrenOfName=new ArrayList();
       for (int i=0;i<children.getLength();i++) {
          if (children.item(i).getNodeName().equalsIgnoreCase(name)) childrenOfName.add(children.item(i));
       }
       return (Node[])childrenOfName.toArray(new Node[0]);
   }


   private class KeyValue {
      String key;
      String value;
      public KeyValue(String key, String value) {
          this.key=key;
          this.value=value;
      }
   }




}
