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
/**
 *
 * CVS_ID:  $Id$=
 */

package client.gui.other.xml.xml_writer;

import api.entity_model.access.visitor.GenomicEntityVisitor;
import api.entity_model.management.ModelMgr;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.*;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.fundtype.SingleAlignmentSingleAxis;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.annotations.GeneFacade;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.stub.data.GenomicEntityComment;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;
import api.stub.data.ReplacementRelationship;
import api.stub.geometry.Range;
import api.stub.sequence.DNA;
import api.stub.sequence.Sequence;
import client.gui.framework.session_mgr.SessionMgr;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;


public class XMLdumper {

    private XmlGenomicEntityVisitor xmlvisitor;
    private FileWriter fout;
    private PrintWriter pout;
    private ArrayList compFeatureEntityList;
    private ArrayList curatedGeneList;
    private Vector contigVector;
    private int ctCounter = 0;
    private int cgCounter = 0;
    private GenomicAxis masterAxis;
    private GenomeVersion model;
    private Workspace workspace;
    private boolean written_obsoletedFeatures;
	private boolean notifyUser;
	
    public XMLdumper(String newFpath, boolean appendToExistingGame, boolean notifyUser) throws IOException {
        this.notifyUser = notifyUser;
        this.fout = new FileWriter(newFpath,appendToExistingGame);
        this.pout = new PrintWriter(fout);
        this.compFeatureEntityList = new ArrayList();
        this.curatedGeneList = new ArrayList();
        this.contigVector = new Vector();
        this.written_obsoletedFeatures=false;
       for(Iterator i=ModelMgr.getModelMgr().getSelectedGenomeVersions().iterator();i.hasNext();){
         this.model=(GenomeVersion)i.next();
         if(model.hasWorkspace()){
          this.workspace= model.getWorkspace();
          break;
        }
       }
    }


    public int getNumWrittenGenes() {
      return cgCounter;
    }

    public int getNumWrittenUnparentedTranscripts(){
      return ctCounter;
    }

    public void xmlVisit(GenomicEntity entity) {
        this.masterAxis=(GenomicAxis)entity;
        OID masteroid=masterAxis.getOid();
        if(masteroid==null){
            SessionMgr.getSessionMgr().handleException(new Exception ("Oid of was null"));
        }
        xmlvisitor = new XmlGenomicEntityVisitor();
        masterAxis.acceptVisitorForAlignedEntities(xmlvisitor, false);

       // also write out the unaligned root features because they will not be visited
       // we need to write out them only once. The workspace has obsoleted features on
       // all the axes, so once we write them we are done. However the alignedEntities are
       // written particular to an axes in the loop at the XMLWriter level.
       if(written_obsoletedFeatures==false){
          xmlvisitor.writeObsoletedRootFeatures();
        }
      }


    /**
     * Ends writeback by flushing and closing main writer.
     * This must be placed where it can be called outside the
     * writeback loop.
     */
    public void endWrite() {
        pout.flush();
        pout.close();
    } // End method: endWrite


    private class XmlGenomicEntityVisitor extends GenomicEntityVisitor {
        /**
         * we first get its CompositefeatureEntity and develop a list. Later on we iterate on this list and get to the
         * subfeatures of each of them and pull out the span_type and their start and end on the contig
         */
        public void visitFeature(Feature featureEntity) {

            if (featureEntity instanceof CuratedExon && featureEntity.getOid().isScratchOID()) {
                // for a CuratedExonEntity centity is CuratedTranscriptentity
                Feature centity = featureEntity.getSuperFeature();
                // for a CuratedTranscriptEntity Genecentity is CuratedGene
                CuratedGene genecentity = (CuratedGene)(centity.getSuperFeature());

                if (genecentity != null) {
                    OID geneId = genecentity.getOid();

                    if ((isInListAlready(geneId, curatedGeneList) == false)) {
                        curatedGeneList.add((genecentity));
                    }
                }
                CuratedTranscript ct = (CuratedTranscript)(centity);
                OID id = ct.getOid();
                if ((isInListAlready(id, compFeatureEntityList) == false) && (genecentity == null)) {
                    compFeatureEntityList.add(centity);
                }
                // this for loop dumps xml for CuratedTranscripts that are not attachedlist of
                // transcripts for a gene
                for (int i = ctCounter; i < compFeatureEntityList.size(); i++) {
                    handleCuratedTranscript(((CuratedTranscript)(compFeatureEntityList.get(i))), pout);
                } //for

                // this for loop iterates over the Gene list and gets to their attached transcripts
                // one by one
                for (int i = cgCounter; i < curatedGeneList.size(); i++) {
                   writeGene(((CuratedGene)(curatedGeneList.get(i))));
                 } //for
                ctCounter = compFeatureEntityList.size();
                cgCounter = curatedGeneList.size();

            } // if
        }



