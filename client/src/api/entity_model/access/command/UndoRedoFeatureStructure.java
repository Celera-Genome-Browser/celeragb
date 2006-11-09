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


import api.entity_model.management.ModelMgr;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.annotation.CuratedFeature;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.annotation.WorkspaceToken;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.GenomicEntity;
import api.stub.data.OID;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 *
 * This class is a command.
 * @author Jay T. Schira
 * @version $Id$
 */
public class UndoRedoFeatureStructure extends FeatureStructureBoundedCommand
{
    private static final boolean DEBUG_CLASS = false;
    // "Source" are the source features of the previous command that this command is
    // going to Undo.  These are NOT the source of "this" command.
    // This command will put these "source" items back when executed.
    private boolean previousCommandsSourceRootInitialzied = false;
    private ArrayList listOfSerializedSourceFeatures = new ArrayList();
    private HashMap sourceAlignableGEToAlignmentsMap = new HashMap();  // A Hashmap, key of Feature
    private WorkspaceToken sourceWorkspaceToken;
    // "Results" are the results of the previous command the this command is
    // going to Undo.  These are NOT the results of "this" command.
    // This command will remove these results when executed.
    private boolean previousCommandsResultsRootInitialzied = false;
    private ArrayList previousCommandsResultsRootOids = new ArrayList();
    private boolean isRedo = false;  // Can be toggled between undo and redo.
    private OID undoFocusOID;
    private OID redoFocusOID;

    private HashSet myResultsRootFeatures = new HashSet();
    private String undoRedoCommandLogMessage = null;

    /**
     * Constructor with a set of affectedGenes...
     */
    public UndoRedoFeatureStructure(Axis anAxis, String undoName) {
        super(anAxis);
        this.undoCommandName = undoName;
        this.isActingAsUndo = true;
    }

    public void setUndoRedoFocusOIDs(OID undoOID, OID redoOID) {
        undoFocusOID = undoOID;
        redoFocusOID = redoOID;
    }

  /**
   * This is a late-construction behavior.
   * Set the pre-command root features.
   * Save off the tokens of the workspace and the state of the feature structure.
   */
  public void initializeForPreviousCommandsSourceRootFeatureSet(Set someRootFeatures) {
    if (DEBUG_CLASS) System.out.println("ENTER >> UndoRedoGeneSet.initializeForPreviousCommandsSourceRootFeatureSet(Set someRootFeatures));");
    // Save off the feature list... (as OIDs so we don't cause a memory leak).
    // sourceRootFeatures.addAll(someRootFeatures);
    // Clone off all the feature structure...
    // Clean out the list of Serialized soruce features...
    listOfSerializedSourceFeatures.clear();
    byte aSerialzedEntityStructure[];
    Feature aRootFeature;
    for (Iterator itr = someRootFeatures.iterator(); itr.hasNext(); ) {
      // Get the next rootFeature
      aRootFeature = (Feature)itr.next();

      // Serialize the rootFeeature's structure and save it off into a list...
      aSerialzedEntityStructure = this.getSerializedEntityStructureAsByteArray(aRootFeature);
      listOfSerializedSourceFeatures.add(aSerialzedEntityStructure);

      // Now we need to walk the tree and save off copies of the Alignments...
      this.walkFeatureStructureAndSaveAlignments(aRootFeature);
    }

    // Save off the Workspace Token...
    sourceWorkspaceToken = workspace.getUndoToken();
    // Mark as initialized...
    previousCommandsSourceRootInitialzied = true;
    if (DEBUG_CLASS) System.out.println("EXIT << UndoRedoGeneSet.initializeForPreviousCommandsSourceRootFeatureSet(Set someRootFeatures));");
  }


