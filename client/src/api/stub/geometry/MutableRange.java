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


/**
	A mutable version of the Range class. Allows transformation operations.
 */

public class MutableRange extends Range implements java.io.Serializable  {

// Constructors

    public MutableRange(){
    	super();
    }

    public MutableRange( Range other ){
		change( other );
    }

    public MutableRange( int start, int end) {
		super(start, end);
    }

    public MutableRange( int start, int end, IndexingScheme indexingScheme)
    {
	super(start, end, indexingScheme);
    }

    public MutableRange( MutableRange other ){
		change( other );
    }

    public Range toRange()	{ return new Range(this); }


    // transformation operations, which modify this


    /**
     * Reverse the direction of the range "in-place".
     * Start should become End, End Start, and orientation is reversed
     */
    public void reverse(){
      start = this.getEnd();
	    orientation=(byte)-orientation;
    }


    /**
     * Set the orientation explicitely.
     * public static final Orientation REVERSE_ORIENTATION  = new Orientation((byte)-1);
     * public static final Orientation UNKNOWN_ORIENTATION  = new Orientation((byte)0);
     * public static final Orientation FORWARD_ORIENTATION  =  new Orientation((byte)1);
    public void setOrientation(byte newOrientation) {
      // Make sure it is recognized...
      if (!Range.FORWARD_ORIENTATION.isEqual(newOrientation)
          && !Range.UNKNOWN_ORIENTATION.isEqual(newOrientation)
          && !Range.REVERSE_ORIENTATION.isEqual(newOrientation)) return;
      // Set it...
      this.orientation = newOrientation;
    }
     */


    /**
       Rotates the interval 180 degrees about the
       zero point.
     */
    public void rotateAroundOrigin(){
	start = -start;
	orientation=(byte)-orientation;
    }

    /** Mirror (reflect) about a point */
    public void mirror(int point)
    {
    	start = point - start;
	orientation=(byte)-orientation;
    }

    /**
       Moves the range a fixed distance
    */
    public void translate( int distance )
    {
		start += distance;
    }

    public void change( int start, int end ){
		super.start = start;
                int delta = end - start;
		super.magnitude = Math.abs(delta);
                if (delta > 0) orientation = Range.FORWARD_ORIENTATION.getValue();
                else if (delta < 0) orientation = Range.REVERSE_ORIENTATION.getValue();
                else orientation = Range.UNKNOWN_ORIENTATION.getValue();
    }

    public void change( int start, int end, IndexingScheme indexingScheme)
    {
    	if (end < start) // reversed
        {
    		start = (indexingScheme == ZERO_BASED_INDEXING) ? start+1 : start;
    		end =   (indexingScheme == ONE_BASED_INDEXING)  ? end-1 : end;
        }
        else
        {
    		start = (indexingScheme == ONE_BASED_INDEXING)  ? start-1 : start;
    		end = (indexingScheme == ZERO_BASED_INDEXING) ? end+1 : end;
        }
		change(start, end);
    }

    public void change( Range other ){
		start = other.start; magnitude = other.magnitude;
                orientation = other.orientation;
    }

}