       /**
        * method to write out obsoleted Genes
        */
        private void writeObsoletedRootFeatures(){
          Set obsoletedRootFeatures=workspace.getRootObsoletedFeatures();
          for(Iterator iter=obsoletedRootFeatures.iterator();iter.hasNext();){
            CuratedFeature cf=(CuratedFeature)iter.next();
            /*
            // not writing gene that are oboleted by detaching last
            // transcript, this also conforms to the DTD and avoids
            // dangling Genes in GBW where in axis reference through sub features
            // exons was not recorded
            */
            //writing back osoleted GENES with obsoleted subfeatures.
            boolean condition1=cf instanceof CuratedGene ;
            Set s= workspace.getObsoletedSubFeatureOfSuperFeature(cf.getOid());
            boolean condition2=cf.getSubFeatureCount()==0 && s!=null && !s.isEmpty();
            boolean condition3=cf.getSubFeatureCount()!=0 && s!=null && s.isEmpty();
            if(condition1 &&(condition2||condition3)){
             writeGene((CuratedGene)cf);
             written_obsoletedFeatures=true;
           }
            // writing gene that are oboleted by detaching last
            // transcript, this now conforms to the DTD with additions of new tags
           else if(condition1 &&(!condition2||!condition3)){
             writeObsoletedGeneWithNoObsoletedSubFeatures((CuratedGene)cf);
             written_obsoletedFeatures=true;
           }
          }
        }


        private void writeGene(CuratedGene g){
          Collection ctFeatureList=g.getSubFeatures();
          Set obsoletedFeatures=workspace.getObsoletedSubFeatureOfSuperFeature(g.getOid());
          ctFeatureList.addAll(obsoletedFeatures);

          String geneId=prepareOidForWriteOut(g.getOid());
          GenomicProperty  gp=(g.getProperty(GeneFacade.GENE_ACCESSION_PROP));
          String geneAccno=gp.getInitialValue();
          try {
             pout.println(" <annotation id="+"\""+geneId+"\""+"> ");
             pout.println("  <name>" + geneAccno + "</name>");
             printFeatureComments(g,pout," ");
             Iterator it = ctFeatureList.iterator();
             while (it.hasNext()) {
               CuratedTranscript Ct = (CuratedTranscript)(it.next());
               handleCuratedTranscript(Ct, pout);
             } //while
             printReplaceTagForCuratedFeature(g,pout," ");
             printPropertiesForFeature(g,pout," ");
             pout.println(" </annotation>");
           } catch(Exception e) {
             SessionMgr.getSessionMgr().handleException(e);
           }

      }



      private void writeObsoletedGeneWithNoObsoletedSubFeatures(CuratedGene g){
          String geneId=g.getOid().toString();
          if(g.isScratchReplacingPromoted()){
            ReplacementRelationship r = g.getReplacementRelationship();
            OID[] replacedOids=null;
            if(r!=null){
              replacedOids=r.getReplacementOIDs();
            }
            if(replacedOids!=null || replacedOids.length!=0){
              for(int i=0;i<replacedOids.length;i++){
                GenomicEntity ge= model.getLoadedGenomicEntityForOid(replacedOids[i]);

                System.out.println(replacedOids[i].toString());
                boolean b=findIfGeneHasMoreThanOneReplaces((CuratedGene)ge);

                if(((CuratedFeature)ge).isObsoletedByWorkspace() && !b){
                  GeometricAlignment genega=(GeometricAlignment)workspace.getObsoletedAlignmentForWorkspaceOid(g.getOid());
                  try {
                    String axisId=genega.getAxis().getOid().toString();
                    pout.println(" <annotation_obsoletion id="+"\""+geneId+"\""+ " obsoleted_id=" +"\""+replacedOids[i]+"\""+" query_seq_relationship_id="+"\""+axisId+"\""+"> ");
                    pout.println(" </annotation_obsoletion>");
                  }catch(Exception e) {
                    SessionMgr.getSessionMgr().handleException(e);
                  }
                }
              }//for
            }
          }
        }


        private boolean findIfGeneHasMoreThanOneReplaces(CuratedGene promotedGene){
          boolean bool=false;
          for (int i = 0; i < curatedGeneList.size(); i++) {
            CuratedGene workSpaceGene=((CuratedGene)(curatedGeneList.get(i)));
            ReplacementRelationship r = workSpaceGene.getDerivedReplacementRelationship();
            OID[] replacedOids=null;
            if(r!=null){
              replacedOids=r.getReplacementOIDs();
            }
            if(replacedOids!=null || replacedOids.length!=0){
              for(int j=0;j<replacedOids.length;j++){
                 if(promotedGene.getOid().equals((OID)replacedOids[j])){
                   bool=true;
                   return bool;
                 }

              }
            }
          }
          return bool;
       }

