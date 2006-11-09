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
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.fundtype.Axis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;


/**
 * This class is meant to be a helper for the DoSwapFeaturesForTranscript.  It
 * takes a new Transcript and a collection of the Transcript's old features, and
 * assigns the applicable old features' evidence to the new Transcript's children.
 */
public class DoExchangeEvidenceWithReplacementFeatures extends FeatureStructureBoundedCommand {

  private static final String COMMAND_NAME = "Exchange Evidence With Replacement Feature";
  private Feature targetTranscript;
  private ArrayList removedSubFeatures;

  public DoExchangeEvidenceWithReplacementFeatures(Axis anAxis, Feature targetTranscript,
    ArrayList removedSubFeatures) {
    super(anAxis);
    this.targetTranscript = targetTranscript;
    this.removedSubFeatures = removedSubFeatures;
    this.setFocusEntity(targetTranscript);
  }


  /**
   * This method takes care of the evidence swapping from old features to the new.
   */
  public void executeWithNoUndo() {
    // Ensure that the new exons shared the evidence relationship with the old features.
    for (Iterator it = targetTranscript.getSubFeatures().iterator(); it.hasNext();) {
      Feature tmpFeature = (Feature)it.next();

      Feature.FeatureMutator featureMutator = mutatorAcceptor.getFeatureMutatorFor(tmpFeature);
      //  There should be at least one evidence or something upstream is broken.
      Feature targetFeature = (Feature)tmpFeature.getEvidence().iterator().next();
      // Loop through the removed features and find if the evidence was shared.
      for (Iterator it2 = removedSubFeatures.iterator(); it2.hasNext(); ) {
        Collection oldEvidenceFeatures;
        Feature removedFeature = (Feature)it2.next();
        oldEvidenceFeatures = removedFeature.getEvidence();
        if (oldEvidenceFeatures.contains(targetFeature)) {
          ArrayList evidenceOIDs = new ArrayList();
          for (Iterator it3 = oldEvidenceFeatures.iterator(); it3.hasNext();) {
            evidenceOIDs.add(((Feature)it3.next()).getOid());
          }
          featureMutator.addAllEvidenceOids(evidenceOIDs);
          System.out.println("Assigning evidence to "+tmpFeature.toString());
          break;
        }
      }
    }
  }



  /**
   * This method should return the original of the command object execution.  In
   * this case it means the transcript before it has had the new exons added.
   */
  public HashSet getCommandSourceRootFeatures(){
    HashSet tmpSet = new HashSet();
    tmpSet.add(targetTranscript.getRootFeature());
    return tmpSet;
  }


  /**
   * This method should return the result of the command object execution.  In
   * this case it means the transcript after it has had the new exons added.
   */
  public HashSet getCommandResultsRootFeatures(){
    HashSet tmpSet = new HashSet();
    tmpSet.add(targetTranscript.getRootFeature());
    return tmpSet;
  }


  /**
   * This method is used by the Command Object execution mechanism as the text
   * to display in the Undo/Redo buttons.
   */
  public String toString() {
      return COMMAND_NAME;
  }
}