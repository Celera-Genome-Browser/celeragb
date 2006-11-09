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
package client.gui.framework.browser;

import javax.swing.*;
import java.awt.event.*;
import java.util.*;

public class EditorMenuFactory {

  static private EditorMenuFactory editorMenuFactory;

  private EditorMenuFactory() {} //singleton

  static public EditorMenuFactory getEditorMenuFactory() {
    if (editorMenuFactory==null) editorMenuFactory=new EditorMenuFactory();
    return editorMenuFactory;
  }

  public JPopupMenu createPopupMenu(String title,String[] items,String disabledItem, Browser invoker,int x,int y){
     return new  EditorPopUpMenu(title,items,disabledItem,invoker,x,y);
  }

  public JMenu createMenu(String title,String[] items,Browser invoker) {
     return new EditorMenu(title,items,invoker);
  }

class EditorMenu extends JMenu implements ActionListener {
   Browser browser;
   Hashtable menuItems=new Hashtable();

    EditorMenu(String title,String[] items,Browser browser) {
      setText(title);
      this.browser=browser;
      JMenuItem item;
      for (int i=0;i<items.length;i++) {
        item =new JMenuItem(items[i]);
        menuItems.put(items[i],item);
        item.addActionListener(this);
        add(item);
      }
  }

  public void actionPerformed(ActionEvent e) {
     this.setVisible(false);
     new Perform(e).run();
  }

  void setDisabled(String menuItem) {
     for (Enumeration enum=menuItems.elements();enum.hasMoreElements();)
        {((JMenuItem)enum.nextElement()).setEnabled(true);}
     ((JMenuItem)menuItems.get(menuItem)).setEnabled(false);
  }

  class Perform implements Runnable {
       ActionEvent e;
       Perform (ActionEvent e) {this.e=e;}
       public void run() {
         setDisabled(e.getActionCommand());
         browser.setMasterEditor(e.getActionCommand());
       }
  }
}


class EditorPopUpMenu extends JPopupMenu implements ActionListener {
  int x,y;


  EditorPopUpMenu(String  title,String[] items,String disabledItem,Browser invoker,int x,int y) {
     if (title!=null) {
       add("Please choose an editor");
       addSeparator();
     }
     this.x=x;
     this.y=y;
     JMenuItem item;
     for (int i=0;i<items.length;i++) {
       item =new JMenuItem(items[i]);
       item.addActionListener(this);
       add(item);
       if (items[i].equals(disabledItem)) {
          item.setEnabled(false);
       }
     }
     setInvoker(invoker);
     show( this.getInvoker(), x, y );
  }


  public void actionPerformed(ActionEvent e) {
     this.setVisible(false);
      new Perform(e).run();
  }

  class Perform implements Runnable {
       ActionEvent e;
       Perform (ActionEvent e) {this.e=e;}

       public void run () {
          ((Browser)getInvoker()).setMasterEditor(e.getActionCommand());
       }
  }
}

}