        /** This method is written to remove the duplicate elements
         * in the compFeatureEntityVector
         */
        private boolean isInListAlready(OID Id, List list) {
            for (int i = 0; i < list.size(); i++) {
                OID temp = ((Feature)(list.get(i))).getOid();
                if (temp.equals(Id)) {
                    return true;
                }
            }
            return false;
        }


        private void printTranscriptFeatures(Collection featureList, PrintWriter Pout, Collection startCodons, Collection stopCodons) {
            Iterator it = featureList.iterator();
            while (it.hasNext()) {

                Feature fp = ((Feature)(it.next()));
                String featureType=fp.getEntityType().getEntityName();
                String parentId=prepareOidForWriteOut(getAxisForFeature(fp));

                Collection fpEvidence = fp.getEvidenceOids();
                int start = getCuratedSimpleFeatStart(fp);
                int end = getCuratedSimpleFeatEnd(fp);
                try {
                    if(fp.getOid().isScratchOID()){
                        printFeatureSpan(fp, fpEvidence.iterator(), prepareFeatureTypeForWriteOut(featureType), parentId, start, end, Pout);
                        Pout.println("   </feature_span>");
                    }
                } catch(Exception e) {
                    SessionMgr.getSessionMgr().handleException(e);
                }
            }
            // Print the starts and the ends of the codon entity.
            Iterator startIterator= startCodons.iterator();
            Iterator stopIterator= stopCodons.iterator();
            while(startIterator.hasNext()){
                printCodonGenomicEntity((CuratedCodon)(startIterator.next()), Pout);
            }
            while(stopIterator.hasNext()){
                printCodonGenomicEntity((CuratedCodon)(stopIterator.next()), Pout);
            }
            // Terminate the feature set to which these spans belong.
            Pout.println("  </feature_set>");
        } // End method


        private void printCodonGenomicEntity(CuratedCodon codon, PrintWriter Pout) {
            if (codon != null) {
                OID codonId = codon.getOid();
                String featureType=codon.getEntityType().getEntityName();

                String codonParent=prepareOidForWriteOut(getAxisForFeature(codon));
                Feature codonCompParent=(Feature)codon.getSuperFeature();

                int codonStart = getCuratedSimpleFeatStart(codon);
                int codonEnd=getCuratedSimpleFeatEnd(codon);

                // This is a special case. Right now Codons are coming back form the database with
                // axisBegin=axisEnd, and when actually in the XML files they should have a magnitude
                // of 3. This fix would make the reload of files ok however, it is trying to manipulate
                // actual data to be saved out. Issue needs to be REVISITED!
                if(codonStart==codonEnd){
                    boolean parentReversed=checkIfParentReversed(codonCompParent);
                    if(parentReversed){
                        codonEnd=codonEnd-3;
                    }else{
                        codonEnd=codonEnd+3;
                    }
                }
                try {
                    // Codons dont have getEvidence on them thats why the second parameter is null
                    if(codonId.isScratchOID()){

                        printFeatureSpan(codon, null, prepareFeatureTypeForWriteOut(featureType), codonParent, codonStart, codonEnd, Pout);
                        Pout.println("   </feature_span>");
                    }
                } catch(Exception e) {
                    SessionMgr.getSessionMgr().handleException(e);
                } // End catch block for general exception.
            } // Non-null codon.
            else {
                try {
                } catch(Exception e) {
                    SessionMgr.getSessionMgr().handleException(e);
                } // End catch block for general exceptions.
            } // Null codon
        } // End method: printCodonGenomicEntity


