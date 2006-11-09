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
package client.gui.components.annotation.axis_annotation;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

import api.entity_model.access.observer.ReportObserver;
import api.entity_model.access.report.LineItem;
import api.entity_model.access.report.PropertyReport;
import api.entity_model.access.report.PropertyReportRequest;
import api.entity_model.access.report.Report;
import api.entity_model.access.report.ReportRequest;
import api.entity_model.management.PropertyMgr;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.genetics.GenomeVersion;
import api.stub.data.GenomicProperty;
import api.stub.data.OID;
import api.stub.geometry.Range;
import client.gui.framework.browser.Browser;
import client.gui.framework.display_rules.ColorIntensityInfo;
import client.gui.framework.display_rules.DisplayFilterInfo;
import client.gui.framework.display_rules.PropertySortInfo;
import client.gui.framework.session_mgr.BrowserModelListener;
import vizard.Glyph;
import vizard.genomics.glyph.TierGlyph;
import vizard.glyph.Packer;
import vizard.glyph.PropertySortedPacker;
import vizard.glyph.VerticalPacker;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DisplayRuleController extends Controller implements BrowserModelListener {

   private GenomicAxisAnnotationView view;
   private Browser browser;
   private PropertySortInfo currentPropertySortRule = null;
   private Range currentRuleRange = new Range(0,0);
   private ColorIntensityInfo currentColorRule = null;
   private Report currentColorRulePropertyReport = new PropertyReport();
   private boolean DEBUG = false;
   private Set sortedRootFeaturesByProperty = new HashSet();

   public DisplayRuleController(GenomicAxisAnnotationView view,
                                Browser browser  )
   {
      super(view);
      this.view=view;
      this.browser = browser;
      view.getBrowserModel().addBrowserModelListener(this);

   }


   public void modelPropertyChanged(Object key, Object oldValue, Object newValue){
      if ( key.equals(browser.getBrowserModel().DISPLAY_FILTER_PROPERTY) ) {
         if ( newValue instanceof ColorIntensityInfo ) {
            applyColorRule((ColorIntensityInfo)newValue);
         }
         else {
            applyPropertySortRule((PropertySortInfo)newValue);
         }
      }
      else if ( key.equals("ResetVerticalPacking") ) {
         restoreVerticalPacking();
      }

   }


   private void applyPropertySortRule(PropertySortInfo tmpRule) {
      this.currentPropertySortRule = (PropertySortInfo)tmpRule.clone();
      ArrayList targetFeatures=findTargetFeatures(currentPropertySortRule);
      Feature[] features = new Feature[targetFeatures.size()];

      targetFeatures.toArray(features);
      if ( features.length <= 0 )
         return;
      GenomeVersion genomeVersion = browser.getBrowserModel().getMasterEditorEntity().getGenomeVersion();
      String[] propNames = new String[1];
      propNames[0] = currentPropertySortRule.getTargetProperty();
      genomeVersion.generateReportBackground(new PropertyReportRequest(features, propNames), new MyPropertySortRuleReportObserver(features));
   }




   private void restoreVerticalPacking(){

      Packer packer;
      TierGlyph ftierGlyph = null;
      TierGlyph rtierGlyph = null;
      ArrayList glyphs=new ArrayList();
      Feature rootFeature;
      GenomeVersion genomeVersion = browser.getBrowserModel().getMasterEditorEntity().getGenomeVersion();



      for ( int k= 0;k<currentPropertySortRule.getEffectedFGs().size();k++ ) {
         ftierGlyph= view.getTierGlyph((String)currentPropertySortRule.getEffectedFGs().get(k), true);
         rtierGlyph= view.getTierGlyph((String)currentPropertySortRule.getEffectedFGs().get(k), false);

         for ( Iterator iter=sortedRootFeaturesByProperty.iterator();iter.hasNext(); ) {
            OID rootFeatureOid = ((Feature)iter.next()).getOid();
            rootFeature = (Feature) genomeVersion.getLoadedGenomicEntityForOid(rootFeatureOid);
            GBGenomicGlyph glyph=( GBGenomicGlyph)view.getGlyphFor(rootFeature);
            if ( glyph.isForward() && glyph.tierAncestor().equals(ftierGlyph) ) {
               packer=ftierGlyph.getPacker();
               packer.unpackChild(glyph);
               glyphs.add(glyph);
            }
            else {


               if ( rtierGlyph !=null && glyph.tierAncestor().equals(rtierGlyph) ) {
                  packer=rtierGlyph.getPacker();
                  packer.unpackChild(glyph);
                  glyphs.add(glyph);

               }

            }
         }

         packer=ftierGlyph.getPacker();
         ftierGlyph.removePacker();
         VerticalPacker newPacker=new VerticalPacker();
         ftierGlyph.addPacker(newPacker);

         if ( rtierGlyph !=null ) {
            packer=rtierGlyph.getPacker();
            rtierGlyph.removePacker();
            rtierGlyph.addPacker(new VerticalPacker());
         }


         for ( Iterator iterG = glyphs.iterator(); iterG.hasNext(); ) {
            Glyph g=(Glyph)iterG.next();
            if ( ((GBGenomicGlyph)g).isForward() )
               ftierGlyph.addGenomicChild((GBGenomicGlyph)g);

            else if ( rtierGlyph != null )
               rtierGlyph.addGenomicChild((GBGenomicGlyph)g);

         }
      }
      // finally refresh the view
      view.repaint();

   }


   /**
   * Find the features that have the targetProperty, and that belong to All
   * or a specifically-defined feature group.
   */
   private ArrayList findTargetFeatures(DisplayFilterInfo targetRule){
      ArrayList targetFeatures = new ArrayList();
      Collection items = view.getAllGBGenomicGlyphs();
      GBGenomicGlyph gl;
      Feature tmpEntity;
      GeometricAlignment featureAlignment;
      for ( Iterator i = items.iterator(); i.hasNext(); ) {
         gl = (GBGenomicGlyph)i.next();
         GeometricAlignment dataModel = gl.alignment();
         if ( (dataModel.getEntity() instanceof Feature) ) {

            tmpEntity = (Feature)dataModel.getEntity();
            if ( targetRule.getEffectedFGs().contains(tmpEntity.getEnvironment()) ) {
               featureAlignment = ((Feature)tmpEntity).getOnlyGeometricAlignmentToAnAxis((GenomicAxis)(browser.getBrowserModel().getMasterEditorEntity()));
               if ( isFeatureInSelectedRange(featureAlignment) ) {

                  Set names = PropertyMgr.getPropertyMgr().getPropertyNamesForEntity(tmpEntity);
                  if ( names.contains(targetRule.getTargetProperty()) ) {

                     targetFeatures.add(tmpEntity);

                  }
               }
            }
         }
      }
      return (targetFeatures);
   }



   private boolean isFeatureInSelectedRange(GeometricAlignment featureAlignment) {
      Range selRng = browser.getBrowserModel().getMasterEditorSelectedRange();
      return(selRng == null) ? false : selRng.intersects(featureAlignment.getRangeOnAxis());
   }



   private class MyPropertySortRuleReportObserver implements ReportObserver {
      private Feature[] requestFeatures;

      public MyPropertySortRuleReportObserver(Feature[] requestFeatures) {
         this.requestFeatures  = requestFeatures;
      }

      public void reportArrived(GenomicEntity entityThatReportWasRequestedFrom,
                                ReportRequest request, Report report)
      {
         fixReportPropertyNames(entityThatReportWasRequestedFrom, request, report);
         DisplayRuleController.this.propertySortPack((PropertyReport)report);
      }

   }


   private void propertySortPack(PropertyReport report){

      Feature feature;
      Feature rootFeature;
      GenomeVersion genomeVersion =browser.getBrowserModel().getMasterEditorEntity().getGenomeVersion();
      LineItem[] items = report.getLineItems();
      List itemsList= Arrays.asList(items);
      PropertyReport.ReportLineItem reportLineItem;
      Packer packer;
      Set rootFeatures =new HashSet();

      // assimilate only the root features from the line items.
      for ( int i=0; i < itemsList.size(); i++ ) {
         reportLineItem =  (PropertyReport.ReportLineItem) itemsList.get(i);
         feature = (Feature) genomeVersion.getLoadedGenomicEntityForOid(reportLineItem.getOid());
         rootFeatures.add(feature.getRootFeature());
      }
      List sortedList = sortBasedOnAverageValues(Collections.unmodifiableSet(rootFeatures), report);


      TierGlyph tierGlyph = null;
      //TierGlyph rtierGlyph = null;
      HashMap glyphTierHash = new HashMap();
      for ( Iterator iter=sortedList.iterator();iter.hasNext(); ) {
         OID rootFeatureOid = (OID)iter.next();
         rootFeature = (Feature) genomeVersion.getLoadedGenomicEntityForOid(rootFeatureOid);
         GBGenomicGlyph glyph =( GBGenomicGlyph)view.getGlyphFor(rootFeature);
         System.out.println("OID "+rootFeatureOid.getIdentifierAsString()+" Tier "+glyph.tierAncestor().name());
         // if(glyph.isForward()/* && glyph.tierAncestor().equals(ftierGlyph)*/ ){
         tierGlyph = glyph.tierAncestor();
         glyphTierHash.put(glyph, tierGlyph);
         packer=tierGlyph.getPacker();
         packer.unpackChild(glyph);

         //   }else {
         //    if(rtierGlyph !=null /*&& glyph.tierAncestor().equals(rtierGlyph)*/){
         /*     rtierGlyph = glyph.tierAncestor();
              glyphTierHash.add(glyph, rtierGlyph);
              packer=rtierGlyph.getPacker();
              packer.unpackChild(glyph);
            }
                       }
        */
      }// finished unpacking the glyphs

      // Iterate through all tiers and add new packers
      TierGlyph ftierGlyph = null;
      TierGlyph rtierGlyph = null;
      for ( int k= 0;k<currentPropertySortRule.getEffectedFGs().size();k++ ) {
         ftierGlyph= view.getTierGlyph((String)currentPropertySortRule.getEffectedFGs().get(k), true);
         rtierGlyph= view.getTierGlyph((String)currentPropertySortRule.getEffectedFGs().get(k), false);
         packer=ftierGlyph.getPacker();
         ftierGlyph.removePacker();
         PropertySortedPacker newPacker=new PropertySortedPacker();
         ftierGlyph.addPacker(newPacker);

         if ( rtierGlyph !=null ) {
            packer=rtierGlyph.getPacker();
            rtierGlyph.removePacker();
            rtierGlyph.addPacker(new PropertySortedPacker());
         }
      }

      // add all the sorted glyphs back to their respective tiers
      for ( Iterator iter=sortedList.iterator();iter.hasNext(); ) {
         OID rootFeatureOid = (OID)iter.next();
         rootFeature = (Feature) genomeVersion.getLoadedGenomicEntityForOid(rootFeatureOid);
         GBGenomicGlyph glyph =( GBGenomicGlyph)view.getGlyphFor(rootFeature);
         TierGlyph tier = (TierGlyph)glyphTierHash.get(glyph);
         tier.addGenomicChild((GBGenomicGlyph)glyph);
      }

      // finally refresh the view
      view.repaint();
   }


   private class PropValueComparator implements java.util.Comparator {
      public int compare(Object o1, Object o2) {

         Double d1=(Double)(o1);
         Double d2=(Double)(o2);
         return (d1.compareTo(d2));
      }
   } // End of ReportLineItemComparator class


   private List sortBasedOnAverageValues(Set rootFeatures, PropertyReport report){
      HashMap avgValueMap = new HashMap();
      List valueList = new ArrayList();
      List sortedFeatOids = new ArrayList();
      for ( Iterator iter = rootFeatures.iterator(); iter.hasNext(); ) {
         Feature feature = (Feature)iter.next();
         double values= 0.0;
         OID featOid = null;
         for ( Iterator iter2 = feature.getSubFeatures().iterator(); iter2.hasNext(); ) {
            featOid = ((Feature)iter2.next()).getOid();
            LineItem[] lineItems= report.getLineItems();
            List itemsList = Arrays.asList(lineItems);
            for ( Iterator iter3 = itemsList.iterator(); iter3.hasNext(); ) {
               PropertyReport.ReportLineItem lineItem = ((PropertyReport.ReportLineItem)iter3.next());
               OID itemOid = lineItem.getOid();
               if ( featOid.equals(itemOid) ) {
                  values = values + Double.parseDouble((String)lineItem.getValue(currentPropertySortRule.getTargetProperty()));
                  break;
               }
            }
         }

         avgValueMap.put(feature.getOid(), new Double(values/feature.getSubFeatureCount()));
      }
      valueList.addAll(avgValueMap.values());
      Collections.sort(valueList, new PropValueComparator());

      //sort feat oids based on value list
      for ( Iterator iter = valueList.iterator(); iter.hasNext(); ) {
         Double d  = (Double)iter.next();
         System.out.println("double "+d.doubleValue());
         for ( Iterator iter2 = avgValueMap.keySet().iterator(); iter2.hasNext(); ) {
            OID oid = (OID)iter2.next();
            if ( ((Double)avgValueMap.get(oid)).equals(d) && !sortedFeatOids.contains(oid) ) {
               sortedFeatOids.add(oid);
            }
         }
      }
      for ( Iterator i  = sortedFeatOids.iterator(); i.hasNext(); ) {
         OID o = (OID)i.next();
         System.out.println("OID "+o.getIdentifierAsString());
      }
      return (sortedFeatOids);

   }


   public void browserMasterEditorEntityChanged(GenomicEntity masterEditorEntity){}

   public void browserSubViewFixedRangeChanged(Range subViewFixedRange){}


   public void browserSubViewVisibleRangeChanged(Range subViewVisibleRange){}


   public void browserCurrentSelectionChanged(GenomicEntity newSelection){}


   public void browserMasterEditorSelectedRangeChanged(Range masterEditorSelectedRange){}

   public void browserClosing(){}

   private void applyColorRule(ColorIntensityInfo tmpRule) {
      boolean reuseReport = false;
      Range currentRange = browser.getBrowserModel().getMasterEditorSelectedRange();
      if ( currentColorRule==null || currentRange==null ) reuseReport=false;
      else if ( tmpRule.canReuseReport(currentColorRule) &&
                currentRange.equals(currentRuleRange) ) {
         if ( DEBUG ) System.out.println("Reusing report");
         reuseReport=true;
      }
      else {
         if ( DEBUG ) System.out.println("Not reusing report");
         reuseReport=false;
      }

      /**
       * Set the currentColorRule to the one passed in.  It may have different
       * criteria.  Whether we reuse the report or not.
       */
      currentColorRule = (ColorIntensityInfo)tmpRule.clone();

      if ( !reuseReport ) {
         currentRuleRange=currentRange;
         /**
          * Find the features that have the targetProperty, and that belong to All
          * or a specifically-defined feature group.
          */
         ArrayList targetFeatures = findTargetFeatures(tmpRule);
         Feature[] features = new Feature[targetFeatures.size()];
         targetFeatures.toArray(features);
         if ( features.length <= 0 )
            return;
         GenomeVersion genomeVersion = browser.getBrowserModel().getMasterEditorEntity().getGenomeVersion();
         String[] propNames = new String[1];
         propNames[0] = currentColorRule.getTargetProperty();
         genomeVersion.generateReportBackground(new PropertyReportRequest(features, propNames),
                                                new MyColorRuleReportObserver(features));
      }
      else {
         float[] intensities = new float[currentColorRulePropertyReport.getNumberOfLineItems()];
         Feature[] features = new Feature[currentColorRulePropertyReport.getNumberOfLineItems()];

         computeIntensities(currentColorRulePropertyReport, features,  intensities);
         setIntensities(features, intensities);
      }
   }


   /**
    * Inner class used to do intensity mapping of feature colors.
    */
   private class MyColorRuleReportObserver implements ReportObserver {
      private Feature[] requestFeatures;

      public MyColorRuleReportObserver(Feature[] requestFeatures) {
         this.requestFeatures  = requestFeatures;
      }

      public void reportArrived(GenomicEntity entityThatReportWasRequestedFrom,
                                ReportRequest request, Report report)
      {
         fixReportPropertyNames(entityThatReportWasRequestedFrom, request, report);
         currentColorRulePropertyReport = report;

         float[] intensities = new float[report.getNumberOfLineItems()];
         Feature[] features = new Feature[report.getNumberOfLineItems()];

         computeIntensities(report, features,  intensities);
         setIntensities(features, intensities);
      }
   }


   private void computeIntensities(Report report, Feature[] features,  float[] intensities) {
      Feature feature;
      String targetPropertyString;
      double targetPropertyValue;
      GenomeVersion genomeVersion =browser.getBrowserModel().getMasterEditorEntity().getGenomeVersion();

      LineItem[] items = report.getLineItems();
      PropertyReport.ReportLineItem reportLineItem;
      for ( int i=0; i < items.length; i++ ) {
         reportLineItem =  (PropertyReport.ReportLineItem) items[i];
         targetPropertyString = (String) reportLineItem.getValue(currentColorRule.getTargetProperty());
         feature = (Feature) genomeVersion.getLoadedGenomicEntityForOid(reportLineItem.getOid());
         features[i] = feature;

         try {
            targetPropertyValue = Double.parseDouble(targetPropertyString);
            intensities[i] = currentColorRule.getIntensityForValue(targetPropertyValue);
         }
         catch ( NumberFormatException ex ) {
            intensities[i] = -1.0f;
         }

         features[i] = (Feature) genomeVersion.getLoadedGenomicEntityForOid(reportLineItem.getOid());
      }
   }


   private void setIntensities(Feature[] features,  float[] intensities) {
      GBGenomicGlyph gl;
      Color origColor;
      float[] origRGB = new float[3];
      float intensity;
      GeometricAlignment featureAlignment;

      for ( int i=0; i < features.length; i++ ) {
         intensity = intensities[i];
         if ( (intensity >= 0) && (intensity <= 1) ) {
            featureAlignment = (GeometricAlignment)features[i].getOnlyAlignmentToAnAxis((GenomicAxis)(browser.getBrowserModel().getMasterEditorEntity()));
            if ( featureAlignment != null ) {
               gl = view.getGlyphFor(features[i]);
               origColor = gl.color();
               origColor.getRGBColorComponents(origRGB);
               gl.setColor(new Color(origRGB[0], origRGB[1], origRGB[2], intensity));
            }
         }
      }
      view.repaint();
   }


   /**
    * Evil helper method that exists till everyone uses the same property names.
    * Server Generated Sequence Analysis features need to have property names
    * changed.
    */
   private void fixReportPropertyNames(GenomicEntity entityThatReportWasRequestedFrom,
                                       ReportRequest request, Report report) {
      if ( report instanceof PropertyReport ) {
         OID [] oidCollection = request.getRequestedOids();
         for ( int x = 0; x < oidCollection.length; x++ ) {
            if ( oidCollection[x].isServerGeneratedOID() ) {
               //  This seems circuitous.  I wish there was a better way to get the axis entity.
               Feature feature = (Feature)((GenomeVersion)entityThatReportWasRequestedFrom).getGenomicEntityForOid(oidCollection[x]);
               PropertyReport.ReportLineItem newItem = new PropertyReport.ReportLineItem((OID)oidCollection[x]);
               GenomicProperty prop = feature.getProperty(currentColorRule.getTargetProperty());
               String name = currentColorRule.getTargetProperty();
               if ( name.equalsIgnoreCase("num_sim_or_pos") ) {
                  //  This is an evil hack until we all speak the same property name language.
                  prop = feature.getProperty("num_similar");
               }
               String value = new String();
               if ( prop==null ) value = "";
               else value = prop.getInitialValue();
               newItem.addProperty(name, value);
               report.addLineItem(newItem);
            }
         }
      }
   }
}