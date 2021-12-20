// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Copyright (c) 1999 - 2006 Applera Corporation.
// 301 Merritt 7 
// P.O. Box 5435 
// Norwalk, CT 06856-5435 USA
//
// This is free software; you can redistribute it and/or modify it under the 
// terms of the GNU Lesser General Public License as published by the 
// Free Software Foundation; version 2.1 of the License.
//
// This software is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License 
// along with this software; if not, write to the Free Software Foundation, Inc.
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package client.gui.components.annotation.query_alignment_view;

import client.shared.vizard.AlignmentDNAGlyph;
import vizard.EventDispatcher;
import vizard.Glyph;
import vizard.genomics.glyph.TierNameGlyph;
import vizard.interactor.ClickInteractor;

import java.awt.*;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;



public class AlignNucleotideHighlightController implements ClickInteractor.Adapter
{
    private ClickInteractor interactor;
    private QueryAlignmentView view;

    public AlignNucleotideHighlightController(final QueryAlignmentView view) {
        interactor = new ClickInteractor(this);
        this.view = view;
	EventDispatcher.instance.addInteractor(AlignmentDNAGlyph.class, interactor,
          new EventDispatcher.Filter() {
            public boolean isValid(Glyph glyph) {
              if (glyph == null || glyph.getRootGlyph()==null) return false;
              Component comp = (Component)glyph.getRootGlyph().container();
              return EventDispatcher.hasAncestor(comp, view);
            }
          });
    }

    public void delete() {
	EventDispatcher.instance.removeInteractor(TierNameGlyph.class, interactor);
    }

    //ClickInteractor adapter specialization

    public void clicked(ClickInteractor itor) {
      Point windowLocation = new Point(itor.event().getX(), itor.event().getY());

      Point2D p = new Point2D.Double(windowLocation.getX(), windowLocation.getY());
      try { itor.transform().inverseTransform(p, p); }
      catch(NoninvertibleTransformException ex) {}
      int axisLocation = (int)p.getX();

      view.setNucleotideSelection(new Integer(axisLocation));
    }
}
