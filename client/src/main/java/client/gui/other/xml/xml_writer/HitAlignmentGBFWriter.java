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
package client.gui.other.xml.xml_writer;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

/**
 * This class is takes features from the data model and produces a GBF file for
 * those features.  This class should eventually be refactored to work better
 * with the XMLWriter that produces GBW files.  There should be generic writers
 * that know how to generate many different output formats FASTA, GBW, GBA, GBF.
 * Most of these methods come from the XMLDumper and XMLWriter classes.
 */


import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.SingleAlignmentSingleAxis;
import api.facade.abstract_facade.annotations.GeneFacade;
import api.facade.abstract_facade.annotations.TranscriptFacade;
import api.stub.data.GenomicEntityComment;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;
import api.stub.data.Util;
import api.stub.geometry.Range;
import client.gui.framework.session_mgr.SessionMgr;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;



public class HitAlignmentGBFWriter {

  private String species;
  private long assemblyVersion;
  private String completeFilePath;
  private ArrayList compositeRootEntities;

  public HitAlignmentGBFWriter(String species, long assemblyVersion, String completeFilePath,
      ArrayList compositeRootEntities) {
    this.species = species;
    this.assemblyVersion = assemblyVersion;
    this.completeFilePath = completeFilePath;
    this.compositeRootEntities = compositeRootEntities;

    try {
      createGameFile();
    }
    catch (Exception ex) {
      SessionMgr.getSessionMgr().handleException(ex);
    }
  }


  private void createGameFile() throws IOException{
       FileWriter writer=new FileWriter(completeFilePath);
       PrintWriter pwOut = new PrintWriter(writer);
       pwOut.println("<game version="+"\""+SessionMgr.getSessionMgr().getApplicationVersion()+"\""+" assembly_version="+"\""+assemblyVersion+
           "\""+" taxon="+"\""+species+"\">");

      Calendar cal = new GregorianCalendar();
      int year = cal.get( Calendar.YEAR );
      int day = cal.get( Calendar.DAY_OF_MONTH );
      String date=Util.getDateTimeStringNow();

      String month=date.substring(0,date.indexOf("/"));
      Feature setupFeature = (Feature)compositeRootEntities.iterator().next();
      pwOut.println("   <computational_analysis id=" + "\"Interactive:" + System.currentTimeMillis() + "\">");
      pwOut.println("     <date day=" + "\"" + day + "\"" + " year=" + "\"" + year + "\"" + " month=" + "\"" + month + "\">" + "</date>" );
      pwOut.println("     <program>" + setupFeature.getEnvironment() + "</program>");

      // Get all of the parent features of the entities passed in.
      for (Iterator itParents = compositeRootEntities.iterator(); itParents.hasNext(); ) {
        Feature tmpParent = (Feature) itParents.next();
        /**
         * @todo This is assuming that there is one alignment per axis.  This is
         * wrong.
         */
        GenomicAxis masterAxis = (GenomicAxis)((GeometricAlignment)tmpParent.getAlignmentsToAxes().
          iterator().next()).getAxis();
        Range tmpRange = ((GeometricAlignment)tmpParent.getOnlyAlignmentToAnAxis(masterAxis)).getRangeOnAxis();
        int tmpStart  = tmpRange.getStart();
        int tmpEnd    = tmpRange.getEnd();
        String parentId=prepareOidForWriteOut(getAxisForFeature(tmpParent));
        pwOut.println("     <result_set id=\"" + prepareOidForWriteOut(tmpParent.getOid()) + "\">");
        pwOut.println("     <type>" + prepareFeatureTypeForWriteOut(tmpParent.getEntityType().toString()) + "</type>");
        for (Iterator itChildren = tmpParent.getSubFeatures().iterator(); itChildren.hasNext(); ) {
          Feature tmpChild = (Feature)itChildren.next();
          printResultSpan(masterAxis, tmpChild, prepareFeatureTypeForWriteOut(tmpChild.getEntityType().toString()),
            parentId, tmpStart, tmpEnd, pwOut);
        }
        printPropertiesForFeature(tmpParent,pwOut,"        ");
        pwOut.println("     </result_set>");
      }

      pwOut.println("   </computational_analysis>");
      pwOut.println("</game>");
      writer.flush();
      writer.close();
  }