        private void handleCuratedTranscript(CuratedTranscript trscpt, PrintWriter pwtr) {
            String parentFeatureType = trscpt.getEntityType().getEntityName();
            GenomicProperty accgp =trscpt.getProperty(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP);
            String trscptAccno="";
            if(accgp!=null){
               trscptAccno=accgp.getInitialValue();
            }

            //
            // Start codons can be more than one on a Transcript but we are assuming that there will be only one
            //   NOTE: in the branch code, this code used a method call (see below) that gathered the collection
            //   directly from the transcript.
            //Collection startCodonList =(Collection)(trscpt.getAllStartCodons());
            //Collection stopCodonList = (Collection)(trscpt.getAllStopCodons());

            CuratedCodon startCodon = trscpt.getStartCodon();
            Collection startCodonList = new java.util.ArrayList();
            startCodonList.add(startCodon);
            startCodon = null;

            CuratedCodon stopCodon = trscpt.getStopCodon();
            Collection stopCodonList = new java.util.ArrayList();
            stopCodonList.add(stopCodon);
            stopCodon = null;

            try {
                if(trscpt.getOid().isScratchOID()){

                    pwtr.println("  <feature_set " + "id= " + "\"" + prepareOidForWriteOut(trscpt.getOid())+ "\"" + ">");
                    pwtr.println("   <name>" + trscptAccno + "</name>");
                    pwtr.println("   <type>" + prepareFeatureTypeForWriteOut(parentFeatureType) + "</type>");
                    printFeatureComments(trscpt,pwtr,"  ");
                    printReplaceTagForCuratedFeature(trscpt,pwtr,"  ");
                    printPropertiesForFeature(trscpt,pwtr,"  ");
                    Collection trscptFeatureList = trscpt.getSubFeatures();
                     Set obsoletedFeatures=workspace.getObsoletedSubFeatureOfSuperFeature(trscpt.getOid());
                    trscptFeatureList.addAll(obsoletedFeatures);
                    // PrintTranscriptFeatures(trscptFeatureList, pwtr, startCodon, stopCodon);
                    printTranscriptFeatures(trscptFeatureList, pwtr, startCodonList, stopCodonList);
                }
            } catch(Exception e) {
                SessionMgr.getSessionMgr().handleException(e);
            }
        }




        private void printReplaceTagForCuratedFeature(CuratedFeature fentity, PrintWriter pw, String noOfWhiteSpaces){

            ReplacementRelationship r;
            // If we are writing out a Gene... we need to ask for the "derived" replacement relationship...
            if (fentity instanceof CuratedGene) r = ((CuratedGene)fentity).getDerivedReplacementRelationship();
            else r=fentity.getReplacementRelationship();

            OID[] replacedOids=null;
            if(r!=null){
              replacedOids=r.getReplacementOIDs();
            }

            String replacedOIDStr=" ";
            if(replacedOids!=null){
              String whiteSpaces=noOfWhiteSpaces.concat(" ");
              for(int i=0; i<replacedOids.length;i++){
                OID featOID=replacedOids[i];
                if(featOID!=null){
                  replacedOIDStr=replacedOIDStr+featOID.toString()+" ";
                }
              }//for
              int lastWhiteSpace=replacedOIDStr.lastIndexOf(" ");
               if(lastWhiteSpace!=0){
                pw.println(whiteSpaces+"<replaced " + "ids=" + "\"" + replacedOIDStr.substring(1,lastWhiteSpace) + "\"" + " type=" + "\"" +r.getReplacementType()+ "\""+">");
                pw.println(whiteSpaces+"</replaced>");
              }
              // case if the entity is NEW, it will replaced ids to be empty and replaces type=NEW
              else if(r.getReplacementType().equals(ReplacementRelationship.TYPE_NEW)){
                pw.println(whiteSpaces+"<replaced " + "ids=" + "\"" + ""+ "\"" + " type=" + "\"" +r.getReplacementType()+ "\""+">");
                pw.println(whiteSpaces+"</replaced>");
              }
           }//if
        }


        private void printFeatureSpan(Feature fentity, Iterator itr, String spanType, String parenId, int fpstart, int fpend, PrintWriter pwOut) {
            pwOut.println("   <feature_span id=" + "\"" + prepareOidForWriteOut(fentity.getOid()) + "\"" + ">");
            pwOut.println("    <type>" + spanType + "</type>");
            printFeatureComments(fentity,pwOut,"   ");
            pwOut.println("    <seq_relationship " + "id=" + "\"" + parenId + "\"" + " type=" + "\"query\"" + ">");
            pwOut.println("      <span>");
            pwOut.println("        <start>" + fpstart + "</start>");
            pwOut.println("        <end>" + fpend + "</end>");
            pwOut.println("      </span>");
            //print the alignment for the exon, skip codons
            if(fentity instanceof CuratedExon && ((CuratedExon)fentity).getOnlyGeometricAlignmentToOnlyAxis()!=null ){
              Sequence s=masterAxis.getNucleotideSeq(((CuratedExon)fentity).getOnlyGeometricAlignmentToOnlyAxis().getRangeOnAxis());
              String residues =DNA.toString(s);
              pwOut.println("       <alignment>");
              pwOut.println("        " + residues);
              pwOut.println("       </alignment>");

			  // Simply a notification.  Do not break anything.
              if(residues.toLowerCase().indexOf("n")>=0 && notifyUser){
				JOptionPane.showMessageDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
				"Exon "+ fentity.getOid()+" of Transcript "+parenId +" has N's in the sequence ",
			   "Exon Nucleotide Sequence", JOptionPane.PLAIN_MESSAGE);
              }
            }

