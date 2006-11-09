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
package api.entity_model.access.command;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */
import api.entity_model.management.CompositeCommand;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.assembly.GenomicAxis;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * This class wipes out a Workspace Transcript's exons and start/stop codons and
 * adds the HSP's or Detail features from an evidence HitAlignment.
 */
public class DoSwapFeaturesForTranscript extends CompositeCommand {

  private static String COMMAND_NAME = "Swap Evidence Feature With Curated Transcript";
  private Feature targetTranscript;
  private Feature replacementTranscript;
  private GenomicAxis axis;

  public DoSwapFeaturesForTranscript(GenomicAxis anAxis, Feature targetTranscript,
      Feature replacementTranscript) {
    super(COMMAND_NAME);
    this.axis = anAxis;
    this.targetTranscript = targetTranscript;
    this.replacementTranscript = replacementTranscript;
    setFocusEntity(targetTranscript);
    formatCommandsStructure();
  }


  /**
   * These are the three main actions of this command object.  Unfortunately,
   * we had to break the evidence relationship swap into a separate command object.
   * This was for undo/redo reasons.
   */
  public void formatCommandsStructure() {
    // The list of removed subfeatures will be used to reestablish the evidenciary
    // relationships.
    ArrayList removedSubFeatures = new ArrayList();

    // Loop through the Curated Transcript and remove all of the exons and store temporarily.
    for (Iterator it = targetTranscript.getSubFeatures().iterator(); it.hasNext();) {
      Feature tmpFeature = (Feature)it.next();
      Alignment tmpAlignment = tmpFeature.getOnlyAlignmentToAnAxis(axis);
      if (tmpAlignment != null) {
        removedSubFeatures.add(tmpFeature);
        this.addNextCommand(new DoDeleteCuration(tmpAlignment, true));
      }
    }

    // Create and attach new exons to the old transcript.
    for (Iterator it = replacementTranscript.getSubFeatures().iterator(); it.hasNext();) {
      this.addNextCommand(new DoAddEvidenceAndCreateExonIfNecessary(axis,
        (Feature)it.next(), (CuratedTranscript)targetTranscript));
    }

    //  Swap Old Evidences onto new features.
    this.addNextCommand(new DoExchangeEvidenceWithReplacementFeatures(axis,
      targetTranscript, removedSubFeatures));
  }
}