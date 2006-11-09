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
package shared.util;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.SortedSet;


/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author       Peter Davies
 * @version      $Id$
 *
 * This class is a high performance set that provides two benefits:
 *
 * Memory Benefit: The first is in memory savings.  It has been found that
 * HashSet and TreeSet can require 4 times the amount of memory necessary to
 * hold small objects.  It is desireable to have a Set that requires little or
 * no additional overhead to hold it's data. To this end, the ComparableSet uses
 * an Object[] as a backing data store, and handles it identically to the way
 * ArrayList handles it (start with size 10 and 50% size increase when maximum
 * room is reached).  This is very efficient, especially if you give a proper
 * initial size.
 *
 * Performance Benefit:  For insertion and retrival, HashSet is rougly 3
 * times faster than TreeSet. This set falls somewhere in the middle, being
 * roughly twice as slow as a HashSet and about 40% faster than TreeSet.  This
 * set also offers linear iteration performance, similar to ArrayList, which
 * cannot be offered by HashSet or TreeSet.
 *
 * Tradeoff:  Sounds good, why didn't Javasoft implement this? Well, the tradeoff
 * is this set can only handle objects that are naturally comparable and support
 * the java.lang.Comparable interface, or a Comparator must be passed to the
 * constructor.  The reason for this is that this set uses a binary search in
 * order to properly postion objects.  A binary search starts in the middle,
 * decides if the searched for element is to the left of right, and then cuts
 * that set in half and selects the middle element.  This is repeated until the
 * element is found.  With a set size of 100,000 elements, this works out to 17
 * comparasons, which is very reasonable.
 *
 * FailFast.  This set, like other Java collections is FailFast, meaning a
 * ConcurrentModificationException will be thrown if you add or remove something
 * from the set while you are iterating.
 */

public class ComparableSet extends AbstractSet implements SortedSet,Cloneable {

  private Object[] backing;
  private int begin,end;
  private int modCount;
  private Comparator comparator;

  /**
   * Creates a ComparableSet with a default backing size of 10.
   */
  public ComparableSet() {
     this(11);
  }

  /**
   * Creates a ComparableSet with a default backing size of 10 and uses the
   * passed comparator for set ordering
   */
  public ComparableSet(Comparator comparator) {
     this();
     this.comparator=comparator;
  }

  /**
   * Creates a ComparableSet with a backing size of the passed int
   */
  public ComparableSet(int size) {
     if (even(size)) size++;  //always make the object array odd size - zero based
     backing=new Object[size];
     end=calculateBackingMid();
     begin=calculateBackingMid();
  }

  /**
   * Creates a ComparableSet with a backing size of the passed int and uses the
   * passed comparator for set ordering
   */
  public ComparableSet(int size,Comparator comparator) {
     this(size);
     this.comparator=comparator;
  }

  /**
   * @return number of objects in this set
   * @see java.util.Collection
   */
  public int size() {
    return end-begin;
  }

  /**
   * @see java.util.Collection
   */
  public void clear() {
     backing=new Object[backing.length];
     begin=calculateBackingMid();
     end=calculateBackingMid();
     modCount++;
  }

  /**
   * @return an Iterator for this set
   * @see java.util.Collection
   */
  public Iterator iterator() {
    return new Itr (modCount);
  }

  /**
   * Higher performance override of the default abstract contains method
   *
   * @see java.util.Collection
   */
  public boolean contains(Object o) {
     return  findObjectOrInsertPosition(o)>=0;
  }

  /**
   * This set will only accept objects that support the comparable interface, unless
   * a Comparator was passed during construction.
   *
   * @return true if set was modified, false if not
   * @throws IllegalStateException - if the object passed is not Comparable and
   *  a Comparator was not passed during construction.
   */
  public boolean add(Object obj) {
    if (comparator==null &&  !(obj instanceof Comparable)) throw new
      IllegalStateException("You cannot add a non-Comparable object "+
      "to this set, unless you constructed it with a Comparator");
    int index=findObjectOrInsertPosition(obj);
    if (index<0) {
      if (ensureCapacity(1)) index=findObjectOrInsertPosition(obj); //recalculate index if the array moved
      int insertPoint=Math.abs(index)-1;
      if (insertPoint>=end) {
        backing[insertPoint]=obj;
        end++;
        modCount++;
        return true;
      }
      if (insertPoint<=begin) {
        backing[insertPoint]=obj;
        begin--;
        modCount++;
        return true;
      }
      if (insertPoint>=calculateBackingMid()) {
         System.arraycopy(backing, insertPoint, backing, insertPoint + 1,
           end - insertPoint);
         backing[insertPoint]=obj;
         end++;
      }
      if (insertPoint<=calculateBackingMid()) {
         System.arraycopy(backing, begin, backing, begin-1,
           insertPoint-begin);
         backing[insertPoint]=obj;
         begin--;
      }
      modCount++;
      return true;
    }
    return false;
  }

