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
 * CVS_ID:  $Id$
 */

package client.gui.components.annotation.axis_annotation;

import api.entity_model.access.observer.LoadRequestStatusObserverAdapter;
import api.entity_model.management.LoadLimitor;
import api.entity_model.management.ModifyManager;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.ActiveThreadModel;
import api.entity_model.model.fundtype.Axis;
import api.entity_model.model.fundtype.GenomicEntity;
import api.entity_model.model.fundtype.LoadFilter;
import api.entity_model.model.fundtype.LoadRequest;
import api.entity_model.model.fundtype.LoadRequestState;
import api.entity_model.model.fundtype.LoadRequestStatus;
import api.stub.geometry.Range;
import client.gui.framework.session_mgr.BrowserModel;
import client.gui.framework.session_mgr.BrowserModelListenerAdapter;
import client.gui.framework.session_mgr.SessionMgr;
import client.gui.other.xml.xml_writer.XMLWriter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

/** This class provides an Annotation Menu Initially writted by: Peter Davies */
public class AnnotationMenu extends JMenu {
    private JMenuItem menuFrequentFeatures;
    private JMenuItem menuAxisHiPri;
    private JMenuItem menuAxisLowPri;
    private JMenuItem menuAxisHuman;
    private JMenuItem computeAxisSpliceForwardMI;
    private JMenuItem computeAxisSpliceReverseMI;
    private JMenuItem computeAxisStopForwardMI;
    private JMenuItem computeAxisStopReverseMI;
    private JMenuItem computeAxisStartForwardMI;
    private JMenuItem computeAxisStartReverseMI;
    private JMenuItem loadContigsMI;
    private BrowserModel model;
    private GenomicAxisAnnotationView view;
    private JMenu computeAxisSpliceSubMenu;
    private JMenu computeAxisStopSubMenu;
    private JMenu computeAxisStartSubMenu;
    private BrowserModelListener browserModelListener = new BrowserModelListener();
    private boolean isLoadMenu;
    private int loadingLimit=1000000; //default to 1Mb
    private LoadLimitor loadLimitor=LoadLimitor.getLoadLimitor();


     public AnnotationMenu(BrowserModel browserModel, GenomicAxisAnnotationView theView, boolean isLoadMenu) {
        this.isLoadMenu=isLoadMenu;
        model = browserModel;
        this.view = theView;

        String loadingLimitString=System.getProperty(
           "x.genomebrowser.SingleRequestLoadingLimit");
        if (loadingLimitString!=null) {
            try {
              loadingLimit=Integer.parseInt(loadingLimitString);
            }
            catch (Exception ex) {  //do nothing, use default
            }
        }

        init();
     }

