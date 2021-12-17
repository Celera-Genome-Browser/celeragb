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
 ********************************************************************
 * CVS_ID:  $Id$
 */

package client.gui.framework.outline;

import api.entity_model.access.observer.AxisObserverAdapter;
import api.entity_model.access.observer.LoadRequestStatusObserverAdapter;
import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.assembly.GenomicAxis;
import api.entity_model.model.fundtype.*;
import api.entity_model.model.genetics.Chromosome;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class ChromosomeTreeNode extends GenomicEntityTreeNode {
    // JCVI LLF: 10/20/2006
	//	 RT 10/27/2006
    private static final ImageIcon targetIcon = new ImageIcon(Renderer.class.getResource("/resource/client/images/chromosome.gif"));
    private LoadRequestStatus axesLoadStatus;
    private byte sortOrder=ChromosomePopUpMenu.DESCENDING_LENGTH;

    private ArrayList children = new ArrayList();
    private ChromosomePopUpMenu popUpMenu=new ChromosomePopUpMenu(
      ChromosomePopUpMenu.DESCENDING_LENGTH,false,true);
    private Point lastRightClickLoc;
    private JComponent lastComponent;
    private boolean observingLoadStatus=false;
    private ChromosomeObserver chromosomeObserver;
    private boolean limitedView;
    private boolean oldLimitedView;
    private int minValue=0;
    private int maxValue=0;
    private int oldMaxValue=maxValue;
    private int oldMinValue=minValue;

    public ChromosomeTreeNode(Chromosome ge) {
        super(ge);
        chromosomeObserver=new ChromosomeObserver();
        ge.addAxisObserver(chromosomeObserver);
        popUpMenu.addAscendingLengthActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
              if (sortOrder!=ChromosomePopUpMenu.ASCENDING_LENGTH ) {
                  if (sortOrder==ChromosomePopUpMenu.ASCENDING_ORDER ||
                      sortOrder==ChromosomePopUpMenu.DESCENDING_ORDER) {
                        removeFilter();
                        sortOrder=ChromosomePopUpMenu.ASCENDING_LENGTH;
                        displayChildren();
                        return;
                  }
                  sortOrder=ChromosomePopUpMenu.ASCENDING_LENGTH;
                  displayChildren();
              }
           }
        });
        popUpMenu.addDecendingLengthActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
              if (sortOrder!=ChromosomePopUpMenu.DESCENDING_LENGTH ) {
                  if (sortOrder==ChromosomePopUpMenu.ASCENDING_ORDER ||
                      sortOrder==ChromosomePopUpMenu.DESCENDING_ORDER) {
                        removeFilter();
                        sortOrder=ChromosomePopUpMenu.DESCENDING_LENGTH;
                        displayChildren();
                        return;
                  }
                  sortOrder=ChromosomePopUpMenu.DESCENDING_LENGTH;
                  displayChildren();
              }
           }
        });
        popUpMenu.addAscendingOrderActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
              if (sortOrder!=ChromosomePopUpMenu.ASCENDING_ORDER ) {
                  if (sortOrder==ChromosomePopUpMenu.ASCENDING_LENGTH ||
                      sortOrder==ChromosomePopUpMenu.DESCENDING_LENGTH) {
                        removeFilter();
                        sortOrder=ChromosomePopUpMenu.ASCENDING_ORDER;
                        displayChildren();
                        return;
                  }
                  sortOrder=ChromosomePopUpMenu.ASCENDING_ORDER;
                  displayChildren();
              }
           }
        });
        popUpMenu.addDecendingOrderActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
              if (sortOrder!=ChromosomePopUpMenu.DESCENDING_ORDER ) {
                  if (sortOrder==ChromosomePopUpMenu.ASCENDING_LENGTH ||
                      sortOrder==ChromosomePopUpMenu.DESCENDING_LENGTH) {
                        removeFilter();
                        sortOrder=ChromosomePopUpMenu.DESCENDING_ORDER;
                        displayChildren();
                        return;
                  }
                  sortOrder=ChromosomePopUpMenu.DESCENDING_ORDER;
                  displayChildren();
              }
           }
        });
        popUpMenu.addRemoveBoundsActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
            removeFilter();
            displayChildren();
           }
        });

        popUpMenu.addSetBoundsActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
              boolean isSortedByLength = false;
              if (sortOrder==ChromosomePopUpMenu.ASCENDING_LENGTH ||
                  sortOrder==ChromosomePopUpMenu.DESCENDING_LENGTH)
                    isSortedByLength = true;
              oldMaxValue=maxValue;
              oldMinValue=minValue;
              oldLimitedView=limitedView;
              final MagnitudeLimitor limitor=new MagnitudeLimitor((Frame)lastComponent.getTopLevelAncestor(),
                lastRightClickLoc, isSortedByLength);
              limitor.setCurrentHighLimit(maxValue);
              limitor.setCurrentLowLimit(minValue);
              limitor.setMinAndMax(findMaxLowLimit(isSortedByLength),
                                   findMaxHighLimit(isSortedByLength));
              limitor.addHighLimitDocumentListener(new DocumentListener() {
                  public void insertUpdate(DocumentEvent e){
                    update();
                  }
                  public void removeUpdate(DocumentEvent e){
                     update();
                  }
                  public void changedUpdate(DocumentEvent e){
                     update();
                  }
                  public void update(){
                     maxValue=limitor.getHighLimit();
                     minValue=limitor.getLowLimit();
                     limitedView=true;
                     displayChildren();
                  }
              });
              limitor.addLowLimitDocumentListener(new DocumentListener() {
                  public void insertUpdate(DocumentEvent e){
                    update();
                  }
                  public void removeUpdate(DocumentEvent e){
                     update();
                  }
                  public void changedUpdate(DocumentEvent e){
                     update();
                  }
                  public void update(){
                     maxValue=limitor.getHighLimit();
                     minValue=limitor.getLowLimit();
                     limitedView=true;
                     displayChildren();
                  }
              });
              limitor.addOKButtonListener(new ActionListener() {
                  public void actionPerformed(ActionEvent e){
                     maxValue=limitor.getHighLimit();
                     minValue=limitor.getLowLimit();
                     limitedView=true;
                     displayChildren();
                  }
              });
              limitor.addCancelButtonListener(new ActionListener() {
                  public void actionPerformed(ActionEvent e){
                     maxValue=oldMaxValue;
                     minValue=oldMinValue;
                     limitedView=oldLimitedView;
                     displayChildren();
                  }
              });
              limitor.setVisible(true);
           }
        });

    }

    public Icon getNodeIcon() {
      return targetIcon;
    }

    public void loadChildren() {
        LoadRequest request=((Chromosome)userObject).
            getGenomicAxisLoadRequest();
        axesLoadStatus =((Chromosome)userObject).
            loadAlignmentsToEntitiesBackground(request);
        axesLoadStatus.addLoadRequestStatusObserver(
          new MyLoadRequestStatusObserver(),true);
        observingLoadStatus=true;
    }


    public boolean isLeaf() { //if the children are not loaded always appear as branch
        if (!childrenLoaded) return false;
        return super.isLeaf();
    }

    public String toString() {
       if (!limitedView) return super.toString();
       boolean isSortedByLength = false;
       if (sortOrder==ChromosomePopUpMenu.ASCENDING_LENGTH ||
           sortOrder==ChromosomePopUpMenu.DESCENDING_LENGTH)
             isSortedByLength = true;
       if (isSortedByLength) return super.toString()+" (Lengths: "+convertToUnits(minValue)+
        ":"+convertToUnits(maxValue)+")";
       else return super.toString()+" (Positions: "+convertToUnits(minValue)+
        ":"+convertToUnits(maxValue)+")";
    }

    private String convertToUnits(int value) {
      double endValue = (double) value;
      String suffix = new String("");
      if      (endValue >= 0.0     && endValue <  1000.0)
        suffix="b";
      else if (endValue >= 1000.0  && endValue <  1000000.0) {
        suffix = "kb";
        endValue = endValue/1000.0;
      }
      else if (endValue >= 1000000.0) {
        suffix = "Mb";
        endValue = endValue/1000000.0;
      }
      DecimalFormat tFormat = (DecimalFormat) DecimalFormat.getInstance();
      tFormat.applyPattern("0.##");
      String valueText = tFormat.format(endValue);
      return valueText+suffix;
    }

    private void removeFilter() {
      if (limitedView) {
        limitedView=false;
      }
    }

    void receivedRightClick (JComponent component, MouseEvent e) {
        popUpMenu.setFilterOn(this.limitedView);
        popUpMenu.show(component,
          (int)e.getPoint().getX()+10,
          (int)e.getPoint().getY());
        lastComponent=component;
        lastRightClickLoc=new Point (
           (int)(component.getLocationOnScreen().getX()+e.getPoint().getX()),
           (int)(component.getLocationOnScreen().getY()+e.getPoint().getY()));
    }

    private int findMaxHighLimit(boolean isSortedByLength) {
        if (children.isEmpty()) return 0;
        if (isSortedByLength) {
          return Math.max(((Axis)children.get(0)).getMagnitude(),
            ((Axis)children.get(children.size()-1)).getMagnitude());
        }
        else {
          return Math.max(((GenomicAxis)children.get(0)).getOrder(),
            ((GenomicAxis)children.get(children.size()-1)).getOrder());
        }
    }

    private int findMaxLowLimit(boolean isSortedByLength) {
        if (children.isEmpty()) return 0;
        if (isSortedByLength) {
          return Math.min(((Axis)children.get(0)).getMagnitude(),
             ((Axis)children.get(children.size()-1)).getMagnitude());
        }
        else {
          return Math.min(((GenomicAxis)children.get(0)).getOrder(),
             ((GenomicAxis)children.get(children.size()-1)).getOrder());
        }
    }

    private void displayChildren() {
        removeAll();
        Collections.sort(children,new ChildComparator(sortOrder));
        GenomicAxis[] axes=(GenomicAxis[])children.toArray(new GenomicAxis[0]);
        int axisValue;
        int index=0;
        for (int i=0;i<axes.length;i++) {
            if (limitedView) {
               if (sortOrder==ChromosomePopUpMenu.ASCENDING_LENGTH ||
                   sortOrder==ChromosomePopUpMenu.DESCENDING_LENGTH)
                      axisValue=axes[i].getMagnitude();
               else axisValue = axes[i].getOrder();
               if (axisValue<minValue || axisValue>maxValue) continue;
            }
            index++;
            add(new GenomicAxisTreeNode(axes[i]));
        }
        int[] changedIndicies=new int[index];
        for (int i=0;i<index;i++) {
           changedIndicies[i]=i;
        }
        postChildrenAdded(this, changedIndicies);
    }

    private void removeAll() {
       int childCount=getChildCount();
       Object[] children=new Object[childCount];
       int [] indecies=new int[childCount];
       for (int i=0;i<childCount;i++) {
           children[i]=getChildAt(i);
           indecies[i]=i;
       }
       removeAllChildren();
       postChildrenRemoved(this, indecies, children);
    }

    void aboutToBeRemoved(){
       ((GenomicEntity)getUserObject()).removeGenomicEntityObserver(chromosomeObserver);
    }

    class ChromosomeObserver extends AxisObserverAdapter {
        private ChromosomeObserver() {
        }

        public void noteAlignmentOfEntity(Alignment alignment) {
            GenomicEntity ge=alignment.getEntity();
            if (ge instanceof GenomicAxis) {
               //  System.out.println("Heard alignment of: "+ge);
                 children.add(ge);
                 if (!observingLoadStatus) {
                    //Search for the LoadRequestStatus and observe it if found.
                    LoadFilter lf=((Chromosome)getUserObject()).getGenomicAxisLoadFilter();
                    LoadRequestStatus[] requestStatusArray=
                      ActiveThreadModel.getActiveThreadModel().getActiveLoadRequestStatusObjects();
                    for (int i=0;i<requestStatusArray.length;i++) {
                      if (requestStatusArray[i].getLoadFilter().equals(lf)) {
                        requestStatusArray[i].addLoadRequestStatusObserver(
                          new MyLoadRequestStatusObserver(),true);
                        observingLoadStatus=true;
                        return;
                      }
                    }
                    //If you can't find the right LoadRequestStatus, display children
                    displayChildren();
                 }
            }
        }

        public void noteUnalignmentOfEntity(Alignment alignment) {
            GenomicEntity ge=alignment.getEntity();
            if (ge instanceof GenomicAxis) {
                GenomicEntity entity;
                DefaultMutableTreeNode treeNode;
                for (int i = 0; i < getChildCount(); i++) {
                    try {
                        treeNode = (DefaultMutableTreeNode)getChildAt(i);
                        entity = (GenomicEntity)treeNode.getUserObject();
                    }
                    catch(Exception ex) { continue; }
                    if (ge.equals(entity)) {
                        remove(treeNode);
                        children.remove(entity);
                        postChildrenRemoved(ChromosomeTreeNode.this, new int[] { i }, new Object[] { treeNode });
                        //reference to the instance of the Outer (enclosing) class
                        return;
                    }
                }
            }
        }
    }

    class ChildComparator implements Comparator {
        private final byte sortOrder;

        public ChildComparator (byte sortOrder) {
           this.sortOrder=sortOrder;
        }

        public int compare(Object o1, Object o2){
            if (o1 instanceof GenomicAxis && o2 instanceof GenomicAxis) {
              switch (sortOrder) {
                case ChromosomePopUpMenu.ASCENDING_LENGTH:
                  return ((GenomicAxis)o1).getMagnitude()-((GenomicAxis)o2).getMagnitude();
                case ChromosomePopUpMenu.DESCENDING_LENGTH:
                  return ((GenomicAxis)o2).getMagnitude()-((GenomicAxis)o1).getMagnitude();
                case ChromosomePopUpMenu.ASCENDING_ORDER:
                  return ((GenomicAxis)o1).getOrder()-((GenomicAxis)o2).getOrder();
                case ChromosomePopUpMenu.DESCENDING_ORDER:
                  return ((GenomicAxis)o2).getOrder()-((GenomicAxis)o1).getOrder();
                default:
                  return 0;
              }
           }
           else return 0;
        }
    }


    class MyLoadRequestStatusObserver extends LoadRequestStatusObserverAdapter {
        public void stateChanged(LoadRequestStatus loadRequestStatus,
           LoadRequestState newState) {
           //This is notified since the Autonavigator waits for complete and tries to send the
           // setCurrentlSelection.  If this waited for complete, it would not have called displayChildren
           // by the time the setSelection is attempted and the node will not be selected.
           if (newState==LoadRequestStatus.NOTIFIED){
                childrenLoaded = true;
                axesLoadStatus = null;
                observingLoadStatus=false;
                displayChildren();
                loadRequestStatus.removeLoadRequestStatusObserver(this);
            }
        }
    }
}


