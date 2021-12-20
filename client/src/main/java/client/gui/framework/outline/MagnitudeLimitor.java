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

/**
 * Title:        Your Product Name<p>
 * Description:  This is the main Browser in the System<p>
 * @author Peter Davies
 * @version
 */
package client.gui.framework.outline;

import client.shared.text_component.StandardTextField;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class MagnitudeLimitor extends JDialog {
  private JSlider maxSlider = new JSlider();
  private JSlider minSlider = new JSlider();
  private JButton okButton = new JButton();
  private JButton cancelButton = new JButton();
  private JPanel panel = new JPanel();
  private JTextField minText=new StandardTextField();
  private JTextField maxText=new StandardTextField();
  private String sortType = new String("");

  public MagnitudeLimitor(Frame parent,Point location, boolean isSortedByLength) {
    super(parent, true);
    if (isSortedByLength) sortType = "Length";
    else sortType = "Position";
    String title = new String(sortType + " Filter");
    this.setTitle(title);
    try {
      jbInit(location);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit(Point location) throws Exception {
    this.setResizable(false);
    maxSlider.addChangeListener(new MySliderChangeListener(true));
    maxSlider.setOrientation(JSlider.VERTICAL);
    maxSlider.setPaintTicks(true);
    maxSlider.setPaintLabels(true);
    minSlider.addChangeListener(new MySliderChangeListener(false));
    minSlider.setOrientation(JSlider.VERTICAL);
    minSlider.setPaintTicks(true);
    minSlider.setPaintLabels(true);
    maxText.setHorizontalAlignment(JTextField.CENTER);
    minText.setHorizontalAlignment(JTextField.CENTER);
    okButton.setText("OK");
    okButton.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        okButton_actionPerformed(e);
      }
    });
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        cancelButton_actionPerformed(e);
      }
    });
    panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
    JPanel maxPanel=new JPanel();
    maxPanel.setLayout(new BoxLayout(maxPanel,BoxLayout.Y_AXIS));
    maxPanel.add(new JLabel("Max " + sortType));
    JPanel maxSliderPanel=new JPanel();
    maxSliderPanel.add(maxSlider);
    maxPanel.add(maxSliderPanel);
    maxPanel.add(maxText);

    JPanel minPanel=new JPanel();
    minPanel.setLayout(new BoxLayout(minPanel,BoxLayout.Y_AXIS));
    minPanel.add(new JLabel("Min " + sortType));
    JPanel minSliderPanel=new JPanel();
    minSliderPanel.add(minSlider);
    minPanel.add(minSliderPanel);
    minPanel.add(minText);

    JPanel topInnerPanel=new JPanel();
    topInnerPanel.setLayout(new BoxLayout(topInnerPanel,BoxLayout.X_AXIS));
    topInnerPanel.add(Box.createHorizontalStrut(10));
    topInnerPanel.add(minPanel);
    topInnerPanel.add(Box.createHorizontalStrut(10));
    topInnerPanel.add(maxPanel);
    topInnerPanel.add(Box.createHorizontalStrut(10));

    JPanel buttonPanel=new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(okButton);
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(cancelButton);
    buttonPanel.add(Box.createHorizontalGlue());

    panel.add(topInnerPanel);
    panel.add(Box.createVerticalStrut(10));
    panel.add(buttonPanel);
    panel.add(Box.createVerticalStrut(10));
    getContentPane().add(panel);
    pack();
    setLocation(location);
  }

  void setMinAndMax(int min, int max) {
     maxSlider.setMinimum(min);
     maxSlider.setMaximum(max);
     minSlider.setMinimum(min);
     minSlider.setMaximum(max);
     maxSlider.setValue(max);
     minSlider.setValue(min);
  }

  void setCurrentHighLimit(int highLimit) {
     maxSlider.setValue(highLimit);
     maxText.setText(Integer.toString(highLimit));
  }

  void setCurrentLowLimit(int lowLimit) {
     minSlider.setValue(lowLimit);
     minText.setText(Integer.toString(lowLimit));
  }

  void okButton_actionPerformed(ActionEvent e) {
     this.setVisible(false);
     this.dispose();
  }

  void cancelButton_actionPerformed(ActionEvent e) {
     this.setVisible(false);
     this.dispose();
  }

  void addHighLimitDocumentListener(DocumentListener listener) {
     maxText.getDocument().addDocumentListener(listener);
  }

  void addLowLimitDocumentListener(DocumentListener listener) {
     minText.getDocument().addDocumentListener(listener);
  }

  void removeHighLimitDocumentListener(DocumentListener listener) {
     maxText.getDocument().removeDocumentListener(listener);
  }

  void removeLowLimitDocumentListener(DocumentListener listener) {
     minText.getDocument().removeDocumentListener(listener);
  }

  void addOKButtonListener(ActionListener listener) {
     okButton.addActionListener(listener);
  }

  void addCancelButtonListener(ActionListener listener) {
     cancelButton.addActionListener(listener);
  }

  void removeOKButtonListener(ActionListener listener) {
     okButton.removeActionListener(listener);
  }

  void removeCancelButtonListener(ActionListener listener) {
     cancelButton.removeActionListener(listener);
  }



  int getHighLimit() {
     String limit=maxText.getText();
     int limitInt;
     try {
       limitInt=Integer.decode(limit).intValue();
     }
     catch (Exception ex) {
       return maxSlider.getMaximum();
     }
     return limitInt;
  }

  int getLowLimit() {
     String limit=minText.getText();
     int limitInt;
     try {
       limitInt=Integer.decode(limit).intValue();
     }
     catch (Exception ex) {
       return minSlider.getMinimum();
     }
     return limitInt;
  }

  class MySliderChangeListener implements ChangeListener {
    private boolean maxChange;

    public MySliderChangeListener(boolean maxChange) {
       this.maxChange=maxChange;
    }
    public void stateChanged(ChangeEvent e){
       if (maxChange) {
          maxText.setText(Integer.toString(maxSlider.getValue()));
          if (maxSlider.getValue()<minSlider.getValue()) minSlider.setValue(maxSlider.getValue());
       }
       else {
         minText.setText(Integer.toString(minSlider.getValue()));
         if (minSlider.getValue()>maxSlider.getValue()) maxSlider.setValue(minSlider.getValue());
       }
    }

  }

}
