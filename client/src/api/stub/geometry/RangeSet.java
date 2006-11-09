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
 *		  Confidential -- Do Not Distribute                  *
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/

package api.stub.geometry;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
*  This class represents a set of ranges.  The set can be bounded by using the
* boundingRange constructor.  Using the boundingRange constructor will modify the
* behaviour as documented in the methods.
*
* One thing to keep in mind is that this class represents multiple intervals
* that are either "on" or "off".  As such, an add operation basically
* turns a region "on", while a remove turns a region "off".  The passed ranges
* on adds are not necessiarily simply stored, unless the whole of the region is
* turned off.  If any part of the added region was previously turned on, the
* passed region will become part of the previous region.  By the same token,
* the remove call will turn the passed region "off", without regards to if the
* passed region instance is actually in the set.  Also, the boolean
* return values indicate modification of the set (as documented in the
* Set API), not addition of the range instance.
*
* All specified ranges are INCLUSIVE of endpoints
*
* Lastly, contains only returns true if the range passed is completely "on",
* partials will return false.
*
* Example (time progresses downward)
*    <pre>
*     method              return
*
*     add (0,10)          true
*     add (20,30)         true
*     iterator            (0,10),(20,30)
*     add (25,35)         true
*     iterator            (0,10),(20,35)
*     add (22,25)         false
*     remove (40,50)      false
*     iterator            (0,10),(20,35)
*     remove (25,30)      true
*     iterator            (0,10),(20,24),(31,35)
*     contains (0,10)     true
*     contains (5,15)     false
*     contains (20,35)    false
*     findGapSet (5,30)    (11,19),(25,30)
*    </pre>
*
*
*  @author  Peter Davies
*
*/

public class RangeSet extends AbstractSet implements java.io.Serializable{

  private TreeSet set=new TreeSet(new RangeComparator()); //backing Set
  private Range boundingRange;
  private static final long serialVersionUID=-3474811494277176980L;

  public RangeSet() {
  }

  public RangeSet(Range boundingRange) {
     if (boundingRange instanceof MutableRange) boundingRange=((MutableRange)boundingRange).toRange();
     this.boundingRange=boundingRange;
  }

    /**
     * Returns the number of ranges in this set (its cardinality).  If this
     * set contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of ranges in this set (its cardinality).
     */
    public int size() {
       return set.size();
    }


    /**
     * Returns an iterator over the ranges in this set.  The ranges are
     * returned in order sorted from lowest to highest.
     *
     * @return an iterator over the ranges in this set, which will always be
     * non-mutable.
     */
    public Iterator iterator(){
       return set.iterator();
    }


    /**
     * Returns aboolean indicating if the range passed is wholly contained in this set.
     *
     * @return boolean indicating whole containment
     */
    public boolean contains(Range range) {
       Range tmpRange;
       for (Iterator it=set.iterator();it.hasNext();) {
          tmpRange=(Range)it.next();
          if (tmpRange.contains(range)) return true;   //range was already in set
       }
       return false;
    }


    // Modification Operations

  public boolean add(Object o) {
    if (o instanceof Range) return add((Range)o);
    return super.add( o);
  }

    /**
     * Adds the specified range to this set if it is not already present.
     * More formally, adds the specified Range,
     * <code>range</code>, to this set if this set contains no element
     * <code>e</code> such that <code>(o==null ? e==null :
     * o.equals(e))</code>.  If this set already contains the specified
     * element, the call leaves this set unchanged and returns <tt>false</tt>.
     * In combination with the restriction on constructors, this ensures that
     * sets never contain duplicate elements.<p>
     *
     * The stipulation above does not imply that sets must accept all
     * elements; sets may refuse to add any particular element, including
     * <tt>null</tt>, and throwing an exception, as described in the
     * specification for <tt>Collection.add</tt>.
     *
     * @param range element to be added to this set.
     * @return <tt>true</tt> if this set did not already contain the specified
     *         element.
     *
     * @throws IllegalArgumentException if null is passed, or if a boundingRange
     *         was set on construction and the passed range exceeds those boundries.
     */
    public boolean add(Range range){
       if (range instanceof MutableRange) range=((MutableRange)range).toRange();
       if (boundingRange!=null && !boundingRange.contains(range)) {
         throw new IllegalArgumentException("Range sent (" + range +
           " to add of RangeSet exceeded the bounding range ("
           + boundingRange + ").");  // new Range fell outside of the boundingRange;
       }
       Range tmpRange;
       //coelesce the ranges
       List intersectingRanges=new ArrayList();
       Object[] ranges=set.toArray();
       for (int i=0;i<ranges.length;i++) {
          tmpRange=(Range)ranges[i];
          if (tmpRange.contains(range)) return false;   //range was already in set
          if (range.contains(tmpRange)) {
             set.remove(tmpRange);  //remove any contained ranges
             continue;
          }
          if (range.intersects(tmpRange)) {
              intersectingRanges.add(tmpRange);  //add intersecting ranges to list
              set.remove(tmpRange);
          }
       }
      for (Iterator it=intersectingRanges.iterator();it.hasNext();){
           range=Range.union(range,(Range)it.next());
       }
       return set.add(range);
    }


