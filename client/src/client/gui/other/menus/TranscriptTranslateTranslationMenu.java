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
package client.gui.other.menus;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Deepali Bhandari
 * @version $Id$
 */

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.*;
/**
 *  Subtype of Translation for TranscriptTranslateView only for toggling off/on
 *  of starts and stops
 */


public class TranscriptTranslateTranslationMenu extends TranslationMenu{
    public JMenu  toggleStartStopHighlightMenu = new JMenu("Display of Starts And Stops");
    public JCheckBoxMenuItem  transOneToggleStartStopHighlightMI= new JCheckBoxMenuItem("+1 ORF");
    public JCheckBoxMenuItem  transTwoToggleStartStopHighlightMI= new JCheckBoxMenuItem("+2 ORF");
    public JCheckBoxMenuItem  transThreeToggleStartStopHighlightMI= new JCheckBoxMenuItem("+3 ORF");
    public JCheckBoxMenuItem  translateEntireTranscriptMI = new JCheckBoxMenuItem("Translate Entire Transcript");


    public TranscriptTranslateTranslationMenu (ActionListener aL) {

    super (aL);
    add (new JSeparator());

    add(translateEntireTranscriptMI);
    translateEntireTranscriptMI.setState(false);
    translateEntireTranscriptMI.addActionListener(actionListener);

    add(toggleStartStopHighlightMenu);
    toggleStartStopHighlightMenu.addActionListener(actionListener);

    toggleStartStopHighlightMenu.add( transOneToggleStartStopHighlightMI);
    transOneToggleStartStopHighlightMI.addActionListener(actionListener);
    transOneToggleStartStopHighlightMI.setSelected(true);

    toggleStartStopHighlightMenu.add(transTwoToggleStartStopHighlightMI);
    transTwoToggleStartStopHighlightMI.addActionListener(actionListener);
    transTwoToggleStartStopHighlightMI.setSelected(true);

    toggleStartStopHighlightMenu.add(transThreeToggleStartStopHighlightMI);
    transThreeToggleStartStopHighlightMI.addActionListener(actionListener);
    transThreeToggleStartStopHighlightMI.setSelected(true);
  }



  public List getFrameNumbersForMenuItemsSelected(){
    List retFrameList=new ArrayList();
    if(transOneToggleStartStopHighlightMI.getState()){
      retFrameList.add(new Integer(1));
    } if(transTwoToggleStartStopHighlightMI.getState()){
      retFrameList.add(new Integer(2));
    } if(transThreeToggleStartStopHighlightMI.getState()){
      retFrameList.add(new Integer(3));
    }
    return retFrameList;
  }


}