  private void printResultSpan(GenomicAxis masterAxis, Feature fentity, String spanType, String parenId, int fpstart, int fpend, PrintWriter pwOut)
    throws IOException {
      printFeatureComments(fentity,pwOut,"   ");
      pwOut.println("       <result_span id=\"" + prepareOidForWriteOut(fentity.getOid()) + "\">");
      pwOut.println("         <span_type>");
      pwOut.println("           "+prepareFeatureTypeForWriteOut(fentity.getEntityType().toString()));
      pwOut.println("         </span_type>");
      pwOut.println("         <seq_relationship type=\"query\" id=\"" + prepareOidForWriteOut(masterAxis.getOid()) + "\">");
      pwOut.println("           <span>");
      pwOut.println("             <start>" + fpstart + "</start>");
      pwOut.println("             <end>" + fpend + "</end>");
      pwOut.println("           </span>");
      pwOut.println("         </seq_relationship>");
      printPropertiesForFeature(fentity,pwOut,"          ");
      pwOut.println("       </result_span>");
  }


  private void printPropertiesForFeature(Feature f, PrintWriter pwOut, String noOfWhiteSpaces)
    throws IOException {
      Set properties=f.getProperties();
      if(properties!=null && properties.size()!=0){
          printPropertiesRecursive(f,properties,pwOut,noOfWhiteSpaces);
      }
  }


  private void printPropertiesRecursive(Feature featPI,Set props,PrintWriter pwOut,String noOfWhiteSpaces)
    throws IOException {
      for(Iterator it = props.iterator();it.hasNext();){
          GenomicProperty tmpProperty = (GenomicProperty) it.next();
          String name=tmpProperty.getName();
          if(!(name.equals(TranscriptFacade.TRANSCRIPT_ACCESSION_PROP))&& !(name.equals(TranscriptFacade.GENE_ACCESSION_PROP))&& !(name.equals(GeneFacade.GENE_ACCESSION_PROP)) && !(tmpProperty.isComputed())){
              if(!tmpProperty.getExpandable()){
                  printPropertyStartTag(tmpProperty,pwOut,noOfWhiteSpaces);
                  printPropertyEndTag(pwOut,noOfWhiteSpaces);
              }else{
                  printPropertyStartTag(tmpProperty,pwOut,noOfWhiteSpaces);
                  Set expandedProps=new HashSet();
                  GenomicProperty[] subProps=tmpProperty.getSubProperties();
                  if(subProps!=null){
                    for(int j=0;j<subProps.length;j++){
                      expandedProps.add(subProps[j]);
                    }
                  }

                  if(expandedProps!=null){
                      printPropertiesRecursive(featPI,expandedProps,pwOut,noOfWhiteSpaces.concat(" "));
                  }
                  printPropertyEndTag(pwOut,noOfWhiteSpaces);
              }
          }
      }//for
  }



  private void printPropertyStartTag(GenomicProperty p, PrintWriter pwOut, String noOfWhiteSpaces)
    throws IOException {
      String name=p.getName();
      String value=p.getInitialValue();
      boolean editable=p.getEditable();
      // Look for &.  Perhaps in the future add <,> and other characters.
      value=checkForEscapeCharacter(value);
      pwOut.println(noOfWhiteSpaces.concat(" ")+"<property "+"name="+ "\""+ name+"\""+" value="+"\""+value+"\""+" editable="+"\""+editable+"\""+">");
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
  private void printFeatureComments(Feature f, PrintWriter pwOut,String noOfWhiteSpaces)
    throws IOException {
      Collection newComments=f.getComments();

      for (Iterator it = newComments.iterator();it.hasNext();)
      {
          GenomicEntityComment coe=(GenomicEntityComment)it.next();
          String commentAuthor=coe.getCreatedBy();
          String commentDate=coe.getCreationDateAsString();
          String comment=coe.getComment();

          // Check and fix XML escape character.
          comment=checkForEscapeCharacter(comment);
          pwOut.println(noOfWhiteSpaces.concat(" ")+"<comments"+" author="+"\""+commentAuthor+"\""+" date="+"\""+commentDate+"\""+">");
          pwOut.println(noOfWhiteSpaces.concat("  ")+comment);
          pwOut.println(noOfWhiteSpaces.concat(" ")+"</comments>");
      }//for
  }// End method: printFeatureComments


  private void printPropertyEndTag(PrintWriter pwOut, String noOfWhiteSpaces) throws IOException {
      pwOut.println(noOfWhiteSpaces.concat(" ")+"</property>");
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
      return null;
  }
}