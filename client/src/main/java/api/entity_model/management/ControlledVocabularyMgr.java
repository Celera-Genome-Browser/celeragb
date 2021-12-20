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
package api.entity_model.management;


import api.facade.abstract_facade.fundtype.NavigationConstants;
import api.facade.abstract_facade.genetics.ControlledVocabService;
import api.facade.facade_mgr.FacadeManager;
import api.facade.facade_mgr.InUseProtocolListener;
import api.stub.data.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class ControlledVocabularyMgr {

  private ControlledVocabService ctrlVocabServ = null;
  private Map vocabHash=new Hashtable();
  private boolean cacheControlledVocabs = true;
  private final ControlledVocabulary EMPTY_VOCAB = new ControlledVocabulary();
  private InUseProtocolListener protocolListener = new MyInUseProtocolListener();

  private ControlledVocabularyMgr() {
      FacadeManager.addInUseProtocolListener(protocolListener);
  }

  private static ControlledVocabularyMgr staticMgr = new ControlledVocabularyMgr();

  public static ControlledVocabularyMgr getMgr() {
    return staticMgr;
  }

  public static boolean isNullVocabIndex(String vocabIndex) {
    return ControlledVocabUtil.isNullVocabIndex(vocabIndex);
  }

  public ControlledVocabulary getControlledVocabulary(OID entityOID, String vocabIndex)
  {
    // Return a cached one if we have it
    if (this.isCaching()) {
      ControlledVocabulary retVal = (ControlledVocabulary) this.vocabHash.get(vocabIndex);
      if (retVal != null) {
        return retVal;
      }
    }

    // Otherwise get it from the server
    if (! this.checkSetConnection()) {
      System.out.println("Error in ControlledVocabularyMgr.getControlledVocabulary: unable to make server connection");
      return EMPTY_VOCAB;
    }
    // Convert from IDL type to a first-class object
    ControlledVocabElement[] vocabElements=new ControlledVocabElement[0];
    try {
      vocabElements = ctrlVocabServ.getControlledVocab(entityOID, vocabIndex);
      if (vocabElements.length==0) {  //if nothing returned from remote call, get local version
        Map map = ControlledVocabUtil.getControlledVocabulariesFromResource();
        vocabElements=ControlledVocabUtil.getControlledVocab(vocabIndex,map);
      }
    }
    catch (NoData noDataEx) {
      try{
        Map map = ControlledVocabUtil.getControlledVocabulariesFromResource();
        vocabElements=ControlledVocabUtil.getControlledVocab(vocabIndex,map);
      }
      catch (NoData noDataEx1) {
        return EMPTY_VOCAB;
      }
    }

    List orderedNames = new ArrayList(vocabElements.length);
    ControlledVocabulary retVal = new ControlledVocabulary(orderedNames);
    for (int i=0; i<vocabElements.length; i++) {
      orderedNames.add(vocabElements[i].value);
      retVal.addEntry(vocabElements[i].value, vocabElements[i].name);
    }

    // Cache the new ControlledVocabulary
    if (this.isCaching()) {
      this.vocabHash.put(vocabIndex,retVal);
    }

    return retVal;
  }

  public ControlledVocabulary getNavigationVocabulary() {
    // A null entityId is OK to pass in this particular call,
    // but is not suggested for other nav indexes
    return this.getControlledVocabulary(null, NavigationConstants.NAVIGATION_VOCAB_INDEX);
  }

  public void setCaching(boolean doCaching) {
    this.cacheControlledVocabs = doCaching;
  }

  public void cleanCache() {
    this.vocabHash.clear();
  }

  public boolean isCaching() {
    return this.cacheControlledVocabs;
  }

  public void ejectFromCache(String vocabIndex) {
    this.vocabHash.remove(vocabIndex);
  }

  public boolean isCached(String vocabIndex) {
    return this.vocabHash.containsKey(vocabIndex);
  }

  protected boolean checkSetConnection() {
    if (this.ctrlVocabServ == null) {
      try {
        this.ctrlVocabServ =
          api.facade.facade_mgr.FacadeManager.getFacadeManager().getControlledVocabService();
      }
      catch(Exception ex) {
        return false;
      }
    }
    return true;
  }

  private class MyInUseProtocolListener implements InUseProtocolListener {
    public void protocolAddedToInUseList(String protocol){
      cleanCache();
    }
    public void protocolRemovedFromInUseList(String protocol){}
  }
}
