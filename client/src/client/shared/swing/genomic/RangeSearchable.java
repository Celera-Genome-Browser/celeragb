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

/**
 * A Class should implement this interface if it needs to be able
 * to be stored in a <code>RangeSearchTree</code>.
 */
public interface RangeSearchable {

    /**
     * The Key that represents the left endpoint of the range
     * to be stored in a RangeSearchTree
     */
    public static final int BEGIN = 0;

    /**
     * The Key that represents the right endpoint of the range
     * to be stored in a RangeSearchTree
     */
    public static final int END = 1;

    /**
     * Given a desired endpoint type, implementers should return a <code>long</code>
     * that will be used as a key for Range Searches
     * @param endpoint the endpoint type used for key retrieval
     * @see #BEGIN
     * @see #END
     */
    public long getKey(int endpoint);

}
