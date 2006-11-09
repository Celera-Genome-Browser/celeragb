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
 *      Confidential -- Do Not Distribute                  *
 *********************************************************************
   CVS_ID:  $Id$
 *********************************************************************/

package api.stub.geometry;

/**
  An immutable, integer, one-dimensional, directed line segment. Like 2D and
  3D line segments in computer graphics, its coordinates are assumed to be
  in a particular coordinate system. Although this class is immutable, its
  mutable version, MutableRange, can be transformed into different coordinate
  systems. Unlike 3D line segments, however, transformations are limited to
  translation and "reversal" (i.e. exchanging the start and end points).
  Ranges that point in a negative direction (i.e. start > end) are considered
  to be "reversed" and are often distinguished when visualized. Magnitude is
  considered, like a vector's magnitude, to be always positive.

  Ranges are always stored using "Space-based indexing". This means that
  although ranges are integer values, they should be treated as
  points on a number line, so that the magnitude is always the difference
  between the start and the end. For example, if the range represented the
  length of a DNA sequence consisting of three nucleotides, its getStart() would
  be the point just to the left of the first base pair, getEnd() would be the
  point just to the right of the third base pair, and getMagnitude() would
  return the value of getEnd() - getStart(), that is, 3.

  There exist at least two alternate indexing schemes in use, which are referred
  to here as "Zero-based indexing" and "One-based indexing". These schemes treat
  the coordinates as being in the middle of the nucleotide pairs, with either 0
  or 1 as the starting index. Additional accessor methods allow Ranges to be
  queried using these alternate schemes.
 */


public class Range implements java.io.Serializable, Cloneable {

   static final long serialVersionUID =  -8096302651398423513L;

   /*
       Indexing schemes. Range coordinates can be queried using any of the
       following three indexing schemes:
    */

   /**
     Treat coordinates as being located in the spaces between nucleotides
       (the default)
   */
   public static final IndexingScheme SPACE_BASED_INDEXING  = new IndexingScheme((byte)-1);

   /**
     Treat coordinates as being located in the middle of nucleotides,
       with 0 as starting index.
   */
   public static final IndexingScheme ZERO_BASED_INDEXING  = new IndexingScheme((byte)0);

   /**
     Treat coordinates as being located in the middle of nucleotides,
       with 1 as starting index.
   */
   public static final IndexingScheme ONE_BASED_INDEXING  =  new IndexingScheme((byte)1);

   /*
      Orientation schemes. Ranges can be stored in the following orientations:
   */

   /**
     Orientation is reversed
   */
   public static final Orientation REVERSE_ORIENTATION  = new Orientation( ( byte )-1 );

   /**
     Orientation is not known
   */
   public static final Orientation UNKNOWN_ORIENTATION  = new Orientation( ( byte )0 );

   /**
     Orientation is forward (the default)
   */
   public static final Orientation FORWARD_ORIENTATION  =  new Orientation( ( byte )1 );

   protected int start;
   protected int magnitude;
   protected byte orientation;


   public Range() {
      start = 0; magnitude = 0; orientation=UNKNOWN_ORIENTATION.getValue();
   }

   public Range( int start, int end ) {
      this.start = start;
      this.magnitude = Math.abs(end-start);
      this.orientation = getOrientation(start, end);
   }

   public Range( int start, int end, byte orientationFlag ) {
      this.start = start;
      this.magnitude = Math.abs( end - start );
      this.orientation = orientationFlag;
   }


   public Range( int start, int magnitude, Orientation orientation) {
      this.start = start;
      this.magnitude=Math.abs(magnitude);
      this.orientation=orientation.getValue();
   }

   public Range( int start, int magnitude, Orientation orientation, IndexingScheme indexingScheme) {
      if ( orientation == REVERSE_ORIENTATION ) { // reversed
         start = (indexingScheme == ZERO_BASED_INDEXING) ? start+1 : start;
      }
      else {
         start = (indexingScheme == ONE_BASED_INDEXING)  ? start-1 : start;
      }
      this.start = start;
      this.magnitude=Math.abs(magnitude);
      this.orientation=orientation.getValue();
   }

   public Range( int start, int end, IndexingScheme indexingScheme) {
      if ( end < start ) { // reversed
         start = (indexingScheme == ZERO_BASED_INDEXING) ? start+1 : start;
         end =   (indexingScheme == ONE_BASED_INDEXING)  ? end-1 : end;
      }
      else {
         start = (indexingScheme == ONE_BASED_INDEXING)  ? start-1 : start;
         end = (indexingScheme == ZERO_BASED_INDEXING) ? end+1 : end;
      }
      this.start = start;
      this.magnitude=Math.abs(end-start);
      this.orientation=getOrientation(start, end);
   }

