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

/**
 * Title:        Your Product Name<p>
 * Description:  This is the main Browser in the System<p>
 * @author Peter Davies
 * @version
 */
package client.gui.components.annotation.axis_annotation;

import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import client.gui.framework.session_mgr.*;
import api.stub.geometry.Range;
import api.entity_model.model.fundtype.*;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.annotation.*;
import api.entity_model.model.alignment.GeometricAlignment;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.access.observer.*;

public class OptionsMenu extends JMenu {
  private GenomicAxisAnnotationView gaView;
  private JCheckBoxMenuItem highlightEvidenceMI= new JCheckBoxMenuItem("Highlight Evidence");
  private JCheckBoxMenuItem spliceMismatchMI= new JCheckBoxMenuItem("Show non-GT/AG Splice Matches");


  private JCheckBoxMenuItem showEdgeMatchesMI= new JCheckBoxMenuItem("Show Edge Matches");
  private JCheckBoxMenuItem showEvidenceEdgeMisMatchesMI= new JCheckBoxMenuItem("Show Evidence Edge Mismatches");

  private JCheckBoxMenuItem curationEnabledMI= new JCheckBoxMenuItem("Enable Curation");
  private JCheckBoxMenuItem showSearchMI= new JCheckBoxMenuItem("Zoom To Axis Position");
  private JMenuItem zoomToSelectedRegion= new JMenuItem("Zoom To Selected Region");
  private JCheckBoxMenuItem doRevCompMI= new JCheckBoxMenuItem("Reverse Complement Axis");
  private JMenuItem zoomToSelectedMI=new JMenuItem("Zoom To Selection");
  private JCheckBoxMenuItem zoomToSubviewSelectionMI = new JCheckBoxMenuItem("Zoom To SubView Selection");
  private JCheckBoxMenuItem lockToSelectedMI= new JCheckBoxMenuItem("Lock To Selection");
  private JMenuItem selectVisibleRegionMI= new JMenuItem("Select Visible Region");