  /**
   * Higher performance override of the default abstract remove method
   *
   *  @see java.util.Collection
   *
   *  @throws IllegalStateException - if the object passed is not Comparable and
   *   a Comparator was not passed during construction.
   */
  public boolean remove(Object obj) {
    if (comparator==null &&  !(obj instanceof Comparable)) throw new
      IllegalStateException("You cannot add a non-Comparable object "+
      "to this set, unless you constructed it with a Comparator");
    int index=findObjectOrInsertPosition(obj);
    if (index>=0) {
       remove (index);
       return true;
    }
    return false;
  }

  /**
   * High Performance override of the AbstractCollection.toArray
   *
   * @see jaav.util.AbstractCollection
   */
  public Object[] toArray() {
      Object[] newArray = new Object[size()];
      System.arraycopy(backing,0,newArray,0,size());
      return newArray;
  }

  /**
   * High Performance override of the AbstractCollection.toArray
   *
   * @see java.util.AbstractCollection
   */
  public Object[] toArray(Object a[]) {
      int size=size();
      if (a.length < size)
          a = (Object[])java.lang.reflect.Array.newInstance(
                                a.getClass().getComponentType(), size);

      System.arraycopy(backing,0,a,0,size);
      if (a.length > size)
          a[size] = null;
      return a;
  }

  /**
   * @see java.util.SortedSet
   */
  public Comparator comparator(){
     return comparator;
  }

  /**
   * @see java.util.SortedSet
   */
  public Object first() {
     return backing[0];
  }

  /**
   * @see java.util.SortedSet
   */
  public Object last() {
     return backing[end];
  }

  /**
   * @see java.util.SortedSet
   */
  public SortedSet tailSet(Object fromElement) {
    /* if (comparator==null && !(fromElement instanceof Comparable)) throw  new
      ClassCastException("You cannot get a tailSet using a non-Comparable object "+
      ", unless you constructed this set with a Comparator");
     int index=findObjectOrInsertPosition(fromElement);
     if (index<0) throw new IllegalArgumentException("FromElement not found in this set");
     ComparableSet set=new ComparableSet(calculateDataMid());
     set.comparator=comparator;
     set.begin=0;
     set.end=
     System.arraycopy(backing,index,set.backing,0,set.size());
     return set;*/
     return null;
  }

  /**
   * @see java.util.SortedSet
   */
  public SortedSet headSet(Object toElement) {
/*     if (comparator==null && !(toElement instanceof Comparable)) throw  new
      ClassCastException("You cannot get a tailSet using a non-Comparable object "+
      ", unless you constructed this set with a Comparator");
     int index=findObjectOrInsertPosition(toElement);
     if (index<0) throw new IllegalArgumentException("ToElement not found in this set");
     ComparableSet set=new ComparableSet(index);
     set.comparator=comparator;
     set.size=index;
     System.arraycopy(backing,0,set.backing,0,set.size);
     return set;*/
     return null;
  }

  /**
   * @see java.util.SortedSet
   */
  public SortedSet subSet(Object fromElement,Object toElement) {
    /* if (comparator==null && !(toElement instanceof Comparable) &&
        !(fromElement instanceof Comparable)) throw  new
        ClassCastException("You cannot get a subSet using a non-Comparable object "+
        ", unless you constructed this set with a Comparator");
     int fromIndex=findObjectOrInsertPosition(fromElement);
     if (fromIndex<0) throw new IllegalArgumentException("FromElement not found in this set");
     int toIndex=findObjectOrInsertPosition(toElement)-1;
     if (toIndex<0) throw new IllegalArgumentException("ToElement not found in this set");
     if (fromIndex > toIndex) throw new IllegalArgumentException("FromElement must be less "+
        "than ToElement");
     ComparableSet set=new ComparableSet(toIndex-fromIndex);
     set.comparator=comparator;
     set.size=toIndex-fromIndex;
     System.arraycopy(backing,0,set.backing,0,set.size);
     return set;*/
     return null;
  }