     private void init() {
        if (isLoadMenu) {
          setText("Load Data");
          this.setMnemonic('L');
        }
        else {
          setText ("Unload Data");
          setMnemonic('U');
        }

        ActiveThreadModel.getActiveThreadModel().addObserver(new MyActiveThreadObserver());

        // Splice site menu
        computeAxisSpliceSubMenu = new JMenu("Predicted Splice Sites");
        computeAxisSpliceForwardMI = new JMenuItem();
        computeAxisSpliceForwardMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkRange(model.getMasterEditorSelectedRange());
                Range masterAxisSelectedRange = model.getMasterEditorSelectedRange();
                if (!masterAxisSelectedRange.isForwardOrientation()) {
                    masterAxisSelectedRange = masterAxisSelectedRange.toReverse();
                }
                if (!AnnotationMenu.this.isLoadMenu || loadLimitor.canLoadSpliceSitesOverRange(masterAxisSelectedRange)) {
                   GenomicAxis genomicAxis =(GenomicAxis)model.getMasterEditorEntity();
                   loadOrUnloadAlignments(genomicAxis, masterAxisSelectedRange, genomicAxis.getLocallyComputedSpliceSiteLoadFilter(true));
                }
                else showMemoryDialog();
            }
        });
        computeAxisSpliceReverseMI = new JMenuItem();
        computeAxisSpliceReverseMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkRange(model.getMasterEditorSelectedRange());
                Range masterAxisSelectedRange = model.getMasterEditorSelectedRange();
                if (masterAxisSelectedRange.isForwardOrientation()) {
                    masterAxisSelectedRange = masterAxisSelectedRange.toReverse();
                }
                if (!AnnotationMenu.this.isLoadMenu || loadLimitor.canLoadSpliceSitesOverRange(masterAxisSelectedRange)) {
                   GenomicAxis genomicAxis =(GenomicAxis)model.getMasterEditorEntity();
                   loadOrUnloadAlignments(genomicAxis, masterAxisSelectedRange, genomicAxis.getLocallyComputedSpliceSiteLoadFilter(false));
                }
                else showMemoryDialog();
            }
        });

        computeAxisSpliceSubMenu.add(computeAxisSpliceForwardMI);
        computeAxisSpliceSubMenu.add(computeAxisSpliceReverseMI);

       // Start codon menu
        computeAxisStartSubMenu = new JMenu("Predicted Start Codons");
        computeAxisStartForwardMI = new JMenuItem();
        computeAxisStartForwardMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkRange(model.getMasterEditorSelectedRange());
                Range masterAxisSelectedRange = model.getMasterEditorSelectedRange();
                if (!masterAxisSelectedRange.isForwardOrientation()) {
                    masterAxisSelectedRange = masterAxisSelectedRange.toReverse();
                }
                if (!AnnotationMenu.this.isLoadMenu || loadLimitor.canLoadCodonsOverRange(masterAxisSelectedRange)) {
                   GenomicAxis genomicAxis =(GenomicAxis)model.getMasterEditorEntity();
                   loadOrUnloadAlignments(genomicAxis,masterAxisSelectedRange, genomicAxis.getLocallyComputedStartCodonLoadFilter(true));
                }
                else showMemoryDialog();
            }
        });
        computeAxisStartReverseMI = new JMenuItem();
        computeAxisStartReverseMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkRange(model.getMasterEditorSelectedRange());
                Range masterAxisSelectedRange = model.getMasterEditorSelectedRange();
                if (masterAxisSelectedRange.isForwardOrientation()) {
                    masterAxisSelectedRange = masterAxisSelectedRange.toReverse();
                }
                if (!AnnotationMenu.this.isLoadMenu || loadLimitor.canLoadCodonsOverRange(masterAxisSelectedRange)) {
                   GenomicAxis genomicAxis =(GenomicAxis)model.getMasterEditorEntity();
                   loadOrUnloadAlignments(genomicAxis,masterAxisSelectedRange, genomicAxis.getLocallyComputedStartCodonLoadFilter(false));
                }
                else showMemoryDialog();
            }
        });

        computeAxisStartSubMenu.add(computeAxisStartForwardMI);
        computeAxisStartSubMenu.add(computeAxisStartReverseMI);

        // Stop codon menu
        computeAxisStopSubMenu = new JMenu("Predicted Stop Codons");
        computeAxisStopForwardMI = new JMenuItem();
        computeAxisStopForwardMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkRange(model.getMasterEditorSelectedRange());
                Range masterAxisSelectedRange = model.getMasterEditorSelectedRange();
                if (!masterAxisSelectedRange.isForwardOrientation()) {
                    masterAxisSelectedRange = masterAxisSelectedRange.toReverse();
                }
                if (!AnnotationMenu.this.isLoadMenu || loadLimitor.canLoadCodonsOverRange(masterAxisSelectedRange)) {
                   GenomicAxis genomicAxis =(GenomicAxis)model.getMasterEditorEntity();
                   loadOrUnloadAlignments(genomicAxis,masterAxisSelectedRange, genomicAxis.getLocallyComputedStopCodonLoadFilter(true));
                }
                else showMemoryDialog();
            }
        });
        computeAxisStopReverseMI = new JMenuItem();
        computeAxisStopReverseMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkRange(model.getMasterEditorSelectedRange());
                Range masterAxisSelectedRange = model.getMasterEditorSelectedRange();
                if (masterAxisSelectedRange.isForwardOrientation()) {
                    masterAxisSelectedRange = masterAxisSelectedRange.toReverse();
                }
                if (!AnnotationMenu.this.isLoadMenu || loadLimitor.canLoadCodonsOverRange(masterAxisSelectedRange)) {
                   GenomicAxis genomicAxis =(GenomicAxis)model.getMasterEditorEntity();
                   loadOrUnloadAlignments(genomicAxis,masterAxisSelectedRange, genomicAxis.getLocallyComputedStopCodonLoadFilter(false));
                }
                else showMemoryDialog();
            }
        });

        computeAxisStopSubMenu.add(computeAxisStopForwardMI);
        computeAxisStopSubMenu.add(computeAxisStopReverseMI);

        loadContigsMI = new JMenuItem("Contigs");
        loadContigsMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                 checkRange(model.getMasterEditorSelectedRange());
                 GenomicAxis ga=(GenomicAxis)model.getMasterEditorEntity();
                 loadOrUnloadAlignments(ga,model.getMasterEditorSelectedRange(), ga.getContigLoadFilter());
            }
        });
        menuAxisHiPri = new JMenuItem("High Pri Computed Features");
        if (isLoadMenu) {
          menuAxisHiPri.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_MASK, false));
        }
        else {
          menuAxisHiPri.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, InputEvent.CTRL_MASK, false));
        }
        menuAxisHiPri.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkRange(model.getMasterEditorSelectedRange());
                if (!AnnotationMenu.this.isLoadMenu || loadLimitor.canLoadHiPriorityOverRange(model.getMasterEditorSelectedRange())) {
                  GenomicAxis ga=(GenomicAxis)model.getMasterEditorEntity();
                  LoadFilter filter=ga.getHighPriPreComputeLoadFilter();
                  loadOrUnloadAlignments(ga,model.getMasterEditorSelectedRange(), filter);
                }
                else showMemoryDialog();
            }
        });
        menuAxisLowPri = new JMenuItem("Low Pri Computed Features");
        if (isLoadMenu) {
          menuAxisLowPri.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, InputEvent.CTRL_MASK, false));
        }
        else {
          menuAxisLowPri.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, InputEvent.CTRL_MASK, false));
        }
        menuAxisLowPri.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                  checkRange(model.getMasterEditorSelectedRange());
                  if (!AnnotationMenu.this.isLoadMenu || loadLimitor.canLoadLoPriorityOverRange(model.getMasterEditorSelectedRange())) {
                    GenomicAxis ga=(GenomicAxis)model.getMasterEditorEntity();
                    LoadFilter filter=ga.getLowPriPreComputeLoadFilter();
                    loadOrUnloadAlignments(ga,model.getMasterEditorSelectedRange(), filter);
                 }
                 else showMemoryDialog();
            }
        });
        menuAxisHuman = new JMenuItem("Human Curated Features");
        if (isLoadMenu) {
          menuAxisHuman.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, InputEvent.CTRL_MASK, false));
        }
        else {
          menuAxisHuman.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, InputEvent.CTRL_MASK, false));
        }
        menuAxisHuman.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkRange(model.getMasterEditorSelectedRange());
                if (!AnnotationMenu.this.isLoadMenu || loadLimitor.canLoadHumanOverRange(model.getMasterEditorSelectedRange())) {

                  GenomicAxis ga=(GenomicAxis)model.getMasterEditorEntity();

                  // unload the workspace only if the unload of Human curated features because
                  // workspace features can have replaces relationship to promoted features
                  if(
                    !AnnotationMenu.this.isLoadMenu && ga.getGenomeVersion().getWorkspace().getWorkspaceOids().size()>0){
                    // then give the user the option to save his workspace.
                     int ans=JOptionPane.showConfirmDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
                      "Workspace is about to be unloaded.\nWould you like to save the Workspace?","Save Workspace?",JOptionPane.YES_NO_OPTION);
                     if (ans==JOptionPane.YES_OPTION) {
                        XMLWriter.getXMLWriter().saveAsXML();
                     }
                      ModifyManager.getModifyMgr().flushStacks();
                      ga.getGenomeVersion().unloadWorkspace();
                  }

                  LoadFilter filter=ga.getCurationLoadFilter();
                  loadOrUnloadAlignments(ga,model.getMasterEditorSelectedRange(), filter);
                }
                else showMemoryDialog();
            }
        });

        menuFrequentFeatures = new JMenuItem("All Non-Predicted Features");
        if (isLoadMenu) {
          menuFrequentFeatures.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, InputEvent.CTRL_MASK, false));
        }
        else {
          menuFrequentFeatures.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, InputEvent.CTRL_MASK, false));
        }
        menuFrequentFeatures.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkRange(model.getMasterEditorSelectedRange());
                if (!AnnotationMenu.this.isLoadMenu || loadLimitor.canLoadHiPriAndHumanOverRange(model.getMasterEditorSelectedRange())) {
                  GenomicAxis ga=(GenomicAxis)model.getMasterEditorEntity();
                  if(!AnnotationMenu.this.isLoadMenu  && ga.getGenomeVersion().getWorkspace().getWorkspaceOids().size()>0){
                    int ans=JOptionPane.showConfirmDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
                      "Workspace is about to be unloaded.\nWould you like to save the Workspace?","Save Workspace?",JOptionPane.YES_NO_OPTION);
                    if (ans==JOptionPane.YES_OPTION) {
                        XMLWriter.getXMLWriter().saveAsXML();
                  }
                   ModifyManager.getModifyMgr().flushStacks();
                   ga.getGenomeVersion().unloadWorkspace();
                 }
                  loadOrUnloadAlignments(ga,model.getMasterEditorSelectedRange(), ga.getCurationLoadFilter());
                  loadOrUnloadAlignments(ga,model.getMasterEditorSelectedRange(), ga.getHighPriPreComputeLoadFilter());
                  loadOrUnloadAlignments(ga,model.getMasterEditorSelectedRange(), ga.getLowPriPreComputeLoadFilter());
                  loadOrUnloadAlignments(ga,model.getMasterEditorSelectedRange(), ga.getContigLoadFilter());
                }
                else showMemoryDialog();
            }
        });

        add(menuFrequentFeatures);
        add(menuAxisHuman);
        add(menuAxisHiPri);
        add(menuAxisLowPri);
        add(loadContigsMI);
        add(new JSeparator());
        add(computeAxisSpliceSubMenu);
        add(computeAxisStartSubMenu);
        add(computeAxisStopSubMenu);
        model.addBrowserModelListener(browserModelListener);
        boolean isRevComped = false;
        if (model.getModelProperty(BrowserModel.REV_COMP_PROPERTY)!=null)
          isRevComped=((Boolean)model.getModelProperty(BrowserModel.REV_COMP_PROPERTY)).booleanValue();
        else isRevComped=false;
        setButtonSuffix(isRevComped);
    }

    /**
     * Method designed to provide descriptive button names when the user has
     * reverse complemented the axis.
     */
    private void setButtonSuffix(boolean isRevComp) {
      String currentForwardSuffix = new String();
      String currentReverseSuffix = new String();
      String ABOVE_AXIS = " ( Above Axis )";
      String BELOW_AXIS = " ( Below Axis )";
      if (isRevComp) {
        currentForwardSuffix=BELOW_AXIS;
        currentReverseSuffix=ABOVE_AXIS;
      }
      else {
        currentForwardSuffix=ABOVE_AXIS;
        currentReverseSuffix=BELOW_AXIS;
      }

      computeAxisSpliceForwardMI.setText("Forward" + currentForwardSuffix);
      computeAxisSpliceReverseMI.setText("Reverse" + currentReverseSuffix);
      computeAxisStartForwardMI.setText("Forward" + currentForwardSuffix);
      computeAxisStartReverseMI.setText("Reverse" + currentReverseSuffix);
      computeAxisStopForwardMI.setText("Forward" + currentForwardSuffix);
      computeAxisStopReverseMI.setText("Reverse" + currentReverseSuffix);
    }

    private Range checkRange(Range range) {
       if (!isLoadMenu) return range;
       if (range.getMagnitude()>loadingLimit) {
          Range oldRange=model.getMasterEditorSelectedRange();
          Range newRange;
          if (oldRange.isForwardOrientation()) {
            newRange=new Range(oldRange.getStart(),
            oldRange.getStart()+loadingLimit);
          }
          else {
            newRange=new Range(oldRange.getStart(),
            oldRange.getStart()-loadingLimit);
          }
          model.setMasterEditorSelectedRange(newRange);
          return newRange;
       }
       return range;
    }

    public void dispose() {
      model.removeBrowserModelListener(browserModelListener);
    }

    private LoadRequestStatus loadOrUnloadAlignments(Axis axis, Range range, LoadFilter loadFilter) {
       // If the filter is NOT strand specific, we need to always have a forward range...
       if (!loadFilter.isStrandSpecific() && range.isReversed()) {
         // System.out.print("Reversing Range over same start / end: " + range);
         range = range.toReverse();
         // System.out.print(" to: " + range);
       }
       if (isLoadMenu) {
         return axis.loadAlignmentsToEntitiesBackground(new LoadRequest(range,loadFilter,false));
       }
       else {
         return axis.unloadAlignmentsToEntitiesBackground(new LoadRequest(range,loadFilter,true));
       }
    }

    private void showMemoryDialog() {
      JOptionPane.showMessageDialog(SessionMgr.getSessionMgr().getActiveBrowser(),
        "You do not have enough memory remaining to load this request. "+
                "\nSelect a smaller region.");
    }

    private class BrowserModelListener extends BrowserModelListenerAdapter {
      public void modelPropertyChanged(Object key, Object oldValue, Object newValue) {
        if (key.equals(BrowserModel.REV_COMP_PROPERTY)) {
          setButtonSuffix(((Boolean)newValue).booleanValue());
        }
      }

      public void browserMasterEditorEntityChanged(GenomicEntity masterEditorEntity) {
        if (!(masterEditorEntity instanceof GenomicAxis)) {
            setEnabled (false);
        }
      }
    }

    private class MyActiveThreadObserver implements Observer {
        public void update(Observable o, Object arg){
            if (arg instanceof LoadRequestStatus) {
              LoadRequestStatus lrs=(LoadRequestStatus)arg;
              LoadFilter lf=lrs.getLoadFilter();
              GenomicAxis ga=(GenomicAxis)model.getMasterEditorEntity();
              setMenusForLoadFilter(ga,lf,lrs.getLoadRequestState()==LoadRequestStatus.COMPLETE);
            }
        }

        void setMenusForLoadFilter(GenomicAxis ga, LoadFilter lf, boolean enable){
            if (lf == ga.getContigLoadFilter()) {
                loadContigsMI.setEnabled(enable);
                return;
            }
            if (lf==ga.getCurationLoadFilter()) {
                menuAxisHuman.setEnabled(enable);
                return;
            }
            if (lf==ga.getHighPriPreComputeLoadFilter()) {
              menuAxisHiPri.setEnabled(enable);
              return;
            }
            if (lf==ga.getLowPriPreComputeLoadFilter()) {
              menuAxisLowPri.setEnabled(enable);
              return;
            }

            if (lf==ga.getLocallyComputedSpliceSiteLoadFilter(true)) {
              computeAxisSpliceForwardMI.setEnabled(enable);
              return;
            }
            if (lf==ga.getLocallyComputedSpliceSiteLoadFilter(false)) {
              computeAxisSpliceReverseMI.setEnabled(enable);
              return;
            }
            if (lf==ga.getLocallyComputedStartCodonLoadFilter(true)) {
              computeAxisStartForwardMI.setEnabled(enable);
              return;
            }
            if (lf==ga.getLocallyComputedStartCodonLoadFilter(false)) {
              computeAxisStartReverseMI.setEnabled(enable);
              return;
            }
            if (lf==ga.getLocallyComputedStopCodonLoadFilter(true)) {
              computeAxisStopForwardMI.setEnabled(enable);
              return;
            }
            if (lf==ga.getLocallyComputedStopCodonLoadFilter(false)) {
              computeAxisStopReverseMI.setEnabled(enable);
              return;
            }

       }
    }


    private class MenuLoadStatusObserver extends LoadRequestStatusObserverAdapter {
        private JMenuItem menuItem;

        public MenuLoadStatusObserver(JMenuItem menuItem) {
            this.menuItem = menuItem;
            menuItem.setEnabled(false);
        }

        public void stateChanged(LoadRequestStatus loadRequestStatus, LoadRequestState newState){
          if (newState == LoadRequestStatus.COMPLETE) {
            menuItem.setEnabled(true);
            loadRequestStatus.removeLoadRequestStatusObserver(this);
          }
        }
    } //end MenuLoadStatusObserver
}


