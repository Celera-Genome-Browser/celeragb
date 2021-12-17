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

import client.shared.text_component.StandardTextField;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class ExpandableEditor extends DefaultCellEditor {

   String showValue;
   StringRenderer gpr = new StringRenderer();
   JComboBox comboBox = new JComboBox();
   TableCellEditor comboEditor = new DefaultCellEditor(comboBox);
   boolean fontSet;

   public ExpandableEditor() {
      super(new StandardTextField());
   }

   public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

      if (!fontSet) {
         fontSet = true;
         Rectangle rec = table.getCellRect(row, column, false);
         while (true) {
            Font currentFont = comboBox.getFont();
            FontMetrics fm = comboBox.getFontMetrics(currentFont);
            if (fm.getHeight() - 1 > rec.getHeight())
               comboBox.setFont(new Font(currentFont.getName(), currentFont.getStyle(), currentFont.getSize() - 1));
            else
               break;
         }

      }

      if (value instanceof ExpandableProperty && table instanceof Inspector) {
         showValue = ((ExpandableProperty) value).getInitialValue();
         Inspector insp = ((Inspector) table).addChildInspector(((ExpandableProperty) value).getProperty());
         insp.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
            }
            public void componentMoved(ComponentEvent e) {
            }
            public void componentShown(ComponentEvent e) {
               cancelCellEditing();
               e.getComponent().removeComponentListener(this);
            }
            public void componentHidden(ComponentEvent e) {
            }
         });
         return gpr.getTableCellRendererComponent(table, showValue, isSelected, true, row, column);
      }
      else
         return super.getTableCellEditorComponent(table, showValue, isSelected, row, column);
   }

}
