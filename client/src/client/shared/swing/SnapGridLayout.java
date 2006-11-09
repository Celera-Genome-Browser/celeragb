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
package client.shared.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class SnapGridLayout implements LayoutManager2 {
  protected Container target;
  protected int rowCount = 0;
  protected int maxRowHeight = 0;
  protected int maxRowWidth = 0;
  protected int numComponents = 0;
  protected boolean growingToFit = false;

  /**
   * A linked list of ArrayLists that hold references to the children of
   * target.
   */
  protected ArrayList rowList;



  private SnapGridLayout() {
  }

  public SnapGridLayout(Container target){
    rowList = new ArrayList();
    this.target = target;
  }

  /**
   * Move the component and adjust the layout as needed.
   * @param component the component to be moved
   * @param point the location where the component should be moved to.
   */
  public void moveComponent(Component component, Point point){
    Iterator rowItr = rowList.iterator();
    Iterator colItr = rowItr;// so compiler won't complain "not initialized"
    boolean found = false;
    while(rowItr.hasNext() && !found ){
      ArrayList colList = (ArrayList)rowItr.next();
      colItr = colList.iterator();
      while(colItr.hasNext() && !found ){
        Component m = (Component)colItr.next();
        found = m.equals(component);
      }
    }
    if(found)
      colItr.remove();
    int rowInsertIndex = point.y / maxRowHeight;
    rowInsertIndex = Math.min(rowInsertIndex, rowCount);
    if(rowInsertIndex == rowCount){
      rowCount++;
      rowList.add(rowCount - 1, new ArrayList());
    }
    ArrayList colList = (ArrayList)rowList.get(rowInsertIndex);
    for(int i=0; found && i < colList.size(); i++){
      Component m = (Component)colList.get(i);
      int currX = m.getLocation().x;
      int midpoint = m.getWidth() / 2;
      if( point.x < currX + midpoint ){
        colList.add(i,component);
        found = false;
      }
    }
    if(found)// component belongs at end of list
      colList.add(component);
    layoutContainer(target);
  }

  /**
   * Adds the specified component to the layout, using the specified
   * constraint object.
   * @param      comp         the component to be added.
   * @param      constraints  an object that determines how
   *                              the component is added to the layout.
   */
  public void addLayoutComponent(Component comp, Object constraints) {
    if(rowCount == 0){
      rowList.add(new ArrayList() );
      rowCount++;
    }
    ArrayList compList = (ArrayList)rowList.get(Math.max(rowCount-1,0));
    compList.add(comp);
    numComponents++;
    maxRowHeight = Math.max(maxRowHeight,comp.getPreferredSize().height);
  }

  /**
   * Returns the maximum dimensions for this layout given the components
   * in the specified target container.
   * @param target the component which needs to be laid out
   * @see Container
   * @see #minimumLayoutSize(Container)
   * @see #preferredLayoutSize(Container)
   */
  public Dimension maximumLayoutSize(Container target) {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  /**
   * Returns the alignment along the x axis.  This specifies how
   * the component would like to be aligned relative to other
   * components.  The value should be a number between 0 and 1
   * where 0 represents alignment along the origin, 1 is aligned
   * the furthest away from the origin, 0.5 is centered, etc.
   */
  public float getLayoutAlignmentX(Container target) {
    return 0.5f;
  }

  /**
   * Returns the alignment along the y axis.  This specifies how
   * the component would like to be aligned relative to other
   * components.  The value should be a number between 0 and 1
   * where 0 represents alignment along the origin, 1 is aligned
   * the furthest away from the origin, 0.5 is centered, etc.
   */
  public float getLayoutAlignmentY(Container target) {
    return 0.5f;
  }

  /**
   * Invalidates the layout, indicating that if the layout manager
   * has cached information it should be discarded.
   */
  public void invalidateLayout(Container target) {

  }

  /**
   * Adds the specified component with the specified name to the layout.
   * @param      name         the name of the component.
   * @param      comp         the component to be added.
   */
  public void addLayoutComponent(String name, Component comp) {
  }

  /**
     * Removes the specified component from this layout.
     * <p>
     * Most applications do not call this method directly.
     * @param    comp   the component to be removed.
     * @see      java.awt.Container#remove(java.awt.Component)
     * @see      java.awt.Container#removeAll()
   */
  public void removeLayoutComponent(Component comp) {
    Iterator rowItr = rowList.iterator();
    boolean found = false;
    while( !found && rowItr.hasNext() ){
      ArrayList colList = (ArrayList)rowItr.next();
      found = colList.remove(comp);
      if(found){
        numComponents--;
        if( colList.isEmpty() ){// don't need an empty row
          rowItr.remove();
          rowCount--;
        }
      }//end if(found)
    }//end while
  }


  /**
     * Determines the preferred size of the <code>target</code>
     * container using this grid bag layout.
     * <p>
     * Most applications do not call this method directly.
     * @param     target   the container in which to do the layout.
     * @see       java.awt.Container#getPreferredSize
   */
  public Dimension preferredLayoutSize(Container parent) {
    if(!growingToFit)
      return new Dimension(maxRowWidth,rowCount*maxRowHeight);
    else
      return new Dimension(maxRowWidth, (rowCount+1)*maxRowHeight );
  }

  /**
   * When this should grow is set to true, preferredLayout size will be padded
   * by one row, so that dragging below the current visible edge will create space
   * for a new row.
   */
  public void setGrowingToFit(boolean shouldGrow){
    growingToFit = shouldGrow;
  }
  /**
     * Determines the minimum size of the <code>target</code> container
     * using this grid bag layout.
     * <p>
     * Most applications do not call this method directly.
     * @param     target   the container in which to do the layout.
     * @see       java.awt.Container#doLayout
   */
  public Dimension minimumLayoutSize(Container parent) {
    return new Dimension(20,30);
  }

  /**
   * Lays out the specified container using this grid bag layout.
   * This method reshapes components in the specified container in
   * order to satisfy the contraints of this <code>GridBagLayout</code>
   * object.
   * <p>
   * Most applications do not call this method directly.
   * @param parent the container in which to do the layout.
   * @see java.awt.Container
   * @see java.awt.Container#doLayout
   */
  public void layoutContainer(Container parent) {
    resizeComponents();
    Iterator rowItr = rowList.iterator();
    int y=0;
    int x;
    while( rowItr.hasNext() ){
      ArrayList colList = (ArrayList)rowItr.next();
      x = 0;
      for(int i=0; i < colList.size(); i++){
        Component m = (Component)colList.get(i);
        m.setLocation(x, y);
        x += m.getWidth();
      }
      y += maxRowHeight;
    }
    parent.invalidate();
    parent.getParent().doLayout();
    //parent.getParent().repaint();
  }

  protected void resizeComponents(){
    int maxWidth = target.getSize().width - target.getInsets().left -
                   target.getInsets().right;
    //Iterator rowItr = rowList.iterator();
    LinkedList overflowList = new LinkedList();
    for(int rowItr = 0; rowItr < rowList.size(); rowItr++){
      int rowWidth = 0;
      ArrayList colList = (ArrayList)rowList.get(rowItr);
      //handle overflow from previous row
      if( !overflowList.isEmpty() ){
        Iterator overItr = overflowList.iterator();
        while( overItr.hasNext() ){
          //colList.add(overItr.next());
          colList.add(0,overItr.next());
        }
        overflowList.clear();
      }
      if( colList.isEmpty() ){//clean as we go
        rowCount--;
        rowList.remove(rowItr);
      }
      else{
        for(int i=0; i < colList.size(); i++){
          Component m = (Component)colList.get(i);
          if(m.isVisible() ){
            Dimension d = m.getPreferredSize();
            d.height = maxRowHeight;
            m.setSize(d);
            if(!(d.width > maxWidth) && rowWidth + d.width > maxWidth ){
              // move it to front of next row
              overflowList.addFirst(m);
              colList.remove(m);
            }
            else
              rowWidth += d.width;
          }
          else{
            Dimension d = new Dimension(0,0);
            m.setSize(d);
          }
        }
        if( !overflowList.isEmpty() && (rowItr == rowList.size() - 1) ){
          rowList.add(new ArrayList() );
          rowCount++;
        }
      }
      maxRowWidth = Math.max(maxRowWidth, rowWidth);
    }//end for
  }
}