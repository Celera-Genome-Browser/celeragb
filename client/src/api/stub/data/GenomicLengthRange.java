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
package api.stub.data;

public class GenomicLengthRange implements java.io.Serializable
{
    public int minLength;

    public boolean minUnbounded;

    public int maxLength;

    public boolean maxUnbounded;

    public boolean endsInclusive;

    public GenomicLengthRange()
    {
    }

    public GenomicLengthRange
        (int minLength,
        boolean minUnbounded,
        int maxLength,
        boolean maxUnbounded,
        boolean endsInclusive)
    {
        this.minLength = minLength;
        this.minUnbounded = minUnbounded;
        this.maxLength = maxLength;
        this.maxUnbounded = maxUnbounded;
        this.endsInclusive = endsInclusive;
    }

    public int getMinLength() { return minLength; }
    public boolean getMinUnbounded() { return minUnbounded; }
    public int getMaxLength() { return maxLength; }
    public boolean getMaxUnbounded() { return maxUnbounded; }
    public boolean getEndsInclusive() { return endsInclusive; }

    public String toString()
    {
      String str =
        "min " + minLength + " unbounded " + minUnbounded +
        " max " +  maxLength + " unbounded " + maxUnbounded +
        " ends inclusive " + endsInclusive;

      return str;
    }

}
