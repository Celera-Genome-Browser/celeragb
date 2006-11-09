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
package api.entity_model.model.assembly;

import api.entity_model.model.fundtype.EntityTypeSet;
import api.entity_model.model.fundtype.LoadFilter;
import api.stub.data.FeatureDisplayPriority;
import api.stub.geometry.Range;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies
 * @version $Id$
 */


public class GenomicAxisLoadFilter extends LoadFilter {

  private Set discoveryEnvironments=new HashSet();
  private FeatureDisplayPriority displayPriority;

  GenomicAxisLoadFilter(String name) {
     super(name);
  }

  GenomicAxisLoadFilter(String name, EntityTypeSet entityTypes, Range range, boolean isStrandSpecific) {
     super(name,entityTypes,range,isStrandSpecific);
  }

  GenomicAxisLoadFilter(String name, String[] discoveryEnviroments, Range range, boolean isStrandSpecific) {
     super(name,range,isStrandSpecific);
     addDiscoveryEnvironments(discoveryEnviroments);
  }

  GenomicAxisLoadFilter(String name, FeatureDisplayPriority priority, Range range, boolean isStrandSpecific) {
     super(name,range,isStrandSpecific);
     displayPriority=priority;
  }

  GenomicAxisLoadFilter(String name, String[] discoveryEnviroments,
         FeatureDisplayPriority priority, Range range, boolean isStrandSpecific) {
     super(name, range,isStrandSpecific);
     addDiscoveryEnvironments(discoveryEnviroments);
     displayPriority=priority;
  }

  GenomicAxisLoadFilter(String name, EntityTypeSet entityTypes,
         FeatureDisplayPriority priority, Range range, boolean isStrandSpecific) {
     super(name,entityTypes, range, isStrandSpecific);
     displayPriority=priority;
  }

  GenomicAxisLoadFilter(String name, EntityTypeSet entityTypes,
         String[] discoveryEnviroments, Range range, boolean isStrandSpecific) {
     super(name,entityTypes, range,isStrandSpecific);
     addDiscoveryEnvironments(discoveryEnviroments);
  }

  GenomicAxisLoadFilter(String name, EntityTypeSet entityTypes,
         String[] discoveryEnviroments,FeatureDisplayPriority priority, Range range, boolean isStrandSpecific) {
     super(name,entityTypes, range, isStrandSpecific);
     addDiscoveryEnvironments(discoveryEnviroments);
     displayPriority=priority;
  }


  void addDiscoveryEnvironment(String discoveryEnvironment) {
     discoveryEnvironments.add(discoveryEnvironment);
  }

  public Collection getDiscoveryEnvironmentsAsCollection() {
     return Collections.unmodifiableCollection(discoveryEnvironments);
  }

  public String[] getDiscoveryEnvironmentsAsArray() {
     return (String[])discoveryEnvironments.toArray(new String[0]);
  }

  public boolean isFilteringOnDiscoveryEnvironment() {
     return (discoveryEnvironments.size()>0);
  }

  public boolean isFilteringOnDisplayPriority() {
     return (displayPriority!=null);
  }

  public FeatureDisplayPriority getFeatureDisplayPriority() {
     return displayPriority;
  }

  private void addDiscoveryEnvironments(String[] discoveryEnvironments) {
     for (int i=0;i<discoveryEnvironments.length;i++) {
        addDiscoveryEnvironment(discoveryEnvironments[i]);
     }
  }


}