            pwOut.println("    </seq_relationship>");
            printPropertiesForFeature(fentity,pwOut,"   ");
            printReplaceTagForCuratedFeature((CuratedFeature)fentity,pwOut,"   ");
            // Beginning to print the evidence inforamtion if the feature span has one
            if (itr != null) {
                while (itr.hasNext()) {
                    OID oid = ((OID)(itr.next()));
                    pwOut.println("    <evidence result=" + "\"" + prepareOidForWriteOut(oid) + "\"" + ">");
                    pwOut.println("    </evidence>");
                }
            }
        }


        private void printPropertiesForFeature(Feature f, PrintWriter pw, String noOfWhiteSpaces){
            Set properties=f.getProperties();
            if(properties!=null && properties.size()!=0){
                printPropertiesRecursive(f,properties,pw,noOfWhiteSpaces);
                /*  for(int i=0;i<properties.length;i++){

                   String name=properties[i].getName();
                   String value=properties[i].getInitialValue();
                   boolean editable=properties[i].getEditable();
                   if(!(name.equals(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP))&& !(name.equals(TranscriptFacade.GENE_ACCESSION_PROP))&& !(name.equals(GeneFacade.GENE_ACCESSION_PROP))){
                    pw.println(noOfWhiteSpaces.concat(" ")+"<property "+"name="+ "\""+ name+"\""+" value="+"\""+value+"\""+" editable="+"\""+editable+"\""+">");
                    pw.println(noOfWhiteSpaces.concat(" ")+"</property>");

                  // }//if, these properties are written already within the name tag

                 }//for
                 */
            }//if
        }


        private void printPropertiesRecursive(Feature featPI,Set props,PrintWriter pw,String noOfWhiteSpaces){
            for(Iterator it = props.iterator();it.hasNext();){
                GenomicProperty tmpProperty = (GenomicProperty) it.next();
                String name=tmpProperty.getName();
                if(!(name.equals(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP))&& !(name.equals(TranscriptFacade.GENE_ACCESSION_PROP))&& !(name.equals(GeneFacade.GENE_ACCESSION_PROP)) && !(tmpProperty.isComputed())){
                    if(!tmpProperty.getExpandable()){
                        printPropertyStartTag(tmpProperty,pw,noOfWhiteSpaces);
                        printPropertyEndTag(pw,noOfWhiteSpaces);
                    }else{
                        printPropertyStartTag(tmpProperty,pw,noOfWhiteSpaces);
                        Set expandedProps=new HashSet();
                        GenomicProperty[] subProps=tmpProperty.getSubProperties();
                        //featPI.getExpandedProperties(tmpProperty);
                        if(subProps!=null){
                          for(int j=0;j<subProps.length;j++){
                            expandedProps.add(subProps[j]);
                          }
                        }

                        if(expandedProps!=null){
                            printPropertiesRecursive(featPI,expandedProps,pw,noOfWhiteSpaces.concat(" "));
                        }
                        printPropertyEndTag(pw,noOfWhiteSpaces);
                    }
                }
            }//for

        }



        private void printPropertyStartTag(GenomicProperty p, PrintWriter pw, String noOfWhiteSpaces){
            String name=p.getName();
            String value=p.getInitialValue();
            boolean editable=p.getEditable();
            // Look for &.  Perhaps in the future add <,> and other characters.
            if(value!=null){
	      value=checkForEscapeCharacter(value);
            }
	    pw.println(noOfWhiteSpaces.concat(" ")+"<property "+"name="+ "\""+ name+"\""+" value="+"\""+value+"\""+" editable="+"\""+editable+"\""+">");
        }


