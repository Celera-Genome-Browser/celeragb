// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package client.gui.framework.inspector;

import api.entity_model.access.command.DoModifyProperty;
import api.entity_model.access.comparator.GenomicPropertyComparator;
import api.entity_model.access.observer.GenomicEntityObserver;
import api.entity_model.access.observer.GenomicEntityObserverAdapter;
import api.entity_model.access.observer.LoadRequestStatusObserverAdapter;
import api.entity_model.management.ControlledVocabularyMgr;
import api.entity_model.management.PropertyMgr;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.fundtype.LoadRequestState;
import api.entity_model.model.fundtype.LoadRequestStatus;
import api.facade.abstract_facade.annotations.FeatureFacade;
import api.stub.data.ControlledVocabulary;
import api.stub.data.GenomicProperty;

import javax.swing.table.AbstractTableModel;
import java.util.*;


public class PropertiesTableModel extends AbstractTableModel {
  GenomicProperty[] properties;
  GenomicProperty parentProperty = null;
  GenomicEntity entity;
  static final String[] COLUMN_NAMES = new String[]{"Property","Value"};
  GenomicEntityObserver detailsObserver;
  ControlledVocabularyMgr vocabMgr= ControlledVocabularyMgr.getMgr();
  private boolean loadingProperties;
  private boolean isScratch;
  LoadRequestStatus lrs;
  LRSObserver lrsObserver=new LRSObserver();
  private boolean curationState=true;
  private static Comparator genomicPropertyComparator=new GenomicPropertyComparator();

  PropertiesTableModel () {
    detailsObserver=new DetailsObserver(this);
  }

  void setGenomicEntity(GenomicEntity entity) {
    properties=null;
    fireTableStructureChanged();
    if (this.entity != entity) {
      if (this.entity!=null) this.entity.removeGenomicEntityObserver(detailsObserver);
      this.entity=entity;
      if (this.entity != null) {
        entity.addGenomicEntityObserver(detailsObserver,true);
        isScratch=entity.isWorkspace();
      }
      else {
         setProperties(null);
         return;
      }
    }
    if (entity==null) return;
    try {
        lrs=entity.loadPropertiesBackground();
        if (lrs.getLoadRequestState().equals(LoadRequestStatus.LOADED) || lrs.getLoadRequestState().equals(LoadRequestStatus.COMPLETE)) {
           loadingProperties=false;
           setProperties (entity);
        }
        else {
          loadingProperties=true;
          lrs.addLoadRequestStatusObserver(lrsObserver,true, true);
          this.fireTableStructureChanged();
        }
    }
    catch (Exception ex) {ex.printStackTrace();} //Allow continuation will null properties

  }

  private void setProperties(GenomicEntity entity) {
    GenomicProperty[] properties=null;
    if (entity==null) {
        fireTableStructureChanged();
        fireTableDataChanged();
        return;
    }
    Set propertySet;
    if (parentProperty==null) {
      propertySet=entity.getLoadedProperties(genomicPropertyComparator);
      TreeMap orderedMap = new TreeMap();
      List unorderedProps = new ArrayList();
      List finalList = new ArrayList();
      for (Iterator it = propertySet.iterator(); it.hasNext();) {
        GenomicProperty tmpProperty = (GenomicProperty) it.next();
        if (tmpProperty!=null && tmpProperty.getInitialValue()!=null) {
          if (!tmpProperty.getInitialValue().equals("")) {
            GenomicProperty propertyItem = (GenomicProperty)tmpProperty.clone();

            Integer order = PropertyMgr.getPropertyMgr().getPropertyOrderValue(propertyItem.getName());
            if (order.intValue()<=0) unorderedProps.add(propertyItem);
            else orderedMap.put(order, propertyItem);
            String propertyName = propertyItem.getName();
            String propertyValue = propertyItem.getInitialValue();
            //System.out.println("Name: "+propertyName+" , Value: "+propertyValue);
            if (propertyName.equalsIgnoreCase(FeatureFacade.REVIEWED_BY_PROP) ||
                propertyName.equalsIgnoreCase(FeatureFacade.CURATED_BY_PROP) ||
                propertyName.equalsIgnoreCase(FeatureFacade.CREATED_BY_PROP)) {
                propertyItem.setInitialValue(propertyValue.toLowerCase());
            }
          }
        }
        // Debug code below.
//        else {
//          System.out.println("Throwing out: "+tmpProperty.getName());
//          System.out.println("       value: "+tmpProperty.getInitialValue());
//        }
      }
      for (Iterator it = orderedMap.keySet().iterator(); it.hasNext();) {
        finalList.add(orderedMap.get(it.next()));
      }
      for (int x = 0; x < unorderedProps.size(); x++) {
        finalList.add(unorderedProps.get(x));
      }
      properties=(GenomicProperty[])finalList.toArray(new GenomicProperty[finalList.size()]);
    }
    else {
      parentProperty=entity.getProperty(parentProperty.getName());
      properties=parentProperty.getSubProperties();
      if (properties!=null) Arrays.sort(properties, genomicPropertyComparator);
    }
    if ((this.properties==null&&properties!=null)||(this.properties!=null&&properties==null)) {
        this.properties=properties;
        fireTableStructureChanged();
     }
     else this.properties=properties;
     fireTableDataChanged();
  }

