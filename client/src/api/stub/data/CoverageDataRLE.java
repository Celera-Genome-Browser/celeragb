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

public class CoverageDataRLE implements java.io.Serializable
{

    /* Suggesting this for use in holding information Run-Length-Encoded Coverage data for
       the scaffold aseembly view. This is paired data, with each value having a corresponding
       number indicating how long that value is valid
    */

    private short[] values, lengths;

    public CoverageDataRLE ( short[] values, short[] lengths ) {
	this.values = values;
	this.lengths = lengths;
	if ( values.length != lengths.length ) System.err.println ( "CoverageDataRLE Warning: values[] and lengths[] are not the same size." );
    }

    public short[] getValues () {
	return values;
    }
    
    public short[] getLengths() {
	return lengths;
    }
}