    /**
     * Removes the specified range from this set if it is present.
     * More formally, removes an element <code>e</code> such that
     * <code>(o==null ?  e==null : o.equals(e))</code>, if the set contains
     * such an element.  Returns <tt>true</tt> if the set contained the
     * specified range (or equivalently, if the set changed as a result of
     * the call).  (The set will not contain the specified element once the
     * call returns.)
     *
     * @param range Range to be removed from this set, if present.
     * @return true if the set contained the range.
     */
    public boolean remove(Range range) {
       boolean retVal=false;

       if (boundingRange!=null && !boundingRange.contains(range)) {
         throw new IllegalArgumentException("Range sent to remove of RangeSet "+
           "exceeded the bounding range.");  // new Range fell outside of the boundingRange;
       }

       Range tmpRange;
       //coelesce the ranges
       List intersectingRanges=new ArrayList();
       Object[] ranges=set.toArray();
       for (int i=0;i<ranges.length;i++) {
          tmpRange=(Range)ranges[i];
          if (range.contains(tmpRange)) {
             set.remove(tmpRange);  //remove any contained ranges
             retVal=true;
             continue;
          }
          if (range.intersects(tmpRange)) {
              intersectingRanges.add(tmpRange);  //add intersecting ranges to list
              set.remove(tmpRange);
              retVal=true;
          }
       }
       Range originalRange;
       for (Iterator it=intersectingRanges.iterator();it.hasNext();){
            originalRange=(Range)it.next();
            if (!originalRange.contains(range)) {         //intersection is not containment
                Range intersectionRange;
                intersectionRange=Range.intersection(range,originalRange);
                set.add(Range.subtract(originalRange,intersectionRange));
            }
            else {
                set.add(new Range(originalRange.getMinimum(),range.getMinimum()-1));
                set.add(new Range(range.getMaximum()+1,originalRange.getMaximum()));
            }
       }
       return retVal;
    }


   /**
    * Returns a RangeSet that contains ranges that are the gaps in this RangeSet
    * over the bounding range of this RangeSet.
    * The resulting ranges will be in the same orientation as this RangeSet's boundingRange.
    * If this RangeSet does not have a boundingRange, the resulting Range(s) will be forward.
    * This essentially creates an "inverse" set.
    *
    * @param  setNewRangeSetBoundingRange will set the boundingRange of returned RangeSet to
    *         this RangeSet's boundingRange if true.
    * @return Another RangeSet that holds the gaps in the form of ranges.
    */
    public RangeSet findGapSet(boolean setNewRangeSetBoundingRange) {
       // Create the returned RangeSet with the same bounding Range...
       RangeSet newRangeSet;
       if (setNewRangeSetBoundingRange) newRangeSet = new RangeSet(boundingRange);
       else newRangeSet = new RangeSet();

       // See if the gaps should be reversed...
       boolean reverseGaps = false;
       if (boundingRange != null) {
         reverseGaps = boundingRange.isReversed();
       }

       // Check for an empty RangeSet...
       if (this.size()==0) {
         newRangeSet.add(boundingRange);
         return newRangeSet;
       }

       Range lastRange;
       Range thisRange;
       Object[] ranges=set.toArray();

       // Check for the beginning gap...
       if (boundingRange != null) {
         if (boundingRange.getMinimum()<((Range)set.first()).getMinimum()) {
           newRangeSet.add(newRange(boundingRange.getMinimum(),
                                    ((Range)set.first()).getMinimum()-1,
                                    reverseGaps));
         }
       }

       // Check for inner gaps...
       lastRange = (Range)ranges[0];
       for (int i=1; i < ranges.length; i++) {
         thisRange = (Range)ranges[i];
         newRangeSet.add(newRange((lastRange.getMaximum()+1),
                                  (thisRange.getMaximum()-1),
                                  reverseGaps));
         lastRange = thisRange;
       }

       // Check for ending gap...
       if (boundingRange != null) {
         if (boundingRange.getMaximum() > lastRange.getMaximum()) {
           newRangeSet.add(newRange((lastRange.getMaximum()+1),
                                    boundingRange.getMaximum(),
                                    reverseGaps));
         }
       }

       return newRangeSet;
    }


