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
import api.facade.abstract_facade.annotations.HitAlignmentDetailLoader;
import api.stub.data.OID;
import api.stub.geometry.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Reports the location of Genomic Entities that are aligned to a particular
 * GenomeVersion. The specifics of the call made on GenomeVersion to generate
 * the report determines what set of Genomic Entities will be reported.
 *
 * @author        Lou Blick <lou.blick>
 * @version       2.0
 */
public class GenomeVersionAlignmentReport extends AbstractReport {

   /**
    * Inner class used to hold each line item of the Entity Alignment Report.
    */
   static public class AlignmentReportLineItem implements LineItem {

      /**
       * The OID of the axis that the entities are aligned to.
       */
      private           OID            axisOID                 = null;

      /**
       * The name of the axis that the entities are aligned to.
       */
      private           String         axisName                = null;

      /**
       * The type of the axis that the entities are aligned to.
       */
      private           String         axisType                = null;

      /**
       * The OID of the entity.
       */
      private           OID            entityOID               = null;

      /**
       * The axis position to which the entity begins its alignment.
       */
      private           int            alignmentBeginOnAxis    = 0;

      /**
       * The axis position to which the entity ends its alignment.
       */
      private           int            alignmentEndOnAxis      = 0;

      /**
       * The length of the entity.
       */
      private           int            entityLength            = 0;

      /**
       * The type of entity.
       */
      private           String         entityType              = null;

      /**
       * The subject description of the entity.
       */
      private           String         description             = null;

      /**
       * The subject accession for the entity.
       */
      private           String         accession               = null;

      /**
       * An alternative subject accession for the entity.
       */
      private           String         altAccession            = null;

      /**
       * The orientation of the entity to the axis.
       */
      private           String         entityOrientation       = null;

      /**
       * The list of report fields contained in this line item.
       */
      private  static   List           fields                  = new ArrayList();

      /**
       * Initialize the field list.
       */
      static {
         fields.add( FeatureFacade.GENOMIC_AXIS_NAME_PROP );
         fields.add( "Axis Type" );
         fields.add( FeatureFacade.AXIS_BEGIN_PROP );
         fields.add( FeatureFacade.AXIS_END_PROP );
         fields.add( HitAlignmentDetailLoader.DESCRIPTION_PROP );
         fields.add( HitAlignmentDetailLoader.ACCESSSION_NUM_PROP );
         fields.add( HitAlignmentDetailLoader.ALT_ACCESSION_PROP );
         fields.add( FeatureFacade.ENTITY_LENGTH_PROP );
         fields.add( "Strand" );
      }

      /**
       * Default constructor.
       */
      public AlignmentReportLineItem() {
      }


      /**
       * Constructor. Initializes the report line item with the following data:
       *
       * @param   anAxisOID      The OID of the axis to which the entity is aligned.
       * @param   anAxisName     The name of the axis to which the entity is aligned.
       * @param   anAxisType     The type of the axis to which the entity is aligned.
       * @param   anEntityOID    The OID of the aligned entity.
       * @param   anEntityType   The type of aligned entity.
       * @param   aRangeOnAxis   The alignment range of the entity on the axis.
       * @param   anEntityLength The entity length.
       */
      public AlignmentReportLineItem( OID anAxisOID,
                                      String anAxisName,
                                      String anAxisType,
                                      OID anEntityOID,
                                      String anEntityType,
                                      Range aRangeOnAxis,
                                      int anEntityLength ) {
         this.axisOID               = anAxisOID;
         this.axisName              = anAxisName;
         this.axisType              = anAxisType;
         this.entityOID             = anEntityOID;
         this.entityType            = anEntityType;
         this.alignmentBeginOnAxis  = aRangeOnAxis.getStart();
         this.alignmentEndOnAxis    = this.alignmentBeginOnAxis + aRangeOnAxis.getMagnitude();
         this.entityLength          = anEntityLength;
         if ( aRangeOnAxis.getOrientation().equals( Range.REVERSE_ORIENTATION ) ) {
            this.entityOrientation  = "Reverse";
         }
         else if ( aRangeOnAxis.getOrientation().equals( Range.FORWARD_ORIENTATION ) ) {
            this.entityOrientation  = "Forward";
         }
         else {
            this.entityOrientation  = "Unknown";
         }
         this.accession = "";
         this.altAccession = "";
         this.description = "";
      }