  /**
   * Get the serialized entity structure as a byte array.
   */
  private byte[] getSerializedEntityStructureAsByteArray(GenomicEntity rootEntity) {
    if (DEBUG_CLASS) System.out.println("ENTER >> UndoRedoGeneSet.getSerializedEntityStructureAsByteArray(GenomicEntity="
                                        + rootEntity.getOid() + ");");
    byte serializedEntityStructure[] = null;
    try {
      // Set up a ByteArrayOutputStream with an initial size...
      ByteArrayOutputStream baOutStream = new ByteArrayOutputStream(100);
      // Set up an ObjectOutputStream...
      ObjectOutputStream objOutStream = new ObjectOutputStream(baOutStream);
      // Write out the source root entity to the ObjectOutputStream...
      objOutStream.writeObject(rootEntity);
      // Get the byte buffer...
      serializedEntityStructure = baOutStream.toByteArray();
      // Close the
      objOutStream.close();
      // Return byte array
    }
    catch (Exception ex) {
      ModelMgr.getModelMgr().handleException(ex);
    }
    if (DEBUG_CLASS) {
      if (serializedEntityStructure != null) System.out.println("RETURNING byte[] of length = "
                                            + serializedEntityStructure.length);
      System.out.println("EXIT << UndoRedoGeneSet.getSerializedEntityStructureAsByteArray(GenomicEntity="
                                        + rootEntity.getOid() + ");");
    }
    return serializedEntityStructure;
  }


  /**
   * @todo: This may be removed in favor of the serialization method.
  private FeatureStructureMemento initializeForSourceRootFeature(Feature aSourceRootFeature) {
    // We have a Feature...
    // Create a new FeatureStructureToken
    FeatureStructureMemento featureStructureMemento = new FeatureStructureMemento(axis);
    // Pass it in to the copyFeatureStructure(FeatureStructureToken aFST);
    // Set the root feature
    featureStructureMemento.setRootFeature(aSourceRootFeature.cloneForUndoRedo(null, featureStructureMemento));
    // Return the FeatureStructureMemento...
    return featureStructureMemento;
  }
   */


  /**
   * Walk a feature structure (from it's root) and save off the alignments...
   */
  private void walkFeatureStructureAndSaveAlignments(Feature aFeature) {
    if (aFeature == null) return;
    if (DEBUG_CLASS) {
      System.out.println("ENTER >> UndoRedoGeneSet.walkFeatureStructureAndSaveAlignments(Feature="
                                        + aFeature.getOid() + ");");
      System.out.print("Saving alingment to feature: " + aFeature.getEntityType().getEntityName()
                                + "." + aFeature.getOid());
      if (aFeature.getSuperFeature() != null) {
              System.out.println("Feature has SuperFeature: " + aFeature.getSuperFeature().getOid());
      }
      else {
              System.out.println("Feature has NO SuperFeature!");
      }
    }

    // First get the alignments for this feature...
    Set aSet = aFeature.getAlignmentsToAxis(axis);
    if (!aSet.isEmpty()) {
      Alignment[] originalAlignments=(Alignment[])aSet.toArray(new Alignment[0]);
      Alignment[] clonedAligments = new Alignment[originalAlignments.length];
      for (int i=0; i<originalAlignments.length; i++) {
        clonedAligments[i] = originalAlignments[i].cloneWithNewEntity(null);
      }
      sourceAlignableGEToAlignmentsMap.put(aFeature.getOid(), clonedAligments);
    }

    // Walk the sub-structure...
    Collection subStructureColl = aFeature.getSubStructure();
    for (Iterator itr = subStructureColl.iterator(); itr.hasNext(); ) {
      this.walkFeatureStructureAndSaveAlignments((Feature)itr.next());
    }
    if (DEBUG_CLASS) System.out.println("EXIT << UndoRedoGeneSet.walkFeatureStructureAndSaveAlignments(Feature="
                                        + aFeature.getOid() + ");");
  }


  /**
   * This is a late-construction behavior.
   * Set the post-command root features.
   * Save off the tokens of the workspace and the state of the feature structure.
   */
  public void initializeForPreviousCommandsResultsRootFeatureSet(Set someRootFeatures) {
    if (DEBUG_CLASS) System.out.println("ENTER >> UndoRedoGeneSet.initializeForPreviousCommandsResultsRootFeatureSet(Set);");
    Feature aRootFeature;
    for (Iterator itr = someRootFeatures.iterator(); itr.hasNext(); ) {
      aRootFeature = (Feature)itr.next();
      previousCommandsResultsRootOids.add(aRootFeature.getOid());
    }
    previousCommandsResultsRootInitialzied = true;
    if (DEBUG_CLASS) System.out.println("EXIT << UndoRedoGeneSet.initializeForPreviousCommandsResultsRootFeatureSet(Set);");
  }