   public Range( Range other ) {
      start = other.start;
      magnitude = other.magnitude;
      orientation = other.orientation;
   }

   public MutableRange toMutableRange() {
      return (new MutableRange(this));
   }

   public final int getStart() {
      return (start);
   }

   public int getStart(IndexingScheme indexingScheme) {
      if ( isReversed() )
         return(indexingScheme == ZERO_BASED_INDEXING) ? start-1 : start;
      else
         return(indexingScheme == ONE_BASED_INDEXING) ? start+1 : start;
   }

   public final int getEnd() {
      if ( FORWARD_ORIENTATION.isEqual(orientation) ) return (start+magnitude);
      if ( REVERSE_ORIENTATION.isEqual(orientation) ) return (start-magnitude);
      return (start+magnitude); //Not sure this is true, assume forward.
   }

   public int getEnd(IndexingScheme indexingScheme) {
      if ( isReversed() )
         return(indexingScheme == ONE_BASED_INDEXING) ? getEnd()+1 : getEnd();
      else
         return(indexingScheme == ZERO_BASED_INDEXING) ? getEnd()-1 : getEnd();
   }

   public int getMinimum() {
      return (isReversed() ? getEnd() : getStart());
   }

   public int getMinimum(IndexingScheme indexingScheme) {
      return (isReversed() ? getEnd(indexingScheme) : getStart(indexingScheme));
   }

   public int getMaximum() {
      return (isReversed() ? getStart() : getEnd());
   }

   public int getMaximum(IndexingScheme indexingScheme) {
      return (isReversed() ? getStart(indexingScheme) : getEnd(indexingScheme));
   }

   /** Magnitude of range. Always a positive number. */
   public int getMagnitude() {
      return (magnitude);
   }

   /** True if pointing in negative direction */
   public boolean isReversed() {
      return (REVERSE_ORIENTATION.isEqual(orientation));
   }

   /** True if direction is unknown */
   public boolean isUnknownOrientation() {
      return (UNKNOWN_ORIENTATION.isEqual(orientation));
   }

   /** True if pointing in forward direction */
   public boolean isForwardOrientation() {
      return (FORWARD_ORIENTATION.isEqual(orientation));
   }

   public Orientation getOrientation() {
      switch ( orientation ) {
         case -1: return (REVERSE_ORIENTATION);
         case 0: return (UNKNOWN_ORIENTATION);
         case 1: return (FORWARD_ORIENTATION);
      }
      return (null);
   }


   /**
    * Returns a new range which is the reverse "in-place" of this Range.
    * Start will be End, End  will be Start, and orientation  will be reversed.
   */
   public Range toReverse() {
      switch ( orientation ) {
         case -1:
            return (new Range(getEnd(), magnitude,FORWARD_ORIENTATION ));
         case 1:
            return (new Range(getEnd(), magnitude,REVERSE_ORIENTATION ));
         default:
            return (new Range(start, magnitude,UNKNOWN_ORIENTATION ));
      }
   }


   /**
    * present the internal information for printing.
    */
   public String toString() {
      String ret = "";
      ret += "[Start: "+ start;
      ret += " Mag: " + getMagnitude();
      ret += " Orientation: " + orientation;
      ret += "]";
      return (ret);
   }

   /** True if THIS range has a magnitude of zero */
   public boolean isNull() {
      return (magnitude == 0);
   }

   /** True if point is inside THIS range, inclusive */
   public boolean contains(int p) {
      return(!isReversed()) ? (p >= getStart() && p <= getEnd()) :
      (p >= getEnd() && p <= getStart());
   }

   /** True if point is inside THIS range, inclusive */
   public boolean contains(int p, IndexingScheme indexingScheme) {
      int s = getStart(indexingScheme);
      int e = getEnd(indexingScheme);
      return (isReversed() ? (p >= e && p <= s) : (p >= s && p <= e));
   }

   /** True if parameter is completely inside THIS range, inclusive */
   public boolean contains(Range r) {
      return (contains(r.getStart()) && contains(r.getEnd()));
   }

   /** True if parameter intersects THIS range, inclusive */
   public boolean intersects(Range r) {
      return (contains(r.getStart()) || contains(r.getEnd()) || r.contains(this));
   }


   /**
    * @return is a range representing the intersection of rng1 and rng2
    * if rng1 and rng2 do not intersects, intersectingRange will be null
    * return orientation is always forward
    */
   public static Range intersection(Range rng1,Range rng2) {
      if ( !(rng1.intersects(rng2)) ) return (null);
      if ( rng1.contains(rng2) ) return (rng2);
      if ( rng2.contains(rng1) ) return (rng1);
      if ( rng1.getMinimum() < rng2.getMinimum() ) {
         return (new Range(rng2.getMinimum(),rng1.getMaximum()));
      }
      else {
         return (new Range(rng1.getMinimum(),rng2.getMaximum()));
      }

   }

