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

package client.gui.components.annotation.transcript_translate_view;

import java.awt.event.ActionListener;
import javax.swing.*;

public class TranscriptTranslateCopyMenu extends JMenu
{
  private ActionListener actionListener;


  //region selection menu
  public JMenu copySelectedRegionMenu = new JMenu("Selected Region");
  public JMenuItem selectedRegionNT_MI = new JMenuItem("Nucleotide");
  public JMenu selectedRegionAAMenu = new JMenu("Amino Acid");
  public JMenuItem selectedRegionAA1_MI = new JMenuItem("+1 ORF");
  public JMenuItem selectedRegionAA2_MI = new JMenuItem("+2 ORF");
  public JMenuItem selectedRegionAA3_MI = new JMenuItem("+3 ORF");

  //translation region selection
  public JMenu copyTranscriptORFMenu = new JMenu("Transcript ORF");
  public JMenuItem transcriptORFNT_MI = new JMenuItem("Nucleotide");
  public JMenuItem transcriptORFAA_MI = new JMenuItem("Amino Acid");

  //entire transcript
  public JMenu copyEntireTranscriptMenu = new JMenu("Entire Transcript");
  public JMenuItem entireTranscriptNT_MI = new JMenuItem("Nucleotide");
  public JMenu entireTranscriptAAMenu = new JMenu("Amino Acid");
  public JMenuItem entireTranscriptAA1_MI = new JMenuItem("+1 ORF");
  public JMenuItem entireTranscriptAA2_MI = new JMenuItem("+2 ORF");
  public JMenuItem entireTranscriptAA3_MI = new JMenuItem("+3 ORF");


  public TranscriptTranslateCopyMenu (String name,ActionListener aL) {

    super (name);

    actionListener = aL;

    selectedRegionAAMenu.add(selectedRegionAA1_MI);
    selectedRegionAAMenu.add(selectedRegionAA2_MI);
    selectedRegionAAMenu.add(selectedRegionAA3_MI);
    selectedRegionAA1_MI.addActionListener(actionListener);
    selectedRegionAA2_MI.addActionListener(actionListener);
    selectedRegionAA3_MI.addActionListener(actionListener);
    copySelectedRegionMenu.add(selectedRegionNT_MI);
    selectedRegionNT_MI.addActionListener(actionListener);
    copySelectedRegionMenu.add(selectedRegionAAMenu);



    copyTranscriptORFMenu.add(transcriptORFAA_MI);
    copyTranscriptORFMenu.add(transcriptORFNT_MI);
    transcriptORFNT_MI.addActionListener(actionListener);
    transcriptORFAA_MI.addActionListener(actionListener);


    entireTranscriptAAMenu.add(entireTranscriptAA1_MI);
    entireTranscriptAAMenu.add(entireTranscriptAA2_MI);
    entireTranscriptAAMenu.add(entireTranscriptAA3_MI);
    entireTranscriptAA1_MI.addActionListener(actionListener);
    entireTranscriptAA2_MI.addActionListener(actionListener);
    entireTranscriptAA3_MI.addActionListener(actionListener);
    copyEntireTranscriptMenu.add(entireTranscriptNT_MI);
    entireTranscriptNT_MI.addActionListener(actionListener);
    copyEntireTranscriptMenu.add(entireTranscriptAAMenu);

    add(copySelectedRegionMenu);
    add(copyTranscriptORFMenu);
    add(copyEntireTranscriptMenu);


  }
}
