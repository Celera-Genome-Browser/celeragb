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
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/
package client.gui.components.annotation.ga_feature_report_view;

import api.entity_model.access.observer.ReportObserver;
import api.entity_model.access.report.*;
import api.entity_model.management.PropertyMgr;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.genetics.GenomeVersion;
import api.entity_model.model.genetics.Species;
import api.facade.abstract_facade.annotations.*;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;

import javax.swing.table.AbstractTableModel;
import java.util.*;

public class FeaturePropertyDataModel extends AbstractTableModel {
     private boolean DEBUG = false;
     private String visibleType;
     private Map typeMap = new HashMap(); //maps a type string to a list of features
     private List visibleFeatures;
     private boolean modelLoaded = false;
     private PropertyReport propertyReport;
     private HashMap cachedReports = new HashMap();
     private MyReportObserver currentReportObserver;

     // Use the modes to determine which properties to display in the table.
     public static final Integer BLAST_MODE    		= new Integer(0);
     public static final Integer GENEWISE_MODE 		= new Integer(1);
     public static final Integer SIM4_MODE     		= new Integer(2);
     public static final Integer EST_MAPPER_MODE    = new Integer(3);
	 public static final Integer ATALANTA_MODE     	= new Integer(4);

     // The properties that will be displayed for the specific modes.
     private String[] blastPropNames = new String[] {HSPFacade.ACCESSSION_NUM_PROP,
      HSPFacade.ALT_ACCESSION_PROP, HSPFacade.DESCRIPTION_PROP,
      HSPFacade.PERCENT_IDENTITY_PROP, HSPFacade.NUM_IDENTICAL_PROP,
      HSPFacade.NUM_GAPS_PROP, HSPFacade.E_VAL_PROP,
      HSPFacade.ALIGNMENT_LENGTH_PROP,
      HSPFacade.SUBJECT_SEQ_LENGTH_PROP, HSPFacade.BIT_SCORE_PROP};
     private String[] genewisePropNames = new String[] {GenewiseFacade.ACCESSSION_NUM_PROP,
      GenewiseFacade.ALT_ACCESSION_PROP, GenewiseFacade.DESCRIPTION_PROP,
      GenewiseFacade.PERCENT_HIT_IDENTITY_PROP, GenewiseFacade.BITS_PROP,
      GenewiseFacade.FRAMESHIFTS_PROP, GenewiseFacade.PERCENT_LENGTH_PROP,
      GenewiseFacade.SUBJECT_SEQ_LENGTH_PROP
     };
     private String[] sim4PropNames = new String[] { Sim4HitFacade.ACCESSSION_NUM_PROP,
      Sim4HitFacade.ALT_ACCESSION_PROP, Sim4HitFacade.DESCRIPTION_PROP,
      Sim4HitFacade.PERCENT_HIT_IDENTITY_PROP, Sim4DetailFacade.SEQ_IDENTITY_SCORE_PROP ,
      Sim4HitFacade.PERCENT_LENGTH_PROP, Sim4HitFacade.SUBJECT_SEQ_LENGTH_PROP
     };
	private String[] estMapperPropNames = new String[] { ESTMapperHitFacade.ACCESSSION_NUM_PROP,
	 ESTMapperHitFacade.ALT_ACCESSION_PROP, ESTMapperHitFacade.DESCRIPTION_PROP,
	 ESTMapperHitFacade.PERCENT_HIT_IDENTITY_PROP, /*ESTMapperHitFacade.SEQ_IDENTITY_SCORE_PROP ,*/
	 ESTMapperHitFacade.PERCENT_LENGTH_PROP, ESTMapperHitFacade.SUBJECT_SEQ_LENGTH_PROP
	};
	private String[] atalantaPropNames = new String[] { AtalantaHitFacade.ACCESSSION_NUM_PROP,
		AtalantaHitFacade.ALT_ACCESSION_PROP, AtalantaHitFacade.DESCRIPTION_PROP,
		AtalantaHitFacade.PERCENT_HIT_IDENTITY_PROP, /*AtalantaHitFacade.SEQ_IDENTITY_SCORE_PROP , */
		AtalantaHitFacade.PERCENT_LENGTH_PROP, AtalantaHitFacade.SUBJECT_SEQ_LENGTH_PROP
	};
     //  The collection that will be used for the table
     private String[] columnNames;


     public FeaturePropertyDataModel() {
      super();
     }


     public Report getPropertyReport() { return propertyReport; }


     public Feature getFeatureForRowIndex(int rowIndex) {
      return (Feature)visibleFeatures.get(rowIndex);
     }

