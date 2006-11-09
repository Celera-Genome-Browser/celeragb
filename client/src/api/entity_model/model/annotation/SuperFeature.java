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
package api.entity_model.model.annotation;

import java.util.Collection;


/**
 * Title:        Genome Browser<p>
 * Description:  <p>
 * @author Jay T. Schira
 * @version $Id$
 *
 * A SuperFeature is any feature that can have sub-features.
 */
public interface SuperFeature {
    /**
     * Determine if I will accept a SubFeature as a sub feature of mine.
     * This is usually called right before a call to addSubFeature on the
     * mutator class.
     * Subclasses can over-ride this method to make sure that there is
     * the correct hierarchy of containment... such as Gene, Transcript, Exon.
     * Subclasses should ALWAYS be more restrictive than thier super classes.
     */
    public boolean willAcceptSubFeature(Feature newSubFeature);

    /**
     * Provide the count of the sub features contained by this feature.
     */
    public int getSubFeatureCount();

    /**
     * Provide an immutable version of the collection of sub features
     * contained by this feature.
     */
    public Collection getSubFeatures();

    /**
     * Check to see if a given feature is contained by this feature.
     */
    public boolean containsSubFeature(Feature aSubFeature);

    /**
     * Check to see if all of a collection of feature are contained by this feature.
     */
    public boolean containsAllSubFeatures(Collection someSubFeatures);
}