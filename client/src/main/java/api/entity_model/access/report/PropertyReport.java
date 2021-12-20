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

import api.stub.data.GenomicProperty;
import api.stub.data.OID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reports the property values of features.
 *
 * @author Lou Blick <lou.blick>
 * @version 1.0
 */
public class PropertyReport extends AbstractReport {


   /**
    * ReportLineItem is an internal class used by PropertyReport
    * to hold the properties for an individual feature.
    */
   static public class ReportLineItem implements LineItem {

      /**
       * The OID of the feature.
       */
      private OID oid = null;

      /**
       * An array of property names for the feature.
       */
      private List propertyNames = new ArrayList();

      /**
       * An array of property values for the feature.
       */
      private List propertyValues = new ArrayList();


      /**
       * Constructor for the FeatureReportLineItem.
       * @todo Remove this constructor
       */
      public ReportLineItem( OID oid, GenomicProperty [] props ) {

      }

      /**
       * Constructor for this lineItem
       */
      public ReportLineItem( OID oid ) {
         this.oid=oid;
      }

      /**
       * Used to add a new property/value into this line item
       */
      public void addProperty(String name, String value) {
        if (!propertyNames.contains(name)){
          propertyNames.add(name);
          propertyValues.add(value);
        }
      }


      /**
       * Returns the OID of the feature.
       */
      public OID getOid() {
         return ( this.oid );
      }


      public Object getValue(Object field) {
         if (field.equals("id")) return getOid();
         int index=propertyNames.indexOf(field);
         if (index<0) return null;
         return propertyValues.get(index);
      }

      public List getFields(){
         List allProps=new ArrayList(propertyNames);
         allProps.add(0,"id");
         return Collections.unmodifiableList(allProps);
      }
   }
}

