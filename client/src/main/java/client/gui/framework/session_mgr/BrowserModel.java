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

import api.entity_model.access.observer.GenomeVersionObserverAdapter;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.genetics.GenomeVersion;
import api.stub.geometry.MutableRange;
import api.stub.geometry.Range;

import java.util.ArrayList;
import java.util.List;

/**
* The BrowserModel is a model of the views of the browser.  It handles
* Implicit and Explicit selection, Visible range and the scaffold path.
* Changes to any of these elements will broadcast events notifing all listeners of
* the change.
*
* Initially written by: Peter Davies
*/

public class BrowserModel extends GenericModel implements Cloneable {
  public static final String REV_COMP_PROPERTY = "GAAnnotRevComped";
  public static final String DISPLAY_FILTER_PROPERTY = "DISPLAYFILTER";


  private GenomicEntity selection;
  private GenomicEntity masterEditorEntity;
  //  This is the entire range the SubViews are working with.
  private Range subViewFixedRange= new Range();
  //  This is only the current visible portion of the fixed range.
  private Range subViewVisibleRange = new Range();
  private Range masterEditorSelectedRange=new Range();
  private MyGenomeVersionObserver genomeVersionObserver = new MyGenomeVersionObserver();
  private int loadingLimit = 1000000; // 1MB limit by default

  public BrowserModel(){
    String loadingLimitString=System.getProperty("x.genomebrowser.SingleRequestLoadingLimit");
    if (loadingLimitString!=null) {
        try {
          loadingLimit=Integer.parseInt(loadingLimitString);
        }
        catch (Exception ex) {  //do nothing, use default
        }
    }

  }  //Constructor can only be called within the package --PED 5/13

  public GenomicEntity getCurrentSelection() { return selection; }

    public void setCurrentSelection(GenomicEntity newSelection) {
        if (newSelection!=null && newSelection.equals(selection)) return;

        GenomeVersion genomeVersion = (selection == null) ? null : selection.getGenomeVersion();
        GenomeVersion newGenomeVersion = (newSelection == null) ? null : newSelection.getGenomeVersion();
        if (newGenomeVersion != genomeVersion) {
            if (genomeVersion != null)
                genomeVersion.removeGenomeVersionObserver(genomeVersionObserver);
            if (newGenomeVersion != null)
                newGenomeVersion.addGenomeVersionObserver(genomeVersionObserver, false);
        }

        selection = newSelection;
        fireSelectionChangeEvent();
    }

  public void reset() {
     setMasterEditorEntity(null);
     setMasterEditorSelectedRange(new Range());
     setSubViewFixedRange(new Range());
     setSubViewVisibleRange(new Range());
     setCurrentSelection(null);
  }

  public GenomicEntity getMasterEditorEntity() {return masterEditorEntity;}
  public void setMasterEditorEntity(GenomicEntity masterEditorEntity) {
     if (this.masterEditorEntity!=null && this.masterEditorEntity.equals(masterEditorEntity)) return;
     this.masterEditorEntity=masterEditorEntity;
     setMasterEditorSelectedRange(new Range()); //reset the range on the new MasterAxis
     // Clear these out for the new axis and views.
     setSubViewFixedRange(new Range());
     setSubViewVisibleRange(new Range());
     fireMasterEditorEntityChangeEvent();
  }




  public Range getMasterEditorSelectedRange () {
    return masterEditorSelectedRange;
  }

  public void getMasterEditorSelectedRange (MutableRange mutableRange) {
    mutableRange.change(masterEditorSelectedRange);
  }


  public void setMasterEditorSelectedRange(Range masterEditorSelectedRange) {
    if (this.masterEditorSelectedRange.equals(masterEditorSelectedRange)) return;
    if (masterEditorSelectedRange instanceof MutableRange) {
       this.masterEditorSelectedRange=((MutableRange)masterEditorSelectedRange).toRange();
    }
    else {
       this.masterEditorSelectedRange=masterEditorSelectedRange;
    }

    // Check if the subview has been set yet.  If no, set it.
    if (subViewFixedRange == null || subViewFixedRange.getMagnitude()==0) {
        // Must take rev-comp into account.
        MutableRange tmpRange = new MutableRange(masterEditorSelectedRange);
        Boolean isRevComp = (Boolean)getModelProperty(BrowserModel.REV_COMP_PROPERTY);
        if (isRevComp == null)
            throw new IllegalStateException("Model property GAAnnotRevComped not found.");
        if (isRevComp.booleanValue())
            tmpRange.mirror(((api.entity_model.model.assembly.GenomicAxis)getMasterEditorEntity()).getMagnitude());
        setSubViewFixedRange(tmpRange);
    }

    fireMasterEditorRangeChangeEvent();
  }



  /**
   * These methods get and set the SubView fixed range which is the entire range
   * that the SubViews work with.  It sets the bounds.
   */
  public Range getSubViewFixedRange() {
    return subViewFixedRange;
  }

