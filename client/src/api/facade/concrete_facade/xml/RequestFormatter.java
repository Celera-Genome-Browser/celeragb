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
package api.facade.concrete_facade.xml;

/**
 * Title:        Request Formatter
 * Description:  Formats requests for features into URLs.
 * @author Les Foster
 * @version $Id$
 */

import api.entity_model.management.ModelMgr;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.fundtype.NavigationConstants;
import api.facade.facade_mgr.FacadeManager;
import api.stub.data.GenomeVersionInfo;
import api.stub.data.OID;
import api.stub.data.ReservedNameSpaceMapping;
import shared.util.GANumericConverter;

import java.net.URLEncoder;
import java.util.Set;

/**
 * Builds requests for feature loads, given the root URL, the entity model,
 * and the user name.
 */
public class RequestFormatter {

   //-------------------------------------CLASS MEMBER VARIABLES
   static private boolean mDebug = true;

   //-------------------------------------INSTANCE MEMBER VARIABLES
   private String mBaseLocation;
   private String mSeparator;

   //-------------------------------------CONSTRUCTORS
   /**
    * Constructor, takes root URL.
    */
   public RequestFormatter(String baseLocation) {
      mBaseLocation = baseLocation;
      if ( mBaseLocation.endsWith("/") ) {
         mSeparator = "/";
      } // Extra path URL
      else {
         mSeparator = "&";
         if ( mBaseLocation.indexOf("?") == -1 )
            mBaseLocation = mBaseLocation + "?";
      } // Parameters URL

   } // End constructor

   //-------------------------------------INTERFACE METHODS
   /**
    * Formats a request so that XML can be queried to obtain XML representing
    * all info on the OID in question.
    */
   public String formatRequestFor(OID featureOid) {
      StringBuffer accumulator = new StringBuffer(450);
      accumulator.append(mBaseLocation);
      accumulator.append("action=elaborate");
      accumulator.append(mSeparator);

      accumulator.append("feature="+featureOid.toString());
      accumulator.append(mSeparator);

      int lGVId = appendStandardParameters(accumulator, featureOid);
      accumulator.append(mSeparator);

      // Look for the entity for the feature in the entity model.  Need that
      // to obtain the axis identifiers.
      ModelMgr lModelManager = ModelMgr.getModelMgr();
      GenomeVersion lVersion = lModelManager.getGenomeVersionById(lGVId);
      Feature lFeature = (Feature)lVersion.getGenomicEntityForOid(featureOid);
      if ( lFeature != null ) {
         Set lAlignments = lFeature.getAlignmentsToAxes();
         if ( lAlignments.size() == 1 ) {
            Alignment lAlignment = (Alignment)lAlignments.iterator().next();
            Axis lAxis = lAlignment.getAxis();
            if ( lAxis != null ) {
               OID lAxisOid = lAxis.getOid();
               accumulator.append("axisid=").append(lAxisOid.toString());
               accumulator.append(mSeparator);

               // Two important cases:
               //  1> using internal namespace.  Must provide axis name.
               //  2> non-internal.  Provide the prefix-free version as alternative.
               accumulator.append("axisga=");
               if ( lAxisOid.isInternalDatabaseOID() )
                  accumulator.append(ReservedNameSpaceMapping.translateFromReservedNameSpace(lAxisOid.getNameSpaceAsString())+":"+
                                     GANumericConverter.getConverter().getGANameForOIDSuffix(lAxisOid.getIdentifierAsString()));
               else
                  accumulator.append(lAxisOid.toString());

            } // Got an axis.
         } // Has at least one alignment to an axis.
         else if ( lAlignments.size() > 1 ) {
            FacadeManager.handleException(new IllegalStateException("Multiple alignments to axes unhandled for XML feature retrieval "+
                                                                    "\nNot retrieving feature info for feature "+featureOid.toString()));
         } // More than one -- unhandled.
      } // Got a feature.

      if ( mDebug )
         System.out.println("Get Feature Data With "+accumulator.toString());
      return (accumulator.toString());
   } // End method

