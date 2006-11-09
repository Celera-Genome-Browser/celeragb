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

package client.gui.framework.browser;

import api.entity_model.model.fundtype.ActiveThreadModel;
import client.gui.framework.progress_meter.ProgressMeter;
import shared.util.MultiHash;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;


/**
* This class is designed with generalized behaviours to support the browser.
* It is designed with the intent that it will be specialized for the application.
*/
public class BrowserMenuBar extends JMenuBar {
  public static final Position LEFT = new Position(0);
  public static final Position AFTER_FILE = new Position(1);
  public static final Position AFTER_VIEW = new Position(2);
  public static final Position AFTER_EDITOR_SPECIFIC = new Position(3);
  public static final Position AFTER_SPACING_GLUE = new Position(4);
  public static final Position RIGHT = AFTER_SPACING_GLUE;
  private static final Position EDITOR_SPECIFIC = new Position(5);

  protected Browser browser;   //protected so that when subclasses, the subclasses menus can have access to it.
  private boolean animation;
  private MultiHash addedMenus=new MultiHash();

  protected JLabel staticImageLabel,animatedImageLabel;
  protected FileMenu fileMenu;
  protected JMenu viewMenu;
  protected JMenu windowMenu;
  protected Component menuGlue=Box.createHorizontalGlue();


  protected BrowserMenuBar(Browser browser) {
     this.browser=browser;
     BoxLayout layout=new BoxLayout(this,BoxLayout.X_AXIS);
     setLayout(layout);
     constructMenus();
     addMenus();
     ActiveThreadModel.getActiveThreadModel().addObserver(new ThreadModelObserver());
     browser.addBrowserObserver(new MenuBarBrowserObserver());
  }

  private void constructMenus() {
     fileMenu=new FileMenu(browser);
     viewMenu=new ViewMenu(browser);
     windowMenu=new WindowMenu(browser);
  }

  private void addMenus() {
     add(fileMenu);
     add(viewMenu);
     add(windowMenu);
     add(menuGlue);
     if (staticImageLabel!=null) add(staticImageLabel);
  }

  public void add(Component comp, Position pos) {
     addedMenus.put(pos,comp);
     redraw();
  }

  public void remove(Component comp) {
     Vector vec;
     for (Enumeration e=addedMenus.elements();e.hasMoreElements();) {
       vec=(Vector)e.nextElement();
       if (vec.contains(comp)) {
         vec.remove(comp);
         break;
       }
     }
     redraw();
  }

  public void setAnimationIcon(ImageIcon animationIcon) {
      animatedImageLabel=new JLabel(animationIcon);
      animatedImageLabel.addMouseListener(new MyMouseListener());
  }

  public void setStaticIcon(ImageIcon staticIcon) {
      staticImageLabel=new JLabel(staticIcon);
      staticImageLabel.addMouseListener(new MyMouseListener());
  }

  private void setEditorSpecificMenus(JMenuItem[] menus) {
     addedMenus.remove(EDITOR_SPECIFIC);
     if (menus!=null)
        for (int i=0;i<menus.length;i++) {
            addedMenus.put(EDITOR_SPECIFIC,menus[i]);
        }
     redraw();
  }

  private void redraw() {
     Vector menuList=new Vector();
     Vector tmpVec;
     if (addedMenus.containsKey(LEFT)) {
        tmpVec=(Vector)addedMenus.get(LEFT);
        menuList.addAll(tmpVec);
     }
     menuList.addElement(fileMenu);
     if (addedMenus.containsKey(AFTER_FILE)) {
        tmpVec=(Vector)addedMenus.get(AFTER_FILE);
        menuList.addAll(tmpVec);
     }
     menuList.add(viewMenu);
     if (addedMenus.containsKey(AFTER_VIEW)) {
        tmpVec=(Vector)addedMenus.get(AFTER_VIEW);
        menuList.addAll(tmpVec);
     }
     if (addedMenus.containsKey(EDITOR_SPECIFIC)) {
        tmpVec=(Vector)addedMenus.get(EDITOR_SPECIFIC);
        menuList.addAll(tmpVec);
     }
     menuList.add(windowMenu);
     if (addedMenus.containsKey(AFTER_EDITOR_SPECIFIC)) {
        tmpVec=(Vector)addedMenus.get(AFTER_EDITOR_SPECIFIC);
        menuList.addAll(tmpVec);
     }
     menuList.add(menuGlue);
     if (addedMenus.containsKey(AFTER_SPACING_GLUE)) {
        tmpVec=(Vector)addedMenus.get(AFTER_SPACING_GLUE);
        menuList.addAll(tmpVec);
     }
     if (staticImageLabel!=null)
         if (!animation) menuList.add(staticImageLabel);
         else menuList.add(animatedImageLabel);


     removeAll();
     for (int i=0;i<menuList.size();i++) {
       add((Component)menuList.elementAt(i));
     }
     validate();
     browser.repaint();
  }

  private void modifyImageState(boolean animated) {
     if (staticImageLabel==null || animatedImageLabel==null) return;
     animation=animated;
     redraw();
  }


  static class Position {
    int pos;
    Position (int position) {
      pos=position;
    }
  }

  class MenuBarBrowserObserver extends BrowserObserverAdapter {
     public void editorSpecificMenusChanged(JMenuItem[] menus){
       setEditorSpecificMenus(menus);
     }
  }

  class ThreadModelObserver implements Observer{
     public void update(Observable o, Object arg) {
       if (o instanceof ActiveThreadModel)
         if (((ActiveThreadModel)o).getActiveThreadCount()>0) modifyImageState(true);
         else modifyImageState(false);
    }
  }

  class MyMouseListener implements MouseListener {
    private ProgressMeter meter = ProgressMeter.getProgressMeter();
    public void mouseClicked(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e) {
      meter.setLocationRelativeTo(browser);
      meter.setVisible(true);
    }
    public void mouseExited(MouseEvent e){
      meter.setVisible(false);
    }
  }

}