  public void setSubViewFixedRange(Range subViewFixedRange) {
    if (this.subViewFixedRange.equals(subViewFixedRange)) return;
    if (subViewFixedRange instanceof MutableRange) {
       this.subViewFixedRange=((MutableRange)subViewFixedRange).toRange();
    }
    else {
       this.subViewFixedRange=subViewFixedRange;
    }
    // Truncate the range to be at least less than loadingLimit.
    if (this.subViewFixedRange.getMagnitude() > loadingLimit) {
      this.subViewFixedRange = (new Range(this.subViewFixedRange.getStart(), loadingLimit,
        this.subViewFixedRange.getOrientation())).toMutableRange();
    }

    fireSubViewFixedRangeChangeEvent();
  }


  /**
   * These methods get and set the SubView visible range.  That range is the current
   * visible region that a SubView is working with.  It roams within the fixed range.
   */
  public Range getSubViewVisibleRange() {
    return subViewVisibleRange;
  }

  public void setSubViewVisibleRange(Range subViewVisibleRange) {
    if (this.subViewVisibleRange.equals(subViewVisibleRange)) return;
    if (subViewVisibleRange instanceof MutableRange) {
       this.subViewVisibleRange=((MutableRange)subViewVisibleRange).toRange();
    }
    else {
       this.subViewVisibleRange=subViewVisibleRange;
    }
    fireSubViewVisibleRangeChangeEvent();
  }

  public void addBrowserModelListener(BrowserModelListener browserModelListener) {
    addBrowserModelListener(browserModelListener,true);
  }

  public void addBrowserModelListener(BrowserModelListener browserModelListener,boolean bringUpToDate) {
    modelListeners.add(browserModelListener);
    if (bringUpToDate) {
      browserModelListener.browserMasterEditorEntityChanged(masterEditorEntity);
      browserModelListener.browserMasterEditorSelectedRangeChanged(masterEditorSelectedRange);
      browserModelListener.browserSubViewFixedRangeChanged(subViewFixedRange);
      browserModelListener.browserSubViewVisibleRangeChanged(subViewVisibleRange);
      browserModelListener.browserCurrentSelectionChanged(selection);
    }
  }

  public void removeBrowserModelListener(BrowserModelListener browserModelListener) {
    modelListeners.remove(browserModelListener);
  }


  public void dispose() {
    fireBrowserClosing();
  }


  public Object clone() {
     BrowserModel browserModel=new BrowserModel();
     browserModel.selection = selection;
     browserModel.masterEditorEntity = masterEditorEntity;
     browserModel.subViewFixedRange = subViewFixedRange;
     browserModel.subViewVisibleRange = subViewVisibleRange;
     //shallow copy of the ranges should be OK as they are forced immutable
     browserModel.masterEditorSelectedRange = masterEditorSelectedRange;
     browserModel.modelListeners=new ArrayList(); //Trash the listener list of the clone
     return browserModel;
   }

  private void fireMasterEditorRangeChangeEvent() {
        BrowserModelListener browserModelListener;
        List listeners=(List)modelListeners.clone();
        for (int i=0; i < listeners.size(); i++) {
          browserModelListener=(BrowserModelListener)listeners.get(i);
          browserModelListener.browserMasterEditorSelectedRangeChanged(masterEditorSelectedRange);
        }
  }

  private void fireMasterEditorEntityChangeEvent() {
        BrowserModelListener browserModelListener;
        List listeners=(List)modelListeners.clone();
        for (int i=0; i < listeners.size(); i++) {
          browserModelListener=(BrowserModelListener)listeners.get(i);
          browserModelListener.browserMasterEditorEntityChanged(masterEditorEntity);
        }
  }





  private void fireSubViewFixedRangeChangeEvent() {
        BrowserModelListener browserModelListener;
        List listeners=(List)modelListeners.clone();
        for (int i=0; i < listeners.size(); i++) {
            browserModelListener=(BrowserModelListener)listeners.get(i);
            browserModelListener.browserSubViewFixedRangeChanged(subViewFixedRange);
        }
  }

  private void fireSubViewVisibleRangeChangeEvent() {
        BrowserModelListener browserModelListener;
        List listeners=(List)modelListeners.clone();
        for (int i=0; i < listeners.size(); i++) {
            browserModelListener=(BrowserModelListener)listeners.get(i);
            browserModelListener.browserSubViewVisibleRangeChanged(subViewVisibleRange);
        }
  }

  private void fireSelectionChangeEvent() {
        BrowserModelListener browserModelListener;
        List listeners=(List)modelListeners.clone();
        for (int i=0; i < listeners.size(); i++) {
          browserModelListener=(BrowserModelListener)listeners.get(i);
          browserModelListener.browserCurrentSelectionChanged(selection);
        }
  }

  private void fireBrowserClosing() {
        List listeners=(List)modelListeners.clone();
        for (int i=0; i < listeners.size(); i++) {
            ((BrowserModelListener)listeners.get(i)).browserClosing();
        }
  }


    /**
     * Inner class to observer the genome version.
     * Need to affect selection if the selected item is removed from the genome version.
     */
    class MyGenomeVersionObserver extends GenomeVersionObserverAdapter
    {
        public void noteEntityRemovedFromGenomeVersion(GenomicEntity entity) {
            if (selection == entity)
                setCurrentSelection(null);
        }
    }

}
