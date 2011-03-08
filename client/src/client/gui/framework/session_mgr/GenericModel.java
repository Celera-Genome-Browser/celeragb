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

package client.gui.framework.session_mgr;

import shared.util.EmptyIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

/**
* The GenericModel is a generic observer model for the components of the browser.
* Changes to any of these elements will broadcast events notifing all listeners of
* the change.
*
* Initially written by: Todd Safford
*/

public abstract class GenericModel {
  protected ArrayList modelListeners=new ArrayList();
  protected TreeMap modelProperties;

  public GenericModel(){
      modelProperties= new TreeMap();
      modelListeners = new ArrayList();
  }  //Constructor can only be called within the package



  void addModelListener(GenericModelListener modelListener) {
    if (!modelListeners.contains(modelListener)) modelListeners.add(modelListener);
  }

  void removeModelListener(GenericModelListener modelListener) {
    modelListeners.remove(modelListener);
  }

  /**
  @return The previous value of the this key or null
  */
  public Object setModelProperty(Object key, Object newValue) {
     if (modelProperties==null) modelProperties=new TreeMap();
     Object oldValue = modelProperties.put(key,newValue);
     fireModelPropertyChangeEvent(key, oldValue, newValue);
     return oldValue;
  }

  public Object getModelProperty (Object key) {
    if (modelProperties==null) return null;
    return modelProperties.get(key);
  }

  int sizeofProperties() {
    if (modelProperties.isEmpty()==false) return modelProperties.size();
    else return 0;
  }

  public Iterator getModelPropertyKeys() {
    if (modelProperties==null) return new EmptyIterator();
    return modelProperties.keySet().iterator();
  }

  protected TreeMap getModelProperties() { return modelProperties; }

  protected void setModelProperties(TreeMap modelProperties) {
    this.modelProperties = modelProperties;
  }

  private void fireModelPropertyChangeEvent(Object key, Object oldValue, Object newValue) {
        GenericModelListener modelListener;
        for (int i=0; i < modelListeners.size(); i++) {
          modelListener=(GenericModelListener)modelListeners.get(i);
          modelListener.modelPropertyChanged(key, oldValue, newValue);
        }
  }

}
