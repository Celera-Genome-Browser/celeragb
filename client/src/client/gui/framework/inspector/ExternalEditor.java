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
package client.gui.framework.inspector;

import client.gui.framework.ics_tabpane.ICSTabPane;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.SessionMgr;
import client.shared.text_component.StandardTextField;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;

public class ExternalEditor extends DefaultCellEditor{
    ICSTabPane tabPane;
    StringRenderer gpr=new StringRenderer();

    public ExternalEditor(ICSTabPane tabPane) {
        super(new StandardTextField());
        this.tabPane=tabPane;
    }

  public Component getTableCellEditorComponent(JTable table, Object value,
                          boolean isSelected, int row, int column) {

          String errorMsg=null;
          try {
              Class newClass=((ExternallyEditedProperty)value).getEditingClass();
              Constructor cons=newClass.getConstructor(new Class[]{BrowserModel.class,String.class});
              Object obj=cons.newInstance(new Object[]{tabPane.getBrowserModel(),((ExternallyEditedProperty)value).getName()});
              if (obj instanceof Window) {
                 Window window=(Window)obj;
                 window.addWindowListener(new WindowListener() {
                      public void windowOpened(WindowEvent e){}
                      public void windowClosing(WindowEvent e){}
                      public void windowClosed(WindowEvent e){
                       cancelCellEditing();
                       e.getWindow().removeWindowListener(this);
                      }
                      public void windowIconified(WindowEvent e){}
                      public void windowDeiconified(WindowEvent e){}
                      public void windowActivated(WindowEvent e){}
                      public void windowDeactivated(WindowEvent e){}
                });
              }
              else if (obj instanceof Component) {
                 Component comp=(Component)obj;
                 comp.addComponentListener(new ComponentListener(){
                    public void componentResized(ComponentEvent e){}
                    public void componentMoved(ComponentEvent e){}
                    public void componentShown(ComponentEvent e){

                    }
                    public void componentHidden(ComponentEvent e){
                       cancelCellEditing();
                       e.getComponent().removeComponentListener(this);
                    }
                 });
              }
           }
           catch (NoSuchMethodException nsm) {
             errorMsg="Error!! The method specified to edit this property could not be found!";
           }
           catch (InvocationTargetException it) {
             errorMsg="Error!! The class specified throw an exception!";
           }
           catch (IllegalAccessException ia) {
             errorMsg="Error!! The class specified is not accessable!";
           }
           catch (InstantiationException ie) {
             errorMsg="Error!! The class specified throw an exception!";
           }
           if (errorMsg!=null) {
               SessionMgr.getSessionMgr().handleException(new Exception(errorMsg));
           }
          return gpr.getTableCellRendererComponent(table, ((ExternallyEditedProperty)value).getInitialValue(), isSelected, true, row,column);
   }

}