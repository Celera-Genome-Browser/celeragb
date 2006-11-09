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
package api.entity_model.access.report;

import api.facade.abstract_facade.annotations.FeatureFacade;
import api.facade.abstract_facade.annotations.GenewiseFacade;
import api.facade.abstract_facade.annotations.HitAlignmentFacade;
import api.facade.abstract_facade.annotations.Sim4HitFacade;
import api.stub.data.OID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reports the location of Genomic Entities that have the same
 * subject Sequence ID as the request.
 *
 * @author Peter Davies <peter.davies>
 * @version 1.0
 */
public class SubjectSequenceReport extends AbstractReport {

/**********************
 * Inner Classes
 **********************/

   /**
    * Base class for all line items in this report
    */
   static public class SubjectSequenceReportLineItem implements LineItem {
      private OID oid;
      private OID axisOid;
      private String axisName;
      private String entityType;
      private int entityBeginOnAxis;
      private String accessionNumber;
      private String description;

      // Use these constants to set the report type.
      protected final static int BLAST_HIT_REPORT_TYPE   = 0;
      protected final static int SIM4_REPORT_TYPE        = 1;
      protected final static int GENEWISE_REPORT_TYPE    = 2;
      protected int reportType = -1;

      protected static List fields=new ArrayList();

      static {
         fields.add( FeatureFacade.FEATURE_TYPE_PROP );
         fields.add( FeatureFacade.GENOMIC_AXIS_NAME_PROP );
         fields.add( FeatureFacade.AXIS_BEGIN_PROP );
         fields.add( HitAlignmentFacade.ACCESSSION_NUM_PROP );
         fields.add( HitAlignmentFacade.DESCRIPTION_PROP );
      }

      public SubjectSequenceReportLineItem ( OID featureOid, OID axisOid, String axisName, String entityType, int entityBeginOnAxis, String accessionNumber, String description ) {
         this.oid = featureOid;
         this.axisOid = axisOid;
         this.axisName = axisName;
         this.entityType = entityType;
         this.entityBeginOnAxis = entityBeginOnAxis;
         this.accessionNumber = accessionNumber;
         this.description = description;
      }

      public OID getOid() {
         return ( oid );
      }

      public OID getAxisOid() {
         return ( axisOid );
      }

      /**
       * Build on the baseline field list for specific report types.
       * Report types vary by feature types, and hence line items created.
       */
      public List getFields() {
         List expandedFields = new ArrayList( fields );
         if ( reportType == SIM4_REPORT_TYPE ) {
            expandedFields.add( Sim4HitFacade.PERCENT_HIT_IDENTITY_PROP );
         }
         else if ( reportType == GENEWISE_REPORT_TYPE ) {
            expandedFields.add( GenewiseFacade.NUM_SIMILAR_PROP );
            expandedFields.add( GenewiseFacade.PERCENT_HIT_IDENTITY_PROP );
         }
         return ( Collections.unmodifiableList( expandedFields ) );
      }


      public String axisName() {
         return ( axisName );
      }

      public String entityType() {
         return ( entityType );
      }

      public int entityBeginOnAxis() {
         return ( entityBeginOnAxis );
      }

      public String accessionNumber() {
         return ( accessionNumber );
      }

      public String description() {
         return ( description );
      }

      public Object getValue( Object field ){
         int index = getFields().indexOf( field );
         switch ( index ) {
            case 0:
               return ( entityType() );
            case 1:
               return ( axisName() );
            case 2:
               return ( Integer.toString( entityBeginOnAxis ) );
            case 3:
               return ( accessionNumber() );
            case 4:
               return ( description() );
            default:
               return ( null );
         }
      }
   }

   /**
    * class to be used for all BlastHits
    */
   static public class BlastHitReportLineItem extends SubjectSequenceReportLineItem {
      public BlastHitReportLineItem ( OID featureOid, OID axisOid, String axisName, String entityType, int entityBeginOnAxis, String accessionNumber, String description ) {
         super( featureOid, axisOid, axisName, entityType, entityBeginOnAxis, accessionNumber, description );
         reportType = BLAST_HIT_REPORT_TYPE;
      }

      public Object getValue( Object field ){
         return ( super.getValue( field ) );
      }
   }

   /**
    * class to be used for all Sim4s
    */
   static public class Sim4ReportLineItem extends SubjectSequenceReportLineItem {
      private String pctHitIdentity;

      public Sim4ReportLineItem ( OID featureOid, OID axisOid, String axisName, String entityType, int entityBeginOnAxis, String accessionNumber, String description, String pctHitIdentity ) {
         super( featureOid, axisOid, axisName, entityType, entityBeginOnAxis, accessionNumber, description );
         reportType = SIM4_REPORT_TYPE;
         this.pctHitIdentity = pctHitIdentity;
      }

      public String pctHitIdentity() {
         return ( pctHitIdentity );
      }

      public Object getValue( Object field ){
         int index = getFields().indexOf( field );
         switch ( index ) {
            case 5:
               return ( pctHitIdentity() );
            default:
               return ( super.getValue( field ) );
         }
      }
   }


   /**
    * class to be used for all Genewise
    */
   static public class GenewiseReportLineItem extends SubjectSequenceReportLineItem {
      private int numSimilar;
      private String pctHitIdentity;

      public GenewiseReportLineItem ( OID featureOid, OID axisOid, String axisName, String entityType, int entityBeginOnAxis, String accessionNumber, String description, int numSimilar, String pctHitIdentity ) {
         super( featureOid, axisOid, axisName, entityType, entityBeginOnAxis, accessionNumber, description );
         this.numSimilar = numSimilar;
         this.pctHitIdentity = pctHitIdentity;
         reportType = GENEWISE_REPORT_TYPE;
      }

      public int numSimilar() {
         return ( numSimilar );
      }

      public String pctHitIdentity() {
         return ( pctHitIdentity );
      }

      public Object getValue( Object field ){
         int index = getFields().indexOf( field );
         switch ( index ) {
            case 5:
               return ( Integer.toString( numSimilar() ) );
            case 6:
               return ( pctHitIdentity() );
            default:
               return ( super.getValue( field ) );
         }
      }
   }
}