  /**
   * Invoked BEFORE the command is executed.
   * @returns the set of root features that will be affected.
   */
  public HashSet getCommandSourceRootFeatures()  {
    if (DEBUG_CLASS) System.out.println("ENTER >> UndoRedoGeneSet.getCommandSourceRootFeatures();");
    HashSet mySourceRootFeatures = new HashSet();
    OID anOid;
    GenomicEntity anEntity;
    for (Iterator itr = previousCommandsResultsRootOids.iterator(); itr.hasNext(); ) {
      anOid = (OID)itr.next();
      anEntity = genomeVersion.getGenomicEntityForOid(anOid);
      if (anEntity != null) mySourceRootFeatures.add(anEntity);
    }
    if (DEBUG_CLASS) System.out.println("EXIT << UndoRedoGeneSet.getCommandSourceRootFeatures();");
    return mySourceRootFeatures;
  }


  /**
   * Execute the command with out returning the undo.
   * The undo will be created for us by the GeneBoundaryCommand super-class.
   */
  public void executeWithNoUndo() {
    if (DEBUG_CLASS) System.out.println(">> ENTER: UndoRedoGeneSet.executeWithNoUndo();");
    // Need to roll OUT the previous command's resulting feature structure.
    this.removeFeatureStructureForRootOids(previousCommandsResultsRootOids);
    // Need to roll IN the previous command's source feature structure.
    myResultsRootFeatures.clear();
    byte aSerialzedEntityStructure[];
    GenomicEntity aRootEntity = null;
    for (Iterator itr = listOfSerializedSourceFeatures.iterator(); itr.hasNext(); ) {
      aSerialzedEntityStructure = (byte[])itr.next();
      aRootEntity = this.getEntityStructureFromByteArray(aSerialzedEntityStructure);
      if (aRootEntity != null) {
        // Set the whole feature structure as "Under Construction", suspending active validation behavior.
        Feature.FeatureMutator aRootEntityMutator = this.mutatorAcceptor.getFeatureMutatorFor((Feature)aRootEntity);
        aRootEntityMutator.setFeatureStructureUnderConstruction(true);
        // Fill in the myResultsRootFeatures with the newly instanciated root features.
        myResultsRootFeatures.add(aRootEntity);
        // Apply the alinments...
        this.walkFeatureStructureAndApplyAlignments((Feature)aRootEntity);
        // Set the whole feature structure as active (no longer constructing)...
        aRootEntityMutator.setFeatureStructureUnderConstruction(false);
      }
    }
    // Need to roll back the workspace...
    workspace.applyUndoToken(sourceWorkspaceToken);

    if (redoFocusOID != null)
        setFocusEntity(axis.getGenomeVersion().getGenomicEntityForOid(redoFocusOID));
    else
        setFocusEntity(null);

    if (DEBUG_CLASS) System.out.println("<< EXIT: UndoRedoGeneSet.executeWithNoUndo();");
  }


  /**
   * Remove the entire structure for the set of root OIDs...
   */
  private void removeFeatureStructureForRootOids(List rootOids) {
    if (DEBUG_CLASS) System.out.println(">> ENTER: UndoRedoGeneSet.removeFeatureStructureForRootOids(List rootOids);");
    OID anOid;
    GenomicEntity anEntity;
    CuratedFeature.CuratedFeatureMutator aRootFeatureMutator, aFeatureMutator;
    if (rootOids == null) return;
    for (Iterator itr = rootOids.iterator(); itr.hasNext(); ) {
      anOid = (OID)itr.next();
      anEntity = this.genomeVersion.getGenomicEntityForOid(anOid);
      if ((anEntity != null) && (anEntity instanceof CuratedFeature)) {
        aRootFeatureMutator = mutatorAcceptor.getCuratedFeatureMutatorFor((CuratedFeature)anEntity);
        // Mark the whole feature structure as "under construction" (or destruction in this case).
        aRootFeatureMutator.setFeatureStructureUnderConstruction(true);
        // need to use the collection  of remvoed features in order to mark them as
        // no londer under construction.
        Collection removedFeatures = aRootFeatureMutator.removeSubStructureAndRemoveAlignments(true, workspace);
        for (Iterator removedItr = removedFeatures.iterator(); removedItr.hasNext(); ) {
          aFeatureMutator = mutatorAcceptor.getCuratedFeatureMutatorFor((CuratedFeature)removedItr.next());
          aFeatureMutator.setUnderConstruction(false);
        }
      }
    }
    if (DEBUG_CLASS) System.out.println("<< EXIT: UndoRedoGeneSet.removeFeatureStructureForRootOids(List rootOids);");
  }


