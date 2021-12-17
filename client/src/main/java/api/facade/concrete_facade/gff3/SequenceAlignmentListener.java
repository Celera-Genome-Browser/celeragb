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

/**
 * Title:        Genome Browser<p>
 * Description:  Listener interface for when new sequence alignments encountered.<p>
 * @author Les Foster
 * @version $Id$
 */
package api.facade.concrete_facade.gff3;

/**
 * Implementer will react to the discovery of new Sequence Alignment
 * objects.
 */
public interface SequenceAlignmentListener {

  /** Called when alignment found.  */
  public abstract void foundSequenceAlignment(SequenceAlignment sequenceAlignment, int genomeVersionId);

  /**
   * Called when no possibility exists of more alignments being found.
   * The listener should stop listening and/or remove itself when this is fired.
   */
  public abstract void noMoreAlignments(Object source);

} // End interface: SequenceAlignmentListener