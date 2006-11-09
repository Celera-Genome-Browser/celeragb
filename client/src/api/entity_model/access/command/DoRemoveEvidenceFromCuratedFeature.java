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
import api.entity_model.model.annotation.CuratedFeature;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.annotation.SuperFeature;
import api.entity_model.model.fundtype.Axis;
import api.stub.data.OID;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;


public class DoRemoveEvidenceFromCuratedFeature extends FeatureStructureBoundedCommand {

    private String cmdName = "Remove Evidence From Curated Feature";
    private Feature evidence;
    private CuratedFeature feature;

 public DoRemoveEvidenceFromCuratedFeature(Axis anAxis,Feature evidence,
                                                  CuratedFeature feature) {
        super(anAxis);
        this.evidence = evidence;
        this.feature = feature;
        this.setIsActingAsUndoAndUndoName(false, null);
    }

  /**
     * Invoked BEFORE the command is executed.
     * @returns the set of pre-existing root features that will be affected.
     */
    public HashSet getCommandSourceRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      rootFeatureSet.add(feature.getRootFeature());
      return rootFeatureSet;
    }

    /**
     * Invoked AFTER the command is executed.
     * @returns the set of root features that display changes "after" the command has executed.
     */
    public HashSet getCommandResultsRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      rootFeatureSet.add(feature.getRootFeature());
      return rootFeatureSet;
    }

    /**
     * Execute the command with out returning the undo.
     * The undo will be created for us by the GeneBoundaryCommand super-class.
     */
    public void executeWithNoUndo() throws Exception {
         doRemoveEvidenceFromCuratedFeature(evidence, feature);
         setFocusEntity(feature);
         this.timeofCommandExecution=new Date().toString();
    }

     protected OID getUndoFocusOID() {
      return feature.getOid();
    }

    protected OID getRedoFocusOID() {
      return feature.getOid();
    }


    private void doRemoveEvidenceFromCuratedFeature(Feature ev, CuratedFeature feat) {

      CuratedFeature.CuratedFeatureMutator featureMutator = mutatorAcceptor.getCuratedFeatureMutatorFor(feature);

            // if composite evidence, then recurse down through composite to add leafs to existing curation
            if (!evidence.isSimple()&& (((Feature)evidence).getSubFeatureCount()!=0)) {

                SuperFeature parent_feature = (SuperFeature)evidence;
                Feature sub_feature;
                Collection subFeatures = parent_feature.getSubFeatures();
                Iterator it=subFeatures.iterator();
                while (it.hasNext()) {
                    sub_feature = (Feature)it.next();
                    doRemoveEvidenceFromCuratedFeature(sub_feature, feature);
                }
            }

           else {
              /*
              if (!(evidence instanceof SingleAlignmentSingleAxis)) {
                // todo: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s).
                System.out.println("DoRemoveEvidenceFromCuratedFeature: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s)");
              }
              */
              featureMutator.removeEvidence(ev);

        }
    }



    public String toString() {
        return cmdName;
    }

     public String getCommandLogMessage(){

       String featureid=feature.getOid().toString();
       String featureType=feature.getEntityType().toString();
       String evidenceType=evidence.getEntityType().toString();
       String evidenceId=evidence.getOid().toString();
       this.actionStr="Removed Evidence "+evidenceId+" with type="+evidenceType +" from feature id= "+featureid+" and feature type="+featureType;
       return(super.getCommandLogMessage());


    }
}