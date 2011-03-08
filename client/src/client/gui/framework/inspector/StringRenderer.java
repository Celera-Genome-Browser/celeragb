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

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StringRenderer extends DefaultTableCellRenderer  {

  JLabel textField=new JLabel();
  private Border selectedBorder=new  EtchedBorder(EtchedBorder.RAISED,Color.darkGray,Color.darkGray);
  private Border normalBorder=new  EtchedBorder(EtchedBorder.RAISED,Color.white,Color.white);
  boolean fontSet;

  public Component getTableCellRendererComponent(JTable table, Object value,
                          boolean isSelected, boolean hasFocus, int row, int column) {

     if (!fontSet) {
        fontSet=true;
        Rectangle rec=table.getCellRect(row,column,false);
        while (true) {
          Font currentFont=textField.getFont();
          FontMetrics fm=textField.getFontMetrics(currentFont);
          if (fm.getHeight()-1>rec.getHeight())
            textField.setFont(new Font(currentFont.getName(),currentFont.getStyle(),currentFont.getSize()-1));
          else
            break;
        }


     }

     textField.setText(value.toString());
     textField.setToolTipText(value.toString());
     if (isSelected /*&& hasFocus*/) textField.setBorder(selectedBorder);
     else textField.setBorder(normalBorder);
     return textField;
  }

}
