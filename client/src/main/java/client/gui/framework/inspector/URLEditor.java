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

import client.gui.other.util.URLLauncher;
import client.shared.text_component.StandardTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class URLEditor extends DefaultCellEditor{
  StringRenderer gpr=new StringRenderer();
  boolean fontSet;

  public URLEditor () {
    super(new StandardTextField());
  }

  public Component getTableCellEditorComponent(JTable table, Object value,
                          boolean isSelected, int row, int column) {
        String showValue=((URLProperty)value).getInitialValue();
        URLLauncher.launchURL(((URLProperty)value).getInitialValue());
/*        try {
        	Runtime.getRuntime().exec( "rundll32 url.dll,FileProtocolHandler " +
                ((URLProperty)value).getInitialValue() );
          } catch (Exception e) {
            e.printStackTrace();
          }*/

          Component comp=gpr.getTableCellRendererComponent(table, showValue, isSelected, true, row,column);
          if (!fontSet) {
            fontSet=true;
            Rectangle rec=table.getCellRect(row,column,false);
            while (true) {
              Font currentFont=comp.getFont();
              FontMetrics fm=comp.getFontMetrics(currentFont);
              if (fm.getHeight()-1>rec.getHeight())
                comp.setFont(new Font(currentFont.getName(),currentFont.getStyle(),currentFont.getSize()-1));
              else
                break;
            }
          }
          comp.addComponentListener(new ComponentListener(){
                    public void componentResized(ComponentEvent e){}
                    public void componentMoved(ComponentEvent e){}
                    public void componentShown(ComponentEvent e){
                       cancelCellEditing();
                       e.getComponent().removeComponentListener(this);
                    }
                    public void componentHidden(ComponentEvent e){}
                 });
          return comp;
    }

}