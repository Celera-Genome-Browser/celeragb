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
package client.gui.framework.browser;

import shared.util.FreeMemoryWatcher;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatusBar  extends JPanel {
   private FreeMemoryViewer freeMemoryViewer;
   private BoxLayout boxLayout=new BoxLayout(this,BoxLayout.X_AXIS);
   private JLabel label=new JLabel();
   private Component glue=Box.createHorizontalGlue();

   public StatusBar() {
      setLayout(boxLayout);
      add(label);
      add(glue);

      this.addComponentListener(new ComponentAdapter(){
          /**
           * Invoked when the component's size changes.
           */
          public void componentResized(ComponentEvent e) {
              resetPreferredSize();
              // This series of calls MUST BE DONE to ensure that
              // this widget is redrawn to reflect the new sizes
              // of its child widgets on screen.  Please do not
              // change this to "validate/repaint" or any other
              // combination, because it just does not work!  Maybe
              // because this method is always called AFTER the
              // container has been repainted.
              invalidate();
              validate();
              repaint();
          }

      });
   }

   public void setDescription(String description) {
       label.setText(description);
       label.setToolTipText(description);
       if (freeMemoryViewer != null) {
           resetPreferredSize();
       }
   }

   public void useFreeMemoryViewer(boolean use){
    if (use) {
      if (freeMemoryViewer==null) freeMemoryViewer =new FreeMemoryViewer();
      FreeMemoryWatcher.getFreeMemoryWatcher().addObserver(freeMemoryViewer);
      add(freeMemoryViewer);
      validate();
    }
    else {
      if (freeMemoryViewer!=null) {
        remove(freeMemoryViewer);
        FreeMemoryWatcher.getFreeMemoryWatcher().deleteObserver(freeMemoryViewer);
        freeMemoryViewer=null;
        label.setText(label.getText());
        this.repaint();
      }
     }
    }

    private void resetPreferredSize() {
      if (this.getParent() != null) {
        int height = this.getHeight();
        int width = this.getParent().getWidth() - freeMemoryViewer.getWidth();
        label.setPreferredSize(new Dimension(width, height));
      }
    }
}
