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

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

import vizard.genomics.glyph.FeaturePainter;
import vizard.genomics.glyph.GenomicGlyph;
import vizard.genomics.model.FeatureAdapter;

public class DeletionGlyph extends GenomicGlyph{

  private FeatureAdapter featureAdapter;

  public DeletionGlyph(FeatureAdapter featureAdapter) {
    this.featureAdapter=featureAdapter;
    FeaturePainter fp=new FeaturePainter(featureAdapter);
    addChild(fp);
  }

  public double height() {
    return featureAdapter.height();
  }

  public int end() {
    return featureAdapter.end();
  }

  public int start() {
    return featureAdapter.start();
  }
}