      /**
       * Constructor. Initializes the report line item with the following data:
       *
       * @param   anAxisOID      The OID of the axis to which the entity is aligned.
       * @param   anAxisName     The name of the axis to which the entity is aligned.
       * @param   anAxisType     The type of the axis to which the entity is aligned.
       * @param   anEntityOID    The OID of the aligned entity.
       * @param   anEntityType   The type of aligned entity.
       * @param   aRangeOnAxis   The alignment range of the entity on the axis.
       * @param   aDescription  The description.
       * @param   anAccession    The subject seq accession.
       * @param   anAltAccession The subject seq alternative accession.
       * @param   anEntityLength The entity length.
       */
      public AlignmentReportLineItem( OID anAxisOID,
                                      String anAxisName,
                                      String anAxisType,
                                      OID anEntityOID,
                                      String anEntityType,
                                      Range aRangeOnAxis,
                                      String aDescription,
                                      String anAccession,
                                      String anAltAccession,
                                      int anEntityLength ) {
         this.axisOID               = anAxisOID;
         this.axisName              = anAxisName;
         this.axisType              = anAxisType;
         this.entityOID             = anEntityOID;
         this.entityType            = anEntityType;
         this.alignmentBeginOnAxis  = aRangeOnAxis.getStart();
         this.alignmentEndOnAxis    = this.alignmentBeginOnAxis + aRangeOnAxis.getMagnitude();
         this.description           = aDescription;
         this.accession             = anAccession;
         this.altAccession          = anAltAccession;
         this.entityLength          = anEntityLength;
         if ( aRangeOnAxis.getOrientation().equals( Range.REVERSE_ORIENTATION ) ) {
            this.entityOrientation  = "Reverse";
         }
         else if ( aRangeOnAxis.getOrientation().equals( Range.FORWARD_ORIENTATION ) ) {
            this.entityOrientation  = "Forward";
         }
         else {
            this.entityOrientation  = "Unknown";
         }
      }





      /**
       * Returns the OID of the axis to which the entity is aligned to.
       *
       * @return  OID containing the axis.
       */
      public OID getAxisOID() {
         return ( this.axisOID );
      }


      /**
       * Returns the name of the axis to which the entity is aligned to.
       *
       * @return  String containing the axis name.
       */
      public String getAxisName() {
         return ( this.axisName );
      }


      /**
       * Returns the type of the axis to which the entity is aligned to.
       *
       * @return  String containing the axis type.
       */
      public String getAxisType() {
         return ( this.axisType );
      }


      /**
       * Returns the OID of the aligned entity.
       *
       * @return  OID containing the aligned entity.
       */
      public OID getEntityOID() {
         return ( this.entityOID );
      }


      /**
       * Returns the type of the aligned entity.
       *
       * @return  String containing the aligned entity type.
       */
      public String getEntityType() {
         return ( this.entityType );
      }


      /**
       * Returns the entity alignment start position on the axis.
       *
       * @param   int containing the alignment start position.
       */
      public int getEntityBeginOnAxis() {
         return ( this.alignmentBeginOnAxis );
      }

      /**
       * Returns the entity length on axis.
       *
       * @return   int entity length on axis.
       */
      public int getEntityLength() {
         return ( this.entityLength );
      }

      /**
       * Returns the entity alignment end position on the axis.
       *
       * @param   int containing the alignment end position.
       */
      public int getEntityEndOnAxis() {
         return ( this.alignmentEndOnAxis );
      }


      /**
       * Returns the entity's subject description.
       *
       * @return   String description
       */
      public String getDescription() {
         return ( this.description );
      }


      /**
       * Returns the entity alignment end position on the axis.
       *
       * @return   String accession of subject.
       */
      public String getAccession() {
         return ( this.accession );
      }


      /**
       * Returns the length of the aligned entity.
       *
       * @return  String the alternative accession for subject seq.
       */
      public String getAltAccession() {
         return ( this.altAccession );
      }


      /**
       * Returns the name of the strand to which the entity is aligned.
       *
       * @return  String containing the aligned entity strand: Forward,
       *          Reverse, or Unknown.
       */
      public String getEntityStrandOnAxis() {

         return ( this.entityOrientation );
      }


      /**
       * Returns the value for a particular field.
       *
       * @param
       */
      public Object getValue( Object aField ) {
         int index = fields.indexOf( aField );
         switch ( index ) {
            case 0:
               return ( this.getAxisName() );
            case 1:
               return ( this.getAxisType() );
            case 2:
               return ( Integer.toString( this.getEntityBeginOnAxis() ) );
            case 3:
               return ( Integer.toString( this.getEntityEndOnAxis() ) );
            case 4:
               return ( Integer.toString( this.getEntityLength() ) );
            case 5:
               return ( this.getDescription() );
            case 6:
               return ( this.getAccession() );
            case 7:
               return ( this.getAltAccession() );
            case 8:
               return ( this.getEntityStrandOnAxis() );
            default:
               return ( null );
         }
      }


      /**
       * Returns a list of the report fields contained in each line item.
       *
       * @return  List containing an unmodifiable list of fields names.
       */
      public List getFields() {
         return ( Collections.unmodifiableList( this.fields ) );
      }
   }
}

