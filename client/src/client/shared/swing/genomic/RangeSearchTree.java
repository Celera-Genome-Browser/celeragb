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
package client.shared.swing.genomic;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This Class implements a K-d Tree for two-dimensional range searches.
 * This structure supports average case range searches approximately O(log N),
 * but unbalanced trees can bring the time up to O(N).  A class should implement
 * the interface <code>RangeSearchable</code> to be able to be stored in this tree.
 * Performance is strongly correlated to tree height, so care should be taken to
 * determine optimal insertion order when possible.
 * @see client.shared.swing.genomic.RangeSearchable
 */
public class RangeSearchTree {

    /**
     * The root node of this <code>RangeSearchTree</code>.
     */
    private RangeSearchNode root = null;

    /**
     * Counter for number of non-deleted nodes
     */
    private long nodeCount = 0;

    /**
     * Empties the tree of all data
     */
    public void clearAll(){
	root = null;
        nodeCount = 0;
    }

    /**
     * Add an item to this <code>RangeSearchTree</code>.
     * @param data the object to be added to the tree.
     */
    public void add( RangeSearchable data) {
        nodeCount++;
	if( root == null ){
	    root = new RangeSearchNode( data );
	    return;
	}

	RangeSearchNode walker = root;
	RangeSearchNode parent = null;
	boolean isToLeft = false;
	int side = RangeSearchable.BEGIN;

	while(walker != null){
	    if( data.getKey(side) < walker.data.getKey(side) )
		{
		    parent = walker;
		    walker = walker.left;
		    isToLeft = true;
		}
	    else
		{
		    parent = walker;
		    walker = walker.right;
		    isToLeft = false;
		}
	    side = 1 - side;
	}
	if( isToLeft)
	    parent.left= new RangeSearchNode( data );
	else
	    parent.right = new RangeSearchNode( data );

    }

    /**
     * Removes data from the tree.
     * @param data The object to be removed from the tree.
     */
    public void remove(RangeSearchable data){
	root = remove(data,root,RangeSearchable.BEGIN);
        if(nodeCount==0)
          root = null;
    }

    /**
     * Helper function for data deletion.  This data-structure implements a lazy
     * deletion strategy, due to the fact that for the current uses single deletion
     * will not occur as frequently as <code>clearALl()</code>
     */
    private RangeSearchNode remove(RangeSearchable data, RangeSearchNode node, int side ){
	if(node == null )
		return null;
	else{
	  if( !node.isDeleted && data.getKey(RangeSearchable.BEGIN) == node.data.getKey(RangeSearchable.BEGIN) &&
             data.getKey(RangeSearchable.END) == node.data.getKey(RangeSearchable.END) &&
	     data.equals(node.data) )
             {
              node.isDeleted = true;
              nodeCount--;
              if(node.left==null && node.right==null)
                return null;
              else
                return node;
             }
          else
            {
              if( data.getKey(side) <= node.data.getKey(side) )
		node.left = remove( data, node.left, 1 - side );
	      else
		node.right = remove( data, node.right, 1 - side);
            }
        }
       return node;
    }

    /**
     * Returns an ArrayList containing all <code>RangeSearchable</code> objects who satisfy the following property
     * low.getKey(BEGIN) <= object.getKey(Begin) <= high.getKey(BEGIN) &&
     *   low.getKey(END) <= object.getKey(END) <= high.getKey(END)
     * @param low a <code>RangeSearchable</code> representing the lower bounds in the
     *        range search.
     * @param high a <code>RangeSearchabel</code> representing the upper bounds in the
     *        range search.
     * @see #findRange(long, long, long, long)
     */
    public ArrayList findRange( RangeSearchable low, RangeSearchable high ){
	return findRange( low, high, root, RangeSearchable.BEGIN, new ArrayList() );
    }

