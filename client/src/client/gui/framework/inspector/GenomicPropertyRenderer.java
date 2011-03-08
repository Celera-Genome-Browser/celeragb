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

import api.stub.data.GenomicProperty;
import client.shared.text_component.StandardTextField;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class GenomicPropertyRenderer extends DefaultTableCellRenderer  {

  JPanel extendedPropertyComponent=new JPanel();

//  JCVI LLF: 10/19/2006
//  static ImageIcon icon = null;
// RT 10/27/2006
static ImageIcon icon=new ImageIcon(GenomicPropertyRenderer.class.getResource("/resource/client/images/plus.gif"));
  JTextField textField=new StandardTextField();
  JLabel iconTextField=new JLabel("test",icon,JLabel.LEFT);
  DefaultTableCellRenderer defaultRenderer=new DefaultTableCellRenderer();
  String showValue;
  java.awt.Rectangle rec;
  boolean fontSet;

  private Border selectedBorder=new  EtchedBorder(EtchedBorder.RAISED,Color.darkGray,Color.darkGray);
  private Border normalBorder=new  EtchedBorder(EtchedBorder.RAISED,Color.white,Color.white);


  public GenomicPropertyRenderer () {
    iconTextField.setHorizontalTextPosition(SwingConstants.LEFT);
    textField.setBorder(null);
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
                          boolean isSelected, boolean hasFocus, int row, int column) {

     if (!fontSet) {
        fontSet=true;
        Rectangle rec=table.getCellRect(row,column,false);
        while (true) {
          Font currentFont=textField.getFont();
          FontMetrics fm=textField.getFontMetrics(currentFont);
          if (fm.getHeight()-1>rec.getHeight()) {
            textField.setFont(new Font(currentFont.getName(),currentFont.getStyle(),currentFont.getSize()-1));
            iconTextField.setFont(new Font(currentFont.getName(),currentFont.getStyle(),currentFont.getSize()-1));
          }
          else
            break;
        }


     }
    if (value instanceof ExternallyEditedProperty) value=((ExternallyEditedProperty)value).getProperty();
    if (value instanceof ExpandableProperty) value=((ExpandableProperty)value).getProperty();
    if (value instanceof URLProperty) value = ((URLProperty)value).getProperty();
    if (value instanceof GenomicProperty) {
       showValue=((GenomicProperty)value).getInitialValue();
       if ( (! (((GenomicProperty)value).getEditingClass() == null))  &&
            (! (((GenomicProperty)value).getInitialValue() == null))  &&
	   (((GenomicProperty)value).getExpandable() ||
        ((GenomicProperty)value).getInitialValue().startsWith("http://") ||
	    (!((GenomicProperty)value).getEditingClass().equals("")))
	   ) {
         iconTextField.setText(showValue);
         if (((GenomicProperty)value).getEditable()
             ) iconTextField.setForeground(java.awt.Color.black);
         else iconTextField.setForeground(java.awt.Color.gray);
         rec=table.getCellRect(row,column,true);
         iconTextField.setIconTextGap((int)rec.getWidth()-icon.getIconWidth()-iconTextField.getFontMetrics(iconTextField.getFont()).stringWidth(showValue)-5);
         iconTextField.setToolTipText(showValue);
         if (isSelected /*&& hasFocus*/)iconTextField.setBorder(selectedBorder);
         else iconTextField.setBorder(normalBorder);
         return iconTextField;
       }
       else {
         textField.setText(showValue);
         if (((GenomicProperty)value).getEditable()
             ) textField.setForeground(java.awt.Color.black);
         else textField.setForeground(java.awt.Color.gray);
         textField.setToolTipText(showValue);
         if (isSelected /*&& hasFocus*/) {
           textField.setBorder(selectedBorder);
           //textField.setBackground(Color.lightGray);
           //System.out.println("Inside getTableCellRendererComponent of GenomicPropertyRenderer");
         }
         else textField.setBorder(normalBorder);//setBackground(Color.white);

         return textField;
       }

    }
    return super.getTableCellRendererComponent( table,  value,  isSelected,  hasFocus,  row,  column);
  }

}