    /**
     * Utility to create a new range instance and conditionally reverse it.
     */
    private Range newRange(int start, int end, boolean reverseIt) {
      if (reverseIt) return new Range(end, start);
      return new Range(start, end);
    }


   /**
    * Returns a RangeSet that contains ranges that are the gaps in this RangeSet
    * over the interval of the passed range.
    *
    * @param range Range of interest to find gaps in
    * @param  setNewRangeSetBoundingRange will set the boundingRange of returned RangeSet to
    *         passed range if true
    * @return Another RangeSet that holds the gaps in the form of ranges
    */
    public RangeSet findGapSubSet(Range range, boolean setNewRangeSetBoundingRange) {
       RangeSet subSetRangeSet = this.findSubSet(range, true);  // Always want.
       RangeSet gapSet = null;
       if (subSetRangeSet != null) gapSet = subSetRangeSet.findGapSet(setNewRangeSetBoundingRange);
       return gapSet;
    }


   /**
    * Returns a RangeSet that contains ranges that are the loaded in this RangeSet
    * over the interval of the passed range.
    *
    * @param range Range of interest for returned subset to cover
    * @return Another RangeSet that holds the subset of loaded ranges in the passed range
    */
    public RangeSet findSubSet(Range range) {
        return this.findSubSet(range, false);
    }


   /**
    * Returns a RangeSet that contains ranges that are the loaded in this RangeSet
    * over the interval of the passed range.
    *
    * @param range Range of interest for returned subset to cover
    * @param  setNewRangeSetBoundingRange will set the boundingRange of returned RangeSet to
    *         passed range if true
    * @return Another RangeSet that holds the subset of loaded ranges in the passed range
    */
    public RangeSet findSubSet(Range range, boolean setNewRangeSetBoundingRange) {
        Range[] ranges=new Range[set.size()];
        set.toArray(ranges);
        RangeSet rangeSet;
        // Create the returned RangeSet...
        if (setNewRangeSetBoundingRange) rangeSet = new RangeSet(range);
        else rangeSet = new RangeSet();
        // Loop through the ranges...
        for (int i=0;i<ranges.length;i++) {
           if (range.contains(ranges[i])) rangeSet.add(ranges[i]);
           else if (range.intersects(ranges[i])) {
             rangeSet.add(Range.intersection(range, ranges[i]));
           }
        }
        return rangeSet;
    }


   /**
    * Returns a Range that is set the bounginRange, or null
    *
    * @return Returns a Range that is set the bounginRange, or null
    */
    public Range getBoundingRange() {
       return boundingRange;
    }

    class RangeComparator implements Comparator,java.io.Serializable {
        public int compare(Object r1, Object r2) {
           if (!(r1 instanceof Range) || !(r2 instanceof Range)) return 0;
           return ((Range)r1).getMinimum() - ((Range)r2).getMinimum();

        }
    }

}

/*
$Log$
Revision 1.18  2002/11/07 18:38:24  lblick
Removed obsolete imports and unused local variables.

Revision 1.17  2002/11/07 16:08:06  lblick
Removed obsolete imports and unused local variables.

Revision 1.16  2001/07/13 13:24:07  pdavies
Hopefully fixes unsupported operation exception on add

Revision 1.15  2001/05/31 21:45:07  schirajt
Changes to the RangeSet so it is sensitive to Range orientation.

Revision 1.14  2001/05/31 15:07:50  schirajt
Changes to the RangeSet API.

Revision 1.13  2001/04/12 21:57:22  schirajt
Added more information to exception thrown.

Revision 1.12  2001/02/06 23:01:17  tsaf
Here we go...

Revision 1.11.2.2  2001/02/06 17:16:52  pdavies
Made serializable

Revision 1.11.2.1  2001/02/06 16:32:22  pdavies
Made serializable

Revision 1.11  2000/09/27 21:44:03  pdavies
New Unloading capable

Revision 1.10  2000/03/31 21:53:26  tsaf
Cleaned out "dead code"

Revision 1.9  2000/03/02 17:57:36  pdavies
Remove debug

Revision 1.8  2000/03/02 15:49:39  pdavies
Fixed the concurrent modification problem

Revision 1.7  2000/03/01 20:20:18  pdavies
Bug fix

Revision 1.6  2000/03/01 19:08:47  pdavies
Fixed

Revision 1.5  2000/02/28 21:32:22  pdavies
Bug fix in findGaps for empty set

Revision 1.4  2000/02/28 20:24:57  pdavies
Fixes


*/