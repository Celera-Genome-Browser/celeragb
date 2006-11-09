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
package api.entity_model.access.filter;

import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.annotation.Feature;
import api.entity_model.model.genetics.GenomeVersion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

public class FiltrationDevice {

  private static FiltrationDevice device;

  private FiltrationDevice() {}

  public static FiltrationDevice getDevice() {
      if (device==null) device=new FiltrationDevice();
      return device;
  }

  public List executeAlignmentFilter(Collection collection, AlignmentCollectionFilter filter) {
      ArrayList rtnCollection=null;
      if (!filter.requestFilteredCollection()) {
        rtnCollection=new ArrayList(collection);
      }
      else {
        rtnCollection=new ArrayList(collection.size());
        Alignment[] alignments=getAlignmentCollectionArray(collection);
        for (int i=0;i<alignments.length;i++) {
          if (filter.addAlignmentToReturnCollection(alignments[i]))
             rtnCollection.add(alignments[i]);
        }
        rtnCollection.trimToSize();
      }
      if (filter.requestSortedCollection())
         Collections.sort(rtnCollection,filter.getComparator());
      return rtnCollection;
  }

  public List executeFeatureFilter( Collection collection, FeatureCollectionFilter filter) {
      ArrayList rtnCollection=null;
      if (!filter.requestFilteredCollection()) {
        rtnCollection=new ArrayList(collection);
      }
      else {
        rtnCollection=new ArrayList(collection.size());
        Feature[] features=(Feature[])getFeatureCollectionArray(collection);
        for (int i=0;i<features.length;i++) {
          if (filter.addFeatureToReturnCollection(features[i]))
             rtnCollection.add(features[i]);
        }
        rtnCollection.trimToSize();
      }
      if (filter.requestSortedCollection())
         Collections.sort(rtnCollection,filter.getComparator());
      return rtnCollection;
  }

  public List executeGenomeVersionFilter( Collection collection,
      GenomeVersionCollectionFilter filter) {

      ArrayList rtnCollection=null;
      if (!filter.requestFilteredCollection()) {
        rtnCollection=new ArrayList(collection);
      }
      else {
        rtnCollection=new ArrayList(collection.size());
        GenomeVersion[] versions=(GenomeVersion[])getGenomeVersionCollectionArray(collection);
        for (int i=0;i<versions.length;i++) {
          if (filter.addGenomeVersionToReturnCollection(versions[i]))
             rtnCollection.add(versions[i]);
        }
        rtnCollection.trimToSize();
      }
      if (filter.requestSortedCollection())
         Collections.sort(rtnCollection,filter.getComparator());
      return rtnCollection;
  }

  private Alignment[] getAlignmentCollectionArray(Collection collection) {
       return (Alignment[])collection.toArray(new Alignment[collection.size()]);
  }

  private Feature[] getFeatureCollectionArray(Collection collection) {
       Feature[] features=new Feature[collection.size()];
       collection.toArray(features);
       return features;
  }

  private GenomeVersion[] getGenomeVersionCollectionArray(Collection collection) {
       GenomeVersion[] versions=new GenomeVersion[collection.size()];
       collection.toArray(versions);
       return versions;
  }
}