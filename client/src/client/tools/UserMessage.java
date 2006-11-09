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
package client.tools;

import javax.swing.*;
import java.util.*;

public class UserMessage {

  static private ResourceBundle rb;

  static {
    try {
      rb=ResourceBundle.getBundle("UserMessage");
    }
    catch (java.util.MissingResourceException mre) {
      System.exit(0);
    }
  }

  public UserMessage() {
    displayDialog();
    System.exit(getExitValue());
  }

  private void displayDialog(){
     JFrame mainFrame = new JFrame();
     JOptionPane optionPane = new JOptionPane();
//     mainFrame.setIconImage((new ImageIcon(this.getClass().getResource("/resource/client/images/window_icon.gif")).getImage()));
     mainFrame.getContentPane().add(optionPane);
     if (getExitValue() ==0)
        optionPane.showMessageDialog(mainFrame, getText(), "Information", JOptionPane.INFORMATION_MESSAGE);
     else
        optionPane.showMessageDialog(mainFrame, getText(), "Error", JOptionPane.ERROR_MESSAGE);

  }

  private int getExitValue(){
     String valueStr= rb.getString("exitValue");
     if (valueStr==null) System.exit(0);
     int rtn=0;
     try{
       rtn=Integer.parseInt(valueStr);
     }
     catch (Exception ex) {System.exit(0);}
     return rtn;
  }

  private String[] getText() {
     String valueStr= rb.getString("textValue");
     if (valueStr==null) System.exit(0);
     return new String[]{valueStr};
  }

  public static void main(String[] args) {
    UserMessage userMessage = new UserMessage();
    userMessage.invokedStandalone = true;
  }
  private boolean invokedStandalone = false;
}