   /**
    * Formats a request so that servlet can be queried for a complete GAME
    * document containing <seq and <residues corresponding to the subject
    * seq whose OID is given.
    */
   public String formatSubjectSequenceRequestFor(OID subjectSequenceOid) {

      StringBuffer accumulator = new StringBuffer(450);
      accumulator.append(mBaseLocation);
      accumulator.append("action=sequence");
      accumulator.append(mSeparator);

      accumulator.append("id="+subjectSequenceOid.toString());
      accumulator.append(mSeparator);

      appendStandardParameters(accumulator, subjectSequenceOid);

      if ( mDebug )
         System.out.println("Get Subject Sequence Strings With "+accumulator.toString());
      return (accumulator.toString());

   } // End method

   /**
    * Formats a request so that servlet can be queried to obtain XML representing
    * all info on the OID in question.
    */
   public String formatAlignmentRequestFor(OID featureOid, OID axisOid) {
      StringBuffer accumulator = new StringBuffer(450);
      accumulator.append(mBaseLocation);
      accumulator.append("action=elaborate");
      accumulator.append(mSeparator);
      accumulator.append("type=alignment");
      accumulator.append(mSeparator);

      accumulator.append("feature="+featureOid.toString());
      accumulator.append(mSeparator);

      appendStandardParameters(accumulator, axisOid);
      accumulator.append(mSeparator);

      accumulator.append("axisid=").append(axisOid.toString());
      accumulator.append(mSeparator);
      // Two important cases:
      //  1> using internal namespace.  Must provide axis name.
      //  2> non-internal.  Provide the prefix-free version as alternative.
      accumulator.append("axisga=");

      if ( axisOid.isInternalDatabaseOID() )
         accumulator.append(ReservedNameSpaceMapping.translateFromReservedNameSpace(axisOid.getNameSpaceAsString())+":"+
                            GANumericConverter.getConverter().getGANameForOIDSuffix(axisOid.getIdentifierAsString()));
      else
         accumulator.append(axisOid.toString());

      if ( mDebug )
         System.out.println("Get Feature Alignment Strings With "+accumulator.toString());
      return (accumulator.toString());
   } // End method

   /** Formats a request to ask the servlet for its supported operations. */
   public String formatProtocolSupportRequest() {
      StringBuffer accumulator = new StringBuffer(mBaseLocation);
      accumulator.append("action=gb_protocol_support");
      return (accumulator.toString());
   } // End method

   /** Formats a protocol request for "load axis". */
   public String formatLoadFor(OID axisOID, int start, int end, boolean curation) {
      StringBuffer tmpString = new StringBuffer(mBaseLocation);

      tmpString.append("axisid=");
      tmpString.append(axisOID.toString());
      tmpString.append(mSeparator);

      tmpString.append("axisga=");
      // Two important cases:
      //  1> using internal namespace.  Must provide axis name.
      //  2> using unknown.  Provide the prefix-free version as alternative.
      if ( axisOID.isInternalDatabaseOID() )
         tmpString.append(ReservedNameSpaceMapping.translateFromReservedNameSpace(axisOID.getNameSpaceAsString())+":"+
                          GANumericConverter.getConverter().getGANameForOIDSuffix(axisOID.getIdentifierAsString()));
      else if ( axisOID.UNKNOWN_NAMESPACE.equals(axisOID.getNameSpaceAsString()) )
         tmpString.append(axisOID.getIdentifierAsString());
      tmpString.append(mSeparator);

      tmpString.append("action=load");
      tmpString.append(mSeparator);

      tmpString.append("start="+start);
      tmpString.append(mSeparator);

      tmpString.append("end="+end);
      tmpString.append(mSeparator);

      tmpString.append(curation ? "loadtype=curation" : "loadtype=precompute");
      tmpString.append(mSeparator);

      appendStandardParameters(tmpString, axisOID);

      String returnString = tmpString.toString();
      System.out.println("Contacting "+returnString);
      return (returnString);
   } // End method

