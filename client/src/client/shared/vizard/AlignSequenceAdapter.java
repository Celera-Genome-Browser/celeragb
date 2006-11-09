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
package client.shared.vizard;

import vizard.genomics.model.SequenceAdapter;

import java.awt.Color;

/**
 * Title:
 * Description:
 * Company:
 * @author
 * @version 1.0
 */

public class AlignSequenceAdapter implements SequenceAdapter {

  String sequence;
  Color color;
  int locationStart=-1;
  int locationEnd=-1;

  public AlignSequenceAdapter(String sequence, Color color, int start, boolean isProtein) {
    int ntScale = isProtein ? 3 : 1;
    this.sequence=sequence.toUpperCase();
    this.color=color;
    this.locationStart=start;
    this.locationEnd=start + ntScale * sequence.length();
  }


  /**
   * This method will initiate the printing of the sequence.
   */
  public void getSequence(int start, int end, SequenceAdapter.SequenceReadyHandler handler) {
     handler.handleNow(sequence, locationStart);
  }

  public void setSequence(String newSequence) {
    this.sequence = newSequence;
  }

  public boolean isForward() {
    return true;
  }

  public Color color() {
    if (color==null) return Color.black;
    return color;
  }

  public int start() {
    return locationStart;
  }
  public int end() {
    return locationEnd;
  }
  public double height() {
    return 10;
  }
}