  /**
   * Get the serialized entity structure as a byte array.
   */
  private GenomicEntity getEntityStructureFromByteArray(byte[] aSerialzedEntityStructure) {
    if (DEBUG_CLASS) {
      System.out.println("EXIT << UndoRedoGeneSet.getEntityStructureFromByteArray(byte[]);");
    }
    GenomicEntity rootEntity = null;
    try {
      // Get the ByteArrayInputStream...
      ByteArrayInputStream baInputStream = new ByteArrayInputStream(aSerialzedEntityStructure);
      // Get the ObjectInputStream...
      ObjectInputStream objInputStream = new ObjectInputStream(baInputStream);
      // Read back the root Entity
      rootEntity = (GenomicEntity)objInputStream.readObject();
      // Close the object input stream...
      objInputStream.close();
    }
    catch (Exception ex) {
      ModelMgr.getModelMgr().handleException(ex);
    }
    if (DEBUG_CLASS) {
      if (rootEntity != null) System.out.println("RETURNING root entity OID = "
                                                   + rootEntity.getOid());
      else System.out.println("RETURNING root entity = NULL");
      System.out.println("EXIT << UndoRedoGeneSet.getEntityStructureFromByteArray(byte[]);");
    }
    return rootEntity;
  }



  /**
   * Walk a feature structure (from it's root) and save off the alignments...
   */
  private void walkFeatureStructureAndApplyAlignments(Feature aFeature) {
    if (aFeature == null) return;
    if (DEBUG_CLASS) System.out.println("ENTER >> UndoRedoGeneSet.walkFeatureStructureAndApplyAlignments(Feature="
                                        + aFeature.getOid() + ");");

    // First get the alignments for this feature...
    Alignment[] alignments=(Alignment[])sourceAlignableGEToAlignmentsMap.get(aFeature.getOid());
    if ((alignments != null) && (alignments.length > 0)) {
      // Need to get the mutator...
      Feature.FeatureMutator featureMutator = mutatorAcceptor.getFeatureMutatorFor(aFeature);
      for (int i = 0; i< alignments.length; i++) {
        // Need to clone for new entity, because this is not the same entity
        // (reference) as when it was stored.
        Alignment newAlignment = alignments[i].cloneWithNewEntity(aFeature);
        try {
          if (DEBUG_CLASS) {
            System.out.print("Applying alingment to feature: " + aFeature.getEntityType().getEntityName()
                                + "." + aFeature.getOid());
            if (aFeature.getSuperFeature() != null) {
              System.out.println("Feature has SuperFeature: " + aFeature.getSuperFeature().getOid());
            }
            else {
              System.out.println("Feature has NO SuperFeature!");
            }
          }
          featureMutator.addAlignmentToAxis(newAlignment);
        }
        catch (Exception ex) {
          ModelMgr.getModelMgr().handleException(ex);
        }
      }
    }

    // Walk the sub-structure...
    Collection subStructureColl = aFeature.getSubStructure();
    for (Iterator itr = subStructureColl.iterator(); itr.hasNext(); ) {
      this.walkFeatureStructureAndApplyAlignments((Feature)itr.next());
    }
    if (DEBUG_CLASS) System.out.println("EXIT << UndoRedoGeneSet.walkFeatureStructureAndApplyAlignments(Feature="
                                        + aFeature.getOid() + ");");
  }


  /**
   * Invoked AFTER the command is executed.
   * @returns the set of root features that display changes "after" the command has executed.
   */
  public HashSet getCommandResultsRootFeatures()  {  return myResultsRootFeatures;  }


  /**
   * toString() will return the name of the command.
   */
  public String toString() {
    return undoCommandName;

  }

  public void setUndoRedoCommandLogMessage(String undoRedoCommandLogMessage) {
    this.undoRedoCommandLogMessage=undoRedoCommandLogMessage;
  }

  public String getCommandLogMessage(){

    return (undoRedoCommandLogMessage);
  }

  void setRedoFocusOID(OID oid) {
    redoFocusOID = oid;
  }

  /**
   * Return the OID of the feature that should have the user focus after the
   * command is undo-ed.
   */
  protected OID getUndoFocusOID() {
      return isRedo ? redoFocusOID : undoFocusOID;
  }

  protected OID getRedoFocusOID() {
      return isRedo ? undoFocusOID : redoFocusOID;
  }
}