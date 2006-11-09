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
 * Title:        Genome Browser Client Description:  This project is for JBuilder 4.0
 * @author       Deepali Bhandari
 * @version $Id$
 */
import api.entity_model.management.CommandPreconditionException;
import api.entity_model.management.GenomicEntityFactory;
import api.entity_model.management.ModelMgr;
import api.entity_model.management.PropertyMgr;
import api.entity_model.model.annotation.CodonFeature;
import api.entity_model.model.annotation.ComputedCodon;
import api.entity_model.model.annotation.CuratedCodon;
import api.entity_model.model.annotation.CuratedExon;
import api.entity_model.model.annotation.CuratedTranscript;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.EntityType;
import api.entity_model.model.fundtype.MutableAlignment;
import api.entity_model.model.fundtype.SingleAlignmentSingleAxis;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.facade.abstract_facade.fundtype.EntityTypeConstants;
import api.stub.data.FeatureDisplayPriority;
import api.stub.data.OID;
import api.stub.data.OIDGenerator;
import api.stub.geometry.Range;
import api.stub.sequence.DNA;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;


public class DoAddStartSite extends FeatureStructureBoundedCommand {
    private static boolean DEBUG_CLASS = false;
    public static final String COMMAND_NAME = "Add Start Codon";
    private ComputedCodon site;
    private CuratedTranscript transcript;
    private Range rangeOnAxis;
    // private CuratedCodon.CuratedCodonMutator startMutator = null;
    // private CuratedTranscript.CuratedTranscriptMutator transcriptMutator = null;
    private EntityType codonType=null;
    private boolean usingComputedSite=false;

    /**
     *  Use this constructor if the site is already aligned to the axis
     */
    public DoAddStartSite(Axis anAxis, ComputedCodon site, CuratedTranscript transcript) {
        super(anAxis);
        this.site = site;
        this.transcript = transcript;
        this.usingComputedSite=true;
    }


    /**
     * Use this constructor if the site is NOT aligned to the axis
     */
    public DoAddStartSite(Axis anAxis, Range rangeOnAxis, CuratedTranscript transcript, EntityType codonType) {
        super(anAxis);
        this.transcript = transcript;
        this.rangeOnAxis=rangeOnAxis;
        this.codonType=codonType;
        this.usingComputedSite=false;
    }


    /**
     * Invoked BEFORE the command is executed.
     * @returns the set of pre-existing root features that will be affected.
     */
    public HashSet getCommandSourceRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      rootFeatureSet.add(transcript.getRootFeature());
      return rootFeatureSet;
    }


    /**
     * Invoked AFTER the command is executed.
     * @returns the set of root features that display changes "after" the command has executed.
     */
    public HashSet getCommandResultsRootFeatures() {
      HashSet rootFeatureSet = new HashSet();
      rootFeatureSet.add(transcript.getRootFeature());
      return rootFeatureSet;
    }


     public void validatePreconditions() throws CommandPreconditionException {
      // check if the codon lies in any of the exons on the transcript
      // this check is suitable only when a computed codon is used to set the
      // start codon the transcript
      boolean validtoexecute=false;
      if(site !=null){
        int startOfCodon=site.getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis().getStart();
        Iterator iter=transcript.getSubFeatures().iterator();
        while(iter.hasNext()){
          Range exonRange=((CuratedExon)iter.next()).getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis();
          if(exonRange.contains(startOfCodon)){
            validtoexecute=true;
            break;
          }
        }
        if(!validtoexecute){
          throw new CommandPreconditionException(this, "codon should lie in atleast one of the exons of the transcript");
        }
      }
      int pos=0;
      String splicedres=null;
      String[] startCodonStrings=CodonFeature.forwardStartCodonStrings;
      if(this.usingComputedSite==false){
        if(codonType.equals(EntityType.getEntityTypeForValue(EntityTypeConstants.Translation_Start_Position))){return;}
        pos=transcript.transformAxisPositionToSplicedPosition(rangeOnAxis.getStart());
        splicedres=DNA.toString(transcript.getSplicedResidues());

        for(int i=0;i<startCodonStrings.length;i++){
          if(!(splicedres.substring(pos,pos+3).equals(startCodonStrings[i]))){
            throw new CommandPreconditionException(this, " Start codon must be " +startCodonStrings[i]);
          }
         }

       }

    }



    /**
     * Execute the command with out returning the undo.
     * The undo will be created for us by the GeneBoundaryCommand super-class.
     */
    public void executeWithNoUndo() throws Exception {
        CuratedCodon.CuratedCodonMutator startMutator = null;
        CuratedTranscript.CuratedTranscriptMutator transcriptMutator = null;

        transcriptMutator = mutatorAcceptor.getCuratedTranscriptMutatorFor(transcript);
        GenomicEntityFactory geFactory = (ModelMgr.getModelMgr()).getEntityFactory();
        EntityType typeForCodon=null;

        // First if there is an old codon, remove it...
        if (transcript.hasStartCodon()) {
          transcriptMutator.removeStartOrStopCodon(transcript.getStartCodon());
        }

        if (codonType!=null) {
          typeForCodon=codonType;
        }
        else {
          typeForCodon=EntityType.getEntityTypeForValue(EntityTypeConstants.Start_Codon_Start_Position);
        }
        CuratedCodon newsite = (CuratedCodon)geFactory.create(
            OIDGenerator.getOIDGenerator().generateScratchOIDForGenomeVersion(transcript.getGenomeVersion().hashCode()),
            "StartCodon", typeForCodon/*EntityType.getEntityTypeForValue(EntityTypeConstants.Start_Codon_Start_Position)*/,
            // "Curation");
            "Curation", null, transcript, FeatureDisplayPriority.DEFAULT_PRIORITY);

        startMutator = mutatorAcceptor.getCuratedCodonMutatorFor(newsite);
        MutableAlignment newsitema;
        if (site!=null) {
          newsitema = new MutableAlignment(site.getOnlyGeometricAlignmentToOnlyAxis().getAxis(),
                                           newsite,
                                           site.getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis());
        }
        else {
          newsitema = new MutableAlignment(transcript.getOnlyGeometricAlignmentToOnlyAxis().getAxis(),
                                           newsite, rangeOnAxis);
        }
        try {
          // this will also take care of adding Stops
          startMutator.addAlignmentToAxis(newsitema);
          PropertyMgr.getPropertyMgr().handleProperties(PropertyMgr.NEW_ENTITY, newsite, false);
        }
        catch (Exception e) {
          ModelMgr.getModelMgr().handleException(e);
        }
        startMutator.updatePropertiesBasedOnGeometricAlignment(((SingleAlignmentSingleAxis)newsite).getOnlyGeometricAlignmentToOnlyAxis());
        setFocusEntity(transcript);
        this.timeofCommandExecution=new Date().toString();
    }


    public String toString() {
        return COMMAND_NAME;
    }



    protected OID getUndoFocusOID() {
      return transcript.getOid();
    }

    protected OID getRedoFocusOID() {
      return transcript.getOid();
    }

  /** This returns the Log message with the time stamp expalaning which entities
    * underewent change, of what kind
    *
    */
   public String getCommandLogMessage() {

     String transcriptAcc=transcript.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP).getInitialValue();
     String codonId=transcript.getStartCodon().getOid().toString();
     this.actionStr="Added StartCodon id= "+codonId+" On Transcript "+transcriptAcc;
     return(super.getCommandLogMessage());
   }

}
