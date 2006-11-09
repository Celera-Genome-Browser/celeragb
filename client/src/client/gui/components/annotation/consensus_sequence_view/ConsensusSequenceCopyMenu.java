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

package client.gui.components.annotation.consensus_sequence_view;

import java.awt.event.ActionListener;
import javax.swing.*;


public class ConsensusSequenceCopyMenu extends JMenu
{
  private ActionListener actionListener;
  public JMenu aminoAcidCopyMenu = new JMenu("Amino Acid Sequence");
  public JMenuItem AAPlus1MI = new JMenuItem("+1 ORF");
  public JMenuItem AAPlus2MI = new JMenuItem("+2 ORF");
  public JMenuItem AAPlus3MI = new JMenuItem("+3 ORF");
  public JMenuItem nucleotideMI =  new JMenuItem("Nucleotide Sequence");

  public ConsensusSequenceCopyMenu (String name,ActionListener aL) {
    //super ("Copy Selection");
    super(name);
    this.setMnemonic('C');
    actionListener = aL;
    add(nucleotideMI);
    aminoAcidCopyMenu.add(AAPlus1MI);
    aminoAcidCopyMenu.add(AAPlus2MI);
    aminoAcidCopyMenu.add(AAPlus3MI);
    add(aminoAcidCopyMenu);
    nucleotideMI.addActionListener(actionListener);
    AAPlus1MI.addActionListener(actionListener);
    AAPlus2MI.addActionListener(actionListener);
    AAPlus3MI.addActionListener(actionListener);
  }

}  // end public class ConsensusResiduesCopyMenu extends JMenu