     private String checkForEscapeCharacter(String value) {
        StringBuffer testString = new StringBuffer(value);
        boolean escapeCharacterCorrect = false;
        Vector insertionPlaces = new Vector();

        // This could probably be refactored for 3.1.
        // Test for amp.
        for (int x=0;x<testString.length();x++) {
          if (testString.charAt(x)=='&') {
            //System.out.println("Character is "+testString.charAt(x));
            StringBuffer substring = new StringBuffer(value.substring(x,value.length()));
            for (int y=0;y<substring.length();y++) {
              if (substring.charAt(y)==';') escapeCharacterCorrect=true;
              // Break out if other character is seen before ;
              if (substring.charAt(y)=='<' || substring.charAt(y)=='>' ||
                  substring.charAt(y)=='\'' || substring.charAt(y)=='\"') break;
            }
            if (!escapeCharacterCorrect) insertionPlaces.addElement(new Integer(x+1));
            else escapeCharacterCorrect=false;
          } // if
        } //for
        for (int z=0;z<insertionPlaces.size();z++) {
          testString.insert(((Integer)insertionPlaces.get(z)).intValue()+(z*4),"amp;");
        }

        // Test for Less than <
        insertionPlaces=new Vector();
        for (int x=0;x<testString.length();x++) {
          if (testString.charAt(x)=='<') {
            //System.out.println("Character is "+testString.charAt(x));
            StringBuffer substring = new StringBuffer(testString.substring(x,testString.length()));
            for (int y=0;y<substring.length();y++) {
              if (substring.charAt(y)==';') escapeCharacterCorrect=true;
              // Break out if other character is seen before ;
              if (substring.charAt(y)=='&' || substring.charAt(y)=='>' ||
                  substring.charAt(y)=='\'' || substring.charAt(y)=='\"') break;
            }
            if (!escapeCharacterCorrect) insertionPlaces.addElement(new Integer(x));
            else escapeCharacterCorrect=false;
          } // if
        } //for
        for (int z=0;z<insertionPlaces.size();z++) {
          int start=((Integer)insertionPlaces.get(z)).intValue()+(z*3);
          int end=start+1;
          testString.replace(start,end,"&lt;");
        }

        // Test for Greater than >
        insertionPlaces=new Vector();
        for (int x=0;x<testString.length();x++) {
          if (testString.charAt(x)=='>') {
            //System.out.println("Character is "+testString.charAt(x));
            StringBuffer substring = new StringBuffer(testString.substring(x,testString.length()));
            for (int y=0;y<substring.length();y++) {
              if (substring.charAt(y)==';') escapeCharacterCorrect=true;
              // Break out if other character is seen before ;
              if (substring.charAt(y)=='&' || substring.charAt(y)=='<' ||
                  substring.charAt(y)=='\'' || substring.charAt(y)=='\"') break;
            }
            if (!escapeCharacterCorrect) insertionPlaces.addElement(new Integer(x));
            else escapeCharacterCorrect=false;
          } // if
        } //for
        for (int z=0;z<insertionPlaces.size();z++) {
          int start=((Integer)insertionPlaces.get(z)).intValue()+(z*3);
          int end=start+1;
          testString.replace(start,end,"&gt;");
        }

        // Test for apostrophe '
        insertionPlaces=new Vector();
        for (int x=0;x<testString.length();x++) {
          if (testString.charAt(x)=='\'') {
            //System.out.println("Character is "+testString.charAt(x));
            StringBuffer substring = new StringBuffer(testString.substring(x,testString.length()));
            for (int y=0;y<substring.length();y++) {
              if (substring.charAt(y)==';') escapeCharacterCorrect=true;
              // Break out if other character is seen before ;
              if (substring.charAt(y)=='&' || substring.charAt(y)=='<' ||
                  substring.charAt(y)=='>' || substring.charAt(y)=='\"') break;
            }
            if (!escapeCharacterCorrect) insertionPlaces.addElement(new Integer(x));
            else escapeCharacterCorrect=false;
          } // if
        } //for
        for (int z=0;z<insertionPlaces.size();z++) {
          int start=((Integer)insertionPlaces.get(z)).intValue()+(z*5);
          int end=start+1;
          testString.replace(start,end,"&apos;");
        }

        // Test for quote "
        insertionPlaces=new Vector();
        for (int x=0;x<testString.length();x++) {
          if (testString.charAt(x)=='\"') {
            //System.out.println("Character is "+testString.charAt(x));
            StringBuffer substring = new StringBuffer(testString.substring(x,testString.length()));
            for (int y=0;y<substring.length();y++) {
              if (substring.charAt(y)==';') escapeCharacterCorrect=true;
              // Break out if other character is seen before ;
              if (substring.charAt(y)=='&' || substring.charAt(y)=='<' ||
                  substring.charAt(y)=='>' || substring.charAt(y)=='\'') break;
            }
            if (!escapeCharacterCorrect) insertionPlaces.addElement(new Integer(x));
            else escapeCharacterCorrect=false;
          } // if
        } //for
        for (int z=0;z<insertionPlaces.size();z++) {
          int start=((Integer)insertionPlaces.get(z)).intValue()+(z*5);
          int end=start+1;
          testString.replace(start,end,"&quot;");
        }

       String tmpString=new String(testString);
       tmpString=escapeOver127(tmpString.toCharArray());
       return tmpString;
     }


    private String escapeOver127(char[] characters) {
        StringBuffer accumulator = new StringBuffer();
        for (int i = 0; i < characters.length; i++) {
          if (((int)characters[i]) > 127) {
            accumulator.append("&#"+((int)characters[i])+";");
          }
          else {
            accumulator.append(characters[i]);
          }
        }
        return accumulator.toString();
    } // End method


        private void printPropertyEndTag(PrintWriter pw, String noOfWhiteSpaces){
            pw.println(noOfWhiteSpaces.concat(" ")+"</property>");
        }