  GenomicEntity getGenomicEntity() {
    return entity;
  }

  /**
   * It would appear that this method is used only when there is a child
   * Inspector created.
   */
  void setGenomicEntityAndProperty (GenomicEntity entity, GenomicProperty property) {
    parentProperty=property;
    isScratch=entity.isWorkspace();
    if (this.entity!=null) this.entity.removeGenomicEntityObserver(detailsObserver);
    this.entity=entity;
    entity.addGenomicEntityObserver(detailsObserver,true);
    GenomicProperty[] properties=null;
    try {
      properties = property.getSubProperties();
      Arrays.sort(properties, genomicPropertyComparator);
    }
    catch (Exception ex) {} //Do nothing here!! - Allow continuation will null properties
    if ((this.properties==null&&properties!=null)||(this.properties!=null&&properties==null)) {
        this.properties=properties;
        fireTableStructureChanged();
     }
     else this.properties=properties;
     fireTableDataChanged();
  }

  public void setValueAt(Object aValue, int row, int column) {
    try {
        if (aValue==null) return;
        if (aValue.equals(properties[row].getInitialValue())) return;
        if (properties[row].getEditable()) {
          if (vocabMgr.isNullVocabIndex(properties[row].getVocabIndex()))
            api.entity_model.management.ModifyManager.getModifyMgr().doCommand(
              new DoModifyProperty(entity,properties[row].getName(),properties[row].getInitialValue(),aValue.toString()));
          else  {
            String newValue=vocabMgr.getControlledVocabulary(getGenomicEntity().getOid(), properties[row].getVocabIndex()).reverseLookup(aValue.toString());
            api.entity_model.management.ModifyManager.getModifyMgr().doCommand(
              new DoModifyProperty(entity,properties[row].getName(),properties[row].getInitialValue(),newValue));
          }
        }
      }
      catch (Exception ex) {
         ex.printStackTrace();
      }
  }

  public Object getValueAt(int row,int column) {

    if (column==0) {
      String nativeName = properties[row].getName();
      return PropertyMgr.getPropertyMgr().getPropertyDisplayName(nativeName);
    }

    if ((properties[row].getInitialValue()!=null) && (properties[row].getInitialValue().startsWith("http://")))
         return new URLProperty(properties[row]);
    if ((properties[row].getEditingClass()!=null) && (!(properties[row]).getEditingClass().equals(""))                                                                                        )
         return new ExternallyEditedProperty(properties[row]);
    if (! vocabMgr.isNullVocabIndex(properties[row].getVocabIndex())) {
         ControlledVocabulary tmpVocab = vocabMgr.getControlledVocabulary(getGenomicEntity().getOid(),properties[row].getVocabIndex());
         if (!properties[row].getEditable()) return tmpVocab.lookup(properties[row].getInitialValue());
         else return new SelectableProperty(properties[row],tmpVocab);
    }
    if (properties[row].getExpandable())
         return new ExpandableProperty(properties[row]);
    return properties[row];
  }

  public String getStringValueAt(int row, int column) {
     if (column==0) return properties[row].getName();
     return properties[row].getInitialValue();
  }

  public int getRowCount() {
    if (properties==null) return 0;
    else return properties.length;
  }

  public int getColumnCount() {
    if (properties==null) return 1;
    else return 2;
  }

  public void setCurationState (boolean state) {
    this.curationState = state;
  }

  public String getColumnName(int index) {
    if (properties==null) {
      if (this.loadingProperties) {
        return "Loading Properties";
      }
      else if (entity==null) {
        return "     ";
      }
      else {
        return "No Properties Found";
      }
    }
    else return COLUMN_NAMES[index];
  }


  /**
   *  Overriding isCellEditable() to return true for those property values that
   *  are specified as editable, false otherwise
   *  (names of properties are not editable, but values may be if property's editable
   *  field is set to true)
   */
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    if (columnIndex == 1) {
       if (entity.getGenomeVersion().isReadOnly())
        return properties[rowIndex].getExpandable() ||
          !properties[rowIndex].getEditingClass().equals("") ||
          properties[rowIndex].getInitialValue().startsWith("http://");
       else {
          if (properties[rowIndex].getExpandable() ||!properties[rowIndex].getEditingClass().equals("")
              ||properties[rowIndex].getInitialValue().startsWith("http://")) return true;
          else return
              (isScratch&&curationState&&
              (properties[rowIndex].getEditable()||
              !properties[rowIndex].getEditingClass().equals("")
          ));
       }

    }
    return false;
  }


  class DetailsObserver extends GenomicEntityObserverAdapter {
    PropertiesTableModel model;
    DetailsObserver (PropertiesTableModel model) {
      this.model=model;
    }

    public void noteEntityDetailsChanged(GenomicEntity entity, boolean initialLoad){
      if (entity.equals(model.entity)) {
          setProperties(entity);
      }
    }
  }

  class LRSObserver extends LoadRequestStatusObserverAdapter {
     public void stateChanged(LoadRequestStatus loadRequestStatus, LoadRequestState newState){
          if (newState.equals(LoadRequestStatus.LOADED) || newState.equals(LoadRequestStatus.COMPLETE)) {
           loadingProperties=false;
           loadRequestStatus.removeLoadRequestStatusObserver(this);
           fireTableStructureChanged();
           fireTableDataChanged();
         }
     }
  }
}