     public int getRowCount() {
        if (propertyReport!=null)
           return propertyReport.getNumberOfLineItems();
        return 0;
     }


     public int getColumnCount() {
        if (!modelLoaded) return 0;
        if (columnNames==null) return 0;
        return columnNames.length;
     }


     public String getColumnName(int col) {
          if (!modelLoaded) return null;
          return PropertyMgr.getPropertyMgr().getPropertyDisplayName(columnNames[col]);
     }


    private Double getScore(GenomicProperty[] props) {
      if (!modelLoaded) return new Double(0);
      if ( props == null ) return Double.valueOf("0");
      for ( int c = 0; c < props.length; c++ )
      {
          if ((props[c].getName() != null) && (props[c].getName().toUpperCase().equals("SCORE")))
          {
               try
               {
                    Double d = Double.valueOf(props[c].getInitialValue());
                    return d;
               }
               catch(Exception e) { }
          }
      }
      return Double.valueOf("0");
    }


     public Object getValueAt(int row, int col) {
      if (!modelLoaded) return null;
      if (row >= getRowCount()) return null;

      if (propertyReport == null) return null;

      LineItem[] lineItems = propertyReport.getLineItems();

      if (col >= getColumnCount()) return null;

      if (row < lineItems.length) {
        Collection props = ((PropertyReport.ReportLineItem)lineItems[row]).getFields();
        if (col < columnNames.length) {
          for (Iterator it = props.iterator();it.hasNext();) {
            String tmpProps = (String)it.next();
            if (tmpProps.equals(columnNames[col])) {
              String propVal = (String)((PropertyReport.ReportLineItem)lineItems[row]).getValue(tmpProps);
              if (propVal==null) return new String("");
              try { //try as an integer
                //System.out.println("parseing " + propVal + " as Integer");
                Integer val = Integer.valueOf(propVal);
                return val;
              }
              catch (NumberFormatException e_int) {
                // This could be done better.
                try {
                  if (tmpProps.equals(HSPFacade.PERCENT_IDENTITY_PROP)) {
                    return Float.valueOf(propVal);
                  }
                  Double val = Double.valueOf(propVal);
                  //System.out.println("parsed " + propVal + " as Double");
                  return val;
                } catch (NumberFormatException e_double) { }
              }
              //System.out.println("parsed " + propVal + " as String");
              return propVal;
            }
          } //end for
        }
      }
      return null;
     }

     /**
      * Given an object thought to be represented in the datamodel, return
      * either its index, or -1.
      */
     public int getObjectRow(Object dataObject) {
         if (!modelLoaded)
             return 0;

         // Trundle through the line items, trying to match OIDs.
         LineItem[] allLineItems = propertyReport.getLineItems();
         OID nextOid = null;
         if (dataObject instanceof GenomicEntity) {
             OID featureOid = ((GenomicEntity)dataObject).getOid();
             for (int i = 0; i < allLineItems.length; i++) {
                 nextOid = (OID)allLineItems[i].getValue("id");
                 if (nextOid.equals(featureOid))
                     return i;
             } // For all entities reported upon
         } // Right type of data.

         return -1;
     } // End method

     public boolean isLoaded() {
        return modelLoaded;
     }


     /**
      * clear the model of its current data.
      */
     public void clear() {
          cachedReports.clear();
          modelLoaded = false;
          columnNames = null;
          visibleFeatures = null;
          typeMap.clear();
          propertyReport = null;
     }


    /**
    * Search a column for an object.
    *
    public int getObjectRow(Object obj) {
        if (!modelLoaded) return 0;
        return visibleFeatures.indexOf(obj);
    }
    */

    public Class getColumnClass(int col) {
          if (!modelLoaded) return null;
          Object val = getValueAt(0, col);
          if (val != null)
               return val.getClass();
          else
               return String.class;
     }


      /**
       *  New version calls new bizobj method
       */
      public void setVisibleType(GenomeVersion genomeVersion, String type, Integer tableModeForEntityType){
          Species speciesEntity = genomeVersion.getSpecies();
          visibleType = type;
          visibleFeatures = (List) typeMap.get(type);

          if (visibleFeatures == null) return;
          /**
           * Check for the pre-existence of the report.
           */
          if (cachedReports.containsKey(visibleType)) {
            propertyReport = (PropertyReport)cachedReports.get(visibleType);
            setColumnNames(tableModeForEntityType);
            fireTableDataChanged();
            fireTableStructureChanged();
            return;
          }

          int numRows = visibleFeatures.size();
          Feature[] fa = new Feature[numRows];
          for ( int row = 0; row < numRows; row++ ) {
            fa[row] = (Feature)visibleFeatures.get(row);
          }

          setColumnNames(tableModeForEntityType);

          // Unset response for any outstanding report observer.
          if (currentReportObserver != null)
            currentReportObserver.setRelevant(false);

          // Get the report for the selected tab (feature group).
          MyReportObserver reportObserver = new MyReportObserver(type);
          speciesEntity.getGenomeVersion().generateReportBackground(
            new PropertyReportRequest(fa, columnNames), reportObserver);

     }