        /**
         * This method was written because we need to determine the start and end of the
         * CuratedTranscripts and Genes. This cannot be done directly. So the idea is to
         * get the (start,end) pairs of the children of these composite features.
         */

        private void sort(Vector vector) {
            int j = 0;
            int k;
            int temp;
            Integer first;
            Integer second;
            for (j = 0; j < vector.size(); j++) {
                for (k = j + 1; k < vector.size(); k++) {
                    first = (Integer)(vector.elementAt(j));
                    second = (Integer)(vector.elementAt(k));
                    if ((first.intValue()) > (second.intValue())) {
                        // swap begins
                        temp = first.intValue();
                        vector.setElementAt(second, j);
                        vector.setElementAt((new Integer(temp)), k);
                    }
                }
            }
        }


        private int getCuratedSimpleFeatStart(Feature fentity) {
            if (!(fentity instanceof SingleAlignmentSingleAxis)) {
                // todo: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s).
                System.out.println("XMLdumper: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s)");
                return 0;
            }
            GeometricAlignment exonAr=((SingleAlignmentSingleAxis)fentity).getOnlyGeometricAlignmentToOnlyAxis();

            Range r;
            if(exonAr!=null){
               r=exonAr.getRangeOnAxis();
            }
            // case when the feature is unaligned and obsoleted, therefore the range is obtained
            // from the workspace
            else{
              GeometricAlignment exonga=(GeometricAlignment)workspace.getObsoletedAlignmentForWorkspaceOid(fentity.getOid());
              r=exonga.getRangeOnAxis();
            }


            return (r.getStart());
        }




        private OID getAxisForFeature(Feature fentity) {
            if (!(fentity instanceof SingleAlignmentSingleAxis)) {
                // todo: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s).
                System.out.println("XMLdumper: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s)");
                return null;
            }
            GeometricAlignment exonAr=((SingleAlignmentSingleAxis)fentity).getOnlyGeometricAlignmentToOnlyAxis();

            OID axisOid;
            if(exonAr!=null){
               axisOid=exonAr.getAxis().getOid();
               return axisOid;
            }
            // case when the feature is unaligned and obsoleted, therefore the axisoid is obtained
            // from the workspace
            GeometricAlignment exonga=(GeometricAlignment)workspace.getObsoletedAlignmentForWorkspaceOid(fentity.getOid());
            if (exonga != null) {
              axisOid=exonga.getAxis().getOid();
              return axisOid;
            }

            System.out.println("XMLdumper: Could not getAxisForFeature(Feature=" +
                                fentity.getEntityType().getEntityName() + ":"
                                + fentity.getOid() + ");");
            return null;
        }


        private int getCuratedSimpleFeatEnd(Feature fentity) {
            if (!(fentity instanceof SingleAlignmentSingleAxis)) {
                // todo: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s).
                System.out.println("XMLdumper: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s)");
                return 0;
            }
            GeometricAlignment exonAr=((SingleAlignmentSingleAxis)fentity).getOnlyGeometricAlignmentToOnlyAxis();
            Range r;
            if(exonAr!=null){
               r=exonAr.getRangeOnAxis();
            }
            // case when the feature is unaligned and obsoleted, therefore the range is obtained
            // from the workspace
            else{
              GeometricAlignment exonga=(GeometricAlignment)workspace.getObsoletedAlignmentForWorkspaceOid(fentity.getOid());
              r=exonga.getRangeOnAxis();
            }
            return (r.getEnd());
        }


        private int getTranscriptStart(Feature fentity) {
            Collection featureList = fentity.getSubFeatures();
            Vector ctStart = new Vector();
            boolean checkIsInvertedOnparent = false;
            Iterator it = featureList.iterator();
            while (it.hasNext()) {
                Feature ftp = (Feature)it.next();
                int ftpstart = getCuratedSimpleFeatStart(ftp);
                ctStart.addElement(new Integer(ftpstart));
                checkIsInvertedOnparent = false;
                /**
                 * @todo What did this method turn into?.  Above should equal below line.
                 */
                //ftp.isInvertedOnParent(false);
            }
            sort(ctStart);
            if (checkIsInvertedOnparent) {
                return (((Integer)(ctStart.lastElement())).intValue());
            } else {
                return (((Integer)(ctStart.firstElement())).intValue());
            }
        }


