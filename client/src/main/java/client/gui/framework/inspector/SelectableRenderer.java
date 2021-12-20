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
package client.gui.framework.inspector;

import api.stub.data.FlaggedGenomicProperty;
import client.shared.text_component.StandardTextField;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class SelectableRenderer extends DefaultTableCellRenderer  {

  JTextField textField=new StandardTextField();
  private String showValue;
  private Border selectedBorder=new  EtchedBorder(EtchedBorder.RAISED,Color.darkGray,Color.darkGray);
  private Border normalBorder=new  EtchedBorder(EtchedBorder.RAISED,Color.white,Color.white);
  private boolean fontSet;

  public SelectableRenderer () {
    textField.setBorder(null);
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
                          boolean isSelected, boolean hasFocus, int row, int column) {
     //  This is to make sure we can continually display these types of properties correctly.
     if (((SelectableProperty)value).getGenomicProperty() instanceof FlaggedGenomicProperty) {
        FlaggedGenomicProperty tmpProperty = (FlaggedGenomicProperty)((SelectableProperty)value).getGenomicProperty();
        if (tmpProperty.hasBeenModified()) textField.setBackground(Color.cyan);
        else textField.setBackground(Color.white);
     }
     if (!fontSet) {
        fontSet=true;
        Rectangle rec=table.getCellRect(row,column,false);
        while (true) {
          Font currentFont=textField.getFont();
          FontMetrics fm=textField.getFontMetrics(currentFont);
          if (fm.getHeight()-1>rec.getHeight()) {
            textField.setFont(new Font(currentFont.getName(),currentFont.getStyle(),currentFont.getSize()-1));
          }
          else
            break;
        }
     }
    if (!(value instanceof SelectableProperty))
             return super.getTableCellRendererComponent( table,  value,  isSelected,  hasFocus,  row,  column);
    textField.setHorizontalAlignment(JTextField.CENTER);
    showValue=((SelectableProperty)value).getSelectedElement();
    textField.setText(showValue);
    if (((SelectableProperty)value).isEditable()) textField.setForeground(java.awt.Color.black);
    else textField.setForeground(java.awt.Color.gray);
    textField.setToolTipText(showValue);
    if (isSelected /* &&hasFocus*/) textField.setBorder(selectedBorder);
    else textField.setBorder(normalBorder);
    return textField;
  }

}