    /**
     * Returns an ArrayList containing all <code>RangeSearchable</code> objects that satisfy
     * the following property: begin_min <= object.getKey(Begin) <= begin_max &&
     * end_min <= object.getKey(END) <= end_max.
     * @param begin_min the minimum value for the RangeSearchable.BEGIN keytype
     * @param begin_max the maximum value for the RangeSearchable.BEGIN keytype
     * @param end_min the minimum value for the RangeSearchable.END keytype
     * @param end_max the maximum value for the RangeSearchable.END keytype
     * @see #findRange(RangeSearchable, RangeSearchable)
     */
    public ArrayList findRange(long begin_min, long begin_max, long end_min, long end_max){
      return findRange( new DefaultSearchable(begin_min,end_min) ,
                        new DefaultSearchable(begin_max,end_max));
    }

    /**
     * Recursive Helper funtion used for both <code>findRange()</code> functions.
     */
    private ArrayList findRange( RangeSearchable low, RangeSearchable high, RangeSearchNode node, int side, ArrayList list ){
	if(node != null ){
	    if( !node.isDeleted &&
                low.getKey(RangeSearchable.BEGIN) <= node.data.getKey(RangeSearchable.BEGIN) &&
		low.getKey(RangeSearchable.END) <= node.data.getKey(RangeSearchable.END) &&
		high.getKey(RangeSearchable.BEGIN) >= node.data.getKey(RangeSearchable.BEGIN) &&
		high.getKey(RangeSearchable.END) >= node.data.getKey(RangeSearchable.END) )
		{
		    list.add(node.data );
		}

	    if( low.getKey(side) <= node.data.getKey(side) )
		findRange( low, high, node.left, 1 - side , list );
	    if( high.getKey(side) >= node.data.getKey(side) )
		findRange( low, high, node.right, 1 - side, list );
	}
	return list;

    }

    /**
     * Returns a view of this RangeSearchTree as an ArrayList. Note: if you modify
     * any of the RangeSearchables in a way that affects the values of their getKey(int)
     * functions, you will corrupt the tree structure, and you should call restoreTree()
     * before using any other <code>RangeSearchTree</code> methods.
     * @return An ArrayList of the <code>RangeSearchable</code> objects in this
     *          <code>RangeSearchTree</code>.
     * @see RangeSearchable
     * @see #restoreTree
     */
    public ArrayList toArrayList(){
      ArrayList list = new ArrayList();
      traverseTree(root,list);
      return list;
    }

    /**
     * Private method that traverses a <code>RangeSearchTree</code> and returns an
     * <code>ArrayList</code> of it's contents.
     * @param node the <code>RangeSearchNode</code> to traverse
     * @param list the <code>ArrayList</code> to store the data in.
     */
    private void traverseTree(RangeSearchNode node, ArrayList list){
      if(node==null)
        return;
      if(!node.isDeleted)
        list.add(node.data);
      traverseTree(node.left,list);
      traverseTree(node.right,list);
    }
    /**
     * Restores corrupted tree structure, tries to increase tree balance as well.
     */
    public void restoreTree(){
      ArrayList al = toArrayList();
      clearAll();
      Collections.shuffle(al);// On average, this should result in pretty balanced trees
      for(int i=0; i< al.size(); i++){
        add((RangeSearchable)al.get(i));
      }
    }
    /**
     * This Class defines the Nodes in a RangeSearchTree
     */
    protected class RangeSearchNode {
	RangeSearchNode left;
	RangeSearchNode right;

	RangeSearchable data;
        boolean isDeleted;

	RangeSearchNode( RangeSearchable r ){
	    data = r;
	    left = null;
	    right = null;
            isDeleted = false;
	}

    }

    /**
     * Default RangeSearchable class used internally by
     * <code>findRange(long, long, long, long)
     * @see #findRange(long, long, long, long)
     */
    private class DefaultSearchable implements RangeSearchable {
      long begin;
      long end;

      public DefaultSearchable(long b, long e ){
        begin = b;
        end = e;
      }
      public long getKey(int keytype){
        if(keytype == RangeSearchable.BEGIN )
          return begin;
        else
          return end;
      }

    }

}