        private int getTranscriptEnd(Feature fentity) {
            Collection featureList = fentity.getSubFeatures();
            Vector ctEnd = new Vector();
            boolean checkIsInvertedOnparent = false;
            Iterator it = featureList.iterator();
            while (it.hasNext()) {
                Feature ftp = (Feature)it.next();
                int ftpend = getCuratedSimpleFeatEnd(ftp);
                ctEnd.addElement(new Integer(ftpend));
                checkIsInvertedOnparent = false;
                /**
                 * @todo Need method.  Above should be equal to below line.
                 */
                //ftp.isInvertedOnParent(false);
            }
            sort(ctEnd);
            if (checkIsInvertedOnparent) {
                return (((Integer)(ctEnd.firstElement())).intValue());
            } else {
                return (((Integer)(ctEnd.lastElement())).intValue());
            }
        }


        private int getGeneStart(Feature fentity) {
            Collection geneFeatureList = fentity.getSubFeatures();
            Vector cgStart = new Vector();
            int ctpStart = 0;
            int ctpEnd = 0;
            Iterator it = geneFeatureList.iterator();
            while (it.hasNext()) {
                CuratedTranscript ctp = (CuratedTranscript)it.next();
                ctpStart = getTranscriptStart(ctp);
                ctpEnd = getTranscriptEnd(ctp);
                cgStart.addElement(new Integer(ctpStart));
            }
            sort(cgStart);
            if (ctpEnd < ctpStart) {
                return (((Integer)(cgStart.lastElement())).intValue());
            } else {
                return (((Integer)(cgStart.firstElement())).intValue());
            }
        }


        private int getGeneEnd(Feature fentity) {
            Collection geneFeatureList = fentity.getSubFeatures();
            Vector cgEnd = new Vector();
            int ctpStart = 0;
            int ctpEnd = 0;
            Iterator it = geneFeatureList.iterator();
            while (it.hasNext()) {
                CuratedTranscript ctp = (CuratedTranscript)it.next();
                ctpStart = getTranscriptStart(ctp);
                ctpEnd = getTranscriptEnd(ctp);
                cgEnd.addElement(new Integer(ctpEnd));
            }
            sort(cgEnd);
            if (ctpEnd < ctpStart) {
                return (((Integer)(cgEnd.firstElement())).intValue());
            } else {
                return (((Integer)(cgEnd.lastElement())).intValue());
            }
        }


        /**
        * This method will remove any $ signs appearing in the name prefixes of the oids.
        * Later it would also use the internalToexternal name space translations.
        */
        private String prepareOidForWriteOut(OID o){
            String oidStr=o.toString();
            String preparedOidStr;
            if(oidStr.startsWith("$")){
                preparedOidStr=oidStr.substring(1);
           }else{
                preparedOidStr=oidStr;
            }
            return (preparedOidStr);
        }


        /**
         * This method will remove any _ signs appearing in the name prefixes of the FeatureTypes.
         *
         */
        private String prepareFeatureTypeForWriteOut(String prefixedFeatType){

            String preparedFeatType;
            if(prefixedFeatType.startsWith("_")){
                preparedFeatType=prefixedFeatType.substring(1);

            }else{
                preparedFeatType=prefixedFeatType;// input featureType is OK
            }
            return (preparedFeatType);
        }


        /**
         * method for writing out comments for genes from CommentEntry objects.
         * only the comment String is extracted from the CommentEntry objects.
         */
        private void printFeatureComments(Feature f, PrintWriter pw,String noOfWhiteSpaces){
            Collection newComments=f.getComments();

            for (Iterator it = newComments.iterator();it.hasNext();)
            {
                GenomicEntityComment coe=(GenomicEntityComment)it.next();
                String commentAuthor=coe.getCreatedBy();
                String commentDate=coe.getCreationDateAsString();
                String comment=coe.getComment();

                // Check and fix XML escape character.
                comment=checkForEscapeCharacter(comment);
                pw.println(noOfWhiteSpaces.concat(" ")+"<comments"+" author="+"\""+commentAuthor+"\""+" date="+"\""+commentDate+"\""+">");
                pw.println(noOfWhiteSpaces.concat("  ")+comment);
                pw.println(noOfWhiteSpaces.concat(" ")+"</comments>");
            }//for
        }// End method: printFeatureComments


        private boolean checkIfParentReversed(Feature compFentity){
            if (!(compFentity instanceof SingleAlignmentSingleAxis)) {
                // todo: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s).
                System.out.println("DoCreateNewCurationAndAlignCommand: Need to do something smart with non-SingleAlignmentSingleAxis such as STSMarker(s)");
                return false;
            }
            GeometricAlignment ar=((SingleAlignmentSingleAxis)compFentity).getOnlyGeometricAlignmentToOnlyAxis();
            Range  rangeOnAxis= ar.getRangeOnAxis();
            /**
             * @todo Need method getRangeOnEntity. Above should equal below line.
             */
            //ar.getRangeOnEntity();
            // We only check the rangeOnAxis for Orientation...
            if(rangeOnAxis.isReversed()){
                return true;
            }else{
                return false;
            }

        }
    }
}

