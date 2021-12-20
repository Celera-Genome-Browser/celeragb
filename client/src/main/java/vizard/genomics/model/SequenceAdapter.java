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
 * The purpose of SequenceAdapter is to specify the interface between
 * a genomic sequence model specific to an application and the reusable
 * sequence glyphs.
 *
 * Due to memory constraints, an application might not always
 * have the genomic sequence available. For that reason, the following
 * interface provides two working modes: one where the sequence is
 * available when requested, and one where the sequence will be
 * loaded by the application (hopefully in a separate thread:) and
 * the caller will be notified when the sequence is ready.
 */
public interface SequenceAdapter extends FeatureAdapter
{
    /**
     * Define the object to notify once the sequence is ready.
     */
    public static interface SequenceReadyHandler
    {
	/**
	 * This handler method must be called by the SequenceAdapter
	 * with whatever subsequence is available at the time
	 * getSequence was called.
	 */
	void handleNow(String sequence, int start);

	/**
	 * This handler method must be called by the SequenceAdapter
	 * when the remaining sequence gets available.
	 * (note that it is fine to call handleLater in a separate thread)
	 */
	void handleLater(String sequence, int start);
    }

    /**
     * Ask the SequenceAdapter to notify the given handler when
     * the sequence at the given range is ready.
     *
     * The sequence adapter will call now handler.handleNow with
     * whatever subsequences are already available.
     * If some subsequences must be loaded, the adapter has to start
     * the loading process in a separate thread and, when the data
     * gets available, it has to call handler.handleLater.
     */
    void getSequence(int start, int end, SequenceReadyHandler handler);

    boolean isForward();
}
