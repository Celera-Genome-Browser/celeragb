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

import api.facade.facade_mgr.FacadeManager;

import java.util.*;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author Peter Davies
 * @version $Id$
 *
 * This class allows you to define a set of entities and give it a certain name.
 *
 * The static methods allow you to get all defined EntityTypeSets, or a given set by name.
 *
 * The initial sets are created by reading the resource.shared.EntityTypeSet.properties file.
 *
 * Other sets can be added using the public constructor, which will had them into the array
 * returned by the static method getEntityTypeSets().
 *
 * 02/13/2001 JB - It is also now possible to create temporary EntityTypeSet
 * objects that are unamed.
 *
 * The class is an AbstractSet as defined by the collections framework.  The Set contains the
 * EntityTypes
 */

public class EntityTypeSet extends AbstractSet implements java.io.Serializable {

  private static Map nameToEntityTypeSet=new HashMap();

  private static final long serialVersionUID=1;

  static {
      ResourceBundle bundle=ResourceBundle.getBundle("resource.shared.EntityTypeSet");
      String setName;
      String resourceValue;
      StringTokenizer tokenizer;
      EntityTypeSet newSet;
      List typeList=new ArrayList();
      try {
        for (Enumeration e=bundle.getKeys();e.hasMoreElements();) {
           setName=(String)e.nextElement();
           resourceValue=bundle.getString(setName);
           if (resourceValue.indexOf(",")==-1) {
             newSet=new EntityTypeSet(setName,new EntityType[] {EntityType.getEntityTypeForName(resourceValue)});
             nameToEntityTypeSet.put(setName,newSet);
           }
           else {
             tokenizer=new StringTokenizer(resourceValue, ",");
             typeList.clear();
             while (tokenizer.hasMoreTokens()) {
                typeList.add(EntityType.getEntityTypeForName(tokenizer.nextToken()));
             }
             newSet=new EntityTypeSet(setName,(EntityType[])typeList.toArray(new EntityType[0]));
             nameToEntityTypeSet.put(setName,newSet);
           }
        }
      }
      catch(Exception ex) {
         FacadeManager.handleException(ex);
      }
  } // End static initializer

  public static EntityTypeSet[] getEntityTypeSets() {
    ArrayList sets=new ArrayList();
    Set keySet=nameToEntityTypeSet.keySet();
    for (Iterator it=keySet.iterator();it.hasNext();) {
      sets.add(nameToEntityTypeSet.get(it.next()));
    }
    return (EntityTypeSet[])sets.toArray(new EntityTypeSet[0]);
  }

  public static String[] getEntityTypeSetNames() {
    ArrayList names=new ArrayList();
    Set keySet=nameToEntityTypeSet.keySet();
    for (Iterator it=keySet.iterator();it.hasNext();) {
      names.add(it.next());
    }
    return (String[])names.toArray(new String[0]);
  }

  public static EntityTypeSet getEntityTypeSet(String setName) {
    return (EntityTypeSet)nameToEntityTypeSet.get(setName);
  }

  //private ArrayList entityTypes=new ArrayList();
  private Set entityTypes = new HashSet();
  private String name;

  public EntityTypeSet(){};


  public EntityTypeSet(String setName, EntityType[] entityTypes) {
     for (int i=0;i<entityTypes.length;i++) {
        this.entityTypes.add(entityTypes[i]);
     }
     nameToEntityTypeSet.put(setName,this);
     name=setName;
  }

  /**
   * Creates a more temporary EntityTypeSet that cannot be retrieved by
   * name at a later date.
   */
  public EntityTypeSet(EntityType[] entityTypes) {
     for (int i=0;i<entityTypes.length;i++) {
        this.entityTypes.add(entityTypes[i]);
     }
     name="";
  }


  public int size() {
    return entityTypes.size();
  }

  public Iterator iterator() {
    return entityTypes.iterator();
  }

  public String toString() {
    return "Entity Type Set: "+name;
  }

  public EntityType[] getEntityTypes() {
     return (EntityType[]) entityTypes.toArray(new EntityType[0]);
  }

  public boolean equals(Object other)
  {
    if ((other != null) && (other instanceof EntityTypeSet))
    {
      EntityTypeSet otherSet = (EntityTypeSet)other;
      return (this.entityTypes.equals(otherSet.entityTypes));
    }
    else
    {
      return false;
    }
  }

  public boolean add(Object o) {
    if (!(o instanceof EntityType)) throw new IllegalArgumentException("Entity Type Set can only contain EntityTypes");
    name="";
    return entityTypes.add(o);
  }


}