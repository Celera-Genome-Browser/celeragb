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
package client.gui.components.annotation.axis_annotation;

import api.stub.geometry.Range;
import vizard.EventDispatcher;
import vizard.Glyph;
import vizard.genomics.glyph.AxisRulerGlyph;
import vizard.interactor.ClickInteractor;

import java.awt.Component;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

public class AxisPopupMenuController extends Controller
    implements ClickInteractor.Adapter
{
    private ClickInteractor interactor;
    private AxisRulerGlyph currentAxisGlyph;

    public AxisPopupMenuController(GenomicAxisAnnotationView view) {
	super(view);

        interactor = new ClickInteractor(this);
        interactor.activeWithLeftButton = false;

	EventDispatcher.instance.addInteractor(AxisRulerGlyph.class, interactor,
          new EventDispatcher.Filter() {
            public boolean isValid(Glyph glyph) {
              if (glyph == null || glyph.getRootGlyph()==null) return false;
              Component comp = (Component)glyph.getRootGlyph().container();
              return EventDispatcher.hasAncestor(comp, AxisPopupMenuController.this.view);
            }
        });
    }

    public void delete() {
	EventDispatcher.instance.removeInteractor(AxisRulerGlyph.class, interactor);
        super.delete();
    }

    //ClickInteractor adapter specialization

    public void clicked(ClickInteractor itor) {
      Range viewRange =  view.getBrowserModel().getMasterEditorSelectedRange();
      // Only show popup if a master editor range has been selected.
      if (viewRange==null || viewRange.getMagnitude()==0) return;
      JComponent component = (JComponent)EventDispatcher.instance.root().container();
      JPopupMenu axisMenu = new JPopupMenu();
      for (Iterator it = view.getDataMenuItems().iterator(); it.hasNext();) {
        axisMenu.add((JComponent)it.next());
      }
      axisMenu.show(component, itor.event().getX(), itor.event().getY());
    }
}