  public OptionsMenu(GenomicAxisAnnotationView genomicAxisAnnotationView) {
    this.gaView=genomicAxisAnnotationView;
    SessionMgr.getSessionMgr().addSessionModelListener(new MySessionPropertyListener());
    gaView.getBrowserModel().addBrowserModelListener(new MyBrowserModelListener());

    setText("Options");
    setMnemonic('O');

    zoomToSelectedMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, InputEvent.CTRL_MASK, false));
    zoomToSelectedMI.addActionListener( new ActionListener () {
      public void actionPerformed(ActionEvent e) {
        gaView.zoomToSelection();
      }
    });

    selectVisibleRegionMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_MASK, false));
    selectVisibleRegionMI.addActionListener( new ActionListener () {
      public void actionPerformed(ActionEvent e) {
        gaView.selectVisibleRegion();
      }
    });


    highlightEvidenceMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK, false));
    highlightEvidenceMI.addActionListener( new ActionListener () {
      public void actionPerformed(ActionEvent e) {
        if (highlightEvidenceMI.isSelected()!=((Boolean)SessionMgr.getSessionMgr().
          getModelProperty("HighlightEvidenceProperty")).booleanValue()) {
            SessionMgr.getSessionMgr().setModelProperty("HighlightEvidenceProperty",
              new Boolean(highlightEvidenceMI.isSelected()));
        }
        if (highlightEvidenceMI.isSelected()) loadEvidenceIfNecessary();
      }
    });

    spliceMismatchMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK, false));
    spliceMismatchMI.addActionListener( new ActionListener () {
      public void actionPerformed(ActionEvent e) {
        if (spliceMismatchMI.isSelected()!=((Boolean)SessionMgr.getSessionMgr().
          getModelProperty("SpliceMismatchProperty")).booleanValue()) {
            SessionMgr.getSessionMgr().setModelProperty("SpliceMismatchProperty",
              new Boolean(spliceMismatchMI.isSelected()));
        }
      }
    });

    showEdgeMatchesMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK, false));
    showEdgeMatchesMI.addActionListener( new ActionListener () {
      public void actionPerformed(ActionEvent e) {
        if (showEdgeMatchesMI.isSelected()!=((Boolean)SessionMgr.getSessionMgr().
          getModelProperty("ShowEdgeMatchesProperty")).booleanValue()) {
            SessionMgr.getSessionMgr().setModelProperty("ShowEdgeMatchesProperty",
              new Boolean(showEdgeMatchesMI.isSelected()));
            if (showEdgeMatchesMI.isSelected()) {
              showEvidenceEdgeMisMatchesMI.setSelected(false);
              SessionMgr.getSessionMgr().setModelProperty("ShowEvidenceEdgeMismatches",
                new Boolean(false));
            }
        }
      }
    });

    showEvidenceEdgeMisMatchesMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK, false));
    showEvidenceEdgeMisMatchesMI.addActionListener( new ActionListener () {
      public void actionPerformed(ActionEvent e) {
        if (showEvidenceEdgeMisMatchesMI.isSelected()!=((Boolean)SessionMgr.getSessionMgr().
          getModelProperty("ShowEvidenceEdgeMismatches")).booleanValue()) {
            SessionMgr.getSessionMgr().setModelProperty("ShowEvidenceEdgeMismatches",
              new Boolean(showEvidenceEdgeMisMatchesMI.isSelected()));
            if (showEvidenceEdgeMisMatchesMI.isSelected()) {
              showEdgeMatchesMI.setSelected(false);
              SessionMgr.getSessionMgr().setModelProperty("ShowEdgeMatchesProperty",
               new Boolean(false));
            }
        }
        if (showEvidenceEdgeMisMatchesMI.isSelected()) loadEvidenceIfNecessary();
      }
    });

    curationEnabledMI.addActionListener( new ActionListener () {
      public void actionPerformed(ActionEvent e) {
        if (curationEnabledMI.isSelected()!=((Boolean)SessionMgr.getSessionMgr().
          getModelProperty("CurationEnabled")).booleanValue()) {
            SessionMgr.getSessionMgr().setModelProperty("CurationEnabled",
              new Boolean(curationEnabledMI.isSelected()));
        }
      }
    });

    zoomToSubviewSelectionMI.addActionListener( new ActionListener () {
      public void actionPerformed(ActionEvent e) {
        if (zoomToSubviewSelectionMI.isSelected()!=((Boolean)SessionMgr.getSessionMgr().
          getModelProperty("ZoomToSubviewSelection")).booleanValue()) {
            SessionMgr.getSessionMgr().setModelProperty("ZoomToSubviewSelection",
              new Boolean(zoomToSubviewSelectionMI.isSelected()));
        }
      }
    });

    showSearchMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK, false));
    showSearchMI.addActionListener( new ActionListener () {
       public void actionPerformed(ActionEvent e) {
            showSearchMI.setState(gaView.showSearch(showSearchMI.getState()));
       }
    });

    doRevCompMI.addActionListener( new ActionListener () {
       public void actionPerformed(ActionEvent e) {
            doRevCompMI.setState(gaView.reverseComplementAxis(doRevCompMI.getState()));
       }
    });

    lockToSelectedMI.addActionListener( new ActionListener () {
       public void actionPerformed(ActionEvent e) {
            lockToSelectedMI.setState(gaView.lockToSelection(lockToSelectedMI.getState()));
       }
    });

    zoomToSelectedRegion.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, InputEvent.CTRL_MASK, false));
    zoomToSelectedRegion.addActionListener( new ActionListener () {
       public void actionPerformed(ActionEvent e) {
            gaView.zoomToSelectedRegion();
       }
    });

    zoomToSelectedRegion.setEnabled(false);
    zoomToSelectedMI.setEnabled(false);
    add(zoomToSelectedMI);
    add(zoomToSelectedRegion);
    add(showSearchMI);
    add(zoomToSubviewSelectionMI);
    add(selectVisibleRegionMI);
    add(lockToSelectedMI);
    add(new JSeparator());
    add(highlightEvidenceMI);
    add(doRevCompMI);
    add(showEdgeMatchesMI);
    add(showEvidenceEdgeMisMatchesMI);
    add(spliceMismatchMI);
    add(curationEnabledMI);

    if (SessionMgr.getSessionMgr().getModelProperty("HighlightEvidenceProperty")!=null)
      highlightEvidenceMI.setSelected(((Boolean)SessionMgr.getSessionMgr().getModelProperty("HighlightEvidenceProperty")).booleanValue());
    else SessionMgr.getSessionMgr().setModelProperty("HighlightEvidenceProperty", Boolean.FALSE);
    if (SessionMgr.getSessionMgr().getModelProperty("ShowEdgeMatchesProperty")!=null)
      showEdgeMatchesMI.setSelected(((Boolean)SessionMgr.getSessionMgr().getModelProperty("ShowEdgeMatchesProperty")).booleanValue());
    else SessionMgr.getSessionMgr().setModelProperty("ShowEdgeMatchesProperty", Boolean.FALSE);
    if (SessionMgr.getSessionMgr().getModelProperty("ShowEvidenceEdgeMismatches")!=null)
      showEvidenceEdgeMisMatchesMI.setSelected(((Boolean)SessionMgr.getSessionMgr().getModelProperty("ShowEvidenceEdgeMismatches")).booleanValue());
    else SessionMgr.getSessionMgr().setModelProperty("ShowEvidenceEdgeMismatches", Boolean.FALSE);
    if (SessionMgr.getSessionMgr().getModelProperty("SpliceMismatchProperty")!=null)
      spliceMismatchMI.setSelected(((Boolean)SessionMgr.getSessionMgr().getModelProperty("SpliceMismatchProperty")).booleanValue());
    else SessionMgr.getSessionMgr().setModelProperty("SpliceMismatchProperty", Boolean.FALSE);
    if (SessionMgr.getSessionMgr().getModelProperty("CurationEnabled")!=null)
      curationEnabledMI.setSelected(((Boolean)SessionMgr.getSessionMgr().getModelProperty("CurationEnabled")).booleanValue());
    else SessionMgr.getSessionMgr().setModelProperty("CurationEnabled", Boolean.TRUE);
    if (SessionMgr.getSessionMgr().getModelProperty("ZoomToSubviewSelection")!=null)
      zoomToSubviewSelectionMI.setSelected(((Boolean)SessionMgr.getSessionMgr().getModelProperty("ZoomToSubviewSelection")).booleanValue());
    else SessionMgr.getSessionMgr().setModelProperty("ZoomToSubviewSelection", Boolean.FALSE);

    disableLockToSelection();

  }

    public void disableLockToSelection() {
        if (!lockToSelectedMI.getState())
            lockToSelectedMI.setEnabled(false);
    }

    public void enableLockToSelection() {
        lockToSelectedMI.setEnabled(true);
    }


  void resetMenus() {
    lockToSelectedMI.setState(false);
  }

  boolean getZoomToSubviewSelectionState() { return zoomToSubviewSelectionMI.getState(); }


  private class MyBrowserModelListener extends BrowserModelListenerAdapter {
    public void browserMasterEditorSelectedRangeChanged(Range masterEditorSelectedRange) {
      if (masterEditorSelectedRange !=null && masterEditorSelectedRange.getMagnitude()>50) {
          zoomToSelectedRegion.setEnabled(true);
       }
       else zoomToSelectedRegion.setEnabled(false);
    }

    public void browserCurrentSelectionChanged(GenomicEntity newSelection) {
      loadEvidenceIfNecessary();
      if (newSelection ==null) zoomToSelectedMI.setEnabled(false);
      else zoomToSelectedMI.setEnabled(true);
    }
  }


  private void loadEvidenceIfNecessary() {
    BrowserModel browserModel=SessionMgr.getSessionMgr().getActiveBrowser().getBrowserModel();

    GenomicEntity tmpEntity = browserModel.getCurrentSelection();
    if (null!=tmpEntity && tmpEntity instanceof CuratedFeature && !(tmpEntity instanceof CuratedCodon)) {
      GenomicAxis axis = (GenomicAxis)browserModel.getMasterEditorEntity();
      GeometricAlignment tmpAlignment = (GeometricAlignment)((CuratedFeature)tmpEntity).getOnlyAlignmentToAnAxis(axis);
      if (null==tmpAlignment) {
      	System.out.println("Error - loadEvidenceIfNecessary has an entity with null alignment on axis!");
      	return;
      }
      Range tmpRange = tmpAlignment.getRangeOnAxis();

      if (highlightEvidenceMI.isSelected()) {
        LoadFilter hiPriFilter=axis.getHighPriPreComputeLoadFilter();
        LoadRequestStatus status = loadAlignments(axis, tmpRange, hiPriFilter);
        status.addLoadRequestStatusObserver(new LoadStatusObserverForHighlightEvidence(tmpEntity),true);
      }
      if (showEvidenceEdgeMisMatchesMI.isSelected()) {
        LoadFilter hiPriFilter=axis.getHighPriPreComputeLoadFilter();
        LoadRequestStatus status = loadAlignments(axis, tmpRange, hiPriFilter);
        status.addLoadRequestStatusObserver(new LoadStatusObserverForEdgeMisMatch(tmpEntity),true);
      }
    }
  }


  private class LoadStatusObserverForHighlightEvidence extends LoadRequestStatusObserverAdapter {
        GenomicEntity entity;

        public LoadStatusObserverForHighlightEvidence(GenomicEntity tmpEntity) {
            this.entity = tmpEntity;

        }

        public void stateChanged(LoadRequestStatus loadRequestStatus, LoadRequestState newState){
          if (newState == LoadRequestStatus.COMPLETE) {
            gaView.highlightEvidence((Feature)entity);
          }
        }
    } //end LoadStatusObserverForHighlightEvidence

   private class LoadStatusObserverForEdgeMatch extends LoadRequestStatusObserverAdapter {
        GenomicEntity entity;
        public LoadStatusObserverForEdgeMatch(GenomicEntity tmpEntity) {
            this.entity = tmpEntity;

        }

        public void stateChanged(LoadRequestStatus loadRequestStatus, LoadRequestState newState){
          if (newState == LoadRequestStatus.COMPLETE) {
            gaView.showEdgeMatches((Feature)entity);
          }
        }
    } //end LoadStatusObserverForEdgeMatch


    private class LoadStatusObserverForEdgeMisMatch extends LoadRequestStatusObserverAdapter {
        GenomicEntity entity;
        public LoadStatusObserverForEdgeMisMatch(GenomicEntity tmpEntity) {
            this.entity = tmpEntity;

        }

        public void stateChanged(LoadRequestStatus loadRequestStatus, LoadRequestState newState){
          if (newState == LoadRequestStatus.COMPLETE) {
            gaView.showEvidenceEdgeMismatches((Feature)entity);
          }
        }
    } //end LoadStatusObserverForEdgeMatch



  public LoadRequestStatus loadAlignments(Axis axis, Range range, LoadFilter loadFilter) {
     // If the filter is NOT strand specific, we need to always have a forward range...
     if (!loadFilter.isStrandSpecific() && range.isReversed()) {
       range = range.toReverse();
     }
     return axis.loadAlignmentsToEntitiesBackground(new LoadRequest(range,loadFilter,false));

  }

  private class MyStringComparator implements Comparator {
    public int compare(Object key1, Object key2) {
          String keyName1 = (String)key1;
          String keyName2 = (String)key2;
          return keyName1.compareToIgnoreCase(keyName2);
    }
  }

  private class MySessionPropertyListener implements SessionModelListener {
    public void browserAdded(BrowserModel browserModel){}
    public void browserRemoved(BrowserModel browserModel){}
    public void sessionWillExit(){}
    public void modelPropertyChanged(Object key, Object oldValue, Object newValue){
       if (key.equals("HighlightEvidenceProperty")) {
        highlightEvidenceMI.setSelected(((Boolean)newValue).booleanValue());
       }
       else if (key.equals("ShowEdgeMatchesProperty")) {
        showEdgeMatchesMI.setSelected(((Boolean)newValue).booleanValue());
       }
       else if (key.equals("ShowEvidenceEdgeMismatches")) {
        showEvidenceEdgeMisMatchesMI.setSelected(((Boolean)newValue).booleanValue());
       }
       else if (key.equals("SpliceMismatchProperty")) {
        spliceMismatchMI.setSelected(((Boolean)newValue).booleanValue());
       }
       else if (key.equals("CurationEnabled")) {
        curationEnabledMI.setSelected(((Boolean)newValue).booleanValue());
       }
       else if (key.equals("ZoomToSubviewSelection")) {
        zoomToSubviewSelectionMI.setSelected(((Boolean)newValue).booleanValue());
       }
    }
  }
}