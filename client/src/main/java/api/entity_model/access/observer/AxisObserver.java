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
package api.entity_model.access.observer;

import api.entity_model.model.alignment.Alignment;
import api.entity_model.model.fundtype.Axis;
import api.stub.geometry.Range;
import api.stub.sequence.Sequence;

public interface AxisObserver extends AlignableGenomicEntityObserver {

    /**
     * Notification that an alignment has just changed.
     */
    void noteEntityAlignmentChanged(Alignment changedAlignment);

    /**
     * Notification that an entity has just been aligned to the observed axis.
     */
    void noteAlignmentOfEntity(Alignment addedAlignment);

    /**
     * Notification that an entity that was previously aligned to the observed axis has
     * just unaligned from the observed axis.
     * The Alignment will still have references to the Axis and AlignableEntityProxy,
     * but neither the Axis or the AlignableEntityProxy will have this Alignment
     * in thier alignments.
     */
    void noteUnalignmentOfEntity(Alignment removedAlignment);

    /**
     * Notification that a requested sequence has arrived
     */
    void noteSequenceArrived(Axis axis, Range rangeOfSequence, Sequence sequence);
}