   /**
    * Given subject seq on which to report, make an explicit URL to request rpt,
    * with the classic ? & CGI format.
    */
   public String formatReportOn(String searchTarget,
                                OID oidForGenomeVersionAndSpecies) {

      StringBuffer tmpString = new StringBuffer(mBaseLocation);

      // Make sure ampersands are consistent.
      tmpString.append("action=report");
      tmpString.append(mSeparator);

      tmpString.append("user=");
      tmpString.append(System.getProperties().getProperty("user.name"));
      tmpString.append(mSeparator);

      appendStandardParameters(tmpString, oidForGenomeVersionAndSpecies);
      tmpString.append(mSeparator);

      tmpString.append("rpttype=subjectseq");
      tmpString.append(mSeparator);

      tmpString.append("seqid=");
      tmpString.append(searchTarget);

      return (tmpString.toString());
   } // End method

   /**
    * Given type and value, and the user's URL, make an explicit URL for searching,
    * with the classic ? & CGI format.
    */
   public String formatSearchFor( int searchType, String searchTarget, OID oidForGenomeVersionAndSpecies ) {

      StringBuffer tmpString = new StringBuffer(mBaseLocation);

      tmpString.append(keywordForType(searchType)+"=");
      tmpString.append(URLEncoder.encode(searchTarget));
      tmpString.append(mSeparator);

      tmpString.append("action=search");
      tmpString.append(mSeparator);

      appendStandardParameters(tmpString, oidForGenomeVersionAndSpecies);

      if ( mDebug )
         System.out.println("Running a search with "+tmpString.toString());
      return (tmpString.toString());
   } // End method

   //-------------------------------------HELPER METHODS
   /**
    * Adds some params which go out for every request.
    */
   private int appendStandardParameters(StringBuffer accumulator, OID oid) {
      // Must obtain the genome version information, to flesh out the
      // assembly version and species.
      int lGVId = oid.getGenomeVersionId();
      GenomeVersionInfo lInfo = FacadeManager.getGenomeVersionInfo(lGVId);

      if ( lInfo != null ) {
         accumulator.append("assembly_number=").append(lInfo.getAssemblyVersion());
         accumulator.append(mSeparator);

         accumulator.append("species=").append(lInfo.getSpeciesName().replace(' ', '+'));
         accumulator.append(mSeparator);
      } // Got an info.

      accumulator.append("user=").append(System.getProperties().getProperty("user.name"));

      return (lGVId);

   } // End method

   /** Given a number, substitute back a string. */
   private String keywordForType( int searchType ) {
      switch ( searchType ) {
         case NavigationConstants.UNKNOWN_OID_INDEX:           return ("unknown");
         case NavigationConstants.TRANSCRIPT_ACCESSION_INDEX:  return ("transcript");
         case NavigationConstants.GENE_ACCESSION_INDEX:        return ("gene");
         case NavigationConstants.FEATURE_OID_INDEX:           return ("feature");
         case NavigationConstants.HIT_ALIGN_ACCESSION_INDEX:   return ("hitalignmentaccession");
         case NavigationConstants.PROTEIN_ACCESSION_INDEX:     return ("protein");
         case NavigationConstants.POLY_ACCESSION_INDEX:        return ("snp");
         case NavigationConstants.STS_NAME_INDEX:              return ("stsname");
         case NavigationConstants.SUBSEQ_OID_INDEX:            return ("samesubseq");
         case NavigationConstants.GENE_ONTOLOGY_NAME_INDEX:    return ("geneontology");
         case NavigationConstants.REG_REGION_ACCESSION_INDEX:  return ("reregion");
         default: throw new IllegalArgumentException( "Bad search type " + searchType );
      } // End switch
   } // End method

} // End class
