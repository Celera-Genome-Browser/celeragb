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
package vizard.genomics.model;

import vizard.model.WorldViewModel;


/**
 * The purpose of the GenomicAxisViewModel is to provide a reusable
 * WorldViewModel for a genomic axis.
 */
public class GenomicAxisViewModel extends WorldViewModel
{
  private static final double PIXELS_PER_NUCLEOTIDE = 9.0;

    /**
     * Initialize the model with the given genomic axis.
     */
    public GenomicAxisViewModel(int baseCount) {
	super(true, 0, baseCount, 1, 1/PIXELS_PER_NUCLEOTIDE);
    }

    /**
     * Allow construction of model that is a slice of a genomic axis.
     */
    public GenomicAxisViewModel(double axisStart, int baseCount) {
	super(true, axisStart, baseCount, 1, 1/PIXELS_PER_NUCLEOTIDE);
    }

    /**
     * Return the size (in residus) of this genomic axis model.
     */
    public int baseCount() {
	return (int)Math.ceil(worldSize());
    }

    /**
     * Set the number of bases on the genomic axis.
     */
    public void setBaseCount(int baseCount) {
	setWorld(0, baseCount);
        setZoomCenter(baseCount / 2);
    }
}

