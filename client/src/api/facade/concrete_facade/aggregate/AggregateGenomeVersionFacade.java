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
 * CVS_ID:  $Id$
 */

package api.facade.concrete_facade.aggregate;

import api.entity_model.access.report.GenomeVersionAlignmentReport;
import api.entity_model.access.report.PropertyReport;
import api.entity_model.access.report.SubjectSequenceReport;
import api.entity_model.model.genetics.GenomeVersion;
import api.facade.abstract_facade.genetics.GenomeVersionLoader;
import api.stub.data.InvalidPropertyFormat;
import api.stub.data.NavigationPath;
import api.stub.data.OID;

import java.util.Vector;



public class AggregateGenomeVersionFacade extends AggregateGenomicFacade implements GenomeVersionLoader {

   GenomeVersion gv;

   public GenomeVersionAlignmentReport generateAlignmentReportForEntity(OID entityOID) {
      Object[]  aggregates = getAggregates();
      GenomeVersionAlignmentReport fpr = null;
      GenomeVersionAlignmentReport retVal = new GenomeVersionAlignmentReport();

      for ( int aggIdx = 0; aggIdx < aggregates.length; aggIdx++ ) {
         fpr = ( ( GenomeVersionLoader )aggregates[ aggIdx ] ).generateAlignmentReportForEntity( entityOID );

         if ( fpr != null ) {
            retVal.addAllLineItems(fpr);
         }
      }

      return ( retVal );
   }


   /**
    * Generates the feature property report for all available GenomeVersion
    * facades and aggregates the results into a single instance of
    * PropertyReport.
    *
    * @param      assemblyVersion   The assembly version that contains the
    *                               specified features.
    * @param      featureOIDs       Array of OID instances containing the
    *                               feature OIDs to retrieve properties for.
    * @param      propNames         Array of String instances containing the
    *                               name of the properties to retrieve.
    *
    * @return     Instance of PropertyReport containing the aggregate
    *             report from all GenomeVersion facades.
    */
   public PropertyReport generatePropertyReport( OID genomeVerOID, OID [] entityOIDs, String [] propNames ) {

      Object[]                                 aggregates  = this.getAggregates();
      PropertyReport                           fpr         = null;
      PropertyReport                           retVal      = new PropertyReport();

      for ( int aggIdx = 0; aggIdx < aggregates.length; aggIdx++ ) {

        fpr = ( ( GenomeVersionLoader )aggregates[ aggIdx ] ).generatePropertyReport( genomeVerOID, entityOIDs, propNames );

        if ( fpr != null ) {
          retVal.addAllLineItems(fpr);
        }
      }
      return ( retVal );
   }


   /**
    * Generates the subject sequence report for all features within the identified
    * features entityOID that share it's subject sequence definition
    *
    * @param      entityOID   The assembly version that contains the
    *                               specified features.
    * @param      featureOIDs       Array of OID instances containing the
    *                               feature OIDs to retrieve properties for.
    * @param      propNames         Array of String instances containing the
    *                               name of the properties to retrieve.
    *
    * @return     Instance of PropertyReport containing the aggregate
    *             report from all GenomeVersion facades.
    */
   public SubjectSequenceReport generateSubjectSequenceReportForEntity( OID entityOID ) {

      Object[]                                 aggregates  = this.getAggregates();
      SubjectSequenceReport                   fpr         = null;
      SubjectSequenceReport                   retVal      = new SubjectSequenceReport();

      for ( int aggIdx = 0; aggIdx < aggregates.length; aggIdx++ ) {
        fpr = ( ( GenomeVersionLoader )aggregates[ aggIdx ] ).generateSubjectSequenceReportForEntity( entityOID );

         if ( fpr != null ) {
            retVal.addAllLineItems(fpr);
         }
      }
      return ( retVal );
   }


  public NavigationPath[] getNavigationPath
    (OID speciesOID,
     String targetType,
     String target)
    throws InvalidPropertyFormat {

        Object[] aggregates=getAggregates();
        Vector navPathsVector= new Vector();
        NavigationPath[] partialArray = null;
        NavigationPath[] finalArray = null;
         // Search through all facades in the aggregate to find complete or partial paths.
        InvalidPropertyFormat ipf=null;
         for (int i = 0; i < aggregates.length; i++) {
           try {
             partialArray=null;
             partialArray=((GenomeVersionLoader)aggregates[i]).
                  getNavigationPath(speciesOID, targetType,target);
           }
           catch (InvalidPropertyFormat ipfEx) {ipf = ipfEx;}
           catch (Exception ex) {}


           if (partialArray != null) {
            if (partialArray.length > 0) {
              for (int j=0; j < partialArray.length; j++) {
                navPathsVector.addElement(partialArray[j]);
              } // for
            } // inner if
           } // outer if
         } // try
         if (navPathsVector != null) {
          if (navPathsVector.size() > 0) {
            // Now format the Paths to a NavigationNode array.
            finalArray = new NavigationPath[navPathsVector.size()];
            finalArray = (NavigationPath[])(navPathsVector.toArray(finalArray));
          }
          else
          {
            finalArray = new NavigationPath[0];
          }
         }
         if (ipf!=null && (finalArray.length==0)) throw ipf;
         return finalArray;
  }

  public NavigationPath[] getNavigationPath
    (String targetType,
     String target)
    throws InvalidPropertyFormat {

        Object[] aggregates=getAggregates();
        Vector navPathsVector= new Vector();
        NavigationPath[] partialArray = null;
        NavigationPath[] finalArray = null;
         // Search through all facades in the aggregate to find complete or partial paths.
        InvalidPropertyFormat ipf=null;
         for (int i = 0; i < aggregates.length; i++) {
           try {
             partialArray=null;
             partialArray=((GenomeVersionLoader)aggregates[i]).
                  getNavigationPath(targetType,target);
           }
           catch (InvalidPropertyFormat ipfEx) {ipf = ipfEx;}
           catch (Exception ex) {}


           if (partialArray != null) {
            if (partialArray.length > 0) {
              for (int j=0; j < partialArray.length; j++) {
                navPathsVector.addElement(partialArray[j]);
              } // for
            } // inner if
           } // outer if
         } // try
         if (navPathsVector != null) {
          if (navPathsVector.size() > 0) {
            // Now format the Paths to a NavigationNode array.
            finalArray = new NavigationPath[navPathsVector.size()];
            finalArray = (NavigationPath[])(navPathsVector.toArray(finalArray));
          }
          else
          {
            finalArray = new NavigationPath[0];
          }
         }
         if (ipf!=null) throw ipf;
         return finalArray;
  }

  public String getNavigationVocabIndex(){
        Object[] aggregates=getAggregates();
        String tmp = null;
        for (int i = 0; i < aggregates.length; i++) {
           try {
             tmp=((GenomeVersionLoader)aggregates[i]).
                  getNavigationVocabIndex();
           }
           catch (Exception ex) {}
           if (tmp != null && !tmp.equals("")) return tmp;
         }
         return "";
  }


   protected String getMethodNameForAggregates() {
      return ("getGenomeVersion");
   }

   protected Class[] getParameterTypesForAggregates() {
      return (new Class[0]);
   };

   protected  Object[] getParametersForAggregates() {
      return (new Object[0]);
   }
}
