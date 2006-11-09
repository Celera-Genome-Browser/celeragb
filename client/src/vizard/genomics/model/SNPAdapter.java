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

import java.awt.Color;


/**
 * The purpose of SNPAdapter is to specify the interface between
 * an SNP entity belonging to the application and the reusable
 * SNP glyphs.
 */
public interface SNPAdapter extends FeatureAdapter
{
    //the 3 kinds of SNP
    public final static int SUBSTITUTION = 0;
    public final static int INSERTION = 1;
    public final static int DELETION = 2;

    /**
     * Return the kind of SNP (SUBSTITUTION, INSERTION, or DELETION).
     */
    int kind();

    /**
     * Return the color of the head
     */
    Color headColor();
}