     private void setColumnNames(Integer tableModeForEntityType) {
          //  Add the properties that correspond to the mode.
          ArrayList tmpList = new ArrayList();
          if (tableModeForEntityType.equals(BLAST_MODE))
            tmpList.addAll(Arrays.asList(blastPropNames));
          else if (tableModeForEntityType.equals(GENEWISE_MODE))
            tmpList.addAll(Arrays.asList(genewisePropNames));
          else if (tableModeForEntityType.equals(SIM4_MODE))
            tmpList.addAll(Arrays.asList(sim4PropNames));
		  else if (tableModeForEntityType.equals(EST_MAPPER_MODE))
		    tmpList.addAll(Arrays.asList(estMapperPropNames));
		  else if (tableModeForEntityType.equals(ATALANTA_MODE))
		    tmpList.addAll(Arrays.asList(atalantaPropNames));
          columnNames = new String[tmpList.size()];
          for (int i = 0; i < tmpList.size(); i++) {
            columnNames[i] = (String)tmpList.get(i);
          }

     }

     /**
     * load the data model by getting the genomic properties for each feature in the feature set.
     */
     public void loadModel(Set featureSet) {
          if (featureSet == null) return;
          modelLoaded = false;
          Feature feature;
          List typeList;
          for (Iterator i=featureSet.iterator(); i.hasNext(); )
          {
               feature = (Feature) i.next();
               if (feature !=null)
               {
                    typeList = (List) typeMap.get(feature.getRootFeature().getEnvironment());
                    if (typeList==null)
                    {
                         //add new feature type
                         typeList = new ArrayList();
                         typeMap.put(feature.getRootFeature().getEnvironment(), typeList);
                    }
                    typeList.add(feature);
               }
          }
     }


     private class MyReportObserver implements ReportObserver {
        private boolean isRelevant = true;
        private String observedVisibleType;

        /** Retaining the type that this observer is waiting for. */
        public MyReportObserver(String type) {
          observedVisibleType = type;
        } // End constructor

        /** Allow external dictation of whether report observation will be 'taken seriously'. */
        public void setRelevant(boolean relevant) {
          isRelevant = relevant;
        } // End method

        public void reportArrived(GenomicEntity genomicEntity, ReportRequest request,Report report) {
          // Do not "observe" the event.
          if (! isRelevant)
            return;

          if (! visibleType.equals(observedVisibleType))
            return;

          if (report instanceof PropertyReport) {
            propertyReport = (PropertyReport)report;
            OID [] oidCollection = request.getRequestedOids();
            modelLoaded = true;
            for (int x = 0; x < oidCollection.length; x++) {
              if (oidCollection[x].isServerGeneratedOID()) {
                //  This seems circuitous.  I wish there was a better way to get the axis entity.
                Feature feature = (Feature)((GenomeVersion)genomicEntity).getGenomicEntityForOid(oidCollection[x]);
                PropertyReport.ReportLineItem newItem = new PropertyReport.ReportLineItem((OID)oidCollection[x]);
                for (int y=0; y < blastPropNames.length; y++) {
                  GenomicProperty prop = feature.getProperty(blastPropNames[y]);
                  String name = blastPropNames[y];
                  if (name.equalsIgnoreCase("num_sim_or_pos")) {
                    //  todo This is an evil hack until we all speak the same property name language.
                    prop = feature.getProperty("num_similar");
                  }
                  String value = new String();
                  if (prop==null) value = "";
                  else value = prop.getInitialValue();
                  newItem.addProperty(name, value);
                }
                report.addLineItem(newItem);
              }
            }

            /**
             * Requirement to cache previous reports.  Hopefully this isn't
             * equivalent to calling getProperties on all these things, in terms
             * of memory space.
             */
            cachedReports.put(observedVisibleType, propertyReport);
            if (observedVisibleType.equals(visibleType))
              fireTableStructureChanged();
          }
        }
     }
}