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

import shared.util.FreeMemoryWatcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observable;
import java.util.Observer;

public class FreeMemoryViewer extends JPanel implements Observer {
  private BoxLayout boxLayout=new BoxLayout(this,BoxLayout.X_AXIS);
  private JProgressBar bar;
  private JLabel label = new JLabel("  Mem Usage  ");
  private long totalMemory;
  private static int FIRST_WARNING_PERCENT=10;
  private static int SECOND_WARNING_PERCENT=5;
  private static int FINAL_WARNING_PERCENT=1;
  private static int BAR_HEIGHT;
  private static int BAR_WIDTH=200;
  private static int RED_BAR=10;
  private static int YELLOW_BAR=30;
  private boolean reachedFirstWarningPercent;
  private boolean reachedSecondWarningPercent;
  private boolean reachedFinalWarningPercent;

  public FreeMemoryViewer() {
    bar=new JProgressBar(JProgressBar.HORIZONTAL,0,100);
	BAR_HEIGHT = this.getFontMetrics(this.getFont()).getHeight();
    bar.setMaximumSize(new Dimension(BAR_WIDTH,BAR_HEIGHT));
    bar.setStringPainted(true);
    setLayout(boxLayout);
    this.add(label);
    this.add(bar);
    this.addMouseListener(new MouseListener(){
       public void mouseClicked(MouseEvent e){
          if (e.getClickCount()==2) {
             showMemoryDialog();
          }
       }
       public void mousePressed(MouseEvent e){}
       public void mouseReleased(MouseEvent e){}
       public void mouseEntered(MouseEvent e){}
       public void mouseExited(MouseEvent e){}
    });
  }

  public void update(Observable observable, Object obj) {
      if (observable instanceof shared.util.FreeMemoryWatcher &&
          obj instanceof Integer) {
           int value=((Integer)obj).intValue();
           if (value >= YELLOW_BAR)
             bar.setForeground(Color.green);
           if (value >= RED_BAR &&  value < YELLOW_BAR)
             bar.setForeground(Color.yellow);
          //   System.out.println("yello reached");
           if (value < RED_BAR)
             bar.setForeground(Color.red);
           bar.setValue(100-value);
           checkWarning (value);
      }
  }

  /**
   * Overrides of getters for sizeing, so that layout managers will not
   * allow this widget to be obliterated.
   */
  public Dimension getPreferredSize() {
    int width = BAR_WIDTH + label.getWidth();
    int height = this.getFontMetrics(this.getFont()).getHeight() + 4;
    return new Dimension(width, height);
  }

  public Dimension getMaximumSize() {
    return getPreferredSize();
  }

  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  private void checkWarning(int percentRemaining) {
     if (percentRemaining>FIRST_WARNING_PERCENT) {
        reachedSecondWarningPercent=false;
        return;
     }

     if (percentRemaining<=FIRST_WARNING_PERCENT &&
         percentRemaining>SECOND_WARNING_PERCENT &&
         !reachedFirstWarningPercent) {

            showMemWarningDialog(percentRemaining);
            reachedFirstWarningPercent=true;
            reachedFinalWarningPercent=false;
     }

     if (percentRemaining<=SECOND_WARNING_PERCENT &&
         percentRemaining>FINAL_WARNING_PERCENT &&
         !reachedSecondWarningPercent) {

            showMemWarningDialog(percentRemaining);
            reachedFirstWarningPercent=true;
            reachedSecondWarningPercent=true;
     }

     if (percentRemaining<=FINAL_WARNING_PERCENT && !reachedFinalWarningPercent) {

            showMemErrorDialog(percentRemaining);
            reachedFirstWarningPercent=true;
            reachedSecondWarningPercent=true;
            reachedFinalWarningPercent=true;
     }


  }

  private void showMemWarningDialog(int percentAvailable) {
    String[] strings=new String[1];
    strings[0]="Available memory is currently at "+percentAvailable+" percent.";
    JOptionPane.showMessageDialog(this.getParent().getParent(), strings, "Warning: Low Available Memory", JOptionPane.WARNING_MESSAGE);
  }

  private void showMemErrorDialog(int percentAvailable) {
    String[] strings=new String[1];
    strings[0]="Available memory is currently at "+percentAvailable+" and is critically low!";
    JOptionPane.showMessageDialog(this.getParent().getParent(), strings, "Error: Critically Low Available Memory", JOptionPane.ERROR_MESSAGE);
  }

  private void showMemoryDialog() {
    String[] strings=new String[6];
    strings[0]="Total Memory: "+(getTotalMemory()/1024)+" KB";
    strings[1]="Free Memory: "+(getFreeMemory()/1024)+" KB";
    strings[2]="-----------------------------------";
    strings[3]="Used Memory: "+(getUsedMemory()/1024)+" KB";
    strings[4]="                                   ";
    strings[5]="Would you like to compact memory now?";
    int ans=JOptionPane.showConfirmDialog(this.getParent().getParent(), strings, "Memory Usage", JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE);
    if (ans==JOptionPane.YES_OPTION) System.gc();
  }

  private long getTotalMemory() {
    return FreeMemoryWatcher.getFreeMemoryWatcher().getTotalMemory();
  }

  private long getUsedMemory() {
    return FreeMemoryWatcher.getFreeMemoryWatcher().getUsedMemory();
  }

  private long getFreeMemory() {
    return FreeMemoryWatcher.getFreeMemoryWatcher().getFreeMemory();
  }

}

/*
$Log$
Revision 1.1  2006/11/09 21:36:13  rjturner
Initial upload of source

Revision 1.15  2003/08/13 02:14:11  masonda
Fixed truncated font

Revision 1.14  2002/06/27 16:47:34  BhandaDn
evidence can be obtained only  on the leaves in the tree data model so corrected
the assumptiom of transcripts having evidences in  printfasta method

Revision 1.13  2001/10/18 16:03:41  lfoster
Fixed bug 109.  Now the label in the status bar reports a size adequate to allow the memory meeter to show.  AND now a tooltip will display OVER the label.

Revision 1.12  2000/12/13 18:12:29  pdavies
Fixes error messages

Revision 1.8.4.5  2000/12/13 18:11:24  pdavies
Fixes error messages

Revision 1.8.4.4  2000/12/04 20:01:29  pdavies
Refers the user to the documentation for changing memory settings

Revision 1.8.4.3  2000/10/06 18:15:45  pdavies
Changed memory viewer to used memory

Revision 1.8.4.2  2000/09/29 17:02:56  pdavies
Now has popups when memory becomes critical

Revision 1.8.4.1  2000/09/22 19:27:56  pdavies
Better handles total memory

Revision 1.8  2000/08/31 21:46:02  tsaf
JOptionPane icons set to GB icon.

Revision 1.7  2000/08/11 18:51:05  pdavies
Changed wording of garbage collection suggestion

Revision 1.6  2000/08/11 18:47:12  pdavies
Added garbage collection suggestion

Revision 1.5  2000/02/11 13:36:33  pdavies
Mods for new Package structure

Revision 1.4  1999/10/22 14:45:08  DaviesPE
Adds double-clicking to the free memory viewer for pop-up of more concise memory information

Revision 1.3  1999/09/21 18:07:32  DaviesPE
*/