  /**
   * High Performance override of the AbstractCollection clone
   *
   * @return a shallow copy of this set (referenced objects are the same)
   * @see java.lang.Object
   */
  public Object clone() {
    /* ComparableSet set=new ComparableSet(size);
     set.comparator=comparator;
     System.arraycopy(backing, 0, set.backing, 0, size);
     set.modCount = 0;
     return set;*/
     return null;
  }

  private boolean ensureCapacity(int numberToBeAdded) {
     int endFree = backing.length-end-1;
     int beginFree = begin-1;
     float totalFree=(float)beginFree+endFree;
     if (((beginFree/totalFree)<.1) || ((endFree/totalFree)<.1)) {
        recenter();
        return true;
     }
     if (endFree==0 || beginFree==0 || (endFree+beginFree<numberToBeAdded)) {
        resize(numberToBeAdded);
        return true;
     }
     return false;
  }

  private void recenter() {
     System.out.println("Recenter");
     int backingMid=calculateBackingMid();
     int dataMid=calculateDataMid();
     int newBegin=begin+(backingMid-dataMid);
     int dataSize=size();
     System.arraycopy(backing,begin,backing,newBegin,dataSize);
     begin=newBegin;
     end=newBegin+dataSize;
  }

  private void resize(int numberToBeAdded) {
     System.out.println("Resize");
     int oldCapacity = backing.length;
     Object[] oldData = backing;
     int newCapacity = (oldCapacity * 3)/2 + 1;
     if (newCapacity < (oldCapacity+numberToBeAdded))
         newCapacity = oldCapacity+numberToBeAdded;
     int size=size();
     int newMid=(newCapacity/2);
     backing = new Object[newCapacity];
     int newBegin=(newMid-(size/2));
     System.arraycopy(oldData, begin, backing, newBegin, size());
     begin=newBegin;
     end=begin+size;
  }
  /**
   * returns object index if found or -(position to insert - 1)
   */
  private int findObjectOrInsertPosition(Object obj) {
    if (comparator!=null) return binarySearch(backing,obj,begin,end,comparator);
    else return binarySearch(backing,obj,begin,end);
  }

  /**
   * Remove a certain index from the array;
   */
  private void remove (int index) {
      System.arraycopy(backing, index+1, backing, index,
        size() - index-1);
      end--;
      modCount++;
  }

  /**
   * This method does a binary search of a Object[], looking for a given key,
   * limiting it's scope to a maximum length in the array, using a natural ordering
   */
  private int binarySearch(Object[] a, Object key,int begin, int end) {
  //   if (begin==end) return -(begin +1);  //starting case
     int low = begin;
     int high = end-1;
     while (low <= high) {
         int mid =(low + high)/2;
         Object midVal = a[mid];
         int cmp = ((Comparable)midVal).compareTo(key);
         if (cmp < 0)
            low = mid + 1;
         else if (cmp > 0)
            high = mid - 1;
         else
            return mid; // key found
     }
     return -(low + 1);  // key not found.
 }

  /**
   * This method does a binary search of a Object[], looking for a given key,
   * limiting it's scope to a maximum length in the array, using a comparator
   */
  private int binarySearch(Object[] a, Object key,int begin,int end, Comparator comparator) {
     int low = begin;
     int high = end-1;
     while (low <= high) {
         int mid =(low + high)/2;
         Object midVal = a[mid];
         int cmp = comparator.compare(midVal,key);
         if (cmp < 0)
            low = mid + 1;
         else if (cmp > 0)
            high = mid - 1;
         else
            return mid; // key found
     }
     return -(low + 1);  // key not found.
 }

 private int calculateBackingMid() {
    return backing.length/2;
 }

 private int calculateDataMid() {
    return (end+begin)/2;
 }

 private boolean even(int value) {
    return value/2.0==value/2;
 }



 private class Itr implements Iterator {
    private int cursor=begin;
    private int modNumber=0;

    private Itr(int modNumber) {
      this.modNumber=modNumber;
    }

    public boolean hasNext() {
       return cursor<end;
    }

    public Object next() {
       Object obj= backing[cursor];
       checkForComodification();
       cursor++;
       return obj;
    }

    public void remove(){
       ComparableSet.this.remove (cursor);
    }

    private void checkForComodification() {
        if (modCount != modNumber)
            throw new ConcurrentModificationException();
    }

 }
}
