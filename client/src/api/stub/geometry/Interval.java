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
 *		  Confidential -- Do Not Distribute                           *
 *********************************************************************
 CVS_ID:  $Id$
 *********************************************************************/

package api.stub.geometry;

/**
 * The geometry model allows composition of axes
 * (which is just another interval with no parent) and intervals.
 * An interval is aware of the axis on which it is attached, the position of
 * its origin on that axis, its orientation relative to the orientation of
 * that axis, and its magnitude.
 *
 * Because intervals are also axes, intervals can be attached to other
 * intervals. For an unattached interval (i.e an interval that has no
 * parent interval), only the magnitude is meaningful.
 *
 * @author James Baxendale <jbaxenda>
 * @version 1.0
 */
public interface Interval
{
  public Range getRange();

  public MutableRange getRangeOnAncestor
  (
    Interval ancestor,
    boolean mirrorAxis
  ) throws IllegalArgumentException;

  public MutableRange getRangeOnAncestor
  (
    Interval ancestor,
    Range subRange,
    boolean mirrorAxis
  ) throws IllegalArgumentException;

  public int getMagnitude();

  public void transformRangeToAncestor(Interval ancestor, boolean mirrored, MutableRange rangeInOut);  
}

/*
$Log$
Revision 1.1  2000/02/22 17:58:15  simpsomd
Moved the geometry package

Revision 1.1  2000/02/22 03:14:45  jbaxenda
Interval captures the idea of composed intervals without being
tied to a ProxyInterval implementation.

Revision 1.2  2000/01/18 22:23:23  jbaxenda
Checkin to save intermediate stage of work.

Revision 1.1  2000/01/16 17:12:33  jbaxenda
Start of construction of EJB's for Amgen API

*/

