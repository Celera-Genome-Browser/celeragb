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

import vizard.genomics.glyph.GenomicGlyph;
import vizard.genomics.glyph.SequencePainter;
import vizard.genomics.model.SequenceAdapter;

/**
 * Title:
 * Description:
 * Company:
 * @author
 * @version 1.0
 */

public class AlignmentDNAGlyph extends GenomicGlyph {

  private SequenceAdapter queryAdapter;

  public AlignmentDNAGlyph(SequenceAdapter queryAdapter) {
    this.queryAdapter=queryAdapter;
    SequencePainter queryPainter=new SequencePainter(queryAdapter);
    this.addChild(queryPainter);
  }

  public double height() {
    return queryAdapter.height();
  }

  public int end() {
    // Just choosing subjectAdapter range as query wil be the same.
    return queryAdapter.end();
  }

  public int start() {
    // Just choosing subjectAdapter range as query wil be the same.
    return queryAdapter.start();
  }

}