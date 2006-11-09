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
package api.entity_model.model.fundtype;
/*********************************************************************
 *********************************************************************
   CVS_ID:  $Id$
*********************************************************************/

//import java.lang.reflect.*;
import api.facade.facade_mgr.FacadeManager;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

public class EntityType implements java.io.Serializable {
   static private Map discoveryEnvToFeatureName = new HashMap();
   static private Map valueToFeatureName = new HashMap();
   static private Map valueToDisplayName = new HashMap();
   static private Map featureNameToValue = new HashMap();
   static private Map valueToEntityType = new HashMap();

   private static final long serialVersionUID = 1;

   //  private final static String RESOURCE_FILE_NAME = "/resource/shared/DiscoveryEnvironment2FeatureType.txt";

   static {
      ResourceBundle bundle = ResourceBundle.getBundle("resource.shared.EntityType");
      String key;
      Integer newValue;
      String resourceValue;
      String featureName;
      String displayName;
      StringTokenizer tokenizer;
      try {
         for (Enumeration enum = bundle.getKeys(); enum.hasMoreElements();) {
            key = (String) enum.nextElement();
            resourceValue = bundle.getString(key);
            newValue = new Integer(key);
            if (resourceValue.indexOf("|") == -1) {
               featureName = resourceValue;
               displayName = featureName;
            }
            else {
               tokenizer = new StringTokenizer(resourceValue, "|");
               featureName = tokenizer.nextToken();
               displayName = tokenizer.nextToken();
               while (tokenizer.hasMoreTokens()) {
                  discoveryEnvToFeatureName.put(tokenizer.nextToken(), featureName);
               }
            }
            valueToFeatureName.put(newValue, featureName);
            valueToDisplayName.put(newValue, displayName);
            featureNameToValue.put(featureName, newValue);
            valueToEntityType.put(newValue, new EntityType(newValue.intValue()));
         }
      }
      catch (Exception ex) {
         FacadeManager.handleException(ex);
      }
   } // End static initializer

   private int value;

   private EntityType(int value) {
      this.value = value;
   }

   public static EntityType getEntityTypeForValue(int value) {
      EntityType et = (EntityType) valueToEntityType.get(new Integer(value));
      if (et == null)
         throw new IllegalArgumentException("Feature Value " + value + " is undefined");
      return et;
   }

   public static EntityType getEntityTypeForName(String name) {
      Integer value = (Integer) featureNameToValue.get(name);
      if (value == null)
         throw new IllegalArgumentException("Feature " + name + " is undefined");
      return getEntityTypeForValue(value.intValue());
   }

   public static EntityType getEntityTypeForDiscoveryEnvironment(String discoveryEnvironment) {
      String name = (String) discoveryEnvToFeatureName.get(discoveryEnvironment);
      if (name == null)
         throw new IllegalArgumentException("Feature with discovery environment " + discoveryEnvironment + " is undefined");
      return getEntityTypeForName(name);
   }

   public static EntityType[] allEntityTypes() {
      ArrayList entities = new ArrayList();
      Set keySet = valueToEntityType.keySet();
      for (Iterator it = keySet.iterator(); it.hasNext();) {
         entities.add(valueToEntityType.get(it.next()));
      }
      return (EntityType[]) entities.toArray(new EntityType[0]);
   }

   public static String[] allEntityNames() {
      ArrayList names = new ArrayList();
      Set keySet = valueToFeatureName.keySet();
      for (Iterator it = keySet.iterator(); it.hasNext();) {
         names.add(valueToFeatureName.get(it.next()));
      }
      return (String[]) names.toArray(new String[0]);
   }

   public String getEntityName() {
      return (String) valueToFeatureName.get(new Integer(value()));
   }

   public final int value() {
      return value;
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof EntityType)) {
         return false;
      }
      EntityType other = (EntityType) obj;
      return other.value() == this.value();
   }

   public int hashCode() {
      return this.value();
   }

   public String toString() {
      return (String) valueToDisplayName.get(new Integer(value()));
   }

}
