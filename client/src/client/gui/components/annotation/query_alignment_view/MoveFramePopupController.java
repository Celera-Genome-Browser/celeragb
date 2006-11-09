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
package client.gui.components.annotation.query_alignment_view;

/**
 * Title:        Genome Browser Client
 * Description:  This project is for JBuilder 4.0
 * @author
 * @version $Id$
 */

import client.shared.vizard.DNAComparisonGlyph;
import client.shared.vizard.ProteinComparisonGlyph;
import client.shared.vizard.SubjectAndQueryComparisonGlyph;
import vizard.EventDispatcher;
import vizard.interactor.ClickInteractor;

import java.awt.Point;

import javax.swing.JComponent;

public class MoveFramePopupController implements ClickInteractor.Adapter
{
    private ClickInteractor interactor;
    private QueryAlignmentView view;

    public MoveFramePopupController(QueryAlignmentView view) {
      this.view = view;
      interactor = new ClickInteractor(this);
      interactor.activeWithLeftButton = false;

      EventDispatcher.instance.addInteractor(DNAComparisonGlyph.class,interactor);
      EventDispatcher.instance.addInteractor(ProteinComparisonGlyph.class,interactor);
    }

    public void delete() {
      EventDispatcher.instance.removeInteractor(DNAComparisonGlyph.class,interactor);
      EventDispatcher.instance.removeInteractor(ProteinComparisonGlyph.class,interactor);
    }

    //ClickInteractor adapter specialization

    public void clicked(ClickInteractor itor) {
	SubjectAndQueryComparisonGlyph gg = (SubjectAndQueryComparisonGlyph)itor.glyph();
        JComponent component = (JComponent)EventDispatcher.instance.root().container();
	Point windowLocation = new Point(itor.event().getX(), itor.event().getY());

	view.glyphPopup(component, gg, windowLocation);
    }
}
