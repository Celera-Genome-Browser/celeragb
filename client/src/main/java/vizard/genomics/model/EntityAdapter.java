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


/**
 * The purpose of EntityAdapter and its subinterfaces
 * is to specify the interface between a concrete application
 * and the reusable genomic-glyphs.
 *
 * We were careful in letting a concrete application be free in the way
 * it maps its model to the genomic-glyphs. The fact that
 * EntityAdapter is an interface allows many kinds of mapping.
 * For example:
 *   1. a new application might make its model objects implement
 *      the interfaces.
 *   2. another application might create Adapter classes that implement
 *      the interfaces.
 *   3. another application might implement the interfaces in GenomicGlyph
 *      subclasses.
 */
public interface EntityAdapter
{
    /**
     * Return where this entity starts on the genomic axis
     * (the first base on the axis starts at 0)
     */
    int start();

    /**
     * Return where this entity ends on the genomic axis
     * (an entity equal to the very first base would have
     * start = 0 and end = 1)
     */
    int end();

    /**
     * Return the height of the visual representation of this entity.
     */
    double height();
}