   /**
    * @return is a range representing the union of rng1 and rng2
    * return orientation is always forward
    *
    * Maintain the orientation of the input ranges.
    * If the input ranges have different orientation, use the orientation of the
    * first argument.
    */
   public static Range union(Range rng1,Range rng2) {
      int min, max;
      if ( rng1.contains(rng2) ) return (rng1);
      if ( rng2.contains(rng1) ) return (rng2);
      min = Math.min(rng1.getMinimum(), rng2.getMinimum());
      max = Math.max(rng1.getMaximum(), rng2.getMaximum());
      if ( rng1.isReversed() ) return (new Range(max, min));
      return (new Range(min, max));
   }

   /**
    * @return is a range representing the gap between rng1 and rng2
    * null if intersects
    * return orientation is always forward
    */
   public static Range findGap(Range rng1,Range rng2) {
      if ( rng1.intersects(rng2) ) return (null);
      if ( rng1.getMaximum()<=rng2.getMinimum() ) return (new Range (rng1.getMaximum(),rng2.getMinimum()));
      else return (new Range (rng2.getMaximum(),rng1.getMinimum()));

   }

   /**
    * Subtracts range 2 from range 1, range 2 must be wholly contained in rng1
    * rng1 and rng2 must have 1 endpoint in common, otherwise null returned
    *
    * @return is a range representing the difference of rng1 and rng2
    * return orientation is always forward
    */
   public static Range subtract(Range rng1,Range rng2) {
      if ( !(rng1.contains(rng2)) ) return (null);
      if ( rng1.getMinimum() == rng2.getMinimum() )
         return (new Range(rng2.getMaximum()+1,rng1.getMaximum()));
      if ( rng1.getMaximum() == rng2.getMaximum() )
         return (new Range(rng2.getMinimum(),rng2.getMinimum()-1));
      return (null);
   }

   /**
    *  Orientation of Range rng1 determines orientation of union Range
    *  unionrng is a MutableRange that will hold returned union range
    *
    * Maintain the orientation of the input ranges.
    * If the input ranges have different orientation, use the orientation of the
    * first argument.
    */
   public static void union(Range rng1, Range rng2, MutableRange unionrng) {
      boolean fwd1 = (rng1.getStart() <= rng1.getEnd());
      int min1 = (fwd1 ? rng1.getStart() : rng1.getEnd());
      int max1 = (fwd1 ? rng1.getEnd() : rng1.getStart());

      boolean fwd2 = (rng2.getStart() <= rng2.getEnd());
      int min2 = (fwd2 ? rng2.getStart() : rng2.getEnd());
      int max2 = (fwd2 ? rng2.getEnd() : rng2.getStart());

      int unionmin = (int)Math.min(min1, min2);
      int unionmax = (int)Math.max(max1, max2);

      if ( fwd1 ) {
         unionrng.change(unionmin, unionmax);
      }
      else {
         unionrng.change(unionmax, unionmin);
      }
   }


   /**
    * Hashcode to help with putting Ranges in HashSet's and such.
    * Ranges with the same geometry should return the same hashcode.
    * Adding the start and length doesn't give us much, so we will ignore length.
    * Including the orientation is valuable.
    */
   public int hashCode() {
      // protected int start;
      // protected int magnitude;
      // protected byte orientation;
      if ( this.isReversed() ) return (-start);
      return (start);
   }


   public boolean equals(Object range) {
      if ( !(range instanceof Range) ) return (false);
      Range otherRange=(Range) range;
      if ( otherRange.start==this.start &&
           otherRange.magnitude==this.magnitude  &&
           otherRange.orientation==this.orientation ) return (true);
      return (false);
   }


   public Object clone() {
      return (new Range(this));
   }


   private byte getOrientation(int start, int end) {
      if ( (end-start)>0 ) {
         return (FORWARD_ORIENTATION.getValue());
      }
      if ( (start-end)>0 ) {
         return (REVERSE_ORIENTATION.getValue());
      }
      return (UNKNOWN_ORIENTATION.getValue());
   }

   public static class Orientation {
      private byte orientation;
      private Orientation (byte orientation) {
         this.orientation=orientation;
      }

      private final boolean isEqual(byte value) {
         return (orientation==value);
      }

      final byte getValue() {
         return (orientation);
      }
   }

   protected static class IndexingScheme {
      private byte indexingScheme;
      private IndexingScheme (byte indexingScheme) {
         this.indexingScheme=indexingScheme;
      }

      private final boolean isEqual(byte value) {
         return (indexingScheme==value);
      }

      final byte getValue() {
         return (indexingScheme);
      }
   }
}







